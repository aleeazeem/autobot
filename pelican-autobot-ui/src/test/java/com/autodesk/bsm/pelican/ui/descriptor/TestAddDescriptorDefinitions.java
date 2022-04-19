package com.autodesk.bsm.pelican.ui.descriptor;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.DescriptorGroups;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorDefinitionsPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Admin Tool tests to automate test cases in US5555
 *
 * @author kishor
 */
public class TestAddDescriptorDefinitions extends SeleniumWebdriver {

    private static final String OTHER_GROUP = "Other_Group";
    private AdminToolPage adminToolPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestAddDescriptorDefinitions.class.getSimpleName());

    /**
     * Data setup - create Product Lines if not available
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

    }

    /**
     * Tests the combinations of valid values for the Add A Descriptor
     */
    @Test(dataProvider = "getValidData")
    public void testAddDescriptorWithAllValidValues(final String testDescription,
        final DescriptorEntityTypes entityType, final String groupName, String fieldName, final String localized,
        final String maxLength) {
        // Generate a random string to make sure unique names are generated
        // everytime
        final String generatedString = RandomStringUtils.randomAlphabetic(4);
        fieldName = fieldName + generatedString;
        final String apiName = fieldName.replaceAll(" ", "-");
        // Create a descriptor object with all values set with valid entries
        final Descriptor descriptorObj =
            createDescriptorObject(entityType, groupName, fieldName, apiName, localized, maxLength);
        final AddDescriptorDefinitionsPage descriptorPage = adminToolPage.getPage(AddDescriptorDefinitionsPage.class);

        // invoking add descriptor method from AddDescriptorDefinitionPage pageClass
        descriptorPage.addDescriptors(descriptorObj);
        final Descriptor actualDescriptorCreated =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables())
                .getDescriptorEntityFromDetails();

        AssertCollector.assertThat("Invalid field Name found", actualDescriptorCreated.getFieldName(),
            equalTo(descriptorObj.getFieldName()), assertionErrorList);
        AssertCollector.assertThat("Invalid Api Name found", actualDescriptorCreated.getApiName(),
            equalTo(descriptorObj.getApiName()), assertionErrorList);
        // Don't assert if null value is sent by dataprovider
        if (entityType != null) {
            AssertCollector.assertThat("Invalid Entity found", actualDescriptorCreated.getEntity(),
                equalTo(descriptorObj.getEntity()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (groupName != null) {
            AssertCollector.assertThat("Invalid Group Name found", actualDescriptorCreated.getGroupName(),
                equalTo(descriptorObj.getGroupName()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (maxLength != null) {
            AssertCollector.assertThat("Invalid Max Length", actualDescriptorCreated.getMaxLength(),
                equalTo(descriptorObj.getMaxLength()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (localized != null) {
            AssertCollector.assertThat("Invalid Localized Status", actualDescriptorCreated.getLocalized(),
                equalTo(descriptorObj.getLocalized()), assertionErrorList);
        }

        final String descriptorId = actualDescriptorCreated.getId();
        LOGGER.info("Id :" + descriptorId);
        // Delete the descriptor created by the tests
        descriptorPage.deleteDescriptor(descriptorId);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test will test all combinations of add descriptors without any max length value set and expects to work
     * without any errors
     */
    @Test(dataProvider = "getDataWithNoMaxLength")
    public void testAddDescriptorWithoutMaxLength(final String testDescription, final DescriptorEntityTypes entityType,
        final String groupName, String fieldName, final String localized) {
        // Generate a random string to make sure unique names are generated
        // everytime
        final String generatedString = RandomStringUtils.randomAlphabetic(4);
        fieldName = fieldName + generatedString;
        final String apiName = fieldName.replaceAll(" ", "-");
        // Create a descriptor with empty maxLength Field
        final String maxLength = "";
        final Descriptor descriptorObj =
            createDescriptorObject(entityType, groupName, fieldName, apiName, localized, maxLength);
        final AddDescriptorDefinitionsPage descriptorPage = adminToolPage.getPage(AddDescriptorDefinitionsPage.class);

        descriptorPage.addDescriptors(descriptorObj);
        final Descriptor actualDescriptorCreated =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables())
                .getDescriptorEntityFromDetails();

        AssertCollector.assertThat("Invalid field Name found", actualDescriptorCreated.getFieldName(),
            equalTo(descriptorObj.getFieldName()), assertionErrorList);
        AssertCollector.assertThat("Invalid Api Name found", actualDescriptorCreated.getApiName(),
            equalTo(descriptorObj.getApiName()), assertionErrorList);
        // Don't assert if null value is sent by dataprovider
        if (entityType != null) {
            AssertCollector.assertThat("Invalid Entity found", actualDescriptorCreated.getEntity(),
                equalTo(descriptorObj.getEntity()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (groupName != null) {
            AssertCollector.assertThat("Invalid Group Name found", actualDescriptorCreated.getGroupName(),
                equalTo(descriptorObj.getGroupName()), assertionErrorList);
        }
        // Make sure the Max length is null value
        AssertCollector.assertThat("Invalid Max Length", actualDescriptorCreated.getMaxLength(), is(nullValue()),
            assertionErrorList);
        // Don't assert if null value is sent by dataprovider
        if (localized != null) {
            AssertCollector.assertThat("Invalid Localized Status", actualDescriptorCreated.getLocalized(),
                equalTo(descriptorObj.getLocalized()), assertionErrorList);
        }

        final String descriptorId = actualDescriptorCreated.getId();
        LOGGER.info("Id :" + descriptorId);
        // Delete the descriptor created by the tests
        descriptorPage.deleteDescriptor(descriptorId);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test will test all combinations of add descriptors without any group name set and expects to work without
     * any errors
     *
     * @param groupName - will be sent as empty from the dataprovider
     */
    @Test(dataProvider = "getDataWithNoGroupName")
    public void testAddDescriptorWithoutGroupName(final String testDescription, final DescriptorEntityTypes entityType,
        final String groupName, String fieldName, final String localized, final String maxLength) {
        // Generate a random string to make sure unique names are generated
        // everytime
        final String generatedString = RandomStringUtils.randomAlphabetic(4);
        fieldName = fieldName + generatedString;
        final String apiName = fieldName.replaceAll(" ", "-");
        // Create a descriptor without any groupName, So it should select the default option "None"
        final Descriptor descriptorObj =
            createDescriptorObject(entityType, groupName, fieldName, apiName, localized, maxLength);
        final AddDescriptorDefinitionsPage descriptorPage = adminToolPage.getPage(AddDescriptorDefinitionsPage.class);

        descriptorPage.addDescriptors(descriptorObj);
        final Descriptor actualDescriptorCreated =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables())
                .getDescriptorEntityFromDetails();

        AssertCollector.assertThat("Invalid field Name found", actualDescriptorCreated.getFieldName(),
            equalTo(descriptorObj.getFieldName()), assertionErrorList);
        AssertCollector.assertThat("Invalid Api Name found", actualDescriptorCreated.getApiName(),
            equalTo(descriptorObj.getApiName()), assertionErrorList);
        // Don't assert if null value is sent by dataprovider
        if (entityType != null) {
            AssertCollector.assertThat("Invalid Entity found", actualDescriptorCreated.getEntity(),
                equalTo(descriptorObj.getEntity()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (groupName != null) {
            // Assert this to be null as There is no Group Name selected
            AssertCollector.assertThat("Invalid Group Name found", actualDescriptorCreated.getGroupName(),
                is(nullValue()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (localized != null) {
            AssertCollector.assertThat("Invalid Localized Status", actualDescriptorCreated.getLocalized(),
                equalTo(descriptorObj.getLocalized()), assertionErrorList);
        }

        final String descriptorId = actualDescriptorCreated.getId();
        LOGGER.info("Id :" + descriptorId);
        // Delete the descriptor created by the tests
        descriptorPage.deleteDescriptor(descriptorId);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test will test all combinations of add descriptors without "Other" GroupName set and expects to work without
     * any errors
     */
    @Test(dataProvider = "getDataWithOtherGroupName")
    public void testAddDescriptorWithOtherGroupName(final String testDescription,
        final DescriptorEntityTypes entityType, final String groupName, String fieldName, final String localized,
        final String maxLength) {
        LOGGER.info("Test Scanrio : " + testDescription);
        // Generate a random string to make sure unique names are generated
        // everytime
        final String generatedString = RandomStringUtils.randomAlphabetic(4);
        fieldName = fieldName + generatedString;
        final String apiName = fieldName.replaceAll(" ", "-");
        // Create a descriptor with groupName as "Other" and will give a group Name as "Other_group"
        final Descriptor descriptorObj =
            createDescriptorObject(entityType, groupName, fieldName, apiName, localized, maxLength);
        final AddDescriptorDefinitionsPage descriptorPage = adminToolPage.getPage(AddDescriptorDefinitionsPage.class);

        descriptorPage.addDescriptors(descriptorObj);
        final Descriptor actualDescriptorCreated =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables())
                .getDescriptorEntityFromDetails();

        AssertCollector.assertThat("Invalid field Name found", actualDescriptorCreated.getFieldName(),
            equalTo(descriptorObj.getFieldName()), assertionErrorList);
        AssertCollector.assertThat("Invalid Api Name found", actualDescriptorCreated.getApiName(),
            equalTo(descriptorObj.getApiName()), assertionErrorList);
        // Don't assert if null value is sent by dataprovider
        if (entityType != null) {
            AssertCollector.assertThat("Invalid Entity found", actualDescriptorCreated.getEntity(),
                equalTo(descriptorObj.getEntity()), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (maxLength.isEmpty()) {
            AssertCollector.assertThat("Invalid Max Length", actualDescriptorCreated.getMaxLength(), is(nullValue()),
                assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (groupName != null) {
            // Assert this to be null as There is no Group Name selected
            AssertCollector.assertThat("Invalid Group Name found", actualDescriptorCreated.getGroupName(),
                equalTo(OTHER_GROUP), assertionErrorList);
        }
        // Don't assert if null value is sent by dataprovider
        if (localized != null) {
            AssertCollector.assertThat("Invalid Localized Status", actualDescriptorCreated.getLocalized(),
                equalTo(descriptorObj.getLocalized()), assertionErrorList);
        }

        final String descriptorId = actualDescriptorCreated.getId();
        LOGGER.info("Id :" + descriptorId);
        // Delete the descriptor created by the tests
        descriptorPage.deleteDescriptor(descriptorId);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This test will check the error messages displayed when mandatory fields like "Api Name", "Field Name","Entity
     * type" are left empty while submitting the addDescriptors form
     */
    @Test
    public void testAddDescriptorWithoutMandatoryFields() {

        final String fieldName = "";
        final String apiName = "";
        final DescriptorEntityTypes entityType = null;
        final String groupName = "";
        final String localized = "";
        final String maxLength = "";
        // Create a descriptor with groupName as "Other" and will give a group Name as "Other_group"
        final Descriptor descriptorObj =
            createDescriptorObject(entityType, groupName, fieldName, apiName, localized, maxLength);
        final AddDescriptorDefinitionsPage descriptorPage = adminToolPage.getPage(AddDescriptorDefinitionsPage.class);
        descriptorPage.addDescriptors(descriptorObj);

        AssertCollector.assertThat("No/ Invalid error message displayed for empty field name",
            descriptorPage.getFieldNameRequiredErrorMessage(), equalTo("Required"), assertionErrorList);
        AssertCollector.assertThat("No/ Invalid error message displayed for empty api name",
            descriptorPage.getApiNameRequiredErrorMessage(), equalTo("Required"), assertionErrorList);
        AssertCollector.assertThat("No/ Invalid error message displayed for empty Entity type",
            descriptorPage.getEntityRequiredErrorMessage(), equalTo("Required"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Dataprovider for valid data
     */
    @DataProvider(name = "getValidData")
    public Object[][] getValidData() {
        return new Object[][] {
                { "Add Localized descriptor for Subscription plan for Estore group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, DescriptorGroups.estore.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes", "50" },

                { "Add Non Localized descriptor for Subscription Offer for IPP group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, DescriptorGroups.ipp.toString(),
                        "AUTO TEST DESCRIPTOR", "No", "50" },

                { "Add Localized descriptor for Basic Offering for Estore group ", DescriptorEntityTypes.BASIC_OFFERING,
                        DescriptorGroups.estore.toString(), "AUTO TEST DESCRIPTOR", "Yes", "50" },

                { "Add Localized descriptor for Shipping Method for Estore group ",
                        DescriptorEntityTypes.SHIPPING_METHODS, DescriptorGroups.estore.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes", "50" },

                { "Add Localized descriptor for Promotion for IPP group ", DescriptorEntityTypes.PROMOTION,
                        DescriptorGroups.ipp.toString(), "AUTO TEST DESCRIPTOR", "Yes", "50" } };
    }

    /**
     * Dataprovider for valid data without any max length
     */
    @DataProvider(name = "getDataWithNoMaxLength")
    public Object[][] getDataWithNoMaxLength() {
        return new Object[][] {
                { "Add Localized descriptor Without MaxLength for Subscription plan for Estore group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, DescriptorGroups.estore.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes" },
                { "Add Localized descriptor Without MaxLength for Subscription plan for IPP group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, DescriptorGroups.ipp.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes" },

                { "Add Localized descriptor Without MaxLength for Subscription Offer for Estore group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, DescriptorGroups.estore.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes" },
                { "Add Localized descriptor Without MaxLength for Subscription Offer for IPP group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, DescriptorGroups.ipp.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes" },

                { "Add Localized descriptor Without MaxLength for Promotion for Estore group ",
                        DescriptorEntityTypes.PROMOTION, DescriptorGroups.estore.toString(), "AUTO TEST DESCRIPTOR",
                        "Yes" },
                { "Add Localized descriptor Without MaxLength for Promotion for IPP group ",
                        DescriptorEntityTypes.PROMOTION, DescriptorGroups.ipp.toString(), "AUTO TEST DESCRIPTOR",
                        "Yes" }, };
    }

    /**
     * Dataprovider for valid data with groupname as empty
     */
    @DataProvider(name = "getDataWithNoGroupName")
    public Object[][] getDataWithNoGroupName() {
        return new Object[][] {
                { "Add Localized descriptor Without MaxLength for Subscription plan without specific group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, "", "AUTO TEST DESCRIPTOR", "Yes", "" },
                { "Add Localized descriptor Without MaxLength for Subscription Offer without Specific group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, "", "AUTO TEST DESCRIPTOR", "Yes", "" },
                { "Add Localized descriptor Without MaxLength for Promotion without Specific group ",
                        DescriptorEntityTypes.PROMOTION, "", "AUTO TEST DESCRIPTOR", "Yes", "" },

                { "Add Non Localized descriptor for Subscription plan without specific group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, "", "AUTO TEST DESCRIPTOR", "No", "50" },

                { "Add Localized descriptor for Subscription Offer without specific group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, "", "AUTO TEST DESCRIPTOR", "Yes", "50" },

                { "Add Localized descriptor for Promotion without specific group ", DescriptorEntityTypes.PROMOTION, "",
                        "AUTO TEST DESCRIPTOR", "Yes", "50" } };
    }

    /**
     * Dataprovider for valid data with groupname as Other
     */
    @DataProvider(name = "getDataWithOtherGroupName")
    public Object[][] getDataWithOtherGroupName() {
        return new Object[][] {
                { "Add Localized descriptor Without MaxLength for Subscription plan for Other group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, DescriptorGroups.Other.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes", "" },
                { "Add Localized descriptor Without MaxLength for Subscription Offer for Other group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, DescriptorGroups.Other.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes", "" },

                { "Add Localized descriptor Without MaxLength for Promotion for Other group ",
                        DescriptorEntityTypes.PROMOTION, DescriptorGroups.Other.toString(), "AUTO TEST DESCRIPTOR",
                        "Yes", "" },

                { "Add Non Localized descriptor for Subscription plan for Other group ",
                        DescriptorEntityTypes.SUBSCRIPTION_PLAN, DescriptorGroups.Other.toString(),
                        "AUTO TEST DESCRIPTOR", "No", "50" },

                { "Add Localized descriptor for Subscription Offer for Other group ",
                        DescriptorEntityTypes.SUBSCRIPTION_OFFER, DescriptorGroups.Other.toString(),
                        "AUTO TEST DESCRIPTOR", "Yes", "50" },

                { "Add Non Localized descriptor for Promotion for Other group ", DescriptorEntityTypes.PROMOTION,
                        DescriptorGroups.Other.toString(), "AUTO TEST DESCRIPTOR", "No", "50" } };
    }

    /**
     * Just another helper method in test to create a Descriptor object with all the values sent by each test
     */
    private Descriptor createDescriptorObject(final DescriptorEntityTypes entityType, final String groupName,
        final String fieldName, final String apiName, final String localized, final String maxLength) {
        final Descriptor descriptorObj = new Descriptor();
        descriptorObj.setEntity(entityType);
        descriptorObj.setFieldName(fieldName);
        descriptorObj.setApiName(apiName);
        descriptorObj.setLocalized(localized);
        descriptorObj.setMaxLength(maxLength);
        descriptorObj.setGroupName(groupName);
        if (groupName.equalsIgnoreCase(DescriptorGroups.Other.toString())) {
            descriptorObj.setOtherGroupName(OTHER_GROUP);
        }
        return descriptorObj;
    }

}
