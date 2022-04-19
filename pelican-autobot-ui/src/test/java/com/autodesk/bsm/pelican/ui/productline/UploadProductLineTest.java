package com.autodesk.bsm.pelican.ui.productline;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.FindProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineSearchResultPage;
import com.autodesk.bsm.pelican.ui.pages.productline.UploadProductLineStatusPage;
import com.autodesk.bsm.pelican.ui.pages.productline.UploadProductLinesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

public class UploadProductLineTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private static final String FILE_NAME = "UploadProductLine.xlsx";
    private static UploadProductLinesPage uploadProductLinesPage;
    private static UploadProductLineStatusPage uploadProductLineStatusPage;
    private static FindProductLinePage findProductLinePage;
    private static ProductLineSearchResultPage productLineSearchResultPage;
    private static ProductLineDetailsPage productLineDetailsPage;
    private static final String PRODUCT_LINE = "ProductLine";

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        // Instantiate admin tool
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        uploadProductLinesPage = adminToolPage.getPage(UploadProductLinesPage.class);
        findProductLinePage = adminToolPage.getPage(FindProductLinePage.class);
        productLineSearchResultPage = adminToolPage.getPage(ProductLineSearchResultPage.class);
    }

    /**
     * Add a product line through upload with valid name and external key and Default Active Status.
     * <p>
     * Result: Product line should be added
     */
    @Test
    public void testAddProductLineWithValidNameAndExternalKeyThroughUpload() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        uploadProductLineStatusPage =
            HelperForProductLine.createDataAndUploadFile(PRODUCT_LINE, name, externalKey, null, uploadProductLinesPage);
        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_COMPLETED.toUpperCase(),
            PelicanConstants.EMPTY_STRING, uploadProductLineStatusPage, assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidExternalKey(externalKey);
        AssertCollector.assertThat("Title of the page is not Product Line Detail", productLineDetailsPage.getTitle(),
            equalTo(PelicanConstants.PRODUCT_LINE_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("ID of the added product line is not found", productLineDetailsPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Name of the added product line is not correct", productLineDetailsPage.getName(),
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("External key of the added product line is not correct",
            productLineDetailsPage.getExternalKey(), equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product Line Active status", productLineDetailsPage.getActiveStatus(),
            equalTo(PelicanConstants.YES), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add a product line through upload with valid name and external key and Active Status NO
     * <p>
     * Result: Product line should be added with In-Active Status
     */
    @Test
    public void testAddProductLineWithInActiveStatusThroughUpload() {
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        uploadProductLineStatusPage = HelperForProductLine.createDataAndUploadFile(PRODUCT_LINE, name, externalKey,
            PelicanConstants.NO, uploadProductLinesPage);
        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_COMPLETED.toUpperCase(),
            PelicanConstants.EMPTY_STRING, uploadProductLineStatusPage, assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidExternalKey(externalKey);
        AssertCollector.assertThat("Title of the page is not Product Line Detail", productLineDetailsPage.getTitle(),
            equalTo(PelicanConstants.PRODUCT_LINE_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("ID of the added product line is not found", productLineDetailsPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Name of the added product line is not correct", productLineDetailsPage.getName(),
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("External key of the added product line is not correct",
            productLineDetailsPage.getExternalKey(), equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product Line Active status", productLineDetailsPage.getActiveStatus(),
            equalTo(PelicanConstants.NO), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add a product line through upload with empty name and external key Result: Product line should not be added
     *
     * @param productLine
     * @param name
     * @param externalKey
     */
    @Test(dataProvider = "dataForProductLine")
    public void testAddProductLineThroughUploadWithMissingMandatoryFields(final String productLine, final String name,
        final String externalKey) {
        uploadProductLineStatusPage =
            HelperForProductLine.createDataAndUploadFile(productLine, name, externalKey, null, uploadProductLinesPage);
        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_FAILED.toUpperCase(),
            PelicanErrorConstants.VIEW_ERRORS, uploadProductLineStatusPage, assertionErrorList);

        findProductLinePage.findByValidExternalKey(externalKey);
        AssertCollector.assertThat("Record of product line found in data base",
            productLineSearchResultPage.getTotalItems(), equalTo(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add a product line through upload with a name which already exists in existing product line
     * <p>
     * Result: Product line should be added with same name
     */
    @Test
    public void testAddProductLineWithExistingName() {
        // create two product Lines through upload with same name but different external keys
        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        final String externalKey1 = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        final String externalKey2 = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6);
        HelperForProductLine.createDataAndUploadFile(PRODUCT_LINE, name, externalKey1, null, uploadProductLinesPage);
        uploadProductLineStatusPage = HelperForProductLine.createDataAndUploadFile(PRODUCT_LINE, name, externalKey2,
            null, uploadProductLinesPage);
        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_COMPLETED.toUpperCase(),
            PelicanConstants.EMPTY_STRING, uploadProductLineStatusPage, assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidExternalKey(externalKey1);
        AssertCollector.assertThat("Product line name doesn't match with specified name",
            productLineDetailsPage.getName(), equalTo(name), assertionErrorList);
        productLineDetailsPage = findProductLinePage.findByValidExternalKey(externalKey2);
        AssertCollector.assertThat("Product line name doesn't match with specified name",
            productLineDetailsPage.getName(), equalTo(name), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add a product line through upload with a external which already exists in any other existing product line
     * <p>
     * Result: Product line should not be added
     */
    @Test
    public void testAddProductLineWithExistingExternalKey() {
        // create two product Lines through upload with same name but different external keys
        final String name1 = RandomStringUtils.randomAlphabetic(6);
        final String name2 = RandomStringUtils.randomAlphabetic(6);
        final String externalKey = RandomStringUtils.randomAlphabetic(6);
        HelperForProductLine.createDataAndUploadFile(PRODUCT_LINE, name1, externalKey, null, uploadProductLinesPage);
        uploadProductLineStatusPage = HelperForProductLine.createDataAndUploadFile(PRODUCT_LINE, name2, externalKey,
            null, uploadProductLinesPage);
        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_COMPLETED.toUpperCase(),
            PelicanConstants.EMPTY_STRING, uploadProductLineStatusPage, assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidExternalKey(externalKey);
        AssertCollector.assertThat("Name is not correct in the found result", productLineDetailsPage.getName(),
            equalTo(name2), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Add a product line through upload with missing any header Result: Product line should not be added and status
     * should be failed
     *
     * @param productLineHeader
     * @param nameHeader
     * @param externalKeyHeader
     * @param active
     */
    @Test(dataProvider = "dataForProductLineHeaders")
    public void testAddProductLineThroughUploadWithMissingHeader(final String productLineHeader,
        final String nameHeader, final String externalKeyHeader, final String active) {
        final String externalKey = RandomStringUtils.randomAlphabetic(8);

        final XlsUtils utils = new XlsUtils();
        final ArrayList<String> columnHeadersList = new ArrayList<>();
        final ArrayList<String> recordsDataList = new ArrayList<>();
        columnHeadersList.add("#" + productLineHeader + "," + nameHeader + "," + externalKeyHeader + active);
        recordsDataList.add("productLine" + "," + externalKey + "," + externalKey + active);
        try {
            utils.createAndWriteToXls(FILE_NAME, columnHeadersList, recordsDataList, false);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        uploadProductLineStatusPage = uploadProductLinesPage.uploadXlxsFile(FILE_NAME);

        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_FAILED.toUpperCase(),
            PelicanErrorConstants.VIEW_ERRORS, uploadProductLineStatusPage, assertionErrorList);

        // need to verify product line is not added if any of the header is missing
        productLineSearchResultPage = findProductLinePage.findByNonExistingExternalKey(externalKey);
        AssertCollector.assertTrue("Product line found in search result",
            productLineSearchResultPage.isNoneFoundPresent(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Edit a product line through file upload to set Active status Yes Use Product Line from
     * testDeclineConfirmationPopUpForProductLine method and change its status to Active from In-Active.
     * <p>
     * Result: Product line is activated
     */
    @Test
    public void testEditProductLineThroughFileUpload() {

        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        final AddProductLinePage addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);

        addProductLinePage.addProductLine(name, externalKey, null);
        productLineDetailsPage = addProductLinePage.clickOnSubmit();

        uploadProductLineStatusPage = HelperForProductLine.createDataAndUploadFile(
            PelicanConstants.DOWNLOAD_PRODUCT_LINE, name, externalKey, PelicanConstants.YES, uploadProductLinesPage);
        HelperForProductLine.commonAssertionsAfterUploadingAFile(PelicanConstants.JOB_STATUS_COMPLETED.toUpperCase(),
            PelicanConstants.EMPTY_STRING, uploadProductLineStatusPage, assertionErrorList);

        productLineDetailsPage = findProductLinePage.findByValidExternalKey(externalKey);
        AssertCollector.assertThat("Title of the page is not Product Line Detail", productLineDetailsPage.getTitle(),
            equalTo(PelicanConstants.PRODUCT_LINE_DETAIL_TITLE), assertionErrorList);
        AssertCollector.assertThat("ID of the added product line is not found", productLineDetailsPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Name of the added product line is not correct", productLineDetailsPage.getName(),
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("External key of the added product line is not correct",
            productLineDetailsPage.getExternalKey(), equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect Product Line Active status", productLineDetailsPage.getActiveStatus(),
            equalTo(PelicanConstants.YES), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider to return data required to create product line
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "dataForProductLine")
    public static Object[][] getProductLineData() {
        return new Object[][] {
                { PRODUCT_LINE, PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6), "" },
                { "", PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6),
                        PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6) },
                { PRODUCT_LINE, "", "" } };
    }

    /**
     * This is a data provider to return headers of a file
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "dataForProductLineHeaders")
    public static Object[][] getProductLineDataHeader() {
        return new Object[][] {
                { PRODUCT_LINE, "", PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6),
                        PelicanConstants.ACTIVE_FIELD_NAME },
                { "", PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6),
                        PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphabetic(6),
                        PelicanConstants.ACTIVE_FIELD_NAME } };
    }
}
