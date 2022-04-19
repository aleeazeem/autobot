package com.autodesk.bsm.pelican.bvt;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.OfferingsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient.FieldName;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.helpers.HelperForPriceQuote;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.json.CseECNotification;
import com.autodesk.bsm.pelican.api.pojos.json.JAdditionalFee;
import com.autodesk.bsm.pelican.api.pojos.json.JLineItem;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionReference;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes.PriceQuoteData;
import com.autodesk.bsm.pelican.api.pojos.json.Shipping;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.apidoc.APIDocPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.AddFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Case : Get Purchase Orders API
 *
 * @author mandas
 */
public class BVTTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private String purchaseOrderIdForBicCreditCard;
    private BuyerUser buyerUser;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int quantity = 1;
    private Offerings bicOfferings;
    private String priceIdForBic;
    private PurchaseOrder purchaseOrder;
    private String subscriptionIdBicCreditCard;
    private JobsClient jobsResource;
    private AdminToolPage adminToolPage;
    private APIDocPage apiDocPage;
    private static final String SUBSCRIPTION_ACTIVITY_CHANGE = "EC_CHANGE";
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private ChangeNotificationConsumer personMasterConsumer = null;
    private ChangeNotificationProducer personMasterProducer = null;
    private String pelicanEventsNotificationChannel;
    private CSEHelper cseHelper;
    private List<ChangeNotificationMessage> eventsList;
    private static final int DEFAULT_BLOCK_SIZE_WITH_SUBSCRIPTION_IDS = 20;
    private static final int DEFAULT_START_INDEX = 0;
    private String featureTypeName;
    private String userId;
    private AddFeaturePage addFeaturePage;
    private FeatureDetailPage featureDetailPage;
    private String appId;
    private String featureTypeId;
    private String personMasterNotificationChannel;
    private String ecUser;
    private User user;
    private String bicSubscriptionId;
    private SubscriptionOffering subscriptionOffering;
    private FindSubscriptionsPage subscriptionPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private JProductLine productLine;

    private static final Logger LOGGER = LoggerFactory.getLogger(BVTTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        userId = getEnvironmentVariables().getUserId();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        initializeDriver(getEnvironmentVariables());
        apiDocPage = new APIDocPage(getDriver(), getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addFeaturePage = adminToolPage.getPage(AddFeaturePage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
        AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
        subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        final PelicanTriggerClient triggerResource1 = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource1.jobs();
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();

        // get application
        final Applications applications = resource.application().getApplications();
        if (applications.getApplications().size() > 0) {
            appId = applications.getApplications().get(applications.getApplications().size() - 1).getId();
        }
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // Creating offerings

        bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Creating price id for BIC
        priceIdForBic = bicOfferings.getIncluded().getPrices().get(0).getId();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Create a new user for every run
        final HashMap<String, String> userParams = new HashMap<>();
        final String userExternalKey = "$TestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);

        final UserUtils userUtils = new UserUtils();
        user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        LOGGER.info("User id: " + user.getId());
        LOGGER.info("User ext key: " + user.getExternalKey());
        ecUser = user.getExternalKey();

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        personMasterNotificationChannel = getEnvironmentVariables().getPersonMasterNotificationChannel();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        buyerUser = new BuyerUser();
        buyerUser.setId(getEnvironmentVariables().getUserId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(getEnvironmentVariables().getUserExternalKey());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        personMasterConsumer = cseHelper.initializeConsumer(brokerUrl, personMasterNotificationChannel, accessToken);
        personMasterProducer = cseHelper.initializeProducer(brokerUrl, personMasterNotificationChannel, authClient);
        // Initialize Consumer
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

        // Create a buyer user
        buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(user.getExternalKey());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Submit a purchase order with bic line item and retrieve the bic
        // purchase order id
        final int quantity = 1;
        PurchaseOrder bicPurchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            priceIdForBic, buyerUser, quantity);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, bicPurchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, bicPurchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        bicPurchaseOrder = resource.purchaseOrder().getById(bicPurchaseOrder.getId());
        bicSubscriptionId = bicPurchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
            .getSubscriptionId();
        LOGGER.info("Bic Subscription Id created:" + bicSubscriptionId);

        addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
        featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

        // Navigate to the add feature page and add a feature
        addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
            getEnvironmentVariables().getApplicationDescription(), featureTypeName, featureTypeName);
        final FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
        featureTypeId = featureTypeDetailPage.getId();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        cseHelper.terminateConsumer(personMasterConsumer, personMasterNotificationChannel, eventsList);
        cseHelper.terminateProducer(personMasterProducer, personMasterNotificationChannel);
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    /**
     * Test Submit -> Process -> Charge
     */
    @Test
    public void testSubmitAndProcessPurchaseOrder() {
        pelicanEventsConsumer.clearNotificationsList();
        // Submit a purchase order with Credit card
        purchaseOrderIdForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);
        subscriptionIdBicCreditCard =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        AssertCollector.assertThat("Purchase Order is null", purchaseOrder.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Subscription is null", subscriptionIdBicCreditCard, notNullValue(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Run trigger Invoice job
     */
    @Test(dependsOnMethods = "testSubmitAndProcessPurchaseOrder", priority = 2)
    public void testInvoiceJobTrigger() {
        jobsResource.invoiceNumbers(Util.getBasicAuthHeaderValue(getEnvironmentVariables().getInvoiceAdminUsername(),
            getEnvironmentVariables().getInvoiceAdminPassword()));
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Find Subscription by ID from api doc page
     */
    @Test(dependsOnMethods = "testInvoiceJobTrigger", priority = 3)
    public void testFindSubscriptionsAPI() {
        // Submit a purchase order with Credit card
        purchaseOrderIdForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard);

        final String subscriptionIdBicCreditCard2 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        resource.subscriptionJson().getSubscription(subscriptionIdBicCreditCard, PelicanConstants.CONTENT_TYPE);

        // create Map of request parameters and send to "Find Subscriptions"
        final HashMap<String, String> requestParametersMap = new HashMap<>();
        // Create a list of VALID 20 subscription ids
        final String subscriptionIds = "     " + subscriptionIdBicCreditCard + "," + subscriptionIdBicCreditCard2;

        requestParametersMap.put(FieldName.SUBSCRIPTION_IDS.getName(), subscriptionIds);

        // send the request
        final Object entity =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);
        final JSubscriptions subscriptions = (JSubscriptions) entity;

        // Subscriptions are returned in ascending order of ids
        AssertCollector.assertThat("Incorrect Subscription Id1",
            subscriptions.getData().getSubscriptions().get(0).getId(), equalTo(subscriptionIdBicCreditCard),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Subscription Id2",
            subscriptions.getData().getSubscriptions().get(1).getId(), equalTo(subscriptionIdBicCreditCard2),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect block size", subscriptions.getData().getBlockSize(),
            equalTo(DEFAULT_BLOCK_SIZE_WITH_SUBSCRIPTION_IDS), assertionErrorList);
        AssertCollector.assertThat("Incorrect start index", subscriptions.getData().getStartIndex(),
            equalTo(DEFAULT_START_INDEX), assertionErrorList);
        AssertCollector.assertThat("Error should not be produced", subscriptions.getErrors(), equalTo(null),
            assertionErrorList);
        for (int i = 0; i < subscriptions.getData().getSubscriptions().size(); i++) {
            AssertCollector.assertThat("Incorrect owner external key",
                subscriptions.getData().getSubscriptions().get(i).getOwnerExternalKey(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Application Family Id ",
                subscriptions.getData().getSubscriptions().get(i).getApplicationFamilyId(),
                equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing count",
                subscriptions.getData().getSubscriptions().get(i).getBillingOption().getBillingPeriod().getCount(),
                is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Incorrect Billing type",
                subscriptions.getData().getSubscriptions().get(i).getBillingOption().getBillingPeriod().getType(),
                is(notNullValue()), assertionErrorList);
        }
        for (int i = 0; i < subscriptions.getIncluded().getOfferings().size(); i++) {
            AssertCollector.assertThat("Incorrect Subscription Plan usage type",
                subscriptions.getIncluded().getOfferings().get(i).getUsageType(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Subscription Plan support level",
                subscriptions.getIncluded().getOfferings().get(i).getSupportLevel(), is(notNullValue()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Find Subscription by ID from Admin Tool
     */
    @Test(dependsOnMethods = "testInvoiceJobTrigger", priority = 4)
    public void testFindSubscriptionbyIdUI() {

        final FindSubscriptionsPage page = adminToolPage.getPage(FindSubscriptionsPage.class);
        page.doOpenSearch();

        final List<String> subIdList = page.getGrid().getColumnValues("ID");
        final String subId = subIdList.get(0);

        LOGGER.info("Searching by subscription Id: " + subId);
        page.findBySubscriptionId(subId);

        LOGGER.info("Subscription Id Key is " + page.getDetails().getId());

        // Validate that the selected subscription had the correct Id
        AssertCollector.assertThat("Subscription not returned", page.getDetails().getId(), equalTo(subId),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Compare Platform version in Platform and Triggers match
     */
    @Test(priority = 5)
    public void testComparePlatformVersionInTriggersAndPlatform() {

        // Validate Platform version is consistent in Platform and Triggers
        AssertCollector.assertThat("Platform version missmatch between Platform and Triggers",
            apiDocPage.getPlatformVersion(PelicanConstants.PLATFORM),
            equalTo(apiDocPage.getPlatformVersion(PelicanConstants.TRIGGERS)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test CSE Change Notification for Order Complete
     */
    @Test(dependsOnMethods = "testSubmitAndProcessPurchaseOrder", priority = 6)
    public void testChangeNotificationOrderComplete() {
        final User user = new User();
        user.setId(buyerUser.getId());
        user.setExternalKey(buyerUser.getExternalKey());
        user.setApplicationFamily(getEnvironmentVariables().getApplicationFamily());
        pelicanEventsConsumer.waitForEvents(5000);
        cseHelper.helperToFindChangeNotificationWithStateForPurchaseOrder(eventsList, purchaseOrderIdForBicCreditCard,
            PelicanConstants.CREATED, OrderState.CHARGED, user, false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for single line item with single promotion
     *
     * Validate price totals of the shopping cart items
     */
    @Test(priority = 7)
    public void getPriceQuoteSingleLineItemSuccess() {
        final PriceQuotes priceQuotes = new PriceQuotes();
        final PriceQuoteData requestData =
            HelperForPriceQuote.getPriceQuoteData(getEnvironmentVariables().getUserExternalKey(), true);

        // Adding additional fees
        final List<JAdditionalFee> additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        final List<JLineItem> lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(getBasicOfferingUsPerpetualDvdActive().getIncluded().getPrices().get(0).getId(),
            additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        final Shipping shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);
        priceQuotes.setData(requestData);
        final List<JPromotionReference> promotionReferencesList = new ArrayList<>();

        requestData.setPromotionReferences(promotionReferencesList);

        final PriceQuotes priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        final Map<Offerings, List<JPromotion>> offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(getBasicOfferingUsPerpetualDvdActive(), null);

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, "offering", null, getEnvironmentVariables(), false, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store external key for Subscription Plan
     *
     * @result Response with only price from requested store with active offers
     */
    @Test(priority = 8)
    public void testGetOfferingsByProductLineAndStoreExternalKey() {
        final HashMap<String, String> params = new HashMap<>();
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(),
            bicOfferings.getOfferings().get(0).getProductLine());
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUk());
        final Offerings offerings = resource.offerings().getOfferings(params);

        // Assertions
        AssertCollector.assertThat("Unable to get offerings", offerings.getOfferings().size(), greaterThanOrEqualTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Billing plans was not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(bicOfferings.getOfferings().get(0).getProductLine()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests adding a new feature and verifying audit log
     *
     * @param status
     */
    @Test(priority = 9)
    public void testAddFeature() {

        // Navigate to the add feature page and add a feature
        final String featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);
        addFeaturePage.addFeature(featureTypeName, featureName, featureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        final String featureId = featureDetailPage.getFeatureId();

        HelperForCommonAssertionsOfFeature.commonAssertionsOfFeature(featureDetailPage, featureId, featureName,
            featureName, featureTypeName, PelicanConstants.YES, assertionErrorList);

        // Verify the CREATE feature audit data
        FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, null, featureTypeId, null, featureName, null,
            featureName, null, null, Action.CREATE, userId, null, null, PelicanConstants.TRUE.toLowerCase(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case to change the ec status to review, block and then to unverified when the subscription status
     * is active
     *
     * @param ecStatus - ec status which has to be updated with
     */
    @Test(priority = 10)
    public void testSubscriptionECStatusChangeOnActiveStatus() {

        final DateFormat dateFormat = DateTimeUtils.getSimpleDateFormatInPST(PelicanConstants.DB_DATE_FORMAT);
        final Date date = new Date();
        final CseECNotification ecNotification = new CseECNotification();
        final String utcDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT);

        // Constructing the ec status and posting to the cse channel
        final String[] ecUserArray = { ecUser };
        ecNotification.setEcOxygenIds(ecUserArray);
        ecNotification.setECStatus(ECStatus.REVIEW.getName());
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

        // set subscription offering object
        subscriptionOffering.setId(bicOfferings.getOfferings().get(0).getId());
        productLine.setCode(bicOfferings.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, bicSubscriptionId, user,
                subscriptionOffering, true, assertionErrorList),
            PelicanConstants.UPDATED, bicSubscriptionId, true, assertionErrorList);

        cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForNonSherpaMigration(eventsList,
            bicSubscriptionId, PelicanConstants.UPDATED, assertionErrorList);

        AssertCollector.assertThat("Incorrect ec status in bic subscription", bicSubscription.getExportControlStatus(),
            equalTo(ECStatus.REVIEW.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription activity for the subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUBSCRIPTION_ACTIVITY_CHANGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription memo for the subscription",
            bicSubscriptionActivity.getMemo().split("\n")[0], equalTo(ECStatus.REVIEW.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect ec change timestamp in subscription memo",
            bicSubscriptionActivity.getMemo().split("\n")[1].split(" " + PelicanConstants.UTC_TIME_ZONE)[0],
            equalTo(utcDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect bic subscription export control last modified",
            exportControlLastModifiedDate.split(" " + PelicanConstants.UTC_TIME_ZONE)[0], equalTo(utcDate),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
