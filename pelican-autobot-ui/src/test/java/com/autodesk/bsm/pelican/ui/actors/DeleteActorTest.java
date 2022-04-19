package com.autodesk.bsm.pelican.ui.actors;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddApiSecretPage;
import com.autodesk.bsm.pelican.ui.pages.actors.FindActorsPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Class to cover Delete Actor Scenario.
 */
public class DeleteActorTest extends SeleniumWebdriver {

    private AddActorPage addActorPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addActorPage = adminToolPage.getPage(AddActorPage.class);
    }

    /**
     * This method test Delete Actor in Admin Tool.
     */
    @Test
    public void testSuccessDeleteActor() {

        // Adding actor with external key
        addActorPage.addActor("$TestActor_Externalkey_" + RandomStringUtils.randomAlphabetic(8));
        ActorDetailsPage actorDetailsPage = addActorPage.clickOnSave();

        final String actorId = actorDetailsPage.getId();
        final String externalKey = actorDetailsPage.getExternalKey();
        final String addApiCredential = "Test1234";

        final AddApiSecretPage addApiSecretPage = actorDetailsPage.clickOnAddApiSecret();
        addApiSecretPage.addApiCredential(getEnvironmentVariables().getPassword(), addApiCredential, addApiCredential);

        actorDetailsPage = addApiSecretPage.clickOnAddApiSecret();

        // Click on Delete and then click on Cancel on confirmation pop up.
        actorDetailsPage = actorDetailsPage.clickOnDeleteCancel();
        AssertCollector.assertThat("Actor should not be deleted", actorDetailsPage.getId(), equalTo(actorId),
            assertionErrorList);

        // Click on Delete and then confirm Delete Action.
        final FindActorsPage findActorsPage = actorDetailsPage.clickOnDeleteActorConfirm();
        final String message = findActorsPage.getMessageOnDelete();

        final String expectedMessage = String.format("Successfully deleted actor %s", externalKey);
        AssertCollector.assertThat("Incorrect Message on Delete Actor", message, equalTo(expectedMessage),
            assertionErrorList);

        final List<String> noOfCredential = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_CRED_FOR_ACTOR, actorId), "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Credential should not be present", noOfCredential.get(0), equalTo("0"),
            assertionErrorList);

        final List<String> noOfActor = DbUtils.selectQuery(String.format(PelicanDbConstants.SELET_ACTOR, actorId),
            "count(*)", getEnvironmentVariables());
        AssertCollector.assertThat("Actor should not be present", noOfActor.get(0), equalTo("0"), assertionErrorList);

        // TODO : Audit Log needs to change, uncomment this when user story is picked up
        // Verify the Delete Actor audit data
        // final boolean isAuditLogFound = ActorAuditLogHelper.helperToValidateDynamoDbForActor(actorId, null,
        // getEnvironmentVariables().getAppFamilyId(), null, null, null, null, null, externalKey, null,
        // Action.DELETE, userId);
        // AssertCollector.assertTrue("Audit log not found", isAuditLogFound);
        AssertCollector.assertAll(assertionErrorList);
    }
}
