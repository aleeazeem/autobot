package com.autodesk.bsm.pelican.ui.pages.purchaseorder;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.List;
import java.util.Set;

/**
 * Page class for purchase order detail page.
 *
 * @author jains
 *
 */
public class PurchaseOrderDetailPage extends GenericDetails {

    @FindBy(xpath = "/html/body/div/div[2]/div[2]/div[2]/div/div/table/tbody/tr/td[2]/div/p/span")
    private WebElement subscriptionId;

    @FindBy(xpath = ".//*[@id='line-items']/div/div/table/tfoot[2]/tr/td/div/strong")
    private WebElement totalOrderAmount;

    @FindBy(name = "Mark as Refunded")
    private WebElement markAsRefundedButton;

    @FindBy(id = "markRefunded-notes")
    private WebElement notesForMarkAsRefundedTextArea;

    @FindBy(id = "confirm-btn")
    private WebElement confirmMarkAsRefundedButton;

    @FindBy(name = "Refund")
    private WebElement refundButton;

    @FindBy(xpath = ".//*[@id='invoice-generation-status']/div/table/tbody/tr/td[2]")
    private WebElement invoiceGenerationStatus;

    @FindBy(xpath = ".//*[@id='fulfillment-groups']/div/div/table/tbody/tr/td[contains(text(),'"
        + PelicanConstants.CLOUD_CREDITS + "')]/following-sibling::td[2]//button")
    private WebElement cloudCreditRetriggerButton;

    @FindBy(xpath = ".//*[@id='fulfillment-groups']/div/div/table/tbody/tr/td[contains(text(),'"
        + PelicanConstants.LEGACY + "')]/following-sibling::td[2]//button")
    private WebElement legacyRetriggerButton;

    @FindBy(xpath = ".//*[@id='confirm-btn']")
    private WebElement confirmButton;

    @FindBy(className = "popup-dialog")
    private WebElement popUpDialog;

