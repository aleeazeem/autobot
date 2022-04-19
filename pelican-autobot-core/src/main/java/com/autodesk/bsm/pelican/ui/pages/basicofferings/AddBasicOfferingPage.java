package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.joda.time.LocalDateTime;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a page object for Add Basic Offering page
 *
 * @author t_mohag
 */
public class AddBasicOfferingPage extends GenericDetails {

    public AddBasicOfferingPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "appFamilyId")
    private WebElement appFamilyIdSelect;

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "input-name")
    private WebElement nameInput;

    @FindBy(id = "mediaType")
    private WebElement mediaTypeSelect;

    @FindBy(id = "input-languageCode")
    private WebElement languageCode;

    @FindBy(id = "offeringDetailId")
    private WebElement offeringDetailIdSelect;

    @FindBy(id = "status")
    private WebElement statusSelect;

    @FindBy(id = "usageType")
    private WebElement usageTypeSelect;

    @FindBy(id = "currencyId")
    private WebElement currencyIdSelect;

    @FindBy(id = "input-currencyAmount")
    private WebElement amountInput;

    @FindBy(id = "storeSelect")
    private WebElement storeSelect;

    @FindBy(id = "priceListSelect")
    private WebElement priceListSelect;

    @FindBy(id = "addPrice")
    private WebElement addPriceButton;

    @FindBy(className = "priceAmount")
    private WebElement priceAmount;

    @FindBy(className = "startDatePicker")
    private WebElement startDatePicker;

    @FindBy(className = "endDatePicker")
    private WebElement endDatePicker;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddBasicOfferingPage.class.getSimpleName());

    /**
     * Add a Basic Offering Info section in Add Basic Offering Page This method is only for Basic Offering Info section
     *
     * @param offeringType
     * @param productLine
     * @param name
     * @param externalKey
     * @param mediaType
     * @param OfferingDetail
     * @param status
     * @param usageType
     * @param currency
     * @param amount
     */
    public void addBasicOfferingInfo(final OfferingType offeringType, final String productLine, final String name,
        final String externalKey, final MediaType mediaType, final String OfferingDetail, final Status status,
        final UsageType usageType, final String currency, final String amount) {

        navigateToAddBasicOffering();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        addOfferingType(offeringType);
        selectProductLine(productLine);
        addName(name);
        setExternalKey(externalKey);
        if (offeringType.getDisplayName().equals(OfferingType.PERPETUAL.getDisplayName())) {
            addMediaType(mediaType);
        }
        if (offeringType.getDisplayName().equals(OfferingType.CURRENCY.getDisplayName())) {
            addCurrencyType(currency);
            addAmount(amount);
        }
        addOfferingDetail(OfferingDetail);
        addUsageType(usageType);
        addStatus(status);
    }

    /**
     * Add prices to the basic offering
     *
     * @param numberOfPrices
     * @param store
     * @param priceList
     * @param amount
     * @param startDate
     * @param endDate
     */
    public void addPrices(final int numberOfPrices, final String store, final String priceList, final String amount,
        final String startDate, final String endDate) {

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        LOGGER.info("Number of Prices are going to be added: " + numberOfPrices);
        for (int i = 0; i < numberOfPrices; i++) {
            selectStoreId(store);
            selectPriceList(priceList);
            setAmount(amount);
            setStartDate(startDate);
            setEndDate(endDate);
            LOGGER.info("Adding a price to basic offering is completed ");
        }
    }

    /**
     * This method adds offering type
     */
    private void addOfferingType(final OfferingType offeringType) {

        if (offeringType != null) {

            getActions().select(offeringTypeSelect, offeringType.getDisplayName());
            LOGGER.info("Set Basic Offering type to : " + offeringType.toString());
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Add name field of a basic offering
     */
    private void addName(final String name) {

        if (name != null) {
            getActions().setText(nameInput, name);
            LOGGER.info("Set Basic Offering name to : " + name);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            getActions().setText(nameInput, "BasicOffering" + LocalDateTime.now());
        }
    }

    /**
     * Add media type of a basic offering
     */
    private void addMediaType(final MediaType mediaType) {

        if (mediaType != null) {
            getActions().select(mediaTypeSelect, mediaType.getValue());
            LOGGER.info("Set Basic Offering media type to : " + mediaType.toString());
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Add usage type of a basic offering
     */
    private void addUsageType(final UsageType usageType) {

        if (usageType != null) {
            getActions().select(usageTypeSelect, usageType.toString());
            LOGGER.info("Set Basic Offering usage type to : " + usageType.toString());
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Add currency type of a basic offering
     */
    private void addCurrencyType(final String currency) {

        if (currency != null) {
            getActions().select(currencyIdSelect, currency);
            LOGGER.info("Set Basic Offering currency type to : " + currency);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Add amount of a currency offering
     */
    private void addAmount(final String amount) {

        if (amount != null) {
            getActions().setText(amountInput, amount);
            LOGGER.info("Set Basic Offering amount to : " + amount);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Add status of a basic offering
     */
    private void addStatus(final Status status) {

        if (status != null) {
            getActions().select(statusSelect, status.getDisplayName());
            LOGGER.info("Set Basic Offering status to : " + status.getDisplayName());
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Add offering detail of a basic offering
     */
    private void addOfferingDetail(final String offeringDetail) {

        if (offeringDetail != null) {
            getActions().select(offeringDetailIdSelect, offeringDetail);
            LOGGER.info("Set Basic Offering detail to : " + offeringDetail);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * This method selects store while adding price
     *
     * @param store
     */
    private void selectStoreId(final String store) {
        getActions().select(storeSelect, store);
        LOGGER.info("Selected store is: " + store);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * This method selects pricelist for the price
     *
     * @param priceList
     */
    private void selectPriceList(final String priceList) {

        getActions().select(priceListSelect, priceList);
        LOGGER.info("Selected Price List is: " + priceList);
        getActions().click(addPriceButton);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * This method sets amount while adding a price
     *
     * @param amount
     */
    private void setAmount(final String amount) {

        getActions().setText(priceAmount, amount);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
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
        Util.waitInSeconds(TimeConstants.THREE_SEC);
    }

    /**
     * This method sets end date for the price
     *
     * @param endDate
     */
    private void setEndDate(final String endDate) {

        getActions().setText(endDatePicker, endDate);
        LOGGER.info("Price End Date is set to: " + endDate);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Click on the save button of the basic offering which is inherited from Generic Page This method is written so
     * that any test class can use this
     *
     * @return BasicOfferingDetailPage
     */
    public BasicOfferingDetailPage clickOnSave() {
        submit(TimeConstants.THREE_SEC);
        return super.getPage(BasicOfferingDetailPage.class);
    }

    /**
     * Click on the cancel button of the basic offering which is inherited from Generic Page This method is written so
     * that any test class can use this
     */
    public void clickOnCancel() {
        cancel();
    }

    /**
     * Navigating to add basic offering
     */
    public void navigateToAddBasicOffering() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BASIC_OFFERINGS.getForm() + "/"
            + AdminPages.ADD_FORM.getForm();
        getDriver().get(url);
    }

    /**
     * Method to get all product lines available under drop down menu.
     *
     * @return ProductLine List
     */
    public List<String> getProductLinesListUnderProductLineDropDown() {

        final Select select = new Select(productLineSelect);

        final List<String> productLineList = new ArrayList<>();
        final List<WebElement> optionsList = select.getOptions();
        LOGGER.info("Total Available Product Lines: " + optionsList.size());
        for (final WebElement element : optionsList) {
            productLineList.add(element.getText());
        }
        return productLineList;
    }
}
