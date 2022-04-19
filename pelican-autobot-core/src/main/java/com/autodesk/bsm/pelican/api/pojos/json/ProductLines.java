package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings.Included;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the JSON object of Product Lines.
 *
 * @author Muhammad
 */
public class ProductLines extends PelicanPojo {
    private List<ProductLineData> productLineData;
    private Included included;
    private List<Errors> errors;
    private MetaPagination meta;

    public Included getIncluded() {
        return included;
    }

    public void setIncluded(final Included included) {
        this.included = included;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    public List<ProductLineData> getProductLineData() {
        if (productLineData == null) {
            productLineData = new ArrayList<>();
        }
        return productLineData;
    }

    public void setProductLineData(final List<ProductLineData> productLineData) {
        this.productLineData = productLineData;
    }

    public void setMetaPagination(final MetaPagination meta) {
        this.meta = meta;
    }

    public MetaPagination getMetaPagination() {
        return meta;
    }
}
