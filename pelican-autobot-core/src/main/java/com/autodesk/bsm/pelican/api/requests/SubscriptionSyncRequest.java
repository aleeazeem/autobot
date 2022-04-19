package com.autodesk.bsm.pelican.api.requests;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Request object to create subscription in the micro service world Created by vineel on 5/17/18.
 */
public class SubscriptionSyncRequest implements Serializable {
    private static final long serialVersionUID = 6688485377102847852L;

    private Long id;
    private String externalKey;

    private DateTime created;
    private DateTime lastModified;

    private Long appFamilyId;

    private Integer status;
    private Integer quantity;
    private Integer quantityToReduce;
    private Integer usageType;
    private Integer offeringType;

    private DateTime nextBillingDate;
    private DateTime expirationDate;
    private DateTime resolveByDate;

    private Integer ecStatus;
    private DateTime ecStatusLastUpdated;

    private Long userId;
    private String userExternalKey;

    private Long planId;
    private String planExternalKey;
    private Long offerId;
    private Long priceId;
    private Long paymentProfileId;
    private Long addedToSubscriptionId;
    private Long purchaseOrderId;
    private Long paymentGatewayConfigId;

    private Integer daysCredited;
    private Integer billingCount;

    private boolean paymentPending;
    private DateTime lastRenewalReminderTimeStamp;
    private boolean emailRemindersEnabled;
    private boolean autoRenewEnabled;

