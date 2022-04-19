package com.autodesk.bsm.pelican.ui.downloads.users;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.UserAccessReportPage;
import com.autodesk.bsm.pelican.ui.pages.reports.UserAccessReportResultPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.core.Every;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User access review report tests. Accessible in admin tool under Reports -> User Reports with EBSO role only.
 *
 * @author jains
 */

public class UserAccessReportTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static UserAccessReportPage userAccessReportPage;
    private GenericDetails genericDetails;
    private List<String> roleList1;
    private List<String> roleList3;
    private String adminToolUserId;
    private String adminToolUserName;
    private String adminToolUserExternalKey;
    private UserDetailsPage userDetailsPage;
    private static final String PERMISSION_DENIED_ERROR =
        "This operation requires the report.user_access_review permission.";
    private HashMap<String, String> userParams;
    private RolesHelper rolesHelper;
    private UserUtils userUtils;
    private UserAccessReportResultPage userAccessReportResultPage;
    private String userColumnData;
    private static final String DOWNLOAD_FILE_NAME = "UserAccessReport.xlsx";
    private List<String> roleNameList;
    private final JSONObject request = new JSONObject();
    private final JSONArray dataArray = new JSONArray();
    private final JSONObject dataObject1 = new JSONObject();
    private final Map<String, String> requestParams = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccessReportTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        userAccessReportPage = adminToolPage.getPage(UserAccessReportPage.class);
        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        userAccessReportResultPage = adminToolPage.getPage(UserAccessReportResultPage.class);
        genericDetails = adminToolPage.getPage(UserAccessReportResultPage.class);
        userParams = new HashMap<>();
        rolesHelper = new RolesHelper(getEnvironmentVariables());
        userUtils = new UserUtils();
        roleNameList = new ArrayList<>();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        final Map<String, String> rolesMap = DbUtils.getAllRoles(getEnvironmentVariables());

        // create role list for data provider
        roleList1 = new ArrayList<>(Arrays.asList(Role.ADMIN.getValue(), Role.SECURITY_MANAGER.getValue(),
            Role.APPLICATION_MANAGER.getValue()));
        roleList3 = new ArrayList<>(Arrays.asList(Role.EBSO.getValue()));

        // get user id of a admin tool user. Order by desc is used to get the
        // latest record from db. For old records
        // created by is not captured in db and test will fail.
        final List<Map<String, String>> queryResultMapList = DbUtils
            .selectQuery("Select NAME, ID, XKEY from NAMED_PARTY where " + "IS_ADMINTOOL_USER=1" + " and APPF_ID='"
                + getEnvironmentVariables().getAppFamilyId() + "' order by ID desc", getEnvironmentVariables());
        adminToolUserId = queryResultMapList.get(0).get("ID");
        adminToolUserName = queryResultMapList.get(0).get("NAME");
        adminToolUserExternalKey = queryResultMapList.get(0).get("XKEY");

        // Add a user and assign a role so that report will not be empty.
        final String userName = RandomStringUtils.randomAlphabetic(8);
        final String userExternalKey = RandomStringUtils.randomAlphanumeric(12);

        // Set the request
        final Map<String, String> userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), userName);
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);

        final User user = resource.user().addUser(userRequestParam);

        final String userId = user.getId();
        LOGGER.info("User Id created: " + userId);
        // Adding a role
        dataObject1.clear();
        dataArray.clear();
        dataObject1.put("type", "roles");
        dataObject1.put("id", rolesMap.get(Role.QA_ONLY.getValue()));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        requestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        requestParams.put("body", request.toJSONString());
        requestParams.put("userID", userId);

        // Assign role to the created user
        rolesHelper.assignUserRole(requestParams);

    }

    /**
     * Verify that user access report does not show non-admin tool user.
     */
    @Test
    public void testUserAccessReportForNonAdminToolUserView() {
        // get non-admin tool user from db for given application family
        final List<String> nonAdminToolUserList =
            DbUtils.selectQuery("Select ID from NAMED_PARTY where IS_ADMINTOOL_USER=0" + " and APPF_ID='"
                + getEnvironmentVariables().getAppFamilyId() + "'", "ID", getEnvironmentVariables());
        // check if non-admin tool user is found in db
        if (nonAdminToolUserList.size() > 0) {
            userAccessReportResultPage =
                userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME,
                    nonAdminToolUserList.get(0), null, null, null, PelicanConstants.VIEW);
            final List<String> userColumnList = userAccessReportResultPage.getValuesFromUserColumn();
            final List<String> userAccessReportResultPageColumnHeaders = userAccessReportResultPage.getColumnHeaders();
            LOGGER
                .info("userAccessReportResultPageColumnHeaders size " + userAccessReportResultPageColumnHeaders.size());
            AssertCollector.assertThat("Non-admin tool user should not be shown in report.", userColumnList.get(0),
                equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);
        } else {
            Assert.fail("There is no non-admin tool user available in db to run this test.");
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Verify that report returns correct data with selected filters.
     */
    @Test(dataProvider = "dataForAccessReviewReport")
    public void testUserAccessReportDataWithEBSORoleView(final String userId, final String userName,
        final String creationStartDate, final String creationEndDate, final List<String> roleList) {
        userAccessReportResultPage =
            userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME, userId,
                creationStartDate, creationEndDate, roleList, PelicanConstants.VIEW);

        final int totalRecordsInReport = userAccessReportResultPage.getTotalItems();
        LOGGER.info("Total number of records in report" + totalRecordsInReport);

        if (totalRecordsInReport > 0) {
            // validation on user id column
            if (userId != null) {
                final List<String> userColumnList = userAccessReportResultPage.getValuesFromUserColumn();
                AssertCollector.assertThat("There should be only 1 record when user id is provided",
                    String.valueOf(userColumnList.size()), equalTo("1"), assertionErrorList);

                // user column is constructed with "UserName (UserId)" in
                // the report
                if (userName != null) {
                    LOGGER.info("Validating user name in user column");
                    AssertCollector.assertThat("User column value is not correct", userColumnList.get(0),
                        equalTo(userName + " (" + userId + ")"), assertionErrorList);
                } else {
                    LOGGER.info("Validating user id in user column:" + userColumnList.get(0));
                    userColumnData = userAccessReportResultPage.getValuesFromUserColumn().get(0);
                    AssertCollector.assertThat("User id in user column  is not correct",
                        userAccessReportResultPage.getUserIdFromUserColumn(userColumnData), equalTo(userId),
                        assertionErrorList);
                }

            }
            // validation on creation start date
            if (creationStartDate != null) {
                LOGGER.info("Validating creation start date");
                AssertCollector.assertThat(
                    "Creation start date for all records should be after " + creationStartDate + " date",
                    DateTimeUtils.convertStringListToDateList(userAccessReportResultPage.getValuesFromCreatedColumn(),
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(greaterThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(creationStartDate, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);
            }
            // validation on creation end date
            if (creationEndDate != null) {
                LOGGER.info("Validating creation end date");
                AssertCollector.assertThat(
                    "Creation end date for all records should be less than " + creationEndDate + " date",
                    DateTimeUtils.convertStringListToDateList(userAccessReportResultPage.getValuesFromCreatedColumn(),
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(lessThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(creationEndDate, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);
            }
            // validation on role list
            if (roleList != null) {
                LOGGER.info("Validating role list");
                final List<String> actualReportRoleList = userAccessReportResultPage.getValuesFromRolesColumn();

                // checking if any of the role is present in the report data for
                // all records on first page
                boolean isRoleFound = false;
                for (final String anActualReportRoleList : actualReportRoleList) {
                    for (final String aRoleList : roleList) {
                        if (anActualReportRoleList.contains(aRoleList)) {
                            isRoleFound = true;
                            break;
                        }
                    }
                    AssertCollector.assertThat("Roles are not filtered correctly", isRoleFound, equalTo(true),
                        assertionErrorList);
                }
            }

            // Verify application family for all records
            AssertCollector.assertThat("Application family is not correct ",
                userAccessReportResultPage.getValuesFromApplicationFamilyColumn(),
                Every.everyItem(equalTo(getEnvironmentVariables().getApplicationFamily() + " ("
                    + getEnvironmentVariables().getAppFamilyId() + ")")),
                assertionErrorList);
        } else {
            Assert.fail("Report is empty for the selected filters.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report header names in view report.
     */
    @Test
    public void testUserAccessReportHeadersView() {
        userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME, null, null, null,
            null, PelicanConstants.VIEW);
        final List<String> userAccessReportResultPageColumnHeaders = userAccessReportResultPage.getColumnHeaders();

        AssertCollector.assertThat("Total number of columns is not correct",
            userAccessReportResultPageColumnHeaders.size(), equalTo(8), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", userAccessReportResultPageColumnHeaders.get(0),
            equalTo(PelicanConstants.USER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", userAccessReportResultPageColumnHeaders.get(1),
            equalTo(PelicanConstants.CREATED_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", userAccessReportResultPageColumnHeaders.get(2),
            equalTo(PelicanConstants.CREATED_BY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", userAccessReportResultPageColumnHeaders.get(3),
            equalTo(PelicanConstants.LAST_MODIFIED_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", userAccessReportResultPageColumnHeaders.get(4),
            equalTo(PelicanConstants.LAST_MODIFIED_BY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", userAccessReportResultPageColumnHeaders.get(5),
            equalTo(PelicanConstants.APPLICATION_FAMILY), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", userAccessReportResultPageColumnHeaders.get(6),
            equalTo(PelicanConstants.STATE), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", userAccessReportResultPageColumnHeaders.get(7),
            equalTo(PelicanConstants.ROLES), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that clicking on a row in the report takes the navigation to user detail page and validate user data. Test
     * selects a random row from the report.
     */
    @Test(dataProvider = "dataForAccessReviewReport")
    public void verifyUserAccessReportLinkToUserDetailPageView(final String userId, final String userName,
        final String creationStartDate, final String creationEndDate, final List<String> roleList) {
        userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME, userId,
            creationStartDate, creationEndDate, roleList, PelicanConstants.VIEW);
        userAccessReportResultPage = adminToolPage.getPage(UserAccessReportResultPage.class);

        final int totalRecordsInReport = userAccessReportResultPage.getTotalItems();

        // check if report is empty or not
        if (totalRecordsInReport > 0) {

            // get index of selected row
            final int selectedRowIndex =
                userAccessReportResultPage.selectRowRandomlyFromFirstPage(totalRecordsInReport);
            userColumnData = userAccessReportResultPage.getValuesFromUserColumn().get(selectedRowIndex);
            final String expectedCreated =
                userAccessReportResultPage.getValuesFromCreatedColumn().get(selectedRowIndex);
            final String expectedCreatedBy =
                userAccessReportResultPage.getValuesFromCreatedByColumn().get(selectedRowIndex).split(" ")[0];
            final String expectedLastModified =
                userAccessReportResultPage.getValuesFromModifiedColumn().get(selectedRowIndex);
            final String expectedLastModifiedBy =
                userAccessReportResultPage.getValuesFromModifiedByColumn().get(selectedRowIndex).split(" ")[0];
            final String expectedState = userAccessReportResultPage.getValuesFromStateColumn().get(selectedRowIndex);

            LOGGER.info("Navigating to user detail page.");
            userAccessReportResultPage.selectResultRow(selectedRowIndex + 1);

            if (genericDetails.getTitle().equals(PelicanConstants.USER_DETAIL_TITLE)) {
                AssertCollector.assertThat("User Name is not correct on user detail page",
                    userDetailsPage.getUserName(),
                    equalTo(userAccessReportResultPage.getUserNameFromUserColumn(userColumnData)), assertionErrorList);
                AssertCollector.assertThat("User Id is not correct on user detail page", userDetailsPage.getId(),
                    equalTo(userAccessReportResultPage.getUserIdFromUserColumn(userColumnData)), assertionErrorList);
                AssertCollector.assertThat("Created date is not correct on user detail page",
                    userDetailsPage.getCreated(), equalTo(expectedCreated), assertionErrorList);
                AssertCollector.assertThat("Created by is not correct on user detail page",
                    userDetailsPage.getCreatedBy(), equalTo(expectedCreatedBy), assertionErrorList);
                AssertCollector.assertThat("Last modified is not correct on user detail page",
                    userDetailsPage.getLastModified(), equalTo(expectedLastModified), assertionErrorList);
                AssertCollector.assertThat("Last modified by is not correct on user detail page",
                    userDetailsPage.getLastModifiedBy(), equalTo(expectedLastModifiedBy), assertionErrorList);
                AssertCollector.assertThat("State is not correct on user detail page", userDetailsPage.getState(),
                    equalTo(expectedState), assertionErrorList);

            } else {
                Assert.fail("Navigation is not on user detail page.");
            }
        } else {
            // fail the test if no data is found with the selected filters. If
            // test fails due to this reason, update
            // data provider such that it returns data.
            Assert.fail("Report does not have any data to run the test.");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that the report is not accessible to a non-EBSO user.
     */
    @Test
    public void verifyUserAccessReportAccessWithNonEBSORole() {
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), PelicanConstants.NON_EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        // Log in as a non-ebso user
        final List<String> nonEbsoRoleList = rolesHelper.getNonEbsoRoleList();
        userUtils.createAssignRoleAndLoginUser(userParams, nonEbsoRoleList, adminToolPage, getEnvironmentVariables());

        userAccessReportPage.navigateToUserAccessReportPage();

        userAccessReportPage.clickOnShowDetailsLink();
        // Verify that user is not on report page by trying to enter value into
        // one filter
        boolean isUserIdInputPresent = true;
        try {
            userAccessReportPage.setUserId(adminToolUserId);
        } catch (final NoSuchElementException e) {
            isUserIdInputPresent = false;
        }
        AssertCollector.assertFalse("User id input should not be present. Report is accessible to non-EBSO user.",
            isUserIdInputPresent, assertionErrorList);

        // Verify the error message
        AssertCollector.assertThat("Permission denied error message for Non-Ebso user is not correct.",
            genericDetails.getErrorDetails(), equalTo(PERMISSION_DENIED_ERROR), assertionErrorList);

        // Logout from non EBSO user
        adminToolPage.logout();
        // Login back with EBSO user
        adminToolPage.login();
        AssertCollector.assertAll(assertionErrorList);
    }

    @Test
    public void verifyErrorMessageInAccessReviewReport() {
        userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME, null,
            DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH),
            DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1), null, PelicanConstants.VIEW);
        AssertCollector.assertThat("Error message is not correct", userAccessReportPage.getH3ErrorMessage(),
            equalTo(PelicanErrorConstants.END_DATE_BEFORE_START_DATE_ERROR_MEESAGE), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that report returns correct data with selected filters.
     */
    @Test(dataProvider = "dataForAccessReviewReport")
    public void testUserAccessReportDataWithEBSORoleDownload(final String userId, final String userName,
        final String creationStartDate, final String creationEndDate, final List<String> roleList) throws IOException {
        // Delete all files before downloading report
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);
        userAccessReportResultPage =
            userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME, userId,
                creationStartDate, creationEndDate, roleList, PelicanConstants.DOWNLOAD);

        roleNameList = userAccessReportPage.getRoleNameList();

        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;

        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        final int totalRecordsInReport = XlsUtils.getNumRowsInXlsx(fileName);

        LOGGER.info("Total number of records in the report " + String.valueOf(totalRecordsInReport - 1));
        // If file has only one record, report contains only header.
        if (totalRecordsInReport > 1) {
            // validation on user id column
            if (userId != null) {
                // comparing against 2 since first row is header.
                AssertCollector.assertThat("There should be only 1 record when user id is provided",
                    String.valueOf(totalRecordsInReport), equalTo("2"), assertionErrorList);

                if (userName != null) {
                    LOGGER.info("Validating user name");
                    AssertCollector.assertThat("User name is not correct", fileData[1][1], equalTo(userName),
                        assertionErrorList);
                }

                AssertCollector.assertThat("User external key is not correct", fileData[1][2],
                    equalTo(adminToolUserExternalKey), assertionErrorList);
                AssertCollector.assertThat("User id is not correct", fileData[1][0], equalTo(userId),
                    assertionErrorList);

            }

            final List<String> createdDateColumnValues = new ArrayList<>();
            final List<String> applicationFamilyIdColumnValues = new ArrayList<>();
            final List<String> applicationFamilyNameColumnValues = new ArrayList<>();
            // i=2, since first row is empty in report and second is headers
            for (int i = 1; i < totalRecordsInReport; i++) {
                createdDateColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 3)));
                applicationFamilyIdColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 9)));
                applicationFamilyNameColumnValues.add(XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, 10)));
            }

            // validation on creation start date
            if (creationStartDate != null) {
                LOGGER.info("Validating creation start date");

                AssertCollector.assertThat(
                    "Creation start date for all records should be after " + creationStartDate + " date",
                    DateTimeUtils.convertStringListToDateList(createdDateColumnValues,
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(greaterThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(creationStartDate, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);
            }
            // validation on creation end date
            if (creationEndDate != null) {
                LOGGER.info("Validating creation end date");
                AssertCollector.assertThat(
                    "Creation end date for all records should be less than " + creationEndDate + " date",
                    DateTimeUtils.convertStringListToDateList(createdDateColumnValues,
                        PelicanConstants.DATE_FORMAT_WITH_SLASH),
                    Every.everyItem(lessThanOrEqualTo(
                        DateTimeUtils.convertStringToDate(creationEndDate, PelicanConstants.DATE_FORMAT_WITH_SLASH))),
                    assertionErrorList);
            }
            // validation on role list
            if (roleList != null) {
                LOGGER.info("Validating role list");
                final List<Map<String, String>> roleMappingList = new ArrayList<>();
                // i=2, since first row is empty in report and second is headers
                for (int i = 1; i < totalRecordsInReport; i++) {
                    final Map<String, String> roleMap = new HashMap<>();

                    for (int j = 0; j < roleNameList.size(); j++) {
                        // Role column starts from column 13 in downloaded file
                        roleMap.put(roleNameList.get(j),
                            XlsUtils.getColumnValueFromXlsx(fileName, new XlsCell(i, j + 12)));
                    }
                    roleMappingList.add(roleMap);
                }

                // checking if any of the selected role is present in the report
                // data for all records
                for (final Map<String, String> actualRoleMap : roleMappingList) {
                    boolean isRoleFound = false;
                    for (final String aRoleList : roleList) {
                        if (actualRoleMap.get(aRoleList).equals("true")) {
                            isRoleFound = true;
                            break;
                        }
                    }
                    AssertCollector.assertThat("Roles are not filtered correctly", isRoleFound, equalTo(true),
                        assertionErrorList);
                }
            }

            // Verify application family name and id for all records
            AssertCollector.assertThat("Application family id is not correct ", applicationFamilyIdColumnValues,
                Every.everyItem(equalTo(getEnvironmentVariables().getAppFamilyId())), assertionErrorList);
            AssertCollector.assertThat("Application family name is not correct ", applicationFamilyNameColumnValues,
                Every.everyItem(equalTo(getEnvironmentVariables().getApplicationFamily())), assertionErrorList);

        } else {
            Assert.fail("Report is empty for the selected filters.");
        }

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify report header names in download report.
     */
    @Test
    public void testUserAccessReportHeadersDownload() throws IOException {
        // Delete all files before downloading report
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), DOWNLOAD_FILE_NAME);

        userAccessReportPage.getReportWithSelectedFilters(PelicanConstants.APPLICATION_FAMILY_NAME, null, null, null,
            null, PelicanConstants.DOWNLOAD);
        roleNameList = userAccessReportPage.getRoleNameList();
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + DOWNLOAD_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final int expectedNumberOfColumnsInReport = roleNameList.size() + 12;

        AssertCollector.assertThat("Total number of columns is not correct", fileData[0].length,
            equalTo(expectedNumberOfColumnsInReport), assertionErrorList);
        LOGGER.info("fileData " + fileData[0][1]);
        LOGGER.info("fileData " + fileData[1][1]);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0],
            equalToIgnoringCase(PelicanConstants.USER_ID_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1],
            equalToIgnoringCase(PelicanConstants.USER_NAME_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2],
            equalToIgnoringCase(PelicanConstants.USER_EXTERNAL_KEY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3], equalToIgnoringCase(PelicanConstants.CREATED),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 5", fileData[0][4],
            equalToIgnoringCase(PelicanConstants.CREATED_BY_FIELD + " (" + PelicanConstants.NAME + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 6", fileData[0][5],
            equalToIgnoringCase(PelicanConstants.CREATED_BY_FIELD + " (" + PelicanConstants.EXTERNAL_KEY_FIELD + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 7", fileData[0][6],
            equalToIgnoringCase(PelicanConstants.LAST_MODIFIED_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 8", fileData[0][7],
            equalToIgnoringCase(PelicanConstants.LAST_MODIFIED_BY_FIELD + " (" + PelicanConstants.NAME + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 9", fileData[0][8],
            equalToIgnoringCase(
                PelicanConstants.LAST_MODIFIED_BY_FIELD + " (" + PelicanConstants.EXTERNAL_KEY_FIELD + ")"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 10", fileData[0][9],
            equalToIgnoringCase(PelicanConstants.APPLICATION_FAMILY_ID_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 11", fileData[0][10],
            equalToIgnoringCase(PelicanConstants.APPLICATION_FAMILY_NAME_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 12", fileData[0][11], equalToIgnoringCase(PelicanConstants.STATE),
            assertionErrorList);

        // verifying roles column name
        for (int i = 0; i < roleNameList.size(); i++) {
            AssertCollector.assertThat("Incorrect Header " + i + 13, fileData[0][i + 12],
                equalToIgnoringCase(roleNameList.get(i)), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Data provider with different filters
     *
     * @return Object[][]
     */
    @DataProvider(name = "dataForAccessReviewReport")
    public Object[][] getTestDataForAccessReviewReport() {
        return new Object[][] { { adminToolUserId, adminToolUserName, null, null, null },
                { null, null, DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH),
                        DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH), null },
                { null, null, DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1),
                        DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH), roleList1 },
                { null, null, DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 2), null,
                        roleList3 }, };
    }

}
