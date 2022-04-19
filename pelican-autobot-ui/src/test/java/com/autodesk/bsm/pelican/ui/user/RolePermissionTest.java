package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.helper.auditlog.RoleAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddRolePage;
import com.autodesk.bsm.pelican.ui.pages.users.EditRolePage;
import com.autodesk.bsm.pelican.ui.pages.users.FindRolePage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleSearchResultsPage;
import com.autodesk.bsm.pelican.ui.pages.users.SetRolePermissionsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This is a test class which have the test methods to test the audit logs on role creation/edit/deletion
 *
 * @author vineel
 */
public class RolePermissionTest extends SeleniumWebdriver {

    private AddRolePage addRolePage;
    private RoleDetailPage roleDetailPage;
    private EditRolePage editRolePage;
    private static HashMap<String, String> propertiesMap;
    private static String updatedRoleId;
    private static String updatedRoleName;
    private static String updatedRoleDescription;
    private static final String ROLE_NAME = "Test Role";
    private static final String ROLE_DESCRIPTION = "A Role for Testing";
    private static final String UPDATE_ROLE_NAME = "Test Role1";
    private static final String UPDATE_ROLE_DESCRIPTION = "A Role1 for Testing";
    private static final String PROPERTY_NAME1 = "TestName1";
    private static final String PROPERTY_NAME2 = "TestName2";
    private static final String PROPERTY_VALUE1 = "TestValue1";
    private static final String PROPERTY_VALUE2 = "TestValue2";
    private String userId;
    private List<String> setPermissions;
    private List<String> unsetPermissions;
    private static String permissionString;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        userId = getEnvironmentVariables().getGetUserIdInTwoFishFamily();

        final AdminToolPage adminToolTwofishUser = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolTwofishUser.login(getEnvironmentVariables().getRoleAppFamilyID(),
            getEnvironmentVariables().getUserName(), getEnvironmentVariables().getPassword());
        addRolePage = adminToolTwofishUser.getPage(AddRolePage.class);
        roleDetailPage = adminToolTwofishUser.getPage(RoleDetailPage.class);
        editRolePage = adminToolTwofishUser.getPage(EditRolePage.class);
        final FindRolePage findRolePage = adminToolTwofishUser.getPage(FindRolePage.class);
        final RoleSearchResultsPage roleSearchResultsPage = adminToolTwofishUser.getPage(RoleSearchResultsPage.class);

        propertiesMap = new HashMap<>();
        propertiesMap.put(PROPERTY_NAME1, PROPERTY_VALUE1);
        propertiesMap.put(PROPERTY_NAME2, PROPERTY_VALUE2);

        // List of permission to assign
        setPermissions = new ArrayList<>();
        setPermissions.add("user.add");
        setPermissions.add("user.edit");
        setPermissions.add("user.delete");

        // List of permission to unassign
        unsetPermissions = new ArrayList<>();
        unsetPermissions.add("user.add");
        unsetPermissions.add("user.edit");

        permissionString = getPermissionString(Arrays.asList("user.add", "user.edit"));

        // Find a Role By Name and delete that role if it exists as we are
        // creating the same role in test method
        final GenericGrid roleSearchResult = findRolePage.findRoleByName(ROLE_NAME);
        final String roleSearchText = roleSearchResultsPage.getRoleSearchResultsText();
        if (!(roleSearchText.equalsIgnoreCase(PelicanConstants.NONE_FOUND))) {
            final RoleDetailPage roleDetailPage = roleSearchResultsPage.viewFoundRole(roleSearchResult);
            roleDetailPage.deleteRole();
            Util.waitInSeconds(TimeConstants.THREE_SEC);
        }

