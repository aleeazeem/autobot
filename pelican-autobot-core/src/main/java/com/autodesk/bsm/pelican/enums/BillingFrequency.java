package com.autodesk.bsm.pelican.enums;

public enum BillingFrequency {

    DAY("Days", "DAY"),
    WEEK("Weeks", "WEEK"),
    SEMIMONTH("Semi-Months", "SEMIMONTH"),
    MONTH("Months", "MONTH"),
    QUARTER("Quarters", "QUARTER"),
    SEMIYEAR("Semi-Years", "SEMIYEAR"),
    YEAR("Years", "YEAR"),
    LIFETIME("LifeTime", "LIFETIME");

    private String displayName;
    private String name;

    BillingFrequency(final String displayName, final String name) {
        this.displayName = displayName;
        this.name = name;
    }

    /**
     * This method returns Admin Tool value
     *
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * This method returns Api value
     *
     * @return String
     */
    public String getName() {
        return name;
    }
}
