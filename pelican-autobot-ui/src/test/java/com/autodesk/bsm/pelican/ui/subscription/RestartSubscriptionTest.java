package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.CancelSubscriptionPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

public class RestartSubscriptionTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private static final int quantity = 1;
    private PurchaseOrderUtils purchaseOrderUtils;
    private FindSubscriptionsPage findSubscriptionsPage;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This test case first cancels a subscription with cancellation policy as cancel at end and restarts a subscription
     */
    @Test
    public void startASubscriptionAfterCancellation() {

        // Submit a purchase order with authorize state
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(getBicMonthlyUsPriceId(), 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, buyerUser);

        // get subscription id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        // else cast it to subscription
        final SubscriptionDetailPage subscriptionDetailPage =
            findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final CancelSubscriptionPage cancelSubscriptionPage = subscriptionDetailPage.clickOnCancelSubscription();
        cancelSubscriptionPage.cancelASubscription(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, null);
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        subscriptionDetailPage.restartASubscription();
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
            equalTo(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect boolean value for auto-renewed", actualSubscription.getAutoRenew(),
            equalTo(String.valueOf(true)), assertionErrorList);
        AssertCollector.assertThat("Incorrect credit days discount for a subscription",
            actualSubscription.getCreditDays(), equalTo(String.valueOf(0)), assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing price id of a subscription",
            actualSubscription.getNextBillingPriceId(), equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect expiration date of a subscription",
            actualSubscription.getExpirationDate(), equalTo(expectedSubscription.getExpirationDate()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing date of a subscription",
            actualSubscription.getNextBillingDate(), equalTo(expectedSubscription.getNextBillingDate()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
