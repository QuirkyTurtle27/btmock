package ro.gs1.btmock.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;

import java.util.Objects;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

@MongoEntity(collection = "creditCards")
public class CreditCardEntity extends PanacheMongoEntity {

	public String cardNumber;
	public String expiryDate;
	public String securityCode;
	public String nameOnCard;

	public CreditCardEntity() {
	}

	public CreditCardEntity(String cardNumber, String expiryDate, String securityCode, String nameOnCard) {
		this.cardNumber = cardNumber;
		this.expiryDate = expiryDate;
		this.securityCode = securityCode;
		this.nameOnCard = nameOnCard;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CreditCardEntity other = (CreditCardEntity) obj;
		return Objects.equals(id, other.id);
	}

}