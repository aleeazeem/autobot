package com.autodesk.bsm.pelican.cse;

import static org.hamcrest.Matchers.greaterThan;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
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

/**
 * CSE notifications being published in CSE channel for different types as following 1. changeNotifications 2.
 * subscriptionChangeNotification. This is a debug class not meant to test cse notifications and not to be run as a part
 * of full regression.
 *
 * @author mandas
 */
@Test(enabled = false)
public class CSEConsumerDebugger extends SeleniumWebdriver {

    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private String pelicanEventsBootStrapNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private ChangeNotificationConsumer pelicanEventsBootstrapConsumer;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSEConsumerDebugger.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        final PelicanClient pelicanResource = new PelicanClient(getEnvironmentVariables());
        final PelicanPlatform resource = pelicanResource.platform();
        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsBootStrapNotificationChannel =
            getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();
        final String notificationConsSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();
        final String notificationConsKey = getEnvironmentVariables().getNotificationConsKey();
        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationConsKey, notificationConsSecret);
        final String accesstoken = authClient.getAuthToken();
        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource);
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accesstoken);
        pelicanEventsBootstrapConsumer =
            cseHelper.initializeConsumer(brokerUrl, pelicanEventsBootStrapNotificationChannel, accesstoken);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
        cseHelper.terminateConsumer(pelicanEventsBootstrapConsumer, pelicanEventsBootStrapNotificationChannel,
            eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        LOGGER.info("$$$$$$$$$$$$$$$$$$$$ COMPLETED $$$$$$$$$$$");
        eventsList.clear();
        pelicanEventsConsumer.clearNotificationsList();
    }

    @Test(enabled = false)
    public void testDebugConsumer() {
        pelicanEventsConsumer.waitForEvents(9900000);
        pelicanEventsBootstrapConsumer.waitForEvents(2000);
        eventsList = pelicanEventsConsumer.getNotifications();
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);
    }
}
