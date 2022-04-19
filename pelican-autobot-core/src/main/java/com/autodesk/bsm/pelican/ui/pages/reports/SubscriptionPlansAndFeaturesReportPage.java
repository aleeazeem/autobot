package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is a page object for Subscription Plans and Features Report Page in Pelican admin tool This page can be viewed
 * in pelican Admin in Reports tab > Offering Reports link Web element objects created may not be complete. If missing
 * any web element which is required for your testing please add them.
 *
 * @author Muhammad
 */
public class SubscriptionPlansAndFeaturesReportPage extends GenericDetails {

    @FindBy(name = "includeNew")
    private WebElement includeNew;

    @FindBy(name = "includeActive")
    private WebElement includeActive;

    @FindBy(name = "includeCancelled")
    private WebElement includeCancelled;

    @FindBy(id = "externalKeyPL")
    private WebElement externalKeyProductLine;

    @FindBy(id = "externalKeyFeature")
    private WebElement externalKeyFeature;

    @FindBy(id = "keySelectionButtonPL")
    private WebElement keySelectionButtonProductLine;

    @FindBy(id = "keySelectionButtonFeature")
    private WebElement keySelectionButtonFeature;

    @FindBy(css = "#error-message-pl")
    private WebElement errorForDuplicateProductLineExtKey;

    @FindBy(css = "#error-message-feature")
    private WebElement errorForDuplicateFeatureExternalKey;

    @FindBy(id = "plFind")
    private WebElement productLineFind;

    @FindBy(id = "featureFind")
    private WebElement featureFind;

    // Status active check box
    @FindBy(name = "includeActive")
    private WebElement statusActiveCheckbox;

    // Status currency check box
    @FindBy(name = "includeCurrency")
    private WebElement includeCurrencyCheckbox;

