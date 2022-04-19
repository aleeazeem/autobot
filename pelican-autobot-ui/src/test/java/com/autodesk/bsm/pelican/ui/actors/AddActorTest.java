package com.autodesk.bsm.pelican.ui.actors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.actors.FindActorsPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This test class test features of adding actor in Admin Tool
 *
 * @author Shweta Hegde
 */
public class AddActorTest extends SeleniumWebdriver {

    private static final String NOT_ADMIN_TOOL_USER = "0";
    private AddActorPage addActorPage;
    private FindActorsPage findActorsPage;
    private static final String ONLY_ONE_CREDENTIAL_NOTE =
        "Only one API secret is allowed. " + "You can edit an existing credential if you intend to change it.";
    private String externalKey;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addActorPage = adminToolPage.getPage(AddActorPage.class);
        findActorsPage = adminToolPage.getPage(FindActorsPage.class);
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
     * This method test adding Actor in Admin Tool with feature flag ON. Data is written into MySQL and DynamoDB.
     * Internally calls Actor Microservice. This validates that it will not be saved as Admin tool user in named_party
     * table.
     */
    @Test
    public void testAddActor() {

        externalKey = "$TestActor_Externalkey_" + RandomStringUtils.randomAlphabetic(8);

        // Adding actor with external key
        addActorPage.addActor(externalKey);
        final ActorDetailsPage actorDetailsPage = addActorPage.clickOnSave();

        final String actorId = actorDetailsPage.getId();

        // select a value from named_party column
        final List<String> selectResult =
            DbUtils.selectQuery("select IS_ADMINTOOL_USER from NAMED_PARTY where id = " + actorId, "IS_ADMINTOOL_USER",
                getEnvironmentVariables());
        // Assertion
        AssertCollector.assertThat("User should NOT be a Admin Tool user", selectResult.get(0),
            equalTo(NOT_ADMIN_TOOL_USER), assertionErrorList);
        AssertCollector.assertThat("Actor id should not be null", actorId, notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect actor external key", actorDetailsPage.getExternalKey(),
            equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Created should not be empty", actorDetailsPage.getCreated().split("\\s+")[0],
            equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
        AssertCollector.assertThat("Incorrect 'Created By'", actorDetailsPage.getCreatedBy(),
            equalTo(getEnvironmentVariables().getUserName()), assertionErrorList);
        AssertCollector.assertThat("'Updated By' should same as 'Created By'", actorDetailsPage.getUpdatedBy(),
            equalTo(getEnvironmentVariables().getUserName()), assertionErrorList);
        AssertCollector.assertThat("'Updated' should be same as 'Created'",
            actorDetailsPage.getUpdated().split("\\s+")[0],
            equalTo(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
        AssertCollector.assertThat("Incorrect subtitle", actorDetailsPage.getCredentialsNote(),
            equalTo(ONLY_ONE_CREDENTIAL_NOTE), assertionErrorList);
        AssertCollector.assertThat("Credentials should be empty", actorDetailsPage.getEmptyCredentials(),
            equalTo(PelicanConstants.NONE_FOUND), assertionErrorList);

        // TODO : Audit Log needs to change, uncomment this when user story is picked up
        // Verify the Create Actor audit data
        // final boolean isAuditLogFound = ActorAuditLogHelper.helperToValidateDynamoDbForActor(actorId, null,
        // getEnvironmentVariables().getAppFamilyId(), null, null, null, null, null, externalKey, null,
        // Action.CREATE, userId);
        // AssertCollector.assertTrue("Audit log not found", isAuditLogFound);
        AssertCollector.assertAll(assertionErrorList);
    }
}
