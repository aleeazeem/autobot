package com.autodesk.bsm.pelican.ui.pages.promotions;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.apache.commons.lang.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Page object Pattern represent Add Promotion Page. Access via Promotions | Add
 *
 * @author Vineel
 */

public class AddPromotionPage extends GenericGrid {
    public AddPromotionPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static String BASIC_OFFERING_TABLE_XPATH = "//*[@id='Basic Offerings-section']/div/table";
    private static String SUBSCRIPTION_OFFERS_TABLE_XPATH = "//*[@id='Subscription Offers-section']/div/table";
    private static String DISCOUNT_AMOUNT_DETAILS_TABLE_XPATH =
        "//*[@id='Discount Amount Details-section']/div/div/table";

    // Final Variables that hold xpath value
    private static final String amountInputXpath = ".//*[@id='field-pricelists']//*[@class='input']//*[@class='text']";
    private static final String noneFoundMessageXpath = ".//*[@class='results max row-select-table']/tbody/tr/td";
    private static final String storeRadioButtonXpath = "//form[@id='find-results']//tbody/tr[1]//input[@type='radio']";
    private static final String findResultsId = "find-results";
    private static final String buttonXpath = ".//*[@class='buttons']/span[2]";
    private static final String pricelistFieldXpath = ".//*[@id='field-pricelists']//*[@class='input']";
    private static final String pricelistNamesXpath = ".//*[@class='note']/span[2]";
    private static final String currencyNameXpath = ".//*[@class='note']/span[1]";
    private static final String storeCheckBox = "//form[@id='find-results']//tbody/tr[1]//input[@type='checkbox']";
    private static final String QUANTITY_XPATH = ".//*[@class='name inline']//*[@type='text']";
    private static final String APPLY_DISCOUNT_CHECKBOX_XPATH = ".//*[@class='name inline']//*[@type='checkbox']";

    @FindBy(id = "input-name")
    private WebElement nameInput;

    @FindBy(id = "input-description")
    private WebElement descriptionInput;

    @FindBy(id = "storeWide")
    private WebElement storeWideCheckbox;

    @FindBy(id = "bundled")
    private WebElement bundledCheckbox;

    @FindBy(id = "input-discountCode")
    private WebElement discountCodeInput;

    @FindBy(id = "limitStoreId")
    private WebElement storeInput;

    @FindBy(xpath = ".//*[@id='form-advancedFindForm']/span[1]/button")
    private WebElement findAStoreSearchButton;

    @FindBy(xpath = "(.//*[@class='name inline']//*[@type='text']")
    private WebElement quantityForOfferingInput;

    @FindBy(xpath = "(.//*[@class='name inline']//*[@type='checkbox']")
    private WebElement applyDiscountForOfferingCheckbox;

    @FindBy(id = "storeSelectionButton")
    private WebElement addStoresButton;

    @FindBy(id = "promotionType")
    private WebElement promotionTypeSelect;

    @FindBy(id = "discountType")
    private WebElement discountTypeSelect;

    @FindBy(name = "promoPriceListAmount[0].amount")
    private WebElement amountInput;

    @FindBy(id = "input-discountPercent")
    private WebElement percentageInput;

    @FindBy(id = "supplementType")
    private WebElement supplementTypeSelect;

    @FindBy(id = "externalKeyBO")
    private WebElement basicOfferingsInput;

    @FindBy(id = "billingCycle")
    private WebElement billingCyclesWebElement;

    @FindBy(id = "offeringType")
    private WebElement offeringType;

    @FindBy(id = "find-basicoffering-trigger")
    private WebElement offeringTypeSearchButton;

    @FindBy(id = "keySelectionButtonBO")
    private WebElement basicOfferingAddButton;

    @FindBy(id = "externalKeySO")
    private WebElement subscriptionOffers;

    @FindBy(id = "find-subscriptionoffer-trigger")
    private WebElement subscriptionOffersSearchButtton;

    @FindBy(id = "keySelectionButtonSO")
    private WebElement subscriptionOffersAddButton;

    @FindBy(id = "billingCycle")
    private WebElement numberOfBillingCycles;

    @FindBy(id = "unlimited")
    private WebElement unlimitedCheckBox;

    @FindBy(id = "input-standalone")
    private WebElement standaloneCheckbox;

    @FindBy(id = "input-effectiveDate")
    private WebElement effectiveDateInput;

    @FindBy(id = "input-expirationDate")
    private WebElement expirationDateInput;

    @FindBy(id = "input-maxUses")
    private WebElement maxUsesInput;

    @FindBy(id = "input-maxUsesPerUser")
    private WebElement maxUsesPerUserInput;

    @FindBy(id = "input-timePeriodCount")
    private WebElement timePeriodCount;

    @FindBy(id = "timePeriodType")
    private WebElement timePeriodType;

    @FindBy(id = "find-store-trigger")
    private WebElement storeFinder;

    @FindBy(css = ".error-message")
    private WebElement errorMessage;

    @FindBy(css = ".errors")
    private WebElement allErrorMessages;

    @FindBy(id = "error-message-bo")
    private WebElement offeringsErrorMessage;

    @FindBy(id = "error-message-so")
    private WebElement subscriptionOfferErrorMsg;

    @FindBy(id = "input-supplementItemOfferId")
    private WebElement itemOffer;

    @FindBy(id = "basicoffering-trigger")
    private WebElement searchButton;

    @FindBy(xpath = "//a[text()='[find]'])[1]")
    private WebElement itemOfferSearchButton;

    @FindBy(xpath = ".//*[@type='checkbox']")
    private WebElement selectCheckBox;

    @FindBy(id = "input-activateNow")
    private WebElement activateNowCheck;

    @FindBy(xpath = ".//*[@name='effectiveDateStartHour']")
    private WebElement effectiveDateStartHour;

    @FindBy(xpath = ".//*[@name='effectiveDateStartMinute']")
    private WebElement effectiveDateStartMinute;

    @FindBy(xpath = ".//*[@name='effectiveDateStartSecond']")
    private WebElement effectiveDateStartSecond;

    @FindBy(xpath = ".//*[@name='expirationDateEndHour']")
    private WebElement effectiveExpirationDateEndHour;

    @FindBy(xpath = ".//*[@name='expirationDateEndMinute']")
    private WebElement effectiveExpirationDateEndMinute;

    @FindBy(xpath = ".//*[@name='expirationDateEndSecond']")
    private WebElement effectiveExpirationDateEndSecond;

    @FindBy(id = "error-message-store")
    private WebElement storeErrorMessage;

    @FindBy(xpath = ".//*[@class='error-message']")
    private WebElement errorMessageInAddPromotion;

    @FindBy(xpath = ".//*[@id='field-pricelists']//*[@class='input']")
    private WebElement amountInputFieldForPricelists;

    @FindBy(xpath = ".//*[@class='note']/span[1]")
    private WebElement currencyAmounts;

    @FindBy(xpath = ".//*[@class='note']/span[2]")
    private WebElement priceListNames;

    @FindBy(id = "keySelectionButtonBO")
    private WebElement addButton;

    @FindBy(xpath = ".//*[@id='offeringsListErrors']/span/label")
    private WebElement errorMessageForBasicOfferingInvalidQuantity;

    @FindBy(xpath = ".//*[@id='offersListErrors']/span/label")
    private WebElement errorMessageForSubscriptionOfferingInvalidQuantity;

