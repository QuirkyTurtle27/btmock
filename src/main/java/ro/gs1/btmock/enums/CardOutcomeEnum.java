package ro.gs1.btmock.enums;

public enum CardOutcomeEnum {

    // 22 Decline Codes
    CARD_RESTRICTED("0104", 104, "Card restricționat"),
    RESTRICTED_BY_AUTH("0124", 124, "Tranzacția restricționată de autorități/reglementări"),
    INACTIVE_CARD("0320", 320, "Card inactiv. Vă rugăm activați cardul."),
    ISSUER_UNAVAILABLE("0801", 801, "Emitent indisponibil"),
    CARD_BLOCKED("0803", 803, "Card blocat. Nu reîncercați cu același card"),
    TRANSACTION_NOT_ALLOWED("0804", 804, "Tranzacția nu este permisă. Nu reîncercați cu același card"),
    TRANSACTION_DENIED("0805", 805, "Tranzacție respinsă"),
    INVALID_EXPIRY("0861", 861, "Dată expirare card greșită"),
    INVALID_CVV("0871", 871, "CVV greșit"),
    INVALID_CARD("0905", 905, "Card invalid"),
    CARD_EXPIRED("0906", 906, "Card expirat"),
    INVALID_TRANSACTION("0913", 913, "Tranzacție invalidă. Nu reîncercați cu același card"),
    INVALID_ACCOUNT("0914", 914, "Cont invalid"),
    INSUFFICIENT_FUNDS("0915", 915, "Fonduri insuficiente"),
    LIMIT_EXCEEDED("0917", 917, "Limită tranzacționare depășită"),
    FRAUD_SUSPECTED("0952", 952, "Suspect de fraudă"),
    INSTALLMENTS_NOT_ALLOWED("0998", 998, "Rate nepermise cu acest card"),

    _3DS2_DECLINED_ARES("6016", 341016, "3DS2 ARes declined (issuer)"),
    _3DS2_ARES_UNKNOWN("6017", 341017, "3DS2 ARes status unknown"),
    _3DS2_CHALLENGE_CANCELLED("6018", 341018, "3DS2 challenge cancelled by user"),
    _3DS2_CHALLENGE_FAILED("6019", 341019, "3DS2 challenge failed"),
    _3DS2_RREQ_UNKNOWN("6020", 341020, "3DS2 RReq status unknown"),

    // Success case if last4 doesn’t match
    SUCCESS(null, 0, "Request processed successfully");

    private final String last4;
    private final int actionCode;
    private final String description;

    CardOutcomeEnum(String last4, int actionCode, String description) {
        this.last4 = last4;
        this.actionCode = actionCode;
        this.description = description;
    }

    public String getLast4() {
        return last4;
    }

    public int getActionCode() {
        return actionCode;
    }

    public String getDescription() {
        return description;
    }

    public static CardOutcomeEnum fromPan(String pan) {
        if (pan == null) return SUCCESS;
        String digits = pan.replaceAll("\\s", "");
        if (digits.length() < 16) return SUCCESS;
        String last4 = digits.substring(digits.length() - 4);
        for (CardOutcomeEnum outcome : values()) {
            if (outcome.last4 != null && outcome.last4.equals(last4)) {
                return outcome;
            }
        }
        return SUCCESS;
    }
}
