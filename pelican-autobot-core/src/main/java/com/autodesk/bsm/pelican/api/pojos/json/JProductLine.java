package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/**
 * This class represents ProductLine JSON object
 *
 * @author jains
 */
public class JProductLine extends PelicanPojo {
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(final String value) {
        this.code = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }
}
