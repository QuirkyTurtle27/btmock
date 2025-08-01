package ro.gs1.btmock.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBundle {

    public String orderCreationDate;
    public CustomerDetails customerDetails;

    public static class CustomerDetails {
        public String email;
        public String phone;
        public String contact;
        public Address deliveryInfo;
        public Address billingInfo;
    }

    public static class Address {
        public String deliveryType;
        public String country;
        public String city;
        public String postAddress;
        public String postAddress2;
        public String postAddress3;
        public String postalCode;
        public String state;
    }
}