package ro.gs1.btmock.beans;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import ro.gs1.btmock.entity.CreditCardEntity;
import ro.gs1.btmock.entity.OrderEntity;

@Named
@RequestScoped
public class IndexPageBean {

    private List<CreditCardEntity> creditCards;
    private List<OrderEntity> orders;

    @PostConstruct
    public void init() {
        creditCards = CreditCardEntity.listAll();
        orders = OrderEntity.listAll();
    }

    public List<CreditCardEntity> getCreditCards() {
        return creditCards;
    }

    public List<OrderEntity> getOrders() {
        return orders;
    }
}
