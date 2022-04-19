package com.autodesk.bsm.pelican.api.pojos;

import com.autodesk.bsm.pelican.enums.Currency;

public class DRSubscription {
    private String productName;
    private String locale;
    private String sku;
    private Integer subscriptionId;
    private Currency currency;
    private String contractTerm;
    private double quantity;
    private Integer applicationFamilyId;

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(final String sku) {
        this.sku = sku;
    }

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(final Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    public String getContractTerm() {
        return contractTerm;
    }

    public void setContractTerm(final String contractTerm) {
        this.contractTerm = contractTerm;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(final double quantity) {
        this.quantity = quantity;
    }

    public Integer getApplicationFamilyId() {
        return applicationFamilyId;
    }

    public void setApplicationFamilyId(final Integer applicationFamilyId) {
        this.applicationFamilyId = applicationFamilyId;
    }

}
