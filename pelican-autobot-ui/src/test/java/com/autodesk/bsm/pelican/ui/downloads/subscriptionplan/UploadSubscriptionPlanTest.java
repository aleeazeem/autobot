package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.OFFERING_DETAILS1;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlansClient.Parameter;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferData;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlans;
import com.autodesk.bsm.pelican.commonassertions.AssertionsForViewSubscriptionPlanPage;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAndBasicOfferingsAuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanDynamoQuery;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.UploadSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.ViewUploadStatusSubscriptionPlanPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.ItemUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.Iterables;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
import java.util.Map;

/**
 * Admin Tool's Upload Subscription Plans tests. On Admin Tool's Main Tab navigate to Subscriptions -> Upload
 * Subscription Plans Validate if the Subscription Plans xlsx file has been uploaded
 *
 * @author sunitha
 */
public class UploadSubscriptionPlanTest extends SeleniumWebdriver {

    private static final String FILE_NAME = "UploadSubscriptionPlan.xlsx";
    private static final String LAST_FEATURE_COMPOSITION_TIME = "lastFeatureCompositionChangedTime";
    private static final String INVALID_FILE_NAME = "invalid_file_name.csv";
    private static final String BILLING_PERIOD_ORIG_VALUE = "SubscriptionPeriod[count=1,type=MONTH]";
    private static final String SUBSCRIPTION_PLAN_PREFIX = "SubPlan_Name_";
    private static final String ORIG_PRICE = "100.00";

