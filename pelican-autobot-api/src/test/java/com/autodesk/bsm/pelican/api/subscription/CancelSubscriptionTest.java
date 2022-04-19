package com.autodesk.bsm.pelican.api.subscription;

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
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;

/**
 * Test Case : Cancel subscription
 *
 * @author Shweta Hegde
 */
public class CancelSubscriptionTest extends BaseTestData {

    private PelicanPlatform resource;
    private Object apiResponse;
    private Subscription subscription;
    private HttpError httpError;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int quantity = 1;

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
     * This method tests cancel subscription immediately for BIC pending PO with credit card
     */
    @Test
    public void cancelSubscriptionImmediateForPendingBicCreditCardOrder() {
        // Get purchase order for BIC with credit card
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), getBuyerUser(), quantity);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        // get subscription id from purchase order
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // cancel the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.IMMEDIATE_NO_REFUND);

        // get subscription by id to validate the status of canceled subscription
        apiResponse = resource.subscription().getById(subscriptionId);
        // if apiresponse is instance of http error
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            // else typecast to subscription
            subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("Unable to find subscription", subscription, is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription id", subscription.getId(), equalTo(subscriptionId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect owner id", subscription.getOwnerId(), equalTo(getBuyerUser().getId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect status", subscription.getStatus(), equalTo(Status.EXPIRED.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect expiration date", subscription.getExpirationDate().split("\\s+")[0],
                equalTo(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests cancel subscription at the end of billing period for Meta charged PO with Paypal
     */
    @Test
    public void cancelSubscriptionAtEndOfBillingPeriodForChargedMetaPaypalOrder() throws ParseException {
        // submit PO for meta with Paypal
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL,
            getMetaYearlyUsPriceId(), getBuyerUser(), quantity);
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(), purchaseOrder.getId());
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        // fulfill the request since it is Meta
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        // get purchase order api response
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // cancel the subscription
        resource.subscription().cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        // get subscription by id to validate the status of canceled subscription
        apiResponse = resource.subscription().getById(subscriptionId);
        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(PurchaseOrder.class), assertionErrorList);
        } else {
            subscription = (Subscription) apiResponse;
            AssertCollector.assertThat("Unable to find subscription", subscription, is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription id", subscription.getId(), equalTo(subscriptionId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect owner id", subscription.getOwnerId(), equalTo(getBuyerUser().getId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect status", subscription.getStatus(),
                equalTo(Status.CANCELLED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect next billing date",
                subscription.getNextBillingDate().split("\\s+")[0],
                equalTo(
                    DateTimeUtils.getNextBillingDate(DateTimeUtils.getCurrentDate(), BillingFrequency.YEAR.toString())),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect expiration date", subscription.getExpirationDate(),
                equalTo(subscription.getNextBillingDate()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
