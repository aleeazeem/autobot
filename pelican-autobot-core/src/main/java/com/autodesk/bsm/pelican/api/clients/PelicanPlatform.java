package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

/**
 * Access to pelican core rest api
 *
 * @author Vineel.
 */

public class PelicanPlatform {

    private BicReleaseClient bicReleaseClient;

    private FinanceReportClient financeReportClient;

    // Item
    private ItemClient itemClient;

    // Items
    private ItemsClient itemsClient;

    // Item Instance
    private ItemInstanceClient itemInstanceClient;

    // Item Instances
    private ItemInstancesClient itemInstancesClient;

    // Item Type
    private ItemTypeClient itemTypeClient;

    // Item Types
    private ItemTypesClient itemTypesClient;

    // Health Check Status
    private final HealthCheckStatusClient healthCheckStatusClient;

    // Offerings
    private final OfferingsClient offeringClient;

    // Offerings V3
    private final OfferingsV3Client offeringV3Client;

    // Purchase Order
    private PurchaseOrderClient purchaseOrderClient;

    // Purchase Order
    private PurchaseOrdersClient purchaseOrdersClient;

    // Subscription
    private SubscriptionClient subscriptionClient;

    // Subscriptions
    private SubscriptionsClient subscriptionsClient;

    // Subscription Plan
    private SubscriptionPlanClient subscriptionPlanClient;

    // Stores
    private StoreClient storeClient;

    // Subscription Plans
    private SubscriptionPlansClient subscriptionPlansClient;

    // User Offerings
    private UserOfferingsClient userOfferingsClient;

    // User
    private UserClient userClient;

    // Users
    private UsersClient usersClient;

    // Payment Profile
    private PaymentProfileClient paymentProfileClient;
    private PaymentProfilesClient paymentProfilesClient;

    // Promotions
    private PromotionClient promotionClient;
    private PromotionsClient promotionsClient;

    // StoreType
    private StoreTypeClient storeTypeClient;

    // Create store
    private CreateStoreClient createStoreClient;

    // Create pricelist
    private PriceListClient priceListClient;

    // Assign country
    private CountryClient countryClient;

    // Create ProductLine
    private ProductLineClient productLineClient;

    // Create BasicOffering Price Date
    private BasicOfferingPriceClient basicOfferingPriceClient;

    // Create subscription offer
    private SubscriptionOfferClient subscriptionOfferClient;

    // Create SubscriptionOfferPrice Data
    private SubscriptionOfferPriceClient subscriptionOfferPriceClient;
    // Process PurchaseOrder Resource
    private ProcessPurchaseOrderClient processPurchaseOrderClient;

    private FulfillmentCallBackClient fulfillmentCallBackClient;

    // Price Quote
    private PriceQuotesClient priceQuoteClient;

    // Currecncy
    private CurrencyClient currencyClient;

    // Application resource
    private ApplicationClient applicationClient;

    // Subscription Json Resource
    private SubscriptionJsonClient subscriptionJsonClient;

    // Add Default Store Resource
    private AddDefaultStoreClient defaultStoreClient;

    // default PelicanEmail resource
    private CheckPelicanEmailClient pelicanEmail;

    // Subscription Entitlement Resource
    // Add properties Resource
    private AddPropertiesClient addPropertiesClient;

    // Subscription Entitlement Resource
    private SubscriptionEntitlementClient subscriptionEntitlementClient;

    // Product Lines Resource
    private ProductLinesClient productLinesClient;

    // Get Default Store resource
    private GetDefaultStoreClient getDefaultStoreClient;

    // Get reduce seats by subscription id client
    private ReduceSeatsBySubscriptionIdClient reduceSeatsBySubscriptionIdClient;

    // Get price id by price id client
    private GetPriceIdClient getPriceIdClient;

    // Get Subscription owners client
    private SubscriptionOwnersClient subscriptionOwnersClient;

    // Get Subscription events client
    private SubscriptionEventsClient subscriptionEventsClient;

