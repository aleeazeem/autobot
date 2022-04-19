package com.autodesk.bsm.pelican.ui.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.commonassertions.AssertionsForViewSubscriptionPlanPage;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
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
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanGenericPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This test class is created to test Subscription Plan Details Page.
 *
 * @author Vy Ly
 */
public class ViewSubscriptionPlanTest extends SeleniumWebdriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewSubscriptionPlanTest.class.getSimpleName());
    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static FindSubscriptionPlanPage findSubscriptionPlanPage;

    private String bicOfferingId;
    private Random rand;

    private List<String> oneTimeEntitlementIds;
    private AdminToolPage adminToolPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private PelicanPlatform resource;
    private AddSubscriptionPlanPage addSubscriptionPlanPage;
    private static String productLineNameAndExternalKey;
    private SubscriptionPlanGenericPage subscriptionPlanGenericPage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        subscriptionPlanGenericPage = adminToolPage.getPage(SubscriptionPlanGenericPage.class);

        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOfferingId = bicOffering.getOfferings().get(0).getId();
        rand = new Random();
        oneTimeEntitlementIds = new ArrayList<>();

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());

        // Add between 3 and 10 one time entitlement features to plan
        final int numFeatures = rand.nextInt(7) + 3;

        for (int i = 0; i < numFeatures; i++) {
            final Item item = featureApiUtils.addFeature(null, null, null);
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferingId, item.getId(), null, true);
            final String newEntitlementId =
                DbUtils.getEntitlementIdFromItemId(bicOfferingId, item.getId(), getEnvironmentVariables());
            oneTimeEntitlementIds.add(newEntitlementId);
        }

        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferingId);

        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        // Add 2 Currency Amount Entitlements
        editSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("100", PelicanConstants.CLOUD_CURRENCY_SELECT,
            numFeatures);
        editSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("200", PelicanConstants.CLOUD_CURRENCY_SELECT,
            (numFeatures + 1));

        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        LOGGER.info("Adding subscription plan with " + numFeatures + " one time entitlements");

        // Add product Line
        final ProductLine productLine = new ProductLine();
        final ProductLineData productLineData = new ProductLineData();
        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        subscriptionPlanApiUtils.addProductLine(resource, productLine);
        productLineNameAndExternalKey = productLine.getData().getName() + " (" + productLine.getData().getName() + ")";
    }

    /**
     * This method tests expandable rows on the Subscription Plan view page.
     */
    @Test
    public void testOneTimeEntitlementExpandableRows() {
        subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(bicOfferingId);

        final List<String> collapsedIndexIds = new ArrayList<>(oneTimeEntitlementIds);
        final List<String> expandedIndexIds = new ArrayList<>();

        for (final String id : oneTimeEntitlementIds) {
            AssertCollector.assertFalse("Row " + id + " should be collapsed",
                subscriptionPlanDetailPage.isEntitlementRowExpanded(id), assertionErrorList);
        }

        final int randItem = rand.nextInt(collapsedIndexIds.size());

        String randOneTimeEntitlementId = collapsedIndexIds.get(randItem);
        // Expand one random entitlement row
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(randOneTimeEntitlementId);
        expandedIndexIds.add(randOneTimeEntitlementId);
        collapsedIndexIds.remove(randOneTimeEntitlementId);

        AssertCollector.assertTrue(String.format("Row %s should be expanded", randOneTimeEntitlementId),
            subscriptionPlanDetailPage.isEntitlementRowExpanded(randOneTimeEntitlementId), assertionErrorList);

        for (final String id : collapsedIndexIds) {
            AssertCollector.assertFalse(String.format("Row %s should be collapsed", id),
                subscriptionPlanDetailPage.isEntitlementRowExpanded(id), assertionErrorList);
        }

        final int randItem2 = rand.nextInt(collapsedIndexIds.size());
        randOneTimeEntitlementId = collapsedIndexIds.get(randItem2);

        // Expand another entitlement row
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(randOneTimeEntitlementId);
        expandedIndexIds.add(randOneTimeEntitlementId);
        collapsedIndexIds.remove(randOneTimeEntitlementId);

        AssertCollector.assertTrue(String.format("Row %s should be expanded", randOneTimeEntitlementId),
            subscriptionPlanDetailPage.isEntitlementRowExpanded(randOneTimeEntitlementId), assertionErrorList);

        for (final String id : collapsedIndexIds) {
            AssertCollector.assertFalse(String.format("Row %s should be collapsed", id),
                subscriptionPlanDetailPage.isEntitlementRowExpanded(id), assertionErrorList);
        }

        // Collapse one entitlement row
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(randOneTimeEntitlementId);
        expandedIndexIds.remove(randOneTimeEntitlementId);
        collapsedIndexIds.add(randOneTimeEntitlementId);

        for (final String id : collapsedIndexIds) {
            AssertCollector.assertFalse(String.format("Row %s should be collapsed", id),
                subscriptionPlanDetailPage.isEntitlementRowExpanded(id), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the values of assignable and new date fields of a subscription plan when
     * feature flags are turned on
     *
     * @throws ParseException
     */
    @Test
    public void testAssignableAndDateFieldsInViewSubscriptionPlanWhenFeatureFlagTrue() throws ParseException {

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

        FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId);
        final String itemId = item.getId();

        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item1.getId();

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        // Add Feature Entitlement
        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        AssertCollector.assertThat("Checkbox is disabled for the non CSR item type",
            addSubscriptionPlanPage.getDisabledAttributeValueFromAssignableCheckbox(0), equalTo(null),
            assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(true, null, eolRenewalDate, eolImmediateDate, 0);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);
        AssertCollector.assertThat("Checkbox is not disabled for the non CSR item type",
            addSubscriptionPlanPage.getDisabledAttributeValueFromAssignableCheckbox(1), equalTo("true"),
            assertionErrorList);
        AssertCollector.assertFalse("Incorrect default value for the non CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(1), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, null, eolImmediateDate, 1);

        addSubscriptionPlanPage.clickOnSave(false);
        String planId = subscriptionPlanDetailPage.getId();

        final String newEntitlementId1 =
            DbUtils.getEntitlementIdFromItemId(planId, item.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, "true", PelicanConstants.HIPHEN, eolRenewalDate, eolImmediateDate, true,
            assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(planId, item1.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId2, "false", eosDate, PelicanConstants.HIPHEN, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editStatus(Status.ACTIVE);

        featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item2 = featureApiUtils.addFeature(null, null, null);
        final String itemId2 = item2.getId();

        final Item item3 = featureApiUtils.addFeature(null, null, null);
        final String itemId3 = item3.getId();

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId2, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 2);
        AssertCollector.assertThat("Checkbox is not disabled for the non CSR item type",
            addSubscriptionPlanPage.getDisabledAttributeValueFromAssignableCheckbox(2), equalTo("true"),
            assertionErrorList);
        AssertCollector.assertFalse("Incorrect default value for the non CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(2), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, null, 2);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId3, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 3);
        AssertCollector.assertThat("Checkbox is not disabled for the non CSR item type",
            addSubscriptionPlanPage.getDisabledAttributeValueFromAssignableCheckbox(3), equalTo("true"),
            assertionErrorList);
        AssertCollector.assertFalse("Incorrect default value for the non CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(3), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 3);

        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        planId = subscriptionPlanDetailPage.getId();

        final String newEntitlementId3 =
            DbUtils.getEntitlementIdFromItemId(planId, item2.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId3);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId3, "false", eosDate, eolRenewalDate, PelicanConstants.HIPHEN, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId3);

        final String newEntitlementId4 =
            DbUtils.getEntitlementIdFromItemId(planId, item3.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId4);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId4, "false", eosDate, eolRenewalDate, eolImmediateDate, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId4);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void testAssignableForCurrencyEntitlementsInViewSubscriptionPlanWhenFeatureFlagTrue() throws ParseException {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.NEW,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("100.00", PelicanConstants.CLOUD_CURRENCY_SELECT,
            0);

        addSubscriptionPlanPage.clickOnSave(false);
        final String planId = subscriptionPlanDetailPage.getId();

        final String newEntitlementId = DbUtils.getCurrencyEntitlementId(planId, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId, "false", "", "", "", false, assertionErrorList);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("200.00", PelicanConstants.CLOUD_CURRENCY_SELECT,
            1);
        addSubscriptionPlanPage.clickOnSave(false);

        final String newEntitlementId1 = DbUtils.getCurrencyEntitlementId(planId, getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, "false", "", "", "", false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the values of assignable and new date fields of a subscription plan when
     * feature flags are turned off
     *
     * @throws ParseException
     *
     */
    @Test
    public void testAssignableAndDateFieldsInViewSubscriptionPlanWhenFeatureFlagFalse() throws ParseException {

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

        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item1.getId();

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);

        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        final String planId = subscriptionPlanDetailPage.getId();

        final String newEntitlementId1 =
            DbUtils.getEntitlementIdFromItemId(planId, item.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId1, null, null, null, null, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId1);

        final String newEntitlementId2 =
            DbUtils.getEntitlementIdFromItemId(planId, item1.getId(), getEnvironmentVariables());
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);
        AssertionsForViewSubscriptionPlanPage.assertCollapsableEntitlementFields(subscriptionPlanDetailPage,
            newEntitlementId2, null, null, null, null, true, assertionErrorList);
        subscriptionPlanDetailPage.clickOnEntitlementExpandableRowToggle(newEntitlementId2);

        AssertCollector.assertAll(assertionErrorList);
    }
}
