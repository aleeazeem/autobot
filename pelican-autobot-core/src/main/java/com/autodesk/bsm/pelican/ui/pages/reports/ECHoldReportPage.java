package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object for Export Control Report Page in Pelican admin tool This page can be viewed in pelican Admin
 * in Reports tab / Purchase Order Reports >Order in EC hold Reports link Web element objects created may not be
 * complete. If missing any web element which is required for your testing please add them.
 *
 * @author Muhammad
 */
public class ECHoldReportPage extends GenericDetails {

    public ECHoldReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-purchasedAfter")
    private WebElement purchasedDateAfterInput;

    @FindBy(id = "input-purchasedBefore")
    private WebElement purchasedDateBeforeInput;

    @FindBy(name = "includeUnverified")
    private WebElement includeUnverified;

    @FindBy(name = "includeBlock")
    private WebElement includeBlock;

    @FindBy(name = "includeReview")
    private WebElement includeReview;

    @FindBy(name = "includeReopen")
    private WebElement includeReopen;

    @FindBy(name = "includeHardBlock")
    private WebElement includeHardBlock;

    @FindBy(xpath = ".//*[@id='field-purchasedAfter-purchasedBefore']/span")
    private WebElement dateErrorMessage;

    @FindBy(xpath = ".//*[@id='field-status']/span")
    private WebElement statusErrorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(ECHoldReportPage.class.getSimpleName());

    /**
     * method check include unverified option
     */
    private void includeUnverifiedCheck() {
        if (includeUnverified.getAttribute("checked") == null) {
            includeUnverified.click();
        } else {
            LOGGER.info("Checkbox *Include Unverified* is already checked.");
        }
    }

    /**
     * method uncheck include unverified option
     */
    private void includeUnverifiedUncheck() {
        if (includeUnverified.getAttribute("checked") != null) {
            includeUnverified.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Include Unverified* is already unchecked.");
        }
    }

    /**
     * method check include block option
     */
    private void includeBlockCheck() {
        if (includeBlock.getAttribute("checked") == null) {
            includeBlock.click();
        } else {
            LOGGER.info("Checkbox *Include Block* is already checked.");
        }
    }

    /**
     * method uncheck include block option
     */
    private void includeBlockUncheck() {
        if (includeBlock.getAttribute("checked") != null) {
            includeBlock.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Include Block* is already unchecked.");
        }
    }

    /**
     * method check include review option
     */
    private void includeReviewCheck() {
        if (includeReview.getAttribute("checked") == null) {
            includeReview.click();
        } else {
            LOGGER.info("Checkbox *Include Review* is already checked.");
        }
    }

    /**
     * method uncheck include review option
     */
    private void includeReviewUncheck() {
        if (includeReview.getAttribute("checked") != null) {
            includeReview.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Include Review* is already unchecked.");
        }
    }

    /**
     * method check include reopen option
     */
    private void includeReopenCheck() {
        if (includeReopen.getAttribute("checked") == null) {
            includeReopen.click();
        } else {
            LOGGER.info("Checkbox *Include Reopen* is already checked.");
        }
    }

    /**
     * method uncheck include review option
     */
    private void includeReopenUncheck() {
        if (includeReopen.getAttribute("checked") != null) {
            includeReopen.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Include Reopen* is already unchecked.");
        }
    }

    /**
     * method check include hardBlockCheck option
     */
    private void includeHardBlockCheck() {
        if (includeHardBlock.getAttribute("checked") == null) {
            includeHardBlock.click();
        } else {
            LOGGER.info("Checkbox *Include Reopen* is already checked.");
        }
    }

    /**
     * method uncheck include review option
     */
    private void includeHardBlockUncheck() {
        if (includeHardBlock.getAttribute("checked") != null) {
            includeHardBlock.click();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            LOGGER.info("Checkbox *Include Hard Block* is already unchecked.");
        }
    }

    /**
     * Method to set order after date
     */
    private void purchaseAfterDate(final String orderAfterDate) {
        LOGGER.info("Order before date '" + orderAfterDate + "'");
        getActions().setText(purchasedDateAfterInput, orderAfterDate);
    }

    /**
     * Method to set order before date
     */
    private void purchaseBeforeDate(final String orderBeforeDate) {
        LOGGER.info("Order before date '" + orderBeforeDate + "'");
        getActions().setText(purchasedDateBeforeInput, orderBeforeDate);
    }

    /**
     * Method to clear order after date
     */
    public void purchaseAfterDateClear() {
        LOGGER.info("Clear Date After");
        purchasedDateAfterInput.clear();
    }

    /**
     * Method to clear order before date
     */
    public void purchaseBeforeDateClear() {
        LOGGER.info("Clear Date Before");
        purchasedDateBeforeInput.clear();
    }

    /**
     * Method to get url of orders in export control hold report page.
     *
     * @return string (page url)
     */
    private String getExportControlReportUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.ORDERS_IN_EXPORT_CONTROL_HOLD_REPORT.getForm();
    }

    /**
     * Method to navogate to the page of orders in export control hold report
     */
    public void navigateToExportControlReportPage() {
        final String url = getExportControlReportUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to set filters to generate a report
     *
     * @return subscription grid
     */
    public void getReportWithSelectedFilters(final String store, final String dateAfter, final String dateBefore,
        final String ecStatus) {
        navigateToExportControlReportPage();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        if (store != null) {
            selectStore(store);
        }
        purchaseAfterDate(dateAfter);
        purchaseBeforeDate(dateBefore);

        if (ecStatus != null) {
            selectEcStatus(ecStatus);
        }
        submit(TimeConstants.ONE_SEC);
    }

    /**
     * Method to select ecStatus
     */
    private void selectEcStatus(final String ecStatus) {
        uncheckAllStatuses();
        LOGGER.info("Set Include Export Control Status: '" + ecStatus + "'");
        if (ecStatus.equals("Include Unverified")) {
            includeUnverifiedCheck();
        }
        if (ecStatus.equals("Include Block")) {
            includeBlockCheck();
        }
        if (ecStatus.equals("Include Review")) {
            includeReviewCheck();
        }
        if (ecStatus.equals("Include Reopen")) {
            includeReopenCheck();
        }
        if (ecStatus.equals("Include Hard Block")) {
            includeHardBlockCheck();
        }
    }

    /**
     * Method to uncheck all filters
     */
    public void uncheckAllStatuses() {
        includeUnverifiedUncheck();
        includeBlockUncheck();
        includeReviewUncheck();
        includeReopenUncheck();
        includeHardBlockUncheck();
    }

    /**
     * Method to return error of date Range
     *
     * @return Title
     */
    public String getDateError() {
        String error = null;
        try {
            error = dateErrorMessage.getText();
            LOGGER.info("Error is generated");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Error is not generated");
        }
        return error;
    }

    /**
     * Method to return an error
     *
     * @return error
     */
    public String getStatusErrorMessage() {
        String error = null;
        try {
            error = statusErrorMessage.getText();
            LOGGER.info("Error is generated");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Error is not generated");
        }
        return error;
    }
}
