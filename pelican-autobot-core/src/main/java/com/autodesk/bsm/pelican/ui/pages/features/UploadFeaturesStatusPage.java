package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Page object represent the View Upload Status Page. Access via Catalog | Items | View Upload Status
 *
 * @author t_mohag
 */
public class UploadFeaturesStatusPage extends GenericDetails {

    public UploadFeaturesStatusPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // View upload status link
    @FindBy(xpath = ".//*[@id='subnav-link-view-upload-status']")
    private WebElement itemsViewUploadStatusLink;

    // Job status drop down
    @FindBy(id = "jobStatus")
    private WebElement jobStatusSelect;

    // Job entity Drop down
    @FindBy(id = "jobEntity")
    private WebElement jobEntitySelect;

    @FindBy(xpath = "//*[@class='o']/td[9]")
    private WebElement uploadStatus;

    private GenericGrid genericGrid;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFeaturesStatusPage.class.getSimpleName());

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
        getActions().select(jobEntitySelect, jobEntity);

    }

    /**
     * Method to get the view upload status url
     *
     * @return url
     */
    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/" + AdminPages.ITEM.getForm() + "/"
            + AdminPages.VIEW_UPLOAD_STATUS.getForm();
    }

    /**
     * Navigating to view upload status page
     */
    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);
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
     * This method returns the upload status of feature
     *
     * @return upload status
     */
    public String getUploadStatus() {

        LOGGER.info("Upload status is : " + uploadStatus.getText());
        return uploadStatus.getText();
    }

    /**
     * This method returns total items
     *
     * @return total items (int)
     */
    public int getTotalItems() {

        genericGrid = super.getPage(GenericGrid.class);
        return genericGrid.getTotalItems();
    }

    /**
     * This method returns column headers
     *
     * @return List<String>
     */
    public List<String> getColumnHeaders() {

        genericGrid = super.getPage(GenericGrid.class);
        return genericGrid.getColumnHeaders();
    }

    /**
     * This method returns value of column
     *
     * @return List<String>
     */
    public List<String> getColumnValues(final String columnName) {

        genericGrid = super.getPage(GenericGrid.class);
        return genericGrid.getColumnValues(columnName);
    }

    /**
     * This methods gets value from the page
     *
     * @return String
     */
    public String getValue(final String id) {

        genericGrid = super.getPage(GenericGrid.class);
        return genericGrid.getValue(id);
    }

    /**
     * This method clicks on "Submit" button
     *
     * @return UploadFeaturesStatusPage
     */
    public UploadFeaturesStatusPage getUploadJobs() {
        submit();
        return super.getPage(UploadFeaturesStatusPage.class);
    }
}
