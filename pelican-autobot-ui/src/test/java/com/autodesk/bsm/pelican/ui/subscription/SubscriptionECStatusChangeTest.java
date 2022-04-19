package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.CseECNotification;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.ChangeNotificationProducer;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This is a test class which will test the ec status change events on the subscription when the subscription is in
 * active, canceled and in expired status
 *
 * @author Vineel
 */
public class SubscriptionECStatusChangeTest extends SeleniumWebdriver {

    private PelicanClient pelicanResource;
    private PelicanPlatform resource;
    private ChangeNotificationConsumer personMasterConsumer = null;
    private ChangeNotificationProducer personMasterProducer = null;
    private String personMasterNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private String ecUser;
    private String secondEcUser;
    private String thirdEcUser;
    private User user;
    private Offerings bicOfferings;
    private Offerings metaOfferings;
    private String bicSubscriptionId;
    private String metaSubscriptionId;
    private SubscriptionDetailPage subscriptionDetailPage;
    private FindSubscriptionsPage subscriptionPage;
    private String secondUserSubscriptionId;
    private String thirdUserSubscriptionId;
    private static final String SUBSCRIPTION_ACTIVITY_CHANGE = "EC_CHANGE";
    private static final String SUBSCRIPTION_ACTIVITY_IGNORED = "EC_CHANGE_IGNORED";
    private static final String PROVISION_CREDITS_ACTIVITY = "PROVISION_CREDITS";
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private static final String SUBSCRIPTION_MEMO =
        "The event timestamp is earlier than the subscription EC timestamp.";
    private SubscriptionOffering subscriptionOffering;
    private JProductLine productLine;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionECStatusChangeTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        pelicanResource = new PelicanClient(getEnvironmentVariables());
        resource = pelicanResource.platform();
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();

        // Create a new user for every run
        final HashMap<String, String> userParams = new HashMap<>();
        final String userExternalKey = "$TestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        userParams.put(UserParameter.NAME.getName(), userExternalKey);
        UserUtils userUtils = new UserUtils();
        user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        LOGGER.info("User id: " + user.getId());
        LOGGER.info("User ext key: " + user.getExternalKey());

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        personMasterNotificationChannel = getEnvironmentVariables().getPersonMasterNotificationChannel();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();
        ecUser = user.getExternalKey();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        personMasterConsumer = cseHelper.initializeConsumer(brokerUrl, personMasterNotificationChannel, accessToken);
        personMasterProducer = cseHelper.initializeProducer(brokerUrl, personMasterNotificationChannel, authClient);
        // Initialize Consumer
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

