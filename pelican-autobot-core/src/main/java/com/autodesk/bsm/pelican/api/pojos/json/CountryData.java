package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.Country;

public class CountryData {

    private String type;
    private Country countryCode;
    private String priceList;
    private String id;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Country getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final Country countryCode) {
        this.countryCode = countryCode;
    }

    public String getPriceList() {
        return priceList;
    }

    public void setPriceList(final String priceList) {
        this.priceList = priceList;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

}
