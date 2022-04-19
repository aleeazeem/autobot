package com.autodesk.bsm.pelican.ui.pages.storetype;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * This is the page class for the Add Store Type module which can be accessed from Stores --> StoreType --> Add
 *
 * @author vineel
 */

public class AddStoreTypePage extends GenericDetails {

    public AddStoreTypePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "appFamilyId")
    private WebElement applicationFamilySelect;

    @FindBy(id = "input-name")
    private WebElement nameInput;

    @FindBy(css = "#field-name .error-message")
    private WebElement nameInputError;

    @FindBy(css = "#field-externalKey .error-message")
    private WebElement extKeyInputError;

    @FindBy(xpath = ".//*[@class='error-message']")
    private WebElement errorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddStoreTypePage.class.getSimpleName());

    /**
     * This is the method which will add the store type details in the admin tool
     *
     * @param external key
     */
    public void addStoreType(final String name, final String externalKey) {

        navigateToAddForm();
        LOGGER.info("Add Store Type");
        setName(name);
        setExternalKey(externalKey);
    }

    /**
     * This is the method which will click on submit to add a store type in admin tool
     *
     * @return StoreTypeDetailPage
     */
    public StoreTypeDetailsPage clickOnSubmit() {
        LOGGER.info("Click on submit to save a store type");
        submit();

        return super.getPage(StoreTypeDetailsPage.class);
    }

    /**
     * This method will click on cancel button on the add store type page in the admin tool
     */
    public void clickOnCancelButton() {
        LOGGER.info("Click on Cancel Button");
        cancel();
    }

    /**
     * Get name error message if exists
     */
    public String getNameErrorMessage() {
        return getError(nameInputError);
    }

    /**
     * Get ext key error message if exists
     */
    public String getExtKeyErrorMessage() {
        return getError(extKeyInputError);
    }

    /**
     * Get Error Message if exists
     *
     * @return String - Error Message
     */
    public String getH3ErrorMessage() {
        return (errorMessage.getText());
    }

    /**
     * This is the method which will return the error text from the error web element
     *
     * @return Error message as String
     */
    private String getError(final WebElement element) {

        String error = "";
        getDriver().manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);

        try {
            error = element.getText();
        } catch (final Exception e) {
            LOGGER.info(e.getMessage());
        } finally {
            getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }

        return error;
    }

    /**
     * Navigate to store type add form
     */
    private void navigateToAddForm() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.STORE_TYPE.getForm() + "/"
            + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }
}
