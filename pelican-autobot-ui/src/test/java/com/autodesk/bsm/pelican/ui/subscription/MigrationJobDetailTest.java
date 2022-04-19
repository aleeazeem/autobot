package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionMigrationJobStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.ConfirmationPopup;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
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
 * This is the test class which will test the Re-Upload and delete functionality on the subscription migration detail
 * page
 *
 * @author yerragv
 */
@Test(groups = { "excludedClass" })
public class MigrationJobDetailTest extends SeleniumWebdriver {

    private static HashMap<String, String> userParams;
    private UserUtils userUtils;
    private AdminToolPage adminToolPage;
    private RolesHelper rolesHelper;
    private boolean isNonOfferingManagerUserLoggedIn;
    private String jobId;
    private static final String FILE_NAME = "UploadSubscriptionMigration.xlsx";
    private static final String NEW_FILE_NAME = "UploadSubscriptionMigration1.xlsx";
    private UploadSubscriptionMigrationPage uploadSubscriptionMigrationPage;
    private SubscriptionMigrationJobDetailPage subscriptionMigrationJobDetailPage;
    private SubscriptionMigrationJobStatusPage subscriptionMigrationJobStatusPage;
    private String subscriptionOfferingExternalKey1;
    private String offerExternalKey1;
    private String subscriptionOfferingExternalKey2;
    private String offerExternalKey2;
    private String subscriptionOfferingExternalKey3;
    private String offerExternalKey3;
    private String subscriptionOfferingExternalKey4;
    private String offerExternalKey4;
    private String jobName;
    private GenericDetails genericDetails;
    private static final String UPDATE_TABLE_NAME = "update subscription_migration_job set status= ";
    private static final String UPDATE_CONDITION = " where id= ";
    private static final String APP_FAMILY_ID_IN_QUERY = " and app_family_id = ";
    private String updateQuery;
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationJobDetailTest.class.getSimpleName());

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
        genericDetails = adminToolPage.getPage(GenericDetails.class);
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        rolesHelper = new RolesHelper(getEnvironmentVariables());
        isNonOfferingManagerUserLoggedIn = false;

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

        final PurchaseOrder purchaseOrder1 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id1: " + subscriptionId1);

        userParams = new HashMap<>();
        userUtils = new UserUtils();
        // Create stores, offerings and subscriptions for the negative test
        // cases
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        final JStore store1 = storeApiUtils.addStore(Status.ACTIVE, Country.DE, Currency.USD, null, false);
        final String externalKeyOfPriceList1 = store1.getIncluded().getPriceLists().get(0).getExternalKey();

        final Offerings subscriptionOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList1,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey3 = subscriptionOffering3.getOfferings().get(0).getExternalKey();
        offerExternalKey3 = subscriptionOffering3.getIncluded().getBillingPlans().get(0).getExternalKey();
        final Offerings subscriptionOffering4 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList1,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey4 = subscriptionOffering4.getOfferings().get(0).getExternalKey();

        offerExternalKey4 = subscriptionOffering4.getIncluded().getBillingPlans().get(0).getExternalKey();

        final String sourcePriceId1 = subscriptionOffering3.getIncluded().getPrices().get(0).getId();

        // Submit purchase order to create subscriptions
        priceMap.clear();
        priceMap.put(sourcePriceId1, 1);

        final PurchaseOrder purchaseOrder2 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId5 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id5: " + subscriptionId5);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, true);
    }

    /**
     * This is a test case which will test the visibility of the re-upload button when the upload job is in progress
     * state.
     *
     * @param status - job status.
     */
    @Test(dataProvider = "inprogressjobstatuses")
    public void testReUploadButtonVisibilityForInProgressJobs(final SubscriptionMigrationJobStatus status) {

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

            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            subscriptionMigrationJobDetailPage.refreshPage();
            AssertCollector.assertThat("Re-Upload Button should not be present on the page",
                subscriptionMigrationJobDetailPage.isReUploadButtonPresent(), equalTo(false), assertionErrorList);

            updateQuery = UPDATE_TABLE_NAME + "5" + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            subscriptionMigrationJobDetailPage.refreshPage();

        } else {
            Assert.fail("The subscription upload job has a incorrect status");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test case will test the subscription migration re-upload functionality when the migration upload job is in
     * completed status.
     *
     * @param status
     */
    @Test(dataProvider = "finaljobstatuses")
    public void testReUploadFunctionalityForCompletedJobs(final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())
            && status.equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED)) {

            AssertCollector.assertThat("Re-Upload Button should be present on the page",
                subscriptionMigrationJobDetailPage.isReUploadButtonPresent(), equalTo(true), assertionErrorList);

            final String oldMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String oldJobId = subscriptionMigrationJobDetailPage.getId();
            final String oldCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String oldCreated = subscriptionMigrationJobDetailPage.getCreated();
            final String oldFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String oldLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
            final String oldLastModified = subscriptionMigrationJobDetailPage.getLastModified();
            final String oldSourcePlanName = subscriptionMigrationJobDetailPage.getSourcePlanNameInPriceMapping();
            final String oldTargetPlanName = subscriptionMigrationJobDetailPage.getTargetPlanNameInPriceMapping();
            final String oldJobStatus = subscriptionMigrationJobDetailPage.getStatus();

            uploadSubscriptionMigrationPage = subscriptionMigrationJobDetailPage.clickOnReUploadButton();
            final String newJobName = RandomStringUtils.randomAlphabetic(6);
            uploadSubscriptionMigrationPage.createXlsxAndWriteData(NEW_FILE_NAME, subscriptionOfferingExternalKey3,
                offerExternalKey3, subscriptionOfferingExternalKey4, offerExternalKey4);

            subscriptionMigrationJobDetailPage =
                uploadSubscriptionMigrationPage.reUploadSubscriptionMigrationFileWithoutWait(jobName, NEW_FILE_NAME);
            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            subscriptionMigrationJobDetailPage.refreshPage();

            final String newMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String newJobId = subscriptionMigrationJobDetailPage.getId();
            final String newCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String newCreated = subscriptionMigrationJobDetailPage.getCreated();
            final String newFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String newLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
            final String newLastModified = subscriptionMigrationJobDetailPage.getLastModified();
            final String newSourcePlanName = subscriptionMigrationJobDetailPage.getSourcePlanNameInPriceMapping();
            final String newTargetPlanName = subscriptionMigrationJobDetailPage.getTargetPlanNameInPriceMapping();
            final String newJobStatus = subscriptionMigrationJobDetailPage.getStatus();

            AssertCollector.assertThat("Incorrect subscription migration job name", oldMigrationJobName,
                equalTo(newMigrationJobName), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration job id", oldJobId, equalTo(newJobId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration created by", oldCreatedBy,
                equalTo(newCreatedBy), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration created time", oldCreated, equalTo(newCreated),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration file upload job id", oldFileUploadId,
                not(newFileUploadId), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration last modified by", oldLastModifiedBy,
                equalTo(newLastModifiedBy), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration last modified", oldLastModified,
                not(newLastModified), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration source plan name", oldSourcePlanName,
                not(newSourcePlanName), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration target plan name", oldTargetPlanName,
                not(newTargetPlanName), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration status", oldJobStatus, equalTo(newJobStatus),
                assertionErrorList);

        } else if (status.equals(SubscriptionMigrationJobStatus.VALIDATION_FAILED)) {

            final String oldMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String oldJobId = subscriptionMigrationJobDetailPage.getId();
            final String oldCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String oldCreated = subscriptionMigrationJobDetailPage.getCreated();
            final String oldFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String oldLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
            final String oldLastModified = subscriptionMigrationJobDetailPage.getLastModified();
            final String oldSourcePlanName = subscriptionMigrationJobDetailPage.getSourcePlanNameInPriceMapping();
            final String oldTargetPlanName = subscriptionMigrationJobDetailPage.getTargetPlanNameInPriceMapping();
            final String oldJobStatus = subscriptionMigrationJobDetailPage.getStatus();

            updateQuery =
                UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                    + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            subscriptionMigrationJobDetailPage.refreshPage();

            AssertCollector.assertThat("Re-Upload Button should be present on the page",
                subscriptionMigrationJobDetailPage.isReUploadButtonPresent(), equalTo(true), assertionErrorList);

            uploadSubscriptionMigrationPage = subscriptionMigrationJobDetailPage.clickOnReUploadButton();
            final String newJobName = RandomStringUtils.randomAlphabetic(6);
            uploadSubscriptionMigrationPage.createXlsxAndWriteData(NEW_FILE_NAME, subscriptionOfferingExternalKey3,
                offerExternalKey3, subscriptionOfferingExternalKey4, offerExternalKey4);

            subscriptionMigrationJobDetailPage =
                uploadSubscriptionMigrationPage.reUploadSubscriptionMigrationFileWithoutWait(jobName, NEW_FILE_NAME);
            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            subscriptionMigrationJobDetailPage.refreshPage();

            final String newMigrationJobName = subscriptionMigrationJobDetailPage.getJobName();
            final String newJobId = subscriptionMigrationJobDetailPage.getId();
            final String newCreatedBy = subscriptionMigrationJobDetailPage.getCreatedBy();
            final String newCreated = subscriptionMigrationJobDetailPage.getCreated();
            final String newFileUploadId = subscriptionMigrationJobDetailPage.getFileUploadJobId();
            final String newLastModifiedBy = subscriptionMigrationJobDetailPage.getLastModifiedBy();
            final String newLastModified = subscriptionMigrationJobDetailPage.getLastModified();
            final String newSourcePlanName = subscriptionMigrationJobDetailPage.getSourcePlanNameInPriceMapping();
            final String newTargetPlanName = subscriptionMigrationJobDetailPage.getTargetPlanNameInPriceMapping();
            final String newJobStatus = subscriptionMigrationJobDetailPage.getStatus();

            AssertCollector.assertThat("Incorrect subscription migration job name", oldMigrationJobName,
                equalTo(newMigrationJobName), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration job id", oldJobId, equalTo(newJobId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration created by", oldCreatedBy,
                equalTo(newCreatedBy), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration created time", oldCreated, equalTo(newCreated),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration file upload job id", oldFileUploadId,
                not(newFileUploadId), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration last modified by", oldLastModifiedBy,
                equalTo(newLastModifiedBy), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration last modified", oldLastModified,
                not(newLastModified), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration source plan name", oldSourcePlanName,
                not(newSourcePlanName), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration target plan name", oldTargetPlanName,
                not(newTargetPlanName), assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription migration status", oldJobStatus, equalTo(newJobStatus),
                assertionErrorList);

        } else {
            Assert.fail("The subscription upload has a incorrect status");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method to check whether Non Offering-Manager is able to click on re-upload button
     *
     * @param status - subscription migration job status
     */
    @Test
    public void testNonOfferingManagerClickReUploadJob() {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();
        String jobId = "";
        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            jobId = subscriptionMigrationJobDetailPage.getId();
        }

        if (!isNonOfferingManagerUserLoggedIn) {
            userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
            // Log in as a non-offering manager user
            final List<String> nonOfferingManagerRoleList = rolesHelper.getNonOfferingManagerRoleList();
            userUtils.createAssignRoleAndLoginUser(userParams, nonOfferingManagerRoleList, adminToolPage,
                getEnvironmentVariables());
            isNonOfferingManagerUserLoggedIn = true;
        }
        subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, jobId, null, null, null);
        final int totalRecordsInReport = subscriptionMigrationJobStatusPage.getTotalItems();
        LOGGER.info("Total number of records in report: " + totalRecordsInReport);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        if (totalRecordsInReport == 1) {
            subscriptionMigrationJobStatusPage.selectResultRow(totalRecordsInReport);
            Util.waitInSeconds(TimeConstants.THREE_SEC);
            AssertCollector.assertFalse("Non Offering Manager is able to click on the re-upload button",
                subscriptionMigrationJobDetailPage.isReUploadButtonclickable(), assertionErrorList);
        } else {
            Assert.fail("There are multiple jobs with the same job id");
        }
        AssertCollector.assertAll(assertionErrorList);
        adminToolPage.logout();
        adminToolPage.login();
    }

    /**
     * Verify Delete Migration Job functionality for Jobs status ending with "ed"(example: Uploaded) as offering manager
     * user (Roles)
     *
     * @param jobStatus
     * @param validation
     */
    @Test(dataProvider = "subscriptionMigrationJobStatus")
    public void testDeleteMigrationJobFeatureAsOfferingManagerUser(final String jobStatus, final boolean validation) {
        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        jobId = subscriptionMigrationJobDetailPage.getId();
        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(jobStatus, jobId);
        if (validation) {
            subscriptionMigrationJobStatusPage.deleteJob(jobId);
        } else {
            if (subscriptionMigrationJobStatusPage.doesWebElementExist(genericDetails.deleteButton)) {
                Assert
                    .fail("Delete button is seen in the status " + jobStatus + " it shouldnt show,test case Failure ");
                subscriptionMigrationJobStatusPage
                    .updateMigrationJobStatus(SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED.getDbValue(), jobId);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Delete Migration Job functionality for Jobs status ending with "ed"(example: Uploaded) as Non Offering
     * Manager user(role) Note: No Delete's are allowed for non Offering Manager user
     *
     * @param jobStatus
     * @param validation
     */
    @Test(dataProvider = "subscriptionMigrationJobStatus")
    public void testDeleteMigrationJobFeatureAsNonOfferingManagerUser(final String jobStatus,
        final boolean validation) {
        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        jobId = subscriptionMigrationJobDetailPage.getId();
        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(jobStatus, jobId);

        try {
            userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
            userUtils.createAssignRoleAndLoginUser(userParams, rolesHelper.getNonOfferingManagerRoleList(),
                adminToolPage, getEnvironmentVariables());

            subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, jobId, "", "", null);
            subscriptionMigrationJobStatusPage.selectResultRow(subscriptionMigrationJobStatusPage.getTotalItems());
            if (validation) {
                genericDetails.clickOnDeleteButton();
                final ConfirmationPopup popup = genericDetails.getPage(ConfirmationPopup.class);
                if (popup.isPopUpContainerExists()) {
                    Assert.fail("For Non Offering Manager User, Delete functionality is working in the job status "
                        + jobStatus + ". Test case Failure!!!!!");
                }
            } else {
                if (subscriptionMigrationJobStatusPage.doesWebElementExist(genericDetails.deleteButton)) {
                    Assert.fail("For non offering manager user: Delete button is seen in the status " + jobStatus
                        + " it shouldnt show,test case Failure ");
                }
            }
        } finally {
            adminToolPage.logout();
            adminToolPage.login();
            subscriptionMigrationJobStatusPage
                .updateMigrationJobStatus(SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED.getDbValue(), jobId);
        }
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
        return new Object[][] { { SubscriptionMigrationJobStatus.VALIDATION_FAILED },
                { SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED } };
    }

    /**
     * Data provider for Delete Sherpa Job with status name and validation condition
     *
     * @return Object[][]
     */
    @DataProvider(name = "subscriptionMigrationJobStatus")
    public Object[][] getSubscriptionMigrationJobStatus() {
        return new Object[][] { { SubscriptionMigrationJobStatus.UPLOADING_FILE.getDbValue(), false },
                { SubscriptionMigrationJobStatus.VALIDATION_FAILED.getDbValue(), true },
                { SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue(), true },
                { SubscriptionMigrationJobStatus.RUNNING_VALIDATIONS.getDbValue(), false },
                { SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED.getDbValue(), false },
                { SubscriptionMigrationJobStatus.COMPLETED.getDbValue(), false },
                { SubscriptionMigrationJobStatus.FAILED.getDbValue(), false } };
    }
}
