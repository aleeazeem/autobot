package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.entities.SubscriptionActivity;
import com.autodesk.bsm.pelican.ui.generic.ConfirmationPopup;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the page object for Subscription Detail Page in Pelican admin tool. This page can be viewed in AT from
 * Subscriptions tab / User >Find Subscriptions-->subscription search results --> subscription detail
 *
 * @author jains
 */
public class SubscriptionDetailPage extends GenericDetails {

    public SubscriptionDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(name = "Enable Auto-Renew")
    private WebElement enableAutoRenewButton;

    @FindBy(id = "changeECStatusLink")
    private WebElement changeExportControlStatusLink;

    @FindBy(id = "subscriptionMigrationLink")
    private WebElement subscriptionMigrationLink;

    @FindBy(name = "Re-trigger Cloud Credit Fulfillment")
    private List<WebElement> reTriggerCloudCreditFulfillmentButton;

    @FindBy(xpath = "//*[@id='subscription-events']/div/div/table/tbody/tr")
    private List<WebElement> subscriptionActivityRows;

    @FindBy(id = "reduceSeatsFormLink")
    private WebElement reduceSeatsLink;

    @FindBy(id = "editPendingPaymentFlagLink")
    private WebElement editPendingPaymentFlagLink;

    @FindBy(id = "subscriptionsForUserLink")
    private WebElement subscriptionsForUserLink;

