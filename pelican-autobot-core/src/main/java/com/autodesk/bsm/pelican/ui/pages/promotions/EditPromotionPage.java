package com.autodesk.bsm.pelican.ui.pages.promotions;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.entities.Promotion;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Page object Pattern represents PromotionPage.
 *
 * @author Muhammad Azeem
 */
public class EditPromotionPage extends GenericGrid {
    public EditPromotionPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static String BASIC_OFFERING_TABLE_XPATH = "//*[@id='Basic Offerings-section']/div/table";
    private static String SUBSCRIPTION_OFFERS_TABLE_XPATH = "//*[@id='Subscription Offers-section']/div/table";
    private static String DISCOUNT_AMOUNT_DETAILS_TABLE_XPATH =
        "//*[@id='Discount Amount Details-section']/div/div/table";

    @FindBy(id = "input-name")
    private WebElement nameInput;

    @FindBy(id = "input-description")
    private WebElement descriptionInput;

    @FindBy(id = "storeWide")
    private WebElement storeWideCheckbox;

    @FindBy(id = "input-discountCode")
    private WebElement promotionCode;

    @FindBy(id = "input-discountPercent")
    private WebElement percentageDiscount;

    @FindBy(id = "promotionType")
    private WebElement promotionTypeSelect;

    @FindBy(id = "discountType")
    private WebElement discountTypeSelect;

    @FindBy(id = "input-discountCode")
    private WebElement discountCodeInput;

    @FindBy(name = "promoPriceListAmount[0].amount")
    private WebElement amountDiscount;

    @FindBy(id = "input-standalone")
    private WebElement standaloneCheckbox;

    @FindBy(id = "state")
    private WebElement state;

    @FindBy(id = "discountCashCurrencyId")
    private WebElement discountCredits;

    @FindBy(id = "input-storeId")
    private WebElement storeIdFind;

    @FindBy(id = "subtype")
    private WebElement subTypeFind;

    @FindBy(id = "input-activateNow")
    private WebElement activatePromotion;

    @FindBy(id = "input-effectiveDate")
    private WebElement effectiveDateInput;

    @FindBy(id = "input-expirationDate")
    private WebElement expirationDateInput;

    @FindBy(id = "input-maxUses")
    private WebElement maxUsesInput;

    @FindBy(id = "input-maxUsesPerUser")
    private WebElement maxUsesPerUserInput;

    @FindBy(id = "unlimited")
    private WebElement unlimitedCheckBox;

    @FindBy(id = "limitStoreId")
    private WebElement getAStore;

    @FindBy(id = "storeSelectionButton")
    private WebElement addStoreButton;

    @FindBy(id = "input-activateNow")
    private WebElement activateNowCheckbox;

    @FindBy(id = "discountCashCurrencyId")
    private WebElement discountCashCurrencyIdSelect;

    @FindBy(id = "supplementType")
    private WebElement selectSupplementType;

    @FindBy(id = "input-timePeriodCount")
    private WebElement timePeriodCount;

    @FindBy(id = "timePeriodType")
    private WebElement timePeriodType;

    @FindBy(id = "offeringType")
    private WebElement selectOfferingType;

    @FindBy(id = "externalKeyBO")
    private WebElement selectbasicOffering;

    @FindBy(id = "keySelectionButtonBO")
    private WebElement addBasicOffering;

    @FindBy(id = "externalKeySO")
    private WebElement selectSubscriptionOffer;

    @FindBy(id = "keySelectionButtonSO")
    private WebElement addSubscriptionOffer;

    @FindBy(id = "billingCycle")
    private WebElement numberOfBillingCycles;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(xpath = ".//*[@id='bd']/h1")
    private WebElement detailSelection;

    @FindBy(xpath = ".//*[@class='remove-button']")
    private WebElement removeButton;

    @FindBy(id = "billingCycle")
    private WebElement billingCyclesWebElement;

    // This has to be here in this class, the webelement is different than other
    // "cancel" button
    @FindBy(xpath = ".//*[@class='button cancel-btn']/*")
    private WebElement cancelButton;

    @FindBy(xpath = ".//*[@id='offeringsListErrors']/span/label")
    private WebElement errorMessageForBasicOfferingInvalidQuantity;

