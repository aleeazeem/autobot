package com.autodesk.bsm.pelican.ui.licensingmodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.AddLicensingModelPage;
import com.autodesk.bsm.pelican.ui.pages.licensingmodel.LicensingModelDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * This Test class tests the Add Licensing Model scenarios
 *
 * @author mandas
 */
public class AddLicensingModelTest extends SeleniumWebdriver {

    private AddLicensingModelPage addEditLicensingModelPage;
    private LicensingModelDetailPage licensingModelDetailPage;
    private AddSubscriptionPlanPage addSubscriptionPlan;
    private String name;
    private String description;
    private String externalKey;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        addEditLicensingModelPage = adminToolPage.getPage(AddLicensingModelPage.class);
        licensingModelDetailPage = adminToolPage.getPage(LicensingModelDetailPage.class);
        addSubscriptionPlan = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        name = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        description = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
        externalKey = PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }

    /**
     * Test to add a Licensing Model with valid data
     */
    @Test
    public void testAddLicensingModelSuccess() {

        licensingModelDetailPage =
            addEditLicensingModelPage.addLicencingModel(name, description, externalKey, false, false);
        AssertCollector.assertThat("Could NOT find Licensing Model ID", licensingModelDetailPage.getId(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Name did not match", licensingModelDetailPage.getName(),
            equalTo(name), assertionErrorList);
        AssertCollector.assertThat("Licensing Model Description did not match",
            licensingModelDetailPage.getDescription(), equalTo(description), assertionErrorList);
        AssertCollector.assertThat("Licensing Model External Key did not match",
            licensingModelDetailPage.getExternalKey(), equalTo(externalKey), assertionErrorList);
        AssertCollector.assertFalse("Licensing Model Subscription Lifecycle did not match",
            Boolean.valueOf(licensingModelDetailPage.getSubscriptionLifecycle()), assertionErrorList);
        AssertCollector.assertFalse("Licensing Model For Finite Time did not match",
            Boolean.valueOf(licensingModelDetailPage.getForFiniteTime()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to add a Licensing Model with Invalid data
     */
    @Test(dataProvider = "dataForLicensingModel")
    public void testAddLicensingModelErrorScenarios(final String name, final String description,
        final String externalKey) {
        addEditLicensingModelPage.addLicencingModel(name, description, externalKey, false, false);
        AssertCollector.assertThat("Add Licensing Model with Invalid Input Failed!",
            addEditLicensingModelPage.getError(), equalTo(""), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests adding a subscription plan with new Licensing Model
     *
     * @throws ParseException
     */
    @Test
    public void testAddSubscriptionPlanWithNewLicenseModel() {

        // Add Licensing Model.
        licensingModelDetailPage =
            addEditLicensingModelPage.addLicencingModel(name, description, externalKey, false, false);

        final String subscriptionPlanExtKey = RandomStringUtils.randomAlphanumeric(8);
        // Add subscription Plan Info
        addSubscriptionPlan.addSubscriptionPlanInfo(subscriptionPlanExtKey + "Name", subscriptionPlanExtKey,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD,
            UsageType.COM, "OfferingDetails1 (DC020500)",
            getProductLineExternalKeyMaya() + " (" + getProductLineExternalKeyMaya() + ")", SupportLevel.BASIC, null,
            true);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        // Add Feature Entitlement
        addSubscriptionPlan.addOneTimeFeatureEntitlement(item.getId(), name, new ArrayList<>(
            ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1, PelicanConstants.CORE_PRODUCT_AUTO_2)), 0);
        AssertCollector.assertThat("Incorrect Feature Name is disaplyed", item.getName(),
            equalTo(addSubscriptionPlan.getFeatureName(0)), assertionErrorList);

        final String currencyAmount = "100";
        // Add Currency Amount Entitlement
        addSubscriptionPlan.addOneTimeCurrencyAmountEntitlement(currencyAmount, PelicanConstants.CLOUD_CURRENCY_SELECT,
            1);

        // Click on Save
        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = addSubscriptionPlan.clickOnSave(true);
        AssertCollector.assertThat("Failed to create Subscription Plan with valid Licensing Model",
            subscriptionPlanDetailPage.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Licensing Model name Key did not match",
            subscriptionPlanDetailPage.getOneTimeEntitlementLicensingModelColumnValues().get(0), equalTo(name),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a data provider to return data required to Licensing Model error scenarios
     *
     * @return A two dimensional object array
     */
    @DataProvider(name = "dataForLicensingModel")
    public static Object[][] addLicensingModel() {
        return new Object[][] {
                { "", PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphabetic(6),
                        PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphabetic(6) },
                { PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphabetic(6),
                        PelicanConstants.LICENSE_MODEL_PREFIX + RandomStringUtils.randomAlphabetic(6), "" },
                { "", "", "" } };
    }

}