    private PelicanPlatform resource;
    private AdminToolPage adminToolPage;
    private UploadSubscriptionPlanPage uploadSubscriptionPlanPage;
    private UploadUtils uploadUtils;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private static final Date DATE_TODAY =
        DateTimeUtils.convertStringToDate(DateTimeUtils.getCurrentDate(), PelicanConstants.DATE_FORMAT_WITH_SLASH);
    private static String effectiveStartDate =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 30);
    private static String effectiveEndDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15);
    private static FindSubscriptionPlanPage findSubscriptionPlanPage;
    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private boolean createSubscriptionPlanLogFound1 = false;
    private boolean createSubscriptionPlanLogFound2 = false;
    private boolean updateSubscriptionPlanLogFound1 = false;
    private String productLineName;
    private String productLineName1;
    private String externalKey1;
    private String externalKey2;
    private ItemUtils itemUtils;
    private String externalKey3;
    private String externalKey4;
    private String externalKey5;
    private String entitlementId;
    private String entlmntExternalKey;
    private String subscriptionPlanId;
    private String subscriptionPlanId2;
    private String subscriptionPlanId3;
    private String subscriptionPlanName1;
    private String subscriptionPlanName2;
    private String subscriptionPlanName3;
    private Map<String, String> requestParam;
    private SubscriptionPlans subscriptionPlans;
    private String productLineId;
    private String productLineId1;
    private String offeringDetailId;
    private String autoGeneratedSubscriptionPlanExternalKey;
    private String subscriptionPlanNameForAutoGenerate;
    private String autoGeneratedOfferExternalKey;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private String subsPlanName1;
    private String subsPlanName2;
    private SubscriptionOfferData subscriptionOfferData = new SubscriptionOfferData(null, 1, BillingFrequency.MONTH);
    private FeatureApiUtils featureApiUtils;
    private AuditLogReportResultPage auditLogReportResultPage;
    private AuditLogReportPage auditLogReportPage;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private boolean isCseHeadersFeatureFlagChanged;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;

    private Item item1;
    private Item item2;
    private Item item3;
    private String eolImmediateDate;
    private String eolRenewalDate;
    private String eosDate;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionPlanTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        itemUtils = new ItemUtils(getEnvironmentVariables());
        adminToolUserId = getEnvironmentVariables().getUserId();
        adminToolPage.login();

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        uploadSubscriptionPlanPage = adminToolPage.getPage(UploadSubscriptionPlanPage.class);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        auditLogReportResultPage = adminToolPage.getPage(AuditLogReportResultPage.class);

        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

        final SubscriptionPlanApiUtils subscriptionPlanUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        productLineName = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        ProductLine productLine = subscriptionPlanUtils.addProductLine(productLineName);
        productLineId = productLine.getData().getId();

        productLineName1 = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        productLine = subscriptionPlanUtils.addProductLine(productLineName1);
        productLineId1 = productLine.getData().getId();
        featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        offeringDetailId = getEnvironmentVariables().getOfferingDetailId();

        final Applications applications = resource.application().getApplications();
        String appId = "";
        String featureTypeId = null;

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        final List<Map<String, String>> resultMapList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ITEM_TYPE_ID,
                PelicanConstants.CSR_FEATURE_TYPE_EXTERNAL_KEY, appId), getEnvironmentVariables());

        if (resultMapList.size() == 0) {

            final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
            final String featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

            // Navigate to the add feature page and add a feature
            addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
                getEnvironmentVariables().getApplicationDescription(), featureTypeName,
                PelicanConstants.CSR_EXTERNAL_KEY);
            final FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
            featureTypeId = featureTypeDetailPage.getId();
        } else {
            featureTypeId = resultMapList.get(0).get("ID");
        }

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item1 = featureApiUtils.addFeature(null, null, featureTypeId);
        item2 = featureApiUtils.addFeature(null, null, featureTypeId);

        final String itemName3 = RandomStringUtils.randomAlphanumeric(6);
        item3 = itemUtils.addItem(itemName3, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName3);

        eosDate = new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        eolRenewalDate = new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        eolImmediateDate = new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isCseHeadersFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, true);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        if (uploadUtils.deleteFilefromLocal(FILE_NAME)) {
            LOGGER.info(FILE_NAME + " is successfully deleted from /testdata");
        } else {
            LOGGER.warn(FILE_NAME + " is NOT deleted from /testdata");
        }
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
        if (isCseHeadersFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, false);
        }

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
    }

    /**
     * Method to Upload Subscription Plans
     */
    @Test
    public void uploadSubscriptionPlans() throws IOException {
        createXlsxAndWriteData(FILE_NAME, null, null, null, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.NCM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null, false, true,
            PackagingType.NULL, null);
        final GenericGrid searchResult = uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

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
     * Test Method to Upload Subscription Plans to verify UTF8 FileName
     */
    @Test
    public void testUploadSubscriptionPlansVerifyUTF8FileName() throws IOException {

        final String UTF_FILE_NAME = "SubscriptionPlans" + "赛巴巴.xlsx";
        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(UTF_FILE_NAME, null, null, new SubscriptionOfferData(null, 1, BillingFrequency.MONTH),
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, true,
            null, null, false, null, null, false, true, PackagingType.NULL, null);

        final GenericGrid searchResult = uploadUtils.uploadSubscriptionPlan(adminToolPage, UTF_FILE_NAME);
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
        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);
        // Validate pagination (i.e. results per page)

        AssertCollector.assertThat(
            "Expected " + UTF_FILE_NAME + " File, but found " + searchResult.getColumnValues("File Name").get(0)
                + " File",
            searchResult.getColumnValues("File Name").get(0), equalTo(UTF_FILE_NAME), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify File invalid upload file Error message.
     */
    @Test
    public void verifyInvalidFileUploadErrorMessage() {
        uploadUtils.uploadSubscriptionPlan(adminToolPage, INVALID_FILE_NAME);
        final String actualErrorMsg = uploadSubscriptionPlanPage.getH3ErrorMessage();
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
        createXlsxAndWriteData(FILE_NAME, null, null, null, OfferingType.META_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.IMMEDIATE_NO_REFUND, UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName,
            SupportLevel.ADVANCED, null, null, false, null, null, false, null, null, false, true, PackagingType.NULL,
            null);
        final GenericGrid searchResult = uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);
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
     * Test case to verify upload status page results
     */
    @Test
    public void verifyUploadStatusPageResultsTest() throws IOException, ParseException {
        // Create and upload a file and then navigate to viewUplaodStatus page
        createXlsxAndWriteData(FILE_NAME, null, null, null, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.TRL.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.BASIC, null, null, false, null, null, false, null, null, false, true,
            PackagingType.NULL, null);
        final GenericGrid searchResult = uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);
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
        // Assertions on the values under each header
        final List<String> actualAppFamilyId = searchResult.getColumnValues("App Family");
        AssertCollector.assertThat("AdminTool: Found uploads other than <b>AUTO Family</b>", actualAppFamilyId,
            everyItem(equalTo("2001")), assertionErrorList);
        AssertCollector.assertThat("Incorrect created by", searchResult.getColumnValues("Created By").get(0),
            equalTo("svc_p_pelican"), assertionErrorList);
        AssertCollector.assertThat(
            "Expected " + FILE_NAME + " File, but found " + searchResult.getColumnValues("File Name").get(0) + " File",
            searchResult.getColumnValues("File Name").get(0), equalTo(FILE_NAME), assertionErrorList);

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
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with COMPLETED job status
     *
     * @result Valid header and data is returned
     */
    @Test
    public void findJobsWithCompletedJobStatusFilter() {

        final ViewUploadStatusSubscriptionPlanPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusSubscriptionPlanPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_COMPLETED);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_SUBSCRIPTION_PLANS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        viewUploadStatusPage.submit(TimeConstants.ONE_SEC);

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

        // Assert that job entity is 'Subscription Plans'
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

        final ViewUploadStatusSubscriptionPlanPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusSubscriptionPlanPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_IN_PROGRESS_DROPDOWN);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_SUBSCRIPTION_PLANS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        viewUploadStatusPage.submit(TimeConstants.ONE_SEC);

        final GenericGrid searchResult = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        final int totalItems = searchResult.getTotalItems();
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
            // Assert that entity type is 'Subscription Plan'
            assertEntityType(searchResult);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the upload jobs with FAILED job status
     *
     * @result Valid header and data is returned
     */
    @Test
    public void findJobsWithFailedJobStatusFilter() {

        final ViewUploadStatusSubscriptionPlanPage viewUploadStatusPage =
            adminToolPage.getPage(ViewUploadStatusSubscriptionPlanPage.class);
        viewUploadStatusPage.selectJobStatus(PelicanConstants.JOB_STATUS_FAILED);
        viewUploadStatusPage.selectJobEntity(PelicanConstants.ENTITY_SUBSCRIPTION_PLANS);

        // Waiting for the page components to get loaded completely. Don't
        // remove as the test fails
        viewUploadStatusPage.submit(TimeConstants.ONE_SEC);

        final GenericGrid searchResult = adminToolPage.getPage(GenericGrid.class);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", searchResult.getTotalItems(), greaterThanOrEqualTo(0),
            assertionErrorList);

        // Validate job status as 'FAILED'
        final List<String> actualJobStatus = searchResult.getColumnValues("Status");
        for (final String status : actualJobStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found jobs other than <b>FAILED</b>", status,
                    equalTo(Status.FAILED.toString()), assertionErrorList);
            }
        }
        // Assert that job entity type is 'Subscription Plan'
        assertEntityType(searchResult);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify offer price can be added with dates in past for subscription plan through upload a subscription plan in UI
     *
     * @result Date should be added in Past
     */
    @Test
    public void addPricesWithPastDateThroughUploadSubscriptionPlan() throws IOException {
        final String subscriptionPlanExternalKeyForUpload = "SubPlan_ExtKey" + RandomStringUtils.randomAlphanumeric(5);
        createXlsxAndWriteData(FILE_NAME, null, subscriptionPlanExternalKeyForUpload,
            new SubscriptionOfferData(null, 1, BillingFrequency.MONTH), OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, true, null, null, false, null, null, false, true,
            PackagingType.NULL, null);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);
        findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(subscriptionPlanExternalKeyForUpload);
        final Date getEffectiveEndDateInDateFormat = DateTimeUtils.convertStringToDate(
            subscriptionPlanDetailPage.getEffectiveEndDateOfPriceInAnOffer(), PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Date getEffectiveStartDateInDateFormat =
            DateTimeUtils.convertStringToDate(subscriptionPlanDetailPage.getEffectiveStartDateOfPriceInAnOffer(),
                PelicanConstants.DATE_FORMAT_WITH_SLASH);
        AssertCollector.assertThat("Effective End Date is bigger than current Date ", getEffectiveEndDateInDateFormat,
            lessThan(DATE_TODAY), assertionErrorList);
        AssertCollector.assertThat("Effective Start Date is bigger than End Date ", getEffectiveStartDateInDateFormat,
            lessThan(getEffectiveEndDateInDateFormat), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case test Audit log for Subscription Plan Creation Through Upload This verifies Multi fields and multi
     * plans Audit log
     */
    @Test
    public void testAuditLogOfCreateSubscriptionPlansThroughUpload() throws IOException {

        // create external key to be unique
        externalKey1 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        externalKey2 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        externalKey3 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        // Create a file and write to the file
        createXlsxAndWriteData(FILE_NAME, null, externalKey1, null, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null, false, true,
            PackagingType.NULL, PelicanConstants.YES.toLowerCase());
        createXlsxAndWriteData(FILE_NAME, null, externalKey2, null, OfferingType.META_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, false, null, null, true, null, null, false, true,
            PackagingType.NULL, null);
        createXlsxAndWriteData(FILE_NAME, null, externalKey3, null, OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.IMMEDIATE_NO_REFUND, UsageType.TRL.getUploadName(), OFFERING_DETAILS1, productLineName,
            SupportLevel.BASIC, null, null, false, null, null, true, null, null, false, true, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey1);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();
        AssertCollector.assertThat("Send Expiration Reminder Emails is No/False for " + subscriptionPlanId,
            subscriptionPlanDetailPage.getSendExpirationReminderEmails(), equalTo(PelicanConstants.YES),
            assertionErrorList);
        subscriptionPlanName1 = subscriptionPlanDetailPage.getName();

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey2);
        subscriptionPlanId2 = subscriptionPlanDetailPage.getId();
        AssertCollector.assertThat("Send Expiration Reminder Emails is No/False for " + subscriptionPlanId2,
            subscriptionPlanDetailPage.getSendExpirationReminderEmails(), equalTo(PelicanConstants.YES),
            assertionErrorList);
        subscriptionPlanName2 = subscriptionPlanDetailPage.getName();

        // Find the id of the Subscription Plan based on external key
        // TODO here using API to get Plan id, because NEW plans are not
        // returned based on external key in Admin Tool
        requestParam = new HashMap<>();
        requestParam.put(Parameter.PLAN_EXT_KEYS.getName(), externalKey3);
        subscriptionPlans = resource.subscriptionPlans().getSubscriptionPlans(requestParam);
        subscriptionPlanId3 = subscriptionPlans.getSubscriptionPlans().get(0).getId();
        subscriptionPlanName3 = subscriptionPlans.getSubscriptionPlans().get(0).getName();

        // Query Dynamo DB for each Subscription Plan
        SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(subscriptionPlanId).setOldName(null).setNewName(subscriptionPlanName1)
            .setOldExternalKey(null).setNewExternalKey(externalKey1).setOldOfferingType(null)
            .setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null).setNewStatus(Status.ACTIVE)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD)
            .setOldUsageType(null).setNewUsageType(UsageType.COM).setOldOfferingDetailId(null)
            .setNewOfferingDetailId(offeringDetailId).setOldProductLine(null).setNewProductLine(productLineId)
            .setOldSupportLevel(null).setNewSupportLevel(SupportLevel.ADVANCED).setAction(Action.CREATE)
            .setFileName(FILE_NAME).setOldPackagingValue(null).setNewPackagingValue(null)
            .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        createSubscriptionPlanLogFound1 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(subscriptionPlanId2)
            .setOldName(null).setNewName(subscriptionPlanName2).setOldExternalKey(null).setNewExternalKey(externalKey2)
            .setOldOfferingType(null).setNewOfferingType(OfferingType.META_SUBSCRIPTION).setOldStatus(null)
            .setNewStatus(Status.ACTIVE).setOldCancellationPolicy(null)
            .setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD).setOldUsageType(null)
            .setNewUsageType(UsageType.COM).setOldOfferingDetailId(null).setNewOfferingDetailId(offeringDetailId)
            .setOldProductLine(null).setNewProductLine(productLineId).setOldSupportLevel(null)
            .setNewSupportLevel(SupportLevel.ADVANCED).setAction(Action.CREATE).setFileName(FILE_NAME)
            .setOldPackagingValue(null).setNewPackagingValue(null).setOldExpReminderEmailEnabled(null)
            .setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        createSubscriptionPlanLogFound2 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(subscriptionPlanId3)
            .setOldName(null).setNewName(subscriptionPlanName3).setOldExternalKey(null).setNewExternalKey(externalKey3)
            .setOldOfferingType(null).setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null)
            .setNewStatus(Status.NEW).setOldCancellationPolicy(null)
            .setNewCancellationPolicy(CancellationPolicy.IMMEDIATE_NO_REFUND).setOldUsageType(null)
            .setNewUsageType(UsageType.TRL).setOldOfferingDetailId(null).setNewOfferingDetailId(offeringDetailId)
            .setOldProductLine(null).setNewProductLine(productLineId).setOldSupportLevel(null)
            .setNewSupportLevel(SupportLevel.BASIC).setAction(Action.CREATE).setFileName(FILE_NAME)
            .setOldPackagingValue(null).setNewPackagingValue(null).setOldExpReminderEmailEnabled(null)
            .setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        final boolean createSubscriptionPlanLogFound3 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId,
            createSubscriptionPlanLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId2,
            createSubscriptionPlanLogFound2, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId3,
            createSubscriptionPlanLogFound3, assertionErrorList);

        // Query Audit Log Report for each subscription plan1
        final HashMap<String, List<String>> descriptionPropertyValues1 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // External Key
        final List<String> externalKeyValues1 = descriptionPropertyValues1.get(PelicanConstants.EXTERNAL_KEY_FIELD);
        AssertCollector.assertThat("Invalid old external key value in audit log report", externalKeyValues1.get(0),
            nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new external key value in audit log report", externalKeyValues1.get(1),
            equalTo(externalKey1), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues1.containsKey(FILE_NAME), assertionErrorList);

        final HashMap<String, List<String>> descriptionPropertyValues2 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId2, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // External Key
        final List<String> externalKeyValues2 = descriptionPropertyValues2.get(PelicanConstants.EXTERNAL_KEY_FIELD);
        AssertCollector.assertThat("Invalid old external key value in audit log report", externalKeyValues2.get(0),
            nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new external key value in audit log report", externalKeyValues2.get(1),
            equalTo(externalKey2), assertionErrorList);
        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues2.containsKey(FILE_NAME), assertionErrorList);

        final HashMap<String, List<String>> descriptionPropertyValues3 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId3, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // External Key
        final List<String> externalKeyValues3 = descriptionPropertyValues3.get(PelicanConstants.EXTERNAL_KEY_FIELD);
        AssertCollector.assertThat("Invalid old external key value in audit log report", externalKeyValues3.get(0),
            nullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid new external key value in audit log report", externalKeyValues3.get(1),
            equalTo(externalKey3), assertionErrorList);
        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues3.containsKey(FILE_NAME), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case test Audit log for Subscription Plan Update Through Upload This verifies Multi fields and multi
     * plans Audit log
     */
    @Test(dependsOnMethods = { "testAuditLogOfCreateSubscriptionPlansThroughUpload" })
    public void testAuditLogOfUpdateSubscriptionPlansThroughUpload() throws IOException {

        // Using the external keys of depends on method, updating the
        // Subscription plan
        createXlsxAndWriteData(FILE_NAME, "NewName1", externalKey1, null, OfferingType.BIC_SUBSCRIPTION,
            Status.CANCELED, CancellationPolicy.IMMEDIATE_NO_REFUND, UsageType.TRL.getUploadName(), OFFERING_DETAILS1,
            productLineName1, SupportLevel.BASIC, null, null, false, null, null, false, null, null, false, true,
            PackagingType.NULL, PelicanConstants.NO.toLowerCase());
        createXlsxAndWriteData(FILE_NAME, "NewName2", externalKey2, null, OfferingType.META_SUBSCRIPTION,
            Status.CANCELED, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(),
            OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, false, null, null, true, null, null,
            false, true, PackagingType.NULL, PelicanConstants.NO.toLowerCase());
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName3, externalKey3, null, OfferingType.BIC_SUBSCRIPTION,
            Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(),
            OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, false, null, null, true, null, null,
            false, true, PackagingType.NULL, PelicanConstants.NO.toLowerCase());

        // Uploading the file
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Query Dynamo DB for Subscription Plan update
        SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(subscriptionPlanId).setOldName(subscriptionPlanName1).setNewName("NewName1")
            .setOldExternalKey(null).setNewExternalKey(null).setOldOfferingType(null).setNewOfferingType(null)
            .setOldStatus(Status.ACTIVE).setNewStatus(Status.CANCELED)
            .setOldCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD)
            .setNewCancellationPolicy(CancellationPolicy.IMMEDIATE_NO_REFUND).setOldUsageType(UsageType.COM)
            .setNewUsageType(UsageType.TRL).setOldOfferingDetailId(null).setNewOfferingDetailId(null)
            .setOldProductLine(productLineId).setNewProductLine(productLineId1)
            .setOldSupportLevel(SupportLevel.ADVANCED).setNewSupportLevel(SupportLevel.BASIC).setAction(Action.UPDATE)
            .setFileName(FILE_NAME).setOldPackagingValue(null).setNewPackagingValue(null)
            .setOldExpReminderEmailEnabled(PelicanConstants.TRUE).setNewExpReminderEmailEnabled(PelicanConstants.FALSE)
            .build();

        updateSubscriptionPlanLogFound1 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(subscriptionPlanId2)
            .setOldName(subscriptionPlanName2).setNewName("NewName2").setOldExternalKey(null).setNewExternalKey(null)
            .setOldOfferingType(null).setNewOfferingType(null).setOldStatus(Status.ACTIVE).setNewStatus(Status.CANCELED)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(null).setOldUsageType(null).setNewUsageType(null)
            .setOldOfferingDetailId(null).setNewOfferingDetailId(null).setOldProductLine(null).setNewProductLine(null)
            .setOldSupportLevel(null).setNewSupportLevel(null).setAction(Action.UPDATE).setFileName(FILE_NAME)
            .setOldPackagingValue(null).setNewPackagingValue(null).setOldExpReminderEmailEnabled(PelicanConstants.TRUE)
            .setNewExpReminderEmailEnabled(PelicanConstants.FALSE).build();

        final boolean updateSubscriptionPlanLogFound2 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(subscriptionPlanId3)
            .setOldName(null).setNewName(null).setOldExternalKey(null).setNewExternalKey(null).setOldOfferingType(null)
            .setNewOfferingType(null).setOldStatus(Status.NEW).setNewStatus(Status.ACTIVE)
            .setOldCancellationPolicy(CancellationPolicy.IMMEDIATE_NO_REFUND)
            .setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD).setOldUsageType(UsageType.TRL)
            .setNewUsageType(UsageType.COM).setOldOfferingDetailId(null).setNewOfferingDetailId(null)
            .setOldProductLine(null).setNewProductLine(null).setOldSupportLevel(SupportLevel.BASIC)
            .setNewSupportLevel(SupportLevel.ADVANCED).setAction(Action.UPDATE).setFileName(FILE_NAME)
            .setOldPackagingValue(null).setNewPackagingValue(null).setOldExpReminderEmailEnabled(PelicanConstants.TRUE)
            .setNewExpReminderEmailEnabled(PelicanConstants.FALSE).build();

        final boolean updateSubscriptionPlanLogFound3 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        AssertCollector.assertTrue(
            "Update Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId,
            updateSubscriptionPlanLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Update Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId2,
            updateSubscriptionPlanLogFound2, assertionErrorList);
        AssertCollector.assertTrue(
            "Update Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId3,
            updateSubscriptionPlanLogFound3, assertionErrorList);

        // Query Audit Log Report for each subscription plan
        final HashMap<String, List<String>> descriptionPropertyValues1 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues1.containsKey(FILE_NAME), assertionErrorList);

        final HashMap<String, List<String>> descriptionPropertyValues2 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId2, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues2.containsKey(FILE_NAME), assertionErrorList);

        final HashMap<String, List<String>> descriptionPropertyValues3 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId3, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues3.containsKey(FILE_NAME), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case test Audit log for Subscription Plan Creation and Update Through one Upload This verifies Multi
     * fields and multi plans Audit log
     */
    @Test
    public void testAuditLogOfCreateAndUpdateSubscriptionPlansThroughUpload() throws IOException {

        // create external key to be unique
        final String externalKey = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        final String subscriptionPlanName4 = SUBSCRIPTION_PLAN_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        // Create a file and write to the file
        // 1st line - New Subscription Plan
        // 2nd line - Update to existing subscription plan
        // 3rd line - Update to 1st line subscription plan
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName4, externalKey, null, OfferingType.BIC_SUBSCRIPTION,
            Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(),
            OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null,
            false, true, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName4, externalKey, null, OfferingType.BIC_SUBSCRIPTION,
            Status.CANCELED, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.TRL.getUploadName(),
            OFFERING_DETAILS1, productLineName, SupportLevel.BASIC, null, null, false, null, null, true, null, null,
            false, true, PackagingType.NULL, PelicanConstants.NO.toUpperCase());

        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        requestParam = new HashMap<>();
        requestParam.put(Parameter.PLAN_EXT_KEYS.getName(), externalKey);
        subscriptionPlans = resource.subscriptionPlans().getSubscriptionPlans(requestParam);
        final String subscriptionPlanId4 = subscriptionPlans.getSubscriptionPlans().get(0).getId();

        // Query Dynamo DB for each Subscription Plan
        SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(subscriptionPlanId4).setOldName(null).setNewName(subscriptionPlanName4)
            .setOldExternalKey(null).setNewExternalKey(externalKey).setOldOfferingType(null)
            .setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null).setNewStatus(Status.NEW)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD)
            .setOldUsageType(null).setNewUsageType(UsageType.COM).setOldOfferingDetailId(null)
            .setNewOfferingDetailId(offeringDetailId).setOldProductLine(null).setNewProductLine(productLineId)
            .setOldSupportLevel(null).setNewSupportLevel(SupportLevel.ADVANCED).setAction(Action.CREATE)
            .setFileName(FILE_NAME).setOldPackagingValue(null).setNewPackagingValue(null)
            .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        createSubscriptionPlanLogFound1 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(subscriptionPlanId4)
            .setOldName(null).setNewName(null).setOldExternalKey(null).setNewExternalKey(null).setOldOfferingType(null)
            .setNewOfferingType(null).setOldStatus(Status.NEW).setNewStatus(Status.CANCELED)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(null).setOldUsageType(UsageType.COM)
            .setNewUsageType(UsageType.TRL).setOldOfferingDetailId(null).setNewOfferingDetailId(null)
            .setOldProductLine(null).setNewProductLine(null).setOldSupportLevel(SupportLevel.ADVANCED)
            .setNewSupportLevel(SupportLevel.BASIC).setAction(Action.UPDATE).setFileName(FILE_NAME)
            .setOldPackagingValue(null).setNewPackagingValue(null).setOldExpReminderEmailEnabled(PelicanConstants.TRUE)
            .setNewExpReminderEmailEnabled(PelicanConstants.FALSE).build();

        updateSubscriptionPlanLogFound1 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId4,
            createSubscriptionPlanLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Update Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId4,
            updateSubscriptionPlanLogFound1, assertionErrorList);

        // Query Audit Log Report for each subscription plan1
        final HashMap<String, List<String>> descriptionPropertyValues4 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId4, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // External Key
        final List<String> externalKeyValues4 = descriptionPropertyValues4.get(PelicanConstants.EXTERNAL_KEY_FIELD);
        AssertCollector.assertThat("Invalid old id value in audit log report", externalKeyValues4.get(0), nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid new id value in audit log report", externalKeyValues4.get(1),
            equalTo(externalKey), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);

        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId4, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        // Action
        final List<String> actionValues =
            descriptionPropertyValues.get(StringUtils.capitalize(PelicanConstants.STATUS));
        AssertCollector.assertThat("Invalid old action value in audit log report", actionValues.get(0),
            equalTo(Status.NEW.toString()), assertionErrorList);
        AssertCollector.assertThat("Invalid new action value in audit log report", actionValues.get(1),
            equalTo(Status.CANCELED.toString()), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues.containsKey(FILE_NAME), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case to update subscription plan with entitlements having only entitlement section
     *
     * @throws IOException
     */
    @Test
    public void testAuditLogOfCreateAndUpdateSubscriptionPlansEntitlementsThroughUpload() throws IOException {

        // create external key to be unique
        final String externalKey = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(6);
        final String subscriptionPlanName4 = SUBSCRIPTION_PLAN_PREFIX + RandomStringUtils.randomAlphanumeric(6);

        final String itemName1 = RandomStringUtils.randomAlphanumeric(6);

        final Item item1 = itemUtils.addItem(itemName1, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName1);

        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName4, externalKey, null, OfferingType.BIC_SUBSCRIPTION,
            Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(),
            OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null,
            false, true, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);

        createXlsxAndWriteDataForEntitlements(FILE_NAME, externalKey, item1.getExternalKey(),
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, null, null, false);

        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        requestParam = new HashMap<>();
        requestParam.put(Parameter.PLAN_EXT_KEYS.getName(), externalKey);
        subscriptionPlans = resource.subscriptionPlans().getSubscriptionPlans(requestParam);
        AssertCollector.assertThat("Subscription Plans object is null", subscriptionPlans, is(notNullValue()),
            assertionErrorList);
        final String subscriptionPlanId4 = subscriptionPlans.getSubscriptionPlans().get(0).getId();

        // Query Dynamo DB for each Subscription Plan
        final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(subscriptionPlanId4).setOldName(null).setNewName(subscriptionPlanName4)
            .setOldExternalKey(null).setNewExternalKey(externalKey).setOldOfferingType(null)
            .setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null).setNewStatus(Status.ACTIVE)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD)
            .setOldUsageType(null).setNewUsageType(UsageType.COM).setOldOfferingDetailId(null)
            .setNewOfferingDetailId(offeringDetailId).setOldProductLine(null).setNewProductLine(productLineId)
            .setOldSupportLevel(null).setNewSupportLevel(SupportLevel.ADVANCED).setAction(Action.CREATE)
            .setFileName(FILE_NAME).setOldPackagingValue(null).setNewPackagingValue(null)
            .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        createSubscriptionPlanLogFound1 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId4,
            createSubscriptionPlanLogFound1, assertionErrorList);

        // Query Audit Log Report for each subscription plan1
        HashMap<String, List<String>> descriptionPropertyValues4 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId4, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // External Key
        final List<String> externalKeyValues4 = descriptionPropertyValues4.get(PelicanConstants.EXTERNAL_KEY_FIELD);
        AssertCollector.assertThat("Invalid old id value in audit log report", externalKeyValues4.get(0), nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid new id value in audit log report", externalKeyValues4.get(1),
            equalTo(externalKey), assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);

        descriptionPropertyValues4 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId4, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);

        AssertCollector.assertTrue("Last feature composition value not found in audit log report",
            descriptionPropertyValues4.containsKey(LAST_FEATURE_COMPOSITION_TIME), assertionErrorList);

        final String idFromSubscriptionEntitlementTable1 =
            DbUtils.getSubscriptionEntitlementId(subscriptionPlanId4, getEnvironmentVariables());

        // Verify audit log data for subscription entitlement table
        final boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateFeatureEntitlementInDynamoDB(null,
            subscriptionPlanId4, null, entitlementId, null, idFromSubscriptionEntitlementTable1, null,
            DbUtils.getLicensingModelId(getEnvironmentVariables().getAppFamilyId(),
                PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, getEnvironmentVariables()),
            null, null, Action.CREATE, assertionErrorList);
        AssertCollector.assertTrue(
            "Audit log not found for subscription entitlement table for offering id " + subscriptionPlanId4,
            isAuditLogFound, assertionErrorList);

        // Audit log report page for subscription entitlement
        descriptionPropertyValues4 = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId4, idFromSubscriptionEntitlementTable1,
            adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues4.containsKey(FILE_NAME), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests Audit log for Subscription Plan creation with entitlements, offers and prices through Upload
     * This verifies Multi fields and multi plans Audit log
     */
    @Test
    public void testAuditLogOfCreateSubscriptionPlansWithEntlmntsOffersAndPricesThroughUpload() throws IOException {

        // create unique subscription plan external keys
        final String externalKey11 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        final String externalKey12 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        // add features and get feature ids and feature external keys
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        entitlementId = item.getId();
        entlmntExternalKey = item.getExternalKey();

        // Create a file and write to the file
        createXlsxAndWriteData(FILE_NAME, null, externalKey11,
            new SubscriptionOfferData(null, 1, BillingFrequency.MONTH), OfferingType.META_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, entlmntExternalKey,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, true, null, null, false, null, null, false, true,
            PackagingType.NULL, null);

        createXlsxAndWriteData(FILE_NAME, null, externalKey12,
            new SubscriptionOfferData(null, 1, BillingFrequency.MONTH), OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.IMMEDIATE_NO_REFUND, UsageType.TRL.getUploadName(), OFFERING_DETAILS1, productLineName,
            SupportLevel.BASIC, entlmntExternalKey, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null,
            null, true, null, null, false, true, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey11);
        final String subscriptionPlanId11 = subscriptionPlanDetailPage.getId();
        final String subscriptionPlanName11 = subscriptionPlanDetailPage.getName();
        final String priceId = subscriptionPlanDetailPage.getPriceId();
        final String idFromSubscriptionEntitlementTable1 =
            DbUtils.getSubscriptionEntitlementId(subscriptionPlanId11, getEnvironmentVariables());

        // Find the id of the Subscription Plan based on external key
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey12);
        final String subscriptionPlanId12 = subscriptionPlanDetailPage.getId();
        final String subscriptionPlanName12 = subscriptionPlanDetailPage.getName();
        final String idFromSubscriptionEntitlementTable2 =
            DbUtils.getSubscriptionEntitlementId(subscriptionPlanId12, getEnvironmentVariables());

        // Query Dynamo DB for each Subscription Plan
        SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(subscriptionPlanId11).setOldName(null).setNewName(subscriptionPlanName11)
            .setOldExternalKey(null).setNewExternalKey(externalKey11).setOldOfferingType(null)
            .setNewOfferingType(OfferingType.META_SUBSCRIPTION).setOldStatus(null).setNewStatus(Status.ACTIVE)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD)
            .setOldUsageType(null).setNewUsageType(UsageType.COM).setOldOfferingDetailId(null)
            .setNewOfferingDetailId(offeringDetailId).setOldProductLine(null).setNewProductLine(productLineId)
            .setOldSupportLevel(null).setNewSupportLevel(SupportLevel.ADVANCED).setAction(Action.CREATE)
            .setFileName(FILE_NAME).setOldPackagingValue(null).setNewPackagingValue(null)
            .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        createSubscriptionPlanLogFound1 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(subscriptionPlanId12)
            .setOldName(null).setNewName(subscriptionPlanName12).setOldExternalKey(null)
            .setNewExternalKey(externalKey12).setOldOfferingType(null).setNewOfferingType(OfferingType.BIC_SUBSCRIPTION)
            .setOldStatus(null).setNewStatus(Status.NEW).setOldCancellationPolicy(null)
            .setNewCancellationPolicy(CancellationPolicy.IMMEDIATE_NO_REFUND).setOldUsageType(null)
            .setNewUsageType(UsageType.TRL).setOldOfferingDetailId(null).setNewOfferingDetailId(offeringDetailId)
            .setOldProductLine(null).setNewProductLine(productLineId).setOldSupportLevel(null)
            .setNewSupportLevel(SupportLevel.BASIC).setAction(Action.CREATE).setFileName(FILE_NAME)
            .setOldPackagingValue(null).setNewPackagingValue(null).setOldExpReminderEmailEnabled(null)
            .setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        createSubscriptionPlanLogFound2 =
            SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId11,
            createSubscriptionPlanLogFound1, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Subscription Plan Audit Log not found for Subscription Plan id : " + subscriptionPlanId12,
            createSubscriptionPlanLogFound2, assertionErrorList);

        // Verify audit log data for subscription entitlement table
        boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateFeatureEntitlementInDynamoDB(null,
            subscriptionPlanId11, null, entitlementId, null, idFromSubscriptionEntitlementTable1, null,
            DbUtils.getLicensingModelId(getEnvironmentVariables().getAppFamilyId(),
                PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, getEnvironmentVariables()),
            null, null, Action.CREATE, assertionErrorList);
        AssertCollector.assertTrue(
            "Audit log not found for subscription entitlement table for offering id " + subscriptionPlanId11,
            isAuditLogFound, assertionErrorList);

        // Audit log report page for subscription entitlement
        final HashMap<String, List<String>> descriptionPropertyValues1 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                subscriptionPlanId11, idFromSubscriptionEntitlementTable1, adminToolUserId, Action.CREATE.toString(),
                null, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues1.containsKey(FILE_NAME), assertionErrorList);

        isAuditLogFound = SubscriptionPlanAuditLogHelper.validateFeatureEntitlementInDynamoDB(null,
            subscriptionPlanId12, null, entitlementId, null, idFromSubscriptionEntitlementTable2, null,
            DbUtils.getLicensingModelId(getEnvironmentVariables().getAppFamilyId(),
                PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, getEnvironmentVariables()),
            null, null, Action.CREATE, assertionErrorList);
        AssertCollector.assertTrue(
            "Audit log not found for subscription entitlement table for offering id " + subscriptionPlanId12,
            isAuditLogFound, assertionErrorList);

        // Audit log report page for subscription entitlement
        final HashMap<String, List<String>> descriptionPropertyValues2 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                subscriptionPlanId12, idFromSubscriptionEntitlementTable2, adminToolUserId, Action.CREATE.toString(),
                null, assertionErrorList);

        // File Name
        AssertCollector.assertTrue("File Name value not found in audit log report",
            descriptionPropertyValues2.containsKey(FILE_NAME), assertionErrorList);

        // Verify audit log data for subscription offer
        SubscriptionPlan subscriptionPlan = resource.subscriptionPlan().getById(subscriptionPlanId11, null);
        SubscriptionOffer subscriptionOffer = subscriptionPlan.getSubscriptionOffers().getSubscriptionOffers().get(0);
        final String subscriptionOfferId1 = subscriptionOffer.getId();
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(subscriptionOfferId1, null, subscriptionPlanId11, null,
            subscriptionOffer.getName(), null, subscriptionOffer.getExternalKey(), null, BILLING_PERIOD_ORIG_VALUE,
            null, subscriptionOffer.getStatus(), Action.CREATE, assertionErrorList);
        subscriptionPlan = resource.subscriptionPlan().getById(subscriptionPlanId12, null);
        subscriptionOffer = subscriptionPlan.getSubscriptionOffers().getSubscriptionOffers().get(0);
        final String subscriptionOfferId2 = subscriptionOffer.getId();
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(subscriptionOfferId2, null, subscriptionPlanId12, null,
            subscriptionOffer.getName(), null, subscriptionOffer.getExternalKey(), null, BILLING_PERIOD_ORIG_VALUE,
            null, subscriptionOffer.getStatus(), Action.CREATE, assertionErrorList);

        // Verify audit log data for subscription price
        SubscriptionPlanAuditLogHelper.validateSubscriptionPrice(subscriptionPlanId11, subscriptionOfferId1, priceId,
            null, String.valueOf(Double.valueOf(ORIG_PRICE).intValue()), null, Currency.USD, null,
            DateTimeUtils.formatDate(effectiveStartDate, PelicanConstants.AUDIT_LOG_DATE_FORMAT), null,
            DateTimeUtils.formatDate(effectiveEndDate, PelicanConstants.AUDIT_LOG_DATE_FORMAT), Action.CREATE,
            assertionErrorList);

        // Verify audit log data for subscription price
        final HashMap<String, List<String>> descriptionPropertyValues3 =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                subscriptionPlanId11, priceId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // AMOUNT
        final List<String> amountValues3 =
            descriptionPropertyValues3.get(StringUtils.capitalize(PelicanConstants.AMOUNT));
        AssertCollector.assertThat("Invalid old amount value in audit log report", amountValues3.get(0), nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid new amount value in audit log report", amountValues3.get(1),
            equalTo(String.valueOf(Double.valueOf(ORIG_PRICE).intValue())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests create Subscription Plan creation with currency one time entitlements
     */
    @Test
    public void testCreateSubscriptionPlansWithCurrencyEntitlementsOffersAndPricesThroughUpload() throws IOException {

        // create unique subscription plan external keys
        externalKey4 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        externalKey5 = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        subsPlanName1 = PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        subsPlanName2 = PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        // add features and get feature ids and feature external keys
        final Item item = featureApiUtils.addFeature(null, null, null);
        entitlementId = item.getId();
        entlmntExternalKey = item.getExternalKey();

        // Create a file and write to the file for Meta Subscription with Currency Entitlement as 100 CLOUD
        createXlsxAndWriteData(FILE_NAME, subsPlanName1, externalKey4, subscriptionOfferData,
            OfferingType.META_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            entlmntExternalKey, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, true, null, null, false, null,
            null, false, true, PelicanConstants.CLDCR, "100", PackagingType.NULL, null);

        // Create a file and write to the file for BIC Subscription with Currency Entitlement as 100 CLOUD
        createXlsxAndWriteData(FILE_NAME, subsPlanName2, externalKey5, subscriptionOfferData,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.IMMEDIATE_NO_REFUND,
            UsageType.TRL.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.BASIC, entlmntExternalKey,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, true, null, null, false, true,
            PelicanConstants.CLDCR, "150", PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey4);

        AssertCollector.assertTrue("Currency Entitlement not found for externalKey4",
            isOneTimeEntitlementFound("CURRENCY 100 CLOUD"), assertionErrorList);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey5);
        AssertCollector.assertTrue("Currency Entitlement not found for externalKey5",
            isOneTimeEntitlementFound("CURRENCY 150 CLOUD"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests update Subscription Plan created above with currency one time entitlements
     */
    @Test(dependsOnMethods = "testCreateSubscriptionPlansWithCurrencyEntitlementsOffersAndPricesThroughUpload")
    public void testUpdateSubscriptionPlansWithCurrencyEntitlementsOffersAndPricesThroughUpload() throws IOException {
        final String fileName = "UploadSubscriptionPlanWithCurrencyEntitlements.xlsx";
        // Update the subscription plan
        // Update the file for Meta Subscription with Currency Entitlement as 125 CLOUD
        createXlsxAndWriteData(fileName, subsPlanName1, externalKey4, subscriptionOfferData,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            entlmntExternalKey, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, true, null, null, false, null,
            null, false, true, PelicanConstants.CLDCR, "125", PackagingType.NULL, null);

        // Update the file for BIC Subscription with Currency Entitlement as 175 CLOUD
        createXlsxAndWriteData(fileName, subsPlanName2, externalKey5, subscriptionOfferData,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.IMMEDIATE_NO_REFUND,
            UsageType.TRL.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.BASIC, entlmntExternalKey,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, true, null, null, false, true,
            PelicanConstants.CLDCR, "175", PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, fileName);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey4);

        AssertCollector.assertTrue("Currency Entitlement not found for externalKey4",
            isOneTimeEntitlementFound("CURRENCY 125 CLOUD"), assertionErrorList);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(externalKey5);
        AssertCollector.assertTrue("Currency Entitlement not found for externalKey5",
            isOneTimeEntitlementFound("CURRENCY 175 CLOUD"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to check the given OneTimeEntitlement is Found in the Find Subscription Plan Page
     *
     * @param message required
     * @return true if found
     */
    private boolean isOneTimeEntitlementFound(final String message) {
        for (final WebElement oneTimeEntitlement : findSubscriptionPlanPage.getOneTimeEntitlements()) {
            if (oneTimeEntitlement.getText().contains(message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This test case tries to create Subscription Plan creation with currency one time entitlement with currency value
     * as 0
     */
    @Test
    public void testCreateSubscriptionPlansWithCurrencyEntitlementsAmountIsZero() throws IOException {

        // create unique subscription plan external keys
        final String externalKey = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        subsPlanName1 = PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        // add features and get feature ids and feature external keys
        final Item item = featureApiUtils.addFeature(null, null, null);
        entitlementId = item.getId();
        entlmntExternalKey = item.getExternalKey();

        // Create a file and write to the file for Meta Subscription with Currency Entitlement as 100 CLOUD
        createXlsxAndWriteData(FILE_NAME, subsPlanName1, externalKey, subscriptionOfferData,
            OfferingType.META_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            entlmntExternalKey, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, true, null, null, false, null,
            null, false, true, PelicanConstants.CLDCR, "0", PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        // Validate Error message
        AssertCollector.assertTrue("CurrencyAmount must be great than 0 error message not found",
            uploadSubscriptionPlanPage.getViewErrorsLink().size() > 0, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tries to create Subscription Plan creation with currency one time entitlement with currency value
     * as 0
     */
    @Test
    public void testCreateSubscriptionPlansWithCurrencyEntitlementsInvalidSKU() throws IOException {

        // create unique subscription plan external keys
        final String externalKey = PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        subsPlanName1 = PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        // add features and get feature ids and feature external keys
        final Item item = featureApiUtils.addFeature(null, null, null);
        entitlementId = item.getId();
        entlmntExternalKey = item.getExternalKey();

        // Create a file and write to the file for Meta Subscription with Currency Entitlement as 100 CLOUD
        createXlsxAndWriteData(FILE_NAME, subsPlanName1, externalKey, subscriptionOfferData,
            OfferingType.META_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            entlmntExternalKey, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, true, null, null, false, null,
            null, false, true, "INVALID-SKU", "100", PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        // Validate Error message
        AssertCollector.assertTrue("Could not find a currency with currencySku=INVALID-SKU",
            uploadSubscriptionPlanPage.getViewErrorsLink().size() > 0, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies that the subscription plan external key is auto generated, if left blank in upload file.
     */
    @Test
    public void testSubscriptionPlanExternalKeyAutoGeneration() throws IOException {

        // create random subscription plan name
        subscriptionPlanNameForAutoGenerate = SUBSCRIPTION_PLAN_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        // Create a file and write to the file
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanNameForAutoGenerate, null, null,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, false,
            null, null, false, null, null, false, true, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        final List<String> subscriptionPlanExternalKeyList =
            DbUtils.selectQuery("Select EXTERNAL_KEY from offering where name = '" + subscriptionPlanNameForAutoGenerate
                + "' order by id desc", "EXTERNAL_KEY", getEnvironmentVariables());
        if (subscriptionPlanExternalKeyList.size() > 0) {
            autoGeneratedSubscriptionPlanExternalKey = Iterables.getLast(subscriptionPlanExternalKeyList);

            AssertCollector.assertThat("External key length is not correct",
                autoGeneratedSubscriptionPlanExternalKey.length(), equalTo(15), assertionErrorList);
            AssertCollector.assertThat(
                "External key name should start with "
                    + PelicanConstants.AUTO_GENERATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_PREFIX,
                autoGeneratedSubscriptionPlanExternalKey.substring(0, 3),
                equalTo(PelicanConstants.AUTO_GENERATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_PREFIX), assertionErrorList);
        } else {
            Assert.fail("Subscription plan not found in database");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies that subscription offer external key is auto generated if left blank in upload file.
     */
    @Test(dependsOnMethods = "testSubscriptionPlanExternalKeyAutoGeneration")
    public void testSubscriptionOfferExternalKeyAutoGeneration() throws IOException {

        // Create a file and write to the file
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanNameForAutoGenerate, autoGeneratedSubscriptionPlanExternalKey,
            new SubscriptionOfferData(null, 1, BillingFrequency.MONTH), OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null, true, true,
            PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(autoGeneratedSubscriptionPlanExternalKey);

        final int offerCount = subscriptionPlanDetailPage.getOfferCount();
        autoGeneratedOfferExternalKey = subscriptionPlanDetailPage.getOfferExternalKey(offerCount);
        LOGGER.info(
            "offerExternalKeyForAutoGenerate IN offerExternalKeyForAutoGenerate " + autoGeneratedOfferExternalKey);

        AssertCollector.assertThat("Offer external key length is not correct", autoGeneratedOfferExternalKey.length(),
            greaterThanOrEqualTo(10), assertionErrorList);
        AssertCollector.assertThat(
            "Offer external key name should start with " + PelicanConstants.AUTO_GENERATE_OFFER_EXTERNAL_KEY_PREFIX,
            autoGeneratedOfferExternalKey.substring(0, 2),
            equalTo(PelicanConstants.AUTO_GENERATE_OFFER_EXTERNAL_KEY_PREFIX), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * this test case verifies that subscription offer external key is updated with value provided in file.
     */
    @Test(dependsOnMethods = "testSubscriptionOfferExternalKeyAutoGeneration")
    public void testSubscriptionOfferExternalKeyUpdate() throws IOException {
        final String newOfferExternalKey =
            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        // Create file with to update offer external key. subscriptionPlanNameForAutoGenerate is used
        // from previous method since we need to
        // have existing subscription plan key to add/update offer
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanNameForAutoGenerate, autoGeneratedSubscriptionPlanExternalKey,
            new SubscriptionOfferData(autoGeneratedOfferExternalKey, 1, BillingFrequency.MONTH),
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null, null, false,
            null, null, false, newOfferExternalKey, null, false, true, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(autoGeneratedSubscriptionPlanExternalKey);

        final int offerCount = subscriptionPlanDetailPage.getOfferCount();
        final String offerExternalKeyActual = subscriptionPlanDetailPage.getOfferExternalKey(offerCount);

        AssertCollector.assertThat("Offer external key is not updated with the new value", offerExternalKeyActual,
            equalTo(newOfferExternalKey), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies that subscription offer name is auto generated based on product line, support level and
     * billing frequency.
     */
    @Test(dataProvider = "dataForSubscriptionOfferNameAutoGeneration",
        dependsOnMethods = "testSubscriptionPlanExternalKeyAutoGeneration")
    public void testSubscriptionOfferNameAutoGeneration(final OfferingType offeringType,
        final SupportLevel supportLevel, final Status status, final BillingFrequency billingFrequency,
        final int billingFrequencyCount, final String offerName) throws IOException {
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        // Create a file and write to the file
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, autoGeneratedSubscriptionPlanExternalKey,
            new SubscriptionOfferData(offerName, billingFrequencyCount, billingFrequency), offeringType, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM.getUploadName(), OFFERING_DETAILS1,
            productLineName, supportLevel, null, null, false, null, null, false, null, offerName, true, true,
            PackagingType.NULL, null);
        LOGGER.info("AutoGeneratedSubscriptionPlanExternalKey " + autoGeneratedSubscriptionPlanExternalKey);
        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(autoGeneratedSubscriptionPlanExternalKey);

        final int offerCount = subscriptionPlanDetailPage.getOfferCount();
        // get external key of last offer
        autoGeneratedOfferExternalKey = subscriptionPlanDetailPage.getOfferExternalKey(offerCount);
        final String actualOfferName = subscriptionPlanDetailPage.getOfferName(offerCount);
        String expectedOfferName = "";

        String billingFrequencyInOfferName;

        if (billingFrequencyCount == 1) {
            billingFrequencyInOfferName = billingFrequency.getName().toLowerCase();
        } else {
            billingFrequencyInOfferName = billingFrequency.getDisplayName().toLowerCase();
        }

        // If support level is advanced then offer name has word subscription after product line
        // If support level is basic then offer name has word basic after product line
        // if support level is none then offer name will be without support level
        if (offerName != null) {
            expectedOfferName = offerName;
            LOGGER.info("offer name in null " + expectedOfferName);
        } else {
            if (supportLevel == null) {
                expectedOfferName = productLineName + " " + billingFrequencyCount + " " + billingFrequencyInOfferName;
                LOGGER.info("offer name in null " + expectedOfferName);
            } else if (supportLevel == SupportLevel.ADVANCED) {
                expectedOfferName =
                    productLineName + " Subscription " + billingFrequencyCount + " " + billingFrequencyInOfferName;
                LOGGER.info("offer name in advanced " + expectedOfferName);

            } else if (supportLevel == SupportLevel.BASIC) {
                expectedOfferName = productLineName + " " + supportLevel.getDisplayName() + " " + billingFrequencyCount
                    + " " + billingFrequencyInOfferName;
                LOGGER.info("offer name in basic " + expectedOfferName);
            } else {
                LOGGER.info("support level not found");
            }
        }

        AssertCollector.assertThat("Auto generated offer name is not correct.", actualOfferName,
            equalToIgnoringCase(expectedOfferName), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies that subscription offer external key and name are not auto generated if values are
     * provided in upload file.
     */

    @Test(dependsOnMethods = "testSubscriptionPlanExternalKeyAutoGeneration")
    public void testCreateSubscriptionOfferWithProvidedNameAndExternalKey() throws IOException {
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        final String expectedOfferName = PelicanConstants.OFFER_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        final String expectedOffeExternalKey =
            PelicanConstants.OFFER_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);
        // Create file with to update offer external key. subscriptionPlanNameForAutoGenerate is used
        // from previous method since we need to
        // have existing subscription plan key to add/update offer
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, autoGeneratedSubscriptionPlanExternalKey,
            new SubscriptionOfferData(expectedOffeExternalKey, 2, BillingFrequency.MONTH),
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.BASIC, null, null, false,
            null, null, false, null, expectedOfferName, false, false, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(autoGeneratedSubscriptionPlanExternalKey);

        final int offerCount = subscriptionPlanDetailPage.getOfferCount();
        // get external key of last offer
        autoGeneratedOfferExternalKey = subscriptionPlanDetailPage.getOfferExternalKey(offerCount);
        final String actualOfferName = subscriptionPlanDetailPage.getOfferName(offerCount);
        final String actualOfferExternalKey = subscriptionPlanDetailPage.getOfferExternalKey(offerCount);

        AssertCollector.assertThat("Offer external key is not correct.", actualOfferExternalKey,
            equalToIgnoringCase(expectedOffeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Offer name is not correct.", actualOfferName,
            equalToIgnoringCase(expectedOfferName), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the test case to test the description in the view audit log report.
     *
     * @throws IOException
     */
    @Test
    public void testAuditLogDescription() throws IOException {

        final String extKey = "Test_" + RandomStringUtils.randomAlphabetic(10);
        createXlsxAndWriteData(FILE_NAME, null, extKey, null, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.NCM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null, false, true,
            PackagingType.NULL, null);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(extKey);
        final String planId = subscriptionPlanDetailPage.getId();
        // Query Audit Log Report for each subscription plan1
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, null,
            planId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);
        final List<String> descriptionList = auditLogReportResultPage.getValuesFromDescriptionColumn();
        AssertCollector.assertThat("Incorrect description for feature update entry", descriptionList.get(0),
            equalTo(PelicanConstants.DESCRIPTION_CHANGES_FOR_UPLOAD_PLAN), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the test case to test the description in the download audit log report.
     *
     * @throws IOException
     */
    @Test
    public void testDownloadAuditLogReportDescription() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        final String extKey = "Test_" + RandomStringUtils.randomAlphabetic(10);
        createXlsxAndWriteData(FILE_NAME, null, extKey, null, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.NCM.getUploadName(), OFFERING_DETAILS1,
            productLineName, SupportLevel.ADVANCED, null, null, false, null, null, false, null, null, false, true,
            PackagingType.NULL, null);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(extKey);
        final String planId = subscriptionPlanDetailPage.getId();
        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, planId,
            adminToolUserId, true);

        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String description = fileData[1][PelicanConstants.DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT];
        AssertCollector.assertThat("Incorrect description for feature update entry", description,
            equalTo(PelicanConstants.DESCRIPTION_CHANGES_FOR_UPLOAD_PLAN), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests create Subscription Plan creation with currency one time entitlements
     */
    @Test(dataProvider = "getPackagingTypes")
    public void testCreateUpdateSubPlanWithPackagingTypeValuesThroughUpload(final PackagingType packagingType,
        final PackagingType packagingTypeShortName) throws IOException {

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        // Create a file and write to the file for Meta Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false, null, null, false, true,
            null, null, packagingTypeShortName, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(subscriptionPlanExtKey);

        AssertCollector.assertThat("PackagingType is not " + packagingType.getDisplayName(),
            subscriptionPlanDetailPage.getPackagingType(), equalTo(packagingType.getDisplayName()), assertionErrorList);

        // Create a file and write to the file for Meta Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED, null,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false, null, null, false, true,
            null, null, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Find the id of the Subscription Plan based on external key
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanByExternalKey(subscriptionPlanExtKey);

        AssertCollector.assertThat("PackagingType is not empty", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(PackagingType.NONE.getDisplayName()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which can add a feature to a plan in new status
     *
     * @throws IOException
     */
    @Test
    public void testUploadPlanWithEntitlementInNewStatus() throws IOException {

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String itemName1 = RandomStringUtils.randomAlphanumeric(6);

        final Item item1 = itemUtils.addItem(itemName1, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName1);

        // Create a file and write to the file for Meta Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            item1.getExternalKey(), PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false,
            null, null, false, true, null, null, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        final String planId = DbUtils.getOfferingId(subscriptionPlanExtKey, getEnvironmentVariables());

        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);

        final String itemName2 = RandomStringUtils.randomAlphanumeric(6);

        final Item item2 = itemUtils.addItem(itemName2, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName2);

        // Create a file and write to the file for Meta Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            item2.getExternalKey(), PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false,
            null, null, false, true, null, null, PackagingType.NULL, null);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        AssertCollector.assertThat("Feature is not added to the plan",
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables()), is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testUploadPlanWithEntitlementFromNewToActiveStatus() throws IOException {

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String itemName1 = RandomStringUtils.randomAlphanumeric(6);

        final Item item1 = itemUtils.addItem(itemName1, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName1);

        // Create a file and write to the file for bic Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            item1.getExternalKey(), PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false,
            null, null, false, true, null, null, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        final String planId = DbUtils.getOfferingId(subscriptionPlanExtKey, getEnvironmentVariables());

        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);

        final String itemName2 = RandomStringUtils.randomAlphanumeric(6);

        final Item item2 = itemUtils.addItem(itemName2, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName2);

        eventsList.clear();

        // Create a file and write to the file for bic Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            item2.getExternalKey(), PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false,
            null, null, false, true, null, null, PackagingType.NULL, null);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        findSubscriptionPlanPage.findSubscriptionPlanById(planId);

        final boolean isNotificationFound =
            cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList, planId,
                PelicanConstants.UPDATED, getUser(), subscriptionPlanExtKey, true, item2, assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Subscription Offering : "
            + subscriptionPlanDetailPage.getId() + ", is Not Found in eventList", isNotificationFound,
            assertionErrorList);

        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Feature is not added to the plan",
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables()), is(notNullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testUploadPlanWithEntitlementInActiveStatus() throws IOException {

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String itemName1 = RandomStringUtils.randomAlphanumeric(6);

        final Item item1 = itemUtils.addItem(itemName1, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName1);

        // Create a file and write to the file for Meta Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            item1.getExternalKey(), PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false,
            null, null, false, true, null, null, PackagingType.NULL, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        final String planId = DbUtils.getOfferingId(subscriptionPlanExtKey, getEnvironmentVariables());

        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);

        final String itemName2 = RandomStringUtils.randomAlphanumeric(6);

        final Item item2 = itemUtils.addItem(itemName2, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName2);

        // Create a file and write to the file for Meta Subscription
        createXlsxAndWriteData(FILE_NAME, subscriptionPlanName, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM.getUploadName(), OFFERING_DETAILS1, productLineName, SupportLevel.ADVANCED,
            item2.getExternalKey(), PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY, false, null, null, false,
            null, null, false, true, null, null, PackagingType.NULL, null);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        // Validate Error message
        AssertCollector.assertTrue("Upload is not succesful", uploadSubscriptionPlanPage.getViewErrorsLink().size() > 0,
            assertionErrorList);
        AssertCollector.assertThat("Feature is added to the plan",
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables()), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will test the upload with no user input with feature flag false and true
     *
     * @param featureFlagValue
     * @throws IOException
     * @throws ParseException
     */
    @Test(dataProvider = "getFeatureFlagValues")
    public void testUploadPlanWithAssignableAndRemoveFeaturesWhenNoUserInput(final boolean featureFlagValue)
        throws IOException, ParseException {

        final ArrayList<String> planColumnHeaders = new ArrayList<>();
        final ArrayList<String> planColumnData = new ArrayList<>();

        final ArrayList<String> entitlementColumnHeaders = new ArrayList<>();
        final ArrayList<String> entitlementColumnData = new ArrayList<>();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, featureFlagValue);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, featureFlagValue);

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final Applications applications = resource.application().getApplications();
        String appId = "";
        String featureTypeId = null;

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        final List<Map<String, String>> resultMapList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ITEM_TYPE_ID,
                PelicanConstants.CSR_FEATURE_TYPE_EXTERNAL_KEY, appId), getEnvironmentVariables());

        if (resultMapList.size() == 0) {

            final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
            final String featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

            // Navigate to the add feature page and add a feature
            addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
                getEnvironmentVariables().getApplicationDescription(), featureTypeName,
                PelicanConstants.CSR_EXTERNAL_KEY);
            final FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
            featureTypeId = featureTypeDetailPage.getId();
        } else {
            featureTypeId = resultMapList.get(0).get("ID");
        }

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item1 = featureApiUtils.addFeature(null, null, featureTypeId);

        final String itemName2 = RandomStringUtils.randomAlphanumeric(6);
        final Item item2 = itemUtils.addItem(itemName2, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName2);

        planColumnHeaders
            .add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
                + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        planColumnData.add("SubscriptionPlan," + subscriptionPlanName + "," + subscriptionPlanExtKey + ","
            + OfferingType.BIC_SUBSCRIPTION + "," + Status.ACTIVE + ","
            + CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD + "," + OFFERING_DETAILS1 + ","
            + UsageType.COM.getUploadName() + "," + productLineName + "," + SupportLevel.ADVANCED + ","
            + PackagingType.NULL.getDisplayName() + "," + "");

        entitlementColumnHeaders.add(
            "#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount,assignable,eos,eol immediate,eol renewal");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,,,,");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item2.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,,false,,");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + ",,,,currency,"
            + PelicanConstants.CLDCR + "," + "150" + ",,,,");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        final String planId = DbUtils.getOfferingId(subscriptionPlanExtKey, getEnvironmentVariables());

        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);

        findSubscriptionPlanPage.findSubscriptionPlanById(planId);

        final String newEntitlementId1 =
            DbUtils.getEntitlementIdFromItemId(planId, item1.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId1, "true", "", "", "", true, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId1, null, null, null, null, true, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId2, "false", "", "", "", true, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId2, null, null, null, null, true, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);

        final String newEntitlementId3 = DbUtils.getCurrencyEntitlementId(planId, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId3);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId3, "false", "", "", "", false, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId3, null, null, null, null, false, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId3);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will test the upload with user input with feature flag false and true
     *
     * @param featureFlagValue
     * @throws IOException
     * @throws ParseException
     */
    @Test(dataProvider = "getFeatureFlagValues")
    public void testUploadPlanWithAssignableAndRemoveFeaturesWhenUserInput(final boolean featureFlagValue)
        throws IOException, ParseException {

        final ArrayList<String> planColumnHeaders = new ArrayList<>();
        final ArrayList<String> planColumnData = new ArrayList<>();

        final ArrayList<String> entitlementColumnHeaders = new ArrayList<>();
        final ArrayList<String> entitlementColumnData = new ArrayList<>();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, featureFlagValue);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, featureFlagValue);

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        planColumnHeaders
            .add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
                + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        planColumnData.add("SubscriptionPlan," + subscriptionPlanName + "," + subscriptionPlanExtKey + ","
            + OfferingType.BIC_SUBSCRIPTION + "," + Status.NEW + ","
            + CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD + "," + OFFERING_DETAILS1 + ","
            + UsageType.COM.getUploadName() + "," + productLineName + "," + SupportLevel.ADVANCED + ","
            + PackagingType.NULL.getDisplayName() + "," + "");

        entitlementColumnHeaders.add(
            "#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount,assignable,eos,eol immediate,eol renewal");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "true," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item2.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "false," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item3.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "false," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + ",,,,currency,"
            + PelicanConstants.CLDCR + "," + "150,," + eosDate + "," + eolImmediateDate + "," + eolRenewalDate + ",");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        final String planId = DbUtils.getOfferingId(subscriptionPlanExtKey, getEnvironmentVariables());

        AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()), assertionErrorList);

        findSubscriptionPlanPage.findSubscriptionPlanById(planId);

        final String newEntitlementId1 =
            DbUtils.getEntitlementIdFromItemId(planId, item1.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId1, "true", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId1, null, null, null, null, true, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId2, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId2, null, null, null, null, true, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);

        final String newEntitlementId3 =
            DbUtils.getEntitlementIdFromItemId(planId, item3.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId3);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId3, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId3, null, null, null, null, true, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId3);

        final String newEntitlementId4 = DbUtils.getCurrencyEntitlementId(planId, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId4);
        if (featureFlagValue) {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId4, "false", eosDate, eolRenewalDate, eolImmediateDate, false, assertionErrorList);
        } else {
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId4, null, null, null, null, false, assertionErrorList);
        }
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId4);

        // Query Audit Log Report for subscription plan
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, planId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        HashMap<String, List<String>> subscriptionEntitlementDescriptionValues =
            auditLogReportHelper.getAuditDescription(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT, planId, newEntitlementId1, Action.CREATE.toString());
        subscriptionEntitlementDescriptionValues.putAll(descriptionPropertyValues);

        // Get Licensing Model Id
        final String licensingModelId =
            DbUtils
                .selectQuery(PelicanDbConstants.SQL_QUERY_ID_FROM_LICENSING_MODEL
                    + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + "'", "ID", getEnvironmentVariables())
                .get(0);

        if (!(featureFlagValue)) {
            SubscriptionAndBasicOfferingsAuditLogReportHelper
                .assertionsForFeatureEntitlementInAuditLogReportDescription(subscriptionEntitlementDescriptionValues,
                    null, item1.getId(), null,
                    PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + " (" + licensingModelId + ")", null, null,
                    null, "true", null, null, null, null, null, null, getEnvironmentVariables(), assertionErrorList);
        } else {
            SubscriptionAndBasicOfferingsAuditLogReportHelper
                .assertionsForFeatureEntitlementInAuditLogReportDescription(subscriptionEntitlementDescriptionValues,
                    null, item1.getId(), null,
                    PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + " (" + licensingModelId + ")", null, null,
                    null, "true", null, eosDate, null, eolImmediateDate, null, eolRenewalDate,
                    getEnvironmentVariables(), assertionErrorList);
        }

        uploadUtils.deleteFilefromLocal(FILE_NAME);

        entitlementColumnData.clear();

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "false," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item2.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "false," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item3.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "false," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + ",,,,currency,"
            + PelicanConstants.CLDCR + "," + "150,," + eosDate + "," + eolImmediateDate + "," + eolRenewalDate + ",");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        subscriptionEntitlementDescriptionValues =
            auditLogReportHelper.getAuditDescription(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT, planId, newEntitlementId1, Action.UPDATE.toString());

        if (featureFlagValue) {
            // Assignable
            final List<String> assginableValues =
                subscriptionEntitlementDescriptionValues.get(PelicanConstants.AUDIT_ASSIGNABLE_COLUMN_NAME);
            AssertCollector.assertThat("Invalid old Assignable value in audit log report", assginableValues.get(0),
                equalTo("true"), assertionErrorList);
            AssertCollector.assertThat("Invalid new Assignable value in audit log report", assginableValues.get(1),
                equalTo("false"), assertionErrorList);
        } else {
            AssertCollector.assertThat("Assignable field is updated when Feature flag is false ",
                subscriptionEntitlementDescriptionValues, equalTo(null), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will test the upload with user input with feature flag false and true
     *
     * @param featureFlagValue
     * @throws IOException
     * @throws ParseException
     */
    @Test(dataProvider = "getPlanStatusAndFeatureFlag")
    public void testUploadPlanWithMandatoryAssignableValue(final Status status, final boolean featureFlagValue)
        throws IOException, ParseException {

        final ArrayList<String> planColumnHeaders = new ArrayList<>();
        final ArrayList<String> planColumnData = new ArrayList<>();

        final ArrayList<String> entitlementColumnHeaders = new ArrayList<>();
        final ArrayList<String> entitlementColumnData = new ArrayList<>();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, featureFlagValue);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, featureFlagValue);

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        planColumnHeaders
            .add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
                + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        planColumnData.add("SubscriptionPlan," + subscriptionPlanName + "," + subscriptionPlanExtKey + ","
            + OfferingType.BIC_SUBSCRIPTION + "," + status + "," + CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD
            + "," + OFFERING_DETAILS1 + "," + UsageType.COM.getUploadName() + "," + productLineName + ","
            + SupportLevel.ADVANCED + "," + PackagingType.NULL.getDisplayName() + "," + "");

        entitlementColumnHeaders.add(
            "#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount,assignable,eos,eol immediate,eol renewal");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "true," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        if (status == Status.NEW) {
            final String planId = DbUtils.getOfferingId(subscriptionPlanExtKey, getEnvironmentVariables());
            AssertCollector.assertThat("Subscription plan is not found", planId, is(notNullValue()),
                assertionErrorList);
            findSubscriptionPlanPage.findSubscriptionPlanById(planId);

            final String newEntitlementId1 =
                DbUtils.getEntitlementIdFromItemId(planId, item1.getId(), getEnvironmentVariables());
            subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
            if (featureFlagValue) {
                AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                    newEntitlementId1, "true", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
            } else {
                AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                    newEntitlementId1, null, null, null, null, true, assertionErrorList);
            }
        } else if (status == Status.ACTIVE) {

            // Validate Error message
            AssertCollector.assertThat("Plan with mandatory Assinable value check, failed",
                uploadSubscriptionPlanPage.getUploadError(uploadSubscriptionPlanPage.getUploadJobId()),
                containsString(PelicanErrorConstants.SUB_PLAN_UPLOAD_ASSINABLE_ERROR), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the plan creation with past eos date
     *
     * @throws IOException
     */
    @Test
    public void testUploadPlanWithRemoveFeaturesDatesInPast() throws IOException {

        final ArrayList<String> planColumnHeaders = new ArrayList<>();
        final ArrayList<String> planColumnData = new ArrayList<>();

        final ArrayList<String> entitlementColumnHeaders = new ArrayList<>();
        final ArrayList<String> entitlementColumnData = new ArrayList<>();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final String eosDate =
            new DateTime().minusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        planColumnHeaders
            .add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
                + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        planColumnData.add("SubscriptionPlan," + subscriptionPlanName + "," + subscriptionPlanExtKey + ","
            + OfferingType.BIC_SUBSCRIPTION + "," + Status.ACTIVE + ","
            + CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD + "," + OFFERING_DETAILS1 + ","
            + UsageType.COM.getUploadName() + "," + productLineName + "," + SupportLevel.ADVANCED + ","
            + PackagingType.NULL.getDisplayName() + "," + "");

        entitlementColumnHeaders.add(
            "#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount,assignable,eos,eol immediate,eol renewal");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "true," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        // Validate Error message
        AssertCollector.assertThat("Plan with mandatory Assinable value check, failed",
            uploadSubscriptionPlanPage.getUploadError(uploadSubscriptionPlanPage.getUploadJobId()),
            containsString(PelicanErrorConstants.SUB_PLAN_UPLOAD_PAST_EOS_DATE_ERROR), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the plan creation through dates out of order
     *
     * @throws IOException
     */
    @Test
    public void testUploadPlanWithRemoveFeaturesDatesOutOfOrder() throws IOException {

        final ArrayList<String> planColumnHeaders = new ArrayList<>();
        final ArrayList<String> planColumnData = new ArrayList<>();

        final ArrayList<String> entitlementColumnHeaders = new ArrayList<>();
        final ArrayList<String> entitlementColumnData = new ArrayList<>();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final String eosDate =
            new DateTime().plusDays(4).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        final String eolRenewalDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        planColumnHeaders
            .add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
                + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        planColumnData.add("SubscriptionPlan," + subscriptionPlanName + "," + subscriptionPlanExtKey + ","
            + OfferingType.BIC_SUBSCRIPTION + "," + Status.ACTIVE + ","
            + CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD + "," + OFFERING_DETAILS1 + ","
            + UsageType.COM.getUploadName() + "," + productLineName + "," + SupportLevel.ADVANCED + ","
            + PackagingType.NULL.getDisplayName() + "," + "");

        entitlementColumnHeaders.add(
            "#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount,assignable,eos,eol immediate,eol renewal");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "true," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        // Validate Error message
        AssertCollector.assertThat("Plan with mandatory Assinable value check, failed",
            uploadSubscriptionPlanPage.getUploadError(uploadSubscriptionPlanPage.getUploadJobId()),
            containsString(PelicanErrorConstants.SUB_PLAN_UPLOAD_DATES_OUT_OF_ORDER), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will test the upload with invalid user input with feature flag true
     *
     * @param featureFlagValue
     * @throws IOException
     */
    @Test
    public void testUploadPlanWithAssignableAndRemoveFeaturesWithInvalidUserInput() throws IOException {

        final ArrayList<String> planColumnHeaders = new ArrayList<>();
        final ArrayList<String> planColumnData = new ArrayList<>();

        final ArrayList<String> entitlementColumnHeaders = new ArrayList<>();
        final ArrayList<String> entitlementColumnData = new ArrayList<>();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        // create unique subscription plan external keys
        final String subscriptionPlanName =
            PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        final String subscriptionPlanExtKey =
            PelicanConstants.SUB_PLAN_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5);

        planColumnHeaders
            .add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
                + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        planColumnData.add("SubscriptionPlan," + subscriptionPlanName + "," + subscriptionPlanExtKey + ","
            + OfferingType.BIC_SUBSCRIPTION + "," + Status.ACTIVE + ","
            + CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD + "," + OFFERING_DETAILS1 + ","
            + UsageType.COM.getUploadName() + "," + productLineName + "," + SupportLevel.ADVANCED + ","
            + PackagingType.NULL.getDisplayName() + "," + "");

        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        entitlementColumnHeaders.add(
            "#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount,assignable,eos,eol immediate,eol renewal");

        entitlementColumnData.add("SubscriptionEntitlement," + subscriptionPlanExtKey + "," + item1.getExternalKey()
            + "," + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + ",,,,," + "true," + eosDate + ","
            + eolImmediateDate + "," + eolRenewalDate + ",");

        entitlementColumnData
            .add("SubscriptionEntitlement," + subscriptionPlanExtKey + ",,,,currency," + PelicanConstants.CLDCR + ","
                + "150," + "true," + eosDate + "," + eolImmediateDate + "," + eolRenewalDate + ",");

        // Create a file and write to the file for BIC Subscription
        createXlsxAndWriteData(planColumnHeaders, planColumnData, entitlementColumnHeaders, entitlementColumnData, null,
            null, null, null);

        // upload the Xlsx
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        uploadUtils.uploadSubscriptionPlan(adminToolPage, FILE_NAME);

        // Refresh page
        uploadSubscriptionPlanPage.refreshPage();

        // Validate Error message
        AssertCollector.assertTrue("Upload is  succesful", uploadSubscriptionPlanPage.getViewErrorsLink().size() > 0,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to assert the job entity
     */
    private void assertEntityType(final GenericGrid result) {
        // Validate job entity as 'Subscription Plan'
        final List<String> actualEntityType = result.getColumnValues("Entity Type");
        for (final String entityType : actualEntityType) {
            AssertCollector.assertThat("AdminTool: Found entity type other than <b>SUBSCRIPTION_PLAN</b>", entityType,
                equalTo("SUBSCRIPTION_PLAN"), assertionErrorList);
        }
    }

    /**
     * Method to create and write xls with Subscription Plans ,Offers and Offer price headers and data with out currency
     * one time entitlement
     *
     * @param FILE_NAME required
     * @param name required
     * @param externalKey required
     * @param subscriptionOfferData required
     * @param offeringType required
     * @param planStatus required
     * @param cancellationPolicy required
     * @param usageType required
     * @param offeringDetail required
     * @param productLine required
     * @param supportLevel required
     * @param entitlementExtKey required
     * @param entitlementLicensingModel required
     * @param addPriceToOffer required
     * @param priceId required
     * @param price required
     * @param append required
     * @param newOfferExternalKey required
     * @param offerName required
     * @param autoGenerateOfferExternalKey required
     * @param autoGenerateOfferName required
     * @param packagingType required
     * @param sendExpirationReminderEmails required
     * @throws IOException
     */
    private void createXlsxAndWriteData(final String FILE_NAME, final String name, final String externalKey,
        final SubscriptionOfferData subscriptionOfferData, final OfferingType offeringType, final Status planStatus,
        final CancellationPolicy cancellationPolicy, final String usageType, final String offeringDetail,
        final String productLine, final SupportLevel supportLevel, final String entitlementExtKey,
        final String entitlementLicensingModel, final boolean addPriceToOffer, final String priceId, final String price,
        final boolean append, final String newOfferExternalKey, final String offerName,
        final boolean autoGenerateOfferExternalKey, final boolean autoGenerateOfferName,
        final PackagingType packagingType, final String sendExpirationReminderEmails) throws IOException {
        createXlsxAndWriteData(FILE_NAME, name, externalKey, subscriptionOfferData, offeringType, planStatus,
            cancellationPolicy, usageType, offeringDetail, productLine, supportLevel, entitlementExtKey,
            entitlementLicensingModel, addPriceToOffer, priceId, price, append, newOfferExternalKey, offerName,
            autoGenerateOfferExternalKey, autoGenerateOfferName, null, null, packagingType,
            sendExpirationReminderEmails);
    }

    /**
     * Method to create and write xls with Subscription Plans ,Offers and Offer price headers and data.
     *
     * @param FILE_NAME required
     * @param name required
     * @param externalKey required
     * @param subscriptionOfferData required
     * @param offeringType required
     * @param planStatus required
     * @param cancellationPolicy required
     * @param usageType required
     * @param offeringDetail required
     * @param productLine required
     * @param supportLevel required
     * @param entitlementExtKey required
     * @param entitlementLicensingModel required
     * @param addPriceToOffer required
     * @param priceId required
     * @param price required
     * @param append required
     * @param newOfferExternalKey required
     * @param offerName required
     * @param autoGenerateOfferExternalKey required
     * @param autoGenerateOfferName required
     * @param currencyAmountEntitlement required
     * @throws IOException
     */
    private void createXlsxAndWriteData(final String FILE_NAME, String name, String externalKey,
        final SubscriptionOfferData subscriptionOfferData, final OfferingType offeringType, final Status planStatus,
        final CancellationPolicy cancellationPolicy, final String usageType, String offeringDetail, String productLine,
        final SupportLevel supportLevel, final String entitlementExtKey, String entitlementLicensingModel,
        final boolean addPriceToOffer, String priceId, final String price, final boolean append,
        String newOfferExternalKey, String offerName, final boolean autoGenerateOfferExternalKey,
        final boolean autoGenerateOfferName, final String currencySku, final String currencyAmountEntitlement,
        final PackagingType packagingType, final String sendExpirationReminderEmails) throws IOException {
        final XlsUtils utils = new XlsUtils();
        // Add subscription plan header values to list
        ArrayList<String> columnHeaders = new ArrayList<>();
        ArrayList<String> columnData = new ArrayList<>();
        String offerExtKey = null;
        String sendExpirationReminderEmailsFlag = sendExpirationReminderEmails;

        if (name == null) {
            name = "TestPlan_" + RandomStringUtils.randomAlphanumeric(5);
        }

        if (externalKey == null) {
            externalKey = "";
        }

        if (subscriptionOfferData != null) {
            if (subscriptionOfferData.getExternalKey() == null) {
                if (!autoGenerateOfferExternalKey) {
                    offerExtKey = "TestOfferKey_" + RandomStringUtils.randomAlphanumeric(5);
                } else {
                    offerExtKey = "";
                }
            } else {
                offerExtKey = subscriptionOfferData.getExternalKey();
            }
        }

        if (offeringDetail == null) {
            offeringDetail = "OfferingDetail_" + RandomStringUtils.randomAlphanumeric(3);
        }

        if (productLine == null) {
            productLine = "ProductLine_" + RandomStringUtils.randomAlphanumeric(3);
        }

        columnHeaders.add("#SubscriptionPlan,name,externalKey,offeringType,status,cancellationPolicy,offeringDetail,"
            + "usageType,productLine,supportLevel,packagingType,sendExpirationReminderEmails");

        if (null == sendExpirationReminderEmailsFlag) {
            sendExpirationReminderEmailsFlag = "";
        }

        // this condition is to cover None support level.
        if (supportLevel == null) {
            columnData.add("SubscriptionPlan," + name + "," + externalKey + "," + offeringType + "," + planStatus + ","
                + cancellationPolicy + "," + offeringDetail + "," + usageType + "," + productLine + ","
                + packagingType.getDisplayName() + "," + sendExpirationReminderEmailsFlag);
        } else {
            columnData.add("SubscriptionPlan," + name + "," + externalKey + "," + offeringType + "," + planStatus + ","
                + cancellationPolicy + "," + offeringDetail + "," + usageType + "," + productLine + "," + supportLevel
                + "," + packagingType.getDisplayName() + "," + sendExpirationReminderEmailsFlag);
        }
        // Write Subscription plan headers and data to excel.
        // modify the count to create multiple plans for load test
        utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, append);

        if (entitlementExtKey != null) {
            entitlementLicensingModel = entitlementLicensingModel != null ? entitlementLicensingModel : "";
            columnHeaders.clear();
            columnData.clear();
            columnHeaders
                .add("#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount");
            columnData.add("SubscriptionEntitlement," + externalKey + "," + entitlementExtKey + ","
                + entitlementLicensingModel + ",,,,");
            if (null != currencyAmountEntitlement) {
                final String sku = null != currencySku ? currencySku : PelicanConstants.CLDCR;
                columnData.add("SubscriptionEntitlement," + externalKey + ",,,,currency," + sku + ","
                    + currencyAmountEntitlement + ",");
            }
            // Write entitlement headers and data to excel.
            utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, true);
        }

        if (subscriptionOfferData != null) {
            // Add subscription Offer headers to list.
            columnHeaders = new ArrayList<>();
            columnData = new ArrayList<>();
            if (newOfferExternalKey != null) {
                columnHeaders.add("#SubscriptionOffer,plan,name,externalKey,status,billingCycleCount,"
                    + "billingFrequencyCount,billingFrequencyType,newExternalKey");
            } else {
                newOfferExternalKey = "";
                columnHeaders.add("#SubscriptionOffer,plan,name,externalKey,status,billingCycleCount,"
                    + "billingFrequencyCount,billingFrequencyType");
            }

            // Add Subscription Offer data to list.
            if (autoGenerateOfferName) {
                offerName = "";
            }

            final String billingFrequencyType = subscriptionOfferData.getBillingFrequency().getName();
            columnData.add("SubscriptionOffer," + externalKey + "," + offerName + "," + offerExtKey + ","
                + "ACTIVE,UNLIMITED," + String.valueOf(subscriptionOfferData.getBillingFrequencyCount()) + ","
                + billingFrequencyType + "," + newOfferExternalKey);

            // Write Subscription Offer headers and data to excel.
            utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, true);
        }

        if (addPriceToOffer) {
            // Add Subscription Offer price headers and data to list.
            columnHeaders = new ArrayList<>();
            columnData = new ArrayList<>();
            columnHeaders
                .add("#SubscriptionOfferPrice,offer,priceId,store,priceList,amount,currency,startDate,endDate");
            priceId = priceId != null ? priceId : "";
            final String effectivePrice = price != null ? price : ORIG_PRICE;
            columnData.add("SubscriptionOfferPrice," + offerExtKey + "," + priceId + "," + getStoreExternalKeyUs() + ","
                + getPricelistExternalKeyUs() + "," + effectivePrice + ",USD," + effectiveStartDate + ","
                + effectiveEndDate);
            // Write Subscription offer price headers and data to excel.
            utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, true);
        }
    }

    /**
     * This is a method to upload entitlements to the existing plan with only entitlement section
     *
     * @param FILE_NAME
     * @param externalKey
     * @param entitlementExtKey
     * @param entitlementLicensingModel
     * @param currencySku
     * @param currencyAmountEntitlement
     * @throws IOException
     */
    private void createXlsxAndWriteDataForEntitlements(final String FILE_NAME, final String externalKey,
        final String entitlementExtKey, final String entitlementLicensingModel, final String currencySku,
        final String currencyAmountEntitlement, final boolean append) throws IOException {
        final XlsUtils utils = new XlsUtils();
        // Add subscription plan header values to list
        final ArrayList<String> columnHeaders = new ArrayList<>();
        final ArrayList<String> columnData = new ArrayList<>();

        if (entitlementExtKey != null) {
            final String entitlementLicensingModelExtKey =
                entitlementLicensingModel != null ? entitlementLicensingModel : "";
            columnHeaders.clear();
            columnData.clear();
            columnHeaders
                .add("#SubscriptionEntitlement,plan,item,licensingModel,coreProducts,type,currencySku,currencyAmount");
            columnData.add("SubscriptionEntitlement," + externalKey + "," + entitlementExtKey + ","
                + entitlementLicensingModelExtKey + ",,,,");
            if (null != currencyAmountEntitlement) {
                final String sku = null != currencySku ? currencySku : PelicanConstants.CLDCR;
                columnData.add("SubscriptionEntitlement," + externalKey + ",,,,currency," + sku + ","
                    + currencyAmountEntitlement + ",");
            }
            // Write entitlement headers and data to excel.
            utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, append);
        }
    }

    /**
     * This is a method to create XLSX file with plan, entitlements, offer and price
     *
     * @param planColumnHeaders
     * @param planColumnData
     * @param entitlementColumnHeaders
     * @param entitlementColumnData
     * @param offerColumnHeaders
     * @param offerColumnData
     * @param priceColumnHeaders
     * @param priceColumnData
     * @throws IOException
     */
    private void createXlsxAndWriteData(final ArrayList<String> planColumnHeaders,
        final ArrayList<String> planColumnData, final ArrayList<String> entitlementColumnHeaders,
        final ArrayList<String> entitlementColumnData, final ArrayList<String> offerColumnHeaders,
        final ArrayList<String> offerColumnData, final ArrayList<String> priceColumnHeaders,
        final ArrayList<String> priceColumnData) throws IOException {
        final XlsUtils utils = new XlsUtils();

        if (planColumnHeaders != null && planColumnData != null) {
            utils.createAndWriteToXls(FILE_NAME, planColumnHeaders, planColumnData, false);
        }

        if (entitlementColumnHeaders != null && entitlementColumnData != null) {
            // Write entitlement headers and data to excel.
            utils.createAndWriteToXls(FILE_NAME, entitlementColumnHeaders, entitlementColumnData, true);
        }

        if (offerColumnHeaders != null && offerColumnData != null) {
            // Write Subscription Offer headers and data to excel.
            utils.createAndWriteToXls(FILE_NAME, offerColumnHeaders, offerColumnData, true);
        }

        if (priceColumnHeaders != null && priceColumnData != null) {
            utils.createAndWriteToXls(FILE_NAME, priceColumnHeaders, priceColumnData, true);
        }
    }

    /**
     * Data provider with different filters for testSubscriptionOfferNameAutoGeneration test
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForSubscriptionOfferNameAutoGeneration")
    public Object[][] getTestDataForSubscriptionOfferNameAutoGeneration() {
        return new Object[][] {
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.BASIC, Status.ACTIVE, BillingFrequency.DAY, 1, null },
                { OfferingType.BIC_SUBSCRIPTION, null, Status.ACTIVE, BillingFrequency.MONTH, 1, null },
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.BASIC, Status.ACTIVE, BillingFrequency.YEAR, 2, null },
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.ADVANCED, Status.ACTIVE, BillingFrequency.SEMIYEAR, 1,
                        null },
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.BASIC, Status.ACTIVE, BillingFrequency.QUARTER, 1,
                        null } };
    }

    /**
     * Provider to return different PackagintTypes
     *
     * @return PackagingType
     */
    @DataProvider(name = "getPackagingTypes")
    public Object[][] getPackagingTypes() {
        return new Object[][] { { PackagingType.INDUSTRY_COLLECTION, PackagingType.IC },
                { PackagingType.VERTICAL_GROUPING, PackagingType.VG } };
    }

    /**
     * Method to return the values of the feature flags
     *
     * @return true or false
     */
    @DataProvider(name = "getFeatureFlagValues")
    public Object[][] getFeatureFlagValues() {
        return new Object[][] { // { false },
                { true } };
    }

    /**
     * Method to return the plan status values along with feature flags
     *
     * @return New/Active , true or false
     */
    @DataProvider(name = "getPlanStatusAndFeatureFlag")
    public Object[][] getPlanStatusAndFeatureFlag() {
        return new Object[][] { { Status.NEW, true }, { Status.ACTIVE, true }, { Status.ACTIVE, false } };
    }
}
