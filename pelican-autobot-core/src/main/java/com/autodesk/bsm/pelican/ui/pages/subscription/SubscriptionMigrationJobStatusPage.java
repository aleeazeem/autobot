package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Page class for Subscription Migration job status (Sherpa).
 *
 * @author jains
 */

public class SubscriptionMigrationJobStatusPage extends GenericGrid {

    public SubscriptionMigrationJobStatusPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionMigrationJobStatusPage.class.getSimpleName());

    @FindBy(id = "jobStatus")
    private WebElement jobStatusSelect;

    @FindBy(id = "input-jobId")
    private WebElement jobIdInput;

    @FindBy(id = "input-runDate")
    private WebElement runDateInput;

    @FindBy(id = "input-runBy")
    private WebElement runByInput;

    @FindBy(id = "input-createdBy")
    private WebElement createdByInput;

    private static final String UPDATE_TABLE_NAME = "update subscription_migration_job set status= ";
    private static final String UPDATE_CONDITION = " where id= ";
    private static final String APP_FAMILY_ID_IN_QUERY = " and app_family_id = ";

    private void selectJobStatus(final String jobStatus) {
        getActions().select(jobStatusSelect, jobStatus);
        LOGGER.info("Job status selected: " + jobStatus);
    }

    private void setRunDate(final String runDate) {
        getActions().setText(runDateInput, runDate);
        LOGGER.info("Run date set: " + runDate);
    }

    private void setRunBy(final String runBy) {
        getActions().setText(runByInput, runBy);
        LOGGER.info("Run by set: " + runBy);
    }

    private void setCreatedBy(final String createdBy) {
        getActions().setText(createdByInput, createdBy);
        LOGGER.info("Created by set: " + createdBy);
    }

    /**
     * Method to set the id.
     *
     * @param id
     */
    @Override
    public void setId(final String id) {
        getActions().setText(jobIdInput, id);
        LOGGER.info("Id set to: " + id);
    }

    /**
     * Method to get migration job status with filters.
     *
     * @param jobStatus
     * @param id
     * @param runDate
     * @param runBy
     * @param createdBy
     * @return
     */
    public SubscriptionMigrationJobStatusPage getMigrationJobStatusWithFilters(final String jobStatus, final String id,
        final String runDate, final String runBy, final String createdBy) {
        navigateToSubscriptionMigrationJobStatusPage();
        if (jobStatus != null) {
            selectJobStatus(jobStatus);
        }

        setId(id);
        setRunDate(runDate);
        setRunBy(runBy);
        setCreatedBy(createdBy);
        submit(TimeConstants.ONE_SEC);
        return super.getPage(SubscriptionMigrationJobStatusPage.class);
    }

    /**
     * Method to navigate to subscription migration job status page.
     */
    public void navigateToSubscriptionMigrationJobStatusPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm() + "/"
            + AdminPages.SUBSCRIPTION_MIGRATION_JOB_STATUS.getForm();
        getDriver().get(url);
        LOGGER.info("Navigated to page: " + url);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Method to get all values from id column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromIdColumn() {
        return getColumnValues(PelicanConstants.ID_FIELD);
    }

    /**
     * Method to get all values from name column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromNameColumn() {
        return getColumnValues(PelicanConstants.NAME_FIELD);
    }

    /**
     * Method to get all values from Status column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromStatusColumn() {
        return getColumnValues(PelicanConstants.STATUS_FIELD);
    }

    /**
     * Method to get all values from Created Date column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromCreatedDateColumn() {
        return getColumnValues(PelicanConstants.CREATED_DATE);
    }

    /**
     * Method to get all values from Created By column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromCreatedByColumn() {
        return getColumnValues(PelicanConstants.CREATED_BY_FIELD);
    }

    /**
     * Method to get all values from Last Modified Date column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromLastModifiedDateColumn() {
        return getColumnValues(PelicanConstants.LAST_MODIFIED_DATE_FIELD);
    }

    /**
     * Method to get all values from Last Modified By column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromLastModifiedByColumn() {
        return getColumnValues(PelicanConstants.LAST_MODIFIED_BY_FIELD);
    }

    /**
     * Method to get all values from Run Date column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromRunDateColumn() {
        return getColumnValues(PelicanConstants.RUN_DATE);
    }

    /**
     * Method to get all values from run by column.
     *
     * @return List<String>
     */
    public List<String> getValuesFromRunByColumn() {
        return getColumnValues(PelicanConstants.RUN_BY);
    }

    /**
     * Method to change status of Migration Job from Database
     *
     * @param jobStatus
     * @param jobId
     */
    public void updateMigrationJobStatus(final String jobStatus, final String jobId) {
        String updateQuery;
        // update job status in database
        updateQuery = UPDATE_TABLE_NAME + jobStatus + UPDATE_CONDITION + jobId + APP_FAMILY_ID_IN_QUERY
            + environmentVariables.getAppFamilyId();
        DbUtils.updateQuery(updateQuery, environmentVariables);
        LOGGER.info("Status updated for Job with ID: " + jobId + " to " + jobStatus);
        refreshPage();
    }

    /**
     * method to delete the migration job
     *
     * @param jobId
     */
    public void deleteJob(final String jobId) {
        LOGGER.info("Deleting a migration Job with ID: " + jobId);
        deleteAndConfirm();
    }

}
