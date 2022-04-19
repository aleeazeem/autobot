package com.autodesk.bsm.pelican.ui.downloads.features;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.clients.ItemClient.ItemParameter;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.features.DownloadFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureSearchResultsPage;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.XlsUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Admin Tool's Download Features tests. On Admin Tool's Main Tab navigate to Catalog -> Features ->Download Features
 * Validate if the features xlsx file has been downloaded successfully
 *
 * @author Vineel
 */

public class DownloadFeaturesTest extends SeleniumWebdriver {

    private ItemType itemType;
    private String appId;
    private static DownloadFeaturePage downloadFeaturePage;
    private static final String FEATUREHEADER = "#CreateFeature";
    private static final String FEATURENAMEHEADER = "featureName";
    private static final String FEATURETYPEHEADER = "featureType";
    private static final String FEATUREEXTERNALKEYHEADER = "externalKey";
    private static String ACTUAL_FILE_NAME;
    private static final String SQL_QUERY = "select count(*) from item where app_id = 2001 and IS_ACTIVE=1";
    private static final String FIELD_NAME = "count(*)";
    private String itemName;
    private String itemExternalKey;
    private FindFeaturePage findFeaturePage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);

        final Applications applications = resource.application().getApplications();
        if (applications.getApplications().size() > 0) {
            appId = applications.getApplications().get(applications.getApplications().size() - 1).getId();
        }
        final String FEATURETYPE_NAME = "FeatureType";
        final String itemTypeName = FEATURETYPE_NAME + RandomStringUtils.randomAlphanumeric(8);
        itemName = "Item" + RandomStringUtils.randomAlphanumeric(10);
        itemExternalKey = "Item_Key" + RandomStringUtils.randomAlphanumeric(9);
        itemType = resource.itemType().addItemType(appId, itemTypeName);

        final HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(ItemParameter.NAME.getName(), itemName);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), itemExternalKey);
        resource.item().addItem(paramMap);

        downloadFeaturePage = adminToolPage.getPage(DownloadFeaturePage.class);
        if (getEnvironmentVariables().getAppFamily().equals("AUTO")) {
            ACTUAL_FILE_NAME = "catalog_AUTOAPP1.xlsx";
        } else if (getEnvironmentVariables().getAppFamily().equals("Demo")) {
            ACTUAL_FILE_NAME = "catalog_DemoAPP.xlsx";
        } else if (getEnvironmentVariables().getAppFamily().equals("AUOTDESK")) {
            ACTUAL_FILE_NAME = "catalog_AUTODESK.xlsx";
        }

    }

    /**
     * Method to verify the download features page title change
     */
    @Test
    public void testDownloadFeaturesHeaderChange() {

        // Navigate to the download features page and retrieve the page title
        downloadFeaturePage.navigateToDownloadFeaturesPage();
        final String pageTitle = downloadFeaturePage.getPageTitle();
        // Validate the download features page title
        AssertCollector.assertThat("Incorrect title for the download features page", pageTitle,
            equalTo("Download Features"), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify the fields present on the download features page
     */
    @Test
    public void testFieldsOnDownloadFeaturesPage() {

        // Navigate to the download features page
        downloadFeaturePage.navigateToDownloadFeaturesPage();
        final boolean isApplicationFamilyPresent = downloadFeaturePage.isApplicationFamilyFieldPresent();
        final boolean isApplicationPresent = downloadFeaturePage.isApplicationFieldPresent();
        final boolean isFeatureTypePresent = downloadFeaturePage.isFeatureTypeFieldPresent();
        // validate the fields present on the download features page
        AssertCollector.assertThat("Application Family field is missing on the download features page",
            isApplicationFamilyPresent, equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Application field is missing on the download features page", isApplicationPresent,
            equalTo(true), assertionErrorList);
        AssertCollector.assertThat("Feature Type field is missing on the download features page", isFeatureTypePresent,
            equalTo(true), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify the column headers present in the download features file
     */
    @Test
    public void testColumnHeadersInDownloadFeaturesFile() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download features page and download the features excel file
        downloadFeaturePage.downloadFeaturesXlsxFile(itemType.getName(), PelicanConstants.YES);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length, equalTo(5),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 1", fileData[0][0], equalTo(FEATUREHEADER), assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 2", fileData[0][1], equalTo(FEATURENAMEHEADER),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 3", fileData[0][2], equalTo(FEATURETYPEHEADER),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Header 4", fileData[0][3], equalTo(FEATUREEXTERNALKEYHEADER),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect header 5", fileData[0][4], equalTo(PelicanConstants.ACTIVE_FIELD_NAME),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify the column data present in the download features file for default feature type
     */
    @Test
    public void testColumnDataInDownloadFeaturesFileForDefaultFeatureType() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download features page and download the features excel file
        downloadFeaturePage.downloadFeaturesXlsxFile("ANY (*)", PelicanConstants.YES);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final List<String> countOfFeaturesList = DbUtils.selectQuery(SQL_QUERY, FIELD_NAME, getEnvironmentVariables());
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length, equalTo(5),
            assertionErrorList);
        AssertCollector.assertThat("The created feature Name is not found in the download file",
            fileData[fileData.length - 1][1], equalTo(itemName), assertionErrorList);
        AssertCollector.assertThat("The created feature type is not found in the download file",
            fileData[fileData.length - 1][2], equalTo(itemType.getName()), assertionErrorList);
        AssertCollector.assertThat("The created feature external key is not found in the download file",
            fileData[fileData.length - 1][3], equalTo(itemExternalKey), assertionErrorList);
        AssertCollector.assertThat("The created feature status is not found in the download file",
            fileData[fileData.length - 1][4], equalTo(PelicanConstants.YES), assertionErrorList);
        AssertCollector.assertThat("Total Features are not downloaded to the xlsx file", fileData.length - 1,
            lessThanOrEqualTo(Integer.parseInt(countOfFeaturesList.get(0))), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify the column data present in the download features file for specific feature type
     */
    @Test
    public void testColumnDataInDownloadFeaturesFileForSpecificFeatureType() throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download features page and download the features excel file
        downloadFeaturePage.downloadFeaturesXlsxFile(itemType.getName(), PelicanConstants.YES);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        final int totalNumberOfFeatures = fileData.length - 1;
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length, equalTo(5),
            assertionErrorList);
        AssertCollector.assertThat("The created feature Name is not found in the download file",
            fileData[fileData.length - 1][1], equalTo(itemName), assertionErrorList);
        AssertCollector.assertThat("The created feature type is not found in the download file",
            fileData[fileData.length - 1][2], equalTo(itemType.getName()), assertionErrorList);
        AssertCollector.assertThat("The created feature external key is not found in the download file",
            fileData[fileData.length - 1][3], equalTo(itemExternalKey), assertionErrorList);
        AssertCollector.assertThat("The created feature status is not found in the download file",
            fileData[fileData.length - 1][4], equalTo(PelicanConstants.YES), assertionErrorList);
        AssertCollector.assertThat("Total Features are not downloaded to the xlsx file", totalNumberOfFeatures,
            equalTo(1), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the feature status in download features.
     *
     */
    @Test
    public void testFeatureStatusFieldInDownloadFeatures() {

        FeatureSearchResultsPage featureSearchResultsPage = findFeaturePage.findFeatureByAdvancedFind(true);
        AssertCollector.assertThat("Incorrect value for the active field in the feature search results page",
            featureSearchResultsPage.getColumnValuesOfActiveStatus(),
            everyItem(isOneOf(PelicanConstants.YES, PelicanConstants.NO)), assertionErrorList);

        featureSearchResultsPage = findFeaturePage.findFeatureByAdvancedFind(false);
        AssertCollector.assertThat("Incorrect value for the active field in the feature search results page",
            featureSearchResultsPage.getColumnValuesOfActiveStatus(), everyItem(equalTo(PelicanConstants.YES)),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test whether the status field is present on the download features page.
     *
     */
    @Test
    public void testIsActiveFieldPresentOnDownloadFeaturesPage() {

        // Navigate to the download features page
        downloadFeaturePage.navigateToDownloadFeaturesPage();
        final boolean isPresent = downloadFeaturePage.isActiveFieldPresent();
        AssertCollector.assertTrue("Is Active field is not present on the download features page", isPresent,
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test case which will test the status field functionality on the download features page.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test(dataProvider = "status")
    public void testDownloadFeaturesWithIsActiveStatus(final String isActive) throws IOException {

        XlsUtils.cleanDownloadedFile(getEnvironmentVariables(), ACTUAL_FILE_NAME);
        // Navigate to the download features page and download the features excel file
        downloadFeaturePage.downloadFeaturesXlsxFile(null, isActive);
        final String fileName = XlsUtils.getDirPath(getEnvironmentVariables()) + ACTUAL_FILE_NAME;
        final String[][] fileData = XlsUtils.readDataFromXlsx(fileName);
        AssertCollector.assertThat("Total number of columns are not correct", fileData[0].length, equalTo(5),
            assertionErrorList);
        final List<String> statusList = new ArrayList<>();

        if (PelicanConstants.YES.equalsIgnoreCase(isActive)) {
            for (int i = 1; i <= fileData.length - 1; i++) {
                AssertCollector.assertThat("Incorrect status of the features in the download", fileData[i][4],
                    equalTo(PelicanConstants.YES), assertionErrorList);
            }
        } else if (PelicanConstants.NO.equalsIgnoreCase(isActive)) {
            for (int i = 1; i <= fileData.length - 1; i++) {
                AssertCollector.assertThat("Incorrect status of the features in the download", fileData[i][4],
                    equalTo(PelicanConstants.NO), assertionErrorList);
            }
        } else if (PelicanConstants.ANY.equalsIgnoreCase(isActive)) {
            for (int i = 1; i <= fileData.length - 1; i++) {
                statusList.add(fileData[i][4]);
            }
        }

        AssertCollector.assertThat("Incorrect status of the features in the download", statusList,
            everyItem(isOneOf(PelicanConstants.YES, PelicanConstants.NO)), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a data provider which will return the various statuses for active field.
     *
     * @return a two dimensional array object.
     */
    @DataProvider(name = "status")
    public Object[][] getIsActiveStatuses() {
        return new Object[][] {

                { PelicanConstants.YES }, { PelicanConstants.NO }, { PelicanConstants.ANY } };
    }
}
