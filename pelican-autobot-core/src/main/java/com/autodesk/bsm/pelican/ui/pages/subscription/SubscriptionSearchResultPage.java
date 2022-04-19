package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class represents the page object of Subscription search result page. This page can be viewed in AT from
 * Subscriptions tab / subscriptions --> Find Subscriptions --> findById(without giving any id) or advancedFind
 *
 * @author Muhammad
 */
public class SubscriptionSearchResultPage extends GenericGrid {
    public SubscriptionSearchResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionSearchResultPage.class.getSimpleName());

    /*
     * This method selects subscription randomly from first page
     */
    public SubscriptionDetailPage selectSubscriptionRandomly() {
        final int totalSubscriptions = getTotalItems();
        LOGGER.info("Total number of subscriptions are: " + totalSubscriptions);
        final int selectedRow = selectRowRandomlyFromFirstPage(totalSubscriptions);
        selectResultRow(selectedRow);
        return getPage(SubscriptionDetailPage.class);
    }

    /*
     * Method to get values of column Id.
     */
    public List<String> getColumnValueOfId() {
        return getColumnValues(PelicanConstants.ID_FIELD);
    }

    /*
     * Method to get values of column user.
     */
    public List<String> getColumnValuesOfUser() {
        return getColumnValues(PelicanConstants.USER_FIELD);
    }

    /*
     * Method to get values of column status.
     */
    public List<String> getColumnValuesOfStatus() {
        return getColumnValues(PelicanConstants.STATUS_FIELD);
    }

    /*
     * Method to get values of column subscription plan.
     */
    public List<String> getColumnValuesOfSubscriptionPlan() {
        return getColumnValues(PelicanConstants.SUBSCRIPTION_PLAN_FIELD);
    }

    /*
     * Method to get values of column ec status.
     */

    public List<String> getColumnValuesOfECStatus() {
        return getColumnValues(PelicanConstants.EC_STATUS_FIELD);
    }

}
