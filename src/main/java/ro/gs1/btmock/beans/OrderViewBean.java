package ro.gs1.btmock.beans;

import java.io.Serializable;

import org.jboss.logging.Logger;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import ro.gs1.btmock.entity.OrderEntity;

@Named
public class OrderViewBean implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(OrderViewBean.class);

	private String orderId;
	private OrderEntity order;

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
	}
}
