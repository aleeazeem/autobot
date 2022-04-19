package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.MUST_SELECT_AT_LEAST_ONE_STATUS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionPlansAndFeaturesReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.SubscriptionPlansAndFeaturesReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This test class tests Subscription Plans and Feature Report in AdminTool This page can be accessed as Reports -->
 * Offering Reports --> Subscription Plan And Features Report.
 *
 * @author Muhammad
 */
public class SubscriptionPlansAndFeaturesReportTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static SubscriptionPlansAndFeaturesReportPage subscriptionPlansAndFeaturesReportPage;
    private static final String productLineExtKey = RandomStringUtils.randomAlphabetic(10);
    private static String productLineName;
    private static String featureExternalKey;
    private static final String ERROR_MESSAGE_DUPLICATE_AND_NO_KEYS = "Duplicate or No key(s)";
    private SubscriptionPlansAndFeaturesReportResultPage subscriptionPlansAndFeaturesReportResultPage;
    private List<String> expectedOfferingStatusList;
    private static final String DOWNLOAD_FILE_NAME = "SubscriptionPlansAndFeatures.xlsx";
    private List<String> expectedColumnHeaderList;
    private SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private Item item;
    private Item item1;
    private String planName;
    private String planId;
    private String eosDate;
    private String eolRenewalDate;
    private String eolImmediateDate;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionPlansAndFeaturesReportTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        subscriptionPlansAndFeaturesReportResultPage =
            adminToolPage.getPage(SubscriptionPlansAndFeaturesReportResultPage.class);
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        expectedOfferingStatusList = new ArrayList<>();

        // Add product line
        subscriptionPlanApiUtils.addProductLine(productLineExtKey);
        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 500);
        final String offeringId = bicOfferings.getOfferings().get(0).getId();
        final String featureId =
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(offeringId, null, null, true);
        // get feature external key
        item = resource.item().getItem(featureId);
        featureExternalKey = item.getExternalKey();

        subscriptionPlansAndFeaturesReportPage = adminToolPage.getPage(SubscriptionPlansAndFeaturesReportPage.class);
        subscriptionPlansAndFeaturesReportResultPage =
            adminToolPage.getPage(SubscriptionPlansAndFeaturesReportResultPage.class);

        // Add all column names to a list
        expectedColumnHeaderList = new ArrayList<>();
        expectedColumnHeaderList.add(PelicanConstants.PLAN_ID_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PLAN_EXTERNAL_KEY_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PLAN_NAME_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PLAN_STATUS_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PLAN_USAGE_TYPE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PRODUCT_LINE_CODE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PRODUCT_LINE_NAME_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.OFFERING_DETAIL_EXTERNAL_KEY_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.FEATURE_EXTERNAL_KEY_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.FEATURE_NAME_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.FEATURE_TYPE_EXTERNAL_KEY_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.FEATURE_TYPE_NAME_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.LICENSING_MODEL_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PARENT_FEATURE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.CURRENCY_NAME_REPORT);
        expectedColumnHeaderList.add(PelicanConstants.SKU_REPORT);
        expectedColumnHeaderList.add(PelicanConstants.AMOUNT_REPORT);
        expectedColumnHeaderList.add(PelicanConstants.ASSIGNABLE_COLUMN_NAME);
        expectedColumnHeaderList.add(PelicanConstants.EOS_DATE_COLUMN_NAME);
        expectedColumnHeaderList.add(PelicanConstants.EOL_IMMEDIATE_DATE_COLUMN_NAME);
        expectedColumnHeaderList.add(PelicanConstants.EOL_RENEWAL_DATE_COLUMN_NAME);

        final Applications applications = resource.application().getApplications();
        String appId = "";
        String featureTypeId = null;

        for (final Application app : applications.getApplications()) {
            appId = appId + app.getId().concat(",");
        }

        appId = appId.substring(0, appId.length() - 1);

        final List<Map<String, String>> resultMapList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ITEM_TYPE_ID,
                PelicanConstants.CSR_FEATURE_TYPE_EXTERNAL_KEY, appId), getEnvironmentVariables());

        if (resultMapList.size() == 0) {

            final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
            final String featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);

            // Navigate to the add feature page and add a feature
            addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
                getEnvironmentVariables().getApplicationDescription(), featureTypeName,
                PelicanConstants.CSR_EXTERNAL_KEY);
            final FeatureTypeDetailPage featureTypeDetailPage = addFeatureTypePage.clickOnAddFeatureTypeButton();
            featureTypeId = featureTypeDetailPage.getId();
        } else {
            featureTypeId = resultMapList.get(0).get("ID");
        }

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, featureTypeId);
        final String itemId = item.getId();

        item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item1.getId();

        final SubscriptionPlanApiUtils subscriptionPlanUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        productLineName = "ProductLine_" + RandomStringUtils.randomAlphanumeric(7);
        subscriptionPlanUtils.addProductLine(productLineName);

        planName = RandomStringUtils.randomAlphanumeric(8);
        final AddSubscriptionPlanPage addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(planName, planName, OfferingType.BIC_SUBSCRIPTION,
            Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null,
            productLineName + " (" + productLineName + ")", SupportLevel.BASIC, null, true);

        // Add Feature Entitlement
        eosDate = new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        eolRenewalDate = new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        eolImmediateDate = new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        AssertCollector.assertTrue("Incorrect default value for the CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(0), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(true, eosDate, eolRenewalDate, eolImmediateDate, 0);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);
        AssertCollector.assertFalse("Incorrect default value for the non CSR item type checkbox",
            addSubscriptionPlanPage.getAssignable(1), assertionErrorList);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);

        addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("100.00", PelicanConstants.CLOUD_CURRENCY_SELECT,
            2);

        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlanPage.clickOnSave(true);
        planId = subscriptionPlanDetailPage.getId();

    }

    /**
     * Verify all column headers in Report View
     *
     * @result report should show 14 columns and name of the headers of each column should be Plan Id, Plan Ext Key,
     *         Plan Name, Plan Status, Plan Usage Type, Product Line Code, Product Line Name, Offering Detail Ext Key,
     *         Feature Ext Key, Feature Name, Feature Type Ext Key, Feature Type Name, Licensing Model, Parent Feature.
     */
    @Test(dataProvider = "featureFlagCombinations")
    public void testSubscriptionPlansAndFeaturesReportHeadersView(final boolean assignable,
        final boolean removeFeature) {

        int expectedColumn = 0;

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, assignable);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, removeFeature);

        subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(null, null, true, true, false, true, true,
            PelicanConstants.VIEW);
        final List<String> actualColumnHeadersList = subscriptionPlansAndFeaturesReportResultPage.getColumnHeaders();

        if (!assignable && !removeFeature) {
            expectedColumn = expectedColumnHeaderList.size() - 4;
        } else {
            expectedColumn = expectedColumnHeaderList.size();
        }

        if (expectedColumn != actualColumnHeadersList.size()) {
            // Failing the test here before checking header names to prevent array out of bounds
            // exception.
            Assert.fail("Number of columns are not correct.");
        }

        for (int i = 0; i < expectedColumn; i++) {
            AssertCollector.assertThat("Incorrect Header '" + i + 1 + "'", actualColumnHeadersList.get(i),
                equalTo(expectedColumnHeaderList.get(i)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify all column headers in Report Download
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @result report should show 14 columns and name of the headers of each column should be Plan Id, Plan Ext Key,
     *         Plan Name, Plan Status, Plan Usage Type, Product Line Code, Product Line Name, Offering Detail Ext Key,
     *         Feature Ext Key, Feature Name, Feature Type Ext Key, Feature Type Name, Licensing Model, Parent Feature.
     */
    @Test(dataProvider = "featureFlagCombinations")
    public void testSubscriptionPlansAndFeaturesReportHeadersDownload(final boolean assignable,
        final boolean removeFeature) throws IOException {

        int expectedColumn = 0;

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, assignable);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, removeFeature);

        // Delete all files before downloading report
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);
        subscriptionPlansAndFeaturesReportResultPage = subscriptionPlansAndFeaturesReportPage
            .getReportWithSelectedFilters(null, null, true, true, false, true, true, PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        if (!assignable && !removeFeature) {
            expectedColumn = expectedColumnHeaderList.size() - 4;
        } else {
            expectedColumn = expectedColumnHeaderList.size();
        }

        if (expectedColumn != fileData[0].length) {
            // Failing the test here before checking header names to prevent array out of bounds
            // exception.
            Assert.fail("Number of columns are not correct.");
        }

        for (int i = 0; i < expectedColumn; i++) {
            AssertCollector.assertThat("Incorrect Header '" + i + 1 + "'", fileData[0][i],
                equalTo(expectedColumnHeaderList.get(i)), assertionErrorList);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests Assignable, EOS and EOL dates for CSR/Non CSR and Currency type Features in Report View
     */
    @Test
    public void testAssignableAndRemoveFeatureFieldsInSubscriptionPlansAndFeaturesReportDataView() {

        subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(productLineName, null, true, true, false,
            true, true, PelicanConstants.VIEW);

        final int results = subscriptionPlansAndFeaturesReportResultPage.getTotalItems();

        AssertCollector.assertThat("Expected 3 rows from search, but found " + results + " rows", 3, equalTo(results),
            assertionErrorList);

        for (int i = 0; i < results; i++) {
            final String planIdInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanIdColumn().get(i);
            final String planExtKeyInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanExternalKeyColumn().get(i);
            final String planNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanNameColumn().get(i);
            final String planStatusInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanStatusColumn().get(i);
            final String planUsageTypeInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanUsageTypeColumn().get(i);
            final String productLineNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromProductLineNameColumn().get(i);
            final String featureExtKeyInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromFeatureExternalKeyColumn().get(i);
            final String featureNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromFeatureNameColumn().get(i);
            final String parentCurrencyNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromOfferingDetailCurrencyNameColumn().get(i);
            final String parentSkuInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromOfferingDetailSkuColumn().get(i);
            final String parentCurrencyAmountInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromOfferingDetailAmountColumn().get(i);
            final String parentAssignableInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromAssignableColumn().get(i);
            final String parentEOLImmediateInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromParentEOLImmediateDateColumn().get(i);
            final String parentEOLRenewalInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromParentEOLRenewalDateColumn().get(i);
            final String parentEOSDateInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromParentEOSDateColumn().get(i);

            AssertCollector.assertThat("Subscription Id on Subscription Detail Page is not same as shown in Report",
                planId, equalTo(planIdInReport), assertionErrorList);
            AssertCollector.assertThat("Subscription Name on Subscription Detail Page is not same as shown in Report",
                planName, equalTo(planNameInReport), assertionErrorList);
            AssertCollector.assertThat(
                "Subscription External Key on Subscription Detail Page is not same as shown in Report", planName,
                equalTo(planExtKeyInReport), assertionErrorList);

            AssertCollector.assertThat(
                "Subscription plan Status on Subscription Detail Page is not same as shown in Report",
                Status.ACTIVE.toString(), equalTo(planStatusInReport.toUpperCase()), assertionErrorList);
            AssertCollector.assertThat(
                "Subscription Usage Type on Subscription Detail Page is not same as shown in Report",
                UsageType.COM.toString(), equalTo(planUsageTypeInReport), assertionErrorList);
            AssertCollector.assertThat("Product Line on Subscription Detail Page is not same as shown in Report",
                productLineName, equalTo(productLineNameInReport), assertionErrorList);

            if (i == 0) {

                AssertCollector.assertThat(
                    "External Key of a feature on Subscription Detail Page is not same as shown in Report",
                    item.getExternalKey(), equalTo(featureExtKeyInReport), assertionErrorList);
                AssertCollector.assertThat("Name of a feature Subscription Detail Page is not same as shown in Report",
                    item.getName(), equalTo(featureNameInReport), assertionErrorList);
                AssertCollector.assertThat("Currency Name on Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentCurrencyNameInReport), assertionErrorList);
                AssertCollector.assertThat("SKU currency in Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentSkuInReport), assertionErrorList);
                AssertCollector.assertThat("Currency Amount on Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentCurrencyAmountInReport), assertionErrorList);
                AssertCollector.assertThat("Assignable on Subscription Detail Page is not same as shown in Report",
                    "true", equalTo(parentAssignableInReport), assertionErrorList);
                AssertCollector.assertThat("EOS Date on Subscription Detail Page is not same as shown in Report",
                    eosDate, equalTo(parentEOSDateInReport), assertionErrorList);
                AssertCollector.assertThat("EOL Immediate on Subscription Detail Page is not same as shown in Report",
                    eolImmediateDate, equalTo(parentEOLImmediateInReport), assertionErrorList);
                AssertCollector.assertThat("EOL Renwal on Subscription Detail Page is not same as shown in Report",
                    eolRenewalDate, equalTo(parentEOLRenewalInReport), assertionErrorList);

            } else if (i == 1) {

                AssertCollector.assertThat(
                    "External Key of a feature on Subscription Detail Page is not same as shown in Report",
                    item1.getExternalKey(), equalTo(featureExtKeyInReport), assertionErrorList);
                AssertCollector.assertThat("Name of a feature Subscription Detail Page is not same as shown in Report",
                    item1.getName(), equalTo(featureNameInReport), assertionErrorList);
                AssertCollector.assertThat("Currency Name on Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentCurrencyNameInReport), assertionErrorList);
                AssertCollector.assertThat("SKU currency in Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentSkuInReport), assertionErrorList);
                AssertCollector.assertThat("Currency Amount on Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentCurrencyAmountInReport), assertionErrorList);
                AssertCollector.assertThat("Assignable on Subscription Detail Page is not same as shown in Report",
                    "false", equalTo(parentAssignableInReport), assertionErrorList);
                AssertCollector.assertThat("EOS Date on Subscription Detail Page is not same as shown in Report",
                    eosDate, equalTo(parentEOSDateInReport), assertionErrorList);
                AssertCollector.assertThat("EOL Immediate on Subscription Detail Page is not same as shown in Report",
                    eolImmediateDate, equalTo(parentEOLImmediateInReport), assertionErrorList);
                AssertCollector.assertThat("EOL Renwal on Subscription Detail Page is not same as shown in Report",
                    eolRenewalDate, equalTo(parentEOLRenewalInReport), assertionErrorList);

            } else {

                AssertCollector.assertThat(
                    "External Key of a feature on Subscription Detail Page is not same as shown in Report", "-",
                    equalTo(featureExtKeyInReport), assertionErrorList);
                AssertCollector.assertThat("Name of a feature Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(featureNameInReport), assertionErrorList);
                AssertCollector.assertThat("Currency Name on Subscription Detail Page is not same as shown in Report",
                    PelicanConstants.CLOUD, equalTo(parentCurrencyNameInReport), assertionErrorList);
                AssertCollector.assertThat("SKU currency in Subscription Detail Page is not same as shown in Report",
                    PelicanConstants.CLDCR, equalTo(parentSkuInReport), assertionErrorList);
                AssertCollector.assertThat("Currency Amount on Subscription Detail Page is not same as shown in Report",
                    "100", equalTo(parentCurrencyAmountInReport), assertionErrorList);
                AssertCollector.assertThat("Assignable on Subscription Detail Page is not same as shown in Report", "-",
                    equalTo(parentAssignableInReport), assertionErrorList);
                AssertCollector.assertThat("EOS Date on Subscription Detail Page is not same as shown in Report", "-",
                    equalTo(parentEOSDateInReport), assertionErrorList);
                AssertCollector.assertThat("EOL Immediate on Subscription Detail Page is not same as shown in Report",
                    "-", equalTo(parentEOLImmediateInReport), assertionErrorList);
                AssertCollector.assertThat("EOL Renwal on Subscription Detail Page is not same as shown in Report", "-",
                    equalTo(parentEOLRenewalInReport), assertionErrorList);
            }

            AssertCollector.assertAll(assertionErrorList);

        }

    }

    /**
     * This test method tests Assignable, EOS and EOL dates for CSR/Non CSR and Currency type Features in Report
     * Download
     *
     * @throws IOException
     */
    @Test
    public void testAssignableAndRemoveFeatureFieldsInSubscriptionPlansAndFeaturesReportDataDownload()
        throws IOException {

        // Delete all files before downloading report
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);

        subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(productLineName, null, true, true, false,
            true, true, PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;

        final int totalRecordsInReport = XlsUtils.getNumRowsInXlsx(fileName);

        if (totalRecordsInReport == 4) {

            AssertCollector.assertThat("Incorrect Assignable value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(1, 17)), equalTo("true"), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOS Date value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(1, 18)), equalTo(eosDate), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOL Immediate date value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(1, 19)), equalTo(eolImmediateDate),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect EOL Renewal dare value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(1, 20)), equalTo(eolRenewalDate),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Assignable value for CPR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(2, 17)), equalTo("false"), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOS Date value for CPR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(2, 18)), equalTo(eosDate), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOL Immediate date value for CPR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(2, 19)), equalTo(eolImmediateDate),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect EOL Renewal dare value for CPR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(2, 20)), equalTo(eolRenewalDate),
                assertionErrorList);

            AssertCollector.assertThat("Incorrect Assignable value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(3, 17)), equalTo("-"), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOS Date value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(3, 18)), equalTo("-"), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOL Immediate date value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(3, 19)), equalTo("-"), assertionErrorList);
            AssertCollector.assertThat("Incorrect EOL Renewal dare value for CSR",
                XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(3, 20)), equalTo("-"), assertionErrorList);

        } else {
            Assert.fail("No result found");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test method verifies the data which is populated in Report in their respective column correctly. To check
     * this each column value need to be compared with values on detail page of subscription plan.
     */
    @Test
    public void testSubscriptionPlansAndFeaturesReportDataView() {
        // selecting default filters
        subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(null, null, true, true, false, true, false,
            PelicanConstants.VIEW);
        final int results = subscriptionPlansAndFeaturesReportResultPage.getTotalItems();
        final int reportIndex = subscriptionPlansAndFeaturesReportResultPage.selectRowRandomlyFromFirstPage(results);
        if (results > 0) {
            final String planIdInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanIdColumn().get(reportIndex);
            final String planExtKeyInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanExternalKeyColumn().get(reportIndex);
            final String planNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanNameColumn().get(reportIndex);
            final String planStatusInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanStatusColumn().get(reportIndex);
            final String planUsageTypeInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanUsageTypeColumn().get(reportIndex);
            final String productLineNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromProductLineNameColumn().get(reportIndex);
            final String offeringDetailExternalKeyInReport = subscriptionPlansAndFeaturesReportResultPage
                .getValuesFromOfferingDetailExternalKeyColumn().get(reportIndex);
            final String featureExtKeyInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromFeatureExternalKeyColumn().get(reportIndex);
            final String featureNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromFeatureNameColumn().get(reportIndex);
            final String featureTypeNameInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromFeatureTypeNameColumn().get(reportIndex);
            final String licensingModelInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromLicensingModelColumn().get(reportIndex);
            final String parentFeatureInReport =
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromParentFeatureColumn().get(reportIndex);

            subscriptionPlansAndFeaturesReportResultPage.selectResultRow(reportIndex + 1);
            final SubscriptionPlanDetailPage subscriptionPlanDetails =
                adminToolPage.getPage(SubscriptionPlanDetailPage.class);
            LOGGER.info("Plan Id in Report " + planIdInReport);
            LOGGER.info("ID on Subscription Plan Detail Page" + subscriptionPlanDetails.getId());
            AssertCollector.assertThat("Subscription Id on Subscription Detail Page is not same as shown in Report",
                subscriptionPlanDetails.getId(), equalTo(planIdInReport), assertionErrorList);
            AssertCollector.assertThat("Subscription Name on Subscription Detail Page is not same as shown in Report",
                subscriptionPlanDetails.getName(), equalTo(planNameInReport), assertionErrorList);
            AssertCollector.assertThat(
                "Subscription External Key on Subscription Detail Page is not same as shown in" + "Report",
                subscriptionPlanDetails.getExternalKey(), equalTo(planExtKeyInReport), assertionErrorList);
            AssertCollector.assertThat("Subscription Status on Subscription Detail Page is not same as shown in Report",
                subscriptionPlanDetails.getStatus(), equalTo(planStatusInReport.toUpperCase()), assertionErrorList);
            AssertCollector.assertThat(
                "Subscription Usage Type on Subscription Detail Page is not same as shown in " + "Report",
                subscriptionPlanDetails.getUsageType(), equalTo(planUsageTypeInReport), assertionErrorList);
            AssertCollector.assertThat("Product Line on Subscription Detail Page is not same as shown in Report",
                Util.excludeBracketPart(subscriptionPlanDetails.getProductLine()).trim(),
                equalTo(productLineNameInReport), assertionErrorList);

            AssertCollector.assertThat(
                "External Key of a feature on Subscription Detail Page is not same as shown in " + "Report",
                subscriptionPlanDetails.getOneTimeEntitlementExternalKeyColumnValues(), hasItem(featureExtKeyInReport),
                assertionErrorList);
            AssertCollector.assertThat("Name of a feature Subscription Detail Page is not same as shown in Report",
                subscriptionPlanDetails.getOneTimeEntitlementNameOrAmountColumnValues(), hasItem(featureNameInReport),
                assertionErrorList);

            AssertCollector.assertThat(
                "Name of feature Type on Subscription Detail Page is not same as shown in Report",
                subscriptionPlanDetails.getOneTimeEntitlementFeatureTypeColumnValues(),
                hasItem(featureTypeNameInReport), assertionErrorList);
            AssertCollector.assertThat("Licensing Model on Subscription Detail Page is not same as shown in Report",
                subscriptionPlanDetails.getOneTimeEntitlementLicensingModelColumnValues(),
                hasItem(licensingModelInReport), assertionErrorList);
            // If core product is not empty then compare it otherwise not.
            if (!parentFeatureInReport.equals("-")) {
                AssertCollector.assertThat(
                    "Core Product on Subscription Plan Detail Page is not same as shown in Report",
                    subscriptionPlanDetails.getOneTimeEntitlementCoreProductColumnValues().toString(),
                    containsString(parentFeatureInReport), assertionErrorList);
            }

            final String selectQueryForOfferingDetailExternalKey =
                "select od.EXTERNAL_KEY " + "from offering o , offering_detail od "
                    + "where o.OFFERING_DETAIL_ID = od.id and " + "o.id =" + planIdInReport;
            final List<String> expectedOfferingDetailExternalKey = DbUtils
                .selectQuery(selectQueryForOfferingDetailExternalKey, "od.EXTERNAL_KEY", getEnvironmentVariables());
            if (expectedOfferingDetailExternalKey.size() > 0) {
                AssertCollector.assertThat("Offering detail on Subscription Detail Page is not same as shown in Report",
                    offeringDetailExternalKeyInReport, equalTo(expectedOfferingDetailExternalKey.get(0)),
                    assertionErrorList);
            } else {
                AssertCollector.assertThat("Offering detail on Subscription Detail Page is not same as shown in Report",
                    offeringDetailExternalKeyInReport, equalTo("-"), assertionErrorList);
            }

        } else {
            LOGGER.info("No Results Found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests column values of a report depending upon the selection of filters in View report.
     *
     * @param productLine
     * @param featureExtKey
     * @param isActiveStatus
     * @param isNewStatus
     * @param isCancelledStatus
     */
    @Test(dataProvider = "combinationOfOptions")
    public void testSubscriptionPlansAndFeaturesReportWithSearchFiltersView(final String productLine,
        final String featureExtKey, final boolean isActiveStatus, final boolean isNewStatus,
        final boolean isCancelledStatus) {
        subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(productLine, featureExtKey, isActiveStatus,
            isNewStatus, isCancelledStatus, true, false, PelicanConstants.VIEW);
        final int reportCount = subscriptionPlansAndFeaturesReportResultPage.getTotalItems();
        if (reportCount > 0) {
            if (productLine != null) {
                AssertCollector.assertThat("Column Values of *Product Line Name* are not Correct ",
                    subscriptionPlansAndFeaturesReportResultPage.getValuesFromProductLineNameColumn(),
                    everyItem(equalTo(productLineExtKey)), assertionErrorList);
            } else {
                LOGGER.info("No product line is selected for Report");
            }

            if (featureExtKey != null) {
                AssertCollector.assertThat("Column Values of *Product Line Name* are not Correct ",
                    subscriptionPlansAndFeaturesReportResultPage.getValuesFromFeatureExternalKeyColumn(),
                    everyItem(equalTo(featureExternalKey)), assertionErrorList);
            } else {
                LOGGER.info("No feature is selected for Report");
            }

            // get expected offering list and validate against that list.
            expectedOfferingStatusList = getExpectedOfferingStatusList(isNewStatus, isActiveStatus, isCancelledStatus);

            AssertCollector.assertThat("Offering status is not correct",
                subscriptionPlansAndFeaturesReportResultPage.getValuesFromPlanStatusColumn(),
                everyItem(isIn(expectedOfferingStatusList)), assertionErrorList);

        } else {
            LOGGER.info("No result found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests column values of a report depending upon the selection of filters in Download report.
     *
     * @param productLine
     * @param featureExtKey
     * @param isActiveStatus
     * @param isNewStatus
     * @param isCancelledStatus
     * @throws IOException
     * @throws FileNotFoundException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test(dataProvider = "combinationOfOptions")
    public void testSubscriptionPlansAndFeaturesReportWithSearchFiltersDownload(final String productLine,
        final String featureExtKey, final boolean isActiveStatus, final boolean isNewStatus,
        final boolean isCancelledStatus) throws IOException {
        // Delete all files before downloading report
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);

        subscriptionPlansAndFeaturesReportResultPage =
            subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(productLine, featureExtKey,
                isActiveStatus, isNewStatus, isCancelledStatus, true, false, PelicanConstants.DOWNLOAD);

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;

        final int totalRecordsInReport = XlsUtils.getNumRowsInXlsx(fileName);
        // If file has only one record, report contains only header.
        if (totalRecordsInReport > 1) {
            final List<String> productLineNameColumnValue = new ArrayList<>();
            final List<String> featureExternalKeyColumnValue = new ArrayList<>();
            final List<String> planStatusColumnValue = new ArrayList<>();

            for (int i = 1; i < totalRecordsInReport; i++) {
                productLineNameColumnValue.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 5)));
                featureExternalKeyColumnValue.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 8)));
                planStatusColumnValue.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 3)));
            }

            if (productLine != null) {
                AssertCollector.assertThat("Column Values of *Product Line Name* are not Correct ",
                    productLineNameColumnValue, everyItem(equalTo(productLineExtKey)), assertionErrorList);
            } else {
                LOGGER.info("No product line is selected for Report");
            }

            if (featureExtKey != null) {
                AssertCollector.assertThat("Column Values of *feature external key* are not Correct ",
                    featureExternalKeyColumnValue, everyItem(equalTo(featureExternalKey)), assertionErrorList);
            } else {
                LOGGER.info("No feature is selected for Report");
            }

            // get expected offering list and validate against that list.
            expectedOfferingStatusList = getExpectedOfferingStatusList(isNewStatus, isActiveStatus, isCancelledStatus);
            AssertCollector.assertThat("Offering status is not correct", planStatusColumnValue,
                everyItem(isIn(expectedOfferingStatusList)), assertionErrorList);

        } else {
            LOGGER.info("No result found");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the generation of Error Message if any following things happen i) if feature external key is
     * added twice ii) if product line external key is added twice iii) no status is selected
     */
    @Test
    public void testSubscriptionPlansAndFeaturesReportErrorMessage() {
        subscriptionPlansAndFeaturesReportPage.navigateToSubscriptionPlanAndFeaturesReport();
        subscriptionPlansAndFeaturesReportPage.addFeatureWithExternalKey(featureExternalKey);
        subscriptionPlansAndFeaturesReportPage.addFeatureWithExternalKey(featureExternalKey);
        AssertCollector.assertThat("Error Message is not generated for duplication of feature external key",
            subscriptionPlansAndFeaturesReportPage.getDuplicateFeatureExternalKeyError(),
            equalTo(ERROR_MESSAGE_DUPLICATE_AND_NO_KEYS), assertionErrorList);

        subscriptionPlansAndFeaturesReportPage.addProductLineWithExternalKey(productLineExtKey);
        subscriptionPlansAndFeaturesReportPage.addProductLineWithExternalKey(productLineExtKey);
        AssertCollector.assertThat("Error Message is not generated for duplication of product line external key",
            subscriptionPlansAndFeaturesReportPage.getDuplicateProductLineExtKeyError(),
            equalTo(ERROR_MESSAGE_DUPLICATE_AND_NO_KEYS), assertionErrorList);

        subscriptionPlansAndFeaturesReportPage.getReportWithSelectedFilters(null, featureExternalKey, false, false,
            false, true, false, PelicanConstants.VIEW);

        Util.waitInSeconds(TimeConstants.TWO_SEC);

        AssertCollector.assertThat("Error Message of mandatory selection of at least one status is not Generated",
            subscriptionPlansAndFeaturesReportPage.getErrorMessage(), equalTo(MUST_SELECT_AT_LEAST_ONE_STATUS),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests report can be generated through the search functionality of feature and product line.
     */
    @Test
    public void testSubscriptionPlansAndFeaturesReportWithProductLineAndFeatureView() {
        final int numberOfFeaturesAdding = 1;
        final int numberOfProductLineAdding = 1;
        subscriptionPlansAndFeaturesReportPage.navigateToSubscriptionPlanAndFeaturesReport();
        subscriptionPlansAndFeaturesReportPage.addFeatureBySearchFunctionality(numberOfFeaturesAdding);
        subscriptionPlansAndFeaturesReportPage.addProductLineBySearchFunctionality(numberOfProductLineAdding);

        final int featuresShownOnPage = subscriptionPlansAndFeaturesReportPage.getFeaturesAdded();
        LOGGER.info("Total No of features shown on Page under Feature: " + Integer.toString(featuresShownOnPage));
        final int productLineShownOnPage = subscriptionPlansAndFeaturesReportPage.getProductLineAdded();
        LOGGER.info(
            "Total No of productLines shown on Page under ProductLine: " + Integer.toString(productLineShownOnPage));

        AssertCollector.assertThat("Could not be able to add features through search functionality",
            featuresShownOnPage, equalTo(numberOfFeaturesAdding), assertionErrorList);
        AssertCollector.assertThat("Could not be able to add product line through search functionality",
            productLineShownOnPage, equalTo(numberOfProductLineAdding), assertionErrorList);
        subscriptionPlansAndFeaturesReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Subscription Plans and Features Report is not opened",
            subscriptionPlansAndFeaturesReportResultPage.getDriver().getTitle(),
            equalTo("Pelican - Subscription Plans and Features Report"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests report can be generated through the search functionality of feature and product line.
     */
    @Test
    public void testSubscriptionPlansAndFeaturesReportWithCurrencyEntitlmentType() {
        // Add product line
        final String productLineExtKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        final AddSubscriptionPlanPage addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        subscriptionPlanApiUtils.addProductLine(productLineExtKey);
        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, productLineExtKey + " (" + productLineExtKey + ")", SupportLevel.BASIC, null, true);

        final String currencyAmount = "100";
        // Add Currency Amount Entitlement
        addSubscriptionPlan.addOneTimeCurrencyAmountEntitlement(currencyAmount, PelicanConstants.CLOUD_CURRENCY_SELECT,
            0);

        // Click on Save
        addSubscriptionPlan.clickOnSave(false);

        subscriptionPlansAndFeaturesReportPage.navigateToSubscriptionPlanAndFeaturesReport();
        subscriptionPlansAndFeaturesReportPage.activateIncludeCurrencyCheckBox();
        subscriptionPlansAndFeaturesReportPage.addProductLineWithExternalKey(productLineExtKey);

        subscriptionPlansAndFeaturesReportPage.submit(TimeConstants.ONE_SEC);

        AssertCollector.assertThat("Subscription Plans and Features Report is not opened",
            subscriptionPlansAndFeaturesReportResultPage.getDriver().getTitle(),
            equalTo("Pelican - Subscription Plans and Features Report"), assertionErrorList);

        AssertCollector
            .assertTrue(
                "Currency Name - CLOUD not found in the report", subscriptionPlansAndFeaturesReportResultPage
                    .getValuesFromOfferingDetailCurrencyNameColumn().contains(PelicanConstants.CLOUD),
                assertionErrorList);

        AssertCollector.assertTrue("SKU - CLDCR not found in the report", subscriptionPlansAndFeaturesReportResultPage
            .getValuesFromOfferingDetailSkuColumn().contains(PelicanConstants.CLDCR), assertionErrorList);

        AssertCollector.assertTrue("Amount - 100 not found in the report",
            subscriptionPlansAndFeaturesReportResultPage.getValuesFromOfferingDetailAmountColumn().contains("100"),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    @BeforeMethod
    public void beforeMethod() {
        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);
    }

    /**
     * Method to get a list of all selected offer status.
     *
     * @param isOfferStatusNew
     * @param isOfferStatusActive
     * @param isOfferStatusCanceled
     * @return List<String>
     */
    private List<String> getExpectedOfferingStatusList(final boolean isOfferStatusNew,
        final boolean isOfferStatusActive, final boolean isOfferStatusCanceled) {
        // empty list before adding any element
        expectedOfferingStatusList.clear();
        if (isOfferStatusNew) {
            expectedOfferingStatusList.add(Status.NEW.getDisplayName());
        }
        if (isOfferStatusActive) {
            expectedOfferingStatusList.add(Status.ACTIVE.getDisplayName());
        }
        if (isOfferStatusCanceled) {
            expectedOfferingStatusList.add(Status.CANCELED.getDisplayName());
        }

        return expectedOfferingStatusList;
    }

    /**
     * combination of filters use to find subscriptions by advanced find
     */
    @DataProvider(name = "combinationOfOptions")
    public Object[][] getTestData() {
        return new Object[][] { { productLineExtKey, featureExternalKey, true, true, true },
                { productLineExtKey, featureExternalKey, true, false, false }, { null, null, true, true, true },
                { null, null, false, true, false }, { null, null, false, true, true },
                { null, null, false, false, true } };
    }

    /**
     * combination of filters use to find subscriptions by advanced find
     */
    @DataProvider(name = "featureFlagCombinations")
    public Object[][] getFeatureFlagTestData() {
        return new Object[][] { { true, true }, { false, false } };
    }
}
