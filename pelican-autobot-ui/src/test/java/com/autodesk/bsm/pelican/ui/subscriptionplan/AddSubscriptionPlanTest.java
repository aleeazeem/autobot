package com.autodesk.bsm.pelican.ui.subscriptionplan;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
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
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAndBasicOfferingsAuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanDynamoQuery;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.coreproducts.AddCoreProductsPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.FindDescriptorDefinitionsPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.FindProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanAndOfferDescriptorsPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionOfferDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.ui.productline.HelperForProductLine;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.DescriptorUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class is created to test Subscription Plan Page. As of now, tests related to add dates in offer prices in
 * Past Dates are added.
 *
 * @author Vineel Yerragudi and Muhammad Azeem
 */
public class AddSubscriptionPlanTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private static String subscriptionPlanId;
    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private static String productLineNameAndExternalKey;
    private static FindSubscriptionPlanPage findSubscriptionPlanPage;
    private static AddSubscriptionPlanPage addSubscriptionPlanPage;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static AddDescriptorPage addDescriptorPage;
    private static FindDescriptorDefinitionsPage findDescriptorDefinitionsPage;
    private static DescriptorDefinitionDetailPage descriptorDetailPage;
    private static EditSubscriptionPlanAndOfferDescriptorsPage editSubscriptionPlanAndOfferDescriptorsPage;
    private static SubscriptionOfferDetailPage subscriptionOfferDetailPage;
    private static AddProductLinePage addProductLinePage;
    private ProductLineDetailsPage productLineDetailsPage;
    private FindProductLinePage findProductLinePage;
    private FeatureApiUtils featureApiUtils;
    private static String namerStoreName;
    private static String nameOfNamerPriceList;
    private static String namerPriceListId;
    private static final Date DATE_TODAY =
        DateTimeUtils.convertStringToDate(DateTimeUtils.getCurrentDate(), PelicanConstants.DATE_FORMAT_WITH_SLASH);
    private static final String effectiveStartDate =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 30);
    private static final String effectiveEndDate =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 15);
    private static final String priceStartDateInAuditLog =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_START_DATE, 30);
    private static final String priceEndDateInAuditLog =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_END_DATE, 15);
    private static final String SUBSCRIPTION_OFFER_ORIGINAL_PRICE = "40";
    private static final String SUBSCRIPTION_OFFER_NEW_PRICE = "80";
    private static final String MAX_DESCRIPTOR_LENGTH = "40";
    private static final String BILLING_PERIOD_ORIG_VALUE = "SubscriptionPeriod[count=1,type=MONTH]";
    private static final String BILLING_PERIOD_NEW_VALUE = "SubscriptionPeriod[count=1,type=WEEK]";
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private String newProductLineNameAndExternalKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddSubscriptionPlanTest.class.getSimpleName());
    private AddCoreProductsPage addCoreProductsPage;
    private String cprItemId1;

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
        namerPriceListId = getStoreUs().getIncluded().getPriceLists().get(0).getId();
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
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils.addProductLine(resource, productLine);
        newProductLineNameAndExternalKey =
            productLine.getData().getName() + " (" + productLine.getData().getName() + ")";

        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        addDescriptorPage = adminToolPage.getPage(AddDescriptorPage.class);
        findDescriptorDefinitionsPage = adminToolPage.getPage(FindDescriptorDefinitionsPage.class);
        editSubscriptionPlanAndOfferDescriptorsPage =
            adminToolPage.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
        subscriptionOfferDetailPage = adminToolPage.getPage(SubscriptionOfferDetailPage.class);
        addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);
        productLineDetailsPage = adminToolPage.getPage(ProductLineDetailsPage.class);
        findProductLinePage = adminToolPage.getPage(FindProductLinePage.class);
        addCoreProductsPage = adminToolPage.getPage(AddCoreProductsPage.class);
        BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            adminToolPage.getPage(BankingConfigurationPropertiesPage.class);

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

        featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId);
        cprItemId1 = item.getId();

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);
    }

    /**
     * This method tests adding a subscription plan with offer, price and entitlement Test also verifies Dynamo DB log
     * and audit log report
     *
     */
    @Test
    public void testAddSubscriptionPlanWithOfferPriceEntitlement() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

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

        // Add Subscription Offer
        addSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 1, false);

        final String priceStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String priceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 180);

        // Add Subscription Price
        addSubscriptionPlanPage.addPricesInOffer(1, namerStoreName, nameOfNamerPriceList,
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, priceStartDate, priceEndDate);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();
        final String priceId = subscriptionPlanDetailPage.getPriceId();

        final String productLineId = DbUtils.selectQuery("select ID from PRODUCT_LINE where APP_FAMILY_ID = 2001 "
            + "and EXTERNAL_KEY = '" + getProductLineExternalKeyMaya() + "'", "ID", getEnvironmentVariables()).get(0);

        final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery = SubscriptionPlanDynamoQuery.builder()
            .setSubscriptionPlanId(subscriptionPlanId).setOldName(null).setNewName(subscriptionPlanExtKey + "Name")
            .setOldExternalKey(null).setNewExternalKey(subscriptionPlanExtKey).setOldOfferingType(null)
            .setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null).setNewStatus(Status.ACTIVE)
            .setOldCancellationPolicy(null).setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD)
            .setOldUsageType(null).setNewUsageType(UsageType.COM).setOldOfferingDetailId(null)
            .setNewOfferingDetailId(getEnvironmentVariables().getOfferingDetailId()).setOldProductLine(null)
            .setNewProductLine(productLineId).setOldSupportLevel(null).setNewSupportLevel(SupportLevel.BASIC)
            .setAction(Action.CREATE).setFileName(null).setOldPackagingValue(null).setNewPackagingValue(null)
            .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        // Get Feature Entitlement Id
        final String featureEntitlementId =
            DbUtils.selectQuery("select ID from SUBSCRIPTION_ENTITLEMENT where ITEM_ID = " + itemId, "ID",
                getEnvironmentVariables()).get(0);
        // Get Core Product Id1
        final String coreProductId1 = DbUtils.selectQuery(
            "select ID from CORE_PRODUCT where EXTERNAL_KEY='" + PelicanConstants.CORE_PRODUCT_AUTO_1 + "'", "ID",
            getEnvironmentVariables()).get(0);
        // Get Core Product Id2
        final String coreProductId2 = DbUtils.selectQuery(
            "select ID from CORE_PRODUCT where EXTERNAL_KEY='" + PelicanConstants.CORE_PRODUCT_AUTO_2 + "'", "ID",
            getEnvironmentVariables()).get(0);
        // Get Licensing Model Id
        final String licensingModelId =
            DbUtils
                .selectQuery(PelicanDbConstants.SQL_QUERY_ID_FROM_LICENSING_MODEL
                    + PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + "'", "ID", getEnvironmentVariables())
                .get(0);

        // Query Dynamo DB for Feature Entitlement
        SubscriptionPlanAuditLogHelper.validateFeatureEntitlementInDynamoDB(null, subscriptionPlanId, null, itemId,
            null, featureEntitlementId, null, licensingModelId, null,
            "[" + coreProductId2 + ", " + coreProductId1 + "]", Action.CREATE, assertionErrorList);

        // Get Currency Amount Entitlement Id
        final String currencyAmountEntitlementId = DbUtils.selectQuery(
            "select ID from SUBSCRIPTION_ENTITLEMENT where RELATED_ID = " + subscriptionPlanId + " and GRANT_TYPE = 0",
            "ID", getEnvironmentVariables()).get(0);

        // Query Dynamo DB for Currency Amount Entitlement
        SubscriptionPlanAuditLogHelper.assertionsOnCurrencyAmountEntitlement(subscriptionPlanId, null,
            currencyAmountEntitlementId, null, getEnvironmentVariables().getCloudCurrencyId(), null, currencyAmount,
            Action.CREATE, assertionErrorList);

        // Get Offer Id
        final String offerId =
            DbUtils.selectQuery("select ID from SUBSCRIPTION_OFFER where PLAN_ID = " + subscriptionPlanId, "ID",
                getEnvironmentVariables()).get(0);

        final String subscriptionOfferName =
            getProductLineExternalKeyMaya() + " " + SupportLevel.BASIC.getDisplayName() + " 1 month - Recurring";
        // Query to Dynamo DB for Subscription Offer
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(offerId, null, subscriptionPlanId, null,
            subscriptionOfferName, null, subscriptionOfferExternalKey, null, BILLING_PERIOD_ORIG_VALUE, null,
            Status.ACTIVE.toString(), Action.CREATE, assertionErrorList);

        final String priceStartDateInAuditLog =
            DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_START_DATE);
        final String priceEndDateInAuditLog =
            DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_END_DATE, 180);

        // Query Dynamo DB for Subscription Price
        SubscriptionPlanAuditLogHelper.validateSubscriptionPrice(subscriptionPlanId, offerId, priceId, null,
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, null, Currency.USD, null, priceStartDateInAuditLog, null,
            priceEndDateInAuditLog, Action.CREATE, assertionErrorList);

        // Query Audit Log Report for subscription plan
        HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, subscriptionPlanId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // Subscription Plan description assertion
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanInAuditLogReportDescription(
            descriptionPropertyValues, null, subscriptionPlanExtKey + "Name", null, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, null, Status.ACTIVE, null,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null, UsageType.COM, null,
            getEnvironmentVariables().getOfferingDetailId(), null, productLineId, null, SupportLevel.BASIC,
            getEnvironmentVariables(), null, null, assertionErrorList);

        // Query Audit Log Report for subscription feature entitlement
        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, featureEntitlementId, adminToolUserId,
            Action.CREATE.toString(), null, assertionErrorList);

        final String coreProducts = "[" + PelicanConstants.CORE_PRODUCT_AUTO_2 + " (" + coreProductId2 + "), "
            + PelicanConstants.CORE_PRODUCT_AUTO_1 + " (" + coreProductId1 + ")]";
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForFeatureEntitlementInAuditLogReportDescription(
            descriptionPropertyValues, null, itemId, null,
            PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY + " (" + licensingModelId + ")", null, coreProducts,
            null, null, null, null, null, null, null, null, getEnvironmentVariables(), assertionErrorList);

        // Query Audit Log Report for subscription currency amount entitlement
        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, currencyAmountEntitlementId, adminToolUserId,
            Action.CREATE.toString(), null, assertionErrorList);
        SubscriptionAndBasicOfferingsAuditLogReportHelper
            .assertionsForCurrencyAmountEntitlementInAuditLogReportDescription(descriptionPropertyValues, null,
                currencyAmount, null, "CLOUD (" + getEnvironmentVariables().getCloudCurrencyId() + ")",
                assertionErrorList);

        // Query Audit Log Report for subscription offer
        descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                subscriptionPlanId, offerId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // Subscription Offer description assertion
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionOfferInAuditLogReportDescription(
            descriptionPropertyValues, null, subscriptionOfferName, null, subscriptionOfferExternalKey, null,
            Status.ACTIVE, null, BILLING_PERIOD_ORIG_VALUE, assertionErrorList);

        // Query Audit Log Report for subscription price
        descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                subscriptionPlanId, priceId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        // Subscription Price description assertion
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues, null, Currency.USD + " (4)", null, SUBSCRIPTION_OFFER_ORIGINAL_PRICE, null,
            nameOfNamerPriceList + " (" + namerPriceListId + ")", null, priceStartDateInAuditLog, null,
            priceEndDateInAuditLog, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify offer price can be added with dates in past for subscription plan through add Subscription Plan in Admin
     * Tool
     *
     * @result Date should be added in Past.
     */
    @Test
    public void addPricesWithPastDateThroughUi() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8), subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Add Subscription Offer
        addSubscriptionPlanPage.addOffer(RandomStringUtils.randomAlphabetic(7), null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 0, false);
        // Add Subscription Price
        addSubscriptionPlanPage.addPricesInOffer(1, namerStoreName, nameOfNamerPriceList, "40", effectiveStartDate,
            effectiveEndDate);
        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final Date getEffectiveEndDateInDateFormat = DateTimeUtils.convertStringToDate(
            subscriptionPlanDetailPage.getEffectiveEndDateOfPriceInAnOffer(), PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Date getEffectiveStartDateInDateFormat =
            DateTimeUtils.convertStringToDate(subscriptionPlanDetailPage.getEffectiveStartDateOfPriceInAnOffer(),
                PelicanConstants.DATE_FORMAT_WITH_SLASH);
        AssertCollector.assertThat("Effective End Date is bigger than current Date ", getEffectiveEndDateInDateFormat,
            lessThan(DATE_TODAY), assertionErrorList);
        AssertCollector.assertThat("Effective Start Date is bigger than End Date ", getEffectiveStartDateInDateFormat,
            lessThan(getEffectiveEndDateInDateFormat), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify subscription plan is added with default packaging type in the admin tool.
     *
     * @result packaging type value should be none.
     */
    @Test
    public void addSubscriptionPlanPageWithDefaultPackagingType() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect packaging type", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(PelicanConstants.NONE), assertionErrorList);

        final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery =
            SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(planId).setOldName(null)
                .setNewName(subscriptionPlanExtKey).setOldExternalKey(null).setNewExternalKey(subscriptionPlanExtKey)
                .setOldOfferingType(null).setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null)
                .setNewStatus(Status.ACTIVE).setOldCancellationPolicy(null)
                .setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD).setOldUsageType(null)
                .setNewUsageType(UsageType.COM).setOldOfferingDetailId(null).setNewOfferingDetailId(null)
                .setOldProductLine(null).setNewProductLine(getProductLineMaya().getData().getId())
                .setOldSupportLevel(null).setNewSupportLevel(SupportLevel.BASIC).setAction(Action.CREATE)
                .setFileName(null).setOldPackagingValue(null).setNewPackagingValue(null)
                .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        // Verify Audit Log report results for subscription plan id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, planId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanInAuditLogReportDescription(
            descriptionPropertyValues, null, subscriptionPlanExtKey, null, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, null, Status.ACTIVE, null,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null, UsageType.COM, null, null, null,
            getProductLineMaya().getData().getId(), null, SupportLevel.BASIC, getEnvironmentVariables(), null, null,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify offer price can be added with dates in past for subscription plan through add Subscription Plan in Admin
     * Tool
     *
     * @result Date should be added in Past.
     */
    @Test(dataProvider = "getPackagingTypes")
    public void addSubscriptionPlanPageWithDifferentPackagingType(final PackagingType packagingType) {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, packagingType, true);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect packaging type", subscriptionPlanDetailPage.getPackagingType(),
            equalTo(packagingType.getDisplayName()), assertionErrorList);

        final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery =
            SubscriptionPlanDynamoQuery.builder().setSubscriptionPlanId(planId).setOldName(null)
                .setNewName(subscriptionPlanExtKey).setOldExternalKey(null).setNewExternalKey(subscriptionPlanExtKey)
                .setOldOfferingType(null).setNewOfferingType(OfferingType.BIC_SUBSCRIPTION).setOldStatus(null)
                .setNewStatus(Status.ACTIVE).setOldCancellationPolicy(null)
                .setNewCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD).setOldUsageType(null)
                .setNewUsageType(UsageType.COM).setOldOfferingDetailId(null).setNewOfferingDetailId(null)
                .setOldProductLine(null).setNewProductLine(getProductLineMaya().getData().getId())
                .setOldSupportLevel(null).setNewSupportLevel(SupportLevel.BASIC).setAction(Action.CREATE)
                .setFileName(null).setOldPackagingValue(null).setNewPackagingValue(packagingType)
                .setOldExpReminderEmailEnabled(null).setNewExpReminderEmailEnabled(PelicanConstants.TRUE).build();

        SubscriptionPlanAuditLogHelper.helperToQueryDynamoDb(subscriptionPlanDynamoQuery, assertionErrorList);

        // Verify Audit Log report results for subscription plan id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
                null, planId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanInAuditLogReportDescription(
            descriptionPropertyValues, null, subscriptionPlanExtKey, null, subscriptionPlanExtKey, null,
            OfferingType.BIC_SUBSCRIPTION, null, Status.ACTIVE, null,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null, UsageType.COM, null, null, null,
            getProductLineMaya().getData().getId(), null, SupportLevel.BASIC, getEnvironmentVariables(), null,
            packagingType, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the dynamoDB for data corresponding to create, edit and delete a subscription offer.
     *
     */
    @Test
    public void testCreateEditAndDeleteSubscriptionOfferSuccess() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8), subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);
        // Create subscription offer
        addSubscriptionPlanPage.addOffer(subscriptionOfferExternalKey, null, Status.NEW, true, null,
            BillingFrequency.MONTH, 1, false);
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        final String subscriptionOfferId = resource.subscriptionPlan().getById(subscriptionPlanId, null)
            .getSubscriptionOffers().getSubscriptionOffers().get(0).getId();
        AssertCollector.assertThat("Invalid subscriptionOfferId", subscriptionOfferId, notNullValue(),
            assertionErrorList);

        final String subscriptionOfferName = getProductLineExternalKeyMaya() + " " + SupportLevel.BASIC.getDisplayName()
            + " 1 month" + " - " + PelicanConstants.RECURRING;

        // Update Subscription Offer
        EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.clickOnEditOfferButton();
        editSubscriptionPlanPage.selectBillingFrequency("Weeks");
        editSubscriptionPlanPage.clickOnSaveOfferButton();
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        // Delete Subscription Offer
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        Util.scroll(getDriver(), "500", "0");
        editSubscriptionPlanPage.clickOnDeleteOfferButton();
        editSubscriptionPlanPage.clickOnSave(false);

        // Validate Subscription Offer add in Dynamo DB
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(subscriptionOfferId, null, subscriptionPlanId, null,
            subscriptionOfferName, null, subscriptionOfferExternalKey, null, BILLING_PERIOD_ORIG_VALUE, null,
            Status.NEW.toString(), Action.CREATE, assertionErrorList);
        // Dynamo DB validation on Update of subscription offer
        final String subscriptionOfferUpdatedName = getProductLineExternalKeyMaya() + " "
            + SupportLevel.BASIC.getDisplayName() + " 1 week" + " - " + PelicanConstants.RECURRING;
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(subscriptionOfferId, null, null, subscriptionOfferName,
            subscriptionOfferUpdatedName, null, null, BILLING_PERIOD_ORIG_VALUE, BILLING_PERIOD_NEW_VALUE, null, null,
            Action.UPDATE, assertionErrorList);
        // Dynamo DB validation on Delete
        SubscriptionPlanAuditLogHelper.validateSubscriptionOffer(subscriptionOfferId, null, null,
            subscriptionOfferUpdatedName, null, subscriptionOfferExternalKey, null, null, BILLING_PERIOD_NEW_VALUE,
            Status.NEW.toString(), null, Action.DELETE, assertionErrorList);

        // Verify Audit Log report results for subscription offer id.
        HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null,
            null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, subscriptionOfferId, adminToolUserId,
            Action.CREATE.toString(), subscriptionOfferName, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionOfferInAuditLogReportDescription(
            descriptionPropertyValues, null, subscriptionOfferName, null, subscriptionOfferExternalKey, null,
            Status.NEW, null, BILLING_PERIOD_ORIG_VALUE, assertionErrorList);

        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, subscriptionOfferId, adminToolUserId,
            Action.UPDATE.toString(), subscriptionOfferUpdatedName, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionOfferInAuditLogReportDescription(
            descriptionPropertyValues, subscriptionOfferName, subscriptionOfferUpdatedName, null, null, null, null,
            BILLING_PERIOD_ORIG_VALUE, BILLING_PERIOD_NEW_VALUE, assertionErrorList);

        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
            subscriptionPlanId, subscriptionOfferId, adminToolUserId, Action.DELETE.toString(),
            subscriptionOfferUpdatedName, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the dynamoDB for data corresponding to create, edit and delete a subscription offer price
     *
     */
    @Test
    public void testCreateEditAndDeleteSubscriptionOfferPriceSuccess() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8), subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Create subscription offer
        addSubscriptionPlanPage.addOffer(RandomStringUtils.randomAlphabetic(7), null, Status.NEW, true, null,
            BillingFrequency.MONTH, 0, false);
        // Adding price to the created offer
        addSubscriptionPlanPage.addPricesInOffer(1, namerStoreName, nameOfNamerPriceList,
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, effectiveStartDate, effectiveEndDate);
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        final String priceId = subscriptionPlanDetailPage.getPriceId();
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        final String subscriptionOfferWithPriceId = resource.subscriptionPlan().getById(subscriptionPlanId, null)
            .getSubscriptionOffers().getSubscriptionOffers().get(0).getId();
        AssertCollector.assertThat("Invalid subscriptionOfferId", subscriptionOfferWithPriceId, notNullValue(),
            assertionErrorList);

        // Update subscription Offer Price
        EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        Util.scroll(getDriver(), "500", "0");
        final String subscriptionOfferPriceId = editSubscriptionPlanPage.getPriceId();
        AssertCollector.assertThat("Invalid subscriptionOfferPriceId", subscriptionOfferPriceId, notNullValue(),
            assertionErrorList);
        editSubscriptionPlanPage.clickOnEditPriceButton();
        editSubscriptionPlanPage.editPriceAmount(SUBSCRIPTION_OFFER_NEW_PRICE);
        editSubscriptionPlanPage.clickOnSavePriceButton();
        editSubscriptionPlanPage.checkExpiredPrice();
        editSubscriptionPlanPage.clickOnSavePriceButton();
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        // Delete Subscription Offer Price subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.clickOnDeletePriceButton();
        editSubscriptionPlanPage.clickOnSave(false);

        // Dynamo DB validation
        SubscriptionPlanAuditLogHelper.validateSubscriptionPrice(subscriptionPlanId, subscriptionOfferWithPriceId,
            priceId, null, SUBSCRIPTION_OFFER_ORIGINAL_PRICE, null, Currency.USD, null, priceStartDateInAuditLog, null,
            priceEndDateInAuditLog, Action.CREATE, assertionErrorList);
        SubscriptionPlanAuditLogHelper.validateSubscriptionPrice(subscriptionPlanId, subscriptionOfferWithPriceId,
            priceId, SUBSCRIPTION_OFFER_ORIGINAL_PRICE, SUBSCRIPTION_OFFER_NEW_PRICE, null, null, null, null, null,
            null, Action.UPDATE, assertionErrorList);
        SubscriptionPlanAuditLogHelper.validateSubscriptionPrice(subscriptionPlanId, subscriptionOfferWithPriceId,
            priceId, null, SUBSCRIPTION_OFFER_NEW_PRICE, Currency.USD, null, priceStartDateInAuditLog, null,
            priceEndDateInAuditLog, null, Action.DELETE, assertionErrorList);

        // Verify Audit Log report results for subscription offer price id.
        HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null,
            null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, subscriptionOfferPriceId,
            adminToolUserId, Action.CREATE.toString(), nameOfNamerPriceList, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues, null, Currency.USD + " (4)", null, SUBSCRIPTION_OFFER_ORIGINAL_PRICE, null,
            nameOfNamerPriceList + " (" + namerPriceListId + ")", null, priceStartDateInAuditLog, null,
            priceEndDateInAuditLog, assertionErrorList);

        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, subscriptionOfferPriceId, adminToolUserId,
            Action.UPDATE.toString(), nameOfNamerPriceList, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues, null, null, SUBSCRIPTION_OFFER_ORIGINAL_PRICE, SUBSCRIPTION_OFFER_NEW_PRICE,
            null, null, null, null, null, null, assertionErrorList);

        auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN,
            subscriptionPlanId, subscriptionOfferPriceId, adminToolUserId, Action.DELETE.toString(),
            nameOfNamerPriceList, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the dynamoDB for data corresponding to create and edit subscription plan descriptors.
     */
    @Test(dataProvider = "getSubscriptionPlanAndOfferDescriptorData")
    public void testCreateAndEditSubscriptionPlanDescriptorsSuccess(final String groupName, final String fieldName,
        final String apiName, final boolean isLocalDescriptor) {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8), subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        // Add descriptor
        Descriptor descriptor;
        String descriptorId = DescriptorUtils.getExistingDescriptorId(adminToolPage, findDescriptorDefinitionsPage,
            DescriptorEntityTypes.SUBSCRIPTION_PLAN, groupName, fieldName);
        // Descriptor is not present and hence add the descriptor
        if (descriptorId == null) {
            if (isLocalDescriptor) {
                descriptor = DescriptorUtils.getDescriptorData(DescriptorEntityTypes.SUBSCRIPTION_PLAN, groupName,
                    fieldName, apiName, PelicanConstants.YES, MAX_DESCRIPTOR_LENGTH);
            } else {
                descriptor = DescriptorUtils.getDescriptorData(DescriptorEntityTypes.SUBSCRIPTION_PLAN, groupName,
                    fieldName, apiName, PelicanConstants.NO, MAX_DESCRIPTOR_LENGTH);
            }
            addDescriptorPage.addDescriptor(descriptor);
            descriptorDetailPage =
                new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
            descriptorId = descriptorDetailPage.getDescriptorEntityFromDetails().getId();
        }

        // Add a descriptor value to the subscription plan
        subscriptionPlanDetailPage.navigateToSubscriptionPlanPage(subscriptionPlanId);
        final String descriptorValue = "TestPlanDescriptor oldValue";
        final String modifiedDescriptorValue = "TestPlanDescriptor modified";
        updateSubscriptionPlanDescriptorValue(groupName, apiName, descriptorValue, isLocalDescriptor);

        // Update the descriptor value
        updateSubscriptionPlanDescriptorValue(groupName, apiName, modifiedDescriptorValue, isLocalDescriptor);

        final String descriptorIdFromTable = getSubscriptionPlanOrOfferDescriptorIdFromTable(subscriptionPlanId);
        final String language = isLocalDescriptor ? PelicanConstants.LANGUAGE_EN : PelicanConstants.EMPTY_STRING;
        SubscriptionPlanAuditLogHelper.validateSubscriptionPlanOrOfferDescriptor(descriptorIdFromTable,
            subscriptionPlanId, null, null, getEnvironmentVariables().getAppFamilyId(), null, descriptorId, null,
            descriptorValue, null, language, null, PelicanConstants.EMPTY_STRING, Action.CREATE, assertionErrorList);
        SubscriptionPlanAuditLogHelper.validateSubscriptionPlanOrOfferDescriptor(descriptorIdFromTable,
            subscriptionPlanId, null, null, null, null, null, descriptorValue, modifiedDescriptorValue, null, null,
            null, null, Action.UPDATE, assertionErrorList);

        // Verify Audit Log report results for CREATE action for subscription plan or offer descriptor id.
        HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null,
            null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, descriptorIdFromTable, adminToolUserId,
            Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
            descriptionPropertyValues, descriptorId, null, descriptorValue, fieldName, Action.CREATE,
            assertionErrorList);

        // Verify Audit Log report results for UPDATE action for subscription plan id.
        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, descriptorIdFromTable, adminToolUserId,
            Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
            descriptionPropertyValues, descriptorId, descriptorValue, modifiedDescriptorValue, fieldName, Action.UPDATE,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the dynamoDB for data corresponding to create and edit subscription offer descriptors
     */
    @Test(dataProvider = "getSubscriptionPlanAndOfferDescriptorData")
    public void testCreateAndEditSubscriptionOfferDescriptorsSuccess(final String groupName, final String fieldName,
        final String apiName, final boolean isLocalDescriptor) {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8), subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Create subscription offer
        final String subscriptionOfferExtKey = RandomStringUtils.randomAlphabetic(8);
        addSubscriptionPlanPage.addOffer(subscriptionOfferExtKey, null, Status.NEW, true, null, BillingFrequency.MONTH,
            0, false);
        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        // Add descriptor
        Descriptor descriptor;
        String descriptorId = DescriptorUtils.getExistingDescriptorId(adminToolPage, findDescriptorDefinitionsPage,
            DescriptorEntityTypes.SUBSCRIPTION_OFFER, groupName, fieldName);
        // Descriptor is not present and hence add the descriptor
        if (descriptorId == null) {
            if (isLocalDescriptor) {
                descriptor = DescriptorUtils.getDescriptorData(DescriptorEntityTypes.SUBSCRIPTION_OFFER, groupName,
                    fieldName, apiName, PelicanConstants.YES, MAX_DESCRIPTOR_LENGTH);
            } else {
                descriptor = DescriptorUtils.getDescriptorData(DescriptorEntityTypes.SUBSCRIPTION_OFFER, groupName,
                    fieldName, apiName, PelicanConstants.NO, MAX_DESCRIPTOR_LENGTH);
            }
            addDescriptorPage.addDescriptor(descriptor);
            descriptorDetailPage =
                new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
            descriptorId = descriptorDetailPage.getDescriptorEntityFromDetails().getId();
        }

        // Add a descriptor value to the subscription plan
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(subscriptionPlanId);
        final String descriptorValue = "TestOfferDescriptor oldValue";
        final String modifiedDescriptorValue = "TestOfferDescriptor modifiedValue";
        subscriptionOfferDetailPage = subscriptionPlanDetailPage.clickOnViewOfferDescriptors();
        updateSubscriptionOfferDescriptorValue(groupName, apiName, descriptorValue, isLocalDescriptor);

        // Update the descriptor value
        updateSubscriptionOfferDescriptorValue(groupName, apiName, modifiedDescriptorValue, isLocalDescriptor);

        final String subscriptionOfferId = resource.subscriptionPlan().getById(subscriptionPlanId, null)
            .getSubscriptionOffers().getSubscriptionOffers().get(0).getId();
        AssertCollector.assertThat("Invalid subscriptionOfferId", subscriptionPlanId, notNullValue(),
            assertionErrorList);

        final String descriptorIdFromTable = getSubscriptionPlanOrOfferDescriptorIdFromTable(subscriptionOfferId);
        final String language = isLocalDescriptor ? PelicanConstants.LANGUAGE_EN : PelicanConstants.EMPTY_STRING;
        SubscriptionPlanAuditLogHelper.validateSubscriptionPlanOrOfferDescriptor(descriptorIdFromTable,
            subscriptionPlanId, subscriptionOfferId, null, getEnvironmentVariables().getAppFamilyId(), null,
            descriptorId, null, descriptorValue, null, language, null, PelicanConstants.EMPTY_STRING, Action.CREATE,
            assertionErrorList);
        SubscriptionPlanAuditLogHelper.validateSubscriptionPlanOrOfferDescriptor(descriptorIdFromTable,
            subscriptionPlanId, subscriptionOfferId, null, null, null, null, descriptorValue, modifiedDescriptorValue,
            null, null, null, null, Action.UPDATE, assertionErrorList);

        // Verify Audit Log report results for subscription plan or offer descriptor id.
        HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null,
            null, PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, descriptorIdFromTable, adminToolUserId,
            Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
            descriptionPropertyValues, descriptorId, null, descriptorValue, fieldName, Action.CREATE,
            assertionErrorList);

        descriptionPropertyValues = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_SUBSCRIPTION_PLAN, subscriptionPlanId, descriptorIdFromTable, adminToolUserId,
            Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
            descriptionPropertyValues, descriptorId, descriptorValue, modifiedDescriptorValue, fieldName, Action.UPDATE,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will test whether product line and usage type are required fields in add subscription plan
     */
    @Test
    public void testProductLineAndUsageTypeAsRequiredInAddSubscriptionPlan() {

        addSubscriptionPlanPage.addSubscriptionPlanInfo(RandomStringUtils.randomAlphanumeric(8), null,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null,
            null, null, SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        final String usageTypeErrorMessage = addSubscriptionPlanPage.getUsageTypeErrorMessage();
        final String productLineErrorMessage = addSubscriptionPlanPage.getProductLineErrorMessage();
        commonAssertionsForErrorMessages(usageTypeErrorMessage, productLineErrorMessage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the random external key if no external key is provided by the user when
     * adding a subscription plan in the admin tool
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testRandomExternalKeyGeneratedInAddSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, "", OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, null, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the user entered external key if external key is provided by the user
     * when adding a subscription plan in the admin tool
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testUserExternalKeyGeneratedInAddSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        final HashMap<String, String> planFieldsMap = getFieldsFromSubscriptionPlan(subscriptionPlanDetailPage);
        commonAssertionsForSubscriptionPlanFields(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, planFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test adding the subscription plan with a duplicate external key
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testDuplicateExternalKeyGeneratedInAddSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);
        addSubscriptionPlanPage.clickOnSave(false);
        final String errorMessage = addSubscriptionPlanPage.getExternalKeyErrorMessage();
        AssertCollector.assertThat("Incorrect error message displayed for duplicate external key", errorMessage,
            equalTo(PelicanErrorConstants.DUPLICATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Product Line with In-Active State won't show up under drop down menu while adding to
     * subscription plan.
     */
    @Test
    public void testInActiveProductLineIsNotVisibleToSelectForAddSubscriptionPlan() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";
        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.NO);

        addSubscriptionPlanPage.navigateToAddSubscriptionPlan();
        final List<String> productLineList = addSubscriptionPlanPage.getActiveProductLine();
        final boolean isProductLinePresent = productLineList.contains(prodLineNameAndExternalKey);
        AssertCollector.assertFalse("Product Line should not be visible but it is visible", isProductLinePresent,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify that after editing Product Line with In-Active State to active State it will show up under drop
     * down menu while adding to subscription plan.
     */
    @Test
    public void testAfterEditingInActiveProductLineToActiveItsVisibleForAddSubscriptionPlan() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";
        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.NO);

        productLineDetailsPage = addProductLinePage.clickOnSubmit();
        final String productLineId = productLineDetailsPage.getId();
        addSubscriptionPlanPage.navigateToAddSubscriptionPlan();
        final List<String> productLineList = addSubscriptionPlanPage.getActiveProductLine();
        final boolean isProductLinePresent = productLineList.contains(prodLineNameAndExternalKey);
        AssertCollector.assertFalse("Product Line should not be visible but it is visible", isProductLinePresent,
            assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidId(productLineId);
        // Edit ProductLine for Active Status.
        productLineDetailsPage.clickOnEdit();
        productLineDetailsPage.selectActiveStatus(PelicanConstants.YES);
        productLineDetailsPage.submit();
        HelperForProductLine.assertNameAndExternalKey(productLineDetailsPage, name, externalKey, PelicanConstants.YES,
            assertionErrorList);
        addSubscriptionPlanPage.navigateToAddSubscriptionPlan();
        final List<String> productLineList1 = addSubscriptionPlanPage.getActiveProductLine();
        final boolean isProductLinePresent1 = productLineList1.contains(prodLineNameAndExternalKey);
        AssertCollector.assertTrue("Product Line should not be visible but it is visible", isProductLinePresent1,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method tests the Auto Generation of External Key and Name of an Offer.
     */
    @Test(dataProvider = "getDataForAutoGenerationForOfferName")
    public void testAutoGenerationOfferNameAndExternalKeyInSubscriptionPlan(final OfferingType offeringType,
        final SupportLevel supportLevel, final Status status, final BillingFrequency billingFrequency,
        int billingFrequencyCount) {
        final String randomString = RandomStringUtils.randomAlphanumeric(8);
        // add Subscription Plan
        addSubscriptionPlanPage.addSubscriptionPlanInfo(randomString, randomString, offeringType, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            supportLevel, null, true);
        // add Offer in Subscription Plan Without Name
        addSubscriptionPlanPage.addOffer(null, null, Status.ACTIVE, true, null, billingFrequency, billingFrequencyCount,
            false);
        AssertCollector.assertThat("External Key of Subscription Offer is not null ",
            subscriptionPlanDetailPage.getOfferExternalKey(1), equalTo(""), assertionErrorList);

        String expectedOfferName = null;
        String billingFrequencyInOffer;
        if (billingFrequencyCount == 0 || billingFrequencyCount == 1) {
            billingFrequencyInOffer = billingFrequency.getName().toLowerCase();
            billingFrequencyCount = 1;
        } else {
            billingFrequencyInOffer = billingFrequency.getDisplayName().toLowerCase();
        }

        // if support level is advanced then offer name has word subscription after productline and if
        // support level
        // is basic then offer name will have basic right after product line and there is no support
        // level then it
        // offer name will be without support level
        if (supportLevel == null) {
            expectedOfferName = getProductLineExternalKeyMaya() + " " + billingFrequencyCount + " "
                + billingFrequencyInOffer + " - " + PelicanConstants.RECURRING;
        } else if (supportLevel == SupportLevel.ADVANCED) {
            expectedOfferName = getProductLineExternalKeyMaya() + " Subscription " + billingFrequencyCount + " "
                + billingFrequencyInOffer + " - " + PelicanConstants.RECURRING;
        } else if (supportLevel == SupportLevel.BASIC) {
            expectedOfferName = getProductLineExternalKeyMaya() + " " + supportLevel.getDisplayName() + " "
                + billingFrequencyCount + " " + billingFrequencyInOffer + " - " + PelicanConstants.RECURRING;
        } else {
            LOGGER.info("support level not found");
        }
        // add prices in an offer to Add subscription Plan with an offer
        addSubscriptionPlanPage.addPricesInOffer(1, namerStoreName, nameOfNamerPriceList, "40", effectiveStartDate,
            DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 5));
        addSubscriptionPlanPage.clickOnSave(false);
        AssertCollector.assertThat(
            "External Key of Subscription Offer is not Prefixed with "
                + PelicanConstants.AUTO_GENERATE_OFFER_EXTERNAL_KEY_PREFIX,
            (subscriptionPlanDetailPage.getOfferExternalKey(1)).substring(0, 2),
            equalTo(PelicanConstants.AUTO_GENERATE_OFFER_EXTERNAL_KEY_PREFIX), assertionErrorList);
        AssertCollector.assertThat(
            "External Key of Subscription Offer has more than 13 characters "
                + PelicanConstants.AUTO_GENERATE_OFFER_EXTERNAL_KEY_PREFIX,
            ((subscriptionPlanDetailPage.getOfferExternalKey(1)).substring(3)).length(), lessThan(13),
            assertionErrorList);
        AssertCollector.assertThat("Subscription Offer Name is not correct", subscriptionPlanDetailPage.getOfferName(1),
            equalTo(expectedOfferName), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method is to verify the Add Subscription Flow for, Adding two features to the offering which
     * should default the core product (if available) value that matches the feature's external key
     */

    @Test
    public void testAddSubscriptionPlanPopulateDefaultCoreProductWithLookup() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey = RandomStringUtils.randomAlphanumeric(8);
        addCoreProductsPage.addCoreProduct(commonExtKey);

        final Item item = featureApiUtils.addFeature(commonExtKey, commonExtKey, null);
        final String itemId = item.getId();

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        // Add 1st Feature Entitlement
        int entitlement = 0;
        addSubscriptionPlanPage.getResultsOnFeatureSearch(entitlement, commonExtKey);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(addSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        // Add 2nd Feature Entitlement
        entitlement = 1;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(itemId);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(addSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        final boolean isCoreProductSelected =
            addSubscriptionPlanPage.validateCoreProductSelection(entitlement, commonExtKey);

        AssertCollector.assertTrue("Core Product with same extkey as Feature is not selected by default from lookup",
            isCoreProductSelected, assertionErrorList);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        AssertCollector.assertThat("Incorrect Core Product displayed in Subscription details page",
            subscriptionPlanDetailPage.getOneTimeEntitlementCoreProductColumnValues().get(0),
            containsString(commonExtKey), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method is to verify the Add Subscription Flow for, Adding a feature to the offering should default
     * the core product (if available) value that matches the feature's external key
     */

    @Test
    public void testAddSubscriptionPlanPopulateDefaultCoreProductWithInputId() {
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey = RandomStringUtils.randomAlphanumeric(8);
        addCoreProductsPage.addCoreProduct(commonExtKey);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final Item item = featureApiUtils.addFeature(commonExtKey, commonExtKey, null);
        final String itemId = item.getId();

        // Add Feature Entitlement
        final int entitlement = 0;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(itemId);

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(addSubscriptionPlanPage.getFeatureName(entitlement)), assertionErrorList);

        final boolean isCoreProductSelected =
            addSubscriptionPlanPage.validateCoreProductSelection(entitlement, commonExtKey);

        AssertCollector.assertTrue("Core Product with same extkey as Feature is selected by default",
            isCoreProductSelected, assertionErrorList);
        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        final String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        AssertCollector.assertThat("Incorrect Core Product displayed in Subscription details page",
            subscriptionPlanDetailPage.getOneTimeEntitlementCoreProductColumnValues().get(0),
            containsString(commonExtKey), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the add feature functionality in new status
     *
     */

    @Test
    public void testAddFeatureToSubscriptionPlanInNewStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        Item item = featureApiUtils.addFeature(null, null, null);
        String itemId = item.getId();

        // Add Feature Entitlement
        final int entitlement = 0;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(itemId);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        item = featureApiUtils.addFeature(null, null, null);
        itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 1);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the add feature functionality in active status
     *
     */

    @Test
    public void testAddFeatureToSubscriptionPlanInActiveStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();

        // Add Feature Entitlement
        final int entitlement = 0;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(itemId);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        final String hoursOfBlock = DbUtils.getMidasHiveValue(
            PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS, getEnvironmentVariables());

        final int blockHours = Integer.parseInt(hoursOfBlock);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        // Retrieve entitlement options
        final List<WebElement> options = addSubscriptionPlanPage.getOptionsUnderEntitlement(0);
        final String featureDisableText = options.get(options.size() - 1).getText();
        final String date =
            DateTimeUtils.getDatetimeWithAddedHoursAsString(PelicanConstants.FEATURE_DISABLE_DATE_FORMAT, blockHours);

        AssertCollector.assertThat("Add Feature is allowed within the configuration time", featureDisableText,
            equalTo(PelicanConstants.DISABLE_FEATURE_MESSAGE + date + PelicanConstants.CLOSING_PARANTHESIS),
            assertionErrorList);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the add feature functionality in active status
     *
     */

    @Test
    public void testAddFeatureToSubscriptionPlanFromNewToActiveStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey = RandomStringUtils.randomAlphanumeric(8);
        addCoreProductsPage.addCoreProduct(commonExtKey);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        Item item = featureApiUtils.addFeature(null, null, null);
        String itemId = item.getId();

        // Add Feature Entitlement
        final int entitlement = 0;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(itemId);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        String planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", planId, notNullValue(), assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        item = featureApiUtils.addFeature(null, null, null);
        itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 1);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave(true);
        planId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", planId, notNullValue(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Bug: Feature disappearing bug. This is a test method which will test the fix for disappearing of features in edit
     * flow
     *
     */

    @Test
    public void testDisappearingOfFeaturesToSubscriptionPlan() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final String commonExtKey = RandomStringUtils.randomAlphanumeric(8);
        addCoreProductsPage.addCoreProduct(commonExtKey);

        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final Item item = featureApiUtils.addFeature(null, null, null);
        final String firstItemName = item.getName();

        // Add Feature Entitlement
        int entitlement = 0;
        addSubscriptionPlanPage.addFeatureInOneTimeEntitlement(entitlement);
        addSubscriptionPlanPage.setFeatureAsInput(item.getId());

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Incorrect subscription plan id", subscriptionPlanId, notNullValue(),
            assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null,
            null, productLineNameAndExternalKey, SupportLevel.BASIC, null, true);

        final Item item2 = featureApiUtils.addFeature(null, null, null);
        final String secondItemName = item2.getName();
        entitlement++;
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(item2.getId(), null, null, entitlement);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave(true);

        editSubscriptionPlanPage.editUsageType(UsageType.COM);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        final String newEntitlementId1 =
            DbUtils.getEntitlementIdFromItemId(subscriptionPlanId, item.getId(), getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed",
            subscriptionPlanDetailPage.getEntitlementDetails(newEntitlementId1, 2), containsString(firstItemName),
            assertionErrorList);

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(subscriptionPlanId, item2.getId(), getEnvironmentVariables());

        AssertCollector.assertThat("Incorrect Feature Name is disaplyed",
            subscriptionPlanDetailPage.getEntitlementDetails(newEntitlementId2, 2), containsString(secondItemName),
            assertionErrorList);
        subscriptionPlanId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to test Offer name with combinations of supportLevel, billingFrequency and billingCount.
     */
    @DataProvider(name = "getDataForAutoGenerationForOfferName")
    public Object[][] getDataForAutoGenerationForOfferName() {
        return new Object[][] {
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.BASIC, Status.ACTIVE, BillingFrequency.DAY, 1 },
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.BASIC, Status.ACTIVE, BillingFrequency.MONTH, 1 },
                { OfferingType.BIC_SUBSCRIPTION, SupportLevel.ADVANCED, Status.ACTIVE, BillingFrequency.YEAR, 1 } };
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
     * Get the id of the matching descriptor from the descriptor table
     *
     * @return id of the matching descriptor in descriptor table
     */
    private String getSubscriptionPlanOrOfferDescriptorIdFromTable(final String subscriptionPlanOrOfferId) {
        return DbUtils.selectQuery(
            "Select ID from descriptor where ENTITY_ID = " + subscriptionPlanOrOfferId + " order by ID desc", "ID",
            getEnvironmentVariables()).get(0);
    }

    /**
     * Update the Localized/Non-localized subscription plan descriptor value
     */
    private void updateSubscriptionPlanDescriptorValue(final String groupName, final String fieldName,
        final String descriptorValue, final boolean isLocalDescriptor) {
        if (isLocalDescriptor) {
            editSubscriptionPlanAndOfferDescriptorsPage = subscriptionPlanDetailPage.clickOnEditLocalizedDescriptors();
        } else {
            editSubscriptionPlanAndOfferDescriptorsPage =
                subscriptionPlanDetailPage.clickOnEditNonLocalizedDescriptors();
        }
        editSubscriptionPlanAndOfferDescriptorsPage.editDescriptorValue(groupName, fieldName, descriptorValue);
        editSubscriptionPlanAndOfferDescriptorsPage.clickOnUpdateDescriptorsButton();
    }

    /**
     * Update the Localized/Non-localized subscription offer descriptor value
     */
    private void updateSubscriptionOfferDescriptorValue(final String groupName, final String fieldName,
        final String descriptorValue, final boolean isLocalDescriptor) {
        if (isLocalDescriptor) {
            editSubscriptionPlanAndOfferDescriptorsPage = subscriptionOfferDetailPage.clickOnEditLocalizedDescriptors();
        } else {
            editSubscriptionPlanAndOfferDescriptorsPage =
                subscriptionOfferDetailPage.clickOnEditNonLocalizedDescriptors();
        }
        editSubscriptionPlanAndOfferDescriptorsPage.editDescriptorValue(groupName, fieldName, descriptorValue);
        editSubscriptionPlanAndOfferDescriptorsPage.clickOnUpdateDescriptorsButton();
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
     * This is a test case which will test validations for adding CSR with assignable in different states
     *
     * @throws ParseException
     */
    @Test(dataProvider = "SubscriptionPlanStatusesAssignableTest")
    public void testAssignableValuesInAddSubscriptionPlan(final Status status, final Boolean isAssignable)
        throws ParseException {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        if (status == Status.CANCELED) {

            addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
                CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
                SupportLevel.BASIC, null, true);

        } else {

            addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
                CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
                SupportLevel.BASIC, null, true);
        }

        // Add Feature Entitlement
        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        AssertCollector.assertTrue("Incorrect default value for the CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(0), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(isAssignable, eosDate, eolRenewalDate,
            eolImmediateDate, 0);

        Boolean shouldHandlePopup = false;
        if (status != Status.NEW) {
            shouldHandlePopup = true;
        }

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(shouldHandlePopup);

        final String newEntitlementId1 = DbUtils.getEntitlementIdFromItemId(subscriptionPlanDetailPage.getId(),
            cprItemId1, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, isAssignable.toString(), eosDate, eolRenewalDate, eolImmediateDate, true,
            assertionErrorList);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        if (status == Status.NEW) {
            final Boolean newFlag = !(isAssignable);
            editSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(newFlag, eosDate, eolRenewalDate,
                eolImmediateDate, 0);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

            subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
            AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
                newEntitlementId1, newFlag.toString(), eosDate, eolRenewalDate, eolImmediateDate, true,
                assertionErrorList);

        } else if (status == Status.ACTIVE) {

            editSubscriptionPlanPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
            AssertCollector.assertThat("Assignable Checkbox is editabled in ACTIVE state",
                editSubscriptionPlanPage.setAssignable(0, true).toString(), is("false"), assertionErrorList);

        } else if (status == Status.CANCELED) {
            editSubscriptionPlanPage.editStatus(Status.CANCELED);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
            subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

            AssertCollector.assertThat("Assignable Checkbox is editable in Cancel state",
                subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(newEntitlementId1, 1, 4),
                equalTo(isAssignable.toString()), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will test adding csr with the assignable false in new and active state
     *
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testAtleastoneCsrInSubscriptionPlan(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        AssertCollector.assertTrue("Incorrect default value for the CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(0), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(true, eosDate, eolRenewalDate, eolImmediateDate, 0);

        if (status == Status.NEW) {
            final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
            AssertCollector.assertThat("Add Subscription plan failed when saved with 1 CSR in NEW state",
                subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);

        } else if (status == Status.ACTIVE) {
            SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
            AssertCollector.assertThat(
                "Add Subscription plan, when saved with 1 CSR with assignable false in Active state",
                subscriptionPlanDetailPage.getError(), equalTo(PelicanErrorConstants.CSR_ERROR), assertionErrorList);
            addSubscriptionPlanPage.removeFeature();
            subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
            editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
            addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1,
                PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
                new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
            AssertCollector.assertThat(
                "Edit Subscription plan, when saved with 1 CSR with assignable false in Active state",
                subscriptionPlanDetailPage.getError(), equalTo(PelicanErrorConstants.CSR_ERROR), assertionErrorList);
            subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);
            AssertCollector.assertThat(
                "Edit Subscription plan, when saved with 1 CSR with assignable false in Active state",
                subscriptionPlanDetailPage.getError(), equalTo(PelicanErrorConstants.CSR_ERROR), assertionErrorList);
        }

    }

    /**
     * This is a test case which will test adding dates with past value in active state
     *
     */

    @Test
    public void testEntitlementtDatesInSubscriptionPlanForActivePastDate() {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        String eosDate = new DateTime().minusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        String eolRenewalDate =
            new DateTime().minusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        String eolImmediateDate =
            new DateTime().minusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);

        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        AssertCollector.assertThat("eosDate must be in the future", subscriptionPlanDetailPage.getError(),
            equalTo(PelicanErrorConstants.SUB_PLAN_UPLOAD_PAST_EOS_DATE_ERROR), assertionErrorList);

        eosDate = new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        eolRenewalDate = new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        eolImmediateDate = new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        AssertCollector.assertThat(
            "SubscriptionPlan page Add fail when adding non-null dates for a one-time entitlement",
            subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);

    }

    /**
     * This is a test case which will test adding dates with wrong relationship in active state
     *
     */

    @Test
    public void testEntitlementtDatesInSubscriptionPlanForActiveWrongRelation() {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final String eosDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        AssertCollector.assertThat(
            "When setting multiple EOS and EOL dates, follow this logic: EOS Date < EOL Renewal Date < EOL Immediate Date",
            subscriptionPlanDetailPage.getError(), equalTo(PelicanErrorConstants.SUB_PLAN_UPLOAD_DATES_OUT_OF_ORDER),
            assertionErrorList);

    }

    /**
     * This is a test case which will test adding null dates new and active state
     *
     */
    @Test(dataProvider = "SubscriptionPlanStatuses")
    public void testEntitlementtDatesInSubscriptionPlanForNull(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, status,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final String eosDate = null;
        final String eolRenewalDate = null;
        final String eolImmediateDate = null;

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        if (status == Status.NEW) {
            subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        } else if (status == Status.ACTIVE) {
            subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        }
        AssertCollector.assertThat("SubscriptionPlan page Add fail when adding null dates for a one-time entitlement",
            subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);

    }

    /**
     * This is a test case which will test adding past dates in new and active state
     *
     */
    @Test
    public void testEntitlementtDatesInSubscriptionPlanForNew() {

        final String name = RandomStringUtils.randomAlphanumeric(8);

        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        final String eosDate =
            new DateTime().minusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().minusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().minusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(cprItemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);
        AssertCollector.assertThat("Add Subscription plan failed when saved with past dates in NEW state",
            subscriptionPlanDetailPage.getId(), is(notNullValue()), assertionErrorList);

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
        planFieldsMap.put("Actual Audit Trail Link", subscriptionPlanDetailPage.getAuditTrailLink());
        planFieldsMap.put("Expected Audit Trail Link",
            subscriptionPlanDetailPage.generateAuditTrailLink(subscriptionPlanDetailPage.getId()));

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
                planFieldsMap.get("ExternalKey").substring(0, 3),
                equalTo(PelicanConstants.AUTO_GENERATE_SUBSCRIPTION_PLAN_EXTERNAL_KEY_PREFIX), assertionErrorList);
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
        AssertCollector.assertThat("Incorrect Audit Trail Link", planFieldsMap.get("Actual Audit Trail Link"),
            equalTo(planFieldsMap.get("Expected Audit Trail Link")), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass local and non-local descriptor data to testCreateAndEditSubscriptionPlanDescriptorsSuccess
     * and testCreateAndEditSubscriptionOfferDescriptorsSuccess methods
     */
    @DataProvider(name = "getSubscriptionPlanAndOfferDescriptorData")
    public Object[][] getSubscriptionPlanAndOfferDescriptorData() {
        return new Object[][] {
                { PelicanConstants.IPP, "AUTO_TEST_LOCAL_DESCRIPTOR", PelicanConstants.LOCAL_DESCRIPTOR_API, true },
                { PelicanConstants.ESTORE, "AUTO_TEST_DESCRIPTOR", PelicanConstants.DESCRIPTOR_API, false } };
    }

    @DataProvider(name = "SubscriptionPlanStatuses")
    public Object[][] getSubscriptionPlanStatuses() {
        return new Object[][] { { Status.NEW }, { Status.ACTIVE } };
    }

    /**
     * Test Assignable and Remove features dates with Feature flags
     *
     * @return
     */
    @DataProvider(name = "SubscriptionPlanStatusesAssignableTest")
    public Object[][] getSubscriptionPlanStatusesAssignableTest() {
        return new Object[][] { { Status.NEW, true }, { Status.NEW, false }, { Status.ACTIVE, false },
                { Status.CANCELED, false } };
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
