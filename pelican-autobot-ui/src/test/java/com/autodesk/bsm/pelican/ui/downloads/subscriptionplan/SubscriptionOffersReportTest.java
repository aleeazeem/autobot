package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionOffersReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.core.Every;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin Tool's Subscription Offers Reports tests. This page can be navigated from Reports -> Subscription Offers Report
 *
 * @author jains
 */
public class SubscriptionOffersReportTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static final String DOWNLOAD_FILE_NAME = "SubscriptionOffersReport.xlsx";
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOffersReportTest.class.getSimpleName());
    private SubscriptionOffersReportPage subscriptionOffersReportPage;
    private SubscriptionOffersReportResultPage subscriptionOffersReportResultPage;
    private List<String> expectedOfferStatusList;
    private List<String> expectedUsageTypeList;
    private static final String SUBSCRIPTION_OFFER_ORIGINAL_PRICE = "40";
    private String productLineForView = null;
    private String productLineForDownload = null;
    private String subscriptionPlanIdForView = null;
    private String subscriptionPlanIdForDownload = null;
    private SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private EditSubscriptionPlanPage editSubscriptionPlanPage;
    private FindSubscriptionPlanPage findSubscriptionPlanPage;

    /**
     * Data setup - create Product Lines if not available
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        subscriptionOffersReportPage = adminToolPage.getPage(SubscriptionOffersReportPage.class);
        subscriptionOffersReportResultPage = adminToolPage.getPage(SubscriptionOffersReportResultPage.class);
        expectedOfferStatusList = new ArrayList<>();
        expectedUsageTypeList = new ArrayList<>();

        final AddSubscriptionPlanPage addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        final SubscriptionPlanApiUtils subscriptionPlanUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        String productLineName = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        subscriptionPlanUtils.addProductLine(productLineName);
        productLineForView = productLineName + " (" + productLineName + ")";

        // Add subscription Plan with IC for View test cases
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineForView, SupportLevel.BASIC, PackagingType.INDUSTRY_COLLECTION, true);

        String subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer
        addSubscriptionPlan.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 1, true);

        String priceStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        String priceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 180);

        // Add Subscription Price
        addSubscriptionPlan.addPricesInOffer(1, getStoreUs().getName(), getPricelistExternalKeyUs(),
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, priceStartDate, priceEndDate);

        // Click on Save
        SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlan.clickOnSave(false);
        subscriptionPlanIdForView = subscriptionPlanDetailPage.getId();

        // Add another subscription Plan with VG for Download test cases, as we run tests in parallel we need similar
        // plan
        subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        productLineName = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        subscriptionPlanUtils.addProductLine(productLineName);
        productLineForDownload = productLineName + " (" + productLineName + ")";

        // Add subscription Plan with VG packaging type
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineForDownload, SupportLevel.BASIC, PackagingType.VERTICAL_GROUPING, true);

        subscriptionOfferExternalKey = RandomStringUtils.randomAlphabetic(7);

        // Add Subscription Offer
        addSubscriptionPlan.addOffer(subscriptionOfferExternalKey, null, Status.ACTIVE, true, null,
            BillingFrequency.MONTH, 1, false);

        priceStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        priceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 180);

        // Add Subscription Price
        addSubscriptionPlan.addPricesInOffer(1, getStoreUs().getName(), getPricelistExternalKeyUs(),
            SUBSCRIPTION_OFFER_ORIGINAL_PRICE, priceStartDate, priceEndDate);

        // Click on Save
        subscriptionPlanDetailPage = addSubscriptionPlan.clickOnSave(false);
        subscriptionPlanIdForDownload = subscriptionPlanDetailPage.getId();

    }

    /**
     * Verify Subscription Offers report without any status filter set, throws an error message.
     */
    @Test
    public void testSubscriptionOffersReportErrorMessageForNonSelectionOfStatus() {
        subscriptionOffersReportPage =
            subscriptionOffersReportPage.getReportWithSelectedFiltersError(PelicanConstants.APPLICATION_FAMILY_NAME,
                null, false, false, false, true, true, true, true, true, 0, 0, false, false, PelicanConstants.VIEW);

        AssertCollector.assertThat("Error message for selecting at lease one status checkbox is not correct. ",
            subscriptionOffersReportPage.getError(), equalTo(PelicanErrorConstants.AT_LEAST_ONE_STATUS_ERROR_MESSAGE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify Subscription Offers report without any usage type filter set, throws an error message.
     */
    @Test
    public void testSubscriptionOffersReportErrorMessageForNonSelectionOfUsageType() {
        subscriptionOffersReportPage =
            subscriptionOffersReportPage.getReportWithSelectedFiltersError(PelicanConstants.APPLICATION_FAMILY_NAME,
                null, true, false, false, false, false, false, false, false, 0, 0, false, false, PelicanConstants.VIEW);

        AssertCollector.assertThat("Error message for selecting at least one usage type checkbox is not correct. ",
            subscriptionOffersReportPage.getError(),
            equalTo(PelicanErrorConstants.AT_LEAST_ONE_USAGE_TYPE_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify subscription offer report with different filters on download.
     *
     * @param productLineIndex
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @param storeType
     * @param store
     * @param isIncludePlanData
     * @param isIncludePrices
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test(dataProvider = "dataForSubscriptionOffersReport")
    public void testSubscriptionOffersReportWithSelectedFiltersDownload(final String productLine,
        final boolean isOfferStatusNew, final boolean isOfferStatusActive, final boolean isOfferStatusCanceled,
        final boolean isUsageTypeCommercial, final boolean isUsageTypeEducation, final boolean isUsageTypeNonCommercial,
        final boolean isUsageTypeTrial, final boolean isUsageTypeGovernment, final int storeTypeIndex,
        final int storeIndex, final boolean isIncludePlanData, final boolean isIncludePrices) throws IOException {

        // clean up existing xls file before downloading
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);
        // Download report with selected filters
        subscriptionOffersReportPage = adminToolPage.getPage(SubscriptionOffersReportPage.class);

        subscriptionOffersReportResultPage = subscriptionOffersReportPage.getReportWithSelectedFilters(productLine,
            isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled, isUsageTypeCommercial, isUsageTypeEducation,
            isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment, storeTypeIndex, storeIndex,
            isIncludePlanData, isIncludePrices, PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;

        final int totalNumberOfRowsInReport = XlsUtils.getNumRowsInXlsx(fileName);
        LOGGER.info("Total number of records in the report " + String.valueOf(totalNumberOfRowsInReport - 1));

        final List<String> productLineColumnValues = new ArrayList<>();
        final List<String> usageTypeColumnValues = new ArrayList<>();
        final List<String> offerStatusColumnValues = new ArrayList<>();

        if (totalNumberOfRowsInReport > 1) {
            // i=1, since first row is header in the report.
            for (int i = 1; i < totalNumberOfRowsInReport; i++) {
                // If includePlanData is checked then offerStatus will be at 13th column of the report
                // otherwise it will
                // be at 5th column.
                if (isIncludePlanData) {
                    offerStatusColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 13)));
                } else {
                    offerStatusColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 4)));
                }
                if (isIncludePlanData) {
                    if (productLine != null) {
                        productLineColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 2)));
                        AssertCollector.assertThat("Product line is not correct", productLineColumnValues,
                            Every.everyItem(
                                equalTo(subscriptionOffersReportPage.getSelectedProductLine().split(" \\(")[0])),
                            assertionErrorList);

                    }
                    usageTypeColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 7)));
                }
            }

            // validate product line
            if (productLine != null && isIncludePlanData) {
                AssertCollector.assertThat("Product line is not correct", productLineColumnValues,
                    Every.everyItem(equalTo(subscriptionOffersReportPage.getSelectedProductLine().split(" \\(")[0])),
                    assertionErrorList);
            }

            // get expected offer list and validate against that list.
            expectedOfferStatusList =
                getExpectedOfferStatusList(isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled);

            AssertCollector.assertThat("Offer status is not correct", offerStatusColumnValues,
                everyItem(isIn(expectedOfferStatusList)), assertionErrorList);

            expectedUsageTypeList = getExpectedUsageTypeList(isUsageTypeCommercial, isUsageTypeEducation,
                isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment);

            if (isIncludePlanData) {
                AssertCollector.assertThat("Usage type is not correct", usageTypeColumnValues,
                    everyItem(isIn(expectedUsageTypeList)), assertionErrorList);
            }

        } else {
            Assert.fail("Report is empty for selected filters.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to verify subscription offer report with different filters on view.
     *
     * @param productLineIndex
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @param storeType
     * @param store
     * @param isIncludePlanData
     * @param isIncludePrices
     */
    @Test(dataProvider = "dataForSubscriptionOffersReport")
    public void testSubscriptionOffersReportWithSelectedFiltersView(final String productLine,
        final boolean isOfferStatusNew, final boolean isOfferStatusActive, final boolean isOfferStatusCanceled,
        final boolean isUsageTypeCommercial, final boolean isUsageTypeEducation, final boolean isUsageTypeNonCommercial,
        final boolean isUsageTypeTrial, final boolean isUsageTypeGovernment, final int storeTypeIndex,
        final int storeIndex, final boolean isIncludePlanData, final boolean isIncludePrices) {

        subscriptionOffersReportResultPage = subscriptionOffersReportPage.getReportWithSelectedFilters(productLine,
            isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled, isUsageTypeCommercial, isUsageTypeEducation,
            isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment, storeTypeIndex, storeIndex,
            isIncludePlanData, isIncludePrices, PelicanConstants.VIEW);

        String expectedHeader;
        // verify report headers
        if (isIncludePlanData && isIncludePrices) {
            expectedHeader = "PlanExternalKey,PlanName,PlanProductLine,PlanOfferingType,PlanStatus,PlanPackagingType,"
                + "PlanSupportLevel,PlanUsageType,PlanCancellationPolicy,PlanOfferingDetailName,PlanTaxCode,"
                + "OfferExternalKey,OfferName,OfferStatus,OfferFrequency,Amount,Currency,PriceList,Store,"
                + "StoreType,PriceStartDate,PriceEndDate,PriceId";
        } else if (isIncludePlanData) {
            expectedHeader = "PlanExternalKey,PlanName,PlanProductLine,PlanOfferingType,PlanStatus,PlanPackagingType,"
                + "PlanSupportLevel,PlanUsageType,PlanCancellationPolicy,PlanOfferingDetailName,PlanTaxCode,"
                + "OfferExternalKey,OfferName,OfferStatus,OfferFrequency";
        } else if (isIncludePrices) {
            expectedHeader = "PlanExternalKey,PlanName,OfferExternalKey,OfferName,OfferStatus,OfferFrequency,"
                + "Amount,Currency,PriceList,Store,StoreType,PriceStartDate,PriceEndDate,PriceId";
        } else {
            expectedHeader = "PlanExternalKey,PlanName,OfferExternalKey,OfferName,OfferStatus,OfferFrequency";
        }
        AssertCollector.assertThat("Report headers are not correct",
            subscriptionOffersReportResultPage.getReportHeadersLine(), equalTo(expectedHeader), assertionErrorList);

        if (subscriptionOffersReportResultPage.getReportData().size() > 0) {
            if (productLine != null && isIncludePlanData) {
                AssertCollector.assertThat("Product line is not correct",
                    subscriptionOffersReportResultPage.getValuesFromPlanProductLineColumn(),
                    Every.everyItem(equalTo(subscriptionOffersReportPage.getSelectedProductLine().split(" \\(")[0])),
                    assertionErrorList);
            }

            // get expected offer list and validate against that list.
            expectedOfferStatusList =
                getExpectedOfferStatusList(isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled);
            AssertCollector.assertThat("Offer status is not correct",
                subscriptionOffersReportResultPage.getValuesFromOfferStatusColumn(),
                everyItem(isIn(expectedOfferStatusList)), assertionErrorList);

            expectedUsageTypeList = getExpectedUsageTypeList(isUsageTypeCommercial, isUsageTypeEducation,
                isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment);
            if (isIncludePlanData) {
                AssertCollector.assertThat("Usage type is not correct",
                    subscriptionOffersReportResultPage.getValuesFromPlanUsageTypeColumn(),
                    everyItem(isIn(expectedUsageTypeList)), assertionErrorList);
            }

            // Verify price columns
            if (isIncludePrices) {
                try {
                    subscriptionOffersReportResultPage.getValuesFromAmountColumn();
                    subscriptionOffersReportResultPage.getValuesFromCurrencyColumn();
                    subscriptionOffersReportResultPage.getValuesFromPriceListColumn();
                    subscriptionOffersReportResultPage.getValuesFromStoreColumn();
                    subscriptionOffersReportResultPage.getValuesFromStoreTypeColumn();
                    subscriptionOffersReportResultPage.getValuesFromPriceStartDateColumn();
                    subscriptionOffersReportResultPage.getValuesFromPriceEndDateColumn();
                    subscriptionOffersReportResultPage.getValuesFromPriceIdColumn();
                } catch (final Exception ex) {
                    Assert.fail("All price related columns are not included in the report.");
                    ex.getStackTrace();
                    LOGGER.info("stack trace " + Arrays.toString(ex.getStackTrace()));
                }
            }

            if (isIncludePlanData) {
                try {
                    subscriptionOffersReportResultPage.getValuesFromPlanProductLineColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanOfferingTypeColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanStatusColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanSupportLevelColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanUsageTypeColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanCancellationPolicyColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanOfferingDetailNameColumn();
                    subscriptionOffersReportResultPage.getValuesFromPlanTaxCodeColumn();
                } catch (final Exception ex) {
                    Assert.fail("All include plan data related columns are not included in the report.");
                }
            }

        } else {
            Assert.fail("Report is empty for selected filters.");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test to verify subscription offer report for PackagingType with IC/None on view.
     *
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @param storeType
     * @param store
     * @param isIncludePlanData
     * @param isIncludePrices
     * @param hasPackagingType
     */
    @Test(dataProvider = "dataForPackagingTypeDataInReport")
    public void testSubscriptionOffersReportWithAndWithOutPackagingTypeforView(final boolean isOfferStatusNew,
        final boolean isOfferStatusActive, final boolean isOfferStatusCanceled, final boolean isUsageTypeCommercial,
        final boolean isUsageTypeEducation, final boolean isUsageTypeNonCommercial, final boolean isUsageTypeTrial,
        final boolean isUsageTypeGovernment, final int storeTypeIndex, final int storeIndex,
        final boolean isIncludePlanData, final boolean isIncludePrices, final boolean hasPackagingType) {

        subscriptionOffersReportResultPage = subscriptionOffersReportPage.getReportWithSelectedFilters(
            productLineForView, isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled, isUsageTypeCommercial,
            isUsageTypeEducation, isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment, storeTypeIndex,
            storeIndex, isIncludePlanData, isIncludePrices, PelicanConstants.VIEW);

        try {
            if (hasPackagingType) {
                AssertCollector.assertThat("Plan Packaging Type value didnt match in report",
                    subscriptionOffersReportResultPage.getValuesFromPlanPackagingTypeColumn().get(0),
                    equalTo(PackagingType.INDUSTRY_COLLECTION.getDisplayName()), assertionErrorList);
            } else {
                AssertCollector.assertThat("Plan Packaging Type value didnt match in report",
                    subscriptionOffersReportResultPage.getValuesFromPlanPackagingTypeColumn().get(0),
                    equalTo(PelicanConstants.EMPTY_VALUE), assertionErrorList);
            }
        } finally {

            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(subscriptionPlanIdForView);
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
            if (hasPackagingType) {
                editSubscriptionPlanPage.editPackagingType(PackagingType.NONE);

            } else {
                editSubscriptionPlanPage.editPackagingType(PackagingType.INDUSTRY_COLLECTION);
            }
            editSubscriptionPlanPage.clickOnSave(false);
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Test to verify subscription offer report for PackagingType with VG/None on Download.
     *
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @param storeType
     * @param store
     * @param isIncludePlanData
     * @param isIncludePrices
     * @param hasPackagingType
     */
    @Test(dataProvider = "dataForPackagingTypeDataInReport")
    public void testSubscriptionOffersReportWithAndWithOutPackagingTypeForDownload(final boolean isOfferStatusNew,
        final boolean isOfferStatusActive, final boolean isOfferStatusCanceled, final boolean isUsageTypeCommercial,
        final boolean isUsageTypeEducation, final boolean isUsageTypeNonCommercial, final boolean isUsageTypeTrial,
        final boolean isUsageTypeGovernment, final int storeTypeIndex, final int storeIndex,
        final boolean isIncludePlanData, final boolean isIncludePrices, final boolean hasPackagingType)
        throws IOException {

        // clean up existing xls file before downloading
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);
        // Download report with selected filters
        subscriptionOffersReportPage = adminToolPage.getPage(SubscriptionOffersReportPage.class);

        subscriptionOffersReportResultPage = subscriptionOffersReportPage.getReportWithSelectedFilters(
            productLineForDownload, isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled, isUsageTypeCommercial,
            isUsageTypeEducation, isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment, storeTypeIndex,
            storeIndex, isIncludePlanData, isIncludePrices, PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;

        final int totalNumberOfRowsInReport = XlsUtils.getNumRowsInXlsx(fileName);
        LOGGER.info("Total number of records in the report " + String.valueOf(totalNumberOfRowsInReport - 1));

        if (totalNumberOfRowsInReport > 1) {
            // i=1, since first row is header in the report.
            for (int i = 1; i < totalNumberOfRowsInReport; i++) {
                // If includePlanData is checked then PlanPackaingType will be at 5th column of the report
                try {
                    if (hasPackagingType) {
                        AssertCollector.assertThat("Plan Packaging Type value didnt match in report",
                            XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 5)),
                            equalTo(PackagingType.VERTICAL_GROUPING.getDisplayName()), assertionErrorList);
                    } else {
                        AssertCollector.assertThat("Plan Packaging Type value didnt match in report",
                            XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 5)),
                            equalTo(PelicanConstants.EMPTY_VALUE), assertionErrorList);
                    }
                } catch (final IOException e) {
                    Assert.fail("Failed while fetching PackagingType column from Download Xls ");
                } finally {
                    subscriptionPlanDetailPage =
                        findSubscriptionPlanPage.findSubscriptionPlanById(subscriptionPlanIdForDownload);
                    subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
                    if (hasPackagingType) {
                        editSubscriptionPlanPage.editPackagingType(PackagingType.NONE);

                    } else {
                        editSubscriptionPlanPage.editPackagingType(PackagingType.VERTICAL_GROUPING);
                    }
                    editSubscriptionPlanPage.clickOnSave(false);
                }
            }
        } else {
            Assert.fail("Report is empty for selected filters.");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Data provider with different filters
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForSubscriptionOffersReport")
    public Object[][] getTestDataForAccessReviewReport() {
        final String productLine = getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")";
        return new Object[][] {
                // productLine, isOfferStatusNew, isOfferStatusActive, isOfferStatusCanceled,
                // isUsageTypeCommercial,
                // isUsageTypeEducation, isUsageTypeNonCommercial, isUsageTypeTrial, isUsageTypeGovernment,
                // storeType,
                // store, isIncludePlanData, isIncludePrices

                { productLine, false, true, false, true, false, false, false, false, 0, 0, true, false },
                { null, true, false, false, true, false, true, false, false, 0, 0, false, false },
                { productLine, true, true, true, true, false, false, true, false, 0, 0, true, false },
                { productLine, false, true, false, true, false, false, false, false, 0, 0, true, false },
                { null, false, true, false, true, false, false, false, false, 1, 1, true, false }, };
    }

    /**
     * SubscriptionOffer report Data provider for Packaging Type
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForPackagingTypeDataInReport")
    public Object[][] getTestDataForPackagingTypeReport() {
        return new Object[][] { { false, true, false, true, false, false, false, false, 0, 0, true, false, true },
                { false, true, false, true, false, false, false, false, 0, 0, true, false, false } };
    }

    /**
     * Method to get a list of all selected offer status.
     *
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @return List<String>
     */
    private List<String> getExpectedOfferStatusList(final boolean isOfferStatusNew, final boolean isOfferStatusActive,
        final boolean isOfferStatusCanceled) {
        // empty list before adding any element
        expectedOfferStatusList.clear();
        if (isOfferStatusNew) {
            expectedOfferStatusList.add(Status.NEW.getDisplayName());
        }
        if (isOfferStatusActive) {
            expectedOfferStatusList.add(Status.ACTIVE.getDisplayName());
        }
        if (isOfferStatusCanceled) {
            expectedOfferStatusList.add(Status.CANCELED.getDisplayName());
        }

        return expectedOfferStatusList;
    }

    /**
     * Method to get a list of all selected usage type.
     *
     * @param isUsageTypeCommercial
     * @param isUsageTypeEducation
     * @param isUsageTypeNonCommercial
     * @param isUsageTypeTrial
     * @param isUsageTypeGovernment
     * @return List<String>
     */
    private List<String> getExpectedUsageTypeList(final boolean isUsageTypeCommercial,
        final boolean isUsageTypeEducation, final boolean isUsageTypeNonCommercial, final boolean isUsageTypeTrial,
        final boolean isUsageTypeGovernment) {
        // Validate usage type

        if (isUsageTypeCommercial) {
            expectedUsageTypeList.add(UsageType.COM.getDisplayName());
        }
        if (isUsageTypeEducation) {
            expectedUsageTypeList.add(UsageType.EDU.getDisplayName());
        }
        if (isUsageTypeNonCommercial) {
            expectedUsageTypeList.add(UsageType.NCM.getDisplayName());
        }
        if (isUsageTypeTrial) {
            expectedUsageTypeList.add(UsageType.TRL.getDisplayName());
        }
        if (isUsageTypeGovernment) {
            expectedUsageTypeList.add(UsageType.GOV.getDisplayName());
        }
        return expectedUsageTypeList;
    }

}
