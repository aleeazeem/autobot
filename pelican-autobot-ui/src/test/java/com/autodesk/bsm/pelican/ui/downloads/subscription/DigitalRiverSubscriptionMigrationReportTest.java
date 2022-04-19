package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.DRSubscriptionMigrationGenerateReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.DRSubscriptionMigrationResultsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This is a test class which have test methods to test the digital river subscription migration report.
 *
 * @author yerragv
 */
public class DigitalRiverSubscriptionMigrationReportTest extends SeleniumWebdriver {

    private DRSubscriptionMigrationGenerateReportPage drSubscriptionMigrationGenerateReportPage;
    private static final String recordsCountResult = "0 results";
    private static final String ACTUAL_FILE_NAME = "DigitalRiverSubscriptionMigrationReport.xlsx";
    private static DRSubscriptionMigrationResultsPage drSubscriptionMigrationResultsPage;

    /**
     * Data setup - create user for each test!
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        drSubscriptionMigrationGenerateReportPage =
            adminToolPage.getPage(DRSubscriptionMigrationGenerateReportPage.class);
    }

    /**
     * This is a test method which can validate the report headers in the view DR Migration Report.
     */
    @Test
    public void testDRSubscriptionMigrationReportHeadersInViewReport() {

        drSubscriptionMigrationResultsPage = drSubscriptionMigrationGenerateReportPage
            .generateMigrationReport(PelicanConstants.SUCCESSFULLY_MIGRATED, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect headers in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().size(), equalTo(10), assertionErrorList);
        AssertCollector.assertThat("Incorrect 1st header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(0), equalTo(PelicanConstants.ID_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 2nd header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(1), equalTo(PelicanConstants.EXTERNAL_KEY_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 3rd header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(2), equalTo(PelicanConstants.STATUS_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 4th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(3), equalTo(PelicanConstants.USER_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 5th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(4), equalTo(PelicanConstants.PLAN_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 6th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(5), equalTo(PelicanConstants.OFFER_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 7th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(6), equalTo(PelicanConstants.STORE_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 8th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(7), equalTo(PelicanConstants.SEATS_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect 9th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(8),
            equalTo(PelicanConstants.NEXT_BILLING_DATE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect 10th header in the report",
            drSubscriptionMigrationResultsPage.getColumnHeaders().get(9),
            equalTo(PelicanConstants.NEXT_BILLING_AMOUNT_FIELD), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which can validate the data in successfully migrated status in view report.
     */
    @Test
    public void testDRSubscriptionMigrationReportDataInSuccessfullyMigratedStatusInView() {

        drSubscriptionMigrationResultsPage = drSubscriptionMigrationGenerateReportPage
            .generateMigrationReport(PelicanConstants.SUCCESSFULLY_MIGRATED, PelicanConstants.VIEW);
        final String totalRecordsCount = drSubscriptionMigrationResultsPage.getTotalNumberOfResultsOnPage();
        AssertCollector.assertThat("Incorrect count of records", totalRecordsCount, equalTo(recordsCountResult),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which can validate the data in successfully migrated but failed to update DR/Siebel status
     * in view report.
     */
    @Test
    public void testDRSubscriptionMigrationReportDataInMigratedButFailedToUpdateDRStatusInView() {

        drSubscriptionMigrationResultsPage = drSubscriptionMigrationGenerateReportPage.generateMigrationReport(
            PelicanConstants.MIGRATED_BUT_FAILED_TO_UPDATE_IN_DR_OR_SIEBEL, PelicanConstants.VIEW);
        final String totalRecordsCount = drSubscriptionMigrationResultsPage.getTotalNumberOfResultsOnPage();
        AssertCollector.assertThat("Incorrect count of records", totalRecordsCount, equalTo(recordsCountResult),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which can validate the data in successfully migrated status but failed to get info from
     * Digital River in view report.
     */
    @Test
    public void testDRSubscriptionMigrationReportDataInMigratedButFailedToGetInfoFromDRStatusInView() {

        drSubscriptionMigrationResultsPage = drSubscriptionMigrationGenerateReportPage.generateMigrationReport(
            PelicanConstants.MIGRATED_BUT_FAILED_TO_UPDATE_IN_DR_OR_SIEBEL, PelicanConstants.VIEW);
        final String totalRecordsCount = drSubscriptionMigrationResultsPage.getTotalNumberOfResultsOnPage();
        AssertCollector.assertThat("Incorrect count of records", totalRecordsCount, equalTo(recordsCountResult),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which can validate the report headers in the download DR Migration Report.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testDRSubscriptionMigrationReportHeadersInDownloadReport() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        drSubscriptionMigrationGenerateReportPage.generateMigrationReport(PelicanConstants.SUCCESSFULLY_MIGRATED,
            PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length, equalTo(9),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0], equalTo(PelicanConstants.ID_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1], equalTo(PelicanConstants.EXTERNAL_KEY_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2], equalTo(PelicanConstants.STATUS_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3], equalTo(PelicanConstants.PLAN_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", fileData[0][4], equalTo(PelicanConstants.OFFER_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", fileData[0][5], equalTo(PelicanConstants.USER_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", fileData[0][6], equalTo(PelicanConstants.STORE_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", fileData[0][7], equalTo(PelicanConstants.SEATS_FIELD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", fileData[0][8],
            equalTo(PelicanConstants.NEXT_BILLING_DATE_FIELD), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

}
