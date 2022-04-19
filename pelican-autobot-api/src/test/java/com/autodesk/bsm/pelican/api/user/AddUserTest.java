package com.autodesk.bsm.pelican.api.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddRoleAssignmentPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;

import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class tests adding user using "Add User" API
 *
 * @author Shweta Hegde
 */
public class AddUserTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private PelicanPlatform resource;
    private HttpError httpError;
    private Map<String, String> userRequestParam;
    private Object apiResponse;
    private User user;
    private String userId;
    private final JSONObject request = new JSONObject();
    private final JSONArray dataArray = new JSONArray();
    private final JSONObject dataObject1 = new JSONObject();
    private List<String> selectResult;
    private static final String NOT_ADMIN_TOOL_USER = "0";
    private static final String ADMIN_TOOL_USER = "1";
    private RolesHelper rolesHelper;
    private final Map<String, String> requestParams = new HashMap<>();
    private Map<String, String> roleList = new HashMap<>();
    private static final String QA_ROLE = Role.QA_ONLY.getValue();
    private String userExternalKey;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        roleList = DbUtils.getAllRoles(getEnvironmentVariables());
        rolesHelper = new RolesHelper(getEnvironmentVariables());
    }

    /**
     * This test method tests adding user through "Add User" API This also verifies that user created through API is not
     * a Admin Tool User
     */
    @Test
    public void testAddUser() {

        final String userName = RandomStringUtils.randomAlphabetic(8);
        userExternalKey = RandomStringUtils.randomAlphanumeric(12);

        // Set the request
        userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), userName);
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);

        // Add user
        apiResponse = resource.user().addUser(userRequestParam);

        if (apiResponse instanceof HttpError) {

            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(User.class), assertionErrorList);
        } else {

            user = (User) apiResponse;

            userId = user.getId();
            // Query to verify that added user is not a admin tool user
            selectResult = DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where id = " + userId,
                "IS_ADMINTOOL_USER", getEnvironmentVariables());
            AssertCollector.assertThat("User should NOT be a Admin Tool user", selectResult.get(0),
                equalTo(NOT_ADMIN_TOOL_USER), assertionErrorList);
            AssertCollector.assertThat("Id should not be null", userId, notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect username", user.getName(), equalTo(userName), assertionErrorList);
            AssertCollector.assertThat("Incorrect user external key", user.getExternalKey(), equalTo(userExternalKey),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests user added through API, if given roles, then will become a Admin Tool User Roles are assigned
     * using "Assign Roles to the User" API
     */
    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAddUser" })
    public void testAssignRolesToUserCreatedByApi() {

        // Adding QA role
        dataObject1.clear();
        dataArray.clear();
        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(QA_ROLE));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        requestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        requestParams.put("body", request.toJSONString());
        requestParams.put("userID", userId);

        // Assign role to the created user
        rolesHelper.assignUserRole(requestParams);

        // User will become a Admin Tool User
        selectResult = DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where id = " + userId,
            "IS_ADMINTOOL_USER", getEnvironmentVariables());
        AssertCollector.assertThat("User should be a Admin Tool user", selectResult.get(0), equalTo(ADMIN_TOOL_USER),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test, if all roles are removed from a user, then user will still be Admin Tool User Roles are
     * unassigned using "Delete roles from the user" API
     */
    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignRolesToUserCreatedByApi" })
    public void testUnAssignRolesToUserCreatedByApi() {

        // Unassign QA role
        dataObject1.clear();
        dataArray.clear();
        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(QA_ROLE));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        rolesHelper.unassignUserRole(requestParams);

        // Verify User is still a admin tool user
        selectResult = DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where id = " + userId,
            "IS_ADMINTOOL_USER", getEnvironmentVariables());
        AssertCollector.assertThat("User should be a Admin Tool user", selectResult.get(0), equalTo(ADMIN_TOOL_USER),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests user added by API and given roles through Admin Tool User is a Admin Tool User
     */
    @Test
    public void addUserThroughApiAndAssignRolesInAdminTool() {

        final String userName = RandomStringUtils.randomAlphabetic(8);
        userExternalKey = RandomStringUtils.randomAlphanumeric(12);

        userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), userName);
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);

        // Create a user through API, user will not be a Admin Tool User
        apiResponse = resource.user().addUser(userRequestParam);

        if (apiResponse instanceof HttpError) {

            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(User.class), assertionErrorList);
        } else {
            user = (User) apiResponse;

            userId = user.getId();
            final FindUserPage findUserPage = adminToolPage.getPage(FindUserPage.class);
            final UserDetailsPage userDetailsPage = findUserPage.getById(userId);
            final AddRoleAssignmentPage roleAssignmentPage = userDetailsPage.clickOnAddRoleAssignmentLink();

            // Assign roles to the user
            final List<Role> roles = new ArrayList<>();
            roles.add(Role.QA_ONLY);
            roles.add(Role.READ_ONLY);
            roleAssignmentPage.selectRole(roles);
            roleAssignmentPage.clickOnAssignRole();

            selectResult = DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where id = " + userId,
                "IS_ADMINTOOL_USER", getEnvironmentVariables());
            AssertCollector.assertThat("User should be a Admin Tool user", selectResult.get(0),
                equalTo(ADMIN_TOOL_USER), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
