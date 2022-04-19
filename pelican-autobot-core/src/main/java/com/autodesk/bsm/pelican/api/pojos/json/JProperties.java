package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/**
 * This class represents JProperties json object
 *
 * @author jains
 */
public class JProperties extends PelicanPojo {
    private JProperty[] property;

    public JProperty[] getProperty() {
        return property;
    }

    public void setProperty(final JProperty[] property) {
        this.property = property;
    }

}
