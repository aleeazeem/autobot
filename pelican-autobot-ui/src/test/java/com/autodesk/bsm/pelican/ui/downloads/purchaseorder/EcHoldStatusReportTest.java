package com.autodesk.bsm.pelican.ui.downloads.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.ECHoldReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * This test class tests Order In Export Control Hold Report. AdminTool --> Reports --> Purchase Order Reports -->
 * Orders in EC Hold Report
 *
 * @author Muhammad
 */
public class EcHoldStatusReportTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private static ECHoldReportPage ecHoldReportPage;
    private static GenericGrid reportGrid;
    private static final String getDateAfter =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 60);
    private static final String getDateBefore =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1);
    private static final Logger LOGGER = LoggerFactory.getLogger(EcHoldStatusReportTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final String priceIdForBic = bicOfferings.getIncluded().getPrices().get(0).getId();
        final HashMap<String, String> userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        final String userExternalKey = "User-EC-Hold-Status";
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        LOGGER.info("User id: " + user.getId());
        LOGGER.info("User ext key: " + user.getExternalKey());

        String initialEcStatus = null;
        for (int i = 1; i < 7; i++) {
            if (i == 1) {
                initialEcStatus = ECStatus.UNVERIFIED.getName();
            }
            if (i == 2) {
                initialEcStatus = ECStatus.REVIEW.getName();
            }
            if (i == 3) {
                initialEcStatus = ECStatus.BLOCK.getName();
            }
            if (i == 4) {
                initialEcStatus = ECStatus.REOPEN.getName();
            }
            if (i == 5) {
                initialEcStatus = ECStatus.ACCEPT.getName();
            }
            if (i == 6) {
                initialEcStatus = ECStatus.HARDBLOCK.getName();
            }
            final BuyerUser buyerUser = new BuyerUser();
            buyerUser.setId(user.getId());
            buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
            buyerUser.setExternalKey(userExternalKey);
            buyerUser.setInitialExportControlStatus(initialEcStatus);

            // Submitting purchase order with authorize command
            LOGGER.info("Submitting a Purchase Order");
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceIdForBic, buyerUser, 1);
        }

        ecHoldReportPage = adminToolPage.getPage(ECHoldReportPage.class);
        reportGrid = adminToolPage.getPage(GenericGrid.class);
        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * Verify all headers of columns in Report
     *
     * @result report should show 11 columns and name of the headers of each column should be Order ID, Order Date,
     *         Order State, Order type, EC Status, User Customer Name, Company Name, Billing Country, Store, Order
     *         Total.
     */
    @Test
    public void testEcHoldStatusReportHeaders() {
        ecHoldReportPage.navigateToExportControlReportPage();
        ecHoldReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Total number of columns are not correct", reportGrid.getColumnHeaders().size(),
            equalTo(11), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", reportGrid.getColumnHeaders().get(0), equalTo("Order ID"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", reportGrid.getColumnHeaders().get(1), equalTo("Order Date"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", reportGrid.getColumnHeaders().get(2), equalTo("Order State"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", reportGrid.getColumnHeaders().get(3), equalTo("Order Type"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", reportGrid.getColumnHeaders().get(4), equalTo("EC Status"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", reportGrid.getColumnHeaders().get(5), equalTo("User"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", reportGrid.getColumnHeaders().get(6), equalTo("Customer Name"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", reportGrid.getColumnHeaders().get(7), equalTo("Company Name"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", reportGrid.getColumnHeaders().get(8),
            equalTo("Billing Country"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 10", reportGrid.getColumnHeaders().get(9), equalTo("Store"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 11", reportGrid.getColumnHeaders().get(10), equalTo("Order Total"),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies the data which is populated in Report in their respective column correctly.
     */
    @Test
    public void testEcHoldStatusReportData() {
        ecHoldReportPage.navigateToExportControlReportPage();
        ecHoldReportPage.submit(TimeConstants.ONE_SEC);
        final int orderCount = reportGrid.getTotalItems();

        if (orderCount > 0) {
            AssertCollector.assertThat("Why Order Type of PO is not *NEW ACQUISITION*",
                reportGrid.getColumnValues("Order Type"), everyItem(equalTo("NEW_ACQUISITION")), assertionErrorList);
            AssertCollector.assertThat("Why status of PO is not *AUTHORIZED*",
                reportGrid.getColumnValues("Order State"), everyItem(equalTo(OrderState.AUTHORIZED.toString())),
                assertionErrorList);
            final int reportIndex = selectOrderRandomlyFromFirstPage(orderCount);
            final String orderIdInReport = reportGrid.getColumnValues("Order ID").get(reportIndex);
            final String orderDateInReport = reportGrid.getColumnValues("Order Date").get(reportIndex);
            final String orderStateInReport = reportGrid.getColumnValues("Order State").get(reportIndex);
            final String ecStatusInReport = reportGrid.getColumnValues("EC Status").get(reportIndex);
            final String userInReport = reportGrid.getColumnValues("User").get(reportIndex);
            final String storeInReport = reportGrid.getColumnValues("Store").get(reportIndex);
            final String orderTotalInReport = reportGrid.getColumnValues("Order Total").get(reportIndex);
            reportGrid.selectResultRow(reportIndex + 1);

            final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(orderIdInReport);

            AssertCollector.assertThat("Id in PO is not same as shown in Report", getPurchaseOrder.getId(),
                equalTo(orderIdInReport), assertionErrorList);
            AssertCollector.assertThat("Created Date in PO is not same as shown in Report",
                (getPurchaseOrder.getCreationTime()).substring(0, 10), equalTo(orderDateInReport), assertionErrorList);
            AssertCollector.assertThat("Order State in PO as shown in Report", getPurchaseOrder.getOrderState(),
                equalTo(orderStateInReport), assertionErrorList);
            AssertCollector.assertThat("Export Control Status in PO is not same as shown in Report",
                getPurchaseOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ecStatusInReport),
                assertionErrorList);
            AssertCollector.assertThat("External key on detail Page is not same as shown in Report",
                getPurchaseOrder.getBuyerUser().getExternalKey(), equalTo(userInReport), assertionErrorList);
            AssertCollector.assertThat("Store in PO is not same as shown in Report",
                getPurchaseOrder.getStoreExternalKey(), equalTo(storeInReport), assertionErrorList);
            final String amountInGetPo =
                getPurchaseOrder.getTransactions().getTransactions().get(0).getGatewayResponse().getAmountCharged()
                    + " " + getPurchaseOrder.getTransactions().getTransactions().get(0).getGatewayResponse()
                        .getAmountChargedCurrencyISOCode();
            AssertCollector.assertThat("Total Amount in PO is not same as shown in Report", amountInGetPo,
                equalTo(orderTotalInReport), assertionErrorList);
            AssertCollector.assertAll(assertionErrorList);
        }
    }

    /**
     * This test method verifies the result of Report with combination of different filters
     *
     * @param ecStatus ()
     */
    @Test(dataProvider = "combinationOfOptions")
    public void testEcHoldStatusReportSearchFilters(final String store, final String dateAfter, final String dateBefore,
        final String ecStatus) {
        ecHoldReportPage.getReportWithSelectedFilters(store, getDateAfter, getDateBefore, ecStatus);
        final int ordersCount = reportGrid.getTotalItems();
        if (ordersCount > 0) {
            AssertCollector.assertThat("Why status of PO is *Accept*", reportGrid.getColumnValues("EC Status"),
                everyItem(not(equalTo("Accept"))), assertionErrorList);

            final List<String> createdOrderDates = reportGrid.getColumnValues("Order Date");
            for (final String createdDate : createdOrderDates) {
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(createdDate, PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    greaterThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(getDateAfter, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                    assertionErrorList);
                AssertCollector.assertThat("Found order date outside of requested range",
                    DateTimeUtils.convertStringToDate(createdDate, PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    lessThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(getDateBefore, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                    assertionErrorList);
            }
            if (ecStatus.equals("Include Unverified")) {
                AssertCollector.assertThat("Why EC status is not *Unverified*", reportGrid.getColumnValues("EC Status"),
                    everyItem(equalTo("Unverified")), assertionErrorList);
            }
            if (ecStatus.equals("Include Block")) {
                AssertCollector.assertThat("Why EC status is not *Block*", reportGrid.getColumnValues("EC Status"),
                    everyItem(equalTo("Block")), assertionErrorList);
            }
            if (ecStatus.equals("Include Review")) {
                AssertCollector.assertThat("Why EC status is not *Review*", reportGrid.getColumnValues("EC Status"),
                    everyItem(equalTo("Review")), assertionErrorList);
            }
            if (ecStatus.equals("Included Hard Block")) {
                AssertCollector.assertThat("Why EC status is not *Hard Block*", reportGrid.getColumnValues("EC Status"),
                    everyItem(equalTo("Hard Block")), assertionErrorList);
            }
            if (ecStatus.equals("Included Reopen")) {
                AssertCollector.assertThat("Why EC status is not *Unverified*", reportGrid.getColumnValues("EC Status"),
                    everyItem(equalTo("Include Hard Block")), assertionErrorList);
            }
        } else {
            LOGGER.info("No result found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies the error message if no status is selected to generate report
     */
    @Test
    public void testEcHoldStatusReportErrorMessageForNonSelectionOfAnyStatus() {
        ecHoldReportPage.navigateToExportControlReportPage();
        ecHoldReportPage.uncheckAllStatuses();
        ecHoldReportPage.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertThat("Option Error is not Generated for Filters",
            ecHoldReportPage.getStatusErrorMessage(), equalTo("Must select at least one status"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies the error message if date after and date before are empty
     */
    @Test
    public void testEcHoldStatusReportErrorMessageForNonSelectionOfAnyDate() {
        ecHoldReportPage.navigateToExportControlReportPage();
        ecHoldReportPage.purchaseAfterDateClear();
        ecHoldReportPage.purchaseBeforeDateClear();
        ecHoldReportPage.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertThat("Option Error is not Generated for Filters", ecHoldReportPage.getDateError(),
            equalTo("Date range cannot be empty. Please enter two valid dates."), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * combination of filters use to find subscriptions by advanced find
     */
    @DataProvider(name = "combinationOfOptions")
    public Object[][] getTestData() {
        return new Object[][] { { getStoreUs().getName(), getDateAfter, getDateBefore, "Include Unverified" },
                { getStoreUs().getName(), getDateAfter, getDateBefore, "Include Block" },
                { null, getDateAfter, getDateBefore, "Include Review" },
                { null, getDateAfter, getDateBefore, "Include Reopen" },
                { null, getDateAfter, getDateBefore, "Include Hard Block" } };
    }

    /**
     * Method to get order index randomly from first page even if more than 20 results exist on grid
     */
    private int selectOrderRandomlyFromFirstPage(final int orderCount) {
        if (orderCount > 0) {
            int promoIndex;
            final Random index = new Random();
            if (orderCount <= 20) {
                promoIndex = index.nextInt(orderCount);
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
}
