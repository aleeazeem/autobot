package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
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
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Test class to verify Subscription Expiration Reminder job.
 *
 * @author jains
 *
 */

public class SubscriptionExpirationReminderTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private WorkInProgressReportPage workInProgressReportPage;
    private WorkInProgressReportResultPage workInProgressReportResultPage;
    private static final String userExternalKey = "Automation_test_ExpirationReminder";
    private PurchaseOrderUtils purchaseOrderUtils;
    private BuyerUser buyerUser;
    private SubscriptionDetailPage subscriptionDetailPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private EditSubscriptionPage editSubscriptionPage;
    private JobsClient jobsResource;
    private static final String expirationDate = DateTimeUtils.getNowPlusDays(31);
    private static final String objectId = userExternalKey + "_" + DateTimeUtils.changeDateFormat(expirationDate,
        PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_FORMAT_WITH_MONTH_NAME);

    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionExpirationReminderTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        workInProgressReportPage = adminToolPage.getPage(WorkInProgressReportPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        editSubscriptionPage = adminToolPage.getPage(EditSubscriptionPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

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
    }

    /**
     * Test method to verify that yearly subscriptions in cancelled state are picked up by the expiration reminder job.
     *
     * @param priceId
     * @param isMeta
     * @param expirationDate
     */
    @Test(dataProvider = "priceIds")
    public void testExpirationReminderJobPicksUpEligibleSubscription(final String priceId, final boolean isMeta) {
        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(priceId, isMeta);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        AssertCollector.assertThat(
            "Subscription activity is not recorded for Cancellation of subscription " + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getActivity(), equalTo(PelicanConstants.CANCEL),
            assertionErrorList);
        final String jobGuid = runExpirationReminderJob();
        // Refresh page after running the job
        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat(
            "Subscription activity is not recorded for subscription expiration reminder job  for subscription id "
                + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getActivity(),
            equalTo(PelicanConstants.EXPIRATION_REMINDER), assertionErrorList);

        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, objectId);
        AssertCollector.assertThat("Wip should had been created for subscription id: " + subscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(1), assertionErrorList);

        final WorkInProgressSubscriptionDetailsPage workInProgressSubscriptionDetailsPage =
            workInProgressReportResultPage.clickOnObjectId(objectId);

        AssertCollector.assertThat("There should be only 1 subscription id",
            workInProgressSubscriptionDetailsPage.getSubscriptions().split(" ").length, equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Subscription id on wip details page is not correct on wip details page",
            workInProgressSubscriptionDetailsPage.getSubscriptions().split(" ")[0], equalTo(subscriptionId),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to verify that expiration reminder job consolidates eligible subscription for same user and same
     * expiration date in single wip. Separate wip is created fro another user.
     */
    @Test
    public void testExpirationReminderJobConsolidatesSubscription() {
        // Get 3 subscriptions with user1, 2 cancelled and 1 active
        final String subscriptionId1CancelledWithUser1 =
            submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        final String subscriptionId2CancelledWithUser1 =
            submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        final String subscriptionId3ActiveWithUser1 =
            submitPurchaseOrderAndGetSubscriptionId(getBic2YearsUkPriceId(), false);

        editSubscriptionStatusAndExpirationDate(subscriptionId1CancelledWithUser1, expirationDate,
            SubscriptionStatus.CANCELLED);
        editSubscriptionStatusAndExpirationDate(subscriptionId2CancelledWithUser1, expirationDate,
            SubscriptionStatus.CANCELLED);
        editSubscriptionStatusAndExpirationDate(subscriptionId3ActiveWithUser1, expirationDate,
            SubscriptionStatus.ACTIVE);

        // get 1 subscription with user2
        final String purchaseOrderIdWithUser2 = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBic2YearsUsPriceId(), 1)), null, true, true, buyerUser);
        final PurchaseOrder purchaseOrderWithUser2 = resource.purchaseOrder().getById(purchaseOrderIdWithUser2);
        final String subscriptionIdWithUser2 = purchaseOrderWithUser2.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id " + subscriptionIdWithUser2);
        editSubscriptionStatusAndExpirationDate(subscriptionIdWithUser2, expirationDate, SubscriptionStatus.CANCELLED);

        // run expiration reminder job
        final String jobGuid = runExpirationReminderJob();
        // There should be 2 or more wips with the same jobGuid while not providing object id
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, null);
        AssertCollector.assertThat("There should be more than 2 wips", workInProgressReportResultPage.getTotalItems(),
            greaterThanOrEqualTo(2), assertionErrorList);

        // verify all 2 subscriptions for user1 wip
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, objectId);
        AssertCollector.assertThat("There should be only 1 wip", workInProgressReportResultPage.getTotalItems(),
            equalTo(1), assertionErrorList);
        final WorkInProgressSubscriptionDetailsPage workInProgressSubscriptionDetailsPage =
            workInProgressReportResultPage.clickOnObjectId(objectId);
        // There should be only 2 subscriptions inside the wip
        AssertCollector.assertThat("There should be 2 subscription ids",
            workInProgressSubscriptionDetailsPage.getSubscriptions().split(" ").length, equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Subscription id on wip details page are not correct",
            Arrays.asList(workInProgressSubscriptionDetailsPage.getSubscriptions().split(" ")),
            hasItems(subscriptionId1CancelledWithUser1, subscriptionId2CancelledWithUser1), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to verify that a subscription is not picked up again by expiration reminder job if there is already
     * EXPIRATION_REMINDER_SENT activity recorded for the same subscription.
     */
    @Test
    public void testExpirationReminderJobDoesNotPickSubscriptionTwice() {
        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        String jobGuid = runExpirationReminderJob();
        // Refresh page after running the job
        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat(
            "Subscription activity is not recorded for subscription expiration reminder job  for subscription id "
                + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getActivity(),
            equalTo(PelicanConstants.EXPIRATION_REMINDER), assertionErrorList);
        // run expiration reminder job again
        jobGuid = runExpirationReminderJob();

        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, objectId);
        AssertCollector.assertThat("There should not be any wip created with job guid: " + jobGuid,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that monthly commercial subscriptions are not picked up by expiration reminder job.
     */
    @Test
    public void testExpirationReminderJobDoesNotPickMonthlySubscription() {
        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(getBicMonthlyUsPriceId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        final String jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify annual subscriptions are not picked up by expiration reminder job if annual subscription has
     * expiration date less than 30 days.
     *
     * @param isLessThanThirtyDays
     */
    @Test(dataProvider = "expirationDateLessThanThirtyDays")
    public void testExpirationReminderActivityAndWip(final boolean isLessThanThirtyDays) {

        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        final DateTime nextBillingDate = DateTimeUtils.getNowPlusDaysAsUTC(PelicanConstants.AUDIT_LOG_DATE_FORMAT, 30);
        final DateTimeFormatter format = DateTimeFormat.forPattern(PelicanConstants.AUDIT_LOG_DATE_FORMAT);
        String nextBillingDateString = null;
        if (isLessThanThirtyDays) {
            nextBillingDateString = nextBillingDate.minusMinutes(2).toString(format);
        } else {
            // need additional time for next billing date because cancelled date should be bigger than time of
            // expiration reminder job then subscription will be picked up
            nextBillingDateString = nextBillingDate.plusMinutes(2).toString(format);
        }
        // set next billing date in db
        final String sqlQuery = "update subscription set NEXT_BILLING_DATE = '%s' where id = '%s' ";
        final String updateSqlQuery = String.format(sqlQuery, nextBillingDateString + ".000000", subscriptionId);

        DbUtils.updateQuery(updateSqlQuery, getEnvironmentVariables());
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        final String jobGuid = runExpirationReminderJob();
        subscriptionDetailPage.refreshPage();
        final String objectId = userExternalKey + "_" + DateTimeUtils.changeDateFormat(nextBillingDateString,
            PelicanConstants.AUDIT_LOG_DATE_FORMAT, PelicanConstants.DATE_FORMAT_WITH_MONTH_NAME);
        if (isLessThanThirtyDays) {
            commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);
        } else {
            AssertCollector.assertThat("Expiration reminder activity is recorded for subscription id " + subscriptionId,
                subscriptionDetailPage.getLastSubscriptionActivity().getActivity(),
                equalTo(PelicanConstants.EXPIRATION_REMINDER), assertionErrorList);
            workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, objectId);
            AssertCollector.assertThat("Wip is not created with job guid: " + jobGuid,
                workInProgressReportResultPage.getTotalItems(), equalTo(1), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "expirationDateLessThanThirtyDays")
    public Object[][] getExpirationDateLessThanThirtyDays() {
        return new Object[][] { { true }, { false } };
    }

    /**
     * Method to verify that subscriptions are not picked up by expiration reminder job if send expiration emails field
     * in offering is set to false.
     */
    @Test
    public void testExpirationReminderJobDoesNotPickUpSubscriptionWithExpirationEmailFlagOff() {
        // create a subscription plan and set expiration email value to false
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(bicOffering.getOfferings().get(0).getId());
        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSendExpirationEmails(false);
        editSubscriptionPlanPage.clickOnSave(false);
        // submit purchase order with above offering and get subscription id
        final String subscriptionId =
            submitPurchaseOrderAndGetSubscriptionId(bicOffering.getIncluded().getPrices().get(0).getId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        final String jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that non-commercial subscriptions are not picked up by expiration reminder job.
     */
    @Test
    public void testExpirationReminderJobDoesNotPickUpNonCommericalSubscription() {
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.NCM);

        final String subscriptionId =
            submitPurchaseOrderAndGetSubscriptionId(bicOffering.getIncluded().getPrices().get(0).getId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        final String jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that subscriptions are not picked up if expiration date is greater than today + 30 days.
     */
    @Test
    public void testExpirationReminderJobDoesNotPickUpCommericalSubscriptionWhenExpirationDateIsMoreThan30Days() {
        final String expirationDate = DateTimeUtils.getNowPlusDays(32);
        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        final String jobGuid = runExpirationReminderJob();
        final String objectId = userExternalKey + "_" + DateTimeUtils.changeDateFormat(expirationDate,
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_FORMAT_WITH_MONTH_NAME);
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that non cancelled subscriptions are not picked up by the reminder job.
     */
    @Test
    public void testExpirationReminderJobDoesNotPickUpSubscriptionNotInCancelledStatus() {
        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, DateTimeUtils.getNowPlusDays(30),
            SubscriptionStatus.ACTIVE);
        String jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);

        editSubscriptionStatusAndExpirationDate(subscriptionId, DateTimeUtils.getNowPlusDays(30),
            SubscriptionStatus.EXPIRED);
        jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);

        editSubscriptionStatusAndExpirationDate(subscriptionId, DateTimeUtils.getNowPlusDays(30),
            SubscriptionStatus.PENDING_MIGRATION);
        jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);

        editSubscriptionStatusAndExpirationDate(subscriptionId, DateTimeUtils.getNowPlusDays(30),
            SubscriptionStatus.DELINQUENT);
        jobGuid = runExpirationReminderJob();
        commonAssertionForSubscriptionNotPickedUpByJob(subscriptionId, jobGuid, objectId);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Subscription Expiration Reminder WIP Detail page. This method also verifies that subscription is
     * linked to subscription detail page and oxygen id is linked to user detail page.
     */
    @Test
    public void testSubscriptionExpirationReminderWipDetailPage() {
        final String subscriptionId = submitPurchaseOrderAndGetSubscriptionId(getBicYearlyUsPriceId(), false);
        editSubscriptionStatusAndExpirationDate(subscriptionId, expirationDate, SubscriptionStatus.CANCELLED);
        final String jobGuid = runExpirationReminderJob();
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, objectId);
        if (workInProgressReportResultPage.getTotalItems() > 0) {
            WorkInProgressSubscriptionDetailsPage workInProgressSubscriptionDetailsPage =
                workInProgressReportResultPage.clickOnObjectId(objectId);
            AssertCollector.assertThat("Header on wip detail page is not correct",
                workInProgressSubscriptionDetailsPage.getHeader(),
                equalTo("Subscription Expiration Reminder WIP Details"), assertionErrorList);
            AssertCollector.assertThat("Expiration date on wip detail page is not correct",
                workInProgressSubscriptionDetailsPage.getExpirationDate(), equalTo(DateTimeUtils
                    .changeDateFormat(expirationDate, PelicanConstants.DATE_FORMAT_WITH_SLASH, "MMMM-d-yyyy")),
                assertionErrorList);

            final String subscriptionIdOnWipDetailPage = workInProgressSubscriptionDetailsPage.getSubscriptions();
            final String wipDetailPageUrl = getDriver().getCurrentUrl();
            // verify subscription link to subscription detail page
            subscriptionDetailPage =
                workInProgressSubscriptionDetailsPage.clickOnSubscriptionId(subscriptionIdOnWipDetailPage);
            AssertCollector.assertThat("Subscription id on wip detail page and subscription detail page should match.",
                subscriptionDetailPage.getId(), equalTo(subscriptionIdOnWipDetailPage), assertionErrorList);
            // verify oxygen id link to user detail page
            getDriver().get(wipDetailPageUrl);
            workInProgressSubscriptionDetailsPage = adminToolPage.getPage(WorkInProgressSubscriptionDetailsPage.class);
            final String oxygenIdOnWipDetailPage = workInProgressSubscriptionDetailsPage.getOxygenId().split(" ")[0];
            final UserDetailsPage userDetailsPage =
                workInProgressSubscriptionDetailsPage.clickOnOxygenId(oxygenIdOnWipDetailPage);
            AssertCollector.assertThat("Oxygen id on wip detail page and user detail page should match.",
                userDetailsPage.getExternalKey(), equalTo(oxygenIdOnWipDetailPage), assertionErrorList);
        } else {
            Assert.fail("Wips should had been created.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testExpirationReminderJobPicksUpEligibleSubscription method.
     *
     * @return
     */
    @DataProvider(name = "priceIds")
    public Object[][] getYearlyPriceIds() {
        return new Object[][] { { getBicYearlyUsPriceId(), false }, { getMeta2YearsUkPriceId(), true } };
    }

    /**
     * Method to submit purchase order and gte subscription id.
     *
     * @param priceId
     * @param isMeta
     * @return String
     */
    private String submitPurchaseOrderAndGetSubscriptionId(final String priceId, final boolean isMeta) {
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        if (isMeta) {
            // Fulfillment request if it is meta
            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        }
        // get subscription id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id " + subscriptionId);
        return subscriptionId;
    }

    /**
     * Method to edit subscription status and expiration date.
     *
     * @param subscriptionId
     * @param expirationDate
     * @param subscriptionStatus
     */
    private void editSubscriptionStatusAndExpirationDate(final String subscriptionId, final String expirationDate,
        final SubscriptionStatus subscriptionStatus) {
        // Edit expiration date and status for subscription expiration reminder job subscriptionDetailPage =
        findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        subscriptionDetailPage =
            editSubscriptionPage.editASubscription(null, expirationDate, subscriptionStatus.toString(), null);
    }

    /**
     * Run expiration reminder job.
     *
     * @return String
     */
    private String runExpirationReminderJob() {
        // Run expiration reminder job
        final JsonApiJobStatus jsonApiJobStatus =
            jobsResource.subscriptionExpirationReminder(getEnvironmentVariables());
        final String jobGuid = jsonApiJobStatus.getId();
        LOGGER.info("subscriptionExpirationReminderJobGuid: " + jobGuid);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        return jobGuid;
    }

    /**
     * common assertions on wips and subscription when subscription is not picked up by the job.
     *
     * @param subscriptionId
     * @param jobGuid
     * @param objectId
     */
    private void commonAssertionForSubscriptionNotPickedUpByJob(final String subscriptionId, final String jobGuid,
        final String objectId) {
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        AssertCollector.assertThat(
            "Subscription activity for expiration reminder should not be recorded for subscription id "
                + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getActivity(),
            not(PelicanConstants.EXPIRATION_REMINDER), assertionErrorList);
        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, objectId);
        AssertCollector.assertThat("There should not be any wip created with job guid: " + jobGuid,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);
    }
}