    @FindBy(xpath = ".//*[@id='offersListErrors']/span/label")
    private WebElement errorMessageForSubscriptionOfferingInvalidQuantity;

    @FindBy(id = "error-message-bo")
    private WebElement errorMessageForMoreBasicOfferings;

    @FindBy(id = "error-message-so")
    private WebElement errorMessageForMoreSubscriptionOfferings;

    @FindBy(xpath = ".//*[@class='error-message']")
    private WebElement errorMessageInAddPromotion;

    @FindBy(css = ".error-message")
    private WebElement errorMessage;

    @FindBy(id = "bundled")
    private WebElement bundledCheckbox;

    @FindBy(id = "limitStoreId")
    private WebElement storeInput;

    @FindBy(id = "error-message-store")
    private WebElement storeErrorMessage;

    @FindBy(name = "promoPriceListAmount[0].amount")
    private WebElement amountInput;

    @FindBy(id = "input-discountPercent")
    private WebElement percentageInput;

    @FindBy(id = "externalKeySO")
    private WebElement subscriptionOffers;

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

    @FindBy(id = "storeSelectionButton")
    private WebElement addStoresButton;

    private static final String QUANTITY_XPATH = ".//*[@class='name inline']//*[@type='text']";
    private static final String amountInputXpath = ".//*[@id='field-pricelists']//*[@class='input']//*[@class='text']";
    private static final String APPLY_DISCOUNT_CHECKBOX_XPATH = ".//*[@class='name inline']//*[@type='checkbox']";
    private static final String BASIC_OFFERING_XPATH = ".//*[@id='offeringsList']//*[@class='remove-button']";
    private static final String SUBSCRIPTION_OFFERING_XPATH = ".//*[@id='offersList']//*[@class='remove-button']";

