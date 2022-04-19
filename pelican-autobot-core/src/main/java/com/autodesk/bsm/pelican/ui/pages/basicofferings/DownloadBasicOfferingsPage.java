package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page object of Download Basic Offerings Page
 *
 * @author Shweta Hegde
 */
public class DownloadBasicOfferingsPage extends GenericDetails {

    public DownloadBasicOfferingsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    @FindBy(id = "appFamilyId")
    private WebElement applicationFamilyIdSelect;

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "input-includeNew")
    private WebElement includeNewCheckbox;

    @FindBy(id = "input-includeActive")
    private WebElement includeActiveCheckbox;

    @FindBy(id = "input-includeCanceled")
    private WebElement includeCanceledCheckbox;

    @FindBy(id = "input-includeDescriptors")
    private WebElement includeDescriptorsCheckbox;

    @FindBy(id = "input-includeExpiredPrices")
    private WebElement includeExpiredPricesCheckbox;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadBasicOfferingsPage.class.getSimpleName());

    /**
     * Navigate to the Download Basic Offering Page URL
     */
    private void navigateToDownloadBasicOfferingPage() {
        @SuppressWarnings("static-access")
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BASIC_OFFERINGS.getForm() + "/"
            + AdminPages.DOWNLOAD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
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
     * Method to check "include canceled" check box if not checked
     */
    private void checkIncludeCanceledOfferingsCheckbox() {

        getActions().check(includeCanceledCheckbox);
    }

    /**
     * Method to uncheck "include canceled" check box if checked
     */
    private void uncheckIncludeCanceledOfferingsCheckbox() {

        getActions().uncheck(includeCanceledCheckbox);
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
     * Method to filter download basic offerings and to download
     *
     * @return DownloadBasicOfferingsPage
     */
    public DownloadBasicOfferingsPage downloadBasicOfferingsXlsxFile(final String productLine,
        final String offeringType, final boolean isIncludeNewSelected, final boolean isIncludeActiveSelected,
        final boolean isIncludeCanceledSelected, final boolean isIncludeDescriptorsSelected,
        final boolean isIncludeExpiredPricesSelected) {

        navigateToDownloadBasicOfferingPage();
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

        if (isIncludeCanceledSelected) {
            checkIncludeCanceledOfferingsCheckbox();
        } else {
            uncheckIncludeCanceledOfferingsCheckbox();
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
        submit(TimeConstants.THREE_SEC);
        return super.getPage(DownloadBasicOfferingsPage.class);
    }
}
