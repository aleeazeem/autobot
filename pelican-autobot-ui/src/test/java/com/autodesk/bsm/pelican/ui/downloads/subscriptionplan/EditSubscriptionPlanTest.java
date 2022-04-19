package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAndBasicOfferingsAuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanDynamoQuery;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.AuditLogReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanGenericPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the test class created to test Edit Subscription Plan Page. As of now, tests related to edit subscription
 * plan are added
 *
 * @author vineel
 */
public class EditSubscriptionPlanTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private AddSubscriptionPlanPage addSubscriptionPlanPage;
    private EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static String productLineNameAndExternalKey;
    private static String newProductLineNameAndExternalKey;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private AuditLogReportResultPage auditLogReportResultPage;
    private AuditLogReportPage auditLogReportPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private SubscriptionPlanGenericPage subscriptionPlanGenericPage;
    private FindSubscriptionPlanPage findSubscriptionPlanPage;

    private static final String SUBSCRIPTION_OFFER_ORIGINAL_PRICE = "40";
    private static String namerStoreName;
    private static String nameOfNamerPriceList;

    private Offerings bicOfferings1;
    private String featureId1;
    private String featureId2;
    private String featureId3;
    private String featureExtKey1;
    private String featureExtKey2;
    private String featureExtKey3;
    private String featureName1;
    private String featureName2;
    private PelicanPlatform resource;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());

        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        auditLogReportResultPage = adminToolPage.getPage(AuditLogReportResultPage.class);
        auditLogReportPage = adminToolPage.getPage(AuditLogReportPage.class);
        adminToolUserId = getEnvironmentVariables().getUserId();

        // Add product Line
        final ProductLine productLine = new ProductLine();
        final ProductLineData productLineData = new ProductLineData();
        String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        subscriptionPlanApiUtils.addProductLine(resource, productLine);
        productLineNameAndExternalKey = productLine.getData().getName() + " (" + productLine.getData().getName() + ")";

        // Add product Line
        productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        subscriptionPlanApiUtils.addProductLine(resource, productLine);
        newProductLineNameAndExternalKey =
            productLine.getData().getName() + " (" + productLine.getData().getName() + ")";

        namerStoreName = getStoreUs().getName();
        nameOfNamerPriceList = getStoreUs().getIncluded().getPriceLists().get(0).getName();

        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        subscriptionPlanGenericPage = adminToolPage.getPage(SubscriptionPlanGenericPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        // Add multiple bic subscriptions
        bicOfferings1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, null, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUk(), 500);

        // add a feature and get feature id and feature external key
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item1 = featureApiUtils.addFeature(null, null, null);
        featureId1 = item1.getId();
        featureExtKey1 = item1.getExternalKey();
        featureName1 = item1.getName();

        final Item item2 = featureApiUtils.addFeature(null, null, null);
        featureId2 = item2.getId();
        featureExtKey2 = item2.getExternalKey();
        featureName2 = item2.getName();

        final Item item3 = featureApiUtils.addFeature(null, null, null);
        featureId3 = item3.getId();
        featureExtKey3 = item3.getExternalKey();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            featureId1, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            featureId2, null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            featureId3, null, true);
    }

    @BeforeClass(alwaysRun = true)
    public void tearDown() {
        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
    }

    /**
     * This is a test which will test whether product line and usage type are required fields in add subscription plan
     */
    @Test
    public void testProductLineAndUsageTypeAsRequiredInEditSubscriptionPlan() {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, null, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editUsageType(null);
        editSubscriptionPlanPage.editProductLine(null);
        editSubscriptionPlanPage.clickOnSave(false);

        final String usageTypeErrorMessage = editSubscriptionPlanPage.getUsageTypeErrorMessage();
        final String productLineErrorMessage = editSubscriptionPlanPage.getProductLineErrorMessage();
        commonAssertionsForErrorMessages(usageTypeErrorMessage, productLineErrorMessage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the random external key if no external key is provided by the user when
     * editing a subscription plan in the admin tool
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testRandomExternalKeyGeneratedInEditSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanDetailPage = editSubscriptionPlanExternalKey("");
        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, null, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will edit the packaging type from none to industry collection and vertical Grouping
     */
    @Test(dataProvider = "getPackagingTypes")
    public void testEditPackagingTypeFromNoneToExistingPackagingTypesInSubscriptionPlan(
        final PackagingType packagingType) {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect packaging type", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(PelicanConstants.NONE), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, packagingType, true);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect packaging type", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(packagingType.getDisplayName()), assertionErrorList);

        final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(planId).setOldName(null).setNewName(null).setOldExternalKey(null)
            .setNewExternalKey(null).setOldOfferingType(null).setNewOfferingType(null).setOldStatus(null)
            .setNewStatus(null).setOldCancellationPolicy(null).setNewCancellationPolicy(null).setOldUsageType(null)
            .setNewUsageType(null).setOldOfferingDetailId(null).setNewOfferingDetailId(null).setOldProductLine(null)
            .setNewProductLine(null).setOldSupportLevel(null).setNewSupportLevel(null).setAction(Action.UPDATE)
            .setFileName(null).setOldPackagingValue(null).setNewPackagingValue(packagingType)
            .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(null).build();

        SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        // Verify Audit Log report results for subscription plan id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, planId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanInAuditLogReportDescription(
            descriptionPropertyValues, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, getEnvironmentVariables(), null, packagingType, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will edit the packaging type from industry collection and vertical grouping to none
     */
    @Test(dataProvider = "getPackagingTypes")
    public void testEditPackagingTypeFromExistingPackagingTypesToNoneInSubscriptionPlan(
        final PackagingType packagingType) {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, packagingType, true);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect packaging type", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(packagingType.getDisplayName()), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, false);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect packaging type", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(PelicanConstants.NONE), assertionErrorList);

        final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery =
            SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(planId).setOldName(null).setNewName(null)
                .setOldExternalKey(null).setNewExternalKey(null).setOldOfferingType(null).setNewOfferingType(null)
                .setOldStatus(null).setNewStatus(null).setOldCancellationPolicy(null).setNewCancellationPolicy(null)
                .setOldUsageType(null).setNewUsageType(null).setOldOfferingDetailId(null).setNewOfferingDetailId(null)
                .setOldProductLine(null).setNewProductLine(null).setOldSupportLevel(null).setNewSupportLevel(null)
                .setAction(Action.UPDATE).setFileName(null).setOldPackagingValue(packagingType)
                .setNewPackagingValue(null).setOldExpReminderEmailEnabled(PelicanConstants.TRUE)
                .setNewExpReminderEmailEnabled(PelicanConstants.FALSE).build();

        SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        // Verify Audit Log report results for subscription plan id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, planId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanInAuditLogReportDescription(
            descriptionPropertyValues, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, getEnvironmentVariables(), packagingType, null, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the user entered external key if external key is provided by the user
     * when editing a subscription plan in the admin tool
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testUserExternalKeyGeneratedInEditSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        final String newName = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanDetailPage = editSubscriptionPlanExternalKey(newName);
        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, newName, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test editing the subscription plan with a duplicate external key
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testDuplicateExternalKeyGeneratedInEditSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        final String newName = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        editSubscriptionPlanExternalKey(newName);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editExternalKey(newName);
        editSubscriptionPlanPage.clickOnSave(false);
        final String errorMessage = editSubscriptionPlanPage.getPlanExternalKeyErrorMessage();
        AssertCollector.assertThat("Incorrect error message displayed for duplicate external key", errorMessage,
            equalTo(PelicanErrorConstants.DUPLICATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the audit log report for edit subscription plan.
     *
     */
    @Test
    public void testAuditLogReportDescriptionInEditSubscriptionPlan() {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();
        // Query Audit Log Report for each subscription plan1
        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, null,
            planId, adminToolUserId, Action.UPDATE.toString(), null, assertionErrorList);
        final List<String> descriptionList = auditLogReportResultPage.getValuesFromDescriptionColumn();
        AssertCollector.assertThat("Incorrect description for feature update entry", descriptionList.get(0),
            equalTo(PelicanConstants.DESCRIPTION_CHANGES), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case to check the possibility of editing the entitlements on the subscription plan in new status
     *
     */
    @Test
    public void testEditEntitlmentsOnPlanInNewStatus() {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);
        addSubscriptionPlanPage.clickOnSave(false);
        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();
        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        final Item item2 = featureApiUtils.addFeature(null, null, null);
        final String itemId2 = item2.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId2, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            null, 0);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(subscriptionPlanId, itemId2, getEnvironmentVariables());

        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Entitlement is not editable in new status",
            subscriptionPlanDetailPage.getEntitlementDetails(newEntitlementId2, 2),
            equalTo(item2.getName() + " (" + item2.getId() + ")"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case to check the possibility of editing the entitlements on the subscription plan in active
     * status
     *
     */
    @Test
    public void testEditEntitlmentsOnPlanInActiveStatus() {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();
        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        AssertCollector.assertThat("Incorrect feature name value in the plan",
            editSubscriptionPlanPage.getEntitlementDetails(1, 2), equalTo(item.getName() + " (" + item.getId() + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect licensing model name value in the plan",
            editSubscriptionPlanPage.getEntitlementDetails(1, 5).split(" ")[0],
            equalTo(PelicanConstants.RETAIL_LICENSING_MODEL_NAME), assertionErrorList);

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();
        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the description in the download audit log report for edit subscription plan.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testAuditLogDownloadReportDescriptionInEditSubscriptionPlan() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME);
        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        auditLogReportPage.generateReport(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, planId,
            adminToolUserId, true);

        final String fileName =
            XlsUtils.getDirPath(getEnvironmentVariables()) + PelicanConstants.AUDIT_LOG_REPORT_DOWNLOADED_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final String description = fileData[1][PelicanConstants.DESCRIPTION_INDEX_IN_AUDIT_LOG_REPORT];
        AssertCollector.assertThat("Incorrect description for feature update entry", description,
            equalTo(PelicanConstants.DESCRIPTION_CHANGES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to save a Subscription Plan by adding offer with no price in edit flow
     */
    @Test
    public void testEditSubscriptionPlanAddOfferWithNoPrice() {

        boolean isPricePresent = true;
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        // Click on Save
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer
        editSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 1, false);

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        AssertCollector.assertThat("Failed to match Subscription Plan ID before and after Edit plan",
            subscriptionPlanId, equalTo(subscriptionPlanDetailPage.getId()), assertionErrorList);

        try {
            AssertCollector.assertThat("Assert failed for Offer in subscription plan", subscriptionOfferExternalKey,
                equalTo(subscriptionPlanDetailPage.getOfferExternalKey(1)), assertionErrorList);
            // this step should trigger exception which will be handled in catch block
            subscriptionPlanDetailPage.getPriceId();
            Assert.fail("Price ID is returned when not expected!!!");
        } catch (final NoSuchElementException nse) {
            isPricePresent = false;
        }
        AssertCollector.assertFalse("Excepted no Price, but price returned ", isPricePresent, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Edit a subscription plan with recurring offer for default name check
     *
     * @throws ParseException
     */
    @Test(dataProvider = "getSubscriptionPlan")
    public void testEditdSubscriptionPlanWithOfferForRecurringInOfferName(final UsageType usageType,
        final Boolean isUnlimitedCycle, final String cycleCount, final int billingFrequencyCount,
        final Boolean shouldDisplayRecurring) {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, usageType,
            "OfferingDetails1 (DC020500)", newProductLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer
        final String offerName = editSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE,
            isUnlimitedCycle, cycleCount, BillingFrequency.MONTH, billingFrequencyCount, true);

        if (shouldDisplayRecurring) {

            AssertCollector.assertThat("Recurring word is missing from Offer Name in add offer Pop up", offerName,
                containsString(PelicanConstants.RECURRING), assertionErrorList);

            AssertCollector.assertThat("Recurring word is missing from Offer Name in Edit Subscription plan page",
                editSubscriptionPlanPage.getOfferNameFromSubscriptionPlanPages(),
                containsString(PelicanConstants.RECURRING), assertionErrorList);
        } else {
            AssertCollector.assertThat("Recurring word is displayed in from Offer Name in add offer Pop up", offerName,
                not(PelicanConstants.RECURRING), assertionErrorList);

            AssertCollector.assertThat("Recurring word is displayed in from Offer Name in Edit Subscription plan page",
                editSubscriptionPlanPage.getOfferNameFromSubscriptionPlanPages(), not(PelicanConstants.RECURRING),
                assertionErrorList);
        }

        final String priceStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String priceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 180);

        // Add Subscription Price
        editSubscriptionPlanPage.addPricesInOffer(1, namerStoreName, nameOfNamerPriceList,
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, priceStartDate, priceEndDate);

        // Click on Save
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanDetailPage.getId();

        if (shouldDisplayRecurring) {

            AssertCollector.assertThat("Recurring word is missing from Offer Name in Subscription plan details page",
                subscriptionPlanDetailPage.getOfferNameFromSubscriptionPlanPages(),
                containsString(PelicanConstants.RECURRING), assertionErrorList);

            AssertCollector.assertThat("Recurring word is missing from Offer Name in Subscription plan details page",
                subscriptionPlanDetailPage.getOfferNameFromSubscriptionPlanPages(),
                containsString(PelicanConstants.RECURRING), assertionErrorList);
            final SubscriptionOffersReportPage subscriptionOffersReportPage =
                adminToolPage.getPage(SubscriptionOffersReportPage.class);
            final SubscriptionOffersReportResultPage subscriptionOffersReportResultPage =
                subscriptionOffersReportPage.getReportWithSelectedFilters(newProductLineNameAndExternalKey, false, true,
                    false, true, false, false, false, false, 0, 0, true, true, PelicanConstants.VIEW);
            AssertCollector.assertThat("Incorrect records are returned for subscriptionOffer",
                subscriptionOffersReportResultPage.getReportData().size(), equalTo(1), assertionErrorList);
            AssertCollector.assertThat("Wrong OfferName is displayed in Offer report",
                subscriptionOffersReportResultPage.getValuesFromOfferNameColumn().get(0), equalTo(offerName),
                assertionErrorList);

        } else {
            AssertCollector.assertThat("Recurring word is displayed in Offer Name in Subscription plan details page",
                subscriptionPlanDetailPage.getOfferNameFromSubscriptionPlanDetailsPage(),
                not(PelicanConstants.RECURRING), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the common method for assertions of the required fields on the subscription plan page
     *
     * @param usageTypeErrorMessage - Error message displayed on the usage type field
     * @param productLineErrorMessage - Error message displayed on the product line field
     */
    private void commonAssertionsForErrorMessages(final String usageTypeErrorMessage,
        final String productLineErrorMessage) {
        AssertCollector.assertThat("Incorrect error message for required usage type field", usageTypeErrorMessage,
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for required product line field", productLineErrorMessage,
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method stores the fields in a subscription plan into a hashmap
     *
     * @return HashMap<String,String>
     */
    private HashMap<String, String> getFieldsFromSubscriptionPlan(
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage) {

        final HashMap<String, String> planFieldsMap = new HashMap<>();
        planFieldsMap.put("Id", subscriptionPlanDetailPage.getId());
        planFieldsMap.put("Name", subscriptionPlanDetailPage.getName());
        planFieldsMap.put("ExternalKey", subscriptionPlanDetailPage.getExternalKey());
        planFieldsMap.put("OfferingType", subscriptionPlanDetailPage.getOfferingType());
        planFieldsMap.put("Status", subscriptionPlanDetailPage.getStatus());
        planFieldsMap.put("CancellationPolicy", subscriptionPlanDetailPage.getCancellationPolicy());
        planFieldsMap.put("UsageType", subscriptionPlanDetailPage.getUsageType());
        planFieldsMap.put("ProductLine", subscriptionPlanDetailPage.getProductLine());
        planFieldsMap.put("OfferingDetail", subscriptionPlanDetailPage.getOfferingDetail());
        planFieldsMap.put("SupportLevel", subscriptionPlanDetailPage.getSupportLevel());

        return planFieldsMap;

    }

    /**
     * This is a method which will assert the fields on the subscription plan page
     */
    private void commonAssertionsForSubscriptionPlanFields(final String name, final String externalKey,
        final OfferingType offeringType, final Status status, final CancellationPolicy cancellationPolicy,
        final UsageType usageType, final String offeringDetail, final String productLine,
        final SupportLevel supportLevel, final HashMap<String, String> planFieldsMap) {

        AssertCollector.assertThat("Incorrect id in the subscription plan", planFieldsMap.get("Id"), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect name of the subscription plan", planFieldsMap.get("Name"), equalTo(name),
            assertionErrorList);
        if (externalKey == null) {
            AssertCollector.assertThat("Incorrect external key prefix for the subscription plan",
                planFieldsMap.get("ExternalKey").split("-")[0], equalTo("SO"), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key length for the subscription plan",
                planFieldsMap.get("ExternalKey").split("-")[1].length(), equalTo(12), assertionErrorList);

        } else {
            AssertCollector.assertThat("Incorrect external key of the subscription plan",
                planFieldsMap.get("ExternalKey"), equalTo(externalKey), assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect offering type of the subscription plan",
            planFieldsMap.get("OfferingType"), equalTo(offeringType.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the subscription plan", planFieldsMap.get("Status"),
            equalTo(status.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect cancellation policy of the subscription plan",
            planFieldsMap.get("CancellationPolicy"), equalTo(cancellationPolicy.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type of the subscription plan", planFieldsMap.get("UsageType"),
            equalTo(usageType.toString()), assertionErrorList);
        if (offeringDetail == null) {
            AssertCollector.assertThat("Incorrect offering detail of the subscription plan",
                planFieldsMap.get("OfferingDetail"), equalTo("-"), assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect offering detail of the subscription plan",
                planFieldsMap.get("OfferingDetail"), equalTo(offeringDetail), assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect product line of the subscription plan",
            planFieldsMap.get("ProductLine").split(" ")[0], equalTo(productLine.split(" ")[0]), assertionErrorList);
        AssertCollector.assertThat("Incorrect support level of the subscription plan",
            planFieldsMap.get("SupportLevel"), equalTo(supportLevel.getDisplayName()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify the remove Feature option for Offering Manager user and verify if Non OM user can remove feature
     *
     * @param user
     * @param isOMUser
     */
    @Test(dataProvider = "getUsers")
    public void testRemoveFeaturePermissionForUser(final String user, final Boolean isOMUser) {

        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), user);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        final UserUtils userUtils = new UserUtils();
        final List<String> requiredRoleList = new RolesHelper(getEnvironmentVariables()).getAllRolesList();

        if (isOMUser) {
            userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage,
                getEnvironmentVariables());
        } else {
            requiredRoleList.remove(Role.OFFERING_MANAGER.getValue());
            userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage,
                getEnvironmentVariables());
        }

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferings1.getOfferings().get(0).getId());
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editStatus(Status.ACTIVE);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final String entitlementId1 = DbUtils.getEntitlementIdFromItemId(bicOfferings1.getOfferings().get(0).getId(),
            featureId1, getEnvironmentVariables());

        final String entitlementId2 = DbUtils.getEntitlementIdFromItemId(bicOfferings1.getOfferings().get(0).getId(),
            featureId2, getEnvironmentVariables());

        final String entitlementId3 = DbUtils.getEntitlementIdFromItemId(bicOfferings1.getOfferings().get(0).getId(),
            featureId3, getEnvironmentVariables());

        if (isOMUser) {
            AssertCollector.assertTrue("Offering Manager could not delete the feature",
                editSubscriptionPlanPage.removeFeature(entitlementId1, true), assertionErrorList);
            AssertCollector.assertTrue("Offering Manager could not delete the feature",
                editSubscriptionPlanPage.removeFeature(entitlementId2, true), assertionErrorList);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

            final String entitlementId4 =
                DbUtils.getEntitlementId(bicOfferings1.getOfferings().get(0).getId(), getEnvironmentVariables());

            AssertCollector.assertThat("Feature Name is not found",
                subscriptionPlanDetailPage.getEntitlementDetails(String.valueOf(entitlementId4), 3),
                equalTo(featureExtKey3), assertionErrorList);

            final AuditLogReportResultPage auditLogReportResultPage = auditLogReportPage.generateReport(null, null,
                PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, bicOfferings1.getOfferings().get(0).getId(), null, false);

            final Boolean foundAuditEntryForItem1 =
                auditLogReportResultPage.validateFeatureRemoveFromAuditLog(auditLogReportResultPage,
                    bicOfferings1.getOfferings().get(0).getName() + " (" + bicOfferings1.getOfferings().get(0).getId()
                        + ")",
                    featureName1 + " (" + entitlementId1 + ")", Action.DELETE.getDisplayName(), assertionErrorList);

            final Boolean foundAuditEntryForItem2 =
                auditLogReportResultPage.validateFeatureRemoveFromAuditLog(auditLogReportResultPage,
                    bicOfferings1.getOfferings().get(0).getName() + " (" + bicOfferings1.getOfferings().get(0).getId()
                        + ")",
                    featureName2 + " (" + entitlementId2 + ")", Action.DELETE.getDisplayName(), assertionErrorList);

            AssertCollector.assertTrue("Delete Audit entry for feature " + featureName1 + " is missing",
                foundAuditEntryForItem1, assertionErrorList);
            AssertCollector.assertTrue("Delete Audit entry for feature " + featureName2 + " is missing",
                foundAuditEntryForItem2, assertionErrorList);

        } else {
            AssertCollector.assertFalse("Non Offering manager would delete the feature",
                editSubscriptionPlanPage.removeFeature(entitlementId3, true), assertionErrorList);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

            AssertCollector.assertThat("Feature Name is not found",
                subscriptionPlanDetailPage.getEntitlementDetails(entitlementId1, 3),
                isOneOf(featureExtKey1, featureExtKey2, featureExtKey3), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test editing the assignable and new date fields of a subscription plan
     *
     */
    @Test
    public void testAssignableAndDateFieldsInEditSubscriptionPlanWhenFeatureFlagTrue() {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final Applications applications = resource.application().getApplications();
        String appId = "";
        String featureTypeId = null;

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        final List<Map<String, String>> resultMapList =
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
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId);
        final String itemId = item.getId();

        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item1.getId();

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        // Add Feature Entitlement
        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        AssertCollector.assertTrue("Incorrect default value for the CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(0), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(true, eosDate, eolRenewalDate, eolImmediateDate, 0);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);
        AssertCollector.assertFalse("Incorrect default value for the non CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(1), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);

        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the assignable and date fields to a currency entitlement
     *
     */
    @Test
    public void testAssignableAndDateFieldsForCurrencyEntitlementInEditSubscriptionPlan() {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("100.00", PelicanConstants.CLOUD_CURRENCY_SELECT,
            0);
        AssertCollector.assertFalse("Incorrect field is displayed while adding currency entitlement",
            addSubscriptionPlanPage.isAssignablePresent(0), assertionErrorList);
        AssertCollector.assertFalse("Incorrect field is displayed while adding currency entitlement",
            addSubscriptionPlanPage.isEOSDateFieldPresent(0), assertionErrorList);
        AssertCollector.assertFalse("Incorrect field is displayed while adding currency entitlement",
            addSubscriptionPlanPage.isEOLRenewalDateFieldPresent(0), assertionErrorList);
        AssertCollector.assertFalse("Incorrect field is displayed while adding currency entitlement",
            addSubscriptionPlanPage.isEOLImmediateDateFieldPresent(0), assertionErrorList);

        addSubscriptionPlanPage.clickOnSave(false);

        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the assignable and date fields on edit subscription plan when the feature
     * flags are turned off
     *
     */
    @Test
    public void testAssignableAndDateFieldsInEditSubscriptionPlanWhenFeatureFlagFalse() {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, false);

        final Applications applications = resource.application().getApplications();
        String appId = "";
        String featureTypeId = null;

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        final List<Map<String, String>> resultMapList =
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
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId);
        final String itemId = item.getId();

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);

        AssertCollector.assertFalse("Incorrect field is displayed while adding feature entitlement",
            addSubscriptionPlanPage.isAssignablePresent(0), assertionErrorList);
        AssertCollector.assertFalse("Incorrect field is displayed while adding feature entitlement",
            addSubscriptionPlanPage.isEOSDateFieldPresent(0), assertionErrorList);
        AssertCollector.assertFalse("Incorrect field is displayed while adding feature entitlement",
            addSubscriptionPlanPage.isEOLRenewalDateFieldPresent(0), assertionErrorList);
        AssertCollector.assertFalse("Incorrect field is displayed while adding feature entitlement",
            addSubscriptionPlanPage.isEOLImmediateDateFieldPresent(0), assertionErrorList);

        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);

        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case where large number of features are added through item look up
     */
    @Test
    public void testFeatureAdditionThroughLookup() {

        final Applications applications = resource.application().getApplications();
        String appId = "";

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());

        final Item item = featureApiUtils.addFeature(null, null, null);
        final Item item1 = featureApiUtils.addFeature(null, null, null);

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        addSubscriptionPlanPage.getResultsOnFeatureSearch(0, item.getName());
        addSubscriptionPlanPage.getResultsOnFeatureSearch(1, item1.getName());
        addSubscriptionPlanPage.getResultsOnFeatureSearch(2, item.getName());
        addSubscriptionPlanPage.getResultsOnFeatureSearch(3, item1.getName());

        addSubscriptionPlanPage.clickOnSave(true);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        addSubscriptionPlanPage.getResultsOnFeatureSearch(4, item.getName());
        addSubscriptionPlanPage.getResultsOnFeatureSearch(5, item1.getName());
        addSubscriptionPlanPage.getResultsOnFeatureSearch(6, item.getName());
        addSubscriptionPlanPage.getResultsOnFeatureSearch(7, item1.getName());

        addSubscriptionPlanPage.clickOnSave(false);

        final String planId = subscriptionPlanDetailPage.getId();

        final List<String> entitlementIdsForItem1 =
            DbUtils.getAllEntitlementIdsForPlan(planId, item.getId(), getEnvironmentVariables());

        final List<String> entitlementIdsForItem2 =
            DbUtils.getAllEntitlementIdsForPlan(planId, item1.getId(), getEnvironmentVariables());

        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanDetailPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertTrue("Plan is saved without any entitlements", entitlementIdsForItem1 != null,
            assertionErrorList);
        AssertCollector.assertTrue("Plan is saved without any entitlements", entitlementIdsForItem2 != null,
            assertionErrorList);
        AssertCollector.assertTrue("Plan is not saved with all entilements correctly",
            entitlementIdsForItem1.size() == 4, assertionErrorList);
        AssertCollector.assertTrue("Plan is not saved with all entilements correctly",
            entitlementIdsForItem2.size() == 4, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * DataProvider to test OM User vs Non OM User scenarios
     */
    @SuppressWarnings("unused")
    @DataProvider(name = "getUsers")
    private Object[][] getUsers() {
        return new Object[][] { { PelicanConstants.NON_OFFERING_MANAGER_USER_EXTERNAL_KEY, false },
                { PelicanConstants.OFFERING_MANAGER_ONLY_USER, true } };
    }

    @DataProvider(name = "SubscriptionPlanStatuses")
    public Object[][] getSubscriptionPlanStatuses() {
        return new Object[][] { { Status.NEW }, { Status.ACTIVE } };
    }

    /**
     * Provider to return different PackagintTypes
     *
     * @return PackagingType
     */
    @DataProvider(name = "getPackagingTypes")
    public Object[][] getPackagingTypes() {
        return new Object[][] { { PackagingType.INDUSTRY_COLLECTION }, { PackagingType.VERTICAL_GROUPING } };
    }

    /**
     * This is a method to edit the subscription plan external key
     */
    private SubscriptionPlanDetailPage editSubscriptionPlanExternalKey(final String externalKey) {
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editExternalKey(externalKey);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        return subscriptionPlanDetailPage;
    }

    /**
     * Provider to return different Subscription plan for recurring key word tests
     *
     * @return PackagingType
     */
    @DataProvider(name = "getSubscriptionPlan")
    public Object[][] getSubscriptionPlan() {
        return new Object[][] { { UsageType.COM, true, null, 0, true }, { UsageType.COM, false, "1", 2, false },
                { UsageType.TRL, true, null, 0, false } };
    }
}
