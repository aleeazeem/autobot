package com.autodesk.bsm.pelican.ui.subscriptionplan;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.commonassertions.AssertionsForViewSubscriptionPlanPage;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAndBasicOfferingsAuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.coreproducts.AddCoreProductsPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class is to test Edit Subscription Plan Page.
 *
 * @author mandas
 */
public class EditSubscriptionPlanTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static String productLineNameAndExternalKey;
    private static AddSubscriptionPlanPage addSubscriptionPlanPage;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static String namerStoreName;
    private static String nameOfNamerPriceList;
    private static final String SUBSCRIPTION_OFFER_ORIGINAL_PRICE = "40";
    private static final String BILLING_PERIOD_ORIG_VALUE = "SubscriptionPeriod[count=1,type=MONTH]";
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private String newProductLineNameAndExternalKey;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private AddCoreProductsPage addCoreProductsPage;

    private String itemId = null;
    private String newPlanId = null, activePlanId = null;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        adminToolUserId = getEnvironmentVariables().getUserId();

        nameOfNamerPriceList = getStoreUs().getIncluded().getPriceLists().get(0).getName();
        namerStoreName = getStoreUs().getName();

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
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils.addProductLine(resource, productLine);
        newProductLineNameAndExternalKey =
            productLine.getData().getName() + " (" + productLine.getData().getName() + ")";

        addCoreProductsPage = adminToolPage.getPage(AddCoreProductsPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);

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
        final Item item1 = featureApiUtils.addFeature(null, null, featureTypeId);
        itemId = item1.getId();

        newPlanId = addSubscriptionPlan(Status.NEW);
        activePlanId = addSubscriptionPlan(Status.ACTIVE);
    }

    private String addSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        return subscriptionPlanDetailPage.getId();
    }

    /**
     * This is a test method is to verify the Edit Subscription Flow for, Adding two features to the offering which
     * should default the core product (if available) value that matches the feature's external key
     */
    @Test
    public void testEditSubscriptionPlanAddFeaturePopulateDefaultCoreProductWithLookup() throws ParseException {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey1 = RandomStringUtils.randomAlphanumeric(8);
        addCoreProductsPage.addCoreProduct(commonExtKey);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item1 = featureApiUtils.addFeature(commonExtKey, commonExtKey, null);
        final String itemId1 = item1.getId();

        final Item item2 = featureApiUtils.addFeature(commonExtKey1, commonExtKey1, null);
        final String itemId2 = item2.getId();

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        // Edit Subscription Plan page
        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        // Add 1st Feature Entitlement
        int entitlement = 0;
        editSubscriptionPlanPage.getResultsOnFeatureSearch(entitlement, commonExtKey);

        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item1.getName(),
            equalTo(editSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        final boolean isCoreProductSelected =
            editSubscriptionPlanPage.validateCoreProductSelection(entitlement, commonExtKey);

        AssertCollector.assertTrue("Core Product with same extkey as Feature is not selected by default from lookup",
            isCoreProductSelected, assertionErrorList);

        // Add 2nd Feature Entitlement
        entitlement = 1;
        editSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        editSubscriptionPlanPage.setFeatureAsInput(itemId2, entitlement);

        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item2.getName(),
            equalTo(editSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(true);

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        AssertCollector.assertThat("Incorrect Core Product displayed in Subscription details page",
            subscriptionPlanDetailPage.getOneTimeEntitlementCoreProductColumnValues().get(0),
            containsString(commonExtKey), assertionErrorList);

        final String newEntitlementId1 = DbUtils.getEntitlementIdFromItemId(planId, itemId1, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId2, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case that adds the feature and edits the dates on the feature and validate the dates on the
     * subscription plan
     *
     */
    @Test
    public void testEditActiveSubscriptionPlanAndEditDatesOnExistFeature() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey = RandomStringUtils.randomAlphanumeric(8);
        addCoreProductsPage.addCoreProduct(commonExtKey);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(commonExtKey, commonExtKey, null);
        final String itemId = item.getId();

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Add 1st Feature Entitlement
        int entitlement = 0;
        editSubscriptionPlanPage.getResultsOnFeatureSearch(entitlement, commonExtKey);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(editSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        // Add 2nd Feature Entitlement
        entitlement = 1;
        editSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        editSubscriptionPlanPage.setFeatureAsInput(itemId, entitlement);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(editSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        final String planId = subscriptionPlanDetailPage.getId();

        // Edit Subscription Plan page
        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final List<String> oneTimeEntitlementIds =
            editSubscriptionPlanPage.getOneTimeEntitlementIds(planId, itemId, getEnvironmentVariables());

        // Expand last entitlement row
        final String LastOneTimeEntitlementId = oneTimeEntitlementIds.get(0);
        editSubscriptionPlanPage.clickOnEntitlementExpandableRowToggle(LastOneTimeEntitlementId);

        editSubscriptionPlanPage.setEOSDate(oneTimeEntitlementIds.size() - 1, DateTimeUtils.getNowPlusDays(5));
        editSubscriptionPlanPage.setEOLImmediateDate(oneTimeEntitlementIds.size() - 1, DateTimeUtils.getNowPlusDays(7));
        editSubscriptionPlanPage.setEOLRenewDate(oneTimeEntitlementIds.size() - 1, DateTimeUtils.getNowPlusDays(6));

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(true);
        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(LastOneTimeEntitlementId);
        AssertCollector.assertFalse("Incorrect value of assignable for CPR",
            editSubscriptionPlanPage.getAssignable(oneTimeEntitlementIds.size() - 1), assertionErrorList);
        AssertCollector.assertThat("Incorrect value of eos date",
            editSubscriptionPlanPage.getEOSDate(oneTimeEntitlementIds.size() - 1),
            equalTo(DateTimeUtils.getNowPlusDays(5)), assertionErrorList);
        AssertCollector.assertThat("Incorrect value of eos renewal date",
            editSubscriptionPlanPage.getEOLRenewDate(oneTimeEntitlementIds.size() - 1),
            equalTo(DateTimeUtils.getNowPlusDays(6)), assertionErrorList);
        AssertCollector.assertThat("Incorrect value of eos immediate date",
            editSubscriptionPlanPage.getEOLImmediateDate(oneTimeEntitlementIds.size() - 1),
            equalTo(DateTimeUtils.getNowPlusDays(7)), assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(LastOneTimeEntitlementId);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the add feature functionality in new status
     *
     * @throws ParseException
     */
    @Test
    public void testAddFeatureToSubscriptionPlanInNewStatus() throws ParseException {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        Item item = featureApiUtils.addFeature(null, null, null);
        String itemId = item.getId();

        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        // Add Feature Entitlement
        final int entitlement = 0;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(itemId);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        String planId = subscriptionPlanDetailPage.getId();
        final String newEntitlementId1 = DbUtils.getEntitlementIdFromItemId(planId, itemId, getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        item = featureApiUtils.addFeature(null, null, null);
        itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 1);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        final String newEntitlementId2 = DbUtils.getEntitlementIdFromItemId(planId, itemId, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId2, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test Audit log for cloud services and remove features
     *
     * @throws ParseException
     */
    @Test
    public void testAuditLogAddFeatureToSubscriptionPlan() throws ParseException {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        // final Item item = featureApiUtils.addFeature(null, null, null);
        // final String itemId = item.getId();

        // Add Feature Entitlement
        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        // Add Feature Entitlement
        final int entitlement = 0;
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(
                ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)),
            entitlement);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);

        final String newEntitlementId1 = DbUtils.getEntitlementIdFromItemId(planId, itemId, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        // Query Audit Log Report for subscription plan
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, planId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        HashMap<String, List<String>> subscriptionEntitlementDescriptionValues =
            auditLogReportHelper.getAuditDescription(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT, planId, newEntitlementId1, Action.CREATE.toString());

        // descriptionPropertyValues.putAll(subscriptionEntitlementDescriptionValues);
        subscriptionEntitlementDescriptionValues.putAll(descriptionPropertyValues);

        // Get Licensing Model Id
        final String licensingModelId =
            DbUtils
                .selectQuery(PelicanDbConstants.SQL_QUERY_ID_FROM_LICENSING_MODEL
                    + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + "'", "ID", getEnvironmentVariables())
                .get(0);

        // Get Core Product Id1
        final String coreProductId1 = DbUtils.selectQuery(
            "select ID from CORE_PRODUCT where EXTERNAL_KEY='" + PelicanConstants.CORE_PRODUCT_AUTO_1 + "'", "ID",
            getEnvironmentVariables()).get(0);
        // Get Core Product Id2
        final String coreProductId2 = DbUtils.selectQuery(
            "select ID from CORE_PRODUCT where EXTERNAL_KEY='" + PelicanConstants.CORE_PRODUCT_AUTO_2 + "'", "ID",
            getEnvironmentVariables()).get(0);

        final String coreProducts = "[" + PelicanConstants.CORE_PRODUCT_AUTO_2 + " (" + coreProductId2 + "), "
            + PelicanConstants.CORE_PRODUCT_AUTO_1 + " (" + coreProductId1 + ")]";

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForFeatureEntitlementInAuditLogReportDescription(
            subscriptionEntitlementDescriptionValues, null, itemId, null,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + " (" + licensingModelId + ")", null, coreProducts,
            null, "false", null, eosDate, null, eolImmediateDate, null, eolRenewalDate, getEnvironmentVariables(),
            assertionErrorList);

        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(planId);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        editSubscriptionPlanPage.setAssignable(0, true);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        subscriptionEntitlementDescriptionValues.clear();
        subscriptionEntitlementDescriptionValues =
            auditLogReportHelper.getAuditDescription(PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                PelicanConstants.ENTITY_SUBSCRIPTION_ENTITLEMENT, planId, newEntitlementId1, Action.UPDATE.toString());

        // Assignable
        final List<String> assginableValues =
            subscriptionEntitlementDescriptionValues.get(PelicanConstants.AUDIT_ASSIGNABLE_COLUMN_NAME);
        AssertCollector.assertThat("Invalid old Assignable value in audit log report", assginableValues.get(0),
            equalTo("false"), assertionErrorList);
        AssertCollector.assertThat("Invalid new Assignable value in audit log report", assginableValues.get(1),
            equalTo("true"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test ability to add subscription plan without price in offer
     *
     */
    @Test
    public void testActivateSubscriptionPlanNoPrice() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(
                ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)),
            0);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(addSubscriptionPlanPage.getFeatureName(0)), assertionErrorList);

        final String currencyAmount = "100";
        // Add Currency Amount Entitlement
        addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement(currencyAmount,
            PelicanConstants.CLOUD_CURRENCY_SELECT, 1);

        final String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer. Note: No Price is added to this offer
        addSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 1, false);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();

        // Get Offer Id
        final String offerId =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_SUBSCRIPTION_OFFER_ID, subscriptionPlanId),
                "ID", getEnvironmentVariables()).get(0);

        final String subscriptionOfferName =
            getProductLineExternalKeyMaya() + " " + SupportLevel.BASIC.getDisplayName() + " 1 month - Recurring";
        // Query to Dynamo DB for Subscription Offer
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(offerId, null, subscriptionPlanId, null,
            subscriptionOfferName, null, subscriptionOfferExternalKey, null, BILLING_PERIOD_ORIG_VALUE, null,
            Status.ACTIVE.toString(), Action.CREATE, assertionErrorList);

        // Query Audit Log Report for subscription plan
        HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // Query Audit Log Report for subscription offer
        descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                subscriptionPlanId, offerId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // Subscription Offer description assertion
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionOfferInAuditLogReportDescription(
            descriptionPropertyValues, null, subscriptionOfferName, null, subscriptionOfferExternalKey, null,
            Status.ACTIVE, null, BILLING_PERIOD_ORIG_VALUE, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test ability to add subscription plan without price in offer with feature flag false
     *
     */
    @Test
    public void testActivateSubscriptionPlanWithOutPrice() {

        try {

            final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
            // Add subscription Plan Info
            addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
                OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
                UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
                true);

            final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
            final Item item = featureApiUtils.addFeature(null, null, null);
            final String itemId = item.getId();
            // Add Feature Entitlement
            addSubscriptionPlanPage
                .addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
                    new ArrayList<>(
                        ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)),
                    0);

            AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
                equalTo(addSubscriptionPlanPage.getFeatureName(0)), assertionErrorList);

            final String currencyAmount = "100";
            // Add Currency Amount Entitlement
            addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement(currencyAmount,
                PelicanConstants.CLOUD_CURRENCY_SELECT, 1);

            final String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

            // Add Subscription Offer. Note: No Price is added to this offer
            addSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
                BillingFrequency.MONTH, 1, false);

            // Click on Save without adding price to offer
            addSubscriptionPlanPage.clickOnSave(true);

            AssertCollector.assertThat("Subscription Plan without price to offer is saved !",
                addSubscriptionPlanPage.getError(),
                equalTo(PelicanConstants.SUBSCRIPTION_PLAN_WITH_NO_PRICE_ERROR + subscriptionOfferExternalKey),
                assertionErrorList);

        } catch (final Exception e) {
            Assert
                .fail("Something went wrong in testEditSubscriptionPlanAddOfferWithNoPriceWithFeatureFlagFalse method: "
                    + e.getMessage());
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests adding a subscription plan with recurring offer for default name check
     */
    @Test(dataProvider = "getSubscriptionPlan")
    public void testAddSubscriptionPlanWithOfferForRecurringInOfferName(final UsageType usageType,
        final Boolean isUnlimitedCycle, final String cycleCount, final int billingFrequencyCount,
        final Boolean shouldDisplayRecurring) {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, usageType,
            "OfferingDetails1 (DC020500)", newProductLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer
        final String offerName = addSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE,
            isUnlimitedCycle, cycleCount, BillingFrequency.MONTH, billingFrequencyCount, true);

        if (shouldDisplayRecurring) {
            AssertCollector.assertThat("Recurring word is missing from Offer Name in add offer Pop up", offerName,
                containsString(PelicanConstants.RECURRING), assertionErrorList);

            AssertCollector.assertThat("Recurring word is missing from Offer Name in Add Subscription page",
                addSubscriptionPlanPage.getOfferNameFromSubscriptionPlanPages(),
                containsString(PelicanConstants.RECURRING), assertionErrorList);
        } else {
            AssertCollector.assertThat("Recurring word is displayed in Offer Name in add offer Pop up", offerName,
                not(PelicanConstants.RECURRING), assertionErrorList);

            AssertCollector.assertThat("Recurring word is displayed in Offer Name in Add Subscription plan page",
                addSubscriptionPlanPage.getOfferNameFromSubscriptionPlanPages(), not(PelicanConstants.RECURRING),
                assertionErrorList);
        }
        final String priceStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String priceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 180);

        // Add Subscription Price
        addSubscriptionPlanPage.addPricesInOffer(1, namerStoreName, nameOfNamerPriceList,
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, priceStartDate, priceEndDate);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanDetailPage.getId();

        if (shouldDisplayRecurring) {
            AssertCollector.assertThat("Recurring word is missing from Offer Name in Subscription plan details page",
                subscriptionPlanDetailPage.getOfferNameFromSubscriptionPlanPages(),
                containsString(PelicanConstants.RECURRING), assertionErrorList);
            SubscriptionOffersReportResultPage subscriptionOffersReportResultPage =
                adminToolPage.getPage(SubscriptionOffersReportResultPage.class);
            final SubscriptionOffersReportPage subscriptionOffersReportPage =
                adminToolPage.getPage(SubscriptionOffersReportPage.class);
            subscriptionOffersReportResultPage =
                subscriptionOffersReportPage.getReportWithSelectedFilters(newProductLineNameAndExternalKey, false, true,
                    false, true, false, false, false, false, 0, 0, true, true, PelicanConstants.VIEW);
            AssertCollector.assertThat("Incorrect records are returned for subscriptionOffer",
                subscriptionOffersReportResultPage.getReportData().size(), equalTo(1), assertionErrorList);
            AssertCollector.assertThat("Wrong OfferName is displayed in Offer report",
                subscriptionOffersReportResultPage.getValuesFromOfferNameColumn().get(0), equalTo(offerName),
                assertionErrorList);

        } else {
            AssertCollector.assertThat("Recurring word is displayed in Offer Name in Subscription details page",
                addSubscriptionPlanPage.getOfferNameFromSubscriptionPlanDetailsPage(), not(PelicanConstants.RECURRING),
                assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test adding dates with past value
     *
     */

    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testEntitlementtDatesInSubscriptionPlanForPastDate(final Status status) {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = null;

        if (status == Status.NEW) {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(newPlanId);
        }
        if (status == Status.ACTIVE) {
            final String activePlanId = addSubscriptionPlan(Status.ACTIVE);
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(activePlanId);
        }
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        final String eosDate =
            new DateTime().minusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().minusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().minusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        editSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        editSubscriptionPlanPage.addRemoveFeatureDates(eosDate, eolRenewalDate, eolImmediateDate, 0);

        if (status == Status.NEW) {
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
            AssertCollector.assertThat(
                "SubscriptionPlan page Edit fail when adding null dates for a one-time entitlement",
                subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);
        } else if (status == Status.ACTIVE) {
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave();
            AssertCollector.assertThat("eosDate must be in the future", subscriptionPlanDetailPage.getError(),
                equalTo(PelicanErrorConstants.SUB_PLAN_UPLOAD_PAST_EOS_DATE_ERROR), assertionErrorList);
        }

    }

    /**
     * This is a test case which will test adding dates with wrong relationship in active state
     *
     */
    @Test
    public void testEntitlementtDatesInSubscriptionPlanForWrongRelationInActive() {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = null;

        final String activePlanId = addSubscriptionPlan(Status.ACTIVE);

        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(activePlanId);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        final String eosDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        editSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        editSubscriptionPlanPage.addRemoveFeatureDates(eosDate, eolRenewalDate, eolImmediateDate, 0);

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(true);
        AssertCollector.assertThat(
            "When setting multiple EOS and EOL dates, follow this logic: EOS Date < EOL Renewal Date < EOL Immediate Date",
            subscriptionPlanDetailPage.getError(), equalTo(PelicanErrorConstants.SUB_PLAN_UPLOAD_DATES_OUT_OF_ORDER),
            assertionErrorList);

    }

    /**
     * This is a test case which will test adding dates with wrong relationship in new state
     *
     */
    @Test
    public void testEntitlementtDatesInSubscriptionPlanForWrongRelationInNew() {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = null;

        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(newPlanId);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        final String eosDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        editSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        editSubscriptionPlanPage.addRemoveFeatureDates(eosDate, eolRenewalDate, eolImmediateDate, 0);

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        AssertCollector.assertThat(
            "SubscriptionPlan page Edit fail when adding non-null dates for a one-time entitlement",
            subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);

    }

    /**
     * This is a test case which will test adding null dates new and active state
     *
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testEntitlementDatesInSubscriptionPlanForNull(final Status status) {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = null;

        if (status == Status.NEW) {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(newPlanId);
        }
        if (status == Status.ACTIVE) {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(activePlanId);
        }
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        final String eosDate = null;
        final String eolRenewalDate = null;
        final String eolImmediateDate = null;

        editSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        editSubscriptionPlanPage.addRemoveFeatureDates(eosDate, eolRenewalDate, eolImmediateDate, 0);

        if (status == Status.NEW) {
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
        } else if (status == Status.ACTIVE) {
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(true);
        }
        AssertCollector.assertThat("SubscriptionPlan page Add fail when adding null dates for a one-time entitlement",
            subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);

    }

    /**
     * This is a test case which will test adding dates with past value and then modifying them to be in the future for
     * a plan in active state
     *
     */

    @Test
    public void testEntitlementtDatesInSubscriptionPlanForPastDateChangedToFuture() {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);

        String eosDate = new DateTime().minusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT);
        final String planId = subscriptionPlanDetailPage.getId();
        final String entitlementId = DbUtils.getSubscriptionEntitlementId(planId, getEnvironmentVariables());
        DbUtils.updateRemoveFeatureDates(eosDate, null, null, entitlementId, getEnvironmentVariables());

        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        subscriptionPlanDetailPage =
            findSubscriptionPlanPage.findSubscriptionPlanById(subscriptionPlanDetailPage.getId());
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        eosDate = new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(entitlementId);

        editSubscriptionPlanPage.addRemoveFeatureDates(eosDate, null, null, 0);

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(true);
        AssertCollector.assertThat("EOS Date in the past is not editable", subscriptionPlanDetailPage.getError(),
            equalTo(PelicanErrorConstants.PAST_DATE_CHANGED_TO_FUTURE_ERROR), assertionErrorList);

    }

    /**
     * Provider for the subcriptionplan statuses
     *
     * @return
     */
    @DataProvider(name = "SubscriptionPlanStatuses")
    public Object[][] getSubscriptionPlanStatuses() {
        return new Object[][] { { Status.NEW }, { Status.ACTIVE } };
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
