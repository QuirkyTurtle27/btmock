package ro.gs1.btmock.beans;

import java.io.Serializable;
import java.util.Date;

import org.jboss.logging.Logger;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import ro.gs1.btmock.entity.OrderEntity;

@Named
@ViewScoped
public class PaymentResultBean implements Serializable {

	private static final long serialVersionUID = 2455747052202290966L;
	private static final Logger LOG = Logger.getLogger(PaymentPageBean.class);
	private OrderEntity order;
	private String merchantReturnUrl;
	private String orderId;
	private Date date;

	public void actionViewInit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		LOG.infof("ActionViewInit() - the amount is: %s", order);
		orderId = facesContext.getExternalContext().getRequestParameterMap().get("order");

		if (orderId != null && !orderId.isBlank()) {
			order = OrderEntity.find("orderId", orderId).firstResult();
			LOG.infof("ActionViewInit() - the amount is: %s", order);
		} else {
			LOG.warn("ActionViewInit() - No order ID provided.");
		}
		merchantReturnUrl = order.returnUrl;
		Date now = new Date();
		setDate(now);
	}

	public void redirectToMerchant() {

	}

	public String getCardMaskedPan() {
		LOG.debugf("Functioneaza probabil");
		return order != null ? order.cardMaskedPan : "";
	}

	public void setCardMaskedPan() {}

	public String getCardExpiration() {
		return order != null ? order.cardExpiration : "";
	}

	public String getCardholderName() {
		return order != null ? order.cardholderName : "";
		}

	public Long getAmount() {
		return order != null ? order.amount : 0L;
	}

	public void setAmount() {};

	public String getMerchantReturnUrl() {
		return merchantReturnUrl;
	}

	public void setMerchantReturnUrl(String redirect) {
		this.merchantReturnUrl = redirect;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return order != null ? order.description : "";
	}

	public void setDescription(String description) {
	}

}
