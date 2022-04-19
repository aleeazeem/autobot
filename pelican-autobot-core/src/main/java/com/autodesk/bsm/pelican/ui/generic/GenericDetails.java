package com.autodesk.bsm.pelican.ui.generic;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Readonly form (description list) for the detail page.
 *
 * @author yin
 */
public class GenericDetails {

    protected WebDriver driver;
    protected EnvironmentVariables environmentVariables;
    private final PelicanActions actions;

    @FindBy(className = "detail-sections")
    private WebElement detailContainer;

    @FindBy(css = "[name=Edit]")
    protected WebElement editButton;

    @FindBy(id = "SelectAll")
    private WebElement selectAllButton;

    @FindBy(css = "#bd h1")
    private WebElement headerContainer;

    @FindBy(css = "#page-error h3")
    private WebElement errorContainer;

    @FindBy(id = "ui-id-2")
    private WebElement errorsDetailsTab;

    @FindBy(css = "#offers-table\\5b 0\\5d > tbody > tr > td:nth-child(2)")
    public WebElement getOfferNameInSubscriptionDetailsPage;

    @FindBy(xpath = "//*[@id='offers-table[0]']/tbody/tr/td[2]")
    public WebElement offerNameFromSubscriptionPlanPage;

    @FindBy(css = "#page-error h2")
    private WebElement errorHeaderContainer;

    @FindBy(css = "#page-error > div.summary > a")
    private WebElement showErrorDetailsLink;

    @FindBy(xpath = "//*[@id='page-error']/div[2]")
    private WebElement errorDetails;

    @FindBy(xpath = "//a[contains(text(),'Show Details')]")
    private WebElement showDetailsLink;

    // Shared control on each page
    @FindBy(id = "appFamilyId")
    private WebElement applFamilySelect;

    @FindBy(id = "appId")
    private WebElement appIdSelect;

    @FindBy(className = "submit")
    protected WebElement submitButton;

    @FindBy(name = "Cancel")
    private WebElement cancelButton;

    @FindBy(className = "button")
    private WebElement cancelButtonInEditPage;

    @FindBy(xpath = ".//*[@id='bd']/h1")
    protected WebElement titleOfThePage;

    @FindBy(id = "SubmitBtn")
    private WebElement submitButtonOnPopUpGrid;

    @FindBy(xpath = "//*[@id='bd']/div[2]/div/p")
    protected WebElement errorMessageWebElement;

    @FindBy(xpath = ".//*[@id='field-usageType']//*[@class='error-message']")
    private WebElement usageTypeErrorMessage;

    @FindBy(xpath = ".//*[@id='field-productLineId']//*[@class='error-message']")
    private WebElement productLineErrorMessage;

    @FindBy(xpath = ".//*[@class='error-message']")
    private WebElement errorMessage;

    @FindBy(css = ".errors")
    protected WebElement error;

    // file Input
    @FindBy(id = "input-file")
    private WebElement xlsxInput;

    // product line drop down
    @FindBy(id = "productLineId")
    protected WebElement productLineSelect;

    // Store Type Drop down
    @FindBy(id = "storeTypeId")
    protected WebElement storeTypeSelect;

    // storeId Drop down
    @FindBy(id = "storeId")
    protected WebElement storeSelect;

    @FindBy(id = "input-id")
    private WebElement idInput;

    @FindBy(id = "input-name")
    protected WebElement nameInput;

    @FindBy(id = "input-description")
    private WebElement descriptionInput;

    @FindBy(id = "input-externalKey")
    protected WebElement externalKeyInput;

    @FindBy(id = "confirm-btn")
    protected WebElement confirmButton;

    @FindBy(id = "cancel-btn")
    protected WebElement cancelPopUpButton;

    @FindBy(id = "action")
    private WebElement actionViewDownloadSelect;

    @FindBy(css = "#bd > div:last-child")
    private WebElement reportData;

    @FindBy(name = "Delete")
    public WebElement deleteButton;

    @FindBy(id = "auditTrail")
    protected WebElement auditTrailLink;

    @FindBy(id = "isActive")
    protected WebElement activeSelect;

