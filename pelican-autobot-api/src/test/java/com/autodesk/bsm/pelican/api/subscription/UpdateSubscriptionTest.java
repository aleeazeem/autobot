package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.OrderResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Class to verify update subscription api.
 *
 * @author jains
 *
 */
public class UpdateSubscriptionTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private PaymentProfile newPaymentProfile;
    private static String bicPrice1;
    private static String metaPrice1;
    private FindSubscriptionsPage findSubscriptionPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSubscriptionTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        bicPrice1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
            .getIncluded().getPrices().get(0).getId();
        metaPrice1 =
            subscriptionPlanApiUtils
                .addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                    BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
                .getIncluded().getPrices().get(0).getId();
    }

    /**
     * Verify that update subscription by id works for BIC subscription.
     *
     * @result SPP updated from credit card to paypal
     */
    @Test
    public void updatePaymentProfileOfBicSubscriptionFromCCToPaypal() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);

        // Submit PO
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        final String subscriptionId =
            newPurchase.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        LOGGER.info("Subscription Id: " + subscriptionId);

        final SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        final String timeStampForChargeActivity = subscriptionDetailPage.getLastSubscriptionActivity().getDate();
        final String oldPaymentProfileId = subscriptionDetailPage.getPaymentProfile().split("-")[0];
        // Add new payment profile
        newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addPaypalPaymentProfile(newPurchase.getBuyerUser().getId(), null);

        // Update the subscription
        final boolean isSubscriptionUpdated =
            resource.subscription().updateSubscription(subscriptionId, newPaymentProfile.getId());
        // refresh page after adding payment profile
        subscriptionDetailPage.refreshPage();
        AssertCollector.assertThat(
            "Subscription activity time stamp is not correct for payment profile update for subscription id "
                + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getDate(), not(timeStampForChargeActivity),
            assertionErrorList);
        AssertCollector.assertThat(
            "Subscription activity is not correct for payment profile update  for subscription id " + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getActivity(),
            equalTo(PelicanConstants.PAYMENT_PROFILE_UPDATE), assertionErrorList);
        AssertCollector.assertThat(
            "Subscription activity requestor is not correct for payment profile update for subscription id "
                + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getRequestor().split(" ")[0],
            equalTo(getEnvironmentVariables().getPartnerId()), assertionErrorList);
        final String expectedMemo = "Old payment profile id: " + oldPaymentProfileId + "\n" + "New payment profile id: "
            + newPaymentProfile.getId();
        AssertCollector.assertThat(
            "Subscription activity memo is not correct for payment profile update for subscription id "
                + subscriptionId,
            subscriptionDetailPage.getLastSubscriptionActivity().getMemo(), equalTo(expectedMemo), assertionErrorList);

        AssertCollector.assertThat("Unable to update subscription", isSubscriptionUpdated, equalTo(true),
            assertionErrorList);

        // Find subscription by id to verify that new payment profile is
        // associated with it
        final Subscription subscription = resource.subscription().getById(subscriptionId);
        AssertCollector.assertThat("Subscription not updated", subscription.getStorePaymentProfileId(),
            equalTo(newPaymentProfile.getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that update subscription by id works for BIC subscription.
     *
     * @result SPP updated from credit card to paypal
     */
    @Test
    public void updatePaymentProfileOfBicSubscriptionFromPaypalToCC() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);

        // Submit PO
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, null);
        final String subscriptionPaypalToCCPaymentMethodChangedForBIC =
            newPurchase.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        LOGGER.info("Subscription Id: " + subscriptionPaypalToCCPaymentMethodChangedForBIC);

        // Add new payment profile
        newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addCreditCardPaymentProfile(newPurchase.getBuyerUser().getId(),
                    PaymentProcessor.BLUESNAP_NAMER.getValue());

        // Update the subscription
        final boolean isSubscriptionUpdated = resource.subscription()
            .updateSubscription(subscriptionPaypalToCCPaymentMethodChangedForBIC, newPaymentProfile.getId());
        AssertCollector.assertThat("Unable to update subscription", isSubscriptionUpdated, equalTo(true),
            assertionErrorList);

        // Find subscription by id to verify that new payment profile is
        // associated with it
        final Subscription updatedSubscription =
            resource.subscription().getById(subscriptionPaypalToCCPaymentMethodChangedForBIC);
        AssertCollector.assertThat("Subscription not updated", updatedSubscription.getStorePaymentProfileId(),
            equalTo(newPaymentProfile.getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that update subscription by id works for Meta subscription.
     *
     * @result SPP updated from credit card to paypal
     */
    @Test
    public void updatePaymentProfileOfMetaSubscriptionFromCCToPaypal() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(metaPrice1, 10);

        // Submit PO
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
        // Add new payment profile
        newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addPaypalPaymentProfile(newPurchase.getBuyerUser().getId(), null);

        // Fulfillment request
        final OrderResponse response =
            purchaseOrderUtils.fulfillRequest(newPurchase, FulfillmentCallbackStatus.Created);
        AssertCollector.assertThat("Fulfillment Status Not as Expected", response.getOrderCreationStatus(),
            equalTo("Received"), assertionErrorList);
        AssertCollector.assertThat("Invalid PurchaseOrder Id", response.getOrderReference().getPONumber(),
            equalTo(newPurchase.getId()), assertionErrorList);

        // Get purchase order to find the subscriptionId
        final PurchaseOrder fullFilledPO = resource.purchaseOrder().getById(response.getOrderReference().getPONumber());
        final String subscriptionCCToPaypalPaymentMethodChangedForMeta =
            fullFilledPO.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id: " + subscriptionCCToPaypalPaymentMethodChangedForMeta);

        // Update the subscription
        final boolean isSubscriptionUpdated = resource.subscription()
            .updateSubscription(subscriptionCCToPaypalPaymentMethodChangedForMeta, newPaymentProfile.getId());
        AssertCollector.assertThat("Unable to update subscription", isSubscriptionUpdated, equalTo(true),
            assertionErrorList);

        // Find subscription by id to verify that new payment profile is
        // associated with it
        final Subscription updatedSubscription =
            resource.subscription().getById(subscriptionCCToPaypalPaymentMethodChangedForMeta);
        AssertCollector.assertThat("Subscription not updated", updatedSubscription.getStorePaymentProfileId(),
            equalTo(newPaymentProfile.getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that update subscription by id works for Meta subscription.
     *
     * @result SPP updated from credit card to paypal
     */
    @Test
    public void updatePaymentProfileOfMetaSubscriptionFromPaypalToCC() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(metaPrice1, 10);

        // Submit PO
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, true, null);
        // Add new payment profile
        newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addCreditCardPaymentProfile(newPurchase.getBuyerUser().getId(),
                    PaymentProcessor.BLUESNAP_NAMER.getValue());

        // Fulfillment request
        final OrderResponse response =
            purchaseOrderUtils.fulfillRequest(newPurchase, FulfillmentCallbackStatus.Created);
        AssertCollector.assertThat("Fulfillment Status Not as Expected", response.getOrderCreationStatus(),
            equalTo("Received"), assertionErrorList);
        AssertCollector.assertThat("Invalid PurchaseOrder Id", response.getOrderReference().getPONumber(),
            equalTo(newPurchase.getId()), assertionErrorList);

        // Get purchase order to find the subscriptionId
        final PurchaseOrder fullFilledPO = resource.purchaseOrder().getById(response.getOrderReference().getPONumber());
        final String subscriptionPaypalToCCPaymentMethodChangedForMeta =
            fullFilledPO.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id: " + subscriptionPaypalToCCPaymentMethodChangedForMeta);

        // Update the subscription
        final boolean isSubscriptionUpdated = resource.subscription()
            .updateSubscription(subscriptionPaypalToCCPaymentMethodChangedForMeta, newPaymentProfile.getId());
        AssertCollector.assertThat("Unable to update subscription", isSubscriptionUpdated, equalTo(true),
            assertionErrorList);

        // Find subscription by id to verify that new payment profile is
        // associated with it
        final Subscription updatedSubscription =
            resource.subscription().getById(subscriptionPaypalToCCPaymentMethodChangedForMeta);
        AssertCollector.assertThat("Subscription not updated", updatedSubscription.getStorePaymentProfileId(),
            equalTo(newPaymentProfile.getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
