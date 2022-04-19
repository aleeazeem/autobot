package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Subscriptions {

    private List<Subscription> subscriptionList;

    public List<Subscription> getSubscription() {
        return subscriptionList;
    }

    @XmlElement(name = "subscription")
    public void setSubscription(final List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

}
