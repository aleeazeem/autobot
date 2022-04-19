package com.autodesk.bsm.pelican.api.pojos.json;

/**
 * This class represents the JSON object for BillingPeriod.
 *
 * @author jains
 */
public class BillingPeriod {
    private int count;
    private String type;

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
