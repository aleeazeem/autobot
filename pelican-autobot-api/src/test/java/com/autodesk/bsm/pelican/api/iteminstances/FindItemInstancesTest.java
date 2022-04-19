package com.autodesk.bsm.pelican.api.iteminstances;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.ItemInstancesClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstances;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * This test class test "Find Item Instances" API.
 *
 * @author jains
 */

public class FindItemInstancesTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private PurchaseOrderUtils purchaseOrderUtils;
    private ItemInstances itemInstances;
    private HashMap<String, String> requestParameters;
    private String bicCommercialPriceId;
    private String userExternalKey;
    private String bicTrialSubscriptionOfferExternalKey;
    private String bicNonCommercialSubscriptionOfferExternalKey;
    private Subscription subscription;
    private String bicTrialEntitlement1;
    private String bicNonCommercialEntitlement1;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        // Set up the environment and login to Admin Tool
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());

        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        // create Bic Commercial Subscription Plan and add entitlements to it
        final Offerings bicCommercialOfferings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        final String bicCommercialOfferingsId = bicCommercialOfferings.getOfferings().get(0).getId();
        bicCommercialPriceId = bicCommercialOfferings.getIncluded().getPrices().get(0).getId();

        // create Meta Commercial Subscription Plan and add entitlements to it
        final Offerings metaCommercialOfferings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.SEMIYEAR, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM);

        final String metaCommercialOfferingsId = metaCommercialOfferings.getOfferings().get(0).getId();

        // create Entitlements for BIC Commercial Subscription Plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicCommercialOfferingsId, null, null, true);

        // create Entitlements for Meta Commercial Subscription Plan
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaCommercialOfferingsId, null, null, true);

        // create Bic Trial Subscription Plan and add entitlements to it
        final Offerings bicTrialOfferings = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.TRL);

        final String bicTrialOfferingsId = bicTrialOfferings.getOfferings().get(0).getId();
        bicTrialSubscriptionOfferExternalKey =
            bicTrialOfferings.getIncluded().getBillingPlans().get(0).getExternalKey();

        // create Entitlements for BIC Trial Subscription Plan
        bicTrialEntitlement1 =
            subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicTrialOfferingsId, null, null, true);

        // create Bic Non Commercial Subscription Plan and add entitlements to
        // it
        final Offerings bicNonCommercialOfferings =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.NCM);

        final String bicNonCommercialOfferingsId = bicNonCommercialOfferings.getOfferings().get(0).getId();
        bicNonCommercialSubscriptionOfferExternalKey =
            bicNonCommercialOfferings.getIncluded().getBillingPlans().get(0).getExternalKey();

        // create Entitlements for BIC Non Commercial Subscription Plan
        bicNonCommercialEntitlement1 = subscriptionPlanApiUtils
            .helperToAddEntitlementToSubscriptionPlan(bicNonCommercialOfferingsId, null, null, true);

        userExternalKey = "User_" + RandomStringUtils.randomAlphabetic(5);

    }

    /**
     * This method tests item instances are NOT created for BIC Commercial Subscription.
     */
    @Test
    public void testItemInstancesAreNotCreatedForBicCommercialSubscription() {

        // submit a purchase order to create a commercial subscription
        final HashMap<String, Integer> priceQuantityMap = new HashMap<>();
        priceQuantityMap.put(bicCommercialPriceId, 2);
        final String purchaseOrderId =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null).getId();
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        final String subscriptionIdForBicCommercial =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // send the request to get item instances
        requestParameters = new HashMap<>();
        requestParameters.put(ItemInstancesClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBicCommercial);
        itemInstances = resource.itemInstances().getItemInstances(requestParameters);

        AssertCollector.assertThat("There should NOT be any item instances for BIC Commercial Subscription",
            itemInstances.getItemInstances(), equalTo(null), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests item instances are created for BIC Trial Subscription.
     */
    @Test
    public void testItemInstancesAreCreatedForBicTrialSubscription() {

        // create trial subscription
        subscription = resource.subscription().add(userExternalKey, bicTrialSubscriptionOfferExternalKey, Currency.USD);
        final String subscriptionIdForBicTrial = subscription.getId();

        // send the request to get item instances
        requestParameters = new HashMap<>();
        requestParameters.put(ItemInstancesClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBicTrial);
        itemInstances = resource.itemInstances().getItemInstances(requestParameters);

        AssertCollector.assertThat("There should be item instances for BIC Trial Subscription",
            itemInstances.getItemInstances().size(), is(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect item id1", itemInstances.getItemInstances().get(0).getItemId(),
            equalTo(bicTrialEntitlement1), assertionErrorList);
        helperForCommonAssertions(itemInstances, subscriptionIdForBicTrial);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests item instances are created for BIC Non Commercial Subscription.
     */
    @Test
    public void testItemInstancesAreCreatedForBicNonCommercialSubscription() {

        // create non commercial subscription
        subscription =
            resource.subscription().add(userExternalKey, bicNonCommercialSubscriptionOfferExternalKey, Currency.USD);
        final String subscriptionIdForBicNonCommercial = subscription.getId();
        commonAssertionsForNoncommercialSubscriptions(subscriptionIdForBicNonCommercial, bicNonCommercialEntitlement1);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a method which has common assertions for the owner and subscription id in the find item instances
     */
    private void helperForCommonAssertions(final ItemInstances itemInstances, final String subscriptionId) {
        AssertCollector.assertTrue("Item instances should not be null", itemInstances.getItemInstances().size() > 0,
            assertionErrorList);
        for (int i = 0; i < itemInstances.getItemInstances().size(); i++) {
            AssertCollector.assertThat("Incorrect owner external key",
                itemInstances.getItemInstances().get(i).getOwnerExternalKey(), equalTo(userExternalKey),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect subscription id",
                itemInstances.getItemInstances().get(i).getSubscriptionId(), equalTo(subscriptionId),
                assertionErrorList);
        }
    }

    /**
     * This method will have common assertions on subscription id and entitlements in the find item instances api
     */
    private void commonAssertionsForNoncommercialSubscriptions(final String subscriptionId, final String entitlement) {

        // send the request to get item instances
        requestParameters = new HashMap<>();
        requestParameters.put(ItemInstancesClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionId);
        requestParameters.put(ItemInstancesClient.Parameter.INCLUDE_EXPIRED.getName(), "true");
        itemInstances = resource.itemInstances().getItemInstances(requestParameters);

        AssertCollector.assertThat("There should be item instances for BIC/Meta Non Commercial Subscription",
            itemInstances.getItemInstances().size(), is(1), assertionErrorList);
        AssertCollector.assertThat("Incorrect item id", itemInstances.getItemInstances().get(0).getItemId(),
            equalTo(entitlement), assertionErrorList);
        helperForCommonAssertions(itemInstances, subscriptionId);

    }

}
