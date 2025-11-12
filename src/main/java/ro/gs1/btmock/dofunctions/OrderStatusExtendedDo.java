package ro.gs1.btmock.dofunctions;

import java.util.ArrayList;
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

@Path("/payment/rest/getOrderStatusExtended.do")
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
        if ((orderId == null || orderId.isBlank()) && (orderNumber == null || orderNumber.isBlank())) {
            return okError("1", "OrderId or orderNumber is required");
        }

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

     // === Build response per examples (grouping & order) ===
        Map<String, Object> resp = new LinkedHashMap<>();

        // 1) envelope + core (in the same order as examples)
        resp.put("errorCode", "0");
        resp.put("errorMessage", "Success");

        putIfNotNull(resp, "orderNumber", order.orderNumber);
        putIfNotNull(resp, "orderStatus", order.orderStatus);
        putIfNotNull(resp, "actionCode", order.actionCode);
        putIfNotNull(resp, "actionCodeDescription", order.actionCodeDescription);
        putIfNotNull(resp, "amount", order.amount);
        if (order.currency != null) resp.put("currency", String.valueOf(order.currency));
        resp.put("date", order.authDateTime != null ? order.authDateTime : order.createdAt);
        putIfNotNull(resp, "orderDescription", order.description);
        putIfNotNull(resp, "ip", order.ip);

        if (order.merchantOrderParams != null && !order.merchantOrderParams.isEmpty()) {
            resp.put("merchantOrderParams", order.merchantOrderParams);
        }

        {
            List<Map<String, Object>> attributes = new ArrayList<>();
            String mdOrder = (order.attributeMdOrder != null) ? order.attributeMdOrder : order.orderId;
            attributes.add(Map.of("name", "mdOrder", "value", mdOrder));
            resp.put("attributes", attributes);
        }

        {
            Map<String, Object> cardAuthInfo = new LinkedHashMap<>();
            putIfNotNull(cardAuthInfo, "expiration", order.cardExpiration);
            putIfNotNull(cardAuthInfo, "cardholderName", order.cardholderName);
            putIfNotNull(cardAuthInfo, "approvalCode", order.approvalCode);
            putIfNotNull(cardAuthInfo, "pan", order.cardMaskedPan);
            if (!cardAuthInfo.isEmpty()) {
                resp.put("cardAuthInfo", cardAuthInfo);
            }
        }

        // 5) auth fields (after cardAuthInfo, exactly like examples)
        putIfNotNull(resp, "authDateTime", order.authDateTime);
        putIfNotNull(resp, "terminalId", order.terminalId);
        putIfNotNull(resp, "authRefNum", order.authRefNum);

        // 6) paymentAmountInfo (paymentState, approvedAmount, depositedAmount, refundedAmount)
        {
            Map<String, Object> paymentAmountInfo = new LinkedHashMap<>();
            putIfNotNull(paymentAmountInfo, "paymentState", order.paymentState);
            putIfNotNull(paymentAmountInfo, "approvedAmount", order.paymentApprovedAmount);
            putIfNotNull(paymentAmountInfo, "depositedAmount", order.paymentDepositedAmount);
            putIfNotNull(paymentAmountInfo, "refundedAmount", order.paymentRefundedAmount);
            if (!paymentAmountInfo.isEmpty()) {
                resp.put("paymentAmountInfo", paymentAmountInfo);
            }
        }

        // 7) bankInfo (bankName, bankCountryCode, bankCountryName)
        {
            Map<String, Object> bankInfo = new LinkedHashMap<>();
            putIfNotNull(bankInfo, "bankName", order.bankName);
            putIfNotNull(bankInfo, "bankCountryCode", order.bankCountryCode);
            putIfNotNull(bankInfo, "bankCountryName", order.bankCountryName);
            if (!bankInfo.isEmpty()) {
                resp.put("bankInfo", bankInfo);
            }
        }

        // 8) orderBundle (orderCreationDate + customerDetails.email/phone + deliveryInfo + billingInfo)
        if (order.orderBundle != null) {
            Map<String, Object> bundle = new LinkedHashMap<>();

            // orderCreationDate in examples is a long (epoch millis). If you store it in jsonParams:
            Object creationMs = getJson(order, "orderCreationDate");
            if (creationMs instanceof Number) {
                bundle.put("orderCreationDate", ((Number) creationMs).longValue());
            }

            if (order.orderBundle.customerDetails != null) {
                Map<String, Object> cust = new LinkedHashMap<>();
                putIfNotNull(cust, "email", order.orderBundle.customerDetails.email);
                putIfNotNull(cust, "phone", order.orderBundle.customerDetails.phone);

                // deliveryInfo
                if (order.orderBundle.customerDetails.deliveryInfo != null) {
                    Map<String, Object> delivery = new LinkedHashMap<>();
                    putIfNotNull(delivery, "deliveryType", order.orderBundle.customerDetails.deliveryInfo.deliveryType);
                    putIfNotNull(delivery, "country",     order.orderBundle.customerDetails.deliveryInfo.country);
                    putIfNotNull(delivery, "city",        order.orderBundle.customerDetails.deliveryInfo.city);
                    putIfNotNull(delivery, "postAddress", order.orderBundle.customerDetails.deliveryInfo.postAddress);
                    putIfNotNull(delivery, "postalCode",  order.orderBundle.customerDetails.deliveryInfo.postalCode);
                    if (!delivery.isEmpty()) {
                        cust.put("deliveryInfo", delivery);
                    }
                }

                // billingInfo
                if (order.orderBundle.customerDetails.billingInfo != null) {
                    Map<String, Object> billing = new LinkedHashMap<>();
                    putIfNotNull(billing, "country",      order.orderBundle.customerDetails.billingInfo.country);
                    putIfNotNull(billing, "city",         order.orderBundle.customerDetails.billingInfo.city);
                    putIfNotNull(billing, "postAddress",  order.orderBundle.customerDetails.billingInfo.postAddress);
                    putIfNotNull(billing, "postAddress2", order.orderBundle.customerDetails.billingInfo.postAddress2);
                    putIfNotNull(billing, "postAddress3", order.orderBundle.customerDetails.billingInfo.postAddress3);
                    putIfNotNull(billing, "postalCode",   order.orderBundle.customerDetails.billingInfo.postalCode);
                    if (!billing.isEmpty()) {
                        cust.put("billingInfo", billing);
                    }
                }

                if (!cust.isEmpty()) {
                    bundle.put("customerDetails", cust);
                }
            }
            if (!bundle.isEmpty()) {
                resp.put("orderBundle", bundle);
            }
        }

        // (Optional blocks you can add before returning, if you keep them: bindingInfo, chargeback, refunds)
        // But your three examples don’t include them, so we keep the output identical in grouping.

        return Response.ok(resp).build();

    }

    // -------- helpers --------

    private static Response okError(String code, String message) {
        return Response.ok(Map.of("errorCode", code, "errorMessage", message)).build();
    }

    private static void putIfNotNull(Map<String, Object> m, String k, Object v) {
        if (v != null) m.put(k, v);
    }

    private static Object getJson(OrderEntity order, String key) {
        return (order.jsonParams != null) ? order.jsonParams.get(key) : null;
    }
}