    /**
     * constructor for PelicanPlatform
     *
     * @param appFamily
     */
    public PelicanPlatform(final EnvironmentVariables environmentVariables, final String appFamily) {

        bicReleaseClient = new BicReleaseClient(environmentVariables);
        userOfferingsClient = new UserOfferingsClient(environmentVariables, appFamily);
        financeReportClient = new FinanceReportClient(environmentVariables, appFamily);
        itemClient = new ItemClient(environmentVariables, appFamily);
        itemsClient = new ItemsClient(environmentVariables, appFamily);
        itemInstanceClient = new ItemInstanceClient(environmentVariables, appFamily);
        itemInstancesClient = new ItemInstancesClient(environmentVariables, appFamily);
        itemTypeClient = new ItemTypeClient(environmentVariables, appFamily);
        itemTypesClient = new ItemTypesClient(environmentVariables, appFamily);
        healthCheckStatusClient = new HealthCheckStatusClient(environmentVariables, appFamily);
        offeringClient = new OfferingsClient(environmentVariables, appFamily);
        offeringV3Client = new OfferingsV3Client(environmentVariables, appFamily);
        purchaseOrderClient = new PurchaseOrderClient(environmentVariables, appFamily);
        purchaseOrdersClient = new PurchaseOrdersClient(environmentVariables, appFamily);
        subscriptionClient = new SubscriptionClient(environmentVariables, appFamily);
        subscriptionsClient = new SubscriptionsClient(environmentVariables, appFamily);
        subscriptionPlanClient = new SubscriptionPlanClient(environmentVariables, appFamily);
        subscriptionPlansClient = new SubscriptionPlansClient(environmentVariables, appFamily);
        userClient = new UserClient(environmentVariables, appFamily);
        usersClient = new UsersClient(environmentVariables, appFamily);
        storeClient = new StoreClient(environmentVariables, appFamily);
        promotionClient = new PromotionClient(environmentVariables, appFamily);
        promotionsClient = new PromotionsClient(environmentVariables, appFamily);
        paymentProfileClient = new PaymentProfileClient(environmentVariables, appFamily);
        paymentProfilesClient = new PaymentProfilesClient(environmentVariables);
        storeTypeClient = new StoreTypeClient(environmentVariables, appFamily);
        createStoreClient = new CreateStoreClient(environmentVariables, appFamily);
        priceListClient = new PriceListClient(environmentVariables, appFamily);
        productLineClient = new ProductLineClient(environmentVariables, appFamily);
        basicOfferingPriceClient = new BasicOfferingPriceClient(environmentVariables, appFamily);
        subscriptionOfferClient = new SubscriptionOfferClient(environmentVariables, appFamily);
        subscriptionOfferPriceClient = new SubscriptionOfferPriceClient(environmentVariables, appFamily);
        countryClient = new CountryClient(environmentVariables, appFamily);
        processPurchaseOrderClient = new ProcessPurchaseOrderClient(environmentVariables, appFamily);
        fulfillmentCallBackClient = new FulfillmentCallBackClient(environmentVariables, appFamily);
        priceQuoteClient = new PriceQuotesClient(environmentVariables, appFamily);
        currencyClient = new CurrencyClient(environmentVariables, appFamily);
        applicationClient = new ApplicationClient(environmentVariables, appFamily);
        subscriptionJsonClient = new SubscriptionJsonClient(environmentVariables, appFamily);
        defaultStoreClient = new AddDefaultStoreClient(environmentVariables, appFamily);
        pelicanEmail = new CheckPelicanEmailClient(environmentVariables);
        addPropertiesClient = new AddPropertiesClient(environmentVariables, appFamily);
        subscriptionEntitlementClient = new SubscriptionEntitlementClient(environmentVariables, appFamily);
        productLinesClient = new ProductLinesClient(environmentVariables, appFamily);
        getDefaultStoreClient = new GetDefaultStoreClient(environmentVariables, appFamily);
        reduceSeatsBySubscriptionIdClient = new ReduceSeatsBySubscriptionIdClient(environmentVariables, appFamily);
        getPriceIdClient = new GetPriceIdClient(environmentVariables, appFamily);
        subscriptionOwnersClient = new SubscriptionOwnersClient(environmentVariables, appFamily);
        subscriptionEventsClient = new SubscriptionEventsClient(environmentVariables, appFamily);
    }

    /**
     * @return BicReleaseResource.
     */
    public BicReleaseClient bicRelease() {
        return bicReleaseClient;
    }

    /**
     * @return CheckPelicanEmail.
     */
    public CheckPelicanEmailClient pelicanEmail() {
        return pelicanEmail;
    }

    /**
     * @return FinanceReportResource.
     */
    public FinanceReportClient financeReport() {
        return financeReportClient;
    }

    /**
     * @return ItemResource.
     */
    public ItemClient item() {
        return itemClient;
    }

    /**
     * @return ItemsResource.
     */
    public ItemsClient items() {
        return itemsClient;
    }

    /**
     * @return ItemInstanceResource.
     */
    public ItemInstanceClient itemInstance() {
        return itemInstanceClient;
    }

    /**
     * @return ItemInstancesResource.
     */
    public ItemInstancesClient itemInstances() {
        return itemInstancesClient;
    }

    /**
     * @return ItemTypeResource.
     */
    public ItemTypeClient itemType() {
        return itemTypeClient;
    }

    /**
     * @return HealthCheckStatusResource.
     */
    public HealthCheckStatusClient healthCheckStatusResource() {
        return healthCheckStatusClient;
    }

    /**
     * @return ItemTypesClient Resource.
     */
    public ItemTypesClient itemTypes() {
        return itemTypesClient;
    }

    /**
     * @return OfferingsResource.
     */
    public OfferingsClient offerings() {
        return offeringClient;
    }

    /**
     * @return OfferingsResource.
     */
    public OfferingsV3Client offeringsV3() {
        return offeringV3Client;
    }

    /**
     * @return PurchaseOrderResource.
     */
    public PurchaseOrderClient purchaseOrder() {
        return purchaseOrderClient;
    }

