package com.autodesk.bsm.pelican.cse.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.cse.HelperForAssertions;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.BootstrapEntityType;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationChannel;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationEntity;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationFilterType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationRequester;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapChangeNotificationsPage;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapEventStatusPage;
import com.autodesk.bsm.pelican.ui.pages.events.RepublishEventsPage;
import com.autodesk.bsm.pelican.ui.pages.events.RepublishJobsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class should contain all tests related to AUM subscriptions and subscriptions Republish tests as well as boot
 * strap tests
 *
 * @author Shweta Hegde
 */
public class RepublishAndBootstrapSubscriptionTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private RepublishEventsPage eventsPage;
    private BootstrapChangeNotificationsPage bootstrapChangeNotificationsPage;
    private CSEHelper cseHelper;
    private ChangeNotificationConsumer pelicanBootStrapEventsConsumer = null;
    private String pelicanBootStrapNotificationChannel;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        cseHelper = new CSEHelper(resource);
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();
        RepublishEventsPage.checkAndClearRepublishHungJobs(getEnvironmentVariables());

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        final String pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
        eventsPage = adminToolPage.getPage(RepublishEventsPage.class);
        bootstrapChangeNotificationsPage = adminToolPage.getPage(BootstrapChangeNotificationsPage.class);

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Initialize Consumer
        pelicanBootStrapNotificationChannel = getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();
        pelicanBootStrapEventsConsumer =
            cseHelper.initializeConsumer(brokerUrl, pelicanBootStrapNotificationChannel, accessToken);
    }

    /**
     * Test GDPR Delete Flagged User's AUM Subscriptions should not be republished and boot strapped 1. Verify that the
     * Subscription is not returned in "Find Matching Entities" 2. Verify that the Subscription is not included in
     * Processed Record count 3. Verify that the subscription is not included in Boot strap
     */
    @Test
    public void testGDPRDeleteUsersAumSubscriptionsAreNotRepublished() {

        // Create PO for svc_p_pelican, GDPR Delete flag should be 0
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        final String subscriptionId1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        final String subscriptionId2 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a user for GDPR delete flag value 1
        final Map<String, String> userParams = new HashMap<>();
        final String userExternalKey = RandomStringUtils.randomAlphanumeric(9);
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User gdprDeleteUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        final BuyerUser gdprBuyerUser = new BuyerUser();
        gdprBuyerUser.setId(gdprDeleteUser.getId());
        gdprBuyerUser.setExternalKey(gdprDeleteUser.getExternalKey());
        gdprBuyerUser.setEmail("abc.pxrs@xyz.com");

        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD,
            getBicYearlyUkPriceId(), gdprBuyerUser, 3);
        final String purchaseOrderId2 = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId2);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId2);

        final String subscriptionId3 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Update the GDPR delete flag to 1
        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, gdprDeleteUser.getId()),
            getEnvironmentVariables());

        final List<String> subscriptionIdList = Arrays.asList(subscriptionId1, subscriptionId2, subscriptionId3);

        // Republish the POs through Admin Tool
        eventsPage.navigateToEventsPublishPage();
        eventsPage.selectEntity(RepublishChangeNotificationEntity.AUM_SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Incorrect total count of AUM subscriptions eligible for republish",
            eventsPage.getTotalCount(), is(2), assertionErrorList);
        final RepublishJobsPage republishJobsPage = eventsPage.clickRepublishButton();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        eventsPage.refreshPage();

        final String jobId = republishJobsPage.getId();
        // Verify that only 2 subscriptions are republished out of 3 Subscriptions sent in the request
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + jobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("2"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + jobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + jobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.AUM_SUBSCRIPTION_CHANGE_NOTIFICATION_REPUBLISH_JOB.getJobCategory()),
            assertionErrorList);

        // Bootstrap
        final BootstrapEventStatusPage bootstrapEventStatusPage =
            bootstrapChangeNotificationsPage.publishBootStrapEntities(BootstrapEntityType.SUBSCRIPTION.getDisplayName(),
                PelicanConstants.ID_RANGE, PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, subscriptionId1,
                subscriptionId3, null, null, RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        bootstrapEventStatusPage.refreshPage();
        final String bootstrapJobId = bootstrapEventStatusPage.getId();
        // Verify that only 2 subscriptions are boot strapped out of 3 Subscriptions sent in the request
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + bootstrapJobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("2"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + bootstrapJobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + bootstrapJobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGE_NOTIFICATION_BOOTSTRAP_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Flagged User's Subscriptions should not be republished. 1. Verify that the Subscription is not
     * returned in "Find Matching Entities" 2. Verify that the Subscription is not included in Processed Record count
     */
    @Test
    public void testGDPRDeleteUsersSubscriptionsAreNotRepublished() {

        // Create PO for svc_p_pelican, GDPR Delete flag should be 0
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a user for GDPR delete flag value 1
        final Map<String, String> userParams = new HashMap<>();
        final String userExternalKey = RandomStringUtils.randomAlphanumeric(9);
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User gdprDeleteUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        final Offerings subscriptionPlan =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        final String subscriptionOfferExternalKey =
            subscriptionPlan.getIncluded().getBillingPlans().get(0).getExternalKey();

        final String subscriptionId2 =
            resource.subscription().add(userExternalKey, subscriptionOfferExternalKey, Currency.USD).getId();

        final String subscriptionId3 =
            resource.subscription().add(userExternalKey, subscriptionOfferExternalKey, Currency.USD).getId();

        // Update the GDPR delete flag to 1
        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, gdprDeleteUser.getId()),
            getEnvironmentVariables());

        final List<String> subscriptionIdList = Arrays.asList(subscriptionId1, subscriptionId2, subscriptionId3);

        // Republish the POs through Admin Tool
        eventsPage.navigateToEventsPublishPage();
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Incorrect total count of subscriptions eligible for republish",
            eventsPage.getTotalCount(), is(1), assertionErrorList);
        final RepublishJobsPage republishJobsPage = eventsPage.clickRepublishButton();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        eventsPage.refreshPage();

        final String jobId = republishJobsPage.getId();
        // Verify that only 1 subscription is republished out of 3 Subscriptions sent in the request
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + jobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("1"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + jobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + jobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the cse bootstrap from the admin tool with date range as filter
     */
    @Test
    public void testBootStrapWithDateRangeFilter() {

        final BootstrapEventStatusPage bootstrapEventStatusPage = bootstrapChangeNotificationsPage
            .publishBootStrapEntities(BootstrapEntityType.SUBSCRIPTION.getDisplayName(), PelicanConstants.DATE_RANGE,
                PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, null, null, DateTimeUtils.getToday(),
                DateTimeUtils.getToday(), RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        pelicanBootStrapEventsConsumer.waitForEvents(30000);

        final List<ChangeNotificationMessage> eventsList = pelicanBootStrapEventsConsumer.getNotifications();

        bootstrapEventStatusPage.refreshPage();

        HelperForAssertions.assertionsForBootstrap(bootstrapEventStatusPage,
            PelicanConstants.SUBSCRIPTION_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB, eventsList.size(), assertionErrorList);
        HelperForAssertions.commonAssertionsOnChangeNotifications(eventsList,
            BootstrapEntityType.SUBSCRIPTION.getDisplayName(), assertionErrorList);
        cseHelper.terminateConsumer(pelicanBootStrapEventsConsumer, pelicanBootStrapNotificationChannel, eventsList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
