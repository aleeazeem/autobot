package com.autodesk.bsm.pelican.ui.productline;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.pages.productline.ProductLineDetailsPage;
import com.autodesk.bsm.pelican.ui.pages.productline.UploadProductLineStatusPage;
import com.autodesk.bsm.pelican.ui.pages.productline.UploadProductLinesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class, which handles common assertions for Product Line Module
 *
 * @author Shweta Hegde
 *
 */
public class HelperForProductLine {
    private static final String HEADERS_OF_XLSX_FILE = "#ProductLine,name,externalKey,active";
    private static final String FILE_NAME = "UploadProductLine.xlsx";

    /**
     * This method does common assertions on name and externalkey
     *
     * @param productLineDetailsPage
     * @param name
     * @param externalKey
     * @param activeStatus
     * @param assertionErrorList
     */
    public static void assertNameAndExternalKey(final ProductLineDetailsPage productLineDetailsPage, final String name,
        final String externalKey, final String activeStatus, final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect product line name", productLineDetailsPage.getName(), equalTo(name),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect product line external key", productLineDetailsPage.getExternalKey(),
            equalTo(externalKey), assertionErrorList);
        AssertCollector.assertThat("Product Line id should not be empty", productLineDetailsPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect product line active status", productLineDetailsPage.getActiveStatus(),
            equalTo(activeStatus), assertionErrorList);
    }

    /**
     * method to create a file and upload to create product line
     *
     * @param productLine
     * @param name
     * @param externalKey
     * @param active
     * @param uploadProductLinesPage
     * @return UploadProductLineStatusPage
     */
    public static UploadProductLineStatusPage createDataAndUploadFile(final String productLine, final String name,
        final String externalKey, final String active, final UploadProductLinesPage uploadProductLinesPage) {
        final XlsUtils utils = new XlsUtils();
        final ArrayList<String> columnHeadersList = new ArrayList<>();
        final ArrayList<String> recordsDataList = new ArrayList<>();
        columnHeadersList.add(HEADERS_OF_XLSX_FILE);
        recordsDataList.add(productLine + "," + name + "," + externalKey + "," + active);
        try {
            utils.createAndWriteToXls(FILE_NAME, columnHeadersList, recordsDataList, false);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return uploadProductLinesPage.uploadXlxsFile(FILE_NAME);
    }

    /**
     * method of assertions for validations after uploading a file
     *
     * @param status
     * @param error
     * @param uploadProductLineStatusPage
     * @param assertionErrorList
     */
    public static void commonAssertionsAfterUploadingAFile(final String status, final String error,
        final UploadProductLineStatusPage uploadProductLineStatusPage, final List<AssertionError> assertionErrorList) {

        for (int i = 0; i <= 3; i++) {

            Util.waitInSeconds(TimeConstants.TWO_SEC);
            uploadProductLineStatusPage.refreshPage();

            if (uploadProductLineStatusPage.getColumnValuesOfStatus().get(0).equals(status)) {
                AssertCollector.assertThat("Title of the page is not Upload Status",
                    uploadProductLineStatusPage.getTitle(), equalTo(PelicanConstants.UPLOAD_STATUS_TITLE),
                    assertionErrorList);
                AssertCollector.assertThat("Error is generated for success upload",
                    uploadProductLineStatusPage.getColumnValuesOfErrors().get(0), equalTo(error), assertionErrorList);
                break;
            }

            if (i == 3) {
                AssertCollector.assertThat("Status is not correct for success upload",
                    uploadProductLineStatusPage.getColumnValuesOfStatus().get(0), equalTo(status), assertionErrorList);
            }
        }
    }
}
