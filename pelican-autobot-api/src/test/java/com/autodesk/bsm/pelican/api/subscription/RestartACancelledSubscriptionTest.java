package com.autodesk.bsm.pelican.api.subscription;
/**
 * Test Case : Restart a Cancelled Subscription
 *
 * @author Shweta Hegde
 */

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.HashMap;

public class RestartACancelledSubscriptionTest extends BaseTestData {

    private PelicanPlatform resource;
    private Object apiResponse;
    private Subscription subscription;
    private HttpError httpError;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int quantity = 1;
    private boolean isSubscriptionCanceled;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        apiResponse = null;
    }

    /**
     * This method tests restarted cancelled subscription for BIC pending PO with credit card
     */
    @Test
    public void restartCancelledSubscriptionForPendingBicCreditCardOrder() throws ParseException {
        // Get purchase order for BIC with credit card
        PurchaseOrder purchaseOrderForBicCreditCard = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), getBuyerUser(), quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard.getId());
        // get purchase order api response
        purchaseOrderForBicCreditCard = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard.getId());
        // get subscription id from purchase order
        final String subscriptionIdForBicCreditCard = purchaseOrderForBicCreditCard.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        // cancel the subscription
        isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionIdForBicCreditCard,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (!isSubscriptionCanceled) {
            Assert.fail("Error! Subscription is not canceled");
        } else {
            // restart cancelled subscription
            resource.subscription().restartCancelledSubscription(subscriptionIdForBicCreditCard);
            // get subscription by id to validate the status of restarted
            // subscription
            apiResponse = resource.subscription().getById(subscriptionIdForBicCreditCard);
            // if apiresponse is instance of http error
            if (apiResponse instanceof HttpError) {
                httpError = (HttpError) apiResponse;
                AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                    instanceOf(PurchaseOrder.class), assertionErrorList);
            } else {
                subscription = (Subscription) apiResponse;
                AssertCollector.assertThat("Unable to find subscription", subscription, is(notNullValue()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(),
                    equalTo(subscriptionIdForBicCreditCard), assertionErrorList);
                AssertCollector.assertThat("Incorrect owner id ", subscription.getOwnerId(),
                    equalTo(getBuyerUser().getId()), assertionErrorList);
                AssertCollector.assertThat("Incorrect status ", subscription.getStatus(),
                    equalTo(Status.ACTIVE.toString()), assertionErrorList);
                AssertCollector.assertThat("Incorrect next billing date",
                    subscription.getNextBillingDate().split("\\s+")[0], equalTo(DateTimeUtils
                        .getNextBillingDate(DateTimeUtils.getCurrentDate(), BillingFrequency.MONTH.toString())),
                    assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests restarted cancelled subscription for Meta charged PO with Paypal
     */
    @Test
    public void restartCancelledSubscriptionForChargedMetaPaypalOrder() throws ParseException {
        // submit PO for meta with Paypal
        PurchaseOrder purchaseOrderForMetaPaypal = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, getMetaYearlyUsPriceId(), getBuyerUser(), quantity);
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(), purchaseOrderForMetaPaypal.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForMetaPaypal.getId());
        // get purchase order api response
        purchaseOrderForMetaPaypal = resource.purchaseOrder().getById(purchaseOrderForMetaPaypal.getId());
        // fulfill the request since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrderForMetaPaypal, FulfillmentCallbackStatus.Created);
        // get purchase order api response
        purchaseOrderForMetaPaypal = resource.purchaseOrder().getById(purchaseOrderForMetaPaypal.getId());
        final String subscriptionIdForMetaPaypal = purchaseOrderForMetaPaypal.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();
        // cancel the subscription
        isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionIdForMetaPaypal,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (!isSubscriptionCanceled) {
            Assert.fail("Error! Subscription is not canceled");

        } else {
            // restart cancelled subscription
            resource.subscription().restartCancelledSubscription(subscriptionIdForMetaPaypal);
            // get subscription by id to validate the status of restarted
            // canceled subscription
            apiResponse = resource.subscription().getById(subscriptionIdForMetaPaypal);
            if (apiResponse instanceof HttpError) {
                httpError = (HttpError) apiResponse;
                AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                    instanceOf(PurchaseOrder.class), assertionErrorList);
            } else {
                subscription = (Subscription) apiResponse;
                AssertCollector.assertThat("Unable to find subscription", subscription, is(notNullValue()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription id ", subscription.getId(),
                    equalTo(subscriptionIdForMetaPaypal), assertionErrorList);
                AssertCollector.assertThat("Incorrect owner id ", subscription.getOwnerId(),
                    equalTo(getBuyerUser().getId()), assertionErrorList);
                AssertCollector.assertThat("Incorrect status ", subscription.getStatus(),
                    equalTo(Status.ACTIVE.toString()), assertionErrorList);
                AssertCollector.assertThat("Incorrect next billing date",
                    subscription.getNextBillingDate().split("\\s+")[0], equalTo(DateTimeUtils
                        .getNextBillingDate(DateTimeUtils.getCurrentDate(), BillingFrequency.YEAR.toString())),
                    assertionErrorList);

            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests that subscription can not be started if subscription doesn't has spp.
     */
    @Test
    public void testErrorMessageForRestartingASubscriptionWithoutSpp() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String subscriptionId = getPurchaseOrder.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        // cancel the subscription
        isSubscriptionCanceled = resource.subscription().cancelSubscription(subscriptionId,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (!isSubscriptionCanceled) {
            Assert.fail("Subscription should had been canceled " + subscriptionId);
        } else {
            // set stored payment profile to null
            final String updateQuery =
                "Update subscription set STORED_PAYMENT_PROFILE_ID = null where id = " + subscriptionId;
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
        }
        // restart cancelled subscription
        httpError = resource.subscription().restartCancelledSubscription(subscriptionId);

        AssertCollector.assertThat("Restart api response is not correct for subscription id: " + subscriptionId,
            httpError.getStatus(), is(400), assertionErrorList);
        AssertCollector.assertThat(
            "Error message for restarting subscription with out payment profile is not correct for subscription id: "
                + subscriptionId,
            httpError.getErrorMessage(),
            equalTo("Stored payment profile id needs to be set before restarting subscription."), assertionErrorList);

        subscription = resource.subscription().getById(subscriptionId);
        AssertCollector.assertThat("Subscription status is not correct for subscription id: " + subscriptionId,
            subscription.getStatus(), equalTo(Status.CANCELLED.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
