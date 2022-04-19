package com.autodesk.bsm.pelican.api.requests;

import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.enums.UsageType;

public class SubscriptionSyncRequestBuilder {

    private UsageType usageType;
    private SubscriptionPlan plan;
    private String userExternalKey;
    private long promotionId;
    private Integer promotionCyclesUsed;
    private int promotionCyclesRemaining;
    private int appliedCount;
    private String newNextBillingDate;

    public UsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(final UsageType usageType) {
        this.usageType = usageType;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(final SubscriptionPlan plan) {
        this.plan = plan;
    }

    public String getUserExternalKey() {
        return userExternalKey;
    }

    public void setUserExternalKey(final String userExternalKey) {
        this.userExternalKey = userExternalKey;
    }

    public long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(final long promotionId) {
        this.promotionId = promotionId;
    }

    public Integer getPromotionCyclesUsed() {
        return promotionCyclesUsed;
    }

    public void setPromotionCyclesUsed(final Integer promotionCyclesUsed) {
        this.promotionCyclesUsed = promotionCyclesUsed;
    }

    public int getPromotionCyclesRemaining() {
        return promotionCyclesRemaining;
    }

    public void setPromotionCyclesRemaining(final int promotionCyclesRemaining) {
        this.promotionCyclesRemaining = promotionCyclesRemaining;
    }

    public int getAppliedCount() {
        return appliedCount;
    }

    public void setAppliedCount(final int appliedCount) {
        this.appliedCount = appliedCount;
    }

    public String getNewNextBillingDate() {
        return newNextBillingDate;
    }

    public void setNewNextBillingDate(final String newNextBillingDate) {
        this.newNextBillingDate = newNextBillingDate;
    }
}
