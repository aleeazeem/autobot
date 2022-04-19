package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.FindPromotionsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionsSearchResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.Lists;

import org.hamcrest.core.Every;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * This is a test class for finding a bundle promotion in admin tool**
 *
 * @author yerragv
 */
public class FindBundlePromotionTest extends SeleniumWebdriver {

    private static JPromotion newNonStoreWideDiscountAmountPromo;
    private static JPromotion newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering;
    private static JPromotion newStoreWidePercentPromo;
    private FindPromotionsPage findPromotionsPage;
    private PromotionDetailsPage promotionDetailsPage;
    private PromotionsSearchResultPage promotionSearchResultPage;
    private static final int DEFAULT_BILLING_CYCLES = 1;
    private static final String CASH_AMOUNT = "100.21";
    private static final String PERCENTAGE_AMOUNT = "15";

    private static final Logger LOGGER = LoggerFactory.getLogger(FindBundlePromotionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        findPromotionsPage = adminToolPage.getPage(FindPromotionsPage.class);

        // open a chrome browser and login into Admin Tool
        adminToolPage.login();

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        // create a non storeWide discount amount bundled promo
        // Add subscription plans
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        newNonStoreWideDiscountAmountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering1), promotionUtils.getRandomPromoCode(),
            false, Status.NEW, null, CASH_AMOUNT, DateTimeUtils.getUTCFutureExpirationDate(), null, null,
            DEFAULT_BILLING_CYCLES, null, null);

        // create a store wide discount percent bundled promo
        newStoreWidePercentPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering1), promotionUtils.getRandomPromoCode(),
            true, Status.NEW, PERCENTAGE_AMOUNT, null, DateTimeUtils.getUTCFutureExpirationDate(), null, null,
            DEFAULT_BILLING_CYCLES, null, null);

        // Add active meta subscription for bundled offers
        final Offerings bundledBasicOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Add active subscription subscription for bundled offers
        final Offerings bundledSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // create a nonStoreWide bundled promo with both basic offering and
        // subscription offering
        final List<BundlePromoOfferings> bundlePromoOfferings = new ArrayList<>();
        bundlePromoOfferings.add(promotionUtils.createBundlePromotionOffering(bundledBasicOffering2, 5, true));
        bundlePromoOfferings.add(promotionUtils.createBundlePromotionOffering(bundledSubscriptionOffering1, 3, true));
        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                bundlePromoOfferings, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, PERCENTAGE_AMOUNT,
                null, DateTimeUtils.getUTCFutureExpirationDate(), 1, "5", "6");
    }

    /**
     * Verify, Bundle Promotion can be find by its ID.
     *
     * @result Promotion Detail Page
     */
    @Test(dataProvider = "promotions")
    public void testFindPromotionById(final JPromotion promotion) {
        LOGGER.info("Promotion ID : " + promotion.getData().getId());
        promotionDetailsPage = findPromotionsPage.findById(promotion.getData().getId());
        HelperForPromotions.assertionsForPromotions(promotion, promotionDetailsPage, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion can be find by its Code.
     *
     * @result Promotion Detail Page
     */
    @Test(dataProvider = "promotions")
    public void testFindPromotionByCode(final JPromotion promotion) {
        LOGGER.info("Promotion Code : " + promotion.getData().getCustomPromoCode());
        promotionDetailsPage =
            findPromotionsPage.selectResultRowWithFindByCode(1, promotion.getData().getCustomPromoCode());
        HelperForPromotions.assertionsForPromotions(promotion, promotionDetailsPage, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion column exist on Promotion Search Page.
     */
    @Test
    public void testBundleColumnExistInPromotionSearchResult() {

        promotionSearchResultPage = findPromotionsPage.findByCode("");
        final List<String> columnHeaders = promotionSearchResultPage.getColumnHeaders();
        AssertCollector.assertTrue("Bundled column is not present search result page",
            columnHeaders.contains(PelicanConstants.BUNDLED_COLUMN), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion column exist on Promotion Search Page.
     */
    @Test(dataProvider = "promotions")
    public void testBundleColumnValueInFindPromotion(final JPromotion promotion) {

        LOGGER.info("Promotion Code : " + promotion.getData().getCustomPromoCode());
        LOGGER.info("Promotion isBundle : " + promotion.getData().getIsBundledPromo());

        promotionSearchResultPage = findPromotionsPage.findByCode(promotion.getData().getCustomPromoCode());
        final List<String> bundledColumnValues =
            promotionSearchResultPage.getColumnValues(PelicanConstants.BUNDLED_COLUMN);
        final String isBundle = promotion.getData().getIsBundledPromo() ? PelicanConstants.YES : PelicanConstants.NO;
        AssertCollector.assertTrue("Bundled value is not present in column values",
            bundledColumnValues.contains(isBundle), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion column exist on Promotion Search Page when find by advanced search
     */
    @Test(dataProvider = "promotions")
    public void testBundleColumnValueInAdvancedFind(final JPromotion promotion) {

        LOGGER.info("PROMOTION TYPE:" + promotion.getData().getPromotionType());
        LOGGER.info("STORE ID :" + getStoreIdUs());
        LOGGER.info("PROMOTION STATUS:" + promotion.getData().getStatus());
        promotionSearchResultPage = findPromotionsPage.findByAdvancedFind(promotion.getData().getPromotionType(),
            getStoreIdUs(), promotion.getData().getStatus(), null, null);
        final List<String> bundledColumnValues =
            promotionSearchResultPage.getColumnValues(PelicanConstants.BUNDLED_COLUMN);
        final String isBundle = promotion.getData().getIsBundledPromo() ? PelicanConstants.YES : PelicanConstants.NO;
        AssertCollector.assertTrue("Bundled value is not present in column values",
            bundledColumnValues.contains(isBundle), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a data provider to return promotion objects
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "promotions")
    public static Object[][] getPromotions() {
        return new Object[][] { { newNonStoreWideDiscountAmountPromo }, { newStoreWidePercentPromo },
                { newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering } };
    }

    /**
     * Method to verify Advance search by Storewide filter returns all respective records.
     *
     * @Result : It returns Promotion Search Results Page
     */
    @Test(dataProvider = "storeWideAndBundledStatus")
    public void testStoreWideFilterInAdvancedFind(final String storeWideStatus,
        final List<String> expectedColumnStatus) {
        promotionSearchResultPage = findPromotionsPage.findByAdvancedFind(null, null, null, storeWideStatus, null);
        final List<String> storeWideColumnValues =
            promotionSearchResultPage.getColumnValues(PelicanConstants.STOREWIDE_COLUMN);
        final int totalPromotionRecords = promotionSearchResultPage.getTotalItems();
        // check if report is empty or not
        if (totalPromotionRecords > 0) {
            AssertCollector.assertThat("Store Wide Column Contains false value", storeWideColumnValues,
                Every.everyItem(isIn(expectedColumnStatus)), assertionErrorList);
        } else {
            AssertCollector.assertThat("Promotion Records are not available", totalPromotionRecords, notNullValue(),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Advance search by Bundled filter returns all respective records.
     *
     * @Result : It returns Promotion Search Results Page
     */
    @Test(dataProvider = "storeWideAndBundledStatus")
    public void testBundledFilterInAdvancedFind(final String bundledStatus, final List<String> expectedColumnStatus) {
        promotionSearchResultPage = findPromotionsPage.findByAdvancedFind(null, null, null, null, bundledStatus);
        final List<String> bundledColumnValues =
            promotionSearchResultPage.getColumnValues(PelicanConstants.BUNDLED_COLUMN);
        final int totalPromotionRecords = promotionSearchResultPage.getTotalItems();
        // check if report is empty or not
        if (totalPromotionRecords > 0) {
            AssertCollector.assertThat("Store Wide Column Contains false value", bundledColumnValues,
                Every.everyItem(isIn(expectedColumnStatus)), assertionErrorList);
        } else {
            AssertCollector.assertThat("Promotion Records are not available", totalPromotionRecords, notNullValue(),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider to set Storewide And Bundled Filter
     *
     * @return A two dimensional object array (filter state, expected column value)
     */
    @DataProvider(name = "storeWideAndBundledStatus")
    public static Object[][] getStoreWideAndBundledStatus() {
        return new Object[][] { { "ANY (*)", Arrays.asList("Yes", "No") }, { "Yes", Arrays.asList("Yes") },
                { "No", Arrays.asList("No") } };
    }

}
