package com.autodesk.bsm.pelican.enums;

/**
 * Enum for subscription statuses.
 *
 * @author Muhammad
 *
 */
public enum SubscriptionStatus {

    ACTIVE("Active"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    DELINQUENT("Delinquent"),
    PENDING("Pending"),
    PENDING_MIGRATION("Pending_Migration");

    private String displayName;

    SubscriptionStatus(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Convert a string to enum value.
     *
     * @param string representation of a subscription status
     * @return subscription status Campaign status enum
     */
    public static SubscriptionStatus getByValue(final String value) {
        if (value != null) {
            for (final SubscriptionStatus item : SubscriptionStatus.values()) {
                if (item.toString().equalsIgnoreCase(value)) {
                    return item;
                }
            }
        }
        throw new IllegalArgumentException("No constant with value '" + value + "' found.");
    }

    public String getDisplayName() {
        return displayName;
    }

}
