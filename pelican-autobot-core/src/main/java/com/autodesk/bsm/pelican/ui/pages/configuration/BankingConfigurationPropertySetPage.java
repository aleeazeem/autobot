package com.autodesk.bsm.pelican.ui.pages.configuration;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is reserved for adding/editing banking configuration property values
 *
 * @author Shweta Hegde
 */
public class BankingConfigurationPropertySetPage extends GenericDetails {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(BankingConfigurationPropertySetPage.class.getSimpleName());

    @FindBy(className = "value")
    private WebElement keyInEditPage;

    @FindBy(id = "input-value")
    private WebElement valueInEditPage;

    @FindBy(xpath = "//button[text()='Set Property']")
    private WebElement setPropertyButton;

    public BankingConfigurationPropertySetPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Check if Key webelement is displayed or not
     *
     * @return If visible True, else false
     */
    public boolean isKeyDisplayed() {

        Boolean elementFound = true;
        try {
            LOGGER.info("Value WebElement is : " + keyInEditPage.isDisplayed());
        } catch (NoSuchElementException | StaleElementReferenceException se) {
            elementFound = false;
        }

        getDriver().navigate().back();
        return elementFound;

    }

    /**
     * Get the type of the value, whether it is a password or text
     *
     * @return type
     */
    public String getSecretFieldAttribute() {

        String attribute = null;
        try {

            attribute = valueInEditPage.getAttribute("type");
            LOGGER.info("Value : " + attribute);
            getDriver().navigate().back();
        } catch (NoSuchElementException | StaleElementReferenceException se) {
            se.printStackTrace();
        }
        return attribute;
    }

    /**
     * Edit Properties by setting the value
     */
    public void setProperties(final String key, final String value) {

        valueInEditPage.clear();
        valueInEditPage.sendKeys(value);
        setPropertyButton.click();
        LOGGER.info("Setting value of " + key + " to " + value);
    }
}