    @FindBy(linkText = "Edit Subscription")
    private WebElement editSubscriptionLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionDetailPage.class.getSimpleName());

    /**
     * This method picks Id of the Subscription and returns
     *
     * @return Subscription Id
     */
    public String getId() {

        return getFieldValueByKey("ID");
    }

    /**
     * Method to read Added Quantity Subscription Id.
     *
     * @return Subscription Id.
     */
    public String getAddedQuantitySubscriptionId() {

        return getFieldValueByKey("Added To Subscription ID");
    }

    public String getDaysCredited() {

        return getFieldValueByKey("Days Credited");
    }

    public String getExternalKey() {

        return getFieldValueByKey("External Key");
    }

    public String getStatus() {

        return getFieldValueByKey("Status");
    }

    public String getAutoRenewEnabled() {

        return getFieldValueByKey("Auto-Renew Enabled");
    }

    /**
     * This is the method which will get the value of the next billing price id field on the subscription detail page
     *
     * @return String - value of the next billing price id field.
     */
    public String getNextBillingPriceId() {

        return getFieldValueByKey("Next Billing Price Id");
    }

    /**
     * Get Pending Payment flag value
     *
     * @return String - true or false
     */
    public String getPendingPaymentFlag() {

        return getFieldValueByKey("Pending Payment");
    }

    /**
     * method to click on change export control status link under related actions.
     */
    public ChangeExportControlStatusPage clickOnChangeExportControlStatusLink() {

        getActions().click(changeExportControlStatusLink);
        LOGGER.info("Clicked on Change Export Control Status Link");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return super.getPage(ChangeExportControlStatusPage.class);
    }

    /**
     * method to click on Subscription Migration link under related actions
     *
     * @return subscription migration page.
     */
    public SubscriptionMigrationPage clickOnMigrateSubscriptionLink() {

        getActions().click(subscriptionMigrationLink);
        LOGGER.info("Clicked on Subscription Migration Link");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return super.getPage(SubscriptionMigrationPage.class);
    }

    /**
     * method to click on Edit Pending PaymentFlaglink under related actions
     *
     * @return Edit Pending Payment Flag page.
     */
    public EditPendingPaymentFlagPage clickOnEditPendingPaymentFlagLink() {

        getActions().click(editPendingPaymentFlagLink);
        LOGGER.info("Clicked on Edit Pending Payment Flag Link");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return getPage(EditPendingPaymentFlagPage.class);
    }

    /**
     * method returns boolean value based on editPendingPaymentFlagLink presense
     *
     * @return boolean
     */
    public Boolean isEditPendingPaymentFlagPresent() {

        final Boolean found = isElementPresent(editPendingPaymentFlagLink);
        LOGGER.info("Is \"Edit Pending Payment Flag displayed\"(true/false): " + found);
        return found;
    }

    /**
     * method to click on reduce seats link under related actions
     *
     * @return reduce seats page.
     */
    public ReduceSeatsPage clickOnReduceSeatsLink() {

        getActions().click(reduceSeatsLink);
        LOGGER.info("Clicked on reduce seats Link");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return getPage(ReduceSeatsPage.class);
    }

    /**
     * method to check the presence of reduce seats link.
     *
     * @return boolean - presence of reduce seats link
     */

    public boolean isReduceSeatsLinkDisplayed() {

        boolean isReduceSeatsLinkDisplayed = false;
        try {
            if (reduceSeatsLink.isDisplayed()) {
                isReduceSeatsLinkDisplayed = true;
                LOGGER.info("Reduce Seats link Exists");
            }
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Reduce Seats link doesn't Exist");
        }
        return isReduceSeatsLinkDisplayed;
    }

    /**
     * This method is to click on "Edit" button and then navigate to editSubscriptionPage
     *
     * @return EditSubscriptionPage
     */
    public EditSubscriptionPage clickOnEditSubscriptionLink() {
        editSubscriptionLink.click();
        return super.getPage(EditSubscriptionPage.class);
    }

    /**
     * This method is to click on auto renew button. This will make canceled subscription to be active
     */
    public void enableAutoRenew() {

        enableAutoRenewButton.click();
        final ConfirmationPopup popup = getPage(ConfirmationPopup.class);
        popup.confirm();
    }

    /**
     * Check if change export control status is displayed or not.
     *
     * @return Boolean - true if found, else false not found
     */
    public Boolean checkExportControlStatusLink() {

        Boolean changeExportControlStatus = true;
        try {
            changeExportControlStatusLink.isDisplayed();
        } catch (final NoSuchElementException e) {
            changeExportControlStatus = false;
        }
        return changeExportControlStatus;
    }

    /**
     * Check if migrate subscription is displayed or not.
     *
     * @return Boolean - true if found, else false not found
     */
    public Boolean isMigrateSubscriptionLinkExists() {

        Boolean isMigrateSubscriptionLinkExists = false;
        try {
            subscriptionMigrationLink.isDisplayed();
            LOGGER.info("Link of Migrate Subscription exists");
            isMigrateSubscriptionLinkExists = true;
        } catch (final NoSuchElementException e) {
            LOGGER.info("Link of Migrate Subscription doesn't exist");
        }
        return isMigrateSubscriptionLinkExists;
    }

    /**
     * This method returns value of Subscription Export Control Status.
     *
     * @return String
     */
    public String getExportControlStatus() {

        return getFieldValueByKey("Export Control Status");
    }

    /**
     * This method returns value of Subscription Export Control Status last modified date with timestamp.
     *
     * @return String
     */
    public String getExportControlStatusLastModified() {

        return getFieldValueByKey("Export Control Last Modified");
    }

    /**
     * This method returns value of Subscription id.
     *
     * @return String
     */
    public String getSubscriptionId() {

        return getFieldValueByKey("ID");
    }

    /**
     * This method returns value of Subscription plan.
     *
     * @return String
     */
    public String getSubscriptionPlan() {

        return getFieldValueByKey("Subscription Plan");
    }

    /**
     * This method returns value of Subscription offer.
     *
     * @return String
     */
    public String getSubscriptionOffer() {

        return getFieldValueByKey("Subscription Offer");
    }

    /**
     * This method returns value of user.
     *
     * @return String
     */
    public String getUser() {

        return getFieldValueByKey("User");
    }

    /**
     * This method returns value of Expiration Date(Removes time stamp).
     *
     * @return String
     */
    public String getExpirationDate() {

        return getFieldValueByKey(PelicanConstants.EXPIRATION_DATE_FIELD).split(" ")[0];
    }

    /**
     * This method returns value of Expiration Date(includes time stamp and UTC).
     *
     * @return String
     */
    public String getCompleteExpirationDate() {
        return getFieldValueByKey(PelicanConstants.EXPIRATION_DATE_FIELD);
    }

    /**
     * This method returns value of Next Billing Date(Removes time stamp).
     *
     * @return String
     */
    public String getNextBillingDate() {

        return getFieldValueByKey(PelicanConstants.NEXT_BILLING_DATE_FIELD).split(" ")[0];
    }

    /**
     * This method returns value of Next Billing Date(Includes time stamp and UTC).
     *
     * @return String
     */
    public String getCompleteNextBillingDate() {

        return getFieldValueByKey(PelicanConstants.NEXT_BILLING_DATE_FIELD);
    }

    /**
     * This method returns value of Pending Payment Status.
     *
     * @return String
     */
    public String getPendingPaymentStatus() {

        return getFieldValueByKey("Pending Payment");
    }

    /**
     * This method returns value of Next Billing Charge.
     *
     * @return String
     */
    public String getNextBillingCharge() {

        return getFieldValueByKey("Next Billing Charge");
    }

    /**
     * Method to return subscription activity from subscription detail page.
     *
     * @return List<SubscriptionActivity>
     */
    public List<SubscriptionActivity> getSubscriptionActivity() {
        return getSubscriptionActivity(0);
    }

    /**
     * Method to return last subscription activity from subscription detail page.
     *
     * @return SubscriptionActivity
     */
    public SubscriptionActivity getLastSubscriptionActivity() {
        return getSubscriptionActivity(subscriptionActivityRows.size() - 1).get(0);
    }

    /**
     * Method to return number of subscription activity.
     *
     * @return int
     */
    public int getNumberOfSubscriptionActivity() {
        return subscriptionActivityRows.size();
    }

    public List<SubscriptionActivity> getSubscriptionActivity(final int startIndex) {
        LOGGER.info("Getting subscription activity");
        final List<SubscriptionActivity> subscriptionActivityList = new ArrayList<>();
        final int subscriptionActivityRowsCount = subscriptionActivityRows.size();
        LOGGER.info("Total number of subscription activity: " + subscriptionActivityRowsCount);
        WebElement subscriptionActivityRow;
        // Iterating through subscription activity to get all the fields
        for (int i = startIndex; i < subscriptionActivityRowsCount; i++) {
            final SubscriptionActivity subscriptionActivity = new SubscriptionActivity();
            subscriptionActivityRow = subscriptionActivityRows.get(i);
            final List<WebElement> cells = subscriptionActivityRow.findElements(By.tagName("td"));

            // Subscription activity has 7 columns
            if (cells.size() == 7) {
                subscriptionActivity.setDate(cells.get(0).getText());
                subscriptionActivity.setActivity(cells.get(1).getText());
                subscriptionActivity.setRequestor(cells.get(2).getText());
                subscriptionActivity.setCharge(cells.get(3).getText());
                subscriptionActivity.setGrant(cells.get(4).getText());
                subscriptionActivity.setPurchaseOrder(cells.get(5).getText());
                subscriptionActivity.setMemo(cells.get(6).getText());
                subscriptionActivityList.add(subscriptionActivity);
            } else {
                LOGGER.error("Subscription activity row does not have correct number of fields so could not "
                    + "parse subscription activity details.");
            }
        }
        return subscriptionActivityList;
    }

    /**
     * Check if Re-trigger Cloud Credit Fulfillment button is displayed or not
     *
     * @return Boolean - true if found, else false not found
     */
    public Boolean isReTriggerCloudCreditFulfillmentButtonPresent() {

        return reTriggerCloudCreditFulfillmentButton.size() > 0;
    }

    /**
     * Method to click the Re-trigger Cloud Credit Fulfillment button
     */
    public void clickReTriggerCloudCreditFulfillmentButton() {

        getActions().click(reTriggerCloudCreditFulfillmentButton.get(0));
        LOGGER.info("Click Re-trigger Cloud Credit Fulfillment button");
    }

    /**
     * This method returns value of Store.
     *
     * @return String.
     */
    public String getStore() {

        return getFieldValueByKey(PelicanConstants.STORE_FIELD);
    }

    /**
     * This method returns value of Payment Profile.
     *
     * @return String.
     */
    public String getPaymentProfile() {

        return getFieldValueByKey(PelicanConstants.PAYMENT_PROFILE_FIELD);
    }

    /**
     * This method returns value of Added To Subscription ID.
     *
     * @return String
     */
    public String getAddedToSubscriptionId() {

        return getFieldValueByKey("Added To Subscription ID");
    }

    /**
     * This method returns value of Quantity To Reduce.
     *
     * @return int
     */
    public int getQuantityToReduce() {

        return Integer.parseInt(getFieldValueByKey("Quantity To Reduce"));
    }

    /**
     * This method returns value of Quantity.
     *
     * @return int.
     */
    public int getQuantity() {

        return Integer.parseInt(getFieldValueByKey("Quantity"));
    }

    /**
     * method to click on subscription for this user link under related actions
     *
     * @return user subscription result page.
     */
    public UserSubscriptionResultsPage clickOnFindSubscriptionsForUserLink() {

        getActions().click(subscriptionsForUserLink);
        LOGGER.info("Clicked on subscription for this user Link");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return getPage(UserSubscriptionResultsPage.class);
    }

    /**
     * method to find link of subscription for this user under related actions
     *
     * @return boolean: is link present
     */
    public boolean isSubscriptionsForUserLinkVisible() {

        boolean isSubscriptionsForUserLinkVisible = false;
        try {
            isSubscriptionsForUserLinkVisible = subscriptionsForUserLink.isDisplayed();
            LOGGER.info("Link of find subscriptions for this user link exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Link of find subscriptions for this user link doesn't exist");
        }
        return isSubscriptionsForUserLinkVisible;
    }

    /**
     * Method to change status and Pending payment flag for Subscription in DataBase
     *
     * @param status
     * @param pendingPaymentFlag
     */
    public void updateSubscriptionStatusAndPendingPaymentFlag(final String subscriptionStatus,
        final String pendingPaymentFlag, final String subscriptionID) {

        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_SUBSCRIPTION_WITH_STATUS_PENDING_PAYMENT_FIELDS,
            subscriptionStatus, pendingPaymentFlag, subscriptionID, subscriptionID), environmentVariables);

        LOGGER.info("For Subscription ID: " + subscriptionID + ", Status updated to: " + subscriptionStatus
            + " and Pending_Payment flag to " + pendingPaymentFlag);
        refreshPage();
    }

    /**
     * Method to check if Edit Subscription link is present or not.
     *
     * @return boolean
     */
    public boolean isEditSubscriptionLinkDisplayed() {
        boolean isEditSubscriptionLinkDisplayed = false;
        try {
            isEditSubscriptionLinkDisplayed = editSubscriptionLink.isDisplayed();
            LOGGER.info("Edit Subscription link Exists");

        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Edit Subscription link doesn't Exist");
        }
        return isEditSubscriptionLinkDisplayed;

    }

    /**
     * Method to click on cancel button.
     *
     * @return
     */
    public CancelSubscriptionPage clickOnCancelSubscription() {
        LOGGER.info("Click on Cancel Button");
        cancel();
        return getPage(CancelSubscriptionPage.class);
    }

    /**
     * The method restarts a cancelled subscription
     */
    public void restartASubscription() {
        enableAutoRenewButton.click();
        final String parentWindowHandle = getDriver().getWindowHandle();
        final int numberOfHandles = getDriver().getWindowHandles().size();
        if (numberOfHandles == 1) {
            final String handle = getDriver().getWindowHandles().toArray()[0].toString();
            getDriver().switchTo().window(handle);
            confirmButton.click();
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            getDriver().switchTo().window(parentWindowHandle);
        } else if (numberOfHandles == 2) {
            final String handle = getDriver().getWindowHandles().toArray()[1].toString();
            getDriver().switchTo().window(handle);
            confirmButton.click();
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            getDriver().switchTo().window(parentWindowHandle);

        }
    }
}
