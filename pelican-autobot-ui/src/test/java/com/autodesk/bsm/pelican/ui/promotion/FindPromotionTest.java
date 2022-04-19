package com.autodesk.bsm.pelican.ui.promotion;

import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
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
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FindPromotionTest extends SeleniumWebdriver {

    private JPromotion activeNonStoreWideDiscountPercentPromo;
    private JPromotion activeStoreWideDiscountAmountPromo;
    private JPromotion newStoreWideSupplementTimePromo;
    private FindPromotionsPage findPromotionsPage;
    private PromotionDetailsPage promotionDetailsPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(FindPromotionTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        // create subscription plan
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // Add subscription plans
        final Offerings bicOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // open a chrome browser and login into Admin Tool
        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findPromotionsPage = adminToolPage.getPage(FindPromotionsPage.class);

        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());

        activeNonStoreWideDiscountPercentPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, "15.00", null, DateTimeUtils.getUTCFutureExpirationDate(), null, null, 3, null, null);
        activeStoreWideDiscountAmountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            true, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        newStoreWideSupplementTimePromo = promotionUtils.addPromotion(PromotionType.SUPPLEMENT_TIME,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(bicOffering2), promotionUtils.getRandomPromoCode(),
            false, Status.NEW, null, null, DateTimeUtils.getUTCFutureExpirationDate(), "5", "DAY", 2, null, null);
    }

    /**
     * Verify, Promotion can be find by its ID.
     *
     * @result Promotion Detail Page
     */
    @Test(dataProvider = "promotionDetails")
    public void testFindPromotionById(final JPromotion promotion) {

        LOGGER.info("Promotion ID : " + promotion.getData().getId());
        promotionDetailsPage = findPromotionsPage.findById(promotion.getData().getId());

        HelperForPromotions.assertionsForPromotions(promotion, promotionDetailsPage, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Promotion can be find by promoCode.
     *
     * @result Promotion Detail Page
     */
    @Test(dataProvider = "promotionDetails")
    public void testFindPromotionByCode(final JPromotion promotion) {

        promotionDetailsPage =
            findPromotionsPage.selectResultRowWithFindByCode(1, promotion.getData().getCustomPromoCode());

        HelperForPromotions.assertionsForPromotions(promotion, promotionDetailsPage, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the
     */
    @DataProvider(name = "promotionDetails")
    public Object[][] getTestDataForPromoState() {
        return new Object[][] { { activeNonStoreWideDiscountPercentPromo }, { activeStoreWideDiscountAmountPromo },
                { newStoreWideSupplementTimePromo }

        };
    }

    @DataProvider(name = "promotionTypes")
    public Object[][] getPromotionTypes() {
        return new Object[][] { { PromotionType.DISCOUNT_AMOUNT, PromotionType.DISCOUNT_PERCENTAGE },
                { PromotionType.SUPPLEMENT_TIME } };
    }

    @DataProvider(name = "promotionState")
    public Object[][] getPromotionState() {
        return new Object[][] { { Status.NEW }, { Status.ACTIVE }, { Status.CANCELED }, { Status.EXPIRED } };
    }
}
