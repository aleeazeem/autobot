package com.autodesk.bsm.pelican.api.pojos;

import com.autodesk.bsm.pelican.api.pojos.subscription.Price;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BillingOption {

    private String id;
    private String name;
    private String extKey;
    private String planId;
    private String planExtKey;
    private Integer billingCycleCount;
    private BillingPeriod billingPeriod;
    private BillingOption.Prices prices;
    private BillingDate billingDate;

    public static class Prices {
        private List<Price> prices;

        @XmlElement(name = "price")
        public List<Price> getPrices() {
            if (prices == null) {
                prices = new ArrayList<>();
            }
            return prices;
        }
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String value) {
        this.id = value;
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

    public Integer getBillingCycleCount() {
        return billingCycleCount;
    }

    @XmlElement
    public void setBillingCycleCount(final Integer billingCycleCount) {
        this.billingCycleCount = billingCycleCount;
    }

    public BillingPeriod getBillingPeriod() {
        return billingPeriod;
    }

    @XmlElement(name = "billingPeriod")
    public void setBillingPeriod(final BillingPeriod billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public BillingOption.Prices getPrices() {
        return prices;
    }

    @XmlElement
    public void setPrices(final BillingOption.Prices prices) {
        this.prices = prices;
    }

    public BillingDate getBillingDate() {
        return billingDate;
    }

    @XmlElement
    public void setBillingDate(final BillingDate value) {
        this.billingDate = value;
    }
}
