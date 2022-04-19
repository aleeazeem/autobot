package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BuyerUser {

    private String id;
    private String email;
    private String externalKey;
    private String name;
    private String initialExportControlStatus;
    private String finalExportControlStatus;

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(final String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    @XmlAttribute
    public void setEmail(final String email) {
        this.email = email;
    }

    public String getExternalKey() {
        return externalKey;
    }

    @XmlAttribute
    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String name) {
        this.name = name;
    }

    public String getInitialExportControlStatus() {
        return initialExportControlStatus;
    }

    @XmlElement
    public void setInitialExportControlStatus(final String initialExportControlStatus) {
        this.initialExportControlStatus = initialExportControlStatus;
    }

    public String getFinalExportControlStatus() {
        return finalExportControlStatus;
    }

    @XmlElement(name = "finalExportControlStatus")
    public void setFinalExportControlStatus(final String finalExportControlStatus) {
        this.finalExportControlStatus = finalExportControlStatus;
    }
}
