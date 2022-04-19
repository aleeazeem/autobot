package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.entities.Subscription;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * This is the page object for Subscription Page in Pelican admin tool This page can be viewed in pelican Admin in
 * Subscriptions tab / User >Find Subscriptions link Web element objects created may not be complete. If missing any web
 * element which is required for your testing please add them. You can get the page details by using
 * getPage(SubscriptionPage.class) in AdminTool Class
 *
 * @author Vineel
 */
public class FindSubscriptionsPage extends GenericDetails {

    public FindSubscriptionsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-subscriptionId")
    private WebElement subscriptionIdInput;

    @FindBy(id = "input-ownerId")
    private WebElement userIdInput;

    @FindBy(xpath = ".//*[@id='bd']/div[3]/ul/li[2]/a")
    private WebElement uploadSubscriptions;

    @FindBy(id = "input-poId")
    private WebElement poIdInput;

    @FindBy(id = "input-planId")
    private WebElement subscriptionPlanIdInput;

    @FindBy(id = "input-userExternalKey")
    private WebElement userExternalKeyInput;

    @FindBy(id = "input-subscriptionExternalKey")
    private WebElement subExternalKeyInput;

    @FindBy(id = "input-advancedOwnerId")
    private WebElement userNameIdInput;

    @FindBy(id = "input-advancedUserExternalKey")
    private WebElement userExtKeyInput;

    @FindBy(id = "status")
    private WebElement statusSelect;

    @FindBy(id = "input-creationDateAfter")
    private WebElement creationDateFromInput;

    @FindBy(id = "input-creationDateBefore")
    private WebElement creationDateToInput;

    @FindBy(id = "input-nextBillingDateAfter")
    private WebElement nextBillingDateFromInput;

    @FindBy(id = "input-nextBillingDateBefore")
    private WebElement nextBillingDateToInput;

    @FindBy(className = "form-inactive")
    private WebElement advancedSearchTab;

