package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object for Subscription Migration Page in Pelican admin tool.
 *
 * @author Muhammad
 */

public class SubscriptionMigrationPage extends GenericDetails {

    public SubscriptionMigrationPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "priceIdInput")
    private WebElement priceIdInput;

    @FindBy(id = "offerExtKey")
    private WebElement offerExternalKeyInput;

    @FindBy(id = "planId")
    private WebElement planSelect;

    @FindBy(id = "offerId")
    private WebElement offerSelect;

    @FindBy(id = "priceId")
    private WebElement priceSelect;

    @FindBy(xpath = ".//*[@class='error-message']")
    private WebElement errorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionMigrationPage.class.getSimpleName());

    /**
     * method to get value of the field displayed for subscription on subscriptiopn migration page
     *
     * @return value of the field
     */
    @Override
    public String getValueByField(final String field) {
        final String selector = ".//*[@id='form-migrateSubscriptionForm']//label[contains(text(),'" + field
            + ":')]/following-sibling" + "::div[@class='value']";
        final WebElement cell = getDriver().findElement(By.xpath(selector));
        return cell.getText();
    }

    /**
     * method to return main Error below the Title of the Page
     *
     * @return main Error text
     */
    public String getError() {
        return error.getText();
    }

    /**
     * method to return main Error below the Title of the Page
     *
     * @return main Error text
     */
    public String getErrorMessageForField() {
        return errorMessage.getText();
    }

    /**
     * Get Subscription id under Subscription Info on Subscripition Migration Page
     */
    public String getSubscriptionId() {
        final String id = getValueByField("Id");
        LOGGER.info("Subscription ID : " + id);
        return id;
    }

    /**
     * Get User (oxygen id) under Subscription Info on Subscripition Migration Page
     */
    public String getUser() {
        final String user = getValueByField("User");
        LOGGER.info("User: " + user);
        return user;
    }

    /**
     * Get product line under Migrate form on Subscripition Migration Page
     */
    public String getProductLine() {
        final String productLine = getValueByField("Product Line");
        LOGGER.info("Product Line: " + productLine);
        return productLine;
    }

    /**
     * Get plan under Migrate form on Subscripition Migration Page
     */
    public String getPlan() {
        final String plan = getValueByField("Plan");
        LOGGER.info("Plan: " + plan);
        return plan;
    }

    /**
     * Get offer under Migrate form on Subscripition Migration Page
     */
    public String getOffer() {
        final String offer = getValueByField("Offer");
        LOGGER.info("Offer: " + offer);
        return offer;
    }

    /**
     * Get price under Migrate form on Subscripition Migration Page
     */
    public String getPrice() {
        final String price = getValueByField("Price");
        LOGGER.info("Price: " + price);
        return price;
    }

    /**
     * Method to set price id text field
     */
    public void setPriceId(final String priceId) {
        getActions().setText(this.priceIdInput, priceId);
        LOGGER.info("Price ID Key has been set to: " + priceId);
    }

    /**
     * Method to set offer external key text field
     */
    public void setOfferExternalKey(final String offerExternalKey) {
        getActions().setText(this.offerExternalKeyInput, offerExternalKey);
        LOGGER.info("External Key has been set to: " + offerExternalKey);
    }

    /**
     * Method to select subscription plan from drop down plan
     */
    private void selectPlan(final String plan) {
        getActions().select(this.planSelect, plan);
        LOGGER.info("Selected plan in drop down is: " + plan);
    }

    /**
     * Method to select subscription offer from drop down plan
     */
    private void selectOffer(final String offer) {
        getActions().select(this.offerSelect, offer);
        LOGGER.info("Selected offer in drop down is: " + offer);
    }

    /**
     * Method to select subscription offer from drop down plan
     */
    private void selectPrice(final String price) {
        getActions().select(this.priceSelect, price);
        LOGGER.info("Selected price in drop down is: " + price);
    }

    /**
     * method to migrate subscription using filters
     */
    public void selectionOfFiltersForMigration(final String productLine, final String plan, final String offer,
        final String price) {
        LOGGER.info("Option of Filters is selected to Migrate Subscription");
        selectProductLine(productLine);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        selectPlan(plan);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        selectOffer(offer);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        selectPrice(price);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * method to click on migrate button
     *
     * @return Subscription Detail Page
     */
    public SubscriptionDetailPage clickOnMigrateButton() {
        submit(TimeConstants.ONE_SEC);
        LOGGER.info("Migrate Button has been clicked");
        if (getTitle().equals("Subscription Detail")) {
            LOGGER.info("Subscription Detail Page is opened");
            return super.getPage(SubscriptionDetailPage.class);
        } else {
            LOGGER.info("Subscription Detail Page is not opened");
            return null;
        }
    }
}
