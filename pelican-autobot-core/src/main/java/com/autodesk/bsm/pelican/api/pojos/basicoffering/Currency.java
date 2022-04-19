package com.autodesk.bsm.pelican.api.pojos.basicoffering;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "currency")
public class Currency extends PelicanPojo {
    private String id;
    private String name;
    private String radix;
    private String description;
    private String taxCode;
    private String sku;
    private String skuExtension;
    private boolean shouldCreateUserAccount;
    private String userAccountDefaultMinBalance;
    private String userAccountDefaultMaxBalance;
    private boolean virtual;

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    @XmlAttribute(name = "sku")
    public void setSku(final String sku) {
        this.sku = sku;
    }

    public String getSkuExtension() {
        return skuExtension;
    }

    @XmlAttribute(name = "skuExtension")
    public void setSkuExtension(final String skuExtension) {
        this.skuExtension = skuExtension;
    }

    public boolean isShouldCreateUserAccount() {
        return shouldCreateUserAccount;
    }

    @XmlAttribute(name = "shouldCreateUserAccount")
    public void setShouldCreateUserAccount(final boolean shouldCreateUserAccount) {
        this.shouldCreateUserAccount = shouldCreateUserAccount;
    }

    public String getUserAccountDefaultMinBalance() {
        return userAccountDefaultMinBalance;
    }

    @XmlAttribute(name = "userAccountDefaultMinBalance")
    public void setUserAccountDefaultMinBalance(final String userAccountDefaultMinBalance) {
        this.userAccountDefaultMinBalance = userAccountDefaultMinBalance;
    }

    public String getUserAccountDefaultMaxBalance() {
        return userAccountDefaultMaxBalance;
    }

    @XmlAttribute(name = "userAccountDefaultMaxBalance")
    public void setUserAccountDefaultMaxBalance(final String userAccountDefaultMaxBalance) {
        this.userAccountDefaultMaxBalance = userAccountDefaultMaxBalance;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(final String name) {
        this.name = name;
    }

    public String getRadix() {
        return radix;
    }

    @XmlAttribute(name = "radix")
    public void setRadix(final String radix) {
        this.radix = radix;
    }

    public String getDescription() {
        return description;
    }

    @XmlAttribute(name = "description")
    public void setDescription(final String description) {
        this.description = description;
    }

    public String getTaxCode() {
        return taxCode;
    }

    @XmlAttribute(name = "taxCode")
    public void setTaxCode(final String taxCode) {
        this.taxCode = taxCode;
    }

    public boolean isVirtual() {
        return virtual;
    }

    @XmlAttribute(name = "virtual")
    public void setVirtual(final boolean virtual) {
        this.virtual = virtual;
    }
}
