package com.autodesk.bsm.pelican.api.pojos.json;

public class BasicOfferingPriceData {

    private String type;
    private String startDate;
    private String endDate;
    private String priceList;
    private int amount;
    private String id;

    public String getType() {
        return type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public String getPriceList() {
        return priceList;
    }

    public void setPriceList(final String priceList) {
        this.priceList = priceList;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setStartDate(final String date) {
        this.startDate = date;
    }

    public void setEndDate(final String date) {
        this.endDate = date;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
