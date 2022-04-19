package com.autodesk.bsm.pelican.ui.downloads.productline;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.BicRelease;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.bicrelease.BicReleasePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * This is a test class which will download the bic release report and validate the date in the download report
 *
 * @author vineel
 */
public class DownloadBicReleaseReport extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static final String FILE_NAME = "bicReleases";
    private static final String ACTUAL_FILE_NAME = "bicReleases.xlsx";
    private BicReleasePage bicReleasePage;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadBicReleaseReport.class.getSimpleName());

    /*
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    private void setUp() {

        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        bicReleasePage = adminToolPage.getPage(BicReleasePage.class);

        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // Clean all the bic release download report files from the path
        cleanBicReleaseReportFile();
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        // Clean all the bic release download report files from the path
        cleanBicReleaseReportFile();
    }

    /*
     * This is a test method which will test whether the bic release report is downloadable or not
     */
    @Test
    public void testIsReportDownloadable() {
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.clickOnDownloadReleaseReport();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final ArrayList<String> names =
            new ArrayList<>(Arrays.asList(new File(XlsUtils.getDirPath(getEnvironmentVariables())).list()));
        for (final String name : names) {
            System.out.println("Content of the folder: " + name);
        }
        final File bicReleaseDownloadsFile =
            new File(XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME);
        final boolean isFileExist = bicReleaseDownloadsFile.exists();
        AssertCollector.assertThat("Bic Release Report is not downloaded", isFileExist, equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the filters are added for the download bic releases or not
     */
    @Test
    public void isFiltersPresent() {
        bicReleasePage.navigateToDownloadReport();
        final boolean isActiveCheckboxPresent = bicReleasePage.isIncludeActiveReleasesCheckboxPresent();
        final boolean isInActiveCheckboxPresent = bicReleasePage.isIncludeInActiveReleasesCheckboxPresent();
        AssertCollector.assertThat("Include active releases checkbox is not present", isActiveCheckboxPresent,
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Include inactive releases checkbox is not present", isInActiveCheckboxPresent,
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will validate the error message being displayed if we don't select any of the filters
     * in the download bic releases report
     */
    @Test
    public void verifyErrorMessage() {
        final String errorMessage = bicReleasePage.getErrorMessageInDownloadReportForm();
        AssertCollector.assertThat(
            "Correct error message is not being displayed when we dont select any of the filters", errorMessage,
            equalTo("You must select the option to download either active and/or inactive BIC releases."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the subscription plan product line filter is present or not
     */
    @Test
    public void verifyIsSubscriptionPlanProductLineAdded() {
        bicReleasePage.navigateToDownloadReport();
        final boolean isSubscriptionPlanProductLineFilterPresent = bicReleasePage.isproductLineFilterDropDownPresent();
        AssertCollector.assertThat("Subscription Plan Product Line Filter is not present in the download report page",
            isSubscriptionPlanProductLineFilterPresent, equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the download report is populated with correct data or not for a
     * specific product line and with include inactive offering filter selected
     */
    @Test
    public void verifyDataInInActiveDefaultStatusInDownloadReport() throws IOException {
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
        final Random random = new Random();
        final String fcsDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String legacySku = RandomStringUtils.randomAlphabetic(5);
        final BicRelease bicRelease = setFieldOfBicReleaseObject(String.valueOf(random.nextInt()),
            productLineExternalKey, productLineExternalKey, Status.INACTIVE, true, legacySku, fcsDate, false);
        final BicRelease actualBicRelease = bicReleasePage.add(bicRelease);
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.setProductLine(productLineExternalKey);
        bicReleasePage.clickOnIncludeActiveOfferingsFilter();
        bicReleasePage.clickOnIncludeInActiveOfferingsFilter();
        bicReleasePage.clickOnDownloadReleaseReport();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String[][] excelData = setStatusFieldToString(fileData);
        assertRowAndColumnDataInExcel(excelData, actualBicRelease);
        assertAllDownloadReportHeaders(excelData);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the download report is populated with correct data or not for a
     * specific product line and with include active offering filter selected
     */
    @Test
    public void verifyDataInActiveStatusForProductLineInDownloadReport() throws IOException {
        final BicReleasePage bicReleasePage = adminToolPage.getPage(BicReleasePage.class);
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);

        final Random random = new Random();
        final String fcsDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String legacySku = RandomStringUtils.randomAlphabetic(5);
        final BicRelease bicRelease = setFieldOfBicReleaseObject(String.valueOf(random.nextInt()),
            productLineExternalKey, productLineExternalKey, Status.ACTIVE, true, legacySku, fcsDate, false);
        final BicRelease actualBicRelease = bicReleasePage.add(bicRelease);
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.setProductLine(productLineExternalKey);
        bicReleasePage.clickOnDownloadReleaseReport();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String[][] excelData = setStatusFieldToString(fileData);
        assertRowAndColumnDataInExcel(excelData, actualBicRelease);
        assertAllDownloadReportHeaders(excelData);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the download report is populated with correct data or not for a
     * specific product line and with both include active and inactive offering filter selected
     */
    @Test
    public void verifyDataInInBothStatusInDownloadReport() throws IOException {
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);

        final Random random = new Random();
        final String fcsDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String legacySku = RandomStringUtils.randomAlphabetic(5);
        final BicRelease bicRelease = setFieldOfBicReleaseObject(String.valueOf(random.nextInt()),
            productLineExternalKey, productLineExternalKey, Status.ACTIVE, true, legacySku, fcsDate, false);
        final BicRelease actualBicRelease = bicReleasePage.add(bicRelease);
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.setProductLine(productLineExternalKey);
        bicReleasePage.clickOnIncludeInActiveOfferingsFilter();
        bicReleasePage.clickOnDownloadReleaseReport();

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String[][] excelData = setStatusFieldToString(fileData);
        assertRowAndColumnDataInExcel(excelData, actualBicRelease);
        assertAllDownloadReportHeaders(excelData);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the download report is populated with correct data or not for a
     * default product line and with include active offering filter selected
     */
    @Test
    public void verifyDataInActiveStatusWithDefaultProductLineInDownloadReport() throws IOException {
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);

        final Random random = new Random();
        final String fcsDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String legacySku = RandomStringUtils.randomAlphabetic(5);
        final BicRelease bicRelease = setFieldOfBicReleaseObject(String.valueOf(random.nextInt()),
            productLineExternalKey, productLineExternalKey, Status.ACTIVE, true, legacySku, fcsDate, false);
        final BicRelease actualBicRelease = bicReleasePage.add(bicRelease);
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.clickOnDownloadReleaseReport();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String[][] excelData = setStatusFieldToString(fileData);
        assertRowAndColumnDataInExcel(excelData, actualBicRelease);
        assertAllDownloadReportHeaders(excelData);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the download report is populated with correct data or not for a
     * default product line and with include in-active offering filter selected
     */
    @Test
    public void verifyDataInInActiveStatusWithDefaultProductLineInDownloadReport() throws IOException {
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);

        final Random random = new Random();
        final String fcsDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String legacySku = RandomStringUtils.randomAlphabetic(5);
        final BicRelease bicRelease = setFieldOfBicReleaseObject(String.valueOf(random.nextInt()),
            productLineExternalKey, productLineExternalKey, Status.INACTIVE, true, legacySku, fcsDate, false);
        final BicRelease actualBicRelease = bicReleasePage.add(bicRelease);
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.clickOnIncludeActiveOfferingsFilter();
        bicReleasePage.clickOnIncludeInActiveOfferingsFilter();
        bicReleasePage.clickOnDownloadReleaseReport();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String[][] excelData = setStatusFieldToString(fileData);
        assertRowAndColumnDataInExcel(excelData, actualBicRelease);
        assertAllDownloadReportHeaders(excelData);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether the download report is populated with correct data or not for a
     * default product line and with both include active and in-active offering filter selected
     */
    @Test
    public void verifyDataInInBothStatusWithDefaultProductLineInDownloadReport() throws IOException {
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(4);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);

        final Random random = new Random();
        final String fcsDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String legacySku = RandomStringUtils.randomAlphabetic(5);
        final BicRelease bicRelease = setFieldOfBicReleaseObject(String.valueOf(random.nextInt()),
            productLineExternalKey, productLineExternalKey, Status.INACTIVE, true, legacySku, fcsDate, false);
        final BicRelease actualBicRelease = bicReleasePage.add(bicRelease);
        bicReleasePage.navigateToDownloadReport();
        bicReleasePage.clickOnIncludeInActiveOfferingsFilter();
        bicReleasePage.clickOnDownloadReleaseReport();

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String[][] excelData = setStatusFieldToString(fileData);
        assertRowAndColumnDataInExcel(excelData, actualBicRelease);
        assertAllDownloadReportHeaders(excelData);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * @return BicRelease Object
     */
    private BicRelease setFieldOfBicReleaseObject(final String downloadRelease,
        final String subscriptionPlanProductLine, final String downloadProductLine, final Status status,
        final boolean isClicEnabled, final String legacySku, final String fcsDate,
        final boolean ignoreEmailNotification) {
        final BicRelease bicRelease = new BicRelease();
        bicRelease.setDownloadRelease(downloadRelease);
        bicRelease.setSubsPlanProductLine(subscriptionPlanProductLine);
        bicRelease.setDownloadProductLine(downloadProductLine);
        bicRelease.setStatus(status);
        bicRelease.setClic(isClicEnabled);
        bicRelease.setLegacySku(legacySku);
        bicRelease.setFcsDate(fcsDate);
        bicRelease.setIgnoredEmailNotification(ignoreEmailNotification);

        return bicRelease;
    }

    /**
     * @param fileData - Two Dimensional String Array of Excel Data
     */
    private void assertAllDownloadReportHeaders(final String[][] fileData) {
        try {
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][0],
                equalTo("#BICRelease"), assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][1],
                equalTo("subPlanProductLine"), assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][2],
                equalTo("downloadProductLine"), assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][3],
                equalTo("downloadRelease"), assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][4],
                equalTo("clicEnabled"), assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][5], equalTo("legacySku"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][6], equalTo("fcsDate"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][7],
                equalTo("ignoreScfEmails"), assertionErrorList);
            AssertCollector.assertThat("Incorrect headers in the download report", fileData[0][8], equalTo("active"),
                assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        } catch (final ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileData
     * @param actualBicRelease
     */
    private void assertRowAndColumnDataInExcel(final String[][] fileData, final BicRelease actualBicRelease) {
        final int lastRow = fileData.length - 1;
        AssertCollector.assertThat("Incorrect data in download report", fileData[lastRow][0], equalTo("BICRelease"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription plan product line data in download report",
            fileData[lastRow][1], equalTo(actualBicRelease.getSubsPlanProductLine()), assertionErrorList);
        AssertCollector.assertThat("Incorrect download product line data in download report", fileData[lastRow][2],
            equalTo(actualBicRelease.getDownloadProductLine()), assertionErrorList);
        AssertCollector.assertThat("Incorrect download release data in download report", fileData[lastRow][3],
            equalTo(actualBicRelease.getDownloadRelease()), assertionErrorList);
        AssertCollector.assertThat("Incorrect click enabled data in download report", fileData[lastRow][4],
            equalTo(String.valueOf(actualBicRelease.isClicEnabled())), assertionErrorList);
        AssertCollector.assertThat("Incorrect legacy sku data in download report", fileData[lastRow][5],
            equalTo(actualBicRelease.getLegacySku()), assertionErrorList);
        AssertCollector.assertThat("Incorrect ignore email notification data in download report", fileData[lastRow][7],
            equalTo(String.valueOf(actualBicRelease.getIgnoreEmailNotification())), assertionErrorList);
        AssertCollector.assertThat("Incorrect status data in download report", fileData[lastRow][8],
            equalTo(actualBicRelease.getStatus().toString()), assertionErrorList);

    }

    private String[][] setStatusFieldToString(final String[][] fileData) {
        final int lastRow = fileData.length - 1;
        // Change the values of the boolean excel data status to string values
        for (int i = 1; i <= lastRow; i++) {
            if (fileData[i][8].startsWith("true")) {
                fileData[i][8] = Status.ACTIVE.toString();
            } else {
                fileData[i][8] = Status.INACTIVE.toString();
            }
        }
        return fileData;
    }

    /**
     * Delete all the bic release report excel file from the download path
     */
    private void cleanBicReleaseReportFile() {
        // Delete all existing files with name "bicReleases" in the download path
        final Util util = new Util();
        String downloadPath;
        if (System.getProperty("os.name").startsWith("Mac")) {
            final String home = System.getProperty("user.home");
            downloadPath = home + getEnvironmentVariables().getDownloadPathForMac();
            LOGGER.info("Download path" + downloadPath);
            util.deleteAllFilesWithSpecificFileName(downloadPath, FILE_NAME);
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            downloadPath = getEnvironmentVariables().getDownloadPathForWindows();
            util.deleteAllFilesWithSpecificFileName(downloadPath, FILE_NAME);
        }
    }

}
