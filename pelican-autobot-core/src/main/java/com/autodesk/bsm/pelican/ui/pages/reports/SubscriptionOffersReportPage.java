package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Admin Tool's Subscription Offers Report. Reports ->Offering Reports ->Subscription Offers Reports
 *
 * @author jains
 */

public class SubscriptionOffersReportPage extends GenericDetails {

    public SubscriptionOffersReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(name = "includeNew")
    private WebElement offerStatusNewCheckbox;

    @FindBy(name = "includeActive")
    private WebElement offerStatusActiveCheckbox;

    @FindBy(name = "includeCanceled")
    private WebElement offerStatusCanceledCheckbox;

    @FindBy(name = "includeCommercial")
    private WebElement planUsageTypeCommercialCheckbox;

    @FindBy(name = "includeEducation")
    private WebElement planUsageTypeEducationCheckbox;

    @FindBy(name = "includeNonCommercial")
    private WebElement planUsageTypeNonCommercialCheckbox;

    @FindBy(name = "includeTrial")
    private WebElement planUsageTypeTrialCheckbox;

    @FindBy(name = "includeGovernment")
    private WebElement planUsageTypeGovernmentCheckbox;

    @FindBy(name = "includePlanData")
    private WebElement includePlanDataCheckbox;

    @FindBy(name = "includePrices")
    private WebElement includePricesCheckbox;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOffersReportPage.class.getSimpleName());

    private String selectedProductLine = "";

    /**
     * Method to get subscription offer report with the selected filters when page is navigated to report result page.
     *
     * @param productLine
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @param isIncludePlanData
     * @param isIncludePrices
     * @param action
     * @param storeType
     * @param store
     *
     * @return SubscriptionOffersReportResultPage
     */
    public SubscriptionOffersReportResultPage getReportWithSelectedFilters(final String productLine,
        final boolean isOfferStatusNew, final boolean isOfferStatusActive, final boolean isOfferStatusCanceled,
        final boolean isUsageTypeCommercial, final boolean isUsageTypeEducation, final boolean isUsageTypeNonCommercial,
        final boolean isUsageTypeTrial, final boolean isUsageTypeGovernment, final int storeTypeIndex,
        final int storeIndex, final boolean isIncludePlanData, final boolean isIncludePrices, final String action) {

        navigateToPage(getUrl());
        checkPageLoaded(TimeConstants.THREE_SEC);

        // select product line
        if (productLine != null) {
            selectProductLine(productLine);
            selectedProductLine = getFirstSelectedSelectValue(productLineSelect);
        }

        // set offer status
        setOfferStatus(isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled);
        // set usage type
        setUsageType(isUsageTypeCommercial, isUsageTypeEducation, isUsageTypeNonCommercial, isUsageTypeTrial,
            isUsageTypeGovernment);
        // select store type
        if (storeTypeIndex != 0) {
            selectStoreTypeByIndex(storeTypeIndex);
        }
        // select store
        if (storeIndex != 0) {
            selectStoreByIndex(storeIndex);
        }

        // check/uncheck include plan data checkbox
        if (isIncludePlanData) {
            getActions().check(includePlanDataCheckbox);
        } else {
            getActions().uncheck(includePlanDataCheckbox);
        }

        // check/uncheck include prices checkbox
        if (isIncludePrices) {
            getActions().check(includePricesCheckbox);
        } else {
            getActions().uncheck(includePricesCheckbox);
        }

        selectViewDownloadAction(action);

        submit(TimeConstants.ONE_SEC);

        return super.getPage(SubscriptionOffersReportResultPage.class);
    }

    /**
     * Method to get subscription offer report with the selected filters when there is an error on the page.
     *
     * @param applicationFamily
     * @param productLine
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @param storeType
     * @param store
     * @param isIncludePlanData
     * @param isIncludePrices
     * @param action
     * @return SubscriptionOffersReportPage
     */
    public SubscriptionOffersReportPage getReportWithSelectedFiltersError(final String applicationFamily,
        final String productLine, final boolean isOfferStatusNew, final boolean isOfferStatusActive,
        final boolean isOfferStatusCanceled, final boolean isUsageTypeCommercial, final boolean isUsageTypeEducation,
        final boolean isUsageTypeNonCommercial, final boolean isUsageTypeTrial, final boolean isUsageTypeGovernment,
        final int storeTypeIndex, final int storeIndex, final boolean isIncludePlanData, final boolean isIncludePrices,
        final String action) {

        getReportWithSelectedFilters(productLine, isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled,
            isUsageTypeCommercial, isUsageTypeEducation, isUsageTypeNonCommercial, isUsageTypeTrial,
            isUsageTypeGovernment, storeTypeIndex, storeIndex, isIncludePlanData, isIncludePrices, action);
        Util.waitInSeconds(TimeConstants.ONE_SEC);

        return super.getPage(SubscriptionOffersReportPage.class);
    }

    /**
     * Method to get the selected product line.
     *
     * @return
     */
    public String getSelectedProductLine() {
        return selectedProductLine;
    }

    /**
     * Method to set usage type.
     *
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     */
    private void setUsageType(final boolean isUsageTypeCommercial, final boolean isUsageTypeEducation,
        final boolean isUsageTypeNonCommercial, final boolean isUsageTypeTrial, final boolean isUsageTypeGovernment) {
        if (isUsageTypeCommercial) {
            getActions().check(planUsageTypeCommercialCheckbox);
        } else {
            getActions().uncheck(planUsageTypeCommercialCheckbox);
        }

        if (isUsageTypeEducation) {
            getActions().check(planUsageTypeEducationCheckbox);
        } else {
            getActions().uncheck(planUsageTypeEducationCheckbox);
        }

        if (isUsageTypeNonCommercial) {
            getActions().check(planUsageTypeNonCommercialCheckbox);
        } else {
            getActions().uncheck(planUsageTypeNonCommercialCheckbox);
        }

        if (isUsageTypeTrial) {
            getActions().check(planUsageTypeTrialCheckbox);
        } else {
            getActions().uncheck(planUsageTypeTrialCheckbox);
        }
        if (isUsageTypeGovernment) {
            getActions().check(planUsageTypeGovernmentCheckbox);
        } else {
            getActions().uncheck(planUsageTypeGovernmentCheckbox);
        }
        LOGGER.info("Usage type is set.");
    }

    /**
     * Method to set offer status.
     *
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     */
    private void setOfferStatus(final boolean isOfferStatusNew, final boolean isOfferStatusActive,
        final boolean isOfferStatusCanceled) {
        if (isOfferStatusNew) {
            getActions().check(offerStatusNewCheckbox);
        } else {
            getActions().uncheck(offerStatusNewCheckbox);
        }
        if (isOfferStatusActive) {
            getActions().check(offerStatusActiveCheckbox);
        } else {
            getActions().uncheck(offerStatusActiveCheckbox);
        }
        if (isOfferStatusCanceled) {
            getActions().check(offerStatusCanceledCheckbox);
        } else {
            getActions().uncheck(offerStatusCanceledCheckbox);
        }
        LOGGER.info("Offer status is set.");
    }

    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.SUBSCRIPTION_OFFERS_REPORT.getForm();
    }

    private void selectStoreTypeByIndex(final int storeTypeIndex) {
        if (storeTypeIndex > 0) {
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            getActions().selectByIndex(storeTypeSelect, storeTypeIndex);
        }
    }

    private void selectStoreByIndex(final int storeIndex) {
        if (storeIndex > 0) {
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            getActions().selectByIndex(storeSelect, storeIndex);
        }
    }

}
