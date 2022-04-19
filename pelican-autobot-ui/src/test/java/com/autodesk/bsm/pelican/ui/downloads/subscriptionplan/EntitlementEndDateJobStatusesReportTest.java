package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.reports.JobStatusesReportPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is a test class which will test the Entitlements end date job statuses report in admin tool
 *
 * @author mandas
 */
public class EntitlementEndDateJobStatusesReportTest extends SeleniumWebdriver {

    private JobStatusesReportPage jobStatusesReport;
    private JobsClient jobsResource;
    private String itemId1;
    private String planId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        jobStatusesReport = adminToolPage.getPage(JobStatusesReportPage.class);
        final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        final AddSubscriptionPlanPage addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);

        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

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
        itemId1 = item1.getId();

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null,
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")", SupportLevel.BASIC, null,
            true);

        // Add Feature Entitlement
        final String eosDate =
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE, 1);
        final String eolRenewalDate =
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE, 2);
        final String eolImmediateDate =
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE, 3);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        planId = subscriptionPlanDetailPage.getId();
        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

    }

    /**
     * Cleanup Jobs before running each Test, otherwise the 3 runs limit will impact the automation test
     */
    @BeforeMethod
    public void deleteJobsAndWipsForToday() {

        DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.DELETE_WORK_IN_PROGRESS_WITH_START_DATE,
                PelicanConstants.ENTITLEMENTS_END_DATE_WORK_IN_PROGRESS_OBJECT_TYPE,
                DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT) + " %"),
            getEnvironmentVariables());
        DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.DELETE_JOB_STATUSES_WITH_START_DATE,
                PelicanConstants.ENTITLEMENTS_END_DATE_JOB_CATEGORY,
                DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT) + " %"),
            getEnvironmentVariables());
    }

    /**
     * This is a test method which will test the job statuses report with Job Category and job state
     */
    @Test(dataProvider = "JobState")
    public void testEntitlementEndDateJobStatusesReportWithJobState(final String state) {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory(PelicanConstants.ENTITLEMENTS_END_DATE_REACHED_REPORT);
        jobStatusesReport.selectJobState(state);
        final GenericGrid jobStatusesReportGrid = jobStatusesReport.clickOnSubmit();

        final List<String> jobCategoryList = jobStatusesReportGrid.getColumnValues("Category");
        if (jobCategoryList.size() > 0) {
            for (final String jobCategory : jobCategoryList) {
                AssertCollector.assertThat("Incorrect job category value of a report", jobCategory,
                    equalTo(PelicanConstants.ENTITLEMENTS_END_DATE_REACHED_REPORT), assertionErrorList);
            }
        }

        final List<String> jobStateList = jobStatusesReportGrid.getColumnValues("State");
        if (jobStateList.size() > 0) {
            for (final String jobState : jobStateList) {
                AssertCollector.assertThat("Incorrect job state value of a report", jobState, equalTo(state),
                    assertionErrorList);
            }
        }

        final List<String> startDateList = jobStatusesReportGrid.getColumnValues("Created Date");
        if (startDateList.size() > 0) {
            for (final String startDate : startDateList) {
                AssertCollector.assertThat("Incorrect created start date value of a report", startDate, notNullValue(),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the error message with invalid start date and end date
     */
    @Test
    public void testJobStatusesReportErrorMessageWithInvalidStartDateAndEndDate() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory(PelicanConstants.ENTITLEMENTS_END_DATE_REACHED_REPORT);
        jobStatusesReport.selectJobState("COMPLETE_WITH_FAILURES");
        jobStatusesReport.fillStartDate("abcd");
        jobStatusesReport.fillEndDate("abcd");
        jobStatusesReport.submit(TimeConstants.ONE_SEC);
        final String errorMessage = jobStatusesReport.getErrorMessageForField();
        AssertCollector.assertThat("Incorrect error message of the date field", errorMessage,
            equalTo("Start Date must be in 'MM/dd/yyyy' format."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the error message with start date filled and end
     */
    @Test
    public void testJobStatusesReportErrorMessageWithStartDateFilledAndEndDateEmpty() {
        jobStatusesReport.navigateToJobStatusesReportPage();
        jobStatusesReport.selectJobCategory(PelicanConstants.ENTITLEMENTS_END_DATE_REACHED_REPORT);
        jobStatusesReport.selectJobState("COMPLETE_WITH_FAILURES");
        jobStatusesReport.fillStartDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7));
        jobStatusesReport.submit(TimeConstants.ONE_SEC);
        final String errorMessage = jobStatusesReport.getErrorMessageForField();
        AssertCollector.assertThat("Incorrect error message of the date field", errorMessage,
            equalTo("End Date cannot be blank."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate success Job run for Entitlements end date Job, along with rerun of job same day
     */
    @Test
    public void testSuccessEntitlementEndDateJobStatusReportAndRetry() {

        final String entitlementIdPlanItem1 =
            DbUtils.getEntitlementIdFromItemId(planId, itemId1, getEnvironmentVariables());

        final String todayDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT);
        DbUtils.updateRemoveFeatureDates(todayDate, null, null, entitlementIdPlanItem1, getEnvironmentVariables());

        // Run Entitlements end date Job
        jobsResource.entitlementEndDate();

        jobStatusesReport.navigateToJobStatusesReportPage();
        // need to change this ca
        jobStatusesReport.selectJobCategory(JobCategory.ENTITLEMENTS_END_DATE_REACHED.toString());
        jobStatusesReport.selectJobState(Status.COMPLETE.getDisplayName());
        jobStatusesReport.fillStartDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));
        jobStatusesReport.fillEndDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));
        final GenericGrid jobStatusReportGrid = jobStatusesReport.clickOnSubmit();

        List<String> jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdFirstRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        List<Map<String, String>> wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdFirstRun);

        validateJobDetails(jobGuidList, jobStatusReportGrid, wipRecords);

        // Run Entitlements end date Job 2nd time
        jobsResource.entitlementEndDate();

        jobStatusesReport.refreshPage();

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdSecondRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdSecondRun);

        AssertCollector.assertTrue(
            "2nd Run: WIP are processed When 1st ENTITLEMENTS_END_DATE_REACHED run passed with " + "no failures",
            wipRecords.size() == 0, assertionErrorList);
        AssertCollector.assertThat("Incorrect Job State",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0), equalTo(Status.COMPLETE.toString()),
            assertionErrorList);

        // Run Entitlements end date Job 3rd time
        jobsResource.entitlementEndDate();

        jobStatusesReport.refreshPage();

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdThirdRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdThirdRun);

        AssertCollector.assertTrue("3rd Run: WIP are processed When 1st and 2nd ENTITLEMENTS_END_DATE_REACHED "
            + "run passed with no failures", wipRecords.size() == 0, assertionErrorList);
        AssertCollector.assertThat("Incorrect Job State",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0), equalTo(Status.COMPLETE.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate rerun of Entitlements end date Job when Job has Partial Failures
     */
    @Test
    public void testPartialFailuresInFailedJob() {

        final String entitlementIdPlanItem1 =
            DbUtils.getEntitlementIdFromItemId(planId, itemId1, getEnvironmentVariables());

        final String todayDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT);
        DbUtils.updateRemoveFeatureDates(todayDate, null, null, entitlementIdPlanItem1, getEnvironmentVariables());

        // Run Entitlements end date Job, 1st run
        jobsResource.entitlementEndDate();

        GenericGrid jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        List<String> jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdFirstRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        List<Map<String, String>> wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdFirstRun);

        AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run", wipRecords.size() > 0,
            assertionErrorList);

        DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.UPDATE_JOB_STATUSES_WITH_ID, 2, jobIdFirstRun), getEnvironmentVariables());
        final int totalFailedWIPs = DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.UPDATE_WORK_IN_PROGRESS_WITH_OBJECT_ID, 4, jobIdFirstRun, planId),
            getEnvironmentVariables());

        // Run Entitlements end date Job, 2nd time
        jobsResource.entitlementEndDate();

        jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdSecondRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdSecondRun);

        validateJobDetails(jobGuidList, jobStatusReportGrid, wipRecords);

        AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run",
            wipRecords.size() == totalFailedWIPs, assertionErrorList);

        // Run Entitlements end date Job 3rd time
        jobsResource.entitlementEndDate();

        jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdThirdRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdThirdRun);

        AssertCollector.assertTrue("3rd Run: WIP are processed When 1st and 2nd ENTITLEMENTS_END_DATE_REACHED "
            + "run passed with no failures", wipRecords.size() == 0, assertionErrorList);

        AssertCollector.assertThat("Incorrect Job State for 3rd run",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0), equalTo(Status.COMPLETE.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate rerun of Entitlements end date Job when Job has Failed with no WIPs
     */
    @Test
    public void testFailedJobWithNoWipsRetry() {

        final String entitlementIdPlanItem1 =
            DbUtils.getEntitlementIdFromItemId(planId, itemId1, getEnvironmentVariables());

        final String todayDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT);
        DbUtils.updateRemoveFeatureDates(todayDate, null, null, entitlementIdPlanItem1, getEnvironmentVariables());

        // Run Entitlements end date Job, 1st run
        jobsResource.entitlementEndDate();

        GenericGrid jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        List<String> jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdFirstRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        List<Map<String, String>> wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdFirstRun);

        AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run", wipRecords.size() > 0,
            assertionErrorList);

        DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.UPDATE_JOB_STATUSES_WITH_ID, 2, jobIdFirstRun), getEnvironmentVariables());
        final int totalDeletedWIPs = DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.DELETE_WORK_IN_PROGRESS_WITH_JOB_ID, jobIdFirstRun),
            getEnvironmentVariables());

        // Run Entitlements end date Job, 2nd time
        jobsResource.entitlementEndDate();

        jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdSecondRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdSecondRun);

        validateJobDetails(jobGuidList, jobStatusReportGrid, wipRecords);

        AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run",
            wipRecords.size() == totalDeletedWIPs, assertionErrorList);

        // Run Entitlements end date Job 3rd time
        jobsResource.entitlementEndDate();

        jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdThirdRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdThirdRun);

        AssertCollector.assertTrue("3rd Run: WIP are processed When 1st and 2nd ENTITLEMENTS_END_DATE_REACHED "
            + "run passed with no failures", wipRecords.size() == 0, assertionErrorList);
        AssertCollector.assertThat("Incorrect Job State for 3rd run",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0), equalTo(Status.COMPLETE.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate In Progress Job run for Entitlements end date Job
     */
    @Test
    public void testHungEntitlementEndDateJobRetry() {

        final String entitlementIdPlanItem1 =
            DbUtils.getEntitlementIdFromItemId(planId, itemId1, getEnvironmentVariables());

        final String todayDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT);
        DbUtils.updateRemoveFeatureDates(todayDate, null, null, entitlementIdPlanItem1, getEnvironmentVariables());

        // Run Entitlements end date Job, 1st run
        jobsResource.entitlementEndDate();

        GenericGrid jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        List<String> jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdFirstRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        List<Map<String, String>> wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdFirstRun);

        final int totalWIPs = wipRecords.size();

        AssertCollector.assertThat("Incorrect Job State",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0),
            equalTo(Status.COMPLETE.toString().toUpperCase()), assertionErrorList);

        AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run", wipRecords.size() > 0,
            assertionErrorList);

        DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.UPDATE_JOB_STATUSES_WITH_ID, 0, jobIdFirstRun), getEnvironmentVariables());
        DbUtils.insertOrUpdateQueryFromWorkerDb(
            String.format(PelicanDbConstants.UPDATE_WORK_IN_PROGRESS_WITH_OBJECT_ID, 0, jobIdFirstRun, planId),
            getEnvironmentVariables());

        // Run Entitlements end date Job, 2nd time
        jobsResource.entitlementEndDate();

        jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdSecondRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdSecondRun);

        AssertCollector.assertThat("Incorrect Job State for 1st run",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(1),
            equalTo(Status.COMPLETE_WITH_FAILURES.toString().toUpperCase()), assertionErrorList);

        AssertCollector.assertThat("Incorrect Job State for 2nd run",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0),
            equalTo(Status.COMPLETE.toString().toUpperCase()), assertionErrorList);

        validateJobDetails(jobGuidList, jobStatusReportGrid, wipRecords);

        AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run", wipRecords.size() == totalWIPs,
            assertionErrorList);

        // Run Entitlements end date Job 3rd time
        jobsResource.entitlementEndDate();

        jobStatusReportGrid = getEntitlementEndDateJobStatusReportGrid(null,
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        jobGuidList = jobStatusReportGrid.getColumnValues(PelicanConstants.JOB_GUID);
        final String jobIdThirdRun = DbUtils.getIdFromGuidInJobStatuses(getEnvironmentVariables(), jobGuidList.get(0));
        wipRecords = DbUtils.getRecordsFromWip(getEnvironmentVariables(), jobIdThirdRun);

        AssertCollector.assertThat("Incorrect Job State for 1st run",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(2),
            equalTo(Status.COMPLETE_WITH_FAILURES.toString().toUpperCase()), assertionErrorList);

        AssertCollector.assertThat("Incorrect Job State for 2nd run",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(1),
            equalTo(Status.COMPLETE.toString().toUpperCase()), assertionErrorList);

        AssertCollector.assertTrue("3rd Run: WIP are processed When 1st and 2nd ENTITLEMENTS_END_DATE_REACHED "
            + "run passed with no failures", wipRecords.size() == 0, assertionErrorList);
        AssertCollector.assertThat("Incorrect Job State",
            jobStatusReportGrid.getColumnValues(PelicanConstants.STATE).get(0), equalTo(Status.COMPLETE.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider which provides the different job states for the job status report in the admin tool
     *
     * @return two dimensional object array containing job statuses
     */
    @DataProvider(name = "JobState")
    public Object[][] getDifferentJobStates() {
        return new Object[][] { { "IN_PROGRESS" }, { "COMPLETE" }, { "COMPLETE_WITH_FAILURES" } };
    }

    /**
     * Validate job details
     *
     * @param jobGuidList
     * @param jobStatusReportGrid
     * @param wipRecords
     */
    public void validateJobDetails(final List<String> jobGuidList, final GenericGrid jobStatusReportGrid,
        final List<Map<String, String>> wipRecords) {

        if (jobGuidList.size() > 0) {
            if (jobGuidList.get(0).equals("None found")) {
                AssertCollector.assertFalse("Expected a Completed Job, but none found",
                    jobGuidList.get(0).equals("None found"), assertionErrorList);

            } else {

                for (final String jobGuid : jobGuidList) {
                    AssertCollector.assertThat("Job GUID should not be Null", jobGuid, notNullValue(),
                        assertionErrorList);
                }
                final List<String> categoryList = jobStatusReportGrid.getColumnValues(PelicanConstants.CATEGORY);
                for (final String category : categoryList) {
                    AssertCollector.assertThat("Incorrect Category", category,
                        equalTo(JobCategory.ENTITLEMENTS_END_DATE_REACHED.toString()), assertionErrorList);
                }
                // final List<String> stateList = jobStatusReportGrid.getColumnValues(PelicanConstants.STATE);
                // for (final String state : stateList) {
                // AssertCollector.assertThat("Incorrect Job State", state, equalTo(Status.COMPLETE.toString()),
                // assertionErrorList);
                // }
                final List<String> createDateList = jobStatusReportGrid.getColumnValues(PelicanConstants.CREATED_DATE);
                for (final String createDate : createDateList) {
                    AssertCollector.assertThat("Incorrect Job Created Date", createDate.split(" ")[0],
                        equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                        assertionErrorList);
                }
                final List<String> lastModifiedDateList =
                    jobStatusReportGrid.getColumnValues(PelicanConstants.LAST_MODIFIED_DATE_FIELD);

                for (final String lastModifiedDate : lastModifiedDateList) {
                    AssertCollector.assertThat("Incorrect Jobs Last Modified Date", lastModifiedDate.split(" ")[0],
                        equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                        assertionErrorList);
                }

                AssertCollector.assertTrue("No WIP for ENTITLEMENTS_END_DATE_REACHED job run", wipRecords.size() > 0,
                    assertionErrorList);
            }
        } else {
            AssertCollector.assertTrue("Report has no Data to validate.", jobGuidList.size() > 0, assertionErrorList);
        }
    }

    /**
     * Method to get job report page
     *
     * @param status
     * @param startDate
     * @param endDate
     * @return
     */
    private GenericGrid getEntitlementEndDateJobStatusReportGrid(final Status status, final String startDate,
        final String endDate) {

        jobStatusesReport.navigateToJobStatusesReportPage();

        jobStatusesReport.selectJobCategory(JobCategory.ENTITLEMENTS_END_DATE_REACHED.toString());
        if (status != null) {
            jobStatusesReport.selectJobState(status.getDisplayName());
        }
        jobStatusesReport.fillStartDate(startDate);
        jobStatusesReport.fillEndDate(endDate);

        return jobStatusesReport.clickOnSubmit();
    }

}
