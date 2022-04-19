package com.autodesk.bsm.pelican.enums;

public enum Country {

    US("United States", "United States (US)", "US"),
    CA("Canada", "Canada (CA)", "CA"),
    GB("United Kingdom", "United Kingdom (GB)", "GB"),
    MX("Mexico", "Mexico (MX)", "MX"),
    DE("Germany", "Germany (DE)", "DE"),
    FR("France", "France (FR)", "FR"),
    AU("Australia", "Australia (AU)", "AU"),
    TC("Turks and Caicos Islands", "Turks and Caicos Islands (TC)", "TC");

    private String desc;
    private String longDesc;
    private String countryCode;

    Country(final String desc, final String longDesc, final String countryCode) {
        this.desc = desc;
        this.longDesc = longDesc;
        this.countryCode = countryCode;
    }

    public String getDescription() {
        return desc;
    }

    public String getLongDescription() {
        return longDesc;
    }

    public static Country getByDescription(final String description) {
        if (description != null) {
            for (final Country item : Country.values()) {
                if (item.desc.equalsIgnoreCase(description)) {
                    return item;
                }
            }
        }
        throw new IllegalArgumentException("No Country with value '" + description + "' found.");
    }

    public static Country getByCode(final String countryCode) {
        if (countryCode != null) {
            for (final Country item : Country.values()) {
                if (item.countryCode.equalsIgnoreCase(countryCode)) {
                    return item;
                }
            }
        }
        throw new IllegalArgumentException("No Country with code '" + countryCode + "' found.");
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

}
