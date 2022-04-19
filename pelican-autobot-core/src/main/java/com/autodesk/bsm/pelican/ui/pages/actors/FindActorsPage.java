package com.autodesk.bsm.pelican.ui.pages.actors;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page represents FindActorPage
 *
 * @author Shweta Hegde
 */
public class FindActorsPage extends GenericDetails {

    @FindBy(id = "bd")
    protected WebElement messageOnDelete;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindActorsPage.class.getSimpleName());

    public FindActorsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * Find actor by id, navigates to a page, fills in actor id
     *
     * @param id
     */
    public void findById(final String id) {

        navigateToFindByIdActor();
        setId(id);
    }

    /**
     * Method to navigate to the page and enter external key
     *
     * @param externalKey
     */
    public void findByExternalKey(final String externalKey) {
        navigateToFindByIdActor();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
    }

    /**
     * Getting error message when invalid input is given
     *
     * @return error message
     */
    public String getErrorMessage() {

        return getError();
    }

    /**
     * Get Error message for the field
     *
     * @return error
     */
    public String getErrorMessageForField() {
        return super.getErrorMessageForField();
    }

    /**
     * Navigate to FindActor By id (this is feature flagged as part of microservice)
     */
    private void navigateToFindByIdActor() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.ACTOR.getForm() + "/" + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * This method is to click on Submit button and return ActorDetailsPage, Success scenario
     *
     * @return ActorDetailsPage
     */
    public ActorDetailsPage clickOnSubmit(final int index) {

        submit(index);
        return super.getPage(ActorDetailsPage.class);
    }

    /**
     * This method is to click on Submit button and return FindActorsPage, Error scenario
     *
     * @return FindActorsPage
     */
    public FindActorsPage clickOnSubmitForError(final int index) {

        submit(index);
        return super.getPage(FindActorsPage.class);
    }

    /**
     * Method to return Delete Message from page.
     */
    public String getMessageOnDelete() {
        return getActions().getText(messageOnDelete).split("\n")[1];
    }
}
