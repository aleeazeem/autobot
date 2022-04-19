package com.autodesk.bsm.pelican.api.offerings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.OfferingsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.BasicOfferingPrice;
import com.autodesk.bsm.pelican.api.pojos.json.BasicOfferingPriceData;
import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * Get Offerings API Test
 *
 * @author Shweta Hegde
 */
public class GetOfferingsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private String bicOfferingId;
    private String basicOfferingId;
    private String productLineExternalKeyOfBicOffering;
    private String productLineExternalKeyOfBasicOffering;
    private HashMap<String, String> params;
    private Offerings offerings;
    private String usPriceIdOfPresentForBic;
    private String usPriceIdOfFutureForBic;
    private String ukPriceIdOfPresentForBic;
    private String usPriceIdOfPresentForBasic;
    private String ukPriceIdOfFutureForBasic;

    private Offerings bicOffering;
    private Offerings basicOffering;

    private Offerings metaOffering2;
    private Offerings metaOffering3;
    private Offerings metaOffering4;
    private Offerings basicOffering1;
    private Offerings basicOffering2;
    private Offerings basicOffering3;
    private Offerings basicOffering7;
    private Offerings bicOffering3;
    private Offerings bicOffering4;
    private Offerings bicOffering5;
    private Offerings bicOffering6;

    private String bicOffering5PriceId1;
    private String bicOffering5PriceId2;
    private String metaOffering5PriceId1;
    private String metaOffering5PriceId2;
    private String featureExternalKey;
    private String featureExternalKey1;
    private String productLine;
    private String productLineForOfferingWithNoPrice;
    private String priceIdforBICWithIC;
    private String priceIdforBICWithVG;
    private String priceIdforBICWithoutPackagingType;

    private JPromotion activeStoreWidePercentageBasicOfferingsBundlePromo;
    private JPromotion activeStoreWideCashAmountSubscriptionOffersBundlePromo;
    private JPromotion activeStoreWideCashAmountBothOfferingsBundlePromo;
    private JPromotion activeStoreWidePercentageBothOfferingsBundlePromo;

    private Item item;

    private AdminToolPage adminToolPage;
    private Descriptor descriptor;
    private AddDescriptorPage addDescriptorsPage;
    private String apiName;

    /**
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        final DescriptorDefinitionDetailPage detailPage =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
        descriptor = new Descriptor();

        adminToolPage.login();

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        // Add EMEA store without VAT
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        final JStore storeFranceWithoutVat =
            storeApiUtils.addStore(Status.ACTIVE, Country.FR, Currency.EUR, null, false);
        final String pricelistExternalKeyOfStoreFrance =
            storeFranceWithoutVat.getIncluded().getPriceLists().get(0).getExternalKey();

        // Add active bic subscription without IC
        bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // get id of the offering
        bicOfferingId = bicOffering.getOfferings().get(0).getId();
        usPriceIdOfPresentForBic = bicOffering.getIncluded().getPrices().get(0).getId();

        // add one more offer/billing plan
        SubscriptionOffer addedSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 1, Status.ACTIVE),
            bicOfferingId);

        // add price to the offer
        SubscriptionOfferPrice usPriceInFutureForBic = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 2, 2),
            bicOfferingId, addedSubscriptionOffer.getData().getId());
        usPriceIdOfFutureForBic = usPriceInFutureForBic.getData().getId();

        // add price to the offer
        SubscriptionOfferPrice ukPriceInPresentForBic =
            subscriptionPlanApiUtils
                .addPricesToSubscriptionOffer(resource,
                    subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000,
                        pricelistExternalKeyOfStoreFrance, 0, 1),
                    bicOfferingId, addedSubscriptionOffer.getData().getId());
        ukPriceIdOfPresentForBic = ukPriceInPresentForBic.getData().getId();

        // Adding Active BIC subscription with default PackagintType as None
        priceIdforBICWithoutPackagingType = bicOffering.getIncluded().getPrices().get(0).getId();

        // Add active bic subscription with IC
        final Offerings bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // get id of the offering
        final String bicOfferingIdForIc = bicOffering2.getOfferings().get(0).getId();

        // add one more offer/billing plan
        addedSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 1, Status.ACTIVE),
            bicOfferingIdForIc);

        // add price to the offer
        usPriceInFutureForBic = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 2, 2),
            bicOfferingIdForIc, addedSubscriptionOffer.getData().getId());
        usPriceIdOfFutureForBic = usPriceInFutureForBic.getData().getId();

        // add price to the offer
        ukPriceInPresentForBic =
            subscriptionPlanApiUtils.addPricesToSubscriptionOffer(
                resource, subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000,
                    pricelistExternalKeyOfStoreFrance, 0, 1),
                bicOfferingIdForIc, addedSubscriptionOffer.getData().getId());
        ukPriceIdOfPresentForBic = ukPriceInPresentForBic.getData().getId();

        productLineExternalKeyOfBicOffering = bicOffering2.getOfferings().get(0).getProductLine();

        // Adding PackagingType as Industry collections for Active BIC subscription
        priceIdforBICWithIC = bicOffering2.getIncluded().getPrices().get(0).getId();

        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = findSubscriptionPlanPage
            .findSubscriptionPlanByExternalKey(bicOffering2.getOfferings().get(0).getExternalKey());
        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editPackagingType(PackagingType.INDUSTRY_COLLECTION);
        editSubscriptionPlanPage.clickOnSave(false);

        // Add active bic subscription with VG
        Offerings bicOffering7 = subscriptionPlanApiUtils.addPlanWithProductLine(getProductLineExternalKeyMaya(),
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), PackagingType.VG);

        final String bicOfferingIdForVg = bicOffering7.getOffering().getId();
        // add one more offer/billing plan
        addedSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 1, Status.ACTIVE),
            bicOfferingIdForVg);

        // add price to the offer
        usPriceInFutureForBic = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 2, 2),
            bicOfferingIdForVg, addedSubscriptionOffer.getData().getId());
        usPriceIdOfFutureForBic = usPriceInFutureForBic.getData().getId();

        productLineExternalKeyOfBicOffering = getProductLineExternalKeyMaya();

        bicOffering7 = resource.offerings().getOfferingById(bicOfferingIdForVg, "prices");

        // Adding PackagingType as Vertical Grouping for Active BIC subscription
        priceIdforBICWithVG = bicOffering7.getIncluded().getPrices().get(0).getId();

        // create Basic Offerings
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        basicOfferingId = basicOffering.getOfferings().get(0).getId();
        usPriceIdOfPresentForBasic = basicOffering.getIncluded().getPrices().get(0).getId();
        productLineExternalKeyOfBasicOffering = basicOffering.getOfferings().get(0).getProductLine();
        // add price to the offering
        final BasicOfferingPrice ukPriceInFutureForBasic = basicOfferingApiUtils.addPricesToBasicOffering(resource,
            addPriceToBasicOffering(1000, pricelistExternalKeyOfStoreFrance, 2, 2), basicOfferingId);
        ukPriceIdOfFutureForBasic = ukPriceInFutureForBasic.getData().getId();

        // ***Bundle Promo Offering Test Data***
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering3 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUk(),
            OfferingType.PHYSICAL_MEDIA, MediaType.ELD, Status.ACTIVE, 300, UsageType.COM, null, null);
        basicOffering7 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUk(),
            OfferingType.PHYSICAL_MEDIA, MediaType.ELD, Status.ACTIVE, 300, UsageType.COM, null, null);

        // Add subscription Offers
        bicOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        bicOffering4 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        metaOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        metaOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        metaOffering4 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUk(),
            OfferingType.META_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Create Bundle Promo Offerings with Basic Offerings
        final List<BundlePromoOfferings> bundleOfBasicOfferingsList = new ArrayList<>();
        bundleOfBasicOfferingsList.add(promotionUtils.createBundlePromotionOffering(basicOffering1, 5, true));
        bundleOfBasicOfferingsList.add(promotionUtils.createBundlePromotionOffering(basicOffering2, 5, true));

        // bundle storewide percentage promotion creation
        activeStoreWidePercentageBasicOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                bundleOfBasicOfferingsList, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "20", null,
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        final List<BundlePromoOfferings> bundleOfSubscriptionOffersList = new ArrayList<>();
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(bicOffering3, 1, true));
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(bicOffering4, 1, true));
        bundleOfSubscriptionOffersList.add(promotionUtils.createBundlePromotionOffering(metaOffering2, 1, true));

        // bundle storewide Cash Amount promotion creation
        activeStoreWideCashAmountSubscriptionOffersBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUk()),
                bundleOfSubscriptionOffersList, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, null, "200",
                DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        // Create Bundle Promo Offerings with Subscription Offers & Basic
        // Offerings
        List<BundlePromoOfferings> bundleOfSubscriptionOffersAndBasicOfferings = new ArrayList<>();
        bundleOfSubscriptionOffersAndBasicOfferings
            .add(promotionUtils.createBundlePromotionOffering(metaOffering4, 2, true));
        bundleOfSubscriptionOffersAndBasicOfferings
            .add(promotionUtils.createBundlePromotionOffering(basicOffering3, 2, true));

        // bundle storewide Cash Amount promotion creation
        activeStoreWidePercentageBothOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUk()),
                bundleOfSubscriptionOffersAndBasicOfferings, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE,
                "20", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        bundleOfSubscriptionOffersAndBasicOfferings = new ArrayList<>();
        bundleOfSubscriptionOffersAndBasicOfferings
            .add(promotionUtils.createBundlePromotionOffering(metaOffering3, 2, true));
        bundleOfSubscriptionOffersAndBasicOfferings
            .add(promotionUtils.createBundlePromotionOffering(basicOffering7, 2, true));

        // bundle storewide Cash Amount promotion creation
        activeStoreWideCashAmountBothOfferingsBundlePromo =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUk()),
                bundleOfSubscriptionOffersAndBasicOfferings, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE,
                null, "200", DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        productLine = "SQA_PRODLINE" + RandomStringUtils.randomAlphabetic(10);
        subscriptionPlanApiUtils.addProductLine(productLine, true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        bicOffering5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 30);
        bicOffering5PriceId1 = bicOffering5.getIncluded().getPrices().get(0).getId();
        SubscriptionOfferPrice subscriptionOfferPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(40, getPricelistExternalKeyUk(), 0, 30);
        bicOffering5PriceId2 = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionOfferPrice,
            bicOffering5.getOfferings().get(0).getId(), bicOffering5.getIncluded().getBillingPlans().get(0).getId())
            .getData().getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine, OfferingType.BIC_SUBSCRIPTION,
            Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, BillingFrequency.YEAR, 2, getPricelistExternalKeyUs(),
            35);

        final Offerings metaOffering5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM,
            BillingFrequency.SEMIMONTH, 3, getPricelistExternalKeyUs(), 45);
        metaOffering5PriceId1 = metaOffering5.getIncluded().getPrices().get(0).getId();
        subscriptionOfferPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(50, getPricelistExternalKeyUk(), 0, 30);
        metaOffering5PriceId2 = subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionOfferPrice,
            metaOffering5.getOfferings().get(0).getId(), metaOffering5.getIncluded().getBillingPlans().get(0).getId())
            .getData().getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        bicOffering6 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.DAY, 7,
            getPricelistExternalKeyUs(), 30);
        subscriptionOfferPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(40, getPricelistExternalKeyUs(), 0, 30);

        item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey1 = item.getExternalKey();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering6.getOfferings().get(0).getId(),
            item.getId(), null, true);

        // SubscriptionOffers storewide Cash Amount promotion creation
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(bicOffering6), promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, null, "10.00",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

        // add Productline for get offerings with no prices
        productLineForOfferingWithNoPrice = "SQA_PRODLINE" + RandomStringUtils.randomAlphabetic(10);
        subscriptionPlanApiUtils.addProductLine(productLineForOfferingWithNoPrice, true);

        Offerings bicOfferingWithOfferNoPrice = subscriptionPlanApiUtils.addPlanWithProductLine(
            productLineForOfferingWithNoPrice, OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.BASIC,
            UsageType.COM, resource, RandomStringUtils.randomAlphanumeric(10), PackagingType.VG);
        final String bicOfferingIdForOfferWithNoPrice = bicOfferingWithOfferNoPrice.getOffering().getId();
        subscriptionPlanApiUtils.addOffer(bicOfferingIdForOfferWithNoPrice);
        bicOfferingWithOfferNoPrice = resource.offerings().getOfferingById(bicOfferingIdForOfferWithNoPrice,
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);

        // Add Discriptor to Promotion for Include Descriptor Offering tests
        final String fieldName = "TestDescriptor_" + RandomStringUtils.randomAlphanumeric(5);
        apiName = "DescriptorHeader_" + RandomStringUtils.randomAlphanumeric(5);

        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptor = detailPage.getDescriptorEntityFromDetails();
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        params = new HashMap<>();
    }

    /**
     * Get offerings by valid product line for Bic Subscription Plan
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidProductLineForBicSubscriptionPlan() {
        // Query by product line
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBicOffering);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Product line name is not correct",
            offerings.getOfferings().get(0).getProductLineName(), equalTo(getProductLineExternalKeyMaya()),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertThat("Custom date is not correct",
            offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getCustomDate(), is("false"),
            assertionErrorList);
        AssertCollector.assertThat("Value of month under billing date is not correct",
            offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getMonth(), is("0"), assertionErrorList);
        AssertCollector.assertThat("Value of day under billing date is not correct",
            offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getDay(), is("0"), assertionErrorList);
        AssertCollector.assertThat("Entitlement period count under data is not correct",
            offerings.getOfferings().get(0).getEntitlementPeriod().getCount(), is(0), assertionErrorList);
        AssertCollector.assertThat("Entitlement period type under data is not correct",
            offerings.getOfferings().get(0).getEntitlementPeriod().getType(), is(BillingFrequency.LIFETIME.getName()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings without price to offer
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsForOfferWithOutPrice() {
        // Query by product line
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineForOfferingWithNoPrice);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Offering detail was found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(nullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Billing plans were  found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(0), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line for Bic Subscription Plan
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsWithIncludeEntitlements() {
        // Query by product line
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.INCLUDE.getName(), PelicanConstants.INCLUDE_ENTITLEMENTS);
        offerings = resource.offerings().getOfferings(params);

        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Product line is not same for all offerings", offering.getProductLineName(),
                equalTo(productLine), assertionErrorList);
            if (offering.getId().equals(bicOffering5PriceId1)) {
                AssertCollector.assertThat("Entitlement id is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getId(), equalTo(item.getId()), assertionErrorList);
                AssertCollector.assertThat("Entitlement name is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getName(), equalTo(item.getName()), assertionErrorList);
                AssertCollector.assertThat("Entitlement external key is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getExternalKey(), equalTo(item.getExternalKey()),
                    assertionErrorList);
                AssertCollector.assertThat("Entitlement type is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getEntityType(), equalTo(EntityType.ITEM),
                    assertionErrorList);
                AssertCollector.assertThat(
                    "Entitlement licensing model externalKey is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getLicensingModelExternalKey(),
                    equalTo(PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY), assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings with Include Descriptors by valid feature externalkey
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsWithIncludeDescriptorsWhenFeatureFlagFalse() {
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey1);
        params.put(OfferingsClient.Parameter.INCLUDE.getName(), PelicanConstants.INCLUDE_DESCRIPTORS);
        offerings = resource.offerings().getOfferings(params);

        // Validate Descriptors in Offerings
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("OfferingId didnt match", offering.getId(),
                equalTo(bicOffering6.getOfferings().get(0).getId()), assertionErrorList);
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in Offering Section",
                offering.getDescriptors().getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API),
                assertionErrorList);
            AssertCollector.assertTrue("EStore Descriptor is NOT returned in Offering Section",
                offering.getDescriptors().getEstore().getProperties().containsKey(PelicanConstants.DESCRIPTOR_API),
                assertionErrorList);
        }

        // Validate Descriptors in Offer
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in Offer Section", billingPlan.getDescriptors()
                .getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API), assertionErrorList);
            AssertCollector.assertTrue("EStore Descriptor is NOT returned in Offer Section",
                billingPlan.getDescriptors().getEstore().getProperties().containsKey(PelicanConstants.DESCRIPTOR_API),
                assertionErrorList);
        }

        // Validate Descriptors in Promotion
        for (final JPromotionData promotion : offerings.getIncluded().getPromotions()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in promotion Section",
                promotion.getDescriptors().getIpp().getProperties().containsKey(apiName), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings with out Include Descriptors by valid feature externalkey
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsWithoutIncludeDescriptorsWhenFeatureflagFalse() {

        // Query by product line
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        offerings = resource.offerings().getOfferings(params);

        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Product line is not same for all offerings", offering.getProductLineName(),
                equalTo(productLine), assertionErrorList);
            if (offering.getId().equals(bicOffering5PriceId1)) {
                AssertCollector.assertTrue("IPP Descriptor is NOT returned in Offering Section", offering
                    .getDescriptors().getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API),
                    assertionErrorList);
            }
        }

        // Validate Descriptors in Offer
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in Offer Section", billingPlan.getDescriptors()
                .getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API), assertionErrorList);
            AssertCollector.assertTrue("EStore Descriptor is NOT returned in Offer Section",
                billingPlan.getDescriptors().getEstore().getProperties().containsKey(PelicanConstants.DESCRIPTOR_API),
                assertionErrorList);
        }

        // Validate Descriptors in Promotion
        for (final JPromotionData promotion : offerings.getIncluded().getPromotions()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in promotion Section",
                promotion.getDescriptors().getIpp().getProperties().containsKey(apiName), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings with out Include Descriptors by valid feature externalkey
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsWithoutIncludeDescriptors() {
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey1);
        offerings = resource.offerings().getOfferings(params);

        // Validate Descriptors in Offerings
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("OfferingId didnt match", offering.getId(),
                equalTo(bicOffering6.getOfferings().get(0).getId()), assertionErrorList);
            AssertCollector.assertThat("Found Offering Descriptor is NOT NULL", offering.getDescriptors(),
                is(nullValue()), assertionErrorList);
        }

        // Validate Descriptors in Offer
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Found Offer Descriptor is NOT NULL", billingPlan.getDescriptors(),
                is(nullValue()), assertionErrorList);
        }

        // Validate Descriptors in Promotion
        for (final JPromotionData promotion : offerings.getIncluded().getPromotions()) {
            AssertCollector.assertThat("Found Promotion Descriptor is NOT NULL", promotion.getDescriptors(),
                is(nullValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings with out Include Descriptors by valid feature externalkey
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsWithtIncludeDescriptors() {
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey1);
        params.put(OfferingsClient.Parameter.INCLUDE.getName(), PelicanConstants.INCLUDE_DESCRIPTORS);
        offerings = resource.offerings().getOfferings(params);

        // Validate Descriptors in Offerings
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("OfferingId didnt match", offering.getId(),
                equalTo(bicOffering6.getOfferings().get(0).getId()), assertionErrorList);
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in Offering Section",
                offering.getDescriptors().getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API),
                assertionErrorList);
            AssertCollector.assertTrue("EStore Descriptor is NOT returned in Offering Section",
                offering.getDescriptors().getEstore().getProperties().containsKey(PelicanConstants.DESCRIPTOR_API),
                assertionErrorList);
        }

        // Validate Descriptors in Offer
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in Offer Section", billingPlan.getDescriptors()
                .getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API), assertionErrorList);
            AssertCollector.assertTrue("EStore Descriptor is NOT returned in Offer Section",
                billingPlan.getDescriptors().getEstore().getProperties().containsKey(PelicanConstants.DESCRIPTOR_API),
                assertionErrorList);
        }

        // Validate Descriptors in Promotion
        for (final JPromotionData promotion : offerings.getIncluded().getPromotions()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in promotion Section",
                promotion.getDescriptors().getIpp().getProperties().containsKey(apiName), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line for Bic Subscription Plan
     *
     */
    @Test
    public void testGetOfferingsWithIncludeEntitlementsAndDescriptors() {
        // Query by product line
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.INCLUDE.getName(),
            PelicanConstants.INCLUDE_ENTITLEMENTS + "," + PelicanConstants.INCLUDE_DESCRIPTORS);
        offerings = resource.offerings().getOfferings(params);

        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Product line is not same for all offerings", offering.getProductLineName(),
                equalTo(productLine), assertionErrorList);
            if (offering.getId().equals(bicOffering5PriceId1)) {
                AssertCollector.assertThat("Entitlement id is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getId(), equalTo(item.getId()), assertionErrorList);
                AssertCollector.assertThat("Entitlement name is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getName(), equalTo(item.getName()), assertionErrorList);
                AssertCollector.assertThat("Entitlement external key is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getExternalKey(), equalTo(item.getExternalKey()),
                    assertionErrorList);
                AssertCollector.assertThat("Entitlement type is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getEntityType(), equalTo(EntityType.ITEM),
                    assertionErrorList);
                AssertCollector.assertThat(
                    "Entitlement licensing model externalKey is not found for offering: " + offering.getId(),
                    offering.getOneTimeEntitlements().get(0).getLicensingModelExternalKey(),
                    equalTo(PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY), assertionErrorList);
                AssertCollector.assertTrue("IPP Descriptor is NOT returned in Offering Section", offering
                    .getDescriptors().getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API),
                    assertionErrorList);
            }
        }

        // Validate Descriptors in Offer
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in Offer Section", billingPlan.getDescriptors()
                .getIpp().getProperties().containsKey(PelicanConstants.LOCAL_DESCRIPTOR_API), assertionErrorList);
            AssertCollector.assertTrue("EStore Descriptor is NOT returned in Offer Section",
                billingPlan.getDescriptors().getEstore().getProperties().containsKey(PelicanConstants.DESCRIPTOR_API),
                assertionErrorList);
        }

        // Validate Descriptors in Promotion
        for (final JPromotionData promotion : offerings.getIncluded().getPromotions()) {
            AssertCollector.assertTrue("Ipp Descriptor is NOT returned in promotion Section",
                promotion.getDescriptors().getIpp().getProperties().containsKey(apiName), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line for Bic Subscription Plan
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidProductLineForBasicOffering() {
        // Query by product line
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBasicOffering);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBasicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
            isOneOf(UsageType.COM, UsageType.TRL, UsageType.EDU, UsageType.GOV, UsageType.NCM), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertThat("Product line name is not correct",
            offerings.getOfferings().get(0).getProductLineName(),
            equalTo(basicOffering.getOfferings().get(0).getProductLineName()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by invalid (non-existing) product line
     *
     * @result Empty response
     */
    @Test
    public void testGetOfferingsByInvalidProductLine() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), "notexist");
        final Offerings offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings is not empty", offerings.getOfferings().size(), is(0),
            assertionErrorList);
        AssertCollector.assertThat("Found prices in 'included'", offerings.getIncluded().getPrices().size(), is(0),
            assertionErrorList);
        AssertCollector.assertThat("Found billing plans in 'included'",
            offerings.getIncluded().getBillingPlans().size(), is(0), assertionErrorList);
        AssertCollector.assertThat("Found offering detail in 'included'", offerings.getIncluded().getOfferingDetail(),
            is(nullValue()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by invalid price id
     *
     * @result empty response
     */
    @Test
    public void testGetOfferingsByInvalidPriceId() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), "1");
        final Offerings offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Offerings is not empty", offerings.getOfferings().size(), is(0),
            assertionErrorList);
        AssertCollector.assertThat("Found prices in 'included'", offerings.getIncluded().getPrices().size(), is(0),
            assertionErrorList);
        AssertCollector.assertThat("Found billing plans in 'included'",
            offerings.getIncluded().getBillingPlans().size(), is(0), assertionErrorList);
        AssertCollector.assertThat("Found offering detail in 'included'", offerings.getIncluded().getOfferingDetail(),
            is(nullValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", offerings.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", offerings.getErrors().get(0).getCode(),
            equalTo("invalid-price"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", offerings.getErrors().get(0).getDetail(),
            equalTo("Invalid price id."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id
     *
     * @result empty response
     */
    @Test(dataProvider = "getPriceIdForOfferingForPackagingType")
    public void testGetOfferingsByValidPriceIdWithPackagingType(final String priceId,
        final PackagingType packagingType) {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceId);
        final Offerings offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Offerings is empty", offerings.getOfferings().size(), is(1), assertionErrorList);

        for (int i = 0; i < offerings.getOfferings().size(); i++) {
            if (packagingType.getDisplayName().equals(PackagingType.IC.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, expected IC",
                    offerings.getOfferings().get(i).getPackagingType(), equalTo(PackagingType.IC), assertionErrorList);
            } else if (packagingType.getDisplayName().equals(PackagingType.VG.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, expected VG",
                    offerings.getOfferings().get(i).getPackagingType(), equalTo(PackagingType.VG), assertionErrorList);
            } else {
                AssertCollector.assertThat("Invalid PackagingType", offerings.getOfferings().get(i).getPackagingType(),
                    nullValue(), assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and country for Bic Subscription Plan
     *
     * @result Response with only price from requested country
     */
    @Test
    public void testGetOfferingsByProductLineAndCountryForBicSubscriptionPlan() {

        // Query by product code and country
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBicOffering);
        params.put(OfferingsClient.Parameter.COUNTRY.getName(), Country.US.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Product line name is not correct",
            offerings.getOfferings().get(0).getProductLineName(), equalTo(getProductLineExternalKeyMaya()),
            assertionErrorList);
        helperToValidateWithStoreAndPrices(Currency.USD.toString(), productLineExternalKeyOfBicOffering);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and country for Basic Offering
     *
     * @result Response with only price from requested country
     */
    @Test
    public void testGetOfferingsByProductLineAndCountryForBasicOffering() {

        // Query by product code and country
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBasicOffering);
        params.put(OfferingsClient.Parameter.COUNTRY.getName(), Country.US.toString());
        offerings = resource.offerings().getOfferings(params);

        // Assertions
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBasicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store id for Bic Subscription Plan
     *
     * @result Response with only price from requested store id
     */
    @Test
    public void testGetOfferingsByProductLineAndStoreIdForBicSubscriptionPlan() {

        // Query by product code and store id
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBicOffering);
        params.put(OfferingsClient.Parameter.STORE_ID.getName(), getStoreIdUs());
        offerings = resource.offerings().getOfferings(params);

        helperToValidateWithStoreAndPrices(Currency.USD.toString(), productLineExternalKeyOfBicOffering);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store id for Basic Offering
     *
     * @result Response with only price from requested store id
     */
    @Test
    public void testGetOfferingsByProductLineAndStoreIdForBasicOffering() {

        // Query by product code and store id
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBasicOffering);
        params.put(OfferingsClient.Parameter.STORE_ID.getName(), getStoreIdUs());
        offerings = resource.offerings().getOfferings(params);

        // Assertions
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBasicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store external key for Subscription Plan
     *
     * @result Response with only price from requested store with active offers
     */
    @Test
    public void testGetOfferingsByProductLineAndStoreExternalKeyForBicSubscriptionPlan() {

        // Query by product line and store external key
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBicOffering);
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUs());
        offerings = resource.offerings().getOfferings(params);

        // Assertions
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Billing plans was not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store external key for Basic Offering
     *
     * @result Response with only price from requested store with active offers
     */
    @Test
    public void testGetOfferingsByProductLineAndStoreExternalKeyForBasicOffering() {

        // Query by product line and store external key
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBasicOffering);
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUs());
        offerings = resource.offerings().getOfferings(params);

        // Assertions
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBasicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store type which has offer in Active status for Subscription Plan
     *
     * @result Response with only price from requested store type
     */
    @Test
    public void testGetOfferingsByProductLineAndStoreTypeForBicSubscriptionPlan() {

        // Query by product code and country
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBicOffering);
        params.put(OfferingsClient.Parameter.STORE_TYPE.getName(), getStoreUs().getStoreType());
        offerings = resource.offerings().getOfferings(params);

        // Assertions
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Billing plans was not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by product line and store type which has offer in Active status for Basic Offering
     *
     * @result Response with only price from requested store type
     */
    @Test
    public void testGetOfferingsByProductLineAndStoreTypeForBasicOffering() {

        // Query by product code and country
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBasicOffering);
        params.put(OfferingsClient.Parameter.STORE_TYPE.getName(), getStoreUs().getStoreType());
        offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLineExternalKeyOfBasicOffering), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings without required product line
     *
     * @result Response include errors
     */
    @Test
    public void testErrorGetOfferingsByNoProductLineAndPriceList() {

        // submit without productline and pricelist
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings is not empty", offerings.getOfferings().size(), is(0),
            assertionErrorList);
        AssertCollector.assertThat("Included is not empty", offerings.getIncluded(), is(nullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", offerings.getErrors().get(0).getDetail(),
            equalTo("Either Product Line External Key, Price Id(s) or Feature is required."), assertionErrorList);
        AssertCollector.assertThat("Incorrect status in errors", offerings.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by unparsable price id that is alpha value
     *
     * @result empty response
     */

    @Test
    public void testGetOfferingsByUnparsablePriceId() {

        final String priceId = "abc";
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceId);
        offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Unable to find expected error", offerings.getErrors(), is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect title", offerings.getErrors().get(0).getDetail(),
            equalTo("Contains invalid number : " + priceId), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", offerings.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by past price id for Bic Subscription Plan
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPastPriceIdsForBicSubscriptionPlan() {

        DbUtils.updateTableInDb("SUBSCRIPTION_PRICE", "START_DATE", "'2000-01-01 00:00:00.000000'", "ID",
            ukPriceIdOfPresentForBic, getEnvironmentVariables());
        DbUtils.updateTableInDb("SUBSCRIPTION_PRICE", "END_DATE", "'2015-12-31 23:59:59.999000'", "ID",
            ukPriceIdOfPresentForBic, getEnvironmentVariables());
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), ukPriceIdOfPresentForBic);
        offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Error should be reported", offerings.getErrors(), is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", offerings.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", offerings.getErrors().get(0).getDetail(),
            equalTo("This price is no longer available for purchase."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by future price id for Bic Subscription Plan
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFuturePriceIdsForBicSubscriptionPlan() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), usPriceIdOfFutureForBic);
        offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Error should be reported", offerings.getErrors(), is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", offerings.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", offerings.getErrors().get(0).getDetail(),
            equalTo("This price is not active yet for purchase."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by future price id for Basic Offering
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFuturePriceIdsForBasicOffering() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), ukPriceIdOfFutureForBasic);
        offerings = resource.offerings().getOfferings(params);

        AssertCollector.assertThat("Error should be reported", offerings.getErrors(), is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error status", offerings.getErrors().get(0).getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error detail", offerings.getErrors().get(0).getDetail(),
            equalTo("This price is not active yet for purchase."), assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type", offerings.getOfferings().get(0).getUsageType(),
            is(UsageType.COM), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by 1 valid price id of Subscription Plan
     *
     * @result 1 offerings returned
     */
    @Test
    public void testGetOfferingsByValidPriceIdForSubscriptionPlan() {
        // Get the first offering query on price id
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), usPriceIdOfPresentForBic);
        offerings = resource.offerings().getOfferings(params);

        // Validate - data
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect type", offerings.getOfferings().get(0).getOfferingType(),
            equalTo(bicOffering.getOfferings().get(0).getOfferingType()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering id", offerings.getOfferings().get(0).getId(),
            equalTo(bicOfferingId), assertionErrorList);
        // Validate Included Price
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id in Included Price",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(usPriceIdOfPresentForBic), assertionErrorList);
        AssertCollector.assertThat("Incorrect currency in Included Price",
            offerings.getIncluded().getPrices().get(0).getCurrency(),
            equalTo(bicOffering.getIncluded().getPrices().get(0).getCurrency()), assertionErrorList);
        AssertCollector.assertThat("Incorrect amount in Included Price",
            offerings.getIncluded().getPrices().get(0).getAmount(),
            equalTo(String.valueOf(bicOffering.getIncluded().getPrices().get(0).getAmount())), assertionErrorList);
        // Validate Included Billing Plan
        AssertCollector.assertThat("Billing plans was not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing plan id in Included Billing Plan ",
            offerings.getIncluded().getBillingPlans().get(0).getId(),
            equalTo(offerings.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id in billing plan linkage",
            offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices().getLinkage().get(0).getId(),
            equalTo(usPriceIdOfPresentForBic), assertionErrorList);
        // Validate there's no errors
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by all parameters
     */
    @Test
    public void testGetOfferingsWithAllParametersForBicSubscriptionPlan() {

        params.put(OfferingsClient.Parameter.COUNTRY.getName(), Country.US.toString());
        params.put(OfferingsClient.Parameter.STORE_TYPE.getName(), getStoreUs().getStoreType());
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBicOffering);
        params.put(OfferingsClient.Parameter.STORE_ID.getName(), getStoreUs().getId());
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUs());
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), usPriceIdOfPresentForBic);
        offerings = resource.offerings().getOfferings(params);

        // Validate - data
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect type", offerings.getOfferings().get(0).getOfferingType(),
            equalTo(bicOffering.getOfferings().get(0).getOfferingType()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering id", offerings.getOfferings().get(0).getId(),
            equalTo(bicOfferingId), assertionErrorList);
        // Validate Included Price
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id in Included Price",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(usPriceIdOfPresentForBic), assertionErrorList);
        AssertCollector.assertThat("Incorrect currency in Included Price",
            offerings.getIncluded().getPrices().get(0).getCurrency(),
            equalTo(bicOffering.getIncluded().getPrices().get(0).getCurrency()), assertionErrorList);
        AssertCollector.assertThat("Incorrect amount in Included Price",
            offerings.getIncluded().getPrices().get(0).getAmount(),
            equalTo(String.valueOf(bicOffering.getIncluded().getPrices().get(0).getAmount())), assertionErrorList);
        // Validate Included Billing Plan
        AssertCollector.assertThat("Billing plans was not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect billing plan id in Included Billing Plan ",
            offerings.getIncluded().getBillingPlans().get(0).getId(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id in billing plan linkage",
            offerings.getIncluded().getBillingPlans().get(0).getLinks().getPrices().getLinkage().get(0).getId(),
            equalTo(usPriceIdOfPresentForBic), assertionErrorList);
        // Validate there's no errors
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by all parameters
     */
    @Test
    public void testGetOfferingsWithAllParametersForBasicOfferings() {
        params.put(OfferingsClient.Parameter.COUNTRY.getName(), Country.US.toString());
        params.put(OfferingsClient.Parameter.STORE_TYPE.getName(), getStoreUs().getStoreType());
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLineExternalKeyOfBasicOffering);
        params.put(OfferingsClient.Parameter.STORE_ID.getName(), getStoreIdUs());
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUs());
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), usPriceIdOfPresentForBasic);
        offerings = resource.offerings().getOfferings(params);

        // Validate - data
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect type", offerings.getOfferings().get(0).getOfferingType(),
            equalTo(basicOffering.getOfferings().get(0).getOfferingType()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering id", offerings.getOfferings().get(0).getId(),
            equalTo(basicOfferingId), assertionErrorList);
        // Validate Included Price
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id in Included Price",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(usPriceIdOfPresentForBasic),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect currency in Included Price",
            offerings.getIncluded().getPrices().get(0).getCurrency(),
            equalTo(basicOffering.getIncluded().getPrices().get(0).getCurrency()), assertionErrorList);
        AssertCollector.assertThat("Incorrect amount in Included Price",
            offerings.getIncluded().getPrices().get(0).getAmount(),
            equalTo(String.valueOf(basicOffering.getIncluded().getPrices().get(0).getAmount())), assertionErrorList);
        // Validate there's no errors
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method adds price to basic offering
     */
    private BasicOfferingPrice addPriceToBasicOffering(final int amount, final String priceListExternalKey,
        final int startDateNeeded, final int endDateNeeded) {

        // Add basic offering price
        final BasicOfferingPrice basicOfferingPrice = new BasicOfferingPrice();
        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, startDateNeeded);
        final Date startDate = calendar.getTime();
        calendar.add(Calendar.YEAR, endDateNeeded);
        final Date endDate = calendar.getTime();
        final String priceStartDate = dateFormat.format(startDate);
        final String priceEndDate = dateFormat.format(endDate);
        final BasicOfferingPriceData basicOfferingPriceData = new BasicOfferingPriceData();
        basicOfferingPriceData.setType("price");
        basicOfferingPriceData.setAmount(amount);
        basicOfferingPriceData.setStartDate(priceStartDate);
        basicOfferingPriceData.setEndDate(priceEndDate);
        basicOfferingPriceData.setPriceList(priceListExternalKey);
        basicOfferingPrice.setData(basicOfferingPriceData);

        return basicOfferingPrice;
    }

    /**
     * This is a helper method which does validation for common functionality
     */
    private void helperToValidateWithStoreAndPrices(final String currency, final String productLine) {
        // Assertions
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Billing plans was not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line", offerings.getOfferings().get(0).getProductLine(),
            equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);

        // Validate included price are only from requested store
        for (final Price price : offerings.getIncluded().getPrices()) {
            AssertCollector.assertThat("Unexpected currency type in price", price.getCurrency(), equalTo(currency),
                assertionErrorList);
        }
    }

    /**
     * Test GetOffering API for bundled Promo with multiple combination 1. Basic Offering 2. Subscription Offer 3. Both
     * 1 & 2 Tested above combination with Percentage and Cash Amount, so total of 6 test scenarios
     */
    @Test(dataProvider = "getOfferingBundlePromotionDetails")
    public void testGetOfferingsForBundledPromos(final List<Offerings> offeringsList, final JPromotion promotion,
        final String testType) {

        final List<String> pricesList = new ArrayList<>();
        for (final Offerings offering : offeringsList) {
            pricesList.add(offering.getIncluded().getPrices().get(0).getId());
        }
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), StringUtils.join(pricesList, ","));
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Error reported" + testType, offerings.getErrors(), is(nullValue()),
            assertionErrorList);

        // Validate - data
        AssertCollector.assertThat("Incorrect offerings size:" + testType, offerings.getOfferings().size(),
            equalTo(offeringsList.size()), assertionErrorList);

        // Validate isBundled value
        AssertCollector.assertTrue(
            "Offering contains 'isBundle' value false for " + offerings.getOfferings().get(0).getId(),
            offerings.getIncluded().getPromotions().get(0).getIsBundled(), assertionErrorList);

        for (final Offerings expectedOffering : offeringsList) {
            for (final Price expectedPrice : expectedOffering.getIncluded().getPrices()) {
                Double expectedDiscount = 0D;
                Double actualDiscount = 0D;
                Double expectedAmount = 0D;
                Double actualAmountAfterDiscount = 0D;
                for (final Price actualPrice : offerings.getIncluded().getPrices()) {
                    actualDiscount = Double.valueOf(actualPrice.getDiscount());
                    actualAmountAfterDiscount = Double.valueOf(actualPrice.getAmountAfterDiscount());
                    if (actualPrice.getId().equals(expectedPrice.getId()) && null != promotion.getData()
                        && null != promotion.getData().getPromotionType()) {
                        expectedAmount = Double.valueOf(expectedPrice.getAmount());
                        switch (promotion.getData().getPromotionType()) {
                            case DISCOUNT_AMOUNT:
                                expectedDiscount = promotion.getData().getDiscountAmount();
                                break;
                            case DISCOUNT_PERCENTAGE:
                                expectedDiscount = expectedAmount * ((promotion.getData().getDiscountPercent()) / 100);
                                break;
                        }
                        AssertCollector.assertThat("Incorrect currency in Included Price", actualPrice.getCurrency(),
                            equalTo(expectedPrice.getCurrency()), assertionErrorList);
                        AssertCollector.assertThat("Incorrect amount in Included Price", actualPrice.getAmount(),
                            equalTo(String.valueOf(expectedPrice.getAmount())), assertionErrorList);
                        break;
                    }
                }
                // Validate Discount & AmountAfterDiscount
                AssertCollector.assertThat("Incorrect offerings Discount:", actualDiscount, equalTo(expectedDiscount),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect offerings AmountAfterDiscount:", actualAmountAfterDiscount,
                    equalTo(expectedAmount - expectedDiscount), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid feature external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidFeatureExternalKey() {
        // Query by feature external key
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(4), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering 1",
            offerings.getOfferings().get(0).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering2",
            offerings.getOfferings().get(1).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state1", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state2", offerings.getOfferings().get(1).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state1",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state2",
            offerings.getIncluded().getBillingPlans().get(1).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type1",
            offerings.getOfferings().get(0).getOfferingType().toString(), equalTo("BIC_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type2",
            offerings.getOfferings().get(1).getOfferingType().toString(), equalTo("META_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line and feature external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidProductLineAndFeatureExternalKey() {
        // Query by product line and feature external key
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(4), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering 1",
            offerings.getOfferings().get(0).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering2",
            offerings.getOfferings().get(1).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state1", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state2", offerings.getOfferings().get(1).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state1",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state2",
            offerings.getIncluded().getBillingPlans().get(1).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type1",
            offerings.getOfferings().get(0).getOfferingType().toString(), equalTo("BIC_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type2",
            offerings.getOfferings().get(1).getOfferingType().toString(), equalTo("META_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid country, product line and feature external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidCountryProductLineAndFeatureExternalKey() {
        // Query by product line, feature and country code
        params.put(OfferingsClient.Parameter.COUNTRY.getName(), Country.GB.toString());
        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(bicOffering5PriceId2), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(1).getId(), equalTo(metaOffering5PriceId2), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering 1",
            offerings.getOfferings().get(0).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering2",
            offerings.getOfferings().get(1).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state1", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state2", offerings.getOfferings().get(1).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state1",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state2",
            offerings.getIncluded().getBillingPlans().get(1).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type1",
            offerings.getOfferings().get(0).getOfferingType().toString(), equalTo("BIC_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type2",
            offerings.getOfferings().get(1).getOfferingType().toString(), equalTo("META_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid store id and feature external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidStoreIdAndFeatureExternalKey() {
        // Query by product line, feature and country code
        params.put(OfferingsClient.Parameter.STORE_ID.getName(), getStoreIdUs());
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(bicOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(1).getId(), equalTo(metaOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering 1",
            offerings.getOfferings().get(0).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering2",
            offerings.getOfferings().get(1).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state1", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state2", offerings.getOfferings().get(1).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state1",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state2",
            offerings.getIncluded().getBillingPlans().get(1).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type1",
            offerings.getOfferings().get(0).getOfferingType().toString(), equalTo("BIC_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type2",
            offerings.getOfferings().get(1).getOfferingType().toString(), equalTo("META_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid store external key and feature external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidStoreExternalKeyAndFeatureExternalKey() {
        // Query by product line, feature and country code
        params.put(OfferingsClient.Parameter.STORE_EXTKEY.getName(), getStoreExternalKeyUs());
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(bicOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(1).getId(), equalTo(metaOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(2), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering 1",
            offerings.getOfferings().get(0).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering2",
            offerings.getOfferings().get(1).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state1", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state2", offerings.getOfferings().get(1).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state1",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state2",
            offerings.getIncluded().getBillingPlans().get(1).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type1",
            offerings.getOfferings().get(0).getOfferingType().toString(), equalTo("BIC_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type2",
            offerings.getOfferings().get(1).getOfferingType().toString(), equalTo("META_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id and feature external key
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByValidPriceIdAndFeatureExternalKey() {
        // Query by product line, feature and country code
        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), metaOffering5PriceId2);
        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Offering detail was not found in 'included'",
            offerings.getIncluded().getOfferingDetail(), is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'", offerings.getIncluded().getPrices().size(),
            equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Prices was not found in 'included'",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(metaOffering5PriceId2), assertionErrorList);
        AssertCollector.assertThat("Billing plans were not found in 'included' ",
            offerings.getIncluded().getBillingPlans().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product line in offering 1",
            offerings.getOfferings().get(0).getProductLine(), equalTo(productLine), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering state1", offerings.getOfferings().get(0).getStatus(),
            equalToIgnoringCase(Status.ACTIVE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offer state1",
            offerings.getIncluded().getBillingPlans().get(0).getStatus(), equalToIgnoringCase(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offering type2",
            offerings.getOfferings().get(0).getOfferingType().toString(), equalTo("META_SUBSCRIPTION"),
            assertionErrorList);
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line external key and offering type
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByProductLineAndOfferingType() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.OFFERING_TYPE_FILTER.getName(), OfferingType.BIC_SUBSCRIPTION.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(3), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect product line returned in the get offerings response",
                offering.getProductLine(), equalTo(productLine), assertionErrorList);
            AssertCollector.assertThat("Incorrect offering type returned in the get offerings response",
                offering.getOfferingType().toString(), equalTo(OfferingType.BIC_SUBSCRIPTION.toString()),
                assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id and offering type
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPriceIdAndOfferingType() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceIdforBICWithoutPackagingType);
        params.put(OfferingsClient.Parameter.OFFERING_TYPE_FILTER.getName(), OfferingType.BIC_SUBSCRIPTION.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(priceIdforBICWithoutPackagingType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect offering type returned in the get offerings response",
                offering.getOfferingType().toString(), equalTo(OfferingType.BIC_SUBSCRIPTION.toString()),
                assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid feature external key and offering type
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFeatureExternalKeyAndOfferingType() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        final String featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsClient.Parameter.OFFERING_TYPE_FILTER.getName(), OfferingType.BIC_SUBSCRIPTION.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(bicOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(1).getId(), equalTo(bicOffering5PriceId2), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(2), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect offering type returned in the get offerings response",
                offering.getOfferingType().toString(), equalTo(OfferingType.BIC_SUBSCRIPTION.toString()),
                assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line external key and usage type
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByProductLineAndUsageType() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.USAGE_TYPE_FILTER.getName(), UsageType.COM.getUploadName());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(4), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect product line returned in the get offerings response",
                offering.getProductLine(), equalTo(productLine), assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type returned in the get offerings response",
                offering.getUsageType().getUploadName(), equalTo(UsageType.COM.getUploadName()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id and usage type
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPriceIdAndUsageType() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceIdforBICWithoutPackagingType);
        params.put(OfferingsClient.Parameter.USAGE_TYPE_FILTER.getName(), UsageType.COM.getUploadName());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(priceIdforBICWithoutPackagingType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect usage type returned in the get offerings response",
                offering.getUsageType().getUploadName(), equalTo(UsageType.COM.getUploadName()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid feature external key and usage type
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFeatureExternalKeyAndUsageType() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        final String featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsClient.Parameter.USAGE_TYPE_FILTER.getName(), UsageType.COM.getUploadName());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect usage type returned in the get offerings response",
                offering.getUsageType().getUploadName(), equalTo(UsageType.COM.getUploadName()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line external key and support level
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByProductLineAndSupportLevel() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.SUPPORT_LEVEL_FILTER.getName(), SupportLevel.BASIC.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(3), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect product line returned in the get offerings response",
                offering.getProductLine(), equalTo(productLine), assertionErrorList);
            AssertCollector.assertThat("Incorrect support level returned in the get offerings response",
                offering.getSupportLevel().toString(), equalTo(SupportLevel.BASIC.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id and support level
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPriceIdAndSupportLevel() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceIdforBICWithoutPackagingType);
        params.put(OfferingsClient.Parameter.SUPPORT_LEVEL_FILTER.getName(), SupportLevel.ADVANCED.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(priceIdforBICWithoutPackagingType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect support level returned in the get offerings response",
                offering.getSupportLevel().toString(), equalTo(SupportLevel.ADVANCED.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid feature external key and support level
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFeatureExternalKeyAndSupportLevel() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        final String featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsClient.Parameter.SUPPORT_LEVEL_FILTER.getName(), SupportLevel.BASIC.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect support level returned in the get offerings response",
                offering.getSupportLevel().toString(), equalTo(SupportLevel.BASIC.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id, usage type, offering type and support level
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPriceIdUsageTypeSupportLevelOfferingTypeFilterWithSpaces() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceIdforBICWithoutPackagingType);
        params.put(OfferingsClient.Parameter.OFFERING_TYPE_FILTER.getName(), OfferingType.BIC_SUBSCRIPTION.toString());
        params.put(OfferingsClient.Parameter.USAGE_TYPE_FILTER.getName(), UsageType.COM.getUploadName());
        params.put(OfferingsClient.Parameter.SUPPORT_LEVEL_FILTER.getName(), SupportLevel.ADVANCED.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(priceIdforBICWithoutPackagingType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect offering type returned in the get offerings response",
                offering.getOfferingType().toString(), equalTo(OfferingType.BIC_SUBSCRIPTION.toString()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect usage type returned in the get offerings response",
                offering.getUsageType().getUploadName(), equalTo(UsageType.COM.getUploadName()), assertionErrorList);
            AssertCollector.assertThat("Incorrect support level returned in the get offerings response",
                offering.getSupportLevel().toString(), equalTo(SupportLevel.ADVANCED.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line external key and Billing Cycle Count
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByProductLineAndBillingCycleCount() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.BILLING_CYCLE_COUNT_FILTER.getName(), PelicanConstants.UNLIMITED);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(4), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect product line returned in the get offerings response",
                offering.getProductLine(), equalTo(productLine), assertionErrorList);
        }
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing cycle count returned in the get offerings response",
                billingPlan.getBillingCycleCount(), is(nullValue()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id and Billing Cycle Count
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPriceIdAndBillingCycleCount() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceIdforBICWithoutPackagingType);
        params.put(OfferingsClient.Parameter.BILLING_CYCLE_COUNT_FILTER.getName(), PelicanConstants.UNLIMITED);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(priceIdforBICWithoutPackagingType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(1), assertionErrorList);
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing cycle count returned in the get offerings response",
                billingPlan.getBillingCycleCount(), is(nullValue()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid feature external key and Billing Cycle Count
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFeatureExternalKeyAndBillingCycleCount() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        final String featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsClient.Parameter.BILLING_CYCLE_COUNT_FILTER.getName(), PelicanConstants.UNLIMITED);
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(bicOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(1).getId(), equalTo(bicOffering5PriceId2), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(2), assertionErrorList);
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing cycle count returned in the get offerings response",
                billingPlan.getBillingCycleCount(), is(nullValue()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line external key, billing period count and billing period
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByProductLineBillingPeriodCountAndBillingPeriod() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_COUNT_FILTER.getName(), "1");
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_FILTER.getName(), BillingFrequency.MONTH.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect product line returned in the get offerings response",
                offering.getProductLine(), equalTo(productLine), assertionErrorList);
        }
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing period count returned in the get offerings response",
                billingPlan.getBillingPeriodCount(), equalTo("1"), assertionErrorList);
            AssertCollector.assertThat("Incorrect billing period returned in the get offerings response",
                billingPlan.getBillingPeriod(), equalTo(BillingFrequency.MONTH.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid price id, billing period count and billing period
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByPriceIdBillingPeriodCountAndBillingPeriod() {

        params.put(OfferingsClient.Parameter.PRICE_ID.getName(), priceIdforBICWithoutPackagingType);
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_COUNT_FILTER.getName(), "1");
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_FILTER.getName(), BillingFrequency.MONTH.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), greaterThanOrEqualTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(priceIdforBICWithoutPackagingType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(1), assertionErrorList);
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing period count returned in the get offerings response",
                billingPlan.getBillingPeriodCount(), equalTo("1"), assertionErrorList);
            AssertCollector.assertThat("Incorrect billing period returned in the get offerings response",
                billingPlan.getBillingPeriod(), equalTo(BillingFrequency.MONTH.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid feature external key, billing period count and billing period
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFeatureExternalKeyBillingPeriodCountAndBillingPeriod() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        final String featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_COUNT_FILTER.getName(), "1");
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_FILTER.getName(), BillingFrequency.MONTH.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(0).getId(), equalTo(bicOffering5PriceId1), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id returned in the get offerings response",
            offerings.getIncluded().getPrices().get(1).getId(), equalTo(bicOffering5PriceId2), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of prices returned in the get offerings response",
            offerings.getIncluded().getPrices().size(), equalTo(2), assertionErrorList);
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing period count returned in the get offerings response",
                billingPlan.getBillingPeriodCount(), equalTo("1"), assertionErrorList);
            AssertCollector.assertThat("Incorrect billing period returned in the get offerings response",
                billingPlan.getBillingPeriod(), equalTo(BillingFrequency.MONTH.toString()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @AfterClass
    public void tearDown() {
        deleteDescriptors(descriptor.getFieldName(), descriptor.getGroupName(), descriptor.getEntity().getEntity());
    }

    /**
     * Get offerings by valid feature external key and billing period
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByFeatureExternalKeyAndBillingPeriod() {

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        final String featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering5.getOfferings().get(0).getId(),
            featureId, null, true);

        params.put(OfferingsClient.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_FILTER.getName(), BillingFrequency.MONTH.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Error reported", offerings.getErrors().get(0).getDetail(),
            equalTo(PelicanErrorConstants.EXPECTED_BILLING_PERIOD_OR_BILLING_COUNT_MISSING_MESSAGE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get offerings by valid product line external key, billing cycle count, billing period count and billing period
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsByProductLineBillingCycleCountBillingPeriodCountAndBillingPeriod() {

        params.put(OfferingsClient.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsClient.Parameter.BILLING_CYCLE_COUNT_FILTER.getName(), PelicanConstants.UNLIMITED);
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_COUNT_FILTER.getName(), "1");
        params.put(OfferingsClient.Parameter.BILLING_PERIOD_FILTER.getName(), BillingFrequency.MONTH.toString());
        offerings = resource.offerings().getOfferings(params);
        AssertCollector.assertThat("Offerings Count mismatch in get offerings api response",
            offerings.getOfferings().size(), equalTo(1), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Incorrect product line returned in the get offerings response",
                offering.getProductLine(), equalTo(productLine), assertionErrorList);
        }
        for (final BillingPlan billingPlan : offerings.getIncluded().getBillingPlans()) {
            AssertCollector.assertThat("Incorrect billing period count returned in the get offerings response",
                billingPlan.getBillingPeriodCount(), equalTo("1"), assertionErrorList);
            AssertCollector.assertThat("Incorrect billing period returned in the get offerings response",
                billingPlan.getBillingPeriod(), equalTo(BillingFrequency.MONTH.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect billing cycle count returned in the get offerings response",
                billingPlan.getBillingCycleCount(), is(nullValue()), assertionErrorList);
        }
        AssertCollector.assertThat("Error reported", offerings.getErrors(), is(nullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to delete the descriptors.
     */
    private void deleteDescriptors(final String fieldName, final String groupName, final String entity) {
        // Navigate to Find Descriptors page.
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        addDescriptorsPage.navigateToFindDescriptors();
        // Delete the descriptors.
        addDescriptorsPage.deleteDescriptors(fieldName, groupName, entity);
    }

    @DataProvider(name = "getOfferingBundlePromotionDetails")
    public Iterator<Object[]> getOfferingBundlePromotionDetails() {
        final Collection<Object[]> dp = new ArrayList<>();
        dp.add(new Object[] { new ArrayList<>(ImmutableList.of(basicOffering1, basicOffering2)),
                activeStoreWidePercentageBasicOfferingsBundlePromo,
                "BundlePromo - Multiple Basic Offering - Discount Percentage" });
        dp.add(new Object[] { new ArrayList<>(ImmutableList.of(metaOffering4, basicOffering3)),
                activeStoreWidePercentageBothOfferingsBundlePromo,
                "BundlePromo - Combination Offering - Discount Percentage" });
        dp.add(new Object[] { new ArrayList<>(ImmutableList.of(bicOffering3, bicOffering4, metaOffering2)),
                activeStoreWideCashAmountSubscriptionOffersBundlePromo,
                "BundlePromo - Multiple Subscription Offering - Discount Amount" });
        dp.add(new Object[] { new ArrayList<>(ImmutableList.of(metaOffering3, basicOffering7)),
                activeStoreWideCashAmountBothOfferingsBundlePromo,
                "BundlePromo - Combination Offering - Discount Amount" });
        return dp.iterator();
    }

    /**
     * DataProvider to pass Price Id and boolean flag to identify for packagingType if its IC or None
     *
     */
    @DataProvider(name = "getPriceIdForOfferingForPackagingType")
    public Object[][] getPriceIdForOffering() {
        return new Object[][] { { priceIdforBICWithIC, PackagingType.IC }, { priceIdforBICWithVG, PackagingType.VG },
                { priceIdforBICWithoutPackagingType, PackagingType.NONE } };
    }

    /**
     * This method reads the values of all attributes in a descriptor details Page and creates a descriptor entity
     */
    private Descriptor getDescriptorsData(final DescriptorEntityTypes entityType, final String groupName,
        final String fieldName, final String apiName, final String localized, final String maxLength) {
        final Descriptor descriptors = new Descriptor();
        descriptors.setAppFamily("Automated Tests (AUTO)");
        descriptors.setEntity(entityType);
        descriptors.setGroupName(groupName);
        descriptors.setFieldName(fieldName);
        descriptors.setApiName(apiName);
        descriptors.setLocalized(localized);
        descriptors.setMaxLength(maxLength);
        return descriptors;
    }
}
