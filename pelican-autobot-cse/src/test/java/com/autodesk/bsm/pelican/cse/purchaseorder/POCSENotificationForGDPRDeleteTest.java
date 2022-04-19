package com.autodesk.bsm.pelican.cse.purchaseorder;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Class to validate there is no CSE events while processing PO for GDPR Delete User.
 *
 * @author t_joshv
 *
 */
public class POCSENotificationForGDPRDeleteTest extends SeleniumWebdriver {

    private User user;
    private BuyerUser buyerUser;
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

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        resource = pelicanResource.platform();
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        // Create GDPR Deleted Buyer User.
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

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
        eventsList.clear();
    }

    /**
     * Verify no CSE Events sent out for GDPR Delete User on PO CHARGED, FULFILLED AND INVOICE NUMBER GENERATION.
     */
    @Test
    public void testSuccessNoCseEventsForPoChargedForGDPRDeleteUser() {

        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, buyerUser.getId()),
            getEnvironmentVariables());
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getMetaMonthlyUsPriceId(), buyerUser, 1);
        final String metaPurchaseOrderIDForChangeNotification = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, metaPurchaseOrderIDForChangeNotification);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, metaPurchaseOrderIDForChangeNotification);
        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            metaPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, OrderState.CHARGED, user, true,
            assertionErrorList);

        AssertCollector.assertFalse("ChangeNotification for Purchase Order : "
            + metaPurchaseOrderIDForChangeNotification + ", Found in eventList", isNotificationFound,
            assertionErrorList);

        purchaseOrder = resource.purchaseOrder().getById(metaPurchaseOrderIDForChangeNotification);
        // fulfill the request, since it is Meta and check CSE Events.
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        pelicanEventsConsumer.waitForEvents(30000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithoutContextForPurchaseOrder(eventsList,
            metaPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, true, assertionErrorList);

        AssertCollector.assertFalse("ChangeNotification for Purchase Order : "
            + metaPurchaseOrderIDForChangeNotification + ", Found in eventList", isNotificationFound,
            assertionErrorList);

        // running the invoice job to check CSE events for invoice number generation.
        final PelicanTriggerClient triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        final JobsClient jobsResource = triggerResource.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource,
            metaPurchaseOrderIDForChangeNotification);

        pelicanEventsConsumer.waitForEvents(30000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithoutContextForPurchaseOrder(eventsList,
            metaPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, true, assertionErrorList);

        AssertCollector.assertFalse("ChangeNotification for Purchase Order : "
            + metaPurchaseOrderIDForChangeNotification + ", Found in eventList", isNotificationFound,
            assertionErrorList);

        // Process the PO for CHARGE BACK and check CSE events.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, metaPurchaseOrderIDForChangeNotification);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            metaPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, OrderState.CHARGED_BACK, user, true,
            assertionErrorList);

        AssertCollector.assertFalse("ChangeNotification for Purchase Order : "
            + metaPurchaseOrderIDForChangeNotification + ", Found in eventList", isNotificationFound,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test CSE change notification for Refund Order scenario for GDPR Delete User.
     */
    @Test
    public void testSuccessNoCseEventsOnPoRefundForGDPRDeleteUser() {
        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, buyerUser.getId()),
            getEnvironmentVariables());
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, 1);
        final String metaPurchaseOrderIDForChangeNotification = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, metaPurchaseOrderIDForChangeNotification);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, metaPurchaseOrderIDForChangeNotification);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, metaPurchaseOrderIDForChangeNotification);
        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList,
            metaPurchaseOrderIDForChangeNotification, PelicanConstants.UPDATED, OrderState.REFUNDED, user, true,
            assertionErrorList);

        AssertCollector.assertFalse("ChangeNotification for Purchase Order : "
            + metaPurchaseOrderIDForChangeNotification + ", Found in eventList", isNotificationFound,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
