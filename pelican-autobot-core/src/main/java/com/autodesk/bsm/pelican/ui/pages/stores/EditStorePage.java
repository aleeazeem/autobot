package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page class for the Edit stores Page.
 *
 * @author t_joshv
 */
public class EditStorePage extends GenericDetails {

    public EditStorePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = "#field-name .error-message")
    private WebElement nameInputError;

    @FindBy(css = "#field-externalKey .error-message")
    private WebElement externalKeyInputError;

    // Status Type on edit page
    @FindBy(id = "store-status")
    private WebElement storeStatusSelect;

    @FindBy(id = "typeId")
    private WebElement storeTypeSelect;

    @FindBy(id = "input-sendTaxInvoicesEmails")
    private WebElement sendTaxInvoiceEmailCheckbox;

    @FindBy(id = "input-vatPercentage")
    private WebElement vatPercentInput;

    @FindBy(id = "input-soldToCSN")
    private WebElement soldToCSNInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditStorePage.class.getSimpleName());

    /**
     * This method to edit store page.
     *
     * @param name TODO
     * @param externalKey TODO
     */
    public void editStore(final String status, final String storeType, final boolean sendTaxInvoiceMails,
        final String vatPercent, final String soldToCsn, final String name, final String externalKey) {
        setName(name);
        setExternalKey(externalKey);
        if (status != null) {
            setStatus(status);
        }
        if (storeType != null) {
            selectStoreType(storeType);
        }
        if (sendTaxInvoiceMails) {
            setSendTaxInvoiceEmails();
        }
        if (vatPercent != null) {
            setVatPercentage(vatPercent);
        }
        if (soldToCsn != null) {
            setSoldToCSN(soldToCsn);
        }

    }

    /**
     * This is the method which will set the Status in the edit store page
     *
     * @param status
     */
    private void setStatus(final String status) {
        LOGGER.info("Select Store Status to '" + status + "'");
        getActions().select(storeStatusSelect, status);
    }

    /**
     * Select store store type in edit store page by StoreType name
     *
     * @param storeTypeName
     */
    public void selectStoreType(final String storeType) {
        if (storeType != null) {
            LOGGER.info("Select '" + storeType + "' from store");
            getActions().select(storeTypeSelect, storeType);
        }
    }

    /**
     * Method to Select the check box for send tax invoice email
     */
    private void setSendTaxInvoiceEmails() {
        if (!sendTaxInvoiceEmailCheckbox.isSelected()) {
            sendTaxInvoiceEmailCheckbox.click();
            LOGGER.info("Clicking the checkbox of send tax invoice emails");
        }
    }

    /**
     * set Vat Percentage.
     *
     * @param vat
     */
    private void setVatPercentage(final String vat) {
        LOGGER.info("Set vat percentage to '" + vat + "'");
        getActions().setText(vatPercentInput, vat);
    }

    /**
     * Setting sold to CSN value.
     *
     * @param sold to csn value
     */
    private void setSoldToCSN(final String value) {

        soldToCSNInput.sendKeys(value);
        LOGGER.info("Setting 'Sold To CSN' value");
    }

    /**
     * This method will click on Save Changes after editing the store detail page in admin tool
     *
     * @return StoreDetailPage object
     */
    public StoreDetailPage clickOnSaveChanges() {
        LOGGER.info("Click on Save Changes Button");
        submit();

        return super.getPage(StoreDetailPage.class);
    }

    /**
     * This method will click on Cancel button on the store detail page in admin tool
     *
     * @return StoreDetailPage object
     */
    public StoreDetailPage clickOnCancel() {
        LOGGER.info("Click on Cancel Button");
        cancel();

        return super.getPage(StoreDetailPage.class);
    }
}
