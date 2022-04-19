package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStrategy;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentResponse.FulfillmentResponseResult;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentResponse.FulfillmentResponseType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.helper.auditlog.PurchaseOrderAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.HashMap;
import java.util.List;

/**
 * As a part of BIC to SAP, Retrigger mechanism is getting removed for Pending or Failed Meta Orders. Need to Update all
 * test cases once its live. When Feature flag STOP_LEGACY_FULFILLMENT_CALL is ON , Retrigger Button is not visible When
 * Feature flag STOP_LEGACY_FULFILLMENT_CALL is OFF, EBSO and GCSO can see Retrigger button.
 *
 * @author Vaibhavi Joshi
 */
public class LegacyFulfillmentTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String purchaseOrderIdForMeta;
    private UserUtils userUtils;
    private RolesHelper rolesHelper;
    private HashMap<String, String> userParams;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isAuditLogFound = false;
    private boolean isFeatureFlagChanged = false;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        rolesHelper = new RolesHelper(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Create user
        userParams = new HashMap<>();
        userUtils = new UserUtils();
        // Login to Admin Page.
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged = bankingConfigurationPropertiesPage
            .setFeatureFlag(PelicanConstants.STOP_LEGACY_FULFILLMENT_CALL_FLAG, false);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.STOP_LEGACY_FULFILLMENT_CALL_FLAG, true);
        }
    }

    /**
     * Test to verify that GCSO user is able to see retrigger button for order that have legacy fulfillment in failed
     * status.
     */
    @Test
    public void testRetriggerButtonIsVisibleForFailedOrderForGCSO() {

        purchaseOrderIdForMeta = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaYearlyUsPriceId(), buyerUser, 3).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMeta);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMeta);

        adminToolPage.login();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.GCSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final User gcsoUser =
            userUtils.createAssignRoleAndLoginUser(
                userParams, Lists.newArrayList(Role.GCSO.getValue(), Role.BANKING_ADMIN.getValue(),
                    Role.ADMIN.getValue(), Role.APPLICATION_MANAGER.getValue()),
                adminToolPage, getEnvironmentVariables());

        // getting purchase order page with failed
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMeta);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfilment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1), equalTo(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for GCSO role when fulfillment status is failed",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        if (purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString())) {
            // Click retrigger button.
            purchaseOrderDetailPage.clickRetrigger(FulFillmentStrategy.LEGACY.toString());

            // Verify the Update Purchase order audit data
            isAuditLogFound = PurchaseOrderAuditLogHelper.helperToValidateDynamoDbForPurchaseOrder(
                purchaseOrderIdForMeta, Action.UPDATE, gcsoUser.getId(), assertionErrorList);
            AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify that Non GCSO user is able to see retrigger button for order that have legacy fulfillment in
     * failed status.
     */
    @Test
    public void testRetriggerButtonIsVisibleForFailedOrderForNonGCSO() {

        purchaseOrderIdForMeta = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaYearlyUsPriceId(), buyerUser, 3).getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMeta);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMeta);

        adminToolPage.login();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_GCSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final List<String> nonGcsoRoleList = rolesHelper.getNonGcsoRoleList();
        userUtils.createAssignRoleAndLoginUser(userParams, nonGcsoRoleList, adminToolPage, getEnvironmentVariables());

        // getting purchase order page with failed

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMeta);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfilment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1), equalTo(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        // Check Retrigger button is visible for all user except Gcso.
        AssertCollector.assertTrue("Retriger Button is not Visible for Non GCSO role when fulfillment status is failed",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify that EBSO user is able to see retrigger button for order that have legacy fulfillment in pending
     * status.
     */
    @Test
    public void testRetriggerButtonIsVisibleForPendingOrderForEBSO() {

        purchaseOrderIdForMeta = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaYearlyUsPriceId(), buyerUser, 3).getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMeta);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMeta);

        // Manually changing status to Pending for failed order.
        purchaseOrderUtils.updatePurchaseOrderStatus(purchaseOrderIdForMeta);
        purchaseOrderUtils.updateFulfillmentGroupStatus(purchaseOrderIdForMeta, FulFillmentStatus.PENDING, 1);

        // creating and logging as ebso user
        adminToolPage.login();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final User ebsoUser =
            userUtils.createAssignRoleAndLoginUser(
                userParams, Lists.newArrayList(Role.EBSO.getValue(), Role.BANKING_ADMIN.getValue(),
                    Role.ADMIN.getValue(), Role.APPLICATION_MANAGER.getValue()),
                adminToolPage, getEnvironmentVariables());

        // getting purchase order page with pending

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMeta);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfilment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1), equalTo(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.PENDING.toString()),
            assertionErrorList);

        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for EBSO role when fulfillment status is pending",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        if (purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString())) {
            // Click retrigger button.
            purchaseOrderDetailPage.clickRetrigger(FulFillmentStrategy.LEGACY.toString());

            // Verify the Update Purchase order audit data
            isAuditLogFound = PurchaseOrderAuditLogHelper.helperToValidateDynamoDbForPurchaseOrder(
                purchaseOrderIdForMeta, Action.UPDATE, ebsoUser.getId(), assertionErrorList);
            AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);

        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify that Non EBSO user is not able to see retrigger button for order that have legacy fulfillment in
     * pending status.
     */
    @Test
    public void testRetriggerButtonIsNotVisibleForPendingOrderForNonEBSO() {

        purchaseOrderIdForMeta = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaYearlyUsPriceId(), buyerUser, 3).getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMeta);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMeta);

        // Manually changing status to Pending for failed order.
        purchaseOrderUtils.updatePurchaseOrderStatus(purchaseOrderIdForMeta);

        // Manually changing legacy fulfillment status to Pending for failed
        // order.
        purchaseOrderUtils.updateFulfillmentGroupStatus(purchaseOrderIdForMeta, FulFillmentStatus.PENDING, 1);

        Util.waitInSeconds(TimeConstants.THREE_SEC);

        adminToolPage.login();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        // Log in as a non-ebso user
        final List<String> nonEbsoRoleList = rolesHelper.getNonEbsoRoleList();
        userUtils.createAssignRoleAndLoginUser(userParams, nonEbsoRoleList, adminToolPage, getEnvironmentVariables());

        // getting purchase order page with pending

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMeta);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfilment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1), equalTo(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.PENDING.toString()),
            assertionErrorList);

        // Check Retrigger button is not visible.
        AssertCollector.assertFalse("Retriger Button is Visible for Non EBSO role when fulfillment status is pending",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify on processing Meta Orders for CHARGE sends TRIGGERS_ASYNC_REQUEST and LEGACY_ASYNC_REQUEST
     * request for Fulfillment.
     */
    @Test
    public void testProcessingMetaOrderForChargeSendsFulfillmentRequest() {

        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaMonthlyUsPriceId(), buyerUser, 1)
            .getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        Util.waitInSeconds(TimeConstants.ONE_SEC);
        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailpage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertTrue("Fulfillment Status should be FAILED at Order",
            purchaseOrderDetailpage.getFulfillmentStatus().equals(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        AssertCollector.assertTrue("Fulfillment Strategy Should be present for Legacy",
            purchaseOrderDetailpage.getFulfillmentGroupStrategy(1).equals(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        AssertCollector.assertTrue("Fulfillment Status should be FAILED at Fulfillment Group",
            purchaseOrderDetailpage.getFulfillmentGroupStatus(1).equals(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        // Assert on TRIGGERS_ASYNC_REQUEST and LEGACY_ASYNC_REQUEST.
        AssertCollector.assertThat("Fulfillment First Response Type is incorrect",
            purchaseOrderDetailpage.getFulfillmentResponsesType(1),
            equalTo(FulfillmentResponseType.TRIGGERS_ASYNC_REQUEST.getResponseType()), assertionErrorList);
        AssertCollector.assertThat("Fulfillment Second Response Type is incorrect",
            purchaseOrderDetailpage.getFulfillmentResponsesType(2),
            equalTo(FulfillmentResponseType.LEGACY_ASYNC_REQUEST.getResponseType()), assertionErrorList);

        // Fulfillment call back for Meta Order.
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrderDetailpage.refreshPage();
        // On Fulfillment call back, it adds Fulfillment Response.
        AssertCollector.assertThat("Fulfillment Response Type on call back is incorrect",
            purchaseOrderDetailpage.getFulfillmentResponsesType(3),
            equalTo(FulfillmentResponseType.LEGACY_ASYNC_NOTIFICATION.getResponseType()), assertionErrorList);
        AssertCollector.assertThat("Fulfillment Response Result should be present",
            purchaseOrderDetailpage.getFulfillmentResponsesResult(3),
            equalTo(FulfillmentResponseResult.SUCCEEDED.getResponseResult()), assertionErrorList);

        AssertCollector.assertThat("There should not be more than Three Entires ",
            purchaseOrderDetailpage.getFulfillmentResponsesGridSize(), is(3), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
