package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.AddPromotionPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.ImmutableList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/***
 * This is the test class for adding the bundle promotion in the admin tool**
 *
 * @author yerragv
 */
public class AddBundlePromotionTest extends SeleniumWebdriver {

    private static Promotion newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering;
    private AddPromotionPage addPromotionPage;
    private List<String> storeIdList;
    private static final String DEFAULT_BILLING_CYCLES = "1";
    private static final String BOTH_OFFERING_TYPE = "Both";
    private static final String CASH_AMOUNT = "100.21";
    private static final String PERCENTAGE_AMOUNT = "15";
    private static final String MAXIMUM_USES = "200";
    private static final String MAXIMUM_USES_PER_USER = "150";
    private String startDate;
    private String endDate;
    private String basicOfferingExternalKey1;
    private String basicOfferingExternalKey2;
    private List<Integer> quantityToBasicOfferingList;
    private List<Integer> quantityToSubscriptionOfferingList;
    private List<Boolean> applyDiscountForBasicOfferingList;
    private List<Boolean> applyDiscountForSubscriptionOfferingList;
    private PelicanPlatform resource;
    private static String subscriptionOfferExternalKey1;
    private static String subscriptionOfferExternalKey2;
    private static List<String> expectedBasicOfferingNameList;
    private static List<String> expectedSubscriptionOfferNameList;
    private BasicOfferingApiUtils basicOfferingApiUtils;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private String promoName;
    private PromotionDetailsPage promotionsDetailPage;
    private PromotionUtils promotionUtils;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());

        // Create two basic offerings
        basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOfferingExternalKey1 = basicOffering1.getOfferings().get(0).getExternalKey();
        final Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOfferingExternalKey2 = basicOffering2.getOfferings().get(0).getExternalKey();

        expectedBasicOfferingNameList = new ArrayList<>();
        expectedBasicOfferingNameList.add(basicOffering1.getOfferings().get(0).getName());
        expectedBasicOfferingNameList.add(basicOffering2.getOfferings().get(0).getName());

        // create two bic offerings
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan1 =
            resource.subscriptionPlan().getById(subscriptionOffering1.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey1 =
            subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan2 =
            resource.subscriptionPlan().getById(subscriptionOffering2.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey2 =
            subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        expectedSubscriptionOfferNameList = new ArrayList<>();
        expectedSubscriptionOfferNameList
            .add(subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getName());
        expectedSubscriptionOfferNameList
            .add(subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getName());

        // login into Admin Tool
        adminToolPage.login();

        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        final ArrayList<String> startAndEndDayOfPromotionList = DateTimeUtils.getStartDateOfTodayAndEndDateInNextYear();
        startDate = startAndEndDayOfPromotionList.get(0);
        endDate = startAndEndDayOfPromotionList.get(1);
        storeIdList = new ArrayList<>();
        storeIdList.add(getStoreIdUs());
    }

    /**
     * Verify Add Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with subscription offering
     */
    @Test
    public void testAddStoreWidePercentageDiscountBundlePromotionWithSubscriptionOffering() {

        promoName = promotionUtils.getRandomPromoCode();
        promotionsDetailPage = addPromotionPage.addPromotion(promoName, null, true, true, null, storeIdList,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, null, 0,
            PERCENTAGE_AMOUNT, null, null, null, null,
            new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)), null,
            false, startDate, endDate, PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, MAXIMUM_USES, MAXIMUM_USES_PER_USER, true);

        AssertCollector.assertThat("Incorrect promotion Type", promotionsDetailPage.getType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect discount type", promotionsDetailPage.getDiscountType(),
            equalTo(PelicanConstants.PROMOTION_DISCOUNT_TYPE_PERCENTAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion subscription offerings",
            promotionsDetailPage.getSubscriptionOffersNameList(),
            containsInAnyOrder(expectedSubscriptionOfferNameList.toArray()), assertionErrorList);

        AssertCollector.assertThat("Incorrect promotion percentage amount",
            promotionsDetailPage.getPercentage().split("%")[0], equalTo(PERCENTAGE_AMOUNT), assertionErrorList);
        HelperForPromotions.validateFewCommonAssertions(promoName, promotionsDetailPage.getName(),
            promotionsDetailPage.getPromotionCode(), assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(startDate, endDate, Status.ACTIVE.toString(), MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, promotionsDetailPage.getEffectiveDateRange(), promotionsDetailPage.getState(),
            promotionsDetailPage.getMaximumNumberOfUses(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Add Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with subscription offering
     */
    @Test
    public void testAddNonStoreWideDiscountBundlePromotionWithSubscriptionOffering() {
        promoName = promotionUtils.getRandomPromoCode();
        promotionsDetailPage = addPromotionPage.addPromotion(promoName, null, false, true, null, storeIdList,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE, CASH_AMOUNT, 0, null, null,
            null, null, null,
            new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)), null,
            false, startDate, endDate, PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, MAXIMUM_USES, MAXIMUM_USES_PER_USER, true);

        AssertCollector.assertThat("Incorrect promotion Type", promotionsDetailPage.getType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect discount type", promotionsDetailPage.getDiscountType(),
            equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion subscription offerings",
            promotionsDetailPage.getSubscriptionOffersNameList(),
            containsInAnyOrder(expectedSubscriptionOfferNameList.toArray()), assertionErrorList);

        AssertCollector.assertThat("Incorrect promotion cash amount", promotionsDetailPage.getAmount().split(" ")[0],
            equalTo(CASH_AMOUNT), assertionErrorList);
        HelperForPromotions.validateFewCommonAssertions(promoName, promotionsDetailPage.getName(),
            promotionsDetailPage.getPromotionCode(), assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(startDate, endDate, Status.ACTIVE.toString(), MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, promotionsDetailPage.getEffectiveDateRange(), promotionsDetailPage.getState(),
            promotionsDetailPage.getMaximumNumberOfUses(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with basic offering
     */
    @Test
    public void testAddStoreWidePercentageDiscountBundlePromotionWithBasicOffering() {

        promoName = promotionUtils.getRandomPromoCode();
        promotionsDetailPage = addPromotionPage.addPromotion(promoName, null, true, true, null, storeIdList,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, null, 0,
            PERCENTAGE_AMOUNT, null, null, null,
            new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)), null, null, false,
            startDate, endDate, PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, MAXIMUM_USES, MAXIMUM_USES_PER_USER, true);

        AssertCollector.assertThat("Incorrect promotion Type", promotionsDetailPage.getType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect discount type", promotionsDetailPage.getDiscountType(),
            equalTo(PelicanConstants.PROMOTION_DISCOUNT_TYPE_PERCENTAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion basic offerings",
            promotionsDetailPage.getBasicOfferingsNameList(),
            containsInAnyOrder(expectedBasicOfferingNameList.toArray()), assertionErrorList);

        AssertCollector.assertThat("Incorrect promotion percentage amount",
            promotionsDetailPage.getPercentage().split("%")[0], equalTo(PERCENTAGE_AMOUNT), assertionErrorList);
        HelperForPromotions.validateFewCommonAssertions(promoName, promotionsDetailPage.getName(),
            promotionsDetailPage.getPromotionCode(), assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(startDate, endDate, Status.ACTIVE.toString(), MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, promotionsDetailPage.getEffectiveDateRange(), promotionsDetailPage.getState(),
            promotionsDetailPage.getMaximumNumberOfUses(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify, Add Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with basic offering
     */
    @Test
    public void testAddNonStoreWideDiscountBundlePromotionWithBasicOffering() {

        promoName = promotionUtils.getRandomPromoCode();

        promotionsDetailPage = addPromotionPage.addPromotion(promoName, null, false, true, null, storeIdList,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE, CASH_AMOUNT, 0, null, null,
            null, null, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)), null,
            null, false, startDate, endDate, PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, MAXIMUM_USES, MAXIMUM_USES_PER_USER, true);

        AssertCollector.assertThat("Incorrect promotion Type", promotionsDetailPage.getType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect discount type", promotionsDetailPage.getDiscountType(),
            equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion basic offerings",
            promotionsDetailPage.getBasicOfferingsNameList(),
            containsInAnyOrder(expectedBasicOfferingNameList.toArray()), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", promotionsDetailPage.getAmount().split(" ")[0],
            equalTo(CASH_AMOUNT), assertionErrorList);
        AssertCollector.assertThat("Incorrect store Id", promotionsDetailPage.getDiscountAmountStore().split(" ")[0],
            equalTo(getStoreExternalKeyUs()), assertionErrorList);
        HelperForPromotions.validateFewCommonAssertions(promoName, promotionsDetailPage.getName(),
            promotionsDetailPage.getPromotionCode(), assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(startDate, endDate, Status.ACTIVE.toString(), MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, promotionsDetailPage.getEffectiveDateRange(), promotionsDetailPage.getState(),
            promotionsDetailPage.getMaximumNumberOfUses(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering
     */
    @Test
    public void testAddStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOffering() {
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        final String basicOfferingExternalKey1 = basicOffering1.getOfferings().get(0).getExternalKey();
        final Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        final String basicOfferingExternalKey2 = basicOffering2.getOfferings().get(0).getExternalKey();

        final List<String> expectedBasicOfferingNameList = new ArrayList<>();
        expectedBasicOfferingNameList.add(basicOffering1.getOfferings().get(0).getName());
        expectedBasicOfferingNameList.add(basicOffering2.getOfferings().get(0).getName());

        // create two bic offerings
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan1 =
            resource.subscriptionPlan().getById(subscriptionOffering1.getOfferings().get(0).getId(), null);
        final String subscriptionOfferExternalKey1 =
            subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan2 =
            resource.subscriptionPlan().getById(subscriptionOffering2.getOfferings().get(0).getId(), null);
        final String subscriptionOfferExternalKey2 =
            subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final List<String> expectedSubscriptionOfferNameList = new ArrayList<>();
        expectedSubscriptionOfferNameList
            .add(subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getName());
        expectedSubscriptionOfferNameList
            .add(subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getName());

        promoName = promotionUtils.getRandomPromoCode();
        promotionsDetailPage = addPromotionPage.addPromotion(promoName, null, true, true, null, storeIdList,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, null, 0,
            PERCENTAGE_AMOUNT, null, null, null,
            new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
            new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)), null,
            false, startDate, endDate, PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, MAXIMUM_USES, MAXIMUM_USES_PER_USER, true);

        AssertCollector.assertThat("Incorrect promotion Type", promotionsDetailPage.getType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect discount type", promotionsDetailPage.getDiscountType(),
            equalTo(PelicanConstants.PROMOTION_DISCOUNT_TYPE_PERCENTAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion basic offerings",
            promotionsDetailPage.getBasicOfferingsNameList(),
            containsInAnyOrder(expectedBasicOfferingNameList.toArray()), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion subscription offerings",
            promotionsDetailPage.getSubscriptionOffersNameList(),
            containsInAnyOrder(expectedSubscriptionOfferNameList.toArray()), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion percentage amount",
            promotionsDetailPage.getPercentage().split("%")[0], equalTo(PERCENTAGE_AMOUNT), assertionErrorList);
        HelperForPromotions.validateFewCommonAssertions(promoName, promotionsDetailPage.getName(),
            promotionsDetailPage.getPromotionCode(), assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(startDate, endDate, Status.ACTIVE.toString(), MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, promotionsDetailPage.getEffectiveDateRange(), promotionsDetailPage.getState(),
            promotionsDetailPage.getMaximumNumberOfUses(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering
     */
    @Test
    public void testAddNonStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOffering() {

        promoName = promotionUtils.getRandomPromoCode();
        promotionsDetailPage = addPromotionPage.addPromotion(promoName, null, false, true, null, storeIdList,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, null, 0,
            PERCENTAGE_AMOUNT, null, null, null,
            new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
            new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)), null,
            false, startDate, endDate, PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, MAXIMUM_USES, MAXIMUM_USES_PER_USER, true);

        AssertCollector.assertThat("Incorrect promotion Type", promotionsDetailPage.getType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect discount type", promotionsDetailPage.getDiscountType(),
            equalTo(PelicanConstants.PROMOTION_DISCOUNT_TYPE_PERCENTAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion basic offerings",
            promotionsDetailPage.getBasicOfferingsNameList(),
            containsInAnyOrder(expectedBasicOfferingNameList.toArray()), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion subscription offerings",
            promotionsDetailPage.getSubscriptionOffersNameList(),
            containsInAnyOrder(expectedSubscriptionOfferNameList.toArray()), assertionErrorList);

        AssertCollector.assertThat("Incorrect promotion percentage amount",
            promotionsDetailPage.getPercentage().split("%")[0], equalTo(PERCENTAGE_AMOUNT), assertionErrorList);
        HelperForPromotions.validateFewCommonAssertions(promoName, promotionsDetailPage.getName(),
            promotionsDetailPage.getPromotionCode(), assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(startDate, endDate, Status.ACTIVE.toString(), MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, promotionsDetailPage.getEffectiveDateRange(), promotionsDetailPage.getState(),
            promotionsDetailPage.getMaximumNumberOfUses(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify Add Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with invalid quantity for basic
     * offering
     */
    @Test
    public void testAddNonStoreWidePercentageDiscountBundlePromotionWithInvalidQuantityForBasicOffering() {

        // Construct a promotion object for non store-wide percent discount for bundle promotion
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering = HelperForPromotions.getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_PER_USER,
            Status.NEW.toString(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, true, assertionErrorList);

        quantityToBasicOfferingList = HelperForPromotions.getInvalidQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = HelperForPromotions.getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = HelperForPromotions.getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = HelperForPromotions.getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering =
            HelperForPromotions.addDiscountPromo(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList,
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForBasicOfferingInvalidQuantity("Invalid-Quantity");
        final Promotion newPromotion =
            addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering);
        AssertCollector.assertThat("Invalid error type", newPromotion.getErrorMessageForBasicOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with invalid quantity for basic
     * offering
     */
    @Test
    public void testAddNonStoreWidePercentageDiscountBundlePromotionWithInvalidQuantityForSubscriptionOffering() {

        // Construct a promotion object for non store-wide percent discount for bundle promotion
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering = HelperForPromotions.getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_PER_USER,
            Status.NEW.toString(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, true, assertionErrorList);

        quantityToBasicOfferingList = HelperForPromotions.getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = HelperForPromotions.getInvalidQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = HelperForPromotions.getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = HelperForPromotions.getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering =
            HelperForPromotions.addDiscountPromo(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList,
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForSubscriptionOfferingInvalidQuantity("Invalid-Quantity");
        final Promotion newPromotion =
            addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering);
        AssertCollector.assertThat("Invalid error type",
            newPromotion.getErrorMessageForSubscriptionOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with invalid quantity for basic
     * offering
     */
    @Test
    public void testAddNonStoreWidePercentageDiscountBundlePromotionWithInvalidQuantityForBothBasicOfferingAndSubscriptionOffering() {

        // Construct a promotion object for non store-wide percent discount for bundle promotion
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering = HelperForPromotions.getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_PER_USER,
            Status.NEW.toString(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, true, assertionErrorList);

        quantityToBasicOfferingList = HelperForPromotions.getInvalidQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = HelperForPromotions.getInvalidQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = HelperForPromotions.getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = HelperForPromotions.getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering =
            HelperForPromotions.addDiscountPromo(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList,
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForBasicOfferingInvalidQuantity("Invalid-Quantity");
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForSubscriptionOfferingInvalidQuantity("Invalid-Quantity");
        final Promotion newPromotion =
            addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering);
        AssertCollector.assertThat("Invalid error type", newPromotion.getErrorMessageForBasicOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Invalid error type",
            newPromotion.getErrorMessageForSubscriptionOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Add Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with more than 5 basic
     * offerings
     */
    @Test
    public void testAddNonStoreWideDiscountBundlePromotionWithMoreThanFiveBasicOffering() {

        // Create 4 basic offerings
        basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        Offerings basicOffering3 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering3 = resource.offerings().getOfferingById(basicOffering3.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey3 = basicOffering3.getOfferings().get(0).getExternalKey();
        Offerings basicOffering4 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering4 = resource.offerings().getOfferingById(basicOffering4.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey4 = basicOffering4.getOfferings().get(0).getExternalKey();
        Offerings basicOffering5 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering5 = resource.offerings().getOfferingById(basicOffering5.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey5 = basicOffering5.getOfferings().get(0).getExternalKey();
        Offerings basicOffering6 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering6 = resource.offerings().getOfferingById(basicOffering6.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey6 = basicOffering6.getOfferings().get(0).getExternalKey();

        // Construct a promotion object for non store-wide percent discount for bundle promotion
        Promotion newNonStoreWideDiscountAmountPromoForBasicOffering = HelperForPromotions.getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            PelicanConstants.BASIC_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_PER_USER,
            Status.NEW.toString(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, true, assertionErrorList);
        newNonStoreWideDiscountAmountPromoForBasicOffering =
            HelperForPromotions.addDiscountPromo(newNonStoreWideDiscountAmountPromoForBasicOffering, false, null, null,
                null, null, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        newNonStoreWideDiscountAmountPromoForBasicOffering = HelperForPromotions.addMoreBasicOfferingsToPromotion(
            newNonStoreWideDiscountAmountPromoForBasicOffering, basicOfferingExternalKey1, basicOfferingExternalKey2,
            basicOfferingExternalKey3, basicOfferingExternalKey4, basicOfferingExternalKey5, basicOfferingExternalKey6);
        newNonStoreWideDiscountAmountPromoForBasicOffering.setErrorMessageForMoreBasicOfferings("more-offerings");
        final Promotion newPromotion =
            addPromotionPage.addPromotion(newNonStoreWideDiscountAmountPromoForBasicOffering);
        AssertCollector.assertThat("Incorrect error message for more than 5 basic offerings",
            newPromotion.getErrorMessageForMoreBasicOfferings(),
            equalTo(PelicanErrorConstants.EXPECTED_MORE_OFFERING_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Add Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with more than 5 subscription
     * offerings
     */
    @Test
    public void testAddNonStoreWideDiscountBundlePromotionWithMoreThanFiveSubscriptionOffering() {

        // Create 4 bic offerings
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering3 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan3 =
            resource.subscriptionPlan().getById(subscriptionOffering3.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey3 =
            subscriptionPlan3.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering4 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan4 =
            resource.subscriptionPlan().getById(subscriptionOffering4.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey4 =
            subscriptionPlan4.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering5 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan5 =
            resource.subscriptionPlan().getById(subscriptionOffering5.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey5 =
            subscriptionPlan5.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering6 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan6 =
            resource.subscriptionPlan().getById(subscriptionOffering6.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey6 =
            subscriptionPlan6.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        // Construct a promotion object for non store-wide percent discount for bundle promotion
        Promotion newNonStoreWideDiscountAmountPromoForSubscriptionOffer = HelperForPromotions.getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, Status.NEW.toString(), PelicanConstants.START_HOUR, PelicanConstants.START_MINUTE,
            PelicanConstants.START_SECOND, PelicanConstants.END_HOUR, PelicanConstants.END_MINUTE,
            PelicanConstants.END_SECOND, true, assertionErrorList);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffer = HelperForPromotions.addDiscountPromo(
            newNonStoreWideDiscountAmountPromoForSubscriptionOffer, false, null, null, null, null,
            new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
            new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        newNonStoreWideDiscountAmountPromoForSubscriptionOffer = HelperForPromotions
            .addMoreSubscriptionOfferingsToPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffer,
                subscriptionOfferExternalKey1, subscriptionOfferExternalKey2, subscriptionOfferingExternalKey3,
                subscriptionOfferingExternalKey4, subscriptionOfferingExternalKey5, subscriptionOfferingExternalKey6);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffer
            .setErrorMessageForMoreSubscriptionOfferings("more-offerings");
        final Promotion newPromotion =
            addPromotionPage.addPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffer);
        AssertCollector.assertThat("Incorrect error message for more than 5 subscription offerings",
            newPromotion.getErrorMessageForMoreSubscriptionOfferings(),
            equalTo(PelicanErrorConstants.EXPECTED_MORE_OFFERING_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
