package com.autodesk.bsm.pelican.ui.features;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlanClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeaturesHelper;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a test class which will test the add features functionality in bulk features add
 *
 * @author yerragv
 *
 */
public class AddOrRemoveFeatureFunctionalityInBulkFeaturesTest extends SeleniumWebdriver {

    private String productLineExternalKey3;
    private String productLineSelectText1;
    private String productLineSelectText2;
    private String productLineSelectText3;
    private String productLineSelectText5;
    private String featureId1;
    private String featureExternalKey1;
    private String featureId2;
    private String featureExternalKey2;
    private String featureId3;
    private String featureId5;
    private String featureName3;
    private String featureName5;
    private String featureExternalKey3;
    private String featureExternalKey5;
    private String featureId4;
    private String featureName4;
    private String featureExternalKey4;
    private SubscriptionPlanClient subscriptionPlanResource;
    private Offerings bicOfferings5;
    private Offerings bicOfferings6;
    private FeatureDetailPage featureDetailPage;
    private static FindSubscriptionPlanPage findSubscriptionPlanPage;
    private Offerings bicOfferings1;
    private Offerings bicOfferings2;
    private Offerings bicOfferings3;
    private Offerings bicOfferings4;
    private Offerings bicOfferings7;
    private Offerings bicOfferings8;
    private Offerings bicOfferings9;
    private Offerings bicOfferings10;
    private Offerings bicOfferings11;
    private Offerings bicOfferings12;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private static FindFeaturePage findFeaturePage;
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 = "Success! ";
    private static final String POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART1 = "Success! Feature ";
    private static final String POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART2 = "was removed from ";
    private static final String POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART3 = " Subscription Plan(s).";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 = " subscription plan(s) were updated with feature ";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART3 =
        "Success! 0 subscription plan(s) were updated with feature ";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART4 =
        " subscription plan(s) were not updated because the plan(s) cannot allow features to be added this time; please see below to find out when you can try to add the feature.";
    private FeaturesHelper featuresHelper;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private static int numberOfPlansWithFeature;
    private static int numberOfPlansWithRemoveFeature;
    private FeatureApiUtils featureApiUtils;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(AddOrRemoveFeatureFunctionalityInBulkFeaturesTest.class.getSimpleName());

