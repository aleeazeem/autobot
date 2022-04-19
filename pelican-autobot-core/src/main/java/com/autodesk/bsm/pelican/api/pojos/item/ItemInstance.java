package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "itemInstance")
public class ItemInstance extends PelicanPojo {

    private String createdDateTime;
    private String id;
    private String itemId;
    private String appId;
    private String expirationStatus;
    private String expirationDate;
    private String ownerId;
    private String ownerExtKey;
    private String revision;
    private String subscriptionId;
    private boolean active;
    private Item item;
    private String lastModified;

    public String getLastModified() {
        return lastModified;
    }

    @XmlAttribute(name = "lastModified")
    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    @XmlAttribute(name = "createdDate")
    public void setCreatedDateTime(final String dateTime) {
        this.createdDateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String value) {
        this.id = value;
    }

    public String getItemId() {
        return itemId;
    }

    @XmlAttribute
    public void setItemId(final String value) {
        this.itemId = value;
    }

    public String getAppId() {
        return appId;
    }

    @XmlAttribute
    public void setAppId(final String value) {
        this.appId = value;
    }

    public String getExpirationStatus() {
        return expirationStatus;
    }

    @XmlAttribute
    public void setExpirationStatus(final String value) {
        this.expirationStatus = value;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    @XmlAttribute
    public void setExpirationDate(final String value) {
        this.expirationDate = value;
    }

    public String getOwnerId() {
        return ownerId;
    }

    @XmlAttribute
    public void setOwnerId(final String value) {
        this.ownerId = value;
    }

    public String getOwnerExternalKey() {
        return ownerExtKey;
    }

    @XmlAttribute
    public void setOwnerExternalKey(final String value) {
        this.ownerExtKey = value;
    }

    public String getRevision() {
        return revision;
    }

    @XmlAttribute
    public void setRevision(final String value) {
        this.revision = value;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    @XmlAttribute
    public void setSubscriptionId(final String value) {
        this.subscriptionId = value;
    }

    public boolean isActive() {
        return active;
    }

    @XmlAttribute
    public void setActive(final boolean value) {
        this.active = value;
    }

    @XmlElement
    public Item getItem() {
        return item;
    }

    public void setItem(final Item item) {
        this.item = item;
    }
}
