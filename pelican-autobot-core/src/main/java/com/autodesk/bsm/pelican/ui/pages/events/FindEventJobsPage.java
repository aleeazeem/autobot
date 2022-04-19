package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.EventJobStatus;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object for Find events jobs page in pelican admin tool. This page can be viewed in AT from
 * applications tab / events > event jobs / find.
 *
 * @author Muhammad
 *
 */
public class FindEventJobsPage extends GenericDetails {

    public FindEventJobsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "category")
    private WebElement categorySelect;

    @FindBy(id = "status")
    private WebElement statusSelect;

    @FindBy(id = "input-createdDateStart")
    private WebElement createdDateStartInput;

    @FindBy(id = "input-createdDateEnd")
    private WebElement createdDateEndInput;

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement advancedFindTab;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindEventJobsPage.class.getSimpleName());

    /**
     * Method to navigate to find event jobs page.
     */
    private void navigateToPage() {
        final String url = getEnvironment().getAdminUrl() + "/event/job/" + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to find jobs through advanced find
     *
     * @param category
     * @param status
     * @param createdStartDate
     * @param createdEndDate
     */
    public void findJobsThroughAdvancedFind(final JobCategory category, final EventJobStatus status,
        final String createdStartDate, final String createdEndDate) {
        navigateToPage();
        advancedFindTab.click();
        selectCategory(category);
        selectStatus(status);
        setCreatedStartDate(createdStartDate);
        setCreatedEndDate(createdEndDate);
    }

    /**
     * Method to find event job by id.
     *
     * @param jobId
     * @param jobId
     * @return EventJobStatusDetailsPage
     */
    public EventJobStatusDetailsPage findEventsByJobId(final String jobId) {
        findEventsByjobId(jobId);
        return getPage(EventJobStatusDetailsPage.class);
    }

    /**
     * Method to find event job by invalid id.
     *
     * @param jobId
     * @return FindEventJobsPage
     */
    public FindEventJobsPage findEventsByJobIdForErrors(final String jobId) {
        findEventsByjobId(jobId);
        return getPage(FindEventJobsPage.class);
    }

    /**
     * Method to click on find event jobs button.
     */
    public EventJobsSearchResultsPage clickOnFindEventJobsButton() {
        submit(1);
        return getPage(EventJobsSearchResultsPage.class);
    }

    /**
     * Method to click on find event jobs button.
     */
    public FindEventJobsPage clickOnFindEventJobsButtonErrors() {
        submit(1);
        return getPage(FindEventJobsPage.class);
    }

    /**
     * Method to set jobId and click on submit button.
     *
     * @param jobId
     */
    private void findEventsByjobId(final String jobId) {
        navigateToPage();
        setId(jobId);
        submit(TimeConstants.ZERO_SEC);
    }

    /**
     * Method to select category from drop down.
     *
     * @param category
     */
    private void selectCategory(final JobCategory category) {
        if (category != null) {
            getActions().select(categorySelect, category.getJobCategory());
            LOGGER.info("Selected category is '" + category.getJobCategory() + "'");
        }
    }

    /**
     * Method to select status from drop down.
     *
     * @param status
     */
    private void selectStatus(final EventJobStatus status) {
        if (status != null) {
            getActions().select(statusSelect, status.getName());
            LOGGER.info("Selected status is '" + status.getName() + "'");
        }
    }

    /**
     * Method to set created start date.
     *
     * @param createdStartDate
     */
    private void setCreatedStartDate(final String createdStartDate) {
        getActions().setText(createdDateStartInput, createdStartDate);
        LOGGER.info("Created start date is set to selected");
    }

    /**
     * Method to set created end date.
     *
     * @param createdEndDate
     */
    private void setCreatedEndDate(final String createdEndDate) {
        getActions().setText(createdDateEndInput, createdEndDate);
        LOGGER.info("Created end date is set to: " + createdEndDate);
    }

}