    /*
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        subscriptionPlanResource =
            new SubscriptionPlanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        // Add Product Line1
        final String productLineExternalKey1 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);
        productLineSelectText1 = productLineExternalKey1 + " (" + productLineExternalKey1 + ")";

        // Add Product Line2
        final String productLineExternalKey2 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);
        productLineSelectText2 = productLineExternalKey2 + " (" + productLineExternalKey2 + ")";

        // Add Product Line3
        productLineExternalKey3 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey3);
        productLineSelectText3 = productLineExternalKey3 + " (" + productLineExternalKey3 + ")";

        // Add multiple bic subscriptions
        bicOfferings1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings3 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.EDU, BillingFrequency.MONTH,
            1, getPricelistExternalKeyUk(), 500);

        bicOfferings4 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.EDU, BillingFrequency.MONTH,
            1, getPricelistExternalKeyUk(), 500);

        bicOfferings5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey3,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings6 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey3,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        // Add Product Line4
        final String productLineExternalKey4 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey4);

        bicOfferings7 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey4,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings8 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey4,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings9 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey4,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        // Add Product Line5
        final String productLineExternalKey5 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey5);

        bicOfferings10 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey5,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings11 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey5,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings12 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey5,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        productLineSelectText5 = productLineExternalKey5 + " (" + productLineExternalKey5 + ")";

        // add a feature and get feature id and feature external key
        featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item1 = featureApiUtils.addFeature(null, null, null);
        featureExternalKey1 = item1.getExternalKey();
        featureId1 = item1.getId();

        final Item item2 = featureApiUtils.addFeature(null, null, null);
        featureExternalKey2 = item2.getExternalKey();
        featureId2 = item2.getId();

        final Item item3 = featureApiUtils.addFeature(null, null, null);
        featureName3 = item3.getName();
        featureExternalKey3 = item3.getExternalKey();
        featureId3 = item3.getId();

        final Item item4 = featureApiUtils.addFeature(null, null, null);
        featureName4 = item4.getName();
        featureExternalKey4 = item4.getExternalKey();
        featureId4 = item4.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings7.getOfferings().get(0).getId(),
            featureId3, null, true);
        numberOfPlansWithFeature++;
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings8.getOfferings().get(0).getId(),
            featureId3, null, true);
        numberOfPlansWithFeature++;
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings9.getOfferings().get(0).getId(),
            featureId3, null, true);
        numberOfPlansWithFeature++;
        numberOfPlansWithRemoveFeature++;

        final Item item5 = featureApiUtils.addFeature(null, null, null);
        featureName5 = item5.getName();
        featureExternalKey5 = item5.getExternalKey();
        featureId5 = item5.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings10.getOfferings().get(0).getId(),
            featureId5, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings11.getOfferings().get(0).getId(),
            featureId5, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings12.getOfferings().get(0).getId(),
            featureId5, null, true);

        bicOfferings1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        bicOfferings2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        adminToolPage.login();
        new PelicanClient(getEnvironmentVariables()).platform();
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
        featuresHelper = new FeaturesHelper();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        adminToolUserId = getEnvironmentVariables().getUserId();

        subscriptionPlanDetailPage = findSubscriptionPlanPage
            .findSubscriptionPlanByExternalKey(bicOfferings9.getOfferings().get(0).getExternalKey());
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editStatus(Status.CANCELED);
        editSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanDetailPage = findSubscriptionPlanPage
            .findSubscriptionPlanByExternalKey(bicOfferings12.getOfferings().get(0).getExternalKey());
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editStatus(Status.CANCELED);
        editSubscriptionPlanPage.clickOnSave(false);
    }

    /**
     * This is a test case which will test the addition of the feature to the plans in new status through bulk add
     *
     */
    @Test
    public void testAddFeatureToPlansInNewStatus() {

        GenericGrid subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId2,
            productLineSelectText1, findFeaturePage, featureDetailPage, assertionErrorList);
        int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plans result: " + subscriptionPlanResultCount);

        final String expectedWarnMessage = PelicanConstants.PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE
            + PelicanConstants.PRE_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE
            + subscriptionPlanResults.getColumnValues("ID").size()
            + PelicanConstants.POST_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE;

        String expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey2 + " (" + featureId2 + ").";

