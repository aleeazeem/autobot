package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
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
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/***
 * This is a test
 *
 * class to clone a bundle promotion in the admin tool**
 *
 * @author yerragv
 */
public class CloneBundlePromotionTest extends SeleniumWebdriver {

    private static Promotion newNonStoreWideDiscountAmountPromoForSubscriptionOffering;
    private static Promotion newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering;
    private static final String promotionName = "Test";
    private AddPromotionPage addPromotionPage;
    private PromotionDetailsPage promotionDetailsPage;
    private List<String> storeIdList;
    private static final String DEFAULT_BILLING_CYCLES = "1";
    private static final String DISCOUNT_PROMOTION_TYPE = "Discount";
    private static final String BOTH_OFFERING_TYPE = "Both";
    private static final String CASH_AMOUNT = "100.21";
    private static final String PERCENTAGE_AMOUNT = "15";
    private static final String MAXIMUM_USES = "200";
    private static final String MAXIMUM_USES_BY_USER = "200";
    private static final String START_HOUR = "03";
    private static final String START_MINUTE = "08";
    private static final String START_SECOND = "56";
    private static final String END_HOUR = "09";
    private static final String END_MINUTE = "28";
    private static final String END_SECOND = "26";
    private static final int PROMOTION_CODE_LENGTH = 13;
    private static final String EXPECTED_PERCENTAGE_DISCOUNT = "Percentage Discount";
    private static final String EXPECTED_PERCENTAGE_AMOUNT = "15%";
    private static final Integer BASIC_OFFERING1_QUANTITY = 3;
    private static final Integer BASIC_OFFERING2_QUANTITY = 6;
    private static final Integer SUBSCRIPTION_OFFERING1_QUANTITY = 9;
    private static final Integer SUBSCRIPTION_OFFERING2_QUANTITY = 12;
    private String startDate;
    private String endDate;
    private String basicOfferingExternalKey1;
    private String basicOfferingExternalKey2;
    private static String subscriptionOfferingExternalKey1;
    private static String subscriptionOfferingExternalKey2;
    private static final List<String> expectedBasicOfferingExternalKey = new LinkedList<>();
    private static final List<String> expectedSubscriptionOfferingExternalKey = new LinkedList<>();

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        final PelicanPlatform resource =
            new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());

        // Create two basic offerings
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getUserExternalKey(), OfferingType.PERPETUAL,
            MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering1 = resource.offerings().getOfferingById(basicOffering1.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey1 = basicOffering1.getOfferings().get(0).getExternalKey();
        Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getUserExternalKey(), OfferingType.PERPETUAL,
            MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering2 = resource.offerings().getOfferingById(basicOffering2.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey2 = basicOffering2.getOfferings().get(0).getExternalKey();
        expectedBasicOfferingExternalKey.add(basicOfferingExternalKey1);
        expectedBasicOfferingExternalKey.add(basicOfferingExternalKey2);

        // create two bic offerings
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(getUserExternalKey(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan1 =
            resource.subscriptionPlan().getById(subscriptionOffering1.getOfferings().get(0).getId(), null);
        subscriptionOfferingExternalKey1 =
            subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getUserExternalKey(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan2 =
            resource.subscriptionPlan().getById(subscriptionOffering2.getOfferings().get(0).getId(), null);
        subscriptionOfferingExternalKey2 =
            subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        expectedSubscriptionOfferingExternalKey.add(subscriptionOfferingExternalKey1);
        expectedSubscriptionOfferingExternalKey.add(subscriptionOfferingExternalKey2);

        // login into Admin Tool
        adminToolPage.login();

        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);

        final ArrayList<String> startAndEndDayOfPromotionList = DateTimeUtils.getStartDateOfTodayAndEndDateInNextYear();
        startDate = startAndEndDayOfPromotionList.get(0);
        endDate = startAndEndDayOfPromotionList.get(1);
        storeIdList = new ArrayList<>();
        storeIdList.add(getStoreIdUs());
    }

    /**
     * Verify clone Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with subscription offering
     */
    @Test
    public void testCloneStoreWidePercentageDiscountBundlePromotionWithSubscriptionOffering() {

        Promotion newStoreWidePercentPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newStoreWidePercentPromoForSubscriptionOffering =
            addDiscountPromo(newStoreWidePercentPromoForSubscriptionOffering, true, null, null, null, null);
        addPromotionPage.addPromotion(newStoreWidePercentPromoForSubscriptionOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion =
            addPromotionPage.setAllFieldsOfPromotion(newStoreWidePercentPromoForSubscriptionOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, clone Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with subscription offering
     */
    @Test
    public void testCloneNonStoreWideDiscountBundlePromotionWithSubscriptionOffering() {

        newNonStoreWideDiscountAmountPromoForSubscriptionOffering = getPromotion(storeIdList, DEFAULT_BILLING_CYCLES,
            DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE,
            CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR,
            START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountAmountPromoForSubscriptionOffering, false, null, null, null, null);
        addPromotionPage.addPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion =
            addPromotionPage.setAllFieldsOfPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(CASH_AMOUNT),
            assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with basic offering
     */
    @Test
    public void testCloneStoreWidePercentageDiscountBundlePromotionWithBasicOffering() {

        Promotion newStoreWidePercentPromoForBasicOffering = getPromotion(storeIdList, DEFAULT_BILLING_CYCLES,
            DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE,
            PERCENTAGE_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(),
            START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newStoreWidePercentPromoForBasicOffering =
            addDiscountPromo(newStoreWidePercentPromoForBasicOffering, true, null, null, null, null);
        addPromotionPage.addPromotion(newStoreWidePercentPromoForBasicOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion =
            addPromotionPage.setAllFieldsOfPromotion(newStoreWidePercentPromoForBasicOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Add Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with basic offering
     */
    @Test
    public void testCloneNonStoreWideDiscountBundlePromotionWithBasicOffering() {

        newNonStoreWideDiscountAmountPromoForSubscriptionOffering = getPromotion(storeIdList, DEFAULT_BILLING_CYCLES,
            DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE,
            CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR,
            START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountAmountPromoForSubscriptionOffering, false, null, null, null, null);
        addPromotionPage.addPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion =
            addPromotionPage.setAllFieldsOfPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(CASH_AMOUNT),
            assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering
     */
    @Test
    public void testCloneStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOffering_DEFECT_BIC6032() {

        Promotion newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE,
            BOTH_OFFERING_TYPE, PERCENTAGE_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = addDiscountPromo(
            newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, true, null, null, null, null);
        addPromotionPage.addPromotion(newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion =
            addPromotionPage.setAllFieldsOfPromotion(newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Add Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering
     */
    @Test
    public void testCloneNonStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOffering_DEFECT_BIC6032() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE, BOTH_OFFERING_TYPE,
            CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR,
            START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = addDiscountPromo(
            newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false, null, null, null, null);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion = addPromotionPage
            .setAllFieldsOfPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(CASH_AMOUNT),
            assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify Add Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering
     */
    @Test
    public void testCloneNonStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOfferingWithValidQuantity_DEFECT_BIC6032() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE, BOTH_OFFERING_TYPE,
            CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR,
            START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        final List<Integer> quantityToBasicOfferingList = getQuantityListForBasicOffering();
        final List<Integer> quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        final List<Boolean> applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        final List<Boolean> applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        addPromotionPage = promotionDetailsPage.clickOnCloneButton();
        addPromotionPage.clickOnAddPromotion();
        final Promotion newPromotion = addPromotionPage
            .setAllFieldsOfPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        final String promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(PelicanConstants.EXPECTED_CASH_TYPE), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(CASH_AMOUNT),
            assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName);
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is the method to construct a promotion object
     *
     * @return Promotion Object
     */
    private Promotion getPromotion(final List<String> storeIdList, final String numberOfBillingCycles,
        final String discountPromotionType, final String discountType, final String offeringType,
        final String discountValue, final String startDate, final String endDate, final String maximumUses,
        final String maximumUsesByUser, final String activateStatus, final String startHourForPromotion,
        final String startMinuteForPromotion, final String startSecondForPromotion, final String endHourForPromotion,
        final String endMinuteForPromotion, final String endSecondForPromotion, final boolean bundled) {

        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final String name = promotionName + (int) (Math.random() * 10);

        return HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId, discountPromotionType,
            discountType, offeringType, discountValue, startDate, endDate, maximumUses, maximumUsesByUser,
            activateStatus, startHourForPromotion, startMinuteForPromotion, startSecondForPromotion,
            endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, numberOfBillingCycles, bundled,
            assertionErrorList);

    }

    /**
     * This is to add some more fields to the promotion object
     *
     * @param promotion
     * @param storeWide
     * @param quantityForBasicOfferingList
     * @param quantityForSubscriptionOfferingList
     * @param applyDiscountForBasicOfferingList
     * @param applyDiscountForSubscriptionOfferingList
     * @return
     */
    private Promotion addDiscountPromo(final Promotion promotion, final boolean storeWide,
        final List<Integer> quantityForBasicOfferingList, final List<Integer> quantityForSubscriptionOfferingList,
        final List<Boolean> applyDiscountForBasicOfferingList,
        final List<Boolean> applyDiscountForSubscriptionOfferingList) {

        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWide(storeWide);
        if (!promotion.isBundled()
            && (PelicanConstants.BASIC_OFFERING_TYPE).equalsIgnoreCase(promotion.getBasicOfferings())) {
            basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
            basicOfferingExternalKeyList.add(basicOfferingExternalKey2);
            promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        } else if (!promotion.isBundled()
            && (PelicanConstants.SUBSCRIPTION_OFFERS_TYPE).equalsIgnoreCase(promotion.getSubscriptionOfferings())) {
            subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
            subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey2);
            promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        } else {
            basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
            basicOfferingExternalKeyList.add(basicOfferingExternalKey2);
            promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
            promotion.setQuantityOfBasicOfferingsList(quantityForBasicOfferingList);
            promotion.setApplyDiscountForBasicOfferingsList(applyDiscountForBasicOfferingList);
            subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
            subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey2);
            promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
            promotion.setQuantityOfSubscriptionOfferingsList(quantityForSubscriptionOfferingList);
            promotion.setApplyDiscountForSubscriptionOfferingsList(applyDiscountForSubscriptionOfferingList);
        }

        return promotion;
    }

    /**
     * @param newPromotion
     */
    private void validateAllCommonAssertions(final Promotion newPromotion) {
        AssertCollector.assertThat("Incorrect promotion max uses", newPromotion.getMaxUses(), equalTo(MAXIMUM_USES),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion max uses per user", newPromotion.getMaxUsesPerUser(),
            equalTo(MAXIMUM_USES_BY_USER), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion start date", newPromotion.getEffectiveDate().substring(5, 15),
            equalTo(startDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion expiration date",
            newPromotion.getEffectiveDate().substring(32, 42), equalTo(endDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start hours", newPromotion.getEffectiveDate().substring(16, 18),
            equalTo(START_HOUR), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start minutes",
            newPromotion.getEffectiveDate().substring(19, 21), equalTo(START_MINUTE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start second",
            newPromotion.getEffectiveDate().substring(22, 24), equalTo(START_SECOND), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration hours",
            newPromotion.getEffectiveDate().substring(43, 45), equalTo(END_HOUR), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration minutes",
            newPromotion.getEffectiveDate().substring(46, 48), equalTo(END_MINUTE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration seconds",
            newPromotion.getEffectiveDate().substring(49, 51), equalTo(END_SECOND), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion state", newPromotion.getState(), equalTo(Status.NEW.toString()),
            assertionErrorList);
    }

    /**
     * @param newPromotion
     */
    private void validateFewCommonAssertions(final Promotion newPromotion, final String name) {
        AssertCollector.assertThat("Incorrect promotion name", newPromotion.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store Id", newPromotion.getStoreId(), equalTo(getStoreExternalKeyUs()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promo code", newPromotion.getPromotionCode(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion code length", newPromotion.getPromotionCode().length(),
            equalTo(PROMOTION_CODE_LENGTH), assertionErrorList);
    }

    /**
     * This is the method to return the quantity for basic offering
     *
     * @return List<Integer>
     */
    private List<Integer> getQuantityListForBasicOffering() {

        final List<Integer> quantityToBasicOfferingList = new ArrayList<>();
        quantityToBasicOfferingList.add(BASIC_OFFERING1_QUANTITY);
        quantityToBasicOfferingList.add(BASIC_OFFERING2_QUANTITY);

        return quantityToBasicOfferingList;
    }

    /**
     * This is the method to return the quantity for subscription offering
     *
     * @return List<Integer>
     */
    private List<Integer> getQuantityListForSubscriptionOffering() {

        final List<Integer> quantityToSubscriptionOfferingList = new ArrayList<>();
        quantityToSubscriptionOfferingList.add(SUBSCRIPTION_OFFERING1_QUANTITY);
        quantityToSubscriptionOfferingList.add(SUBSCRIPTION_OFFERING2_QUANTITY);

        return quantityToSubscriptionOfferingList;
    }

    /**
     * This is the method to return the applyDiscountValues for basic offerings
     *
     * @return List<Boolean>
     */
    private List<Boolean> getApplyDiscountListForBasicOffering() {

        final List<Boolean> applyDiscountForBasicOfferingList = new ArrayList<>();
        applyDiscountForBasicOfferingList.add(PelicanConstants.FALSE_VALUE);
        applyDiscountForBasicOfferingList.add(PelicanConstants.TRUE_VALUE);

        return applyDiscountForBasicOfferingList;
    }

    /**
     * This is the method to return the applyDiscountValues for subscription offerings
     *
     * @return List<Boolean>
     */
    private List<Boolean> getApplyDiscountListForSubscriptionOffering() {

        final List<Boolean> applyDiscountForSubscriptionOfferingList = new ArrayList<>();
        applyDiscountForSubscriptionOfferingList.add(PelicanConstants.FALSE_VALUE);
        applyDiscountForSubscriptionOfferingList.add(PelicanConstants.TRUE_VALUE);

        return applyDiscountForSubscriptionOfferingList;
    }

}
