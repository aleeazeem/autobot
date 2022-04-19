package com.autodesk.bsm.pelican.api.pojos.json;

/**
 * @author vineel
 */
public class Meta {

    private CurrentOffering currentOffering;
    private CurrentBillingPlan currentBillingPlan;

    public CurrentOffering getCurrentOffering() {
        return currentOffering;
    }

    public void setCurrentOffering(final CurrentOffering currentOffering) {
        this.currentOffering = currentOffering;
    }

    public CurrentBillingPlan getCurrentBillingPlan() {
        return currentBillingPlan;
    }

    public void setCurrentBillingPlan(final CurrentBillingPlan currentBillingPlan) {
        this.currentBillingPlan = currentBillingPlan;
    }

}
