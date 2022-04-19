package com.autodesk.bsm.pelican.ui.financereport;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FinanceReportHeaders;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.AddCreditDaysPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Finance Report Tests for Credit Days Discounts
 * <p>
 * Validate the correct headers and data text are in csv format. This test will run in Demo application for DEV For the
 * details of the story please check the rally link :
 * https://rally1.rallydev.com/#/19951292602d/detail/userstory/31821574121 /testcases
 * https://rally1.rallydev.com/#/19951292602d/detail/userstory/33549930533
 *
 * @author kishor
 */
public class CreditDayDiscountsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private static final String ORDER_NUMBER = FinanceReportHeaders.ORDER_NUMBER.getHeader();
    private static final String CREDIT_DAYS_DISCOUNT = FinanceReportHeaders.CREDIT_DAYS_DISCOUNT.getHeader();
    private static final String TOTAL_ORDER_PRICE = FinanceReportHeaders.TOTAL_ORDER_PRICE.getHeader();
    private static final String TOTAL_PRICE = FinanceReportHeaders.TOTAL_PRICE.getHeader();
    private FindSubscriptionsPage findSubscriptionsPage;
    private AddCreditDaysPage addCreditDaysPage;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(CreditDayDiscountsTest.class.getSimpleName());
    private BuyerUser buyerUser;

    /**
     * Data setup - create user for each test!
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        addCreditDaysPage = adminToolPage.getPage(AddCreditDaysPage.class);

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This test method validates the FinanceReports include Credit Day discounts and Total order value calculated
     * including the discounts
     *
     * @param creditDays - Credit Days allocated
     * @param additionalCreditDays - Additional credit days. Set it to '0' unless the test want to test allocation of
     *        credit days multiple times scenario [E.g add 10 days and deduct 5 days, then addCreditDays parameter will
     *        be -5]
     * @param creditNote - Note for the credit allocation
     * @param expTax - The tax amount to be added in the order price in double type
     */
    @Test(dataProvider = "getCreditDaysData")
    public void generateReportWithCreditDiscountsTest(final String creditNote, final int creditDays,
        final int additionalCreditDays, final String priceId, final double expTax) {
        // Create subscription before creating a Financial report! Submit a
        // purchase order and get the subscription Id from the response

        // submit a purchase order to create a commercial subscription
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, 2);

        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        final String subsId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        LOGGER.info("Subscription Id :" + subsId);
        // Get the subscription by Id which just got created to access more
        // subscription attributes
        SubscriptionDetailPage subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subsId);

        // Get the next billing date to calculate the discount
        final String nxtBillingDate = subscriptionDetailPage.getNextBillingDate();
        LOGGER.info("Next Billing Date for New Acquisition is : " + nxtBillingDate);

        final double expListPrice =
            Double.parseDouble(subscriptionDetailPage.getNextBillingCharge().split("\\s+")[0].replace(",", ""));
        LOGGER.info("List price : " + expListPrice);

        // Validate the Subscription details
        String existingCreditDays = subscriptionDetailPage.getDaysCredited();
        LOGGER.info("Credit Days already available : " + existingCreditDays);

        // Add Credit Days for the user
        addCreditDaysPage.addCreditDays(subsId, creditDays, creditNote);

        // Checking the credit Days! convert String to Int for mathematical
        // calculation
        int intCreditDays = creditDays + Integer.parseInt(existingCreditDays);
        // Convert back to string
        String expCreditDays = Integer.toString(intCreditDays);
        AssertCollector.assertThat("Incorrect Credit Days updated", subscriptionDetailPage.getDaysCredited(),
            equalTo(expCreditDays), assertionErrorList);

        // Add additional credit Days, i.e when additional days if >0 only
        if (additionalCreditDays != 0) {
            addCreditDaysPage.addCreditDays(subsId, additionalCreditDays, creditNote);
            // Checking the credit Days! convert String to Int for mathematical
            // calculation
            intCreditDays = intCreditDays + additionalCreditDays;
            // Convert back to string and setting the new value to expected
            // credit Days
            expCreditDays = Integer.toString(intCreditDays);
            AssertCollector.assertThat("Incorrect Credit Days updated", subscriptionDetailPage.getDaysCredited(),
                equalTo(expCreditDays), assertionErrorList);
            LOGGER.info("Credit Days available after Additional credits " + expCreditDays);
        }

        // Setting the start time limit for generating finance report
        final String expStartDateTime = DateTimeUtils.getNowMinusMinutes(PelicanConstants.DB_DATE_FORMAT, 2);

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subsId);

        final String renewOrderId = purchaseOrderUtils.submitPurchaseOrderAndGetId(Payment.PaymentType.CREDIT_CARD,
            null, listOfSubscriptions, false, true, buyerUser);
        if (creditDays == 0) {
            LOGGER.info("Added mini wait to update Renewal Subscription.");
            Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);
        }
        // Getting the renewed subscription Object to get the next billing date!
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subsId);
        final String nxtRenewalBillingDate = subscriptionDetailPage.getNextBillingDate();
        LOGGER
            .info("The Renewal Details : Purchase Order : " + renewOrderId + " Billing Date :" + nxtRenewalBillingDate);
        // Verify the credits displayed in the Subscription Details page
        existingCreditDays = subscriptionDetailPage.getDaysCredited();
        LOGGER.info("Credit Days available after renewal: " + existingCreditDays);

        // Verify the credit days left after the renewal are proper.
        // If Credit days are < No of days in the renewal month credits left
        // should be 0
        // Else The leftover credit days should be displayed E.g if 100 days
        // credit has been given and month has 31 days,
        // After renewal 100-31=69 days should be displayed in the Days Credited
        // column.
        int creditDaysLeft;
        final double daysInBillingCycle = DateTimeUtils.getDaysInBillingCycle(
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), nxtRenewalBillingDate);
        if (intCreditDays > daysInBillingCycle) {
            // Assign the days in the month as credit days as max credit days
            // for a month can be days in month
            expCreditDays = Double.toString(daysInBillingCycle);
            creditDaysLeft = (int) (intCreditDays - daysInBillingCycle);
            AssertCollector.assertThat("Incorrect Credit Days updated after renewal",
                subscriptionDetailPage.getDaysCredited(), equalTo(Integer.toString(creditDaysLeft)),
                assertionErrorList);
        } else {
            // If credit days allocated is lesser than days in a month.Days
            // credited should be set to zero after renewal.
            AssertCollector.assertThat("Incorrect Credit Days updated after renewal",
                subscriptionDetailPage.getDaysCredited(), equalTo("0"), assertionErrorList);
        }

        Util.waitInSeconds(TimeConstants.THREE_SEC);
        // Get the Finance report using the API using end timestamp after all
        // POs created and renewed
        final String expEndDateTime = DateTimeUtils.getNowAsUTCPlusMinutes(PelicanConstants.DB_DATE_FORMAT, 3);

        LOGGER.info("The Finance Report between :" + expStartDateTime + " and " + expEndDateTime);
        // Generate finance report using finance Report using API
        final String actualRestHeader =
            resource.financeReport().getReportHeader(expStartDateTime, expEndDateTime, null, null, null, null);
        final List<String> actualReport =
            resource.financeReport().getReportData(expStartDateTime, expEndDateTime, null, null, null, null);

        // Loop through the report lines for the purchase order Id only if size
        // of the report is more than 0
        if (actualReport.size() == 0) {
            Assert.fail("FAILURE!! NO ENTRIES IN THE REPORTS!");
        }

        // Variable to return true if the Order number with PurchaseOrder Id
        // is present in the report
        boolean recordFound = false;
        for (final String reportRow : actualReport) {
            // Get the line of the report with the Renewal PO id as Order
            // Number!
            if (getColumnValueFromList(reportRow, actualRestHeader, ORDER_NUMBER).equalsIgnoreCase(renewOrderId)) {
                recordFound = true;// Yes the renewal Purchase order is
                // available in the report
                LOGGER.info("Purchase Order No. " + getColumnValueFromList(reportRow, actualRestHeader, ORDER_NUMBER));
                // Calculate the Credit Discount
                // Credit Discount = (ListPrice/Number of days)*No of
                // creditDays!
                final double dailyValue = expListPrice / daysInBillingCycle;
                final double days = Double.parseDouble(expCreditDays);
                final double expCreditDiscount = dailyValue * days;

                final String expTotalPrice = getFormattedDecimal(expListPrice - expCreditDiscount);
                final String expOrderPrice = getFormattedDecimal(expListPrice - expCreditDiscount);
                // Validate the credit days and other fields in the report
                final String actualCreditDays =
                    getColumnValueFromList(reportRow, actualRestHeader, CREDIT_DAYS_DISCOUNT);
                final String actualTotalPrice = getColumnValueFromList(reportRow, actualRestHeader, TOTAL_PRICE);
                final String actualOrderPrice = getColumnValueFromList(reportRow, actualRestHeader, TOTAL_ORDER_PRICE);

                // Assuming the Last entry in the report is the expected Row
                // of report!
                AssertCollector.assertThat("Credit Days is not as expected", actualCreditDays,
                    equalTo(getFormattedDecimal(expCreditDiscount)), assertionErrorList);
                AssertCollector.assertThat("Total Price is not as expected", actualTotalPrice, equalTo(expTotalPrice),
                    assertionErrorList);
                AssertCollector.assertThat("Order Price is not as expected", actualOrderPrice, equalTo(expOrderPrice),
                    assertionErrorList);
                break;
            }
        }
        if (!recordFound) {
            Assert.fail("FAILURE!! PURCHASE ORDER NOT INCLUDED IN THE REPORT");
        }
        // Collect all asserts and print it to the logs
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * @return a string value in the list which corresponds the columname Given the single report row and a header row
     *         as strings. It finds the string which belongs to the column from the report row
     */
    private String getColumnValueFromList(final String fullLine, final String allHeaders, final String columnName) {
        String value;
        // get column index for order date
        int columnIndex = -1;
        final String[] columns = allHeaders.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex < 0) {
            throw new RuntimeException("Unable to find header '" + columnName + "' in Finance Report\n" + allHeaders);
        }

        // get values from the report row by splitting using a ","
        final String[] rowData = fullLine.split(",");
        value = rowData[columnIndex]; // Find the value from the list which
        // belongs to the column name index
        return value;
    }

    /* Data provider for the test */
    @DataProvider(name = "getCreditDaysData")
    private Object[][] getCreditDaysData() {

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final String bicMonthlyPriceId = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
            .getIncluded().getPrices().get(0).getId();
        final String bicAnnualPriceId = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.YEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM)
            .getIncluded().getPrices().get(0).getId();
        return new Object[][] {

                { "Testing finance reports with 100, i.e more than billing cycle credit days for BIC MONTHLY OFFERING ",
                        100, 0, bicMonthlyPriceId, 1.0 },
                { "Testing finance reports with 5 credit days for BIC ANNUAL OFFERING Without Tax", 5, 0,
                        bicAnnualPriceId, 0.0 } };
    }

    /**
     * @return This method returns 2 digit decimal formatted String format of the input(double number)
     */
    private String getFormattedDecimal(final double number) {
        LOGGER.info("Actual Value : " + number);
        final BigDecimal bd = new BigDecimal(number);
        return bd.setScale(2, RoundingMode.HALF_EVEN).toPlainString();
    }

}