    @FindBy(id = "exportControlStatus")
    private WebElement exportControlStatusSelect;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindSubscriptionsPage.class.getSimpleName());

    public SubscriptionDetailPage findBySubscriptionId(final String value) {
        navigateToPage();
        getActions().setText(subscriptionIdInput, value);
        LOGGER.info("Subscription Id has been set to: " + value);
        submit(TimeConstants.ONE_SEC);
        LOGGER.info("Find Button is clicked");
        return super.getPage(SubscriptionDetailPage.class);
    }

    /*
     * This method navigates to find subscription page and finds a subscription by user name
     */
    public void findByUserId(final String value) {
        navigateToPage();
        LOGGER.info("Set user Id" + value);
        getActions().setText(userIdInput, value);
    }

    /*
     * This method navigates to find subscription page and finds a subscription by purchase order id
     */
    public void findSubscriptionByPoId(final String value) {
        navigateToPage();
        LOGGER.info("Set PO Id " + value);
        getActions().setText(poIdInput, value);
    }

    /*
     * This method navigates to find form and find a subscription by external key of a user
     */
    public void findSubscriptionByUserExternalKey(final String externalKey) {
        // navigates to find subscription page
        navigateToPage();
        LOGGER.info("Set externalkey of a user " + externalKey);
        getActions().setText(userExternalKeyInput, externalKey);
    }

    /**
     * This method navigates to find form and sets external key.
     */
    public void getSubscriptionByExternalKey(final String externalKey) {
        navigateToPage();
        getActions().setText(subExternalKeyInput, externalKey);
    }

    /**
     * Method to click on find subscriptions button.
     *
     * @return SubscriptionDetailPage
     */
    public SubscriptionDetailPage clickOnFindSubscriptionsButtonToDetails() {
        submit(TimeConstants.ONE_SEC);
        return super.getPage(SubscriptionDetailPage.class);
    }

    /**
     * Method to click on find subscriptions button.
     *
     * @return SubscriptionSearchResultPage
     */
    public SubscriptionSearchResultPage clickOnFindSubscriptionsButtonToGrid() {
        submit(TimeConstants.ONE_SEC);
        return super.getPage(SubscriptionSearchResultPage.class);
    }

    public GenericDetails getDetailPage(final String id) {
        LOGGER.info("URL ->Subscription  URL is : " + getEnvironment().getAdminUrl());
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm() + "/show?id=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        return getPage(GenericDetails.class);
    }

    /**
     *
     */
    public void clickOnUploadSubscription() {
        uploadSubscriptions.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    public boolean isUploadSubscriptionsLinkDisplayed() {
        return isElementPresent(uploadSubscriptions);
    }

    /*
     * The method sets the fields of a subscription object
     */
    public Subscription assignAllFieldsToSubscription() {

        final GenericDetails genericDetails = super.getPage(GenericDetails.class);
        final Subscription newSubscription = new Subscription();
        newSubscription.setId(genericDetails.getValueByField("ID"));
        newSubscription.setExternalKey(genericDetails.getValueByField("External Key"));
        newSubscription.setUserName(genericDetails.getValueByField("User"));
        newSubscription.setQuantity(genericDetails.getValueByField("Quantity"));
        newSubscription.setStatus(genericDetails.getValueByField("Status"));
        newSubscription.setAutoRenew(genericDetails.getValueByField("Auto-Renew Enabled"));
        newSubscription.setCreditDays(genericDetails.getValueByField("Days Credited"));
        newSubscription.setNextBillingDate(genericDetails.getValueByField("Next Billing Date"));
        newSubscription.setNextBillingPriceAmount(genericDetails.getValueByField("Next Billing Charge"));
        newSubscription.setNextBillingPriceId(genericDetails.getValueByField("Next Billing Price Id"));
        newSubscription.setExpirationDate(genericDetails.getValueByField("Expiration Date"));

        return newSubscription;
    }

    /**
     * this method returns a list of subscriptions by giving any of the following combinations which are given below
     *
     * @return subscription search result page
     */
    public void getSubscriptionByAdvancedFind(final Offerings subscriptionPlan, final String userNameOrId,
        final String userExternalKey, final Status subStatus, final ECStatus ecStatus, final String creationDateFrom,
        final String creationDateTo, final String nextBillingDateFrom, final String nextBillingDateTo) {
        navigateToAdvancedSearchTab();
        setSubscriptionPlanId(subscriptionPlan);
        setUserNameOrId(userNameOrId);
        setUserExternalKey(userExternalKey);
        selectStatus(subStatus);
        setCreationDateFrom(creationDateFrom);
        setCreationDateTo(creationDateTo);
        setNextBillingDateFrom(nextBillingDateFrom);
        setNextBillingDateTo(nextBillingDateTo);
        selectEcStatus(ecStatus);
    }

    /**
     * Method to click on advanced find button.
     *
     * @return subscription search result page.
     */
    public SubscriptionSearchResultPage clickOnAdvFindSubscriptionsButton() {
        submit(1);
        LOGGER.info("Clicked on find subscription button for advanced find");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return getPage(SubscriptionSearchResultPage.class);
    }

    /*
     * Navigate to the url
     */
    public void navigateToSubscriptionsForm() {

        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTIONS.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
    }

    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    // do an open search in the find subscriptions page.
    public void doOpenSearch() {
        LOGGER.info("Doing an open search.");
        submit(TimeConstants.ONE_SEC);
    }

    public GenericGrid getGrid() {

        return super.getPage(GenericGrid.class);
    }

    // CLick on advanced search page
    private void navigateToAdvancedSearchTab() {
        navigateToPage();
        LOGGER.info("Click the advanced search tab.");
        advancedSearchTab.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    // set subscription plan id on advanced search screen
    private void setSubscriptionPlanId(final Offerings subscriptionPlan) {
        if (subscriptionPlan != null) {
            LOGGER.info("Set user ext key to '" + subscriptionPlan.getOfferings().get(0).getId() + "'");
            getActions().setText(subscriptionPlanIdInput, subscriptionPlan.getOfferings().get(0).getId());
        }
    }

    // set external key on advanced search screen

    private void setUserExternalKey(final String userExternalKey) {
        if (userExternalKey != null) {
            LOGGER.info("Set user ext key to '" + userExternalKey + "'");
            getActions().setText(userExtKeyInput, userExternalKey);
        }
    }

    // set user name or Id on advanced search screen
    private void setUserNameOrId(final String userNameOrId) {
        if (userNameOrId != null) {
            LOGGER.info("Set user name or Id to '" + userNameOrId + "'");
            getActions().setText(userNameIdInput, userNameOrId);
        }
    }

    // set creation date from- on advanced search screen
    private void setCreationDateFrom(final String creationDateFrom) {
        if (creationDateFrom != null) {
            LOGGER.info("Set creation date -From- to '" + creationDateFrom + "'");
            getActions().setText(creationDateFromInput, creationDateFrom);
        }
    }

    // set creation date to- on advanced search screen
    private void setCreationDateTo(final String creationDateTo) {
        if (creationDateTo != null) {
            LOGGER.info("Set creation date -To- to '" + creationDateTo + "'");
            getActions().setText(creationDateToInput, creationDateTo);
        }
    }

    // set Next billing date From- on advanced search screen
    private void setNextBillingDateFrom(final String nextBillingDateFrom) {
        if (nextBillingDateFrom != null) {
            LOGGER.info("Set creation date -From- to '" + nextBillingDateFrom + "'");
            getActions().setText(nextBillingDateFromInput, nextBillingDateFrom);
        }
    }

    // set Next billing date To- on advanced search screen
    private void setNextBillingDateTo(final String nextBillingDateTo) {
        if (nextBillingDateTo != null) {
            LOGGER.info("Set creation date -To- to '" + nextBillingDateTo + "'");
            getActions().setText(nextBillingDateToInput, nextBillingDateTo);
        }
    }

    // select status on advanced search screen
    private void selectStatus(final Status status) {
        if (status != null) {
            LOGGER.info("Select '" + status + "' from Status.");
            getActions().select(statusSelect, status.toString());
        }
    }

    // select ec status on advanced search screen
    private void selectEcStatus(final ECStatus ecStatus) {
        if (ecStatus != null) {
            LOGGER.info("Set EC Status to: " + ecStatus.getDisplayName());
            getActions().select(exportControlStatusSelect, ecStatus.toString());
        }
    }

    public Subscription getDetails() {

        final Subscription subscription = new Subscription();

        // Get detail after creation
        final GenericDetails details = super.getPage(GenericDetails.class);

        subscription.setId(details.getValueByField("ID"));
        subscription.setExternalKey(details.getValueByField("External Key"));
        subscription.setSubscriptionPlan(details.getValueByField("Subscription Plan"));
        subscription.setSubscriptionOffer(details.getValueByField("Subscription Offer"));
        subscription.setUser(details.getValueByField("User"));
        subscription.setQuantity(details.getValueByField("Quantity"));
        subscription.setStatus(details.getValueByField("Status"));
        subscription.setAutoRenew(details.getValueByField("Auto-Renew Enabled"));
        subscription.setDaysCredited(details.getValueByField("Days Credited"));
        subscription.setNextBillingDate(details.getValueByField("Next Billing Date"));
        subscription.setNextBillingCharge(details.getValueByField("Next Billing Charge"));
        subscription.setNextBillingPriceId(details.getValueByField("Next Billing Price Id"));
        subscription.setExpirationDate(details.getValueByField("Expiration Date"));

        return subscription;
    }

    // This method selects a subscription randomly from the results if subscription is found and
    // returns true.
    // Returns false if no subscription is found.
    public boolean selectSubscriptionFromGridIfExists(final FindSubscriptionsPage findSubscription,
        final AdminToolPage adminToolPage) {
        // if generic grid is returned, page title will be Subscription Search Results
        if (findSubscription.getTitle().equals("Subscription Search Results")) {
            final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);
            final int subscriptionsCount = grid.getTotalItems();
            boolean isSubscriptionFound;
            if (subscriptionsCount > 0) {
                LOGGER.info("Subscription found with selected Filter");
                LOGGER.info(findSubscription.getTitle());
                // The subscription will be selected from first Page only.Each Page shows up to 20
                // subscription.
                int subIndex;
                final Random index = new Random();
                if (subscriptionsCount <= 20) {
                    subIndex = index.nextInt(subscriptionsCount);
                } else {
                    subIndex = index.nextInt(19);
                }
                LOGGER.info("Random index:" + subIndex);
                grid.selectResultRow(subIndex);
                Util.waitInSeconds(TimeConstants.ONE_SEC);
                isSubscriptionFound = true;
            } else {
                LOGGER.info("No subscription exists with selected Filter");
                isSubscriptionFound = false;
            }
            return isSubscriptionFound;
        } else if (findSubscription.getTitle().equals("Subscription Detail")) {
            // If only one subscription is found, page title will be subscription detail
            LOGGER.info("Only one subscription found");
            return true;
        } else {
            LOGGER.error("Page is not valid");
            return false;
        }
    }

}
