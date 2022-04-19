package com.autodesk.bsm.pelican.ui.pages.configuration;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is for Banking Configuration Page.
 */
public class BankingConfigurationPropertiesPage extends GenericDetails {

    @FindBy(id = "subnav-link-bankingConfig-set")
    private WebElement setBankingProperties;

    @FindBy(xpath = "//*[@id=\"subnav\"]/ul/li[4]/span")
    private WebElement configurationXpath;

    @FindBy(className = "delete-button")
    private WebElement deleteProperty;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(BankingConfigurationPropertiesPage.class.getSimpleName());

    public BankingConfigurationPropertiesPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Navigate to Edit Properties page in Admin Tools
     */
    public BankingConfigurationPropertySetPage editProperties(final String key) {

        getDriver().findElement(By.cssSelector("a[href*='" + key + "']")).click();
        return super.getPage(BankingConfigurationPropertySetPage.class);
    }

    /**
     * Verify if Set Property is displayed under Configurations Drop down
     *
     * @return Boolean true of false based on visibility
     */
    public Boolean isSetPropertyVisibleInConfigurationDropdown() {
        configurationXpath.click();

        Boolean elementFound = true;
        try {
            setBankingProperties.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException se) {
            elementFound = false;
        }
        return elementFound;
    }

    /**
     * Method to check if Remove Property is Visible or Hidden, via Boolean variable
     *
     * @return Boolean true or false based on visibility
     */
    public Boolean isRemovePropertyButtonVisible() {
        Boolean elementFound = true;
        try {
            deleteProperty.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException se) {
            elementFound = false;
        }
        return elementFound;
    }

    /**
     * Method to check if Add Property is Visible or Hidden, via Boolean variable
     *
     * @return Boolean true or false based on visibility
     */
    public Boolean isAddPropertyButtonVisible() {

        Boolean elementFound = true;
        try {
            submitButton.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException se) {
            elementFound = false;
        }
        return elementFound;

    }

    /**
     * This method selects the feature flag which has to be edited
     *
     * @return FeatureFlagSetPage
     */
    private FeatureFlagSetPage selectTheFeatureFlag(final String featureFlag) {

        final WebElement featureFlagText = getDriver().findElement(By.linkText(featureFlag));
        featureFlagText.click();
        LOGGER.info("Selected the feature flag '" + featureFlag + "' to edit");
        Util.waitInSeconds(0L);
        return super.getPage(FeatureFlagSetPage.class);
    }

    /**
     * Method to set feature flag to true or false Method receives feature flag name and value and updates the feature
     * flag.
     *
     * It is a util method, which helps reduce code in many test classes. Because of which it returns boolean, instead
     * of any page.
     *
     * @param featureFlagName
     * @param isTrue
     * @return boolean
     */
    public boolean setFeatureFlag(final String featureFlagName, final boolean isTrue) {
        final FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage =
            super.getPage(FindBankingConfigurationPropertiesPage.class);
        findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.FEATURE_FLAG);
        final FeatureFlagSetPage featureFlagSetPage = selectTheFeatureFlag(featureFlagName);

        if (isTrue) {
            return featureFlagSetPage.editValue(PelicanConstants.TRUE);
        } else {
            return featureFlagSetPage.editValue(PelicanConstants.FALSE);
        }
    }

    /**
     * Method to get selected feature value for a specific feature.
     *
     * @param featureFlagName
     * @return
     */
    public boolean getFeatureFlag(final String featureFlagName) {
        final FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage =
            super.getPage(FindBankingConfigurationPropertiesPage.class);
        findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.FEATURE_FLAG);
        final FeatureFlagSetPage featureFlagSetPage = selectTheFeatureFlag(featureFlagName);
        return featureFlagSetPage.getSelectedFeatureFlagValue();
    }

    /**
     * Method to check whether given key exists in the page
     *
     * @param key
     * @return boolean
     */
    public boolean isRequiredKeyDisplayed(final String key) {

        boolean keyDisplayed = false;
        try {
            final WebElement keyLink = driver.findElement(By.partialLinkText(key));
            keyDisplayed = keyLink.isDisplayed();
        } catch (ElementNotFoundException | NoSuchElementException e) {
            LOGGER.error(key + " element not found in this page");
        }

        return keyDisplayed;
    }

}
