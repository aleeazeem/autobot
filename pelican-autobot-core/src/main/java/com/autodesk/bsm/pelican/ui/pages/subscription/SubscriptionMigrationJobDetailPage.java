package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.reports.WorkInProgressReportResultPage;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Page class for subscription migration job detail page (Sherpa).
 *
 * @author jains and yerragv
 */

public class SubscriptionMigrationJobDetailPage extends GenericGrid {

    public SubscriptionMigrationJobDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "ui-id-1")
    private WebElement detailsTab;

    @FindBy(xpath = ".//*[@id='price-matching-tabs']/ul/li[last()]")
    private WebElement errorsTab;

    @FindBy(id = "banner")
    private WebElement banner;

    @FindBy(xpath = ".//*[@name='Run']")
    private WebElement runButton;

    @FindBy(xpath = ".//*[@name='Delete']")
    private WebElement deleteButton;

    @FindBy(xpath = ".//*[@id='Application Family-prop']//dd[last()-3]//a")
    private WebElement triggersJobLink;

    @FindBy(name = "Cancel")
    private WebElement cancelButton;

    @FindBy(name = "Re-Upload")
    private WebElement reUploadButton;

    @FindBy(xpath = ".//*[@id='find-results']/div[2]/table/tbody//td[2]")
    private WebElement sourcePlanName;

    @FindBy(xpath = ".//*[@id='find-results']/div[2]/table/tbody/tr/td[6]")
    private WebElement targetPlanName;

    @FindBy(name = "Rollback")
    private WebElement rollBackButton;

    @FindBy(xpath = ".//*[@id='bd']/div[2]//div[2]//h2")
    private WebElement rollBackHeaderDetails;

    @FindBy(xpath = ".//*[@id='Parent Job-prop']/dd/a")
    private WebElement parentJobLink;

    @FindBy(id = "ui-id-1")
    private WebElement migrationResultsTab;

    @FindBy(xpath = ".//*[@class='inner-inner']//dt[contains(text(),'Migration Errors:')]/following-sibling::dd[1]")
    private WebElement migrationErrorsDownloadLink;

    @FindBy(css = ".none-found")
    private WebElement noneFound;

    @FindBy(xpath = ".//*[@class='inner-inner']//dt[contains(text(),'Mapping Errors:')]/following-sibling::dd[1]/a")
    private WebElement mappingErrorsDownloadLink;

    private static final int TOTAL_COLUMNS_ON_DETAILS_TAB = 12;
    private static final int TOTAL_COLUMNS_ON_ERRORS_TAB = 3;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(SubscriptionMigrationJobDetailPage.class.getSimpleName());

    /**
     * Method to return application family
     *
     * @return String.
     */
    public String getApplicationFamily() {
        return getFieldValueByKey(PelicanConstants.APPLICATION_FAMILY);
    }

    /**
     * Method to return Id
     *
     * @return String.
     */
    public String getId() {
        return getFieldValueByKey(PelicanConstants.ID_FIELD);
    }

    /**
     * Method to return job name
     *
     * @return String.
     */
    public String getJobName() {
        return getFieldValueByKey("Job Name");
    }

    /**
     * Method to return Created
     *
     * @return String.
     */
    public String getCreated() {
        return getFieldValueByKey(PelicanConstants.CREATED_FIELD);
    }

    /**
     * Method to return Created By
     *
     * @return String.
     */
    public String getCreatedBy() {
        return getFieldValueByKey(PelicanConstants.CREATED_BY_FIELD);
    }

    /**
     * Method to return Last Modified
     *
     * @return String.
     */
    public String getLastModified() {
        return getFieldValueByKey(PelicanConstants.LAST_MODIFIED_FIELD);
    }

    /**
     * Method to return Last Modified By
     *
     * @return String.
     */
    public String getLastModifiedBy() {
        return getFieldValueByKey(PelicanConstants.LAST_MODIFIED_BY_FIELD);
    }

    /**
     * Method to return Run Date
     *
     * @return String.
     */
    public String getRunDate() {
        return getFieldValueByKey("Run Date");
    }

    /**
     * Method to return Run By
     *
     * @return String.
     */
    public String getRunBy() {
        return getFieldValueByKey("Run By");
    }

    /**
     * Method to return File Upload Job Id
     *
     * @return String.
     */
    public String getFileUploadJobId() {
        return getFieldValueByKey("File Upload Job Id");
    }

    /**
     * Method to return Status
     *
     * @return String.
     */
    public String getStatus() {
        return getFieldValueByKey(PelicanConstants.STATUS_FIELD);
    }

    /**
     * Method to return the text from the parent job field
     *
     * @return String - parent job name
     */
    public String getParentJob() {
        return getFieldValueByKey(PelicanConstants.PARENT_JOB);
    }

    /**
     * This is the method to return the roll back job name
     *
     * @return String - roll back job name
     */
    public String getRollBackJob() {
        return getFieldValueByKey(PelicanConstants.ROLLBACK_JOB);
    }

    /**
     * This is the method to return the status of the roll back job
     *
     * @return String - Roll Back Job Status
     */
    public String getRollBackJobStatus() {
        return getFieldValueByKey(PelicanConstants.Rollback_JOB_STATUS);
    }

    /**
     * Method click on errors tab
     */
    public void clickOnErrorsTab() {
        errorsTab.click();
        LOGGER.info("Clicked on errors tab");
    }

    /**
     * Method click on migration results tab
     */
    public void clickOnMigrationResultsTab() {
        migrationResultsTab.click();
        LOGGER.info("Clicked on migration results tab");
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * method to return values of column Source Plan Name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourcePlanNameHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.SOURCE_PLAN_NAME);
    }

    /**
     * method to return values of column Source Offer Name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourceOfferNameHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.SOURCE_OFFER_NAME);
    }

    /**
     * method to return values of column Source Price ID
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourcePriceIdHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.SOURCE_PRICE_ID);
    }

    /**
     * method to return values of column Source Amount
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourceAmountHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.SOURCE_AMOUNT);
    }

    /**
     * method to return values of column Target Plan Name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPlanNameHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.TARGET_PLAN_NAME);
    }

    /**
     * method to return values of column Target Offer Name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetOfferNameHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.TARGET_OFFER_NAME);
    }

    /**
     * method to return values of column Target Offer Name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPriceIdHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.TARGET_PRICE_ID);
    }

    /**
     * method to return values of column Target Amount
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetAmountHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.TARGET_AMOUNT);
    }

    /**
     * method to return values of column Store
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfStoreHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.STORE_UPLOAD);
    }

    /**
     * method to return values of column Count
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSubscriptionCountHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.SUBSCRIPTION_COUNT);
    }

    /**
     * method to return values of column Mapping Results
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfMappingResultsHeader() {
        return getColumnValuesOnDetailsTab(PelicanConstants.MAPPING_RESULTS);
    }

    /**
     * method to return values of column Errors
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfErrorHeader() {
        return getColumnValuesOnErrorTab(PelicanErrorConstants.ERRORS_UPLOAD);
    }

    /**
     * method to return values of column Warnings
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfHeader() {
        return getColumnValuesOnErrorTab(PelicanErrorConstants.WARNINGS_UPLOAD);
    }

    /**
     * method to return values of column Subscription Id
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSubscriptionIdHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SUBSCRIPTION_ID);
    }

    /**
     * method to return values of column Source Plan id
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourcePlanIdHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_PLAN_ID);
    }

    /**
     * method to return values of column Source Plan External Key
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourcePlanExternalKeyHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_PLAN_EXTERNAL_KEY);
    }

    /**
     * method to return values of column Source offer external key
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourceOfferExternalKeyHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_OFFER_EXTERNAL_KEY);
    }

    /**
     * method to return values of column source offer name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourceOfferNameHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_OFFER_NAME);
    }

    /**
     * method to return values of column source offer price id
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourcePriceIdHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_PRICE_ID);
    }

    /**
     * method to return values of column source amount
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourceAmountHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_AMOUNT);
    }

    /**
     * method to return values of source price end date
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfSourcePriceEndDateHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.SOURCE_PRICE_END_DATE);
    }

    /**
     * method to return values of column Target Price ID
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPriceIdHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_PRICE_ID);
    }

    /**
     * method to return values of column target Plan id
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPlanIdHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_PLAN_ID);
    }

    /**
     * method to return values of column target Plan external key
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPlanExternalKeyOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_PLAN_EXTERNAL_KEY);
    }

    /**
     * method to return values of column target offer external key
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetOfferExternalKeyOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_OFFER_EXTERNAL_KEY);
    }

    /**
     * method to return values of column target offer name
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetOfferNameOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_OFFER_NAME);
    }

    /**
     * method to return values of column target price id
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPriceIdOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_PRICE_ID);
    }

    /**
     * method to return values of column target amount
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetAmountOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_AMOUNT);
    }

    /**
     * method to return values of source price end date
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfTargetPriceEndDateHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.TARGET_PRICE_END_DATE);
    }

    /**
     * method to return values of store
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfStoreHeaderOnMigrationResultsTab() {
        return getColumnValuesOnMigrationResultTab(PelicanConstants.STORE_UPLOAD);
    }

    /**
     * Method to get text of file uploading Method to get the value of triggers job run id
     *
     * @return String.
     */
    public String getTriggersJobRunId() {
        return getFieldValueByKey("Triggers Job Run Id");
    }

    /**
     * This is the method to return the source plan name in price mapping table
     *
     * @return String
     */
    public String getSourcePlanNameInPriceMapping() {
        return (sourcePlanName.getText());
    }

    /**
     * This is the method to return the target plan name in price mapping table
     *
     * @return String
     */
    public String getTargetPlanNameInPriceMapping() {
        return (targetPlanName.getText());
    }

    /**
     * This is a method which will run the migration job and refresh the page after job has run.
     */
    public void runAJob() {
        runJob();
        refreshPage();
    }

    /**
     * This is a method which will run the migration job but will not refresh the page after job has run.
     */
    public void runAJobWithOutPageRefresh() {
        runJob();
    }

    private void runJob() {
        getActions().click(runButton);
        LOGGER.info("Clicking on the run button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        switchDriverControlToPopUp();
        getActions().click(confirmButton);
        switchDriverControlToParentWindow();
        Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);

    }

    /**
     * @return the presence of Run Button
     */
    public boolean isRunButtonPresent() {
        LOGGER.info("Checking whether the re-upload button is displayed on the page");
        return isElementPresent(runButton);
    }

    /**
     * This is the method which will delete a migration job.
     */
    public void deleteAJob() {
        getActions().click(deleteButton);
        LOGGER.info("Clicking on the delete button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        switchDriverControlToPopUp();
        getActions().click(confirmButton);
        switchDriverControlToParentWindow();
    }

    /**
     * This is a method to click on the triggers job link
     *
     * @return WorkInProgressReportPage.
     */
    public WorkInProgressReportResultPage clickOnTriggersJobId() {
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        getActions().click(triggersJobLink);
        LOGGER.info("Clicking on the triggers job id link");

        return super.getPage(WorkInProgressReportResultPage.class);
    }

    /*
     * Method to get error or info message.
     *
     * @return String
     */
    public String getErrorOrInfoMessage() {
        return banner.getText();
    }

    /**
     * Method to get column values on details tab per specified column
     *
     * @param columnName
     * @return List<String>
     */
    private List<String> getColumnValuesOnDetailsTab(final String columnName) {
        final String columnHeaderSelector = ".//*[@id='details-tab']//table/thead/tr/th";
        final int columnIndex = getColumnIndexOnResultTab(columnName, columnHeaderSelector) + 1;
        final String columnValuesSelector = ".//*[@id='details-tab']//table/tbody//td[" + columnIndex + "]";
        return getColumnValuesOnTab(columnValuesSelector);
    }

    /**
     * This is a method to return the roll back header details
     *
     * @return
     */
    public String getRollBackHeaderDetails() {
        return rollBackHeaderDetails.getText();
    }

    /**
     * The column values per specified column This is the method which will check whether the cancel button is present
     * on the subscription migration job detail page.
     *
     * @return boolean - true or false based on element exists or not.
     */
    public boolean isCancelButtonPresent() {

        LOGGER.info("Checking whether the cancel button is displayed on the page");

        return isElementPresent(cancelButton);
    }

    /**
     * This is the method which will click on cancel button and confirm the popup
     *
     * @return SubscriptionMigrationDetailPage
     */
    public SubscriptionMigrationJobDetailPage clickOnCancelButton() {

        getActions().click(cancelButton);
        LOGGER.info("Clicked on the cancel button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        switchDriverControlToPopUp();
        getActions().click(confirmButton);
        LOGGER.info("Confirmation button has been clicked on the popup window");
        switchDriverControlToParentWindow();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        return super.getPage(SubscriptionMigrationJobDetailPage.class);
    }

    /**
     * The column values per specified column Method to get column values on errors tab per specified column
     *
     * @param columnName
     * @return List<String>
     */
    private List<String> getColumnValuesOnErrorTab(final String columnName) {
        final String columnHeaderSelector = ".//*[@id='errors-tab']//table/thead/tr/th";
        final int columnIndex = getColumnIndexOnResultTab(columnName, columnHeaderSelector) + 1;
        final String columnValuesSelector = ".//*[@id='errors-tab']//table/tbody//td[" + columnIndex + "]";
        return getColumnValuesOnTab(columnValuesSelector);
    }

    /**
     * Method to get column values migration result table per specified column
     *
     * @param columnName
     * @return List<String>
     */
    private List<String> getColumnValuesOnMigrationResultTab(final String columnName) {
        final String columnHeaderSelector = ".//*[@id='results-tab']//table/thead/tr/th";
        final int columnIndex = getColumnIndexOnResultTab(columnName, columnHeaderSelector) + 1;
        final String columnValuesSelector = ".//*[@id='results-tab']//table/tbody//td[" + columnIndex + "]";
        return getColumnValuesOnTab(columnValuesSelector);
    }

    /**
     * Method to get column values per specified column
     *
     * @param columnName
     * @return List<String>
     */
    private List<String> getColumnValuesOnTab(final String selector) {
        final List<String> values = new ArrayList<>();
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

    /**
     * This is the method to return the target plan name in price mapping table /** Method to get the column index for
     * the given column name in a table
     *
     * @param columnName
     * @return index of given column name
     */
    private int getColumnIndexOnResultTab(final String columnName, final String selector) {
        int columnIndex = 0;
        boolean foundColumn = false;

        // Get all the header elements
        final List<WebElement> headerElements = getDriver().findElements(By.xpath(selector));
        for (final WebElement element : headerElements) {
            if (columnName.equalsIgnoreCase(element.getText())) {
                foundColumn = true;
                break;
            }
            columnIndex++;
        }

        if (!foundColumn) {
            Assert.fail("Unable to get column index for " + columnName);
        }

        return columnIndex;
    }

    /**
     * This is the method which will check whether the re-upload button is present on the subscription migration job
     * detail page.
     *
     * @return boolean - true or false based on element exists or not.
     */
    public boolean isReUploadButtonPresent() {

        LOGGER.info("Checking whether the re-upload button is displayed on the page");
        return isElementPresent(reUploadButton);
    }

    public UploadSubscriptionMigrationPage clickOnReUploadButton() {

        getActions().click(reUploadButton);
        LOGGER.info("Clicked on the re-upload button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        switchDriverControlToPopUp();
        getActions().click(confirmButton);
        LOGGER.info("Confirmation button has been clicked on the popup window");
        switchDriverControlToParentWindow();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        return super.getPage(UploadSubscriptionMigrationPage.class);
    }

    /**
     * This is a method which will check whether the cancel button is clickable or not
     *
     * @return true or false
     */
    public boolean isCancelButtonClickable() {

        getActions().click(cancelButton);
        LOGGER.info("Clicking on the cancel button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final Set<String> windowHandles = getDriver().getWindowHandles();
        final int numberOfWindowHandles = windowHandles.size();
        LOGGER.info("Number of window handles is :" + numberOfWindowHandles);

        return numberOfWindowHandles != 1;

    }

    /**
     * This is a method which will check whether the re-upload button is clickable or not
     *
     * @return true or false
     */
    public boolean isReUploadButtonclickable() {

        getActions().click(reUploadButton);
        LOGGER.info("Clicked on the re-upload button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final Set<String> windowHandles = getDriver().getWindowHandles();
        final int numberOfWindowHandles = windowHandles.size();
        LOGGER.info("Number of window handles is :" + numberOfWindowHandles);

        return numberOfWindowHandles != 1;

    }

    /**
     * This is the method which will check whether the roll back button is present on the subscription migration job
     * detail page.
     *
     * @return boolean - true or false based on element exists or not.
     */
    public boolean isRollbackButtonPresent() {

        LOGGER.info("Checking whether the roll back button is displayed on the page");

        return isElementPresent(rollBackButton);
    }

    /**
     * This is the method to click on the parent job name link
     */
    public void clickOnParentJobName() {

        LOGGER.info("Clicking on the parent job name link");
        getActions().click(parentJobLink);
    }

    /**
     * This is the method which will click on roll back button and confirm the popup
     *
     * @return SubscriptionMigrationDetailPage
     */
    public SubscriptionMigrationJobDetailPage clickOnRollbackButton() {

        getActions().click(rollBackButton);
        LOGGER.info("Clicked on the roll back button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        switchDriverControlToPopUp();
        getActions().click(confirmButton);
        LOGGER.info("Confirmation button has been clicked on the popup window");
        switchDriverControlToParentWindow();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        return super.getPage(SubscriptionMigrationJobDetailPage.class);
    }

    /**
     * This is a method which will check whether the Roll back button is clickable or not
     *
     * @return true or false
     */
    public boolean isRollBackButtonClickable() {

        getActions().click(rollBackButton);
        LOGGER.info("Clicking on the roll back button");
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final Set<String> windowHandles = getDriver().getWindowHandles();
        final int numberOfWindowHandles = windowHandles.size();
        LOGGER.info("Number of window handles is :" + numberOfWindowHandles);

        return numberOfWindowHandles != 1;
    }

    /**
     * Method to navigate to subscription migration detail page with the given job id.
     *
     * @param jobId
     * @return SubscriptionMigrationDetailPage
     */
    public SubscriptionMigrationJobDetailPage navigateToSubscriptionMigrationJobDetailPage(final String jobId) {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm() + "/"
            + AdminPages.SUBSCRIPTION_MIGRATION_JOB_DETAIL.getForm() + "?id=" + jobId;
        getDriver().get(url);
        LOGGER.info("Navigated to page: " + url);
        return super.getPage(SubscriptionMigrationJobDetailPage.class);

    }

    /**
     * Method to click on list of Subscription on errors tab.
     *
     * @param index
     */
    public void clickSubscriptionList(final int index) {
        getDriver().findElements(By.linkText(PelicanErrorConstants.LIST_OF_SUBSCRIPTION_WARNING_MESSAGE)).get(index)
            .click();
        LOGGER.info("Clicked on subscription detail list.");
    }

    /**
     * Get column headers of mapping details tab.
     *
     * @return List<String>
     */
    public List<String> getMappingDetailsColumnHeaders() {
        return getColumnHeaders("details-tab");
    }

    /**
     * Get column headers of mapping errors tab.
     *
     * @return List<String>
     */
    public List<String> getMappingErrosColumnHeaders() {
        return getColumnHeaders("errors-tab");
    }

    /**
     * Get column headers of the tabs.
     *
     * @param tabId
     * @return List<String>
     */
    private List<String> getColumnHeaders(final String tabId) {

        final List<String> headers = new ArrayList<>();

        // Get all the header elements
        final List<WebElement> headerElements =
            getDriver().findElements(By.xpath(".//*[@id='" + tabId + "']//table / thead / tr / th"));
        for (final WebElement element : headerElements) {
            final String header = element.getText();
            LOGGER.info("Column header: '" + header + "'");
            headers.add(header);
        }

        return headers;
    }

    /**
     * method to get all values of warning header
     *
     * @return list of strings
     */
    public List<String> getColumnValuesOfWarningHeader() {
        return getColumnValuesOnErrorTab(PelicanErrorConstants.WARNINGS_UPLOAD);
    }

    /**
     * method to get whether or not results are found on error tabs
     */
    public boolean isNoneFoundPresent() {
        boolean isNoneFoundPresent = false;
        try {
            isNoneFoundPresent = noneFound.isDisplayed();
            LOGGER.info("Promotion Exists");
        } catch (final org.openqa.selenium.NoSuchElementException ex) {
            LOGGER.info("Promotion Doesn't Exist");
        }
        return isNoneFoundPresent;
    }

    /**
     * This is the method to click on migration errors download link under related actions
     */
    public void clickOnMigrationErrorsDownloadLink(final int index) {
        getDriver().findElements(By.linkText(PelicanConstants.DOWNLOAD)).get(index).click();
        LOGGER.info("Migration errors download link has been clicked");
    }

    /**
     * This is the method to click on mapping errors download link under related actions
     */
    public void clickOnMappingErrorsDownloadLink() {
        getActions().click(mappingErrorsDownloadLink);
        LOGGER.info("Mapping errors download link has been clicked");
    }
}
