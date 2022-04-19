package com.autodesk.bsm.pelican.api.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Promotions;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.ImmutableList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Promotion API Test
 *
 * @author t_mohag
 */
public class AddPromotionTest extends BaseTestData {

    private static List<BundlePromoOfferings> bundlePromoOfferingsWithBasicOffers;
    private static List<BundlePromoOfferings> bundlePromoOfferingsWithSubscriptionOffers;
    private static List<BundlePromoOfferings> bundlePromoOfferingsWithBothOffers;

    private PelicanPlatform resource;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private List<JStore> storeList;
    private List<Offerings> basicOfferingsList;
    private List<Offerings> subscriptionOfferingsList;
    private final List<String> promoCodes = new ArrayList<>();
    private JPromotion createdPromo;
    private PromotionUtils promotionUtils;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        // create multiple stores
        final JStore store1 = storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final JStore store2 = storeApiUtils.addStore(Status.ACTIVE, Country.GB, Currency.EUR, null, false);
        final JStore store3 = storeApiUtils.addStore(Status.ACTIVE, Country.CA, Currency.USD, null, false);
        // create store list
        storeList = new ArrayList<>();
        storeList.add(store1);
        storeList.add(store2);
        storeList.add(store3);

        // get externalKeyOfPriceList for all stores
        final String externalKeyOfPriceListStore1 = store1.getIncluded().getPriceLists().get(0).getExternalKey();
        final String externalKeyOfPriceListStore2 = store1.getIncluded().getPriceLists().get(0).getExternalKey();
        final String externalKeyOfPriceListStore3 = store1.getIncluded().getPriceLists().get(0).getExternalKey();

        // create multiple basic offerings
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(externalKeyOfPriceListStore1,
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(externalKeyOfPriceListStore2,
            OfferingType.PERPETUAL, MediaType.USB, Status.ACTIVE, UsageType.COM, null);
        // adding to basic offerings list
        basicOfferingsList = new ArrayList<>();
        basicOfferingsList.add(basicOffering1);
        basicOfferingsList.add(basicOffering2);
        // creating multiple subscription offerings
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // Creating 3 offerings for BIC, Meta and Perpetual
        final Offerings bicSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceListStore1, OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicSubscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceListStore2, OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings bicSubscriptionOffering3 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceListStore3, OfferingType.META_SUBSCRIPTION,
                BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // adding to subscription offerings list
        subscriptionOfferingsList = new ArrayList<>();
        subscriptionOfferingsList.add(bicSubscriptionOffering1);
        subscriptionOfferingsList.add(bicSubscriptionOffering2);
        subscriptionOfferingsList.add(bicSubscriptionOffering3);

        // Bundle Promo Offerings with Basic Offerings only.
        bundlePromoOfferingsWithBasicOffers = new ArrayList<>();
        bundlePromoOfferingsWithBasicOffers.add(promotionUtils.createBundlePromotionOffering(basicOffering1, 1, true));
        bundlePromoOfferingsWithBasicOffers.add(promotionUtils.createBundlePromotionOffering(basicOffering2, 1, true));

        // Bundle Promo Offerings with Subscription Offerings only.
        bundlePromoOfferingsWithSubscriptionOffers = new ArrayList<>();
        bundlePromoOfferingsWithSubscriptionOffers
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering1, 1, true));
        bundlePromoOfferingsWithSubscriptionOffers
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering2, 1, true));
        bundlePromoOfferingsWithSubscriptionOffers
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering3, 1, true));

        // Bundle Promo Offerings with Basic and Subscription Offerings.
        bundlePromoOfferingsWithBothOffers = new ArrayList<>();
        bundlePromoOfferingsWithBothOffers.add(promotionUtils.createBundlePromotionOffering(basicOffering1, 1, true));
        bundlePromoOfferingsWithBothOffers
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering3, 1, true));
    }

    /**
     * Verify ADD promotion with discount percentage
     */
    @Test
    public void testAddPromoWithDiscountPercentSuccess() {
        // creating promotion
        createdPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, storeList, basicOfferingsList,
            promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, "12", null,
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 1, "5", "6");
        // validating promotion
        verifySuccessfulPromo(createdPromo);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify ADD promotion with cash discount
     */
    @Test
    public void testAddPromoWithCashDiscountSuccess() {
        // creating promotion
        createdPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, storeList, basicOfferingsList,
            promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null, "99",
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 1, "5", "6");
        // validating promotion
        verifySuccessfulPromo(createdPromo);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify ADD promotion with supplement
     */
    @Test
    public void testAddPromoWithSupplementSuccess() {

        // creating promotion
        createdPromo = promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME, storeList, subscriptionOfferingsList,
            promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null, null,
            DateTimeUtils.getUTCFutureExpirationDate(), "1", "MONTH", 2, "3", "2");
        // validating promotion
        verifySuccessfulPromo(createdPromo);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to create bundle promotion for the given data provider and invoke the GET promotion API and validate the
     * response. This method also adds the created promotion's codes to an array so that next test method can use these
     * promo codes to invoke the GET promotion by code
     *
     * @param type
     * @param bundlePromoOfferings
     * @param discountPercentage
     * @param discountAmount
     */
    @Test(dataProvider = "bundlePromoData")
    public void testAddBundlePromoCreation(final PromotionType type,
        final List<BundlePromoOfferings> bundlePromoOfferings, final String discountPercentage,
        final String discountAmount) {
        // creating bundle promotion
        createdPromo = promotionUtils.addBundlePromotion(type, storeList, bundlePromoOfferings,
            promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, discountPercentage, discountAmount,
            DateTimeUtils.getUTCFutureExpirationDate(), 1, "5", "6");
        final Promotions promotionById = resource.promotions()
            .getPromotionsByIds(new ArrayList<>(ImmutableList.of(createdPromo.getData().getId())), null);
        AssertCollector.assertTrue("Invalid value for isBundled. isBundled should have been true.",
            promotionById.getData().get(0).getIsBundled(), assertionErrorList);
        // Adding the created promotion's code to the list. This list is used in
        // the next method
        // testBundlePromoInvokeByCodes()
        promoCodes.add(createdPromo.getData().getCustomPromoCode());
        // validating promotion
        verifySuccessfulPromo(createdPromo);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test(dependsOnMethods = "testAddBundlePromoCreation",
        description = "Invoke the Get Promotion API by code for a list of Bundled promos and validate the response body "
            + "contain the isBundled flag value set to true ")
    public void testBundlePromoInvokeByCodes() {
        // Invoking GET promotions API by code
        final Promotions promotionByCode = resource.promotions().getPromotionsByCodes(promoCodes, null);
        for (final JPromotionData promotionData : promotionByCode.getData()) {
            AssertCollector.assertTrue("Invalid value for isBundled. isBundled should have been true.",
                promotionData.getIsBundled(), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    // Method to do validate if a promotion is successfully created
    private void verifySuccessfulPromo(final JPromotion createdPromo) {
        AssertCollector.assertThat("Invalid promo id", createdPromo.getData().getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid entity type ", createdPromo.getData().getType(),
            equalTo(EntityType.PROMOTION), assertionErrorList);
    }

    @DataProvider(name = "bundlePromoData")
    private static Object[][] getBundlePromoData() {
        return new Object[][] { { PromotionType.DISCOUNT_PERCENTAGE, bundlePromoOfferingsWithBasicOffers, "10", null },
                { PromotionType.DISCOUNT_AMOUNT, bundlePromoOfferingsWithSubscriptionOffers, null, "99" },
                { PromotionType.DISCOUNT_AMOUNT, bundlePromoOfferingsWithBothOffers, null, "99" } };
    }

}
