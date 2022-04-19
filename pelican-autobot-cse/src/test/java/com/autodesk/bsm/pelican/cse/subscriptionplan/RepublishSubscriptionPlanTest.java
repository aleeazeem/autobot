package com.autodesk.bsm.pelican.cse.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationChannel;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationEntity;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationFilterType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationRequester;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationType;
import com.autodesk.bsm.pelican.enums.Status;
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
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import junit.framework.Assert;

import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RepublishSubscriptionPlanTest extends SeleniumWebdriver {

    private RepublishEventsPage eventsPage;
    private RepublishJobsPage publishJobPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage;

    private List<String> subscriptionPlanIdList = new ArrayList<>();

    private List<String> subscriptionPlanDateList = new ArrayList<>();

    private List<String> subscriptionPlanCountFromDateRange;

    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private String pelicanEventsNotificationChannel;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private BankingConfigurationPropertySetPage bankingConfigurationPropertySetPage;

    private static final String REPUBLISH_LIMIT = "20";
    private static final String REPUBLISH_DEFAULT_LIMIT = "20000";
    private static final String START_DATE_RANGE = "11/1/2010 00:00:00";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepublishSubscriptionPlanTest.class.getSimpleName());

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
        eventsPage = adminToolPage.getPage(RepublishEventsPage.class);
        publishJobPage = adminToolPage.getPage(RepublishJobsPage.class);
        RepublishEventsPage.checkAndClearRepublishHungJobs(getEnvironmentVariables());

        // Subscription Plan
        subscriptionPlanIdList = DbUtils.selectQuery(
            "select ID from Offering where DISCRIMINATOR=0 and APP_FAMILY_ID='"
                + getEnvironmentVariables().getAppFamilyId() + "' order by ID asc limit 5",
            PelicanConstants.ID.toUpperCase(), getEnvironmentVariables());

        subscriptionPlanDateList = DbUtils.selectQuery(
            "select DATE_FORMAT(LAST_MODIFIED,'%m/%d/%Y %H:%i:%s') as LAST_MODIFIED from Offering where LAST_MODIFIED is NOT NULL and DISCRIMINATOR=0 and APP_FAMILY_ID='"
                + getEnvironmentVariables().getAppFamilyId()
                + "' order by DATE_FORMAT(LAST_MODIFIED,'%m/%d/%Y %H:%i:%s') asc limit 5",
            PelicanConstants.LAST_MODIFIED_DB.toUpperCase(), getEnvironmentVariables());

        subscriptionPlanCountFromDateRange = DbUtils.selectQuery(
            "select count(*) AS COUNT from Offering where LAST_MODIFIED is NOT NULL and DISCRIMINATOR=0 and APP_FAMILY_ID='"
                + getEnvironmentVariables().getAppFamilyId()
                + "' and DATE_FORMAT(LAST_MODIFIED,'%m/%d/%Y %H:%i:%s.%f') BETWEEN '"
                + subscriptionPlanDateList.get(0).split("\\.")[0] + PelicanConstants.ZERO_DECIMALS + "' AND '"
                + subscriptionPlanDateList.get(subscriptionPlanDateList.size() - 1).split("\\.")[0]
                + PelicanConstants.NINER_DECIMALS + "'",
            PelicanConstants.COUNT.toUpperCase(), getEnvironmentVariables());

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
    public void testRepubCNSubscriptionPlanIdList() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(subscriptionPlanIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription Plan result with ID List count did not match the expected",
            subscriptionPlanIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionPlanIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());
        AssertCollector.assertThat("Republish Flag for republished CSE message Header is false", true,
            equalTo(cseHelper.validateRepublishChangeNotificationHeader(eventsList,
                RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityCSEName(), subscriptionPlanIdList)),
            assertionErrorList);
        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(subscriptionPlanIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNSubscriptionPlanIdRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_RANGE.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdRange(subscriptionPlanIdList.get(0),
            subscriptionPlanIdList.get(subscriptionPlanIdList.size() - 1));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription Plan result with ID Range count did not match the expected",
            subscriptionPlanIdList.size(), equalTo(eventsPage.getTotalCount()), assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(subscriptionPlanIdList.size()), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()), equalTo(subscriptionPlanIdList.size()),
            assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepubCNSubscriptionPlanDateRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.DATE_RANGE.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectDateRange(subscriptionPlanDateList.get(0),
            subscriptionPlanDateList.get(subscriptionPlanDateList.size() - 1));
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Total Subscription Plan result with Date Range count did not match the expected",
            Integer.parseInt(subscriptionPlanCountFromDateRange.get(0)), equalTo(eventsPage.getTotalCount()),
            assertionErrorList);
        eventsPage.clickRepublishButton();

        pelicanEventsConsumer.waitForEvents(5000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(),
            greaterThanOrEqualTo(Integer.parseInt(subscriptionPlanCountFromDateRange.get(0))), assertionErrorList);
        LOGGER.info("Events list size:" + eventsList.size());

        AssertCollector.assertThat("Status is not Complete", publishJobPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Records published count mismatched in Job page",
            Integer.parseInt(publishJobPage.getProcessedEventsCount()),
            equalTo(Integer.parseInt(subscriptionPlanCountFromDateRange.get(0))), assertionErrorList);
        AssertCollector.assertThat("Category mismatch", publishJobPage.getCategory(),
            equalTo(JobCategory.SUBSCRIPTION_OFFERING_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testRepublishLimitScenariosForIdRange() {

        eventsPage.navigateToEventsPublishPage();

        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityName());
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

    @Test
    public void testRepubCN20kLimitForDateRange() {
        eventsPage.selectEntity(RepublishChangeNotificationEntity.SUBSCRIPTION_PLAN.getEntityName());
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
}
