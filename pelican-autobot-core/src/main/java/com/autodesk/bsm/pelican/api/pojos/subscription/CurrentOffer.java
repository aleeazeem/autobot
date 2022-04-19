package com.autodesk.bsm.pelican.api.pojos.subscription;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CurrentOffer {

    private String id;
    private String appFamilyId;
    private String name;
    private String extKey;

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String value) {
        this.id = value;
    }

    public String getApplicationFamilyId() {
        return appFamilyId;
    }

    @XmlAttribute
    public void setApplicationFamilyId(final String value) {
        this.appFamilyId = value;
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
}
