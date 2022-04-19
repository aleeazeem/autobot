package com.autodesk.bsm.pelican.ui.descriptor;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.promotions.PromotionDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * This class tests Add Descriptors to Promotions functionality, includes tests to verify the following:
 */
public class AddDescriptorsToPromotionsTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private DescriptorDefinitionDetailPage detailPage;
    private String fieldName;
    private String apiName;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private Descriptor descriptorsPage;
    private String promotionId;
    private PromotionDetailsPage promotionDetails;
    private AddDescriptorPage addDescriptorsPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddDescriptorsToPromotionsTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings basicOffering1 = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        final JPromotion activeDiscountAmountPromo = promotionUtils.addPromotion(PromotionType.DISCOUNT_AMOUNT,
            Lists.newArrayList(getStoreUs()), Lists.newArrayList(basicOffering1), promotionUtils.getRandomPromoCode(),
            false, Status.ACTIVE, null, "10.00", DateTimeUtils.getUTCFutureExpirationDate(), null, null, 2, null, null);
        promotionId = activeDiscountAmountPromo.getData().getId();
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        promotionDetails = adminToolPage.getPage(PromotionDetailsPage.class);

    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        fieldName = "TestDescriptor_" + RandomStringUtils.randomAlphanumeric(5);
        apiName = "DescriptorHeader_" + RandomStringUtils.randomAlphanumeric(5);
        detailPage = new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
        descriptorsPage = new Descriptor();
    }

    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final Method method) {
        LOGGER.info(String.format("====== End: %s ======", method.getName()));
        deleteDescriptors(descriptorsPage.getFieldName(), descriptorsPage.getGroupName(),
            descriptorsPage.getEntity().getEntity());
    }

    /**
     * Method to verify if Descriptors are added successfully
     */
    @Test
    public void verifyDescriptorsAddedSuccessfully() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        final GenericGrid descriptorGrid = promotionDetails.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    // In all the test methods ,after each submit and click actions there is sleep statement .Don't
    // remove as test fails

    /**
     * Method to verify if Localized descriptors can be edited , saved and deleted
     */
    @Test
    public void verifyEditLocalizedDescriptors() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        // Wait for descriptor name to be loaded. Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = promotionDetails.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.clickEditLocalizedDescriptors();
        // Wait for Localized descriptors to be loaded. Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String descriptorValue = "Descriptor";
        promotionDetails.editLocalizedDescriptorValue(descriptorsPage.getFieldName(), descriptorValue);
        promotionDetails.submit(TimeConstants.TWO_SEC);
        // Don't remove as test fails
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isEditDescriptorDisplayed(descriptorValue), equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.clickEditLocalizedDescriptors();
        // Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetails.editLocalizedDescriptorValue(descriptorsPage.getFieldName(), "");
        promotionDetails.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify if country and language can be selected while choosing Other Locale
     */
    @Test
    public void verifyDifferentLocaleDescriptors() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        // wait for Edit Promotions page to load.Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = promotionDetails.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.setOtherLocales("Other Locale...");
        // wait for Locales to be set ,Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String language = "Hindi (hi)";
        promotionDetails.setLanguague(language);
        final String country = "India (IN)";
        promotionDetails.setCountry(country);
        promotionDetails.submit(TimeConstants.ONE_SEC);
        // Don't remove as test fails
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        final String localeValue = "India - Hindi";
        promotionDetails.editLocalizedDescriptorValue(descriptorsPage.getFieldName(), localeValue);
        promotionDetails.submit(TimeConstants.ONE_SEC);
        final String lang =
            language.substring(0, language.indexOf("(") - 1) + ", " + country.substring(0, country.indexOf("(") - 1);
        final String coun = language.substring(language.indexOf("(") + 1, language.indexOf(")")) + "-"
            + country.substring(country.indexOf("(") + 1, country.indexOf(")"));
        final String expectedSelectValue = lang + " (" + coun + ")";
        final String actualSelectedValue = promotionDetails.getFirstSelectedSelectValue();
        AssertCollector.assertThat("Expected Language and country values are not matched", actualSelectedValue,
            equalTo(expectedSelectValue), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.clickEditLocalizedDescriptors();
        // Wait for Localized descriptors to load,don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetails.editLocalizedDescriptorValue(descriptorsPage.getFieldName(), "");
        promotionDetails.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify if various languages can be selected for Localized Descriptors
     */
    @Test
    public void verifyDifferentLanguageDescriptors() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        GenericGrid descriptorGrid = promotionDetails.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.setOtherLocales("Other Locale...");
        // Don't remove as test fails
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String frenchLanguage = "French (fr)";
        promotionDetails.setLanguague(frenchLanguage);
        promotionDetails.submit(TimeConstants.ONE_SEC);
        // wait for find promotions page .don't remove
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String frenchLocaleValue = "French";
        promotionDetails.editLocalizedDescriptorValue(descriptorsPage.getFieldName(), frenchLocaleValue);
        promotionDetails.submit(TimeConstants.TWO_SEC);
        descriptorGrid = promotionDetails.getGrid();
        final String actualSelectedValue = promotionDetails.getFirstSelectedSelectValue();
        AssertCollector.assertThat("Selected Locale value is not matched with expected", actualSelectedValue,
            equalTo(frenchLanguage), assertionErrorList);
        AssertCollector.assertThat("Descriptors field value is not matched with expected",
            descriptorGrid.getLocalizedDescriptorValue(descriptors.getFieldName()), equalTo(frenchLocaleValue),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.clickEditLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetails.editLocalizedDescriptorValue(descriptorsPage.getFieldName(), "");
        promotionDetails.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify if Non Localized Descriptors can be Edited ,saved and deleted
     */
    // This method is temporarily disabled. In AUTO family, not seeing non localized descriptors
    // @Test
    public void verifyEditNonLocalizedDescriptors() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = promotionDetails.getGrid();
        AssertCollector.assertThat("Descriptors field name is not matched",
            descriptorGrid.isDescriptorDisplayed(descriptorsPage.getFieldName()), equalTo(true), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
        promotionDetails.clickEditNonLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final String descriptorValue = "Sample Non-localized";
        promotionDetails.editNonLocalizedDescriptorValue(descriptorValue);
        promotionDetails.submit(TimeConstants.TWO_SEC);
        promotionDetails.clickEditNonLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        promotionDetails.editNonLocalizedDescriptorValue("");
        promotionDetails.submit(TimeConstants.ONE_SEC);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to verify all Localized Descriptors available
     */
    @Test
    public void verifyAllLocalizedDescriptorsNames() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final List<String> localizedDescriptorsList = promotionDetails.getAllLocalizedDescriptorsNames();
        promotionDetails.clickEditLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = promotionDetails.getGrid();
        for (final String localizedDescriptorsNames : localizedDescriptorsList) {
            AssertCollector.assertThat("Localized Descriptors name is not matched",
                descriptorGrid.isEditDescriptorDisplayed(localizedDescriptorsNames), equalTo(true), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to verify all Non Localized Descriptors available
     */
    // This method is temporarily disabled. In AUTO family, not seeing non localized descriptors
    // @Test
    public void verifyAllNonLocalizedDescriptorsNames() {
        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptorsPage = detailPage.getDescriptorEntityFromDetails();
        LOGGER.info("Verify descriptors added successfully");
        promotionDetails.navigateToPromotionDetails(promotionId);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final List<String> localizedDescriptorsList = promotionDetails.getAllNonLocalizedDescriptorsNames();
        promotionDetails.clickEditNonLocalizedDescriptors();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        final GenericGrid descriptorGrid = promotionDetails.getGrid();
        for (final String localizedDescriptorsNames : localizedDescriptorsList) {
            AssertCollector.assertThat("Localized Descriptors name is not matched",
                descriptorGrid.isEditDescriptorDisplayed(localizedDescriptorsNames), equalTo(true), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method reads the values of all attributes in a descriptor details Page and creates a descriptor entity
     */
    private Descriptor getDescriptorsData(final DescriptorEntityTypes entityType, final String groupName,
        final String fieldName, final String apiName, final String localized, final String maxLength) {
        LOGGER.info("Set the descriptors values");
        final Descriptor descriptors = new Descriptor();
        descriptors.setAppFamily("Automated Tests (AUTO)");
        descriptors.setEntity(entityType);
        descriptors.setGroupName(groupName);
        descriptors.setFieldName(fieldName);
        descriptors.setApiName(apiName);
        descriptors.setLocalized(localized);
        descriptors.setMaxLength(maxLength);
        return descriptors;
    }

    /**
     * Method to delete the descriptors.
     */
    private void deleteDescriptors(final String fieldName, final String groupName, final String entity) {
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        // Navigate to Find Descriptors page.
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        addDescriptorsPage.navigateToFindDescriptors();
        // Delete the descriptors.
        addDescriptorsPage.deleteDescriptors(fieldName, groupName, entity);
    }
}
