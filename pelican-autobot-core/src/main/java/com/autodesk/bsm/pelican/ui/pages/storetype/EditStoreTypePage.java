package com.autodesk.bsm.pelican.ui.pages.storetype;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * This is the page class for the Edit Store Type Page
 *
 * @author vineel
 */
public class EditStoreTypePage extends GenericDetails {

    public EditStoreTypePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(EditStoreTypePage.class.getSimpleName());

    /**
     * This is the method which will edit the store type in the admin tool
     *
     * @param external key
     */
    public void editStoreType(final String name, final String externalKey) {

        LOGGER.info("Edit Store Type");
        setName(name);
        setExternalKey(externalKey);
    }

    /**
     * This method will click on submit after editing the store type details in admin tool
     *
     * @return StoreTypeDetailPage object
     */
    public StoreTypeDetailsPage clickOnSubmit() {
        LOGGER.info("Click on Submit Button");
        submit();

        return super.getPage(StoreTypeDetailsPage.class);
    }

    /**
     * Get name error message if exists
     *
     * @return ErrorMessage
     */
    public String getNameErrorMessage() {
        return getError(nameInputError);
    }

    /**
     * Get ext key error message if exists
     *
     * @return ErrorMessage
     */
    public String getExtKeyErrorMessage() {
        return getError(extKeyInputError);
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
}
