package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class LineItem {

    private SubscriptionPlan subsPlan;
    private SubscriptionRenewal subRenewal;
    private SubscriptionQuantity subscriptionQuantity;
    private AdditionalFees addFees;
    private Offering offering;
    private PromotionReferences promotionReferences;
    private SubscriptionExtension subscriptionExtension;
    private LineItemTotals lineItemTotals;

    public static class PromotionReferences {
        private PromotionReference promotionReference;

        public PromotionReference getPromotionReference() {
            return promotionReference;
        }

        @XmlElement
        public void setPromotionReference(final PromotionReference promoReference) {
            this.promotionReference = promoReference;
        }
    }

    public static class PromotionReference {
        private String id;
        private String code;

        public String getId() {
            return id;
        }

        @XmlAttribute
        public void setId(final String id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        @XmlAttribute
        public void setCode(final String code) {
            this.code = code;
        }
    }

    public AdditionalFees getAdditionalFees() {
        return addFees;
    }

    @XmlElement(name = "additionalFees")
    public void setAdditionalFees(final AdditionalFees addFees) {
        this.addFees = addFees;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subsPlan;
    }

    @XmlElement(name = "subscriptionPlan")
    public void setSubscriptionPlan(final SubscriptionPlan subsPlan) {
        this.subsPlan = subsPlan;
    }

    public SubscriptionRenewal getSubscriptionRenewal() {
        return subRenewal;
    }

    @XmlElement(name = "subscriptionRenewal")
    public void setSubscriptionRenewal(final SubscriptionRenewal subRenewal) {
        this.subRenewal = subRenewal;
    }

    public SubscriptionQuantity getSubscriptionQuantity() {
        return subscriptionQuantity;
    }

    @XmlElement(name = "subscriptionQuantity")
    public void setSubscriptionQuantity(final SubscriptionQuantity subscriptionQuantity) {
        this.subscriptionQuantity = subscriptionQuantity;
    }

    public Offering getOffering() {
        return offering;
    }

    @XmlElement(name = "offering")
    public void setOffering(final Offering offering) {
        this.offering = offering;
    }

    public PromotionReferences getPromotionReferences() {
        return promotionReferences;
    }

    @XmlElement(name = "promotionReferences")
    public void setPromotionReferences(final PromotionReferences promotionReferences) {
        this.promotionReferences = promotionReferences;
    }

    public SubscriptionExtension getSubscriptionExtension() {
        return subscriptionExtension;
    }

    @XmlElement(name = "subscriptionExtension")
    public void setSubscriptionExtension(final SubscriptionExtension subscriptionExtension) {
        this.subscriptionExtension = subscriptionExtension;
    }

    public LineItemTotals getLineItemTotals() {
        return lineItemTotals;
    }

    @XmlElement(name = "lineItemTotals")
    public void setLineItemTotals(final LineItemTotals lineItemTotals) {
        this.lineItemTotals = lineItemTotals;
    }

}
