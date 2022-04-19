package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This is pojo for subscription extension request Created by Shweta Hegde on 3/24/17.
 */
public class SubscriptionExtension {

    private SubscriptionExtensionRequest subscriptionExtensionRequest;
    private SubscriptionExtensionResponse subscriptionExtensionResponse;

    public static class SubscriptionExtensionRequest {

        private String priceId;
        private String quantity;
        private String subscriptionId;
        private String subscriptionRenewalDate;
        private String targetRenewalDate;

        public String getPriceId() {
            return priceId;
        }

        @XmlAttribute
        public void setPriceId(final String priceId) {
            this.priceId = priceId;
        }

        public String getQuantity() {
            return quantity;
        }

        @XmlAttribute
        public void setQuantity(final String quantity) {
            this.quantity = quantity;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        public String getSubscriptionRenewalDate() {
            return subscriptionRenewalDate;
        }

        @XmlAttribute
        public void setSubscriptionRenewalDate(final String subscriptionRenewalDate) {
            this.subscriptionRenewalDate = subscriptionRenewalDate;
        }

        public String getTargetRenewalDate() {
            return targetRenewalDate;
        }

        @XmlAttribute
        public void setTargetRenewalDate(final String targetRenewalDate) {
            this.targetRenewalDate = targetRenewalDate;
        }
    }

    /**
     * Class to represent Subscription Extension Response.
     *
     * @author t_joshv
     */
    public static class SubscriptionExtensionResponse {

        private String subscriptionId;
        private String currencyName;
        private String offeringType;
        private String offerId;
        private String fulfillmentGroupId;
        private String productLineName;
        private String productLineCode;
        private String unitPrice;
        private String eventGroupId;
        private String timeStamp;
        private Subscriptions subscriptions;
        private ChargeDetails chargeDetails;

        public String getCurrencyName() {
            return currencyName;
        }

        public String getOfferingType() {
            return offeringType;
        }

        public String getOfferId() {
            return offerId;
        }

        public String getFulfillmentGroupId() {
            return fulfillmentGroupId;
        }

        public String getProductLineName() {
            return productLineName;
        }

        public String getProductLineCode() {
            return productLineCode;
        }

        public String getUnitPrice() {
            return unitPrice;
        }

        public String getEventGroupId() {
            return eventGroupId;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public Subscriptions getSubscriptions() {
            return subscriptions;
        }

        public ChargeDetails getChargeDetails() {
            return chargeDetails;
        }

        @XmlAttribute
        public void setCurrencyName(final String currencyName) {
            this.currencyName = currencyName;
        }

        @XmlAttribute
        public void setOfferingType(final String offeringType) {
            this.offeringType = offeringType;
        }

        @XmlAttribute
        public void setOfferId(final String offerId) {
            this.offerId = offerId;
        }

        @XmlAttribute
        public void setFulfillmentGroupId(final String fulfillmentGroupId) {
            this.fulfillmentGroupId = fulfillmentGroupId;
        }

        @XmlAttribute
        public void setProductLineName(final String productLineName) {
            this.productLineName = productLineName;
        }

        @XmlAttribute
        public void setProductLineCode(final String productLineCode) {
            this.productLineCode = productLineCode;
        }

        @XmlAttribute
        public void setUnitPrice(final String unitPrice) {
            this.unitPrice = unitPrice;
        }

        @XmlAttribute
        public void setEventGroupId(final String eventGroupId) {
            this.eventGroupId = eventGroupId;
        }

        @XmlAttribute
        public void setTimeStamp(final String timeStamp) {
            this.timeStamp = timeStamp;
        }

        @XmlElement(name = "subscriptions")
        public void setSubscriptions(final Subscriptions subscriptions) {
            this.subscriptions = subscriptions;
        }

        @XmlElement(name = "chargeDetails")
        public void setChargeDetails(final ChargeDetails chargeDetails) {
            this.chargeDetails = chargeDetails;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

    }

    public SubscriptionExtensionRequest getSubscriptionExtensionRequest() {
        return subscriptionExtensionRequest;
    }

    @XmlElement
    public void setSubscriptionExtensionRequest(final SubscriptionExtensionRequest subscriptionExtensionRequest) {
        this.subscriptionExtensionRequest = subscriptionExtensionRequest;
    }

    public SubscriptionExtensionResponse getSubscriptionExtensionResponse() {
        return subscriptionExtensionResponse;
    }

    @XmlElement
    public void setSubscriptionExtensionResponse(final SubscriptionExtensionResponse subscriptionExtensionResponse) {
        this.subscriptionExtensionResponse = subscriptionExtensionResponse;
    }
}
