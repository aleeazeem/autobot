package com.autodesk.bsm.pelican.ui.pages.events;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Page Objects represents Events page.
 *
 * @author mandas
 */
public class RepublishEventsPage extends GenericDetails {

    @FindBy(id = "filterType")
    private WebElement filterTypeSelect;

    @FindBy(id = "entityType")
    private WebElement entityTypeSelect;

    @FindBy(id = "changeNotificationType")
    private WebElement changeNotificationType;

    @FindBy(id = "requester")
    private WebElement requester;

    @FindBy(id = "channel")
    private WebElement channel;

    @FindBy(id = "input-idList")
    private WebElement idListTextArea;

    @FindBy(id = "input-firstId")
    private WebElement firstIdInput;

    @FindBy(id = "input-lastId")
    private WebElement lastIdInput;

    @FindBy(id = "input-startDate")
    private WebElement inputStartDate;

    @FindBy(id = "input-endDate")
    private WebElement inputEndDate;

    @FindBy(name = "startHour")
    private WebElement startHour;

    @FindBy(name = "startMinute")
    private WebElement startMinute;

    @FindBy(name = "startSecond")
    private WebElement startSecond;

    @FindBy(name = "endHour")
    private WebElement endHour;

    @FindBy(name = "endMinute")
    private WebElement endMinute;

    @FindBy(name = "endSecond")
    private WebElement endSecond;

    @FindBy(xpath = "//button[text()='Download']")
    private WebElement downloadButton;

    @FindBy(xpath = "//button[text()='Republish']")
    private WebElement republishButton;

    private static final String QUERY_RETURN_HUNG_JOBS =
        "SELECT exec.* FROM batch_job_execution exec JOIN batch_job_instance job on exec.job_instance_id = "
            + "job.job_instance_id where exec.status not in (\"COMPLETED\", \"FAILED\", \"STOPPED\") "
            + "and job.job_name like '%Republish%'";

    private SimpleDateFormat repubCNDateFormat = new SimpleDateFormat(PelicanConstants.REPUB_DATE_FORMAT);
    private DateFormat dbDateFormat = new SimpleDateFormat(PelicanConstants.AUDIT_LOG_DATE_FORMAT);

    private static HttpRestClient client = new HttpRestClient();

    private static final Logger LOGGER = LoggerFactory.getLogger(RepublishEventsPage.class.getSimpleName());

