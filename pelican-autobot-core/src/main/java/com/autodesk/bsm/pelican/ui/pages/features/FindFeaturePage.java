package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for Find Feature Page under Catalog menu
 *
 * @author vineel
 */
public class FindFeaturePage extends GenericDetails {

    public FindFeaturePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = "#bd > div.form-group > div.form-group-labels > h3:nth-child(2) > span")
    private WebElement findByExternalKey;

    @FindBy(css = "#bd > div.form-group > div.form-group-labels > h3:nth-child(3) > span")
    private WebElement findByAdvancedTab;

    @FindBy(xpath = ".//*[@id='form-advancedFindForm']//*[@id='input-pattern']")
    private WebElement nameInputInAdvancedFindTab;

    @FindBy(xpath = ".//*[@id='form-advancedFindForm']//*[@class='input']//*[@name='type']")
    private WebElement featureTypeInAdvancedFindTab;

    @FindBy(id = "externalKeyAppId")
    private WebElement applicationIdSelect;

    @FindBy(id = "input-includeInactive")
    private WebElement includeInActiveFeaturesCheckbox;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindFeaturePage.class.getSimpleName());

    /**
     * Navigate to the Find Features Page URL
     */
    private void navigateToFindFeaturesPage() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.FEATURE.getForm() + "/" + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);

    }

    /**
     * This is a method which will find the feature by feature id
     */
    public FeatureDetailPage findFeatureById(final String featureId) {
        navigateToFindFeaturesPage();
        setId(featureId);
        submit();
        return super.getPage(FeatureDetailPage.class);
    }

    /**
     * This is a method which will find the feature by empty feature id
     */
    public FeatureSearchResultsPage findFeatureByEmptyId() {
        navigateToFindFeaturesPage();
        setId(PelicanConstants.EMPTY_STRING);
        submit();
        return super.getPage(FeatureSearchResultsPage.class);
    }

    /**
     * This is a method which will find the feature by feature external key
     *
     * @param feature external key
     */
    public FeatureDetailPage findFeatureByExternalKey(final String featureExternalKey) {
        navigateToFindFeaturesPage();
        getActions().click(findByExternalKey);
        selectApplication(environmentVariables.getApplicationDescription());
        setExternalKey(featureExternalKey);
        submit(1);
        return super.getPage(FeatureDetailPage.class);
    }

    /**
     * This method will set the feature name and feature type in the advanced find tab
     *
     * @return GenericGrid
     */
    public GenericGrid findFeatureByAdvancedFind(final String featureName, final String featureType) {
        navigateToFindFeaturesPage();
        getActions().click(findByAdvancedTab);
        selectApplication(environmentVariables.getApplicationDescription());
        if (featureName != null) {
            setFeatureNameInAdvancedFindTab(featureName);
        }
        if (featureType != null) {
            setFeatureTypeInAdvancedFindTab(featureType);
        }
        submit(2);
        return super.getPage(GenericGrid.class);

    }

    /**
     * This method will set the feature name and feature type in the advanced find tab
     *
     * @param checkbox
     * @return GenericGrid
     */
    public FeatureSearchResultsPage findFeatureByAdvancedFind(final boolean checkbox) {
        navigateToFindFeaturesPage();
        getActions().click(findByAdvancedTab);
        selectApplication(environmentVariables.getApplicationDescription());
        if (!checkbox) {
            uncheckCheckbox();
        }
        submit(2);
        return super.getPage(FeatureSearchResultsPage.class);

    }

    /**
     * This method does Advanced find and click on the required row and return Feature Detail Page
     *
     * @return FeatureDetailPage
     */
    public FeatureDetailPage selectResultRowWithAdvancedFind(final int row, final String featureName,
        final String featureType) {

        final GenericGrid genericGrid = findFeatureByAdvancedFind(featureName, featureType);
        genericGrid.selectResultRow(row);
        return super.getPage(FeatureDetailPage.class);
    }

    /**
     * This method will set the feature name in the feature advanced find tab
     */
    private void setFeatureNameInAdvancedFindTab(final String featureName) {
        getActions().setText(nameInputInAdvancedFindTab, featureName);
    }

    /**
     * This method will select the feature type in the feature advanced find tab
     */
    private void setFeatureTypeInAdvancedFindTab(final String featureType) {
        getActions().select(featureTypeInAdvancedFindTab, featureType);
    }

    /**
     * This is the method to show the advanced find in features tab.
     */
    public void showAdvancedFindTab() {
        navigateToFindFeaturesPage();
        findByAdvancedTab.click();
    }

    /**
     * This is a method to return whether include inactive features checkbox is present.
     *
     * @return true or false
     */
    public boolean isIncludeInActiveFeaturesCheckboxPresent() {
        return (isElementPresent(includeInActiveFeaturesCheckbox));
    }

    /**
     * This method selects the application
     */
    @Override
    public void selectApplication(final String application) {
        try {
            getActions().select(applicationIdSelect, application);
            LOGGER.info("Select the application" + application);
        } catch (final Exception ex) {
            LOGGER.info("Unable to select the application.");

        }

    }

    /**
     * This is a method to uncheck the include inactive features checkbox
     */
    private void uncheckCheckbox() {
        Wait.elementClickable(driver, includeInActiveFeaturesCheckbox);
        includeInActiveFeaturesCheckbox.click();
    }
}
