package com.autodesk.bsm.pelican.ui.pages.catalog;

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
 * Page Object for Find Feature Type under the Catalog menu
 *
 * @author vineel
 */
public class FindFeatureTypePage extends GenericDetails {

    public FindFeatureTypePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-pattern")
    private WebElement featureTypeNameInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindFeatureTypePage.class.getSimpleName());

    /**
     * Navigate to the Find Feature Type Page URL
     */
    private void navigateToFindFeatureTypePage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.FEATURE_TYPE.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);

    }

    /**
     * This is a method which will find the feature type by feature type name
     *
     * @param featureType name
     * @return Generic Grid
     */
    public FeatureTypeSearchResultsPage findFeatureTypeByName(final String featureTypeName) {
        navigateToFindFeatureTypePage();
        selectApplication(environmentVariables.getApplicationDescription());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        setFeatureTypeName(featureTypeName);
        submit(0);
        return super.getPage(FeatureTypeSearchResultsPage.class);
    }

    /**
     * This method will set the feature type name
     *
     * @param feature type name
     */
    private void setFeatureTypeName(final String featureTypeName) {
        getActions().setText(featureTypeNameInput, featureTypeName);
    }
}
