package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CreditCardAPITest {

    private static final Logger log = Logger.getLogger(CreditCardAPITest.class);

    @Test
    public void testCreateCreditCardEndpoint() {
        var response = given()
            .when()
            .post("/credit-card")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("message", equalTo("Card created successfully"))
            .body("cardNumber", not(emptyString()))
            .extract()
            .response();

        String cardNumber = response.jsonPath().getString("cardNumber");
        String message = response.jsonPath().getString("message");

        log.infof("testCreateCreditCardEndpoint(): Received response - %s (Card: %s)", message, cardNumber);
        System.out.printf("→ API Response: %s | Card: %s%n", message, cardNumber);
    }
}