package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is a page object for Find Subscription Plan Page This class works for - Find Subscription Plan By Id - Find
 * Subscription Plan By External Key - Find Subscription Plan By Advanced Find with multiple filters
 *
 * @author Shweta Hegde
 */
public class FindSubscriptionPlanPage extends SubscriptionPlanGenericPage {

    public FindSubscriptionPlanPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@id='nav-link-subscriptions']")
    private WebElement subscriptionsTab;

    @FindBy(xpath = "//span[contains(text(), 'Subscription Plans')]")
    private WebElement subscriptionPlanTab;

    @FindBy(css = ".form-group-labels > h3:nth-child(3)")
    private WebElement findByAdvancedFindTab;

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "offeringDetailId")
    private WebElement offeringDetailSelect;

    @FindBy(id = "usageType")
    private WebElement usageTypeSelect;

    @FindBy(name = "includeNew")
    private WebElement newStatusCheckbox;

    @FindBy(name = "includeActive")
    private WebElement activeStatusCheckBox;

    @FindBy(name = "includeCanceled")
    private WebElement canceledStatusCheckbox;

    @FindBy(id = "input-id")
    private WebElement idInput;

    @FindBy(xpath = "//*[@id='oneTimeEntitlements']/div/div/table/tbody/tr")
    private List<WebElement> oneTimeEntitlements;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindSubscriptionPlanPage.class.getSimpleName());

    /**
     * Method to navigate to Find Subscription plans
     */
    private void navigateToFindSubscriptionPlan() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION_PLAN.getForm() + "/findForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);
    }

    public List<WebElement> getOneTimeEntitlements() {
        return oneTimeEntitlements;
    }

    /**
     * Method to navigate to Advanced Find Subscription Plans
     */
    public void navigateToAdvancedFind() {
        navigateToFindSubscriptionPlan();
        getActions().click(findByAdvancedFindTab);
    }

    /**
     * Remove Unused/Unwanted Code Find Subscription Plan Id
     *
     * @return SubscriptionPlanPage
     */
    public SubscriptionPlanDetailPage findSubscriptionPlanById(final String id) {

        navigateToFindSubscriptionPlan();
        fillInSubscriptionPlanId(id);
        submit();
        return super.getPage(SubscriptionPlanDetailPage.class);
    }

    /**
     * Find a Subscription Plan by external key
     *
     * @return SubscriptionPlanPage
     */
    public SubscriptionPlanDetailPage findSubscriptionPlanByExternalKey(final String externalKey) {
        navigateToFindSubscriptionPlan();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
        LOGGER.info("Finding Subscription Plan By External Key : " + externalKey);
        submit(1);
        return super.getPage(SubscriptionPlanDetailPage.class);
    }

    /**
     * Find a Subscription Plan by Advanced Find
     *
     * @return GenericGrid
     */
    public GenericGrid findSubscriptionPlanByAdvancedFind(final String productLine, final String offeringType,
        final String offeringDetail, final UsageType usageType, final Boolean isNewSelected,
        final Boolean isActiveSelected, final Boolean isCanceledSelected) {

        navigateToFindSubscriptionPlan();
        clickOnFindAdvancedFindButton();

        // select filters
        selectProductLine(productLine);
        selectOfferingType(offeringType);
        selectOfferingDetail(offeringDetail);
        selectUsageType(usageType.toString());

        // select/unselect the status checkboxes
        if (isNewSelected) {
            checkNewStatusCheckbox();
        } else {
            uncheckNewStatusCheckbox();
        }
        if (isActiveSelected) {
            checkActiveStatusCheckbox();
        } else {
            uncheckActiveStatusCheckbox();
        }
        if (isCanceledSelected) {
            checkCanceledStatusCheckbox();
        } else {
            uncheckCanceledStatusCheckbox();
        }

        submit(2);

        return super.getPage(GenericGrid.class);
    }

    /**
     * Fill in the subscription plan id
     */
    private void fillInSubscriptionPlanId(final String id) {
        getActions().setText(idInput, id);
    }

    /**
     * Click on the Advanced Find tab of the subscription plan
     */
    private void clickOnFindAdvancedFindButton() {

        getActions().click(findByAdvancedFindTab);
    }

    /**
     * This method is to select offering type if it is not null
     */
    private void selectOfferingType(final String offeringType) {

        if (offeringType != null) {
            getActions().select(offeringTypeSelect, offeringType);
        }
    }

    /**
     * This method is to select offering detail if it is not null
     */
    private void selectOfferingDetail(final String offeringDetail) {

        if (offeringDetail != null) {
            getActions().select(offeringDetailSelect, offeringDetail);
        }
    }

    /**
     * This method is to select usage type if it is not null
     */
    private void selectUsageType(final String usageType) {

        if (usageType != null) {
            getActions().select(usageTypeSelect, usageType);
        }
    }

    /**
     * Select NEW status checkbox, if not selected in the advanced find of the subscription plan
     */
    private void checkNewStatusCheckbox() {

        getActions().check(newStatusCheckbox);
    }

    /**
     * Select ACTIVE status checkbox, if not selected in the advanced find of the subscription plan
     */
    private void checkActiveStatusCheckbox() {

        getActions().check(activeStatusCheckBox);
    }

    /**
     * Select CANCELED status checkbox, if not selected in the advanced find of the subscription plan
     */
    private void checkCanceledStatusCheckbox() {

        getActions().check(canceledStatusCheckbox);
    }

    /**
     * UnSelect NEW status checkbox, if not selected in the advanced find of the subscription plan
     */
    private void uncheckNewStatusCheckbox() {

        getActions().uncheck(newStatusCheckbox);
    }

    /**
     * Unselect ACTIVE status checkbox, if not selected in the advanced find of the subscription plan
     */
    private void uncheckActiveStatusCheckbox() {

        getActions().uncheck(activeStatusCheckBox);
    }

    /**
     * UnSelect CANCELED status checkbox, if not selected in the advanced find of the subscription plan
     */
    private void uncheckCanceledStatusCheckbox() {

        getActions().uncheck(canceledStatusCheckbox);
    }
}
