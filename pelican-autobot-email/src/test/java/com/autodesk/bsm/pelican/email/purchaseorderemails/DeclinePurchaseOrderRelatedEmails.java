package com.autodesk.bsm.pelican.email.purchaseorderemails;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.email.PelicanDefaultEmailValidations;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.DeclineReason;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionUtils;
import com.autodesk.bsm.pelican.util.Util;

import com.beust.jcommander.internal.Lists;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DeclinePurchaseOrderRelatedEmails extends BaseTestData {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private static final int quantity = 4;
    private static String priceIdForBic;
    private static String priceIdForMeta;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        // Set up the environment
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        // Creating 3 offerings for BIC, Meta and Perpetual
        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);
        final Offerings metaOfferings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        // Creating price id for BIC, Meta and Perpetual
        priceIdForBic = bicOfferings.getIncluded().getPrices().get(0).getId();
        priceIdForMeta = metaOfferings.getIncluded().getPrices().get(0).getId();

    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
    }

    /**
     * Test Method to verify Payment Error (Delinquency) Email Includes Subscription Id. Step 1. Place New Acquisition
     * Order. Step 2.change NBD in past by 3 Days. Step 3. Submit Renewal Order 4. Process Renewal Order for PENDING
     * followed by DECLINE.
     */
    @Test
    public void testDelinquentEmailIncludesSubscriptionId() {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceIdForBic, 2);
        final PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, getBuyerUser());
        // get subscription Id
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // change NBD in past by 3 Days, in order to simulate that on 4th Day it ll send Payment Error Mail.
        final String changedNextBillingDate = DateTimeUtils.changeDateFormat(DateTimeUtils.getNowMinusDays(3),
            PelicanConstants.DATE_FORMAT_WITH_SLASH, PelicanConstants.DATE_WITH_TIME_ZONE);

        // Changing DB for subscription table with above created date.
        SubscriptionUtils.updateSubscriptionNBD(subscriptionIdForBicCommercial, resource,
            getEnvironmentVariables().getAppFamily(), getEnvironmentVariables(), changedNextBillingDate);

        final PurchaseOrder renewalPurchaseOrder =
            purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(Lists.newArrayList(subscriptionIdForBicCommercial),
                false, PaymentType.CREDIT_CARD, null, false, getBuyerUser());

        final String renewalPurchaseOrderId = renewalPurchaseOrder.getId();

        // Processing Renewal order to PENDING State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, renewalPurchaseOrderId);

        // Processing Renewal order to DECLINE State.
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.DECLINE, renewalPurchaseOrderId);

        PelicanDefaultEmailValidations.paymentErrorEmailOnRenewal(subscriptionIdForBicCommercial,
            getBuyerUser().getEmail(), getEnvironmentVariables());
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * this test method tests declined orders email are being sent for new Acquisition orders.
     *
     * @param priceId
     * @param declineReason
     */
    @Test(dataProvider = "priceIdsAndExportControlReason")
    public void testDeclinedOrderNewAcquisitionEmailDueToExportControl(final String priceId,
        final DeclineReason declineReason) {
        PurchaseOrder purchaseOrderCreatedForBic =
            purchaseOrderUtils.submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, getBuyerUser(), 1);
        // process purchase order with pending and charge commands
        final PurchaseOrder purchaseOrderProcessForBic =
            purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderCreatedForBic.getId());
        purchaseOrderCreatedForBic = purchaseOrderUtils.processPurchaseOrderWithDeclineReason(OrderCommand.DECLINE,
            purchaseOrderProcessForBic.getId(), declineReason);

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        PelicanDefaultEmailValidations.declinedOrderEmailForNewAcquisition(priceId, getBuyerUser().getEmail(),
            getEnvironmentVariables(), purchaseOrderCreatedForBic.getId());
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * this test method tests declined orders email are being sent for new Renewal orders.
     *
     * @param priceId
     * @param declineReason
     */
    @Test(dataProvider = "priceIdsAndExportControlReason")
    public void testDeclinedOrderRenewalEmailDueToExportControl(final String priceId,
        final DeclineReason declineReason) {
        final HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(priceId, quantity);
        PurchaseOrder purchaseOrder = purchaseOrderUtils
            .submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, getBuyerUser());
        String subscriptionId;
        if (priceId.equals(priceIdForMeta)) {
            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        }
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        final List<String> listOfSubscriptions = new ArrayList<>();
        listOfSubscriptions.add(subscriptionId);
        final PurchaseOrder renewalPurchaseOrder = purchaseOrderUtils.submitRenewalPurchaseOrderWithPromos(
            listOfSubscriptions, false, PaymentType.CREDIT_CARD, null, true, getBuyerUser());
        purchaseOrderUtils.processPurchaseOrderWithDeclineReason(OrderCommand.DECLINE, renewalPurchaseOrder.getId(),
            declineReason);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        PelicanDefaultEmailValidations.declinedOrderEmailForRenewal(priceId, getBuyerUser().getEmail(),
            getEnvironmentVariables(), listOfSubscriptions);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This data provider is used to send only bic and meta price ids and decline Reason to the purchase order request
     */
    @DataProvider(name = "priceIdsAndExportControlReason")
    private static Object[][] getPriceIdsAndExportControlReason() {
        return new Object[][] { { priceIdForBic, DeclineReason.EXPORT_CONTROL_BLOCKED },
                { priceIdForMeta, DeclineReason.EXPORT_CONTROL_UNRESOLVED } };
    }
}