    @FindBy(linkText = "Process Order Manually")
    private WebElement processPOLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderDetailPage.class.getSimpleName());

    private String parentElementSelector;

    private String fulfillmentResponsesSelector = ".//*[@id='fulfillment-responses']/div/div/table/tbody/tr";

    public PurchaseOrderDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        this.parentElementSelector = "";
    }

    /**
     * Method to get Purchase Order Id.
     *
     * @return Purchase Order ID.
     */
    public String getPurchaseOrderId() {
        return getFieldValueByKey(PelicanConstants.ID_FIELD);
    }

    /**
     * Method to Invoice Number.
     *
     * @return Invoice Number.
     */
    public String getInvoiceNumber() {

        return getFieldValueByKey(PelicanConstants.INVOICE_NUMBER);
    }

    /**
     * Method to get Buyer.
     *
     * @return Buyer
     */
    public String getBuyer() {
        return getFieldValueByKey("Buyer");
    }

    /**
     * Method to get Buyers First Name.
     *
     * @return First Name.
     */
    public String getFirstName() {
        return getFieldValueByKey("First Name");
    }

    /**
     * Method to get Buyers Last Name.
     *
     * @return Last Name.
     */
    public String getLastName() {
        return getFieldValueByKey("Last Name");
    }

    /**
     * Method to get Buyers Email Address.
     *
     * @return
     */
    public String getEmailAddress() {
        return getFieldValueByKey("Email Address");
    }

    /**
     * Method to get Buyers Billing Address.
     *
     * @return Buyers Billing Address.
     */
    public String getBillingAddress() {
        return getFieldValueByKey("Billing Address");
    }

    /**
     * Method to get Buyers Last Four Digits.
     *
     * @return Last 4 Digits.
     */
    public String getLastFourDigits() {
        return getFieldValueByKey("Last 4 Digits");
    }

    /**
     * Method to get Buyers Phone Number.
     *
     * @return Phone Number.
     */
    public String getPhoneNumber() {
        return getFieldValueByKey("Phone Number");
    }

    /**
     * method to get line items Proration Days
     *
     * @return line item proration days
     */
    public String getLineItemsProrationDays(final int lineItemsIndex) {
        return getLineItemValueByColumnName(lineItemsIndex, "Proration Days");
    }

    /**
     * method to get Line Items Quantity.
     *
     * @return Quantity
     */
    public String getLineItemsQuantity(final int lineItemsIndex) {
        return getLineItemValueByColumnName(lineItemsIndex, "Quantity");
    }

    /**
     * method to get Line Items Unit Price.
     *
     * @return Unit Price
     */
    public String getLineItemsUnitPrice(final int lineItemsIndex) {
        return getLineItemValueByColumnName(lineItemsIndex, "Unit Price");
    }

    /**
     * method to get Line Items Promo Discounts.
     *
     * @return Promo Discounts
     */
    public String getLineItemsPromoDiscounts(final int lineItemsIndex) {
        return getLineItemValueByColumnName(lineItemsIndex, "Promo Discounts");
    }

    /**
     * method to get Line Items Sub Total.
     *
     * @return Sub Total
     */
    public String getLineItemsSubTotal(final int lineItemsIndex) {
        return getLineItemValueByColumnName(lineItemsIndex, "Subtotal");
    }

    /**
     * method to get Transaction ID.
     *
     * @return Transaction ID.
     */
    public String getTransactionId(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "ID");
    }

    /**
     * method to get Transaction EC Status.
     *
     * @return EC Status.
     */
    public String getTransactionEcStatus(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "EC Status");
    }

    /**
     * method to get Transaction TYPE.
     *
     * @return Transaction Type.
     */
    public String getTransactionType(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "Type");
    }

    /**
     * method to get Transaction Amount.
     *
     * @return Transaction Amount.
     */
    public String getTransactionAmount(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "Amount");
    }

    /**
     * method to get Transaction Date.
     *
     * @return Transaction Date.
     */
    public String getTransactionDate(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "Date");
    }

    /**
     * method to get Transaction State.
     *
     * @r eturn Transaction State.
     */
    public String getTransactionState(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "State");
    }

    /**
     * method to get Transaction Requested By.
     *
     * @return Transaction Requested By.
     */
    public String getTransactionRequestedBy(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "Requested By");
    }

    /**
     * Method to get Transaction Payment Gateway.
     *
     * @param transactionIndex
     * @return Transaction Payment Gateway.
     */
    public String getTransactionPaymentGateway(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "Payment Gateway");
    }

    /**
     * Method to get Transaction Payment Type.
     *
     * @param transactionIndex
     * @return Transaction Payment Type.
     */
    public String getTransactionPaymentType(final int transactionIndex) {
        return getTransactionValueByColumnName(transactionIndex, "Payment Type");
    }

    /**
     * method to get Fulfillment Group Id.
     *
     * @return Fulfillment Group Id.
     */
    public String getFulfillmentGroupId(final int fulFillmentGroupIndex) {
        return getFulfillmentGroupValueByColumnName(fulFillmentGroupIndex, "ID");
    }

    /**
     * method to get Fulfillment Group Strategy.
     *
     * @return Fulfillment Group Strategy.
     */
    public String getFulfillmentGroupStrategy(final int fulFillmentGroupIndex) {
        return getFulfillmentGroupValueByColumnName(fulFillmentGroupIndex, "Strategy");
    }

    /**
     * method to get Fulfillment Group Status.
     *
     * @return Fulfillment Group Status.
     */
    public String getFulfillmentGroupStatus(final int fulFillmentGroupIndex) {
        return getFulfillmentGroupValueByColumnName(fulFillmentGroupIndex, "Status");
    }

    /**
     * method to get Fulfillment Response Result.
     */
    public String getFulfillmentResponsesResult(final int fulfillmentResponseIndex) {
        return getFulfillmentResponsesByColumnName(fulfillmentResponseIndex, "Result");
    }

    /**
     * method to get Fulfillment Response Type.
     */
    public String getFulfillmentResponsesType(final int fulfillmentResponseIndex) {
        return getFulfillmentResponsesByColumnName(fulfillmentResponseIndex, "Type");
    }

    /**
     * This method returns subscription id from Purchase Order Page.
     *
     * @param lineItemIndex
     * @return String
     */
    public String getSubscriptionIdLinkedToLineItem(final int lineItemIndex) {

        final String selector = ".//*[@id='line-items']/div/div/table/tbody/tr[" + lineItemIndex + "]/td[2]/div/p/span";
        final WebElement element = getDriver().findElement(By.xpath(selector));
        final String subscriptionId = element.getText().replace("(", "").replace(")", "");
        LOGGER.info("Subscription Id : " + subscriptionId);
        return subscriptionId;
    }

    /**
     * This method returns fulfillment status from Purchase Order Page
     *
     * @return String
     */
    public String getFulfillmentStatus() {
        final String fulfillmentStatusOnOrder = getFieldValueByKey("Fulfillment Status");
        LOGGER.info("Fulfillment Status : " + fulfillmentStatusOnOrder);
        return fulfillmentStatusOnOrder;
    }

    /**
     * This method returns Order Type from Purchase Order Page
     *
     * @return String
     */
    public String getOrderType() {
        final String orderTypeOnOrder = getFieldValueByKey("Order Type");
        LOGGER.info("Purchase Order Type : " + orderTypeOnOrder);
        return orderTypeOnOrder;
    }

    /**
     * This method returns Order Status from Purchase Order Page
     *
     * @return String
     */
    public String getOrderState() {
        final String orderStateOnOrder = getFieldValueByKey("Order State");
        LOGGER.info("Purchase Order Type : " + orderStateOnOrder);
        return orderStateOnOrder;
    }

    /**
     * This method returns Order Amount from Purchase Order Page
     *
     * @return String
     */
    public String getOrderAmount() {
        final String orderAmount = getFieldValueByKey("Amount");
        LOGGER.info("Purchase Order Amount : " + orderAmount);
        return orderAmount;
    }

    /**
     * This method returns total order amount from Purchase Order Page
     *
     * @return String
     */
    public String getTotalAmountOrder() {
        final String orderAmount = totalOrderAmount.getText();
        LOGGER.info("Fulfillment Status : " + orderAmount);
        return orderAmount;
    }

    /**
     * This method returns generated Credit Note Number.
     *
     * @return String
     */
    public String getCreditNoteNumber() {
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        final String creditNoteNumber = getFieldValueByKey("Credit Note Number");
        LOGGER.info("Credit Note Number : " + creditNoteNumber);
        return creditNoteNumber;
    }

    /**
     * Click on mark as refund button in Purchase Order
     */
    public void clickMarkAsRefunded() {
        LOGGER.info("Click on MARK AS REFUNDED");
        getActions().click(markAsRefundedButton);
    }

    /**
     * Add Refund Notes.
     */
    private void addRefundNotes(final String notes) {
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        notesForMarkAsRefundedTextArea.sendKeys(notes);
    }

    /**
     * Add Refund Notes.
     */
    public PurchaseOrderDetailPage addRefundNotesAndClickConfirmationForSuccess(final String notes) {

        if (notes != null) {
            addRefundNotes(notes);
        }
        getActions().click(confirmMarkAsRefundedButton);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        return getPage(PurchaseOrderDetailPage.class);
    }

    /**
     * Method to get Initial EC Status.
     *
     * @return
     */
    public String getInitialECStatus() {
        return getFieldValueByKey("Initial EC Status");
    }

    /**
     * Method to get Final EC Status.
     *
     * @return
     */
    public String getFinalECStatus() {
        return getFieldValueByKey("Final EC Status");
    }

    /**
     * Click on refund button in Purchase Order
     */
    public void clickRefund() {
        if (checkRefund()) {
            getActions().click(refundButton);
            LOGGER.info("Click on REFUND");
        } else {
            Assert.fail("Refund button not found");
        }
    }

    /**
     * Check if Admin Tools has retrigger button enabled if order status is pending or failed for ebso\gsco users.
     *
     * @return true if button if visible else false.
     */
    public Boolean isRetriggerButtonVisible(final String strategy) {
        Boolean foundRetriggerButton = false;
        LOGGER.info("Refreshing Page to see if retrigger button is visible!");
        getDriver().navigate().refresh();
        WebElement retriggerButton;

        // set variable
        if (strategy.equals(PelicanConstants.CLOUD_CREDITS)) {
            retriggerButton = cloudCreditRetriggerButton;
        } else {
            retriggerButton = legacyRetriggerButton;
        }

        try {
            if (retriggerButton.isDisplayed()) {
                LOGGER.info("Found Retrigger Button");
                foundRetriggerButton = true;
            } else {
                LOGGER.info("No Retrigger Button Found");
            }
        } catch (final Exception e) {
            LOGGER.info("No Retrigger Button Found");
        }

        return foundRetriggerButton;
    }

    /**
     * Check if Admin Tools has refund button enabled after planning order from cart
     */
    private Boolean checkRefund() {
        int interateFinder = 1;
        final Boolean foundRefundButton = false;

        while (interateFinder <= 5) {
            LOGGER.info("Refreshing Page to see if refund button is displayed!");
            getDriver().navigate().refresh();

            try {
                if (refundButton.isDisplayed()) {
                    LOGGER.info("Found Refund Button");
                    interateFinder = 3;
                    return true;
                } else {
                    LOGGER.info("Refund button found, Order still in " + getOrderState() + " state");
                    Util.waitInSeconds(TimeConstants.MINI_WAIT);
                    interateFinder++;
                }
            } catch (final Exception e) {
                LOGGER.info("Driver Wait for Refund button: iteration " + interateFinder + " of 5");
                Util.waitInSeconds(TimeConstants.MINI_WAIT);
                interateFinder++;
            }

        }
        return foundRefundButton;
    }

    /**
     * Method to check if notes can be left blank. Returns true if empty notes are not allowed else return false.
     */
    public boolean checkEmptyNotesOnMarkAsRefundedOrder() {
        clickMarkAsRefunded();
        // entering notes
        addRefundNotesAndClickConfirmationForSuccess(null);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        if (notesForMarkAsRefundedTextArea.isDisplayed()) {

            clickOnCancelPopUpButton();
            return true;
        } else {
            LOGGER.error("Empty notes should not be allowed.");
            return false;
        }
    }

    /**
     * Method to verify Mark As Refuned is Enabled or Not for Different User.
     *
     * @return
     */
    public boolean isMarkAsRefundedEnabled() {

        return markAsRefundedButton.isEnabled();

    }

    /**
     * When click on REFUND, pop up window is opened. This method will capture the message.
     *
     * @return popup message
     */
    public String getPopUpMessageOnRefund() {
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String popUpHandle = getDriver().getWindowHandles().toArray()[0].toString();
        getDriver().switchTo().window(popUpHandle);
        LOGGER.info("Get the pop up window message");
        final String popUpMessage = popUpDialog.getText();

        getActions().click(confirmButton);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        return popUpMessage;
    }

    /**
     * Method to get Properties Name and Value depending on dataIndex passed.
     */
    public String getProperties(final int rowIndex, final int dataIndex) {
        Util.scroll(driver, "1000", "0");

        final String xpath = ".//*[@class='find-results detail-results no-link find-results-']/div/table/tbody/tr["
            + rowIndex + "]/td[" + dataIndex + "]";
        return driver.findElement(By.xpath(xpath)).getText();
    }

    /**
     * Click on Retrigger button in Pending\Failed Order
     */
    public void clickRetrigger(final String strategy) {
        LOGGER.info("Click on Retrigger");
        if (strategy.equals(PelicanConstants.CLOUD_CREDITS)) {
            getActions().click(cloudCreditRetriggerButton);
        } else {
            getActions().click(legacyRetriggerButton);
        }

        // Saving the main window handle before switching to the popup window
        final String mainWindow = getDriver().getWindowHandle();
        final Set<String> windowHandles = getDriver().getWindowHandles();
        getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        clickOnConfirmButton();
        // Switching back to main Window
        getDriver().switchTo().window(mainWindow);
    }

    /**
     * This method clicks on Process PO and returns Process PO Page
     *
     * @return ProcessPurchaseOrderPage
     */
    public ProcessPurchaseOrderPage clickOnProcessPoLink() {

        getActions().click(processPOLink);
        return super.getPage(ProcessPurchaseOrderPage.class);
    }

    /**
     * Method to check "Process PO Manually" link's visibility.
     *
     * @return
     */
    public boolean isProcessPoLinkDisplayed() {
        boolean isDisplayed;
        try {
            isDisplayed = processPOLink.isDisplayed();
        } catch (final NoSuchElementException e) {
            isDisplayed = false;
        }
        return isDisplayed;
    }

    /**
     * This method will click on the confirm button in the popup
     */
    public PurchaseOrderDetailPage clickOnConfirmButton() {
        getActions().click(confirmButton);
        return getPage(PurchaseOrderDetailPage.class);
    }

    /**
     * Get the message from pop up
     *
     * @return
     */
    public String getPopUpMessage() {
        getDriver().getWindowHandles().toArray()[0].toString();
        return popUpDialog.getText();
    }

    /**
     * Method to get line items value column name wise.
     *
     * @return value by column .//*[@id='line-items']/div/div/table/tbody/tr[1]/td[1]
     */
    private String getLineItemValueByColumnName(final int offerIndex, final String columnName) {

        String selector;
        if (offerIndex == 1) {
            selector =
                ".//*[@id='line-items']/div/div/table/tbody/tr/td[" + (getColumnIndexForLineItems(columnName)) + "]";
        } else {
            selector = ".//*[@id='line-items']/div/div/table/tbody/tr[" + (offerIndex - 1) + "]/td["
                + (getColumnIndexForLineItems(columnName)) + "]";
        }

        final WebElement element = getDriver().findElement(By.xpath(selector));
        LOGGER.info("Line Items " + columnName + " is found: " + element.getText());
        return element.getText();
    }

    /**
     * Method to get Transactions value column name wise.
     *
     * @return value by column .//*[@id='transactions-list']/div/div/table/tbody/tr[1]/td[1]
     */
    private String getTransactionValueByColumnName(final int offerIndex, final String columnName) {

        String selector;
        if (getDriver().findElement(By.xpath(".//*[@id='transactions-list']/div/div/table/tbody/tr/td["
            + (getColumnIndexForTransactions(columnName)) + "]")).isDisplayed()) {

            if (offerIndex == 1) {
                selector = ".//*[@id='transactions-list']/div/div/table/tbody/tr/td["
                    + (getColumnIndexForTransactions(columnName)) + "]";
            } else {

                selector = ".//*[@id='transactions-list']/div/div/table/tbody/tr[" + (offerIndex - 1) + "]/td["
                    + (getColumnIndexForTransactions(columnName)) + "]";
            }
        } else {
            selector = ".//*[@id='transactions-list']/div/div/table/tbody/tr[" + (offerIndex) + "]/td["
                + (getColumnIndexForTransactions(columnName)) + "]";
        }

        final WebElement element = getDriver().findElement(By.xpath(selector));
        LOGGER.info("Transactions " + columnName + " is found: " + element.getText());
        return element.getText();
    }

    /**
     * Method to get Fulfillment Groups value column name wise.
     *
     * @return value by column .//*[@id='line-items']/div/div/table/tbody/tr[1]/td[1]
     */
    private String getFulfillmentGroupValueByColumnName(final int offerIndex, final String columnName) {

        String selector;

        selector = ".//*[@id='fulfillment-groups']/div/div/table/tbody/tr[" + (offerIndex) + "]/td["
            + (getColumnIndexForFulfillmentGroup(columnName)) + "]";

        final WebElement element = getDriver().findElement(By.xpath(selector));
        LOGGER.info("Fulfillment Group " + columnName + " is found: " + element.getText());
        return element.getText();
    }

    /**
     * Method to get Fulfillment Response by column name and index.
     *
     * @param offerIndex
     * @param columnName
     * @return
     */
    private String getFulfillmentResponsesByColumnName(final int offerIndex, final String columnName) {
        String selector;

        selector = fulfillmentResponsesSelector + "[" + (offerIndex) + "]/td["
            + (getColumnIndexForFulfillmentResponses(columnName)) + "]";

        final WebElement element = getDriver().findElement(By.xpath(selector));
        LOGGER.info("Fulfillment Group " + columnName + " is found: " + element.getText());
        return element.getText();
    }

    /**
     * This Method returns Grid size for Fulfillment Responses.
     */
    public int getFulfillmentResponsesGridSize() {
        return getGridValues(fulfillmentResponsesSelector).size();
    }

    /**
     * method to get index of column by Name in Line Items in Purchase Order Detail Page
     *
     * @return index of column
     */
    private int getColumnIndexForLineItems(final String columnName) {
        int columnIndex = 0;
        boolean foundColumn = false;
        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElementsOfLineItems();

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
     * method to get index of column by Name in Transactions in Purchase Order Detail Page
     *
     * @return index of column
     */
    private int getColumnIndexForTransactions(final String columnName) {
        int columnIndex = 0;
        boolean foundColumn = false;
        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElementsOfTransactions();

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
     * method to get index of column by Name in Fulfillment Group in Purchase Order Detail Page
     *
     * @return index of column
     */
    private int getColumnIndexForFulfillmentGroup(final String columnName) {
        int columnIndex = 0;
        boolean foundColumn = false;
        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElementsOfFulfillmentGroup();

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
     * method to get index of column by Name in Fulfillment Responses in Purchase Order Detail Page
     *
     * @return index of column
     */
    private int getColumnIndexForFulfillmentResponses(final String columnName) {
        int columnIndex = 0;
        boolean foundColumn = false;
        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElementsOfFulfillmentResponses();

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
     * Method to get header element of LineItems
     *
     * @return element
     */
    private List<WebElement> getHeaderElementsOfLineItems() {
        return getDriver()
            .findElements(By.xpath(parentElementSelector + ".//*[@id='line-items']/div/div/table/thead/tr/th"));
    }

    /**
     * Method to get header element of Transactions.
     *
     * @return element
     */
    private List<WebElement> getHeaderElementsOfTransactions() {
        return getDriver()
            .findElements(By.xpath(parentElementSelector + ".//*[@id='transactions-list']/div/div/table/thead/tr/th"));
    }

    /**
     * Method to get header element of Fulfillment Group
     *
     * @return element
     */
    private List<WebElement> getHeaderElementsOfFulfillmentGroup() {
        return getDriver()
            .findElements(By.xpath(parentElementSelector + ".//*[@id='fulfillment-groups']/div/div/table/thead/tr/th"));
    }

    /**
     * Method to get header element of Fulfillment Responses
     *
     * @return element
     */
    private List<WebElement> getHeaderElementsOfFulfillmentResponses() {
        return getDriver().findElements(
            By.xpath(parentElementSelector + ".//*[@id='fulfillment-responses']/div/div/table/thead/tr/th"));
    }

}
