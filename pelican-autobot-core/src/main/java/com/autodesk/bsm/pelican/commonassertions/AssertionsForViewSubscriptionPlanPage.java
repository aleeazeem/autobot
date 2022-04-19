package com.autodesk.bsm.pelican.commonassertions;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.List;

public class AssertionsForViewSubscriptionPlanPage {

    /**
     * This is a method which will assert on the collapsable fields on the entitlement section on the offering detail
     * page
     *
     * @param entitlementId
     * @param expectedAssignableValue
     * @param eosDate
     * @param eolRenewalDate
     * @param eolImmediateDate
     * @param isFeature TODO
     * @param assertionErrorList
     * @param subscriptionplandetailpage
     *
     * @throws ParseException
     */
    public static void assertCollapsableEntitlementFields(final SubscriptionPlanDetailPage subscriptionPlanDetailPage,
        final String entitlementId, final String expectedAssignableValue, final String eosDate,
        final String eolRenewalDate, final String eolImmediateDate, final boolean isFeature,
        final List<AssertionError> assertionErrorList) throws ParseException {
        AssertCollector.assertThat("Incorrect Item type column name in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 1, 1),
            equalTo(PelicanConstants.ITEM_TYPE), assertionErrorList);
        if (isFeature) {

            AssertCollector.assertThat("Incorrect Feature column name in the entitlement section",
                subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 1, 2),
                equalTo(PelicanConstants.FEATURE_FIELD_SUBSCRIPTION_PLAN), assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect Currency column name in the entitlement section",
                subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 1, 2),
                equalTo(PelicanConstants.CURRENCY_FIELD_SUBSCRIPTION_PLAN), assertionErrorList);
        }

        AssertCollector.assertThat("Incorrect assignable column name in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 1, 3),
            equalTo(PelicanConstants.ASSIGNABLE_COLUMN_NAME + ":"), assertionErrorList);

        if (isFeature) {
            AssertCollector.assertThat("Incorrect assignable column value in the entitlement section",
                subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 1, 4),
                equalTo(expectedAssignableValue), assertionErrorList);
        } else {
            AssertCollector.assertThat("Incorrect assignable column value in the entitlement section",
                subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 1, 4), equalTo("-"),
                assertionErrorList);
        }

        final String expectedEosDate;
        final String expectedEolImmediateDate;
        final String expectedEolRenewalDate;

        if (StringUtils.isNotEmpty(eosDate) && !(eosDate.equals(PelicanConstants.HIPHEN))) {
            expectedEosDate = DateTimeUtils.getDateAsText(eosDate);
        } else {
            expectedEosDate = "-";
        }

        if (StringUtils.isNotEmpty(eolImmediateDate) && !(eolImmediateDate.equals(PelicanConstants.HIPHEN))) {
            expectedEolImmediateDate = DateTimeUtils.getDateAsText(eolImmediateDate);
        } else {
            expectedEolImmediateDate = "-";
        }

        if (StringUtils.isNotEmpty(eolRenewalDate) && !(eolRenewalDate.equals(PelicanConstants.HIPHEN))) {
            expectedEolRenewalDate = DateTimeUtils.getDateAsText(eolRenewalDate);
        } else {
            expectedEolRenewalDate = "-";
        }

        AssertCollector.assertThat("Incorrect eos date in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 2, 1),
            equalTo(PelicanConstants.EOS_DATE_COLUMN_NAME + ":"), assertionErrorList);

        AssertCollector.assertThat("Incorrect eos date value in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 2, 2),
            equalTo(expectedEosDate), assertionErrorList);

        AssertCollector.assertThat("Incorrect eol renewal date in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 2, 3),
            equalTo(PelicanConstants.EOL_RENEWAL_DATE_COLUMN_NAME + ":"), assertionErrorList);

        AssertCollector.assertThat("Incorrect eol renewal date value in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 2, 4),
            equalTo(expectedEolRenewalDate), assertionErrorList);

        AssertCollector.assertThat("Incorrect eol immediate date in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 3, 3),
            equalTo(PelicanConstants.EOL_IMMEDIATE_DATE_COLUMN_NAME + ":"), assertionErrorList);

        AssertCollector.assertThat("Incorrect eol immediate date in the entitlement section",
            subscriptionPlanDetailPage.getDataFromFeatureCollapsableSection(entitlementId, 3, 4),
            equalTo(expectedEolImmediateDate), assertionErrorList);
    }
}
