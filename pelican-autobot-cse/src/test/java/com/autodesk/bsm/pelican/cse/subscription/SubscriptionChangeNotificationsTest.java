package com.autodesk.bsm.pelican.cse.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.cse.ChangeNotifications;
import com.autodesk.bsm.pelican.enums.SalesChannel;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test class to verify cse events for subscription update.
 *
 * @author jains
 */

public class SubscriptionChangeNotificationsTest extends BaseTestData {

    private CSEHelper cseHelper;
    private SubscriptionOffering subscriptionOffering;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String PELICANEVENTS_NOTIFICATION_CHANNEL;
    private List<ChangeNotificationMessage> eventsList;
    private PelicanPlatform resource;
    private JProductLine productLine;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionChangeNotificationsTest.class.getSimpleName());
    private static BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();
        resource = new PelicanClient(getEnvironmentVariables()).platform();

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
        UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        subscriptionOffering.setId(getBicSubscriptionPlan().getOfferings().get(0).getId());
        subscriptionOffering.setUsageType(getBicSubscriptionPlan().getOfferings().get(0).getUsageType());
        subscriptionOffering.setOfferingType(getBicSubscriptionPlan().getOfferings().get(0).getOfferingType());
        productLine.setName(getBicSubscriptionPlan().getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);
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
     * This test verifies that change notification are generated when payment profile is updated for a subscription.
     * Subscription change notification are not generated for this.
     *
     */
    @Test
    public void testChangeNotoficationsForASubscription() {

        eventsList = pelicanEventsConsumer.getNotifications();

        // Submit PO
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id: " + subscriptionId);
        final JSubscription subscription =
            resource.subscriptionJson().getSubscription(subscriptionId, null, PelicanConstants.CONTENT_TYPE);
        final User user = resource.user().getUserById(buyerUser.getId());

        // Add new payment profile
        final PaymentProfile newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addPaypalPaymentProfile(buyerUser.getId(), null);

        // Update subscription
        final boolean isSubscriptionUpdated =
            resource.subscription().updateSubscription(subscriptionId, newPaymentProfile.getId());
        AssertCollector.assertThat("Unable to update subscription", isSubscriptionUpdated, equalTo(true),
            assertionErrorList);
        pelicanEventsConsumer.waitForEvents(10000);

        List<ChangeNotifications> cseEvents =
            cseHelper.helperToFindChangeNotificationsForSubscription(eventsList, subscriptionId);

        if (3 == cseEvents.size()) {
            cseHelper.helperToValidateChangeNotificationForSubscription(cseEvents.get(0), subscription, user,
                subscriptionOffering, PelicanConstants.CREATED, SalesChannel.BIC_DIRECT, assertionErrorList);
            cseHelper.helperToValidateChangeNotificationForSubscription(cseEvents.get(1), subscription, user,
                subscriptionOffering, PelicanConstants.UPDATED, SalesChannel.BIC_DIRECT, assertionErrorList);
            cseHelper.helperToValidateChangeNotificationForSubscription(cseEvents.get(2), subscription, user,
                subscriptionOffering, PelicanConstants.UPDATED, SalesChannel.BIC_DIRECT, assertionErrorList);
        } else if (0 == cseEvents.size()) {
            Assert.fail("None of the cse notifications got pusblished for subscription: " + subscriptionId);
        } else {
            Assert.fail("Number of cse events are not correct for subscription: " + subscriptionId
                + ". Expected 3 found " + cseEvents.size());
        }

        AssertCollector.assertAll(assertionErrorList);
    }

}
