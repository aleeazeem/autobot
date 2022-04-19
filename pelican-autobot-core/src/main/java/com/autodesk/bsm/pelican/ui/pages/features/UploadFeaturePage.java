package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Admin Tool's Upload Catalog tests. On Admin Tool's Main Tab navigate to Catalog -> Items ->Upload Items Upload
 * Catalog page is common for Upload Items, Upload Item Group ,Upload Item Types
 *
 * @author sunitha
 */

public class UploadFeaturePage extends GenericDetails {
    public UploadFeaturePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Select App family
    @FindBy(id = "appFamilyId")
    private WebElement appFamilyIdSelect;

    // Find Error messages
    @FindBy(xpath = "//span[@class='error-message']")
    private WebElement errorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFeaturePage.class.getSimpleName());

    // Method to select App Family
    public void selectApplicationFamily(final String appFamilyName) {
        LOGGER.info("Select application family to '" + appFamilyName + "'");
        getActions().select(appFamilyIdSelect, appFamilyName);
    }

    // Method to get Error Messages
    public String getH3ErrorMessage() {
        final String errorMessage = this.errorMessage.getText();
        LOGGER.info("Error Message is '" + errorMessage + "'");
        return errorMessage;
    }

    /**
     * Method to Navigate upload Feature file.
     */
    public void navigateToFeatureUploadPage() {
        final String catalogUploadForm = getEnvironment().getAdminUrl() + "/feature/importForm";
        if (!isPageValid(catalogUploadForm)) {
            getDriver().get(catalogUploadForm);
        }
        Wait.pageLoads(driver);
    }

}
