package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test class to verify subscription monitoring job.
 *
 * @author jains
 */

public class SubscriptionMonitoringJobTest extends SeleniumWebdriver {

    private JobsClient jobsResource;
    private FindSubscriptionsPage subscriptionPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private String bicActiveSubscriptionId;
    private String metaActiveSubscriptionId;
    private String bicDelinquentSubscriptionId;
    private String metaDelinquentSubscriptionId;
    private String metaActivePurchaseOrderId;
    private String bicDelinquentPurchaseOrderId;
    private String metaDelinquentPurchaseOrderId;
    private WorkInProgressReportPage workInProgressReportPage;
    private WorkInProgressReportResultPage workInProgressReportResultPage;
    private static final String MONITORING_ALERT = "MONITORING_ALERT";
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionMonitoringJobTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        final PelicanTriggerClient triggersResource = pelicanResource.trigger();
        jobsResource = triggersResource.jobs();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        workInProgressReportPage = adminToolPage.getPage(WorkInProgressReportPage.class);

        // create bic offering and bic subscription
        final Offerings bicCommercialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String bicCommercialPriceId = bicCommercialOffering.getIncluded().getPrices().get(0).getId();

        // create 2 purchase order for bic
        priceQuantityMap.put(bicCommercialPriceId, 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // get purchase order after charge
        final String bicActivePurchaseOrderId = purchaseOrder.getId();
        purchaseOrder = resource.purchaseOrder().getById(bicActivePurchaseOrderId);
        bicActiveSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("bicActiveSubscriptionId: " + bicActiveSubscriptionId);

        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // get purchase order after charge
        bicDelinquentPurchaseOrderId = purchaseOrder.getId();
        purchaseOrder = resource.purchaseOrder().getById(bicDelinquentPurchaseOrderId);
        bicDelinquentSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // set subscription status to delinquent
        editSubscription(bicDelinquentSubscriptionId, null, SubscriptionStatus.DELINQUENT);

        LOGGER.info("bicDelinquentSubscriptionId: " + bicDelinquentSubscriptionId);

        // create meta offering and meta subscription
        final Offerings metaCommercialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String metaPriceId1 = metaCommercialOffering.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(metaPriceId1, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // get purchase order after charge
        metaActivePurchaseOrderId = purchaseOrder.getId();
        // fulfill the order
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        metaActiveSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        LOGGER.info("metaActiveSubscriptionId: " + metaActiveSubscriptionId);

        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // get purchase order after charge
        metaDelinquentPurchaseOrderId = purchaseOrder.getId();
        purchaseOrder = resource.purchaseOrder().getById(metaDelinquentPurchaseOrderId);
        // fulfill the order
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        metaDelinquentSubscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        LOGGER.info("metaDelinquentSubscriptionId: " + metaDelinquentSubscriptionId);
        // set subscription status to delinquent
        editSubscription(metaDelinquentSubscriptionId, null, SubscriptionStatus.DELINQUENT);
    }

    /**
     * This test method verifies that subscription monitoring job does not pick up any active or delinquent subscription
     * if next billing date is in the past for less than 10 days.
     */
    @Test
    public void testSubscriptionMonitoringJobDoesNotPickUpNonEligibleActiveAndDelinquentSubscription() {
        // Since this is the first test to run so next billing date will be a future date so no need to update any data.
        final JsonApiJobStatus jsonApiJobStatus = jobsResource.subscriptionMonitoring();
        final String jobGuid = jsonApiJobStatus.getId();
        LOGGER.info("JobGuid " + jobGuid);
        AssertCollector.assertThat("Job category is not correct.", jsonApiJobStatus.getJobCategory().getJobCategory(),
            equalTo(JobCategory.MONITORING_JOB.getJobCategory()), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, bicActiveSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + bicActiveSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, bicDelinquentSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + bicDelinquentSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, metaActiveSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + metaActiveSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, metaDelinquentSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + metaDelinquentSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that subscription monitoring job does not pick up any active or delinquent subscription
     * if next billing date is in the past for greater than 10 days.
     */

    @Test(dependsOnMethods = { "testSubscriptionMonitoringJobDoesNotPickUpNonEligibleActiveAndDelinquentSubscription" },
        enabled = false)
    public void testSubscriptionMonitoringJobCreatesSfdcCase() {
        // edit next billing date to today-10 days or more for subscription in active and delinquent status
        editSubscription(bicActiveSubscriptionId, DateTimeUtils.getNowMinusDays(10), null);
        editSubscription(bicDelinquentSubscriptionId, DateTimeUtils.getNowMinusDays(11), null);
        editSubscription(metaActiveSubscriptionId, DateTimeUtils.getNowMinusDays(15), null);
        editSubscription(metaDelinquentSubscriptionId, DateTimeUtils.getNowMinusDays(20), null);

        // setting PURCHASE_ORDER_ID to null to verify that SFDC is created in this case as well. Defect # BIC-5648
        DbUtils.updateQuery("update subscription set PURCHASE_ORDER_ID = null where id = " + bicActiveSubscriptionId,
            getEnvironmentVariables());

        final JsonApiJobStatus jsonApiJobStatus = jobsResource.subscriptionMonitoring();
        final String jobGuid = jsonApiJobStatus.getId();
        LOGGER.info("JobGuid " + jobGuid);
        AssertCollector.assertThat("Job category is not correct.", jsonApiJobStatus.getJobCategory().getJobCategory(),
            equalTo(JobCategory.MONITORING_JOB.getJobCategory()), assertionErrorList);

        assertForSfdcCaseCreation(jobGuid, bicActiveSubscriptionId, "-");
        assertForSfdcCaseCreation(jobGuid, bicDelinquentSubscriptionId, bicDelinquentPurchaseOrderId);
        assertForSfdcCaseCreation(jobGuid, metaActiveSubscriptionId, metaActivePurchaseOrderId);
        assertForSfdcCaseCreation(jobGuid, metaDelinquentSubscriptionId, metaDelinquentPurchaseOrderId);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method verifies that subscription monitoring job does not pick up any active or delinquent subscription
     * if the subscription is processed by monitoring job in last 10 days.
     */
    @Test(dependsOnMethods = { "testSubscriptionMonitoringJobCreatesSfdcCase" }, enabled = false)
    public void testSubscriptionMonitorJobDoesNotPickUpSubscriptionThatAreProcessedInLast10Days() {
        final String updateSql =
            "Update subscription_event set Created = '%s' where type = 32 and subscription_id in (%s, %s, %s, %s)";
        DbUtils.updateQuery(String.format(updateSql,
            DateTimeUtils.getNowMinusDays(PelicanConstants.RENEWAL_DATE_FORMAT, 9), bicActiveSubscriptionId,
            bicDelinquentSubscriptionId, metaActiveSubscriptionId, metaDelinquentSubscriptionId),
            getEnvironmentVariables());

        final JsonApiJobStatus jsonApiJobStatus = jobsResource.subscriptionMonitoring();
        final String jobGuid = jsonApiJobStatus.getId();
        LOGGER.info("JobGuid " + jobGuid);
        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, bicActiveSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + bicActiveSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, bicDelinquentSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + bicDelinquentSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, metaActiveSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + metaActiveSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, metaDelinquentSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + metaDelinquentSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method verifies that subscription monitoring job picks up any active or delinquent subscription if the
     * subscription is processed by monitoring job in 10 or more days ago.
     */
    @Test(dependsOnMethods = { "testSubscriptionMonitorJobDoesNotPickUpSubscriptionThatAreProcessedInLast10Days" },
        enabled = false)
    public void testSubscriptionMonitorJobPickUpSubscrtionThatAreProcessed10DaysAgo() {

        final String updateSql =
            "Update subscription_event set Created = '%s' where type = 32 and subscription_id = %s";
        DbUtils.updateQuery(String.format(updateSql,
            DateTimeUtils.getNowMinusDays(PelicanConstants.RENEWAL_DATE_FORMAT, 10), bicActiveSubscriptionId),
            getEnvironmentVariables());

        DbUtils.updateQuery(String.format(updateSql,
            DateTimeUtils.getNowMinusDays(PelicanConstants.RENEWAL_DATE_FORMAT, 11), bicDelinquentSubscriptionId),
            getEnvironmentVariables());

        DbUtils.updateQuery(String.format(updateSql,
            DateTimeUtils.getNowMinusDays(PelicanConstants.RENEWAL_DATE_FORMAT, 12), metaActiveSubscriptionId),
            getEnvironmentVariables());

        DbUtils.updateQuery(String.format(updateSql,
            DateTimeUtils.getNowMinusDays(PelicanConstants.RENEWAL_DATE_FORMAT, 13), metaDelinquentSubscriptionId),
            getEnvironmentVariables());

        final JsonApiJobStatus jsonApiJobStatus = jobsResource.subscriptionMonitoring();
        final String jobGuid = jsonApiJobStatus.getId();
        LOGGER.info("JobGuid " + jobGuid);

        assertForSfdcCaseCreation(jobGuid, bicActiveSubscriptionId, "-");
        assertForSfdcCaseCreation(jobGuid, bicDelinquentSubscriptionId, bicDelinquentPurchaseOrderId);
        assertForSfdcCaseCreation(jobGuid, metaActiveSubscriptionId, metaActivePurchaseOrderId);
        assertForSfdcCaseCreation(jobGuid, metaDelinquentSubscriptionId, metaDelinquentPurchaseOrderId);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that subscription monitoring job does not pick up any expire subscription even if next
     * billing date is in the past for more than 10 days.
     */

    @Test(dependsOnMethods = { "testSubscriptionMonitorJobPickUpSubscrtionThatAreProcessed10DaysAgo" }, enabled = false)
    public void testSubscriptionMonitorJobDoesNotPickUpSubscrtiontInExpireStatus() {
        editSubscription(bicActiveSubscriptionId, DateTimeUtils.getNowMinusDays(10), SubscriptionStatus.EXPIRED);
        editSubscription(metaDelinquentSubscriptionId, DateTimeUtils.getNowMinusDays(20), SubscriptionStatus.EXPIRED);

        final JsonApiJobStatus jsonApiJobStatus = jobsResource.subscriptionMonitoring();
        final String jobGuid = jsonApiJobStatus.getId();
        LOGGER.info("JobGuid " + jobGuid);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, bicActiveSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + bicActiveSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        workInProgressReportResultPage =
            workInProgressReportPage.generateReport(jobGuid, null, null, metaDelinquentSubscriptionId);

        AssertCollector.assertThat("There should not be any wip for subscription id: " + metaDelinquentSubscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method navigates to subscription detail page and edit the subscription next billing date and status.
     *
     * @param subscriptionId
     * @param nextBillingDate
     * @param subscriptionStatus
     */
    private void editSubscription(final String subscriptionId, final String nextBillingDate,
        final SubscriptionStatus subscriptionStatus) {
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(subscriptionId);
        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        if (subscriptionStatus != null) {
            editSubscriptionPage.editASubscription(nextBillingDate, null, subscriptionStatus.toString(), null);
        } else {
            editSubscriptionPage.editASubscription(nextBillingDate, null, null, null);
        }
    }

    /**
     * Common assert for SFDC case creation validation.
     *
     * @param jobGuid
     * @param subscriptionId
     * @param purchaseOrderId
     */
    private void assertForSfdcCaseCreation(final String jobGuid, final String subscriptionId,
        final String purchaseOrderId) {
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(subscriptionId);
        AssertCollector.assertThat(
            "Subscription activity is not recorded for monitoing alert for subscription id " + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getActivity(), equalTo(MONITORING_ALERT),
            assertionErrorList);
        final String memo = subscriptionDetailPage.getLastSubscriptionActivity().getMemo();
        AssertCollector.assertTrue(
            "Subscription memo substring end is not correct for monitoing alert for subscription id " + subscriptionId,
            memo.endsWith("Last PO id: " + purchaseOrderId), assertionErrorList);
        AssertCollector
            .assertTrue("Subscription memo substring start is not correct for monitoing alert for subscription id "
                + subscriptionId, memo.startsWith("SFDC case id:"), assertionErrorList);

        workInProgressReportResultPage = workInProgressReportPage.generateReport(jobGuid, null, null, subscriptionId);
        AssertCollector.assertThat("Wip should had been created for subscription id: " + subscriptionId,
            workInProgressReportResultPage.getTotalItems(), equalTo(1), assertionErrorList);
    }
}
