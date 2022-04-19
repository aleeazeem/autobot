package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Test class to verify Order Complete , Invoice, Credit Note and Auto Renewal Email for Bic and Meta Subscription plan
 * and for Credit card and PAYPAL Payment Methods.
 *
 * @author t_joshv
 *
 */

public class PurchaseOrderRelatedEmails extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PelicanTriggerClient triggerClient;
    private PurchaseOrderUtils purchaseOrderUtils;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        triggerClient = new PelicanTriggerClient(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /**
     * Test Method to verify New Acquisition Order Complete, Invoice And Credit Note Email for Bic Subscription and
     * Credit card payment method.
     */
    @Test
    public void testNewAcquisitionOrderCompleteAndInvoiceAndCreditNoteEmailsForBicCreditCard() {

        // Create a purchase order and get BIC subscription Order id.
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(getBicMonthlyUkPriceId(), 4)), null, true, true, getBuyerUser());
        // Order Complete Email Validation.
        PelicanDefaultEmailValidations.orderComplete(purchaseOrderId, getEnvironmentVariables(), true,
            Arrays.asList(getBuyerUser().getEmail()));

        final JobsClient triggerjob = triggerClient.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(triggerjob, purchaseOrderId);
        // Invoice Email Validation.
        PelicanDefaultEmailValidations.invoice(purchaseOrderId, getEnvironmentVariables(), false);

        // CreditNote Validation.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        PelicanDefaultEmailValidations.creditNoteMemo(purchaseOrderId, getEnvironmentVariables());
    }

    /**
     * Test Method to verify New Acquisition Order Complete, Invoice And Credit Note Email for Meta Subscription and
     * Paypal payment method.
     */
    @Test
    public void testNewAcquisitionOrderCompleteAndInvoiceAndCreditNoteEmailsForMetaPaypal() {

        // Create a purchase order and get Meta subscription Order id.
        final String purchaseOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(PaymentType.PAYPAL,
            new HashMap<>(ImmutableMap.of(getMetaYearlyUkPriceId(), 4)), null, true, true, getBuyerUser());

        // Order Complete Email Validation.
        PelicanDefaultEmailValidations.orderComplete(purchaseOrderId, getEnvironmentVariables(), false,
            Arrays.asList(getBuyerUser().getEmail()));

        purchaseOrderUtils.fulfillRequest(resource.purchaseOrder().getById(purchaseOrderId),
            FulfillmentCallbackStatus.Created);

        final PelicanTriggerClient triggerClient = new PelicanTriggerClient(getEnvironmentVariables());
        final JobsClient triggerjob = triggerClient.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(triggerjob, purchaseOrderId);
        // Invoice Email Validation.
        PelicanDefaultEmailValidations.invoice(purchaseOrderId, getEnvironmentVariables(), false);

        // CreditNote Validation.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        PelicanDefaultEmailValidations.creditNoteMemo(purchaseOrderId, getEnvironmentVariables());
    }

    /**
     * Test Method to verify New Acquisition Order and Renewal order With Promotion.
     */
    @Test
    public void testNewAcquisitionAndRenewalOrderEmailsWithPromotion() {

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // Add subscription plans
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        final String priceIdForBicOffering = bicOffering.getIncluded().getPrices().get(0).getId();

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        final JPromotion nonStoreWideCashDiscountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 5, null, null);

        final String promotionId = nonStoreWideCashDiscountPromo.getData().getId();

        final PromotionReferences promotionReferences = new LineItem.PromotionReferences();
        final PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(promotionId);
        promotionReferences.setPromotionReference(promotionReference);

        // Submit a purchase order with Credit card and process it to charged
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, 5)), false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicOffering, promotionReferences)), getBuyerUser());
        final String newAcquisitionPurchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, newAcquisitionPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, newAcquisitionPurchaseOrderId);

        PelicanDefaultEmailValidations.orderCompleteWithPromotion(newAcquisitionPurchaseOrderId,
            getEnvironmentVariables(), true, Arrays.asList(getBuyerUser().getEmail()), "10", "490");

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrderWithPromotion =
            resource.purchaseOrder().getById(newAcquisitionPurchaseOrderId);
        final String subscriptionId = purchaseOrderWithPromotion.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        final PurchaseOrder renewedOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(getBuyerUser(),
            Arrays.asList(subscriptionId), false, PaymentType.CREDIT_CARD, null, true);

        PelicanDefaultEmailValidations.autoRenewal(renewedOrder.getId(), subscriptionId, getEnvironmentVariables());

    }

}
