package com.autodesk.bsm.pelican.email.subscriptionemails;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class RestartSubscriptionEmail extends BaseTestData {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    @Test
    public void testRenewalRestartEmailForCancelledSubscription() {
        // Get purchase order for BIC with credit card
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUsPriceId(), 1)), null, true, true, null);

        // get purchase order api response
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // cancel the subscription
        final Boolean isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionId,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (!isSubscriptionCanceled) {
            AssertCollector.assertThat("Error! Subscription is not canceled", true, equalTo(isSubscriptionCanceled),
                assertionErrorList);

        } else {
            // restart cancelled subscription
            resource.subscription().restartCancelledSubscription(subscriptionId);
            // get subscription by id to validate the status of restarted
            // canceled subscription
            final Object apiResponse = resource.subscription().getById(subscriptionId);

            final Subscription subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("Unable to find subscription", subscription, is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(), equalTo(subscriptionId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect owner id ", subscription.getOwnerId(),
                equalTo(getBuyerUser().getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect status ", subscription.getStatus(), equalTo(Status.ACTIVE.toString()),
                assertionErrorList);
            PelicanDefaultEmailValidations.renewalRestart(purchaseOrder.getId(), subscriptionId,
                getEnvironmentVariables());
        }
        AssertCollector.assertAll(assertionErrorList);
    }

}
