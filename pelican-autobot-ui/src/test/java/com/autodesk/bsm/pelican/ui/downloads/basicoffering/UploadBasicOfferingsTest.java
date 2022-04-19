package com.autodesk.bsm.pelican.ui.downloads.basicoffering;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Store;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.BasicOfferingAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAndBasicOfferingsAuditLogReportHelper;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.FindBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.UploadBasicOfferingsPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.ViewUploadStatusBasicOfferingsPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Admin Tool's Upload Basic Offerings tests. On Admin Tool's Main Tab navigate to Catalog->Basic Offerings -> Upload
 * Validate if the Basic Offerings xlsx file has been uploaded
 *
 * @author sunitha
 */

public class UploadBasicOfferingsTest extends SeleniumWebdriver {

    private static final String FILE_NAME = "UploadBasicOfferings.xlsx";
    private static final String INVALID_FILE_NAME = "invalid_file_name.csv";
    private AdminToolPage adminToolPage;
    private UploadBasicOfferingsPage uploadBasicOfferingsPage;
    private UploadUtils uploadUtils;
    private String priceListExternalKey;
    private String storeExternalKey;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private ArrayList<String> columnHeaders;
    private ArrayList<String> columnData;
    private XlsUtils xlsUtils;
    private String externalKey1;
    private String name1;
    private String basicOfferingId1;
    private String newBasicOfferingId1;
    private String newBasicOfferingId2;
    private String newBasicOfferingId3;
    private String productLineName;
    private String productLineName1;
    private AuditLogReportHelper auditLogReportHelper;
    private String productLineId;
    private String productLineId1;
    private String adminToolUserId;
    private static final String offeringDetail = "OfferingDetails1";
    private static final String DATE_FORMAT_FOR_UPLOAD = "MM/dd/yy";
    private final String startDate = DateTimeUtils.getNowPlusDays(DATE_FORMAT_FOR_UPLOAD, 1);
    private final String endDate = DateTimeUtils.getNowPlusDays(DATE_FORMAT_FOR_UPLOAD, 30);
    private final String startDateInAuditLog =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_START_DATE, 1);
    private final String endDateInAuditLog =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_END_DATE, 30);
    private boolean createBasicOfferingPriceLogFound1 = false;
    private FindBasicOfferingPage findBasicOfferingPage;
    private BasicOfferingDetailPage basicOfferingPage;
    private AuditLogReportResultPage auditLogReportResultPage;
    private AuditLogReportPage auditLogReportPage;
    private String offeringDetailId;
    private String basicOfferingPriceId1;
    private String priceListId;
    private int totalItems;
    private String basicOfferingName1;
    private String basicOfferingExternalKey1;
    private String basicOfferingName2;
    private String basicOfferingExternalKey2;
    private String basicOfferingName3;
    private String basicOfferingExternalKey3;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadBasicOfferingsTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        adminToolUserId = getEnvironmentVariables().getUserId();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        uploadBasicOfferingsPage = adminToolPage.getPage(UploadBasicOfferingsPage.class);
        auditLogReportResultPage = adminToolPage.getPage(AuditLogReportResultPage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        final StoreApiUtils storeUtils = new StoreApiUtils(getEnvironmentVariables());
        final Store createdStore = storeUtils.addStoreWithoutPriceListAndCountry(Status.ACTIVE);
        final String storeId = createdStore.getData().getId();
        storeExternalKey = createdStore.getData().getExternalKey();
        priceListExternalKey = "TEST_PRICELIST_USD" + RandomStringUtils.randomAlphanumeric(3);
        priceListId =
            storeUtils.addPriceListWithExternalKey(storeId, priceListExternalKey, Currency.USD).getData().getId();
        storeUtils.addCountryToStoreAndPriceList(priceListExternalKey, storeId, Country.US);

        final SubscriptionPlanApiUtils subscriptionPlanUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        productLineName = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        ProductLine productLine = subscriptionPlanUtils.addProductLine(productLineName);
        productLineId = productLine.getData().getId();

        productLineName1 = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        productLine = subscriptionPlanUtils.addProductLine(productLineName1);
        productLineId1 = productLine.getData().getId();

        offeringDetailId = getEnvironmentVariables().getOfferingDetailId();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        if (uploadUtils.deleteFilefromLocal(FILE_NAME)) {
            LOGGER.info(FILE_NAME + " is successfully deleted from /testdata");
        } else {
            LOGGER.warn(FILE_NAME + " is NOT deleted from /testdata");
        }
    }

    /**
     * Method to upload Basic Offerings file containing Basic Offerings and Offering Price with Headers and data
     */
    @Test
    public void uploadBasicOfferings() throws IOException {
        createXlsxAndWriteData(FILE_NAME);
        final GenericGrid searchResult = uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);
        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - View Upload Status"), assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", searchResult.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify invalid Upload file Error message
     */
    @Test
    public void verifyInvalidFileUploadErrorMessage() {
        uploadUtils.uploadBasicOffering(adminToolPage, INVALID_FILE_NAME);
        final String actualErrorMsg = uploadBasicOfferingsPage.getUploadErrorMessage();
        final String expectedErrorMsg = "Please upload a valid XLSX file that is not password-protected.";
        AssertCollector.assertThat("Incorrect error message", actualErrorMsg, equalTo(expectedErrorMsg),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify the view upload status page headers
     */
    @Test
    public void verifyUploadStatusPageHeadersTest() throws IOException {

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final GenericGrid searchResult = uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);
        LOGGER.info(getDriver().getCurrentUrl());

        final List<String> columnHeaders = searchResult.getColumnHeaders();

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - View Upload Status"), assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", searchResult.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);
        // Validate column headers
        AssertCollector.assertThat("ID header not found", columnHeaders.contains("ID"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Created Date header not found", columnHeaders.contains("Created Date"),
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("App Family header not found", columnHeaders.contains("App Family"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Last Modified header not found", columnHeaders.contains("Last Modified"),
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Created By header not found", columnHeaders.contains("Created By"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("File Name header not found", columnHeaders.contains("File Name"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Triggers Job ID header not found", columnHeaders.contains("Triggers Job ID"),
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Entity Type header not found", columnHeaders.contains("Entity Type"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Status header not found", columnHeaders.contains("Status"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertThat("Errors header not found", columnHeaders.contains("Errors"), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify upload status page results.
     */
    @Test
    public void verifyUploadStatusPageResultsTest() throws IOException, ParseException {
        final Date currentTime =
            java.util.Calendar.getInstance(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE)).getTime();

        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final GenericGrid searchResult = uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);
        LOGGER.info(getDriver().getCurrentUrl());

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - View Upload Status"), assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", searchResult.getHeader(),
            equalTo("Upload Status"), assertionErrorList);
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);
        // Validate pagination (i.e. results per page)
        AssertCollector.assertThat("Incorrect number of results per page", searchResult.getColumnValues("ID").size(),
            lessThanOrEqualTo(20), assertionErrorList);
        // Assertions on the value under Created Date (i.e. most recently
        // created record or not)
        final Date mostRecentCreatedDate = dateFormat.parse(searchResult.getColumnValues("Created Date").get(0));
        AssertCollector.assertThat("Incorrect created date", mostRecentCreatedDate, is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Most recently created record not found", mostRecentCreatedDate,
            greaterThan(currentTime), assertionErrorList);
        // Assertions on the values under each header
        final List<String> actualAppFamilyId = searchResult.getColumnValues("App Family");
        AssertCollector.assertThat("AdminTool: Found uploads other than <b>AUTO Family</b>", actualAppFamilyId,
            everyItem(equalTo("2001")), assertionErrorList);
        AssertCollector.assertThat("Incorrect created by", searchResult.getColumnValues("Created By").get(0),
            equalTo("svc_p_pelican"), assertionErrorList);
        AssertCollector.assertThat("Incorrect file extension", searchResult.getColumnValues("File Name").get(0),
            containsString(".xlsx"), assertionErrorList);

        final String entityType = searchResult.getColumnValues("Entity Type").get(0);
        AssertCollector.assertThat("Incorrect entity type", entityType.matches("BASIC_OFFERING|SUBSCRIPTION_PLAN|ITEM"),
            equalTo(true), assertionErrorList);
        final String status = searchResult.getColumnValues("Status").get(0);
        AssertCollector.assertThat("Incorrect status", status.matches("NOT_STARTED|COMPLETED|FAILED|IN_PROGRESS"),
            equalTo(true), assertionErrorList);
        final List<String> statusList = searchResult.getColumnValues("Status");
        for (int i = 0; i < statusList.size(); i++) {
            if (statusList.get(i).equals(Status.FAILED.toString())) {
                AssertCollector.assertThat("Incorrect errors", searchResult.getColumnValues("Errors").get(i),
                    containsString("View Errors"), assertionErrorList);
                final String actualErrorMessage =
                    searchResult.getValue("errors_" + searchResult.getColumnValues("ID").get(i));
                String expectedErrorMessage = null;
                try {
                    expectedErrorMessage = DbUtils.getUploadErrorMessage(searchResult.getColumnValues("ID").get(i),
                        getEnvironmentVariables());
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
                AssertCollector.assertThat("Error message not matched", actualErrorMessage,
                    equalTo(expectedErrorMessage), assertionErrorList);
                break;
            }
        }
        AssertCollector.assertThat("Incorrect entity type", searchResult.getColumnValues("Entity Type").get(0),
            equalTo("BASIC_OFFERING"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", searchResult.getColumnValues("Status").get(0),
            anyOf(equalTo(PelicanConstants.JOB_STATUS_NOT_STARTED), equalTo(PelicanConstants.JOB_STATUS_IN_PROGRESS)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify upload status page results
     */
    @Test
    public void testUploadBasicOfferingsVerifyUTF8FileName() throws IOException {
        final String UTF_FILE_NAME = "uploadBasicOfferings" + "赛巴巴.xlsx";
        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(UTF_FILE_NAME);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final GenericGrid searchResult = uploadUtils.uploadBasicOffering(adminToolPage, UTF_FILE_NAME);
        LOGGER.info(getDriver().getCurrentUrl());

        if (uploadUtils.deleteFilefromLocal(UTF_FILE_NAME)) {
            LOGGER.info(UTF_FILE_NAME + " is successfully deleted from /testdata");
        } else {
            LOGGER.warn(UTF_FILE_NAME + " is NOT deleted from /testdata");
        }

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - View Upload Status"), assertionErrorList);
        // Validate admin tool's header
        AssertCollector.assertThat("Admin tool: Incorrect page header", searchResult.getHeader(),
            equalTo("Upload Status"), assertionErrorList);

        AssertCollector.assertThat(
            "Expected " + UTF_FILE_NAME + " File, but found " + searchResult.getColumnValues("File Name").get(0)
                + " File",
            searchResult.getColumnValues("File Name").get(0), equalTo(UTF_FILE_NAME), assertionErrorList);

        System.out.println("Expected " + UTF_FILE_NAME + " File, but found "
            + searchResult.getColumnValues("File Name").get(0) + " File");

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with COMPLETED job status
     *
     * @result Valid header and data is returned
     */
    @Test(dependsOnMethods = "uploadBasicOfferings")
    public void findJobsWithCompletedJobStatusFilter() {

        final ViewUploadStatusBasicOfferingsPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusBasicOfferingsPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_COMPLETED);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_BASIC_OFFERINGS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        viewUploadStatusPage.submit(TimeConstants.TWO_SEC);

        final GenericGrid searchResult = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);

        // Validate job status as 'Completed'
        final List<String> actualJobStatus = searchResult.getColumnValues("Status");
        for (final String status : actualJobStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found jobs other than <b>COMPELTED</b>", status,
                    equalTo(Status.COMPLETED.toString()), assertionErrorList);
            }
        }

        // Assert that job entity is 'BASIC OFFERINGS'
        assertEntityType(searchResult);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with IN PROGRESS job status
     *
     * @result Valid header and data is returned
     */
    @Test
    public void findJobsWithInProgressJobStatusFilter() {

        final ViewUploadStatusBasicOfferingsPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusBasicOfferingsPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_IN_PROGRESS_DROPDOWN);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_BASIC_OFFERINGS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        viewUploadStatusPage.submit(TimeConstants.TWO_SEC);

        final GenericGrid searchResult = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        totalItems = searchResult.getTotalItems();
        AssertCollector.assertThat("Admin Tool: Got no data", totalItems, greaterThanOrEqualTo(0), assertionErrorList);

        if (totalItems != 0) {
            // Validate job status as 'In-progress'
            final List<String> actualJobStatus = searchResult.getColumnValues("Status");
            for (final String status : actualJobStatus) {
                if (!status.isEmpty()) {
                    AssertCollector.assertThat("AdminTool: Found jobs other than <b>IN_PROGRESS</b>", status,
                        equalTo("IN_PROGRESS"), assertionErrorList);
                }
            }
            // Assert that entity type is 'BASIC OFFERINGS'
            assertEntityType(searchResult);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with NOT STARTED job status
     *
     * @result Valid header and data is returned
     */
    @Test(dependsOnMethods = { "verifyUploadStatusPageResultsTest" })
    public void findJobsWithNotStartedJobStatusFilter() {

        final ViewUploadStatusBasicOfferingsPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusBasicOfferingsPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_NOT_STARTED_DROPDOWN);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_BASIC_OFFERINGS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        viewUploadStatusPage.submit(TimeConstants.TWO_SEC);

        final GenericGrid searchResult = adminToolPage.getPage(GenericGrid.class);

        totalItems = searchResult.getTotalItems();
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(0),
            assertionErrorList);

        if (totalItems != 0) {
            // Validate job status as 'Not Started'
            final List<String> actualJobStatus = searchResult.getColumnValues("Status");
            for (final String status : actualJobStatus) {
                if (!status.isEmpty()) {
                    AssertCollector.assertThat("AdminTool: Found jobs other than <b>NOT_STARTED</b>", status,
                        equalTo("NOT_STARTED"), assertionErrorList);
                }
            }
        }
        // Assert that job entity type is 'BASIC OFFERINGS'
        assertEntityType(searchResult);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with FAILED job status
     *
     * @result Valid header and data is returned
     */
    @Test
    public void findJobsWithFailedJobStatusFilter() {

        final ViewUploadStatusBasicOfferingsPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusBasicOfferingsPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_FAILED);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_BASIC_OFFERINGS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        viewUploadStatusPage.submit(TimeConstants.TWO_SEC);

        final GenericGrid searchResult = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);

        // Validate job status as 'Failed'
        final List<String> actualJobStatus = searchResult.getColumnValues("Status");
        for (final String status : actualJobStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found jobs other than <b>FAILED</b>", status,
                    equalTo(Status.FAILED.toString()), assertionErrorList);
            }
        }
        // Assert that job entity type is 'BASIC OFFERINGS'
        assertEntityType(searchResult);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Creating Basic Offering and Price through Upload and verifying the AUDIT LOG for the same
     */
    @Test
    public void testAuditLogForBasicOfferingCreateThroughUpload() throws IOException {

        // create entries of Basic Offering by writing to a file
        basicOfferingName1 = "BasicOffering_name_" + RandomStringUtils.randomAlphanumeric(4);
        basicOfferingExternalKey1 = "BasicOffering_ext_" + RandomStringUtils.randomAlphanumeric(5);
        createXlsxAndWriteDataForBasicOffering(FILE_NAME, basicOfferingName1, basicOfferingExternalKey1,
            OfferingType.PERPETUAL, Status.NEW, productLineName, MediaType.ELD, "EN", offeringDetail, UsageType.COM,
            SupportLevel.BASIC, null, null, "a=2;b=2", false);

        basicOfferingName2 = "BasicOffering_name_" + RandomStringUtils.randomAlphanumeric(4);
        basicOfferingExternalKey2 = "BasicOffering_ext_" + RandomStringUtils.randomAlphanumeric(5);
        createXlsxAndWriteDataForBasicOffering(FILE_NAME, basicOfferingName2, basicOfferingExternalKey2,
            OfferingType.MAINTENANCE_RENEWAL, Status.ACTIVE, productLineName, MediaType.DVD, "EN", offeringDetail,
            UsageType.TRL, SupportLevel.ADVANCED, null, null, "a=2;b=2", true);

        basicOfferingName3 = "BasicOffering_name_" + RandomStringUtils.randomAlphanumeric(4);
        basicOfferingExternalKey3 = "BasicOffering_ext_" + RandomStringUtils.randomAlphanumeric(5);
        createXlsxAndWriteDataForBasicOffering(FILE_NAME, basicOfferingName3, basicOfferingExternalKey3,
            OfferingType.CURRENCY, Status.ACTIVE, productLineName, MediaType.ELD, "EN", offeringDetail, UsageType.EDU,
            SupportLevel.BASIC, "200.00", Currency.USD, "a=2;b=2", true);

        // create Basic Offering Price
        createXlsxAndWriteDataForBasicOfferingPrice(FILE_NAME, basicOfferingExternalKey1, priceListExternalKey,
            storeExternalKey, "400.00", Currency.USD, startDate, endDate, "", true);

        // Uploading the file
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);

        // Wait for 2 min to finish processing
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // Find the Basic Offering Id
        findBasicOfferingPage = adminToolPage.getPage(FindBasicOfferingPage.class);
        basicOfferingPage = findBasicOfferingPage.findBasicOfferingByExternalKey(basicOfferingExternalKey1);
        newBasicOfferingId1 = basicOfferingPage.getId();
        // Get the Basic Offering Price Id
        basicOfferingPriceId1 = basicOfferingPage.getFirstPriceId();

        // Find the Basic Offering Id
        basicOfferingPage = findBasicOfferingPage.findBasicOfferingByExternalKey(basicOfferingExternalKey2);
        newBasicOfferingId2 = basicOfferingPage.getId();

        // Find the Basic Offering Id
        basicOfferingPage = findBasicOfferingPage.findBasicOfferingByExternalKey(basicOfferingExternalKey3);
        newBasicOfferingId3 = basicOfferingPage.getId();

        // Query Dynamo DB for each Basic Offering
        final boolean createBasicOfferingLogFound1 =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOffering(newBasicOfferingId1, null,
                basicOfferingName1, null, basicOfferingExternalKey1, null, OfferingType.PERPETUAL, null, Status.NEW,
                null, productLineId, null, MediaType.ELD.getValue(), null, "EN", null, offeringDetailId, null,
                SupportLevel.BASIC, null, UsageType.COM, null, "a=2;b=2", Action.CREATE, FILE_NAME, assertionErrorList);

        final boolean createBasicOfferingLogFound2 =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOffering(newBasicOfferingId2, null,
                basicOfferingName2, null, basicOfferingExternalKey2, null, OfferingType.MAINTENANCE_RENEWAL, null,
                Status.ACTIVE, null, productLineId, null, MediaType.DVD.getValue(), null, "EN", null, offeringDetailId,
                null, SupportLevel.ADVANCED, null, UsageType.TRL, null, "a=2;b=2", Action.CREATE, FILE_NAME,
                assertionErrorList);

        final boolean createBasicOfferingLogFound3 =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOffering(newBasicOfferingId3, null,
                basicOfferingName3, null, basicOfferingExternalKey3, null, OfferingType.CURRENCY, null, Status.ACTIVE,
                null, productLineId, null, MediaType.ELD.getValue(), null, "EN", null, offeringDetailId, null,
                SupportLevel.BASIC, null, UsageType.EDU, null, "a=2;b=2", Action.CREATE, FILE_NAME, assertionErrorList);

        createBasicOfferingPriceLogFound1 = BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOfferingPrice(
            basicOfferingPriceId1, null, basicOfferingId1, null, priceListId, null, storeExternalKey, null, "400", null,
            String.valueOf(Currency.USD.getCode()), null, startDateInAuditLog, null, endDateInAuditLog, Action.CREATE,
            FILE_NAME, assertionErrorList);

        AssertCollector.assertTrue(
            "Create Basic Offering Audit Log not found for Basic Offering id : " + newBasicOfferingId1,
            createBasicOfferingLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Basic Offering Audit Log not found for Basic Offering id : " + newBasicOfferingId2,
            createBasicOfferingLogFound2, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Basic Offering Audit Log not found for Basic Offering id : " + newBasicOfferingId3,
            createBasicOfferingLogFound3, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Basic Offering Price Audit Log not found for Basic Offering Price id : " + basicOfferingPriceId1,
            createBasicOfferingPriceLogFound1, assertionErrorList);

        // Verify Audit Log report results for Basic Offering1.
        final HashMap<String, List<String>> descriptionPropertyValues1 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                newBasicOfferingId1, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues1, null, basicOfferingName1, null, basicOfferingExternalKey1, null,
            OfferingType.PERPETUAL, null, Status.NEW, null, UsageType.COM, null, offeringDetailId, null, productLineId,
            null, MediaType.ELD, getEnvironmentVariables(), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues1.containsKey(FILE_NAME), assertionErrorList);

        // Verify Audit Log report results for Basic Offering2.
        final HashMap<String, List<String>> descriptionPropertyValues2 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                newBasicOfferingId2, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues2, null, basicOfferingName2, null, basicOfferingExternalKey2, null,
            OfferingType.MAINTENANCE_RENEWAL, null, Status.ACTIVE, null, UsageType.TRL, null, offeringDetailId, null,
            productLineId, null, MediaType.DVD, getEnvironmentVariables(), assertionErrorList);
        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues2.containsKey(FILE_NAME), assertionErrorList);

        // Verify Audit Log report results for Basic Offering3.
        final HashMap<String, List<String>> descriptionPropertyValues3 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                newBasicOfferingId3, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues3, null, basicOfferingName3, null, basicOfferingExternalKey3, null,
            OfferingType.CURRENCY, null, Status.ACTIVE, null, UsageType.EDU, null, offeringDetailId, null,
            productLineId, null, MediaType.ELD, getEnvironmentVariables(), assertionErrorList);
        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues3.containsKey(FILE_NAME), assertionErrorList);

        // Verify Audit Log report results for Basic Offering price id.
        final HashMap<String, List<String>> descriptionPropertyValues4 = auditLogReportHelper
            .verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, newBasicOfferingId1,
                basicOfferingPriceId1, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues4, null, Currency.USD + " (4)", null, "400", null,
            priceListExternalKey + " (" + priceListId + ")", null, startDateInAuditLog, null, endDateInAuditLog,
            assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests Updating Basic Offering and Price through Upload and verifying the AUDIT LOG for the same
     */
    @Test(dependsOnMethods = { "testAuditLogForBasicOfferingCreateThroughUpload" })
    public void testAuditLogForBasicOfferingUpdateThroughUpload() throws IOException {

        final String newStartDate = DateTimeUtils.getNowPlusDays(DATE_FORMAT_FOR_UPLOAD, 15);
        final String newEndDate = DateTimeUtils.getNowPlusDays(DATE_FORMAT_FOR_UPLOAD, 60);
        final String newStartDateForAuditLog =
            DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_START_DATE, 15);
        final String newEndDateForAuditLog =
            DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_END_DATE, 60);

        // create entries of Basic Offering by writing to a file
        createXlsxAndWriteDataForBasicOffering(FILE_NAME, "name1", basicOfferingExternalKey1, OfferingType.PERPETUAL,
            Status.ACTIVE, productLineName1, MediaType.DVD, "EN", offeringDetail, UsageType.TRL, SupportLevel.ADVANCED,
            "100.00", null, "c=2;d=2", false);

        createXlsxAndWriteDataForBasicOfferingPrice(FILE_NAME, basicOfferingExternalKey1, priceListExternalKey,
            storeExternalKey, "1000.00", Currency.USD, newStartDate, newEndDate, basicOfferingPriceId1, true);

        createXlsxAndWriteDataForBasicOffering(FILE_NAME, "name2", basicOfferingExternalKey2,
            OfferingType.MAINTENANCE_RENEWAL, Status.ACTIVE, productLineName, MediaType.DVD, "EN", offeringDetail,
            UsageType.COM, SupportLevel.BASIC, null, null, "a=2;b=2", true);

        createXlsxAndWriteDataForBasicOffering(FILE_NAME, basicOfferingName3, basicOfferingExternalKey3,
            OfferingType.CURRENCY, Status.CANCELED, productLineName, MediaType.ELD, "EN", offeringDetail, UsageType.EDU,
            SupportLevel.BASIC, "600.00", Currency.USD, "a=2;b=2", true);

        createXlsxAndWriteDataForBasicOfferingPrice(FILE_NAME, basicOfferingExternalKey2, priceListExternalKey,
            storeExternalKey, "1000.00", Currency.USD, startDate, endDate, "", true);

        // Uploading the file
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);

        // Wait for 2 min to finish processing
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // Get Price Id
        basicOfferingPage = findBasicOfferingPage.findBasicOfferingByExternalKey(basicOfferingExternalKey2);
        final String basicOfferingPriceId2 = basicOfferingPage.getFirstPriceId();

        // Query Dynamo DB for each Basic Offering
        final boolean updateBasicOfferingLogFound1 = BasicOfferingAuditLogHelper
            .helperToValidateDynamoDbForBasicOffering(newBasicOfferingId1, basicOfferingName1, "name1", null, null,
                null, null, Status.NEW, Status.ACTIVE, productLineId, productLineId1, MediaType.ELD.getValue(),
                MediaType.DVD.getValue(), null, null, null, null, SupportLevel.BASIC, SupportLevel.ADVANCED,
                UsageType.COM, UsageType.TRL, "a=2;b=2", "c=2;d=2", Action.UPDATE, FILE_NAME, assertionErrorList);

        // Verify Audit Log report results for Basic Offering1.
        final HashMap<String, List<String>> descriptionPropertyValues1 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                newBasicOfferingId1, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues1, basicOfferingName1, "name1", null, null, null, null, Status.NEW, Status.ACTIVE,
            UsageType.COM, UsageType.TRL, null, null, productLineId, productLineId1, MediaType.ELD, MediaType.DVD,
            getEnvironmentVariables(), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues1.containsKey(FILE_NAME), assertionErrorList);

        final boolean updateBasicOfferingLogFound2 =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOffering(newBasicOfferingId2,
                basicOfferingName2, "name2", null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, SupportLevel.ADVANCED, SupportLevel.BASIC, UsageType.TRL, UsageType.COM, null, null,
                Action.UPDATE, FILE_NAME, assertionErrorList);

        // Verify Audit Log report results for Basic Offering2.
        final HashMap<String, List<String>> descriptionPropertyValues2 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                newBasicOfferingId2, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues2, basicOfferingName2, "name2", null, null, null, null, null, null, UsageType.TRL,
            UsageType.COM, null, null, null, null, null, null, getEnvironmentVariables(), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues2.containsKey(FILE_NAME), assertionErrorList);

        final boolean updateBasicOfferingLogFound3 =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOffering(newBasicOfferingId3, null, null, null,
                null, null, null, Status.ACTIVE, Status.CANCELED, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, Action.UPDATE, FILE_NAME, assertionErrorList);

        // Verify Audit Log report results for Basic Offering3.
        final HashMap<String, List<String>> descriptionPropertyValues3 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                newBasicOfferingId3, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues3, null, null, null, null, null, null, Status.ACTIVE, Status.CANCELED, null, null,
            null, null, null, null, null, null, getEnvironmentVariables(), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues3.containsKey(FILE_NAME), assertionErrorList);

        final boolean updateBasicOfferingPriceLogFound1 =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOfferingPrice(basicOfferingPriceId1, null, null,
                null, null, null, null, "400", "1000", null, null, startDateInAuditLog, newStartDateForAuditLog,
                endDateInAuditLog, newEndDateForAuditLog, Action.UPDATE, FILE_NAME, assertionErrorList);

        // Query Audit Log Report for basic offering price.
        final HashMap<String, List<String>> descriptionPropertyValues4 = auditLogReportHelper
            .verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, newBasicOfferingId1,
                basicOfferingPriceId1, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues4, null, null, "400", "1000", null, null, startDateInAuditLog,
            newStartDateForAuditLog, endDateInAuditLog, newEndDateForAuditLog, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);

        createBasicOfferingPriceLogFound1 = BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOfferingPrice(
            basicOfferingPriceId2, null, newBasicOfferingId2, null, priceListId, null, storeExternalKey, null, "1000",
            null, String.valueOf(Currency.USD.getCode()), null, startDateInAuditLog, null, endDateInAuditLog,
            Action.CREATE, FILE_NAME, assertionErrorList);

        // Query Audit Log Report for basic offering price.
        final HashMap<String, List<String>> descriptionPropertyValues5 = auditLogReportHelper
            .verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, newBasicOfferingId2,
                basicOfferingPriceId2, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues5, null, Currency.USD + " (4)", null, "1000", null,
            priceListExternalKey + " (" + priceListId + ")", null, startDateInAuditLog, null, endDateInAuditLog,
            assertionErrorList);
        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);

        AssertCollector.assertTrue(
            "Update Basic Offering Audit Log not found for Basic Offering id : " + newBasicOfferingId1,
            updateBasicOfferingLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Update Basic Offering Audit Log not found for Basic Offering id : " + newBasicOfferingId2,
            updateBasicOfferingLogFound2, assertionErrorList);
        AssertCollector.assertTrue(
            "Update Basic Offering Audit Log not found for Basic Offering id : " + newBasicOfferingId3,
            updateBasicOfferingLogFound3, assertionErrorList);
        AssertCollector.assertTrue(
            "Update Basic Offering Price Audit Log not found for Basic Offering Price id : " + basicOfferingPriceId1,
            updateBasicOfferingPriceLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Basic Offering Price Audit Log not found for Basic Offering Price id : " + basicOfferingPriceId2,
            createBasicOfferingPriceLogFound1, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the description in the view audit log report.
     *
     * @throws IOException
     */
    @Test
    public void testDescriptionInAuditLog() throws IOException {
        // create entries of Basic Offering by writing to a file
        name1 = "BasicOffering_name_" + RandomStringUtils.randomAlphanumeric(4);
        externalKey1 = "BasicOffering_ext_" + RandomStringUtils.randomAlphanumeric(5);
        createXlsxAndWriteDataForBasicOffering(FILE_NAME, name1, externalKey1, OfferingType.PERPETUAL, Status.NEW,
            productLineName, MediaType.ELD, "EN", offeringDetail, UsageType.COM, SupportLevel.BASIC, null, null,
            "a=2;b=2", false);

        // Uploading the file
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);

        // Wait for 2 min to finish processing
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // Find the Basic Offering Id
        findBasicOfferingPage = adminToolPage.getPage(FindBasicOfferingPage.class);
        basicOfferingPage = findBasicOfferingPage.findBasicOfferingByExternalKey(externalKey1);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        basicOfferingId1 = basicOfferingPage.getId();

        // Verify Audit Log report results for Basic Offering1.
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
            basicOfferingId1, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        final List<String> descriptionList = auditLogReportResultPage.getValuesFromDescriptionColumn();
        AssertCollector.assertThat("Incorrect description for feature update entry", descriptionList.get(0),
            equalTo(PelicanConstants.DESCRIPTION_CHANGES_FOR_UPLOAD_BASIC_OFFERING), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the description in the download audit log report.
     *
     * @throws IOException
     */
    @Test
    public void testDescriptionInDownloadAuditLogReport() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        // create entries of Basic Offering by writing to a file
        name1 = "BasicOffering_name_" + RandomStringUtils.randomAlphanumeric(4);
        externalKey1 = "BasicOffering_ext_" + RandomStringUtils.randomAlphanumeric(5);
        createXlsxAndWriteDataForBasicOffering(FILE_NAME, name1, externalKey1, OfferingType.PERPETUAL, Status.NEW,
            productLineName, MediaType.ELD, "EN", offeringDetail, UsageType.COM, SupportLevel.BASIC, null, null,
            "a=2;b=2", false);

        // Uploading the file
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);

        // Wait for 2 min to finish processing
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        uploadUtils.uploadBasicOffering(adminToolPage, FILE_NAME);
        Util.waitInSeconds(TimeConstants.TWO_MINS);

        // Find the Basic Offering Id
        findBasicOfferingPage = adminToolPage.getPage(FindBasicOfferingPage.class);
        basicOfferingPage = findBasicOfferingPage.findBasicOfferingByExternalKey(externalKey1);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        basicOfferingId1 = basicOfferingPage.getId();

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId1,
            adminToolUserId, true);
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String description = fileData[1][PelicanConstants.DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT];
        AssertCollector.assertThat("Incorrect description for feature update entry", description,
            equalTo(PelicanConstants.DESCRIPTION_CHANGES_FOR_UPLOAD_BASIC_OFFERING), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to assert the job entity
     */
    private void assertEntityType(final GenericGrid result) {
        // Validate job entity as 'BASIC OFFERINGS'
        final List<String> actualEntityType = result.getColumnValues("Entity Type");
        for (final String entityType : actualEntityType) {
            AssertCollector.assertThat("AdminTool: Found entity type other than <b>BASIC OFFERING</b>", entityType,
                equalTo("BASIC_OFFERING"), assertionErrorList);
        }
    }

    /**
     * Method to create and write xls with Basic Offerings
     */
    private void createXlsxAndWriteDataForBasicOffering(final String fileName, final String name,
        final String externalKey, final OfferingType offeringType, final Status status, final String productLine,
        final MediaType mediaType, final String languageCode, final String offeringDetail, final UsageType usageType,
        final SupportLevel supportLevel, final String amount, final Currency currency, final String properties,
        final boolean append) throws IOException {

        xlsUtils = new XlsUtils();
        // Add basic Offerings header values to list
        columnHeaders = new ArrayList<>();
        columnData = new ArrayList<>();
        columnHeaders
            .add("#BasicOffering,name,externalKey,offeringType,usageType,status,productLine,mediaType,languageCode,"
                + "offeringDetail,supportLevel,amount,currency,properties");

        // Add basic Offerings data to list
        columnData.add("BasicOffering," + name + "," + externalKey + "," + offeringType + ","
            + usageType.getUploadName() + "," + status + "," + productLine + "," + mediaType + "," + languageCode + ","
            + offeringDetail + "," + supportLevel + "," + amount + "," + currency + "," + properties);

        // Write Basic Offerings price headers and data to excel.
        // modify the count to create multiple Offering prices for load test
        xlsUtils.createAndWriteToXls(fileName, columnHeaders, columnData, append);
    }

    /**
     * Method to create Basic Offering Price in XLS
     */
    private void createXlsxAndWriteDataForBasicOfferingPrice(final String fileName, final String offeringExternalKey,
        final String priceListExternalKey, final String storeExternalKey, final String amount, final Currency currency,
        final String startDate, final String endDate, final String priceId, final boolean append) throws IOException {

        xlsUtils = new XlsUtils();
        // Add Basic Offering Price headers to list.
        columnHeaders = new ArrayList<>();
        columnData = new ArrayList<>();
        columnHeaders.add("#BasicOfferingPrice,offering,priceList,store,amount,currency,startDate,endDate,priceId");

        // Add Basic Offerings data to list.
        columnData.add("BasicOfferingPrice," + offeringExternalKey + "," + priceListExternalKey + "," + storeExternalKey
            + "," + amount + "," + currency + "," + startDate + "," + endDate + "," + priceId);

        // Write Basic Offering price headers and data to excel.
        xlsUtils.createAndWriteToXls(fileName, columnHeaders, columnData, append);
    }

    /**
     * This method create and write to xlsx
     */
    private void createXlsxAndWriteData(final String FILE_NAME) throws IOException {
        final XlsUtils utils = new XlsUtils();
        // Add basic Offerings header values to list
        ArrayList<String> columnHeaders = new ArrayList<>();
        ArrayList<String> columnData = new ArrayList<>();

        String basicOfferingExternalKey = "BASIC_EXT_KEY_" + RandomStringUtils.randomAlphanumeric(3);
        String basicOfferingName = "BASIC_EXT_NAME_" + RandomStringUtils.randomAlphanumeric(3);
        columnHeaders.add("#BasicOffering,name,externalKey,offeringType,status,productLine,mediaType,languageCode");

        // Add basic Offerings data to list
        columnData.add(
            "BasicOffering," + basicOfferingName + "," + basicOfferingExternalKey + ",PERPETUAL,NEW," + "ACD,ELD,en");

        // Write Basic Offerings price headers and data to excel.
        // modify the count to create multiple Offering prices for load test
        utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, false);

        columnHeaders.add("#BasicOfferingPrice,offering,priceList,store,amount,currency,startDate,endDate");
        // Add Basic Offerings data to list.
        // Adding one day in order to handle the UTC time conversion after 4 PM
        String startDate = DateTimeUtils.getNowPlusDays(DATE_FORMAT_FOR_UPLOAD, 1);
        String endDate = DateTimeUtils.getNowPlusDays(DATE_FORMAT_FOR_UPLOAD, 30);
        for (int j = 0; j < 5; j++) {
            columnData.add("BasicOfferingPrice," + basicOfferingExternalKey + "," + priceListExternalKey + "," + ""
                + storeExternalKey + ",200.00,USD," + startDate + "," + endDate);
            startDate = DateTimeUtils.addDaysToDate(endDate, DATE_FORMAT_FOR_UPLOAD, 1);
            endDate = DateTimeUtils.addDaysToDate(startDate, DATE_FORMAT_FOR_UPLOAD, 100);
        }
        // Write Basic Offering price headers and data to excel.
        utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, true);

        // Add basic Offerings header values to list
        columnHeaders = new ArrayList<>();
        columnData = new ArrayList<>();
        basicOfferingExternalKey = "BASIC_EXT_KEY_" + RandomStringUtils.randomAlphanumeric(3);
        basicOfferingName = "BASIC_EXT_NAME_" + RandomStringUtils.randomAlphanumeric(3);
        columnHeaders.add("#BasicOffering,name,externalKey,offeringType,status,productLine,mediaType,languageCode");

        // Add basic Offerings data to list
        columnData.add("BasicOffering," + basicOfferingName + "," + basicOfferingExternalKey + ",PERPETUAL,NEW,"
            + "3DSMAX,ELD,en");

        // Write Basic Offerings price headers and data to excel.
        utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, true);
    }
}
