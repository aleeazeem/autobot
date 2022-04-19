package com.autodesk.bsm.pelican.ui.currency;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.currency.AddCurrencyPage;
import com.autodesk.bsm.pelican.ui.pages.currency.CurrencyDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class tests adding virtual currency.
 *
 * @author t_joshv
 */
public class AddCurrencyTest extends SeleniumWebdriver {

    private static AddCurrencyPage addCurrencyPage;
    private static CurrencyDetailPage currencyDetailPage;
    private final String currencyDescription = "Description" + RandomStringUtils.randomAlphabetic(8);
    private final String currencyName = PelicanConstants.CURRENCY_NAME + RandomStringUtils.randomAlphabetic(8);
    private final String currencyName1 = PelicanConstants.CURRENCY_NAME + RandomStringUtils.randomAlphabetic(8);
    private final String currencyRadix = RandomStringUtils.randomNumeric(5);
    private final String currencySku = "SKU" + RandomStringUtils.randomAlphabetic(8);
    private final String currencySkuExtension = "SKU_EXTENSION" + RandomStringUtils.randomAlphabetic(8);
    private final String currencyTaxCode = "code" + RandomStringUtils.randomAlphabetic(8);
    private static final String CURRENCY_PAGE_TITLE = "Pelican - Add Virtual Currency";

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        // initialize webdriver
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        addCurrencyPage = adminToolPage.getPage(AddCurrencyPage.class);
        currencyDetailPage = adminToolPage.getPage(CurrencyDetailPage.class);
    }

    /**
     * This method tests to add currency from AT
     *
     * @Result : Currency will be added successfully and navigate to Currency Detail Page.
     */
    @Test
    public void testAddCurrency() {

        // Navigate to the add feature page and add a feature
        addCurrencyPage.addCurrency(currencyName, currencyDescription, currencyRadix, currencySku, currencySkuExtension,
            currencyTaxCode);
        currencyDetailPage = addCurrencyPage.clickOnAddCurrencyButton();
        // Get the Currency Id from the Currency Detail page
        final String currencyId = currencyDetailPage.getCurrencyId();

        AssertCollector.assertThat("Incorrect currency id", currencyDetailPage.getCurrencyId(), equalTo(currencyId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect currency name", currencyDetailPage.getCurrencyName(),
            equalTo(currencyName), assertionErrorList);
        AssertCollector.assertThat("Incorrect currency sku key", currencyDetailPage.getSku(), equalTo(currencySku),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect currency sku extension", currencyDetailPage.getSkuExtension(),
            equalTo(currencySkuExtension), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate two Currency cannot be added with same SKU.
     *
     * @Result: Page will stay on add currency page with the Error messages
     */
    @Test
    public void testAddCurrencyWithDuplicateSku() {

        // Navigate to the add feature page and add a feature
        addCurrencyPage.addCurrency(currencyName, currencyDescription, currencyRadix, currencySku, currencySkuExtension,
            currencyTaxCode);
        currencyDetailPage = addCurrencyPage.clickOnAddCurrencyButton();

        // Add new currency with existing SKU used by testAddCurrency Method.
        addCurrencyPage.addCurrency(currencyName, currencyDescription, currencyRadix, currencySku, currencySkuExtension,
            currencyTaxCode);
        currencyDetailPage = addCurrencyPage.clickOnAddCurrencyButton();

        AssertCollector.assertThat("Wrong Title Page", getDriver().getTitle(), equalTo(CURRENCY_PAGE_TITLE),
            assertionErrorList);
        AssertCollector.assertThat("Main Error Message is not Generated", addCurrencyPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Error for unique SKU value is not Generated", addCurrencyPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.DUPLICATE_CURRENCY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate Currency cannot be added without SKU.
     *
     * @Result: Page will stay on add currency page with the Error messages
     */
    @Test
    public void testSkuAsMandatoryFieldForAddCurrency() {
        // Add currency by passing SKU value as null.
        addCurrencyPage.addCurrency(currencyName1, currencyDescription, currencyRadix, null, currencySkuExtension,
            currencyTaxCode);
        currencyDetailPage = addCurrencyPage.clickOnAddCurrencyButton();

        AssertCollector.assertThat("Wrong Title Page", getDriver().getTitle(), equalTo(CURRENCY_PAGE_TITLE),
            assertionErrorList);
        AssertCollector.assertThat("Main Error Message is not Generated", addCurrencyPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Error for SKU value is not generated", addCurrencyPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