    @FindBy(id = "error-message-bo")
    private WebElement errorMessageForMoreBasicOfferings;

    @FindBy(id = "error-message-so")
    private WebElement errorMessageForMoreSubscriptionOfferings;

    @FindBy(xpath = "/html/body/div[1]/div[2]/div[2]/form/fieldset[2]/div[3]/div/span[3]")
    private WebElement errorMessageForDiscountAmount;

    @FindBy(xpath = "/html/body/div[1]/div[2]/div[2]/form/fieldset[4]/div[2]/span/label")
    private WebElement errorMessageForMissingOfferings;

    @FindBy(xpath = "/html/body/div[1]/div[2]/div[2]/form/fieldset[5]/div[2]/span")
    private WebElement errorMessageForEffectiveDateMissing;

    @FindBy(xpath = "/html/body/div[1]/div[2]/div[2]/form/fieldset[1]/div/div[1]/span/label")
    private WebElement errorMessageForMissingStore;

    @FindBy(xpath = "/html/body/div[1]/div[2]/div[2]/form/fieldset[5]/div[2]/span")
    private WebElement errorMessageForPastEffectiveDate;

    @FindBy(xpath = "/html/body/div[1]/div[2]/div[2]/form/div[6]/span/label")
    private WebElement errorMessageForDuplicateStorewide;

    @FindBy(xpath = "/html/body/div[2]/div[1]/div[2]/div")
    private WebElement differentOfferTermsWarning;