        final List<String> coreProductList = new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1));
        final List<String> addedSubscriptionPlanIdList = featuresHelper.helperToAddFeatureToPlans(
            subscriptionPlanResultCount, subscriptionPlanResults, expectedWarnMessage, expectedAddFeaturePopUpMessage,
            featureDetailPage, coreProductList, assertionErrorList);

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);

        subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId1, productLineSelectText1,
            findFeaturePage, featureDetailPage, assertionErrorList);
        subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription result: " + subscriptionPlanResultCount);

        expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey1 + " (" + featureId1 + ").";

        featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount, subscriptionPlanResults,
            expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage, coreProductList,
            assertionErrorList);

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);

        featuresHelper.helperForDynamoDbAssertions(bicOfferings1.getOfferings().get(0).getId(),
            bicOfferings2.getOfferings().get(0).getId(), featureId2, featureId1, Action.UPDATE, true,
            getEnvironmentVariables(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will test the addition of the feature to the plans in active status through bulk add
     *
     */
    @Test
    public void testAddFeatureToPlansInActiveStatus() {

        GenericGrid subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId2,
            productLineSelectText2, findFeaturePage, featureDetailPage, assertionErrorList);
        int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plans result: " + subscriptionPlanResultCount);

        final String expectedWarnMessage = PelicanConstants.PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE
            + PelicanConstants.PRE_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE
            + subscriptionPlanResults.getColumnValues("ID").size()
            + PelicanConstants.POST_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE;

        String expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey2 + " (" + featureId2 + ").";

        final List<String> coreProductList = new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_2));
        List<String> addedSubscriptionPlanIdList = featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount,
            subscriptionPlanResults, expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage,
            coreProductList, assertionErrorList);

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);

        subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId1, productLineSelectText2,
            findFeaturePage, featureDetailPage, assertionErrorList);
        subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription result: " + subscriptionPlanResultCount);

        expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART3 + featureExternalKey1 + " (" + featureId1
            + ")." + " " + subscriptionPlanResultCount + POP_UP_ADD_SUBS_SUCCESS_MSG_PART4;

        addedSubscriptionPlanIdList = featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount,
            subscriptionPlanResults, expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage,
            coreProductList, assertionErrorList);

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);

        featuresHelper.helperForDynamoDbAssertions(bicOfferings3.getOfferings().get(0).getId(),
            bicOfferings4.getOfferings().get(0).getId(), null, featureId2, Action.UPDATE, false,
            getEnvironmentVariables(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the addition of the feature to the plans edited from new to active status
     * through bulk add
     *
     */
    @Test
    public void testAddFeatureToPlansFromNewToActive() {

        GenericGrid subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId1,
            productLineSelectText3, findFeaturePage, featureDetailPage, assertionErrorList);
        int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plans result: " + subscriptionPlanResultCount);

        final String expectedWarnMessage = PelicanConstants.PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE
            + PelicanConstants.PRE_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE
            + subscriptionPlanResults.getColumnValues("ID").size()
            + PelicanConstants.POST_ADD_BULK_SUB_PLAN_POPUP_EXPECTED_MESSAGE;

        String expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey1 + " (" + featureId1 + ").";

        final List<String> coreProductList = new ArrayList<>(
            ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2));
        List<String> addedSubscriptionPlanIdList = featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount,
            subscriptionPlanResults, expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage,
            coreProductList, assertionErrorList);

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);

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

        subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId2, productLineSelectText3,
            findFeaturePage, featureDetailPage, assertionErrorList);
        subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plans result: " + subscriptionPlanResultCount);

        expectedAddFeaturePopUpMessage = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionPlanResultCount
            + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey2 + " (" + featureId2 + ").";

        addedSubscriptionPlanIdList = featuresHelper.helperToAddFeatureToPlans(subscriptionPlanResultCount,
            subscriptionPlanResults, expectedWarnMessage, expectedAddFeaturePopUpMessage, featureDetailPage,
            coreProductList, assertionErrorList);

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);

        featuresHelper.helperForDynamoDbAssertions(bicOfferings5.getOfferings().get(0).getId(),
            bicOfferings6.getOfferings().get(0).getId(), featureId1, featureId2, Action.UPDATE, true,
            getEnvironmentVariables(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * <<<<<<<
     * HEAD:pelican-autobot-ui/src/test/java/com/autodesk/bsm/pelican/ui/features/AddOrRemoveFeatureFunctionalityInBulkFeaturesTest.java
     * This is a test case which will test the removal of the feature to the plans in new status through bulk remove
     *
     */
    @Test
    public void testRemoveFeatureToPlansInNewActiveAndCancelledStatus() {

        final String offeringId = bicOfferings7.getOfferings().get(0).getId();
        final String entitlementId = DbUtils.getSubscriptionEntitlementId(offeringId, getEnvironmentVariables());

        // Find feature by id
        findFeaturePage.findFeatureById(featureId3);
        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId3),
            assertionErrorList);

        // click remove feature link
        featureDetailPage.clickRemoveFeatureFromAllSubs();
        AssertCollector.assertThat("Incorrect bulk remove message",
            featureDetailPage.getPopUpMessageOnRemoveFeatureFromAllSubs(),
            equalTo(PelicanConstants.BULK_REMOVE_MESSAGE1 + numberOfPlansWithFeature
                + PelicanConstants.BULK_REMOVE_MESSAGE2 + featureName3 + " (" + featureExternalKey3 + ")."),
            assertionErrorList);

        // Click remove feature on confirmation pop up
        featureDetailPage.clickRemoveFeatureButtonOnAllSubsPopUp();
        // confirmation message after removing feature
        final String successMessageAfterRemoveFeatureActual =
            featureDetailPage.getPopUpSuccessMessageOnRemoveFeatureFromAllSubs();
        // close pop up
        featureDetailPage.clickOkButtonToCloseUp();

        AssertCollector.assertThat("Success message on remove feature from all subscriptions pop up is not correct",
            successMessageAfterRemoveFeatureActual,
            equalTo(POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART1 + featureName3 + " (" + featureExternalKey3 + ") "
                + POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART2 + numberOfPlansWithRemoveFeature
                + POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART3),
            assertionErrorList);

        // Check subscription plan entitlement after removing feature
        AssertCollector.assertThat("Entitlement count for offering7 is not zero",
            subscriptionPlanResource.getById(bicOfferings7.getOfferings().get(0).getId(), null).getOneTimeEntitlements()
                .getEntitlements().size(),
            equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Entitlement count for offering8 is zero",
            subscriptionPlanResource.getById(bicOfferings8.getOfferings().get(0).getId(), null).getOneTimeEntitlements()
                .getEntitlements().size(),
            equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Entitlement count for offering9 is zero",
            subscriptionPlanResource.getById(bicOfferings9.getOfferings().get(0).getId(), null).getOneTimeEntitlements()
                .getEntitlements().size(),
            equalTo(1), assertionErrorList);

        // Verify audit log data for all subscription plan
        final boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(offeringId,
            entitlementId, null, Action.UPDATE, false, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found for offering 7 ", isAuditLogFound, assertionErrorList);

        // Verify audit log report
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
            offeringId, entitlementId, adminToolUserId, Action.DELETE.toString(), featureName3, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the removal of the feature which don't have any plans through bulk remove
     *
     */
    @Test
    public void testRemoveFeatureWithZeroPlans() {

        // Find feature by id
        findFeaturePage.findFeatureById(featureId4);
        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId4),
            assertionErrorList);

        // click remove feature link
        featureDetailPage.clickRemoveFeatureFromAllSubs();
        AssertCollector.assertThat("Incorrect bulk remove message",
            featureDetailPage.getPopUpMessageOnRemoveFeatureFromAllSubs(),
            equalTo(
                PelicanConstants.BULK_REMOVE_MESSAGE_FOR_ZERO_PLANS + featureName4 + " (" + featureExternalKey4 + ")."),
            assertionErrorList);

        // close pop up
        featureDetailPage.clickOkButtonToCloseUp();
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the search and removal of the feature to the plans in new status through bulk
     * remove
     *
     */
    @Test
    public void testSearchAndRemoveFeatureToPlansInNewActiveAndCancelledStatus() {

        final String offeringId = bicOfferings10.getOfferings().get(0).getId();
        final String entitlementId = DbUtils.getSubscriptionEntitlementId(offeringId, getEnvironmentVariables());

        // Find feature by id
        findFeaturePage.findFeatureById(featureId5);
        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId5),
            assertionErrorList);

        // click remove feature link
        featureDetailPage.clickSearchAndRemoveFeatureFromSubs();
        featureDetailPage.selectProductLine(productLineSelectText5);

        // find subscription plan with selected filters
        final GenericGrid subscriptionPlanResults = featureDetailPage.clickFindSubscriptionPlansToRemove();

        final int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plans result: " + subscriptionPlanResultCount);

        featureDetailPage.clickSelectAll();

        // click on remove button
        featureDetailPage.submitButtonOnPopUpGrid();

        // click confirm on pop up
        featureDetailPage.clickConfirmPopUp();

        final String successMessageAfterRemoveFeatureActual =
            featureDetailPage.getPopUpSuccessMessageOnRemoveFeatureFromAllSubs();
        AssertCollector.assertThat(
            "Success message on remove feature from all subscription plans pop up is not correct",
            successMessageAfterRemoveFeatureActual,
            equalTo(POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART1 + featureName5 + " (" + featureExternalKey5 + ") "
                + POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART2 + numberOfPlansWithRemoveFeature
                + POP_UP_REMOVE_SUBS_SUCCESS_MSG_PART3),
            assertionErrorList);

        // close pop up
        featureDetailPage.clickOkButtonToCloseUp();

        // Check subscription plan entitlement after removing feature
        AssertCollector.assertThat("Entitlement count for offering10 is not zero",
            subscriptionPlanResource.getById(bicOfferings10.getOfferings().get(0).getId(), null)
                .getOneTimeEntitlements().getEntitlements().size(),
            equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Entitlement count for offering11 is zero",
            subscriptionPlanResource.getById(bicOfferings11.getOfferings().get(0).getId(), null)
                .getOneTimeEntitlements().getEntitlements().size(),
            equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Entitlement count for offering12 is zero",
            subscriptionPlanResource.getById(bicOfferings12.getOfferings().get(0).getId(), null)
                .getOneTimeEntitlements().getEntitlements().size(),
            equalTo(1), assertionErrorList);

        // Verify audit log data for all subscription plan
        final boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(offeringId,
            entitlementId, null, Action.UPDATE, false, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found for offering 10 ", isAuditLogFound, assertionErrorList);

        // Verify audit log report
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
            offeringId, entitlementId, adminToolUserId, Action.DELETE.toString(), featureName5, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the search and removal of the feature to the non existing plans through bulk
     * remove
     *
     */
    @Test
    public void testSearchAndRemoveFeatureWithZeroPlans() {

        // Find feature by id
        findFeaturePage.findFeatureById(featureId4);
        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId4),
            assertionErrorList);

        // click remove feature link
        featureDetailPage.clickSearchAndRemoveFeatureFromSubs();
        featureDetailPage.selectProductLine(productLineSelectText5);

        // find subscription plan with selected filters
        final GenericGrid subscriptionPlanResults = featureDetailPage.clickFindSubscriptionPlansToRemove();

        final int subscriptionPlanResultCount = subscriptionPlanResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription plans result: " + subscriptionPlanResultCount);

        AssertCollector.assertThat("Incorrect number of plans available for removal of feature",
            subscriptionPlanResultCount, equalTo(0), assertionErrorList);

        featureDetailPage.clickCancelOnSubscriptionSearchResultsPopUp();

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case validates that if a core product is attached to the feature, it is populated on add core product
     * pop up.
     *
     */

    @Test
    public void testDefaultCoreProductForBulkAddFeature() {
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        String featureId = "";
        // check if feature exists with the given external key
        final Object apiResponse = resource.item().getItemByExternalKey(PelicanConstants.CORE_PRODUCT_AUTO_2,
            getEnvironmentVariables().getAppId());
        // if feature does not exist, create the feature
        if (apiResponse instanceof HttpError) {
            final Item item = featureApiUtils.addFeature(null, PelicanConstants.CORE_PRODUCT_AUTO_2, null);
            featureId = item.getId();
        }

        final Item item = (Item) apiResponse;
        featureId = item.getId();

        final GenericGrid subscriptionPlanResults = featuresHelper.getSubscriptionPlanResultCount(featureId,
            productLineSelectText2, findFeaturePage, featureDetailPage, assertionErrorList);

        final List<String> addedSubscriptionPlanIdList = subscriptionPlanResults.getColumnValues("ID");
        LOGGER.info("Selected subscription id list: " + addedSubscriptionPlanIdList);

        // get default core product
        final List<String> coreProductList = new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_2));

        featureDetailPage.clickSelectAll();
        featureDetailPage.clickSelectSubscriptionPlansToAddFeature();

        AssertCollector.assertThat("Default core product is not correct", featureDetailPage.getCoreProductsDisplay(),
            equalTo(PelicanConstants.CORE_PRODUCT_AUTO_2), assertionErrorList);

        featureDetailPage.clickAddFeature();
        featureDetailPage.clickConfirmPopUp();
        featureDetailPage.clickOkButtonToCloseUp();

        // verify core product is added to the plan
        featuresHelper.helperForCoreProductAssertion(addedSubscriptionPlanIdList, coreProductList,
            findSubscriptionPlanPage, assertionErrorList);
        // verify audit log
        featuresHelper.helperForDynamoDbAssertions(bicOfferings3.getOfferings().get(0).getId(),
            bicOfferings4.getOfferings().get(0).getId(), null, featureId, Action.UPDATE, false,
            getEnvironmentVariables(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
