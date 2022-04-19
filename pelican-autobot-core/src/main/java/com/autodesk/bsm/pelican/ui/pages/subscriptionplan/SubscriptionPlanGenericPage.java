package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPlanGenericPage extends GenericDetails {

    @FindBy(id = "billingFrequency")
    private WebElement billingFrequency;

    @FindBy(id = "billingFrequencyCount")
    private WebElement billingFrequencyCountInput;

    @FindBy(id = "billingCountType")
    private WebElement billingCountType;

    @FindBy(id = "billingCount")
    private WebElement billingCount;

    @FindBy(id = "offerStatus")
    private WebElement offerStatus;

    @FindBy(id = "addOffer")
    private WebElement addOffer;

    @FindBy(id = "offerExternalKey")
    private WebElement offerExternalKey;

    @FindBy(id = "saveOffer")
    private WebElement saveOfferButton;

    @FindBy(className = "addPriceButton")
    private WebElement addPriceLink;

    @FindBy(id = "storeIdSelect")
    private WebElement storeIdSelect;

    @FindBy(id = "priceListIdSelect")
    private WebElement priceListIdSelect;

    @FindBy(id = "priceAmount")
    private WebElement priceAmount;

    @FindBy(id = "startDatePicker")
    private WebElement startDatePicker;

    @FindBy(id = "endDatePicker")
    private WebElement endDatePicker;

    @FindBy(id = "savePrice")
    private WebElement savePriceButton;

    @FindBy(id = "expiredPrice")
    private WebElement addExpiredPriceCheckBox;

    @FindBy(id = "coreProductSelect")
    protected WebElement coreProductSelect;

    @FindBy(id = "saveCoreProducts")
    protected WebElement saveCoreProductsButton;

    @FindBy(id = "input-pattern")
    private WebElement featureInputPattern;

    @FindBy(xpath = ".//*[@id='find-results']/table/thead[1]/tr/td")
    private WebElement featureResultsHeader;

    @FindBy(xpath = ".//*[@id='find-results']/table/tbody/tr/td")
    private WebElement featureResultsBody;

    @FindBy(name = "item")
    private WebElement itemRadioButton;

    private int entitlementNumber;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanGenericPage.class.getSimpleName());

    public SubscriptionPlanGenericPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * This is a method to click on confirm popup for add features on the subscription plan page
     */
    public void clickOnConfirmAddFeaturesConfirmationPopUp() {
        switchDriverControlToPopUp();
        clickOnConfirmPopUpButton();
        switchDriverControlToParentWindow();
    }

    /**
     * This is a method to read the message in add feature confirmation popup and click on confirm/cancel button in the
     * popup
     *
     * @param isConfirm - Boolean to click on confirm button
     *
     * @return String - message in the pop up
     */
    public String readAndConfirmOrCancelMessage(final boolean isConfirm) {
        Wait.alertAppears(driver);
        switchDriverControlToPopUp();
        final String popUpMessage = readMessageInPopUp();
        if (isConfirm) {
            clickOnConfirmPopUpButton();
        } else {
            clickOnCancelPopUpButton();
        }
        switchDriverControlToParentWindow();

        return popUpMessage;
    }

    /**
     * This is a method to click on cancel button in the add features confirmation popup on the subscription plan page
     */
    public void clickOnCancelAddFeatureConfirmationPopUp() {
        switchDriverControlToPopUp();
        clickOnCancelPopUpButton();
        switchDriverControlToParentWindow();
    }

    /**
     * Method to add offer in subscription plan
     *
     * @param externalKey
     * @param name
     * @param status
     * @param isUnlimitedCycle
     * @param cycleCount
     * @param frequency
     * @param billingFrequencyCount
     * @param populateDefaultOfferName
     * @return offerName
     */
    public String addOffer(final String externalKey, final String name, final Status status,
        final Boolean isUnlimitedCycle, final String cycleCount, final BillingFrequency frequency,
        final int billingFrequencyCount, final Boolean populateDefaultOfferName) {

        LOGGER.info("Adding a Offer in Subscription Plan");
        Util.scroll(driver, "1000", "0");

        clickOnAddOfferLink();
        addStatus(status, offerStatus);
        addBillingCycleCount(isUnlimitedCycle, cycleCount);
        addBillingFrequency(frequency, billingFrequencyCount);
        final String getOfferName = addName(name, PelicanConstants.OFFER_FIELD, offerName, populateDefaultOfferName);
        addExternalKey(externalKey, PelicanConstants.OFFER_FIELD, offerExternalKey);
        clickOnSaveOffer();

        return getOfferName;
    }

    /**
     * This method sets billing frequency and count
     *
     * @param frequency
     * @param billingFrequencyCount
     */
    private void addBillingFrequency(final BillingFrequency frequency, final int billingFrequencyCount) {
        getActions().select(billingFrequency, frequency.getDisplayName());
        LOGGER.info("Billing Frequency is selected to: " + frequency.getDisplayName());
        // Since 1 is selected by default so we don't need to select 1.
        // Some of the existing classes are using 0 for logic so condition is kept as >1 instead of ==1.
        if (billingFrequencyCount > 1) {
            getActions().setText(billingFrequencyCountInput, String.valueOf(billingFrequencyCount));
            LOGGER.info("Billing Frequency  Count is selected to: " + billingFrequencyCount);
            // Clicking on offerExternalKey so that offer name is auto populated.
            // If we perform the same action manually, we don't need to click. This is only for automation.
            getActions().click(offerExternalKey);
        }
    }

    /**
     * Method to add Billing cycle count
     *
     * @param isUnlimited
     * @param billingCycleCount
     */
    private void addBillingCycleCount(final Boolean isUnlimited, final String billingCycleCount) {
        if (!(isUnlimited)) {
            LOGGER.info("Setting Billing Count Type as limited and cycle count as " + billingCycleCount);
            getActions().select(billingCountType, PelicanConstants.LIMITED_OFFER_TYPE);
            getActions().setText(billingCount, billingCycleCount);
        }
    }

    /**
     * This method clicks on "Add Offer" link
     */
    private void clickOnAddOfferLink() {
        getActions().click(addOffer);
        LOGGER.info("Clicked on Add Offer Link");
    }

    /**
     * This method clicks on save offer button
     */
    private void clickOnSaveOffer() {
        getActions().click(saveOfferButton);
        Wait.pageLoads(driver);
        LOGGER.info("Offer is added to Subscription Plan");
    }

    /**
     * Add status of a subscription plan
     *
     * @param status
     * @param statusWebElement
     */
    public void addStatus(final Status status, final WebElement statusWebElement) {
        if (status != null) {
            getActions().select(statusWebElement, status.getDisplayName());
            LOGGER.info("Stauts is set to: " + status);
        } else {
            LOGGER.info("Stauts is set to *New* By Default");
        }
    }

    /**
     * Add name field of a subscription plan
     *
     * @param name
     * @param type
     * @param nameInputWebElement
     */
    public String addName(final String name, final String type, final WebElement nameInputWebElement) {
        return addName(name, type, nameInputWebElement, false);
    }

    /**
     * Add name field of a subscription Offer
     *
     * @param name
     * @param type
     * @param nameInputWebElement
     * @param populateDefault
     * @return offerName
     */
    public String addName(final String name, final String type, final WebElement nameInputWebElement,
        final Boolean populateDefault) {

        String offerName = null;
        if (!(populateDefault)) {
            if (name == null && type.equals(PelicanConstants.PLAN_FIELD)) {
                offerName = "SubscriptionOffer" + LocalDateTime.now();
                getActions().setText(nameInputWebElement, offerName);
                LOGGER.info("Subscription Offer Name is set with a time");
            }
            if (name != null) {
                setName(name);
            }
        } else {
            offerName = getOfferName();
        }

        return offerName;
    }

    /**
     * get Offer Name from Subscription Plan pages
     *
     * @return offerName
     */
    public String getOfferNameFromSubscriptionPlanPages() {
        return offerNameFromSubscriptionPlanPage.getText();
    }

    /**
     * get Offer Name from Subscription Plan pages
     *
     * @return offerName
     */
    public String getOfferNameFromSubscriptionPlanDetailsPage() {
        return getOfferNameInSubscriptionDetailsPage.getText();
    }

    /**
     * Add external key field of a subscription plan
     *
     * @param externalKey
     * @param type
     * @param externalKeyInputWebElement
     */
    public void addExternalKey(final String externalKey, final String type,
        final WebElement externalKeyInputWebElement) {
        String extKey = externalKey;
        if (extKey == null && type.equals(PelicanConstants.PLAN_FIELD)) {
            extKey = RandomStringUtils.randomAlphanumeric(8);
            LOGGER.info("Subscription Plan External Key is Set by auto generated random string");
        }
        if (type.equals(PelicanConstants.PLAN_FIELD) && extKey != null) {
            setExternalKey(extKey);
        } else if (type.equals(PelicanConstants.OFFER_FIELD) && extKey != null) {
            getActions().setText(externalKeyInputWebElement, extKey);
        }
    }

    /**
     * Method to add prices in an offer
     */
    public void addPricesInOffer(final int noOfPrices, final String store, final String priceList, final String amount,
        final String startDate, final String endDate) {

        LOGGER.info("Number of Prices are going to be added: " + noOfPrices);
        for (int i = 0; i < noOfPrices; i++) {
            clickOnAddPriceLink();
            selectStoreId(store);
            selecPriceList(priceList);
            setAmount(amount);
            setStartDate(startDate);
            setEndDate(endDate);
            clickOnSavePrice();

            if (isAddExpiredPriceDisplayed()) {
                checkConfirmationMessage();
            }
            Wait.pageLoads(driver);
            LOGGER.info("Adding a price in Offer is completed");
        }
    }

    /**
     * This method clicks on "Add Price" link
     */
    private void clickOnAddPriceLink() {
        getActions().click(addPriceLink);
        LOGGER.info("Clicked on Add Price Link");
    }

    /**
     * This method selects store while adding price
     *
     * @param store
     */
    private void selectStoreId(final String store) {
        getActions().select(storeIdSelect, store);
        LOGGER.info("Selected store is: " + store);
    }

    /**
     * This method selects pricelist for the price
     *
     * @param priceList
     */
    private void selecPriceList(final String priceList) {
        getActions().select(priceListIdSelect, priceList);
        LOGGER.info("Selected Price List is: " + priceList);
    }

    /**
     * This method sets amount while adding a price
     *
     * @param amount
     */
    private void setAmount(final String amount) {
        getActions().setText(priceAmount, amount);
        LOGGER.info("Price Amount is Set to: " + amount);
    }

    /**
     * This method sets start date for the price
     *
     * @param startDate
     */
    private void setStartDate(final String startDate) {
        getActions().setText(startDatePicker, startDate);
        LOGGER.info("Price Start Date is Set to: " + startDate);
        // in order to Diminish Pop Calendar need to click any where on window
        getActions().click(priceAmount);
    }

    /**
     * This method sets end date for the price
     *
     * @param endDate
     */
    private void setEndDate(final String endDate) {
        getActions().setText(endDatePicker, endDate);
        LOGGER.info("Price End Date is set to: " + endDate);
        // in order to Diminish Pop Calendar need to click any where on window
        getActions().click(priceAmount);
    }

    /**
     * This method clicks on save price button
     */
    private void clickOnSavePrice() {
        getActions().click(savePriceButton);
        LOGGER.info("Save Price button is clicked");
    }

    /**
     * Method to see whether or not warning message of Add Expired Prices is Displayed
     *
     * @return warning message as true or false
     */
    private boolean isAddExpiredPriceDisplayed() {
        boolean isAddExpiredPriceDisplayed = false;
        try {
            Wait.elementVisibile(driver, addExpiredPriceCheckBox);
            isAddExpiredPriceDisplayed = addExpiredPriceCheckBox.isDisplayed();
            LOGGER.info("**Add expired price** Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("**Add expired price** Doesn't Exist");
        }
        return isAddExpiredPriceDisplayed;
    }

    /**
     * This method checks expired price confirmation message
     */
    private void checkConfirmationMessage() {
        getActions().check(addExpiredPriceCheckBox);
        LOGGER.info("Confirmation Message Checkbox is Checked");
        clickOnSavePrice();
    }

    /**
     * method to return Feature Name from Add Subscription Plan page
     *
     * @return Feature name
     */
    public String getFeatureName(final int entitlementNumber) {
        final WebElement featureInput =
            driver.findElement(By.id("oneTimeEntitlements[" + entitlementNumber + "].itemName"));
        getActions().waitForElementDisplayed(featureInput);
        return featureInput.getText();
    }

    /**
     * This is a method which will search the feature using feature name in the pop up.
     *
     * @param featureName
     * @return List<String> - Feature Search Results Table Header and Body.
     */
    public List<String> getResultsOnFeatureSearch(final int entitlementNumber, final String featureName) {
        this.entitlementNumber = entitlementNumber;
        Util.scroll(driver, "600", "0");
        selectEntitlement(PelicanConstants.FEATURE_FIELD);

        return searchFeatureUsingPopUp(featureName, entitlementNumber);
    }

    /**
     * This method adds feature, Licensing Model, Core Product and Entitlement to a subscription plan
     *
     * @param feature
     * @param licensingModel
     * @param coreProducts
     * @param entitlementNumber
     */
    public void addOneTimeFeatureEntitlement(final String feature, final String licensingModel,
        final List<String> coreProducts, final int entitlementNumber) {
        this.entitlementNumber = entitlementNumber;
        Util.scroll(driver, "600", "0");
        selectEntitlement(PelicanConstants.FEATURE_FIELD);
        setFeatureAsInput(feature, entitlementNumber);
        selectLicensingModel(licensingModel);
        selectCoreProducts(coreProducts);
    }

    /**
     * This method adds feature to a subscription plan
     *
     * @param feature
     * @param licensingModel
     * @param coreProducts
     * @param entitlementNumber
     */
    public void addFeatureInOneTimeEntitlement(final int entitlementNumber) {
        this.entitlementNumber = entitlementNumber;
        Util.scroll(driver, "600", "0");
        selectEntitlement(PelicanConstants.FEATURE_FIELD);
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
     * This method to set the feature as text input(not lookup)
     *
     * @param feature
     */
    public void setFeatureAsInput(final String feature, final int entitlementNumber) {
        final WebElement featureInput =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].itemId"));
        getActions().setText(featureInput, feature);
        featureInput.sendKeys(Keys.TAB);
        LOGGER.info("Feature name populated is : " + getFeatureName(entitlementNumber));
    }

    /**
     * This method to set the feature from lookup
     *
     * @param feature
     */
    public void setFeatureFromLookUp(final String feature, final int entitlementNumber) {
        final WebElement lookup = driver.findElement(
            By.xpath(".//*[@id='oneTimeEntitlements[" + entitlementNumber + "]-table']//*[@class='trigger']"));
        getActions().click(lookup);
        final WebElement featureInput =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].itemId"));
        getActions().setText(featureInput, feature);
        featureInput.sendKeys(Keys.TAB);
        LOGGER.info("Feature is set to : " + feature);
    }

    /**
     * This method selects the licensing model
     *
     * @param licensingModel
     */
    public void selectLicensingModel(final String licensingModel) {
        if (!StringUtils.isEmpty(licensingModel)) {
            final WebElement licensingModelSelect =
                driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].itemLicensingModelId"));
            getActions().select(licensingModelSelect, licensingModel);
            LOGGER.info("Licensing Model selected : " + licensingModel);
        }
    }

    /**
     * This method selects core products
     *
     * @param coreProducts
     */
    public void selectCoreProducts(final List<String> coreProducts) {
        if (coreProducts != null) {
            final WebElement coreProductsLink =
                driver.findElement(By.id("oneTimeEntitlements[" + entitlementNumber + "].coreProducts"));
            getActions().click(coreProductsLink);
            for (final String coreProduct : coreProducts) {
                getActions().select(coreProductSelect, coreProduct);
                LOGGER.info("Core Product Selected : " + coreProduct);
            }
            getActions().click(saveCoreProductsButton);
            LOGGER.info("Clicked on save core products");
        }
    }

    /**
     * This is a method to search the feature in the pop up and return the feature search results table header and body
     *
     * @param featureName
     * @return
     */
    protected List<String> searchFeatureUsingPopUp(final String featureName, final int entitlementNumber) {
        final WebElement popupSearch = driver.findElement(
            By.xpath(".//*[@id='oneTimeEntitlements[" + entitlementNumber + "]-table']//*[@class='trigger']"));
        getActions().click(popupSearch);
        switchDriverControlToPopUp();
        getActions().setText(featureInputPattern, featureName);
        submit();
        Wait.elementVisibile(driver, featureResultsHeader);
        Wait.elementVisibile(driver, featureResultsBody);
        final String featureResultsHeaderText = featureResultsHeader.getText();
        final String featureResultsBodyText = featureResultsBody.getText();
        final List<String> featureSearchResultsList = new ArrayList<>();
        if (featureResultsHeaderText.equals(PelicanConstants.ZERO_RESULTS)) {
            getActions().click(cancelButtonInPopUp);
        } else {
            getActions().click(itemRadioButton);
            submitButtonOnPopUpGrid();
        }
        switchDriverControlToParentWindow();
        popupSearch.sendKeys(Keys.TAB);
        featureSearchResultsList.add(featureResultsHeaderText);
        featureSearchResultsList.add(featureResultsBodyText);
        return featureSearchResultsList;
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
     * This method checks selected core products
     *
     * @param coreProduct
     */
    public boolean validateCoreProductSelection(final int entitlementNumber, final String featureExtKey) {
        final WebElement coreProductsDisplay =
            driver.findElement(By.id("oneTimeEntitlements[" + entitlementNumber + "].coreProductsDisplay"));
        LOGGER.info("Core Product already Selected : " + coreProductsDisplay.getText());
        if (Wait.textPresentInElement(getDriver(), coreProductsDisplay, featureExtKey)) {
            return true;
        } else {
            LOGGER.info("Core Product External Key didnt match Feature External Key");
            return false;
        }
    }

    /**
     * Method to check whether entitlement row is expanded
     *
     * @param itemId
     * @return
     */
    public boolean isEntitlementRowExpanded(final String itemId) {
        final WebElement row1 = driver.findElement(By.id("row_1_" + itemId));
        final WebElement row2 = driver.findElement(By.id("row_2_" + itemId));
        return row1.isDisplayed() && row2.isDisplayed();
    }

    /**
     * Method to get all one time entitlement ids
     *
     * @return
     */
    public List<String> getOneTimeEntitlementIds(final String planId, final String featureId,
        final EnvironmentVariables environmentVariables) {
        final List<String> entitlementIdsList =
            DbUtils.getAllEntitlementIdsForPlan(planId, featureId, environmentVariables);
        return entitlementIdsList;
    }

    /**
     * Method to click on entitlement row to collapse or expand it.
     *
     * @param itemId
     */
    public void clickOnEntitlementExpandableRowToggle(final String itemId) {
        Util.scroll(driver, "2400", "0");
        final WebElement expandableRowToggle = driver.findElement(By.id("item_" + itemId));
        getActions().click(expandableRowToggle);
        LOGGER.info("Click on entitlement row " + itemId);
    }

    /**
     * This is the method to check or uncheck the assignable checkbox for the entitlements
     *
     * @param assignable
     * @param entitlementNumber
     */
    public Boolean setAssignable(final int entitlementNumber, final boolean assignable) {

        final WebElement assignableCheckbox =
            driver.findElement(By.name("oneTimeEntitlements[" + entitlementNumber + "].assignable"));

        final boolean selected = assignableCheckbox.isSelected();

        try {
            if (selected != assignable) {
                getActions().click(assignableCheckbox);
            }
            LOGGER.info("Assignable checkbox selected : " + assignableCheckbox);

            return true;
        } catch (final ElementNotVisibleException e) {
            return false;
        }

    }

    // set EOS date
    public void setEOSDate(final int entitlement, final String date) {
        LOGGER.info("Set EOS Date to '" + date + "'");
        getActions().setText(driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].eosDate")), date);
    }

    // set EOL Renewal date
    public void setEOLRenewDate(final int entitlement, final String date) {
        LOGGER.info("Set EOLRenew Date to '" + date + "'");
        getActions().setText(driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].eolRenewalDate")),
            date);
    }

    // set EOL ImmediateDate date
    public void setEOLImmediateDate(final int entitlement, final String date) {
        LOGGER.info("Set EOLImmediate Date to '" + date + "'");
        getActions().setText(driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].eolImmediateDate")),
            date);
    }

    // get Assignable value for feature
    public boolean getAssignable(final int entitlement) {
        return driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].assignable")).isSelected();
    }

    // get EOS Renewal date
    public String getEOSDate(final int entitlement) {
        return driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].eosDate")).getAttribute("value");
    }

    // get EOL Renewal date
    public String getEOLRenewDate(final int entitlement) {
        return driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].eolRenewalDate"))
            .getAttribute("value");
    }

    // get EOL Immediate date
    public String getEOLImmediateDate(final int entitlement) {
        return driver.findElement(By.name("oneTimeEntitlements[" + entitlement + "].eolImmediateDate"))
            .getAttribute("value");
    }

    /**
     * This is a method which will set the assignable and remove feature dates to the plan
     */
    public void addAssignableAndRemoveFeatureDates(final boolean assignable, final String eosDate,
        final String eolRenewalDate, final String eolImmediateDate, final int entitlementNumber) {
        setAssignable(entitlementNumber, assignable);
        addRemoveFeatureDates(eosDate, eolRenewalDate, eolImmediateDate, entitlementNumber);
    }

    /**
     * This is a method which will set the assignable and remove feature dates to the plan
     */
    public void addRemoveFeatureDates(final String eosDate, final String eolRenewalDate, final String eolImmediateDate,
        final int entitlementNumber) {

        setEOSDate(entitlementNumber, eosDate);
        setEOLRenewDate(entitlementNumber, eolRenewalDate);
        setEOLImmediateDate(entitlementNumber, eolImmediateDate);
    }

}
