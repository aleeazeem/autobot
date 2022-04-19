package com.autodesk.bsm.pelican.ui.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.ui.pages.purchaseorder.PurchaseOrderDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import java.util.List;

/**
 * Helper class for Purchase Order Asserts.
 *
 * @author t_joshv
 *
 */
public class PurchaseOrderHelper {

    /**
     * Assert method to verify purchase order after Refund.
     *
     * @param refundedPurchaseOrderDetailPage
     * @param assertionErrorList
     */
    public static void assertionsForRefundPurchaseOrder(final PurchaseOrderDetailPage refundedPurchaseOrderDetailPage,
        final List<AssertionError> assertionErrorList) {

        // Assert on Purchase Order State.
        AssertCollector.assertTrue("Purchase Order Status is not Refunded",
            OrderState.REFUNDED.toString().equals(refundedPurchaseOrderDetailPage.getOrderState()), assertionErrorList);

        // Assert on Purchase Order Transaction Activity.
        AssertCollector.assertTrue("Purchase Order Transaction Type is incorrect",
            OrderCommand.REFUND.toString().equals(refundedPurchaseOrderDetailPage.getTransactionType(5)),
            assertionErrorList);

        // Assert on Purchase Order Transaction Activity.
        AssertCollector.assertThat("Purchase Order Transaction Amount is incorrect",
            refundedPurchaseOrderDetailPage.getTransactionAmount(5),
            equalTo("-" + refundedPurchaseOrderDetailPage.getOrderAmount()), assertionErrorList);

        // Assert on Credit Not got generated.
        AssertCollector.assertThat("Credit Not Number not found", refundedPurchaseOrderDetailPage.getCreditNoteNumber(),
            notNullValue(), assertionErrorList);

    }

    public static void commonAssertionsForPurchaseOrderPage(final PurchaseOrderDetailPage purchaseOrderDetailPage,
        final String purchaseOrderId, final OrderState orderState, final String totalAmount, final String unitPrice,
        final int transactionIndex, final List<AssertionError> assertionErrorList) {
        purchaseOrderDetailPage.refreshPage();
        AssertCollector.assertThat("Incorrect Order State PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(orderState.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Charge transaction amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionAmount(transactionIndex), equalTo(totalAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect unit price for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getLineItemsUnitPrice(1), equalTo(unitPrice), assertionErrorList);
        AssertCollector.assertThat("Incorrect Subtotal value for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getLineItemsSubTotal(1), equalTo(totalAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Total Order Amount for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTotalAmountOrder().split(": ")[1], equalTo(totalAmount), assertionErrorList);
    }

    /**
     * Common assertions for properties, status and transaction.
     *
     * @param purchaseOrderDetailPage
     * @param purchaseOrderId
     * @param orderState
     * @param propertyName
     * @param propertyValue
     * @param rowIndex
     * @param transactionIndex
     * @param requestedBy
     * @param assertionErrorList
     */
    public static void commonAssertionsForProperties(final PurchaseOrderDetailPage purchaseOrderDetailPage,
        final String purchaseOrderId, final OrderState orderState, final String propertyName,
        final String propertyValue, final int rowIndex, final int transactionIndex, final String requestedBy,
        final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect status of PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getOrderState(), equalTo(orderState.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect properties Name for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getProperties(rowIndex, 1), equalTo(propertyName), assertionErrorList);
        AssertCollector.assertThat("Incorrect properties value for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getProperties(rowIndex, 2), equalTo(propertyValue), assertionErrorList);
        AssertCollector.assertThat("Incorrect Requested By for PO : " + purchaseOrderId,
            purchaseOrderDetailPage.getTransactionRequestedBy(transactionIndex), equalTo(requestedBy),
            assertionErrorList);
    }
}
