package com.autodesk.bsm.pelican.util;

import com.google.common.base.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Introducing wait mechanism on the page, for the control to wait till elements appear on page or certain conditions
 * are fulfilled before any action is performed
 *
 * a. The WebDriver wait uses default polling of 500ms and can be changed by passing the value in .polling condition
 *
 * b. WebDriver wait will ignore instances of NotFoundException that are encountered (thrown) by default in the 'until'
 * condition, and immediately propagate all others. You can add more to the ignore list by calling ignoring(exceptions
 * to add).
 *
 *
 *
 * @author Sheenam
 * @version 1.0.0
 */

public final class Wait {

    // class variables to be used for specific functionality
    private static final Logger LOGGER = LoggerFactory.getLogger(Wait.class.getSimpleName());
    private static Long timeOutInSecs;
    private static Long pollingTime;
    private static TimeUnit timeUnit = TimeUnit.SECONDS;
    private static final String JS_PAGESTATE = "return document.readyState";
    private static final String JS_PAGESTATE_COMPLETE = "complete";
    private static WebDriverWait webDriverWait = null;

    static {
        timeOutInSecs = Long.valueOf(PelicanEnvironment.getValueFromProperty("totalWaitTime"));
        pollingTime = Long.valueOf(PelicanEnvironment.getValueFromProperty("pollingTime"));
    }

    /**
     * This method will wait till the element is visible on the page as per time provided and will also throw exception
     * when element is not found.
     *
     * @param driver - takes WebDriver instance as parameter
     * @param elementToBeLoaded - takes WebElement reference which is to be validated on the page
     * @return return a boolean value as true if it finds the element within specified time else returns false
     */
    public static boolean elementVisibile(final WebDriver driver, final WebElement elementToBeLoaded) {
        try {
            getWebDriverWait(driver).until(ExpectedConditions.visibilityOf(elementToBeLoaded));
            LOGGER.debug(String.format("elementVisibile, webElement=%s ", elementToBeLoaded));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
            return true;
        }

    }

    /**
     * This method will wait till the element is enabled on the page as per time provided, and will also throw exception
     * when element is not found.
     *
     * @param driver - takes WebDriver instance as parameter
     * @param element - takes WebElement reference which is to be validated on the page
     * @return return a boolean value as true if it passed element is enabled within specified time else returns false
     */
    public static boolean elementEnabled(final WebDriver driver, final WebElement element) {
        elementVisibile(driver, element);
        try {
            getWebDriverWait(driver).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(final WebDriver d) {
                    return element.isEnabled();
                }

                public boolean test(final WebDriver input) {
                    return apply(input);
                }
            });
            LOGGER.debug(String.format("elementEnabled, webElement=%s", element));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait till all the elements on the page are located as per the time provided and will also throw
     * exception when element is not found.
     *
     * @param driver - WebDriver instance
     * @param list of elements on the page to be checked
     * @return return a boolean value as true if it finds the element within specified time else returns false
     */
    public static boolean allElementsVisible(final WebDriver driver, final List<WebElement> elements) {
        try {
            getWebDriverWait(driver).until(ExpectedConditions.visibilityOfAllElements(elements));
            LOGGER.debug(String.format("allElementsVisible, webElement=%s ", elements));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }

    }

