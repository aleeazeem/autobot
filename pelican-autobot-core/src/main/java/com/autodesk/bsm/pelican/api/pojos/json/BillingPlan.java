package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.BillingDate;
import com.autodesk.bsm.pelican.api.pojos.Entitlement;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi.LinkageArray;
import com.autodesk.bsm.pelican.api.pojos.subscription.SubscriptionTag;
import com.autodesk.bsm.pelican.enums.EntityType;

import java.util.List;

public class BillingPlan {

    private EntityType type;
    private String id;
    private String externalKey;
    private String billingCycleCount;
    private String billingPeriodCount;
    private String status;
    private String billingPeriod;
    private Links links;
    private String appFamilyId;
    private String name;
    private List<Entitlement> oneTimeEntitlements;
    private List<SubscriptionTag> subscriptionTags;
    public Descriptors descriptors;
    private BillingDate billingDate;

    public static class Links {
        private LinkageArray prices;

        public LinkageArray getPrices() {
            return prices;
        }

        public void setPrices(final LinkageArray prices) {
            this.prices = prices;
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

    public void setExternalKey(final String extKey) {
        this.externalKey = extKey;
    }

    public String getBillingCycleCount() {
        return billingCycleCount;
    }

    public void setBillingCycleCount(final String count) {
        this.billingCycleCount = count;
    }

    public String getBillingPeriodCount() {
        return billingPeriodCount;
    }

    public void setBillingPeriodCount(final String count) {
        this.billingPeriodCount = count;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setBillingPeriod(final String period) {
        this.billingPeriod = period;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public String getAppFamilyId() {
        return appFamilyId;
    }

    public void setAppFamilyId(final String appFamilyId) {
        this.appFamilyId = appFamilyId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Entitlement> getOneTimeEntitlements() {
        return oneTimeEntitlements;
    }

    public void setOneTimeEntitlements(final List<Entitlement> oneTimeEntitlements) {
        this.oneTimeEntitlements = oneTimeEntitlements;
    }

    public List<SubscriptionTag> getSubscriptionTags() {
        return subscriptionTags;
    }

    public void setSubscriptionTags(final List<SubscriptionTag> subscriptionTags) {
        this.subscriptionTags = subscriptionTags;
    }

    public Descriptors getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(final Descriptors descriptors) {
        this.descriptors = descriptors;
    }

    public BillingDate getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(final BillingDate billingDate) {
        this.billingDate = billingDate;
    }
}
