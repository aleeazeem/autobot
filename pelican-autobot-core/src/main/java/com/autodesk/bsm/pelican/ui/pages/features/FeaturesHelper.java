package com.autodesk.bsm.pelican.ui.pages.features;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.helper.auditlog.SubscriptionPlanAuditLogHelper;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.Iterables;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FeaturesHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeaturesHelper.class.getSimpleName());

    /**
     * This is a helper method to return the subscriptionplan results
     *
     * @param featureId
     * @param productLineSelectText
     * @param findFeaturePage
     * @param featureDetailPage
     * @param assertionErrorList
     * @return GenericGrid - Subscription Plan Result Count
     */
    public GenericGrid getSubscriptionPlanResultCount(final String featureId, final String productLineSelectText,
        final FindFeaturePage findFeaturePage, final FeatureDetailPage featureDetailPage,
        final List<AssertionError> assertionErrorList) {
        // Find feature by id
        findFeaturePage.findFeatureById(featureId);

        AssertCollector.assertThat("Feature id does not match", featureDetailPage.getFeatureId(), equalTo(featureId),
            assertionErrorList);
        // click add feature link
        featureDetailPage.clickAddFeatureLink();

        Util.waitInSeconds(TimeConstants.TWO_SEC);
        // select product line
        if (productLineSelectText != null) {
            featureDetailPage.selectProductLine(productLineSelectText);
        }

        // find subscription with selected filters

        return featureDetailPage.clickFindSubscriptionPlansToAddFeature();
    }

    /**
     * This is a helper method for adding features to plans
     *
     * @param subscriptionPlanResultCount
     * @param subscriptionPlanResults
     * @param expectedWarnMessage
     * @param expectedAddFeaturePopUpMessage
     * @param featureDetailPage
     * @param assertionErrorList
     * @param coreProduct
     * @return TODO
     */
    public List<String> helperToAddFeatureToPlans(final int subscriptionPlanResultCount,
        final GenericGrid subscriptionPlanResults, final String expectedWarnMessage,
        final String expectedAddFeaturePopUpMessage, final FeatureDetailPage featureDetailPage,
        final List<String> coreProductList, final List<AssertionError> assertionErrorList) {
        List<String> addedSubscriptionPlanIdList = new ArrayList<>();
        if (subscriptionPlanResultCount != 0) {

            // select all subscription plan and get list of all the subscription
            // id
            addedSubscriptionPlanIdList = subscriptionPlanResults.getColumnValues("ID");
            LOGGER.info("Selected subscription id list: " + addedSubscriptionPlanIdList);

            featureDetailPage.clickSelectAll();

            featureDetailPage.clickSelectSubscriptionPlansToAddFeature();
            featureDetailPage.clickSelectCoreProducts(coreProductList);
            featureDetailPage.saveCoreProduct();

            featureDetailPage.clickAddFeature();
            final String popUpMessage = featureDetailPage.readMessageInPopUp();
            String warnMessage = null;

            if (StringUtils.isNotEmpty(popUpMessage)) {
                warnMessage = popUpMessage.split(PelicanConstants.SPLITTER)[0];
            }

            AssertCollector.assertThat("Warning popup message is incorrect", warnMessage, equalTo(expectedWarnMessage),
                assertionErrorList);

            // click confirm on pop up
            featureDetailPage.clickConfirmPopUp();

            // click ok on pop
            final String addFeatureMessage = featureDetailPage.readMessageOnAddFeaturePopUp();
            AssertCollector.assertThat("Feature is not added to the plans in the new state through bulk add",
                addFeatureMessage, equalTo(expectedAddFeaturePopUpMessage), assertionErrorList);
            featureDetailPage.clickConfirmOnAddFeaturePopUp();
        } else {
            LOGGER.error("No subscriptions found matching with search criteria");
        }
        featureDetailPage.switchDriverControlToParentWindow();
        return addedSubscriptionPlanIdList;
    }

    /**
     * This is a helper method for common DynamoDB assertions on add features.
     *
     * @param offeringId1
     * @param offeringId2
     * @param featureId1
     * @param featureId2
     * @param action
     * @param isAddFeature
     * @param environmentVariables
     * @param assertionErrorList
     * @throws ParseException
     */
    public void helperForDynamoDbAssertions(final String offeringId1, final String offeringId2, final String featureId1,
        final String featureId2, final Action action, final boolean isAddFeature,
        final EnvironmentVariables environmentVariables, final List<AssertionError> assertionErrorList) {

        String oldEntitlementId1 = null;
        String oldEntitlementId2 = null;
        String newEntitlementId1 = null;
        String newEntitlementId2 = null;
        if (StringUtils.isNotEmpty(featureId1)) {
            oldEntitlementId1 = DbUtils.getEntitlementIdFromItemId(offeringId1, featureId1, environmentVariables);
            oldEntitlementId2 = DbUtils.getEntitlementIdFromItemId(offeringId2, featureId1, environmentVariables);
        }

        if (StringUtils.isNotEmpty(featureId2)) {
            newEntitlementId1 = DbUtils.getEntitlementIdFromItemId(offeringId1, featureId2, environmentVariables);
            newEntitlementId2 = DbUtils.getEntitlementIdFromItemId(offeringId2, featureId2, environmentVariables);
        }

        // Verify audit log data for all subscription plan
        final Boolean isAuditLogFound1 = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(offeringId1,
            oldEntitlementId1, newEntitlementId1, action, isAddFeature, assertionErrorList);

        final Boolean isAuditLogFound2 = SubscriptionPlanAuditLogHelper.validateSubscriptionPlanEntitlement(offeringId2,
            oldEntitlementId2, newEntitlementId2, action, isAddFeature, assertionErrorList);

        AssertCollector.assertTrue("Feature update is not found in the dynamo db for plan1", isAuditLogFound1,
            assertionErrorList);
        AssertCollector.assertTrue("Feature update is not found in the dynamo db for plan2", isAuditLogFound2,
            assertionErrorList);
    }

    /**
     * Helper method for verifying the core product on subscription plan page.
     *
     * @param SubscriptionPlanIdList
     * @param coreProductList
     * @param findSubscriptionPlanPage
     * @param assertionErrorList
     */
    public void helperForCoreProductAssertion(final List<String> SubscriptionPlanIdList,
        final List<String> coreProductList, final FindSubscriptionPlanPage findSubscriptionPlanPage,
        final List<AssertionError> assertionErrorList) {
        for (final String subscriptionPlanId : SubscriptionPlanIdList) {
            final SubscriptionPlanDetailPage subscriptionPlanDetailPage =
                findSubscriptionPlanPage.findSubscriptionPlanById(subscriptionPlanId);
            AssertCollector.assertThat(
                "Core product value is not correct on subscription plan page for plan id: " + subscriptionPlanId,
                Iterables.getLast(subscriptionPlanDetailPage.getOneTimeEntitlementCoreProductColumnValues()),
                equalTo(String.join(", ", coreProductList)), assertionErrorList);
        }
    }

}