    /**
     * @return PurchaseOrdersResource.
     */
    public PurchaseOrdersClient purchaseOrders() {
        return purchaseOrdersClient;
    }

    /**
     * @return SubscriptionResource.
     */
    public SubscriptionClient subscription() {
        return subscriptionClient;
    }

    /**
     * @return SubscriptionsResource.
     */
    public SubscriptionsClient subscriptions() {
        return subscriptionsClient;
    }

    /**
     * @return SubscriptionPlanResource.
     */
    public SubscriptionPlanClient subscriptionPlan() {
        return subscriptionPlanClient;
    }

    /**
     * @return SubscriptionPlansResource.
     */
    public SubscriptionPlansClient subscriptionPlans() {
        return subscriptionPlansClient;
    }

    /**
     * @return CatalogResource.
     */
    public UserOfferingsClient userOfferings() {
        return userOfferingsClient;
    }

    /**
     * @return UserResource.
     */
    public UserClient user() {
        return userClient;
    }

    /**
     * @return UsersResource.
     */
    public UsersClient users() {
        return usersClient;
    }

    /**
     * @return PaymentProfileClient.
     */
    public PaymentProfileClient paymentProfile() {
        return paymentProfileClient;
    }

    public PaymentProfilesClient paymentProfiles() {
        return paymentProfilesClient;
    }

    /**
     * @return storeClient.
     */
    public StoreClient stores() {
        return storeClient;
    }

    /**
     * @return promotionClient.
     */
    public PromotionClient promotion() {
        return promotionClient;
    }

    /**
     * @return promotionsClient.
     */
    public PromotionsClient promotions() {
        return promotionsClient;
    }

    /**
     * @return storeTypeClient.
     */
    public StoreTypeClient storeType() {
        return storeTypeClient;
    }

    /**
     * @return createStoreClient.
     */
    public CreateStoreClient createStore() {
        return createStoreClient;
    }

    /**
     * @return countryClient.
     */
    public CountryClient country() {
        return countryClient;
    }

    /**
     * @return priceListClient.
     */
    public PriceListClient priceList() {
        return priceListClient;
    }

    /**
     * @return ProductLineClient.
     */
    public ProductLineClient productLine() {
        return productLineClient;
    }

    /**
     * @return Basic Offering Price Client.
     */
    public BasicOfferingPriceClient basicOfferingPrice() {
        return basicOfferingPriceClient;
    }

    /**
     * @return Subscription Offer Client.
     */
    public SubscriptionOfferClient subscriptionOffer() {
        return subscriptionOfferClient;
    }

    /**
     * @return Subscription offer Price Client.
     */
    public SubscriptionOfferPriceClient subscriptionOfferPrice() {
        return subscriptionOfferPriceClient;
    }

    /**
     * @return processPurchase Order Client resource.
     */
    public ProcessPurchaseOrderClient processPurchaseOrder() {
        return processPurchaseOrderClient;
    }

    /**
     * @return fulfillmentCallBackClient.
     */
    public FulfillmentCallBackClient fulfillmentCallBackClient() {
        return fulfillmentCallBackClient;
    }

    /**
     * @return priceQuoteClient.
     */
    public PriceQuotesClient priceQuote() {
        return priceQuoteClient;
    }

    /**
     * @return currencyClient.
     */
    public CurrencyClient currency() {
        return currencyClient;
    }

    /**
     * @return applicationClient.
     */
    public ApplicationClient application() {
        return applicationClient;
    }

    /**
     * @return subscriptionJsonClient.
     */
    public SubscriptionJsonClient subscriptionJson() {
        return subscriptionJsonClient;
    }

    /**
     * @return defaultStoreClient.
     */
    public AddDefaultStoreClient defaultStore() {
        return defaultStoreClient;
    }

    /**
     * @return PropertiesClient.
     */
    public AddPropertiesClient properties() {
        return addPropertiesClient;
    }

    /**
     * @return subscriptionEntitlementClient.
     */
    public SubscriptionEntitlementClient subscriptionEntitlement() {
        return subscriptionEntitlementClient;
    }

    public ProductLinesClient productLines() {
        return productLinesClient;
    }

    public GetDefaultStoreClient getDefaultStore() {
        return getDefaultStoreClient;
    }

    /**
     * Method to get reduce seats by subscription id resource
     *
     * @return getReduceSeatsBySubscriptionIdClient
     */
    public ReduceSeatsBySubscriptionIdClient reduceSeatsBySubscriptionId() {
        return reduceSeatsBySubscriptionIdClient;
    }

    public GetPriceIdClient getPriceIdClient() {
        return getPriceIdClient;
    }

    /**
     * Method to get subscriptionOwnersClient.
     *
     * @return subscriptionOwnersClient.
     */
    public SubscriptionOwnersClient getSubscriptionOwnersClient() {
        return subscriptionOwnersClient;
    }

    /**
     * Method to get subscriptionEventsClient.
     *
     * @return subscriptionEventsClient
     */
    public SubscriptionEventsClient getSubscriptionEventsClient() {
        return subscriptionEventsClient;
    }
}
