package ro.gs1.btmock.registration;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/payment/rest")
@RegisterRestClient(configKey = "pay-api")
@ApplicationScoped
public interface PaymentClient {

    @POST
    @Path("/register.do")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    RegisterResponse register(@BeanParam RegisterForm form);
}
