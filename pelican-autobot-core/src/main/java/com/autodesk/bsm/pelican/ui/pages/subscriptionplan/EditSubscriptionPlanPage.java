package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a page object for Edit Subscription Plan page This class methods are called by Find Subscription Plan
 * page class
 *
 * @author Shweta Hegde
 */
public class EditSubscriptionPlanPage extends SubscriptionPlanGenericPage {

    @FindBy(id = "input-name")
    private WebElement nameInput;

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

    @FindBy(id = "packagingType")
    private WebElement packagingTypeSelect;

    @FindBy(id = "input-module")
    private WebElement isModuleCheckBox;

    @FindBy(id = "moduleSelect")
    private WebElement moduleInput;

    @FindBy(id = "delete-button")
    private WebElement deleteModuleButton;

    @FindBy(id = "oneTimeEntitlements[0].type")
    private WebElement oneTimeEntitlementsSelect;

    @FindBy(id = "addOffer")
    private WebElement addOfferLink;

    @FindBy(css = ".editOfferButton")
    private WebElement editOfferButton;

    @FindBy(className = "deleteOfferButton")
    private WebElement deleteOfferButton;

    @FindBy(id = "billingFrequency")
    private WebElement billingFrequency;

    @FindBy(id = "saveOffer")
    private WebElement saveOffer;

    @FindBy(className = "editPriceButton")
    private WebElement editPriceButton;

    @FindBy(className = "deletePriceButton")
    private WebElement deletePriceButton;

    @FindBy(id = "priceAmount")
    private WebElement priceAmount;

    @FindBy(id = "expiredPrice")
    private WebElement expiredPrice;

    @FindBy(id = "savePrice")
    private WebElement savePrice;

    @FindBy(xpath = "//*[@id='prices-table[0]']/tbody/tr/td[1]")
    private WebElement priceId;

    @FindBy(xpath = ".//*[@name='Edit Subscription Plan']")
    private WebElement editSubscriptionPlanButton;

    @FindBy(css = ".errors")
    private WebElement planExternalKeyErrorMessage;

    @FindBy(xpath = ".//*[@class='form-section-content']//*[@class='buttons']//*[@class='submit']")
    private WebElement saveChangesButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(id = "offerStatus")
    private WebElement offerStatusSelect;

    @FindBy(id = "billingCountType")
    private WebElement billingCycleTypeSelect;

    @FindBy(id = "billingFrequencyCount")
    private WebElement billingFrequencyCountNumber;

    @FindBy(id = "expirationEmailsEnabled")
    private WebElement sendExpirationEmailsSelect;

    @FindBy(id = "input-shortDescription")
    private WebElement shortDescriptionInput;

    @FindBy(id = "input-longDescription")
    private WebElement longDescriptionInput;

    @FindBy(id = "input-smallImageURL")
    private WebElement smallImageUrlInput;

    @FindBy(id = "input-mediumImageURL")
    private WebElement mediumImageUrlInput;

    @FindBy(id = "input-largeImageURL")
    private WebElement largeImageUrlInput;

    @FindBy(id = "input-buttonDisplayName")
    private WebElement buttonDisplayNameInput;

    private static final String CHOOSE_ONE = "-- CHOOSE ONE --";

