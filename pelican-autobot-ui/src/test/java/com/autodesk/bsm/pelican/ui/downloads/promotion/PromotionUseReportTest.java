package com.autodesk.bsm.pelican.ui.downloads.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPriceList;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Store;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PromotionUseReportHeaderConstants;
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
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.PromotionUseReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This test class test all required Scenarios of Promotion Use Report.
 *
 * @author muhammad
 */
public class PromotionUseReportTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static final int TOTAL_COLUMNS_IN_REPORT =
        PromotionUseReportHeaderConstants.PROMOTION_USE_REPORT_HEADERS_SIZE + 1;
    private static final int TOTAL_COLUMNS_IN_DOWNLOAD_REPORT =
        PromotionUseReportHeaderConstants.PROMOTION_USE_REPORT_DOWNLOAD_HEADERS_SIZE + 1;
    private static PromotionUseReportPage promotionUseReportPage;
    private static GenericGrid grid;
    private static JStore store;
    private PromotionUtils promotionUtils;
    private static Offerings basicOffering;
    private static JPromotion promotion;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final String promoCode = RandomStringUtils.randomAlphanumeric(14);
    private static GenericDetails details;
    private static final String START_DATE = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 40);
    private static final String END_DATE = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
    private static final String MAX_USES = "5";
    private static final String MAX_USES_PER_USER = "3";
    private static final String ACTUAL_FILE_NAME = "PromotionReport.xlsx";

    // Bundle Promotion test
    private static final int DEFAULT_BILLING_CYCLES = 1;
    private static final String CASH_AMOUNT = "100.21";
    private static final String PERCENTAGE_AMOUNT = "15";
    private static JPromotion newNonStoreWideDiscountAmountPromo;
    private static JPromotion newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering;
    private static JPromotion newStoreWidePercentPromo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionUseReportTest.class.getSimpleName());

    /**
     * Data setup - open a admin tool page and login into it
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        promotionUseReportPage = adminToolPage.getPage(PromotionUseReportPage.class);
        grid = adminToolPage.getPage(GenericGrid.class);
        details = adminToolPage.getPage(GenericDetails.class);

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Create a store.
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        final Store addedStore = storeApiUtils.addStoreWithoutPriceListAndCountry(Status.ACTIVE);
        final String storeId = addedStore.getData().getId();
        final JPriceList priceList = storeApiUtils.addPriceList(storeId, Currency.USD);
        final String priceListExternalKey = priceList.getData().getExternalKey();
        storeApiUtils.addCountryToStoreAndPriceList(priceListExternalKey, storeId, Country.US);
        store = resource.stores().getStore(addedStore.getData().getId());
        final String externalKeyOfPriceList = store.getIncluded().getPriceLists().get(0).getExternalKey();

        // Add active basic offering
        basicOffering = basicOfferingApiUtils.addBasicOffering(priceListExternalKey, OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, 500, UsageType.COM, null, null);

        // Add subscription plans
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList,
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Add promotion for bic subscription plan offer.
        newNonStoreWideDiscountAmountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(store), Lists.newArrayList(bicOffering1), promotionUtils.getRandomPromoCode(), false,
            Status.ACTIVE, null, CASH_AMOUNT, DateTimeUtils.getUTCFutureExpirationDate(), null, null,
            DEFAULT_BILLING_CYCLES, null, null);
        final String priceIdForBicOffering = bicOffering1.getIncluded().getPrices().get(0).getId();

        // Create line item Submit PO and porcess it for charge for storewide
        // bic subscription offer promotion.
        final LineItem lineitem0 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicOffering, 1,
            newNonStoreWideDiscountAmountPromo.getData().getId(), null);
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicOffering, 2, null, null);
        final List<LineItem> lineItems0 = Arrays.asList(lineitem0, lineitem1);

        // submit a purchase order
        final PurchaseOrder purchaseOrder0 =
            purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems0, PaymentType.CREDIT_CARD, null);

        final String purchaseOrderId0 = purchaseOrder0.getId();
        LOGGER.info("Purchase Order ID0 :" + purchaseOrderId0);

        // create a store wide discount percent bundled promo
        newStoreWidePercentPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(store), Lists.newArrayList(bicOffering1), promotionUtils.getRandomPromoCode(), true,
            Status.ACTIVE, PERCENTAGE_AMOUNT, null, DateTimeUtils.getUTCFutureExpirationDate(), null, null,
            DEFAULT_BILLING_CYCLES, null, null);

        // Create line items for Submit PO and porcess it for charge for
        // storewide bic subscription offer promotion.
        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicOffering, 2,
            newStoreWidePercentPromo.getData().getId(), null);
        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicOffering, 2, null, null);
        final List<LineItem> lineItems1 = Arrays.asList(lineitem2, lineitem3);

        // submit a purchase order using newStoreWidePercentPromo
        final PurchaseOrder purchaseOrder1 =
            purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems1, PaymentType.CREDIT_CARD, null);

        final String purchaseOrderId1 = purchaseOrder1.getId();
        LOGGER.info("Purchase Order ID1 :" + purchaseOrderId1);

        // Add active meta subscription for bundled offers
        final Offerings bundledBasicOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList, OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // Add active subscription subscription for bundled offers
        final Offerings bundledSubscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(externalKeyOfPriceList, OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // create a nonStoreWide bundled promo with both basic offering and
        // subscription offering
        final List<BundlePromoOfferings> bundlePromoOfferings = new ArrayList<>();
        bundlePromoOfferings.add(promotionUtils.createBundlePromotionOffering(bundledBasicOffering2, 5, true));
        bundlePromoOfferings.add(promotionUtils.createBundlePromotionOffering(bundledSubscriptionOffering1, 3, true));
        final String priceIdForBundledBasicOffering2 = bundledBasicOffering2.getIncluded().getPrices().get(0).getId();
        final String priceIdForBundledSubscriptionOffering1 =
            bundledSubscriptionOffering1.getIncluded().getPrices().get(0).getId();

        newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(store),
                bundlePromoOfferings, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, PERCENTAGE_AMOUNT,
                null, DateTimeUtils.getUTCFutureExpirationDate(), 1, "5", "6");

        // Submit PO and process it for Charge : Use Promotion :
        // newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering
        final LineItem lineitem4 = purchaseOrderUtils.createOfferingLineItem(priceIdForBundledBasicOffering2, 5,
            newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering.getData().getId(), null);

        final LineItem lineitem5 = purchaseOrderUtils.createOfferingLineItem(priceIdForBundledSubscriptionOffering1, 3,
            newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering.getData().getId(), null);

        final LineItem lineitem6 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForBundledBasicOffering2, 2, null, null);

        final LineItem lineitem7 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForBundledSubscriptionOffering1, 1, null, null);
        final List<LineItem> lineItems2 = Arrays.asList(lineitem4, lineitem5, lineitem6, lineitem7);

        // submit a purchase order
        final PurchaseOrder purchaseOrder2 =
            purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems2, PaymentType.CREDIT_CARD, null);

        final String purchaseOrderId2 = purchaseOrder2.getId();
        LOGGER.info("Purchase Order ID2 :" + purchaseOrderId2);
    }

    /**
     * This test method verifies all the required Headers of a Report
     *
     * @result Report should be opened with the column of all Headers which are 14 in number
     */
    @Test
    public void testPromotionUseReportHeaders() {
        promotionUseReportPage.navigateToPage();
        promotionUseReportPage.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertThat("Title of the Page is not Correct", grid.getDriver().getTitle(),
            equalTo("Pelican - Promotion Report"), assertionErrorList);
        AssertCollector.assertThat("Total number of columns are not correct", grid.getColumnHeaders().size(),
            equalTo(TOTAL_COLUMNS_IN_REPORT), assertionErrorList);
        AssertCollector.assertThat("First Column should be *Promotion ID*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_ID_POSITION),
            equalTo("Promotion ID"), assertionErrorList);
        AssertCollector.assertThat("Second Column Should be *Code*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_CODE_POSITION), equalTo("Code"),
            assertionErrorList);
        AssertCollector.assertThat("Third Column Should be *Name*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_NAME_POSITION), equalTo("Name"),
            assertionErrorList);
        AssertCollector.assertThat("Forth Column Should be *Description*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_DECRIPTION_POSITION),
            equalTo("Description"), assertionErrorList);
        AssertCollector.assertThat("Fifth Column Should be *State*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_STATE_POSITION), equalTo("State"),
            assertionErrorList);

        AssertCollector.assertThat("Sixth Column Should be *Store Wide*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_STORE_WIDE_POSITION),
            equalTo("Store Wide"), assertionErrorList);
        AssertCollector.assertThat("Seventh Column Should be *Type*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_TYPE_POSITION), equalTo("Type"),
            assertionErrorList);
        AssertCollector.assertThat("Eighth Column Should be *Effective Start Date*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_EFFECTIVE_START_DATE_POSITION),
            equalTo("Effective Start Date"), assertionErrorList);
        AssertCollector.assertThat("Ninth Column Should be *Effective End Date*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_EFFECTIVE_END_DATE_POSITION),
            equalTo("Effective End Date"), assertionErrorList);
        AssertCollector.assertThat("Tenth Column Should be *Max Number of Uses*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_MAX_NUMBER_OF_USES_POSITION),
            equalTo("Max Number of Uses"), assertionErrorList);
        AssertCollector.assertThat("Eleventh Column Should be *Max Uses per User*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_MAX_USES_PER_USER_POSITION),
            equalTo("Max Uses per User"), assertionErrorList);
        AssertCollector.assertThat("Twelveth Column Should be *Times Used*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_TIMES_USED_POSITION),
            equalTo("Times Used"), assertionErrorList);

        AssertCollector.assertThat("Thirteenth Column Should be *Bundled*",
            grid.getColumnHeaders().get(PromotionUseReportHeaderConstants.PROMOTION_BUNDLED_POSITION),
            equalTo("Bundled"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies the report which is generated by selecting different parameters.
     *
     * @result Report should be opened as expected
     */
    @Test(dataProvider = "combinationOfOptions")
    public void testPromotionUseReportWithSearchFilters(final String promoId, final String promoCode,
        final String storeWide, final String promoType, final Status promoState, final String START_DATE,
        final String END_DATE) {
        promotionUseReportPage = adminToolPage.getPage(PromotionUseReportPage.class);
        promotionUseReportPage.getReportWithCombination(promoId, promoCode, storeWide, promoType, promoState,
            START_DATE, END_DATE, null);
        final int promosCount = grid.getTotalItems();
        if (promosCount > 0) {
            if (promoId != null) {
                AssertCollector.assertThat("Promo id in report doesn't match with selected filter of Id",
                    grid.getColumnValues("Promotion ID").get(0), equalTo(promoId), assertionErrorList);
                AssertCollector.assertThat("Why Report is showing more than one results", grid.getTotalItems(),
                    equalTo(1), assertionErrorList);
            }
            if (promoCode != null) {
                AssertCollector.assertThat("Promo Code in report doesn't match with selected filter of Code",
                    grid.getColumnValues("Code").get(0), equalTo(promoCode), assertionErrorList);
                AssertCollector.assertThat("Why Report is showing more than one results ", grid.getTotalItems(),
                    equalTo(1), assertionErrorList);
            }
            if (storeWide != null) {
                final String storeWideFirstletterInCaps =
                    storeWide.subSequence(0, 1) + storeWide.substring(1).toLowerCase();
                AssertCollector.assertThat("Why values of storeWide are not" + storeWide,
                    grid.getColumnValues("Store Wide"), everyItem(equalTo(storeWideFirstletterInCaps)),
                    assertionErrorList);
            } else {
                LOGGER.info("Filter store wide is not selected to generate report");
            }

            if (promoType != null) {
                for (final String type : grid.getColumnValues("Type")) {
                    if (promoType.equals("Discount")) {
                        AssertCollector.assertThat("Why type is not Discount at index" + type, type.substring(0, 8),
                            equalTo(promoType), assertionErrorList);
                    } else {
                        AssertCollector.assertThat("Why type is not Supplement at index" + type, type.substring(0, 10),
                            equalTo(promoType), assertionErrorList);
                    }
                }
            }
            if (promoState != null) {
                AssertCollector.assertThat("Why every item of State is not" + promoState, grid.getColumnValues("State"),
                    everyItem(equalTo(promoState.toString())), assertionErrorList);
            } else {
                LOGGER.info("Filter Status is not selected to generate report");
            }

        } else {
            LOGGER.info("No result found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method randomly select a row from list and verifies all the values with report.
     *
     * @result Detail page values of promotion should be same as shown in Report for that promotion
     */
    @Test
    public void testPromotionUseReportDataWithPromotionDetails() {
        promotionUseReportPage = adminToolPage.getPage(PromotionUseReportPage.class);
        promotionUseReportPage.navigateToPage();
        promotionUseReportPage.getReportWithCombination(null, null, null, "Discount", Status.ACTIVE, START_DATE,
            END_DATE, null);
        final int promosCount = grid.getTotalItems();

        if (promosCount > 0) {
            final int promoIndex = selectPromoRandomlyFromFirstPage(promosCount);
            final String idInReport = grid.getColumnValues("Promotion ID").get(promoIndex);
            final String codeInReport = grid.getColumnValues("Code").get(promoIndex);
            final String nameInReport = (grid.getColumnValues("Name").get(promoIndex));
            final String stateInReport = grid.getColumnValues("State").get(promoIndex);
            final String storeWideInReport = (grid.getColumnValues("Store Wide").get(promoIndex)).toUpperCase();
            final String typeInReport = (grid.getColumnValues("Type").get(promoIndex)).substring(0, 8);
            final String effectiveStartDateInReport = grid.getColumnValues("Effective Start Date").get(promoIndex);
            final String effectiveEndDateInReport = grid.getColumnValues("Effective End Date").get(promoIndex);
            String maxUsesInReport = grid.getColumnValues("Max Number of Uses").get(promoIndex);
            if (maxUsesInReport.equals("")) {
                maxUsesInReport = null;
            }
            String maxUsesPerUserInReport = grid.getColumnValues("Max Uses per User").get(promoIndex);
            if (maxUsesPerUserInReport.equals("")) {
                maxUsesPerUserInReport = null;
            }

            grid.selectResultRow(promoIndex + 1);
            AssertCollector.assertThat("Id of promo is not same as shown in Report", details.getValueByField("ID"),
                equalTo(idInReport), assertionErrorList);
            AssertCollector.assertThat("Code is not same as shown in Report", details.getValueByField("Promotion Code"),
                equalTo(codeInReport), assertionErrorList);
            assertPromotionName(nameInReport, details.getValueByField("Name"));
            AssertCollector.assertThat("State of the promo is not same as shown in Report",
                details.getValueByField("State"), equalTo(stateInReport), assertionErrorList);
            AssertCollector.assertThat("Store Wide is not same as shown in Report",
                details.getValueByField("Store Wide"), equalTo(storeWideInReport), assertionErrorList);
            AssertCollector.assertThat("Prmotion Type is not same as shown in Report", details.getValueByField("Type"),
                equalTo(typeInReport), assertionErrorList);
            AssertCollector.assertThat("Value of Maximum number of uses is not same as shown in report",
                details.getValueByField("Maximum Number of Uses"), equalTo(maxUsesInReport), assertionErrorList);
            AssertCollector.assertThat("Value of Maximum Uses per user is not same as shown in report",
                details.getValueByField("Maximum Uses per User"), equalTo(maxUsesPerUserInReport), assertionErrorList);
            AssertCollector.assertThat("Value of effective date range is not same as shown in report",
                details.getValueByField("Effective Date Range"),
                equalTo("from " + effectiveStartDateInReport + " to " + effectiveEndDateInReport), assertionErrorList);

            AssertCollector.assertThat("Value of effective date range is not same as shown in report",
                DateTimeUtils.convertStringToDate((details.getValueByField("Created")),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH),
                greaterThanOrEqualTo(
                    DateTimeUtils.convertStringToDate(START_DATE, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                assertionErrorList);
            AssertCollector.assertThat("Value of effective date range is not same as shown in report",
                DateTimeUtils.convertStringToDate((details.getValueByField("Created")),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH),
                lessThanOrEqualTo(DateTimeUtils.convertStringToDate(END_DATE, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                assertionErrorList);
        } else {
            LOGGER.info("No result found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the generation of error if no status is selected to generate a report
     *
     * @result Error should be generated if no status is selected
     */
    @Test
    public void testPromotionUseReportErrorMessageForNonSelectionOfStatus() {
        promotionUseReportPage = adminToolPage.getPage(PromotionUseReportPage.class);
        promotionUseReportPage.navigateToPage();
        promotionUseReportPage.uncheckActiveStatus();
        promotionUseReportPage.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertThat("Error for status is not generated for selection of status",
            promotionUseReportPage.getStatusErrorMessage(), equalTo("At least one promotion state must be selected."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify promotion view reports.
     */
    @Test
    public void testPromotionUseReportBySubmittingAnOrder() {
        final String promoCode1 = promotionUtils.getRandomPromoCode();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        promotion = promotionUtils.addPromotion(promoType, Lists.newArrayList(store), Lists.newArrayList(basicOffering),
            promoCode1, false, Status.ACTIVE, "10.0", null, DateTimeUtils.getFutureExpirationDate(), null, null, 1,
            MAX_USES, MAX_USES_PER_USER);
        // Submit purchase order
        purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion, basicOffering, 2, null, true, 1);
        final String promoId = promotion.getData().getId();
        int timesUsed = 0;
        try {
            timesUsed = DbUtils.getPromotionDetails(promoId, getEnvironmentVariables());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        promotionUseReportPage.getReportWithCombination(promoId, null, null, null, Status.ACTIVE, null, null, null);

        AssertCollector.assertThat("Incorrect Promotion ID", grid.getColumnValues("Promotion ID").get(0),
            equalTo(promoId), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Code", grid.getColumnValues("Code").get(0),
            equalTo(promotion.getData().getCustomPromoCode()), assertionErrorList);
        assertPromotionName(grid.getColumnValues("Name").get(0), promotion.getData().getName());
        AssertCollector.assertThat("Incorrect Promotion Name",
            (grid.getColumnValues("Description").get(0)).substring(0, 10),
            equalTo((promotion.getData().getDescription()).substring(0, 10)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion State", grid.getColumnValues("State").get(0),
            equalTo(promotion.getData().getStatus().getDisplayName().toUpperCase()), assertionErrorList);
        final String promotionType = promotion.getData().getPromotionType().toString().replace("_", " ");
        AssertCollector.assertThat("Incorrect Promotion Type", grid.getColumnValues("Type").get(0).toUpperCase(),
            equalTo(promotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect Max uses per user", grid.getColumnValues("Max Uses per User").get(0),
            equalTo(String.valueOf(MAX_USES_PER_USER)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Max Uses", grid.getColumnValues("Max Number of Uses").get(0),
            equalTo(String.valueOf(MAX_USES)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Times used", grid.getColumnValues("Times Used").get(0),
            equalTo(String.valueOf(timesUsed)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify promotion Order promotion use reports with Discount Percentage.
     */
    @Test
    public void testPromotionUseReportWithDiscountPercentage() {
        // Create Discount Percentage Promotion
        final String promoCode1 = promotionUtils.getRandomPromoCode();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        promotion = promotionUtils.addPromotion(promoType, Lists.newArrayList(store), Lists.newArrayList(basicOffering),
            promoCode1, false, Status.ACTIVE, "10.0", null, DateTimeUtils.getFutureExpirationDate(), null, null, 1,
            MAX_USES, MAX_USES_PER_USER);
        // Submit purchase order
        purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion, basicOffering, 2, null, true, 1);
        int timesUsed = 0;
        try {
            timesUsed = DbUtils.getPromotionDetails(promotion.getData().getId(), getEnvironmentVariables());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        promotionUseReportPage.getReportWithCombination(promotion.getData().getId(), null, null, null, Status.ACTIVE,
            null, null, null);

        AssertCollector.assertThat("Incorrect Promotion ID", grid.getColumnValues("Promotion ID").get(0),
            equalTo(promotion.getData().getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Code", grid.getColumnValues("Code").get(0),
            equalTo(promotion.getData().getCustomPromoCode()), assertionErrorList);
        assertPromotionName(grid.getColumnValues("Name").get(0), promotion.getData().getName());
        AssertCollector.assertThat("Incorrect Description",
            (grid.getColumnValues("Description").get(0)).substring(0, 10),
            equalTo((promotion.getData().getDescription()).substring(0, 10)), assertionErrorList);

        AssertCollector.assertThat("Incorrect Promotion State", grid.getColumnValues("State").get(0),
            equalTo(promotion.getData().getStatus().getDisplayName().toUpperCase()), assertionErrorList);
        final String promotionType = promotion.getData().getPromotionType().toString().replace("_", " ");
        AssertCollector.assertThat("Incorrect Promotion Type", grid.getColumnValues("Type").get(0).toUpperCase(),
            equalTo(promotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect Max uses per user", grid.getColumnValues("Max Uses per User").get(0),
            equalTo(String.valueOf(MAX_USES_PER_USER)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Max Uses", grid.getColumnValues("Max Number of Uses").get(0),
            equalTo(String.valueOf(MAX_USES)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Times used", grid.getColumnValues("Times Used").get(0),
            equalTo(String.valueOf(timesUsed)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify promotion Order promotion use reports with Discount Amount.
     */
    @Test
    public void testPromotionUseReportWithDiscountAmount() {
        // Create Discount Percentage Promotion
        final String promoCode1 = promotionUtils.getRandomPromoCode();
        final PromotionType promoType = PromotionType.DISCOUNT_AMOUNT;
        promotion = promotionUtils.addPromotion(promoType, Lists.newArrayList(store), Lists.newArrayList(basicOffering),
            promoCode1, false, Status.ACTIVE, null, "100.0", DateTimeUtils.getFutureExpirationDate(), null, null, 1,
            MAX_USES, MAX_USES_PER_USER);
        // Submit purchase order
        purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion, basicOffering, 2, null, true, 1);
        int timesUsed = 0;
        try {
            timesUsed = DbUtils.getPromotionDetails(promotion.getData().getId(), getEnvironmentVariables());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        promotionUseReportPage.getReportWithCombination(promotion.getData().getId(), null, null, null, Status.ACTIVE,
            null, null, null);

        AssertCollector.assertThat("Incorrect Promotion ID", grid.getColumnValues("Promotion ID").get(0),
            equalTo(promotion.getData().getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Code", grid.getColumnValues("Code").get(0),
            equalTo(promotion.getData().getCustomPromoCode()), assertionErrorList);
        assertPromotionName(grid.getColumnValues("Name").get(0), promotion.getData().getName());
        AssertCollector.assertThat("Incorrect Description",
            (grid.getColumnValues("Description").get(0)).substring(0, 10),
            equalTo((promotion.getData().getDescription()).substring(0, 10)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion State", grid.getColumnValues("State").get(0),
            equalTo(promotion.getData().getStatus().getDisplayName().toUpperCase()), assertionErrorList);
        final String promotionType = promotion.getData().getPromotionType().toString().replace("_", " ");
        AssertCollector.assertThat("Incorrect Promotion Type", grid.getColumnValues("Type").get(0).toUpperCase(),
            equalTo(promotionType), assertionErrorList);
        AssertCollector.assertThat("Incorrect Max uses per user", grid.getColumnValues("Max Uses per User").get(0),
            equalTo(String.valueOf(MAX_USES_PER_USER)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Max Uses", grid.getColumnValues("Max Number of Uses").get(0),
            equalTo(String.valueOf(MAX_USES)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Times used", grid.getColumnValues("Times Used").get(0),
            equalTo(String.valueOf(timesUsed)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies all the required Headers of a Report in downloaded file
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @result Report should be opened with the column of all Headers which are 13 in number
     */
    @Test
    public void testPromotionUseReportHeadersInDownloadReport() throws IOException {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        promotionUseReportPage.getReportWithCombination(null, null, null, null, Status.ACTIVE, null, null,
            PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(TOTAL_COLUMNS_IN_DOWNLOAD_REPORT), assertionErrorList);
        AssertCollector.assertThat("First Column should be *Promotion ID*", fileData[0][0], equalTo("Promotion ID"),
            assertionErrorList);
        AssertCollector.assertThat("Second Column Should be *Code*", fileData[0][1], equalTo("Code"),
            assertionErrorList);
        AssertCollector.assertThat("Third Column Should be *Name*", fileData[0][2], equalTo("Name"),
            assertionErrorList);
        AssertCollector.assertThat("Forth Column Should be *Description*", fileData[0][3], equalTo("Description"),
            assertionErrorList);
        AssertCollector.assertThat("Fifth Column Should be *State*", fileData[0][4], equalTo("State"),
            assertionErrorList);

        AssertCollector.assertThat("Sixth Column Should be *Store Wide*", fileData[0][5], equalTo("Store Wide"),
            assertionErrorList);
        AssertCollector.assertThat("Seventh Column Should be *Type*", fileData[0][6], equalTo("Type"),
            assertionErrorList);
        AssertCollector.assertThat("Eighth Column Should be *Effective Start Date*", fileData[0][7],
            equalTo("Effective Start Date"), assertionErrorList);
        AssertCollector.assertThat("Ninth Column Should be *Effective End Date*", fileData[0][8],
            equalTo("Effective End Date"), assertionErrorList);
        AssertCollector.assertThat("Tenth Column Should be *Max Number of Uses*", fileData[0][9],
            equalTo("Max Number of Uses"), assertionErrorList);
        AssertCollector.assertThat("Eleventh Column Should be *Max Uses per User*", fileData[0][10],
            equalTo("Max Uses per User"), assertionErrorList);
        AssertCollector.assertThat("Twelfth Column Should be *Times Used*", fileData[0][11], equalTo("Times Used"),
            assertionErrorList);
        AssertCollector.assertThat("Thirteenth Column Should be *Bundled*", fileData[0][12], equalTo("Bundled"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion column exist on Promotion Use Report Page.
     */
    @Test(dataProvider = "promotions")
    public void testPromotionUseReportWithBundledForView(final JPromotion promotion) {

        promotionUseReportPage.getReportWithCombination(promotion.getData().getId(), null, null, null, Status.ACTIVE,
            null, null, PelicanConstants.VIEW);

        AssertCollector.assertThat("Incorrect Promotion ID", grid.getColumnValues("Promotion ID").get(0),
            equalTo(promotion.getData().getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Code", grid.getColumnValues("Code").get(0),
            equalTo(promotion.getData().getCustomPromoCode()), assertionErrorList);
        assertPromotionName(grid.getColumnValues("Name").get(0), promotion.getData().getName());
        AssertCollector.assertThat("Incorrect Promotion Description",
            (grid.getColumnValues("Description").get(0)).substring(0, 10),
            equalTo((promotion.getData().getDescription()).substring(0, 10)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion State", grid.getColumnValues("State").get(0),
            equalTo(promotion.getData().getStatus().getDisplayName().toUpperCase()), assertionErrorList);
        final String promotionType = promotion.getData().getPromotionType().toString().replace("_", " ");
        AssertCollector.assertThat("Incorrect Promotion Type", grid.getColumnValues("Type").get(0).toUpperCase(),
            equalTo(promotionType), assertionErrorList);
        final String isBundled = promotion.getData().getIsBundledPromo() ? "Yes" : "No";
        AssertCollector.assertThat("Incorrect Promotion Bundle", grid.getColumnValues("Bundled").get(0).toUpperCase(),
            equalTo(isBundled.toUpperCase()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Use count", grid.getColumnValues("Times Used").get(0),
            equalTo("1"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion column exist on Promotion Use Report Page.
     */
    @Test(dataProvider = "promotions")
    public void testPromotionUseReportWithBundledForDownload(final JPromotion promotion) throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        promotionUseReportPage.getReportWithCombination(promotion.getData().getId(), null, null, null, Status.ACTIVE,
            null, null, PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(TOTAL_COLUMNS_IN_DOWNLOAD_REPORT), assertionErrorList);

        AssertCollector.assertThat("Incorrect Promotion ID", fileData[1][0], equalTo(promotion.getData().getId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion Code", fileData[1][1],
            equalTo(promotion.getData().getCustomPromoCode()), assertionErrorList);
        assertPromotionName(fileData[1][2], promotion.getData().getName());
        AssertCollector.assertThat("Incorrect Promotion Description", (fileData[1][3]).substring(0, 10),
            equalTo((promotion.getData().getDescription()).substring(0, 10)), assertionErrorList);
        AssertCollector.assertThat("Incorrect Promotion State", fileData[1][4],
            equalTo(promotion.getData().getStatus().getDisplayName().toUpperCase()), assertionErrorList);
        final String promotionType = promotion.getData().getPromotionType().toString().replace("_", " ");
        AssertCollector.assertThat("Incorrect Promotion Type", fileData[1][6].toUpperCase(), equalTo(promotionType),
            assertionErrorList);
        final String isBundled = promotion.getData().getIsBundledPromo() ? "Yes" : "No";
        AssertCollector.assertThat("Incorrect Promotion Bundle", fileData[1][12].toUpperCase(),
            equalTo(isBundled.toUpperCase()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * combination of filters use to generate promotion use report
     */
    @DataProvider(name = "combinationOfOptions")
    public Object[][] getTestData() {
        return new Object[][] { { null, promoCode, "YES", "Discount", Status.ACTIVE, null, null },

                { null, null, "NO", "Discount", Status.ACTIVE, null, null },
                { null, null, "YES", "Discount", Status.CANCELLED, null, null },

                { null, null, "YES", "Supplement", Status.NEW, null, null },
                { null, null, "YES", "Supplement", Status.EXPIRED, null, null },

                { null, null, null, null, Status.ACTIVE, START_DATE, END_DATE },
                { null, null, null, null, Status.CANCELLED, START_DATE, END_DATE } };
    }

    /**
     * This is a data provider to return promotion objects
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "promotions")
    public static Object[][] getPromotions() {

        return new Object[][] { { newNonStoreWideDiscountAmountPromo }, { newStoreWidePercentPromo },
                { newNonStoreWideDiscountPromoWithBasicOfferingAndSubscriptionOffering } };
    }

    /**
     * This method returns an index of promotion randomly from first page.
     *
     * @return promotion from first page
     */
    private int selectPromoRandomlyFromFirstPage(final int promosCount) {
        if (promosCount > 0) {
            int promoIndex;
            final Random index = new Random();
            if (promosCount <= 20) {
                promoIndex = index.nextInt(promosCount);
            } else {
                promoIndex = index.nextInt(19);
            }
            LOGGER.info("Random index:" + promoIndex);
            return promoIndex;
        } else {
            LOGGER.info("No promotion exists with selected filters");
            return 0;
        }
    }

    private void assertPromotionName(final String promotionNameInReport, final String promotionNameOnDetailPage) {
        if (promotionNameInReport.length() > 10) {
            AssertCollector.assertThat("Name of the promo is not same as shown in Report",
                promotionNameInReport.substring(0, 10), equalTo(promotionNameOnDetailPage.substring(0, 10)),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Name of the promo is not same as shown in Report", promotionNameInReport,
                equalTo(promotionNameOnDetailPage), assertionErrorList);
        }
    }
}
