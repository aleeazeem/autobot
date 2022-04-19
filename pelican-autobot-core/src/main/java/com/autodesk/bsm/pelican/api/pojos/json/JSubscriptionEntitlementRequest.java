package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

/**
 * This class represents the JSON object of the Add Entitlement request
 *
 * @author jains
 */
public class JSubscriptionEntitlementRequest extends PelicanPojo {
    private List<JSubscriptionEntitlementData> data;

    public List<JSubscriptionEntitlementData> getData() {
        return data;
    }

    public void setData(final List<JSubscriptionEntitlementData> data) {
        this.data = data;
    }

}