    private int entitlementNumber;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditSubscriptionPlanPage.class.getSimpleName());

    public EditSubscriptionPlanPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Edit a subscription plan with multiple parameters This method is only for Plan Info section.
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
    public void editSubscriptionPlanInfo(final String name, final String externalKey, final OfferingType offeringType,
        final Status status, final CancellationPolicy cancellationPolicy, final UsageType usageType,
        final String offeringDetail, final String productLine, final SupportLevel supportLevel,
        final PackagingType packagingType, final boolean isSendExpirationEmails) {

        editName(name);
        editExternalKey(externalKey);
        editOfferingType(offeringType);
        editStatus(status);
        editCancellationPolicy(cancellationPolicy);
        editUsageType(usageType);
        editOfferingDetail(offeringDetail);
        editProductLine(productLine);
        editPackagingType(packagingType);
        editSupportLevel(supportLevel);
        editSendExpirationEmails(isSendExpirationEmails);
    }

    /**
     * This method is to edit Modules in Add Subscription Plan Page
     */
    public void editSubscriptionPlanModules(final Boolean isModule, final String module) {

        if (isModule) {
            checkIsModule();
        } else {
            uncheckIsModule();
        }
        addModule(module);
    }

    /**
     * Edit name field of a subscription plan
     */
    private void editName(final String name) {

        if (StringUtils.isNotEmpty(name)) {
            Wait.elementDisplayed(driver, nameInput);
            nameInput.clear();
            getActions().setText(nameInput, name);
        }
    }

    /**
     * Edit external key field of a subscription plan
     */
    public void editExternalKey(final String externalKey) {

        if (StringUtils.isNotEmpty(externalKey)) {
            Wait.elementDisplayed(driver, externalKeyInput);
            externalKeyInput.clear();
            getActions().setText(externalKeyInput, externalKey);
        } else if (externalKey != null) {
            Wait.elementDisplayed(driver, externalKeyInput);
            externalKeyInput.clear();
            getActions().setText(externalKeyInput, externalKey);
        }

    }

    /**
     * Edit offering type of a subscription plan
     */
    private void editOfferingType(final OfferingType offeringType) {
        if (offeringType != null && StringUtils.isNotEmpty(offeringType.toString())) {
            getActions().select(offeringTypeSelect, offeringType.toString());
        }
    }

    /**
     * Edit status of a subscription plan
     */
    public void editStatus(final Status status) {

        if (status != null && StringUtils.isNotEmpty(status.getDisplayName())) {
            getActions().select(statusSelect, status.getDisplayName());
        }
    }

    /**
     * Edit cancellation policy of a subscription plan
     */
    private void editCancellationPolicy(final CancellationPolicy cancellationPolicy) {

        if (cancellationPolicy != null && StringUtils.isNotEmpty(cancellationPolicy.toString())) {
            getActions().select(cancellationPolicySelect, cancellationPolicy.toString());
        }
    }

    /**
     * Edit usage type of a subscription plan
     */
    public void editUsageType(final UsageType usageType) {

        if (usageType != null) {
            getActions().select(usageTypeSelect, usageType.toString());
        } else {
            getActions().select(usageTypeSelect, CHOOSE_ONE);
        }
    }

    /**
     * Edit offering detail of a subscription plan
     */
    private void editOfferingDetail(final String offeringDetail) {
        if (StringUtils.isNotEmpty(offeringDetail)) {
            getActions().select(offeringDetailSelect, offeringDetail);
        }
    }

    /**
     * Edit product line of a subscription plan
     */
    public void editProductLine(final String productLine) {

        if (productLine != null) {
            getActions().select(productLineSelect, productLine);
        } else {
            getActions().select(productLineSelect, CHOOSE_ONE);
        }
    }

    /**
     * Edit support level of a subscription plan
     */
    private void editSupportLevel(final SupportLevel supportLevel) {
        if (supportLevel != null && StringUtils.isNotEmpty(supportLevel.toString())) {
            getActions().select(supportLevelSelect, supportLevel.toString());
        }
    }

    /**
     * This is the method to edit the packaging type on the edit subscription plan page
     *
     * @param packagingType - packaging type for the plan
     */
    public void editPackagingType(final PackagingType packagingType) {
        if (packagingType != null) {
            getActions().select(packagingTypeSelect, packagingType.getDisplayName());
        } else {
            getActions().select(packagingTypeSelect, PelicanConstants.NONE);
        }
    }

    /**
     * Method to edit Send Expiration Emails flag.
     *
     * @param isSendExpirationEmails
     */
    public void editSendExpirationEmails(final boolean isSendExpirationEmails) {
        if (isSendExpirationEmails) {
            getActions().select(sendExpirationEmailsSelect, PelicanConstants.YES);
        } else {
            getActions().select(sendExpirationEmailsSelect, PelicanConstants.NO);
        }
    }

    /**
     * Check 'is Module'
     */
    private void checkIsModule() {
        getActions().check(isModuleCheckBox);
    }

    /**
     * UnCheck 'is Module'
     */
    private void uncheckIsModule() {
        getActions().uncheck(isModuleCheckBox);
    }

    /**
     * Delete module
     */
    public void editModule() {

        getActions().click(deleteModuleButton);
    }

    /**
     * Add module
     */
    private void addModule(final String module) {

        Wait.elementVisibile(driver, moduleInput);
        getActions().setText(moduleInput, module);
    }

    /**
     * Click on the save button of the subscription plan which is inherited from Generic Page This method is written so
     * that any test class can use this
     *
     * @param shouldHandlePopup TODO
     * @return SubscriptionPlanPage
     */
    public SubscriptionPlanDetailPage clickOnSave(final boolean shouldHandlePopup) {

        Wait.elementVisibile(driver, saveChangesButton);
        try {
            getActions().click(saveChangesButton);
            LOGGER.info("Clicked on the save button");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Could not be able to click on save changes button");
        }
        Wait.elementClickable(driver, confirmButton);
        try {
            getActions().click(confirmButton);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
            clickOnConfirmAddFeaturesConfirmationPopUp();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("No Such Element Exception thrown!");
        }
        Wait.pageLoads(getDriver());
        return super.getPage(SubscriptionPlanDetailPage.class);
    }

    /**
     * Click on the save button of the subscription plan which is inherited from Generic Page This method is written so
     * that any test class can use this
     *
     * @return SubscriptionPlanPage
     */
    public SubscriptionPlanDetailPage clickOnSave() {

        Util.scroll(driver, "2400", "0");
        getActions().click(submitButton);
        Wait.pageLoads(getDriver());

        clickOnConfirmAddFeaturesConfirmationPopUp();

        return super.getPage(SubscriptionPlanDetailPage.class);
    }

    /**
     * Click on the cancel button of the subscription plan which is inherited from Generic Page This method is written
     * so that any test class can use this
     */
    public void clickOnCancel() {

        cancel();
    }

    /**
     * Click on the edit offer button of the subscription plan
     */
    public void clickOnEditOfferButton() {

        Util.scroll(driver, "1000", "0");
        try {
            Wait.elementDisplayed(driver, editOfferButton);
            editOfferButton.sendKeys(Keys.RETURN);
            LOGGER.info("Clicked on the save offer button");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Could not click on save offer button");
            try {
                getActions().click(editOfferButton);
                LOGGER.info("Clicked on the save offer button");
            } catch (final org.openqa.selenium.NoSuchElementException exception) {
                LOGGER.info("Attempt 2: failed to click on save offer button");
            }
        }
        final String popUpHandle = getDriver().getWindowHandles().toArray()[0].toString();
        getDriver().switchTo().window(popUpHandle);
    }

    /**
     * Click on the edit price button of the subscription plan
     */
    public void clickOnEditPriceButton() {
        getActions().click(editPriceButton);
        final String popUpHandle = getDriver().getWindowHandles().toArray()[0].toString();
        getDriver().switchTo().window(popUpHandle);
    }

    /**
     * Select the billing frequency in Add/Edit offer pop-up window
     */
    public void selectBillingFrequency(final String frequency) {
        getActions().select(billingFrequency, frequency);
    }

    /**
     * Click on the delete offer button of the subscription plan
     */
    public void clickOnDeleteOfferButton() {
        getActions().click(deleteOfferButton);
        LOGGER.info("Clicked on delete offer button");
    }

    /**
     * Click on the delete price button of the subscription plan
     */
    public void clickOnDeletePriceButton() {
        Util.scroll(driver, "600", "100");
        getActions().click(deletePriceButton);
    }

    /**
     * Click on the save offer button
     */
    public void clickOnSaveOfferButton() {
        getActions().click(saveOffer);
        LOGGER.info("Save offer button has been clicked");
    }

    /**
     * method to check the presence of save button
     */
    public boolean isSaveChangesButtonPresent() {
        boolean isSaveChangesButtonPresent = false;
        try {
            Wait.elementDisplayed(driver, saveOffer);
            isSaveChangesButtonPresent = saveOffer.isDisplayed();
            LOGGER.info("Save changes button Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Save changes button Doesn't Exist");
        }
        return isSaveChangesButtonPresent;
    }

    /**
     * This is a method which returns the entitlement details from the plan detail page
     *
     * @param rowIndex
     * @param columnIndex
     * @return Feature details - String
     */
    public String getEntitlementDetails(final int rowIndex, final int columnIndex) {

        final WebElement element = driver.findElement(
            By.xpath("//*[@id=\"fieldset-onetime-entitlements\"]//tr[" + rowIndex + "]/td[" + columnIndex + "]"));

        return element.getText();

    }

    /**
     * Edit the price amount field of a subscription plan
     */
    public void editPriceAmount(final String priceAmountVal) {
        getActions().setText(priceAmount, priceAmountVal);
    }

    /**
     * Click on the save price button
     */
    public void clickOnSavePriceButton() {
        getActions().click(savePrice);
    }

    /**
     * Check the expired price checkbox
     */
    public void checkExpiredPrice() {
        getActions().check(expiredPrice);
    }

    /**
     * This method selects the licensing model
     *
     * @param licensingModel
     */
    public void selectLicensingModel(final int index, final String licensingModel) {
        final WebElement licensingModelSelect =
            driver.findElement(By.name("oneTimeEntitlements[" + index + "].itemLicensingModelId"));
        getActions().select(licensingModelSelect, licensingModel);
        LOGGER.info("Licensing Model selected : " + licensingModel);
    }

    /**
     * Get the priceId of a subscription plan
     *
     * @return priceId
     */
    public String getPriceId() {
        return priceId.getText();
    }

    /**
     * This method will return the error message for the external key on edit subscription plan page
     *
     * @return String = SubscriptionPlanErrorMessage
     */
    public String getPlanExternalKeyErrorMessage() {
        final String planErrorMessage = planExternalKeyErrorMessage.getText();
        LOGGER.info("Subscription Plan  External Key Error Message:" + planErrorMessage);

        return planErrorMessage;
    }

    /**
     * Method to edit fields of an offer
     *
     * @param status
     * @param cycleType
     * @param billingFrequencyInNumber
     * @param frequency
     */
    public void editFieldsOfOffer(final Status status, final String cycleType, final String billingFrequencyInNumber,
        final BillingFrequency frequency) {
        if (status != null) {
            getActions().select(offerStatusSelect, status.getDisplayName());
            LOGGER.info("Status offer is set to " + status.getDisplayName());
        }
        if (cycleType != null) {
            getActions().select(billingCycleTypeSelect, cycleType);
            LOGGER.info("Billing Cycle Type is set to " + cycleType);
        }
        if (billingFrequencyInNumber != null) {
            getActions().select(billingFrequencyCountNumber, billingFrequencyInNumber);
            LOGGER.info("Billing Frequency Count is set to " + billingFrequencyInNumber);
        }
        if (frequency != null) {
            getActions().select(billingFrequency, frequency.getDisplayName());
            LOGGER.info("Frequency offer is set to " + frequency.getDisplayName());
        }
    }

    /**
     * Method to set additional data in plan.
     *
     * @param shortDescription
     * @param longDescription
     * @param smallImageUrl
     * @param mediumImageUrl
     * @param largeImageUrl
     * @param buttonDisplayName
     */
    public void editAdditionalData(final String shortDescription, final String longDescription,
        final String smallImageUrl, final String mediumImageUrl, final String largeImageUrl,
        final String buttonDisplayName) {
        if (shortDescription != null) {
            getActions().setText(shortDescriptionInput, shortDescription);
            LOGGER.info("Short decription is set to " + shortDescription);
        }
        if (longDescription != null) {
            getActions().setText(longDescriptionInput, longDescription);
            LOGGER.info("Long description is set to " + longDescription);
        }
        if (smallImageUrl != null) {
            getActions().setText(smallImageUrlInput, smallImageUrl);
            LOGGER.info("Small image url is set to " + smallImageUrl);
        }
        if (mediumImageUrl != null) {
            getActions().setText(mediumImageUrlInput, mediumImageUrl);
            LOGGER.info("Medium image url is set to " + mediumImageUrl);
        }
        if (largeImageUrl != null) {
            getActions().setText(largeImageUrlInput, largeImageUrl);
            LOGGER.info("Large image url is set to " + largeImageUrl);
        }
        if (buttonDisplayName != null) {
            getActions().setText(buttonDisplayNameInput, buttonDisplayName);
            LOGGER.info("Button display name is set to " + buttonDisplayName);
        }
    }

    /**
     * This method adds currency amount entitlement to a subscription plan
     *
     * @param amount
     * @param currencyType
     * @param entitlementNumber
     */
    public void addOneTimeCurrencyAmountEntitlement(final String amount, final String currencyType,
        final int entitlementNumber) {
        this.entitlementNumber = entitlementNumber;
        Util.scroll(driver, "1000", "0");
        selectEntitlement(PelicanConstants.CURRENCY_FIELD + " " + PelicanConstants.AMOUNT_FIELD);
        setCurrencyAmount(amount);
        selectVirtualCurrency(currencyType);
    }

    /**
     * This method selects the entitlement type
     *
     * @param entitlementType
     */
    protected void selectEntitlement(final String entitlementType) {
        final WebElement entitlementSelect =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].type"));
        getActions().select(entitlementSelect, entitlementType);
        LOGGER.info("Entitlement selected : " + entitlementType);
    }

    /**
     * This method sets currency amount
     *
     * @param amount
     */
    private void setCurrencyAmount(final String amount) {
        final WebElement amountInput =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].currencyAmount"));
        getActions().setText(amountInput, amount);
        LOGGER.info("Currency Amount is set to : " + amount);
    }

    /**
     * This method selects virtual currency.
     *
     * @param virtualCurrency
     */
    private void selectVirtualCurrency(final String virtualCurrency) {
        final WebElement virtualCurrencySelect =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].currencyId"));
        getActions().select(virtualCurrencySelect, virtualCurrency);
        LOGGER.info("Virtual Currency selected : " + virtualCurrency);
    }

    /**
     * method to check/uncheck the remove feature check box
     *
     * @return boolean based on success or failure
     */
    public Boolean removeFeature(final String id, final Boolean shouldCheck) {
        final String selector = ".//*[@id='itemRemove-" + id + "']";
        try {
            if (!(getDriver().findElement(By.xpath(selector)).isSelected()) && shouldCheck) {
                getDriver().findElement(By.xpath(selector)).click();
            }
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            return false;
        }

        return true;
    }

}
