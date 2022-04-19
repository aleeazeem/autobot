package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for Add Feature under Catalog in the admin tool
 *
 * @author vineel
 */
public class AddFeaturePage extends GenericDetails {

    public AddFeaturePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "appId")
    private WebElement appIdSelect;

    @FindBy(id = "type")
    private WebElement featureTypeSelect;

    @FindBy(id = "input-name")
    private WebElement featureNameInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddFeaturePage.class.getSimpleName());

    /**
     * Navigate to the Add Feature Page
     */
    private void navigateToAddFeaturePage() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.FEATURE.getForm() + "/" + AdminPages.ADD_FORM.getForm();
        getDriver().get(url);
        Wait.pageLoads(getDriver());
    }

    /**
     * This method will add the feature in the admin tool.
     *
     * @param featureType
     * @param featureName
     * @param featureExternalKey
     * @param featureStatus
     * @param featureStatusSelect TODO
     */
    public void addFeature(final String featureType, final String featureName, final String featureExternalKey,
        final String featureStatus) {
        navigateToAddFeaturePage();
        selectApplication(getEnvironment().getApplicationDescription());
        selectFeatureType(featureType);
        setFeatureName(featureName);
        setExternalKey(featureExternalKey);
        selectFeatureStatus(featureStatus);
    }

    /**
     * This method will select the feature type in the add feature page
     */
    private void selectFeatureType(final String featureType) {
        try {
            getActions().select(featureTypeSelect, featureType);
            LOGGER.info("Select the feature type" + featureType);
        } catch (final Exception ex) {
            LOGGER.info("Unable to select the feature type in the add features page");

        }

    }

    /**
     * This method will set the feature name in the add feature page
     */
    private void setFeatureName(final String featureName) {
        try {
            getActions().setText(featureNameInput, featureName);
            LOGGER.info("Set the feature name to " + featureName);
        } catch (final Exception ex) {
            LOGGER.info("Unable to set the feature name in the add features page");
        }
    }

    /**
     * This method will select the feature status from the drop down in the admin tool.
     *
     * @param featureStatus
     */
    private void selectFeatureStatus(final String featureStatus) {
        if (featureStatus != null) {
            if (featureStatus.equals(PelicanConstants.YES) || featureStatus.equals(PelicanConstants.NO)) {
                getActions().select(activeSelect, featureStatus);
            }
        }
    }

    /**
     * Click on the save button of the feature which is inherited from Generic Page This method is written so that any
     * test class can use this
     *
     * @return FeatureDetailPage
     */
    public FeatureDetailPage clickOnSave() {
        submit();
        return super.getPage(FeatureDetailPage.class);
    }

    /**
     * Click on the cancel button of the feature which is inherited from Generic Page This method is written so that any
     * test class can use this
     */
    public void clickOnCancel() {
        cancel();
    }
}
