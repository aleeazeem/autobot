package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page class for DR Subscription Migration Generate Report Page
 *
 * @author yerragv
 */
public class DRSubscriptionMigrationGenerateReportPage extends GenericDetails {

    public DRSubscriptionMigrationGenerateReportPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "status")
    private WebElement statusSelect;

    @FindBy(id = "action")
    private WebElement actionSelect;

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DRSubscriptionMigrationGenerateReportPage.class.getSimpleName());

    /**
     * This is a method to navigate to the DR SubscriptionMigration Generate Report Page.
     */
    private void navigateToGenerateReportPage() {

        final String url = getEnvironment().getAdminUrl() + "/reports/" + AdminPages.DR_MIGRATION_REPORT.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * This is a method to view the DR Subscription Migration Report in admin tool.
     *
     * @param status - status of the subscription in migration process.
     * @param action - action on the report.
     * @return DRSubscriptionMigrationResultsPage
     */
    public DRSubscriptionMigrationResultsPage generateMigrationReport(final String status, final String action) {

        navigateToGenerateReportPage();
        selectStatus(status);
        selectAction(action);
        submit(TimeConstants.THREE_SEC);

        return super.getPage(DRSubscriptionMigrationResultsPage.class);
    }

    /**
     * This is a method to select status on the DR migration report page
     *
     * @param status
     */
    private void selectStatus(final String status) {

        if (status != null) {
            getActions().select(statusSelect, status);
            LOGGER.info("Set the status to: " + status);
        }
    }

    /**
     * This is a method to select action on the DR migration report page
     *
     * @param action
     */
    private void selectAction(final String action) {

        if (action != null) {
            getActions().select(actionSelect, action);
            LOGGER.info("Set the action to: " + action);
        }
    }

}
