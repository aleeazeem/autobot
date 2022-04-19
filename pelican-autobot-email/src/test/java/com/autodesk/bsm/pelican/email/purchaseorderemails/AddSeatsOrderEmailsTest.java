package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests Add Seats Order Complete email
 *
 * @author Shweta Hegde
 */
public class AddSeatsOrderEmailsTest extends BaseTestData {

    private PurchaseOrderUtils purchaseOrderUtils;
    private HashMap<String, Integer> priceQuantityMap;
    private Map<String, String> subscriptionQuantityMap;
    private String promotionId;
    private String priceIdForBic;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // Creating price id for BIC and Meta.
        priceIdForBic = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        // Creating Regular Promotions for BIC subscriptions non Store wide % Discount
        final JPromotion nonStoreWidePercentDiscountPromo = promotionUtils.addPromotion(
            PromotionType.DISCOUNT_PERCENTAGE, com.google.common.collect.Lists.newArrayList(getStoreUk()),
            com.google.common.collect.Lists.newArrayList(bicSubscriptionOffering), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, "10", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 4, null, null);

        promotionId = nonStoreWidePercentDiscountPromo.getData().getId();

    }

    /**
     * Test email is sent when new seats are added to the same subscription
     */
    @Test
    public void testAddSeatsOrderCompleteEmailWhenSeatsAreAddedToTheSameSubscription() {

        // submit a purchase order to create a commercial subscription
        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic, 2);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);

        // get subscription
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        // Submit Add Seats order
        final PurchaseOrder purchaseOrderForAddedSeats =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, getBuyerUser());

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        // final running the final invoice job
        final PelicanTriggerClient triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        final JobsClient jobsResource = triggerResource.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, addSeatsPurchaseOrderId);

        // Validate email
        PelicanDefaultEmailValidations.orderCompleteForAddSeats(addSeatsPurchaseOrderId, subscriptionId, 5, 7,
            purchaseOrderForAddedSeats.getCreationTime(), getEnvironmentVariables(),
            Arrays.asList(getBuyerUser().getEmail()));

        // Email validation
        PelicanDefaultEmailValidations.addSeatsInvoice(addSeatsPurchaseOrderId, subscriptionId, 5,
            purchaseOrderForAddedSeats.getCreationTime(), getEnvironmentVariables());

        // Process Extension Order For REFUND.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, addSeatsPurchaseOrderId);

        // Helper Method to validate Email is Found with all required fields.
        PelicanDefaultEmailValidations.addSeatsCreditNote(addSeatsPurchaseOrderId, subscriptionId, 2,
            purchaseOrderForAddedSeats.getCreationTime(), getEnvironmentVariables());

    }

    /**
     * Test Add Seats Order Complete email when Add Seats order creating a new subscription with recurring promotion
     */
    @Test
    public void testAddSeatsOrderCompleteEmailWhenSeatsAreAddedCreatesNewSubscription() {

        priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic, 2);
        // Submit New Acquisition order
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        // get subscription Id
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Map containing Subscription Id, Price Id and Quantity
        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceIdForBic);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "3");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), promotionId);

        // Submit Add Seats order
        final PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.PAYPAL, OrderCommand.AUTHORIZE, null, getBuyerUser());

        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();

        // Processing for PENDING Order State.
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(getBuyerUser(), addSeatsPurchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);

        final String addSeatsSubscriptionId = purchaseOrder.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // final running the final invoice job
        final PelicanTriggerClient triggerResource = new PelicanTriggerClient(getEnvironmentVariables());
        final JobsClient jobsResource = triggerResource.jobs();
        purchaseOrderUtils.runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(jobsResource, addSeatsPurchaseOrderId);

        // Email validation
        PelicanDefaultEmailValidations.orderCompleteForAddSeats(addSeatsPurchaseOrderId, addSeatsSubscriptionId, 3, 3,
            purchaseOrderForAddedSeats.getCreationTime(), getEnvironmentVariables(),
            Arrays.asList(getBuyerUser().getEmail()));

        // Email validation
        PelicanDefaultEmailValidations.addSeatsInvoice(addSeatsPurchaseOrderId, addSeatsSubscriptionId, 3,
            purchaseOrderForAddedSeats.getCreationTime(), getEnvironmentVariables());

        // Process Extension Order For REFUND.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, addSeatsPurchaseOrderId);

        // Helper Method to validate Email is Found with all required fields.
        PelicanDefaultEmailValidations.addSeatsCreditNote(addSeatsPurchaseOrderId, addSeatsSubscriptionId, 3,
            purchaseOrderForAddedSeats.getCreationTime(), getEnvironmentVariables());
    }
}
