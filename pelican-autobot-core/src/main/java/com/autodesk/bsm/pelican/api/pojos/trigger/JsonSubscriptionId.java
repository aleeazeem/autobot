package com.autodesk.bsm.pelican.api.pojos.trigger;

/**
 * This class represents pojo for the JSON body for subscription renewal, expiration job and invoice job
 *
 * @author jains
 */
public class JsonSubscriptionId {

    private String subscriptionId;

    /**
     * This method return the subscription id
     *
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * This method sets the subscription id
     */
    public void setSubscriptionId(final String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

}
