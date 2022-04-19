package com.autodesk.bsm.pelican.cse.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.ItemInstancesClient;
import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.SubscriptionClient;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstances;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
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
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
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
import java.util.HashMap;
import java.util.List;

/**
 * Test class to test subscription expiration job with subscription ids.
 *
 * @author jains
 */

public class SubscriptionExpirationJobTest extends SeleniumWebdriver {

    private SubscriptionClient subscriptionResouce;
    private JobsClient jobsResource;
    private FindSubscriptionsPage subscriptionPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private EditSubscriptionPage editSubscriptionPage;
    private Subscription expiredSubscriptionFromApi;
    private CSEHelper cseHelper;
    private SubscriptionOffering subscriptionOffering;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String PELICANEVENTS_NOTIFICATION_CHANNEL;
    private List<ChangeNotificationMessage> eventsList;
    private PelicanPlatform resource;
    private Offerings bicCommercialOffering1;
    private Offerings metaCommercialOffering1;
    private JProductLine productLine;
    private String bicSubscriptionId1;
    private String metaSubscriptionId1;
    private String trialOfferExternalKey;
    private Offerings trialOffering;
    private JsonSubscriptionId jsonSubscriptionId;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionExpirationJobTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        subscriptionResouce =
            new SubscriptionClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        final PelicanTriggerClient triggersResource = pelicanResource.trigger();
        jobsResource = triggersResource.jobs();
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        // create bic offering and bic subscription
        bicCommercialOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicPriceId1 = bicCommercialOffering1.getIncluded().getPrices().get(0).getId();
        // create purchase order for bic
        priceQuantityMap.put(bicPriceId1, 1);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        bicSubscriptionId1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // create meta offering and meta subscription
        metaCommercialOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String metaPriceId1 = metaCommercialOffering1.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(metaPriceId1, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // fulfill the order
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        metaSubscriptionId1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        trialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        trialOfferExternalKey = trialOffering.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String trialOfferingId = trialOffering.getOfferings().get(0).getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(trialOfferingId, null, null, true);

        // start cse events
        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String BROKER_URL = getEnvironmentVariables().getBrokerUrl();
        PELICANEVENTS_NOTIFICATION_CHANNEL = getEnvironmentVariables().getPelicanEventsNotificationChannel();
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
            cseHelper.initializeConsumer(BROKER_URL, PELICANEVENTS_NOTIFICATION_CHANNEL, ACCESS_TOKEN);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, PELICANEVENTS_NOTIFICATION_CHANNEL, eventsList);
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
     * This test case validates if subscription(both bic and meta) is expired after running subscription expiration job
     * . It also validates change notifications and subscriptionChangeNotifications.
     *
     */
    @Test
    public void testSubscriptionExpirationForCommercialSubscription() {
        // Edit subscription status, billing date and expiration date before
        // running expiration job
        final String todayDate = DateTimeUtils.getNowMinusDays(0);
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(bicSubscriptionId1);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, todayDate,
            Status.EDIT_SUBSCRIPTION_STATUS_CANCELLED.getDisplayName(), null);

        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(metaSubscriptionId1);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, todayDate,
            Status.EDIT_SUBSCRIPTION_STATUS_CANCELLED.getDisplayName(), null);

        jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(bicSubscriptionId1 + "," + metaSubscriptionId1);

        // clear events list before triggering expiration job
        eventsList.clear();
        jobsResource.subscriptionExpiration(jsonSubscriptionId);

        eventsList = pelicanEventsConsumer.getNotifications();
        pelicanEventsConsumer.waitForEvents(2000);

        // verify cse notification for bicSubscriptionId1
        subscriptionOffering.setId(bicCommercialOffering1.getOfferings().get(0).getId());
        productLine.setCode(bicCommercialOffering1.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, bicSubscriptionId1,
                getUser(), subscriptionOffering, true, assertionErrorList),
            PelicanConstants.UPDATED, bicSubscriptionId1, true, assertionErrorList);

        // verify cse notification for metaSubscriptionId1
        subscriptionOffering.setId(metaCommercialOffering1.getOfferings().get(0).getId());
        productLine.setCode(metaCommercialOffering1.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, metaSubscriptionId1,
                getUser(), subscriptionOffering, true, assertionErrorList),
            PelicanConstants.UPDATED, metaSubscriptionId1, false, assertionErrorList);

        // verify api and admin tool for bicSubscriptionId1
        expiredSubscriptionFromApi = subscriptionResouce.getById(bicSubscriptionId1);
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(bicSubscriptionId1);
        // Api validation
        AssertCollector.assertThat("Incorrect api subscription status for " + bicSubscriptionId1,
            expiredSubscriptionFromApi.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        // Admin tool validation
        AssertCollector.assertThat("Incorrect AT subscription status for " + bicSubscriptionId1,
            subscriptionDetailPage.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect expiration date for " + bicSubscriptionId1,
            subscriptionDetailPage.getExpirationDate(), equalTo(todayDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing date " + bicSubscriptionId1,
            subscriptionDetailPage.getNextBillingDate(), equalTo("-"), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing charge " + bicSubscriptionId1,
            subscriptionDetailPage.getNextBillingCharge(), equalTo("-"), assertionErrorList);

        // verify api and admin tool for metaSubscriptionId1
        expiredSubscriptionFromApi = subscriptionResouce.getById(metaSubscriptionId1);
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(metaSubscriptionId1);
        // Api validation
        AssertCollector.assertThat("Incorrect api subscription status for " + metaSubscriptionId1,
            expiredSubscriptionFromApi.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        // Admin tool validation
        AssertCollector.assertThat("Incorrect AT subscription status for " + metaSubscriptionId1,
            subscriptionDetailPage.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect expiration date for " + metaSubscriptionId1,
            subscriptionDetailPage.getExpirationDate(), equalTo(todayDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing date for " + metaSubscriptionId1,
            subscriptionDetailPage.getNextBillingDate(), equalTo("-"), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing charge for " + metaSubscriptionId1,
            subscriptionDetailPage.getNextBillingCharge(), equalTo("-"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates if trial subscription is expired after running subscription expiration job . It also
     * validates change notifications and subscriptionChangeNotifications.
     *
     */
    @Test
    public void testSubscriptionExpirationForTrialSubscription() {
        final Subscription trialSubscription =
            resource.subscription().add(getUser().getExternalKey(), trialOfferExternalKey, Currency.USD);
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

        boolean isNotificationFoundForItemInstance = cseHelper.assertionToValidateChangeNotificationForItemInstances(
            eventsList, itemInstanceId, PelicanConstants.CREATED, assertionErrorList);
        AssertCollector.assertTrue("Change notification not found for create for item instance id:" + itemInstanceId,
            isNotificationFoundForItemInstance, assertionErrorList);
        // Edit subscription status, billing date and expiration date before
        // running expiration job
        final String todayDate = DateTimeUtils.getNowMinusDays(0);
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(trialSubscriptionId);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(todayDate, todayDate,
            Status.EDIT_SUBSCRIPTION_STATUS_CANCELLED.getDisplayName(), null);

        jsonSubscriptionId = new JsonSubscriptionId();
        jsonSubscriptionId.setSubscriptionId(trialSubscriptionId);

        // clear events list before triggering expiration job
        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
        jobsResource.subscriptionExpiration(jsonSubscriptionId);

        eventsList = pelicanEventsConsumer.getNotifications();
        pelicanEventsConsumer.waitForEvents(10000);

        // verify cse notification for trial subscription
        subscriptionOffering.setId(trialOffering.getOfferings().get(0).getId());
        productLine.setCode(trialOffering.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        // we don't send subscription change notification for trial subscription
        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, trialSubscriptionId,
                getUser(), subscriptionOffering, false, assertionErrorList),
            PelicanConstants.UPDATED, trialSubscriptionId, false, assertionErrorList);

        // verify that subscription change notification are not sent.
        final boolean isSubscriptionChangeNotificationSent =
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, trialSubscriptionId,
                getUser(), subscriptionOffering, true, assertionErrorList).get(1);
        AssertCollector.assertThat(
            "Subscription change notifications should not be sent for trial subscription expiration.",
            isSubscriptionChangeNotificationSent, equalTo(false), assertionErrorList);

        isNotificationFoundForItemInstance = cseHelper.assertionToValidateChangeNotificationForItemInstances(eventsList,
            itemInstanceId, PelicanConstants.UPDATED, assertionErrorList);
        AssertCollector.assertTrue("Change notification not found for update for item instance id:" + itemInstanceId,
            isNotificationFoundForItemInstance, assertionErrorList);

        // verify api and admin tool for trialSubscriptionId
        expiredSubscriptionFromApi = subscriptionResouce.getById(trialSubscriptionId);
        subscriptionDetailPage = subscriptionPage.findBySubscriptionId(trialSubscriptionId);
        // Api validation
        AssertCollector.assertThat("Incorrect api subscription status for " + trialSubscriptionId,
            expiredSubscriptionFromApi.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
