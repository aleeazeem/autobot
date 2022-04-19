package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;

import java.util.List;

/**
 * This class represents the JSON object of the request to get default store for countries
 *
 * @author Muhammad
 */
public class GetDefaultStore extends PelicanPojo {

    private List<DefaultStoreData> data;
    private Included included;
    private List<Errors> errors;
    private MetaPagination meta;

    /**
     * @return the data
     */
    public List<DefaultStoreData> getData() {
        return data;
    }

    /**
     * @param set the data
     */
    public void setData(final List<DefaultStoreData> data) {
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

    public MetaPagination getMeta() {
        return meta;
    }

    public void setMeta(final MetaPagination meta) {
        this.meta = meta;
    }
}
