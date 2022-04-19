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
 * Page object represent DR Migrtation Report Page.
 *
 * @author Muhammad Azeem
 */
public class PendingMigrationSubscriptionsReportPage extends GenericDetails {

    public PendingMigrationSubscriptionsReportPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@id='bd']/h1")
    private WebElement drMigrationCustomerReportTitle;

    // find Generate Report button
    @FindBy(className = "submit")
    private WebElement submitButton;

    @FindBy(css = "#bd>ul>li:nth-child(1)")
    private WebElement totalSubscriptionUploaded;

    @FindBy(css = "#bd>ul>li:nth-child(2)")
    private WebElement totalSubscriptionNotConverted;

    @FindBy(css = "#bd>ul>li:nth-child(3)")
    private WebElement percentageOfSubscriptionConverted;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(PendingMigrationSubscriptionsReportPage.class.getSimpleName());

    /**
     * Method to navigate to DR Migration Report page
     */
    public void navigateToDrMigrationReportPage() {
        final String drMigrationReport = getEnvironment().getAdminUrl() + "/reports/"
            + AdminPages.PENDING_MIGRATION_SUBSCRIPTIONS_REPORT.getForm() + "FindForm";
        if (!isPageValid(drMigrationReport)) {
            getDriver().get(drMigrationReport);
        }
    }

    /**
     * @return the presence of Title Page
     */
    public boolean isTitlePagePresent() {
        boolean titlePage = false;
        try {
            titlePage = drMigrationCustomerReportTitle.isDisplayed();
            LOGGER.info("Report Generated");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Report is not Generated");
        }
        return titlePage;
    }

    /**
     * This method returns a complete text of total number of subscriptions uploaded including text which is Displayed
     * in Subscription In Pending Migration Status - Report below the Title of the Page
     *
     * @return totalSubsUploaded - complete text of total subscriptions uploaded
     */
    public String getTotalSubsUploaded() {
        String totalSubsUploaded = null;
        try {
            totalSubsUploaded = totalSubscriptionUploaded.getText();
            LOGGER.info("Total Number of Subscriptions Uploaded is shown");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Total Number of Subscriptions Uploaded is not shown");
        }
        return totalSubsUploaded;
    }

    /**
     * This method returns a complete number of not converted subscription including text which is Displayed in
     * Subscription in Pending Migration Status - Report below the Title of the Page
     *
     * @return notConvertedSubscriptions - text of total subscriptions uploaded which are in state of Pending Migration
     */
    public String getTotalPendingMigrationSubs() {
        String notConvertedSubscriptions = null;
        try {
            notConvertedSubscriptions = totalSubscriptionNotConverted.getText();
            LOGGER.info("Total Number of Subscriptions with status of Pending Migration");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Total Number of Subscriptions with status of Pending Migration is not correct");
        }
        return notConvertedSubscriptions;
    }

    /**
     * This method returns a percentage fo converted subscriptions including text which is Displaye in Subscription in
     * Pending Migration Status - Report below the Title of the Page
     *
     * @return percentageOfSubsCoverted - complete text with percentage of converted subscriptions
     */
    public String getPercentageOfConvertedSubs() {
        String percentageOfSubsCoverted = null;
        try {
            percentageOfSubsCoverted = percentageOfSubscriptionConverted.getText();
            LOGGER.info(percentageOfSubsCoverted + " is Displayed");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Total Subscriptions Uploaded is not Displayed");
        }
        return percentageOfSubsCoverted;
    }

    /**
     * method to click on generate report button
     */
    public void generateReportButton() {
        LOGGER.info("Click Generate Report button.");
        getActions().click(submitButton);
    }
}
