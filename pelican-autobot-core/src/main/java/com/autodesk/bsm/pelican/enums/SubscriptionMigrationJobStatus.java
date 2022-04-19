package com.autodesk.bsm.pelican.enums;

/**
 * This is an enum class for the SubscriptionMigrationJobStatus which can be found on the Subscriptions ----> Find Jobs.
 *
 * @author yerragv
 */
public enum SubscriptionMigrationJobStatus {

    UPLOADING_FILE("UPLOADING FILE", "0"),
    RUNNING_VALIDATIONS("RUNNING VALIDATIONS", "1"),
    VALIDATION_FAILED("VALIDATION FAILED", "2"),
    VALIDATION_SUCCEEDED("VALIDATION SUCCEEDED", "3"),
    RUNNING_FILE("RUNNING FILE", "4"),
    COMPLETED("COMPLETED", "5"),
    PARTIALLY_COMPLETED("PARTIALLY COMPLETED", "6"),
    FAILED("FAILED", "7"),
    CANCELLED("CANCELLED", "8");

    private String displayName;
    private String dbValue;

    SubscriptionMigrationJobStatus(final String displayName, final String dbValue) {
        this.displayName = displayName;
        this.dbValue = dbValue;
    }

    /**
     * This method returns Admin Tool value.
     *
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * This method returns db value
     *
     * @return String
     */
    public String getDbValue() {
        return dbValue;
    }
}
