package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.enums.UserStatus;
import com.autodesk.bsm.pelican.helper.auditlog.RoleAssignmentAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddRoleAssignmentPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentCompletePage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentDeletePage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentResultPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class test all the scenarios of adding, updating, removing Role to a User
 *
 * @author Vaibhavi
 */

public class UserRoleTest extends SeleniumWebdriver {

    private static final String USER_NAME = "UserName_" + RandomStringUtils.randomAlphanumeric(8);
    private static final String USER_EXTERNAL_KEY = "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private AddUserPage addUserPage;
    private FindUserPage findUserPage;
    private UserDetailsPage userDetailsPage;
    private static HashMap<String, String> propertiesMap;
    private static final String PROPERTY_NAME1 = "TestName1";
    private static final String PROPERTY_NAME2 = "TestName2";
    private static final String PROPERTY_VALUE1 = "TestValue1";
    private static final String PROPERTY_VALUE2 = "TestValue2";
    private String userId;
    private String namedPartyId;
    private boolean isAuditLogFound = false;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        userId = getEnvironmentVariables().getUserId();

        addUserPage = adminToolPage.getPage(AddUserPage.class);
        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        findUserPage = adminToolPage.getPage(FindUserPage.class);

        propertiesMap = new HashMap<>();
        propertiesMap.put(PROPERTY_NAME1, PROPERTY_VALUE1);
        propertiesMap.put(PROPERTY_NAME2, PROPERTY_VALUE2);
    }

    /**
     * Validate that Role is Added to the User through Admin Tool
     *
     * @Result: Page Displays the Detail Page User
     */
    @Test
    public void verifyRoleIsAddedToUser() {

        // Define Role to Assign to User
        final Role selectRole = Role.ADMIN;

        addUserPage.addUser(USER_NAME, USER_EXTERNAL_KEY, UserStatus.ENABLED);
        userDetailsPage = addUserPage.clickOnSave();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        namedPartyId = userDetailsPage.getId();
        final AddRoleAssignmentPage addRoleAssignmentPage = userDetailsPage.clickOnAddRoleAssignmentLink();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final List<Role> assignRole = new ArrayList<>();
        assignRole.add(selectRole);
        addRoleAssignmentPage.selectRole(assignRole);

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final RoleAssignmentCompletePage roleAssignmentCompletePage = addRoleAssignmentPage.clickOnAssignRole();

        // Assert on the created role
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentCompletePage.getRoles(),
            equalTo(selectRole.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Named Party", roleAssignmentCompletePage.getNamedParty(),
            equalTo(USER_NAME), assertionErrorList);

        // Search UserName
        userDetailsPage = findUserPage.getByName(USER_NAME);

        // Click on Role Assignment Link , It navigates to Role Assignment
        // Result Page
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final RoleAssignmentResultPage roleAssignmentResultPage = userDetailsPage.clickOnShowRoleAssignmentLink();

        final RoleAssignmentDetailPage roleAssignmentDetailPage =
            roleAssignmentResultPage.selectRole(namedPartyId, selectRole.getValue());

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // Verify the Create role Assignment audit data
        isAuditLogFound = RoleAssignmentAuditLogHelper.helperToValidateDynamoDbForRoleAssignment(
            roleAssignmentDetailPage.getRoleAssignmentId(), null, getEnvironmentVariables().getAppFamilyId(), null,
            namedPartyId, null, null, null, null, null, Action.CREATE, userId, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate that Role is Modified for the User through Admin Tool
     *
     * @Result: Page Displays the Detail Page User
     */

    @Test(dependsOnMethods = "verifyRoleIsAddedToUser")
    public void verifyRoleIsModifiedForUser() {

        // Define Role to Modify
        final Role selectRole = Role.ADMIN;

        // Search UserName
        userDetailsPage = findUserPage.getByName(USER_NAME);

        // Click on Role Assignment Link , It navigates to Role Assignment
        // Result Page
        final RoleAssignmentResultPage roleAssignmentResultPage = userDetailsPage.clickOnShowRoleAssignmentLink();

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        RoleAssignmentDetailPage roleAssignmentDetailPage =
            roleAssignmentResultPage.selectRole(namedPartyId, selectRole.getValue());

        // Assert the updated role
        AssertCollector.assertThat("Incorrect Role Assignment ID", roleAssignmentDetailPage.getRoleAssignmentId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentDetailPage.getRoleName(),
            equalTo(Role.ADMIN.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Named Party", roleAssignmentDetailPage.getNamedParty(),
            equalTo(USER_NAME), assertionErrorList);
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        roleAssignmentDetailPage = roleAssignmentDetailPage.updateProperties(propertiesMap);
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // Verify the Create role Assignment audit data
        isAuditLogFound = RoleAssignmentAuditLogHelper.helperToValidateDynamoDbForRoleAssignment(
            roleAssignmentDetailPage.getRoleAssignmentId(), null, null, null, null, null, null, null, propertiesMap,
            null, Action.UPDATE, userId, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Validate that Role is Deleted for the User through Admin Tool
     *
     * @Result: Page Displays the Detail Page User
     */
    @Test(dependsOnMethods = "verifyRoleIsModifiedForUser")
    public void verifyRoleIsDeletedForUser() {

        // Define Role to Delete
        final Role selectRole = Role.ADMIN;

        // Search UserID
        userDetailsPage = findUserPage.getByName(USER_NAME);

        // Click on Role Assignment Link , It navigates to Role Assignment
        // Result Page
        final RoleAssignmentResultPage roleAssignmentResultPage = userDetailsPage.clickOnShowRoleAssignmentLink();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        final RoleAssignmentDetailPage roleAssignmentDetailPage =
            roleAssignmentResultPage.selectRole(namedPartyId, selectRole.getValue());

        final RoleAssignmentDeletePage roleAssignmentDeletePage = roleAssignmentDetailPage.deleteRole();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // Assert the deleted role
        AssertCollector.assertThat("Incorrect Role Assignment ID", roleAssignmentDeletePage.getRoleAssignmentId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentDeletePage.getRoleName(),
            equalTo(Role.ADMIN.getValue()), assertionErrorList);

        // Verify the Delete role Assignment audit data
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        isAuditLogFound = RoleAssignmentAuditLogHelper.helperToValidateDynamoDbForRoleAssignment(
            roleAssignmentDeletePage.getRoleAssignmentId(), getEnvironmentVariables().getAppFamilyId(), null,
            namedPartyId, null, null, null, propertiesMap, null, roleAssignmentDetailPage.getRoleAssignmentId(),
            Action.DELETE, userId, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
