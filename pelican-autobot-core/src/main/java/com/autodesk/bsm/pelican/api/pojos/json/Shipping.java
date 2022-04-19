package com.autodesk.bsm.pelican.api.pojos.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Shipping {

    private ShippingMethod shippingMethod;
    private List<JAdditionalFee> additionalFees;
    private ShipTo shipTo;
    private Totals totals;

    public enum ShippingMethod {
        @SerializedName("UPS-GROUND")
        UPS_GROUND,

        @SerializedName("UPS-GROUND-EMEA")
        UPS_GROUND_EMEA
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(final ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public List<JAdditionalFee> getAdditionalFees() {
        return additionalFees;
    }

    public void setAdditionalFees(final List<JAdditionalFee> additionalFees) {
        this.additionalFees = additionalFees;
    }

    public ShipTo getShipTo() {
        return shipTo;
    }

    public void setShipTo(final ShipTo shipTo) {
        this.shipTo = shipTo;
    }

    public Totals getTotals() {
        return totals;
    }

    public void setTotals(final Totals totals) {
        this.totals = totals;
    }
}
