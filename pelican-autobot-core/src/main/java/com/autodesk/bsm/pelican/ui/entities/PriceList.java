package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.enums.Currency;

import java.util.ArrayList;
import java.util.List;

public class PriceList extends BaseEntity {

    private Currency money;
    private String name;
    private String extKey;

    public Currency getCurrency() {
        return money;
    }

    public void setCurrency(final Currency value) {
        this.money = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getExternalKey() {
        return extKey;
    }

    public void setExternalKey(final String value) {
        this.extKey = value;
    }

    public List<String> getProperties() {
        final List<String> properties = new ArrayList<>();
        properties.add("currency");
        properties.add("name");
        properties.add("externalKey");
        return properties;
    }
}
