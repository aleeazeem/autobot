package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is the page class for the DR Subscription Migration Results Page
 *
 * @author yerragv
 */
public class DRSubscriptionMigrationResultsPage extends GenericGrid {

    public DRSubscriptionMigrationResultsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(className = "find-results-hd-inner")
    private WebElement resultsField;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DRSubscriptionMigrationResultsPage.class.getSimpleName());

    /**
     * This is a method to return the list of column headers in the report.
     *
     * @param adminToolPage
     * @return column headers as List<String>
     */
    public List<String> getColumnHeaders(final AdminToolPage adminToolPage) {

        return getColumnHeaders();
    }

    /**
     * This is the method to return the column values from the ID field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromIdColumn() {
        return getColumnValues(PelicanConstants.ID_FIELD);
    }

    /**
     * This is the method to return the column values from the external key field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromExternalKeyColumn() {
        return getColumnValues(PelicanConstants.EXTERNAL_KEY_FIELD);
    }

    /**
     * This is the method to return the column values from the status field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromStatusColumn() {
        return getColumnValues(PelicanConstants.STATUS_FIELD);
    }

    /**
     * This is the method to return the column values from the user field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromUserColumn() {
        return getColumnValues(PelicanConstants.USER_FIELD);
    }

    /**
     * This is the method to return the column values from the plan field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanColumn() {
        return getColumnValues(PelicanConstants.PLAN_FIELD);
    }

    /**
     * This is the method to return the column values from the offer field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferColumn() {
        return getColumnValues(PelicanConstants.OFFER_FIELD);
    }

    /**
     * This is the method to return the column values from the store field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromStoreColumn() {
        return getColumnValues(PelicanConstants.STORE_FIELD);
    }

    /**
     * This is the method to return the column values from the Seats field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromSeatsColumn() {
        return getColumnValues(PelicanConstants.SEATS_FIELD);
    }

    /**
     * This is the method to return the column values from the Next Billing Date field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromNextBillingDateColumn() {
        return getColumnValues(PelicanConstants.NEXT_BILLING_DATE_FIELD);
    }

    /**
     * This is the method to return the column values from the Next Billing Amount field.
     *
     * @return List<String>
     */
    public List<String> getValuesFromNextBillingAmountColumn() {
        return getColumnValues(PelicanConstants.NEXT_BILLING_AMOUNT_FIELD);
    }

    /**
     * This is a method to return the count of total results on page
     *
     * @return String
     */
    public String getTotalNumberOfResultsOnPage() {
        LOGGER.info("Getting the count of records on page");
        return (resultsField.getText());
    }
}
