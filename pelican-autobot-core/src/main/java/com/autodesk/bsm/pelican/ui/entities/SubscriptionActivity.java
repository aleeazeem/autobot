package com.autodesk.bsm.pelican.ui.entities;

/**
 * Entity Pojo for Subscription Activity in Subscription detail page
 *
 * @author jains
 */
public class SubscriptionActivity extends BaseEntity {

    private String date;
    private String activity;
    private String requestor;
    private String charge;
    private String grant;
    private String purchaseOrder;
    private String memo;

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(final String activity) {
        this.activity = activity;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(final String requestor) {
        this.requestor = requestor;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(final String charge) {
        this.charge = charge;
    }

    public String getGrant() {
        return grant;
    }

    public void setGrant(final String grant) {
        this.grant = grant;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(final String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(final String memo) {
        this.memo = memo;
    }
}
