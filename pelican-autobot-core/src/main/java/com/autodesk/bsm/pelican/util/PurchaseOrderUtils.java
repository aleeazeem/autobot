package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.JobsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.Asset;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.LineItem;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.ListOfCurrentAsset;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.ListOfLineItem;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.Root;
import com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback.Root.Order;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.AdditionalFees;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.AdditionalFees.AdditionalFee;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BillingInformation;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStrategy;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItems;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Offering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Offering.OfferingRequest;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentMethod;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrderCommand;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Recorder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.ShipTo;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Shipping;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.ShippingMethod;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionExtension;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionQuantity;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionQuantity.SubscriptionQuantityRequest;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionRenewal;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.SubscriptionRenewal.SubscriptionRenewalRequest;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonInvoiceNumberStatus;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.DeclineReason;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.enums.StateProvince;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * This class have methods defined to place/process purchase orders
 *
 * @author kishor
 */
public class PurchaseOrderUtils {

    private EnvironmentVariables environmentVariables;
    private String appFamily;
    private PelicanPlatform resource;
    private OfferingRequest offeringRequest;
    private SubscriptionRenewalRequest subscriptionRenewalRequest;
    private Offering offering;
    private SubscriptionRenewal subscriptionRenewal;
    private LineItems lineItems;
    private com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem lineItem;
    private PurchaseOrder purchaseOrder;
    private PaymentProfileUtils paymentProfileUtils;
    private Payment payment;
    private Subscription subscription;
    private String currencyName;
    private String currencyId;
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderUtils.class.getSimpleName());

    public PurchaseOrderUtils(final EnvironmentVariables environmentVariables, final String applicationFamilyId) {
        this.environmentVariables = environmentVariables;
        if (applicationFamilyId.equals(environmentVariables.getAppFamilyId())) {
            resource = new PelicanClient(environmentVariables, environmentVariables.getAppFamily()).platform();
            appFamily = environmentVariables.getAppFamily();
        } else {
            resource = new PelicanClient(environmentVariables, environmentVariables.getOtherAppFamily()).platform();
            appFamily = environmentVariables.getOtherAppFamily();
        }
        paymentProfileUtils = new PaymentProfileUtils(environmentVariables, applicationFamilyId);
    }

    /**
     * Complete a PO, after creating a user, adding a CC SPP to the user, and submit PO with AUTHORIZE,PENDING and
     * CHARGE
     *
     * @param priceQuantityMap - Which should be the priceId:Quantity mapping - E.g We need to purchase 2 Quantity of
     *        price Id 1001, mapping looks like [1001:2]
     * @param addTax - Boolean , if true appends a fixed tax(additional Fees entity), probably need a enhancement
     * @param buyerUser
     * @return PurchaseOrder[CHARGED]
     */
    public PurchaseOrder submitAndProcessNewAcquisitionPurchaseOrderWithCC(
        final HashMap<String, Integer> priceQuantityMap, final boolean addTax, final BuyerUser buyerUser) {
        return submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap, addTax, PaymentType.CREDIT_CARD, null,
            null, buyerUser);
    }

    /**
     * Complete a PO, after creating a user, adding a Paypal SPP to the user, and submit PO with AUTHORIZE,PENDING and
     * CHARGE
     *
     * @param priceQuantityMap - Which should be the priceId:Quantity mapping - E.g We need to purchase 2 Quantity of
     *        price Id 1001, mapping looks like [1001:2]
     * @param addTax - Boolean , if true appends a fixed tax(additional Fees entity), probably need a enhancement
     * @param buyerUser
     * @return PurchaseOrder[CHARGED]
     */
    public PurchaseOrder submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(
        final HashMap<String, Integer> priceQuantityMap, final boolean addTax, final BuyerUser buyerUser) {
        return submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap, addTax, PaymentType.PAYPAL, null, null,
            buyerUser);
    }

    /**
     * Submit and Process Purchase Order for Subscription Quantity (Add Seats)
     *
     * @param subscriptionQuantityRequestLineItemList
     * @param taxAmount
     * @param buyerUser
     * @paymentType :PaymentType.CREDIT_CARD or PaymentType.PAYPAL
     * @orderCommand : OrderCommand.AUTHORIZE.getValue()
     */
    public <T extends PelicanPojo> T submitSubscriptionQuantityPurchaseOrder(
        final List<Map<String, String>> subscriptionQuantityRequestLineItemList, final PaymentType paymentType,
        final OrderCommand orderCommand, final String taxAmount, final BuyerUser buyerUser) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setBuyerUser(buyerUser);

        final LineItems lineItems = new LineItems();
        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> listOfLineItem = new ArrayList<>();
        for (final Map<String, String> subscriptionQuantityRequestLineItemMap : subscriptionQuantityRequestLineItemList) {

            // create line item for subscription quantity
            final SubscriptionQuantity subscriptionQuantity =
                createSubscriptionQuantityLineItem(subscriptionQuantityRequestLineItemMap);

            final com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem lineItem =
                new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
            lineItem.setSubscriptionQuantity(subscriptionQuantity);

            if (subscriptionQuantityRequestLineItemMap.get(LineItemParams.PROMOTION_REFERENCE_ID.getValue()) != null) {
                final PromotionReference promotionReference = new PromotionReference();
                promotionReference.setId(
                    subscriptionQuantityRequestLineItemMap.get(LineItemParams.PROMOTION_REFERENCE_ID.getValue()));
                final PromotionReferences promotionReferences = new PromotionReferences();
                promotionReferences.setPromotionReference(promotionReference);
                lineItem.setPromotionReferences(promotionReferences);
            }

            if (taxAmount != null) {
                final AdditionalFees addFees = getAdditionalFees(taxAmount);
                lineItem.setAdditionalFees(addFees);
            }

            listOfLineItem.add(lineItem);
        }

        lineItems.setLineItems(listOfLineItem);
        purchaseOrder.setLineItems(lineItems);
        purchaseOrder.setOrderCommand(orderCommand.toString());
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(), null,
            PaymentProcessor.BLUESNAP_EMEA.getValue(), PaymentProcessor.BLUESNAP_EMEA.getValue());

        // Place a PO by calling submitPurchaseOrder API
        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * Complete a Line Item for Subscription Quantity Request
     *
     * @param subscriptionQuantityRequestLineItemMap
     * @return subscriptionQuantity.
     */
    private SubscriptionQuantity createSubscriptionQuantityLineItem(
        final Map<String, String> subscriptionQuantityRequestLineItemMap) {
        final SubscriptionQuantity subscriptionQuantity = new SubscriptionQuantity();
        final SubscriptionQuantityRequest subscriptionQuantityRequest = new SubscriptionQuantityRequest();

        subscriptionQuantityRequest
            .setSubscriptionId(subscriptionQuantityRequestLineItemMap.get(LineItemParams.SUBSCRIPTION_ID.getValue()));
        subscriptionQuantityRequest
            .setQuantity(subscriptionQuantityRequestLineItemMap.get(LineItemParams.QUANTITY.getValue()));
        subscriptionQuantityRequest
            .setPriceId(subscriptionQuantityRequestLineItemMap.get(LineItemParams.PRICE_ID.getValue()));

        subscriptionQuantity.setSubscriptionQuantityRequest(subscriptionQuantityRequest);
        return subscriptionQuantity;

    }

    /**
     * Complete a Subscription Renewal PO, for the given user and SPP Id , and submit PO with AUTHORIZE,PENDING and
     * CHARGE
     *
     * @param buyerUser
     * @param subscriptionIdList
     * @param addTax
     * @param paymentType
     * @param subscriptionPromoReferencesMap
     * @param updateSubscriptionNBDByToday
     * @return <T>
     */
    public <T extends PelicanPojo> T submitAndProcessRenewalPurchaseOrder(final BuyerUser buyerUser,
        final List<String> subscriptionIdList, final boolean addTax, final PaymentType paymentType,
        final Map<String, PromotionReferences> subscriptionPromoReferencesMap,
        final Boolean updateSubscriptionNBDByToday) {

        // Place a PO by calling submitPurchaseOrder API
        final PurchaseOrder resultRenewalPO = submitRenewalPurchaseOrderWithPromos(subscriptionIdList, addTax,
            paymentType, subscriptionPromoReferencesMap, updateSubscriptionNBDByToday, buyerUser);
        if (paymentType == PaymentType.CREDIT_CARD) {
            processPurchaseOrder(OrderCommand.PENDING, resultRenewalPO.getId());
        } else {
            processPendingPurchaseOrderWithPaypal(buyerUser, resultRenewalPO.getId());
        }
        // Process the PO with CHARGE command
        final PurchaseOrderCommand chargeRenewalPurchaseOrderCommand = new PurchaseOrderCommand();
        chargeRenewalPurchaseOrderCommand.setCommandType(OrderCommand.CHARGE.toString());

        return resource.processPurchaseOrder().process(chargeRenewalPurchaseOrderCommand, resultRenewalPO.getId());
    }

    /**
     * Complete a PO, for the given user and SPP Id, and submit PO with AUTHORIZE, PENDING and CHARGE
     *
     * @param priceQuantityMap
     * @param addTax
     * @param paymentType
     * @param pricePromoReferencesMap
     * @param billingInformation
     * @param buyerUser
     * @return <T>
     * @throws ClientException
     */
    public <T extends PelicanPojo> T submitAndProcessNewAcquisitionPurchaseOrder(
        final Map<String, Integer> priceQuantityMap, final boolean addTax, final PaymentType paymentType,
        final Map<String, PromotionReferences> pricePromoReferencesMap, final BillingInformation billingInformation,
        final BuyerUser buyerUser) throws ClientException {
        PurchaseOrder resultPO;
        // Place a PO by calling submitPurchaseOrder API
        final Object entity = submitNewAcquisitionPurchaseOrderWithPromosAndBillingInfo(priceQuantityMap, addTax,
            paymentType, pricePromoReferencesMap, billingInformation, null, buyerUser);
        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            return (T) error;
        } else {
            resultPO = (PurchaseOrder) entity;
        }
        // Process the PO with PENDING command
        if (paymentType == PaymentType.CREDIT_CARD) {
            processPurchaseOrderWithECStatus(OrderCommand.PENDING, resultPO.getId(), ECStatus.ACCEPT);
        } else {
            processPendingPurchaseOrderWithPaypal(buyerUser, resultPO.getId());
        }
        // Process the PO with CHARGE command
        final PurchaseOrderCommand chargePurchaseOrderCommand = new PurchaseOrderCommand();
        chargePurchaseOrderCommand.setCommandType(OrderCommand.CHARGE.toString());

        return resource.processPurchaseOrder().process(chargePurchaseOrderCommand, resultPO.getId());
    }

    /**
     * Complete a PO, for the given user, shipping info, billing info and SPP Id, and submit PO with AUTHORIZE, PENDING
     * and CHARGE
     *
     * @param priceQuantityMap
     * @param addTax
     * @param paymentType
     * @param shippingInfo
     * @param billingInfo
     * @param buyerUser
     * @return <T>
     * @throws ClientException
     */
    public <T extends PelicanPojo> T submitAndProcessNewAcquisitionPurchaseOrderWithShippingAndBillingInfo(
        final Map<String, Integer> priceQuantityMap, final boolean addTax, final PaymentType paymentType,
        final Shipping shippingInfo, final BillingInformation billingInfo, final BuyerUser buyerUser)
        throws ClientException {
        // Place a PO by calling submitPurchaseOrder API
        final PurchaseOrder resultPO = submitNewAcquisitionPurchaseOrderWithPromosAndBillingInfo(priceQuantityMap,
            addTax, paymentType, null, billingInfo, shippingInfo, buyerUser);
        // Process the PO with PENDING command
        if (paymentType == PaymentType.PAYPAL) {
            processPendingPurchaseOrderWithPaypal(buyerUser, resultPO.getId());
        } else {
            processPurchaseOrder(OrderCommand.PENDING, resultPO.getId());
        }
        // Process the PO with CHARGE command
        processPurchaseOrder(OrderCommand.CHARGE, resultPO.getId());

        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(resultPO.getId());
        return (T) purchaseOrder;
    }

    /**
     * Create an authorized PO, for the given user and SPP Id , and submit PO with AUTHORIZE
     *
     * @param priceQuantityMap
     * @param addTax
     * @param paymentType
     * @param pricePromoReferencesMap
     * @param buyerUser
     * @return <T>
     * @throws ClientException
     */
    public <T extends PelicanPojo> T submitNewAcquisitionPurchaseOrderWithPromos(
        final Map<String, Integer> priceQuantityMap, final boolean addTax, final PaymentType paymentType,
        final Map<String, PromotionReferences> pricePromoReferencesMap, final BuyerUser buyerUser)
        throws ClientException {

        // invoke submitNewAcquisitionPurchaseOrderWithPromosAndBillingInfo with null BillingInfo
        return submitNewAcquisitionPurchaseOrderWithPromosAndBillingInfo(priceQuantityMap, addTax, paymentType,
            pricePromoReferencesMap, null, null, buyerUser);
    }

    /**
     * Create an authorized PO, for the given user and SPP Id , and submit PO with AUTHORIZE
     *
     * @param priceQuantityMap
     * @param addTax
     * @param paymentType
     * @param pricePromoReferencesMap
     * @param billingInfo
     * @param shippingInfo
     * @param buyerUser
     * @return <T>
     * @throws ClientException
     */
    public <T extends PelicanPojo> T submitNewAcquisitionPurchaseOrderWithPromosAndBillingInfo(
        final Map<String, Integer> priceQuantityMap, final boolean addTax, final PaymentType paymentType,
        final Map<String, PromotionReferences> pricePromoReferencesMap, final BillingInformation billingInfo,
        final Shipping shippingInfo, final BuyerUser buyerUser) throws ClientException {
        purchaseOrder = new PurchaseOrder();

        final LineItems newLineItems = new LineItems();
        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItems = new ArrayList<>();
        for (final String key : priceQuantityMap.keySet()) {
            final com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem lineItem =
                new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
            final Offering offering = new Offering();
            final OfferingRequest offeringRequest = new OfferingRequest();
            offeringRequest.setPriceId(key);
            offeringRequest.setQuantity(priceQuantityMap.get(key));
            offering.setOfferingRequest(offeringRequest);
            lineItem.setOffering(offering);
            if (pricePromoReferencesMap != null) {
                lineItem.setPromotionReferences(pricePromoReferencesMap.get(key));
            }
            if (addTax) {
                final AdditionalFees addFees = new AdditionalFees();
                final AdditionalFee addFee = new AdditionalFee();
                addFee.setAmount(String.valueOf(RandomUtils.nextInt(15)));
                addFee.setFeeCollectorId(environmentVariables.getFeeCollectorId());
                addFee.setTaxPayer("BUYER");
                addFee.setTaxIncludedInBasePrice("false");
                addFee.setCurrencyId(4);
                addFee.setType("TAX");
                addFees.setAdditionalFee(addFee);
                lineItem.setAdditionalFees(addFees);
            }
            lineItems.add(lineItem);
        }

        newLineItems.setLineItems(lineItems);
        if (billingInfo != null) {
            purchaseOrder.setBillingInformation(billingInfo);
        }

        if (shippingInfo != null) {
            purchaseOrder.setShipping(shippingInfo);
        }
        purchaseOrder.setLineItems(newLineItems);
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());

        if (buyerUser == null) {
            purchaseOrder.setBuyerUser(BaseTestData.getBuyerUser());
            purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, BaseTestData.getBuyerUser(),
                environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
                environmentVariables.getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
                PaymentProcessor.BLUESNAP_EMEA.getValue());
        } else {
            purchaseOrder.setBuyerUser(buyerUser);
            purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
                environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
                environmentVariables.getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
                PaymentProcessor.BLUESNAP_EMEA.getValue());
        }

        // Place a PO by calling submitPurchaseOrder API
        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * Complete a Renewal PO, for the given user and SPP Id , and submit PO with AUTHORIZE
     *
     * @param subscriptionIdList
     * @param addTax
     * @param paymentType
     * @param subscriptionPromoReferencesMap
     * @param updateSubscriptionNBDByToday
     * @param buyerUser
     * @return <T>
     * @throws ClientException
     */
    public <T extends PelicanPojo> T submitRenewalPurchaseOrderWithPromos(final List<String> subscriptionIdList,
        final boolean addTax, final PaymentType paymentType,
        final Map<String, PromotionReferences> subscriptionPromoReferencesMap,
        final Boolean updateSubscriptionNBDByToday, final BuyerUser buyerUser) throws ClientException {
        PurchaseOrder purchaseOrder = new PurchaseOrder();

        buyerUser.setInitialExportControlStatus(ECStatus.ACCEPT.getName());
        final LineItems newLineItems = new LineItems();
        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItems = new ArrayList<>();
        for (final String key : subscriptionIdList) {
            // UPDATE SUBSCRIPTIONS NBD.
            if (updateSubscriptionNBDByToday) {
                SubscriptionUtils.updateSubscriptionNBD(key, resource, appFamily, environmentVariables,
                    DateTimeUtils.getCurrentDate(PelicanConstants.DATE_WITH_TIME_ZONE));
            }
            lineItem = new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
            subscriptionRenewal = new SubscriptionRenewal();
            subscriptionRenewalRequest = new SubscriptionRenewalRequest();
            subscription = resource.subscription().getById(key);
            currencyId = subscription.getNextBillingPriceCurrencyId();
            currencyName = subscription.getNextBillingPriceCurrencyName();
            subscriptionRenewalRequest.setSubscriptionId(key);
            subscriptionRenewalRequest.setCurrencyId(currencyId);
            subscriptionRenewalRequest.setCurrencyName(currencyName);
            subscriptionRenewal.setSubscriptionRenewalRequest(subscriptionRenewalRequest);
            lineItem.setSubscriptionRenewal(subscriptionRenewal);
            if (subscriptionPromoReferencesMap != null) {
                lineItem.setPromotionReferences(subscriptionPromoReferencesMap.get(key));
            }
            if (addTax) {
                final AdditionalFees addFees = new AdditionalFees();
                final AdditionalFee addFee = new AdditionalFee();
                addFee.setAmount(String.valueOf(RandomUtils.nextInt(15)));
                addFee.setFeeCollectorId(environmentVariables.getFeeCollectorId());
                addFee.setTaxPayer("BUYER");
                addFee.setTaxIncludedInBasePrice("false");
                addFee.setCurrencyId(Integer.parseInt(currencyId));
                addFee.setType("TAX");
                addFees.setAdditionalFee(addFee);
                lineItem.setAdditionalFees(addFees);
            }
            lineItems.add(lineItem);
        }

        newLineItems.setLineItems(lineItems);

        purchaseOrder.setBuyerUser(buyerUser);
        purchaseOrder.setLineItems(newLineItems);
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
            PaymentProcessor.BLUESNAP_EMEA.getValue());

        // Place a PO by calling submitPurchaseOrder API
        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * Submit Paypal Order with Recorder State and billing info.
     */
    public PurchaseOrder submitPaypalPurchaseOrderWithRecorderStateAndBillingInfo(
        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItems, final String paymentGatewayId,
        final BuyerUser buyerUser) throws ClientException {
        // Setup Line Items.
        final LineItems newLineItems = new LineItems();
        newLineItems.setLineItems(lineItems);

        // Create Purchase Order object with Line Items and buyer User.
        final PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setBuyerUser(buyerUser);
        purchaseOrder.setLineItems(newLineItems);

        // Set Authorized Command.
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());

        // Set Billing info.
        final BillingInformation billingInformation = getBillingInformation("Automation", "TestUSer", "Autodesk",
            "Northville Street", "Benton Dr", "95051", "Nebraska", "Nashville", Country.US, "6556265666", "1234",
            PaymentType.PAYPAL.getValue(), null, null, null, null);
        purchaseOrder.setBillingInformation(billingInformation);

        // Set payment info.
        final Recorder recorder = new Recorder();
        recorder.setState(Status.APPROVED.toString());
        payment = new Payment();
        payment.setConfigId(paymentGatewayId);
        payment.setRecorder(recorder);
        purchaseOrder.setPayment(payment);
        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * This method fulfill a PO by calling FulfillmentCallback API by getting the Fulfillment_GroupId from the given PO
     * This method assumes that the PO contains only Legacy Line Items
     *
     * @param chargedPO purchaseOrder entity
     * @param fulfillmentStatus status of fulfillment Possible values are Created/Error
     * @return OrderResponse IN case of success/ HttpError in case of failure of the fulfillmentCallback API
     */
    public <T extends PelicanPojo> T fulfillRequest(final PurchaseOrder chargedPO,
        final FulfillmentCallbackStatus fulfillmentStatus) {

        T responseEntity = null;

        // Fulfill only if not fulfilled yet
        if (chargedPO.getFulFillmentStatus() != FulfillmentGroup.FulFillmentStatus.FULFILLED) {
            final String PO_ID = chargedPO.getId();
            LOGGER.info("PO ID :" + PO_ID);
            Util.waitInSeconds(TimeConstants.MINI_WAIT);
            for (int i = 0; i < chargedPO.getFulFillmentGroups().getFulfillmentGroups().size(); i++) {
                if (chargedPO.getFulFillmentGroups().getFulfillmentGroups().get(i).getStrategy()
                    .equals(FulFillmentStrategy.LEGACY)) {
                    // Getting the FulfillmentGroupId
                    final String fulfillmentGroupId =
                        chargedPO.getFulFillmentGroups().getFulfillmentGroups().get(i).getId();
                    LOGGER.info("FulfillmentGroupId :" + fulfillmentGroupId);
                    final Root testRoot = new Root();
                    final Order newOrder = new Order();
                    newOrder.setExternalRefNumber(PO_ID + "-" + fulfillmentGroupId);
                    newOrder.setOrderNumber(PO_ID + "-" + fulfillmentGroupId);
                    newOrder.setPODate(DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.AUDIT_LOG_DATE_FORMAT));
                    newOrder.setPONumber(PO_ID + "-" + fulfillmentGroupId);
                    newOrder.setStatus(fulfillmentStatus.toString());

                    final ListOfLineItem listOfLineItem = new ListOfLineItem();
                    final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItemsList =
                        chargedPO.getLineItems().getLineItems();
                    int countOfLegacyLineItems = 0;
                    final List<Integer> indexOfLegacyLineItemsList = new ArrayList<>();
                    for (int k = 0; k < lineItemsList.size(); k++) {
                        if (lineItemsList.get(k).getOffering().getOfferingResponse().getfulfillmentGroupId()
                            .equalsIgnoreCase(fulfillmentGroupId)) {
                            countOfLegacyLineItems++;
                            indexOfLegacyLineItemsList.add(k);
                        }

                    }
                    LOGGER.info("Number of LineItems to be fulfilled : " + countOfLegacyLineItems);
                    final List<LineItem> lineItems = new ArrayList<>();

                    for (final Integer anIndexOfLegacyLineItemsList : indexOfLegacyLineItemsList) {

                        final LineItem lineItem = new LineItem();
                        final ListOfCurrentAsset listOfCurrentAsset = new ListOfCurrentAsset();
                        final Asset asset = new Asset();
                        // Setting a randum serial number
                        asset.setSerialNumber("AUTO_" + RandomStringUtils.randomAlphabetic(8));
                        listOfCurrentAsset.setAsset(asset);
                        lineItem.setListOfCurrentAsset(listOfCurrentAsset);
                        lineItem.setExternalRefNumber("" + anIndexOfLegacyLineItemsList);
                        lineItems.add(lineItem);
                    }
                    listOfLineItem.setListOfLineItems(lineItems);
                    newOrder.setListOfLineItem(listOfLineItem);
                    testRoot.setOrder(newOrder);
                    responseEntity = resource.fulfillmentCallBackClient().fulfillPurchaseOrder(testRoot);
                } else {
                    LOGGER.info("NOT LEGACY, SO NOT FULFILLED");
                }
            }
        }
        return responseEntity;
    }

    /**
     * Method processes the purchase order to different order state with different Final EC Status
     */
    public void processPurchaseOrderWithECStatus(final OrderCommand orderCommand, final String purchaseOrderId,
        final ECStatus finalEcStatus) {
        final PurchaseOrderCommand purchaseOrderCommand = new PurchaseOrderCommand();
        purchaseOrderCommand.setCommandType(orderCommand.toString());
        purchaseOrderCommand.setFinalExportControlStatus(finalEcStatus.getName());
        resource.processPurchaseOrder().process(purchaseOrderCommand, purchaseOrderId);
    }

    /**
     * Method processes the purchase order with final EC Status and Decline Reason
     */
    public <T extends PelicanPojo> T processPurchaseOrderWithDeclineReason(final OrderCommand orderCommand,
        final String purchaseOrderId, final DeclineReason declineReason) {
        final PurchaseOrderCommand purchaseOrderCommand = new PurchaseOrderCommand();
        purchaseOrderCommand.setCommandType(orderCommand.toString());
        purchaseOrderCommand.setDeclineReason(declineReason.name());
        return resource.processPurchaseOrder().process(purchaseOrderCommand, purchaseOrderId);
    }

    /**
     * Method processes the purchase order to different order state and return purchase order
     *
     * @return purchaseOrder
     */
    public <T extends PelicanPojo> T processPurchaseOrder(final OrderCommand orderCommand,
        final String purchaseOrderId) {
        final PurchaseOrderCommand purchaseOrderCommand = new PurchaseOrderCommand();
        purchaseOrderCommand.setCommandType(orderCommand.toString());
        return resource.processPurchaseOrder().process(purchaseOrderCommand, purchaseOrderId);
    }

    /**
     * This method is to process Pending purchase order with Paypal
     */
    public void processPendingPurchaseOrderWithPaypal(final BuyerUser buyerUser, final String purchaseOrderId) {

        final PurchaseOrderCommand purchaseOrderCommand = paymentProfileUtils.getPaypalPaymentForPO(buyerUser.getId(),
            PaymentProcessor.PAYPAL_EMEA.getValue(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getPaypalEmeaPaymentGatewayId(), OrderCommand.PENDING.toString());
        resource.processPurchaseOrder().process(purchaseOrderCommand, purchaseOrderId);
    }

    /**
     * This method is to process Pending purchase order with Paypal
     */
    public void processPaypalPurchaseOrderToDeclineWithECStatus(final BuyerUser buyerUser, final String purchaseOrderId,
        final ECStatus finalEcStatus) {

        final PurchaseOrderCommand purchaseOrderCommand = paymentProfileUtils.getPaypalPaymentForPO(buyerUser.getId(),
            PaymentProcessor.PAYPAL_EMEA.getValue(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getPaypalEmeaPaymentGatewayId(), OrderCommand.DECLINE.toString());
        purchaseOrderCommand.setFinalExportControlStatus(finalEcStatus.getName());
        resource.processPurchaseOrder().process(purchaseOrderCommand, purchaseOrderId);
    }

    /**
     * This method is to process Pending purchase order with Paypal
     */
    public HttpError processPendingPurchaseOrderWithPaypal(final BuyerUser user, final String purchaseOrderId,
        final String paymentGatewayId, final String paymentProcessorForPayment,
        final String paymentProcessorForPaymentProfile) {
        final PurchaseOrderCommand purchaseOrderCommand =
            paymentProfileUtils.getPaypalPaymentForPO(user.getId(), paymentProcessorForPayment,
                paymentProcessorForPaymentProfile, paymentGatewayId, OrderCommand.PENDING.toString());

        return resource.processPurchaseOrder().process(purchaseOrderCommand, purchaseOrderId);
    }

    /**
     * This method creates purchase order id depending on the parameter passed
     *
     * @return purchaseOrder
     */
    public PurchaseOrder submitNewAcquisitionPurchaseOrder(final PaymentType paymentType, final String priceId,
        final BuyerUser buyerUser, final int quantity) {

        purchaseOrder = new PurchaseOrder();
        purchaseOrder = createPurchaseOrderWithLineItems(priceId, buyerUser, quantity, purchaseOrder);
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(), null,
            PaymentProcessor.BLUESNAP_NAMER.getValue(), PaymentProcessor.BLUESNAP_NAMER.getValue());
        // submitting purchase order
        purchaseOrder = resource.purchaseOrder().add(purchaseOrder);

        return purchaseOrder;
    }

    /**
     * This method creates purchase order id depending on the parameter passed
     *
     * @return purchaseOrder or HttpError
     */
    public Object getEntityWithOrderCommand(final PaymentType paymentType, final String priceId,
        final BuyerUser buyerUser, final int quantity, final OrderCommand orderCommand) {

        purchaseOrder = new PurchaseOrder();
        purchaseOrder = createPurchaseOrderWithLineItems(priceId, buyerUser, quantity, purchaseOrder);
        purchaseOrder.setOrderCommand(orderCommand.toString());
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
            PaymentProcessor.BLUESNAP_EMEA.getValue());
        // submitting purchase order

        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * This method creates Renewal purchase order id depending on the parameter passed
     *
     * @return Object of purchaseOrder in case of success
     */
    public Object getRenewalPurchaseOrderWithOrderCommand(final PaymentType paymentType, final String subscriptionId,
        final BuyerUser buyerUser, final OrderCommand orderCommand, final Boolean updateSubscriptionNBDByToday) {

        subscriptionRenewalRequest = new SubscriptionRenewalRequest();
        if (updateSubscriptionNBDByToday) {
            SubscriptionUtils.updateSubscriptionNBD(subscriptionId, resource, appFamily, environmentVariables,
                DateTimeUtils.getCurrentDate(PelicanConstants.DATE_WITH_TIME_ZONE));
        }
        subscriptionRenewalRequest.setSubscriptionId(subscriptionId);
        subscription = resource.subscription().getById(subscriptionId);
        currencyId = subscription.getNextBillingPriceCurrencyId();
        currencyName = subscription.getNextBillingPriceCurrencyName();
        subscriptionRenewalRequest.setCurrencyId(currencyId);
        subscriptionRenewalRequest.setCurrencyName(currencyName);
        subscriptionRenewal = new SubscriptionRenewal();
        subscriptionRenewal.setSubscriptionRenewalRequest(subscriptionRenewalRequest);
        lineItem = new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
        lineItem.setSubscriptionRenewal(subscriptionRenewal);
        lineItems = new LineItems();
        lineItems.setLineItems(Arrays.asList(lineItem));
        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setBuyerUser(buyerUser);
        purchaseOrder.setOrderCommand(orderCommand.toString());
        purchaseOrder.setLineItems(lineItems);
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getBluesnapNamerPaymentGatewayId(), PaymentProcessor.BLUESNAP_NAMER.getValue(),
            PaymentProcessor.BLUESNAP_NAMER.getValue());
        // submitting purchase order

        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * This method submits purchase order with different combination of payment config id and payment processor
     *
     * @return Purchase Order or HttpError
     */
    public <T extends PelicanPojo> T submitPurchaseOrderWithPaymentProcessorAndConfigId(final PaymentType paymentType,
        final String priceId, final BuyerUser buyerUser, final int quantity, final String paypalPaymentGatewayId,
        final String paypalPaymentProcessor, final String bluesnapPaymentGatewayId,
        final String bluesnapPaymentProcessorForPayment, final String bluesnapPaymentProcessorForPaymentProfile) {

        purchaseOrder = new PurchaseOrder();
        purchaseOrder = createPurchaseOrderWithLineItems(priceId, buyerUser, quantity, purchaseOrder);
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser, paypalPaymentGatewayId,
            paypalPaymentProcessor, bluesnapPaymentGatewayId, bluesnapPaymentProcessorForPayment,
            bluesnapPaymentProcessorForPaymentProfile);

        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * Submit New Acquisition Or Renewal Order and get Purchase Order Id.
     *
     * @param paymentType
     * @param priceQuantityMap
     * @param subscriptionIds
     * @param isNewAcquisition
     * @param updateSubscriptionNBDByToday
     * @param buyerUser
     * @return
     */
    public String submitPurchaseOrderAndGetId(final PaymentType paymentType,
        final HashMap<String, Integer> priceQuantityMap, final List<String> subscriptionIds,
        final boolean isNewAcquisition, final Boolean updateSubscriptionNBDByToday, final BuyerUser buyerUser) {

        String purchaseOrderId;
        if (isNewAcquisition) {
            // submit purchase order with charge command based on payment type
            if (paymentType == PaymentType.PAYPAL) {
                purchaseOrder =
                    submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceQuantityMap, false, buyerUser);
            } else if (paymentType == PaymentType.CREDIT_CARD) {
                purchaseOrder = submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, buyerUser);
            }
            purchaseOrderId = purchaseOrder.getId();

        } else {
            // submit purchase order with charge command based on payment type
            if (paymentType == PaymentType.PAYPAL) {
                purchaseOrder = submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIds, false,
                    PaymentType.PAYPAL, null, updateSubscriptionNBDByToday);
            } else if (paymentType == PaymentType.CREDIT_CARD) {
                purchaseOrder = submitAndProcessRenewalPurchaseOrder(buyerUser, subscriptionIds, false,
                    PaymentType.CREDIT_CARD, null, updateSubscriptionNBDByToday);
            }
            purchaseOrderId = purchaseOrder.getId();
        }
        // fulfill the request, since it is Meta
        fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        return purchaseOrderId;
    }

    /**
     * Method to create a BillingInformation entity
     *
     * @param debitType ACH/SEPA
     * @return BillingInformation
     */
    public static BillingInformation getBillingInformation(final String firstName, final String surname,
        final String companyName, final String street, final String street2, final String postalCode,
        final String stateProvince, final String city, final Country countryCode, final String phone,
        final String vatRegistrationId, final String paymentMethod, final String cardType, final String expDate,
        final String last4Digits, final String debitType) {

        final BillingInformation billingInfo = new BillingInformation();
        billingInfo.setFirstName(firstName);
        billingInfo.setSurname(surname);
        billingInfo.setCompanyName(companyName);
        billingInfo.setStreet(street);
        billingInfo.setStreet2(street2);
        billingInfo.setPostalCode(postalCode);
        billingInfo.setStateProvince(stateProvince);
        billingInfo.setCity(city);
        billingInfo.setCountryCode(countryCode);
        billingInfo.setPhone(phone);
        billingInfo.setVatRegistrationId(vatRegistrationId);
        billingInfo.setPaymentMethod(paymentMethod);
        if (debitType != null) {
            billingInfo.setDebitType(debitType);
        }
        if (cardType != null) {
            billingInfo.setCardType(cardType);
        }
        if (expDate != null) {
            billingInfo.setExpDate(expDate);
        }
        if (last4Digits != null) {
            billingInfo.setLast4Digits(last4Digits);
        }
        return billingInfo;
    }

    /**
     * Method to create a shippingInformation entity
     *
     * @return shippingDetails
     */
    public static Shipping getShippingInformation(final String firstName, final String lastName, final String street,
        final String street2, final String city, final StateProvince state, final String postalCode,
        final Country country) {
        final ShipTo shipTo = new ShipTo();
        shipTo.setFirstName(firstName);
        shipTo.setSurname(lastName);
        shipTo.setStreet(street);
        shipTo.setStreet2(street2);
        shipTo.setCity(city);
        shipTo.setState(state);
        shipTo.setPostalCode(postalCode);
        shipTo.setCountry(country);
        final Shipping shippingDetails = new Shipping();
        final ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setExternalKey("UPS-GROUND");
        shippingDetails.setShippingMethod(shippingMethod);
        shippingDetails.setShipTo(shipTo);
        return shippingDetails;
    }

    /**
     * This method adds Payment to the purchase order depending on Payment Profile type.
     *
     * @return PurchaseOrder
     */
    private PurchaseOrder addPaymentToPurchaseOrder(final PaymentType paymentType, final PurchaseOrder purchaseOrder,
        final BuyerUser buyerUser, final String paypalPaymentGatewayId, final String paypalPaymentProcessorForPayment,
        final String bluesnapPaymentGatewayId, final String bluesnapPaymentProcessorForPayment,
        final String bluesnapPaymentProcessorForPaymentProfile) {
        // Stored payment profile for Credit Order, or else if its Paypal, send reorder details
        if (paymentType == PaymentType.CREDIT_CARD) {
            purchaseOrder.setPayment(
                paymentProfileUtils.getCreditCardPayment(buyerUser.getId(), bluesnapPaymentProcessorForPayment,
                    bluesnapPaymentProcessorForPaymentProfile, bluesnapPaymentGatewayId));
        } else if (paymentType == PaymentType.PAYPAL) {
            final Recorder recorder = new Recorder();
            recorder.setState(Status.APPROVED.toString());
            payment = new Payment();
            payment.setConfigId(paypalPaymentGatewayId);
            payment.setPaymentProcessor(paypalPaymentProcessorForPayment);
            payment.setRecorder(recorder);
            purchaseOrder.setPayment(payment);
        }
        return purchaseOrder;
    }

    /**
     * This method creates Purchase Order with line items, buyeruser, order command
     */
    public PurchaseOrder createPurchaseOrderWithLineItems(final String priceId, final BuyerUser buyerUser,
        final int quantity, final PurchaseOrder purchaseOrder) {

        offeringRequest = new OfferingRequest();
        offeringRequest.setPriceId(priceId);
        offeringRequest.setQuantity(quantity);
        offering = new Offering();
        offering.setOfferingRequest(offeringRequest);
        lineItem = new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
        lineItem.setOffering(offering);
        lineItems = new LineItems();
        lineItems.setLineItems(Arrays.asList(lineItem));
        purchaseOrder.setBuyerUser(buyerUser);
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());
        purchaseOrder.setLineItems(lineItems);

        return purchaseOrder;
    }

    /**
     * Helper function for submitting Purchase Order with Line Items and Payment Profile.
     *
     * @param lineItems - List of LineItems
     * @param paymentType - Payment profile type
     * @param buyerUser
     * @return Purchase order object
     * @throws ClientException API Exception
     */
    public <T extends PelicanPojo> T submitAndProcessPurchaseOrderWithLineItems(
        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItems, final PaymentType paymentType,
        final BuyerUser buyerUser) throws ClientException {

        // Setup Line Items.
        final LineItems newLineItems = new LineItems();
        newLineItems.setLineItems(lineItems);

        // Create Purchase Order object with Line Items and buyer User.
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setBuyerUser(buyerUser);
        purchaseOrder.setLineItems(newLineItems);

        // Set Authorized Command.
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());

        // Set Payment Profile for Purchase Order.
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
            PaymentProcessor.BLUESNAP_EMEA.getValue());

        // Place a PO by calling submitPurchaseOrder API
        final T responseEntity = resource.purchaseOrder().add(purchaseOrder);
        if (responseEntity instanceof HttpError) {
            return responseEntity;
        }

        final PurchaseOrder resultPO = (PurchaseOrder) responseEntity;

        // Process the PO with PENDING command
        if (paymentType == PaymentType.CREDIT_CARD) {
            processPurchaseOrder(OrderCommand.PENDING, resultPO.getId());
        } else {
            processPendingPurchaseOrderWithPaypal(buyerUser, resultPO.getId());
        }

        // Process the PO with CHARGE command
        final PurchaseOrderCommand chargePurchaseOrderCommand = new PurchaseOrderCommand();
        chargePurchaseOrderCommand.setCommandType(OrderCommand.CHARGE.toString());

        return resource.processPurchaseOrder().process(chargePurchaseOrderCommand, resultPO.getId());
    }

    /**
     * Helper method for creating Line item.
     *
     * @param priceId
     * @param quantity
     * @param promotionId
     * @param taxAmount
     * @return LineItem
     */
    public com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem createOfferingLineItem(final String priceId,
        final int quantity, final String promotionId, final String taxAmount) {

        offeringRequest = new OfferingRequest();
        offeringRequest.setPriceId(priceId);
        offeringRequest.setQuantity(quantity);
        offering = new Offering();
        offering.setOfferingRequest(offeringRequest);

        lineItem = new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();
        lineItem.setOffering(offering);

        if (promotionId != null) {
            final PromotionReference promotionReference = new PromotionReference();
            promotionReference.setId(promotionId);
            final PromotionReferences promotionReferences = new PromotionReferences();
            promotionReferences.setPromotionReference(promotionReference);
            lineItem.setPromotionReferences(promotionReferences);
        }

        if (taxAmount != null) {
            final AdditionalFees addFees = getAdditionalFees(taxAmount);
            lineItem.setAdditionalFees(addFees);
        }
        return lineItem;
    }

    /**
     * Create AdditionalFees Object using Tax amount.
     *
     * @param taxAmount
     * @return returns AdditionalFees Object.
     */
    private AdditionalFees getAdditionalFees(final String taxAmount) {
        final AdditionalFee addFee = new AdditionalFee();
        addFee.setAmount(taxAmount);
        addFee.setFeeCollectorId(environmentVariables.getFeeCollectorId());
        addFee.setTaxPayer("BUYER");
        addFee.setTaxIncludedInBasePrice("false");
        addFee.setCurrencyId(4);
        addFee.setType("TAX");

        final AdditionalFees addFees = new AdditionalFees();
        addFees.setAdditionalFee(addFee);

        return addFees;
    }

    /**
     * This method submits purchase order for subscription extension request
     *
     * @param paramsMap
     * @param paymentType
     * @param orderCommand
     * @param taxAmountList
     * @param buyerUser
     * @return Object
     */
    public Object submitSubscriptionExtensionPurchaseOrder(final Map<String, List<String>> paramsMap,
        final PaymentType paymentType, final OrderCommand orderCommand, final List<String> taxAmountList,
        final BuyerUser buyerUser) {

        // Create Purchase Order object with Line Items and buyer User.
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setBuyerUser(buyerUser);
        // Set order Command.
        purchaseOrder.setOrderCommand(orderCommand.toString());

        // Setup Line Items.
        final LineItems lineItems = new LineItems();
        lineItems.setLineItems(createSubscriptionExtensionLineItem(paramsMap, taxAmountList));
        purchaseOrder.setLineItems(lineItems);

        // Set Payment Profile for Purchase Order.
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalEmeaPaymentGatewayId(), PaymentProcessor.PAYPAL_EMEA.getValue(),
            environmentVariables.getBluesnapEmeaPaymentGatewayId(), PaymentProcessor.BLUESNAP_EMEA.getValue(),
            PaymentProcessor.BLUESNAP_EMEA.getValue());

        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * This method prepares line items with subscription extension request
     *
     * @param paramsMap
     * @param taxAmountList
     * @return List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem>
     */
    private List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> createSubscriptionExtensionLineItem(
        final Map<String, List<String>> paramsMap, final List<String> taxAmountList) {

        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItemsList = new ArrayList<>();

        // Go through the size of key list
        for (int i = 0; i < paramsMap.get(paramsMap.keySet().iterator().next()).size(); i++) {

            final com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem lineItem =
                new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();

            final SubscriptionExtension subscriptionExtension = new SubscriptionExtension();

            final SubscriptionExtension.SubscriptionExtensionRequest subscriptionExtensionRequest =
                new SubscriptionExtension.SubscriptionExtensionRequest();

            if (paramsMap.containsKey(LineItemParams.SUBSCRIPTION_ID.getValue())) {
                subscriptionExtensionRequest
                    .setSubscriptionId(paramsMap.get(LineItemParams.SUBSCRIPTION_ID.getValue()).get(i));
            }
            if (paramsMap.containsKey(LineItemParams.QUANTITY.getValue())) {
                subscriptionExtensionRequest.setQuantity(paramsMap.get(LineItemParams.QUANTITY.getValue()).get(i));
            }
            if (paramsMap.containsKey(LineItemParams.PRICE_ID.getValue())) {
                subscriptionExtensionRequest.setPriceId(paramsMap.get(LineItemParams.PRICE_ID.getValue()).get(i));
            }
            if (paramsMap.containsKey(LineItemParams.SUBSCRIPTION_RENEWAL_DATE.getValue())) {
                subscriptionExtensionRequest.setSubscriptionRenewalDate(
                    paramsMap.get(LineItemParams.SUBSCRIPTION_RENEWAL_DATE.getValue()).get(i));
            }
            if (paramsMap.containsKey(LineItemParams.TARGET_RENEWAL_DATE.getValue())) {
                subscriptionExtensionRequest
                    .setTargetRenewalDate(paramsMap.get(LineItemParams.TARGET_RENEWAL_DATE.getValue()).get(i));
            }
            subscriptionExtension.setSubscriptionExtensionRequest(subscriptionExtensionRequest);
            lineItem.setSubscriptionExtension(subscriptionExtension);
            if (taxAmountList != null && taxAmountList.get(i) != null) {
                final AdditionalFees addFees = getAdditionalFees(taxAmountList.get(i));
                lineItem.setAdditionalFees(addFees);
            }
            lineItemsList.add(lineItem);
        }
        return lineItemsList;
    }

    /**
     * This method submits PO for single/mutiple line items
     *
     * @param paramsMap
     * @param paymentType
     * @param storeExternalKey
     * @param updateSubscriptionNBDByToday
     * @param buyerUser
     * @return Object
     */
    public Object submitSubscriptionRenewalPurchaseOrder(final Map<String, List<String>> paramsMap,
        final PaymentType paymentType, final String storeExternalKey, final Boolean updateSubscriptionNBDByToday,
        final BuyerUser buyerUser) {

        // Create Purchase Order object with Line Items and buyer User.
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setStoreExternalKey(storeExternalKey);
        purchaseOrder.setBuyerUser(buyerUser);
        // Set order Command.
        purchaseOrder.setOrderCommand(OrderCommand.AUTHORIZE.toString());

        // Setup Line Items for subscriptionRenewals
        final LineItems lineItems = new LineItems();
        lineItems.setLineItems(createSubscriptionRenewalLineItem(paramsMap, updateSubscriptionNBDByToday));
        purchaseOrder.setLineItems(lineItems);

        // Set Payment Profile for Purchase Order.
        purchaseOrder = addPaymentToPurchaseOrder(paymentType, purchaseOrder, buyerUser,
            environmentVariables.getPaypalNamerPaymentGatewayId(), PaymentProcessor.PAYPAL_NAMER.getValue(),
            environmentVariables.getBluesnapNamerPaymentGatewayId(), PaymentProcessor.BLUESNAP_NAMER.getValue(),
            PaymentProcessor.BLUESNAP_NAMER.getValue());

        return resource.purchaseOrder().add(purchaseOrder);
    }

    /**
     * This method sets line items for multiple subscription renewal requests
     *
     * @param paramsMap
     * @param updateSubscriptionNBDByToday
     * @return List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem>
     */
    private List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> createSubscriptionRenewalLineItem(
        final Map<String, List<String>> paramsMap, final Boolean updateSubscriptionNBDByToday) {

        final List<com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem> lineItemsList = new ArrayList<>();

        // Go through the size of key list
        for (int i = 0; i < paramsMap.get(paramsMap.keySet().iterator().next()).size(); i++) {

            final com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem lineItem =
                new com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem();

            final SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal();

            final SubscriptionRenewal.SubscriptionRenewalRequest subscriptionRenewalRequest =
                new SubscriptionRenewal.SubscriptionRenewalRequest();

            if (paramsMap.containsKey(LineItemParams.SUBSCRIPTION_ID.getValue())) {
                if (updateSubscriptionNBDByToday) {
                    SubscriptionUtils.updateSubscriptionNBD(
                        paramsMap.get(LineItemParams.SUBSCRIPTION_ID.getValue()).get(i), resource, appFamily,
                        environmentVariables, DateTimeUtils.getCurrentDate(PelicanConstants.DATE_WITH_TIME_ZONE));
                }
                subscriptionRenewalRequest
                    .setSubscriptionId(paramsMap.get(LineItemParams.SUBSCRIPTION_ID.getValue()).get(i));
            }
            if (paramsMap.containsKey(LineItemParams.CURRENCY_NAME.getValue())) {
                subscriptionRenewalRequest
                    .setCurrencyName(paramsMap.get(LineItemParams.CURRENCY_NAME.getValue()).get(i));
            }
            if (paramsMap.containsKey(LineItemParams.CURRENCY_ID.getValue())) {
                subscriptionRenewalRequest.setCurrencyId(paramsMap.get(LineItemParams.CURRENCY_ID.getValue()).get(i));
            }
            subscriptionRenewal.setSubscriptionRenewalRequest(subscriptionRenewalRequest);
            lineItem.setSubscriptionRenewal(subscriptionRenewal);
            // Adding promotion to renewal order
            if (paramsMap.containsKey(LineItemParams.PROMOTION_REFERENCE.getValue())
                && paramsMap.get(LineItemParams.PROMOTION_REFERENCE.getValue()).get(i) != null) {
                final PromotionReferences promotionReferences = new PromotionReferences();
                final PromotionReference promotionReference = new PromotionReference();
                promotionReference.setId(paramsMap.get(LineItemParams.PROMOTION_REFERENCE.getValue()).get(i));
                promotionReferences.setPromotionReference(promotionReference);
                lineItem.setPromotionReferences(promotionReferences);
            }
            lineItemsList.add(lineItem);
        }
        return lineItemsList;
    }

    /**
     * ** This method submits a PO with promotion. This method submits a PO with promotion
     *
     * @param createdPromo - Promotion corresponding to the basic offering
     * @param offerings - Basic offering
     * @param buyerUser
     * @param isMeta
     * @param lineItemQuantity @return PurchaseOrder or HttpError
     */
    public List<PurchaseOrder> getFulfilledPurchaseOrderWithPromotion(final JPromotion createdPromo,
        final Offerings offerings, final int count, final BuyerUser buyerUser, final boolean isMeta,
        final int lineItemQuantity) {

        final List<PurchaseOrder> purchaseOrdersList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final PromotionReferences promotionReferences = new PromotionReferences();
            final PromotionReference promotionReference = new PromotionReference();
            promotionReference.setId(createdPromo.getData().getId());
            promotionReferences.setPromotionReference(promotionReference);

            // Submit PO with AUTHORIZE,PENDING and CHARGE
            final Map<String, Integer> priceQuantityMap = new HashMap<>();
            final Map<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
            priceQuantityMap.put(offerings.getIncluded().getPrices().get(0).getId(), lineItemQuantity);
            pricePromoReferencesMap.put(offerings.getIncluded().getPrices().get(0).getId(), promotionReferences);
            final PurchaseOrder newPurchase = submitAndProcessNewAcquisitionPurchaseOrder(priceQuantityMap, false,
                PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, buyerUser);
            final String purchaseOrderId = newPurchase.getId();
            final String fulfillmentGroupId = newPurchase.getFulFillmentGroups().getFulfillmentGroups().get(0).getId();
            LOGGER.info("PO ID :" + purchaseOrderId);
            LOGGER.info("Fulfillment GroupId :" + fulfillmentGroupId);

            if (isMeta) {

                // Fulfillment request if it is meta or bic+meta combination
                fulfillRequest(newPurchase, FulfillmentCallbackStatus.Created);
            }
            purchaseOrdersList.add(newPurchase);
        }
        return purchaseOrdersList;
    }

    /**
     * This Util method is for running invoice job and waiting till number gets generated
     *
     * @param jobsResource
     * @param purchaseOrderId
     */
    public void runInvoiceJobAndWaitTillInvoiceNumberGetsGenerated(final JobsClient jobsResource,
        final String purchaseOrderId) {

        if (purchaseOrderId != null) {
            final JsonInvoiceNumberStatus response =
                jobsResource.invoiceNumbers(Util.getBasicAuthHeaderValue(environmentVariables.getInvoiceAdminUsername(),
                    environmentVariables.getInvoiceAdminPassword()));
            LOGGER.info("Triggers response: " + response);

            int count = 0;
            // Wait till invoice number is generated (60 seconds)
            String invoiceNumber;
            do {
                Util.waitInSeconds(TimeConstants.MINI_WAIT);
                invoiceNumber = resource.purchaseOrder().getById(purchaseOrderId).getInvoiceNumber();
                LOGGER.info("Waiting for Invoice Number to be generated");
                count++;
            } while (invoiceNumber == null && count < 12);
        }
    }

    /**
     * Submit purchase order using direct debit payment profile
     *
     * @param paymentMethod
     * @param paymentProcessor
     * @param priceId
     * @param buyerUser
     * @param quantity
     * @param billingInformation
     * @return Purchase order
     */
    public PurchaseOrder submitPurchaseOrderUsingDirectDebitPaymentProfile(final PaymentMethod paymentMethod,
        final String paymentProcessor, final String priceId, final BuyerUser buyerUser, final int quantity,
        final BillingInformation billingInformation) {
        purchaseOrder = new PurchaseOrder();
        purchaseOrder = createPurchaseOrderWithLineItems(priceId, buyerUser, quantity, purchaseOrder);

        purchaseOrder.setPayment(
            paymentProfileUtils.getDirectDebitPaymentProfile(buyerUser.getId(), paymentProcessor, paymentMethod));

        if (null != billingInformation) {
            purchaseOrder.setBillingInformation(billingInformation);
        }
        purchaseOrder = resource.purchaseOrder().add(purchaseOrder);

        return purchaseOrder;
    }

    /**
     * Change Purchase order Status to Pending.
     * <p>
     * Note:This is done to overcome issue when order placed via API becomes fail.
     */
    public void updatePurchaseOrderStatus(final String purchaseOrderId) {

        DbUtils.updateTableInDb("purchase_order", "fulfillment_status", "2", "id", purchaseOrderId,
            environmentVariables);
    }

    /**
     * Change legacy fulfillment status to Pending. Note:This is done to overcome issue when order placed via API
     * becomes fail.
     *
     * @param fulFillmentStatus
     * @param fulfillmentGroupIndex
     */
    public void updateFulfillmentGroupStatus(final String purchaseOrderId, final FulFillmentStatus fulFillmentStatus,
        final int fulfillmentGroupIndex) {
        // Get Payload from DB.
        final String payload = DbUtils.selectQuery("select PAYLOAD from purchase_order where id = " + purchaseOrderId,
            "PAYLOAD", environmentVariables).get(0);

        // Set fulfillmentGroup status to Pending in payload
        final String updatedPayload = updatePayload(payload, fulFillmentStatus, fulfillmentGroupIndex);

        // Update payload to DB.
        DbUtils.updateTableInDb("purchase_order", "PAYLOAD", "'" + updatedPayload + "'", "id", purchaseOrderId,
            environmentVariables);
    }

    /**
     * Update Purchase Order Payload
     *
     * @param index
     *
     * @return updatedPayload
     */
    private String updatePayload(final String payload, final FulFillmentStatus fulFillmentStatus, final int index) {
        try {
            final Document document = Util.loadXMLFromString(payload);
            final Node fulfillmentGroup = document.getElementsByTagName("fulfillmentGroup").item(index - 1);
            final NamedNodeMap attr = fulfillmentGroup.getAttributes();
            final Node nodeAttr = attr.getNamedItem("status");
            nodeAttr.setTextContent(fulFillmentStatus.toString());

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return payload;
    }

    /**
     * Change Transaction Date For PO. Note:This is to simulate Pending PO transaction Date in Past.
     */
    public void updateTransactionDate(final String purchaseOrderId, final String transactionDate) {

        // Get Payload from DB.
        final String payload = DbUtils.selectQuery("select PAYLOAD from purchase_order where id = " + purchaseOrderId,
            "PAYLOAD", environmentVariables).get(0);

        // Set fulfillmentGroup status to Pending in payload
        final String updatedPayload = updatePayload(payload, transactionDate);

        // Update payload to DB.
        DbUtils.updateTableInDb("purchase_order", "PAYLOAD", "'" + updatedPayload + "'", "id", purchaseOrderId,
            environmentVariables);
    }

    /**
     * Update Purchase Order Payload submitAndProcessRenewalPurchaseOrder
     *
     * @return updatedPayload
     */
    private static String updatePayload(final String payload, final String transactionDate) {
        try {
            final Document document = Util.loadXMLFromString(payload);
            final XPath xPath = XPathFactory.newInstance().newXPath();
            final Node gatewayResponseNode =
                (Node) xPath.evaluate("/purchaseOrder/transactions/transaction/gatewayResponse[@state='PENDING']",
                    document, XPathConstants.NODE);

            /// purchaseOrder/transactions/transaction[@type='SALE']
            final NamedNodeMap attr = gatewayResponseNode.getAttributes();
            final Node nodeAttr = attr.getNamedItem("txnDate");
            nodeAttr.setTextContent(transactionDate);

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return payload;
    }
}
