package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Page object for Admin Tool's Cancelled Subscription Result Report Page
 *
 * @author Muhammad
 */
public class CancelledSubscriptionsReportResultsPage extends GenericGrid {
    public CancelledSubscriptionsReportResultsPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER =
        LoggerFactory.getLogger(CancelledSubscriptionsReportResultsPage.class.getSimpleName());

    /**
     * Method to get column values of auto subscription id field.
     *
     * @return List
     */
    public List<String> getColumnValuesOfSubscriptionId() {
        return getColumnValues(PelicanConstants.SUBSCRIPTION_ID_FIELD);
    }

    /**
     * Method to get column values of subscription owner field.
     *
     * @return List
     */
    public List<String> getColumnValuesOfSubscriptionOwner() {
        return getColumnValues(PelicanConstants.SUBSCRIPTION_OWNER_FIELD);
    }

    /**
     * Method to get column values of auto renew field.
     *
     * @return List
     */
    public List<String> getColumnValuesOfAutoRenew() {
        return getColumnValues(PelicanConstants.AUTO_RENEW_FIELD);
    }

    /**
     * Method to get column values of subscription status.
     *
     * @return List
     */
    public List<String> getColumnValuesOfSubscriptionStatus() {
        return getColumnValues(PelicanConstants.SUBSCRIPTION_STATUS_FIELD);
    }

    /**
     * Method to get column values of cancelled date.
     *
     * @return List
     */
    public List<String> getColumnValuesOfCancelledDate() {
        return getColumnValues(PelicanConstants.CANCELLED_DATE_FIELD);
    }

    /**
     * Method to get column values of expiration date.
     *
     * @return List
     */
    public List<String> getColumnValuesOfExpirationDate() {
        return getColumnValues(PelicanConstants.EXPIRATION_DATE_FIELD);
    }

    /**
     * Method to get column values of requester.
     *
     * @return List
     */
    public List<String> getColumnValuesOfRequestor() {
        return getColumnValues(PelicanConstants.REQUESTOR_FIELD);
    }
}
