package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.PurchaseType;

import java.util.List;

public class JLineItem {

    private String offering;
    private String offer;
    private String priceId;
    private Integer quantity;
    private List<JAdditionalFee> additionalFees;
    private Totals totals;
    private String promotionId;
    private String promotionCode;
    private String purchaseType;
    private String subscriptionId;
    private String targetRenewalDate;
    private String creditDays;

    private JLineItem(final String offering, final String offer, final String priceId, final Integer quantity,
        final List<JAdditionalFee> additionalFees, final PurchaseType purchaseType, final String subscriptionId) {
        this.offering = offering;
        this.offer = offer;
        this.priceId = priceId;
        this.quantity = quantity;
        this.additionalFees = additionalFees;
        this.purchaseType = purchaseType.getName();
        this.subscriptionId = subscriptionId;
    }

    private JLineItem(final String offering, final String offer, final String priceId, final Integer quantity,
        final List<JAdditionalFee> additionalFees, final PurchaseType purchaseType, final String subscriptionId,
        final String targetRenewalDate, final String promotionId, final String creditDays) {
        this.offering = offering;
        this.offer = offer;
        this.priceId = priceId;
        this.quantity = quantity;
        this.additionalFees = additionalFees;
        this.purchaseType = purchaseType.getName();
        this.subscriptionId = subscriptionId;
        this.targetRenewalDate = targetRenewalDate;
        this.promotionId = promotionId;
        this.creditDays = creditDays;
    }

    private JLineItem(final String offering, final String offer, final String priceId,
        final List<JAdditionalFee> additionalFees, final PurchaseType purchaseType, final String subscriptionId) {
        this.offering = offering;
        this.offer = offer;
        this.priceId = priceId;
        this.additionalFees = additionalFees;
        this.purchaseType = purchaseType.getName();
        this.subscriptionId = subscriptionId;
    }

    public JLineItem(final String priceId, final Integer quantity, final String subscriptionId) {
        this.priceId = priceId;
        this.quantity = quantity;
        this.subscriptionId = subscriptionId;
    }

    public JLineItem(final String priceId, final Integer quantity, final String subscriptionId,
        final String targetRenewalDate) {
        this.priceId = priceId;
        this.quantity = quantity;
        this.subscriptionId = subscriptionId;
        this.setTargetRenewalDate(targetRenewalDate);
    }

    public JLineItem(final String priceId, final int quantity, final List<JAdditionalFee> additionalFees) {
        this(null, null, priceId, quantity, additionalFees, PurchaseType.OFFERING, null);
    }

    public JLineItem(final String priceId, final List<JAdditionalFee> additionalFees) {
        this(priceId, 1, additionalFees);
    }

    public JLineItem(final PurchaseType purchaseType, final String priceId, final Integer quantity,
        final String subscriptionId, final List<JAdditionalFee> additionalFees) {
        this(null, null, priceId, quantity, additionalFees, purchaseType, subscriptionId);
    }

    public JLineItem(final PurchaseType purchaseType, final String priceId, final Integer quantity,
        final String subscriptionId, final String targetRenewalDate, final List<JAdditionalFee> additionalFees) {
        this(null, null, priceId, quantity, additionalFees, purchaseType, subscriptionId, targetRenewalDate, null,
            null);
    }

    /**
     *
     * @param priceId
     * @param quantity
     * @param purchaseType
     * @param subscriptionId
     * @param targetRenewalDate
     * @param promotionId
     * @param creditDays
     */
    public JLineItem(final String priceId, final Integer quantity, final List<JAdditionalFee> additionalFees,
        final PurchaseType purchaseType, final String subscriptionId, final String targetRenewalDate,
        final String promotionId, final String creditDays) {
        this(null, null, priceId, quantity, additionalFees, purchaseType, subscriptionId, targetRenewalDate,
            promotionId, creditDays);
    }

    public JLineItem(final PurchaseType purchaseType, final String priceId, final String subscriptionId,
        final List<JAdditionalFee> additionalFees) {
        this(null, null, priceId, additionalFees, purchaseType, subscriptionId);
    }

    public String getOffering() {
        return offering;
    }

    public void setOffering(final String offering) {
        this.offering = offering;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(final String offer) {
        this.offer = offer;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(final String priceId) {
        this.priceId = priceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public List<JAdditionalFee> getAdditionalFees() {
        return additionalFees;
    }

    public void setAdditionalFees(final List<JAdditionalFee> additionalFees) {
        this.additionalFees = additionalFees;
    }

    public Totals getTotals() {
        return totals;
    }

    public void setTotals(final Totals totals) {
        this.totals = totals;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(final String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(final String promotionCode) {
        this.promotionCode = promotionCode;
    }

    /**
     * @return the purchaseType
     */
    public String getPurchaseType() {
        return purchaseType;
    }

    /**
     * @param purchaseType the purchaseType to set
     */
    public void setPurchaseType(final String purchaseType) {
        this.purchaseType = purchaseType;
    }

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param subscriptionId the subscriptionId to set
     */
    public void setSubscriptionId(final String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * This is the method to return the target renewal date of the subscription in the line item.
     *
     * @return target renewal date of the subscription
     */
    public String getTargetRenewalDate() {
        return targetRenewalDate;
    }

    /**
     * This is the method to set the target renewal date of the subscription in the line item
     *
     * @param targetRenewalDate - the date to which subscription has to be extended
     */
    private void setTargetRenewalDate(final String targetRenewalDate) {
        this.targetRenewalDate = targetRenewalDate;
    }

    public String getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(final String creditDays) {
        this.creditDays = creditDays;
    }
}
