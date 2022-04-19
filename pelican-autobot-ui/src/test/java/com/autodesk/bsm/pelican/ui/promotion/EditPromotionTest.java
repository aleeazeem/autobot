package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.PromotionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.AddPromotionPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.EditPromotionPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.FindPromotionsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionsSearchResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This class tests Edit Promotion functionality, includes tests to verify the following: Verify that Edit option is
 * available only for Promotion in New State and not in ACTIVE, EXPIRED or CANCELLED. Verify that if a Promotions in NEW
 * state can be Edited and saved. Verify that after editing fields the changes should not be saved after clicking the
 * Cancel Button.
 *
 * @author Muhammad Azeem
 */
public class EditPromotionTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private EditPromotionPage editPromotionPage;
    private Promotion newPromotion;
    private Promotion promoDetails;
    private String createdPromotionId;
    private String autoCode;
    private String autoName;
    private String autoDescription;
    private String autoPromotionType;
    private String autoDiscountType;
    private String autoValueOfDiscount;
    private String autoOfferingType;
    private String autoOfferingOrSubscription;
    private boolean autoStoreWide;
    private String autoExpirationDate;
    private String autoMaxNumberOfuses;
    private String autoMaxUsesPerUser;
    private String startingDate1;
    private String startingDate2;
    private String expirationDate1;
    private String expirationDate2;
    private String basicofferingExternalKey;
    private String subscriptionOfferExternalKey;
    private static final Currency currency = Currency.USD;
    private static final String PROMO_TYPE_SUPPLEMENT = "Supplement";
    private String now;
    private FindPromotionsPage findPromotionsPage;
    private PromotionDetailsPage promotionDetailsPage;
    private PromotionsSearchResultPage promotionsSearchResultPage;
    private AddPromotionPage addPromotionPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(EditPromotionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        // Initialize a web driver object
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        // create a basic offering
        final BasicOfferingApiUtils newBasicOffering = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings addedBasicOffering = newBasicOffering.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.USB, Status.ACTIVE, UsageType.COM, null);
        basicofferingExternalKey = addedBasicOffering.getOfferings().get(0).getExternalKey();
        LOGGER.info("External key of Basic Offer: " + basicofferingExternalKey);

        // create subscription offer
        final SubscriptionPlanApiUtils newSubscriptionOffer = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings addedSubscriptionOffer = newSubscriptionOffer.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.GOV);
        subscriptionOfferExternalKey = addedSubscriptionOffer.getIncluded().getBillingPlans().get(0).getExternalKey();
        System.out.println("External Key of Subscription Offer: " + subscriptionOfferExternalKey);

        // add new Promotion only one time and will be deleted after running all the test cases which
        // are associated
        // with this promotion.
        autoCode = "AutoCode" + RandomStringUtils.randomAlphabetic(7);
        autoName = "Auto Name " + RandomStringUtils.randomAlphabetic(10);
        autoDescription = "Auto Description " + RandomStringUtils.randomAlphabetic(10);
        autoPromotionType = "Supplement";
        autoDiscountType = "Days";
        autoOfferingType = "Subscription Offers";
        autoOfferingOrSubscription = " ";
        autoValueOfDiscount = "10";
        autoStoreWide = true;
        final String autoStartingDate = "08/02/2015";
        autoExpirationDate = "08/02/2033";
        autoMaxNumberOfuses = "2";
        autoMaxUsesPerUser = "2";

        // Edit the Date in a way that promotion can never be expired.
        startingDate1 = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1);
        startingDate2 = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        expirationDate1 = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 26);
        expirationDate2 = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 27);
        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.AUDIT_LOG_DATE_FORMAT);
        final Date date = new Date();
        now = dateFormat.format(date);
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        editPromotionPage = adminToolPage.getPage(EditPromotionPage.class);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);
        findPromotionsPage = adminToolPage.getPage(FindPromotionsPage.class);
        addPromotionPage = adminToolPage.getPage(AddPromotionPage.class);

        LOGGER.info("Add Promotion Details");
        newPromotion = createPromotion(autoName, autoCode, autoDescription, getStoreIdUs(), autoPromotionType,
            autoDiscountType, autoValueOfDiscount, autoOfferingType, subscriptionOfferExternalKey, autoStoreWide,
            autoStartingDate, autoExpirationDate, autoMaxNumberOfuses, autoMaxUsesPerUser);
        addPromotionPage.add(newPromotion);
        createdPromotionId = promotionDetailsPage.getId();

    }

    /**
     * Validate the following fields can be edited. name, description, Promotional code discount, type,
     * time/amount/percentage, effective date, Offering Type, Offering External Key, expiration date, check/uncheck,
     * storeWide, check standalone, maximum number of uses, maximum uses per user along with following: 1)This Promo is
     * editing from Subscription Offer (Time) to basic Offering (Cash Amount) 2)Editing from basic offering (Amount) to
     * Subscription Offer (Time) 3)Editing from Subscription Offer (Time) to basic offering (Percentage) 4)Editing from
     * basic offering (Percentage) to Subscription Offer (Amount)
     *
     * @param name - promo name to Edit Promo
     * @param code - Promo Code to use in Edit Promo
     * @param description - description of promo to be edited
     * @param storeId - store will be the same
     * @param promotionType - type of promo to be edited
     * @param discountSupplementSubType - discount type of promo to edit
     * @param value - value of amount, percentage or time to be edited
     * @param offeringType - type of offering in promotion to be edited
     * @param externalKeyOffer - external key of offering in promotion to be edited
     * @param storeWide - storewide to edit as true or false.
     * @param startDate - starting date of promo to edit
     * @param expireDate - end date of promo to edit
     * @param maxUses - maximum use of promo to edit
     * @param maxUsesPerUser - maximum use of promo per use to edit
     * @Result: Page Displays with New Edited Fields.
     */
    @Test(dataProvider = "editPromoWithOffer", priority = 1)
    public void verifyEditingFieldsOfPromotionWithOffers_DEFECT_BIC6031(final String name, final String code,
        final String description, final String storeId, final String promotionType,
        final String discountSupplementSubType, final String value, final String offeringType,
        final String externalKeyOffer, final boolean storeWide, final String startDate, final String expireDate,
        final String maxUses, final String maxUsesPerUser) {
        // Find the Promotion which is Added above by the code
        final GenericDetails promoDetail = adminToolPage.getPage(GenericDetails.class);
        newPromotion.setId(promoDetail.getValueByField("ID"));

        // editPromotionPage.edit();
        final Promotion oldPromotion = editPromotionPage.getDetails();
        editPromotionPage.edit();
        // Start editing the Promotion which is Added
        final Promotion editPromotion =
            createPromotion(name, code, description, storeId, promotionType, discountSupplementSubType, value,
                offeringType, externalKeyOffer, storeWide, startDate, expireDate, maxUses, maxUsesPerUser);
        editPromotionPage.editPromotion(editPromotion);
        promoDetails = editPromotionPage.getDetails();
        // Compare the values of fields. Fields of First Editing should not be equal to the fields of
        // Added promotion
        // Except ID. Fields of Second Editing should not be equal to the fields of First Editing and
        // than so on.
        autoCode = promotionDetailsPage.getPromotionCode();
        autoName = promotionDetailsPage.getName();
        autoDescription = promotionDetailsPage.getDescription();
        autoPromotionType = promotionDetailsPage.getType();
        autoDiscountType = promotionDetailsPage.getDiscountType();
        autoOfferingType = promotionDetailsPage.getType();
        if (promotionDetailsPage.isBasicOfferingPresent()) {
            autoOfferingOrSubscription = PelicanConstants.BASIC_OFFERING_TYPE.toUpperCase();
        } else {
            autoOfferingOrSubscription = PelicanConstants.SUBSCRIPTION_OFFERS_TYPE.toUpperCase();
        }
        if (autoPromotionType.equals(PelicanConstants.PROMOTION_TYPE_DISCOUNT)) {
            if (autoDiscountType.equals(PelicanConstants.PROMOTION_DISCOUNT_TYPE_CASH)) {
                LOGGER.info(promotionDetailsPage.getAmount());
                autoValueOfDiscount = promotionDetailsPage.getAmount();
            } else {
                autoValueOfDiscount = promotionDetailsPage.getPercentage();
            }
        } else {
            autoValueOfDiscount = promotionDetailsPage.getTimePeriodCount();
        }
        autoStoreWide = promotionDetailsPage.getStoreWide().equals("YES");
        autoExpirationDate = promotionDetailsPage.getEffectiveDateRange();
        autoMaxNumberOfuses = promotionDetailsPage.getMaximumNumberOfUses();
        autoMaxUsesPerUser = promotionDetailsPage.getMaximumUsesPerUser();

        AssertCollector.assertThat("IDs are not same", promotionDetailsPage.getId(), equalTo(createdPromotionId),
            assertionErrorList);
        AssertCollector.assertThat("Unable to Edit Name", autoName, equalTo(name), assertionErrorList);
        AssertCollector.assertThat("Unable to Edit PromotionCode", autoCode, equalTo(code), assertionErrorList);
        AssertCollector.assertThat("Unable checked Or Unchecked StoreWide", autoStoreWide, equalTo(storeWide),
            assertionErrorList);
        AssertCollector.assertThat("Unable to Edit description", autoDescription, equalTo(description),
            assertionErrorList);
        AssertCollector.assertThat("Promotion Type is incorrect", promotionDetailsPage.getType(),
            equalTo(promotionType), assertionErrorList);

        if (autoPromotionType.equals(PelicanConstants.PROMOTION_TYPE_DISCOUNT)) {
            if (autoDiscountType.equals(PelicanConstants.PROMOTION_DISCOUNT_TYPE_CASH)) {
                AssertCollector.assertThat("Unable to Edit Amount", promotionDetailsPage.getAmount(),
                    equalTo(value + ".00 " + currency), assertionErrorList);
                AssertCollector.assertThat("Unable to Edit Discount/Supplement Type", autoDiscountType.substring(0, 3),
                    equalTo(discountSupplementSubType.substring(0, 3)), assertionErrorList);
            } else {
                AssertCollector.assertThat("Unable to Edit Percentage", promotionDetailsPage.getPercentage(),
                    equalTo(value + "%"), assertionErrorList);
                AssertCollector.assertThat("Unable to Edit Discount/Supplement Type", autoDiscountType,
                    equalTo(discountSupplementSubType + " " + promotionType), assertionErrorList);
            }
        } else {
            AssertCollector.assertThat("Unable to Edit Supplement Type", promotionDetailsPage.getSupplementType(),
                equalTo("Time"), assertionErrorList);
            final String timePeriodType = promoDetail.getValueByField("Time Period Type") + "S";
            AssertCollector.assertThat("Unable to Edit Time Period Type", timePeriodType,
                equalTo(StringUtils.upperCase((discountSupplementSubType))), assertionErrorList);
            AssertCollector.assertThat("Unable to Edit Time Period Count", promotionDetailsPage.getTimePeriodCount(),
                equalTo(value), assertionErrorList);
        }
        AssertCollector.assertThat("StandAlone is not YES as Default ", promotionDetailsPage.getStandalone(),
            equalTo("YES"), assertionErrorList);
        if (promotionDetailsPage.getBundledPromo().equalsIgnoreCase("NO")) {
            if (offeringType.equalsIgnoreCase("Subscription Offers")) {
                AssertCollector.assertThat(
                    "Subscription Offer is not present after editing from basic offering " + "to subscription offer ",
                    promotionDetailsPage.isSubscriptionOfferPresent(), equalTo(true), assertionErrorList);
                AssertCollector.assertThat(
                    "Basic Offering is  present after editing from subscription to basic " + "offering ",
                    promotionDetailsPage.isBasicOfferingPresent(), equalTo(false), assertionErrorList);
                AssertCollector.assertThat("Unable to Edit Basic Offerings to Subscription Offer ",
                    (promotionDetailsPage.getSubscriptionOffersDetails("Offer Name").get(0)).trim(),
                    equalTo(externalKeyOffer), assertionErrorList);
                autoOfferingType = "Subscription Offers";
            } else {
                AssertCollector.assertThat(
                    "Basic Offering is not present after editing from basic offering to " + "subscription offer",
                    promotionDetailsPage.isSubscriptionOfferPresent(), equalTo(false), assertionErrorList);
                AssertCollector.assertThat("StandAlone is not YES as Default ",
                    promotionDetailsPage.isBasicOfferingPresent(), equalTo(true), assertionErrorList);
                autoOfferingType = "Basic Offerings";
                AssertCollector.assertThat("Unable to Edit Subscription Offer to Basic Offering ",
                    (promotionDetailsPage.getBasicOfferingsNameList().get(0)).trim(), equalTo(externalKeyOffer),
                    assertionErrorList);
            }
        }
        AssertCollector.assertThat("Unable to Edit Maximum Number of Uses",
            promotionDetailsPage.getMaximumNumberOfUses(), equalTo(maxUses), assertionErrorList);
        AssertCollector.assertThat("Unable to Edit Maximum uses Per User", promotionDetailsPage.getMaximumUsesPerUser(),
            equalTo(maxUsesPerUser), assertionErrorList);
        AssertCollector.assertThat("Unable to Edit dates", promotionDetailsPage.getEffectiveDateRange(),
            equalTo("from " + startDate + " 00:00:00 UTC to " + expireDate + " 23:59:59 UTC"), assertionErrorList);
        if (!PROMO_TYPE_SUPPLEMENT.equals(promotionType)) {
            PromotionAuditLogHelper.verifyAuditDataForEditPromotion(oldPromotion, promoDetails, assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "editPromoWithOffer")
    public Object[][] getTestDataForPromo1() {
        final String random = RandomStringUtils.randomAlphabetic(5);
        return new Object[][] {
                { "Edited Name1 " + random, "EditedCode1" + random, "Edited Description 1 " + random, getStoreIdUs(),
                        "Discount", "Cash Amount", "20", "Basic Offerings", basicofferingExternalKey, false,
                        startingDate1, expirationDate1, "7", "12" },
                { "Edited Name3 " + random, "EditedCode3" + random, "Edited Description 3 " + random, getStoreIdUs(),
                        "Discount", "Percentage", "5", "Basic Offerings", basicofferingExternalKey, false,
                        startingDate2, expirationDate2, "4", "3" },
                { "Edited Name 4" + random, "EditedCode4" + random, "Edited Description 4" + random, getStoreIdUs(),
                        "Discount", "Cash Amount", "8", "Subscription Offers", subscriptionOfferExternalKey, true,
                        startingDate1, expirationDate1, "5", "6" },
                { "Edited Name2 " + random, "EditedCode2" + random, "Edited Description 2" + random, getStoreIdUs(),
                        PROMO_TYPE_SUPPLEMENT, "Months", "1", "Subscription Offers", subscriptionOfferExternalKey, true,
                        startingDate2, expirationDate2, "4", "5" }

        };
    }

    /**
     * Validate that fields should be same after canceling the editing Page.
     *
     * @Result: Page Displays with New Edited Fields.
     */
    @Test(dataProvider = "ed2PromoData", priority = 2)
    public void verifyCancelEditingPromotion_DEFECT_BIC6031(final String name, final String code,
        final String description, final String storeId, final String promoType, final String discountSupplementSubType,
        final String value, final String offeringType, final String basicOrSubscriptionOffering,
        final boolean storeWide, final String startDate, final String expireDate, final String maxUses,
        final String maxUsesPerUser) {

        promotionDetailsPage = findPromotionsPage.selectResultRowWithFindByCode(1, autoCode);
        editPromotionPage = promotionDetailsPage.clickOnEdit();

        final Promotion editPromotion =
            createPromotion(name, code, description, storeId, promoType, discountSupplementSubType, value, offeringType,
                basicOrSubscriptionOffering, storeWide, startingDate1, expirationDate2, maxUses, maxUsesPerUser);
        editPromotionPage.cancelEditOfPromotion(editPromotion);

        // Compare the values of fields. All Fields should be same and nothing should be Changed
        promoDetails = editPromotionPage.getDetails();
        AssertCollector.assertThat("Edit the Name", promoDetails.getName(), equalTo(autoName), assertionErrorList);
        AssertCollector.assertThat("Edit the PromotionCode", promoDetails.getPromotionCode(), equalTo(autoCode),
            assertionErrorList);
        AssertCollector.assertThat("Edit the Description", promoDetails.getDescription(), equalTo(autoDescription),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "ed2PromoData")
    public Object[][] getTestDataForCancelTheChangesOfPromo() {
        return new Object[][] { { "Edited Name Cancel", "EditedCodeCancel", "Edited Description Cancel", getStoreIdUk(),
                "Discount", "Percentage", "1", autoOfferingType, autoOfferingOrSubscription, false, startingDate1,
                expirationDate1, "5", "6" } };
    }

    /**
     * Validate the store can be edited.
     *
     * @Result: Page Displays with New Edited Store.
     */
    @Test(priority = 5)
    public void verifyEditStoreTest_DEFECT_BIC6031() {

        promotionDetailsPage = findPromotionsPage.selectResultRowWithFindByCode(1, autoCode);
        editPromotionPage = promotionDetailsPage.clickOnEdit();
        editPromotionPage.deleteStoreAndSetAStore(getStoreIdUk());
        editPromotionPage.addStore();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        promotionDetailsPage = editPromotionPage.clickOnUpdatePromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        AssertCollector.assertThat("Unable to Edit store", promotionDetailsPage.getLimitedToStore(),
            equalTo(getStoreExternalKeyUk()), assertionErrorList);
        final Promotion editedPromotion = editPromotionPage.getDetails();

        promotionDetailsPage.clickOnPromotionDeleteButton();
        PromotionAuditLogHelper.verifyAuditDataForPromotionStoreEditAndDelete(editedPromotion, getStoreIdUs(),
            getStoreIdUk(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate the Edit Button Exists IN New Promotion.
     *
     * @Result: Page Displays including "Edit Button"
     */
    @Test(priority = 7)
    public void verifyEditButtonOnNew_DEFECT_BIC6031() {

        // Navigate to the list of all Promotions which are New through Advanced Find Under Promotions
        promotionsSearchResultPage = findPromotionsPage.findByAdvancedFind(null, null, Status.NEW, null, null);
        final int promotionCount = promotionsSearchResultPage.getTotalItems();
        int promotionIndex;
        LOGGER.info("PROMOTION COUNT: " + promotionCount);

        // The promotion(NEW) will be selected from first Page only.Each Page shows up to 20 Promotions.
        final Random index = new Random();
        if (promotionCount <= 20) {
            promotionIndex = index.nextInt(promotionCount);
        } else {
            promotionIndex = index.nextInt(19);
        }
        LOGGER.info("Random index:" + String.valueOf(promotionIndex));
        getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        promotionDetailsPage = promotionsSearchResultPage.selectResultRow((promotionIndex + 1));

        // Verify the existence of Edit Button on the Page of new promotion
        AssertCollector.assertThat("Edit button does not exist", promotionDetailsPage.isEditButtonPresent(),
            equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate the Edit Button Doesn't Exists In Active Promotion.
     *
     * @Result: Page Displays Without Edit Button.
     */
    @Test(priority = 3)
    public void verifyEditButtonOnActive_DEFECT_BIC6031() {

        // Navigate to the list of all Promotions which are Active through Advanced Find Under
        // Promotions
        promotionsSearchResultPage = findPromotionsPage.findByAdvancedFind(null, null, Status.ACTIVE, null, null);
        final int promotionCount = promotionsSearchResultPage.getTotalItems();
        LOGGER.info("PROMOTION COUNT: " + promotionCount);
        int promotionIndex;

        // The promotion(ACTIVE) will be selected from first Page only.Each Page shows up to 20
        // Promotions.
        final Random index = new Random();
        if (promotionCount <= 20) {
            promotionIndex = index.nextInt(promotionCount);
        } else {
            promotionIndex = index.nextInt(19);
        }
        LOGGER.info("Random index:" + promotionIndex);
        promotionDetailsPage = promotionsSearchResultPage.selectResultRow(promotionIndex + 1);

        // Verify the existence of Edit Button on the Page of active promotion
        AssertCollector.assertThat("Edit button exist", promotionDetailsPage.isEditButtonPresent(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate the Edit Button Doesn't Exists In Cancelled Promotion.
     *
     * @Result: Page Displays Without Edit Button.
     */
    @Test(priority = 4)
    public void verifyEditButtonOnCancelled_DEFECT_BIC6031() {

        // Navigate to the list of all Promotions which are Cancelled through Advanced Find Under
        // Promotions
        promotionsSearchResultPage = findPromotionsPage.findByAdvancedFind(null, null, Status.CANCELLED, null, null);
        final int promotionCount = promotionsSearchResultPage.getTotalItems();
        LOGGER.info("PROMOTION COUNT: " + promotionCount);
        int promotionIndex;

        // The promotion(CANCELLED) will be selected from first Page only.Each Page shows up to 20
        // Promotions.
        final Random index = new Random();
        if (promotionCount <= 20) {
            promotionIndex = index.nextInt(promotionCount);
        } else {
            promotionIndex = index.nextInt(19);
        }
        LOGGER.info("Random index:" + promotionIndex + 1);
        promotionDetailsPage = promotionsSearchResultPage.selectResultRow(promotionIndex);

        // Verify the existence of Edit Button on the Page of cancelled promotion
        AssertCollector.assertThat("Edit button exist", promotionDetailsPage.isEditButtonPresent(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate the Edit Button Doesn't Exist In Expired Promotion.
     *
     * @Result: Page Display Without Edit Button.
     */
    @Test(priority = 8)
    public void verifyEditButtonOnExpired_DEFECT_BIC6031() {

        // Navigate to the list of all Promotions which are Expired through Advanced Find Under
        // Promotions
        promotionsSearchResultPage = findPromotionsPage.findByAdvancedFind(null, null, Status.EXPIRED, null, null);
        final int promotionCount = promotionsSearchResultPage.getTotalItems();
        LOGGER.info("PROMOTION COUNT: " + promotionCount);
        int promotionIndex;

        // The promotion(EXPIRED) will be selected from first Page only.Each Page shows up to 20
        // Promotions.
        final Random index = new Random();
        if (promotionCount <= 20) {
            promotionIndex = index.nextInt(promotionCount);
        } else {
            promotionIndex = index.nextInt(19);
        }
        LOGGER.info("Random index:" + promotionIndex);
        promotionDetailsPage = promotionsSearchResultPage.selectResultRow(promotionIndex + 1);

        // Verify the existence of Edit Button on the Page of expired promotion
        AssertCollector.assertThat("Edit button exist", promotionDetailsPage.isEditButtonPresent(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate the store can be edited.
     *
     * @Result discount amount promo is first created, then the amount is modified and finally the promo is deleted.
     */
    @Test(priority = 6)
    public void verifyEditPriceDiscountAmountPromoTest_DEFECT_BIC6031() {
        LOGGER.info("Adding ACTIVE discount amount type promo");

        // Add a discount amount promotion
        final String promoCodeForActive = "AMTPROMO" + new Random().nextInt(999999);
        final Promotion promotion = createPromotion("testAmtPromo", promoCodeForActive, "testAmtPromoDescription",
            getStoreIdUs(), "Discount", "Cash Amount", "10", "Subscription Offers", subscriptionOfferExternalKey, false,
            startingDate2, expirationDate2, "5", "6");
        addPromotionPage.add(promotion);
        // Get promotion details page after adding a promotion
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        final String promoIdForActive = promotionDetailsPage.getId();
        LOGGER.info("PromoId of the discount amount promo in ACTIVE status is: " + promoIdForActive);

        final Promotion oldPromotion = editPromotionPage.getDetails();
        oldPromotion.setValue(promotion.getValue());
        editPromotionPage = promotionDetailsPage.clickOnEdit();

        final String modifiedDiscountAmt = "100";
        editPromotionPage.editAmountEnteredForPricelists(modifiedDiscountAmt);
        editPromotionPage.clickOnUpdatePromotion();
        promotion.setValue(modifiedDiscountAmt);

        promotionDetailsPage.clickOnPromotionDeleteButton();
        PromotionAuditLogHelper.verifyAuditDataForDiscountAmountPromotion(oldPromotion, promotion, now,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to test cash amount input field is displayed
     *
     * @result cash amount field is hidden or displayed on the page.
     */
    @Test(dependsOnMethods = "verifyEditingFieldsOfPromotionWithOffers_DEFECT_BIC6031", priority = 9)
    public void testWhetherCashAmountFieldIsDisplayedOnPageInEditPromotion_DEFECT_BIC6031() {

        // Adding a discount percentage promotion in new state
        final String promoType = PelicanConstants.PROMOTION_TYPE_DISCOUNT;
        final String discountType = PelicanConstants.PERCENTAGE_DISCOUNT_TYPE;
        final String promoCode = "AutoCode" + RandomStringUtils.randomAlphabetic(7);
        final String discountValue = "";
        final String startingDate = "5/10/2017";
        newPromotion = createPromotion(autoName, promoCode, autoDescription, getStoreIdUs(), promoType, discountType,
            discountValue, autoOfferingType, subscriptionOfferExternalKey, autoStoreWide, startingDate, startingDate,
            autoMaxNumberOfuses, autoMaxUsesPerUser);
        addPromotionPage.add(newPromotion);

        promotionDetailsPage.clickOnEdit();
        addPromotionPage.selectDiscountType(PromotionUtils.DISCOUNT_TYPE_AMOUNT);
        addPromotionPage.setActivateNow(true);
        addPromotionPage.clickOnAddPromotion();
        final boolean isCashAmountInputFieldPresent = addPromotionPage.isCashAmountFieldPresentOnPage();
        LOGGER.info("Is Field Present: " + isCashAmountInputFieldPresent);

        AssertCollector.assertTrue("Cash Amount Input field is hidden on the page", isCashAmountInputFieldPresent,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * @param name - Promo name which will be added and than edited
     * @param code - Promo code which will be added and than edited
     * @param description - Promo description which will be added and than edited
     * @param storeId - Store which will be added in Promo with id and than edited
     * @param promoType - Promo type either Discount or Supplement which will be added and than edited
     * @param discountSupplementSubType - Promo subType i.e Amount/percentage/timePeriodType which will be added and
     *        than edited
     * @param value - Promo value in the form of amount/percentage/weeks,days or month which will be added and than
     *        edited
     * @param offeringType - Offering type i.e basic or subscription which will be added in Promo and than edited
     * @param basicOrSubscriptionOffering-Externalkey of basic or subscription offer which will be added and than edited
     * @param storeWide - Promo added or Edited with store wide or not
     * @param effectiveDate - Promo which will be added with starting date and than edited
     * @param expireDate - Promo which will be added with end date and than edited
     * @param maxUses - Promo which will be added with maximum number of uses and than edited
     * @param maxUsesPerUser - Promo which will be added with maximum number of uses per user and than edited
     */
    private Promotion createPromotion(final String name, final String code, final String description,
        final String storeId, final String promoType, final String discountSupplementSubType, final String value,
        final String offeringType, final String basicOrSubscriptionOffering, final boolean storeWide,
        final String effectiveDate, final String expireDate, final String maxUses, final String maxUsesPerUser) {

        final Promotion newPromotion = new Promotion();
        newPromotion.setName(name);
        newPromotion.setDescription(description);
        newPromotion.setStoreWide(storeWide);
        newPromotion.setPromotionCode(code);
        newPromotion.setStoreId(storeId);
        newPromotion.setPromotionType(promoType);
        newPromotion.setDiscountType(discountSupplementSubType);
        newPromotion.setOfferingType(offeringType);
        newPromotion.setBasicOrSubscriptionOffering(basicOrSubscriptionOffering);
        newPromotion.setValue(value);
        if ("Cash Amount".equals(discountSupplementSubType)) {
            newPromotion.setAmount(value);
        }
        newPromotion.setEffectiveDate(effectiveDate);
        newPromotion.setExpirationDate(expireDate);
        newPromotion.setMaxUses(maxUses);
        newPromotion.setMaxUsesPerUser(maxUsesPerUser);
        return newPromotion;
    }
}
