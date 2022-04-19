package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * This is the page object for User Access Report result page.
 *
 * @author jains
 */
public class UserAccessReportResultPage extends GenericGrid {

    public UserAccessReportResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to get all values in user column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromUserColumn() {
        return getColumnValues(PelicanConstants.USER);
    }

    /**
     * Method to get all values in created column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromCreatedColumn() {
        return getColumnValues(PelicanConstants.CREATED);
    }

    /**
     * Method to get all values in created by column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromCreatedByColumn() {
        return getColumnValues(PelicanConstants.CREATED_BY_FIELD);
    }

    /**
     * Method to get all values in last modified column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromModifiedColumn() {
        return getColumnValues(PelicanConstants.LAST_MODIFIED_FIELD);
    }

    /**
     * Method to get all values in last mdified by column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromModifiedByColumn() {
        return getColumnValues(PelicanConstants.LAST_MODIFIED_BY_FIELD);
    }

    /**
     * Method to get all values in application family column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromApplicationFamilyColumn() {
        return getColumnValues(PelicanConstants.APPLICATION_FAMILY);
    }

    /**
     * Method to get all values in state column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromStateColumn() {
        return getColumnValues(PelicanConstants.STATE);
    }

    /**
     * Method to get all values in roles column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromRolesColumn() {
        return getColumnValues(PelicanConstants.ROLES);
    }

    /**
     * Method to return user name from user column
     *
     * @return String (userName)
     */
    public String getUserNameFromUserColumn(final String userColumnData) {
        // userColumnData is constructed with user name and user id e.g.
        // username (userid)
        return userColumnData.split(" ")[0];
    }

    /**
     * Method to return user id from user column
     *
     * @return String (userId)
     */
    public String getUserIdFromUserColumn(final String userColumnData) {
        // userColumnData is constructed with username and user id e.g. username
        // (userid)
        return userColumnData.split(" ")[1].replaceAll("[(,)]", "");
    }

}
