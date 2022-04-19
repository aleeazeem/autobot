package com.autodesk.bsm.pelican.api.pojos.subscriptionplan;

import com.autodesk.bsm.pelican.api.pojos.BillingOption;
import com.autodesk.bsm.pelican.api.pojos.Entitlement;
import com.autodesk.bsm.pelican.api.pojos.Property;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.UsageType;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SubscriptionPlan {

    private String applFamilyId;
    private String id;
    private String name;
    private String extKey;
    private String offeringType;
    private PackagingType packagingType;
    private UsageType usageType;
    private String taxCode;
    private String tier;
    private boolean moduled;
    private String shortDescription;
    private String longDescription;
    private String smallImageUrl;
    private String buttonDisplayName;
    private String supportLevel;
    private String status;
    private String cancellationPolicy;
    private ProductLine productLine;
    private SubscriptionPlan.Properties properties;
    private String modules;
    private SubscriptionPlan.BillingOptions billingOptions;
    private SubscriptionPlan.SubscriptionOffers subscriptionOffers;
    private SubscriptionPlan.RecurringEntitlements recurringTimeEntitlements;
    private SubscriptionPlan.OneTimeEntitlements oneTimeEntitlements;

    public static class Properties {

        @XmlElement(name = "property")
        private List<Property> properties;

        public List<Property> getProperties() {
            if (properties == null) {
                properties = new ArrayList<>();
            }
            return this.properties;
        }
    }

    public static class BillingOptions {

        private List<BillingOption> billingOptions;

        @XmlElement(name = "billingOption")
        public List<BillingOption> getBillingOptions() {
            if (billingOptions == null) {
                billingOptions = new ArrayList<>();
            }
            return billingOptions;
        }
    }

    public static class SubscriptionOffers {
        private List<SubscriptionOffer> subscriptionOffers;

        @XmlElement(name = "subscriptionOffer")
        public List<SubscriptionOffer> getSubscriptionOffers() {
            if (subscriptionOffers == null) {
                subscriptionOffers = new ArrayList<>();
            }
            return subscriptionOffers;
        }
    }

    public static class RecurringEntitlements {

        @XmlElement(name = "entitlement")
        private List<Entitlement> entitlements;

        public List<Entitlement> getEntitlements() {
            if (entitlements == null) {
                entitlements = new ArrayList<>();
            }
            return this.entitlements;
        }

    }

    public static class OneTimeEntitlements {

        @XmlElement(name = "entitlement")
        private List<Entitlement> entitlements;

        public List<Entitlement> getEntitlements() {
            if (entitlements == null) {
                entitlements = new ArrayList<>();
            }
            return this.entitlements;
        }
    }

    public String getApplicationFamilyId() {
        return applFamilyId;
    }

    @XmlAttribute(name = "appFamilyId")
    public void setApplicationFamilyId(final String value) {
        this.applFamilyId = value;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String value) {
        this.name = value;
    }

    public String getExternalKey() {
        return extKey;
    }

    @XmlAttribute
    public void setExternalKey(final String value) {
        this.extKey = value;
    }

    public String getOfferingType() {
        return offeringType;
    }

    @XmlAttribute
    public void setOfferingType(final String offeringType) {
        this.offeringType = offeringType;
    }

    public PackagingType getPackagingType() {
        return packagingType;
    }

    @XmlAttribute
    public void setPackagingType(final PackagingType packagingType) {
        this.packagingType = packagingType;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    @XmlAttribute
    public void setUsageType(final UsageType value) {
        this.usageType = value;
    }

    public String getTaxCode() {
        return taxCode;
    }

    @XmlAttribute
    public void setTaxCode(final String value) {
        this.taxCode = value;
    }

    public String getTier() {
        return tier;
    }

    @XmlAttribute
    public void setTier(final String value) {
        this.tier = value;
    }

    public boolean isModuled() {
        return moduled;
    }

    @XmlAttribute
    public void setModuled(final boolean value) {
        this.moduled = value;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    @XmlAttribute
    public void setShortDescription(final String value) {
        this.shortDescription = value;
    }

    public String getLongDescription() {
        return longDescription;
    }

    @XmlAttribute
    public void setLongDescription(final String value) {
        this.longDescription = value;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    @XmlAttribute
    public void setSmallImageUrl(final String value) {
        this.smallImageUrl = value;
    }

    public String getButtonDisplayName() {
        return buttonDisplayName;
    }

    @XmlAttribute
    public void setButtonDisplayName(final String value) {
        this.buttonDisplayName = value;
    }

    @XmlElement(name = "supportLevel")
    public String getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(final String supportLevel) {
        this.supportLevel = supportLevel;
    }

    @XmlElement
    public String getStatus() {
        return status;
    }

    public void setStatus(final String value) {
        this.status = value;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    @XmlElement
    public void setCancellationPolicy(final String value) {
        this.cancellationPolicy = value;
    }

    public SubscriptionPlan.Properties getProperties() {
        return properties;
    }

    @XmlElement(name = "properties")
    public void setProperties(final SubscriptionPlan.Properties props) {
        this.properties = props;
    }

    public String getModules() {
        return modules;
    }

    // TODO: Need a class?
    @XmlElement(name = "modules")
    public void setModules(final String modules) {
        this.modules = modules;
    }

    public SubscriptionPlan.BillingOptions getBillingOptions() {
        return billingOptions;
    }

    @XmlElement(name = "billingOptions")
    public void setBillingOptions(final SubscriptionPlan.BillingOptions billingOptions) {
        this.billingOptions = billingOptions;
    }

    public SubscriptionPlan.SubscriptionOffers getSubscriptionOffers() {
        return subscriptionOffers;
    }

    @XmlElement(name = "subscriptionOffers")
    public void setSubscriptionOffers(final SubscriptionPlan.SubscriptionOffers subscriptionOffers) {
        this.subscriptionOffers = subscriptionOffers;
    }

    public ProductLine getProductLine() {
        return productLine;
    }

    @XmlElement
    public void setProductLine(final ProductLine productLine) {
        this.productLine = productLine;
    }

    public SubscriptionPlan.RecurringEntitlements getRecurringEntitlements() {
        return recurringTimeEntitlements;
    }

    @XmlElement(name = "recurringEntitlements")
    public void setRecurringEntitlements(final SubscriptionPlan.RecurringEntitlements entitlements) {
        this.recurringTimeEntitlements = entitlements;
    }

    public SubscriptionPlan.OneTimeEntitlements getOneTimeEntitlements() {
        return oneTimeEntitlements;
    }

    @XmlElement(name = "oneTimeEntitlements")
    public void setOneTimeEntitlements(final SubscriptionPlan.OneTimeEntitlements entitlements) {
        this.oneTimeEntitlements = entitlements;
    }
}
