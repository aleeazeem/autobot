package com.autodesk.bsm.pelican.ui.pages.coreproducts;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class represents POM for Add functionality of Core Products
 *
 * @author mandas
 */
public class AddCoreProductsPage extends GenericDetails {

    public AddCoreProductsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCoreProductsPage.class.getSimpleName());

    /**
     * This method will navigate to the Add Core Products page
     *
     * @param formPage
     */
    private void navigateToAddCoreProductsPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.COREPRODUCT.getForm() + "/"
            + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
    }

    /**
     * Navigate to Add Core Products page and add a new core product.
     *
     * @param externalKey- String
     * @return coreProductDetailsPage
     */
    public CoreProductDetailPage addCoreProduct(final String externalKey) {
        navigateToAddCoreProductsPage();
        setExternalKey(externalKey);
        submit();
        return super.getPage(CoreProductDetailPage.class);
    }
}
