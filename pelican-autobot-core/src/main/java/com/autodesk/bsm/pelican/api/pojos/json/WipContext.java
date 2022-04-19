package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/**
 * This class represents the JSON object for WipContext. Context column in work_in_progress_context is WipContext.
 *
 * @author jains
 */
public class WipContext extends PelicanPojo {
    private String ownerId;
    private String oxygenId;
    private String storedPaymentProfileId;
    private String nextBillingDate;
    private BillingPeriod billingPeriod;
    private String priceListId;
    private String priceListExternalKey;
    private String[] subscriptions;
    private String appFamilyId;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOxygenId() {
        return oxygenId;
    }

    public void setOxygenId(final String oxygenId) {
        this.oxygenId = oxygenId;
    }

    public String getStoredPaymentProfileId() {
        return storedPaymentProfileId;
    }

    public void setStoredPaymentProfileId(final String storedPaymentProfileId) {
        this.storedPaymentProfileId = storedPaymentProfileId;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(final String nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public BillingPeriod getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(final BillingPeriod billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public String getPriceListId() {
        return priceListId;
    }

    public void setPriceListId(final String priceListId) {
        this.priceListId = priceListId;
    }

    public String getPriceListExternalKey() {
        return priceListExternalKey;
    }

    public void setPriceListExternalKey(final String priceListExternalKey) {
        this.priceListExternalKey = priceListExternalKey;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(final String[] subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getAppFamilyId() {
        return appFamilyId;
    }

    public void setAppFamilyId(final String appFamilyId) {
        this.appFamilyId = appFamilyId;
    }
}
