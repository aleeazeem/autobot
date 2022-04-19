package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;

/**
 * Entity Pojo for Order details in Purchase Order details page
 *
 * @author t_mohag
 */
public class OrderDetails extends BaseEntity {

    private String orderType;
    private String orderOrigin;
    private String amount;
    private OrderState orderState;
    private String fulfillmentStatus;
    private String created;
    private String lastModified;
    private String invoiceNumber;
    private String creditNoteNumber;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(final String orderType) {
        this.orderType = orderType;
    }

    public String getOrderOrigin() {
        return orderOrigin;
    }

    public void setOrderOrigin(final String orderOrigin) {
        this.orderOrigin = orderOrigin;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(final OrderState orderState) {
        this.orderState = orderState;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(final String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    public void setCreditNoteNumber(final String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }
}
