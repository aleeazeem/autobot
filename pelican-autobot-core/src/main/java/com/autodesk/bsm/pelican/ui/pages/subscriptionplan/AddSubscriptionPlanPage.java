package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.constants.FieldNameConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is a page object for Add Subscription Plan Page
 *
 * @author Shweta Hegde
 */
public class AddSubscriptionPlanPage extends SubscriptionPlanGenericPage {

    @FindBy(xpath = ".//*[@id='nav-link-subscriptions']")
    private WebElement subscriptionsTab;

    @FindBy(xpath = "//span[contains(text(), 'Subscription Plans')]")
    private WebElement subscriptionPlanTab;

    @FindBy(xpath = ".//*[@id='subnav-link-subscriptionPlan-add']")
    private WebElement subscriptionPlanAddLink;

    @FindBy(xpath = ".//*[@class='oneTimeEntitlement']//*[@class='delete-button']")
    private WebElement removeFeatureLink;

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "plan-status")
    private WebElement statusSelect;

    @FindBy(id = "cancellationPolicy")
    private WebElement cancellationPolicySelect;

    @FindBy(id = "usageType")
    private WebElement usageTypeSelect;

    @FindBy(id = "offeringDetailId")
    private WebElement offeringDetailSelect;

    @FindBy(id = "supportLevel")
    private WebElement supportLevelSelect;

    @FindBy(id = FieldNameConstants.PACKAGING_TYPE)
    private WebElement packagingTypeSelect;

    @FindBy(id = "input-module")
    private WebElement isModuleCheckBox;

    @FindBy(id = "moduleSelect")
    private WebElement moduleInput;

    @FindBy(id = "expirationEmailsEnabled")
    private WebElement sendExpirationEmailsSelect;

    private int entitlementNumber;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddSubscriptionPlanPage.class.getSimpleName());

    public AddSubscriptionPlanPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Add a Subscription Plan Info section in Add Subscription Plan Page This method is only for Plan Info section.
     *
     * @param name
     * @param externalKey
     * @param offeringType
     * @param status
     * @param cancellationPolicy
     * @param usageType
     * @param offeringDetail
     * @param productLine
     * @param supportLevel
     * @param packagingType
     * @param isSendExpirationEmails
     */
    public void addSubscriptionPlanInfo(final String name, final String externalKey, final OfferingType offeringType,
        final Status status, final CancellationPolicy cancellationPolicy, final UsageType usageType,
        final String offeringDetail, final String productLine, final SupportLevel supportLevel,
        final PackagingType packagingType, final boolean isSendExpirationEmails) {

        navigateToAddSubscriptionPlan();
        addName(name, PelicanConstants.PLAN_FIELD, nameInput);
        addExternalKey(externalKey, PelicanConstants.PLAN_FIELD, externalKeyInput);
        addOfferingType(offeringType);
        addStatus(status, statusSelect);
        addCancellationPolicy(cancellationPolicy);
        addUsageType(usageType);
        addOfferingDetail(offeringDetail);
        selectProductLine(productLine);
        selectPackagingType(packagingType);
        addSupportLevel(supportLevel);
        selectSendExpirationEmails(isSendExpirationEmails);
        Wait.pageLoads(driver);
    }

    public GenericGrid getGrid() {
        return super.getPage(GenericGrid.class);
    }

    /**
     * Navigating to add Subscription Plan
     */
    public void navigateToAddSubscriptionPlan() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION_PLAN.getForm() + "/addForm";
        LOGGER.info("Navigate to '" + url + "'");
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
    }

    /**
     * This is the method to check whether name input field is present on the page.
     *
     * @return Boolean - Is name input field present
     */
    public boolean isNameInputFieldPresent() {
        return isElementPresent(nameInput);
    }

    /**
     * Add offering type of a subscription plan
     *
     * @param offeringType
     */
    private void addOfferingType(final OfferingType offeringType) {
        if (offeringType != null) {
            getActions().select(offeringTypeSelect, offeringType.getDisplayName());
            LOGGER.info("Offering Type is Set to: " + offeringType);
        } else {
            LOGGER.info("Offering Type is required to add Subscription Plan");
        }
    }

    /**
     * Add cancellation policy of a subscription plan
     *
     * @param cancellationPolicy
     */
    private void addCancellationPolicy(final CancellationPolicy cancellationPolicy) {
        if (cancellationPolicy != null) {
            getActions().select(cancellationPolicySelect, cancellationPolicy.getDisplayName());
            LOGGER.info("Cancellation Policy is Set to: " + cancellationPolicy);
        } else {
            LOGGER.info("Cancellation Policy is set to *Cancel immediately without a refund* by Default");
        }
    }

    /**
     * Add usage type of a subscription plan
     *
     * @param usageType
     */
    private void addUsageType(final UsageType usageType) {
        if (usageType != null) {
            getActions().select(usageTypeSelect, usageType.getDisplayName());
            LOGGER.info("Usage Type is set to: " + usageType);
        } else {
            LOGGER.info("Usage Type is set to Null");
        }
    }

    /**
     * Add offering detail of a subscription plan
     *
     * @param offeringDetail
     */
    private void addOfferingDetail(final String offeringDetail) {
        if (offeringDetail != null) {
            getActions().select(offeringDetailSelect, offeringDetail);
            LOGGER.info("Offering detail is Set to:" + offeringDetail);
        } else {
            LOGGER.info("Offering detail is Set to null");
        }
    }

    /**
     * Add support level of a subscription plan
     *
     * @param supportLevel
     */
    private void addSupportLevel(final SupportLevel supportLevel) {
        if (supportLevel != null) {
            getActions().select(supportLevelSelect, supportLevel.getDisplayName());
            LOGGER.info("Support Level is Set to: " + supportLevel);
        } else {
            LOGGER.info("Support Level is Set to NONE");
        }
    }

    /**
     * This is the method to select the packaging type on the add subscription plan page
     *
     * @param packagingType - packaging type for the plan
     */
    private void selectPackagingType(final PackagingType packagingType) {
        if (packagingType != null) {
            getActions().select(packagingTypeSelect, packagingType.getDisplayName());
            LOGGER.info("Packaging Type is Set to: " + packagingType);
        } else {
            LOGGER.info("Packaging type is Set to NONE");
        }
    }

    /**
     * This is the method to select Send Expiration Emails flag.
     *
     * @param isSendExpirationEmails
     */
    private void selectSendExpirationEmails(final boolean isSendExpirationEmails) {
        if (!isSendExpirationEmails) {
            getActions().select(sendExpirationEmailsSelect, PelicanConstants.NO);
        }
        LOGGER.info("Send expiration emails is set to : " + isSendExpirationEmails);
    }

    /**
     * This method returns the list of options under Entitlement
     *
     * @param entitlementType
     * @return
     */
    public List<WebElement> getOptionsUnderEntitlement(final int entitlementNumber) {
        final Select entitlementSelect =
            new Select(driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].type")));

        return entitlementSelect.getOptions();
    }

    public void removeFeature() {
        getActions().click(removeFeatureLink);
        LOGGER.info("Clicked on remove feature");

    }

    /**
     * This method to set the feature as text input(not lookup)
     *
     * @param feature
     */
    public void setFeatureAsInput(final String feature) {
        final WebElement featureInput =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].itemId"));
        getActions().setText(featureInput, feature);
        featureInput.sendKeys(Keys.TAB);
        LOGGER.info("Feature name populated is : " + getFeatureName(entitlementNumber));
    }

    /**
     * This is the method to determine whether the assignable checkbox is checked or not
     *
     * @param entitlementNumber
     * @return
     */

    public boolean getAssignable(final int entitlementNumber) {

        final WebElement assignableCheckbox =
            driver.findElement(By.id("input-oneTimeEntitlements[" + entitlementNumber + "].assignable"));

        return assignableCheckbox.isSelected();
    }

    /**
     * This is a method to return the text from the disabled attribute from the assignable checkbox
     *
     * @param entitlementNumber
     * @return String
     */
    public String getDisabledAttributeValueFromAssignableCheckbox(final int entitlementNumber) {

        final WebElement assignableCheckbox =
            driver.findElement(By.id("input-oneTimeEntitlements[" + entitlementNumber + "].assignable"));

        return assignableCheckbox.getAttribute("disabled");
    }

    /**
     * This is the method to determine whether the assignable field is present on the entitlement section
     *
     * @param entitlementNumber
     * @return
     */
    public boolean isAssignablePresent(final int entitlementNumber) {

        try {
            final WebElement assignableCheckbox =
                driver.findElement(By.id("input-oneTimeEntitlements[" + entitlementNumber + "].assignable"));
            assignableCheckbox.isDisplayed();
            assignableCheckbox.click();
        } catch (final Exception e) {
            LOGGER.info("Assignable Checkbox is not present");
            return false;
        }
        LOGGER.info("Assignable Checkbox is present");
        return true;

    }

    /**
     * This is the method to determine whether the eos date field is present on the entitlement section
     *
     * @param entitlementNumber
     * @return
     */
    public boolean isEOSDateFieldPresent(final int entitlementNumber) {

        try {
            final WebElement eosDate =
                driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].eosDate"));
            eosDate.isDisplayed();
            eosDate.sendKeys("");
        } catch (final Exception e) {
            LOGGER.info("EOS Date Field is not present");
            return false;
        }
        LOGGER.info("EOS Date Field is present");
        return true;
    }

    /**
     * This is the method to determine whether the eol renewal date field is present on the entitlement section
     *
     * @param entitlementNumber
     * @return
     */
    public boolean isEOLRenewalDateFieldPresent(final int entitlementNumber) {

        try {
            final WebElement eolRenewalDate =
                driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].eolRenewalDate"));
            eolRenewalDate.isDisplayed();
            eolRenewalDate.sendKeys("");
        } catch (final Exception e) {
            LOGGER.info("EOL Renewal Date is not present");
            return false;
        }
        LOGGER.info("EOL Renewal Date is present");
        return true;
    }

    /**
     * This is the method to determine whether the eol immediate date field is present on the entitlement section
     *
     * @param entitlementNumber
     * @return
     */
    public boolean isEOLImmediateDateFieldPresent(final int entitlementNumber) {

        try {
            final WebElement eolImmediateDate =
                driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].eolImmediateDate"));
            eolImmediateDate.isDisplayed();
            eolImmediateDate.sendKeys("");
        } catch (final Exception e) {
            LOGGER.info("EOl Immediate Date Element is not present");
            return false;
        }
        LOGGER.info("EOl Immediate Date Element is present");
        return true;
    }

    /**
     * Click on the save button of the subscription plan which is inherited from Generic Page This method is written so
     * that any test class can use this
     *
     * @param shouldHandlePopup TODO
     *
     * @return SubscriptionPlanPage
     */
    public SubscriptionPlanDetailPage clickOnSave(final boolean shouldHandlePopup) {

        Util.scroll(driver, "2400", "0");
        getActions().click(submitButton);
        Wait.pageLoads(getDriver());
        if (shouldHandlePopup) {
            clickOnConfirmAddFeaturesConfirmationPopUp();
        }
        return super.getPage(SubscriptionPlanDetailPage.class);
    }

    public void clickOnSave() {
        Util.scroll(driver, "2400", "0");
        getActions().click(submitButton);
        Wait.pageLoads(getDriver());
    }

    /**
     * Click on the cancel button of the subscription plan which is inherited from Generic Page This method is written
     * so that any test class can use this
     */
    public void clickOnCancel() {

        cancel();
    }

}
