package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.api.pojos.user.User;

/*
 * @Author: Vineel Reddy
 */
public class PurchaseOrder extends BaseEntity {

    private String id;
    private String extKey;
    private String gatewayId;
    private String createdOn;
    private String lastModifiedOn;
    private String fulfillmentStatus;
    private String orderState;
    private User buyerUser;
    private String initialExportControlStatus;
    private String finalExportControlStatus;

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(final String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getExternalKey() {
        return extKey;
    }

    public void setExternalKey(final String value) {
        this.extKey = value;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(final String value) {
        this.fulfillmentStatus = value;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(final String value) {
        this.createdOn = value;
    }

    public String getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(final String value) {
        this.lastModifiedOn = value;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(final String value) {
        this.orderState = value;
    }

    public User getBuyerUser() {
        return buyerUser;
    }

    public void setBuyerUser(final User buyerUser) {
        this.buyerUser = buyerUser;
    }

    public String getInitialExportControlStatus() {
        return initialExportControlStatus;
    }

    public void setInitialExportControlStatus(final String initialExportControlStatus) {
        this.initialExportControlStatus = initialExportControlStatus;
    }

    public String getFinalExportControlStatus() {
        return finalExportControlStatus;
    }

    public void setFinalExportControlStatus(final String finalExportControlStatus) {
        this.finalExportControlStatus = finalExportControlStatus;
    }

}