    private Long promotionId;
    private Integer promotionCyclesRemaining;
    private Integer promotionCyclesUsed;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(final DateTime created) {
        this.created = created;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Long getAppFamilyId() {
        return appFamilyId;
    }

    public void setAppFamilyId(final Long appFamilyId) {
        this.appFamilyId = appFamilyId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantityToReduce() {
        return quantityToReduce;
    }

    public void setQuantityToReduce(final Integer quantityToReduce) {
        this.quantityToReduce = quantityToReduce;
    }

    public Integer getUsageType() {
        return usageType;
    }

    public void setUsageType(final Integer usageType) {
        this.usageType = usageType;
    }

    public Integer getOfferingType() {
        return offeringType;
    }

    public void setOfferingType(final Integer offeringType) {
        this.offeringType = offeringType;
    }

    public DateTime getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(final DateTime nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public DateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final DateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public DateTime getResolveByDate() {
        return resolveByDate;
    }

    public void setResolveByDate(final DateTime resolveByDate) {
        this.resolveByDate = resolveByDate;
    }

    public Integer getEcStatus() {
        return ecStatus;
    }

    public void setEcStatus(final Integer ecStatus) {
        this.ecStatus = ecStatus;
    }

    public DateTime getEcStatusLastUpdated() {
        return ecStatusLastUpdated;
    }

    public void setEcStatusLastUpdated(final DateTime ecStatusLastUpdated) {
        this.ecStatusLastUpdated = ecStatusLastUpdated;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public String getUserExternalKey() {
        return userExternalKey;
    }

    public void setUserExternalKey(final String userExternalKey) {
        this.userExternalKey = userExternalKey;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(final Long planId) {
        this.planId = planId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(final Long offerId) {
        this.offerId = offerId;
    }

    public Long getPriceId() {
        return priceId;
    }

    public void setPriceId(final Long priceId) {
        this.priceId = priceId;
    }

    public Long getPaymentProfileId() {
        return paymentProfileId;
    }

    public void setPaymentProfileId(final Long paymentProfileId) {
        this.paymentProfileId = paymentProfileId;
    }

    public Long getAddedToSubscriptionId() {
        return addedToSubscriptionId;
    }

    public void setAddedToSubscriptionId(final Long addedToSubscriptionId) {
        this.addedToSubscriptionId = addedToSubscriptionId;
    }

    public Integer getDaysCredited() {
        return daysCredited;
    }

    public void setDaysCredited(final Integer daysCredited) {
        this.daysCredited = daysCredited;
    }

    public Integer getBillingCount() {
        return billingCount;
    }

    public void setBillingCount(final Integer billingCount) {
        this.billingCount = billingCount;
    }

    public boolean isPaymentPending() {
        return paymentPending;
    }

    public void setPaymentPending(final boolean paymentPending) {
        this.paymentPending = paymentPending;
    }

    public DateTime getLastRenewalReminderTimeStamp() {
        return lastRenewalReminderTimeStamp;
    }

    public void setLastRenewalReminderTimeStamp(final DateTime lastRenewalReminderTimeStamp) {
        this.lastRenewalReminderTimeStamp = lastRenewalReminderTimeStamp;
    }

    public boolean isEmailRemindersEnabled() {
        return emailRemindersEnabled;
    }

    public void setEmailRemindersEnabled(final boolean emailRemindersEnabled) {
        this.emailRemindersEnabled = emailRemindersEnabled;
    }

    public boolean isAutoRenewEnabled() {
        return autoRenewEnabled;
    }

    public void setAutoRenewEnabled(final boolean autoRenewEnabled) {
        this.autoRenewEnabled = autoRenewEnabled;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(final Long promotionId) {
        this.promotionId = promotionId;
    }

    public Integer getPromotionCyclesRemaining() {
        return promotionCyclesRemaining;
    }

    public void setPromotionCyclesRemaining(final Integer promotionCyclesRemaining) {
        this.promotionCyclesRemaining = promotionCyclesRemaining;
    }

    public String getPlanExternalKey() {
        return planExternalKey;
    }

    public void setPlanExternalKey(final String planExternalKey) {
        this.planExternalKey = planExternalKey;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(final Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getPaymentGatewayConfigId() {
        return paymentGatewayConfigId;
    }

    public void setPaymentGatewayConfigId(final Long paymentGatewayConfigId) {
        this.paymentGatewayConfigId = paymentGatewayConfigId;
    }

    public Integer getPromotionCyclesUsed() {
        return promotionCyclesUsed;
    }

    public void setPromotionCyclesUsed(final Integer promotionCyclesUsed) {
        this.promotionCyclesUsed = promotionCyclesUsed;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SubscriptionSyncRequest [id=").append(id).append(", externalKey=").append(externalKey)
            .append(", created=").append(created).append(", lastModified=").append(lastModified)
            .append(", appFamilyId=").append(appFamilyId).append(", status=").append(status).append(", quantity=")
            .append(quantity).append(", quantityToReduce=").append(quantityToReduce).append(", usageType=")
            .append(usageType).append(", nextBillingDate=").append(nextBillingDate).append(", expirationDate=")
            .append(expirationDate).append(", resolveByDate=").append(resolveByDate).append(", ecStatus=")
            .append(ecStatus).append(", ecStatusLastUpdated=").append(ecStatusLastUpdated).append(", userId=")
            .append(userId).append(", userExternalKey=").append(userExternalKey).append(", planId=").append(planId)
            .append(", planExternalKey=").append(planExternalKey).append(", purchaseOrderId=").append(purchaseOrderId)
            .append(", offerId=").append(offerId).append(", priceId=").append(priceId).append(", paymentProfileId=")
            .append(paymentProfileId).append(", addedToSubscriptionId=").append(addedToSubscriptionId)
            .append(", daysCredited=").append(daysCredited).append(", billingCount=").append(billingCount)
            .append(", paymentPending=").append(paymentPending).append(", lastRenewalReminderTimeStamp=")
            .append(lastRenewalReminderTimeStamp).append(", emailRemindersEnabled=").append(emailRemindersEnabled)
            .append(", autoRenewEnabled=").append(autoRenewEnabled).append(", promotionId=").append(promotionId)
            .append(", promotionCyclesRemaining=").append(promotionCyclesRemaining).append(", promotionCyclesUsed=")
            .append(promotionCyclesUsed).append("]").append(", paymentGatewayConfigId=").append(paymentGatewayConfigId);
        return builder.toString();
    }
}
