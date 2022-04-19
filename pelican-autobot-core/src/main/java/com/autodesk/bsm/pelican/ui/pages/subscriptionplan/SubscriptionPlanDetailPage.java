package com.autodesk.bsm.pelican.ui.pages.subscriptionplan;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.LicensingModelDetailPage;
import com.autodesk.bsm.pelican.util.Util;
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
import java.util.Arrays;
import java.util.List;

/**
 * @ Author: Vineel Reddy
 */
public class SubscriptionPlanDetailPage extends SubscriptionPlanGenericPage {

    @FindBy(name = "Edit Subscription Plan")
    private WebElement editSubscriptionPlanButton;

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

    @FindBy(xpath = "//*[@id=\"oneTimeEntitlements\"]/div/div/table/tbody/tr[1]/td[5]/a")
    private WebElement licensingModel;

    @FindBy(id = "subscription-plan-info")
    private WebElement subscriptionPlanInfo;

    @FindBy(xpath = "//td[text()='Yes']/preceding-sibling::th")
    private List<WebElement> yesDescriptors;

    @FindBy(xpath = "//td[text()='No']/preceding-sibling::th")
    private List<WebElement> noDescriptors;

    @FindBy(xpath = "//input[contains(@name,'ipp')]")
    private WebElement ippDescriptors;

    @FindBy(xpath = "//*[@id='prices-table[0]']/tbody/tr/td[1]")
    private WebElement priceId;

    @FindBy(xpath = ".//*[@id='prices-table[0]']/tbody/tr/td[5]")
    private WebElement effectiveStartDate;

    @FindBy(xpath = ".//*[@id='prices-table[0]']/tbody/tr/td[6]")
    private WebElement effectiveEndDate;

    @FindBy(className = "field-group subscription-fieldset")
    private List<WebElement> subscriptionPlanMetaData;

