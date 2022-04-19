package com.autodesk.bsm.pelican.ui.actors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.helper.auditlog.RoleAssignmentAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.actors.FindActorsPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddRoleAssignmentPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentCompletePage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentDeletePage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentResultPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class test all the scenarios of adding, updating, removing Role to a Actor
 *
 * @author Vaibhavi
 */
public class ActorRoleTest extends SeleniumWebdriver {

    private static final String ACTOR_EXTERNAL_KEY =
        "$TestActor_Externalkey_" + RandomStringUtils.randomAlphanumeric(8);
    private AddActorPage addActorPage;
    private FindActorsPage findActorsPage;
    private ActorDetailsPage actorDetailsPage;
    private static HashMap<String, String> propertiesMap;
    private static final String PROPERTY_NAME1 = "TestName1";
    private static final String PROPERTY_NAME2 = "TestName2";
    private static final String PROPERTY_VALUE1 = "TestValue1";
    private static final String PROPERTY_VALUE2 = "TestValue2";
    private String userId;
    private String actorId;
    private boolean isAuditLogFound = false;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        userId = getEnvironmentVariables().getUserId();

        addActorPage = adminToolPage.getPage(AddActorPage.class);
        actorDetailsPage = adminToolPage.getPage(ActorDetailsPage.class);
        findActorsPage = adminToolPage.getPage(FindActorsPage.class);

        propertiesMap = new HashMap<>();
        propertiesMap.put(PROPERTY_NAME1, PROPERTY_VALUE1);
        propertiesMap.put(PROPERTY_NAME2, PROPERTY_VALUE2);
    }

    /**
     * Delete Actor with Tear Down Method.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        findActorsPage.findByExternalKey(ACTOR_EXTERNAL_KEY);
        actorDetailsPage = findActorsPage.clickOnSubmit(1);
        actorDetailsPage.clickOnDeleteActorConfirm();
        AssertCollector.assertThat("Actor is not Deleted Successfully", findActorsPage.getMessageOnDelete(),
            equalTo(String.format("Successfully deleted actor %s", ACTOR_EXTERNAL_KEY)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate that Role is Added to the Actor through Admin Tool
     *
     * @Result: Page Displays the Detail Page Actor
     */
    @Test
    public void verifyRoleIsAddedToActor() {

        // Define Role to Assign to Actor
        final Role selectRole = Role.ADMIN;

        addActorPage.addActor(ACTOR_EXTERNAL_KEY);
        actorDetailsPage = addActorPage.clickOnSave();

        actorId = actorDetailsPage.getId();
        final AddRoleAssignmentPage addRoleAssignmentPage = actorDetailsPage.clickOnAddRoleAssignmentLink();
        final List<Role> assignRole = new ArrayList<>();
        assignRole.add(selectRole);

        addRoleAssignmentPage.selectRole(assignRole);
        final RoleAssignmentCompletePage roleAssignmentCompletePage = addRoleAssignmentPage.clickOnAssignRole();

        // Assert on the created role
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentCompletePage.getRoles(),
            equalTo(selectRole.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Named Party", roleAssignmentCompletePage.getNamedParty(),
            equalTo(ACTOR_EXTERNAL_KEY), assertionErrorList);

        // Search Actor
        findActorsPage.findByExternalKey(ACTOR_EXTERNAL_KEY);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(1);

        // Click on Role Assignment Link , It navigates to Role Assignment Result Page
        final RoleAssignmentResultPage roleAssignmentResultPage = actorDetailsPage.clickOnShowRoleAssignmentLink();

        final RoleAssignmentDetailPage roleAssignmentDetailPage =
            roleAssignmentResultPage.selectRole(actorId, selectRole.getValue());

        // Verify the Create role Assignment audit data
        isAuditLogFound = RoleAssignmentAuditLogHelper.helperToValidateDynamoDbForRoleAssignment(
            roleAssignmentDetailPage.getRoleAssignmentId(), null, getEnvironmentVariables().getAppFamilyId(), null,
            actorId, null, null, null, null, null, Action.CREATE, userId, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate that Role is Modified for the Actor through Admin Tool
     *
     * @Result: Page Displays the Detail Page Actor
     */

    @Test(dependsOnMethods = "verifyRoleIsAddedToActor")
    public void verifyRoleIsModifiedForActor() {

        // Define Role to Modify
        final Role selectRole = Role.ADMIN;

        // Search Actor
        findActorsPage.findByExternalKey(ACTOR_EXTERNAL_KEY);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(1);

        // Click on Role Assignment Link , It navigates to Role Assignment Result Page
        final RoleAssignmentResultPage roleAssignmentResultPage = actorDetailsPage.clickOnShowRoleAssignmentLink();

        RoleAssignmentDetailPage roleAssignmentDetailPage =
            roleAssignmentResultPage.selectRole(actorId, selectRole.getValue());

        // Assert the updated role
        AssertCollector.assertThat("Incorrect Role Assignment ID", roleAssignmentDetailPage.getRoleAssignmentId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentDetailPage.getRoleName(),
            equalTo(Role.ADMIN.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Named Party", roleAssignmentDetailPage.getNamedParty(),
            equalTo(ACTOR_EXTERNAL_KEY), assertionErrorList);

        roleAssignmentDetailPage = roleAssignmentDetailPage.updateProperties(propertiesMap);

        // Verify the Create role Assignment audit data
        isAuditLogFound = RoleAssignmentAuditLogHelper.helperToValidateDynamoDbForRoleAssignment(
            roleAssignmentDetailPage.getRoleAssignmentId(), null, null, null, null, null, null, null, propertiesMap,
            null, Action.UPDATE, userId, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Validate that Role is Deleted for the Actor through Admin Tool
     *
     * @Result: Page Displays the Detail Page Actor
     */
    @Test(dependsOnMethods = "verifyRoleIsModifiedForActor")
    public void verifyRoleIsDeletedForActor() {

        // Define Role to Delete
        final Role selectRole = Role.ADMIN;

        findActorsPage.findByExternalKey(ACTOR_EXTERNAL_KEY);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(1);

        // Click on Role Assignment Link , It navigates to Role Assignment
        // Result Page
        final RoleAssignmentResultPage roleAssignmentResultPage = actorDetailsPage.clickOnShowRoleAssignmentLink();

        final RoleAssignmentDetailPage roleAssignmentDetailPage =
            roleAssignmentResultPage.selectRole(actorId, selectRole.getValue());

        final RoleAssignmentDeletePage roleAssignmentDeletePage = roleAssignmentDetailPage.deleteRole();
        final String roleAssignmentId = roleAssignmentDetailPage.getRoleAssignmentId();

        // Assert the deleted role
        AssertCollector.assertThat("Incorrect Role Assignment ID", roleAssignmentId, notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Role Name", roleAssignmentDeletePage.getRoleName(),
            equalTo(Role.ADMIN.getValue()), assertionErrorList);

        // Verify the Delete role Assignment audit data
        isAuditLogFound = RoleAssignmentAuditLogHelper.helperToValidateDynamoDbForRoleAssignment(
            roleAssignmentDeletePage.getRoleAssignmentId(), getEnvironmentVariables().getAppFamilyId(), null, actorId,
            null, null, null, propertiesMap, null, roleAssignmentId, Action.DELETE, userId, assertionErrorList);
        AssertCollector.assertTrue("Audit log not found", isAuditLogFound, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

}
