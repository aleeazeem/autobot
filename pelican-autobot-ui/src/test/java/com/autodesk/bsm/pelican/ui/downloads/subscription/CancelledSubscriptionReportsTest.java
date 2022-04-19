package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionClient;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscriptions;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.CancelledSubscriptionsReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.CancelledSubscriptionsReportResultsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Admin Tool's Cancelled Subscription Reports. On Admin Tool's Main Tab navigate to Reports -> Subscription Reports ->
 * Cancelled Subscription Report.
 *
 * @author Muhammad
 */
public class CancelledSubscriptionReportsTest extends SeleniumWebdriver {

    private static final String CONTENT_TYPE = "application/xml";
    private static CancelledSubscriptionsReportPage cancelledSubscriptionReportPage;
    private static CancelledSubscriptionsReportResultsPage cancelledSubscriptionReportResultPage;
    private static SubscriptionDetailPage subscriptionDetailPage;
    private PelicanPlatform resource;
    private static final String FILE_NAME = "CancelledSubscriptionsReport";
    private static String fileName;
    private static List<String> expectedColumnHeaderList;
    private static final String creationDateAfter =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7);
    private static final String creationDateBefore = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);
    private static final String cancellationDateAfter =
        DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 7);
    private static final String cancellationDateBefore =
        DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);

    /**
     * Data setup - create user for each test!
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionsClient subscriptionsResource =
            new SubscriptionsClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        final SubscriptionClient subscriptionResource =
            new SubscriptionClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        cancelledSubscriptionReportPage = adminToolPage.getPage(CancelledSubscriptionsReportPage.class);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME + "." + PelicanConstants.XLSX_FORMAT;

        final HashMap<String, String> requestParameters = new HashMap<>();
        requestParameters.put("statuses", "EXPIRED");
        requestParameters.put("daysPastExpired", "5");
        final Subscriptions subscriptions = subscriptionsResource.getSubscriptions(requestParameters, CONTENT_TYPE);

        String subscriptionId;
        // if there is no subscription in response then create one cancelled subscription
        if (subscriptions.getSubscriptions().size() < 1) {
            final Offerings bicBasicMonthlyOffering =
                subscriptionPlanApiUtils.addSubscriptionPlan(getStoreExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                    BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
            final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
            priceQuantityMap.put(bicBasicMonthlyOffering.getIncluded().getPrices().get(0).getId(), 1);
            final PurchaseOrder purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, null);
            subscriptionId = purchaseOrder.getSubscriptionId();
            // cancel the created subscription
            subscriptionResource.cancelSubscription(subscriptionId, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        }

        // Add all column names to a list
        expectedColumnHeaderList = new ArrayList<>();
        expectedColumnHeaderList.add(PelicanConstants.SUBSCRIPTION_ID_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.SUBSCRIPTION_OWNER_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.AUTO_RENEW_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.SUBSCRIPTION_STATUS_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.CANCELLED_DATE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.EXPIRATION_DATE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.REQUESTOR_FIELD);
    }

    /**
     * Test case to verify Cancelled subscription report headers after.
     */
    @Test
    public void testCancelledSubscriptionReportHeadersView() {
        cancelledSubscriptionReportResultPage = cancelledSubscriptionReportPage.generateReport(PelicanConstants.VIEW,
            creationDateAfter, creationDateBefore, null, null);
        AssertCollector.assertThat("Title of the Page is not Correct",
            cancelledSubscriptionReportResultPage.getDriver().getTitle(),
            equalTo(PelicanConstants.PELICAN + " - " + PelicanConstants.CANCELLED_SUBSCRIPTION_REPORT),
            assertionErrorList);
        final List<String> actualColumnHeadersList = cancelledSubscriptionReportResultPage.getColumnHeaders();

        if (expectedColumnHeaderList.size() != actualColumnHeadersList.size()) {
            // Failing the test here before checking header names to prevent array out of bounds
            // exception.
            Assert.fail("Number of columns are not correct.");
        }

        for (int i = 0; i < expectedColumnHeaderList.size(); i++) {
            AssertCollector.assertThat("Incorrect Header '" + i + 1 + "'", actualColumnHeadersList.get(i),
                equalTo(expectedColumnHeaderList.get(i)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to verify Cancelled subscription report headers in download.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testCancelledSubscriptionReportHeadersInDownload() throws IOException {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), FILE_NAME + "." + PelicanConstants.XLSX_FORMAT);
        cancelledSubscriptionReportResultPage = cancelledSubscriptionReportPage
            .generateReport(PelicanConstants.DOWNLOAD, creationDateAfter, creationDateBefore, null, null);
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        if (expectedColumnHeaderList.size() != fileData[0].length) {
            Assert.fail("Number of columns are not correct.");
        }

        for (int i = 0; i < expectedColumnHeaderList.size(); i++) {
            AssertCollector.assertThat("Incorrect Header '" + i + 1 + "'", fileData[0][i],
                equalTo(expectedColumnHeaderList.get(i)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the values populated in report in their respective columns. this method more focus on
     * validating following: all column Values of auto-renew should be false all column values of subscription status
     * wither should be cancelled or expired cancelled date should not be out of range which is given as a filter to
     * generate report creation date should not be out of range which is given as a filter to generate report.
     *
     * @param action
     * @param creationDateAfter
     * @param creationDateBefore
     * @param cancellationDateAfter
     * @param cancellationDateBefore
     */
    @Test(dataProvider = "cancelledSubscriptionReportFilters")
    public void testDataInCancelledSubscriptionReportView(final String action, final String creationDateAfter,
        final String creationDateBefore, final String cancellationDateAfter, final String cancellationDateBefore) {
        cancelledSubscriptionReportResultPage = cancelledSubscriptionReportPage.generateReport(action,
            creationDateAfter, creationDateBefore, cancellationDateAfter, cancellationDateBefore);
        AssertCollector.assertThat("Title of the Page is not Correct",
            cancelledSubscriptionReportResultPage.getDriver().getTitle(),
            equalTo(PelicanConstants.PELICAN + " - " + PelicanConstants.CANCELLED_SUBSCRIPTION_REPORT),
            assertionErrorList);
        final int totalResults = cancelledSubscriptionReportResultPage.getTotalItems();

        // validate all values of auto renew column should be false
        AssertCollector.assertThat("Found Auto Renew other than false",
            cancelledSubscriptionReportResultPage.getColumnValues(PelicanConstants.AUTO_RENEW_FIELD),
            everyItem(equalTo("false")), assertionErrorList);

        // validate all values of subscription status column should be either cancelled or expired
        final List<String> actualOfferingStatus =
            cancelledSubscriptionReportResultPage.getColumnValues(PelicanConstants.SUBSCRIPTION_STATUS_FIELD);
        AssertCollector.assertThat("Found Subscription status other than cancelled and expired status",
            actualOfferingStatus, everyItem(isOneOf(Status.CANCELLED.toString(), Status.EXPIRED.toString())),
            assertionErrorList);

        // validate all values of cancellation date are in the date range
        if (cancellationDateAfter != null && cancellationDateBefore != null) {
            final Date cancellationDateBeforeInDateFormat = DateTimeUtils
                .convertStringToDate((cancellationDateBefore + " 00:00:00"), PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final Date cancellationDateAfterInDateFormat = DateTimeUtils
                .convertStringToDate(cancellationDateAfter + " 00:00:00", PelicanConstants.DATE_FORMAT_WITH_SLASH);
            for (final String cancelledDate : cancelledSubscriptionReportResultPage.getColumnValuesOfCancelledDate()) {
                final Date cancelledDateInDateFormat = DateTimeUtils.convertStringToDate(cancelledDate.substring(0, 18),
                    PelicanConstants.DATE_FORMAT_WITH_SLASH);
                AssertCollector.assertThat("Cancelled date is greater than cancelled date which is given as filter",
                    cancelledDateInDateFormat, lessThanOrEqualTo(cancellationDateBeforeInDateFormat),
                    assertionErrorList);
                AssertCollector.assertThat("Cancelled date is lesser than cancelled date which is given as filter",
                    cancelledDateInDateFormat, greaterThanOrEqualTo(cancellationDateAfterInDateFormat),
                    assertionErrorList);
            }
        }

        // validating of column value from subscription detail page.
        final int selectedIndex = cancelledSubscriptionReportResultPage.selectRowRandomlyFromFirstPage(totalResults);

        final String subscriptionIdOnReport =
            cancelledSubscriptionReportResultPage.getColumnValuesOfSubscriptionId().get(selectedIndex);
        final String subscriptionOwnerOnReport =
            cancelledSubscriptionReportResultPage.getColumnValuesOfSubscriptionOwner().get(selectedIndex);
        final String autoRenewOnReport =
            cancelledSubscriptionReportResultPage.getColumnValuesOfAutoRenew().get(selectedIndex);
        final String subscriptionStatusOnReport =
            cancelledSubscriptionReportResultPage.getColumnValuesOfSubscriptionStatus().get(selectedIndex);
        final String cancelledDateOnReport =
            cancelledSubscriptionReportResultPage.getColumnValuesOfCancelledDate().get(selectedIndex);
        final String expirationDateOnReport =
            cancelledSubscriptionReportResultPage.getColumnValuesOfExpirationDate().get(selectedIndex);
        cancelledSubscriptionReportPage.selectResultRow(selectedIndex + 1);

        int cancelDateIndex = 0;
        final List<SubscriptionActivity> subscriptionActivityList = subscriptionDetailPage.getSubscriptionActivity();
        final int totalRecordsInSubscriptionActivity = subscriptionActivityList.size();
        // Looping backwards for performance reason. Cancel activity will most likely be the last activity.
        for (int i = (totalRecordsInSubscriptionActivity - 1); i >= 0; i--) {
            if ((subscriptionActivityList.get(i).getActivity()).equals(PelicanConstants.CANCEL)) {
                cancelDateIndex = i;
                break;
            }
        }

        AssertCollector.assertThat("Subscription id on detail page is not same as shown in report",
            subscriptionDetailPage.getId(), equalTo(subscriptionIdOnReport), assertionErrorList);
        AssertCollector.assertThat("Auto-Renew on detail page is not same as shown in report",
            subscriptionDetailPage.getAutoRenewEnabled(), equalTo(autoRenewOnReport), assertionErrorList);
        AssertCollector.assertThat("Subscription status on detail page is not same as shown in report",
            subscriptionDetailPage.getStatus(), equalTo(subscriptionStatusOnReport), assertionErrorList);
        AssertCollector.assertThat("Cancelled date on detail page is not same as shown in report",
            subscriptionDetailPage.getSubscriptionActivity().get(cancelDateIndex).getDate(),
            equalTo(cancelledDateOnReport), assertionErrorList);
        AssertCollector.assertThat("Expiration date on detail page is not same as shown in report",
            subscriptionDetailPage.getExpirationDate(),
            equalTo((expirationDateOnReport.equals("") ? "-" : expirationDateOnReport.split(" ")[0])),
            assertionErrorList);
        String expectedUserName = "";
        String expectedUserId = "";
        final Object apiResponse = resource.user().getUserByExternalKey(subscriptionOwnerOnReport);
        if (apiResponse instanceof HttpError) {
            final HttpError httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(User.class), assertionErrorList);
        } else {
            // type casting apiResponse to User
            final User user = (User) apiResponse;
            expectedUserId = user.getId();
            expectedUserName = user.getName();
        }
        AssertCollector.assertThat("User on detail page is not same as shown in report",
            subscriptionDetailPage.getUser(), equalTo(expectedUserName + " (" + expectedUserId + ")"),
            assertionErrorList);

        // report doesn't show column of creation date of subscriptions. Creation date can be verified on subscription
        // details page whether or not creation date falls between the date range which was given as filter to generate
        // report
        if (creationDateAfter != null && creationDateBefore != null) {
            final String creationDateOnDetailPage = subscriptionActivityList.get(0).getDate().substring(0, 18);
            final Date creationDateInDateFormat =
                DateTimeUtils.convertStringToDate(creationDateOnDetailPage, PelicanConstants.DATE_FORMAT_WITH_SLASH);
            AssertCollector.assertThat(
                "Creation Date on subscription detail page is greater than the given date range " + "report page ",
                creationDateInDateFormat,
                lessThanOrEqualTo(
                    DateTimeUtils.convertStringToDate(creationDateBefore, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                assertionErrorList);
            AssertCollector.assertThat(
                "Creation Date on subscription detail page is smaller than the given date range " + "report page",
                creationDateInDateFormat,
                greaterThanOrEqualTo(
                    DateTimeUtils.convertStringToDate(creationDateAfter, PelicanConstants.DATE_FORMAT_WITH_SLASH)),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the values populated in report in their respective columns. this method more focus on
     * validating following: all column Values of auto-renew should be false all column values of subscription status
     * wither should be cancelled or expired cancelled date should not be out of range which is given as a filter to
     * generate report creation date should not be out of range which is given as a filter to generate report.
     *
     * @param download
     * @param creationDateAfter
     * @param creationDateBefore
     * @param cancellationDateAfter
     * @param cancellationDateBefore
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test(dataProvider = "cancelledSubscriptionReportFilters")
    public void testDataInCancelledSubscriptionReportDownload(final String action, final String creationDateAfter,
        final String creationDateBefore, final String cancellationDateAfter, final String cancellationDateBefore)
        throws IOException {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), FILE_NAME + "." + PelicanConstants.XLSX_FORMAT);
        // get download report
        cancelledSubscriptionReportPage.generateReport(PelicanConstants.DOWNLOAD, creationDateAfter, creationDateBefore,
            cancellationDateAfter, cancellationDateBefore);
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        Date cancellationDateBeforeInDateFormat = null;
        Date cancellationDateAfterInDateFormat = null;
        if (cancellationDateBefore != null && cancellationDateAfter != null) {
            cancellationDateBeforeInDateFormat = DateTimeUtils
                .convertStringToDate((cancellationDateBefore + " 00:00:00"), PelicanConstants.DATE_FORMAT_WITH_SLASH);
            cancellationDateAfterInDateFormat = DateTimeUtils.convertStringToDate(cancellationDateAfter + " 00:00:00",
                PelicanConstants.DATE_FORMAT_WITH_SLASH);
        }

        int i;
        if (fileData.length > 0) {
            for (i = 1; i < fileData.length; i++) {
                AssertCollector.assertThat("All values of auto-renew in excel file are not false", fileData[i][2],
                    equalTo("false"), assertionErrorList);
                AssertCollector.assertThat(
                    "Found subscription status other then expired or cancelled in downloaded file", fileData[i][3],
                    isOneOf(Status.CANCELLED.toString(), Status.EXPIRED.toString()), assertionErrorList);

                if (cancellationDateAfter != null && cancellationDateBefore != null) {
                    final Date cancelledDateInXlsxFile = DateTimeUtils
                        .convertStringToDate(fileData[i][4].substring(0, 18), PelicanConstants.DATE_FORMAT_WITH_SLASH);
                    AssertCollector.assertThat(
                        "cancelled date is greater than the date which is given as a start date " + "filter",
                        cancelledDateInXlsxFile, lessThanOrEqualTo(cancellationDateBeforeInDateFormat),
                        assertionErrorList);
                    AssertCollector.assertThat(
                        "cancelled date is lesser than the date which is given as a start date" + "filter",
                        cancelledDateInXlsxFile, greaterThanOrEqualTo(cancellationDateAfterInDateFormat),
                        assertionErrorList);
                }
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * filters required to generate cancelled subscription report
     *
     * @return values of filters to generate report
     */
    @DataProvider(name = "cancelledSubscriptionReportFilters")
    public Object[][] getCancelledSubscriptionReportFilters() {
        return new Object[][] { { PelicanConstants.VIEW, creationDateAfter, creationDateBefore, null, null },
                { PelicanConstants.VIEW, creationDateAfter, creationDateBefore, cancellationDateAfter,
                        cancellationDateBefore } };
    }

    /**
     * Test case to verify error scenarios for Cancelled subscriptions Reports.
     */
    @Test
    public void testCancelledSubscriptionReportErrorScenarios() {
        final String startDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 93);
        final String endDate = DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        cancelledSubscriptionReportPage.generateReportWithErrors(PelicanConstants.VIEW, null, null, null, null);
        AssertCollector.assertThat("Default date error message is not displayed",
            cancelledSubscriptionReportPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_DATE_SELECTION_ERROR_MESSAGE), assertionErrorList);

        cancelledSubscriptionReportPage.generateReport(PelicanConstants.VIEW, null, null, startDate, endDate);
        AssertCollector.assertThat("Default error message is not displayed", cancelledSubscriptionReportPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Cancellation Date: Error message for data range limitation is not displayed",
            cancelledSubscriptionReportPage.getErrorMessageForField(), equalTo(PelicanErrorConstants.DATE_RANGE_ERROR),
            assertionErrorList);

        cancelledSubscriptionReportPage.generateReport(PelicanConstants.VIEW, startDate, endDate, null, null);
        AssertCollector.assertThat("Creation Date: Error message for data range limitation is not displayed",
            cancelledSubscriptionReportPage.getErrorMessageForField(), equalTo(PelicanErrorConstants.DATE_RANGE_ERROR),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
