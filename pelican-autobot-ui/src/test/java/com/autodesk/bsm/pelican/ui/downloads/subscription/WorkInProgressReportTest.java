package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.RENEWAL_REMINDER_EMAIL_DAYS_YEARLLY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.JobStatusesClient;
import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.WipContext;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusesData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressSubscriptionDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.ParseException;
import org.hamcrest.core.Every;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class verifies Work in Progress Report functionality. This report can be accessed from Reports --> Job
 * Report --> Work in Progress Report from admin tool.
 *
 * @author jains
 */
public class WorkInProgressReportTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private WorkInProgressReportPage workInProgressReportPage;
    private WorkInProgressReportResultPage workInProgressReportResultPage;
    private String subscriptionRenewalJobGuid;
    private String renewalObjectId;
    private String renewalReminderObjectId;
    private String subscriptionRenewalReminderJobGuid;
    private static final String userExternalKey = "Automation_test_RenewalUserForWorkInProgressReportTest";
    private PurchaseOrderUtils purchaseOrderUtils;
    private BuyerUser buyerUser;
    private PaymentProfile paymentProfile;
    private SubscriptionDetailPage subscriptionDetailPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private EditSubscriptionPage editSubscriptionPage;
    private JobsClient jobsResource;
    private String updateSQL = "update work_in_progress " + "set created = DATE_SUB(created , INTERVAL '%s' MINUTE) "
        + "where state in (0,1) and object_type= 4 and object_id = '%s'";
    private String todayDate = DateTimeUtils.getNowMinusDays(0);

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkInProgressReportTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        workInProgressReportPage = adminToolPage.getPage(WorkInProgressReportPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        editSubscriptionPage = adminToolPage.getPage(EditSubscriptionPage.class);

        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        jobsResource = triggerResource.jobs();

        // create a bic offering
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // Create new user
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // Create buyer user
        buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(user.getExternalKey());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        paymentProfile = new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
            .addCreditCardPaymentProfile(user.getId(), PaymentProcessor.BLUESNAP_NAMER.getValue());

        // create first bic purchase order
        PurchaseOrder bicPurchaseOrder1 = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicOfferings.getIncluded().getPrices().get(0).getId(), buyerUser, 1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrder1.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, bicPurchaseOrder1.getId());
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        bicPurchaseOrder1 = resource.purchaseOrder().getById(bicPurchaseOrder1.getId());
        // get subscription id
        final String bicSubscriptionId1 = bicPurchaseOrder1.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("bicSubscriptionId1 " + bicSubscriptionId1);

        // create second bic purchase order
        PurchaseOrder bicPurchaseOrder2 = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicOfferings.getIncluded().getPrices().get(0).getId(), buyerUser, 1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrder2.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, bicPurchaseOrder2.getId());
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        bicPurchaseOrder2 = resource.purchaseOrder().getById(bicPurchaseOrder2.getId());
        // get subscription id
        final String bicSubscriptionId2 = bicPurchaseOrder2.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("bicSubscriptionId2 " + bicSubscriptionId2);

        // Edit next billing date for renewal
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId1);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, null, null, null);

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId2);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, null, null, null);

        // Run renewal job
        final JsonSubscriptionId jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(bicSubscriptionId1 + "," + bicSubscriptionId2);
        subscriptionRenewalJobGuid = jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());

        // Edit next billing date for renewal reminder
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId1);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowPlusDays(RENEWAL_REMINDER_EMAIL_DAYS_YEARLLY), null,
            null, null);

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId2);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowPlusDays(RENEWAL_REMINDER_EMAIL_DAYS_YEARLLY), null,
            null, null);
        // Run renewal reminder job
        jobsResource.renewalReminder();

        JsonApiJobStatusesData response = null;

        try {
            response = jobsStatusesResource.getJobStatuses(JobCategory.RENEWAL_REMINDERS.toString(), null, null, null);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        final List<JsonApiJobStatus> jobStatusesList = response.getData();

        if (jobStatusesList.size() > 0) {
            subscriptionRenewalReminderJobGuid = jobStatusesList.get(0).getId();
        }
        LOGGER.info("subscriptionRenewalReminderJobGuid " + subscriptionRenewalReminderJobGuid);

        // Object id for renewal job is created with userExternalKey_PriceListExternalKey_SppId_term_nextBillingDate
        renewalObjectId = userExternalKey + "_" + getPricelistExternalKeyUs() + "_" + paymentProfile.getId()
            + "_1_YEAR_" + DateTimeUtils.changeDateFormat(todayDate, PelicanConstants.DATE_FORMAT_WITH_SLASH,
                PelicanConstants.DATE_FORMAT_WITH_HYPHEN);
        LOGGER.info("Renewal objectId: " + renewalObjectId);

        // Object id for renewal reminder job is created with userExternalKey_PriceListExternalKey_SppId
        renewalReminderObjectId = userExternalKey + "_" + getPricelistExternalKeyUs() + "_" + paymentProfile.getId();
        LOGGER.info("renewalReminderObjectId: " + renewalReminderObjectId);
    }

    @BeforeMethod(alwaysRun = true)
    public void setRenewalWipCreatedTime() {
        // Setting created time for renewal wips to 4 hour before so that next renewal job can renew the same
        // subscription.
        DbUtils.insertOrUpdateQueryFromWorkerDb(String.format(updateSQL, "240", renewalObjectId),
            getEnvironmentVariables());
    }

    /*
     * This is a test method which will test the wip statuses report headers in admin tool.
     */
    @Test
    public void testWIPStatusReportHeaders() throws ParseException {
        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(subscriptionRenewalJobGuid, null, null, null);
        final List<String> workInProgressReportHeaders = workInProgressReportResultPage.getColumnHeaders();
        AssertCollector.assertThat("Title of the Page is not Correct", workInProgressReportResultPage.getTitle(),
            equalTo("Work In Progress Report"), assertionErrorList);
        AssertCollector.assertThat("Total number of columns are not correct", workInProgressReportHeaders.size(),
            equalTo(8), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", workInProgressReportHeaders.get(0),
            equalTo(PelicanConstants.JOB_GUID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", workInProgressReportHeaders.get(1),
            equalTo(PelicanConstants.WIP_GUID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", workInProgressReportHeaders.get(2),
            equalTo(PelicanConstants.OBJECT_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", workInProgressReportHeaders.get(3),
            equalTo(PelicanConstants.OBJECT_ID_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", workInProgressReportHeaders.get(4),
            equalTo(PelicanConstants.STATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", workInProgressReportHeaders.get(5),
            equalTo(PelicanConstants.CREATED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", workInProgressReportHeaders.get(6),
            equalTo(PelicanConstants.LAST_MODIFIED_DATE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", workInProgressReportHeaders.get(7),
            equalTo(PelicanConstants.ERRORS_FIELD), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies all columns of work in progress report.
     *
     * @param jobGuid
     * @param wipState
     * @param objectType
     * @param objectId
     */
    @Test(dataProvider = "testDataForWIPStatusReportWithDifferntFilters")
    public void testWIPStatusReportWithDifferentFilters(final String jobGuid, final String wipState,
        final String objectType, final String objectId) {
        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, wipState, objectType, objectId);

        if (workInProgressReportResultPage.getTotalItems() > 0) {
            if (jobGuid != null) {
                AssertCollector.assertThat("Job GUID is not correct. ",
                    workInProgressReportResultPage.getColumnValuesOfJobGuid(), Every.everyItem(equalTo(jobGuid)),
                    assertionErrorList);
            }

            if (wipState != null) {
                AssertCollector.assertThat("Wip State is not correct. ",
                    workInProgressReportResultPage.getColumnValuesOfState(), Every.everyItem(equalTo(wipState)),
                    assertionErrorList);
            }

            if (objectType != null) {
                AssertCollector.assertThat("Object type is not correct. ",
                    workInProgressReportResultPage.getColumnValuesOfObjectType(), Every.everyItem(equalTo(objectType)),
                    assertionErrorList);
            }

            if (objectId != null) {
                AssertCollector.assertThat("Object Id is not correct. ",
                    workInProgressReportResultPage.getColumnValuesOfObjectId(), Every.everyItem(equalTo(objectId)),
                    assertionErrorList);
            }

            final List<Date> lastModifiedDateList = DateTimeUtils.convertStringListToDateList(
                workInProgressReportResultPage.getColumnValuesOfLastModifiedDate(),
                PelicanConstants.DATE_FORMAT_WITH_SLASH);

            final List<Date> createdDateList = DateTimeUtils.convertStringListToDateList(
                workInProgressReportResultPage.getColumnValuesOfCreatedDate(), PelicanConstants.DATE_FORMAT_WITH_SLASH);

            if (lastModifiedDateList.size() == createdDateList.size()) {
                for (int i = 0; i < lastModifiedDateList.size(); i++) {
                    AssertCollector.assertThat("Last modified date should be after created date. ",
                        lastModifiedDateList.get(i), greaterThanOrEqualTo(createdDateList.get(i)), assertionErrorList);
                }
            } else {
                AssertCollector.assertThat("Last modified date list and created date list size should be equal",
                    lastModifiedDateList.size(), equalTo(createdDateList.size()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies: 1) objectID is constructed correctly on WIP Report page. 2) Details on
     * WorkInProgressSubscriptionDetailsPage for both renewal and renewal reminder jobs.
     *
     * @param jobGuid
     * @param jobCategory
     * @param expectedObjectId
     */
    @Test(dataProvider = "testDataForRenewalAndRenewalReminderJob")
    public void testObjectIdAndDetailsOnWorkInProgressSubscriptionDetailsPage(final String jobGuid,
        final String jobCategory, final String expectedObjectId) {
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, null);
        final int totalRecordsInReport = workInProgressReportResultPage.getTotalItems();

        // check if report is empty or not
        if (totalRecordsInReport > 0) {
            final List<String> objectIdList = workInProgressReportResultPage.getColumnValuesOfObjectId();
            int selectedRowIndex = 0;
            // This logic is written to pick up records from AUTO family only.
            for (int i = 0; i < objectIdList.size(); i++) {
                if (objectIdList.get(i).startsWith(userExternalKey)) {
                    selectedRowIndex = i;
                    break;
                }
            }
            final String objectIdFromResultPage = objectIdList.get(selectedRowIndex);
            final String wipGuid = workInProgressReportResultPage.getColumnValuesOfWipGuid().get(selectedRowIndex);
            LOGGER.info("object id : " + objectIdFromResultPage);
            LOGGER.info("wipGuid : " + wipGuid);

            AssertCollector.assertThat("ObjectId is not constructed correctly.", objectIdFromResultPage,
                equalTo(expectedObjectId), assertionErrorList);
            final WorkInProgressSubscriptionDetailsPage workInProgressSubscriptionDetailsPage =
                workInProgressReportResultPage.clickOnObjectId(objectIdFromResultPage);

            final String contextQuery =
                "Select context from work_in_progress_context wipc , work_in_progress wip where "
                    + "wipc.wip_id = wip.id and  wip.guid = '" + wipGuid + "'";

            final List<Map<String, String>> context =
                DbUtils.selectQueryFromWorkerDb(contextQuery, getEnvironmentVariables());

            WipContext wipContext = new WipContext();
            final ObjectMapper mapper = new ObjectMapper();

            try {
                wipContext = mapper.readValue(context.get(0).get("context"), WipContext.class);
            } catch (final IOException e) {
                LOGGER.info(e.getMessage());
                Assert.fail("Context in work_in_progress_context table can not be parsed to WipContext object.");
            }

            // verifying details on WIP subscription details page
            if (jobCategory.equals(JobCategory.SUBSCRIPTION_RENEWALS.toString())) {
                AssertCollector.assertThat("Page header on Renewal WIP details page is not correct.",
                    workInProgressSubscriptionDetailsPage.getHeader(), equalTo("Subscription Renewal WIP Details"),
                    assertionErrorList);

            } else {
                AssertCollector.assertThat("Page header on Renewal Reminder WIP details page is not correct.",
                    workInProgressSubscriptionDetailsPage.getHeader(),
                    equalTo("Subscription Renewal Reminder WIP Details"), assertionErrorList);
            }
            AssertCollector.assertThat("OxygenId on wip details page should match with DB value.",
                workInProgressSubscriptionDetailsPage.getOxygenId(),
                equalTo(wipContext.getOxygenId() + " (" + wipContext.getOwnerId() + ")"), assertionErrorList);
            AssertCollector.assertThat("Stored Payment Profile Id on wip details page should match with DB value.",
                workInProgressSubscriptionDetailsPage.getStoredPaymentProfileId(),
                equalTo(wipContext.getStoredPaymentProfileId()), assertionErrorList);
            AssertCollector.assertThat("Next Billing Date on wip details page should match with DB value.",
                workInProgressSubscriptionDetailsPage.getNextBillingDate(), equalTo(wipContext.getNextBillingDate()),
                assertionErrorList);
            AssertCollector.assertThat("Billing Period on wip details page should match with DB value.",
                workInProgressSubscriptionDetailsPage.getBillingPeriod(),
                equalTo(wipContext.getBillingPeriod().getCount() + " " + wipContext.getBillingPeriod().getType()),
                assertionErrorList);
            AssertCollector.assertThat("PriceList ExternalKey on wip details page should match with DB value.",
                workInProgressSubscriptionDetailsPage.getPriceListExternalKey(),
                equalTo(wipContext.getPriceListExternalKey()), assertionErrorList);
            AssertCollector.assertThat("Subscription(s) on wip details page should match with DB value.",
                workInProgressSubscriptionDetailsPage.getSubscriptions().split(" "),
                equalTo(wipContext.getSubscriptions()), assertionErrorList);

        } else {
            Assert.fail("Wips should have been created.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method validates that Oxygen id on WIP details page is linked to User Detail Page.
     *
     * @param jobGuid
     * @param jobCategory
     */
    @Test(dataProvider = "testDataForRenewalAndRenewalReminderJob")
    public void testOxygenIdLinkToUserDetailPage(final String jobGuid, final String jobCategory,
        final String expectedObjectId) {
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, null);
        final int totalRecordsInReport = workInProgressReportResultPage.getTotalItems();

        // check if report is empty or not
        if (totalRecordsInReport > 0) {
            final List<String> objectIdList = workInProgressReportResultPage.getColumnValuesOfObjectId();
            int selectedRowIndex = 0;
            // This logic is written to pick up records from AUTO family only.
            for (int i = 0; i < objectIdList.size(); i++) {
                if (objectIdList.get(i).startsWith(userExternalKey)) {
                    selectedRowIndex = i;
                    break;
                }
            }
            final String objectId = workInProgressReportResultPage.getColumnValuesOfObjectId().get(selectedRowIndex);

            final WorkInProgressSubscriptionDetailsPage workInProgressSubscriptionDetailsPage =
                workInProgressReportResultPage.clickOnObjectId(objectId);

            final String oxygenIdOnWipDetailPage = workInProgressSubscriptionDetailsPage.getOxygenId();
            final String userExternalKeyOnWipDetailPage = oxygenIdOnWipDetailPage.split(" ")[0];
            final String userIdOnWipDetailPage =
                oxygenIdOnWipDetailPage.split(" ")[1].replace("(", "").replace(")", "");

            final UserDetailsPage userDetailsPage =
                workInProgressSubscriptionDetailsPage.clickOnOxygenId(userExternalKeyOnWipDetailPage);

            AssertCollector.assertThat("User external key on wip detail page and user detail page should match.",
                userDetailsPage.getExternalKey(), equalTo(userExternalKeyOnWipDetailPage), assertionErrorList);
            AssertCollector.assertThat("User Id on wip detail page and user detail page should match.",
                userDetailsPage.getId(), equalTo(userIdOnWipDetailPage), assertionErrorList);
        } else {
            Assert.fail("Wips should have been created.");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method validates that subscription id on WIP details page is linked to Subscription Detail Page.
     *
     * @param jobGuid
     * @param jobCategory
     */
    @Test(dataProvider = "testDataForRenewalAndRenewalReminderJob")
    public void testSubscriptionIdLinkToSubscriptionDetailPage(final String jobGuid, final String jobCategory,
        final String expectedObjectId) {
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, null);
        final int totalRecordsInReport = workInProgressReportResultPage.getTotalItems();

        // check if report is empty or not
        if (totalRecordsInReport > 0) {
            final WorkInProgressSubscriptionDetailsPage workInProgressSubscriptionDetailsPage =
                workInProgressReportResultPage.clickOnObjectId(expectedObjectId);
            final String subscriptionIdOnWipDetailPage =
                workInProgressSubscriptionDetailsPage.getSubscriptions().split(" ")[0];
            subscriptionDetailPage =
                workInProgressSubscriptionDetailsPage.clickOnSubscriptionId(subscriptionIdOnWipDetailPage);
            AssertCollector.assertThat("User external key on wip detail page and user detail page should match.",
                subscriptionDetailPage.getId(), equalTo(subscriptionIdOnWipDetailPage), assertionErrorList);
        } else {
            Assert.fail("Wips should have been created.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies that 1) if there is a wip in IN_PRORGESS(state = 0) or SENT_TO_PAYPORT(state = 1) status
     * within last 4 hours and if the renewal job is run again then that subscription will not be picked up renewal job.
     * 2) If the wip is created more than 4 hours before and if the renewal job is run again then the subscription will
     * be picked by the job again.
     */
    @Test(dataProvider = "wipState")
    public void testRenewalJobDoesNotPickUpSameSubscriptionWhenThereIsWipInLastFourHours(final int wipState) {
        // create a purchase order
        PurchaseOrder bicPurchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicYearlyUsPriceId(), buyerUser, 1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, bicPurchaseOrder.getId());
        bicPurchaseOrder = resource.purchaseOrder().getById(bicPurchaseOrder.getId());

        // get subscription id
        final String bicSubscriptionId = bicPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        // Edit next billing date for renewal
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicSubscriptionId);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, null, null, null);

        // object id is constructed with a different date format
        final String renewalObjectId = userExternalKey + "_" + getPricelistExternalKeyUs() + "_"
            + paymentProfile.getId() + "_1_YEAR_" + DateTimeUtils.changeDateFormat(todayDate,
                PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_FORMAT_WITH_HYPHEN);
        DbUtils.insertOrUpdateQueryFromWorkerDb(String.format(updateSQL, "240", renewalObjectId),
            getEnvironmentVariables());

        // Run renewal job
        final JsonSubscriptionId jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(bicSubscriptionId);
        subscriptionRenewalJobGuid = jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(subscriptionRenewalJobGuid, null, null, null);
        int totalRecordsInReport = workInProgressReportResultPage.getTotalItems();
        AssertCollector.assertTrue(
            "Subscription should had been picked up by running the renewal job first time. JobGuid: "
                + subscriptionRenewalJobGuid + " Subscription id: " + bicSubscriptionId,
            totalRecordsInReport == 1, assertionErrorList);

        // update wip to in_progress or sent_in_payport
        final String updateSQLWithState = "update work_in_progress set state = " + wipState
            + " where object_type= 4 and state in (0,1) and object_id = '%s'";
        DbUtils.insertOrUpdateQueryFromWorkerDb(String.format(updateSQLWithState, renewalObjectId),
            getEnvironmentVariables());

        // Run renewal job second time with wip within 240 minutes
        subscriptionRenewalJobGuid = jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());
        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(subscriptionRenewalJobGuid, null, null, null);
        totalRecordsInReport = workInProgressReportResultPage.getTotalItems();
        AssertCollector.assertTrue(
            "Subscription should not had been picked up by running the renewal job second time. JobGuid: "
                + subscriptionRenewalJobGuid + " Subscription id: " + bicSubscriptionId,
            totalRecordsInReport == 0, assertionErrorList);

        // Setting created time for renewal wips to 238 minutes to check boundry condition of 240 minutes
        DbUtils.insertOrUpdateQueryFromWorkerDb(String.format(updateSQL, "238", renewalObjectId),
            getEnvironmentVariables());
        // Run renewal job third time with wip still within 240 minutes
        subscriptionRenewalJobGuid = jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());
        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(subscriptionRenewalJobGuid, null, null, null);
        totalRecordsInReport = workInProgressReportResultPage.getTotalItems();
        AssertCollector.assertTrue(
            "Subscription should not had been picked up by running the renewal job third time. JobGuid: "
                + subscriptionRenewalJobGuid + " Subscription id: " + bicSubscriptionId,
            totalRecordsInReport == 0, assertionErrorList);

        // Setting created time for renewal wips to ~240 minutes (238+5 =243 minutes) before so that next renewal job
        // can
        // pick up same subscription for renewal.
        DbUtils.insertOrUpdateQueryFromWorkerDb(String.format(updateSQL, "5", renewalObjectId),
            getEnvironmentVariables());
        // Run job again
        subscriptionRenewalJobGuid = jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());
        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(subscriptionRenewalJobGuid, null, null, null);
        totalRecordsInReport = workInProgressReportResultPage.getTotalItems();
        AssertCollector.assertTrue(
            "Subscription should  be picked up by running the renewal job fourth time. JobGuid: "
                + subscriptionRenewalJobGuid + " Subscription id: " + bicSubscriptionId,
            totalRecordsInReport == 1, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testWIPStatusReportWithDifferntFilters method.
     *
     * @return Object[][]
     */
    @DataProvider(name = "testDataForWIPStatusReportWithDifferntFilters")
    public Object[][] getDataForWIPStatusReportWithDifferentFilters() {
        // (jobGuid, wipState, objectType, objectId)
        return new Object[][] { { subscriptionRenewalJobGuid, null, null, null },
                { subscriptionRenewalReminderJobGuid, null, null, null },
                { subscriptionRenewalJobGuid, null, null, renewalObjectId } };
    }

    /**
     * Data provider with Subscription Renewal and Subscription Renewal Reminder job Guid.
     *
     * @return
     */
    @DataProvider(name = "testDataForRenewalAndRenewalReminderJob")
    public Object[][] getDataForRenewalAndRenewalReminderJob() {
        return new Object[][] {
                { subscriptionRenewalJobGuid, JobCategory.SUBSCRIPTION_RENEWALS.toString(), renewalObjectId },
                { subscriptionRenewalReminderJobGuid, JobCategory.RENEWAL_REMINDERS.toString(),
                        renewalReminderObjectId } };
    }

    /**
     * Data provider with Subscription Renewal and Subscription Renewal Reminder job Guid.
     *
     * @return
     */
    @DataProvider(name = "wipState")
    public Object[][] getDataForRenewalJobDoesNotPickUpSameSubscriptionWhenThereIsWipInLastFourHours() {
        return new Object[][] { { 0 }, { 1 } };
    }

}
