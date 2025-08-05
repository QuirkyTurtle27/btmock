package ro.gs1.btmock.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;

@MongoEntity(collection = "creditCards")
public class CreditCardEntity extends PanacheMongoEntity {

    public String cardNumber;
    public String expiryDate;
    public String securityCode;

    public CreditCardEntity() {
    }

    public CreditCardEntity(String cardNumber, String expiryDate, String securityCode) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.securityCode = securityCode;
    }
}