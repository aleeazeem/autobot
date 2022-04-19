package com.autodesk.bsm.pelican.ui.pages.actors;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.ConfirmationPopup;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.pages.users.AddRoleAssignmentPage;
import com.autodesk.bsm.pelican.ui.pages.users.RoleAssignmentResultPage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class represents the Actor Details Page. This method should have only getter methods and click actions such as
 * Edit, Delete clicks
 *
 * @author Shweta Hegde
 */
public class ActorDetailsPage extends GenericDetails {

    @FindBy(partialLinkText = "Add a Role Assignment")
    private WebElement addRoleAssignment;

    @FindBy(partialLinkText = "Show Role Assignments")
    private WebElement showRoleAssignment;

    @FindBy(xpath = ".//button[contains(.,'Add API Secret')]")
    private WebElement addApiSceretButton;

    @FindBy(xpath = ".//button[contains(.,'Add Password')]")
    private WebElement addPasswordButton;

    @FindBy(partialLinkText = "ecret created")
    private WebElement apiCredentialRow;

    @FindBy(className = "subtitle")
    private WebElement subTitleText;

    @FindBy(className = "none-found")
    private WebElement credentialNoneFoundText;

    @FindBy(xpath = ".//*[@id='delete-secret-form']/span/button")
    private WebElement deleteCredentialButton;

    @FindBy(xpath = ".//*[@id='delete-actor-form']/span/button")
    private WebElement deleteActorButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(ActorDetailsPage.class.getSimpleName());

    public ActorDetailsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * This method returns id of a actor
     *
     * @return id
     */
    public String getId() {

        return getValueByField("ID");
    }

    /**
     * This method returns external key of a actor
     *
     * @return externalKey
     */
    public String getExternalKey() {

        return getValueByField("External Key");
    }

    /**
     * This method returns the date and time of the created the actor
     *
     * @return created
     */
    public String getCreated() {

        return getValueByField("Created");
    }

    /**
     * This method returns the user name who created the actor
     *
     * @return Created By
     */
    public String getCreatedBy() {

        return getValueByField("Created By");
    }

    /**
     * This method returns the date and time of the last update
     *
     * @return Updated
     */
    public String getUpdated() {

        return getValueByField("Updated");
    }

    /**
     * This method returns the user name who updated the actor recently
     *
     * @return Updated By
     */
    public String getUpdatedBy() {

        return getValueByField("Updated By");
    }

    /**
     * This is a method which will click on the 'Add a Role Assignment' link on the user detail page.
     *
     * @return AddRoleAssignmentPage
     */
    public AddRoleAssignmentPage clickOnAddRoleAssignmentLink() {
        getActions().click(addRoleAssignment);
        return super.getPage(AddRoleAssignmentPage.class);
    }

    /**
     * This is a method which will click on the 'Show Role Assignment' link on the user detail page.
     *
     * @return RoleAssignmentResultPage
     */
    public RoleAssignmentResultPage clickOnShowRoleAssignmentLink() {
        getActions().click(showRoleAssignment);
        return super.getPage(RoleAssignmentResultPage.class);
    }

    /**
     * This is a method to click on Delete button on the Actor details Page
     *
     * @return FindActorsPage
     */
    public FindActorsPage clickOnDeleteActorConfirm() {
        clickOnDeleteActor();
        clickPopUpButtonOnDelete("confirm");
        return super.getPage(FindActorsPage.class);
    }

    /**
     * Method to click on Cancel Action on Delete confirmation pop up
     *
     * @return
     */
    public ActorDetailsPage clickOnDeleteCancel() {
        clickOnDeleteActor();
        clickPopUpButtonOnDelete("cancel");
        return super.getPage(ActorDetailsPage.class);
    }

    /**
     * This is a method to click on Add Api Secret on the Actor details Page
     *
     * @return Add Api Secret Page
     */
    public AddApiSecretPage clickOnAddApiSecret() {

        getActions().click(addApiSceretButton);
        return super.getPage(AddApiSecretPage.class);
    }

    /**
     * This is a method to click on Edit Api Secret on the Actor details Page
     *
     * @return EditApiSecretPage
     */
    public EditApiSecretPage navigateToEditAPICredentialPage() {
        getActions().click(apiCredentialRow);
        return super.getPage(EditApiSecretPage.class);
    }

    /**
     * This method returns the credential related subtext
     *
     * @return note
     */
    public String getCredentialsNote() {

        return getActions().getText(subTitleText);
    }

    /**
     * This method returns None Found text when credentials are empty
     *
     * @return String
     */
    public String getEmptyCredentials() {

        return getActions().getText(credentialNoneFoundText);
    }

    /**
     * This method returns boolean value whether "Add Password" button is displayed or not
     *
     * @return isButtonDisplayed
     */
    public boolean isAddPasswordButtonDisplayed() {

        boolean isButtonDisplayed = false;
        try {
            if (addPasswordButton.isDisplayed()) {
                isButtonDisplayed = true;
            }
        } catch (final Exception e) {
            LOGGER.warn("Add Password button is not displayed");
        }
        return isButtonDisplayed;
    }

    /**
     * This method returns boolean value whether "Add API Secret" button is displayed or not
     *
     * @return isButtonDisplayed
     */
    public boolean isAddApiSecretButtonDisplayed() {

        boolean isButtonDisplayed = false;
        try {
            if (addApiSceretButton.isDisplayed()) {
                isButtonDisplayed = true;
            }
        } catch (final Exception e) {
            LOGGER.warn("Add API Secret button is not displayed");
        }
        return isButtonDisplayed;
    }

    /**
     * This method clicks on "Delete" button of Credentials
     */
    public void clickOnDeleteCredentialButton() {

        getActions().click(deleteCredentialButton);
    }

    /**
     * This method clicks on "Delete" Actor button
     */
    public void clickOnDeleteActor() {
        getActions().click(deleteActorButton);
    }

    /**
     * This method clicks on Confirm or Cancel button depending on the input.
     *
     * @param command
     */
    public void clickPopUpButtonOnDelete(final String command) {

        final ConfirmationPopup popup = getPage(ConfirmationPopup.class);
        if (command.equalsIgnoreCase("Confirm")) {
            popup.confirm();
        } else {
            popup.cancel();
        }
    }
}
