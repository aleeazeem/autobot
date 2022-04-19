package com.autodesk.bsm.pelican.ui.productline;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.ConfirmationPopup;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class is for Edit Product Line Functionality
 *
 * @author t_joshv
 */
public class EditProductLineTest extends SeleniumWebdriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddProductLineTest.class.getSimpleName());
    private static ProductLineDetailsPage productLineDetailsPage;
    private String name;
    private String externalKey;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        // Instantiate admin tool
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final AddProductLinePage addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);
        name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        addProductLinePage.addProductLine(name, externalKey, null);
        productLineDetailsPage = addProductLinePage.clickOnSubmit();
    }

    /**
     * Edit a product line to set Product Line Active status to NO Result: Product line is in-activated
     */
    @Test
    public void testEditProductLine() {
        productLineDetailsPage.clickOnEdit();
        productLineDetailsPage.selectActiveStatus(PelicanConstants.NO);
        final ConfirmationPopup popup = productLineDetailsPage.getPage(ConfirmationPopup.class);
        popup.confirm();
        productLineDetailsPage.submit(0L);

        HelperForProductLine.assertNameAndExternalKey(productLineDetailsPage, name, externalKey, PelicanConstants.NO,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify canceling confirmation to change product line active status will not change its status.
     *
     * @Result : It will not change active status.
     */
    @Test
    public void testDeclineConfirmationPopUpForProductLine() {
        productLineDetailsPage.clickOnEdit();
        productLineDetailsPage.selectActiveStatus(PelicanConstants.NO);
        LOGGER.info("Clicked on Cancel button");
        final ConfirmationPopup popup = productLineDetailsPage.getPage(ConfirmationPopup.class);
        popup.cancel();
        LOGGER.info("Clicked on confirmation button in the pop up");
        Util.waitInSeconds(0L);
        productLineDetailsPage.submit(0L);

        HelperForProductLine.assertNameAndExternalKey(productLineDetailsPage, name, externalKey, PelicanConstants.YES,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
