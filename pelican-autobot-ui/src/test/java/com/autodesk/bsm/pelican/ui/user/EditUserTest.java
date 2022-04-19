package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.UserStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddPasswordForUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddRoleAssignmentPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.CredentialDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.EditUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class tests the Edit User functionality.
 *
 * @author t_mohag
 */
public class EditUserTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private UserUtils userUtils;
    private String user1Id;
    private static final String USERNAME = RandomStringUtils.randomAlphanumeric(10);
    private static final String USER_EXTERNAL_KEY = RandomStringUtils.randomAlphanumeric(10);
    private static final String MODIFIED_USERNAME = RandomStringUtils.randomAlphanumeric(10);
    private static final String MODIFIED_USER_EXTERNAL_KEY = RandomStringUtils.randomAlphanumeric(10);
    private static final String EBSO_USER_EXTERNAL_KEY = RandomStringUtils.randomAlphanumeric(10);
    private UserDetailsPage userDetailsPage;
    private HashMap<String, String> userParams;
    private AddUserPage addUserPage;
    private FindUserPage findUserPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(EditUserTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        userUtils = new UserUtils();

        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        addUserPage = adminToolPage.getPage(AddUserPage.class);
        findUserPage = adminToolPage.getPage(FindUserPage.class);
        userParams = new HashMap<>();
    }

    /**
     * Validate that fields should be same after canceling the editing of the user details
     */
    @Test
    public void verifyCancelEditingUser() {
        addUserPage.addUser(USERNAME, USER_EXTERNAL_KEY, UserStatus.ENABLED);
        userDetailsPage = addUserPage.clickOnSave();
        user1Id = userDetailsPage.getId();
        AssertCollector.assertThat("User is not Added", getDriver().getTitle(), equalTo("Pelican User Detail"),
            assertionErrorList);
        AssertCollector.assertThat("Id is not Generated", user1Id, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Failed to create Custom External Key", userDetailsPage.getExternalKey(),
            equalTo(USER_EXTERNAL_KEY), assertionErrorList);

        final EditUserPage editUserPage = userDetailsPage.editUser();
        editUserPage.setName(MODIFIED_USERNAME);
        editUserPage.setExternalKey(MODIFIED_USER_EXTERNAL_KEY);
        editUserPage.clickOnCancel();
        Util.waitInSeconds(TimeConstants.ONE_SEC);

        // Compare the values of fields to ensure that they have NOT changed
        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        AssertCollector.assertThat("Name should not have changed", userDetailsPage.getUserName(), equalTo(USERNAME),
            assertionErrorList);
        AssertCollector.assertThat("External key should not have changed", userDetailsPage.getExternalKey(),
            equalTo(USER_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that user details can be successfully edited.
     */
    @Test(dependsOnMethods = { "verifyCancelEditingUser" })
    public void verifyEditUserSuccess() {
        userDetailsPage.navigateToUserDetails(user1Id);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        UserDetailsPage userDetail = adminToolPage.getPage(UserDetailsPage.class);
        final EditUserPage editUserPage = userDetail.editUser();
        editUserPage.setName(MODIFIED_USERNAME);
        editUserPage.setExternalKey(MODIFIED_USER_EXTERNAL_KEY);
        userDetail = editUserPage.clickOnSave();

        // Compare the values of fields to ensure that they have changed
        AssertCollector.assertThat("Name must have changed", userDetail.getUserName(), equalTo(MODIFIED_USERNAME),
            assertionErrorList);
        AssertCollector.assertThat("External key must have changed", userDetail.getExternalKey(),
            equalTo(MODIFIED_USER_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_FIELD, userDetail.getCreated(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_BY_FIELD, userDetail.getCreatedBy(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat(
            "User's " + PelicanConstants.LAST_MODIFIED_FIELD + " must not be same as the "
                + PelicanConstants.CREATED_FIELD,
            userDetail.getLastModified(), is(not(equalTo(userDetail.getCreated()))), assertionErrorList);
        AssertCollector.assertThat(
            "User's " + PelicanConstants.LAST_MODIFIED_BY_FIELD + " is not same as the "
                + PelicanConstants.CREATED_BY_FIELD,
            userDetail.getLastModifiedBy(), equalTo(userDetail.getCreatedBy()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that user details can be successfully edited.
     */
    @Test(dependsOnMethods = { "verifyEditUserSuccess" })
    public void verifyEditUserLastModifiedByDifferentFromCreatedBy() {
        // creating and logging as ebso user
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), EBSO_USER_EXTERNAL_KEY);
        userParams.put(UserParameter.PASSWORD.getName(), getEnvironmentVariables().getPassword());
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userUtils.createAssignRoleAndLoginUser(userParams, Lists.newArrayList(Role.EBSO.getValue(),
            Role.ADMIN.getValue(), Role.READ_ONLY.getValue(), Role.APPLICATION_MANAGER.getValue()), adminToolPage,
            getEnvironmentVariables());

        // Add a role to the newly created user as NON 'svc_p_pelican' user
        userDetailsPage.navigateToUserDetails(user1Id);
        final AddRoleAssignmentPage addRoleAssignmentPage = userDetailsPage.clickOnAddRoleAssignmentLink();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final List<Role> roles = new ArrayList<>();
        roles.add(Role.READ_ONLY);
        addRoleAssignmentPage.selectRole(roles);
        addRoleAssignmentPage.clickOnAssignRole();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        adminToolPage.logout();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        adminToolPage.login();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        userDetailsPage.navigateToUserDetails(user1Id);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        AssertCollector.assertThat("Name must have changed", userDetailsPage.getUserName(), equalTo(MODIFIED_USERNAME),
            assertionErrorList);
        AssertCollector.assertThat("External key must have changed", userDetailsPage.getExternalKey(),
            equalTo(MODIFIED_USER_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_FIELD, userDetailsPage.getCreated(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_BY_FIELD, userDetailsPage.getCreatedBy(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat(
            "User's " + PelicanConstants.LAST_MODIFIED_FIELD + " must not be same as the "
                + PelicanConstants.CREATED_FIELD,
            userDetailsPage.getLastModified(), is(not(equalTo(userDetailsPage.getCreated()))), assertionErrorList);
        AssertCollector.assertThat(
            "User's " + PelicanConstants.LAST_MODIFIED_BY_FIELD + " is same as the "
                + PelicanConstants.CREATED_BY_FIELD,
            userDetailsPage.getLastModifiedBy(), is(not(equalTo(userDetailsPage.getCreatedBy()))), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the 'Created By' value of the existing user ('svc_p_pelican')
     */
    @Test
    public void verifyExistingUserCreatedByValue() {
        // Fetching the user id of Admin Tool user from database (first in the list but not "svc_p_pelican" )
        final String sqlQuery =
            "select ID, CREATED_BY_ID from NAMED_PARTY where APPF_ID = '" + getEnvironmentVariables().getAppFamilyId()
                + "' and STATE = 0 and IS_ADMINTOOL_USER = 1 AND NAME NOT LIKE '" + PelicanConstants.AUTO_USER_NAME
                + "' order by ID LIMIT 1";
        final List<Map<String, String>> resultList = DbUtils.selectQuery(sqlQuery, getEnvironmentVariables());
        final String userId = resultList.get(0).get("ID");
        final String createdByUserId = resultList.get(0).get("CREATED_BY_ID");
        if (createdByUserId != null) {
            // updating the same user's created by as null
            final String updateQuery =
                "UPDATE NAMED_PARTY SET CREATED_BY_ID = null, LAST_MODIFIED_BY_ID = null WHERE ID = " + userId;
            DbUtils.updateQuery(updateQuery, getEnvironmentVariables());
        }
        final UserDetailsPage userDetailsPage = findUserPage.getById(userId);

        // Compare the values of fields to ensure that they have changed
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_BY_FIELD, userDetailsPage.getCreatedBy(),
            equalTo(PelicanConstants.NOT_RECORDED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify the 'Last Modified' value of a user
     */
    @Test(dependsOnMethods = { "verifyCancelEditingUser" })
    public void verifyUserLastModifiedDate() {
        userDetailsPage.navigateToUserDetails(user1Id);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        UserDetailsPage userDetail = adminToolPage.getPage(UserDetailsPage.class);

        final String existingLastModified = userDetail.getLastModified();
        userDetail = userDetail.addTag("AutoTest");
        LOGGER.info("Its the first edit");

        // Verify that the 'Last Modified' is NOT updated when a tag is added.
        AssertCollector.assertThat("Invalid user " + PelicanConstants.LAST_MODIFIED + "field for 1st edit",
            userDetail.getLastModified(), equalTo(existingLastModified), assertionErrorList);

        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // Verify that the 'Last Modified' is updated when properties are
        // updated.
        userDetail = userDetail.updateProperties("isAutoTestUser", "true");
        LOGGER.info("Its the second edit");
        final String updatedLastModified1 = userDetail.getLastModified();
        AssertCollector.assertThat("Invalid user " + PelicanConstants.LAST_MODIFIED + "field for 2nd edit",
            updatedLastModified1, is(not(existingLastModified)), assertionErrorList);

        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        // Verify that the 'Last Modified' is updated when properties are
        // updated.
        final AddPasswordForUserPage addPasswordForUserPage = userDetail.clickOnAddPasswordButton();
        addPasswordForUserPage.enterPassword(getEnvironmentVariables().getPassword());
        addPasswordForUserPage.enterSecret(getEnvironmentVariables().getPassword());
        final CredentialDetailPage credentialsDetailPage = addPasswordForUserPage.clickOnAddPassword();
        LOGGER.info("Its the 3rd edit edit");
        final String createdDate = credentialsDetailPage.getCreated();
        AssertCollector.assertThat("Password is not saved", createdDate, notNullValue(), assertionErrorList);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        userDetailsPage.navigateToUserDetails(user1Id);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        userDetail = adminToolPage.getPage(UserDetailsPage.class);
        final String updatedLastModified2 = userDetail.getLastModified();
        AssertCollector.assertThat("Invalid user " + PelicanConstants.LAST_MODIFIED + "field for 3rd edit ",
            updatedLastModified2, is(not(updatedLastModified1)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
