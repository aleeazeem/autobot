package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * This class is a page object for Edit Basic Offering page
 *
 * @author t_mohag
 */
public class EditBasicOfferingPage extends GenericDetails {

    public EditBasicOfferingPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "appFamilyId")
    private WebElement appFamilyIdSelect;

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "productLineId")
    private WebElement productLineSelect;

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

    @FindBy(className = "delete-button")
    private WebElement deletePriceButton;

    @FindBy(xpath = ".//*[@class='atc-action']//*[@type='submit']")
    private WebElement editOfferingButton;

    private static final String CHOOSE_ONE = "-- CHOOSE ONE --";

    /**
     * Edit a basic offering with multiple parameters This method is only for offering Info section
     */
    public void editBasicOfferingInfo(final String name, final String externalKey, final OfferingType offeringType,
        final Status status, final CancellationPolicy cancellationPolicy, final String offeringDetail,
        final String productLine) {

        editName(name);
        editExternalKey(externalKey);
        editOfferingType(offeringType);
        editStatus(status);
        editOfferingDetail(offeringDetail);
        editProductLine(productLine);
    }

    /**
     * Edit a basic offering with multiple parameters This method is only for offering Info section
     */
    public void editBasicOfferingPrice(final String priceAmount, final String startDate, final String endDate) {
        editPriceAmount(priceAmount);
        editStartDate(startDate);
        editEndDate(endDate);
    }

    private void editPriceAmount(final String priceAmountVal) {
        if (priceAmountVal != null) {
            getActions().setText(priceAmount, priceAmountVal);
        }
    }

    private void editStartDate(final String startDate) {
        if (startDate != null) {
            getActions().setText(startDatePicker, startDate);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    private void editEndDate(final String endDate) {
        if (endDate != null) {
            getActions().setText(endDatePicker, endDate);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Edit name field of a basic offering
     */
    private void editName(final String name) {

        getActions().setText(nameInput, name);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Edit external key field of a basic offering
     */
    public void editExternalKey(final String externalKey) {

        externalKeyInput.clear();
        getActions().setText(externalKeyInput, externalKey);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Edit offering type of a basic offering
     */
    private void editOfferingType(final OfferingType offeringType) {

        getActions().select(offeringTypeSelect, offeringType.toString());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * Edit status of a basic offering
     */
    public void editStatus(final Status status) {

        getActions().select(statusSelect, status.getDisplayName());
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Edit offering detail of a basic offering
     */
    private void editOfferingDetail(final String offeringDetail) {

        getActions().select(offeringDetailIdSelect, offeringDetail);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Edit product line of a basic offering
     */
    public void editProductLine(final String productLine) {

        if (productLine != null) {
            getActions().select(productLineSelect, productLine);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        } else {
            getActions().select(productLineSelect, CHOOSE_ONE);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
        }
    }

    /**
     * Click on the save button of the basic offering which is inherited from Generic Page This method is written so
     * that any test class can use this
     *
     * @return BasicOfferingDetailPage
     */
    public BasicOfferingDetailPage clickOnSave() {
        submit(TimeConstants.ONE_SEC);
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
     * Click on the delete price button of the basic offering
     */
    public void clickOnDeletePriceButton() {
        deletePriceButton.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    public void clickOnEditOfferingButton() {
        editOfferingButton.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }
}
