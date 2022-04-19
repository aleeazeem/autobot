package com.autodesk.bsm.pelican.cse.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.CSEClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.cse.ChangeNotifications;
import com.autodesk.bsm.pelican.cse.ChangeNotificationsHeader;
import com.autodesk.bsm.pelican.cse.HelperForAssertions;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.BootstrapEntityType;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationChannel;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationEntity;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationFilterType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationRequester;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class tests Republishing of updated purchase orders with Category and GDPR delete related tests as well
 * Bootstraping Purchase Order events
 *
 * @author Shweta Hegde
 */
public class RepublishAndBootstrapPurchaseOrderTest extends SeleniumWebdriver {

    private ChangeNotificationConsumer pelicanEventsConsumer;
    private PurchaseOrderUtils purchaseOrderUtils;
    private RepublishEventsPage eventsPage;
    private RepublishJobsPage publishJobPage;
    private boolean isCseHeadersFeatureFlagChanged;
    private BootstrapChangeNotificationsPage bootstrapChangeNotificationsPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private CSEHelper cseHelper;
    private String pelicanBootStrapNotificationChannel;
    private String brokerUrl;
    private String accessToken;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        cseHelper = new CSEHelper(resource);
        brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();
        RepublishEventsPage.checkAndClearRepublishHungJobs(getEnvironmentVariables());

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        accessToken = authClient.getAuthToken();

