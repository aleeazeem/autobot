package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page class for the New Price List Page.
 *
 * @author t_joshv
 */
public class AddPriceListPage extends GenericDetails {
    public AddPriceListPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "currencyId")
    private WebElement currencySelector;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddPriceListPage.class.getSimpleName());

    /**
     * Add Price List.
     *
     * @param name
     * @param externalKey
     * @param currency
     */
    public void addPriceList(final String name, final String externalKey, final String currency) {

        setName(name);
        setExternalKey(externalKey);
        selectCurrency(currency);
    }

    /**
     * Set currency
     *
     * @param currency
     * @return void
     */
    private void selectCurrency(final String value) {
        if (value != null) {
            LOGGER.info("Select " + value + " from currency drop down");
            getActions().select(currencySelector, value);
        }
    }

    /**
     * This method will click on Cancel button on the New Price List page in admin tool
     *
     * @return StoreDetailPage object
     */
    public StoreDetailPage clickOnCancel() {
        LOGGER.info("Click on Cancel Button");
        cancel();

        return super.getPage(StoreDetailPage.class);
    }

    /**
     * This method will click on Cancel button on the New Price List page in admin tool
     *
     * @return StoreDetailPage object
     */
    public StoreDetailPage clickOnAddPriceList() {
        LOGGER.info("Click on Add Price List Button");
        submit();

        return super.getPage(StoreDetailPage.class);
    }
}
