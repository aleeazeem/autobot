package com.autodesk.bsm.pelican.ui.downloads.subscription;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionMigrationJobStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationJobDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionMigrationPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This test class tests subscription migration upload.
 *
 * @author yerragv
 */
public class UploadSubscriptionMigrationTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private UploadSubscriptionMigrationPage uploadSubscriptionMigrationPage;
    private SubscriptionMigrationJobDetailPage subscriptionMigrationJobDetailPage;
    private static final String FILE_NAME = "UploadSubscriptionMigration.xlsx";
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private FindSubscriptionPlanPage findSubscriptionPlanPage;
    private SubscriptionPlanDetailPage subscriptionPlanDetailPage;
    private EditSubscriptionPlanPage editSubscriptionPlanPage;
    private String subscriptionOfferingExternalKey1;
    private String subscriptionOfferingExternalKey2;
    private String offerExternalKey1;
    private String offerExternalKey2;
    private String jobName;
    private static final String UPDATE_TABLE_NAME = "update subscription_migration_job set status= ";
    private static final String UPDATE_CONDITION = " where id= ";
    private static final String APP_FAMILY_ID_IN_QUERY = " and app_family_id = ";
    private static final int TOTAL_COLUMNS_DETAILS_TAB = 12;
    private static final int TOTAL_COLUMNS_ERRORS_TAB = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionMigrationTest.class.getSimpleName());
    private static final String FILE_UPLOAD_STATUS_MESSAGE = "File is being uploaded.";
    private static List<String> expectedColumnHeaderListOnDetailsTab;
    private static List<String> expectedColumnHeaderListOnErrorsTab;
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static PromotionUtils promotionUtils;
    private String sourcePlanId;
    private String sourcePlanName;
    private String sourceOfferName;
    private String sourcePriceId;
    private String sourceAmount;
    private String sourceCurrency;
    private String targetPlanId;
    private String targetPlanName;
    private String targetOfferName;
    private String targetPriceId;
    private String targetAmount;
    private String targetCurrency;
    private Offerings sourceOffering1;
    private SubscriptionDetailPage subscriptionDetailPage;
    private static final String DOWNLOAD_FILE_NAME = "mapping_errors";
    private static String fileName;
    private static final int ERROR_HEADER_POSITION_IN_DONWLOAD = 6;
    private static final int WARNINGS_COLUMN_IN_DOWNLOAD_FILE = 7;
    private static final int ROW_OF_HEADERS_IN_XLSX_FILE = 1;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        uploadSubscriptionMigrationPage = adminToolPage.getPage(UploadSubscriptionMigrationPage.class);
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);
        subscriptionPlanDetailPage = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        editSubscriptionPlanPage = adminToolPage.getPage(EditSubscriptionPlanPage.class);
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        promotionUtils = new PromotionUtils(getEnvironmentVariables());
        // create two bic offerings
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings subscriptionOffering1 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey1 = subscriptionOffering1.getOfferings().get(0).getExternalKey();
        offerExternalKey1 = subscriptionOffering1.getIncluded().getBillingPlans().get(0).getExternalKey();
        final Offerings subscriptionOffering2 =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionOfferingExternalKey2 = subscriptionOffering2.getOfferings().get(0).getExternalKey();
        offerExternalKey2 = subscriptionOffering2.getIncluded().getBillingPlans().get(0).getExternalKey();

        // Add all column names to a list
        expectedColumnHeaderListOnDetailsTab = new ArrayList<>();
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.HASH_VALUE);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.SOURCE_PLAN_NAME);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.SOURCE_OFFER_NAME);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.SOURCE_PRICE_ID);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.SOURCE_AMOUNT);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.TARGET_PLAN_NAME);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.TARGET_OFFER_NAME);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.TARGET_PRICE_ID);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.TARGET_AMOUNT);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.STORE_UPLOAD);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.SUBSCRIPTION_COUNT);
        expectedColumnHeaderListOnDetailsTab.add(PelicanConstants.MAPPING_RESULTS);
        expectedColumnHeaderListOnErrorsTab = new ArrayList<>();
        expectedColumnHeaderListOnErrorsTab.add(PelicanConstants.EMPTY_VALUE);
        expectedColumnHeaderListOnErrorsTab.add(PelicanConstants.SOURCE_PLAN_NAME);
        expectedColumnHeaderListOnErrorsTab.add(PelicanConstants.SOURCE_OFFER_NAME);
        expectedColumnHeaderListOnErrorsTab.add(PelicanConstants.SOURCE_PRICE_ID);
        expectedColumnHeaderListOnErrorsTab.add(PelicanConstants.SOURCE_AMOUNT);
        expectedColumnHeaderListOnErrorsTab.add(PelicanConstants.STORE_UPLOAD);
        expectedColumnHeaderListOnErrorsTab.add(PelicanErrorConstants.ERRORS_UPLOAD);
        expectedColumnHeaderListOnErrorsTab.add(PelicanErrorConstants.WARNINGS_UPLOAD);
    }

    /**
     * This is a test method which will test the subscription migration job without specifying the job name.
     */

    @Test
    public void testUploadSubscriptionMigrationJobwithoutSpecifyingJobName() {

        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        uploadSubscriptionMigrationPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileError("", FILE_NAME);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String errorMessageInUploadJob = uploadSubscriptionMigrationPage.getErrorMessageForField();
        AssertCollector.assertThat("Incorrect error value for the missing job name", errorMessageInUploadJob,
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the upload subscription migration with a valid job name and a valid file
     * name.
     */
    @Test
    public void testUploadSubscriptionMigrationJob() {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);

        AssertCollector.assertThat("Incorrect info message", subscriptionMigrationJobDetailPage.getStatus(),
            anyOf(equalTo(SubscriptionMigrationJobStatus.UPLOADING_FILE.getDisplayName()),
                equalTo(SubscriptionMigrationJobStatus.RUNNING_VALIDATIONS.getDisplayName()),
                equalTo(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName())),
            assertionErrorList);

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        AssertCollector.assertThat("Incorrect job status", subscriptionMigrationJobDetailPage.getStatus(),
            equalTo(SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED.getDisplayName()), assertionErrorList);

        validateCommonAssertions(jobName, SubscriptionMigrationJobStatus.VALIDATION_SUCCEEDED);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the upload subscription migration with few missing mandatory fields.
     */
    @Test
    public void testUploadSubscriptionMigrationJobWithMisisngValues() {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, "", offerExternalKey1, "", offerExternalKey2);

        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileWithoutWait(jobName, FILE_NAME);
        AssertCollector.assertThat("Incorrect info message", subscriptionMigrationJobDetailPage.getErrorOrInfoMessage(),
            equalTo(FILE_UPLOAD_STATUS_MESSAGE), assertionErrorList);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        subscriptionMigrationJobDetailPage.refreshPage();
        validateCommonAssertions(jobName, SubscriptionMigrationJobStatus.VALIDATION_FAILED);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the subscription migration job detail page with different job statuses.
     */
    @Test(dataProvider = "jobstatuses")
    public void testSubscriptionMigrationJobDetailPageWithDifferentStatuses(
        final SubscriptionMigrationJobStatus status) {

        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        final String updateQuery =
            UPDATE_TABLE_NAME + status.getDbValue() + UPDATE_CONDITION + subscriptionMigrationJobDetailPage.getId()
                + APP_FAMILY_ID_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        subscriptionMigrationJobDetailPage.refreshPage();
        subscriptionMigrationJobDetailPage = adminToolPage.getPage(SubscriptionMigrationJobDetailPage.class);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        validateCommonAssertions(jobName, status);
        // Reset the status to completed
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test verifies that an error is generated when a non-xls file is uploaded.
     */
    @Test
    public void testUploadSubscriptionMigrationJobWithNonXlsFile() {
        jobName = RandomStringUtils.randomAlphabetic(6);
        uploadSubscriptionMigrationPage = uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFileError(jobName,
            PelicanConstants.INVALID_UPLOAD_FILE_NAME);
        AssertCollector.assertThat("Error message under the page header is not correct.",
            uploadSubscriptionMigrationPage.getError(), equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Error message under the file input is not correct.",
            uploadSubscriptionMigrationPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.DEAFULT_UPLOAD_FILE_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all column headers after uploading a job. table shows 12 columns on details tab and 3 columns on Errors
     * tab. All columns have headers except indexes column
     */
    @Test
    public void testRequiredColumnsOnDetailsAndErrorsTabsAfterUploadingValidFile() {
        jobName = RandomStringUtils.randomAlphabetic(6);
        // Create Upload xlsx file for subscription migration upload
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, subscriptionOfferingExternalKey1,
            offerExternalKey1, subscriptionOfferingExternalKey2, offerExternalKey2);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final List<String> actualColumnHeadersListOnDetailsTab =
            subscriptionMigrationJobDetailPage.getMappingDetailsColumnHeaders();
        // assertions on details tab
        AssertCollector.assertThat("Number of columns are not correct.", actualColumnHeadersListOnDetailsTab.size(),
            equalTo(expectedColumnHeaderListOnDetailsTab.size()), assertionErrorList);

        for (int i = 0; i < (expectedColumnHeaderListOnDetailsTab.size() - TOTAL_COLUMNS_ERRORS_TAB); i++) {
            if (expectedColumnHeaderListOnDetailsTab.get(i).equals("Target Price ID\t")) {
                AssertCollector.assertThat("Incorrect Header '" + (i + 1) + "'",
                    actualColumnHeadersListOnDetailsTab.get(i), equalTo("Target Price ID"), assertionErrorList);
            } else {
                AssertCollector.assertThat("Incorrect Header '" + (i + 1) + "'",
                    actualColumnHeadersListOnDetailsTab.get(i), equalTo(expectedColumnHeaderListOnDetailsTab.get(i)),
                    assertionErrorList);
            }
        }
        // assertions on errors tab
        subscriptionMigrationJobDetailPage.clickOnErrorsTab();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final List<String> actualColumnHeadersListOnErrorsTab =
            subscriptionMigrationJobDetailPage.getMappingErrosColumnHeaders();
        for (int i = TOTAL_COLUMNS_DETAILS_TAB; i < (expectedColumnHeaderListOnErrorsTab.size()
            - TOTAL_COLUMNS_DETAILS_TAB); i++) {
            AssertCollector.assertThat("Incorrect Header '" + (i + 1) + "'", actualColumnHeadersListOnErrorsTab.get(i),
                equalTo(expectedColumnHeaderListOnErrorsTab.get(i)), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the plan offer mapping and price mapping for success scenario. In order to get file upload
     * Success scenario following should be valid which are given below. 1) price amount should be same in source and
     * target offer 2) no subscription which has next billing with any promotion should be associated with source plan
     * 3) source and target plans should have active status 4) source and target offers should have active status
     *
     * @result mapping result should be "✔" (ok)
     */
    @Test
    public void testSubscriptionMigrationMappingSuccessScenarioAndMigrationResults() {
        final String sourcePriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String targetPriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);

        // add source and target plan for migration
        addSourceAndTargetPlans(100, 100, getPricelistExternalKeyUs(), getPricelistExternalKeyUs(),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), sourcePriceEndDate,
            targetPriceEndDate, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE,
            OfferingType.BIC_SUBSCRIPTION, OfferingType.BIC_SUBSCRIPTION);
        // submit purchase order in order to create subscription with price id,
        // from source offer
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(sourcePriceId, 1);
        purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);

        // Create and Upload xlsx file for subscription migration upload
        jobName = RandomStringUtils.randomAlphabetic(6);
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, sourcePlanName, sourceOfferName,
            targetPlanName, targetOfferName);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);

        // assertions on details tab
        AssertCollector.assertThat("Incorrect source plan name",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePlanNameHeader().get(0), equalTo(sourcePlanName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect source offer name",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourceOfferNameHeader().get(0),
            equalTo(sourceOfferName), assertionErrorList);
        AssertCollector.assertThat("Incorrect source price id",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePriceIdHeader().get(0), equalTo(sourcePriceId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect source Amount",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSourceAmountHeader().get(0),
            equalTo(sourceAmount + " " + sourceCurrency), assertionErrorList);
        AssertCollector.assertThat("Incorrect target plan name",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPlanNameHeader().get(0), equalTo(targetPlanName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect target offer name",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetOfferNameHeader().get(0),
            equalTo(targetOfferName), assertionErrorList);
        AssertCollector.assertThat("Incorrect target price id",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPriceIdHeader().get(0), equalTo(targetPriceId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect target Amount",
            subscriptionMigrationJobDetailPage.getColumnValuesOfTargetAmountHeader().get(0),
            equalTo(targetAmount + " " + targetCurrency), assertionErrorList);
        AssertCollector.assertThat("Incorrect Store",
            subscriptionMigrationJobDetailPage.getColumnValuesOfStoreHeader().get(0), equalTo(getStoreExternalKeyUs()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Store",
            subscriptionMigrationJobDetailPage.getColumnValuesOfStoreHeader().get(0), equalTo(getStoreExternalKeyUs()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription count",
            subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionCountHeader().get(0), equalTo("1"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Mapping Results",
            subscriptionMigrationJobDetailPage.getColumnValuesOfMappingResultsHeader(),
            everyItem(equalTo(PelicanConstants.OK)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the plan offer mapping and price mapping for warning scenario. In order to get file upload
     * warning scenario following should be there which are given below. 1) source and target price amount should not be
     * same 2) subscription which has next billing with any promotion should be associated with source plan
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @result mapping result should be "⚠" (warning)
     */
    @Test(dataProvider = "offeringType")
    public void testSubscriptionMigrationMappingWarningScenarioAndMappingResults(final OfferingType sourceOfferingType,
        final OfferingType targetOfferingType) throws IOException {
        // add source and target plan for migration (one source plan is already
        // added at class level)
        final int sourcePriceAmount = 100;
        final int tragetPriceAmount = 200;
        final int totalSubscriptionsCreationWithSourceOffer1 = 2;
        final int totalSubscriptionsCreationWithSourceOffer2 = 1;
        final List<String> subscriptionIdListForSourceOffer1 = new ArrayList();
        final List<String> subscriptionIdListForSourceOffer2 = new ArrayList();

        final String sourcePriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final String targetPriceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);

        addSourceAndTargetPlans(sourcePriceAmount, tragetPriceAmount, getPricelistExternalKeyUs(),
            getPricelistExternalKeyUs(), DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            sourcePriceEndDate, targetPriceEndDate, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE,
            sourceOfferingType, targetOfferingType);

        final Offerings sourceOffering2 = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            sourceOfferingType, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);

        // create two different promotions for two source plans
        final String promoCode = promotionUtils.getRandomPromoCode();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        final JStore store = resource.stores().getStore(getStoreIdUs());
        final JPromotion promotion1 = promotionUtils.addPromotion(promoType, Lists.newArrayList(store),
            Lists.newArrayList(sourceOffering1), promoCode, false, Status.ACTIVE, "10.0", null,
            DateTimeUtils.getFutureExpirationDate(), null, null, 5, "2", "2");
        final JPromotion promotion2 = promotionUtils.addPromotion(promoType, Lists.newArrayList(store),
            Lists.newArrayList(sourceOffering2), promoCode, false, Status.ACTIVE, "15.0", null,
            DateTimeUtils.getFutureExpirationDate(), null, null, 5, "2", "2");
        // promotion 3 is added to cover for defect BIC-1730 scenario.
        promotionUtils.addPromotion(promoType, Lists.newArrayList(store), Lists.newArrayList(sourceOffering2),
            promoCode, false, Status.ACTIVE, "10.0", null, DateTimeUtils.getFutureExpirationDate(), null, null, 4, "1",
            "1");

        // Submit two purchase orders with one offering and one purchase order
        // with different offering with Promotions
        final List<PurchaseOrder> purchaseOrder1 = purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion1,
            sourceOffering1, totalSubscriptionsCreationWithSourceOffer1, null, false, 1);
        if (sourceOfferingType.equals(OfferingType.META_SUBSCRIPTION)) {
            for (int i = 0; i < totalSubscriptionsCreationWithSourceOffer1; i++) {
                purchaseOrderUtils.fulfillRequest(purchaseOrder1.get(i), FulfillmentCallbackStatus.Created);
            }
        }

        // get subscription id associated with SourceOffer1
        for (int i = 0; i < totalSubscriptionsCreationWithSourceOffer1; i++) {
            final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrder1.get(i).getId());
            subscriptionIdListForSourceOffer1.add(purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId());
        }

        final List<PurchaseOrder> purchaseOrder2 = purchaseOrderUtils.getFulfilledPurchaseOrderWithPromotion(promotion2,
            sourceOffering2, totalSubscriptionsCreationWithSourceOffer2, null, false, 1);
        if (sourceOfferingType.equals(OfferingType.META_SUBSCRIPTION)) {
            for (int i = 0; i < totalSubscriptionsCreationWithSourceOffer2; i++) {
                purchaseOrderUtils.fulfillRequest(purchaseOrder2.get(i), FulfillmentCallbackStatus.Created);
            }
        }

        // get subscription id associated with SourceOffer2
        for (int i = 0; i < totalSubscriptionsCreationWithSourceOffer2; i++) {
            final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrder2.get(i).getId());
            subscriptionIdListForSourceOffer2.add(purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId());
        }

        // Create and Upload xlsx file for subscription migration upload
        jobName = RandomStringUtils.randomAlphabetic(6);
        final XlsUtils utils = new XlsUtils();
        final ArrayList<String> columnHeadersList = new ArrayList<>();
        final ArrayList<String> recordsDataList = new ArrayList<>();
        columnHeadersList.add("Source Plan XKEY,Source Offer XKEY,Target Plan XKEY,Target Offer XKEY");
        recordsDataList.add(sourcePlanName + "," + sourceOfferName + "," + targetPlanName + "," + targetOfferName);
        recordsDataList.add(sourceOffering2.getOfferings().get(0).getName() + ","
            + sourceOffering2.getIncluded().getBillingPlans().get(0).getName() + "," + targetPlanName + ","
            + targetOfferName);
        try {
            utils.createAndWriteToXls(FILE_NAME, columnHeadersList, recordsDataList, false);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        final String jobIdForWarningsMapping = subscriptionMigrationJobDetailPage.getId();

        int errorsAndWarningsIndex1 = 0;
        int errorsAndWarningsIndex2 = 0;
        for (int i = 0; i < recordsDataList.size(); i++) {
            if (subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePlanNameHeader().get(i)
                .equals(sourcePlanName)) {
                AssertCollector.assertThat("Incorrect source plan name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePlanNameHeader().get(i),
                    equalTo(sourcePlanName), assertionErrorList);
                AssertCollector.assertThat("Incorrect source offer name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourceOfferNameHeader().get(i),
                    equalTo(sourceOfferName), assertionErrorList);
                AssertCollector.assertThat("Incorrect source price id",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePriceIdHeader().get(i),
                    equalTo(sourcePriceId), assertionErrorList);
                AssertCollector.assertThat("Incorrect source Amount",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourceAmountHeader().get(i),
                    equalTo(sourceAmount + " " + sourceCurrency), assertionErrorList);
                AssertCollector.assertThat("Incorrect target plan name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPlanNameHeader().get(i),
                    equalTo(targetPlanName), assertionErrorList);
                AssertCollector.assertThat("Incorrect target offer name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetOfferNameHeader().get(i),
                    equalTo(targetOfferName), assertionErrorList);
                AssertCollector.assertThat("Incorrect target price id",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPriceIdHeader().get(i),
                    equalTo(targetPriceId), assertionErrorList);
                AssertCollector.assertThat("Incorrect target Amount",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetAmountHeader().get(i),
                    equalTo(targetAmount + " " + targetCurrency), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription count",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionCountHeader().get(i), equalTo("2"),
                    assertionErrorList);
                errorsAndWarningsIndex1 = i;
            } else {
                AssertCollector.assertThat("Incorrect source plan name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePlanNameHeader().get(i),
                    equalTo(sourceOffering2.getOfferings().get(0).getName()), assertionErrorList);
                AssertCollector.assertThat("Incorrect source offer name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourceOfferNameHeader().get(i),
                    equalTo(sourceOffering2.getIncluded().getBillingPlans().get(0).getName()), assertionErrorList);
                AssertCollector.assertThat("Incorrect source price id",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourcePriceIdHeader().get(i),
                    equalTo(sourceOffering2.getIncluded().getPrices().get(0).getId()), assertionErrorList);
                AssertCollector.assertThat("Incorrect source Amount",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSourceAmountHeader().get(i),
                    equalTo(sourceOffering2.getIncluded().getPrices().get(0).getAmount() + " "
                        + sourceOffering2.getIncluded().getPrices().get(0).getCurrency()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect target plan name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPlanNameHeader().get(i),
                    equalTo(targetPlanName), assertionErrorList);
                AssertCollector.assertThat("Incorrect target offer name",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetOfferNameHeader().get(i),
                    equalTo(targetOfferName), assertionErrorList);
                AssertCollector.assertThat("Incorrect target price id",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetPriceIdHeader().get(i),
                    equalTo(targetPriceId), assertionErrorList);
                AssertCollector.assertThat("Incorrect target Amount",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfTargetAmountHeader().get(i),
                    equalTo(targetAmount + " " + targetCurrency), assertionErrorList);
                AssertCollector.assertThat("Incorrect subscription count",
                    subscriptionMigrationJobDetailPage.getColumnValuesOfSubscriptionCountHeader().get(i), equalTo("1"),
                    assertionErrorList);
                errorsAndWarningsIndex2 = i;
            }
        }
        AssertCollector.assertThat("Incorrect Store", subscriptionMigrationJobDetailPage.getColumnValuesOfStoreHeader(),
            everyItem(equalTo(getStoreExternalKeyUs())), assertionErrorList);
        AssertCollector.assertThat("Incorrect Mapping Results",
            subscriptionMigrationJobDetailPage.getColumnValuesOfMappingResultsHeader(),
            everyItem(equalTo(PelicanErrorConstants.WARNING)), assertionErrorList);

        // Assertions on errors tab
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME + "_"
            + subscriptionMigrationJobDetailPage.getId() + "." + PelicanConstants.XLSX_FORMAT;
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        subscriptionMigrationJobDetailPage.clickOnErrorDetailsTab();
        subscriptionMigrationJobDetailPage.clickOnMappingErrorsDownloadLink();

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Incorrect Errors",
            subscriptionMigrationJobDetailPage.getColumnValuesOfErrorHeader(), everyItem(equalTo("")),
            assertionErrorList);

        // on error tabs target price is shown as int not double
        final DecimalFormat decimalFormat = new DecimalFormat("#.00");
        final String targetPriceAmountInDouble = decimalFormat.format((double) tragetPriceAmount);

        if (sourceOfferingType.equals(targetOfferingType)) {
            AssertCollector.assertThat(
                "Incorrect Warnings of amount, promotion, list of subscription " + "on migration job detail page",
                subscriptionMigrationJobDetailPage.getColumnValuesOfHeader().get(errorsAndWarningsIndex1),
                equalTo(PelicanErrorConstants.WARNING_MESSAGE_FOR_TARGET_AMOUNT + targetPriceAmountInDouble
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_SOURCE_AMOUNT + sourceAmount + " " + sourceCurrency
                    + "; " + totalSubscriptionsCreationWithSourceOffer1 + " "
                    + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE + " " + PelicanConstants.HIPHEN + " "
                    + PelicanErrorConstants.LIST_OF_SUBSCRIPTION_WARNING_MESSAGE),
                assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Warnings of amount, promotion, list of subscription in downloaded file",
                fileData[errorsAndWarningsIndex1 + ROW_OF_HEADERS_IN_XLSX_FILE][WARNINGS_COLUMN_IN_DOWNLOAD_FILE]
                    .trim(),
                equalTo(PelicanErrorConstants.WARNING_MESSAGE_FOR_TARGET_AMOUNT + targetPriceAmountInDouble
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_SOURCE_AMOUNT + sourceAmount + " " + sourceCurrency
                    + ";  " + totalSubscriptionsCreationWithSourceOffer1 + " "
                    + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE),
                assertionErrorList);
        } else {
            AssertCollector.assertThat(
                "Incorrect Warnings of different offering type, amount, promotion, "
                    + "list of subscription on migration job detail page on row:" + errorsAndWarningsIndex1,
                subscriptionMigrationJobDetailPage.getColumnValuesOfHeader().get(errorsAndWarningsIndex1),
                equalTo(PelicanErrorConstants.WARNING_MESSAGE_FOR_DIFFERENT_OFFERING_TYPE + "\n"
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_TARGET_AMOUNT + targetPriceAmountInDouble
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_SOURCE_AMOUNT + sourceAmount + " " + sourceCurrency
                    + "; " + totalSubscriptionsCreationWithSourceOffer1 + " "
                    + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE + " " + PelicanConstants.HIPHEN + " "
                    + PelicanErrorConstants.LIST_OF_SUBSCRIPTION_WARNING_MESSAGE),
                assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Warnings of different offering type, amount, promotion, "
                    + "list of subscription on migration job detail page on row:" + errorsAndWarningsIndex2,
                subscriptionMigrationJobDetailPage.getColumnValuesOfHeader().get(errorsAndWarningsIndex2),
                equalTo(PelicanErrorConstants.WARNING_MESSAGE_FOR_DIFFERENT_OFFERING_TYPE + "\n"
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_TARGET_AMOUNT + targetPriceAmountInDouble
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_SOURCE_AMOUNT
                    + sourceOffering2.getIncluded().getPrices().get(0).getAmount() + " "
                    + sourceOffering2.getIncluded().getPrices().get(0).getCurrency() + "; "
                    + totalSubscriptionsCreationWithSourceOffer2 + " " + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE
                    + " " + PelicanConstants.HIPHEN + " " + PelicanErrorConstants.LIST_OF_SUBSCRIPTION_WARNING_MESSAGE),
                assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect Warnings of amount, promotion, list of subscription in downloaded file",
                fileData[errorsAndWarningsIndex1 + ROW_OF_HEADERS_IN_XLSX_FILE][WARNINGS_COLUMN_IN_DOWNLOAD_FILE]
                    .trim(),
                equalTo(PelicanErrorConstants.WARNING_MESSAGE_FOR_DIFFERENT_OFFERING_TYPE + ";  "
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_TARGET_AMOUNT + targetPriceAmountInDouble
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_SOURCE_AMOUNT + sourceAmount + " " + sourceCurrency
                    + ";  " + totalSubscriptionsCreationWithSourceOffer1 + " "
                    + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE),
                assertionErrorList);

            AssertCollector.assertThat(
                "Incorrect Warnings of amount, promotion, list of subscription in downloaded file",
                fileData[errorsAndWarningsIndex2 + ROW_OF_HEADERS_IN_XLSX_FILE][WARNINGS_COLUMN_IN_DOWNLOAD_FILE]
                    .trim(),
                equalTo(PelicanErrorConstants.WARNING_MESSAGE_FOR_DIFFERENT_OFFERING_TYPE + ";  "
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_TARGET_AMOUNT + targetPriceAmountInDouble
                    + PelicanErrorConstants.WARNING_MESSAGE_FOR_SOURCE_AMOUNT
                    + sourceOffering2.getIncluded().getPrices().get(0).getAmount() + " "
                    + sourceOffering2.getIncluded().getPrices().get(0).getCurrency() + ";  "
                    + totalSubscriptionsCreationWithSourceOffer2 + " "
                    + PelicanErrorConstants.PROMOTION_WARNING_MESSAGE),
                assertionErrorList);
        }
        AssertCollector.assertThat("Run button doesn't exist", subscriptionMigrationJobDetailPage.isRunButtonPresent(),
            equalTo(true), assertionErrorList);

        // verify subscription id for sourceOffer2
        subscriptionMigrationJobDetailPage.clickSubscriptionList(1);
        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        AssertCollector.assertThat("Subscription id for sourceOffer2 is not correct", subscriptionDetailPage.getId(),
            equalTo(subscriptionIdListForSourceOffer2.get(0)), assertionErrorList);

        // verify subscription id for sourceOffer1
        // navigate back to job detail page on error tab
        subscriptionMigrationJobDetailPage =
            subscriptionMigrationJobDetailPage.navigateToSubscriptionMigrationJobDetailPage(jobIdForWarningsMapping);
        subscriptionMigrationJobDetailPage.clickOnErrorDetailsTab();
        subscriptionMigrationJobDetailPage.clickSubscriptionList(0);

        final List<String> subscriptionIds = subscriptionMigrationJobDetailPage.getColumnValues("ID");
        for (final String id : subscriptionIds) {
            if (!id.equals(subscriptionIdListForSourceOffer1.get(0))
                && !id.equals(subscriptionIdListForSourceOffer1.get(1))) {
                Assert.fail("Subscription not found");
            }
        }
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the plan offer mapping and price mapping for Error scenario. In order to get file upload
     * warning scenario source price could not be able to map with target price (pricelist could be different).
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @result mapping result should be "❗" (error)
     */
    @Test
    public void testSubscriptionMigrationErrorScenarioForPriceList() throws IOException {
        final String sourceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 3);
        final String targetEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);
        addSourceAndTargetPlans(100, 100, getPricelistExternalKeyUs(), getPricelistExternalKeyUk(),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), sourceEndDate, targetEndDate,
            Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, OfferingType.BIC_SUBSCRIPTION,
            OfferingType.BIC_SUBSCRIPTION);

        // Create and Upload xlsx file for subscription migration upload
        jobName = RandomStringUtils.randomAlphabetic(6);
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, sourcePlanName, sourceOfferName,
            targetPlanName, targetOfferName);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        AssertCollector.assertThat("Incorrect Mapping Results",
            subscriptionMigrationJobDetailPage.getColumnValuesOfMappingResultsHeader(),
            everyItem(equalTo(PelicanErrorConstants.Error)), assertionErrorList);

        final String errorMessageForNonMatchingPriceList = PelicanErrorConstants.ERROR_MESSAGE_FOR_PRICELIST_1
            + targetOfferName + PelicanErrorConstants.ERROR_MESSAGE_FOR_PRICELIST_2 + sourcePriceId;
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME + "_"
            + subscriptionMigrationJobDetailPage.getId() + "." + PelicanConstants.XLSX_FORMAT;
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);

        subscriptionMigrationJobDetailPage.clickOnErrorDetailsTab();
        subscriptionMigrationJobDetailPage.clickOnMappingErrorsDownloadLink();
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        AssertCollector.assertThat("Incorrect Errors",
            subscriptionMigrationJobDetailPage.getColumnValuesOfErrorHeader().get(0),
            equalTo(errorMessageForNonMatchingPriceList), assertionErrorList);
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.isRunButtonPresent(),
            equalTo(false), assertionErrorList);
        AssertCollector.assertThat("Incorrect Errors", fileData[1][ERROR_HEADER_POSITION_IN_DONWLOAD].trim(),
            equalTo(errorMessageForNonMatchingPriceList), assertionErrorList);
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the plan status for Error scenario. In order to get file upload error scenario source or
     * target plan should have canceled Status.
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @result mapping result should be "❗" (error)
     */
    @Test(dataProvider = "canceledStatusOfSourceOrTarget")
    public void testSubscriptionMigrationErrorWithCanceledSourceAndTargetPlan(final String sourceOrTargetPlan)
        throws IOException {
        final String sourceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 3);
        final String targetEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);
        // add source and target subscription plan
        addSourceAndTargetPlans(100, 100, getPricelistExternalKeyUs(), getPricelistExternalKeyUs(),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), sourceEndDate, targetEndDate,
            Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, OfferingType.BIC_SUBSCRIPTION,
            OfferingType.BIC_SUBSCRIPTION);

        // cancel the status of plan through ui
        if (sourceOrTargetPlan.equals("source")) {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(sourcePlanId);
        } else {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(targetPlanId);
        }
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editStatus(Status.CANCELED);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        // create and upload an excel file
        jobName = RandomStringUtils.randomAlphabetic(6);
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, sourcePlanName, sourceOfferName,
            targetPlanName, targetOfferName);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);
        AssertCollector.assertThat("Incorrect Mapping Results",
            subscriptionMigrationJobDetailPage.getColumnValuesOfMappingResultsHeader(),
            everyItem(equalTo(PelicanErrorConstants.Error)), assertionErrorList);

        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME + "_"
            + subscriptionMigrationJobDetailPage.getId() + "." + PelicanConstants.XLSX_FORMAT;
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        subscriptionMigrationJobDetailPage.clickOnErrorDetailsTab();
        subscriptionMigrationJobDetailPage.clickOnMappingErrorsDownloadLink();

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        LOGGER.info("Total number of records in file is: " + fileData.length);
        final String errorMessageForInvalidSourcePlan = PelicanErrorConstants.ERROR_MESSAGE_FOR_INVALID_SOURCE_PLAN
            + sourcePlanName + PelicanErrorConstants.ERROR_MESSAGE_INVALID;
        final String errorMessageForInvalidTargetPlan = PelicanErrorConstants.ERROR_MESSAGE_FOR_INVALID_TARGET_PLAN
            + targetPlanName + PelicanErrorConstants.ERROR_MESSAGE_INVALID;

        if (sourceOrTargetPlan.equals("source")) {
            AssertCollector.assertThat(
                "Incorrect error message for source plan with invalid status on migration " + "job detail page",
                subscriptionMigrationJobDetailPage.getColumnValuesOfErrorHeader().get(0),
                equalTo(errorMessageForInvalidSourcePlan), assertionErrorList);
            AssertCollector.assertThat("Incorrect error message for source plan with invalid status in downloaded file",
                fileData[1][ERROR_HEADER_POSITION_IN_DONWLOAD].trim(), equalTo(errorMessageForInvalidSourcePlan + ";"),
                assertionErrorList);
        } else {
            AssertCollector.assertThat(
                "Incorrect error message for target plan with invalid status on migration " + "job detail page",
                subscriptionMigrationJobDetailPage.getColumnValuesOfErrorHeader().get(0),
                equalTo(errorMessageForInvalidTargetPlan), assertionErrorList);
            AssertCollector.assertThat("Incorrect Errors for target plan with invalid status in downloaded file",
                fileData[1][ERROR_HEADER_POSITION_IN_DONWLOAD].trim(), equalTo(errorMessageForInvalidTargetPlan + ";"),
                assertionErrorList);
        }
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.isRunButtonPresent(),
            equalTo(false), assertionErrorList);
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests the offer status in plan for Error scenario. In order to get file upload error scenario
     * source offer or target offer should have canceled Status.
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @result mapping result should be "❗" (error)
     */
    @Test(dataProvider = "canceledStatusOfSourceOrTarget")
    public void testSubscriptionMigrationErrorWithCanceledSourceAndTargetOffer(final String sourceOrTargetOffer)
        throws IOException {
        final String sourceEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 3);
        final String targetEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);
        // add source and target subscription plan
        addSourceAndTargetPlans(100, 100, getPricelistExternalKeyUs(), getPricelistExternalKeyUs(),
            DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH), sourceEndDate, targetEndDate,
            Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, Status.ACTIVE, OfferingType.BIC_SUBSCRIPTION,
            OfferingType.BIC_SUBSCRIPTION);

        // cancel the status of source offer through ui
        if (sourceOrTargetOffer.equals("source")) {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(sourcePlanId);
        } else {
            subscriptionPlanDetailPage = findSubscriptionPlanPage.findSubscriptionPlanById(targetPlanId);
        }
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        editSubscriptionPlanPage = subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.clickOnEditOfferButton();
        editSubscriptionPlanPage.editFieldsOfOffer(Status.CANCELED, null, null, null);
        editSubscriptionPlanPage.clickOnSaveOfferButton();
        subscriptionPlanDetailPage = editSubscriptionPlanPage.clickOnSave(false);

        // create and upload an excel file
        jobName = RandomStringUtils.randomAlphabetic(6);
        uploadSubscriptionMigrationPage.createXlsxAndWriteData(FILE_NAME, sourcePlanName, sourceOfferName,
            targetPlanName, targetOfferName);
        subscriptionMigrationJobDetailPage =
            uploadSubscriptionMigrationPage.uploadSubscriptionMigrationFile(jobName, FILE_NAME);

        AssertCollector.assertThat("Incorrect Mapping Results",
            subscriptionMigrationJobDetailPage.getColumnValuesOfMappingResultsHeader(),
            everyItem(equalTo(PelicanErrorConstants.Error)), assertionErrorList);
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME + "_"
            + subscriptionMigrationJobDetailPage.getId() + "." + PelicanConstants.XLSX_FORMAT;
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        subscriptionMigrationJobDetailPage.clickOnErrorDetailsTab();
        subscriptionMigrationJobDetailPage.clickOnMappingErrorsDownloadLink();

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        LOGGER.info("Total number of records in file is: " + fileData.length);
        final String errorMessageForInvalidSourceOffer = PelicanErrorConstants.ERROR_MESSAGE_FOR_INVALID_SOURCE_OFFER
            + sourceOfferName + PelicanErrorConstants.ERROR_MESSAGE_INVALID;
        final String errorMessageForInvalidTargetOffer = PelicanErrorConstants.ERROR_MESSAGE_FOR_INVALID_TARGET_OFFER
            + targetOfferName + PelicanErrorConstants.ERROR_MESSAGE_INVALID;

        if (sourceOrTargetOffer.equals("source")) {
            AssertCollector.assertThat(
                "Incorrect error message for source offer with invalid status on " + "migration job detail Page",
                subscriptionMigrationJobDetailPage.getColumnValuesOfErrorHeader().get(0),
                equalTo(errorMessageForInvalidSourceOffer), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect error message for source offer with invalid status in downloaded file",
                fileData[1][ERROR_HEADER_POSITION_IN_DONWLOAD].trim(), equalTo(errorMessageForInvalidSourceOffer + ";"),
                assertionErrorList);
        } else {
            AssertCollector.assertThat(
                "Incorrect error message for target offer with invalid status on" + "migration job detail Page",
                subscriptionMigrationJobDetailPage.getColumnValuesOfErrorHeader().get(0),
                equalTo(errorMessageForInvalidTargetOffer), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect error message for target offer with invalid status in downloaded file",
                fileData[1][ERROR_HEADER_POSITION_IN_DONWLOAD].trim(), equalTo(errorMessageForInvalidTargetOffer + ";"),
                assertionErrorList);
        }
        AssertCollector.assertThat("Run button exists", subscriptionMigrationJobDetailPage.isRunButtonPresent(),
            equalTo(false), assertionErrorList);
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), fileName);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to add two plans (source and target) with given parameters which are written below
     *
     * @param sourcePrice
     * @param targetPrice
     * @param targetPriceList
     * @param startDate
     * @param sourceEndDate
     * @param targetEndDate
     * @param sourcePlanStatus
     * @param targetPlanStatus
     * @param sourceOfferStatus
     * @param targetOfferStatus
     * @param sourceOfferingType
     * @param targetOfferingType
     */
    private void addSourceAndTargetPlans(final int sourcePrice, final int targetPrice, final String sourcePriceList,
        final String targetPriceList, final String startDate, final String sourceEndDate, final String targetEndDate,
        final Status sourcePlanStatus, final Status targetPlanStatus, final Status sourceOfferStatus,
        final Status targetOfferStatus, final OfferingType sourceOfferingType, final OfferingType targetOfferingType) {

        sourceOffering1 =
            addSubscriptionPlan("PL_" + RandomStringUtils.randomAlphabetic(6), getPricelistExternalKeyUs(),
                sourceOfferingType, BillingFrequency.MONTH, sourcePlanStatus, sourceOfferStatus, SupportLevel.BASIC,
                RandomStringUtils.randomAlphabetic(6), resource, sourcePrice, startDate, sourceEndDate);
        sourcePlanId = sourceOffering1.getOfferings().get(0).getId();
        sourcePlanName = sourceOffering1.getOfferings().get(0).getName();
        sourceOfferName = sourceOffering1.getIncluded().getBillingPlans().get(0).getName();
        sourcePriceId = sourceOffering1.getIncluded().getPrices().get(0).getId();
        sourceAmount = sourceOffering1.getIncluded().getPrices().get(0).getAmount();
        sourceCurrency = sourceOffering1.getIncluded().getPrices().get(0).getCurrency();

        // add target plan with different price from source offer for migration
        final Offerings targetOffering = addSubscriptionPlan(RandomStringUtils.randomAlphabetic(6), targetPriceList,
            targetOfferingType, BillingFrequency.MONTH, targetPlanStatus, targetOfferStatus, SupportLevel.BASIC,
            RandomStringUtils.randomAlphabetic(6), resource, targetPrice, startDate, targetEndDate);
        targetPlanId = targetOffering.getOfferings().get(0).getId();
        targetPlanName = targetOffering.getOfferings().get(0).getName();
        targetOfferName = targetOffering.getIncluded().getBillingPlans().get(0).getName();
        targetPriceId = targetOffering.getIncluded().getPrices().get(0).getId();
        targetAmount = targetOffering.getIncluded().getPrices().get(0).getAmount();
        targetCurrency = targetOffering.getIncluded().getPrices().get(0).getCurrency();
    }

    /**
     * This is a method for common assertions for subscription migration job status.
     *
     * @param jobName - job name
     * @param status - SubscriptionMigrationJobStatus
     */
    private void validateCommonAssertions(final String jobName, final SubscriptionMigrationJobStatus status) {

        AssertCollector.assertThat("Incorrect job id", subscriptionMigrationJobDetailPage.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect job name", subscriptionMigrationJobDetailPage.getJobName(),
            equalTo(jobName), assertionErrorList);
        AssertCollector.assertThat("Incorrect created date", subscriptionMigrationJobDetailPage.getCreated(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect created by",
            subscriptionMigrationJobDetailPage.getCreatedBy().split(" ")[0],
            equalTo(getEnvironmentVariables().getUserName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect last modified", subscriptionMigrationJobDetailPage.getLastModified(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect file upload job id",
            subscriptionMigrationJobDetailPage.getFileUploadJobId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect file upload status", subscriptionMigrationJobDetailPage.getStatus(),
            equalTo(status.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect last modified by", subscriptionMigrationJobDetailPage.getLastModifiedBy(),
            equalTo(PelicanConstants.AUTO_USER_NAME + " (" + getEnvironmentVariables().getUserId() + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect run date", subscriptionMigrationJobDetailPage.getRunDate(),
            equalTo(PelicanConstants.HIPHEN), assertionErrorList);
        AssertCollector.assertThat("Incorrect run by", subscriptionMigrationJobDetailPage.getRunBy(),
            equalTo(PelicanConstants.HIPHEN), assertionErrorList);
    }

    /**
     * This is a data provider to return SubscriptionMigrationJobStatuses.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "jobstatuses")
    public static Object[][] getSubscriptionMigrationJobStatuses() {
        return new Object[][] { { SubscriptionMigrationJobStatus.UPLOADING_FILE },
                { SubscriptionMigrationJobStatus.RUNNING_VALIDATIONS }, { SubscriptionMigrationJobStatus.COMPLETED },
                { SubscriptionMigrationJobStatus.PARTIALLY_COMPLETED }, { SubscriptionMigrationJobStatus.FAILED } };
    }

    /**
     * This is a data provider to return SubscriptionMigrationJobStatuses.
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "offeringType")
    public static Object[][] getOfferingType() {
        return new Object[][] { { OfferingType.BIC_SUBSCRIPTION, OfferingType.BIC_SUBSCRIPTION },
                { OfferingType.META_SUBSCRIPTION, OfferingType.META_SUBSCRIPTION },
                { OfferingType.META_SUBSCRIPTION, OfferingType.BIC_SUBSCRIPTION } };
    }

    /**
     * This is a data provider to return source and target plan for canceled status
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "canceledStatusOfSourceOrTarget")
    public static Object[][] getStatuses() {
        return new Object[][] { { "source" }, { "target" } };
    }

    /**
     * Add Subscription plan with data which is given below
     *
     * @param productLineExternalKey
     * @param priceListExternalKey
     * @param offeringType
     * @param billingFrequency
     * @param status
     * @param supportLevel
     * @param subscriptionOfferExternalKey
     * @param resource
     * @param amount
     * @param startDate
     * @param endDate
     * @return subscription plan with offer and price
     */
    private Offerings addSubscriptionPlan(final String productLineExternalKey, final String priceListExternalKey,
        final OfferingType offeringType, final BillingFrequency billingFrequency, final Status planStatus,
        final Status offerStatus, final SupportLevel supportLevel, final String subscriptionOfferExternalKey,
        final PelicanPlatform resource, final int amount, final String startDate, final String endDate) {
        // add product line
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
        // add subscription plan
        final Offerings newSubscriptionPlan = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey,
            offeringType, planStatus, supportLevel, UsageType.COM, resource, null, null);

        final String subscriptionPlanOfferingId = newSubscriptionPlan.getOffering().getId();
        // Add an offer to Subscription plan
        final SubscriptionOffer subscriptionOffer = subscriptionPlanApiUtils
            .helperToAddSubscriptionOfferToPlan(subscriptionOfferExternalKey, billingFrequency, 1, offerStatus);
        final String subscriptionOfferId = subscriptionPlanApiUtils
            .addSubscriptionOffer(resource, subscriptionOffer, subscriptionPlanOfferingId).getData().getId();
        // add prices to an offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOfferWithDates(amount, priceListExternalKey, startDate, endDate),
            subscriptionPlanOfferingId, subscriptionOfferId);
        // get offerings by id
        return resource.offerings().getOfferingById(subscriptionPlanOfferingId, "offers,prices");
    }

}
