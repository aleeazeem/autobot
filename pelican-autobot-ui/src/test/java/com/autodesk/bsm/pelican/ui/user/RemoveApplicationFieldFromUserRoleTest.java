package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.UserStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddRoleAssignmentPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentCompletePage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a test class which will test whether the application field is removed from adding a role to the user page
 *
 * @author yerragv.
 */
public class RemoveApplicationFieldFromUserRoleTest extends SeleniumWebdriver {

    private AddUserPage addUserPage;
    private UserDetailsPage userDetailsPage;
    private AddRoleAssignmentPage addRoleAssignmentPage;
    private static final String USER_NAME = "UserName_" + RandomStringUtils.randomAlphanumeric(8);
    private static final String USER_EXTERNAL_KEY = "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private static final String SQL_QUERY =
        "select count(*) from sec_role_assign where appf_id=2001 and app_id is not null";
    private static final String FIELD_NAME = "count(*)";
    private static final Logger LOGGER =
        LoggerFactory.getLogger(RemoveApplicationFieldFromUserRoleTest.class.getSimpleName());

    /**
     * This is a before class method.
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        addUserPage = adminToolPage.getPage(AddUserPage.class);
        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        addRoleAssignmentPage = adminToolPage.getPage(AddRoleAssignmentPage.class);
    }

    /**
     * This is a test method which will test whether the application field is hidden from the UI.
     */
    @Test
    public void testApplicationFieldIsHiddenOnRoleAssignmentPage() {

        addUserPage.addUser(USER_NAME, USER_EXTERNAL_KEY, UserStatus.ENABLED);
        userDetailsPage = addUserPage.clickOnSave();

        final String namedPartyId = userDetailsPage.getId();
        LOGGER.info("User id:" + namedPartyId);

        addRoleAssignmentPage = userDetailsPage.clickOnAddRoleAssignmentLink();

        // Define Role to Assign to User
        final Role selectRole = Role.ADMIN;
        final List<Role> assignRole = new ArrayList<>();
        assignRole.add(selectRole);
        addRoleAssignmentPage.selectRole(assignRole);

        final RoleAssignmentCompletePage roleAssignmentCompletePage = addRoleAssignmentPage.clickOnAssignRole();

        // Assert on the created role
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentCompletePage.getRoles(),
            equalTo(selectRole.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect application value for the role",
            roleAssignmentCompletePage.getApplication(), isEmptyOrNullString(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Named Party", roleAssignmentCompletePage.getNamedParty(),
            equalTo(USER_NAME), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test whether the application value is null for all roles in DB.
     */
    @Test
    public void testApplicationFieldIsNullForAllRolesInDb() {

        final int countOfRecords =
            Integer.parseInt(DbUtils.selectQuery(SQL_QUERY, FIELD_NAME, getEnvironmentVariables()).get(0));
        AssertCollector.assertThat("Incorrect count of records with application value not null in the db",
            countOfRecords, equalTo(0), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
