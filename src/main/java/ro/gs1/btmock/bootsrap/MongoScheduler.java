package ro.gs1.btmock.bootsrap;

import java.util.concurrent.TimeUnit;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Startup
public class MongoScheduler {
	@Inject
	MongoClient mongo;

	@PostConstruct
    void postConstruct() {
    	createIndex();
    }

	void createIndex() {
		var coll = mongo.getDatabase("BTpay").getCollection("orders");
		coll.createIndex(Indexes.ascending("creationDate"), new IndexOptions().expireAfter(30L, TimeUnit.DAYS));
	}
}
