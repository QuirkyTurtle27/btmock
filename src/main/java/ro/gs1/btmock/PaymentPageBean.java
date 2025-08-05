package ro.gs1.btmock;

import java.io.IOException;
import java.io.Serializable;

import org.jboss.logging.Logger;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import ro.gs1.btmock.entity.OrderEntity;

@Named
@ViewScoped
public class PaymentPageBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(PaymentPageBean.class);

	private OrderEntity order;
	private String orderId;
	private String redirect = "/paymentfailed.xhtml";

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
		redirect = redirect + "?order=" + orderId;
	}

	public void actionPay() {
		try {
			LOG.errorf("processPayment(): Payment failed for orderId=%s", orderId);
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext ext = context.getExternalContext();
            ext.redirect(ext.getRequestContextPath() + redirect + "?order=" + orderId);
        } catch (IOException e) {
        	LOG.error("processPayment(): Exception while redirecting to paymentfailed.xhtml", e);
        }
	}

	public String getDescription() {
		return order != null ? order.description : "";
	}

	public Long getAmount() {
		return order != null ? order.amount : 0L;
	}

	public Integer getSessionTimeoutSecs() {
		return order != null ? order.sessionTimeoutSecs : 0;
	}

	public void setSessionTimeoutSecs(Integer time) {
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

}
