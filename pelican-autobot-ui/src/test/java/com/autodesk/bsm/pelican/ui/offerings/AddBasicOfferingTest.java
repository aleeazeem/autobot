package com.autodesk.bsm.pelican.ui.offerings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.basicoffering.Currency;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.helper.auditlog.AuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.BasicOfferingAuditLogHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAndBasicOfferingsAuditLogReportHelper;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.AddBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.BasicOfferingDetailPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.FindBasicOfferingPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.FindDescriptorDefinitionsPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.FindProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.ui.productline.HelperForProductLine;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.DescriptorUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

public class AddBasicOfferingTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private static String productLineNameAndExternalKey;
    private static AddBasicOfferingPage addBasicOfferingPage;
    private static String basicOfferingId;
    private static AddDescriptorPage addDescriptorPage;
    private static FindDescriptorDefinitionsPage findDescriptorDefinitionsPage;
    private static final String AMOUNT = "10";
    private static final String effectiveStartDate =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1);
    private static final String effectiveEndDate =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
    private static final String effectiveStartDateInAuditLog =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_START_DATE, 1);
    private static final String effectiveEndDateInAuditLog =
        DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_FOR_AUDIT_LOG_END_DATE, 2);
    private static final String OFFERING_DETAILS = PelicanConstants.OFFERING_DETAILS1 + " (DC020500)";
    private String currencyName;
    private Currency currency;
    private BasicOfferingDetailPage basicOfferingDetailPage;
    private FindBasicOfferingPage findBasicOfferingPage;
    private static final String MAX_DESCRIPTOR_LENGTH = "40";
    private static final int EXTERNAL_KEY_LENGTH = 12;
    private AddProductLinePage addProductLinePage;
    private ProductLineDetailsPage productLineDetailsPage;
    private FindProductLinePage findProductLinePage;
    private AuditLogReportHelper auditLogReportHelper;
    private String adminToolUserId;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddBasicOfferingTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        // Initiating the environment and the appFamily set to AUTO
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        auditLogReportHelper = new AuditLogReportHelper(adminToolPage);
        adminToolUserId = getEnvironmentVariables().getUserId();

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        productLineNameAndExternalKey =
            getProductLineExternalKeyRevit() + " (" + getProductLineExternalKeyRevit() + ")";
        addDescriptorPage = adminToolPage.getPage(AddDescriptorPage.class);
        addBasicOfferingPage = adminToolPage.getPage(AddBasicOfferingPage.class);
        findDescriptorDefinitionsPage = adminToolPage.getPage(FindDescriptorDefinitionsPage.class);
        findBasicOfferingPage = adminToolPage.getPage(FindBasicOfferingPage.class);
        addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);
        productLineDetailsPage = adminToolPage.getPage(ProductLineDetailsPage.class);
        findProductLinePage = adminToolPage.getPage(FindProductLinePage.class);
    }

    /**
     * Verify the Audit Log for data corresponding to create basic offering price. It is addressed as "Subscription
     * Price"
     */
    @Test
    public void testCreateBasicOfferingPriceSuccess() {

        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey,
            RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(8), MediaType.DVD, null, null,
            UsageType.COM, null, null);

        final String namerStoreName = getStoreUs().getName();
        final String nameOfNamerPriceList = getStoreUs().getIncluded().getPriceLists().get(0).getName();
        final String namerPriceListId = getStoreUs().getIncluded().getPriceLists().get(0).getId();

        // Create subscription price
        addBasicOfferingPage.addPrices(1, namerStoreName, nameOfNamerPriceList, AMOUNT, effectiveStartDate,
            effectiveEndDate);
        basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // Get the Basic Offering id
        basicOfferingId = basicOfferingDetailPage.getId();
        final String subscriptionPriceId = basicOfferingDetailPage.getFirstPriceId();
        AssertCollector.assertThat("Invalid subscriptionOfferId", subscriptionPriceId, notNullValue(),
            assertionErrorList);

        // Audit Log validation
        final boolean createPriceAuditDataFound =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOfferingPrice(subscriptionPriceId, null,
                basicOfferingId, null, namerPriceListId, null, namerStoreName, null, AMOUNT, null,
                String.valueOf(com.autodesk.bsm.pelican.enums.Currency.USD.getCode()), null,
                effectiveStartDateInAuditLog, null, effectiveEndDateInAuditLog, Action.CREATE, null,
                assertionErrorList);

        AssertCollector.assertTrue("CREATE basic offering subscription price audit data not found",
            createPriceAuditDataFound, assertionErrorList);

        // Verify Audit Log report results for Basic Offering price id.
        final HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper
            .verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId,
                subscriptionPriceId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPriceInAuditLogReportDescription(
            descriptionPropertyValues, null, com.autodesk.bsm.pelican.enums.Currency.USD + " (4)", null, AMOUNT, null,
            nameOfNamerPriceList + " (" + namerPriceListId + ")", null, effectiveStartDateInAuditLog, null,
            effectiveEndDateInAuditLog, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests adding Basic Offering with Usage Type. It also validates audit log Testcase validation: it
     * validates if null is passed as Usage Type, it will default it to "Commercial"
     */
    @Test(dataProvider = "basicOfferingInfo")
    public void testAddBasicOffering(final OfferingType offeringType, final String productLine, final String name,
        final String externalKey, final MediaType mediaType, final String offeringDetail, final Status status,
        UsageType usageType) {

        // Add currency and amount only when Offering Type is "Currency"
        if (offeringType == OfferingType.CURRENCY) {
            currency = resource.currency().getById(getEnvironmentVariables().getCloudCurrencyId());
            currencyName = currency.getDescription() + " (" + currency.getName() + ")";
        }
        // Add Basic Offering with the data provided by the data provider
        addBasicOfferingPage.addBasicOfferingInfo(offeringType, productLine, name, externalKey, mediaType,
            offeringDetail, status, usageType, currencyName, AMOUNT);
        basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();

        // Get the basic offering id
        basicOfferingId = basicOfferingDetailPage.getId();

        // Assertions
        AssertCollector.assertThat("Incorrect offering type", basicOfferingDetailPage.getOfferingType(),
            equalTo(offeringType.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line", basicOfferingDetailPage.getProductLine(),
            equalTo(getProductLineExternalKeyRevit()), assertionErrorList);
        AssertCollector.assertThat("Incorrect name", basicOfferingDetailPage.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect external key", basicOfferingDetailPage.getExternalKey(),
            equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering detail", basicOfferingDetailPage.getOfferingDetail(),
            equalTo(PelicanConstants.OFFERING_DETAILS1), assertionErrorList);
        AssertCollector.assertThat("Incorrect status", basicOfferingDetailPage.getStatus(),
            equalTo(status.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Id should not be null", basicOfferingId, notNullValue(), assertionErrorList);

        String mediaTypeFormat = null;
        // Media type is present only when Offering Type is Perpetual
        if (offeringType == OfferingType.PERPETUAL) {
            AssertCollector.assertThat("Incorrect media type", basicOfferingDetailPage.getMediaType(),
                equalTo(mediaType.getValue()), assertionErrorList);
            mediaTypeFormat = mediaType.getDisplayValue();
        }

        // If usage type is not null, then it will be same as what is provided in data provider
        if (usageType == null) {
            usageType = UsageType.COM;
        }
        AssertCollector.assertThat("Incorrect usage type", basicOfferingDetailPage.getUsageType(),
            equalTo(usageType.toString()), assertionErrorList);

        // Assertions on Currency and amount only when Offering Type is Perpetual
        if (offeringType == OfferingType.CURRENCY) {
            AssertCollector.assertThat("Incorrect currency name", basicOfferingDetailPage.getCurrency(),
                equalTo(currency.getName()), assertionErrorList);
            AssertCollector.assertThat("Incorrect amount", basicOfferingDetailPage.getAmount(), equalTo(AMOUNT + ".00"),
                assertionErrorList);

            // Get Currency Amount Entitlement Id
            final String currencyAmountEntitlementId = DbUtils.selectQuery(
                "select ID from SUBSCRIPTION_ENTITLEMENT where RELATED_ID = " + basicOfferingId + " and GRANT_TYPE = 0",
                "ID", getEnvironmentVariables()).get(0);

            // Query Dynamo DB for Currency Amount Entitlement
            SubscriptionPlanAuditLogHelper.assertionsOnCurrencyAmountEntitlement(null, null,
                currencyAmountEntitlementId, null, getEnvironmentVariables().getCloudCurrencyId(), null, AMOUNT,
                Action.CREATE, assertionErrorList);

            // Query Audit Log Report for subscription currency amount entitlement
            final HashMap<String, List<String>> descriptionPropertyValues = auditLogReportHelper
                .verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId,
                    currencyAmountEntitlementId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);
            SubscriptionAndBasicOfferingsAuditLogReportHelper
                .assertionsForCurrencyAmountEntitlementInAuditLogReportDescription(descriptionPropertyValues, null,
                    AMOUNT, null, "CLOUD (" + getEnvironmentVariables().getCloudCurrencyId() + ")", assertionErrorList);
        }

        final String productLineId = DbUtils.selectQuery("select ID from PRODUCT_LINE where APP_FAMILY_ID = 2001 "
            + "and EXTERNAL_KEY = '" + getProductLineExternalKeyRevit() + "'", "ID", getEnvironmentVariables()).get(0);

        final boolean createBasicOfferingAuditLogFound =
            BasicOfferingAuditLogHelper.helperToValidateDynamoDbForBasicOffering(basicOfferingId, null, name, null,
                externalKey, null, offeringType, null, status, null, productLineId, null, mediaTypeFormat, null, null,
                null, getEnvironmentVariables().getOfferingDetailId(), null, null, null, usageType, null, null,
                Action.CREATE, null, assertionErrorList);
        AssertCollector.assertTrue(
            "Create Basic Offering Audit Log not found for Basic Offering id : " + basicOfferingId,
            createBasicOfferingAuditLogFound, assertionErrorList);

        // Verify Audit Log report results for Basic Offering id.
        final HashMap<String, List<String>> descriptionPropertyValues =
            auditLogReportHelper.verifyAuditLogReportResults(null, null, PelicanConstants.ENTITY_BASIC_OFFERING, null,
                basicOfferingId, adminToolUserId, Action.CREATE.toString(), null, assertionErrorList);
        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForBasicOfferingInAuditLogReport(
            descriptionPropertyValues, null, name, null, externalKey, null, offeringType, null, status, null, usageType,
            null, getEnvironmentVariables().getOfferingDetailId(), null, productLineId, null, mediaType,
            getEnvironmentVariables(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the dynamoDB for data corresponding to create and edit basic offering descriptors
     */
    @Test(dataProvider = "getBasicOfferingAndDescriptorData")
    public void testAddAndEditBasicOfferingDescriptorsSuccess(final String groupName, final String fieldName,
        final String apiName, final boolean isLocalDescriptor) {

        final String basicOfferingName = "BasicOffering_" + RandomStringUtils.randomNumeric(6);
        // Add Basic Offering with the data provided by the data provider
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey,
            basicOfferingName, basicOfferingName, MediaType.ELECTRONIC_DOWNLOAD, OFFERING_DETAILS, Status.ACTIVE,
            UsageType.COM, null, null);
        basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();
        basicOfferingId = basicOfferingDetailPage.getId();
        final String descriptorValue = "BasicOfferDescriptor FirstTimeValue";
        final String modifiedDescriptorValue = "BasicOfferDescriptor modifiedValue";

        // Get Id or Add Descriptor in Basic Offering and get Descriptor Id from
        // Admin Tool
        Descriptor descriptor;
        String descriptorId = DescriptorUtils.getExistingDescriptorId(adminToolPage, findDescriptorDefinitionsPage,
            DescriptorEntityTypes.BASIC_OFFERING, groupName, fieldName);
        // Descriptor is not present and hence add the descriptor
        if (descriptorId == null) {
            if (isLocalDescriptor) {
                descriptor = DescriptorUtils.getDescriptorData(DescriptorEntityTypes.BASIC_OFFERING, groupName,
                    fieldName, apiName, PelicanConstants.YES, MAX_DESCRIPTOR_LENGTH);
            } else {
                descriptor = DescriptorUtils.getDescriptorData(DescriptorEntityTypes.BASIC_OFFERING, groupName,
                    fieldName, apiName, PelicanConstants.NO, MAX_DESCRIPTOR_LENGTH);
            }
            addDescriptorPage.addDescriptor(descriptor);
            final DescriptorDefinitionDetailPage descriptorDetailPage =
                new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
            descriptorId = descriptorDetailPage.getDescriptorEntityFromDetails().getId();
        }

        findBasicOfferingPage.findBasicOfferingById(basicOfferingId);
        if (isLocalDescriptor) {
            basicOfferingDetailPage.editLocalizedDescriptorValue(descriptorValue);
            basicOfferingDetailPage.editLocalizedDescriptorValue(modifiedDescriptorValue);
        } else {
            basicOfferingDetailPage.editNonLocalizedDescriptorValue(descriptorValue);
            basicOfferingDetailPage.editNonLocalizedDescriptorValue(modifiedDescriptorValue);
        }

        final String descriptorIdFromTable =
            DbUtils.getOfferDescriptorIdFromTable(basicOfferingId, getEnvironmentVariables());
        LOGGER.info("Descriptor ID from table: " + descriptorIdFromTable);
        final String language = isLocalDescriptor ? PelicanConstants.LANGUAGE_EN : PelicanConstants.EMPTY_STRING;

        BasicOfferingAuditLogHelper.validateBasicOfferingDescriptors(descriptorIdFromTable, basicOfferingId, null,
            getEnvironmentVariables().getAppFamilyId(), null, descriptorId, null, descriptorValue, null, language, null,
            PelicanConstants.EMPTY_STRING, Action.CREATE, assertionErrorList);

        BasicOfferingAuditLogHelper.validateBasicOfferingDescriptors(descriptorIdFromTable, basicOfferingId, null, null,
            null, null, descriptorValue, modifiedDescriptorValue, null, null, null, null, Action.UPDATE,
            assertionErrorList);

        // Verify Audit Log report results for Basic Offering descriptor id.
        HashMap<String, List<String>> descriptionPropertyValue = auditLogReportHelper.verifyAuditLogReportResults(null,
            null, PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId, descriptorIdFromTable, adminToolUserId,
            Action.CREATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
            descriptionPropertyValue, descriptorId, null, descriptorValue, fieldName, Action.CREATE,
            assertionErrorList);

        descriptionPropertyValue = auditLogReportHelper.verifyAuditLogReportResults(null, null,
            PelicanConstants.ENTITY_BASIC_OFFERING, basicOfferingId, descriptorIdFromTable, adminToolUserId,
            Action.UPDATE.toString(), null, assertionErrorList);

        SubscriptionAndBasicOfferingsAuditLogReportHelper.assertionsForSubscriptionPlanOfferDescriptorsInAuditLogReport(
            descriptionPropertyValue, descriptorId, descriptorValue, modifiedDescriptorValue, fieldName, Action.UPDATE,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * DataProvider to pass local and non-local descriptor data to testCreateAndEditSubscriptionPlanDescriptorsSuccess
     * and testCreateAndEditSubscriptionOfferDescriptorsSuccess methods
     */
    @DataProvider(name = "getBasicOfferingAndDescriptorData")
    public Object[][] getBasicOfferingAndDescriptorData() {
        return new Object[][] {
                { PelicanConstants.IPP, "AUTO_TEST_LOCAL_DESCRIPTOR", "AUTO_TEST_LOCAL_DESCRIPTOR_API", true },
                { PelicanConstants.ESTORE, "AUTO_TEST_DESCRIPTOR", "AUTO_TEST_DESCRIPTOR_API", false } };
    }

    /**
     * This is a test which will test whether product line is a required field in add basic offering
     */
    @Test
    public void testProductLineAsRequiredInAddBasicOffering() {

        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, null, RandomStringUtils.randomAlphanumeric(8),
            RandomStringUtils.randomAlphanumeric(8), MediaType.DVD, null, null, null, null, null);
        addBasicOfferingPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final String productLineErrorMessage = addBasicOfferingPage.getProductLineErrorMessage();
        commonAssertionsForErrorMessages(productLineErrorMessage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data Provider to Create Basic Offering
     *
     * @return Basic Offering Info Data
     */
    @DataProvider(name = "basicOfferingInfo")
    private static Object[][] provideBasicOfferingInfo() {
        return new Object[][] {
                { OfferingType.PERPETUAL, productLineNameAndExternalKey,
                        "BasicOffering_" + RandomStringUtils.randomNumeric(6),
                        "BasicOffering_ExternalKey_" + RandomStringUtils.randomNumeric(6),
                        MediaType.ELECTRONIC_DOWNLOAD, OFFERING_DETAILS, Status.NEW, UsageType.COM },
                { OfferingType.CURRENCY, productLineNameAndExternalKey,
                        "BasicOffering_" + RandomStringUtils.randomNumeric(6),
                        "BasicOffering_ExternalKey_" + RandomStringUtils.randomNumeric(6), null, OFFERING_DETAILS,
                        Status.ACTIVE, UsageType.COM } };
    }

    /**
     * This is a test method which will generate the random external key if no external key is provided by the user when
     * adding a basic offering in the admin tool
     */
    @Test(dataProvider = "BasicOfferingStatuses")
    public void testRandomExternalKeyGeneratedInAddBasicOffering(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, null,
            MediaType.DVD, null, status, null, null, null);
        basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();
        final HashMap<String, String> offeringFieldsMap = getFieldsFromBasicOffering(basicOfferingDetailPage);
        commonAssertionsForBasicOfferingFields(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, null,
            MediaType.DVD, null, status, offeringFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the user entered external key if external key is provided by the user
     * when adding a basic offering in the admin tool
     */
    @Test(dataProvider = "BasicOfferingStatuses")
    public void testManuallyEnteredExternalKeyGenerationInAddBasicOffering(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, name,
            MediaType.DVD, null, status, null, null, null);
        basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();
        final HashMap<String, String> offeringFieldsMap = getFieldsFromBasicOffering(basicOfferingDetailPage);
        commonAssertionsForBasicOfferingFields(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, name,
            MediaType.DVD, null, status, offeringFieldsMap);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will generate the user entered external key if external key is provided by the user
     * when adding a basic offering in the admin tool
     */
    @Test(dataProvider = "BasicOfferingStatuses")
    public void testDuplicateExternalKeyInAddBasicOffering(final Status status) {

        final String name = RandomStringUtils.randomAlphanumeric(8);
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, name,
            MediaType.DVD, null, status, null, null, null);
        basicOfferingDetailPage = addBasicOfferingPage.clickOnSave();
        // create a basic offering
        addBasicOfferingPage.addBasicOfferingInfo(OfferingType.PERPETUAL, productLineNameAndExternalKey, name, name,
            MediaType.DVD, null, status, null, null, null);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        addBasicOfferingPage.clickOnSave();
        final String externalKeyErrorMessage = addBasicOfferingPage.getExternalKeyErrorMessage();
        AssertCollector.assertThat("Incorrect external key error message for duplicate basic offering external key",
            externalKeyErrorMessage, equalTo(PelicanErrorConstants.DUPLICATE_VALUE_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify Product Line with In-Active State won't show up under drop down menu while adding to Basic
     * Offering.
     */
    @Test
    public void testInActiveProductLineIsNotVisibleToSelectForAddBasicOffering() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";
        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.NO);
        addProductLinePage.clickOnSubmit();

        Util.waitInSeconds(TimeConstants.THREE_SEC);
        addBasicOfferingPage.navigateToAddBasicOffering();
        final List<String> productLineList = addBasicOfferingPage.getProductLinesListUnderProductLineDropDown();
        final boolean isProductLinePresent = productLineList.contains(prodLineNameAndExternalKey);
        AssertCollector.assertFalse("Product Line should not be visible but it is visible", isProductLinePresent,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that after editing Product Line with In-Active State to active State it will show up under drop
     * down menu while adding to Basic Offering.
     */
    @Test
    public void testAfterEditingInActiveProductLineToActiveItsVisibleForAddBasicOffering() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String prodLineNameAndExternalKey = name + " (" + externalKey + ")";
        addProductLinePage.addProductLine(name, externalKey, PelicanConstants.NO);

        productLineDetailsPage = addProductLinePage.clickOnSubmit();
        final String productLineId = productLineDetailsPage.getId();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        addBasicOfferingPage.navigateToAddBasicOffering();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<String> productLineList = addBasicOfferingPage.getActiveProductLine();
        final boolean isProductLinePresent = productLineList.contains(prodLineNameAndExternalKey);
        AssertCollector.assertFalse("Product Line should not be visible but it is visible", isProductLinePresent,
            assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidId(productLineId);
        // Edit ProductLine for Active Status.
        productLineDetailsPage.clickOnEdit();
        productLineDetailsPage.selectActiveStatus(PelicanConstants.YES);
        productLineDetailsPage.submit(TimeConstants.ONE_SEC);

        HelperForProductLine.assertNameAndExternalKey(productLineDetailsPage, name, externalKey, PelicanConstants.YES,
            assertionErrorList);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        addBasicOfferingPage.navigateToAddBasicOffering();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<String> productLineList1 = addBasicOfferingPage.getActiveProductLine();
        final boolean isProductLinePresent1 = productLineList1.contains(prodLineNameAndExternalKey);
        AssertCollector.assertTrue("Product Line should not be visible but it is visible", isProductLinePresent1,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the common method for assertions of the required fields on the basic offering page
     *
     * @param productLineErrorMessage - Error message displayed on the product line field
     */
    private void commonAssertionsForErrorMessages(final String productLineErrorMessage) {
        AssertCollector.assertThat("Incorrect error message for required product line field", productLineErrorMessage,
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method stores the fields in a basic offering into a hashmap
     *
     * @return HashMap<String,String>
     */
    private HashMap<String, String> getFieldsFromBasicOffering(final BasicOfferingDetailPage basicOfferingDetailPage) {

        final HashMap<String, String> offeringFieldsMap = new HashMap<>();
        offeringFieldsMap.put("Id", basicOfferingDetailPage.getId());
        offeringFieldsMap.put("Name", basicOfferingDetailPage.getName());
        offeringFieldsMap.put("ExternalKey", basicOfferingDetailPage.getExternalKey());
        offeringFieldsMap.put("OfferingType", basicOfferingDetailPage.getOfferingType());
        offeringFieldsMap.put("Status", basicOfferingDetailPage.getStatus());
        offeringFieldsMap.put("MediaType", basicOfferingDetailPage.getMediaType());
        offeringFieldsMap.put("UsageType", basicOfferingDetailPage.getUsageType());
        offeringFieldsMap.put("ProductLine", basicOfferingDetailPage.getProductLine());
        offeringFieldsMap.put("OfferingDetail", basicOfferingDetailPage.getOfferingDetail());

        return offeringFieldsMap;
    }

    /**
     * This is a method which will assert the fields on the basic offering page
     */
    private void commonAssertionsForBasicOfferingFields(final OfferingType offeringType, final String productLine,
        final String name, final String externalKey, final MediaType mediaType, final String offeringDetail,
        final Status status, final HashMap<String, String> offeringFieldsMap) {

        AssertCollector.assertThat("Incorrect id in the basic offering", offeringFieldsMap.get("Id"), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect name of the basic offering", offeringFieldsMap.get("Name"), equalTo(name),
            assertionErrorList);
        if (externalKey == null) {
            AssertCollector.assertThat("Incorrect external key prefix for the basic plan",
                offeringFieldsMap.get("ExternalKey").split("-")[0], equalTo("BO"), assertionErrorList);
            AssertCollector.assertThat("Incorrect external key length for the basic offering",
                offeringFieldsMap.get("ExternalKey").split("-")[1].length(), equalTo(EXTERNAL_KEY_LENGTH),
                assertionErrorList);

        } else {
            AssertCollector.assertThat("Incorrect external key of the basic offering",
                offeringFieldsMap.get("ExternalKey"), equalTo(externalKey), assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect offering type of the basic offering",
            offeringFieldsMap.get("OfferingType"), equalTo(offeringType.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status of the basic offering", offeringFieldsMap.get("Status"),
            equalTo(status.getDisplayName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect media type of the basic offering", offeringFieldsMap.get("MediaType"),
            equalTo(mediaType.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering detail of the basic offering",
            offeringFieldsMap.get("OfferingDetail"), equalTo(offeringDetail), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line of the basic offering",
            offeringFieldsMap.get("ProductLine").split(" ")[0], equalTo(productLine.split(" ")[0]), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is the data provider which provides the different statuses such as new and active to a basic offering tests
     */
    @DataProvider(name = "BasicOfferingStatuses")
    public Object[][] getBasicOfferingStatuses() {
        return new Object[][] { { Status.NEW }, { Status.ACTIVE } };
    }
}
