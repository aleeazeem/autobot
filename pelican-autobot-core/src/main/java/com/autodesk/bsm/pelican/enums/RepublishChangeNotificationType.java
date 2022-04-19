package com.autodesk.bsm.pelican.enums;

/**
 * This is a enum class for change notifcation type on the republish events page
 *
 * @author yerragv
 *
 */
public enum RepublishChangeNotificationType {

    CREATE_UPDATE("Create/Update"),
    OFFERINGENTITLEMENTENDDATE("Offering Entitlement End Date");

    private String value;

    private RepublishChangeNotificationType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
