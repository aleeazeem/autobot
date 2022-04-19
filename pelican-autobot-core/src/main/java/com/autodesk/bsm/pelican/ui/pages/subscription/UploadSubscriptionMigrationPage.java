package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.Wait;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Page class to upload subscription migration file (Sherpa).
 *
 * @author jains
 */

public class UploadSubscriptionMigrationPage extends GenericDetails {

    public UploadSubscriptionMigrationPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "input-jobName")
    private WebElement jobNameInput;

    @FindBy(id = "jobName")
    private WebElement jobNameInputInReUpload;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionMigrationPage.class.getSimpleName());

    /**
     * Method to upload a valid subscription migration file. No errors should be thrown on
     * UploadSubscriptionMigrationPage. SubscriptionMigrationDetailPage will be returned after waiting for 2 seconds
     * after upload.
     *
     * @return SubscriptionMigrationDetailPage
     */
    public SubscriptionMigrationJobDetailPage uploadSubscriptionMigrationFile(final String jobName,
        final String fileName) {
        uploadSubscriptionMigrationFileHelper(jobName, fileName);
        Util.waitInSeconds(TimeConstants.SHORT_WAIT);
        return super.getPage(SubscriptionMigrationJobDetailPage.class);
    }

    /**
     * Method to upload a valid subscription migration file. No errors should be thrown on
     * UploadSubscriptionMigrationPage. SubscriptionMigrationDetailPage will be immediately returned after upload
     * without any wait.
     *
     * @return SubscriptionMigrationDetailPage
     */
    public SubscriptionMigrationJobDetailPage uploadSubscriptionMigrationFileWithoutWait(final String jobName,
        final String fileName) {
        uploadSubscriptionMigrationFileHelper(jobName, fileName);
        return super.getPage(SubscriptionMigrationJobDetailPage.class);
    }

    /**
     * Method to reupload a valid subscription migration file. No errors should be thrown on
     * UploadSubscriptionMigrationPage. SubscriptionMigrationDetailPage will be immediately returned after upload
     * without any wait.
     *
     * @param JobName
     * @param FileName
     * @return SubscriptionMigrationDetailPage
     */
    public SubscriptionMigrationJobDetailPage reUploadSubscriptionMigrationFileWithoutWait(final String jobName,
        final String fileName) {
        reUploadSubscriptionMigrationFile(jobName, fileName);
        return super.getPage(SubscriptionMigrationJobDetailPage.class);
    }

    /**
     * Method to upload a subscription migration file when the page throws an error.
     *
     * @return UploadSubscriptionMigrationPage
     */
    public UploadSubscriptionMigrationPage uploadSubscriptionMigrationFileError(final String jobName,
        final String fileName) {
        uploadSubscriptionMigrationFileHelper(jobName, fileName);
        return super.getPage(UploadSubscriptionMigrationPage.class);
    }

    /**
     * Method to set job name.
     */
    private void setJobName(final String jobName) {
        getActions().setText(jobNameInput, jobName);
        LOGGER.info("Job name is set: " + jobName);
    }

    /**
     * Method to set job name.
     *
     * @param JobName
     */
    private void setJobNameInReUpload(final String jobName) {
        jobNameInputInReUpload.clear();
        getActions().setText(jobNameInputInReUpload, jobName);
        LOGGER.info("Job name is set: " + jobName);
    }

    /**
     * Method to navigate to subscription migration upload page.
     */
    private void navigateToUploadSubscriptionMigrationPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm() + "/"
            + AdminPages.UPLOAD_SUBSCRIPTION_MIGRATION.getForm();
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        Wait.pageLoads(driver);
        LOGGER.info("Navigated to page: " + url);
    }

    /**
     * Method to set job name and upload subscription migration file.
     */
    private void uploadSubscriptionMigrationFileHelper(final String jobName, final String fileName) {
        try {
            navigateToUploadSubscriptionMigrationPage();
            setJobName(jobName);
            setUploadFile(fileName, TimeConstants.TWO_SEC);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * Method to set job name and upload subscription migration file.
     */
    private void reUploadSubscriptionMigrationFile(final String jobName, final String fileName) {
        try {
            setJobNameInReUpload(jobName);
            setUploadFile(fileName, TimeConstants.TWO_SEC);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    /**
     * This is a method to create and write data to xlsx file.
     *
     * @param fileName - File Name is passed as parameter
     * @param subscriptionOfferingExternalKey1 - source subscriptionOfferingExternalKey1
     * @param offerExternalKey1 - source offerExternalKey1
     * @param subscriptionOfferingExternalKey2 - target subscriptionOfferingExternalKey1
     * @param offerExternalKey2 - target offerExternalKey2
     */
    public void createXlsxAndWriteData(final String fileName, final String subscriptionOfferingExternalKey1,
        final String offerExternalKey1, final String subscriptionOfferingExternalKey2, final String offerExternalKey2) {

        try {
            final XlsUtils utils = new XlsUtils();

            final ArrayList<String> columnHeaders = new ArrayList<>();
            final ArrayList<String> columnData = new ArrayList<>();

            // Add store header values to list
            columnHeaders.add("Source Plan XKEY,Source Offer XKEY,Target Plan XKEY,Target Offer XKEY");
            columnData.add(subscriptionOfferingExternalKey1 + "," + offerExternalKey1 + ","
                + subscriptionOfferingExternalKey2 + "," + offerExternalKey2);

            // Append the Data to the excel file
            utils.createAndWriteToXls(fileName, columnHeaders, columnData, false);

        } catch (final Exception ex) {
            LOGGER.info(ex.getMessage());
        }
    }

}
