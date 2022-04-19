package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a pojo class for item type
 *
 * @author t_mohag
 */
@XmlRootElement
public class ItemType extends PelicanPojo {

    private String id;
    private String name;
    private boolean displayLeftNav;
    private int positionInTree;
    private String appId;
    private String externalKey;
    private HttpError httpError;

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

    public boolean isDisplayLeftNav() {
        return displayLeftNav;
    }

    @XmlAttribute
    public void setDisplayLeftNav(final boolean value) {
        this.displayLeftNav = value;
    }

    public int getPositionInTree() {
        return positionInTree;
    }

    @XmlAttribute
    public void setPositionInTree(final int value) {
        this.positionInTree = value;
    }

    public String getAppId() {
        return appId;
    }

    @XmlAttribute
    public void setAppId(final String value) {
        this.appId = value;
    }

    public String getExternalKey() {
        return externalKey;
    }

    @XmlAttribute
    public void setExternalKey(final String value) {
        this.externalKey = value;
    }

    public HttpError getHttpError() {
        return httpError;
    }

    @XmlElement(name = "error")
    public void setHttpError(final HttpError httpError) {
        this.httpError = httpError;
    }
}
