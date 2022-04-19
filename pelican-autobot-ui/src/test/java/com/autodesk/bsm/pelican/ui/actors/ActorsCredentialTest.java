package com.autodesk.bsm.pelican.ui.actors;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddApiSecretPage;
import com.autodesk.bsm.pelican.ui.pages.actors.EditApiSecretPage;
import com.autodesk.bsm.pelican.ui.pages.actors.FindActorsPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Class to Edit actor credential.
 *
 * @author Vaibhavi Joshi
 */
public class ActorsCredentialTest extends SeleniumWebdriver {

    private ActorDetailsPage actorDetailsPage;
    private static final String externalKey = "$TestActor_Externalkey_" + RandomStringUtils.randomAlphabetic(8);
    private static final String addApiCredential = "Test1234";
    private static final String editApiCredential = "Test5678";
    private FindActorsPage findActorsPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        findActorsPage = adminToolPage.getPage(FindActorsPage.class);

        // creating actor with external key
        final AddActorPage addActorPage = adminToolPage.getPage(AddActorPage.class);
        addActorPage.addActor(externalKey);
        actorDetailsPage = addActorPage.clickOnSave();
    }

    /**
     * Delete Actor with Tear Down Method.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {

        findActorsPage.findByExternalKey(externalKey);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(1);
        actorDetailsPage.clickOnDeleteActorConfirm();
        AssertCollector.assertThat("Actor is not Deleted Successfully", findActorsPage.getMessageOnDelete(),
            equalTo(String.format("Successfully deleted actor %s", externalKey)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method test Add API Secret, which call Update Actor API of Actor Service.
     */
    @Test
    public void testAddEditAndDeleteAPISecretForActor() {

        final AddApiSecretPage addApiSecretPage = actorDetailsPage.clickOnAddApiSecret();
        addApiSecretPage.addApiCredential(getEnvironmentVariables().getPassword(), addApiCredential, addApiCredential);

        actorDetailsPage = addApiSecretPage.clickOnAddApiSecret();

        AssertCollector.assertFalse("There should NOT be 'Add Password' button",
            actorDetailsPage.isAddPasswordButtonDisplayed(), assertionErrorList);
        AssertCollector.assertFalse("There should NOT be 'Add API Secret' button",
            actorDetailsPage.isAddApiSecretButtonDisplayed(), assertionErrorList);

        // NOTE : Audit Logs will be uncommented later when user story is planned
        // // Verify Audit Log Data
        // isAuditLogFound =
        // ApiSecretCredentialAuditLogHelper.helperToValidateDynamoDbForApiSecretCredential(credentialId,
        // null, actorId, null, Action.CREATE, getEnvironmentVariables().getUserId());
        // AssertCollector.assertThat("Audit log not found", isAuditLogFound, equalTo(true));

        final EditApiSecretPage editApiSecretPage = actorDetailsPage.navigateToEditAPICredentialPage();
        editApiSecretPage.editApiCredential(getEnvironmentVariables().getPassword(), editApiCredential,
            editApiCredential);

        actorDetailsPage = editApiSecretPage.clickOnChangeApiSecret();

        AssertCollector.assertFalse("There should NOT be 'Add Password' button",
            actorDetailsPage.isAddPasswordButtonDisplayed(), assertionErrorList);
        AssertCollector.assertFalse("There should NOT be 'Add API Secret' button",
            actorDetailsPage.isAddApiSecretButtonDisplayed(), assertionErrorList);

        // NOTE : Audit Logs will be uncommented later when user story is planned
        // // Verify Audit Log Data
        // isAuditLogFound =
        // ApiSecretCredentialAuditLogHelper.helperToValidateDynamoDbForApiSecretCredential(credentialId,
        // null, null, null, Action.UPDATE, getEnvironmentVariables().getUserId());
        // AssertCollector.assertThat("Audit log not found", isAuditLogFound, equalTo(true));

        actorDetailsPage.clickOnDeleteCredentialButton();
        actorDetailsPage.clickPopUpButtonOnDelete("Confirm");

        AssertCollector.assertFalse("There should NOT be 'Add Password' button",
            actorDetailsPage.isAddPasswordButtonDisplayed(), assertionErrorList);
        AssertCollector.assertTrue("There should be 'Add API Secret' button",
            actorDetailsPage.isAddApiSecretButtonDisplayed(), assertionErrorList);
        AssertCollector.assertThat("Credentials should be empty", actorDetailsPage.getEmptyCredentials(),
            equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);
        // NOTE : Audit Logs will be uncommented later when user story is planned
        // // Verify Audit Log Data
        // isAuditLogFound =
        // ApiSecretCredentialAuditLogHelper.helperToValidateDynamoDbForApiSecretCredential(credentialId,
        // actorId, null, null, Action.DELETE, getEnvironmentVariables().getUserId());
        // AssertCollector.assertThat("Audit log not found", isAuditLogFound, equalTo(true));
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests 3 error scenarios while Adding API Secret. 1. When api secret is not matching the password
     * standard. 2. When api confirm secret is not matching the secret entered. 3. When create user's password is
     * incorrect.
     */
    @Test
    public void testErrorAddAPISecretForActor() {

        AddApiSecretPage addApiSecretPage = actorDetailsPage.clickOnAddApiSecret();
        addApiSecretPage.addApiCredential(getEnvironmentVariables().getPassword(), "djhgfkgkjh", "hdgfh");

        addApiSecretPage = addApiSecretPage.clickOnAddApiSecretError();

        AssertCollector.assertThat("Incorrect error message", addApiSecretPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for api secret", addApiSecretPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.PASSWORD_NOT_MET_CRITERIA), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for api secret",
            addApiSecretPage.getErrorMessageList().get(1), equalTo(PelicanErrorConstants.PLEASE_TRY_AGAIN),
            assertionErrorList);

        addApiSecretPage.addApiCredential("agfhsajh", "P@ssw0rd", "P@ssw0rd");

        addApiSecretPage = addApiSecretPage.clickOnAddApiSecretError();
        AssertCollector.assertThat("Incorrect error message", addApiSecretPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for user's password", addApiSecretPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.INVALID_PASSWORD), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests 3 error scenarios while Editing API Secret. 1. When api secret is not matching the password
     * standard. 2. When api confirm secret is not matching the secret entered. 3. When create user's password is
     * incorrect.
     */
    @Test
    public void testErrorEditAPISecretForActor() {

        final AddApiSecretPage addApiSecretPage = actorDetailsPage.clickOnAddApiSecret();
        addApiSecretPage.addApiCredential(getEnvironmentVariables().getPassword(), addApiCredential, addApiCredential);

        actorDetailsPage = addApiSecretPage.clickOnAddApiSecret();

        EditApiSecretPage editApiSecretPage = actorDetailsPage.navigateToEditAPICredentialPage();
        editApiSecretPage.editApiCredential(getEnvironmentVariables().getPassword(), "fgdhgfdl", "dghfshgfhdgfsghfhg");

        editApiSecretPage = editApiSecretPage.clickOnChangeApiSecretError();

        AssertCollector.assertThat("Incorrect error message", editApiSecretPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for api secret", editApiSecretPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.PASSWORD_NOT_MET_CRITERIA), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for api secret",
            editApiSecretPage.getErrorMessageList().get(1), equalTo(PelicanErrorConstants.PLEASE_TRY_AGAIN),
            assertionErrorList);

        editApiSecretPage.editApiCredential("agfhsajh", "P@ssw0rd", "P@ssw0rd");

        editApiSecretPage = editApiSecretPage.clickOnChangeApiSecretError();
        AssertCollector.assertThat("Incorrect error message", editApiSecretPage.getError(),
            equalTo(PelicanErrorConstants.DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Incorrect error message for user's password", editApiSecretPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.INVALID_PASSWORD), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
