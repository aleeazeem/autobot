package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionMigrationJobStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.Iterables;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a test class which will test the roll back of the migration job in the Admin tool.
 *
 * @author yerragv
 */
@Test(groups = { "excludedClass" })
public class RollbackMigrationJobTest extends SeleniumWebdriver {

    private UploadSubscriptionMigrationPage uploadSubscriptionMigrationPage;
    private String jobName;
    private AdminToolPage adminToolPage;
    private SubscriptionMigrationJobDetailPage subscriptionMigrationJobDetailPage;
    private String subscriptionOfferingExternalKey1;
    private String subscriptionOfferingExternalKey2;
    private String offerExternalKey1;
    private String offerExternalKey2;
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private RolesHelper rolesHelper;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private SubscriptionMigrationJobStatusPage subscriptionMigrationJobStatusPage;
    private WorkInProgressReportResultPage workInProgressReportResultPage;
    private FindSubscriptionsPage subscriptionPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private boolean isNonOfferingManagerUserLoggedIn;
    private SubscriptionOffering subscriptionOffering;
    private JProductLine productLine;
    private static final String FILE_NAME = "UploadSubscriptionMigration.xlsx";
    private static final String SELECT_QUERY =
        "select id,count(*) from subscription_migration_job where status=4 and app_family_id=2001";
    private static final String UPDATE_QUERY =
        "update subscription_migration_job set status= 1 where status =4 and APP_FAMILY_ID=2001";
    private static final String FIELD_NAME = "count(*)";
    private static final String ID_FIELD_NAME = "id";
    private static final String UPDATE_TABLE_NAME = "update subscription_migration_job set status= ";
    private static final String UPDATE_CONDITION = " where id= ";
    private static final String APP_FAMILY_ID_IN_QUERY = " and app_family_id = ";
    private static final String UPDATE_QUERY_TO_RUNNING_FILE_STATUS =
        "update subscription_migration_job set status=4 where id= ";
    private static final String UPDATE_QUERY_TO_COMPLETED_STATUS =
        "update subscription_migration_job set status=5 where id= ";
    private static final String UPDATE_RUN_DATE_OF_MIGRATION_JOB = "update subscription_migration_job set run_date = ";
    private static final String CONDITION_STRING = "where id= ";
    private static final int EXPECTED_COUNT_OF_RECORDS = 3;
    private static final String SUB_REVERT_MIGRATION = "SUB_MIGRATION_REVERTED";
    private static final String SUBSCRIPTION_MEMO = "Migration Job Id:";
    private static final String OLD_PRICE_ID = "Old Price Id:";
    private static final String NEW_PRICE_ID = "New Price Id:";
    private String updateQuery;
    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackMigrationJobTest.class.getSimpleName());

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        uploadSubscriptionMigrationPage = adminToolPage.getPage(UploadSubscriptionMigrationPage.class);
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        subscriptionMigrationJobStatusPage = adminToolPage.getPage(SubscriptionMigrationJobStatusPage.class);
        workInProgressReportResultPage = adminToolPage.getPage(WorkInProgressReportResultPage.class);
        subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        rolesHelper = new RolesHelper(getEnvironmentVariables());

        // create two bic offerings
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        offerExternalKey1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();

        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey2 = subscriptionOffering2.getOfferings().get(0).getExternalKey();
        offerExternalKey2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();

        // Submit purchase order to create subscriptions
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(subscriptionOffering1.getIncluded().getPrices().get(0).getId(), 1);

        purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);

        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
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
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

        final List<Map<String, String>> countOfRecordsList =
            DbUtils.selectQuery(SELECT_QUERY, getEnvironmentVariables());

        if (Integer.parseInt(countOfRecordsList.get(0).get(FIELD_NAME)) > 0) {
            LOGGER.info("The id(s) of jobs in running file state are: " + countOfRecordsList.get(0).get(ID_FIELD_NAME));
            LOGGER.info("The number of jobs in running state are: " + countOfRecordsList.get(0).get(FIELD_NAME));
            DbUtils.updateQuery(UPDATE_QUERY, getEnvironmentVariables());
        }
    }

    /**
     * Driver Close.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result - ITestResult would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
    }

    /**
     * This is a test case where we test that rollback button should be visible for the subscription migration jobs with
     * completed, partially completed and cancelled status
     *
     * @param status - job status.
     */
    @Test(dataProvider = "successfuljobstatuses")
    public void testRollBackButtonVisibilityForCompletedJobs(final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();
            if (subscriptionMigrationJobDetailPage.getStatus().equals(status.getDisplayName())) {
                AssertCollector.assertTrue(
                    "Roll Back button should be displayed for jobs with status " + status.getDisplayName(),
                    subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);
            } else {

                // Update the job status in db
                updateQuery = UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION
                    + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                    + getEnvironmentVariables().getAppFamilyId();
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

                // Assert on the roll back button
                AssertCollector.assertTrue(
                    "Roll Back button should be displayed for jobs with status " + status.getDisplayName(),
                    subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);

                // Set the job status to previous value
                updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.COMPLETED.getDbValue()
                    + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                    + getEnvironmentVariables().getAppFamilyId();
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
            }

        } else {

            Assert.fail("Subscription Upload Job Status is incorrect");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case where we test that rollback button should not be displayed for subscription migration jobs
     * whose status is not completed, partially completed and cancelled status
     *
     * @param status - job status.
     */
    @Test(dataProvider = "unsuccessfuljobstatuses")
    public void testRollBackButtonVisibilityForUnSuccesfulJobs(final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();

            // Update the job status in db
            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            subscriptionMigrationJobDetailPage.refreshPage();

            // Assert on the roll back button
            AssertCollector.assertFalse(
                "Roll Back button should not be displayed for jobs with status " + status.getDisplayName(),
                subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);

            // Set the job status to previous value
            updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.COMPLETED.getDbValue() + UPDATE_CONDITION
                + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        } else {

            Assert.fail("Subscription Upload Job Status is incorrect");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method which will test the roll back button functionality for successfully completed migrated jobs
     *
     */
    @Test(dataProvider = "successfuljobstatuses")
    public void testRollBackFunctionalityForSuccessfullyCompletedJobs(final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();
        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();

            // Update the job status in db
            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
            subscriptionMigrationJobDetailPage.refreshPage();

            AssertCollector.assertTrue(
                "Roll Back button should be displayed for jobs with status " + status.getDisplayName(),
                subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);

            final String oldMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String oldJobId = subscriptionMigrationJobDetailPage.getId();
            final String oldCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String oldFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String oldLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();

            subscriptionMigrationJobDetailPage.clickOnRollbackButton();

            final String newMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String newJobId = subscriptionMigrationJobDetailPage.getId();
            final String newCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String newFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String newLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();

            AssertCollector.assertThat("Incorrect roll back job name ", newMigrationJobName,
                equalTo("Rollback of " + oldMigrationJobName), assertionErrorList);
            AssertCollector.assertThat("Incorrect roll back job id ", newJobId, not(oldJobId), assertionErrorList);
            AssertCollector.assertThat("Incorrect roll back job created by ", newCreatedBy, equalTo(oldCreatedBy),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect roll back job fileupload id", newFileUploadId,
                equalTo(oldFileUploadId), assertionErrorList);
            AssertCollector.assertThat("Incorrect roll back job last modified by", newLastModifiedBy,
                equalTo(oldLastModifiedBy), assertionErrorList);
            AssertCollector.assertThat("Incorrect page header for roll back details",
                subscriptionMigrationJobDetailPage.getRollBackHeaderDetails(), equalTo("ROLLBACK JOB DETAILS"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect roll back parent job name",
                subscriptionMigrationJobDetailPage.getParentJob().split("\\(")[0], equalTo(oldMigrationJobName + " "),
                assertionErrorList);

            // click on the parent job name
            subscriptionMigrationJobDetailPage.clickOnParentJobName();
            AssertCollector.assertThat("Incorrect page header for parent roll back details",
                subscriptionMigrationJobDetailPage.getRollBackHeaderDetails(), equalTo("ROLLBACK JOB DETAILS"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect name for parent roll back job",
                subscriptionMigrationJobDetailPage.getRollBackJob().split("\\(")[0], equalTo(newMigrationJobName + " "),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect status for parent roll back job",
                subscriptionMigrationJobDetailPage.getRollBackJobStatus(), equalTo("ROLLBACK JOB CREATED"),
                assertionErrorList);

            // Set the job status to previous value
            updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.COMPLETED.getDbValue() + UPDATE_CONDITION
                + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        } else {
            Assert.fail("Incorrect upload job status found after uploading the subscription migration file");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test whether to determine only non offering manager is able to run the roll back job.
     *
     */
    @Test(dataProvider = "successfuljobstatuses")
    public void testNonOfferingManagerRunRollBackJob(final SubscriptionMigrationJobStatus status) {
        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            final String jobId = subscriptionMigrationJobDetailPage.getId();
            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();

            // Update the job status in db
            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            if (!isNonOfferingManagerUserLoggedIn) {
                adminToolPage.login();
                final HashMap<String, String> userParams = new HashMap<>();
                userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                    PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY);
                userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
                userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
                // Log in as a non-offering manager user
                final List<String> nonOfferingManagerRoleList = rolesHelper.getNonOfferingManagerRoleList();
                new UserUtils().createAssignRoleAndLoginUser(userParams, nonOfferingManagerRoleList, adminToolPage,
                    getEnvironmentVariables());
                isNonOfferingManagerUserLoggedIn = true;
            } else {
                Assert.fail("Non Offering Manager is already logged in to the admin tool");
            }

            subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, jobId, null, null, null);
            final int totalRecordsInReport = subscriptionMigrationJobStatusPage.getTotalItems();
            LOGGER.info("Total number of records in report: " + totalRecordsInReport);
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            if (totalRecordsInReport == 1) {
                subscriptionMigrationJobStatusPage.selectResultRow(totalRecordsInReport);
                Util.waitInSeconds(TimeConstants.THREE_SEC);

                AssertCollector.assertFalse("Non-Offering Manager is able to click on the roll back button",
                    subscriptionMigrationJobDetailPage.isRollBackButtonClickable(), assertionErrorList);

                // Set the job status to previous value
                updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.COMPLETED.getDbValue()
                    + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                    + getEnvironmentVariables().getAppFamilyId();
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            }
        } else {
            Assert.fail("Subscription Migration Upload Job Status is incorrect");
        }

        adminToolPage.logout();
        adminToolPage.login();
        isNonOfferingManagerUserLoggedIn = false;
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method which will test the error message on the rollback when another subscription migration job
     * is already running
     *
     */
    @Test
    public void testRunRollBackWhenAnotherJobIsRunning() {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();
            if (subscriptionMigrationJobDetailPage.getStatus()
                .equals(SubscriptionMigrationJobStatus.COMPLETED.getDisplayName())) {

                AssertCollector.assertTrue("Roll Back button should be displayed for completed jobs",
                    subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);

                final String jobId = subscriptionMigrationJobDetailPage.getId();
                updateQuery = UPDATE_QUERY_TO_RUNNING_FILE_STATUS + jobId;
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
                subscriptionMigrationJobDetailPage.clickOnRollbackButton();
                // Run the subscription migration rollback job
                subscriptionMigrationJobDetailPage.runAJobWithOutPageRefresh();
                Util.waitInSeconds(TimeConstants.THREE_SEC);
                AssertCollector.assertThat("Incorrect 'run by' of a rollback job",
                    subscriptionMigrationJobDetailPage.getRunBy(), equalTo(PelicanConstants.HIPHEN),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect 'run date' of a rollback job",
                    subscriptionMigrationJobDetailPage.getRunDate(), equalTo(PelicanConstants.HIPHEN),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect error message when running a rollback job",
                    subscriptionMigrationJobDetailPage.getErrorOrInfoMessage(),
                    equalTo(PelicanErrorConstants.ANOTHER_MIGRATION_JOB_RUNNING), assertionErrorList);
                updateQuery = UPDATE_QUERY_TO_COMPLETED_STATUS + jobId;
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
            } else {

                Assert.fail("Run migration job has incorrect status");
            }

        } else {

            Assert.fail("Subscription Upload Job Status is incorrect");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will test the roll back job functionality which means whatever the subscriptions are
     * migrated with run migration job should be rolled back to its original state and rollback event should be added to
     * subscription activity. CSE and Audit logs should also be generated for the rollback job.
     *
     * @throws org.apache.http.ParseException
     * @throws IOException
     */
    @Test
    public void testRunRollBackFunctionality() throws org.apache.http.ParseException, IOException {

        // create source bic offering
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String offerId1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getId();
        final String subscriptionOfferingId1 = subscriptionOffering1.getOfferings().get(0).getId();
        LOGGER.info("subscriptionOfferingId1:" + subscriptionOfferingId1);

        // create target bic offering
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String offerId2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getId();
        final String subscriptionOfferingId2 = subscriptionOffering2.getOfferings().get(0).getId();
        LOGGER.info("subscriptionOfferingId2:" + subscriptionOfferingId2);

        final String sourcePriceId = subscriptionOffering1.getIncluded().getPrices().get(0).getId();
        final String targetPriceId = subscriptionOffering2.getIncluded().getPrices().get(0).getId();

        // Submit purchase order to create subscriptions
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(sourcePriceId, 1);

        final PurchaseOrder purchaseOrder1 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final PurchaseOrder purchaseOrder2 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final PurchaseOrder purchaseOrder3 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId3 =
            purchaseOrder3.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create Upload xlsx file for subscription migration upload
        jobName = RandomStringUtils.randomAlphabetic(6);
        final String offerExternalKey1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        final String subscriptionOfferingExternalKey2 = subscriptionOffering2.getOfferings().get(0).getExternalKey();
        final String offerExternalKey2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();
            if (subscriptionMigrationJobDetailPage.getStatus()
                .equals(SubscriptionMigrationJobStatus.COMPLETED.getDisplayName())) {

                String jobId = subscriptionMigrationJobDetailPage.getId();
                validateRollback(subscriptionId1, subscriptionId2, subscriptionId3);
                subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, jobId, null, null, null);
                final int totalRecordsInReport = subscriptionMigrationJobStatusPage.getTotalItems();
                LOGGER.info("Total number of records in report: " + totalRecordsInReport);
                Util.waitInSeconds(TimeConstants.TWO_SEC);
                if (totalRecordsInReport == 1) {
                    subscriptionMigrationJobStatusPage.selectResultRow(totalRecordsInReport);
                    Util.waitInSeconds(TimeConstants.THREE_SEC);
                } else {
                    Assert.fail("There are more than 1 job for a given subscription migration job id");
                }
                subscriptionMigrationJobDetailPage.clickOnRollbackButton();

                pelicanEventsConsumer.clearNotificationsList();
                // Run the subscription migration rollback job
                subscriptionMigrationJobDetailPage.runAJob();
                pelicanEventsConsumer.waitForEvents(5000);
                eventsList = pelicanEventsConsumer.getNotifications();
                if (subscriptionMigrationJobDetailPage.getStatus()
                    .equals(SubscriptionMigrationJobStatus.COMPLETED.getDisplayName())) {

                    jobId = subscriptionMigrationJobDetailPage.getId();
                    validateRollback(subscriptionId1, subscriptionId2, subscriptionId3);

                    // Validate the CSE and AUM notifications for the migrated subscriptions
                    AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0),
                        assertionErrorList);

                    // set subscription offering object
                    subscriptionOffering.setId(subscriptionOfferingId1);
                    productLine.setCode(subscriptionOffering1.getOfferings().get(0).getProductLine());
                    subscriptionOffering.setJProductLine(productLine);
                    // validate change notification
                    validateChangeNotificationForSubscription(subscriptionId1);
                    validateChangeNotificationForSubscription(subscriptionId2);
                    validateChangeNotificationForSubscription(subscriptionId3);
                    // Validate the audit logs
                    SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId1,
                        subscriptionOfferingId2, subscriptionOfferingId1, offerId2, offerId1, targetPriceId,
                        sourcePriceId, assertionErrorList);
                    SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId2,
                        subscriptionOfferingId2, subscriptionOfferingId1, offerId2, offerId1, targetPriceId,
                        sourcePriceId, assertionErrorList);
                    SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId3,
                        subscriptionOfferingId2, subscriptionOfferingId1, offerId2, offerId1, targetPriceId,
                        sourcePriceId, assertionErrorList);

                    // Validate the subscription events
                    validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId1, jobId, sourcePriceId,
                        targetPriceId);
                    validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId2, jobId, sourcePriceId,
                        targetPriceId);
                    validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId3, jobId, sourcePriceId,
                        targetPriceId);
                } else {
                    Assert.fail("Rollback job has an incorrect status");
                }

            } else {

                Assert.fail("Run migration job has incorrect status");
            }

        } else {

            Assert.fail("Subscription Upload Job Status is incorrect");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method which will test the error message on the rollback job when the config time for the rollback
     * job has been passed out.
     *
     */
    @Test
    public void testRunRollBackAfterConfigTimeForRollBack() {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();
            if (subscriptionMigrationJobDetailPage.getStatus()
                .equals(SubscriptionMigrationJobStatus.COMPLETED.getDisplayName())) {

                AssertCollector.assertTrue("Roll Back button should be displayed for completed jobs",
                    subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);

                final String jobId = subscriptionMigrationJobDetailPage.getId();
                updateQuery = UPDATE_RUN_DATE_OF_MIGRATION_JOB + "'"
                    + DateTimeUtils.getCurrentTimeMinusSpecifiedHours(PelicanConstants.AUDIT_LOG_DATE_FORMAT, 49) + "'"
                    + " " + CONDITION_STRING + jobId;
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
                subscriptionMigrationJobDetailPage.clickOnRollbackButton();
                // Run the subscription migration rollback job
                subscriptionMigrationJobDetailPage.runAJobWithOutPageRefresh();
                Util.waitInSeconds(TimeConstants.THREE_SEC);
                AssertCollector.assertThat("Incorrect run by of a rollback job",
                    subscriptionMigrationJobDetailPage.getRunBy(), equalTo(PelicanConstants.HIPHEN),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect run date of a rollback job",
                    subscriptionMigrationJobDetailPage.getRunDate(), equalTo(PelicanConstants.HIPHEN),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect error message when running a rollback job",
                    subscriptionMigrationJobDetailPage.getErrorOrInfoMessage().split("\\(")[0],
                    equalTo(PelicanErrorConstants.ROLL_BACK_ERROR_MESSAGE), assertionErrorList);
            } else {

                Assert.fail("Run migration job has incorrect status");
            }

        } else {

            Assert.fail("Subscription Upload Job Status is incorrect");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a method to find a subscription and validate the subscription activity
     *
     * @param subscriptionId
     * @param jobId
     * @param sourcePriceId
     * @param targetPriceId
     * @throws IOException
     * @throws org.apache.http.ParseException
     */
    private void validateMigratedSubscriptionAndSubscriptionEvent(final String subscriptionId, final String jobId,
        final String sourcePriceId, final String targetPriceId) throws org.apache.http.ParseException, IOException {

        // Find the subscriptions in the admin tool and validate the subscriptions
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        subscriptionPage.findBySubscriptionId(subscriptionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        AssertCollector.assertThat("Incorrect next billing price id", subscriptionDetailPage.getNextBillingPriceId(),
            equalTo(sourcePriceId), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity for a subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUB_REVERT_MIGRATION), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity requestor for a subscription",
            bicSubscriptionActivity.getRequestor().split(" ")[0], equalTo(PelicanConstants.AUTO_USER_NAME),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity memo for a subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(SUBSCRIPTION_MEMO + " " + jobId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity memo old price id for a subscription",
            bicSubscriptionActivity.getMemo().split("\n")[1], equalTo(OLD_PRICE_ID + " " + targetPriceId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity memo new price id for a subscription",
            bicSubscriptionActivity.getMemo().split("\n")[2], equalTo(NEW_PRICE_ID + " " + sourcePriceId),
            assertionErrorList);

        // verify subscription event api for SUB_MIGRATION_REVERTED
        final JSubscriptionEvents subscriptionEvents =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, null);
        final SubscriptionEventsData subscriptionEventsData = Iterables.getLast(subscriptionEvents.getEventsData());
        AssertCollector.assertThat(
            "Activity is not correct for get subscription api for subscription id " + subscriptionId,
            subscriptionEventsData.getEventType(), equalTo(SUB_REVERT_MIGRATION), assertionErrorList);
        AssertCollector.assertThat(
            "Requestor name is not correct for get subscription api for subscription id " + subscriptionId,
            subscriptionEventsData.getRequesterName(), equalTo(PelicanConstants.AUTO_USER_NAME), assertionErrorList);
        AssertCollector.assertThat(
            "Purchase order is not correct for get subscription api for subscription id " + subscriptionId,
            subscriptionEventsData.getPurchaseOrderId(), equalTo(null), assertionErrorList);
        final String expectedMemo = SUBSCRIPTION_MEMO + " " + jobId + ". " + OLD_PRICE_ID + " " + targetPriceId + ". "
            + NEW_PRICE_ID + " " + sourcePriceId;
        AssertCollector.assertTrue("Memo is not correct for for get subscription api subscription id " + subscriptionId,
            subscriptionEventsData.getMemo().startsWith(expectedMemo), assertionErrorList);

    }

    /**
     * This is a data provider to return final successful SubscriptionMigrationJobStatuses.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "successfuljobstatuses")
    public static Object[][] getSuccessfulSubscriptionMigrationJobStatuses() {
        return new Object[][] { { SubscriptionMigrationJobStatus.COMPLETED },
                { SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED }, { SubscriptionMigrationJobStatus.CANCELLED } };
    }

    /**
     * This is a data provider to return final successful SubscriptionMigrationJobStatuses.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "unsuccessfuljobstatuses")
    public static Object[][] getUnSuccessfulSubscriptionMigrationJobStatuses() {
        return new Object[][] { { SubscriptionMigrationJobStatus.FAILED },
                { SubscriptionMigrationJobStatus.VALIDATION_FAILED },
                { SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED },
                { SubscriptionMigrationJobStatus.UPLOADING_FILE },
                { SubscriptionMigrationJobStatus.RUNNING_VALIDATIONS },
                { SubscriptionMigrationJobStatus.RUNNING_FILE } };
    }

    private void validateChangeNotificationForSubscription(final String subscriptionId) {
        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, subscriptionId,
                getUser(), subscriptionOffering, true, assertionErrorList),
            PelicanConstants.UPDATED, subscriptionId, true, assertionErrorList);

        cseHelper.assertionToValidateChangeNotificationHeaderForSubscriptionForSherpaMigration(eventsList,
            subscriptionId, PelicanConstants.UPDATED, assertionErrorList);

        cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForSherpaMigration(eventsList, subscriptionId,
            PelicanConstants.UPDATED, assertionErrorList);

    }

    private void validateRollback(final String subscriptionId1, final String subscriptionId2,
        final String subscriptionId3) {
        AssertCollector.assertTrue("Roll Back button should be displayed for completed jobs",
            subscriptionMigrationJobDetailPage.isRollbackButtonPresent(), assertionErrorList);
        // Click on the triggers job id link and validate the wips in the wip report page
        workInProgressReportResultPage = subscriptionMigrationJobDetailPage.clickOnTriggersJobId();
        AssertCollector.assertThat("Incorrect number of records on the page",
            workInProgressReportResultPage.getTotalResultsInTheReportPage(), equalTo(EXPECTED_COUNT_OF_RECORDS),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect wip state in the report",
            workInProgressReportResultPage.getColumnValuesOfState(),
            everyItem(equalTo(Status.COMPLETE.getDisplayName())), assertionErrorList);
        AssertCollector.assertThat("Incorrect object id in the report",
            workInProgressReportResultPage.getColumnValuesOfObjectId(),
            everyItem(isOneOf(subscriptionId1, subscriptionId2, subscriptionId3)), assertionErrorList);

    }

}
