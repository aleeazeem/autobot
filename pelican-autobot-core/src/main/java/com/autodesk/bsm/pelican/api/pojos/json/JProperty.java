package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/**
 * This class represents Property JSON object
 *
 * @author jains
 */
public class JProperty extends PelicanPojo {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
