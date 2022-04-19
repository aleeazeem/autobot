package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import com.google.common.collect.Iterables;

import java.util.List;

/**
 * Helper class for Subscription assert.
 *
 * @author t_joshv
 */
public class SubscriptionHelper {

    /**
     * Method to verify Subscription after Refund Process.
     *
     * @param subscriptionDetailPage
     * @param quantity
     * @param status
     * @param purchaseOrderId
     * @param refundedSeats
     * @param assertionErrorList
     */
    public static void assertionsOnSubscriptionAfterRefund(final SubscriptionDetailPage subscriptionDetailPage,
        final int quantity, final Status status, final String purchaseOrderId, final int refundedSeats,
        final List<AssertionError> assertionErrorList) {

        // Assert on Quantity.
        AssertCollector.assertThat("Incorrect Quantity left after Refund Process", subscriptionDetailPage.getQuantity(),
            is(quantity), assertionErrorList);

        // Assert on Subscription Status.
        AssertCollector.assertThat("Incorrect Subscription State after Refund", status.toString(),
            equalTo(subscriptionDetailPage.getStatus()), assertionErrorList);

        // Assert on Subscription Memo.
        if (status != Status.EXPIRED) {

            AssertCollector.assertThat("Incorrect Subscription Activity Memo",
                Iterables.getLast(subscriptionDetailPage.getSubscriptionActivity()).getMemo(),

                equalTo(String.format("Refunded PO #%s.\nReduced %s seats.", purchaseOrderId, refundedSeats)),
                assertionErrorList);
        }
    }

}
