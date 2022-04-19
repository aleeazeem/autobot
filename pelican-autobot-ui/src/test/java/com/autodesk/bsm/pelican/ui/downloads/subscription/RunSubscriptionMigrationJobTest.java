package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionMigrationJobStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.EditSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.Iterables;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a test class which will test running the migration job and migrating multiple subscriptions at the same time
 * in the Admin tool.
 *
 * @author yerragv, Muhammad
 */
public class RunSubscriptionMigrationJobTest extends SeleniumWebdriver {

    private String pelicanEventsNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private static PelicanPlatform resource;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private UploadSubscriptionMigrationPage uploadSubscriptionMigrationPage;
    private String jobName;
    private AdminToolPage adminToolPage;
    private SubscriptionMigrationJobDetailPage subscriptionMigrationJobDetailPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private WorkInProgressReportResultPage workInProgressReportResultPage;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private Offerings subscriptionOffering1;
    private Offerings subscriptionOffering2;
    private Offerings metaSubscriptionOffering2;
    private String subscriptionOfferingExternalKey1;
    private String metaSubscriptionOfferingExternalKey1;
    private String subscriptionOfferingExternalKey2;
    private String subscriptionOfferingExternalKey3;
    private String subscriptionOfferingExternalKey4;
    private String subscriptionOfferingId1;
    private String subscriptionOfferingId2;
    private String offerExternalKey1;
    private String metaOfferExternalKey1;
    private String offerExternalKey2;
    private String metaOfferExternalKey2;
    private String metaSubscriptionOfferingExternalKey2;
    private String offerExternalKey3;
    private String offerExternalKey4;
    private String offerId1;
    private String metaOfferId1;
    private String offerId2;
    private String metaOfferId2;
    private String sourcePriceId;
    private String metaSourcePriceId;
    private String targetPriceId;
    private String metaTargetPriceId;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String subscriptionId1;
    private String metaSubscriptionId1;
    private String metaSubscriptionOfferingId1;
    private String subscriptionId2;
    private String metaSubscriptionId2;
    private String metaSubscriptionOfferingId2;
    private String subscriptionId3;
    private String metaSubscriptionId3;
    private String subscriptionId4;
    private String metaSubscriptionId4;
    private String subscriptionId5;
    private FindSubscriptionsPage subscriptionPage;
    private EditSubscriptionPage editSubscriptionPage;
    private RolesHelper rolesHelper;
    private JProductLine productLine;
    private boolean isNonOfferingManagerUserLoggedIn;
    private SubscriptionOffering subscriptionOffering;
    private String jobId;
    private static final String FILE_NAME = "UploadSubscriptionMigration.xlsx";
    private static final int EXPECTED_COUNT_OF_RECORDS = 4;
    private static final int EXPECTED_COUNT_OF_FAILED__RECORDS = 1;
    private static final String SUB_MIGRATED = "SUB_MIGRATED";
    private static final String SUBSCRIPTION_MEMO = "Migration Job Id:";
    private static final String OLD_PRICE_ID = "Old Price Id:";
    private static final String NEW_PRICE_ID = "New Price Id:";
    private static final String SELECT_QUERY =
        "select id,count(*) from subscription_migration_job where status=4 and app_family_id=2001";
    private static final String UPDATE_QUERY_STATUS_TO_RUNNING_VALIDATIONS =
        "update subscription_migration_job set status= 1 where status = 4 and APP_FAMILY_ID=2001";
    private static final String UPDATE_QUERY_STATUS_TO_VALIDATION_SUCCEEDED =
        "update subscription_migration_job set status= 3 where id = ";
    private static final String FIELD_NAME = "count(*)";
    private static final String ID_FIELD_NAME = "id";
    private static final Logger LOGGER = LoggerFactory.getLogger(RunSubscriptionMigrationJobTest.class.getSimpleName());
    private static final String SELECT_JOB_ID_SQL =
        "select id from subscription_migration_job where APP_FAMILY_ID = 2001 and status = 5 order by id asc limit 1";
    private Offerings sourceOffering;
    private Offerings targetOffering;
    private static PromotionUtils promotionUtils;
    private static String fileName;
    private static final String DOWNLOAD_FILE_FORMAT = ".xlsx";
    private static final String FILE_NAME_ERROR_DOWNLOAD = "migration_errors_";

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        uploadSubscriptionMigrationPage = adminToolPage.getPage(UploadSubscriptionMigrationPage.class);
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        subscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        editSubscriptionPage = adminToolPage.getPage(EditSubscriptionPage.class);
        workInProgressReportResultPage = adminToolPage.getPage(WorkInProgressReportResultPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        promotionUtils = new PromotionUtils(getEnvironmentVariables());

        rolesHelper = new RolesHelper(getEnvironmentVariables());
        isNonOfferingManagerUserLoggedIn = false;

        // create two bic offerings
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        subscriptionOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        subscriptionOfferingId1 = subscriptionOffering1.getOfferings().get(0).getId();
        LOGGER.info("subscriptionOfferingId1:" + subscriptionOfferingId1);
        offerExternalKey1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();
        offerId1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getId();
        subscriptionOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey2 = subscriptionOffering2.getOfferings().get(0).getExternalKey();
        subscriptionOfferingId2 = subscriptionOffering2.getOfferings().get(0).getId();
        LOGGER.info("subscriptionOfferingId2:" + subscriptionOfferingId2);
        offerExternalKey2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();
        offerId2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getId();

        sourcePriceId = subscriptionOffering1.getIncluded().getPrices().get(0).getId();
        targetPriceId = subscriptionOffering2.getIncluded().getPrices().get(0).getId();

        // Create meta offerings
        final Offerings metaSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        metaSubscriptionOfferingExternalKey1 = metaSubscriptionOffering1.getOfferings().get(0).getExternalKey();
        metaSubscriptionOfferingId1 = metaSubscriptionOffering1.getOfferings().get(0).getId();
        LOGGER.info("meta subscriptionOfferingId1:" + metaSubscriptionOfferingId1);
        metaOfferExternalKey1 = metaSubscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();
        metaOfferId1 = metaSubscriptionOffering1.getIncluded().getBillingPlans().get(0).getId();
        metaSubscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        metaSubscriptionOfferingExternalKey2 = metaSubscriptionOffering2.getOfferings().get(0).getExternalKey();
        metaSubscriptionOfferingId2 = metaSubscriptionOffering2.getOfferings().get(0).getId();
        LOGGER.info("meta subscriptionOfferingId2:" + metaSubscriptionOfferingId2);
        metaOfferExternalKey2 = metaSubscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();
        metaOfferId2 = metaSubscriptionOffering2.getIncluded().getBillingPlans().get(0).getId();

        metaSourcePriceId = metaSubscriptionOffering1.getIncluded().getPrices().get(0).getId();
        metaTargetPriceId = metaSubscriptionOffering2.getIncluded().getPrices().get(0).getId();

        // Submit purchase order to create subscriptions
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(sourcePriceId, 1);

        final PurchaseOrder purchaseOrder1 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        subscriptionId1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id1: " + subscriptionId1);

        final PurchaseOrder purchaseOrder2 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        subscriptionId2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id2: " + subscriptionId2);

        final PurchaseOrder purchaseOrder3 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        subscriptionId3 =
            purchaseOrder3.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id3: " + subscriptionId3);

        final PurchaseOrder purchaseOrder4 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        subscriptionId4 =
            purchaseOrder4.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id4: " + subscriptionId4);

        // Edit the 2nd subscription status to delinquent
        editASubscriptionStatus(subscriptionId2, Status.DELINQUENT.toString());

        // Edit the 3rd subscription status to cancelled
        editASubscriptionStatus(subscriptionId3, Status.CANCELLED.toString());

        // Edit the 4th subscription status to pending migration
        editASubscriptionStatus(subscriptionId4, Status.PENDING_MIGRATION.toString());

        // Submit purchase order to create meta subscriptions
        priceMap.clear();
        priceMap.put(metaSourcePriceId, 1);

        PurchaseOrder metaPurchaseOrder1 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(metaPurchaseOrder1, FulfillmentCallbackStatus.Created);

        metaPurchaseOrder1 = resource.purchaseOrder().getById(metaPurchaseOrder1.getId());
        metaSubscriptionId1 = metaPurchaseOrder1.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Meta Subscription id1: " + metaSubscriptionId1);

        PurchaseOrder metaPurchaseOrder2 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(metaPurchaseOrder2, FulfillmentCallbackStatus.Created);
        metaPurchaseOrder2 = resource.purchaseOrder().getById(metaPurchaseOrder2.getId());
        metaSubscriptionId2 = metaPurchaseOrder2.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Meta Subscription id2: " + metaSubscriptionId2);

        PurchaseOrder metaPurchaseOrder3 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(metaPurchaseOrder3, FulfillmentCallbackStatus.Created);

        metaPurchaseOrder3 = resource.purchaseOrder().getById(metaPurchaseOrder3.getId());
        metaSubscriptionId3 = metaPurchaseOrder3.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Meta Subscription id3: " + metaSubscriptionId3);

        PurchaseOrder metaPurchaseOrder4 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        // fulfill the request, since it is Meta
        purchaseOrderUtils.fulfillRequest(metaPurchaseOrder4, FulfillmentCallbackStatus.Created);

        metaPurchaseOrder4 = resource.purchaseOrder().getById(metaPurchaseOrder4.getId());
        metaSubscriptionId4 = metaPurchaseOrder4.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();
        LOGGER.info("Meta Subscription id4: " + metaSubscriptionId4);

        // Edit the 2nd meta subscription status to delinquent
        editASubscriptionStatus(metaSubscriptionId2, Status.DELINQUENT.toString());

        // Edit the 3rd meta subscription status to cancelled
        editASubscriptionStatus(metaSubscriptionId3, Status.CANCELLED.toString());

        // Edit the 4th meta subscription status to pending migration
        editASubscriptionStatus(metaSubscriptionId4, Status.PENDING_MIGRATION.toString());

        // Create 3 subscriptions for the source subscription plan offer

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
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
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

        // Create stores, offerings and subscriptions for the negative test cases
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        final JStore store1 = storeApiUtils.addStore(Status.ACTIVE, Country.DE, Currency.USD, null, false);
        final String externalKeyOfPriceList1 = store1.getIncluded().getPriceLists().get(0).getExternalKey();

        final JStore store2 = storeApiUtils.addStore(Status.ACTIVE, Country.DE, Currency.USD, null, false);
        final String externalKeyOfPriceList2 = store2.getIncluded().getPriceLists().get(0).getExternalKey();

        final Offerings subscriptionOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList1,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey3 = subscriptionOffering3.getOfferings().get(0).getExternalKey();
        offerExternalKey3 = subscriptionOffering3.getIncluded().getBillingPlans().get(0).getExternalKey();
        final Offerings subscriptionOffering4 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList2,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey4 = subscriptionOffering4.getOfferings().get(0).getExternalKey();
        final String subscriptionOfferingId2 = subscriptionOffering2.getOfferings().get(0).getId();
        LOGGER.info("Subscription offering id2: " + subscriptionOfferingId2);
        offerExternalKey4 = subscriptionOffering4.getIncluded().getBillingPlans().get(0).getExternalKey();

        final String sourcePriceId1 = subscriptionOffering3.getIncluded().getPrices().get(0).getId();

        // Submit purchase order to create subscriptions
        priceMap.clear();
        priceMap.put(sourcePriceId1, 1);

        final PurchaseOrder purchaseOrder5 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        subscriptionId5 =
            purchaseOrder5.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id5: " + subscriptionId5);

        final JStore store3 = storeApiUtils.addStore(Status.ACTIVE, Country.MX, Currency.USD, null, false);
        final String externalKeyOfPriceList3 = store3.getIncluded().getPriceLists().get(0).getExternalKey();

        Offerings subscriptionOffering5 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList3,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String subscriptionOfferingId5 = subscriptionOffering5.getOfferings().get(0).getId();

        final JStore store4 = storeApiUtils.addStore(Status.ACTIVE, Country.MX, Currency.USD, null, false);
        final String externalKeyOfPriceList4 = store4.getIncluded().getPriceLists().get(0).getExternalKey();
        Offerings subscriptionOffering6 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList4,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String subscriptionOfferingId6 = subscriptionOffering6.getOfferings().get(0).getId();

        subscriptionOffering5 =
            subscriptionPlanApiUtils.addOfferAndPrices(subscriptionOfferingId5, getPricelistExternalKeyUs());
        subscriptionOffering6 =
            subscriptionPlanApiUtils.addOfferAndPrices(subscriptionOfferingId6, externalKeyOfPriceList2);

        final String sourcePriceId2 = subscriptionOffering5.getIncluded().getPrices().get(0).getId();
        final String sourcePriceId3 = subscriptionOffering5.getIncluded().getPrices().get(1).getId();

        // Submit purchase order to create subscriptions
        priceMap.clear();
        priceMap.put(sourcePriceId2, 1);

        final PurchaseOrder purchaseOrder6 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId6 =
            purchaseOrder6.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id6: " + subscriptionId6);

        priceMap.clear();
        priceMap.put(sourcePriceId3, 1);

        final PurchaseOrder purchaseOrder7 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId7 =
            purchaseOrder7.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription id7: " + subscriptionId7);

        final List<Map<String, String>> countOfRecordsList =
            DbUtils.selectQuery(SELECT_QUERY, getEnvironmentVariables());

        if (Integer.parseInt(countOfRecordsList.get(0).get(FIELD_NAME)) > 0) {
            LOGGER.info("The id(s) of jobs in running file state are: " + countOfRecordsList.get(0).get(ID_FIELD_NAME));
            LOGGER.info("The number of jobs in running state are: " + countOfRecordsList.get(0).get(FIELD_NAME));
            DbUtils.updateQuery(UPDATE_QUERY_STATUS_TO_RUNNING_VALIDATIONS, getEnvironmentVariables());
        }
    }

