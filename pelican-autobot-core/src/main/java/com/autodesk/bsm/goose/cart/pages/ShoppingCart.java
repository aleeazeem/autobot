package com.autodesk.bsm.goose.cart.pages;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.CartConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShoppingCart extends GenericDetails {

    @FindBy(xpath = ".//*[@id='cart']//*[@class='row cart-table ng-scope']//*[@class='text ng-binding']")
    private WebElement checkout;

    @FindBy(xpath = "//*[@id=\"cart\"]/div[2]/div/h3")
    private WebElement emptyCart;

    @FindBy(id = "userName_str")
    private WebElement userNameField;

    @FindBy(xpath = "//*[@id=\"password\"]")
    private WebElement passwordField;

    @FindBy(className = "pointer ng-touched ng-dirty ng-valid-parse ng-valid ng-valid-required")
    private WebElement checkboxAgreement;

    @FindBy(className = "login-iframe pos-rel width100 mt-25")
    private WebElement loginClass;

    @FindBy(xpath = "//*[@id=\"signin\"]/div[2]/div[1]/div/div[1]/div/div[1]/p")
    private WebElement pageHeading;

    @FindBy(xpath = "//div[contains(concat(' ', @class, ' '), 'next')]")
    private WebElement reviewOrder;

    @FindBy(xpath = "//input[@type='checkbox']")
    private WebElement agreeCheckbox;

    @FindBy(xpath = "//div[contains(concat(' ', @class, ' '), 'next')]")
    private WebElement continueOrderSummary;

    @FindBy(xpath = "//tab-heading[contains(text(),'PayPal')]")
    private WebElement choosePaypal;

    @FindBy(xpath = "//tab-heading[contains(text(),'Credit Card')]")
    private WebElement chooseCreditCard;

    @FindBy(id = "email")
    private WebElement emailAddressPaypal;

    @FindBy(id = "password")
    private WebElement passwordPaypal;

    @FindBy(id = "btnLogin")
    private WebElement paypalLogin;

    @FindBy(id = "confirmButtonTop")
    private WebElement paypalAgreeContinue;

    @FindBy(xpath = "//input[@value='Agree & Continue' and @type='submit']")
    private WebElement paypalAgreeContinuexpath;

    @FindBy(xpath = ".//*[@id='order-complete']//*[@class='page-heading']//*[@class='page-title ng-binding']")
    private WebElement orderCompleteHeading;

    @FindBy(css = "order-number.ng-binding")
    private WebElement orderCompleteOrderId;

    @FindBy(xpath = ".//*[@ng-model='product.quantity']")
    private WebElement quantityInput;

    private WebDriver driver;

    private List<WebElement> elementsList = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCart.class.getSimpleName());

    /**
     * Constructor to set the environmentVariables and driver
     */
    public ShoppingCart(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        PageFactory.initElements(driver, this); // init the page fields
        this.driver = driver;
    }

    /**
     * Method to launch Cart with StoreKey and ProductIDs
     */
    public void loadShoppingCart(final EnvironmentVariables environmentVariables, final String storeKey,
        final String productType) {

        String url = environmentVariables.getCartUrl() + "/cart/";
        LOGGER.info("Navigate to Cart url: '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(getDriver());

        if (!(isCartEmpty())) {
            emptyCart(environmentVariables);
        }

        url = environmentVariables.getCartUrl() + "/r?storeKey=" + storeKey + "&productIds="
            + getPriceId(environmentVariables.getCartUrl(), productType);
        LOGGER.info("Navigate to Cart url: '" + url + "'");
        getDriver().get(url);

    }

    /**
     * Based on the test bed, we need to fetch the priceIds
     */
    private String getPriceId(final String cartUrl, final String productType) {
        if (cartUrl.contains("stg") && productType.equalsIgnoreCase("BIC")) {
            return CartConstants.STG_BIC_MAYALT_ADVANCED_MONTHLY;
        } else if (cartUrl.contains("stg") && productType.equalsIgnoreCase("META")) {
            return CartConstants.STG_META_3DS_MAX_BASIC_MONTHLY;
        } else if (cartUrl.contains("stg") && productType.equalsIgnoreCase("BIC&BIC")) {
            return CartConstants.STG_BIC_MAYALT_ADVANCED_MONTHLY + "," + CartConstants.STG_BIC_BIC_MAYALT_BASIC_MONTHLY;
        } else if (cartUrl.contains("dev") && productType.equalsIgnoreCase("BIC")) {
            return CartConstants.DEV_BIC_MAYALT_ADVANCED_MONTHLY;
        } else if (cartUrl.contains("dev") && productType.equalsIgnoreCase("META")) {
            return CartConstants.DEV_META_3DS_MAX_ADVANCED_ANNUAL;
        } else if (cartUrl.contains("dev") && productType.equalsIgnoreCase("BIC&BIC")) {
            return CartConstants.DEV_BIC_MAYALT_ADVANCED_MONTHLY + "," + CartConstants.DEV_BIC_MAYALT_BASIC_MONTHLY;
        }

        return null;
    }

    /**
     * Method to checkout Shopping cart
     */
    public void checkoutShoppingCart() {
        Wait.elementVisibile(driver, checkout);
        getActions().click(checkout);
        LOGGER.info("Clicked on Checkout in Shopping Cart");
    }

    /**
     * When User checkout a product from cart, If this is the 1st order in this session then User Cart login is
     * performed
     */
    public void cartLogin(final String userName, final String password) {
        Boolean isButtonFound = false;
        // Tried to remove the static wait as much as I can. If I remove it further, tests are failing.
        // Spent lot of time in investigation. Couldn't find the exact reason and fix for them.

        Util.waitInSeconds(TimeConstants.LONG_WAIT);
        try {
            isButtonFound = reviewOrder.isDisplayed();
        } catch (final Exception e) {
            LOGGER.info("Performing User Login...");
        }
        if (isButtonFound) {
            LOGGER.info("User already Logged IN, so skipping login steps...");
        } else {
            final List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            if (iframes.size() > 0) {
                driver.switchTo().frame(iframes.get(0));
            }
            try {
                elementsList.clear();
                elementsList.add(userNameField);
                if (userNameField.isDisplayed()) {
                    getActions().click(userNameField);
                    userNameField.sendKeys(userName);
                    getActions().click(passwordField);
                    passwordField.sendKeys(password);
                    submit();
                    driver.switchTo().defaultContent();
                    LOGGER
                        .info("Entered Username:" + userName + " and Password:" + password + " and clicked on submit");
                }
            } catch (final Exception e) {
                driver.switchTo().defaultContent();
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * Based on the payment type parameter, we need to select the payment option
     */
    public void choosePaymentTypeAndReviewOrder(final String paymentType) {
        final List<AssertionError> assertionErrorList = new ArrayList<>();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        Wait.elementVisibile(driver, reviewOrder);
        elementsList.clear();
        elementsList.add(reviewOrder);
        AssertCollector.assertThat("Failed to find ReviewOrder Button in Billing and Payments Page",
            reviewOrder.isDisplayed(), equalTo(true), assertionErrorList);

        if (paymentType.equalsIgnoreCase("CreditCard")) {
            getActions().click(chooseCreditCard);
            LOGGER.info("Select Credit Card as payment");
        } else if (paymentType.equalsIgnoreCase("Paypal")) {
            getActions().click(choosePaypal);
            LOGGER.info("Select Paypal as payment");
        } else {
            Assert.fail("Invalid PaymentType!!!");
        }
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        getActions().click(reviewOrder);
        LOGGER.info("Clicked on Review Order");
    }

    /**
     * For Paypal Order, this method will login from the paypal popup
     */
    public void submitPaypalOrderSummary(final String paypalEmail, final String paypalPassword) {

        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        getActions().click(agreeCheckbox);
        final String mainWindowHandle = driver.getWindowHandle();

        getActions().click(continueOrderSummary);

        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);

        try {
            final Set windows = driver.getWindowHandles();
            for (final Object window : windows) {
                final String popupHandle = window.toString();
                if (!popupHandle.contains(mainWindowHandle)) {
                    driver.switchTo().window(popupHandle);

                    Boolean isButtonFound = false;
                    try {
                        isButtonFound = paypalAgreeContinue.isDisplayed();
                    } catch (final Exception e) {
                        LOGGER.info("Performing Paypal Login...");
                    }

                    if (isButtonFound) {
                        LOGGER.info("User already logged into Paypal Sandbox, so skipping Paypal Login...");
                    } else {
                        final List<WebElement> paypalIFrames = driver.findElements(By.tagName("iframe"));
                        if (paypalIFrames.size() > 0) {
                            driver.switchTo().frame(paypalIFrames.get(0));
                        }
                        emailAddressPaypal.clear();
                        emailAddressPaypal.sendKeys(paypalEmail);
                        passwordPaypal.clear();
                        passwordPaypal.sendKeys(paypalPassword);
                        paypalLogin.submit();
                        LOGGER.info("Successfully logged into Paypal Sandbox");
                        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
                    }
                    driver.manage().window().maximize();
                    driver.switchTo().defaultContent();
                    getActions().click(paypalAgreeContinue);
                    LOGGER.info("Clicked on Agree and Continue in Paypal popup");

                }
            }
        } catch (final Exception e) {
            driver.switchTo().window(mainWindowHandle);
            Assert.fail(e.getMessage());
        }
        driver.switchTo().window(mainWindowHandle);
    }

    /**
     * This method will grep the Purchase Order Id from the summary page
     *
     * @return purchase Order
     */
    public String getPurchaseOrderID() {

        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);

        Wait.textToBePresentInElementLocated(driver,
            By.xpath(".//*[@id='order-complete']//*[@class='page-heading']//*[@class='page-title ng-binding']"),
            "Thank You. Your order is complete!");
        if (!(orderCompleteHeading.getText().equalsIgnoreCase("Thank You. Your order is complete!"))) {
            Assert.fail("Order Complete page validation failed.");
        }

        String[] splitString = null;
        if (orderCompleteOrderId.getText().contains("Order Number")) {
            splitString = orderCompleteOrderId.getText().split(": ");
            LOGGER.info("Order Number: " + splitString[1]);
        } else {
            Assert.fail("Could not find Order Number from Order Complete page!!!");
        }
        // Added wait here to reduce some duplicates in the class. this wait is needed for PO to get CHARGED
        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        return splitString[1];

    }

    /**
     * Method which will empty cart, by deleting the items from the cart
     *
     * @return true or false
     */
    private Boolean emptyCart(final EnvironmentVariables environmentVariables) {

        final String url = environmentVariables.getCartUrl() + "/cart/";
        LOGGER.info("Navigate to Cart url: '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);
        if (!(isCartEmpty())) {
            try {
                final List<WebElement> trashItems =
                    driver.findElements(By.xpath("//img[contains(@src,'./images/trash-icon.png')]"));
                LOGGER.info("Total number of products found in cart: " + trashItems.size());
                for (final WebElement trashItem : trashItems) {
                    getActions().click(trashItem);
                }
            } catch (final Exception e) {
                LOGGER.info("Error while Deleteing Products from cart.");
            }
        }

        return isCartEmpty();
    }

    /**
     * Method to check if cart is empty
     *
     * @return true or false
     */
    private Boolean isCartEmpty() {
        try {

            if (emptyCart.getText().contains("Your shopping cart is currently empty.")) {
                LOGGER.info("Cart is Empty!!!");
                return true;

            } else {
                LOGGER.info("Cart is Not empty!!!");
                return false;
            }

        } catch (final Exception e) {
            LOGGER.info("Cart is Not empty, need to cleanup!!!");
            return false;
        }
    }

    /**
     * Method to set line item quantity.
     *
     * @param qunatity
     */
    public void setQuantity(final String qunatity) {
        getActions().setText(quantityInput, qunatity);
    }
}