    // Status Feature check box
    @FindBy(name = "includeFeature")
    private WebElement includeFeatureCheckbox;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionPlansAndFeaturesReportPage.class.getSimpleName());

    public SubscriptionPlansAndFeaturesReportPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private void setOfferingStatus(final boolean isOfferingStatusNew, final boolean isOfferingStatusActive,
        final boolean isOfferingStatusCanceled) {
        if (isOfferingStatusNew) {
            getActions().check(includeNew);
        } else {
            getActions().uncheck(includeNew);
        }
        if (isOfferingStatusActive) {
            getActions().check(includeActive);
        } else {
            getActions().uncheck(includeActive);
        }
        if (isOfferingStatusCanceled) {
            getActions().check(includeCancelled);
        } else {
            getActions().uncheck(includeCancelled);
        }
        LOGGER.info("Offering status is set.");
    }

    /**
     * Method to set product line external key
     */
    private void setProductLineExternalKey(final String productLineExternalKey) {
        LOGGER.info("External Key of product line '" + productLineExternalKey + "'");
        getActions().setText(externalKeyProductLine, productLineExternalKey);
    }

    /**
     * Method to add product line after product line is set.
     */
    private void addProductLine() {
        keySelectionButtonProductLine.click();
        LOGGER.info("Product Line has been added by clicking a button of Add ProductLine");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Method to add product line with given external key.
     *
     * @param productLineExternalKey
     */
    public void addProductLineWithExternalKey(final String productLineExternalKey) {
        setProductLineExternalKey(productLineExternalKey);
        addProductLine();
    }

    /**
     * Method to set feature external key
     */
    private void setFeatureExternalKey(final String featureExtKey) {
        LOGGER.info("External Key of product line '" + featureExtKey + "'");
        getActions().setText(externalKeyFeature, featureExtKey);
    }

    /**
     * Method to add product line button
     */
    private void addFeature() {
        keySelectionButtonFeature.click();
        LOGGER.info("Feature has been added by clicking a button of Add Feature");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    public void addFeatureWithExternalKey(final String featureExtKey) {
        setFeatureExternalKey(featureExtKey);
        addFeature();
    }

    /**
     * Method to get url of subscription plans and features report page.
     *
     * @return string (page url)
     */
    private String getReportUrl() {
        return getEnvironment().getAdminUrl() + "/reports/"
            + AdminPages.SUBSCRIPTION_PLANS_AND_FEATURES_REPORT.getForm();
    }

    /**
     * Method to navigate to the page of orders in export control hold report
     */
    public void navigateToSubscriptionPlanAndFeaturesReport() {
        final String url = getReportUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        // we can reduce wait once test starts passing on jenkins.
        checkPageLoaded(TimeConstants.SHORT_WAIT);
    }

    /**
     * Method to get subscription plan and feature report when page is navigated to result page.
     *
     * @param productLine
     * @param featureExtKey
     * @param isOfferingStatusActive
     * @param isOfferingStatusNew
     * @param isOfferingStatusCanceled
     * @param action
     * @return SubscriptionPlansAndFeaturesReportResultPage
     */
    public SubscriptionPlansAndFeaturesReportResultPage getReportWithSelectedFilters(final String productLine,
        final String featureExtKey, final boolean isOfferingStatusActive, final boolean isOfferingStatusNew,
        final boolean isOfferingStatusCanceled, final boolean isFeatureType, final boolean isCurrencyType,
        final String action) {
        navigateToSubscriptionPlanAndFeaturesReport();

        actionOnIncludeCheckBox(includeFeatureCheckbox, isFeatureType);
        actionOnIncludeCheckBox(includeCurrencyCheckbox, isCurrencyType);

        setOfferingStatus(isOfferingStatusNew, isOfferingStatusActive, isOfferingStatusCanceled);
        if (productLine != null) {
            addProductLineWithExternalKey(productLine);
        }
        if (featureExtKey != null) {
            addFeatureWithExternalKey(featureExtKey);
        }
        selectViewDownloadAction(action);
        submit(TimeConstants.ONE_SEC);
        return super.getPage(SubscriptionPlansAndFeaturesReportResultPage.class);
    }

    /**
     * Method to get subscription plan and feature report when there is an error on page.
     *
     * @param productLine
     * @param featureExtKey
     * @param isOfferingStatusActive
     * @param isOfferingStatusNew
     * @param isOfferingStatusCanceled
     * @param action
     * @return SubscriptionPlansAndFeaturesReportPage
     */
    public SubscriptionPlansAndFeaturesReportPage getReportWithSelectedFiltersError(final String productLine,
        final String featureExtKey, final boolean isOfferingStatusActive, final boolean isOfferingStatusNew,
        final boolean isOfferingStatusCanceled, final String action) {
        getReportWithSelectedFilters(productLine, featureExtKey, isOfferingStatusActive, isOfferingStatusNew,
            isOfferingStatusCanceled, true, false, action);
        return super.getPage(SubscriptionPlansAndFeaturesReportPage.class);
    }

    /**
     * Method to return error of Duplicate key of product line
     *
     * @return error
     */
    public String getDuplicateProductLineExtKeyError() {
        String error = null;
        try {
            error = errorForDuplicateProductLineExtKey.getText();
            LOGGER.info("Duplicate Product Line Error is generated");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Duplicate Product Line Error is not generated");
        }
        return error;
    }

    /**
     * Method to add feature or features through search functionality of features
     */
    public void addFeatureBySearchFunctionality(final int noOfTimes) {
        addThroughBySearchFunctionality(noOfTimes, featureFind);
        addFeature();
    }

    /**
     * Method to add product line through search functionality of features
     */
    public void addProductLineBySearchFunctionality(final int noOfTimes) {
        addThroughBySearchFunctionality(noOfTimes, productLineFind);
        addProductLine();
    }

    /**
     * Method to add options from pop Window through search functionality
     */
    private void addThroughBySearchFunctionality(final int noOfTimes, final WebElement element) {
        getActions().click(element);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String popUpHandle = getDriver().getWindowHandles().toArray()[1].toString();
        getDriver().switchTo().window(popUpHandle);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        for (int i = 1; i < noOfTimes + 1; i++) {
            selectRowFromPopUpGrid(i);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }

        submitButtonOnPopUpGrid();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // switch driver control back to parent window
        final String parentWindowHandle = getDriver().getWindowHandles().toArray()[0].toString();
        getDriver().switchTo().window(parentWindowHandle);

    }

    private void selectRowFromPopUpGrid(final int row) {
        final String selector = ".//*[@id='find-results']/table/tbody/tr[" + row + "]/td[1]";
        LOGGER.info("Selector for selected row: " + selector);
        final WebElement webElement = getDriver().findElement(By.xpath(selector));
        LOGGER.info("Selected element from Grid: " + webElement.toString());
        webElement.click();
    }

    /**
     * Method to get feature which are added under features
     */
    public int getFeaturesAdded() {
        final List<WebElement> rows = getDriver().findElements(By.cssSelector("#featureList>li"));
        return rows.size();

    }

    /**
     * Method to get product line which are added under Product line filter
     */
    public int getProductLineAdded() {
        final List<WebElement> rows = getDriver().findElements(By.cssSelector("#plList>li"));
        return rows.size();

    }

    /**
     * Method to return error of Duplicate key of feature filter
     *
     * @return error
     */
    public String getDuplicateFeatureExternalKeyError() {
        String error = null;
        try {
            error = errorForDuplicateFeatureExternalKey.getText();
            LOGGER.info("Duplicate Feature Error is generated");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Duplicate Feature Error is not generated");
        }
        return error;
    }

    /**
     * method to Uncheck the checked check box
     */
    public void uncheckCheckedBox(final WebElement checkbox) {

        if (checkbox.getAttribute("checked") != null) {
            checkbox.click();

        } else {
            LOGGER.info("Checkbox *Active* is already unchecked.");
        }
    }

    /**
     * method to deActivate the Active check box
     */
    public void actionOnIncludeCheckBox(final WebElement checkbox, final boolean shouldCheck) {

        if (shouldCheck ^ (checkbox.getAttribute("checked") == null) ? false : true) {
            checkbox.click();
        } else {
            LOGGER.info("Checkbox is already in required state.");
        }
    }

    /**
     * method to deActivate the Active check box
     */
    public void activateIncludeCurrencyCheckBox() {

        actionOnIncludeCheckBox(includeCurrencyCheckbox, true);
    }
}
