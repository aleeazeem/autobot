package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.DRSubscription;
import com.autodesk.bsm.pelican.api.pojos.DRWIPData;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEvents;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Store;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionEventsData;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionEventType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UploadUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsCell;
import com.autodesk.bsm.pelican.util.XlsUtils;

import com.google.common.collect.Iterables;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class verifies DR Migration.
 * <p>
 * Please refer to https://wiki.autodesk.com/pages/viewpage.action?spaceKey=~t_godwm&title=DR+
 * Subscription+Migration+Excel+Upload+Workflow to understand DR Migration implementation
 *
 * @author t_mohag
 */
@Test(groups = { "excludedClass" })
public class DRMigrationTest extends SeleniumWebdriver {

    private static final String INVALID_FILE_NAME = "UploadSubscriptions_Invalid_Locale_SKU.xlsx";
    private static final String FILE_NAME_WITH_MISSING_DATA = "UploadSubscriptionsMissingData.xlsx";
    private static final String PRODUCT_LINE_EXT_KEY = "ACD";
    private static final Integer DR_SUBSCRIPTION_STATUS = 5;
    private PelicanPlatform resource;
    private AdminToolPage adminToolPage;
    private UploadUtils uploadUtils;
    private XlsUtils xlsUtils;
    private String subscriptionIdInFile;
    private static final String QUANTITY = "1";
    private static final Logger LOGGER = LoggerFactory.getLogger(DRMigrationTest.class.getSimpleName());
    private static final String SUBSCRIPTION_FILE_PROCESSING_CATEGORY = "17";

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        xlsUtils = new XlsUtils();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        uploadUtils = adminToolPage.getPage(UploadUtils.class);

        // Add product line if it doesn't exists
        final ProductLine productLine = DbUtils.getProductLineData(PRODUCT_LINE_EXT_KEY, getEnvironmentVariables());

        if (productLine == null) {
            subscriptionPlanApiUtils.addProductLine(PRODUCT_LINE_EXT_KEY);
        }

