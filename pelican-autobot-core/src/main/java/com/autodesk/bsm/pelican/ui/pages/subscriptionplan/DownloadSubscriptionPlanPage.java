package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page object of Download Subscription Plan Page
 *
 * @author Sumant Manda
 */
public class DownloadSubscriptionPlanPage extends SubscriptionPlanGenericPage {

    public DownloadSubscriptionPlanPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "input-includeNew")
    private WebElement includeNewCheckbox;

    @FindBy(id = "input-includeActive")
    private WebElement includeActiveCheckbox;

    @FindBy(id = "input-includeCanceled")
    private WebElement includeCanceledCheckPlansbox;

    @FindBy(id = "input-includeCanceledOffers")
    private WebElement includeCanceledCheckOffersbox;

    @FindBy(id = "input-includeDescriptors")
    private WebElement includeDescriptorsCheckbox;

    @FindBy(id = "input-includeExpiredPrices")
    private WebElement includeExpiredPricesCheckbox;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadSubscriptionPlanPage.class.getSimpleName());

    /**
     * Navigate to the Download Subscription Plan Page URL
     */
    private void navigateToDownloadSubscriptionPlanPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION_PLAN.getForm() + "/"
            + AdminPages.DOWNLOAD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
    }

    /**
     * Method to select offering type
     */
    private void selectOfferingType(final String offeringType) {
        getActions().select(offeringTypeSelect, offeringType);
    }

    /**
     * Method to check "include new" check box if not checked
     */
    private void checkIncludeNewOfferingsCheckbox() {

        getActions().check(includeNewCheckbox);
    }

    /**
     * Method to uncheck "include new" check box if checked
     */
    private void uncheckIncludeNewOfferingsCheckbox() {

        getActions().uncheck(includeNewCheckbox);
    }

    /**
     * Method to check "include active" check box if not checked
     */
    private void checkIncludeActiveOfferingsCheckbox() {

        getActions().check(includeActiveCheckbox);
    }

    /**
     * Method to uncheck "include active" check box if checked
     */
    private void uncheckIncludeActiveOfferingsCheckbox() {

        getActions().uncheck(includeActiveCheckbox);
    }

    /**
     * Method to check "include canceled Plans" check box if not checked
     */
    private void checkIncludeCanceledPlansCheckbox() {

        getActions().check(includeCanceledCheckPlansbox);
    }

    /**
     * Method to uncheck "include canceled Plans" check box if checked
     */
    private void uncheckIncludeCanceledPlansCheckbox() {

        getActions().uncheck(includeCanceledCheckPlansbox);
    }

    /**
     * Method to check "include canceled Offers" check box if not checked
     */
    private void checkIncludeCanceledOffersCheckbox() {

        getActions().check(includeCanceledCheckOffersbox);
    }

    /**
     * Method to uncheck "include canceled offers" check box if checked
     */
    private void uncheckIncludeCanceledOffersCheckbox() {

        getActions().uncheck(includeCanceledCheckOffersbox);
    }

    /**
     * Method to check "include descriptors" check box if not checked
     */
    private void checkIncludeDescriptorsCheckbox() {

        getActions().check(includeDescriptorsCheckbox);
    }

    /**
     * Method to uncheck "include descriptors" check box if checked
     */
    private void uncheckIncludeDescriptorsCheckbox() {

        getActions().uncheck(includeDescriptorsCheckbox);
    }

    /**
     * Method to check "include expired prices" check box if not checked
     */
    private void checkIncludeExpiredPricesCheckbox() {

        getActions().check(includeExpiredPricesCheckbox);
    }

    /**
     * Method to uncheck "include expired prices" check box if checked
     */
    private void uncheckIncludeExpiredPricesCheckbox() {

        getActions().uncheck(includeExpiredPricesCheckbox);
    }

    /**
     * Method to filter download Subscription Plan and to download
     *
     * @return DownloadSubscriptionPlanPage
     */
    public DownloadSubscriptionPlanPage downloadSubscriptionPlanXlsxFile(final String productLine,
        final String offeringType, final boolean isIncludeNewSelected, final boolean isIncludeActiveSelected,
        final boolean isIncludeCanceledOffersSelected, final boolean isIncludeCanceledPlansSelected,
        final boolean isIncludeDescriptorsSelected, final boolean isIncludeExpiredPricesSelected) {

        navigateToDownloadSubscriptionPlanPage();
        if (productLine != null) {
            selectProductLine(productLine);
        }

        if (offeringType != null) {
            selectOfferingType(offeringType);
        }

        if (isIncludeNewSelected) {
            checkIncludeNewOfferingsCheckbox();
        } else {
            uncheckIncludeNewOfferingsCheckbox();
        }

        if (isIncludeActiveSelected) {
            checkIncludeActiveOfferingsCheckbox();
        } else {
            uncheckIncludeActiveOfferingsCheckbox();
        }

        if (isIncludeCanceledOffersSelected) {
            checkIncludeCanceledOffersCheckbox();
        } else {
            uncheckIncludeCanceledOffersCheckbox();
        }

        if (isIncludeCanceledPlansSelected) {
            checkIncludeCanceledPlansCheckbox();
        } else {
            uncheckIncludeCanceledPlansCheckbox();
        }

        if (isIncludeDescriptorsSelected) {
            checkIncludeDescriptorsCheckbox();
        } else {
            uncheckIncludeDescriptorsCheckbox();
        }

        if (isIncludeExpiredPricesSelected) {
            checkIncludeExpiredPricesCheckbox();
        } else {
            uncheckIncludeExpiredPricesCheckbox();
        }
        submit();
        return super.getPage(DownloadSubscriptionPlanPage.class);
    }
}
