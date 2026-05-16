package ro.gs1.btmock.beans;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ro.gs1.btmock.registration.PaymentClient;
import ro.gs1.btmock.registration.RegisterForm;
import ro.gs1.btmock.registration.RegisterResponse;

@Named
@RequestScoped
public class TestPageBean {

    private static final Logger LOG = Logger.getLogger(TestPageBean.class);
    private String description;

    @Inject
    @RestClient
    PaymentClient paymentClient;

    public void createTestOrder() {
    	LOG.debug("createTestOrder() - Start");
        try {
            String orderNumber = "TEST-ORDER-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 8);

            RegisterForm form = new RegisterForm();
            form.userName   = "usergs1";
            form.password   = "parola2";
            form.orderNumber= orderNumber;
            form.amount     = "1200";
            form.currency   = "946";
            form.returnUrl  = "https://btmock.gs1.ro";
            form.description= description;

            // optional fields
            form.language           = "ro";
            form.pageView           = "DESKTOP";
            form.childId            = "submerchant_test";
            form.clientId           = "client-123";
            form.bindingId          = "00000000-0000-0000-0000-000000000000";
            form.sessionTimeoutSecs = "600";
            form.expirationDate     = "2025-12-31T23:59:59";

            // JSON-in-form (send as raw JSON strings; client will URL-encode)
            form.jsonParams  = "{\"campaignId\":\"CAMP-001\",\"source\":\"testSuite\"}";
            form.orderBundle = """
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

            RegisterResponse resp = paymentClient.register(form);

            if (resp == null || resp.formUrl == null || resp.orderId == null) {
                LOG.error("createTestOrder() - Missing expected fields in response");
                return; // stay on page
            }
            LOG.infov("createTestOrder() - Created order {0}; redirecting to {1}",
                      resp.orderId, resp.formUrl);
            FacesContext.getCurrentInstance().getExternalContext().redirect(resp.formUrl);
            FacesContext.getCurrentInstance().responseComplete();
            return;
        } catch (Exception e) {
            LOG.error("createTestOrder() - Error creating test order", e);
            return;
        }
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
