package com.autodesk.bsm.pelican.ui.bicrelease;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.BicReleaseClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.BicVersion;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.EligibleProduct;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.EligibleVersions;
import com.autodesk.bsm.pelican.api.pojos.bicrelease.ProductLineEligibleProduct;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.BicRelease;
import com.autodesk.bsm.pelican.ui.entities.BicReleaseAdvSearch;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.bicrelease.BicReleasePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.ItemUtils;
import com.autodesk.bsm.pelican.util.PropertiesComparator;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Bic Release Admin Test Access by Subscriptions | BIC Releases Assume the following application family and
 * applications are pre-created: localHost and Dev: Auto Stage: ???
 *
 * @author yin
 */
public class BicReleaseTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private List<String> productLineNameList;
    private List<String> productLineExternalKeyList;
    private static String utcSuffix;
    private static final List<BicRelease> newlyAddedBicReleases = new ArrayList<>();
    private String bicReleaseId;
    private PelicanPlatform resource;
    private ItemUtils itemUtils;
    private BicReleasePage bicReleaseAddPage;

    private static final Logger LOGGER = LoggerFactory.getLogger(BicReleaseTest.class.getSimpleName());

    /**
     * Data setup - create Product Lines if not available
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());

        utcSuffix = " " + getEnvironmentVariables().getTimezoneOffset();

        // Instantiate admin tool
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        productLineDataSetup();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        itemUtils = new ItemUtils(getEnvironmentVariables());
        bicReleaseAddPage = adminToolPage.getPage(BicReleasePage.class);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DbUtils.updateTableInDb("BIC_RELEASE", "IS_ACTIVE", "0", "DATE(CREATED)", "DATE(NOW())",
            getEnvironmentVariables());
    }

    /**
     * Validate items are correctly populated in dropdowns for the addForm <br>
     * - application family - contains only the logged in application family <br>
     * - application - contains all applications per appl family <br>
     * - subscription plan product line - contains only the product lines defined <br>
     * - download product line - contains only the product lines defined <br>
     * - status - active and inactive
     * <p>
     * <p>
     * Result: all dropdowns have correct items
     */
    // @Test
    public void addLayout() {

        final List<String> expProductLines = new ArrayList<>();
        expProductLines.add("-- SELECT ONE --");
        expProductLines.addAll(productLineNameList);

        // By default, the login user has bic admin role
        adminToolPage.selectSubscriptionLink();
        AssertCollector.assertThat(
            "Add bic release link is not enabled for " + getEnvironmentVariables().getUserName()
                + " in application family '" + getEnvironmentVariables().getApplicationFamily() + "'",
            adminToolPage.isAddBicReleaseEnabled(), is(true), assertionErrorList);

        final BicReleasePage bicReleasePage = adminToolPage.getPage(BicReleasePage.class);

        bicReleasePage.add();

        AssertCollector.assertThat("Application family drop down", bicReleasePage.getApplicationFamily(),
            equalTo(getEnvironmentVariables().getCombinedApplicationFamily()), assertionErrorList);
        AssertCollector.assertThat("Default option for application drop down", bicReleasePage.getApplication(),
            equalTo("ANY (*)"), assertionErrorList);
        AssertCollector.assertThat("Subs product line options", bicReleasePage.getSubPlanProductLineOptions(),
            containsInAnyOrder(expProductLines.toArray()), assertionErrorList);
        AssertCollector.assertThat("Download product line options", bicReleasePage.getDownloadProductLineOptions(),
            containsInAnyOrder(expProductLines.toArray()), assertionErrorList);
        AssertCollector.assertThat("Default status", bicReleasePage.getStatus(), equalTo(Status.ACTIVE),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * TODO:// Cannot assign a password to newly created user. Once this can be done, we can revisit this test case.
     * <p>
     * Users without the role assigned should not get the link and thus can not add nor edit bic release
     *
     * @Result: Admin tool obey the role assignment.
     */
    @Test(enabled = false)
    public void noAdminRole() {

        // By default, the login user has permission so logout to relogin with
        // user without bic admin role
        adminToolPage.logout();

        final AdminToolPage loginPage = adminToolPage.getPage(AdminToolPage.class);
        if (getEnvironmentVariables().getEnvironmentType().equalsIgnoreCase("stg")) {
            loginPage.setApplicationFamily("AUTODESK");
        } else if (getEnvironmentVariables().getEnvironmentType().equalsIgnoreCase("dev")) {
            loginPage.setApplicationFamily("Demo");
        } else {
            loginPage.setApplicationFamily(getEnvironmentVariables().getApplicationFamily());
        }
        loginPage.setUserName(getEnvironmentVariables().getUserName());
        loginPage.setPassword(getEnvironmentVariables().getPassword());
        loginPage.submit();

        // No "Add Bic Release Link"
        adminToolPage.selectSubscriptionLink();
        AssertCollector.assertThat(
            "Add bic release link is enabled for " + getEnvironmentVariables().getUserName() + " in application family "
                + getEnvironmentVariables().getApplicationFamily(),
            adminToolPage.isAddBicReleaseEnabled(), is(false), assertionErrorList);

        // Reset login
        adminToolPage.login();

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Validate items are correctly populated in dropdowns for the findForm (Advanced Search): <br>
     * - application family - contains only the logged in application family <br>
     * - subscription plan product line - contains only the product lines defined <br>
     * - download product line - contains only the product lines defined <br>
     * - fcs start and end edit boxes are rendered
     * <p>
     * Result: all dropdowns have correct items
     */
    @Test(enabled = false)
    public void advancedFindLayout() {

        final List<String> expProductLines = new ArrayList<>();
        expProductLines.add("-- SELECT ONE --");
        expProductLines.addAll(productLineNameList);

        final BicReleasePage bicReleasePage = adminToolPage.getPage(BicReleasePage.class);
        bicReleasePage.advancedFind();

        AssertCollector.assertThat("Application family drop down", bicReleasePage.getApplicationFamily(),
            equalTo(getEnvironmentVariables().getCombinedApplicationFamily()), assertionErrorList);
        AssertCollector.assertThat("Subs product line options", bicReleasePage.getSubPlanProductLineOptions(),
            containsInAnyOrder(expProductLines.toArray()), assertionErrorList);
        AssertCollector.assertThat("Download product line options", bicReleasePage.getDownloadProductLineOptions(),
            containsInAnyOrder(expProductLines.toArray()), assertionErrorList);
        AssertCollector.assertThat("FCS start date does not exist", bicReleasePage.doesFcsStartDateExist(), is(true),
            assertionErrorList);
        AssertCollector.assertThat("FCS end date does not exist", bicReleasePage.doesFcsEndDateExist(), is(true),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Validate correct columns are in the search result
     */
    @Test
    public void searchResults() {
        final List<String> expHeaders = new ArrayList<>();
        expHeaders.add("ID");
        expHeaders.add("SubPlan Product Line");
        expHeaders.add("Download Product Line");
        expHeaders.add("Download Release");
        expHeaders.add("CLIC Enabled");
        expHeaders.add("Legacy SKU");
        expHeaders.add("FCS Date");
        expHeaders.add("Ignore SCF E-Mails");
        expHeaders.add("Status");

        final BicReleasePage bicReleasePage = adminToolPage.getPage(BicReleasePage.class);
        AssertCollector.assertThat("Incorrect headers", bicReleasePage.getGrid().getColumnHeaders(),
            equalTo(expHeaders), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Add Bic Releases with the following values: <br>
     * - Any application and specific application (positive) <br>
     * - Status of active and inactive (positive) <br>
     * - clic enabled with legacy sku (positive) <br>
     * - clic enabled without legacy sku (positve) <br>
     * - clic disabled with legacy sku (positive) <br>
     * - click disabled without legacy sku (negative) <br>
     * - ignore email notification - enabled and disabled (positive)
     * <p>
     * <p>
     * Result: All positive tests are created successfully. Correct values for Created By and Created On
     */
    @Test(dataProvider = "addBicReleases")
    public void addBicRelease(final BicRelease bicRelease) {
        newlyAddedBicReleases.add(bicRelease);
        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Get # of existing bic releases
        final int existingCount = page.getGrid().getTotalItems();

        // Add bic release via admin
        final BicRelease actualBicRelease = page.add(bicRelease);

        // Add expected values for Id, Created by and Created on (Feb 2, 2015
        // 2:14:23 PM PST)
        bicRelease.setApplicationFamily(getEnvironmentVariables().getApplicationFamily());
        bicRelease.setId(actualBicRelease.getId());
        bicRelease.setCreatedBy(getEnvironmentVariables().getUserName());
        bicRelease.setCreatedOn(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_WITH_ZONE));
        if (bicRelease.getFcsDate() != null) {
            bicRelease.setFcsDate(bicRelease.getFcsDate() + utcSuffix);
        }
        // Get # of bic releases after adding the new one
        AssertCollector.assertThat("Incorrect number of bic releases after adding a new one",
            page.getGrid().getTotalItems(), equalTo(existingCount + 1), assertionErrorList);

        // Validate bic release is added successfully
        final PropertiesComparator comparator = new PropertiesComparator();

        // iterating over the results from the comparison
        for (final PropertiesComparator.PropertyComparisonResult result : comparator.getResults()) {

            AssertCollector.assertThat("Incorrect value for '" + result.getProperty() + "'\n" + result.toString(),
                result.isEqual(), equalTo(true), assertionErrorList);
        }

        comparator.diff(actualBicRelease.getDateProperties(), PelicanConstants.DATE_FORMAT_WITH_SLASH, actualBicRelease,
            bicRelease);

        // Validate bic release via api
        validateBicReleaseEligibleVersions(bicRelease);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Error when subscription plan product line, download product line and download release combination with
     * ACTIVE status is same as another BIC release entry of ACTIVE status It tries to create a same entry what was
     * created in "addBicRelease" method
     */
    @Test(dependsOnMethods = { "addBicRelease" })
    public void testGetErrorWhenDuplicateIsAddedInActiveStateInBicRelease() {

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Get # of existing bic releases
        final int existingCount = page.getGrid().getTotalItems();

        // Setting up the input and adding in "Add form"
        final BicRelease bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(newlyAddedBicReleases.get(2).getSubsPlanProductLine());
        bicRelease.setDownloadProductLine(newlyAddedBicReleases.get(2).getDownloadProductLine());
        bicRelease.setDownloadRelease(newlyAddedBicReleases.get(2).getDownloadRelease());
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(true);
        bicRelease.setFcsDate(newlyAddedBicReleases.get(2).getFcsDate());
        bicRelease.setIgnoredEmailNotification(true);
        page.addBicReleaseFail(bicRelease);

        AssertCollector.assertThat("Incorrect error message", page.getErrorMessageFromFormHeader(),
            equalTo("An active BIC Release with the same PLAN PRODUCT LINE/DOWNLOAD PRODUCT LINE/DOWNLOAD RELEASE "
                + "combination already exists."),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect number of bic releases after failed to add one",
            page.getGrid().getTotalItems(), equalTo(existingCount), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Success when subscription plan product line, download product line and download release combination with
     * INACTIVE status is same as another BIC release entry of ACTIVE status It tries to create a same entry what was
     * created in "addBicRelease" method
     */
    @Test(dependsOnMethods = { "addBicRelease" })
    public void testSuccessWhenDuplicateIsAddedInInactiveStateInBicRelease() {

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Get # of existing bic releases
        final int existingCount = page.getGrid().getTotalItems();

        final BicRelease bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(newlyAddedBicReleases.get(0).getSubsPlanProductLine());
        bicRelease.setDownloadProductLine(newlyAddedBicReleases.get(0).getDownloadProductLine());
        bicRelease.setDownloadRelease(newlyAddedBicReleases.get(0).getDownloadRelease());
        bicRelease.setStatus(Status.INACTIVE);
        bicRelease.setClic(true);
        bicRelease.setFcsDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        final BicRelease addingBicRelease = page.add(bicRelease);

        bicReleaseId = addingBicRelease.getId();
        // Get number of bic releases after adding the new one
        AssertCollector.assertThat("Incorrect number of bic releases after failed to add one",
            page.getGrid().getTotalItems(), equalTo(existingCount + 1), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests error when INACTIVE bic release is edited to ACTIVE, which is a duplicate of another BIC
     * release
     */
    @Test(dependsOnMethods = { "testSuccessWhenDuplicateIsAddedInInactiveStateInBicRelease" })
    public void testErrorWhenEditingDuplicateInactiveToActive() {

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);
        final GenericDetails findBicRelease = page.findById(bicReleaseId);
        findBicRelease.edit();
        page.selectStatus(Status.ACTIVE);
        page.submit();

        AssertCollector.assertThat("Incorrect error message", page.getErrorMessageFromFormHeader(),
            equalTo("An active BIC Release with the same PLAN PRODUCT LINE/DOWNLOAD PRODUCT LINE/DOWNLOAD RELEASE "
                + "combination already exists."),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Edit the following fields: <br>
     * - application <br>
     * - product download both update and null <br>
     * - subscription plan product line <br>
     * - download product line <br>
     * - status <br>
     * - clic enabled / disabled <br>
     * - fcs date - ignore email notification
     * <p>
     * <p>
     * Result: All fields are editable and updated correctly. Created By and Created On are not changed. Correct values
     * for Updated By and Updated On
     */
    @Test
    public void editBicRelease() {

        final List<BicRelease> expBicReleases = new ArrayList<>();
        final List<BicRelease> actBicReleases = new ArrayList<>();

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Add some bic release
        final List<BicRelease> newBicReleases = getBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            expBicReleases.add(page.add(bicRelease));
        }

        // Now edit the newly added bic release
        expBicReleases.get(0).setStatus(Status.INACTIVE);
        expBicReleases.get(0).setFcsDate(DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2));
        actBicReleases.add(page.edit(expBicReleases.get(0)));
        expBicReleases.get(0).setUpdatedBy(getEnvironmentVariables().getUserName());
        expBicReleases.get(0).setUpdatedOn(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_WITH_ZONE));
        expBicReleases.get(0).setFcsDate(expBicReleases.get(0).getFcsDate() + utcSuffix);

        expBicReleases.get(1).setStatus(Status.ACTIVE);
        expBicReleases.get(1).setIgnoredEmailNotification(false);
        actBicReleases.add(page.edit(expBicReleases.get(1)));
        expBicReleases.get(1).setUpdatedBy(getEnvironmentVariables().getUserName());
        expBicReleases.get(1).setUpdatedOn(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_WITH_ZONE));

        expBicReleases.get(2).setClic(true);
        expBicReleases.get(2).setIgnoredEmailNotification(true);
        expBicReleases.get(2).setDownloadProductLine(productLineNameList.get(2));
        actBicReleases.add(page.edit(expBicReleases.get(2)));
        expBicReleases.get(2).setUpdatedBy(getEnvironmentVariables().getUserName());
        expBicReleases.get(2).setUpdatedOn(DateTimeUtils.getNowAsUTC(PelicanConstants.DATE_TIME_WITH_ZONE));

        for (int i = 0; i < expBicReleases.size(); i++) {
            final PropertiesComparator comparator = new PropertiesComparator();
            final BicRelease actualBicRelease = actBicReleases.get(i);
            final BicRelease expectedBicRelease = expBicReleases.get(i);

            // iterating over the results from the comparison
            for (final PropertiesComparator.PropertyComparisonResult result : comparator.getResults()) {

                AssertCollector.assertThat("Incorrect value for '" + result.getProperty() + "'\n" + result.toString(),
                    result.isEqual(), equalTo(true), assertionErrorList);
            }

            comparator.diff(actualBicRelease.getDateProperties(), PelicanConstants.DATE_FORMAT_WITH_SLASH,
                actualBicRelease, expectedBicRelease);
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find Bic release without id
     * <p>
     * <p>
     * Result: All bic release per application family
     */
    @Test
    public void findBicRelease() {

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Get # of existing bic releases
        final int existingCount = page.getGrid().getTotalItems();

        // Add some bic release
        final List<BicRelease> newBicReleases = getBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            page.add(bicRelease);
        }
        final int newCount = existingCount + newBicReleases.size();

        final GenericGrid grid = page.findAll();
        AssertCollector.assertThat("Incorrect cound for find by empty id", grid.getTotalItems(), equalTo(newCount),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find Bic release by valid id
     * <p>
     * <p>
     * Result: Bic release with said id
     */
    @Test
    public void findBicReleaseByValidId() {

        final List<String> ids = new ArrayList<>();

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Add some bic release
        final List<BicRelease> newBicReleases = getBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            final BicRelease actualBicRelease = page.add(bicRelease);
            ids.add(actualBicRelease.getId());
        }

        // Now try to find these new bic releases by id
        for (final String id : ids) {
            AssertCollector.assertThat("Unable to find bic release by id #" + id, page.findById(id), is(notNullValue()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Find Bic release by invalid id
     * <p>
     * <p>
     * Result: Empty result set
     */
    @Test
    public void findBicReleaseByInvalidId() {
        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);
        AssertCollector.assertThat("Incorrect search result page", page.findById("0"), is(nullValue()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Find bic release with following criterias: <br>
     * - product download valid <br>
     * - subscription plan product line <br>
     * - download product line <br>
     * - legacy sku <br>
     * <p>
     * Result: Correct result set
     */
    @Test
    public void findByAdvancedSearch() {
        final List<BicRelease> bicReleases = new ArrayList<>();
        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Add some bic release
        final List<BicRelease> newBicReleases = getBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            final BicRelease newBicRelease = page.add(bicRelease);
            bicReleases.add(newBicRelease);
        }

        for (final BicRelease bicRelease : bicReleases) {

            LOGGER.info("==== Advanced Search for subscription plan product line: "
                + bicRelease.getSubsPlanProductLine() + " ====");

            BicReleaseAdvSearch searchCriteria = new BicReleaseAdvSearch();
            searchCriteria.setSubPlanProductLine(bicRelease.getSubsPlanProductLine());
            GenericGrid resultSet = page.findByCriteria(searchCriteria);

            // Validate
            String column = "SubPlan Product Line";
            List<String> actualValues = getColumnValueWithoutId(resultSet.getColumnValues(column));
            AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualValues,
                everyItem(equalTo(bicRelease.getSubsPlanProductLine())), assertionErrorList);

            LOGGER.info(
                "==== Advanced Search for download product line: " + bicRelease.getDownloadProductLine() + " ====");

            searchCriteria = new BicReleaseAdvSearch();
            searchCriteria.setDownloadProductLine(bicRelease.getDownloadProductLine());
            resultSet = page.findByCriteria(searchCriteria);

            // Validate
            column = "Download Product Line";
            actualValues = getColumnValueWithoutId(resultSet.getColumnValues(column));
            AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualValues,
                everyItem(equalTo(bicRelease.getDownloadProductLine())), assertionErrorList);

            LOGGER.info("==== Advanced Search for download release: " + bicRelease.getDownloadRelease() + " ====");

            searchCriteria = new BicReleaseAdvSearch();
            searchCriteria.setDownloadRelease(bicRelease.getDownloadRelease());
            resultSet = page.findByCriteria(searchCriteria);

            // Validate
            column = "Download Release";
            actualValues = resultSet.getColumnValues(column);
            AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualValues,
                everyItem(equalTo(bicRelease.getDownloadRelease())), assertionErrorList);

            if (bicRelease.getLegacySku() != null) {
                LOGGER.info("==== Advanced Search for legacy sku: " + bicRelease.getLegacySku() + " ====");

                searchCriteria = new BicReleaseAdvSearch();
                searchCriteria.setLegacySku(bicRelease.getLegacySku());
                resultSet = page.findByCriteria(searchCriteria);

                // Validate
                column = "Legacy SKU";
                actualValues = resultSet.getColumnValues(column);
                AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualValues,
                    everyItem(equalTo(bicRelease.getLegacySku())), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find bic release from advanced search by status - Active and Inactive Result: Correct result set
     */
    @Test(dataProvider = "statusOptions")
    public void findByAdvancedSearchStatus(final Status status) {

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        final int existingCount = page.getGrid().getTotalItems();

        if (existingCount < 10) {
            // Add some bic release
            final List<BicRelease> newBicReleases = getBicReleases();
            for (final BicRelease bicRelease : newBicReleases) {
                page.add(bicRelease);
            }
        }
        LOGGER.info("==== Advanced Search for status: " + status + " ====");

        final BicReleaseAdvSearch searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setStatus(status);
        final GenericGrid resultSet = page.findByCriteria(searchCriteria);

        // Validate
        final String column = "Status";
        LOGGER.info("Validate column: " + column);
        final List<String> actualValue = resultSet.getColumnValues(column);
        AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualValue,
            everyItem(equalTo(status.toString())), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find bic release by FCS date. Bic release will have FCS with today and today + 5 <br>
     * - Start (Today) <br>
     * - Start (Today + 1) <br>
     * - End (Today + 2) <br>
     * - Start (Today - 2) and End (Today + 6) <br>
     * - Start (Today + 10) - no result
     * <p>
     * Result: Correct result set
     */
    @Test
    public void findByAdvancedSearchFcsDate() throws ParseException {

        final String column = "FCS Date";
        String expStartDate;
        String expEndDate;

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Add some bic release
        final List<BicRelease> newBicReleases = getBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            page.add(bicRelease);
        }

        LOGGER.info("==== Advanced Search for date range of today ====");

        expStartDate = DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);

        BicReleaseAdvSearch searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setFcsStartDate(expStartDate);
        GenericGrid resultSet = page.findByCriteria(searchCriteria);

        // Validate
        LOGGER.info("Validate column: " + column);
        List<String> actualValues = resultSet.getColumnValues(column);
        AssertCollector.assertThat("Incorrect values in '" + column + "'", actualValues,
            everyItem(greaterThanOrEqualTo(expStartDate + utcSuffix)), assertionErrorList);

        LOGGER.info("==== Advanced Search for date range of today + 1 ====");

        expStartDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1);

        searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setFcsStartDate(expStartDate);
        resultSet = page.findByCriteria(searchCriteria);

        // Validate
        LOGGER.info("Validate column: " + column);
        actualValues = resultSet.getColumnValues(column);
        AssertCollector.assertThat("Incorrect values in '" + column + "'", actualValues,
            everyItem(greaterThanOrEqualTo(expStartDate + utcSuffix)), assertionErrorList);

        LOGGER.info("==== Advanced Search for date ending by today + 2 ====");

        expEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        final Date expectedEndDate = DateTimeUtils.getDate(PelicanConstants.DATE_FORMAT_WITH_SLASH, expEndDate);

        searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setFcsEndDate(expEndDate);
        resultSet = page.findByCriteria(searchCriteria);

        // Validate
        LOGGER.info("Validate column: " + column);
        actualValues = resultSet.getColumnValues(column);

        for (final String value : actualValues) {

            final Date actualDate = DateTimeUtils.getDate(PelicanConstants.DATE_FORMAT_WITH_SLASH, value);

            AssertCollector.assertTrue("Incorrect values in '" + column + "' column",
                actualDate.compareTo(expectedEndDate) <= 0, assertionErrorList);
        }

        LOGGER.info("==== Advanced Search for date (today - 2, today + 6) ====");

        expStartDate = DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2);
        expEndDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 6);

        searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setFcsStartDate(expStartDate);
        searchCriteria.setFcsEndDate(expEndDate);
        resultSet = page.findByCriteria(searchCriteria);

        // Validate
        LOGGER.info("Validate column: " + column);
        actualValues = resultSet.getColumnValues(column);
        AssertCollector.assertThat("Incorrect values in '" + column + "'", actualValues,
            everyItem(allOf(greaterThanOrEqualTo(expStartDate + utcSuffix), lessThanOrEqualTo(expEndDate + utcSuffix))),
            assertionErrorList);

        LOGGER.info("==== Advanced Search for date range of today + 10 ====");

        expStartDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 10);

        searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setFcsStartDate(expStartDate);
        resultSet = page.findByCriteria(searchCriteria);

        // Validate
        LOGGER.info("Validate column: " + column);
        actualValues = resultSet.getColumnValues(column);
        AssertCollector.assertThat("Incorrect values in '" + column + "'", actualValues, hasSize(0),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     *
     */
    @Test(dataProvider = "getPackagingTypes")
    public void testFindByAdvancedSearchForPlansWithPackagingType(final PackagingType packagingType) {

        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        final String productLineExternalKey1 = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);

        final Offerings bicOffering1 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), packagingType);

        final String productLineExternalKey2 = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);
        final Offerings bicOfferings2 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey2,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), PackagingType.NONE);

        final String productLineExternalKey3 = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey3);
        final Offerings bicOfferings3 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey3,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), PackagingType.NONE);

        final String productLineExternalKey4 = RandomStringUtils.randomAlphanumeric(10);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey4);
        final Offerings bicOfferings4 = subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKey4,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), PackagingType.NONE);

        final Item item2 = itemUtils.addItem(productLineExternalKey2, getEnvironmentVariables().getAppId(),
            getBuyerUser().getId(), getItemTypeId(), productLineExternalKey2);
        final Item item3 = itemUtils.addItem(productLineExternalKey3, getEnvironmentVariables().getAppId(),
            getBuyerUser().getId(), getItemTypeId(), productLineExternalKey3);
        final Item item4 = itemUtils.addItem(productLineExternalKey4, getEnvironmentVariables().getAppId(),
            getBuyerUser().getId(), getItemTypeId(), productLineExternalKey4);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings2.getOffering().getId(),
            item2.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings3.getOffering().getId(),
            item3.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings4.getOffering().getId(),
            item4.getId(), null, true);

        bicReleaseAddPage.createBicRelease(productLineExternalKey2, PelicanConstants.DOWNLOAD_RELEASE1);
        bicReleaseAddPage.createBicRelease(productLineExternalKey2, PelicanConstants.DOWNLOAD_RELEASE2);

        bicReleaseAddPage.createBicRelease(productLineExternalKey3, PelicanConstants.DOWNLOAD_RELEASE1);
        bicReleaseAddPage.createBicRelease(productLineExternalKey3, PelicanConstants.DOWNLOAD_RELEASE2);

        bicReleaseAddPage.createBicRelease(productLineExternalKey4, PelicanConstants.DOWNLOAD_RELEASE1);
        bicReleaseAddPage.createBicRelease(productLineExternalKey4, PelicanConstants.DOWNLOAD_RELEASE2);

        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering1.getOffering().getId(),
            item2.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering1.getOffering().getId(),
            item3.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering1.getOffering().getId(),
            item4.getId(), null, true);

        final BicReleaseAdvSearch searchCriteria = new BicReleaseAdvSearch();
        searchCriteria.setSubPlanProductLine(productLineExternalKey1);
        final GenericGrid resultSet = page.findByCriteria(searchCriteria);

        final List<String> subPlanProdLineList = resultSet.getColumnValues(PelicanConstants.SUB_PLAN_PROD_LINE);
        final List<String> downloadPlanProdLineList = resultSet.getColumnValues(PelicanConstants.DOWNLOAD_PROD_LINE);
        final List<String> downloadReleaseList = resultSet.getColumnValues(PelicanConstants.DOWNLOAD_RELEASE);

        AssertCollector.assertThat("Incorrect number of subscription plan productlines", subPlanProdLineList.size(),
            equalTo(6), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of download productlines", downloadPlanProdLineList.size(),
            equalTo(6), assertionErrorList);
        AssertCollector.assertThat("Incorrect number of download releases", downloadReleaseList.size(), equalTo(6),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release1",
            subPlanProdLineList.get(0).split(" ")[0],
            isOneOf(productLineExternalKey2, productLineExternalKey3, productLineExternalKey4), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release1",
            subPlanProdLineList.get(0).split(" ")[0],
            isOneOf(productLineExternalKey2, productLineExternalKey3, productLineExternalKey4), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release2",
            subPlanProdLineList.get(0).split(" ")[0],
            isOneOf(productLineExternalKey2, productLineExternalKey3, productLineExternalKey4), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release2",
            subPlanProdLineList.get(0).split(" ")[0],
            isOneOf(productLineExternalKey2, productLineExternalKey3, productLineExternalKey4), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release3",
            subPlanProdLineList.get(0).split(" ")[0],
            isOneOf(productLineExternalKey2, productLineExternalKey3, productLineExternalKey4), assertionErrorList);
        AssertCollector.assertThat("Incorrect productline code for bic release3",
            subPlanProdLineList.get(0).split(" ")[0],
            isOneOf(productLineExternalKey2, productLineExternalKey3, productLineExternalKey4), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Find bic release with following criterias: <br>
     * - Subscription plan product line <br>
     * - status = active/inactive
     * <p>
     * <p>
     * Result: Correct result set
     */
    @Test(dataProvider = "statusOptions")
    public void findBySubscriptionPlanProductLineAndStatus(final Status status) {

        final List<String> productLines = new ArrayList<>();
        final BicReleasePage page = adminToolPage.getPage(BicReleasePage.class);

        // Add some bic release
        final List<BicRelease> newBicReleases = getBicReleases();
        for (final BicRelease bicRelease : newBicReleases) {
            final BicRelease newBicRelease = page.add(bicRelease);
            productLines.add(newBicRelease.getSubsPlanProductLine());
        }

        // Advanced search on subscription product line and status = active
        for (final String productLine : productLines) {

            LOGGER.info("==== Advanced Search for '" + productLine + "' status = '" + status.toString() + "' ====");

            final BicReleaseAdvSearch searchCriteria = new BicReleaseAdvSearch();
            searchCriteria.setSubPlanProductLine(productLine);
            searchCriteria.setStatus(status);
            final GenericGrid resultSet = page.findByCriteria(searchCriteria);

            // Validate subs product line column
            String column = "SubPlan Product Line";
            LOGGER.info("Validate column: " + column);
            final List<String> actualProductLines = getColumnValueWithoutId(resultSet.getColumnValues(column));
            AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualProductLines,
                everyItem(equalTo(productLine)), assertionErrorList);

            // Validate status column
            column = "Status";
            LOGGER.info("Validate column: " + column);
            final List<String> actualStatus = resultSet.getColumnValues(column);
            AssertCollector.assertThat("Incorrect values in column '" + column + "'", actualStatus,
                everyItem(equalTo(status.toString())), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Create 3 bic releaese instances for test cases
     */
    private List<BicRelease> getBicReleases() {
        final List<BicRelease> bicReleases = new ArrayList<>();
        BicRelease bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(productLineNameList.get(0));
        bicRelease.setDownloadProductLine(productLineNameList.get(0));
        bicRelease.setDownloadRelease("Rel" + String.valueOf(new Date().getTime()));
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(true);
        bicRelease.setFcsDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));
        bicRelease.setIgnoredEmailNotification(true);
        bicReleases.add(bicRelease);

        bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(productLineNameList.get(2));
        bicRelease.setDownloadProductLine(productLineNameList.get(1));
        bicRelease.setDownloadRelease("Rel" + String.valueOf(new Date().getTime()));
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(true);
        bicRelease.setLegacySku("some legacy sku 1");
        bicRelease.setIgnoredEmailNotification(true);
        bicReleases.add(bicRelease);

        bicRelease = new BicRelease();
        bicRelease.setSubsPlanProductLine(productLineNameList.get(1));
        bicRelease.setDownloadProductLine(productLineNameList.get(0));
        bicRelease.setDownloadRelease("Rel" + String.valueOf(new Date().getTime()));
        bicRelease.setStatus(Status.ACTIVE);
        bicRelease.setClic(false);
        bicRelease.setLegacySku("some legacy sku 2");
        bicRelease.setFcsDate(DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 5));
        bicRelease.setIgnoredEmailNotification(false);
        bicReleases.add(bicRelease);

        return bicReleases;
    }

    @DataProvider(name = "addBicReleases")
    private Object[][] addBicReleases() {

        final List<BicRelease> origBicReleases = getBicReleases();

        // Convert list to array
        final BicRelease[][] bicReleases = new BicRelease[origBicReleases.size()][1];
        for (int i = 0; i < origBicReleases.size(); i++) {
            bicReleases[i][0] = origBicReleases.get(i);
        }

        return bicReleases;
    }

    @DataProvider(name = "statusOptions")
    private static Object[][] getStatusOptions() {
        return new Status[][] { { Status.ACTIVE }, { Status.INACTIVE } };
    }

    /**
     * Create 3 product lines
     */
    private List<ProductLine> productLineDataSetup() {

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        productLineExternalKeyList = new ArrayList<>();
        productLineNameList = new ArrayList<>();

        final List<ProductLine> productLineLists = new ArrayList<>();
        for (int i = 0; i < 3; i++) {

            final String productLineExternalKey =
                PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
            productLineExternalKeyList.add(productLineExternalKey);
            productLineNameList.add(productLineExternalKey);
            productLineLists.add(subscriptionPlanApiUtils.addProductLine(productLineExternalKey));
        }
        return productLineLists;
    }

    private String getProductLineExtKey(final String name) {
        for (int i = 0; i < productLineNameList.size(); i++) {
            if (productLineNameList.get(i).equalsIgnoreCase(name)) {
                return productLineExternalKeyList.get(i);
            }
        }
        return null;
    }

    /**
     * If the bic release is in active, then api will not return that item.
     */
    private void validateBicReleaseEligibleVersions(final BicRelease expBicRelease) {
        final BicReleaseClient bicReleaseRest = new BicReleaseClient(getEnvironmentVariables());

        // Get product line code
        final String productLineCode = getProductLineExtKey(expBicRelease.getSubsPlanProductLine());
        final String downloadProductLineCode = getProductLineExtKey(expBicRelease.getDownloadProductLine());

        final EligibleVersions versions =
            bicReleaseRest.getEligibleVersions(Collections.singletonList(productLineCode));

        // For inactive release, we should not be included in query
        if (versions == null && expBicRelease.getStatus() == Status.INACTIVE) {
            LOGGER.info("As expected, unable to find inactive version for product line: " + productLineCode);
        }

        AssertCollector.assertThat("Unable to query newly created active product line: " + productLineCode, versions,
            is(notNullValue()), assertionErrorList);

        // Since we're querying by specified product line, there should only be
        // 1 returned
        if (versions != null) {
            AssertCollector.assertThat("Incorrect count for " + productLineCode, versions.getProducts().size(),
                equalTo(1), assertionErrorList);
        }

        // Validate we got the correct product line code
        ProductLineEligibleProduct eligibleProduct = null;
        if (versions != null) {
            eligibleProduct = versions.getProducts().get(0);
        }
        if (eligibleProduct != null) {
            AssertCollector.assertThat("Incorrect product line code for " + productLineCode,
                eligibleProduct.getProductLineCode(), equalTo(productLineCode), assertionErrorList);
        }

        // Find the correct download product line and verify that the release
        // version is correct
        boolean found = false;
        if (eligibleProduct != null) {
            for (final EligibleProduct downloadProductLine : eligibleProduct.getEligibleProducts()) {
                if (downloadProductLine.getEligibleProductLineCode().equalsIgnoreCase(downloadProductLineCode)) {
                    for (final BicVersion version : downloadProductLine.getVersions()) {
                        if (version.getVersion().equalsIgnoreCase(expBicRelease.getDownloadRelease())) {
                            found = true;
                        }
                    }
                }
            }
        }

        if (expBicRelease.getStatus() == Status.ACTIVE) {
            LOGGER.info(
                "Validate newly created active product line '" + productLineCode + "' is included in eligibleVersions");
            AssertCollector.assertThat("Unable to find newly created active version '"
                + expBicRelease.getDownloadRelease() + "' for product line: " + productLineCode, found, is(true),
                assertionErrorList);

        } else {
            LOGGER.info("Validate newly created inactive product line '" + productLineCode
                + "' is not included in eligibleVersions");
            AssertCollector.assertThat("Found newly created inactive version '" + expBicRelease.getDownloadRelease()
                + "' for product line: " + productLineCode, found, is(false), assertionErrorList);
        }
    }

    private List<String> getColumnValueWithoutId(final List<String> columnValues) {
        final List<String> columnValuesWithoutId = new ArrayList<>();

        for (final String value : columnValues) {
            if (value != null) {
                final String columnValue = value.contains("(") ? value.substring(0, value.indexOf("(")).trim() : value;
                columnValuesWithoutId.add(columnValue);
            }
        }

        return columnValuesWithoutId;
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
