package com.autodesk.bsm.pelican.ui.pages.catalog;

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
 * Page Object model for Add Feature Type under Catalog in the admin tool
 *
 * @author vineel
 */
public class AddFeatureTypePage extends GenericDetails {

    public AddFeatureTypePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "appId")
    private WebElement appIdSelect;

    @FindBy(id = "input-name")
    private WebElement featureTypeNameInput;

    @FindBy(name = "addItemType")
    private WebElement addFeatureTypeButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddFeatureTypePage.class.getSimpleName());

    /**
     * Navigate to the Add Feature Type Page
     */
    private void navigateToAddFeatureTypePage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.FEATURE_TYPE.getForm() + "/"
            + AdminPages.ADD_FORM.getForm();
        getDriver().get(url);
        Wait.pageLoads(getDriver());
    }

    /**
     * This method will add the feature type in the admin tool
     */
    public void addFeatureType(final String applicationFamily, final String application, final String featureTypeName,
        final String featureTypeExternalKey) {
        navigateToAddFeatureTypePage();
        selectApplicationFamily(applicationFamily);
        selectApplication(application);
        setFeatureTypeName(featureTypeName);
        setExternalKey(featureTypeExternalKey);
    }

    /**
     * This method will select the application in the add feature type page
     */
    public void selectApplication(final String application) {
        try {
            getActions().select(appIdSelect, application);
            LOGGER.info("Select the application" + application);
        } catch (final Exception ex) {
            LOGGER.info("Unable to select the application in the add feature type page");

        }

    }

    /**
     * This method will set the feature type name in the add feature type page
     */
    private void setFeatureTypeName(final String featureTypeName) {
        try {
            getActions().setText(featureTypeNameInput, featureTypeName);
            LOGGER.info("Set the feature type name to " + featureTypeName);
        } catch (final Exception ex) {
            LOGGER.info("Unable to set the feature type name in the add feature type page");

        }

    }

    /**
     * This method will click on the add feature type button in the add feature type page
     */
    public FeatureTypeDetailPage clickOnAddFeatureTypeButton() {
        try {
            getActions().click(addFeatureTypeButton);
            LOGGER.info("Clicking on the add feature type button");
        } catch (final Exception ex) {
            LOGGER.info("Unable to find and click on the add feature type button");

        }
        return super.getPage(FeatureTypeDetailPage.class);
    }

}
