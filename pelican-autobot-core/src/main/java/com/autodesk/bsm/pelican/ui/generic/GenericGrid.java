package com.autodesk.bsm.pelican.ui.generic;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generic grid page object for admin tool. This is primary used for search result set.
 *
 * @author yin
 */
public class GenericGrid extends GenericDetails {

    protected String parentElementSelector;

    @FindBy(css = ".find-results-hd-inner .count")
    private WebElement displayTotalText;

    @FindBy(xpath = "//*[@id=\"find-results\"]/div[2]/div/div/a[3]")
    private WebElement lastPageInPagination;

    @FindBy(xpath = "(.//*[@class='pgn'])[1]/span[contains(@class,'page current')]")
    private WebElement currentPageIndex;

    @FindBy(xpath = "(.//*[@class='pgn'])[1]/a[contains(@class,'previous')]")
    private WebElement previousPageLink;

    @FindBy(xpath = "(.//*[@class='pgn'])[1]/a[contains(@class,'next')]")
    private WebElement nextPageLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericGrid.class.getSimpleName());

    public GenericGrid(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        this.parentElementSelector = "";
    }

    public GenericGrid(final String parentElementSelector, final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        this.parentElementSelector = parentElementSelector.trim() + " ";
    }

    /**
     * Method to return grid
     */
    public GenericGrid getGrid() {
        return super.getPage(GenericGrid.class);
    }

    /**
     * Get total items on grid. Referencing the text on the grid header
     *
     * @return Total items on the grid
     */
    public int getTotalItems() {
        String value;
        if (!doesElementExist(displayTotalText)) {
            value = "0";
        } else {
            String sentence = displayTotalText.getText();
            final int index = sentence.indexOf("of");

            if (index > 0) { // showing 1-20 of 78 results
                sentence = sentence.substring(index, sentence.length() - 1);
            }
            final String[] word = sentence.split(" ");
            value = word[1];
        }
        LOGGER.info("There are " + value + " items");

        return Integer.parseInt(value);
    }

    /**
     * Get total rows on the grid
     */
    public int getTotalRows() {
        final List<WebElement> rows =
            getDriver().findElements(By.cssSelector(parentElementSelector + ".results > tbody > tr > .first"));
        return rows.size();
    }

    public GenericDetails selectResultRow(final int row) {
        final List<WebElement> totalElements =
            getDriver().findElements(By.cssSelector(parentElementSelector + ".results > tbody > tr > td:nth-child(1)"));
        if (row > totalElements.size()) {
            throw new RuntimeException(
                "Requested row, " + row + ", is greater than the total rows of " + totalElements.size());
        }
        if (row < 1) {
            totalElements.get(row).click();
        } else {
            totalElements.get(row - 1).click();
        }
        return super.getPage(GenericDetails.class);
    }

    /**
     * Get column headers for the grid
     *
     * @return List of headers
     */
    public List<String> getColumnHeaders() {

        final List<String> headers = new ArrayList<>();

        // Get all the header elements
        final List<WebElement> headerElements = getHeaderElements();
        for (final WebElement element : headerElements) {
            final String header = element.getText();
            LOGGER.info("Column header: '" + header + "'");
            headers.add(header);
        }

        return headers;
    }

    /**
     * The column values (including value in bracket) per specified column
     *
     * @param columnName
     * @return List<String>
     */
    public List<String> getColumnValues(final String columnName) {
        final int columnIndex = getColumnIndex(columnName) + 1;
        final String selector = parentElementSelector + ".results > tbody > tr > td:nth-child(" + columnIndex + ")";
        return getColumnValuesBySelector(selector);
    }

    /**
     * Returns a list of values based on the column index
     * 
     * @param columnIndex
     * @return List<String>
     */
    public List<String> getColumnValues(final int columnIndex) {
        final String selector = parentElementSelector + ".results > tbody > tr > td:nth-child(" + columnIndex + ")";
        return getColumnValuesBySelector(selector);
    }

    /**
     * Method to get column values based on valueNumber. e.g. if columnValue is mayalt (420). If valueNumber is 1 then
     * mayalt will be returned. If valueNumber is 2 then 420 will be returned.
     *
     * @param columnName
     * @param valueNumber
     * @return List<String>
     * @overload
     */
    public List<String> getColumnValues(final String columnName, final int valueNumber) {
        final int columnIndex = getColumnIndex(columnName) + 1;
        String selector = "";
        if (valueNumber == 1) {
            selector = parentElementSelector + ".results > tbody > tr > td:nth-child(" + columnIndex + ") > a";
        } else if (valueNumber == 2) {
            selector = parentElementSelector + ".results > tbody > tr > td:nth-child(" + columnIndex + ") > span";
        } else {
            LOGGER.error(
                "Please provide a valid valueNumber (either 1 or 2). " + valueNumber + " is not a valid number.");
        }
        return getColumnValuesBySelector(selector);
    }

    /**
     * The column values per specified column name and selector
     *
     * @param selector
     * @return @return List<String>
     */
    private List<String> getColumnValuesBySelector(final String selector) {
        final List<String> values = new ArrayList<>();
        final List<WebElement> cells = getDriver().findElements(By.cssSelector(selector));
        if (cells.size() == 1) {
            values.add(cells.get(0).getText());
        } else {
            for (final WebElement cell : cells) {
                values.add(cell.getText());
            }
        }
        return values;
    }

    /**
     * Method to get the column index for the given column name in a table.
     *
     * @param columnName
     * @return columnIndex
     */
    private int getColumnIndex(final String columnName) {
        int columnIndex = 0;
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

        return columnIndex;
    }

    private List<WebElement> getHeaderElements() {
        return getDriver().findElements(By.cssSelector(parentElementSelector + ".results > thead > tr > th"));
    }

    private boolean doesElementExist(final WebElement element) {
        boolean found = false;
        try {
            element.isDisplayed();
            found = true;
        } catch (final NoSuchElementException e) {
            // ignore
        }
        return found;
    }

    // Select a row with a particular data in the anchor href parameter

    public GenericDetails selectResultRowWithHyperLink(final String hyperLinkParmeter, final int row)
        throws NoSuchElementException {
        final List<WebElement> totalElements =
            getDriver().findElements(By.cssSelector(".o.last>td>a[href*='" + hyperLinkParmeter + "']"));
        if (row >= totalElements.size()) {
            throw new NoSuchElementException(
                "Requested row, " + row + ", is greater than the total rows of " + totalElements.size());
        }
        totalElements.get(row).click();
        return super.getPage(GenericDetails.class);
    }

    /**
     * Method to get 'value' attribute of an element.
     *
     * @param id (Element id)
     * @return value (Element value)
     */
    public String getValue(final String id) {
        final String script = "return document.getElementById('" + id + "').getAttribute('value');";
        return ((JavascriptExecutor) getDriver()).executeScript(script).toString();
    }

    /**
     * This method returns boolean "true" / "false" which is displayed in the Promotion Detail Page
     */
    public boolean isDescriptorDisplayed(final String fieldName) {
        final String descriptorXpath = ".//*[@class='field-name' and text()='" + fieldName + ":']";
        boolean isDescriptorPresent;
        try {
            final WebElement descriptorElement = getDriver().findElement(By.xpath(descriptorXpath));
            isDescriptorPresent = true;
        } catch (final NoSuchElementException ex) {
            // If the Element is not present, Selenium throws NoSuchElementException. Catching the same to
            // return false
            isDescriptorPresent = false;
        }
        return isDescriptorPresent;
    }

    /**
     * This method returns boolean "true" / "false" which is displayed in the Edit Descriptors Page
     */
    public boolean isEditDescriptorDisplayed(final String fieldName) {
        final String descriptorXpath = ".//*[text()='" + fieldName + "']";
        boolean isDescriptorPresent;
        try {
            getDriver().findElement(By.xpath(descriptorXpath));
            isDescriptorPresent = true;
        } catch (final NoSuchElementException ex) {
            // If the Element is not present, Selenium throws NoSuchElementException. Catching the same to
            // return false
            isDescriptorPresent = false;
        }
        return isDescriptorPresent;
    }

    public String getLocalizedDescriptorValue(final String fieldName) {
        return getDescriptorProperties("value", fieldName, true);
    }

    private String getDescriptorProperties(final String fieldType, final String fieldName, final boolean localized) {
        String descriptorXpath;
        // Xpath locators for localized and non localized descriptors differ, So handling them according
        // to value
        // of localized boolean parameter
        if (localized) {
            descriptorXpath = ".//*[@class='field-name' and text()='" + fieldName + ":']/following-sibling::*[@class='"
                + fieldType + "']";
        } else {
            descriptorXpath = ".//*[@class='field-name' and text()='" + fieldName + ":']/following-sibling::*[@class='"
                + fieldType + " value-not-localized']";
        }
        String resultString;
        try {
            final WebElement descriptorElement = getDriver().findElement(By.xpath(descriptorXpath));
            resultString = descriptorElement.getText();
        } catch (final NoSuchElementException ex) {
            // If the Element is not present, Selenium throws NoSuchElementException and returning the
            // value as empty
            // The validation will happen in test class
            resultString = "";
        }
        return resultString;
    }

    /**
     * This method clicks on the last page number in the pagination TODO: need to fix this method -> currently clicks on
     * the second page but not on the last page
     */
    public void getLastPage() {

        lastPageInPagination.click();
        LOGGER.info("Clicked on the last page in pagination");
    }

    /**
     * This method clicks on the last page in the pagination
     */
    public void getLastPage(final int totalItems, final int rowsPerPage) {
        int lastPageNumber = totalItems / rowsPerPage;
        if (totalItems % rowsPerPage != 0) { // (ex: 180/40 = 4 which is incorrect)
            lastPageNumber++;
        }

        if (lastPageNumber == 1) { // last page is same as the first page
            return;
        }
        getDriver().findElement(By.linkText(Integer.toString(lastPageNumber))).click();
        LOGGER.info("Clicked on the last page in pagination");
    }

    /**
     * This method clicks on the First page in the pagination
     */

    public void navigateToFirstPage() {

        try {
            final WebElement firstPage = driver.findElement(By.linkText("1"));
            firstPage.click();
        } catch (final Exception e) {
            LOGGER.info("navigateToFirstPage: Link for first page not found");
        }
    }

    /**
     * Method to select a column(check box) in a particular row.
     *
     * @return GenericDetails
     */
    public GenericDetails selectResultColumnWithName(final String columnName, final int row)
        throws NoSuchElementException {
        final int columnIndex = getColumnIndex(columnName) + 1;
        final String selector =
            parentElementSelector + ".results > tbody > tr > td:nth-child(" + columnIndex + ") > input";
        final List<WebElement> totalElements = getDriver().findElements(By.cssSelector(selector));
        if (row >= totalElements.size()) {
            throw new NoSuchElementException(
                "Requested row, " + row + ", is greater than the total rows of " + totalElements.size());
        }
        getActions().click(totalElements.get(row));
        return super.getPage(GenericDetails.class);
    }

    /**
     * Method to click a column field in a particular row.
     *
     * @return GenericDetails
     */
    public GenericDetails clickResultColumnWithName(final String columnName, final int row)
        throws NoSuchElementException {
        final int columnIndex = getColumnIndex(columnName) + 1;
        final String selector = parentElementSelector + ".results > tbody > tr > td:nth-child(" + columnIndex + ")";
        final List<WebElement> totalElements = getDriver().findElements(By.cssSelector(selector));
        if (row >= totalElements.size()) {
            throw new NoSuchElementException(
                "Requested row, " + row + ", is greater than the total rows of " + totalElements.size());
        }
        getActions().click(totalElements.get(row));
        return super.getPage(GenericDetails.class);
    }

    /**
     * Method to get row index randomly from first page even if more than 20 results exist on grid.
     *
     * @return index
     */
    public int selectRowRandomlyFromFirstPage(final int totalRows) {
        if (totalRows > 0) {
            int index;
            final Random random = new Random();
            if (totalRows <= 20) {
                index = random.nextInt(totalRows);
            } else {
                index = random.nextInt(19);
            }
            LOGGER.info("Random index:" + index);
            return index;
        } else {
            LOGGER.info("No rows exists");
            return 0;
        }
    }

    /** Pagination Functions. **/

    /**
     * This method clicks on the next page in the pagination.
     *
     * @return true if link is visible & enabled.
     */
    public boolean navigateToNextPage() {

        // if next page button is on the page (visible & enable), click it
        if (nextPageLink.isDisplayed() && nextPageLink.isEnabled()) {
            nextPageLink.click();
            LOGGER.info("Navigating to NextPage");
            return true;
        }
        return false;
    }

    /**
     * This method clicks on the Prev page in the pagination.
     *
     * @return true if link is visible & enabled.
     */
    public boolean navigateToPrevPage() {

        // if prev page button is on the page (visible & enable), click it
        if (previousPageLink.isDisplayed() && previousPageLink.isEnabled()) {
            previousPageLink.click();
            LOGGER.info("Navigating to PrevPage");
            return true;
        }
        return false;
    }

    /**
     * Returns Total no of Page for paging.
     *
     * @param pageSize
     * @return Total no of Page.
     */

    public int getTotalPageCount(final int pageSize) {
        int totalPage = 1;

        if (getTotalItems() != 0) {
            totalPage = getTotalItems() / pageSize;

            if (getTotalItems() % pageSize != 0) {
                totalPage = totalPage + 1;
            }
        }
        return totalPage;
    }

    /**
     * Returns current visible page index for paging.
     *
     * @return Page index.
     */
    public int getCurrentPageIndex() {
        final int pageIndex = 1;

        if (currentPageIndex.isDisplayed()) {

            final String strPageIndex = currentPageIndex.getText();

            return strPageIndex != null && !strPageIndex.isEmpty() ? Integer.parseInt(strPageIndex) : pageIndex;
        }

        return pageIndex;
    }

    /**
     * Method to get index of required value in particular column.
     *
     * @param columnName
     * @param value
     * @return index
     */
    public int getRowIndexForRequiredValue(final String columnName, final String value) {
        int indexByValue = 0;
        boolean rowFound = false;
        List<String> rows;
        rows = getColumnValues(columnName);
        for (final String row : rows) {
            indexByValue++;
            if (row.equals(value)) {
                rowFound = true;
                break;
            }
        }
        if (!rowFound) {
            LOGGER.error("Unable to get row index for required row");
        }
        return indexByValue;
    }
}
