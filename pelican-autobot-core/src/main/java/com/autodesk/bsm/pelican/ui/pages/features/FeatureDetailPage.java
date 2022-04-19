package com.autodesk.bsm.pelican.ui.pages.features;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This page represents feature detail page in AT. This page can be viewed from Catalog -> Features -> Find feature
 *
 * @author vineel
 */

public class FeatureDetailPage extends GenericDetails {

    @FindBy(xpath = ".//*[@name='addFromSubPlansResults']//*[@class='message']")
    private WebElement addFeaturePopUp;

    @FindBy(xpath = ".//*[@name='addFromSubPlansResults']//*[@class='button']")
    private WebElement addFeaturePopUpConfirmButton;

    @FindBy(id = "removeFeature")
    private WebElement removeFeatureFromAllSubscriptionPlanLink;

    @FindBy(id = "copy")
    private WebElement makeFeatureCopyLink;

    @FindBy(id = "removeFromAllSubPlans")
    private WebElement removeAllFeaturePopUp;

    @FindBy(id = "removeFromSubPlansResults")
    private WebElement removeAllFeatureSuccessPopUp;

    @FindBy(id = "Remove Feature")
    private WebElement removeAllFeatureDialogRemoveButton;

    @FindBy(id = "Cancel")
    private WebElement removeAllFeatureDialogCancelButton;

    @FindBy(id = "Ok")
    private WebElement successDialogOkButton;

    @FindBy(tagName = "h1")
    private WebElement removeFeaturePopUpDialogHeader;

    @FindBy(id = "findAndRemove")
    private WebElement removeFeatureFromSelectedSubscriptionPlanLink;

    @FindBy(id = "offeringType")
    private WebElement offeringTypePopUpSelect;

    @FindBy(id = "usageType")
    private WebElement usageTypePopUpSelect;

    @FindBy(id = "supportLevel")
    private WebElement supportLevelPopUpSelect;

    @FindBy(id = "BackBtn")
    private WebElement removeSelectFeatureDialogBackButton;

    @FindBy(id = "CancelBtn")
    private WebElement removeSelectFeatureDialogCancelButton;

    @FindBy(id = "SubmitBtn")
    private WebElement removeSelectFeatureDialogRemoveFeatureButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmDialogConfirmButton;

    @FindBy(id = "cancel-btn")
    private WebElement confirmDialogCancelButton;

    @FindBy(xpath = ".//*[@class='popup-dialog']")
    private WebElement confirmDialogMessage;

    @FindBy(xpath = ".//*[@class='hd']")
    private WebElement confirmDialogHeader;

    @FindBy(xpath = ".//*[@class='hd']")
    private WebElement secondDialogHeader;

    @FindBy(xpath = ".//*[@class='error-message']")
    private WebElement removeSelectFeatureErrorMessage;

    @FindBy(xpath = ".//*[@id='find-results']//tbody//tr//td")
    private WebElement noSubscriptionFoundText;

    @FindBy(id = "findSubPlans")
    private WebElement findSubscriptionPlans;

    @FindBy(id = "addToSubPlans")
    private WebElement addFeatureToSubscriptionPlansLink;

    @FindBy(id = "Add Feature")
    private WebElement addFeatureButton;

    @FindBy(id = "licensingModelDropDown")
    private WebElement licensingModelSelect;

    @FindBy(id = "SubmitBtn")
    private WebElement selectPlansToAddFeatureButton;

    @FindBy(xpath = ".//*[@class='hd']")
    private WebElement addFeatureSelectLicensingPopUpHeader;

    @FindBy(id = "addToSubPlans")
    private WebElement addFeatureSelectLicensingPopUpMessage;

    @FindBy(id = "addFromSubPlansResults")
    private WebElement addFeatureSuccessPopUp;

    @FindBy(xpath = "//*[@id='updateproperties']/table/tbody/tr[1]/td[1]/span/input")
    private WebElement propertyNameInput;

    @FindBy(xpath = "//*[@id='updateproperties']/table/tbody/tr[1]/td[2]/span/input")
    private WebElement propertyValueInput;

    @FindBy(id = "updatepropertiessubmit")
    private WebElement updatePropertiesButton;

    @FindBy(name = "Edit")
    private WebElement editFeatureButton;

    @FindBy(id = "SelectAll")
    private WebElement selectAllButton;

    @FindBy(id = "coreProductSelect")
    private WebElement coreProductSelect;

    @FindBy(id = "manageCoreProducts")
    private WebElement manageCoreProductsLink;

