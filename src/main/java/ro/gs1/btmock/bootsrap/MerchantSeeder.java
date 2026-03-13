package ro.gs1.btmock.bootsrap;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import io.quarkus.runtime.Startup;
import ro.gs1.btmock.entity.MerchantEntity;

@Startup
@ApplicationScoped
public class MerchantSeeder {

    private static final Logger log = Logger.getLogger(MerchantSeeder.class);

    private static final List<MerchantEntity> SEED_MERCHANTS = List.of(
        new MerchantEntity("userds", "parolads", "Asociatia GS1 Romania"),
        new MerchantEntity("usergs1", "parola2", "GS1Data Systems"),
        new MerchantEntity("userdex", "paroladex", "GS1Data Exchange")
    );

    @PostConstruct
    void postConstruct() {
        seedMerchants();
    }

    void seedMerchants() {
        int inserted = 0;

        for (MerchantEntity merchant : SEED_MERCHANTS) {
            long count = MerchantEntity.count("userName", merchant.userName);

            if (count == 0) {
                merchant.persist();
                inserted++;
                log.infof("Inserted merchant: %s (%s)", merchant.userName, merchant.merchantName);
            }
        }

        if (inserted == 0) {
            log.info("All predefined merchants already present. No inserts performed.");
        } else {
            log.infof("Seed complete. Inserted %d merchants.", inserted);
        }
    }
}