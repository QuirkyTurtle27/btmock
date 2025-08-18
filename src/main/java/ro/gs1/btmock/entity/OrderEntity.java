package ro.gs1.btmock.entity;

import java.util.Map;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "orders")

public class OrderEntity extends PanacheMongoEntity {

    // === Core fields from register.do ===
    public String orderId;          // generated UUID from register.do
    public String orderNumber;      // merchant's order number
    public String userName;         // merchant login
    public String password;         // merchant password (if stored)
    public Long amount;             // amount in minor units
    public Integer currency;        // ISO numeric currency code (e.g., 946 RON)
    public String returnUrl;        // URL to redirect after payment
    public String description;      // order description
    public String language;         // ro, en etc.
    public String pageView;         // DESKTOP/MOBILE
    public String email;            // customer email
    public String childId;          // submerchant
    public String clientId;         // card-on-file client ID
    public String bindingId;        // card-on-file binding ID
    public Integer sessionTimeoutSecs;
    public String expirationDate;   // optional expiration date
    public Map<String, Object> jsonParams;
    public OrderBundle orderBundle;
    public String formUrl;

    // === Status lifecycle ===
    public String status = "CREATED";   // CREATED, DEPOSITED, DECLINED, REFUNDED
    public long createdAt = System.currentTimeMillis();

    // === Extended status fields for getOrderStatusExtended.do ===
    public Integer orderStatus;         // 0 CREATED, 2 DEPOSITED, 6 DECLINED, 4 REFUNDED, 7 PARTIALLY_REFUNDED
    public Integer actionCode;          // 0 for success, otherwise one of the 22 decline codes
    public String actionCodeDescription;

    // Payment amounts
    public Long paymentApprovedAmount;  // authorized amount
    public Long paymentDepositedAmount; // settled amount
    public Long paymentRefundedAmount;  // refunded amount
    public String paymentState;         // textual state e.g. "DEPOSITED"

    // Card authentication info
    public String cardMaskedPan;        // e.g. ****1111
    public String cardExpiration;       // YYYYMM
    public String cardholderName;
    public String approvalCode;         // 6-digit code
    public Integer eci;                 // Electronic Commerce Indicator (3DS)

    // Attributes & audit info
    public String attributeMdOrder;     // same as orderId
    public Long authDateTime;           // timestamp of auth
    public String authRefNum;           // reference number
    public String terminalId;           // terminal ID
    public String ip;                   // client IP
}
