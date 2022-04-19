package com.autodesk.bsm.pelican.api.pricequote;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.helpers.HelperForPriceQuote;
import com.autodesk.bsm.pelican.api.helpers.PriceQuoteRequestBuilder;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
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
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.PurchaseType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class tests Price Quotes response - With/Without VAT - With/Without Shipping - With/Without Promotion -
 * With/Without Bundle Promotion - With/Without Additional Fees
 * <p>
 * This class is inherited from Selenium Webdriver, because to change Add Seats Feature Flag in Admin tool
 *
 * @author Shweta Hegde
 */
public class PriceQuotesTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private PromotionUtils promotionUtils;
    private Offerings bicOffering;
    private Offerings bicOffering2;
    private Offerings bicOffering4;
    private Offerings bicOffering5;
    private Offerings bicOffering6;
    private Offerings bicOffering7;
    private Offerings bicOffering8;
    private Offerings bicOffering9;
    private Offerings bicOffering10;
    private Offerings metaOffering1;
    private Offerings metaOffering2;
    private Offerings basicOffering1;
    private Offerings basicOffering2;
    private Offerings basicOffering3;
    private Offerings basicOffering4;
    private Offerings basicOffering5;
    private Offerings basicOffering6;
    private Offerings basicOffering7;
    private JPromotion promo1;
    private static final String INVALID_PROMO_ID = "123456783456";
    private static final String INVALID_PROMO_CODE = "XXXXXXX";
    private JPromotion activeNonStoreWideDiscountAmountPromo1;
    private JPromotion activeNonStoreWideDiscountPercentPromo2;
    private JPromotion activeNonStoreWideSupplementTimePromo3;
    private JPromotion activeNonStoreWideDiscountAmountPromo4;
    private JPromotion activeNonStoreWideDiscountPercentPromo5;
    private JPromotion newNonStoreWideSupplementTimePromo6;
    private JPromotion activeNonStoreWideDiscountAmountPromo7;
    private JPromotion activeNonStoreWideDiscountPercentPromo8;
    private JPromotion newNonStoreWideDiscountPercentPromo9;
    private JPromotion activeStoreWidePromo1;
    private JPromotion activeStoreWidePromo2;
    private JPromotion activeBundledPromotionForMaxUses;
    private JPromotion activeBundledPromotionForMaxUsesPerUser;
    private JPromotion newStoreWidePromo3;
    private JPromotion activeStoreWidePromo4;
    private JPromotion activePromotionWithMaxUses;
    private JPromotion activePromotionWithMaxUsesPerUser;
    private JPromotion activeNonStorewideWithDiscountAmount;
    private JPromotion activeStoreWidePercentageBasicOfferingsBundlePromo;
    private JPromotion activeNonStoreWidePercentageBasicOfferingsBundlePromo;
    private JPromotion activeStoreWideCashAmountSubscriptionOffersBundlePromo;
    private JPromotion activeNonStoreWidePercentageSubscriptionOffersBundlePromo;
    private JPromotion activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo;
    private JPromotion activeNonStoreWideCashAmountSubscriptionOffersBundlePromo;
    private JPromotion activeStoreWideCashAmountBothOfferingsBundlePromo;
    private JPromotion activeNonStoreWidePercentageBothOfferingsBundlePromo;
    private JPromotion activeBicOfferingPromo;
    private JPromotion activeNonStorewideWithVat;
    private JPromotion activeStoreWidePercentageForTwoStores;
    private JPromotion activeStoreWidePercentageForOffering7;
    private JPromotion cancelledOnlyStoreWidePercentageForOffering7;
    private JPromotion activeStoreWidePercentageForOffering8;
    private JPromotion cancelledOnlyStoreWidePercentageForOffering8;
    private JPromotion activeStoreWidePercentageBicOfferingsBundlePromo;
    private JPromotion cancelledOnlyStoreWidePercentageBicOfferingsBundlePromo;
    private static String buyerId = "";
    private PriceQuotes priceQuotes;
    private PriceQuotes priceQuotesResponse;
    private PriceQuoteData requestData;
    private List<JAdditionalFee> additionalFees;
    private List<JLineItem> lineItems;
    private List<JLineItem> lineItemsForPriceQuote;
    private Shipping shipping;
    private List<JPromotionReference> promotionReferencesList;
    private Map<Offerings, List<JPromotion>> offeringsJPromotionMap;
    private Set<String> errorCodesSet;
    private Set<String> errorDetailsSet;
    private String priceIdForBasicOffering7;
    private String priceIdForBicOffering6;
    private String priceIdForMetaOffering2;
    private String priceIdForBicOffering2;
    private String priceIdOfCanadaStoreForBicOffering2;
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceQuotesTest.class.getSimpleName());

    /**
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        buyerId = getEnvironmentVariables().getUserExternalKey();
        // Create Canada store with same currency as of US Store
        final JStore canadaStore = storeApiUtils.addStore(Status.ACTIVE, Country.CA, Currency.USD, null, false);

        final JStore storeWithVat = storeApiUtils.addStore(resource, Status.ACTIVE, Country.FR, Currency.EUR, 10.0,
            null, false, null, null, getStoreTypeNameBic());

        // adding another price list to the same store
        final JPriceList jPriceList =
            storeApiUtils.addPriceListWithCountry(storeWithVat.getId(), Currency.GBP, Country.FR);

        final String priceListExternalKeyOfEmeaStore =
            storeWithVat.getIncluded().getPriceLists().get(0).getExternalKey();

        // Add active basic offering
        basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 100, UsageType.COM, null, null);
        basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 500, UsageType.COM, null, null);
        basicOffering3 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering4 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering5 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering6 = basicOfferingApiUtils.addBasicOffering(priceListExternalKeyOfEmeaStore,
            OfferingType.PHYSICAL_MEDIA, MediaType.ELD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering7 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.CURRENCY,
            MediaType.DVD, Status.ACTIVE, 100, UsageType.COM, null, null);

        // Add subscription Offerings
        bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        // adding another price to the same subscription offer
        SubscriptionOfferPrice subscriptionOfferPrice = subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOffer(40, jPriceList.getData().getExternalKey(), 0, 30);
        priceIdForBicOffering2 = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionOfferPrice,
            bicOffering.getOfferings().get(0).getId(), bicOffering.getIncluded().getBillingPlans().get(0).getId())
            .getData().getId();

        bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        // Add another price to the same offer
        subscriptionOfferPrice = subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(40,
            canadaStore.getIncluded().getPriceLists().get(0).getExternalKey(), 0, 30);
        priceIdOfCanadaStoreForBicOffering2 = subscriptionPlanApiUtils
            .addPricesToSubscriptionOffer(resource, subscriptionOfferPrice, bicOffering2.getOfferings().get(0).getId(),
                bicOffering2.getIncluded().getBillingPlans().get(0).getId())
            .getData().getId();

        final Offerings bicOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOffering4 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        bicOffering5 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOffering6 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOffering7 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOffering8 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOffering9 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOffering10 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        metaOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKeyOfEmeaStore,
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        metaOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        activeNonStoreWideDiscountAmountPromo1 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStoreWideDiscountPercentPromo2 = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, "15.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 3, null, null);
        activeNonStoreWideSupplementTimePromo3 = promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, null, DateTimeUtils.getUTCFutureExpirationDate(), "5", "DAY", 2, null, null);
        activeNonStoreWideDiscountAmountPromo4 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStoreWideDiscountPercentPromo5 = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, "15.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        newNonStoreWideSupplementTimePromo6 = promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.NEW, null, null, DateTimeUtils.getUTCFutureExpirationDate(), "10", "DAY", 2, null, null);
        activeNonStoreWideDiscountAmountPromo7 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering1), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStoreWideDiscountPercentPromo8 = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, "20.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        newNonStoreWideDiscountPercentPromo9 = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering3), promotionUtils.getRandomPromoCode(),
            false, Status.NEW, "20.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeStoreWidePromo1 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeStoreWidePromo2 = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering1), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, "20.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        newStoreWidePromo3 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering2), promotionUtils.getRandomPromoCode(),
            true, Status.NEW, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeStoreWidePromo4 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering3), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "30.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activePromotionWithMaxUses = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering1), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, "1", "1");
        activePromotionWithMaxUsesPerUser = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering1), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, "2", "1");
        activeBicOfferingPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(storeWithVat), Lists.newArrayList(bicOffering), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "50.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStorewideWithVat =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(storeWithVat),
                Lists.newArrayList(bicOffering4), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null,
                "100.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        activeNonStorewideWithDiscountAmount = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(storeWithVat), Lists.newArrayList(bicOffering5), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "50.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        final String promoCode7 = promotionUtils.getRandomPromoCode();
        activeStoreWidePercentageForOffering7 = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering7), promoCode7, true, Status.ACTIVE, null,
            "50.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOffering7), promoCode7, true, Status.CANCELLED, null, "70.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        final String promoCode8 = promotionUtils.getRandomPromoCode();
        activeStoreWidePercentageForOffering8 = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering8), promoCode8, true, Status.ACTIVE,
            "10.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOffering8), promoCode8, true, Status.CANCELLED, "15.00", null,
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        cancelledOnlyStoreWidePercentageForOffering7 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering7), promotionUtils.getRandomPromoCode(), true, Status.CANCELLED, "15.00",
                null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        cancelledOnlyStoreWidePercentageForOffering8 =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicOffering8), promotionUtils.getRandomPromoCode(), true, Status.CANCELLED, "15.00",
                null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        // Create Bundle Promo Offerings with Basic Offerings
        final List<BundlePromoOfferings> bundleOfBasicOfferingsList = new ArrayList<>();
        bundleOfBasicOfferingsList.add(promotionUtils.createBundlePromotionOffering(basicOffering4, 5, true));
        bundleOfBasicOfferingsList.add(promotionUtils.createBundlePromotionOffering(basicOffering5, 5, true));

        // bundle storewide percentage promotion creation
        activeStoreWidePercentageBasicOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                bundleOfBasicOfferingsList, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "20", null,
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Create Bundle Promo Offerings with bic Offerings
        final List<BundlePromoOfferings> bundleOfBicOfferingsList = new ArrayList<>();
        bundleOfBicOfferingsList.add(promotionUtils.createBundlePromotionOffering(bicOffering9, 3, true));
        bundleOfBicOfferingsList.add(promotionUtils.createBundlePromotionOffering(bicOffering10, 2, true));

        // bundle storewide cash amount promotion creation
        final String promoCode9 = promotionUtils.getRandomPromoCode();
        activeStoreWidePercentageBicOfferingsBundlePromo = promotionUtils.addBundlePromotion(
            PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()), bundleOfBicOfferingsList, promoCode9, true,
            Status.ACTIVE, null, "20.00", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            bundleOfBicOfferingsList, promoCode9, true, Status.CANCELLED, null, "20.00",
            DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        cancelledOnlyStoreWidePercentageBicOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                bundleOfBicOfferingsList, promotionUtils.getRandomPromoCode(), true, Status.CANCELLED, null, "20.00",
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // bundle non storewide percentage promotion creation
        activeNonStoreWidePercentageBasicOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                bundleOfBasicOfferingsList, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "30", null,
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Create Bundle Promo Offerings with Subscription Offers
        List<BundlePromoOfferings> bundleOfSubscriptionOffersList = new ArrayList<>();
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(bicOffering, 3, true));
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(bicOffering4, 3, true));
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(metaOffering1, 3, true));

        // bundle storewide Cash Amount promotion creation
        activeStoreWideCashAmountSubscriptionOffersBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(storeWithVat),
                bundleOfSubscriptionOffersList, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, null, "200",
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // bundle non storewide percentage promotion creation
        activeNonStoreWidePercentageSubscriptionOffersBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(storeWithVat),
                bundleOfSubscriptionOffersList, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "20", null,
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // bundle non storewide cash amount promotion creation
        activeNonStoreWideCashAmountSubscriptionOffersBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(storeWithVat),
                bundleOfSubscriptionOffersList, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null, "10",
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Create Bundle Promo Offerings with only BIC Subscription Offers
        bundleOfSubscriptionOffersList = new ArrayList<>();
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(bicOffering4, 2, true));
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(bicOffering5, 2, true));

        // bundle non storewide percentage promotion creation
        activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(storeWithVat),
                bundleOfSubscriptionOffersList, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "50", null,
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Create Bundle Promo Offerings with Subscription Offers & Basic
        // Offerings
        final List<BundlePromoOfferings> bundleOfSubscriptionOffersAndBasicOfferings = new ArrayList<>();
        bundleOfSubscriptionOffersAndBasicOfferings
            .add(promotionUtils.createBundlePromotionOffering(bicOffering5, 2, true));
        bundleOfSubscriptionOffersAndBasicOfferings
            .add(promotionUtils.createBundlePromotionOffering(basicOffering6, 2, true));

        // bundle storewide Cash Amount promotion creation
        activeStoreWideCashAmountBothOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(storeWithVat),
                bundleOfSubscriptionOffersAndBasicOfferings, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE,
                null, "200", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // bundle non storewide percentage promotion creation
        activeNonStoreWidePercentageBothOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(storeWithVat),
                bundleOfSubscriptionOffersAndBasicOfferings, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "20", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // bundle promotion for 2 stores
        final List<BundlePromoOfferings> bundleOfSubscriptionOffersFor2Stores = new ArrayList<>();
        bundleOfSubscriptionOffersFor2Stores.add(promotionUtils.createBundlePromotionOffering(bicOffering, 2, true));
        bundleOfSubscriptionOffersFor2Stores.add(promotionUtils.createBundlePromotionOffering(bicOffering2, 2, true));
        bundleOfSubscriptionOffersFor2Stores.add(promotionUtils.createBundlePromotionOffering(bicOffering3, 2, true));
        bundleOfSubscriptionOffersFor2Stores.add(promotionUtils.createBundlePromotionOffering(bicOffering4, 2, true));

        // non storewide bundle promotion for 2 stores
        activeStoreWidePercentageForTwoStores = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(storeWithVat, getStoreUs()), bundleOfSubscriptionOffersFor2Stores,
            promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "20", null,
            DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Create Bundle Promo Offerings with Basic Offerings, Meta Subscription
        // plan and Bic Subscirption plan
        final List<BundlePromoOfferings> bundleOfOfferingsForMaxUse = new ArrayList<>();
        bundleOfOfferingsForMaxUse.add(promotionUtils.createBundlePromotionOffering(basicOffering7, 1, true));
        bundleOfOfferingsForMaxUse.add(promotionUtils.createBundlePromotionOffering(metaOffering2, 1, true));
        bundleOfOfferingsForMaxUse.add(promotionUtils.createBundlePromotionOffering(bicOffering6, 1, true));
        priceIdForBasicOffering7 = basicOffering7.getIncluded().getPrices().get(0).getId();
        priceIdForMetaOffering2 = metaOffering2.getIncluded().getPrices().get(0).getId();
        priceIdForBicOffering6 = bicOffering6.getIncluded().getPrices().get(0).getId();

        // bundle promo to validate error exceeding max uses count
        activeBundledPromotionForMaxUses = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), bundleOfOfferingsForMaxUse, promotionUtils.getRandomPromoCode(), false,
            Status.ACTIVE, null, "200", DateTimeUtils.getUTCFutureExpirationDate(), 1, "1", null);

        // bundle promo to validate error exceeding max uses per user count
        activeBundledPromotionForMaxUsesPerUser = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), bundleOfOfferingsForMaxUse, promotionUtils.getRandomPromoCode(), false,
            Status.ACTIVE, null, "200", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, "1");
    }

    /**
     * Get Price Quote for single line item with single promotion
     * <p>
     * Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteSingleLineItemSuccess() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideDiscountAmountPromo7.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(basicOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountAmountPromo7)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for multiple line items with multiple promotions (promoCode and promoId) references
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteMultipleLineItemsSuccess() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(basicOffering2.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideDiscountAmountPromo7.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStoreWideDiscountPercentPromo8.getData().getCustomPromoCode()));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(basicOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountAmountPromo7)));
        offeringsJPromotionMap.put(basicOffering2,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountPercentPromo8)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a line item with more than one promotion references, Non-store wide Promo wins over store
     * wide promo
     * <p>
     * For a Line item, below are the promotions sent in the order 1. active Store wide 2. Inactive promo 3. Invalid
     * promo 4. active non store wide promo (5 promo refernces) 5. non-applicable 1st nonstorewide promo which is active
     * wins
     *
     * @result Validate price totals of the shopping cart items, Information message displays “multiple valid promotions
     *         for line item, applied promo code promo”.
     */
    @Test
    public void testSuccessFirstNonStoreWidePromoWinsOverStoreWideForSingleLineItem() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering2.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        // store wide active promo
        promotionReferencesList.add(new JPromotionReference(activeStoreWidePromo1.getData().getId(), null));
        // new promo
        promotionReferencesList
            .add(new JPromotionReference(newNonStoreWideSupplementTimePromo6.getData().getId(), null));
        // invalid promo
        promotionReferencesList.add(new JPromotionReference(null, INVALID_PROMO_CODE));
        // valid promo, which gets applied eventually
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStoreWideDiscountAmountPromo1.getData().getCustomPromoCode()));
        // 2nd valid promo
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStoreWideDiscountPercentPromo2.getData().getCustomPromoCode()));
        // 3rd valid promo
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideSupplementTimePromo3.getData().getId(), null));
        // 4th valid promo
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideDiscountAmountPromo4.getData().getId(), null));
        // 5th valid promo
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideDiscountPercentPromo5.getData().getId(), null));
        // Non-applicable promo
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStoreWideDiscountAmountPromo7.getData().getCustomPromoCode()));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering2,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountAmountPromo1)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        errorCodesSet = new HashSet<>();
        errorCodesSet.add(PelicanErrorConstants.INVALID_PROMOTION_CODE);
        errorCodesSet.add(PelicanErrorConstants.NOT_APPLICABLE_PROMOTION);
        errorCodesSet.add(PelicanErrorConstants.INACTIVE_PROMOTION);
        errorCodesSet.add(PelicanErrorConstants.MORE_THAN_ONE_PROMOS_PER_PRICE_FIRST_PROMO_SELECTED);
        errorCodesSet.add(PelicanErrorConstants.MORE_THAN_ONE_PROMOS_PER_PRICE_PROMO_CODE_SELECTED);

        errorDetailsSet = new HashSet<>();
        errorDetailsSet.add(PelicanErrorConstants.INVALID_PROMOTION_ERROR_MESSAGE + INVALID_PROMO_CODE);
        errorDetailsSet.add("Promotion with ID: " + activeNonStoreWideDiscountAmountPromo7.getData().getId()
            + " is not applicable for any of the offerings.");
        errorDetailsSet
            .add("Promotion with ID: " + newNonStoreWideSupplementTimePromo6.getData().getId() + " is not active.");
        errorDetailsSet.add(PelicanErrorConstants.MULTIPLE_VALID_PROMO_CODES_FOR_LINE_ITEM_APPLIED_FIRST_ONE);
        errorDetailsSet.add(PelicanErrorConstants.MULTIPLE_VALID_PROMOTIONS_FOR_LINE_ITEMS_APPLIED_PROMO_CODE_PROMO);

        // Assert error codes and details
        for (final Errors error : priceQuotesResponse.getErrors()) {
            AssertCollector.assertTrue("Error code is missing in price quote response",
                errorCodesSet.contains(error.getCode()), assertionErrorList);
            AssertCollector.assertTrue("Error detail is missing in price quote response",
                errorDetailsSet.contains(error.getDetail()), assertionErrorList);

        }
        AssertCollector.assertThat("Incorrect number of errors", priceQuotesResponse.getErrors().size(), is(5),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a line item with more than one promotion references, Non-store wide Promo wins over store
     * wide promo for multiple line items
     * <p>
     * For Line item1, ACTIVE non storewide, ACTIVE Storewide - nonstorewide wins For Line item2, ACTIVE non storewide,
     * INACTIVE Storewide - nonstorewide wins For Line item3, INACTIVE non storewide, ACTIVE Storewide - storewide wins
     *
     * @result Validate price totals of the shopping cart items, Information message displays “multiple valid promotions
     *         for line item, applied promo code promo”.
     */
    @Test
    public void testSuccessFirstNonStoreWidePromoWinsOverStoreWideForMultipleLineItems() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(basicOffering2.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(basicOffering3.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        // for basic offering1, active storewide and active non-storewide
        promotionReferencesList.add(new JPromotionReference(activeStoreWidePromo2.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStoreWideDiscountAmountPromo7.getData().getCustomPromoCode()));
        // for basic offering2, inactive storewide and active non-storewide
        promotionReferencesList.add(new JPromotionReference(null, newStoreWidePromo3.getData().getCustomPromoCode()));
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStoreWideDiscountPercentPromo8.getData().getCustomPromoCode()));
        // for basic offering3, active storewide and inactive non-storewide
        promotionReferencesList
            .add(new JPromotionReference(null, activeStoreWidePromo4.getData().getCustomPromoCode()));
        promotionReferencesList
            .add(new JPromotionReference(newNonStoreWideDiscountPercentPromo9.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(basicOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountAmountPromo7)));
        offeringsJPromotionMap.put(basicOffering2,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountPercentPromo8)));
        offeringsJPromotionMap.put(basicOffering3, new ArrayList<>(ImmutableList.of(activeStoreWidePromo4)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        errorCodesSet = new HashSet<>();
        errorCodesSet.add(PelicanErrorConstants.MORE_THAN_ONE_PROMOS_PER_PRICE_PROMO_CODE_SELECTED);
        errorCodesSet.add(PelicanErrorConstants.INACTIVE_PROMOTION);

        errorDetailsSet = new HashSet<>();
        errorDetailsSet.add(PelicanErrorConstants.MULTIPLE_VALID_PROMOTIONS_FOR_LINE_ITEMS_APPLIED_PROMO_CODE_PROMO);
        errorDetailsSet.add("Promotion with ID: " + newStoreWidePromo3.getData().getId() + " is not active.");
        errorDetailsSet
            .add("Promotion with ID: " + newNonStoreWideDiscountPercentPromo9.getData().getId() + " is not active.");

        // Assert error codes and details
        for (final Errors error : priceQuotesResponse.getErrors()) {
            AssertCollector.assertTrue("Error code is missing in price quote response",
                errorCodesSet.contains(error.getCode()), assertionErrorList);
            AssertCollector.assertTrue("Error detail is missing in price quote response",
                errorDetailsSet.contains(error.getDetail()), assertionErrorList);

        }

        AssertCollector.assertThat("Incorrect number of errors", priceQuotesResponse.getErrors().size(), is(3),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case for testing the price quotes with a promo code of a store wide promotion which is in active
     * and cancelled state
     */
    @Test
    public void testPriceQuotesMultipleLineItemsWithActiveAndCancelledPromoCode() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering7.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(bicOffering8.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        shipping = HelperForPriceQuote.getNamerShippingDetails(true);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(null, activeStoreWidePercentageForOffering7.getData().getCustomPromoCode()));
        promotionReferencesList
            .add(new JPromotionReference(null, activeStoreWidePercentageForOffering8.getData().getCustomPromoCode()));

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7,
            new ArrayList<>(ImmutableList.of(activeStoreWidePercentageForOffering7)));
        offeringsJPromotionMap.put(bicOffering8,
            new ArrayList<>(ImmutableList.of(activeStoreWidePercentageForOffering8)));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder =
            PriceQuoteRequestBuilder.builder().setBuyerId(buyerId).setLineItems(lineItems)
                .setAdditionalFees(additionalFees).setPromotionReferences(promotionReferencesList).setResource(resource)
                .setEnvironmentVariables(getEnvironmentVariables()).setOfferingsJPromotionMap(offeringsJPromotionMap)
                .setPurchaseType(PurchaseType.OFFERING).setSubscriptionList(null).setShipping(shipping).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case for testing the price quotes with a promo code of a store wide promotion which is in
     * cancelled state
     */
    @Test
    public void testPriceQuotesMultipleLineItemsWithCancelledPromoCode() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering7.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(bicOffering8.getIncluded().getPrices().get(0).getId(), additionalFees));

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(null, cancelledOnlyStoreWidePercentageForOffering7.getData().getCustomPromoCode()));
        promotionReferencesList.add(
            new JPromotionReference(null, cancelledOnlyStoreWidePercentageForOffering8.getData().getCustomPromoCode()));

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering7, null);
        offeringsJPromotionMap.put(bicOffering8, null);

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder =
            PriceQuoteRequestBuilder.builder().setBuyerId(buyerId).setLineItems(lineItems)
                .setAdditionalFees(additionalFees).setPromotionReferences(promotionReferencesList).setResource(resource)
                .setEnvironmentVariables(getEnvironmentVariables()).setOfferingsJPromotionMap(offeringsJPromotionMap)
                .setPurchaseType(PurchaseType.OFFERING).setSubscriptionList(null).setShipping(shipping).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case for testing the price quotes with a bundle promo code of a store wide promotion which is in
     * active and cancelled state
     */
    @Test
    public void testPriceQuotesMultipleLineItemsWithActiveAndCancelledBundlePromoCode() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);
        additionalFees = new ArrayList<>();
        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering9.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        lineItems.add(new JLineItem(bicOffering10.getIncluded().getPrices().get(0).getId(), 2, additionalFees));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null,
            activeStoreWidePercentageBicOfferingsBundlePromo.getData().getCustomPromoCode()));

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering9,
            new ArrayList<>(ImmutableList.of(activeStoreWidePercentageBicOfferingsBundlePromo)));
        offeringsJPromotionMap.put(bicOffering10,
            new ArrayList<>(ImmutableList.of(activeStoreWidePercentageBicOfferingsBundlePromo)));

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder =
            PriceQuoteRequestBuilder.builder().setBuyerId(buyerId).setLineItems(lineItems)
                .setAdditionalFees(additionalFees).setPromotionReferences(promotionReferencesList).setResource(resource)
                .setEnvironmentVariables(getEnvironmentVariables()).setOfferingsJPromotionMap(offeringsJPromotionMap)
                .setPurchaseType(PurchaseType.OFFERING).setSubscriptionList(null).setShipping(shipping).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case for testing the price quotes with a bundle promo code of a store wide promotion which is in
     * cancelled state
     */
    @Test
    public void testPriceQuotesMultipleLineItemsWithCancelledBundlePromoCode() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);
        additionalFees = new ArrayList<>();
        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering9.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        lineItems.add(new JLineItem(bicOffering10.getIncluded().getPrices().get(0).getId(), 2, additionalFees));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null,
            cancelledOnlyStoreWidePercentageBicOfferingsBundlePromo.getData().getCustomPromoCode()));

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering9, null);
        offeringsJPromotionMap.put(bicOffering10, null);

        final PriceQuoteRequestBuilder priceQuoteRequestBuilder =
            PriceQuoteRequestBuilder.builder().setBuyerId(buyerId).setLineItems(lineItems)
                .setAdditionalFees(additionalFees).setPromotionReferences(promotionReferencesList).setResource(resource)
                .setEnvironmentVariables(getEnvironmentVariables()).setOfferingsJPromotionMap(offeringsJPromotionMap)
                .setPurchaseType(PurchaseType.OFFERING).setSubscriptionList(null).setShipping(shipping).build();

        HelperForPriceQuote.createPriceQuoteRequestDataForOfferingPurchaseType(priceQuoteRequestBuilder,
            assertionErrorList, resource);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for line items corresponding to promotions with invalid status (i.e NEW/CANCELLED/EXPIRED)
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteInvalidPromotionStatus() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(basicOffering2.getIncluded().getPrices().get(0).getId(), additionalFees));
        lineItems.add(new JLineItem(basicOffering3.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        final String promoCode1 = promotionUtils.getRandomPromoCode();
        promo1 = promotionUtils.getDiscountPercentPromo(getStoreUs(), basicOffering1, promoCode1, false, Status.NEW,
            10.0, DateTimeUtils.getUTCFutureExpirationDate(), 1);
        final String promoCode2 = promotionUtils.getRandomPromoCode();
        final JPromotion promo2 = promotionUtils.getDiscountPercentPromo(getStoreUs(), basicOffering2, promoCode2,
            false, Status.EXPIRED, 20.0, DateTimeUtils.getUTCFutureExpirationDate(), 1);
        final String promoCode3 = promotionUtils.getRandomPromoCode();
        final JPromotion promo3 = promotionUtils.getDiscountPercentPromo(getStoreUs(), basicOffering3, promoCode3,
            false, Status.CANCELLED, 20.0, DateTimeUtils.getUTCFutureExpirationDate(), 1);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(promo1.getData().getId(), null)); // new
        // promo
        promotionReferencesList.add(new JPromotionReference(promo2.getData().getId(), null)); // expired
        // promo
        promotionReferencesList.add(new JPromotionReference(null, promo3.getData().getCustomPromoCode())); // cancelled
        // promo
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(3),
            assertionErrorList);
        final Set<String> promotionIdsSet = new HashSet<>();
        promotionIdsSet.add(promo1.getData().getId());
        promotionIdsSet.add(promo2.getData().getId());
        promotionIdsSet.add(promo3.getData().getId());

        for (final Errors error : priceQuotesResponse.getErrors()) {
            AssertCollector.assertThat("Error status doesn't match", error.getStatus(), equalTo(HttpStatus.SC_OK),
                assertionErrorList);
            AssertCollector.assertThat("Error detail doesn't match", error.getDetail(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Promotion linkage type doesn't match",
                error.getLinks().getPromotions().getLinkage().get(0).getType(), equalTo(EntityType.PROMOTION),
                assertionErrorList);
            AssertCollector.assertThat("Error links price type doesn't match", error.getLinks().getPrice(), nullValue(),
                assertionErrorList);

            AssertCollector.assertTrue("Promotion linkage id doesn't match",
                promotionIdsSet.contains(error.getLinks().getPromotions().getLinkage().get(0).getId()),
                assertionErrorList);

            AssertCollector.assertThat("Promotion linkage request code doesn't match",
                error.getLinks().getPromotions().getLinkage().get(0).getMeta().getRequestedCode(),
                isOneOf(promoCode1, promoCode2, promoCode3), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a line item corresponding to a promotion which have exceeded the maximum number of uses
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuotePromoExceedsMaxUses() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();

        final PromotionReferences promotionReferencesForPO = new PromotionReferences();
        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(activePromotionWithMaxUses.getData().getId());
        promotionReferencesForPO.setPromotionReference(promotionReference);

        // Submit purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(basicOffering1.getIncluded().getPrices().get(0).getId(), 1)), false,
            PaymentType.CREDIT_CARD,
            new HashMap<>(
                ImmutableMap.of(basicOffering1.getIncluded().getPrices().get(0).getId(), promotionReferencesForPO)),
            null);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrder.getId());

        // promotion exceeded max uses
        promotionReferencesList.add(new JPromotionReference(activePromotionWithMaxUses.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo("exceeds-max-use-count"), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links price id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getId(),
            equalTo(activePromotionWithMaxUses.getData().getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a line item corresponding to a promotion which have exceeded the maximum number of uses per
     * user
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuotePromoExceedsMaxUsesPerUser() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();

        final PromotionReferences promotionReferencesForPO = new PromotionReferences();
        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(activePromotionWithMaxUsesPerUser.getData().getId());
        promotionReferencesForPO.setPromotionReference(promotionReference);

        // Submit purchase order
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(basicOffering1.getIncluded().getPrices().get(0).getId(), 1)), false,
            PaymentType.CREDIT_CARD,
            new HashMap<>(
                ImmutableMap.of(basicOffering1.getIncluded().getPrices().get(0).getId(), promotionReferencesForPO)),
            null);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrder.getId());

        // promotion exceeded maxUsesPerUser
        promotionReferencesList.add(new JPromotionReference(activePromotionWithMaxUsesPerUser.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo("exceeds-max-use-count-per-user"), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links price id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getId(),
            equalTo(activePromotionWithMaxUsesPerUser.getData().getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a non-existing promotion id
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuotePromoIdDoesntExists() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering2.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(INVALID_PROMO_ID, null));// promotion
        // doesn't
        // exists
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo("invalid-promotion-id"), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getId(),
            equalTo(INVALID_PROMO_ID), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a non-existing promotion code
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuotePromoCodeDoesntExists() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering3.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(null, INVALID_PROMO_CODE));// promotion
        // doesn't
        // exists
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.INVALID_PROMOTION_CODE), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match", priceQuotesResponse.getErrors().get(0).getLinks()
            .getPromotions().getLinkage().get(0).getMeta().getRequestedCode(), equalTo(INVALID_PROMO_CODE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for a good promotion but not associated to the given line item
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuotePromoNotApplicable() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        final String promoCode1 = promotionUtils.getRandomPromoCode();
        promo1 = promotionUtils.getDiscountPercentPromo(getStoreUs(), basicOffering2, promoCode1, false, Status.ACTIVE,
            15.0, DateTimeUtils.getUTCFutureExpirationDate(), 1);
        promotionReferencesList.add(new JPromotionReference(promo1.getData().getId(), null)); // promotion
        // not
        // applicable
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.NOT_APPLICABLE_PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getId(),
            equalTo(promo1.getData().getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for single line item with VAT
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteWithVAT() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 4, additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getEmeaShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(activeNonStorewideWithVat.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        requestData.setEstimateVat(true); // setting estimateVat as true
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering4, new ArrayList<>(ImmutableList.of(activeNonStorewideWithVat)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            10.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        // Assert errors
        AssertCollector.assertThat("No error should be thrown", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for single line item without VAT
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteWithoutVAT() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(basicOffering3.getIncluded().getPrices().get(0).getId(), additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        // Adding promotion references
        promotionReferencesList.add(new JPromotionReference(activeStoreWidePromo4.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(basicOffering3, new ArrayList<>(ImmutableList.of(activeStoreWidePromo4)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        // Assert errors
        AssertCollector.assertThat("No error should be thrown", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for bic products in the following combination
     * <p>
     * 1. single line item 2. more than one seat 3. single promotion 4. without VAT
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteBicSingleLineItemWithMoreThanOneSeatWithOutVatSuccess() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering2.getIncluded().getPrices().get(0).getId(), 2, additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideDiscountPercentPromo2.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering2,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideDiscountPercentPromo2)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        // Assert errors
        AssertCollector.assertThat("No error should be thrown", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for bic products in the following combination
     * <p>
     * 1. multiple line items 2. more than one seat 3. multiple promotions 4. without VAT
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteBicMultiLineItemsWithMoreThanOneSeatWithOutVatSuccess() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering2.getIncluded().getPrices().get(0).getId(), 2, additionalFees));
        lineItems.add(new JLineItem(basicOffering1.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getNamerShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeNonStoreWideSupplementTimePromo3.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(null, activeStoreWidePromo2.getData().getCustomPromoCode()));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering2,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideSupplementTimePromo3)));
        offeringsJPromotionMap.put(basicOffering1, new ArrayList<>(ImmutableList.of(activeStoreWidePromo2)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        // Assert errors
        AssertCollector.assertThat("There should not be error", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get Price Quote for bic products in the following combination
     * <p>
     * 1. multiple line items 2. more than one seat 3. multiple promotions 4. with VAT
     *
     * @result Validate price totals of the shopping cart items
     */
    @Test
    public void getPriceQuoteBicMultiLineItemsWithMoreThanOneSeatWithVatSuccess() {
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding additional fees
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 4, additionalFees));
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 5, additionalFees));
        requestData.setLineItems(lineItems);

        // Adding shipping details
        shipping = HelperForPriceQuote.getEmeaShippingDetails(true);
        requestData.setShipping(shipping);

        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(activeBicOfferingPromo.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStorewideWithVat.getData().getCustomPromoCode()));
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering, new ArrayList<>(ImmutableList.of(activeBicOfferingPromo)));
        offeringsJPromotionMap.put(bicOffering4, new ArrayList<>(ImmutableList.of(activeNonStorewideWithVat)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        // Assert errors
        AssertCollector.assertThat("There should not be error", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests price quote with bundle promotion These tests also tests splitting line items based on least
     * common denominator
     *
     * @param offeringsList
     * @param promotion
     * @param quantity
     * @param applyShipping
     * @param applyAdditionalTax
     */
    @Test(dataProvider = "bundlePromotionDetails")
    public void testSuccessApplyBundleStoreWidePromotion(final List<Offerings> offeringsList,
        final JPromotion promotion, final int quantity, final boolean applyShipping, final boolean applyAdditionalTax,
        final boolean applyVat) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = new ArrayList<>();
        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        // Adding line items
        lineItems = new ArrayList<>();
        for (final Offerings offering : offeringsList) {
            lineItems.add(new JLineItem(offering.getIncluded().getPrices().get(0).getId(), quantity, additionalFees));
        }
        requestData.setLineItems(lineItems);

        if (applyShipping) {
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }
            requestData.setShipping(shipping);
        }

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(promotion.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        Double vatPercentage = 0.00;
        if (applyVat) {
            vatPercentage = 10.00;
            requestData.setEstimateVat(true); // setting estimateVat as true
        }

        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        for (final Offerings offering : offeringsList) {
            offeringsJPromotionMap.put(offering, new ArrayList<>(ImmutableList.of(promotion)));
        }

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        AssertCollector.assertThat("There should not be error", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "bundlePromotionDetails")
    private Object[][] getBundlePromotionDetails() {
        return new Object[][] {
                // Storewide Percentage promo for 2 basic offerings with
                // Shipping and Additional fees
                { new ArrayList<>(ImmutableList.of(basicOffering4, basicOffering5)),
                        activeStoreWidePercentageBasicOfferingsBundlePromo, 5, true, true, false },
                // Non Storewide Percentage promo for 2 basic offerings with
                // Shipping and Additional fees
                { new ArrayList<>(ImmutableList.of(basicOffering4, basicOffering5)),
                        activeNonStoreWidePercentageBasicOfferingsBundlePromo, 5, true, true, false },
                // Storewide Percentage promo for 2 basic offerings with only
                // Shipping
                { new ArrayList<>(ImmutableList.of(basicOffering4, basicOffering5)),
                        activeStoreWidePercentageBasicOfferingsBundlePromo, 8, true, false, false },
                // Non Storewide Percentage promo for 2 basic offerings with
                // only Additional fees
                { new ArrayList<>(ImmutableList.of(basicOffering4, basicOffering5)),
                        activeNonStoreWidePercentageBasicOfferingsBundlePromo, 20, false, true, false },
                // Storewide Cash Amount promo for 2 bic and 1 meta offerings
                // with Shipping and with Additional fees
                { new ArrayList<>(ImmutableList.of(bicOffering, bicOffering4, metaOffering1)),
                        activeStoreWideCashAmountSubscriptionOffersBundlePromo, 11, false, true, true },
                // Non Storewide Cash Amount promo for 2 bic and 1 meta
                // offerings without Shipping and Additional fees
                { new ArrayList<>(ImmutableList.of(bicOffering, bicOffering4, metaOffering1)),
                        activeNonStoreWideCashAmountSubscriptionOffersBundlePromo, 10, false, false, true },
                // Non Storewide Cash Amount promo for 2 bic and 1 meta
                // offerings without Shipping and with Additional fees
                { new ArrayList<>(ImmutableList.of(bicOffering, bicOffering4, metaOffering1)),
                        activeNonStoreWideCashAmountSubscriptionOffersBundlePromo, 3, false, true, true },
                // Non Storewide Percentage promo for 2 bic and 1 meta offerings
                // without Shipping and Additional fees
                { new ArrayList<>(ImmutableList.of(bicOffering, bicOffering4, metaOffering1)),
                        activeNonStoreWidePercentageSubscriptionOffersBundlePromo, 9, false, false, true },
                // Storewide Cash Amount promo for 1 basic and 1 meta offerings
                // without Shipping and Additional fees
                { new ArrayList<>(ImmutableList.of(basicOffering6, bicOffering5)),
                        activeStoreWideCashAmountBothOfferingsBundlePromo, 2, false, false, false },
                // Non Storewide Percentage promo for 1 basic and 1 meta
                // offerings without Shipping and with Additional fees
                { new ArrayList<>(ImmutableList.of(basicOffering6, bicOffering5)),
                        activeNonStoreWidePercentageBothOfferingsBundlePromo, 4, false, true, false },
                // Non Storewide Percentage promo for 1 basic and 1 meta
                // offerings without Shipping and Additional fees
                { new ArrayList<>(ImmutableList.of(basicOffering6, bicOffering5)),
                        activeNonStoreWidePercentageBothOfferingsBundlePromo, 33, false, false, false } };
    }

    /**
     * This method tests error thrown when insufficient quantity is provided with the line items Error : does not have
     * the minimum quantity required for the purchase.
     *
     * @param offeringsList
     * @param promotion
     * @param quantity
     * @param applyShipping
     * @param applyAdditionalTax
     */
    @Test(dataProvider = "bundlePromotionDetails")
    public void testErrorForInsufficientQuantityToApplyBundlePromotion(final List<Offerings> offeringsList,
        final JPromotion promotion, final int quantity, final boolean applyShipping, final boolean applyAdditionalTax,
        final boolean applyVat) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        // Adding line items
        lineItems = new ArrayList<>();
        // Quantity is given lesser than bundle promotion quantity
        for (final Offerings offering : offeringsList) {
            lineItems.add(new JLineItem(offering.getIncluded().getPrices().get(0).getId(), 1, additionalFees));
        }
        requestData.setLineItems(lineItems);

        if (applyShipping) {
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }
            requestData.setShipping(shipping);
        }

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(promotion.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        Double vatPercentage = 0.00;
        if (applyVat) {
            vatPercentage = 10.00;
            requestData.setEstimateVat(true); // setting estimateVat as true
        }
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        for (final Offerings offering : offeringsList) {
            offeringsJPromotionMap.put(offering, null);
        }

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        // Assertion on insufficient quantity
        AssertCollector.assertThat("There should be insufficient quantity error code",
            priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.INSUFFICIENT_QUANTITY_FOR_BUNDLED_PROMOTION), assertionErrorList);
        AssertCollector.assertThat("There should be insufficient quantity error details",
            priceQuotesResponse.getErrors().get(0).getDetail(), equalTo("Line Item with promotion ID: "
                + promotion.getData().getId() + " does not have the minimum quantity required for the purchase."),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error when line item is missing for a bundle promotion
     *
     * @param offeringsList
     * @param promotion
     * @param quantity
     * @param applyShipping
     * @param applyAdditionalTax
     */
    @Test(dataProvider = "bundlePromotionDetails")
    public void testErrorForMissingItemForBundlePromotion(final List<Offerings> offeringsList,
        final JPromotion promotion, final int quantity, final boolean applyShipping, final boolean applyAdditionalTax,
        final boolean applyVat) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        if (applyAdditionalTax) {
            // Adding additional fees
            additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();
        }

        // Since same dataprovider is used in 3 methods, this method removing
        // one offerings to test missing line item
        offeringsList.remove(0);

        // Adding line items
        lineItems = new ArrayList<>();
        for (final Offerings offering : offeringsList) {
            lineItems.add(new JLineItem(offering.getIncluded().getPrices().get(0).getId(), quantity, additionalFees));
        }
        requestData.setLineItems(lineItems);

        if (applyShipping) {
            // Adding shipping details
            if (offeringsList.get(0).getIncluded().getPrices().get(0).getCurrency().equals(Currency.USD.toString())) {
                shipping = HelperForPriceQuote.getNamerShippingDetails(applyAdditionalTax);
            } else {
                shipping = HelperForPriceQuote.getEmeaShippingDetails(applyAdditionalTax);
            }
            requestData.setShipping(shipping);
        }

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(promotion.getData().getId(), null));
        requestData.setPromotionReferences(promotionReferencesList);
        Double vatPercentage = 0.00;
        if (applyVat) {
            vatPercentage = 10.00;
            requestData.setEstimateVat(true); // setting estimateVat as true
        }
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        for (final Offerings offering : offeringsList) {
            offeringsJPromotionMap.put(offering, null);
        }

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            vatPercentage, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        // Assertion on missing line item
        AssertCollector.assertThat("There should be missing item error code",
            priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.MISSING_ITEM_FOR_BUNDLED_PROMO), assertionErrorList);
        AssertCollector.assertThat("There should be missing item error details",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo("Promotion with ID: " + promotion.getData().getId()
                + " is not applicable since some items required for the Bundled Promo are missing."),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests promotion getting applied on a non bundled product along with bundled promotion
     */
    @Test
    public void testSuccessWithAnotherItemApartFromBundlePromotion() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        lineItems.add(new JLineItem(metaOffering1.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        lineItems.add(new JLineItem(bicOffering5.getIncluded().getPrices().get(0).getId(), 1, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(activeNonStorewideWithDiscountAmount.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering4,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(metaOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering5,
            new ArrayList<>(ImmutableList.of(activeNonStorewideWithDiscountAmount)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        AssertCollector.assertThat("There should not be error", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error thrown when 2 offerings belongs to different stores and different currencies along with
     * bundle promotion Error all priced ids should contain same currency is thrown
     *
     * @param offeringsPromotionMap
     */
    @Test(dataProvider = "dataForNotMatchingStores")
    public void testErrorForDifferentStoreDifferentCurrency(
        final LinkedHashMap<Offerings, JPromotion> offeringsPromotionMap) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        // Adding promotion references
        promotionReferencesList = new ArrayList<>();

        // Add offering and bundle promotion to the request
        for (final Offerings offering : offeringsPromotionMap.keySet()) {
            lineItems.add(new JLineItem(offering.getIncluded().getPrices().get(0).getId(), 1, null));
            promotionReferencesList
                .add(new JPromotionReference(offeringsPromotionMap.get(offering).getData().getId(), null));
        }
        requestData.setLineItems(lineItems);

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Incorrect error status", priceQuotesResponse.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_DIFFERENT_STORES), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error thrown when 2 offerings belongs to different stores but same currency Error price should
     * belong to the same store is thrown
     */
    @Test
    public void testErrorForDifferentStoreSameCurrency() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering2.getIncluded().getPrices().get(0).getId(), 1, null));
        lineItems.add(new JLineItem(priceIdOfCanadaStoreForBicOffering2, 1, null));
        // Adding promotion references
        promotionReferencesList = new ArrayList<>();

        requestData.setLineItems(lineItems);

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Incorrect error status", priceQuotesResponse.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_DIFFERENT_STORES), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error thrown when 2 prices belong to same store but different currency Error prices ids should
     * belong to same store is thrown
     */
    @Test
    public void testErrorForSameStoreDifferentCurrency() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 1, null));
        lineItems.add(new JLineItem(priceIdForBicOffering2, 1, null));

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();

        requestData.setLineItems(lineItems);

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Incorrect error status", priceQuotesResponse.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.ERROR_MESSAGE_FOR_DIFFERENT_CURRENCY), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify price quote API throws an error on exceeding max use count for bundled promo.
     */
    @Test
    public void testErrorForExceedInBundlePromotionMaxUses() {

        // Create LineItem for purchase order.
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering7, 5,
            activeBundledPromotionForMaxUses.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicOffering6, 5,
            activeBundledPromotionForMaxUses.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForMetaOffering2, 5,
            activeBundledPromotionForMaxUses.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // Submit a purchase order in order to use promotion one time.
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, getBuyerUser());

        final String purchaseOrderId = purchaseOrder.getId();
        LOGGER.info("Purchase Order ID :" + purchaseOrderId);

        // Checking Using Price Quote API
        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);
        // Adding line items for pricequote api request.
        lineItemsForPriceQuote = new ArrayList<>();
        lineItemsForPriceQuote.add(new JLineItem(basicOffering7.getIncluded().getPrices().get(0).getId(), 5, null));
        lineItemsForPriceQuote.add(new JLineItem(metaOffering2.getIncluded().getPrices().get(0).getId(), 5, null));
        lineItemsForPriceQuote.add(new JLineItem(bicOffering6.getIncluded().getPrices().get(0).getId(), 5, null));

        requestData.setLineItems(lineItemsForPriceQuote);
        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(activeBundledPromotionForMaxUses.getData().getId(), null));

        // promotion exceeded max uses
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        // capture error respons for exceeding max use count.
        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.EXCEEDS_MAX_USE_COUNT), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links price id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getId(),
            equalTo(activeBundledPromotionForMaxUses.getData().getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test to verify price quote api throws an error on exceeding maximum uses per user for bundled promo.
     */
    @Test
    public void testErrorForExceedingBundlePromotionMaxUsesPerUser() {
        // Create LineItem for purchase order.
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering7, 5,
            activeBundledPromotionForMaxUsesPerUser.getData().getId(), null);

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForMetaOffering2, 5,
            activeBundledPromotionForMaxUsesPerUser.getData().getId(), null);

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicOffering6, 5,
            activeBundledPromotionForMaxUsesPerUser.getData().getId(), null);

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // Submit a purchase order in order to get promotion times used as one.
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, getBuyerUser());

        final String purchaseOrderId = purchaseOrder.getId();
        LOGGER.info("Purchase Order ID :" + purchaseOrderId);

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);
        // Adding line items for pricequote api request.
        lineItemsForPriceQuote = new ArrayList<>();
        lineItemsForPriceQuote.add(new JLineItem(basicOffering7.getIncluded().getPrices().get(0).getId(), 5, null));
        lineItemsForPriceQuote.add(new JLineItem(bicOffering6.getIncluded().getPrices().get(0).getId(), 5, null));
        lineItemsForPriceQuote.add(new JLineItem(metaOffering2.getIncluded().getPrices().get(0).getId(), 5, null));

        requestData.setLineItems(lineItemsForPriceQuote);
        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList
            .add(new JPromotionReference(activeBundledPromotionForMaxUsesPerUser.getData().getId(), null));

        // promotion exceeded max uses per user count, and capture the error.
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);
        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // Assert errors
        AssertCollector.assertThat("Number of errors doesn't match", priceQuotesResponse.getErrors().size(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Code doesn't match", priceQuotesResponse.getErrors().get(0).getCode(),
            equalTo(PelicanErrorConstants.EXCEEDS_MAX_USE_COUNT_PER_USER), assertionErrorList);
        AssertCollector.assertThat("Error detail doesn't match", priceQuotesResponse.getErrors().get(0).getDetail(),
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Error links price type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links price id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPrice(), nullValue(), assertionErrorList);
        AssertCollector.assertThat("Error links type doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Error links id doesn't match",
            priceQuotesResponse.getErrors().get(0).getLinks().getPromotions().getLinkage().get(0).getId(),
            equalTo(activeBundledPromotionForMaxUsesPerUser.getData().getId()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests when Bundle Promo Code is provided, but cart doesn't contain any of the products Invalid
     * promotion error is thrown, promotion is not applied to any of the line items
     */
    @Test
    public void testInvalidBundlePromotion() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        // Adding line items
        lineItems = new ArrayList<>();

        // Add 2 line items
        lineItems.add(new JLineItem(basicOffering4.getIncluded().getPrices().get(0).getId(), 1, additionalFees));
        lineItems.add(new JLineItem(basicOffering5.getIncluded().getPrices().get(0).getId(), 1, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        // Add a bundle promotion which is applicable to the line items selected
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        // this map is created for assertions purpose
        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(basicOffering4, null);
        offeringsJPromotionMap.put(basicOffering5, null);

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        // Assert the invalid promotion error
        AssertCollector.assertThat("There should be invalid promotion error code ",
            priceQuotesResponse.getErrors().get(0).getCode(), equalTo(PelicanErrorConstants.NOT_APPLICABLE_PROMOTION),
            assertionErrorList);
        AssertCollector.assertThat("There should be invalid promotion error",
            priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo("Promotion with ID: " + activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId()
                + " is not applicable for any of the offerings."),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This testcase validates merging of splitted items
     */
    @Test(dataProvider = "cartUpdateFlagValue")
    public void testSuccessMergeOfSplitItemsAlongWithCartUpdatedFlag(final boolean isCartUpdated) {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, isCartUpdated);
        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 3, additionalFees));
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 5, additionalFees));
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 6, additionalFees));
        lineItems.add(new JLineItem(metaOffering1.getIncluded().getPrices().get(0).getId(), 3, null));
        lineItems.add(new JLineItem(metaOffering1.getIncluded().getPrices().get(0).getId(), 3, null));
        lineItems.add(new JLineItem(bicOffering5.getIncluded().getPrices().get(0).getId(), 1, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(activeNonStorewideWithDiscountAmount.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering4,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(metaOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering5,
            new ArrayList<>(ImmutableList.of(activeNonStorewideWithDiscountAmount)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        AssertCollector.assertThat("There should not be error", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Price Quote response when bundle and non bundle promotions are applied. The first promotion in
     * "PromotionReference" list wins. The next ones will also be applied, if they are non-storewide In this case
     * bundled applied first, then non-bundled (depending on the order)
     */
    @Test
    public void testSuccessWithBundleAndNonBundlePromotion() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 6, additionalFees));
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 4, additionalFees));
        lineItems.add(new JLineItem(metaOffering1.getIncluded().getPrices().get(0).getId(), 5, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId(), null));
        promotionReferencesList.add(new JPromotionReference(activeBicOfferingPromo.getData().getId(), null));
        promotionReferencesList
            .add(new JPromotionReference(null, activeNonStorewideWithVat.getData().getCustomPromoCode()));

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering, new ArrayList<>(
            ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo, activeBicOfferingPromo)));
        offeringsJPromotionMap.put(bicOffering4, new ArrayList<>(
            ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo, activeNonStorewideWithVat)));
        offeringsJPromotionMap.put(metaOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);
        AssertCollector.assertThat("There should not be error", priceQuotesResponse.getErrors(), is(nullValue()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Price Quote response when bundle and bundle promotions are applied. The first promotion in
     * "PromotionReference" list wins. The next ones will also be applied, if they are non-storewide
     */
    @Test
    public void testSuccessWithBundleAndBundlePromotion() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 6, additionalFees));
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 5, additionalFees));
        lineItems.add(new JLineItem(bicOffering5.getIncluded().getPrices().get(0).getId(), 5, additionalFees));
        lineItems.add(new JLineItem(metaOffering1.getIncluded().getPrices().get(0).getId(), 5, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId(), null));
        promotionReferencesList.add(new JPromotionReference(
            activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering4,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo,
                activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering5,
            new ArrayList<>(ImmutableList.of(activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(metaOffering1,
            new ArrayList<>(ImmutableList.of(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo)));

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Price Quote response when non-bundle and bundle promotions are applied. The first promotion in
     * "PromotionReference" list wins. The next ones will also be applied, if they are non-storewide. In this case
     * non-bundled applied first, then bundle (depending on the order)
     */
    @Test
    public void testSuccessWithNonBundleAndBundlePromotion() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem(bicOffering.getIncluded().getPrices().get(0).getId(), 6, additionalFees));
        lineItems.add(new JLineItem(bicOffering4.getIncluded().getPrices().get(0).getId(), 5, additionalFees));
        lineItems.add(new JLineItem(bicOffering5.getIncluded().getPrices().get(0).getId(), 6, additionalFees));
        lineItems.add(new JLineItem(metaOffering1.getIncluded().getPrices().get(0).getId(), 5, null));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        promotionReferencesList.add(new JPromotionReference(activeBicOfferingPromo.getData().getId(), null));
        promotionReferencesList.add(
            new JPromotionReference(activeNonStoreWideCashAmountSubscriptionOffersBundlePromo.getData().getId(), null));
        promotionReferencesList.add(new JPromotionReference(
            activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo.getData().getId(), null));

        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);
        offeringsJPromotionMap = new HashMap<>();
        offeringsJPromotionMap.put(bicOffering, new ArrayList<>(ImmutableList.of(activeBicOfferingPromo)));
        offeringsJPromotionMap.put(bicOffering4,
            new ArrayList<>(ImmutableList.of(activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(bicOffering5,
            new ArrayList<>(ImmutableList.of(activeNonStoreWidePercentageBICSubscriptionOffersBundlePromo)));
        offeringsJPromotionMap.put(metaOffering1, null);

        // Assert line item level and total calculation along with promotions
        HelperForPriceQuote.assertionForLineItemCalculation(offeringsJPromotionMap, priceQuotesResponse, requestData,
            0.00, PurchaseType.OFFERING.getName(), null, getEnvironmentVariables(), false, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Price Quote response when price id is invalid Price Quote throws error says price id is invalid
     */
    @Test
    public void testErrorWhenPriceIdIsInvalid() {

        priceQuotes = new PriceQuotes();
        requestData = HelperForPriceQuote.getPriceQuoteData(buyerId, true);

        additionalFees = HelperForPriceQuote.getAdditionalFeesDetails();

        // Adding line items
        lineItems = new ArrayList<>();
        lineItems.add(new JLineItem("7654635", 6, additionalFees));

        requestData.setLineItems(lineItems);

        // Adding promotion references
        promotionReferencesList = new ArrayList<>();
        requestData.setPromotionReferences(promotionReferencesList);
        priceQuotes.setData(requestData);

        priceQuotesResponse = resource.priceQuote().getPriceQuotes(priceQuotes);

        AssertCollector.assertThat("Incorrect error status", priceQuotesResponse.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", priceQuotesResponse.getErrors().get(0).getDetail(),
            equalTo("No active store found for the price Ids informed."), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This data provider provides 2 offerings belongs to different store
     *
     * @return offerings and bundle promotion
     */
    @DataProvider(name = "dataForNotMatchingStores")
    private Object[][] getDataForNotMatchingStores() {
        return new Object[][] {
                { new LinkedHashMap<>(ImmutableMap.of(bicOffering, activeBicOfferingPromo, bicOffering2,
                    activeNonStoreWideDiscountAmountPromo1)) },
                { new LinkedHashMap<>(ImmutableMap.of(bicOffering2, activeNonStoreWideDiscountAmountPromo1, bicOffering,
                    activeBicOfferingPromo)) },
                { new LinkedHashMap<>(ImmutableMap.of(bicOffering2, activeStoreWidePercentageForTwoStores, bicOffering,
                    activeStoreWidePercentageForTwoStores)) },
                { new LinkedHashMap<>(ImmutableMap.of(bicOffering, activeStoreWidePercentageForTwoStores, bicOffering2,
                    activeStoreWidePercentageForTwoStores)) }, };
    }

    /**
     * This is a data provider for card updated value
     *
     * @return cart updated flag value
     */
    @DataProvider(name = "cartUpdateFlagValue")
    private Object[][] getCartUpdatedValue() {
        return new Object[][] { { true }, { false } };
    }
}
