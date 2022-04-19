package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Status;

public class SubscriptionOfferData {
    private String name;
    private String externalKey;
    private String type;
    private String id;
    private Status status;
    private int billingFrequencyCount;
    private BillingFrequency billingFrequency;

    public SubscriptionOfferData() {}

    public SubscriptionOfferData(final String externalKey, final int billingFrequencyCount,
        final BillingFrequency billingFrequency) {
        this.externalKey = externalKey;
        this.billingFrequencyCount = billingFrequencyCount;
        this.billingFrequency = billingFrequency;
    }

    public String getExtKey() {
        return externalKey;
    }

    public void setExtKey(final String extKey) {
        this.externalKey = extKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public int getBillingFrequencyCount() {
        return billingFrequencyCount;
    }

    public void setBillingFrequencyCount(final int billingFrequencyCount) {
        this.billingFrequencyCount = billingFrequencyCount;
    }

    public BillingFrequency getBillingFrequency() {
        return billingFrequency;
    }

    public void setBillingFrequency(final BillingFrequency billingFrequency) {
        this.billingFrequency = billingFrequency;
    }

}
