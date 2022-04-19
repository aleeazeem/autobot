package com.autodesk.bsm.pelican.api.userofferings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.OfferingsClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.clients.UserOfferingsClient;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementData;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.EditSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.FindSubscriptionPlanPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.SubscriptionPlanDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.PromotionUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;

import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a test class which is used to test the get user offerings api
 *
 * @author vineel
 */

public class GetUserOfferingsTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private String prideIdOfBic4;
    private Offerings bicOfferings1;
    private HashMap<String, Integer> offeringIdMap;
    private String subscriptionIdForBic;
    private String subscriptionIdWithOneOfferNoPrice;
    private SubscriptionOffer subscriptionOffer;
    private JStore storeFranceWithoutVat;
    private final String productLine = "AUTO_MAYA_" + RandomStringUtils.randomAlphabetic(6);
    private static final String ENTITLEMENTS = "entitlements";
    private static List<Item> itemsList;

    /*
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        final FindSubscriptionPlanPage findSubscriptionPlanPage = adminToolPage.getPage(FindSubscriptionPlanPage.class);

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        // Add Product Line
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());

        subscriptionPlanApiUtils.addProductLine(productLine);

        final JStore store2 =
            new StoreApiUtils(getEnvironmentVariables()).addStore(Status.ACTIVE, Country.FR, Currency.EUR, null, false);
        final String externalKeyOfPriceList2 = store2.getIncluded().getPriceLists().get(0).getExternalKey();

        // Creating offerings for BIC in the created 3 stores
        bicOfferings1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 500);
        // Add one time entitlement to plan
        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final Item item2 = featureApiUtils.addFeature(null, null, null);
        itemsList = new ArrayList<>();
        itemsList.add(item1);
        itemsList.add(item2);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            item1.getId(), null, true);
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOfferings1.getOfferings().get(0).getId(),
            item2.getId(), null, true);

        final Offerings bicOfferings2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 2,
            externalKeyOfPriceList2, 500);
        final Offerings bicOfferings3 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 3,
            getPricelistExternalKeyUk(), 500);
        final Offerings bicOfferings4 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 500);
        final Offerings bicOfferings5 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 500);
        Offerings bicOfferingsForNoPrice = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 500);

        // Add offering ids to a offering id map
        offeringIdMap = new HashMap<>();
        offeringIdMap.put(bicOfferings1.getOfferings().get(0).getId(), 1);
        offeringIdMap.put(bicOfferings2.getOfferings().get(0).getId(), 1);
        offeringIdMap.put(bicOfferings3.getOfferings().get(0).getId(), 1);
        offeringIdMap.put(bicOfferings4.getOfferings().get(0).getId(), 1);
        offeringIdMap.put(bicOfferings5.getOfferings().get(0).getId(), 1);
        offeringIdMap.put(bicOfferingsForNoPrice.getOfferings().get(0).getId(), 1);

        // add another offer with no price for bicOfferingsForNoPrice
        subscriptionOffer = subscriptionPlanApiUtils.addOffer(bicOfferingsForNoPrice.getOfferings().get(0).getId());
        bicOfferingsForNoPrice = resource.offerings().getOfferingById(
            bicOfferingsForNoPrice.getOfferings().get(0).getId(), PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);

        HashMap<String, Integer> priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(bicOfferingsForNoPrice.getIncluded().getPrices().get(0).getId(), 1);
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        PurchaseOrder purchaseOrderForBicWithOneOfferNoPrice =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        purchaseOrderForBicWithOneOfferNoPrice =
            resource.purchaseOrder().getById(purchaseOrderForBicWithOneOfferNoPrice.getId());
        subscriptionIdWithOneOfferNoPrice = purchaseOrderForBicWithOneOfferNoPrice.getLineItems().getLineItems().get(0)
            .getOffering().getOfferingResponse().getSubscriptionId();

        // Create StoreWide Bundle Promo bicOfferings4 & bicOfferings5 with 10%
        // discount
        final PromotionUtils promotionUtils = new PromotionUtils(getEnvironmentVariables());
        final List<BundlePromoOfferings> bundleOfBicOfferingsList = new ArrayList<>();
        bundleOfBicOfferingsList.add(promotionUtils.createBundlePromotionOffering(bicOfferings4, 2, true));
        bundleOfBicOfferingsList.add(promotionUtils.createBundlePromotionOffering(bicOfferings5, 2, true));

        // bundle storewide amount promotion creation - 10% discount
        promotionUtils.addBundlePromotion(PromotionType.DISCOUNT_PERCENTAGE, Lists.newArrayList(getStoreUs()),
            bundleOfBicOfferingsList, promotionUtils.getRandomPromoCode(), true, Status.ACTIVE, "10", null,
            DateTimeUtils.getUTCFutureExpirationDate(), 1, null, null);

        prideIdOfBic4 = bicOfferings4.getIncluded().getPrices().get(0).getId();
        // Retrieving the price ids for bic and meta
        final String priceIdForBicOffering1 = bicOfferings1.getIncluded().getPrices().get(0).getId();

        priceQuantityMap = new LinkedHashMap<>();
        priceQuantityMap.put(priceIdForBicOffering1, 1);
        PurchaseOrder purchaseOrderForBic =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceQuantityMap, false, null);
        purchaseOrderForBic = resource.purchaseOrder().getById(purchaseOrderForBic.getId());
        subscriptionIdForBic = purchaseOrderForBic.getLineItems().getLineItems().get(0).getOffering()
            .getOfferingResponse().getSubscriptionId();

        // Add EMEA store without VAT
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        storeFranceWithoutVat = storeApiUtils.addStore(Status.ACTIVE, Country.FR, Currency.EUR, null, false);
        final String pricelistExternalKeyOfStoreFrance =
            storeFranceWithoutVat.getIncluded().getPriceLists().get(0).getExternalKey();

        // Add active bic subscription without IC
        final Offerings bicOfferingWithoutIc = subscriptionPlanApiUtils.addSubscriptionPlan(
            pricelistExternalKeyOfStoreFrance, OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE,
            SupportLevel.ADVANCED, UsageType.COM);

        // get id of the offering
        String bicOfferingId = bicOfferingWithoutIc.getOfferings().get(0).getId();

        // add one more offer/billing plan
        SubscriptionOffer addedSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 1, Status.ACTIVE),
            bicOfferingId);

        // add price to the offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOffer(1000, pricelistExternalKeyOfStoreFrance, 2, 2), bicOfferingId,
            addedSubscriptionOffer.getData().getId());

        // add price to the offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOffer(1000, pricelistExternalKeyOfStoreFrance, 0, 1), bicOfferingId,
            addedSubscriptionOffer.getData().getId());

        // Add active bic subscription with IC
        final Offerings bicOfferingWithIc = subscriptionPlanApiUtils.addSubscriptionPlan(
            pricelistExternalKeyOfStoreFrance, OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE,
            SupportLevel.ADVANCED, UsageType.COM);

        // get id of the offering
        bicOfferingId = bicOfferingWithIc.getOfferings().get(0).getId();

        // add one more offer/billing plan
        addedSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 1, Status.ACTIVE),
            bicOfferingId);

        // add price to the offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOffer(1000, pricelistExternalKeyOfStoreFrance, 2, 2), bicOfferingId,
            addedSubscriptionOffer.getData().getId());

        // add price to the offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
            .helperToAddPricesToSubscriptionOffer(1000, pricelistExternalKeyOfStoreFrance, 0, 1), bicOfferingId,
            addedSubscriptionOffer.getData().getId());

        final SubscriptionPlanDetailPage subscriptionPlanDetailPage = findSubscriptionPlanPage
            .findSubscriptionPlanByExternalKey(bicOfferingWithIc.getOfferings().get(0).getExternalKey());
        final EditSubscriptionPlanPage editSubscriptionPlanPage =
            subscriptionPlanDetailPage.clickOnEditSubscriptionPlanButton();
        editSubscriptionPlanPage.editPackagingType(PackagingType.INDUSTRY_COLLECTION);
        editSubscriptionPlanPage.clickOnSave(false);

        // Add active bic subscription with VG
        final Offerings bicOfferingWithVg = subscriptionPlanApiUtils.addPlanWithProductLine(
            getProductLineExternalKeyMaya(), OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC,
            UsageType.COM, resource, RandomStringUtils.randomAlphanumeric(10), PackagingType.VG);

        final String bicOfferingIdForVg = bicOfferingWithVg.getOffering().getId();
        // add one more offer/billing plan
        addedSubscriptionOffer = subscriptionPlanApiUtils.addSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(null, BillingFrequency.YEAR, 1, Status.ACTIVE),
            bicOfferingIdForVg);

        // add price to the offer
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource,
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(1000, getPricelistExternalKeyUs(), 2, 2),
            bicOfferingIdForVg, addedSubscriptionOffer.getData().getId());

    }

    /**
     * This is a test case which tests the find user offerings api without providing any of the request parameters
     */
    @Test
    public void testGetUserOfferingsApiWithoutParameters() {
        final Map<String, String> params = new HashMap<>();
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(),
            equalTo("Either subscription id or offer external key is required."), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with invalid user external key
     */
    @Test
    public void testGetUserOfferingsApiWithInvalidExternalKey() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), "hahcscdcs");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(),
            equalTo("Either subscription id or offer external key is required."), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with valid user external key and offer external key
     * left empty
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKey() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(),
            equalTo("Either subscription id or offer external key is required."), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with valid user external key and non-matching
     * subscription id
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndNonMatchingSubscriptionId() {
        final String externalKey = "jains";
        final HashMap<String, String> newUserParams = new HashMap<>();
        newUserParams.put(UserParameter.APPLICATION_FAMILY.getName(), getEnvironmentVariables().getAppFamily());
        newUserParams.put(UserParameter.EXTERNAL_KEY.getName(), externalKey);
        final User newUser = new UserUtils().createPelicanUser(newUserParams, getEnvironmentVariables());
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), newUser.getExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("Invalid subscription for the specified user."),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with valid user external key and with invalid
     * subscription id
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndInvalidSubscriptionId() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.OFFER_EXT_KEY.getName(), "22112233222222");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("Invalid offer: 22112233222222"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with valid user external key and valid subscription id
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndValidSubscriptionId() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect number of offerings returned", offerings.getOfferings().size(),
            equalTo(offeringIdMap.size()), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            boolean found = true;
            if (offeringIdMap.get(offering.getId()) == null) {
                found = false;
            }
            AssertCollector.assertThat("Incorrect offering returned by the api response", found, equalTo(true),
                assertionErrorList);
        }
        validatePriceDiscountForBundledPromo(offerings);
        AssertCollector.assertThat("Incorrect meta offering response returned by the api",
            offerings.getMeta().getCurrentOffering().getId(), equalTo(bicOfferings1.getOfferings().get(0).getId()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with 2 offers, one offer has valid price and other
     * doesn't have price.
     */
    @Test
    public void testGetUserOfferingsApiWithOneOfferWithNoPrice() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdWithOneOfferNoPrice);
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect number of offerings returned", offerings.getOfferings().size(),
            equalTo(offeringIdMap.size()), assertionErrorList);
        boolean found = false;
        for (int i = 0; i < offerings.getOfferings().size(); i++) {
            if (offerings.getIncluded().getBillingPlans().get(i).getExternalKey()
                .equalsIgnoreCase(subscriptionOffer.getData().getExternalKey())) {
                found = true;
            }
        }
        AssertCollector.assertFalse("offering with no price should not be returned by the api response", found,
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which tests the entitlements and product line in get offerings api. It will return all
     * offerings in response if product line is comment between offerings and a offering which is associated with
     * provided subscription. In order to retrieve entitlements in response entitlement should be passed as included
     * parameter.
     */
    @Test
    public void testEntitlementsAndProductLineInGetUserOfferingsApi() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.INCLUDE.getName(), ENTITLEMENTS);
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect number of offerings returned", offerings.getOfferings().size(),
            equalTo(offeringIdMap.size()), assertionErrorList);
        for (int i = 0; i < offerings.getOfferings().size(); i++) {
            AssertCollector.assertThat("Incorrect custom date under billing date",
                offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getCustomDate(), equalTo("false"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect value of day under billing date",
                offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getDay(), equalTo("0"),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect value of month under billing date",
                offerings.getIncluded().getBillingPlans().get(0).getBillingDate().getMonth(), equalTo("0"),
                assertionErrorList);
        }
        for (final Offering offering : offerings.getOfferings()) {
            AssertCollector.assertThat("Product line is not same for all offerings", offering.getProductLineName(),
                equalTo(productLine), assertionErrorList);
            if (offering.getId().equals(bicOfferings1.getOfferings().get(0).getId())) {
                for (final JSubscriptionEntitlementData entitlement : offering.getOneTimeEntitlements()) {
                    final int index = offering.getOneTimeEntitlements().indexOf(entitlement);
                    AssertCollector.assertThat("Entitlement id is not found for offering: " + offering.getId(),
                        entitlement.getId(), equalTo(itemsList.get(index).getId()), assertionErrorList);
                    AssertCollector.assertThat("Entitlement name is not found for offering: " + offering.getId(),
                        entitlement.getName(), equalTo(itemsList.get(index).getName()), assertionErrorList);
                    AssertCollector.assertThat(
                        "Entitlement external key is not found for offering: " + offering.getId(),
                        entitlement.getExternalKey(), equalTo(itemsList.get(index).getExternalKey()),
                        assertionErrorList);
                    AssertCollector.assertThat("Entitlement type is not found for offering: " + offering.getId(),
                        entitlement.getEntityType(), equalTo(EntityType.ITEM), assertionErrorList);
                    AssertCollector.assertThat(
                        "Entitlement licensing model externalKey is not found for offering: " + offering.getId(),
                        entitlement.getLicensingModelExternalKey(),
                        equalTo(PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY), assertionErrorList);
                }
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with valid user external key and invalid offer external
     * key
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndInValidOfferExternalKey() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.OFFER_EXT_KEY.getName(), "dffwweffwewfffew");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("Invalid offer: dffwweffwewfffew"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get offerings api with valid user external key and valid offer external
     * key
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndValidOfferExternalKey() {
        final Map<String, String> params = new HashMap<>();
        final com.autodesk.bsm.pelican.api.pojos.subscriptionplan.SubscriptionPlan subscriptionPlan =
            resource.subscriptionPlan().getById(bicOfferings1.getOfferings().get(0).getId(), null);
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.OFFER_EXT_KEY.getName(),
            subscriptionPlan.getSubscriptionOffers().getSubscriptionOffers().get(0).getExternalKey());
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect number of offerings returned", offerings.getOfferings().size(),
            equalTo(offeringIdMap.size()), assertionErrorList);
        for (final Offering offering : offerings.getOfferings()) {
            boolean found = true;
            if (offeringIdMap.get(offering.getId()) == null) {
                found = false;
            }
            AssertCollector.assertThat("Incorrect offering returned by the api response", found, equalTo(true),
                assertionErrorList);
        }

        validatePriceDiscountForBundledPromo(offerings);
        AssertCollector.assertThat("Incorrect meta offering response returned by the api",
            offerings.getMeta().getCurrentOffering().getId(), equalTo(bicOfferings1.getOfferings().get(0).getId()),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get user offerings api with valid user external key, valid subscription
     * id and an invalid store id
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndInValidStoreId() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.STORE_ID.getName(), "vdbdvdvsdd");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("Invalid store id."), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get user offerings api with valid user external key, valid subscription
     * id and an valid store id
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndValidStoreId() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.STORE_ID.getName(), getStoreIdUs());
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect number of offerings returned", offerings.getOfferings().size(),
            equalTo(4), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering returned by the api", offerings.getOfferings().get(0).getId(),
            equalTo(bicOfferings1.getOfferings().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta offering response returned by the api",
            offerings.getMeta().getCurrentOffering().getId(), equalTo(bicOfferings1.getOfferings().get(0).getId()),
            assertionErrorList);
        validatePriceDiscountForBundledPromo(offerings);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get user offerings api with valid user external key, valid subscription
     * id and an invalid store external key
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndInValidStoreExternalKey() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.STORE_EXT_KEY.getName(), "vdbdvdvsdd");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("No store exists for store external key: vdbdvdvsdd"),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get user offerings api with valid user external key, valid subscription
     * id and an invalid store type external key
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndInValidStoreType() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.STORE_TYPE_EXT_KEY.getName(), "gcsdsdc");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("Invalid store type: gcsdsdc"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get user offerings api with valid user external key, valid subscription
     * id and an invalid country
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndInValidCountry() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.COUNTRY.getName(), "KK");
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect error detail for the get user offerings api",
            offerings.getErrors().get(0).getDetail(), equalTo("Invalid country code: KK"), assertionErrorList);
        AssertCollector.assertThat("Incorrect error status for the api", offerings.getErrors().get(0).getStatus(),
            equalTo(400), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test case which will test the get user offerings api with valid user external key, valid subscription
     * id and a valid country
     */
    @Test
    public void testGetUserOfferingsApiWithValidExternalKeyAndValidCountry() {
        final Map<String, String> params = new HashMap<>();
        params.put(UserOfferingsClient.Parameter.USER_EXT_KEY.getName(), getUserExternalKey());
        params.put(UserOfferingsClient.Parameter.SUBSCRIPTION_ID.getName(), subscriptionIdForBic);
        params.put(UserOfferingsClient.Parameter.COUNTRY.getName(), Country.US.getCountryCode());
        final Offerings offerings = resource.userOfferings().getUserOfferings(params);
        AssertCollector.assertThat("Incorrect number of offerings returned", offerings.getOfferings().size(),
            equalTo(4), assertionErrorList);
        AssertCollector.assertThat("Incorrect offering returned by the api", offerings.getOfferings().get(0).getId(),
            equalTo(bicOfferings1.getOfferings().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect meta offering response returned by the api",
            offerings.getMeta().getCurrentOffering().getId(), equalTo(bicOfferings1.getOfferings().get(0).getId()),
            assertionErrorList);
        validatePriceDiscountForBundledPromo(offerings);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests Get User Offering By Id for a Subscription Plan in ACTIVE state, to test the Packaging Type
     * status
     */
    @Test(dataProvider = "getOfferIdForPackagingType")
    public void testGetUserOfferingForPackaingType(final PackagingType packagingType) {

        final HashMap<String, String> params = new HashMap<>();
        params.put(OfferingsClient.Parameter.STORE_ID.getName(), storeFranceWithoutVat.getId());
        final Offerings offerings = resource.offerings().getOfferings(params);
        for (int i = 0; i < offerings.getOfferings().size(); i++) {
            if (packagingType.getDisplayName().equals(PackagingType.IC.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, excepted IC",
                    offerings.getOfferings().get(i).getPackagingType(), equalTo(PackagingType.IC), assertionErrorList);
            } else if (packagingType.getDisplayName().equals(PackagingType.VG.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, excepted VG",
                    offerings.getOfferings().get(i).getPackagingType(), equalTo(PackagingType.VG), assertionErrorList);
            } else if (packagingType.getDisplayName().equals(PackagingType.NONE.getDisplayName())) {
                AssertCollector.assertThat("Invalid PackagingType, excepted NONE",
                    offerings.getOfferings().get(i).getPackagingType(), nullValue(), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);

    }

    /**
     * Method to validate the Price discount for the offering's
     *
     * @param offerings
     */
    private void validatePriceDiscountForBundledPromo(final Offerings offerings) {
        for (final Price price : offerings.getIncluded().getPrices()) {
            if (price.getId().equals(prideIdOfBic4)) {
                AssertCollector.assertThat("Incorrect discount amount returned by the api", price.getDiscount(),
                    equalTo("50.00"), assertionErrorList);
                AssertCollector.assertThat("Incorrect Amount After Discount returned by the api",
                    price.getAmountAfterDiscount(), equalTo("450.00"), assertionErrorList);
            }
        }
    }

    /**
     * DataProvider to pass Price Id and boolean flag to identify for packagingType if its IC or None
     *
     */
    @DataProvider(name = "getOfferIdForPackagingType")
    public Object[][] getOfferId() {
        return new Object[][] { { PackagingType.IC }, { PackagingType.VG }, { PackagingType.NONE } };
    }
}
