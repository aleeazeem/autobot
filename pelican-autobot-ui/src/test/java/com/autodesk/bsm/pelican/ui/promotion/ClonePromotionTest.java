package com.autodesk.bsm.pelican.ui.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanTriggerClient;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonStatus;
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
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.FindPromotionsPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Date;

/***
 * This
 *
 * class tests Clone Promotion functionality.Any Type of Promotion can be cloned which includes New, Active,* Expired
 * and Cancelled but promotions which are cloned have a status NEW. After clicking on clone Button of any*Promotion, the
 * cloned Promo is displayed in Add Mode.REF:US 10283**
 *
 * @author Muhammad Azeem
 */

public class ClonePromotionTest extends SeleniumWebdriver {

    private PromotionUtils promotionUtils;
    private static final Currency CURRENCY = Currency.USD;
    private static final int indexOfOfferOnDetailPage = 13;
    private static String storeName;
    private static String newPromoId;
    private static Offerings bicSubOffer1;
    private static Offerings metaSubOffer1;
    private static Offerings basicOffering1;
    private static boolean storeWideFlag;
    private PelicanTriggerClient triggerResource;
    private JStore store1;
    private FindPromotionsPage findPromotionsPage;
    private PromotionDetailsPage promotionDetailsPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClonePromotionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();

        final JobsClient jobsResource = triggerResource.jobs();
        final JsonStatus response = jobsResource.promotionExpiration();
        LOGGER.info("triggers response: " + response);
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        // create a store
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        store1 = storeApiUtils.addStore(Status.ACTIVE, Country.US, Currency.USD, null, false);
        final String externalKeyOfPriceList1 = store1.getIncluded().getPriceLists().get(0).getExternalKey();

        // create basic offerings
        basicOffering1 = basicOfferingApiUtils.addBasicOffering(externalKeyOfPriceList1, OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        // create bic offering
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // create bic offerings
        bicSubOffer1 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList1,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // create meta offerings
        metaSubOffer1 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList1,
            OfferingType.META_SUBSCRIPTION, BillingFrequency.QUARTER, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // open a chrome browser and login into Admin Tool
        initializeDriver(getEnvironmentVariables());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        triggerResource = new PelicanClient(getEnvironmentVariables()).trigger();

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        findPromotionsPage = adminToolPage.getPage(FindPromotionsPage.class);
        promotionDetailsPage = adminToolPage.getPage(PromotionDetailsPage.class);
    }

