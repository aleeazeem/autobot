package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfiles;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.TokenPayment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.CreditCard;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.DirectDebitPayment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentMethod;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.StoredProfilePayment;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PaymentProfileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProfileUtils.class.getSimpleName());

    private PelicanPlatform resource;
    private Payment payment;
    private StoredProfilePayment storedPaymentProfile;
    private String appFamilyId;

    public PaymentProfileUtils(final EnvironmentVariables environmentVariables, final String appFamilyId) {
        this.appFamilyId = appFamilyId;
        if (appFamilyId.equals(environmentVariables.getAppFamilyId())) {
            resource = new PelicanClient(environmentVariables, environmentVariables.getAppFamily()).platform();
        } else {
            resource = new PelicanClient(environmentVariables, environmentVariables.getOtherAppFamily()).platform();
        }
    }

    /**
     * Utility method to create CreditCard SPP with IpCountry
     *
     * @return PaymentProfile
     */
    public PaymentProfile addCreditCardPaymentProfile(final String userId, final String paymentProcessor) {
        final PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setAppFamilyId(appFamilyId);
        paymentProfile.setUserId(userId);
        paymentProfile.setpaymentProcessor(paymentProcessor);
        final CreditCard creditCard = new CreditCard();
        creditCard.setCreditCardType("VISA");
        creditCard.setCreditCardNumber("9818");
        creditCard.setExpirationDate("0420");
        creditCard.setFirstName("Bob");
        creditCard.setSurname("Lee");
        creditCard.setStreetAddress("123 Main Street");
        creditCard.setCity("San Francisco");
        creditCard.setCountryCode("US");
        creditCard.setState("CA");
        creditCard.setZipCode("94105");
        paymentProfile.setCreditCard(creditCard);
        paymentProfile.setIpCountry("US");
        return resource.paymentProfile().add(paymentProfile);
    }

    /**
     * Utility method to create the Paypal Payment Profile
     */
    public PaymentProfile addPaypalPaymentProfile(final String userId, final String paymentProcessor) {

        final PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setAppFamilyId(appFamilyId);
        paymentProfile.setUserId(userId);
        paymentProfile.setpaymentProcessor(paymentProcessor);
        final TokenPayment tokenPayment = new TokenPayment();
        tokenPayment.setPaymentMethod(PaymentType.PAYPAL.getValue());
        tokenPayment.setToken("B-2LB48326BC2190442");
        tokenPayment.setFirstName("Bob");
        tokenPayment.setSurname("Lee");
        tokenPayment.setBillToStreetAddress("123 Main Street");
        tokenPayment.setCity("San Francisco");
        tokenPayment.setStateProvince("CA");
        tokenPayment.setCountryCode("US");
        tokenPayment.setZipCode("94105");
        paymentProfile.setTokenPayment(tokenPayment);
        return resource.paymentProfile().add(paymentProfile);
    }

    /**
     * This method return payment done through Credit card
     *
     * @return Payment
     */
    public Payment getCreditCardPayment(final String userId, final String paymentProcessorForPayment,
        final String paymentProcessorForPaymentProfile, final String paymentGatewayId) {

        String paymentProfileId;
        if (userId.equals(BaseTestData.getUser().getId())) {
            if (paymentProcessorForPaymentProfile.equals(PaymentProcessor.BLUESNAP_NAMER.getValue())) {
                paymentProfileId = BaseTestData.getPaymentProfileIdForBlueSnapNamer();
            } else {
                paymentProfileId = BaseTestData.getPaymentProfileIdForBlueSnapEmea();
            }
        } else {
            paymentProfileId = createPaymentProfileIfNotExists(userId, paymentProcessorForPaymentProfile);
        }
        storedPaymentProfile = new StoredProfilePayment();
        storedPaymentProfile.setStoredPaymentProfileId(paymentProfileId);
        payment = new Payment();
        payment.setStoredProfilePayment(storedPaymentProfile);
        payment.setConfigId(paymentGatewayId);
        payment.setPaymentProcessor(paymentProcessorForPayment);
        return payment;
    }

    /**
     * This method returns purchase order command for Pending purchase order with Paypal
     *
     * @return PurchaseOrderCommand
     */
    public PurchaseOrderCommand getPaypalPaymentForPO(final String userId, final String paymentProcessorForPayment,
        final String paymentProcessorForPaymentProfile, final String paymentGatewayId, final String orderCommand) {

        String paymentProfileId;
        if (userId.equals(BaseTestData.getUser().getId())) {
            if (paymentProcessorForPaymentProfile.equals(PaymentProcessor.PAYPAL_NAMER.getValue())) {
                paymentProfileId = BaseTestData.getPaymentProfileIdForPaypalNamer();
            } else {
                paymentProfileId = BaseTestData.getPaymentProfileIdForPaypalEmea();
            }
        } else {
            paymentProfileId = createPaymentProfileIfNotExists(userId, paymentProcessorForPaymentProfile);
        }
        storedPaymentProfile = new StoredProfilePayment();
        storedPaymentProfile.setStoredPaymentProfileId(paymentProfileId);
        payment = new Payment();
        payment.setStoredProfilePayment(storedPaymentProfile);
        payment.setConfigId(paymentGatewayId);
        payment.setPaymentProcessor(paymentProcessorForPayment);
        final PurchaseOrderCommand purchaseOrderCommand = new PurchaseOrderCommand();
        purchaseOrderCommand.setCommandType(orderCommand);
        purchaseOrderCommand.setPayment(payment);
        return purchaseOrderCommand;
    }

    /**
     * Helper Method to add ACH or SEPA Direct Debit Payment Profile.
     *
     * @param userId
     * @param paymentProcessor : BLUESNAP-NAMER OR BLUSNAP-EMEA
     * @param paymentMethod ACH OR SEPA
     * @param addNonMandatoryField if true set AccountNickname, StateProvince and Zipcode else not
     * @return ACH OR SEPA payment Profile or HTTP Error
     */
    public <T extends PelicanPojo> T addDirectDebitPayment(final String userId, final String paymentProcessor,
        final PaymentMethod paymentMethod, final Boolean addNonMandatoryField) {

        final PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setAppFamilyId(appFamilyId);
        paymentProfile.setUserId(userId);
        paymentProfile.setpaymentProcessor(paymentProcessor);
        final DirectDebitPayment directDebitPayment = new DirectDebitPayment();
        directDebitPayment.setPaymentMethod(paymentMethod.getValue());

        getNameAndBillingInfo(directDebitPayment, addNonMandatoryField, paymentMethod);

        if (paymentMethod == PaymentMethod.ACH) {
            directDebitPayment.setAccountType("CHECKING");
            directDebitPayment.setAccountNumber(RandomStringUtils.randomNumeric(5));
            directDebitPayment.setAccountNumberLastFour(RandomStringUtils.randomNumeric(4));
            directDebitPayment.setRoutingNumber(RandomStringUtils.randomNumeric(5));
            directDebitPayment.setRoutingNumberLastFour(RandomStringUtils.randomNumeric(4));
        } else {
            directDebitPayment.setIban(RandomStringUtils.randomNumeric(10));
            directDebitPayment.setIbanFirstFour(RandomStringUtils.randomNumeric(4));
            directDebitPayment.setIbanLastFour(RandomStringUtils.randomNumeric(4));
        }
        if (addNonMandatoryField) {
            directDebitPayment.setAccountNickname(RandomStringUtils.randomAlphanumeric(8));
        }
        paymentProfile.setDirectDebitPayment(directDebitPayment);

        return resource.paymentProfile().add(paymentProfile);
    }

    public Payment getDirectDebitPaymentProfile(final String userId, final String paymentProcessor,
        final PaymentMethod paymentMethod) {
        final PaymentProfile paymentProfile = addDirectDebitPayment(userId, paymentProcessor, paymentMethod, false);
        final String paymentProfileId = paymentProfile.getId();
        storedPaymentProfile = new StoredProfilePayment();
        storedPaymentProfile.setStoredPaymentProfileId(paymentProfileId);
        payment = new Payment();
        payment.setStoredProfilePayment(storedPaymentProfile);
        payment.setPaymentProcessor(paymentProcessor);
        if (paymentMethod == PaymentMethod.SEPA) {
            payment.setMandateId(RandomStringUtils.randomNumeric(6));
            payment.setMandateDate(DateTimeUtils.getCurrentDate(PelicanConstants.DATE_WITH_TIME_ZONE));
        }
        return payment;
    }

    /**
     * Get Buyer Details for Direct Debit Payment Method.
     *
     * @param directDebitPayment
     * @param addNonMandatoryField
     * @return DirectDebitPayment
     */
    private DirectDebitPayment getNameAndBillingInfo(final DirectDebitPayment directDebitPayment,
        final Boolean addNonMandatoryField, final PaymentMethod paymentMethod) {

        directDebitPayment.setFirstName("FirstName : " + RandomStringUtils.randomAlphanumeric(8));
        directDebitPayment.setSurname("Surname : " + RandomStringUtils.randomAlphanumeric(8));
        directDebitPayment.setStreetAddress("581 College Ave" + RandomStringUtils.randomAlphanumeric(1));
        directDebitPayment.setCity("San Ramon" + RandomStringUtils.randomAlphabetic(1));

        directDebitPayment.setCountryCode(RandomStringUtils.randomAlphabetic(2));
        if (addNonMandatoryField) {
            directDebitPayment.setState(RandomStringUtils.randomAlphabetic(2));
            directDebitPayment.setZipCode(RandomStringUtils.randomNumeric(5));
        }

        if (paymentMethod == PaymentMethod.SEPA) {
            directDebitPayment.setCountryCode("GB");
        }
        directDebitPayment.setCountryCode("US");
        directDebitPayment.setState(RandomStringUtils.randomAlphabetic(2));
        directDebitPayment.setZipCode(RandomStringUtils.randomNumeric(5));
        directDebitPayment.setCompanyName("Company Name : " + RandomStringUtils.randomAlphabetic(5));

        return directDebitPayment;
    }

    /**
     * This is a helper method, which checks if Payment Profile exists for the user and payment processor, if exists, it
     * returns that one or returns a newly created one.
     *
     * @param userId
     * @param paymentProcessor
     * @return String
     */
    public String createPaymentProfileIfNotExists(final String userId, final String paymentProcessor) {

        final Map<String, String> paymentParams = new HashMap<>();
        paymentParams.put(PelicanConstants.USER_ID, userId);
        paymentParams.put(PelicanConstants.PAYMENT_PROCESSOR, paymentProcessor);
        final PaymentProfiles paymentProfiles = resource.paymentProfiles().getPaymentProfiles(paymentParams);

        // If get payment profiles returns none, then add a new payment profile
        if (paymentProfiles.getPaymentProfile().size() == 0) {
            if (PaymentProcessor.BLUESNAP_NAMER.getValue().equals(paymentProcessor)
                || PaymentProcessor.BLUESNAP_EMEA.getValue().equals(paymentProcessor)) {
                return addCreditCardPaymentProfile(userId, paymentProcessor).getId();
            }
            return addPaypalPaymentProfile(userId, paymentProcessor).getId();
        } else {
            LOGGER.info("Payment Profile for " + paymentProcessor + " Exists");
            return paymentProfiles.getPaymentProfile().get(0).getId();
        }
    }
}
