package com.autodesk.bsm.pelican.cse.features;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeaturesHelper;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
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
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a test class which will test CSE notification for add features functionality in bulk features add
 *
 * @author mandas
 *
 */
public class CSENotificationForFeatureBulkAddOfferingTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private String productLineExternalKey3;
    private String productLineSelectText2;
    private String productLineSelectText3;
    private Item item1;
    private Item item2;
    private Offerings bicOfferings5;
    private Offerings bicOfferings6;
    private FeatureDetailPage featureDetailPage;
    private static FindSubscriptionPlanPage findSubscriptionPlanPage;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private static FindFeaturePage findFeaturePage;
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 = "Success! ";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 = " subscription plan(s) were updated with feature ";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART3 =
        "Success! 0 subscription plan(s) were updated with feature ";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART4 =
        " subscription plan(s) were not updated because the plan(s) cannot allow features to be added this time; please see below to find out when you can try to add the feature.";
    private FeaturesHelper featuresHelper;

    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private String pelicanEventsNotificationChannel;
    private boolean isNotificationFound;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private boolean isCseHeadersFeatureFlagChanged;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private List<String> coreProductList;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(CSENotificationForFeatureBulkAddOfferingTest.class.getSimpleName());

    /*
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        // Add Product Line2
        final String productLineExternalKey2 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);
        productLineSelectText2 = productLineExternalKey2 + " (" + productLineExternalKey2 + ")";

        // Add Product Line3
        productLineExternalKey3 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey3);
        productLineSelectText3 = productLineExternalKey3 + " (" + productLineExternalKey3 + ")";

        // Add multiple bic subscriptions

        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, BillingFrequency.MONTH,
            1, getPricelistExternalKeyUk(), 500);

        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, BillingFrequency.MONTH,
            1, getPricelistExternalKeyUk(), 500);

        bicOfferings5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey3,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings6 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey3,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        // add a feature and get feature id and feature external key
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item1 = featureApiUtils.addFeature(null, null, null);
        item2 = featureApiUtils.addFeature(null, null, null);

        adminToolPage.login();
        new PelicanClient(getEnvironmentVariables()).platform();
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
        featuresHelper = new FeaturesHelper();
        coreProductList = new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1));

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
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isCseHeadersFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.CSE_HEADER_FEATURE_FLAG, true);
    }

    /**
     * Method to terminate consumer and revert feature flag back
     */
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
     * This is a test case which will test the addition of the feature to the plans in active status through bulk add
     * and capture the CSE notification with header
     *
     */
    @Test
    public void testAddFeatureToPlansInActiveStatus() {

        GenericGrid subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(item2.getId(),
            productLineSelectText2, findFeaturePage, featureDetailPage, assertionErrorList);
        int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plan result: " + subscriptionPlanResultCount);

        final String expectedWarnMessage = PelicanConstants.PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE
            + PelicanConstants.PRE_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE
            + subscriptionPlanResults.getColumnValues("ID").size()
            + PelicanConstants.POST_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE;

        String expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + item2.getExternalKey() + " (" + item2.getId() + ").";

        featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount, subscriptionPlanResults,
            expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage, coreProductList,
            assertionErrorList);

        subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(item1.getId(), productLineSelectText2,
            findFeaturePage, featureDetailPage, assertionErrorList);
        final String subscriptionPlanId1 = subscriptionPlanResults.getColumnValues("ID").get(0);
        final String subscriptionPlanId2 = subscriptionPlanResults.getColumnValues("ID").get(1);
        final String subscriptionPlanExternalKey1 = subscriptionPlanResults.getColumnValues("External Key").get(0);
        final String subscriptionPlanExternalKey2 = subscriptionPlanResults.getColumnValues("External Key").get(1);

        subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription result: " + subscriptionPlanResultCount);

        expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART3 + item1.getExternalKey() + " ("
            + item1.getId() + ")." + " " + subscriptionPlanResultCount + POP_UP_ADD_SUBS_SUCCESS_MSG_PART4;

        featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount, subscriptionPlanResults,
            expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage, coreProductList,
            assertionErrorList);

        pelicanEventsConsumer.waitForEvents(5000);

        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound =
            cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList, subscriptionPlanId1,
                PelicanConstants.UPDATED, getUser(), subscriptionPlanExternalKey1, true, item2, assertionErrorList);

        isNotificationFound =
            cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList, subscriptionPlanId2,
                PelicanConstants.UPDATED, getUser(), subscriptionPlanExternalKey2, true, item2, assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Bulk Add Subscription Offering to Feature not Found.",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the addition of the feature to the plans edited from new to active status
     * through bulk add and capture the CSE notification with header
     *
     */
    @Test
    public void testAddFeatureToPlansFromNewToActive() {

        GenericGrid subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(item1.getId(),
            productLineSelectText3, findFeaturePage, featureDetailPage, assertionErrorList);
        int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plan result: " + subscriptionPlanResultCount);

        final String expectedWarnMessage = PelicanConstants.PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE
            + PelicanConstants.PRE_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE
            + subscriptionPlanResults.getColumnValues("ID").size()
            + PelicanConstants.POST_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE;

        String expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + item1.getExternalKey() + " (" + item1.getId() + ").";

        featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount, subscriptionPlanResults,
            expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage, coreProductList,
            assertionErrorList);

        findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferings5.getOfferings().get(0).getId());
        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(null, null, null, Status.ACTIVE, null, UsageType.COM, null,
            productLineExternalKey3, null, null, true);
        editSubscriptionPlanPage.clickOnSave(false);

        findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferings6.getOfferings().get(0).getId());
        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(null, null, null, Status.ACTIVE, null, UsageType.COM, null,
            productLineExternalKey3, null, null, true);
        editSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(item2.getId(), productLineSelectText3,
            findFeaturePage, featureDetailPage, assertionErrorList);
        subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plan result: " + subscriptionPlanResultCount);

        expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + item2.getExternalKey() + " (" + item2.getId() + ").";

        eventsList.clear();
        pelicanEventsConsumer.clearNotificationsList();

        featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount, subscriptionPlanResults,
            expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage, coreProductList,
            assertionErrorList);

        pelicanEventsConsumer.waitForEvents(5000);

        eventsList = pelicanEventsConsumer.getNotifications();

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList,
            bicOfferings5.getOfferings().get(0).getId(), PelicanConstants.UPDATED, getUser(),
            bicOfferings5.getOfferings().get(0).getExternalKey(), true, item2, assertionErrorList);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithStateForSubscriptionOffering(eventsList,
            bicOfferings6.getOfferings().get(0).getId(), PelicanConstants.UPDATED, getUser(),
            bicOfferings6.getOfferings().get(0).getExternalKey(), true, item2, assertionErrorList);

        AssertCollector.assertTrue("ChangeNotification for Bulk Add Subscription Offering to Feature not Found.",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }
}
