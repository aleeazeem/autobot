package com.autodesk.bsm.pelican.cse.purchaseorder;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to verify purchase order change notification been sent for Purchase Order Submit and while Processing
 * Order for Authorized, Pending, Charged, Refund, Declined and ChargedBack
 *
 * @author t_joshv
 *
 */
public class CSENotificationForPurchaseOrderTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private ChangeNotificationConsumer pelicanEventsBootstrapConsumer = null;
    private String pelicanEventsNotificationChannel;
    private String pelicanEventsBootstrapNotificationChannel;
    private PurchaseOrderUtils purchaseOrderUtils;
    private PurchaseOrder purchaseOrder;
    private boolean isNotificationFound;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private boolean isCseHeadersFeatureFlagChanged;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(CSENotificationForPurchaseOrderTest.class.getSimpleName());
    private User user;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        resource = pelicanResource.platform();
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsBootstrapNotificationChannel =
            getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();
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
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
        pelicanEventsBootstrapConsumer =
            cseHelper.initializeConsumer(brokerUrl, pelicanEventsBootstrapNotificationChannel, accessToken);

        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isCseHeadersFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, true);

        final Map<String, String> userParams = new HashMap<>();
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final String userExternalKey =
            "AutoUser" + RandomStringUtils.randomNumeric(4) + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // build buyerUser object to submit a PO
        buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(userExternalKey);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
        cseHelper.terminateConsumer(pelicanEventsBootstrapConsumer, pelicanEventsBootstrapNotificationChannel,
            eventsList);

        if (isCseHeadersFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, false);
        }
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        LOGGER.info("$$$$$$$$$$$$$$$$$$$$    COMPLETED    $$$$$$$$$$$");
        pelicanEventsConsumer.clearNotificationsList();
        pelicanEventsBootstrapConsumer.clearNotificationsList();
        eventsList.clear();
    }

    /**
     * Test CSE change notification for Purchase Order submitted scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForSubmitted() {

        // place an order to create the notification in Authorized state
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, 1);
        final String bicPurchaseOrderIDForChangeNotification = purchaseOrder.getId();

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            bicPurchaseOrderIDForChangeNotification, PelicanConstants.CREATED, OrderState.SUBMITTED, user, false,
            assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Purchase Order : " + bicPurchaseOrderIDForChangeNotification
            + ", is Not Found in eventList", isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test CSE change notification for Purchase Order Authorized scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForAuthorized() {

        // place an order to create the notification in Authorized state
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, 1);
        final String bicPurchaseOrderIDForChangeNotification = purchaseOrder.getId();

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            bicPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, OrderState.AUTHORIZED, user, false,
            assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Purchase Order : " + bicPurchaseOrderIDForChangeNotification
            + ", is Not Found in eventList", isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test CSE change notification for Purchase Order Pending scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForPending() {

        // place an order to create the notification in Authorized state
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, 1);
        final String bicPurchaseOrderIDForChangeNotification = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrderIDForChangeNotification);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            bicPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, OrderState.PENDING, user, false,
            assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Purchase Order : " + bicPurchaseOrderIDForChangeNotification
            + ", is Not Found in eventList", isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test CSE change notification for Purchase Order Decline scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForDecline() {

        // place an order to create the notification in Authorized state
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, 1);
        final String bicPurchaseOrderIDForChangeNotification = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrderIDForChangeNotification);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, bicPurchaseOrderIDForChangeNotification);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            bicPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, OrderState.DECLINED, user, false,
            assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Purchase Order : " + bicPurchaseOrderIDForChangeNotification
            + ", is Not Found in eventList", isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test CSE change notification for Charge Order Authorized scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForCharge() {
        // place an order to create the notification in Authorized state

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            purchaseOrderId, PelicanConstants.UPDATED, OrderState.CHARGED, user, false, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Purchase Order : " + purchaseOrderId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test CSE change notification for ChargeBacked Order scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForChargeBacked() {

        // place an order to create the notification in charge back state
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            purchaseOrderId, PelicanConstants.UPDATED, OrderState.CHARGED_BACK, user, false, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Purchase Order : " + purchaseOrderId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test CSE change notification for Refund Order scenario for BIC Credit Card
     */
    @Test
    public void testBICPurchaseOrderChangeNotificationEventForRefund() {
        // place an order to create the notification in Refunded state
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            purchaseOrderId, PelicanConstants.UPDATED, OrderState.REFUNDED, user, false, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Purchase Order : " + purchaseOrderId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test CSE change notification for Purchase Order Cancelled scenario for Meta Credit Card
     */
    @Test
    public void testMetaPurchaseOrderChangeNotificationEventForCancelled() {
        // place an order to create the notification in Authorized state
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getMetaMonthlyUsPriceId(), buyerUser, 1);
        final String purchaseOrderId = purchaseOrder.getId();

        pelicanEventsConsumer.clearNotificationsList();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CANCEL, purchaseOrderId);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            purchaseOrderId, PelicanConstants.UPDATED, OrderState.SUBMITTED, user, false, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Purchase Order : " + purchaseOrderId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /***
     * Test CSE change notification sent for Purchase Order Fulfillment.
     */
    @Test
    public void testMetaPurchaseOrderChangeNotificationEventForFulfillment() {
        // Submit a purchase order with Credit card and process it to charged
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getMetaMonthlyUsPriceId(), buyerUser, 1);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        pelicanEventsConsumer.clearNotificationsList();

        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        pelicanEventsConsumer.waitForEvents(30000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithoutContextForPurchaseOrder(eventsList,
            purchaseOrderId, PelicanConstants.UPDATED, false, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Purchase Order : " + purchaseOrderId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * Test CSE change notification sent for Purchase Order Invoice Number Generation.
     */
    @Test
    public void testPurchaseOrderChangeNotificationEventForInvoice() {
        // Submit a purchase order with Credit card and process it to charged
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUkPriceId(), buyerUser, 1);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        pelicanEventsConsumer.clearNotificationsList();

        // final running the final invoice job
        final PelicanTriggerClient triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        final JobsClient jobsResource = triggerResource.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrderId);

        pelicanEventsConsumer.waitForEvents(30000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithoutContextForPurchaseOrder(eventsList,
            purchaseOrderId, PelicanConstants.UPDATED, false, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Purchase Order : " + purchaseOrderId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
