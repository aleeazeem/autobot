package com.autodesk.bsm.pelican.cse;

public class ChangeNotificationRelationships {
    private ChangeNotificationRelationship user;
    private ChangeNotificationRelationship subscription;
    private ChangeNotificationRelationship purchaseOrder;
    private ChangeNotificationRelationship store;
    private ChangeNotificationApplicationFamily appFamily;
    private ChangeNotificationRelationship subscriptionOffering;
    private ChangeNotificationRelationship basicOffering;
    private ChangeNotificationRelationship entitlement;

    /**
     * Method to parse changeNotification relationship for User
     */
    public void setChangeNotificationUser(final ChangeNotificationRelationship user) {
        this.user = user;
    }

    /**
     * Method to return Change Notification for User
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationUser() {
        return user;
    }

    /**
     * Method to set Change Notification for Application Family
     */
    public void setChangeNotificationApplicationFamily(final ChangeNotificationApplicationFamily appFamily) {
        this.appFamily = appFamily;
    }

    /**
     * Method to return Change Notification for Application Family
     *
     * @return ChangeNotificationApplicationFamily
     */
    public ChangeNotificationApplicationFamily getChangeNotificationApplicationFamily() {
        return appFamily;
    }

    /**
     * Method to return Change Notification for Subscription
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationSubscription() {
        return subscription;
    }

    /**
     * Method to set ChangeNotificationRelationship for subscription
     */
    public void setChangeNotificationSubscription(final ChangeNotificationRelationship subscription) {
        this.subscription = subscription;
    }

    /**
     * Method to return Change Notification for Subscription Offering
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationSubscriptionOffering() {
        return subscriptionOffering;
    }

    /**
     * Method to set ChangeNotificationRelationship for subscriptionOffering
     */
    public void setChangeNotificationSubscriptionOffering(final ChangeNotificationRelationship subscriptionOffering) {
        this.subscriptionOffering = subscriptionOffering;
    }

    /**
     * Method to return Change Notification for Purchase Order
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationPurchaseOrder() {
        return purchaseOrder;
    }

    /**
     * Method to set ChangeNotificationRelationship for Purchase Order
     */
    public void setChangeNotificationPurchaseOrder(final ChangeNotificationRelationship purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    /**
     * Method to return ChangeNotificationRelationship for Store
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationStore() {
        return store;
    }

    /**
     * Method to set ChangeNotificationRelationship for store
     */
    public void setChangeNotificationStore(final ChangeNotificationRelationship store) {
        this.store = store;
    }

    /**
     * Method to return Change Notification for Basic Offering
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationBasicOffering() {
        return basicOffering;
    }

    /**
     * Method to set ChangeNotificationRelationship for basicOffering
     */
    public void setChangeNotificationBasicOffering(final ChangeNotificationRelationship basicOffering) {
        this.basicOffering = basicOffering;
    }

    /**
     * Method to return Change Notification for Entitlement
     *
     * @return ChangeNotificationRelationship
     */
    public ChangeNotificationRelationship getChangeNotificationEntitlement() {
        return entitlement;
    }

    /**
     * Method to set Change Notification for Entitlement
     */
    public void setChangeNotificationEntitlement(final ChangeNotificationRelationship entitlement) {
        this.entitlement = entitlement;
    }
}
