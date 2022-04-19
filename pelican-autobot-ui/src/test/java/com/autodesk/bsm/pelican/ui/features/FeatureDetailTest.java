package com.autodesk.bsm.pelican.ui.features;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionPlanClient;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.FeatureAuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.features.EditFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This test class verifies the functionality of links under Related Actions on Feature Detail page.
 *
 * @author jains
 */
public class FeatureDetailTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private String appId;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private Offerings bicOffering1;
    private Offerings bicOffering2;
    private String bicOfferingId1;
    private String bicOfferingId2;
    private String bicOfferingId3;
    private String bicOfferingId4;
    private String bicOfferingId5;
    private String bicOfferingId6;
    private String bicOfferingId7;
    private String metaOfferingId1;
    private String metaOfferingId2;
    private String metaOfferingId3;
    private String metaOfferingId4;
    private String metaOfferingId5;
    private String productLineExternalKey1;
    private String productLineExternalKey2;
    private String productLineSelectText1;
    private String productLineSelectText2;
    private String productLineSelectText3;
    private String productLineSelectText4;
    private String featureId;
    private String featureTypeId;
    private String featureTypeName;
    private String featureExternalKey;
    private SubscriptionPlanClient subscriptionPlanResource;
    private FeatureDetailPage featureDetailPage;
    private static FindFeaturePage findFeaturePage;
    private static EditFeaturePage editFeaturePage;
    private String popUpMessageActual;
    private String popUpMessageExpected;
    private String successMessageExpected;
    private String successMessageActual;
    private static final String POP_UP_CONFIRM_REMOVE_SUBS_MSG_PART2 = " subscription plan(s) with feature ";
    private static final String POP_UP_CONFIRM_REMOVE_SUBS_MSG_PART1 = "There are ";
    private static final String POP_UP_HEADER_CONFIRM_SELECT_SUBS_MSG = "Please Confirm";
    private static final String SELECT_SUBSCRIPTION_PLAN_ERROR_MSG = "Please select at least one Subscription Plan";
    private static final String POP_UP_HEADER_ADD_SUBS_MSG_PART1 = "Find Subscription Plans To Add To Feature ";
    private static final String POP_UP_HEADER_SELECT_LICENSING_MODEL =
        "Select a Licensing Model for this Feature Across All Selected Subscription Plans";
    private static final String POP_UP_SELECT_LICENSING_MODEL_MSG_PART1 = "Select a licensing model for feature ";
    private static final String POP_UP_SELECT_LICENSING_MODEL_MSG_PART2 = " that will be added to ";
    private static final String POP_UP_SELECT_LICENSING_MODEL_MSG_PART3 = " subscription plan(s).";
    private static final String POP_UP_HEADER_ADD_SUBS_SUCCESS_HEADER = "Add Feature to Subscription Plans";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 = "Success! ";
    private static final String POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 = " subscription plan(s) were updated with feature ";
    private static final String POP_UP_HEADER_SELECTED_SUBS_SEARCH_RESULTS_ADD_FEATURE_MSG = "Subscription Plan Search "
        + "Results\nSelect one or more subscription plans from the list below to add the feature.";
    private static final String BIC_SUBSCRIPTION = "BiC Subscription";
    private static final String SUBSCRIPTION_RESULTS_HEADER1 = "ID";
    private static final String SUBSCRIPTION_RESULTS_HEADER2 = "Name";
    private static final String SUBSCRIPTION_RESULTS_HEADER3 = "External Key";
    private static final String SUBSCRIPTION_RESULTS_HEADER4 = "Product Line";
    private static final String SUBSCRIPTION_RESULTS_HEADER5 = "Usage Type";
    private static final String SUBSCRIPTION_RESULTS_HEADER6 = "Support Level";
    private static final String SUBSCRIPTION_RESULTS_HEADER7 = "Offering Type";
    private static final String SUBSCRIPTION_RESULTS_HEADER8 = "Status";
    private static final String SUBSCRIPTION_RESULTS_HEADER9 = "Select";
    private static final String ADVANCED_SUPPORT = "Advanced Support";
    private static final String COMMERCIAL = "Commercial";
    private static final String NON_COMMERCIAL = "Non Commercial";
    private static final String NONE = "None";
    private static final String PROPERTY_PREFIX = "v3";
    private static final String TEST_PROPERTY_NAME = "testPropertyName1";
    private static final String TEST_PROPERTY_VALUE = "testPropertyValue1";
    private String appFamilyId;
    private String userId;
    private boolean isAuditLogFound = false;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private static int numberOfPlansWithFeature;
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureDetailTest.class.getSimpleName());

    /*
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    private void setUp() {

        appFamilyId = getEnvironmentVariables().getAppFamilyId();
        userId = getEnvironmentVariables().getUserId();

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        subscriptionPlanResource =
            new SubscriptionPlanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());

        // Add Product Line1
        productLineExternalKey1 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);
        productLineSelectText1 = productLineExternalKey1 + " (" + productLineExternalKey1 + ")";

        // Add Product Line2
        productLineExternalKey2 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);
        productLineSelectText2 = productLineExternalKey2 + " (" + productLineExternalKey2 + ")";

        // Add multiple bic subscription
        bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);
        bicOfferingId1 = bicOffering1.getOfferings().get(0).getId();
        bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.ADVANCED, UsageType.EDU, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);
        bicOfferingId2 = bicOffering2.getOfferings().get(0).getId();
        final Offerings bicOffering3 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey1, OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.ADVANCED, UsageType.NCM,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUs(), 500);
        bicOfferingId3 = bicOffering3.getOfferings().get(0).getId();
        final Offerings bicOffering4 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey2, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUk(), 500);
        bicOfferingId4 = bicOffering4.getOfferings().get(0).getId();
        final Offerings bicOffering5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey1, OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.BASIC, UsageType.GOV,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUs(), 500);
        bicOfferingId5 = bicOffering5.getOfferings().get(0).getId();

        // Add Product Line3
        final String productLineExternalKey3 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey3);
        productLineSelectText3 = productLineExternalKey3 + " (" + productLineExternalKey3 + ")";

        final Offerings bicOffering6 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey3, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUk(), 500);
        bicOfferingId6 = bicOffering6.getOfferings().get(0).getId();

        // Add Product Line4
        final String productLineExternalKey4 = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey4);
        productLineSelectText4 = productLineExternalKey4 + " (" + productLineExternalKey4 + ")";

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey4, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUk(), 500);
        bicOfferingId7 = bicOffering7.getOfferings().get(0).getId();

        // Add multiple meta subscription
        final Offerings metaOffering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey1, OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUs(), 500);
        metaOfferingId1 = metaOffering1.getOfferings().get(0).getId();
        final Offerings metaOffering2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey2, OfferingType.META_SUBSCRIPTION, Status.NEW, SupportLevel.ADVANCED, UsageType.EDU,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUk(), 500);
        metaOfferingId2 = metaOffering2.getOfferings().get(0).getId();
        final Offerings metaOffering3 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey1, OfferingType.META_SUBSCRIPTION, Status.NEW, SupportLevel.ADVANCED, UsageType.NCM,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUs(), 500);
        metaOfferingId3 = metaOffering3.getOfferings().get(0).getId();
        final Offerings metaOffering4 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey2, OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUk(), 500);
        metaOfferingId4 = metaOffering4.getOfferings().get(0).getId();
        final Offerings metaOffering5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(
            productLineExternalKey1, OfferingType.META_SUBSCRIPTION, Status.NEW, SupportLevel.BASIC, UsageType.GOV,
            BillingFrequency.MONTH, 1, getPricelistExternalKeyUk(), 500);
        metaOfferingId5 = metaOffering5.getOfferings().get(0).getId();

        // add a feature and get feature id and feature external key
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey = item.getExternalKey();
        featureId = item.getId();
        featureTypeId = item.getItemType().getId();
        featureTypeName = item.getItemType().getName();
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        adminToolUserId = getEnvironmentVariables().getUserId();
        final Applications applications = resource.application().getApplications();
        if (applications.getApplications().size() > 0) {
            appId = applications.getApplications().get(applications.getApplications().size() - 1).getId();
        }
        editFeaturePage = adminToolPage.getPage(EditFeaturePage.class);
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        subscriptionPlanResource =
            new SubscriptionPlanClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
    }

    /**
     * Verify clicking on Cancel on pop up does not remove feature from subscription plan.
     */
    @Test
    public void testCancelRemoveFeatureFromAllSubscription() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey = item.getExternalKey();
        featureId = item.getId();
        featureTypeId = item.getItemType().getId();
        featureTypeName = item.getItemType().getName();

        // Add multiple bic subscription
        bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);
        bicOfferingId1 = bicOffering1.getOfferings().get(0).getId();
        bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.ADVANCED, UsageType.EDU, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);
        bicOfferingId2 = bicOffering2.getOfferings().get(0).getId();

        // Add entitlement to us subscription plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId1, featureId, null, true);
        numberOfPlansWithFeature++;
        // Add same entitlement to uk subscription plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId2, featureId, null, true);
        numberOfPlansWithFeature++;

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);
        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);

        // remove feature from all subscription plan
        featureDetailPage.clickRemoveFeatureFromAllSubs();
        featureDetailPage.clickCancelButtonOnAllSubsPopUp();
        featureDetailPage.clickRemoveFeatureFromAllSubs();
        popUpMessageActual = featureDetailPage.getPopUpMessageOnRemoveFeatureFromAllSubs();
        popUpMessageExpected = POP_UP_CONFIRM_REMOVE_SUBS_MSG_PART1 + numberOfPlansWithFeature
            + POP_UP_CONFIRM_REMOVE_SUBS_MSG_PART2 + item.getName() + " (" + item.getExternalKey() + ").";

        AssertCollector.assertThat("Pop up message on remove feature from all subscriptions is not correct",
            popUpMessageActual, equalTo(popUpMessageExpected), assertionErrorList);

        // click on cancel to close pop up
        featureDetailPage.clickCancelButtonOnAllSubsPopUp();

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the Find subscription plans under related action. By Clicking on find subscription plans,
     * grid will be opened of subscription plans which are associated with that feature.
     */
    @Test
    public void testFindSubscriptionPlansUnderRelatedActions() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey = item.getExternalKey();
        featureId = item.getId();
        featureTypeId = item.getItemType().getId();
        featureTypeName = item.getItemType().getName();

        // Add entitlement to bic subscription plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId1, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId2, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId3, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId4, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId5, featureId, null, true);

        // Add entitlement to meta subscription plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOfferingId1, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOfferingId2, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOfferingId3, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOfferingId4, featureId, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOfferingId5, featureId, null, true);

        findFeaturePage.findFeatureById(featureId);
        final GenericDetails genericDetails = adminToolPage.getPage(GenericDetails.class);
        final String externalKeyOfFeature = genericDetails.getValueByField("External Key");
        final String nameOffeature = genericDetails.getValueByField("Name");
        final String featureType = genericDetails.getValueByField("Feature Type");
        featureDetailPage.clickFindSubscriptionPlans();

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);

        if (getDriver().getTitle().equals("Pelican - Feature Detail")) {
            LOGGER.info("Report page is not yet loaded so waiting for some time.");

        }
        AssertCollector.assertThat("Page title is not correct", getDriver().getTitle(),
            equalTo("Pelican - Subscription Plans and Features Report"), assertionErrorList);
        AssertCollector.assertThat("Total number of results are not correct", grid.getTotalItems(), equalTo(10),
            assertionErrorList);
        AssertCollector.assertThat("Feature external key is not same as shown on Detail Page of Feature",
            grid.getColumnValues("Feature Ext Key"), everyItem(equalTo(externalKeyOfFeature)), assertionErrorList);
        AssertCollector.assertThat("Feature name is not same as shown on Detail Page of Feature",
            grid.getColumnValues("Feature Name"), everyItem(equalTo(nameOffeature)), assertionErrorList);
        AssertCollector.assertThat("Feature type name is not same as shown on Detail Page of Feature",
            grid.getColumnValues("Feature Type Name"), everyItem(equalTo(featureType)), assertionErrorList);
        AssertCollector.assertThat("Plan IDs don't match with the Plan IDs which are added",
            grid.getColumnValues("Plan ID"),
            everyItem(isOneOf(bicOfferingId1, bicOfferingId2, bicOfferingId3, bicOfferingId4, bicOfferingId5,
                metaOfferingId1, metaOfferingId2, metaOfferingId3, metaOfferingId4, metaOfferingId5)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify whether a feature is added to selected subscription plan or not. Disabling Audit log asserts due to Defect
     * in Auditlog Report Page for "CREATE" action for onetime entitlement.
     */
    @Test(dataProvider = "dataForAddFeatureToSelectedSubscriptionPlan")
    public void testAddNewFeatureToSubscriptionPlansWithAndWithoutLicensingModel(final String productLineSelectText,
        final String productLine, final String usageType, final String supportLevel,
        final String licensingModelExternalKey) {
        // remove feature from all subscription before starting test.
        cleanUpEntitlementFromAllSubscriptionPlan(featureId);

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);

        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);

        // click add feature link
        featureDetailPage.clickAddFeatureLink();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        String popUpHeaderActual = featureDetailPage.getPopUpHeaderOnFeatureFirstPopUp();
        String popUpHeaderExpected = POP_UP_HEADER_ADD_SUBS_MSG_PART1 + featureExternalKey + " (" + featureId + ")";

        AssertCollector.assertThat(
            "Pop up header on Find Subscription Plans To Add To Feature(first pop up) is not correct",
            popUpHeaderActual, equalTo(popUpHeaderExpected), assertionErrorList);

        // select search criteria
        if (productLineSelectText != null) {
            featureDetailPage.selectProductLine(productLineSelectText);
        }
        if (usageType != null) {
            featureDetailPage.selectUsageType(usageType);
        }
        if (supportLevel != null) {
            featureDetailPage.selectSupportLevel(supportLevel);
        }

        // find subscription with selected filters
        final GenericGrid subscriptionResults = featureDetailPage.clickFindSubscriptionPlansToAddFeature();

        // verify pop up header on subscription search results page
        popUpHeaderActual = featureDetailPage.getPopUpHeaderOnFeatureSecondPopUp();
        popUpHeaderExpected = POP_UP_HEADER_SELECTED_SUBS_SEARCH_RESULTS_ADD_FEATURE_MSG;

        AssertCollector.assertThat(
            "Pop up header on Subscription Plan Search Results For Feature(second pop up) is not correct",
            popUpHeaderActual, equalTo(popUpHeaderExpected), assertionErrorList);

        // verify headers on subscription search results page
        final List<String> subscriptionResultHeaderList = subscriptionResults.getColumnHeaders();
        AssertCollector.assertThat("Incorrect subscription result header1", subscriptionResultHeaderList.get(0),
            equalTo(SUBSCRIPTION_RESULTS_HEADER1), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header2", subscriptionResultHeaderList.get(1),
            equalTo(SUBSCRIPTION_RESULTS_HEADER2), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header3", subscriptionResultHeaderList.get(2),
            equalTo(SUBSCRIPTION_RESULTS_HEADER3), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header4", subscriptionResultHeaderList.get(3),
            equalTo(SUBSCRIPTION_RESULTS_HEADER4), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header5", subscriptionResultHeaderList.get(4),
            equalTo(SUBSCRIPTION_RESULTS_HEADER5), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header6", subscriptionResultHeaderList.get(5),
            equalTo(SUBSCRIPTION_RESULTS_HEADER6), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header7", subscriptionResultHeaderList.get(6),
            equalTo(SUBSCRIPTION_RESULTS_HEADER7), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header8", subscriptionResultHeaderList.get(7),
            equalTo(SUBSCRIPTION_RESULTS_HEADER8), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription result header9", subscriptionResultHeaderList.get(8),
            equalTo(SUBSCRIPTION_RESULTS_HEADER9), assertionErrorList);

        final int subscriptionResultCount = subscriptionResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription result: " + subscriptionResultCount);

        if (subscriptionResultCount != 0) {
            AssertCollector.assertThat("Subscription Result should show only BIC ofefring",
                subscriptionResults.getColumnValues("Offering Type"), everyItem(equalTo(BIC_SUBSCRIPTION)),
                assertionErrorList);
            if (productLine != null) {
                AssertCollector.assertThat("Subscription Result did not filter product line correctly",
                    subscriptionResults.getColumnValues("Product Line"), everyItem(equalTo(productLine)),
                    assertionErrorList);
            }
            if (usageType != null) {
                AssertCollector.assertThat("Subscription Result did not filter Usage type correctly",
                    subscriptionResults.getColumnValues("Usage Type"), everyItem(equalTo(usageType)),
                    assertionErrorList);
            }
            if (supportLevel != null) {
                AssertCollector.assertThat("Subscription Result did not filter Support level correctly",
                    subscriptionResults.getColumnValues("Support Level"), everyItem(equalTo(supportLevel)),
                    assertionErrorList);
            }

            // click on Select Plans to Add Feature without selecting plans to
            // get the error message
            LOGGER.info("Cicking on select subscription plan button without selecting subscription plan");
            featureDetailPage.clickSelectSubscriptionPlansToAddFeature();

            final String selectPlanErrorMessage = featureDetailPage.getSelectSubscriptionPlanErrorMessage();

            AssertCollector.assertThat("Error message to select subscription plan is not correct",
                selectPlanErrorMessage, equalTo(SELECT_SUBSCRIPTION_PLAN_ERROR_MSG), assertionErrorList);

            // select all subscription plan and get list of all the subscription
            // id
            final List<String> addedSubscriptionIdList = subscriptionResults.getColumnValues("ID");
            LOGGER.info("Selected subscription id list: " + addedSubscriptionIdList);
            featureDetailPage.clickSelectAll();

            featureDetailPage.clickSelectSubscriptionPlansToAddFeature();

            // checking header on select licensing pop up
            popUpHeaderActual = featureDetailPage.getPopUpHeaderOnAddFeatureThirdPopUp();
            popUpMessageActual = featureDetailPage.getPopUpMessageOnAddFeatureThirdPopUp();
            popUpMessageExpected = POP_UP_SELECT_LICENSING_MODEL_MSG_PART1 + featureExternalKey + " (" + featureId + ")"
                + POP_UP_SELECT_LICENSING_MODEL_MSG_PART2 + subscriptionResultCount
                + POP_UP_SELECT_LICENSING_MODEL_MSG_PART3;

            AssertCollector.assertThat("Pop up header on select licensing model is not correct", popUpHeaderActual,
                equalTo(POP_UP_HEADER_SELECT_LICENSING_MODEL), assertionErrorList);
            AssertCollector.assertThat("Pop up message on select licensing model pop up is not correct",
                popUpMessageActual, equalTo(popUpMessageExpected), assertionErrorList);

            // select Licensing Model
            if (licensingModelExternalKey != null) {
                featureDetailPage.selectLicensingModel(PelicanConstants.RETAIL_LICENSING_MODEL_NAME);
            }

            featureDetailPage.clickAddFeature();

            // after clicking add feature
            popUpHeaderActual = featureDetailPage.getConfirmPopUpHeader();
            popUpMessageActual = featureDetailPage.getConfirmPopUpMessage();
            popUpMessageExpected = PelicanConstants.PRE_ADD_BULK_SUB_PLAN_BLOCK_MESSAGE
                + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                    getEnvironmentVariables())
                + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE_HOURS;

            AssertCollector.assertThat("Pop up header on add feature to subscription plans is not correct",
                popUpHeaderActual, equalTo(POP_UP_HEADER_CONFIRM_SELECT_SUBS_MSG), assertionErrorList);
            AssertCollector.assertThat(
                "Pop up message on add feature to selected subscriptions confirmation pop up is not correct",
                popUpMessageActual, equalTo(popUpMessageExpected), assertionErrorList);

            // click confirm on pop up
            featureDetailPage.clickConfirmPopUp();

            successMessageActual = featureDetailPage.getPopUpSuccessMessageOnAddFeature();

            successMessageExpected = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + subscriptionResultCount
                + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey + " (" + featureId + ").";
            final String successHeaderAfterRemoveFeatureActual = featureDetailPage.getPopUpHeaderOnFeatureSecondPopUp();

            AssertCollector.assertThat("Success header on add feature to subscription plan pop up is not correct",
                successHeaderAfterRemoveFeatureActual, equalTo(POP_UP_HEADER_ADD_SUBS_SUCCESS_HEADER),
                assertionErrorList);

            AssertCollector.assertThat("Success message on add feature to subscription plan pop up is not correct",
                successMessageExpected, equalTo(successMessageActual), assertionErrorList);

            // close pop up
            featureDetailPage.clickOkButtonToCloseUp();

            // Check all subscription entitlement after adding feature
            final Iterator<String> iAddedSubscriptionId = addedSubscriptionIdList.iterator();
            if (iAddedSubscriptionId.hasNext()) {
                final String addedSubscriptionId = iAddedSubscriptionId.next();
                final String subscriptionEntitlementId = subscriptionPlanResource.getById(addedSubscriptionId, null)
                    .getOneTimeEntitlements().getEntitlements().get(0).getId();
                final String subscriptionEntitlementLicensingModelExternalKey =
                    subscriptionPlanResource.getById(addedSubscriptionId, null).getOneTimeEntitlements()
                        .getEntitlements().get(0).getLicensingModelExternalKey();

                AssertCollector.assertThat(
                    "Entitlement id for subscription plan id " + addedSubscriptionId + " is not correct",
                    subscriptionEntitlementId, equalTo(featureId), assertionErrorList);
                AssertCollector.assertThat(
                    "Licensing Model External Key for subscription plan id " + addedSubscriptionId + " is not correct",
                    subscriptionEntitlementLicensingModelExternalKey, equalTo(licensingModelExternalKey),
                    assertionErrorList);

                // Verify audit log data for all subscription plan
                final String idFromSubscriptionEntitlementTable =
                    DbUtils.getSubscriptionEntitlementId(addedSubscriptionId, getEnvironmentVariables());
                String licensingModelNewValue = null;
                if (licensingModelExternalKey != null) {
                    licensingModelNewValue =
                        DbUtils.getLicensingModelId(appFamilyId, licensingModelExternalKey, getEnvironmentVariables());
                }

                // Verify audit log data for all subscription plan
                isAuditLogFound =
                    SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(addedSubscriptionId, null,
                        idFromSubscriptionEntitlementTable, Action.UPDATE, false, assertionErrorList);
                AssertCollector.assertTrue("Audit log not found for subscription plan entitlement", isAuditLogFound,
                    assertionErrorList);

                // Verify audit log data for subscription entitlement table
                isAuditLogFound = SubscriptionPlanAuditLogHelper.validateFeatureEntitlementInDynamoDB(null,
                    addedSubscriptionId, null, featureId, null, idFromSubscriptionEntitlementTable, null,
                    licensingModelNewValue, null, null, Action.CREATE, assertionErrorList);
                AssertCollector.assertTrue("Audit log not found for subscription entitlement table", isAuditLogFound,
                    assertionErrorList);
            }
        } else {
            LOGGER.info("No subscriptions found matching with search criteria");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify that subscription plan should not be updated if subscription plan already has the feature.
     */
    @Test(dataProvider = "dataForAddExistingFeatureToSubscriptionPlans")
    public void testAddExistingFeatureToSubscriptionPlansWithAndWithoutLicensingModel(
        final String productLineSelectText, final String offeringIdWithFeature, final String licensingModelExternalKey,
        final String licensingModelName) {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey = item.getExternalKey();
        featureId = item.getId();
        featureTypeId = item.getItemType().getId();
        featureTypeName = item.getItemType().getName();

        // remove feature from all subscription before starting test.
        cleanUpEntitlementFromAllSubscriptionPlan(featureId);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(offeringIdWithFeature, featureId,
            licensingModelExternalKey, false);

        // getting last modified date after adding feature from api and before
        // adding it through admin tool
        final String subscriptionPlanLastModifiedBeforeAddingFeature =
            DbUtils.selectQuery("Select LAST_MODIFIED from offering where ID = " + offeringIdWithFeature,
                "LAST_MODIFIED", getEnvironmentVariables()).get(0);
        LOGGER.info("Subscription Plan Last Modified Before Adding Feature: " + offeringIdWithFeature + " ---"
            + subscriptionPlanLastModifiedBeforeAddingFeature);

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);

        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);

        // click add feature link
        featureDetailPage.clickAddFeatureLink();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // select product line
        if (productLineSelectText != null) {
            featureDetailPage.selectProductLine(productLineSelectText);
        }

        // find subscription with selected filters
        final GenericGrid subscriptionResults = featureDetailPage.clickFindSubscriptionPlansToAddFeature();

        final int subscriptionResultCount = subscriptionResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription result: " + subscriptionResultCount);

        if (subscriptionResultCount != 0) {

            // select all subscription plan and get list of all the subscription
            // id
            final List<String> addedSubscriptionIdList = subscriptionResults.getColumnValues("ID");
            LOGGER.info("Selected subscription id list: " + addedSubscriptionIdList);
            featureDetailPage.clickSelectAll();

            featureDetailPage.clickSelectSubscriptionPlansToAddFeature();

            // select Licensing Model
            if (licensingModelName != null) {
                featureDetailPage.selectLicensingModel(licensingModelName);
            }

            featureDetailPage.clickAddFeature();

            // click confirm on pop up
            featureDetailPage.clickConfirmPopUp();

            successMessageActual = featureDetailPage.getPopUpSuccessMessageOnAddFeature();
            successMessageExpected = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + (subscriptionResultCount - 1)
                + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2 + featureExternalKey + " (" + featureId + ").";
            final String successHeaderAfterRemoveFeatureActual = featureDetailPage.getPopUpHeaderOnFeatureSecondPopUp();

            AssertCollector.assertThat("Success header on add feature to subscription plan pop up is not correct",
                successHeaderAfterRemoveFeatureActual, equalTo(POP_UP_HEADER_ADD_SUBS_SUCCESS_HEADER),
                assertionErrorList);
            AssertCollector.assertThat("Success message on add feature to subscription plan pop up is not correct",
                successMessageActual, equalTo(successMessageExpected), assertionErrorList);

            // close pop up
            featureDetailPage.clickOkButtonToCloseUp();

            // Check all subscription entitlement after adding feature
            final Iterator<String> iAddedSubscriptionId = addedSubscriptionIdList.iterator();
            if (iAddedSubscriptionId.hasNext()) {
                final String addedSubscriptionId = iAddedSubscriptionId.next();
                final int numberOfSubscriptionEntitlement = subscriptionPlanResource.getById(addedSubscriptionId, null)
                    .getOneTimeEntitlements().getEntitlements().size();
                final String subscriptionEntitlementId = subscriptionPlanResource.getById(addedSubscriptionId, null)
                    .getOneTimeEntitlements().getEntitlements().get(0).getId();
                final String subscriptionEntitlementLicensingModelExternalKey =
                    subscriptionPlanResource.getById(addedSubscriptionId, null).getOneTimeEntitlements()
                        .getEntitlements().get(0).getLicensingModelExternalKey();

                AssertCollector.assertThat(
                    "Entitlement count for subscription plan id " + addedSubscriptionId + " should be 1",
                    numberOfSubscriptionEntitlement, equalTo(1), assertionErrorList);

                AssertCollector.assertThat(
                    "Entitlement id for subscription plan id " + addedSubscriptionId + " is not correct",
                    subscriptionEntitlementId, equalTo(featureId), assertionErrorList);
                AssertCollector.assertThat(
                    "Licensing Model External Key for subscription plan id " + addedSubscriptionId + " is not correct",
                    subscriptionEntitlementLicensingModelExternalKey, equalTo(licensingModelExternalKey),
                    assertionErrorList);
            }
            // getting last modified date after adding feature from db
            final String subscriptionPlanLastModifiedAfterAddingFeature =
                DbUtils.selectQuery("Select LAST_MODIFIED from offering where ID = " + offeringIdWithFeature,
                    "LAST_MODIFIED", getEnvironmentVariables()).get(0);
            LOGGER.info("Subscription Plan Last Modified After Adding Feature "
                + subscriptionPlanLastModifiedAfterAddingFeature);
            AssertCollector.assertThat("Subscription plan with existing feature should not have been updated.",
                subscriptionPlanLastModifiedBeforeAddingFeature,
                equalTo(subscriptionPlanLastModifiedAfterAddingFeature), assertionErrorList);

        } else {
            LOGGER.info("No subscriptions found matching with search criteria");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method validates that subscription plan is updated when licensing model of an existing one time entitlement
     * is updated.
     */
    @Test(dataProvider = "dataForAddFeatureWithLicensingModelUpdateToSubscriptionPlan")
    public void testAddFeatureWithLicensingModelUpdateToSubscriptionPlan(final String productLineSelectText,
        final String offeringIdWithFeature, final String oldLicensingModelExternalKey,
        final String newLicensingModelExternalKey) {
        // remove feature from all subscription before starting test.
        cleanUpEntitlementFromAllSubscriptionPlan(featureId);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(offeringIdWithFeature, featureId,
            oldLicensingModelExternalKey, false);

        // getting last modified date after adding feature from api and before
        // adding it through admin tool
        final String subscriptionPlanLastModifiedBeforeAddingFeature =
            DbUtils.selectQuery("Select LAST_MODIFIED from offering where ID = " + offeringIdWithFeature,
                "LAST_MODIFIED", getEnvironmentVariables()).get(0);
        LOGGER.info("Subscription Plan Last Modified Before Adding Feature: " + offeringIdWithFeature + " ---"
            + subscriptionPlanLastModifiedBeforeAddingFeature);

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);

        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);

        // click add feature link
        featureDetailPage.clickAddFeatureLink();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // select product line
        if (productLineSelectText != null) {
            featureDetailPage.selectProductLine(productLineSelectText);
        }

        // find subscription with selected filters
        final GenericGrid subscriptionResults = featureDetailPage.clickFindSubscriptionPlansToAddFeature();

        final int subscriptionResultCount = subscriptionResults.getColumnValues("Name").size();
        LOGGER.info("Number of subscription result: " + subscriptionResultCount);

        if (subscriptionResultCount != 0) {
            // select subscription plan with id as offeringIdWithFeature
            for (int i = 0; i < subscriptionResultCount; i++) {
                if (subscriptionResults.getColumnValues("ID").get(i).equals(offeringIdWithFeature)) {
                    subscriptionResults.selectResultColumnWithName("Select", i);
                }
            }

            featureDetailPage.clickSelectSubscriptionPlansToAddFeature();

            // select Licensing Model
            if (newLicensingModelExternalKey != null) {
                featureDetailPage.selectLicensingModel(PelicanConstants.RETAIL_LICENSING_MODEL_NAME);
            }

            featureDetailPage.clickAddFeature();

            // click confirm on pop up
            featureDetailPage.clickConfirmPopUp();
            successMessageActual = featureDetailPage.getPopUpSuccessMessageOnAddFeature();
            successMessageExpected = POP_UP_ADD_SUBS_SUCCESS_MSG_PART1 + "1" + POP_UP_ADD_SUBS_SUCCESS_MSG_PART2
                + featureExternalKey + " (" + featureId + ").";
            final String successHeaderAfterAddingFeatureActual = featureDetailPage.getPopUpHeaderOnFeatureSecondPopUp();

            AssertCollector.assertThat("Success header on add feature to subscription plan pop up is not correct",
                successHeaderAfterAddingFeatureActual, equalTo(POP_UP_HEADER_ADD_SUBS_SUCCESS_HEADER),
                assertionErrorList);
            AssertCollector.assertThat("Success message on add feature to subscription plan pop up is not correct",
                successMessageActual, equalTo(successMessageExpected), assertionErrorList);

            // close pop up
            featureDetailPage.clickOkButtonToCloseUp();

            // Check all subscription entitlement after adding feature
            final int numberOfSubscriptionEntitlement = subscriptionPlanResource.getById(offeringIdWithFeature, null)
                .getOneTimeEntitlements().getEntitlements().size();
            final String subscriptionEntitlementId = subscriptionPlanResource.getById(offeringIdWithFeature, null)
                .getOneTimeEntitlements().getEntitlements().get(0).getId();
            final String subscriptionEntitlementLicensingModelExternalKey =
                subscriptionPlanResource.getById(offeringIdWithFeature, null).getOneTimeEntitlements().getEntitlements()
                    .get(0).getLicensingModelExternalKey();

            AssertCollector.assertThat(
                "Entitlement count for subscription plan id " + offeringIdWithFeature + " should be 1",
                numberOfSubscriptionEntitlement, equalTo(1), assertionErrorList);

            AssertCollector.assertThat(
                "Entitlement id for subscription plan id " + offeringIdWithFeature + " is not correct",
                subscriptionEntitlementId, equalTo(featureId), assertionErrorList);
            AssertCollector.assertThat(
                "Licensing Model External Key for subscription plan id " + offeringIdWithFeature + " is not correct",
                subscriptionEntitlementLicensingModelExternalKey, equalTo(newLicensingModelExternalKey),
                assertionErrorList);

            // getting last modified date after adding feature from db
            final String subscriptionPlanLastModifiedAfterAddingFeature =
                DbUtils.selectQuery("Select LAST_MODIFIED from offering where ID = " + offeringIdWithFeature,
                    "LAST_MODIFIED", getEnvironmentVariables()).get(0);
            LOGGER.info("Subscription Plan Last Modified After Adding Feature "
                + subscriptionPlanLastModifiedAfterAddingFeature);
            AssertCollector.assertThat("Subscription plan with existing feature should not have been updated.",
                subscriptionPlanLastModifiedBeforeAddingFeature, not(subscriptionPlanLastModifiedAfterAddingFeature),
                assertionErrorList);

            // Verify audit log data for all subscription plan
            final String idFromSubscriptionEntitlementTable =
                DbUtils.getSubscriptionEntitlementId(offeringIdWithFeature, getEnvironmentVariables());
            String newLicensingModelId = null;
            String oldLicensingModelId = null;

            if (newLicensingModelExternalKey != null) {
                newLicensingModelId =
                    DbUtils.getLicensingModelId(appFamilyId, newLicensingModelExternalKey, getEnvironmentVariables());
            }
            if (oldLicensingModelExternalKey != null) {
                oldLicensingModelId =
                    DbUtils.getLicensingModelId(appFamilyId, oldLicensingModelExternalKey, getEnvironmentVariables());
            }
            // Verify audit log data for all subscription plan
            isAuditLogFound = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(offeringIdWithFeature,
                null, null, Action.UPDATE, false, assertionErrorList);
            AssertCollector.assertTrue("Audit log not found for subscription plan entitlement", isAuditLogFound,
                assertionErrorList);

            // Verify audit log data for subscription entitlement table
            isAuditLogFound = SubscriptionPlanAuditLogHelper.validateFeatureEntitlementInDynamoDB(null, null, null,
                null, idFromSubscriptionEntitlementTable, idFromSubscriptionEntitlementTable, oldLicensingModelId,
                newLicensingModelId, null, null, Action.UPDATE, assertionErrorList);
            AssertCollector.assertTrue("Audit log not found for subscription entitlement table", isAuditLogFound,
                assertionErrorList);

        } else {
            LOGGER.info("No subscriptions found matching with search criteria");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the 'Make a copy' of this feature functionality
     */
    @Test
    public void testMakeFeatureCopyAndEditNameSuccess() {

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);

        // Click on 'Make a copy' link
        featureDetailPage.clickMakeFeatureCopyLink();
        // Get the Feature id of the copied feature
        featureId = featureDetailPage.getFeatureId();
        String status = null;
        if (featureDetailPage.getActive().equalsIgnoreCase(PelicanConstants.YES)) {
            status = PelicanConstants.TRUE.toLowerCase();
        }

        final String copiedFeatureName = featureDetailPage.getFeatureName();
        final String copiedFeatureExternalKey = featureDetailPage.getFeatureExternalKey();
        FeatureAuditLogHelper.validateFeatureData(featureId, null, appId, null, featureTypeId, null, copiedFeatureName,
            null, featureDetailPage.getFeatureExternalKey(), null, null, Action.CREATE, userId, null, null, status,
            assertionErrorList);

        // Verify Audit Log for Create Action
        HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_FEATURE, null,
                featureId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, null,
            copiedFeatureName, null, copiedFeatureExternalKey, null, featureTypeName + " (" + featureTypeId + ")", null,
            status, assertionErrorList);

        // Edit the name of the copied feature
        findFeaturePage.findFeatureById(featureId);

        featureDetailPage.edit();

        final String newFeatureName = "testModifiedFeatureName";
        editFeaturePage.setFeatureName(newFeatureName);
        featureDetailPage = editFeaturePage.clickOnUpdateFeatureButton();

        FeatureAuditLogHelper.validateFeatureData(featureId, null, null, null, null, copiedFeatureName,
            featureDetailPage.getFeatureName(), null, null, null, null, Action.UPDATE, userId, null, null, null,
            assertionErrorList);

        // Verify Audit Log for Update Action
        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
            DateTimeUtils.getNowAsUTCPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 0),
            PelicanConstants.ENTITY_FEATURE, null, featureId, adminToolUserId, Action.UPDATE.toString(), null,
            assertionErrorList);

        FeatureAuditLogReportHelper.assertionsForFeatureInAuditLogReport(descriptionPropertyValues, copiedFeatureName,
            newFeatureName, null, null, null, null, null, null, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests adding properties to a feature
     */
    @Test(dependsOnMethods = { "testMakeFeatureCopyAndEditNameSuccess" })
    public void testAddPropertyToFeatureSuccess() {

        // Find feature by id
        findFeaturePage.findFeatureById(featureId);

        // Add a property to the feature
        featureDetailPage.setProperty(TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);
        featureDetailPage.clickOnUpdatePropertiesButton();

        FeatureAuditLogHelper.validateFeatureData(featureId, null, null, null, null, null, null, null, null,
            PROPERTY_PREFIX, FeatureAuditLogHelper
                .getPropertyValueFormatInAuditLog(PROPERTY_PREFIX + TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE),
            Action.UPDATE, userId, null, null, null, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testRemoveFeatureFromSelectedSubscriptionPlan method
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForRemoveFeatureFromSelectedSubscriptionPlan")
    public Object[][] getTestData() {
        return new Object[][] {
                // data for no subscription found
                { productLineSelectText1, productLineExternalKey1, null, null, null },
                { null, null, BIC_SUBSCRIPTION, null, null }, { null, null, null, COMMERCIAL, null },
                { null, null, null, null, NONE }, { null, null, null, null, null } };
    }

    /**
     * Data provider for testAddFeatureToSubscriptionPlansWithLicensingModel method
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForAddFeatureToSelectedSubscriptionPlan")
    public Object[][] getTestDataForAddFeatureToSubscriptionPlansWithLicensingModel() {
        return new Object[][] {
                { productLineSelectText2, productLineExternalKey2, null, ADVANCED_SUPPORT,
                        PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY },
                { productLineSelectText1, productLineExternalKey1, NON_COMMERCIAL, ADVANCED_SUPPORT,
                        PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY } };
    }

    /**
     * Data provider for testAddExistingFeatureToSubscriptionPlans method dataFor
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForAddExistingFeatureToSubscriptionPlans")
    public Object[][] getTestDataForAddExistingFeatureToSubscriptionPlansWithAndWithoutLicensingModel() {
        return new Object[][] { { productLineSelectText3, bicOfferingId6, null, null },
                { productLineSelectText4, bicOfferingId7, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY,
                        PelicanConstants.RETAIL_LICENSING_MODEL_NAME } };
    }

    /**
     * Data provider for testAddExistingFeatureToSubscriptionPlans method dataFor
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForAddFeatureWithLicensingModelUpdateToSubscriptionPlan")
    public Object[][] getTestDataForAddFeatureWithLicensingModelUpdateToSubscriptionPlan() {
        return new Object[][] {
                { productLineSelectText1, bicOfferingId1, null, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY },
                { productLineSelectText2, bicOfferingId2, PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY,
                        null } };
    }

    /**
     * Method to remove feature from all subscription plans.
     */
    private void cleanUpEntitlementFromAllSubscriptionPlan(final String featureId) {
        // Remove feature from all subscriptions
        findFeaturePage.findFeatureById(featureId);

        featureDetailPage.clickRemoveFeatureFromAllSubs();
        // Click remove feature on confirmation pop up
        try {
            featureDetailPage.clickRemoveFeatureButtonOnAllSubsPopUp();
        } catch (final Exception e) {
            LOGGER.info("There are no subscriptions associated with the feature");
        } finally {
            // close pop up
            featureDetailPage.clickOkButtonToCloseUp();
        }
    }

}
