package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Admin Tool's Upload Basic Offerings - On Admin Tool's Main Tab navigate to Catalog->Basic Offerings
 * -> Upload
 *
 * @author Sunitha
 */

public class UploadBasicOfferingsPage extends GenericDetails {
    public UploadBasicOfferingsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Select App family
    @FindBy(id = "appFamilyId")
    private WebElement appFamilyIdSelect;

    // Find Error message
    @FindBy(xpath = "//div/p[@class='errors']")
    private WebElement errorMessage;

    // @FindBy(xpath = "//div/p[@class='error-message']")
    @FindBy(xpath = "//*[@id=\"field-file\"]/span[2]")
    private WebElement uploaderrorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadBasicOfferingsPage.class.getSimpleName());

    // Method to select App family
    public void selectApplicationFamily(final String appFamilyName) {
        LOGGER.info("Select application family to '" + appFamilyName + "'");
        getActions().select(appFamilyIdSelect, appFamilyName);
    }

    // Method to get Error messages
    public String getH3ErrorMessage() {
        final String errorMessage = this.errorMessage.getText();
        LOGGER.info("Error Message is '" + errorMessage + "'");
        return errorMessage;
    }

    // Method to get Error messages
    public String getUploadErrorMessage() {
        final String uploaderrorMessage = this.uploaderrorMessage.getText();
        LOGGER.info("Error Message is '" + uploaderrorMessage + "'");
        return uploaderrorMessage;
    }

    /**
     * Method to navigate to Basic Offerings Import form
     */
    public void navigateToBasicOfferingUploadPage() {
        final String basicOfferingImportForm = getEnvironment().getAdminUrl() + "/offering/importForm";
        if (!isPageValid(basicOfferingImportForm)) {
            getDriver().get(basicOfferingImportForm);
        }
    }

}
