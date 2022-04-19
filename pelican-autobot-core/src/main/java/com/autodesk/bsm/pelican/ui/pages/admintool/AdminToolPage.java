package com.autodesk.bsm.pelican.ui.pages.admintool;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main page of admin tool to logout and navigate to various tab
 *
 * @author yin
 */
public class AdminToolPage extends GenericDetails {

    @FindBy(id = "input-appFamilyId")
    private WebElement applicationFamilyInput;

    @FindBy(id = "input-username")
    private WebElement userNameInput;

    @FindBy(id = "input-password")
    private WebElement passwordInput;

    @FindBy(className = "errors")
    private WebElement loginErrorMessage;

    @FindBy(className = "first")
    private WebElement signedInText;

    @FindBy(css = ".login-info > dl >  dd:nth-child(2) > span > a")
    private WebElement logoutLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminToolPage.class.getSimpleName());

    // Tabs
    @FindBy(id = "subnav-link-bicRelease-add")
    private WebElement addBicReleaseElement;

    public AdminToolPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        PageFactory.initElements(driver, this); // init the page fields
        for (int i = 0; i < 5; i++) {
            driver.manage().window().maximize();
        }
    }

    public void setApplicationFamily(final String value) {
        getActions().setText(applicationFamilyInput, value);
    }

    public void setUserName(final String value) {
        getActions().setText(userNameInput, value);
    }

    public void setPassword(final String value) {
        getActions().setText(passwordInput, value);
    }

    /**
     * This method returns the error message when logging in with invalid credentials or application family
     *
     * @return String
     */
    public String getLoginErrorMessage() {

        final String errorMessage = loginErrorMessage.getText();
        LOGGER.debug("Getting the login error message as:" + errorMessage);
        return errorMessage;
    }

    /**
     * This method would help if someone want to login to admin tool using the details from environmentVariables
     * properties
     */
    public void login() {
        login(environmentVariables.getApplicationFamily(), environmentVariables.getUserName(),
            environmentVariables.getPassword());
    }

    /**
     * Generic method for ALL logIn in Admin Tool
     */
    public void login(final String appFamily, final String userName, final String secret) {
        // Navigate to login page
        final String url = environmentVariables.getAdminUrl() + "/" + AdminPages.LOGIN.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        setApplicationFamily(appFamily);
        setUserName(userName);
        setPassword(secret);
        LOGGER.debug("AppFamily: " + appFamily + "\tUserName: " + userName + "\tPassword: <This is not Password>");
        submit();
    }

    /**
     * Login to admin tool with other userName which is not environmentVariables properties
     */
    public void login(final String userName, final String secret) {

        login(getEnvironment().getApplicationFamily(), userName, secret);
    }

    // Upper right corner
    public void logout() {
        LOGGER.info("Logout from Admin Tool");
        logoutLink.click();
    }

    // Tabs
    public void selectSubscriptionLink() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTIONS.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    // Subscriptions
    public boolean isAddBicReleaseEnabled() {
        return addBicReleaseElement.getAttribute("href") != null;

    }
}
