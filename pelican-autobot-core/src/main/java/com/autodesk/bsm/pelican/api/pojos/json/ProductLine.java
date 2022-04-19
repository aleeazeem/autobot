package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;

public class ProductLine extends PelicanPojo {

    private ProductLineData data;
    private Included included;
    private Errors errors;

    public ProductLineData getData() {
        return data;
    }

    public void setData(final ProductLineData data) {
        this.data = data;
    }

    /**
     * @return the included
     */
    public Included getIncluded() {
        return included;
    }

    /**
     * @param included the included to set
     */
    public void setIncluded(final Included included) {
        this.included = included;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(final Errors errors) {
        this.errors = errors;
    }
}
