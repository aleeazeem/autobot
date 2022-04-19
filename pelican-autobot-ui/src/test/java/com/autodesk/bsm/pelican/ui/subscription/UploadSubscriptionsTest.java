package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionAuditLogHelper;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UploadSubscriptionsStatusPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This test class verifies subscription upload page in Admin Tool
 *
 * @author t_mohag
 */
@Test(groups = { "excludedClass" })
public class UploadSubscriptionsTest extends SeleniumWebdriver {

    private static final String UTF8_FILE_NAME = "UploadSubscriptions赛巴巴.xlsx";
    private static final String INVALID_FILE_NAME = "invalid_file_name.csv";
    private AdminToolPage adminToolPage;
    private UploadSubscriptionsPage uploadSubscriptionsPage;
    private UploadUtils uploadUtils;
    private XlsUtils xlsUtils;
    private static final String QUANTITY = "4";
    private static final String OXYGEN_ID = "AutoUploadSubscription";
    private String userId;
    private String priceId;
    private String planId;
    private String ownerId;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSubscriptionsTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        uploadSubscriptionsPage = adminToolPage.getPage(UploadSubscriptionsPage.class);
        uploadUtils = adminToolPage.getPage(UploadUtils.class);

        xlsUtils = new XlsUtils();
        userId = getEnvironmentVariables().getUserId();
        priceId = getEnvironmentVariables().getPriceId();
        planId = getEnvironmentVariables().getPlanId();
        ownerId = getEnvironmentVariables().getOwnerId();
    }

    /**
     * Test Method to Upload Subscriptions to verify UTF8 FileName
     */
    @Test
    public void uploadSubscriptionsUsingUTF8FileNameSuccess() throws IOException {
        // Create and upload a file
        createXlsxAndWriteData(UTF8_FILE_NAME);
        final UploadSubscriptionsStatusPage statusPage = uploadUtils.uploadSubscriptions(adminToolPage, UTF8_FILE_NAME);
        LOGGER.info(getDriver().getCurrentUrl());

        if (uploadUtils.deleteFilefromLocal(UTF8_FILE_NAME)) {
            LOGGER.info(UTF8_FILE_NAME + " is successfully deleted from /testdata");
        } else {
            LOGGER.warn(UTF8_FILE_NAME + " is NOT deleted from /testdata");
        }

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - View Upload Status"), assertionErrorList);

        // Validate the fileName in the upload status page
        AssertCollector.assertThat("File name doesn't match", statusPage.getFileName(), equalTo(UTF8_FILE_NAME),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that the invalid filename (other than .xlsx) provided for upload results in failure.
     */
    @Test
    public void invalidFilenameUploadFailure() {
        uploadUtils.uploadSubscriptions(adminToolPage, INVALID_FILE_NAME);
        final String actualErrorMsg = uploadSubscriptionsPage.getH3ErrorMessage();
        final String expectedErrorMsg = "Please upload a valid XLSX file that is not password-protected.";
        AssertCollector.assertThat("Incorrect error message", actualErrorMsg, equalTo(expectedErrorMsg),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that with no file provided for upload results in failure.
     */
    @Test
    public void verifyUploadWithNoFileFailure() {
        uploadUtils.clickUploadButtonWithoutAnyFile(adminToolPage);
        final String actualErrorMsg = uploadSubscriptionsPage.getH3ErrorMessage();
        final String expectedErrorMsg = "Required";
        AssertCollector.assertThat("Incorrect error message", actualErrorMsg, equalTo(expectedErrorMsg),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test case tests the upload of subscription and validates Audit log for CREATE
     */
    @Test
    public void testSubscriptionCreateThroughUpload() throws IOException {

        // get the file path which will be uploaded
        final String xlsFile = uploadUtils.getFilePath(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();

        String subscriptionId = RandomStringUtils.randomNumeric(10);
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SUB_ID_COLUMN), subscriptionId);
        columnValuesMap.put(new XlsCell(1, PelicanConstants.QUANTITY_COLUMN), QUANTITY);
        columnValuesMap.put(new XlsCell(1, PelicanConstants.OXYGEN_ID_COLUMN), OXYGEN_ID);
        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // Upload the XLSx file
        uploadUtils = adminToolPage.getPage(UploadUtils.class);
        UploadSubscriptionsStatusPage uploadSubscriptionsStatusPage =
            uploadUtils.uploadSubscriptions(adminToolPage, PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);

        // Validate admin tool's page title
        AssertCollector.assertThat("Title is incorrect", getDriver().getTitle(),
            equalTo("Pelican - View Upload Status"), assertionErrorList);

        // Validate the fileName in the upload status page
        AssertCollector.assertThat("File name doesn't match", uploadSubscriptionsStatusPage.getFileName(),
            equalTo(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME), assertionErrorList);

        // refresh the page to get the latest result
        getDriver().navigate().refresh();

        // Get the status of the upload
        uploadSubscriptionsStatusPage = adminToolPage.getPage(UploadSubscriptionsStatusPage.class);
        final String uploadStatus = uploadSubscriptionsStatusPage.getUploadStatus();

        if (uploadStatus.equals(Status.COMPLETED.toString())) {

            // Get to the Subscription page.
            final FindSubscriptionsPage findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);

            final String externalKey = "MIG-" + subscriptionId;
            // Find the subscription by external key

            findSubscriptionPage.getSubscriptionByExternalKey(externalKey);
            final SubscriptionDetailPage subscriptionDetailPage =
                findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();
            subscriptionId = subscriptionDetailPage.getId();

            // Verify the audit log
            final boolean createSubscriptionAuditLogFound =
                SubscriptionAuditLogHelper.helperToValidateDynamoDbForCreateSubscriptions(subscriptionId,
                    Status.PENDING_MIGRATION, externalKey, priceId, planId, ownerId, userId,
                    PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME, getEnvironmentVariables(), assertionErrorList);

            AssertCollector.assertTrue(
                "Create Subscription Audit Log not found for Subscription id : " + subscriptionId,
                createSubscriptionAuditLogFound, assertionErrorList);
        } else {
            Assert.fail("Upload failed");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to create and write xls with Subscriptions details
     */
    private void createXlsxAndWriteData(final String FILE_NAME) throws IOException {
        final XlsUtils utils = new XlsUtils();

        // Add subscriptions header values to list
        final ArrayList<String> columnHeaders = new ArrayList<>();
        final ArrayList<String> columnData = new ArrayList<>();
        final String extKey = "TestSubsKey_" + RandomStringUtils.randomAlphanumeric(5);

        columnHeaders.add("#Subscription, externalKey, subscriptionplan, subscriptionoffer, user, quantity, status, "
            + "autorenewenabled, dayscredited, nextbillingdate, nextbillingcharge, nextbillingpriceId, expirationdate");

        for (int i = 1; i <= 10; i++) {
            columnData.add("Subscription" + i + "," + extKey + ",SubscriptionPlan" + i + ",offer" + i + ",user" + i
                + ",1, ACTIVE, TRUE," + i + "04/04/2016 17:57:57 UTC, 330.00 USD,"
                + "04/04/2017 17:57:57 UTC, 330.00 USD, 04/04/2018 17:57:57 UTC");
        }

        // Write Subscriptions headers data to excel.
        utils.createAndWriteToXls(FILE_NAME, columnHeaders, columnData, false);
    }
}
