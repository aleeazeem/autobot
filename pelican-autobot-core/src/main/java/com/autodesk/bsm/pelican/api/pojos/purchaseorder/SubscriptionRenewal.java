package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This is a pojo for subscriptionRenewal object in PurchaseOrder API for renewal
 *
 * @author Vineel
 */
public class SubscriptionRenewal {

    private SubscriptionRenewalRequest subscriptionRenewalRequest;
    private SubscriptionRenewalResponse subscriptionRenewalResponse;

    public static class SubscriptionRenewalRequest {
        private String subscriptionId;
        private String currencyId;
        private String currencyName;

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        public String getCurrencyId() {
            return currencyId;
        }

        @XmlAttribute
        public void setCurrencyId(final String currencyId) {
            this.currencyId = currencyId;
        }

        public String getCurrencyName() {
            return currencyName;
        }

        @XmlAttribute
        public void setCurrencyName(final String currencyName) {
            this.currencyName = currencyName;
        }

    }

    public static class SubscriptionRenewalResponse {
        private String subscriptionId;
        private String subscriptionOfferId;
        private String billingOptionId;
        private String nextBillingDate;
        private String unitPrice;
        private String currencyName;
        private String planName;
        private String eventGroupId;
        private String timestamp;
        private ChargeDetails chargeDetails;
        private Subscriptions subscriptions;

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        /**
         * @return the subscriptionOfferId
         */
        public String getSubscriptionOfferId() {
            return subscriptionOfferId;
        }

        public Subscriptions getSubscriptions() {
            return subscriptions;
        }

        /**
         * @param subscriptionOfferId the subscriptionOfferId to set
         */
        @XmlAttribute
        public void setSubscriptionOfferId(final String subscriptionOfferId) {
            this.subscriptionOfferId = subscriptionOfferId;
        }

        @XmlElement(name = "subscriptions")
        public void setSubscriptions(final Subscriptions subscriptions) {
            this.subscriptions = subscriptions;
        }

        /**
         * @return the billingOptionId
         */
        public String getBillingOptionId() {
            return billingOptionId;
        }

        /**
         * @param billingOptionId the billingOptionId to set
         */
        @XmlAttribute
        public void setBillingOptionId(final String billingOptionId) {
            this.billingOptionId = billingOptionId;
        }

        /**
         * @return the nextBillingDate
         */
        public String getNextBillingDate() {
            return nextBillingDate;
        }

        /**
         * @param nextBillingDate the nextBillingDate to set
         */
        @XmlAttribute
        public void setNextBillingDate(final String nextBillingDate) {
            this.nextBillingDate = nextBillingDate;
        }

        /**
         * @return the unitPrice
         */
        public String getUnitPrice() {
            return unitPrice;
        }

        /**
         * @param unitPrice the unitPrice to set
         */
        @XmlAttribute
        public void setUnitPrice(final String unitPrice) {
            this.unitPrice = unitPrice;
        }

        /**
         * @return the currencyName
         */

        public String getCurrencyName() {
            return currencyName;
        }

        /**
         * @param currencyName the currencyName to set
         */
        @XmlAttribute
        public void setCurrencyName(final String currencyName) {
            this.currencyName = currencyName;
        }

        /**
         * @return the planName
         */
        public String getPlanName() {
            return planName;
        }

        /**
         * @param planName the planName to set
         */
        @XmlAttribute
        public void setPlanName(final String planName) {
            this.planName = planName;
        }

        /**
         * @return the eventGroupId
         */
        public String getEventGroupId() {
            return eventGroupId;
        }

        /**
         * @param eventGroupId the eventGroupId to set
         */
        @XmlAttribute
        public void setEventGroupId(final String eventGroupId) {
            this.eventGroupId = eventGroupId;
        }

        /**
         * @return the timestamp
         */
        public String getTimestamp() {
            return timestamp;
        }

        /**
         * @param timestamp the timestamp to set
         */
        @XmlAttribute
        public void setTimestamp(final String timestamp) {
            this.timestamp = timestamp;
        }

        public ChargeDetails getChargeDetails() {
            return chargeDetails;
        }

        @XmlElement(name = "chargeDetails")
        public void setChargeDetails(final ChargeDetails chargeDetails) {
            this.chargeDetails = chargeDetails;
        }
    }

    public SubscriptionRenewalRequest getSubscriptionRenewalRequest() {
        return subscriptionRenewalRequest;
    }

    @XmlElement
    public void setSubscriptionRenewalRequest(final SubscriptionRenewalRequest subscriptionRenewalRequest) {
        this.subscriptionRenewalRequest = subscriptionRenewalRequest;
    }

    /**
     * @return the subscriptionRenewalResponse
     */
    public SubscriptionRenewalResponse getSubscriptionRenewalResponse() {
        return subscriptionRenewalResponse;
    }

    /**
     * @param subscriptionRenewalResponse the subscriptionRenewalResponse to set
     */
    @XmlElement
    public void setSubscriptionRenewalResponse(final SubscriptionRenewalResponse subscriptionRenewalResponse) {
        this.subscriptionRenewalResponse = subscriptionRenewalResponse;
    }
}
