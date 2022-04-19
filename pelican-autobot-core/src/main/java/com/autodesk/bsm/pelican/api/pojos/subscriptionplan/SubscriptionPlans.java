package com.autodesk.bsm.pelican.api.pojos.subscriptionplan;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SubscriptionPlans extends PelicanPojo {

    private List<SubscriptionPlan> subscriptionPlansList;
    private int startIndex;
    private int blockSize;
    private int total;

    public List<SubscriptionPlan> getSubscriptionPlans() {
        return subscriptionPlansList;
    }

    @XmlElement(name = "subscriptionPlan")
    public void setSubscriptionPlans(final List<SubscriptionPlan> subsPlans) {
        this.subscriptionPlansList = subsPlans;
    }

    public int getStartIndex() {
        return startIndex;
    }

    @XmlAttribute(name = "startIndex")
    public void setStartIndex(final int value) {
        this.startIndex = value;
    }

    public int getBlockSize() {
        return blockSize;
    }

    @XmlAttribute(name = "blockSize")
    public void setBlockSize(final int value) {
        this.blockSize = value;
    }

    public int getTotal() {
        return total;
    }

    @XmlAttribute(name = "total")
    public void setTotal(final int value) {
        this.total = value;
    }
}
