package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;

/*
 * This is the class which is used to set and get the values for the Data, Included and errors when creating a store
 * type through create store type api
 */
public class StoreType extends PelicanPojo {
    private Data data;
    private Included included;
    private Errors errors;

    /**
     * @return the data
     */
    public Data getData() {
        return data;
    }

    /**
     * @param set the data
     */
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
     * @param set the included
     */
    public void setIncluded(final Included included) {
        this.included = included;
    }

    /**
     * @return the errors
     */
    public Errors getErrors() {
        return errors;
    }

    /**
     * @param set the errors
     */
    public void setErrors(final Errors errors) {
        this.errors = errors;
    }

}