        final String pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
        eventsPage = adminToolPage.getPage(RepublishEventsPage.class);
        publishJobPage = adminToolPage.getPage(RepublishJobsPage.class);
        bootstrapChangeNotificationsPage = adminToolPage.getPage(BootstrapChangeNotificationsPage.class);

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isCseHeadersFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, true);

        // Initialize Consumer
        pelicanBootStrapNotificationChannel = getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {

        if (isCseHeadersFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, false);
        }
    }

    /**
     * Test purchase order with pelican-context is republishes successfully
     */
    @Test
    public void testRepublishUpdatedPurchaseOrderWithCategory() {

        // Create 2 PO in CHARGED states
        HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String purchaseOrderId1 = purchaseOrder.getId();

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 2);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String purchaseOrderId2 = purchaseOrder.getId();

        final List<String> purchaseOrderIdList = Arrays.asList(purchaseOrderId1, purchaseOrderId2);

        // Republish the POs through Admin Tool
        eventsPage.navigateToEventsPublishPage();
        eventsPage.selectEntity(RepublishChangeNotificationEntity.PURCHASEORDER.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(purchaseOrderIdList);
        eventsPage.clickFindMatchingEntities();
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        final List<ChangeNotificationMessage> eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Incorrect events list size", eventsList.size(),
            greaterThanOrEqualTo(purchaseOrderIdList.size()), assertionErrorList);

        for (final ChangeNotificationMessage event : eventsList) {

            final ChangeNotifications cseEvent = CSEClient.parseCSENotificationResponse(event.getData());
            if (cseEvent.getData().getAttributes().getSubject()
                .equalsIgnoreCase(RepublishChangeNotificationEntity.PURCHASEORDER.getEntityCSEName())
                && purchaseOrderIdList.contains(cseEvent.getData().getRelationships()
                    .getChangeNotificationPurchaseOrder().getLink().getData().getId())) {

                final ChangeNotificationsHeader cseEventHeader =
                    CSEClient.parseCSENotificationHeaderResponse(event.getHeader());

                if (cseEventHeader.getcategory().equals("pelican-change.notifications-purchaseorder-updated")) {
                    try {
                        if (cseEventHeader.getRepublishRequester().equals("pelican")) {
                            AssertCollector.assertTrue(
                                "Events should be republished for PO : " + purchaseOrderId1 + " and "
                                    + purchaseOrderId2,
                                cseEventHeader.getRepublishFlag().equalsIgnoreCase("true"), assertionErrorList);
                        }
                    } catch (final Exception e) {

                    }
                }
            }
        }

        AssertCollector.assertThat("Status is not Completed", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(purchaseOrderIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.PURCHASE_ORDER_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR Delete Flagged User's PO should not be republished or bootstraped 1. Verify that the PO is not returned
     * in "Find Matching Entities" 2. Verify that the PO is not included in Processed Record count 3. Verify that
     * bootstraping should not publish PO event
     */
    @Test
    public void testGDPRDeleteUsersPurchaseOrdersAreNotRepublished() {

        // Create PO for svc_p_pelican, GDPR Delete flag should be 0
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String purchaseOrderId1 = purchaseOrder.getId();

        // Create a user for GDPR delete flag value 1
        final Map<String, String> userParams = new HashMap<>();
        final String userExternalKey = RandomStringUtils.randomAlphanumeric(8);
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User gdprDeleteUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        final BuyerUser gdprBuyerUser = new BuyerUser();
        gdprBuyerUser.setId(gdprDeleteUser.getId());
        gdprBuyerUser.setExternalKey(gdprDeleteUser.getExternalKey());
        gdprBuyerUser.setEmail("abc@xyz.com");

        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD,
            getBicYearlyUkPriceId(), gdprBuyerUser, 3);
        final String purchaseOrderId2 = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId2);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId2);

        // Update the GDPR delete flag to 1
        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, gdprDeleteUser.getId()),
            getEnvironmentVariables());

        final List<String> purchaseOrderIdList = Arrays.asList(purchaseOrderId1, purchaseOrderId2);

        // Republish the POs through Admin Tool
        eventsPage.navigateToEventsPublishPage();
        eventsPage.selectEntity(RepublishChangeNotificationEntity.PURCHASEORDER.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(purchaseOrderIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Incorrect total count of purchase orders eligible for republish",
            eventsPage.getTotalCount(), is(1), assertionErrorList);
        final RepublishJobsPage republishJobsPage = eventsPage.clickRepublishButton();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        eventsPage.refreshPage();

        final String republishJobId = republishJobsPage.getId();
        // Verify that only 1 PO is republished out of 2 POs sent in the request
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + republishJobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("1"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + republishJobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + republishJobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.PURCHASE_ORDER_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);

        // Bootstrap
        final BootstrapEventStatusPage bootstrapEventStatusPage = bootstrapChangeNotificationsPage
            .publishBootStrapEntities(BootstrapEntityType.PURCHASE_ORDER.getDisplayName(), PelicanConstants.ID_RANGE,
                PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, purchaseOrderId1, purchaseOrderId2, null, null,
                RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        bootstrapEventStatusPage.refreshPage();
        final String bootstrapJobId = bootstrapEventStatusPage.getId();
        // Verify that only 1 PO is boot strapped out of 2 POs sent in the request
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + bootstrapJobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("1"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + bootstrapJobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + bootstrapJobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.PURCHASE_ORDER_CHANGE_NOTIFICATION_BOOTSTRAP_JOB.getJobCategory()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the cse bootstrap from the admin tool with date range as filter Note:
     * Disabled by Shweta on 08/02/18, because this test is fragile. Even though bootstrap is kicked for
     * "purchase_order", at that time there might be 100 other notifications are published to CSE. And when we do
     * assertion that all events are "purchase_order" events, they fail for obvious reason. Not deleting the test, since
     * it can be used to run in local on need basis.
     */
    @Test(enabled = false)
    public void testBootStrapWithDateRangeFilter() {

        final ChangeNotificationConsumer pelicanBootStrapEventsConsumer =
            cseHelper.initializeConsumer(brokerUrl, pelicanBootStrapNotificationChannel, accessToken);

        final BootstrapEventStatusPage bootstrapEventStatusPage = bootstrapChangeNotificationsPage
            .publishBootStrapEntities(BootstrapEntityType.PURCHASE_ORDER.getDisplayName(), PelicanConstants.DATE_RANGE,
                PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, null, null, DateTimeUtils.getToday(),
                DateTimeUtils.getToday(), RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        pelicanBootStrapEventsConsumer.waitForEvents(30000);

        final List<ChangeNotificationMessage> eventsList = pelicanBootStrapEventsConsumer.getNotifications();

        bootstrapEventStatusPage.refreshPage();

        HelperForAssertions.assertionsForBootstrap(bootstrapEventStatusPage,
            PelicanConstants.PURCHASE_ORDER_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB, eventsList.size(), assertionErrorList);
        HelperForAssertions.commonAssertionsOnChangeNotifications(eventsList,
            BootstrapEntityType.PURCHASE_ORDER.getDisplayName(), assertionErrorList);
        cseHelper.terminateConsumer(pelicanBootStrapEventsConsumer, pelicanBootStrapNotificationChannel, eventsList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
