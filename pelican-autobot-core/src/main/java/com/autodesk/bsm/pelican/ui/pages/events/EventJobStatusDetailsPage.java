package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;

/**
 * Page object model for Event Job Status Details Page
 *
 * @author Muhammad
 *
 */
public class EventJobStatusDetailsPage extends GenericDetails {

    public EventJobStatusDetailsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * This method returns id of job.
     *
     * @return id
     */
    public String getId() {
        return getValueByField(PelicanConstants.ID_FIELD);
    }

    /**
     * This method returns created date of job.
     *
     * @return created
     */
    public String getCreated() {
        return getValueByField(PelicanConstants.CREATED_FIELD);

    }

    /**
     * This method returns lastModified of job.
     *
     * @return created
     */
    public String getLastModified() {
        return getValueByField("LastModified");

    }

    /**
     * This method returns category of job.
     *
     * @return category
     */
    public String getCategory() {
        return getValueByField(PelicanConstants.CATEGORY);

    }

    /**
     * This method returns status of job.
     *
     * @return status
     */
    public String getStatus() {
        return getValueByField(PelicanConstants.STATUS_FIELD);
    }

    /**
     * This method returns batch size of job.
     *
     * @return batchSize
     */
    public String getBatchSize() {
        return getValueByField(PelicanConstants.BATCH_SIZE);
    }

    /**
     * This method returns step count of job.
     *
     * @return stepCount
     */
    public String getStepCount() {
        return getValueByField(PelicanConstants.STEP_COUNT);
    }

    /**
     * This method returns processed records count of job.
     *
     * @return processedRecordsCount
     */
    public String getProcessedRecordsCount() {
        return getValueByField(PelicanConstants.PROCESSED_RECORDS_COUNT);
    }

    /**
     * This method returns rollback records count of job.
     *
     * @return rollbackRecordsCount
     */
    public String getRollbackRecordsCount() {
        return getValueByField(PelicanConstants.ROLL_BACK_RECORDS_COUNT);
    }

    /**
     * This method returns skipped records count of job.
     *
     * @return skippedRecordsCount
     */
    public String getSkippedRecordsCount() {
        return getValueByField(PelicanConstants.SKIPPED_RECORDS_COUNT);
    }

    /**
     * This method returns Errors count of job.
     *
     * @return errors
     */
    public String getErrors() {
        return getValueByField(PelicanConstants.ERRORS_FIELD);
    }
}
