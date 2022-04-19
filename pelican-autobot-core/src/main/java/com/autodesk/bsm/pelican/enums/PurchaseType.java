package com.autodesk.bsm.pelican.enums;

/**
 * This is an enum class for the PurchaseType field in the price quotes api.
 *
 * @author yerragv
 */
public enum PurchaseType {

    OFFERING("offering"),
    SUBSCRIPTIONQUANTITY("subscriptionQuantity"),
    SUBSCRIPTIONEXTENSION("subscriptionExtension"),
    SUBSCRIPTIONRENEWAL("subscriptionRenewal");

    PurchaseType(final String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

}
