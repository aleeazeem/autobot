package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object represent the View Upload Status Page. Access via Catalog | Basic Offerings | View Upload Status
 *
 * @author t_mohag
 */
public class ViewUploadStatusBasicOfferingsPage extends GenericDetails {

    public ViewUploadStatusBasicOfferingsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // View upload status link
    @FindBy(xpath = ".//*[@id='subnav-link-view-upload-status']")
    private WebElement viewUploadStatusLink;

    // Job status drop down
    @FindBy(id = "jobStatus")
    private WebElement jobStatusSelect;

    // Job entity Drop down
    @FindBy(id = "jobEntity")
    private WebElement jobEntitySelect;

    // find upload jobs button
    @FindBy(className = "submit")
    private WebElement submitButton;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ViewUploadStatusBasicOfferingsPage.class.getSimpleName());

    /**
     * Method to select job status.
     */
    public void selectJobStatus(final String jobStatus) {
        if (!isPageValid(getUrl())) {
            navigateToPage();
        }
        LOGGER.info("Select '" + jobStatus + "' from jobStatus.");
        getActions().select(jobStatusSelect, jobStatus);
    }

    /**
     * Method to select job entity.
     */
    public void selectJobEntity(final String jobEntity) {
        if (!isPageValid(getUrl())) {
            navigateToPage();
        }
        LOGGER.info("Select '" + jobEntity + "' from jobEntity.");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        getActions().select(jobEntitySelect, jobEntity);
    }

    /**
     * Method to get the view upload status url
     *
     * @return url
     */
    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/" + AdminPages.BASIC_OFFERINGS.getForm() + "/"
            + AdminPages.VIEW_UPLOAD_STATUS.getForm();
    }

    /**
     * Navigating to view upload status page
     */
    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Determine if we're on the View Upload Status page by getting the current url
     *
     * @return true if we're on the page. Otherwise false
     */
    public boolean isPageValid(final String url) {
        boolean pageDisplayed = false;
        if (url.equalsIgnoreCase(getDriver().getCurrentUrl())) {
            pageDisplayed = true;
        }
        return pageDisplayed;
    }

    /**
     * Get Basic offerings Upload Status Page
     *
     * @return genericGrid
     */
    public GenericGrid getUploadStatusPage() {
        navigateToPage();
        return super.getPage(GenericGrid.class);
    }
}
