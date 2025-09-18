package ro.gs1.btmock.bootsrap;

import java.util.List;

import org.jboss.logging.Logger;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ro.gs1.btmock.entity.CreditCardEntity;


/**
 * CreditCardSeeder
 *
 * This startup bean ensures that all predefined test credit cards used to simulate
 * BT API error scenarios are present in the MongoDB "creditCards" collection.
 *
 * On application startup:
 *  - Iterates through the predefined list of test cards (22 PANs mapped to action codes).
 *  - For each card, checks if it already exists in the collection (by cardNumber).
 *  - If not found, persists the card into MongoDB.
 *  - Logs which cards were inserted, or confirms that all were already present.
 *
 * These seeded cards are used by the mock payment flow to trigger predictable outcomes
 * (success or specific decline codes) when testing `getOrderStatusExtended.do`.
 */

@Startup
@ApplicationScoped
public class CreditCardSeeder {

    @Inject
    Logger log;

    // Predefined list of test cards
    private static final List<CreditCardEntity> SEED_CARDS = List.of(
        new CreditCardEntity("4444444444440104", "12/26", "104", "Card 104 - Restricted"),
        new CreditCardEntity("4444444444440124", "12/26", "124", "Card 124 - Regulatory Block"),
        new CreditCardEntity("4444444444440320", "12/26", "320", "Card 320 - Inactive"),
        new CreditCardEntity("4444444444440801", "12/26", "801", "Card 801 - Issuer Down"),
        new CreditCardEntity("4444444444440803", "12/26", "803", "Card 803 - Blocked"),
        new CreditCardEntity("4444444444440804", "12/26", "804", "Card 804 - Not Allowed"),
        new CreditCardEntity("4444444444440805", "12/26", "805", "Card 805 - Denied"),
        new CreditCardEntity("4444444444440861", "00/00", "861", "Card 861 - Invalid Expiry"),
        new CreditCardEntity("4444444444440871", "12/26", "000", "Card 871 - Invalid CVV"),
        new CreditCardEntity("4444444444440905", "12/26", "905", "Card 905 - Invalid"),
        new CreditCardEntity("4444444444440906", "01/20", "906", "Card 906 - Expired"),
        new CreditCardEntity("4444444444440913", "12/26", "913", "Card 913 - Invalid Txn"),
        new CreditCardEntity("4444444444440914", "12/26", "914", "Card 914 - Invalid Account"),
        new CreditCardEntity("4444444444440915", "12/26", "915", "Card 915 - No Funds"),
        new CreditCardEntity("4444444444440917", "12/26", "917", "Card 917 - Limit Exceeded"),
        new CreditCardEntity("4444444444440952", "12/26", "952", "Card 952 - Fraud"),
        new CreditCardEntity("4444444444440998", "12/26", "998", "Card 998 - No Installments"),
        new CreditCardEntity("4444444444446016", "12/26", "016", "Card 341016 - 3DS ARes Declined"),
        new CreditCardEntity("4444444444446017", "12/26", "017", "Card 341017 - 3DS ARes Unknown"),
        new CreditCardEntity("4444444444446018", "12/26", "018", "Card 341018 - 3DS Cancelled"),
        new CreditCardEntity("4444444444446019", "12/26", "019", "Card 341019 - 3DS Failed"),
        new CreditCardEntity("4444444444446020", "12/26", "020", "Card 341020 - 3DS RReq Unknown")
    );

    @PostConstruct
    void postConstruct() {
    	seedCreditCards();
    }

    void seedCreditCards() {
        int inserted = 0;
        for (CreditCardEntity card : SEED_CARDS) {
            long count = CreditCardEntity.count("cardNumber", card.cardNumber);
            if (count == 0) {
                card.persist();
                inserted++;
                log.infof("Inserted test card: %s (%s)", card.cardNumber, card.nameOnCard);
            }
        }
        if (inserted == 0) {
            log.info("All predefined test credit cards already present. No inserts performed.");
        } else {
            log.infof("Seed complete. Inserted %d test credit cards.", inserted);
        }
    }
}