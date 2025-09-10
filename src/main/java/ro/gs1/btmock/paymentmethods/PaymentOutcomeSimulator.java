package ro.gs1.btmock.paymentmethods;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import ro.gs1.btmock.entity.OrderEntity;
import ro.gs1.btmock.enums.CardOutcomeEnum;

import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class PaymentOutcomeSimulator {

    @Inject
    Logger log;

    // ---------- Public API ----------

    /** Simulate a payment using orderId (preferred). */
    public boolean simulateByOrderId(String orderId, String pan, String expiry, String cvv, String cardholderName) {
        OrderEntity order = OrderEntity.find("orderId", orderId).firstResult();
        if (order == null) {
            log.errorf("simulateByOrderId: order not found (orderId=%s)", orderId);
            return false;
        }
        return applyOutcome(order, pan, expiry, cvv, cardholderName);
    }

    /** Simulate a payment using orderNumber (fallback). */
    public boolean simulateByOrderNumber(String orderNumber, String pan, String expiry, String cvv, String cardholderName) {
        OrderEntity order = OrderEntity.find("orderNumber", orderNumber).firstResult();
        if (order == null) {
            log.errorf("simulateByOrderNumber: order not found (orderNumber=%s)", orderNumber);
            return false;
        }
        return applyOutcome(order, pan, expiry, cvv, cardholderName);
    }

    // ---------- Core logic ----------

    private boolean applyOutcome(OrderEntity order, String pan, String expiryRaw, String cvv, String cardholderName) {
        // 1) Determine outcome from PAN last-4 using your enum
        CardOutcomeEnum outcome = CardOutcomeEnum.fromPan(pan);

        // 2) If enum says SUCCESS, run additional realistic validation to possibly turn it into a decline
        if (outcome == CardOutcomeEnum.SUCCESS) {
            // CVV must be 3-4 digits
            if (cvv == null || !cvv.matches("\\d{3,4}")) {
                outcome = CardOutcomeEnum.INVALID_CVV; // 871
            } else {
                String normalizedExpiry = normalizeExpiry(expiryRaw); // YYYYMM or null if invalid format
                if (normalizedExpiry == null) {
                    outcome = CardOutcomeEnum.INVALID_EXPIRY; // 861
                } else if (isExpired(normalizedExpiry)) {
                    outcome = CardOutcomeEnum.CARD_EXPIRED; // 906
                }
            }
        }

        // 3) Update OrderEntity extended-status fields
        setCommonCardFields(order, pan, expiryRaw, cardholderName);

        if (outcome == CardOutcomeEnum.SUCCESS) {
            applySuccess(order);
        } else {
            applyDecline(order, outcome);
        }

        order.persistOrUpdate();
        log.debugf("Payment simulation applied: orderId=%s, status=%s, actionCode=%s",
                order.orderId, order.orderStatus, order.actionCode);
        return true;
    }

    // ---------- Field setters ----------

    private void applySuccess(OrderEntity order) {
        long amt = Objects.requireNonNullElse(order.amount, 0L);

        order.actionCode = 0;
        order.actionCodeDescription = "Request processed successfully";
        order.orderStatus = 2; // DEPOSITED for 1-phase
        order.status = "DEPOSITED";

        // paymentAmountInfo
        order.paymentApprovedAmount = amt;
        order.paymentDepositedAmount = amt;
        order.paymentRefundedAmount = 0L;
        order.paymentState = "DEPOSITED";

        // cardAuthInfo
        order.approvalCode = "627744";               // mock approval
        order.eci = 5;                                // mock ECI (3DS success)

        // audit/attributes
        order.authDateTime = System.currentTimeMillis();
        order.authRefNum = "002311" + order.approvalCode;
        if (order.attributeMdOrder == null) {
            order.attributeMdOrder = order.orderId;  // mdOrder
        }

        // terminal/ip (leave existing if already set)
        if (order.terminalId == null) order.terminalId = "TST0000001";
        if (order.ip == null)         order.ip = "127.0.0.1";
    }

    private void applyDecline(OrderEntity order, CardOutcomeEnum outcome) {
        order.actionCode = outcome.getActionCode();
        order.actionCodeDescription = outcome.getDescription();
        order.orderStatus = 6; // DECLINED
        order.status = "DECLINED";

        // Declines have zeroed amounts / state
        order.paymentApprovedAmount = 0L;
        order.paymentDepositedAmount = 0L;
        order.paymentRefundedAmount = 0L;
        order.paymentState = "DECLINED";

        // Approval/ECI empty
        order.approvalCode = "000000";
        order.eci = null;

        // audit/attributes
        order.authDateTime = System.currentTimeMillis();
        order.authRefNum = "002311095612";
        if (order.attributeMdOrder == null) {
            order.attributeMdOrder = order.orderId;
        }

        if (order.terminalId == null) order.terminalId = "TST0000001";
        if (order.ip == null)         order.ip = "127.0.0.1";
    }

    private void setCommonCardFields(OrderEntity order, String pan, String expiryRaw, String cardholderName) {
        order.cardMaskedPan = maskPan(pan);              // ****-masked pan
        order.cardExpiration = normalizeExpiry(expiryRaw); // YYYYMM or null
        order.cardholderName = cardholderName != null ? cardholderName : order.cardholderName;

        // Ensure mdOrder is in attributes bucket (the response builder will emit it)
        if (order.attributeMdOrder == null) {
            order.attributeMdOrder = order.orderId != null ? order.orderId : UUID.randomUUID().toString();
        }
        // Keep createdAt if not set
        if (order.createdAt == 0L) order.createdAt = System.currentTimeMillis();
    }

    // ---------- Helpers ----------

    /** Masks PAN as 6 + * + last4 (e.g., 411111******1111). */
    private String maskPan(String pan) {
        if (pan == null) return null;
        String digits = pan.replaceAll("\\s", "");
        if (digits.length() < 10) return "********";
        String first6 = digits.substring(0, Math.min(6, digits.length()));
        String last4  = digits.substring(Math.max(digits.length() - 4, 0));
        StringBuilder sb = new StringBuilder(first6);
        int stars = Math.max(digits.length() - (first6.length() + last4.length()), 0);
        for (int i = 0; i < stars; i++) sb.append('*');
        sb.append(last4);
        return sb.toString();
    }

    /**
     * Accepts "MM/YY", "MMYY", or "YYYYMM".
     * Returns normalized "YYYYMM" or null if invalid format.
     */
    private String normalizeExpiry(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.replaceAll("[^0-9]", "");
        if (s.length() == 4) { // MMYY
            String mm = s.substring(0, 2);
            String yy = s.substring(2, 4);
            if (!mm.matches("0[1-9]|1[0-2]")) return null;
            int year = 2000 + Integer.parseInt(yy);
            return year + mm;
        } else if (s.length() == 6) { // MMYYYY or YYYYMM
            String a = s.substring(0, 2);
            String b = s.substring(2, 6);
            if (a.matches("0[1-9]|1[0-2]")) {
                // MMYYYY -> YYYYMM
                return b + a;
            } else {
                // assume YYYYMM
                String mm = s.substring(4, 6);
                if (!mm.matches("0[1-9]|1[0-2]")) return null;
                return s;
            }
        }
        return null;
        }

    /** True if the YYYYMM expiry is strictly before current month. */
    private boolean isExpired(String yyyymm) {
        if (yyyymm == null || !yyyymm.matches("\\d{6}")) return true;
        int year = Integer.parseInt(yyyymm.substring(0, 4));
        int month = Integer.parseInt(yyyymm.substring(4, 6));
        YearMonth exp = YearMonth.of(year, month);
        return exp.isBefore(YearMonth.now());
    }
}
