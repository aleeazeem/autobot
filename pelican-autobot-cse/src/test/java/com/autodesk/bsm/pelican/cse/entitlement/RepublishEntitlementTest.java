package com.autodesk.bsm.pelican.cse.entitlement;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationChannel;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationEntity;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationFilterType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationRequester;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertySetPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.FindBankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.events.RepublishEventsPage;
import com.autodesk.bsm.pelican.ui.pages.events.RepublishJobsPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.ImmutableList;

import junit.framework.Assert;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepublishEntitlementTest extends SeleniumWebdriver {

    private RepublishEventsPage eventsPage;
    private RepublishJobsPage publishJobPage;
    private String planId;
    private String itemId1;
    private String itemId2;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage;

    private List<String> itemInstanceIdList = new ArrayList<>();

    private List<String> bicCommercialSubscriptionIdList = new ArrayList<>();

    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private String pelicanEventsNotificationChannel;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private BankingConfigurationPropertySetPage bankingConfigurationPropertySetPage;

    private static final String REPUBLISH_LIMIT = "20";
    private static final String REPUBLISH_DEFAULT_LIMIT = "20000";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepublishEntitlementTest.class.getSimpleName());
    private static final int TOTAL_BIC_COMMERCIAL_SUBSCRIPTIONS = 4;

    /**
     * Setup method will perform below, Login to Admin Tool navigate to Applications -> Banking Properties -> Show Hives
     * page click on show Hives button And read the pages for the Keys
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        eventsPage = adminToolPage.getPage(RepublishEventsPage.class);
        final AddSubscriptionPlanPage addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        publishJobPage = adminToolPage.getPage(RepublishJobsPage.class);
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage =
            adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        RepublishEventsPage.checkAndClearRepublishHungJobs(getEnvironmentVariables());

        // Need User Id to get Item Instance for automation user
        final Map<String, String> userParams = new HashMap<>();
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final String userExternalKey =
            "AutoUser" + RandomStringUtils.randomNumeric(4) + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS");
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // build buyerUser object to submit a PO
        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(userExternalKey);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        itemId1 = item.getId();
        final Item item1 = featureApiUtils.addFeature(null, null, null);
        itemId2 = item1.getId();

        // creating bic commercial subscriptions
        final Offerings bicTrialOfferingYearly =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        final String bicTrialPriceIdYearly = bicTrialOfferingYearly.getIncluded().getPrices().get(0).getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(
            bicTrialOfferingYearly.getOfferings().get(0).getId(), item.getId(), null, true);
        // create purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicTrialPriceIdYearly, 1);
        for (int i = 0; i < TOTAL_BIC_COMMERCIAL_SUBSCRIPTIONS; i++) {
            final PurchaseOrder purchaseOrder = purchaseOrderUtils
                .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
            final String subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId();
            bicCommercialSubscriptionIdList.add(subscriptionId);
        }
        LOGGER.info("Total number of bic commercial subscriptions " + bicCommercialSubscriptionIdList.size());

        // Entitlements
        itemInstanceIdList = DbUtils.selectQuery(("select ID from item_inst where item_id = " + item.getId()),
            PelicanConstants.ID.toUpperCase(), getEnvironmentVariables());

        // Set Republish Change Notification LIMIT to "20"
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        findBankingConfigurationPropertiesPage = adminToolPage.getPage(FindBankingConfigurationPropertiesPage.class);

        bankingConfigurationPropertiesPage =
            findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.CORE);
        bankingConfigurationPropertySetPage =
            bankingConfigurationPropertiesPage.editProperties(PelicanConstants.REPUBLISH_MAX_LIMIT);
        bankingConfigurationPropertySetPage.setProperties(PelicanConstants.REPUBLISH_MAX_LIMIT, REPUBLISH_LIMIT);

        try {
            // Navigate to Event Republish page
            eventsPage.navigateToEventsPublishPage();
            Assert.assertEquals(PelicanConstants.REPUB_PAGE_HEADER, eventsPage.getPageHeader());
        } catch (final WebDriverException e) {
            LOGGER.info("Couldn't access the Republish Page, might be Feature Flag is set to False. Resetting it...");

            // Setting Republish cse notifications feature flag to true
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REPUBLISH_CHANGE_NOTIFICATION_ENABLE,
                true);
        }

        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        // Add product Line
        final ProductLine productLine = new ProductLine();
        final ProductLineData productLineData = new ProductLineData();
        String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        subscriptionPlanApiUtils.addProductLine(resource, productLine);
        final String productLineNameAndExternalKey =
            productLine.getData().getName() + " (" + productLine.getData().getName() + ")";

        // Add product Line
        productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        subscriptionPlanApiUtils.addProductLine(resource, productLine);

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId2, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);
        addSubscriptionPlanPage.clickOnSave(true);
        planId = subscriptionPlanDetailPage.getId();

        // Initialize Consumer
        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource);
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

        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

    }

    /**
     * Close the Driver, to kill all open driver instances
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        // Terminate Consumer
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);

        // Reverting Republish cse notifications feature flag to true
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REPUBLISH_CHANGE_NOTIFICATION_ENABLE, true);

        // Revert the Republish Limit
        bankingConfigurationPropertiesPage =
            findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.CORE);
        bankingConfigurationPropertySetPage =
            bankingConfigurationPropertiesPage.editProperties(PelicanConstants.REPUBLISH_MAX_LIMIT);
        bankingConfigurationPropertySetPage.setProperties(PelicanConstants.REPUBLISH_MAX_LIMIT,
            REPUBLISH_DEFAULT_LIMIT);

    }

    /**
     * this is to O/P pretty display for Test case start
     */
    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        eventsList.clear();

        try {
            // Navigate to Event Republish page
            eventsPage.navigateToEventsPublishPage();
            Assert.assertEquals(PelicanConstants.REPUB_PAGE_HEADER, eventsPage.getPageHeader());
        } catch (final WebDriverException e) {
            LOGGER.info("Couldn't access the Republish Page, might be Feature Flag is set to False. Resetting it...");

            // Setting Republish cse notifications feature flag to true
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REPUBLISH_CHANGE_NOTIFICATION_ENABLE,
                true);
        }

    }

    @Test
    public void testRepubCNEntitlementsIdList() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(itemInstanceIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Entitlements result with ID list count did not match the expected",
            itemInstanceIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(itemInstanceIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());
        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName(), itemInstanceIdList)),
            assertionErrorList);
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(itemInstanceIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNEntitlementsIdRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdRange(itemInstanceIdList.get(0), itemInstanceIdList.get(itemInstanceIdList.size() - 1));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Entitlements result count with ID Range did not match the expected",
            itemInstanceIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(itemInstanceIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(itemInstanceIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNEntitlementsDateRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectDateRange(
            DateTimeUtils.getDatetimeWithAddedMinutesAsString(PelicanConstants.DB_DATE_FORMAT, -5),
            DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Entitlements Order result with Date Range count did not match the expected",
            (itemInstanceIdList.size()), equalTo(eventsPage.getTotalCount()), assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(itemInstanceIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(itemInstanceIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepublishLimitScenariosForIdRange() {

        eventsPage.navigateToEventsPublishPage();

        eventsPage.selectEntity(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.getRequester(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType());
        eventsPage.selectIdRange("1", "10000000000000000");
        eventsPage.clickFindMatchingEntities();

        if (eventsPage.getTotalCount() > 20) {
            AssertCollector.assertThat("Total result count did not match the expected",
                PelicanErrorConstants.REPUB_MAX_LIMIT_ERROR, equalTo(eventsPage.getErrorMessageFromFormHeader()),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Total result count did not match the expected", eventsPage.getTotalCount(),
                lessThanOrEqualTo(20), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the republish functionality for entitlements end date notification events
     */
    @Test
    public void testRepublishEntitlementsEndDateNotification() {

        eventsPage.selectEntity(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.OFFERINGENTITLEMENTENDDATE.getValue());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.getRequester(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType());
        eventsPage.selectDateRangeWithoutTime(
            DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT),
            DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT));
        eventsPage.clickFindMatchingEntities();

        final int numberOfColumns = eventsPage.getGrid().getColumnHeaders().size();
        AssertCollector.assertThat("EOS date column is not showed up",
            eventsPage.getGrid().getColumnHeaders().get(numberOfColumns - 4),
            equalTo(PelicanConstants.EOS_DATE_COLUMN_NAME), assertionErrorList);
        AssertCollector.assertThat("EOL Renewal date column is not showed up",
            eventsPage.getGrid().getColumnHeaders().get(numberOfColumns - 3),
            equalTo(PelicanConstants.EOL_RENEWAL_DATE_COLUMN_NAME), assertionErrorList);
        AssertCollector.assertThat("EOL Immediate date column is not showed up",
            eventsPage.getGrid().getColumnHeaders().get(numberOfColumns - 2),
            equalTo(PelicanConstants.EOL_IMMEDIATE_DATE_COLUMN_NAME), assertionErrorList);

        final String newEntitlementId1 = DbUtils.getEntitlementIdFromItemId(planId, itemId1, getEnvironmentVariables());

        final String newEntitlementId2 = DbUtils.getEntitlementIdFromItemId(planId, itemId2, getEnvironmentVariables());

        final List<String> idValues = eventsPage.getGrid().getColumnValues(1);
        AssertCollector.assertTrue("Generated first entitlement is not in the list",
            idValues.contains(newEntitlementId1), assertionErrorList);
        AssertCollector.assertTrue("Generated second entitlement is not in the list",
            idValues.contains(newEntitlementId2), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
