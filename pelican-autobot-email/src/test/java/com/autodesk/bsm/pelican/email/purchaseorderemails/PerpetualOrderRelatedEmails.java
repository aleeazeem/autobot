package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test class to validate Perpetual Emails for Order Complete and Order Fulfillment
 *
 * @author Vaibhavi
 */
public class PerpetualOrderRelatedEmails extends SeleniumWebdriver {

    private String priceIdForBasicOfferingWithShipping;
    private PurchaseOrderUtils purchaseOrderUtils;
    private boolean isFeatureFlagChanged;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        // Creating 3 offerings for BIC, Meta and Perpetual
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings basicOfferingsWithShipping = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, null);

        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isFeatureFlagChanged = bankingConfigurationPropertiesPage
            .setFeatureFlag(PelicanConstants.VERIFY_ENTITLEMENTS_FOR_EMAIL_DELIVERY, false);

        // Creating price id for Perpetual
        priceIdForBasicOfferingWithShipping = basicOfferingsWithShipping.getIncluded().getPrices().get(0).getId();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        if (isFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.VERIFY_ENTITLEMENTS_FOR_EMAIL_DELIVERY,
                true);
        }
    }

    /**
     * Test case for Order Complete email for Perpetual Order with Credit Card payment
     */
    @Test
    public void testOrderCompleteAndFulfillmentEmailForPerpetualOrderWithCreditCard() {

        final int quantity = 2;
        PurchaseOrder purchaseOrderForPerpetualCreditCard = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            PaymentType.CREDIT_CARD, priceIdForBasicOfferingWithShipping, getBuyerUser(), quantity);
        // Get purchase order Id
        final String purchaseOrderIdForPerpetualCreditCard = purchaseOrderForPerpetualCreditCard.getId();
        // Purchase order created in previous method is processed to "Pending"
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderIdForPerpetualCreditCard);

        // Purchase order created in previous method is processed to "Charged'
        // state and validate get purchase order
        purchaseOrderForPerpetualCreditCard =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderIdForPerpetualCreditCard);

        purchaseOrderUtils.fulfillRequest(purchaseOrderForPerpetualCreditCard, FulfillmentCallbackStatus.Created);

        // 2 extra minute is required for Perpetual Order fulfillment
        Util.waitInSeconds(TimeConstants.TWO_MINS);
        PelicanDefaultEmailValidations.orderComplete(purchaseOrderIdForPerpetualCreditCard, getEnvironmentVariables(),
            false, Arrays.asList(getBuyerUser().getEmail()));
        PelicanDefaultEmailValidations.orderFulfillment(purchaseOrderIdForPerpetualCreditCard,
            getEnvironmentVariables(), true);
    }
}
