package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BillingInformation;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentMethod;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.StoredProfilePayment;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.DeclineReason;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class verifies the submit purchase Order API
 *
 * @author t_mohag
 */
public class SubmitPurchaseOrderTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int QUANTITY = 1;
    private static final int ERROR_CODE = 990002;
    private String priceIdForBasicOffering1;
    private String priceIdForBasicOffering2;
    private String priceIdForBasicOffering5;
    private String priceIdForBasicOffering6;
    private String priceIdForBasicOffering7;
    private String priceIdForSubscriptionOffering1;
    private String priceIdForSubscriptionOffering2;
    private String priceIdForSubscriptionOffering3;
    private String priceIdForSubscriptionOffering5;
    private String priceIdForBicSubscriptionOffering;
    private String priceIdForMetaSubscriptionOffering;
    // Promotion
    private JPromotion storeWideDiscountPromo;
    private JPromotion nonStoreWideDiscountPromo;
    private JPromotion bundledBasicOfferingsPromo;
    private JPromotion bundledSubscriptionOfferPromo;
    private JPromotion regularPromo;
    private JPromotion nonStoreWideDiscountAmountPromo;
    private JPromotion multiplePromo;
    private JPromotion multipleBundledPromo;
    private JPromotion activeBundledPromotionForMaxUses;
    private JPromotion activeBundledPromotionForMaxUsesPerUser;
    private Map<String, String> priceOfferingAmountMap;
    private Map<String, JPromotion> promotionsMap;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitPurchaseOrderTest.class.getSimpleName());
    private BuyerUser buyerUser;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminTool = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminTool.login();

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        // Initialize maps
        priceOfferingAmountMap = new HashMap<>();
        promotionsMap = new HashMap<>();

        /*
         * Create Basic Offerings and Subscription Offers for Promotions for Submit PO for Bundled Promo
         */
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUk(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PHYSICAL_MEDIA, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering3 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering4 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering5 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering6 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PHYSICAL_MEDIA, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering7 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.CURRENCY, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);

        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings subscriptionMonthlyOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings subscriptionOffering3 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings subscriptionOffering4 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings subscriptionOffering5 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings metaSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        priceIdForBasicOffering1 = basicOffering1.getIncluded().getPrices().get(0).getId();
        priceIdForBasicOffering2 = basicOffering2.getIncluded().getPrices().get(0).getId();
        final String priceIdForBasicOffering3 = basicOffering3.getIncluded().getPrices().get(0).getId();
        final String priceIdForBasicOffering4 = basicOffering4.getIncluded().getPrices().get(0).getId();
        priceIdForBasicOffering5 = basicOffering5.getIncluded().getPrices().get(0).getId();
        priceIdForBasicOffering6 = basicOffering6.getIncluded().getPrices().get(0).getId();
        priceIdForBasicOffering7 = basicOffering7.getIncluded().getPrices().get(0).getId();

        // Added to PriceId/Amount Map
        priceOfferingAmountMap.put(priceIdForBasicOffering1,
            basicOffering1.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBasicOffering2,
            basicOffering2.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBasicOffering3,
            basicOffering3.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBasicOffering4,
            basicOffering4.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBasicOffering5,
            basicOffering5.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBasicOffering6,
            basicOffering6.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBasicOffering7,
            basicOffering7.getIncluded().getPrices().get(0).getAmount());

        priceIdForSubscriptionOffering1 = subscriptionOffering1.getIncluded().getPrices().get(0).getId();
        priceIdForSubscriptionOffering2 = subscriptionOffering2.getIncluded().getPrices().get(0).getId();
        priceIdForSubscriptionOffering3 = subscriptionOffering3.getIncluded().getPrices().get(0).getId();
        final String priceIdForSubscriptionOffering4 = subscriptionOffering4.getIncluded().getPrices().get(0).getId();
        priceIdForSubscriptionOffering5 = subscriptionOffering5.getIncluded().getPrices().get(0).getId();
        priceIdForBicSubscriptionOffering = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();
        priceIdForMetaSubscriptionOffering = metaSubscriptionOffering.getIncluded().getPrices().get(0).getId();
        final String priceIdForSubscriptionMonthlyOffering =
            subscriptionMonthlyOffering.getIncluded().getPrices().get(0).getId();

        // Added to PriceId/Amount Map
        priceOfferingAmountMap.put(priceIdForSubscriptionOffering1,
            subscriptionOffering1.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForSubscriptionOffering2,
            subscriptionOffering2.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForSubscriptionOffering3,
            subscriptionOffering3.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForSubscriptionOffering4,
            subscriptionOffering4.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForSubscriptionOffering5,
            subscriptionOffering5.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForBicSubscriptionOffering,
            bicSubscriptionOffering.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForMetaSubscriptionOffering,
            metaSubscriptionOffering.getIncluded().getPrices().get(0).getAmount());
        priceOfferingAmountMap.put(priceIdForSubscriptionMonthlyOffering,
            subscriptionMonthlyOffering.getIncluded().getPrices().get(0).getAmount());

        // Promotion 1 : Bundled storewide Discount Percentage Promotion
        // creation
        final List<BundlePromoOfferings> offeringsForstoreWideDiscountPromo = new ArrayList<>();
        offeringsForstoreWideDiscountPromo.add(promotionUtils.createBundlePromotionOffering(basicOffering1, 1, true));
        offeringsForstoreWideDiscountPromo
            .add(promotionUtils.createBundlePromotionOffering(subscriptionOffering1, 1, true));

        storeWideDiscountPromo = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUk()), offeringsForstoreWideDiscountPromo, promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, "10", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("storeWideDiscountPromo id: " + storeWideDiscountPromo.getData().getId());

        // Add to Map
        promotionsMap.put(storeWideDiscountPromo.getData().getId(), storeWideDiscountPromo);

        // Promotion 2 : Bundled non storewide Discount Percentage Promotion
        final List<BundlePromoOfferings> offeringsForNonStoreWideDiscountPromo = new ArrayList<>();
        offeringsForNonStoreWideDiscountPromo
            .add(promotionUtils.createBundlePromotionOffering(basicOffering1, 1, true));
        offeringsForNonStoreWideDiscountPromo
            .add(promotionUtils.createBundlePromotionOffering(subscriptionOffering1, 1, true));

        nonStoreWideDiscountPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUk()),
                offeringsForNonStoreWideDiscountPromo, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null,
                "100", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("nonStoreWideDiscountPromo id: " + nonStoreWideDiscountPromo.getData().getId());

        // Add to Map
        promotionsMap.put(nonStoreWideDiscountPromo.getData().getId(), nonStoreWideDiscountPromo);

        // Promotion 3 : Bundled basic offering storewide Discount Percentage
        // Promotion
        final List<BundlePromoOfferings> offeringsForbundledBasicOfferingsPromo = new ArrayList<>();
        offeringsForbundledBasicOfferingsPromo
            .add(promotionUtils.createBundlePromotionOffering(basicOffering2, 3, true));

        bundledBasicOfferingsPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsForbundledBasicOfferingsPromo, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "5.5",
                null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("bundledBasicOfferingsPromo id: " + bundledBasicOfferingsPromo.getData().getId());

        // Add to Map
        promotionsMap.put(bundledBasicOfferingsPromo.getData().getId(), bundledBasicOfferingsPromo);

        // Promotion 4 : Bundled Subscription offer storewide Discount
        // Percentage Promotion
        final List<BundlePromoOfferings> offeringsForBundledSubscriptionOfferPromo = new ArrayList<>();
        offeringsForBundledSubscriptionOfferPromo
            .add(promotionUtils.createBundlePromotionOffering(subscriptionOffering2, 2, true));

        bundledSubscriptionOfferPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsForBundledSubscriptionOfferPromo, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE,
                "4", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("bundledSubscriptionOfferPromo id: " + bundledSubscriptionOfferPromo.getData().getId());

        // Add to Map
        promotionsMap.put(bundledSubscriptionOfferPromo.getData().getId(), bundledSubscriptionOfferPromo);

        // Promotion 5 : Regular Subscription offer non storewide Discount
        // Amount Promotion
        regularPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(subscriptionOffering2), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null,
            "50", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        LOGGER.info("regularPromo id: " + regularPromo.getData().getId());

        // Add to Map
        promotionsMap.put(regularPromo.getData().getId(), regularPromo);

        // Promotion 6 : Bundled non storewide Discount Amount Promotion
        final List<BundlePromoOfferings> offeringsFornonStoreWideDiscountAmountPromo = new ArrayList<>();
        offeringsFornonStoreWideDiscountAmountPromo
            .add(promotionUtils.createBundlePromotionOffering(basicOffering2, 1, true));
        offeringsFornonStoreWideDiscountAmountPromo
            .add(promotionUtils.createBundlePromotionOffering(subscriptionOffering3, 2, true));

        nonStoreWideDiscountAmountPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsFornonStoreWideDiscountAmountPromo, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "8", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("nonStoreWideDiscountAmountPromo id: " + nonStoreWideDiscountAmountPromo.getData().getId());

        // Add to Map
        promotionsMap.put(nonStoreWideDiscountAmountPromo.getData().getId(), nonStoreWideDiscountAmountPromo);

        // Promotion 7 : Promotion to link left over item with another promotion
        final List<BundlePromoOfferings> offeringsFormultiplePromo = new ArrayList<>();
        offeringsFormultiplePromo.add(promotionUtils.createBundlePromotionOffering(basicOffering5, 2, true));
        offeringsFormultiplePromo.add(promotionUtils.createBundlePromotionOffering(subscriptionOffering5, 2, true));

        multiplePromo = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), offeringsFormultiplePromo, promotionUtils.getRandomPromoCode(), true,
            Status.ACTIVE, "10", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("multiplePromo id: " + multiplePromo.getData().getId());

        // Add to Map
        promotionsMap.put(multiplePromo.getData().getId(), multiplePromo);

        // Promotion 8 : More promotions to Link left over item to other
        // promotion
        final List<BundlePromoOfferings> offeringsForMultipleBundledPromo = new ArrayList<>();
        offeringsForMultipleBundledPromo.add(promotionUtils.createBundlePromotionOffering(basicOffering6, 1, true));
        offeringsForMultipleBundledPromo
            .add(promotionUtils.createBundlePromotionOffering(subscriptionOffering5, 1, true));

        multipleBundledPromo = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), offeringsForMultipleBundledPromo, promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, "10", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("multipleBundledPromo id: " + multipleBundledPromo.getData().getId());
        // Add to Map
        promotionsMap.put(multipleBundledPromo.getData().getId(), multipleBundledPromo);

        // Promotion 9 : Promotion to validate error for exceeding max use
        // count.
        final List<BundlePromoOfferings> offeringsForActiveBundledPromotionForMaxUses = new ArrayList<>();
        offeringsForActiveBundledPromotionForMaxUses
            .add(promotionUtils.createBundlePromotionOffering(basicOffering7, 1, true));
        offeringsForActiveBundledPromotionForMaxUses
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering, 1, true));
        offeringsForActiveBundledPromotionForMaxUses
            .add(promotionUtils.createBundlePromotionOffering(metaSubscriptionOffering, 1, true));

        activeBundledPromotionForMaxUses =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsForActiveBundledPromotionForMaxUses, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "10", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, "1", null);
        LOGGER.info("activeBundledPromotionForMaxUses id: " + multipleBundledPromo.getData().getId());

        // Add to Map
        promotionsMap.put(activeBundledPromotionForMaxUses.getData().getId(), activeBundledPromotionForMaxUses);

        // Promotion 10 : Promotion to validate error for exceeding max uses per
        // getUser() count.
        final List<BundlePromoOfferings> offeringsForActiveBundledPromotionForMaxUsesPerUser = new ArrayList<>();
        offeringsForActiveBundledPromotionForMaxUsesPerUser
            .add(promotionUtils.createBundlePromotionOffering(basicOffering7, 1, true));
        offeringsForActiveBundledPromotionForMaxUsesPerUser
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering, 1, true));
        offeringsForActiveBundledPromotionForMaxUsesPerUser
            .add(promotionUtils.createBundlePromotionOffering(metaSubscriptionOffering, 1, true));

        activeBundledPromotionForMaxUsesPerUser =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsForActiveBundledPromotionForMaxUses, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "10", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, "1");
        LOGGER.info(
            "activeBundledPromotionForMaxUsesForUser id: " + activeBundledPromotionForMaxUsesPerUser.getData().getId());
        // Add to Map
        promotionsMap.put(activeBundledPromotionForMaxUsesPerUser.getData().getId(),
            activeBundledPromotionForMaxUsesPerUser);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Method to test SubmitPurchaseOrder with single line item and credit card as the payment type
     */
    @Test
    public void testSubmitPOWithSingleLineItemWithCreditCardSuccess() {

        PurchaseOrder purchaseOrderForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard.getId());
        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard.getId());
        // get purchase order api response
        purchaseOrderForBicCreditCard = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard.getId());
        AssertCollector.assertThat("Incorrect order state of the purchase order",
            purchaseOrderForBicCreditCard.getOrderState(), equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect priceid for the first line item", purchaseOrderForBicCreditCard.getLineItems().getLineItems()
                .get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test Purchase Order for Offering Request throws an Error for Zero Quantity.
     */
    @Test
    public void testErrorForOfferingRequestPurchaseWithZeroQuantity() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), 0);
        final HttpError httpError = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap,
            false, PaymentType.CREDIT_CARD, null, null, null);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SUBSCRIPTION_ZERO_OR_NEGATIVE_QUANTITY_ERROR_MESSAGE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test Payment Failure for CreditCard
     */
    @Test
    public void testPaymentFailureEmailCreditCard() {

        PurchaseOrder purchaseOrderForBicCreditCard = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY);
        // process the purchase order to 'pending' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard.getId());

        // process the purchase order to 'charge' state
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderForBicCreditCard.getId());

        // get purchase order api response
        purchaseOrderForBicCreditCard = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard.getId());

        AssertCollector.assertThat("Incorrect order state of the purchase order",
            purchaseOrderForBicCreditCard.getOrderState(), equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        AssertCollector.assertThat(
            "Incorrect priceid for the first line item", purchaseOrderForBicCreditCard.getLineItems().getLineItems()
                .get(0).getOffering().getOfferingRequest().getPriceId(),
            equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder with multi line item and credit card as the payment type
     */
    @Test
    public void testSubmitPOWithMultiLineItemWithCreditCardSuccess() {
        final LinkedHashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);
        priceQuantityMap.put(getBicYearlyUsPriceId(), QUANTITY);

        final Object entity =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(PurchaseOrder.class), assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        } else {
            PurchaseOrder purchaseOrderForBicCreditCard = (PurchaseOrder) entity;
            // process the purchase order to 'pending' state
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderForBicCreditCard.getId());
            // process the purchase order to 'charge' state
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForBicCreditCard.getId());
            // get purchase order api response
            purchaseOrderForBicCreditCard = resource.purchaseOrder().getById(purchaseOrderForBicCreditCard.getId());
            AssertCollector.assertThat("Incorrect store external key for the purchase order",
                purchaseOrderForBicCreditCard.getStoreExternalKey(), equalTo(getStoreUs().getExternalKey()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect order state of the purchase order",
                purchaseOrderForBicCreditCard.getOrderState(), equalTo(OrderState.CHARGED.toString()),
                assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect priceid for the first line item", purchaseOrderForBicCreditCard.getLineItems().getLineItems()
                    .get(0).getOffering().getOfferingRequest().getPriceId(),
                equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect priceid for the second line item", purchaseOrderForBicCreditCard.getLineItems()
                    .getLineItems().get(1).getOffering().getOfferingRequest().getPriceId(),
                equalTo(getBicYearlyUsPriceId()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder without a buyer getUser() email (buyer email is mandatory after US10212)
     */
    @Test(dataProvider = "getInvalidEmailData")
    public void testErrorSubmitPOWithoutBuyerUserEmail(final String testDescription, final String buyerEmail) {

        LOGGER.info("Test Scenario Email Validation in SubmitPO with :" + testDescription);
        // setting an empty email for buyer getUser()
        buyerUser.setEmail(buyerEmail);
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (entity instanceof PurchaseOrder) {
            AssertCollector.assertThat("Email Validation Not working!! ", entity, instanceOf(HttpError.class),
                assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        } else {
            final HttpError emailValidationError = (HttpError) entity;

            AssertCollector.assertThat("Incorrect Error Message", emailValidationError.getErrorMessage(),
                equalTo("Buyer user email address must exist in purchase order."), assertionErrorList);
            AssertCollector.assertThat("Incorrect Error Status", emailValidationError.getErrorCode(), equalTo(990002),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Error Status", emailValidationError.getStatus(),
                equalTo(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        }
        // setting back the getUser() email to make sure all futures tests in
        // this class works fine
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Credit Card in Charged state with Payment Processor If payport sends only payment
     * processor in PO XML, Pelican adds config id
     */
    @Test
    public void testSubmitPurchaseOrderWithPaymentProcessorAndCreditCard() {

        // submit purchase order
        final PurchaseOrder purchaseOrderWithPaymentProcessor =
            purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(PaymentType.CREDIT_CARD,
                getBicMonthlyUsPriceId(), buyerUser, QUANTITY, null, null, null,
                PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentProcessor.BLUESNAP_EMEA.getValue());
        final String pOIdWithPaymentProcessor = purchaseOrderWithPaymentProcessor.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, pOIdWithPaymentProcessor);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, pOIdWithPaymentProcessor);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(pOIdWithPaymentProcessor);// api

        AssertCollector.assertThat("Incorrect Payment Processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Config Id", purchaseOrder.getPayment().getConfigId(),
            equalTo(getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Paypal in Charged state with Payment Processor If payport sends only payment
     * processor in PO XML, Pelican adds config id
     */
    @Test
    public void testSubmitPurchaseOrderWithPaymentProcessorAndPaypal() {

        // submit purchase order
        final PurchaseOrder purchaseOrderWithPaymentProcessor = purchaseOrderUtils
            .submitPurchaseOrderWithPaymentProcessorAndConfigId(PaymentType.PAYPAL, getBicMonthlyUsPriceId(), buyerUser,
                QUANTITY, null, PaymentProcessor.PAYPAL_EMEA.getValue(), null, null, null);
        final String pOIdWithPaymentProcessor = purchaseOrderWithPaymentProcessor.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, pOIdWithPaymentProcessor);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, pOIdWithPaymentProcessor);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(pOIdWithPaymentProcessor);// api

        AssertCollector.assertThat("Incorrect Payment Processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.PAYPAL_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Config Id", purchaseOrder.getPayment().getConfigId(),
            equalTo(getEnvironmentVariables().getPaypalEmeaPaymentGatewayId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Credit Card in Charged state with config id If payport sends only payment config id
     * in PO XML, Pelican adds Payment Processor
     */

    @Test
    public void testSubmitPurchaseOrderWithConfigIdAndCreditCard() {

        // submit purchase order
        final PurchaseOrder purchaseOrderWithPaymentProcessor = purchaseOrderUtils
            .submitPurchaseOrderWithPaymentProcessorAndConfigId(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(),
                buyerUser, QUANTITY, null, null, getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId(), null,
                PaymentProcessor.BLUESNAP_EMEA.getValue());
        final String pOIdWithPaymentProcessor = purchaseOrderWithPaymentProcessor.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, pOIdWithPaymentProcessor);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, pOIdWithPaymentProcessor);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(pOIdWithPaymentProcessor);// api

        AssertCollector.assertThat("Incorrect Payment Processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.BLUESNAP_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Config Id", purchaseOrder.getPayment().getConfigId(),
            equalTo(getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Paypal in Charged state with Config Id If payport sends only Config Id in PO XML,
     * Pelican adds Payment Processor
     */
    @Test
    public void testSubmitPurchaseOrderWithPaymentConfigIdAndPaypal() {

        // submit purchase order
        final PurchaseOrder purchaseOrderWithPaymentProcessor = purchaseOrderUtils
            .submitPurchaseOrderWithPaymentProcessorAndConfigId(PaymentType.PAYPAL, getBicMonthlyUsPriceId(), buyerUser,
                QUANTITY, getEnvironmentVariables().getPaypalEmeaPaymentGatewayId(), null, null, null, null);
        final String pOIdWithPaymentProcessor = purchaseOrderWithPaymentProcessor.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, pOIdWithPaymentProcessor);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, pOIdWithPaymentProcessor);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(pOIdWithPaymentProcessor);// api

        AssertCollector.assertThat("Incorrect Payment Processor", purchaseOrder.getPayment().getPaymentProcessor(),
            equalTo(PaymentProcessor.PAYPAL_EMEA.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Config Id", purchaseOrder.getPayment().getConfigId(),
            equalTo(getEnvironmentVariables().getPaypalEmeaPaymentGatewayId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Meta Purchase order with Credit Card with config id & Payment Processor which do not match Error is produced
     * while submitting PO
     */
    @Test
    public void testErrorWhenConfigIdAndPaymentProcessorDoNotMatchForCreditCard() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.CREDIT_CARD, getMetaMonthlyUsPriceId(), buyerUser, QUANTITY, null, null,
            getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_NAMER.getValue(),
            PaymentProcessor.BLUESNAP_NAMER.getValue());
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage().split("\\.")[0],
            equalTo("Payment Config Id and PaymentProcessor do not match"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Meta Purchase order with Paypal with config id & Payment Processor which do not match Error is produced
     * while submitting PO
     */
    @Test
    public void testErrorWhenConfigIdAndPaymentProcessorDoNotMatchForPaypal() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.PAYPAL, getMetaMonthlyUsPriceId(), buyerUser, QUANTITY,
            getEnvironmentVariables().getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_NAMER.getValue(), null,
            null, null);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage().split("\\.")[0],
            equalTo("Payment Config Id and PaymentProcessor do not match"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Credit card with invalid config id Error is produced while submitting PO
     */
    @Test
    public void testErrorWhenPaymentConfigIdIsInvalidForCreditCard() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY, null, null, "1000000", null,
            PaymentProcessor.BLUESNAP_NAMER.getValue());
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage().split(":")[0],
            equalTo("Payment Configuration not found for id"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Paypal with invalid config id Error is produced while submitting PO
     */
    @Test
    public void testErrorWhenPaymentConfigIdIsInvalidForPaypal() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.PAYPAL, getBicMonthlyUsPriceId(), buyerUser, QUANTITY, "50000", null, null, null, null);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage().split(":")[0],
            equalTo("Payment Configuration not found for id"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Credit card with invalid PaymentProcessor Error is produced while submitting PO
     */
    @Test
    public void testErrorWhenPaymentProcessorIsInvalidForCreditCard() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY, null, null, null, "Autodesk",
            PaymentProcessor.BLUESNAP_NAMER.getValue());
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo("Payment Configuration not found for payment processor: Autodesk"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Paypal with invalid PaymentProcessor Error is produced while submitting PO
     */
    @Test
    public void testErrorWhenPaymentProcessorIsInvalidForPaypal() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.PAYPAL, getBicMonthlyUsPriceId(), buyerUser, QUANTITY, null, "Autodesk", null, null, null);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo("Payment Configuration not found for payment processor: Autodesk"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test BIC Purchase order with Credit card When PaymentProcessor Is Not Same In Payment And PaymentProfile Error is
     * produced while submitting PO
     */
    @Test
    public void testErrorWhenPaymentProcessorIsNotSameInPaymentAndPaymentProfileWithCreditCard() {

        // submit purchase order
        final HttpError httpError = purchaseOrderUtils.submitPurchaseOrderWithPaymentProcessorAndConfigId(
            PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, QUANTITY, null, null,
            getEnvironmentVariables().getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
            PaymentProcessor.BLUESNAP_NAMER.getValue());
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo("The paymentProcessor in PaymentGatewayConfig does not match paymentProcessor "
                + "in specified StoredPaymentProfile"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Meta Purchase order with Paypal When PaymentProcessor Is Not Same In Payment And PaymentProfile Error is
     * produced while processing "AUTHORIZE" PO to "PENDING"
     */
    @Test
    public void testErrorWhenPaymentProcessorIsNotSameInPaymentAndPaymentProfileWithPaypal() {

        // submit purchase order
        final PurchaseOrder purchaseOrderForMeta = purchaseOrderUtils
            .submitPurchaseOrderWithPaymentProcessorAndConfigId(PaymentType.PAYPAL, getMetaMonthlyUsPriceId(),
                buyerUser, QUANTITY, getEnvironmentVariables().getPaypalNamerPaymentGatewayId(),
                PaymentProcessor.PAYPAL_NAMER.getValue(), null, null, null);
        final HttpError httpError = purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser,
            purchaseOrderForMeta.getId(), getEnvironmentVariables().getPaypalNamerPaymentGatewayId(),
            PaymentProcessor.PAYPAL_NAMER.getValue(), PaymentProcessor.PAYPAL_EMEA.getValue());
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo("The paymentProcessor in PaymentGatewayConfig does not match paymentProcessor "
                + "in specified StoredPaymentProfile"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the error for decline reason if order command is other than declined. If Declined is not
     * provided as order command than pruchase order will not be processed and give an error.
     */
    @Test(dataProvider = "dataForDeclineReason")
    public void testErrorWhenDeclineReasonIsProvidedWithOrderStateOtherThanDecline(final DeclineReason declineReason) {
        final PurchaseOrder purchaseOrderCreatedForBic = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);
        // process purchase order with pending and charge commands
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderCreatedForBic.getId());

        final HttpError httpError = purchaseOrderUtils.processPurchaseOrderWithDeclineReason(OrderCommand.CHARGE,
            purchaseOrderCreatedForBic.getId(), declineReason);

        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(OrderCommand.CHARGE + " doesnot supports 'declineReason' "), assertionErrorList);
        AssertCollector.assertThat("Incorrect status code", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", httpError.getErrorCode(), is(ERROR_CODE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the error for decline reason if order command is other than declined. If Declined is not
     * provided as order command than pruchase order will not be processed and give an error.
     */
    @Test(dataProvider = "dataForDeclineReason")
    public void testDeclinedReasonWithOrderCommandDeclined(final DeclineReason declineReason) {
        final PurchaseOrder purchaseOrderCreatedForBic = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, getBicMonthlyUsPriceId(), buyerUser, 1);

        // process purchase order with pending and charge commands
        final PurchaseOrder purchaseOrderProcessForBic =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderCreatedForBic.getId());
        final PurchaseOrder purchaseOrderResponse = purchaseOrderUtils.processPurchaseOrderWithDeclineReason(
            OrderCommand.DECLINE, purchaseOrderProcessForBic.getId(), declineReason);

        AssertCollector.assertThat("Decline Reason is not correct", purchaseOrderResponse.getDeclineReason(),
            equalTo(declineReason.toString()), assertionErrorList);
        AssertCollector.assertThat("Order state is not Decline", purchaseOrderResponse.getOrderState(),
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for Bundled Promo with credit card as a payment method
     */
    @Test
    public void testSubmitPoForBundledPromoWithCreditCard() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering1, 5,
            storeWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering1, 5,
            storeWideDiscountPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled promotion with Paypal
     */
    @Test
    public void testSubmitPurchaseOrderForBundledPromoForWithPaypal() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering1, 2,
            storeWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering1, 2,
            storeWideDiscountPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems, PaymentType.PAYPAL, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled promo with additional fee.
     */
    @Test
    public void testSubmitPurchaseOrderForBundledPromoWithAdditionalFee() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 1,
            nonStoreWideDiscountAmountPromo.getData().getId(), "5");

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering3, 2,
            nonStoreWideDiscountAmountPromo.getData().getId(), "5");

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for the combination of Storewide and Nonstorewide promotion.
     */
    @Test
    public void testSubmitPurchaseOrderForStoredWideAndNonStoredWide() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering1, 2,
            storeWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering1, 1,
            nonStoreWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering1, 2,
            storeWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem4 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering1, 1,
            nonStoreWideDiscountPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3, lineitem4);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to test SubmitPurchaseOrder for bundled basic offerings only
     */
    @Test
    public void testSubmitPoForBundledBasicOfferingsPromo() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 3,
            bundledBasicOfferingsPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 3,
            bundledBasicOfferingsPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for basic offering and throws an error on insufficient quantity.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsErrorOnInsufficientQuantity() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 3,
            bundledBasicOfferingsPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 2,
            bundledBasicOfferingsPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final HttpError httpError = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        final String expectedErrorMessage =
            "com.twofish.tempest.mrule.InsufficientQuantityForBundledPromotionException";
        AssertCollector.assertThat("Error Message Not coming on Insufficient Quantity",
            httpError.getErrorMessage().split(":")[0], equalTo(expectedErrorMessage), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled subscription Refunded Successfully.
     */
    @Test
    public void testSubmitPurchaseOrderForRefund() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering2, 2,
            bundledSubscriptionOfferPromo.getData().getId(), null);

        final LineItem lineitem2 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering2, 1, null, null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        final PurchaseOrder savedPurchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect order state", savedPurchaseOrder.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled subscription and basic offering with promotions and non
     * promotional item.
     */
    @Test
    public void testSubmitPurchaseOrderForBundledPromoForPromotionalAndNonPromotionalItem() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering1, 1,
            storeWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering1, 1,
            storeWideDiscountPromo.getData().getId(), null);

        final LineItem lineitem3 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering1, 1, null, null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for Regular promo with bundled promotion
     */
    @Test
    public void testSubmitPurchaseOrderForBundledAndNonBundledPromo() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 3,
            bundledBasicOfferingsPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering2, 1,
            regularPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled storewide and non storewide promotion Throws an error for missing
     * items.
     */
    @Test
    public void testErrorSubmitPurchaseOrderForMissingItem() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering3, 1,
            nonStoreWideDiscountAmountPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering2, 2,
            regularPromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final HttpError httpError = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        final String expectedErrorMsg = "com.twofish.tempest.mrule.MissingItemForBundledPromotionException";
        AssertCollector.assertThat("Error Message Not coming on Missing Item",
            httpError.getErrorMessage().split(":")[0], equalTo(expectedErrorMsg), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled non storewide promotion
     */
    @Test
    public void testSubmitPurchaseOrderForBundledNonStorewidePromo() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 1,
            nonStoreWideDiscountAmountPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering3, 2,
            nonStoreWideDiscountAmountPromo.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 1, null, null);

        final LineItem lineitem4 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering3, 1, null, null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3, lineitem4);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        // Perform Asserts using Helper
        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify Submit PO throws an error for Insufficient Quntity in Cart for applied.
     */
    @Test
    public void testSubmitPurchaseOrderForInsufficientQuantityThrowsError() {

        final String nonStorePromoId = nonStoreWideDiscountAmountPromo.getData().getId();
        // Create LineItem
        final LineItem lineitem1 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 1, nonStorePromoId, null);

        final LineItem lineitem2 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering3, 1, nonStorePromoId, null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final HttpError httpError = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message", httpError.getErrorMessage(),
            equalTo(String.format("com.twofish.tempest.mrule.InsufficientQuantityForBundledPromotionException: "
                + "Line Item with promotion ID: %s does not have the minimum quantity " + "required for the purchase.",
                nonStorePromoId)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for bundled subscription for CHARGBACK.
     */
    @Test
    public void testSubmitPurchaseOrderForChargeBack() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering2, 2,
            bundledSubscriptionOfferPromo.getData().getId(), null);

        final LineItem lineitem2 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering2, 1, null, null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);
        final PurchaseOrder savedPo = resource.purchaseOrder().getById(purchaseOrderId);

        AssertCollector.assertThat("Incorrect order state", savedPo.getOrderState(),
            equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for Left over item can be linked to other promotion
     */
    @Test
    public void testSubmitPurchaseOrderForLeftOverItemLinkedToOtherPromotion() {

        // Create LineItem
        final LineItem lineItem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering5, 1,
            multipleBundledPromo.getData().getId(), null);

        final LineItem lineItem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering6, 1,
            multipleBundledPromo.getData().getId(), null);

        final LineItem lineItem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering5, 2,
            multiplePromo.getData().getId(), null);

        final LineItem lineItem4 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering5, 2,
            multiplePromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineItem1, lineItem2, lineItem3, lineItem4);

        // submit a purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        HelperForPurchaseOrder.assertionForLineItemCalculation(lineItems, purchaseOrder, priceOfferingAmountMap,
            promotionsMap, 0.00, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test SubmitPurchaseOrder for Left over item linked to other promotion throws an error on missing line
     * item.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsErrorOnMissingLineItem() {

        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering5, 1,
            multipleBundledPromo.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering6, 1,
            multipleBundledPromo.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering5, 2, null, null);

        final LineItem lineitem4 = purchaseOrderUtils.createOfferingLineItem(priceIdForSubscriptionOffering5, 2,
            multiplePromo.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3, lineitem4);

        // submit a purchase order
        final HttpError httpError = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        final String expectedErrorMessage = "com.twofish.tempest.mrule.MissingItemForBundledPromotionException";
        AssertCollector.assertThat("Error Message Not comming on Missing Line Item",
            httpError.getErrorMessage().split(":")[0], equalTo(expectedErrorMessage), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to validate error while exceeding set promotion use count.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsErrorOnExceedingMaxUse() {
        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering7, 1,
            activeBundledPromotionForMaxUses.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicSubscriptionOffering, 1,
            activeBundledPromotionForMaxUses.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForMetaSubscriptionOffering, 1,
            activeBundledPromotionForMaxUses.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // submit a purchase order in order to get Times Used count as one.
        purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems, PaymentType.CREDIT_CARD, buyerUser);

        // Submit second purchase order in order to catch error.
        final HttpError httpError = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        final String expectedErrorMessage = "com.twofish.tempest.mrule.PromotionExceedsMaxUsesException";
        AssertCollector.assertThat("Error Message Not coming on Exceeding Max Use",
            httpError.getErrorMessage().split(":")[0], equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to validate error while exceeding set promotion use per getUser() count.
     */
    @Test
    public void testSubmitPurchaseOrderThrowsErrorOnExceedingMaxUsePerUser() {
        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering7, 1,
            activeBundledPromotionForMaxUsesPerUser.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicSubscriptionOffering, 1,
            activeBundledPromotionForMaxUsesPerUser.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForMetaSubscriptionOffering, 1,
            activeBundledPromotionForMaxUsesPerUser.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // submit a purchase order in order to get Times Used count as one.
        purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems, PaymentType.CREDIT_CARD, buyerUser);

        // Submit second purchase order in order to catch error.
        final HttpError httpError = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        final String expectedErrorMessage = "com.twofish.tempest.mrule.UserExceedsMaxPromotionUsesException";
        AssertCollector.assertThat("Incorrect Error message when Max use count per user exceeded for promotion",
            httpError.getErrorMessage().split(":")[0], equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testDeclineReasonInPurchaseOrderApi
     *
     * @return Object[][]
     */
    @SuppressWarnings("unused")
    @DataProvider(name = "dataForDeclineReason")
    private Object[][] getTestDataForDeclinedOrder() {
        return new Object[][] { { DeclineReason.OTHER_REASON }, { DeclineReason.PAYMENT_PROCESSOR_DECLINED },
                { DeclineReason.EXPORT_CONTROL_BLOCKED }, { DeclineReason.EXPORT_CONTROL_UNRESOLVED }, };
    }

    /**
     * DataProvider to send invalid emails to submitPO email
     *
     * @return data for test in two d array
     */
    @DataProvider(name = "getInvalidEmailData")
    private static Object[][] getInvalidEmailData() {

        return new Object[][] { { "Empty Email", "" }, { "Null Email", null } };
    }

    /**
     * Verify Submit PO throws an error when price is expired
     */
    @Test
    public void testErrorForSubmitOfferingPurchaseOrderWithExpiredPrice() {

        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Creating price id for BIC and Meta.
        final String priceIdForBic = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceIdForBic), getEnvironmentVariables());

        final HttpError httpError = (HttpError) purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            priceIdForBic, buyerUser, 2, OrderCommand.AUTHORIZE);

        AssertCollector.assertThat("Incorrect Error Status", httpError.getStatus(), is(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message ", httpError.getErrorMessage(),
            equalTo(String.format(PelicanErrorConstants.PRICE_ID_NOT_ACTIVE_ERROR, priceIdForBic)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Process PO does not throw an error when price is expired
     */
    @Test
    public void testSuccessForProcessOfferingPurchaseOrderWithExpiredPrice() {

        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Creating price id for BIC and Meta.
        final String priceIdForBic = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, 4);

        // get one date to change Subscriptions next billing date
        final String getDateInPast = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(5),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_TIME_FORMAT);

        // Changing DB for subscription_price table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_START_AND_END_DATE, getDateInPast,
            getDateInPast, priceIdForBic), getEnvironmentVariables());

        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrder.getId());

        AssertCollector.assertThat("Incorrect response", purchaseOrder, instanceOf(PurchaseOrder.class),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify Processing PO successfully for PENDING, CHARGE & REFUND when Offer External Key for BIC
     * Subscription Plan got changed after submitting PO.
     */
    @Test
    public void testProcessPurchaseOrderWithUpdatedOfferExternalKey() {
        // Add New Offering.
        final Offerings offering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        final String offeringId = offering.getOfferings().get(0).getId();

        // Submit New Acquisition order
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 1).getId();

        // Update Offer External Key.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_OFFER_EXTERNAL_KEY,
            RandomStringUtils.randomAlphanumeric(6), offeringId), getEnvironmentVariables());

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        AssertCollector.assertThat("Incorrect Purchase Order Status",
            resource.purchaseOrder().getById(purchaseOrderId).getOrderState(), equalTo(OrderState.CHARGED.toString()),
            assertionErrorList);

        // Update Offer External Key.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_OFFER_EXTERNAL_KEY,
            RandomStringUtils.randomAlphanumeric(6), offeringId), getEnvironmentVariables());

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        AssertCollector.assertThat("Incorrect Purchase Order Status", purchaseOrder.getOrderState(),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test Method to verify Processing PO successfully for PENDING & DECLINED when Offer External Key for Meta
     * Subscription Plan got changed after submitting PO.
     */
    @Test
    public void testProcessDeclinePurchaseOrderWithUpdatedOfferExternalKey() {
        // Add New Offering.
        final Offerings offering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId = offering.getIncluded().getPrices().get(0).getId();

        final String offeringId = offering.getOfferings().get(0).getId();

        // Submit New Acquisition order
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 1).getId();

        // Update Offer External Key.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_OFFER_EXTERNAL_KEY,
            RandomStringUtils.randomAlphanumeric(6), offeringId), getEnvironmentVariables());

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderId);

        AssertCollector.assertThat("Incorrect Purchase Order Status",
            resource.purchaseOrder().getById(purchaseOrderId).getOrderState(), equalTo(OrderState.DECLINED.toString()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * test error for SEPA orders on missing either mandate Id or mandate Date field.
     */
    @Test
    public void testErrorOnMissingMandateIdFieldForSEPAPaymentMethod() {

        final BillingInformation billingInfo = new BillingInformation();
        billingInfo.setDebitType(PaymentMethod.SEPA.getValue());

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder =
            purchaseOrderUtils.createPurchaseOrderWithLineItems(getBicMonthlyUkPriceId(), buyerUser, 1, purchaseOrder);

        final PaymentProfileUtils paymentProfileUtils =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final PaymentProfile paymentProfile = paymentProfileUtils.addDirectDebitPayment(buyerUser.getId(),
            PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentMethod.SEPA, false);
        final String paymentProfileId = paymentProfile.getId();
        final StoredProfilePayment storedPaymentProfile = new StoredProfilePayment();
        storedPaymentProfile.setStoredPaymentProfileId(paymentProfileId);

        final Payment payment = new Payment();
        payment.setStoredProfilePayment(storedPaymentProfile);
        payment.setPaymentProcessor(PaymentProcessor.BLUESNAP_EMEA.getValue());
        payment.setMandateDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_TIME_WITH_ZONE));
        purchaseOrder.setPayment(payment);
        purchaseOrder.setBillingInformation(billingInfo);
        final HttpError httpError = resource.purchaseOrder().add(purchaseOrder);
        AssertCollector.assertThat("Incorrect http response", httpError.getStatus(), equalTo(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.SEPA_MANDATE_FIELD_ERROR_MSG), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
