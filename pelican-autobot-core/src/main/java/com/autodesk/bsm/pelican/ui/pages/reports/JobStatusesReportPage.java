package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page which represents the elements and actions taken in the job statuses report page
 *
 * @author vineel
 */
public class JobStatusesReportPage extends GenericDetails {

    public JobStatusesReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "jobCategory")
    private WebElement jobCategoryDropDown;

    @FindBy(id = "jobState")
    private WebElement jobStateDropDown;

    @FindBy(id = "input-createdDateStart")
    private WebElement startDateField;

    @FindBy(id = "input-createdDateEnd")
    private WebElement endDateField;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStatusesReportPage.class.getSimpleName());

    /**
     * Method to navigate to Job Statuses Report page
     */
    public void navigateToJobStatusesReportPage() {
        final String jobStatusesReportUrl =
            getEnvironment().getAdminUrl() + "/reports/" + AdminPages.JOB_STATUSES_REPORT.getForm() + "FindForm";
        if (!isPageValid(jobStatusesReportUrl)) {
            getDriver().get(jobStatusesReportUrl);
        }
    }

    /**
     * method to select a job category
     */
    public void selectJobCategory(final String jobCategory) {
        LOGGER.info("Job Category Selected: " + jobCategory);
        getActions().select(jobCategoryDropDown, jobCategory);
    }

    /**
     * method to select a job state
     */
    public void selectJobState(final String jobState) {
        LOGGER.info("Job State Selected: " + jobState);
        getActions().select(jobStateDropDown, jobState);
    }

    /**
     * method to fill start date
     *
     * @param start date
     */
    public void fillStartDate(final String startDate) {
        LOGGER.info("Start Date selected: " + startDate);
        getActions().setText(startDateField, startDate);
    }

    /**
     * method to fill end date
     *
     * @param end date
     */
    public void fillEndDate(final String endDate) {
        LOGGER.info("End Date selected: " + endDate);
        getActions().setText(endDateField, endDate);
    }

    /**
     * Method to click on submit
     */
    public GenericGrid clickOnSubmit() {
        submit(TimeConstants.ONE_SEC);
        return getPage(GenericGrid.class);
    }

}
