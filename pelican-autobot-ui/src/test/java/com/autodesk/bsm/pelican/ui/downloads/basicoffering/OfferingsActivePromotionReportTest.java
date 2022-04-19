package com.autodesk.bsm.pelican.ui.downloads.basicoffering;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PromotionUserReportConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.OfferingsActivePromotionReportPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Admin Tool's Offerings with Active Promotion Reports. On Admin Tool's Main Tab navigate to Promotion Reports ->
 * Offerings with Active Promotion Report
 *
 * @author sunitha
 */
public class OfferingsActivePromotionReportTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static final String ACTUAL_FILE_NAME = "OfferingsPromotionReport.xlsx";

    /**
     * Data setup - create user for each test!
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
    }

    /**
     * Test case to verify Offerings with active Promotion report headers.
     */
    @Test
    public void testOfferingsActivePromotionReportHeaders() {
        final OfferingsActivePromotionReportPage reportPage =
            adminToolPage.getPage(OfferingsActivePromotionReportPage.class);
        reportPage.submit(TimeConstants.ONE_SEC);

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);
        // Validate admin tool's page title.
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - Offerings Promotion Report"), assertionErrorList);
        // Validate Admin tool's header.
        AssertCollector.assertThat("Admin tool: Incorrect page header", grid.getHeader(),
            equalTo("Offerings with Active Promotion Report"), assertionErrorList);

        // Validate column headers.
        final List<String> columnHeaders = grid.getColumnHeaders();
        AssertCollector.assertTrue("Offering External key not found", columnHeaders.contains("Offering External Key"),
            assertionErrorList);
        AssertCollector.assertTrue("Offering Name not found", columnHeaders.contains("Offering Name"),
            assertionErrorList);
        AssertCollector.assertTrue("Offering Type not found", columnHeaders.contains("Offering Type"),
            assertionErrorList);
        AssertCollector.assertTrue("Offering Billing Term not found", columnHeaders.contains("Offering Billing Term"),
            assertionErrorList);
        AssertCollector.assertTrue("Promotion not found", columnHeaders.contains("Promotion"), assertionErrorList);
        AssertCollector.assertTrue("Promotion Code header not found", columnHeaders.contains("Promotion Code"),
            assertionErrorList);
        AssertCollector.assertTrue("Promotion Store header not found", columnHeaders.contains("Promotion Store"),
            assertionErrorList);
        AssertCollector.assertTrue("Store Wide header not found", columnHeaders.contains("Store Wide"),
            assertionErrorList);
        AssertCollector.assertTrue("Promotion Type header not found", columnHeaders.contains("Promotion Type"),
            assertionErrorList);
        AssertCollector.assertTrue("Promotion Subtype not found", columnHeaders.contains("Promotion Subtype"),
            assertionErrorList);
        AssertCollector.assertTrue("Price Id header not found", columnHeaders.contains("Price Id"), assertionErrorList);
        AssertCollector.assertTrue("Price Amount header not found", columnHeaders.contains("Price Amount"),
            assertionErrorList);
        AssertCollector.assertTrue("Promo Discount Applied header not found",
            columnHeaders.contains("Promo Discount Applied"), assertionErrorList);
        AssertCollector.assertTrue("Discounted Price header not found", columnHeaders.contains("Discounted Price"),
            assertionErrorList);
        AssertCollector.assertTrue("Bundled header not found", columnHeaders.contains("Bundled"), assertionErrorList);

        // Validate some data is returned.
        AssertCollector.assertThat("Admin Tool: Got no data", grid.getTotalItems(), greaterThanOrEqualTo(1),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test case to verify Offerings with Active Promotion Report.
     */
    @Test
    public void testOfferingsActivePromotionReportWithActivePromotion() {
        final OfferingsActivePromotionReportPage reportPage =
            adminToolPage.getPage(OfferingsActivePromotionReportPage.class);
        reportPage.submit(TimeConstants.ONE_SEC);

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);
        // Validate admin tool's page title.
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - Offerings Promotion Report"), assertionErrorList);
        // Validate Admin tool's header.
        AssertCollector.assertThat("Admin tool: Incorrect page header", grid.getHeader(),
            equalTo("Offerings with Active Promotion Report"), assertionErrorList);

        // Validate pagination (i.e. results per page).
        AssertCollector.assertThat("Incorrect number of results per page",
            grid.getColumnValues("Offering External Key").size(), lessThanOrEqualTo(40), assertionErrorList);

        // Validate Offering External key.
        for (final String offeringExternalKey : grid.getColumnValues("Offering External Key")) {
            AssertCollector.assertThat("Incorrect Offering External Key", offeringExternalKey, is(notNullValue()),
                assertionErrorList);
        }

        // Validate Offering Name.
        for (final String offeringName : grid.getColumnValues("Offering Name")) {
            AssertCollector.assertThat("Incorrect Offering Name", offeringName, is(notNullValue()), assertionErrorList);
        }

        // Validate Offering Type.
        final List<String> offeringType = grid.getColumnValues("Offering Type");
        for (final String type : offeringType) {
            AssertCollector.assertThat("Incorrect Offering Type", type.matches("Basic Offering|Subscription Offer"),
                equalTo(true), assertionErrorList);
        }

        // Validate Offering Billing Term.
        final List<String> offeringBillingPlan = grid.getColumnValues("Offering Billing Term");
        for (int i = 0; i < offeringBillingPlan.size(); i++) {
            AssertCollector.assertThat("Incorrect Offering Billing Term for record number " + i,
                offeringBillingPlan.get(i)
                    .matches(PelicanConstants.NOT_APPLICABLE + "|" + PelicanConstants.ONCE + "|"
                        + PelicanConstants.EVERY_MONTH + "|" + PelicanConstants.EVERY_2_MONTHS + "|"
                        + PelicanConstants.EVERY_3_MONTHS + "|" + PelicanConstants.EVERY_6_MONTHS + "|"
                        + PelicanConstants.EVERY_YEAR + "|" + PelicanConstants.EVERY_2_YEARS + "|"
                        + PelicanConstants.EVERY_3_YEARS + "|" + PelicanConstants.EVERY_4_YEARS),
                equalTo(true), assertionErrorList);
        }

        // Validate Promotion.
        for (final String promotion : grid.getColumnValues("Promotion")) {
            AssertCollector.assertThat("Incorrect Promotion", promotion, is(notNullValue()), assertionErrorList);
        }

        // Validate Promotion Code.
        for (final String promotionCode : grid.getColumnValues("Promotion Code")) {
            AssertCollector.assertThat("Incorrect Promotion Code", promotionCode, is(notNullValue()),
                assertionErrorList);
        }

        // Validate Promotion Store
        for (final String promotionStore : grid.getColumnValues("Promotion Store")) {
            AssertCollector.assertThat("Incorrect Promotion Store", promotionStore, is(notNullValue()),
                assertionErrorList);
        }

        // Validate Store Wide.
        final List<String> actualOfferingStatus = grid.getColumnValues("Store Wide");
        for (final String status : actualOfferingStatus) {
            AssertCollector.assertThat("Incorrect Store Wide", status.matches("No|Yes"), equalTo(true),
                assertionErrorList);
        }

        // Validate Promotion Type.
        final List<String> promotionType = grid.getColumnValues("Promotion Type");
        for (final String promoType : promotionType) {
            AssertCollector.assertThat("Incorrect Promotion Type", promoType.matches("Discount|Supplement"),
                equalTo(true), assertionErrorList);
        }

        // Validate Promotion Subtype.
        final List<String> promotionSubType = grid.getColumnValues("Promotion Subtype");
        for (final String subType : promotionSubType) {
            AssertCollector.assertThat("Incorrect Promotion Type",
                subType.matches("Discount Percentage|Discount Amount|Supplement Time"), equalTo(true),
                assertionErrorList);
        }

        // Validate Price Id.
        for (final String priceId : grid.getColumnValues("Price Id")) {
            AssertCollector.assertThat("Incorrect Price Id", priceId, is(notNullValue()), assertionErrorList);
        }

        // Validate Price Amount.
        for (final String priceAmount : grid.getColumnValues("Price Amount")) {
            AssertCollector.assertThat("Incorrect Price Amount", priceAmount, is(notNullValue()), assertionErrorList);
        }

        // Validate Promo Discount Applied.
        for (final String promoDiscountApplied : grid.getColumnValues("Promo Discount Applied")) {
            AssertCollector.assertThat("Incorrect Promo Discount Applied", promoDiscountApplied, is(notNullValue()),
                assertionErrorList);
        }

        // Validate Discounted Price.
        for (final String discountPrice : grid.getColumnValues("Discounted Price")) {
            AssertCollector.assertThat("Incorrect Discounted Price data", discountPrice, is(notNullValue()),
                assertionErrorList);
        }

        // Validate Bundled.
        final List<String> actualBundled = grid.getColumnValues("Bundled");
        for (final String bundled : actualBundled) {
            AssertCollector.assertThat("Incorrect Bundled", bundled.matches("No|Yes"), equalTo(true),
                assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test case to Verify whether the report consists of offerings with active promotion or not.
     */
    @Test
    public void testOfferingsActivePromotionReportConsistsOfferingsWithActivePromotion() {
        final OfferingsActivePromotionReportPage reportPage =
            adminToolPage.getPage(OfferingsActivePromotionReportPage.class);
        reportPage.submit(TimeConstants.ONE_SEC);

        // Here sleep is required till the active promotion is generated.
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // Click on first Active Promotion.
        reportPage.clickOnFirstActivePromotion();

        // Sleep is required for loading promotion details page ,if removed test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);
        // Validate admin tool's page title.
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(), equalTo("Pelican - Promotion Detail"),
            assertionErrorList);
        // Validate Admin tool's header.
        AssertCollector.assertThat("Admin tool: Incorrect page header", grid.getHeader(), equalTo("Promotion Detail"),
            assertionErrorList);

        // Verify 'Active' state for Promotion.
        final GenericDetails details = adminToolPage.getPage(GenericDetails.class);
        final String status = details.getValueByField("State");
        AssertCollector.assertThat("Active status is not matched", status, equalTo(Status.ACTIVE.toString()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all headers of columns in download Report.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testOfferingsWithActivePromotionReportHeaderInDownloadFile() throws IOException {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        final OfferingsActivePromotionReportPage reportPage =
            adminToolPage.getPage(OfferingsActivePromotionReportPage.class);
        reportPage.selectviewDownload(PelicanConstants.DOWNLOAD);
        reportPage.submit(TimeConstants.ONE_SEC);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length,
            equalTo(PromotionUserReportConstants.TOTAL_COLUMNS_OFFERING_WITH_ACTIVE_PROMOTION_REPORT),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0],
            equalTo(PromotionUserReportConstants.OFFERING_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1],
            equalTo(PromotionUserReportConstants.OFFERING_NAME), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2],
            equalTo(PromotionUserReportConstants.OFFERING_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3],
            equalTo(PromotionUserReportConstants.OFFERING_BILLING_TERM), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", fileData[0][4],
            equalTo(PromotionUserReportConstants.PROMOTION_NAME), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", fileData[0][5],
            equalTo(PromotionUserReportConstants.PROMOTION_CODE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", fileData[0][6],
            equalTo(PromotionUserReportConstants.PROMOTION_STORE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", fileData[0][7],
            equalTo(PromotionUserReportConstants.STORE_WIDE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", fileData[0][8],
            equalTo(PromotionUserReportConstants.PROMOTION_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 10", fileData[0][9],
            equalTo(PromotionUserReportConstants.PROMOTION_SUB_TYPE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 11", fileData[0][10],
            equalTo(PromotionUserReportConstants.PRICE_ID), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 12", fileData[0][11],
            equalTo(PromotionUserReportConstants.PRICE_AMOUTN), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 13", fileData[0][12],
            equalTo(PromotionUserReportConstants.PROMO_DISCOUNT_APPLIED), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 14", fileData[0][13],
            equalTo(PromotionUserReportConstants.DISCOUNTED_PRICE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 15", fileData[0][14],
            equalTo(PromotionUserReportConstants.BUNDLED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