    /**
     * Driver Close.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
        // Resetting the subscription status
        editASubscriptionStatus(subscriptionId2, Status.ACTIVE.toString());
        editASubscriptionStatus(subscriptionId3, Status.ACTIVE.toString());
        editASubscriptionStatus(subscriptionId4, Status.ACTIVE.toString());
        editASubscriptionStatus(metaSubscriptionId2, Status.ACTIVE.toString());
        editASubscriptionStatus(metaSubscriptionId3, Status.ACTIVE.toString());
        editASubscriptionStatus(metaSubscriptionId4, Status.ACTIVE.toString());
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result - ITestResult would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
    }

    /**
     * This is a test method which will test the run subscription migration.
     *
     * @throws org.apache.http.ParseException
     * @throws IOException
     */
    @Test
    public void testRunSubscriptionMigrationJob() throws org.apache.http.ParseException, IOException {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();
        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            pelicanEventsConsumer.clearNotificationsList();
            subscriptionMigrationJobDetailPage.runAJob();
            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();

            subscriptionMigrationJobDetailPage.refreshPage();
            if (subscriptionMigrationJobDetailPage.getStatus().equals(Status.COMPLETED.toString())) {

                AssertCollector.assertThat("Re-Upload Button should not be present on the page",
                    subscriptionMigrationJobDetailPage.isReUploadButtonPresent(), equalTo(false), assertionErrorList);
                // Validate the CSE and AUM notifications for the migrated subscriptions
                AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

                // set subscription offering object
                subscriptionOffering.setId(subscriptionOfferingId2);
                productLine.setCode(subscriptionOffering2.getOfferings().get(0).getProductLine());
                subscriptionOffering.setJProductLine(productLine);

                validateChangeNotificationForBicSubscription(subscriptionId1);
                validateChangeNotificationForBicSubscription(subscriptionId2);
                validateChangeNotificationForBicSubscription(subscriptionId3);
                validateChangeNotificationForBicSubscription(subscriptionId4);

                AssertCollector.assertThat("Incorrect run by user",
                    subscriptionMigrationJobDetailPage.getRunBy().split(" ")[0],
                    equalTo(PelicanConstants.AUTO_USER_NAME), assertionErrorList);
                AssertCollector.assertThat("Incorrect run date",
                    subscriptionMigrationJobDetailPage.getRunDate().split(" ")[0],
                    equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
                AssertCollector.assertThat("Incorrect file upload job id",
                    subscriptionMigrationJobDetailPage.getFileUploadJobId(), notNullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect triggers job run id",
                    subscriptionMigrationJobDetailPage.getTriggersJobRunId(), notNullValue(), assertionErrorList);
                jobId = subscriptionMigrationJobDetailPage.getId();

                // Click on the triggers job id link and validate the wips in the wip report page
                workInProgressReportResultPage = subscriptionMigrationJobDetailPage.clickOnTriggersJobId();
                AssertCollector.assertThat("Incorrect number of records on the page",
                    workInProgressReportResultPage.getTotalResultsInTheReportPage(), equalTo(EXPECTED_COUNT_OF_RECORDS),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect wip state in the report",
                    workInProgressReportResultPage.getColumnValuesOfState(),
                    everyItem(equalTo(Status.COMPLETE.getDisplayName())), assertionErrorList);
                AssertCollector.assertThat("Incorrect object id in the report",
                    workInProgressReportResultPage.getColumnValuesOfObjectId(),
                    everyItem(isOneOf(subscriptionId1, subscriptionId2, subscriptionId3, subscriptionId4)),
                    assertionErrorList);

                // Validate the subscription events
                validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId1, jobId, sourcePriceId, targetPriceId);
                validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId2, jobId, sourcePriceId, targetPriceId);
                validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId3, jobId, sourcePriceId, targetPriceId);
                validateMigratedSubscriptionAndSubscriptionEvent(subscriptionId4, jobId, sourcePriceId, targetPriceId);

