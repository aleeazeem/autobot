package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.BillingOption;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi.Linkage;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;

/**
 * This class represents the JSON object of Subscription.
 *
 * @author jains
 */
public class JSubscriptionData {
    private EntityType type;
    private String id;
    private String externalKey;
    private String createdDate;
    private String ownerId;
    private String ownerExternalKey;
    private String appFamilyId;
    private Status status;
    private boolean isAutoRenewEnabled;
    private Integer storedPaymentProfileId;
    private Links links;
    private BillingOption billingOption;
    private Integer nextBillingOfferId;
    private Integer creditDays;
    private String nextBillingPriceAmount;
    private String nextBillingPriceCurrencyId;
    private String nextBillingPriceCurrencyName;
    private String nextBillingDate;
    private Integer priceId;
    private String lastModified;
    private int quantity;
    private OfferingType offeringType;
    private String addedToSubscriptionId;
    private String nextBillingUnitPriceAmount;
    private int qtyToReduce;
    private String exportControlStatus;

    /**
     * This class represents JSON object of Links inside Subscription JSON object
     */
    public static class Links {
        private Linkage price;
        private Linkage offering;
        private Linkage billingPlan;

        public Linkage getPrice() {
            return price;
        }

        public void setPrice(final Linkage price) {
            this.price = price;
        }

        public void setOffering(final Linkage offering) {
            this.offering = offering;
        }

        public Linkage getOffering() {
            return offering;
        }

        public void setBillingPlan(final Linkage billingPlan) {
            this.billingPlan = billingPlan;
        }

        public Linkage getBillingPlan() {
            return billingPlan;
        }
    }

    public EntityType getType() {
        return type;
    }

    public void setType(final EntityType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final String createdDate) {
        this.createdDate = createdDate;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerExternalKey() {
        return ownerExternalKey;
    }

    public void setOwnerExternalKey(final String ownerExternalKey) {
        this.ownerExternalKey = ownerExternalKey;
    }

    public String getApplicationFamilyId() {
        return appFamilyId;
    }

    public void setApplicationFamilyId(final String appFamilyId) {
        this.appFamilyId = appFamilyId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public boolean getIsAutoRenewEnabled() {
        return isAutoRenewEnabled;
    }

    public void setIsAutoRenewEnabled(final boolean isAutoRenewEnabled) {
        this.isAutoRenewEnabled = isAutoRenewEnabled;
    }

    public Integer getStoredPaymentProfileId() {
        return storedPaymentProfileId;
    }

    public void setStoredPaymentProfileId(final Integer storedPaymentProfileId) {
        this.storedPaymentProfileId = storedPaymentProfileId;
    }

    public Integer getNextBillingOfferId() {
        return nextBillingOfferId;
    }

    public void setNextBillingOfferId(final Integer nextBillingOfferId) {
        this.nextBillingOfferId = nextBillingOfferId;
    }

    public Integer getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(final Integer creditDays) {
        this.creditDays = creditDays;
    }

    public String getNextBillingPriceAmount() {
        return nextBillingPriceAmount;
    }

    public void setNextBillingPriceAmount(final String nextBillingPriceAmount) {
        this.nextBillingPriceAmount = nextBillingPriceAmount;
    }

    public String getNextBillingPriceCurrencyId() {
        return nextBillingPriceCurrencyId;
    }

    public void setNextBillingPriceCurrencyId(final String nextBillingPriceCurrencyId) {
        this.nextBillingPriceCurrencyId = nextBillingPriceCurrencyId;
    }

    public String getNextBillingPriceCurrencyName() {
        return nextBillingPriceCurrencyName;
    }

    public void setNextBillingPriceCurrencyName(final String nextBillingPriceCurrencyName) {
        this.nextBillingPriceCurrencyName = nextBillingPriceCurrencyName;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(final String nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public Integer getPriceId() {
        return priceId;
    }

    public void setPriceId(final Integer priceId) {
        this.priceId = priceId;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public OfferingType getOfferingType() {
        return offeringType;
    }

    public void setOfferingType(final OfferingType offeringType) {
        this.offeringType = offeringType;
    }

    public Links getLinks() {
        return links;
    }

    public BillingOption getBillingOption() {
        return billingOption;
    }

    public void setLinks(final BillingOption billingOption) {
        this.billingOption = billingOption;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public String getAddedToSubscriptionId() {
        return addedToSubscriptionId;
    }

    public void setAddedToSubscriptionId(final String addedToSubscriptionId) {
        this.addedToSubscriptionId = addedToSubscriptionId;
    }

    public String getNextBillingUnitPriceAmount() {
        return nextBillingUnitPriceAmount;
    }

    public void setNextBillingUnitPriceAmount(final String nextBillingUnitPriceAmount) {
        this.nextBillingUnitPriceAmount = nextBillingUnitPriceAmount;
    }

    public int getQtyToReduce() {
        return qtyToReduce;
    }

    public void setQtyToReduce(final int qtyToReduce) {
        this.qtyToReduce = qtyToReduce;
    }

    public String getExportControlStatus() {
        return exportControlStatus;
    }

    public void setExportControlStatus(final String exportControlStatus) {
        this.exportControlStatus = exportControlStatus;
    }
}
