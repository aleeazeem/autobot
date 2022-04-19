package com.autodesk.bsm.pelican.ui.generic;

import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PelicanActions {

    private WebDriver driver;
    private static final Logger LOGGER = LoggerFactory.getLogger(PelicanActions.class.getSimpleName());

    public PelicanActions(final WebDriver driver) {
        this.driver = driver;
    }

    private PelicanActions() {

    }

    public void setText(final WebElement element, final String value) {

        Wait.elementDisplayed(driver, element);
        if (value != null) {
            element.clear();
            element.sendKeys(value);
        }
    }

    public String getText(final WebElement element) {

        Wait.elementDisplayed(driver, element);
        return element.getText();
    }

    public void waitForElementDisplayed(final WebElement element) {
        Wait.elementDisplayed(driver, element);
    }

    public void check(final WebElement element) {
        Wait.elementVisibile(driver, element);
        if (!element.isSelected()) {
            element.click();
        }
    }

    public void uncheck(final WebElement element) {
        Wait.elementVisibile(driver, element);
        if (element.isSelected()) {
            element.click();
        }
    }

    public void select(final WebElement element, final String text) {
        Wait.elementEnabled(driver, element);
        Wait.dropdownPopulatedWithValue(driver, element, text);
    }

    public void selectWithElementText(final WebElement element, final String text) {
        Wait.dropdownPopulatedWithValue(driver, element, text);
    }

    public void selectByIndex(final WebElement element, final int index) {
        new Select(element).selectByIndex(index);
    }

    /**
     * This utility method will help you to select the option from dropdown using the value attribute of the option
     */
    public void selectByValue(final WebElement element, final String value) {
        new Select(element).selectByValue(value);

    }

    public void click(final WebElement element) {
        try {
            LOGGER.info(element.getTagName() + " : " + Wait.elementClickable(driver, element));
            element.click();
        } catch (final TimeoutException te) {
            LOGGER.error("Failed to Click on WebElement: " + element.getTagName());
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
    }

}
