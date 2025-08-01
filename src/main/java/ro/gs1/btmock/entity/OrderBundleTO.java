package ro.gs1.btmock.entity;

import java.io.Serializable;

public class OrderBundleTO implements Serializable {

   private static final long serialVersionUID = -7099786838075828130L;

   private String orderCreationDate;

   private CustomerDetailsTO customerDetails;

   public String getOrderCreationDate() {
      return orderCreationDate;
   }

   public void setOrderCreationDate(String orderCreationDate) {
      this.orderCreationDate = orderCreationDate;
   }

   public CustomerDetailsTO getCustomerDetails() {
      return customerDetails;
   }

   public void setCustomerDetails(CustomerDetailsTO customerDetails) {
      this.customerDetails = customerDetails;
   }
}
