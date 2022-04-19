package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.UploadBasicOfferingsPage;
import com.autodesk.bsm.pelican.ui.pages.features.UploadFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.UploadFeaturesStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionsStatusPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.UploadSubscriptionPlanPage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class UploadUtils extends GenericDetails {
    public UploadUtils(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    // file Input
    @FindBy(id = "input-file")
    private WebElement xlsxInput;

    // upload file path
    private String uploadFilePath =
        System.getProperty("user.dir") + "/../pelican-autobot-core/src/test/resources/testdata/";
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadUtils.class.getSimpleName());

    /**
     * This method helps to upload a file for Subscription Plan upload
     *
     * @param fileName - a fileName with .xlsx or xls extension saved in pelican/testdata folder
     */
    public GenericGrid uploadSubscriptionPlan(final AdminToolPage adminToolPage, final String fileName) {
        final UploadSubscriptionPlanPage uploadSubscriptionPlanPage =
            adminToolPage.getPage(UploadSubscriptionPlanPage.class);
        try {
            uploadSubscriptionPlanPage.navigateToSubscriptionPlanUploadPage();
            setUploadFile(fileName, TimeConstants.SHORT_WAIT);
            Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return super.getPage(GenericGrid.class);
    }

    /**
     * This method helps to upload a file for basic offering upload.
     *
     * @param fileName - a fileName with .xlsx or xls extension saved in pelican/testdata folder
     */
    public GenericGrid uploadBasicOffering(final AdminToolPage adminToolPage, final String fileName) {
        final UploadBasicOfferingsPage uploadBasicOfferingsPage = adminToolPage.getPage(UploadBasicOfferingsPage.class);
        try {
            uploadBasicOfferingsPage.navigateToBasicOfferingUploadPage();
            setUploadFile(fileName, TimeConstants.SHORT_WAIT);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return super.getPage(GenericGrid.class);
    }

    /**
     * This method helps to upload a file for feature upload.
     *
     * @param fileName - a fileName with .xlsx or xls extension saved in pelican/testdata folder
     * @return UploadFeaturePage
     */
    public UploadFeaturesStatusPage uploadFeature(final AdminToolPage adminToolPage, final String fileName) {
        final UploadFeaturePage uploadFeaturePage = adminToolPage.getPage(UploadFeaturePage.class);
        try {
            uploadFeaturePage.navigateToFeatureUploadPage();
            selectApplication(environmentVariables.getApplicationDescription());
            setUploadFile(fileName, TimeConstants.SHORT_WAIT);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return super.getPage(UploadFeaturesStatusPage.class);
    }

    /**
     * Method to upload subscriptions.
     *
     * @param fileName - a fileName with .xlsx or xls extension saved in pelican/testdata folder
     */
    public UploadSubscriptionsStatusPage uploadSubscriptions(final AdminToolPage adminToolPage, final String fileName) {
        final UploadSubscriptionsPage uploadSubscriptionPage = adminToolPage.getPage(UploadSubscriptionsPage.class);
        try {
            uploadSubscriptionPage.navigateToSubscriptionUploadPage();
            setUploadFile(fileName, TimeConstants.SHORT_WAIT);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(UploadSubscriptionsStatusPage.class);
    }

    /**
     * Method used to delete file from local.
     */
    public boolean deleteFilefromLocal(final String fileName) {
        Boolean removeFile;
        LOGGER.info("*** upload file path: " + uploadFilePath);
        final File file = new File(uploadFilePath + "/" + fileName);
        removeFile = file.delete();

        return removeFile;
    }

    /**
     * Method used to click upload button without providing any file.
     */
    public void clickUploadButtonWithoutAnyFile(final AdminToolPage adminToolPage) {
        final UploadSubscriptionsPage uploadSubscriptionPage = adminToolPage.getPage(UploadSubscriptionsPage.class);
        uploadSubscriptionPage.navigateToSubscriptionUploadPage();
        submitButton.click();
    }

    /**
     * Method used to get the path of the file.
     *
     * @return path of the file
     */
    public String getFilePath(final String fileName) {
        final String fileFullName = Util.getTestRootDir() + "/src/test/resources/testdata/" + fileName;
        LOGGER.info("FileName : " + fileFullName);
        return fileFullName;
    }

}