    /**
     * This method will wait till the element is click-able on the page as per time provided
     *
     * @param driver - takes WebDriver instance as parameter
     * @param elementToBeLoaded - takes WebElement reference which is to be validated on the page
     * @return return a boolean value as true if it finds the element within specified time else returns false
     */
    public static boolean elementClickable(final WebDriver driver, final WebElement elementToBeLoaded) {

        try {
            elementDisplayed(driver, elementToBeLoaded);
            getWebDriverWait(driver).ignoring(StaleElementReferenceException.class);
            getWebDriverWait(driver).until(ExpectedConditions.elementToBeClickable(elementToBeLoaded));
            LOGGER.debug(String.format("elementClickable, webElement=%s, ", elementToBeLoaded));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait till the provided text is present in the element
     *
     * @param driver - takes WebDriver instance as parameter
     * @param elementToBeLoaded - takes WebElement reference which is to be validated on the page
     * @param text - given text is present in the specified element
     * @return return a boolean value as true if it finds the element within specified time else returns false
     */
    public static boolean textPresentInElement(final WebDriver driver, final WebElement elementToBeLoaded,
        final String text) {

        try {
            getWebDriverWait(driver).until(ExpectedConditions.textToBePresentInElement(elementToBeLoaded, text));
            LOGGER.debug(
                String.format("textPresentInElement, webElement=%s, textToBeVerified=%s", elementToBeLoaded, text));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait for an alert box to appear on page. This alert box is not dependent on any web element but
     * appears on page after certain action. e.g. clicking a checkbox
     *
     * @param driver
     */
    public static boolean alertAppears(final WebDriver driver) {

        try {
            getWebDriverWait(driver).until(ExpectedConditions.alertIsPresent());
            LOGGER.debug(String.format("alertAppears,  maxTimeOutInSecs=%s ", timeOutInSecs));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait for a page with specific Title.
     *
     * @param driver
     * @param title
     */
    public static boolean pageTitle(final WebDriver driver, final String title) {

        try {
            getWebDriverWait(driver).until(ExpectedConditions.titleIs(title));
            LOGGER.debug(String.format("pageTitle, pageTitle=%s ", title));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method waits for a frame to be available and then as soon as the frame is available, the control switches to
     * it automatically.
     *
     * @param driver
     * @param frameLocator: used to find the frame (id or name)
     */
    public static boolean switchWhenFrameIsAvailable(final WebDriver driver, final String frameLocator) {

        try {
            getWebDriverWait(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
            LOGGER.debug(String.format("switchWhenFrameIsAvailable, frameLocator=%s ", frameLocator));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait till the specified text is present in the element's value attribute
     *
     * @param driver
     * @param elementToBeLoaded
     * @param textInValue
     */
    public static boolean textPresentInElementValue(final WebDriver driver, final WebElement elementToBeLoaded,
        final String textInValue) {

        try {
            getWebDriverWait(driver)
                .until(ExpectedConditions.textToBePresentInElementValue(elementToBeLoaded, textInValue));
            LOGGER.debug(String.format("textPresentInElementValue, webElement=%s, textValueToBeVerified=%s ",
                elementToBeLoaded, textInValue));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait till the specified text is present in the element's value attribute
     *
     * @param driver
     * @param elementToBeLoaded
     * @param textInValue
     */
    public static boolean textToBePresentInElementLocated(final WebDriver driver, final By locator, final String text) {

        try {
            getWebDriverWait(driver).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    /**
     * This method will wait till the specified text is present in the element's value attribute
     *
     * @param driver
     *
     */
    public static boolean pageLoads(final WebDriver driver) {

        try {
            getWebDriverWait(driver).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(final WebDriver driver) {
                    return ((JavascriptExecutor) driver).executeScript(JS_PAGESTATE).equals(JS_PAGESTATE_COMPLETE);
                }

                public boolean test(final WebDriver driver) {
                    return apply(driver);
                }
            });
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
            return true;
        }
        LOGGER.debug(String.format("pageLoads,  maxTimeOutInSecs=%s ", timeOutInSecs));
        return true;
    }

    /**
     * This method waits until passed WebElement is actually visible in ViewPort It will be used when you want to check
     * for webelement on the page without performing any action like scrolling on the page till element is displayed
     *
     * @param driver
     * @param webElement
     */
    public static boolean elementDisplayed(final WebDriver driver, final WebElement webElement) {

        try {
            getWebDriverWait(driver).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(final WebDriver d) {
                    try {
                        // Sending a non-action key. SendKeys throws WebDriverException if WebElement is not visible
                        webElement.sendKeys(Keys.SHIFT);
                        return true;
                    } catch (final WebDriverException e) {
                        return false;
                    }
                }

                public boolean test(final WebDriver driver) {
                    return apply(driver);
                }
            });
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
            return true;
        }
        LOGGER.debug(String.format("elementDisplayedAfterScroll, webElement=%s", webElement));
        return true;
    }

    /**
     * This method waits till the dropdown list is populated and selects the desired value in the dropdown
     *
     * @param driver
     * @param elementToBeLoaded
     * @param valueToSelect
     * @return
     */
    public static boolean dropdownPopulatedWithValue(final WebDriver driver, final WebElement elementToBeLoaded,
        final String valueToSelect) {

        elementVisibile(driver, elementToBeLoaded);
        getWebDriverWait(driver).ignoring(StaleElementReferenceException.class);
        try {
            getWebDriverWait(driver).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(final WebDriver d) {
                    // Note: This has to be checked this way because for SelectElements with a lot of options,
                    // it will take a lot of time if we use Select.getOptions(), Because Select.getOptions()
                    // fetches all the options of Select element (using "element.findElements(By.tagName("option"));"),
                    // which makes it very slow. Tested for ProductLine, and ~12K product line takes 12 seconds for
                    // Select.getOptions().
                    try {
                        final Select droplist = new Select(elementToBeLoaded);
                        droplist.selectByVisibleText(valueToSelect);
                        LOGGER.debug("dropdownPopulatedWithValue , webElement=%s " + elementToBeLoaded);
                        return true;
                    } catch (final NoSuchElementException | StaleElementReferenceException noSuchElementException) {
                        return false;
                    }
                }

                /**
                 * Seems like this is needed to run it in java 7 or lesser, which is used in jenkins
                 */
                public boolean test(final WebDriver input) {
                    return apply(input);
                }
            });
            return true;
        } catch (final Exception ex) {
            LOGGER.error("Exception: " + ex.getMessage());
            return false;
        }
    }

    /**
     * This method will be used to initialise the WebDriverWait instance which can be used by different methods
     *
     * @param webDriver
     * @return - returns wait instance
     */
    private static WebDriverWait getWebDriverWait(final WebDriver webDriver) {
        if (webDriverWait == null) {
            final WebDriverWait webDriverWait = new WebDriverWait(webDriver, timeOutInSecs);
            webDriverWait.pollingEvery(pollingTime, timeUnit);
            return webDriverWait;
        }
        return webDriverWait;
    }

    /**
     * This is a method which will return a boolean value based on the element is reachable by page DOM
     *
     * @param driver
     * @param selector
     * @return true or false
     */
    public static boolean presenceOfElement(final WebDriver driver, final String selector) {
        try {
            getWebDriverWait(driver).until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
            LOGGER.debug(String.format("elementpresent, at xpath=%s ", selector));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }

    }

    /**
     * This is a method which will wait till the value of the attribute changes
     *
     * @param driver
     * @param element
     * @param name
     * @param value
     * @return boolean true or false.
     */
    public static boolean isElementAttributeValueChanged(final WebDriver driver, final String selector,
        final String name, final String value) {
        try {
            final WebElement element = driver.findElement(By.xpath(selector));
            getWebDriverWait(driver).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(final WebDriver d) {
                    return element.getAttribute(name).equals(value);
                }

                public boolean test(final WebDriver input) {
                    return apply(input);
                }
            });
            LOGGER.debug(String.format("element attribute changed, webElement=%s", element));
            return true;
        } catch (final TimeoutException toe) {
            LOGGER.error("Exception: " + toe.getMessage());
            return false;
        }
    }

    public static boolean dropdownPopulatedWithValues(final WebDriver driver, final WebElement elementToBeLoaded) {
        elementVisibile(driver, elementToBeLoaded);
        getWebDriverWait(driver).ignoring(StaleElementReferenceException.class);
        try {
            getWebDriverWait(driver).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(final WebDriver d) {
                    // Note: This has to be checked this way because for SelectElements with a lot of options,
                    // it will take a lot of time if we use Select.getOptions(), Because Select.getOptions()
                    // fetches all the options of Select element (using "element.findElements(By.tagName("option"));"),
                    // which makes it very slow. Tested for ProductLine, and ~12K product line takes 12 seconds for
                    // Select.getOptions().
                    try {
                        final Select droplist = new Select(elementToBeLoaded);
                        final int droplistSize = droplist.getOptions().size();
                        if (droplistSize > 1) {
                            LOGGER.info("Drop down size " + droplistSize);
                            return true;
                        } else {
                            return false;
                        }
                    } catch (final NoSuchElementException | StaleElementReferenceException noSuchElementException) {
                        return false;
                    }
                }

                /**
                 * Seems like this is needed to run it in java 7 or lesser, which is used in jenkins
                 */
                public boolean test(final WebDriver input) {
                    return apply(input);
                }
            });
            return true;
        } catch (final Exception ex) {
            LOGGER.error("Exception: " + ex.getMessage());
            return false;
        }

    }

    /**
     * This is a method to check whether the element is stale
     *
     * @param element
     * @return
     */
    public static ExpectedCondition<Boolean> stalenessOf(final WebElement element) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver ignored) {
                try {
                    // Calling any method forces a staleness check
                    element.isEnabled();
                    return false;
                } catch (final StaleElementReferenceException expected) {
                    return true;
                }
            }

            @Override
            public String toString() {
                return String.format("element (%s) to become stale", element);
            }
        };
    }
}
