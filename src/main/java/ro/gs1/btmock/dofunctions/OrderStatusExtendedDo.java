package ro.gs1.btmock.dofunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ro.gs1.btmock.entity.OrderEntity;

@Path("/payment/rest/extendedRegister.do")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class OrderStatusExtendedDo {

    @POST
    public Response getOrderStatusExtended(
            @FormParam("userName") String userName,
            @FormParam("password") String password,
            @FormParam("orderId") String orderId,
            @FormParam("orderNumber") String orderNumber
    ) {
        // --- Minimal request validation (one of orderId/orderNumber must be present)
        if ((orderId == null || orderId.isBlank()) && (orderNumber == null || orderNumber.isBlank())) {
            return okError("1", "OrderId or orderNumber is required");
        }

        // --- Locate the order (orderId has priority)
        OrderEntity order = null;
        if (orderId != null && !orderId.isBlank()) {
            order = OrderEntity.find("orderId", orderId).firstResult();
        }
        if (order == null && orderNumber != null && !orderNumber.isBlank()) {
            order = OrderEntity.find("orderNumber", orderNumber).firstResult();
        }
        if (order == null) {
            return okError("6", "Order not found");
        }

        // === Build response per 6.7.1 (all fields, mandatory + optional) ===
        Map<String, Object> resp = new LinkedHashMap<>();

        // Transport envelope
        resp.put("errorCode", "0");                     // 0 = no system error
        resp.put("errorMessage", "Success");

        // Core
        resp.put("orderNumber", safe(order.orderNumber));
        resp.put("orderStatus", orDefault(order.orderStatus, 0));    // CREATED/DEPOSITED/DECLINED etc.
        resp.put("actionCode", orDefault(order.actionCode, 0));
        resp.put("actionCodeDescription", safe(order.actionCodeDescription));
        resp.put("amount", orDefault(order.amount, 0L));             // minor units
        if (order.currency != null) resp.put("currency", String.valueOf(order.currency));
        // "date" = auth date/time or createdAt if auth missing (epoch millis)
        resp.put("date", order.authDateTime != null ? order.authDateTime : order.createdAt);
        if (order.description != null) resp.put("orderDescription", order.description);
        if (order.ip != null) resp.put("ip", order.ip);

        // cardAuthInfo (only meaningful for paid/attempted)
        Map<String, Object> cardAuthInfo = new LinkedHashMap<>();
        putIfNotNull(cardAuthInfo, "pan", order.cardMaskedPan);
        putIfNotNull(cardAuthInfo, "expiration", order.cardExpiration);     // YYYYMM
        putIfNotNull(cardAuthInfo, "cardholderName", order.cardholderName);
        putIfNotNull(cardAuthInfo, "approvalCode", order.approvalCode);

        // secureAuthInfo (ECI + optional 3DS array)
        Map<String, Object> secureAuthInfo = new LinkedHashMap<>();
        if (order.eci != null) secureAuthInfo.put("eci", order.eci);
        // Example 3DS list: read from jsonParams if you store them there; otherwise omit
        // If you saved CAVV/XID in jsonParams:
        List<Map<String, Object>> threeDSInfo = new ArrayList<>();
        Object cavv = getJson(order, "cavv");
        Object xid  = getJson(order, "xid");
        if (cavv != null || xid != null) {
            Map<String, Object> item = new LinkedHashMap<>();
            if (cavv != null) item.put("cavv", cavv.toString());
            if (xid  != null) item.put("xid",  xid.toString());
            threeDSInfo.add(item);
        }
        if (!threeDSInfo.isEmpty()) secureAuthInfo.put("threeDSInfo", threeDSInfo);
        if (!secureAuthInfo.isEmpty()) cardAuthInfo.put("secureAuthInfo", secureAuthInfo);
        if (!cardAuthInfo.isEmpty()) resp.put("cardAuthInfo", cardAuthInfo);

        // bindingInfo (COF/network token), fill from entity if present
        Map<String, Object> bindingInfo = new LinkedHashMap<>();
        putIfNotNull(bindingInfo, "clientId", order.clientId);
        putIfNotNull(bindingInfo, "bindingId", order.bindingId);
        // Optional brand artwork / BIN / last4 if you store them:
        putIfNotNull(bindingInfo, "cardArtUrl",  strJson(order, "cardArtUrl"));
        putIfNotNull(bindingInfo, "cardArtPicture", strJson(order, "cardArtPicture"));
        putIfNotNull(bindingInfo, "cardArtForegroundColor", strJson(order, "cardArtForegroundColor"));
        putIfNotNull(bindingInfo, "bin", strJson(order, "bin"));
        putIfNotNull(bindingInfo, "panLastFour", strJson(order, "panLastFour"));
        if (!bindingInfo.isEmpty()) resp.put("bindingInfo", bindingInfo);

        // merchantOrderParams[] (name/value list). If you store timings/etc. in jsonParams, project them here.
        List<Map<String, Object>> merchantOrderParams = new ArrayList<>();
        addNameValueIfPresent(merchantOrderParams, "paymentTime", strJson(order, "paymentTime"));
        addNameValueIfPresent(merchantOrderParams, "numberTime",  strJson(order, "numberTime"));
        if (!merchantOrderParams.isEmpty()) resp.put("merchantOrderParams", merchantOrderParams);

        // attributes[] (must include mdOrder)
        List<Map<String, Object>> attributes = new ArrayList<>();
        attributes.add(Map.of("name", "mdOrder", "value", safe(orDefault(order.attributeMdOrder, order.orderId))));
        // add any other attributes you keep in jsonParams, e.g. installments, loyalty, etc.
        addAttributeIfPresent(attributes, "installment", strJson(order, "installment"));
        if (!attributes.isEmpty()) resp.put("attributes", attributes);

        // audit
        if (order.authDateTime != null) resp.put("authDateTime", order.authDateTime);
        if (order.authRefNum != null)  resp.put("authRefNum", order.authRefNum);
        if (order.terminalId != null)  resp.put("terminalId", order.terminalId);

        // paymentAmountInfo
        Map<String, Object> paymentAmountInfo = new LinkedHashMap<>();
        putIfNotNull(paymentAmountInfo, "approvedAmount", order.paymentApprovedAmount);
        putIfNotNull(paymentAmountInfo, "depositedAmount", order.paymentDepositedAmount);
        putIfNotNull(paymentAmountInfo, "refundedAmount", order.paymentRefundedAmount);
        putIfNotNull(paymentAmountInfo, "paymentState", order.paymentState);
        if (!paymentAmountInfo.isEmpty()) resp.put("paymentAmountInfo", paymentAmountInfo);

        // bankInfo (optional – fill if you keep it)
        Map<String, Object> bankInfo = new LinkedHashMap<>();
        putIfNotNull(bankInfo, "bankName",         strJson(order, "bankName"));
        putIfNotNull(bankInfo, "bankCountryCode", strJson(order, "bankCountryCode"));
        putIfNotNull(bankInfo, "bankCountryName", strJson(order, "bankCountryName"));
        if (!bankInfo.isEmpty()) resp.put("bankInfo", bankInfo);

        // orderBundle (echo back the structure your order holds)
        if (order.orderBundle != null) {
            // If you store creation date as millis in extended response, include that; otherwise omit or convert.
            Map<String, Object> bundle = new LinkedHashMap<>();
            Object creationMs = getJson(order, "orderCreationDateMs");
            if (creationMs instanceof Number) {
                bundle.put("orderCreationDate", ((Number) creationMs).longValue());
            }
            // You can serialize the existing object directly if it’s Jackson-friendly:
            // If not, manually map the parts you need as in previous examples.
            // For simplicity, just put the whole object (Jackson will serialize nested fields).
            bundle.put("customerDetails", order.orderBundle.customerDetails);
            resp.put("orderBundle", bundle);
        }

        // chargeback flag (if you keep it; default false)
        Object chargeback = getJson(order, "chargeback");
        resp.put("chargeback", chargeback instanceof Boolean ? (Boolean) chargeback : Boolean.FALSE);

        // refunds[] (history list) – if you keep them in jsonParams or a dedicated field
        Object refundsObj = getJson(order, "refunds");
        if (refundsObj instanceof List) {
            // assume already list of map-like objects
            resp.put("refunds", refundsObj);
        } else {
            resp.put("refunds", Collections.emptyList());
        }

        return Response.ok(resp).build();
    }

    // -------- helpers --------

    private static Response okError(String code, String message) {
        return Response.ok(Map.of("errorCode", code, "errorMessage", message)).build();
    }

    private static <T> T orDefault(T v, T def) {
        return v != null ? v : def;
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static void putIfNotNull(Map<String, Object> m, String k, Object v) {
        if (v != null) m.put(k, v);
    }

    private static void addNameValueIfPresent(List<Map<String, Object>> list, String name, String value) {
        if (value != null) list.add(Map.of("name", name, "value", value));
    }

    private static void addAttributeIfPresent(List<Map<String, Object>> list, String name, String value) {
        if (value != null) list.add(Map.of("name", name, "value", value));
    }

    private static Object getJson(OrderEntity order, String key) {
        return (order.jsonParams != null) ? order.jsonParams.get(key) : null;
    }

    private static String strJson(OrderEntity order, String key) {
        Object v = getJson(order, key);
        return v != null ? String.valueOf(v) : null;
    }
}
