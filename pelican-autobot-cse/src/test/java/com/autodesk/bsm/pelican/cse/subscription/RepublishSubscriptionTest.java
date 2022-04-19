package com.autodesk.bsm.pelican.cse.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
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
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import junit.framework.Assert;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepublishSubscriptionTest extends SeleniumWebdriver {

    private RepublishEventsPage eventsPage;
    private RepublishJobsPage publishJobPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage;

    private List<String> subscriptionIdList = new ArrayList<>();
    private List<String> subscriptionBatchIdList = new ArrayList<>();
    private List<String> subscriptionDateList = new ArrayList<>();
    private List<String> bicCommercialSubscriptionIdList = new ArrayList<>();

    private List<String> subscriptionCountFromDateRange;

    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private String pelicanEventsNotificationChannel;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private BankingConfigurationPropertySetPage bankingConfigurationPropertySetPage;

    private static final String REPUBLISH_LIMIT = "20";
    private static final String REPUBLISH_DEFAULT_LIMIT = "20000";
    private static final String START_DATE_RANGE = "11/1/2010 00:00:00";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepublishSubscriptionTest.class.getSimpleName());
    private static final int TOTAL_BIC_COMMERCIAL_SUBSCRIPTIONS = 4;
    private static String dateBeforeCreatingBicCommercialSubscriptions;
    private static String dateAfterCreatingBicCommercialSubscriptions;

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
        publishJobPage = adminToolPage.getPage(RepublishJobsPage.class);
        RepublishEventsPage.checkAndClearRepublishHungJobs(getEnvironmentVariables());

        // creating bic commercial subscriptions
        dateBeforeCreatingBicCommercialSubscriptions = DateTimeUtils.getCurrentDate(PelicanConstants.DB_DATE_FORMAT);
        final Offerings bicCommercialOfferingYearly =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicCommercialPriceIdYearly = bicCommercialOfferingYearly.getIncluded().getPrices().get(0).getId();
        // create purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceIdYearly, 1);
        for (int i = 0; i < TOTAL_BIC_COMMERCIAL_SUBSCRIPTIONS; i++) {
            final PurchaseOrder purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
            final String subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId();
            bicCommercialSubscriptionIdList.add(subscriptionId);
        }
        LOGGER.info("Total number of bic commercial subscriptions " + bicCommercialSubscriptionIdList.size());
        dateAfterCreatingBicCommercialSubscriptions = DateTimeUtils.getCurrentDate(PelicanConstants.DB_DATE_FORMAT);

        // Prepare Test Data
        // Subscriptions
        subscriptionIdList = DbUtils.selectQuery("select ID from subscription where APP_FAMILY_ID='"
            + getEnvironmentVariables().getAppFamilyId() + "' order by ID asc limit 5",
            PelicanConstants.ID.toUpperCase(), getEnvironmentVariables());

        subscriptionDateList = DbUtils.selectQuery(
            "select DATE_FORMAT(from_unixTime(LAST_MODIFIED/1000),'%m/%d/%Y %H:%i:%s.%f') as LAST_MODIFIED from subscription where LAST_MODIFIED is NOT NULL and APP_FAMILY_ID='"
                + getEnvironmentVariables().getAppFamilyId()
                + "' order by DATE_FORMAT(from_unixTime(LAST_MODIFIED/1000),'%m/%d/%Y %H:%i:%s.%f') asc limit 5",
            PelicanConstants.LAST_MODIFIED_DB.toUpperCase(), getEnvironmentVariables());

        subscriptionCountFromDateRange = DbUtils.selectQuery(
            "select count(*) AS COUNT from subscription where LAST_MODIFIED is NOT NULL and APP_FAMILY_ID='"
                + getEnvironmentVariables().getAppFamilyId()
                + "' and DATE_FORMAT(from_unixTime(LAST_MODIFIED/1000),'%m/%d/%Y %H:%i:%s.%f') BETWEEN '"
                + subscriptionDateList.get(0).split("\\.")[0] + PelicanConstants.ZERO_DECIMALS + "' AND '"
                + subscriptionDateList.get(subscriptionDateList.size() - 1).split("\\.")[0]
                + PelicanConstants.NINER_DECIMALS + "'",
            PelicanConstants.COUNT.toUpperCase(), getEnvironmentVariables());

        subscriptionBatchIdList = DbUtils.selectQuery("select ID from subscription where APP_FAMILY_ID='"
            + getEnvironmentVariables().getAppFamilyId() + "' order by ID asc limit 150",
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

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        pelicanEventsConsumer.clearNotificationsList();
    }

    @Test
    public void testRepubCNSubscriptionIdList() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName(), subscriptionIdList)),
            assertionErrorList);
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(subscriptionIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method tests the republishing of subscriptionChangeNotification upon request.
     *
     * @param republishChangeNotificationFilterType
     */
    @Test(dataProvider = "republishChangeNotificationFilterType")
    public void testRepubSCNForSubscription(final String republishChangeNotificationFilterType) {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.AUM_SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(republishChangeNotificationFilterType);
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        System.out.println(dateBeforeCreatingBicCommercialSubscriptions);
        System.out.println(dateAfterCreatingBicCommercialSubscriptions);
        if (republishChangeNotificationFilterType
            .equalsIgnoreCase(RepublishChangeNotificationFilterType.ID_LIST.getFilterType())) {
            eventsPage.selectIdList(bicCommercialSubscriptionIdList);
        } else if (republishChangeNotificationFilterType
            .equalsIgnoreCase(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType())) {
            eventsPage.selectIdRange(bicCommercialSubscriptionIdList.get(0),
                bicCommercialSubscriptionIdList.get(bicCommercialSubscriptionIdList.size() - 1));
        } else if (republishChangeNotificationFilterType
            .equalsIgnoreCase(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType())) {
            eventsPage.selectDateRange(dateBeforeCreatingBicCommercialSubscriptions,
                dateAfterCreatingBicCommercialSubscriptions);
        }
        eventsPage.clickFindMatchingEntities();

        if (!republishChangeNotificationFilterType
            .equalsIgnoreCase(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType())) {
            AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
                bicCommercialSubscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);
        } else {
            AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
                bicCommercialSubscriptionIdList.size(), lessThanOrEqualTo(eventsPage.getTotalCount()),
                assertionErrorList);
        }

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(bicCommercialSubscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.AUM_SUBSCRIPTION.getEntityName(), bicCommercialSubscriptionIdList)),
            assertionErrorList);
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()),
            greaterThanOrEqualTo(bicCommercialSubscriptionIdList.size()), assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.AUM_SUBSCRIPTION_CHANGE_NOTIFICATION_REPUBLISH_JOB.getJobCategory()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNSubscriptionIdRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdRange(subscriptionIdList.get(0), subscriptionIdList.get(subscriptionIdList.size() - 1));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count with ID Range did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(subscriptionIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNSubscriptionDateRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectDateRange(subscriptionDateList.get(0),
            subscriptionDateList.get(subscriptionDateList.size() - 1));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result with Date Range count did not match the expected",
            Integer.parseInt(subscriptionCountFromDateRange.get(0)), equalTo(eventsPage.getTotalCount()),
            assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(Integer.parseInt(subscriptionCountFromDateRange.get(0))), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()),
            equalTo(Integer.parseInt(subscriptionCountFromDateRange.get(0))), assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testAbortChangeNotification() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();
        publishJobPage.clickAbort();

        AssertCollector.assertThat("Status is not FAILED", publishJobPage.getStatus(),
            equalTo(Status.FAILED.toString()), assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testForceFailChangeNotification() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();
        publishJobPage.clickForceFail();

        AssertCollector.assertThat("Status is not FAILED", publishJobPage.getStatus(),
            equalTo(Status.FAILED.toString()), assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testBatchRepublishChangeNotification() {

        // Revert the Republish Limit
        findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.CORE);
        bankingConfigurationPropertySetPage =
            bankingConfigurationPropertiesPage.editProperties(PelicanConstants.REPUBLISH_MAX_LIMIT);
        bankingConfigurationPropertySetPage.setProperties(PelicanConstants.REPUBLISH_MAX_LIMIT,
            REPUBLISH_DEFAULT_LIMIT);

        eventsPage.navigateToEventsPublishPage();

        try {

            eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
            eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
            eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType());
            eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
                RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
            eventsPage.selectIdRange(subscriptionBatchIdList.get(0),
                subscriptionBatchIdList.get(subscriptionBatchIdList.size() - 1));
            eventsPage.clickFindMatchingEntities();

            AssertCollector.assertThat("Total Subscription result count with ID Range did not match the expected",
                subscriptionBatchIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);
            eventsPage.clickRepublishButton();

            pelicanEventsConsumer.waitForEvents(50000);
            eventsList = pelicanEventsConsumer.getNotifications();
            AssertCollector.assertThat("Empty events list", eventsList.size(),
                greaterThanOrEqualTo(subscriptionBatchIdList.size()), assertionErrorList);
            LOGGER.info("Events list size:" + eventsList.size());
            AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
                equalTo(Status.COMPLETED.toString()), assertionErrorList);
            AssertCollector.assertThat("Records published count mismatched in Job page",
                Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(subscriptionBatchIdList.size()),
                assertionErrorList);

            AssertCollector.assertThat("Records published count mismatched in Job page",
                Integer.parseInt(publishJobPage.getStepCount()), equalTo(2), assertionErrorList);
            AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
                equalTo(JobCategory.SUBSCRIPTION_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()),
                assertionErrorList);

            // Set Republish Change Notification LIMIT to "20"
            findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.CORE);
            bankingConfigurationPropertySetPage =
                bankingConfigurationPropertiesPage.editProperties(PelicanConstants.REPUBLISH_MAX_LIMIT);
            bankingConfigurationPropertySetPage.setProperties(PelicanConstants.REPUBLISH_MAX_LIMIT, REPUBLISH_LIMIT);

        } catch (final WebDriverException e) {

            LOGGER.info("Something unexpected happened, reverting the config changes and also failing the Test case");

            // Set Republish Change Notification LIMIT to "20"
            findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.CORE);
            bankingConfigurationPropertySetPage =
                bankingConfigurationPropertiesPage.editProperties(PelicanConstants.REPUBLISH_MAX_LIMIT);
            bankingConfigurationPropertySetPage.setProperties(PelicanConstants.REPUBLISH_MAX_LIMIT, REPUBLISH_LIMIT);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNDefaultError() {

        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Default Republish Change Notifications page",
            PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE, equalTo(eventsPage.getErrorMessageFromFormHeader()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepublishFlag() {
        // Set Republish cse notifications feature flag to false
        // Reverting Republish cse notifications feature flag to true
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REPUBLISH_CHANGE_NOTIFICATION_ENABLE, false);

        eventsPage.navigateToEventsPublishPage();

        final List<WebElement> webElement = eventsPage.getAllWebElements();

        // Reverting Republish cse notifications feature flag to true
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REPUBLISH_CHANGE_NOTIFICATION_ENABLE, true);

        AssertCollector.assertThat("Test Feature Flag for Republish Change Notifications failed", 0,
            equalTo(webElement.size()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @Test
    public void testRepublishLimitScenariosForIdRange() {

        eventsPage.navigateToEventsPublishPage();

        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.getRequester(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType());
        eventsPage.selectIdRange("1", "10000000000000000");
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total result count did not match the expected",
            PelicanErrorConstants.REPUB_MAX_LIMIT_ERROR, equalTo(eventsPage.getErrorMessageFromFormHeader()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    @Test()
    public void testRepubCN20kLimitForDateRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.getRequester(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType());
        eventsPage.selectDateRange(START_DATE_RANGE, DateTimeUtils.getNowAsString(PelicanConstants.DB_DATE_FORMAT));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total result count did not match the expected",
            PelicanErrorConstants.REPUB_MAX_LIMIT_ERROR, equalTo(eventsPage.getErrorMessageFromFormHeader()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @Test
    public void testRepubCNPelicanRequester() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName(), subscriptionIdList,
                RepublishChangeNotificationRequester.PELICAN.toString())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @Test
    public void testRepubCNAdpRequester() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.ADP.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName(), subscriptionIdList,
                RepublishChangeNotificationRequester.ADP.toString())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @Test
    public void testRepubCNForgeRequester() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.FORGE.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName(), subscriptionIdList,
                RepublishChangeNotificationRequester.FORGE.toString())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    @Test
    public void testRepubCNSalesforceRequester() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.SALESFORCE.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription result count for ID List did not match the expected",
            subscriptionIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);

        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.SUBSCRIPTION.getEntityName(), subscriptionIdList,
                RepublishChangeNotificationRequester.SALESFORCE.toString())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Data provider with different filter type.
     *
     * @return Object[]
     */
    @DataProvider(name = "republishChangeNotificationFilterType")
    public Object[][] getRepublishChangeNotificationFilterType() {
        return new Object[][] { { RepublishChangeNotificationFilterType.ID_LIST.getFilterType() },
                { RepublishChangeNotificationFilterType.ID_RANGE.getFilterType() },
                { RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType() } };
    }
}