    /**
     * This test method is for to test Cloning of Active and New Promotions with . Amount Promo - Bic, Meta and Basic
     * Offering . Percentage Promo - Bic, Meta and Basic Offering . Supplement Promo - Bic And Meta subscription offers
     * The important scenarios which are tested for cloned Promotion are as follow . New ID will be assigned to Cloned
     * Promotion . Status should be New . Date Range should be same in Cloned Promotion as iN Original Promotion . Name
     * should be Start with "COPY OF" . Description should be copied to cloned Promotion from original Promotion . Flag
     * of storeWide in Cloned Promotion should be same as in Original Promotion . PromoType in Cloned Promotion should
     * be same as in Original Promotion (i.e Discount or Supplement) . AmountValue in Cloned Promotion should be same as
     * in Original Promotion (if Original Promo is DiscountAmount Promo) . PercentageValue in Cloned Promotion should be
     * same as in Original Promotion (if Original Promo is DiscountPercent Promo) . PromotionCode should be empty for
     * cloned Promotion which can be customized or by clicking on add promotion button, new promoCode will be issued by
     * a System . Offering in Cloned Promotion should be same as in Original Promotion which are - Subscription Offer -
     * Basic Offering . Maximum number of uses in Cloned Promotion should be same as in Original Promotion. If it is
     * null in Original Promotion than it will be shown as null in Cloned Promotion. . Maximum number of uses per user
     * in Cloned Promotion should be same as in Original Promotion. If it is null in Original Promotion than it will be
     * shown as null in Cloned Promotion.
     */
    @Test(dataProvider = "clonePromotionForNewAndActive")
    public void clonePromotionWithStatusNewAndActive(final String code, final boolean storeWide, final Status status,
        final PromotionType type, final Double value, final Offerings offer, final int maxUses,
        final int maxUsesPerUser, final JStore store, final int numberOfBillingCycles, final int timePeriodCount,
        final String timePeriodType) {
        final Date expirationdate = DateTimeUtils.getNowPlusSecs(8);
        // create a promotion in new state
        final JPromotion newPromo =
            promotionUtils.addPromotion(type, Lists.newArrayList(store), Lists.newArrayList(offer), code, storeWide,
                status, value.toString(), value.toString(), expirationdate, Integer.toString(timePeriodCount),
                timePeriodType, numberOfBillingCycles, Integer.toString(maxUses), Integer.toString(maxUsesPerUser));

        newPromoId = newPromo.getData().getId();
        storeName = store.getName();

        promotionDetailsPage = findPromotionsPage.findById(newPromoId);
        final String dateRangeOfPromo = promotionDetailsPage.getEffectiveDateRange();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        promotionDetailsPage.clickOnCloneButton();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        promotionDetailsPage.submit();
        Util.waitInSeconds(TimeConstants.ONE_SEC);

        final String dateRangeofClonedPromo = promotionDetailsPage.getEffectiveDateRange();
        // new ID will be issued for cloned promotion
        AssertCollector.assertThat("Unable to Edit Code", promotionDetailsPage.getId(),
            not(equalTo(newPromo.getData().getId())), assertionErrorList);
        AssertCollector.assertThat("Effective Date range are not same", dateRangeOfPromo,
            equalTo(dateRangeofClonedPromo), assertionErrorList);

        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Basic Offerings")) {
            AssertCollector.assertThat("Offering is not Correct",
                promotionDetailsPage.getValueByField("Basic Offerings"),
                equalTo(basicOffering1.getOfferings().get(0).getExternalKey()), assertionErrorList);
        }
        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Subscription Offers")) {
            AssertCollector.assertThat("Offering is not Correct",
                promotionDetailsPage.getValueByField("Subscription Offers"),
                equalTo(bicSubOffer1.getIncluded().getBillingPlans().get(0).getExternalKey()), assertionErrorList);
        }
        AssertCollector.assertThat("Name of the Promotion is not Correct", promotionDetailsPage.getName(),
            equalTo("COPY OF " + newPromo.getData().getName()), assertionErrorList);

        AssertCollector.assertThat("Description of the Promotion is not same", promotionDetailsPage.getDescription(),
            equalTo(newPromo.getData().getDescription()), assertionErrorList);

        storeWideFlag = promotionDetailsPage.getStoreWide().equals("YES");
        AssertCollector.assertThat("Flag of Store Wide is not same", newPromo.getData().isStoreWide(),
            equalTo(storeWideFlag), assertionErrorList);

        AssertCollector.assertThat("Promo Code is not null ", promotionDetailsPage.getPromotionCode(),
            not(equalTo(newPromo.getData().getCustomPromoCode())), assertionErrorList);
        AssertCollector.assertThat("Store is not Cloned ", promotionDetailsPage.getLimitedToStore(), equalTo(storeName),
            assertionErrorList);

        if (promotionDetailsPage.getType().equals("Discount")) {
            if (promotionDetailsPage.getDiscountType().equals("Cash Discount")) {
                AssertCollector.assertThat("Discount Type is not correct",
                    newPromo.getData().getPromotionType().toString(), equalTo("DISCOUNT_AMOUNT"), assertionErrorList);
                AssertCollector.assertThat("Amount is not cloned", promotionDetailsPage.getAmount(),
                    equalTo(value.toString() + " " + CURRENCY.toString()), assertionErrorList);
            } else {
                AssertCollector.assertThat("Discount Type is not correct",
                    newPromo.getData().getPromotionType().toString(), equalTo("DISCOUNT_PERCENTAGE"),
                    assertionErrorList);
                AssertCollector.assertThat("Percentage is not cloned", promotionDetailsPage.getPercentage(),
                    equalTo(value.toString() + "%"), assertionErrorList);
            }
        } else {
            AssertCollector.assertThat("Supplement Type is not correct",
                newPromo.getData().getPromotionType().toString(), equalTo("SUPPLEMENT_TIME"), assertionErrorList);
        }

        AssertCollector.assertThat("Maximum Number of Uses is not cloned Properly",
            promotionDetailsPage.getMaximumNumberOfUses(), equalTo(Integer.toString(maxUses)), assertionErrorList);
        AssertCollector.assertThat("Maximum Number of Uses is not cloned Properly",
            promotionDetailsPage.getMaximumUsesPerUser(), equalTo(Integer.toString(maxUsesPerUser)),
            assertionErrorList);

        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Subscription Offers")
            && !promotionDetailsPage.getType().equals("Supplement")) {
            AssertCollector.assertThat("Number of BillingCycle is not cloned", promotionDetailsPage.getBillingCycle(),
                equalTo(Integer.toString(numberOfBillingCycles)), assertionErrorList);
        }
        // Promotion which is cloned is always in NEW state
        AssertCollector.assertThat("Cloned Promotion is not saved in New State", promotionDetailsPage.getState(),
            equalTo(Status.NEW.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        if (status.equals(Status.ACTIVE) && storeWide) {
            promotionDetailsPage = findPromotionsPage.findById(newPromoId);
            promotionDetailsPage.cancelPromotion();
        }
    }

    /**
     * This test method is for to test Cloning of Cancelled Promotions with . Amount Promo - Bic, Meta and Basic
     * Offering . Percentage Promo - Bic, Meta and Basic Offering . Supplement Promo - Bic And Meta subscription offers
     * The important scenarios which are tested for cloned Promotion are as follow . New ID will be assigned to Cloned
     * Promotion . Status of cloned promotion should be NEW . Date Range should be same in Cloned Promotion as iN
     * Original Promotion . Name should be Start with "COPY OF" . Description should be copied to cloned Promotion from
     * original Promotion . Flag of storeWide in Cloned Promotion should be same as in Original Promotion . PromoType in
     * Cloned Promotion should be same as in Original Promotion (i.e Discount or Supplement) . AmountValue in Cloned
     * Promotion should be same as in Original Promotion (if Original Promo is DiscountAmount Promo) . PercentageValue
     * in Cloned Promotion should be same as in Original Promotion (if Original Promo is DiscountPercent Promo) .
     * PromotionCode should be empty for cloned Promotion which can be customized or by clicking on add promotion
     * button, new promoCode will be issued by a System . Offering in Cloned Promotion should be same as in Original
     * Promotion which are - Subscription Offer - Basic Offering . Maximum number of uses in Cloned Promotion should be
     * same as in Original Promotion. If it is null in Original Promotion than it will be shown as null in Cloned
     * Promotion. . Maximum number of uses per user in Cloned Promotion should be same as in Original Promotion. If it
     * is null in Original Promotion than it will be shown as null in Cloned Promotion.
     */
    @Test(dataProvider = "clonePromotionForCancel")
    public void clonePromotionWithStatusCancelled(final String code, final boolean storeWide, final Status status,
        final PromotionType type, final Double value, final Offerings offer, final Date expirationdate,
        final int maxUses, final int maxUsesPerUser, final JStore store, final int numberOfBillingCycles,
        final int timePeriodCount, final String timePeriodType) {
        // create a promotion in new state
        final JPromotion newPromo =
            promotionUtils.addPromotion(type, Lists.newArrayList(store), Lists.newArrayList(offer), code, storeWide,
                status, value.toString(), value.toString(), expirationdate, Integer.toString(timePeriodCount),
                timePeriodType, numberOfBillingCycles, Integer.toString(maxUses), Integer.toString(maxUsesPerUser));

        newPromoId = newPromo.getData().getId();
        storeName = store.getName();

        promotionDetailsPage = findPromotionsPage.findById(newPromoId);
        final String dateRangeOfPromo = promotionDetailsPage.getEffectiveDateRange();

        promotionDetailsPage.clickOnCancelButton();
        promotionDetailsPage.popUpWindowConfirm();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage.clickOnCloneButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage.submit();

        final String dateRangeofClonedPromo = promotionDetailsPage.getEffectiveDateRange();
        // new ID will be issued for cloned promotion
        AssertCollector.assertThat("Unable to Edit Code", promotionDetailsPage.getId(),
            not(equalTo(newPromo.getData().getId())), assertionErrorList);
        AssertCollector.assertThat("Effective Date range are not same", dateRangeOfPromo,
            equalTo(dateRangeofClonedPromo), assertionErrorList);

        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Basic Offerings")) {
            AssertCollector.assertThat("Offering is not Correct",
                promotionDetailsPage.getValueByField("Basic Offerings"),
                equalTo(basicOffering1.getOfferings().get(0).getExternalKey()), assertionErrorList);
        }
        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Subscription Offers")) {
            AssertCollector.assertThat("Offering is not Correct",
                promotionDetailsPage.getValueByField("Subscription Offers"),
                equalTo(bicSubOffer1.getIncluded().getBillingPlans().get(0).getExternalKey()), assertionErrorList);
        }
        AssertCollector.assertThat("Name of the Promotion is not Correct", promotionDetailsPage.getName(),
            equalTo("COPY OF " + newPromo.getData().getName()), assertionErrorList);

        AssertCollector.assertThat("Description of the Promotion is not same", promotionDetailsPage.getDescription(),
            equalTo(newPromo.getData().getDescription()), assertionErrorList);

        storeWideFlag = promotionDetailsPage.getStoreWide().equals("YES");
        AssertCollector.assertThat("Flag of Store Wide is not same", newPromo.getData().isStoreWide(),
            equalTo(storeWideFlag), assertionErrorList);

        AssertCollector.assertThat("Promo Code is not null ", promotionDetailsPage.getPromotionCode(),
            not(equalTo(newPromo.getData().getCustomPromoCode())), assertionErrorList);

        AssertCollector.assertThat("Store is not Cloned ", promotionDetailsPage.getLimitedToStore(), equalTo(storeName),
            assertionErrorList);

        if (promotionDetailsPage.getType().equals("Discount")) {
            if (promotionDetailsPage.getDiscountType().equals("Cash Discount")) {
                AssertCollector.assertThat("Discount Type is not correct",
                    newPromo.getData().getPromotionType().toString(), equalTo("DISCOUNT_AMOUNT"), assertionErrorList);
                AssertCollector.assertThat("Amount is not cloned", promotionDetailsPage.getAmount(),
                    equalTo(value.toString() + " " + CURRENCY.toString()), assertionErrorList);
            } else {
                AssertCollector.assertThat("Discount Type is not correct",
                    newPromo.getData().getPromotionType().toString(), equalTo("DISCOUNT_PERCENTAGE"),
                    assertionErrorList);
                AssertCollector.assertThat("Percentage is not cloned", promotionDetailsPage.getPercentage(),
                    equalTo(value.toString() + "%"), assertionErrorList);
            }
        } else {
            AssertCollector.assertThat("Supplement Type is not correct",
                newPromo.getData().getPromotionType().toString(), equalTo("SUPPLEMENT_TIME"), assertionErrorList);
        }

        AssertCollector.assertThat("Maximum Number of Uses is not cloned Properly",
            promotionDetailsPage.getMaximumNumberOfUses(), equalTo(Integer.toString(maxUses)), assertionErrorList);
        AssertCollector.assertThat("Maximum Number of Uses is not cloned Properly",
            promotionDetailsPage.getMaximumUsesPerUser(), equalTo(Integer.toString(maxUsesPerUser)),
            assertionErrorList);

        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Subscription Offers")
            && !promotionDetailsPage.getType().equals("Supplement")) {
            AssertCollector.assertThat("Number of BillingCycle is not cloned", promotionDetailsPage.getBillingCycle(),
                equalTo(Integer.toString(numberOfBillingCycles)), assertionErrorList);
        }
        // Promotion which is cloned is always in NEW state
        AssertCollector.assertThat("Cloned Promotion is not saved in New State", promotionDetailsPage.getState(),
            equalTo(Status.NEW.toString()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method is for to test Cloning of Expired Promotions with . Amount Promo - Bic, Meta and Basic Offering
     * . Percentage Promo - Bic, Meta and Basic Offering . Supplement Promo - Bic And Meta subscription offers The
     * important scenarios which are tested for cloned Promotion are as follow . New ID will be assigned to Cloned
     * Promotion . Status of cloned promotion should be NEW . Date Range should be same in Cloned Promotion as iN
     * Original Promotion . Name should be Start with "COPY OF" . Description should be copied to cloned Promotion from
     * original Promotion . Flag of storeWide in Cloned Promotion should be same as in Original Promotion . PromoType in
     * Cloned Promotion should be same as in Original Promotion (i.e Discount or Supplement) . AmountValue in Cloned
     * Promotion should be same as in Original Promotion (if Original Promo is DiscountAmount Promo) . PercentageValue
     * in Cloned Promotion should be same as in Original Promotion (if Original Promo is DiscountPercent Promo) .
     * PromotionCode should be empty for cloned Promotion which can be customized or by clicking on add promotion
     * button, new promoCode will be issued by a System . Offering in Cloned Promotion should be same as in Original
     * Promotion which are - Subscription Offer - Basic Offering . Maximum number of uses in Cloned Promotion should be
     * same as in Original Promotion. If it is null in Original Promotion than it will be shown as null in Cloned
     * Promotion. . Maximum number of uses per user in Cloned Promotion should be same as in Original Promotion. If it
     * is null in Original Promotion than it will be shown as null in Cloned Promotion.
     */
    @Test(dataProvider = "clonePromotionForExpired")
    public void clonePromotionWithStatusExpired(final String code, final boolean storeWide, final Status status,
        final PromotionType type, final Double value, final Offerings offer, final int maxUses,
        final int maxUsesPerUser, final JStore store, final int numberOfBillingCycles, final int timePeriodCount,
        final String timePeriodType) {
        final Date expirationdate = DateTimeUtils.getNowPlusSecs(10);
        // create a promotion in new state
        final JPromotion newPromo =
            promotionUtils.addPromotion(type, Lists.newArrayList(store), Lists.newArrayList(offer), code, storeWide,
                status, value.toString(), value.toString(), expirationdate, Integer.toString(timePeriodCount),
                timePeriodType, numberOfBillingCycles, Integer.toString(maxUses), Integer.toString(maxUsesPerUser));

        newPromoId = newPromo.getData().getId();

        storeName = store.getName();

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage = findPromotionsPage.findById(newPromoId);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        // Running triggers job
        LOGGER.info("Run Triggers Job");
        final JobsClient jobsResource = triggerResource.jobs();
        final JsonStatus response = jobsResource.promotionExpiration(newPromoId);

        LOGGER.info("triggers response: " + response);
        // time required to get promotion expired
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);

        // If the status is still NEW - give one more try
        if (promotionDetailsPage.getState().equals(Status.NEW.toString())) {
            promotionDetailsPage = findPromotionsPage.findById(newPromoId);
        }
        AssertCollector.assertThat("Why Promotion is not Expired", promotionDetailsPage.getState(),
            equalTo(Status.EXPIRED.toString()), assertionErrorList);
        final String dateRangeOfPromo = promotionDetailsPage.getEffectiveDateRange();
        promotionDetailsPage.clickOnCloneButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetailsPage.submit();
        helperForClonePromotion(promotionDetailsPage, newPromo, dateRangeOfPromo, value, maxUses, maxUsesPerUser,
            numberOfBillingCycles);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "clonePromotionForNewAndActive")
    public Object[][] getTestDataForNewPromo() {
        return new Object[][] {
                // data to create nonStoreWide promotion with NEW status
                { "1" + RandomStringUtils.randomAlphabetic(10), false, Status.NEW, PromotionType.DISCOUNT_PERCENTAGE,
                        25.55, bicSubOffer1, 2, 2, store1, 1, 0, null },
                { "2" + RandomStringUtils.randomAlphabetic(10), false, Status.NEW, PromotionType.DISCOUNT_AMOUNT, 12.67,
                        bicSubOffer1, 2, 2, store1, 1, 0, null },
                // data to create storeWide promotion with ACTIVE status
                { "25" + RandomStringUtils.randomAlphabetic(10), true, Status.ACTIVE, PromotionType.DISCOUNT_PERCENTAGE,
                        25.55, bicSubOffer1, 2, 2, store1, 1, 0, null },
                { "27" + RandomStringUtils.randomAlphabetic(10), true, Status.ACTIVE, PromotionType.SUPPLEMENT_TIME,
                        0.00, bicSubOffer1, 2, 2, store1, 0, 1, "MONTH" } };
    }

    @DataProvider(name = "clonePromotionForCancel")
    public Object[][] getTestDataForCancelPromo() {
        return new Object[][] {
                // data to create storeWide promotion with ACTIVE status
                { "34" + RandomStringUtils.randomAlphabetic(10), true, Status.ACTIVE, PromotionType.DISCOUNT_AMOUNT,
                        12.67, bicSubOffer1, DateTimeUtils.getFutureExpirationDate(), 2, 2, store1, 1, 0, null },
                { "35" + RandomStringUtils.randomAlphabetic(10), true, Status.ACTIVE, PromotionType.SUPPLEMENT_TIME,
                        0.00, bicSubOffer1, DateTimeUtils.getFutureExpirationDate(), 2, 2, store1, 0, 1, "MONTH" },
                // data to create nonStoreWide promotion with ACTIVE status
                { "44" + RandomStringUtils.randomAlphabetic(10), false, Status.ACTIVE,
                        PromotionType.DISCOUNT_PERCENTAGE, 22.53, metaSubOffer1,
                        DateTimeUtils.getFutureExpirationDate(), 2, 2, store1, 1, 0, null },
                { "48" + RandomStringUtils.randomAlphabetic(10), false, Status.ACTIVE, PromotionType.DISCOUNT_AMOUNT,
                        25.55, basicOffering1, DateTimeUtils.getFutureExpirationDate(), 2, 2, store1, 0, 0, null } };
    }

    @DataProvider(name = "clonePromotionForExpired")
    public Object[][] getTestDataForExpiredPromo() {
        return new Object[][] {
                // data to create storeWide promotion with NEW status
                { "55" + RandomStringUtils.randomAlphabetic(10), true, Status.NEW, PromotionType.DISCOUNT_PERCENTAGE,
                        25.55, basicOffering1, 2, 2, store1, 0, 0, null },
                { "56" + RandomStringUtils.randomAlphabetic(10), true, Status.NEW, PromotionType.DISCOUNT_AMOUNT, 25.55,
                        basicOffering1, 2, 2, store1, 0, 0, null },
                { "59" + RandomStringUtils.randomAlphabetic(10), false, Status.NEW, PromotionType.SUPPLEMENT_TIME, 0.00,
                        bicSubOffer1, 2, 2, store1, 0, 1, "MONTH" } };
    }

    /**
     * This is a helper method for clone promotion test method
     *
     * @param promotionDetailsPage
     * @param newPromo
     * @param dateRangeOfPromo
     * @param value
     * @param maxUses
     * @param maxUsesPerUser
     * @param numberOfBillingCycles
     */
    private void helperForClonePromotion(final PromotionDetailsPage promotionDetailsPage, final JPromotion newPromo,
        final String dateRangeOfPromo, final Double value, final int maxUses, final int maxUsesPerUser,
        final int numberOfBillingCycles) {
        final String dateRangeofClonedPromo = promotionDetailsPage.getEffectiveDateRange();
        // new ID will be issued for cloned promotion
        AssertCollector.assertThat("Unable to Edit Code", promotionDetailsPage.getId(),
            not(equalTo(newPromo.getData().getId())), assertionErrorList);
        AssertCollector.assertThat("Effective Date range are not same", dateRangeOfPromo,
            equalTo(dateRangeofClonedPromo), assertionErrorList);

        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Basic Offerings")) {
            AssertCollector.assertThat("Offering is not Correct",
                promotionDetailsPage.getValueByField("Basic Offerings"),
                equalTo(basicOffering1.getOfferings().get(0).getExternalKey()), assertionErrorList);
        }
        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Subscription Offers")) {
            AssertCollector.assertThat("Offering is not Correct",
                promotionDetailsPage.getValueByField("Subscription Offers"),
                equalTo(bicSubOffer1.getIncluded().getBillingPlans().get(0).getExternalKey()), assertionErrorList);
        }
        AssertCollector.assertThat("Name of the Promotion is not Correct", promotionDetailsPage.getName(),
            equalTo("COPY OF " + newPromo.getData().getName()), assertionErrorList);

        AssertCollector.assertThat("Description of the Promotion is not same", promotionDetailsPage.getDescription(),
            equalTo(newPromo.getData().getDescription()), assertionErrorList);

        storeWideFlag = promotionDetailsPage.getStoreWide().equals("YES");
        AssertCollector.assertThat("Flag of Store Wide is not same", newPromo.getData().isStoreWide(),
            equalTo(storeWideFlag), assertionErrorList);

        AssertCollector.assertThat("Promo Code is not null ", promotionDetailsPage.getPromotionCode(),
            not(equalTo(newPromo.getData().getCustomPromoCode())), assertionErrorList);

        AssertCollector.assertThat("Store is not Cloned ", promotionDetailsPage.getLimitedToStore(), equalTo(storeName),
            assertionErrorList);

        if (promotionDetailsPage.getType().equals("Discount")) {
            if (promotionDetailsPage.getDiscountType().equals("Cash Discount")) {
                AssertCollector.assertThat("Discount Type is not correct",
                    newPromo.getData().getPromotionType().toString(), equalTo("DISCOUNT_AMOUNT"), assertionErrorList);
                AssertCollector.assertThat("Amount is not cloned", promotionDetailsPage.getAmount(),
                    equalTo(value.toString() + " " + CURRENCY.toString()), assertionErrorList);
            } else {
                AssertCollector.assertThat("Discount Type is not correct",
                    newPromo.getData().getPromotionType().toString(), equalTo("DISCOUNT_PERCENTAGE"),
                    assertionErrorList);
                AssertCollector.assertThat("Percentage is not cloned", promotionDetailsPage.getPercentage(),
                    equalTo(value.toString() + "%"), assertionErrorList);
            }
        } else {
            AssertCollector.assertThat("Supplement Type is not correct",
                newPromo.getData().getPromotionType().toString(), equalTo("SUPPLEMENT_TIME"), assertionErrorList);
        }

        AssertCollector.assertThat("Maximum Number of Uses is not cloned Properly",
            promotionDetailsPage.getMaximumNumberOfUses(), equalTo(Integer.toString(maxUses)), assertionErrorList);
        AssertCollector.assertThat("Maximum Number of Uses is not cloned Properly",
            promotionDetailsPage.getMaximumUsesPerUser(), equalTo(Integer.toString(maxUsesPerUser)),
            assertionErrorList);

        if (promotionDetailsPage.getAllFields().get(indexOfOfferOnDetailPage).equals("Subscription Offers")
            && !promotionDetailsPage.getType().equals("Supplement")) {
            AssertCollector.assertThat("Number of BillingCycle is not cloned", promotionDetailsPage.getBillingCycle(),
                equalTo(Integer.toString(numberOfBillingCycles)), assertionErrorList);
        }
        // Promotion which is cloned is always in NEW state
        AssertCollector.assertThat("Cloned Promotion is not saved in New State", promotionDetailsPage.getState(),
            equalTo(Status.NEW.toString()), assertionErrorList);
    }
}
