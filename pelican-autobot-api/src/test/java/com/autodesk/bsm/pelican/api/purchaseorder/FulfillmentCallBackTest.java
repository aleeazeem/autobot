package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.OrderResponse;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * This test class contains method to test the fulfillment API calls. Need to add more scenarios like Contract
 * verification, Error message validation verification permission based validations etc.
 *
 * @author kishor
 */
public class FulfillmentCallBackTest extends BaseTestData {

    private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentCallBackTest.class.getSimpleName());
    private PurchaseOrderUtils purchaseOrderUtils;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    /**
     * This test method verifies the fulfillment call back API with single lineItem and PO using CreditCard
     */
    @Test
    public void testSingleLineItemLegacyFulfillmentWithCreditCard() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBasicOfferingUsPerpetualDvdActivePriceId(), 2);
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, true, buyerUser);
        final String PO_ID = newPurchase.getId();
        final String fulfillmentGroupId = newPurchase.getFulFillmentGroups().getFulfillmentGroups().get(0).getId();
        LOGGER.info("PO ID :" + PO_ID);
        LOGGER.info("Fulfillment GroupId :" + fulfillmentGroupId);
        final OrderResponse response =
            purchaseOrderUtils.fulfillRequest(newPurchase, FulfillmentCallbackStatus.Created);
        AssertCollector.assertThat("Fulfillment Status Not as Expected", response.getOrderCreationStatus(),
            equalTo("Received"), assertionErrorList);
        AssertCollector.assertThat("Invalid PurchaseOrder Id", response.getOrderReference().getPONumber(),
            equalTo(PO_ID), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This test method verifies the fulfillment call back API with multi lineItem and PO using Paypal
     */
    @Test
    public void testMultiLineItemsLegacyFulfillmentWithPaypal() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(getBasicOfferingUsPerpetualDvdActivePriceId(), 2);
        priceQuantityMap.put(getMetaMonthlyUsPriceId(), 2);
        final PurchaseOrder newPurchase =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, true, buyerUser);
        final String PO_ID = newPurchase.getId();
        final String fulfillmentGroupId = newPurchase.getFulFillmentGroups().getFulfillmentGroups().get(0).getId();
        LOGGER.info("PO ID :" + PO_ID);
        LOGGER.info("Fulfillment GroupId :" + fulfillmentGroupId);
        final OrderResponse response =
            purchaseOrderUtils.fulfillRequest(newPurchase, FulfillmentCallbackStatus.Created);
        AssertCollector.assertThat("Fulfillment Status Not as Expected", response.getOrderCreationStatus(),
            equalTo("Received"), assertionErrorList);
        AssertCollector.assertThat("Invalid PurchaseOrder Id", response.getOrderReference().getPONumber(),
            equalTo(PO_ID), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
