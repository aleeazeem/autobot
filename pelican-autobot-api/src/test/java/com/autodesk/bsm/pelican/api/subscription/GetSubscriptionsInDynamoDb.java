package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.instanceOf;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient.FieldName;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItems;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Offering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Offering.OfferingRequest;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.Lists;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientException;

/**
 * This test class is created to create n number of subscriptions in any application family. It creates user first and
 * with that user it creates 6 subscriptions (2 subscriptions, 2 subscriptions with promotion and 2 trial
 * subscriptions). After the creation of subscriptions it also checks all subscriptions are stored in subscription table
 * in dynamo db. These class can be enhanced more to read the data of a subscription for particular subscription.
 *
 * @author Muhammad
 */
public class GetSubscriptionsInDynamoDb extends BaseTestData {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int TOTAL_NUMBER_OF_USERS = 1;
    private static String bicOfferingId1;
    private static String bicOfferingId2;
    private static String bicOfferingId3;
    private static User user;
    private static String trialSubscriptionOfferExternalKey;
    private HashMap<String, PromotionReferences> pricePromoReferencesMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetSubscriptionsInDynamoDb.class.getSimpleName());
    private List<String> subscriptionIds;
    private List<String> subscriptionIdsNotFound = new ArrayList<>();
    private List<String> subscriptionIdsMoreThanOneGuid = new ArrayList<>();

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = false)
    public void classSetup() {
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        user = new User();

        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOfferingId1 = bicOffering1.getIncluded().getPrices().get(0).getId();

        final Offerings bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOfferingId2 = bicOffering2.getIncluded().getPrices().get(0).getId();

        // add above offerings in promotion
        final List<Offerings> offerings = new ArrayList<>();
        offerings.add(bicOffering1);
        offerings.add(bicOffering2);
        final PromotionReferences promotionReferences = new PromotionReferences();
        final PromotionReference promotionReference = new PromotionReference();
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final JPromotion amountDiscountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), offerings, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
            null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionReference.setId(amountDiscountPromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);
        pricePromoReferencesMap = new HashMap<>();
        pricePromoReferencesMap.put(bicOffering2.getIncluded().getPrices().get(0).getId(), promotionReferences);

        final Offerings bicOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOfferingId3 = bicOffering3.getIncluded().getPrices().get(0).getId();

        final Offerings trialSubscriptionPlan =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        trialSubscriptionOfferExternalKey =
            trialSubscriptionPlan.getIncluded().getBillingPlans().get(0).getExternalKey();
        subscriptionIds = new ArrayList<>();

    }

    @AfterClass(alwaysRun = false)
    protected synchronized void printEndClassLog() {

        final String message = String.format("======########## Class End: %s ##########======", getClass().getName());
        LOGGER.info(StringUtils.repeat("=", message.length()));
        LOGGER.info(message);
        LOGGER.info(StringUtils.repeat("=", message.length()));
        LOGGER.info("Subscriptions not synced with dynamo db:" + "\n" + subscriptionIdsNotFound.toString());
        LOGGER.info("Subscriptions stored twice in dynamo db:" + "\n" + subscriptionIdsMoreThanOneGuid.toString());
    }

    @Test(enabled = false)
    public void subscriptionsCreation() {
        int purchaseOrderIndexForTest1 = 0;

        // creation of 6 subscriptions with each user.
        for (int z = 0; z < TOTAL_NUMBER_OF_USERS; z++) {
            resource = new PelicanClient(getEnvironmentVariables()).platform();
            // Add user
            final String userExternalKey = "Automation_test_" + RandomStringUtils.randomAlphanumeric(12) + "_user";
            final Map<String, String> userRequestParam = new HashMap<>();
            userRequestParam.put(UserParameter.NAME.getName(), userExternalKey);
            userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);

            final Object apiResponse = resource.user().addUser(userRequestParam);
            if (apiResponse instanceof HttpError) {
                final HttpError httpError = (HttpError) apiResponse;
                AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                    instanceOf(User.class), assertionErrorList);
            } else {
                user = (User) apiResponse;
            }

            // submit purchaseOrder with 4 line items
            final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
            priceQuantityMap.put(bicOfferingId1, 1);
            priceQuantityMap.put(bicOfferingId2, 1);
            priceQuantityMap.put(bicOfferingId3, 2);
            priceQuantityMap.put(getBicMonthlyUsPriceId(), 2);
            PurchaseOrder purchaseOrder = null;
            try {
                purchaseOrder = completePurchaseOrderWithCreditCard(priceQuantityMap, pricePromoReferencesMap, user);
            } catch (final ClassCastException e) {
                // skip
            }

            final List<String> purchaseOrderIds = new ArrayList<>();
            if (purchaseOrder.getId() != null) {
                purchaseOrderIds.add(purchaseOrder.getId());
                purchaseOrderIndexForTest1++;
            } else {
                LOGGER.info("purchase order: " + purchaseOrder.getId() + " is not submitted at index "
                    + (purchaseOrderIndexForTest1));
            }
            for (int a = 0; a < 2; a++) {
                resource.subscription().add(userExternalKey, trialSubscriptionOfferExternalKey, Currency.USD);
            }

            final HashMap<String, String> requestParametersMap = new HashMap<>();
            requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), userExternalKey);
            final JSubscriptions subscriptions =
                resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);
            final List<Subscription> totalSubscriptionsOfUser = subscriptions.getData().getSubscriptions();
            for (final Subscription eachSubscription : totalSubscriptionsOfUser) {
                subscriptionIds.add(eachSubscription.getId());
            }
        }

        // connects to amazon dynamo db client in west region region.
        final AmazonDynamoDBClient client = DynamoDBUtil.createAWSDynamoDBSubscriptonTableClient();

        List<Map<String, AttributeValue>> items;
        // check each subscription in dynamo db.
        for (final String subscriptionId : subscriptionIds) {
            items = DynamoDBUtil.getSubscriptionItemAWSDynamodb(subscriptionId, client);
            if (items.size() < 1) {
                subscriptionIdsNotFound.add(subscriptionId);
            }
            if (items.size() > 1) {
                subscriptionIdsMoreThanOneGuid.add(subscriptionId);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    private PurchaseOrder completePurchaseOrderWithCreditCard(final Map<String, Integer> priceQuantityMap,
        final Map<String, PromotionReferences> pricePromoReferencesMap, final User user) throws ClientException {
        final PurchaseOrder purchaseOrder = new PurchaseOrder();
        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());

        final LineItems newLineItems = new LineItems();
        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItems = new ArrayList<>();
        for (final String key : priceQuantityMap.keySet()) {
            final com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem lineItem =
                new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
            final Offering offering = new Offering();
            final OfferingRequest offeringRequest = new OfferingRequest();
            offeringRequest.setPriceId(key);
            offeringRequest.setQuantity(priceQuantityMap.get(key));
            offering.setOfferingRequest(offeringRequest);
            lineItem.setOffering(offering);
            if (pricePromoReferencesMap != null) {
                if (key.equals(bicOfferingId2) || key.equals(bicOfferingId1)) {
                    lineItem.setPromotionReferences(pricePromoReferencesMap.get(key));
                }
            }
            lineItems.add(lineItem);
        }

        newLineItems.setLineItems(lineItems);
        purchaseOrder.setBuyerUser(buyerUser);
        purchaseOrder.setLineItems(newLineItems);
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());
        final PaymentProfileUtils paymentProfileUtils =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        purchaseOrder.setPayment(paymentProfileUtils.getCreditCardPayment(buyerUser.getId(),
            PaymentProcessor.BLUESNAP_NAMER.getValue(), PaymentProcessor.BLUESNAP_NAMER.getValue(),
            getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()));
        final PurchaseOrder resultPO = resource.purchaseOrder().add(purchaseOrder);
        // Process the PO with PENDING command
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, resultPO.getId());
        // Process the PO with CHARGE command
        final PurchaseOrderCommand chargePurchaseOrderCommand = new PurchaseOrderCommand();
        chargePurchaseOrderCommand.setCommandType(OrderCommand.CHARGE.toString());
        return resource.processPurchaseOrder().process(chargePurchaseOrderCommand, resultPO.getId());
    }
}
