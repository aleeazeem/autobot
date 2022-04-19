package com.autodesk.bsm.pelican.api.paymentprofile;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.PurchaseOrdersClient.PurchaseOrderParameter;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfiles;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Class to validate Find Payment Profiles API with user id, user External Key, Credit Card Last 4 Digit and
 * payment processor as input.
 *
 * @author t_joshv
 */

public class GetPaymentProfilesTest extends BaseTestData {

    private PelicanPlatform resource;
    private PaymentProfileUtils paymentProfileUtils;

    /*
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        paymentProfileUtils =
            new PaymentProfileUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        resource = new PelicanClient(getEnvironmentVariables()).platform();
    }

    /**
     * Test Method to verify find payment profiles return result based on filter value for user ExternalKey and Last
     * Four Digit of card.
     */
    @Test
    public void testSuccessGetPaymentProfilesForBluesnapPaymentProcessor() {

        // Add a user , Set the request.
        final Map<String, String> userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), RandomStringUtils.randomAlphabetic(8));
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));

        final User user = resource.user().addUser(userRequestParam);
        final String userId = user.getId();
        final String userExternalKey = user.getExternalKey();
        final String last4DigitOfCreditCard = "9818";

        paymentProfileUtils.addCreditCardPaymentProfile(userId, PaymentProcessor.BLUESNAP_NAMER.getValue());

        // request map for find payment profiles.
        final Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(PurchaseOrderParameter.SKIP_COUNT.getName(), "true");
        paramsMap.put("userExternalKey", userExternalKey);
        paramsMap.put("accountLast4", last4DigitOfCreditCard);

        // get Payment profiles.
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        final PaymentProfile paymentProfile = paymentProfiles.getPaymentProfile().get(0);

        // Assert on User Id(Response does not include External key hence asserting Id) , credit card number and Payment
        // Processor.

        AssertCollector.assertThat("Incorrect User Id", userId, equalTo(paymentProfile.getUserId()),
            assertionErrorList);
        AssertCollector.assertThat("Resultant Record should not be more than one",
            paymentProfiles.getPaymentProfile().size(), is(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Payment Processor", PaymentProcessor.BLUESNAP_NAMER.getValue(),
            equalTo(paymentProfiles.getPaymentProfile().get(0).getpaymentProcessor()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Last 4 Digit Of Credit Card", last4DigitOfCreditCard,
            equalTo(paymentProfiles.getPaymentProfile().get(0).getCreditCard().getCreditCardNumber()),
            assertionErrorList);

        final boolean isProfileGotDeleted = resource.paymentProfile().deletePaymentProfile(paymentProfile.getId());
        AssertCollector.assertTrue("Failed to Delete Payment Profile:", isProfileGotDeleted, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Test Method to verify get payment profiles return result based on filter value for user id and payment processor.
     */
    @Test
    public void testSuccessGetPaymentProfilesForPaypalPaymentProcessor() {

        // Add a user , Set the request
        final Map<String, String> userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), RandomStringUtils.randomAlphabetic(8));
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), RandomStringUtils.randomAlphanumeric(12));

        final User user = resource.user().addUser(userRequestParam);
        final String userId = user.getId();

        paymentProfileUtils.addPaypalPaymentProfile(userId, PaymentProcessor.PAYPAL_EMEA.getValue());

        // request map for find payment profiles.
        final Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(PelicanConstants.USER_ID, userId);
        paramsMap.put(PelicanConstants.PAYMENT_PROCESSOR, PaymentProcessor.PAYPAL_EMEA.getValue());
        paramsMap.put(PurchaseOrderParameter.SKIP_COUNT.getName(), "true");

        // get Payment profiles.
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paramsMap);
        final PaymentProfile paymentProfile = paymentProfiles.getPaymentProfile().get(0);

        // Assert on User Id and Payment Processor.
        AssertCollector.assertThat("Incorrect User Id", userId, equalTo(paymentProfile.getUserId()),
            assertionErrorList);
        AssertCollector.assertThat("Resultant Record should not be more than one",
            paymentProfiles.getPaymentProfile().size(), is(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect Payment Processor", PaymentProcessor.PAYPAL_EMEA.getValue(),
            equalTo(paymentProfiles.getPaymentProfile().get(0).getpaymentProcessor()), assertionErrorList);

        resource.paymentProfile().deletePaymentProfile(paymentProfile.getId());

        AssertCollector.assertAll(assertionErrorList);
    }

}
