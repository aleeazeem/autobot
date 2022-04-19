package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.PromotionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.ExtendPromotionPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.FindPromotionsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This class tests Extend Promotion functionality in AT.
 *
 * @author jains
 */

public class ExtendPromotionTest extends SeleniumWebdriver {

    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private List<JStore> storeList;
    private List<Offerings> basicOfferingsList;
    private List<Offerings> subscriptionOfferingsList;
    private PromotionUtils promoUtils;
    private AdminToolPage adminToolPage;
    private JPromotion promotion;
    private static final String expirationDateErrorMessage =
        "To extend an active promotion, Expiration Date must be later than current date";
    private static final Date expirationDate = DateTimeUtils.getUTCFutureExpirationDate();
    private FindPromotionsPage findPromotionsPage;
    private PromotionDetailsPage promotionDetailsPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendPromotionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        promoUtils = new PromotionUtils(getEnvironmentVariables());
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
        final String externalKeyOfPriceListStore2 = store2.getIncluded().getPriceLists().get(0).getExternalKey();
        final String externalKeyOfPriceListStore3 = store3.getIncluded().getPriceLists().get(0).getExternalKey();

        // create multiple basic offerings
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(externalKeyOfPriceListStore1,
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        final Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(externalKeyOfPriceListStore2,
            OfferingType.PERPETUAL, MediaType.USB, Status.ACTIVE, UsageType.COM, null);
        // adding to basic offerings list
        basicOfferingsList = new ArrayList<>();
        basicOfferingsList.add(basicOffering1);
        basicOfferingsList.add(basicOffering2);

        // creating multiple subscription offerings for bic and meta
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceListStore1, OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings bicSubscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceListStore2, OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final Offerings metaSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceListStore3, OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        // adding to subscription offerings list
        subscriptionOfferingsList = new ArrayList<>();
        subscriptionOfferingsList.add(bicSubscriptionOffering1);
        subscriptionOfferingsList.add(bicSubscriptionOffering2);
        subscriptionOfferingsList.add(metaSubscriptionOffering1);

        // open a chrome browser and login into Admin Tool
        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPromotionsPage = adminToolPage.getPage(FindPromotionsPage.class);
    }

