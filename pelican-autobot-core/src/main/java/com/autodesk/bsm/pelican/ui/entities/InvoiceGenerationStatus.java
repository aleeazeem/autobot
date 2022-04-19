package com.autodesk.bsm.pelican.ui.entities;

/**
 * Entity Pojo for Invoice Generation Status in Purchase Order details page
 *
 * @author t_mohag
 */
public class InvoiceGenerationStatus extends BaseEntity {

    private String invoiceNumber;
    private String status;
    private String details;
    private String attempts;
    private String lastRun;

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }

    public String getAttempts() {
        return attempts;
    }

    public void setAttempts(final String attempts) {
        this.attempts = attempts;
    }

    public String getLastRun() {
        return lastRun;
    }

    public void setLastRun(final String lastRun) {
        this.lastRun = lastRun;
    }
}
