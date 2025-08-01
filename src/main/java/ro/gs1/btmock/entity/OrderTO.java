package ro.gs1.btmock.entity;

import java.io.Serializable;
import java.util.Map;

public class OrderTO implements Serializable {

   private static final long serialVersionUID = 867516960169504070L;

   private String orderId;

   private String orderNumber;

   private String userName;

   private String password;

   private Long amount;

   private Integer currency;

   private String returnUrl;

   private String description;

   private String language;

   private String pageView;

   private String email;

   private String childId;

   private String clientId;

   private String bindingId;

   private Integer sessionTimeoutSecs;

   private String expirationDate;

   private Map<String, Object> jsonParams;

   private OrderBundleTO orderBundle;

   private String formUrl;

   private String status;

   private long createdAt;

   public String getOrderId() {
      return orderId;
   }

   public void setOrderId(String orderId) {
      this.orderId = orderId;
   }

   public String getOrderNumber() {
      return orderNumber;
   }

   public void setOrderNumber(String orderNumber) {
      this.orderNumber = orderNumber;
   }

   public String getUserName() {
      return userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public Long getAmount() {
      return amount;
   }

   public void setAmount(Long amount) {
      this.amount = amount;
   }

   public Integer getCurrency() {
      return currency;
   }

   public void setCurrency(Integer currency) {
      this.currency = currency;
   }

   public String getReturnUrl() {
      return returnUrl;
   }

   public void setReturnUrl(String returnUrl) {
      this.returnUrl = returnUrl;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getLanguage() {
      return language;
   }

   public void setLanguage(String language) {
      this.language = language;
   }

   public String getPageView() {
      return pageView;
   }

   public void setPageView(String pageView) {
      this.pageView = pageView;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getChildId() {
      return childId;
   }

   public void setChildId(String childId) {
      this.childId = childId;
   }

   public String getClientId() {
      return clientId;
   }

   public void setClientId(String clientId) {
      this.clientId = clientId;
   }

   public String getBindingId() {
      return bindingId;
   }

   public void setBindingId(String bindingId) {
      this.bindingId = bindingId;
   }

   public Integer getSessionTimeoutSecs() {
      return sessionTimeoutSecs;
   }

   public void setSessionTimeoutSecs(Integer sessionTimeoutSecs) {
      this.sessionTimeoutSecs = sessionTimeoutSecs;
   }

   public String getExpirationDate() {
      return expirationDate;
   }

   public void setExpirationDate(String expirationDate) {
      this.expirationDate = expirationDate;
   }

   public Map<String, Object> getJsonParams() {
      return jsonParams;
   }

   public void setJsonParams(Map<String, Object> jsonParams) {
      this.jsonParams = jsonParams;
   }

   public OrderBundleTO getOrderBundle() {
      return orderBundle;
   }

   public void setOrderBundle(OrderBundleTO orderBundle) {
      this.orderBundle = orderBundle;
   }

   public String getFormUrl() {
      return formUrl;
   }

   public void setFormUrl(String formUrl) {
      this.formUrl = formUrl;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public long getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(long createdAt) {
      this.createdAt = createdAt;
   }
}