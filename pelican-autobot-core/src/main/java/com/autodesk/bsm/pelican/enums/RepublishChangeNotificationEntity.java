package com.autodesk.bsm.pelican.enums;

public enum RepublishChangeNotificationEntity {

    SUBSCRIPTION("Subscription", "Subscription"),
    PURCHASEORDER("Purchase Order", "purchaseOrder"),
    ENTITLEMENT("Entitlement", "Entitlement"),
    STORE("Store", "Store"),
    SUBSCRIPTION_PLAN("Subscription Plan", "subscriptionOffering"),
    BASIC_OFFERING("Basic Offering", "basicOffering"),
    AUM_SUBSCRIPTION("AUM Subscription", "subscription");

    private String[] entityName;

    RepublishChangeNotificationEntity(final String... entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName[0];
    }

    public String getEntityCSEName() {
        return entityName[1];
    }

}
