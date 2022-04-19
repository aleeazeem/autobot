package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.Entitlement;
import com.autodesk.bsm.pelican.api.pojos.Property;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.UsageType;

import java.util.List;

/**
 * Subscription offering entity for Json API
 *
 * @author jains
 */
public class SubscriptionOffering {
    private EntityType type;
    private String id;
    private String appFamilyId;
    private String name;
    private String externalKey;
    private String supportLevel;
    private String status;
    private CancellationPolicy cancellationPolicy;
    private UsageType usageType;
    private PackagingType packagingType;
    private EntitlementPeriod entitlementPeriod;
    private List<Entitlement> oneTimeEntitlements;
    private String tier;
    private boolean moduled;
    private JProductLine productLine;
    private OfferingDetail offeringDetail;
    private boolean isModule;
    private List<Entitlement> recurringEntitlements;
    private List<Property> properties;
    private OfferingType offeringType;

    public static class EntitlementPeriod {
        private int count;
        private String type;

        public void setCount(final int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public EntityType getEntityType() {
        return type;
    }

    public void setEntityType(final EntityType value) {
        this.type = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
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

    public String getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(final String value) {
        this.supportLevel = value;
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

    public boolean isModuled() {
        return moduled;
    }

    public void setModuled(final boolean state) {
        this.moduled = state;
    }

    /*
     * public JProperties getProperties() { return properties; }
     *
     * public void setProperties(JProperties properties) { this.properties = properties; }
     */
    public String getAppFamilyId() {
        return appFamilyId;
    }

    public void setAppFamilyId(final String appFamilyId) {
        this.appFamilyId = appFamilyId;
    }

    public EntitlementPeriod getEntitlementPeriod() {
        return entitlementPeriod;
    }

    public void setEntitlementPeriod(final EntitlementPeriod entitlementPeriod) {
        this.entitlementPeriod = entitlementPeriod;
    }

    public JProductLine getJProductLine() {
        return productLine;
    }

    public void setJProductLine(final JProductLine productLine) {
        this.productLine = productLine;
    }

    public List<Entitlement> getOneTimeEntitlements() {
        return oneTimeEntitlements;
    }

    public void setOneTimeEntitlements(final List<Entitlement> oneTimeEntitlements) {
        this.oneTimeEntitlements = oneTimeEntitlements;
    }

    public OfferingDetail getOfferingDetail() {
        return offeringDetail;
    }

    public void setOfferingDetail(final OfferingDetail offeringDetail) {
        this.offeringDetail = offeringDetail;
    }

    public boolean isModule() {
        return isModule;
    }

    public void setModule(final boolean isModule) {
        this.isModule = isModule;
    }

    public List<Entitlement> getRecurringEntitlements() {
        return recurringEntitlements;
    }

    public void setRecurringEntitlements(final List<Entitlement> recurringEntitlements) {
        this.recurringEntitlements = recurringEntitlements;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(final List<Property> properties) {
        this.properties = properties;
    }

    public OfferingType getOfferingType() {
        return offeringType;
    }

    public void setOfferingType(OfferingType offeringType) {
        this.offeringType = offeringType;
    }
}
