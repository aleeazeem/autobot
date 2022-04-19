package com.autodesk.bsm.pelican.ui.pages.productline;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page class for download Product Lines Page
 *
 * @author Muhammad
 */
public class DownloadProductLinePage extends GenericDetails {

    public DownloadProductLinePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "included")
    private WebElement isActiveDropdown;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadProductLinePage.class.getSimpleName());

    /**
     * Navigate to product line's download page
     */
    public void navigateToDownloadPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PRODUCT_LINE.getForm() + "/downloadForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * method to download a file
     *
     * @param selectIsActive TODO
     */
    public void downloadAfile(final String selectIsActive) {
        navigateToDownloadPage();
        if (selectIsActive != null) {
            if (!selectIsActive.isEmpty()) {
                selectIsActive(selectIsActive);
            }
        }

        submit();
        LOGGER.info("Clicked on download xlsx button and file is being downloaded");
    }

    /**
     * This is a method to select the is active status on the download product lines page.
     *
     * @param selectIsActive
     */
    private void selectIsActive(final String selectIsActive) {
        getActions().select(isActiveDropdown, selectIsActive);
    }

    /**
     * Method to return whether the is active field is present on the download features page.
     *
     * @return boolean value of the is active field.
     */
    public boolean isActiveFieldPresent() {
        return (isActiveDropdown.isDisplayed());
    }

}
