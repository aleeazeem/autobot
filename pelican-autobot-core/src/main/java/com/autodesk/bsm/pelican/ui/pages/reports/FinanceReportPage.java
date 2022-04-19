package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Page object for Admin Tool's Finance Report - on the main tab
 *
 * @author yin
 */

/**
 * @author kishor
 */
public class FinanceReportPage extends GenericDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceReportPage.class.getSimpleName());

    @FindBy(id = "input-orderDateAfter")
    private WebElement startDateInput;

    @FindBy(id = "input-orderDateBefore")
    private WebElement endDateInput;

    @FindBy(id = "input-fulfillmentDateAfter")
    private WebElement fulfillmentStartDateInput;

    @FindBy(id = "input-fulfillmentDateBefore")
    private WebElement fulfillmentEndDateInput;

    @FindBy(id = "input-lastModifiedDateAfter")
    private WebElement lastModifiedStartDateInput;

    @FindBy(id = "input-lastModifiedDateBefore")
    private WebElement lastModifiedEndDateInput;

    @FindBy(className = "error-message")
    private WebElement errorText;

    @FindBy(css = ".errors")
    private WebElement errorSection;

    @FindBy(css = "#subnav > ul > li > span")
    private WebElement reportContainer;

    @FindBy(name = "orderStartHour")
    private WebElement orderStartHourSelect;

    @FindBy(name = "fulfillmentStartHour")
    private WebElement fulfillmentStartHourSelect;

    @FindBy(name = "lastModifiedStartHour")
    private WebElement lastModifiedStartHourSelect;

    @FindBy(name = "orderStartMinute")
    private WebElement orderStartMinuteSelect;

    @FindBy(name = "fulfillmentStartMinute")
    private WebElement fulfillmentStartMinuteSelect;

    @FindBy(name = "lastModifiedStartMinute")
    private WebElement lastModifiedStartMinuteSelect;

    @FindBy(name = "orderStartSecond")
    private WebElement orderStartSecondSelect;

    @FindBy(name = "fulfillmentStartSecond")
    private WebElement fulfillmentStartSecondSelect;

    @FindBy(name = "lastModifiedStartSecond")
    private WebElement lastModifiedStartSecondSelect;

    @FindBy(name = "orderEndHour")
    private WebElement orderEndHourSelect;

    @FindBy(name = "fulfillmentEndHour")
    private WebElement fulfillmentEndHourSelect;

    @FindBy(name = "lastModifiedEndHour")
    private WebElement lastModifiedEndHourSelect;

    @FindBy(name = "orderEndMinute")
    private WebElement orderEndMinuteSelect;

    @FindBy(name = "fulfillmentEndMinute")
    private WebElement fulfillmentEndMinuteSelect;

    @FindBy(name = "lastModifiedEndMinute")
    private WebElement lastModifiedEndMinuteSelect;

    @FindBy(name = "orderEndSecond")
    private WebElement orderEndSecondSelect;

    @FindBy(name = "fulfillmentEndSecond")
    private WebElement fulfillmentEndSecondSelect;

    @FindBy(name = "lastModifiedEndSecond")
    private WebElement lastModifiedEndSecondSelect;

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement findByIdTab;

    @FindBy(id = "input-purchaseOrderId")
    private WebElement purchaseOrderIdInput;

    @FindBy(id = "action")
    private WebElement viewOrDownloadSelect;

    @FindBy(id = "input-userExternalKeys")
    private WebElement findByOxygenId;

    @FindBy(css = ".form-group-labels > h3:nth-child(3)")
    private WebElement findByUserExternalKeyTab;

    public FinanceReportPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    public void setStartDate(final String value) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Set start date to " + value);
        getActions().setText(startDateInput, value);
    }

    public void setEndDate(final String value) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Set end date to " + value);
        getActions().setText(endDateInput, value);
    }

    public void setFulfillmentStartDate(final String value) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Set Fulfillment start date to " + value);
        getActions().setText(fulfillmentStartDateInput, value);
    }

    public void setFulfillmentEndDate(final String value) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Set Fulfillment end date to " + value);
        getActions().setText(fulfillmentEndDateInput, value);
    }

    public void setLastModifiedStartDate(final String value) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Set Last Modified start date to " + value);
        getActions().setText(lastModifiedStartDateInput, value);
    }

    public void setLastModifiedEndDate(final String value) {
        if (!isPageValid()) {
            navigateToPage();
        }
        LOGGER.info("Set Last Modified end date to " + value);
        getActions().setText(lastModifiedEndDateInput, value);
    }

    public String getH3ErrorMessage() {
        final String message = errorText.getText();
        LOGGER.info("Error message is : {}", message);
        return message;
    }

    public String getErrorText() {
        final String message = errorSection.getText();
        LOGGER.info("Error message is {}", message);
        return message;
    }

    /**
     * @param startTime - HH:mm:ss format
     */
    public void setOrderStartTime(final String startTime) {

        LOGGER.info("Setting order Start time to : " + startTime);
        final String[] startTimeArray = startTime.split(":");
        getActions().select(orderStartHourSelect, startTimeArray[0]);
        getActions().select(orderStartMinuteSelect, startTimeArray[1]);
        getActions().select(orderStartSecondSelect, startTimeArray[2]);
    }

    /**
     * @param EndTime - HH:mm:ss format
     */
    public void setOrderEndTime(final String endTime) {
        LOGGER.info("Setting order End time to : " + endTime);
        final String[] endTimeArray = endTime.split(":");
        getActions().select(orderEndHourSelect, endTimeArray[0]);
        getActions().select(orderEndMinuteSelect, endTimeArray[1]);
        getActions().select(orderEndSecondSelect, endTimeArray[2]);
    }

    /**
     * @param startTime - HH:mm:ss format
     */
    public void setFulfillmentStartTime(final String startTime) {

        LOGGER.info("Setting fulfillment Start time to : " + startTime);
        final String[] startTimeArray = startTime.split(":");
        getActions().select(fulfillmentStartHourSelect, startTimeArray[0]);
        getActions().select(fulfillmentStartMinuteSelect, startTimeArray[1]);
        getActions().select(fulfillmentStartSecondSelect, startTimeArray[2]);
    }

    /**
     * @param EndTime - HH:mm:ss format
     */
    public void setFulfillmentEndTime(final String endTime) {
        LOGGER.info("Setting fulfillment End time to : " + endTime);
        final String[] endTimeArray = endTime.split(":");
        getActions().select(fulfillmentEndHourSelect, endTimeArray[0]);
        getActions().select(fulfillmentEndMinuteSelect, endTimeArray[1]);
        getActions().select(fulfillmentEndSecondSelect, endTimeArray[2]);
    }

    /**
     * @param startTime - HH:mm:ss format
     */
    public void setLastModifiedStartTime(final String startTime) {

        LOGGER.info("Setting last modified Start time to : " + startTime);
        final String[] startTimeArray = startTime.split(":");
        getActions().select(lastModifiedStartHourSelect, startTimeArray[0]);
        getActions().select(lastModifiedStartMinuteSelect, startTimeArray[1]);
        getActions().select(lastModifiedStartSecondSelect, startTimeArray[2]);
    }

    /**
     * @param EndTime - HH:mm:ss format
     */
    public void setLastModifiedEndTime(final String endTime) {
        LOGGER.info("Setting last modified End time to : " + endTime);
        final String[] endTimeArray = endTime.split(":");
        getActions().select(lastModifiedEndHourSelect, endTimeArray[0]);
        getActions().select(lastModifiedEndMinuteSelect, endTimeArray[1]);
        getActions().select(lastModifiedEndSecondSelect, endTimeArray[2]);
    }

    /**
     * Assume the first line is the data
     *
     * @return
     */
    public String getReportHeader() {

        return super.getReportHeadersLine();
    }

    /**
     * Data is everything after the first line
     *
     * @return
     */
    public List<String> getReportData() {

        return super.getReportData();
    }

    /**
     * Find the data from the Order Date column
     *
     * @return
     */
    public List<String> getReportValues(final String columnName) {
        final List<String> orderDates = new ArrayList<>();

        // get column index for order date
        int columnIndex = -1;
        final String header = getReportHeader();
        final String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex < 0) {
            throw new RuntimeException("Unable to find header '" + columnName + "' in Finance Report\n" + header);
        }

        // get values
        final List<String> lines = getReportData();
        for (final String line : lines) {
            final String[] rowData = line.split(",");
            orderDates.add(rowData[columnIndex]);
        }
        return orderDates;
    }

    /*
     * This method navigates to the search page of the finance report
     */
    public void navigateToSearchPage() {
        if (!isPageValid()) {
            navigateToPage();
        }
    }

    /*
     * This method finds the last modified date web elements on the page
     *
     * @Return - List of Last Modified Date Web Elements in page
     */
    public List<WebElement> findLastModifiedDateWebElementsOnPage() {
        final WebElement element = getDriver().findElement(By.id("input-lastModifiedDateAfter"));
        final WebElement newElement = getDriver().findElement(By.id("input-lastModifiedDateBefore"));
        final List<WebElement> elementList = new ArrayList<>();
        elementList.add(element);
        elementList.add(newElement);

        return elementList;
    }

    private String getUrl() {
        return getEnvironment().getAdminUrl() + "/" + AdminPages.REPORTS.getForm() + "/"
            + AdminPages.FINANCE_REPORT.getForm();
    }

    private void navigateToPage() {
        final String url = getUrl();
        LOGGER.info("Navigate to {}", url);
        getDriver().get(url);
    }

    /**
     * Determine if we're on the Finance Report page by getting the current url as well as the header = "Generate
     * Finance Report"
     *
     * @return true if we're on the page. Otherwise false
     */
    public boolean isPageValid() {
        boolean pageDisplayed = false;

        if (getDriver().getCurrentUrl().equalsIgnoreCase(getUrl())) {
            try {
                reportContainer.isDisplayed();
                pageDisplayed = true;
            } catch (final NoSuchElementException e) {
                // ignore
            }
        }
        return pageDisplayed;
    }

    /**
     * This method navigates to a page, enters purchase order id and selects view or download
     *
     * @param purchaseOrderId
     * @param viewDownload
     * @param index
     */
    public void findFinanceReportById(final String purchaseOrderId, final String viewDownload, final int index) {

        navigateToPage();
        clickOnFindByIdTab();
        enterPurchaseOrderId(purchaseOrderId);
        selectViewDownload(viewDownload, index);
    }

    public void generateFinanceReportByOxygenId(final List<String> oxygenIdList, final String viewOrDownload) {
        navigateToPage();
        clickOnFindByUserExternalKeyTab();
        enterUserExternalKeysList(oxygenIdList);
        selectViewDownload(viewOrDownload, 2);
    }

    /**
     * This method selects View/Download action.
     *
     * @param action
     */
    private void selectViewDownload(final String action, final int index) {

        getActions().select(viewOrDownloadSelect, action);
        getActions().select(getDriver().findElements(By.id("action")).get(index), action);
    }

    private void clickOnFindByIdTab() {

        getActions().click(findByIdTab);
    }

    private void enterPurchaseOrderId(final String purchaseOrderId) {
        getActions().setText(purchaseOrderIdInput, purchaseOrderId);
    }

    private void clickOnFindByUserExternalKeyTab() {
        getActions().click(findByUserExternalKeyTab);
    }

    private void enterUserExternalKeysList(final List<String> oxygenIdList) {
        getActions().click(findByOxygenId);
        findByOxygenId.sendKeys(StringUtils.join(oxygenIdList, ","));
    }

    /**
     * Click on Generate button which returns the same page, incase of error
     *
     * @param index
     * @return
     */
    public FinanceReportPage clickOnGenerateReportWithError(final int index) {

        submit(index);
        return super.getPage(FinanceReportPage.class);
    }

    /**
     * Click on Generate report, can either take to View Page or if download is selected, it will be in the same page
     *
     * @param index
     */
    public void clickOnGenerateReport(final int index) {

        submit(index);
    }
}
