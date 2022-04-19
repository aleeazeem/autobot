package com.autodesk.bsm.pelican.enums;

public enum RepublishChangeNotificationRequester {

    PELICAN("PELICAN"),
    ADP("ADP"),
    SALESFORCE("SALESFORCE"),
    FORGE("FORGE"),
    SAP("SAP");

    private String requester;

    RepublishChangeNotificationRequester(final String requester) {
        this.requester = requester;
    }

    public String getRequester() {
        return requester;
    }

}
