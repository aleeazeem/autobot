package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

/**
 * This class represents the JSON object of the Add Entitlement Response
 *
 * @author jains
 */
public class JSubscriptionEntitlementResponse extends PelicanPojo {
    private JSubscriptionEntitlementData data;
    private Included included;
    private List<Errors> errors;

    /**
     * This class represents JSON object of Included inside Add Entitlement Response object
     */
    public static class Included {

    }

    public JSubscriptionEntitlementData getData() {
        return data;
    }

    public void setData(final JSubscriptionEntitlementData data) {
        this.data = data;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }

    public Included getIncluded() {
        return included;
    }

    public void setIncluded(final Included included) {
        this.included = included;
    }

}
