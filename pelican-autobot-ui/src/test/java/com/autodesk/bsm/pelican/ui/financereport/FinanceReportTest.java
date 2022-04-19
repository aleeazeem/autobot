package com.autodesk.bsm.pelican.ui.financereport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.FinanceReportClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionRenewal;
import com.autodesk.bsm.pelican.constants.FinanceReportHeaderConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.FinanceReportHeaders;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.FinanceReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Tool's Finance Reports tests.
 * <p>
 * Validate the correct header and data text are in csv format. This test will ran in Demo application for DEV
 *
 * @author Vineel
 */
public class FinanceReportTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String bicPrice1;
    private String metaPrice1;
    private String priceIdForBasicOffering1;
    private String priceIdForBasicOffering2;
    private String priceIdForMetaSubscriptionPlanOffer;
    private String priceIdForBicSubscriptionPlanOffer;
    private JPromotion bundledPromotionWithBasicOffering;
    private JPromotion bundledPromoForSubscriptionPlanOffers;
    private JPromotion activeNonStoreWideDiscountAmountPromo;
    private static final String SELECT_SUBSCRIPTION_PERIOD_QUERY =
        "select SUBSCRIPTION_PERIOD_START_DATE, SUBSCRIPTION_PERIOD_END_DATE from finance_report where SUBSCRIPTION_ID = %s and PURCHASE_ORDER_ID = %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceReportTest.class.getSimpleName());
    private FinanceReportPage financeReportPage;
    private BuyerUser buyerUser;

    /**
     * Data setup - create Product Lines if not available
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        financeReportPage = adminToolPage.getPage(FinanceReportPage.class);
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());

        bicPrice1 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
            .getIncluded().getPrices().get(0).getId();

        metaPrice1 =
            subscriptionPlanApiUtils
                .addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                    BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
                .getIncluded().getPrices().get(0).getId();

        // create Basic Offerings
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PHYSICAL_MEDIA, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        priceIdForBasicOffering1 = basicOffering1.getIncluded().getPrices().get(0).getId();

        final Offerings basicOffering2 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PHYSICAL_MEDIA, MediaType.ELD, Status.ACTIVE, UsageType.COM, null);
        priceIdForBasicOffering2 = basicOffering2.getIncluded().getPrices().get(0).getId();

        final Offerings metaSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceIdForMetaSubscriptionPlanOffer = metaSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        final Offerings bicSubscriptionOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceIdForBicSubscriptionPlanOffer = bicSubscriptionOffering.getIncluded().getPrices().get(0).getId();

        // Promotion : Basic Offerings bundled promo
        final List<BundlePromoOfferings> offeringsForBasicBundledPromo = new ArrayList<>();
        offeringsForBasicBundledPromo.add(promotionUtils.createBundlePromotionOffering(basicOffering1, 1, true));
        offeringsForBasicBundledPromo.add(promotionUtils.createBundlePromotionOffering(basicOffering2, 1, true));

        bundledPromotionWithBasicOffering = promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE,
            Lists.newArrayList(getStoreUs()), offeringsForBasicBundledPromo, promotionUtils.getRandomPromoCode(), false,
            Status.ACTIVE, "8", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("basicBundledPromo id: " + bundledPromotionWithBasicOffering.getData().getId());
        LOGGER.info("basicBundledPromo Name: " + bundledPromotionWithBasicOffering.getData().getName());

        // Add to Map
        final Map<String, JPromotion> promotionsMap = new HashMap<>();
        promotionsMap.put(bundledPromotionWithBasicOffering.getData().getId(), bundledPromotionWithBasicOffering);

        // Promotion : Subscription Offer Bundled promo
        final List<BundlePromoOfferings> offeringsForSubscriptionPlanBundledPromo = new ArrayList<>();
        offeringsForSubscriptionPlanBundledPromo
            .add(promotionUtils.createBundlePromotionOffering(bicSubscriptionOffering, 1, true));
        offeringsForSubscriptionPlanBundledPromo
            .add(promotionUtils.createBundlePromotionOffering(metaSubscriptionOffering, 1, true));

        bundledPromoForSubscriptionPlanOffers =
            promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
                offeringsForSubscriptionPlanBundledPromo, promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                "8", null, DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);
        LOGGER.info("subscriptionBundledPromo id: " + bundledPromoForSubscriptionPlanOffers.getData().getId());
        LOGGER.info("subscriptionBundledPromo Name: " + bundledPromoForSubscriptionPlanOffers.getData().getName());

        promotionsMap.put(bundledPromotionWithBasicOffering.getData().getId(), bundledPromotionWithBasicOffering);
        promotionsMap.put(bundledPromoForSubscriptionPlanOffers.getData().getId(),
            bundledPromoForSubscriptionPlanOffers);

        // Create Regular Promotion.
        activeNonStoreWideDiscountAmountPromo =
            promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT, Lists.newArrayList(getStoreUs()),
                Lists.newArrayList(bicSubscriptionOffering), promotionUtils.getRandomPromoCode(), false, Status.ACTIVE,
                null, "10", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 3, null, null);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * Enter valid date and generate report with data
     *
     * @result Valid header and data is returned
     */
    @Test
    public void generateReportWithPastDateRange() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        // get one date to change Order Date.
        final String getDateInPast = DateTimeUtils.getNowMinusDays(PelicanConstants.AUDIT_LOG_DATE_FORMAT, 2);

        // Updating DB for Finance Report table with above created date.
        DbUtils.updateQuery(String.format(PelicanDbConstants.SQL_QUERY_TO_UPDATE_FINANCE_REPORT_TABLE, getDateInPast,
            purchaseOrder.getId()), getEnvironmentVariables());

        final List<String> dateEntityList = DateTimeUtils.getPastStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);
        final String expStartDateTime = dateEntityList.get(0);
        final String expEndDateTime = dateEntityList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setStartDate(expStartDate);
        financeReportPage.setEndDate(expEndDate);
        financeReportPage.setOrderStartTime(expStartTime);
        financeReportPage.setOrderEndTime(expEndTime);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate admin tool's header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        AssertCollector.assertThat("Admin Tool: Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Admin Tool: Got no data", financeReportPage.getReportData().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualAdminOrderDates =
            financeReportPage.getReportValues(FinanceReportHeaders.ORDER_DATE.getHeader());
        AssertCollector.assertThat("AdminTool: Found order date outside of requested range", actualAdminOrderDates,
            everyItem(allOf(greaterThanOrEqualTo(expStartDateTime), lessThanOrEqualTo(expEndDateTime))),
            assertionErrorList);

        // Validate rest api's header
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        AssertCollector.assertThat("REST: Incorrect header", actualRestHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate order date is correct in api's report
        final List<String> actualRestOrderDates = resource.financeReport().getColumnValues(expStartDateTime,
            expEndDateTime, FinanceReportHeaders.ORDER_DATE.getHeader(), null, null, null, null);
        AssertCollector.assertThat("REST: Found order date outside of requested range", actualRestOrderDates,
            everyItem(allOf(greaterThanOrEqualTo(expStartDateTime), lessThanOrEqualTo(expEndDateTime))),
            assertionErrorList);

        // Validate the data row count from admin tool = rest
        AssertCollector.assertThat("Incorrect report data count from admin tool and rest", actualAdminOrderDates.size(),
            lessThanOrEqualTo(actualRestOrderDates.size()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void generateReportForToday() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);
        priceQuantityMap.put(metaPrice1, 5);
        purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);
        final String expStartDateTime = entityDateList.get(0);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        // setting end date as same day midnight!
        final String expEndDateTime = expStartDate + " 23:59:59";
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];

        financeReportPage.setStartDate(expStartDate);
        financeReportPage.setEndDate(expEndDate);
        financeReportPage.setOrderStartTime(expStartTime);
        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        AssertCollector.assertThat("Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate some data is returned
        AssertCollector.assertThat("Got no data", financeReportPage.getReportData().size(), greaterThanOrEqualTo(1),
            assertionErrorList);

        // Validate order date is within requested range
        final List<String> actualAdminOrderDates =
            financeReportPage.getReportValues(FinanceReportHeaders.ORDER_DATE.getHeader());
        AssertCollector.assertThat("Found order date outside of requested range", actualAdminOrderDates,
            everyItem(allOf(greaterThanOrEqualTo(expStartDateTime), lessThanOrEqualTo(expEndDateTime))),
            assertionErrorList);

        // Validate rest api's header
        final FinanceReportClient rest =
            new FinanceReportClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        final String actualRestHeader = rest.getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        AssertCollector.assertThat("REST: Incorrect header", actualRestHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate order date is correct in api's report
        final List<String> actualRestOrderDates = rest.getColumnValues(expStartDateTime, expEndDateTime,
            FinanceReportHeaders.ORDER_DATE.getHeader(), null, null, null, null);
        AssertCollector.assertThat("REST: Found order date outside of requested range", actualRestOrderDates,
            everyItem(allOf(greaterThanOrEqualTo(expStartDateTime), lessThanOrEqualTo(expEndDateTime))),
            assertionErrorList);

        // Validate the data row count from admin tool = rest
        AssertCollector.assertThat("Incorrect report data count from admin tool and rest", actualAdminOrderDates.size(),
            lessThanOrEqualTo(actualRestOrderDates.size()), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Enter valid date and generate report with no data
     *
     * @result Valid header and zero row of data is returned
     */
    @Test
    public void generateReportWithoutData() {

        final List<String> entityDateList = DateTimeUtils.getFutureStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);
        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setStartDate(expStartDate);
        financeReportPage.setEndDate(expEndDate);
        financeReportPage.setOrderStartTime(expStartTime);
        financeReportPage.setOrderEndTime(expEndTime);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        AssertCollector.assertThat("Incorrect header", actualAdminHeader, equalTo(getExpectedHeader()),
            assertionErrorList);

        // Validate no data is returned
        AssertCollector.assertThat("There's data!", financeReportPage.getReportData().size(), equalTo(0),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Enter date range past 31 days
     *
     * @result Error returned
     */
    @Test
    public void generateReportWithOutOfRangeDate() {

        final List<String> entityDateList =
            DateTimeUtils.getStartDateAndEndDateGreaterRanges(PelicanConstants.DB_DATE_FORMAT);
        // Get only the date part from the Date Time
        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setStartDate(expStartDate);
        financeReportPage.setEndDate(expEndDate);
        financeReportPage.setOrderStartTime(expStartTime);
        financeReportPage.setOrderEndTime(expEndTime);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Incorrect error message found", financeReportPage.getH3ErrorMessage(),
            equalTo(PelicanErrorConstants.INVALID_LAST_MODIFIED_DATE_RANGE.split("\\. ")[1]
                + getEnvironmentVariables().getFinanceReportDateRange() + " " + PelicanConstants.DAYS_LOWER_CASE),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /*
     * This test case determines whether the last modified date field is added to the finance report or not
     */
    @Test
    public void testLastModifiedDateFieldInFinanceReport() {
        final List<String> entityDateList = DateTimeUtils.getFutureStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo("Last Modified Date"), assertionErrorList);

        // Validate no data is returned
        AssertCollector.assertThat("There's data!", financeReportPage.getReportData().size(), equalTo(0),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This test case tests whether the new field last modified date is added to the finance report or not
     */
    @Test
    public void testIsLastModifiedFieldPresentInTheSearchPage() {

        financeReportPage.navigateToSearchPage();
        final List<WebElement> elementList = financeReportPage.findLastModifiedDateWebElementsOnPage();
        AssertCollector.assertThat("Last modified date is not present in the page", elementList.size(), equalTo(2),
            assertionErrorList);
        AssertCollector.assertThat("Last modified date from field cannot take user input",
            elementList.get(0).getTagName(), equalTo("input"), assertionErrorList);
        AssertCollector.assertThat("Last modified date to field cannot take user input",
            elementList.get(1).getTagName(), equalTo("input"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /*
     * This test case tests whether the last modified date field is populated correctly for all charged orders
     */
    @Test
    public void testLastModifiedDateFieldForChargedOrders() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);
        priceQuantityMap.put(metaPrice1, 5);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo("Last Modified Date"), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, purchaseOrderId);

        HelperForFinanceReport.assertionsForFinanceReport(poRecordsList, purchaseOrderId, reportData,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This test case will test the last modified date field for fulfilled orders
     */
    @Test
    public void testLastModifiedDateFieldForFulfilledOrders() {

        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(metaPrice1, 5);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        final String createdPurchaseOrderId = purchaseOrder.getId();

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo("Last Modified Date"), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, createdPurchaseOrderId);
        String initialLastModifiedDate = null;
        for (final Integer aPoRecordsList1 : poRecordsList) {

            final String requiredData = reportData.get(aPoRecordsList1);

            final String[] dataArray = requiredData.split(",");
            initialLastModifiedDate = dataArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];

            // Validate correct data is returned
            AssertCollector.assertThat("Incorrect purchase order id", dataArray[0], equalTo(purchaseOrder.getId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect last modified date", initialLastModifiedDate, is(notNullValue()),
                assertionErrorList);
        }

        // Fulfill the purchase order
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(createdPurchaseOrderId);
        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        final List<String> actualReportData = financeReportPage.getReportData();
        for (final Integer aPoRecordsList : poRecordsList) {
            final String actualRequiredData = actualReportData.get(aPoRecordsList);

            final String[] actualDataArray = actualRequiredData.split(",");
            final String finalLastModifiedDate =
                actualDataArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];

            // Validate correct data is returned
            AssertCollector.assertThat("Incorrect purchase order id", actualDataArray[0],
                equalTo(purchaseOrder.getId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect last modified date", finalLastModifiedDate, is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Last modified date field not modified after fulfillment", finalLastModifiedDate,
                is(not(initialLastModifiedDate)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This test case tests the last modified date field for the refund orders
     */
    @Test
    public void testLastModifiedDateForRefundOrders() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);
        priceQuantityMap.put(metaPrice1, 5);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo("Last Modified Date"), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, purchaseOrderId);
        HelperForFinanceReport.assertionsForFinanceReport(poRecordsList, purchaseOrderId, reportData,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * The test case tests the last modified date for the renewal orders in the finance report
     */
    @Test
    public void testLastModifiedDateForRenewalOrders() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicPrice1, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        final List<String> subscriptionIdList = new ArrayList<>();
        subscriptionIdList.add(
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId());
        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitAndProcessRenewalPurchaseOrder(buyerUser,
            subscriptionIdList, false, PaymentType.CREDIT_CARD, null, true);
        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();

        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo("Last Modified Date"), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, renewalPurchaseOrderId);
        HelperForFinanceReport.assertionsForFinanceReport(poRecordsList, renewalPurchaseOrderId, reportData,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * The test case tests the Basic Offerings bundle promo for purchase orders in the finance report
     */
    @Test
    public void testBasicOfferingsBundlePromoPurchaseOrderInFinanceReport() {
        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering1, 1,
            bundledPromotionWithBasicOffering.getData().getId(), "5");

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 1,
            bundledPromotionWithBasicOffering.getData().getId(), "5");

        final LineItem lineitem3 = purchaseOrderUtils.createOfferingLineItem(priceIdForBasicOffering2, 1, null, "5");

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // Create a purchase order using lineitems.
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        final String purchaseOrderId = purchaseOrder.getId();
        LOGGER.info("Purchase Order ID :" + purchaseOrderId);

        // Get start date and end date filter for finance report
        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = (DateTimeUtils.getDateAndTimePartFromDateTime(expStartDateTime))[0];
        final String expEndDate = (DateTimeUtils.getDateAndTimePartFromDateTime(expEndDateTime))[0];
        final String expStartTime = (DateTimeUtils.getDateAndTimePartFromDateTime(expStartDateTime))[1];
        final String expEndTime = (DateTimeUtils.getDateAndTimePartFromDateTime(expEndDateTime))[1];

        // Generate Finance Report
        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo(PelicanConstants.LAST_MODIFIED_DATE_FIELD), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, purchaseOrderId);
        for (int i = 0; i < poRecordsList.size(); i++) {
            final String requiredData = reportData.get(poRecordsList.get(i));
            final String[] dataArray = requiredData.split(",");

            // Validate correct data is returned
            AssertCollector.assertThat("Incorrect purchase order id", dataArray[0], equalTo(purchaseOrder.getId()),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Product Id",
                dataArray[FinanceReportHeaderConstants.PRODUCT_ID_POSITION], equalTo(purchaseOrder.getLineItems()
                    .getLineItems().get(i).getOffering().getOfferingRequest().getOfferingExternalKey()),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect last modified date",
                dataArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION], is(notNullValue()),
                assertionErrorList);

            final Float discountedAmountInResponse = purchaseOrder.getLineItems().getLineItems().get(i).getOffering()
                .getOfferingResponse().getChargeDetails().getPromotionDiscount();
            final Float discountedAmountInReport =
                Float.parseFloat(dataArray[FinanceReportHeaderConstants.UNIT_DISCOUNT_POSITION]);
            AssertCollector.assertThat("Incorrect Promotion Discount Amount", discountedAmountInReport.toString(),
                equalTo(discountedAmountInResponse.toString()), assertionErrorList);

            if (purchaseOrder.getLineItems().getLineItems().get(i).getPromotionReferences() != null) {
                AssertCollector.assertThat("Incorrect Promotion Name :",
                    dataArray[FinanceReportHeaderConstants.PROMOTION_NAME],
                    equalTo(bundledPromotionWithBasicOffering.getData().getName()), assertionErrorList);
            }

            final Float amountCharged = purchaseOrder.getLineItems().getLineItems().get(i).getOffering()
                .getOfferingResponse().getChargeDetails().getAmountCharged();
            final Float totalPrice = Float.parseFloat(dataArray[FinanceReportHeaderConstants.TOTAL_PRICE]);
            AssertCollector.assertThat("Incorrect Amount Charged :", totalPrice.toString(),
                equalTo(amountCharged.toString()), assertionErrorList);
            // LIST_PRICE
            AssertCollector.assertThat("Incorrect List Price :", dataArray[FinanceReportHeaderConstants.LIST_PRICE],
                equalTo(purchaseOrder.getLineItems().getLineItems().get(i).getOffering().getOfferingResponse()
                    .getChargeDetails().getUnitPrice()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * The test case tests the Subscription Offer bundle promo for purchase orders in the finance report
     */
    @Test
    public void testSubscriptionOfferBundlePromoPurchaseOrderInFinanceReport() {
        // Create LineItem
        final LineItem lineitem1 = purchaseOrderUtils.createOfferingLineItem(priceIdForMetaSubscriptionPlanOffer, 1,
            bundledPromoForSubscriptionPlanOffers.getData().getId(), "5");

        final LineItem lineitem2 = purchaseOrderUtils.createOfferingLineItem(priceIdForBicSubscriptionPlanOffer, 1,
            bundledPromoForSubscriptionPlanOffers.getData().getId(), "5");

        final LineItem lineitem3 =
            purchaseOrderUtils.createOfferingLineItem(priceIdForBicSubscriptionPlanOffer, 1, null, "5");

        final List<LineItem> lineItems = Arrays.asList(lineitem1, lineitem2, lineitem3);

        // Create a purchase order using lineitems.
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessPurchaseOrderWithLineItems(lineItems,
            PaymentType.CREDIT_CARD, buyerUser);

        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        final String purchaseOrderId = purchaseOrder.getId();
        LOGGER.info("Purchase Order ID :" + purchaseOrderId);

        // Get start date and end date filter for finance report
        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = (DateTimeUtils.getDateAndTimePartFromDateTime(expStartDateTime))[0];
        final String expEndDate = (DateTimeUtils.getDateAndTimePartFromDateTime(expEndDateTime))[0];
        final String expStartTime = (DateTimeUtils.getDateAndTimePartFromDateTime(expStartDateTime))[1];
        final String expEndTime = (DateTimeUtils.getDateAndTimePartFromDateTime(expEndDateTime))[1];

        // Generate Finance Report
        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo("Last Modified Date"), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, purchaseOrderId);
        for (int i = 0; i < poRecordsList.size(); i++) {
            final String requiredData = reportData.get(poRecordsList.get(i));
            final String[] dataArray = requiredData.split(",");

            AssertCollector.assertThat("Incorrect purchase order id", dataArray[0], equalTo(purchaseOrder.getId()),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Product Id",
                dataArray[FinanceReportHeaderConstants.PRODUCT_ID_POSITION], equalTo(purchaseOrder.getLineItems()
                    .getLineItems().get(i).getOffering().getOfferingRequest().getOfferExternalKey()),
                assertionErrorList);

            final Float discountedAmountInResponse = purchaseOrder.getLineItems().getLineItems().get(i).getOffering()
                .getOfferingResponse().getChargeDetails().getPromotionDiscount();
            final Float discountedAmountInReport =
                Float.parseFloat(dataArray[FinanceReportHeaderConstants.UNIT_DISCOUNT_POSITION]);

            AssertCollector.assertThat("Incorrect Promotion Discount Amount", discountedAmountInReport.toString(),
                equalTo(discountedAmountInResponse.toString()), assertionErrorList);

            if (purchaseOrder.getLineItems().getLineItems().get(i).getPromotionReferences() != null) {
                AssertCollector.assertThat("Incorrect Promotion Name :",
                    dataArray[FinanceReportHeaderConstants.PROMOTION_NAME],
                    equalTo(bundledPromoForSubscriptionPlanOffers.getData().getName()), assertionErrorList);
            }

            final Float amountCharged = purchaseOrder.getLineItems().getLineItems().get(i).getOffering()
                .getOfferingResponse().getChargeDetails().getAmountCharged();
            final Float totalPrice = Float.parseFloat(dataArray[FinanceReportHeaderConstants.TOTAL_PRICE]);
            AssertCollector.assertThat("Incorrect Amount Charged :", totalPrice.toString(),
                equalTo(amountCharged.toString()), assertionErrorList);

            AssertCollector.assertThat("Incorrect List Price :", dataArray[FinanceReportHeaderConstants.LIST_PRICE],
                equalTo(purchaseOrder.getLineItems().getLineItems().get(i).getOffering().getOfferingResponse()
                    .getChargeDetails().getUnitPrice()),
                assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * This test case validates the error message displayed if we dont provide any parameters
     */
    @Test
    public void testErrorDisplayedWithoutAnyParameters() {
        financeReportPage.navigateToSearchPage();
        financeReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Incorrect error message found", financeReportPage.getErrorText(),
            equalTo("Must select at least one date range."), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /*
     * The test case validates the error message displayed if we leave one last modified date field parameter empty
     */
    @Test
    public void testErrorDisplayedForOneLastModifiedDateParameter() {
        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Incorrect error message found", financeReportPage.getH3ErrorMessage(),
            equalTo("End Date cannot be blank."), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * The test case tests the error message displayed when the start date is later than the end date
     */
    @Test
    public void testErrorDisplayedForStartDateLaterThanEndDate() {
        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expEndDateTime.split(" ")[0];
        final String expEndDate = expStartDateTime.split(" ")[0];
        final String expStartTime = expEndDateTime.split(" ")[1];
        final String expEndTime = expStartDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Incorrect error message found", financeReportPage.getH3ErrorMessage(),
            equalTo("End Date must be same as or older than the Start Date"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /*
     * The test case validates the error message displayed when the last modified start date and end date are out of
     * range
     */
    @Test
    public void testErrorDisplayedForLastModifiedDatesOutOfRange() {
        final List<String> entityDateList =
            DateTimeUtils.getStartDateAndEndDateGreaterRanges(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = expStartDateTime.split(" ")[0];
        final String expEndDate = expEndDateTime.split(" ")[0];
        final String expStartTime = expStartDateTime.split(" ")[1];
        final String expEndTime = expEndDateTime.split(" ")[1];

        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Incorrect error message found", financeReportPage.getH3ErrorMessage(),
            equalTo(PelicanErrorConstants.INVALID_LAST_MODIFIED_DATE_RANGE.split("\\. ")[1]
                + getEnvironmentVariables().getFinanceReportDateRange() + " " + PelicanConstants.DAYS_LOWER_CASE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Subscription Renewal Success for Multiple line item with promotion and without promotion for Bic and Meta
     * Subscription.
     *
     */
    @Test
    public void testFinanceReportSuccessForMultipleSubscriptionRenewalWithAndWithoutPromotion() {
        final LineItem.PromotionReferences promotionReferencesForPO = new LineItem.PromotionReferences();
        final LineItem.PromotionReference promotionReference = new LineItem.PromotionReference();
        promotionReference.setId(activeNonStoreWideDiscountAmountPromo.getData().getId());
        promotionReferencesForPO.setPromotionReference(promotionReference);

        final int quantityOfPurchaseOrderWithPromotion = 2;
        // Create a purchase order with multi billing cycle promotion and get
        // subscription id
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrderWithPromos(
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionPlanOffer, quantityOfPurchaseOrderWithPromotion)),
            false, Payment.PaymentType.CREDIT_CARD,
            new HashMap<>(ImmutableMap.of(priceIdForBicSubscriptionPlanOffer, promotionReferencesForPO)), buyerUser);

        String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        final String bicSubscriptionId = resource.purchaseOrder().getById(purchaseOrderId).getLineItems().getLineItems()
            .get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Create a purchase order and get subscription id
        purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(
            new HashMap<>(ImmutableMap.of(priceIdForMetaSubscriptionPlanOffer, 10)), false, buyerUser);

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        final String metaSubscriptionId = resource.purchaseOrder().getById(purchaseOrder.getId()).getLineItems()
            .getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // Prepare request for Subscription Renewal Request, with subscription
        // id, currency id/currency name
        final HashMap<String, List<String>> subscriptionMap = new HashMap<>();
        subscriptionMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(),
            new ArrayList<>(ImmutableList.of(bicSubscriptionId, metaSubscriptionId)));
        subscriptionMap.put(LineItemParams.CURRENCY_ID.getValue(), new ArrayList<>(
            ImmutableList.of(String.valueOf(Currency.USD.getCode()), String.valueOf(Currency.USD.getCode()))));
        subscriptionMap.put(LineItemParams.PROMOTION_REFERENCE.getValue(),
            new ArrayList<>(Arrays.asList(activeNonStoreWideDiscountAmountPromo.getData().getId(), null)));

        // Submit Subscription renewal Purchase Order
        purchaseOrder = (PurchaseOrder) purchaseOrderUtils.submitSubscriptionRenewalPurchaseOrder(subscriptionMap,
            Payment.PaymentType.CREDIT_CARD, null, true, buyerUser);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrder.getId());

        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        purchaseOrderId = purchaseOrder.getId();
        LOGGER.info("Purchase Order ID :" + purchaseOrderId);

        // Get start date and end date filter for finance report
        final List<String> entityDateList = DateTimeUtils.getStartDateAndEndDate(PelicanConstants.DB_DATE_FORMAT);

        final String expStartDateTime = entityDateList.get(0);
        final String expEndDateTime = entityDateList.get(1);

        // Get only the date part from the Date Time
        final String expStartDate = (DateTimeUtils.getDateAndTimePartFromDateTime(expStartDateTime))[0];
        final String expEndDate = (DateTimeUtils.getDateAndTimePartFromDateTime(expEndDateTime))[0];
        final String expStartTime = (DateTimeUtils.getDateAndTimePartFromDateTime(expStartDateTime))[1];
        final String expEndTime = (DateTimeUtils.getDateAndTimePartFromDateTime(expEndDateTime))[1];

        // Generate Finance Report
        financeReportPage.setLastModifiedStartDate(expStartDate);
        financeReportPage.setLastModifiedEndDate(expEndDate);
        financeReportPage.setLastModifiedStartTime(expStartTime);
        financeReportPage.setLastModifiedEndTime(expEndTime);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        financeReportPage.submit(TimeConstants.ONE_SEC);

        // Validate header
        final String actualAdminHeader = financeReportPage.getReportHeader();
        final String[] actualHeaderArray = actualAdminHeader.split(",");
        final String actualHeader = actualHeaderArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION];
        AssertCollector.assertThat("Incorrect header in the finance report", actualHeader,
            equalTo(FinanceReportHeaderConstants.LAST_MODIFIED_DATE), assertionErrorList);

        final List<String> reportData = financeReportPage.getReportData();
        final List<Integer> poRecordsList = returnFinanceReportRecordIndex(reportData, purchaseOrderId);
        for (int i = 0; i < poRecordsList.size(); i++) {
            final String requiredData = reportData.get(poRecordsList.get(i));
            final String[] dataArray = requiredData.split(",");

            final LineItem lineItem = purchaseOrder.getLineItems().getLineItems().get(i);
            final SubscriptionRenewal subscriptionRenewal = lineItem.getSubscriptionRenewal();

            AssertCollector.assertThat("Incorrect purchase order id", dataArray[0], equalTo(purchaseOrder.getId()),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Subscription Id",
                dataArray[FinanceReportHeaderConstants.SUBSCRIPTION_ID],
                equalTo(subscriptionRenewal.getSubscriptionRenewalResponse().getSubscriptionId()), assertionErrorList);

            final Float discountedAmountInResponse =
                subscriptionRenewal.getSubscriptionRenewalResponse().getChargeDetails().getPromotionDiscount();
            final BigDecimal discountedAmountInReport =
                new BigDecimal(Float.parseFloat(dataArray[FinanceReportHeaderConstants.TOTAL_DISCOUNT_POSITION]))
                    .setScale(1, BigDecimal.ROUND_HALF_UP);

            AssertCollector.assertThat("Incorrect Promotion Discount Amount", discountedAmountInReport.toString(),
                equalTo(discountedAmountInResponse.toString()), assertionErrorList);

            if (lineItem.getPromotionReferences() != null) {
                AssertCollector.assertThat("Incorrect Promotion Name :",
                    dataArray[FinanceReportHeaderConstants.PROMOTION_NAME],
                    equalTo(activeNonStoreWideDiscountAmountPromo.getData().getName()), assertionErrorList);
            }

            final Float amountCharged =
                subscriptionRenewal.getSubscriptionRenewalResponse().getChargeDetails().getAmountCharged();
            final Float totalPrice = Float.parseFloat(dataArray[FinanceReportHeaderConstants.TOTAL_PRICE]);
            AssertCollector.assertThat("Incorrect Amount Charged :", totalPrice.toString(),
                equalTo(amountCharged.toString()), assertionErrorList);

            AssertCollector.assertThat("Incorrect List Price :", dataArray[FinanceReportHeaderConstants.LIST_PRICE],
                equalTo(subscriptionRenewal.getSubscriptionRenewalResponse().getChargeDetails().getUnitPrice()),
                assertionErrorList);

            final List<Map<String, String>> queryResultList = DbUtils.selectQuery(
                String.format(SELECT_SUBSCRIPTION_PERIOD_QUERY,
                    subscriptionRenewal.getSubscriptionRenewalResponse().getSubscriptionId(), purchaseOrderId),
                getEnvironmentVariables());

            String subscriptionStartDate = queryResultList.get(0).get("SUBSCRIPTION_PERIOD_START_DATE").split(" ")[0];

            subscriptionStartDate = DateTimeUtils.changeDateFormat(subscriptionStartDate,
                PelicanConstants.DATE_TIME_FORMAT, PelicanConstants.DATE_FORMAT_WITH_SLASH);

            AssertCollector.assertThat("Incorrect Subscription Start Date :",
                dataArray[FinanceReportHeaderConstants.SUBSCRIPTION_START_DATE].split(" ")[0],
                equalTo(subscriptionStartDate), assertionErrorList);

            String subscriptionEndDate = queryResultList.get(0).get("SUBSCRIPTION_PERIOD_END_DATE").split(" ")[0];

            subscriptionEndDate = DateTimeUtils.changeDateFormat(subscriptionEndDate, PelicanConstants.DATE_TIME_FORMAT,
                PelicanConstants.DATE_FORMAT_WITH_SLASH);

            AssertCollector.assertThat("Incorrect Subscription End Date :",
                dataArray[FinanceReportHeaderConstants.SUBSCRIPTION_END_DATE].split(" ")[0],
                equalTo(subscriptionEndDate), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    private String getExpectedHeader() {
        return getEnvironmentVariables().getFinanceReportHeaders();
    }

    /**
     * This is a method which will return the finance report record index for our required purchase order
     *
     * @return List of index from the list - List<Integer>
     */
    private List<Integer> returnFinanceReportRecordIndex(final List<String> reportData, final String purchaseOrderId) {

        final List<Integer> poIndexList = new ArrayList<>();

        for (int i = 0; i < reportData.size(); i++) {

            final String recordData = reportData.get(i).replace("[", "").replace("]", "");
            final String recordDataArray[] = recordData.split(",");

            if (purchaseOrderId.equalsIgnoreCase(recordDataArray[0])) {
                poIndexList.add(i);
            }
        }
        return poIndexList;
    }
}
