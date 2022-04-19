package com.autodesk.bsm.pelican.ui.downloads.subscriptionplan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Application;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.catalog.FeatureTypeDetailPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.DownloadSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanGenericPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class tests download feature of Subscription Plan
 *
 * @author Sumant Manda
 */
public class DownloadSubscriptionPlanTest extends SeleniumWebdriver {

    private static final String ACTUAL_FILE_NAME = "subscriptionPlans.xlsx";
    private DownloadSubscriptionPlanPage downloadSubscriptionPlanPage;
    private AddProductLinePage addProductLinePage;
    private AddSubscriptionPlanPage addSubscriptionPlan;
    private AdminToolPage adminToolPage;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private PelicanPlatform resource;
    private static final int NUMBER_OF_COLUMNS = 19;
    private AddSubscriptionPlanPage addSubscriptionPlanPage;
    private SubscriptionPlanGenericPage subscriptionPlanGenericPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        subscriptionPlanGenericPage = adminToolPage.getPage(SubscriptionPlanGenericPage.class);

        adminToolPage.login();
        downloadSubscriptionPlanPage = adminToolPage.getPage(DownloadSubscriptionPlanPage.class);
        addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);
        addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);

    }

    /**
     * This method tests SubscriptionPlan download with default filters.
     */
    @Test
    public void testDownloadSubscriptionPlanWithDefaultFilter() throws IOException {

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download Subscription Plan page and download the
        // SubscriptionPlan excel file
        downloadSubscriptionPlanPage.downloadSubscriptionPlanXlsxFile(null, null, true, true, false, true, false,
            false);

        // Get the file name with file path
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        AssertCollector.assertThat("Total number of columns are incorrect", fileData[0].length, is(NUMBER_OF_COLUMNS),
            assertionErrorList);
        AssertCollector.assertThat("The usage type Header for Subscription Plan is not found in the download file",
            fileData[0][7], equalTo("usageType"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Basic Offerings download with default filters.
     */
    @Test(dataProvider = "getPackagingTypesAndExpireReminderEmail")
    public void testDownloadSubscriptionPlanForPackagingTypeAndExpireReminder(final PackagingType packagingType,
        final String packagingTypeDisplayName, final boolean sendExpireReminderEmail,
        final String expireReminderEmailFlag) throws IOException {

        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";

        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.YES);

        addProductLinePage.clickOnSubmit();
        addSubscriptionPlan.navigateToAddSubscriptionPlan();

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, prodLineNameAndExternalKey, SupportLevel.BASIC, packagingType,
            sendExpireReminderEmail);

        // Click on Save
        addSubscriptionPlan.clickOnSave(false);

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download Subscription Plan page and download the
        // SubscriptionPlan excel file
        downloadSubscriptionPlanPage.downloadSubscriptionPlanXlsxFile(prodLineNameAndExternalKey,
            OfferingType.BIC_SUBSCRIPTION.getDisplayName(), true, true, false, true, false, false);

        // Get the file name with file path
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("The Packaging type column header is missing from downloaded file", fileData[0][17],
            equalTo("packagingType"), assertionErrorList);
        AssertCollector.assertThat("The Packaging type of Subscription Plan is not IC in the download file",
            fileData[1][17], equalTo(packagingTypeDisplayName), assertionErrorList);
        AssertCollector.assertThat("The Expire Reminder Emails column header is missing from downloaded file",
            fileData[0][18], equalTo(PelicanConstants.EXPIRATION_REMINDER_EMAILS_ENABLED), assertionErrorList);
        AssertCollector.assertThat("The Expire Reminder Emails flag for Subscription Plan is not"
            + sendExpireReminderEmail + "in the download file", fileData[1][18], equalTo(expireReminderEmailFlag),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will determine assignable and eos/eolI/eolR date fields in download subscription plan
     *
     * @throws IOException
     */
    @Test
    public void testAssignableAndDateFieldsInDownloadSubscriptionPlanWhenFeatureFlagTrue() throws IOException {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, true);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, true);

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
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId);
        final String itemId = item.getId();

        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item1.getId();

        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";

        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.YES);

        addProductLinePage.clickOnSubmit();
        addSubscriptionPlan.navigateToAddSubscriptionPlan();

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, prodLineNameAndExternalKey, SupportLevel.BASIC, null, false);

        // Add Feature Entitlement
        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(true, eosDate, eolRenewalDate, eolImmediateDate, 0);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);

        addSubscriptionPlanPage.addOneTimeCurrencyAmountEntitlement("100.00", PelicanConstants.CLOUD_CURRENCY_SELECT,
            2);

        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download Subscription Plan page and download the
        // SubscriptionPlan excel file
        downloadSubscriptionPlanPage.downloadSubscriptionPlanXlsxFile(prodLineNameAndExternalKey,
            OfferingType.BIC_SUBSCRIPTION.getDisplayName(), true, true, false, true, false, false);

        // Get the file name with file path
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        // Assignable and Remove feature headers validation
        AssertCollector.assertThat(
            "The assignable column header from subscription entitlement is missing from downloaded file",
            fileData[2][8], equalTo("assignable"), assertionErrorList);
        AssertCollector.assertThat(
            "The eos column header from subscription entitlement is missing from downloaded file", fileData[2][9],
            equalTo("eos"), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header from subscription entitlement is missing from downloaded file",
            fileData[2][10], equalTo("eol immediate"), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header from subscription entitlement is missing from downloaded file",
            fileData[2][11], equalTo("eol renewal"), assertionErrorList);

        // Assignable and Remove Features Data Validation for the first CSR type feature
        AssertCollector.assertThat(
            "The assignable column header value from subscription entitlement is missing from downloaded file",
            fileData[3][8], equalTo("true"), assertionErrorList);
        AssertCollector.assertThat(
            "The eos column header value from subscription entitlement is missing from downloaded file", fileData[3][9],
            equalTo(eosDate), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header value from subscription entitlement is missing from downloaded file",
            fileData[3][10], equalTo(eolImmediateDate), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header value from subscription entitlement is missing from downloaded file",
            fileData[3][11], equalTo(eolRenewalDate), assertionErrorList);

        // Assignable and Remove Features Data Validation for the second CPR type feature
        AssertCollector.assertThat(
            "The assignable column header value from subscription entitlement is missing from downloaded file",
            fileData[4][8], equalTo("false"), assertionErrorList);
        AssertCollector.assertThat(
            "The eos column header value from subscription entitlement is missing from downloaded file", fileData[4][9],
            equalTo(eosDate), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header value from subscription entitlement is missing from downloaded file",
            fileData[4][10], equalTo(eolImmediateDate), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header value from subscription entitlement is missing from downloaded file",
            fileData[4][11], equalTo(eolRenewalDate), assertionErrorList);

        // Assignable and Remove Features Data Validation for currency entitlements
        AssertCollector.assertThat(
            "The assignable column header value from subscription entitlement is missing from downloaded file",
            fileData[5][8], equalTo(""), assertionErrorList);
        AssertCollector.assertThat(
            "The eos column header value from subscription entitlement is missing from downloaded file", fileData[5][9],
            equalTo(""), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header value from subscription entitlement is missing from downloaded file",
            fileData[5][10], equalTo(""), assertionErrorList);
        AssertCollector.assertThat(
            "The eol immediate column header value from subscription entitlement is missing from downloaded file",
            fileData[5][11], equalTo(""), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test which will determine assignable and date headers in download subscription plan
     *
     * @throws IOException
     */
    @Test
    public void testAssignableAndDateFieldsInDownloadSubscriptionPlanWhenFeatureFlagFalse() throws IOException {

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.ASSIGNABLE_ENTITLEMENTS_FEATURE_FLAG, false);

        adminToolPage.changeFeatureFlag(bankingConfigurationPropertiesPage,
            PelicanConstants.REMOVE_FEATURES_FEATURE_FLAG, false);

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
        final Item item = featureApiUtils.addFeature(null, null, featureTypeId);
        final String itemId = item.getId();

        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item1.getId();

        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";

        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.YES);

        addProductLinePage.clickOnSubmit();
        addSubscriptionPlan.navigateToAddSubscriptionPlan();

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey, subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, null, prodLineNameAndExternalKey, SupportLevel.BASIC, null, false);

        // Add Feature Entitlement
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);

        // Add non CSR feature
        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);

        addSubscriptionPlanPage.clickOnSave();
        subscriptionPlanGenericPage.readAndConfirmOrCancelMessage(true);

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download Subscription Plan page and download the
        // SubscriptionPlan excel file
        downloadSubscriptionPlanPage.downloadSubscriptionPlanXlsxFile(prodLineNameAndExternalKey,
            OfferingType.BIC_SUBSCRIPTION.getDisplayName(), true, true, false, true, false, false);

        // Get the file name with file path
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        // Assignable and Remove feature headers validation
        AssertCollector.assertThat(
            "The assignable and date column header from subscription entitlement are present in the downloaded file",
            fileData[2][8], equalTo(null), assertionErrorList);

        AssertCollector.assertThat(
            "The assignable and date column header from subscription entitlement are present in the downloaded file",
            fileData[2][9], equalTo(null), assertionErrorList);

        AssertCollector.assertThat(
            "The assignable and date column header from subscription entitlement are present in the downloaded file",
            fileData[2][10], equalTo(null), assertionErrorList);

        AssertCollector.assertThat(
            "The assignable and date column header from subscription entitlement are present in the downloaded file",
            fileData[2][11], equalTo(null), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Provider to return different PackagintTypes
     *
     * @return PackagingType
     */
    @DataProvider(name = "getPackagingTypesAndExpireReminderEmail")
    public Object[][] getPackagingTypes() {
        return new Object[][] {
                { PackagingType.INDUSTRY_COLLECTION, PackagingType.IC.getDisplayName(), PelicanConstants.TRUE_VALUE,
                        PelicanConstants.YES.toUpperCase() },
                { PackagingType.VERTICAL_GROUPING, PackagingType.VG.getDisplayName(), PelicanConstants.FALSE_VALUE,
                        PelicanConstants.NO.toUpperCase() } };
    }
}
