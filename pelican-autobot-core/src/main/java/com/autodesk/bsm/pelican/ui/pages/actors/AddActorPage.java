package com.autodesk.bsm.pelican.ui.pages.actors;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents Add Actor page
 *
 * @author Shweta Hegde
 */
public class AddActorPage extends GenericDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddActorPage.class.getSimpleName());

    public AddActorPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * Click on the save button of the actor which is inherited from Generic Page This method is written so that any
     * test class can use this
     *
     * @return ActorDetailsPage
     */
    public ActorDetailsPage clickOnSave() {

        submit();
        return super.getPage(ActorDetailsPage.class);
    }

    /**
     * This method adds new Actor in Dynamo DB (calls Actor microservice)
     */
    public void addActor(final String externalKey) {

        navigateToAddActor();
        LOGGER.info("Add Actor Details");
        setExternalKey(externalKey);
    }

    /**
     * This method helps to navigate Add Actor Page
     */
    private void navigateToAddActor() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.ACTOR.getForm() + "/" + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);
    }

}
