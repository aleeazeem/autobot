package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionMigrationJobStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.core.Every;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This test class tests subscription migration job status (Sherpa). The page can be accessed in admin tool from
 * Subscriptions -> Subscriptions - > Find jobs.
 *
 * @author jains
 */
@Test(groups = { "excludedClass" })
public class SubscriptionMigrationJobStatusTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionMigrationJobStatusTest.class.getSimpleName());
    private SubscriptionMigrationJobDetailPage subscriptionMigrationJobDetailPage;
    private SubscriptionMigrationJobStatusPage subscriptionMigrationJobStatusPage;
    private static final String FILE_NAME = "UploadSubscriptionMigration.xlsx";
    private List<String> jobIdList;
    private String runByUserId;
    private String runByUserName;
    private String createdByUserId;
    private String createdByUserName;
    private static final String runDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.AUDIT_LOG_DATE_FORMAT);
    private static final String SET_RUN_DATE = ", run_date = '" + runDate + "'";
    private static final String UPDATE_CONDITION = " where id= ";
    private static final String APP_FAMILY_ID_IN_QUERY = " and app_family_id = ";
    private GenericDetails genericDetails;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final UploadSubscriptionMigrationPage uploadSubscriptionMigrationPage =
            adminToolPage.getPage(UploadSubscriptionMigrationPage.class);
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        subscriptionMigrationJobStatusPage = adminToolPage.getPage(SubscriptionMigrationJobStatusPage.class);
        genericDetails = adminToolPage.getPage(GenericDetails.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        jobIdList = new ArrayList<>();
        runByUserId = getEnvironmentVariables().getUserId();
        runByUserName = getEnvironmentVariables().getUserName();
        createdByUserId = getEnvironmentVariables().getUserId();
        createdByUserName = getEnvironmentVariables().getUserName();
        final String setRunBy = ", run_by = " + runByUserId;

        // create offering
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        final String offerExternalKey1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();

        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String subscriptionOfferingExternalKey2 = subscriptionOffering2.getOfferings().get(0).getExternalKey();
        final String offerExternalKey2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();

        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);
        final String jobName = RandomStringUtils.randomAlphabetic(6);

        // upload subscription migration file 7 times to create all the data
        // required by test methods
        for (int i = 0; i < 7; i++) {
            subscriptionMigrationJobDetailPage =
                uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            // add job id to the job id list
            jobIdList.add(subscriptionMigrationJobDetailPage.getId());
        }

        // update job status in database to create all possible job status
        subscriptionMigrationJobStatusPage
            .updateMigrationJobStatus(SubscriptionMigrationJobStatus.UPLOADING_FILE.getDbValue(), jobIdList.get(0));

        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(
            SubscriptionMigrationJobStatus.RUNNING_VALIDATIONS.getDbValue(), jobIdList.get(1));

        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(
            SubscriptionMigrationJobStatus.VALIDATION_FAILED.getDbValue() + SET_RUN_DATE, jobIdList.get(2));

        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(
            SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue(), jobIdList.get(3));

        subscriptionMigrationJobStatusPage
            .updateMigrationJobStatus(SubscriptionMigrationJobStatus.COMPLETED.getDbValue(), jobIdList.get(4));

        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(
            SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED.getDbValue(), jobIdList.get(5));

        subscriptionMigrationJobStatusPage.updateMigrationJobStatus(
            SubscriptionMigrationJobStatus.FAILED.getDbValue() + setRunBy + SET_RUN_DATE, jobIdList.get(6));
    }

    /**
     * This test verifies header of the report.
     */
    @Test
    public void testSubscriptionMigrationJobStatusHeaders() {
        subscriptionMigrationJobStatusPage.navigateToSubscriptionMigrationJobStatusPage();
        final List<String> subscriptionMigrationJobStatusHeaders =
            subscriptionMigrationJobStatusPage.getColumnHeaders();
        AssertCollector.assertThat("Total number of columns is not correct",
            subscriptionMigrationJobStatusHeaders.size(), equalTo(9), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", subscriptionMigrationJobStatusHeaders.get(0),
            equalTo(PelicanConstants.ID_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", subscriptionMigrationJobStatusHeaders.get(1),
            equalTo(PelicanConstants.NAME_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", subscriptionMigrationJobStatusHeaders.get(2),
            equalTo(PelicanConstants.STATUS_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", subscriptionMigrationJobStatusHeaders.get(3),
            equalTo(PelicanConstants.CREATED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", subscriptionMigrationJobStatusHeaders.get(4),
            equalTo(PelicanConstants.CREATED_BY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", subscriptionMigrationJobStatusHeaders.get(5),
            equalTo(PelicanConstants.LAST_MODIFIED_DATE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", subscriptionMigrationJobStatusHeaders.get(6),
            equalTo(PelicanConstants.LAST_MODIFIED_BY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", subscriptionMigrationJobStatusHeaders.get(7),
            equalTo(PelicanConstants.RUN_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", subscriptionMigrationJobStatusHeaders.get(8),
            equalTo(PelicanConstants.RUN_BY), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies all filters of the report.
     *
     * @param jobStatus
     * @param id
     * @param runDate
     * @param runById
     */
    @Test(dataProvider = "dataForSubscriptionMigrationJobStatus")
    public void testSubscriptionMigrationJobStatusWithFilters(final String jobStatus, final String id,
        final String runDate, final String runById, final String createdByUserId) {
        subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(jobStatus, id, runDate, runById,
            createdByUserId);

        final int totalRecordsInReport = subscriptionMigrationJobStatusPage.getTotalItems();
        LOGGER.info("Total number of records in report: " + totalRecordsInReport);

        if (totalRecordsInReport > 0) {
            if (jobStatus != null) {
                AssertCollector.assertThat("Job status is not correct ",
                    subscriptionMigrationJobStatusPage.getValuesFromStatusColumn(), Every.everyItem(equalTo(jobStatus)),
                    assertionErrorList);
            }
            if (id != null) {
                AssertCollector.assertThat("Id is not correct ",
                    subscriptionMigrationJobStatusPage.getValuesFromIdColumn(), Every.everyItem(equalTo(id)),
                    assertionErrorList);
            }
            if (runDate != null) {
                AssertCollector.assertThat("Run Date should be equal to " + runDate,
                    DateTimeUtils.convertStringListToDateList(
                        subscriptionMigrationJobStatusPage.getValuesFromRunDateColumn(),
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(greaterThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(runDate, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);
            }
            if (runById != null) {
                // Run by column is constructed with "RunByUserName
                // (RunByUserId)"
                AssertCollector.assertThat("Run by column value is not correct ",
                    subscriptionMigrationJobStatusPage.getValuesFromRunByColumn(),
                    Every.everyItem(equalTo(runByUserName + " (" + runById + ")")), assertionErrorList);
            }
            if (createdByUserId != null) {
                // Created by column is constructed with "CreatedByUserName
                // (CreatedByUserId)"
                AssertCollector.assertThat("Created by column value is not correct ",
                    subscriptionMigrationJobStatusPage.getValuesFromCreatedByColumn(),
                    Every.everyItem(equalTo(createdByUserName + " (" + createdByUserId + ")")), assertionErrorList);
            }
        } else {
            Assert.fail("Report is empty for the selected filters.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that by clicking on any record on the report page, user is navigated to job detail page.
     *
     * @param jobStatus
     * @param id
     * @param runDate
     * @param runById
     */
    @Test(dataProvider = "dataForSubscriptionMigrationJobStatus")
    public void testSubscriptionMigrationJobStatusLinkToJobDetail(final String jobStatus, final String id,
        final String runDate, final String runById, final String createdByUserId) {
        subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(jobStatus, id, runDate, runById,
            createdByUserId);

        final int totalRecordsInReport = subscriptionMigrationJobStatusPage.getTotalItems();
        LOGGER.info("Total number of records in report: " + totalRecordsInReport);

        if (totalRecordsInReport > 0) {
            final int selectedRowIndex =
                subscriptionMigrationJobStatusPage.selectRowRandomlyFromFirstPage(totalRecordsInReport);
            final String expectedId = subscriptionMigrationJobStatusPage.getValuesFromIdColumn().get(selectedRowIndex);
            final String expectedName =
                subscriptionMigrationJobStatusPage.getValuesFromNameColumn().get(selectedRowIndex);
            final String expectedStatus =
                subscriptionMigrationJobStatusPage.getValuesFromStatusColumn().get(selectedRowIndex);
            final String expectedCreatedDate =
                subscriptionMigrationJobStatusPage.getValuesFromCreatedDateColumn().get(selectedRowIndex);
            final String expectedCreatedBy =
                subscriptionMigrationJobStatusPage.getValuesFromCreatedByColumn().get(selectedRowIndex);
            final String expectedLastModifiedDate =
                subscriptionMigrationJobStatusPage.getValuesFromLastModifiedDateColumn().get(selectedRowIndex);
            final String expectedLastModifiedBy =
                subscriptionMigrationJobStatusPage.getValuesFromLastModifiedByColumn().get(selectedRowIndex);
            final String expectedRunDate =
                subscriptionMigrationJobStatusPage.getValuesFromRunDateColumn().get(selectedRowIndex);
            final String expectedRunBy =
                subscriptionMigrationJobStatusPage.getValuesFromRunByColumn().get(selectedRowIndex);

            LOGGER.info("Navigating to job detail page.");
            subscriptionMigrationJobStatusPage.selectResultRow(selectedRowIndex + 1);

            if (genericDetails.getTitle().equals(PelicanConstants.SUBSCRIPTION_MIGRATION_JOB_DETAIL_TITLE)) {
                AssertCollector.assertThat("Id is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getId(), equalTo(expectedId), assertionErrorList);
                AssertCollector.assertThat("Job name is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getJobName(), equalTo(expectedName), assertionErrorList);
                AssertCollector.assertThat("Created date is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getCreated(), equalTo(expectedCreatedDate), assertionErrorList);
                AssertCollector.assertThat("Created by is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getCreatedBy(), equalTo(expectedCreatedBy), assertionErrorList);
                AssertCollector.assertThat("Last modified date is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getLastModified(), equalTo(expectedLastModifiedDate),
                    assertionErrorList);
                AssertCollector.assertThat("Last modified by is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getLastModifiedBy(), equalTo(expectedLastModifiedBy),
                    assertionErrorList);
                AssertCollector.assertThat("Run date is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getRunDate(), equalTo(expectedRunDate), assertionErrorList);
                AssertCollector.assertThat("Run by is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getRunBy(), equalTo(expectedRunBy), assertionErrorList);
                AssertCollector.assertThat("Status is not correct on migration detail page",
                    subscriptionMigrationJobDetailPage.getStatus(), equalTo(expectedStatus), assertionErrorList);

            } else {
                Assert.fail("Navigation is not on mifration job detail page.");
            }
        } else {
            Assert.fail("Report is empty for the selected filters.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies if a non-numeric value is passed to run by field, an error is generated.
     */
    @Test
    public void testSubscriptionMigrationJobStatusRunByErrorFieldForNonNumericValue() {
        subscriptionMigrationJobStatusPage =
            subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, null, null, "abc", null);
        final String actualErrorMesageForRunByField = subscriptionMigrationJobStatusPage.getErrorMessageForField();
        final String actualErrorMessage = subscriptionMigrationJobStatusPage.getError();
        AssertCollector.assertThat("Error message for run by field is not correct", actualErrorMesageForRunByField,
            equalTo(PelicanErrorConstants.NUMBER_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Error message is not correct", actualErrorMessage,
            equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that the report data is sorted by last modified date.
     */
    @Test
    public void testSubscriptionMigrationJobStatusRecordsSortingByLastModifiedDate() {
        subscriptionMigrationJobStatusPage.navigateToSubscriptionMigrationJobStatusPage();
        final String jobIdBeforeUpdate = subscriptionMigrationJobStatusPage.getValuesFromIdColumn().get(0);

        final String updateQuery = "update subscription_migration_job set last_modified_date='"
            + DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.AUDIT_LOG_DATE_FORMAT) + "'" + UPDATE_CONDITION
            + jobIdList.get(0) + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        subscriptionMigrationJobStatusPage =
            subscriptionMigrationJobStatusPage.getMigrationJobStatusWithFilters(null, null, null, null, null);

        final String expectedJobIdAfterUpdate = jobIdList.get(0);

        final String actualJobIdAfterUpdate = subscriptionMigrationJobStatusPage.getValuesFromIdColumn().get(0);

        AssertCollector.assertThat("Records are not sorted correctly by last modified date", actualJobIdAfterUpdate,
            equalTo(expectedJobIdAfterUpdate), assertionErrorList);
        AssertCollector.assertThat(
            "Job id before updating the job should not be equal to job id after updating the job", jobIdBeforeUpdate,
            not(equalTo(expectedJobIdAfterUpdate)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that subscription migration job status is not accessible when feature flag is set to false.
     */
    @Test
    public void testSubscriptionMigrationJobStatusWithFeatureFlagFalse() {
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, false);
        subscriptionMigrationJobStatusPage.navigateToSubscriptionMigrationJobStatusPage();
        adminToolPage.getPage(SubscriptionMigrationJobStatusPage.class);

        // After setting feature flag to false, blank page is loaded and the title of page is null
        AssertCollector.assertThat(
            "Subscription migration job status page should not be accessible when feature flag is set to false",
            subscriptionMigrationJobStatusPage.getTitle(), equalTo(null), assertionErrorList);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, true);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider with different filters
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForSubscriptionMigrationJobStatus")
    public Object[][] getTestDataForSubscriptionMigrationJobStatus() {
        return new Object[][] { { SubscriptionMigrationJobStatus.FAILED.getDisplayName(), null, null, null, null },
                { null, jobIdList.get(3), null, null, null }, { null, null, null, runByUserId, createdByUserId },
                { null, null, DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                        runByUserId, createdByUserId },

        };
    }
}