    @FindBy(id = "saveCoreProducts")
    private WebElement saveCoreProductsButton;

    @FindBy(id = "coreProductsDisplay")
    private WebElement coreProductsDisplay;

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureDetailPage.class.getSimpleName());

    public FeatureDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * A method to return the feature id from the feature detail page
     *
     * @return featureId
     */
    public String getFeatureId() {
        return getValueByField("ID");
    }

    /**
     * A method to return the feature name from the feature detail page
     *
     * @return feature name
     */
    public String getFeatureName() {
        return getValueByField("Name");
    }

    /**
     * A method to return the feature external key from the feature detail page
     *
     * @return feature external key
     */
    public String getFeatureExternalKey() {
        return getValueByField("External Key");
    }

    /**
     * A method to return the feature type from the feature detail page
     *
     * @return featuretype
     */
    public String getFeatureType() {
        return getValueByField("Feature Type");
    }

    /**
     * A method to return the application from the feature detail page
     *
     * @return Application
     */
    public String getApplication() {
        return getValueByField("Application");
    }

    /**
     * This is a method to return the active field from the feature detail page
     *
     * @return String - active value.
     */
    public String getActive() {
        return getValueByField("Active");
    }

    /**
     * Method to click on Remove (this feature from all subscription plans) on feature detail page
     */
    public void clickRemoveFeatureFromAllSubs() {
        removeFeatureFromAllSubscriptionPlanLink.click();
        LOGGER.info("Clicked on remove feature link");
        switchDriverControlToPopUp();
    }

    /**
     * Method to click on 'make a copy' of this feature link on feature detail page
     */
    public void clickMakeFeatureCopyLink() {
        makeFeatureCopyLink.click();
        LOGGER.info("Clicked on make a copy feature link");
    }

    /**
     * This method returns the message on Remove Feature from All subscription plan pop up
     *
     * @return String
     */
    public String getPopUpMessageOnRemoveFeatureFromAllSubs() {
        return getPopUpMessage(removeAllFeaturePopUp);
    }

    /**
     * This method returns the success message after removing the Feature from All subscription plan pop up
     *
     * @return String
     */
    public String getPopUpSuccessMessageOnRemoveFeatureFromAllSubs() {
        return getPopUpMessage(removeAllFeatureSuccessPopUp);
    }

    /**
     * This method clicks on Remove Feature button on Remove Feature from All subscription plan pop up
     */
    public void clickRemoveFeatureButtonOnAllSubsPopUp() {
        switchDriverControlToPopUp();
        submitButton.click();
        LOGGER.info("Clicked on remove feature button.");
    }

    /**
     * This method clicks on Cancel button on Remove Feature from All subscription plan pop up
     */
    public void clickCancelButtonOnAllSubsPopUp() {
        switchDriverControlToPopUp();
        removeAllFeatureDialogCancelButton.click();
        LOGGER.info("Clicked on cancel.");
        switchDriverControlToParentWindow();
    }

    /**
     * This method returns the header on first pop up of Add/Remove feature 1) Remove Feature from All subscription
     * first pop up header 2) Search and Remove feature from selected subscription first pop up header 3) Add feature
     * first pop up
     *
     * @return String
     */
    public String getPopUpHeaderOnFeatureFirstPopUp() {
        switchDriverControlToPopUp();
        final String popUpHeader = removeFeaturePopUpDialogHeader.getText();
        LOGGER.info("Pop up header on first pop up: " + popUpHeader);
        return popUpHeader;
    }

    /**
     * This method clicks on ok button after the feature is added/removed from subscription plans to close the pop up.
     */
    public void clickOkButtonToCloseUp() {

        Util.waitInSeconds(TimeConstants.ONE_SEC);
        successDialogOkButton.sendKeys(Keys.RETURN);
        LOGGER.info("Clicked on ok.");

        // switch driver control back to parent window
        switchDriverControlToParentWindow();
    }

    /**
     * Method to get the message on pop up
     *
     * @return String
     */
    private String getPopUpMessage(final WebElement popUpElement) {

        String popUpMessage = null;
        switchDriverControlToPopUp();
        final String formText = popUpElement.getText();
        // Regular expression to split string ending with ? or .
        final String[] splitFormText = formText.split("(?<=[?.])");
        if (splitFormText[0] != null) {
            popUpMessage = splitFormText[0];
        }
        return popUpMessage;
    }

    /**
     * Method to click on Search and Remove (this feature from some subscription plans) on feature detail page
     */
    public void clickSearchAndRemoveFeatureFromSubs() {
        removeFeatureFromSelectedSubscriptionPlanLink.click();
        LOGGER.info("Clicked on Search and Remove feature");

    }

    /**
     * Method to click on Find Subscription plans, under related actions on feature detail page
     */
    public void clickFindSubscriptionPlans() {
        findSubscriptionPlans.click();
        LOGGER.info("Clicked on Find Subscription Plans under Related Actions");

    }

    /**
     * This method clicks on Find Subscription Plans To Remove button on Search and Remove pop up.
     */
    public void clickFindSubscriptionPlansToRemoveButtonOnSelectSubsPopUp() {
        switchDriverControlToPopUp();
        submit();
        LOGGER.info("Clicked on find subscription plans to remove.");
    }

    /**
     * This method clicks on Find Subscription Plans to Remove button on Search and Remove pop up.
     */
    public GenericGrid clickFindSubscriptionPlansToRemove() {
        switchDriverControlToPopUp();
        submit();
        LOGGER.info("Clicked on Find Subscription Plans To Remove.");
        return super.getPage(GenericGrid.class);
    }

    /**
     * This method selects the product line in Search and Remove feature pop up.
     */
    public void selectProductLine(final String productLine) {
        getActions().select(productLineSelect, productLine);
        LOGGER.info("Product line selected: " + productLine);
    }

    /**
     * This method selects the offering type in Search and Remove feature pop up.
     */
    public void selectOfferingType(final String offeringType) {

        getActions().select(offeringTypePopUpSelect, offeringType);
        LOGGER.info("Offering Type selected");
    }

    /**
     * This method selects the Usage type in Search and Remove feature pop up.
     */
    public void selectUsageType(final String usageType) {

        getActions().select(usageTypePopUpSelect, usageType);
        LOGGER.info("Usage Type selected");
    }

    /**
     * This method selects the Support Level in Search and Remove feature pop up.
     */
    public void selectSupportLevel(final String supportLevel) {

        getActions().select(supportLevelPopUpSelect, supportLevel);
        LOGGER.info("Support Level selected");
    }

    /**
     * This method clicks Select Plans to Remove feature button on Search and Remove Feature pop up
     */
    public void clickSelectPlansToRemoveFeature() {
        removeSelectFeatureDialogRemoveFeatureButton.click();
        LOGGER.info("clicked on select plans to remove feature button");
    }

    /**
     * This method clicks on Confirm button on Remove Feature from Select subscription plan pop up
     */
    public void clickConfirmPopUp() {
        confirmDialogConfirmButton.click();
        LOGGER.info("Clicked on confirm button pop up.");
    }

    /**
     * This method returns the message on the add feature popup
     *
     * @return String - message on the popup
     */
    public String readMessageOnAddFeaturePopUp() {
        return (addFeaturePopUp.getText());
    }

    /**
     * This is a method to click on confirm button in the add feature popup.
     */
    public void clickConfirmOnAddFeaturePopUp() {
        getActions().click(addFeaturePopUpConfirmButton);
        final String popUpHandle = getDriver().getWindowHandles().toArray()[0].toString();
        getDriver().switchTo().window(popUpHandle);
    }

    /**
     * This method returns the message from confirmation pop up
     *
     * @return String
     */
    public String getConfirmPopUpMessage() {
        return getPopUpMessage(confirmDialogMessage);
    }

    /**
     * This method returns the header from confirmation pop up
     *
     * @return String
     */
    public String getConfirmPopUpHeader() {
        return getPopUpMessage(confirmDialogHeader);
    }

    /**
     * This method returns the header on second pop up of Add/Remove feature 1) Search and Remove subscription plan
     * search results second pop up 2) Remove Feature from All subscription plan success second pop up 3) Add feature -
     * Subscription plan search results second pop up
     *
     * @return String
     */
    public String getPopUpHeaderOnFeatureSecondPopUp() {

        switchDriverControlToPopUp();
        final String popUpHeader = secondDialogHeader.getText();
        LOGGER.info("Pop up header on second pop up: " + popUpHeader);
        return popUpHeader;
    }

    /**
     * This method returns the error message on pop up when no subscription plan is selected
     */
    public String getSelectSubscriptionPlanErrorMessage() {
        return removeSelectFeatureErrorMessage.getText();
    }

    /**
     * Method to click on Add (this feature to subscription plans) on feature detail page
     */
    public void clickAddFeatureLink() {
        addFeatureToSubscriptionPlansLink.click();
        LOGGER.info("Clicked on Add this feature to subscription plans link");
        switchDriverControlToPopUp();

    }

    /**
     * This method clicks on Select Subscription Plans To add Feature button on Add feature second pop up.
     */
    public GenericGrid clickSelectSubscriptionPlansToAddFeature() {
        switchDriverControlToPopUp();
        selectPlansToAddFeatureButton.click();
        LOGGER.info("Clicked on Select Subscription Plans To Add Feature.");
        return super.getPage(GenericGrid.class);
    }

    /**
     * This method clicks on Find Subscription Plans to Add Feature button
     */
    public GenericGrid clickFindSubscriptionPlansToAddFeature() {
        switchDriverControlToPopUp();
        submit();
        LOGGER.info("Clicked on find Subscription Plans to Add Feature.");
        return super.getPage(GenericGrid.class);
    }

    /**
     * This method clicks on Add Feature button on Select Licensing Model pop up
     */
    public void clickAddFeature() {
        switchDriverControlToPopUp();
        addFeatureButton.click();
        LOGGER.info("Clicked on Add Feature on Licensing Model pop up.");
    }

    /**
     * This method selects the Licensing Model in Add Feature pop up.
     */
    public void selectLicensingModel(final String licensingModel) {

        getActions().select(licensingModelSelect, licensingModel);
        LOGGER.info("Licensing Model selected");
    }

    /**
     * This method returns the text when no subscription is found.
     */
    public String getNoSubscriptionFoundText() {
        return noSubscriptionFoundText.getText();
    }

    /**
     * This method clicks on Cancel button on Subscription Search Results page
     */
    public void clickCancelOnSubscriptionSearchResultsPopUp() {
        removeSelectFeatureDialogCancelButton.click();
        LOGGER.info("Clicked on cancel button");
        switchDriverControlToParentWindow();
    }

    /**
     * This method returns the header on third pop up(select licensing model) of add feature.
     *
     * @return String
     */
    public String getPopUpHeaderOnAddFeatureThirdPopUp() {

        switchDriverControlToPopUp();
        LOGGER.info("Get pop up header on select licensing");
        return addFeatureSelectLicensingPopUpHeader.getText();
    }

    /**
     * This method returns the message on third pop up(select licensing model) of add feature.
     *
     * @return String
     */
    public String getPopUpMessageOnAddFeatureThirdPopUp() {

        final String popUpMessage = getPopUpMessage(addFeatureSelectLicensingPopUpMessage);
        LOGGER.info("Pop up message on licensing model pop up: " + popUpMessage);
        return popUpMessage;
    }

    /**
     * This method returns the success message after adding Feature
     *
     * @return String
     */
    public String getPopUpSuccessMessageOnAddFeature() {
        final String successMessage = getPopUpMessage(addFeatureSuccessPopUp);
        LOGGER.info("Success message after adding feature: " + successMessage);
        return successMessage;
    }

    /**
     * This method will set a property for the feature
     */
    public void setProperty(final String propertyName, final String propertyValue) {
        getActions().setText(propertyNameInput, propertyName);
        LOGGER.info("Property name: " + propertyName);
        getActions().setText(propertyValueInput, propertyValue);
        LOGGER.info("Property value: " + propertyValue);
    }

    /**
     * This method clicks on 'Update' button on the feature detail page to add/update a property corresponding to the
     * feature
     */
    public FeatureDetailPage clickOnUpdatePropertiesButton() {
        updatePropertiesButton.click();
        LOGGER.info("Clicked on Update properties button.");

        return super.getPage(FeatureDetailPage.class);
    }

    /**
     * Click on the edit feature button
     */
    public EditFeaturePage clickOnEditFeatureButton() {
        getActions().click(editFeatureButton);

        return super.getPage(EditFeaturePage.class);
    }

    /**
     * This is a Temp method, which will click on Select All button. After the bug fix we need to remove this method
     */
    public void clickSelectAll() {
        getActions().click(selectAllButton);
        LOGGER.info("Clicked on Select All button");
    }

    /**
     * This method selects the Core Product in Add Feature pop up.
     */
    public void clickSelectCoreProducts(final List<String> coreProductList) {
        getActions().click(manageCoreProductsLink);
        for (final String coreProduct : coreProductList) {
            getActions().select(coreProductSelect, coreProduct);
            LOGGER.info("Core product selected " + coreProduct);
        }
    }

    /**
     * This method clicks on Core Product save button.
     */
    public void saveCoreProduct() {
        getActions().click(saveCoreProductsButton);
    }

    /**
     * Method to return selected/default core product.
     *
     * @return String
     */
    public String getCoreProductsDisplay() {
        return getActions().getText(coreProductsDisplay);
    }
}