    @FindBy(id = "CancelBtn")
    protected WebElement cancelButtonInPopUp;

    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement findByExternalKeyTab;

    @FindBy(className = "popup-dialog")
    private WebElement popUpDialog;

    @FindBy(id = "offerName")
    protected WebElement offerName;

    private static final String GET_FIELD = ".//*[@class='inner-inner']//dt[contains(text(),'";
    private static final String GET_VALUE_OF_FIELD = ":')]/following-sibling::dd[1]";
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericDetails.class.getSimpleName());
    private static final String TAG_NAME_A = "a";
    private static final String SPAN_NAME = "span";

    public GenericDetails(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        this.driver = driver;
        this.environmentVariables = environmentVariables;
        this.actions = new PelicanActions(getDriver());
    }

    /**
     * Get all webElements from a webPage.
     *
     * @return List WebElements
     */
    public List<WebElement> getAllWebElements() {
        return driver.findElements(By.id("*"));
    }

    // Shared control on each page
    public String getApplicationFamily() {
        return new Select(applFamilySelect).getFirstSelectedOption().getText();
    }

    public void submit(final long time) {
        Wait.elementClickable(driver, submitButton);
        LOGGER.info("Submit button  :" + submitButton.isEnabled());
        submitButton.click();
        Util.waitInSeconds(time);
        LOGGER.info("Click 'Submit'");
    }

    /**
     * This is a method which will click on submit without any wait
     */
    public void submit() {
        submit(0L);
    }

    /**
     * Method to click on cancel button.
     */
    protected void cancel() {
        LOGGER.info("Click 'Cancel'");
        cancelButton.click();
    }

    protected void cancelInEditPage() {
        getActions().click(cancelButtonInEditPage);
    }

    /**
     * This is a method to refresh a page.
     */
    public void refreshPage() {
        LOGGER.info("Refresh a Page");
        driver.navigate().refresh();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * This is a method to navigate to previous page.
     */
    public void navigateToPreviousMessage() {
        LOGGER.info("Navigating to a previous page");
        driver.navigate().back();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * method to submit a button on a page where more than one submit button exists with submit class name.
     */
    public void submit(final int index) {
        final WebElement submit = getDriver().findElements(By.className("submit")).get(index);
        Wait.elementClickable(driver, submit);
        LOGGER.info("Click 'submit'");
        getDriver().findElements(By.className("submit")).get(index).click();
        Wait.pageLoads(getDriver());
    }

    /**
     * method to submit a button on pop up page.
     */
    public void submitButtonOnPopUpGrid() {
        LOGGER.info("Click 'submit' on Pop up Grid");
        submitButtonOnPopUpGrid.click();
    }

    public String getHeader() {
        return headerContainer.getText();
    }

    public String getH3ErrorMessage() {
        Wait.elementVisibile(getDriver(), errorContainer);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return errorContainer.getText();
    }

    /**
     * This is a method to return the error message in the h2 tag on the page
     *
     * @return String - Error Message.
     */
    public String getH2ErrorMessage() {
        Wait.elementVisibile(getDriver(), errorHeaderContainer);
        return errorHeaderContainer.getText();
    }

    /**
     * method to return main Error below the Title of the Page.
     *
     * @return main Error text
     */
    public String getError() {
        return error.getText();
    }

    /**
     * method to return main Error below the Title of the Page.
     *
     * @return main Error text
     */
    public String getErrorMessageForField() {
        Wait.stalenessOf(errorMessage);
        return errorMessage.getText();
    }

    /**
     * This method will return error message from the page inside the Form Intro section
     *
     * @return String - Error Message.
     */
    public String getErrorMessageFromFormHeader() {
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        LOGGER.info("Error Message from Form Header(Intro section): " + errorMessageWebElement.getText());
        return errorMessageWebElement.getText();
    }

    /**
     * This method retrieves all error messages on the page and return a list of error messages
     *
     * @return list of error messaged
     */
    public List<String> getErrorMessageList() {
        final List<WebElement> errorMessageElementList = driver.findElements(By.xpath(".//*[@class='error-message']"));
        final List<String> errorMessageList = new ArrayList<>();
        for (final WebElement errorMessageElement : errorMessageElementList) {
            errorMessageList.add(errorMessageElement.getText());
        }
        return errorMessageList;
    }

    /**
     * Method to click on show error details link.
     */
    public void clickOnErrorDetailsTab() {
        errorsDetailsTab.click();
        LOGGER.info("Clicked on show error details tab");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * Method to click the "Show Details" link when there is an error in the page
     */
    public void clickOnShowDetailsLink() {
        if (isElementPresent(showDetailsLink)) {
            showDetailsLink.click();
        }
    }

    /**
     * Method to set Active Status Either from Yes Or No.
     */
    public void selectActiveStatus(final String activeStatus) {
        if (activeStatus != null) {
            if (activeStatus.equals(PelicanConstants.YES) || activeStatus.equals(PelicanConstants.NO)) {
                getActions().select(activeSelect, activeStatus);
            }
        }
    }

    /**
     * Method to get Active Status whether its Yes Or No.
     */
    protected String getActiveStatusDisplayValue() {
        return new Select(activeSelect).getFirstSelectedOption().getText();
    }

    public String getErrorDetails() {
        return errorDetails.getText();
    }

    // Util
    public void setDriver(final WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setEnvironment(final EnvironmentVariables login) {
        this.environmentVariables = login;
    }

    protected EnvironmentVariables getEnvironment() {
        return environmentVariables;
    }

    /**
     * public PelicanActions getActions() { return actions; }
     */

    public <T extends GenericDetails> T getPage(final Class<T> klass) {

        T page;
        try {
            // Instantiate page
            try {
                final Constructor<T> constructor = klass.getConstructor(WebDriver.class, EnvironmentVariables.class);
                page = constructor.newInstance(driver, environmentVariables);
            } catch (final NoSuchMethodException e) {
                page = klass.newInstance();
            }
            PageFactory.initElements(driver, page);
            return page;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a child element. If not found, return null.
     *
     * @return Child web element if found. Otherwise, null
     */
    protected List<WebElement> findChildElements(final WebElement parentElement, final By by) {

        List<WebElement> elementsList = null;
        getDriver().manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);

        try {
            elementsList = parentElement.findElements(by);
        } catch (final NoSuchElementException e) {
            // Ignore, it's ok that we don't find it.
        } finally {
            // TODO: Need to add property file for the default time out
            getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
        return elementsList;
    }

    /**
     * This is the method to change the feature flag
     *
     * @param value
     */
    public void changeFeatureFlag(final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage,
        final String featureFlagName, final boolean value) {
        bankingConfigurationPropertiesPage.setFeatureFlag(featureFlagName, value);
    }

    /**
     * this method returns the title of the Page.
     *
     * @return Title
     */
    public String getTitle() {
        String title = null;
        Wait.elementVisibile(driver, titleOfThePage);
        try {
            title = titleOfThePage.getText();
            LOGGER.info("Title Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Title Doesn't Exist");
        }
        return title;
    }

    /**
     * This method selects the application
     */
    protected void selectApplication(final String application) {
        try {
            getActions().select(appIdSelect, application);
            LOGGER.info("Select the application: " + application);
        } catch (final Exception ex) {
            LOGGER.info("Unable to select the application.");

        }

    }

    /**
     * This method checks whether a web element is displayed on a page or not.
     *
     * @return boolean
     */
    protected boolean isElementPresent(final WebElement element) {
        try {
            element.isDisplayed();
        } catch (final NoSuchElementException e) {
            LOGGER.info("Element is not present");
            return false;
        }
        LOGGER.info("Element is present");
        return true;
    }

    /**
     * This is the method which will return the error message from the usage type field in the subscription plan page.
     *
     * @return Error - as string
     */
    public String getUsageTypeErrorMessage() {

        Wait.elementVisibile(getDriver(), usageTypeErrorMessage);
        final String usageTypeError = usageTypeErrorMessage.getText();
        LOGGER.info("Returning the error message displayed on usage type field on subscription plan page");

        return usageTypeError;
    }

    /**
     * This is the method which will return the error message from the product line field in any of the offering page.
     *
     * @return Error - as string
     */
    public String getProductLineErrorMessage() {

        final String productLineError = productLineErrorMessage.getText();
        LOGGER.info("Returning the error message displayed on product line field on subscription plan page");

        return productLineError;
    }

    /**
     * This is the method which will return the error message from the external key field in any of the offering page.
     *
     * @return Error - as string
     */
    public String getExternalKeyErrorMessage() {

        final String externalKeyError = errorMessage.getText();
        LOGGER.info("Returning the error message displayed on external key field on subscription plan page");

        return externalKeyError;
    }

    /**
     * Get all fields for an edit page.
     */
    public List<String> getAllFields() {
        final List<String> fields = new ArrayList<>();

        final List<WebElement> webElements = getFieldElements();
        for (final WebElement webElement : webElements) {
            // Remove ":" if needed
            final String field = webElement.getText().replace(":", "");
            // LOGGER.info("Field: " + field);
            fields.add(field);
        }
        return fields;
    }

    /**
     * Method to read the tabular details and return the particular field's value.
     *
     * @return field's value from the table
     */
    public String getValueByField(final String field) {
        return getValueByField(null, field);
    }

    /**
     * Method to return a field's value from the table, when the table is different from the generic ones and you need
     * to customize the list of tabular value to be picked from.
     *
     * @return String - Value from field
     */
    private String getValueByField(final List<WebElement> webElements, final String field) {
        final String selector = GET_FIELD + field + GET_VALUE_OF_FIELD;
        return getValueByField(webElements, field, selector, TAG_NAME_A);
    }

    /**
     * Method to return a field's id from the table, when the table is different from the generic ones and you need to
     * customize the list of tabular value to be picked from.
     *
     * @return String - Value from field
     */
    protected String getIdByField(final List<WebElement> webElements, final String field) {
        final String selector = GET_FIELD + field + GET_FIELD + "/span";
        return getValueByField(webElements, field, selector, SPAN_NAME);
    }

    /**
     * Method to return a field's value from the table, when the table is different from the generic ones and you need
     * to customize the list of tabular value to be picked from.
     *
     * @param webElements
     * @param field
     * @param selector
     * @return value by field
     */
    private String getValueByField(final List<WebElement> webElements, final String field, final String selector,
        final String tagName) {
        String value = "";
        boolean found = false;
        List<WebElement> listOfWebElements = webElements;
        if (listOfWebElements == null) {
            listOfWebElements = getFieldElements();
        }
        for (final WebElement webElement : listOfWebElements) {
            if (webElement.getText().equalsIgnoreCase(field + ":")) {
                found = true;
                final List<WebElement> cells = getDriver().findElements(By.xpath(selector));
                if (cells.size() > 0) {
                    final WebElement cell = cells.get(0);
                    final List<WebElement> linkCellElementList = findChildElements(cell, By.tagName(tagName));
                    if (linkCellElementList.size() == 0) {
                        value = cell.getText();
                        if (value.trim().equals("-")) {
                            value = null;
                        }
                    } else { // The cell is a link. It will have the id. We do
                        // not want to return that as it is dynamic
                        for (final WebElement linkCellElement : linkCellElementList) {
                            value = value + linkCellElement.getText();
                        }
                    }
                } else {
                    LOGGER.error("Unable to value for field: " + field);
                }
                break;
            }
            // index+=2;
        }
        if (!found) {
            LOGGER.error("Unable to find field: " + field);
        }
        LOGGER.info(field + " = " + value);
        return value;
    }

    /**
     * method to get total number of values of a field.
     *
     * @return total number of values.
     */
    public int getTotalNumberOfValuesInField(final String field) {
        List<WebElement> cells = new ArrayList<>();
        final List<WebElement> webElements = getFieldElements();
        for (final WebElement webElement : webElements) {
            if (webElement.getText().equalsIgnoreCase(field + ":")) {
                final String selector =
                    ".//*[@class='inner-inner']//dt[contains(text(),'" + field + ":')]/" + "following-sibling::dd[1]/a";
                cells = getDriver().findElements(By.xpath(selector));
            }
        }
        LOGGER.info("Total number of Values Under the filed of " + field + " = " + cells.size());
        return cells.size();
    }

    /**
     * method to get value(dd) of a key(dt) in AT with fields in dt and dd format. dd and dt are key and value pair.
     * format.
     *
     * @return field value
     */
    public String getFieldValueByKey(final String key) {
        String value;
        final String selector = ".//div[@class='inner-inner']//dt[contains(., '" + key + "')]/following-sibling::dd[1]";
        value = getDriver().findElement(By.xpath(selector)).getText();
        LOGGER.info("value is " + value);
        return value;
    }

    public List<String> getAllFieldLabels() {

        final List<String> fields = new ArrayList<>();
        final List<WebElement> webElements = getFieldElements();
        for (final WebElement webElement : webElements) {
            LOGGER.info("Field = " + webElement.getText());
            // Assuming that the last character in the field label is ":"
            fields.add(webElement.getText().substring(0, webElement.getText().length() - 1));
        }
        return fields;
    }

    /**
     * Click on the edit button so the page is now editable.
     */
    public void edit() {
        LOGGER.info("Click edit");
        editButton.click();
    }

    /**
     * Are we on the detail page?
     */
    public boolean isPageValid() {
        boolean valid = false;
        getDriver().manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);

        try {
            detailContainer.isDisplayed();
            valid = true;
        } catch (final NoSuchElementException e) {
            // ignore
        } finally {
            // TODO: Need to add property file for the default time out
            getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
        return valid;
    }

    /**
     * Method to check if page is valid or invalid.
     *
     * @param url
     * @return boolean
     */
    protected boolean isPageValid(final String url) {
        return getDriver().getCurrentUrl().equalsIgnoreCase(url);
    }

    /**
     * This method clicks on Select All button.
     */
    public void clickSelectAll() {
        getActions().click(selectAllButton);
        LOGGER.info("Clicked on Select All button");
    }

    private List<WebElement> getFieldElements() {
        return getDriver().findElements(By.cssSelector(".inner-inner > dl > dt"));
    }

    /**
     * @return boolean value - true if field is present and false if field is not present
     */
    public boolean isFieldPresentOnPage(final String key) {
        boolean found = false;
        final String selector = ".//div[@class='inner-inner']//dt[contains(., '" + key + "')]/following-sibling::dd[1]";
        final List<WebElement> webElements = getDriver().findElements(By.xpath(selector));
        if (webElements.size() > 0) {
            found = true;
        }

        return found;
    }

    /**
     * Method to select Application family.
     */
    protected void selectApplicationFamily(final String appFamilyName) {
        getActions().select(applFamilySelect, appFamilyName);
        LOGGER.info("Application family selected: " + appFamilyName);
    }

    /**
     * Method used to set the Upload file.
     *
     * @param fileName
     * @param timeInSeconds
     */
    protected void setUploadFile(final String fileName, final long timeInSeconds) {
        xlsxInput.sendKeys(getFilePath(fileName));
        submit(timeInSeconds);
    }

    // Method to get Error Messages
    public String getErrorMessage() {
        final String errorMessage = this.errorMessage.getText();
        LOGGER.info("Error Message is '" + errorMessage + "'");
        return errorMessage;
    }

    /**
     * Method to select product line by value from drop down.
     *
     * @param productLine
     */
    public void selectProductLine(final String productLine) {
        if (productLine != null) {
            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            getActions().select(this.productLineSelect, productLine);
            LOGGER.info("Selected product line in drop down is: " + productLine);
        }
    }

    /**
     * Method to select store type.
     *
     * @param storeType
     */
    public void selectStoreType(final String storeType) {
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        getActions().select(storeTypeSelect, storeType);
        LOGGER.info("Selected store type: " + storeType);
    }

    /**
     * Method to select store.
     *
     * @param store
     */
    public void selectStore(final String store) {
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        getActions().select(storeSelect, store);
        LOGGER.info("Selected store: " + store);
    }

    /**
     * Method to set the id in the id input field.
     *
     * @param id
     */
    protected void setId(final String id) {
        getActions().setText(idInput, id);
        LOGGER.info("Id set to: " + id);
    }

    /**
     * Method to set name in the name input field.
     *
     * @param name
     */
    protected void setName(final String name) {
        getActions().setText(nameInput, name);
        LOGGER.info("Name set to: " + name);
    }

    /**
     * Method to get Offer name from input field.
     */
    protected String getOfferName() {

        LOGGER.info("Default Offer Name set to: " + offerName.getAttribute("value"));
        return offerName.getAttribute("value");
    }

    /**
     * Method to set description in the description input field.
     *
     * @param description
     */
    protected void setDescription(final String description) {
        getActions().setText(descriptionInput, description);
        LOGGER.info("Description set to: " + description);
    }

    /**
     * Method to set external key in the external key input field.
     *
     * @param externalKey
     */
    public void setExternalKey(final String externalKey) {
        Wait.pageLoads(driver);
        if (externalKey != null) {
            getActions().setText(externalKeyInput, externalKey);
            LOGGER.info("External key set to: " + externalKey);
        }
    }

    /**
     * This method selects View/Download action.
     *
     * @param action
     */
    protected void selectViewDownloadAction(final String action) {
        if (action != null) {
            getActions().select(actionViewDownloadSelect, action);
            LOGGER.info("Action selected: " + action);
        }
    }

    /**
     * Generic method to check or uncheck a webelement
     *
     * @param webElement
     * @param action - String
     */
    protected void actionOnCheckBox(final WebElement webElement, final String action) {

        try {
            if (action.equalsIgnoreCase("check")) {
                getActions().check(webElement);
            } else if (action.equalsIgnoreCase("uncheck")) {
                getActions().uncheck(webElement);
            }

            LOGGER.info("Performed " + action + " on " + webElement);
        } catch (final Exception e) {
            LOGGER.error("Action on CheckBox failed!");
        }
    }

    /**
     * Method to navigate to a page.
     *
     * @param url
     */
    protected void navigateToPage(final String url) {
        getDriver().get(url);
        LOGGER.info("Navigated to '" + url + "'");
    }

    /**
     * Method to get column values from View Report(non-pretty format)
     *
     * @param columnName
     * @return List<String>
     */
    protected List<String> getReportValues(final String columnName) {
        final List<String> columnValues = new ArrayList<>();

        // get column index for order date
        int columnIndex = -1;
        final String header = getReportHeadersLine();
        final String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex < 0) {
            throw new RuntimeException("Unable to find header '" + columnName + "' in Report\n" + header);
        }

        // get values
        final List<String> lines = getReportData();
        LOGGER.info("Total number of lines: " + lines.size());
        for (final String line : lines) {
            final String[] rowData = line.split(",");
            columnValues.add(rowData[columnIndex]);
        }
        return columnValues;
    }

    /**
     * Method to get report header from View Report(non-pretty format)
     *
     * @return String
     */
    public String getReportHeadersLine() {

        String header = null;
        final String data = reportData.getText();

        final BufferedReader bufferedReader = new BufferedReader(new StringReader(data));
        try {
            header = bufferedReader.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return header;
    }

    /**
     * Method to get data from View Report(non-pretty format)
     *
     * @return List<String>
     */
    public List<String> getReportData() {
        final List<String> lines = new ArrayList<>();

        final String data = reportData.getText();
        final BufferedReader rdr = new BufferedReader(new StringReader(data));

        // skip the header
        String line;
        try {
            line = rdr.readLine();
            boolean done = false;

            while (!done) {
                line = rdr.readLine();
                if (line == null) {
                    done = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    /**
     * Method to return selected value from drop down
     *
     * @param element
     * @return String
     */
    protected String getFirstSelectedSelectValue(final WebElement element) {
        LOGGER.info("Selected value '");
        final Select select = new Select(element);
        final WebElement selectedValue = select.getFirstSelectedOption();
        return selectedValue.getText();
    }

    /**
     * This is a method to switch the control to pop up window handle.
     */
    protected void switchDriverControlToPopUp() {
        final int numberOfHandles = getDriver().getWindowHandles().size();
        String handle;
        if (numberOfHandles == 2) {
            handle = getDriver().getWindowHandles().toArray()[1].toString();
            getDriver().switchTo().window(handle);
        }

    }

    /**
     * This is a method to read the message in the pop up dialog box
     *
     * @return String - message in the pop up dialog box
     */
    public String readMessageInPopUp() {
        return (popUpDialog.getText());
    }

    /**
     * This is a method to switch the control to parent window handle.
     */
    public void switchDriverControlToParentWindow() {
        final String popUpHandle = getDriver().getWindowHandles().toArray()[0].toString();
        getDriver().switchTo().window(popUpHandle);
    }

    /**
     * Method used to get the path of the file.
     *
     * @return path of the file
     */
    private String getFilePath(final String fileName) {
        String fileFullName = null;
        try {
            fileFullName =
                Paths.get(System.getProperty("user.dir") + "/../pelican-autobot-core/src/test/resources/testdata")
                    .toRealPath().toString() + "/" + fileName;
        } catch (final IOException e) {
            LOGGER.error("ERROR to find File " + fileName + " from pelican-autobot-core/src/test/resources/testdata/");
        }
        LOGGER.info("FileName : " + fileFullName);
        return fileFullName;
    }

    /**
     * Method to click on Delete button and Confirm the Delete in popup.
     */
    protected void deleteAndConfirm() {
        clickOnDeleteButton();
        LOGGER.info("Clicked on delete button");
        final ConfirmationPopup popup = getPage(ConfirmationPopup.class);
        popup.confirm();
        LOGGER.info("Clicked on confirmation button in the pop up");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Method to click on Pop up Cancel button.
     */
    protected void clickOnCancelPopUpButton() {
        LOGGER.info("Click 'Cancel' ");
        Wait.elementClickable(getDriver(), cancelPopUpButton);
        getActions().check(cancelPopUpButton);
    }

    /**
     * Method to click on Pop up Confirm button.
     */
    protected void clickOnConfirmPopUpButton() {
        LOGGER.info("Click on 'Confirm' button on Confirmation Popup");
        Wait.elementClickable(getDriver(), confirmButton);
        getActions().check(confirmButton);
    }

    /**
     * Method just to click on Delete button
     */
    public void clickOnDeleteButton() {
        LOGGER.info("Click 'Delete'");
        Wait.elementClickable(getDriver(), deleteButton);
        deleteButton.click();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Method to check if specific WebElement exist in a web page
     *
     * @param webElement
     * @return true or false.
     */
    public boolean doesWebElementExist(final WebElement webElement) {
        boolean exist;
        getDriver().manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);

        try {
            LOGGER.info("checking whether the web element is displayed on the page");
            webElement.isDisplayed();
            exist = true;
        } catch (final NoSuchElementException e) {
            exist = false;
        }
        return exist;
    }

    /**
     * Method to get all product lines available under drop down menu.
     *
     * @return ProductLine List
     */
    public List<String> getActiveProductLine() {

        Wait.dropdownPopulatedWithValues(driver, productLineSelect);

        final Select select = new Select(productLineSelect);

        final List<String> productLineList = new ArrayList<>();
        final List<WebElement> optionsList = select.getOptions();
        LOGGER.info("Total Available Product Lines: " + optionsList.size());
        for (final WebElement element : optionsList) {
            productLineList.add(element.getText());
        }
        return productLineList;
    }

    /**
     * Method to check if page title is available. If web element for page title is not displayed, assumption is that
     * page is not loaded yet and we are waiting for time passed in argument.
     *
     * @param time
     */
    protected void checkPageLoaded(final long time) {
        try {
            getTitle();
        } catch (final Exception e) {
            Util.waitInSeconds(time);
        }
        LOGGER.info("Page title:" + getTitle());
    }

    protected PelicanActions getActions() {
        return actions;
    }

    /**
     * Click on the external key tab of the actor
     */
    protected void clickOnFindByExternalKeyLink() {

        getActions().click(findByExternalKeyTab);
    }

    /**
     * Get Grid Size.
     *
     * @param WebElement for Grid.
     */
    protected List<WebElement> getGridValues(final String webElement) {

        return getDriver().findElements(By.xpath(webElement));
    }
}
