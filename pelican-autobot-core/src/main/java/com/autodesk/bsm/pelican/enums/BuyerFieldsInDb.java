package com.autodesk.bsm.pelican.enums;

/**
 * The DB table(`tempestdb`.`Purchase_Order`) fields Enum related to Buyer Details
 *
 * @author kishor
 */
public enum BuyerFieldsInDb {

    BILLING_FIRST_NAME("BILLING_FIRST_NAME"),
    BILLING_LAST_NAME("BILLING_LAST_NAME"),
    BILLING_EMAIL("BILLING_EMAIL"),
    BILLING_PHONE("BILLING_PHONE"),
    BILLING_LAST_4_DIGITS("BILLING_LAST_4_DIGITS");

    private String field;

    BuyerFieldsInDb(final String displayName) {
        this.field = displayName;
    }

    /**
     * Convert a string to enum value.
     *
     * @param string representation of a status
     * @return BuyerFields enum
     */
    public static BuyerFieldsInDb getField(final String value) {
        if (value != null) {
            for (final BuyerFieldsInDb item : BuyerFieldsInDb.values()) {
                if (item.toString().equalsIgnoreCase(value)) {
                    return item;
                }
            }
        }
        throw new IllegalArgumentException("No constant with value '" + value + "' found.");
    }

    public String getFieldName() {
        return field;
    }

}
