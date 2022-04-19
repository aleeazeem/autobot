package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.pages.subscription.UserSubscriptionResultsPage;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDetailsPage extends GenericDetails {

    public UserDetailsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(name = "Edit")
    private WebElement edit;

    @FindBy(xpath = ".//*[@id='bd']/h1")
    private WebElement detailSelection;

    @FindBy(xpath = "//*[@id='bd']/div[2]/div[2]/div[3]/div/form/div[1]/input")
    private WebElement tagInput;

    @FindBy(xpath = "//*[@id='bd']/div[2]/div[2]/div[3]/div/form/div[2]/span/button")
    private WebElement addTag;

    @FindBy(xpath = "//*[@id='updateproperties']/table/tbody/tr[1]/td[1]/span/input")
    private WebElement propertyName;

    @FindBy(xpath = "//*[@id='updateproperties']/table/tbody/tr[1]/td[2]/span/input")
    private WebElement propertyValue;

    @FindBy(id = "updatepropertiessubmit")
    private WebElement updateProperties;

    @FindBy(xpath = "//*[@id='bd']/div[2]/div[4]/div/table/tfoot/tr/td/div/div/form/div[2]/span/button")
    private WebElement addPassword;

    @FindBy(linkText = "Add a Role Assignment")
    private WebElement addRoleAssignment;

    @FindBy(linkText = "Show Role Assignments")
    private WebElement showRoleAssignment;

    @FindBy(linkText = "Find Subscriptions")
    private WebElement findSubscriptionLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsPage.class.getSimpleName());

    /**
     * This method returns id of a user
     *
     * @return id
     */
    public String getId() {

        final String id = getValueByField("ID");
        LOGGER.info("User id : " + id);
        return id;
    }

    /**
     * Method to check gdpr delete user field visible or not.
     *
     * @return
     */
    public boolean isGdprDeleteFieldPresent() {
        return super.isFieldPresentOnPage("GDPR Delete");
    }

    /**
     * Method to check gdpr delete last modified field visible or not.
     *
     * @return
     */
    public boolean isGdprDeleteDateVisible() {
        return super.isFieldPresentOnPage("GDPR Deleted Date");
    }

    /**
     * This method returns GDPR Deleted Date
     *
     * @return GDPR Deleted Date
     */
    public String getGDPRDeleteLastModified() {

        final String value = getValueByField("GDPR Deleted Date");
        LOGGER.info("GDPR Deleted Date:{} ", value);
        return value;
    }

    /**
     * This method returns GDPR Deleted
     *
     * @return GDPR Deleted
     */
    public String getGDPRDelete() {

        final String value = getValueByField("GDPR Deleted");
        LOGGER.info("GDPR Deleted : {}", value);
        return value;
    }

    /**
     * This method returns external key of a user
     *
     * @return externalKey
     */
    public String getExternalKey() {

        final String externalKey = getValueByField("External Key");
        LOGGER.info("User external key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns username of a user
     *
     * @return userName
     */
    public String getUserName() {

        final String userName = getValueByField("Username");
        LOGGER.info("User name : " + userName);
        return userName;
    }

    /**
     * This method returns state of a user
     *
     * @return state
     */
    public String getState() {

        final String state = getValueByField("State");
        LOGGER.info("User state : " + state);
        return state;
    }

    /**
     * This method returns created date of a user
     *
     * @return created date
     */
    public String getCreated() {

        final String created = getValueByField("Created");
        LOGGER.info("User created date : " + created);
        return created;
    }

    /**
     * This method returns the username of the admin who created the user
     *
     * @return created by
     */
    public String getCreatedBy() {

        final String created = getValueByField("Created By");
        LOGGER.info("User created by : " + created);
        return created;
    }

    /**
     * This method returns last modified date of a user
     *
     * @return last modified date
     */
    public String getLastModified() {

        final String lastModified = getValueByField("Last Modified");
        LOGGER.info("User last modified date : " + lastModified);
        return lastModified;
    }

    /**
     * This method returns the username of the admin who modified the user most recently
     *
     * @return last modified by
     */
    public String getLastModifiedBy() {

        final String lastModifiedBy = getValueByField("Last Modified By");
        LOGGER.info("User last modified by : " + lastModifiedBy);
        return lastModifiedBy;
    }

    /**
     * This method returns last login date and time of a user
     *
     * @return last login
     */
    public String getLastLogin() {

        final String lastLogin = getValueByField("Last Login");
        LOGGER.info("User last login : " + lastLogin);
        return lastLogin;
    }

    /**
     * @return the presence of Title Page
     */
    public boolean isTitlePagePresent() {
        boolean titlePage = false;
        try {
            titlePage = detailSelection.isDisplayed();
            LOGGER.info("User Exists");
            titlePage = true;
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("User Doesn't Exist");
        }
        return titlePage;
    }

    /**
     * Navigate to User Details Page
     */
    public void navigateToUserDetails(final String id) {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.USER.getForm() + "/show?id=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Click on the edit user link
     *
     * @return EditUserPage
     */
    public EditUserPage editUser() {
        LOGGER.info("Edit User Details");
        edit.click();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(EditUserPage.class);
    }

    /**
     * Click on the add tag button
     *
     * @return UserDetailsPage
     */
    public UserDetailsPage addTag(final String tagName) {
        LOGGER.info("Adding Tag : " + tagName);
        getActions().setText(tagInput, tagName);
        addTag.click();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(UserDetailsPage.class);
    }

    /**
     * Click on the update properties button
     *
     * @return UserDetailsPage
     */
    public UserDetailsPage updateProperties(final String name, final String value) {
        LOGGER.info("Updating properties (name: " + name + ", value: " + value + ")");
        getActions().setText(propertyName, name);
        getActions().setText(propertyValue, value);
        updateProperties.click();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(UserDetailsPage.class);
    }

    /**
     * This is a method which will click on the 'Add Password' button on the user detail page
     *
     * @return AddPasswordForUserPage
     */
    public AddPasswordForUserPage clickOnAddPasswordButton() {
        addPassword.click();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(AddPasswordForUserPage.class);
    }

    /**
     * This is a method which will click on the 'Add a Role Assignment' link on the user detail page
     */
    public AddRoleAssignmentPage clickOnAddRoleAssignmentLink() {
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        addRoleAssignment.click();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return super.getPage(AddRoleAssignmentPage.class);
    }

    /**
     * This is a method which will click on the 'Show Role Assignment' link on the user detail page
     */
    public RoleAssignmentResultPage clickOnShowRoleAssignmentLink() {
        showRoleAssignment.click();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return super.getPage(RoleAssignmentResultPage.class);
    }

    /**
     * This is a method which will click on find subscription link.
     *
     * @return UserSubscriptionResultsPage
     */
    public UserSubscriptionResultsPage clickOnFindSubscriptionsLink() {
        findSubscriptionLink.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return getPage(UserSubscriptionResultsPage.class);
    }
}
