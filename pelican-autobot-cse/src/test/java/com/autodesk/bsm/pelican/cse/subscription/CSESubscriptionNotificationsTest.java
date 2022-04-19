package com.autodesk.bsm.pelican.cse.subscription;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.CancelSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test Case : CSE notifications being published in CSE channel for different types as following 1. changeNotifications
 * 2. subscriptionChangeNotification
 *
 * @author t_mohag
 */
public class CSESubscriptionNotificationsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PelicanTriggerClient triggersResource;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private ChangeNotificationConsumer pelicanEventsBootstrapConsumer = null;
    private String pelicanEventsNotificationChannel;
    private String pelicanEventsBootstrapNotificationChannel;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String purchaseOrderId;
    private PurchaseOrder purchaseOrder;
    private Offerings commercialBicOfferings;
    private Offerings trialBicOfferings;
    private Offerings metaOfferings;
    private Offerings nonCommercialBicOfferings;
    private String priceIdForComBic;
    private String priceIdForTrialBic;
    private String priceIdForMeta;
    private String priceIdForNonCommBic;
    private String subscriptionIdForComBicCreditCard;
    private String subscriptionIdForComBicCreditCardToCancelInAPI;
    private String subscriptionIdForComBicCreditCardToCancelInAdminTool;
    private String subscriptionIdForNonCommBic;
    private List<ChangeNotificationMessage> eventsList;
    private Subscription subscription;
    private CSEHelper cseHelper;
    private SubscriptionOffering subscriptionOffering;
    private JProductLine productLine;
    private FindSubscriptionsPage findSubscriptionsPage;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(CSESubscriptionNotificationsTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        resource = pelicanResource.platform();
        triggersResource = pelicanResource.trigger();
        initializeDriver(getEnvironmentVariables());
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);

        // Create offerings for commercial, trial BIC and Meta subscription
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        commercialBicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        trialBicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        metaOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        nonCommercialBicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.NCM);

        // Creating price id for commercial, trial BIC and Meta
        priceIdForComBic = commercialBicOfferings.getIncluded().getPrices().get(0).getId();
        priceIdForTrialBic = trialBicOfferings.getIncluded().getPrices().get(0).getId();
        priceIdForMeta = metaOfferings.getIncluded().getPrices().get(0).getId();
        priceIdForNonCommBic = nonCommercialBicOfferings.getIncluded().getPrices().get(0).getId();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String BROKER_URL = getEnvironmentVariables().getBrokerUrl();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsBootstrapNotificationChannel =
            getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();
        final String NOTIFICATION_CONS_KEY = getEnvironmentVariables().getNotificationConsKey();
        final String NOTIFICATION_CONS_SECRET = getEnvironmentVariables().getNotificationConsSecret();
        final String AUTH_URL = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(AUTH_URL);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, NOTIFICATION_CONS_KEY, NOTIFICATION_CONS_SECRET);
        final String ACCESS_TOKEN = authClient.getAuthToken();

        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsConsumer =
            cseHelper.initializeConsumer(BROKER_URL, pelicanEventsNotificationChannel, ACCESS_TOKEN);
        pelicanEventsBootstrapConsumer =
            cseHelper.initializeConsumer(BROKER_URL, pelicanEventsBootstrapNotificationChannel, ACCESS_TOKEN);

    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
        cseHelper.terminateConsumer(pelicanEventsBootstrapConsumer, pelicanEventsBootstrapNotificationChannel,
            eventsList);
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
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired through "Cancel Subscription" API
     */
    @Test(priority = 16)
    public void testCreationAndExpirationThroughCancelSubscriptionAPIForBicCommercialSubscriptionInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        subscriptionIdForComBicCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);
        // expire the subscription with 'Cancel Subscription' API
        resource.subscription().cancelSubscription(subscriptionIdForComBicCreditCard,
            CancellationPolicy.IMMEDIATE_NO_REFUND);
        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();

            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);
            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired through Admin Tool in subscription page
     */
    @Test(priority = 17)
    public void testCreationAndExpirationThroughAdminToolForBicCommercialSubscriptionInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        subscriptionIdForComBicCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);

        // expire the subscription in Admin Tool
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForComBicCreditCard);
        final CancelSubscriptionPage cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.IMMEDIATE_NO_REFUND, null);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method cancels a subscription with GDPR cancel option and verifies CSE and subscription change notification.
     */
    @Test
    public void testCreationAndGDPRCancelThroughAdminToolForBicCommercialSubscriptionInNotificationEvent() {

        // place an order to create the notification for BIC commercial subscription creation
        subscriptionIdForComBicCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());

        // expire the subscription in Admin Tool
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForComBicCreditCard);
        final CancelSubscriptionPage cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY, null);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired when Purchase Order is DECLINED
     */
    @Test(priority = 14)
    public void testCreationOfBICCommercialSubscriptionAndExpirationWhenNewAcquisitionOrderIsDeclinedInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        final int quantity = 1;
        // submit a Purchase Order with AUTHORIZE, process to PENDING and then
        // to DECLINE
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForComBic,
            getBuyerUser(), quantity);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        // Get the subscription id
        subscriptionIdForComBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(10000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            // cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
            // subscriptionIdForComBicCreditCard, PelicanConstants.CREATED);
            // cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
            // subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED);

        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Add Seats Decline Order generates commercial subscription events in CSE channel. here add seats
     * increase the quantity within the same subscription.
     */
    @Test
    public void testCommercialSubscriptionEventForAddSeatsDecline() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForComBic, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdForBicCommercial);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForComBic);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", getBuyerUser());

        final String purchaserOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();

        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaserOrderIdForAddedSeats);

        // process the purchase order to 'Decline' state
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaserOrderIdForAddedSeats);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForBicCommercial);

        if (subscription.getStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

            pelicanEventsConsumer.waitForEvents(10000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForBicCommercial, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForBicCommercial, true, assertionErrorList);
        } else {
            Assert.fail("Subscription is not Active to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify Add Seats Decline Order generates commercial subscription events in CSE channel. here add seats
     * creates new subscription.
     */
    @Test
    public void testCommercialSubscriptionEventForAddSeatsDeclineOnAddedSubscription() {

        PurchaseOrder newAcquisitionOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForComBic, getBuyerUser(), 5);

        final String newAcquisitionOrderId = newAcquisitionOrder.getId();

        // process it for pending
        newAcquisitionOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, newAcquisitionOrderId);

        // get subscription Id
        final String subscriptionIdNewAcquisition = newAcquisitionOrder.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionIdNewAcquisition);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForComBic);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", getBuyerUser());

        final String purchaserOrderIdForAddedSeats = purchaseOrderForAddedSeats.getId();

        // process the purchase order to 'pending' state
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaserOrderIdForAddedSeats);

        // process the purchase order to 'Decline' state
        purchaseOrderForAddedSeats =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaserOrderIdForAddedSeats);

        // get Added Seats subscription Id
        final String subscriptionIdAddSeats = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdAddSeats);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(10000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdAddSeats, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdAddSeats, true, assertionErrorList);
        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Subscription Extension CHARGED Order generates subscription change notification and change
     * notification in CSE channel.
     */
    @Test
    public void testCommercialSubscriptionEventForExtensionFulfilledOrder() {

        // Create a purchase order and get subscription id
        String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForComBic, 4)), null, true, true, null);

        final String subscriptionId1 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.PAYPAL,
            new HashMap<>(ImmutableMap.of(priceIdForMeta, 4)), null, true, true, null);

        final String subscriptionId2 = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Adding all subscription ids to a set to validate subscription ids in Submit PO response
        final Set<String> subscriptionIdsSet = new HashSet<>();
        subscriptionIdsSet.add(subscriptionId1);
        subscriptionIdsSet.add(subscriptionId2);

        final HashMap<String, Offerings> subscriptionOfferingMap = new HashMap<>();
        subscriptionOfferingMap.put(subscriptionId1, commercialBicOfferings);
        subscriptionOfferingMap.put(subscriptionId2, metaOfferings);

        // Prepare request for Subscription Extension Request, with subscription id, price id and target renewal date.
        final Map<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(subscriptionId1, subscriptionId2)));
        subscriptionMap.put(LineItemParams.PRICE_ID.getValue(),
            new ArrayList<>(ImmutableList.of(priceIdForComBic, priceIdForMeta)));
        subscriptionMap.put(LineItemParams.TARGET_RENEWAL_DATE.getValue(),
            new ArrayList<>(
                ImmutableList.of(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 8),
                    DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_FORMAT_WITH_SLASH, 8))));

        // Submit Subscription Extension Purchase Order
        final PurchaseOrder purchaseOrder =
            (PurchaseOrder) purchaseOrderUtils.submitSubscriptionExtensionPurchaseOrder(subscriptionMap,
                PaymentType.PAYPAL, PurchaseOrder.OrderCommand.AUTHORIZE, new ArrayList<>(ImmutableList.of("10", "11")),
                null);

        // Processing for PENDING Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        // Processing for CHARGE Order State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        for (final String subscriptionId : subscriptionOfferingMap.keySet()) {

            subscription = resource.subscription().getById(subscriptionId);

            if (subscription.getStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

                pelicanEventsConsumer.waitForEvents(10000);
                eventsList = pelicanEventsConsumer.getNotifications();
                AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

                // set subscription offering object
                subscriptionOffering.setId(subscriptionOfferingMap.get(subscriptionId).getOfferings().get(0).getId());
                productLine.setCode(subscriptionOfferingMap.get(subscriptionId).getOfferings().get(0).getProductLine());
                subscriptionOffering.setJProductLine(productLine);
                if (subscription.getOfferingType().equals(OfferingType.BIC_SUBSCRIPTION.toString())) {
                    cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                        cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                            subscriptionId, getUser(), subscriptionOffering, true, assertionErrorList),
                        PelicanConstants.CREATE_AND_UPDATE, subscriptionId, true, assertionErrorList);
                } else {
                    cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                        cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                            subscriptionId, getUser(), subscriptionOffering, false, assertionErrorList),
                        PelicanConstants.CREATE_AND_UPDATE, subscriptionId, false, assertionErrorList);
                }
            } else {
                Assert.fail("Subscription is not active to do CSE event validation");
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired when NEW ACQUISITION Purchase Order is
     * 'Marked As Refunded'
     */
    @Test(priority = 13)
    public void testCreationOfBICCommercialSubscriptionAndExpirationWhenNewAcquisitionPOIsMarkedAsRefundedInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        final int quantity = 1;
        // submit a Purchase Order with AUTHORIZE, process to PENDING and then
        // to CHARGE and mark as refunded
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForComBic,
            getBuyerUser(), quantity);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        subscriptionIdForComBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);

        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);

        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired when AUTO RENEWAL Purchase Order is 'Marked
     * As Refunded'
     */
    @Test(priority = 11)
    public void testCreationOfBICCommercialSubscriptionAndExpirationWhenAutoRenewalPOIsMarkedAsRefundedInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        // submit a Purchase Order with AUTHORIZE, process to PENDING and then
        // to CHARGE and mark as refunded
        subscriptionIdForComBicCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdForComBicCreditCard);

        // Submit a renewal request
        final PurchaseOrder renewalPurchaseOrderForBicCreditCard = purchaseOrderUtils
            .submitAndProcessRenewalPurchaseOrder(null, subscriptionIds, false, PaymentType.CREDIT_CARD, null, true);

        final String renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrderForBicCreditCard.getId();
        // getting purchase order page with charge command
        findPurchaseOrdersPage.findPurchaseOrderById(renewalPurchaseOrderIdForBicCreditCard);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        purchaseOrderDetailPage.clickMarkAsRefunded();
        purchaseOrderDetailPage.addRefundNotesAndClickConfirmationForSuccess(PelicanConstants.MARK_AS_REFUND);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired through subscription expiration triggers
     * job
     */
    @Test(priority = 9)
    public void testCreationOfBICCommercialSubscriptionAndExpirationFromSubscriptionExpirationJobInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        // submit a Purchase Order with AUTHORIZE, process to PENDING and then
        // to CHARGE
        subscriptionIdForComBicCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);

        // Change the expiration billing date of the subscription in Admin Tool
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForComBicCreditCard);

        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowMinusDays(2), DateTimeUtils.getNowMinusDays(2),
            Status.EDIT_SUBSCRIPTION_STATUS_CANCELLED.getDisplayName(), null);

        final JsonSubscriptionId jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(subscriptionIdForComBicCreditCard);
        // Running the subscription expiration job
        final JobsClient jobsResource = triggersResource.jobs();
        jobsResource.subscriptionExpirationForSingleSubscription(jsonSubscriptionId);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not expired to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is canceled through "Cancel Subscription" API
     */
    @Test(priority = 7)
    public void testCreationAndStatusChangeOfBICCommercialSubscriptionWhenCanceledThroughApiInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        subscriptionIdForComBicCreditCardToCancelInAPI =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCardToCancelInAPI,
            notNullValue(), assertionErrorList);
        // expire the subscription with 'Cancel Subscription' API
        resource.subscription().cancelSubscription(subscriptionIdForComBicCreditCardToCancelInAPI,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCardToCancelInAPI);

        if (subscription.getStatus().equalsIgnoreCase(Status.CANCELLED.toString())) {

            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCardToCancelInAPI, getUser(), subscriptionOffering, true,
                    assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCardToCancelInAPI, true,
                assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCardToCancelInAPI, PelicanConstants.CREATED, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCardToCancelInAPI, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not Canceled to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is canceled, and canceled subscription is restarted
     * through "Restart A Canceled Subscription" API
     */
    @Test(
        dependsOnMethods = "testCreationAndStatusChangeOfBICCommercialSubscriptionWhenCanceledThroughApiInNotificationEvent",
        priority = 5)
    public void testCreationAndStatusChangeOfBICCommercialSubscriptionWhenRestartACanceledSubscriptionThroughApiInNotificationEvent() {

        resource.subscription().restartCancelledSubscription(subscriptionIdForComBicCreditCardToCancelInAPI);
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCardToCancelInAPI);

        if (subscription.getStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCardToCancelInAPI, getUser(), subscriptionOffering, true,
                    assertionErrorList),
                PelicanConstants.UPDATED, subscriptionIdForComBicCreditCardToCancelInAPI, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCardToCancelInAPI, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not RESTARTED/ACTIVE to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is canceled through Admin Tool in subscription page
     */
    @Test(priority = 15)
    public void testCreationAndStatusChangeOfBICCommercialSubscriptionWhenCanceledThroughAdminToolInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        subscriptionIdForComBicCreditCardToCancelInAdminTool =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForComBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCardToCancelInAdminTool,
            notNullValue(), assertionErrorList);

        // expire the subscription in Admin Tool
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForComBicCreditCardToCancelInAdminTool);
        final CancelSubscriptionPage cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCardToCancelInAdminTool);

        if (subscription.getStatus().equalsIgnoreCase(Status.CANCELLED.toString())) {

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCardToCancelInAdminTool, getUser(), subscriptionOffering, true,
                    assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCardToCancelInAdminTool, true,
                assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCardToCancelInAdminTool, PelicanConstants.CREATED, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCardToCancelInAdminTool, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not CANCELED to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is canceled through Admin Tool in subscription page
     * And auto renew enabled in Admin Tool
     */
    @Test(dependsOnMethods = {
            "testCreationAndStatusChangeOfBICCommercialSubscriptionWhenCanceledThroughAdminToolInNotificationEvent" },
        priority = 6)
    public void testCreationAndStatusChangeOfBICCommercialSubscriptionWhenAutoRenewEnabledInAdminToolInNotificationEvent() {

        // auto renew the subscription in Admin Tool
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForComBicCreditCardToCancelInAdminTool);
        subscriptionDetailPage.enableAutoRenew();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCardToCancelInAdminTool);

        if (subscription.getStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCardToCancelInAdminTool, getUser(), subscriptionOffering, true,
                    assertionErrorList),
                PelicanConstants.UPDATED, subscriptionIdForComBicCreditCardToCancelInAdminTool, true,
                assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCardToCancelInAdminTool, PelicanConstants.UPDATED, assertionErrorList);
        } else {
            Assert.fail("Subscription is not RESTARTED/ACTIVE to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and is expired through DELINQUENT subscription This test
     * method is doing 2 validation 1. Status change from ACTIVE to DELINQUENT 2. Status change from DELINQUENT to
     * EXPIRED
     */
    @Test(priority = 8)
    public void testCreationOfBICCommercialSubscriptionAndExpirationFromSubscriptionDelinquencyInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        final int quantity = 1;
        // submit a Purchase Order with AUTHORIZE, process to PENDING and then
        // to CHARGE
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForComBic,
            getBuyerUser(), quantity);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        subscriptionIdForComBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdForComBicCreditCard);

        // Submit a renewal request with DECLINE order command. After this
        // subscription will be in
        // Delinquent status
        PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(subscriptionIds,
            false, PaymentType.CREDIT_CARD, null, true, null);
        String renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderIdForBicCreditCard);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        // This assertions are for Subscription creation and Status change from
        // ACTIVE to DELINQUENT
        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.DELINQUENT.toString())) {

            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForComBicCreditCard, true, assertionErrorList);

            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.CREATED, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);

        } else {
            Assert.fail("Subscription is not DELINQUENT to do CSE event validation");
        }

        // Change the next billing date of the subscription in Admin Tool
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionIdForComBicCreditCard);

        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        // change the next billing date to 7 days prior
        editSubscriptionPage.editASubscription(DateTimeUtils.getNowMinusDays(8), null, null, null);

        // then DECLINE the renewal purchase order
        renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(subscriptionIds, false,
            PaymentType.CREDIT_CARD, null, true, null);

        renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderIdForBicCreditCard);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        // This assertion is for status change from DELINQUENT to EXPIRED

        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.EXPIRED.toString())) {
            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.UPDATED, subscriptionIdForComBicCreditCard, true, assertionErrorList);
        } else {
            Assert.fail("Subscription is not EXPIRED to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that for BIC Commercial Subscription events are generated in CSE channel 1.
     * changeNotification 2. subscriptionChangeNotification
     * <p>
     * It also validates for when new subscription is created and status change from DELINQUENT to ACTIVE
     */
    @Test(priority = 12)
    public void testStatusChangeOfBICCommercialSubscriptionFromDelinquentToActiveInNotificationEvent() {

        // place an order to create the notification for BIC commercial
        // subscription creation
        final int quantity = 1;
        // submit a Purchase Order with AUTHORIZE, process to PENDING and then
        // to CHARGE
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForComBic,
            getBuyerUser(), quantity);
        purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        subscriptionIdForComBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForComBicCreditCard, notNullValue(),
            assertionErrorList);
        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionIdForComBicCreditCard);

        // Submit a renewal request with DECLINE order command. After this
        // subscription will be in
        // Delinquent status
        PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(subscriptionIds,
            false, PaymentType.CREDIT_CARD, null, true, null);
        String renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderIdForBicCreditCard);

        // then CHARGE the renewal purchase order
        renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(subscriptionIds, false,
            PaymentType.CREDIT_CARD, null, true, null);

        renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPurchaseOrderIdForBicCreditCard);

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        // This assertion is for status change from DELINQUENT to ACTIVE
        // get the subscription to check status
        subscription = resource.subscription().getById(subscriptionIdForComBicCreditCard);

        if (subscription.getStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {
            // set subscription offering object
            subscriptionOffering.setId(commercialBicOfferings.getOfferings().get(0).getId());
            productLine.setCode(commercialBicOfferings.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
                cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                    subscriptionIdForComBicCreditCard, getUser(), subscriptionOffering, true, assertionErrorList),
                PelicanConstants.UPDATED, subscriptionIdForComBicCreditCard, true, assertionErrorList);
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
                subscriptionIdForComBicCreditCard, PelicanConstants.UPDATED, assertionErrorList);

        } else {
            Assert.fail("Subscription is not ACTIVE to do CSE event validation");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that for trial subscription change notification is logged. But not subscription change
     * notification
     * <p>
     * This validates for subscription creation and expiration
     */
    @Test(priority = 4)
    public void testOnlyChangeNotificationIsLoggedInEventForTrialBicSubscriptionCreationAndExpiration() {

        // place an order to create the notification for BIC trial subscription
        // creation
        final String subscriptionIdForTrialBicCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForTrialBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForTrialBicCreditCard, notNullValue(),
            assertionErrorList);
        // expire the subscription with 'Cancel Subscription' API
        resource.subscription().cancelSubscription(subscriptionIdForTrialBicCreditCard,
            CancellationPolicy.IMMEDIATE_NO_REFUND);
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // set subscription offering object
        subscriptionOffering.setId(trialBicOfferings.getOfferings().get(0).getId());
        productLine.setCode(trialBicOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeNotificationFound(
            cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList, subscriptionIdForTrialBicCreditCard,
                getUser(), subscriptionOffering, assertionErrorList),
            PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForTrialBicCreditCard, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that for Meta Commercial subscription, only change notification is logged. But not
     * subscription change notification
     * <p>
     * This validates for subscription creation and expiration
     */
    @Test(priority = 2)
    public void testOnlyChangeNotificationIsLoggedInEventForMetaCommercialSubscriptionCreationAndExpiration() {

        // place an order to create the notification for BIC trial subscription
        // creation
        final String subscriptionIdForMetaCreditCard =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForMeta, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForMetaCreditCard, notNullValue(),
            assertionErrorList);
        // expire the subscription with 'Cancel Subscription' API
        resource.subscription().cancelSubscription(subscriptionIdForMetaCreditCard,
            CancellationPolicy.IMMEDIATE_NO_REFUND);
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // set subscription offering object
        subscriptionOffering.setId(metaOfferings.getOfferings().get(0).getId());
        productLine.setCode(metaOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeNotificationFound(
            cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList, subscriptionIdForMetaCreditCard, getUser(),
                subscriptionOffering, assertionErrorList),
            PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForMetaCreditCard, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that for Non Commercial BIC subscription, only change notification is logged. But not
     * subscription change notification
     * <p>
     * This validates for subscription creation and status change from ACTIVE to CANCELED
     */
    @Test(priority = 3)
    public void testOnlyChangeNotificationIsLoggedInEventForNonCommercialBicSubscriptionCreationAndStatusChange() {

        // place an order to create the notification for BIC non commercial
        // subscription creation
        subscriptionIdForNonCommBic =
            cseHelper.getPurchaseOrderAndSubscription(purchaseOrderUtils, priceIdForNonCommBic, getBuyerUser());
        AssertCollector.assertThat("Subscription creation failed", subscriptionIdForNonCommBic, notNullValue(),
            assertionErrorList);
        // cancel the subscription with 'Cancel Subscription' API
        // so that status changed from ACTIVE to CANCELED
        resource.subscription().cancelSubscription(subscriptionIdForNonCommBic,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // set subscription offering object
        subscriptionOffering.setId(nonCommercialBicOfferings.getOfferings().get(0).getId());
        productLine.setCode(nonCommercialBicOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeNotificationFound(
            cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList, subscriptionIdForNonCommBic, getUser(),
                subscriptionOffering, assertionErrorList),
            PelicanConstants.CREATE_AND_UPDATE, subscriptionIdForNonCommBic, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies that for Non Commercial BIC subscription, only change notification is logged. But not
     * subscription change notification
     * <p>
     * This validates for subscription creation and status change from CANCELED to ACTIVE
     */
    @Test(
        dependsOnMethods = {
                "testOnlyChangeNotificationIsLoggedInEventForNonCommercialBicSubscriptionCreationAndStatusChange" },
        priority = 10)
    public void testOnlyChangeNotificationIsLoggedInEventForNonCommercialBicSubscriptionStatusChange() {

        // restart the canceled subscription with 'Restart A Canceled
        // Subscription' API
        // so that status changed from CANCELED to ACTIVE
        resource.subscription().restartCancelledSubscription(subscriptionIdForNonCommBic);
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // set subscription offering object
        subscriptionOffering.setId(nonCommercialBicOfferings.getOfferings().get(0).getId());
        productLine.setCode(nonCommercialBicOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeNotificationFound(
            cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList, subscriptionIdForNonCommBic, getUser(),
                subscriptionOffering, assertionErrorList),
            PelicanConstants.UPDATED, subscriptionIdForNonCommBic, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
