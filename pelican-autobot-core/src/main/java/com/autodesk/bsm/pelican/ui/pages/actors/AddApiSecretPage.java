package com.autodesk.bsm.pelican.ui.pages.actors;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class represents the Add APISecret Page
 *
 * @author Vaibhavi Joshi
 */

public class AddApiSecretPage extends GenericDetails {

    public AddApiSecretPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    @FindBy(id = "input-password")
    private WebElement yourPassword;

    @FindBy(id = "input-secret")
    private WebElement newApiSecret;

    @FindBy(id = "input-confirmSecret")
    private WebElement reConfirmNewApiSecret;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddApiSecretPage.class.getSimpleName());

    /**
     * This method adds api secrets
     */
    public void addApiCredential(final String userPassword, final String apiSecret, final String reConfirmapiSecret) {

        LOGGER.info("Add API credential Details");
        setYourPassword(userPassword);
        setNewApiSecret(apiSecret);
        setReEnterNewApiSecret(reConfirmapiSecret);
    }

    /**
     * This method adds the API secret and navigates back to ActorDetailPage NOTE: This method to be renamed to
     * "clickOnAddApiSecret" when feature flag is removed
     *
     * @return ActorDetailsPage
     */
    public ActorDetailsPage clickOnAddApiSecret() {
        submit();
        return super.getPage(ActorDetailsPage.class);
    }

    /**
     * This method returns the same page for error scenarios.
     *
     * @return AddApiSecretPage
     */
    public AddApiSecretPage clickOnAddApiSecretError() {
        submit();
        return super.getPage(AddApiSecretPage.class);
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
    private void setReEnterNewApiSecret(final String reConfirmapiSecret) {
        LOGGER.info("Set Re-enter new apiSecret to '" + reConfirmapiSecret + "'");
        getActions().setText(reConfirmNewApiSecret, reConfirmapiSecret);
    }

}
