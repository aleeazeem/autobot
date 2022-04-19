package com.autodesk.bsm.pelican.bvt;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.ui.pages.features.FeatureDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import java.util.List;

public class HelperForCommonAssertionsOfFeature {

    /**
     * This is a method for common assertions on the feature.
     *
     * @param featureDetailPage
     * @param featureId
     * @param featureName
     * @param featureExternalKey
     * @param featureType
     * @param status
     * @param assertionErrorList
     */
    public static void commonAssertionsOfFeature(final FeatureDetailPage featureDetailPage, final String featureId,
        final String featureName, final String featureExternalKey, final String featureType, final String status,
        final List<AssertionError> assertionErrorList) {

        AssertCollector.assertThat("Incorrect feature id", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature name", featureDetailPage.getFeatureName(), equalTo(featureName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect feature external key", featureDetailPage.getFeatureExternalKey(),
            equalTo(featureExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect feature type", featureDetailPage.getFeatureType(), equalTo(featureType),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect status value of the feature", featureDetailPage.getActive(),
            equalTo(status), assertionErrorList);
    }
}
