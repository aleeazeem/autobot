package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.Status;

public class Data {
    private String name;
    private String externalKey;
    private String type;
    private String id;
    private Status status;
    private String storeType;
    private Currency currency;
    private Double vatPercent;
    private String soldToCSN;
    private boolean sendTaxInvoicesEmails;

    public String getExtKey() {
        return externalKey;
    }

    public void setExtKey(final String extKey) {
        this.externalKey = extKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String value) {
        this.externalKey = value;
    }

    public String getType() {
        return type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(final String value) {
        this.storeType = value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency value) {
        this.currency = value;
    }

    public Double getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(final Double vatPercent) {
        this.vatPercent = vatPercent;
    }

    public String getSoldToCsn() {
        return soldToCSN;
    }

    public void setSoldToCsn(final String soldToCSN) {
        this.soldToCSN = soldToCSN;
    }

    public boolean getSendTaxInvoicesEmails() {
        return sendTaxInvoicesEmails;
    }

    public void setSendTaxInvoicesEmails(final boolean sendTaxInvoicesEmails) {
        this.sendTaxInvoicesEmails = sendTaxInvoicesEmails;
    }
}
