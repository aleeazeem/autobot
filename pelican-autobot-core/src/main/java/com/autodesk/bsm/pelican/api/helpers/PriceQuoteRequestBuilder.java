package com.autodesk.bsm.pelican.api.helpers;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JAdditionalFee;
import com.autodesk.bsm.pelican.api.pojos.json.JLineItem;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionReference;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Shipping;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.enums.PurchaseType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import java.util.List;
import java.util.Map;

public class PriceQuoteRequestBuilder {

    private final String buyerId;
    private final List<JLineItem> lineItems;
    private final List<JAdditionalFee> additionalFees;
    private final List<JPromotionReference> promotionReferencesList;
    private final PelicanPlatform resource;
    private final EnvironmentVariables environmentVariables;
    private final Map<Offerings, List<JPromotion>> offeringsJPromotionMap;
    private final PurchaseType purchaseType;
    private final List<Subscription> subscriptionList;
    private final Shipping shipping;

    public PriceQuoteRequestBuilder(final Builder builder) {
        this.buyerId = builder.buyerId;
        this.lineItems = builder.lineItems;
        this.additionalFees = builder.additionalFees;
        this.promotionReferencesList = builder.promotionReferencesList;
        this.resource = builder.resource;
        this.environmentVariables = builder.environmentVariables;
        this.offeringsJPromotionMap = builder.offeringsJPromotionMap;
        this.purchaseType = builder.purchaseType;
        this.subscriptionList = builder.subscriptionList;
        this.shipping = builder.shipping;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBuyerId() {
        return buyerId;
    }

    public List<JLineItem> getLineItems() {
        return lineItems;
    }

    public List<JAdditionalFee> getAdditionalFees() {
        return additionalFees;
    }

    public List<JPromotionReference> getPromotionReferencesList() {
        return promotionReferencesList;
    }

    public PelicanPlatform getResource() {
        return resource;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    public Map<Offerings, List<JPromotion>> getOfferingsJPromotionMap() {
        return offeringsJPromotionMap;
    }

    public PurchaseType getPurchaseType() {
        return purchaseType;
    }

    public List<Subscription> getSubscriptionList() {
        return subscriptionList;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public static class Builder {
        private String buyerId;
        private List<JLineItem> lineItems;
        private List<JAdditionalFee> additionalFees;
        private List<JPromotionReference> promotionReferencesList;
        private PelicanPlatform resource;
        private EnvironmentVariables environmentVariables;
        private Map<Offerings, List<JPromotion>> offeringsJPromotionMap;
        private PurchaseType purchaseType;
        private List<Subscription> subscriptionList;
        private Shipping shipping;

        public PriceQuoteRequestBuilder build() {
            return new PriceQuoteRequestBuilder(this);
        }

        public Builder setBuyerId(final String buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder setLineItems(final List<JLineItem> lineItems) {
            this.lineItems = lineItems;
            return this;
        }

        public Builder setAdditionalFees(final List<JAdditionalFee> additionalFees) {
            this.additionalFees = additionalFees;
            return this;
        }

        public Builder setPromotionReferences(final List<JPromotionReference> promotionReferencesList) {
            this.promotionReferencesList = promotionReferencesList;
            return this;
        }

        public Builder setResource(final PelicanPlatform resource) {
            this.resource = resource;
            return this;
        }

        public Builder setEnvironmentVariables(final EnvironmentVariables environmentVariables) {
            this.environmentVariables = environmentVariables;
            return this;
        }

        public Builder setOfferingsJPromotionMap(final Map<Offerings, List<JPromotion>> offeringsJPromotionMap) {
            this.offeringsJPromotionMap = offeringsJPromotionMap;
            return this;
        }

        public Builder setPurchaseType(final PurchaseType purchaseType) {
            this.purchaseType = purchaseType;
            return this;
        }

        public Builder setSubscriptionList(final List<Subscription> subscriptionList) {
            this.subscriptionList = subscriptionList;
            return this;
        }

        public Builder setShipping(final Shipping shipping) {
            this.shipping = shipping;
            return this;
        }
    }

}
