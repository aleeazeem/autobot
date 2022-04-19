package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient.FieldName;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscriptions;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This test class contains test that needs to run for all 4 subscription apis. This class will reduce duplicate code in
 * 4 subscription test classes.
 *
 * @author jains
 *
 */
public class GetSubscriptionCommonTest extends BaseTestData {

    private PurchaseOrderUtils purchaseOrderUtils;
    private PelicanPlatform resource;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        buyerUser = addBuyerUser();
    }

    /**
     * This test verifies that last modified date is updated for all 4 subscription apis after creating add seats order.
     *
     */
    @Test
    public void testLastModifiedDateAfterAddSeatsOrder() {
        String originalSubscriptionId;
        final Offerings offering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 4);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());
        originalSubscriptionId = resource.purchaseOrder().getById(purchaseOrder.getId()).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final Subscription subscription = resource.subscription().getById(originalSubscriptionId);
        final String lastModifiedDateBeforeAddingSeats = subscription.getLastModified();

        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // create add seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);
        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats = resource.purchaseOrder().getById(addSeatsPurchaseOrderId);
        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();

        // If add seats order is not created then fail test immediately.
        if (addSeatsSubscriptionId == null) {
            Assert.fail("Add seats order is not created correctly.");
        }

        // validate Find Subscription by id xml api
        final Subscription subscriptionByidXml = resource.subscription().getById(originalSubscriptionId);
        AssertCollector.assertThat("Subscription id is not correct in get subscription by id XML api.",
            subscriptionByidXml.getId(), equalTo(originalSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Last modified date is not updated after add seats subscription is created in get "
                + "subscription by id XML api.",
            subscriptionByidXml.getLastModified(), greaterThan(lastModifiedDateBeforeAddingSeats), assertionErrorList);

        // validate Find Subscription by id json api
        final JSubscription subscriptionByIdJson =
            resource.subscriptionJson().getSubscription(originalSubscriptionId, PelicanConstants.CONTENT_TYPE);
        AssertCollector.assertThat("Subscription id is not correct in get subscription by id JSON api.",
            subscriptionByIdJson.getData().getId(), equalTo(originalSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Last modified date is not updated after add seats subscription is created in get subscription"
                + " by id JSON api.",
            subscriptionByIdJson.getData().getLastModified(), greaterThan(lastModifiedDateBeforeAddingSeats),
            assertionErrorList);

        final Map<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.PLAN_ID.getName(), offering.getOfferings().get(0).getId());
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), buyerUser.getExternalKey());

        // validate Find Subscriptions json api
        final JSubscriptions subscriptionsJson =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);
        AssertCollector.assertThat("Subscription id is not correct in get subscriptions JSON api.",
            subscriptionsJson.getData().getSubscriptions().get(0).getId(), equalTo(originalSubscriptionId),
            assertionErrorList);
        AssertCollector.assertThat(
            "Last modified date is not updated after add seats subscription is created in get subscriptions JSON api.",
            subscriptionsJson.getData().getSubscriptions().get(0).getLastModified(),
            greaterThan(lastModifiedDateBeforeAddingSeats), assertionErrorList);

        // validate Find Subscriptions XML api
        final Subscriptions subscriptionsXml =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);
        AssertCollector.assertThat("Subscription id is not correct in get subscriptions XML api.",
            subscriptionsXml.getSubscriptions().get(0).getId(), equalTo(originalSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Last modified date is not updated after add seats subscription is created in get subscriptions XML api.",
            subscriptionsXml.getSubscriptions().get(0).getLastModified(),
            greaterThan(lastModifiedDateBeforeAddingSeats), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests AddedToSubscriptionIdField in all 4 get subscriptions api. AddedToSubscriptionIdField is
     * populated when add seats order is created with promo id.
     *
     */
    @Test
    public void testAddedToSubscriptionIdFieldForAddSeatsOrder() {
        final Offerings offering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId = offering.getIncluded().getPrices().get(0).getId();
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 4);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());
        final String originalSubscriptionId = resource.purchaseOrder().getById(purchaseOrder.getId()).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final JPromotion nonStoreAmountDiscountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(offering), promotionUtils.getRandomPromoCode(), false,
            Status.ACTIVE, null, "10.0", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        final String nonStoreAmountDiscountPromoId = nonStoreAmountDiscountPromo.getData().getId();
        // Map containing Subscription Id, Price Id and Quantity
        final Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), originalSubscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(), priceId);
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");
        subscriptionQuantityMap.put(LineItemParams.PROMOTION_REFERENCE_ID.getValue(), nonStoreAmountDiscountPromoId);

        // create add seats order
        PurchaseOrder purchaseOrderForAddedSeats = purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(
            Arrays.asList(subscriptionQuantityMap), PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, "10", buyerUser);
        final String addSeatsPurchaseOrderId = purchaseOrderForAddedSeats.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId);
        purchaseOrderForAddedSeats = resource.purchaseOrder().getById(addSeatsPurchaseOrderId);
        final String addSeatsSubscriptionId = purchaseOrderForAddedSeats.getLineItems().getLineItems().get(0)
            .getSubscriptionQuantity().getSubscriptionQuantityResponse().getSubscriptionId();
        // If add seats order is not created then fail test immediately.
        if (addSeatsSubscriptionId == null) {
            Assert.fail("Add seats order is not created correctly.");
        }

        // validate Find Subscription by id xml api
        final Subscription subscriptionByidXml = resource.subscription().getById(addSeatsSubscriptionId);
        AssertCollector.assertThat("Subscription id is not correct in get subscription by id XML api.",
            subscriptionByidXml.getId(), equalTo(addSeatsSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Added to subscription id value is not correct for subscription id " + addSeatsSubscriptionId
                + " in get subscription by id XML api.",
            subscriptionByidXml.getAddedToSubscriptionId(), equalTo(originalSubscriptionId), assertionErrorList);

        // validate Find Subscription by id json api
        final JSubscription subscriptionByIdJson =
            resource.subscriptionJson().getSubscription(addSeatsSubscriptionId, PelicanConstants.CONTENT_TYPE);
        AssertCollector.assertThat("Subscription id is not correct in get subscription by id JSON api.",
            subscriptionByIdJson.getData().getId(), equalTo(addSeatsSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Added to subscription id value is not correct for subscription id " + addSeatsSubscriptionId
                + " in get subscription by id JSON api.",
            subscriptionByIdJson.getData().getAddedToSubscriptionId(), equalTo(originalSubscriptionId),
            assertionErrorList);

        final Map<String, String> requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.PLAN_ID.getName(), offering.getOfferings().get(0).getId());
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), buyerUser.getExternalKey());
        // validate Find Subscriptions json api
        final JSubscriptions subscriptionsJson =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);
        AssertCollector.assertThat("Subscriptions size is not correct in response",
            subscriptionsJson.getData().getSubscriptions().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Subscription id is not correct in get subscriptions JSON api.",
            subscriptionsJson.getData().getSubscriptions().get(1).getId(), equalTo(addSeatsSubscriptionId),
            assertionErrorList);
        AssertCollector.assertThat(
            "Added to subscription id value is not correct for subscription id " + addSeatsSubscriptionId
                + " in get subscriptions JSON api.",
            subscriptionsJson.getData().getSubscriptions().get(1).getAddedToSubscriptionId(),
            equalTo(originalSubscriptionId), assertionErrorList);

        // validate Find Subscriptions XML api
        final Subscriptions subscriptionsXml =
            resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE_XML);
        AssertCollector.assertThat("Subscriptions size is not correct in response",
            subscriptionsXml.getSubscriptions().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Subscription id is not correct in get subscriptions XML api.",
            subscriptionsXml.getSubscriptions().get(1).getId(), equalTo(addSeatsSubscriptionId), assertionErrorList);
        AssertCollector.assertThat(
            "Added to subscription id value is not correct for subscription id " + addSeatsSubscriptionId
                + " in get subscriptions XML api.",
            subscriptionsXml.getSubscriptions().get(1).getAddedToSubscriptionId(), equalTo(originalSubscriptionId),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    private BuyerUser addBuyerUser() {
        final String userName = RandomStringUtils.randomAlphabetic(8);
        final String userExternalKey = RandomStringUtils.randomAlphanumeric(12);
        final Map<String, String> userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), userName);
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final User user = resource.user().addUser(userRequestParam);

        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        buyerUser.setExternalKey(userExternalKey);
        return buyerUser;
    }
}
