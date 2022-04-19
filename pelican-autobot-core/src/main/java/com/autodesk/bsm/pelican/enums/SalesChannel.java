package com.autodesk.bsm.pelican.enums;

public enum SalesChannel {
    BIC_DIRECT("BIC Direct"),
    BIC_INDIRECT("BIC Indirect");

    private String displayName;

    SalesChannel(final String displayName) {
        this.displayName = displayName;
    }

    public int getOrdinal() {
        return ordinal();
    }

    public String getName() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SalesChannel getEnumByName(final String code) {
        for (final SalesChannel e : SalesChannel.values()) {
            if (e.getName().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
