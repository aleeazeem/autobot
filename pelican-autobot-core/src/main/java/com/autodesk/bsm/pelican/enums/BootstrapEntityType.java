package com.autodesk.bsm.pelican.enums;

/**
 * This is a enum class for the entities that can be published in the bootstrap job from admin tool
 *
 * @author yerragv
 */
public enum BootstrapEntityType {

    SUBSCRIPTION("Subscription"),
    PURCHASE_ORDER("Purchase Order"),
    ENTITLEMENT("Entitlement"),
    SUBSCRIPTION_PLAN("Subscription Plan"),
    BASIC_OFFERING("Basic Offering");

    private String displayName;

    BootstrapEntityType(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
