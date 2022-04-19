package com.autodesk.bsm.pelican.enums;

import java.util.EnumSet;

public enum OfferingType {

    BIC_SUBSCRIPTION("BiC Subscription"),

    CURRENCY("Currency"),

    PERPETUAL("Perpetual Product"),

    META_SUBSCRIPTION("Meta Subscription"),

    META_RENEWAL("META_RENEWAL"),

    MAINTENANCE_SUBSCRIPTION("MAINTENANCE_SUBSCRIPTION"),

    MAINTENANCE_RENEWAL("MAINTENANCE_RENEWAL"),

    PHYSICAL_MEDIA("Physical Media"),

    LATE_FEE("LATE_FEE"),

    UNKNOWN("UNKNOWN");

    private String displayName;

    private static final EnumSet<OfferingType> BIC_OFFERING_TYPE =
        EnumSet.of(OfferingType.BIC_SUBSCRIPTION, OfferingType.META_SUBSCRIPTION);

    OfferingType(final String displayName) {
        this.displayName = displayName;
    }

    public static final EnumSet<OfferingType> BASIC_OFFERING_TYPE = EnumSet.complementOf(BIC_OFFERING_TYPE);

    public String getDisplayName() {
        return displayName;
    }
}
