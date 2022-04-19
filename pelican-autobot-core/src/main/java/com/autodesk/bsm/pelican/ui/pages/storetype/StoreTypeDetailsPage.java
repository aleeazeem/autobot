package com.autodesk.bsm.pelican.ui.pages.storetype;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This is the page class for the Store Type Details Page
 *
 * @author vineel
 */
public class StoreTypeDetailsPage extends GenericDetails {

    public StoreTypeDetailsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(name = "Delete")
    private WebElement deleteButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(xpath = ".//*[@class='summary']//h3")
    private WebElement errorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddStoreTypePage.class.getSimpleName());

    /**
     * click on the edit button on the store type detail page
     */
    public EditStoreTypePage editStoreType() {
        LOGGER.info("Click on the edit button on the store type detail page");
        editButton.click();

        return super.getPage(EditStoreTypePage.class);
    }

    /**
     * click on the delete button on the store type detail page
     */
    public DeleteStoreTypePage deleteStoreType() {
        LOGGER.info("Click on the delete button on the store type detail page");
        deleteButton.click();
        try {
            final String mainWindow = getDriver().getWindowHandle();
            final Set<String> windowHandles = getDriver().getWindowHandles();
            getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
            confirmButton.click();
            getDriver().switchTo().window(mainWindow);
            return super.getPage(DeleteStoreTypePage.class);
        } catch (final Exception e) {
            LOGGER.info("Error Message " + errorMessage.getText());
            return super.getPage(DeleteStoreTypePage.class);
        }

    }

    /**
     * This method returns application family of a store type
     *
     * @return application family name
     */
    public String getApplicationFamily() {
        final String appFamily = getValueByField("Application Family");
        LOGGER.info("Application Family name : " + appFamily);
        return appFamily;
    }

    /**
     * This method returns id of a store type
     *
     * @return id of a store type
     */
    public String getId() {
        final String id = getValueByField("ID");
        LOGGER.info("ID : " + id);
        return id;
    }

    /**
     * This method returns external key of a store type
     *
     * @return external key of a store type
     */
    public String getExternalKey() {
        final String externalKey = getValueByField("External Key");
        LOGGER.info("External Key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns name of a store type
     *
     * @return Name of a store type
     */
    public String getName() {
        final String name = getValueByField("Name");
        LOGGER.info("Name : " + name);
        return name;
    }

    /**
     * This method returns created date of a store type
     *
     * @return created date of a store type
     */
    public String getCreated() {
        final String created = getValueByField("Created");
        LOGGER.info("Created : " + created);
        return created;
    }

    /**
     * This method returns last modified date of a store type
     *
     * @return last modified date of a store type
     */
    public String getLastModified() {
        final String lastModified = getValueByField("Last Modified");
        LOGGER.info("Last Modified : " + lastModified);
        return lastModified;
    }
}
