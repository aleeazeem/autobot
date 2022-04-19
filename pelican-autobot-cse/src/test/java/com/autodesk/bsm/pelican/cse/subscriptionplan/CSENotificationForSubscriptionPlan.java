package com.autodesk.bsm.pelican.cse.subscriptionplan;

import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This test class is to test CSE Change Notifications for Subscription Plan from Admin Tool.
 *
 * @author mandas
 */
public class CSENotificationForSubscriptionPlan extends SeleniumWebdriver {

    private String productLineNameAndExternalKey;
    private AddSubscriptionPlanPage addSubscriptionPlanPage;
    private EditSubscriptionPlanPage editSubscriptionPlanPage;

    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private boolean isNotificationFound;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private boolean isCseHeadersFeatureFlagChanged;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(CSENotificationForSubscriptionPlan.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);

        // Add product Line name + external key
        productLineNameAndExternalKey = getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";

        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
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
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();

        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isCseHeadersFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, true);
    }

    @AfterClass
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);

        if (isCseHeadersFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, false);
        }
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        LOGGER.info("$$$$$$$$$$$$$$$$$$$$    COMPLETED    $$$$$$$$$$$");
        pelicanEventsConsumer.clearNotificationsList();
        eventsList.clear();
    }

    /**
     * This method tests CSE Change Notification for subscription plan with offer, price and entitlement Test
     *
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testCSENotificationForAddSubscriptionPlan(final Status status, final Boolean isConfirmPopup,
        final Boolean isAddFeatures) {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, status, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM,
            PelicanConstants.OFFERING_DETAIL, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();

        isNotificationFound =
            cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList, subscriptionPlanId,
                PelicanConstants.CREATED, getUser(), subscriptionPlanExtKey, false, null, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Subscription Offering : " + subscriptionPlanId + ", is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test CSE Change Notification on Edit Subscription Plan
     */
    @Test
    public void testCSENotificationForEditSubscriptionPlan() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, PackagingType.INDUSTRY_COLLECTION,
            true);

        eventsList.clear();
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList,
            planId, PelicanConstants.UPDATED, getUser(), subscriptionPlanExtKey, false, null, assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Subscription Offering : "
            + subscriptionPlanDetailPage.getId() + ", is Not Found in eventList", isNotificationFound,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests CSE Change Notification for subscription plan with offer, price and entitlement Test
     *
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testCSENotificationForAddSubscriptionPlanWithFeature(final Status status, final Boolean isConfirmPopup,
        final Boolean isAddFeatures) {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, status, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM,
            PelicanConstants.OFFERING_DETAIL, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(
                ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)),
            0);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage =
            addSubscriptionPlanPage.clickOnSave(isConfirmPopup);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();

        isNotificationFound =
            cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList, subscriptionPlanId,
                PelicanConstants.UPDATED, getUser(), subscriptionPlanExtKey, isAddFeatures, item, assertionErrorList);

        if (isAddFeatures) {
            AssertCollector.assertTrue(
                "ChangeNotification for Subscription Offering : " + subscriptionPlanId + ", is NOT Found in eventList",
                isNotificationFound, assertionErrorList);
        } else {
            AssertCollector.assertFalse(
                "ChangeNotification for Subscription Offering : " + subscriptionPlanId + ", is Found in eventList",
                isNotificationFound, assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test CSE Change Notification on Edit Subscription Plan
     */
    @Test
    public void testCSENotificationForAddFeatureInEditSubscriptionPlan() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(
                ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)),
            0);

        eventsList.clear();
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);

        pelicanEventsConsumer.waitForEvents(10000);
        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList,
            planId, PelicanConstants.UPDATED, getUser(), subscriptionPlanExtKey, true, item, assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Subscription Offering : "
            + subscriptionPlanDetailPage.getId() + ", is Not Found in eventList", isNotificationFound,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "SubscriptionPlanStatuses")
    public Object[][] getSubscriptionPlanStatuses() {
        return new Object[][] { { Status.NEW, false, false }, { Status.ACTIVE, true, true } };
    }

}
