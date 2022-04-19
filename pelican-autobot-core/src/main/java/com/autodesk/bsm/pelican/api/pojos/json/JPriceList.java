package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;

public class JPriceList extends PelicanPojo {

    private Data data;
    private Included included;
    private Errors errors;

    public Data getData() {
        return data;
    }

    public void setData(final Data data) {
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
