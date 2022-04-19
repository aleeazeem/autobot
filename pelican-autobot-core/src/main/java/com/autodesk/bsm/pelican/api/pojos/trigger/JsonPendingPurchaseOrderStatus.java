package com.autodesk.bsm.pelican.api.pojos.trigger;

/**
 * This class is a pojo class for the JSON response for the Pending Purchase Order jobs response
 *
 * @author t_joshv
 */
public class JsonPendingPurchaseOrderStatus {

    private String purchaseOrderId;
    private String sfdcCaseNumber;
    private String created;

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(final String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getSfdcCaseNumber() {
        return sfdcCaseNumber;
    }

    public void setSfdcCaseNumber(final String sfdcCaseNumber) {
        this.sfdcCaseNumber = sfdcCaseNumber;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

}
