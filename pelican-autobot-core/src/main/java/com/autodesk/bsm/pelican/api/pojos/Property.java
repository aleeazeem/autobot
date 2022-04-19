package com.autodesk.bsm.pelican.api.pojos;

import javax.xml.bind.annotation.XmlAttribute;

public class Property {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String value) {
        this.name = value;
    }

    public String getValue() {
        return value;
    }

    @XmlAttribute
    public void setValue(final String value) {
        this.value = value;
    }
}
