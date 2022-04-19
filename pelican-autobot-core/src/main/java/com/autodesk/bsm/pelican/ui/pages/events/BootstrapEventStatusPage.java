package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;

public class BootstrapEventStatusPage extends GenericDetails {

    public BootstrapEventStatusPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final String PROCESSED_RECORDS_COUNT = "Processed Records Count";
    private static final String ROLLBACK_RECORDS_COUNT = "Rollback Records Count";
    private static final String SKIPPED_RECORDS_COUNT = "Skipped Records Count";

    /**
     * This is a method to return the id field on the event status page
     *
     * @return id
     */
    public String getId() {
        return getFieldValueByKey(PelicanConstants.ID_FIELD);
    }

    /**
     * This is a method to return the category field on the event status page
     *
     * @return category
     */
    public String getCategory() {
        return getFieldValueByKey(PelicanConstants.CATEGORY);
    }

    /**
     * This is a method to return the status field on the event status page
     *
     * @return status
     */
    public String getStatus() {
        return getFieldValueByKey(PelicanConstants.STATUS_FIELD);
    }

    /**
     * This is a method to return the processed records count field on the event status page
     *
     * @return ProcessedRecordsCount
     */
    public String getProcessedRecordsCount() {
        return getFieldValueByKey(PROCESSED_RECORDS_COUNT);
    }

    /**
     * This is a method to return the rollback records count field on the event status page
     *
     * @return RollbackRecordsCount
     */
    public String getRollbackRecordsCount() {
        return getFieldValueByKey(ROLLBACK_RECORDS_COUNT);
    }

    /**
     * This is a method to return the skipped records count field on the event status page
     *
     * @return SkippedRecordsCount
     */
    public String getSkippedRecordsCount() {
        return getFieldValueByKey(SKIPPED_RECORDS_COUNT);
    }

    /**
     * This is a method to return the errors on the event status page
     *
     * @return errors
     */
    public String getErrors() {
        return getFieldValueByKey(PelicanConstants.ERRORS_FIELD);
    }
}
