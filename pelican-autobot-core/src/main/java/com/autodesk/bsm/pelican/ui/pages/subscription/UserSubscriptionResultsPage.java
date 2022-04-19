package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

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
public class UserSubscriptionResultsPage extends GenericGrid {
    public UserSubscriptionResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSubscriptionResultsPage.class.getSimpleName());

    /**
     * Method to get values of column ID.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfID() {
        return getColumnValues(PelicanConstants.ID_FIELD);
    }

    /**
     * Method to get values of column external key.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfExternalKey() {
        return getColumnValues(PelicanConstants.EXTERNAL_KEY_FIELD);
    }

    /**
     * Method to get values of column store.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfStore() {
        return getColumnValues(PelicanConstants.STORE_FIELD);
    }

    /**
     * Method to get values of column payment method.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfPaymentMethod() {
        return getColumnValues(PelicanConstants.PAYMENT_METHOD);
    }

    /**
     * Method to get values of column credit card type.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfCreditCardType() {
        return getColumnValues(PelicanConstants.CREDIT_CARD_TYPE);
    }

    /**
     * Method to get values of column subscription offer.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfSubscriptionOffer() {
        return getColumnValues(PelicanConstants.SUBSCRIPTION_OFFER_FIELD);
    }

    /**
     * Method to get values of column billing period.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfBillingPeriod() {
        return getColumnValues(PelicanConstants.BILLING_PERIOD);
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
     * Method to get values of column current quantity.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfCurrentQuantity() {
        return getColumnValues(PelicanConstants.CURRENT_QUANTITY);
    }

    /**
     * Method to get values of column renewal quantity.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfRenewalQuantity() {
        return getColumnValues(PelicanConstants.RENEWAL_QUANTITY);
    }

    /**
     * Method to get values of column next billing date.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfNextBillingDate() {
        return getColumnValues(PelicanConstants.NEXT_BILLING_DATE_FIELD);
    }

    /**
     * Method to get values of column next billing price.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfNextBillingPrice() {
        return getColumnValues(PelicanConstants.NEXT_BILLING_PRICE);
    }

    /**
     * Method to get values of column created.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfCreated() {
        return getColumnValues(PelicanConstants.CREATED);
    }

    /**
     * Method to get values of column expiration date.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfExpirationDate() {
        return getColumnValues(PelicanConstants.EXPIRATION_DATE_FIELD);
    }

    /**
     * Method to get values of column created.
     *
     * @return List<String>
     */
    public List<String> getColumnValuesOfSalesChannel() {
        return getColumnValues(PelicanConstants.SALES_CHANNEL);
    }

    /**
     * Method to select subscription from user subscription result page
     *
     * @param row
     * @param wait
     * @return subscription detail page
     */
    public SubscriptionDetailPage selectSubscriptionFromGrid(final int row) {
        selectResultRow(row);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return getPage(SubscriptionDetailPage.class);
    }
}