        // Find a Role By Name and delete that role if it exists as we are
        // creating the same role in test method
        final GenericGrid updatedRoleSearchResult = findRolePage.findRoleByName(UPDATE_ROLE_NAME);
        final String updatedRoleSearchText = roleSearchResultsPage.getRoleSearchResultsText();
        if (!(updatedRoleSearchText.equalsIgnoreCase(PelicanConstants.NONE_FOUND))) {
            final RoleDetailPage roleDetailPage = roleSearchResultsPage.viewFoundRole(updatedRoleSearchResult);
            roleDetailPage.deleteRole();
            Util.waitInSeconds(TimeConstants.THREE_SEC);
        }

    }

    /**
     * This is a test method to add a role in admin tool and check the audit log entry
     */
    @Test
    public void testAuditLogForAddRole() {
        roleDetailPage = addRolePage.addRole(ROLE_NAME, ROLE_DESCRIPTION);
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // Assert the created role
        AssertCollector.assertThat("Incorrect role id", roleDetailPage.getRoleId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect role name", roleDetailPage.getRoleName(), equalTo(ROLE_NAME),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect role id", roleDetailPage.getRoleDescription(), equalTo(ROLE_DESCRIPTION),
            assertionErrorList);

        // Verify the CREATE role audit data
        RoleAuditLogHelper.validateRoleData(roleDetailPage.getRoleId(), null, null, null, null,
            roleDetailPage.getRoleName(), roleDetailPage.getRoleDescription(), null, null, Action.CREATE, userId,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method validates assigning Permission to Role thorough Admin Tool gets capture into Audit log or not.
     */
    @Test(dependsOnMethods = "testAuditLogForAddRole")
    public void testAssigningPermissionToRoleIsUpdatedInAuditLog() {

        // Navigating to Set Role Permission Page
        final SetRolePermissionsPage rolePermissionsPage = roleDetailPage.clickOnSetPermissionLink();

        // Assign permission to Role
        rolePermissionsPage.selectPermissions(setPermissions);
        roleDetailPage = rolePermissionsPage.clickOnUpdatePermission();

        final String newPermission = getPermissionString(setPermissions);

        // Verify the UPDATE role audit data
        RoleAuditLogHelper.validateRoleData(roleDetailPage.getRoleId(), null, null, null, null, null, null, null,
            newPermission, Action.UPDATE, userId, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method validates unassigning Permission to Role thorough Admin Tool gets capture into Audit log or not.
     */
    @Test(dependsOnMethods = "testAssigningPermissionToRoleIsUpdatedInAuditLog")
    public void testUnassigningPermissionToRoleIsUpdatedInAuditLog() {

        // Navigating to Set Role Permission Page
        final SetRolePermissionsPage rolePermissionsPage = roleDetailPage.clickOnSetPermissionLink();

        // get permission already assigned
        final String oldPermission = getPermissionString(setPermissions);

        // Unassign permission to Role
        rolePermissionsPage.unSelectPermissions(unsetPermissions);
        roleDetailPage = rolePermissionsPage.clickOnUpdatePermission();

        // Final permission
        final String newPermission = getPermissionString(Arrays.asList("user.delete"));

        // Verify the UPDATE role audit data
        RoleAuditLogHelper.validateRoleData(roleDetailPage.getRoleId(), null, null, null, oldPermission, null, null,
            null, newPermission, Action.UPDATE, userId, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method validates Multiple Permission assigning and unassigning to the Role thorough Admin Tool gets
     * capture into Audit log or not.
     */
    @Test(dependsOnMethods = "testUnassigningPermissionToRoleIsUpdatedInAuditLog")
    public void testMultipleAssigningPermissionToRoleIsUpdatedInAuditLog() {

        // Data setup
        setPermissions.clear();
        unsetPermissions.clear();

        setPermissions.add("user.add");
        setPermissions.add("user.edit");

        unsetPermissions.add("user.delete");

        // Navigating to Set Role Permission Page
        SetRolePermissionsPage rolePermissionsPage = roleDetailPage.clickOnSetPermissionLink();

        // Assign permission to Role
        rolePermissionsPage.selectPermissions(setPermissions);
        roleDetailPage = rolePermissionsPage.clickOnUpdatePermission();

        final String oldPermission = getPermissionString(Arrays.asList("user.add", "user.edit", "user.delete"));

        // Navigating to Set Role Permission Page
        rolePermissionsPage = roleDetailPage.clickOnSetPermissionLink();

        // Unassign permission to Role
        rolePermissionsPage.unSelectPermissions(unsetPermissions);
        roleDetailPage = rolePermissionsPage.clickOnUpdatePermission();

        final String newPermission = getPermissionString(Arrays.asList("user.add", "user.edit"));

        // Verify the UPDATE role audit data
        RoleAuditLogHelper.validateRoleData(roleDetailPage.getRoleId(), null, null, null, oldPermission, null, null,
            null, newPermission, Action.UPDATE, userId, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method to update a role in admin tool and check the audit log entry
     */
    @Test(dependsOnMethods = "testMultipleAssigningPermissionToRoleIsUpdatedInAuditLog")
    public void testAuditLogForUpdateRole() {
        roleDetailPage.clickOnEditButton();
        editRolePage.editRole(UPDATE_ROLE_NAME, UPDATE_ROLE_DESCRIPTION);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        updatedRoleId = roleDetailPage.getRoleId();
        updatedRoleName = roleDetailPage.getRoleName();
        updatedRoleDescription = roleDetailPage.getRoleDescription();

        // Assert the created role
        AssertCollector.assertThat("Incorrect role id", updatedRoleId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect role name", updatedRoleName, equalTo(UPDATE_ROLE_NAME),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect role id", updatedRoleDescription, equalTo(UPDATE_ROLE_DESCRIPTION),
            assertionErrorList);

        // Verify the UPDATE role audit data
        RoleAuditLogHelper.validateRoleData(roleDetailPage.getRoleId(), ROLE_NAME, ROLE_DESCRIPTION, null, null,
            updatedRoleName, updatedRoleDescription, null, null, Action.UPDATE, userId, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method to update the properties for a role in admin tool and check the audit log entry
     */
    @Test(dependsOnMethods = "testAuditLogForUpdateRole")
    public void testAuditLogForUpdatePropertiesForRole() {
        roleDetailPage.updateProperties(propertiesMap);
        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // Verify the UPDATE role audit data
        RoleAuditLogHelper.validateRoleData(roleDetailPage.getRoleId(), null, null, null, null,
            roleDetailPage.getRoleName(), roleDetailPage.getRoleDescription(), propertiesMap, null, Action.UPDATE,
            userId, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method to delete a role and check the audit log entry
     */
    @Test(dependsOnMethods = "testAuditLogForUpdatePropertiesForRole")
    public void testAuditLogForDeleteRole() {
        roleDetailPage.deleteRole();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        // Verify the UPDATE role audit data
        RoleAuditLogHelper.validateRoleData(updatedRoleId, updatedRoleName, updatedRoleDescription, propertiesMap,
            permissionString, null, null, null, null, Action.DELETE, userId, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Get string representation of a Audit Log Permission
     *
     * @param params set
     * @return String representation of the audit log permission (Ex: "[permission1, permission2]")
     */
    private static String getPermissionString(final List<String> params) {
        if (params.size() == 0) {
            return null;
        }

        final StringBuilder paramsStrBuilder = new StringBuilder();
        for (final String param : params) {
            paramsStrBuilder.append(param).append(", ");
        }

        final String paramsStr = paramsStrBuilder.toString();

        // Remove the extra comma space added at the end.
        return "[" + paramsStr.substring(0, paramsStr.length() - 2) + "]";
    }

}
