package ro.gs1.btmock.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.util.Objects;

@MongoEntity(collection = "merchants")
public class MerchantEntity extends PanacheMongoEntity {

	public String userName;
	public String password;
	public String merchantName;

	public MerchantEntity() {
	}

	public MerchantEntity(String userName, String password, String merchantName) {
		this.userName = userName;
		this.password = password;
		this.merchantName = merchantName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MerchantEntity))
			return false;
		MerchantEntity that = (MerchantEntity) o;
		return Objects.equals(id, that.id);
	}
}