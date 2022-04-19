package com.autodesk.bsm.pelican.ui.pages.currency;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object model for Add Currency under Currency in the admin tool
 *
 * @author t_joshv
 */
public class AddCurrencyPage extends GenericDetails {

    public AddCurrencyPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-currencyKey")
    private WebElement selectCurrencyName;

    @FindBy(id = "input-radix")
    private WebElement selectRadix;

    @FindBy(id = "input-sku")
    private WebElement selectSku;

    @FindBy(id = "input-skuExtension")
    private WebElement selectSkuExtension;

    @FindBy(id = "input-taxCode")
    private WebElement selectTaxCode;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCurrencyPage.class.getSimpleName());

    /**
     * This method will add the Currency in the admin tool
     */
    public void addCurrency(final String name, final String description, final String radix, final String sku,
        final String skuExtension, final String taxCode) {
        navigateToAddCurrencyPage();
        setCurrencyName(name);
        setDescription(description);
        setRadix(radix);
        setSku(sku);
        setSkuExtension(skuExtension);
        setTaxCode(taxCode);
    }

    /**
     * Method to set Currency Name.
     */
    private void setCurrencyName(final String currencyName) {
        getActions().setText(selectCurrencyName, currencyName);
        LOGGER.info("Currency Name set: " + currencyName);
    }

    /**
     * Method to set Radix.
     */
    private void setRadix(final String radix) {
        getActions().setText(selectRadix, radix);
        LOGGER.info("Radix set to: " + radix);
    }

    /**
     * Method to set SKU.
     */
    private void setSku(final String sku) {
        getActions().setText(selectSku, sku);
        LOGGER.info("SKU set to: " + sku);
    }

    /**
     * Method to set SKU Extension.
     */
    private void setSkuExtension(final String skuExtension) {
        getActions().setText(selectSkuExtension, skuExtension);
        LOGGER.info("SKU Extension set to: " + skuExtension);
    }

    /**
     * Method to set tax code.
     */
    private void setTaxCode(final String taxCode) {
        getActions().setText(selectTaxCode, taxCode);
        LOGGER.info("Taxcode selected selected: " + taxCode);
    }

    /**
     * Navigate to the Add Currency Page
     */
    private void navigateToAddCurrencyPage() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.CURRENCY.getForm() + "/" + AdminPages.ADD_FORM.getForm();
        getDriver().get(url);
    }

    /**
     * This method will click on the add Currency button and navigate to Currency Detail Page
     */
    public CurrencyDetailPage clickOnAddCurrencyButton() {

        submit();
        LOGGER.info("Clicking on the add Currency button");
        return super.getPage(CurrencyDetailPage.class);
    }
}
