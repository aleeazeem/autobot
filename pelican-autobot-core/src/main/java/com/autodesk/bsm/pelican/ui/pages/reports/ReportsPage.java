package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains web elements on Reports page in admin tool.
 *
 * @author jains
 */
public class ReportsPage extends GenericDetails {
    public ReportsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        // TODO Auto-generated constructor stub
    }

    @FindBy(linkText = "Orders in EC Hold Report")
    private WebElement ordersInEcHoldReportLink;

    @FindBy(linkText = "EC Statistics Report")
    private WebElement ecStatisticsReportLink;

    @FindBy(id = "subnav-link-ec-hold-report-generate")
    private WebElement ordersInEcHoldReportSubNavigationLink;

    @FindBy(id = "subnav-link-ec-statistics-report-generate")
    private WebElement ecStatisticsReportSubNavigationLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsPage.class.getSimpleName());

    /**
     * This method checks whether Orders In EC Hold Report Link is present Reports page or not.
     *
     * @return boolean
     */
    public boolean isOrdersInEcHoldReportLinkDisplayed() {
        return isElementPresent(ordersInEcHoldReportLink);
    }

    /**
     * This method checks whether EC Statistics Report Link is present on Reports page or not.
     *
     * @return boolean
     */
    public boolean isEcStatisticsReportDisplayed() {
        return isElementPresent(ecStatisticsReportLink);
    }

    /**
     * This method checks whether Orders In EC Hold Report Link is present under Purchase Order menu or not
     *
     * @return boolean
     */
    public boolean isOrdersInEcHoldReportSubNavigationDisplayed() {
        return isElementPresent(ordersInEcHoldReportSubNavigationLink);
    }

    /**
     * This method checks whether EC Statistics Report Link is present under Purchase Order menu or not.
     *
     * @return boolean
     */
    public boolean isEcStatisticsReportSubNavigationDisplayed() {
        return isElementPresent(ecStatisticsReportSubNavigationLink);
    }

    /**
     * This method navigates to the Reports page.
     */
    public void navigateToReportsPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.REPORTS.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

}
