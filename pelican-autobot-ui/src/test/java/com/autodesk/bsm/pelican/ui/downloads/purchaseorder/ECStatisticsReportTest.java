package com.autodesk.bsm.pelican.ui.downloads.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Transaction.TransactionType;
import com.autodesk.bsm.pelican.constants.ECStatisticsReportHeaderConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.ECStatisticsReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * This is a test class which will test the EC Statistics Report under the reports tab in the admin tool
 *
 * @author vineel
 */
public class ECStatisticsReportTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private String priceIdForBic;
    private String priceIdForMeta;
    private static ECStatisticsReportPage ecStatisticsReportPage;
    private static GenericGrid reportGrid;
    private static final String orderStartDate =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
    private static final String orderEndDate = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);
    private static String storeName;
    private static final String ACTUAL_FILE_NAME = "ExportControlStatistics.xlsx";
    private static int quantity;
    private PurchaseOrder purchaseOrder;
    private String purchaseOrderId;
    private static final String NEW_ACQUISITION = "NEW_ACQUISITION";
    private static final String AUTO_RENEWAL = "AUTO_RENEWAL";
    private static final String USER_EXTERNAL_KEY = "Automation_test_ECStatisticsReportTest";
    private BuyerUser buyerUser;
    private static final Logger LOGGER = LoggerFactory.getLogger(ECStatisticsReportTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        // Set up the environment and test data.
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings metaOfferings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        priceIdForBic = bicOfferings.getIncluded().getPrices().get(0).getId();
        priceIdForMeta = metaOfferings.getIncluded().getPrices().get(0).getId();

        storeName = getStoreUs().getName();

        final UserUtils userUtils = new UserUtils();
        buyerUser = userUtils.createBuyerUser(getEnvironmentVariables(), USER_EXTERNAL_KEY,
            getEnvironmentVariables().getAppFamily());

        // Get the Required Page class Objects
        ecStatisticsReportPage = adminToolPage.getPage(ECStatisticsReportPage.class);
        reportGrid = adminToolPage.getPage(GenericGrid.class);
        quantity = 2;
    }

    /**
     * Verify all headers of columns in Report
     *
     * @result report should show 13 columns and name of the headers of each column should be Order ID, User, Order
     *         Total, Order Type, Currency, Store, Initial EC Status, Order State, Order Created Date, Order Fulfilled
     *         Date, Order Charged Date, Order Declined Date and Order Refunded Date
     */
    @Test
    public void testECStatisticsReportHeaders() {

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Total number of columns are not correct", reportGrid.getColumnHeaders().size(),
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", reportGrid.getColumnHeaders().get(0),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", reportGrid.getColumnHeaders().get(1),
            equalTo(ECStatisticsReportHeaderConstants.USER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", reportGrid.getColumnHeaders().get(2),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_TOTAL), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", reportGrid.getColumnHeaders().get(3),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", reportGrid.getColumnHeaders().get(4),
            equalTo(ECStatisticsReportHeaderConstants.CURRENCY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", reportGrid.getColumnHeaders().get(5),
            equalTo(ECStatisticsReportHeaderConstants.STORE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", reportGrid.getColumnHeaders().get(6),
            equalTo(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", reportGrid.getColumnHeaders().get(7),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_STATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", reportGrid.getColumnHeaders().get(8),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 10", reportGrid.getColumnHeaders().get(9),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_FULFILLED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 11", reportGrid.getColumnHeaders().get(10),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_CHARGED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 12", reportGrid.getColumnHeaders().get(11),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_DECLINED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 13", reportGrid.getColumnHeaders().get(12),
            equalTo(ECStatisticsReportHeaderConstants.ORDER_REFUND_DATE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all headers of columns in download Report
     *
     * @result report should show 13 columns and name of the headers of each column should be Order ID, User, Order
     *         Total, Order Type, Currency, Store, Initial EC Status, Order State, Order Created Date, Order Fulfilled
     *         Date, Order Charged Date, Order Declined Date and Order Refunded Date
     */
    @Test
    public void testECStatisticsReportHeaderInDownloadFile() throws IOException {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1],
            equalTo(ECStatisticsReportHeaderConstants.USER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_TOTAL), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", fileData[0][4],
            equalTo(ECStatisticsReportHeaderConstants.CURRENCY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", fileData[0][5],
            equalTo(ECStatisticsReportHeaderConstants.STORE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", fileData[0][6],
            equalTo(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", fileData[0][7],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_STATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", fileData[0][8],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 10", fileData[0][9],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_FULFILLED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 11", fileData[0][10],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_CHARGED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 12", fileData[0][11],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_DECLINED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 13", fileData[0][12],
            equalTo(ECStatisticsReportHeaderConstants.ORDER_REFUND_DATE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the purchase order in fulfilled state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithFulfilledDateInViewReport() {

        buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);// api

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderCommand.PENDING.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE).get(0)),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order fulfilled date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_FULFILLED_DATE).get(0)),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report downloaded to excel with the purchase order in
     * fulfilled state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithFulfilledDateInDownloadReport() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);// api

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderCommand.PENDING.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order fulfilled date", parseDateUntilMinute(fileData[1][9]),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the purchase order in declined state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithDeclinedDateInViewReport() {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);// api

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order declined date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_DECLINED_DATE).get(0)),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report when downloaded to excel with the purchase order
     * in declined state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithDeclinedDateInDownloadReport() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);// api

        // if API response is HTTP Error, assert or else do the validation
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.DECLINED.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order declined date", parseDateUntilMinute(fileData[1][11]),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the purchase order in charged state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithChargedDateInViewReport() {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE).get(0)),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order charged date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CHARGED_DATE).get(0)),
            equalTo(parseDateUntilMinute(getPurchaseOrderChargeDateFromTransactionDetail(purchaseOrder))),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report when downloaded to excel with the purchase order
     * in charged state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithChargedDateInDownloadReport() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.CHARGED.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order charged date", parseDateUntilMinute(fileData[1][10]),
            equalTo(parseDateUntilMinute(getPurchaseOrderChargeDateFromTransactionDetail(purchaseOrder))),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the purchase order in refunded state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithRefundedDateInViewReport() {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE).get(0)),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order charged date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CHARGED_DATE).get(0)),
            equalTo(parseDateUntilMinute(getPurchaseOrderChargeDateFromTransactionDetail(purchaseOrder))),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order refunded date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_REFUND_DATE).get(0)),
            equalTo(parseDateUntilMinute(Iterables.getLast(purchaseOrder.getTransactions().getTransactions())
                .getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report when downloaded to excel with the purchase order
     * in refunded state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithRefundedDateInDownloadReport() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.REFUNDED.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order charged date", parseDateUntilMinute(fileData[1][10]),
            equalTo(parseDateUntilMinute(getPurchaseOrderChargeDateFromTransactionDetail(purchaseOrder))),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order refunded date", parseDateUntilMinute(fileData[1][12]),
            equalTo(parseDateUntilMinute(Iterables.getLast(purchaseOrder.getTransactions().getTransactions())
                .getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the purchase order in charged back state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithChargedBackDateInViewReport() {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.CHARGED_BACK.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order charged date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CHARGED_DATE).get(0)),
            equalTo(parseDateUntilMinute(getPurchaseOrderChargeDateFromTransactionDetail(purchaseOrder))),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report when downloaded to excel with the purchase order
     * in charged back state
     */
    @Test
    public void testECStatisticsReportSubmitPoWithChargedBackDateInDownloadReport() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(purchaseOrderId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(NEW_ACQUISITION),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.CHARGED_BACK.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order charged date", parseDateUntilMinute(fileData[1][10]),
            equalTo(parseDateUntilMinute(getPurchaseOrderChargeDateFromTransactionDetail(purchaseOrder))),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the renewal purchase order in fulfilled state
     */
    @Test
    public void testECStatisticsReportRenewalPoWithFulfilledDateInViewReport() {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // Get Subscription Id
        final String subscriptionIdForMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        final PurchaseOrder renewalOrderForMetaPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForMetaPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        final String renewalPoId = renewalOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, renewalPoId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPoId);
        purchaseOrder = resource.purchaseOrder().getById(renewalPoId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(renewalPoId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", reportGrid.getColumnValues("Order Type").get(0),
            equalTo(AUTO_RENEWAL), assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.CHARGED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE).get(0)),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order fulfilled date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_FULFILLED_DATE).get(0)),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report downloaded to excel with the renewal purchase
     * order in fulfilled state
     */
    @Test
    public void testECStatisticsReportRenewalPoWithFulfilledDateInDownloadReport() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // Get Subscription Id
        final String subscriptionIdForMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        final PurchaseOrder renewalOrderForMetaPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForMetaPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        final String renewalPoId = renewalOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, renewalPoId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPoId);
        purchaseOrder = resource.purchaseOrder().getById(renewalPoId);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(renewalPoId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(AUTO_RENEWAL), assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.CHARGED.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order fulfilled date", parseDateUntilMinute(fileData[1][9]),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the renewal purchase order in declined state
     */
    @Test
    public void testECStatisticsReportRenewalPoWithDeclinedDateInView() {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // Get subscription id
        final String subscriptionIdForMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        final PurchaseOrder renewalOrderForMetaPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForMetaPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        final String renewalPoId = renewalOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPoId);
        purchaseOrder = resource.purchaseOrder().getById(renewalPoId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(renewalPoId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(AUTO_RENEWAL),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE).get(0)),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order declined date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_DECLINED_DATE).get(0)),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report downloaded to excel with the renewal purchase
     * order in declined state
     */
    @Test
    public void testECStatisticsReportRenewalPoWithDeclinedDateInDownload() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // Get subscription id
        final String subscriptionIdForMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        final PurchaseOrder renewalOrderForMetaPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForMetaPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        final String renewalPoId = renewalOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPoId);
        purchaseOrder = resource.purchaseOrder().getById(renewalPoId);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(renewalPoId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(AUTO_RENEWAL), assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.DECLINED.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order declined date", parseDateUntilMinute(fileData[1][11]),
            equalTo(parseDateUntilMinute(
                purchaseOrder.getTransactions().getTransactions().get(1).getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report with the renewal purchase order in refunded state
     */
    @Test
    public void testECStatisticsReportRenewalPoWithRefundedDateInView() {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        // Get subscription id
        final String subscriptionIdForMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        final PurchaseOrder renewalOrderForMetaPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForMetaPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        final String renewalPoId = renewalOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, renewalPoId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPoId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, renewalPoId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        purchaseOrder = resource.purchaseOrder().getById(renewalPoId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        AssertCollector.assertThat("Incorrect purchase order id",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_ID).get(0), equalTo(renewalPoId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.USER).get(0),
            equalTo(buyerUser.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order type",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_TYPE).get(0), equalTo(AUTO_RENEWAL),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE).get(0), equalTo(storeName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.INITIAL_EC_STATUS).get(0),
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_STATE).get(0),
            equalTo(OrderState.REFUNDED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE).get(0)),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order refunded date",
            parseDateUntilMinute(
                reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_REFUND_DATE).get(0)),
            equalTo(parseDateUntilMinute(Iterables.getLast(purchaseOrder.getTransactions().getTransactions())
                .getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the EC Statistics Report downloaded to excel with the renewal purchase
     * order in refunded state
     */
    @Test
    public void testECStatisticsReportRenewalPoWithRefundedDateInDownload() throws IOException {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.PAYPAL, priceIdForMeta, buyerUser, quantity).getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        final String subscriptionIdForMetaPaypal =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        buyerUser.setInitialExportControlStatus(ECStatus.REVIEW.getName());
        final PurchaseOrder renewalOrderForMetaPaypal =
            (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                subscriptionIdForMetaPaypal, buyerUser, OrderCommand.AUTHORIZE, true);
        final String renewalPoId = renewalOrderForMetaPaypal.getId();
        purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, renewalPoId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, renewalPoId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, renewalPoId);
        purchaseOrder = resource.purchaseOrder().getById(renewalPoId);

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.DOWNLOAD);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(ECStatisticsReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect purchase order id", fileData[1][0], equalTo(renewalPoId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect buyer user", fileData[1][1], equalTo(buyerUser.getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order type", fileData[1][3], equalTo(AUTO_RENEWAL), assertionErrorList);
        AssertCollector.assertThat("Incorrect store", fileData[1][5], equalTo(storeName), assertionErrorList);
        AssertCollector.assertThat("Incorrect initial ec status", fileData[1][6],
            equalTo(purchaseOrder.getBuyerUser().getInitialExportControlStatus()), assertionErrorList);
        AssertCollector.assertThat("Incorrect order state", fileData[1][7], equalTo(OrderState.REFUNDED.toString()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect order created date", parseDateUntilMinute(fileData[1][8]),
            equalTo(parseDateUntilMinute(purchaseOrder.getCreationTime())), assertionErrorList);
        AssertCollector.assertThat("Incorrect order refunded date", parseDateUntilMinute(fileData[1][12]),
            equalTo(parseDateUntilMinute(Iterables.getLast(purchaseOrder.getTransactions().getTransactions())
                .getGatewayResponse().getTxnDate())),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test whether all the records in the report have the start date and the end date
     * in the requested date range and same store as selected
     */
    @Test
    public void testECStatisticsReportOrderStartDateAndEndDateInView() {

        buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
        purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, quantity).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

        ecStatisticsReportPage.viewOrDownloadReport(storeName, orderStartDate, orderEndDate, PelicanConstants.VIEW);
        final String purchaseOrderStartDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String purchaseOrderEndDate = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        AssertCollector.assertThat("Incorrect order created start date",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE, 1),
            everyItem(greaterThanOrEqualTo(purchaseOrderStartDate)), assertionErrorList);
        AssertCollector.assertThat("Incorrect order end date",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.ORDER_CREATED_DATE, 1),
            everyItem(lessThanOrEqualTo(purchaseOrderEndDate)), assertionErrorList);
        AssertCollector.assertThat("Incorrect store",
            reportGrid.getColumnValues(ECStatisticsReportHeaderConstants.STORE), everyItem(equalTo(storeName)),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to parse DateTime string until minutes.
     *
     * @param date
     * @return String
     */
    private String parseDateUntilMinute(final String date) {
        LOGGER.info("Date: " + date);
        if (date != null && !date.equals("")) {
            return date.substring(0, 16);
        }
        return date;
    }

    /**
     * Method to return purchase order charge transaction date.
     *
     * @param purchaseOrder
     * @return String
     */
    private String getPurchaseOrderChargeDateFromTransactionDetail(final PurchaseOrder purchaseOrder) {
        final String chargeDate = null;
        // Looping through from 2 since first 2 transactions are auth and pending.
        for (int i = 2; i < purchaseOrder.getTransactions().getTransactions().size(); i++) {
            if ((purchaseOrder.getTransactions().getTransactions().get(i).getTransactionType())
                .equals(TransactionType.SALE)) {
                return purchaseOrder.getTransactions().getTransactions().get(i).getGatewayResponse().getTxnDate();
            }
        }
        return chargeDate;
    }

}
