package com.autodesk.bsm.pelican.ui.actors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.actors.ActorDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.actors.AddActorPage;
import com.autodesk.bsm.pelican.ui.pages.actors.FindActorsPage;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class is to find actor by id
 *
 * @author Shweta Hegde
 */
public class FindActorTest extends SeleniumWebdriver {

    private FindActorsPage findActorsPage;
    private String actorExternalKey;
    private String actorId;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        findActorsPage = adminToolPage.getPage(FindActorsPage.class);

        actorExternalKey = "$TestActor_Externalkey_" + RandomStringUtils.randomAlphabetic(8);

        // Adding actor with external key
        final AddActorPage addActorPage = adminToolPage.getPage(AddActorPage.class);
        addActorPage.addActor(actorExternalKey);
        final ActorDetailsPage actorDetailsPage = addActorPage.clickOnSave();

        // Get Actor Id
        actorId = actorDetailsPage.getId();
    }

    /**
     * Delete Actor with Tear Down Method.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        findActorsPage.findByExternalKey(actorExternalKey);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(1);
        actorDetailsPage.clickOnDeleteActorConfirm();
        AssertCollector.assertThat("Actor is not Deleted Successfully", findActorsPage.getMessageOnDelete(),
            equalTo(String.format("Successfully deleted actor %s", actorExternalKey)), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method is to find actor by id in Admin Tool. Find is successful and returns details page
     */
    @Test
    public void testFindActorById() {

        findActorsPage.findById(actorId);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(0);

        AssertCollector.assertThat("Incorrect actor id", actorDetailsPage.getId(), equalTo(actorId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect external key", actorDetailsPage.getExternalKey(),
            equalTo(actorExternalKey), assertionErrorList);
        AssertCollector.assertThat("Created should not be null", actorDetailsPage.getCreated(), notNullValue(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method is to find actor by id in Admin Tool when invalid id is given. Error message is thrown and
     * remains in FindActorPage
     */
    @Test
    public void testErrorFindActorByInvalidId() {

        final String invalidId = "hgfdhgdhng-dhg6657-6";
        findActorsPage.findById(invalidId);
        findActorsPage = findActorsPage.clickOnSubmitForError(0);

        AssertCollector.assertThat("Incorrect error message", findActorsPage.getErrorMessage(),
            equalTo(PelicanErrorConstants.ACTOR_NOT_FOUND + invalidId), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test finds actor by external key and validates id and external key
     */
    @Test
    public void testFindActorByExternalKey() {

        findActorsPage.findByExternalKey(actorExternalKey);
        final ActorDetailsPage actorDetailsPage = findActorsPage.clickOnSubmit(1);

        AssertCollector.assertThat("Incorrect actor id", actorDetailsPage.getId(), equalTo(actorId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect external key", actorDetailsPage.getExternalKey(),
            equalTo(actorExternalKey), assertionErrorList);
        AssertCollector.assertThat("Created should not be null", actorDetailsPage.getCreated(), notNullValue(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test throws an error when no external key is given
     */
    @Test
    public void testErrorFindActorByExternalKeyWithNoId() {

        findActorsPage.findByExternalKey("");
        findActorsPage = findActorsPage.clickOnSubmitForError(1);

        AssertCollector.assertThat("Incorrect error message", findActorsPage.getErrorMessageForField(),
            equalTo(PelicanErrorConstants.REQUIRED_ERROR_MESSAGE), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
