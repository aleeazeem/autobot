package com.autodesk.bsm.pelican.ui.pages.stores;

/**
 * This is the page class for the Add Stores Page. Stores --> Stores --> Add
 *
 * @author t_joshv
 */

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddStorePage extends GenericDetails {

    public AddStorePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "store-status")
    private WebElement storeStatus;

    @FindBy(id = "typeId")
    private WebElement storeType;

    @FindBy(id = "input-sendTaxInvoicesEmails")
    private WebElement sendTaxInvoicesEmails;

    @FindBy(id = "input-vatPercentage")
    private WebElement vatPercentage;

    @FindBy(id = "input-soldToCSN")
    private WebElement soldToCsn;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddStorePage.class.getSimpleName());

    /**
     * This is the method which will set the status in the add store page.
     *
     * @param status : Store status
     */
    private void setStatus(final String status) {
        if (status != null) {
            LOGGER.info("Set status to '" + status + "'");
            getActions().select(storeStatus, status);
        } else {
            getActions().select(storeStatus, Status.NEW.getDisplayName());
        }

    }

    /**
     * This is the method which will set the type in the add store page.
     *
     * @param type : store type
     */
    private void setType(final String type) {
        LOGGER.info("Set type to '" + type + "'");
        getActions().select(storeType, type);
    }

    /**
     * This is the method which will select send tax invoice email.
     *
     * @param isSendTaxInvoiceEmailsSet : boolean value for send tax invoice
     */
    private void setSendTaxInvoiceEmails(final boolean isSendTaxInvoiceEmailsSet) {
        LOGGER.info("Set send tax invoice emails");
        if (isSendTaxInvoiceEmailsSet) {
            getActions().check(sendTaxInvoicesEmails);
        }
    }

    /**
     * This is the method which will set Vat Percentage.
     *
     * @param vatPercentageInput : VAT percentage
     */
    private void setVatPercentage(final String vatPercentageInput) {
        LOGGER.info("Set Vat Percentage to " + vatPercentageInput + "'");
        getActions().setText(vatPercentage, vatPercentageInput);
    }

    /**
     * This is the method which will set sold to CSN.
     *
     * @param soldToCsnInput : sold to CSN value
     */
    private void setSoldToCsn(final String soldToCsnInput) {
        LOGGER.info("Set sold to CSN to " + soldToCsnInput + "'");
        getActions().setText(soldToCsn, soldToCsnInput);
    }

    /**
     * This is the method which will Add Store.
     *
     * @param name : store name
     * @param externalKey : store external key
     * @param status : store status
     * @param type : store type
     * @param sendTaxInvoice : boolean value for send tax invoice
     * @param vatPercentage : VAT percentage
     * @param soldToCsnInput : sold to CSN value
     *
     * @return StoreDetailPage
     */
    public void addStore(final String name, final String externalKey, final String status, final String type,
        final boolean sendTaxInvoice, final String vatPercentage, final String soldToCsnInput) {

        navigateToAddStoreForm();
        setName(name);
        setExternalKey(externalKey);
        setStatus(status);
        setType(type);
        setSendTaxInvoiceEmails(sendTaxInvoice);
        setVatPercentage(vatPercentage);
        setSoldToCsn(soldToCsnInput);

    }

    /**
     * Method to click on Add Store Button.
     *
     * @Return StoreDetailPage.
     */
    public StoreDetailPage clickAddStore() {
        submit();
        return super.getPage(StoreDetailPage.class);
    }

    /**
     * This method will navigate to the add store page.
     */
    private void navigateToAddStoreForm() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.STORE.getForm() + "/" + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }
}
