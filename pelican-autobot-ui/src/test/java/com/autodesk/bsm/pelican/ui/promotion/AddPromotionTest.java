package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Store;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonStatus;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
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
import com.autodesk.bsm.pelican.helper.auditlog.PromotionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.AddPromotionPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.EditPromotionPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.FindPromotionsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.beust.jcommander.internal.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * This class tests Add Promotion functionality, includes tests to verify the following.
 */

public class AddPromotionTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private AdminToolPage adminToolPage;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private static final String TEST_NAME = "PromoName_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TEST_DESCRIPTION = "PromoDescription_" + RandomStringUtils.randomAlphanumeric(5);
    private static final int totalNumberOfOfferings = 125;
    private String storeId;
    private JStore addedStore;
    private static String storeExternalKey;
    private static String finalStoreExternalKey;
    private static String multipleStoreExternalKey;
    private List<String> storeIdList;
    private String priceListExternalKey = null;
    private String basicofferingExternalKey = null;
    private String subscriptionOfferExternalKey = null;
    private String subscriptionOfferExternalKey1;
    private Date startDate;
    private Date endDate;
    private List<String> priceListNames;
    private List<String> currencyNames;
    private static final Random random = new Random();
    private String promoCodeForNew;
    private String promoCodeForActive;
    private Calendar calendar;
    private PelicanTriggerClient triggerResource;
    private JobsClient jobsResource;
    private String now;
    private FindPromotionsPage findPromotionsPage;
    private PromotionUtils promoUtils;
    private PromotionDetailsPage promotionDetailsPage;
    private EditPromotionPage editPromotionPage;
    private static final String SUPPLEMENT_PROMO_CODE_PREFIX = "SPLPRM";
    private static final String DISCOUNT_PROMO_CODE_PREFIX = "DISCPROMO";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddPromotionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();
        jobsResource = triggerResource.jobs();

        promoUtils = new PromotionUtils(getEnvironmentVariables());
        // Create a store.
        final StoreApiUtils newStore = new StoreApiUtils(getEnvironmentVariables());
        addedStore = newStore.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        storeId = addedStore.getId();
        storeExternalKey = addedStore.getExternalKey();
        LOGGER.info("Id of a Store which will be used while adding a Promotion ---> " + storeId);
        newStore.addPriceList(storeId, Currency.CAD);
        priceListExternalKey = addedStore.getIncluded().getPriceLists().get(0).getExternalKey();
        LOGGER.info("External key of Price list which will be passed " + "while creating a Basic Offering ----> "
            + priceListExternalKey);
        addedStore = resource.stores().getStore(addedStore.getId());

        // Create second store
        JStore secondStore = newStore.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final String secondStoreId = secondStore.getId();
        final String secondStoreExternalKey = secondStore.getExternalKey();
        newStore.addPriceList(secondStoreId, Currency.CAD);
        secondStore = resource.stores().getStore(secondStore.getId());

        // create third store
        JStore thirdStore = newStore.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final String thirdStoreId = thirdStore.getId();
        final String thirdStoreExternalKey = thirdStore.getExternalKey();
        finalStoreExternalKey = storeExternalKey + secondStoreExternalKey + thirdStoreExternalKey;
        multipleStoreExternalKey = secondStoreExternalKey + thirdStoreExternalKey;
        newStore.addPriceList(thirdStoreId, Currency.CAD);
        thirdStore = resource.stores().getStore(thirdStore.getId());

        // create a basic offering
        final BasicOfferingApiUtils newBasicOffering = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings addedBasicOffering = newBasicOffering.addBasicOffering(priceListExternalKey,
            OfferingType.PERPETUAL, MediaType.USB, Status.ACTIVE, UsageType.COM, null);

        LOGGER.info("The Added Offering Id is : " + addedBasicOffering.getOfferings().get(0).getId());
        basicofferingExternalKey = addedBasicOffering.getOfferings().get(0).getExternalKey();
        LOGGER
            .info("External key of Basic Offer which can be used while adding a Promotion " + basicofferingExternalKey);

        // Create a subscription plan.
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings offerings = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKey,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferExternalKey = offerings.getIncluded().getBillingPlans().get(0).getExternalKey();
        LOGGER.info("External key of Subscription Offers which can be used while adding a Promotion "
            + subscriptionOfferExternalKey);

        final Offerings offerings1 = subscriptionPlanApiUtils.addSubscriptionPlan(priceListExternalKey,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferExternalKey1 = offerings1.getIncluded().getBillingPlans().get(0).getExternalKey();

        // Get the start date and end date for the promotion
        startDate = DateTimeUtils.getNowPlusSecs(500);
        endDate = DateTimeUtils.getUTCFutureExpirationDate();

        // Create a list of store ids
        storeIdList = new ArrayList<>();
        storeIdList.add(storeId);
        storeIdList.add(secondStoreId);
        storeIdList.add(thirdStoreId);

        // Create a list of pricelist names
        priceListNames = new ArrayList<>();
        priceListNames.add(addedStore.getIncluded().getPriceLists().get(0).getName());
        priceListNames.add(addedStore.getIncluded().getPriceLists().get(1).getName());
        priceListNames.add(secondStore.getIncluded().getPriceLists().get(0).getName());
        priceListNames.add(secondStore.getIncluded().getPriceLists().get(1).getName());
        priceListNames.add(thirdStore.getIncluded().getPriceLists().get(0).getName());
        priceListNames.add(thirdStore.getIncluded().getPriceLists().get(1).getName());

        // Create a list of currency names
        currencyNames = new ArrayList<>();
        currencyNames.add(addedStore.getIncluded().getPriceLists().get(0).getCurrency());
        currencyNames.add(addedStore.getIncluded().getPriceLists().get(1).getCurrency());
        currencyNames.add(secondStore.getIncluded().getPriceLists().get(0).getCurrency());
        currencyNames.add(secondStore.getIncluded().getPriceLists().get(1).getCurrency());
        currencyNames.add(thirdStore.getIncluded().getPriceLists().get(0).getCurrency());
        currencyNames.add(thirdStore.getIncluded().getPriceLists().get(1).getCurrency());

        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.AUDIT_LOG_DATE_FORMAT);
        findPromotionsPage = adminToolPage.getPage(FindPromotionsPage.class);
        final Date date = new Date();
        now = dateFormat.format(date);
    }

    /**
     * Test case to verify invalid basic offerings key.
     *
     * @result promotion is created successfully
     */
    @Test
    public void verifyInvalidBasicOfferingsKey() {

        final String offeringsExternalKey = "Invalid123";
        final String[] offeringsExtKey = new String[] { offeringsExternalKey };

        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, "Basic Offerings", offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        promotionPage.add(testPromotion, 0, false, false, false, true);
        LOGGER.info("Verify invalid baisc offerings key");

        // Get the actual and expected error message.
        final String actualErrorMessage = promotionPage.getOfferingsErrorMessage();
        final String expectedErrorMessage = "Invalid key(s)";
        AssertCollector.assertThat("Invalid key error message not matched with expected.", actualErrorMessage,
            equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify duplicate subscription offers.
     *
     * @result promotion is created successfully
     */
    @Test
    public void verifyDuplicateSubscriptionOffersKey() {

        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey, subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "10", "3", false);

        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        promotionPage.add(testPromotion, 0, false, false, false, true);
        LOGGER.info("Verify subscription offers duplicate keys");
        // Get the actual and expected error message.
        final String actualErrorMessage = promotionPage.getSubscriptionOffersErrorMessage();
        final String expectedErrorMessage = "Duplicate key(s)";
        AssertCollector.assertThat("Duplicate key not matched with expected", actualErrorMessage,
            equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify if promotion with Amount discount type can be added for a basic offerings.
     *
     * @result promotion is created successfully
     */
    @Test
    public void verifyBasicOfferingsPromotionWithDiscountTypeAsAmount() {

        final String[] offeringsExtKey = new String[] { basicofferingExternalKey };
        final Promotion testPromotion =
            getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId, PromotionUtils.DISCOUNT_STR, null, null,
                null, "Basic Offerings", offeringsExtKey, "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotionDetailObj = promotionPage.add(testPromotion, 0, true, true, true, true);
        LOGGER.info("Verify add promotion with discount type as amount");
        final String discType = testPromotion.getDiscountType().split(" ")[0] + " Discount";
        AssertCollector.assertThat("Discount type is not matched with expected.", discType,
            equalTo(promotionDetailObj.getDiscountType()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify if promotion with percentage discount type can be added for subscription offers.
     *
     * @result promotion is created successfully
     */
    @Test
    public void verifySubscriptionPromotionWithDiscountTypeAsPercentage() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotionDetailObj = promotionPage.add(testPromotion, 0, true, true, false, true);
        LOGGER.info("Verify add promotion with discount type as percentage.");
        final String discType = testPromotion.getDiscountType() + " Discount";
        AssertCollector.assertThat("Discount type is not matched with expected.", discType,
            equalTo(promotionDetailObj.getDiscountType()), assertionErrorList);
        // Get promotion details page after adding a promotion
        final PromotionDetailsPage expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);
        final String promotionId = expectedPromotion.getId();
        LOGGER.info("PromoId of the promo with discount type as percentage in NEW status is: " + promotionId);
        PromotionAuditLogHelper.verifyAuditDataForAddPromotion(promotionId, promotionDetailObj, addedStore, now,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify subscription Offer promotion with discount type as Amount.
     *
     * @result promotion is created successfully
     */
    @Test
    public void verifySubscriptionPromotionWithDiscountTypeAsAmount() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotionDetailObj = promotionPage.add(testPromotion, 0, true, true, false, true);
        LOGGER.info("Verify promotion with discount type as amount is added");
        final String discType = testPromotion.getDiscountType().split(" ")[0] + " Discount";
        AssertCollector.assertThat("Discount type is not matched with expected.", discType,
            equalTo(promotionDetailObj.getDiscountType()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify custom promotion code rules.
     *
     * @result custom promotion must be between 5 and 16 characters"
     */
    @Test
    public void verifyCustomPromotionCodeRules() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, "123%", storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        promotionPage.add(testPromotion, 0, true, false, true, true);
        LOGGER.info("Verify custom promotion code error message.");
        // Get the actual and expected error message.
        final String actualErrorMessage = promotionPage.getErrorMessageForField();
        final String expectedErrorMessage = "Must be between 5 and 16 characters";
        AssertCollector.assertThat("Custom promotion code not matched with expected", actualErrorMessage,
            equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify name is required for promotion.
     *
     * @result name required
     */
    @Test
    public void verifyNameIsRequiredForPromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData("", TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        promotionPage.add(testPromotion, 0, true, false, false, true);
        LOGGER.info("Verify all required fields");
        // Get actual and expected error message.
        final String actualErrorMessage = promotionPage.getErrorMessageForField();
        final String expectedErrorMessage = "Name required";
        AssertCollector.assertThat("Name is required error message not matched with expected.", actualErrorMessage,
            equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify Effective date cannot be after expiration date.
     *
     * @result Effective date cannot be after expiration date.
     */
    @Test
    public void verifyEffectiveCannotBeAfterExpirationDate() throws ParseException {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final String futurDate = DateTimeUtils.getNextMonthDateAsString();
        final Date futureStartDate =
            DateTimeUtils.getSimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH).parse(futurDate);

        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", futureStartDate, startDate, "50", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        promotionPage.add(testPromotion, 0, true, false, false, true);
        LOGGER.info("Verify effective date range cannot be greater than expiration date");
        // Get actual and expected error message.
        final String actualErrorMessage = promotionPage.getAllErrorMessages();
        final String expectedErrorMessage = "Effective date cannot be after expiration date.";
        AssertCollector.assertThat("Effective date error message not matched with expected.", actualErrorMessage,
            equalTo(expectedErrorMessage), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify Promotion with promo type as supplement.
     *
     * @result
     */
    @Test
    public void verifySupplementPromotionAddedSuccessfully_DEFECT_BIC6030() {
        LOGGER.info("Add promotion with Supplement type details");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion actualPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Months",
            PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey, null, null, startDate, endDate, "100", "3", false);
        final AddPromotionPage promotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion expectedPromotion = promotionPage.add(actualPromotion, 0, true, true, false, true);
        LOGGER.info("Verify supplement type details");
        AssertCollector.assertThat("Promotion type is not matched with expected", actualPromotion.getPromotionType(),
            equalTo(expectedPromotion.getPromotionType()), assertionErrorList);
        AssertCollector.assertThat("Supplement type is not matched with expected", actualPromotion.getSupplementType(),
            equalTo(expectedPromotion.getSupplementType()), assertionErrorList);
        AssertCollector.assertThat("Time period count is not matched with expected",
            actualPromotion.getTimePeriodCount(), equalTo(expectedPromotion.getTimePeriodCount()), assertionErrorList);
        // Get promotion details page after adding a promotion
        final PromotionDetailsPage promoDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        final String promotionId = promoDetailsPage.getId();
        LOGGER.info("PromoId of the promo with discount type as supplement in NEW status is: " + promotionId);
        PromotionAuditLogHelper.verifyAuditDataForAddPromotion(promotionId, expectedPromotion, addedStore, now,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests that a cancelled promo is used to create a NEW promo Step1: Create a promo with promocode in
     * "active" state Step2: Cancel the promo Step3: Create a NEW promo with the same promo(same promo used in Step1) It
     * validates that id of both promotions should be different, promo code should be same and state should be NEW.
     */
    @Test
    public void testAddNewPromoWithCancelledPromoCode_DEFECT_BIC6030() {

        LOGGER.info("Add promotion with Supplement type details");
        // create a promotion with promo code
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey1 };
        promoCodeForNew = SUPPLEMENT_PROMO_CODE_PREFIX + random.nextInt(100000000);
        Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForNew, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Months",
            PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey, null, null, startDate, endDate, "100", "3", true);
        AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        // get promotion details page after adding a promotion
        PromotionDetailsPage expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);
        final String idOfFirstPromo = expectedPromotion.getId();

        // cancel a promotion using the pop up
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        expectedPromotion.cancelPromotion();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        expectedPromotion.popUpWindowConfirm();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        LOGGER.info("Creating an Active Promo with cancelled Promocode");
        // create another promotion using the same promocode in NEW status
        promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForNew, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Months",
            PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey, null, null, startDate, endDate, "100", "3", false);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);

        AssertCollector.assertThat("Ids should be different in both the promotions", expectedPromotion.getId(),
            not(equalTo(idOfFirstPromo)), assertionErrorList);
        AssertCollector.assertThat("Promocode should be same in both the promotions",
            expectedPromotion.getPromotionCode(), equalTo(promoCodeForNew), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", expectedPromotion.getState(), equalTo(Status.NEW.toString()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the error scenario when a NEW promocode used to create another NEW promo.
     */
    @Test(dependsOnMethods = { "testAddNewPromoWithCancelledPromoCode_DEFECT_BIC6030" })
    public void testErrorInAddingNewPromoWithNewPromoCode_DEFECT_BIC6030() {

        adminToolPage.login();
        LOGGER.info("Add promotion with Supplement type details");
        // create a promotion with promo code
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForNew, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Months",
            PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey, null, null, startDate, endDate, "100", "3", false);

        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        final String actualErrorMessage = addPromotionPage.getErrorMessageForField().split(":")[0];
        AssertCollector.assertThat("Incorrect error message", actualErrorMessage,
            equalTo("The promo code is already in use with the promotion"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests that a cancelled promo is used to create a ACTIVE promo Step1: Create a promo with promocode in
     * "active" state Step2: Cancel the promo Step3: Create an ACTIVE promo with the same promo(same promo used in
     * Step1) It validates that id of both promotions should be different, promo code should be same and state should be
     * ACTIVE.
     */
    @Test
    public void testAddActivePromoWithCancelledPromoCode() {

        LOGGER.info("Add promotion with Supplement type details");
        // create a promotion with promo code
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForActive = "DISCPROMO" + random.nextInt(10000);
        Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "50", "3", true);
        AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        // get promotion details page after adding a promotion
        PromotionDetailsPage expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);
        final String idOfFirstPromo = expectedPromotion.getId();

        // cancel a promotion using the pop up
        expectedPromotion.cancelPromotion();
        expectedPromotion.popUpWindowConfirm();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        LOGGER.info("Creating an Active Promo with cancelled Promocode");
        // create another promotion using the same promocode in ACTIVE status
        promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "50", "3", true);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);

        AssertCollector.assertThat("Ids should be different in both the promotions", expectedPromotion.getId(),
            not(equalTo(idOfFirstPromo)), assertionErrorList);
        AssertCollector.assertThat("Promocode should be same in both the promotions",
            expectedPromotion.getPromotionCode(), equalTo(promoCodeForActive), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", expectedPromotion.getState(), equalTo(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the error scenario when a Active promocode used to create another Active promo.
     */
    @Test(dependsOnMethods = { "testAddActivePromoWithCancelledPromoCode" })
    public void testErrorInAddingActivePromoWithActivePromoCode() {

        adminToolPage.login();
        LOGGER.info("Add promotion with Supplement type details");
        // create a promotion with promo code
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "50", "3", true);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String actualErrorMessage = addPromotionPage.getErrorMessageForField().split(":")[0];
        AssertCollector.assertThat("Incorrect error message", actualErrorMessage,
            equalTo("The promo code is already in use with the promotion"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests that a expired promo is used to create a NEW promo Step1: Create a promo with promocode in
     * "new" state Step2: Expire the promo Step3: Create a NEW promo with the same promo(same promo used in Step1) It
     * validates that id of both promotions should be different, promo code should be same and state should be NEW.
     */
    @Test
    public void testAddNewPromoWithExpiredPromoCode_DEFECT_BIC6030() {

        LOGGER.info("Add promotion with Supplement type details");
        // create a promotion with promo code
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForNew = "SUPPLEEXP" + random.nextInt(10000);
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);

        Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForNew, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Months",
            PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey, null, null, startDate, calendar.getTime(), "100",
            "3", false);
        AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        // get promotion details page after adding a promotion
        PromotionDetailsPage expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);
        final String idOfFirstPromo = expectedPromotion.getId();
        // Running the promotion expiration job
        LOGGER.info("Run Triggers Job to make the promotion to get EXPIRED");
        jobsResource = triggerResource.jobs();
        final JsonStatus response = jobsResource.promotionExpiration();
        LOGGER.info("triggers response: " + response);

        LOGGER.info("Creating an Active Promo with expired Promocode");
        // create another promotion using the same promocode in NEW status
        promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForNew, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Months",
            PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey, null, null, startDate, endDate, "100", "3", false);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);

        AssertCollector.assertThat("Ids should be different in both the promotions", expectedPromotion.getId(),
            not(equalTo(idOfFirstPromo)), assertionErrorList);
        AssertCollector.assertThat("Promocode should be same in both the promotions",
            expectedPromotion.getPromotionCode(), equalTo(promoCodeForNew), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", expectedPromotion.getState(), equalTo(Status.NEW.toString()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests that a expired promo is used to create a ACTIVE promo Step1: Create a promo with promocode in
     * "new" state Step2: Expire the promo Step3: Create an ACTIVE promo with the same promo(same promo used in Step1)
     * It validates that id of both promotions should be different, promo code should be same and state should be
     * ACTIVE.
     */
    @Test
    public void testAddActivePromoWithExpiredPromoCode() throws ParseException {

        LOGGER.info("Add promotion with Supplement type details");
        // create a promotion with promo code
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForActive = "SUPPLEPROMO" + random.nextInt(10000);
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        final Date expirationDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, expirationDate, "50", "3", true);
        AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        // get promotion details page after adding a promotion
        PromotionDetailsPage expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);
        final String idOfFirstPromo = expectedPromotion.getId();

        // Running the promotion expiration job
        LOGGER.info("Run Triggers Job to make the promotion to get EXPIRED");
        jobsResource = triggerResource.jobs();
        final JsonStatus response = jobsResource.promotionExpiration();
        LOGGER.info("triggers response: " + response);

        LOGGER.info("Creating an Active Promo with expired Promocode");
        // create another promotion using the same promocode in ACTIVE status
        promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "10", startDate, endDate, "50", "3", true);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);
        expectedPromotion = adminToolPage.getPage(PromotionDetailsPage.class);

        AssertCollector.assertThat("Ids should be different in both the promotions", expectedPromotion.getId(),
            not(equalTo(idOfFirstPromo)), assertionErrorList);
        AssertCollector.assertThat("Promocode should be same in both the promotions",
            expectedPromotion.getPromotionCode(), equalTo(promoCodeForActive), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", expectedPromotion.getState(), equalTo(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify created Promotion has 125 number of Basic Offerings.
     */
    @Test(enabled = false)
    public void getPromotionWith125BasicOfferings() {
        // create 125 basic offerings
        final BasicOfferingApiUtils newBasicOfferings = new BasicOfferingApiUtils(getEnvironmentVariables());
        final JPromotionData.PromotionOfferings[] basicOfferingId =
            new JPromotionData.PromotionOfferings[totalNumberOfOfferings];
        for (int i = 0; i < totalNumberOfOfferings; i++) {
            final Offerings basicOffering = newBasicOfferings.addBasicOffering(priceListExternalKey,
                OfferingType.PERPETUAL, MediaType.USB, Status.ACTIVE, UsageType.COM, null);
            if (basicOffering.getOfferings().get(0) != null) {
                final JPromotionData.PromotionOfferings promoOffering = new JPromotionData.PromotionOfferings();
                promoOffering.setId(basicOffering.getOfferings().get(0).getId());
                promoOffering.setQuantity(1);
                promoOffering.setApplyDiscount(true);
                basicOfferingId[i] = promoOffering;
            }
        }
        LOGGER.info("Total Number of Basic Offerings are: " + basicOfferingId.length);
        for (int j = 0; j < totalNumberOfOfferings; j++) {
            LOGGER.info("Id of Basic Offering " + (j + 1) + " " + basicOfferingId[j]);
        }

        // Create a Promotion with 125 number of basic offering in it
        final String promoCode1 = promoUtils.getRandomPromoCode();
        final JPromotion addPromotionWithBasicOfferings = createPromotion(promoCode1, Status.ACTIVE, 25.0,
            DateTimeUtils.getFutureExpirationDate(), "basicOfferings", basicOfferingId);
        final String promoWithBasicOfferingId = addPromotionWithBasicOfferings.getData().getId();
        final String promoWithBasicOfferingCode = addPromotionWithBasicOfferings.getData().getCustomPromoCode();
        LOGGER.info("Promotion Id: " + promoWithBasicOfferingId);
        LOGGER.info("Promotion Code: " + promoWithBasicOfferingCode);

        // Navigate to the Promotion in Admin Tool and verify the total number
        // of basic offerings in it
        promotionDetailsPage = findPromotionsPage.selectResultRowWithFindByCode(0, promoCode1);

        final int basicOfferingList = promotionDetailsPage.getTotalNumberOfValuesInField("Basic Offerings");
        AssertCollector.assertThat("Could not be able to add 125 Basic Offerings in a Promotion ", basicOfferingList,
            equalTo(totalNumberOfOfferings), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify created Promotion has 125 number of Subscription offers.
     */
    @Test(enabled = false)
    public void getPromotionWith125SubscriptionOffers() {
        // Create 125 subscription Offers.
        final SubscriptionPlanApiUtils newSubscriptionOffer = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final JPromotionData.PromotionOfferings[] subscriptionOfferId =
            new JPromotionData.PromotionOfferings[totalNumberOfOfferings];
        for (int i = 0; i < totalNumberOfOfferings; i++) {
            final Offerings subscriptionOffer =
                newSubscriptionOffer.addSubscriptionPlan(priceListExternalKey, OfferingType.BIC_SUBSCRIPTION,
                    BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
            if (subscriptionOffer.getOfferings().get(0) != null) {
                final JPromotionData.PromotionOfferings promoOffering = new JPromotionData.PromotionOfferings();
                promoOffering.setId(subscriptionOffer.getIncluded().getBillingPlans().get(0).getId());
                promoOffering.setQuantity(1);
                promoOffering.setApplyDiscount(true);
                subscriptionOfferId[i] = promoOffering;

            }
        }
        LOGGER.info("Total Number of Subscription Offerings are: " + subscriptionOfferId.length);
        for (int j = 0; j < totalNumberOfOfferings; j++) {
            LOGGER.info("Id of Subscription Offer " + (j + 1) + " " + subscriptionOfferId[j]);
        }

        // Create a Promotion with 125 number of subscription offers in it
        final String promoCode2 = promoUtils.getRandomPromoCode();
        final JPromotion addPromotionWithSubscriptionOffers = createPromotion(promoCode2, Status.ACTIVE, 25.0,
            DateTimeUtils.getFutureExpirationDate(), "subscriptionOffers", subscriptionOfferId);
        final String promoWithSubscriptionOfferId = addPromotionWithSubscriptionOffers.getData().getId();
        final String promoWithSubscriptionOfferCode = addPromotionWithSubscriptionOffers.getData().getCustomPromoCode();
        LOGGER.info("Promotion Id: " + promoWithSubscriptionOfferId);
        LOGGER.info("Promotion Code: " + promoWithSubscriptionOfferCode);
        promotionDetailsPage = findPromotionsPage.selectResultRowWithFindByCode(0, promoCode2);

        // Navigate to the Promotion in Admin Tool and verify the total number
        // of subscription offers in it
        final int subscriptionOfferList =
            promotionDetailsPage.getTotalNumberOfValuesInField(PromotionUtils.SUBSCRIPTION_OFFERS_STR);
        AssertCollector.assertThat("Could not be able to add 125 Subscription Offer in a Promotion ",
            subscriptionOfferList, equalTo(totalNumberOfOfferings), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether a store is added successfully to a store-wide promotion.
     */
    @Test
    public void verifyStoreAddedForStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(storeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether a store is added successfully to a non-storewide promotion.
     */
    @Test
    public void verifyStoreAddedForNonStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(storeExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether multiple stores are added to a storewide promotion.
     */
    @Test
    public void verifyMultipleStoresAddedForStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /*
     * Verify whether multiple stores are added for a non-storewide promotion.
     */
    @Test
    public void verifyMultipleStoresAddedForNonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether the error message displayed when we enter an invalid store id for a store-wide promotion.
     */
    @Test
    public void verifyInvalidStoreAddedForStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { basicofferingExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, "1111111",
            PromotionUtils.DISCOUNT_STR, null, null, null, "Basic Offerings", offeringsExtKey, "Cash Amount", "10",
            startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(testPromotion, 0, false, false, false, false);
        LOGGER.info("Adding an Invalid Store for the promotion");
        final String errorMessage = addPromotionPage.getStoreErrorMessage();
        AssertCollector.assertThat("Incorrect error message for the invalid store", errorMessage,
            equalTo("Invalid Store Id"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether the error message displayed when we enter a valid and invalid Store Id list for a non-storewide
     * promotion.
     */
    @Test
    public void verifyInvalidStoreAddedForNonStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        storeIdList.add("1111111");
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final Promotion testPromotion =
            getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId, PromotionUtils.DISCOUNT_STR, null, null,
                null, "Basic Offerings", offeringsExtKey, "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(testPromotion, 0, false, false, false, false);
        LOGGER.info("Adding an Invalid Store for the promotion");
        final String errorMessage = addPromotionPage.getStoreErrorMessage();
        AssertCollector.assertThat("Incorrect error message for the invalid store", errorMessage,
            equalTo("One or more of the provided store IDs is invalid"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        // Need to remove this invalid store id from the stores list so that
        // other test cases use the valid
        // store ids
        storeIdList.remove("1111111");
    }

    /*
     * Verify whether we are handling the trailing and leading spaces correctly in the store search area.
     */
    @Test
    public void verifyTrailingAndLeadingSpacesInFindStoreSearchBox() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        storeIdList.set(0, "  " + storeIdList.get(0));
        storeIdList.set(2, storeIdList.get(2) + "   ");
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to edit the promotion in new state and delete all the stores and update the promotion.
     */
    @Test
    public void verifyDeletingStoresInNewStateForAStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(testPromotion, 0, true, true, true, true);
        PromotionDetailsPage promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        editPromotionPage = promotionDetailsPage.clickOnEdit();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        editPromotionPage.deleteAllStoresFromPromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage = editPromotionPage.clickOnUpdatePromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        AssertCollector.assertThat("Incorrect number of stores present after deleting all stores",
            promotionDetailsPage.getLimitedToStore(), equalTo(""), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we able to edit the promotion in new state and delete few stores and update the promotion.
     */
    @Test
    public void verifyDeletingFewStoresInNewStateForAStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(testPromotion, 0, true, true, true, true);
        PromotionDetailsPage promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        editPromotionPage = promotionDetailsPage.clickOnEdit();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        editPromotionPage.deleteFirstStoreFromPromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage = editPromotionPage.clickOnUpdatePromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        AssertCollector.assertThat("Incorrect number of stores present after deleting all stores",
            promotionDetailsPage.getLimitedToStores(), equalTo(multipleStoreExternalKey), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to see stores in new state in the store search finder.
     */
    @Test
    public void verifyIsStoresInNewStateDisplayedForAStoreWidePromotion() {
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        final String appendString = "SQA_Test_Util_";
        final String storeTypeExternalKey = appendString + RandomStringUtils.randomAlphabetic(10);
        final Store newStore =
            storeApiUtils.addStoreWithExternalKey(storeTypeExternalKey, finalStoreExternalKey, Status.NEW);
        final String storeId = "StoreSearchWithName" + newStore.getData().getExternalKey();
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("New Stores are also displayed in the promotion store search results",

            promotion.getStoreErrorMessage(), equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to see the correct number of pricelists for the selected stores in a non-storewide
     * promotion.
     */
    @Test
    public void verifyIsAllPricelistsDisplayedForANonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setPriceListMessage("return-promotion");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, false, true, true);
        AssertCollector.assertThat("Incorrect number of pricelists displayed for that store",
            promotion.getPriceListSize(), equalTo(6), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to see the correct pricelists for the selected stores in a non-storewide promotion.
     */
    @Test
    public void verifyIsAllPricelistsDisplayedCorrectlyForANonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setPriceListMessage("return-promotion");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, false, true, true);
        final Object[] actualPriceListNames = priceListNames.toArray();
        Arrays.sort(actualPriceListNames);
        final Object[] expectedPriceListNames = promotion.getPriceListNames().toArray();
        Arrays.sort(expectedPriceListNames);
        AssertCollector.assertThat("Incorrect pricelists are displayed for the promotion", actualPriceListNames,
            equalTo(expectedPriceListNames), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to see the correct currencies for the selected stores in a store-wide promotion.
     */
    @Test
    public void verifyIsAllCurrenciesDisplayedCorrectlyForANonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setPriceListMessage("return-promotion");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, false, true, true);
        final Object[] actualCurrencyNames = currencyNames.toArray();
        Arrays.sort(actualCurrencyNames);
        final Object[] expectedCurrencyNames = promotion.getCurrencyNames().toArray();
        Arrays.sort(expectedCurrencyNames);
        AssertCollector.assertThat("Incorrect pricelists are displayed for the promotion", actualCurrencyNames,
            equalTo(expectedCurrencyNames), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to save the promotion with the amount discount specified for all pricelists and
     * currencies in a storewide promotion.
     */
    @Test
    public void verifyIsAmountSpecifiedForAllPricelistsInStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("setAmountForAllPricelists");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to save the promotion with the amount discount specified for all pricelists and
     * currencies in a non-storewide promotion.
     */
    @Test
    public void verifyIsAmountSpecifiedForAllPricelistsInNonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("setAmountForAllPricelists");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to save the promotion with the amount discount specified for few pricelists and
     * currencies in a storewide promotion.
     */
    @Test
    public void verifyIsAmountSpecifiedForFewPricelistsInStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("setAmountForFewPricelists");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to save the promotion with the amount discount specified for few pricelists and
     * currencies in a non-storewide promotion.
     */
    @Test
    public void verifyIsAmountSpecifiedForFewPricelistsInNonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("setAmountForFewPricelists");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to save the promotion with the amount discount not specified for any pricelists and
     * currencies in a storewide promotion.
     */
    @Test
    public void verifyIsAmountNotSpecifiedForAnyPricelistsInStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("DontSetAmountToAnyField");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(true),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to save the promotion with the amount discount not specified for any pricelists and
     * currencies in a storewide promotion.
     */
    @Test
    public void verifyIsAmountNotSpecifiedForAnyPricelistsInNonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("DontSetAmountToAnyField");
        final Promotion promotion = addPromotionPage.add(testPromotion, 0, true, true, true, true);
        AssertCollector.assertThat("Promotion is not created", promotion.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion", promotion.getStoreId(),
            equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotion.getStoreWide(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether we are able to edit the promotion in new state and change the amount entered for all pricelists.
     */
    @Test
    public void verifyIsAmountEditedForPricelistsInNonStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("setAmountForAllPricelists");
        addPromotionPage.add(testPromotion, 0, true, true, true, true);
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        editPromotionPage = promotionDetailsPage.clickOnEdit();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        editPromotionPage.editAmountEnteredForPricelists("20");
        promotionDetailsPage = editPromotionPage.clickOnUpdatePromotion();
        AssertCollector.assertThat("Promotion is not created", promotionDetailsPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion",
            promotionDetailsPage.getLimitedToStores(), equalTo(finalStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotionDetailsPage.getStoreWide(),
            equalTo("NO"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * Verify whether stores are deleted, corresponding pricelists are not dispalyed from the promotion.
     */
    @Test
    public void verifyIsPricelistsDisplayedAfterDeletingStoresInStoreWidePromotion() {
        final String storeId = storeIdList.toString().replace("[", "").replace("]", "");
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        final Promotion testPromotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, null, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            "Cash Amount", "10", startDate, endDate, "10", "3", false);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        testPromotion.setAmountMessage("setAmountForAllPricelists");
        addPromotionPage.add(testPromotion, 0, true, true, true, true);
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        editPromotionPage = promotionDetailsPage.clickOnEdit();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        editPromotionPage.deleteFirstStoreFromPromotion();
        editPromotionPage.waitTillStoreIsDeleted();
        promotionDetailsPage = editPromotionPage.clickOnUpdatePromotion();
        AssertCollector.assertThat("Promotion is not created", promotionDetailsPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store associated for that promotion",
            promotionDetailsPage.getLimitedToStores(), equalTo(multipleStoreExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the promotion", promotionDetailsPage.getStoreWide(),
            equalTo("YES"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test add discount percentage promotion with status ACTIVE.
     *
     * @result promotion in ACTIVE status is created successfully
     */
    @Test
    public void testAddActiveDiscountPercentagePromoSuccess() {
        LOGGER.info("Adding ACTIVE discount percentage type promo");

        // Add a discount percentage promotion
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForActive = DISCOUNT_PROMO_CODE_PREFIX + random.nextInt(999999);
        final Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "20", startDate, endDate, "100", "3", true);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);

        // Get promotion details page after adding a promotion
        final PromotionDetailsPage promoDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        final String promoIdForActive = promoDetailsPage.getId();
        LOGGER.info("PromoId of the discount percentage promo in ACTIVE status is: " + promoIdForActive);
        PromotionAuditLogHelper.verifyAuditDataForAddPromotion(promoIdForActive, promotion, addedStore, now,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test add supplement promotion with status ACTIVE.
     *
     * @result promotion in ACTIVE status is created successfully
     */
    @Test
    public void testAddActiveSupplementPromoSuccess_DEFECT_BIC6030() {
        LOGGER.info("Adding ACTIVE supplement type promo");

        // Add a supplement promotion
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForActive = SUPPLEMENT_PROMO_CODE_PREFIX + RandomStringUtils.randomAlphanumeric(6);
        final Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT, "Time", "2", "Days", PromotionUtils.SUBSCRIPTION_OFFERS_STR,
            offeringsExtKey, null, null, startDate, endDate, "100", "3", true);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);

        // Get promotion details page after adding a promotion
        final PromotionDetailsPage promoDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String promoIdForActive = promoDetailsPage.getId();
        LOGGER.info("PromoId of the supplement promo in ACTIVE status is: " + promoIdForActive);
        PromotionAuditLogHelper.verifyAuditDataForAddPromotion(promoIdForActive, promotion, addedStore, now,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test add discount amount promotion with status ACTIVE.
     *
     * @result promotion in ACTIVE status is created successfully
     */
    @Test
    public void testAddActiveDiscountAmountPromoSuccess() {
        LOGGER.info("Adding ACTIVE discount amount type promo");

        // Add a discount amount promotion
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForActive = "AMTPROMO" + random.nextInt(999999);
        final Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, false, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.DISCOUNT_TYPE_AMOUNT, "20", startDate, endDate, "100", "3", true);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);

        // Get promotion details page after adding a promotion
        final PromotionDetailsPage promoDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        final String promoIdForActive = promoDetailsPage.getId();
        LOGGER.info("PromoId of the discount amount promo in ACTIVE status is: " + promoIdForActive);
        PromotionAuditLogHelper.verifyAuditDataForAddPromotion(promoIdForActive, promotion, addedStore, now,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test cash amount input field is displayed
     *
     * @result cash amount field is hidden or displayed on the page.
     */
    @Test
    public void testWhetherCashAmountFieldIsDisplayedOnPage() {
        LOGGER.info("Adding discount percentage type promo without entering discount percentage");

        // Add a discount percentage promotion
        final String[] offeringsExtKey = new String[] { subscriptionOfferExternalKey };
        promoCodeForActive = DISCOUNT_PROMO_CODE_PREFIX + random.nextInt(999999);
        final Promotion promotion = getpromotionData(TEST_NAME, TEST_DESCRIPTION, true, promoCodeForActive, storeId,
            PromotionUtils.DISCOUNT_STR, null, null, null, PromotionUtils.SUBSCRIPTION_OFFERS_STR, offeringsExtKey,
            PromotionUtils.PROMO_DISCOUNT_TYPE_PERCENT, "", startDate, endDate, "100", "3", true);
        final AddPromotionPage addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        addPromotionPage.add(promotion, 0, true, false, false, true);

        Util.waitInSeconds(TimeConstants.THREE_SEC);
        addPromotionPage.selectDiscountType(PromotionUtils.DISCOUNT_TYPE_AMOUNT);
        addPromotionPage.clickOnAddPromotion();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final boolean isCashAmountInputFieldPresent = addPromotionPage.isCashAmountFieldPresentOnPage();
        LOGGER.info("Is Field Present: " + isCashAmountInputFieldPresent);

        AssertCollector.assertTrue("Cash Amount Input field is hidden on the page", isCashAmountInputFieldPresent,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method is used to dynamically create a promotion through Api.
     *
     * @return JPromotion
     */
    private JPromotion createPromotion(final String promoCode, final Status status, final Double discountPercent,
        final Date expirationDate, final String offeringType, final JPromotionData.PromotionOfferings[] promoOffering) {
        final String promoName = UUID.randomUUID().toString();
        final String promoDescription = UUID.randomUUID().toString();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        final boolean storeWide = false;
        final JPromotion createdPromo;

        final JPromotion promoRequest = new JPromotion();
        final JPromotionData promoData = new JPromotionData();
        promoData.setType(EntityType.PROMOTION);
        promoData.setName(promoName);
        promoData.setDescription(promoDescription);
        promoData.setCustomPromoCode(promoCode);
        promoData.setStatus(status);
        promoData.setPromotionType(promoType);
        promoData.setStoreWide(storeWide);
        promoData.setDiscountPercent(discountPercent);
        promoData.setStoreIds(Lists.newArrayList(storeId));
        promoData.setOfferingType(offeringType);

        final List<JPromotionData.PromotionOfferings> listOfOfferingIds = new ArrayList<>();
        for (int i = 0; i < totalNumberOfOfferings; i++) {
            listOfOfferingIds.add(promoOffering[i]);
        }
        if (offeringType.equals("basicOfferings")) {
            promoData.setBasicOfferings(listOfOfferingIds);
        } else {
            promoData.setSubscriptionOffers(listOfOfferingIds);
            promoData.setNumberOfBillingCycles(1);
        }
        final Calendar calendar = Calendar.getInstance();
        final Date startDate = calendar.getTime();
        final String promoStartDate = dateFormat.format(startDate);
        final String promoExpirationDate = dateFormat.format(expirationDate);
        promoData.setEffectiveDate(promoStartDate);
        promoData.setExpirationDate(promoExpirationDate);
        promoRequest.setData(promoData);

        createdPromo = resource.promotion().addPromotion(promoRequest);

        LOGGER.info("Id of the created promotion: " + createdPromo.getData().getId());
        LOGGER.info("EntityType of the created Promotion: " + createdPromo.getData().getType());

        // The response from add promotion API currently has only promoId and
        // entityType. So, inorder to validate
        // the data from get promotions API, I am populating the request data
        // (i.e. name, status, discount etc.)
        // in the response pojo of add promotion API call.
        createdPromo.getData().setName(promoName);
        createdPromo.getData().setDescription(promoDescription);
        createdPromo.getData().setCustomPromoCode(promoCode);
        createdPromo.getData().setStatus(status);
        createdPromo.getData().setPromotionType(promoType);
        createdPromo.getData().setDiscountPercent(discountPercent);
        createdPromo.getData().setStoreWide(storeWide);
        createdPromo.getData().setCustomPromoCode(promoCode);
        if (offeringType.equals("basicOfferings")) {
            createdPromo.getData().setBasicOfferings(listOfOfferingIds);
        } else {
            createdPromo.getData().setSubscriptionOffers(listOfOfferingIds);
        }
        return createdPromo;
    }

    /**
     * Method to set the promotion details values.
     *
     * @return promotion
     */
    private Promotion getpromotionData(final String name, final String description, final boolean isStoreWide,
        final String promoCode, final String storeId, final String promoType, final String supplyType,
        final String timePeriodCount, final String timePeriodType, final String basicOfferings,
        final String[] offeringsExternalKey, final String discountType, final String amount, final Date effectiveDate,
        final Date expirationDate, final String maxUses, final String maxUsesPerUser, final boolean isActivePromotion) {
        LOGGER.info("Set the promotion detail values");
        final Promotion promotionObj = new Promotion();
        promotionObj.setApplicationFamily("Automated Tests (AUTO)");
        promotionObj.setApplication("ANY (*)");
        promotionObj.setName(name);
        promotionObj.setDescription(description);
        promotionObj.setStoreWide(isStoreWide);
        promotionObj.setPromotionCode(promoCode);
        promotionObj.setStoreId(storeId);
        promotionObj.setPromotionType(promoType);
        // Set Supplement type and its values.
        if (promoType.equals(PromotionUtils.PROMO_DISCOUNT_TYPE_SUPPLEMENT)) {
            promotionObj.setSupplementType(supplyType);
            promotionObj.setTimePeriodCount(timePeriodCount);
            promotionObj.setTimePeriodType(timePeriodType);
        } else {
            // Set discount type as amount or percentage.
            promotionObj.setDiscountType(discountType);
            if (discountType.equals("Cash Amount")) {
                promotionObj.setAmount(amount);
            } else {
                promotionObj.setPercentage(amount);
            }
        }
        // Set offerings type as basic offerings or subscription offers.
        promotionObj.setOfferingType(basicOfferings);
        // Set basic offerings or subscription offers external key.
        if (basicOfferings.equals("basicOfferings")) {
            promotionObj.setOfferingsExternalKey(offeringsExternalKey);
        } else {
            promotionObj.setOfferingsExternalKey(offeringsExternalKey);
        }
        promotionObj.setEffectiveDate(dateFormat.format(effectiveDate));
        promotionObj.setExpirationDate(dateFormat.format(expirationDate));
        promotionObj.setMaxUses(maxUses);
        promotionObj.setMaxUsesPerUser(maxUsesPerUser);
        promotionObj.setActivatePromotion(isActivePromotion);
        return promotionObj;
    }
}
