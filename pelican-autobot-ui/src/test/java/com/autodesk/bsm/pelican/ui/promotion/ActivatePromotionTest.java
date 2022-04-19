package com.autodesk.bsm.pelican.ui.promotion;

import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.STORE_WIDE_PROMO_ALREADY_EXISTS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
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
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/***
 * This is a test
 *
 * class which have test methods to test activating the promotion through admin tool**
 *
 * @author Vineel
 */
public class ActivatePromotionTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private static final String expectedErrorMessage = "Name required";
    private static final String NEW_STATE = Status.NEW.toString();
    private static final String promotionName = "Test";
    private static final boolean defaultStoreWideStatus = false;
    private static final boolean storeWideStatus = true;
    private static final String discountPromotionType = "Discount";
    private static final String discountSupplementType = "Supplement";
    private static final String cashDiscountType = "Cash Amount";
    private static final String expectedCashType = "Cash Discount";
    private static final String expectedPercentageDiscount = "Percentage Discount";
    private static final String percentageDiscountType = "Percentage";
    private static final String supplementDiscountType = "Time";
    private static final String basicOfferingsType = "Basic Offerings";
    private static final String subscriptionOffersType = "Subscription Offers";
    private static final String activateStatus = Status.ACTIVE.toString();
    private static final String cashAmount = "100.21";
    private static final String percentageAmount = "15";
    private static final String timePeriodCount = "12";
    private static final String expectedTimePeriodType = "WEEK";
    private static final String expectedPercentageAmount = "15%";
    private static final String maximumUses = "200";
    private static final String maximumUsesByUser = "200";
    private static final int promotioncodeLength = 13;
    private static final String startHourForPromotion = "03";
    private static final String startMinuteForPromotion = "08";
    private static final String startSecondForPromotion = "56";
    private static final String endHourForPromotion = "09";
    private static final String endMinuteForPromotion = "28";
    private static final String endSecondForPromotion = "26";
    private static final String defaultNumberOfBillingCycles = "1";
    private static String startDate;
    private static String endDate;
    private static String futureStartDate;
    private static String futureEndDate;
    private static final List<String> expectedbasicOfferings = new LinkedList<>();
    private static final List<String> expectedSubscriptionOfferings = new LinkedList<>();
    private static List<String> finalOfferingExternalKey = new LinkedList<>();
    private static final List<String> finalSubscriptionOfferExternalKey = new LinkedList<>();
    private List<String> storeIdList;
    private static List<String> storeExternalKeyList;
    private String basicOfferingExternalKey1;
    private String subscriptionOfferingExternalKey1;
    private List<String> multipleBasicOfferingsExternalKeyList;
    private List<String> multipleSubscriptionOfferingsExternalKeyList;

    /*
     * Driver setup
     */
    @BeforeClass(alwaysRun = true)
    private void setUp() {

        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering1 = resource.offerings().getOfferingById(basicOffering1.getOfferings().get(0).getId(), null);
        basicOfferingExternalKey1 = basicOffering1.getOfferings().get(0).getExternalKey();
        Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering2 = resource.offerings().getOfferingById(basicOffering2.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey2 = basicOffering2.getOfferings().get(0).getExternalKey();

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
        final String subscriptionOfferingExternalKey2 =
            subscriptionPlan2.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final ArrayList<String> startAndEndDayOfPromotionList = DateTimeUtils.getStartDateOfTodayAndEndDateInNextYear();
        startDate = startAndEndDayOfPromotionList.get(0);
        endDate = startAndEndDayOfPromotionList.get(1);
        final ArrayList<String> futureStartAndEndDayList = DateTimeUtils.getFuturestartDateAndEndDate();
        futureStartDate = futureStartAndEndDayList.get(0);
        futureEndDate = futureStartAndEndDayList.get(1);
        for (final Offering offering : basicOffering1.getOfferings()) {
            expectedbasicOfferings.add(offering.getExternalKey());
        }

        for (final BillingPlan billingPlan : subscriptionOffering1.getIncluded().getBillingPlans()) {
            expectedSubscriptionOfferings.add(billingPlan.getExternalKey());
        }

        // Create second store
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        JStore secondStore = storeApiUtils.addStore(Status.ACTIVE, Country.CA, Currency.CAD, null, false);
        final String secondStoreId = secondStore.getId();
        final String secondStoreExternalKey = secondStore.getExternalKey();
        storeApiUtils.addPriceList(secondStoreId, Currency.CAD);
        secondStore = resource.stores().getStore(secondStore.getId());
        final String secondPricelistExternalKey = secondStore.getIncluded().getPriceLists().get(0).getExternalKey();

        // create third store
        JStore thirdStore = storeApiUtils.addStore(Status.ACTIVE, Country.DE, Currency.EUR, null, false);
        final String thirdStoreId = thirdStore.getId();
        final String thirdStoreExternalKey = thirdStore.getExternalKey();
        storeApiUtils.addPriceList(thirdStoreId, Currency.EUR);
        thirdStore = resource.stores().getStore(thirdStore.getId());
        final String thirdPricelistExternalKey = thirdStore.getIncluded().getPriceLists().get(0).getExternalKey();

        // Create a list of store ids
        storeIdList = new ArrayList<>();
        storeIdList.add(getStoreIdUs());
        storeIdList.add(secondStoreId);
        storeIdList.add(thirdStoreId);

        // Create a list of store external keys
        storeExternalKeyList = new ArrayList<>();
        storeExternalKeyList.add(getStoreExternalKeyUs());
        storeExternalKeyList.add(secondStoreExternalKey);
        storeExternalKeyList.add(thirdStoreExternalKey);

        // Create basic and subscription offerings for second and third stores
        Offerings basicOffering3 = basicOfferingApiUtils.addBasicOffering(secondPricelistExternalKey,
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering3 = resource.offerings().getOfferingById(basicOffering3.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey3 = basicOffering3.getOfferings().get(0).getExternalKey();

        Offerings basicOffering4 = basicOfferingApiUtils.addBasicOffering(secondPricelistExternalKey,
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering4 = resource.offerings().getOfferingById(basicOffering4.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey4 = basicOffering4.getOfferings().get(0).getExternalKey();

        final Offerings subscriptionOffering3 = subscriptionPlanApiUtils.addSubscriptionPlan(secondPricelistExternalKey,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan3 =
            resource.subscriptionPlan().getById(subscriptionOffering3.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey3 =
            subscriptionPlan3.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final Offerings subscriptionOffering4 =
            subscriptionPlanApiUtils.addSubscriptionPlan(secondPricelistExternalKey, OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan4 =
            resource.subscriptionPlan().getById(subscriptionOffering4.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey4 =
            subscriptionPlan4.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        Offerings basicOffering5 = basicOfferingApiUtils.addBasicOffering(thirdPricelistExternalKey,
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering5 = resource.offerings().getOfferingById(basicOffering5.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey5 = basicOffering5.getOfferings().get(0).getExternalKey();

        Offerings basicOffering6 = basicOfferingApiUtils.addBasicOffering(thirdPricelistExternalKey,
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering6 = resource.offerings().getOfferingById(basicOffering6.getOfferings().get(0).getId(), null);
        final String basicOfferingExternalKey6 = basicOffering6.getOfferings().get(0).getExternalKey();

        final Offerings subscriptionOffering5 =
            subscriptionPlanApiUtils.addSubscriptionPlan(thirdPricelistExternalKey, OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan5 =
            resource.subscriptionPlan().getById(subscriptionOffering5.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey5 =
            subscriptionPlan5.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        final Offerings subscriptionOffering6 =
            subscriptionPlanApiUtils.addSubscriptionPlan(thirdPricelistExternalKey, OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final SubscriptionPlan subscriptionPlan6 =
            resource.subscriptionPlan().getById(subscriptionOffering6.getOfferings().get(0).getId(), null);
        final String subscriptionOfferingExternalKey6 =
            subscriptionPlan6.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey();

        // Append all the external keys of the basic offerings
        final String secondOfferingExternalKey = basicOffering2.getOfferings().get(0).getExternalKey();
        final String thirdOfferingExternalKey = basicOffering3.getOfferings().get(0).getExternalKey();
        final String fourthOfferingExternalKey = basicOffering4.getOfferings().get(0).getExternalKey();
        final String fifthOfferingExternalKey = basicOffering5.getOfferings().get(0).getExternalKey();
        final String sixthOfferingExternalKey = basicOffering6.getOfferings().get(0).getExternalKey();
        finalOfferingExternalKey = new LinkedList<>();
        finalOfferingExternalKey.addAll(expectedbasicOfferings);
        finalOfferingExternalKey.add(secondOfferingExternalKey);
        finalOfferingExternalKey.add(thirdOfferingExternalKey);
        finalOfferingExternalKey.add(fourthOfferingExternalKey);
        finalOfferingExternalKey.add(fifthOfferingExternalKey);
        finalOfferingExternalKey.add(sixthOfferingExternalKey);

        // Append all the external keys of the subscription offers
        final String secondSubscriptionOfferExternalKey =
            subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String thirdSubscriptionOfferExternalKey =
            subscriptionOffering3.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String fourthSubscriptionOfferExternalKey =
            subscriptionOffering4.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String fifthSubscriptionOfferExternalKey =
            subscriptionOffering5.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String sixthSubscriptionOfferExternalKey =
            subscriptionOffering6.getIncluded().getBillingPlans().get(0).getExternalKey();

        finalSubscriptionOfferExternalKey.addAll(expectedSubscriptionOfferings);
        finalSubscriptionOfferExternalKey.add(secondSubscriptionOfferExternalKey);
        finalSubscriptionOfferExternalKey.add(thirdSubscriptionOfferExternalKey);
        finalSubscriptionOfferExternalKey.add(fourthSubscriptionOfferExternalKey);
        finalSubscriptionOfferExternalKey.add(fifthSubscriptionOfferExternalKey);
        finalSubscriptionOfferExternalKey.add(sixthSubscriptionOfferExternalKey);

        // Add all basic offerings external keys to a list
        multipleBasicOfferingsExternalKeyList = new ArrayList<>();
        multipleBasicOfferingsExternalKeyList.add(basicOfferingExternalKey1);
        multipleBasicOfferingsExternalKeyList.add(basicOfferingExternalKey2);
        multipleBasicOfferingsExternalKeyList.add(basicOfferingExternalKey3);
        multipleBasicOfferingsExternalKeyList.add(basicOfferingExternalKey4);
        multipleBasicOfferingsExternalKeyList.add(basicOfferingExternalKey5);
        multipleBasicOfferingsExternalKeyList.add(basicOfferingExternalKey6);

        // Add all subscription offerings external keys to a list
        multipleSubscriptionOfferingsExternalKeyList = new ArrayList<>();
        multipleSubscriptionOfferingsExternalKeyList.add(subscriptionOfferingExternalKey1);
        multipleSubscriptionOfferingsExternalKeyList.add(subscriptionOfferingExternalKey2);
        multipleSubscriptionOfferingsExternalKeyList.add(subscriptionOfferingExternalKey3);
        multipleSubscriptionOfferingsExternalKeyList.add(subscriptionOfferingExternalKey4);
        multipleSubscriptionOfferingsExternalKeyList.add(subscriptionOfferingExternalKey5);
        multipleSubscriptionOfferingsExternalKeyList.add(subscriptionOfferingExternalKey6);
    }

    /*
     * This is a test method to leave all fields of a promotion empty and add a promotion
     */
    @Test
    public void addPromotionWithEmptyFieldsInNewState() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String actualErrorMessage = addPromotionPage.getH3ErrorMessage();
        AssertCollector.assertThat("unexpected error message", actualErrorMessage, equalTo(expectedErrorMessage),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether promotion code is empty or not when you don't enter promotion code
     * when adding a promotion
     */
    @Test
    public void verifyIsPromoCodeEmpty() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = new Promotion();
        final String name = promotionName + (int) (Math.random() * 10);
        promotion.setName(name);
        promotion.setPriceListExternalKey("");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Empty PromoCode Value", newPromotion.getPromotionCode(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Empty name Value", newPromotion.getName(), equalTo(promotion.getName()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the promotion state is new or not if we don't activate the promotion
     */
    @Test
    public void verifyIsPromoStateNew() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = new Promotion();
        final String name = promotionName + (int) (Math.random() * 10);
        promotion.setName(name);
        promotion.setPriceListExternalKey("");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Promotion state", newPromotion.getState(), equalTo(NEW_STATE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the default store wide value is false or not for a promotion
     */
    @Test
    public void verifyStoreWideCheckbox() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = new Promotion();
        final String name = promotionName + (int) (Math.random() * 10);
        promotion.setName(name);
        promotion.setPriceListExternalKey("");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Promotion state", newPromotion.getStoreWide(),
            equalTo(defaultStoreWideStatus), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the store wide status of a promotion is true or not once you check the
     * storewide checkbox
     */
    @Test
    public void verifyStoreWideCheckboxIsTrue() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = new Promotion();
        final String name = promotionName + (int) (Math.random() * 10);
        promotion.setName(name);
        promotion.setStoreWide(true);
        promotion.setPriceListExternalKey("");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect storewide status", newPromotion.getStoreWide(), equalTo(storeWideStatus),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we are able to add a valid store to the promotion or not
     */
    @Test
    public void verifyAddStore() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = new Promotion();
        final String name = promotionName + (int) (Math.random() * 10);
        promotion.setName(name);
        promotion.setStoreId(getStoreIdUs());
        promotion.setPriceListExternalKey("");
        promotion.setPercentageAmount("34");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Store not tied to the promotion", newPromotion.getStoreId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests whether we are able to activate a cash amount discount promotion for a basic
     * offering
     */
    @Test
    public void activateCashDiscountWithBasicOffering() {
        adminToolPage.login();
        final String expectedStore = getStoreExternalKeyUs();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        Promotion promotion;
        final String name = promotionName + (int) (Math.random() * 10);
        promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(), discountPromotionType,
            cashDiscountType, basicOfferingsType, cashAmount, startDate, endDate, maximumUses, maximumUsesByUser,
            activateStatus, startHourForPromotion, startMinuteForPromotion, startSecondForPromotion,
            endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setAddOffering("add-offering");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedbasicOfferings)
                && expectedbasicOfferings.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        validateFewCommonAssertions(newPromotion, name, expectedStore);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test case which tests whether we are able to activate a cash discount promotion with a subscription
     * offer
     */
    @Test
    public void activateCashDiscountWithSubscriptionOffer() {
        adminToolPage.login();
        final String expectedStore = getStoreExternalKeyUs();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, cashDiscountType, subscriptionOffersType, cashAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferings)
                && expectedSubscriptionOfferings.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of billing cycles for a promotion",
            newPromotion.getNumberOfBillingCycles(), equalTo(numberOfBillingCycles), assertionErrorList);
        validateFewCommonAssertions(newPromotion, name, expectedStore);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test case which tests whether we are able to activate a cash discount promotion with limited billing
     * cycles for a subscription offer
     *
     */
    @Test
    public void activateCashDiscountWithSubscriptionOfferLimitedBillingCycles() {
        adminToolPage.login();
        final String expectedStore = getStoreExternalKeyUs();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "3";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, cashDiscountType, subscriptionOffersType, cashAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferings)
                && expectedSubscriptionOfferings.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of billing cycles for a promotion",
            newPromotion.getNumberOfBillingCycles(), equalTo(numberOfBillingCycles), assertionErrorList);
        validateFewCommonAssertions(newPromotion, name, expectedStore);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test case which tests whether we are able to activate a percentage discount for a basic offering
     */
    @Test
    public void activatePercentDiscountWithBasicOffering() {
        adminToolPage.login();
        final String expectedStore = getStoreExternalKeyUs();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedPercentageDiscount), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion basic offerings",
            newPromotion.getBasicOfferingNameList().containsAll(expectedbasicOfferings)
                && expectedbasicOfferings.containsAll(newPromotion.getBasicOfferingNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion percentage amount", newPromotion.getPercentageAmount(),
            equalTo(expectedPercentageAmount), assertionErrorList);
        validateFewCommonAssertions(newPromotion, name, expectedStore);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test case which tests whether we are able to activate a percentage discount promotion with default
     * number of billing cycles for a subscription offer
     */
    @Test
    public void activatePercentDiscountWithSubscriptionOfferWithDefaultBillingCycles() {
        adminToolPage.login();
        final String expectedStore = getStoreExternalKeyUs();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedPercentageDiscount), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferings)
                && expectedSubscriptionOfferings.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion percentage amount", newPromotion.getPercentageAmount(),
            equalTo(expectedPercentageAmount), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of billing cycles for a promotion",
            newPromotion.getNumberOfBillingCycles(), equalTo(defaultNumberOfBillingCycles), assertionErrorList);
        validateFewCommonAssertions(newPromotion, name, expectedStore);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test case which tests whether we are able to activate a supplement type promotion for subscription
     * offers
     */
    @Test
    public void activateSupplementTypePromotionWithSubscriptionOffers_DEFECT_BIC6030() {
        adminToolPage.login();
        final String expectedStore = getStoreExternalKeyUs();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountSupplementType, supplementDiscountType, subscriptionOffersType, timePeriodCount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountSupplementType), assertionErrorList);
        AssertCollector.assertThat("Incorrect Supplement type", newPromotion.getSupplementType(),
            equalTo(supplementDiscountType), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(expectedSubscriptionOfferings)
                && expectedSubscriptionOfferings.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Supplement Time", newPromotion.getTimePeriodCount(),
            equalTo(timePeriodCount), assertionErrorList);
        AssertCollector.assertThat("Incorrect Supplement Time Type", newPromotion.getTimePeriodType(),
            equalTo(expectedTimePeriodType), assertionErrorList);
        validateFewCommonAssertions(newPromotion, name, expectedStore);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message displayed when we activate a promotion for a basic offering
     * without a store
     */
    @Test
    public void activatePromotionForBasicOfferingWithoutStore() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String emptyStoreId = "";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, emptyStoreId,
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect error message for an empty store", newPromotion.getStoreErrorMessage(),
            equalTo("Invalid id:"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message when we activate a promotion for a basic offering without a
     * discount percentage
     */
    @Test
    public void activatePromotionForBasicOfferingWithoutDiscountPercentage() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String emptyPercentageAmount = "";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, emptyPercentageAmount, startDate,
            endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setDiscountErrorMessage("no-discount-entered");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect error message for an empty discount amount",
            newPromotion.getDiscountErrorMessage(), equalTo("Required"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message displayed when we activate a promotion for a basic offering
     * with a store which doesn't have a pricelist
     */
    @Test
    public void activatePromotionForBasicOfferingWithoutPricelist() {
        adminToolPage.login();
        final StoreApiUtils storeUtils = new StoreApiUtils(getEnvironmentVariables());
        final com.autodesk.bsm.pelican.api.pojos.json.Store storeWithoutPricelist =
            storeUtils.addStoreWithoutPriceListAndCountry(Status.ACTIVE);
        final String storeWithoutPricelistId = storeWithoutPricelist.getData().getId();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, storeWithoutPricelistId,
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("Emptypricelist");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect error message for a store with pricelist",
            newPromotion.getPriceListErrorMessage(), equalTo("No PriceList found for this Store"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message being displayed when we activate a promotion without a
     * subscription offer
     */
    @Test
    public void activatePromotionWithoutSubscriptionOffer() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, cashDiscountType, subscriptionOffersType, cashAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("no-offering");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect error message for no offering selected",
            newPromotion.getSubscriptionOfferErrorMessage(), equalTo("Offering(s)/Offer(s) required"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message being displayed when we activate two storewide promotions for
     * the same basic offering
     */
    @Test
    public void activateTwoStorwidePromotionsForBasicOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, null, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("two-store-wide");
        duplicatePromotion.setStoreWide(true);
        duplicatePromotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        AssertCollector.assertThat("Incorrect error message for two store wide promotions",
            newDuplicatePromotion.getStoreWideErrorMessage(), equalTo(STORE_WIDE_PROMO_ALREADY_EXISTS),
            assertionErrorList);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests whether we are able to activate two non-store wide promotions for the same
     * offering or not
     */
    @Test
    public void activateTwoNonStorewidePromotionsForBasicOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect promotion state", newPromotion.getState(), equalTo(activateStatus),
            assertionErrorList);
        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, null, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("");
        duplicatePromotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        AssertCollector.assertThat("Incorrect promotion state", newDuplicatePromotion.getState(),
            equalTo(activateStatus), assertionErrorList);
        cancelActivePromotion(newPromotion.getId());
        cancelActivePromotion(newDuplicatePromotion.getId());
        AssertCollector.assertAll(assertionErrorList);

    }

    /*
     * This is a test method which tests the error message being displayed when we activate a promotion with basic
     * offering and promotion type mismatch
     */
    @Test
    public void activatePromotionWithOfferTimeAndPromotionTypeMisMatch() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, futureStartDate,
            futureEndDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, null, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("Time-Mismatch");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        final String errorMessage = newPromotion.getTimeMismatchError();
        AssertCollector.assertThat("Incorrect error message for promotion time mismatch",
            newPromotion.getTimeMismatchError().substring(0, 12), equalTo(errorMessage.substring(0, 12)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message being displayed when we activate a promotion with end date
     * less than the start date
     */
    @Test
    public void activatePromotionWithEndDateLessThanStartDate() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, endDate, startDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("Time-Mismatch");
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        final String errorMessage = newPromotion.getTimeMismatchError();
        AssertCollector.assertThat("Incorrect error message for promotion time mismatch",
            newPromotion.getTimeMismatchError().substring(0, 12), equalTo(errorMessage.substring(0, 12)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test case which tests the error message being displayed when we activate a promotion for subscription
     * offers without any store
     */
    @Test
    public void activatePromotionForSubscriptionOfferWithoutStore() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String emptyStoreId = "";
        final String numberOfBillingCycles = "";
        final Promotion promotion =
            HelperForPromotions.assignFieldsToPromotionObject(name, emptyStoreId, null, cashDiscountType,
                subscriptionOffersType, cashAmount, startDate, endDate, maximumUses, maximumUsesByUser, activateStatus,
                startHourForPromotion, startMinuteForPromotion, startSecondForPromotion, endHourForPromotion,
                endMinuteForPromotion, endSecondForPromotion, numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("");
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect error message for an empty store", newPromotion.getStoreErrorMessage(),
            equalTo("Invalid id:"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message being displayed when we activate a promotion for basic
     * offering without a basic offering
     */
    @Test
    public void activatePromotionWithoutBasicOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, cashDiscountType, basicOfferingsType, cashAmount, startDate, endDate, maximumUses,
            maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion, startSecondForPromotion,
            endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false, assertionErrorList);
        promotion.setPriceListExternalKey("no-offering");
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect error message for no offering selected",
            newPromotion.getSubscriptionOfferErrorMessage(), equalTo("Offering(s)/Offer(s) required"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /*
     * This is a test case which tests the error message being displayed when we activate two-storwide promotions for
     * the same subscription offer
     */
    @Test
    public void activateTwoStoreWidePromotionForSubscriptionOffer() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, numberOfBillingCycles, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("two-store-wide");
        duplicatePromotion.setStoreWide(true);
        duplicatePromotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        AssertCollector.assertThat("Incorrect error message for two store wide promotions",
            newDuplicatePromotion.getStoreWideErrorMessage(), equalTo(STORE_WIDE_PROMO_ALREADY_EXISTS),
            assertionErrorList);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the activation of a non storewide promotion for the basic offering with
     * existing storewide promotion for that basic offering
     */
    @Test
    public void activateNonStoreWidePromotionForBasicOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, null, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("");
        duplicatePromotion.setStoreWide(false);
        duplicatePromotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        validateAllCommonAssertions(newDuplicatePromotion);
        cancelActivePromotion(newPromotion.getId());
        cancelActivePromotion(newDuplicatePromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the activation of a non storewide promotion for the subscription offering
     * with existing storewide promotion for that subscription offer
     */
    @Test
    public void activateNonStorWidePromotionForSubscriptionOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, numberOfBillingCycles, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("");
        duplicatePromotion.setStoreWide(false);
        duplicatePromotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        validateAllCommonAssertions(newDuplicatePromotion);
        cancelActivePromotion(newPromotion.getId());
        cancelActivePromotion(newDuplicatePromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the error message being displayed when we activate a promotion for the
     * basic offering with existing promo-code
     */
    @Test
    public void activatePromotionWithExistingPromoCodeForBasicOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(false);
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, null, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("duplicate promocode");
        duplicatePromotion.setPromotionCode(newPromotion.getPromotionCode());
        duplicatePromotion.setStoreWide(false);
        duplicatePromotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        addPromotionPage.addPromotion(duplicatePromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the error message being displayed when we activate a promotion for the
     * basic offering with existing promocode
     */
    @Test
    public void activatePromotionWithExistingPromoCodeForSubscriptionOffering() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(false);
        final List<String> subscriptionOfferingExternalKeyList = new ArrayList<>();
        subscriptionOfferingExternalKeyList.add(subscriptionOfferingExternalKey1);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            getStoreIdUs(), discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, numberOfBillingCycles, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("duplicate promocode");
        duplicatePromotion.setPromotionCode(newPromotion.getPromotionCode());
        duplicatePromotion.setStoreWide(false);
        duplicatePromotion.setSubscriptionOfferingsExternalKey(subscriptionOfferingExternalKeyList);
        addPromotionPage.addPromotion(duplicatePromotion);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate two storewide promotions for a basic offering if
     * store is different
     */
    @Test
    public void activateTwoStoreWidePromotionsWithDifferentStore() {
        adminToolPage.login();
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, getStoreIdUs(),
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
        basicOfferingExternalKeyList.add(basicOfferingExternalKey1);
        promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        Offerings basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        basicOffering = resource.offerings().getOfferingById(basicOffering.getOfferings().get(0).getId(), null);
        final String offeringExternalKey = basicOffering.getOfferings().get(0).getExternalKey();
        final String newStoreId = getStoreIdUs();
        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            newStoreId, discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, startDate,
            endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false,
            assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("");
        duplicatePromotion.setStoreWide(true);
        final List<String> offeringExternalKeyList = new ArrayList<>();
        offeringExternalKeyList.add(offeringExternalKey);
        duplicatePromotion.setBasicOfferingsExternalKey(offeringExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        validateAllCommonAssertions(newPromotion);
        cancelActivePromotion(newPromotion.getId());
        cancelActivePromotion(newDuplicatePromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate a non-storewide promotion for a basic offering with
     * multiple stores attached to the promotion
     */
    @Test
    public void activateNonStoreWidePromotionForBasicOfferingWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, cashDiscountType, basicOfferingsType, cashAmount, startDate, endDate, maximumUses,
            maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion, startSecondForPromotion,
            endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setAddOffering("add-offering");
        promotion.setOfferingMessage("all-offerings");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setBasicOfferingsExternalKey(multipleBasicOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector
            .assertTrue("Incorrect promotion basic offerings",
                newPromotion.getBasicOfferingNameList().containsAll(finalOfferingExternalKey)
                    && finalOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
                assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        validateAllCommonAssertions(newPromotion);
        validateFewCommonAssertions(newPromotion, name, finalStoreExternalKey);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate a non-storewide promotion for a subscription offer
     * with multiple stores attached to promotion
     */
    @Test
    public void activateNonStoreWidePromotionForSubscriptionOfferingWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, cashDiscountType, subscriptionOffersType, cashAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setAddOffering("add-offering");
        promotion.setOfferingMessage("all-offerings");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(finalSubscriptionOfferExternalKey)
                && finalSubscriptionOfferExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        validateAllCommonAssertions(newPromotion);
        validateFewCommonAssertions(newPromotion, name, finalStoreExternalKey);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate a storewide promotion for a basic offering with
     * multiple stores attached to the promotion
     */
    @Test
    public void activateStoreWidePromotionForBasicOfferingWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, cashDiscountType, basicOfferingsType, cashAmount, startDate, endDate, maximumUses,
            maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion, startSecondForPromotion,
            endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false, assertionErrorList);
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setOfferingMessage("all-offerings");
        promotion.setAmountInputMessage("all-fields");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setBasicOfferingsExternalKey(multipleBasicOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector
            .assertTrue("Incorrect promotion basic offerings",
                newPromotion.getBasicOfferingNameList().containsAll(finalOfferingExternalKey)
                    && finalOfferingExternalKey.containsAll(newPromotion.getBasicOfferingNameList()),
                assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        validateAllCommonAssertions(newPromotion);
        validateFewCommonAssertions(newPromotion, name, finalStoreExternalKey);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate a storewide promotion for a subscription offer with
     * multiple stores attached to promotion
     */
    @Test
    public void activateStoreWidePromotionForSubscriptionOfferingWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, cashDiscountType, subscriptionOffersType, cashAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setOfferingMessage("all-offerings");
        promotion.setAmountInputMessage("all-fields");
        promotion.setAddOffering("add-offering");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedCashType), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(finalSubscriptionOfferExternalKey)
                && finalSubscriptionOfferExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getCashAmount(), equalTo(cashAmount),
            assertionErrorList);
        validateAllCommonAssertions(newPromotion);
        validateFewCommonAssertions(newPromotion, name, finalStoreExternalKey);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate a storewide promotion with percentage discount for
     * a subscription offer with multiple stores attached to promotion
     */
    @Test
    public void activateStoreWidePromotionWithPercentageDiscountForSubscriptionOfferingWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setOfferingMessage("all-offerings");
        promotion.setAmountInputMessage("all-fields");
        promotion.setAddOffering("add-offering");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedPercentageDiscount), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(finalSubscriptionOfferExternalKey)
                && finalSubscriptionOfferExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(expectedPercentageAmount), assertionErrorList);
        validateAllCommonAssertions(newPromotion);
        validateFewCommonAssertions(newPromotion, name, finalStoreExternalKey);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test whether we can activate a non-storewide promotion with percentage discount
     * for a subscription offer with multiple stores attached to promotion
     */
    @Test
    public void activateNonStoreWidePromotionWithPercentageDiscountForSubscriptionOfferingWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setStoreWideErrorMessage("");
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setOfferingMessage("all-offerings");
        promotion.setAmountInputMessage("all-fields");
        promotion.setAddOffering("add-offering");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect Discount Type", newPromotion.getDiscountType(),
            equalTo(discountPromotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion type", newPromotion.getPromotionType(),
            equalTo(expectedPercentageDiscount), assertionErrorList);
        AssertCollector.assertTrue("Incorrect promotion subscription offerings",
            newPromotion.getSubscriptionOfferNameList().containsAll(finalSubscriptionOfferExternalKey)
                && finalSubscriptionOfferExternalKey.containsAll(newPromotion.getSubscriptionOfferNameList()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion amount", newPromotion.getPercentageAmount(),
            equalTo(expectedPercentageAmount), assertionErrorList);
        validateAllCommonAssertions(newPromotion);
        validateFewCommonAssertions(newPromotion, name, finalStoreExternalKey);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the error message when we add a promotion for subscription offering without
     * entering cash amount for all pricelists
     */
    @Test
    public void verifyErrorMessageForStoreWidePromotionForSubscriptionOfferingWithoutEnteringAllPricelists() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, cashDiscountType, subscriptionOffersType, cashAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setOfferingMessage("all-offerings");
        promotion.setAddOffering("add-offering");
        promotion.setStoreWideErrorMessage("not-all-pricelists");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect promotion error message", newPromotion.getPriceListErrorMessage(),
            equalTo("all price lists must have an amount for a storewide promotion"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the error message when we add a promotion for basic offering without
     * entering cash amount for all pricelists
     */
    @Test
    public void verifyErrorMessageForStoreWidePromotionForBasicOfferingWithoutEnteringAllPricelists() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, cashDiscountType, basicOfferingsType, cashAmount, startDate, endDate, maximumUses,
            maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion, startSecondForPromotion,
            endHourForPromotion, endMinuteForPromotion, endSecondForPromotion, null, false, assertionErrorList);
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setOfferingMessage("all-offerings");
        promotion.setStoreWideErrorMessage("not-all-pricelists");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setBasicOfferingsExternalKey(multipleBasicOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        AssertCollector.assertThat("Incorrect promotion error message", newPromotion.getPriceListErrorMessage(),
            equalTo("all price lists must have an amount for a storewide promotion"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which will test the error message when we add a two store-wide promotions for same basic
     * offering with multiple stores at the same time frame
     */
    @Test
    public void activateTwoStoreWidePromotionForSubscriptionOfferWithMultipleStores() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final String numberOfBillingCycles = "unlimited";
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount, startDate, endDate,
            maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion, startMinuteForPromotion,
            startSecondForPromotion, endHourForPromotion, endMinuteForPromotion, endSecondForPromotion,
            numberOfBillingCycles, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("");
        promotion.setStoreWide(true);
        promotion.setOfferingMessage("all-offerings");
        promotion.setAddOffering("add-offering");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);

        final String duplicatePromotionName = promotionName + (int) (Math.random() * 10);
        final Promotion duplicatePromotion = HelperForPromotions.assignFieldsToPromotionObject(duplicatePromotionName,
            finalStoreId, discountPromotionType, percentageDiscountType, subscriptionOffersType, percentageAmount,
            startDate, endDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, numberOfBillingCycles, false, assertionErrorList);
        duplicatePromotion.setPriceListExternalKey("select-pricelist");
        duplicatePromotion.setStoreWideErrorMessage("two-store-wide");
        duplicatePromotion.setStoreWide(true);
        duplicatePromotion.setSubscriptionOfferingsExternalKey(multipleSubscriptionOfferingsExternalKeyList);
        final Promotion newDuplicatePromotion = addPromotionPage.addPromotion(duplicatePromotion);
        AssertCollector.assertThat("Incorrect error message for two store wide promotions",
            newDuplicatePromotion.getStoreWideErrorMessage(), equalTo(STORE_WIDE_PROMO_ALREADY_EXISTS),
            assertionErrorList);
        cancelActivePromotion(newPromotion.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This is a test method which tests the error message being displayed when we activate a promotion with multiple
     * stores for a basic offering and promotion type mismatch
     */
    @Test
    public void activatePromotionWithMultipleStoresWithOfferTimeAndPromotionTypeMisMatch() {
        adminToolPage.login();
        final String finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final String name = promotionName + (int) (Math.random() * 10);
        final Promotion promotion = HelperForPromotions.assignFieldsToPromotionObject(name, finalStoreId,
            discountPromotionType, percentageDiscountType, basicOfferingsType, percentageAmount, futureStartDate,
            futureEndDate, maximumUses, maximumUsesByUser, activateStatus, startHourForPromotion,
            startMinuteForPromotion, startSecondForPromotion, endHourForPromotion, endMinuteForPromotion,
            endSecondForPromotion, null, false, assertionErrorList);
        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWideErrorMessage("Time-Mismatch");
        promotion.setOfferingMessage("all-offerings");
        String finalStoreExternalKey = "";
        for (final String store : storeExternalKeyList) {
            finalStoreExternalKey = finalStoreExternalKey + store;
        }
        promotion.setBasicOfferingsExternalKey(multipleBasicOfferingsExternalKeyList);
        final Promotion newPromotion = addPromotionPage.addPromotion(promotion);
        final String errorMessage = newPromotion.getTimeMismatchError();
        AssertCollector.assertThat("Incorrect error message for promotion time mismatch",
            newPromotion.getTimeMismatchError().substring(0, 12), equalTo(errorMessage.substring(0, 12)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * @param newPromotion
     */
    private void validateAllCommonAssertions(final Promotion newPromotion) {
        AssertCollector.assertThat("Incorrect promotion max uses", newPromotion.getMaxUses(), equalTo(maximumUses),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion max uses per user", newPromotion.getMaxUsesPerUser(),
            equalTo(maximumUsesByUser), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion start date", newPromotion.getEffectiveDate().substring(5, 15),
            equalTo(startDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion expiration date",
            newPromotion.getEffectiveDate().substring(32, 42), equalTo(endDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start hours", newPromotion.getEffectiveDate().substring(16, 18),
            equalTo(startHourForPromotion), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start minutes",
            newPromotion.getEffectiveDate().substring(19, 21), equalTo(startMinuteForPromotion), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start second",
            newPromotion.getEffectiveDate().substring(22, 24), equalTo(startSecondForPromotion), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration hours",
            newPromotion.getEffectiveDate().substring(43, 45), equalTo(endHourForPromotion), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration minutes",
            newPromotion.getEffectiveDate().substring(46, 48), equalTo(endMinuteForPromotion), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration seconds",
            newPromotion.getEffectiveDate().substring(49, 51), equalTo(endSecondForPromotion), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion state", newPromotion.getState(), equalTo(activateStatus),
            assertionErrorList);

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
            equalTo(promotioncodeLength), assertionErrorList);
    }

    private void cancelActivePromotion(final String promotionId) {
        DbUtils.updateTableInDb("promotion", "state", "2", "id", promotionId, getEnvironmentVariables());
    }
}
