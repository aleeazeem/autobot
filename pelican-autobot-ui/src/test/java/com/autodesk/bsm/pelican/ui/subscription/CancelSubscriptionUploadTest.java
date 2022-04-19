package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionMigrationJobStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is the test class which will cancel the upload subscription upload job if the job is hung for more than 10
 * minutes.
 *
 * @author yerragv.
 */
@Test(groups = { "excludedClass" })
public class CancelSubscriptionUploadTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private RolesHelper rolesHelper;
    private static final String FILE_NAME = "UploadSubscriptionMigration.xlsx";
    private UploadSubscriptionMigrationPage uploadSubscriptionMigrationPage;
    private SubscriptionMigrationJobDetailPage subscriptionMigrationJobDetailPage;
    private SubscriptionMigrationJobStatusPage subscriptionMigrationJobStatusPage;
    private String subscriptionOfferingExternalKey1;
    private String offerExternalKey1;
    private String subscriptionOfferingExternalKey2;
    private String offerExternalKey2;
    private String jobName;
    private static final String UPDATE_TABLE_NAME = "update subscription_migration_job set status= ";
    private static final String UPDATE_COLUMN_NAME = "LAST_MODIFIED_DATE= ";
    private static final String UPDATE_CONDITION = " where id= ";
    private static final String APP_FAMILY_ID_IN_QUERY = " and app_family_id = ";
    private String updateQuery;
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelSubscriptionUploadTest.class.getSimpleName());

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        uploadSubscriptionMigrationPage = adminToolPage.getPage(UploadSubscriptionMigrationPage.class);
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        subscriptionMigrationJobStatusPage = adminToolPage.getPage(SubscriptionMigrationJobStatusPage.class);
        final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        rolesHelper = new RolesHelper(getEnvironmentVariables());
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());

        // create two bic offerings
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        final String subscriptionOfferingId1 = subscriptionOffering1.getOfferings().get(0).getId();
        LOGGER.info("subscriptionOfferingId1:" + subscriptionOfferingId1);
        offerExternalKey1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey2 = subscriptionOffering2.getOfferings().get(0).getExternalKey();
        final String subscriptionOfferingId2 = subscriptionOffering2.getOfferings().get(0).getId();
        LOGGER.info("subscriptionOfferingId2:" + subscriptionOfferingId2);
        offerExternalKey2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();

        final String sourcePriceId = subscriptionOffering1.getIncluded().getPrices().get(0).getId();

        // Submit purchase order to create subscriptions
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(sourcePriceId, 1);
        final UserUtils userUtils = new UserUtils();
        final BuyerUser buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
        final PurchaseOrder purchaseOrder1 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, buyerUser);
        final String subscriptionId1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id1: " + subscriptionId1);

        // Create stores, offerings and subscriptions for the negative test cases
        final JStore store1 = storeApiUtils.addStore(Status.ACTIVE, Country.DE, Currency.USD, null, false);
        final String externalKeyOfPriceList1 = store1.getIncluded().getPriceLists().get(0).getExternalKey();

        final Offerings subscriptionOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList1,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String sourcePriceId1 = subscriptionOffering3.getIncluded().getPrices().get(0).getId();

        // Submit purchase order to create subscriptions
        priceMap.clear();
        priceMap.put(sourcePriceId1, 1);

        final PurchaseOrder purchaseOrder2 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, buyerUser);
        final String subscriptionId5 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id5: " + subscriptionId5);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, true);
    }

    /**
     * This is a test case which will test the visibility of the cancel button when the upload job is in completed
     * state.
     *
     * @param status - job status.
     */
    @Test(dataProvider = "finaljobstatuses")
    public void testCancelButtonVisibilityForCompletedJobs(final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus().equals(status.getDisplayName())) {

            AssertCollector.assertFalse("Cancel button should not be displayed for completed jobs",
                subscriptionMigrationJobDetailPage.isCancelButtonPresent(), assertionErrorList);
        } else {

            // Update the job status in db
            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            Util.waitInSeconds(TimeConstants.ONE_SEC);
            subscriptionMigrationJobDetailPage.refreshPage();

            AssertCollector.assertFalse("Cancel button should not be displayed for completed jobs",
                subscriptionMigrationJobDetailPage.isCancelButtonPresent(), assertionErrorList);

            // Reset the job status in db to its original value
            updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue()
                + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test case will test the cancel functionality when the migration upload job is in progress state for more
     * than 10 minutes.
     *
     * @param status
     */
    @Test(dataProvider = "inprogressjobstatuses")
    public void testCancelFunctionalityForInProgressJobsRunningForMoreThanSpecifiedTime_DEFECT_BIC_4670(
        final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            final String oldMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String oldJobId = subscriptionMigrationJobDetailPage.getId();
            final String oldCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String oldCreated = subscriptionMigrationJobDetailPage.getCreated();
            final String oldFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String oldLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
            final String oldLastModified = subscriptionMigrationJobDetailPage.getLastModified();

            // Update the job status in db
            updateQuery = UPDATE_TABLE_NAME + status.getDbValue() + "," + UPDATE_COLUMN_NAME + "'"
                + DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT) + "'"
                + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            Util.waitInSeconds(TimeConstants.ONE_SEC);
            subscriptionMigrationJobDetailPage.refreshPage();

            final boolean isCancelButtonPresent = subscriptionMigrationJobDetailPage.isCancelButtonPresent();

            if (isCancelButtonPresent) {

                subscriptionMigrationJobDetailPage.clickOnCancelButton();
                final String newMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
                final String newJobId = subscriptionMigrationJobDetailPage.getId();
                final String newCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
                final String newCreated = subscriptionMigrationJobDetailPage.getCreated();
                final String newFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
                final String newLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
                final String newLastModified = subscriptionMigrationJobDetailPage.getLastModified();
                final String newJobStatus = subscriptionMigrationJobDetailPage.getStatus();

                AssertCollector.assertThat("Incorrect subscription migration job name", oldMigrationJobName,
                    equalTo(newMigrationJobName), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration job id", oldJobId, equalTo(newJobId),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration created by", oldCreatedBy,
                    equalTo(newCreatedBy), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration created time", oldCreated,
                    equalTo(newCreated), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration file upload job id", oldFileUploadId,
                    equalTo(newFileUploadId), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration last modified by", oldLastModifiedBy,
                    equalTo(newLastModifiedBy), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration last modified", oldLastModified,
                    not(newLastModified), assertionErrorList);
                AssertCollector.assertThat("Incorrect job status after cancellation", newJobStatus,
                    equalTo(SubscriptionMigrationJobStatus.CANCELLED.getDisplayName()), assertionErrorList);

                // Reset the job status in db to its original value
                updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue()
                    + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                    + getEnvironmentVariables().getAppFamilyId();
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            } else {
                Assert.fail("Cancel button is not present for the jobs in Progress state");
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case will test the cancel functionality when the migration upload job is in progress state for less
     * than 10 minutes.
     *
     * @param status
     */
    @Test(dataProvider = "inprogressjobstatuses")
    public void testCancelFunctionalityForInProgressJobsRunningForLessThanSpecifiedTime(
        final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            final String oldMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String oldJobId = subscriptionMigrationJobDetailPage.getId();
            final String oldCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String oldCreated = subscriptionMigrationJobDetailPage.getCreated();
            final String oldFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String oldLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
            final String oldLastModified = subscriptionMigrationJobDetailPage.getLastModified();

            // Update the job status in db
            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            Util.waitInSeconds(TimeConstants.TWO_SEC);
            subscriptionMigrationJobDetailPage.refreshPage();

            final boolean isCancelButtonPresent = subscriptionMigrationJobDetailPage.isCancelButtonPresent();
            LOGGER.info("Is Cancel Button Present: " + isCancelButtonPresent);
            AssertCollector.assertTrue("Cancel button should be displayed for in progress jobs", isCancelButtonPresent,
                assertionErrorList);

            if (isCancelButtonPresent) {

                subscriptionMigrationJobDetailPage.clickOnCancelButton();

                AssertCollector.assertThat(
                    "Incorrect error message displayed when the job is running for less than the " + "specified time",
                    subscriptionMigrationJobDetailPage.getErrorOrInfoMessage(),
                    equalTo(PelicanErrorConstants.CANCEL_MIGRATION_JOB_ERROR_MESSAGE), assertionErrorList);

                final String newMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
                final String newJobId = subscriptionMigrationJobDetailPage.getId();
                final String newCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
                final String newCreated = subscriptionMigrationJobDetailPage.getCreated();
                final String newFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
                final String newLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
                final String newLastModified = subscriptionMigrationJobDetailPage.getLastModified();

                AssertCollector.assertThat("Incorrect subscription migration job name", oldMigrationJobName,
                    equalTo(newMigrationJobName), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration job id", oldJobId, equalTo(newJobId),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration created by", oldCreatedBy,
                    equalTo(newCreatedBy), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration created time", oldCreated,
                    equalTo(newCreated), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration file upload job id", oldFileUploadId,
                    equalTo(newFileUploadId), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration last modified by", oldLastModifiedBy,
                    equalTo(newLastModifiedBy), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription migration last modified", oldLastModified,
                    equalTo(newLastModified), assertionErrorList);

                // Reset the job status in db to its original value
                updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue()
                    + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                    + getEnvironmentVariables().getAppFamilyId();
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            }
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method to check whether Non Offering Manager is able to click on cancel button
     *
     * @param status - subscription migration job status
     */
    @Test(dataProvider = "inprogressjobstatuses")
    public void testNonOfferingManagerClickCancelJob(final SubscriptionMigrationJobStatus status) {
        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();
        String jobId = "";
        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            jobId = subscriptionMigrationJobDetailPage.getId();
        }

        // Update the job status in db
        updateQuery =
            UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        Util.waitInSeconds(TimeConstants.ONE_SEC);
        subscriptionMigrationJobDetailPage.refreshPage();

        final boolean isCancelButtonPresent = subscriptionMigrationJobDetailPage.isCancelButtonPresent();
        AssertCollector.assertTrue("Cancel button should be displayed for in progress jobs", isCancelButtonPresent,
            assertionErrorList);

        if (isCancelButtonPresent) {
            final HashMap<String, String> userParams = new HashMap<>();
            userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

            // Log in as a non-offering manager user
            final List<String> nonOfferingManagerRoleList = rolesHelper.getNonOfferingManagerRoleList();
            new UserUtils().createAssignRoleAndLoginUser(userParams, nonOfferingManagerRoleList, adminToolPage,
                getEnvironmentVariables());

            subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, jobId, null, null, null);
            final int totalRecordsInReport = subscriptionMigrationJobStatusPage.getTotalItems();
            LOGGER.info("Total number of records in report: " + totalRecordsInReport);
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            if (totalRecordsInReport == 1) {
                subscriptionMigrationJobStatusPage.selectResultRow(totalRecordsInReport);
                Util.waitInSeconds(TimeConstants.THREE_SEC);

                AssertCollector.assertFalse("Non offering manager is able to click on the cancel button",
                    subscriptionMigrationJobDetailPage.isCancelButtonClickable(), assertionErrorList);

                // Reset the job status in db to its original value
                updateQuery = UPDATE_TABLE_NAME + SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue()
                    + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId() + APP_FAMILY_ID_IN_QUERY
                    + getEnvironmentVariables().getAppFamilyId();
                DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            } else {
                Assert.fail("There are multiple jobs with the same job id");
            }
        } else {
            Assert.fail("Cancel button is not displayed for in progress jobs");
        }
        adminToolPage.logout();
        adminToolPage.login();
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a data provider to return In Progress SubscriptionMigrationJobStatuses.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "inprogressjobstatuses")
    public static Object[][] getInProgressSubscriptionMigrationJobStatuses() {
        return new Object[][] { { SubscriptionMigrationJobStatus.UPLOADING_FILE },
                { SubscriptionMigrationJobStatus.RUNNING_VALIDATIONS },
                { SubscriptionMigrationJobStatus.RUNNING_FILE } };
    }

    /**
     * This is a data provider to return final SubscriptionMigrationJobStatuses.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "finaljobstatuses")
    public static Object[][] getFinalSubscriptionMigrationJobStatuses() {
        return new Object[][] { { SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED },
                { SubscriptionMigrationJobStatus.VALIDATION_FAILED }, { SubscriptionMigrationJobStatus.COMPLETED },
                { SubscriptionMigrationJobStatus.FAILED }, { SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED } };
    }
}
