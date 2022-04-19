package com.autodesk.bsm.pelican.ui.pages.actors;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditApiSecretPage extends GenericDetails {

    public EditApiSecretPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    @FindBy(id = "input-password")
    private WebElement yourPassword;

    @FindBy(id = "input-secret")
    private WebElement newApiSecret;

    @FindBy(id = "input-confirmSecret")
    private WebElement reConfirmNewApiSecret;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditApiSecretPage.class.getSimpleName());

    /**
     * This method Edits api secrets
     */
    public void editApiCredential(final String userPassword, final String apiSecret, final String reConfirmapiSecret) {

        LOGGER.info("Edit API credential Details");
        setYourPassword(userPassword);
        setNewApiSecret(apiSecret);
        setReEnterNewApiSecret(reConfirmapiSecret);
    }

    /**
     * This method adds the API secret and navigates back to ActorDetailPage
     *
     * @return ActorDetailsPage
     */
    public ActorDetailsPage clickOnChangeApiSecret() {
        submit();
        return super.getPage(ActorDetailsPage.class);
    }

    /**
     * This method returns the same page when error occurs.
     *
     * @return EditApiSecretPage
     */
    public EditApiSecretPage clickOnChangeApiSecretError() {
        submit();
        return super.getPage(EditApiSecretPage.class);
    }

    /**
     * Set User Password
     */
    private void setYourPassword(final String userPassword) {
        getActions().setText(yourPassword, userPassword);
    }

    /**
     * Set New API Secret
     */
    private void setNewApiSecret(final String apiSecret) {
        LOGGER.info("Set your new apiSecret to '" + apiSecret + "'");
        getActions().setText(newApiSecret, apiSecret);
    }

    /**
     * Set Re-enter new apiSecret
     */
    private void setReEnterNewApiSecret(final String reConfirmApiSecret) {
        LOGGER.info("Set Re-enter new apiSecret to '" + reConfirmApiSecret + "'");
        getActions().setText(reConfirmNewApiSecret, reConfirmApiSecret);
    }

}
