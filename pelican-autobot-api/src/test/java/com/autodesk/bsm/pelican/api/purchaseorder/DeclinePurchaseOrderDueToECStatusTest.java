package com.autodesk.bsm.pelican.api.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderState;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.TreeMap;

public class DeclinePurchaseOrderDueToECStatusTest extends BaseTestData {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int QUANTITY = 4;
    private static TreeMap<String, String> propertiesMap;
    private BuyerUser buyerUser;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        propertiesMap = new TreeMap<>();

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        propertiesMap.clear();
    }

    /**
     * This is a test method which will submit the purchase order with initial EC status as hard block status
     */
    @Test
    public void testPoWithHardBlockStatus() {

        propertiesMap.put("Reason", "terrorist");
        buyerUser.setInitialExportControlStatus(ECStatus.BLOCK.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            getBicMonthlyUsPriceId(), buyerUser, QUANTITY, OrderCommand.DECLINE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrder = resource.properties().addProperties(purchaseOrderId, propertiesMap, true);

            AssertCollector.assertThat("Incorrect purchase order id", purchaseOrder.getId(), equalTo(purchaseOrderId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect purchase order state", purchaseOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect store id", purchaseOrder.getStoreId(), equalTo(getStoreIdUs()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Offering Request Price Id ",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
                equalTo(getBicMonthlyUsPriceId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect quantity",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
                is(QUANTITY), assertionErrorList);
            AssertCollector.assertThat("Incorrect initial ec status",
                purchaseOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ECStatus.BLOCK.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                purchaseOrder.getProperties().getProperty().get(0).getName(), equalTo("Reason"), assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                purchaseOrder.getProperties().getProperty().get(0).getValue(), equalTo("terrorist"),
                assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will submit the purchase order with initial EC status as unverified and final EC
     * status as block status
     */
    @Test
    public void testPoWithHardBlockStatusAfterReview() {

        propertiesMap.put("Reason", "terrorist");
        buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            getBicYearlyUsPriceId(), buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.DECLINE, purchaseOrderId,
                ECStatus.HARDBLOCK);
            purchaseOrder = resource.properties().addProperties(purchaseOrderId, propertiesMap, true);

            AssertCollector.assertThat("Incorrect purchase order id", purchaseOrder.getId(), equalTo(purchaseOrderId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect purchase order state", purchaseOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect store id", purchaseOrder.getStoreId(), equalTo(getStoreIdUs()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Offering Request Price Id ",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
                equalTo(getBicYearlyUsPriceId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect quantity",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
                is(QUANTITY), assertionErrorList);
            AssertCollector.assertThat("Incorrect initial ec status",
                purchaseOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ECStatus.UNVERIFIED.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect final ec status",
                purchaseOrder.getBuyerUser().getFinalExportControlStatus(), equalTo(ECStatus.HARDBLOCK.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                purchaseOrder.getProperties().getProperty().get(0).getName(), equalTo("Reason"), assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                purchaseOrder.getProperties().getProperty().get(0).getValue(), equalTo("terrorist"),
                assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will decline the purchase order because of EC overdue status.
     */
    @Test
    public void testPoWithECOverdueStatusAfterReview() {

        propertiesMap.put("Reason", "ECOverdue");
        buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            getMetaMonthlyUsPriceId(), buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPurchaseOrderWithECStatus(OrderCommand.DECLINE, purchaseOrderId, ECStatus.REVIEW);
            purchaseOrder = resource.properties().addProperties(purchaseOrderId, propertiesMap, true);

            AssertCollector.assertThat("Incorrect purchase order id", purchaseOrder.getId(), equalTo(purchaseOrderId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect purchase order state", purchaseOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect store id", purchaseOrder.getStoreId(), equalTo(getStoreIdUs()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Offering Request Price Id ",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
                equalTo(getMetaMonthlyUsPriceId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect quantity",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
                is(QUANTITY), assertionErrorList);
            AssertCollector.assertThat("Incorrect initial ec status",
                purchaseOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ECStatus.UNVERIFIED.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect final ec status",
                purchaseOrder.getBuyerUser().getFinalExportControlStatus(), equalTo(ECStatus.REVIEW.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                purchaseOrder.getProperties().getProperty().get(0).getName(), equalTo("Reason"), assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                purchaseOrder.getProperties().getProperty().get(0).getValue(), equalTo("ECOverdue"),
                assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will decline the purchase order because of payment failure
     */
    @Test
    public void testPoDeclinedDueToPaymentFailure() {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            getBasicOfferingUsPerpetualDvdActivePriceId(), buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, purchaseOrderId);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

            AssertCollector.assertThat("Incorrect purchase order id", purchaseOrder.getId(), equalTo(purchaseOrderId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect purchase order state", purchaseOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect store id", purchaseOrder.getStoreId(), equalTo(getStoreIdUs()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect Offering Request Price Id ",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getPriceId(),
                equalTo(getBasicOfferingUsPerpetualDvdActivePriceId()), assertionErrorList);
            AssertCollector.assertThat("Incorrect quantity",
                purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingRequest().getQuantity(),
                is(QUANTITY), assertionErrorList);

        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will submit the renewal purchase order with initial EC status as hard block status
     */
    @Test
    public void testRenewalPoWithHardBlockStatus() {

        propertiesMap.put("Reason", "terrorist");
        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.PAYPAL,
            getMetaMonthlyUsPriceId(), buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
            final String subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId();
            buyerUser.setInitialExportControlStatus(ECStatus.BLOCK.getName());
            PurchaseOrder renewalOrder =
                (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                    subscriptionId, buyerUser, OrderCommand.DECLINE, true);
            final String renewalPoId = renewalOrder.getId();
            renewalOrder = resource.properties().addProperties(renewalPoId, propertiesMap, true);
            AssertCollector.assertThat("Incorrect renewal purchase order id", renewalOrder.getId(),
                equalTo(renewalPoId), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order state", renewalOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order store id", renewalOrder.getStoreId(),
                equalTo(getStoreIdUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect initial ec status",
                renewalOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ECStatus.BLOCK.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                renewalOrder.getProperties().getProperty().get(0).getName(), equalTo("Reason"), assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                renewalOrder.getProperties().getProperty().get(0).getValue(), equalTo("terrorist"), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will submit the renewal purchase order with initial EC status as unverified and final
     * ec status as hardblock
     */
    @Test
    public void testRenewalPoWithHardBlockStatusAfterReview() {

        propertiesMap.put("Reason", "terrorist");
        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.PAYPAL, getMetaYearlyUsPriceId(),
            buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
            final String subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId();
            buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
            PurchaseOrder renewalOrder =
                (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                    subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);
            final String renewalPoId = renewalOrder.getId();
            purchaseOrderUtils.processPaypalPurchaseOrderToDeclineWithECStatus(buyerUser, renewalPoId,
                ECStatus.HARDBLOCK);
            renewalOrder = resource.properties().addProperties(renewalPoId, propertiesMap, true);
            AssertCollector.assertThat("Incorrect renewal purchase order id", renewalOrder.getId(),
                equalTo(renewalPoId), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order state", renewalOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order store id", renewalOrder.getStoreId(),
                equalTo(getStoreIdUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect initial ec status",
                renewalOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ECStatus.UNVERIFIED.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect final ec status",
                renewalOrder.getBuyerUser().getFinalExportControlStatus(), equalTo(ECStatus.HARDBLOCK.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                renewalOrder.getProperties().getProperty().get(0).getName(), equalTo("Reason"), assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                renewalOrder.getProperties().getProperty().get(0).getValue(), equalTo("terrorist"), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will submit the renewal purchase order with initial EC status as unverified and final
     * ec status as review
     */
    @Test
    public void testRenewalPoWithEcOverDueStatusAfterReview() {

        propertiesMap.put("Reason", "overdue");
        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.CREDIT_CARD,
            getBicYearlyUsPriceId(), buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
            final String subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId();
            buyerUser.setInitialExportControlStatus(ECStatus.UNVERIFIED.getName());
            PurchaseOrder renewalOrder =
                (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                    subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);
            final String renewalPoId = renewalOrder.getId();
            purchaseOrderUtils.processPaypalPurchaseOrderToDeclineWithECStatus(buyerUser, renewalPoId, ECStatus.REVIEW);
            renewalOrder = resource.properties().addProperties(renewalPoId, propertiesMap, true);
            AssertCollector.assertThat("Incorrect renewal purchase order id", renewalOrder.getId(),
                equalTo(renewalPoId), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order state", renewalOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order store id", renewalOrder.getStoreId(),
                equalTo(getStoreIdUs()), assertionErrorList);
            AssertCollector.assertThat("Incorrect initial ec status",
                renewalOrder.getBuyerUser().getInitialExportControlStatus(), equalTo(ECStatus.UNVERIFIED.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect final ec status",
                renewalOrder.getBuyerUser().getFinalExportControlStatus(), equalTo(ECStatus.REVIEW.getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                renewalOrder.getProperties().getProperty().get(0).getName(), equalTo("Reason"), assertionErrorList);
            AssertCollector.assertThat("Incorrect property key",
                renewalOrder.getProperties().getProperty().get(0).getValue(), equalTo("overdue"), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the decline of the renewal purchase order due to payment failure
     */
    @Test
    public void testRenewalPoDeclinedDueToPaymentFailure() {

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        final Object entity = purchaseOrderUtils.getEntityWithOrderCommand(PaymentType.PAYPAL, getBicYearlyUsPriceId(),
            buyerUser, QUANTITY, OrderCommand.AUTHORIZE);
        if (!(entity instanceof HttpError)) {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            final String purchaseOrderId = purchaseOrder.getId();
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, purchaseOrderId);
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);

            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);

            purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
            final String subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering()
                .getOfferingResponse().getSubscriptionId();
            PurchaseOrder renewalOrder =
                (PurchaseOrder) purchaseOrderUtils.getRenewalPurchaseOrderWithOrderCommand(PaymentType.PAYPAL,
                    subscriptionId, buyerUser, OrderCommand.AUTHORIZE, true);
            final String renewalPoId = renewalOrder.getId();
            purchaseOrderUtils.processPendingPurchaseOrderWithPaypal(buyerUser, renewalPoId);
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPoId);
            renewalOrder = resource.purchaseOrder().getById(renewalPoId);
            AssertCollector.assertThat("Incorrect renewal purchase order id", renewalOrder.getId(),
                equalTo(renewalPoId), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order state", renewalOrder.getOrderState(),
                equalTo(OrderState.DECLINED.toString()), assertionErrorList);
            AssertCollector.assertThat("Incorrect renewal purchase order store id", renewalOrder.getStoreId(),
                equalTo(getStoreIdUs()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
