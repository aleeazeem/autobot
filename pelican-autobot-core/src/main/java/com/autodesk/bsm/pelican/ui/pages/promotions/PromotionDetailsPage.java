package com.autodesk.bsm.pelican.ui.pages.promotions;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PromotionDetailsPage extends GenericGrid {
    public PromotionDetailsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    private static String BASIC_OFFERING_TABLE_XPATH = "//*[@id='Basic Offerings-section']/div/table";
    private static String SUBSCRIPTION_OFFERS_TABLE_XPATH = "//*[@id='Subscription Offers-section']/div/table";
    private static String DISCOUNT_AMOUNT_DETAILS_TABLE_XPATH =
        "//*[@id='Discount Amount Details-section']/div/div/table";

    @FindBy(name = "Extend Active Promotion")
    private WebElement extendActivePromotion;

    @FindBy(id = "confirm-btn")
    private WebElement confirmPopUp;

    @FindBy(id = "cancel-btn")
    private WebElement cancelPopUp;

    @FindBy(xpath = ".//*[@id='Additional Constraints-section']/dt")
    private WebElement additionalConstraintsSection;

    @FindBy(xpath = ".//*[@id='Discount Details-section']/dt")
    private WebElement discountDetailsSection;

    @FindBy(xpath = ".//*[@id='Offerings-section']/dt")
    private WebElement offeringsSection;

    @FindBy(xpath = ".//*[@id='ID-prop']/dt")
    private WebElement idProp;

    @FindBy(name = "Activate")
    private WebElement activateButton;

    @FindBy(xpath = "//button[text()='Edit Localized Descriptors']")
    private WebElement editLocalizedDescriptors;

    @FindBy(xpath = "//button[text()='Edit Non-Localized Descriptors']")
    private WebElement editNonLocalizedDescriptors;

    @FindBy(xpath = "//select")
    private WebElement localeSelect;

    @FindBy(id = "language")
    private WebElement languageSelect;

    @FindBy(id = "country")
    private WebElement countrySelect;

    @FindBy(xpath = "//td[text()='Yes']/preceding-sibling::th")
    private List<WebElement> yesDescriptors;

    @FindBy(xpath = "//td[text()='No']/preceding-sibling::th")
    private List<WebElement> noDescriptors;

    @FindBy(name = "Clone")
    private WebElement cloneButton;

    // Overwriting this webelement because GenericDetails one is not working for this.
    @FindBy(name = "Edit")
    private WebElement editButton;

    // Overwriting this webelement because GenericDetails one is not working for this.
    @FindBy(name = "Cancel")
    private WebElement cancelButton;

    @FindBy(xpath = "//h2[contains(text(),'Subscription Offers')]")
    private WebElement subscriptionOffer;

    @FindBy(xpath = "//h2[contains(text(),'Basic Offerings')]")
    private WebElement basicOffering;

    @FindBy(xpath = ".//*[@id='Discount Amount Details-section']//th")
    private WebElement headersOfDiscountAmountDetailsSection;

    @FindBy(css = "table[class='results']")
    private WebElement offerDetailsTable;

    @FindBy(xpath = "//th[text()='Offer Name']")
    private WebElement offerNameHeader;

    @FindBy(xpath = "//th[text()='External Key']")
    private WebElement offerExternalKeyHeader;

    @FindBy(xpath = "//th[text()='Quantity']")
    private WebElement offerQuantityHeader;

    @FindBy(xpath = "//th[text()='Apply Discount']")
    private WebElement offerApplyDiscountHeader;

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionDetailsPage.class.getSimpleName());
    private static final String EFFECTVE_DATE_RANGE = "Effective Date Range";
    private static final String LIMITED_TO_STORES = "Limited To Stores";
    private static final String LIMITED_TO_STORE = "Limited To Store";
    private static final String TABLE_DETAILS_BY_COLUMN_SELECTOR = ".results > tbody > tr > td:nth-child";
    private static final String HEADER_ELEMENTS_OF_DISCOUNT_AMOUT_SECTION =
        ".//*[@id='Discount Amount Details-section']" + "//th";

    /**
     * Navigate to Promotion Details Page
     */
    public void navigateToPromotionDetails(final String id) {
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

    public void activatePromotion() {
        LOGGER.info("Activate Promotion");
        activateButton.click();
    }

    public void cancelPromotion() {
        LOGGER.info("Cancel A Promotion");
        cancelButton.click();
        try {
            confirmButton.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("No Such Element Exception thrown!");
        }
    }

    public ExtendPromotionPage extendActivePromotion() {
        LOGGER.info("Extend Active Promotion");
        extendActivePromotion.click();
        return super.getPage(ExtendPromotionPage.class);
    }

    public boolean isExtendActivePromotionButtonDisplayed() {
        boolean isFound = true;
        try {
            extendActivePromotion.isDisplayed();
            LOGGER.info("Extend active promotion button is displayed");
        } catch (final Exception e) {
            isFound = false;
            LOGGER.info("Extend active promotion button is not displayed");
        }
        return isFound;
    }

    public void popUpWindowConfirm() {
        try {
            confirmPopUp.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            // Leave as it is if no pop up is available
            System.out.println("No Such Element Exception thrown!");
        }
    }

    public void popUpWindowCancel() {
        try {
            cancelPopUp.click();
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            // Leave as it is if no pop up is available
            System.out.println("No Such Element Exception thrown!");
        }
    }

    public String getId() {
        return getValueByField("ID");
    }

    public String getApplicationFamily() {
        return getValueByField("Application Family");
    }

    public String getApplication() {
        return getValueByField("Application");
    }

    public String getName() {
        return getValueByField("Name");
    }

    public String getCode() {
        return getValueByField("Promotion Code");
    }

    public String getDescription() {
        return getValueByField("Description");
    }

    public String getStoreWide() {
        return getValueByField("Store Wide");
    }

    public String getPromotionCode() {
        return getValueByField("Promotion Code");
    }

    public String getBundledPromo() {
        return getValueByField("Bundled");
    }

    public String getType() {
        return getValueByField("Type");
    }

    public String getState() {
        return getValueByField("State");
    }

    public String getCreated() {
        return getValueByField("Created");
    }

    public String getCreatedBy() {
        return getValueByField("Created By");
    }

    public String getLastModified() {
        return getValueByField("Last Modified");
    }

    public String getSubscriptionOffers() {
        return getValueByField("Subscription Offers");
    }

    public String getBasicOfferings() {
        return getValueByField("Basic Offerings");
    }

    public String getLastModifiedBy() {
        return getValueByField("Last Modified By");
    }

    public String getSupplementType() {
        return getValueByField("Supplement Type");
    }

    public String getTimePeriodCount() {
        return getValueByField("Time Period Count");
    }

    public String getTimePeriodType() {
        return getValueByField("Time Period Type");
    }

    public String getDiscountType() {
        return getValueByField("Discount Type");
    }

    public String getPercentage() {
        return getValueByField("Percentage");
    }

    public String getAmount() {
        return getCashAmountDetailsByColumnName("Amount");
    }

    public String getDiscountAmountStore() {
        return getCashAmountDetailsByColumnName("Store");
    }

    public String getStandalone() {
        return getValueByField("Standalone");
    }

    public String getEffectiveDateRange() {
        return getValueByField(EFFECTVE_DATE_RANGE);
    }

    /**
     * method to get start date from effective date range
     *
     * @param includeHoursMinsSecs
     * @return start date with or without time
     */
    public String getStartDate(final boolean includeHoursMinsSecs) {
        if (includeHoursMinsSecs) {
            return (getValueByField(EFFECTVE_DATE_RANGE)).substring(6, 24);
        } else {
            return (getValueByField(EFFECTVE_DATE_RANGE)).substring(6, 15);
        }
    }

    /**
     * method to get end date from effective date range
     *
     * @param includeHoursMinsSecs
     * @return end date with or without time
     */
    public String getEndDate(final boolean includeHoursMinsSecs) {
        if (includeHoursMinsSecs) {
            return (getValueByField(EFFECTVE_DATE_RANGE)).substring(32, 23);
        } else {
            return (getValueByField(EFFECTVE_DATE_RANGE)).substring(33, 41);
        }
    }

    public String getMaximumNumberOfUses() {
        return getValueByField("Maximum Number of Uses");
    }

    public String getMaximumUsesPerUser() {
        return getValueByField("Maximum Uses per User");
    }

    public String getLimitedToStore() {
        return getValueByField(LIMITED_TO_STORE);
    }

    public String getLimitedToStoreId() {
        String storeId = (getIdByField(null, LIMITED_TO_STORE)).replace("(", "");
        storeId = storeId.replace(")", "");
        return storeId;
    }

    public String getLimitedToStores() {
        return getValueByField(LIMITED_TO_STORES);
    }

    public String getBillingCycle() {
        return getValueByField("Billing Cycle");
    }

    /**
     * Method to click on Edit Localized Descriptors button
     */
    public void clickEditLocalizedDescriptors() {
        editLocalizedDescriptors.click();
    }

    /**
     * Method to click on Edit NonLocalized Descriptors button
     */
    public void clickEditNonLocalizedDescriptors() {
        editNonLocalizedDescriptors.click();
    }

    /**
     * Method to set Other Locale text field
     */
    public void setOtherLocales(final String text) {
        LOGGER.info("Select locale ' " + text + "'");
        getActions().select(localeSelect, text);
    }

    /**
     * Method returns value in locale field
     *
     * @return string ( first selected value)
     */
    public String getFirstSelectedSelectValue() {
        final Select select = new Select(localeSelect);
        final WebElement selectedValue = select.getFirstSelectedOption();
        LOGGER.info("Get first selected value: " + selectedValue.getText());
        return selectedValue.getText();
    }

    /**
     * Method to set Language in Choose Locale page
     */
    public void setLanguague(final String language) {
        LOGGER.info("Select language '" + language + "'");
        getActions().select(languageSelect, language);
    }

    /**
     * Method to set Country in Choose Locale page
     */
    public void setCountry(final String country) {
        LOGGER.info("Select country '" + country + "'");
        getActions().select(countrySelect, country);
    }

    /**
     * Method to edit Localized Descriptor values
     *
     * @return void
     */
    public void editLocalizedDescriptorValue(final String descriptorsName, final String descriptorValue) {
        final String xpath = "//div/label[text()='" + descriptorsName + ":']/following-sibling::span/input";
        final WebElement element = getDriver().findElement(By.xpath(xpath));
        element.clear();
        element.sendKeys(descriptorValue);
    }

    /**
     * Method to edit Non Localized Descriptor values
     */
    public void editNonLocalizedDescriptorValue(final String descriptorValue) {
        final String xpath = "//input[contains(@name,'ipp')]";
        final WebElement element = getDriver().findElement(By.xpath(xpath));
        element.clear();
        element.sendKeys(descriptorValue);
    }

    /**
     * Method to return list of all Localized Descriptors
     */
    public List<String> getAllLocalizedDescriptorsNames() {
        return getListOfText(yesDescriptors);
    }

    /**
     * Method to return list of all Non Localized Descriptors
     */
    public List<String> getAllNonLocalizedDescriptorsNames() {
        return getListOfText(noDescriptors);
    }

    /**
     * Method to return list of titles of descriptors
     *
     * @param webElementList - list of webElements
     * @return list of String
     */
    private List<String> getListOfText(final List<WebElement> webElementList) {
        final List<String> textList = new ArrayList<>();
        for (final WebElement element : webElementList) {
            if (element.getText() != null && element.getText().length() > 0) {
                textList.add(element.getText());
            }
        }
        return textList;
    }

    /**
     * @return the presence of Edit Button
     */
    public boolean isEditButtonPresent() {
        boolean editButtonPresent = false;
        try {
            editButtonPresent = editButton.isDisplayed();
            LOGGER.info("Edit Button Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Edit Button Doesn't Exist");
        }
        return editButtonPresent;
    }

    /**
     * Delete Promotion
     *
     * @return true if Promotion is deleted. otherwise false
     */
    public boolean clickOnPromotionDeleteButton() {
        LOGGER.info("Delete Promotion");
        deleteAndConfirm();
        return getHeader().equalsIgnoreCase("Promotion Deleted");
    }

    /**
     * method to click on Clone button
     */
    public AddPromotionPage clickOnCloneButton() {

        cloneButton.click();
        LOGGER.info("Click on Clone Button");

        return super.getPage(AddPromotionPage.class);
    }

    /**
     * method to click on Cancel button
     *
     * @return PromotionDetailsPage
     */
    public PromotionDetailsPage clickOnCancelButton() {

        cancelButton.click();
        LOGGER.info("Click on Cancel Button");
        return super.getPage(PromotionDetailsPage.class);
    }

    /**
     * Edit the Promotion
     */
    public EditPromotionPage clickOnEdit() {
        LOGGER.info("Edit Promtoion");
        editButton.click();
        return super.getPage(EditPromotionPage.class);
    }

    /**
     * Method to return grid containing Descriptors page
     */
    public GenericGrid getGrid() {
        return super.getPage(GenericGrid.class);
    }

    /**
     * Method to validate bundle promotion details is displayed in the promo details page This method looks for the
     * visibility of elements 1. Offer Details Table 2. Offer Name Table Header 3. Offer External Key Table Header 4.
     * Offer Quantity Table Header 5. Offer Apply Discount Table Header
     */
    public boolean isBundlePromoOfferDetailsPresent() {
        return (isElementPresent(offerDetailsTable) && isElementPresent(offerNameHeader)
            && isElementPresent(offerExternalKeyHeader) && isElementPresent(offerQuantityHeader)
            && isElementPresent(offerApplyDiscountHeader));
    }

    /**
     * Method to get presence of subscription offer
     *
     * @return subscription table title
     */
    public boolean isSubscriptionOfferPresent() {
        boolean isSubscriptionOfferPresent = false;
        try {
            isSubscriptionOfferPresent = subscriptionOffer.isDisplayed();
            LOGGER.info("Subscription Offer Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Subscription Offer Doesn't Exist");
        }
        return isSubscriptionOfferPresent;
    }

    /**
     * Method to get presence of basic offering
     *
     * @return basic offering table
     */
    public boolean isBasicOfferingPresent() {
        boolean isBasicOfferingPresent = false;
        try {
            isBasicOfferingPresent = basicOffering.isDisplayed();
            LOGGER.info("Basic Offerings Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Basic Offerings Doesn't Exist");
        }
        return isBasicOfferingPresent;
    }

    /**
     * method to return value of given column under discount amount table
     *
     * @param columnName
     * @return value of column
     */
    private String getCashAmountDetailsByColumnName(final String columnName) {
        int columnIndex = 1;
        boolean foundColumn = false;

        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElements();
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
        final String selector = ".find-results-inner >" + TABLE_DETAILS_BY_COLUMN_SELECTOR + "(" + columnIndex + ")";
        final WebElement webElement = getDriver().findElement(By.cssSelector(selector));
        return webElement.getText();
    }

    private List<WebElement> getHeaderElements() {
        return getDriver().findElements(By.xpath(HEADER_ELEMENTS_OF_DISCOUNT_AMOUT_SECTION));
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
    public List<String> getDiscountAmountDetails(final String columnName) {
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
    public List<String> getSubscriptionOffersDetails(final String columnName) {
        final int columnIndex = getColumnIndex(getSubscriptionOffersHeaderElements(), columnName) + 1;
        return getPromotionOfferingDetails(SUBSCRIPTION_OFFERS_TABLE_XPATH, columnIndex);
    }

    /**
     * Method to get Offering Name column values as list from Basic Offerings section.
     *
     * @return List<String>
     */
    public List<String> getBasicOfferingsNameList() {
        return getBasicOfferingDetails("Offering Name");
    }

    /**
     * Method to get Offer Name column values as list from Subscription Offerings section.
     *
     * @return List<String>
     */
    public List<String> getSubscriptionOffersNameList() {
        return getSubscriptionOffersDetails("Offer Name");
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
