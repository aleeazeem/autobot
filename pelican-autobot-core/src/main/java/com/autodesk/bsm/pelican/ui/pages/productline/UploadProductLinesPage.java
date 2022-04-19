package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page class for Upload Product Lines Page
 *
 * @author Muhammad
 */
public class UploadProductLinesPage extends GenericDetails {

    public UploadProductLinesPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadProductLinesPage.class.getSimpleName());

    /**
     * Navigate to product line's upload page
     */
    private void navigateToUploadPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PRODUCT_LINE.getForm() + "/importForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * method to upload a file on upload product lines page
     *
     * @param fileName
     */
    public UploadProductLineStatusPage uploadXlxsFile(final String fileName) {
        LOGGER.info("Adding a product line through upload");
        navigateToUploadPage();
        setUploadFile(fileName, TimeConstants.THREE_SEC);
        return super.getPage(UploadProductLineStatusPage.class);
    }
}
