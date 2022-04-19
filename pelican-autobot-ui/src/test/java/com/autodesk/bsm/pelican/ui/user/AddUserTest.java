package com.autodesk.bsm.pelican.ui.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.UserStatus;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.AddUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This class test all the scenarios of adding a User
 *
 * @author Muhammad Azeem
 */
public class AddUserTest extends SeleniumWebdriver {

    private static final String FIRST_USER_NAME = "UserName_" + RandomStringUtils.randomAlphanumeric(8);
    private static final String FIRST_USER_EXTERNAL_KEY = "UserExternalKey_" + RandomStringUtils.randomAlphanumeric(8);
    private UserDetailsPage userDetailsPage;
    private AddUserPage addUserPage;
    private static final String ADMIN_TOOL_USER = "1";

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addUserPage = adminToolPage.getPage(AddUserPage.class);
    }

    /**
     * Validate that user can be Added with all fields and customized external key. This also validates that added user
     * is a Admin Tool User
     *
     * @Result: Page Displays the Detail Page User
     */
    @Test
    public void verifyAddUserWithAllFieldsIncludingStatusAndCustomExternalKey() {

        addUserPage.addUser(FIRST_USER_NAME, FIRST_USER_EXTERNAL_KEY, UserStatus.ENABLED);
        userDetailsPage = addUserPage.clickOnSave();

        final String userId = userDetailsPage.getId();

        // Query to verify that added user is a admin tool user
        final List<String> selectResult =
            DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where id = " + userId, "IS_ADMINTOOL_USER",
                getEnvironmentVariables());

        AssertCollector.assertThat("User should be a Admin Tool user", selectResult.get(0), equalTo(ADMIN_TOOL_USER),
            assertionErrorList);
        AssertCollector.assertThat("User is not Added", getDriver().getTitle(), equalTo("Pelican User Detail"),
            assertionErrorList);
        AssertCollector.assertThat("Id is not Generated", userId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Fail to created Custom External Key", userDetailsPage.getExternalKey(),
            equalTo(FIRST_USER_EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate two users cannot be added with same name and same external key.
     *
     * @Result: Page will stay on add user page with the Error messages
     */
    @Test(dependsOnMethods = "verifyAddUserWithAllFieldsIncludingStatusAndCustomExternalKey")
    public void verifyAddUserWithDuplicateNameWithSameExternalKey() {

        addUserPage.addUser(FIRST_USER_NAME, FIRST_USER_EXTERNAL_KEY, UserStatus.ENABLED);
        addUserPage = addUserPage.clickOnSaveForError();

        AssertCollector.assertThat("Wrong Title Page", getDriver().getTitle(), equalTo("Pelican Add User"),
            assertionErrorList);
        AssertCollector.assertThat("Main Error Message is not Generated", addUserPage.getAllErrorMessage(),
            equalTo("Please correct the error listed below:"), assertionErrorList);
        AssertCollector.assertThat("Error for name is not Generated", addUserPage.getRequiredErrorMessage(),
            equalTo("The specified name is already in use"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate two users cannot be added with same external key
     *
     * @Result: Page will stay on add user page with the Error messages
     */
    @Test(dependsOnMethods = "verifyAddUserWithAllFieldsIncludingStatusAndCustomExternalKey")
    public void verifyAddUserWithDuplicateExternalKey() {

        final String name = "UserName_" + RandomStringUtils.randomAlphanumeric(12);

        addUserPage.addUser(name, FIRST_USER_EXTERNAL_KEY, UserStatus.ENABLED);
        addUserPage = addUserPage.clickOnSaveForError();

        AssertCollector.assertThat("Wrong Title Page", getDriver().getTitle(), equalTo("Pelican Add User"),
            assertionErrorList);
        AssertCollector.assertThat("Main Error Message is not Generated", addUserPage.getAllErrorMessage(),
            equalTo("Please correct the error listed below:"), assertionErrorList);
        AssertCollector.assertThat("Error for external key is not Generated", addUserPage.getRequiredErrorMessage(),
            equalTo("The specified external key is already in use"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate two users cannot be added with same name with different external key.
     *
     * @Result: Page will stay on add user page with the Error messages
     */
    @Test(dependsOnMethods = "verifyAddUserWithAllFieldsIncludingStatusAndCustomExternalKey")
    public void verifyAddUserWithDuplicateNameWithDifferentExternalKey() {

        final String externalKey = "UserExternalkey_" + RandomStringUtils.randomAlphanumeric(15);
        addUserPage.addUser(FIRST_USER_NAME, externalKey, UserStatus.ENABLED);
        addUserPage = addUserPage.clickOnSaveForError();

        AssertCollector.assertThat("Wrong Title Page", getDriver().getTitle(), equalTo("Pelican Add User"),
            assertionErrorList);
        AssertCollector.assertThat("Main Error Message is not Generated", addUserPage.getAllErrorMessage(),
            equalTo("Please correct the error listed below:"), assertionErrorList);
        AssertCollector.assertThat("Error for name is not Generated", addUserPage.getRequiredErrorMessage(),
            equalTo("The specified name is already in use"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate name is mandatory while adding a User
     *
     * @Result: Page will stay on add user page with the Error messages
     */
    @Test
    public void verifyAddUserWithRequiredField() {

        addUserPage.addUser(null, null, UserStatus.ENABLED);
        addUserPage = addUserPage.clickOnSaveForError();

        AssertCollector.assertThat("Wrong Title Page", getDriver().getTitle(), equalTo("Pelican Add User"),
            assertionErrorList);
        AssertCollector.assertThat("Main Error Message is not Generated", addUserPage.getAllErrorMessage(),
            equalTo("Please correct the error listed below:"), assertionErrorList);
        AssertCollector.assertThat("Name is not mandatory to add user", addUserPage.getRequiredErrorMessage(),
            equalTo("Required"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate if external key will be generated by system by adding a user
     *
     * @Result: Detail page of user will be displayed with auto generated external key
     */
    @Test
    public void verifyExternalKeyGeneratedByTheSystem() {

        final String name = "UserName_" + RandomStringUtils.randomAlphanumeric(12);

        addUserPage.addUser(name, null, UserStatus.ENABLED);
        userDetailsPage = addUserPage.clickOnSave();

        AssertCollector.assertThat("External Key is not Generated", userDetailsPage.getExternalKey(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate that user can be Added with all fields and selected status
     *
     * @Result: Page Displays the Detail Page User with selected status
     */
    @Test(dataProvider = "status")
    public void verifyAddUserWithStatus(final String nameUser, final UserStatus state) {

        addUserPage.addUser(nameUser, null, state);
        userDetailsPage = addUserPage.clickOnSave();

        AssertCollector.assertThat("User is not Added", getDriver().getTitle(), equalTo("Pelican User Detail"),
            assertionErrorList);
        AssertCollector.assertThat("Id is not Generated", userDetailsPage.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_FIELD, userDetailsPage.getCreated(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid user " + PelicanConstants.CREATED_BY_FIELD, userDetailsPage.getCreatedBy(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat(
            "User's " + PelicanConstants.LAST_MODIFIED_FIELD + " is not same as the " + PelicanConstants.CREATED_FIELD,
            userDetailsPage.getLastModified(), equalTo(userDetailsPage.getCreated()), assertionErrorList);
        AssertCollector.assertThat(
            "User's " + PelicanConstants.LAST_MODIFIED_BY_FIELD + " is not same as the "
                + PelicanConstants.CREATED_BY_FIELD,
            userDetailsPage.getLastModifiedBy(), equalTo(userDetailsPage.getCreatedBy()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "status")
    private Object[][] getTestDataForUserState() {
        return new Object[][] { { "AUTO" + RandomStringUtils.randomAlphanumeric(6), UserStatus.ENABLED },
                { "AUTO" + RandomStringUtils.randomAlphanumeric(7), UserStatus.DISABLED },
                { "AUTO" + RandomStringUtils.randomAlphanumeric(8), UserStatus.DELETED }, };
    }

}
