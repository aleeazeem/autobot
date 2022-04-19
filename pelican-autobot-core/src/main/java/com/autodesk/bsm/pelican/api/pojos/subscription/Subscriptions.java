package com.autodesk.bsm.pelican.api.pojos.subscription;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "subscriptions")
public class Subscriptions extends PelicanPojo {

    private List<Subscription> subscriptions;
    private int startIndex;
    private int blockSize;
    private Integer total;

    public List<Subscription> getSubscriptions() {
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
        }
        return subscriptions;
    }

    @XmlElement(name = "subscription")
    public void setSubscriptions(final List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public int getStartIndex() {
        return startIndex;
    }

    @XmlAttribute
    public void setStartIndex(final int startIndex) {
        this.startIndex = startIndex;
    }

    public int getBlockSize() {
        return blockSize;
    }

    @XmlAttribute
    public void setBlockSize(final int blockSize) {
        this.blockSize = blockSize;
    }

    public Integer getTotal() {
        return total;
    }

    @XmlAttribute
    public void setTotal(final Integer total) {
        this.total = total;
    }
}
