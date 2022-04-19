package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class SubscriptionPlan {

    private SubscriptionPlanPurchaseRequest purchaseRequest;
    private SubscriptionPlanPurchaseResponse purchaseResponse;

    public static class SubscriptionPlanPurchaseRequest {

        private String currencyId;
        private String offerExtKey;
        private String upgradeFromSubscriptionId;

        public String getCurrencyId() {
            return currencyId;
        }

        @XmlAttribute
        public void setCurrencyId(final String currency) {
            this.currencyId = currency;
        }

        public String getOfferExternalKey() {
            return offerExtKey;
        }

        @XmlAttribute
        public void setOfferExternalKey(final String key) {
            this.offerExtKey = key;
        }

        public String getUpgradeFromSubscriptionId() {
            return upgradeFromSubscriptionId;
        }

        @XmlAttribute
        public void setUpgradeFromSubscriptionId(final String id) {
            this.upgradeFromSubscriptionId = id;
        }
    }

    public static class SubscriptionPlanPurchaseResponse {

        private String subscriptionId;
        private String subscriptionOfferId;
        private String unitPrice;
        private String currencyName;
        private String planName;
        private String nextBillingDate;
        private String eventGroupId;
        private String timestamp;

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String id) {
            this.subscriptionId = id;
        }

        public String getSubscriptionOfferId() {
            return subscriptionOfferId;
        }

        @XmlAttribute
        public void setSubscriptionOfferId(final String id) {
            this.subscriptionOfferId = id;
        }

        public String getUnitPrice() {
            return unitPrice;
        }

        @XmlAttribute
        public void setUnitPrice(final String unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getCurrencyName() {
            return currencyName;
        }

        @XmlAttribute
        public void setCurrencyName(final String value) {
            this.currencyName = value;
        }

        public String getPlanName() {
            return planName;
        }

        @XmlAttribute
        public void setPlanName(final String value) {
            this.planName = value;
        }

        public String getNextBillingDate() {
            return nextBillingDate;
        }

        @XmlAttribute
        public void setNextBillingDate(final String value) {
            this.nextBillingDate = value;
        }

        public String getEventGroupId() {
            return eventGroupId;
        }

        @XmlAttribute
        public void setEventGroupId(final String id) {
            this.eventGroupId = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @XmlAttribute
        public void setTimestamp(final String value) {
            this.timestamp = value;
        }
    }

    public SubscriptionPlanPurchaseRequest getSubscriptionPlanPurchaseRequest() {
        return purchaseRequest;
    }

    @XmlElement
    public void setSubscriptionPlanPurchaseRequest(final SubscriptionPlanPurchaseRequest request) {
        this.purchaseRequest = request;

    }

    public SubscriptionPlanPurchaseResponse getSubscriptionPlanPurchaseResponse() {
        return purchaseResponse;
    }

    @XmlElement
    public void setSubscriptionPlanPurchaseResponse(final SubscriptionPlanPurchaseResponse resonse) {
        this.purchaseResponse = resonse;
    }

}
