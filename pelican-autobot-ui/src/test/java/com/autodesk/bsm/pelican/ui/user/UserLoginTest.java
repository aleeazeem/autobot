package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Role;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.HomePage;
import com.autodesk.bsm.pelican.ui.pages.applications.ApplicationFamilyDetailPage;
import com.autodesk.bsm.pelican.ui.pages.applications.ApplicationFamilyEditPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddPasswordForUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.RolesHelper;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This test class tests the login functionality for the user.
 *
 * @author Shweta Hegde
 */
public class UserLoginTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private UserUtils userUtils;
    private Map<String, String> userParams;
    private User user;
    private String userPassword;
    private RolesHelper rolesHelper;
    private Map<String, String> rolesMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        userUtils = new UserUtils();
        userPassword = getEnvironmentVariables().getPassword();
        rolesHelper = new RolesHelper(getEnvironmentVariables());
        rolesMap = DbUtils.getAllRoles(getEnvironmentVariables());
    }

    /*
     * After all test methods are run, local authentication is set to "YES" to ensure this class not affecting other
     * test results
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {

        adminToolPage.login();
        final ApplicationFamilyDetailPage applicationFamilyDetailPage =
            adminToolPage.getPage(ApplicationFamilyDetailPage.class);
        applicationFamilyDetailPage.getApplicationFamilyDetail();
        applicationFamilyDetailPage.clickEdit();

        final ApplicationFamilyEditPage applicationFamilyEditPage =
            adminToolPage.getPage(ApplicationFamilyEditPage.class);
        applicationFamilyEditPage.enableLocalAuthentication();
        applicationFamilyEditPage.updateApplicationFamily();
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        // login is needed after every method to ensure the correct login as few
        // methods are for error scenarios
        adminToolPage.login();
    }

    /**
     * This test method tests local authentication of the user when setting is set to YES Step1 : set the local
     * authentication to YES Step2 : create user with password and assign role Step3 : login successful with newly
     * created user
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSuccessUserLoginWhenLocalAuthenticationAllowed() {

        // select the application family
        final ApplicationFamilyDetailPage applicationFamilyDetailPage =
            adminToolPage.getPage(ApplicationFamilyDetailPage.class);
        applicationFamilyDetailPage.getApplicationFamilyDetail();
        applicationFamilyDetailPage.clickEdit();

        // Select the 'allow local authentication' checkbox
        final ApplicationFamilyEditPage applicationFamilyEditPage =
            adminToolPage.getPage(ApplicationFamilyEditPage.class);
        applicationFamilyEditPage.enableLocalAuthentication();
        applicationFamilyEditPage.updateApplicationFamily();

        // create a new user
        userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(),
            "AutoTestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS"));
        user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        userParams.put("userID", user.getId());

        final String newUserId = user.getId();
        final String newUserName = user.getName();

        // add password for the newly created user
        final AddPasswordForUserPage userPasswordPage = adminToolPage.getPage(AddPasswordForUserPage.class);
        userPasswordPage.getCredentialDetail(newUserId, userPassword, userPassword);

        // assign QA role to the user
        final JSONObject rolesOfUser = new JSONObject();
        rolesOfUser.put("type", "roles");
        rolesOfUser.put("id", rolesMap.get(Role.QA_ONLY.getValue()));
        final JSONArray jsonArray = new JSONArray();
        jsonArray.add(rolesOfUser);
        final JSONObject requestRoles = new JSONObject();
        requestRoles.put("data", jsonArray);
        userParams.put("body", requestRoles.toJSONString());
        rolesHelper.assignUserRole(userParams);

        adminToolPage.logout();

        // login with newly created user
        adminToolPage.login(newUserName, userPassword);

        final HomePage homePage = adminToolPage.getPage(HomePage.class);

        AssertCollector.assertThat("Not successfully logged into home page", adminToolPage.getDriver().getCurrentUrl(),
            equalTo(homePage.getHomePageUrl(getEnvironmentVariables())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method tests error of local authentication of the user when setting is set to NO Login will not be
     * successful Step1 : set the local authentication to NO Step2 : create user with password and assign role Step3 :
     * login error with newly created user
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testErrorUserLoginWhenLocalAuthenticationIsNotAllowed() {

        // select the application family
        final ApplicationFamilyDetailPage applicationFamilyDetailPage =
            adminToolPage.getPage(ApplicationFamilyDetailPage.class);
        applicationFamilyDetailPage.getApplicationFamilyDetail();
        applicationFamilyDetailPage.clickEdit();

        // Deselect the 'allow local authentication' checkbox
        final ApplicationFamilyEditPage applicationFamilyEditPage =
            adminToolPage.getPage(ApplicationFamilyEditPage.class);
        applicationFamilyEditPage.disableLocalAuthentication();
        applicationFamilyEditPage.updateApplicationFamily();

        // create a new user
        userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(),
            "AutoTestUser" + DateTimeUtils.getNowAsString("ddyyyy_HHmmssSS"));
        user = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        userParams.put("userID", user.getId());
        String actorId = rolesHelper.getActor();
        if (null == actorId) {
            final String externalKey = "$TestActor_Externalkey_" + RandomStringUtils.randomAlphabetic(8);
            // Adding actor with name, external key and name space id
            final AddActorPage addActorPage = adminToolPage.getPage(AddActorPage.class);
            addActorPage.addActor(externalKey);
            final ActorDetailsPage actorDetailsPage = addActorPage.clickOnSave();
            actorId = actorDetailsPage.getId();
        }
        userParams.put("actorID", actorId);

        final String newUserId = user.getId();
        final String newUserName = user.getName();

        // add password for the user
        final AddPasswordForUserPage userPasswordPage = adminToolPage.getPage(AddPasswordForUserPage.class);
        userPasswordPage.getCredentialDetail(newUserId, userPassword, userPassword);

        // assign role to the user
        final JSONObject rolesOfUser = new JSONObject();
        rolesOfUser.put("type", "roles");
        rolesOfUser.put("id", rolesMap.get(Role.QA_ONLY.toString()));
        final JSONArray jsonArray = new JSONArray();
        jsonArray.add(rolesOfUser);
        final JSONObject requestRoles = new JSONObject();
        requestRoles.put("data", jsonArray);
        userParams.put("body", requestRoles.toJSONString());
        rolesHelper.assignUserRole(userParams);

        adminToolPage.logout();

        // login with the new user
        adminToolPage.login(newUserName, userPassword);

        AssertCollector.assertThat("Incorrect login error message", adminToolPage.getLoginErrorMessage(),
            equalTo("Invalid login"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests LAST LOGIN field of a user
     */
    @Test
    public void testLastLoginTimeOfUser() {

        // Get the user details page
        final FindUserPage findUserPage = adminToolPage.getPage(FindUserPage.class);
        UserDetailsPage userDetailsPage = findUserPage.getByName(getEnvironmentVariables().getUserName());

        AssertCollector.assertThat("Last login time is empty", userDetailsPage.getLastLogin(), notNullValue(),
            assertionErrorList);

        // Log out and login to get current login time
        adminToolPage.logout();
        final String dateTime1 = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_WITH_TIME_ZONE);
        adminToolPage.login();

        userDetailsPage = findUserPage.getByName(getEnvironmentVariables().getUserName());
        final String lastLogin1 = userDetailsPage.getLastLogin();

        final int indexOfLastOccurrence = lastLogin1.lastIndexOf(":");

        AssertCollector.assertThat("Incorrect Last login time for the first login",
            lastLogin1.substring(0, indexOfLastOccurrence),
            greaterThanOrEqualTo(dateTime1.substring(0, indexOfLastOccurrence)), assertionErrorList);

        // Log out and login to get current login time
        adminToolPage.logout();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String dateTime2 = DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_WITH_TIME_ZONE);
        adminToolPage.login();

        userDetailsPage = findUserPage.getByName(getEnvironmentVariables().getUserName());
        final String lastLogin2 = userDetailsPage.getLastLogin();
        AssertCollector.assertThat("Incorrect Last login time for the second login",
            lastLogin2.substring(0, indexOfLastOccurrence),
            greaterThanOrEqualTo(dateTime2.substring(0, indexOfLastOccurrence)), assertionErrorList);
        AssertCollector.assertThat("First and last login time should not be same", lastLogin2, not(lastLogin1),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This method tests last modified date for user. Date of last login date should not be applied to date of
     * lastModified.
     *
     * @throws ParseException
     */
    @Test
    public void testLastLoginDateDoesNotAffectLastModfiedTimeOfUser() throws ParseException {

        // Get the user details page
        final FindUserPage findUserPage = adminToolPage.getPage(FindUserPage.class);
        UserDetailsPage userDetailsPage = findUserPage.getByName(getEnvironmentVariables().getUserName());
        LOGGER.info("Last modified date with first login: " + userDetailsPage.getLastModified());
        final Date lastModifiedDateWithFirstLogin = DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT,
            userDetailsPage.getLastModified().replace(" UTC", ""));
        final Date lastLoginDateWithFirstLogin =
            DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT, userDetailsPage.getLastLogin().replace(" UTC", ""));

        // Log out and login to get current login time
        adminToolPage.logout();
        adminToolPage.login();
        userDetailsPage = findUserPage.getByName(getEnvironmentVariables().getUserName());
        LOGGER.info("Last modified date with second login: " + userDetailsPage.getLastModified());

        final Date lastModifiedDateWithSecondLogin = DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT,
            userDetailsPage.getLastModified().replace(" UTC", ""));
        AssertCollector.assertThat("Last modified date should not be changed with two subsequent logins",
            lastModifiedDateWithSecondLogin, equalTo(lastModifiedDateWithFirstLogin), assertionErrorList);

        final Date lastLoginDateWithSecondLogin =
            DateTimeUtils.getDate(PelicanConstants.DB_DATE_FORMAT, userDetailsPage.getLastLogin().replace(" UTC", ""));
        AssertCollector.assertThat("Second login date should be greater than first login date.",
            lastLoginDateWithSecondLogin, greaterThan(lastLoginDateWithFirstLogin), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }
}