                // Validate the audit logs
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId1,
                    subscriptionOfferingId1, subscriptionOfferingId2, offerId1, offerId2, sourcePriceId, targetPriceId,
                    assertionErrorList);
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId2,
                    subscriptionOfferingId1, subscriptionOfferingId2, offerId1, offerId2, sourcePriceId, targetPriceId,
                    assertionErrorList);
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId3,
                    subscriptionOfferingId1, subscriptionOfferingId2, offerId1, offerId2, sourcePriceId, targetPriceId,
                    assertionErrorList);
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(subscriptionId4,
                    subscriptionOfferingId1, subscriptionOfferingId2, offerId1, offerId2, sourcePriceId, targetPriceId,
                    assertionErrorList);
            } else {
                Assert.fail("Incorrect job status found while running the migration job");
            }

        } else {
            Assert.fail("Incorrect upload job status found after uploading the subscription migration file");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the run subscription migration for the meta subscriptions.
     *
     * @throws org.apache.http.ParseException
     * @throws IOException
     */
    @Test
    public void testRunSubscriptionMigrationJobForMetaSubscriptions()
        throws org.apache.http.ParseException, IOException {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, metaSubscriptionOfferingExternalKey1,
            metaOfferExternalKey1, metaSubscriptionOfferingExternalKey2, metaOfferExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();
        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {

            // Run the subscription migration job
            pelicanEventsConsumer.clearNotificationsList();
            subscriptionMigrationJobDetailPage.runAJob();
            pelicanEventsConsumer.waitForEvents(5000);
            eventsList = pelicanEventsConsumer.getNotifications();

            subscriptionMigrationJobDetailPage.refreshPage();
            if (subscriptionMigrationJobDetailPage.getStatus().equals(Status.COMPLETED.toString())) {

                AssertCollector.assertThat("Re-Upload Button should not be present on the page",
                    subscriptionMigrationJobDetailPage.isReUploadButtonPresent(), equalTo(false), assertionErrorList);
                // Validate the CSE and AUM notifications for the migrated subscriptions
                AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

                // set subscription offering object
                subscriptionOffering.setId(metaSubscriptionOfferingId2);
                productLine.setCode(metaSubscriptionOffering2.getOfferings().get(0).getProductLine());
                subscriptionOffering.setJProductLine(productLine);

                validateChangeNotificationForMetaSubscription(metaSubscriptionId1);
                validateChangeNotificationForMetaSubscription(metaSubscriptionId2);
                validateChangeNotificationForMetaSubscription(metaSubscriptionId3);
                validateChangeNotificationForMetaSubscription(metaSubscriptionId4);

                AssertCollector.assertThat("Incorrect run by user",
                    subscriptionMigrationJobDetailPage.getRunBy().split(" ")[0],
                    equalTo(PelicanConstants.AUTO_USER_NAME), assertionErrorList);
                AssertCollector.assertThat("Incorrect run date",
                    subscriptionMigrationJobDetailPage.getRunDate().split(" ")[0],
                    equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
                AssertCollector.assertThat("Incorrect file upload job id",
                    subscriptionMigrationJobDetailPage.getFileUploadJobId(), notNullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect triggers job run id",
                    subscriptionMigrationJobDetailPage.getTriggersJobRunId(), notNullValue(), assertionErrorList);
                jobId = subscriptionMigrationJobDetailPage.getId();

                // Click on the triggers job id link and validate the wips in the wip report page
                workInProgressReportResultPage = subscriptionMigrationJobDetailPage.clickOnTriggersJobId();
                AssertCollector.assertThat("Incorrect number of records on the page",
                    workInProgressReportResultPage.getTotalResultsInTheReportPage(), equalTo(EXPECTED_COUNT_OF_RECORDS),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect wip state in the report",
                    workInProgressReportResultPage.getColumnValuesOfState(),
                    everyItem(equalTo(Status.COMPLETE.getDisplayName())), assertionErrorList);
                AssertCollector.assertThat("Incorrect object id in the report",
                    workInProgressReportResultPage.getColumnValuesOfObjectId(),
                    everyItem(
                        isOneOf(metaSubscriptionId1, metaSubscriptionId2, metaSubscriptionId3, metaSubscriptionId4)),
                    assertionErrorList);

                // Validate the subscription events
                validateMigratedSubscriptionAndSubscriptionEvent(metaSubscriptionId1, jobId, metaSourcePriceId,
                    metaTargetPriceId);
                validateMigratedSubscriptionAndSubscriptionEvent(metaSubscriptionId2, jobId, metaSourcePriceId,
                    metaTargetPriceId);
                validateMigratedSubscriptionAndSubscriptionEvent(metaSubscriptionId3, jobId, metaSourcePriceId,
                    metaTargetPriceId);
                validateMigratedSubscriptionAndSubscriptionEvent(metaSubscriptionId4, jobId, metaSourcePriceId,
                    metaTargetPriceId);

                // Validate the audit logs
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(metaSubscriptionId1,
                    metaSubscriptionOfferingId1, metaSubscriptionOfferingId2, metaOfferId1, metaOfferId2,
                    metaSourcePriceId, metaTargetPriceId, assertionErrorList);
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(metaSubscriptionId2,
                    metaSubscriptionOfferingId1, metaSubscriptionOfferingId2, metaOfferId1, metaOfferId2,
                    metaSourcePriceId, metaTargetPriceId, assertionErrorList);
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(metaSubscriptionId3,
                    metaSubscriptionOfferingId1, metaSubscriptionOfferingId2, metaOfferId1, metaOfferId2,
                    metaSourcePriceId, metaTargetPriceId, assertionErrorList);
                SubscriptionAuditLogHelper.helperToQueryDynamoDbForSubscriptionUpdateForMigration(metaSubscriptionId4,
                    metaSubscriptionOfferingId1, metaSubscriptionOfferingId2, metaOfferId1, metaOfferId2,
                    metaSourcePriceId, metaTargetPriceId, assertionErrorList);
            } else {
                Assert.fail("Incorrect job status found while running the migration job");
            }

        } else {
            Assert.fail("Incorrect upload job status found after uploading the subscription migration file");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the migration job in failed state
     *
     */
    @Test
    public void testRunSubscriptionMigrationJobInFailedState() {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey3,
            offerExternalKey3, subscriptionOfferingExternalKey4, offerExternalKey4);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();
        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_FAILED.getDisplayName())) {
            final String updateQuery =
                UPDATE_QUERY_STATUS_TO_VALIDATION_SUCCEEDED + subscriptionMigrationJobDetailPage.getId();
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
            subscriptionMigrationJobDetailPage.refreshPage();
            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJob();
            subscriptionMigrationJobDetailPage.refreshPage();
            AssertCollector.assertThat("Incorrect status of the migration job",
                subscriptionMigrationJobDetailPage.getStatus(), equalTo(Status.FAILED.toString()), assertionErrorList);
            AssertCollector.assertThat("Re-Upload Button should not be present on the page",
                subscriptionMigrationJobDetailPage.isReUploadButtonPresent(), equalTo(false), assertionErrorList);
            AssertCollector.assertThat("Incorrect run by user",
                subscriptionMigrationJobDetailPage.getRunBy().split(" ")[0], equalTo(PelicanConstants.AUTO_USER_NAME),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect run date",
                subscriptionMigrationJobDetailPage.getRunDate().split(" ")[0],
                equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
            AssertCollector.assertThat("Incorrect file upload job id",
                subscriptionMigrationJobDetailPage.getFileUploadJobId(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect triggers job run id",
                subscriptionMigrationJobDetailPage.getTriggersJobRunId(), notNullValue(), assertionErrorList);

            // Click on the triggers job id link and validate the wips in the wip report page
            workInProgressReportResultPage = subscriptionMigrationJobDetailPage.clickOnTriggersJobId();
            AssertCollector.assertThat("Incorrect number of records on the page",
                workInProgressReportResultPage.getTotalResultsInTheReportPage(),
                equalTo(EXPECTED_COUNT_OF_FAILED__RECORDS), assertionErrorList);
            AssertCollector.assertThat("Incorrect wip state in the report",
                workInProgressReportResultPage.getColumnValuesOfState(), everyItem(equalTo(Status.FAILED.toString())),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect object id in the report",
                workInProgressReportResultPage.getColumnValuesOfObjectId(), everyItem(isOneOf(subscriptionId5)),
                assertionErrorList);

        } else {
            Assert.fail("Upload subscription migration job status is incorrect");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test whether to determine only Offering Manager is able to run the migration job.
     *
     */
    @Test
    public void testNonOfferingManagerRunMigrationJob() {
        if (!isNonOfferingManagerUserLoggedIn) {
            adminToolPage.login();
            final HashMap<String, String> userParams = new HashMap<>();
            userParams.put(UserParameter.EXTERNAL_KEY.getName(),
                PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY);
            userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
            userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
            // Log in as a non offering-manager user
            final List<String> nonOfferingManagerRoleList = rolesHelper.getNonOfferingManagerRoleList();

            new UserUtils().createAssignRoleAndLoginUser(userParams, nonOfferingManagerRoleList, adminToolPage,
                getEnvironmentVariables());
            isNonOfferingManagerUserLoggedIn = true;
        }

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey3,
            offerExternalKey3, subscriptionOfferingExternalKey4, offerExternalKey4);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        AssertCollector.assertThat("Incorrect error header found on the page",
            uploadSubscriptionMigrationPage.getH2ErrorMessage(), equalTo(PelicanErrorConstants.ERROR_HEADER),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message found on the page",
            uploadSubscriptionMigrationPage.getH3ErrorMessage(), equalTo(PelicanErrorConstants.UNEXPECTED_ERROR),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

        // login back with svc_p_pelican user
        adminToolPage.logout();
        adminToolPage.login();
    }

    /**
     * This method validates that two subscription migration job can not run at same time by validating the error
     * message.
     */
    @Test
    public void testErrorMessageOnRunningTwoSubscriptionMigrationJobsParallely() {

        // Get job id of an existing job
        final String jobId = DbUtils.selectQuery(SELECT_JOB_ID_SQL, "id", getEnvironmentVariables()).get(0);
        String updateJobStatusSQL = "update subscription_migration_job set status = 4 where id ='" + jobId + "'";

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.refreshPage();

        if (subscriptionMigrationJobDetailPage.getStatus()
            .equals(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())) {
            // Set job status to running before running another job
            DbUtils.updateQuery(updateJobStatusSQL, getEnvironmentVariables());

            // Run the subscription migration job
            subscriptionMigrationJobDetailPage.runAJobWithOutPageRefresh();

            subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
            AssertCollector.assertThat("Error message is not correct.",
                subscriptionMigrationJobDetailPage.getErrorOrInfoMessage(),
                equalTo(PelicanErrorConstants.ANOTHER_MIGRATION_JOB_RUNNING), assertionErrorList);

            // Re-set the status of job to completed
            updateJobStatusSQL = "update subscription_migration_job set status = 5 where id ='" + jobId + "'";
            DbUtils.updateQuery(updateJobStatusSQL, getEnvironmentVariables());

        } else {
            // Fail test if validation is not succeeded.
            Assert.fail("Subscription Migration upload validation failed. Please validate your file.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests subscriptions migration from plan to another for success scenario. In order to get file
     * upload Success scenario following should be valid which are given below. After that it test migration of
     * subscription(s) from plan to another after clicking a run button 1) price amount should be same in source and
     * target offer 2) no subscription which has next billing with any promotion should be associated with source plan
     * 3) source and target plans should have active status 4) source and target offers should have active status
     *
     * @result mapping result should be "✔" (ok)
     */
    @Test
    public void testSubscriptionMigrationResultsSucessScenario() {
        final String sourcePriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String targetPriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);
        // add source and target plan for migration
        addSourceAndTargetPlans(100, 100, getPricelistExternalKeyUs(), getPricelistExternalKeyUs(),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), sourcePriceEndDate,
            targetPriceEndDate, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE);

        final String sourcePlanId = sourceOffering.getOfferings().get(0).getId();
        final String sourcePlanName = sourceOffering.getOfferings().get(0).getName();
        final String sourceOfferName = sourceOffering.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId = sourceOffering.getIncluded().getPrices().get(0).getId();
        final String sourceAmount = sourceOffering.getIncluded().getPrices().get(0).getAmount();
        final String sourceCurrency = sourceOffering.getIncluded().getPrices().get(0).getCurrency();

        final String targetPlanId = targetOffering.getOfferings().get(0).getId();
        final String targetPlanName = targetOffering.getOfferings().get(0).getName();
        final String targetOfferName = targetOffering.getIncluded().getBillingPlans().get(0).getName();
        final String targetPriceId = targetOffering.getIncluded().getPrices().get(0).getId();
        final String targetAmount = targetOffering.getIncluded().getPrices().get(0).getAmount();
        final String targetCurrency = targetOffering.getIncluded().getPrices().get(0).getCurrency();

        // submit purchase order in order to create subscription with price id, from source offer
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(sourcePriceId, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId = getPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        // Create and Upload xlsx file for subscription migration upload
        jobName = RandomStringUtils.randomAlphabetic(6);
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, sourcePlanName, sourceOfferName,
            targetPlanName, targetOfferName);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.isRunButtonPresent(),
            equalTo(true), assertionErrorList);

        // running a subscription migration job
        subscriptionMigrationJobDetailPage.runAJob();
        subscriptionMigrationJobDetailPage.clickOnMigrationResultsTab();

        assertionsAfterRunningAJobForWarningScenario(0, subscriptionId, sourcePlanId, sourcePlanName, sourceOfferName,
            sourcePriceId, sourceAmount, sourceCurrency, sourcePriceEndDate, targetPlanId, targetPlanName,
            targetOfferName, targetPriceId, targetAmount, targetCurrency, targetPriceEndDate);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the plan offer mapping and price mapping for warning scenario. In order to get file upload
     * warning scenario following should be there which are given below. After that it test migration of subscription(s)
     * from plan to another after clicking a run button 1) source and target price amount should not be same 2)
     * subscription which has next billing with any promotion should be associated with source plan
     *
     * @result mapping result should be "⚠" (warning)
     */
    @Test
    public void testSubscriptionMigrationMappingWarningScenarioAndMappingResults() {
        // add source and target plan for migration (one source plan is already added at class level)
        final int sourcePriceAmount = 100;
        final int tragetPriceAmount = 200;
        final int totalSubscriptionCreationWithSourceOffer = 2;
        final int totalSubscriptionCreationWithSubscriptionOffering = 1;
        final String sourcePriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String source2PriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 3);
        final String targetPriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);

        addSourceAndTargetPlans(sourcePriceAmount, tragetPriceAmount, getPricelistExternalKeyUs(),
            getPricelistExternalKeyUs(), DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            sourcePriceEndDate, targetPriceEndDate, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE);

        final String sourcePlanId = sourceOffering.getOfferings().get(0).getId();
        final String sourcePlanName = sourceOffering.getOfferings().get(0).getName();
        final String sourceOfferName = sourceOffering.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId = sourceOffering.getIncluded().getPrices().get(0).getId();
        final String sourceAmount = sourceOffering.getIncluded().getPrices().get(0).getAmount();
        final String sourceCurrency = sourceOffering.getIncluded().getPrices().get(0).getCurrency();

        final String targetPlanId = targetOffering.getOfferings().get(0).getId();
        final String targetPlanName = targetOffering.getOfferings().get(0).getName();
        final String targetOfferName = targetOffering.getIncluded().getBillingPlans().get(0).getName();
        final String targetPriceId = targetOffering.getIncluded().getPrices().get(0).getId();
        final String targetAmount = targetOffering.getIncluded().getPrices().get(0).getAmount();
        final String targetCurrency = targetOffering.getIncluded().getPrices().get(0).getCurrency();

        final Offerings sourceOffering2 =
            addSubscriptionPlan(PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(6),
                getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE,
                Status.ACTIVE, SupportLevel.ADVANCED, RandomStringUtils.randomAlphabetic(6), resource, 500,
                DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), source2PriceEndDate);
        subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        final String sourcePlanId2 = sourceOffering2.getOfferings().get(0).getId();
        final String sourcePlanName2 = sourceOffering2.getOfferings().get(0).getName();
        final String sourceOfferName2 = sourceOffering2.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId2 = sourceOffering2.getIncluded().getPrices().get(0).getId();
        final String sourceAmount2 = sourceOffering2.getIncluded().getPrices().get(0).getAmount();
        final String sourceCurrency2 = sourceOffering2.getIncluded().getPrices().get(0).getCurrency();

        // create two different promotions for two source plans
        final String promoCode = promotionUtils.getRandomPromoCode();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        final JStore store = resource.stores().getStore(getStoreIdUs());
        final JPromotion promotion1 = promotionUtils.addPromotion(promoType, Lists.newArrayList(store),
            Lists.newArrayList(sourceOffering), promoCode, false, Status.ACTIVE, "10.0", null,
            DateTimeUtils.getFutureExpirationDate(), null, null, 5, "2", "2");
        final JPromotion promotion2 = promotionUtils.addPromotion(promoType, Lists.newArrayList(store),
            Lists.newArrayList(sourceOffering2), promoCode, false, Status.ACTIVE, "15.0", null,
            DateTimeUtils.getFutureExpirationDate(), null, null, 5, "2", "2");
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // Submit two purchase orders with one offering and one purchase order with different offering
        // with Promotions
        final List<PurchaseOrder> purchaseOrder1 = purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion1,
            sourceOffering, totalSubscriptionCreationWithSourceOffer, null, false, 1);
        final List<PurchaseOrder> purchaseOrder2 = purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion2,
            sourceOffering2, totalSubscriptionCreationWithSubscriptionOffering, null, false, 1);

        final String subscriptionId1ForSourceOffering = purchaseOrder1.get(0).getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        final String subscriptionId2ForSourceOffering = purchaseOrder1.get(1).getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        final String subscriptionIdForSourceOffering2 = purchaseOrder2.get(0).getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // Create and Upload xlsx file for subscription migration upload
        createAndUploadAFile(sourcePlanName, sourceOfferName, sourcePlanName2, sourceOfferName2, targetPlanName,
            targetOfferName);

        // running a subscription migration job
        subscriptionMigrationJobDetailPage.runAJob();
        subscriptionMigrationJobDetailPage.clickOnMigrationResultsTab();
        final int totalRows =
            subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionIdHeaderOnMigrationResultsTab().size();
        for (int i = 0; i < totalRows; i++) {
            if (totalRows > 0) {
                if (subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionIdHeaderOnMigrationResultsTab()
                    .get(i).equals(subscriptionId1ForSourceOffering)) {
                    assertionsAfterRunningAJobForWarningScenario(i, subscriptionId1ForSourceOffering, sourcePlanId,
                        sourcePlanName, sourceOfferName, sourcePriceId, sourceAmount, sourceCurrency,
                        sourcePriceEndDate, targetPlanId, targetPlanName, targetOfferName, targetPriceId, targetAmount,
                        targetCurrency, targetPriceEndDate);
                }
                if (subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionIdHeaderOnMigrationResultsTab()
                    .get(i).equals(subscriptionId2ForSourceOffering)) {
                    assertionsAfterRunningAJobForWarningScenario(i, subscriptionId2ForSourceOffering, sourcePlanId,
                        sourcePlanName, sourceOfferName, sourcePriceId, sourceAmount, sourceCurrency,
                        sourcePriceEndDate, targetPlanId, targetPlanName, targetOfferName, targetPriceId, targetAmount,
                        targetCurrency, targetPriceEndDate);
                }
                if (subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionIdHeaderOnMigrationResultsTab()
                    .get(i).equals(subscriptionIdForSourceOffering2)) {
                    assertionsAfterRunningAJobForWarningScenario(i, subscriptionIdForSourceOffering2, sourcePlanId2,
                        sourcePlanName2, sourceOfferName2, sourcePriceId2, sourceAmount2, sourceCurrency2,
                        source2PriceEndDate, targetPlanId, targetPlanName, targetOfferName, targetPriceId, targetAmount,
                        targetCurrency, targetPriceEndDate);
                }
            } else {
                Assert.fail("No results on Migration Results tab");
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests non visibility of list of subscriptions link which are due for renewals after running a
     * job of migration
     */
    @Test
    public void testNonVisibilityOfListOfSubscriptionUnderWarningsAfterRun() {
        addSourceAndTargetPlans(100, 100, getPricelistExternalKeyUs(), getPricelistExternalKeyUs(),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6),
            DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6), Status.ACTIVE, Status.ACTIVE,
            Status.ACTIVE, Status.ACTIVE);
        final String sourcePlanName = sourceOffering.getOfferings().get(0).getName();
        final String sourceOfferName = sourceOffering.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId = sourceOffering.getIncluded().getPrices().get(0).getId();
        final String targetPlanName = targetOffering.getOfferings().get(0).getName();
        final String targetOfferName = targetOffering.getIncluded().getBillingPlans().get(0).getName();
        final int totalSubscriptionsCreationWithSourceOffer = 1;

        final String promoCode = promotionUtils.getRandomPromoCode();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        final JStore store = resource.stores().getStore(getStoreIdUs());
        final JPromotion promotion = promotionUtils.addPromotion(promoType, Lists.newArrayList(store),
            Lists.newArrayList(sourceOffering), promoCode, false, Status.ACTIVE, "10.0", null,
            DateTimeUtils.getFutureExpirationDate(), null, null, 5, "2", "2");

        // submit purchase order in order to create subscription with price id, from source offer
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(sourcePriceId, 1);
        purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion, sourceOffering,
            totalSubscriptionsCreationWithSourceOffer, null, false, 1);

        // Create and Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, sourcePlanName, sourceOfferName,
            targetPlanName, targetOfferName);
        jobName = RandomStringUtils.randomAlphabetic(6);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        subscriptionMigrationJobDetailPage.clickOnErrorsTab();
        AssertCollector.assertThat("List of Subscription link is not visible",
            subscriptionMigrationJobDetailPage.getColumnValuesOfWarningHeader().get(0),
            equalTo(totalSubscriptionsCreationWithSourceOffer + " " + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE
                + " " + PelicanConstants.HIPHEN + " " + PelicanErrorConstants.LIST_OF_SUBSCRIPTION_WARNING_MESSAGE),
            assertionErrorList);
        // Click here for the list of subscription(s).
        // running a subscription migration job
        subscriptionMigrationJobDetailPage.runAJob();
        subscriptionMigrationJobDetailPage.clickOnErrorsTab();
        subscriptionMigrationJobDetailPage.getColumnValuesOfWarningHeader();
        AssertCollector.assertThat("List of Subscription link is still visible",
            subscriptionMigrationJobDetailPage.getColumnValuesOfWarningHeader().get(0),
            equalTo(totalSubscriptionsCreationWithSourceOffer + " " + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test download errors for partially completed job
     */
    @Test
    public void testDownloadErrorsForPartiallyCompletedJob() {
        // add source and target plan for migration (one source plan is already added at class level)
        final int sourcePriceAmount = 100;
        final int tragetPriceAmount = 200;
        final String sourcePriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String source2PriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 3);
        final String targetPriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);

        addSourceAndTargetPlans(sourcePriceAmount, tragetPriceAmount, getPricelistExternalKeyUs(),
            getPricelistExternalKeyUs(), DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            sourcePriceEndDate, targetPriceEndDate, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE);

        final String sourcePlanName = sourceOffering.getOfferings().get(0).getName();
        final String sourceOfferName = sourceOffering.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId = sourceOffering.getIncluded().getPrices().get(0).getId();
        final String targetPlanName = targetOffering.getOfferings().get(0).getName();
        final String targetOfferName = targetOffering.getIncluded().getBillingPlans().get(0).getName();

        final Offerings sourceOffering2 =
            addSubscriptionPlan(PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(6),
                getPricelistExternalKeyUk(), OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE,
                Status.ACTIVE, SupportLevel.ADVANCED, RandomStringUtils.randomAlphabetic(6), resource, 500,
                DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), source2PriceEndDate);
        subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        final String sourcePlanId2 = sourceOffering2.getOfferings().get(0).getId();
        final String sourcePlanName2 = sourceOffering2.getOfferings().get(0).getName();
        final String sourceOfferName2 = sourceOffering2.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId2 = sourceOffering2.getIncluded().getPrices().get(0).getId();

        // submit purchase order in order to create subscription with price id, from source offer
        final LinkedHashMap<String, Integer> priceMap1 = new LinkedHashMap<>();
        priceMap1.put(sourcePriceId, 1);
        purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap1, false, null);

        final LinkedHashMap<String, Integer> priceMap2 = new LinkedHashMap<>();
        priceMap2.put(sourcePriceId2, 1);
        final PurchaseOrder purchaseOrder2 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap2, false, null);
        final PurchaseOrder getPurchaseOrder2 = resource.purchaseOrder().getById(purchaseOrder2.getId());
        final String subscriptionId2 = getPurchaseOrder2.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        // Create and Upload xlsx file for subscription migration upload
        createAndUploadAFile(sourcePlanName, sourceOfferName, sourcePlanName2, sourceOfferName2, targetPlanName,
            targetOfferName);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        final String jobId = subscriptionMigrationJobDetailPage.getId();
        final String migrationDate = subscriptionMigrationJobDetailPage.getLastModified().substring(0, 10);
        final String migrationDateFormated =
            DateTimeUtils.changeDateFormat(migrationDate.replace("/", "-"), "MM-dd-yyyy", "yyyy-MM-dd");
        final String jobName = subscriptionMigrationJobDetailPage.getJobName();

        // Set the status of job to completed
        final String updateJobStatusSQL = "update subscription_migration_job set status = "
            + SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue() + " where id = '" + jobId + "'";
        DbUtils.updateQuery(updateJobStatusSQL, getEnvironmentVariables());
        subscriptionMigrationJobDetailPage.refreshPage();
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.getStatus(),
            equalTo(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName()), assertionErrorList);

        // running a subscription migration job
        subscriptionMigrationJobDetailPage.runAJob();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.clickOnMigrationErrorsDownloadLink(1);
        fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME_ERROR_DOWNLOAD + jobId + DOWNLOAD_FILE_FORMAT;
        LOGGER.info("Name of Downloaded file is: " + fileName);

        String[][] fileData = null;
        try {
            fileData = XlsUtils.readDataFromXlsx(fileName);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Total number of records in file is: " + fileData.length);
        if (fileData.length > 0) {
            for (int i = 1; i < fileData.length; i++) {
                AssertCollector.assertThat("Download: Subscription id is not correct", fileData[i][0],
                    equalTo(subscriptionId2), assertionErrorList);
                AssertCollector.assertThat("Download: Migration date is not correct", (fileData[i][1]).substring(0, 10),
                    equalTo(migrationDateFormated), assertionErrorList);
                AssertCollector.assertThat("Download: Job id is not correct", fileData[i][2], equalTo(jobId),
                    assertionErrorList);
                AssertCollector.assertThat("Download: Job name is not correct", fileData[i][3], equalTo(jobName),
                    assertionErrorList);
                AssertCollector.assertThat("Download: source pan id is not correct", fileData[i][4],
                    equalTo(sourcePlanId2), assertionErrorList);
                AssertCollector.assertThat("Download: source plan name is not correct", fileData[i][5],
                    equalTo(sourcePlanName2), assertionErrorList);
                AssertCollector.assertThat("Download: source external key is not corrects", fileData[i][6],
                    equalTo(sourcePlanName2), assertionErrorList);
                AssertCollector.assertThat("Download: Source offer externla key is not correct", fileData[i][7],
                    equalTo(sourceOfferName2), assertionErrorList);
                AssertCollector.assertThat("Download: Source offer name is not correct", fileData[i][8],
                    equalTo(sourceOfferName2), assertionErrorList);
                AssertCollector.assertThat("Download: Error messages are not correct", fileData[i][9],
                    equalTo("Could not migrate subscription: Could not find Price"), assertionErrorList);
            }
        } else {
            LOGGER.info("No Records found in Downloaded Report");
        }
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test download errors for failed job
     */
    @Test
    public void testDownloadErrorsForFailedJob() {
        // add source and target plan for migration (one source plan is already added at class level)
        final int sourcePriceAmount = 100;
        final int targetPriceAmount = 200;
        final String sourcePriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String targetPriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);

        addSourceAndTargetPlans(sourcePriceAmount, targetPriceAmount, getPricelistExternalKeyUs(),
            getPricelistExternalKeyUk(), DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            sourcePriceEndDate, targetPriceEndDate, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE);

        final String sourcePlanId = sourceOffering.getOfferings().get(0).getId();
        final String sourcePlanName = sourceOffering.getOfferings().get(0).getName();
        final String sourceOfferName = sourceOffering.getIncluded().getBillingPlans().get(0).getName();
        final String sourcePriceId = sourceOffering.getIncluded().getPrices().get(0).getId();
        final String targetPlanName = targetOffering.getOfferings().get(0).getName();
        final String targetOfferName = targetOffering.getIncluded().getBillingPlans().get(0).getName();

        // submit purchase order in order to create subscription with price id, from source offer
        final LinkedHashMap<String, Integer> priceMap1 = new LinkedHashMap<>();
        priceMap1.put(sourcePriceId, 1);
        final PurchaseOrder purchaseOrder1 =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap1, false, null);
        final PurchaseOrder getPurchaseOrder1 = resource.purchaseOrder().getById(purchaseOrder1.getId());
        final String subscriptionId = getPurchaseOrder1.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        // Create and Upload xlsx file for subscription migration upload
        jobName = RandomStringUtils.randomAlphabetic(6);
        final XlsUtils utils = new XlsUtils();
        final ArrayList<String> columnHeadersList = new ArrayList<>();
        final ArrayList<String> recordsDataList = new ArrayList<>();
        columnHeadersList.add("Source Plan XKEY,Source Offer XKEY,Target Plan XKEY,Target Offer XKEY");
        recordsDataList.add(sourcePlanName + "," + sourceOfferName + "," + targetPlanName + "," + targetOfferName);
        try {
            utils.createAndWriteToXls(FILE_NAME, columnHeadersList, recordsDataList, false);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        final String jobId = subscriptionMigrationJobDetailPage.getId();
        final String migrationDate = subscriptionMigrationJobDetailPage.getLastModified().substring(0, 10);
        final String migrationDateFormated =
            DateTimeUtils.changeDateFormat(migrationDate.replace("/", "-"), "MM-dd-yyyy", "yyyy-MM-dd");
        System.out.println(migrationDateFormated);
        final String jobName = subscriptionMigrationJobDetailPage.getJobName();

        // Set the status of job to completed
        final String updateJobStatusSQL = "update subscription_migration_job set status = "
            + SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDbValue() + " where id = '" + jobId + "'";
        DbUtils.updateQuery(updateJobStatusSQL, getEnvironmentVariables());
        subscriptionMigrationJobDetailPage.refreshPage();
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.getStatus(),
            equalTo(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName()), assertionErrorList);

        // running a subscription migration job
        subscriptionMigrationJobDetailPage.runAJob();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.clickOnMigrationErrorsDownloadLink(0);
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.getStatus(),
            equalTo(SubscriptionMigrationJobStatus.FAILED.getDisplayName()), assertionErrorList);
        fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME_ERROR_DOWNLOAD + jobId + DOWNLOAD_FILE_FORMAT;
        LOGGER.info("Name of Downloaded file is: " + fileName);

        String[][] fileData = null;
        try {
            fileData = XlsUtils.readDataFromXlsx(fileName);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Total number of records in file is: " + fileData.length);
        int i;
        if (fileData.length > 0) {
            for (i = 1; i < fileData.length; i++) {
                AssertCollector.assertThat("Download: Subscription id is not correct", fileData[i][0],
                    equalTo(subscriptionId), assertionErrorList);
                AssertCollector.assertThat("Download: Migration date is not correct", (fileData[i][1]).substring(0, 10),
                    equalTo(migrationDateFormated), assertionErrorList);
                AssertCollector.assertThat("Download: Job id is not correct", fileData[i][2], equalTo(jobId),
                    assertionErrorList);
                AssertCollector.assertThat("Download: Job name is not correct", fileData[i][3], equalTo(jobName),
                    assertionErrorList);
                AssertCollector.assertThat("Download: source pan id is not correct", fileData[i][4],
                    equalTo(sourcePlanId), assertionErrorList);
                AssertCollector.assertThat("Download: source plan name is not correct", fileData[i][5],
                    equalTo(sourcePlanName), assertionErrorList);
                AssertCollector.assertThat("Download: source external key is not corrects", fileData[i][6],
                    equalTo(sourcePlanName), assertionErrorList);
                AssertCollector.assertThat("Download: Source offer externla key is not correct", fileData[i][7],
                    equalTo(sourceOfferName), assertionErrorList);
                AssertCollector.assertThat("Download: Source offer name is not correct", fileData[i][8],
                    equalTo(sourceOfferName), assertionErrorList);
                AssertCollector.assertThat("Download: Error messages are not correct", fileData[i][9],
                    equalTo(PelicanErrorConstants.MIGRATE_SUBSCRIPTION_NOT_FIND_PRICE_MESSAGE), assertionErrorList);
            }
        } else {
            LOGGER.info("No Records found in Downloaded Report");
        }
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Create and Upload xlsx file for subscription migration upload
     *
     * @param sourcePlanName
     * @param sourceOfferName
     * @param sourcePlanName2
     * @param sourceOfferName2
     * @param targetPlanName
     * @param targetOfferName
     */
    private void createAndUploadAFile(final String sourcePlanName, final String sourceOfferName,
        final String sourcePlanName2, final String sourceOfferName2, final String targetPlanName,
        final String targetOfferName) {
        jobName = RandomStringUtils.randomAlphabetic(6);
        final XlsUtils utils = new XlsUtils();
        final ArrayList<String> columnHeadersList = new ArrayList<>();
        final ArrayList<String> recordsDataList = new ArrayList<>();
        columnHeadersList.add("Source Plan XKEY,Source Offer XKEY,Target Plan XKEY,Target Offer XKEY");
        recordsDataList.add(sourcePlanName + "," + sourceOfferName + "," + targetPlanName + "," + targetOfferName);
        recordsDataList.add(sourcePlanName2 + "," + sourceOfferName2 + "," + targetPlanName + "," + targetOfferName);

        try {
            utils.createAndWriteToXls(FILE_NAME, columnHeadersList, recordsDataList, false);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
    }

    /**
     * This is a method to find a subscription and validate the subscription activity
     *
     * @param subscriptionId.
     * @param Job Id
     * @throws IOException
     * @throws org.apache.http.ParseException
     */
    private void validateMigratedSubscriptionAndSubscriptionEvent(final String subscriptionId, final String jobId,
        final String sourcePriceId, final String targetPriceId) throws org.apache.http.ParseException, IOException {

        // Find the subscriptions in the admin tool and validate the subscriptions
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        subscriptionPage.findBySubscriptionId(subscriptionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final List<SubscriptionActivity> bicSubscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final SubscriptionActivity bicSubscriptionActivity =
            bicSubscriptionActivityList.get((bicSubscriptionActivityList.size()) - 1);
        AssertCollector.assertThat("Incorrect next billing price id", subscriptionDetailPage.getNextBillingPriceId(),
            equalTo(targetPriceId), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity for a subscription",
            bicSubscriptionActivity.getActivity(), equalTo(SUB_MIGRATED), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription activity requestor for a subscription",
            bicSubscriptionActivity.getRequestor().split(" ")[0], equalTo(PelicanConstants.AUTO_USER_NAME),
            assertionErrorList);
        final String expectedMemoOnsubscriptionDetailPage = SUBSCRIPTION_MEMO + " " + jobId + "\n" + OLD_PRICE_ID + " "
            + sourcePriceId + "\n" + NEW_PRICE_ID + " " + targetPriceId;

        AssertCollector.assertThat("Incorrect subscription activity memo for a subscription",
            bicSubscriptionActivity.getMemo(), equalTo(expectedMemoOnsubscriptionDetailPage), assertionErrorList);

        // verify subscription event api for SUB_MIGRATED activity for one subscription
        final JSubscriptionEvents subscriptionEvents =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(subscriptionId, null);
        final SubscriptionEventsData subscriptionEventsData = Iterables.getLast(subscriptionEvents.getEventsData());
        AssertCollector.assertThat(
            "Activity is not correct for get subscription api for subscription id " + subscriptionId,
            subscriptionEventsData.getEventType(), equalTo(SUB_MIGRATED), assertionErrorList);
        AssertCollector.assertThat(
            "Requestor name is not correct for get subscription api for subscription id " + subscriptionId,
            subscriptionEventsData.getRequesterName(), equalTo(PelicanConstants.AUTO_USER_NAME), assertionErrorList);
        AssertCollector.assertThat(
            "Purchase order is not correct for get subscription api for subscription id " + subscriptionId,
            subscriptionEventsData.getPurchaseOrderId(), equalTo(null), assertionErrorList);
        final String expectedMemoInSubscriptionEventApi = SUBSCRIPTION_MEMO + " " + jobId + ". " + OLD_PRICE_ID + " "
            + sourcePriceId + ". " + NEW_PRICE_ID + " " + targetPriceId + ".";
        AssertCollector.assertThat("Memo is not correct for for get subscription api subscription id " + subscriptionId,
            subscriptionEventsData.getMemo(), equalTo(expectedMemoInSubscriptionEventApi), assertionErrorList);
    }

    /**
     * This is a method to edit a subscription
     *
     * @param subscriptionId - subscription id
     * @param status
     */
    private void editASubscriptionStatus(final String subscriptionId, final String status) {

        final SubscriptionDetailPage subscriptionDetailPage = subscriptionPage.findBySubscriptionId(subscriptionId);
        editSubscriptionPage = subscriptionDetailPage.clickOnEditSubscriptionLink();
        editSubscriptionPage.editASubscription(null, null, status, null);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
    }

    /**
     * Add Subscription plan with deta which is given below
     *
     * @param productLineExternalKey
     * @param priceListExternalKey
     * @param offeringType
     * @param billingFrequency
     * @param status
     * @param supportLevel
     * @param subscriptionOfferExternalKey
     * @param resource
     * @param amount
     * @param startDate
     * @param endDate
     * @return subscription plan with offer and price
     */
    private Offerings addSubscriptionPlan(final String productLineExternalKey, final String priceListExternalKey,
        final OfferingType offeringType, final BillingFrequency billingFrequency, final Status planStatus,
        final Status offerStatus, final SupportLevel supportLevel, final String subscriptionOfferExternalKey,
        final PelicanPlatform resource, final int amount, final String startDate, final String endDate) {
        // add product line
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
        // add subscription plan
        final Offerings newSubscriptionPlan = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey,
            offeringType, planStatus, supportLevel, UsageType.COM, resource, null, null);

        final String subscriptionPlanOfferingId = newSubscriptionPlan.getOffering().getId();
        // Add an offer to Subscription plan
        final SubscriptionOffer subscriptionOffer = subscriptionPlanApiUtils
            .helperToAddSubscriptionOfferToPlan(subscriptionOfferExternalKey, billingFrequency, 1, offerStatus);
        final String subscriptionOfferId = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, subscriptionOffer, subscriptionPlanOfferingId).getData().getId();
        // add prices to an offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOfferWithDates(amount, priceListExternalKey, startDate, endDate),
            subscriptionPlanOfferingId, subscriptionOfferId);
        // get offerings by id
        return resource.offerings().getOfferingById(subscriptionPlanOfferingId, "offers,prices");
    }

    /**
     * Method to add two plans (source and target) with given parameters which are written below
     *
     * @param sourcePrice
     * @param targetPrice
     * @param targetPriceList
     * @param startDate
     * @param sourceEndDate
     * @param targetEndDate
     * @param sourcePlanStatus
     * @param targetPlanStatus
     * @param sourceOfferStatus
     * @param targetOfferStatus
     */
    private void addSourceAndTargetPlans(final int sourcePrice, final int targetPrice, final String sourcePriceList,
        final String targetPriceList, final String startDate, final String sourceEndDate, final String targetEndDate,
        final Status sourcePlanStatus, final Status targetPlanStatus, final Status sourceOfferStatus,
        final Status targetOfferStatus) {

        sourceOffering =
            addSubscriptionPlan(PelicanConstants.SUB_PLAN_EXT_KEY_PREFIX + RandomStringUtils.randomAlphabetic(6),
                getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, sourcePlanStatus,
                sourceOfferStatus, SupportLevel.BASIC, RandomStringUtils.randomAlphabetic(6), resource, sourcePrice,
                startDate, sourceEndDate);

        // add target plan with different price from source offer for migration
        targetOffering = addSubscriptionPlan(RandomStringUtils.randomAlphabetic(6), targetPriceList,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, targetPlanStatus, targetOfferStatus,
            SupportLevel.BASIC, RandomStringUtils.randomAlphabetic(6), resource, targetPrice, startDate, targetEndDate);
    }

    /**
     * common assertions after running a run job
     *
     * @param index
     * @param subscriptionId
     * @param sourcePlanId
     * @param sourcePlanName
     * @param sourceOfferName
     * @param sourcePriceId
     * @param sourceAmount
     * @param sourceCurrency
     * @param sourcePriceEndDate
     * @param targetPlanId
     * @param targetPlanName
     * @param targetOfferName
     * @param targetPriceId
     * @param targetAmount
     * @param targetCurrency
     * @param targetPriceEndDate
     */
    private void assertionsAfterRunningAJobForWarningScenario(final int index, final String subscriptionId,
        final String sourcePlanId, final String sourcePlanName, final String sourceOfferName,
        final String sourcePriceId, final String sourceAmount, final String sourceCurrency,
        final String sourcePriceEndDate, final String targetPlanId, final String targetPlanName,
        final String targetOfferName, final String targetPriceId, final String targetAmount,
        final String targetCurrency, final String targetPriceEndDate) {
        AssertCollector.assertThat("Incorrect subscription id on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionIdHeaderOnMigrationResultsTab().get(index),
            equalTo(subscriptionId), assertionErrorList);
        AssertCollector.assertThat("Incorrect source plan id for first source offering on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePlanIdHeaderOnMigrationResultsTab().get(index),
            equalTo(sourcePlanId), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect source plan external key for first source offering on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePlanExternalKeyHeaderOnMigrationResultsTab()
                .get(index),
            equalTo(sourcePlanName), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect source offer external key for first source offering on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourceOfferExternalKeyHeaderOnMigrationResultsTab()
                .get(index),
            equalTo(sourceOfferName), assertionErrorList);
        AssertCollector.assertThat("Incorrect source price id on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePriceIdHeaderOnMigrationResultsTab().get(index),
            equalTo(sourcePriceId), assertionErrorList);
        AssertCollector.assertThat("Incorrect source Amount on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourceAmountHeaderOnMigrationResultsTab().get(index),
            equalTo(sourceAmount + " " + sourceCurrency), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect source price end date on migration results tab", subscriptionMigrationJobDetailPage
                .getColumnValuesOfSourcePriceEndDateHeaderOnMigrationResultsTab().get(index),
            equalTo(sourcePriceEndDate + " 23:59:59 UTC"), assertionErrorList);
        AssertCollector.assertThat("Incorrect target plan id on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPlanIdHeaderOnMigrationResultsTab().get(index),
            equalTo(targetPlanId), assertionErrorList);
        AssertCollector.assertThat("Incorrect target plan extrnal key on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPlanExternalKeyOnMigrationResultsTab().get(index),
            equalTo(targetPlanName), assertionErrorList);
        AssertCollector.assertThat("Incorrect target offer name on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetOfferNameOnMigrationResultsTab().get(index),
            equalTo(targetOfferName), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect target offer external key on migration results tab", subscriptionMigrationJobDetailPage
                .getColumnValuesOfTargetOfferExternalKeyOnMigrationResultsTab().get(index),
            equalTo(targetOfferName), assertionErrorList);
        AssertCollector.assertThat("Incorrect target offer price id on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPriceIdOnMigrationResultsTab().get(index),
            equalTo(targetPriceId), assertionErrorList);
        AssertCollector.assertThat("Incorrect target Amount on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetAmountOnMigrationResultsTab().get(index),
            equalTo(targetAmount + " " + targetCurrency), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect target price end date on migration results tab", subscriptionMigrationJobDetailPage
                .getColumnValuesOfTargetPriceEndDateHeaderOnMigrationResultsTab().get(index),
            equalTo(targetPriceEndDate + " 23:59:59 UTC"), assertionErrorList);
        AssertCollector.assertThat("Incorrect storeExternalKeyUs on migration results tab",
            subscriptionMigrationJobDetailPage.getColumnValuesOfStoreHeaderOnMigrationResultsTab().get(index),
            equalTo(getStoreExternalKeyUs()), assertionErrorList);
    }

    private void validateChangeNotificationForBicSubscription(final String subscriptionId) {
        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, subscriptionId,
                getUser(), subscriptionOffering, true, assertionErrorList),
            PelicanConstants.UPDATED, subscriptionId, true, assertionErrorList);

        cseHelper.assertionToValidateChangeNotificationHeaderForSubscriptionForSherpaMigration(eventsList,
            subscriptionId, PelicanConstants.UPDATED, assertionErrorList);

        cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForSherpaMigration(eventsList, subscriptionId,
            PelicanConstants.UPDATED, assertionErrorList);
    }

    private void validateChangeNotificationForMetaSubscription(final String subscriptionId) {
        cseHelper.assertionToValidateChangeNotificationFound(
            cseHelper.helperToFindChangeNotificationInCSEEvent(eventsList, subscriptionId, getUser(),
                subscriptionOffering, assertionErrorList),
            PelicanConstants.UPDATED, subscriptionId, assertionErrorList);

        cseHelper.assertionToValidateChangeNotificationHeaderForSubscriptionForSherpaMigration(eventsList,
            subscriptionId, PelicanConstants.UPDATED, assertionErrorList);
    }
}
