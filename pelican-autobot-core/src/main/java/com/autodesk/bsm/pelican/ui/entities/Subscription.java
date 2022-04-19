package com.autodesk.bsm.pelican.ui.entities;

/*
 * This is a subscription class which is used To get and Set the values to the different fields the object from UI and
 * vice versa
 *
 * @Author: Umar Saeed
 */
public class Subscription extends BaseEntity {

    private String id;
    private String externalKey;
    private String subscriptionPlan;
    private String subscriptionOffer;
    private String user;
    private String quantity;
    private String status;
    private String autoRenew;
    private String daysCredited;
    private String nextBillingDate;
    private String nextBillingCharge;
    private String nextBillingPriceId;
    private String expirationDate;
    private String userName;
    private String creditDays;
    private String nextBillingPriceAmount;

    // Get Name field value
    public String getId() {
        return id;
    }

    // Set Name field value
    public void setId(final String value) {
        this.id = value;
    }

    // Get subscription plan field value
    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    // Set subscription plan field value
    public void setSubscriptionPlan(final String value) {
        this.subscriptionPlan = value;
    }

    // Get External Key field value
    public String getExternalKey() {
        return externalKey;
    }

    // Set External Key field value
    public void setExternalKey(final String value) {
        this.externalKey = value;
    }

    // Get subscription offer field value
    public String getSubscriptionOffer() {
        return subscriptionOffer;
    }

    // Set subscription offer field value
    public void setSubscriptionOffer(final String value) {
        this.subscriptionOffer = value;
    }

    // Get user field value
    public String getUser() {
        return user;
    }

    // Set user field value
    public void setUser(final String value) {
        this.user = value;
    }

    // Get quantity field value
    public String getQuantity() {
        return quantity;
    }

    // Set quantity field value
    public void setQuantity(final String value) {
        this.quantity = value;
    }

    // Get status field value
    public String getStatus() {
        return status;
    }

    // Set status field value
    public void setStatus(final String value) {
        this.status = value;
    }

    // Get auto Renew field value
    public String getAutoRenew() {
        return autoRenew;
    }

    // Set auto Renew field value
    public void setAutoRenew(final String value) {
        this.autoRenew = value;
    }

    // Get days Credited field value
    public String getDaysCredited() {
        return daysCredited;
    }

    // Set days Credited field value
    public void setDaysCredited(final String value) {
        this.daysCredited = value;
    }

    // Get next Billing Date field value
    public String getNextBillingDate() {
        return nextBillingDate;
    }

    // Set next Billing Date field value
    public void setNextBillingDate(final String value) {
        this.nextBillingDate = value;
    }

    // Get next Billing Charge field value
    public String getNextBillingCharge() {
        return nextBillingCharge;
    }

    // Set next Billing Charge field value
    public void setNextBillingCharge(final String value) {
        this.nextBillingCharge = value;
    }

    // Get next Billing Price Id field value
    public String getNextBillingPriceId() {
        return nextBillingPriceId;
    }

    // Set next Billing Price Id field value
    public void setNextBillingPriceId(final String value) {
        this.nextBillingPriceId = value;
    }

    // Get expiration Date field value
    public String getExpirationDate() {
        return expirationDate;
    }

    // Set expiration Date field value
    public void setExpirationDate(final String value) {
        this.expirationDate = value;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userExternalKey) {
        this.userName = userExternalKey;
    }

    public String getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(final String creditDays) {
        this.creditDays = creditDays;
    }

    public String getNextBillingPriceAmount() {
        return nextBillingPriceAmount;
    }

    public void setNextBillingPriceAmount(final String nextBillingPriceAmount) {
        this.nextBillingPriceAmount = nextBillingPriceAmount;
    }

}
