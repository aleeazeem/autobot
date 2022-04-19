package com.autodesk.bsm.pelican.api.pojos;

import javax.xml.bind.annotation.XmlAttribute;

public class BillingPeriod {

    private int count;
    private String type;

    public int getCount() {
        return count;
    }

    @XmlAttribute
    public void setCount(final int count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(final String type) {
        this.type = type;
    }
}
