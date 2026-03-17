package ro.gs1.btmock.beans;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.jboss.logging.Logger;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ro.gs1.btmock.entity.CreditCardEntity;
import ro.gs1.btmock.entity.OrderEntity;
import ro.gs1.btmock.paymentmethods.PaymentOutcomeSimulator;

@Named
@ViewScoped
public class PaymentPageBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(PaymentPageBean.class);

	private OrderEntity order;
	private String orderId;
	private String redirect = "/paymentfailed.xhtml";
	private String cardNumber;
	private String cardholderName;
	private String cardExpiration;
	private String cardSecurity;
	private String selectedTestCard;
	private List<CreditCardEntity> allCards;
	private CreditCardEntity selectedCard;
	private Integer sessionTimeoutSeconds;

	@Inject
	private PaymentOutcomeSimulator payBean;

	@Inject
	private EnvironmentBean envB;

	public void actionViewInit() {
		LOG.info("actionViewInit() - Start");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		orderId = facesContext.getExternalContext().getRequestParameterMap().get("order");
		if (orderId != null && !orderId.isBlank()) {
			order = OrderEntity.find("orderId", orderId).firstResult();
			LOG.infof("actionViewInit() - the orderId: %s", order);
		} else {
			LOG.warn("actionViewInit() - No order ID provided.");
			return;
		}
		if (System.currentTimeMillis() - order.createdAt < order.sessionTimeoutSecs * 1000) {
			sessionTimeoutSeconds = Math
					.toIntExact(order.sessionTimeoutSecs - (System.currentTimeMillis() - order.createdAt) / 1000);
			LOG.infof("actionview() - order timeout %s", sessionTimeoutSeconds);
		} else {
			try {
				payBean.simulateByOrderId(orderId, cardNumber, cardExpiration, cardSecurity, cardholderName);
				LOG.errorf("processPayment(): Payment failed for orderId=%s", orderId);
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext ext = context.getExternalContext();
				ext.redirect(envB.getBaseUri() + "/paymentfailed.xhtml?order=" + orderId);
			} catch (IOException e) {
				LOG.error("processPayment(): Exception while redirecting to paymentfailed.xhtml", e);
			}
		}

		allCards = CreditCardEntity.listAll();
	}

	public void actionPay() {
		try {
			if (payBean.simulateByOrderId(orderId, cardNumber, cardExpiration, cardSecurity, cardholderName)) {
				LOG.errorf("processPayment(): Payment failed for orderId=%s", orderId);
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext ext = context.getExternalContext();
				ext.redirect(envB.getBaseUri() + "/successPage.xhtml?order=" + orderId);
			} else {
				LOG.errorf("processPayment(): Payment failed for orderId=%s", orderId);
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext ext = context.getExternalContext();
				ext.redirect(envB.getBaseUri() + "/paymentfailed.xhtml?order=" + orderId);
			}
		} catch (IOException e) {
			LOG.error("processPayment(): Exception while redirecting to paymentfailed.xhtml", e);
		}
	}

	public void actionRedirect() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext ext = context.getExternalContext();
			ext.redirect(envB.getBaseUri() + "/paymentfailed.xhtml?order=" + orderId);
		} catch (IOException e) {
			LOG.error("processPayment(): Exception while redirecting to paymentfailed.xhtml", e);
		}
	}

	public void onCardSelected(AjaxBehaviorEvent event) {
		LOG.debugf("onCardSelected() - Start");
		if (selectedCard == null)
			return;

		cardNumber = selectedCard.cardNumber;
		cardExpiration = selectedCard.expiryDate;
		cardSecurity = selectedCard.securityCode;
		cardholderName = selectedCard.nameOnCard;
	}

	public String getDescription() {
		return order != null ? order.description : "";
	}

	public BigDecimal getAmountDecimal() {
		if (order == null || order.amount == null) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(order.amount, 2);
	}

	public Integer getSessionTimeoutSeconds() {
		return sessionTimeoutSeconds;
	}

	public String getMerchantName() {
		return order != null ? order.merchantName : "NoName";
	}

	public void setSessionTimeoutSeconds(Integer time) {
	}

	public void setDescription(String description) {
	}

	public void setAmount(Long amount) {
	}

	public OrderEntity getOrder() {
		return order;
	}

	public void setOrder(OrderEntity order) {
		this.order = order;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardExpiration() {
		return cardExpiration;
	}

	public void setCardExpiration(String cardExpiration) {
		this.cardExpiration = cardExpiration;
	}

	public String getCardSecurity() {
		return cardSecurity;
	}

	public void setCardSecurity(String cardSecurity) {
		this.cardSecurity = cardSecurity;
	}

	public String getCardholderName() {
		return cardholderName;
	}

	public void setCardholderName(String cardholderName) {
		this.cardholderName = cardholderName;
	}

	public String getSelectedTestCard() {
		return selectedTestCard;
	}

	public void setSelectedTestCard(String selectedTestCard) {
		this.selectedTestCard = selectedTestCard;
	}

	public List<CreditCardEntity> getAllCards() {
		return allCards;
	}

	public void setAllCards(List<CreditCardEntity> allCards) {
		this.allCards = allCards;
	}

	public CreditCardEntity getSelectedCard() {
		return selectedCard;
	}

	public void setSelectedCard(CreditCardEntity selectedCard) {
		LOG.debugf("Oare trimite ceva ? %s", selectedCard);
		this.selectedCard = selectedCard;
	}
}
