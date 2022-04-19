package com.autodesk.bsm.pelican.api.user;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class UserRoleTest extends SeleniumWebdriver {

    private RolesHelper rolesHelper;
    private final Map<String, String> requestParams = new HashMap<>();
    private Map<String, String> roleList = new HashMap<>();
    private Map<String, String> responseMap = new HashMap<>();
    private static final String qaRole = Role.QA_ONLY.getValue();
    private static final String adminRole = "Admin";
    private static final String csatRole = "CSAT Promotion Representative";
    private static final String invalidRole = "999999";
    private static final int SUCCESS = 204;
    private static final String RESPONSE_CODE = "Responsecode";
    private static final String userExternalKey = "Automation_user_role_test";

    private final JSONObject request = new JSONObject();
    private final JSONArray dataArray = new JSONArray();
    private final JSONObject dataObject1 = new JSONObject();
    private final JSONObject dataObject2 = new JSONObject();
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        roleList = DbUtils.getAllRoles(getEnvironmentVariables());
        rolesHelper = new RolesHelper(getEnvironmentVariables());

        requestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        requestParams.put(UserParameter.NAME.getName(), userExternalKey);
        requestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());

        // actor id needed for actor based tests
        String actorId = rolesHelper.getActor();
        if (null == actorId) {
            final String externalKey = "$TestActor_Externalkey_" + RandomStringUtils.randomAlphabetic(8);
            // Adding actor with name, external key and name space id
            final AddActorPage addActorPage = adminToolPage.getPage(AddActorPage.class);
            addActorPage.addActor(externalKey);
            final ActorDetailsPage actorDetailsPage = addActorPage.clickOnSave();
            actorId = actorDetailsPage.getId();
        }
        requestParams.put("actorID", actorId);

        final User user = new UserUtils().createPelicanUser(requestParams, getEnvironmentVariables());
        requestParams.put("userID", user.getId());
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        dataObject1.clear();
        dataObject2.clear();
        dataArray.clear();
        request.clear();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignOneRoleToUserValidScenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER
                .info("Role " + roleList.get(qaRole) + " successfully assgined to User " + requestParams.get("userID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert.fail("Role " + roleList.get(qaRole) + " failed to assgined to User " + requestParams.get("userID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignOneRoleToUserValidScenario" })
    public void testAssignMultipleRolesToUserValidscenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(adminRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Role " + dataArray.toJSONString() + " successfully assgined to User " + requestParams.get("userID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert
                .fail("Role " + roleList.get(qaRole) + " successfully assgined to User " + requestParams.get("userID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignRoleToUserForInvalidUser() {
        final HashMap<String, String> tempRequestParams = new HashMap<>();
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("userID", "99999999");

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());
        responseMap = rolesHelper.assignUserRole(tempRequestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid User, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignRoleToUserWithInvalidRole() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", invalidRole);
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignUserRole(requestParams);
        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid Role, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignRoleToUserInInvalidAppFamily() {

        final HashMap tempRequestParams = new HashMap();
        tempRequestParams.put("changeAppFamilyID", true);
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), "9");
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("userID", requestParams.get("userID"));

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignUserRole(tempRequestParams);
        AssertCollector.assertThat("Response code is not correct.", Integer.parseInt(responseMap.get(RESPONSE_CODE)),
            equalTo(HttpStatus.SC_UNAUTHORIZED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignOneRoleToUserValidScenario" })
    public void testAssignSameRoleTwiceToUser() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Assigned Role failed because of duplicate role which is already assigned to user," + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignMultipleRolesToUserValidscenario" })
    public void testAssignMultipleRolesToUserAlreadyHasSubsetOfRolesAssigned() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(csatRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Assigned Role failed because subset of Roles are already assigned to the User," + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignOneRoleToActorValidscenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Role " + roleList.get(qaRole) + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert.fail(
                "Role " + roleList.get(qaRole) + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignOneRoleToActorValidscenario" })
    public void testAssignMultipleRolesToActorValidScenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(adminRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Role " + dataArray.toJSONString() + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert.fail(
                "Role " + roleList.get(qaRole) + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignRoleToInvalidActor() {
        final HashMap<String, String> tempRequestParams = new HashMap<>();
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("actorID", "99999999");

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(tempRequestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid Actor, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignRoleToActorWithInvalidRole() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", invalidRole);
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid Role, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignRoleToActorWithInvalidAppFamily() {

        final HashMap tempRequestParams = new HashMap();
        tempRequestParams.put("changeAppFamilyID", true);
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), "9");
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("actorID", requestParams.get("actorID"));

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);

        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(tempRequestParams);
        AssertCollector.assertThat("Response code is not correct.", Integer.parseInt(responseMap.get(RESPONSE_CODE)),
            equalTo(HttpStatus.SC_UNAUTHORIZED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignOneRoleToActorValidscenario" })
    public void testAssignSameRoleTwiceToActor() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info("Assigned Role failed because of duplicate role which is already assigned to Actor,"
                + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignMultipleRolesToActorValidScenario" })
    public void testAssignMultipleRolesToActorAlreadyHasSubsetOfRolesAssigned() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(csatRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.assignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Assigned Role failed because subset of Roles are already assigned to the Actor," + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignOneRoleToUserValidScenario" })
    public void testUnassignOneRoleToUserValidScenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER
                .info("Role " + roleList.get(qaRole) + " successfully assgined to User " + requestParams.get("userID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert.fail("Role " + roleList.get(qaRole) + " failed to assgined to User " + requestParams.get("userID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testUnassignOneRoleToUserValidScenario" })
    public void testUnassignMultipleRolesToUserValidScenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(adminRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Role " + dataArray.toJSONString() + " successfully assgined to User " + requestParams.get("userID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert
                .fail("Role " + roleList.get(qaRole) + " successfully assgined to User " + requestParams.get("userID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnassignRoleToUserForInvalidUser() {
        final HashMap<String, String> tempRequestParams = new HashMap<>();
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("userID", "99999999");

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(tempRequestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid User, test case Pass");

        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnassignRoleToUserWithInvalidRole() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", invalidRole);
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid Role, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnassignRoleToUserWithInvalidAppFamily() {

        final HashMap tempRequestParams = new HashMap();
        tempRequestParams.put("changeAppFamilyID", true);
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), "9");
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("userID", requestParams.get("userID"));

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(tempRequestParams);

        AssertCollector.assertThat("Response code is not correct.", Integer.parseInt(responseMap.get(RESPONSE_CODE)),
            equalTo(HttpStatus.SC_UNAUTHORIZED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testUnassignOneRoleToUserValidScenario" })
    public void testUnassignSameRoleTwiceToUser() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Assigned Role failed because of duplicate role which is already assigned to user," + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testUnassignMultipleRolesToUserValidScenario" })
    public void testUnassignMultipleRolesToUserAlreadyHasSubsetOfRolesAssigned() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(csatRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignUserRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Assigned Role failed because subset of Roles are already assigned to the User," + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testAssignOneRoleToActorValidscenario" })
    public void testUnassignOneRoleToActorValidScenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Role " + roleList.get(qaRole) + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert.fail(
                "Role " + roleList.get(qaRole) + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testUnassignOneRoleToActorValidScenario" })
    public void testUnassignMultipleRolesToActorValidsSenario() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(adminRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Role " + dataArray.toJSONString() + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            Assert.fail(
                "Role " + roleList.get(qaRole) + " successfully assgined to Actor " + requestParams.get("actorID"));
        } else {
            Assert.fail("Unknow HTTP error expected 204 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnassignRoleToInvalidActor() {
        final HashMap tempRequestParams = new HashMap();
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("actorID", "99999999");

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignActorRole(tempRequestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid Actor, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnassignRoleToActorWithInvalidRole() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", invalidRole);
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());
        responseMap = rolesHelper.unassignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == HttpStatus.SC_BAD_REQUEST) {
            LOGGER.info("Assigned Role failed because of invalid Role, test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnassignRoleToActorInInvalidAppFamily() {

        final HashMap tempRequestParams = new HashMap();
        tempRequestParams.put("changeAppFamilyID", true);
        tempRequestParams.put(UserParameter.APPLICATION_FAMILY.getName(), "9");
        tempRequestParams.put(UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        tempRequestParams.put("actorID", requestParams.get("actorID"));

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        tempRequestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignActorRole(tempRequestParams);

        AssertCollector.assertThat("Response code is not correct.", Integer.parseInt(responseMap.get(RESPONSE_CODE)),
            equalTo(HttpStatus.SC_UNAUTHORIZED), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testUnassignMultipleRolesToActorValidsSenario" })
    public void testUnassignSameRoleTwiceToActor() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info("Assigned Role failed because of duplicate role which is already assigned to Actor,"
                + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = { "testUnassignMultipleRolesToActorValidsSenario" })
    public void testUnassignMultipleRolesToActorAlreadyHasSubsetOfRolesAssigned() {

        dataObject1.put("type", "roles");
        dataObject1.put("id", roleList.get(qaRole));
        dataArray.add(dataObject1);
        dataObject2.put("type", "roles");
        dataObject2.put("id", roleList.get(csatRole));
        dataArray.add(dataObject2);
        request.put("data", dataArray);
        requestParams.put("body", request.toJSONString());

        responseMap = rolesHelper.unassignActorRole(requestParams);

        if (Integer.parseInt(responseMap.get(RESPONSE_CODE)) == SUCCESS) {
            LOGGER.info(
                "Assigned Role failed because subset of Roles are already assigned to the Actor," + "test case Pass");
        } else {
            Assert.fail("Unknow HTTP error expected 400 and received " + responseMap.get(RESPONSE_CODE));
        }
    }
}
