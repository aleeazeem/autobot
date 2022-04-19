package com.autodesk.bsm.pelican.ui.productline;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class is for Add Product Line Functionality
 *
 * @author Shweta Hegde, Muhammad
 */
public class AddProductLineTest extends SeleniumWebdriver {

    private static AddProductLinePage addProductLinePage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        // Instantiate admin tool
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);
    }

    /**
     * Add a product line with default Product Line Active status. Result: Product line is added
     */
    @Test
    public void testAddProductLine() {

        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        addProductLinePage.addProductLine(name, externalKey, null);
        final ProductLineDetailsPage productLineDetailsPage = addProductLinePage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect product line name", productLineDetailsPage.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect product line external key", productLineDetailsPage.getExternalKey(),
            equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Product Line id should not be empty", productLineDetailsPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product Line Active status", productLineDetailsPage.getActiveStatus(),
            equalTo(PelicanConstants.YES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testAddProductLineWithInActiveStatus() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.NO);

        final ProductLineDetailsPage productLineDetailsPage = addProductLinePage.clickOnSubmit();

        AssertCollector.assertThat("Incorrect product line name", productLineDetailsPage.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect product line external key", productLineDetailsPage.getExternalKey(),
            equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Product Line id should not be empty", productLineDetailsPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product Line Active status", productLineDetailsPage.getActiveStatus(),
            equalTo(PelicanConstants.NO), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