    @FindBy(xpath = "//dt[contains(text(), 'Billing Cycle')]/../dd")
    private WebElement billingCycleValue;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddPromotionPage.class.getSimpleName());

    // navigate to Add New Promotion
    private void navigateToAddPromotion() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PROMOTION.getForm() + "/addForm";
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
     * Method to add a promotion from add promotion page.
     *
     * @param promotionName
     * @param description
     * @param isStoreWide
     * @param isBundled
     * @param promotionCode
     * @param storeList
     * @param promotionType
     * @param discountType
     * @param cashAmount
     * @param cashAmountIndex
     * @param percentage
     * @param timePeriodCount
     * @param timePeriodType
     * @param offeringsType
     * @param basicOfferingsExternalKeyList
     * @param subscriptionOfferingsExternalKeyList
     * @param billingCycleCount
     * @param isUnlimited
     * @param effectiveDate
     * @param expirationDate
     * @param effectiveDateHour
     * @param effectiveDateMinute
     * @param effectiveDateSeconds
     * @param expirationDateHour
     * @param expirationDateMinute
     * @param expirationDateSeconds
     * @param maxUses
     * @param maxUsesPerUser
     * @param isActivateNow
     * @return PromotionDetailsPage
     */
    public PromotionDetailsPage addPromotion(final String promotionName, final String description,
        final boolean isStoreWide, final boolean isBundled, final String promotionCode, final List<String> storeList,
        final String promotionType, final String discountType, final String cashAmount, final int cashAmountIndex,
        final String percentage, final String timePeriodCount, final String timePeriodType, final String offeringsType,
        final List<String> basicOfferingsExternalKeyList, final List<String> subscriptionOfferingsExternalKeyList,
        final String billingCycleCount, final boolean isUnlimited, final String effectiveDate,
        final String expirationDate, final String effectiveDateHour, final String effectiveDateMinute,
        final String effectiveDateSeconds, final String expirationDateHour, final String expirationDateMinute,
        final String expirationDateSeconds, final String maxUses, final String maxUsesPerUser,
        final boolean isActivateNow) {

        navigateToAddPromotion();
        if (promotionName != null) {
            setName(promotionName);
        }
        if (description != null) {
            setDescription(description);
        }

        setStoreWide(isStoreWide);
        setBundled(isBundled);

        if (promotionCode != null) {
            setPromotionCode(promotionCode);
        }

        if (storeList != null) {
            setStoreId(storeList.toString().replace("[", "").replace("]", ""));
            clickAddStore();
        }

        if (promotionType != null) {
            selectPromotionType(promotionType);
        }
        if (discountType != null) {
            selectDiscountType(discountType);
        }
        if (cashAmount != null) {
            setAmount(cashAmount, cashAmountIndex);
        }
        if (percentage != null) {
            setPercentage(percentage);
        }
        if (timePeriodCount != null) {
            setTimePeriodCount(timePeriodCount);
        }
        if (timePeriodType != null) {
            selectTimePeriodType(timePeriodType);
        }
        if (offeringsType != null) {
            selectOfferingsType(offeringsType);
        }

        if (basicOfferingsExternalKeyList != null) {
            setBasicOfferingExternalKey(
                basicOfferingsExternalKeyList.toString().replace("[", "").replace("]", "").replace(" ", ""));
            clickAddBasicOfferings();
        }

        if (subscriptionOfferingsExternalKeyList != null) {
            setSubscriptionOfferExternalKey(
                subscriptionOfferingsExternalKeyList.toString().replace("[", "").replace("]", "").replace(" ", ""));
            clickAddSubscriptionOffer();
        }

        if (billingCycleCount != null) {
            clickOnAddBillingCycles(billingCycleCount);
        }

        if (isUnlimited) {
            clickOnUnlimitedCheckBox();
        }

        if (effectiveDate != null) {
            setEffectiveDate(effectiveDate);
        }

        if (effectiveDateHour != null) {
            getActions().select(effectiveDateStartHour, effectiveDateHour);
        }
        if (effectiveDateMinute != null) {
            getActions().select(effectiveDateStartMinute, effectiveDateMinute);
        }
        if (effectiveDateSeconds != null) {
            getActions().select(effectiveDateStartSecond, effectiveDateSeconds);
        }

        if (expirationDate != null) {
            setExpirationDate(expirationDate);
        }

        if (expirationDateHour != null) {
            getActions().select(effectiveExpirationDateEndHour, expirationDateHour);
        }
        if (expirationDateMinute != null) {
            getActions().select(effectiveExpirationDateEndMinute, expirationDateMinute);
        }
        if (expirationDateSeconds != null) {
            getActions().select(effectiveExpirationDateEndSecond, expirationDateSeconds);
        }

        if (maxUses != null) {
            setMaxUses(maxUses);
        }

        if (maxUsesPerUser != null) {
            setMaxUsesPerUser(maxUsesPerUser);
        }
        setActivateNow(isActivateNow);
        submit(TimeConstants.ONE_SEC);
        return super.getPage(PromotionDetailsPage.class);
    }

    public String getH3ErrorMessage() {
        navigateToAddPromotion();
        clickOnAddPromotion();
        LOGGER.info("'" + (this.errorMessage.getText()) + "'");
        return (this.errorMessage.getText());
    }

    public String getStoreErrorMessage() {
        return (this.storeErrorMessage.getText());
    }

    public String getAllErrorMessages() {
        LOGGER.info("'" + (this.allErrorMessages.getText()) + "'");
        return (this.allErrorMessages.getText());
    }

    public String getOfferingsErrorMessage() {
        final String offeringsErrorMessage = this.offeringsErrorMessage.getText();
        LOGGER.info("'" + offeringsErrorMessage + "'");
        return offeringsErrorMessage;
    }

    public String getSubscriptionOffersErrorMessage() {
        final String offeringsErrorMessage = this.subscriptionOfferErrorMsg.getText();
        LOGGER.info("'" + offeringsErrorMessage + "'");
        return offeringsErrorMessage;
    }

    public String getDiscountAmountMissingError() {
        return errorMessageForDiscountAmount.getText();
    }

    public String getOfferingsMissingError() {
        return errorMessageForMissingOfferings.getText();
    }

    public String getEffectiveDateRangeMissingError() {
        return errorMessageForEffectiveDateMissing.getText();
    }

    public String getStoreMissingError() {
        return errorMessageForMissingStore.getText();
    }

    public String getPastEffectiveDateError() {
        return errorMessageForPastEffectiveDate.getText();
    }

    public String getDuplicateStorewidePromotionError() {
        return errorMessageForDuplicateStorewide.getText();
    }

    protected void setName(String name) {

        if (name == null) {
            name = "PROMOTION_NAME_" + RandomStringUtils.randomAlphanumeric(8);
        }
        LOGGER.info("Set name to '" + name + "'");
        getActions().setText(nameInput, name);
    }

    protected void setDescription(final String description) {
        LOGGER.info("Set description to '" + description + "'");
        getActions().setText(descriptionInput, description);
    }

    private void setStoreWide(final boolean checkStoreWide) {
        if (checkStoreWide) {
            getActions().check(storeWideCheckbox);
        }
    }

    /**
     * Mrthod to check bundled
     *
     * @param checkBundled
     */
    private void setBundled(final boolean checkBundled) {
        if (checkBundled) {
            getActions().check(bundledCheckbox);
        }
    }

    private void setPromotionCode(final String promotionCode) {
        LOGGER.info("Set promotion code  to '" + promotionCode + "'");
        getActions().setText(discountCodeInput, promotionCode);
    }

    private void setStoreId(final String storeId) {
        LOGGER.info("Set store id to '" + storeId + "'");
        getActions().setText(storeInput, storeId);
    }

    private void selectSupplementType(final String supplementType) {
        LOGGER.info("Select supplement type '" + supplementType + "'");
        getActions().select(supplementTypeSelect, supplementType);
    }

    private void setTimePeriodCount(final String timePeriodCount) {
        LOGGER.info("Set the time period count '" + timePeriodCount + "'");
        getActions().setText(this.timePeriodCount, timePeriodCount);
    }

    private void selectTimePeriodType(final String timePeriodType) {
        LOGGER.info("Select the time period type '" + timePeriodType + "'");
        getActions().select(this.timePeriodType, timePeriodType);
    }

    public void clickSubScriptionOfferSearch() {
        LOGGER.info("click subscription offer search button");
        getActions().click(subscriptionOffersSearchButtton);
    }

    private void setSubscriptionOfferExternalKey(final String subscriptionOfferingsExternalKey) {
        LOGGER.info("Set subscription offers to '" + subscriptionOfferingsExternalKey + "'");
        getActions().setText(this.subscriptionOffers, subscriptionOfferingsExternalKey);
    }

    private void setBasicOfferingExternalKey(final String basicOfferingsExternalKey) {
        LOGGER.info("Set subscription offers to '" + basicOfferingsExternalKey + "'");
        getActions().setText(this.basicOfferingsInput, basicOfferingsExternalKey);
    }

    // select promotion type
    private void selectPromotionType(final String promotionType) {
        LOGGER.info("Select promotion type to '" + promotionType + "'");
        getActions().select(promotionTypeSelect, promotionType);
    }

    // select offerings type.
    private void selectOfferingsType(final String offeringsType) {
        LOGGER.info("Select offerings type to '" + offeringsType + "'");
        getActions().selectWithElementText(offeringType, offeringsType);
    }

    // select discount type
    public void selectDiscountType(final String discountType) {
        LOGGER.info("Select Discount type to '" + discountType + "'");
        getActions().select(discountTypeSelect, discountType);
    }

    private void clickAddSubscriptionOffer() {
        LOGGER.info("Click on 'Add Subscription Offer'");
        getActions().click(subscriptionOffersAddButton);
    }

    private void clickAddBasicOfferings() {
        LOGGER.info("Click on 'Add Basic Offerings'");
        getActions().click(basicOfferingAddButton);
    }

    public void setBasicOfferings(final String externalKey) {
        LOGGER.info("Set 'Basic Offerings'");
        getActions().setText(basicOfferingsInput, externalKey);
    }

    private void clickOnUnlimitedCheckBox() {
        LOGGER.info("Click on 'Unlimite' checkbox");
        unlimitedCheckBox.click();
    }

    private void setPercentage(final String percentage) {
        LOGGER.info("Set amount to '" + percentage + "'");
        getActions().setText(this.percentageInput, percentage);
    }

    // set effective date
    private void setEffectiveDate(final String effectiveDate) {
        LOGGER.info("Set effectiveDate to '" + effectiveDate + "'");
        getActions().setText(effectiveDateInput, effectiveDate);
    }

    // set expiration date
    private void setExpirationDate(final String expirationDate) {
        LOGGER.info("Set expirationDate to  '" + expirationDate + "'");
        getActions().setText(expirationDateInput, expirationDate);
    }

    // set maximum number of uses
    private void setMaxUses(final String maxUses) {
        LOGGER.info("Set maxUses to '" + maxUses + "'");
        getActions().setText(maxUsesInput, maxUses);
    }

    // set maximum uses per user
    private void setMaxUsesPerUser(final String maxUsesPerUser) {
        LOGGER.info("Set maxUsesPerUser to '" + maxUsesPerUser + "'");
        getActions().setText(maxUsesPerUserInput, maxUsesPerUser);
    }

    // set activate the promotion now checkbox
    public void setActivateNow(final boolean activateNow) {
        Util.waitInSeconds(1);
        if (activateNow) {
            getActions().check(activateNowCheck);
        }
    }

    public PromotionDetailsPage clickOnAddPromotion() {
        LOGGER.info("Click on add promotion button");
        submit(TimeConstants.ONE_SEC);
        return super.getPage(PromotionDetailsPage.class);
    }

    /**
     * Method to get details from the added promotion details.
     *
     * @return promotion
     */
    private Promotion getDetails(final boolean offeringType) {
        final Promotion promotion = new Promotion();
        // Promotion details after creation.
        final GenericDetails details = super.getPage(GenericDetails.class);
        promotion.setId(details.getValueByField("ID"));
        promotion.setApplicationFamily(details.getValueByField("Application Family"));
        promotion.setApplication(details.getValueByField("Application"));
        promotion.setName(details.getValueByField("Name"));
        promotion.setDescription(details.getValueByField("Description"));
        promotion.setPromotionCode(details.getValueByField("Promotion Code"));
        if (details.getValueByField("Limited To Store").isEmpty()) {
            promotion.setStoreId(details.getValueByField("Limited To Stores"));
        } else {
            promotion.setStoreId(details.getValueByField("Limited To Store"));
        }
        promotion.setPromotionType(details.getValueByField("Type"));
        if ("YES".equalsIgnoreCase(details.getValueByField("Store Wide"))) {
            promotion.setStoreWide(true);
        } else {
            promotion.setStoreWide(false);
        }
        // Set promotion type as supplement.
        if (promotion.getPromotionType().equals("Supplement")) {
            promotion.setSupplementType(details.getValueByField("Supplement Type"));
            promotion.setTimePeriodCount(details.getValueByField("Time Period Count"));
            promotion.setTimePeriodType(details.getValueByField("Time Period Type"));
        } else {
            promotion.setDiscountType(details.getValueByField("Discount Type"));
            // Sets discount type as Percentage.
            if (promotion.getDiscountType().equals("Percentage")) {
                promotion.setPercentage(details.getValueByField("Percentage"));
            }
        }

        // Set the basic offerings or subscription offers external key.
        if (offeringType) {
            promotion.setOfferingsExternalKey(new String[] { details.getValueByField("Basic Offerings") });
        } else {
            promotion.setOfferingsExternalKey(new String[] { details.getValueByField("Subscription Offers") });
        }
        promotion.setStandalonePromotion(details.getValueByField("Standalone"));
        promotion.setEffectiveDate(details.getValueByField("Effective Date Range"));
        promotion.setMaxUses(details.getValueByField("Maximum Number of Uses"));
        promotion.setMaxUsesPerUser(details.getValueByField("Maximum Uses per User"));
        promotion.setStatus(Status.valueOf(details.getValueByField("State")));
        return promotion;
    }

    /**
     * Method to add the promotion.
     *
     * @param promotion (Promotion object)
     * @param amountIndex (Cash Discount price list index)
     * @param isSubmit (click on submit based on variable value)
     * @param isReturn (Returns promotion details based on value)
     * @param offeringType (Set offering type external key based on value)
     * @return promotion (returns added promotion details)
     */
    public Promotion add(final Promotion promotion, final int amountIndex, final boolean isSubmit,
        final boolean isReturn, final boolean offeringType, final boolean validStore) {
        LOGGER.info("Add Promotion");
        navigateToAddPromotion();
        setName(promotion.getName());
        setDescription(promotion.getDescription());
        setStoreWide(promotion.getStoreWide());
        setPromotionCode(promotion.getPromotionCode());
        if (promotion.getStoreId().startsWith("StoreSearchWithName")) {
            final Promotion newPromotion = addStoreUsingStoreFinderName(promotion, promotion.getStoreId());
            promotion.setStoreErrorMessage(newPromotion.getStoreErrorMessage());
        } else {
            addStore(promotion.getStoreId());
        }
        if (("None Found").equalsIgnoreCase(promotion.getStoreErrorMessage())) {
            return promotion;
        } else {
            if (validStore) {
                final int numberOfPriceLists = numberOfPriceListsForAPromotion();
                promotion.setPriceListSize(numberOfPriceLists);
                final List<String> priceListNamesList = getPriceListNamesForAPromotion();
                final List<String> currencyNamesList = getCurrencyNamesForAPromotion();
                promotion.setPriceListNames(priceListNamesList);
                promotion.setCurrencyNames(currencyNamesList);
                selectPromotionType(promotion.getPromotionType());
                // Select Promotion type as Supplement or Discount.
                if (promotion.getPromotionType().equals("Supplement")) {
                    selectSupplementType(promotion.getSupplementType());
                    setTimePeriodCount(promotion.getTimePeriodCount());
                    selectTimePeriodType(promotion.getTimePeriodType());
                } else {
                    selectDiscountType(promotion.getDiscountType());
                    // Select discount type as Cash Amount or Percentage.
                    if (promotion.getDiscountType().equals("Cash Amount")) {
                        // Set the cash amount based on currency.
                        if ("setAmountForAllPricelists".equalsIgnoreCase(promotion.getAmountMessage())) {
                            setAmountForAllPricelistsAndCurrencies(promotion.getAmount());
                        } else if ("setAmountForFewPricelists".equalsIgnoreCase(promotion.getAmountMessage())) {
                            setAmountForAllExceptLastPricelistsAndCurrencies(promotion.getAmount());
                        } else if ("DontSetAmountToAnyField".equalsIgnoreCase(promotion.getAmountMessage())) {
                            setAmountForNoneOfThePricelistsAndCurrencies(promotion.getAmount());
                        } else {
                            setAmount(promotion.getAmount(), amountIndex);
                        }
                    } else {
                        setPercentage(promotion.getPercentage());
                    }
                }
                Util.scroll(getDriver(), "500", "0");
                // Selects Offering type as Basic Offerings or Subscription
                // offers
                selectOfferingsType(promotion.getOfferingType());
                if (promotion.getOfferingType().equals("Basic Offerings")) {
                    // Set the basic offerings external key.
                    addBasicOfferingUsingExternalKey(promotion.getOfferingsExternalKey());
                } else {
                    // Set the subscription offers external key.
                    addSubscriptionOfferingUsingExternalKey(promotion.getOfferingsExternalKey());
                }
                setEffectiveDate(promotion.getEffectiveDate());
                setExpirationDate(promotion.getExpirationDate());
                setMaxUses(promotion.getMaxUses());
                setMaxUsesPerUser(promotion.getMaxUsesPerUser());
                setActivateNow(promotion.getActivatePromotion());
                // Click on Submit button.
                if (isSubmit) {
                    submit(TimeConstants.ONE_SEC);
                    try {
                        if (driver.getTitle().equals("Pelican - Add Promotion")) {
                            final AddPromotionPage page = getPage(AddPromotionPage.class);
                            final List<WebElement> elements =
                                getDriver().findElements(By.xpath(".//*[@class='error-message']"));
                            for (final WebElement element : elements) {
                                LOGGER.info("Error message: " + element.getText());
                            }
                            LOGGER.info("All error messages: " + page.getAllErrorMessages());
                        }
                    } catch (final Exception e) {
                        e.getMessage();
                    }
                }
                // Returns the added promotion details.
                if (isReturn) {
                    return getDetails(offeringType);
                }
                if (("return-promotion").equalsIgnoreCase(promotion.getPriceListMessage())) {
                    return promotion;
                }
            }

            return null;
        }
    }

    private void addSubscriptionOfferingUsingExternalKey(final String[] subscriptionOfferingExternalKey) {
        final List<String> subscriptionOfferingExternalKeyList = Arrays.asList(subscriptionOfferingExternalKey);
        final String offeringExternalKey =
            subscriptionOfferingExternalKeyList.toString().replace("[", "").replace("]", "");
        setSubscriptionOfferExternalKey(offeringExternalKey);
        /**
         * Pass all subscription offerings external keys as a single string in the subscription offering input field and
         * click on add for n times This logic is built according to the behaviour of promotion n - Number of
         * subscription offerings added to the promotion
         */
        for (int i = 1; i <= subscriptionOfferingExternalKeyList.size(); i++) {
            Util.waitInSeconds(TimeConstants.TWO_SEC);
            clickAddSubscriptionOffer();
        }
    }

    private void addBasicOfferingUsingExternalKey(final String[] basicOfferingExternalKey) {
        final List<String> basicOfferingExternalKeyList = Arrays.asList(basicOfferingExternalKey);
        final String offeringExternalKey = basicOfferingExternalKeyList.toString().replace("[", "").replace("]", "");
        setBasicOfferingExternalKey(offeringExternalKey);
        /**
         * Pass all basic offerings external keys as a single string in the basic offering input field and click on add
         * for n times This logic is built according to the behaviour of promotion n - Number of basic offerings added
         * to the promotion
         */
        for (int i = 1; i <= basicOfferingExternalKeyList.size(); i++) {
            clickAddBasicOfferings();
        }
    }

    private void setAmount(final String amount, final int index) {
        final String name = "promoPriceListAmount[" + index + "].amount";
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final WebElement element = getDriver().findElement(By.name(name));
        LOGGER.info("Element found successfully");
        LOGGER.info("Set amount to '" + amount + "'");
        getActions().setText(element, amount);
    }

    private void setAmountForAllPricelistsAndCurrencies(final String amount) {
        final List<WebElement> elementsList = getDriver().findElements(By.xpath(amountInputXpath));
        for (final WebElement element : elementsList) {
            LOGGER.info("Set amount to '" + amount + "'");
            getActions().setText(element, amount);
        }
    }

    private void setAmountForAllExceptLastPricelistsAndCurrencies(final String amount) {
        final List<WebElement> elementsList = getDriver().findElements(By.xpath(amountInputXpath));
        elementsList.remove(elementsList.size() - 1);
        for (final WebElement element : elementsList) {
            LOGGER.info("Set amount to '" + amount + "'");
            getActions().setText(element, amount);
        }
    }

    private void setAmountForNoneOfThePricelistsAndCurrencies(final String amount) {
        final List<WebElement> elementsList = getDriver().findElements(By.xpath(amountInputXpath));
    }

    private void addStoreUsingFinder(final String storeName) {
        final WebDriver driver = getDriver();
        final String mainWindow = driver.getWindowHandle();
        storeFinder.click();
        for (final String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
            if (driver.getTitle().equalsIgnoreCase("Pelican - Find Stores")) { // NOSONAR
                // Clicking on find button without filling any details so that
                // it gets all active store
                getActions().setText(nameInput, storeName);
                submit();
                driver.findElement(By.xpath(storeRadioButtonXpath)).click();
                driver.findElement(By.id(findResultsId)).submit();
            }

        }
        driver.switchTo().window(mainWindow);
    }

    private Promotion addStoreUsingStoreFinderName(final Promotion promotion, final String storeName) {
        storeFinder.click();
        final String storeNames[] = storeName.split("StoreSearchWithName");
        final String name = storeNames[1];
        final String parentHandle = getDriver().getWindowHandle();
        final int numberOfHandles = getDriver().getWindowHandles().size();
        String handle;
        if (numberOfHandles == 2) {
            handle = getDriver().getWindowHandles().toArray()[1].toString();

        } else {
            handle = getDriver().getWindowHandles().toArray()[0].toString();
        }
        getDriver().switchTo().window(handle);
        getActions().setText(nameInput, name);
        findAStoreSearchButton.click();
        final String message = getDriver().findElement(By.xpath(noneFoundMessageXpath)).getText();
        promotion.setStoreErrorMessage(message);
        getDriver().findElement(By.xpath(buttonXpath)).click();
        getDriver().switchTo().window(parentHandle);

        return promotion;

    }

    private void addStore(final String storeName) {
        if (storeName == null) {
            addStoreUsingFinder(storeName);
        } else {
            setStoreId(storeName);
        }
        clickAddStore();
    }

    private int numberOfPriceListsForAPromotion() {
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final List<WebElement> priceListElementsList = getDriver().findElements(By.xpath(pricelistFieldXpath));

        return priceListElementsList.size();
    }

    private List<String> getPriceListNamesForAPromotion() {
        final List<WebElement> priceListElementsList = getDriver().findElements(By.xpath(pricelistNamesXpath));
        final List<String> priceListNamesList = new ArrayList<>();
        for (final WebElement element : priceListElementsList) {
            final String priceListName = element.getText();
            priceListNamesList.add(priceListName);
        }

        return priceListNamesList;
    }

    private List<String> getCurrencyNamesForAPromotion() {
        final List<WebElement> currenciesList = getDriver().findElements(By.xpath(currencyNameXpath));
        final List<String> currencyNamesList = new ArrayList<>();
        for (final WebElement element : currenciesList) {
            final String currencyName = element.getText();
            currencyNamesList.add(currencyName);
        }

        return currencyNamesList;
    }

    public void addBaiscOfferings() {
        addBasicOfferingsUsingFinder();
        clickAddOfferings();
    }

    private void addBasicOfferingsUsingFinder() {
        final WebDriver driver = getDriver();
        final String mainWindow = driver.getWindowHandle();
        offeringTypeSearchButton.click();
        for (final String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
            if (driver.getTitle().equalsIgnoreCase("Pelican - Find Subscription Offers")) { // NOSONAR
                // Clicking on find button without filling any details so that
                // it gets all active Offerings.
                submit();
                driver.findElement(By.xpath(storeCheckBox)).click();
                driver.findElement(By.xpath("find-results")).submit();
            }
        }
        driver.switchTo().window(mainWindow);
    }

    private void clickAddStore() {
        this.addStoresButton.click();
    }

    private void clickAddOfferings() {
        this.basicOfferingAddButton.click();
    }

    public Promotion setAllFieldsOfPromotion(final Promotion newPromotion) {

        final GenericGrid grid = super.getPage(GenericGrid.class);
        final GenericDetails genericDetails = super.getPage(GenericDetails.class);
        final Promotion promotion = new Promotion();

        promotion.setId(genericDetails.getValueByField("ID"));
        promotion.setName(genericDetails.getValueByField("Name"));
        promotion.setPromotionCode((genericDetails.getValueByField("Promotion Code")));
        promotion.setState((genericDetails.getValueByField("State")));
        if ("YES".equalsIgnoreCase(genericDetails.getValueByField("Store Wide"))) {
            promotion.setStoreWide(true);
        } else {
            promotion.setStoreWide(false);
        }

        if ("YES".equalsIgnoreCase(genericDetails.getValueByField("Bundled"))) {
            promotion.setBundled(true);
        } else {
            promotion.setBundled(false);
        }

        if ((newPromotion.getStoreId() != null) && (!(newPromotion.getStoreId().isEmpty()))) {
            if (!genericDetails.getValueByField("Limited To Store").isEmpty()) {
                promotion.setStoreId(genericDetails.getValueByField("Limited To Store"));
            } else {
                promotion.setStoreId(genericDetails.getValueByField("Limited To Stores"));
            }
            promotion.setDiscountType(genericDetails.getValueByField("Type"));
            promotion.setPromotionType(genericDetails.getValueByField("Discount Type"));
            promotion.setBasicOfferingNameList(getBasicOfferingDetails("Offering Name"));
            promotion.setSubscriptionOfferNameList(getSubscriptionOffersDetails("Offer Name"));
            if (newPromotion.getCashAmount() != null) {
                if (!driver.getCurrentUrl().contains("add")) {
                    if (promotion.getSubscriptionOfferNameList().size() > 0
                        && promotion.getBasicOfferingNameList().size() > 0) {
                        promotion.setCashAmount(getDiscountAmountDetails("Amount").get(0).substring(0, 6));
                    } else {
                        promotion.setCashAmount(getDiscountAmountDetails("Amount").get(0).substring(0, 6));
                    }
                }
            } else if (newPromotion.getPercentageAmount() != null) {
                promotion.setPercentageAmount(genericDetails.getValueByField("Percentage"));
            } else {
                promotion.setSupplementType(genericDetails.getValueByField("Supplement Type"));
            }
            promotion.setState(genericDetails.getValueByField("State"));
            promotion.setMaxUses(genericDetails.getValueByField("Maximum Number of Uses"));
            promotion.setMaxUsesPerUser(genericDetails.getValueByField("Maximum Uses per User"));
            promotion.setEffectiveDate(genericDetails.getValueByField("Effective Date Range"));
            promotion.setNumberOfBillingCycles(getNumberOfBillingCycle());
            promotion.setTimePeriodCount(genericDetails.getValueByField("Time Period Count"));
            promotion.setTimePeriodType(genericDetails.getValueByField("Time Period Type"));
        }
        promotion.setStoreErrorMessage(newPromotion.getStoreErrorMessage());
        promotion.setWindowTitle(newPromotion.getWindowTitle());
        return promotion;
    }

    /**
     * Method to get the billing cycle value
     *
     * @return value of billing cycle
     */
    private String getNumberOfBillingCycle() {
        if (isElementPresent(billingCycleValue)) {
            return billingCycleValue.getText().trim();
        }
        return null;
    }

    public String getStoreWideStatus() {
        navigateToAddPromotion();
        setName(null);
        selectStoreWideCheckbox();
        clickOnAddPromotion();
        final GenericDetails genericDetailsPage = super.getPage(GenericDetails.class);
        return genericDetailsPage.getValueByField("Store Wide");
    }

    public void addStore() {
        navigateToAddPromotion();
        setName(null);
        selectStoreWideCheckbox();
        selectStore();
        clickOnAddPromotion();
    }

    private void selectStoreWideCheckbox() {
        storeWideCheckbox.click();
    }

    private void selectStore() {
        findAStoreSearchButton.click();
        final String parentWindowHandle = getDriver().getWindowHandle();
        final String handle = getDriver().getWindowHandles().toArray()[1].toString();
        getDriver().switchTo().window(handle);
        clickAddStore();
    }

    /*
     * @Return Value of the field displayed in the add Promotion page
     */
    public String getValueByFieldInPromotion(final String field) {
        final String selector =
            ".//*[@class='inner-inner']//dt[contains(text(),'" + field + ":')]/following-sibling::dd[1]";
        final WebElement cell = getDriver().findElement(By.xpath(selector));
        return cell.getText();
    }

    public Promotion addPromotion(final Promotion promotion) {
        navigateToAddPromotion();
        final Promotion filledFieldsOfPromotion = fillFieldsInAddPromotion(promotion);
        Promotion newPromotion;

        if (promotion.getStoreId() == null) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();
            newPromotion = setAllFieldsOfPromotion(newPromotion);
        } else if (promotion.getStoreId().isEmpty()) {
            newPromotion = filledFieldsOfPromotion;
        } else if (("Invalid-Quantity").equalsIgnoreCase(promotion.getErrorMessageForBasicOfferingInvalidQuantity())
            && ("Invalid-Quantity")
                .equalsIgnoreCase(promotion.getErrorMessageForSubscriptionOfferingInvalidQuantity())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion
                .setErrorMessageForBasicOfferingInvalidQuantity(errorMessageForBasicOfferingInvalidQuantity.getText());
            newPromotion.setErrorMessageForSubscriptionOfferingInvalidQuantity(
                errorMessageForSubscriptionOfferingInvalidQuantity.getText());
        } else if (("Invalid-Quantity").equalsIgnoreCase(promotion.getErrorMessageForBasicOfferingInvalidQuantity())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion
                .setErrorMessageForBasicOfferingInvalidQuantity(errorMessageForBasicOfferingInvalidQuantity.getText());
        } else if (("Invalid-Quantity")
            .equalsIgnoreCase(promotion.getErrorMessageForSubscriptionOfferingInvalidQuantity())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setErrorMessageForSubscriptionOfferingInvalidQuantity(
                errorMessageForSubscriptionOfferingInvalidQuantity.getText());
        } else if (("more-offerings").equalsIgnoreCase(promotion.getErrorMessageForMoreBasicOfferings())) {
            newPromotion = filledFieldsOfPromotion;

            newPromotion.setErrorMessageForMoreBasicOfferings(errorMessageForMoreBasicOfferings.getText());
        } else if (("more-offerings").equalsIgnoreCase(promotion.getErrorMessageForMoreSubscriptionOfferings())) {
            newPromotion = filledFieldsOfPromotion;

            newPromotion
                .setErrorMessageForMoreSubscriptionOfferings(errorMessageForMoreSubscriptionOfferings.getText());
        } else if ("duplicate promocode".equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setPromotionCodeErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("not-all-pricelists").equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setPriceListErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("no-discount-entered").equalsIgnoreCase(promotion.getDiscountErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setDiscountErrorMessage(errorMessage.getText());
        } else if ((!(promotion.getStoreId().isEmpty())) && !((promotion.getPriceListExternalKey().isEmpty()))
            && !(("Emptypricelist".equalsIgnoreCase(promotion.getPriceListExternalKey())))
            && !("no-offering".equalsIgnoreCase(promotion.getPriceListExternalKey()))
            && !("two-store-wide".equalsIgnoreCase(promotion.getStoreWideErrorMessage()))
            && !("Time-Mismatch".equalsIgnoreCase(promotion.getStoreWideErrorMessage()))) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();
            newPromotion = setAllFieldsOfPromotion(newPromotion);
        } else if ((promotion.getPercentageAmount() == null || promotion.getPercentageAmount().isEmpty())
            && !(promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering"))
            && !("two-store-wide".equalsIgnoreCase(promotion.getStoreWideErrorMessage()))) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();
            newPromotion.setDiscountErrorMessage(errorMessageInAddPromotion.getText());
        } else if (promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist")) {
            newPromotion = filledFieldsOfPromotion;
            newPromotion.setPriceListErrorMessage(errorMessageInAddPromotion.getText());
        } else if (promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering")) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setSubscriptionOfferErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("two-store-wide").equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setStoreWideErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("Time-Mismatch").equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnAddPromotion();

            newPromotion.setTimeMismatchError(errorMessageInAddPromotion.getText());
        } else {
            clickOnAddPromotion();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
            newPromotion = setAllFieldsOfPromotion(filledFieldsOfPromotion);
        }

        return newPromotion;
    }

    /*
     * Fill all the fields in the Promotion add page
     */
    private Promotion fillFieldsInAddPromotion(final Promotion promotion) {
        getActions().setText(nameInput, promotion.getName());
        if (promotion.getPromotionCode() != null) {
            getActions().setText(discountCodeInput, promotion.getPromotionCode());
        }
        if (promotion.getStoreWide()) {
            storeWideCheckbox.click();
        }
        if (promotion.isBundled()) {
            bundledCheckbox.click();
        }
        if (promotion.getStoreId() != null) {

            if (!(promotion.getStoreId().isEmpty())) {
                LOGGER.info("Adding a store");
                getActions().setText(storeInput, promotion.getStoreId());
                clickAddStore();
            } else if (promotion.getStoreId().isEmpty()) {
                LOGGER.info("Cannot add an empty store");
                getActions().setText(storeInput, promotion.getStoreId());
                clickAddStore();

                promotion.setStoreErrorMessage(storeErrorMessage.getText());
            }

            if (promotion.getDiscountType() != null) {

                if (!(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))) {
                    final Select promotionTypeSelect = new Select(getDriver().findElement(By.id("promotionType")));
                    promotionTypeSelect.selectByVisibleText(promotion.getDiscountType());

                }
                if (promotion.getPromotionType() != null && !promotion.getDiscountType().equalsIgnoreCase("Supplement")
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))) {
                    final Select discountTypeSelect = new Select(getDriver().findElement(By.id("discountType")));
                    discountTypeSelect.selectByVisibleText(promotion.getPromotionType());
                    if (promotion.getPromotionType().equalsIgnoreCase("Cash Amount")) {
                        if (("all-fields").equalsIgnoreCase(promotion.getAmountInputMessage())) {
                            fillAllAmountInputFields(promotion.getCashAmount());
                        } else {
                            getActions().setText(amountInput, promotion.getCashAmount());

                        }
                    } else if (promotion.getPromotionType().equalsIgnoreCase("Percentage")) {
                        getActions().setText(percentageInput, promotion.getPercentageAmount());

                    }
                }

                if (promotion.getDiscountType().equalsIgnoreCase("Supplement")
                    && promotion.getSupplementType().equalsIgnoreCase("Time") && !(promotion.isBundled())) {
                    final Select supplementTypeSelect = new Select(getDriver().findElement(By.id("supplementType")));
                    supplementTypeSelect.selectByVisibleText(promotion.getSupplementType());

                    getActions().setText(timePeriodCount, promotion.getTimePeriodCount());

                    final Select timePeriodTypeSelect = new Select(getDriver().findElement(By.id("timePeriodType")));
                    timePeriodTypeSelect.selectByVisibleText(promotion.getTimePeriodType());
                }
                Util.scroll(getDriver(), "500", "0");
                if (promotion.getBasicOfferings() != null
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering"))) {
                    if ((!promotion.isBundled())) {
                        final String basicOffering = promotion.getBasicOfferings();
                        getActions().selectWithElementText(offeringType, basicOffering);
                    }
                    if ((promotion.getStoreId() == null || promotion.getStoreId().isEmpty())) {
                        final String offeringExternalKey =
                            promotion.getBasicOfferingsExternalKey().toString().replace("[", "").replace("]", "");
                        getActions().setText(basicOfferingsInput, offeringExternalKey);
                        for (int i = 0; i < promotion.getBasicOfferingsExternalKey().size(); i++) {
                            clickOnAddButton();
                            Util.waitInSeconds(TimeConstants.ONE_SEC);
                            if ((promotion.isBundled()) && promotion.getQuantityOfBasicOfferingsList() != null) {
                                final List<WebElement> quantityElementList =
                                    getDriver().findElements(By.xpath(QUANTITY_XPATH));
                                getActions().setText(quantityElementList.get(i),
                                    String.valueOf(promotion.getQuantityOfBasicOfferingsList().get(i - 1)));
                                quantityElementList.clear();
                            }
                            if ((promotion.isBundled() && promotion.getApplyDiscountForBasicOfferingsList() != null)) {
                                final List<WebElement> applyDiscountElementList =
                                    getDriver().findElements(By.xpath(APPLY_DISCOUNT_CHECKBOX_XPATH));
                                if (!promotion.getApplyDiscountForBasicOfferingsList().get(i)) {
                                    applyDiscountElementList.get(i).click();
                                }
                                applyDiscountElementList.clear();
                            }
                        }
                    }
                    if (promotion.getStoreId() != null && !(promotion.getStoreId().isEmpty())
                        && !(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))
                        && !(promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering"))
                        && (promotion.getPriceListExternalKey().equalsIgnoreCase("select-pricelist"))) {
                        final String offeringExternalKey =
                            promotion.getBasicOfferingsExternalKey().toString().replace("[", "").replace("]", "");
                        getActions().setText(basicOfferingsInput, offeringExternalKey);
                        for (int i = 0; i < promotion.getBasicOfferingsExternalKey().size(); i++) {
                            clickOnAddButton();
                            Util.waitInSeconds(TimeConstants.ONE_SEC);
                            if ((promotion.isBundled() && promotion.getQuantityOfBasicOfferingsList() != null)) {
                                final List<WebElement> quantityElementList =
                                    getDriver().findElements(By.xpath(QUANTITY_XPATH));
                                getActions().setText(quantityElementList.get(i),
                                    String.valueOf(promotion.getQuantityOfBasicOfferingsList().get(i)));
                                quantityElementList.clear();
                            }

                            if ((promotion.isBundled() && promotion.getApplyDiscountForBasicOfferingsList() != null)) {
                                final List<WebElement> applyDiscountElementList =
                                    getDriver().findElements(By.xpath(APPLY_DISCOUNT_CHECKBOX_XPATH));
                                if (!promotion.getApplyDiscountForBasicOfferingsList().get(i)) {
                                    applyDiscountElementList.get(i).click();
                                }
                                applyDiscountElementList.clear();
                            }
                        }

                    }
                }

                if (promotion.getSubscriptionOfferings() != null
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering"))) {
                    if ((!promotion.isBundled())) {
                        final String subscriptionOffering = promotion.getSubscriptionOfferings();
                        getActions().selectWithElementText(offeringType, subscriptionOffering);
                    }
                    final String offeringExternalKey =
                        promotion.getSubscriptionOfferingsExternalKey().toString().replace("[", "").replace("]", "");
                    getActions().setText(subscriptionOffers, offeringExternalKey);
                    for (int i = 0; i < promotion.getSubscriptionOfferingsExternalKey().size(); i++) {
                        clickAddSubscriptionOffer();
                        Util.waitInSeconds(TimeConstants.ONE_SEC);
                        if ((promotion.isBundled()) && promotion.getQuantityOfSubscriptionOfferingsList() != null) {
                            final List<WebElement> quantityElementList =
                                getDriver().findElements(By.xpath(QUANTITY_XPATH));
                            getActions().setText(
                                quantityElementList.get((promotion.getSubscriptionOfferingsExternalKey().size()) + (i)),
                                String.valueOf(promotion.getQuantityOfSubscriptionOfferingsList().get(i)));
                            quantityElementList.clear();
                        }

                        if ((promotion.isBundled()
                            && promotion.getApplyDiscountForSubscriptionOfferingsList() != null)) {
                            final List<WebElement> applyDiscountElementList =
                                getDriver().findElements(By.xpath(APPLY_DISCOUNT_CHECKBOX_XPATH));
                            if (!promotion.getApplyDiscountForSubscriptionOfferingsList().get(i)) {
                                int j = i;
                                if (promotion.getBasicOfferingsExternalKey() != null) {
                                    j += promotion.getBasicOfferingsExternalKey().size();
                                }
                                applyDiscountElementList.get(j).click();
                            }
                            applyDiscountElementList.clear();
                        }
                    }

                    if (!promotion.getDiscountType().equalsIgnoreCase("Supplement") && !(promotion.isBundled())) {
                        if (promotion.getNumberOfBillingCycles().equalsIgnoreCase("unlimited")) {
                            clickOnUnlimitedCheckBox();
                        } else {
                            clickOnAddBillingCycles(promotion.getNumberOfBillingCycles());
                        }
                    }

                }
            }

            if (promotion.getEffectiveDate() != null && !(promotion.getStoreId().isEmpty())
                && !(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))) {
                getActions().setText(effectiveDateInput, promotion.getEffectiveDate());
                getActions().setText(expirationDateInput, promotion.getExpirationDate());
                if (promotion.getTimeInHours() != null) {
                    getActions().select(effectiveDateStartHour, promotion.getTimeInHours());
                }
                if (promotion.getTimeInMinutes() != null) {
                    getActions().select(effectiveDateStartMinute, promotion.getTimeInMinutes());
                }
                if (promotion.getTimeInSeconds() != null) {
                    getActions().select(effectiveDateStartSecond, promotion.getTimeInSeconds());
                }
                if (promotion.getExpirationTimeInHours() != null) {
                    getActions().select(effectiveExpirationDateEndHour, promotion.getExpirationTimeInHours());
                }
                if (promotion.getExpirationTimeInMinutes() != null) {
                    getActions().select(effectiveExpirationDateEndMinute, promotion.getExpirationTimeInMinutes());
                }
                if (promotion.getExpirationTimeInSeconds() != null) {
                    getActions().select(effectiveExpirationDateEndSecond, promotion.getExpirationTimeInSeconds());
                }
            }
        }
        getActions().setText(maxUsesInput, promotion.getMaxUses());
        getActions().setText(maxUsesPerUserInput, promotion.getMaxUsesPerUser());

        if ((Status.ACTIVE).equals(promotion.getActivateStatus())) {
            setActivateNow(true);
        } else {
            setActivateNow(false);
        }
        return promotion;
    }

    private void clickOnAddBillingCycles(final String numberOfBillingCycles) {
        billingCyclesWebElement.clear();

        getActions().setText(billingCyclesWebElement, numberOfBillingCycles);
    }

    private void fillAllAmountInputFields(final String cashAmount) {
        final List<WebElement> amountFieldsList = getDriver().findElements(By.xpath(amountInputXpath));
        for (final WebElement element : amountFieldsList) {
            getActions().setText(element, cashAmount);
        }
    }

    private void clickOnAddButton() {
        addButton.click();
    }

    /**
     * method to Add Promotion
     *
     * @param promotion fileds of Promotion
     * @return Detail Page of New Promotion.
     */
    public void add(final Promotion promotion) {
        LOGGER.info("Add Promotion");
        navigateToAddPromotion();
        setName(promotion.getName());
        setDescription(promotion.getDescription());
        setStoreWide(promotion.getStoreWide());
        setPromotionCode(promotion.getPromotionCode());
        setStoreId(promotion.getStoreId());
        clickAddStore();
        selectPromotionType(promotion.getPromotionType());
        if (promotion.getPromotionType().equals(PromotionType.SUPPLEMENT_TIME.getValue())) {
            selectTimePeriodType(promotion.getDiscountType());
            setTimePeriodCount(promotion.getValue());
        } else {
            selectDiscountType(promotion.getDiscountType());
            setAmount(promotion.getAmount(), 0);
        }
        selectOfferingsType(promotion.getOfferingType());
        setSubscriptionOfferExternalKey(promotion.getBasicOrSubscriptionOffering());
        setEffectiveDate(promotion.getEffectiveDate());
        setExpirationDate(promotion.getExpirationDate());
        setMaxUses(promotion.getMaxUses());
        setMaxUsesPerUser(promotion.getMaxUsesPerUser());
        submit(TimeConstants.ONE_SEC);
    }

    /**
     * Click on the edit button so the page is now editable
     */
    public void edit() {
        LOGGER.info("Click edit");
        editButton.click();
    }

    /**
     * This is a method which will say whether the bundled checkbox is present in the add promotion page
     */
    public boolean isBundledCheckboxPresent() {

        navigateToAddPromotion();
        return isElementPresent(bundledCheckbox);
    }

    public String getDifferentOffersTermWarning() {

        final WebDriver driver = getDriver();
        final String windowHandle = driver.getWindowHandle();
        driver.switchTo().window(windowHandle);
        return differentOfferTermsWarning.getText().split("\n")[0];
    }

    private List<WebElement> getDiscountAmountDetailsHeaderElements() {
        return getDriver()
            .findElements(By.xpath(parentElementSelector + DISCOUNT_AMOUNT_DETAILS_TABLE_XPATH + "/thead/tr/th"));
    }

    /**
     * This method returns discount cash amount and price list details
     *
     * @param columnName
     * @return List<String>
     */
    private List<String> getDiscountAmountDetails(final String columnName) {
        final List<String> values = new ArrayList<>();
        final int columnIndex = getColumnIndex(columnName) + 1;
        final String selector =
            parentElementSelector + DISCOUNT_AMOUNT_DETAILS_TABLE_XPATH + "/tbody/tr/td[" + columnIndex + "]";
        final List<WebElement> cells = getDriver().findElements(By.xpath(selector));
        if (cells.size() == 1) {
            values.add(cells.get(0).getText());
        } else {
            for (final WebElement cell : cells) {
                values.add(cell.getText());
            }
        }
        return values;
    }

    /***
     * Method to get the column index for the given column name in a table
     *
     * @param columnName
     * @return
     */
    private int getColumnIndex(final String columnName) {
        return getColumnIndex(getDiscountAmountDetailsHeaderElements(), columnName);
    }

    /***
     * Method returns the BasicOfferings Header Elements
     *
     * @return
     */
    private List<WebElement> getBasicOfferingsHeaderElements() {
        return getDriver().findElements(By.xpath(parentElementSelector + BASIC_OFFERING_TABLE_XPATH + "/thead/tr/th"));
    }

    /***
     * Method returns the SubscriptionOffers Header Elements
     *
     * @return
     */
    private List<WebElement> getSubscriptionOffersHeaderElements() {
        return getDriver()
            .findElements(By.xpath(parentElementSelector + SUBSCRIPTION_OFFERS_TABLE_XPATH + "/thead/tr/th"));
    }

    /***
     * Method to get the BasicOfferingDetails
     *
     * @param columnName required
     * @return
     */
    private List<String> getBasicOfferingDetails(final String columnName) {
        final int columnIndex = getColumnIndex(getBasicOfferingsHeaderElements(), columnName) + 1;
        return getPromotionOfferingDetails(BASIC_OFFERING_TABLE_XPATH, columnIndex);
    }

    /***
     * Method to get the SubscriptionOffersDetails
     *
     * @param columnName required
     * @return
     */
    private List<String> getSubscriptionOffersDetails(final String columnName) {
        final int columnIndex = getColumnIndex(getSubscriptionOffersHeaderElements(), columnName) + 1;
        return getPromotionOfferingDetails(SUBSCRIPTION_OFFERS_TABLE_XPATH, columnIndex);
    }

    public boolean isCashAmountFieldPresentOnPage() {
        LOGGER.info("checking whether the cash amount input field is present on the page");

        return isElementPresent(amountInput);
    }

    /**
     * This method returns basic offering name and subscription offer name depending on column name
     *
     * @param index
     * @return List<String>
     */
    private List<String> getPromotionOfferingDetails(final String offerXpath, final int index) {
        final List<String> values = new ArrayList<>();

        final List<WebElement> cells = getDriver().findElements(By.xpath(offerXpath + "/tbody/tr/td[" + index + "]"));

        if (cells.size() == 1) {
            values.add((cells.get(0).getText().split("\\(")[0]).trim());
        } else {
            for (final WebElement cell : cells) {
                values.add((cell.getText().split("\\s+\\(")[0]).trim());
            }
        }
        return values;
    }

    /***
     * Method to get the column index for the given column name & table header values in a table
     *
     * @param headerElements required
     * @param columnName required
     * @return
     */
    private int getColumnIndex(final List<WebElement> headerElements, final String columnName) {
        int columnIndex = 0;
        boolean foundColumn = false;

        // Get all the header elements
        for (final WebElement element : headerElements) {
            if (columnName.equalsIgnoreCase(element.getText())) {
                foundColumn = true;
                break;
            }
            columnIndex++;
        }

        if (!foundColumn) {
            LOGGER.error("Unable to get column index for " + columnName);
        }

        return columnIndex;
    }
}
