package com.autodesk.bsm.pelican.ui.bankingconfig;

import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertySetPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.FindBankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PelicanEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This Class provides access to retrieve the Banking properties from Admin tool Banking Properties/SHow Hives should
 * only display the environmentVariables related properties Note: Is this 1st User Story towards SOX Audit
 *
 * @author mandas
 */

public class ShowHivesTest extends SeleniumWebdriver {

    private FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage;
    private final List<String> showHivesEnvagnosticValues = new ArrayList<>();
    private List<String> showHivesKeyColumnValues = new ArrayList<>();
    private final List<String> showHivesSecureKeyColumnValues = new ArrayList<>();
    private final List<String> showHivesNonSecureKeyColumnValues = new ArrayList<>();
    private final List<String> showHivesSecureValueColumnValues = new ArrayList<>();
    private final List<String> showHivesEnvironmentValues = new ArrayList<>();
    private String expectedENV;
    private final List<String> wrongEnvironments = new ArrayList<>();
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowHivesTest.class.getSimpleName());

    /**
     * Setup method will perform below, Login to Admin Tool navigate to Applications -> Banking Properties -> Show Hives
     * page click on show Hives button And read the pages for the Keys
     */
    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findBankingConfigurationPropertiesPage = adminToolPage.getPage(FindBankingConfigurationPropertiesPage.class);
        findBankingConfigurationPropertiesPage.showHives();
        final GenericGrid showHives = adminToolPage.getPage(GenericGrid.class);
        showHivesKeyColumnValues = showHives.getColumnValues("Key");
        final List<String> showHivesValueColumnValues = showHives.getColumnValues("Value");

        int index = 0;
        for (final String showHivesKeyValue : showHivesKeyColumnValues) {

            if (showHivesKeyValue.contains("DEV") || showHivesKeyValue.contains("STG")
                || showHivesKeyValue.contains("PRD")) {
                showHivesEnvironmentValues.add(showHivesKeyValue);
            } else {
                showHivesEnvagnosticValues.add(showHivesKeyValue);
            }

            if (showHivesKeyValue.contains("_PASSWORD") || showHivesKeyValue.contains("_PWD")
                || showHivesKeyValue.contains("_SECRET") || showHivesKeyValue.contains("_SECURITY_TOKEN")
                || showHivesKeyValue.contains("CONSUMER_KEY") || showHivesKeyValue.contains("_PASS")) {

                showHivesSecureValueColumnValues.add(showHivesValueColumnValues.get(index));
                showHivesSecureKeyColumnValues.add(showHivesKeyValue);
            } else {
                showHivesNonSecureKeyColumnValues.add(showHivesKeyValue);
            }
            index++;
        }

        final String environment = PelicanEnvironment.getEnvironment();
        if (environment.contains("dev")) {
            expectedENV = "DEV.";
            wrongEnvironments.add("STG.");
            wrongEnvironments.add("PRD.");
        } else if (environment.contains("stg")) {
            expectedENV = "STG.";
            wrongEnvironments.add("DEV.");
            wrongEnvironments.add("PRD.");
        } else if (environment.contains("prd")) {
            expectedENV = "PRD.";
            wrongEnvironments.add("STG.");
            wrongEnvironments.add("DEV.");
        }
    }

    /**
     * This Testcase is to verify showHivesEnvironmentValues contain expected environment specific variables ONLY
     */
    @Test
    public void testExpectedEnvironmentVariables() {

        for (final String showHivesEnvironmentValue : showHivesEnvironmentValues) {
            if (!(showHivesEnvironmentValue.contains(expectedENV))) {
                Assert.fail(showHivesEnvironmentValue
                    + ": Found Unexpected Environment Key in Show Hives, Failing Test case here");
            }
        }
        LOGGER.info("Actual Environment variables match the expected Environment Variables");
    }

    /**
     * This Testcase is to verify showHivesEnvironmentValues doesnt contain unexpected environment specific variables
     */
    @Test
    public void testUnexpectedEnvironmentVariables() {

        for (final String showHivesEnvironmentValue : showHivesEnvironmentValues) {
            if (showHivesEnvironmentValue.contains(wrongEnvironments.get(0))
                || showHivesEnvironmentValue.contains(wrongEnvironments.get(1))) {
                Assert.fail(showHivesEnvironmentValue
                    + ": Found Unexpected Environment Key in Show Hives, Failing Test case here");
            }
        }
        LOGGER.info("Unexpected Environment variables are not part of the Environment variables");
    }

    /**
     * This Test case is to verify showHivesEnvagnosticValues contain other important Envagnostic variables
     */
    @Test
    public void testExpectedEnvagnosticEnvironmentVariables() {
        int totalEmailKeys = 0;
        int totalTriggerKeys = 0;
        int totalS3Keys = 0;
        for (final String showHivesEnvagnosticValue : showHivesEnvagnosticValues) {
            if (showHivesEnvagnosticValue.startsWith("EMAIL_")) {
                totalEmailKeys++;
            } else if (showHivesEnvagnosticValue.startsWith("TRIGGERS_")) {
                totalTriggerKeys++;
            } else if (showHivesEnvagnosticValue.equals("S3_CSV_FILES_BUCKET")) {
                totalS3Keys++;
            }
        }

        if (!(totalEmailKeys > 15 && totalTriggerKeys > 1 && totalS3Keys == 1)) {
            Assert.fail(
                "Expected Envagnostic Environment Keys did not match the expetcted count, Failing Test case here");
        } else {
            LOGGER.info("Envagnostic match the expected Variables");
        }
    }

    @Test
    public void testPasswordsAreStarsInAdminTool() {

        for (final String showHivesSecureValueColumnValue : showHivesSecureValueColumnValues) {
            if (!(showHivesSecureValueColumnValue.equalsIgnoreCase("********"))) {
                Assert.fail("Value for one of the Secure Key is not Stars");
            }

        }
    }

    @Test
    public void testDBForIsCredentialForPasswords() {

        System.out.println(showHivesSecureKeyColumnValues);
        if (!(showHivesSecureKeyColumnValues.isEmpty())) {
            if (!(DbUtils.isPasswordFlaggedInMidas(showHivesSecureKeyColumnValues, getEnvironmentVariables()))) {
                Assert.fail("DB: Flag for one of the secure Keys is not Set");
            } else {
                LOGGER.info("DB: Flag for all the Secure Keys are Set to 1");
            }
        } else {
            Assert.fail("List of Secure Keys is Empty");
        }

    }

    @Test
    public void testDeletePropertyIsRemoved() {

        bankingConfigurationPropertiesPage = findBankingConfigurationPropertiesPage.showHives();

        if (!(bankingConfigurationPropertiesPage.isRemovePropertyButtonVisible())) {
            LOGGER.info("Remove Property Button is not Visible");
        } else {
            Assert.fail("BUG: Remove Property Button is Visible");
        }

    }

    @Test
    public void testAddPropertyIsRemoved() {

        bankingConfigurationPropertiesPage = findBankingConfigurationPropertiesPage.showHives();

        if (!(bankingConfigurationPropertiesPage.isAddPropertyButtonVisible())) {
            LOGGER.info("Add Property Button is not Visible");
        } else {
            Assert.fail("BUG: Add Property Button is Visible");
        }
    }

    @Test
    public void testSetPropertyIsRemovedFromConfigurationsDropdown() {

        bankingConfigurationPropertiesPage = findBankingConfigurationPropertiesPage.showHives();

        if (!(bankingConfigurationPropertiesPage.isSetPropertyVisibleInConfigurationDropdown())) {
            LOGGER.info("Set Property option under configurations dropdown is not Visible");
        } else {
            Assert.fail("Set Property option under configurations dropdown is Visible");
        }
    }

    @Test
    public void testKeyIsNotEditableInEditPage() {

        bankingConfigurationPropertiesPage = findBankingConfigurationPropertiesPage.showHives();

        for (final String showHivesKeyColumnValue : showHivesKeyColumnValues) {
            final BankingConfigurationPropertySetPage bankingConfigurationPropertySetPage =
                bankingConfigurationPropertiesPage.editProperties(showHivesKeyColumnValue);
            if (!(bankingConfigurationPropertySetPage.isKeyDisplayed())) {
                Assert.fail("Class WebElement \"Value\" is Not Found. i.e., Key field is editable");
            }
        }
    }

    @Test
    public void testPasswordsAreStarsInEditPage() {

        bankingConfigurationPropertiesPage = findBankingConfigurationPropertiesPage.showHives();

        for (final String showHivesSecureKeyColumnValue : showHivesSecureKeyColumnValues) {
            final BankingConfigurationPropertySetPage bankingConfigurationPropertySetPage =
                bankingConfigurationPropertiesPage.editProperties(showHivesSecureKeyColumnValue);
            if (!(bankingConfigurationPropertySetPage.getSecretFieldAttribute().equalsIgnoreCase("password"))) {
                Assert.fail("Secret Field is NOT Password protected");
            }
        }

    }

    @Test
    public void testNonPasswordsAreNotStarsInEditPage() {

        bankingConfigurationPropertiesPage = findBankingConfigurationPropertiesPage.showHives();
        for (final String showHivesNonSecureKeyColumnValue : showHivesNonSecureKeyColumnValues) {
            final BankingConfigurationPropertySetPage bankingConfigurationPropertySetPage =
                bankingConfigurationPropertiesPage.editProperties(showHivesNonSecureKeyColumnValue);
            if (!(bankingConfigurationPropertySetPage.getSecretFieldAttribute().equalsIgnoreCase("text"))) {
                Assert.fail("Non Secret Field is Password protected");
            }
        }

    }
}
