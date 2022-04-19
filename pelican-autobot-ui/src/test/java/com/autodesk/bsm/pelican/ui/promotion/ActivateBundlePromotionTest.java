package com.autodesk.bsm.pelican.ui.promotion;

import static com.autodesk.bsm.pelican.enums.Status.ACTIVE;
import static com.autodesk.bsm.pelican.enums.Status.NEW;
import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.AddPromotionPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/***
 * This test
 *
 * class tests the Activation Functionality of Bundle promotion**
 *
 * @author Shweta Hegde
 */

public class ActivateBundlePromotionTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static Promotion promotion;
    private List<String> storeIdList;
    private static final String BOTH_OFFERING_TYPE = "Both";
    private static final String CASH_AMOUNT = "200.00";
    private static final String PERCENTAGE_AMOUNT = "20";
    private static final String MAXIMUM_USES = "100";
    private static final String MAXIMUM_USES_PER_USER = "5";
    private static final String START_HOUR = "03";
    private static final String START_MINUTE = "08";
    private static final String START_SECOND = "56";
    private static final String END_HOUR = "09";
    private static final String END_MINUTE = "28";
    private static final String END_SECOND = "26";
    private static String subscriptionOfferExternalKey1;
    private static String subscriptionOfferExternalKey2;
    private static String subscriptionOfferExternalKey3;
    private static String subscriptionOfferExternalKey4;
    private static String subscriptionOfferExternalKey5;
    private String basicOfferingExternalKey1;
    private String basicOfferingExternalKey2;
    private String basicOfferingExternalKey3;
    private String basicOfferingExternalKey4;
    private String basicOfferingExternalKey5;
    private AddPromotionPage addPromotionPage;
    private String startDate;
    private String endDate;
    private String startDateInFuture;
    private String endDateInFuture;
    private Promotion newPromotion;
    private String promotionName;

    /**
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        final PelicanPlatform resource =
            new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        // Create two basic offerings
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, ACTIVE, UsageType.COM, null);
        basicOfferingExternalKey1 = basicOffering1.getOfferings().get(0).getExternalKey();

        Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, ACTIVE, UsageType.COM, null);
        basicOffering2 = resource.offerings().getOfferingById(basicOffering2.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey2 = basicOffering2.getOfferings().get(0).getExternalKey();

        Offerings basicOffering3 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, ACTIVE, UsageType.COM, null);
        basicOffering3 = resource.offerings().getOfferingById(basicOffering3.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey3 = basicOffering3.getOfferings().get(0).getExternalKey();

        Offerings basicOffering4 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, ACTIVE, UsageType.COM, null);
        basicOffering4 = resource.offerings().getOfferingById(basicOffering4.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey4 = basicOffering4.getOfferings().get(0).getExternalKey();

        Offerings basicOffering5 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, ACTIVE, UsageType.COM, null);
        basicOffering5 = resource.offerings().getOfferingById(basicOffering5.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey5 = basicOffering5.getOfferings().get(0).getExternalKey();

        // create two bic offerings
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan1 =
            resource.subscriptionPlan().getById(subscriptionOffering1.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey1 =
            subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan2 =
            resource.subscriptionPlan().getById(subscriptionOffering2.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey2 =
            subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final Offerings subscriptionOffering3 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan3 =
            resource.subscriptionPlan().getById(subscriptionOffering3.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey3 =
            subscriptionPlan3.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final Offerings subscriptionOffering4 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan4 =
            resource.subscriptionPlan().getById(subscriptionOffering4.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey4 =
            subscriptionPlan4.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final Offerings subscriptionOffering5 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.YEAR, ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan5 =
            resource.subscriptionPlan().getById(subscriptionOffering5.getOfferings().get(0).getId(), null);
        subscriptionOfferExternalKey5 =
            subscriptionPlan5.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        startDate = DateTimeUtils.getNowPlusDays(1);
        endDate = DateTimeUtils.getNowPlusDays(30);

        startDateInFuture = DateTimeUtils.getNowPlusDays(40);
        endDateInFuture = DateTimeUtils.getNowPlusDays(80);
        storeIdList = new ArrayList<>();
        storeIdList.add(getStoreIdUs());

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        // These 2 storewide non bundle promotions are created, so that error is thrown when try to create bundle
        // storewide
        // promotion for the same offering
        promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(subscriptionOffering4), promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, null,
            "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(basicOffering4), promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "20.00", null,
            DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);

    }

    /**
     * This method takes various dataprovider to test the below scenarios 1. Bundle Promotion Activation for
     * storewide/non storewide, discount amount/percentage, basic offerings/subscription offers, and both 2. Two ACTIVE
     * storewide bundle promotion can exist if effective time range is different 3. Two ACTIVE, INACTIVE storewide
     * bundle promotion can exist in the same time frame 4. Storewide and non storewide bundle promotion can exist in
     * the same time frame
     *
     * @param discountType
     * @param products
     * @param discount
     * @param maximumUses
     * @param maximumUserPerUser
     * @param status
     * @param isStorewide
     * @param basicOfferingsList
     * @param subscriptionOfferList
     * @param startDate
     * @param endDate
     */
    @Test(dataProvider = "promotionDetails")
    public void testSuccessPromotionActivationWithRequiredFields(final String discountType, final String products,
        final String discount, final String maximumUses, final String maximumUserPerUser, final Status status,
        final boolean isStorewide, final ArrayList<String> basicOfferingsList,
        final ArrayList<String> subscriptionOfferList, final String startDate, final String endDate) {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(storeIdList, null, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
            discountType, products, discount, startDate, endDate, maximumUses, maximumUserPerUser, status.toString(),
            START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true, assertionErrorList);
        // Add more fields to promotion object
        promotion = HelperForPromotions.addDiscountPromo(promotion, isStorewide, null, null, null, null,
            basicOfferingsList, subscriptionOfferList);
        // Add the promotion in the admin tool
        newPromotion = addPromotionPage.addPromotion(promotion);

        promotionName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        if (PromotionType.DISCOUNT_PERCENTAGE.getDisplayName().contains(discountType)) {
            AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
                equalTo(discountType + " " + PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
            AssertCollector.assertThat("Incorrect promotion percentage", newPromotion.getPercentageAmount(),
                equalTo(discount + "%"), assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
                equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
            AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(discount),
                assertionErrorList);
        }
        HelperForPromotions.validateFewCommonAssertions(newPromotion, promotionName, getStoreExternalKeyUs(),
            assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(newPromotion, startDate, endDate, status, null, null,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "promotionDetails")
    private Object[][] getPromotionDetails() {
        return new Object[][] {
                // below are the promotions which are in INACTIVE condition
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, BOTH_OFFERING_TYPE, PERCENTAGE_AMOUNT, null, null, NEW,
                        true, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey2)), startDate, endDate },
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE,
                        PERCENTAGE_AMOUNT, null, null, NEW, false, null,
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1)), startDateInFuture,
                        endDateInFuture },

                // Below are the ACTIVE bundle Promotions
                // Storewide Bundle Promotions in Present
                // Storewide bundle promotion for subscription offer
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE,
                        PERCENTAGE_AMOUNT, null, null, ACTIVE, true, null,
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1)), startDate, endDate },
                // Storewide bundle promotion for basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, CASH_AMOUNT, null, null,
                        ACTIVE, true, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)), null, startDate,
                        endDate },

                // Storewide Bundle Promotions in Future
                // Storewide bundle promotion for subscription offer
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE,
                        PERCENTAGE_AMOUNT, null, null, ACTIVE, true, null,
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1)), startDateInFuture,
                        endDateInFuture },
                // Storewide bundle promotion for subscription offer and basic offering
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, BOTH_OFFERING_TYPE, PERCENTAGE_AMOUNT, null, null, ACTIVE,
                        true, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey2)), startDateInFuture,
                        endDateInFuture },

                // Non Storewide Bundle Promotions in Present
                // Non-Storewide bundle promotion for basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, CASH_AMOUNT, null, null,
                        ACTIVE, false, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)), null, startDate,
                        endDate },
                // Non-Storewide bundle promotion for subscription offer and basic offering
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, BOTH_OFFERING_TYPE, PERCENTAGE_AMOUNT, null, null, ACTIVE,
                        false, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey2)), startDate, endDate },

                // Non Storewide Bundle Promotions in Future
                // Non-Storewide bundle promotion for subscription offer
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE,
                        PERCENTAGE_AMOUNT, null, null, ACTIVE, false, null,
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1)), startDateInFuture,
                        endDateInFuture },
                // Non-Storewide bundle promotion for subscription offer and basic offering
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, BOTH_OFFERING_TYPE, PERCENTAGE_AMOUNT, null, null, ACTIVE,
                        false, new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey2)), startDateInFuture,
                        endDateInFuture } };
    }

    /**
     * This method tests activation error if already a storewide promotion exists in the same time frame
     *
     * @param discountType
     * @param products
     * @param discount
     * @param basicOfferingsList
     * @param subscriptionOfferList
     */
    @Test(dataProvider = "duplicateStorewidePromotionDetails",
        dependsOnMethods = "testSuccessPromotionActivationWithRequiredFields")
    public void testActivationErrorForBundlePromotionWithAnotherActiveStoreWidePromotion(final String discountType,
        final String products, final String discount, final ArrayList<String> basicOfferingsList,
        final ArrayList<String> subscriptionOfferList) {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion =
            HelperForPromotions.getPromotion(storeIdList, null, PelicanConstants.DISCOUNT_PROMOTION_TYPE, discountType,
                products, discount, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_PER_USER, Status.ACTIVE.toString(),
                START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true, assertionErrorList);
        // Add more fields to promotion object
        promotion = HelperForPromotions.addDiscountPromo(promotion, true, null, null, null, null, basicOfferingsList,
            subscriptionOfferList);
        // Add the promotion in the admin tool
        newPromotion = addPromotionPage.addPromotion(promotion);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        AssertCollector.assertThat("Please correct the below error is not thrown",
            addPromotionPage.getAllErrorMessages(), equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Storewide promotion already exists for a offering/offer error is not thrown",
            addPromotionPage.getDuplicateStorewidePromotionError(),
            equalTo(PelicanErrorConstants.STORE_WIDE_PROMO_ALREADY_EXISTS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "duplicateStorewidePromotionDetails")
    private Object[][] getProductDetails() {

        adminToolPage.login();
        return new Object[][] {
                // Storewide cash amount bundle promotion for basic offering (which is same as existing)
                { PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, PERCENTAGE_AMOUNT,
                        new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)), null },
                // Storewide cash amount bundle promotion for subscription offer (which is same as existing)
                { PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, CASH_AMOUNT, null,
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1)) },
                // Storewide cash amount bundle promotion for subscription offer and basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, BOTH_OFFERING_TYPE, CASH_AMOUNT,
                        new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2)),
                        new ArrayList<>(
                            ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)) },
                // Storewide cash amount bundle promotion for subscription offer and basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, BOTH_OFFERING_TYPE, CASH_AMOUNT,
                        new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey3)) },
                // Storewide cash amount bundle promotion for subscription offer and basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, BOTH_OFFERING_TYPE, CASH_AMOUNT,
                        new ArrayList<>(ImmutableList.of(basicOfferingExternalKey3)),
                        new ArrayList<>(
                            ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)) },
                // Storewide cash amount bundle promotion for subscription offer and basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, CASH_AMOUNT,
                        new ArrayList<>(ImmutableList.of(basicOfferingExternalKey4)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey5)) },
                // Storewide cash amount bundle promotion for subscription offer and basic offering
                { PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, CASH_AMOUNT,
                        new ArrayList<>(ImmutableList.of(basicOfferingExternalKey5)),
                        new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey4)) }, };

    }

    /**
     * This method test activation errors if required fields are not provided
     */
    @Test
    public void testActivationErrorWithOutRequiredFields() {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(storeIdList, null, null, null, null, null, null, null, null, null,
            ACTIVE.toString(), null, null, null, null, null, null, true, assertionErrorList);
        // Add more fields to promotion object
        promotion = HelperForPromotions.addDiscountPromo(promotion, true, null, null, null, null, null, null);
        // Add the promotion in the admin tool
        addPromotionPage.addPromotion(promotion);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        AssertCollector.assertThat("Please correct the below error is not thrown",
            addPromotionPage.getAllErrorMessages(), equalTo(PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Discount Amount Required Message is not thrown",
            addPromotionPage.getDiscountAmountMissingError(), equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Offerings/offer required message is not thrown",
            addPromotionPage.getOfferingsMissingError(), equalTo(PelicanErrorConstants.OFFERINGS_OFFERS_REQUIRED),
            assertionErrorList);
        AssertCollector.assertThat("Effective Date Range Required Message is not thrown",
            addPromotionPage.getEffectiveDateRangeMissingError(),
            equalTo(PelicanErrorConstants.EFFECTIVE_DATE_REQUIRED), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests activation error when store id is not provided
     */
    @Test
    public void testActivationErrorWithOutStoreId() {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(null, null, null, null, null, null, null, null, null, null,
            ACTIVE.toString(), null, null, null, null, null, null, true, assertionErrorList);
        // Add more fields to promotion object
        promotion = HelperForPromotions.addDiscountPromo(promotion, true, null, null, null, null, null, null);
        // Add the promotion in the admin tool
        addPromotionPage.addPromotion(promotion);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        AssertCollector.assertThat("Please correct the below error is not thrown",
            addPromotionPage.getAllErrorMessages(), equalTo(PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Store is required Message is not thrown", addPromotionPage.getStoreMissingError(),
            equalTo(PelicanErrorConstants.SELECT_AN_APPLICABLE_STORE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests activation error when effective dates are in past
     *
     */
    @Test
    public void testActivationErrorWithPastEffectiveDates() {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(storeIdList, null, null, null, null, null,
            DateTimeUtils.getNowMinusDays(4), DateTimeUtils.getNowMinusDays(3), null, null, ACTIVE.toString(), null,
            null, null, null, null, null, true, assertionErrorList);
        // Add more fields to promotion object
        promotion = HelperForPromotions.addDiscountPromo(promotion, true, null, null, null, null, null, null);
        // Add the promotion in the admin tool
        newPromotion = addPromotionPage.addPromotion(promotion);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        AssertCollector.assertThat("Please correct the below error is not thrown",
            addPromotionPage.getAllErrorMessages(), equalTo(PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Effective date range in past error is not thrown",
            addPromotionPage.getPastEffectiveDateError(), equalTo(PelicanErrorConstants.EFFECTIVE_DATES_IN_PAST),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests activation warning when subscription offers do not have same billing terms
     */
    @Test
    public void testActivationWarningWhenSubscriptionOffersDoNotHaveSameBillingFrequency() {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(storeIdList, null, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
            PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, PERCENTAGE_AMOUNT,
            startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_PER_USER, Status.ACTIVE.toString(), START_HOUR, START_MINUTE,
            START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true, assertionErrorList);
        // Add more fields to promotion object
        promotion = HelperForPromotions.addDiscountPromo(promotion, true, null, null, null, null, null,
            new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey2, subscriptionOfferExternalKey3)));
        // Add the promotion in the admin tool
        newPromotion = addPromotionPage.addPromotion(promotion);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        AssertCollector.assertThat("Different Billing Frequency warning is not thrown",
            addPromotionPage.getDifferentOffersTermWarning(),
            equalTo(PelicanErrorConstants.NOT_ALL_OFFERS_HAVE_SAME_TERMS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests activation error when not even one offer/offerings have apply discount checkbox checked
     */
    @Test(dataProvider = "offeringsOfferForApplyDiscount")
    public void testActivationErrorWhenOffersOfferingsDoNotHaveApplyDiscount(final String offers) {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(storeIdList, null, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
            PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, offers, PERCENTAGE_AMOUNT, startDate, endDate, MAXIMUM_USES,
            MAXIMUM_USES_PER_USER, Status.ACTIVE.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR,
            END_MINUTE, END_SECOND, true, assertionErrorList);
        // Add more fields to promotion object
        if (offers.equals(PelicanConstants.SUBSCRIPTION_OFFERS_TYPE)) {
            promotion = HelperForPromotions.addDiscountPromo(promotion, false, null, null, null,
                new ArrayList<>(ImmutableList.of(false, false)), null,
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey3, subscriptionOfferExternalKey4)));
        } else if (offers.equals(PelicanConstants.BASIC_OFFERING_TYPE)) {
            promotion = HelperForPromotions.addDiscountPromo(promotion, false, null, null,
                new ArrayList<>(ImmutableList.of(false, false, false, false, false)), null,
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey1, basicOfferingExternalKey2,
                    basicOfferingExternalKey3, basicOfferingExternalKey4, basicOfferingExternalKey5)),
                null);
        } else {
            promotion = HelperForPromotions.addDiscountPromo(promotion, false, null, null,
                new ArrayList<>(ImmutableList.of(false)), new ArrayList<>(ImmutableList.of(false)),
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2)),
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey2)));
        }
        // Add the promotion in the admin tool
        newPromotion = addPromotionPage.addPromotion(promotion);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        AssertCollector.assertThat(
            "Apply discount flag is not set for at least one offers/offerings error is not thrown",
            addPromotionPage.getErrorMessageForField(), equalTo(PelicanErrorConstants.APPLY_DISCOUNT_FLAG_NOT_SET),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests activation success when one out of many do not have have apply discount checkbox checked
     */
    @Test(dataProvider = "offeringsOfferForApplyDiscount")
    public void testSuccessActivationWhenOneOffersOfferingsDoesNotHaveApplyDiscount(final String offers) {

        // Construct a promotion object for store-wide percent discount for bundle promotion
        promotion = HelperForPromotions.getPromotion(storeIdList, null, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
            PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, offers, PERCENTAGE_AMOUNT, startDate, endDate, null, null,
            Status.ACTIVE.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true,
            assertionErrorList);
        // Add more fields to promotion object
        if (offers.equals(PelicanConstants.SUBSCRIPTION_OFFERS_TYPE)) {
            promotion = HelperForPromotions.addDiscountPromo(promotion, false, null, null, null,
                new ArrayList<>(ImmutableList.of(false, true)), null,
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        } else if (offers.equals(PelicanConstants.BASIC_OFFERING_TYPE)) {
            promotion = HelperForPromotions.addDiscountPromo(promotion, false, null, null,
                new ArrayList<>(ImmutableList.of(true, false)), null,
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2, basicOfferingExternalKey3)), null);
        } else {
            promotion = HelperForPromotions.addDiscountPromo(promotion, false, null, null,
                new ArrayList<>(ImmutableList.of(false, false)), null,
                new ArrayList<>(ImmutableList.of(basicOfferingExternalKey2, basicOfferingExternalKey3)),
                new ArrayList<>(ImmutableList.of(subscriptionOfferExternalKey1, subscriptionOfferExternalKey2)));
        }
        // Add the promotion in the admin tool
        newPromotion = addPromotionPage.addPromotion(promotion);
        promotionName = newPromotion.getName();

        HelperForPromotions.validateFewCommonAssertions(newPromotion, promotionName, getStoreExternalKeyUs(),
            assertionErrorList);
        HelperForPromotions.validateAllCommonAssertions(newPromotion, startDate, endDate, Status.ACTIVE, null, null,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "offeringsOfferForApplyDiscount")
    private Object[][] getOfferingsOffer() {
        return new Object[][] { { PelicanConstants.SUBSCRIPTION_OFFERS_TYPE }, { PelicanConstants.BASIC_OFFERING_TYPE },
                { BOTH_OFFERING_TYPE } };
    }
}
