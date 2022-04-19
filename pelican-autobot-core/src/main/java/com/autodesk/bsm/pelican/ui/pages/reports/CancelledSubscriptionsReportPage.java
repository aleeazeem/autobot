package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for Admin Tool's Cancelled Subscription Report - on the main tab
 *
 * @author Sunitha
 */
public class CancelledSubscriptionsReportPage extends GenericGrid {

    public CancelledSubscriptionsReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-createdAfter")
    private WebElement creationDateAfterInput;

    @FindBy(id = "input-createdBefore")
    private WebElement creationDateBeforeInput;

    @FindBy(id = "input-cancelledAfter")
    private WebElement cancelledDateAfterInput;

    @FindBy(id = "input-cancelledBefore")
    private WebElement cancelledDateBeforeInput;

    @FindBy(id = "action")
    private WebElement viewOrDownloadSelect;

    @FindBy(className = "submit")
    private WebElement submitButton;

    @FindBy(css = "#subnav > ul > li > span")
    private WebElement reportContainer;

    @FindBy(css = "#bd > div:last-child")
    private WebElement reportData;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(CancelledSubscriptionsReportPage.class.getSimpleName());

    /**
     * Method to set Creation Date After.
     */
    private void setCreationDateAfterInput(final String creationDateAfterInput) {
        getActions().setText(this.creationDateAfterInput, creationDateAfterInput);
        LOGGER.info("Creation date after is set to '" + creationDateAfterInput + "'");
    }

    /**
     * Method to set Creation Date before.
     */
    private void setCreationDateBeforeInput(final String creationDateBeforeInput) {
        getActions().setText(this.creationDateBeforeInput, creationDateBeforeInput);
        LOGGER.info("Creation date before is set to '" + creationDateBeforeInput + "'");
    }

    /**
     * Method to set cancellation date after.
     */
    private void setCanellationDateAfterInput(final String canellationDateAfterInput) {
        getActions().setText(cancelledDateAfterInput, canellationDateAfterInput);
        LOGGER.info("Cancellation date after is set to '" + canellationDateAfterInput + "'");
    }

    /**
     * Method to set cancellation date before.
     */
    private void setCanellationDateBeforeInput(final String canellationDateBeforeInput) {
        getActions().setText(cancelledDateBeforeInput, canellationDateBeforeInput);
        LOGGER.info("Cancellation date before is set to '" + canellationDateBeforeInput + "'");
    }

    /**
     * Method to set filters to generate report.
     *
     * @param action
     * @param creationDateAfterInput
     * @param creationDateBeforeInput
     * @param canellationDateAfterInput
     * @param canellationDateBeforeInput
     * @return cancelled subscription report result page
     */
    public CancelledSubscriptionsReportResultsPage generateReport(final String action,
        final String creationDateAfterInput, final String creationDateBeforeInput,
        final String canellationDateAfterInput, final String canellationDateBeforeInput) {
        navigateToPage();
        if (creationDateAfterInput != null) {
            setCreationDateAfterInput(creationDateAfterInput);
        }
        if (creationDateBeforeInput != null) {
            setCreationDateBeforeInput(creationDateBeforeInput);
        }
        if (canellationDateAfterInput != null) {
            setCanellationDateAfterInput(canellationDateAfterInput);
        }
        if (canellationDateBeforeInput != null) {
            setCanellationDateBeforeInput(canellationDateBeforeInput);
        }
        selectViewDownloadAction(action);
        submit(TimeConstants.ONE_SEC);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return super.getPage(CancelledSubscriptionsReportResultsPage.class);
    }

    /**
     * Method to set filters for error scenarios.
     *
     * @param action
     * @param creationDateAfterInput
     * @param creationDateBeforeInput
     * @param canellationDateAfterInput
     * @param canellationDateBeforeInput
     * @return cancelled subscription report page
     */
    public CancelledSubscriptionsReportPage generateReportWithErrors(final String action,
        final String creationDateAfterInput, final String creationDateBeforeInput,
        final String canellationDateAfterInput, final String canellationDateBeforeInput) {
        generateReport(action, creationDateAfterInput, creationDateBeforeInput, canellationDateAfterInput,
            canellationDateBeforeInput);
        return super.getPage(CancelledSubscriptionsReportPage.class);
    }

    /**
     * Method to navigate to the page.
     */
    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        checkPageLoaded(TimeConstants.THREE_SEC);
    }

    /**
     * Method to get URL.
     */
    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/reports/" + AdminPages.CANCELLED_SUBSCRIPTION_REPORT.getForm();
    }
}
