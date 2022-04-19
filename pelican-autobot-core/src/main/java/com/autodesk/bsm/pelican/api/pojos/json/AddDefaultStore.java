package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;

import java.util.List;

public class AddDefaultStore extends PelicanPojo {

    private DefaultStoreData data;
    private Included included;
    private List<Errors> errors;

    /**
     * @return the data
     */
    public DefaultStoreData getData() {
        return data;
    }

    /**
     * @param set the data
     */
    public void setData(final DefaultStoreData data) {
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
    public List<Errors> getErrors() {
        return errors;
    }

    /**
     * @param set the errors
     */
    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

}
