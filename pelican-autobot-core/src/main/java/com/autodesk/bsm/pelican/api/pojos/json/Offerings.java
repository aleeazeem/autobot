package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

public class Offerings extends PelicanPojo {

    private List<Offering> offerings;
    private Offering data;
    private Included included;
    private List<Errors> errorsList;
    private Errors error;
    private Meta meta;

    public static class Included {
        private OfferingDetail offeringDetail;
        private List<BillingPlan> billingPlans;
        private List<Price> prices;
        private List<JPromotionData> promotions;

        public OfferingDetail getOfferingDetail() {
            return offeringDetail;
        }

        public void setOfferingDetail(final OfferingDetail offeringDetail) {
            this.offeringDetail = offeringDetail;
        }

        public List<BillingPlan> getBillingPlans() {
            if (billingPlans == null) {
                billingPlans = new ArrayList<>();
            }
            return billingPlans;
        }

        public void setBillingPlans(final List<BillingPlan> billingPlans) {
            this.billingPlans = billingPlans;
        }

        public List<Price> getPrices() {
            if (prices == null) {
                prices = new ArrayList<>();
            }
            return prices;
        }

        public void setPrices(final List<Price> prices) {
            this.prices = prices;
        }

        public List<JPromotionData> getPromotions() {
            if (promotions == null) {
                promotions = new ArrayList<>();
            }
            return promotions;
        }

        public void setPromotions(final List<JPromotionData> promotions) {
            this.promotions = promotions;
        }
    }

    public Offering getOffering() {
        return data;
    }

    public void setOffering(final Offering data) {
        this.data = data;
    }

    public List<Offering> getOfferings() {
        if (offerings == null) {
            offerings = new ArrayList<>();
        }
        return offerings;
    }

    public void setOfferings(final List<Offering> offerings) {
        this.offerings = offerings;
    }

    public Included getIncluded() {
        return included;
    }

    public void setIncluded(final Included included) {
        this.included = included;
    }

    public List<Errors> getErrors() {
        return errorsList;
    }

    public void setErrors(final List<Errors> errorsList) {
        this.errorsList = errorsList;
    }

    public Errors getError() {
        return error;
    }

    public void setError(final Errors error) {
        this.error = error;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(final Meta meta) {
        this.meta = meta;
    }
}
