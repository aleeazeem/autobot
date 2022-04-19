package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page class for Subscription Renewal Reminder WIP Details, Subscription Renewal WIP Details and Subscription
 * Expiration Reminder WIP Details. This page can be navigated from by clicking on objectId from subscription renewal,
 * subscription renewal reminder and subscription expiration reminder WIP Details work in progress report page.
 *
 * @author jains
 */
public class WorkInProgressSubscriptionDetailsPage extends GenericGrid {

    public WorkInProgressSubscriptionDetailsPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to return OxygenId.
     *
     * @return String
     */
    public String getOxygenId() {
        return getFieldValueByKey("OxygenId");
    }

    /**
     * Method to return Stored Payment Profile Id.
     *
     * @return String
     */
    public String getStoredPaymentProfileId() {
        return getFieldValueByKey("Stored Payment Profile Id");
    }

    /**
     * Method to return Next Billing Date.
     *
     * @return String
     */
    public String getNextBillingDate() {
        return getFieldValueByKey(PelicanConstants.NEXT_BILLING_DATE_FIELD);
    }

    /**
     * Method to return Billing Period.
     *
     * @return String
     */
    public String getBillingPeriod() {
        return getFieldValueByKey("Billing Period");
    }

    /**
     * Method to return PriceList ExternalKey.
     *
     * @return String
     */
    public String getPriceListExternalKey() {
        return getFieldValueByKey("PriceList ExternalKey");
    }

    /**
     * Method to return Subscription(s).
     *
     * @return String
     */
    public String getSubscriptions() {
        return getFieldValueByKey("Subscription(s)");
    }

    /**
     * Method to click on oxygen id.
     *
     * @param oxygenId
     * @return UserDetailsPage
     */
    public UserDetailsPage clickOnOxygenId(final String oxygenId) {
        getDriver().findElement(By.linkText(oxygenId)).click();
        return getPage(UserDetailsPage.class);
    }

    /**
     * Method to click on subscriptionId.
     *
     * @param subscriptionId
     * @return SubscriptionDetailPage
     */
    public SubscriptionDetailPage clickOnSubscriptionId(final String subscriptionId) {
        getDriver().findElement(By.linkText(subscriptionId)).click();
        return getPage(SubscriptionDetailPage.class);
    }

    /**
     * Method to return Expiration Date.
     *
     * @return String
     */
    public String getExpirationDate() {
        return getFieldValueByKey("Expiration Date");
    }

}