    public RepublishEventsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Navigate to the Events page in Admin Tool page
     */
    public void navigateToEventsPublishPage() {

        final String eventsUrl = "event/republishForm";
        final String url = getEnvironment().getAdminUrl() + "/" + eventsUrl;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to select Entity from the drop down list
     */
    public void selectEntity(final String entity) {
        getActions().select(entityTypeSelect, entity);
        LOGGER.info("Selected " + entity + " from Dropdown");
    }

    public void selectChangeNotificationType(final String notificationType) {
        getActions().select(changeNotificationType, notificationType);
        LOGGER.info("Selected change notification type as " + notificationType);
    }

    /**
     * Method to select Filter Type from the drop down
     */
    public void selectFilterType(final String filterType) {
        getActions().select(filterTypeSelect, filterType);
        LOGGER.info("Selected " + filterType + " from Dropdown");
    }

    /**
     * Click on republishButton to republish the events through job
     */
    public RepublishJobsPage clickRepublishButton() {
        republishButton.click();
        LOGGER.info("Clicked on Republish Button");
        return super.getPage(RepublishJobsPage.class);
    }

    /**
     * Method to select Requester and Channel from the drop down
     */
    public void selectRequesterAndChannel(final String requesterValue, final String channelValue) {

        selectRequester(requesterValue);
        selectChannel(channelValue);
    }

    /**
     * Method to select Requester from the drop down
     */
    public void selectRequester(final String requesterValue) {
        getActions().select(requester, requesterValue);
        LOGGER.info("Selected " + requesterValue + " from Requester Dropdown");
    }

    /**
     * Method to select Channel from the drop down
     */
    public void selectChannel(final String channelValue) {
        getActions().select(channel, channelValue);
        LOGGER.info("Selected " + channelValue + " from Channel Dropdown");
    }

    /**
     * Method to Find matching Entities by passing Array of ids list
     */
    public void selectIdList(final List<String> ids) {
        idListTextArea.sendKeys(ids.stream().collect(Collectors.joining(",")));
    }

    /**
     * Method to Find matching Entities by passing fist id and last id in the range
     */
    public void selectIdRange(final String firstId, final String lastId) {
        firstIdInput.sendKeys(firstId);
        lastIdInput.sendKeys(lastId);
        LOGGER.info("Entered First ID: " + firstId + " and Last ID:" + lastId + " for the ID Range");
    }

    /**
     * This method is used to fill hours, minutes and seconds in Start dropdown
     */
    private void fillStartTime(final String startTime) {
        getActions().select(startHour, startTime.split(":")[0]);
        getActions().select(startMinute, startTime.split(":")[1]);
        getActions().select(startSecond, startTime.split(":")[2]);
    }

    /**
     * This method is used to fill hours, minutes and seconds in End dropdown
     */
    private void fillEndTime(final String endTime) {
        getActions().select(endHour, endTime.split(":")[0]);
        getActions().select(endMinute, endTime.split(":")[1]);
        getActions().select(endSecond, endTime.split(":")[2]);
    }

    /**
     * Method to Find matching Entities by passing Start and End Date
     */
    public void selectDateRange(String startDate, String endDate) {
        startDate = startDate.split("\\.")[0];
        endDate = endDate.split("\\.")[0];
        inputStartDate.sendKeys(startDate.split(" ")[0]);
        fillStartTime(startDate.split(" ")[1]);
        inputEndDate.sendKeys(endDate.split(" ")[0]);
        fillEndTime(endDate.split(" ")[1]);
        LOGGER.info("Entered Start Date: " + startDate + " and End Date:" + endDate + " for the Date Range");
    }

    /**
     * Method to Find matching Entities by passing Start and End Date
     */
    public void selectDateRangeWithoutTime(final String startDate, final String endDate) {
        final String startTime = startDate.split("\\.")[0];
        final String endTime = endDate.split("\\.")[0];
        inputStartDate.sendKeys(startTime.split(" ")[0]);
        inputEndDate.sendKeys(endTime.split(" ")[0]);
        LOGGER.info("Entered Start Date: " + startTime + " and End Date:" + endTime + " for the Date Range");
    }

    /**
     * Method to Click on Find Matching Entities Button
     */
    public void clickFindMatchingEntities() {
        submit(TimeConstants.THREE_SEC);
    }

    /**
     * Method to Click on Download button
     */
    public void download() {
        downloadButton.click();
    }

    /**
     * Get the grid for the result set of find all with no id
     *
     * @return grid
     */
    public GenericGrid getGrid() {
        return super.getPage(GenericGrid.class);
    }

    /**
     * This method will return the total rows from find matches
     *
     * @return int total
     */
    public int getTotalCount() {
        return getGrid().getTotalItems();
    }

    /**
     * This method will return the page header
     *
     * @return String
     */
    public String getPageHeader() {
        return titleOfThePage.getText();
    }

    /**
     * Clear Hung Republish jobs
     */
    public static void checkAndClearRepublishHungJobs(final EnvironmentVariables environmentVariables) {
        final List<Map<String, String>> republishHungJobs =
            DbUtils.selectQueryFromWorkerDb(QUERY_RETURN_HUNG_JOBS, environmentVariables);
        if (republishHungJobs.size() > 0) {
            for (final Map<String, String> republishHungJob : republishHungJobs) {
                final CloseableHttpResponse response = client.doGet(environmentVariables.getRepublishTriggerUrl()
                    + "/triggers-workers/api/jobs/changeNotifications/batch/" + republishHungJob.get("JOB_EXECUTION_ID")
                    + "/status", null, null, null, null);
                final int status = response.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                    LOGGER.error("Unable to Force Fail a hung Republish Job. Got status code of " + status);
                    RestClientUtils.parseErrorResponse(response);
                }
            }
        } else {
            LOGGER.info("No Republish Trigger Jobs are in Hung state");
        }
    }
}
