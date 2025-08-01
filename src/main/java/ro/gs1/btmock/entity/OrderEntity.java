package ro.gs1.btmock.entity;

import java.util.Map;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "orders")
public class OrderEntity extends PanacheMongoEntity {

   public String orderId;

   public String orderNumber;

   public String userName;

   public String password; // don't store this in production

   public Long amount; // value in minor units (e.g., bani)

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

   public String status = "CREATED"; // CREATED, DEPOSITED, DECLINED, etc.

   public long createdAt = System.currentTimeMillis();
}
