package com.autodesk.bsm.pelican.ui.descriptor;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanAndOfferDescriptorsPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DescriptorUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This class tests Add Descriptors to Subscription Plans functionality, includes tests to verify the following:
 */
public class AddDescriptorsToSubscriptionPlanTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private final String subscriptionOfferExternalKey = null;
    private String subscriptionPlanId = null;
    private String fieldName;
    private String apiName;
    private DescriptorDefinitionDetailPage detailPage;
    private Descriptor descriptorsPage;
    private AddDescriptorPage addDescriptorsPage;
    private SubscriptionPlanDetailPage subscriptionPlan;
    private AddSubscriptionPlanPage addSubscriptionPlanPage;
    private EditSubscriptionPlanAndOfferDescriptorsPage editSubscriptionPlanAndOfferDescriptorsPage;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(AddDescriptorsToSubscriptionPlanTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings offerings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        subscriptionPlanId = offerings.getOfferings().get(0).getId();
        LOGGER.info("External key of Subscription Offers which can be used while adding a Promotion "
            + subscriptionOfferExternalKey);
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        subscriptionPlan = adminToolPage.getPage(SubscriptionPlanDetailPage.class);
        addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);
        editSubscriptionPlanAndOfferDescriptorsPage =
            adminToolPage.getPage(EditSubscriptionPlanAndOfferDescriptorsPage.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        fieldName = "TestDescriptor_" + RandomStringUtils.randomAlphanumeric(5);
        apiName = "DescriptorHeader_" + RandomStringUtils.randomAlphanumeric(5);
        detailPage = new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
    }

    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final Method method) {
        LOGGER.info(String.format("====== End: %s ======", method.getName()));
        deleteDescriptors(descriptorsPage.getFieldName(), descriptorsPage.getGroupName(),
            descriptorsPage.getEntity().getEntity());
    }

    // In all the test methods ,after each submit and click actions there is sleep statement .Don't
    // remove as test fails

    /**
     * Method to verify if Descriptors are added successfully to Subscription Plan
     */
    @Test
    public void verifyAddedDescriptorsInSubscriptionPlan() {
        final Descriptor descriptors = DescriptorUtils.getDescriptorData(
            DescriptorEntityTypes.getEntityType("Subscription Plan"), "ipp", fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        subscriptionPlan.navigateToSubscriptionPlanPage(subscriptionPlanId);

        // wait for Edit subscription page o load.don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = addSubscriptionPlanPage.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);

        // Verify Localized descriptors can be edited.
        editSubscriptionPlanAndOfferDescriptorsPage = subscriptionPlan.clickOnEditLocalizedDescriptors();
        final String descriptorValue = "Subscription";
        editSubscriptionPlanAndOfferDescriptorsPage.editDescriptorValue("ipp", descriptorsPage.getApiName(),
            descriptorValue);
        editSubscriptionPlanAndOfferDescriptorsPage.submit(TimeConstants.TWO_SEC);

        // wait for localized descriptors in Subscription plan to load. don't delete as test may fail
        editSubscriptionPlanAndOfferDescriptorsPage = subscriptionPlan.clickOnEditLocalizedDescriptors();
        editSubscriptionPlanAndOfferDescriptorsPage.editDescriptorValue("ipp", descriptorsPage.getApiName(), "");
        editSubscriptionPlanAndOfferDescriptorsPage.submit(TimeConstants.THREE_SEC);

        // Verify Localized descriptors with different Locales.
        subscriptionPlan.setOtherLocales("Other Locale...");
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // Chinese, China (zh-CN)
        final String language = "Chinese (zh)";
        final String country = "China (CN)";
        subscriptionPlan.setLanguague(language);
        subscriptionPlan.setCountry(country);
        subscriptionPlan.submit(TimeConstants.THREE_SEC);
        // don't remove as test fails
        final String localeValue = "China - Chinese";
        subscriptionPlan.editDescriptorValue(descriptorsPage.getFieldName(), localeValue);
        subscriptionPlan.submit(TimeConstants.TWO_SEC);

        final String lang =
            language.substring(0, language.indexOf("(") - 1) + ", " + country.substring(0, country.indexOf("(") - 1);
        final String count = language.substring(language.indexOf("(") + 1, language.indexOf(")")) + "-"
            + country.substring(country.indexOf("(") + 1, country.indexOf(")"));
        final String expectedSelectValue = lang + " (" + count + ")";
        final String actualSelectedValue = subscriptionPlan.getFirstSelectedSelectValue();

        AssertCollector.assertThat("Expected Language and country values are not matched", actualSelectedValue,
            equalTo(expectedSelectValue), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        subscriptionPlan.clickOnEditLocalizedDescriptors();
        // don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        subscriptionPlan.editDescriptorValue(descriptorsPage.getFieldName(), "");
        subscriptionPlan.submit(TimeConstants.TWO_SEC);

        // Verify No.of Localized Descriptors on Edit Subscription Plan Page.
        final List<String> localizedDescriptorsList = subscriptionPlan.getAllLocalizedDescriptorsNames();
        subscriptionPlan.clickOnEditLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        for (final String localizedDescriptorsNames : localizedDescriptorsList) {
            AssertCollector.assertThat("Localized Descriptors name is not matched",
                descriptorGrid.isEditDescriptorDisplayed(localizedDescriptorsNames), equalTo(true), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to test if Non-Localized Descriptors are added successfully to Subscription plan
     */
    @Test
    public void verifyNonLocalizedDescriptorsForSubscriptionPlan() {
        final Descriptor descriptors = DescriptorUtils.getDescriptorData(
            DescriptorEntityTypes.getEntityType("Subscription Plan"), "ipp", fieldName, apiName, "No", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        subscriptionPlan.navigateToSubscriptionPlanPage(subscriptionPlanId);

        // wait for Edit Subscription plan page to load .Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = addSubscriptionPlanPage.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
        subscriptionPlan.clickOnEditNonLocalizedDescriptors();

        // Verify Non-Localized descriptors can be editable.
        final String descriptorValue = "Non-localized";
        editSubscriptionPlanAndOfferDescriptorsPage.editNonLocalizedIPPDescriptorValue(descriptorValue);
        editSubscriptionPlanAndOfferDescriptorsPage.submit(TimeConstants.TWO_SEC);
        subscriptionPlan.clickOnEditNonLocalizedDescriptors();
        editSubscriptionPlanAndOfferDescriptorsPage.editNonLocalizedIPPDescriptorValue("");
        editSubscriptionPlanAndOfferDescriptorsPage.submit(TimeConstants.TWO_SEC);

        // Verify No.of Non-Localized descriptors on Edit Subscription Plan Page.
        final List<String> localizedDescriptorsList = subscriptionPlan.getAllNonLocalizedDescriptorsNames();
        subscriptionPlan.clickOnEditNonLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        for (final String localizedDescriptorsNames : localizedDescriptorsList) {
            AssertCollector.assertThat("Non Localized Descriptors name is not matched",
                descriptorGrid.isEditDescriptorDisplayed(localizedDescriptorsNames), equalTo(true), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to delete the descriptors.
     */
    private void deleteDescriptors(final String fieldName, final String groupName, final String entity) {
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        addDescriptorsPage.navigateToFindDescriptors();
        addDescriptorsPage.deleteDescriptors(fieldName, groupName, entity);
    }

}
