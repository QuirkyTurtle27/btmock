package ro.gs1.btmock.registration;

import org.jboss.resteasy.reactive.RestForm;

public class RegisterForm {
    @RestForm("userName")           public String userName;
    @RestForm("password")           public String password;
    @RestForm("orderNumber")        public String orderNumber;
    @RestForm("amount")             public String amount;
    @RestForm("currency")           public String currency;
    @RestForm("returnUrl")          public String returnUrl;
    @RestForm("description")        public String description;

    @RestForm("language")           public String language;
    @RestForm("pageView")           public String pageView;
    @RestForm("childId")            public String childId;
    @RestForm("clientId")           public String clientId;
    @RestForm("bindingId")          public String bindingId;
    @RestForm("sessionTimeoutSecs") public String sessionTimeoutSecs;
    @RestForm("expirationDate")     public String expirationDate;

    @RestForm("jsonParams")         public String jsonParams;
    @RestForm("orderBundle")        public String orderBundle;
}
