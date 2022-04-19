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
 * Page Object for the EC Statistics Report which can be seen under Reports --> Purchase Order Reports
 *
 * @author vineel
 */
public class ECStatisticsReportPage extends GenericDetails {

    public ECStatisticsReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-purchasedAfter")
    private WebElement orderStartDateInput;

    @FindBy(id = "input-purchasedBefore")
    private WebElement orderEndDateInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(ECStatisticsReportPage.class.getSimpleName());

    /**
     * This is a method which will navigate to the EC Statistics Report Page
     */
    private void navigateToReportPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.REPORTS.getForm() + "/"
            + AdminPages.EXPORT_CONTROL_STATISTICS_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        // Please don't remove mini wait added otherwise tests will fail on stage.
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

    }

    /**
     * This is the method to set the order start date and order end dates in the report
     */
    private void setOrderDates(final String orderStartDate, final String orderEndDate) {
        getActions().setText(orderStartDateInput, orderStartDate);
        LOGGER.info("order start date is set: " + orderStartDate);
        getActions().setText(orderEndDateInput, orderEndDate);
        LOGGER.info("Order end date is set: " + orderEndDate);
    }

    /**
     * This is a method which will select the view action on the report and generate the report
     */
    public void viewOrDownloadReport(final String store, final String orderStartDate, final String orderEndDate,
        final String action) {
        navigateToReportPage();
        selectStore(store);
        setOrderDates(orderStartDate, orderEndDate);
        selectViewDownloadAction(action);
        submit(TimeConstants.TWO_SEC);
    }
}
