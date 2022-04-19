package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BillingInformation;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.LinkedHashMap;

/**
 * This is a test class used to test the generation of a credit note number in a sequential order for the refunded and
 * charged back orders
 *
 * @author vineel
 */
public class CreditNoteNumberGenerationTest extends BaseTestData {
    private static final int QUANTITY = 2;
    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This is a test method which will test whether the country in the credit note number is generated correctly for a
     * refunded purchase order with country specified in billing information and payment profile
     */
    @Test
    public void verifyCountryInCreditNoteNumberForPurchaseOrderWithBillingInformation() {

        final LinkedHashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicYearlyUsPriceId(), QUANTITY);
        final BillingInformation billingInformation = PurchaseOrderUtils.getBillingInformation("Automation", "TestUser",
            "Autodesk", "Northville Street", "Benton Dr", "95051", "Nebraska", "Nashville", Country.DE, "6556265666",
            "1234", PaymentType.CREDIT_CARD.getValue(), "VISA", "07/17", "2345", null);
        PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithShippingAndBillingInfo(priceQuantityMap,
                false, PaymentType.PAYPAL, null, billingInformation, buyerUser);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());

        final String countryCode = purchaseOrder.getCreditNoteNumber().substring(4, 6);
        AssertCollector.assertThat("Incorrect country code for the credit note value for purchase order", countryCode,
            equalTo(Country.DE.toString()), assertionErrorList);
        final String fixedText = purchaseOrder.getCreditNoteNumber().substring(7, 9);
        AssertCollector.assertThat("Incorrect fixed text for the credit note value for purchase order", fixedText,
            equalTo("CR"), assertionErrorList);
        final String year = purchaseOrder.getCreditNoteNumber().substring(0, 4);
        final Calendar calendar = Calendar.getInstance();
        final int currentYear = calendar.get(Calendar.YEAR);
        final String presentYear = String.valueOf(currentYear);
        AssertCollector.assertThat("Incorrect year for the credit note value for purchase order", year,
            equalTo(presentYear), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * This is a test method which will test whether the credit note number is generated sequentially for a charged-back
     * purchase order with country specified in stored payment profile
     */
    @Test
    public void verifyCreditNoteNumberGeneratedSequentiallyForChargedBackPurchaseOrder() {

        final LinkedHashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUsPriceId(), QUANTITY);
        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, purchaseOrder.getId());
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String creditNumber = purchaseOrder.getCreditNoteNumber().substring(10);
        final long creditNoteNumber = Long.parseLong(creditNumber);
        PurchaseOrder sequentialPurchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGEBACK, sequentialPurchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        sequentialPurchaseOrder = resource.purchaseOrder().getById(sequentialPurchaseOrder.getId());

        final String sequentialCreditNumber = sequentialPurchaseOrder.getCreditNoteNumber().substring(10);
        final long sequentialCreditNoteNumber = Long.parseLong(sequentialCreditNumber);
        final long expectedSequentialCreditNoteNumber = creditNoteNumber + 1;
        AssertCollector.assertThat("Credit Note Number is not generated sequentially for the purchase orders",
            sequentialCreditNoteNumber, greaterThanOrEqualTo(expectedSequentialCreditNoteNumber), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test whether the credit note number is generated sequentially for a refunded
     * purchase order with country specified in stored payment profile
     */
    @Test
    public void verifyCreditNoteNumberGeneratedSequentiallyForRefundedPurchaseOrder() {

        final LinkedHashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(getBicMonthlyUkPriceId(), QUANTITY);
        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, purchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        final String creditNumber = purchaseOrder.getCreditNoteNumber().substring(10);
        final long creditNoteNumber = Long.parseLong(creditNumber);
        PurchaseOrder sequentialPurchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.REFUND, sequentialPurchaseOrder.getId());
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        sequentialPurchaseOrder = resource.purchaseOrder().getById(sequentialPurchaseOrder.getId());

        final String sequentialCreditNumber = sequentialPurchaseOrder.getCreditNoteNumber().substring(10);
        final long sequentialCreditNoteNumber = Long.parseLong(sequentialCreditNumber);
        final long expectedSequentialCreditNoteNumber = creditNoteNumber + 1;
        AssertCollector.assertThat("Credit Note Number is not generated sequentially for the purchase orders",
            sequentialCreditNoteNumber, greaterThanOrEqualTo(expectedSequentialCreditNoteNumber), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
