package com.autodesk.bsm.pelican.enums;

public enum Currency {

    USD("US Dollar", "US Dollar (USD)", 4),
    CAD("Canadian Dollar", "Canadian Dollar (CAD)", 100),
    GBP("Pound Sterling", "Pound Sterling (GBP)", 109),
    MXN("Mexican Peso", "Mexican Peso (MXN)", 117),
    EUR("Euro", "Euro (EUR)", 5),
    AUD("Australian Dollar", "Australian Dollar (AUD)", 101);

    private String desc;
    private String longDesc;
    private int code; // This is from TwofishCurrency

    Currency(final String desc, final String longDesc, final int code) {
        this.desc = desc;
        this.longDesc = longDesc;
        this.code = code;
    }

    public String getDescription() {
        return desc;
    }

    public String getLongDescription() {
        return longDesc;
    }

    public int getCode() {
        return code;
    }

    public static Currency getByValue(final String value) {
        if (value != null) {
            for (final Currency item : Currency.values()) {
                if (item.toString().equalsIgnoreCase(value)) {
                    return item;
                }
            }
        }
        throw new IllegalArgumentException("No Currency with value '" + value + "' found.");
    }

}
