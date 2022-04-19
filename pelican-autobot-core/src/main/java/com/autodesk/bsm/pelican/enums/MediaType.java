package com.autodesk.bsm.pelican.enums;

/**
 * Media Type for the JSON API
 *
 * @author t_mohag
 */
public enum MediaType {
    ELECTRONIC_DOWNLOAD("Electronic Download", "ELD"),

    USB("USB", "USB"),

    DVD("DVD", "DVD"),

    ELD("ELD", "ELD");

    private final String value;
    private final String displayValue;

    public String getValue() {
        return value;
    }

    public String getDisplayValue() {
        return displayValue;

    }

    MediaType(final String value, final String apiValue) {
        this.value = value;
        this.displayValue = apiValue;
    }
}
