package com.autodesk.bsm.pelican.api.paymentprofile;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.TokenPayment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Paypal Payment Profile Test in Admin Tool. Some of the test data was directly created on fly. Hardcoded the values of
 * user name - "Automation_Test_buyer - Find the existing user, if not found create a new user. Hardcoded the values of
 * familyName - AUTO_FAMILY
 *
 * @author Vineel.
 */

public class PaypalPaymentProfileTest extends BaseTestData {

    private PelicanPlatform resource;
    private TokenPayment tokenPayment = null;

    /*
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * Verify whether Paypal Payment Profile has been created successfully or not.
     *
     * @result validates the Payment Method type, Paypal Token, FirstName, MiddleName, LastName and some other fields.
     */
    @Test
    public void addPaypalPaymentProfile() {
        // Add a payment Profile
        final PaymentProfile paymentProfile = addPaymentProfile();
        // Return the id from the payment profile.
        AssertCollector.assertThat("Incorrect Method Type", paymentProfile.getTokenPayment().getPaymentMethod(),
            equalTo(tokenPayment.getPaymentMethod()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Paypal Token", paymentProfile.getTokenPayment().getToken(),
            equalTo(tokenPayment.getToken()), assertionErrorList);
        AssertCollector.assertThat("Incorrect FirstName", paymentProfile.getTokenPayment().getFirstName(),
            equalTo(tokenPayment.getFirstName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect middlename", paymentProfile.getTokenPayment().getMiddleName(),
            equalTo(tokenPayment.getMiddleName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Surname ", paymentProfile.getTokenPayment().getSurname(),
            equalTo(tokenPayment.getSurname()), assertionErrorList);
        AssertCollector.assertThat("Incorrect BillToStreet Address",
            paymentProfile.getTokenPayment().getBillToStreetAddress(), equalTo(tokenPayment.getBillToStreetAddress()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect BillToZipCode", paymentProfile.getTokenPayment().getZipCode(),
            equalTo(tokenPayment.getZipCode()), assertionErrorList);
        AssertCollector.assertThat("Incorrect City", paymentProfile.getTokenPayment().getCity(),
            equalTo(tokenPayment.getCity()), assertionErrorList);
        AssertCollector.assertThat("Incorrect State Province", paymentProfile.getTokenPayment().getStateProvince(),
            equalTo(tokenPayment.getStateProvince()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Country Code", paymentProfile.getTokenPayment().getCountryCode(),
            equalTo(tokenPayment.getCountryCode()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Phone number", paymentProfile.getTokenPayment().getPhoneNumber(),
            equalTo(tokenPayment.getPhoneNumber()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Create new payment profile
     *
     * @return token
     */
    private PaymentProfile addPaymentProfile() {

        // Creates the Token Payment Object used in the payment profile.
        tokenPayment = getTokenPayment();
        final PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setUserId(getUser().getId());
        paymentProfile.setTokenPayment(tokenPayment);

        return resource.paymentProfile().add(paymentProfile);
    }

    /**
     * @return token payment
     */
    private TokenPayment getTokenPayment() {

        final TokenPayment tokenPayment = new TokenPayment();
        tokenPayment.setPaymentMethod(PaymentType.PAYPAL.getValue());
        tokenPayment.setFirstName("Jar-Jar");
        tokenPayment.setMiddleName("John");
        tokenPayment.setSurname("Binks");
        tokenPayment.setBillToStreetAddress("580 College Ave");
        tokenPayment.setZipCode("94036");
        tokenPayment.setCity("Los Angeles");
        tokenPayment.setStateProvince("CA");
        tokenPayment.setCountryCode("US");
        tokenPayment.setPhoneNumber("6501235687");
        tokenPayment.setToken("1234567890123443");

        return tokenPayment;
    }
}
