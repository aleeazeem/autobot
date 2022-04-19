package com.autodesk.bsm.pelican.enums;

public enum Status {

    NEW("New"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    CANCELED("Canceled"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    EDIT_SUBSCRIPTION_STATUS_CANCELLED("CANCELLED"),
    DELINQUENT("Delinquent"),
    PENDING("Pending"),
    PENDING_MIGRATION("Pending_Migration"),
    COMPLETED("completed"),
    FAILED("failed"),
    PASSED("passed"),
    SKIPPED("skipped"),
    APPROVED("approved"),
    IN_PROGRESS("in_progress"),
    SENT_TO_MAILBROKER("sent_to_mail_broker"),
    COMPLETE("COMPLETE"),
    COMPLETE_WITH_FAILURES("complete_with_failures");

    private String displayName;

    Status(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Convert a string to enum value.
     *
     * @param string representation of a status
     * @return Status Campaign status enum
     */
    public static Status getByValue(final String value) {
        if (value != null) {
            for (final Status item : Status.values()) {
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
