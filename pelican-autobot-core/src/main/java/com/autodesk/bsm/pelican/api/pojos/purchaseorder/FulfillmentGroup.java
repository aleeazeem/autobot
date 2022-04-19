package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;

public class FulfillmentGroup {

    private String id;
    private FulFillmentStrategy strategy;
    private FulFillmentStatus status;

    public enum FulFillmentStrategy {
        BIC,
        CLOUD_CREDITS,
        LEGACY,
        SUBSCRIPTION_QUANTITY,
        SUBSCRIPTION_EXTENSION
    }

    public enum FulFillmentStatus {
        FULFILLED,
        PENDING,
        CHARGED_BUT_UNFULFILLED,
        FAILED
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String id) {
        this.id = id;
    }

    public FulFillmentStrategy getStrategy() {
        return strategy;
    }

    @XmlAttribute(name = "strategy")
    public void setStrategy(final FulFillmentStrategy value) {
        this.strategy = value;
    }

    public FulFillmentStatus getStatus() {
        return status;
    }

    @XmlAttribute(name = "status")
    public void setStatus(final FulFillmentStatus value) {
        this.status = value;
    }
}
