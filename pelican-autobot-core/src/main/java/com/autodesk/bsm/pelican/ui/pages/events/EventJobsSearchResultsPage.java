package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Page object model for event jobs search page.
 *
 * @author Muhammad
 *
 */
public class EventJobsSearchResultsPage extends GenericGrid {

    public EventJobsSearchResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to get values of column job id.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfJobId() {
        return getColumnValues(PelicanConstants.JOB_ID);
    }

    /**
     * Method to get values of column category.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfCategory() {
        return getColumnValues(PelicanConstants.CATEGORY);
    }

    /**
     * Method to get values of column status.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfStatus() {
        return getColumnValues(PelicanConstants.STATUS_FIELD);
    }

    /**
     * Method to get values of column created date.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfCreatedDate() {
        return getColumnValues(PelicanConstants.CREATED_DATE);
    }

    /**
     * Method to get values of column last modified date.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfLastModifiedDate() {
        return getColumnValues(PelicanConstants.LAST_MODIFIED_DATE_FIELD);
    }
}
