package com.autodesk.bsm.pelican.api.pojos.subscription;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author vineel
 **/
public class Price {

    private String name;
    private String amount;
    private String id;
    private String currency;
    private String storeId;
    private String storeExternalKey;
    private String pricelistExternalKey;

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(final String value) {
        this.name = value;
    }

    public String getAmount() {
        return amount;
    }

    @XmlAttribute(name = "amount")
    public void setAmount(final String value) {
        this.amount = value;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String value) {
        this.id = value;
    }

    public String getCurrency() {
        return currency;
    }

    @XmlAttribute(name = "currency")
    public void setCurrency(final String value) {
        this.currency = value;
    }

    public String getStoreId() {
        return storeId;
    }

    @XmlAttribute(name = "storeId")
    public void setStoreId(final String value) {
        this.storeId = value;
    }

    public String getStoreExternalKey() {
        return storeExternalKey;
    }

    @XmlAttribute(name = "storeExternalKey")
    public void setStoreExternalKey(final String storeExternalKey) {
        this.storeExternalKey = storeExternalKey;
    }

    public String getPricelistExternalKey() {
        return pricelistExternalKey;
    }

    @XmlAttribute(name = "priceListExternalKey")
    public void setPricelistExternalKey(final String pricelistExternalKey) {
        this.pricelistExternalKey = pricelistExternalKey;
    }
}
