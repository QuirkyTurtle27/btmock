package ro.gs1.btmock.creditCards;

import java.util.Map;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ro.gs1.btmock.entity.CreditCardEntity;

@Path("/credit-card")
public class CreditCardAPI {

    private static final Logger log = Logger.getLogger(CreditCardAPI.class);

    @Inject
    CreditCardService creditCardService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCreditCard() {
        CreditCardEntity card = creditCardService.generateAndStoreCard();

        log.infof("createCreditCard(): Created card with number %s", card.cardNumber);

        return Response.ok(Map.of(
                "message", "Card created successfully",
                "cardNumber", card.cardNumber
        )).build();
    }
}
