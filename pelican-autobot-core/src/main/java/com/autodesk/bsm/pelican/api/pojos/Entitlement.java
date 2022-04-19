package com.autodesk.bsm.pelican.api.pojos;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Entitlement {

    private String id;
    private String name;
    private String externalKey;
    private String type; // enum?
    private String licensingModelExternalKey;
    private List<String> coreProducts;
    private String itemTypeExternalKey;

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(final String name) {
        this.name = name;
    }

    public String getExternalKey() {
        return externalKey;
    }

    @XmlAttribute(name = "externalKey")
    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setType(final String type) {
        this.type = type;
    }

    public String getLicensingModelExternalKey() {
        return licensingModelExternalKey;
    }

    @XmlAttribute(name = "licensingModelExternalKey")
    public void setLicensingModelExternalKey(final String licensingModelExternalKey) {
        this.licensingModelExternalKey = licensingModelExternalKey;
    }

    public List<String> getCoreProducts() {
        return coreProducts;
    }

    @XmlElementWrapper(name = "coreProducts")
    @XmlElement(name = "coreProduct")
    public void setCoreProducts(final List<String> coreProducts) {
        this.coreProducts = coreProducts;
    }

    public String getItemTypeExternalKey() {
        return itemTypeExternalKey;
    }

    @XmlAttribute(name = "itemTypeExternalKey")
    public void setItemTypeExternalKey(final String itemTypeExternalKey) {
        this.itemTypeExternalKey = itemTypeExternalKey;
    }
}
