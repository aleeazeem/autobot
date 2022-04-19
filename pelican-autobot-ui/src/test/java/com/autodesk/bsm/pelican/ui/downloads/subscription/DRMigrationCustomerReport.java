package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionClient;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.PendingMigrationSubscriptionsReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class tests Pending migration subscription report in Admin Tool which will test all the fields of a customer
 * report based upon selected store or stores.
 *
 * @author Muhammad Azeem
 */
public class DRMigrationCustomerReport extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static PelicanPlatform resource;
    private static String storeName;
    private static final int TOTAL_COLUMNS = 9;
    private static final String TITLE = "Subscriptions in Pending Migration Status - Report";
    private static final String SUB_ID = "ID";
    private static final String EXT_KEY = "External Key";
    private static final String STATUS = "Status";
    private static final String SEATS = "Seats";
    private static final String OFFER = "Offer";
    private static final String OFFER_TERM = "Offer Term";
    private static final String USER = "User";
    private static final String STORE = "Store";
    private static final String NEXT_BILLING_AMOUNT = "Next Billing Amount";
    private static final Logger LOGGER = LoggerFactory.getLogger(DRMigrationCustomerReport.class.getSimpleName());

    /**
     * Data setup - open a admin tool page and login into it
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() throws IOException {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        resource = new PelicanPlatform(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        final XlsUtils xlsUtils = new XlsUtils();

        final UploadUtils uploadUtils = adminToolPage.getPage(UploadUtils.class);
        final String xlsFile = uploadUtils.getFilePath(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);
        String basicSubscriptionExtKey;
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();

        // Generate random subscriptionId and skuCode if not present
        final String subscriptionIdInFile = String.valueOf(RandomUtils.nextInt(99999999));
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SUB_ID_COLUMN), subscriptionIdInFile);
        basicSubscriptionExtKey = getSubscriptionExtKey(subscriptionIdInFile);

        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        LOGGER.info("basicSubscriptionExtKey " + basicSubscriptionExtKey);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        uploadUtils.uploadSubscriptions(adminToolPage, PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);
        final Subscription createdAdvancedSubscription =
            DbUtils.getSubscriptionData(basicSubscriptionExtKey, getEnvironmentVariables());
        final String subscriptionId = createdAdvancedSubscription.getId();

        final SubscriptionClient subscriptionResource =
            new SubscriptionClient(getEnvironmentVariables(), getEnvironmentVariables().getAppFamily());
        final Subscription subscription = subscriptionResource.getById(subscriptionId);
        LOGGER.info("subscription store id " + subscription.getPrice().getStoreId());
        LOGGER.info("subscriptionId: " + subscriptionId);
        final JStore store = resource.stores().getStore(subscription.getPrice().getStoreId());
        storeName = store.getName();

    }

    /**
     * This test method verifies all the required Headers of a Report
     *
     * @result Report should be opened with the column of all Headers which are nine in number
     */
    @Test
    public void testDRMigrationCustomerReportHeaders() {
        final PendingMigrationSubscriptionsReportPage drMigrationReport =
            adminToolPage.getPage(PendingMigrationSubscriptionsReportPage.class);
        drMigrationReport.navigateToDrMigrationReportPage();
        drMigrationReport.submit(TimeConstants.ONE_SEC);
        final GenericGrid drMigrationReportGrid = adminToolPage.getPage(GenericGrid.class);

        AssertCollector.assertThat("Title of the Page is not Correct", drMigrationReportGrid.getDriver().getTitle(),
            equalTo("Pelican - " + TITLE), assertionErrorList);
        AssertCollector.assertThat("Total number of columns are not correct",
            drMigrationReportGrid.getColumnHeaders().size(), equalTo(TOTAL_COLUMNS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", drMigrationReportGrid.getColumnHeaders().get(0),
            equalTo(SUB_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", drMigrationReportGrid.getColumnHeaders().get(1),
            equalTo(EXT_KEY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", drMigrationReportGrid.getColumnHeaders().get(2),
            equalTo(STATUS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", drMigrationReportGrid.getColumnHeaders().get(3),
            equalTo(SEATS), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", drMigrationReportGrid.getColumnHeaders().get(4), equalTo(USER),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", drMigrationReportGrid.getColumnHeaders().get(5),
            equalTo(OFFER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", drMigrationReportGrid.getColumnHeaders().get(6),
            equalTo(OFFER_TERM), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", drMigrationReportGrid.getColumnHeaders().get(7),
            equalTo(STORE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", drMigrationReportGrid.getColumnHeaders().get(8),
            equalTo(NEXT_BILLING_AMOUNT), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the report should be generated without selecting any Store and statistics of Uploaded Subscriptions under
     * the Title of the Page
     *
     * @result Report should be opened for all stores
     */
    @Test
    public void testDRMigrationCustomerReportWithAllStores() throws SQLException {
        int subscriptionIndex;
        final PendingMigrationSubscriptionsReportPage drMigrationReport =
            adminToolPage.getPage(PendingMigrationSubscriptionsReportPage.class);
        drMigrationReport.navigateToDrMigrationReportPage();
        drMigrationReport.submit(TimeConstants.ONE_SEC);
        final GenericGrid drMigrationReportGrid = adminToolPage.getPage(GenericGrid.class);

        // getting statistics of a report which are displayed on the Page of
        // Pending Migration Subscriptions Report
        final String totaluploadedSubs = drMigrationReport.getTotalSubsUploaded();
        final String totalPendingMigrationSubs = drMigrationReport.getTotalPendingMigrationSubs();
        final String convertedPercentageOfSubs = drMigrationReport.getPercentageOfConvertedSubs();

        final String expectedTotalSubscriptionsUploaded = "Total Subscriptions Uploaded: "
            + Integer.toString(DbUtils.totalSubscriptionUploaded(getEnvironmentVariables()));
        final String expectedNotConvertedSubs =
            "Total Subscriptions Not Converted: " + Integer.toString(drMigrationReportGrid.getTotalItems());
        final double calculatedPercentOfConvertedSubs =
            ((DbUtils.totalSubscriptionUploaded(getEnvironmentVariables()) - drMigrationReportGrid.getTotalItems())
                * 100.00) / (DbUtils.totalSubscriptionUploaded(getEnvironmentVariables()));
        final String expectedPercentageofSubscriptionsConverted =
            "% of Subscriptions Converted: " + String.format("%.2f", calculatedPercentOfConvertedSubs) + "%";

        AssertCollector.assertThat("Total number of subscriptions is not correct which are uploaded so far",
            totaluploadedSubs, equalTo(expectedTotalSubscriptionsUploaded), assertionErrorList);
        AssertCollector.assertThat("Total number of subscriptions is not correct which are not converted",
            totalPendingMigrationSubs, equalTo(expectedNotConvertedSubs), assertionErrorList);
        AssertCollector.assertThat("Percentage of cConverted Subscription is not correct", convertedPercentageOfSubs,
            equalTo(expectedPercentageofSubscriptionsConverted), assertionErrorList);

        // Report shows upto 40 results. So, either results are less than 40 or
        // more than 40 the subscription will be
        // selected randomly from the first page of the Report for validations.
        final Random index = new Random();
        if (drMigrationReportGrid.getTotalItems() <= 40) {
            subscriptionIndex = index.nextInt(drMigrationReportGrid.getTotalItems());
        } else {
            subscriptionIndex = index.nextInt(39);
        }

        // getting values after selecting a subscriptions randomly in a Report
        final String subIdInReport = drMigrationReportGrid.getColumnValues(SUB_ID).get(subscriptionIndex);
        String externalKeyInReport = drMigrationReportGrid.getColumnValues(EXT_KEY).get(subscriptionIndex);
        final String subStatusInReport = drMigrationReportGrid.getColumnValues(STATUS).get(subscriptionIndex);
        final String seatsInReport = drMigrationReportGrid.getColumnValues(SEATS).get(subscriptionIndex);
        final String offerExtKeyInReport = drMigrationReportGrid.getColumnValues(OFFER).get(subscriptionIndex);
        final String offerTermInReport = drMigrationReportGrid.getColumnValues(OFFER_TERM).get(subscriptionIndex);
        final String userName = getUserName(drMigrationReportGrid.getColumnValues(USER).get(subscriptionIndex));
        final String storeInReport = drMigrationReportGrid.getColumnValues(STORE).get(subscriptionIndex);

        AssertCollector.assertThat("All Subscriptions don't have status with Pending Migration",
            drMigrationReportGrid.getColumnValues(STATUS), everyItem(equalTo(Status.PENDING_MIGRATION.toString())),
            assertionErrorList);
        // opening Detail page of selected subscription
        final GenericDetails subscriptionDetails = drMigrationReportGrid.selectResultRow(subscriptionIndex + 1);

        AssertCollector.assertThat("ID is not same as shown in Report", subscriptionDetails.getValueByField("ID"),
            equalTo(subIdInReport), assertionErrorList);
        if (externalKeyInReport.equals("-")) {
            externalKeyInReport = null;
        }
        AssertCollector.assertThat("External Key is not same as shown in Report",
            subscriptionDetails.getValueByField("External Key"), equalTo(externalKeyInReport), assertionErrorList);
        AssertCollector.assertThat("Status is not same as shown in Report",
            subscriptionDetails.getValueByField("Status"), equalTo(subStatusInReport), assertionErrorList);
        AssertCollector.assertThat("Seats are not same as shown in Report",
            subscriptionDetails.getValueByField("Quantity"), equalTo(seatsInReport), assertionErrorList);
        AssertCollector.assertThat("Quantity is not same as shown in Report",
            subscriptionDetails.getValueByField("Quantity"), equalTo(seatsInReport), assertionErrorList);
        AssertCollector.assertThat("User is not same as shown in Report", subscriptionDetails.getValueByField("User"),
            equalTo(userName), assertionErrorList);
        AssertCollector.assertThat("Next Billing Date is not null for Pending Migration Subscription",
            subscriptionDetails.getValueByField("Next Billing Date"), equalTo(null), assertionErrorList);

        assertionsForStoreAndOfferThroughApi(subIdInReport, offerTermInReport, storeInReport, offerExtKeyInReport,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the report should be generated without selecting any Store
     *
     * @result Report should be opened for selected store
     */
    @Test
    public void testDRMigrationCustomerReportWithSingleStore() {
        final int subscriptionIndex = 0;
        final PendingMigrationSubscriptionsReportPage drMigrationReport =
            adminToolPage.getPage(PendingMigrationSubscriptionsReportPage.class);
        drMigrationReport.navigateToDrMigrationReportPage();
        drMigrationReport.selectStore(storeName);
        drMigrationReport.submit(TimeConstants.ONE_SEC);
        final GenericGrid drMigrationReportGrid = adminToolPage.getPage(GenericGrid.class);

        final String subIdInReport = drMigrationReportGrid.getColumnValues(SUB_ID).get(subscriptionIndex);
        String externalKeyInReport = drMigrationReportGrid.getColumnValues(EXT_KEY).get(subscriptionIndex);
        final String subStatusInReport = drMigrationReportGrid.getColumnValues(STATUS).get(subscriptionIndex);
        final String seatsInReport = drMigrationReportGrid.getColumnValues(SEATS).get(subscriptionIndex);
        final String offerExtKeyInReport = drMigrationReportGrid.getColumnValues(OFFER).get(subscriptionIndex);
        final String offerTermInReport = drMigrationReportGrid.getColumnValues(OFFER_TERM).get(subscriptionIndex);
        final String userName = getUserName(drMigrationReportGrid.getColumnValues(USER).get(subscriptionIndex));

        final String storeInReport = drMigrationReportGrid.getColumnValues(STORE).get(subscriptionIndex);

        AssertCollector.assertThat("Subscription doesn't has status with Pending Migration",
            drMigrationReportGrid.getColumnValues(STATUS), everyItem(equalTo(Status.PENDING_MIGRATION.toString())),
            assertionErrorList);

        final GenericDetails subscriptionDetails = drMigrationReportGrid.selectResultRow(subscriptionIndex + 1);

        AssertCollector.assertThat("ID is not same as shown in Report", subscriptionDetails.getValueByField("ID"),
            equalTo(subIdInReport), assertionErrorList);
        if (externalKeyInReport.equals("-")) {
            externalKeyInReport = null;
        }
        AssertCollector.assertThat("External Key is not same as shown in Report",
            subscriptionDetails.getValueByField("External Key"), equalTo(externalKeyInReport), assertionErrorList);
        AssertCollector.assertThat("Status is not same as shown in Report",
            subscriptionDetails.getValueByField("Status"), equalTo(subStatusInReport), assertionErrorList);
        AssertCollector.assertThat("Seats are not same as shown in Report",
            subscriptionDetails.getValueByField("Quantity"), equalTo(seatsInReport), assertionErrorList);
        AssertCollector.assertThat("User is not same as shown in Report", subscriptionDetails.getValueByField("User"),
            equalTo(userName), assertionErrorList);
        AssertCollector.assertThat("Next Billing Date is not null for Pending Migration Subscription",
            subscriptionDetails.getValueByField("Next Billing Date"), equalTo(null), assertionErrorList);
        assertionsForStoreAndOfferThroughApi(subIdInReport, offerTermInReport, storeInReport, offerExtKeyInReport,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verify subscription id, offer term, store and offer external key through find subscription by id
     * api and compare it with a report.
     *
     * @param assertionErrorList
     */
    private static void assertionsForStoreAndOfferThroughApi(final String subIdInReport, final String offerTermInReport,
        final String storeInReport, final String offerExtKeyInReport, final List<AssertionError> assertionErrorList) {
        final Subscription subscription = resource.subscription().getById(subIdInReport);
        final String storeIdInSubscription = subscription.getPrice().getStoreExternalKey();
        final String offerTermInSubscription = subscription.getBillingOption().getBillingPeriod().getCount() + " "
            + subscription.getBillingOption().getBillingPeriod().getType();
        final String offerExternalKeyInSubscription = subscription.getCurrentOffer().getExternalKey();
        AssertCollector.assertThat("Offer Term is not same as shown in Report", offerTermInSubscription,
            equalTo(offerTermInReport), assertionErrorList);
        AssertCollector.assertThat("Store is not same as shown in Report", storeIdInSubscription,
            equalTo(storeInReport), assertionErrorList);
        AssertCollector.assertThat("External Key of Offer is not same as shown in Report",
            offerExternalKeyInSubscription, equalTo(offerExtKeyInReport), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to get the subscription external key
     *
     * @return string
     */
    private String getSubscriptionExtKey(final String subscriptionId) {
        return "MIG-" + subscriptionId;
    }

    private String getUserName(final String userExternalKey) {
        return DbUtils.selectQuery("select Name from named_party where XKEY ='" + userExternalKey + "'",
            getEnvironmentVariables()).get(0).get("NAME");
    }

}
