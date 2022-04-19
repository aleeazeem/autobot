package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.basicoffering.Currency;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStrategy;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.PurchaseOrderAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.AddBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.FindPurchaseOrdersPage;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test Class Verifies User able to see Retrigger button for orders that have Cloud Credit fulfillment in FAILED status.
 *
 * @author Vaibhavi Joshi
 */

public class CloudCreditFulfillmentForBasicOfferingTest extends SeleniumWebdriver {

    private PurchaseOrderUtils purchaseOrderUtils;
    private String priceIdForCloudCredit;
    private FindPurchaseOrdersPage findPurchaseOrdersPage;
    private LinkedHashMap<String, Integer> priceQuantityMap;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged = false;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(CloudCreditFulfillmentForBasicOfferingTest.class.getSimpleName());
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();

        // Login to Admin Page.
        final AdminToolPage adminTool = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminTool.login();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // Initialize Basic Offering Page.
        final AddBasicOfferingPage addBasicOfferingPage = adminTool.getPage(AddBasicOfferingPage.class);
        findPurchaseOrdersPage = adminTool.getPage(FindPurchaseOrdersPage.class);
        bankingConfigurationPropertiesPage = adminTool.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged = bankingConfigurationPropertiesPage
            .setFeatureFlag(PelicanConstants.STOP_LEGACY_FULFILLMENT_CALL_FLAG, false);

        // Create User without Oxygen Id
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        // Setup Payment Info for Non Oxygen User.
        final PaymentProfileUtils paymentProfileUtils =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        paymentProfileUtils.addCreditCardPaymentProfile(buyerUser.getId(), PaymentProcessor.BLUESNAP_EMEA.getValue());

        // Add currency and amount only when Offering Type is "Currency"
        final Currency currency = resource.currency().getById(getEnvironmentVariables().getCloudCurrencyId());
        final String currencyName = currency.getDescription() + " (" + currency.getName() + ")";
        final String amount = "100.00";

        // Creating meta offering
        final Offerings metaOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.QUARTER, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceIdForMeta = metaOffering.getIncluded().getPrices().get(0).getId();

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

        priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(priceIdForMeta, 1);
        priceQuantityMap.put(priceIdForCloudCredit, 1);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.STOP_LEGACY_FULFILLMENT_CALL_FLAG, true);
        }
    }

    /**
     * Test to verify that Cloud Credits get provision successfully for existing oxygen user, Offering type:Currency and
     * Currency:CLOUD
     */
    @Test
    public void testCloudCreditsProvisionsSuccessfully() {

        // Create Cloud Credit Purchase order with Authorized Status for Buyer
        final String purchaseOrderIdForCloudCredit = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForCloudCredit, getBuyerUser(), 1)
            .getId();

        // Purchase order created in previous step is processed to "Pending'
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForCloudCredit);

        // Purchase order created in previous step is processed to "Charged' but
        // not fulfilled == Pending
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForCloudCredit);
        LOGGER.info("Purchase Order Id: " + purchaseOrderIdForCloudCredit);

        // getting purchase order page

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForCloudCredit);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfilment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1),
            equalTo(FulFillmentStrategy.CLOUD_CREDITS.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.FULFILLED.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify that user is able to see retrigger button for order that have Cloud Credit fulfillment in Failed
     * status.
     */
    @Test
    public void testRetriggerButtonIsVisibleForFailedCloudCredit() {
        // Create Cloud Credit Purchase order with Authorized Status for Non
        // Oxygen Buyer
        final String purchaseOrderIdForCloudCredit = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForCloudCredit, buyerUser, 1).getId();

        // Purchase order created in previous step is processed to "Pending'
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForCloudCredit);

        // Purchase order created in previous step is processed to "Charged' but
        // not fulfilled == Pending
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForCloudCredit);
        LOGGER.info("Purchase Order Id: " + purchaseOrderIdForCloudCredit);

        // getting purchase order page

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForCloudCredit);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();
        // Check that Fulfilment status and strategy.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1),
            equalTo(FulFillmentStrategy.CLOUD_CREDITS.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(1), equalTo(FulFillmentStatus.FAILED.toString()),
            assertionErrorList);

        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for FAILED Cloud Credit Fulfillment status",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.CLOUD_CREDITS.toString()),
            assertionErrorList);

        if (purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.CLOUD_CREDITS.toString())) {
            // Click retrigger button.
            purchaseOrderDetailPage.clickRetrigger(FulFillmentStrategy.CLOUD_CREDITS.toString());

            // Verify the Update Purchase order audit data
            final Boolean isAuditLogFound =
                PurchaseOrderAuditLogHelper.helperToValidateDynamoDbForPurchaseOrder(purchaseOrderIdForCloudCredit,
                    Action.UPDATE, getEnvironmentVariables().getUserId(), assertionErrorList);
            AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify retriggering LEGACY order successfully while LEGACY and CLOUD CREDIT are in fulfillment FAILED
     * Status.
     */
    @Test
    public void testRetriggerButtonProcessFailedLegacyOrder() {

        final PurchaseOrder purchaseOrderForLegacyAndCloudCredit =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForLegacyAndCloudCredit.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForLegacyAndCloudCredit.getId());
        LOGGER.info("Purchase Order Id: " + purchaseOrderForLegacyAndCloudCredit.getId());

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderForLegacyAndCloudCredit.getId());
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Identify the Index for cloud credit strategy and legacy startegy.
        final int fulfilllmentIndexForCouldCrdit =
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1).equals(FulFillmentStrategy.CLOUD_CREDITS.toString())
                ? 1
                : 2;
        final int fulfillmentIndexForLegacy = fulfilllmentIndexForCouldCrdit == 1 ? 2 : 1;
        // Check that Fulfilment status and strategy for LEGACY.
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(fulfillmentIndexForLegacy),
            equalTo(FulFillmentStrategy.LEGACY.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status for Legacy",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(fulfillmentIndexForLegacy),
            equalTo(FulFillmentStatus.FAILED.toString()), assertionErrorList);

        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for FAILED Legacy Fulfillment status",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        if (purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString())) {
            // Click retrigger button.
            purchaseOrderDetailPage.clickRetrigger(FulFillmentStrategy.LEGACY.toString());

            // Verify the Update Purchase order audit data
            final Boolean isAuditLogFound = PurchaseOrderAuditLogHelper.helperToValidateDynamoDbForPurchaseOrder(
                purchaseOrderForLegacyAndCloudCredit.getId(), Action.UPDATE, getEnvironmentVariables().getUserId(),
                assertionErrorList);
            AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);
        }

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
     * Test to verify retriggering CLOUD CREDIT order while LEGACY and CLOUD CREDIT are in fulfillment FAILED status.
     */
    @Test
    public void testRetriggerButtonProcessFailedCloudCreditOrder() {

        final PurchaseOrder purchaseOrderForLegacyAndCloudCredit =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        final String purchaseOrderIdForLegacyAndCloudCredit = purchaseOrderForLegacyAndCloudCredit.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForLegacyAndCloudCredit);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForLegacyAndCloudCredit);

        // getting purchase order page

        findPurchaseOrdersPage.findPurchaseOrderById(purchaseOrderIdForLegacyAndCloudCredit);
        final PurchaseOrderDetailPage purchaseOrderDetailPage = findPurchaseOrdersPage.clickOnSubmit();

        // Check that Fulfilment status and strategy for CLOUD_CREDITS.
        final int fulfilllmentIndexForCouldCrdit =
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(1).equals(FulFillmentStrategy.CLOUD_CREDITS.toString())
                ? 1
                : 2;
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(fulfilllmentIndexForCouldCrdit),
            equalTo(FulFillmentStrategy.CLOUD_CREDITS.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(fulfilllmentIndexForCouldCrdit),
            equalTo(FulFillmentStatus.FAILED.toString()), assertionErrorList);
        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for FAILED Legacy Fulfillment status",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.CLOUD_CREDITS.toString()),
            assertionErrorList);

        if (purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.CLOUD_CREDITS.toString())) {
            // Click retrigger button.
            purchaseOrderDetailPage.clickRetrigger(FulFillmentStrategy.CLOUD_CREDITS.toString());

            // Verify the Update Purchase order audit data
            final Boolean isAuditLogFound = PurchaseOrderAuditLogHelper.helperToValidateDynamoDbForPurchaseOrder(
                purchaseOrderForLegacyAndCloudCredit.getId(), Action.UPDATE, getEnvironmentVariables().getUserId(),
                assertionErrorList);
            AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);
        }

        // Check that Fulfilment status and strategy for Legacy.
        final int fulfillmentIndexForLegacy = fulfilllmentIndexForCouldCrdit == 1 ? 2 : 1;
        AssertCollector.assertThat("Incorrect Order Fulfillment Strategy",
            purchaseOrderDetailPage.getFulfillmentGroupStrategy(fulfillmentIndexForLegacy),
            equalTo(FulFillmentStrategy.LEGACY.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Order Fulfillment Status",
            purchaseOrderDetailPage.getFulfillmentGroupStatus(fulfillmentIndexForLegacy),
            equalTo(FulFillmentStatus.FAILED.toString()), assertionErrorList);
        // Check Retrigger button is visible.
        AssertCollector.assertTrue("Retriger Button is not Visible for FAILED Legacy Fulfillment status",
            purchaseOrderDetailPage.isRetriggerButtonVisible(FulFillmentStrategy.LEGACY.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

}
