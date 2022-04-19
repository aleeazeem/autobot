package com.autodesk.bsm.pelican.api.pojos.json;

public class Price {

    private String type;
    private String id;
    private String pricelistId;
    private String amount;
    private String discount;
    private String amountAfterDiscount;
    private String currency;
    private String startDate;
    private String endDate;
    private String storeId;
    private String storeExternalKey;
    private String priceListExternalKey;
    private String status;

    public Price() {}

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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(final String discount) {
        this.discount = discount;
    }

    public String getAmountAfterDiscount() {
        return amountAfterDiscount;
    }

    public void setAmountAfterDiscount(final String amountAfterDiscount) {
        this.amountAfterDiscount = amountAfterDiscount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }

    public String getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(final String pricelistId) {
        this.pricelistId = pricelistId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(final String storeId) {
        this.storeId = storeId;
    }

    public String getStoreExternalKey() {
        return storeExternalKey;
    }

    public void setStoreExternalKey(final String storeExternalKey) {
        this.storeExternalKey = storeExternalKey;
    }

    public String getPriceListExternalKey() {
        return priceListExternalKey;
    }

    public void setPriceListExternalKey(final String priceListExternalKey) {
        this.priceListExternalKey = priceListExternalKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Long getCurrencyId() {
        // TODO Auto-generated method stub
        return null;
    }

}
