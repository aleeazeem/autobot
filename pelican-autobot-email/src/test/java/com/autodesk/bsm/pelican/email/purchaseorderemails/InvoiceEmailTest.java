package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class InvoiceEmailTest extends BaseTestData {

    private JobsClient jobsResource;
    private PurchaseOrderUtils purchaseOrderUtils;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PelicanTriggerClient triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();
    }

    /**
     * Create a Paypal Order without SPP and verify that invoice email is sent.
     */
    @Test
    public void testSuccessInvoiceEmailWithoutStoredPaymentProfile() {

        // Create LineItem for purchase order.
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(getBic2YearsUkPriceId(), 5, null, null);
        final List<LineItem> lineItems = Arrays.asList(lineitem1);
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitPaypalPurchaseOrderWithRecorderStateAndBillingInfo(
            lineItems, getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId(), getBuyerUser());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        // running the invoice job
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, purchaseOrder.getId());
        PelicanDefaultEmailValidations.invoice(purchaseOrder.getId(), getEnvironmentVariables(), true);

        AssertCollector.assertAll(assertionErrorList);
    }
}
