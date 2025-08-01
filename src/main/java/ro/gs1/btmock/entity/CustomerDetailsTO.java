package ro.gs1.btmock.entity;

import java.io.Serializable;

public class CustomerDetailsTO implements Serializable {

   private static final long serialVersionUID = -5394160661076039226L;

   private String email;

   private String phone;

   private String contact;

   private AddressTO deliveryInfo;

   private AddressTO billingInfo;

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getPhone() {
      return phone;
   }

   public void setPhone(String phone) {
      this.phone = phone;
   }

   public String getContact() {
      return contact;
   }

   public void setContact(String contact) {
      this.contact = contact;
   }

   public AddressTO getDeliveryInfo() {
      return deliveryInfo;
   }

   public void setDeliveryInfo(AddressTO deliveryInfo) {
      this.deliveryInfo = deliveryInfo;
   }

   public AddressTO getBillingInfo() {
      return billingInfo;
   }

   public void setBillingInfo(AddressTO billingInfo) {
      this.billingInfo = billingInfo;
   }
}
