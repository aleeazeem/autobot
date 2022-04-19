package com.autodesk.bsm.pelican.ui.pages.subscription;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object represent the Upload Subscription status Page. Access via Subscriptions | Subscription | Upload | Status
 * page
 *
 * @author t_mohag
 */
public class UploadSubscriptionsStatusPage extends GenericDetails {

    public UploadSubscriptionsStatusPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // Find file name in upload subscriptions status page
    @FindBy(xpath = "//*[@id='find-results']/div[2]/table/tbody/tr[1]/td[6]")
    private WebElement fileName;

    // Find S3 file name in upload subscriptions status page
    @FindBy(xpath = "//*[@id='bd']/div[2]/div/div/dl[2]/dl/dd")
    private WebElement s3FileName;

    @FindBy(xpath = "//*[@class='o']/td[9]")
    private WebElement uploadStatus;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionsStatusPage.class.getSimpleName());

    /**
     * Method to navigate to upload subscription status page
     */
    public void navigateToSubscriptionsUploadStatusPage() {
        final String subscriptionImport = getEnvironment().getAdminUrl() + "/" + AdminPages.SUBSCRIPTION.getForm() + "/"
            + AdminPages.IMPORT.getForm();
        if (!isPageValid(subscriptionImport)) {
            getDriver().get(subscriptionImport);
        }
    }

    /**
     * Method to get file name
     */
    public String getFileName() {
        final String fileName = this.fileName.getText();
        LOGGER.info("File name is '" + fileName + "'");
        return fileName;
    }

    /**
     * Method to get S3 file name
     */
    public String getS3FileName() {
        final String s3FileName = this.s3FileName.getText();
        LOGGER.info("S3 File name is '" + s3FileName + "'");
        return s3FileName;
    }

    /**
     * This method returns the upload status of subscription
     *
     * @return upload status
     */
    public String getUploadStatus() {

        LOGGER.info("Upload status is : " + uploadStatus.getText());
        return uploadStatus.getText();
    }
}