        // Create a buyer user
        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(user.getExternalKey());
        PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Create a bic and meta offering
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        metaOfferings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // create a priceId for bic and mets offerings
        final String priceIdForBic = bicOfferings.getIncluded().getPrices().get(0).getId();
        final String priceIdForMeta = metaOfferings.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with bic line item and retrieve the bic
        // purchase order id
        int quantity = 1;
        PurchaseOrder bicPurchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForBic, buyerUser, quantity);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, bicPurchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        bicPurchaseOrder = resource.purchaseOrder().getById(bicPurchaseOrder.getId());
        bicSubscriptionId = bicPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
        LOGGER.info("Bic Subscription Id created:" + bicSubscriptionId);

        // Submit a purchase order with bic line item and retrieve the bic
        // purchase order id
        quantity = 2;
        PurchaseOrder metaPurchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForMeta, buyerUser, quantity);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, metaPurchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, metaPurchaseOrder.getId());
        metaPurchaseOrder = resource.purchaseOrder().getById(metaPurchaseOrder.getId());
        purchaseOrderUtils.fulfillRequest(metaPurchaseOrder, FulfillmentCallbackStatus.Created);
        metaPurchaseOrder = resource.purchaseOrder().getById(metaPurchaseOrder.getId());
        metaSubscriptionId = metaPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
        LOGGER.info("Meta Subscription Id created:" + metaSubscriptionId);

        // Create a second user
        userParams.clear();
        final String secondUserExternalKey = "$TestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), secondUserExternalKey);
        userParams.put(UserParameter.NAME.getName(), secondUserExternalKey);
        userUtils = new UserUtils();
        final User secondUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        LOGGER.info("User id: " + secondUser.getId());
        LOGGER.info("User ext key: " + secondUser.getExternalKey());
        secondEcUser = secondUser.getExternalKey();

        // Create a buyer user
        final BuyerUser secondBuyerUser = new BuyerUser();
        secondBuyerUser.setId(secondUser.getId());
        secondBuyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        secondBuyerUser.setExternalKey(secondUser.getExternalKey());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Submit a purchase order for the second user
        quantity = 1;
        PurchaseOrder secondPurchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, secondBuyerUser, quantity);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, secondPurchaseOrder.getId());
        // purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE,
        // secondPurchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        secondPurchaseOrder = resource.purchaseOrder().getById(secondPurchaseOrder.getId());
        secondUserSubscriptionId = secondPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Bic Subscription Id created:" + secondUserSubscriptionId);

        // Create a third user
        userParams.clear();
        final String thirdUserExternalKey = "$TestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), thirdUserExternalKey);
        userParams.put(UserParameter.NAME.getName(), thirdUserExternalKey);
        userUtils = new UserUtils();
        final User thirdUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        LOGGER.info("User id: " + thirdUser.getId());
        LOGGER.info("User ext key: " + thirdUser.getExternalKey());
        thirdEcUser = thirdUser.getExternalKey();

        // Create a buyer user for the third user
        final BuyerUser thirdBuyerUser = new BuyerUser();
        thirdBuyerUser.setId(thirdUser.getId());
        thirdBuyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        thirdBuyerUser.setExternalKey(thirdUser.getExternalKey());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Submit a purchase order for the second user
        PurchaseOrder thirdPurchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForBic, thirdBuyerUser, quantity);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, thirdPurchaseOrder.getId());
        // purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE,
        // thirdPurchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        thirdPurchaseOrder = resource.purchaseOrder().getById(secondPurchaseOrder.getId());
        thirdUserSubscriptionId = thirdPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Bic Subscription Id created:" + thirdUserSubscriptionId);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        cseHelper.terminateProducer(personMasterProducer, personMasterNotificationChannel);
        cseHelper.terminateConsumer(personMasterConsumer, personMasterNotificationChannel, eventsList);
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        pelicanResource = new PelicanClient(getEnvironmentVariables());
        resource = pelicanResource.platform();
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
        personMasterConsumer.clearNotificationsList();
        pelicanEventsConsumer.clearNotificationsList();
    }

    /**
     * This is a test case to change the ec status to review, block and then to unverified when the subscription status
     * is active
     *
     * @param ecStatus - ec status which has to be updated with
     */
    @Test(dataProvider = "ecStatusChange")
    public void testSubscriptionECStatusChangeOnActiveStatus(final ECStatus ecStatus) {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final CseECNotification ecNotification = new CseECNotification();
        final String utcDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT);

        // Constructing the ec status and posting to the cse channel
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ecStatus.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        // Find a bic subscription and read the subscription activity for the
        // subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        subscriptionPage.findBySubscriptionId(bicSubscriptionId);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final Subscription bicSubscription = resource.subscription().getById(bicSubscriptionId);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        final String exportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        AssertCollector.assertThat("Incorrect ec status in bic subscription", bicSubscription.getExportControlStatus(),
            equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription activity for the subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription memo for the subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            exportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);

        // Find a meta subscription and read the subscription activity for the
        // subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription metaSubscription = resource.subscription().getById(metaSubscriptionId);
        subscriptionPage.findBySubscriptionId(metaSubscriptionId);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        AssertCollector.assertThat("Incorrect ec status in meta subscription",
            metaSubscription.getExportControlStatus(), equalTo(ecStatus.getDisplayName()), assertionErrorList);
        final List<SubscriptionActivity> metaSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity metaSubscriptionActivity =
            metaSubscriptionActivityList.get((metaSubscriptionActivityList.size()) - 1);
        final String metaExportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();

        // set subscription offering object
        subscriptionOffering.setId(metaOfferings.getOfferings().get(0).getId());
        productLine.setCode(metaOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        final List<Boolean> changeNotificationResults = cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList,
            metaSubscriptionId, user, subscriptionOffering, assertionErrorList);

        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Incorrect meta subscription activity for the subscription",
            metaSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription memo for the subscription",
            metaSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription export control last modified",
            metaExportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);

        AssertCollector.assertTrue("CSE Notification is not found for subscription updation",
            changeNotificationResults.get(3), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case to change the ec status to review, block and then to unverified when the subscription status
     * is canceled
     *
     * @param ecStatus - ec status which has to be updated with
     */
    @Test(dataProvider = "ecStatusChange")
    public void testSubscriptionECStatusChangeOnCanceledStatus(final ECStatus ecStatus) {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String utcDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT);

        // Constructing the ec status and posting to the cse channel
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ecStatus.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        // cancel the subscription if the subscription status is active
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        Subscription bicSubscription = resource.subscription().getById(bicSubscriptionId);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        Subscription metaSubscription = resource.subscription().getById(metaSubscriptionId);
        if (!(bicSubscription.getStatus().equalsIgnoreCase(Status.CANCELLED.toString()))) {
            resource.subscription().cancelSubscription(bicSubscriptionId,
                CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        }
        if (!(metaSubscription.getStatus().equalsIgnoreCase(Status.CANCELLED.toString()))) {
            resource.subscription().cancelSubscription(metaSubscriptionId,
                CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        }
        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        // Find a bic subscription and read the events for the subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        bicSubscription = resource.subscription().getById(bicSubscriptionId);
        subscriptionPage.findBySubscriptionId(bicSubscriptionId);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        final String exportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        AssertCollector.assertThat("Incorrect ec status in bic subscription", bicSubscription.getExportControlStatus(),
            equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription activity for the subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription memo for the subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            exportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);

        // Gind the created meta subscription and read the subscription events
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        metaSubscription = resource.subscription().getById(metaSubscriptionId);
        subscriptionPage.findBySubscriptionId(metaSubscriptionId);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        AssertCollector.assertThat("Incorrect ec status in meta subscription",
            metaSubscription.getExportControlStatus(), equalTo(ecStatus.getDisplayName()), assertionErrorList);
        final List<SubscriptionActivity> metaSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity metaSubscriptionActivity =
            metaSubscriptionActivityList.get((metaSubscriptionActivityList.size()) - 1);
        final String metaExportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();

        // set subscription offering object
        subscriptionOffering.setId(metaOfferings.getOfferings().get(0).getId());
        productLine.setCode(metaOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        final List<Boolean> changeNotificationResults = cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList,
            metaSubscriptionId, user, subscriptionOffering, assertionErrorList);

        // Validate the cse events and subscription activity events for the ec
        // change
        AssertCollector.assertThat("Incorrect meta subscription activity for the subscription",
            metaSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription memo for the subscription",
            metaSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription export control last modified",
            metaExportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);

        AssertCollector.assertTrue("CSE Notification is not found for subscription updation",
            changeNotificationResults.get(3), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test the EC status when the Person Master timestamp is earlier than the subscription timestamp
     */
    @Test
    public void testECStatusWithEventTimeStampEarlierThanSubscriptionTimeStamp() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String utcDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT);

        // Constructing the ec event and posting it to the cse channel
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.BLOCK.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));
        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        // Constructing the another ec event for the same user with the ealier
        // timestamp
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Date oldDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        final CseECNotification newEcNotification = new CseECNotification();
        final String[] newEcUserArray = { ecUser };
        newEcNotification.setEcOxygenIds(newEcUserArray);
        newEcNotification.setECStatus(ECStatus.REVIEW.getName());
        newEcNotification.setECUpdateTimeStamp(dateFormat.format(oldDate));

        final String newECReviewNotification = CSEHelper.buildMessage(newEcNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(newECReviewNotification));

        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription bicSubscription = resource.subscription().getById(bicSubscriptionId);
        subscriptionPage.findBySubscriptionId(bicSubscriptionId);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        final String exportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        // Validating the bic subscription and subscription events for the ec
        // change event
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec status in bic subscription", bicSubscription.getExportControlStatus(),
            not(ECStatus.REVIEW.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription activity for the subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_IGNORED), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription memo for the subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(ECStatus.REVIEW.getDisplayName() + "."),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            exportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change message in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[2], equalTo(SUBSCRIPTION_MEMO), assertionErrorList);

        // Validating the meta subscription and subscription activity for the ec
        // status change event
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription metaSubscription = resource.subscription().getById(metaSubscriptionId);
        subscriptionPage.findBySubscriptionId(metaSubscriptionId);
        final List<SubscriptionActivity> metaSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity metaSubscriptionActivity =
            metaSubscriptionActivityList.get((metaSubscriptionActivityList.size()) - 1);
        final String metaExportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();
        AssertCollector.assertThat("Incorrect ec status in meta subscription",
            metaSubscription.getExportControlStatus(), not(ECStatus.REVIEW.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription activity for the subscription",
            metaSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_IGNORED), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription memo for the subscription",
            metaSubscriptionActivity.getMemo().split("\n")[0], equalTo(ECStatus.REVIEW.getDisplayName() + "."),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription export control last modified",
            metaExportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            metaSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change message in subscription memo",
            metaSubscriptionActivity.getMemo().split("\n")[2], equalTo(SUBSCRIPTION_MEMO), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test the Same EC status when the Person Master timestamp is later than the subscription timestamp
     */
    @Test
    public void testSameECStatusWithEventTimeStampLaterThanSubscriptionTimeStamp() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();

        // Construct a ec event for the user and post it to the cse channel
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.REVIEW.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));
        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Date newDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L);
        final String newUtcDate = DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT);

        // Constrict another ec event for the same user with later timestamp
        pelicanEventsConsumer.clearNotificationsList();
        final CseECNotification newEcNotification = new CseECNotification();
        final String[] newEcUserArray = { ecUser };
        newEcNotification.setEcOxygenIds(newEcUserArray);
        newEcNotification.setECStatus(ECStatus.REVIEW.getName());
        newEcNotification.setECUpdateTimeStamp(dateFormat.format(newDate));
        final String newECReviewNotification = CSEHelper.buildMessage(newEcNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(newECReviewNotification));

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // set subscription offering object for bic
        subscriptionOffering.setId(bicOfferings.getOfferings().get(0).getId());
        productLine.setCode(bicOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        // Find the created bic subscription and read the subscription events
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription bicSubscription = resource.subscription().getById(bicSubscriptionId);
        subscriptionPage.findBySubscriptionId(bicSubscriptionId);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        final String exportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();

        AssertCollector.assertThat("Incorrect ec status in bic subscription", bicSubscription.getExportControlStatus(),
            equalTo(ECStatus.REVIEW.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription activity for the subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription memo for the subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(ECStatus.REVIEW.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(newUtcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            exportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(newUtcDate),
            assertionErrorList);

        // Find the created meta subscription and read the subscription events
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription metaSubscription = resource.subscription().getById(metaSubscriptionId);
        subscriptionPage.findBySubscriptionId(metaSubscriptionId);
        final List<SubscriptionActivity> metaSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity metaSubscriptionActivity =
            metaSubscriptionActivityList.get((metaSubscriptionActivityList.size()) - 1);

        // Validate the meta subscription and meta subscription events for the
        // ec status change
        final String metaExportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();
        AssertCollector.assertThat("Incorrect ec status in meta subscription",
            metaSubscription.getExportControlStatus(), equalTo(ECStatus.REVIEW.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription activity for the subscription",
            metaSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription memo for the subscription",
            metaSubscriptionActivity.getMemo().split("\n")[0], equalTo(ECStatus.REVIEW.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription export control last modified",
            metaExportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(newUtcDate),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            metaSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(newUtcDate), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid state
     */
    @Test
    public void testInvalidValueECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus("DUMMY");
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecInvalidValueNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidValueNotification));
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        personMasterConsumer.waitForEvents(5000);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid format
     */
    @Test
    public void testInvalidFormatMissingFieldECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String ecInvalidNotification =
            "{\"EidMGUID\":\"201608302312173\",\"contactCsns\":[\"66403690\"]," + "\"SummaryECStatus\":\""
                + ECStatus.ACCEPT.getName() + "\",\"SummaryECUpdateTimestamp\":\"" + dateFormat.format(date) + "\"}";

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidNotification));
        personMasterConsumer.waitForEvents(5000);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        LOGGER.info("Events List Size:" + eventsList.size());

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid format
     */
    @Test
    public void testInvalidFormatInvalidSyntaxECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String ecInvalidNotification =
            "\"oxygen_id\":[\"7MYJKXZLG8BN\"],\"EidMGUID\":\"201608302312173\",\"contactCsns\":[\"66403690\"],"
                + "\"SummaryECStatus\":\"" + ECStatus.ACCEPT.getName() + "\",\"SummaryECUpdateTimestamp\":\""
                + dateFormat.format(date) + "\"}";

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidNotification));
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        personMasterConsumer.waitForEvents(5000);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        LOGGER.info("Events List Size:" + eventsList.size());

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Null Value
     */
    @Test
    public void testNullValueECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = {};
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(null);
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecNullValueNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecNullValueNotification));
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        personMasterConsumer.waitForEvents(5000);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        LOGGER.info("Events List Size:" + eventsList.size());

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to EmptyValue
     */
    @Test
    public void testEmptyValueECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final CseECNotification ecNotification = new CseECNotification();
        final String[] ecUserArray = {};
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus("DUMMY");
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecEmptyValueNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecEmptyValueNotification));
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        personMasterConsumer.waitForEvents(5000);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        LOGGER.info("Events List Size:" + eventsList.size());

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Test case to change EC status to Invalid format
     */
    @Test
    public void testInvalidFormatECStatusEvents() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final String ecInvalidFormatNotification = "{\"oxy_id\":[\"" + ecUser
            + "\"],\"EidMGUID\":\"201608302312173\",\"contactCsns\":[\"66403690\"]," + "\"SummaryECStatus\":\""
            + ECStatus.ACCEPT.getName() + "\",\"SummaryECUpdateTimestamp\":\"" + dateFormat.format(date) + "\"}";

        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecInvalidFormatNotification));
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        personMasterConsumer.waitForEvents(5000);
        eventsList = personMasterConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        LOGGER.info("Events List Size:" + eventsList.size());

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case to change the ec status for the multiple users upto 3 users -to review, block and then to
     * unverified when the subscription status is active
     */
    @Test(dataProvider = "ecStatusChange")
    public void testSubscriptionECStatusChangeOnActiveStatusForMultipleUsers(final ECStatus ecStatus) {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date newDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L);
        final String utcDate =
            DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT).split(":")[0];

        final CseECNotification ecNotification = new CseECNotification();

        // Constructing the ec status and posting to the cse channel
        final String inavlidECUser = RandomStringUtils.randomAlphabetic(8);
        final String[] ecUserArray = { ecUser, secondEcUser, thirdEcUser, inavlidECUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ecStatus.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(newDate));

        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        // Find a bic subscription and read the subscription activity for the
        // subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription bicSubscription = resource.subscription().getById(bicSubscriptionId);
        subscriptionPage.findBySubscriptionId(bicSubscriptionId);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        if (bicSubscriptionActivity.getActivity().equalsIgnoreCase(PROVISION_CREDITS_ACTIVITY)) {
            bicSubscriptionActivity = bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 2);
        }
        final String exportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec status in bic subscription", bicSubscription.getExportControlStatus(),
            equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription activity for the subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription memo for the subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getDisplayName() + "."),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(":")[0], equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            exportControlLastModifiedDate.split(":")[0], equalTo(utcDate), assertionErrorList);

        // Find a meta subscription and read the subscription activity for the
        // subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        subscriptionPage.findBySubscriptionId(metaSubscriptionId);
        final List<SubscriptionActivity> metaSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        SubscriptionActivity metaSubscriptionActivity =
            metaSubscriptionActivityList.get((metaSubscriptionActivityList.size()) - 1);
        if (metaSubscriptionActivity.getActivity().equalsIgnoreCase(PROVISION_CREDITS_ACTIVITY)) {
            metaSubscriptionActivity = metaSubscriptionActivityList.get((metaSubscriptionActivityList.size()) - 2);
        }
        final String metaExportControlLastModifiedDate = subscriptionDetailPage.getExportControlStatusLastModified();

        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Incorrect ec status in first meta subscription",
            bicSubscription.getExportControlStatus(), equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription activity for the subscription",
            metaSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription memo for the subscription",
            metaSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta subscription export control last modified",
            metaExportControlLastModifiedDate.split(":")[0], equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(":")[0], equalTo(utcDate), assertionErrorList);

        // Find a second bic subscription and read the subscription activity for
        // the subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription secondBicSubscription = resource.subscription().getById(secondUserSubscriptionId);
        subscriptionPage.findBySubscriptionId(secondUserSubscriptionId);
        final List<SubscriptionActivity> secondSubscriptionSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        SubscriptionActivity secondSubscriptionActivity =
            secondSubscriptionSubscriptionActivityList.get((secondSubscriptionSubscriptionActivityList.size()) - 1);
        if (secondSubscriptionActivity.getActivity().equalsIgnoreCase(PROVISION_CREDITS_ACTIVITY)) {
            secondSubscriptionActivity =
                secondSubscriptionSubscriptionActivityList.get((secondSubscriptionSubscriptionActivityList.size()) - 2);
        }
        final String secondSubscriptionExportControlLastModifiedDate =
            subscriptionDetailPage.getExportControlStatusLastModified();
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Incorrect ec status in second bic subscription",
            secondBicSubscription.getExportControlStatus(), equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        AssertCollector.assertThat("Incorrect second subscription activity for the subscription",
            secondSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect second subscription memo for the subscription",
            secondSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getDisplayName()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            secondSubscriptionActivity.getMemo().split("\n")[1].split(":")[0], equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            secondSubscriptionExportControlLastModifiedDate.split(":")[0], equalTo(utcDate), assertionErrorList);

        // Find a third bic subscription and read the subscription activity for
        // the
        // subscription
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        final Subscription thirdBicSubscription = resource.subscription().getById(thirdUserSubscriptionId);
        subscriptionPage.findBySubscriptionId(thirdUserSubscriptionId);
        final List<SubscriptionActivity> thirdSubscriptionSubscriptionActivityList =
            subscriptionDetailPage.getSubscriptionActivity();
        SubscriptionActivity thirdSubscriptionActivity =
            thirdSubscriptionSubscriptionActivityList.get((thirdSubscriptionSubscriptionActivityList.size()) - 1);
        if (thirdSubscriptionActivity.getActivity().equalsIgnoreCase(PROVISION_CREDITS_ACTIVITY)) {
            thirdSubscriptionActivity =
                thirdSubscriptionSubscriptionActivityList.get((thirdSubscriptionSubscriptionActivityList.size()) - 2);
        }
        final String thirdSubscriptionExportControlLastModifiedDate =
            subscriptionDetailPage.getExportControlStatusLastModified();
        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Incorrect ec status in third bic subscription",
            thirdBicSubscription.getExportControlStatus(), equalTo(ecStatus.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThanOrEqualTo(0), assertionErrorList);
        AssertCollector.assertThat("Incorrect third subscription activity for the subscription",
            thirdSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect third subscription memo for the subscription",
            thirdSubscriptionActivity.getMemo().split("\n")[0], equalTo(ecStatus.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            thirdSubscriptionActivity.getMemo().split("\n")[1].split(":")[0], equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            thirdSubscriptionExportControlLastModifiedDate.split(":")[0], equalTo(utcDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "ecStatusChange")
    public Object[][] getDifferentECStatuses() {
        return new Object[][] {

                { ECStatus.REVIEW }, { ECStatus.BLOCK }, { ECStatus.ACCEPT } };
    }
}
