package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the JSON object of the request to get subscriptions
 *
 * @author t_mohag
 */
public class JSubscriptions extends PelicanPojo {
    private JSubscriptionsData data;
    private Included included;
    private List<Errors> errors;

    /**
     * This class represents JSON object of Included array in subscriptions response
     */
    public static class Included {
        private List<Price> prices = new ArrayList<>();
        private List<SubscriptionOffering> offerings = new ArrayList<>();
        private List<BillingPlan> billingPlans = new ArrayList<>();

        public List<Price> getPrices() {
            return prices;
        }

        public void setPrices(final List<Price> prices) {
            this.prices = prices;
        }

        public List<SubscriptionOffering> getOfferings() {
            return offerings;
        }

        public void setOfferings(final List<SubscriptionOffering> offerings) {
            this.offerings = offerings;
        }

        public List<BillingPlan> getBillingPlans() {
            return billingPlans;
        }

        public void setBillingPlans(final List<BillingPlan> billingPlans) {
            this.billingPlans = billingPlans;
        }
    }

    public JSubscriptionsData getData() {
        return data;
    }

    public void setData(final JSubscriptionsData data) {
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
