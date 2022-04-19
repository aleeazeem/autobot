package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.LineItemParams;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests AUM admin emails when tenant buys Add Seats Order or New Acquisition order. This is a special class
 * with other system dependency. Initial set up needs to be done in Portal. 1. Create portal account(Oxygen Ids) with
 * the below email ids (class variables) 2. Login to AUM and invite/assign users, ex: as secondary admin, named party
 * user 3. Use the Oxygen Ids of the user and create Pelican User 4. Using Pelican User, submit the PO.
 *
 * @author Shweta Hegde
 */
public class AUMAdminEmailsTest extends BaseTestData {

    private PurchaseOrderUtils purchaseOrderUtils;
    private BuyerUser primaryAdminBuyerUser;
    private BuyerUser secondaryAdminBuyerUser;
    private BuyerUser namedPartyBuyerUser;
    private static final String PRIMARY_ADMIN_EMAIL_ID = "pelicantest.primary.admin@ssttest.net";
    private static final String SECONDARY_ADMIN_EMAIL_ID = "pelicantest.secondary.admin@ssttest.net";
    private static final String NAMED_USER_EMAIL_ID = "pelicantest.named.party@ssttest.net";

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getOtherAppFamilyId());
        Map<String, String> userParams = new HashMap<>();
        // Create Primary Admin User In Pelican
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.OTHER.getName());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), getEnvironmentVariables().getPrimaryAdminExternalKey());
        final UserUtils userUtils = new UserUtils();
        final User primaryAdminUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // build Primary Admin buyerUser object from environmentVariables
        primaryAdminBuyerUser = new BuyerUser();
        primaryAdminBuyerUser.setId(primaryAdminUser.getId());
        primaryAdminBuyerUser.setEmail(PRIMARY_ADMIN_EMAIL_ID);
        primaryAdminBuyerUser.setExternalKey(primaryAdminUser.getExternalKey());

        // Create Secondary Admin User In Pelican
        userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.OTHER.getName());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), getEnvironmentVariables().getSecondaryAdminExternalKey());
        final User secondaryAdminUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // build secondary Admin buyerUser object from environmentVariables
        secondaryAdminBuyerUser = new BuyerUser();
        secondaryAdminBuyerUser.setId(secondaryAdminUser.getId());
        secondaryAdminBuyerUser.setEmail(SECONDARY_ADMIN_EMAIL_ID);
        secondaryAdminBuyerUser.setExternalKey(secondaryAdminUser.getExternalKey());

        // Create named party User In Pelican
        userParams = new HashMap<>();
        userParams.put(UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.OTHER.getName());
        userParams.put(UserParameter.EXTERNAL_KEY.getName(), getEnvironmentVariables().getNamedPartyUserExternalKey());
        final User namedPartyUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());

        // build named party buyerUser object from environmentVariables
        namedPartyBuyerUser = new BuyerUser();
        namedPartyBuyerUser.setId(namedPartyUser.getId());
        namedPartyBuyerUser.setEmail(NAMED_USER_EMAIL_ID);
        namedPartyBuyerUser.setExternalKey(namedPartyUser.getExternalKey());

    }

    /**
     * This method tests 3 scenarios. 1. When primary admin buys add seats order, secondary admins receive copy of the
     * email 2. When secondary admin buys add seats order, secondary admins & primary admin receive copy of the email 3.
     * When named party user buys add seats order, secondary admins & primary admin receive copy of the email
     */
    @Test
    public void testAllAdminsReceiveEmailWhenATenantSubmitsAddSeatsOrder() {

        // submit a purchase order to create a commercial subscription
        PurchaseOrder purchaseOrder = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD,
            getEnvironmentVariables().getBicMonthlyPriceId(), primaryAdminBuyerUser, 1);
        final String purchaseOrderId = purchaseOrder.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrder = purchaseOrderUtils.processPurchaseOrder(PurchaseOrder.OrderCommand.CHARGE, purchaseOrderId);

        // get subscription
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        Map<String, String> subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(),
            getEnvironmentVariables().getBicMonthlyPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "5");

        // Submit Add Seats order with primary admin as buyeruser
        final PurchaseOrder purchaseOrderForAddedSeats1 =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, primaryAdminBuyerUser);

        final String addSeatsPurchaseOrderId1 = purchaseOrderForAddedSeats1.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId1);

        // Validate email that secondary admin and primary admin received email
        PelicanDefaultEmailValidations.orderCompleteForAddSeats(addSeatsPurchaseOrderId1, subscriptionId, 5, 6,
            purchaseOrderForAddedSeats1.getCreationTime(), getEnvironmentVariables(),
            Arrays.asList(PRIMARY_ADMIN_EMAIL_ID, SECONDARY_ADMIN_EMAIL_ID));

        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(),
            getEnvironmentVariables().getBicMonthlyPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "2");

        // Submit Add Seats order with secondary admin as buyeruser
        final PurchaseOrder purchaseOrderForAddedSeats2 =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, secondaryAdminBuyerUser);

        final String addSeatsPurchaseOrderId2 = purchaseOrderForAddedSeats2.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId2);

        // Validate email that primary and secondary admins received email
        PelicanDefaultEmailValidations.orderCompleteForAddSeats(addSeatsPurchaseOrderId2, subscriptionId, 2, 8,
            purchaseOrderForAddedSeats2.getCreationTime(), getEnvironmentVariables(),
            Arrays.asList(PRIMARY_ADMIN_EMAIL_ID, SECONDARY_ADMIN_EMAIL_ID));

        subscriptionQuantityMap = new HashMap<>();
        subscriptionQuantityMap.put(LineItemParams.SUBSCRIPTION_ID.getValue(), subscriptionId);
        subscriptionQuantityMap.put(LineItemParams.PRICE_ID.getValue(),
            getEnvironmentVariables().getBicMonthlyPriceId());
        subscriptionQuantityMap.put(LineItemParams.QUANTITY.getValue(), "1");

        // Submit Add Seats order with named party user as buyeruser
        final PurchaseOrder purchaseOrderForAddedSeats3 =
            purchaseOrderUtils.submitSubscriptionQuantityPurchaseOrder(Arrays.asList(subscriptionQuantityMap),
                PaymentType.CREDIT_CARD, OrderCommand.AUTHORIZE, null, namedPartyBuyerUser);

        final String addSeatsPurchaseOrderId3 = purchaseOrderForAddedSeats3.getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, addSeatsPurchaseOrderId3);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, addSeatsPurchaseOrderId3);

        // Validate email that primary admin, secondary admin and named party user received it
        PelicanDefaultEmailValidations.orderCompleteForAddSeats(addSeatsPurchaseOrderId3, subscriptionId, 1, 9,
            purchaseOrderForAddedSeats3.getCreationTime(), getEnvironmentVariables(),
            Arrays.asList(PRIMARY_ADMIN_EMAIL_ID, SECONDARY_ADMIN_EMAIL_ID, NAMED_USER_EMAIL_ID));
    }

    /**
     * This method tests 3 scenarios. 1. When primary admin buys new acquisition order, secondary admins receive copy of
     * the email 2. When secondary admin buys new acquisition order, secondary admins & primary admin receive copy of
     * the email 3. When named party user buys new acquisition order, secondary admins & primary admin receive copy of
     * the email
     */
    @Test
    public void testAllAdminsReceiveEmailWhenTenantSubmitsNewAcquisitionOrder() {

        // submit a purchase order to create a commercial subscription with primary admin as buyeruser
        final PurchaseOrder purchaseOrder1 = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            PaymentType.CREDIT_CARD, getEnvironmentVariables().getBicMonthlyPriceId(), primaryAdminBuyerUser, 1);
        final String purchaseOrderId1 = purchaseOrder1.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId1);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId1);

        // Validate email that primary admin and secondary admin received it
        PelicanDefaultEmailValidations.orderComplete(purchaseOrderId1, getEnvironmentVariables(), true,
            Arrays.asList(PRIMARY_ADMIN_EMAIL_ID, SECONDARY_ADMIN_EMAIL_ID));

        // Submit new acquisition order with secondary admin as buyeruser
        final PurchaseOrder purchaseOrder2 = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            PaymentType.CREDIT_CARD, getEnvironmentVariables().getBicMonthlyPriceId(), secondaryAdminBuyerUser, 2);
        final String purchaseOrderId2 = purchaseOrder2.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId2);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId2);

        // Validate email that primary admin and secondary admin received it
        PelicanDefaultEmailValidations.orderComplete(purchaseOrderId2, getEnvironmentVariables(), true,
            Arrays.asList(PRIMARY_ADMIN_EMAIL_ID, SECONDARY_ADMIN_EMAIL_ID));

        // Submit new acquisition order with named party user as buyeruser
        final PurchaseOrder purchaseOrder3 = purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(
            PaymentType.CREDIT_CARD, getEnvironmentVariables().getBicMonthlyPriceId(), namedPartyBuyerUser, 3);
        final String purchaseOrderId3 = purchaseOrder3.getId();

        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId3);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId3);

        // Validate email that primary admin, secondary admin and named party user received it
        PelicanDefaultEmailValidations.orderComplete(purchaseOrderId3, getEnvironmentVariables(), true,
            Arrays.asList(PRIMARY_ADMIN_EMAIL_ID, SECONDARY_ADMIN_EMAIL_ID, NAMED_USER_EMAIL_ID));
    }
}
