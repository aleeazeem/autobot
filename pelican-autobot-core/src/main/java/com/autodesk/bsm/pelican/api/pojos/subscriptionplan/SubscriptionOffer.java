package com.autodesk.bsm.pelican.api.pojos.subscriptionplan;

import com.autodesk.bsm.pelican.api.pojos.BillingOption;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class SubscriptionOffer {

    private String id;
    private String appFamilyId;
    private String appId;
    private String name;
    private String extKey;
    private String planId;
    private String planExtKey;
    private String status;
    private BillingOption billingOption;

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String value) {
        this.id = value;
    }

    public String getApplFamilyId() {
        return appFamilyId;
    }

    @XmlAttribute(name = "appFamilyId")
    public void setAppFamilyId(final String id) {
        this.appFamilyId = id;
    }

    public String getAppId() {
        return appId;
    }

    @XmlAttribute(name = "appId")
    public void setAppId(final String id) {
        this.appId = id;
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

    public String getPlanId() {
        return planId;
    }

    @XmlAttribute
    public void setPlanId(final String value) {
        this.planId = value;
    }

    public String getPlanExternalKey() {
        return planExtKey;
    }

    @XmlAttribute
    public void setPlanExternalKey(final String value) {
        this.planExtKey = value;
    }

    public String getStatus() {
        return status;
    }

    @XmlElement
    public void setStatus(final String status) {
        this.status = status;
    }

    public BillingOption getBillingOption() {
        return billingOption;
    }

    @XmlElement
    public void setBillingOption(final BillingOption billingOption) {
        this.billingOption = billingOption;
    }
}
