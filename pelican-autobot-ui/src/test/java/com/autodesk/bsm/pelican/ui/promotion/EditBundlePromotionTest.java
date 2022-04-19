package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
import com.autodesk.bsm.pelican.ui.pages.promotions.EditPromotionPage;
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
 * This is a test class for editing the bundle promotion in the admin tool**
 *
 * @author yerragv
 */
public class EditBundlePromotionTest extends SeleniumWebdriver {

    private static Promotion newNonStoreWideDiscountAmountPromoForSubscriptionOffering;
    private static Promotion newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering;
    private static Promotion newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering;
    private static Promotion newStoreWidePercentPromoForSubscriptionOffering;
    private static Promotion newStoreWidePercentPromoForBasicOffering;
    private static Promotion newNonStoreWidePercentPromoForSubscriptionOffering;
    private static final String promotionName = "Test";
    private AddPromotionPage addPromotionPage;
    private EditPromotionPage editPromotionPage;
    private List<String> storeIdList;
    private static final String DEFAULT_BILLING_CYCLES = "1";
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
    private static final String EXPECTED_PERCENTAGE_DISCOUNT = "Percentage Discount";
    private static final String EXPECTED_PERCENTAGE_AMOUNT = "15%";
    private static final Integer BASIC_OFFERING1_QUANTITY = 3;
    private static final Integer BASIC_OFFERING2_QUANTITY = 6;
    private static final Integer EDITED_BASIC_OFFERING1_QUANTITY = 10;
    private static final Integer EDITED_BASIC_OFFERING2_QUANTITY = 20;
    private static final Integer INVALID_OFFERING1_QUANTITY = -2;
    private static final Integer INVALID_OFFERING2_QUANTITY = 1002;
    private static final Integer SUBSCRIPTION_OFFERING1_QUANTITY = 9;
    private static final Integer SUBSCRIPTION_OFFERING2_QUANTITY = 12;
    private static final Integer EDITED_SUBSCRIPTION_OFFERING1_QUANTITY = 30;
    private static final Integer EDITED_SUBSCRIPTION_OFFERING2_QUANTITY = 40;
    private String startDate;
    private String endDate;
    private String basicOfferingExternalKey1;
    private String basicOfferingExternalKey2;
    private List<String> basicOfferingExternalKeyList;
    private List<String> subscriptionOfferingExternalKeyList;
    private List<Integer> quantityToBasicOfferingList;
    private List<Integer> quantityToSubscriptionOfferingList;
    private List<Boolean> applyDiscountForBasicOfferingList;
    private List<Boolean> applyDiscountForSubscriptionOfferingList;
    private PelicanPlatform resource;
    private static String subscriptionOfferingExternalKey1;
    private static String subscriptionOfferingExternalKey2;
    private static final List<String> expectedBasicOfferingExternalKey = new LinkedList<>();
    private static final List<String> expectedSubscriptionOfferingExternalKey = new LinkedList<>();
    private BasicOfferingApiUtils basicOfferingApiUtils;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private Promotion newPromotion;
    private String promoName;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());

        // Create two basic offerings
        basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering1 = resource.offerings().getOfferingById(basicOffering1.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey1 = basicOffering1.getOfferings().get(0).getExternalKey();
        Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering2 = resource.offerings().getOfferingById(basicOffering2.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey2 = basicOffering2.getOfferings().get(0).getExternalKey();
        expectedBasicOfferingExternalKey.add(basicOfferingExternalKey1);
        expectedBasicOfferingExternalKey.add(basicOfferingExternalKey2);

        // create two bic offerings
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan1 =
            resource.subscriptionPlan().getById(subscriptionOffering1.getOfferings().get(0).getId(), null);
        subscriptionOfferingExternalKey1 =
            subscriptionPlan1.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan2 =
            resource.subscriptionPlan().getById(subscriptionOffering2.getOfferings().get(0).getId(), null);
        subscriptionOfferingExternalKey2 =
            subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();
        expectedSubscriptionOfferingExternalKey.add(subscriptionOfferingExternalKey1);
        expectedSubscriptionOfferingExternalKey.add(subscriptionOfferingExternalKey2);

        // login into Admin Tool
        adminToolPage.login();

        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        editPromotionPage = adminToolPage.getPage(EditPromotionPage.class);

        final ArrayList<String> startAndEndDayOfPromotionList = DateTimeUtils.getStartDateOfTodayAndEndDateInNextYear();
        startDate = startAndEndDayOfPromotionList.get(0);
        endDate = startAndEndDayOfPromotionList.get(1);
        storeIdList = new ArrayList<>();
        storeIdList.add(getStoreIdUs());
    }

    /**
     * Verify edit Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with subscription offering to a
     * non bundled promo with basic offerings
     */
    @Test
    public void testEditStoreWidePercentageDiscountBundlePromotionWithSubscriptionOffering() {

        newStoreWidePercentPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newStoreWidePercentPromoForSubscriptionOffering =
            addDiscountPromo(newStoreWidePercentPromoForSubscriptionOffering, true, null, null, null, null);
        addPromotionPage.addPromotion(newStoreWidePercentPromoForSubscriptionOffering);

        addPromotionPage.edit();

        newStoreWidePercentPromoForBasicOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, false);
        newStoreWidePercentPromoForBasicOffering =
            addDiscountPromo(newStoreWidePercentPromoForBasicOffering, false, null, null, null, null);
        newPromotion = editPromotionPage.editBundlePromotion(newStoreWidePercentPromoForBasicOffering);

        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, edit Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with subscription offering to
     * a basic offering
     */
    @Test
    public void testEditNonStoreWideDiscountBundlePromotionWithSubscriptionOffering() {

        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, CASH_AMOUNT, startDate,
                endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountAmountPromoForSubscriptionOffering, false, null, null, null, null);
        addPromotionPage.addPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);
        addPromotionPage.edit();
        Promotion newNonStoreWidePercentPromoForBasicOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, false);
        newNonStoreWidePercentPromoForBasicOffering =
            addDiscountPromo(newNonStoreWidePercentPromoForBasicOffering, false, null, null, null, null);
        newPromotion = editPromotionPage.editBundlePromotion(newNonStoreWidePercentPromoForBasicOffering);
        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify edit Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with basic offering to a
     * subscription offering
     */
    @Test
    public void testEditStoreWidePercentageDiscountBundlePromotionWithBasicOffering() {

        newStoreWidePercentPromoForBasicOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newStoreWidePercentPromoForBasicOffering =
            addDiscountPromo(newStoreWidePercentPromoForBasicOffering, true, null, null, null, null);
        addPromotionPage.addPromotion(newStoreWidePercentPromoForBasicOffering);

        addPromotionPage.edit();

        newStoreWidePercentPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, false);
        newStoreWidePercentPromoForSubscriptionOffering =
            addDiscountPromo(newStoreWidePercentPromoForSubscriptionOffering, false, null, null, null, null);
        newPromotion = editPromotionPage.editBundlePromotion(newStoreWidePercentPromoForSubscriptionOffering);
        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, edit Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with basic offering to
     * subscription offering
     */
    @Test
    public void testEditNonStoreWideDiscountBundlePromotionWithBasicOffering() {

        newNonStoreWideDiscountAmountPromoForSubscriptionOffering = getPromotion(storeIdList, DEFAULT_BILLING_CYCLES,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            PelicanConstants.BASIC_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountAmountPromoForSubscriptionOffering, false, null, null, null, null);
        addPromotionPage.addPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWidePercentPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, false);
        newNonStoreWidePercentPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWidePercentPromoForSubscriptionOffering, false, null, null, null, null);
        newPromotion = editPromotionPage.editBundlePromotion(newNonStoreWidePercentPromoForSubscriptionOffering);
        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify edit Bundle Promotion for a StoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering to only basic offering
     */
    @Test
    public void testEditStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOfferingToBasicOffering_DEFECT_BIC6031() {

        Promotion newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.PERCENTAGE_DISCOUNT_TYPE,
            BOTH_OFFERING_TYPE, PERCENTAGE_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = addDiscountPromo(
            newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, true, null, null, null, null);
        addPromotionPage.addPromotion(newStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newStoreWidePercentPromoForBasicOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.BASIC_OFFERING_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, false);
        newStoreWidePercentPromoForBasicOffering =
            addDiscountPromo(newStoreWidePercentPromoForBasicOffering, false, null, null, null, null);
        newPromotion = editPromotionPage.editBundlePromotion(newStoreWidePercentPromoForBasicOffering);

        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedBasicOfferingExternalKey)
                && expectedBasicOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify edit Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering to only subscription offering
     */
    @Test
    public void testEditNonStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOfferingToSubscriptionOffering() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = addDiscountPromo(
            newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false, null, null, null, null);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWidePercentPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.PERCENTAGE_DISCOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, PERCENTAGE_AMOUNT,
                startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, false);
        newNonStoreWidePercentPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWidePercentPromoForSubscriptionOffering, false, null, null, null, null);
        newPromotion = editPromotionPage.editBundlePromotion(newNonStoreWidePercentPromoForSubscriptionOffering);
        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(EXPECTED_PERCENTAGE_DISCOUNT), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferingExternalKey)
                && expectedSubscriptionOfferingExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(EXPECTED_PERCENTAGE_AMOUNT), assertionErrorList);
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify edit Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with both basic offering and
     * subscription offering with valid quantity
     */
    @Test
    public void testEditNonStoreWidePercentageDiscountBundlePromotionWithBothBasicOfferingAndSubscriptionOfferingWithValidQuantity() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getEditedQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getEditedQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getEditedApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getEditedApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        newPromotion =
            editPromotionPage.editBundlePromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);
        promoName = newPromotion.getName();
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(PelicanConstants.DISCOUNT_PROMOTION_TYPE), assertionErrorList);
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
        validateFewCommonAssertions(newPromotion, promoName, getStoreExternalKeyUs());
        validateAllCommonAssertions(newPromotion);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify edit Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with invalid quantity for basic
     * offering
     */
    @Test
    public void testEditNonStoreWidePercentageDiscountBundlePromotionWithInvalidQuantityForBasicOffering() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getInvalidQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForBasicOfferingInvalidQuantity("Invalid-Quantity");
        newPromotion =
            editPromotionPage.editBundlePromotion(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering);
        AssertCollector.assertThat("Invalid error type", newPromotion.getErrorMessageForBasicOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify edit Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with invalid quantity for
     * subscription offering
     */
    @Test
    public void testEditNonStoreWidePercentageDiscountBundlePromotionWithInvalidQuantityForSubscriptionOffering() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getInvalidQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForSubscriptionOfferingInvalidQuantity("Invalid-Quantity");
        newPromotion =
            editPromotionPage.editBundlePromotion(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering);
        AssertCollector.assertThat("Invalid error type",
            newPromotion.getErrorMessageForSubscriptionOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify edit Bundle Promotion for a NonStoreWide Cash Amount Discount Bundle Promo with invalid quantity for both
     * basic offering and subscription offering
     */
    @Test
    public void testEditNonStoreWidePercentageDiscountBundlePromotionWithInvalidQuantityForBothBasicOfferingAndSubscriptionOffering() {

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getInvalidQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getInvalidQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForBasicOfferingInvalidQuantity("Invalid-Quantity");
        newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering
            .setErrorMessageForSubscriptionOfferingInvalidQuantity("Invalid-Quantity");
        newPromotion =
            editPromotionPage.editBundlePromotion(newNonStoreWideDiscountPromoWithInvalidQuantityForBasicOffering);
        AssertCollector.assertThat("Invalid error type", newPromotion.getErrorMessageForBasicOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Invalid error type",
            newPromotion.getErrorMessageForSubscriptionOfferingInvalidQuantity(),
            equalTo(PelicanErrorConstants.EXPECTED_OFFERING_INVALID_QUANTITY_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, edit Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with more than 5 basic
     * offerings
     */
    @Test
    public void testEditNonStoreWideDiscountBundlePromotionWithMoreThanFiveBasicOffering() {

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

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        Promotion newNonStoreWideDiscountAmountPromoForBasicOffering = getPromotion(storeIdList, DEFAULT_BILLING_CYCLES,
            PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            PelicanConstants.BASIC_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountAmountPromoForBasicOffering =
            addDiscountPromo(newNonStoreWideDiscountAmountPromoForBasicOffering, false, null, null, null, null);
        newNonStoreWideDiscountAmountPromoForBasicOffering = addMoreBasicOfferingsToPromotion(
            newNonStoreWideDiscountAmountPromoForBasicOffering, basicOfferingExternalKey1, basicOfferingExternalKey2,
            basicOfferingExternalKey3, basicOfferingExternalKey4, basicOfferingExternalKey5, basicOfferingExternalKey6);
        newNonStoreWideDiscountAmountPromoForBasicOffering.setErrorMessageForMoreBasicOfferings("more-offerings");
        newPromotion = editPromotionPage.editBundlePromotion(newNonStoreWideDiscountAmountPromoForBasicOffering);
        AssertCollector.assertThat("Incorrect error message for more than 5 basic offerings",
            newPromotion.getErrorMessageForMoreBasicOfferings(),
            equalTo(PelicanErrorConstants.EXPECTED_MORE_OFFERING_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, edit Bundle Promotion for a Non Storewide Percentage Discount Bundle Promo with more than 5 subscription
     * offerings
     */
    @Test
    public void testEditNonStoreWideDiscountBundlePromotionWithMoreThanFiveSubscriptionOffering() {

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

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering = getPromotion(storeIdList,
            DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE, PelicanConstants.CASH_AMOUNT_TYPE,
            BOTH_OFFERING_TYPE, CASH_AMOUNT, startDate, endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER,
            Status.NEW.toString(), START_HOUR, START_MINUTE, START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);

        quantityToBasicOfferingList = getQuantityListForBasicOffering();
        quantityToSubscriptionOfferingList = getQuantityListForSubscriptionOffering();
        applyDiscountForBasicOfferingList = getApplyDiscountListForBasicOffering();
        applyDiscountForSubscriptionOfferingList = getApplyDiscountListForSubscriptionOffering();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering, false,
                quantityToBasicOfferingList, quantityToSubscriptionOfferingList, applyDiscountForBasicOfferingList,
                applyDiscountForSubscriptionOfferingList);
        addPromotionPage.addPromotion(newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering);

        addPromotionPage.edit();

        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            getPromotion(storeIdList, DEFAULT_BILLING_CYCLES, PelicanConstants.DISCOUNT_PROMOTION_TYPE,
                PelicanConstants.CASH_AMOUNT_TYPE, PelicanConstants.SUBSCRIPTION_OFFERS_TYPE, CASH_AMOUNT, startDate,
                endDate, MAXIMUM_USES, MAXIMUM_USES_BY_USER, Status.NEW.toString(), START_HOUR, START_MINUTE,
                START_SECOND, END_HOUR, END_MINUTE, END_SECOND, true);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            addDiscountPromo(newNonStoreWideDiscountAmountPromoForSubscriptionOffering, false, null, null, null, null);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering =
            addMoreSubscriptionOfferingsToPromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering,
                subscriptionOfferingExternalKey1, subscriptionOfferingExternalKey2, subscriptionOfferingExternalKey3,
                subscriptionOfferingExternalKey4, subscriptionOfferingExternalKey5, subscriptionOfferingExternalKey6);
        newNonStoreWideDiscountAmountPromoForSubscriptionOffering
            .setErrorMessageForMoreSubscriptionOfferings("more-offerings");
        newPromotion = editPromotionPage.editBundlePromotion(newNonStoreWideDiscountAmountPromoForSubscriptionOffering);
        AssertCollector.assertThat("Incorrect error message for more than 5 subscription offerings",
            newPromotion.getErrorMessageForMoreSubscriptionOfferings(),
            equalTo(PelicanErrorConstants.EXPECTED_MORE_OFFERING_ERROR_MESSAGE), assertionErrorList);
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

        basicOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList = new ArrayList<>();
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

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * @param newPromotion
     */
    private void validateFewCommonAssertions(final Promotion newPromotion, final String name,
        final String expectedStore) {
        AssertCollector.assertThat("Incorrect promotion name", newPromotion.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store Id", newPromotion.getStoreId(), equalTo(expectedStore),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promo code", newPromotion.getPromotionCode(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion code length", newPromotion.getPromotionCode().length(),
            equalTo(PelicanConstants.PROMOTION_CODE_LENGTH), assertionErrorList);
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
     * This is the method to return the quantity for basic offering
     *
     * @return List<Integer>
     */
    private List<Integer> getEditedQuantityListForBasicOffering() {

        final List<Integer> quantityToBasicOfferingList = new ArrayList<>();
        quantityToBasicOfferingList.add(EDITED_BASIC_OFFERING1_QUANTITY);
        quantityToBasicOfferingList.add(EDITED_BASIC_OFFERING2_QUANTITY);

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
     * This is the method to return the quantity for subscription offering
     *
     * @return List<Integer>
     */
    private List<Integer> getEditedQuantityListForSubscriptionOffering() {

        final List<Integer> quantityToSubscriptionOfferingList = new ArrayList<>();
        quantityToSubscriptionOfferingList.add(EDITED_SUBSCRIPTION_OFFERING1_QUANTITY);
        quantityToSubscriptionOfferingList.add(EDITED_SUBSCRIPTION_OFFERING2_QUANTITY);

        return quantityToSubscriptionOfferingList;
    }

    /**
     * This is the method to return the invalid quantity for basic offering
     *
     * @return List<Integer>
     */
    private List<Integer> getInvalidQuantityListForBasicOffering() {

        final List<Integer> quantityToBasicOfferingList = new ArrayList<>();
        quantityToBasicOfferingList.add(INVALID_OFFERING1_QUANTITY);
        quantityToBasicOfferingList.add(INVALID_OFFERING2_QUANTITY);

        return quantityToBasicOfferingList;
    }

    /**
     * This is the method to return the invalid quantity for subscription offering
     *
     * @return List<Integer>
     */
    private List<Integer> getInvalidQuantityListForSubscriptionOffering() {

        final List<Integer> quantityToBasicOfferingList = new ArrayList<>();
        quantityToBasicOfferingList.add(INVALID_OFFERING1_QUANTITY);
        quantityToBasicOfferingList.add(INVALID_OFFERING2_QUANTITY);

        return quantityToBasicOfferingList;
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
     * This is the method to return the applyDiscountValues for basic offerings
     *
     * @return List<Boolean>
     */
    private List<Boolean> getEditedApplyDiscountListForBasicOffering() {

        final List<Boolean> applyDiscountForBasicOfferingList = new ArrayList<>();
        applyDiscountForBasicOfferingList.add(PelicanConstants.TRUE_VALUE);
        applyDiscountForBasicOfferingList.add(PelicanConstants.FALSE_VALUE);

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

    /**
     * This is the method to return the applyDiscountValues for subscription offerings
     *
     * @return List<Boolean>
     */
    private List<Boolean> getEditedApplyDiscountListForSubscriptionOffering() {

        final List<Boolean> applyDiscountForSubscriptionOfferingList = new ArrayList<>();
        applyDiscountForSubscriptionOfferingList.add(PelicanConstants.TRUE_VALUE);
        applyDiscountForSubscriptionOfferingList.add(PelicanConstants.FALSE_VALUE);

        return applyDiscountForSubscriptionOfferingList;
    }

    /**
     * This is a method to add more offerings to a promotion
     *
     * @return Promotion
     */
    private Promotion addMoreBasicOfferingsToPromotion(final Promotion promotion, final String offeringExternalKey1,
        final String offeringExternalKey2, final String offeringExternalKey3, final String offeringExternalKey4,
        final String offeringExternalKey5, final String offeringExternalKey6) {
        basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(offeringExternalKey1);
        basicOfferingExternalKeyList.add(offeringExternalKey2);
        basicOfferingExternalKeyList.add(offeringExternalKey3);
        basicOfferingExternalKeyList.add(offeringExternalKey4);
        basicOfferingExternalKeyList.add(offeringExternalKey5);
        basicOfferingExternalKeyList.add(offeringExternalKey6);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);

        return promotion;
    }

    /**
     * This is a method to add more offerings to a promotion
     *
     * @return Promotion
     */
    private Promotion addMoreSubscriptionOfferingsToPromotion(final Promotion promotion,
        final String offeringExternalKey1, final String offeringExternalKey2, final String offeringExternalKey3,
        final String offeringExternalKey4, final String offeringExternalKey5, final String offeringExternalKey6) {
        subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(offeringExternalKey1);
        subscriptionOfferingExternalKeyList.add(offeringExternalKey2);
        subscriptionOfferingExternalKeyList.add(offeringExternalKey3);
        subscriptionOfferingExternalKeyList.add(offeringExternalKey4);
        subscriptionOfferingExternalKeyList.add(offeringExternalKey5);
        subscriptionOfferingExternalKeyList.add(offeringExternalKey6);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);

        return promotion;
    }
}
