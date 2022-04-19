package com.autodesk.bsm.pelican.ui.entities;

/**
 * Entity Pojo for Transaction details in Purchase Order details page
 *
 * @author shipra
 */
public class TransactionDetails extends BaseEntity {

    private String transactionId;
    private String transactionDate;
    private String type;
    private String requestedBy;
    private String paymentGateway;
    private String paymentType;
    private String amount;
    private String state;
    private String ecStatus;

    public String gettransactionId() {
        return transactionId;
    }

    public void settransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    public String gettransactionDate() {
        return transactionDate;
    }

    public void settransactionDate(final String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(final String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(final String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(final String paymentType) {
        this.paymentType = paymentType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getState() {
        return state;
    }

    public void setSate(final String state) {
        this.state = state;
    }

    public void setEcStauts(final String ecStatus) {
        this.ecStatus = ecStatus;
    }

    public String getEcStatus() {
        return ecStatus;
    }
}
