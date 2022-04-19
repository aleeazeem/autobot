package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * This class tests adding Properties to Purchase Order in different Order Statuses.
 *
 * @author Shweta Hegde
 */
public class AddPropertiesToPurchaseOrderTest extends BaseTestData {

    private static final String DECLINE_ERROR_MESSAGE =
        "The purchase either failed or was not attempted: Failed to complete BlueSnap Order. "
            + "Transaction failed because of payment processing failure.: 05 - This transaction has been declined. "
            + "Please try a different card or contact the credit card provider for assistance:[Do not Honour] (PV-05)";
    private static final String DECLINE_ERROR_CODE = "14002";
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
     * This tests adding error message to Declined order. Step1 : Place an order and process it to PENDING and then to
     * DECLINE. Step2 : Add Error message and error code using "Add Properties to Purchase Order" API. (with space) and
     * with URL Encoding. Step3 : Verify that properties are added successfully.
     */
    @Test
    public void testAddPropertiesToDeclinePurchaseOrderWithEncoding() {

        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 2);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.DECLINE, purchaseOrderId);

        final HashMap<String, String> properties = new HashMap<>();
        properties.put(PelicanConstants.PROPERTIES_MESSAGE, DECLINE_ERROR_MESSAGE);
        properties.put(PelicanConstants.PROPERTIES_ERROR, DECLINE_ERROR_CODE);
        purchaseOrder = resource.properties().addProperties(purchaseOrderId, properties, true);

        AssertCollector.assertThat("Incorrect property error message name",
            purchaseOrder.getProperties().getProperty().get(0).getName(), equalTo(PelicanConstants.PROPERTIES_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property error message value",
            purchaseOrder.getProperties().getProperty().get(0).getValue(), equalTo(DECLINE_ERROR_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property error code name",
            purchaseOrder.getProperties().getProperty().get(1).getName(), equalTo(PelicanConstants.PROPERTIES_ERROR),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property error code value",
            purchaseOrder.getProperties().getProperty().get(1).getValue(), equalTo(DECLINE_ERROR_CODE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This tests adding bluesnap properties to PENDING order. Step1 : Place an order and process it to PENDING. Step2 :
     * Add bluesanp invoice id and order id using "Add Properties to Purchase Order" API. (without space). Step3 :
     * Verify that properties are added successfully.
     */
    @Test
    public void testAddBluesnapPropertiesToPendingPurchaseOrder() {

        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 2);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);

        final HashMap<String, String> properties = new HashMap<>();
        properties.put("bluesnapInvoiceId", "763547");
        properties.put("bluesnapOrderId", "673683683");
        purchaseOrder = resource.properties().addProperties(purchaseOrderId, properties, true);

        AssertCollector.assertThat("Incorrect property name for bluesnap invoice id",
            purchaseOrder.getProperties().getProperty().get(0).getName(), equalTo("bluesnapInvoiceId"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property value for bluesnap invoice id",
            purchaseOrder.getProperties().getProperty().get(0).getValue(), equalTo("763547"), assertionErrorList);
        AssertCollector.assertThat("Incorrect property name for bluesnap order id",
            purchaseOrder.getProperties().getProperty().get(1).getName(), equalTo("bluesnapOrderId"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property value for bluesnap order id",
            purchaseOrder.getProperties().getProperty().get(1).getValue(), equalTo("673683683"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This tests adding error message to Declined order. Step1 : Place an order and process it to PENDING and then to
     * DECLINE. Step2 : Add Error message and error code using "Add Properties to Purchase Order" API. (with space) and
     * without URL Encoding. Step3 : Verify that properties are added successfully.
     *
     * NOTE: this test case is disabled since there is a defect BIC-7876. Once API-GW fixes this issue, this testcase
     * can be enabled.
     */
    @Test(enabled = false)
    public void testAddPropertiesToDeclinePurchaseOrderWithoutEncodingDefect7876() {

        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(Payment.PaymentType.CREDIT_CARD, getBicYearlyUsPriceId(), buyerUser, 3);
        final String purchaseOrderId = purchaseOrder.getId();
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.DECLINE, purchaseOrderId);

        final HashMap<String, String> properties = new HashMap<>();
        properties.put(PelicanConstants.PROPERTIES_MESSAGE, DECLINE_ERROR_MESSAGE);
        properties.put(PelicanConstants.PROPERTIES_ERROR, DECLINE_ERROR_CODE);
        purchaseOrder = resource.properties().addProperties(purchaseOrderId, properties, false);

        AssertCollector.assertThat("Incorrect property error message name",
            purchaseOrder.getProperties().getProperty().get(0).getName(), equalTo(PelicanConstants.PROPERTIES_MESSAGE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property error message value",
            purchaseOrder.getProperties().getProperty().get(0).getValue(), equalTo(DECLINE_ERROR_CODE),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property error code name",
            purchaseOrder.getProperties().getProperty().get(1).getName(), equalTo(PelicanConstants.PROPERTIES_ERROR),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect property error code value",
            purchaseOrder.getProperties().getProperty().get(1).getValue(), equalTo(DECLINE_ERROR_CODE),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
