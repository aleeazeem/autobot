package com.autodesk.bsm.pelican.enums;

public enum RepublishChangeNotificationFilterType {

    ID_LIST("Id List"),
    ID_RANGE("Id Range"),
    DATE_RANGE("Date Range");

    private String filterType;

    RepublishChangeNotificationFilterType(final String filterType) {
        this.filterType = filterType;
    }

    public String getFilterType() {
        return filterType;
    }

}
