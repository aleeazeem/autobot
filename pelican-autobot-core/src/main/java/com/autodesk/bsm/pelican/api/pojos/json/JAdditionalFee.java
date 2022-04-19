package com.autodesk.bsm.pelican.api.pojos.json;

import com.google.gson.annotations.SerializedName;

public class JAdditionalFee {

    private Category category;
    private String amount;
    private String taxIncludedInBasePrice;
    private TaxPayer taxPayer;

    public JAdditionalFee(final Category category, final String amount, final String taxIncludedInBasePrice,
        final TaxPayer taxPayer) {
        this.category = category;
        this.amount = amount;
        this.taxIncludedInBasePrice = taxIncludedInBasePrice;
        this.taxPayer = taxPayer;
    }

    public enum Category {
        @SerializedName("tax")
        TAX
    }

    public enum TaxPayer {
        @SerializedName("buyer")
        BUYER
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getTaxIncludedInBasePrice() {
        return taxIncludedInBasePrice;
    }

    public void setTaxIncludedInBasePrice(final String taxIncludedInBasePrice) {
        this.taxIncludedInBasePrice = taxIncludedInBasePrice;
    }

    public TaxPayer getTaxPayer() {
        return taxPayer;
    }

    public void setTaxPayer(final TaxPayer taxPayer) {
        this.taxPayer = taxPayer;
    }
}
