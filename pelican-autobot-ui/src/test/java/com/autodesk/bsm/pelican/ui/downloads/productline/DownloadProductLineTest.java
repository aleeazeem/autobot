package com.autodesk.bsm.pelican.ui.downloads.productline;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.productline.AddProductLinePage;
import com.autodesk.bsm.pelican.ui.pages.productline.DownloadProductLinePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This test class is for Download Product Line Functionality
 *
 * @author Vineel and Muhammad
 */
public class DownloadProductLineTest extends SeleniumWebdriver {

    private DownloadProductLinePage downloadProductLinePage;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadProductLineTest.class.getSimpleName());
    private static final String FILE_NAME = "productLines.xlsx";
    private static String fileName;
    private static AddProductLinePage addProductLinePage;
    private static final String SQL_QUERY =
        "select count(*) from product_line where APP_FAMILY_ID = 2001 and IS_ACTIVE=1;";
    private static final String FIELD_NAME = "count(*)";

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        initializeDriver(getEnvironmentVariables());

        // Instantiate admin tool
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        downloadProductLinePage = adminToolPage.getPage(DownloadProductLinePage.class);
        addProductLinePage = adminToolPage.getPage(AddProductLinePage.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), FILE_NAME);
    }

    /**
     * method to test download product line
     * <p>
     * Result: file of product line is downloaded with correct number of records
     */
    @Test
    public void testDownloadProductLine() {
        downloadProductLinePage.downloadAfile(PelicanConstants.YES);
        AssertCollector
            .assertThat(
                "Title of the page is not correct", getDriver().getTitle(), equalTo(PelicanConstants.PELICAN + " "
                    + PelicanConstants.HIPHEN + " " + PelicanConstants.DOWNLOAD_PRODUCT_LINES_TITLE),
                assertionErrorList);
        final List<String> countOfRecordsList = DbUtils.selectQuery(SQL_QUERY, FIELD_NAME, getEnvironmentVariables());
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME;
        LOGGER.info("Name of Downloaded file is: " + fileName);
        String[][] fileData = null;
        try {
            fileData = XlsUtils.readDataFromXlsx(fileName);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final int totalRecordsInFile = fileData.length - 1;
        LOGGER.info("Total number of records in file is: " + totalRecordsInFile);
        AssertCollector.assertThat("Total records in downloaded file is not correct", totalRecordsInFile,
            lessThanOrEqualTo(Integer.parseInt(countOfRecordsList.get(0))), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * this method tests the headers of download product lines
     *
     * @throws IOException
     */
    @Test
    public void testHeadersInDownloadProductLine() throws IOException {
        downloadProductLinePage.downloadAfile(PelicanConstants.YES);
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME;
        LOGGER.info("Name of Downloaded file is: " + fileName);

        String[][] fileData;
        fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("First header in file is not #ProductLine", fileData[0][0],
            equalTo("#" + PelicanConstants.DOWNLOAD_PRODUCT_LINE), assertionErrorList);
        AssertCollector.assertThat("Second header in file is not name", fileData[0][1], equalTo(PelicanConstants.NAME),
            assertionErrorList);
        AssertCollector.assertThat("Third header in file is not external key", fileData[0][2],
            equalTo(PelicanConstants.EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * verify downloaded file contains correct column headers
     *
     * @throws IOException
     */
    @Test
    public void testDownloadProductLineColumnHeaders() throws IOException {
        downloadProductLinePage.downloadAfile(PelicanConstants.YES);
        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME;
        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are incorrect", fileData[0].length, is(4),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect header for 'name", fileData[0][1], equalTo(PelicanConstants.NAME),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect header for 'Entity Id'", fileData[0][2],
            equalTo(PelicanConstants.EXTERNAL_KEY), assertionErrorList);
        AssertCollector.assertThat("Incorrect header for 'Change Date'", fileData[0][3],
            equalTo(PelicanConstants.ACTIVE_STATUS), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Downloaded file contains correct Active Status value for Productline.
     */
    @Test(dataProvider = "dataForProductLine")
    public void testDownloadProductLineFileContainsCorrectActiveStatusForProductLine(final String activeStatus)
        throws IOException {

        final String name = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        final String externalKey = PelicanConstants.PRODUCT_LINE_PREFIX + RandomStringUtils.randomAlphanumeric(8);

        addProductLinePage.addProductLine(name, externalKey, activeStatus);

        addProductLinePage.clickOnSubmit();

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), FILE_NAME);
        downloadProductLinePage.downloadAfile(activeStatus);

        fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME;

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        int index = -1;
        final int rowSize = fileData.length;

        for (int i = 0; i < rowSize; i++) {
            if (fileData[i][1].contains(name)) {
                index = i;
                break;
            }
        }
        AssertCollector.assertThat("Incorrect Name for ProductLine Under Downloaded File", fileData[index][1],
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("Incorrect ExternalKey for ProductLine Under Downloaded File", fileData[index][2],
            equalTo(externalKey), assertionErrorList);

        for (int i = 1; i < rowSize; i++) {
            if (activeStatus.equals(PelicanConstants.ANY)) {
                AssertCollector.assertThat("Incorrect Active Status for ProductLine Under Downloaded File",
                    fileData[i][3], isOneOf(PelicanConstants.YES, PelicanConstants.NO), assertionErrorList);
            } else {
                AssertCollector.assertThat("Incorrect Active Status for ProductLine Under Downloaded File",
                    fileData[i][3], equalTo(activeStatus), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test whether the status field is present on the download product lines page.
     *
     */
    @Test
    public void testIsActiveFieldPresentOnDownloadProductLinesPage() {

        // Navigate to the download features page
        downloadProductLinePage.navigateToDownloadPage();
        final boolean isPresent = downloadProductLinePage.isActiveFieldPresent();
        AssertCollector.assertTrue("Is Active field is not present on the download product lines page", isPresent,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will test whether the status field functionality on the download product lines page.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test(dataProvider = "dataForProductLine")
    public void testDownloadProductLinesWithIsActiveStatus(final String isActive) throws IOException {

        LOGGER.info("Is Active: " + isActive);
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), FILE_NAME);
        // Navigate to the download product lines page and download the product lines excel file
        downloadProductLinePage.downloadAfile(isActive);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length, equalTo(4),
            assertionErrorList);
        final List<String> statusList = new ArrayList<>();

        if (PelicanConstants.YES.equalsIgnoreCase(isActive)) {
            for (int i = 1; i <= fileData.length - 1; i++) {
                AssertCollector.assertThat("Incorrect status of the product lines in the download", fileData[i][3],
                    equalTo(PelicanConstants.YES), assertionErrorList);
            }
        } else if (PelicanConstants.NO.equalsIgnoreCase(isActive)) {
            for (int i = 1; i <= fileData.length - 1; i++) {
                AssertCollector.assertThat("Incorrect status of the product lines in the download", fileData[i][3],
                    equalTo(PelicanConstants.NO), assertionErrorList);
            }
        } else if (PelicanConstants.ANY.equalsIgnoreCase(isActive)) {
            for (int i = 1; i <= fileData.length - 1; i++) {
                statusList.add(fileData[i][3]);
            }
        }

        AssertCollector.assertThat("Incorrect status of the product lines in the download", statusList,
            everyItem(isOneOf(PelicanConstants.YES, PelicanConstants.NO)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a data provider to Add Product Line with Active Status Yes and No
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "dataForProductLine")
    public static Object[][] getProductLineData() {
        return new Object[][] { { PelicanConstants.YES }, { PelicanConstants.NO }, { PelicanConstants.ANY } };
    }
}
