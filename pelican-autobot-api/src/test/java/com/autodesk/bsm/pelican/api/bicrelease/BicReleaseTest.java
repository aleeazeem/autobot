package com.autodesk.bsm.pelican.api.bicrelease;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AllOf.allOf;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.EligibleProduct;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.EligibleVersions;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.ProductLineEligibleProduct;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.BicRelease;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.bicrelease.BicReleasePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.ItemUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test Bic Release's API: productLine/eligibleVersions.
 * <p>
 * This test suite will ran after the Admin Tool to ensure data is available. Also, this test class will ran in AUTO
 * instead of Demo application family
 *
 * @author yin
 */
public class BicReleaseTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private EligibleVersions existingVersions;
    private int numberOfExistingProductLines;
    private static List<String> productLineExternalKeyList;
    private static SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private Offerings icBicOfferings1;
    private BicReleasePage bicReleaseAddPage;
    private List<String> versionsList = new ArrayList<>();
    private ItemUtils itemUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(BicReleaseTest.class.getSimpleName());

    /**
     * Data setup - create Product Lines if not available
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        initializeDriver(getEnvironmentVariables());
        itemUtils = new ItemUtils(getEnvironmentVariables());

        // Instantiate admin tool
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        bicReleaseAddPage = adminToolPage.getPage(BicReleasePage.class);

        // create Product Line.
        productLineDataSetup();

        // Create BIC Release And Add it.
        final List<BicRelease> newBicReleases = createBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            bicReleaseAddPage.add(bicRelease);
        }
        // Get Existing Version Available.
        existingVersions = resource.bicRelease().getEligibleVersions();
        numberOfExistingProductLines = existingVersions.getProducts().size();
        versionsList = new ArrayList<>();
    }

    @AfterClass
    public void tearDown() {
        final String updateQuery = PelicanDbConstants.UPDATE_BIC_RELEASE_TABLE_STATUS
            + PelicanDbConstants.APP_FAMILY_ID_CONDITION_IN_QUERY + getEnvironmentVariables().getAppFamilyId();
        DbUtils.updateQuery(updateQuery, getEnvironmentVariables());

    }

    /**
     * Validate productLine/eligibleVersions with no parameters return the valid product lines
     *
     * @Result: All eligible versions returned
     */
    @Test
    public void noQueryParameters() {

        final EligibleVersions eligibleVersions = resource.bicRelease().getEligibleVersions();
        AssertCollector.assertThat("Unable to get product line's eligible versions", eligibleVersions,
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate productLine/eligibleVersions with 1 valid parameter return the valid product lines
     *
     * @Result: Specified product line is returned
     */
    @Test
    public void oneValidProductLineCode() {

        AssertCollector.assertThat("No existing product line with eligible versions", existingVersions, notNullValue(),
            assertionErrorList);
        final String productLineCode = existingVersions.getProducts().get(0).getProductLineCode();
        final EligibleVersions version = resource.bicRelease().getEligibleVersions(Arrays.asList(productLineCode));
        final String productLineName = version.getProducts().get(0).getProductLineName();
        AssertCollector.assertThat("Incorrect product line name in the api response", productLineName,
            is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Unable to query by product line code", version, is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate productLine/eligibleVersions with 2 valid parameter return the valid product lines
     *
     * @Result: Specified product line is returned
     */
    @Test
    public void multipleValidProductLineCodes() {

        AssertCollector.assertThat("No existing product line with eligible versions", existingVersions, notNullValue(),
            assertionErrorList);
        final List<String> productLineCodes = new ArrayList<>();
        for (int i = 0; i < numberOfExistingProductLines; i++) {
            final String productLineCode = existingVersions.getProducts().get(i).getProductLineCode();
            productLineCodes.add(productLineCode);
        }
        final EligibleVersions versions = resource.bicRelease().getEligibleVersions(productLineCodes);
        AssertCollector.assertThat("Unable to query by multiple product line code", versions, notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of eligible product lines", versions.getProducts().size(),
            equalTo(numberOfExistingProductLines), assertionErrorList);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate productLine/eligibleVersions with 2 valid and 1 invalid parameter return the valid product lines
     *
     * @Result: Specified product line is returned
     */
    @Test
    public void oneInvalidProductLineCodes() {

        AssertCollector.assertThat("No existing product line with eligible versions", existingVersions, notNullValue(),
            assertionErrorList);

        // Prep valid product line code
        final List<String> productLineCodes = new ArrayList<>();
        for (int i = 0; i < numberOfExistingProductLines; i++) {
            final String productLineCode = existingVersions.getProducts().get(i).getProductLineCode();
            productLineCodes.add(productLineCode);
        }

        // prep invalid product line code
        productLineCodes.add("invalidCode1");

        final EligibleVersions versions = resource.bicRelease().getEligibleVersions(productLineCodes);
        AssertCollector.assertThat("Unable to query by multiple product line code", versions, is(notNullValue()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of eligible product lines", versions.getProducts().size(),
            equalTo(numberOfExistingProductLines), assertionErrorList);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate productLine/eligibleVersions with 2 invalid parameters
     *
     * @Result:
     */
    @Test
    public void onlyInvalidProductLineCodes() {

        final HttpError error =
            resource.bicRelease().getEligibleVersions(Arrays.asList("invalidCode1", "invalidCode2"));
        AssertCollector.assertThat("Incorrect error code", error.getStatus(), is(HttpStatus.SC_NO_CONTENT),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void onlyDates() {

        String expStartDate;
        String expEndDate;
        final String productLineCode = null;

        LOGGER.info("==== Advanced Search for date range of today ====");
        expStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_HYPHEN);

        EligibleVersions versions = resource.bicRelease().getEligibleVersions(expStartDate, null);
        validateFcsDate(versions, productLineCode, expStartDate, null);

        LOGGER.info("==== Advanced Search for date range of today + 1 ====");
        expStartDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 1);

        versions = resource.bicRelease().getEligibleVersions(expStartDate, null);
        validateFcsDate(versions, productLineCode, expStartDate, null);

        LOGGER.info("==== Advanced Search for date ending by today + 2 ====");
        expEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 2);

        versions = resource.bicRelease().getEligibleVersions(null, expEndDate);
        validateFcsDate(versions, productLineCode, null, expEndDate);

        LOGGER.info("==== Advanced Search for date (today - 2, today + 6) ====");
        expStartDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 2);
        expEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 6);

        versions = resource.bicRelease().getEligibleVersions(expStartDate, expEndDate);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        validateFcsDate(versions, productLineCode, expStartDate, expEndDate);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void productLineCodesAndDatesForToday() {

        String expStartDate;

        // Get a product line with today as FCS Date
        expStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_HYPHEN);

        EligibleVersions versions = resource.bicRelease().getEligibleVersions(expStartDate, null);
        if (versions.getProducts().size() == 0) {
            throw new RuntimeException("Unable to find any product lines");
        }
        final String productLineCode = versions.getProducts().get(0).getProductLineCode();

        // 1 valid and 1 invalid
        final List<String> productLineCodes = Arrays.asList(productLineCode, "somethingInvalid");

        LOGGER.info("==== Advanced Search for date range of today ====");
        expStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_HYPHEN);

        versions = resource.bicRelease().getEligibleVersions(productLineCodes, expStartDate, null);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        validateFcsDate(versions, productLineCode, expStartDate, null);

        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void productLineCodesAndDatesForTomorrow() {

        String expStartDate;
        // Get a product line with tomorrow as FCS Date
        expStartDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 1);

        EligibleVersions versions = resource.bicRelease().getEligibleVersions(expStartDate, null);
        if (versions.getProducts().size() == 0) {
            throw new RuntimeException("Unable to find any product lines");
        }
        final String productLineCode = versions.getProducts().get(0).getProductLineCode();

        // 1 valid and 1 invalid
        final List<String> productLineCodes = Arrays.asList(productLineCode, "somethingInvalid");

        LOGGER.info("==== Advanced Search for date range of today + 1 ====");
        versions = resource.bicRelease().getEligibleVersions(productLineCodes, expStartDate, null);
        validateFcsDate(versions, productLineCode, expStartDate, null);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void productLineCodesAndEndDate() {

        final String expStartDate = null;
        String expEndDate;

        // Get a product line with FCS Date is 2 days later
        expEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 2);

        EligibleVersions versions = resource.bicRelease().getEligibleVersions(null, expEndDate);
        if (versions.getProducts().size() == 0) {
            throw new RuntimeException("Unable to find any product lines");
        }
        final String productLineCode = versions.getProducts().get(0).getProductLineCode();

        // 1 valid and 1 invalid
        final List<String> productLineCodes = Arrays.asList(productLineCode, "somethingInvalid");

        LOGGER.info("==== Advanced Search for date ending by today + 2 ====");
        versions = resource.bicRelease().getEligibleVersions(productLineCodes, expStartDate, expEndDate);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        validateFcsDate(versions, productLineCode, null, expEndDate);
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void productLineCodesAndDatesForRange() {

        String expStartDate;
        String expEndDate;
        // Get a product line with the FCS Date range
        expStartDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 2);
        expEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_HYPHEN, 6);

        EligibleVersions versions = resource.bicRelease().getEligibleVersions(expStartDate, expEndDate);
        if (versions.getProducts().size() == 0) {
            throw new RuntimeException("Unable to find any product lines");
        }
        final String productLineCode = versions.getProducts().get(0).getProductLineCode();

        // 1 valid and 1 invalid
        final List<String> productLineCodes = Arrays.asList(productLineCode, "somethingInvalid");

        LOGGER.info("==== Advanced Search for date (today - 2, today + 6) ====");

        versions = resource.bicRelease().getEligibleVersions(productLineCodes, expStartDate, expEndDate);
        for (final ProductLineEligibleProduct eligibleProduct : versions.getProducts()) {
            AssertCollector.assertThat("Incorrect product line name in the api response",
                eligibleProduct.getProductLineName(), is(notNullValue()), assertionErrorList);
        }
        validateFcsDate(versions, productLineCode, expStartDate, expEndDate);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Invalid leap year
     *
     * @result: error
     */
    @Test
    public void leapDate() {

        final String date = "02-29-2015";
        final HttpError error = resource.bicRelease().getEligibleVersions(date, null);
        AssertCollector.assertThat("Invalid leap year should generate error condition", error.getStatus(),
            is(HttpStatus.SC_BAD_REQUEST), assertionErrorList);
        AssertCollector.assertThat("Incorrect error code", error.getErrorCode(), equalTo(990002), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message", error.getErrorMessage(),
            equalTo("Cannot parse \"" + date + "\": Value 29 for dayOfMonth must be in the range [1,28]"),
            assertionErrorList);
        LOGGER.info("Message: " + error.getReason());
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test get eligible verisions api for two plans with packaging type offering with different product line code
     *
     * @result: return all the previous versions of the bic releases for all the features in the offering
     */
    @Test(dataProvider = "getPackagingTypes")
    public void testGetEligibleVersionsApiForTwoPlansWithPackagingTypesOfDifferentProductLineCodes(
        final PackagingType packagingType) {

        // Creating a offering for BIC and meta
        final String productLineExternalKey = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
        icBicOfferings1 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), packagingType);
        final Offerings icBicOfferings3 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), packagingType);

        final String productLineExternalKey1 = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);
        final Offerings icBicOfferings2 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), PackagingType.IC);

        final String itemName1 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName1);

        final Item item1 = itemUtils.addItem(itemName1, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName1);

        BicRelease bicRelease = createBicRelease(itemName1);
        versionsList.clear();
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName1);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName1);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName2 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName2);

        final Item item2 = itemUtils.addItem(itemName2, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName2);

        bicRelease = createBicRelease(itemName2);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName2);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName2);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName3 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName3);

        final Item item3 = itemUtils.addItem(itemName3, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName3);

        bicRelease = createBicRelease(itemName3);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName3);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName3);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName4 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName4);

        final Item item4 = itemUtils.addItem(itemName4, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName4);

        bicRelease = createBicRelease(itemName4);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName4);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName4);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName5 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName5);

        final Item item5 = itemUtils.addItem(itemName5, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName5);

        bicRelease = createBicRelease(itemName5);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName5);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName5);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName6 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName6);

        final Item item6 = itemUtils.addItem(itemName6, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName6);

        bicRelease = createBicRelease(itemName6);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName6);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName6);
        versionsList.add(bicRelease.getDownloadRelease());

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings1.getOffering().getId(),
            item1.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings1.getOffering().getId(),
            item2.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings1.getOffering().getId(),
            item3.getId(), null, true);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings3.getOffering().getId(),
            item1.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings3.getOffering().getId(),
            item2.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings3.getOffering().getId(),
            item3.getId(), null, true);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings2.getOffering().getId(),
            item4.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings2.getOffering().getId(),
            item5.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings2.getOffering().getId(),
            item6.getId(), null, true);

        final EligibleVersions version =
            resource.bicRelease().getEligibleVersions(Arrays.asList(productLineExternalKey, productLineExternalKey1));

        AssertCollector.assertThat("Incorrect number of productline codes returned in the api response",
            version.getProducts().size(), equalTo(6), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release1",
            version.getProducts().get(0).getProductLineCode(),
            isOneOf(itemName1, itemName2, itemName3, itemName4, itemName5, itemName6), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline name for bic release1",
            version.getProducts().get(0).getProductLineName(),
            isOneOf(itemName1, itemName2, itemName3, itemName4, itemName5, itemName6), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release2",
            version.getProducts().get(1).getProductLineCode(),
            isOneOf(itemName1, itemName2, itemName3, itemName4, itemName5, itemName6), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline name for bic release2",
            version.getProducts().get(1).getProductLineName(),
            isOneOf(itemName1, itemName2, itemName3, itemName4, itemName5, itemName6), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release3",
            version.getProducts().get(2).getProductLineCode(),
            isOneOf(itemName1, itemName2, itemName3, itemName4, itemName5, itemName6), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline name for bic release1",
            version.getProducts().get(2).getProductLineName(),
            isOneOf(itemName1, itemName2, itemName3, itemName4, itemName5, itemName6), assertionErrorList);

        for (final ProductLineEligibleProduct productLineEligibleProduct : version.getProducts()) {

            for (final EligibleProduct eligibleProduct : productLineEligibleProduct.getEligibleProducts()) {
                AssertCollector.assertThat("Incorrect version1 for bic release",
                    eligibleProduct.getVersions().get(0).getVersion(), isOneOf(versionsList.toArray()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect version2 for bic release",
                    eligibleProduct.getVersions().get(1).getVersion(), isOneOf(versionsList.toArray()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect version3 for bic release",
                    eligibleProduct.getVersions().get(2).getVersion(), isOneOf(versionsList.toArray()),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test get eligible verisions api for one offering with packaging type and one invalid product line code
     *
     * @result: return all the previous versions of the bic releases for all the features in the offering
     */
    @Test(dataProvider = "getPackagingTypes")
    public void testGetEligibleVersionsApiForPlanWithPackagingTypeAndInvalidProductLineCode(
        final PackagingType packagingType) {

        // Creating a offering for BIC and meta
        final String productLineExternalKey = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey);
        icBicOfferings1 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), packagingType);

        final String itemName1 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName1);

        final Item item1 = itemUtils.addItem(itemName1, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName1);

        BicRelease bicRelease = createBicRelease(itemName1);
        versionsList.clear();
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName1);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName1);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName2 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName2);

        final Item item2 = itemUtils.addItem(itemName2, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName2);

        bicRelease = createBicRelease(itemName2);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName2);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName2);
        versionsList.add(bicRelease.getDownloadRelease());

        final String itemName3 = getRandomString();
        subscriptionPlanApiUtils.addProductLine(itemName3);

        final Item item3 = itemUtils.addItem(itemName3, getEnvironmentVariables().getAppId(), getBuyerUser().getId(),
            getItemTypeId(), itemName3);

        bicRelease = createBicRelease(itemName3);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName3);
        versionsList.add(bicRelease.getDownloadRelease());
        bicRelease = createBicRelease(itemName3);
        versionsList.add(bicRelease.getDownloadRelease());

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings1.getOffering().getId(),
            item1.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings1.getOffering().getId(),
            item2.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(icBicOfferings1.getOffering().getId(),
            item3.getId(), null, true);

        final EligibleVersions version =
            resource.bicRelease().getEligibleVersions(Arrays.asList(productLineExternalKey, "abdvvdgggd"));

        AssertCollector.assertThat("Incorrect number of productline codes returned in the api response",
            version.getProducts().size(), equalTo(3), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release1",
            version.getProducts().get(0).getProductLineCode(), isOneOf(itemName1, itemName2, itemName3),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect productline name for bic release1",
            version.getProducts().get(0).getProductLineName(), isOneOf(itemName1, itemName2, itemName3),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release2",
            version.getProducts().get(1).getProductLineCode(), isOneOf(itemName1, itemName2, itemName3),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect productline name for bic release2",
            version.getProducts().get(1).getProductLineName(), isOneOf(itemName1, itemName2, itemName3),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release3",
            version.getProducts().get(2).getProductLineCode(), isOneOf(itemName1, itemName2, itemName3),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect productline name for bic release2",
            version.getProducts().get(2).getProductLineName(), isOneOf(itemName1, itemName2, itemName3),
            assertionErrorList);

        for (final ProductLineEligibleProduct productLineEligibleProduct : version.getProducts()) {

            for (final EligibleProduct eligibleProduct : productLineEligibleProduct.getEligibleProducts()) {
                AssertCollector.assertThat("Incorrect version1 for bic release",
                    eligibleProduct.getVersions().get(0).getVersion(), isOneOf(versionsList.toArray()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect version2 for bic release",
                    eligibleProduct.getVersions().get(1).getVersion(), isOneOf(versionsList.toArray()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect version3 for bic release",
                    eligibleProduct.getVersions().get(2).getVersion(), isOneOf(versionsList.toArray()),
                    assertionErrorList);
            }
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test get eligible verisions api for one invalid product line code
     *
     * @result: return empty set of bic releases versions
     */
    @Test
    public void testGetEligibleVersionsApiForInvalidProductLineCode() {

        final Object apiResponse = resource.bicRelease().getEligibleVersions(Arrays.asList("abdvvdgggd"));
        AssertCollector.assertThat("Incorrect object type is returned in the response", apiResponse,
            instanceOf(HttpError.class), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    private void validateFcsDate(final EligibleVersions versions, final String productLineCode, final String expStart,
        final String expEnd) {

        final Date expStartDate = DateTimeUtils.convertStringToDate(expStart, PelicanConstants.DATE_FORMAT_WITH_HYPHEN);
        final Date expEndDate = DateTimeUtils.convertStringToDate(expEnd, PelicanConstants.DATE_FORMAT_WITH_HYPHEN);

        AssertCollector.assertThat("Unable to find eligible versions", versions.getProducts().size(),
            greaterThanOrEqualTo(1), assertionErrorList);

        for (final ProductLineEligibleProduct product : versions.getProducts()) {
            if (productLineCode != null) {
                AssertCollector.assertThat("Incorrect product line code", product.getProductLineCode(),
                    equalTo(productLineCode), assertionErrorList);
            }
            for (final EligibleProduct eligibleProduct : product.getEligibleProducts()) {
                for (int versionIndex = 0; versionIndex < eligibleProduct.getVersions().size(); versionIndex++) {
                    final String message = eligibleProduct.getEligibleProductLineCode() + "'s version #"
                        + eligibleProduct.getVersions().get(versionIndex).getVersion() + ": FCS date is out of range";
                    if (expStartDate != null && expEndDate != null) {
                        AssertCollector.assertThat(message, eligibleProduct.getVersions().get(versionIndex),
                            hasProperty("fcsDate",
                                allOf(greaterThanOrEqualTo(expStartDate), lessThanOrEqualTo(expEndDate))),
                            assertionErrorList);
                    } else if (expStartDate != null) {
                        AssertCollector.assertThat(message, eligibleProduct.getVersions().get(versionIndex),
                            hasProperty("fcsDate", greaterThanOrEqualTo(expStartDate)), assertionErrorList);
                    } else {
                        AssertCollector.assertThat(message, eligibleProduct.getVersions().get(versionIndex),
                            hasProperty("fcsDate", lessThanOrEqualTo(expEndDate)), assertionErrorList);
                    }
                }
            }
        }
    }

    private List<ProductLine> productLineDataSetup() {

        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        productLineExternalKeyList = new ArrayList<>();

        final List<ProductLine> productLineLists = new ArrayList<>();
        for (int i = 0; i < 3; i++) {

            final String productLineExternalKey =
                PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
            productLineExternalKeyList.add(productLineExternalKey);
            productLineLists.add(subscriptionPlanApiUtils.addProductLine(productLineExternalKey));
        }
        return productLineLists;
    }

    /**
     * Create 3 bic release instances for test cases
     */
    private static List<BicRelease> createBicReleases() {

        final List<BicRelease> bicReleases = new ArrayList<>();
        BicRelease bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(productLineExternalKeyList.get(0));
        bicRelease.setDownloadProductLine(productLineExternalKeyList.get(0));
        bicRelease.setDownloadRelease("Rel" + String.valueOf(new Date().getTime()));
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(true);
        bicRelease.setFcsDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));
        bicRelease.setIgnoredEmailNotification(true);
        bicReleases.add(bicRelease);

        bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(productLineExternalKeyList.get(2));
        bicRelease.setDownloadProductLine(productLineExternalKeyList.get(1));
        bicRelease.setDownloadRelease("Rel" + String.valueOf(new Date().getTime()));
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(true);
        bicRelease.setLegacySku("some legacy sku 1");
        bicRelease.setIgnoredEmailNotification(true);
        bicReleases.add(bicRelease);

        bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(productLineExternalKeyList.get(1));
        bicRelease.setDownloadProductLine(productLineExternalKeyList.get(0));
        bicRelease.setDownloadRelease("Rel" + String.valueOf(new Date().getTime()));
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(false);
        bicRelease.setLegacySku("some legacy sku 2");
        bicRelease.setFcsDate(DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 5));
        bicRelease.setIgnoredEmailNotification(false);
        bicReleases.add(bicRelease);

        return bicReleases;
    }

    private String getRandomString() {
        return (RandomStringUtils.randomAlphanumeric(6));
    }

    private BicRelease createBicRelease(final String subscrtiptionPlanProdLine) {

        BicRelease bicRelease1 = new BicRelease();
        final String release = RandomStringUtils.randomAlphanumeric(4);
        bicRelease1.setDownloadRelease(release);
        bicRelease1.setSubsPlanProductLine(subscrtiptionPlanProdLine);
        bicRelease1.setDownloadProductLine(subscrtiptionPlanProdLine);
        bicRelease1.setStatus(Status.ACTIVE);
        bicRelease1.setClic(true);
        bicRelease1.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease1.setIgnoredEmailNotification(false);
        bicRelease1 = bicReleaseAddPage.add(bicRelease1);

        return bicRelease1;
    }

    /**
     * DataProvider for packagingType
     *
     */
    @DataProvider(name = "getPackagingTypes")
    public Object[][] getPackagingTypes() {
        return new Object[][] { { PackagingType.IC }, { PackagingType.VG } };
    }
}
