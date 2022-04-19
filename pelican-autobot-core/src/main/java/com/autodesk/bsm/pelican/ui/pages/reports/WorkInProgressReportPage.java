package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page which represents the elements and actions taken in the WIP statuses report page
 *
 * @author vineel
 */
public class WorkInProgressReportPage extends GenericDetails {

    public WorkInProgressReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-jobId")
    private WebElement jobGuidInputField;

    @FindBy(id = "wipState")
    private WebElement wipStateDropDown;

    @FindBy(id = "objectType")
    private WebElement objectTypeDropDown;

    @FindBy(id = "input-objectId")
    private WebElement objectIdInputField;

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkInProgressReportPage.class.getSimpleName());

    /**
     * Method to generate Work In Progress Report with selected filters when there is no error.
     *
     * @param jobGuid
     * @param wipState
     * @param objectType
     * @param objectId
     * @return WorkInProgressReportResultPage
     */
    public WorkInProgressReportResultPage generateReport(final String jobGuid, final String wipState,
        final String objectType, final String objectId) {
        navigateToWorkInProgressStatusReportPage();
        setJobGuid(jobGuid);
        selectWipState(wipState);
        selectObjectType(objectType);
        setObjectId(objectId);
        submit(TimeConstants.ONE_SEC);
        return getPage(WorkInProgressReportResultPage.class);
    }

    /**
     * Method to generate Work In Progress Report with selected filters when there is an error on the page.
     *
     * @param jobGuid
     * @param wipState
     * @param objectType
     * @param objectId
     * @return WorkInProgressReportResultPage
     */
    public WorkInProgressReportPage generateReportWithError(final String jobGuid, final String wipState,
        final String objectType, final String objectId) {
        generateReport(jobGuid, wipState, objectType, objectId);
        return getPage(WorkInProgressReportPage.class);
    }

    /**
     * Method to navigate to WIP Statuses Report page.
     */
    private void navigateToWorkInProgressStatusReportPage() {
        final String jobStatusesReportUrl =
            getEnvironment().getAdminUrl() + "/reports/" + AdminPages.WORK_IN_PROGRESS_REPORT.getForm() + "FindForm";
        if (!isPageValid(jobStatusesReportUrl)) {
            getDriver().get(jobStatusesReportUrl);
        }
    }

    /**
     * Method to set the Job Guid in the job-guid-input-field.
     *
     * @param jobGuid
     */
    private void setJobGuid(final String jobGuid) {
        if (jobGuid != null) {
            getActions().setText(jobGuidInputField, jobGuid);
            LOGGER.info("Job Guid set: " + jobGuid);
        }
    }

    /**
     * Method to set the object id in the object id input field.
     *
     * @param objectId
     */
    private void setObjectId(final String objectId) {
        if (objectId != null) {
            getActions().setText(objectIdInputField, objectId);
            LOGGER.info("Object id set: " + objectId);
        }
    }

    /**
     * Method to select a wip state.
     *
     * @param wip state
     */
    private void selectWipState(final String wipState) {
        if (wipState != null) {
            getActions().select(wipStateDropDown, wipState);
            LOGGER.info("WIP State Selected: " + wipState);
        }
    }

    /**
     * Method to select a object type.
     *
     * @param object type
     */
    private void selectObjectType(final String objectType) {
        if (objectType != null) {
            getActions().select(objectTypeDropDown, objectType);
            LOGGER.info("Object type Selected: " + objectType);
        }
    }
}
