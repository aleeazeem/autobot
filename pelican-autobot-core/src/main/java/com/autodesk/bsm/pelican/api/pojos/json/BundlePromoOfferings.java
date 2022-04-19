package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/***
 * @author t_joshv This class represents the JSON object of basic offering / subscription offer.
 */
public class BundlePromoOfferings extends PelicanPojo {
    private Offerings offerings;
    private int quantity;
    private boolean applyDiscount;

    public Offerings getBundleOfferings() {
        return offerings;
    }

    public void setBundleOfferings(final Offerings bundleOfferings) {
        this.offerings = bundleOfferings;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public boolean getApplyDiscount() {
        return applyDiscount;
    }

    public void setApplyDiscount(final boolean applyDiscount) {
        this.applyDiscount = applyDiscount;
    }

}
