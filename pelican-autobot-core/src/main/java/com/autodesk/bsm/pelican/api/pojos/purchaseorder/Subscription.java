package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;

public class Subscription {

    private String id;
    private String subscriptionPeriodStartDate;
    private String subscriptionPeriodEndDate;

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public String getSubscriptionPeriodStartDate() {
        return subscriptionPeriodStartDate;
    }

    @XmlAttribute(name = "subscriptionPeriodStartDate")
    public void setSubscriptionPeriodStartDate(final String subscriptionPeriodStartDate) {
        this.subscriptionPeriodStartDate = subscriptionPeriodStartDate;
    }

    public String getSubscriptionPeriodEndDate() {
        return subscriptionPeriodEndDate;
    }

    @XmlAttribute(name = "subscriptionPeriodEndDate")
    public void setSubscriptionPeriodEndDate(final String subscriptionPeriodEndDate) {
        this.subscriptionPeriodEndDate = subscriptionPeriodEndDate;
    }

}