        // Add store if it doesn't exists
        final JStore jStore = resource.stores().getStoreByExternalKey("STORE-IT");
        if (jStore != null && jStore.getErrors() != null && HttpStatus.SC_NOT_FOUND == jStore.getErrors().getStatus()) {
            final Store store = storeApiUtils.addStoreWithExternalKey(storeApiUtils.getRandomStoreTypeExtKey(),
                "STORE-IT", Status.ACTIVE);
            final String storeId = store.getData().getId();
            storeApiUtils.addPriceListWithCountry(storeId, Currency.EUR, Country.FR);
        }

    }

    /**
     * Method to verify the following data in mig_subscription table is the same as the uploaded file 1. SubscriptionId
     * 2. ProductName 3. Locale 4. Currency 5. ContractTerm 6. Quantity 7. Sku code 8. Application Family Id
     */
    @Test
    public void verifyDataInMigSubscriptionTable() throws IOException {
        // Upload a file and validate the status page
        final String xlsFile = uploadUtils.getFilePath(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();
        subscriptionIdInFile = String.valueOf(RandomUtils.nextInt(99999999));
        final String subExternalKey = "MIG-" + subscriptionIdInFile;
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SUB_ID_COLUMN), subscriptionIdInFile);
        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        LOGGER.info("Subscription external key" + subExternalKey);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils.uploadSubscriptions(adminToolPage, PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);

        // Validate the DB data with that of the uploaded file
        final List<DRSubscription> inputSubs = new ArrayList<>();
        final List<String> inputSubIds = new ArrayList<>();
        inputSubIds.add(subscriptionIdInFile);
        readXlsFileAndPopulateInputSubs(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME, inputSubs, inputSubIds, 2);

        final List<DRSubscription> subsFromDB =
            DbUtils.getDataFromMigSubscriptionTable(inputSubIds, getEnvironmentVariables());
        final List<Integer> subscriptionIdsFromDB = new ArrayList<>();
        final Map<Integer, DRSubscription> subsFromDBMap = new HashMap<>();

        for (final DRSubscription subFromDB : subsFromDB) {
            subscriptionIdsFromDB.add(subFromDB.getSubscriptionId());
            subsFromDBMap.put(subFromDB.getSubscriptionId(), subFromDB);
        }

        AssertCollector.assertThat("DB does not have the subscription from xlsx", subscriptionIdsFromDB,
            hasItem(inputSubs.get(0).getSubscriptionId()), assertionErrorList);
        AssertCollector.assertThat("DB subscription's product name does not match xlsx product name",
            inputSubs.get(0).getProductName(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getProductName()), assertionErrorList);
        AssertCollector.assertThat("DB subscription's locale does not match xlsx locale", inputSubs.get(0).getLocale(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getLocale()), assertionErrorList);
        AssertCollector.assertThat("DB subscription's contract term does not match xlsx contract term",
            inputSubs.get(0).getContractTerm(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getContractTerm()), assertionErrorList);
        AssertCollector.assertThat("DB subscription's currency does not match xlsx currency",
            inputSubs.get(0).getCurrency(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getCurrency()), assertionErrorList);
        AssertCollector.assertThat("DB subscription's quantity does not match xlsx quantity",
            inputSubs.get(0).getQuantity(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getQuantity()), assertionErrorList);
        AssertCollector.assertThat("DB subscription's sku does not match xlsx sku", inputSubs.get(0).getSku(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getSku()), assertionErrorList);
        AssertCollector.assertThat("Incorrect application family returned", inputSubs.get(0).getApplicationFamilyId(),
            equalTo(subsFromDBMap.get(inputSubs.get(0).getSubscriptionId()).getApplicationFamilyId()),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that an upload job status is set to failure ONLY when the processing of all records
     * corresponding to the upload job fail
     */
    @Test
    public void verifyJobStatusFailure() {
        // Upload a file and validate the status page
        uploadUtils.uploadSubscriptions(adminToolPage, INVALID_FILE_NAME);

        final List<Integer> statusInWIP =
            DbUtils.getLatestJobRecordsStatusFromWip(getEnvironmentVariables(), SUBSCRIPTION_FILE_PROCESSING_CATEGORY);
        for (final Integer status : statusInWIP) {
            AssertCollector.assertThat("Record status other than 4(Failed) found: ", status, equalTo(4),
                assertionErrorList);
        }

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final int jobStatus = DbUtils.getDRMigrationJobStatusFromJobStatusesTable(getEnvironmentVariables(),
            SUBSCRIPTION_FILE_PROCESSING_CATEGORY);
        AssertCollector.assertThat("Incorrect job status returned: ", jobStatus, equalTo(2), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify that appropriate error messages are displayed in WIP table under notes column based on data
     * validation
     */
    @Test
    public void verifyErrorMessagesInWip() throws IOException {
        // Upload a file and validate the status page
        final String xlsFile = uploadUtils.getFilePath(FILE_NAME_WITH_MISSING_DATA);
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();

        // Sku code not updated for row no 5 for sku code invalid scenario
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(2, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(3, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(4, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(6, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());

        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils.uploadSubscriptions(adminToolPage, FILE_NAME_WITH_MISSING_DATA);

        final List<DRSubscription> inputSubs = new ArrayList<>();
        final List<String> inputSubIds = new ArrayList<>();
        LOGGER.info("input subs" + inputSubIds.toString());
        readXlsFileAndPopulateInputSubs(FILE_NAME_WITH_MISSING_DATA, inputSubs, inputSubIds, 7);

        final List<DRSubscription> subsFromDB =
            DbUtils.getDataFromMigSubscriptionTable(inputSubIds, getEnvironmentVariables());
        LOGGER.info("subsFromDB size" + subsFromDB.size());
        for (final DRSubscription subFromDB : subsFromDB) {
            final DRWIPData wipSubData =
                DbUtils.getLatestJobDataFromWip(subFromDB.getSubscriptionId(), getEnvironmentVariables());
            if (wipSubData != null) {
                if (wipSubData.getObjectId() == 32762) {
                    LOGGER.info("Validating sku code");
                    AssertCollector.assertThat("Invalid SKU code detected", wipSubData.getNotes(),
                        equalTo("SKU code is not valid;"), assertionErrorList);
                } else if (wipSubData.getObjectId() == 32981) {
                    LOGGER.info("Validating locale");
                    AssertCollector.assertThat("Invalid Locale detected", wipSubData.getNotes(),
                        equalTo("Locale id is Not Valid;"), assertionErrorList);
                } else if (wipSubData.getObjectId() == 32967) {
                    LOGGER.info("Validating product name");
                    AssertCollector.assertThat("Invalid Product name detected", wipSubData.getNotes(),
                        equalTo("Product Name is not present;"), assertionErrorList);
                } else if (wipSubData.getObjectId() == 32770) {
                    LOGGER.info("Validating contract term");
                    AssertCollector.assertThat("Invalid Contract term detected", wipSubData.getNotes(),
                        equalTo("Contract Term not present;"), assertionErrorList);
                } else if (wipSubData.getObjectId() == 32974) {
                    LOGGER.info("Validating quantity");
                    AssertCollector.assertThat("Invalid Quantity detected", wipSubData.getNotes(),
                        equalTo("Quantity is not present;"), assertionErrorList);
                }
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test the complete end to end flow that is 1. upload 2. verify the subscription is created in Pelican
     */
    @Test
    public void verifyDRSubscriptionCreation() throws IOException {

        // Upload a file and validate the status page
        final String xlsFile = uploadUtils.getFilePath(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();
        subscriptionIdInFile = String.valueOf(RandomUtils.nextInt(99999999));
        final String subExternalKey = "MIG-" + subscriptionIdInFile;
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SKU_CODE_COLUMN),
            getEnvironmentVariables().getSkuCodeForMonthlyOffer());
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SUB_ID_COLUMN), subscriptionIdInFile);
        columnValuesMap.put(new XlsCell(1, PelicanConstants.QUANTITY_COLUMN), QUANTITY);

        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        LOGGER.info("Subscription external key" + subExternalKey);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        uploadUtils.uploadSubscriptions(adminToolPage, PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);

        Util.waitInSeconds(TimeConstants.THREE_SEC);

        // Get the PENDING_MIGRATION subscription details
        final Subscription createdSubscription = DbUtils.getSubscriptionData(subExternalKey, getEnvironmentVariables());
        AssertCollector.assertThat("Subscription id is null", createdSubscription.getId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription external key", createdSubscription.getExternalKey(),
            equalTo(subExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription quantity", createdSubscription.getQuantity(), equalTo(1),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect subscription status", createdSubscription.getStatus(),
            equalTo(DR_SUBSCRIPTION_STATUS.toString()), assertionErrorList);

        // verify subscription event api for MIGRATION_START
        final JSubscriptionEvents subscriptionEvents =
            resource.getSubscriptionEventsClient().getSubscriptionEvents(createdSubscription.getId(), null);
        final SubscriptionEventsData subscriptionEventsData = Iterables.getLast(subscriptionEvents.getEventsData());
        AssertCollector.assertThat(
            "Activity is not correct for get subscription api for subscription id " + createdSubscription.getId(),
            subscriptionEventsData.getEventType(), equalTo(SubscriptionEventType.MIGRATION_START.toString()),
            assertionErrorList);
        AssertCollector.assertThat(
            "Requestor name is not correct for get subscription api for subscription id " + createdSubscription.getId(),
            subscriptionEventsData.getRequesterName(), equalTo(null), assertionErrorList);
        AssertCollector.assertThat(
            "Purchase order is not correct for get subscription api for subscription id " + createdSubscription.getId(),
            subscriptionEventsData.getPurchaseOrderId(), equalTo(null), assertionErrorList);
        AssertCollector.assertThat(
            "Memo is not correct for for get subscription api subscription id " + createdSubscription.getId(),
            subscriptionEventsData.getMemo(), equalTo(null), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Validate if user doen't exist in pelican but it exists in DR than new user will be created in Pelican with oxygen
     * id which exists in DR.
     *
     * @Result: New user will be added in pelican using oxygen id which exists in DR
     */
    @Test
    public void verifyAddUserByUploadingFileWithoutOxygenId() throws IOException {
        final String xlsFile = uploadUtils.getFilePath(PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);
        final Map<XlsCell, String> columnValuesMap = new HashMap<>();

        // Generate random subscriptionId and oxygen id
        subscriptionIdInFile = String.valueOf(RandomUtils.nextInt(99999999));
        final String oxygenId = RandomStringUtils.randomAlphanumeric(10);
        LOGGER.info("Subscription Id in DR: " + oxygenId);

        // replace the values of column subscription id and oxygen id in DR xlsx
        // file
        columnValuesMap.put(new XlsCell(1, PelicanConstants.SUB_ID_COLUMN), subscriptionIdInFile);
        columnValuesMap.put(new XlsCell(1, PelicanConstants.OXYGEN_ID_COLUMN), oxygenId);
        xlsUtils.updateColumnsInXlsx(xlsFile, columnValuesMap);
        uploadUtils.uploadSubscriptions(adminToolPage, PelicanConstants.DR_SUBS_VALID_DATA_FILE_NAME);

        // navigate to user Detail Page using oxygen id
        final FindUserPage findUserPage = adminToolPage.getPage(FindUserPage.class);
        final UserDetailsPage userDetails = findUserPage.getByExternalKey(oxygenId);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        AssertCollector.assertThat("Incorrect Title Page", getDriver().getTitle(), equalTo("Pelican User Detail"),
            assertionErrorList);
        AssertCollector.assertThat("User is not created by Uploading a subscription", userDetails.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat(
            "User is not created with external key which exists in uploaded file to create " + "subscription",
            userDetails.getExternalKey(), equalTo(oxygenId), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Read the xlsFile and populate inputSubs and inputSubIds lists using it's data
     */
    private void readXlsFileAndPopulateInputSubs(final String fileName, final List<DRSubscription> inputSubs,
        final List<String> inputSubIds, final int numberOfRows) throws IOException {
        final String xlsFile = uploadUtils.getFilePath(fileName);
        final String[][] fileData = XlsUtils.readDataFromXlsx(xlsFile);

        // Validate the DB data with that of the uploaded file
        for (int i = 1; i < numberOfRows; i++) {
            final DRSubscription inputSub = new DRSubscription();
            inputSub.setProductName(fileData[i][21]);
            inputSub.setLocale(fileData[i][1]);
            inputSub.setCurrency(Currency.valueOf(fileData[i][4]));
            inputSub.setContractTerm(fileData[i][6]);
            inputSub.setQuantity(!fileData[i][31].isEmpty() ? Double.parseDouble(fileData[i][31]) : 0.0);
            inputSub.setSku(fileData[i][22]);
            final Integer subscriptionId =
                (!fileData[i][11].isEmpty() ? Double.valueOf(fileData[i][11]).intValue() : 0);
            inputSub.setSubscriptionId(subscriptionId);
            inputSubs.add(inputSub);
            inputSubIds.add(subscriptionId.toString());
        }
    }
}
