package ro.gs1.btmock.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import ro.gs1.btmock.entity.OrderEntity;

@Named("orderViewBean")
@ViewScoped
public class OrderViewBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(OrderViewBean.class);
	private static final ObjectMapper JSON = new ObjectMapper();

	private String orderId;
	private OrderEntity order;
	private BigDecimal refundAmount;

	private boolean additionalInfoInFlight;
	private String extendedInfoJson;
	private boolean extendedInfoInFlight;

	private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	public void actionViewInit() {
		LOG.info("actionViewInit() - Start");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			LOG.warn("FacesContext is null (non-JSF call?)");
			return;
		}
		orderId = facesContext.getExternalContext().getRequestParameterMap().get("order");

		if (orderId != null && !orderId.isBlank()) {
			order = OrderEntity.find("orderId", orderId).firstResult();
			LOG.infof("actionViewInit() - loaded order: %s", order != null ? order.orderNumber : "null");

			if (order != null && order.creationDate == null && order.createdAt != null) {
				order.creationDate = java.util.Date.from(Instant.ofEpochMilli(order.createdAt));
			}
		} else {
			LOG.warn("actionViewInit() - No order ID provided.");
			addMsg(FacesMessage.SEVERITY_WARN, "Missing order id", "No 'order' request parameter was provided.");
		}
		long refundableMinor = getRefundableMinor();
		this.refundAmount = BigDecimal.valueOf(refundableMinor).movePointLeft(2).setScale(2, RoundingMode.DOWN);
	}

	public void performRefund() {
		if (order == null) {
			addMsg(FacesMessage.SEVERITY_ERROR, "No order", "Cannot refund because the order is not loaded.");
			return;
		}
		if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
			addMsg(FacesMessage.SEVERITY_WARN, "Invalid amount", "Please enter a positive refund amount.");
			return;
		}

		long refundableMinor = getRefundableMinor();
		long amountMinor = refundAmount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue();

		if (refundableMinor <= 0) {
			addMsg(FacesMessage.SEVERITY_WARN, "Nothing to refund", "Refundable amount is zero.");
			return;
		}
		if (amountMinor > refundableMinor) {
			addMsg(FacesMessage.SEVERITY_WARN, "Too large", "Refund exceeds refundable amount.");
			return;
		}

		try {
			String url = getAppBaseUrl() + "/payment/rest/refund.do";
			String body = formEncode(Map.of("userName", nvl(order.userName, ""), "password", nvl(order.password, ""),
					"orderId", nvl(order.orderId, ""), "orderNumber", nvl(order.orderNumber, ""), "amount",
					String.valueOf(amountMinor) // endpoint expects MINOR units
			));

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(body)).build();

			HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
			JsonNode json = JSON.readTree(resp.body());
			String errorCode = json.path("errorCode").asText("");
			String errorMessage = json.path("errorMessage").asText("");

			if ("0".equals(errorCode)) {
				order = OrderEntity.find("orderId", orderId).firstResult();

				long newRefundable = getRefundableMinor();
				this.refundAmount = BigDecimal.valueOf(newRefundable).movePointLeft(2).setScale(2, RoundingMode.DOWN);

				addMsg(FacesMessage.SEVERITY_INFO, "Refund successful",
						formatMinor(amountMinor) + " " + getCurrencyCode() + " refunded.");
			} else {
				addMsg(FacesMessage.SEVERITY_ERROR, "Refund failed",
						"[" + errorCode + "] " + (errorMessage == null ? "Unknown error" : errorMessage));
			}
		} catch (Exception e) {
			LOG.error("performRefund() failed", e);
			addMsg(FacesMessage.SEVERITY_ERROR, "Refund failed", e.getMessage());
		}
	}

	/**
	 * Calls /payment/rest/extendedRegister.do and stores RAW JSON in
	 * extendedInfoJson.
	 */
	public void fetchExtendedInfo() {
		if (order == null && (orderId == null || orderId.isBlank())) {
			addMsg(FacesMessage.SEVERITY_ERROR, "No order", "Cannot fetch info because the order is not loaded.");
			return;
		}

		extendedInfoInFlight = true;
		try {
			String orderIdToUse = (order != null && order.orderId != null && !order.orderId.isBlank()) ? order.orderId
					: orderId;

			String orderNumberToUse = (order != null && order.orderNumber != null && !order.orderNumber.isBlank())
					? order.orderNumber
					: null;

			var bodyMap = new java.util.LinkedHashMap<String, String>();
			bodyMap.put("userName", nvl(order != null ? order.userName : null, ""));
			bodyMap.put("password", nvl(order != null ? order.password : null, ""));
			if (orderIdToUse != null && !orderIdToUse.isBlank())
				bodyMap.put("orderId", orderIdToUse);
			if (orderNumberToUse != null && !orderNumberToUse.isBlank())
				bodyMap.put("orderNumber", orderNumberToUse);
			String body = formEncode(bodyMap);

			String url = getAppBaseUrl() + "/payment/rest/extendedRegister.do";

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();

			HttpResponse<String> resp = client.send(request,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				// Store RAW JSON as-is (no pretty-printing)
				extendedInfoJson = resp.body();
				addMsg(FacesMessage.SEVERITY_INFO, "Extended info", "Response received.");
			} else {
				// Preserve body but indicate HTTP status (still JSON so your textarea shows it)
				extendedInfoJson = "{\"httpStatus\":" + resp.statusCode() + ",\"body\":" + jsonQuote(resp.body()) + "}";
				addMsg(FacesMessage.SEVERITY_ERROR, "Extended info failed", "HTTP " + resp.statusCode());
			}
		} catch (Exception e) {
			LOG.error("fetchExtendedInfo() failed", e);
			extendedInfoJson = "{\"error\":\"" + jsonEscape(e.getMessage()) + "\"}";
			addMsg(FacesMessage.SEVERITY_ERROR, "Extended info exception", e.getMessage());
		} finally {
			extendedInfoInFlight = false;
		}
	}

	private String getAppBaseUrl() {
		var fc = FacesContext.getCurrentInstance();
		var req = (jakarta.servlet.http.HttpServletRequest) fc.getExternalContext().getRequest();
		String scheme = req.getScheme();
		String host = req.getServerName();
		int port = req.getServerPort();
		String ctx = req.getContextPath();
		boolean defPort = ("http".equalsIgnoreCase(scheme) && port == 80)
				|| ("https".equalsIgnoreCase(scheme) && port == 443);
		return scheme + "://" + host + (defPort ? "" : ":" + port) + ctx;
	}

	public String getCurrencyCode() {
		if (order == null || order.currency == null)
			return "";
		return switch (order.currency) {
		case 946 -> "RON";
		case 978 -> "EUR";
		case 840 -> "USD";
		default -> String.valueOf(order.currency);
		};
	}

	/** Formats main amount (assumed minor units). */
	public String getFormattedAmount() {
		if (order == null)
			return "";
		return formatMinor(order.amount);
	}

	/** Format minor units (e.g., bani/cents) to locale string with 2 decimals. */
	public String formatMinor(Long minor) {
		long v = nz(minor);
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(v / 100.0);
	}

	/** Maps status to PrimeFaces Tag severity. */
	public String getStatusSeverity() {
		String s = order != null && order.status != null ? order.status.toUpperCase(Locale.ROOT) : "UNKNOWN";
		return switch (s) {
		case "CREATED", "APPROVED", "DEPOSITED" -> "success";
		case "REFUNDED", "PARTIALLY_REFUNDED" -> "info";
		case "PENDING", "IN_PROGRESS" -> "warning";
		case "DECLINED", "FAILED", "CANCELED" -> "danger";
		default -> "secondary";
		};
	}

	/** Pretty-prints orderBundle and/or jsonParams for the accordion tab. */
	public String getBundleSummary() {
		if (order == null)
			return "";
		StringBuilder sb = new StringBuilder();
		try {
			if (order.orderBundle != null) {
				sb.append("orderBundle:\n").append(PRETTY_MAPPER.writeValueAsString(order.orderBundle)).append("\n");
			}
			if (order.jsonParams != null && !order.jsonParams.isEmpty()) {
				sb.append("jsonParams:\n").append(PRETTY_MAPPER.writeValueAsString(order.jsonParams));
			}
		} catch (JsonProcessingException e) {
			LOG.warn("Pretty print failed", e);
		}
		return sb.length() == 0 ? null : sb.toString();
	}

	/**
	 * Disable refund when nothing deposited, or already fully refunded, or status
	 * not eligible.
	 */
	public boolean isRefundEnabled() {
		if (order == null)
			return true;
		long deposited = nz(order.paymentDepositedAmount);
		long refunded = nz(order.paymentRefundedAmount);
		if (deposited <= 0)
			return false;
		if (refunded >= deposited)
			return false;

		String s = order.status != null ? order.status.toUpperCase(Locale.ROOT) : "";
		return switch (s) {
		case "DEPOSITED", "APPROVED", "PARTIALLY_REFUNDED" -> true;
		default -> false;
		};
	}

	public long getRefundableMinor() {
		long deposited = order != null ? nz(order.paymentDepositedAmount) : 0L;
		long refunded = order != null ? nz(order.paymentRefundedAmount) : 0L;
		long refundable = Math.max(deposited - refunded, 0L);
		return refundable;
	}

	public boolean isAdditionalInfoInFlight() {
		return additionalInfoInFlight;
	}

	private static String jsonEscape(String s) {
		if (s == null)
			return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	private static String jsonQuote(String s) {
		return s == null ? "null" : "\"" + jsonEscape(s) + "\"";
	}

	// --- Getters for page ---

	public OrderEntity getOrder() {
		return order;
	}

	public BigDecimal getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(BigDecimal refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getExtendedInfoJson() {
		return extendedInfoJson;
	}

	public boolean isExtendedInfoInFlight() {
		return extendedInfoInFlight;
	}

	// --- Internal utilities ---

	private static String nvl(String v, String d) {
		return v == null ? d : v;
	}

	private static String formEncode(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		for (var e : params.entrySet()) {
			if (sb.length() > 0)
				sb.append('&');
			sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)).append('=')
					.append(URLEncoder.encode(nvl(e.getValue(), ""), StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

	private static long nz(Long v) {
		return v == null ? 0L : v;
	}

	private void addMsg(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc != null) {
			fc.addMessage(null, new FacesMessage(severity, summary, detail));
		}
	}
}
