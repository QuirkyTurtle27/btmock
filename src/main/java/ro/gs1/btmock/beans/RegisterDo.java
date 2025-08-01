package ro.gs1.btmock.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ro.gs1.btmock.entity.OrderBundle;
import ro.gs1.btmock.entity.OrderEntity;

@Path("/payment/rest/register.do")
public class RegisterDo {

   @Inject
   Logger log;

   @Inject
   ObjectMapper objectMapper;

   @POST
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   public Response receiveIpayRegister(@FormParam("userName") String userName, @FormParam("password") String password,
      @FormParam("orderNumber") String orderNumber, @FormParam("amount") String amount,
      @FormParam("currency") String currency, @FormParam("returnUrl") String returnUrl,
      @FormParam("description") String description, @FormParam("language") String language,
      @FormParam("pageView") String pageView, @FormParam("email") String email, @FormParam("childId") String childId,
      @FormParam("clientId") String clientId, @FormParam("bindingId") String bindingId,
      @FormParam("sessionTimeoutSecs") String sessionTimeoutSecs, @FormParam("expirationDate") String expirationDate,
      @FormParam("jsonParams") String jsonParamsStr, @FormParam("orderBundle") String orderBundleStr) {
      String orderID = UUID.randomUUID()
         .toString();
      String baseUrl = "localhost:8080/PaymentPage.xhtml";
      String formURL = baseUrl + "?order=" + orderID + "&language=" + language;
      Map<String, String> response = new HashMap<>();
      response.put("orderId", orderID);
      response.put("formUrl", formURL);
      try {
         OrderEntity order = new OrderEntity();
         OrderBundle orderBundle = objectMapper.readValue(orderBundleStr, OrderBundle.class);
         if (jsonParamsStr != null && !jsonParamsStr.isEmpty()) {
            Map<String, Object> jsonParams = objectMapper.readValue(jsonParamsStr,
               new TypeReference<Map<String, Object>>() {
               });
            order.jsonParams = jsonParams;
         }
         order.orderId = orderID;
         order.orderNumber = orderNumber;
         order.userName = userName;
         order.password = password;
         order.amount = Long.parseLong(amount);
         order.currency = Integer.parseInt(currency);
         order.returnUrl = returnUrl;
         order.description = description;
         order.language = language;
         order.pageView = pageView;
         order.email = email;
         order.childId = childId;
         order.clientId = clientId;
         order.bindingId = bindingId;
         order.sessionTimeoutSecs = sessionTimeoutSecs != null ? Integer.parseInt(sessionTimeoutSecs) : null;
         order.expirationDate = expirationDate;
         order.orderBundle = orderBundle;
         order.status = "CREATED";
         order.createdAt = System.currentTimeMillis();
         //order.persist();

         log.debug(response);
         log.debugf("Order Number: %s", orderNumber);
         log.debugf("Amount: %s", amount);
         log.debugf("Customer Email: %s", orderBundle.customerDetails.email);
         log.debugf("City: %s", orderBundle.customerDetails.deliveryInfo.city);
         return Response.ok(response)
            .build();
      } catch (Exception e) {
         log.trace("Exception occurred during order processing", e);
         return Response.status(400)
            .entity(Map.of("errorCode", 4, "errorMessage", "Empty order number"))
            .build();
      }
   }
}