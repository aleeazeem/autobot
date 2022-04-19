package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.json.JsonApi.Linkage;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi.LinkageArray;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan.Properties;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;

import java.util.List;

/**
 * Offering entity for Json API
 *
 * @author yin
 */
public class Offering {

    private EntityType type;
    private OfferingType offeringType;
    private String id;
    private String name;
    private String externalKey;
    private String productLine;
    private SupportLevel supportLevel;
    private String status;
    private UsageType usageType;
    private PackagingType packagingType;
    private String tier;
    private CancellationPolicy cancellationPolicy;
    private String taxCode;
    private boolean moduled;
    private Properties properties;
    private Links links;
    private MediaType mediaType;
    private String currency;
    private int amount;
    private String offeringDetail;
    private List<JSubscriptionEntitlementData> oneTimeEntitlements;
    private Errors errors;
    private Offering offering;
    private String productLineName;
    public Descriptors descriptors;
    private String shortDescription;
    private String longDescription;
    private String smallImageURL;
    private String mediumImageURL;
    private String largeImageURL;
    private String buttonDisplayName;
    private SubscriptionOffering.EntitlementPeriod entitlementPeriod;

    public static class Links {
        private Linkage offeringDetail;
        private LinkageArray billingPlans; // Bic/Meta Subscription
        private LinkageArray prices; // Basic Offering

        public Linkage getOfferingDetail() {
            return offeringDetail;
        }

        public void setOfferingDetail(final Linkage value) {
            this.offeringDetail = value;
        }

        public LinkageArray getBillingPlans() {
            return billingPlans;
        }

        public void setBillingPlans(final LinkageArray billingPlans) {
            this.billingPlans = billingPlans;
        }

        /**
         * Get prices for Basic Offerings
         *
         * @return LinkageArray
         */

        public LinkageArray getPrices() {
            return prices;
        }

        public void setPrices(final LinkageArray prices) {
            this.prices = prices;
        }
    }

    public EntityType getEntityType() {
        return type;
    }

    public void setEntityType(final EntityType value) {
        this.type = value;
    }

    public OfferingType getOfferingType() {
        return offeringType;
    }

    public void setOfferingType(final OfferingType value) {
        this.offeringType = value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String value) {
        this.externalKey = value;
    }

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(final String value) {
        this.productLine = value;
    }

    public SupportLevel getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(final SupportLevel supportLevel) {
        this.supportLevel = supportLevel;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(final UsageType value) {
        this.usageType = value;
    }

    public PackagingType getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(final PackagingType packagingType) {
        this.packagingType = packagingType;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(final String value) {
        this.tier = value;
    }

    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(final CancellationPolicy value) {
        this.cancellationPolicy = value;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(final String value) {
        this.taxCode = value;
    }

    public boolean isModuled() {
        return moduled;
    }

    public void setModuled(final boolean state) {
        this.moduled = state;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(final MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties value) {
        this.properties = value;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public List<JSubscriptionEntitlementData> getOneTimeEntitlements() {
        return oneTimeEntitlements;
    }

    public void setOneTimeEntitlements(final List<JSubscriptionEntitlementData> oneTimeEntitlements) {
        this.oneTimeEntitlements = oneTimeEntitlements;
    }

    public String getOfferingDetail() {
        return offeringDetail;
    }

    public void setOfferingDetail(final String offeringDetail) {
        this.offeringDetail = offeringDetail;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(final Errors errors) {
        this.errors = errors;
    }

    public Offering getOffering() {
        return offering;
    }

    public void setOffering(final Offering offering) {
        this.offering = offering;
    }

    public String getProductLineName() {
        return productLineName;
    }

    public void setProductLineName(final String productLineName) {
        this.productLineName = productLineName;
    }

    public Descriptors getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(final Descriptors descriptors) {
        this.descriptors = descriptors;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }

    public String getSmallImageURL() {
        return smallImageURL;
    }

    public void setSmallImageURL(final String smallImageURL) {
        this.smallImageURL = smallImageURL;
    }

    public String getMediumImageURL() {
        return mediumImageURL;
    }

    public void setMediumImageURL(final String mediumImageURL) {
        this.mediumImageURL = mediumImageURL;
    }

    public String getLargeImageURL() {
        return largeImageURL;
    }

    public void setLargeImageURL(final String largeImageURL) {
        this.largeImageURL = largeImageURL;
    }

    public String getButtonDisplayName() {
        return buttonDisplayName;
    }

    public void setButtonDisplayName(final String buttonDisplayName) {
        this.buttonDisplayName = buttonDisplayName;
    }

    public SubscriptionOffering.EntitlementPeriod getEntitlementPeriod() {
        return entitlementPeriod;
    }

    public void setEntitlementPeriod(final SubscriptionOffering.EntitlementPeriod entitlementPeriod) {
        this.entitlementPeriod = entitlementPeriod;
    }
}
