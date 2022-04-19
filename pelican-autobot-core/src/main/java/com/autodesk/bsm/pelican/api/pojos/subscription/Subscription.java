package com.autodesk.bsm.pelican.api.pojos.subscription;

import com.autodesk.bsm.pelican.api.pojos.BillingOption;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.enums.SalesChannel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Subscription extends PelicanPojo {

    private String id;
    private String externalKey;
    private String createdDate;
    private String ownerId;
    private String ownerExternalKey;
    private String appFamilyId;
    private String status;
    private boolean isAutoRenewEnabled;
    private String storedPaymentProfileId;
    private String nextBillingOfferId;
    private int creditDays;
    private String nextBillingPriceAmount;
    private String nextBillingPriceCurrencyId;
    private String nextBillingPriceCurrencyName;
    private String nextBillingDate;
    private String priceId;
    private String lastModified;
    private String expirationDate;
    private int quantity;
    private String offeringType;
    private CurrentOffer currentOffer;
    private BillingOption billingOption;
    private SubscriptionPlan subscriptionPlan;
    private Price price;
    private String userExternalKey;
    private String billingCharge;
    private String nextBillingPriceId;
    private String exportControlStatus;
    private String addedToSubscriptionId;
    private String nextBillingUnitPriceAmount;
    private int qtyToReduce;
    private SalesChannel salesChannel;
    private String aggrementNumber;

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    @XmlAttribute(name = "externalKey")
    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    @XmlAttribute(name = "createdDate")
    public void setCreatedDate(final String dateTime) {
        this.createdDate = dateTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    @XmlAttribute(name = "ownerId")
    public void setOwnerId(final String value) {
        this.ownerId = value;
    }

    public String getOwnerExternalKey() {
        return ownerExternalKey;
    }

    @XmlAttribute(name = "ownerExternalKey")
    public void setOwnerExternalKey(final String ownerExternalKey) {
        this.ownerExternalKey = ownerExternalKey;
    }

    public String getApplicationFamilyId() {
        return appFamilyId;
    }

    @XmlAttribute(name = "appFamilyId")
    public void setApplicationFamilyId(final String value) {
        this.appFamilyId = value;
    }

    public String getStatus() {
        return status;
    }

    @XmlAttribute(name = "status")
    public void setStatus(final String value) {
        this.status = value;
    }

    public boolean isAutoRenewed() {
        return isAutoRenewEnabled;
    }

    @XmlAttribute(name = "isAutoRenewEnabled")
    public void setAutoRenewed(final boolean value) {
        this.isAutoRenewEnabled = value;
    }

    public String getStorePaymentProfileId() {
        return storedPaymentProfileId;
    }

    @XmlAttribute(name = "storedPaymentProfileId")
    public void setStorePaymentProfileId(final String storedPaymentProfileId) {
        this.storedPaymentProfileId = storedPaymentProfileId;
    }

    public String getNextBillingOfferId() {
        return nextBillingOfferId;
    }

    @XmlAttribute(name = "nextBillingOfferId")
    public void setNextBillingOfferId(final String value) {
        this.nextBillingOfferId = value;
    }

    public int getCreditDays() {
        return creditDays;
    }

    @XmlAttribute(name = "creditDays")
    public void setCreditDays(final int days) {
        this.creditDays = days;
    }

    public String getNextBillingPriceAmount() {
        return nextBillingPriceAmount;
    }

    @XmlAttribute(name = "nextBillingPriceAmount")
    public void setNextBillingPriceAmount(final String nextBillingPriceAmount) {
        this.nextBillingPriceAmount = nextBillingPriceAmount;
    }

    public String getNextBillingPriceCurrencyId() {
        return nextBillingPriceCurrencyId;
    }

    @XmlAttribute(name = "nextBillingPriceCurrencyId")
    public void setNextBillingPriceCurrencyId(final String nextBillingPriceCurrencyId) {
        this.nextBillingPriceCurrencyId = nextBillingPriceCurrencyId;
    }

    public String getNextBillingPriceCurrencyName() {
        return nextBillingPriceCurrencyName;
    }

    @XmlAttribute(name = "nextBillingPriceCurrencyName")
    public void setNextBillingPriceCurrencyName(final String nextBillingPriceCurrencyName) {
        this.nextBillingPriceCurrencyName = nextBillingPriceCurrencyName;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    @XmlAttribute(name = "nextBillingDate")
    public void setNextBillingDate(final String dateTime) {
        this.nextBillingDate = dateTime;
    }

    public String getPriceId() {
        return priceId;
    }

    @XmlAttribute(name = "priceId")
    public void setPriceId(final String priceId) {
        this.priceId = priceId;
    }

    public String getLastModified() {
        return lastModified;
    }

    @XmlAttribute(name = "lastModified")
    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    public int getQuantity() {
        return quantity;
    }

    @XmlAttribute(name = "quantity")
    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public String getOfferingType() {
        return offeringType;
    }

    @XmlAttribute(name = "offeringType")
    public void setOfferingType(final String offeringType) {
        this.offeringType = offeringType;
    }

    public CurrentOffer getCurrentOffer() {
        return currentOffer;
    }

    @XmlElement
    public void setCurrentOffer(final CurrentOffer offer) {
        this.currentOffer = offer;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    @XmlAttribute(name = "expirationDate")
    public void setExpirationDate(final String dateTime) {
        this.expirationDate = dateTime;
    }

    public BillingOption getBillingOption() {
        return billingOption;
    }

    @XmlElement
    public void setBillingOption(final BillingOption option) {
        this.billingOption = option;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    @XmlElement
    public void setSubscriptionPlan(final SubscriptionPlan subsPlan) {
        this.subscriptionPlan = subsPlan;
    }

    public Price getPrice() {
        return price;
    }

    @XmlElement
    public void setPrice(final Price price) {
        this.price = price;
    }

    public String getUserExternalKey() {
        return userExternalKey;
    }

    public void setUserExternalKey(final String userExternalKey) {
        this.userExternalKey = userExternalKey;
    }

    public String getBillingCharge() {
        return billingCharge;
    }

    public void setBillingCharge(final String billingCharge) {
        this.billingCharge = billingCharge;
    }

    public String getNextBillingPriceId() {
        return nextBillingPriceId;
    }

    public void setNextBillingPriceId(final String nextBillingPriceId) {
        this.nextBillingPriceId = nextBillingPriceId;
    }

    public String getExportControlStatus() {
        return exportControlStatus;
    }

    @XmlAttribute(name = "exportControlStatus")
    public void setExportControlStatus(final String exportControlStatus) {
        this.exportControlStatus = exportControlStatus;
    }

    public String getAddedToSubscriptionId() {
        return addedToSubscriptionId;
    }

    @XmlAttribute(name = "addedToSubscriptionId")
    public void setAddedToSubscriptionId(final String addedToSubscriptionId) {
        this.addedToSubscriptionId = addedToSubscriptionId;
    }

    public String getNextBillingUnitPriceAmount() {
        return nextBillingUnitPriceAmount;
    }

    @XmlAttribute(name = "nextBillingUnitPriceAmount")
    public void setNextBillingUnitPriceAmount(final String nextBillingUnitPriceAmount) {
        this.nextBillingUnitPriceAmount = nextBillingUnitPriceAmount;
    }

    public int getQtyToReduce() {
        return qtyToReduce;
    }

    @XmlAttribute(name = "qtyToReduce")
    public void setQtyToReduce(final int qtyToReduce) {
        this.qtyToReduce = qtyToReduce;
    }

    public SalesChannel getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(SalesChannel salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getAggrementNumber() {
        return aggrementNumber;
    }

    public void setAggrementNumber(String aggrementNumber) {
        this.aggrementNumber = aggrementNumber;
    }
}
