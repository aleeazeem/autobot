package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;

/*
 * This is the class which is used to set and get the values for the Subscription Offer Data, Included and errors when
 * creating a subscription offer through create subscription offer api
 */
public class SubscriptionOffer extends PelicanPojo {
    private SubscriptionOfferData data;
    private Included included;
    private Errors errors;

    /**
     * @return the subscription offer data
     */
    public SubscriptionOfferData getData() {
        return data;
    }

    /**
     * @param set the subscription offer data
     */
    public void setData(final SubscriptionOfferData data) {
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
