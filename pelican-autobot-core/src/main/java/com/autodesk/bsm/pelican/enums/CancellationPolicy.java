package com.autodesk.bsm.pelican.enums;

/**
 * Cancellation Policy for the subscription policy or subscription
 *
 * @author vineel
 */

public enum CancellationPolicy {
    IMMEDIATE_NO_REFUND("Cancel immediately without a refund", "Cancel Immediately"),
    CANCEL_AT_END_OF_BILLING_PERIOD("Cancel at end of Billing Period", "Cancel at end of Billing Period"),
    GDPR_CANCEL_IMMEDIATELY("GDPR Cancel Immediately", "GDPR_CANCEL");

    private String displayName;
    private String name;

    CancellationPolicy(final String displayName, final String name) {
        this.displayName = displayName;
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }
}
