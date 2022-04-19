package com.autodesk.bsm.pelican.ui.features;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.catalog.AddFeatureTypePage;
import com.autodesk.bsm.pelican.ui.pages.features.AddFeaturePage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.ui.pages.features.FeatureSearchResultsPage;
import com.autodesk.bsm.pelican.ui.pages.features.FindFeaturePage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * This is a test class for finding the features in the admin tool
 *
 * @author vineel
 */
public class FindFeaturesTest extends SeleniumWebdriver {

    private static FeatureDetailPage featureDetailPage;
    private static FindFeaturePage findFeaturePage;
    private String featureId;
    private String featureName;
    private String featureTypeName;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        final AddFeatureTypePage addFeatureTypePage = adminToolPage.getPage(AddFeatureTypePage.class);
        final AddFeaturePage addFeaturePage = adminToolPage.getPage(AddFeaturePage.class);
        featureDetailPage = adminToolPage.getPage(FeatureDetailPage.class);
        findFeaturePage = adminToolPage.getPage(FindFeaturePage.class);

        featureTypeName = PelicanConstants.FEATURE_TYPE + RandomStringUtils.randomAlphabetic(8);
        featureName = PelicanConstants.FEATURE + RandomStringUtils.randomAlphabetic(8);

        // Navigate to the add feature page and add a feature
        addFeatureTypePage.addFeatureType(PelicanConstants.APPLICATION_FAMILY_NAME,
            getEnvironmentVariables().getApplicationDescription(), featureTypeName, featureTypeName);
        addFeatureTypePage.clickOnAddFeatureTypeButton();

        // Navigate to the add feature page and add a feature
        addFeaturePage.addFeature(featureTypeName, featureName, featureName, PelicanConstants.YES);
        featureDetailPage = addFeaturePage.clickOnSave();
        // Get the Feature Id from the feature added
        featureId = featureDetailPage.getFeatureId();

    }

    /**
     * This is a test method which will test the feature by id
     */
    @Test
    public void testFindFeaturesById() {

        // Get the Feature Id from the feature added
        featureDetailPage = findFeaturePage.findFeatureById(featureId);

        commonAssertionForFindFeatures(featureDetailPage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the feature by external key
     */
    @Test
    public void testFindFeaturesByExternalKey() {

        featureDetailPage = findFeaturePage.findFeatureByExternalKey(featureName);

        commonAssertionForFindFeatures(featureDetailPage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the feature by advanced find conditions
     */
    @Test
    public void testFindFeaturesByAdvancedFind() {

        final GenericGrid featureGrid = findFeaturePage.findFeatureByAdvancedFind(null, null);

        final List<String> featureHeadersList = featureGrid.getColumnHeaders();
        AssertCollector.assertThat("Incorrect feature search result header1", featureHeadersList.get(0), equalTo("ID"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header2", featureHeadersList.get(1),
            equalTo("External Key"), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header3", featureHeadersList.get(2),
            equalTo("Name"), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header4", featureHeadersList.get(3),
            equalTo("Feature Type"), assertionErrorList);

        featureDetailPage = findFeaturePage.selectResultRowWithAdvancedFind(1, null, null);
        AssertCollector.assertThat("Incorrect feature id", featureDetailPage.getFeatureId(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature type", featureDetailPage.getFeatureType(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the feature by name in advanced find
     */
    @Test
    public void testFindFeaturesByNameInAdvancedFind() {

        final GenericGrid featureGrid = findFeaturePage.findFeatureByAdvancedFind(featureName, featureTypeName);

        final List<String> featureHeadersList = featureGrid.getColumnHeaders();
        AssertCollector.assertThat("Incorrect feature search result header1", featureHeadersList.get(0), equalTo("ID"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header2", featureHeadersList.get(1),
            equalTo("External Key"), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header3", featureHeadersList.get(2),
            equalTo("Name"), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header4", featureHeadersList.get(3),
            equalTo("Feature Type"), assertionErrorList);

        featureDetailPage = findFeaturePage.selectResultRowWithAdvancedFind(1, featureName, null);

        commonAssertionForFindFeatures(featureDetailPage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the feature by name and feature type in advanced find
     */
    @Test
    public void testFindFeaturesByNameAndFeatureTypeInAdvancedFind() {

        final GenericGrid featureGrid = findFeaturePage.findFeatureByAdvancedFind(featureName, featureTypeName);

        final List<String> featureHeadersList = featureGrid.getColumnHeaders();
        AssertCollector.assertThat("Incorrect feature search result header1", featureHeadersList.get(0), equalTo("ID"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header2", featureHeadersList.get(1),
            equalTo("External Key"), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header3", featureHeadersList.get(2),
            equalTo("Name"), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result header4", featureHeadersList.get(3),
            equalTo("Feature Type"), assertionErrorList);

        featureDetailPage = findFeaturePage.selectResultRowWithAdvancedFind(1, featureName, featureTypeName);

        commonAssertionForFindFeatures(featureDetailPage);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to check whether the advanced find has include inactive features checkbox.
     *
     */
    @Test
    public void testAdvancedFindIncludeInActiveFeaturesChecbox() {

        findFeaturePage.showAdvancedFindTab();
        AssertCollector.assertTrue("The 'include inactive features checkbox is not present'",
            findFeaturePage.isIncludeInActiveFeaturesCheckboxPresent(), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method which will test the active field is present on the feature search results page.
     */
    @Test
    public void testActiveStatusInFeatureSearchResultsPage() {

        FeatureSearchResultsPage featureSearchResultsPage = findFeaturePage.findFeatureByEmptyId();

        List<String> featureHeadersList = featureSearchResultsPage.getColumnHeaders();
        AssertCollector.assertThat("Incorrect feature search by id result header1", featureHeadersList.get(0),
            equalTo(PelicanConstants.ID_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search by id result header2", featureHeadersList.get(1),
            equalTo(PelicanConstants.EXTERNAL_KEY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search by id result header3", featureHeadersList.get(2),
            equalTo(PelicanConstants.NAME_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search by id result header4", featureHeadersList.get(3),
            equalTo(PelicanConstants.FEATURE_TYPE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search by id result header4", featureHeadersList.get(4),
            equalTo(PelicanConstants.ACTIVE_FIELD), assertionErrorList);

        featureSearchResultsPage = findFeaturePage.findFeatureByAdvancedFind(true);
        featureHeadersList.clear();
        featureHeadersList = featureSearchResultsPage.getColumnHeaders();
        AssertCollector.assertThat("Incorrect feature search by advanced find result header1",
            featureHeadersList.get(0), equalTo(PelicanConstants.ID_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search by advanced find result header2",
            featureHeadersList.get(1), equalTo(PelicanConstants.EXTERNAL_KEY_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result by advanced find header3",
            featureHeadersList.get(2), equalTo(PelicanConstants.NAME_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result by advanced find header4",
            featureHeadersList.get(3), equalTo(PelicanConstants.FEATURE_TYPE_FIELD), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature search result by advanced find header4",
            featureHeadersList.get(4), equalTo(PelicanConstants.ACTIVE_FIELD), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test include inactive features checkbox functionality
     *
     */
    @Test
    public void testIncludeInActiveFeaturesCheckboxFunctionality() {

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
     * This method is a common method to do feature page assertion
     */
    private void commonAssertionForFindFeatures(final FeatureDetailPage featureDetailPage) {

        AssertCollector.assertThat("Incorrect feature id", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(), equalTo(featureName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            equalTo(featureName), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature type", featureDetailPage.getFeatureType(),
            equalTo(featureTypeName), assertionErrorList);
    }
}
