package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddPasswordForUserPage extends GenericDetails {

    public AddPasswordForUserPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-password")
    private WebElement passwordInput;

    @FindBy(id = "input-ownerId")
    private WebElement ownerIdInput;

    @FindBy(id = "input-secret")
    private WebElement secretInput;

    @FindBy(id = "input-confirmSecret")
    private WebElement confirmSecretInput;

    @FindBy(name = "Delete")
    private WebElement deleteButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(xpath = ".//*[@id='bd']/div[2]/div[4]/div/table/tbody/tr/td")
    private WebElement credentials;

    @FindBy(partialLinkText = "Password created")
    private WebElement passwordFound;

    private static final String OWNER_ID_FORM = "/addSSCredForm?ownerId=";

    private static final Logger LOGGER = LoggerFactory.getLogger(AddPasswordForUserPage.class.getSimpleName());

    /**
     * This method is created to return Credential Detail of a user after assigning a secret credentials.
     *
     * @return Credential Detail
     */
    public GenericDetails getCredentialDetail(final String ownerId, final String password, final String secret) {
        Boolean credentialsFound = false;
        final String userDetailUrl =
            getEnvironment().getAdminUrl() + "/" + AdminPages.USER.getForm() + "/show?id=" + ownerId;
        getDriver().get(userDetailUrl);

        try {
            if (!credentials.getText().equalsIgnoreCase(PelicanConstants.NONE_FOUND)) {
                credentialsFound = true;
            }
        } catch (final NoSuchElementException e) {
            credentialsFound = true;
        }

        if (credentialsFound) {
            LOGGER.info("Credentials text: " + credentials.getText());
            passwordFound.click();
            deleteButton.click();
            try {
                confirmButton.click();
            } catch (final org.openqa.selenium.NoSuchElementException ex) {
                // Leave as it is if no pop up is available
                System.out.println("No Such Element Exception thrown!");
            }
        }

        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.CREDENTIAL.getForm() + OWNER_ID_FORM + ownerId;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        enterPassword(password);
        enterOwnerId(ownerId);
        enterSecret(secret);
        submit(TimeConstants.ONE_SEC);

        return super.getPage(GenericDetails.class);
    }

    /**
     * method to enter password of a User
     */
    public void enterPassword(final String password) {
        LOGGER.info("Set password to '" + password + "'");
        getActions().setText(passwordInput, password);
    }

    /**
     * method to enter ownerId
     */
    private void enterOwnerId(final String ownerId) {
        LOGGER.info("Set owner id to '" + ownerId + "'");
        getActions().setText(ownerIdInput, ownerId);
    }

    /**
     * method to assign secret to a new user
     */
    public void enterSecret(final String secret) {
        LOGGER.info("Set secret to '" + secret + "'");
        getActions().setText(secretInput, secret);
        getActions().setText(confirmSecretInput, secret);
    }

    /**
     * This method returns the Credential Detail Page when clicked on "AddPassword"
     *
     * @return CredentialDetailPage
     */
    public CredentialDetailPage clickOnAddPassword() {
        LOGGER.info("Adding the password to the user");
        submit(TimeConstants.ONE_SEC);
        LOGGER.info("Following errors are there on the page: " + getErrorMessageList().toString());
        return super.getPage(CredentialDetailPage.class);
    }

}