    @FindBy(linkText = "View Offer Descriptors")
    private WebElement viewOfferDescriptors;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanDetailPage.class.getSimpleName());
    private String parentElementSelector;

    public SubscriptionPlanDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        this.parentElementSelector = "";
    }

    /**
     * This method navigates to Subscription Plan Page
     */
    public void navigateToSubscriptionPlanPage(final String id) {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION_PLAN.getForm() + "/show?id=" + id;
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
     * Get Subscription Plan id
     */
    public String getId() {

        final String id = getValueByFieldInSubscriptionPlan("ID");
        LOGGER.info("Subscription Plan Id : " + id);
        return id;
    }

    public String getName() {

        final String name = getValueByFieldInSubscriptionPlan("Name");
        LOGGER.info("Subscription Plan Name : " + name);
        return name;
    }

    /**
     * Get Subscription Plan external key
     */
    public String getExternalKey() {

        final String externalKey = getValueByFieldInSubscriptionPlan("External Key");
        LOGGER.info("Subscription Plan External Key : " + externalKey);
        return externalKey;
    }

    /**
     * Get Subscription Plan offering type
     */
    public String getOfferingType() {
        return getValueByFieldInSubscriptionPlan("Offering Type");
    }

    /**
     * Get Subscription Plan status
     */
    public String getStatus() {
        return getValueByFieldInSubscriptionPlan("Status");
    }

    /**
     * Get Subscription Plan cancellation policy
     */
    public String getCancellationPolicy() {
        return getValueByFieldInSubscriptionPlan("Cancellation Policy");
    }

    /**
     * Get Subscription Plan usage type
     */
    public String getUsageType() {
        return getValueByFieldInSubscriptionPlan("Usage Type");
    }

    /**
     * Get Subscription Plan packaging type
     */
    public String getPackagingType() {
        return getValueByFieldInSubscriptionPlan("Packaging Type");
    }

    /**
     * Get Subscription Plan support level
     */
    public String getSupportLevel() {
        return getValueByFieldInSubscriptionPlan("Support Level");
    }

    /**
     * Get Subscription Plan offering detail
     */
    public String getOfferingDetail() {
        return getValueByFieldInSubscriptionPlan("Offering Detail");
    }

    /**
     * Get Subscription Plan tax code
     */
    public String getTaxCode() {
        return getValueByFieldInSubscriptionPlan("Tax Code");
    }

    /**
     * Get Subscription Plan product line
     */
    public String getProductLine() {
        return getValueByFieldInSubscriptionPlan("Product Line");
    }

    /**
     * Get Subscription Plan whether is module
     */
    public String getIsModule() {
        return getValueByFieldInSubscriptionPlan("Is Module");
    }

    /**
     * Get Subscription Plan modules
     */
    public String getModules() {
        return getValueByFieldInSubscriptionPlan("Modules");
    }

    /**
     * Get Send Expiration Reminder Emails
     */
    public String getSendExpirationReminderEmails() {
        return getValueByFieldInSubscriptionPlan("Send Expiration Reminder Emails");
    }

    /**
     * Get Subscription Plan entitlement frequency
     */
    public String getEntitlementFrequency() {
        return getValueByFieldInSubscriptionPlan("Entitlement Frequency");
    }

    /**
     * This is a method which returns the entitlement details from the plan detail page
     *
     * @param entitlementId
     * @param columnIndex
     * @return Feature details - String
     */
    public String getEntitlementDetails(final String entitlementId, final int columnIndex) {

        final String xpath = String.format("//*[@id=\"oneTimeEntitlement_%s\"]//td[%d]", entitlementId, columnIndex);
        final WebElement element = driver.findElement(By.xpath(xpath));
        return element.getText();
    }

    /**
     * Click on the edit subscription plan button
     */
    public EditSubscriptionPlanPage clickOnEditSubscriptionPlanButton() {
        Util.scroll(driver, "1300", "0");
        if (editSubscriptionPlanButton.isDisplayed()) {
            getActions().click(editSubscriptionPlanButton);
        }
        Wait.pageLoads(getDriver());
        return super.getPage(EditSubscriptionPlanPage.class);
    }

    /**
     * This is the method to return the feature type
     *
     * @return feature type value
     */
    public String getFeatureType() {
        return (getValueOfNonEditableFeatureFields("type"));
    }

    /**
     * This is the method to return the feature id
     *
     * @return feature id value
     */
    public String getFeatureId() {
        return (getValueOfNonEditableFeatureFields("feature"));
    }

    /**
     * This is the method to return the feature name
     *
     * @return feature name value
     */
    public String getFeatureName() {
        return (getValueOfNonEditableFeatureFields("feature name"));
    }

    /**
     * This is the method to return the name of the licensing model
     *
     * @return licensing model name value
     */
    public String getLicensingModelName() {
        return (getValueOfNonEditableFeatureFields("licensing model"));
    }

    /**
     * This is the method to return the name of the core products
     *
     * @return core products name value
     */
    public String getCoreProductsName() {
        return (getValueOfNonEditableFeatureFields("core products"));
    }

    /**
     * method to check the presence of edit subscription plan button
     */
    public boolean isEditSubscriptionPlanButtonPresent() {
        boolean isEditSubscriptionPlanButtonPresent = false;
        try {
            isEditSubscriptionPlanButtonPresent = editSubscriptionPlanButton.isDisplayed();
            LOGGER.info("Edit subsacription plan button Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Edit subsacription plan button Doesn't Exist");
        }
        return isEditSubscriptionPlanButtonPresent;
    }

    /**
     * Method to click on Edit Localized Descriptors button
     *
     * @return EditSubscriptionPlanAndOfferDescriptorsPage
     */
    public EditSubscriptionPlanAndOfferDescriptorsPage clickOnEditLocalizedDescriptors() {
        Util.scroll(driver, "1000", "0");
        getActions().click(editLocalizedDescriptors);
        Wait.pageLoads(getDriver());
        return super.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
    }

    /**
     * Method to click on Edit NonLocalized Descriptors button
     *
     * @return EditSubscriptionPlanAndOfferDescriptorsPage
     */
    public EditSubscriptionPlanAndOfferDescriptorsPage clickOnEditNonLocalizedDescriptors() {
        Util.scroll(driver, "1000", "0");
        getActions().click(editNonLocalizedDescriptors);
        Wait.pageLoads(getDriver());
        return super.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
    }

    /**
     * Method to set Other Locale text field
     */
    public void setOtherLocales(final String text) {
        LOGGER.info("Select locale ' " + text + "'");
        getActions().select(this.localeSelect, text);
    }

    /**
     * Method returns value in locale field
     */
    public String getFirstSelectedSelectValue() {
        LOGGER.info("Selected value '");
        final Select select = new Select(this.localeSelect);
        final WebElement selectedValue = select.getFirstSelectedOption();
        return selectedValue.getText();
    }

    /**
     * Method to set Language in Choose Locale page
     */
    public void setLanguague(final String language) {
        LOGGER.info("Select language '" + language + "'");
        getActions().select(this.languageSelect, language);
    }

    /**
     * Method to set Country in Choose Locale page
     */
    public void setCountry(final String country) {
        LOGGER.info("Select country '" + country + "'");
        getActions().select(this.countrySelect, country);
    }

    /**
     * Method to edit Localized/Non-Localized Descriptor value
     */
    public void editDescriptorValue(final String descriptorsName, final String descriptorValue) {
        final String xpath = "//div/label[text()='" + descriptorsName + ":']/following-sibling::span/input";
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
     * Method to return list of titles for all descriptors
     *
     * @return textList(list of descriptor names list)
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
     * method to get value of the field displayed in the subscription plan page
     *
     * @return value of the field
     */
    private String getValueByFieldInSubscriptionPlan(final String field) {
        final String selector = ".//*[@id='subscription-plan-info']//label[contains(text(),'" + field
            + ":')]/following-sibling" + "::div[@class='value']";
        final WebElement cell = getDriver().findElement(By.xpath(selector));
        return cell.getText();
    }

    /**
     * This is the method to get the value of non editable feature fields
     *
     * @param fieldName
     * @return String - value of the field
     */
    private String getValueOfNonEditableFeatureFields(final String fieldName) {
        final String selector =
            ".//*[@id='subscription-plan-info']//*[@class='oneTimeEntitlement-static']//label[contains(text(),'"
                + fieldName + ":')]/following-sibling::div[@class='field']";

        return getDriver().findElement(By.xpath(selector)).getText();
    }

    /**
     * method to get price id of a price in an offer
     */
    public String getPriceId() {
        return priceId.getText();
    }

    /**
     * method to return Licensing Model Name
     *
     * @return Licensing Model Name
     */
    public List<String> getLicensingModel() {
        return super.getPage(GenericGrid.class).getColumnValues("Licensing Model");
    }

    /**
     * method to click Licensing Model
     *
     * @return Licensing Model Name
     */
    public LicensingModelDetailPage clickOnLicensingModel() {

        getActions().click(licensingModel);
        return super.getPage(LicensingModelDetailPage.class);
    }

    /**
     * method to get effective start date of a price in an offer
     */
    public String getEffectiveStartDateOfPriceInAnOffer() {
        return effectiveStartDate.getText();
    }

    /**
     * method to get effective end date of a price in an offer
     */
    public String getEffectiveEndDateOfPriceInAnOffer() {
        return effectiveEndDate.getText();
    }

    /**
     * Click on the view offer descriptors link
     *
     * @return SubscriptionOfferDetailPage
     */
    public SubscriptionOfferDetailPage clickOnViewOfferDescriptors() {
        Util.scroll(driver, "1000", "0");
        getActions().click(viewOfferDescriptors);
        Wait.pageLoads(getDriver());
        return super.getPage(SubscriptionOfferDetailPage.class);
    }

    /**
     * method to get offer external key
     *
     * @return offer external key
     */
    public String getOfferExternalKey(final int offerIndex) {
        return getOfferValueByColumnName(offerIndex, "External Key");
    }

    /**
     * method to get offer name
     *
     * @return offer name
     */
    public String getOfferName(final int offerIndex) {
        return getOfferValueByColumnName(offerIndex, "Name");
    }

    /**
     * method to get offer status
     *
     * @return offer status
     */
    public String getOfferStatus(final int offerIndex) {
        return getOfferValueByColumnName(offerIndex, "Status");
    }

    /**
     * method to get offer billing frequency
     *
     * @return offer billing frequency
     */
    public String getOfferBillingFrequency(final int offerIndex) {
        return getOfferValueByColumnName(offerIndex, "Billing Frequency");
    }

    /**
     * Method to get total number of offers in a subscription plan
     *
     * @return int offerCount
     */
    public int getOfferCount() {
        int offerCount;
        final List<WebElement> offers = getDriver().findElements(By.cssSelector("#subscriptionOffers > div"));
        offerCount = offers.size();
        LOGGER.info("Total number of offers in subscription plan: " + offerCount);
        return offerCount;
    }

    /**
     * Method to get all column values of External Key under OneTimeEntitlement
     *
     * @return List<String>
     */
    public List<String> getOneTimeEntitlementExternalKeyColumnValues() {
        return super.getPage(GenericGrid.class).getColumnValues("External Key");
    }

    /**
     * Method to get all column values of Name or Account under OneTimeEntitlement
     *
     * @return List<String>
     */
    public List<String> getOneTimeEntitlementNameOrAmountColumnValues() {
        return super.getPage(GenericGrid.class).getColumnValues("Name or Amount", 1);
    }

    /**
     * Method to get all column values of Feature Type under OneTimeEntitlement
     *
     * @return List<String>
     */
    public List<String> getOneTimeEntitlementFeatureTypeColumnValues() {
        return super.getPage(GenericGrid.class).getColumnValues("Feature Type", 1);
    }

    /**
     * Method to get all column values of Licensing Model under OneTimeEntitlement
     *
     * @return List<String>
     */
    public List<String> getOneTimeEntitlementLicensingModelColumnValues() {
        return super.getPage(GenericGrid.class).getColumnValues("Licensing Model", 1);
    }

    /**
     * Method to get all column values of Core Product under OneTimeEntitlement
     *
     * @return List<String>
     */
    public List<String> getOneTimeEntitlementCoreProductColumnValues() {
        return super.getPage(GenericGrid.class).getColumnValues("Core Product(s)");
    }

    /**
     * Method to check whether entitlement row is expanded
     *
     * @param itemId
     * @return
     */
    public boolean isEntitlementRowExpanded(final String itemId) {
        final WebElement row1 = driver.findElement(By.id("row" + itemId));
        return row1.isDisplayed();
    }

    /**
     * Method to get all one time entitlement ids
     *
     * @return
     */
    public String[] getOneTimeEntitlementIds() {
        final WebElement oneTimeEntitlementIds = driver.findElement(By.id("oneTimeEntitlementsIds"));
        final String[] result = oneTimeEntitlementIds.getAttribute("value").split(",");
        LOGGER.info("getOneTimeEntitlementIds : " + Arrays.toString(result));
        return result;
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
     * This is a method to return the data from the collapsable section
     *
     * @param entitlementId
     * @param rowIndex
     * @param columnIndex
     * @return Data as String
     */
    public String getDataFromFeatureCollapsableSection(final String entitlementId, final int rowIndex,
        final int columnIndex) {
        final String collpsableRowPath = "row" + entitlementId;
        final String xpath = ".//*[@id='" + collpsableRowPath + "']//tr[" + rowIndex + "]//td[" + columnIndex + "]";
        final WebElement element = driver.findElement(By.xpath(xpath));

        return element.getText();
    }

    /**
     * develop Method to get href value attached to See Audit Trail Link.
     *
     * @return actual href value linked to See Audit Trail link.
     */
    public String getAuditTrailLink() {
        return auditTrailLink.getAttribute("href");
    }

    /**
     * Method to generate Audit Trail Link for provided Subscription Plan Entity.
     *
     * @param entityId
     * @return expected href value
     */
    public String generateAuditTrailLink(final String entityId) {
        final String suffixUrl = "?entityType=SubscriptionPlan&entityId=" + entityId;
        return getEnvironment().getAdminUrl() + "/" + AdminPages.REPORTS.getForm() + "/"
            + AdminPages.AUDIT_LOG.getForm() + suffixUrl;
    }

    /**
     * Method to get value of column in an Offer
     *
     * @return value by column
     */
    private String getOfferValueByColumnName(final int offerIndex, final String columnName) {
        final String selector =
            ".//*[@id='offers-table[" + (offerIndex - 1) + "]']/tbody/tr/td[" + (getColumnIndex(columnName)) + "]";
        final WebElement element = getDriver().findElement(By.xpath(selector));
        LOGGER.info("Subscription Offer " + columnName + " is found: " + element.getText());
        return element.getText();
    }

    /**
     * method to get index of column by Name in Offer table in subscription plan Detail Page
     *
     * @return index of column
     */
    private int getColumnIndex(final String columnName) {
        int columnIndex = 0;
        boolean foundColumn = false;
        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElementsOfOffer();

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
        return (columnIndex + 1);
    }

    /**
     * Method to get header element of an offer
     *
     * @return element
     */
    private List<WebElement> getHeaderElementsOfOffer() {
        return getDriver().findElements(By.xpath(parentElementSelector + ".//*[@id='offers-table[0]']/thead/tr/th"));
    }

}
