package com.autodesk.bsm.pelican.api.subscription;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReference;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.LineItem.PromotionReferences;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Get Subscription By Id JSON api tests
 *
 * @author jains
 */
public class GetSubscriptionByIdJsonTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private static final Date currentDateTimeStamp = DateTimeUtils.convertStringToDate(
        DateTimeUtils.getCurrentDate(PelicanConstants.DB_DATE_FORMAT), PelicanConstants.DB_DATE_FORMAT);
    private static final int QUANTITY = 2;
    private String subscriptionId;
    private Offerings offering;
    private String storedPaymentProfileId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        offering = createOffering();
        final String priceIdForBic = offering.getIncluded().getPrices().get(0).getId();
        final JPromotion promotion = createPromotion(offering);

        final PurchaseOrder purchaseOrder = createPurchaseOrder(priceIdForBic, promotion);
        storedPaymentProfileId = purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId();
        subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
    }

    /**
     * This method tests get subscription by id.
     *
     * @throws ParseException
     */
    @Test
    public void testGetSubscriptionById() throws ParseException {
        final JSubscription subscription = resource.subscriptionJson().getSubscription(subscriptionId,
            "offering.entitlements", PelicanConstants.CONTENT_TYPE);
        AssertionUtilsForGetSubscription.assertionsForOffering(subscription.getIncluded().getOffering(), offering,
            assertionErrorList);
        AssertionUtilsForGetSubscription.assertionsForEntitlements(
            subscription.getIncluded().getOffering().getOneTimeEntitlements(),
            offering.getOfferings().get(0).getOneTimeEntitlements(), assertionErrorList);
        AssertionUtilsForGetSubscription.assertionsForBillingPlan(subscription.getIncluded().getBillingPlan(),
            offering.getIncluded().getBillingPlans().get(0), assertionErrorList);
        AssertionUtilsForGetSubscription.assertionsForPrice(subscription.getIncluded().getPrice(),
            offering.getIncluded().getPrices().get(0), assertionErrorList);
        AssertionUtilsForGetSubscription.assertionsForSubscriptionData(subscription.getData(), offering, Status.ACTIVE,
            storedPaymentProfileId, offering.getIncluded().getPrices().get(0), QUANTITY, currentDateTimeStamp,
            offering.getIncluded().getBillingPlans().get(0), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    private PurchaseOrder createPurchaseOrder(final String priceId, final JPromotion promotion) {
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(priceId, QUANTITY);

        final HashMap<String, PromotionReferences> pricePromoReferencesMap = new HashMap<>();
        final PromotionReferences promotionReferences = new PromotionReferences();
        final PromotionReference promotionReference = new PromotionReference();
        promotionReference.setId(promotion.getData().getId());
        promotionReferences.setPromotionReference(promotionReference);
        pricePromoReferencesMap.put(priceId, promotionReferences);

        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final PurchaseOrder purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(
            priceQuantityMap, false, PaymentType.CREDIT_CARD, pricePromoReferencesMap, null, null);
        final PurchaseOrder getPurchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
        return getPurchaseOrder;
    }

    private Offerings createOffering() {
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        final Offerings bicOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        // add Entitlements
        final String bicCommercialEntitlementId1 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicOfferings.getOfferings().get(0).getId(), null, null, true);
        final List<Item> bicCommercialEntitlementItemList = new ArrayList<>();
        bicCommercialEntitlementItemList.add(resource.item().getItem(bicCommercialEntitlementId1));
        final String bicCommercialEntitlementId2 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicOfferings.getOfferings().get(0).getId(), null, null, true);
        bicCommercialEntitlementItemList.add(resource.item().getItem(bicCommercialEntitlementId2));
        final Offerings offering = resource.offerings().getOfferingById(bicOfferings.getOfferings().get(0).getId(),
            "offers,prices,entitlements");
        return offering;
    }

    private JPromotion createPromotion(final Offerings offering) {
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final Double discount = 10.0;
        final JPromotion createdPromo =
            promotionUtils.getDiscountPercentPromo(getStoreUs(), offering, promotionUtils.getRandomPromoCode(), false,
                Status.ACTIVE, discount, DateTimeUtils.getUTCFutureExpirationDate(), 2);
        return createdPromo;
    }
}
