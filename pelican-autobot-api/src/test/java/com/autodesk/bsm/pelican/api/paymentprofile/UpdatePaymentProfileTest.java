package com.autodesk.bsm.pelican.api.paymentprofile;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.TokenPayment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.CreditCard;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.DirectDebitPayment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentMethod;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class to test Update Payment Profile API
 *
 * @author Shweta Hegde
 */
public class UpdatePaymentProfileTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PaymentProfileUtils paymentProfileUtils;
    private String userId;
    private String userExternalKey;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isFeatureFlagChanged;

    /**
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminTool = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminTool.login();

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        paymentProfileUtils =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final Map<String, String> userParams = new HashMap<>();
        userExternalKey = RandomStringUtils.randomAlphanumeric(8);
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        userId = userUtils.createPelicanUser(userParams, getEnvironmentVariables()).getId();
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
     * Test to update few fields of Credit Card. Step1: Add a credit card payment profile. Step2: Update fields of
     * Credit Card. Step3: Get Payment Profile and verify that fields are updated.
     */
    @Test
    public void testUpdateCreditCardPaymentProfile() {

        final PaymentProfile paymentProfile =
            paymentProfileUtils.addCreditCardPaymentProfile(userId, Payment.PaymentProcessor.BLUESNAP_NAMER.getValue());

        final PaymentProfile updatePaymentProfile = new PaymentProfile();
        final CreditCard creditCard = new CreditCard();
        creditCard.setExpirationDate("1225");
        creditCard.setStreetAddress("4567 Main Street");
        creditCard.setCity("Las Vegas");
        creditCard.setState("NV");
        creditCard.setZipCode("81818");
        updatePaymentProfile.setCreditCard(creditCard);

        resource.paymentProfile().update(updatePaymentProfile, paymentProfile.getId());
        // Update Payment Profile does not return any payload in response, so have to call Get Payment Profile
        final PaymentProfile updatedPaymentProfile =
            resource.paymentProfile().getPaymentProfile(paymentProfile.getId());

        AssertCollector.assertThat("Incorrect expiration date",
            updatedPaymentProfile.getCreditCard().getExpirationDate(), equalTo("1225"), assertionErrorList);
        AssertCollector.assertThat("Incorrect street address", updatedPaymentProfile.getCreditCard().getStreetAddress(),
            equalTo("4567 Main Street"), assertionErrorList);
        AssertCollector.assertThat("Incorrect city", updatedPaymentProfile.getCreditCard().getCity(),
            equalTo("Las Vegas"), assertionErrorList);
        AssertCollector.assertThat("Incorrect state", updatedPaymentProfile.getCreditCard().getState(), equalTo("NV"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect zip code", updatedPaymentProfile.getCreditCard().getZipCode(),
            equalTo("81818"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test to update few fields of Paypal. Step1: Add a Paypal payment profile. Step2: Update fields of Paypal. Step3:
     * Get Payment Profile and verify that fields are updated.
     */
    @Test
    public void testUpdatePaypalPaymentProfile() {

        final PaymentProfile paymentProfile =
            paymentProfileUtils.addPaypalPaymentProfile(userId, Payment.PaymentProcessor.PAYPAL_EMEA.getValue());

        final PaymentProfile updatePaymentProfile = new PaymentProfile();
        final TokenPayment tokenPayment = new TokenPayment();
        tokenPayment.setBillToStreetAddress("123 College Street");
        tokenPayment.setCity("Toranto");
        tokenPayment.setStateProvince("Ontario");
        tokenPayment.setCountryCode("CA");
        tokenPayment.setZipCode("65432");
        updatePaymentProfile.setTokenPayment(tokenPayment);

        resource.paymentProfile().update(updatePaymentProfile, paymentProfile.getId());
        // Update Payment Profile does not return any payload in response, so have to call Get Payment Profile
        final PaymentProfile updatedPaymentProfile =
            resource.paymentProfile().getPaymentProfile(paymentProfile.getId());

        AssertCollector.assertThat("Incorrect street address",
            updatedPaymentProfile.getTokenPayment().getBillToStreetAddress(), equalTo("123 College Street"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect city", updatedPaymentProfile.getTokenPayment().getCity(),
            equalTo("Toranto"), assertionErrorList);
        AssertCollector.assertThat("Incorrect state province",
            updatedPaymentProfile.getTokenPayment().getStateProvince(), equalTo("Ontario"), assertionErrorList);
        AssertCollector.assertThat("Incorrect country code", updatedPaymentProfile.getTokenPayment().getCountryCode(),
            equalTo("CA"), assertionErrorList);
        AssertCollector.assertThat("Incorrect zip code", updatedPaymentProfile.getTokenPayment().getZipCode(),
            equalTo("65432"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to validate update payment profile for ACH. 1) Not allowed fields to update : paymentMethod 2)
     * Allowed fields to update : accountType, routingNumber, routingNumber-last-four, stateProvince, accountNickname,
     * billToStreetAddress, accountNumber, accountNumber-last-four.
     */
    @Test
    public void testSuccessUpdateACHDirectDebitPaymentProfile() {
        final PaymentProfile directDebitPayment = paymentProfileUtils.addDirectDebitPayment(userId,
            Payment.PaymentProcessor.BLUESNAP_NAMER.getValue(), PaymentMethod.ACH, false);

        final PaymentProfile updatePaymentProfile = new PaymentProfile();
        final DirectDebitPayment directDebit = new DirectDebitPayment();
        directDebit.setAccountType("CHECKING");
        directDebit.setRoutingNumber("1234567");
        directDebit.setRoutingNumberLastFour("4567");
        directDebit.setAccountNumber("1234567890");
        directDebit.setAccountNumberLastFour("7890");
        directDebit.setState("CA");
        directDebit.setCompanyName("Autodesk");
        directDebit.setAccountNickname("My account");
        updatePaymentProfile.setDirectDebitPayment(directDebit);
        resource.paymentProfile().update(updatePaymentProfile, directDebitPayment.getId());

        final PaymentProfile updatedPaymentProfile =
            resource.paymentProfile().getPaymentProfile(directDebitPayment.getId());
        AssertCollector.assertThat("Incorrect Account Type",
            updatedPaymentProfile.getDirectDebitPayment().getAccountType(), equalTo("CHECKING"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Routing Number",
            updatedPaymentProfile.getDirectDebitPayment().getRoutingNumber(), equalTo("1234567"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Routing Number  Last Four",
            updatedPaymentProfile.getDirectDebitPayment().getRoutingNumberLastFour(), equalTo("4567"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect state province ",
            updatedPaymentProfile.getDirectDebitPayment().getState(), equalTo("CA"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Account Nick name ",
            updatedPaymentProfile.getDirectDebitPayment().getAccountNickname(), equalTo("My account"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Company Name ",
            updatedPaymentProfile.getDirectDebitPayment().getCompanyName(), equalTo("Autodesk"), assertionErrorList);
        AssertCollector.assertThat("Incorrect account number ",
            updatedPaymentProfile.getDirectDebitPayment().getAccountNumber(), equalTo("1234567890"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Account last four",
            updatedPaymentProfile.getDirectDebitPayment().getAccountNumberLastFour(), equalTo("7890"),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test method to validate update payment profile for SEPA. 1) Not allowed fields to update :paymentMethod 2)
     * Allowed fields to update : accountType, accountNickname, billToStreetAddress,iban,iban-first-four,
     * iban-last-four.
     */
    @Test
    public void testSuccessUpdateSEPADirectDebitPaymentProfile() {
        final PaymentProfile directDebitPayment = paymentProfileUtils.addDirectDebitPayment(userId,
            Payment.PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentMethod.SEPA, false);

        final PaymentProfile updatePaymentProfile = new PaymentProfile();
        final DirectDebitPayment directDebit = new DirectDebitPayment();
        directDebit.setAccountType("SAVING");
        directDebit.setState("CA");
        directDebit.setAccountNickname("My account");
        directDebit.setIban("3452145789");
        directDebit.setIbanFirstFour("3452");
        directDebit.setIbanLastFour("5789");
        updatePaymentProfile.setDirectDebitPayment(directDebit);
        resource.paymentProfile().update(updatePaymentProfile, directDebitPayment.getId());

        final PaymentProfile updatedPaymentProfile =
            resource.paymentProfile().getPaymentProfile(directDebitPayment.getId());
        AssertCollector.assertThat("Incorrect Account Type",
            updatedPaymentProfile.getDirectDebitPayment().getAccountType(), equalTo("SAVING"), assertionErrorList);
        AssertCollector.assertThat("Incorrect state province ",
            updatedPaymentProfile.getDirectDebitPayment().getState(), equalTo("CA"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Account Nick name ",
            updatedPaymentProfile.getDirectDebitPayment().getAccountNickname(), equalTo("My account"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Iban Number ", updatedPaymentProfile.getDirectDebitPayment().getIban(),
            equalTo("3452145789"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Iban first four",
            updatedPaymentProfile.getDirectDebitPayment().getIbanFirstFour(), equalTo("3452"), assertionErrorList);
        AssertCollector.assertThat("Incorrect Iban Last four",
            updatedPaymentProfile.getDirectDebitPayment().getIbanLastFour(), equalTo("5789"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
