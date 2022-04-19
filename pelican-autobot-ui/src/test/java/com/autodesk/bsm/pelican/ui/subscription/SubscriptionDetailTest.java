package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.CSEClient;
import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient.FieldName;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscriptions;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.cse.ChangeNotifications;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.AddCreditDaysPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.ChangeExportControlStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditPendingPaymentFlagPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.ReduceSeatsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.Iterables;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This test class tests scenarios related to subscription details.
 *
 * @author Muhammad
 */
public class SubscriptionDetailTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static final String DR_UPLOAD_VALID_DATA = "DRSubscriptions_ValidData.xlsx";
    private static final int SUB_ID_COLUMN = 11;
    private static final int CONTRACT_TERM_COLUMN = 6;
    // private static final int SKU_CODE_COLUMN = 22;
    private UploadUtils uploadUtils;
    private XlsUtils xlsUtils;
    private static final String CONTRACT_TERM_IN_FILE = "Month (1)";
    private static final double daysInMonth = 30.00;
    private String xlsFile;
    private FindSubscriptionsPage findSubscriptionPage;
    private FindSubscriptionsPage findSubscriptionsPage;
    private EditSubscriptionPage editSubscriptionPage;
    private AddCreditDaysPage addCreditDaysPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private EditPendingPaymentFlagPage editPendingPaymentFlagPage;
    private ReduceSeatsPage reduceSeatsPage;
    private ChangeExportControlStatusPage changeExportControlStatusPage;
    private List<ChangeNotificationMessage> eventsList;
    private static final String SUBSCRIPTIONS = "subscriptions";
    private static final String UPDATED = "updated";
    private String PELICANEVENTS_NOTIFICATION_CHANNEL;
    private String bicCommercialSubscriptionIdWithEcAccept;
    private String bicCommercialSubscriptionIdWithEcUnverified;
    private String metaCommercialSubscriptionIdWithEcReview;
    private Offerings bicCommercialOffering2;
    private String bicCommercialPriceId;
    private String bicCommercialPriceId2;
    private String metaCommercialPriceId;
    private String subscriptionIdForEditPendingPaymentFlag;
    private PurchaseOrderUtils purchaseOrderUtils;
    private UserUtils userUtils;
    private HashMap<String, String> userParams;
    private PelicanPlatform resource;
    private PurchaseOrder purchaseOrder;
    private RolesHelper rolesHelper;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private CSEHelper cseHelper;
    private JobsClient jobsResource;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionDetailTest.class.getSimpleName());
    private static final int quantity = 1;
    private HashMap<String, Integer> priceQuantityMap;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static final String CONTENT_TYPE = "application/xml";
    private PromotionUtils promotionUtils;
    private User ebsoUser;
    private User nonEbsoUser;
    private static BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private static boolean isFeatureFlagChanged;
    private static final String discountAmount = "10.00";
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

        userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        xlsUtils = new XlsUtils();
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        editSubscriptionPage = adminToolPage.getPage(EditSubscriptionPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        editPendingPaymentFlagPage = adminToolPage.getPage(EditPendingPaymentFlagPage.class);
        changeExportControlStatusPage = adminToolPage.getPage(ChangeExportControlStatusPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        userParams = new HashMap<>();

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        rolesHelper = new RolesHelper(getEnvironmentVariables());
        priceQuantityMap = new HashMap<>();

        final Offerings bicCommercialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicCommercialPriceId = bicCommercialOffering.getIncluded().getPrices().get(0).getId();

        bicCommercialOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicCommercialPriceId2 = bicCommercialOffering2.getIncluded().getPrices().get(0).getId();

        // submit bic PO with final EC as Accept. submitAndProcessNewAcquisitionPurchaseOrderWithCC method by default
        // submits PO in EC as
        // Accept.
        priceQuantityMap.put(bicCommercialPriceId, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        bicCommercialSubscriptionIdWithEcAccept =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Submit bic PO with final EC as Review
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicCommercialPriceId, buyerUser, quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrder.getId(),
            ECStatus.REVIEW);
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrder.getId(),
            ECStatus.REVIEW);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // Submit bic PO with final EC as unverified
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicCommercialPriceId, buyerUser, quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrder.getId(),
            ECStatus.UNVERIFIED);
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrder.getId(),
            ECStatus.REVIEW);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        bicCommercialSubscriptionIdWithEcUnverified =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // bic non-commercial PO with Accept
        final Offerings bicNonCommercialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.NCM);
        final String bicNonCommercialPriceId = bicNonCommercialOffering.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(bicNonCommercialPriceId, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // bic education PO with Accept
        final Offerings bicEducationOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.EDU);
        final String bicEducationPriceId = bicEducationOffering.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(bicEducationPriceId, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // meta commercial PO with Accept
        Offerings metaCommercialOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        metaCommercialPriceId = metaCommercialOffering.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(metaCommercialPriceId, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // meta commercial PO with Review
        metaCommercialOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        metaCommercialPriceId = metaCommercialOffering.getIncluded().getPrices().get(0).getId();
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            metaCommercialPriceId, buyerUser, quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrder.getId(),
            ECStatus.REVIEW);
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrder.getId(),
            ECStatus.REVIEW);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        metaCommercialSubscriptionIdWithEcReview =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // meta commercial PO with Block
        metaCommercialOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        metaCommercialPriceId = metaCommercialOffering.getIncluded().getPrices().get(0).getId();
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            metaCommercialPriceId, buyerUser, quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrder.getId(),
            ECStatus.UNVERIFIED);
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.CHARGE, purchaseOrder.getId(), ECStatus.BLOCK);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // meta non commercial PO with Accept
        final Offerings metaNonCommercialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.NCM);
        final String metaNonCommercialPriceId = metaNonCommercialOffering.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(metaNonCommercialPriceId, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // meta education PO with Accept
        final Offerings metaEducationOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.EDU);
        final String metaEducationPriceId = metaEducationOffering.getIncluded().getPrices().get(0).getId();
        priceQuantityMap.clear();
        priceQuantityMap.put(metaEducationPriceId, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        // get purchase order after charge
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        // start cse events
        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        PELICANEVENTS_NOTIFICATION_CHANNEL = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        final String notificationConsKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationConsSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationConsKey, notificationConsSecret);
        final String ACCESS_TOKEN = authClient.getAuthToken();
        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());

        // Initialize Consumer
        pelicanEventsConsumer =
            cseHelper.initializeConsumer(brokerUrl, PELICANEVENTS_NOTIFICATION_CHANNEL, ACCESS_TOKEN);

        // uploadUtils = adminToolPage.getPage(UploadUtils.class);
        // xlsFile = uploadUtils.getFilePath(DR_UPLOAD_VALID_DATA);
        // final Map<XlsCell, String> columnValuesMap = new HashMap<>();
        // columnValuesMap.put(new XlsCell(1, SKU_CODE_COLUMN), getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        // xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        addCreditDaysPage = adminToolPage.getPage(AddCreditDaysPage.class);

        // creating EBSO user for edit payment flag tests
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        ebsoUser = userUtils.createAssignRole(userParams,
            Lists.newArrayList(Role.EBSO.getValue(), Role.BANKING_ADMIN.getValue(), Role.ADMIN.getValue(),
                Role.APPLICATION_MANAGER.getValue(), Role.QA_ONLY.getValue()),
            adminToolPage, getEnvironmentVariables());

        // creating Non EBSO user for edit payment flag tests
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        nonEbsoUser = userUtils.createAssignRole(userParams, rolesHelper.getNonEbsoRoleList(), adminToolPage,
            getEnvironmentVariables());

        // bic commercial PO with Accept for edit pending payment flag tests
        purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            bicCommercialPriceId2, buyerUser, 1);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING, purchaseOrder.getId(),
            ECStatus.ACCEPT);
        subscriptionIdForEditPendingPaymentFlag = resource.purchaseOrder().getById(purchaseOrder.getId()).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Set migrate subscription feature flag and reduce seats to true
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, true);

        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REDUCE_SEATS_FEATURE_FLAG, true);

        // Creating Regular Promotions for BIC subscriptions non Store wide % Discount
        final JPromotion nonStorPercentDiscountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicCommercialOffering2), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, discountAmount, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        LOGGER.info("nonStorPercentDiscountPromo id :" + nonStorPercentDiscountPromo.getData().getId());

    }

    /**
     * Driver Close
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {

        // If feature flag is changed in this class, turn it OFF
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REDUCE_SEATS_FEATURE_FLAG, false);
        }

        cseHelper.terminateConsumer(pelicanEventsConsumer, PELICANEVENTS_NOTIFICATION_CHANNEL, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
    }

    /**
     * This method tests following: Number of days displayed after adding on Subscription Details Page. how discounted
     * days reflects on the price of next billing charge event created for credit days under Subscription Activity
     */
    @Test(enabled = false)
    public void verifyAddCreditDaysAndNextBillingUnitPriceForPendingMigrationSubscription() throws IOException {
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        xlsFile = uploadUtils.getFilePath(DR_UPLOAD_VALID_DATA);
        // Generate random subscriptionId to put in xlxs file to create new
        // subscription
        final String subscriptionIdInFile = String.valueOf(RandomUtils.nextInt(99999999));
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();
        columnValuesMap.put(new XlsCell(1, SUB_ID_COLUMN), subscriptionIdInFile);
        columnValuesMap.put(new XlsCell(1, CONTRACT_TERM_COLUMN), CONTRACT_TERM_IN_FILE);
        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils.uploadSubscriptions(adminToolPage, DR_UPLOAD_VALID_DATA);

        // generate random number between 2 and 10 for days
        final Random random = new Random();
        final String daysAdded = String.valueOf(random.nextInt(10) + 2);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        findSubscriptionPage.getSubscriptionByExternalKey("MIG-" + subscriptionIdInFile);
        findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();

        final double creditDaysBeforeAddingOrRemoving =
            Integer.parseInt(subscriptionDetailPage.getValueByField("Days Credited"));

        final String nextBillingCharge = subscriptionDetailPage.getNextBillingCharge().split(" ")[0];
        LOGGER
            .info("Next Billing Charge without currency, taxes and fees in subscription Details: " + nextBillingCharge);
        final double nextBillingChargeBeforeDis = Double.parseDouble(nextBillingCharge);
        addCreditDaysPage.addOrRemoveCreditDaysInSub(subscriptionDetailPage.getId(), daysAdded);
        addCreditDaysPage.addOrRemoveCreditDaysButton();
        final double creditDaysAfterAddingOrRemoving = Double.parseDouble(subscriptionDetailPage.getDaysCredited());

        AssertCollector.assertThat("Days Credited are not correct", creditDaysAfterAddingOrRemoving,
            equalTo(creditDaysBeforeAddingOrRemoving + Double.parseDouble(daysAdded)), assertionErrorList);

        final double oneDayDiscount = nextBillingChargeBeforeDis / daysInMonth;
        final String nextBillingChargeAfterDis =
            String.format("%.2f", (nextBillingChargeBeforeDis - (oneDayDiscount * creditDaysAfterAddingOrRemoving)));
        AssertCollector.assertThat("Next Billing Charge is not calculated accurately",
            subscriptionDetailPage.getNextBillingCharge().split(" ")[0], equalTo(nextBillingChargeAfterDis),
            assertionErrorList);

        final GenericGrid subscriptionActivity = adminToolPage.getPage(GenericGrid.class);
        final int size = subscriptionActivity.getColumnValues("Grant").size();
        AssertCollector.assertThat("Event is not created correctly under Subscription Activity",
            (subscriptionActivity.getColumnValues("Grant").get(size - 1)), equalTo(daysAdded + " Days"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests if auto renew enabled flag is set when a subscription is uploaded.
     */
    @Test(enabled = false)
    public void testAutoRenewalFlagForPendingMigration() throws IOException {
        // Generate random subscriptionId to put in xls file to create new
        // subscription
        xlsFile = uploadUtils.getFilePath(DR_UPLOAD_VALID_DATA);
        final String subscriptionIdInFile = String.valueOf(RandomUtils.nextInt(99999999));
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();
        columnValuesMap.put(new XlsCell(1, SUB_ID_COLUMN), subscriptionIdInFile);
        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // upload subscription
        uploadUtils.uploadSubscriptions(adminToolPage, DR_UPLOAD_VALID_DATA);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        final String subscriptionExternalKey = "MIG-" + subscriptionIdInFile;
        // Find subscription by external key
        findSubscriptionPage.getSubscriptionByExternalKey(subscriptionExternalKey);
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();

        AssertCollector.assertThat("Subscription external key is not correct", subscriptionDetailPage.getExternalKey(),
            equalTo(subscriptionExternalKey), assertionErrorList);

        AssertCollector.assertThat("Subscription status is not correct", subscriptionDetailPage.getStatus(),
            equalTo(Status.PENDING_MIGRATION.toString()), assertionErrorList);
        AssertCollector.assertThat("Auto-renew enabled flag should be set",
            subscriptionDetailPage.getAutoRenewEnabled(), equalTo("true"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests a ec status of a subscription can be changed manually and after that following scenarios
     * need to be verified: i) Export control status in details after a change ii) activity under subscription activity
     * iii) requestor under subscription activity iv) Memo v) ec change date under subscription Activity
     */
    @Test(dataProvider = "ecStatus")
    public void testChangeEcStatusManuallyWithGcsoRole(final ECStatus ecStatus, final String priceId) {
        final HashMap<String, Integer> priceIds = new HashMap<>();
        priceIds.put(priceId, 1);

        // submit a purchase order
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceIds, false, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();

        if (priceId.equals(metaCommercialPriceId)) {
            final PurchaseOrder purchaseOrderForMeta = resource.purchaseOrder().getById(purchaseOrderId);

            purchaseOrderUtils.fulfillRequest(purchaseOrderForMeta, FulfillmentCallbackStatus.Created);
        }
        findSubscriptionPage.findSubscriptionByPoId(purchaseOrderId);
        findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();
        final String subscriptionId = subscriptionDetailPage.getSubscriptionId();
        final String exportControlStatus = subscriptionDetailPage.getExportControlStatus();
        changeExportControlStatusPage = subscriptionDetailPage.clickOnChangeExportControlStatusLink();
        AssertCollector.assertThat(
            "Current EC status on change export control status page is not same as export control status on subscription detail page. ",
            exportControlStatus, equalTo((changeExportControlStatusPage.getCurrentEcStatus()).toUpperCase()),
            assertionErrorList);
        final String note =
            ecStatus + RandomStringUtils.randomAlphabetic(10) + " " + RandomStringUtils.randomAlphabetic(10);

        changeExportControlStatusPage.helperToChangeEcStatus(ecStatus, note);
        subscriptionDetailPage.submit(TimeConstants.ONE_SEC);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        // get subscription activity
        final SubscriptionActivity subscriptionActivity =
            Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity(), null);

        AssertCollector.assertThat("Export Control status is not changed",
            subscriptionDetailPage.getExportControlStatus(), equalTo((ecStatus.getDisplayName()).toUpperCase()),
            assertionErrorList);
        if (subscriptionActivity != null) {
            AssertCollector
                .assertThat("Memo is not correct as expected", subscriptionActivity.getMemo().toLowerCase(),
                    equalTo((ecStatus.getDisplayName() + ".\n"
                        + subscriptionDetailPage.getExportControlStatusLastModified() + ".\n" + note).toLowerCase()),
                    assertionErrorList);
        }
        if (subscriptionActivity != null) {
            AssertCollector.assertThat("User is not correct who changed the ec status",
                getEnvironmentVariables().getUserName(), equalTo(subscriptionActivity.getRequestor().split(" ")[0]),
                assertionErrorList);
        }
        if (subscriptionActivity != null) {
            AssertCollector.assertThat("Activity under Subscription Activity is not correct", "EC_CHANGE",
                equalTo(subscriptionActivity.getActivity()), assertionErrorList);
        }

        // fetching date from cse events
        String publishDate = null;
        pelicanEventsConsumer.waitForEvents(12000);
        eventsList = pelicanEventsConsumer.getNotifications();

        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
        ChangeNotifications cseEvent;
        for (final ChangeNotificationMessage message : eventsList) {
            if (message.getData().contains(SUBSCRIPTIONS)) {
                cseEvent = CSEClient.parseCSENotificationResponse(message.getData());
                if (cseEvent.getData().getRelationships().getChangeNotificationSubscription().getLink().getData()
                    .getId().equals(subscriptionId)
                    && cseEvent.getData().getAttributes().getChangeType().equals(UPDATED)
                    && cseEvent.getData().getType().equals(PelicanConstants.CHANGE_NOTIFICATIONS)) {
                    publishDate = cseEvent.getData().getAttributes().getPublishDate();
                    LOGGER.info("Publish Date of an event:" + publishDate);
                }
            }
        }

        // formating date from cse
        String cseDateExcludingT = null;
        if (null != publishDate) {
            cseDateExcludingT = publishDate.replaceAll("T", " ");
        }
        String cseDateExcludingZ = null;
        if (null != cseDateExcludingT) {
            cseDateExcludingZ = cseDateExcludingT.replaceAll("Z", "");
        }
        String cseDate = null;
        if (null != cseDateExcludingZ) {
            cseDate = cseDateExcludingZ.replaceAll("-", "/");
        }
        final Date expectedSubscriptionActivityDate = DateTimeUtils.convertStringToDate(cseDate, "yyyy/MM/dd HH:mm:ss");

        // formatting date from subscription Details under subscription
        // activity
        Date actualSubscriptionActivityDate = null;
        if (subscriptionActivity != null) {
            actualSubscriptionActivityDate = DateTimeUtils.convertStringToDate(
                subscriptionActivity.getDate().replaceAll(" UTC", ""), PelicanConstants.DB_DATE_FORMAT);
        }

        AssertCollector.assertThat("Date under subscription activity is not same as published in cse events",
            actualSubscriptionActivityDate, equalTo(expectedSubscriptionActivityDate), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies that a new subscription is created with EC Status = Active(5) and Ec last updated = null
     */
    @Test(dataProvider = "dataForSubscriptionECStatus")
    public void testSubscriptionECStatus(final String subscriptionId, final String ecStatus) {

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        AssertCollector.assertThat("Subscription EC status is not correct on subscription detail page. ",
            subscriptionDetailPage.getExportControlStatus(), equalTo(ecStatus), assertionErrorList);

        AssertCollector.assertThat("Subscription EC last modified date should be null on subscription detail page. ",
            subscriptionDetailPage.getExportControlStatusLastModified(), not(PelicanConstants.HIPHEN),
            assertionErrorList);

        final Object apiResponse = resource.subscription().getById(subscriptionId);
        if (apiResponse instanceof HttpError) {
            final HttpError httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Subscription.class), assertionErrorList);
        } else {
            final Subscription subscription = (Subscription) apiResponse;

            AssertCollector.assertThat("Subscription EC status is not correct in get subscription by id api.",
                subscription.getExportControlStatus(), equalTo(ecStatus), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the permissions of changing EC status. If user doesn't has a permission of ec change
     * status than he cannot see a link of change export control status under related actions
     */
    @Test
    public void testEditEcStatusWithoutGcsoRole() {
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_GCSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userUtils.createAssignRoleAndLoginUser(userParams, rolesHelper.getNonGcsoRoleList(), adminToolPage,
            getEnvironmentVariables());

        findSubscriptionPage.findBySubscriptionId(bicCommercialSubscriptionIdWithEcAccept);
        AssertCollector.assertThat("With role of Non-GCSO Change Export Control Status can be seen",
            subscriptionDetailPage.checkExportControlStatusLink(), equalTo(false), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        adminToolPage.logout();
        adminToolPage.login();
    }

    /**
     * This method verifies that subscription EC status is present when EC Status feature flag is set to true
     */

    @Test
    public void testSubscriptionECStatusWithFeatureFlagOn() {
        // Find subscription by id
        findSubscriptionPage.findBySubscriptionId(bicCommercialSubscriptionIdWithEcAccept);
        final SubscriptionDetailPage subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        boolean isExportControlStatusFound = false;
        try {
            subscriptionDetailPage.getExportControlStatus();
            isExportControlStatusFound = true;
            LOGGER.info("Element found");
        } catch (final NoSuchElementException e) {
            LOGGER.info("Element not found");
        }
        AssertCollector.assertThat("Subscription EC status should be present when export control feature flag is on",
            isExportControlStatusFound, equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "ecStatus")
    public Object[][] getEcStatus() {
        return new Object[][] { { ECStatus.REVIEW, bicCommercialPriceId },
                { ECStatus.UNVERIFIED, bicCommercialPriceId }, { ECStatus.BLOCK, metaCommercialPriceId } };
    }

    /**
     * This test case verifies credit days and next billing unit price with multiple quantity. Api validations are part
     * of this test to avoid duplicate code in 4 api test classes.
     *
     * @param priceList
     * @param quantity
     * @param offeringType
     */
    @Test(dataProvider = "addCreditDaysAndNextBillingUnitPrice")
    public void verifyAddCreditDaysAndNextBillingUnitPrice(final String priceList, final int quantity,
        final OfferingType offeringType, final String creditDays) {
        // New offering is required so that we can easily find the newly created subscription with find
        // subscriptions api.
        final Offerings offering = subscriptionPlanApiUtils.addSubscriptionPlan(priceList, offeringType,
            BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        priceQuantityMap.clear();
        priceQuantityMap.put(priceId, quantity);

        if (offeringType == OfferingType.META_SUBSCRIPTION) {
            purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap,
                false, buyerUser);
        } else {
            purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        }

        // Fulfill meta request
        if (offeringType == OfferingType.META_SUBSCRIPTION) {
            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        }

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Get subscription before adding credit days
        final JSubscription subscription =
            resource.subscriptionJson().getSubscription(subscriptionId, PelicanConstants.CONTENT_TYPE);

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        if (quantity < 2 || offeringType == OfferingType.META_SUBSCRIPTION) {
            AssertCollector.assertThat("Reduce seat link is displayed for the subscription which has quantity one",
                subscriptionDetailPage.isReduceSeatsLinkDisplayed(), equalTo(false), assertionErrorList);
        } else {
            AssertCollector.assertThat("Reduce seat link is displayed for the subscription which has quantity one",
                subscriptionDetailPage.isReduceSeatsLinkDisplayed(), equalTo(true), assertionErrorList);
        }

        final BigDecimal nextBillingChargeBeforeCreditDays =
            new BigDecimal(subscription.getData().getNextBillingPriceAmount());
        LOGGER.info("Next Billing Charge before adding credit days: " + nextBillingChargeBeforeCreditDays);

        final BigDecimal nextBillingUnitChargeBeforeCreditDays =
            nextBillingChargeBeforeCreditDays.divide(new BigDecimal(quantity));
        LOGGER.info("Next Billing Unit Charge before adding credit days: " + nextBillingUnitChargeBeforeCreditDays);

        LOGGER.info("Validating subscription apis before adding credit days.");
        commonAssertionsForNextBillingUnitPrice(offering, subscriptionId,
            String.valueOf(nextBillingUnitChargeBeforeCreditDays), String.valueOf(nextBillingChargeBeforeCreditDays));

        // Add credit days
        addCreditDaysPage.addOrRemoveCreditDaysInSub(subscriptionDetailPage.getId(), creditDays);
        addCreditDaysPage.addOrRemoveCreditDaysButton();
        final String creditDaysAfterAdding = subscriptionDetailPage.getDaysCredited();

        AssertCollector.assertThat("Days Credited are not correct", creditDaysAfterAdding, equalTo(creditDays),
            assertionErrorList);

        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        // Get days in next billing cycle, it could be 28, 29, 30 or 31 depending on the month.
        final Double daysInNextRenewalBillingCycle = DateTimeUtils.getDaysInBillingCycle(nextBillingDate,
            DateTimeUtils.addMonthsToDate(nextBillingDate, PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

        // Get number of days to charge after adding credit days
        final int daysToCharge = daysInNextRenewalBillingCycle.intValue() - Integer.valueOf(creditDays);

        // Calculate next billingUnitPrice and nextBillingPrice
        BigDecimal expectedNextBillingUnitPriceAfteraddingCreditDays =
            new BigDecimal(0).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedNextBillingChargeAfterCreditDays = new BigDecimal(0).setScale(2, RoundingMode.HALF_EVEN);
        if (!(Integer.parseInt(creditDays) == 0)) {
            if (daysToCharge > 0) {
                final BigDecimal oneDayDiscount = nextBillingUnitChargeBeforeCreditDays
                    .divide(new BigDecimal(daysInNextRenewalBillingCycle), MathContext.DECIMAL64);
                expectedNextBillingUnitPriceAfteraddingCreditDays = oneDayDiscount
                    .multiply(new BigDecimal(daysToCharge), MathContext.DECIMAL64).setScale(2, RoundingMode.HALF_EVEN);
                expectedNextBillingChargeAfterCreditDays = (expectedNextBillingUnitPriceAfteraddingCreditDays)
                    .multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_EVEN);
            }
        } else {
            expectedNextBillingUnitPriceAfteraddingCreditDays = nextBillingUnitChargeBeforeCreditDays;
            expectedNextBillingChargeAfterCreditDays = nextBillingChargeBeforeCreditDays;
        }
        LOGGER.info("Next Billing charge after adding credit days: " + expectedNextBillingChargeAfterCreditDays);
        LOGGER.info(
            "Next Billing unit price after adding credit days: " + expectedNextBillingUnitPriceAfteraddingCreditDays);

        if (Integer.parseInt(creditDays) == 0) {
            AssertCollector.assertFalse("Credit activity should not be captured under Subscription Activity",
                (Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getActivity()
                    .equals(PelicanConstants.CREDIT)),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Event is not created correctly under Subscription Activity",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getGrant(),
                equalTo(creditDays + " Days"), assertionErrorList);
        }

        LOGGER.info("Validating subscription apis after adding credit days.");
        commonAssertionsForNextBillingUnitPrice(offering, subscriptionId,
            String.valueOf(expectedNextBillingUnitPriceAfteraddingCreditDays),
            String.valueOf(expectedNextBillingChargeAfterCreditDays));

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case verifies next billing unit price in all 4 subscription apis. This test is part of ui test class to
     * avoid duplicate code in 4 api test classes and soon we will add NextBillingUnitPrice in admin tool.
     *
     * @param promotionType
     * @param discountAmount
     * @param discountPercent
     * @param numberOfBillingCycles
     * @param store
     * @param priceList
     * @param quantity
     * @param offeringType
     */
    @Test(dataProvider = "nextBillingUnitPriceWithPromotion")
    public void verifyNextBillingUnitPriceWithPromotion(final PromotionType promotionType, final String discountAmount,
        final String discountPercent, final Integer numberOfBillingCycles, final JStore store, final String priceList,
        final int quantity, final OfferingType offeringType) {
        // New offering is required so that we can easily find the newly created subscription with find
        // subscriptions api.

        final Offerings offering = subscriptionPlanApiUtils.addSubscriptionPlan(priceList, offeringType,
            BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final Double offerPrice = Double.parseDouble(offering.getIncluded().getPrices().get(0).getAmount());

        LOGGER.info("Offer price " + offerPrice);

        JPromotion createdPromo = null;
        // expectedNextBillingUnitPrice will be equal to offerPrice when numberOfBillingCycles = 1.
        String expectedNextBillingUnitPrice = String.format("%.2f", offerPrice);

        if (promotionType == PromotionType.DISCOUNT_AMOUNT) {
            createdPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(store),
                Lists.newArrayList(offering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null,
                discountAmount, DateTimeUtils.getUTCFutureExpirationDate(), null, null, numberOfBillingCycles, null,
                null);
            // if promotion is valid for more than 1 billing cycle, only then discount will be applied to
            // next billing unit price.
            if (numberOfBillingCycles > 1) {
                expectedNextBillingUnitPrice = String.format("%.2f", offerPrice - Double.parseDouble(discountAmount));
            }
        }

        if (promotionType == PromotionType.DISCOUNT_PERCENTAGE) {
            createdPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(store),
                Lists.newArrayList(offering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                discountPercent, null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, numberOfBillingCycles,
                null, null);
            // if promotion is valid for more than 1 billing cycle, only then discount will be applied to
            // next billing unit price.
            if (numberOfBillingCycles > 1) {
                expectedNextBillingUnitPrice =
                    String.format("%.2f", offerPrice - offerPrice * Double.parseDouble(discountPercent) * .01);
            }
        }

        boolean isMeta = false;

        if (offeringType == OfferingType.META_SUBSCRIPTION) {
            isMeta = true;
        }
        purchaseOrder = purchaseOrderUtils
            .getFulfilledPurchaseOrderWithPromotion(createdPromo, offering, 1, buyerUser, isMeta, quantity).get(0);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        LOGGER.info("Next Billing unit price " + expectedNextBillingUnitPrice);

        final String expectedNextBillingCharge =
            String.format("%.2f", Double.parseDouble(expectedNextBillingUnitPrice) * quantity);

        commonAssertionsForNextBillingUnitPrice(offering, subscriptionId, expectedNextBillingUnitPrice,
            expectedNextBillingCharge);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies that quantity to reduce value is 0 on subscription detail page when DB has null value for
     * this field.
     */
    @Test
    public void testSubscriptionDetailForQuantityToReduceWithNullValue() {
        final String sqlQuery = PelicanDbConstants.SELECT_SQL_FOR_SUBSCRIPTION_QUANTITY_TO_REDUCE_NULL
            + getEnvironmentVariables().getAppFamilyId();
        final List<Map<String, String>> resultList = DbUtils.selectQuery(sqlQuery, getEnvironmentVariables());
        String subscriptionId = null;
        if (resultList.size() > 0) {
            subscriptionId = resultList.get(0).get("ID");
        } else {
            final String updateQuery =
                PelicanDbConstants.UPDATE_SQL_TO_SET_SUBSCRIPTION_QUANTITY_TO_REDUCE_NULL + subscriptionId;
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
        }
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Incorrect Subscription Id.", subscriptionDetailPage.getId(),
            equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity to reduce value.", subscriptionDetailPage.getQuantityToReduce(),
            equalTo(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests quantity to reduce filed and subscription activity after reducing the seats following need to
     * be tested - quantity to reduce should be set to given value - subscription activity should be captured including
     * requester and notes - date should be same in subscription activity as it is shown in change notification events
     *
     */
    @Test
    public void verifyImpactOfReudceSeatsOnSubscriptionDetailPage() {
        final String note = "Reducing seats notes";
        final int quantity = 4;
        final int valueToReduceSeats = 2;
        priceQuantityMap.clear();
        priceQuantityMap.put(bicCommercialPriceId2, quantity);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        eventsList.clear();
        reduceSeatsPage = subscriptionDetailPage.clickOnReduceSeatsLink();
        reduceSeatsPage.clickOnReduceSeats(Integer.toString(valueToReduceSeats), note);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        AssertCollector.assertThat("Incorrect Quantity to reduce value.", subscriptionDetailPage.getQuantityToReduce(),
            equalTo(valueToReduceSeats), assertionErrorList);

        final int totalRecordsInSubscriptionActivity = subscriptionDetailPage.getSubscriptionActivity().size();
        LOGGER.info("Total records found under subscription activity: " + totalRecordsInSubscriptionActivity);
        AssertCollector.assertThat("Requestor is no found under subscription activity after reducing the seats",
            subscriptionDetailPage.getSubscriptionActivity().get(totalRecordsInSubscriptionActivity - 1).getRequestor(),
            equalTo(getEnvironmentVariables().getUserName() + " (" + getEnvironmentVariables().getUserId() + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Memo is not found under subscription activity after reducing the seats",
            subscriptionDetailPage.getSubscriptionActivity().get(totalRecordsInSubscriptionActivity - 1).getMemo(),
            equalTo(note), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies all valid errors message for errors which includes value should be greater than 0 cannot be
     * greater or equal to quantity value cannot be negative
     */
    @Test
    public void testErrorScenariosForReduceSeats() {
        priceQuantityMap.clear();
        priceQuantityMap.put(bicCommercialPriceId2, 3);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        reduceSeatsPage = subscriptionDetailPage.clickOnReduceSeatsLink();
        final String valueToReduceSeatsWithGreaterThanQuantity = "100";
        reduceSeatsPage.clickOnReduceSeats(valueToReduceSeatsWithGreaterThanQuantity, null);

        final String negativeValue = "-1";
        final String zeroValue = "0";
        AssertCollector.assertThat("Error is not generated for reduce seat value which is greater than quantity.",
            reduceSeatsPage.getErrorMessageFromFormHeader(),
            equalTo("Invalid reduce seats quantity : " + valueToReduceSeatsWithGreaterThanQuantity),
            assertionErrorList);
        reduceSeatsPage.clickOnReduceSeats(zeroValue, null);
        AssertCollector.assertThat("Error is not generated for reduce seat of 0 value.",
            reduceSeatsPage.getErrorMessageFromFormHeader(), equalTo("Invalid reduce seats quantity : " + zeroValue),
            assertionErrorList);
        reduceSeatsPage.clickOnReduceSeats(negativeValue, null);
        AssertCollector.assertThat("Error is not generated for reduce seat negative value",
            reduceSeatsPage.getErrorMessageFromFormHeader(),
            equalTo("Invalid reduce seats quantity : " + negativeValue), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies Edit Pending Payment flag link for subscription which are in Active / Delinquent state for
     * EBSO User only
     *
     * @param subscriptionStatus status the subscription should be set too
     * @param isEditPendingPaymentLinkVisible should Edit Pending Payment Flag be displayed
     */
    @Test(dataProvider = "subscriptionStatusForEditPendingPayment")
    public void testAuditLogForEditPendingPaymentFlagByEBSOUserForAllSubscriptionStatus(final String subscriptionStatus,
        final boolean isEditPendingPaymentLinkVisible) {

        Boolean foundActivity = false;
        try {
            adminToolPage.logout();
            adminToolPage.login(getEnvironmentVariables().getApplicationFamily(), ebsoUser.getName(),
                getEnvironmentVariables().getPassword());

            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForEditPendingPaymentFlag);
            editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
            editSubscriptionPage.editASubscription(null, null, subscriptionStatus, subscriptionStatus);
            // Navigating again to detail page in case editASubscription page returns an error.
            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForEditPendingPaymentFlag);

            if (isEditPendingPaymentLinkVisible) {
                AssertCollector.assertTrue(
                    "Edit Pending Payment Flag link for EBSO user is not shown for subscription:"
                        + subscriptionIdForEditPendingPaymentFlag + " in state: " + subscriptionStatus,
                    subscriptionDetailPage.isEditPendingPaymentFlagPresent(), assertionErrorList);
                editPendingPaymentFlagPage = subscriptionDetailPage.clickOnEditPendingPaymentFlagLink();

                AssertCollector.assertTrue("Edit Pending Payment is set to Flase",
                    editPendingPaymentFlagPage.getPendingPaymentFlagStatus(), assertionErrorList);
                final String activityNotes = "Flag modified by AUTOBOT on "
                    + DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT);
                subscriptionDetailPage =
                    editPendingPaymentFlagPage.checkAndEnterNotesInPendingPaymentTextAreaAndSubmit(activityNotes);

                final List<SubscriptionActivity> subscriptionActivityList =
                    subscriptionDetailPage.getSubscriptionActivity();

                for (final SubscriptionActivity subscriptionActivity : subscriptionActivityList) {
                    if (subscriptionActivity.getActivity()
                        .contentEquals(PelicanConstants.SUBSCRIPTION_ACTIVITY_FOR_PENDING_PAYMENT_FLAG_UPDATE)
                        && subscriptionActivity.getMemo().equalsIgnoreCase(activityNotes)) {
                        AssertCollector.assertThat(
                            "Expected Requestor " + subscriptionActivity.getRequestor() + " but found "
                                + ebsoUser.getName(),
                            subscriptionActivity.getRequestor(), containsString(ebsoUser.getName()),
                            assertionErrorList);
                        AssertCollector.assertThat(
                            "Expected Notes " + activityNotes + " but found " + subscriptionActivity.getMemo(),
                            subscriptionActivity.getMemo(), equalTo(activityNotes), assertionErrorList);
                        foundActivity = true;
                    }
                }
                AssertCollector.assertTrue(
                    "Couldnt find the Subscription Activity for Edit Pending Payment Flag for Subscription: "
                        + subscriptionIdForEditPendingPaymentFlag,
                    foundActivity, assertionErrorList);

            } else {
                AssertCollector.assertFalse(
                    "Edit Pending Payment Flag link for EBSO user is shown for subscription:"
                        + subscriptionIdForEditPendingPaymentFlag + " in status: " + subscriptionStatus,
                    subscriptionDetailPage.isEditPendingPaymentFlagPresent(), assertionErrorList);
            }
        } finally {
            adminToolPage.logout();
            adminToolPage.login();
        }

        if (foundActivity) {

            // Edit next billing date for renewal
            subscriptionDetailPage =
                findSubscriptionsPage.findBySubscriptionId(subscriptionIdForEditPendingPaymentFlag);

            // Don't try o update next billing date only if it is already set to previous day otherwise Edit
            // subscription page will throw an error.
            if (!subscriptionDetailPage.getNextBillingDate().equals(DateTimeUtils.getNowPlusDays(-1))) {
                editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
                editSubscriptionPage.editASubscription(DateTimeUtils.getNowPlusDays(-1), null, null, null);
            }

            // Run renewal job
            final JsonSubscriptionId jsonSubscriptionId = new JsonSubscriptionId();
            jsonSubscriptionId.setSubscriptionId(subscriptionIdForEditPendingPaymentFlag);
            jobsResource.subscriptionRenewals(jsonSubscriptionId, getEnvironmentVariables());

            // give 5 sec wait for the renewal to complete
            Util.waitInSeconds(TimeConstants.MINI_WAIT);

            subscriptionDetailPage.refreshPage();
            AssertCollector.assertThat(
                "Expected Pending Payment flag value to be \"False\" after renewal but found: "
                    + subscriptionDetailPage.getPendingPaymentFlag(),
                "false", equalTo(subscriptionDetailPage.getPendingPaymentFlag()), assertionErrorList);

            // Not doing common assertion here, because this gives 2 results and we
            // are interested only in CANCELLED to EXPIRED audit log
            final List<Map<String, AttributeValue>> items =
                DynamoDBUtil.subscription(subscriptionIdForEditPendingPaymentFlag);
            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

            Boolean updateSubscriptionAuditLogFound = false;
            for (final AuditLogEntry auditLogEntry : auditLogEntries) {
                LOGGER.info(auditLogEntry.toString());
                final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();
                LOGGER.info(auditData.toString());
                if (auditData.containsKey(PelicanConstants.PENDING_PAYMENT)) {
                    if (auditLogEntry.getAction().equals(Action.UPDATE.toString())
                        && auditData.get(PelicanConstants.PENDING_PAYMENT).getOldValue()
                            .equals(PelicanConstants.TRUE_VALUE.toString())) {
                        updateSubscriptionAuditLogFound = true;
                    }
                }
            }

            AssertCollector.assertTrue(
                "Update Subscription \"Edit Pending Payment flag\" - Audit Log not found for Subscription id : "
                    + subscriptionIdForEditPendingPaymentFlag,
                updateSubscriptionAuditLogFound, assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies Edit Pending Payment flag link is not displayed for subscription which are in Active /
     * Delinquent state for Non EBSO User only
     */
    @Test
    public void testEditPendingPaymentFlagforNonEBSOUser() {
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForEditPendingPaymentFlag);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(null, null, SubscriptionStatus.ACTIVE.name(), null);

        try {
            adminToolPage.logout();
            adminToolPage.login(getEnvironmentVariables().getApplicationFamily(), nonEbsoUser.getName(),
                getEnvironmentVariables().getPassword());

            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionIdForEditPendingPaymentFlag);
            AssertCollector.assertFalse(
                "Edit Pending Payment Flag link for NON EBSO user is shown for subscription:"
                    + subscriptionIdForEditPendingPaymentFlag + " in Active status",
                subscriptionDetailPage.isEditPendingPaymentFlagPresent(), assertionErrorList);
        } finally {
            adminToolPage.logout();
            adminToolPage.login();
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies invisibility of reduce link for subscription with quantity one
     */
    @Test
    public void testInvisibilityOfReduceSeatLinkForSubscriptionWithQuantityOne() {
        priceQuantityMap.clear();
        priceQuantityMap.put(bicCommercialPriceId2, 1);
        purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        AssertCollector.assertThat("Reduce seat link is displayed for the subscription which has quantity one",
            subscriptionDetailPage.isReduceSeatsLinkDisplayed(), equalTo(false), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test the nextBilling price if seats are reduced. Following tests is verified by this method. - after
     * reducing the seats - reduce seats with promotion(discount and percentage). - reduce seats with credit days -
     * reduce seats with promotion and credit days(promotion discount take precedence over credit days discount)
     *
     * @param promotionType
     * @param promotionDisount
     * @param isCreditDays
     * @param days
     */
    @Test(dataProvider = "nextBillingPriceWithPromotionAndCreditDays")
    public void verifyNextBillingCharge(final PromotionType promotionType, final String promotionDisount,
        final boolean isCreditDays, final String days) {
        final int quantity = 6;
        final int valueToReduceSeats = 4;
        final String note = "reducing seats";
        final Double offerPrice =
            Double.parseDouble(bicCommercialOffering2.getIncluded().getPrices().get(0).getAmount());
        LOGGER.info("Offer price " + offerPrice);

        JPromotion createdPromo;
        if (promotionType == PromotionType.DISCOUNT_AMOUNT) {
            createdPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicCommercialOffering2), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, promotionDisount, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
            purchaseOrder = purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(createdPromo,
                bicCommercialOffering2, 1, buyerUser, false, quantity).get(0);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        } else if (promotionType == PromotionType.DISCOUNT_PERCENTAGE) {
            createdPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
                Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicCommercialOffering2),
                promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, promotionDisount, null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
            purchaseOrder = purchaseOrderUtils
                .getFulfilledPurchaseOrderWithPromotion(createdPromo, bicCommercialOffering2, 1, null, false, quantity)
                .get(0);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        } else {
            final HashMap<String, Integer> priceIds = new HashMap<>();
            priceIds.put(bicCommercialPriceId2, quantity);
            // submit a purchase order
            purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceIds, false, buyerUser);
        }

        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        if (isCreditDays) {
            addCreditDaysPage.addOrRemoveCreditDaysInSub(subscriptionId, String.valueOf(days));
            subscriptionDetailPage = addCreditDaysPage.addOrRemoveCreditDaysButton();
        } else {
            subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        }
        // get nextBillingPrice from subscription detail page
        reduceSeatsPage = subscriptionDetailPage.clickOnReduceSeatsLink();
        reduceSeatsPage.clickOnReduceSeats(Integer.toString(valueToReduceSeats), note);
        final String actualNextBillingChargeOnSubscriptionDetailPage =
            ((subscriptionDetailPage.getNextBillingCharge()).split(" ")[0]).replace(",", "");
        LOGGER
            .info("Next Billing price on Subscription detail page " + actualNextBillingChargeOnSubscriptionDetailPage);
        final String nextBillingDate = subscriptionDetailPage.getNextBillingDate();

        String expectedNextBillingCharge = "";
        Double unitPrice = 0.00;
        final int quantityForRenewalAfterReducingSeats = quantity - valueToReduceSeats;
        // promotion takes precedence over credit days discount.
        if (promotionType == PromotionType.DISCOUNT_AMOUNT) {
            unitPrice = offerPrice - Double.valueOf(promotionDisount);
            expectedNextBillingCharge = String.format("%.2f", unitPrice * quantityForRenewalAfterReducingSeats);
        }
        if (promotionType == PromotionType.DISCOUNT_PERCENTAGE) {
            unitPrice = offerPrice - offerPrice * Double.valueOf(promotionDisount) / 100.00;
            expectedNextBillingCharge = String.format("%.2f", unitPrice * quantityForRenewalAfterReducingSeats);

        }
        if (promotionType == null && !isCreditDays) {
            expectedNextBillingCharge = String.format("%.2f", offerPrice * quantityForRenewalAfterReducingSeats);
            LOGGER.info("Expected next billing price after reducing the seats: " + expectedNextBillingCharge);
        }

        if (promotionType == null && isCreditDays) {
            // Get days in next billing cycle, it could be 28, 29, 30 or 31 depending on the month.
            final double daysInNextRenewalBillingCycle = DateTimeUtils.getDaysInBillingCycle(nextBillingDate,
                DateTimeUtils.addMonthsToDate(nextBillingDate, PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

            final double remainingDaysAfterDiscount = (daysInNextRenewalBillingCycle - Double.valueOf(days));
            unitPrice = offerPrice;
            final BigDecimal oneDayDiscount =
                new BigDecimal(unitPrice).divide(new BigDecimal(daysInNextRenewalBillingCycle), MathContext.DECIMAL64);
            final BigDecimal priceForRemainingDays =
                oneDayDiscount.multiply(new BigDecimal(remainingDaysAfterDiscount), MathContext.DECIMAL64).setScale(2,
                    RoundingMode.HALF_UP);
            expectedNextBillingCharge =
                (priceForRemainingDays.multiply(new BigDecimal(quantity - valueToReduceSeats))).toString();
        }
        LOGGER.info("Expected next billing price after promo discount amount and reducing the seats is: "
            + expectedNextBillingCharge);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        // get nextBillingPrice in xml response
        final Subscription subscriptionXml = resource.subscription().getById(subscriptionId);
        final String actualNextBillingPriceAmountInXmlResponse = subscriptionXml.getNextBillingPriceAmount();
        LOGGER.info("Next Billing price on get subscription by id in xml response "
            + actualNextBillingPriceAmountInXmlResponse);

        // get nextBillingPrice in json response
        final JSubscription subscriptionJson =
            resource.subscriptionJson().getSubscription(subscriptionId, PelicanConstants.CONTENT_TYPE);
        final String actualNextBillingPriceAmountInJsonResponse =
            subscriptionJson.getData().getNextBillingPriceAmount();
        LOGGER.info("Next Billing price on get subscription by id in json response "
            + actualNextBillingPriceAmountInJsonResponse);

        AssertCollector.assertThat("Next Billing price is not correct on subscription detail page",
            actualNextBillingChargeOnSubscriptionDetailPage, equalTo(expectedNextBillingCharge), assertionErrorList);
        AssertCollector.assertThat("Next Billing price is not correct in json response",
            actualNextBillingPriceAmountInJsonResponse, equalTo(expectedNextBillingCharge), assertionErrorList);
        AssertCollector.assertThat("Next Billing price is not correct in xml response",
            actualNextBillingPriceAmountInXmlResponse, equalTo(expectedNextBillingCharge), assertionErrorList);

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);
        // Submit subscription renewal purchase order

        purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser, listOfSubscriptions, false,
            PaymentType.CREDIT_CARD, null, true);
        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat("Reduce to seats is not set to 0 after the renewal",
            subscriptionDetailPage.getQuantityToReduce(), equalTo(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the fields on subscription details Page. - id - subscription plan - subscription offer - user -
     * quantity - quantity to reduce - status - auto renew enable - pending payment - next billing price id - store -
     * payment profile (if it is credit card then last four digits of credit card will be shown)
     *
     * @param paymentType
     */
    @Test(dataProvider = "paymentType")
    public void testFieldsOnSubscriptionDetails(final PaymentType paymentType) {
        final HashMap<String, Integer> priceIds = new HashMap<>();
        priceIds.put(bicCommercialPriceId2, quantity);
        // submit a purchase order
        if (paymentType.equals(PaymentType.CREDIT_CARD)) {
            purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceIds, false, buyerUser);
        } else {
            purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceIds, false, buyerUser);
        }
        final String storeExternalKey = purchaseOrder.getStoreExternalKey();
        final String storeId = purchaseOrder.getStoreId();
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final String storedPaymentProfileId =
            purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId();
        // sourceOffering1.getIncluded().getBillingPlans().get(0).getName()
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Subscription id is not correct", subscriptionDetailPage.getId(),
            equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat("Subscription plan is not correct",
            subscriptionDetailPage.getSubscriptionPlan().split(" ")[0],
            equalTo(bicCommercialOffering2.getOfferings().get(0).getName()), assertionErrorList);
        AssertCollector.assertThat("Subscription offer is not correct",
            subscriptionDetailPage.getSubscriptionOffer().split(" ")[0],
            equalTo(bicCommercialOffering2.getIncluded().getBillingPlans().get(0).getName()), assertionErrorList);
        AssertCollector.assertThat("User is not correct", subscriptionDetailPage.getUser().split(" ")[0],
            equalTo(buyerUser.getName()), assertionErrorList);
        AssertCollector.assertThat("Quantity is not correct is not correct", subscriptionDetailPage.getQuantity(),
            equalTo(quantity), assertionErrorList);
        AssertCollector.assertThat("Quantity To Reduce is not correct", subscriptionDetailPage.getQuantityToReduce(),
            equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Status is not correct", subscriptionDetailPage.getStatus(),
            equalTo(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Auto renew enabled is not correct ", subscriptionDetailPage.getAutoRenewEnabled(),
            equalTo("true"), assertionErrorList);
        AssertCollector.assertThat("Pending payment is not correct ", subscriptionDetailPage.getPendingPaymentFlag(),
            equalTo("false"), assertionErrorList);
        AssertCollector.assertThat("Next billing price id is not correct ",
            subscriptionDetailPage.getNextBillingPriceId(),
            equalTo(bicCommercialOffering2.getIncluded().getPrices().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Store is not correct", subscriptionDetailPage.getStore(),
            equalTo(storeExternalKey + " (" + storeId + ")"), assertionErrorList);

        if (paymentType == PaymentType.CREDIT_CARD) {
            final PaymentProfile paymentProfile = resource.paymentProfile().getPaymentProfile(storedPaymentProfileId);
            final String creditCardType = paymentProfile.getCreditCard().getCreditCardType();
            final String creditCardNumber = paymentProfile.getCreditCard().getCreditCardNumber();
            AssertCollector.assertThat("Payment Profile is not correct with payment type: " + paymentType,
                subscriptionDetailPage.getPaymentProfile(),
                equalTo(storedPaymentProfileId + "-" + creditCardType + "-" + creditCardNumber), assertionErrorList);
        } else {
            AssertCollector.assertThat("Payment Profile is not correct with payment type: " + paymentType,
                subscriptionDetailPage.getPaymentProfile(),
                equalTo(storedPaymentProfileId + "-" + PaymentType.PAYPAL.getValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method tests when subscription is trial then field payment type will be null.
     */
    @Test
    public void testFieldPaymentProfileFieldForTrialSubscription() {
        final Offerings subscriptionPlan =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        final String subscriptionOfferExternalKey =
            subscriptionPlan.getIncluded().getBillingPlans().get(0).getExternalKey();

        final Subscription trialSubscription = resource.subscription().add("User" + RandomStringUtils.randomNumeric(4),
            subscriptionOfferExternalKey, Currency.USD);
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(trialSubscription.getId());
        AssertCollector.assertThat("Store is not correct", subscriptionDetailPage.getStore(),
            equalTo(getStoreExternalKeyUs() + " (" + getStoreIdUs() + ")"), assertionErrorList);
        AssertCollector.assertThat("Payment profile is not correct", subscriptionDetailPage.getPaymentProfile(),
            equalTo("-"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies that quantity to reduce field is displayed on subscription detail page when quantity = 1.
     */
    @Test
    public void testQuantityToReduceWithOneSubscriptionQuantity() {
        final List<Map<String, String>> resultList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_SUBSCRIPTION_WITH_QUANTITY_IS_ONE,
                getEnvironmentVariables().getAppFamilyId()), getEnvironmentVariables());
        final String subscriptionId = resultList.get(0).get("ID");

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Incorrect Subscription Id.", subscriptionDetailPage.getId(),
            equalTo(subscriptionId), assertionErrorList);
        boolean isQuantityToReduceFound = false;
        try {
            subscriptionDetailPage.getQuantityToReduce();
            isQuantityToReduceFound = true;
        } catch (final NoSuchElementException e) {
            e.printStackTrace();
        }
        AssertCollector.assertTrue("Quantity to reduce field is not displayed.", isQuantityToReduceFound,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test data required to very correct next billing price
     *
     * @return Object[][]
     */
    @DataProvider(name = "paymentType")
    public Object[][] getTestDataForPaymnetType() {
        return new Object[][] { { PaymentType.CREDIT_CARD }, { PaymentType.PAYPAL } };
    }

    /**
     * Test data for Edit Payment Flag visibility for EBSO
     *
     * @return Object[][]
     */
    @DataProvider(name = "subscriptionStatusForEditPendingPayment")
    public Object[][] getSubscriptionStatusForEditPendingPayment() {
        return new Object[][] { { SubscriptionStatus.ACTIVE.name(), true },
                { SubscriptionStatus.CANCELLED.name(), false } };
    }

    /**
     * Test data for Subscription Renewal with Edit Pending Payment of Subscription, by EBSO
     *
     * @return Object[][]
     */
    @DataProvider(name = "subscriptionForActiveDeliquentStatus")
    public Object[][] getSubscriptionForActiveDeliquentStatus() {
        return new Object[][] { { "0" }, { "3" } };
    }

    /**
     * Test data required to very correct next billing price
     *
     * @return Object[][]
     */
    @DataProvider(name = "nextBillingPriceWithPromotionAndCreditDays")
    public Object[][] getTestDataForNextBillingPriceWithPromotionAndCreditDays() {
        return new Object[][] {
                // promotionType, promotionDisount, creditDays, days
                { PromotionType.DISCOUNT_AMOUNT, "10", false, null }, { null, null, true, "10" },
                { PromotionType.DISCOUNT_AMOUNT, "10", true, "10" }, { null, null, false, null } };
    }

    /**
     * Data provider for testSubscriptionECStatus method dataFor
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForSubscriptionECStatus")
    public Object[][] getTestDataForSubscriptionECStatus() {
        return new Object[][] { { bicCommercialSubscriptionIdWithEcAccept, ECStatus.ACCEPT.getDisplayName() },
                { bicCommercialSubscriptionIdWithEcUnverified, ECStatus.UNVERIFIED.getDisplayName() },
                { metaCommercialSubscriptionIdWithEcReview, ECStatus.REVIEW.getDisplayName() } };
    }

    @DataProvider(name = "addCreditDaysAndNextBillingUnitPrice")
    public Object[][] getTestDataForForBicAndMeta() {
        return new Object[][] { { getPricelistExternalKeyUs(), 1, OfferingType.BIC_SUBSCRIPTION, "35" },
                { getPricelistExternalKeyUk(), 1, OfferingType.META_SUBSCRIPTION, "3" } };
    }

    @DataProvider(name = "nextBillingUnitPriceWithPromotion")
    public Object[][] getTestDataForNextBillingUnitPriceWithPromotion() {
        return new Object[][] {
                // promotionType, discountAmount, discountPercent,numberOfBillingCycles, store, priceList,
                // quantity, offeringType {
                { PromotionType.DISCOUNT_PERCENTAGE, null, "7", 5, getStoreUs(), getPricelistExternalKeyUs(), 3,
                        OfferingType.BIC_SUBSCRIPTION },
                { PromotionType.DISCOUNT_AMOUNT, "15", null, 1, getStoreUs(), getPricelistExternalKeyUs(), 1,
                        OfferingType.META_SUBSCRIPTION }, };
    }

    /**
     * This method contains common assertions for NextBillingUnitPrice and NextBillingPrice.
     *
     * @param offering
     * @param subscriptionId
     * @param nextBillingUnitPrice
     * @param nextBillingPriceAmount
     */
    private void commonAssertionsForNextBillingUnitPrice(final Offerings offering, final String subscriptionId,
        final String nextBillingUnitPrice, final String nextBillingPriceAmount) {
        // validate Find Subscription by id xml api
        Object apiResponse = resource.subscription().getById(subscriptionId);
        if (apiResponse instanceof HttpError) {
            final HttpError httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request for Find subscription by id XML. Status " + httpError.getStatus(),
                apiResponse, instanceOf(Subscription.class), assertionErrorList);
        } else {
            final Subscription subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("NextBillingUnitPriceAmount is not correct in get subscription by id XML api.",
                subscription.getNextBillingUnitPriceAmount(), equalTo(nextBillingUnitPrice), assertionErrorList);
            AssertCollector.assertThat("NextBillingPriceAmount is not correct in get subscription by id XML api.",
                subscription.getNextBillingPriceAmount(), equalTo(nextBillingPriceAmount), assertionErrorList);
        }

        // validate Find Subscription by id json api
        final JSubscription subscription =
            resource.subscriptionJson().getSubscription(subscriptionId, PelicanConstants.CONTENT_TYPE);

        AssertCollector.assertThat("NextBillingUnitPriceAmount is not correct in get subscription by id JSON api.",
            subscription.getData().getNextBillingUnitPriceAmount(), equalTo(nextBillingUnitPrice), assertionErrorList);
        AssertCollector.assertThat("NextBillingPriceAmount is not correct in get subscription by id JSON api.",
            subscription.getData().getNextBillingPriceAmount(), equalTo(nextBillingPriceAmount), assertionErrorList);

        final Map<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.PLAN_ID.getName(), offering.getOfferings().get(0).getId());
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), buyerUser.getExternalKey());

        // validate Find Subscriptions json api
        apiResponse = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);
        if (apiResponse instanceof HttpError) {
            final HttpError httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request for Find subscriptions JSON api. Status " + httpError.getStatus(),
                apiResponse, instanceOf(JSubscriptions.class), assertionErrorList);
        } else {
            final JSubscriptions subscriptions = (JSubscriptions) apiResponse;
            AssertCollector.assertThat("NextBillingUnitPriceAmount is not correct in get subscriptions JSON api.",
                subscriptions.getData().getSubscriptions().get(0).getNextBillingUnitPriceAmount(),
                equalTo(nextBillingUnitPrice), assertionErrorList);

            AssertCollector.assertThat("NextBillingPriceAmount is not correct in get subscriptions JSON api.",
                subscriptions.getData().getSubscriptions().get(0).getNextBillingPriceAmount(),
                equalTo(nextBillingPriceAmount), assertionErrorList);
        }

        // validate Find Subscriptions XML api
        apiResponse = resource.subscriptions().getSubscriptions(requestParametersMap, CONTENT_TYPE);
        if (apiResponse instanceof HttpError) {
            final HttpError httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request for Find subscriptions XML api. Status " + httpError.getStatus(),
                apiResponse, instanceOf(JSubscriptions.class), assertionErrorList);
        } else {
            final Subscriptions subscriptions = (Subscriptions) apiResponse;
            AssertCollector.assertThat("NextBillingUnitPriceAmount is not correct in get subscriptions XML api.",
                subscriptions.getSubscriptions().get(0).getNextBillingUnitPriceAmount(), equalTo(nextBillingUnitPrice),
                assertionErrorList);
            AssertCollector.assertThat("NextBillingPriceAmount is not correct in get subscriptions XML api.",
                subscriptions.getSubscriptions().get(0).getNextBillingPriceAmount(), equalTo(nextBillingPriceAmount),
                assertionErrorList);
        }
    }
}
