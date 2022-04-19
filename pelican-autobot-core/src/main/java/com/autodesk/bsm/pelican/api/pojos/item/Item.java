package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Item extends PelicanPojo {

    private String id;
    private String name;
    private String extKey;
    private String skuExtension;
    private String appId;
    private boolean extCheckRequired;
    private ItemType itemType;
    private String expirationModel;

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String value) {
        this.name = value;
    }

    public String getExternalKey() {
        return extKey;
    }

    @XmlAttribute
    public void setExternalKey(final String value) {
        this.extKey = value;
    }

    public String getSkuExtension() {
        return skuExtension;
    }

    @XmlAttribute
    public void setSkuExtension(final String value) {
        this.skuExtension = value;
    }

    public String getAppId() {
        return appId;
    }

    @XmlAttribute
    public void setAppId(final String value) {
        this.appId = value;
    }

    public boolean isExternalCheckRequired() {
        return extCheckRequired;
    }

    @XmlAttribute
    public void setExternalCheckRequired(final boolean value) {
        this.extCheckRequired = value;
    }

    public ItemType getItemType() {
        return itemType;
    }

    @XmlElement
    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    public String getExpirationModel() {
        return expirationModel;
    }

    @XmlElement
    public void setExpirationModel(final String value) {
        this.expirationModel = value;
    }
}
