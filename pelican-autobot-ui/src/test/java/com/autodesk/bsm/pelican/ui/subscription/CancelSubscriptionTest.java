package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionEventType;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.CancelSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Cancel Subscription Test
 *
 * @author vineel
 */
public class CancelSubscriptionTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static String subscriptionId;
    private static final int quantity = 1;
    private FindSubscriptionsPage findSubscriptionsPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private CancelSubscriptionPage cancelSubscriptionPage;
    private String requestor;
    private AdminToolPage adminToolPage;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        requestor = getEnvironmentVariables().getUserName() + " (" + getEnvironmentVariables().getUserId() + ")";
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This is a test case which selects the cancellation policy as cancel immediately and cancels a subscription in the
     * admin tool.
     */
    @Test
    public void cancelASubscriptionImmediately() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.IMMEDIATE_NO_REFUND, null);

        final Subscription expectedSubscription = resource.subscription().getById(subscriptionId);
        final com.autodesk.bsm.pelican.ui.entities.Subscription actualSubscription =
            findSubscriptionsPage.assignAllFieldsToSubscription();
        AssertCollector.assertThat("Incorrect subscription id", actualSubscription.getId(), equalTo(subscriptionId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer of the subscription", actualSubscription.getUserName(),
            equalTo(buyerUser.getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect quantity of the subscription", actualSubscription.getQuantity(),
            equalTo(String.valueOf(quantity)), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the subscription", actualSubscription.getStatus(),
            equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect boolean value for auto-renewed", actualSubscription.getAutoRenew(),
            equalTo(String.valueOf(false)), assertionErrorList);
        AssertCollector.assertThat("Incorrect credit days discount for a subscription",
            actualSubscription.getCreditDays(), equalTo(String.valueOf(0)), assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing price id of a subscription",
            actualSubscription.getNextBillingPriceId(), equalTo(getBicYearlyUsPriceId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect expiration date of a subscription",
            actualSubscription.getExpirationDate(), equalTo(expectedSubscription.getExpirationDate()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing date of a subscription",
            actualSubscription.getNextBillingDate(), equalTo(null), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests subscription can be cancelled with GDPR immediate cancel option when ENABLE_GDPR feature flag
     * is ON. Audit Log is also tested as part of this test case. Also tests, user with GCSO role can cancel the
     * subscription with this option. Verify that 3 cancel options are available when Subscription is in Active status.
     */
    @Test
    public void testGDPRCancelSubscriptionOnActiveSubscription() {

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // Get Subscription Id
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Find the subscription in Admin Tool and cancel with GDPR option
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();

        // Assertions to make sure all 3 cancel options are available for Active Subscription
        AssertCollector.assertTrue("Cancel At the end of billing period option should displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD),
            assertionErrorList);
        AssertCollector.assertTrue("Cancel Immediately option should displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.IMMEDIATE_NO_REFUND),
            assertionErrorList);
        AssertCollector.assertTrue("GDPR cancel option should be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY),
            assertionErrorList);

        final String cancelNote = "GDPR cancel request";
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY, cancelNote);

        commonAssertionsForExpiredSubscriptions();

        // verify audit log
        final boolean isAuditLogFound =
            SubscriptionAuditLogHelper.helperToQueryDynamoDbForEditSubscription(subscriptionId, null, null, null,
                DateTimeUtils.getAuditLogDate(subscriptionDetailPage.getExpirationDate(), false),
                Status.ACTIVE.toString(), Status.EXPIRED.toString(), assertionErrorList);
        AssertCollector.assertTrue("Audit log is not found for cancelling the subscription id : " + subscriptionId,
            isAuditLogFound, assertionErrorList);

        final int lastActivityIndex = subscriptionDetailPage.getNumberOfSubscriptionActivity();
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.GDPR_CANCEL, requestor, lastActivityIndex - 2,
            cancelNote);
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.EXPIRED, requestor, lastActivityIndex - 1, "");

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test User with GCSO Role can see "Cancel at end of Billing Period" and "Cancel Immediately" cancellation policy.
     */
    @Test
    public void testGDPRCancelImmediatelyNotVisibleToGCSORole() {

        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.GCSO_ONLY_USER);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        final RolesHelper rolesHelper = new RolesHelper(getEnvironmentVariables());
        List<String> requiredRoleList = new ArrayList<>();
        requiredRoleList = rolesHelper.getGCSOOnlyRoleList();

        // Create new user, assign GCSO role and log in with that user.
        final UserUtils userUtils = new UserUtils();
        userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage, getEnvironmentVariables());

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // Get Subscription Id
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Find the subscription in Admin Tool and cancel with GDPR option
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();

        // Assertions to validate two cancel options are visible only.
        AssertCollector.assertTrue("Cancel At the end of billing period option should be visible",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD),
            assertionErrorList);
        AssertCollector.assertTrue("Cancel Immediately option should be visible",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.IMMEDIATE_NO_REFUND),
            assertionErrorList);
        AssertCollector.assertFalse("GDPR cancel option should not be visible",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY),
            assertionErrorList);

        final String cancelNote = "Cancel Immediate";
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.IMMEDIATE_NO_REFUND, cancelNote);

        commonAssertionsForExpiredSubscriptions();

        // Logout from user with GCSO permission
        adminToolPage.logout();
        // Login with original user
        adminToolPage.login();

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR User can see only One cancellation Policy.
     */
    @Test
    public void testGDPRCancelImmediatelyIsOnlyVisibleToGDPRRole() {

        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.GDPR_ONLY_USER);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        final RolesHelper rolesHelper = new RolesHelper(getEnvironmentVariables());
        List<String> requiredRoleList = new ArrayList<>();

        requiredRoleList = rolesHelper.getGDPROnlyRoleList();
        requiredRoleList.addAll(rolesHelper.getReadOnlyRoleList());

        // Create a new user, assign role and log in with that user.
        final UserUtils userUtils = new UserUtils();
        userUtils.createAssignRoleAndLoginUser(userParams, requiredRoleList, adminToolPage, getEnvironmentVariables());

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // Get Active Subscription Id
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Find the subscription in Admin Tool and cancel with GDPR option
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();

        // Assertions to validate only "GDPR Cancel Immediately" option visible to user.
        AssertCollector.assertFalse("Cancel At the end of billing period option should not be visible",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD),
            assertionErrorList);
        AssertCollector.assertFalse("Cancel Immediately option should not be visible",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.IMMEDIATE_NO_REFUND),
            assertionErrorList);
        AssertCollector.assertTrue("GDPR cancel option should be visible",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY),
            assertionErrorList);

        final String cancelNote = "GDPR Cancel";
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY, cancelNote);

        commonAssertionsForExpiredSubscriptions();

        // Logout user with GDPR Role and permission
        adminToolPage.logout();
        // Login with original user
        adminToolPage.login();

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR cancel option is only available if subscription is already cancelled.
     */
    @Test
    public void testGDPRCancelOnCanceledSubscription() throws ParseException {

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getMetaMonthlyUkPriceId(), 1);

        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // Get Subscription Id
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Find the subscription in Admin Tool and cancel with GDPR option
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();

        // Choose Cancel At the end of billing option to cancel a subscription
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null);

        // Assertions on CANCELLED subscription
        AssertCollector.assertThat("Incorrect Subscription Status of subscription id: " + subscriptionId,
            subscriptionDetailPage.getStatus(), equalTo(Status.CANCELLED.toString()), assertionErrorList);
        AssertCollector.assertThat("Auto renew should be false for subscription id: " + subscriptionId,
            subscriptionDetailPage.getAutoRenewEnabled(), equalToIgnoringCase(PelicanConstants.FALSE),
            assertionErrorList);
        final String nextBillingDate =
            DateTimeUtils.getNextBillingDate(DateTimeUtils.getCurrentDate(), BillingFrequency.MONTH.toString());
        AssertCollector.assertThat("Incorrect Next Billing date for subscription id: " + subscriptionId,
            subscriptionDetailPage.getNextBillingDate(), equalTo(nextBillingDate), assertionErrorList);
        AssertCollector.assertThat("Next Billing Charge for subscription id: " + subscriptionId,
            subscriptionDetailPage.getNextBillingCharge(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Expiration Date for subscription id: " + subscriptionId,
            subscriptionDetailPage.getExpirationDate().split("\\s+")[0], equalTo(nextBillingDate), assertionErrorList);

        final String expirationDate = DateTimeUtils.getAuditLogDate(subscriptionDetailPage.getExpirationDate(), false);
        // verify audit log for cancelled subscription
        final boolean isSubscriptionCancelAuditLogFound =
            SubscriptionAuditLogHelper.helperToQueryDynamoDbForEditSubscription(subscriptionId, null, null, null,
                expirationDate, Status.ACTIVE.toString(), Status.CANCELLED.toString(), assertionErrorList);
        AssertCollector.assertTrue("Audit log is not found for expiring the subscription id : " + subscriptionId,
            isSubscriptionCancelAuditLogFound, assertionErrorList);

        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();

        // Assertions to make sure only GDPR cancel option is available and other 2 are not available
        AssertCollector.assertFalse("Cancel At the end of billing period option should not be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD),
            assertionErrorList);
        AssertCollector.assertFalse("Cancel Immediately option should not be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.IMMEDIATE_NO_REFUND),
            assertionErrorList);
        AssertCollector.assertTrue("GDPR cancel option should be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY),
            assertionErrorList);

        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY, null);

        // Assertions on GDPR EXPIRED subscription
        commonAssertionsForExpiredSubscriptions();

        final int lastActivityIndex = subscriptionDetailPage.getNumberOfSubscriptionActivity();
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.CANCEL, requestor, lastActivityIndex - 3, "");
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.GDPR_CANCEL, requestor, lastActivityIndex - 2,
            "");
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.EXPIRED, requestor, lastActivityIndex - 1, "");

        // verify audit log for expired subscription
        final boolean isSubscriptionExpireAuditLogFound =
            SubscriptionAuditLogHelper.helperToQueryDynamoDbForEditSubscription(subscriptionId, null, null,
                expirationDate, DateTimeUtils.getAuditLogDate(subscriptionDetailPage.getExpirationDate(), false),
                Status.CANCELLED.toString(), Status.EXPIRED.toString(), assertionErrorList);
        AssertCollector.assertTrue("Audit log is not found for expiring the subscription id : " + subscriptionId,
            isSubscriptionExpireAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test GDPR cancel option is available along with other 2 options, if subscription is in delinquent status.
     */
    @Test
    public void testGDPRCancelOnDelinquentSubscription() {

        // Submit a purchase order
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUkPriceId(), 1);

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        // Get Subscription Id
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscriptionId);

        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(
            subscriptionIds, false, PaymentType.CREDIT_CARD, null, true, buyerUser);
        final String renewalPurchaseOrderIdForBicCreditCard = renewalPurchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderIdForBicCreditCard);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderIdForBicCreditCard);

        // Find the subscription in Admin Tool and cancel with GDPR option
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);

        AssertCollector.assertThat("Incorrect subscription status", subscriptionDetailPage.getStatus(),
            equalTo(Status.DELINQUENT.toString()), assertionErrorList);

        cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();

        // Assertions to make sure all 3 cancel options are available for Delinquent Subscription
        AssertCollector.assertTrue("Cancel At the end of billing period option should not be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD),
            assertionErrorList);
        AssertCollector.assertTrue("Cancel Immediately option should not be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.IMMEDIATE_NO_REFUND),
            assertionErrorList);
        AssertCollector.assertTrue("GDPR cancel option should be displayed",
            cancelSubscriptionPage.isRequiredCancelOptionDisplayed(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY),
            assertionErrorList);

        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.GDPR_CANCEL_IMMEDIATELY, null);

        // Assertions on GDPR EXPIRED subscription
        commonAssertionsForExpiredSubscriptions();
        final int lastActivityIndex = subscriptionDetailPage.getNumberOfSubscriptionActivity();
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.GDPR_CANCEL, requestor, lastActivityIndex - 2,
            "");
        commonAssertionsForSubscriptionActivity(SubscriptionEventType.EXPIRED, requestor, lastActivityIndex - 1, "");

        // verify audit log
        final boolean isAuditLogFound =
            SubscriptionAuditLogHelper.helperToQueryDynamoDbForEditSubscription(subscriptionId, null, null, null,
                DateTimeUtils.getAuditLogDate(subscriptionDetailPage.getExpirationDate(), false),
                Status.DELINQUENT.toString(), Status.EXPIRED.toString(), assertionErrorList);
        AssertCollector.assertTrue("Audit log is not found for cancelling the subscription id : " + subscriptionId,
            isAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    private void commonAssertionsForExpiredSubscriptions() {

        AssertCollector.assertThat("Incorrect Subscription Status of subscription id: " + subscriptionId,
            subscriptionDetailPage.getStatus(), equalTo(Status.EXPIRED.toString()), assertionErrorList);
        AssertCollector.assertThat("Auto renew should be false for subscription id: " + subscriptionId,
            subscriptionDetailPage.getAutoRenewEnabled(), equalToIgnoringCase(PelicanConstants.FALSE),
            assertionErrorList);
        AssertCollector.assertThat("Next Billing date should be empty for subscription id: " + subscriptionId,
            subscriptionDetailPage.getNextBillingDate(), equalTo(PelicanConstants.HIPHEN), assertionErrorList);
        AssertCollector.assertThat("Next Billing Charge should be empty for subscription id: " + subscriptionId,
            subscriptionDetailPage.getNextBillingCharge(), equalTo(PelicanConstants.HIPHEN), assertionErrorList);
        AssertCollector.assertThat("Expiration Date should NOT be empty for subscription id: " + subscriptionId,
            subscriptionDetailPage.getExpirationDate().split("\\s+")[0],
            equalTo(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
    }

    private void commonAssertionsForSubscriptionActivity(final SubscriptionEventType event, final String requestor,
        final int index, final String memo) {
        AssertCollector.assertThat("Incorrect activity for " + event,
            subscriptionDetailPage.getSubscriptionActivity(index).get(0).getActivity(), equalTo(event.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect requestor for " + event,
            subscriptionDetailPage.getSubscriptionActivity(index).get(0).getRequestor(), equalTo(requestor),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect GDPR cancel memo",
            subscriptionDetailPage.getSubscriptionActivity(index).get(0).getMemo(), equalTo(memo), assertionErrorList);
    }
}
