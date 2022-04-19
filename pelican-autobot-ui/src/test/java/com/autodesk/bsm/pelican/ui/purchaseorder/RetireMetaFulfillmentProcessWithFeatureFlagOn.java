package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.basicoffering.Currency;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStrategy;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentResponse.FulfillmentResponseResult;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentResponse.FulfillmentResponseType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.AddBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Test class to cover Retiring Meta Fulfillment Process which removes Retrigger Functionality.
 *
 * @author t_joshv
 *
 */

public class RetireMetaFulfillmentProcessWithFeatureFlagOn extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String purchaseOrderIdForMeta;
    private UserUtils userUtils;
    private HashMap<String, String> userParams;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged = false;
    private String priceIdForCloudCredit;
    private BuyerUser nonExistingBuyerUser;
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyFulfillmentTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        // HashMap for user
        userParams = new HashMap<>();
        userUtils = new UserUtils();
        // Login to Admin Page.
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPurchaseOrdersPage = adminToolPage.getPage(FindPurchaseOrdersPage.class);
        final AddBasicOfferingPage addBasicOfferingPage = adminToolPage.getPage(AddBasicOfferingPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.STOP_LEGACY_FULFILLMENT_CALL_FLAG, true);

        // Add currency and amount only when Offering Type is "Currency"
        final Currency currency = resource.currency().getById(getEnvironmentVariables().getCloudCurrencyId());
        final String currencyName = currency.getDescription() + " (" + currency.getName() + ")";
        final String amount = "100.00";

        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.CURRENCY,
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")",
            "CloudCredit" + RandomStringUtils.randomAlphanumeric(5),
            "CloudCreditName" + RandomStringUtils.randomAlphanumeric(5), MediaType.ELECTRONIC_DOWNLOAD, null,
            Status.ACTIVE, UsageType.COM, currencyName, amount);

        // Create subscription price
        addBasicOfferingPage.addPrices(1, getStoreUs().getName(),
            getStoreUs().getIncluded().getPriceLists().get(0).getName(), "10", DateTimeUtils.getNowPlusDays(0),
            DateTimeUtils.getNowPlusDays(2));
        final BasicOfferingDetailPage basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // get PriceId for Cloud Credit
        priceIdForCloudCredit = basicOfferingDetailPage.getFirstPriceId();

        // User Params for Non Oxygen User.
        final UserUtils userUtils = new UserUtils();
        nonExistingBuyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.STOP_LEGACY_FULFILLMENT_CALL_FLAG,
                false);
        }
    }

    /**
     * Test to verify that GCSO user is Not able to see retrigger button for order that have legacy fulfillment in
     * failed status with Feature Flag STOP_LEGACY_FULFILLMENT_CALL ON.
     */
    @Test
    public void testRetriggerButtonIsNotVisibleForFailedMetaOrderForGCSO() {

        purchaseOrderIdForMeta = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaYearlyUsPriceId(), getBuyerUser(), 3)
            .getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForMeta);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForMeta);

        adminToolPage.login();
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.GCSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        // login with GCSO user.
        userUtils
            .createAssignRoleAndLoginUser(
                userParams, Lists.newArrayList(Role.GCSO.getValue(), Role.BANKING_ADMIN.getValue(),
                    Role.ADMIN.getValue(), Role.APPLICATION_MANAGER.getValue()),
                adminToolPage, getEnvironmentVariables());

        // getting purchase order page with failed
        purchaseOrderUtils.updateFulfillmentGroupStatus(purchaseOrderIdForMeta, FulFillmentStatus.FAILED, 1);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForMeta);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfillment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1), equalTo(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        // Check Retrigger button is visible.
        AssertCollector.assertFalse(
            "Retriger Button should not Visible for GCSO role when fulfillment status is failed",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify that EBSO user is not able to see retrigger button for order that have legacy fulfillment in
     * pending status with Feature flag STOP_LEGACY_FULFILLMENT_CALL ON.
     */
    @Test
    public void testRetriggerButtonIsNotVisibleForPendingOrderForEBSOWithStopLegacyFulfillmentCallOn() {

        purchaseOrderIdForMeta = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getMetaYearlyUsPriceId(), getBuyerUser(), 3)
            .getId();

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
        // Login with EBSO User.
        userUtils
            .createAssignRoleAndLoginUser(
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
        AssertCollector.assertFalse(
            "Retriger Button should not Visible for EBSO role when fulfillment status is pending",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Retrigger Button is not visible for Failed Meta Order While its visible for Failed Cloud Credits.
     */
    @Test

    public void testRetriggerButtonNotVisibleForFailedLegacyOrder() {

        final LinkedHashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 1);
        priceQuantityMap.put(priceIdForCloudCredit, 1);

        final PurchaseOrder purchaseOrderForLegacyAndCloudCredit = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, nonExistingBuyerUser);

        final String purchaseOrderForLegacyAndCloudCreditId = purchaseOrderForLegacyAndCloudCredit.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForLegacyAndCloudCreditId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForLegacyAndCloudCreditId);
        LOGGER.info("Purchase Order Id: " + purchaseOrderForLegacyAndCloudCredit.getId());

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderForLegacyAndCloudCreditId);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Identify the Index for cloud credit strategy and legacy startegy.
        final int fulfilllmentIndexForCouldCrdit =
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1).equals(FulFillmentStrategy.CLOUD_CREDITS.toString())
                ? 1
                : 2;
        final int fulfillmentIndexForLegacy = fulfilllmentIndexForCouldCrdit == 1 ? 2 : 1;

        purchaseOrderUtils.updateFulfillmentGroupStatus(purchaseOrderForLegacyAndCloudCreditId,
            FulFillmentStatus.FAILED, fulfillmentIndexForLegacy);

        purchaseOrderDetailPage.refreshPage();

        // Check that Fulfilment status and strategy for LEGACY.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(fulfillmentIndexForLegacy),
            equalTo(FulFillmentStrategy.LEGACY.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status for Legacy",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(fulfillmentIndexForLegacy),
            equalTo(FulFillmentStatus.FAILED.toString()), assertionErrorList);

        // Check Retrigger button is visible.
        AssertCollector.assertFalse("Retriger Button should not be Visible for FAILED Legacy Fulfillment status",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        // Check that Fulfilment status and strategy for Cloud Credit.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(fulfilllmentIndexForCouldCrdit),
            equalTo(FulFillmentStrategy.CLOUD_CREDITS.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status for Cloud Credits",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(fulfilllmentIndexForCouldCrdit),
            equalTo(FulFillmentStatus.FAILED.toString()), assertionErrorList);
        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for FAILED Cloud Credit Fulfillment status",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.CLOUD_CREDITS.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify on processing Meta Orders for CHARGE Does not send TRIGGERS_ASYNC_REQUEST and
     * LEGACY_ASYNC_REQUEST request for Fulfillment.
     */
    @Test
    public void testProcessingMetaOrderForChargeWithSuppressedFulfillmentRequest() {

        final LinkedHashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 1);
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        priceQuantityMap.put(priceIdForCloudCredit, 1);

        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, nonExistingBuyerUser);

        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderId);
        final PurchaseOrderDetailPage purchaseOrderDetailpage = findPurchaseOrdersPage.clickOnSubmit();
        AssertCollector.assertTrue("Fulfillment Status should be PENDING at Order",
            purchaseOrderDetailpage.getFulfillmentStatus().equals(FulFillmentStatus.PENDING.toString()),
            assertionErrorList);
        // Iterate over each Fulfillment Group and validate Fulfillment Status for Legacy.
        for (int i = 1; i <= 3; i++) {
            if (purchaseOrderDetailpage.getFulfillmentGroupStrategy(i).equals(FulFillmentStrategy.LEGACY.toString())) {
                AssertCollector.assertTrue("Fulfillment Status should be PENDING at Fulfillment Group",
                    purchaseOrderDetailpage.getFulfillmentGroupStatus(i).equals(FulFillmentStatus.PENDING.toString()),
                    assertionErrorList);
                break;
            }
        }
        // On PO charge, under Fulfillment Response only Cloud Credit Fulfillment Request should be present
        AssertCollector.assertThat("Only CC fulfillment request should be present",
            purchaseOrderDetailpage.getFulfillmentResponsesType(1), equalTo("CC_FULFILLMENT_REQUEST"),
            assertionErrorList);
        AssertCollector.assertThat("Only CC fulfillment result should be present",
            purchaseOrderDetailpage.getFulfillmentResponsesResult(1), equalTo(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        // Fulfillment call back for Meta Order.
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrderDetailpage.refreshPage();
        // On Fulfillment call back, it adds Fulfillment Response.
        AssertCollector.assertThat("Fulfillment Response Type is incorrect",
            purchaseOrderDetailpage.getFulfillmentResponsesType(2),
            equalTo(FulfillmentResponseType.LEGACY_ASYNC_NOTIFICATION.getResponseType()), assertionErrorList);
        AssertCollector.assertThat("Fulfillment Response Result should be present",
            purchaseOrderDetailpage.getFulfillmentResponsesResult(2),
            equalTo(FulfillmentResponseResult.SUCCEEDED.getResponseResult()), assertionErrorList);

        AssertCollector.assertThat("There should not be more than Two Entires ",
            purchaseOrderDetailpage.getFulfillmentResponsesGridSize(), is(2), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

}
