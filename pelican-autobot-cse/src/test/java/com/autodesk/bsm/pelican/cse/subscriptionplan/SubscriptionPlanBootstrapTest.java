package com.autodesk.bsm.pelican.cse.subscriptionplan;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.cse.HelperForAssertions;
import com.autodesk.bsm.pelican.enums.BootstrapEntityType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapChangeNotificationsPage;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapEventStatusPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
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
import java.util.List;
import java.util.Map;

/**
 * This class is for Boot strap related tests for Basic Offering.
 */
public class SubscriptionPlanBootstrapTest extends SeleniumWebdriver {

    private String pelicanBootStrapNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private ChangeNotificationConsumer pelicanBootStrapEventsConsumer = null;
    private BootstrapChangeNotificationsPage bootstrapChangeNotificationsPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanBootstrapTest.class.getSimpleName());

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        eventsList = new ArrayList<>();

        bootstrapChangeNotificationsPage = adminToolPage.getPage(BootstrapChangeNotificationsPage.class);

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

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        cseHelper = new CSEHelper(resource);
        // Initialize Consumer
        pelicanBootStrapNotificationChannel = getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();
        pelicanBootStrapEventsConsumer =
            cseHelper.initializeConsumer(brokerUrl, pelicanBootStrapNotificationChannel, accessToken);

    }

    /**
     * After class to terminate consumer
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanBootStrapEventsConsumer, pelicanBootStrapNotificationChannel, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result - ITestResult would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
        // clear change notification list since we don't need older events.
        pelicanBootStrapEventsConsumer.clearNotificationsList();
    }

    /**
     * This is a test method which will test the cse bootstrap from the admin tool with id as filter
     */
    @Test
    public void testBootStrapWithIdFilter() {

        final List<Map<String, String>> resultList = DbUtils.selectQuery("select id from offering where APP_FAMILY_ID "
            + "= 2001 and offering_type in (0,3) order by id desc limit 5;", getEnvironmentVariables());

        final String startId = resultList.get(4).get(PelicanConstants.ID_FIELD);
        final String endId = resultList.get(0).get(PelicanConstants.ID_FIELD);

        final BootstrapEventStatusPage bootstrapEventStatusPage = bootstrapChangeNotificationsPage
            .publishBootStrapEntities(BootstrapEntityType.SUBSCRIPTION_PLAN.getDisplayName(), PelicanConstants.ID_RANGE,
                PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, startId, endId, null, null,
                RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        pelicanBootStrapEventsConsumer.waitForEvents(30000);

        eventsList = pelicanBootStrapEventsConsumer.getNotifications();

        LOGGER.info("Total number of Events : " + eventsList.size());

        bootstrapEventStatusPage.refreshPage();

        HelperForAssertions.assertionsForBootstrap(bootstrapEventStatusPage,
            PelicanConstants.SUBSCRIPTION_OFFERING_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB, eventsList.size(),
            assertionErrorList);
        HelperForAssertions.commonAssertionsOnChangeNotifications(eventsList,
            BootstrapEntityType.SUBSCRIPTION_PLAN.getDisplayName(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the cse bootstrap from the admin tool with date range as filter
     */
    @Test
    public void testBootStrapWithDateRangeFilter() {

        final BootstrapEventStatusPage bootstrapEventStatusPage =
            bootstrapChangeNotificationsPage.publishBootStrapEntities(
                BootstrapEntityType.SUBSCRIPTION_PLAN.getDisplayName(), PelicanConstants.DATE_RANGE,
                PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, null, null, DateTimeUtils.getToday(),
                DateTimeUtils.getToday(), RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        pelicanBootStrapEventsConsumer.waitForEvents(30000);

        eventsList = pelicanBootStrapEventsConsumer.getNotifications();

        LOGGER.info("Total number of Events : " + eventsList.size());

        bootstrapEventStatusPage.refreshPage();

        HelperForAssertions.assertionsForBootstrap(bootstrapEventStatusPage,
            PelicanConstants.SUBSCRIPTION_OFFERING_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB, eventsList.size(),
            assertionErrorList);
        HelperForAssertions.commonAssertionsOnChangeNotifications(eventsList,
            BootstrapEntityType.SUBSCRIPTION_PLAN.getDisplayName(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
