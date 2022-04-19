package com.autodesk.bsm.pelican.ui.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanGenericPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SubscriptionPlanWarningPopUpTest extends SeleniumWebdriver {

    private static SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private static String productLineNameAndExternalKey;
    private static AddSubscriptionPlanPage addSubscriptionPlanPage;
    private static EditSubscriptionPlanPage editSubscriptionPlanPage;
    private static SubscriptionPlanGenericPage subscriptionPlanGenericPage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);

        // Add product Line name + external key
        productLineNameAndExternalKey = getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";

        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        subscriptionPlanGenericPage = adminToolPage.getPage(SubscriptionPlanGenericPage.class);
    }

    /**
     * This is a method which will test the presence of warning popup in new status
     *
     */
    @Test
    public void testAddFeaturePopUpInNewStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();

        final String newEntitlementId =
            DbUtils.getEntitlementIdFromItemId(subscriptionPlanId, itemId, getEnvironmentVariables());

        // Verify audit log data for all subscription plan
        final Boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(
            subscriptionPlanId, null, newEntitlementId, Action.CREATE, false, assertionErrorList);

        AssertCollector.assertTrue("Feature update is not found in the dynamo db for plan1", isAuditLogFound,
            assertionErrorList);

        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which will test the presence of warning popup and validate the warning pop up message in active
     * status
     *
     */
    @Test
    public void testAddFeaturePopUpInActiveStatus() {

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
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        String warnMessage = subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        if (StringUtils.isNotEmpty(warnMessage)) {
            warnMessage = warnMessage.split(PelicanConstants.SPLITTER)[0];
        }

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();
        final String expectedMessage = PelicanConstants.PRE_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE + PelicanConstants.ADD_FEATURE_POPUP_EXPECTED_MESSAGE
            + item.getName() + " (" + item.getExternalKey() + ")";

        AssertCollector.assertThat("Warning popup message is incorrect", warnMessage, equalTo(expectedMessage),
            assertionErrorList);
        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);

        final String newEntitlementId =
            DbUtils.getEntitlementIdFromItemId(subscriptionPlanId, itemId, getEnvironmentVariables());

        // Verify audit log data for all subscription plan
        final Boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(
            subscriptionPlanId, null, newEntitlementId, Action.CREATE, false, assertionErrorList);

        AssertCollector.assertTrue("Feature update is not found in the dynamo db for plan1", isAuditLogFound,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which will test the presence of warning popup and validate the warning pop up message in active
     * status in edit subscription plan page
     *
     */
    @Test
    public void testAddFeaturePopUpInActiveStatusInEditSubscriptionPlanStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);
        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        String warnMessage = subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        if (StringUtils.isNotEmpty(warnMessage)) {
            warnMessage = warnMessage.split(PelicanConstants.SPLITTER)[0];
        }

        final String subscriptionPlanId = subscriptionPlanDetailPage.getId();
        final String expectedMessage = PelicanConstants.PRE_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE + PelicanConstants.ADD_FEATURE_POPUP_EXPECTED_MESSAGE
            + item.getName() + " (" + item.getExternalKey() + ")";

        final String newEntitlementId =
            DbUtils.getEntitlementIdFromItemId(subscriptionPlanId, itemId, getEnvironmentVariables());

        // Verify audit log data for all subscription plan
        final Boolean isAuditLogFound = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(
            subscriptionPlanId, null, newEntitlementId, Action.UPDATE, false, assertionErrorList);

        AssertCollector.assertTrue("Feature update is not found in the dynamo db for plan1", isAuditLogFound,
            assertionErrorList);

        AssertCollector.assertThat("Warning popup message is incorrect", warnMessage, equalTo(expectedMessage),
            assertionErrorList);
        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which will test the presence of warning popup and test the cancel button on the popup
     *
     */
    @Test
    public void testCancelFeaturePopUpInActiveStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, PelicanConstants.OFFERING_DETAIL, productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        String warnMessage = subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(false);
        if (StringUtils.isNotEmpty(warnMessage)) {
            warnMessage = warnMessage.split(PelicanConstants.SPLITTER)[0];
        }

        final String expectedMessage = PelicanConstants.PRE_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE + PelicanConstants.ADD_FEATURE_POPUP_EXPECTED_MESSAGE
            + item.getName() + " (" + item.getExternalKey() + ")";

        AssertCollector.assertThat("Warning popup message is incorrect", warnMessage, equalTo(expectedMessage),
            assertionErrorList);
        AssertCollector.assertTrue("Cancel button on popup didn't land on the editable page",
            addSubscriptionPlanPage.isNameInputFieldPresent(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which will test the presence of warning popup and test the cancel button on the popup in the
     * edit subscription plan flow
     *
     */
    @Test
    public void testCancelFeaturePopUpInActiveStatusInEditSubscriptionPlanStatus() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);
        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(false);

        subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        String warnMessage = subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(false);
        if (StringUtils.isNotEmpty(warnMessage)) {
            warnMessage = warnMessage.split(PelicanConstants.SPLITTER)[0];
        }

        final String expectedMessage = PelicanConstants.PRE_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE + PelicanConstants.ADD_FEATURE_POPUP_EXPECTED_MESSAGE
            + item.getName() + " (" + item.getExternalKey() + ")";

        AssertCollector.assertThat("Warning popup message is incorrect", warnMessage, equalTo(expectedMessage),
            assertionErrorList);
        AssertCollector.assertTrue("Cancel button on popup didn't land on the editable page",
            addSubscriptionPlanPage.isNameInputFieldPresent(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which will test the presence of warning popup when changing the feature id
     *
     */
    @Test
    public void testAddFeaturePopUpWhileChangingFeature() {

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlanPage.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        Item item = featureApiUtils.addFeature(null, null, null);
        String itemId = item.getId();
        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        String subscriptionPlanId = subscriptionPlanDetailPage.getId();

        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);

        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();

        editSubscriptionPlanPage.editSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)", productLineNameAndExternalKey, SupportLevel.BASIC, null,
            true);

        item = featureApiUtils.addFeature(null, null, null);
        itemId = item.getId();

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, null, null, 0);

        // Click on Save
        addSubscriptionPlanPage.clickOnSave();
        String warnMessage = subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);
        if (StringUtils.isNotEmpty(warnMessage)) {
            warnMessage = warnMessage.split(PelicanConstants.SPLITTER)[0];
        }

        subscriptionPlanId = subscriptionPlanDetailPage.getId();
        final String expectedMessage = PelicanConstants.PRE_SUB_PLAN_BLOCK_MESSAGE
            + DbUtils.getMidasHiveValue(PelicanDbConstants.BLOCKING_FEATURE_COMPOSITION_CHANGE_TIME_IN_HRS,
                getEnvironmentVariables())
            + PelicanConstants.POST_SUB_PLAN_BLOCK_MESSAGE + PelicanConstants.ADD_FEATURE_POPUP_EXPECTED_MESSAGE
            + item.getName() + " (" + item.getExternalKey() + ")";

        AssertCollector.assertThat("Warning popup message is incorrect", warnMessage, equalTo(expectedMessage),
            assertionErrorList);
        AssertCollector.assertThat("Plan id should not be null", subscriptionPlanId, notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
