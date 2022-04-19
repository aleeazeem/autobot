package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This is a pojo for SubscriptionQuantity object in PurchaseOrder API for add seats
 *
 * @author t_joshv
 */
public class SubscriptionQuantity {

    private SubscriptionQuantityRequest subscriptionQuantityRequest;
    private SubscriptionQuantityResponse subscriptionQuantityResponse;

    public static class SubscriptionQuantityRequest {
        private String subscriptionId;
        private String priceId;
        private String quantity;
        private String offeringExternalKey;
        private String offeringId;
        private String offerId;
        private String offerExternalKey;
        private String prorationStartDate;
        private String prorationEndDate;

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

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

        public String getOfferingExternalKey() {
            return offeringExternalKey;
        }

        @XmlAttribute
        public void setOfferingExternalKey(final String offeringExternalKey) {
            this.offeringExternalKey = offeringExternalKey;
        }

        public String getOfferingId() {
            return offeringId;
        }

        @XmlAttribute
        public void setOfferingId(final String offeringId) {
            this.offeringId = offeringId;
        }

        public String getOfferId() {
            return offerId;
        }

        @XmlAttribute
        public void setOfferId(final String offerId) {
            this.offerId = offerId;
        }

        public String getOfferExternalKey() {
            return offerExternalKey;
        }

        @XmlAttribute
        public void setOfferExternalKey(final String offerExternalKey) {
            this.offerExternalKey = offerExternalKey;
        }

        public String getProrationStartDate() {
            return prorationStartDate;
        }

        @XmlAttribute
        public void setProrationStartDate(final String prorationStartDate) {
            this.prorationStartDate = prorationStartDate;
        }

        public String getProrationEndDate() {
            return prorationEndDate;
        }

        @XmlAttribute
        public void setProrationEndDate(final String prorationEndDate) {
            this.prorationEndDate = prorationEndDate;
        }

    }

    /**
     * Class to represent Subscription Quantity Response for Add Seats/Quantity.
     *
     * @author t_joshv
     */
    public static class SubscriptionQuantityResponse {
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

    public SubscriptionQuantityRequest getSubscriptionQuantityRequest() {
        return subscriptionQuantityRequest;
    }

    @XmlElement(name = "subscriptionQuantityRequest")
    public void setSubscriptionQuantityRequest(final SubscriptionQuantityRequest subscriptionQuantityRequest) {
        this.subscriptionQuantityRequest = subscriptionQuantityRequest;
    }

    public SubscriptionQuantityResponse getSubscriptionQuantityResponse() {
        return subscriptionQuantityResponse;
    }

    @XmlElement(name = "subscriptionQuantityResponse")
    public void setSubscriptionQuantityResponse(final SubscriptionQuantityResponse subscriptionQuantityResponse) {
        this.subscriptionQuantityResponse = subscriptionQuantityResponse;
    }
}