    /**
     * Verify, active Promotion can be extended .
     */
    @Test(dataProvider = "extendActivePromotion")
    public void testExtendActivePromotion(final PromotionType promotionType, final List<JStore> store,
        final List<Offerings> offerings, final String promoCode, final boolean isStorewide, final Status status,
        final String discountPercent, final String discountAmount, final String timePeriodCount,
        final String timePeriodType, final Integer numberOfBillingCycles, final String maxUses,
        final String maxUsesPerUser, final String newMaxUses, final String newMaxUsesPerUser,
        final String expectedDiscountType) throws ParseException {
        // add promotion
        promotion = promoUtils.addPromotion(promotionType, store, offerings, promoCode, isStorewide, status,
            discountPercent, discountAmount, expirationDate, timePeriodCount, timePeriodType, numberOfBillingCycles,
            maxUses, maxUsesPerUser);
        final String pastEffectiveDate = DateTimeUtils.getPreviousMonthDateAsString();
        final String futureEffectiveDate = DateTimeUtils.getNextMonthDateAsString();

        final String promotionId = promotion.getData().getId();
        // Find promotion in AT
        promotionDetailsPage = findPromotionsPage.findById(promotionId);
        // Extend active promotion
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        ExtendPromotionPage extendPromotionPage = promotionDetailsPage.extendActivePromotion();
        // setting past date to get the error message
        extendPromotionPage.setEffectiveEndDate(pastEffectiveDate);
        extendPromotionPage.extendActivePromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String expirationDateError = extendPromotionPage.getExpirationDateError();

        // validating error message for past expiration date
        AssertCollector.assertThat("Future expiration date error message is not correct.", expirationDateError,
            equalTo(expirationDateErrorMessage), assertionErrorList);

        // Setting future effective date
        promotionDetailsPage = findPromotionsPage.findById(promotionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        extendPromotionPage = promotionDetailsPage.extendActivePromotion();
        // Update all editable fields on extend promotion page
        extendPromotionPage.setMaxNumberOfUses(newMaxUses);
        extendPromotionPage.setMaxUsesPerUser(newMaxUsesPerUser);
        extendPromotionPage.setEffectiveEndDate(futureEffectiveDate);
        extendPromotionPage.extendActivePromotion();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        AssertCollector.assertThat("Promotion id is not correct.", promotionDetailsPage.getId(), equalTo(promotionId),
            assertionErrorList);
        AssertCollector.assertThat("Promotion code is not correct.", promotionDetailsPage.getPromotionCode(),
            equalTo(promoCode), assertionErrorList);
        AssertCollector.assertThat("Promotion status is not correct.", promotionDetailsPage.getState(),
            equalTo(status.toString()), assertionErrorList);
        AssertCollector.assertThat("Maximum number of uses is not correct.",
            promotionDetailsPage.getMaximumNumberOfUses(), equalTo(newMaxUses), assertionErrorList);
        AssertCollector.assertThat("Maximum Uses per user is not correct.",
            promotionDetailsPage.getMaximumUsesPerUser(), equalTo(newMaxUsesPerUser), assertionErrorList);
        AssertCollector.assertThat("Effective end date is not correct.",
            promotionDetailsPage.getEffectiveDateRange().substring(32, 42), equalTo(futureEffectiveDate),
            assertionErrorList);
        PromotionAuditLogHelper.verifyAuditDataForExtendPromotion(promotion, promotionDetailsPage, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, new or cancel promotion can not be extended.
     */
    @Test(dataProvider = "extendNewOrCancelPromotion")
    public void testExtendNewOrCancelPromotion(final PromotionType promotionType, final List<JStore> store,
        final List<Offerings> offerings, final String promoCode, final boolean isStorewide, final Status status,
        final String discountPercent, final String discountAmount, final String timePeriodCount,
        final String timePeriodType, final Integer numberOfBillingCycles, final String maxUses,
        final String maxUsesPerUser) {
        // add promotion
        promotion = promoUtils.addPromotion(promotionType, store, offerings, promoCode, isStorewide, status,
            discountPercent, discountAmount, expirationDate, timePeriodCount, timePeriodType, numberOfBillingCycles,
            maxUses, maxUsesPerUser);

        final String promotionId = promotion.getData().getId();
        // Find promotion in AT
        promotionDetailsPage = findPromotionsPage.findById(promotionId);
        // checking extend promotion button for new promotion
        LOGGER.info("Verifying extend active promotion button for New promotion.");
        AssertCollector.assertThat("Extend active promotion button should not be displayed for new promotion.",
            promotionDetailsPage.isExtendActivePromotionButtonDisplayed(), equalTo(false), assertionErrorList);

        // activating and canceling promotion
        promotionDetailsPage.activatePromotion();
        promotionDetailsPage.popUpWindowConfirm();
        promotionDetailsPage.cancelPromotion();
        promotionDetailsPage.popUpWindowConfirm();
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
        // checking extend promotion button for cancel promotion
        LOGGER.info("Verifying extend active promotion button for Cancel promotion.");
        AssertCollector.assertThat("Extend active promotion button should not be displayed for canceled promotion.",
            promotionDetailsPage.isExtendActivePromotionButtonDisplayed(), equalTo(false), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass different promotion type for basic and subscription offering type to
     * testExtendActivePromotion method
     */
    @DataProvider(name = "extendActivePromotion")
    public Object[][] getTestDataForExtendActivePromotion() {
        return new Object[][] {
                { PromotionType.DISCOUNT_AMOUNT, storeList, basicOfferingsList, promoUtils.getRandomPromoCode(), false,
                        Status.ACTIVE, null, "10", null, null, 2, "4", "5", "5", "6", "Cash Discount" },
                { PromotionType.DISCOUNT_PERCENTAGE, storeList, basicOfferingsList, promoUtils.getRandomPromoCode(),
                        false, Status.ACTIVE, "5", null, null, null, 2, "4", "5", "5", "6", "Percentage Discount" },
                { PromotionType.DISCOUNT_AMOUNT, storeList, subscriptionOfferingsList, promoUtils.getRandomPromoCode(),
                        false, Status.ACTIVE, null, "10", null, null, 2, "4", "5", "5", "6", "Cash Discount" },
                { PromotionType.SUPPLEMENT_TIME, storeList, subscriptionOfferingsList, promoUtils.getRandomPromoCode(),
                        false, Status.ACTIVE, null, null, "2", "MONTH", 2, "4", "5", "5", "6", "Supplement" } };
    }

    /**
     * DataProvider to pass different promotion type for basic and subscription offering type to
     * testExtendNewOrCancelPromotion method.
     */
    @DataProvider(name = "extendNewOrCancelPromotion")
    public Object[][] getTestDataForNewOrCancelPromotion() {
        return new Object[][] {
                { PromotionType.DISCOUNT_PERCENTAGE, storeList, basicOfferingsList, promoUtils.getRandomPromoCode(),
                        false, Status.NEW, "5", null, null, null, 2, "4", "5" },
                { PromotionType.DISCOUNT_AMOUNT, storeList, subscriptionOfferingsList, promoUtils.getRandomPromoCode(),
                        false, Status.NEW, null, "10", null, null, 2, "4", "5" },
                { PromotionType.DISCOUNT_PERCENTAGE, storeList, subscriptionOfferingsList,
                        promoUtils.getRandomPromoCode(), false, Status.NEW, "5", null, null, null, 2, "4", "5" },
                { PromotionType.SUPPLEMENT_TIME, storeList, subscriptionOfferingsList, promoUtils.getRandomPromoCode(),
                        false, Status.NEW, null, null, "2", "MONTH", 2, "4", "5" } };
    }
}
