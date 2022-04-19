package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This is the page class for the Price List Details Page.
 *
 * @author t_joshv
 */
public class PriceListDetailPage extends GenericDetails {

    public PriceListDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(name = "Delete")
    private WebElement deleteButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceListDetailPage.class.getSimpleName());

    /**
     * This method returns id of a Price.
     *
     * @return id of a Price
     */
    public String getId() {
        final String id = getValueByField("ID");
        LOGGER.info("Price ID : " + id);
        return id;
    }

    /**
     * This method returns external key of a Price.
     *
     * @return external key of a Price
     */
    public String getExternalKey() {
        final String externalKey = getValueByField("External Key");
        LOGGER.info("Price External Key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns name of a Price.
     *
     * @return Name of a Price
     */
    public String getName() {
        final String name = getValueByField("Name");
        LOGGER.info("Price Name : " + name);
        return name;
    }

    /**
     * This method returns a Store.
     *
     * @return store
     */
    public String getStore() {
        final String store = getValueByField("Store");
        LOGGER.info("Price belongs to Store : " + store);
        return store;
    }

    /**
     * This method returns a currency.
     *
     * @return currency
     */
    public String getCurrency() {
        final String currency = getValueByField("Currency");
        LOGGER.info("Currency used for Price : " + currency);
        return currency;
    }

    /**
     * This method returns a created.
     *
     * @return created
     */
    public String getCreated() {
        final String created = getValueByField("Created");
        LOGGER.info("Created Timestamp : " + created);
        return created;
    }

    /**
     * This method returns last modified.
     *
     * @return last modified
     */
    public String getLastModified() {
        final String lastmodified = getValueByField("Last Modified");
        LOGGER.info("Last Modified Timestamp : " + lastmodified);
        return lastmodified;
    }

    /**
     * This method click on edit button return.
     *
     * @return last modified
     */
    public EditPriceListPage editPriceList() {
        editButton.click();
        return super.getPage(EditPriceListPage.class);
    }

    /**
     * This Method to confirm Delete Action.
     *
     * @return StoreDetailPage
     */
    public StoreDetailPage deleteWithConfirm() {
        deleteButton.click();

        final String mainWindow = getDriver().getWindowHandle();
        final Set<String> windowHandles = getDriver().getWindowHandles();
        getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
        clickOnConfirmButton();
        getDriver().switchTo().window(mainWindow);

        return super.getPage(StoreDetailPage.class);
    }

    /**
     * This Method to handle Error for Deleting Price.
     */
    public PriceListDetailPage deleteWithConfirmToHandleError() {
        deleteButton.click();

        final Set<String> windowHandles = getDriver().getWindowHandles();
        getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
        clickOnConfirmButton();

        return super.getPage(PriceListDetailPage.class);
    }

    /**
     * This method will click on the confirm button in the pop up.
     */
    private void clickOnConfirmButton() {
        confirmButton.click();
    }

}
