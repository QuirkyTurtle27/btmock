package ro.gs1.btmock.dofunctions;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ro.gs1.btmock.entity.OrderEntity;

@Path("/payment/rest/refund.do")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class RefundDo {

    @POST
    public Response refund(
            @FormParam("userName") String userName,
            @FormParam("password") String password,
            @FormParam("orderId") String orderId,
            @FormParam("orderNumber") String orderNumber,
            @FormParam("amount") String amountStr
    ) {
        // --- Basic validations (envelope-style responses) ---
        if ((orderId == null || orderId.isBlank()) && (orderNumber == null || orderNumber.isBlank())) {
            return okError("4", "Missing parameter: orderId or orderNumber");
        }
        if (amountStr == null || amountStr.isBlank()) {
            return okError("4", "Missing parameter: amount");
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
            if (amount <= 0) return okError("5", "Invalid amount");
        } catch (NumberFormatException nfe) {
            return okError("5", "Invalid amount");
        }

        // (Optional) auth check can be added here based on userName/password.

        // --- Locate order (orderId has priority) ---
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

        if (order.orderStatus == null ||
        	    !(order.orderStatus == 2 || order.orderStatus == 7)) {
        	    return okError("7", "Refund not allowed for this order status");
        	}

        // --- Business rules ---
        long deposited = order.paymentDepositedAmount != null ? order.paymentDepositedAmount : 0L;
        long alreadyRefunded = order.paymentRefundedAmount != null ? order.paymentRefundedAmount : 0L;
        long refundable = Math.max(deposited - alreadyRefunded, 0L);

        if (refundable <= 0) {
            return okError("5", "Nothing to refund");
        }
        if (amount > refundable) {
            return okError("5", "Refund exceeds refundable amount");
        }

        long newRefunded = alreadyRefunded + amount;
        order.paymentRefundedAmount = newRefunded;

        // Update payment state & orderStatus
        if (newRefunded == deposited) {
            // Full refund
            order.paymentState = "REFUNDED";
            order.orderStatus = 4; // REFUNDED
            order.status = "REFUNDED";
        } else {
            order.paymentState = "PARTIALLY_REFUNDED";
            order.orderStatus = 7; // PARTIALLY_REFUNDED
            order.status = "PARTIALLY_REFUNDED";
        }

        Map<String, Object> refundItem = new LinkedHashMap<>();
        refundItem.put("referenceNumber", "RF-" + UUID.randomUUID());
        refundItem.put("actionCode", "000");
        refundItem.put("amount", amount);
        refundItem.put("date", isoInstantNow());

        if (order.refunds == null) {
            order.refunds = new ArrayList<>();
        }
        order.refunds.add(refundItem);

        order.persistOrUpdate();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("errorCode", "0");
        resp.put("errorMessage", "Success");
        return Response.ok(resp).build();
    }

    private static Response okError(String code, String message) {
        return Response.ok(Map.of("errorCode", code, "errorMessage", message)).build();
    }

    private static String isoInstantNow() {
        return DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }
}

