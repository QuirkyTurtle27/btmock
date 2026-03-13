package ro.gs1.btmock.dofunctions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ro.gs1.btmock.beans.EnvironmentBean;
import ro.gs1.btmock.entity.MerchantEntity;
import ro.gs1.btmock.entity.OrderBundle;
import ro.gs1.btmock.entity.OrderEntity;

@Path("/payment/rest/register.do")
public class RegisterDo {

	@Inject
	EnvironmentBean envB;

	@Inject
	Logger log;

	@Inject
	ObjectMapper objectMapper;

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response receiveIpayRegister(@QueryParam("userName") String userNameQuery, @FormParam("userName") String userNameForm, @QueryParam("password") String passwordQuery, @FormParam("password") String passwordForm,
			@FormParam("orderNumber") String orderNumber, @FormParam("amount") String amount,
			@FormParam("currency") String currency, @FormParam("returnUrl") String returnUrl,
			@FormParam("description") String description, @FormParam("language") String language,
			@FormParam("pageView") String pageView, @FormParam("email") String email,
			@FormParam("childId") String childId, @FormParam("clientId") String clientId,
			@FormParam("bindingId") String bindingId, @FormParam("sessionTimeoutSecs") String sessionTimeoutSecs,
			@FormParam("expirationDate") String expirationDate, @FormParam("jsonParams") String jsonParamsStr,
			@FormParam("orderBundle") String orderBundleStr) {

		String userName = userNameForm != null && !userNameForm.isBlank() ? userNameForm : userNameQuery;
		String password = passwordForm != null && !passwordForm.isBlank() ? passwordForm : passwordQuery;

		if (orderNumber == null || orderNumber.trim().isEmpty()) {
			return errorResponse(4, "Order number is empty");
		}
		if (OrderEntity.count("orderNumber", orderNumber) > 0) {
			return errorResponse(1, "Order with this number was already processed.");
		}
		if (amount == null || amount.trim().isEmpty()) {
			return errorResponse(4, "Empty amount");
		}
		Long amountValue = null;
		try {
			amountValue = Long.parseLong(amount);
			if (amountValue <= 0)
				return errorResponse(5, "Invalid amount value");
		} catch (NumberFormatException ex) {
			return errorResponse(5, "Invalid value of one of the parameters.");
		}
		if (returnUrl == null || returnUrl.trim().isEmpty()) {
			return errorResponse(4, "Empty return URL");
		}
		if (!returnUrl.startsWith("http://") && !returnUrl.startsWith("https://") && !returnUrl.startsWith("localhost:8080/")) {
			return errorResponse(4, "Invalid return URL");
		}
		if (userName == null || userName.trim().isEmpty()) {
		    return errorResponse(4, "Empty merchant user name");
		}
		if (password == null || password.trim().isEmpty()) {
		    return errorResponse(4, "Password cannot be empty");
		}
		MerchantEntity merchant = MerchantEntity.find("userName = ?1 and password = ?2", userName, password).firstResult();

		if (merchant == null) {
		    return errorResponse(5, "Access denied");
		}
		if (description != null && (!description.matches("^[\\x20-\\x7D]*$") || description.contains("~"))) {
			return errorResponse(11, "Wrong orderDescription param value");
		}

		if (orderBundleStr == null || orderBundleStr.trim().isEmpty()) {
			return errorResponse(8, "[orderBundle.customerDetails.*] wrong");
		}
		OrderBundle orderBundle;
		try {
			orderBundle = objectMapper.readValue(orderBundleStr, OrderBundle.class);
		} catch (Exception e) {
			return errorResponse(8, "[orderBundle.customerDetails.*] wrong");
		}
		if (orderBundle.customerDetails == null || orderBundle.customerDetails.email == null
				|| orderBundle.customerDetails.deliveryInfo == null
				|| orderBundle.customerDetails.deliveryInfo.city == null) {
			return errorResponse(8, "[orderBundle.customerDetails.*] wrong");
		}

		Integer currencyValue = (currency != null && !currency.isEmpty()) ? Integer.parseInt(currency) : null;
		Integer sessionTimeout = (sessionTimeoutSecs != null && !sessionTimeoutSecs.isEmpty())
				? Integer.parseInt(sessionTimeoutSecs)
				: 600;
		Map<String, Object> jsonParams = null;
		if (jsonParamsStr != null && !jsonParamsStr.isEmpty()) {
			try {
				jsonParams = objectMapper.readValue(jsonParamsStr,
						new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
						});
			} catch (Exception e) {
				return errorResponse(5, "Invalid [jsonParams]");
			}
		}

		String orderID = java.util.UUID.randomUUID().toString();
		String baseUrl = envB.getBaseUri() + "/PaymentPage.xhtml";
		String formURL = baseUrl + "?order=" + orderID + "&language=" + (language != null ? language : "ro");

		OrderEntity order = new OrderEntity();
		if (clientId != null && !clientId.isBlank()) {
			if (bindingId == null || bindingId.isBlank()) {
				bindingId = java.util.UUID.randomUUID().toString();
				log.debugf("Generated new bindingId=%s for clientId=%s", bindingId, clientId);
			} else {
				log.debugf("Using provided bindingId=%s for clientId=%s", bindingId, clientId);
			}
			order.clientId = clientId;
			order.bindingId = bindingId;

		} else if (bindingId != null && !bindingId.isBlank()) {
			return errorResponse(5, "bindingId requires clientId for COF");
		}
		order.orderId = orderID;
		order.orderNumber = orderNumber;
		order.userName = userName;
		order.password = password;
		order.amount = amountValue;
		order.currency = currencyValue;
		order.returnUrl = returnUrl;
		order.description = description;
		order.language = language;
		order.pageView = pageView;
		order.email = email;
		order.childId = childId;
		order.sessionTimeoutSecs = sessionTimeout;
		order.expirationDate = expirationDate;
		order.jsonParams = jsonParams;
		order.orderBundle = orderBundle;
		order.status = "CREATED";
		order.createdAt = System.currentTimeMillis();
		order.creationDate = new Date(System.currentTimeMillis());
		order.bankName = "Banca Transilvania";
		order.bankCountryCode = "RO";
		order.bankCountryName = "Romania";
		order.merchantName = merchant.merchantName;

		order.persist();

		Map<String, String> response = new HashMap<>();
		response.put("orderId", orderID);
		response.put("formUrl", formURL);

		log.debugf("Order created successfully: orderNumber=%s, orderId=%s, amount=%s, customerEmail=%s, city=%s",
				orderNumber, orderID, amount, orderBundle.customerDetails.email,
				orderBundle.customerDetails.deliveryInfo.city);

		return Response.ok(response).build();
	}

	private Response errorResponse(int code, String msg) {
		log.errorf("Order error: errorCode=%d, errorMessage=%s", code, msg);
		Map<String, Object> resp = new HashMap<>();
		resp.put("errorCode", code);
		resp.put("errorMessage", msg);
		return Response.ok(resp).build();
	}
}
