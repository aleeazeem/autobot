package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import java.util.ArrayList;
import java.util.List;

public class HelperForPromotions {
    private static final String TIME_PERIOD_TYPE = "Weeks";
    private static final Integer BASIC_OFFERING1_QUANTITY = 3;
    private static final Integer BASIC_OFFERING2_QUANTITY = 6;
    private static final Integer INVALID_OFFERING1_QUANTITY = -2;
    private static final Integer INVALID_OFFERING2_QUANTITY = 1002;
    private static final Integer SUBSCRIPTION_OFFER1_QUANTITY = 9;
    private static final Integer SUBSCRIPTION_OFFER2_QUANTITY = 12;
    private static final String promotionName = "Test";
    private static final String START_HOUR = "03";
    private static final String START_MINUTE = "08";
    private static final String START_SECOND = "56";
    private static final String END_HOUR = "09";
    private static final String END_MINUTE = "28";
    private static final String END_SECOND = "26";

    public static void assertionsForPromotions(final JPromotion promotion,
        final PromotionDetailsPage promotionDetailsPage, final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect Promotion Id", promotionDetailsPage.getId(),
            equalTo(promotion.getData().getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Name", promotionDetailsPage.getName(),
            equalTo(promotion.getData().getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Description", promotionDetailsPage.getDescription(),
            equalTo(promotion.getData().getDescription()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Code", promotionDetailsPage.getPromotionCode(),
            equalTo(promotion.getData().getCustomPromoCode()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Type", promotionDetailsPage.getType(),
            equalTo(promotion.getData().getPromotionType().getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Status", promotionDetailsPage.getState(),
            equalTo(promotion.getData().getStatus().toString()), assertionErrorList);

        if (promotion.getData().getIsBundledPromo()) {
            AssertCollector.assertTrue("Bundle Promo Offer Details",
                promotionDetailsPage.isBundlePromoOfferDetailsPresent(), assertionErrorList);
        } else {
            // For Non-Bundle promo these fields should not be present in the offer details page
            AssertCollector.assertFalse("Non-Bundle Promo Offer Details",
                promotionDetailsPage.isBundlePromoOfferDetailsPresent(), assertionErrorList);
        }
    }

    /**
     * @param name
     * @param storeId
     * @param discountType
     * @param promotionType
     * @param offeringType
     * @param promotionDiscount
     * @param effectiveStartDate
     * @param expirationDate
     * @param maxUses
     * @param maxUsesPerUser
     * @param status
     * @param timeInHours
     * @param timeInMinutes
     * @param timeInSeconds
     * @param endTimeInHours
     * @param endTimeInMinutes
     * @param endTimeInSeconds
     * @param numberOfBillingCycles
     * @param bundled
     * @param assertionErrorList
     * @return
     */
    public static Promotion assignFieldsToPromotionObject(final String name, final String storeId,
        final String discountType, final String promotionType, final String offeringType,
        final String promotionDiscount, final String effectiveStartDate, final String expirationDate,
        final String maxUses, final String maxUsesPerUser, final String status, final String timeInHours,
        final String timeInMinutes, final String timeInSeconds, final String endTimeInHours,
        final String endTimeInMinutes, final String endTimeInSeconds, final String numberOfBillingCycles,
        final boolean bundled, final List<AssertionError> assertionErrorList) {
        final Promotion newPromotion = new Promotion();
        newPromotion.setName(name);
        if (storeId != null) {
            newPromotion.setStoreId(storeId);
        }
        newPromotion.setDiscountType(discountType);
        newPromotion.setBundled(bundled);
        if ("Discount".equalsIgnoreCase(discountType)) {
            newPromotion.setPromotionType(promotionType);
            if (offeringType.equalsIgnoreCase("Basic Offerings")) {
                newPromotion.setBasicOfferings(offeringType);
            } else if (offeringType.equalsIgnoreCase("Subscription Offers")) {
                newPromotion.setSubscriptionOfferings(offeringType);
            } else if (offeringType.equalsIgnoreCase("Both")) {
                newPromotion.setBasicOfferings(offeringType);
                newPromotion.setSubscriptionOfferings(offeringType);
            }
            if (promotionType.equalsIgnoreCase("Cash Amount")) {
                newPromotion.setCashAmount(promotionDiscount);
            } else if (promotionType.equalsIgnoreCase("Percentage")) {
                newPromotion.setPercentageAmount(promotionDiscount);
            }
        } else if ("Supplement".equalsIgnoreCase(discountType)) {
            newPromotion.setSupplementType(promotionType);
            newPromotion.setTimePeriodCount(promotionDiscount);
            newPromotion.setTimePeriodType(TIME_PERIOD_TYPE);
            if (offeringType.equalsIgnoreCase("Subscription Offers")) {
                newPromotion.setSubscriptionOfferings(offeringType);
            }
        }
        newPromotion.setEffectiveDate(effectiveStartDate);
        newPromotion.setExpirationDate(expirationDate);
        newPromotion.setMaxUses(maxUses);
        newPromotion.setMaxUsesPerUser(maxUsesPerUser);
        newPromotion.setActivateStatus(Status.valueOf(status));
        newPromotion.setTimeInHours(timeInHours);
        newPromotion.setTimeInMinutes(timeInMinutes);
        newPromotion.setTimeInSeconds(timeInSeconds);
        newPromotion.setExpirationTimeInHours(endTimeInHours);
        newPromotion.setExpirationTimeInMinutes(endTimeInMinutes);
        newPromotion.setExpirationTimeInSeconds(endTimeInSeconds);
        newPromotion.setNumberOfBillingCycles(numberOfBillingCycles);

        if (promotionType != null) {
            if (promotionType.equalsIgnoreCase("Cash Amount")) {
                newPromotion.setCashAmount(promotionDiscount);
            } else if (promotionType.equalsIgnoreCase("Percentage")) {
                newPromotion.setPercentageAmount(promotionDiscount);
            }
        } else if ("Supplement".equalsIgnoreCase(discountType)) {
            newPromotion.setSupplementType(promotionType);
            newPromotion.setTimePeriodCount(promotionDiscount);
            newPromotion.setTimePeriodType(TIME_PERIOD_TYPE);
            if (offeringType.equalsIgnoreCase("Subscription Offers")) {
                newPromotion.setSubscriptionOfferings(offeringType);
            }

        }
        newPromotion.setEffectiveDate(effectiveStartDate);
        newPromotion.setExpirationDate(expirationDate);
        newPromotion.setMaxUses(maxUses);
        newPromotion.setMaxUsesPerUser(maxUsesPerUser);
        newPromotion.setActivateStatus(Status.valueOf(status));
        newPromotion.setTimeInHours(timeInHours);
        newPromotion.setTimeInMinutes(timeInMinutes);
        newPromotion.setTimeInSeconds(timeInSeconds);
        newPromotion.setExpirationTimeInHours(endTimeInHours);
        newPromotion.setExpirationTimeInMinutes(endTimeInMinutes);
        newPromotion.setExpirationTimeInSeconds(endTimeInSeconds);
        newPromotion.setNumberOfBillingCycles(numberOfBillingCycles);

        return newPromotion;
    }

    /**
     * This is the method to assign Fields to Promotion Object
     *
     * @param storeIdList
     * @param numberOfBillingCycles
     * @param discountPromotionType
     * @param discountType
     * @param offeringType
     * @param discountValue
     * @param startDate
     * @param endDate
     * @param maximumUses
     * @param maximumUsesByUser
     * @param activateStatus
     * @param startHourForPromotion
     * @param startMinuteForPromotion
     * @param startSecondForPromotion
     * @param endHourForPromotion
     * @param endMinuteForPromotion
     * @param endSecondForPromotion
     * @param bundled
     * @param assertionErrorList
     * @return
     */
    public static Promotion getPromotion(final List<String> storeIdList, final String numberOfBillingCycles,
        final String discountPromotionType, final String discountType, final String offeringType,
        final String discountValue, final String startDate, final String endDate, final String maximumUses,
        final String maximumUsesByUser, final String activateStatus, final String startHourForPromotion,
        final String startMinuteForPromotion, final String startSecondForPromotion, final String endHourForPromotion,
        final String endMinuteForPromotion, final String endSecondForPromotion, final boolean bundled,
        final List<AssertionError> assertionErrorList) {

        String finalStoreId = null;
        if (storeIdList != null && !storeIdList.isEmpty()) {
            finalStoreId = storeIdList.toString().replace("[", "").replace("]", "");
        }
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
     * @param basicOfferingExternalKeysList
     */
    public static Promotion addDiscountPromo(final Promotion promotion, final boolean storeWide,
        final List<Integer> quantityForBasicOfferingList, final List<Integer> quantityForSubscriptionOfferingList,
        final List<Boolean> applyDiscountForBasicOfferingList,
        final List<Boolean> applyDiscountForSubscriptionOfferingList, final List<String> basicOfferingExternalKeysList,
        final List<String> subscriptionOfferExternalKeysList) {

        promotion.setPriceListExternalKey("select-pricelist");
        promotion.setStoreWide(storeWide);
        if (!promotion.isBundled()
            && (PelicanConstants.BASIC_OFFERING_TYPE).equalsIgnoreCase(promotion.getBasicOfferings())) {
            promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeysList);
        } else if (!promotion.isBundled()
            && (PelicanConstants.SUBSCRIPTION_OFFERS_TYPE).equalsIgnoreCase(promotion.getSubscriptionOfferings())) {
            promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferExternalKeysList);
        } else {
            if (null == basicOfferingExternalKeysList || !basicOfferingExternalKeysList.isEmpty()) {
                promotion.setBasicOfferingsExternalKey(basicOfferingExternalKeysList);
                promotion.setQuantityOfBasicOfferingsList(quantityForBasicOfferingList);
                promotion.setApplyDiscountForBasicOfferingsList(applyDiscountForBasicOfferingList);
            }
            if (null == subscriptionOfferExternalKeysList || !subscriptionOfferExternalKeysList.isEmpty()) {
                promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferExternalKeysList);
                promotion.setQuantityOfSubscriptionOfferingsList(quantityForSubscriptionOfferingList);
                promotion.setApplyDiscountForSubscriptionOfferingsList(applyDiscountForSubscriptionOfferingList);
            }
        }

        return promotion;
    }

    /**
     * Method to validate common assertions for new promotion without entity.
     *
     * @param expectedStartDate
     * @param expectedEndDate
     * @param expectedStatus
     * @param expectedMaximumUses
     * @param expectedMaximumUsesPerUser
     * @param actualEffectiveDate
     * @param actualStatus
     * @param actualMaximumUses
     * @param expectedStartHour TODO
     * @param expectedStartMinute TODO
     * @param expectedStartSecond TODO
     * @param expectedEndHour TODO
     * @param expectedEndMinute TODO
     * @param expectedEndSecond TODO
     * @param assertionErrorList
     * @param actualEndDate
     */
    public static void validateAllCommonAssertions(final String expectedStartDate, final String expectedEndDate,
        final String expectedStatus, final String expectedMaximumUses, final String expectedMaximumUsesPerUser,
        final String actualEffectiveDate, final String actualStatus, final String actualMaximumUses,
        final String expectedStartHour, final String expectedStartMinute, final String expectedStartSecond,
        final String expectedEndHour, final Object expectedEndMinute, final Object expectedEndSecond,
        final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect promotion max uses", expectedMaximumUses, equalTo(actualMaximumUses),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion max uses per user", expectedMaximumUsesPerUser,
            equalTo(expectedMaximumUsesPerUser), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion start date", actualEffectiveDate.substring(5, 15),
            equalTo(expectedStartDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion expiration date", actualEffectiveDate.substring(32, 42),
            equalTo(expectedEndDate), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start hours", actualEffectiveDate.substring(16, 18),
            equalTo(expectedStartHour), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start minutes", actualEffectiveDate.substring(19, 21),
            equalTo(expectedStartMinute), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion start second", actualEffectiveDate.substring(22, 24),
            equalTo(expectedStartSecond), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration hours", actualEffectiveDate.substring(43, 45),
            equalTo(expectedEndHour), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration minutes", actualEffectiveDate.substring(46, 48),
            equalTo(expectedEndMinute), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion expiration seconds", actualEffectiveDate.substring(49, 51),
            equalTo(expectedEndSecond), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion state", actualStatus, equalTo(expectedStatus),
            assertionErrorList);
    }

    /**
     * Method to validate common assertions for new promotion
     *
     * @param newPromotion
     * @param startDate
     * @param endDate
     * @param status
     * @param maximumUses
     * @param maximumUsesPerUser
     * @param assertionErrorList
     */
    public static void validateAllCommonAssertions(final Promotion newPromotion, final String startDate,
        final String endDate, final Status status, final String maximumUses, final String maximumUsesPerUser,
        final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect promotion max uses", newPromotion.getMaxUses(), equalTo(maximumUses),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion max uses per user", newPromotion.getMaxUsesPerUser(),
            equalTo(maximumUsesPerUser), assertionErrorList);
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
        AssertCollector.assertThat("Incorrect promotion state", newPromotion.getState(), equalTo(status.toString()),
            assertionErrorList);
    }

    /**
     * Method to validate more promotion field values
     *
     * @param newPromotion
     * @param name
     * @param expectedStore
     * @param assertionErrorList
     */
    public static void validateFewCommonAssertions(final Promotion newPromotion, final String name,
        final String expectedStore, final List<AssertionError> assertionErrorList) {
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
     * Method to validate some promotion field values without entity.
     *
     * @param expectedName
     * @param actualName
     * @param actualPromotionCode
     * @param assertionErrorList
     * @param expectedStore
     * @param actualStore
     */
    public static void validateFewCommonAssertions(final String expectedName, final String actualName,
        final String actualPromotionCode, final List<AssertionError> assertionErrorList) {
        AssertCollector.assertThat("Incorrect promotion name", actualName, equalTo(expectedName), assertionErrorList);

        AssertCollector.assertThat("Incorrect promo code", actualPromotionCode, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect promotion code length", actualPromotionCode.length(),
            equalTo(PelicanConstants.PROMOTION_CODE_LENGTH), assertionErrorList);
    }

    /**
     * This is the method to return the quantity for basic offering
     *
     * @return List<Integer>
     */
    public static List<Integer> getQuantityListForBasicOffering() {

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
    public static List<Integer> getQuantityListForSubscriptionOffering() {

        final List<Integer> quantityToSubscriptionOfferingList = new ArrayList<>();
        quantityToSubscriptionOfferingList.add(SUBSCRIPTION_OFFER1_QUANTITY);
        quantityToSubscriptionOfferingList.add(SUBSCRIPTION_OFFER2_QUANTITY);

        return quantityToSubscriptionOfferingList;
    }

    /**
     * This is the method to return the invalid quantity for basic offering
     *
     * @return List<Integer>
     */
    public static List<Integer> getInvalidQuantityListForBasicOffering() {

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
    public static List<Integer> getInvalidQuantityListForSubscriptionOffering() {

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
    public static List<Boolean> getApplyDiscountListForBasicOffering() {

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
    public static List<Boolean> getApplyDiscountListForSubscriptionOffering() {

        final List<Boolean> applyDiscountForSubscriptionOfferingList = new ArrayList<>();
        applyDiscountForSubscriptionOfferingList.add(PelicanConstants.FALSE_VALUE);
        applyDiscountForSubscriptionOfferingList.add(PelicanConstants.TRUE_VALUE);

        return applyDiscountForSubscriptionOfferingList;
    }

    /**
     * This is a method to add more offerings to a promotion
     *
     * @param promotion
     * @param offeringExternalKey1
     * @param offeringExternalKey2
     * @param offeringExternalKey3
     * @param offeringExternalKey4
     * @param offeringExternalKey5
     * @param offeringExternalKey6
     * @return
     */
    public static Promotion addMoreBasicOfferingsToPromotion(final Promotion promotion,
        final String offeringExternalKey1, final String offeringExternalKey2, final String offeringExternalKey3,
        final String offeringExternalKey4, final String offeringExternalKey5, final String offeringExternalKey6) {
        final List<String> basicOfferingExternalKeyList = new ArrayList<>();
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
     * @param promotion
     * @param offeringExternalKey1
     * @param offeringExternalKey2
     * @param offeringExternalKey3
     * @param offeringExternalKey4
     * @param offeringExternalKey5
     * @param offeringExternalKey6
     * @return
     */
    public static Promotion addMoreSubscriptionOfferingsToPromotion(final Promotion promotion,
        final String offeringExternalKey1, final String offeringExternalKey2, final String offeringExternalKey3,
        final String offeringExternalKey4, final String offeringExternalKey5, final String offeringExternalKey6) {
        final List<String> subscriptionOfferExternalKeyList = new ArrayList<>();
        subscriptionOfferExternalKeyList.add(offeringExternalKey1);
        subscriptionOfferExternalKeyList.add(offeringExternalKey2);
        subscriptionOfferExternalKeyList.add(offeringExternalKey3);
        subscriptionOfferExternalKeyList.add(offeringExternalKey4);
        subscriptionOfferExternalKeyList.add(offeringExternalKey5);
        subscriptionOfferExternalKeyList.add(offeringExternalKey6);
        promotion.setSubscriptionOfferingsExternalKey(subscriptionOfferExternalKeyList);

        return promotion;
    }
}
