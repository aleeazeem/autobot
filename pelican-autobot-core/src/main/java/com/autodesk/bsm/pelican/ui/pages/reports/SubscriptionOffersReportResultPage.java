package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Page class for subscription offer report result page. Reports ->Subscription Offers Reports
 * ->SubscriptionOffersReportResultPage
 *
 * @author jains
 */
public class SubscriptionOffersReportResultPage extends GenericDetails {

    public SubscriptionOffersReportResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to get PlanExternalKey
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanExternalKeyColumn() {
        return getReportValues("PlanExternalKey");
    }

    /**
     * Method to get PlanName
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanNameColumn() {
        return getReportValues("PlanName");
    }

    /**
     * Method to get PlanProductLine
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanProductLineColumn() {
        return getReportValues("PlanProductLine");
    }

    /**
     * Method to get PlanOfferingType
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanOfferingTypeColumn() {
        return getReportValues("PlanOfferingType");
    }

    /**
     * Method to get PlanStatus
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanStatusColumn() {
        return getReportValues("PlanStatus");
    }

    /**
     * Method to get PlanStatus
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanPackagingTypeColumn() {
        return getReportValues("PlanPackagingType");
    }

    /**
     * Method to get PlanSupportLevel
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanSupportLevelColumn() {
        return getReportValues("PlanSupportLevel");
    }

    /**
     * Method to get PlanUsageType
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanUsageTypeColumn() {
        return getReportValues("PlanUsageType");
    }

    /**
     * Method to get PlanCancellationPolicy
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanCancellationPolicyColumn() {
        return getReportValues("PlanCancellationPolicy");
    }

    /**
     * Method to get PlanOfferingDetailName
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanOfferingDetailNameColumn() {
        return getReportValues("PlanOfferingDetailName");
    }

    /**
     * Method to get PlanTaxCode
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanTaxCodeColumn() {
        return getReportValues("PlanTaxCode");
    }

    /**
     * Method to get OfferExternalKey
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferExternalKeyColumn() {
        return getReportValues("OfferExternalKey");
    }

    /**
     * Method to get OfferName
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferNameColumn() {
        return getReportValues("OfferName");
    }

    /**
     * Method to get OfferStatus
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferStatusColumn() {
        return getReportValues("OfferStatus");
    }

    /**
     * Method to get OfferFrequency
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferFrequencyColumn() {
        return getReportValues("OfferFrequency");
    }

    /**
     * Method to get Amount
     *
     * @return List<String>
     */
    public List<String> getValuesFromAmountColumn() {
        return getReportValues("Amount");
    }

    /**
     * Method to get Currency
     *
     * @return List<String>
     */
    public List<String> getValuesFromCurrencyColumn() {
        return getReportValues("Currency");
    }

    /**
     * Method to get PriceList
     *
     * @return List<String>
     */
    public List<String> getValuesFromPriceListColumn() {
        return getReportValues("PriceList");
    }

    /**
     * Method to get Store
     *
     * @return List<String>
     */
    public List<String> getValuesFromStoreColumn() {
        return getReportValues("Store");
    }

    /**
     * Method to get StoreType
     *
     * @return List<String>
     */
    public List<String> getValuesFromStoreTypeColumn() {
        return getReportValues("StoreType");
    }

    /**
     * Method to get PriceStartDate
     *
     * @return List<String>
     */
    public List<String> getValuesFromPriceStartDateColumn() {
        return getReportValues("PriceStartDate");
    }

    /**
     * Method to get PriceEndDate
     *
     * @return List<String>
     */

    public List<String> getValuesFromPriceEndDateColumn() {
        return getReportValues("PriceEndDate");
    }

    /**
     * Method to get PriceId
     *
     * @return List<String>
     */
    public List<String> getValuesFromPriceIdColumn() {
        return getReportValues("PriceId");
    }

}
