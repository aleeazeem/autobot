package com.autodesk.bsm.pelican.ui.pages.purchaseorder;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.NamedPartyType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page represents FindPurchaseOrdersPage
 *
 * @author t_joshv
 */
public class FindPurchaseOrdersPage extends GenericDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindPurchaseOrdersPage.class.getSimpleName());

    @FindBy(id = "orderType")
    private WebElement orderTypeSelect;

    @FindBy(css = ".form-group-labels > h3:nth-child(4)")
    private WebElement advancedFindTab;

    @FindBy(id = "orderState")
    private WebElement orderStateSelect;

    @FindBy(id = "fulfillmentStatus")
    private WebElement fulfillmentStatusSelect;

    @FindBy(xpath = ".//*[@id='input-name']")
    private WebElement buyerNameInput;

    @FindBy(id = "type")
    private WebElement type;

    @FindBy(xpath = ".//*[@id='field-buyerId']/a")
    private WebElement searchButton;

    @FindBy(id = "input-createdAfter")
    private WebElement createdAfterInput;

    @FindBy(id = "input-createdBefore")
    private WebElement createdBeforeInput;

    @FindBy(id = "input-modifiedAfter")
    private WebElement inputModifiedAfterInput;

    @FindBy(id = "input-modifiedBefore")
    private WebElement inputModifiedBeforeInput;

    @FindBy(css = ".check")
    private WebElement selectNamedPartiesCheckBox;

    @FindBy(xpath = ".//*[@id='find-results']//button[@type='submit']")
    private WebElement selectNamedPartyButton;

    public FindPurchaseOrdersPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * Navigate to find by Id
     *
     * @param purchaseOrderId
     * @return Purchase Order Detail page
     */
    public void findPurchaseOrderById(final String purchaseOrderId) {
        navigateToFindPurchaseOrders();
        setId(purchaseOrderId);
    }

    /**
     * click on submit button on Find Purchase Order By ID.
     */
    public PurchaseOrderDetailPage clickOnSubmit() {
        submit();
        return getPage(PurchaseOrderDetailPage.class);
    }

    /**
     * This is a method to set Order Type.
     */
    private void setOrderType(final String orderType) {
        if (orderType != null) {
            getActions().select(orderTypeSelect, orderType);
        }
    }

    /***
     * Method to find purchase order by advance field.
     *
     * @param orderType
     * @param orderState
     * @param fulfillmentStatus
     * @param buyerName
     * @param namedPartyType
     * @param createdAfter
     * @param createdBefore
     * @param modifiedAfter
     * @param modifiedBefore
     */
    public void findPurchaseOrderByAdvanceField(final String orderType, final String orderState,
        final String fulfillmentStatus, final String buyerName, final NamedPartyType namedPartyType,
        final String createdAfter, final String createdBefore, final String modifiedAfter,
        final String modifiedBefore) {

        navigateToFindPurchaseOrders();
        selectAdvancedFind();
        setOrderType(orderType);
        setOrderState(orderState);
        setFulfillmentStatus(fulfillmentStatus);
        setBuyerUserName(buyerName, namedPartyType);
        setCreatedAfterAndCreatedBefore(createdAfter, createdBefore);
        setModifiedAfterAndModifiedBefore(modifiedAfter, modifiedBefore);
    }

    /**
     * Method to click on submit button on Find Purchase Order by Advance Field.
     *
     * @return
     */
    public PurchaseOrderSearchResultPage clickSubmitOnFindPurchaeOrderByAdvanceField() {
        submit(3);
        return getPage(PurchaseOrderSearchResultPage.class);
    }

    /**
     * Method to click on Advance Find Tab.
     */
    private void selectAdvancedFind() {
        getActions().click(advancedFindTab);
    }

    /**
     * set Modified After and Before.
     */
    private void setModifiedAfterAndModifiedBefore(final String modifiedAfter, final String modifiedBefore) {
        if (modifiedAfter != null) {
            getActions().setText(inputModifiedAfterInput, modifiedAfter);
        }
        if (modifiedBefore != null) {
            getActions().setText(inputModifiedBeforeInput, modifiedBefore);
        }
    }

    /**
     * set created after and before.
     */
    private void setCreatedAfterAndCreatedBefore(final String createdAfter, final String createdBefore) {

        if (createdAfter != null) {
            getActions().setText(createdAfterInput, createdAfter);
        }
        if (createdBefore != null) {
            getActions().setText(createdBeforeInput, createdBefore);
        }
    }

    /**
     * This is a method to set Order State.
     */
    private void setOrderState(final String orderState) {
        if (orderState != null) {
            getActions().select(orderStateSelect, orderState);
        }
    }

    /**
     * Method to select fulfillment status.
     *
     * @param fulfillmentStatus
     */
    private void setFulfillmentStatus(final String fulfillmentStatus) {
        if (StringUtils.isNotEmpty(fulfillmentStatus)) {
            getActions().select(fulfillmentStatusSelect, fulfillmentStatus);
        }
    }

    /**
     * Navigate to FindPurchaseOrders Page.
     */
    private void navigateToFindPurchaseOrders() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.PURCHASE_ORDER.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to set the filter the buyer by buyer name and type
     */
    private void setBuyerUserName(final String buyerName, final NamedPartyType partyType) {
        if (buyerName != null) {
            LOGGER.info("Set Buyer Name to " + buyerName + "");

            getActions().click(searchButton);
            // Saving the parent window handle before switching to the new window
            final String parentWindowHandle = getDriver().getWindowHandle();
            final String handle = getDriver().getWindowHandles().toArray()[1].toString();
            getDriver().switchTo().window(handle);
            Util.waitInSeconds(TimeConstants.ONE_SEC);
            getActions().setText(buyerNameInput, buyerName);

            // If the buyer type is specified
            if (null != partyType) {
                getActions().selectByValue(type, partyType.getValue());
            }
            submit();
            getActions().click(selectNamedPartiesCheckBox);
            getActions().click(selectNamedPartyButton);

            // Switching back to the parent window.
            getDriver().switchTo().window(parentWindowHandle);
        }
    }
}
