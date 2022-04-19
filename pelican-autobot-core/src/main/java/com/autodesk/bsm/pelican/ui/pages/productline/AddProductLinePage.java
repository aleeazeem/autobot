package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.ConfirmationPopup;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page class for Add Product Line Page
 *
 * @author Shweta Hegde
 */
public class AddProductLinePage extends GenericDetails {

    public AddProductLinePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AddProductLinePage.class.getSimpleName());

    /**
     * Navigate to product line's add form
     */
    private void navigateToAddPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PRODUCT_LINE.getForm() + "/addForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);
    }

    public void addProductLine(final String name, final String externalKey, final String activeStatus) {
        LOGGER.info("Add new Product Line");
        navigateToAddPage();
        setName(name);
        setExternalKey(externalKey);
        selectActiveStatus(activeStatus);

        if (getActiveStatusDisplayValue().equals(PelicanConstants.NO)) {
            LOGGER.info("Clicked on Confirmation button");
            final ConfirmationPopup popup = getPage(ConfirmationPopup.class);
            popup.confirm();
            LOGGER.info("Clicked on confirmation button in the pop up");
        }
    }

    /**
     * This method clicks on the submit button and returns productline detail page
     *
     * @return ProductLineDetailsPage
     */
    public ProductLineDetailsPage clickOnSubmit() {
        submit();
        LOGGER.info("Click on Add Product Line");
        return super.getPage(ProductLineDetailsPage.class);
    }

}
