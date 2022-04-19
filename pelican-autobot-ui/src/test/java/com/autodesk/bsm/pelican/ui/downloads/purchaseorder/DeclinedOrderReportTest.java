package com.autodesk.bsm.pelican.ui.downloads.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThan;
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
import com.autodesk.bsm.pelican.constants.DeclinedOrderReportHeaderConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.DeclineReason;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.DeclinedOrderReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * This class tests Declined Order Report Under Order reports in Admin Tool which includes: declined Orders, submitted
 * orders, charged back orders and pending orders
 *
 * @author Muhammad Azeem
 */
public class DeclinedOrderReportTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private DeclinedOrderReportPage declinedOrderReportPage;
    private static final String DATE_TODAY = DateTimeUtils.getCurrentDate();
    private static final String getDateBeforeCurrentDate = DateTimeUtils.getNowMinusDays(15);
    private static final String ORDER_ID = "Order ID";
    private static final String ORDER_STATE = "Order State";
    private static final String ORDER_CREATED_DATE = "Order Created Date";
    private static final String ORDER_LAST_MODIFIED_DATE = "Order LastModified Date";
    private static final String PAYMENT_PROCESSOR = "Payment Processor";
    private static final String ERROR_CODE_DOWNLOAD = "Error Code";
    private static final String ERROR_CODE_VIEW = "ErrorCode";
    private static final String REASON = "Reason";
    private static final String DECLINED_REASON = "Declined Reason";
    private static final String CUSTOMER_EMAIL = "Customer Email";
    private static final String Declined_Reason = "Declined Reason";
    private GenericGrid genericGrid;
    private static final int TOTAL_DECLINE_REASONS = 4;
    private static final String FILE_NAME = "DeclinedOrdersReport";
    private static final String ACTUAL_FILE_NAME = FILE_NAME + ".xlsx";
    private static String fileName;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeclinedOrderReportTest.class.getSimpleName());

    /**
     * Data setup - open a admin tool page and login into it
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        declinedOrderReportPage = adminToolPage.getPage(DeclinedOrderReportPage.class);
        genericGrid = adminToolPage.getPage(GenericGrid.class);
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        final Offerings bicCommercialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String bicCommercialPriceId = bicCommercialOffering.getIncluded().getPrices().get(0).getId();

        final UserUtils userUtils = new UserUtils();
        final BuyerUser buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());

        // create decline orders with all types of declines for meta and bic subscriptions
        DeclineReason declineReason = null;
        for (int i = 0; i < TOTAL_DECLINE_REASONS; i++) {
            if (i == 0) {
                declineReason = DeclineReason.PAYMENT_PROCESSOR_DECLINED;
            }
            if (i == 1) {
                declineReason = DeclineReason.OTHER_REASON;
            }
            if (i == 2) {
                declineReason = DeclineReason.EXPORT_CONTROL_BLOCKED;
            }
            if (i == 3) {
                declineReason = DeclineReason.EXPORT_CONTROL_UNRESOLVED;
            }
            final PurchaseOrder purchaseOrderCreatedForBic = purchaseOrderUtils
                .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicCommercialPriceId, buyerUser, 1);
            // process purchase order with pending and charge commands
            final PurchaseOrder purchaseOrderProcessForBic =
                purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderCreatedForBic.getId());
            purchaseOrderUtils.processPurchaseOrderWithDeclineReason(OrderCommand.DECLINE,
                purchaseOrderProcessForBic.getId(), declineReason);
            LOGGER.info("Purchase Order " + purchaseOrderProcessForBic.getId() + " is submitted with "
                + "Decline Reason: " + declineReason);

            final PurchaseOrder purchaseOrderCreatedForMeta = purchaseOrderUtils
                .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, bicCommercialPriceId, buyerUser, 1);
            // process purchase order with pending and charge commands
            final PurchaseOrder purchaseOrderProcessForMeta =
                purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderCreatedForMeta.getId());
            purchaseOrderUtils.processPurchaseOrderWithDeclineReason(OrderCommand.DECLINE,
                purchaseOrderProcessForMeta.getId(), declineReason);
            fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
            LOGGER.info("Name of Downloaded file is: " + fileName);
        }
    }

    /**
     * Verify the Errors should be generated without selecting any filter
     *
     * @result Error Message should be returned
     */
    @Test
    public void testDeclinedOrderReportWithoutFilters() {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.declinedOrdersUncheck();
        declinedOrderReportPage.clickGenerateReportButton();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        AssertCollector.assertThat("Admin Tool: Main Error is not Generated",
            declinedOrderReportPage.getH3ErrorMessage(), equalTo("Please correct the error listed below:"),
            assertionErrorList);
        AssertCollector.assertThat("Option Error is not Generated for Filters",
            declinedOrderReportPage.getCheckBoxErrorMessages(), equalTo("Must select at least one option"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report of declined orders can be viewed after checking declined order checkbox and selected date range.
     *
     * @result view report of declined orders with a status declined in a selected date range
     */
    @Test
    public void testDeclinedOrderReportWithDeclinedOrderFilterForView() {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.setOrderAfterDate(getDateBeforeCurrentDate);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        declinedOrderReportPage.setOrderBeforeDate(DATE_TODAY);
        declinedOrderReportPage.declinedOrdersCheck();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        declinedOrderReportPage.clickGenerateReportButton();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final GenericGrid declinedOrderReportGrid = adminToolPage.getPage(GenericGrid.class);

        if (declinedOrderReportGrid.getTotalItems() > 0) {
            AssertCollector.assertThat("Declined Order Report is not Generated Correctly ",
                declinedOrderReportGrid.getColumnValues("Order State"),
                everyItem(equalTo(OrderState.DECLINED.toString())), assertionErrorList);

            final List<String> actualRestOrderDates = declinedOrderReportGrid.getColumnValues("Order Created Date");
            for (final String date : actualRestOrderDates) {
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(date, PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    greaterThanOrEqualTo(DateTimeUtils.convertStringToDate(getDateBeforeCurrentDate,
                        PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                    assertionErrorList);
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(date, PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    lessThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(DATE_TODAY, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                    assertionErrorList);
            }
        } else {
            AssertCollector.assertThat("AdminTool: Neither 'None found' Message nor records are found",
                declinedOrderReportPage.getNotFound(), equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report of declined orders can be Downloaded after checking declined order checkbox and selected date
     * range.
     *
     * @result downloaded report of declined orders with a status declined in a selected date range
     */
    @Test
    public void testDeclinedOrderReportWithDeclinedOrderFilterForDownload() throws IOException {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.setOrderAfterDate(getDateBeforeCurrentDate);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        declinedOrderReportPage.setOrderBeforeDate(DATE_TODAY);
        declinedOrderReportPage.declinedOrdersCheck();
        declinedOrderReportPage.selectAction("Download");
        cleanDeclinedOrderReportFile();
        declinedOrderReportPage.submit(0);

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        LOGGER.info("Total number of records in file is: " + fileData.length);

        int i;
        if (fileData.length > 0) {
            for (i = 1; i < fileData.length; i++) {
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(fileData[i][2], PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    greaterThanOrEqualTo(DateTimeUtils.convertStringToDate(getDateBeforeCurrentDate,
                        PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                    assertionErrorList);
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(fileData[i][2], PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    lessThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(DATE_TODAY, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                    assertionErrorList);
            }
        } else {
            LOGGER.info("No Records found in Downloaded Report");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report of submitted orders can be viewed after checking submitted order checkbox
     *
     * @result report of submitted order with status of submitted
     */
    @Test
    public void testDeclinedOrderReportWithSubmittedOrderFilterForView() {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.declinedOrdersUncheck();
        declinedOrderReportPage.submittedOrdersCheck();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        declinedOrderReportPage.clickGenerateReportButton();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final GenericGrid declinedOrderReportGrid = adminToolPage.getPage(GenericGrid.class);

        if (declinedOrderReportGrid.getTotalItems() > 0) {
            AssertCollector.assertThat("AdminTool: Found Order Report other than <b>Submitted Status</b>",
                declinedOrderReportGrid.getColumnValues("Order State"), everyItem(equalTo("SUBMITTED")),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("AdminTool: Neither 'None found' Message nor records are found",
                declinedOrderReportPage.getNotFound(), equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report of submitted orders can be downloaded after checking submitted order checkbox
     *
     * @result downloaded report of submitted order with status of submitted
     */
    @Test
    public void testDeclinedOrderReportWithSubmittedOrderFilterForDownload() throws IOException {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.declinedOrdersUncheck();
        declinedOrderReportPage.submittedOrdersCheck();
        declinedOrderReportPage.selectAction("Download");
        cleanDeclinedOrderReportFile();
        declinedOrderReportPage.submit(0);

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        LOGGER.info("Total number of records in file is: " + fileData.length);
        int i;
        if (fileData.length > 0) {
            for (i = 1; i < fileData.length; i++) {
                AssertCollector.assertThat("Download: Found Order Report other than <b>Submitted Status</b>",
                    fileData[i][1], equalTo("SUBMITTED"), assertionErrorList);
            }
        } else {
            LOGGER.info("No Records found in Downloaded Report");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report of pending orders can be viewed after checking pending order checkbox
     *
     * @result report of pending order with status of pending
     */
    @Test
    public void testDeclinedOrderReportWithPendingOrderFilterForView() {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.declinedOrdersUncheck();
        declinedOrderReportPage.pendingStatusOrdersCheck();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        declinedOrderReportPage.clickGenerateReportButton();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final GenericGrid declinedOrderReportGrid = adminToolPage.getPage(GenericGrid.class);

        final String dateTimeBeforeSixHours =
            DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 360);
        LOGGER.info("Date time before six hours in UTC" + dateTimeBeforeSixHours);

        if (declinedOrderReportGrid.getTotalItems() > 0) {
            AssertCollector.assertThat("AdminTool: Found Order Report other than <b>Pending Status</b>",
                declinedOrderReportGrid.getColumnValues("Order State"),
                everyItem(equalTo(OrderState.PENDING.toString())), assertionErrorList);

            final List<String> actualRestOrderDates = declinedOrderReportGrid.getColumnValues("Order Created Date");
            for (final String date : actualRestOrderDates) {
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(date, PelicanConstants.DB_DATE_FORMAT),
                    lessThan(
                        DateTimeUtils.convertStringToDate(dateTimeBeforeSixHours, PelicanConstants.DB_DATE_FORMAT)),
                    assertionErrorList);
            }
        } else {
            AssertCollector.assertThat("AdminTool: Found Order Report other than <b>Pending Status</b>",
                declinedOrderReportPage.getNotFound(), equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report of pending orders can be downloaded after checking pending order checkbox
     *
     * @result downloaded report of pending order with status of pending
     */
    @Test
    public void testDeclinedOrderReportWithPendingOrderFilterForDownload() throws IOException {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.declinedOrdersUncheck();
        declinedOrderReportPage.pendingStatusOrdersCheck();
        declinedOrderReportPage.selectAction("Download");
        cleanDeclinedOrderReportFile();
        declinedOrderReportPage.submit(0);

        final String dateTimeBeforeSixHours =
            DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 360);
        LOGGER.info("Date time before six hours in UTC" + dateTimeBeforeSixHours);

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        LOGGER.info("Total number of records in file is: " + fileData.length);
        int i;
        if (fileData.length > 0) {
            for (i = 1; i < fileData.length; i++) {
                AssertCollector.assertThat("Download: Found Order Report other than <b>Pending Status</b>",
                    fileData[i][1], equalTo(OrderState.PENDING.toString()), assertionErrorList);
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(fileData[i][2], PelicanConstants.DB_DATE_FORMAT),
                    lessThan(
                        DateTimeUtils.convertStringToDate(dateTimeBeforeSixHours, PelicanConstants.DB_DATE_FORMAT)),
                    assertionErrorList);
            }
        } else {
            LOGGER.info("No Records found in Downloaded Report");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all headers of columns in view Report
     *
     * @result report should show 8 columns and name of the headers of each column should be Order ID, Order State,
     *         Order Created Date, Order LastModified Date, Payment Processor, ErrorCode, Reason and Customer Email
     *         Address
     */
    @Test
    public void testDeclinedOrderReportCheckAllHeadersForView() {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.clickGenerateReportButton();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final GenericGrid declinedOrderReportGrid = adminToolPage.getPage(GenericGrid.class);

        AssertCollector.assertThat("Total number of columns are not correct",
            declinedOrderReportGrid.getColumnHeaders().size(),
            equalTo(DeclinedOrderReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1",
            declinedOrderReportGrid.getColumnHeaders().get(DeclinedOrderReportHeaderConstants.ORDER_ID_POSITION),
            equalTo(ORDER_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2",
            declinedOrderReportGrid.getColumnHeaders().get(DeclinedOrderReportHeaderConstants.ORDER_STATE_POSITION),
            equalTo(ORDER_STATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3",
            declinedOrderReportGrid.getColumnHeaders()
                .get(DeclinedOrderReportHeaderConstants.ORDER_CREATED_DATE_POSITION),
            equalTo(ORDER_CREATED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4",
            declinedOrderReportGrid.getColumnHeaders()
                .get(DeclinedOrderReportHeaderConstants.ORDER_LAST_MODIFIED_DATE_POSITION),
            equalTo(ORDER_LAST_MODIFIED_DATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5",
            declinedOrderReportGrid.getColumnHeaders()
                .get(DeclinedOrderReportHeaderConstants.PAYMENT_PROCESSOR_POSITION),
            equalTo(PAYMENT_PROCESSOR), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6",
            declinedOrderReportGrid.getColumnHeaders().get(DeclinedOrderReportHeaderConstants.ERROR_CODE_POSITION),
            equalTo(ERROR_CODE_VIEW), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7",
            declinedOrderReportGrid.getColumnHeaders().get(DeclinedOrderReportHeaderConstants.REASON_POSITION),
            equalTo(REASON), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8",
            declinedOrderReportGrid.getColumnHeaders().get(DeclinedOrderReportHeaderConstants.DECLINED_REASON_POSITION),
            equalTo(DECLINED_REASON), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9",
            declinedOrderReportGrid.getColumnHeaders().get(DeclinedOrderReportHeaderConstants.CUSTOMER_EMAIL_POSITION),
            equalTo(CUSTOMER_EMAIL), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all headers of columns in downloaded Report
     *
     * @result report should show 8 columns and name of the headers of each column should be Order ID, Order State,
     *         Order Created Date, Order LastModified Date, Payment Processor, ErrorCode, Reason and Customer Email
     *         Address
     */
    @Test
    public void testDeclinedOrderReportCheckAllHeadersForDownload() throws IOException {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.selectAction("Download");
        cleanDeclinedOrderReportFile();
        declinedOrderReportPage.submit(0);

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(DeclinedOrderReportHeaderConstants.TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0], equalTo(ORDER_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1], equalTo(ORDER_STATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2], equalTo(ORDER_CREATED_DATE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3], equalTo(ORDER_LAST_MODIFIED_DATE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", fileData[0][4], equalTo(PAYMENT_PROCESSOR),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", fileData[0][5], equalTo(ERROR_CODE_DOWNLOAD),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", fileData[0][6], equalTo(REASON), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", fileData[0][7], equalTo(DECLINED_REASON), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", fileData[0][8], equalTo(CUSTOMER_EMAIL), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the populated data in a report.
     *
     * @result Following entities in Report and Purchase Order should be same i) OrderIds should be same in both report
     *         and get purchase order response ii) Order states should be same in both report and get purchase order
     *         response iii) Order created dates should be same in both report and get purchase order response iv) Order
     *         last modified dates should be same in both report and get purchase order response v) Payment processors
     *         should be same in both report and get purchase order response vi) Customer emails should be same in both
     *         report and get purchase order response
     */
    @Test
    public void testDeclinedOrderReportData() {
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.submittedOrdersCheck();
        declinedOrderReportPage.submit(TimeConstants.ONE_SEC);
        final GenericGrid declinedOrderReportGrid = adminToolPage.getPage(GenericGrid.class);
        final int results = declinedOrderReportGrid.getTotalItems();
        final int reportIndex = declinedOrderReportGrid.selectRowRandomlyFromFirstPage(results);
        if (results > 0) {
            final String orderIdInReport = declinedOrderReportGrid.getColumnValues(ORDER_ID).get(reportIndex);
            final String orderStateInReport = declinedOrderReportGrid.getColumnValues(ORDER_STATE).get(reportIndex);
            final String orderCreatedDateInReport =
                declinedOrderReportGrid.getColumnValues(ORDER_CREATED_DATE).get(reportIndex);
            final String orderLastModifiedDateInReport =
                declinedOrderReportGrid.getColumnValues(ORDER_LAST_MODIFIED_DATE).get(reportIndex);
            final String paymentProcessorInReport =
                declinedOrderReportGrid.getColumnValues(PAYMENT_PROCESSOR).get(reportIndex);
            final String customerEmailInReport =
                declinedOrderReportGrid.getColumnValues(CUSTOMER_EMAIL).get(reportIndex);
            final String declineReasonInReport =
                declinedOrderReportGrid.getColumnValues(Declined_Reason).get(reportIndex);

            final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(orderIdInReport);
            final String purchaseOrderId = getPurchaseOrder.getId();

            AssertCollector.assertThat("ID of order is not same as shown in Report for PO id - " + purchaseOrderId,
                purchaseOrderId, equalTo(orderIdInReport), assertionErrorList);
            AssertCollector.assertThat("Order Status is not same as shown in Report for PO id - " + purchaseOrderId,
                getPurchaseOrder.getOrderState(), equalTo(orderStateInReport), assertionErrorList);
            AssertCollector.assertThat(
                "Order created date is not same as shown in Report for PO id - " + purchaseOrderId,
                getPurchaseOrder.getCreationTime(), equalTo(orderCreatedDateInReport), assertionErrorList);
            AssertCollector.assertThat(
                "Last Modified Date is not same as shown in Report for PO id - " + purchaseOrderId,
                getPurchaseOrder.getLastModified(), equalTo(orderLastModifiedDateInReport), assertionErrorList);
            AssertCollector.assertThat("Email Address is not same as shown in Report for PO id - " + purchaseOrderId,
                getPurchaseOrder.getBuyerUser().getEmail(), equalTo(customerEmailInReport), assertionErrorList);
            AssertCollector.assertThat("Order Status is not same as shown in Report for PO id - " + purchaseOrderId,
                getPurchaseOrder.getPayment().getPaymentProcessor(), equalTo(paymentProcessorInReport),
                assertionErrorList);
            final String declineReasonFromApi = getPurchaseOrder.getDeclineReason();
            final String expectedDeclineReason = declineReasonFromApi == null ? "" : declineReasonFromApi;
            AssertCollector.assertThat("Decline Reason is not same as shown in Report for PO id - " + purchaseOrderId,
                declineReasonInReport, equalTo(expectedDeclineReason), assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        } else {
            LOGGER.info("No Results Found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the decline reason in a report with feature flag of export control as true
     */
    @Test(dataProvider = "dataForDeclineReasonForDeclinedOrderReport")
    public void testDeclinedOrderReportWithDeclineReasonAndFeatureFlagOn(final String declineReasonSelect) {
        LOGGER.info("Selected decline reason to Generate Report is: " + declineReasonSelect);
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage.selectDeclineReason(declineReasonSelect);
        AssertCollector.assertTrue(
            "DropDown of Decline Reason should be present when export control feature flag " + "is on",
            declinedOrderReportPage.isSelectDeclineReasonFilterPresent(), assertionErrorList);
        declinedOrderReportPage.submit(0);

        if (declineReasonSelect.equals("ANY (*)")) {
            AssertCollector.assertThat("Declined Reason Column values are not correct",
                genericGrid.getColumnValues(DECLINED_REASON),
                everyItem(isOneOf(DeclineReason.EXPORT_CONTROL_BLOCKED.name(), DeclineReason.OTHER_REASON.name(),
                    DeclineReason.PAYMENT_PROCESSOR_DECLINED.name(), DeclineReason.EXPORT_CONTROL_UNRESOLVED.name(),
                    "")),
                assertionErrorList);
        } else if (declineReasonSelect.equals(DeclineReason.OTHER_REASON.name())) {
            AssertCollector.assertThat("All values of Decline Reason Column are not *OTHER_REASON*",
                genericGrid.getColumnValues(DECLINED_REASON), everyItem(equalTo(DeclineReason.OTHER_REASON.name())),
                assertionErrorList);
        } else if (declineReasonSelect.equals(DeclineReason.PAYMENT_PROCESSOR_DECLINED.name())) {
            AssertCollector.assertThat("All values of Decline Reason Column are not *PAYMENT_PROCESSOR_DECLINED*",
                genericGrid.getColumnValues(DECLINED_REASON),
                everyItem(equalTo(DeclineReason.PAYMENT_PROCESSOR_DECLINED.name())), assertionErrorList);
        } else if (declineReasonSelect.equals(DeclineReason.EXPORT_CONTROL_BLOCKED.name())) {
            AssertCollector.assertThat("All values of Decline Reason Column are not *EXPORT_CONTROL_BLOCKED*",
                genericGrid.getColumnValues(DECLINED_REASON),
                everyItem(equalTo(DeclineReason.EXPORT_CONTROL_BLOCKED.name())), assertionErrorList);
        } else {
            AssertCollector.assertThat("All values of Decline Reason Column are not *EXPORT_CONTROL_UNRESOLVED",
                genericGrid.getColumnValues(DECLINED_REASON),
                everyItem(equalTo(DeclineReason.EXPORT_CONTROL_UNRESOLVED.name())), assertionErrorList);
        }
        AssertCollector.assertThat("", genericGrid.getColumnValues(ORDER_STATE),
            everyItem(equalTo(OrderState.DECLINED.toString())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the decline reason in a report with feature flag of export control as true
     */
    @Test(dataProvider = "dataForDeclineReasonForDeclinedOrderReport")
    public void testDeclinedOrderReportWithDeclineReasonInDownload(final String declineReasonSelect)
        throws IOException {
        LOGGER.info("Selected decline reason to Generate Report is: " + declineReasonSelect);
        declinedOrderReportPage.navigateToPage();
        declinedOrderReportPage
            .setOrderAfterDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        declinedOrderReportPage.setOrderBeforeDate(DATE_TODAY);
        declinedOrderReportPage.selectAction("Download");
        declinedOrderReportPage.selectDeclineReason(declineReasonSelect);
        AssertCollector.assertTrue(
            "DropDown of Decline Reason should be present when export control feature flag " + "is on",
            declinedOrderReportPage.isSelectDeclineReasonFilterPresent(), assertionErrorList);
        cleanDeclinedOrderReportFile();
        declinedOrderReportPage.submit(0);

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        LOGGER.info("Total number of records in file is: " + fileData.length);

        for (int i = 1; i < fileData.length; i++) {
            if (declineReasonSelect.equals("ANY (*)")) {
                AssertCollector.assertThat("Declined Reason Column values are not correct", fileData[i][7],
                    isOneOf(DeclineReason.EXPORT_CONTROL_BLOCKED.name(), DeclineReason.OTHER_REASON.name(),
                        DeclineReason.PAYMENT_PROCESSOR_DECLINED.name(), DeclineReason.EXPORT_CONTROL_UNRESOLVED.name(),
                        ""),
                    assertionErrorList);
            } else if (declineReasonSelect.equals(DeclineReason.OTHER_REASON.name())) {
                AssertCollector.assertThat("All values of Decline Reason Column are not *OTHER_REASON*", fileData[i][7],
                    equalTo(DeclineReason.OTHER_REASON.name()), assertionErrorList);
            } else if (declineReasonSelect.equals(DeclineReason.PAYMENT_PROCESSOR_DECLINED.name())) {
                AssertCollector.assertThat("All values of Decline Reason Column are not *PAYMENT_PROCESSOR_DECLINED*",
                    fileData[i][7], equalTo(DeclineReason.PAYMENT_PROCESSOR_DECLINED.name()), assertionErrorList);
            } else if (declineReasonSelect.equals(DeclineReason.EXPORT_CONTROL_BLOCKED.name())) {
                AssertCollector.assertThat("All values of Decline Reason Column are not *EXPORT_CONTROL_BLOCKED*",
                    fileData[i][7], equalTo(DeclineReason.EXPORT_CONTROL_BLOCKED.name()), assertionErrorList);
            } else {
                AssertCollector.assertThat("All values of Decline Reason Column are not *EXPORT_CONTROL_UNRESOLVED",
                    fileData[i][7], equalTo(DeclineReason.EXPORT_CONTROL_UNRESOLVED.name()), assertionErrorList);
            }
            AssertCollector.assertThat("All values of Order State Column are not *DECLINED*", fileData[i][1],
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider for testDeclinedOrderReportWithDeclineReasonAndFeatureFlagOn
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForDeclineReasonForDeclinedOrderReport")
    public Object[][] getTestDataForDeclinedOrder() {
        return new Object[][] { { "ANY (*)" }, { DeclineReason.OTHER_REASON.name() },
                { DeclineReason.PAYMENT_PROCESSOR_DECLINED.name() }, { DeclineReason.EXPORT_CONTROL_BLOCKED.name() } };
    }

    /**
     * Delete all the export control statistics report excel file from the download path
     */
    private void cleanDeclinedOrderReportFile() {
        // Delete all existing files with name "ExportControlStatistics" in the
        // download path
        String downloadPath;
        final Util util = new Util();
        if (System.getProperty("os.name").startsWith("Mac")) {
            final String home = System.getProperty("user.home");
            downloadPath = home + getEnvironmentVariables().getDownloadPathForMac();
            LOGGER.info("Download path" + downloadPath);
            util.deleteAllFilesWithSpecificFileName(downloadPath, FILE_NAME);
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            downloadPath = getEnvironmentVariables().getDownloadPathForWindows();
            util.deleteAllFilesWithSpecificFileName(downloadPath, FILE_NAME);
        }
    }

}
