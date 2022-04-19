package com.autodesk.bsm.pelican.enums;

/**
 * Promotion Type for the JSON API
 *
 * @author t_mohag
 */
public enum PromotionType {

    DISCOUNT_AMOUNT("Discount Amount", "Discount"),
    DISCOUNT_PERCENTAGE("Discount Percentage", "Discount"),
    SUPPLEMENT_TIME("Supplement Time", "Supplement");

    private String displayName;
    private String value;

    PromotionType(final String displayName, final String value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getValue() {
        return value;
    }
}
