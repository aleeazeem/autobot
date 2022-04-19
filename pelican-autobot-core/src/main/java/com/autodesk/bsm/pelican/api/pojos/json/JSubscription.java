package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;

import java.util.List;

/**
 * This class represents the JSON object of the request to get subscription by id
 *
 * @author jains
 */
public class JSubscription extends PelicanPojo {
    private Subscription data;
    private Included included;
    private List<Errors> errors;

    /**
     * This class represents JSON object of Included inside Subscription JSON object
     */
    public static class Included {
        private Price price;
        private SubscriptionOffering offering;
        private BillingPlan billingPlan;

        public Price getPrice() {
            return price;
        }

        public void setPrice(final Price price) {
            this.price = price;
        }

        public BillingPlan getBillingPlan() {
            return billingPlan;
        }

        public void setBillingPlan(final BillingPlan billingPlan) {
            this.billingPlan = billingPlan;
        }

        public SubscriptionOffering getOffering() {
            return offering;
        }

        public void setOffering(final SubscriptionOffering offering) {
            this.offering = offering;
        }
    }

    public Subscription getData() {
        return data;
    }

    public void setData(final Subscription data) {
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
