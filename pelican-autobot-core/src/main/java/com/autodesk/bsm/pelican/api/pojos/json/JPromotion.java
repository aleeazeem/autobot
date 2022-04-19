package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

/**
 * This class represents the JSON object of the request to create a Promotion.
 *
 * @author t_mohag
 */
public class JPromotion extends PelicanPojo {
    private JPromotionData data;
    private List<Errors> errors;

    public JPromotionData getData() {
        return data;
    }

    public void setData(final JPromotionData data) {
        this.data = data;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }
}
