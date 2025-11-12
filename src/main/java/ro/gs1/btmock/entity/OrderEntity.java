package ro.gs1.btmock.entity;


import java.util.Date;
import java.util.List;
import java.util.Map;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "orders")
public class OrderEntity extends PanacheMongoEntity {

    // === Core fields from register.do ===
    public String orderId;
    public String orderNumber;
    public String userName;
    public String password;
    public Long amount;
    public Integer currency;
    public String returnUrl;
    public String description;
    public String language;
    public String pageView;
    public String email;
    public String childId;
    public String clientId;
    public String bindingId;
    public Integer sessionTimeoutSecs;
    public String expirationDate;
    public Map<String, Object> jsonParams;
    public OrderBundle orderBundle;
    public String formUrl;

    // === Status lifecycle ===
    public String status = "CREATED";
    public Long createdAt = System.currentTimeMillis();
    public Date creationDate;

    // === Extended status ===
    public Integer orderStatus;
    public Integer actionCode;
    public String actionCodeDescription;

    // Payment amounts
    public Long paymentApprovedAmount;
    public Long paymentDepositedAmount;
    public Long paymentRefundedAmount;
    public String paymentState;

    // Card auth info
    public String cardMaskedPan;
    public String cardExpiration;
    public String cardholderName;
    public String approvalCode;
    public Integer eci;

    // Attributes / audit
    public String attributeMdOrder;
    public Long authDateTime;
    public String authRefNum;
    public String terminalId;
    public String ip;

    // bindingInfo (Card-on-File / network token details)
    public String cardArtUrl;
    public String cardArtPicture;
    public String cardArtForegroundColor;
    public String bin;
    public String panLastFour;

    // merchantOrderParams (list of name/value)
    public List<Map<String, String>> merchantOrderParams;

    // attributes (list of name/value pairs, must include mdOrder)
    public List<Map<String, String>> attributes;

    // bankInfo
    public String bankName;
    public String bankCountryCode;
    public String bankCountryName;

    // refunds history (list of refund objects)
    public List<Map<String, Object>> refunds;
}
