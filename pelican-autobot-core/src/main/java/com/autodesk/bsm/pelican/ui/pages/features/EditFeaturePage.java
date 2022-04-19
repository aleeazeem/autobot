package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for Edit Feature under Catalog in the admin tool
 *
 * @author t_mohag
 */
public class EditFeaturePage extends GenericDetails {

    public EditFeaturePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "type")
    private WebElement featureTypeSelect;

    @FindBy(id = "input-name")
    private WebElement featureNameInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditFeaturePage.class.getSimpleName());

    /**
     * This method will select the feature type in the edit feature page
     */
    private void selectFeatureType(final String featureType) {
        if (featureType != null) {
            try {
                getActions().select(featureTypeSelect, featureType);
                LOGGER.info("Select the feature type" + featureType);
            } catch (final Exception ex) {
                LOGGER.info("Unable to select the feature type in the edit feature page");

            }
        }
    }

    /**
     * This method will set the feature name in the edit feature page
     */
    public void setFeatureName(final String featureName) {
        if (featureName != null) {
            try {
                getActions().setText(featureNameInput, featureName);
                LOGGER.info("Set the feature name to " + featureName);
            } catch (final Exception ex) {
                LOGGER.info("Unable to set the feature name in the edit feature page");
            }
        }
    }

    /**
     * This is the method to edit the feature name, ext key, feature type and status.
     *
     * @param name
     * @param externalKey
     * @param featureType
     * @param featureStatus
     */
    public void editFeature(final String name, final String externalKey, final String featureType,
        final String featureStatus) {
        setExternalKey(externalKey);
        setFeatureName(name);
        selectFeatureType(featureType);
        selectFeatureStatus(featureStatus);
    }

    /**
     * This is the methods to select the feature status.
     *
     * @param featureStatus
     */
    private void selectFeatureStatus(final String featureStatus) {
        if (featureStatus != null) {
            if (featureStatus.equals(PelicanConstants.YES)) {
                getActions().select(activeSelect, featureStatus);
            } else if (featureStatus.equals(PelicanConstants.NO)) {
                getActions().select(activeSelect, featureStatus);
                switchDriverControlToPopUp();
                getActions().click(confirmButton);
                switchDriverControlToParentWindow();
            }
        }
    }

    /**
     * Click on the 'Update Feature' button to save the updated feature details
     *
     * @return {@link FeatureDetailPage}
     */
    public FeatureDetailPage clickOnUpdateFeatureButton() {
        LOGGER.info("Click Update Feature button");
        submit();
        return super.getPage(FeatureDetailPage.class);
    }
}
