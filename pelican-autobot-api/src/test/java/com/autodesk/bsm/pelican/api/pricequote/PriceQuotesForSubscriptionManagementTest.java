package com.autodesk.bsm.pelican.api.pricequote;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.helpers.HelperForPriceQuote;
import com.autodesk.bsm.pelican.api.helpers.PriceQuoteRequestBuilder;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JAdditionalFee;
import com.autodesk.bsm.pelican.api.pojos.json.JLineItem;
import com.autodesk.bsm.pelican.api.pojos.json.JPriceList;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionReference;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes.PriceQuoteData;
import com.autodesk.bsm.pelican.api.pojos.json.Shipping;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.api.requests.SubscriptionSyncRequest;
import com.autodesk.bsm.pelican.api.requests.SubscriptionSyncRequestBuilder;
import com.autodesk.bsm.pelican.api.requests.helper.SubscriptionSyncRequestHelper;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.PurchaseType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceQuotesForSubscriptionManagementTest extends BaseTestData {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private PromotionUtils promotionUtils;
    private Offerings bicOffering;
    private Offerings bicOffering7;
    private Offerings bicOffering8;
    private Offerings bicOffering9;
    private Offerings bicOffering10;
    private Offerings bicOfferings11;
    private Offerings bicOfferings12;
    private Offerings bicOfferings13;
    private Offerings bicOfferings14;
    private Offerings bicOfferings15;
    private Offerings bicOfferings18;
    private Offerings bicOfferings19;
    private Offerings bicOfferings20;
    private Offerings bicOfferings21;
    private JPromotion activeNonStoreWideDiscountAmountPromo1;
    private JPromotion activeNonStoreWideSupplementTimePromo3;
    private JPromotion activeStoreWideWithCashForAddSeatsPromo1;
    private JPromotion activeStoreWideWithCashForAddSeatsPromo2;
    private JPromotion activeStoreWideWithCashForAddSeatsPromo3;
    private JPromotion activeStoreWideWithCashForAddSeatsPromo4;
    private JPromotion activeNonStoreWideWithCashForAddSeatsPromo1;
    private JPromotion activeNonStoreWideWithCashForAddSeatsPromo2;
    private JPromotion activeStoreWideWithCashForAddSeatsPromo5;
    private JPromotion cancelledOnlyStoreWideWithCashForAddSeatsPromo5;
    private JPromotion activeStoreWidePercentageBicOfferingsBundlePromo;
    private JPromotion activeStoreWideWithCashForSubscriptionExtension1;
    private JPromotion activeStoreWideWithCashForSubscriptionExtension2;
    private JPromotion cancelledOnlyStoreWideWithCashForSubscriptionExtension1;
    private JPromotion cancelledOnlyStoreWideWithCashForSubscriptionExtension2;
    private static String buyerId = "";
    private PriceQuotes priceQuotes;
    private PriceQuotes priceQuotesResponse;
    private PriceQuoteData requestData;
    private List<JAdditionalFee> additionalFees;
    private List<JLineItem> lineItems;
    private Shipping shipping;
    private List<JPromotionReference> promotionReferencesList;
    private Map<Offerings, List<JPromotion>> offeringsJPromotionMap;
    private String priceIdForMetaOffering2;
    private String subscriptionIdBicCreditCardNotActive;
    private String subscriptionIdBicCreditCardNotActive1;
    private String subscriptionIdForMetaCreditCard;
    private String priceId1;
    private String priceId2;
    private String priceId3;
    private String priceId4;
    private String priceId5;
    private String priceId6;
    private String priceId7;
    private String priceId8;
    private String priceId9;
    private String priceId12;
    private String priceId13;
    private String priceId14;
    private String priceId15;
    private final int quantity = 1;
    private static final int QUANTITY = 1;
    private final int quantity1 = 2;
    private final int quantity2 = 4;
    private String subscriptionIdBicCreditCard1;
    private String subscriptionIdBicCreditCard2;
    private String subscriptionIdBicCreditCard3;
    private String subscriptionIdBicCreditCard4;
    private String subscriptionIdBicCreditCard6;
    private String subscriptionIdBicCreditCard7;
    private Subscription subscription1;
    private Subscription subscription2;
    private Subscription subscription3;
    private Subscription subscription4;
    private Subscription subscription6;
    private Subscription subscription7;
    private String updateQuery;
    private String selectQuery;
    private String targetRenewalDate1;
    private String targetRenewalDate2;
    private String targetRenewalDate3;
    private String priceListExternalKeyOfEmeaStore;

    /**
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        buyerId = getEnvironmentVariables().getUserExternalKey();
        // Create Canada store with same currency as of US Store
        final JStore canadaStore = storeApiUtils.addStore(Status.ACTIVE, Country.CA, Currency.USD, null, false);

        final JStore storeWithVat = storeApiUtils.addStore(resource, Status.ACTIVE, Country.FR, Currency.EUR, 10.0,
            null, false, null, null, getStoreTypeNameBic());

        // adding another price list to the same store
        final JPriceList jPriceList =
            storeApiUtils.addPriceListWithCountry(storeWithVat.getId(), Currency.GBP, Country.FR);

        priceListExternalKeyOfEmeaStore = storeWithVat.getIncluded().getPriceLists().get(0).getExternalKey();

        // Add subscription Offerings
        bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        // adding another price to the same subscription offer
        subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(40, jPriceList.getData().getExternalKey(), 0, 30);

        final Offerings bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        // Add another price to the same offer
        subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(40,
            canadaStore.getIncluded().getPriceLists().get(0).getExternalKey(), 0, 30);

        bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();
        bicOffering8 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceId2 = bicOffering8.getIncluded().getPrices().get(0).getId();
        bicOffering9 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId3 = bicOffering9.getIncluded().getPrices().get(0).getId();
        bicOffering10 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceId4 = bicOffering10.getIncluded().getPrices().get(0).getId();
        bicOfferings11 = subscriptionPlanApiUtils.addSubscriptionPlan(jPriceList.getData().getExternalKey(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceId5 = bicOfferings11.getIncluded().getPrices().get(0).getId();
        bicOfferings12 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceId6 = bicOfferings12.getIncluded().getPrices().get(0).getId();
        bicOfferings13 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceId7 = bicOfferings13.getIncluded().getPrices().get(0).getId();

        bicOfferings14 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId8 = bicOfferings14.getIncluded().getPrices().get(0).getId();

        bicOfferings15 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId9 = bicOfferings15.getIncluded().getPrices().get(0).getId();

        bicOfferings18 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId12 = bicOfferings18.getIncluded().getPrices().get(0).getId();

        bicOfferings19 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId13 = bicOfferings19.getIncluded().getPrices().get(0).getId();

        bicOfferings20 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId14 = bicOfferings20.getIncluded().getPrices().get(0).getId();

        bicOfferings21 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        priceId15 = bicOfferings21.getIncluded().getPrices().get(0).getId();

        final Offerings metaOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        activeNonStoreWideDiscountAmountPromo1 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        String purchaseOrderIdForBicCreditCard7 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard7);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard7);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard7);
        subscriptionIdBicCreditCard7 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscription7 = resource.subscription().getById(subscriptionIdBicCreditCard7);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        subscriptionIdBicCreditCard2 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard3 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId3, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard3);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard3);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard3);
        subscriptionIdBicCreditCard3 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscription3 = resource.subscription().getById(subscriptionIdBicCreditCard3);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard4 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId4, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard4);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard4);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard4);
        subscriptionIdBicCreditCard4 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscription4 = resource.subscription().getById(subscriptionIdBicCreditCard4);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard6 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId5, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard6);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard6);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard6);
        subscriptionIdBicCreditCard6 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        subscription6 = resource.subscription().getById(subscriptionIdBicCreditCard6);

        activeStoreWideWithCashForAddSeatsPromo1 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeStoreWideWithCashForAddSeatsPromo2 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering8), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeStoreWideWithCashForAddSeatsPromo3 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(storeWithVat), Lists.newArrayList(bicOffering9), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeStoreWideWithCashForAddSeatsPromo4 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(storeWithVat), Lists.newArrayList(bicOffering10), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStoreWideWithCashForAddSeatsPromo1 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "50.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStoreWideWithCashForAddSeatsPromo2 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering8), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "50.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        final String promoCode = promotionUtils.getRandomPromoCode();
        activeStoreWideWithCashForAddSeatsPromo5 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOfferings12), promoCode, true, Status.ACTIVE, null,
            "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOfferings12), promoCode, true, Status.CANCELLED, null, "30.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOfferings13), promoCode, true, Status.CANCELLED, null, "20.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        cancelledOnlyStoreWideWithCashForAddSeatsPromo5 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOfferings13), promoCode, true, Status.CANCELLED,
            null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        activeNonStoreWideSupplementTimePromo3 = promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, null, DateTimeUtils.getUTCFutureExpirationDate(), "5", "DAY", 2, null, null);

        // Create Bundle Promo Offerings with bic Offerings
        final List<BundlePromoOfferings> bundleOfBicOfferingsList = new ArrayList<>();
        bundleOfBicOfferingsList.add(promotionUtils.createBundlePromotionOffering(bicOfferings14, 3, true));
        bundleOfBicOfferingsList.add(promotionUtils.createBundlePromotionOffering(bicOfferings15, 2, true));

        // bundle storewide cash amount promotion creation
        String promoCode1 = promotionUtils.getRandomPromoCode();
        activeStoreWidePercentageBicOfferingsBundlePromo = promotionUtils.addBundlePromotion(
            PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()), bundleOfBicOfferingsList, promoCode1, true,
            Status.ACTIVE, null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            bundleOfBicOfferingsList, promoCode1, true, Status.CANCELLED, null, "20.00",
            DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        promoCode1 = promotionUtils.getRandomPromoCode();
        activeStoreWideWithCashForSubscriptionExtension1 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOfferings18), promoCode1, true, Status.ACTIVE, null,
            "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOfferings18), promoCode1, true, Status.CANCELLED, null, "30.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        String promoCode2 = promotionUtils.getRandomPromoCode();
        activeStoreWideWithCashForSubscriptionExtension2 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOfferings19), promoCode2, true, Status.ACTIVE, null,
            "20.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOfferings19), promoCode2, true, Status.CANCELLED, null, "20.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promoCode2 = promotionUtils.getRandomPromoCode();
        cancelledOnlyStoreWideWithCashForSubscriptionExtension1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOfferings20), promoCode2, true, Status.CANCELLED, null, "20.00",
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        cancelledOnlyStoreWideWithCashForSubscriptionExtension2 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOfferings21), promoCode2, true, Status.CANCELLED, null, "25.00",
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        // Submit a purchase order with Credit card and process it to charged
        // and inactive the subscription
        final String purchaseOrderIdForBicCreditCard5 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard5);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard5);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard5);
        subscriptionIdBicCreditCardNotActive =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        resource.subscription().getById(subscriptionIdBicCreditCardNotActive);

        // Submit a purchase order with Credit card and process it to charged
        // and inactive the subscription
        purchaseOrderIdForBicCreditCard7 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard7);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard7);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard7);
        subscriptionIdBicCreditCardNotActive1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        resource.subscription().getById(subscriptionIdBicCreditCardNotActive1);

        // Submit PO with credit card for Meta and get Subscription Id
        priceIdForMetaOffering2 = metaOffering2.getIncluded().getPrices().get(0).getId();
        PurchaseOrder purchaseOrderForMetaCreditCard = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            PaymentType.CREDIT_CARD, priceIdForMetaOffering2, getBuyerUser(), quantity);
        purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.PENDING,
            purchaseOrderForMetaCreditCard.getId(), ECStatus.ACCEPT);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderForMetaCreditCard.getId());
        purchaseOrderForMetaCreditCard = resource.purchaseOrder().getById(purchaseOrderForMetaCreditCard.getId());
        purchaseOrderUtils.fulfillRequest(purchaseOrderForMetaCreditCard, FulfillmentCallbackStatus.Created);
        purchaseOrderForMetaCreditCard = resource.purchaseOrder().getById(purchaseOrderForMetaCreditCard.getId());
        subscriptionIdForMetaCreditCard = purchaseOrderForMetaCreditCard.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        targetRenewalDate1 = Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);
        targetRenewalDate2 = Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], 13);
        targetRenewalDate3 = Util.addDaysToAParticularDate(subscription3.getNextBillingDate().split(" ")[0], 9);

    }

    /**
     * This is a test method which will test the price quotes api response while adding seats to the subscription with
     * and without additional taxes.
     */
    @Test(dataProvider = "AddSeatsDetails")
    public void testAddSeatsToSubscriptionWithAndWithOutAdditionalTaxes(final boolean applyShipping,
        final boolean applyAdditionalTax, final boolean applyVat) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();
        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        if (applyShipping && !applyVat) {

            final List<Offerings> offeringsList = new ArrayList<>();
            offeringsList.add(bicOffering7);
            offeringsList.add(bicOffering8);
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }
            requestData.setShipping(shipping);
        }

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1,
            subscriptionIdBicCreditCard1, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, quantity2,
            subscriptionIdBicCreditCard2, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        Double vatPercentage = 0.00;
        if (applyShipping && applyVat) {
            final List<Offerings> offeringsList = new ArrayList<>();
            offeringsList.add(bicOffering9);
            offeringsList.add(bicOffering10);
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }

            requestData.setShipping(shipping);
            vatPercentage = 10.00;
            requestData.setEstimateVat(true);
            lineItems.clear();
            lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId3, quantity1,
                subscriptionIdBicCreditCard3, additionalFees));
            lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId4, quantity2,
                subscriptionIdBicCreditCard4, additionalFees));

            requestData.setLineItems(lineItems);
        }
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        if (applyShipping && applyVat) {
            offeringsJPromotionMap.clear();
            subscriptionList.clear();
            offeringsJPromotionMap.put(bicOffering9, null);
            offeringsJPromotionMap.put(bicOffering10, null);
            subscriptionList.add(subscription3);
            subscriptionList.add(subscription4);
        }
        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test merging of the line items for add seats request.
     */
    @Test
    public void testMergeLineItemsInAddSeats() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1,
            subscriptionIdBicCreditCard1, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity2,
            subscriptionIdBicCreditCard1, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(quantity1 + quantity2),
            assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(quantity1 + quantity2),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test non-merging of the line items for add seats request.
     */
    @Test
    public void testMergeLineItemsInAddSeatsWhenPriceIdAndSubscriptionIdAreDifferent() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1,
            subscriptionIdBicCreditCard1, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1,
            subscriptionIdBicCreditCard7, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription7);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(quantity1), assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(quantity1), assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(1).getQuantity(), equalTo(quantity1), assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(1).getQuantity(), equalTo(quantity1), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api without providing the subscription id while adding
     * seats to subscription
     */
    @Test
    public void testAddSeatsToSubscriptionWithoutSubscriptionId() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);
        requestData.setShipping(shipping);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId3, quantity1, null, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId4, quantity2, null, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setShipping(shipping);
        requestData.setEstimateVat(true);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);
        // Assertion on insufficient quantity
        AssertCollector.assertThat("Subscription id is missing in the request body",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID), assertionErrorList);
        AssertCollector.assertThat("Subscription id is missing in the request body",
            priceQuotesResponse.getErrors().get(1).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api without providing the price id while adding seats to
     * subscription
     */
    @Test
    public void testAddSeatsToSubscriptionWithoutPriceId() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);
        requestData.setShipping(shipping);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, null, quantity1, subscriptionIdBicCreditCard1,
            additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, null, quantity2, subscriptionIdBicCreditCard2,
            additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setShipping(shipping);
        requestData.setEstimateVat(true);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);
        // Assertion on insufficient quantity
        AssertCollector.assertThat("Price id is missing in the request body",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_PRICE_ID), assertionErrorList);
        AssertCollector.assertThat("Price id is missing in the request body",
            priceQuotesResponse.getErrors().get(1).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_PRICE_ID), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with default quantity while adding seats to
     * subscription
     */
    @Test
    public void testAddSeatsToSubscriptionWithDefaultQuantity() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, subscriptionIdBicCreditCard1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, subscriptionIdBicCreditCard2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with StoreWide Cash Amount promotion applied to
     * multiple line items.
     */
    @Test(dataProvider = "AddSeatsDetails")
    public void testAddSeatsToSubscriptionWithStoreWideCashAmountPromotion(final boolean applyShipping,
        final boolean applyAdditionalTax, final boolean applyVat) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();
        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        if (applyShipping && !applyVat) {

            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }
            requestData.setShipping(shipping);
        }

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo1)));
        offeringsJPromotionMap.put(bicOffering8,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo2)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1, subscriptionIdBicCreditCard1, null));
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, quantity2, subscriptionIdBicCreditCard2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeStoreWideWithCashForAddSeatsPromo1.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(activeStoreWideWithCashForAddSeatsPromo2.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        if (applyShipping && applyVat) {

            offeringsList.clear();
            offeringsList.add(bicOffering9);
            offeringsList.add(bicOffering10);
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }

            requestData.setShipping(shipping);
            vatPercentage = 10.00;
            requestData.setEstimateVat(true);

            lineItems.clear();
            lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId3, quantity1,
                subscriptionIdBicCreditCard3, additionalFees));
            lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId4, quantity2,
                subscriptionIdBicCreditCard4, additionalFees));

            offeringsJPromotionMap.clear();
            offeringsJPromotionMap.put(bicOffering9,
                new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo3)));
            offeringsJPromotionMap.put(bicOffering10,
                new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo4)));

            promotionReferencesList.clear();
            promotionReferencesList
                .add(new JPromotionReference(activeStoreWideWithCashForAddSeatsPromo3.getData().getId(), null));
            promotionReferencesList
                .add(new JPromotionReference(activeStoreWideWithCashForAddSeatsPromo4.getData().getId(), null));
            requestData.setPromotionReferences(promotionReferencesList);

            subscriptionList.clear();
            subscriptionList.add(subscription3);
            subscriptionList.add(subscription4);

        }

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with an active and cancelled promo code applied to
     * multiple line items.
     */
    @Test
    public void testAddSeatsToSubscriptionWithActiveAndCancelledPromotion() {

        final Subscription subscription1 = HelperForPriceQuote.getSubscriptionIdForSubscriptionManagement(
            purchaseOrderUtils, priceId6, getBuyerUser(), quantity, resource);

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOfferings12);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOfferings12,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo5)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems
            .add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId6, quantity, subscription1.getId(), null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(null, activeStoreWideWithCashForAddSeatsPromo5.getData().getCustomPromoCode()));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder = PriceQuoteRequestBuilder.builder().setBuyerId(buyerId)
            .setLineItems(lineItems).setAdditionalFees(null).setPromotionReferences(promotionReferencesList)
            .setResource(resource).setEnvironmentVariables(getEnvironmentVariables())
            .setOfferingsJPromotionMap(offeringsJPromotionMap).setPurchaseType(PurchaseType.SUBSCRIPTIONQUANTITY)
            .setSubscriptionList(subscriptionList).setShipping(shipping).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with an active and cancelled bundle promo code applied
     * to multiple line items.
     */
    @Test
    public void testAddSeatsToSubscriptionWithActiveAndCancelledBundlePromotion() {

        final Subscription subscription1 = HelperForPriceQuote
            .getSubscriptionIdForSubscriptionManagement(purchaseOrderUtils, priceId8, getBuyerUser(), 3, resource);

        final Subscription subscription2 = HelperForPriceQuote
            .getSubscriptionIdForSubscriptionManagement(purchaseOrderUtils, priceId9, getBuyerUser(), 2, resource);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOfferings14);
        offeringsList.add(bicOfferings15);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOfferings14, null);
        offeringsJPromotionMap.put(bicOfferings15, null);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, 3, subscription1.getId(), null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, 2, subscription2.getId(), null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null,
            activeStoreWidePercentageBicOfferingsBundlePromo.getData().getCustomPromoCode()));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder = PriceQuoteRequestBuilder.builder().setBuyerId(buyerId)
            .setLineItems(lineItems).setAdditionalFees(additionalFees).setPromotionReferences(promotionReferencesList)
            .setResource(resource).setEnvironmentVariables(getEnvironmentVariables())
            .setOfferingsJPromotionMap(offeringsJPromotionMap).setPurchaseType(PurchaseType.SUBSCRIPTIONQUANTITY)
            .setSubscriptionList(subscriptionList).setShipping(shipping).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with only cancelled promo code for the line item
     */
    @Test
    public void testAddSeatsToSubscriptionWithCancelledPromotion() {

        final Subscription subscription1 = HelperForPriceQuote.getSubscriptionIdForSubscriptionManagement(
            purchaseOrderUtils, priceId7, getBuyerUser(), quantity, resource);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOfferings13);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOfferings13, null);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems
            .add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId7, quantity, subscription1.getId(), null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null,
            cancelledOnlyStoreWideWithCashForAddSeatsPromo5.getData().getCustomPromoCode()));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder = PriceQuoteRequestBuilder.builder().setBuyerId(buyerId)
            .setLineItems(lineItems).setAdditionalFees(null).setPromotionReferences(promotionReferencesList)
            .setResource(resource).setEnvironmentVariables(getEnvironmentVariables())
            .setOfferingsJPromotionMap(offeringsJPromotionMap).setPurchaseType(PurchaseType.SUBSCRIPTIONQUANTITY)
            .setSubscriptionList(subscriptionList).setShipping(null).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with Non StoreWide cash discount promotion applied to
     * multiple line items.
     */
    @Test
    public void testAddSeatsToSubscriptionWithNonStoreWideCashDiscountPromotion() {

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);
        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription2.getId();
        final String oldDateForSubscription2 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final String newDateForSubscription1 = DateTimeUtils.addDaysToDate(oldDateForSubscription1,
            PelicanConstants.RENEWAL_DATE_FORMAT, PelicanConstants.RENEWAL_DAYS_FOR_ADD_SEATS);
        final String newDateForSubscription2 = DateTimeUtils.addDaysToDate(oldDateForSubscription2,
            PelicanConstants.RENEWAL_DATE_FORMAT, PelicanConstants.RENEWAL_DAYS_FOR_ADD_SEATS);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription2 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription2.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        SubscriptionSyncRequestBuilder builder =
            constructRequestBuilder(UsageType.COM, plan1, getBuyerUser().getExternalKey(),
                Long.parseLong(activeNonStoreWideWithCashForAddSeatsPromo1.getData().getId()), 0, 0, 0,
                newDateForSubscription1);

        SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1, builder,
            Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        final SubscriptionPlan plan2 = resource.subscriptionPlan().getById(bicOffering8.getOfferings().get(0).getId(),
            bicOffering8.getIncluded().getBillingPlans().get(0).getId());

        builder = constructRequestBuilder(UsageType.COM, plan2, getBuyerUser().getExternalKey(),
            Long.parseLong(activeNonStoreWideWithCashForAddSeatsPromo2.getData().getId()), 0, 0, 0,
            newDateForSubscription2);

        request = SubscriptionSyncRequestHelper.constructRequestBody(subscription2, builder,
            Long.parseLong(subscriptionIdBicCreditCard2),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId2));

        requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);
        subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideWithCashForAddSeatsPromo1)));
        offeringsJPromotionMap.put(bicOffering8,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideWithCashForAddSeatsPromo2)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1, subscriptionIdBicCreditCard1, null));
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, quantity2, subscriptionIdBicCreditCard2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideWithCashForAddSeatsPromo1.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideWithCashForAddSeatsPromo2.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with StoreWide percentage promotion applied to
     * multiple line items.
     */
    @Test
    public void testAddSeatsToSubscriptionWithStoreWidePercentDiscountPromotion() {

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();
        final Offerings bicOffering8 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId2 = bicOffering8.getIncluded().getPrices().get(0).getId();
        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final JPromotion activeStoreWideWithPercentDiscountForAddSeatsPromo1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "10", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        final JPromotion activeStoreWideWithPercentDiscountForAddSeatsPromo2 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering8), promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "10", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);
        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription2.getId();
        final String oldDateForSubscription2 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final String newDateForSubscription1 = DateTimeUtils.addDaysToDate(oldDateForSubscription1,
            PelicanConstants.RENEWAL_DATE_FORMAT, PelicanConstants.RENEWAL_DAYS_FOR_ADD_SEATS);
        final String newDateForSubscription2 = DateTimeUtils.addDaysToDate(oldDateForSubscription2,
            PelicanConstants.RENEWAL_DATE_FORMAT, PelicanConstants.RENEWAL_DAYS_FOR_ADD_SEATS);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription2 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription2.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        SubscriptionSyncRequestBuilder builder =
            constructRequestBuilder(UsageType.COM, plan1, getBuyerUser().getExternalKey(),
                Long.parseLong(activeStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId()), 0, 0, 0,
                newDateForSubscription1);

        SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1, builder,
            Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        final SubscriptionPlan plan2 = resource.subscriptionPlan().getById(bicOffering8.getOfferings().get(0).getId(),
            bicOffering8.getIncluded().getBillingPlans().get(0).getId());

        builder = constructRequestBuilder(UsageType.COM, plan2, getBuyerUser().getExternalKey(),
            Long.parseLong(activeStoreWideWithPercentDiscountForAddSeatsPromo2.getData().getId()), 0, 0, 0,
            newDateForSubscription2);

        request = SubscriptionSyncRequestHelper.constructRequestBody(subscription2, builder,
            Long.parseLong(subscriptionIdBicCreditCard2),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId2));

        requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);
        subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithPercentDiscountForAddSeatsPromo1)));
        offeringsJPromotionMap.put(bicOffering8,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithPercentDiscountForAddSeatsPromo2)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1, subscriptionIdBicCreditCard1, null));
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, quantity2, subscriptionIdBicCreditCard2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(activeStoreWideWithPercentDiscountForAddSeatsPromo2.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with NonStoreWide percentage promotion applied to
     * multiple line items.
     */
    @Test
    public void testAddSeatsToSubscriptionWithNonStoreWidePercentDiscountPromotion() {

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();
        final Offerings bicOffering8 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceId2 = bicOffering8.getIncluded().getPrices().get(0).getId();
        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);
        // getting purchase order to get the subscription id
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final JPromotion activeNonStoreWideWithPercentDiscountForAddSeatsPromo1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "15", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        final JPromotion activeNonStoreWideWithPercentDiscountForAddSeatsPromo2 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering8), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "15", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);
        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription2.getId();
        final String oldDateForSubscription2 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final String newDateForSubscription1 =
            DateTimeUtils.addDaysToDate(oldDateForSubscription1, PelicanConstants.RENEWAL_DATE_FORMAT, 7);
        final String newDateForSubscription2 =
            DateTimeUtils.addDaysToDate(oldDateForSubscription2, PelicanConstants.RENEWAL_DATE_FORMAT, 7);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription2 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription2.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        SubscriptionSyncRequestBuilder builder =
            constructRequestBuilder(UsageType.COM, plan1, getBuyerUser().getExternalKey(),
                Long.parseLong(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId()), 0, 0, 0,
                newDateForSubscription1);

        SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1, builder,
            Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        final SubscriptionPlan plan2 = resource.subscriptionPlan().getById(bicOffering8.getOfferings().get(0).getId(),
            bicOffering8.getIncluded().getBillingPlans().get(0).getId());

        builder = constructRequestBuilder(UsageType.COM, plan2, getBuyerUser().getExternalKey(),
            Long.parseLong(activeNonStoreWideWithPercentDiscountForAddSeatsPromo2.getData().getId()), 0, 0, 0,
            newDateForSubscription2);

        request = SubscriptionSyncRequestHelper.constructRequestBody(subscription2, builder,
            Long.parseLong(subscriptionIdBicCreditCard2),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId2));

        requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);
        subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1)));
        offeringsJPromotionMap.put(bicOffering8,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideWithPercentDiscountForAddSeatsPromo2)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1, subscriptionIdBicCreditCard1, null));
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId2, quantity2, subscriptionIdBicCreditCard2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId(), null));
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideWithPercentDiscountForAddSeatsPromo2.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with percentage bundle promotion applied to single
     * line items.
     *
     * @param - Boolean - IsStoreWide
     * @param - int - LineItemQuantity
     * @param - int - ProratedDays
     */
    @Test(dataProvider = "BundlePromoForPercentageDetails")
    public void testAddSeatsToSubscriptionWithBundlePercentDiscountPromotion(final boolean isStoreWide,
        final int lineItemQuantity, final int proraatedDays) {

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();

        final List<BundlePromoOfferings> bundleOfSubscriptionOffers = new ArrayList<>();
        bundleOfSubscriptionOffers.add(promotionUtils.createBundlePromotionOffering(bicOffering7, 5, true));

        final JPromotion activeNonStoreWideBunldePercentPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                bundleOfSubscriptionOffers, promotionUtils.getRandomPromoCode(), isStoreWide, Status.ACTIVE, "20", null,
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final String newDateForSubscription1 =
            DateTimeUtils.addDaysToDate(oldDateForSubscription1, PelicanConstants.RENEWAL_DATE_FORMAT, proraatedDays);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        final SubscriptionSyncRequestBuilder builder = constructRequestBuilder(UsageType.COM, plan1,
            getBuyerUser().getExternalKey(), 0L, 0, 0, 0, newDateForSubscription1);

        final SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1,
            builder, Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        final String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideBunldePercentPromo)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, lineItemQuantity,
            subscriptionIdBicCreditCard1, null));
        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideBunldePercentPromo.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with percentage bundle promotion applied to single
     * line items.
     *
     * @param - Boolean - IsStoreWide
     * @param - int - LineItemQuantity
     * @param - int - ProratedDays
     */
    @Test(dataProvider = "BundlePromoForPercentageDetails")
    public void testAddSeatsToSubscriptionWithBundlePercentCashAmountPromotion(final boolean isStoreWide,
        final int lineItemQuantity, final int proraatedDays) {

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();

        final List<BundlePromoOfferings> bundleOfSubscriptionOffers = new ArrayList<>();
        bundleOfSubscriptionOffers.add(promotionUtils.createBundlePromotionOffering(bicOffering7, 5, true));

        final JPromotion activeNonStoreWideBunldeCashPromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                bundleOfSubscriptionOffers, promotionUtils.getRandomPromoCode(), isStoreWide, Status.ACTIVE, null,
                "40.00", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final String newDateForSubscription1 =
            DateTimeUtils.addDaysToDate(oldDateForSubscription1, PelicanConstants.RENEWAL_DATE_FORMAT, proraatedDays);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        final SubscriptionSyncRequestBuilder builder = constructRequestBuilder(UsageType.COM, plan1,
            getBuyerUser().getExternalKey(), 0L, 0, 0, 0, newDateForSubscription1);

        final SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1,
            builder, Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        final String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, new ArrayList<>(ImmutableList.of(activeNonStoreWideBunldeCashPromo)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, lineItemQuantity,
            subscriptionIdBicCreditCard1, null));
        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(activeNonStoreWideBunldeCashPromo.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with Empty or Invalid Purchase Type while adding seats
     * to subscription
     */
    @Test(dataProvider = "PurchaseTypes")
    public void testPQAddSeatsToSubscriptionWithEmptyPurchaseType(final String purchaseType) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        final JLineItem jLineItem = new JLineItem(priceId1, quantity1, subscriptionIdBicCreditCard1);
        jLineItem.setPurchaseType(purchaseType);
        lineItems.add(jLineItem);

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Add seats for Pirce Quote call Invalid PurchaseType",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_INVALID_PURCHASETYPE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with invalid quantity while adding seats to
     * subscription
     */
    @Test(dataProvider = "AddSeatsQuantityValues")
    public void testPQAddSeatsToSubscriptionWithValidationsOnQuantity(final int quantity) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity, subscriptionIdBicCreditCard1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        if (quantity == 0) {
            AssertCollector.assertThat("Add seats for Pirce Quote call with quantity 0",
                priceQuotesResponse.getErrors().get(0).getDetail(),
                equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_QUANTITY_ZERO), assertionErrorList);
        } else if (quantity < 0) {
            AssertCollector.assertThat("Add seats for Pirce Quote call with quantity less than -1",
                priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(1), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with invalid Subscription ID while adding seats to
     * subscription
     */
    @Test(dataProvider = "InvalidSubscriptionId")
    public void testPQAddSeatsToSubscriptionWithInvalidSubscriptionId(final String sunscriptionId) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, 1, sunscriptionId, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Add seats for Pirce Quote call Invalid Subscription",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_INVALID_SUBSCRIPTION + sunscriptionId + "]"),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with Supplement Promo (which is not allowed) while
     * adding seats to subscription
     */
    @Test
    public void testPQAddSeatsToSubscriptionWithSupplementPromo() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems
            .add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, 1, subscriptionIdBicCreditCard1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideSupplementTimePromo3.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Add seats for Pirce Quote call worked Suppliment Promo",
            priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_SUPPLIMENT_PROMO), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with Inactive Subscription while adding seats to
     * subscription
     */
    @Test
    public void testPQAddSeatsToSubscriptionWithInactiveSubscription() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // cancel the subscription
        resource.subscription().cancelSubscription(subscriptionIdBicCreditCardNotActive,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, 1, subscriptionIdBicCreditCardNotActive, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Add seats for Pirce Quote call worked Inactive Subscription",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_PRE_TEXT_INACTIVE_SUBSCRIPTION
                + subscriptionIdBicCreditCardNotActive
                + PelicanErrorConstants.ERROR_MESSAGE_FOR_POST_TEXT_INACTIVE_SUBSCRIPTION),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with null values for SubscriptionId, quantity and
     * priceId while adding seats to subscription
     */
    @Test(dataProvider = "AddSeatsWithNullValues")
    public void testPQAddSeatsToSubscriptionWithNullValuesForLineItems(final String priceId, final Integer quantity,
        final String subscriptionId) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId, quantity, subscriptionId, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        if (priceId == null) {
            AssertCollector.assertTrue("Price id is missing in the request body", HelperForPriceQuote.parsePqErrors(
                priceQuotesResponse, PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_PRICE_ID), assertionErrorList);
        }

        if (quantity == null) {
            AssertCollector.assertTrue("Quantity is optional field in the request body, found error with Quantity",
                !(HelperForPriceQuote.parsePqErrors(priceQuotesResponse, "Quantity")), assertionErrorList);
        }

        if (subscriptionId == null) {
            AssertCollector.assertTrue("Subscritption id is missing in the request body", HelperForPriceQuote
                .parsePqErrors(priceQuotesResponse, PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID),
                assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with Meta Subscription while adding seats to
     * subscription
     */
    @Test
    public void testPQAddSeatsToSubscriptionWithMetaSubscription() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceIdForMetaOffering2, 1,
            subscriptionIdForMetaCreditCard, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Add seats for Price Quote call worked for META Subscription",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_META_SUBSCRIPTION_ID), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method which will test Add seats price quotes api with past next billing date subscription id
     */
    @Test
    public void testPQAddSeatsSubscriptionWithPastNextBillingDate() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        final String currentNextBillingDate = DbUtils
            .selectQuery(PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscriptionIdBicCreditCard1,
                PelicanDbConstants.DB_FIELD_NEXT_BILLING_DATE, getEnvironmentVariables())
            .get(0);

        final String updateDbNextBillingDate = PelicanDbConstants.UPDATE_TABLE_NAME + "'"
            + DateTimeUtils.addDaysToDate(currentNextBillingDate, PelicanConstants.RENEWAL_DATE_FORMAT, -34) + "'"
            + PelicanDbConstants.UPDATE_CONDITION + subscriptionIdBicCreditCard1;

        DbUtils.updateQuery(updateDbNextBillingDate, getEnvironmentVariables());

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, QUANTITY, subscriptionIdBicCreditCard1, null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        AssertCollector.assertThat("Add seats for Price Quote call,with past next billing date Subscription",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(
                PelicanErrorConstants.ERROR_MESSAGE_FOR_PRE_TEXT_INACTIVE_SUBSCRIPTION + subscriptionIdBicCreditCard1
                    + PelicanErrorConstants.ERROR_MESSAGE_FOR_POST_TEXT_PAST_NEXT_NILLLING_DATE),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with mixed PurchaseTypes while adding seats to
     * subscription
     */
    @Test
    public void testAddSeatsToSubscriptionWithMixedPurchaseTypes() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems
            .add(new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, 1, subscriptionIdBicCreditCard1, null));
        lineItems.add(new JLineItem(PurchaseType.OFFERING, priceId2, 1, subscriptionIdBicCreditCard2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        AssertCollector.assertThat("Error message for Add seats for Pirce Quote call for Mixed Purchase Types",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MIXED_PURCHASETYPES), assertionErrorList);

        AssertCollector.assertThat("Status code for Add seats for Pirce Quote call for Mixed Purchase Types",
            priceQuotesResponse.getErrors().get(0).getStatus(), equalTo(HttpStatus.SC_BAD_REQUEST), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test add seats of price quotes api with NonStoreWide percentage promotion
     * applied to line item on the same day of renewal.
     */
    @Test
    public void testAddSeatsToSubscriptionWithNonStoreWidePercentDiscountPromotionOnTheSameDayOfRenewal() {

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final JPromotion activeNonStoreWideWithPercentDiscountForAddSeatsPromo1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "15", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final Integer days = Util.getCountOfBillingDays(subscription1.getId(), resource, getEnvironmentVariables());
        final String newDateForSubscription1 =
            DateTimeUtils.addDaysToDate(oldDateForSubscription1, PelicanConstants.RENEWAL_DATE_FORMAT, -days);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        final SubscriptionSyncRequestBuilder builder =
            constructRequestBuilder(UsageType.COM, plan1, getBuyerUser().getExternalKey(),
                Long.parseLong(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId()), 0, 0, 0,
                newDateForSubscription1);

        final SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1,
            builder, Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        final String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1, subscriptionIdBicCreditCard1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test add seats of price quotes api without any promotion applied to line item on
     * the same day of renewal.
     */
    @Test
    public void testAddSeatsToSubscriptionWithOutPromotionOnTheSameDayOfRenewal() {

        final Offerings bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceId1 = bicOffering7.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final JPromotion activeNonStoreWideWithPercentDiscountForAddSeatsPromo1 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "15", null,
                DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        selectQuery = PelicanDbConstants.SELECT_NEXT_BILLING_DATE_FROM_SUBSCRIPTION + subscription1.getId();
        final String oldDateForSubscription1 = DbUtils
            .selectQuery(selectQuery, PelicanDbConstants.NEXT_BILLING_DATE_DB_FIELD, getEnvironmentVariables()).get(0);

        final Integer days = Util.getCountOfBillingDays(subscription1.getId(), resource, getEnvironmentVariables());
        final String newDateForSubscription1 =
            DateTimeUtils.addDaysToDate(oldDateForSubscription1, PelicanConstants.RENEWAL_DATE_FORMAT, -days);

        updateQuery = PelicanDbConstants.UPDATE_NEXT_BILLING_DATE_IN_SUBSCRIPTION + "'" + newDateForSubscription1 + "'"
            + PelicanDbConstants.WHERE_ID_CONDITION + subscription1.getId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

        final SubscriptionPlan plan1 = resource.subscriptionPlan().getById(bicOffering7.getOfferings().get(0).getId(),
            bicOffering7.getIncluded().getBillingPlans().get(0).getId());

        final SubscriptionSyncRequestBuilder builder =
            constructRequestBuilder(UsageType.COM, plan1, getBuyerUser().getExternalKey(),
                Long.parseLong(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId()), 0, 0, 0,
                newDateForSubscription1);

        final SubscriptionSyncRequest request = SubscriptionSyncRequestHelper.constructRequestBody(subscription1,
            builder, Long.parseLong(purchaseOrderIdForBicCreditCard1),
            Long.parseLong(getEnvironmentVariables().getBluesnapNamerPaymentGatewayId()), Long.parseLong(priceId1));

        final String requestBody = SubscriptionSyncRequestHelper.getSubscriptionAsRequestBodyString(request);

        resource.subscription().syncSubscription(getEnvironmentVariables(), requestBody);

        subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(
            new JLineItem(PurchaseType.SUBSCRIPTIONQUANTITY, priceId1, quantity1, subscriptionIdBicCreditCard1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideWithPercentDiscountForAddSeatsPromo1.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONQUANTITY.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api response while extending the subscription with and
     * without additional taxes.
     */
    @Test(dataProvider = "AddSeatsDetails")
    public void testSubscriptionExtensionWithAndWithOutAdditionalTaxes(final boolean applyShipping,
        final boolean applyAdditionalTax, final boolean applyVat) {

        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicOffering2 = bicOffering8 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings bicOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicOffering4 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId1 = bicOffering1.getIncluded().getPrices().get(0).getId();
        final String priceId2 = bicOffering2.getIncluded().getPrices().get(0).getId();
        final String priceId3 = bicOffering3.getIncluded().getPrices().get(0).getId();
        final String priceId4 = bicOffering4.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder1 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder2 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard3 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId3, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard3);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard3);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder3 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard3);
        final String subscriptionIdBicCreditCard3 =
            purchaseOrder3.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription3 = resource.subscription().getById(subscriptionIdBicCreditCard3);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard4 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId4, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard4);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard4);
        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder4 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard4);
        final String subscriptionIdBicCreditCard4 =
            purchaseOrder4.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription4 = resource.subscription().getById(subscriptionIdBicCreditCard4);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);
        final String targetRenewalDate2 =
            Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], 13);
        final String targetRenewalDate3 =
            Util.addDaysToAParticularDate(subscription3.getNextBillingDate().split(" ")[0], 9);
        final String targetRenewalDate4 =
            Util.addDaysToAParticularDate(subscription4.getNextBillingDate().split(" ")[0], 13);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();
        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        if (applyShipping && !applyVat) {

            final List<Offerings> offeringsList = new ArrayList<>();
            offeringsList.add(bicOffering1);
            offeringsList.add(bicOffering2);
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }
            requestData.setShipping(shipping);
        }

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, quantity1,
            subscriptionIdBicCreditCard1, targetRenewalDate1, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId2, quantity2,
            subscriptionIdBicCreditCard2, targetRenewalDate2, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        Double vatPercentage = 0.00;
        if (applyShipping && applyVat) {
            final List<Offerings> offeringsList = new ArrayList<>();
            offeringsList.add(bicOffering3);
            offeringsList.add(bicOffering4);
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }

            requestData.setShipping(shipping);
            vatPercentage = 10.00;
            requestData.setEstimateVat(true);
            lineItems.clear();
            lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId3, quantity1,
                subscriptionIdBicCreditCard3, targetRenewalDate3, additionalFees));
            lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId4, quantity2,
                subscriptionIdBicCreditCard4, targetRenewalDate4, additionalFees));

            requestData.setLineItems(lineItems);
        }
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering1, null);
        offeringsJPromotionMap.put(bicOffering2, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        if (applyShipping && applyVat) {
            offeringsJPromotionMap.clear();
            subscriptionList.clear();
            offeringsJPromotionMap.put(bicOffering3, null);
            offeringsJPromotionMap.put(bicOffering4, null);
            subscriptionList.add(subscription3);
            subscriptionList.add(subscription4);
        }
        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test subscription extension with active and cancelled promo code for price ids
     */
    @Test
    public void testSubscriptionExtensionWithActiveAndCancelledPromo() {

        final Subscription subscription1 = HelperForPriceQuote.getSubscriptionIdForSubscriptionManagement(
            purchaseOrderUtils, priceId12, getBuyerUser(), quantity, resource);

        final Subscription subscription2 = HelperForPriceQuote.getSubscriptionIdForSubscriptionManagement(
            purchaseOrderUtils, priceId13, getBuyerUser(), quantity, resource);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOfferings18);
        offeringsList.add(bicOfferings19);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOfferings18, null);
        offeringsJPromotionMap.put(bicOfferings19, null);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);
        final String targetRenewalDate2 =
            Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], 13);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId12, quantity, subscription1.getId(),
            targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId13, quantity, subscription2.getId(),
            targetRenewalDate2, null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null,
            activeStoreWideWithCashForSubscriptionExtension1.getData().getCustomPromoCode()));
        promotionReferencesList.add(new JPromotionReference(null,
            activeStoreWideWithCashForSubscriptionExtension2.getData().getCustomPromoCode()));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder = PriceQuoteRequestBuilder.builder().setBuyerId(buyerId)
            .setLineItems(lineItems).setAdditionalFees(null).setPromotionReferences(promotionReferencesList)
            .setResource(resource).setEnvironmentVariables(getEnvironmentVariables())
            .setOfferingsJPromotionMap(offeringsJPromotionMap).setPurchaseType(PurchaseType.SUBSCRIPTIONEXTENSION)
            .setSubscriptionList(subscriptionList).setShipping(null).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test subscription extension with only cancelled promo code for price ids
     */
    @Test
    public void testSubscriptionExtensionWithCancelledPromo() {

        final Subscription subscription1 = HelperForPriceQuote.getSubscriptionIdForSubscriptionManagement(
            purchaseOrderUtils, priceId14, getBuyerUser(), quantity, resource);

        final Subscription subscription2 = HelperForPriceQuote.getSubscriptionIdForSubscriptionManagement(
            purchaseOrderUtils, priceId15, getBuyerUser(), quantity, resource);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOfferings20);
        offeringsList.add(bicOfferings21);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOfferings20, null);
        offeringsJPromotionMap.put(bicOfferings21, null);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);
        final String targetRenewalDate2 =
            Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], 13);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId14, quantity, subscription1.getId(),
            targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId15, quantity, subscription2.getId(),
            targetRenewalDate2, null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null,
            cancelledOnlyStoreWideWithCashForSubscriptionExtension1.getData().getCustomPromoCode()));
        promotionReferencesList.add(new JPromotionReference(null,
            cancelledOnlyStoreWideWithCashForSubscriptionExtension2.getData().getCustomPromoCode()));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder = PriceQuoteRequestBuilder.builder().setBuyerId(buyerId)
            .setLineItems(lineItems).setAdditionalFees(null).setPromotionReferences(promotionReferencesList)
            .setResource(resource).setEnvironmentVariables(getEnvironmentVariables())
            .setOfferingsJPromotionMap(offeringsJPromotionMap).setPurchaseType(PurchaseType.SUBSCRIPTIONEXTENSION)
            .setSubscriptionList(subscriptionList).setShipping(null).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api without providing the subscription id while extending
     * a subscription
     */
    @Test
    public void testExtendASubscriptionWithoutSubscriptionId() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);
        requestData.setShipping(shipping);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId3, quantity1, null, targetRenewalDate1,
            additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId4, quantity2, null, targetRenewalDate2,
            additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setShipping(shipping);
        requestData.setEstimateVat(true);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);
        // Assertion on insufficient quantity
        AssertCollector.assertThat("Subscription id is missing in the request body",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID), assertionErrorList);
        AssertCollector.assertThat("Subscription id is missing in the request body",
            priceQuotesResponse.getErrors().get(1).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_SUBSCRIPTION_ID), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api without providing the price id while extending seats
     * to subscription
     */
    @Test
    public void testExtendASubscriptionWithoutPriceId() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);
        requestData.setShipping(shipping);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, null, quantity1, subscriptionIdBicCreditCard1,
            targetRenewalDate1, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, null, quantity2, subscriptionIdBicCreditCard2,
            targetRenewalDate2, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setShipping(shipping);
        requestData.setEstimateVat(true);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);
        // Assertion on insufficient quantity
        AssertCollector.assertThat("Price id is missing in the request body",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_PRICE_ID), assertionErrorList);
        AssertCollector.assertThat("Price id is missing in the request body",
            priceQuotesResponse.getErrors().get(1).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_PRICE_ID), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api without providing the target renewal date while
     * extending a subscription
     */
    @Test
    public void testExtendASubscriptionWithoutTargetRenewalDate() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);
        requestData.setShipping(shipping);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId3, quantity1,
            subscriptionIdBicCreditCard1, null, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId4, quantity2,
            subscriptionIdBicCreditCard2, null, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setShipping(shipping);
        requestData.setEstimateVat(true);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);
        // Assertion on insufficient quantity
        AssertCollector.assertThat("Target Renewal Date is missing in the request body",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_MISSING_TARGET_RENEWAL_DATE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with Inactive Subscription while extending a
     * subscription
     */
    @Test
    public void testExtendASubscriptionWithCanceledSubscription() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // cancel the subscription
        resource.subscription().cancelSubscription(subscriptionIdBicCreditCardNotActive1,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, 1,
            subscriptionIdBicCreditCardNotActive1, targetRenewalDate1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Align billing dates for Price Quote call worked for Inactive Subscription",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_PRE_TEXT_INACTIVE_SUBSCRIPTION
                + subscriptionIdBicCreditCardNotActive1
                + PelicanErrorConstants.ERROR_MESSAGE_FOR_POST_TEXT_INACTIVE_SUBSCRIPTION),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api with providing the past target renewal date while
     * extending a subscription
     */
    @Test
    public void testExtendASubscriptionWithPastRenewalTargetDate() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);
        requestData.setShipping(shipping);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], -9);
        final String targetRenewalDate2 =
            Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], -13);
        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, quantity1,
            subscriptionIdBicCreditCard1, targetRenewalDate1, additionalFees));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId2, quantity2,
            subscriptionIdBicCreditCard2, targetRenewalDate2, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setLineItems(lineItems);
        requestData.setEstimateVat(false);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assertion on insufficient quantity
        AssertCollector.assertThat("Subscription target renewal date is in the past",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(
                PelicanErrorConstants.ERROR_MESSAGE_FOR_PRE_TEXT_INACTIVE_SUBSCRIPTION + subscriptionIdBicCreditCard1
                    + PelicanErrorConstants.ERROR_MESSAGE_FOR_POST_TEXT_PAST_TARGET_RENEWAL_DATE),
            assertionErrorList);
        AssertCollector.assertThat("Subscription target renewal date is in the past",
            priceQuotesResponse.getErrors().get(1).getDetail(),
            equalTo(
                PelicanErrorConstants.ERROR_MESSAGE_FOR_PRE_TEXT_INACTIVE_SUBSCRIPTION + subscriptionIdBicCreditCard2
                    + PelicanErrorConstants.ERROR_MESSAGE_FOR_POST_TEXT_PAST_TARGET_RENEWAL_DATE),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api subscription extension with promotion
     */
    @Test
    public void testExtendASubscriptionWithPromotion() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering8);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo1)));
        offeringsJPromotionMap.put(bicOffering8,
            new ArrayList<>(ImmutableList.of(activeStoreWideWithCashForAddSeatsPromo2)));

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, quantity1,
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId2, quantity2,
            subscriptionIdBicCreditCard2, targetRenewalDate2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeStoreWideWithCashForAddSeatsPromo1.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(activeStoreWideWithCashForAddSeatsPromo2.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        requestData.setEstimateVat(false);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        // Assertion on insufficient quantity
        AssertCollector.assertThat("Promotion is not applicable for this purchase type",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_NO_PROMOTION_APPLICABLE), assertionErrorList);
        AssertCollector.assertThat("Promotion is not applicable for this purchase type",
            priceQuotesResponse.getErrors().get(1).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_NO_PROMOTION_APPLICABLE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api subscription extension with different stores
     */
    @Test
    public void testSubscriptionExtensionWithDifferentStoresPriceIds() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering9);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription3);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, quantity1,
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId3, quantity2,
            subscriptionIdBicCreditCard3, targetRenewalDate2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assertion on insufficient quantity
        AssertCollector.assertThat("PriceId's have different stores",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_DIFFERENT_STORES), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the price quotes api subscription extension with different currencies
     */
    @Test
    public void testSubscriptionExtensionWithDifferentCurrencyPriceIds() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOfferings11);
        offeringsList.add(bicOffering9);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription6);
        subscriptionList.add(subscription3);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription6.getNextBillingDate().split(" ")[0], 9);
        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId5, quantity1,
            subscriptionIdBicCreditCard6, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId3, quantity2,
            subscriptionIdBicCreditCard3, targetRenewalDate3, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        requestData.setEstimateVat(false);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assertion on insufficient quantity
        AssertCollector.assertThat("PriceIds have different currencies",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_DIFFERENT_CURRENCY), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test merging of the line items for align billing dates request.
     */
    @Test
    public void testMergeLineItemsForSubscriptionExtension() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);
        offeringsList.add(bicOffering7);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription1);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            true, assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(),
            equalTo(subscription1.getQuantity() + subscription1.getQuantity()), assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(),
            equalTo(subscription1.getQuantity() + subscription1.getQuantity()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test merging of the line items for align billing dates request.
     */
    @Test
    public void testMergeLineItemsForSubscriptionExtensionWhenPriceIdAndSubscriptionIdAreDifferent() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering7);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription7.getQuantity(),
            subscriptionIdBicCreditCard7, targetRenewalDate1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription7);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(subscription1.getQuantity()),
            assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(subscription1.getQuantity()),
            assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(1).getQuantity(), equalTo(subscription7.getQuantity()),
            assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(1).getQuantity(), equalTo(subscription7.getQuantity()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test align billing dates for expired prices request.
     */
    @Test
    public void testSubscriptionExtensionWithBothExpiredPrices() {

        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId1 = bicOffering1.getIncluded().getPrices().get(0).getId();
        final String priceId2 = bicOffering2.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder1 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder2 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);
        final String targetRenewalDate2 =
            Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], 13);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering1);
        offeringsList.add(bicOffering2);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId2, subscription2.getQuantity(),
            subscriptionIdBicCreditCard2, targetRenewalDate2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        priceQuotes.setData(requestData);

        final String updateQuery = "update subscription_price set end_date = '2014-01-01' where id = ";
        final String updateQuery1 = updateQuery + priceId1;
        final String updateQuery2 = updateQuery + priceId2;
        DbUtils.updateQuery(updateQuery1, getEnvironmentVariables());
        DbUtils.updateQuery(updateQuery2, getEnvironmentVariables());

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering1, null);
        offeringsJPromotionMap.put(bicOffering2, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test align billing dates for one expired and one active prices request.
     */
    @Test
    public void testSubscriptionExtensionWithOneExpiredAndOneActivePrices() {

        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        final String priceId1 = bicOffering1.getIncluded().getPrices().get(0).getId();
        final String priceId2 = bicOffering2.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder1 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId2, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder2 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);
        final String targetRenewalDate2 =
            Util.addDaysToAParticularDate(subscription2.getNextBillingDate().split(" ")[0], 13);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering1);
        offeringsList.add(bicOffering2);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId2, subscription2.getQuantity(),
            subscriptionIdBicCreditCard2, targetRenewalDate2, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        priceQuotes.setData(requestData);

        final String updateQuery = "update subscription_price set end_date = '2014-01-01' where id = ";
        final String updateQuery1 = updateQuery + priceId1;
        DbUtils.updateQuery(updateQuery1, getEnvironmentVariables());

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering1, null);
        offeringsJPromotionMap.put(bicOffering2, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test align billing dates for same expired price id and subscription id prices
     * request.
     */
    @Test
    public void testSubscriptionExtensionWithExpiredSamePriceIdAndSubscriptionId() {

        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        final String priceId1 = bicOffering1.getIncluded().getPrices().get(0).getId();

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder1 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering1);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;
        priceQuotes.setData(requestData);

        final String updateQuery = "update subscription_price set end_date = '2014-01-01' where id = ";
        final String updateQuery1 = updateQuery + priceId1;
        DbUtils.updateQuery(updateQuery1, getEnvironmentVariables());

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering1, null);

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription1);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            true, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test align billing dates with same price ids and different subscription ids with
     * different quantity
     */
    @Test
    public void testSubscriptionExtensionForSamePriceIdAndDifferentSubscriptionIdsOfDifferentQuantity() {

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard1 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), 5).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard1);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder1 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard1);
        final String subscriptionIdBicCreditCard1 =
            purchaseOrder1.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription1 = resource.subscription().getById(subscriptionIdBicCreditCard1);

        // Submit a purchase order with Credit card and process it to charged
        final String purchaseOrderIdForBicCreditCard2 = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId1, getBuyerUser(), 7).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForBicCreditCard2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForBicCreditCard2);

        // getting purchase order to get the subscription id
        final PurchaseOrder purchaseOrder2 = resource.purchaseOrder().getById(purchaseOrderIdForBicCreditCard2);
        final String subscriptionIdBicCreditCard2 =
            purchaseOrder2.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        final Subscription subscription2 = resource.subscription().getById(subscriptionIdBicCreditCard2);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        final List<Offerings> offeringsList = new ArrayList<>();
        offeringsList.add(bicOffering);

        final String targetRenewalDate1 =
            Util.addDaysToAParticularDate(subscription1.getNextBillingDate().split(" ")[0], 9);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription1.getQuantity(),
            subscriptionIdBicCreditCard1, targetRenewalDate1, null));
        lineItems.add(new JLineItem(PurchaseType.SUBSCRIPTIONEXTENSION, priceId1, subscription2.getQuantity(),
            subscriptionIdBicCreditCard2, targetRenewalDate1, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideDiscountAmountPromo1.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);

        final Double vatPercentage = 0.00;

        requestData.setLineItems(lineItems);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountAmountPromo1)));

        final List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // Assert line item level and total calculation
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.SUBSCRIPTIONEXTENSION.getName(), subscriptionList, getEnvironmentVariables(),
            false, assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(subscription1.getQuantity()),
            assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(0).getQuantity(), equalTo(subscription1.getQuantity()),
            assertionErrorList);

        AssertCollector.assertThat("Merging of line items is incorrect",
            priceQuotesResponse.getData().getLineItems().get(1).getQuantity(), equalTo(subscription2.getQuantity()),
            assertionErrorList);

        AssertCollector.assertThat("Quantity in totals is incorrect",
            priceQuotesResponse.getData().getLineItems().get(1).getQuantity(), equalTo(subscription2.getQuantity()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /***
     * This is a data provider which provides the boolean values for the test methods.
     *
     * The first boolean value represents applyShipping, true - apply, false - not applied. The second boolean value
     * represents applyAdditionalTax, true - apply, false - not applied. The third boolean value represents applyVat,
     * true - apply, false - not applied.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "AddSeatsDetails")
    private Object[][] getAddSeatsDetails() {
        return new Object[][] { { false, false, false }, { true, false, false }, { true, true, false },
                { true, true, true } };
    }

    @DataProvider(name = "AddSeatsWithNullValues")
    private Object[][] getAddSeatsWithNullValues() {
        return new Object[][] { { null, null, null }, { null, 1, subscriptionIdBicCreditCard1 },
                { priceId1, null, subscriptionIdBicCreditCard1 }, { priceId1, 1, null } };
    }

    @DataProvider(name = "InvalidSubscriptionId")
    private Object[][] getInvalidSubscriptionId() {
        return new Object[][] { { PelicanConstants.INVALID_NUMBER }, { "0" }, { "-1000000" } };
    }

    @DataProvider(name = "PurchaseTypes")
    private Object[][] getPurchaseTypes() {
        return new Object[][] { { "" }, { "INVALID_TYPE" } };
    }

    @DataProvider(name = "AddSeatsQuantityValues")
    private Object[][] getQuantityforAddSeats() {
        return new Object[][] { { 0 }, { -1 } };
    }

    /**
     * This is a data provider which passes the boolean is storewide, line item quantity and days to prorate to the
     * respective test method.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "BundlePromoForPercentageDetails")
    private Object[][] getBundlePromoForPercentageDetails() {
        return new Object[][] { { false, 1, 7 }, { false, 5, 9 }, { false, 8, 3 }, { true, 1, 13 }, { true, 5, 17 },
                { true, 9, 21 } };
    }

    /**
     * This is a method to construct the SubscriptionSyncRequestBuilder object
     *
     * @param usageType
     * @param plan
     * @param userExternalKey
     * @param promotionId
     * @param promotionCyclesUsed
     * @param promotionCyclesRemaining
     * @param appliedCount
     * @param newNextBillingDate
     * @return SubscriptionSyncRequestBuilder object
     */
    private SubscriptionSyncRequestBuilder constructRequestBuilder(final UsageType usageType,
        final SubscriptionPlan plan, final String userExternalKey, final long promotionId,
        final Integer promotionCyclesUsed, final int promotionCyclesRemaining, final int appliedCount,
        final String newNextBillingDate) {

        final SubscriptionSyncRequestBuilder builder = new SubscriptionSyncRequestBuilder();
        builder.setUsageType(usageType);
        builder.setPlan(plan);
        builder.setUserExternalKey(userExternalKey);
        builder.setPromotionId(promotionId);
        builder.setPromotionCyclesUsed(promotionCyclesUsed);
        builder.setPromotionCyclesRemaining(promotionCyclesRemaining);
        builder.setAppliedCount(appliedCount);
        builder.setNewNextBillingDate(newNextBillingDate);

        return builder;
    }
}
