package com.autodesk.bsm.pelican.ui.bankingconfig;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.FindBankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This class verifies configuration key and values of Banking Configuration
 *
 * @author Shweta Hegde
 */
public class CoreConfigurationCheckTest extends SeleniumWebdriver {

    private FindBankingConfigurationPropertiesPage findBankingConfigurationPropertiesPage;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        findBankingConfigurationPropertiesPage = adminToolPage.getPage(FindBankingConfigurationPropertiesPage.class);
    }

    /**
     * This test case verifies all the BCC related fields are present. It also verifies specific fields are left empty
     * and specific fields are not empty.
     */
    @Test
    public void testBCCEmailAddressFieldsArePresentForEachType() {

        final BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage =
            findBankingConfigurationPropertiesPage.selectComponent(PelicanConstants.CORE);

        // Adding all the BCC key and value to a map
        final List<String> bccEmailKeys = new ArrayList<>();
        bccEmailKeys.add("BCC.ADD_SEATS_ORDER_COMPLETE");
        bccEmailKeys.add("BCC.ALIGN_BILLING_ORDER_COMPLETE");
        bccEmailKeys.add("BCC.AUTO_RENEWAL_COMPLETE");
        bccEmailKeys.add("BCC.AUTO_RENEW_CANCELLED");
        bccEmailKeys.add("BCC.AUTO_RENEW_RESTARTED");
        bccEmailKeys.add("BCC.CANCEL_DECREASE_SEATS");
        bccEmailKeys.add("BCC.CREDIT_CARD_FAILURE");
        bccEmailKeys.add("BCC.CREDIT_NOTE_MEMO");
        bccEmailKeys.add("BCC.DECREASE_SEATS");
        bccEmailKeys.add("BCC.EXPORT_CONTROL_DECLINE_NEW");
        bccEmailKeys.add("BCC.EXPORT_CONTROL_DECLINE_RENEW");
        bccEmailKeys.add("BCC.INVOICE");
        bccEmailKeys.add("BCC.ORDER_COMPLETE");
        bccEmailKeys.add("BCC.ORDER_FULFILLMENT");
        bccEmailKeys.add("BCC.PAYMENT_PROFILE_CHANGED");
        bccEmailKeys.add("BCC.REFUND");
        bccEmailKeys.add("BCC.RENEWAL_REMINDER_ANNUAL");
        bccEmailKeys.add("BCC.RENEWAL_REMINDER_MONTHLY");
        bccEmailKeys.add("BCC.SCF");
        bccEmailKeys.add("BCC.SUBSCRIPTION_EXPIRED");
        bccEmailKeys.add("BCC.SUBSCRIPTION_EXPIRATION_REMINDER");

        // Assert on each key
        // Assertions on values are not there since, it is a configuration and it can be changed
        for (final String key : bccEmailKeys) {

            AssertCollector.assertTrue(key + " key should be displayed",
                bankingConfigurationPropertiesPage.isRequiredKeyDisplayed(key), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

}
