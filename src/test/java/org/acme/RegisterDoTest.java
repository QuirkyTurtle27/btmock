package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class RegisterDoTest {

    @Test
    public void testRegisterDoWithAllFields() {
        String url = "/payment/rest/register.do";

        // Replace with your sandbox credentials
        String userName = "your_test_username";
        String password = "your_test_password";

        // Required core fields
        String orderNumber = "TEST-ORDER-002";
        String amount = "1200"; // 12.00 RON in bani
        String currency = "946"; // RON
        String returnUrl = "https://yourdomain.ro/finish";
        String description = "Test BT Full Transaction";

        // Optional fields
        String language = "ro";
        String pageView = "DESKTOP";
        String childId = "submerchant_test"; // Required only for aggregators
        String clientId = "client-123"; // Required only for COF
        String bindingId = "00000000-0000-0000-0000-000000000000"; // for COF
        String sessionTimeoutSecs = "600";
        String expirationDate = "2025-12-31T23:59:59"; // Ignored if sessionTimeoutSecs is set

        String jsonParams = """
            {"campaignId":"CAMP-001","source":"testSuite"}
            """;

        String orderBundleJson = """
            {
              "orderCreationDate":"2025-07-18",
              "customerDetails":{
                "email":"test@example.com",
                "phone":"40740123456",
                "contact":"Test Contact",
                "deliveryInfo":{
                  "deliveryType":"comanda",
                  "country":"642",
                  "city":"Cluj",
                  "postAddress":"Strada Test",
                  "postAddress2":"Bloc 3",
                  "postAddress3":"Etaj 2",
                  "postalCode":"123456",
                  "state":"CJ"
                },
                "billingInfo":{
                  "deliveryType":"comanda",
                  "country":"642",
                  "city":"Cluj",
                  "postAddress":"Strada Test",
                  "postAddress2":"Bloc 3",
                  "postAddress3":"Etaj 2",
                  "postalCode":"123456",
                  "state":"CJ"
                }
              }
            }
            """;

        given()
            .contentType(ContentType.URLENC)
            .formParam("userName", userName)
            .formParam("password", password)
            .formParam("orderNumber", orderNumber)
            .formParam("amount", amount)
            .formParam("currency", currency)
            .formParam("returnUrl", returnUrl)
            .formParam("description", description)
            .formParam("language", language)
            .formParam("pageView", pageView)
            .formParam("email", "test@example.com")
            .formParam("childId", childId)
            .formParam("clientId", clientId)
            .formParam("bindingId", bindingId)
            .formParam("sessionTimeoutSecs", sessionTimeoutSecs)
            .formParam("expirationDate", expirationDate)
            .formParam("jsonParams", jsonParams)
            .formParam("orderBundle", orderBundleJson)
        .when()
            .post(url)
        .then()
            .statusCode(200)
            .body("formUrl", notNullValue())
            .body("orderId", notNullValue());
    }
}