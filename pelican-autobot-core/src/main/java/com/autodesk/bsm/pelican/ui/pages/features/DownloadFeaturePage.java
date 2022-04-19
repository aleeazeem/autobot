package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadFeaturePage extends GenericDetails {

    public DownloadFeaturePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "typeId")
    private WebElement featureTypeDropdown;

    @FindBy(id = "appFamilyId")
    private WebElement applicationFamilyIdDropdown;

    @FindBy(id = "appId")
    private WebElement applicationIdDropdown;

    @FindBy(id = "isActive")
    private WebElement isActiveDropdown;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadFeaturePage.class.getSimpleName());

    /**
     * Navigate to the Download Features Page URL
     */
    public void navigateToDownloadFeaturesPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.FEATURE.getForm() + "/"
            + AdminPages.DOWNLOAD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(getDriver());
    }

    /**
     * Method to select the feature type dropdown from the download features page
     */
    private void selectFeatureType(final String featureType) {
        getActions().select(featureTypeDropdown, featureType);
    }

    /**
     * Method to select the is active dropdown from the download features page
     */
    private void selectIsActive(final String isActive) {
        getActions().select(isActiveDropdown, isActive);
    }

    /**
     * Method to download the features xlsx file in the admin tool
     *
     * @param isActive TODO
     */
    public void downloadFeaturesXlsxFile(final String featureType, final String isActive) {
        navigateToDownloadFeaturesPage();
        selectApplication(environmentVariables.getApplicationDescription());
        if (featureType != null) {
            if (!featureType.isEmpty()) {
                selectFeatureType(featureType);
            }
        }

        if (isActive != null) {
            if (!isActive.isEmpty()) {
                selectIsActive(isActive);
            }
        }
        submit();
    }

    /**
     * Method to return the title of the download features page
     *
     * @return Download Features Page Title
     */
    public String getPageTitle() {
        return (getTitle());
    }

    /**
     * Method to return whether the application family field is present on the download features page
     *
     * @return boolean value of the application family field
     */
    public boolean isApplicationFamilyFieldPresent() {
        Wait.elementVisibile(driver, applicationFamilyIdDropdown);
        return (applicationFamilyIdDropdown.isDisplayed());
    }

    /**
     * Method to return whether the application field is present on the download features page
     *
     * @return boolean value of the application field
     */
    public boolean isApplicationFieldPresent() {
        Wait.elementVisibile(driver, applicationIdDropdown);
        return (applicationIdDropdown.isDisplayed());
    }

    /**
     * Method to return whether the feature type field is present on the download features page
     *
     * @return boolean value of the featuretype field
     */
    public boolean isFeatureTypeFieldPresent() {
        Wait.elementVisibile(driver, featureTypeDropdown);
        return (featureTypeDropdown.isDisplayed());
    }

    /**
     * Method to return whether the is active field is present on the download features page.
     *
     * @return boolean value of the is active field.
     */
    public boolean isActiveFieldPresent() {
        Wait.elementVisibile(driver, isActiveDropdown);
        return (isActiveDropdown.isDisplayed());
    }
}
