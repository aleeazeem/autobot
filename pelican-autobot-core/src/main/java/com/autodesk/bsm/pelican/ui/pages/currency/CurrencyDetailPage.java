package com.autodesk.bsm.pelican.ui.pages.currency;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyDetailPage extends GenericDetails {
    public CurrencyDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyDetailPage.class.getSimpleName());

    /**
     * A method to return the currency id from the currency detail page
     *
     * @return currencyId
     */
    public String getCurrencyId() {
        return getValueByField("ID");
    }

    /**
     * A method to return the currency name from the currency detail page
     *
     * @return currency name
     */
    public String getCurrencyName() {
        return getValueByField("Name");
    }

    /**
     * A method to return the sku from the currency detail page
     *
     * @return SKU
     */
    public String getSku() {
        return getValueByField("SKU");
    }

    /**
     * A method to return the SKU extension from the currency detail page
     *
     * @return SKU extension
     */
    public String getSkuExtension() {
        return getValueByField("SKU Extension");
    }
}
