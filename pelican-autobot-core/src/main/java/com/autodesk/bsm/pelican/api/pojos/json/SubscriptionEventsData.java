
package com.autodesk.bsm.pelican.api.pojos.json;

/**
 * This is the class which is used to set and get the values for the Data, Included and errors when get response for get
 * subscription events.
 *
 * @author Muhammad
 *
 */
public class SubscriptionEventsData {
    private String type;
    private String id;
    private String eventType;
    private String createdDate;
    private String requesterName;
    private String grant;
    private String memo;
    private String purchaseOrderId;
    private String cancellationPolicy;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final String createdDate) {
        this.createdDate = createdDate;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(final String requesterName) {
        this.requesterName = requesterName;
    }

    public String getGrant() {
        return grant;
    }

    public void setGrant(final String grant) {
        this.grant = grant;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(final String memo) {
        this.memo = memo;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(final String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getCancllationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(final String cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
