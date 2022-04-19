package com.autodesk.bsm.pelican.enums;

/**
 * Support level for the Json API
 *
 * @author t_mohag
 */
public enum SupportLevel {
    BASIC(0, "Basic Support"),
    ADVANCED(1, "Advanced Support");

    private int value;
    private String displayName;

    SupportLevel(final int value, final String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getNumber() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
