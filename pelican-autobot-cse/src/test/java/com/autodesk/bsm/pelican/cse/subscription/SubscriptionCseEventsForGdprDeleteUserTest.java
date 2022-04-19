package com.autodesk.bsm.pelican.cse.subscription;

import com.autodesk.bsm.pelican.api.clients.ItemInstancesClient;
import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstances;
import com.autodesk.bsm.pelican.api.pojos.json.CseECNotification;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.ChangeNotificationProducer;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SubscriptionCseEventsForGdprDeleteUserTest extends SeleniumWebdriver {

    private CSEHelper cseHelper;
    private JobsClient jobsResource;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private FindSubscriptionsPage findSubscriptionsPage;
    private SubscriptionOffering subscriptionOffering;
    private SubscriptionDetailPage subscriptionDetailPage;
    private ChangeNotificationProducer personMasterProducer = null;
    private List<ChangeNotificationMessage> eventsList;
    private PelicanPlatform resource;
    private JProductLine productLine;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionCseEventsForGdprDeleteUserTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        final PelicanTriggerClient triggersResource = pelicanResource.trigger();
        jobsResource = triggersResource.jobs();

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String personMasterNotificationChannel = getEnvironmentVariables().getPersonMasterNotificationChannel();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        final String notificationConsKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationConsSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationConsKey, notificationConsSecret);
        final String accessToken = authClient.getAuthToken();
        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());

        // Initialize Consumer
        cseHelper.initializeConsumer(brokerUrl, personMasterNotificationChannel, accessToken);
        personMasterProducer = cseHelper.initializeProducer(brokerUrl, personMasterNotificationChannel, authClient);
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
    }

    /**
     * Test to validate no cse events out while EC status update on subscription for GDPR delete user.
     */
    @Test()
    public void testSuccessNoCseEventsOnSubscriptionECStatusChange() {

        // set subscription offering object
        subscriptionOffering.setId(getBicSubscriptionPlan().getOfferings().get(0).getId());
        productLine.setCode(getBicSubscriptionPlan().getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final CseECNotification ecNotification = new CseECNotification();

        final HashMap<String, String> userParams = new HashMap<>();
        final String userExternalKey = "$TestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        LOGGER.info("User id: " + user.getId());
        LOGGER.info("User ext key: " + user.getExternalKey());

        // Create a buyer user
        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(user.getExternalKey());

        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, buyerUser.getId()),
            getEnvironmentVariables());

        // place an order to create the notification in Authorized state
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id: " + subscriptionId);

        // Constructing the ec status and posting to the cse channel
        final String[] ecUserArray = { user.getExternalKey() };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.REVIEW.getName());
        ecNotification.setECUpdateTimeStamp(dateFormat.format(date));

        final String ecReviewNotification = CSEHelper.buildMessage(ecNotification);
        personMasterProducer.notifyChange(new ChangeNotificationMessage(ecReviewNotification));

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();

        // change method to handle gdpr delete user.
        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationNotFoundForGDPRDeleteUser(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, subscriptionId, user,
                subscriptionOffering, true, assertionErrorList),
            subscriptionId, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate no CSE events out on expiring subscriptions through job for GDPR delete user. It does same for
     * Commercial and Trial. Here verified for trial.
     *
     */
    @Test
    public void testSuccessNoCseEventsOnSubscriptionExpirationForTrialSubscriptionThroughJob() {

        final HashMap<String, String> userMap = new HashMap<>();
        userMap.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userMap.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));

        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userMap, getEnvironmentVariables());

        final Offerings trialOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.TRL);
        final String trialOfferExternalKey = trialOffering.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String trialOfferingId = trialOffering.getOfferings().get(0).getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(trialOfferingId, null, null, true);

        final Subscription trialSubscription =
            resource.subscription().add(user.getExternalKey(), trialOfferExternalKey, Currency.USD);
        final String trialSubscriptionId = trialSubscription.getId();

        // get item instances
        final HashMap<String, String> requestParameters = new HashMap<>();
        requestParameters.put(ItemInstancesClient.Parameter.SUBSCRIPTION_ID.getName(), trialSubscriptionId);
        final ItemInstances itemInstances = resource.itemInstances().getItemInstances(requestParameters);
        final String itemInstanceId = itemInstances.getItemInstances().get(0).getId();

        LOGGER.info("TrialSubscriptionId - " + trialSubscriptionId);
        LOGGER.info("ItemInstanceId -" + itemInstanceId);

        eventsList = pelicanEventsConsumer.getNotifications();
        pelicanEventsConsumer.waitForEvents(10000);

        // Edit subscription status, billing date and expiration date before
        // running expiration job
        final String todayDate = DateTimeUtils.getNowMinusDays(0);
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(trialSubscriptionId);
        final EditSubscriptionPage editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, todayDate,
            Status.EDIT_SUBSCRIPTION_STATUS_CANCELLED.getDisplayName(), null);

        final JsonSubscriptionId jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(trialSubscriptionId);

        // clear events list before triggering expiration job
        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
        jobsResource.subscriptionExpiration(jsonSubscriptionId);

        subscriptionDetailPage.refreshPage();
        final String subscriptionStatus = subscriptionDetailPage.getStatus();

        if (subscriptionStatus == Status.EXPIRED.toString()) {
            eventsList = pelicanEventsConsumer.getNotifications();
            pelicanEventsConsumer.waitForEvents(10000);

            // verify cse notification for trial subscription
            subscriptionOffering.setId(trialOffering.getOfferings().get(0).getId());
            productLine.setCode(trialOffering.getOfferings().get(0).getProductLine());
            subscriptionOffering.setJProductLine(productLine);

            cseHelper
                .assertionToValidateChangeAndSubscriptionChangeNotificationNotFoundForGDPRDeleteUser(
                    cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList,
                        trialSubscriptionId, user, subscriptionOffering, true, assertionErrorList),
                    trialSubscriptionId, assertionErrorList);
        } else {
            LOGGER.info("Subscription {} is not Expired to Validate CSE events", trialSubscriptionId);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

}
