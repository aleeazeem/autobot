package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;

import java.util.ArrayList;
import java.util.List;

public class CountryPriceList extends BaseEntity {

    private Country country;
    private String priceList;
    private Currency currency;

    public Country getAssignedCountry() {
        return country;
    }

    public void setAssignedCountry(final Country country) {
        this.country = country;
    }

    public String getAssignedPriceList() {
        return priceList;
    }

    public void setAssignedPriceList(final String priceListName) {
        this.priceList = priceListName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    public List<String> getProperties() {
        final List<String> properties = new ArrayList<>();
        properties.add("assignedCountry");
        properties.add("assignedPriceList");
        properties.add("currency");
        return properties;
    }

}
