package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.enums.OfferingType;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * One of the lineItem in purchase order
 *
 * @author yin
 */
public class Offering {

    private OfferingRequest offeringRequest;
    private OfferingResponse offeringResponse;

    public static class OfferingRequest {

        private String offering;
        private String priceId;
        private String upgradeFromSubscriptionId;
        private int quantity;
        private String offerExternalKey;
        private String offeringExternalKey; // subscription plan
        private String offerId;
        private String offeringId;

        public String getOffering() {
            return offering;
        }

        @XmlAttribute
        public void setOffering(final String offering) {
            this.offering = offering;
        }

        public String getPriceId() {
            return priceId;
        }

        @XmlAttribute
        public void setPriceId(final String priceId) {
            this.priceId = priceId;
        }

        public String getUpgradeFromSubscriptionId() {
            return upgradeFromSubscriptionId;
        }

        @XmlAttribute
        public void setUpgradeFromSubscriptionId(final String id) {
            this.upgradeFromSubscriptionId = id;
        }

        public int getQuantity() {
            return quantity;
        }

        @XmlAttribute
        public void setQuantity(final int quantity) {
            this.quantity = quantity;
        }

        public String getOfferExternalKey() {
            return offerExternalKey;
        }

        @XmlAttribute
        public void setOfferExternalKey(final String key) {
            this.offerExternalKey = key;
        }

        public String getOfferingExternalKey() {
            return offeringExternalKey;
        }

        @XmlAttribute
        public void setOfferingExternalKey(final String offeringExternalKey) {
            this.offeringExternalKey = offeringExternalKey;
        }

        public String getOfferId() {
            return offerId;
        }

        @XmlAttribute
        public String setOfferId(final String offerId) {
            return this.offerId = offerId;
        }

        public String getOfferingId() {
            return offeringId;
        }

        @XmlAttribute
        public String setOfferingId(final String offeringId) {
            return this.offeringId = offeringId;
        }
    }

    public static class OfferingResponse {

        private String subscriptionId;
        private String unitPrice;
        private String currencyName;
        private String offerId;
        private String fulfillmentGroupId;
        private String timestamp;
        private String eventGroupId;
        private OfferingType offeringType;
        private Subscriptions subscriptions;
        private ChargeDetails chargeDetails;
        private SerialNumbers serialNumbers;
        private String productLineName;
        private String productLineCode;

        public String getSubscriptionId() {
            return subscriptionId;
        }

        @XmlAttribute
        public void setSubscriptionId(final String id) {
            this.subscriptionId = id;
        }

        public String getUnitPrice() {
            return unitPrice;
        }

        @XmlAttribute
        public void setUnitPrice(final String price) {
            this.unitPrice = price;
        }

        public String getCurrencyName() {
            return currencyName;
        }

        @XmlAttribute
        public void setCurrencyName(final String name) {
            this.currencyName = name;
        }

        public String getOfferId() {
            return offerId;
        }

        @XmlAttribute
        public void setOfferId(final String id) {
            this.offerId = id;
        }

        public String getfulfillmentGroupId() {
            return fulfillmentGroupId;
        }

        @XmlAttribute(name = "fulfillmentGroupId")
        public void setFulfillmentGroupId(final String id) {
            this.fulfillmentGroupId = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @XmlAttribute
        public void setTimestamp(final String timeStamp) {
            this.timestamp = timeStamp;
        }

        public String getEventGroupId() {
            return eventGroupId;
        }

        @XmlAttribute
        public void setEventGroupId(final String id) {
            this.eventGroupId = id;
        }

        public OfferingType getOfferingType() {
            return offeringType;
        }

        @XmlAttribute
        public void setOfferingType(final OfferingType type) {
            this.offeringType = type;
        }

        public SerialNumbers getSerialNumbers() {
            return serialNumbers;
        }

        @XmlElement(name = "serialNumbers")
        public void setSerialNumbers(final SerialNumbers serialNumbers) {
            this.serialNumbers = serialNumbers;
        }

        public Subscriptions getSubscriptions() {
            return subscriptions;
        }

        @XmlElement(name = "subscriptions")
        public void setSubscriptions(final Subscriptions subscriptions) {
            this.subscriptions = subscriptions;
        }

        public ChargeDetails getChargeDetails() {
            return chargeDetails;
        }

        @XmlElement(name = "chargeDetails")
        public void setChargeDetails(final ChargeDetails chargeDetails) {
            this.chargeDetails = chargeDetails;
        }

        public String getProductLineName() {
            return productLineName;
        }

        @XmlAttribute
        public String setProductLineName(final String productLineName) {
            return this.productLineName = productLineName;
        }

        public String getProductLineCode() {
            return productLineCode;
        }

        @XmlAttribute
        public String setProductLineCode(final String productLineCode) {
            return this.productLineCode = productLineCode;
        }
    }

    static class SerialNumber {

        private String serialNumber;

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(final String serialNumber) {
            this.serialNumber = serialNumber;
        }
    }

    public static class SerialNumbers {

        private List<SerialNumber> serialNumberList;

        public List<SerialNumber> getSerialNumber() {
            return serialNumberList;
        }

        @XmlElement(name = "serialNumber")
        public void setSerialNumber(final List<SerialNumber> serialNumberList) {
            this.serialNumberList = serialNumberList;
        }
    }

    public OfferingRequest getOfferingRequest() {
        return offeringRequest;
    }

    public void setOfferingRequest(final OfferingRequest offeringRequest) {
        this.offeringRequest = offeringRequest;
    }

    public OfferingResponse getOfferingResponse() {
        return offeringResponse;
    }

    public void setOfferingResponse(final OfferingResponse offeringResponse) {
        this.offeringResponse = offeringResponse;
    }
}
