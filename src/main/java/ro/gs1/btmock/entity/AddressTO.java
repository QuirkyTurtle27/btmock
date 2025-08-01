package ro.gs1.btmock.entity;

import java.io.Serializable;

public class AddressTO implements Serializable {

   private static final long serialVersionUID = -8750060309205834562L;

   private String deliveryType;

   private String country;

   private String city;

   private String postAddress;

   private String postAddress2;

   private String postAddress3;

   private String postalCode;

   private String state;

   public String getDeliveryType() {
      return deliveryType;
   }

   public void setDeliveryType(String deliveryType) {
      this.deliveryType = deliveryType;
   }

   public String getCountry() {
      return country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   public String getCity() {
      return city;
   }

   public void setCity(String city) {
      this.city = city;
   }

   public String getPostAddress() {
      return postAddress;
   }

   public void setPostAddress(String postAddress) {
      this.postAddress = postAddress;
   }

   public String getPostAddress2() {
      return postAddress2;
   }

   public void setPostAddress2(String postAddress2) {
      this.postAddress2 = postAddress2;
   }

   public String getPostAddress3() {
      return postAddress3;
   }

   public void setPostAddress3(String postAddress3) {
      this.postAddress3 = postAddress3;
   }

   public String getPostalCode() {
      return postalCode;
   }

   public void setPostalCode(String postalCode) {
      this.postalCode = postalCode;
   }

   public String getState() {
      return state;
   }

   public void setState(String state) {
      this.state = state;
   }
}
