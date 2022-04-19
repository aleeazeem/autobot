package com.autodesk.bsm.pelican.cse.subscriptionplan;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.item.OfferingEntitlement;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This test class is created to test to validate CSE messages for End Entitlement end date.
 *
 * @author mandas
 */
public class CSERemoveDateTest extends SeleniumWebdriver {

    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private static String productLineNameAndExternalKey;
    private static AddSubscriptionPlanPage addSubscriptionPlanPage;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSERemoveDateTest.class.getSimpleName());
    private JobsClient jobsResource;
    private Item item1;
    private Item item3;
    private String cprItemId1;
    private String cprItemId2;
    private String csrItemId3;
    private String itemTypeExternalKey;
    private String planId1;
    private String planId2;
    private String planId3;
    private String planId4;
    private String planExtKey1;
    private String planExtKey2;
    private String planExtKey3;
    private String planExtKey4;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private boolean isNotificationFound;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        // Add product Line name + external key
        productLineNameAndExternalKey = getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";

        // Add product Line
        final ProductLine productLine = new ProductLine();
        final ProductLineData productLineData = new ProductLineData();
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        final PelicanPlatform platformResource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils.addProductLine(platformResource, productLine);

        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        final PelicanTriggerClient triggersResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggersResource.jobs();

        platformResource.healthCheckStatusResource().getHealthCheckStatus();

        final Applications applications = platformResource.application().getApplications();
        String appId = "";
        String featureTypeId = null;

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        List<Map<String, String>> resultMapList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ITEM_TYPE_ID,
                PelicanConstants.CSR_FEATURE_TYPE_EXTERNAL_KEY, appId), getEnvironmentVariables());

        if (resultMapList.size() == 0) {

            final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
            final String featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

            // Navigate to the add feature page and add a feature
            addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
                getEnvironmentVariables().getApplicationDescription(), featureTypeName,
                PelicanConstants.CSR_EXTERNAL_KEY);
            final FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
            featureTypeId = featureTypeDetailPage.getId();
        } else {
            featureTypeId = resultMapList.get(0).get("ID");
        }

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item1 = featureApiUtils.addFeature(null, null, featureTypeId);
        cprItemId1 = item1.getId();
        final Item item2 = featureApiUtils.addFeature(null, null, featureTypeId);
        cprItemId2 = item2.getId();
        item3 = featureApiUtils.addFeature(null, null, null);
        csrItemId3 = item3.getId();

        resultMapList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ITEM_TYPE_XKEY, item3.getItemType().getId()),
                getEnvironmentVariables());

        if (resultMapList.size() > 0) {
            itemTypeExternalKey = resultMapList.get(0).get("XKEY");
        }

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        planExtKey1 = RandomStringUtils.randomAlphanumeric(8) + "_1";
        planExtKey2 = RandomStringUtils.randomAlphanumeric(8) + "_2";
        planExtKey3 = RandomStringUtils.randomAlphanumeric(8) + "_3";
        planExtKey4 = RandomStringUtils.randomAlphanumeric(8) + "_4";

        planId1 = createSubPlansWithFeatures(planExtKey1, Status.NEW);
        planId2 = createSubPlansWithFeatures(planExtKey2, Status.ACTIVE);
        planId3 = createSubPlansWithFeatures(planExtKey3, Status.ACTIVE);
        planId4 = createSubPlansWithFeatures(planExtKey4, Status.CANCELED);

        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE $$$$$$$$$$$$$$$$$$$$");
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
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

        cseHelper = new CSEHelper(platformResource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

    }

    @AfterMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {
        eventsList.clear();
        pelicanEventsConsumer.clearNotificationsList();
    }

    /**
     * This is a test case which will end date for single features in multiple plans in different states
     *
     * @throws ParseException
     */
    @Test()
    public void testCSENotificationForSingleFeatureEndDate() {

        final String entitlementIdPlan1Item1 =
            DbUtils.getEntitlementIdFromItemId(planId1, cprItemId1, getEnvironmentVariables());

        final String entitlementIdPlan2Item1 =
            DbUtils.getEntitlementIdFromItemId(planId2, cprItemId1, getEnvironmentVariables());

        final String entitlementIdPlan3Item1 =
            DbUtils.getEntitlementIdFromItemId(planId3, cprItemId1, getEnvironmentVariables());

        final String entitlementIdPlan4Item1 =
            DbUtils.getEntitlementIdFromItemId(planId4, cprItemId1, getEnvironmentVariables());

        final String todayDate = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_TIME_FORMAT);

        DbUtils.updateRemoveFeatureDates(todayDate, null, null, entitlementIdPlan1Item1, getEnvironmentVariables());

        DbUtils.updateRemoveFeatureDates(null, todayDate, null, entitlementIdPlan2Item1, getEnvironmentVariables());

        DbUtils.updateRemoveFeatureDates(null, null, todayDate, entitlementIdPlan3Item1, getEnvironmentVariables());

        DbUtils.updateRemoveFeatureDates(todayDate, todayDate, todayDate, entitlementIdPlan4Item1,
            getEnvironmentVariables());

        jobsResource.entitlementEndDate();

        pelicanEventsConsumer.waitForEvents(30000);

        eventsList = pelicanEventsConsumer.getNotifications();

        OfferingEntitlement offeringEntitlement = new OfferingEntitlement();

        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(true);
        offeringEntitlement.setIsEolImme(false);
        offeringEntitlement.setIsEosRenewal(false);

        ArrayList<OfferingEntitlement> offeringEntitlements = new ArrayList<>();
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId1,
            planExtKey1, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertFalse(
            "ChangeNotification for Entitlement End Date : " + planId1 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        offeringEntitlements = new ArrayList<>();
        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(false);
        offeringEntitlement.setIsEolImme(true);
        offeringEntitlement.setIsEosRenewal(false);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId2,
            planExtKey2, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Entitlement End Date : " + planId2 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        offeringEntitlements = new ArrayList<>();
        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(false);
        offeringEntitlement.setIsEolImme(false);
        offeringEntitlement.setIsEosRenewal(true);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId3,
            planExtKey3, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Entitlement End Date : " + planId3 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        offeringEntitlements = new ArrayList<>();
        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(true);
        offeringEntitlement.setIsEolImme(true);
        offeringEntitlement.setIsEosRenewal(true);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId4,
            planExtKey4, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Entitlement End Date : " + planId4 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will end date for multiple features in multiple plans in different states
     *
     * @throws ParseException
     */
    @Test(dependsOnMethods = "testCSENotificationForSingleFeatureEndDate", alwaysRun = true)
    public void testCSENotificationForMultiFeatureEndDate() {

        final String entitlementIdPlan1Item3 =
            DbUtils.getEntitlementIdFromItemId(planId1, csrItemId3, getEnvironmentVariables());

        final String entitlementIdPlan2Item3 =
            DbUtils.getEntitlementIdFromItemId(planId2, csrItemId3, getEnvironmentVariables());

        final String entitlementIdPlan3Item3 =
            DbUtils.getEntitlementIdFromItemId(planId3, csrItemId3, getEnvironmentVariables());

        final String entitlementIdPlan4Item3 =
            DbUtils.getEntitlementIdFromItemId(planId4, csrItemId3, getEnvironmentVariables());

        final String todayDate = new DateTime().toString(PelicanConstants.DATE_TIME_FORMAT);

        DbUtils.updateRemoveFeatureDates(todayDate, null, null, entitlementIdPlan1Item3, getEnvironmentVariables());

        DbUtils.updateRemoveFeatureDates(null, todayDate, null, entitlementIdPlan2Item3, getEnvironmentVariables());

        DbUtils.updateRemoveFeatureDates(null, null, todayDate, entitlementIdPlan3Item3, getEnvironmentVariables());

        DbUtils.updateRemoveFeatureDates(todayDate, todayDate, todayDate, entitlementIdPlan4Item3,
            getEnvironmentVariables());

        jobsResource.entitlementEndDate();

        pelicanEventsConsumer.waitForEvents(30000);

        eventsList = pelicanEventsConsumer.getNotifications();

        OfferingEntitlement offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(true);
        offeringEntitlement.setIsEolImme(false);
        offeringEntitlement.setIsEosRenewal(false);

        ArrayList<OfferingEntitlement> offeringEntitlements = new ArrayList<>();
        offeringEntitlements.add(offeringEntitlement);

        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item3.getExternalKey());
        offeringEntitlement.setType(itemTypeExternalKey);
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(true);
        offeringEntitlement.setIsEolImme(false);
        offeringEntitlement.setIsEosRenewal(false);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId1,
            planExtKey1, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertFalse(
            "ChangeNotification for Entitlement End Date : " + planId1 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        offeringEntitlements = new ArrayList<>();
        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(false);
        offeringEntitlement.setIsEolImme(true);
        offeringEntitlement.setIsEosRenewal(false);
        offeringEntitlements.add(offeringEntitlement);

        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item3.getExternalKey());
        offeringEntitlement.setType(itemTypeExternalKey);
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(false);
        offeringEntitlement.setIsEolImme(true);
        offeringEntitlement.setIsEosRenewal(false);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId2,
            planExtKey2, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Entitlement End Date : " + planId2 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        offeringEntitlements = new ArrayList<>();
        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(false);
        offeringEntitlement.setIsEolImme(false);
        offeringEntitlement.setIsEosRenewal(true);
        offeringEntitlements.add(offeringEntitlement);

        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item3.getExternalKey());
        offeringEntitlement.setType(itemTypeExternalKey);
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(false);
        offeringEntitlement.setIsEolImme(false);
        offeringEntitlement.setIsEosRenewal(true);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId3,
            planExtKey3, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Entitlement End Date : " + planId3 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        offeringEntitlements = new ArrayList<>();
        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item1.getExternalKey());
        offeringEntitlement.setType("CSR");
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(true);
        offeringEntitlement.setIsEolImme(true);
        offeringEntitlement.setIsEosRenewal(true);
        offeringEntitlements.add(offeringEntitlement);

        offeringEntitlement = new OfferingEntitlement();
        offeringEntitlement.setItemExternalKey(item3.getExternalKey());
        offeringEntitlement.setType(itemTypeExternalKey);
        offeringEntitlement.setDate(todayDate);
        offeringEntitlement.setIsEos(true);
        offeringEntitlement.setIsEolImme(true);
        offeringEntitlement.setIsEosRenewal(true);
        offeringEntitlements.add(offeringEntitlement);

        isNotificationFound = cseHelper.helperToFindChangeNotificationWithEntitlementEndDate(eventsList, planId4,
            planExtKey4, offeringEntitlements, todayDate, assertionErrorList);

        AssertCollector.assertTrue(
            "ChangeNotification for Entitlement End Date : " + planId4 + " is Not Found in eventList",
            isNotificationFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to create test subscription plans in various states
     *
     * @param subcriptionPlanName
     * @param status
     * @return
     */
    private String createSubPlansWithFeatures(final String subcriptionPlanName, final Status status) {

        if (status == Status.CANCELED) {

            addSubscriptionPlanPage.addSubscriptionPlanInfo(subcriptionPlanName, subcriptionPlanName,
                OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
                UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        } else {

            addSubscriptionPlanPage.addSubscriptionPlanInfo(subcriptionPlanName, subcriptionPlanName,
                OfferingType.BIC_SUBSCRIPTION, status, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
                UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);
        }

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            null, 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, null, null, null, 0);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId2, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            null, 1);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(csrItemId3, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            null, 2);

        if (status == Status.NEW) {
            subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        } else {
            subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        }

        if (status == Status.CANCELED) {
            editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
            editSubscriptionPlanPage.editStatus(Status.CANCELED);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        }

        return subscriptionPlanDetailPage.getId();
    }
}