    public AdminToolPage adminToolPage;
    private String discountSuppType;
    private String promotionType;
    private String offeringType;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditPromotionPage.class.getSimpleName());

    /**
     * Method to navigate to Promotions
     */
    public void navigateToPromotion(final String id) {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PROMOTION.getForm() + "/show?id=" + id;
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
     * @return the presence of Title Page
     */
    public boolean isTitlePagePresent() {
        boolean titlePage = false;
        try {
            titlePage = detailSelection.isDisplayed();
            LOGGER.info("Promotion Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Promotion Doesn't Exist");
        }
        return titlePage;
    }

    /**
     * Get the value of all fields of Promotions
     *
     * @return Details of Promotion.
     */
    public Promotion getDetails() {
        final Promotion promotion = new Promotion();
        final GenericDetails details = super.getPage(GenericDetails.class);
        final GenericGrid detailsColumn = super.getPage(GenericGrid.class);
        promotion.setId(details.getValueByField("ID"));
        promotion.setApplicationFamily(details.getValueByField("Application Family"));
        promotion.setApplication(details.getValueByField("Application"));
        promotion.setName(details.getValueByField("Name"));
        promotion.setDescription(details.getValueByField("Description"));
        promotion.setStoreWideField(details.getValueByField("Store Wide"));
        promotion.setPromotionCode(details.getValueByField("Promotion Code"));
        promotion.setPromotionType(details.getValueByField("Type"));
        promotion.setStoreId(details.getValueByField("Limited To Store"));

        if (details.getValueByField("Type").equals("Discount")) {
            promotion.setDiscountType(details.getValueByField("Discount Type"));
        } else {
            promotion.setSupplementType(details.getValueByField("Supplement Type"));
        }
        if (details.getValueByField("Type").equals("Discount")) {
            if (details.getValueByField("Discount Type").equals("Cash Discount")) {
                try {
                    if (!detailsColumn.getColumnValues("Amount").isEmpty()) {
                        promotion.setValue(detailsColumn.getColumnValues("Amount").get(0));
                    }
                } catch (final org.openqa.selenium.NoSuchElementException ex) {
                    System.out.println("No Such Element Exception thrown!");
                }
            } else {
                promotion.setValue(details.getValueByField("Percentage"));
            }
        } else {
            promotion.setValue(details.getValueByField("Time Period Count"));
        }
        promotion.setStandalonePromotion(details.getValueByField("Standalone"));

        if (details.getValueByField("Subscription Offers") != null) {
            promotion.setBasicOrSubscriptionOffering(details.getValueByField("Subscription Offers"));
            promotion.setNumberOfBillingCycles(details.getValueByField("Billing Cycle"));
        } else {
            promotion.setBasicOrSubscriptionOffering(details.getValueByField("Basic Offerings"));
        }
        promotion.setEffectiveDateRange(details.getValueByField("Effective Date Range"));
        promotion.setMaxUses(details.getValueByField("Maximum Number of Uses"));
        promotion.setMaxUsesPerUser(details.getValueByField("Maximum Uses per User"));
        promotion.setStatus(Status.valueOf(details.getValueByField("State")));
        promotion.setActivatePromotion(true);
        return promotion;
    }

    /**
     * method to Edit Promotion
     *
     * @param promotion to Edit in Promotion
     * @return Detail Page of Promotion with edited fields
     */
    public void editPromotion(final Promotion promotion) {
        LOGGER.info("Start Edit Promotion");
        setName(promotion.getName());
        setDescription(promotion.getDescription());
        setStoreWide(promotion.getStoreWide());
        setPromotionCode(promotion.getPromotionCode());
        setAStore(promotion.getStoreId());
        addStore();
        selectPromotionType(promotion.getPromotionType());
        try {
            confirmButton.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            // Leave as it is if no pop up is available
            System.out.println("No Such Element Exception thrown!");
        }

        selectDiscountType(promotion.getDiscountType());

        setValue(promotion.getValue());
        Util.scroll(getDriver(), "600", "0");

        if (!promotion.getPromotionType().equals("Supplement")) {
            selectOfferingType(promotion.getOfferingType());

            try {
                confirmButton.click();
            } catch (final org.openqa.selenium.NoSuchElementException ex) {
                System.out.println("No Such Element Exception thrown!");
            }
        }
        setBasicOrSubscriptionOffering(promotion.getBasicOrSubscriptionOffering());
        try {
            confirmButton.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            System.out.println("No Such Element Exception thrown!");
        }

        setEffectiveDate(promotion.getEffectiveDate());
        setExpirationDate(promotion.getExpirationDate());
        setMaxUses(promotion.getMaxUses());
        setMaxUsesPerUser(promotion.getMaxUsesPerUser());
        submit();
    }

    /**
     * method to Edit Promotion
     *
     * @param name
     * @param code
     * @param description
     * @param storeId
     * @param promotionType
     * @param discountSupplementSubType
     * @param value
     * @param offeringType
     * @param externalKeyOffer
     * @param storeWide
     * @param startDate
     * @param expireDate
     * @param maxUses
     * @param maxUsesPerUser
     */
    public void editPromotion(final String name, final String code, final String description, final String storeId,
        final String promotionType, final String discountSupplementSubType, final String value,
        final String offeringType, final String externalKeyOffer, final boolean storeWide, final String startDate,
        final String expireDate, final String maxUses, final String maxUsesPerUser) {
        LOGGER.info("Start Edit Promotion");
        setName(name);
        setPromotionCode(code);
        setDescription(description);
        setAStore(storeId);
        setStoreWide(storeWide);
        addStore();
        selectPromotionType(promotionType);
        try {
            confirmButton.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            // Leave as it is if no pop up is available
            System.out.println("No Such Element Exception thrown!");
        }
        selectDiscountType(discountSupplementSubType);

        setValue(value);

        if (!promotionType.equals("Supplement")) {
            selectOfferingType(offeringType);

            try {
                confirmButton.click();
            } catch (final org.openqa.selenium.NoSuchElementException ex) {
                System.out.println("No Such Element Exception thrown!");
            }
        }
        setBasicOrSubscriptionOffering(externalKeyOffer);
        try {
            confirmButton.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            System.out.println("No Such Element Exception thrown!");
        }

        setEffectiveDate(startDate);
        setExpirationDate(expireDate);
        setMaxUses(maxUses);
        setMaxUsesPerUser(maxUsesPerUser);
        submit();
    }

    /**
     * This is a method to edit a bundle promotion in the admin tool
     *
     * @return Promotion Object
     */
    public Promotion editBundlePromotion(final Promotion promotion) {

        final Promotion filledFieldsOfPromotion = editFieldsInPromotion(promotion);
        Promotion newPromotion;

        if (promotion.getStoreId() == null) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();
            newPromotion = setAllFieldsOfPromotion(newPromotion);
        } else if (promotion.getStoreId().isEmpty()) {
            newPromotion = filledFieldsOfPromotion;
        } else if (("Invalid-Quantity").equalsIgnoreCase(promotion.getErrorMessageForBasicOfferingInvalidQuantity())
            && ("Invalid-Quantity")
                .equalsIgnoreCase(promotion.getErrorMessageForSubscriptionOfferingInvalidQuantity())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion
                .setErrorMessageForBasicOfferingInvalidQuantity(errorMessageForBasicOfferingInvalidQuantity.getText());
            newPromotion.setErrorMessageForSubscriptionOfferingInvalidQuantity(
                errorMessageForSubscriptionOfferingInvalidQuantity.getText());
        } else if (("Invalid-Quantity").equalsIgnoreCase(promotion.getErrorMessageForBasicOfferingInvalidQuantity())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion
                .setErrorMessageForBasicOfferingInvalidQuantity(errorMessageForBasicOfferingInvalidQuantity.getText());
        } else if (("Invalid-Quantity")
            .equalsIgnoreCase(promotion.getErrorMessageForSubscriptionOfferingInvalidQuantity())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

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
            clickOnUpdatePromotion();

            newPromotion.setPromotionCodeErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("not-all-pricelists").equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion.setPriceListErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("no-discount-entered").equalsIgnoreCase(promotion.getDiscountErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion.setDiscountErrorMessage(errorMessage.getText());
        } else if ((!(promotion.getStoreId().isEmpty())) && !((promotion.getPriceListExternalKey().isEmpty()))
            && !(("Emptypricelist".equalsIgnoreCase(promotion.getPriceListExternalKey())))
            && !("no-offering".equalsIgnoreCase(promotion.getPriceListExternalKey()))
            && !("two-store-wide".equalsIgnoreCase(promotion.getStoreWideErrorMessage()))
            && !("Time-Mismatch".equalsIgnoreCase(promotion.getStoreWideErrorMessage()))) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();
            newPromotion = setAllFieldsOfPromotion(newPromotion);
        } else if ((promotion.getPercentageAmount() == null || promotion.getPercentageAmount().isEmpty())
            && !(promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering"))
            && !("two-store-wide".equalsIgnoreCase(promotion.getStoreWideErrorMessage()))) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();
            newPromotion.setDiscountErrorMessage(errorMessageInAddPromotion.getText());
        } else if (promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist")) {
            newPromotion = filledFieldsOfPromotion;
            newPromotion.setPriceListErrorMessage(errorMessageInAddPromotion.getText());
        } else if (promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering")) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion.setSubscriptionOfferErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("two-store-wide").equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion.setStoreWideErrorMessage(errorMessageInAddPromotion.getText());
        } else if (("Time-Mismatch").equalsIgnoreCase(promotion.getStoreWideErrorMessage())) {
            newPromotion = filledFieldsOfPromotion;
            clickOnUpdatePromotion();

            newPromotion.setTimeMismatchError(errorMessageInAddPromotion.getText());
        } else {
            clickOnUpdatePromotion();
            Util.waitInSeconds(TimeConstants.ONE_SEC);
            newPromotion = setAllFieldsOfPromotion(filledFieldsOfPromotion);
        }

        return newPromotion;
    }

    /*
     * Fill all the fields in the Promotion add page
     */
    private Promotion editFieldsInPromotion(final Promotion promotion) {
        getActions().setText(nameInput, promotion.getName());
        if (promotion.getPromotionCode() != null) {
            getActions().setText(discountCodeInput, promotion.getPromotionCode());
        }
        if (promotion.getStoreWide()) {
            storeWideCheckbox.click();
        }
        if (!promotion.isBundled()) {
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
                if (promotion.getBasicOfferings() != null
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("Emptypricelist"))
                    && !(promotion.getPriceListExternalKey().equalsIgnoreCase("no-offering"))) {
                    if ((!promotion.isBundled())) {
                        final String basicOffering = promotion.getBasicOfferings();
                        getActions().select(selectOfferingType, basicOffering);
                        handleOfferingPopup();
                    }
                    if ((promotion.getStoreId() == null || promotion.getStoreId().isEmpty())) {
                        final String offeringExternalKey =
                            promotion.getBasicOfferingsExternalKey().toString().replace("[", "").replace("]", "");
                        clearBasicOfferingsList();
                        getActions().setText(selectbasicOffering, offeringExternalKey);
                        for (int i = 1; i <= promotion.getBasicOfferingsExternalKey().size(); i++) {
                            clickOnAddButton();
                            if ((promotion.isBundled()) && promotion.getQuantityOfBasicOfferingsList() != null) {
                                final List<WebElement> quantityElementList =
                                    getDriver().findElements(By.xpath(QUANTITY_XPATH));
                                getActions().setText(quantityElementList.get(i - 1),
                                    String.valueOf(promotion.getQuantityOfBasicOfferingsList().get(i - 1)));
                                quantityElementList.clear();
                            }
                            if ((promotion.isBundled() && promotion.getApplyDiscountForBasicOfferingsList() != null)) {
                                final List<WebElement> applyDiscountElementList =
                                    getDriver().findElements(By.xpath(APPLY_DISCOUNT_CHECKBOX_XPATH));
                                if (!promotion.getApplyDiscountForBasicOfferingsList().get(i - 1)) {
                                    applyDiscountElementList.get(i - 1).click();
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
                        clearBasicOfferingsList();
                        getActions().setText(selectbasicOffering, offeringExternalKey);
                        for (int i = 1; i <= promotion.getBasicOfferingsExternalKey().size(); i++) {
                            clickOnAddButton();
                            Util.waitInSeconds(TimeConstants.ONE_SEC);
                            if ((promotion.isBundled() && promotion.getQuantityOfBasicOfferingsList() != null)) {
                                final List<WebElement> quantityElementList =
                                    getDriver().findElements(By.xpath(QUANTITY_XPATH));
                                getActions().setText(quantityElementList.get(i - 1),
                                    String.valueOf(promotion.getQuantityOfBasicOfferingsList().get(i - 1)));
                                quantityElementList.clear();
                            }

                            if ((promotion.isBundled() && promotion.getApplyDiscountForBasicOfferingsList() != null)) {
                                final List<WebElement> applyDiscountElementList =
                                    getDriver().findElements(By.xpath(APPLY_DISCOUNT_CHECKBOX_XPATH));
                                if (!promotion.getApplyDiscountForBasicOfferingsList().get(i - 1)) {
                                    applyDiscountElementList.get(i - 1).click();
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
                        getActions().select(selectOfferingType, subscriptionOffering);
                        handleOfferingPopup();
                    }
                    final String offeringExternalKey =
                        promotion.getSubscriptionOfferingsExternalKey().toString().replace("[", "").replace("]", "");
                    clearSubscriptionOfferingsList();
                    getActions().setText(subscriptionOffers, offeringExternalKey);
                    for (int i = 1; i <= promotion.getSubscriptionOfferingsExternalKey().size(); i++) {
                        clickAddSubscriptionOffer();
                        Util.waitInSeconds(TimeConstants.ONE_SEC);
                        if ((promotion.isBundled()) && promotion.getQuantityOfSubscriptionOfferingsList() != null) {
                            final List<WebElement> quantityElementList =
                                getDriver().findElements(By.xpath(QUANTITY_XPATH));
                            getActions().setText(
                                quantityElementList.get((promotion.getBasicOfferingsExternalKey().size()) + (i - 1)),
                                String.valueOf(promotion.getQuantityOfSubscriptionOfferingsList().get(i - 1)));
                            quantityElementList.clear();
                        }
                        if ((promotion.isBundled()
                            && promotion.getApplyDiscountForSubscriptionOfferingsList() != null)) {
                            final List<WebElement> applyDiscountElementList =
                                getDriver().findElements(By.xpath(APPLY_DISCOUNT_CHECKBOX_XPATH));
                            if (!promotion.getApplyDiscountForSubscriptionOfferingsList().get(i - 1)) {
                                applyDiscountElementList
                                    .get((promotion.getBasicOfferingsExternalKey().size()) + (i - 1)).click();
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
                getActions().setText(maxUsesInput, promotion.getMaxUses());
                getActions().setText(maxUsesPerUserInput, promotion.getMaxUsesPerUser());
                if ((Status.ACTIVE).equals(promotion.getActivateStatus())) {
                    setActivateNow(true);
                } else {
                    setActivateNow(false);
                }
                getActions().select(effectiveDateStartHour, promotion.getTimeInHours());
                getActions().select(effectiveDateStartMinute, promotion.getTimeInMinutes());
                getActions().select(effectiveDateStartSecond, promotion.getTimeInSeconds());
                getActions().select(effectiveExpirationDateEndHour, promotion.getExpirationTimeInHours());
                getActions().select(effectiveExpirationDateEndMinute, promotion.getExpirationTimeInMinutes());
                getActions().select(effectiveExpirationDateEndSecond, promotion.getExpirationTimeInSeconds());
            }
        }
        return promotion;
    }

    private Promotion setAllFieldsOfPromotion(final Promotion newPromotion) {

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
                if (promotion.getSubscriptionOfferNameList().size() > 0
                    && promotion.getBasicOfferingNameList().size() > 0) {
                    promotion.setCashAmount(getDiscountAmountDetails("Amount").get(0).substring(0, 6));
                } else {
                    promotion.setCashAmount(getDiscountAmountDetails("Amount").get(0).substring(0, 6));
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
            promotion.setNumberOfBillingCycles(genericDetails.getValueByField("Billing Cycle"));
            promotion.setTimePeriodCount(genericDetails.getValueByField("Time Period Count"));
            promotion.setTimePeriodType(genericDetails.getValueByField("Time Period Type"));
        }
        promotion.setStoreErrorMessage(newPromotion.getStoreErrorMessage());
        promotion.setWindowTitle(newPromotion.getWindowTitle());
        return promotion;
    }

    /**
     * method to cancel the Editing of Promotion
     */
    public void cancelEditOfPromotion(final Promotion promotion) {
        LOGGER.info("Start Edit Promotion");
        setName(promotion.getName());
        setDescription(promotion.getDescription());
        setStoreWide(true);
        setPromotionCode(promotion.getPromotionCode());
        setAStore(promotion.getStoreId());
        addStore();

        selectPromotionType(promotion.getPromotionType());
        selectDiscountType(promotion.getDiscountType());

        setValue(promotion.getValue());
        setEffectiveDate(promotion.getEffectiveDate());
        setExpirationDate(promotion.getExpirationDate());
        setMaxUses(promotion.getMaxUses());
        setMaxUsesPerUser(promotion.getMaxUsesPerUser());
        cancelButton.click();

    }

    /**
     * method to Cancel Promotion
     */
    public void cancelButton() {
        LOGGER.info("Click on Cancel Button");
        cancelButton.click();
    }

    /**
     * add a Store in a Promotion
     */
    public void addStore() {
        LOGGER.info("Add Store");
        addStoreButton.click();
    }

    /**
     * Delete all stores from promotion
     */
    public void deleteAllStoresFromPromotion() {
        LOGGER.info("Delete All Stores");
        final List<WebElement> elementsList = getDriver().findElements(By.xpath(".//*[@class='remove-button']"));
        for (final WebElement element : elementsList) {
            element.click();
        }
    }

    /*
     * Delete first store from the promotion
     */
    public void deleteFirstStoreFromPromotion() {
        LOGGER.info("Delete First Store");
        final List<WebElement> elementsList = getDriver().findElements(By.xpath(".//*[@class='remove-button']"));
        elementsList.get(0).click();
    }

    /*
     * Wait till Store is deleted successfully from the promotion
     */
    public void waitTillStoreIsDeleted() {
        LOGGER.info("Wait till the store is removed from the promotion");
        final WebElement store = (new WebDriverWait(getDriver(), 1))
            .until(ExpectedConditions.presenceOfElementLocated(By.xpath(".//*[@class='remove-button']")));
    }

    /*
     * Edit the amount entered in the pricelists
     */
    public void editAmountEnteredForPricelists(final String amount) {
        final List<WebElement> elementsList =
            getDriver().findElements(By.xpath(".//*[@id='field-pricelists']//*[@class='input']//*[@class='text']"));
        for (final WebElement element : elementsList) {
            element.clear();
            LOGGER.info("Set amount to '" + amount + "'");
            getActions().setText(element, amount);
        }
    }

    /**
     * set any name for Promotion
     */
    protected void setName(final String name) {
        LOGGER.info("Set name to '" + name + "'");
        getActions().setText(nameInput, name);
    }

    /**
     * set the Description for Promotion
     */
    protected void setDescription(final String description) {
        LOGGER.info("Set description to '" + description + "'");
        getActions().setText(descriptionInput, description);
    }

    private void setStoreWide(final boolean checkStoreWide) {
        if (!checkStoreWide) {
            getActions().uncheck(storeWideCheckbox);
        } else {
            getActions().check(storeWideCheckbox);
        }
    }

    /**
     * set the custom promotion code
     */
    private void setPromotionCode(final String promotionCode) {
        LOGGER.info("Set promotion code  to '" + promotionCode + "'");
        getActions().setText(discountCodeInput, promotionCode);
    }

    /**
     * set a store
     */
    private void setAStore(final String storeId) {
        LOGGER.info("Set store '" + storeId + "'");
        getActions().setText(getAStore, storeId);
    }

    /**
     * delete a store set a store
     */
    public void deleteStoreAndSetAStore(final String storeId) {
        LOGGER.info("Delete existing store");
        removeButton.click();
        LOGGER.info("Set store '" + storeId + "'");
        getActions().setText(getAStore, storeId);
    }

    /**
     * select type of promotion
     */
    private void selectPromotionType(final String promotionType) {
        this.promotionType = promotionType;
        LOGGER.info("Select Type Of Promotion'" + promotionType + "'");
        getActions().selectWithElementText(promotionTypeSelect, promotionType);
    }

    /**
     * if Promotion Type is Discount than it will not set time in Promotion.
     */
    private void selectDiscountType(final String discountSuppType) {
        this.discountSuppType = discountSuppType;
        if (promotionType.equals("Discount")) {
            LOGGER.info("Select Discount type to '" + discountSuppType + "'");
            getActions().selectWithElementText(discountTypeSelect, discountSuppType);
        } else {
            LOGGER.info("Select Discount type to '" + discountSuppType + "'");
            getActions().selectWithElementText(timePeriodType, discountSuppType);
        }
    }

    /**
     * if Promotion type is Discount and subType is amount than set value in Currency if Promotion type is Discount and
     * subType is Percentage than set value in Percentage if Promotion type is Supplement and than set value in
     * days/month/year.
     */
    private void setValue(final String value) {
        if (promotionType.equals("Discount")) {
            if (discountSuppType.equals("Cash Amount")) {
                LOGGER.info("Set amount  to '" + value + "'");
                getActions().setText(amountDiscount, value);
            } else {
                LOGGER.info("Set percentage to '" + value + "'");
                getActions().setText(percentageDiscount, value);
            }
        } else {
            LOGGER.info("Set time period count '" + value + "'");
            getActions().setText(timePeriodCount, value);

            LOGGER.info("Already Set time period count '" + value + "'");
        }
    }

    /**
     * set standalone promotion
     */
    public void setStandalonePromotion(final boolean checkStandalone) {
        if (checkStandalone) {
            getActions().check(standaloneCheckbox);
        }
    }

    /**
     * set effective date
     */
    private void setEffectiveDate(final String effectiveDate) {
        LOGGER.info("Set effectiveDate to '" + effectiveDate + "'");
        getActions().setText(effectiveDateInput, effectiveDate);
    }

    /**
     * set expiration date
     */
    private void setExpirationDate(final String expirationDate) {
        LOGGER.info("Set expirationDate to  '" + expirationDate + "'");
        getActions().setText(expirationDateInput, expirationDate);
    }

    /**
     * set maximum number of uses
     */
    private void setMaxUses(final String maxUses) {
        LOGGER.info("Set maxUses to '" + maxUses + "'");
        getActions().setText(maxUsesInput, maxUses);
    }

    /**
     * set maximum uses per user
     */
    private void setMaxUsesPerUser(final String maxUsesPerUser) {
        LOGGER.info("Set maxUsesPerUser to '" + maxUsesPerUser + "'");
        getActions().setText(maxUsesPerUserInput, maxUsesPerUser);
    }

    /**
     * select the type of offering either Basic or Subscription
     */
    private void selectOfferingType(final String offeringType) {
        this.offeringType = offeringType;
        LOGGER.info("Select Type Of offering'" + offeringType + "'");
        getActions().selectWithElementText(selectOfferingType, offeringType);

    }

    /**
     * add external key of basic offering if Offering Type is Basic add external key of subscription offer if Offering
     * type is Subscription
     */
    private void setBasicOrSubscriptionOffering(final String basicSubscriptionOffer) {
        if (offeringType.equals("Basic Offerings") && !promotionType.equals("Supplement")) {
            LOGGER.info("Set Basic Offerings '" + basicSubscriptionOffer + "'");
            getActions().setText(selectbasicOffering, basicSubscriptionOffer);
            addBasicOffering.click();
        } else {
            LOGGER.info("Set Subscription Offer '" + basicSubscriptionOffer + "'");
            getActions().setText(selectSubscriptionOffer, basicSubscriptionOffer);
            addSubscriptionOffer.click();
        }
    }

    /**
     * if Offering type is Subscription Offer than set number of billing cycles.
     */
    public void setNumberOfBillingCycles(final String billingCycles) {
        if (offeringType.equals("Subscription Offers")) {
            LOGGER.info("Set Number Of Billing Cycles '" + billingCycles + "'");
            getActions().setText(numberOfBillingCycles, billingCycles);
        }
    }

    public PromotionDetailsPage clickOnUpdatePromotion() {
        LOGGER.info("Click on update promotion button");
        submit();
        return super.getPage(PromotionDetailsPage.class);
    }

    private void clickAddStore() {
        this.addStoresButton.click();
    }

    private void fillAllAmountInputFields(final String cashAmount) {
        final List<WebElement> amountFieldsList = getDriver().findElements(By.xpath(amountInputXpath));
        for (final WebElement element : amountFieldsList) {
            getActions().setText(element, cashAmount);
        }
    }

    private void clickOnAddButton() {
        addBasicOffering.click();
    }

    private void clickAddSubscriptionOffer() {
        LOGGER.info("Click on 'Add Subscription Offer'");
        getActions().click(addSubscriptionOffer);
    }

    private void clickOnUnlimitedCheckBox() {
        LOGGER.info("Click on 'Unlimited' checkbox");
        unlimitedCheckBox.click();
    }

    private void clickOnAddBillingCycles(final String numberOfBillingCycles) {

        billingCyclesWebElement.clear();

        getActions().setText(billingCyclesWebElement, numberOfBillingCycles);
    }

    // set activate the promotion now checkbox
    private void setActivateNow(final boolean activateNow) {
        Util.waitInSeconds(3);
        if (activateNow) {
            getActions().check(activatePromotion);
        }
    }

    private void handleOfferingPopup() {
        final String parentHandle = getDriver().getWindowHandle();
        final int numberOfHandles = getDriver().getWindowHandles().size();
        String handle;
        if (numberOfHandles == 2) {
            handle = getDriver().getWindowHandles().toArray()[1].toString();

        } else {
            handle = getDriver().getWindowHandles().toArray()[0].toString();
        }
        getDriver().switchTo().window(handle);
        confirmButton.click();
        getDriver().switchTo().window(parentHandle);

    }

    private void clearBasicOfferingsList() {
        final List<WebElement> offeringElementsList = getDriver().findElements(By.xpath(BASIC_OFFERING_XPATH));
        if (offeringElementsList.size() > 0) {
            for (final WebElement anOfferingElementsList : offeringElementsList) {
                anOfferingElementsList.click();
            }
        }
    }

    private void clearSubscriptionOfferingsList() {
        final List<WebElement> offeringElementsList = getDriver().findElements(By.xpath(SUBSCRIPTION_OFFERING_XPATH));
        if (offeringElementsList.size() > 0) {
            for (final WebElement anOfferingElementsList : offeringElementsList) {
                anOfferingElementsList.click();
            }
        }
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
