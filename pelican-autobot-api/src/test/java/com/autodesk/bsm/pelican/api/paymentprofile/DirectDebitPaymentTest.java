package com.autodesk.bsm.pelican.api.paymentprofile;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.DirectDebitPayment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentMethod;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanErrorConstants;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * This class to verify Direct Debit payment functionality for ACH & SEPA.
 *
 * @author t_joshv
 *
 */
public class DirectDebitPaymentTest extends SeleniumWebdriver {

    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged;
    private PaymentProfileUtils paymentProfileUtils;
    private UserUtils userUtils;
    private PelicanPlatform resource;

    @BeforeClass()
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminTool = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminTool.login();
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        userUtils = new UserUtils();
        paymentProfileUtils =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        bankingConfigurationPropertiesPage = adminTool.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG, true);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG,
                false);
        }
    }

    /**
     * Test Direct Debit Payment functionality should not be available with FF OFF.
     */
    @Test
    public void testErrorAddDirectDebitPaymentProfileWithFFOff() {
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG, false);
        // Get a buyer user.
        final BuyerUser buyerUser = getBuyerUserForDirectDebit();
        final HttpError httpError = paymentProfileUtils.addDirectDebitPayment(buyerUser.getId(),
            PaymentProcessor.BLUESNAP_NAMER.getValue(), PaymentMethod.ACH, false);
        AssertCollector.assertThat("Incorrect Response ", httpError.getStatus(), equalTo(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Error Message on FF Off", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.UNSUPPORTED_PAYMENT_METHOD), assertionErrorList);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.DIRECT_DEBIT_ENABLED_FEATURE_FLAG, true);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test add ACH Direct Debit payment successfully.Step 1: get buyer user. step 2: add direct debit payment profile
     * for buyer user. Step 3 : assertion on fields present, there is no validations on Value of those fields.Step 4:
     * Delete added payment profile as tear down.
     */
    @Test
    public void testSuccessAddACHDirectDebitPaymentProfile() {

        // Get a buyer user to add payment profile for.
        final BuyerUser buyerUser = getBuyerUserForDirectDebit();

        // Add Payment profile
        final PaymentProfile paymentProfile = paymentProfileUtils.addDirectDebitPayment(buyerUser.getId(),
            PaymentProcessor.BLUESNAP_NAMER.getValue(), PaymentMethod.ACH, true);
        AssertCollector.assertThat("Payment Profile Id should be created", paymentProfile.getId(), notNullValue(),
            assertionErrorList);

        // Assertion on response body.
        final DirectDebitPayment directDebitPaymentProfile = paymentProfile.getDirectDebitPayment();
        AssertCollector.assertThat("Incorrect Payment Method", directDebitPaymentProfile.getPaymentMethod(),
            equalTo(PaymentMethod.ACH.getValue()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Account Type ", directDebitPaymentProfile.getAccountType(),
            equalTo("CHECKING"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Account Number ", directDebitPaymentProfile.getAccountNumber(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Account Number Last Four Digit ",
            directDebitPaymentProfile.getAccountNumberLastFour(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Routing Number ", directDebitPaymentProfile.getRoutingNumber(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect Routing Number Last Four Digit",
            directDebitPaymentProfile.getRoutingNumberLastFour(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect First Name", directDebitPaymentProfile.getFirstName(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Surname", directDebitPaymentProfile.getSurname(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Street Address", directDebitPaymentProfile.getStreetAddress(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Incorrect city", directDebitPaymentProfile.getCity(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect country code", directDebitPaymentProfile.getCountryCode(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect state", directDebitPaymentProfile.getState(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect zip code", directDebitPaymentProfile.getZipCode(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Company Name ", directDebitPaymentProfile.getCompanyName(),
            notNullValue(), assertionErrorList);

        // Delete the payment profile
        final boolean isProfileGotDeleted = resource.paymentProfile().deletePaymentProfile(paymentProfile.getId());
        AssertCollector.assertTrue("Failed to Delete Payment Profile:", isProfileGotDeleted, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to add SEPA direct debit payment profile successfully. Step 1 : get a buyer user. Step 2: add SEPA direct
     * payment profile. Step 3 : assert to have mandatory fields , no validation on field values. Step 4 : Delete added
     * profile as tear down.
     */
    @Test
    public void testSuccessAddSEPADirectDebitPaymentProfile() {
        // Get a buyer user to add payment profile for.
        final BuyerUser buyerUser = getBuyerUserForDirectDebit();

        // Add Payment profile
        final PaymentProfile paymentProfile = paymentProfileUtils.addDirectDebitPayment(buyerUser.getId(),
            PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentMethod.SEPA, false);

        AssertCollector.assertThat("Payment Profile Id should be created", paymentProfile.getId(), notNullValue(),
            assertionErrorList);

        // Assertion on response body.
        final DirectDebitPayment directDebitPaymentProfile = paymentProfile.getDirectDebitPayment();
        AssertCollector.assertThat("Incorrect Payment Method", directDebitPaymentProfile.getPaymentMethod(),
            equalTo(PaymentMethod.SEPA.getValue()), assertionErrorList);
        AssertCollector.assertThat("iban number should not be null", directDebitPaymentProfile.getIban(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("iban first four number should not be null ",
            directDebitPaymentProfile.getIbanFirstFour(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("iban last four number should not be null ",
            directDebitPaymentProfile.getIbanLastFour(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("First Name should not be null", directDebitPaymentProfile.getFirstName(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Surname should not be null", directDebitPaymentProfile.getSurname(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Street Address should not be null", directDebitPaymentProfile.getStreetAddress(),
            notNullValue(), assertionErrorList);
        AssertCollector.assertThat("city should not be null", directDebitPaymentProfile.getCity(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("country code should not be null", directDebitPaymentProfile.getCountryCode(),
            notNullValue(), assertionErrorList);

        // Delete the payment profile
        final boolean isProfileGotDeleted = resource.paymentProfile().deletePaymentProfile(paymentProfile.getId());
        AssertCollector.assertTrue("Failed to Delete Payment Profile:", isProfileGotDeleted, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to validate error on missing mandatory fields while adding payment profile. AccountNickname, StateProvince
     * and zipcode are non mandatory fields. It validates and throws an error as per the order of validation.
     */
    @Test
    public void testErrorOnMissingMandatoryFieldForDirectDebit() {
        // Get a buyer user to add payment profile for.
        final BuyerUser buyerUser = getBuyerUserForDirectDebit();

        final PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setAppFamilyId(getEnvironmentVariables().getAppFamilyId());
        paymentProfile.setUserExternalKey(buyerUser.getExternalKey());
        paymentProfile.setpaymentProcessor(PaymentProcessor.BLUESNAP_EMEA.getValue());
        final DirectDebitPayment directDebitPayment = new DirectDebitPayment();
        directDebitPayment.setPaymentMethod(PaymentMethod.SEPA.getValue());
        directDebitPayment.setIbanFirstFour(RandomStringUtils.randomNumeric(4));
        directDebitPayment.setIbanLastFour(RandomStringUtils.randomNumeric(4));
        paymentProfile.setDirectDebitPayment(directDebitPayment);

        // Add Payment profile
        final HttpError httpError = resource.paymentProfile().add(paymentProfile);
        AssertCollector.assertThat("Incorrect Response ", httpError.getStatus(), equalTo(HttpStatus.SC_BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect validation error message ", httpError.getErrorMessage(),
            equalTo(PelicanErrorConstants.VALIDATION_ERROR_MSG), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Helper Method to get buyer user for Direct Debit Payment.
     *
     * @return BuyerUser
     */
    private BuyerUser getBuyerUserForDirectDebit() {
        // create a buyer user to add payment profile for.
        final HashMap<String, String> userMap = new HashMap<>();
        userMap.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userMap.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));
        final User user = userUtils.createPelicanUser(userMap, getEnvironmentVariables());

        final BuyerUser buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setExternalKey(user.getExternalKey());
        return buyerUser;
    }
}
