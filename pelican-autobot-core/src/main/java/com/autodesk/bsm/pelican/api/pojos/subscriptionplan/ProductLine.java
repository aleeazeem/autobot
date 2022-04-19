package com.autodesk.bsm.pelican.api.pojos.subscriptionplan;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProductLine {

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    @XmlAttribute
    public void setCode(final String value) {
        this.code = value;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String value) {
        this.name = value;
    }
}
