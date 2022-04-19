package com.autodesk.bsm.pelican.ui.productline;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.productline.FindProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineSearchResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests Find Product Line Functionality
 *
 * @author Shweta Hegde
 */
public class FindProductLineTest extends SeleniumWebdriver {

    private FindProductLinePage findProductLinePage;
    private ProductLineDetailsPage productLineDetailsPage;
    private String productLineId;
    private static final String productLineExternalKey =
        PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
    private static final String productLineName =
        PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(4);

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        // Instantiate admin tool
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findProductLinePage = adminToolPage.getPage(FindProductLinePage.class);

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        ProductLine productLine = new ProductLine();
        final ProductLineData productLineData = new ProductLineData();
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineName);
        productLineData.setType(PelicanConstants.PRODUCT_LINE);
        productLine.setData(productLineData);
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        productLine = subscriptionPlanApiUtils.addProductLine(resource, productLine);
        productLineId = productLine.getData().getId();
    }

    /**
     * Find a product line by valid id Result: Product line with given id is found
     */
    @Test
    public void findProductLinesByValidId() {

        productLineDetailsPage = findProductLinePage.findByValidId(productLineId);

        AssertCollector.assertThat("Incorrect product line id", productLineDetailsPage.getId(), equalTo(productLineId),
            assertionErrorList);
        HelperForProductLine.assertNameAndExternalKey(productLineDetailsPage, productLineName, productLineExternalKey,
            PelicanConstants.YES, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find a product line by valid external key Result: Product line with given external key is found
     */
    @Test
    public void findProductLinesByValidExternalKey() {

        productLineDetailsPage = findProductLinePage.findByValidExternalKey(productLineExternalKey);

        AssertCollector.assertThat("Incorrect product line id", productLineDetailsPage.getId(), equalTo(productLineId),
            assertionErrorList);
        HelperForProductLine.assertNameAndExternalKey(productLineDetailsPage, productLineName, productLineExternalKey,
            PelicanConstants.YES, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find a product line by invalid id Result: Empty result set
     */
    @Test(dataProvider = "invalidInput")
    public void findProductLineByInvalidId(final String id) {

        findProductLinePage.findByInValidId(id);

        AssertCollector.assertThat("Incorrect error", findProductLinePage.getErrorMessage(),
            equalTo(PelicanErrorConstants.NUMBER_ERROR_MESSAGE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find a product line by invalid external key Result: Empty result set
     */
    @Test(dataProvider = "invalidInput")
    public void findProductLineByInvalidExtKey(final String externalKey) {

        final ProductLineSearchResultPage productLineSearchResultPage =
            findProductLinePage.findByInValidExternalKey(externalKey);

        AssertCollector.assertThat("Found store type by invalid external key # " + externalKey,
            productLineSearchResultPage.getColumnValuesOfId().get(0), equalTo("None found"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "invalidInput")
    private Object[][] getInvalidInput() {
        return new Object[][] { { "123abc" }, { "xyz" }, { "!@#$%^" } };
    }
}
