package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is a page class for the work in progress report page
 *
 * @author yerragv.
 */
public class WorkInProgressReportResultPage extends GenericGrid {

    public WorkInProgressReportResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = ".//*[@id='find-results']//p")
    private WebElement resultsText;

    @FindBy(xpath = ".//*[@id='find-results']/div[3]//td[8]/a")
    private WebElement errorsLink;

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkInProgressReportResultPage.class.getSimpleName());

    /**
     * This is a method to return the count of total records on the page
     *
     * @return int - total records count as integer value.
     */
    public int getTotalResultsInTheReportPage() {
        final String text = resultsText.getText();
        LOGGER.info("Text on the page:" + text);

        return Integer.parseInt(text.split(" ")[3]);
    }

    /**
     * This method will retrieve the column values for column 'STATE' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfState() {
        return getColumnValues(PelicanConstants.STATE);
    }

    /**
     * This method will retrieve the column values for column 'OBJECT ID' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfObjectId() {
        return getColumnValues(PelicanConstants.OBJECT_ID);
    }

    /**
     * This method will retrieve the column values for column 'Job GUID' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfJobGuid() {
        return getColumnValues(PelicanConstants.JOB_GUID);
    }

    /**
     * This method will retrieve the column values for column 'WipGUID' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfWipGuid() {
        return getColumnValues(PelicanConstants.WIP_GUID);
    }

    /**
     * This method will retrieve the column values for column 'Object Type' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfObjectType() {
        return getColumnValues(PelicanConstants.OBJECT_TYPE);
    }

    /**
     * This method will retrieve the column values for column 'Created Date' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfCreatedDate() {
        return getColumnValues(PelicanConstants.CREATED_DATE);
    }

    /**
     * This method will retrieve the column values for column 'Last Modified Date' from the report result page.
     *
     * @return List<String> - column values.
     */
    public List<String> getColumnValuesOfLastModifiedDate() {
        return getColumnValues(PelicanConstants.LAST_MODIFIED_DATE_FIELD);
    }

    /**
     * Method to click on object id.
     *
     * @param objectId
     * @return WorkInProgressSubscriptionDetailsPage
     */
    public WorkInProgressSubscriptionDetailsPage clickOnObjectId(final String objectId) {
        getDriver().findElement(By.linkText(objectId)).click();
        return getPage(WorkInProgressSubscriptionDetailsPage.class);
    }
}
