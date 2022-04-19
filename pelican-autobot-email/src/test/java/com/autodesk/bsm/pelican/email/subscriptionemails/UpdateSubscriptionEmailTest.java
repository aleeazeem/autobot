package com.autodesk.bsm.pelican.email.subscriptionemails;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentOption;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class UpdateSubscriptionEmailTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private PaymentProfile newPaymentProfile;
    private FindSubscriptionsPage findSubscriptionPage;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /**
     * Verify that updating payment profile for BIC subscription sends Email on change of payment method.
     */
    @Test
    public void updatePaymentProfileOfBicSubscriptionFromCCToPaypalSendsMail() {
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 1)), null, true, true, null);
        final String subscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final SubscriptionDetailPage subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        final String oldPaymentProfileId = subscriptionDetailPage.getPaymentProfile().split("-")[0];
        // Add new payment profile
        newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addPaypalPaymentProfile(resource.purchaseOrder().getById(purchaseOrderId).getBuyerUser().getId(),
                    null);

        // Update the subscription
        final boolean isSubscriptionUpdated =
            resource.subscription().updateSubscription(subscriptionId, newPaymentProfile.getId());
        // refresh page after adding payment profile
        subscriptionDetailPage.refreshPage();

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
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        AssertCollector.assertThat("Subscription not updated", subscription.getStorePaymentProfileId(),
            equalTo(newPaymentProfile.getId()), assertionErrorList);

        PelicanDefaultEmailValidations.paymentMethodChange(purchaseOrderId, subscriptionId, getEnvironmentVariables(),
            PaymentOption.PAYPAL);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that updating payment profile for Meta subscription sends Email on change of payment method.
     */
    @Test
    public void updatePaymentProfileOfMetaSubscriptionFromPaypalToCCSendsMail() {
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.PAYPAL,
            new HashMap<>(ImmutableMap.of(getMetaMonthlyUsPriceId(), 1)), null, true, true, null);

        // Add new payment profile
        newPaymentProfile =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId())
                .addCreditCardPaymentProfile(resource.purchaseOrder().getById(purchaseOrderId).getBuyerUser().getId(),
                    PaymentProcessor.BLUESNAP_NAMER.getValue());

        // Fulfillment request
        purchaseOrderUtils.fulfillRequest(resource.purchaseOrder().getById(purchaseOrderId),
            FulfillmentCallbackStatus.Created);

        // Get purchase order to find the subscriptionId
        final PurchaseOrder fullFilledPO = resource.purchaseOrder().getById(purchaseOrderId);
        final String subscriptionId =
            fullFilledPO.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Update the subscription
        final boolean isSubscriptionUpdated =
            resource.subscription().updateSubscription(subscriptionId, newPaymentProfile.getId());
        AssertCollector.assertThat("Unable to update subscription", isSubscriptionUpdated, equalTo(true),
            assertionErrorList);

        // Find subscription by id to verify that new payment profile is
        // associated with it
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final Subscription updatedSubscription = resource.subscription().getById(subscriptionId);
        AssertCollector.assertThat("Subscription not updated", updatedSubscription.getStorePaymentProfileId(),
            equalTo(newPaymentProfile.getId()), assertionErrorList);

        PelicanDefaultEmailValidations.paymentMethodChange(purchaseOrderId, subscriptionId, getEnvironmentVariables(),
            PaymentOption.VISA);

        AssertCollector.assertAll(assertionErrorList);
    }

}
