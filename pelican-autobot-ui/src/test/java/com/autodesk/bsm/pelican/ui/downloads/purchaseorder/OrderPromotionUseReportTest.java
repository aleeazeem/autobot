package com.autodesk.bsm.pelican.ui.downloads.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PromotionUserReportConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.OrderPromotionUseReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class test all required Scenarios of Order Promotion Use Report.
 *
 * @author t_joshv
 */
public class OrderPromotionUseReportTest extends SeleniumWebdriver {

    private static OrderPromotionUseReportPage orderPromotionUseReportPage;
    private static GenericGrid reportGrid;
    private static final String ACTUAL_FILE_NAME = "OrderPromotionUseReport.xlsx";
    private static final int DEFAULT_BILLING_CYCLES = 1;
    private static final String CASH_AMOUNT = "100.21";
    private static final String PERCENTAGE_AMOUNT = "10";
    private String purchaseOrderIdForBundlePromo;
    private String purchaseOrderIdForRegularPromo;
    private static JPromotion bundledPromo;
    private static JPromotion regularPromo;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderPromotionUseReportTest.class.getSimpleName());

    /**
     * Data setup - open a admin tool page and login into it
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        orderPromotionUseReportPage = adminToolPage.getPage(OrderPromotionUseReportPage.class);
        reportGrid = adminToolPage.getPage(GenericGrid.class);
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        // Add basic offering
        final Offerings basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, 500, UsageType.COM, null, null);
        final String priceIdForBasicOffering = basicOffering.getIncluded().getPrices().get(0).getId();

        // Add subscription plans
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String priceIdForBicOffering = bicOffering.getIncluded().getPrices().get(0).getId();

        // Create Regular Promotion for non storewide
        regularPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
            Lists.newArrayList(basicOffering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE, null,
            CASH_AMOUNT, DateTimeUtils.getUTCFutureExpirationDate(), null, null, DEFAULT_BILLING_CYCLES, null, null);

        // Create Bundled Promo
        final List<BundlePromoOfferings> offeringForBundledPromo = new ArrayList<>();
        offeringForBundledPromo.add(promotionUtils.createBundlePromotionOffering(basicOffering, 1, true));
        offeringForBundledPromo.add(promotionUtils.createBundlePromotionOffering(bicOffering, 1, true));

        bundledPromo = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), offeringForBundledPromo, promotionUtils.getRandomPromoCode(), true,
            Status.ACTIVE, PERCENTAGE_AMOUNT, null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("bundledPromo id: " + bundledPromo.getData().getId());

        final UserUtils userUtils = new UserUtils();
        final BuyerUser buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        final Map<String, Integer> priceQuantityMap1 = new HashMap<>();
        priceQuantityMap1.put(priceIdForBasicOffering, 2);

        final PromotionReferences promotionReferences1 = new PromotionReferences();
        final PromotionReference promotionReference1 = new PromotionReference();
        promotionReference1.setId(regularPromo.getData().getId());
        promotionReferences1.setPromotionReference(promotionReference1);

        final HashMap<String, PromotionReferences> pricePromoReferencesMap1 = new HashMap<>();
        pricePromoReferencesMap1.put(basicOffering.getIncluded().getPrices().get(0).getId(), promotionReferences1);

        // Submit and Process Purchase Order for Regular Promotion:
        final PurchaseOrder purchaseOrderForRegularPromo =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap1, false,
                PaymentType.CREDIT_CARD, pricePromoReferencesMap1, null, buyerUser);
        purchaseOrderIdForRegularPromo = purchaseOrderForRegularPromo.getId();

        // submit a purchase order for Bundled Promo
        final Map<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBasicOffering, 2);
        priceQuantityMap.put(priceIdForBicOffering, 2);

        final PromotionReferences promotionReferences = new PromotionReferences();
        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(bundledPromo.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);

        final Map<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
        pricePromoReferencesMap.put(basicOffering.getIncluded().getPrices().get(0).getId(), promotionReferences);
        pricePromoReferencesMap.put(bicOffering.getIncluded().getPrices().get(0).getId(), promotionReferences);

        // submit a purchase order
        final PurchaseOrder purchaseOrderForBundledPromo =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap, false,
                PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
        purchaseOrderIdForBundlePromo = purchaseOrderForBundledPromo.getId();
    }

    /**
     * Verify all headers of columns in Report
     *
     * @result report should show 8 columns and name of the headers of each column should be Order Promotion, Type,
     *         Store Wide, Store, Order, Buyer External Key, Total Promo Discount Applied and Bundled.
     */
    @Test
    public void testOrderPromotionUseReportHeaders() {

        orderPromotionUseReportPage.viewOrDownloadReport("", PelicanConstants.VIEW);

        AssertCollector.assertThat("Total number of columns are not correct", reportGrid.getColumnHeaders().size(),
            equalTo(PromotionUserReportConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", reportGrid.getColumnHeaders().get(0),
            equalTo(PromotionUserReportConstants.PROMOTION), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", reportGrid.getColumnHeaders().get(1),
            equalTo(PromotionUserReportConstants.TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", reportGrid.getColumnHeaders().get(2),
            equalTo(PromotionUserReportConstants.STORE_WIDE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", reportGrid.getColumnHeaders().get(3),
            equalTo(PromotionUserReportConstants.STORE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", reportGrid.getColumnHeaders().get(4),
            equalTo(PromotionUserReportConstants.ORDER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", reportGrid.getColumnHeaders().get(5),
            equalTo(PromotionUserReportConstants.BUYER_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", reportGrid.getColumnHeaders().get(6),
            equalTo(PromotionUserReportConstants.TOTAL_PROMOTION_DISCOUNT_APPLIED), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", reportGrid.getColumnHeaders().get(7),
            equalTo(PromotionUserReportConstants.BUNDLED), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all headers of columns in download Report.
     *
     * @result report should show 8 columns and name of the headers of each column should be Order Promotion, Type,
     *         Store Wide, Store, Order, Buyer External Key, Total Promo Discount Applied and Bundled.
     */
    @Test
    public void testOrderPromotionUseReportHeaderInDownloadFile() throws IOException {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        orderPromotionUseReportPage.viewOrDownloadReport("", PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(PromotionUserReportConstants.TOTAL_COLUMNS_INFILE_ORDER_PROMOTION_USER_REPORT), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0],
            equalTo(PromotionUserReportConstants.PROMOTION_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1],
            equalTo(PromotionUserReportConstants.PROMOTION_NAME), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2], equalTo(PromotionUserReportConstants.TYPE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3],
            equalTo(PromotionUserReportConstants.STORE_WIDE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", fileData[0][4], equalTo(PromotionUserReportConstants.STORE_ID),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", fileData[0][5],
            equalTo(PromotionUserReportConstants.STORE_NAME), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", fileData[0][6], equalTo(PromotionUserReportConstants.ORDER_ID),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", fileData[0][7],
            equalTo(PromotionUserReportConstants.BUYER_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", fileData[0][8],
            equalTo(PromotionUserReportConstants.TOTAL_PROMOTION_DISCOUNT_APPLIED), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 10", fileData[0][9],
            equalTo(PromotionUserReportConstants.CURRENCY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 11", fileData[0][10],
            equalTo(PromotionUserReportConstants.BUNDLED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify, Bundle Promotion column exist on Order Promotion Use Report Page.
     *
     * @param promotion
     * @param purchaseOrderId purchase order id.
     */
    @Test(dataProvider = "promotionorders")
    public void testOrderPromotionUseReportWithBundledInViewReport(final JPromotion promotion,
        final String purchaseOrderId) {
        orderPromotionUseReportPage.viewOrDownloadReport(promotion.getData().getId(), PelicanConstants.VIEW);

        final String promotionValue = promotion.getData().getName() + " (" + promotion.getData().getId() + ")";

        AssertCollector.assertThat("Incorrect Promotion ID", reportGrid.getColumnValues("Promotion").get(0),
            equalTo(promotionValue), assertionErrorList);

        final String promotionType = promotion.getData().getPromotionType().toString().replace("_", " ");
        AssertCollector.assertThat("Incorrect Type", (reportGrid.getColumnValues("Type").get(0)).toUpperCase(),
            equalTo(promotionType), assertionErrorList);

        final String isStoreWide = promotion.getData().isStoreWide() ? "Yes" : "No";

        AssertCollector.assertThat("Incorrect Store Wide", (reportGrid.getColumnValues("Store Wide").get(0)),
            equalTo(isStoreWide), assertionErrorList);

        final String storeValue = getStoreUs().getName() + " (" + getStoreIdUs() + ")";
        AssertCollector.assertThat("Incorrect Store", reportGrid.getColumnValues("Store").get(0),
            equalTo((storeValue).toUpperCase()), assertionErrorList);

        AssertCollector.assertThat("Incorrect Order", reportGrid.getColumnValues("Order").get(0),
            equalTo(purchaseOrderId), assertionErrorList);

        final String isBundled = promotion.getData().getIsBundledPromo() ? "Yes" : "No";
        AssertCollector.assertThat("Incorrect Promotion Bundle",
            reportGrid.getColumnValues("Bundled").get(0).toUpperCase(), equalTo(isBundled.toUpperCase()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider to return promotion & order objects
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "promotionorders")
    public Object[][] getPromotionOrders() {

        return new Object[][] { { bundledPromo, purchaseOrderIdForBundlePromo },
                { regularPromo, purchaseOrderIdForRegularPromo } };
    }
}
