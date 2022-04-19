package com.autodesk.bsm.pelican.ui.downloads.basicoffering;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.basicofferings.DownloadBasicOfferingsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * This class tests download feature of Basic Offerings
 *
 * @author Shweta Hegde
 */
public class DownloadBasicOfferingsTest extends SeleniumWebdriver {

    private static final String ACTUAL_FILE_NAME = "basicOfferings.xlsx";
    private DownloadBasicOfferingsPage downloadBasicOfferingsPage;
    private static final int NUMBER_OF_COLUMNS = 14;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        downloadBasicOfferingsPage = adminToolPage.getPage(DownloadBasicOfferingsPage.class);
    }

    /**
     * This method tests Basic Offerings download with default filters.
     */
    @Test
    public void testDownloadBasicOfferingsFileForDefaultFilter() throws IOException {

        // Clean previously downloaded files
        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download BasicOfferings page and download the
        // BasicOfferings excel file
        downloadBasicOfferingsPage.downloadBasicOfferingsXlsxFile(null, null, true, true, false, false, false);

        // get the number of basic offerings which are in default "new" and "active" status
        final int numberOfBasicOfferings = DbUtils.getTotalNumberOfBasicOfferings("0,1", getEnvironmentVariables());

        // Get the file name with file path
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        // Read from the file
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);

        AssertCollector.assertThat("Total number of columns are incorrect", fileData[0].length, is(NUMBER_OF_COLUMNS),
            assertionErrorList);
        AssertCollector.assertThat("The usage type of basic offering is not found in the download file", fileData[0][4],
            equalTo("usageType"), assertionErrorList);

        for (int i = 1; i < numberOfBasicOfferings; i++) {
            AssertCollector.assertThat("The usage type of basic offering is not found in the download file",
                fileData[i][4], isOneOf(UsageType.COM.getUploadName(), UsageType.EDU.getUploadName(),
                    UsageType.GOV.getUploadName(), UsageType.NCM.getUploadName(), UsageType.TRL.getUploadName()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
