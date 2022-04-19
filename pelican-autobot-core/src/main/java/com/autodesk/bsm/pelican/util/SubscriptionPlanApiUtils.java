package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementData;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementRequest;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferData;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPriceData;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.GrantType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.collections.Lists;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class used to create a subscription plan, offer and prices to the Subscription offer. Note : Create Offering API
 * creates Subscription Plan as well as Basic Offering Here we are creating Subscription plan, which is a type of
 * Offering
 */
public class SubscriptionPlanApiUtils {

    private static final String PRODUCT_LINE_TEXT = "productLine";
    private static final String TIER = "1";
    private static final String OFFER_PRICE_TYPE = "price";
    private static final int PRICE_AMOUNT = 500;
    private static final int TRIAL_AMOUNT = 0;
    private PelicanPlatform resource;
    private EnvironmentVariables environmentVariables;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanApiUtils.class.getSimpleName());

    public SubscriptionPlanApiUtils(final EnvironmentVariables environmentVariables) {

        this.environmentVariables = environmentVariables;
        resource = new PelicanClient(environmentVariables).platform();
    }

    /**
     * This method helps you to add a subscription plan, add an offer to that plan and return that offering
     *
     * @param priceListExternalKey - external Key of priceList which is already available
     * @param offeringType - OfferingType - E.g BIC_SUBSCRIPTION,CURRENCY See OfferyTpe Enum for more options
     * @param billingFrequency - E.g DAY/MONTH BillingFrequency - See BillingFrequency Enum for more options
     * @param status - E.g ACTIVE,NEW see Status Enum for more options
     * @param supportLevel - Basic/Advanced, Use SupportLevel Enum
     * @param usageType - Commercial/Government etc. Use SupportLevel Enum
     * @return Offering entity. You can use the pojo methods for retrieving appropriate values!
     */
    public Offerings addSubscriptionPlan(final String priceListExternalKey, final OfferingType offeringType,
        final BillingFrequency billingFrequency, final Status status, final SupportLevel supportLevel,
        final UsageType usageType) {

        return addSubscriptionPlanWithCount(priceListExternalKey, offeringType, billingFrequency, status, supportLevel,
            usageType, 1);
    }

    /**
     * A method used to add a basic offering
     *
     * @ param - Pelican Platform, Basic Offering @ Return - Basic Offering
     */
    private Offerings addSubscriptionPlan(final PelicanPlatform resource, final Offerings subscriptionPlan) {
        return resource.offerings().addOffering(subscriptionPlan);
    }

    /**
     * This method creates Subscription Plan(Offering).
     *
     * @return Offerings (Subscription Plan)
     */
    public Offerings addSubscriptionPlan(final PelicanPlatform resource, final OfferingType offeringType,
        final Status status, final SupportLevel supportLevel, final UsageType usageType) {

        /*
         * Add subscription plan Both subscription Plan and Basic Offering will be of type BasicOffering Object as we
         * only have one api to create both subscription plan and a basic offering
         */
        final Offerings subscriptionPlan = new Offerings();
        final String subscriptionPlanExternalKey = "SQA_Sub_Plan_" + RandomStringUtils.randomAlphabetic(10);
        final Offering subscriptionPlanData = new Offering();
        subscriptionPlanData.setExternalKey(subscriptionPlanExternalKey);
        subscriptionPlanData.setName(subscriptionPlanExternalKey);
        subscriptionPlanData.setEntityType(EntityType.OFFERING);
        subscriptionPlanData.setOfferingType(offeringType);
        subscriptionPlanData.setTier(TIER);
        subscriptionPlanData.setStatus(status.toString());
        subscriptionPlanData.setCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        subscriptionPlanData.setSupportLevel(supportLevel);
        subscriptionPlanData.setUsageType(usageType);
        subscriptionPlanData.setProductLine(BaseTestData.getProductLineExternalKeyMaya());
        subscriptionPlanData.setOfferingDetail(PelicanConstants.OFFERING_DETAILS1);
        subscriptionPlan.setOffering(subscriptionPlanData);
        final Offerings newSubscriptionPlan = addSubscriptionPlan(resource, subscriptionPlan);

        return resource.offerings().getOfferingById(newSubscriptionPlan.getOffering().getId(),
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
    }

    /**
     * This method helps you to add a subscription plan, add an offer to that plan and return that offering
     *
     * @param priceListExternalKey - external Key of priceList which is already available
     * @param offeringType - OfferingType - E.g BIC_SUBSCRIPTION,CURRENCY See OfferyTpe Enum for more options
     * @param billingFrequency - E.g DAY/MONTH BillingFrequency - See BillingFrequency Enum for more options
     * @param status - E.g ACTIVE,NEW see Status Enum for more options
     * @param supportLevel - Basic/Advanced, Use SupportLevel Enum
     * @param usageType - Commercial/Government etc. Use SupportLevel Enum
     * @param billingFrequencyCount - Billing frequency count e.g for 1, 2 or 3 etc year, month etc
     * @return Offering entity. You can use the pojo methods for retrieving appropriate values!
     */
    private Offerings addSubscriptionPlanWithCount(final String priceListExternalKey, final OfferingType offeringType,
        final BillingFrequency billingFrequency, final Status status, final SupportLevel supportLevel,
        final UsageType usageType, final int billingFrequencyCount) {

        return addPlanWithOfferAndPrice(BaseTestData.getProductLineExternalKeyMaya(), priceListExternalKey,
            offeringType, billingFrequency, status, supportLevel, usageType, billingFrequencyCount, null, null,
            resource, 500);
    }

    /**
     * Method to add Product Line
     *
     * @param productLineExternalKey
     * @param isActive is the flag to set it Active or Inactive
     * @return ProductLine
     */
    public ProductLine addProductLine(final String productLineExternalKey, final Boolean isActive) {
        ProductLine newProductLine;
        // if productLineExternalKey is null, return the existing product line.
        if (productLineExternalKey == null) {
            return BaseTestData.getProductLineMaya();
        } else {
            final ProductLine productLine = new ProductLine();
            final ProductLineData productLineData = new ProductLineData();
            productLineData.setExternalKey(productLineExternalKey);
            productLineData.setName(productLineExternalKey);
            productLineData.setType(PRODUCT_LINE_TEXT);
            productLineData.setIsActive(isActive);
            productLine.setData(productLineData);
            newProductLine = addProductLine(resource, productLine);
        }

        return newProductLine;
    }

    /**
     * Method to add Product Line
     *
     * @param productLineExternalKey
     * @return ProductLine
     */
    public ProductLine addProductLine(final String productLineExternalKey) {
        return addProductLine(productLineExternalKey, true);
    }

    /**
     * A method used to add a product line
     *
     * @ Param - Pelican Platform, ProductLine @ Return Product Line Object
     */
    public ProductLine addProductLine(final PelicanPlatform resource, final ProductLine productLine) {
        return resource.productLine().addProductLine(productLine);
    }

    /**
     * This is a method to add an offer and prices for a specified subscription plan
     *
     * @return offerings (Subscription Plan)
     */
    public Offerings addOfferAndPrices(final String subscriptionPlanId, final String priceListExternalKey) {
        final SubscriptionOffer newSubscriptionOffer = addOffer(subscriptionPlanId);
        addPrice(priceListExternalKey, subscriptionPlanId, newSubscriptionOffer);

        return resource.offerings().getOfferingById(subscriptionPlanId, "offers,prices");
    }

    /**
     * method to add Offer to subscription plan
     *
     * @param subscriptionPlanId
     * @return
     */
    public SubscriptionOffer addOffer(final String subscriptionPlanId) {
        return addSubscriptionOffer(resource,
            helperToAddSubscriptionOfferToPlan(null, BillingFrequency.MONTH, 1, Status.ACTIVE), subscriptionPlanId);
    }

    /**
     * method to add Price to offer
     *
     * @param priceListExternalKey
     * @param subscriptionPlanId
     * @param newSubscriptionOffer
     */
    private void addPrice(final String priceListExternalKey, final String subscriptionPlanId,
        final SubscriptionOffer newSubscriptionOffer) {
        addPricesToSubscriptionOffer(resource,
            helperToAddPricesToSubscriptionOffer(PRICE_AMOUNT, priceListExternalKey, 0, 12), subscriptionPlanId,
            newSubscriptionOffer.getData().getId());
    }

    /**
     * A method used to add prices to the subscription offer
     *
     * @ param - Subscription offer price, offering id, offer Id Return - Subscription Ofer Price Object
     */
    public SubscriptionOfferPrice addPricesToSubscriptionOffer(final PelicanPlatform resource,
        final SubscriptionOfferPrice subscriptionOfferPrice, final String offeringId, final String offerId) {
        return resource.subscriptionOfferPrice().addSubscriptionOfferPrice(subscriptionOfferPrice, offeringId, offerId);
    }

    /**
     * A method used to add subscription offer
     *
     * @Param - Subscription Offer, Offering Id, Pelican Platform @ Return - Subscription Offer
     */
    public SubscriptionOffer addSubscriptionOffer(final PelicanPlatform resource,
        final SubscriptionOffer subscriptionOffer, final String offeringId) {
        return resource.subscriptionOffer().addSubscriptionOffer(subscriptionOffer, offeringId);

    }

    /**
     * This method creates a subscription plan with a specified product line
     *
     * @param amount TODO
     * @return Offerings (Subscription Plan)
     */
    public Offerings addSubscriptionPlanWithProductLine(final String productLineExternalKey,
        final OfferingType offeringType, final Status status, final SupportLevel supportLevel,
        final UsageType usageType, final BillingFrequency billingFrequency, final int billingFrequencyCount,
        final String priceListExternalKey, final int amount) {

        return addPlanWithOfferAndPrice(productLineExternalKey, priceListExternalKey, offeringType, billingFrequency,
            status, supportLevel, usageType, billingFrequencyCount, null, null, resource, amount);
    }

    /**
     * This is a real method which implements add a subscription plan with offer and price
     *
     * @param productLineExternalKey
     * @param priceListExternalKey
     * @param offeringType
     * @param billingFrequency
     * @param status
     * @param supportLevel
     * @param usageType
     * @param billingFrequencyCount
     * @param subscriptionOfferExternalKey
     * @param subscriptionPlanOfferingId
     * @param resource
     * @param amount
     * @return Offerings (subscription Plan) with offer and price
     */
    private Offerings addPlanWithOfferAndPrice(final String productLineExternalKey, final String priceListExternalKey,
        final OfferingType offeringType, final BillingFrequency billingFrequency, final Status status,
        final SupportLevel supportLevel, final UsageType usageType, final int billingFrequencyCount,
        final String subscriptionOfferExternalKey, String subscriptionPlanOfferingId, final PelicanPlatform resource,
        int amount) {

        if (subscriptionPlanOfferingId == null) {
            final Offerings newSubscriptionPlan = addPlanWithProductLine(productLineExternalKey, offeringType, status,
                supportLevel, usageType, resource, null, null);
            subscriptionPlanOfferingId = newSubscriptionPlan.getOffering().getId();
        }

        // Add a subscription offer to a plan
        final SubscriptionOffer subscriptionOffer = helperToAddSubscriptionOfferToPlan(subscriptionOfferExternalKey,
            billingFrequency, billingFrequencyCount, status);
        final String subscriptionOfferId =
            addSubscriptionOffer(resource, subscriptionOffer, subscriptionPlanOfferingId).getData().getId();
        if (usageType == UsageType.TRL) {
            amount = TRIAL_AMOUNT;
        }
        // add price to the offer which is added above
        addPricesToSubscriptionOffer(resource,
            helperToAddPricesToSubscriptionOffer(amount, priceListExternalKey, 0, 12), subscriptionPlanOfferingId,
            subscriptionOfferId);

        return resource.offerings().getOfferingById(subscriptionPlanOfferingId, "offers,prices");
    }

    /**
     * This is a real method which implements add a subscription plan with product line
     *
     * @param productLineExternalKey
     * @param offeringType
     * @param status
     * @param supportLevel
     * @param usageType
     * @param resource
     * @param offeringExternalKey TODO
     * @param packagingType TODO
     * @return Offerings (subscription Plan)
     */
    public Offerings addPlanWithProductLine(final String productLineExternalKey, final OfferingType offeringType,
        final Status status, final SupportLevel supportLevel, final UsageType usageType, final PelicanPlatform resource,
        final String offeringExternalKey, final PackagingType packagingType) {
        final Offerings subscriptionPlan = new Offerings();
        // Add subscription plan
        final Offering subscriptionPlanData = new Offering();
        if (offeringExternalKey == null) {
            final String externalKey = "SQA_Sub_Plan_" + RandomStringUtils.randomAlphabetic(10);
            subscriptionPlanData.setExternalKey(externalKey);
            subscriptionPlanData.setName(externalKey);
        } else {
            subscriptionPlanData.setExternalKey(offeringExternalKey);
            subscriptionPlanData.setName(offeringExternalKey);
        }
        if (packagingType != null && !(packagingType.equals(PackagingType.NONE))) {
            subscriptionPlanData.setPackagingType(packagingType);
        }
        subscriptionPlanData.setEntityType(EntityType.OFFERING);
        subscriptionPlanData.setOfferingType(offeringType);
        subscriptionPlanData.setTier(TIER);
        subscriptionPlanData.setStatus(status.toString());
        subscriptionPlanData.setCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        subscriptionPlanData.setSupportLevel(supportLevel);
        subscriptionPlanData.setUsageType(usageType);
        subscriptionPlanData.setProductLine(productLineExternalKey);
        subscriptionPlanData.setOfferingDetail(PelicanConstants.OFFERING_DETAILS1);
        subscriptionPlan.setOffering(subscriptionPlanData);
        return addSubscriptionPlan(resource, subscriptionPlan);
    }

    /**
     * Helper Method to create subscription offer to a plan
     *
     * @return SubscriptionOffer
     */
    public SubscriptionOffer helperToAddSubscriptionOfferToPlan(String subscriptionOfferExternalKey,
        final BillingFrequency billingFrequency, final int billingFrequencyCount, final Status status) {

        final SubscriptionOffer subscriptionOffer = new SubscriptionOffer();
        if (subscriptionOfferExternalKey == null) {
            subscriptionOfferExternalKey = "SQA_Sub_Offer_" + RandomStringUtils.randomAlphabetic(6);
        }
        final SubscriptionOfferData subscriptionOfferData = new SubscriptionOfferData();
        subscriptionOfferData.setExternalKey(subscriptionOfferExternalKey);
        subscriptionOfferData.setName(subscriptionOfferExternalKey);
        subscriptionOfferData.setType("offer");
        subscriptionOfferData.setStatus(status);
        subscriptionOfferData.setBillingFrequency(billingFrequency);
        subscriptionOfferData.setBillingFrequencyCount(billingFrequencyCount);
        subscriptionOffer.setData(subscriptionOfferData);

        return subscriptionOffer;
    }

    /**
     * This method adds price to the subscription offer
     *
     * @return SubscriptionOfferPrice
     */
    public SubscriptionOfferPrice helperToAddPricesToSubscriptionOffer(final int amount,
        final String priceListExternalKey, final int effectiveStartDate, final int effectiveEndDate) {

        // Add subscription offer price
        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, effectiveStartDate);
        final Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, effectiveEndDate);
        final Date endDate = calendar.getTime();
        final String priceStartDate = dateFormat.format(startDate);
        final String priceEndDate = dateFormat.format(endDate);
        final SubscriptionOfferPrice subscriptionOfferPrice = new SubscriptionOfferPrice();
        final SubscriptionOfferPriceData subscriptionOfferPriceData = new SubscriptionOfferPriceData();
        subscriptionOfferPriceData.setType(OFFER_PRICE_TYPE);
        subscriptionOfferPriceData.setAmount(amount);
        subscriptionOfferPriceData.setStartDate(priceStartDate);
        subscriptionOfferPriceData.setEndDate(priceEndDate);
        subscriptionOfferPriceData.setPriceList(priceListExternalKey);
        subscriptionOfferPrice.setData(subscriptionOfferPriceData);

        return subscriptionOfferPrice;
    }

    /**
     * @return subscription offer price with start and end date
     */
    public SubscriptionOfferPrice helperToAddPricesToSubscriptionOfferWithDates(final int amount,
        final String priceListExternalKey, final String startDate, final String endDate) {

        // Add subscription offer price
        // DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        // String priceStartDate = dateFormat.format(startDate);
        // String priceEndDate = dateFormat.format(endDate);
        final SubscriptionOfferPrice subscriptionOfferPrice = new SubscriptionOfferPrice();
        final SubscriptionOfferPriceData subscriptionOfferPriceData = new SubscriptionOfferPriceData();
        subscriptionOfferPriceData.setType(OFFER_PRICE_TYPE);
        subscriptionOfferPriceData.setAmount(amount);
        subscriptionOfferPriceData.setStartDate(startDate);
        subscriptionOfferPriceData.setEndDate(endDate);
        subscriptionOfferPriceData.setPriceList(priceListExternalKey);
        subscriptionOfferPrice.setData(subscriptionOfferPriceData);

        return subscriptionOfferPrice;
    }

    /**
     * This method adds given item/feature id as entitlement to a subscription plan. If item/feature id is not passed, a
     * new feature will be created. If defaultLicensingModel is set to true, licensingModel will be ignored and RTL#1
     * will be used for licensing model. If defaultLicensingModel is set to false, licensing model will be used. If
     * defaultLicensingModel is set to false, licensing model value is null, licensing model will not be added to
     * subscription entitlement.
     *
     * @return itemId
     */
    public String helperToAddEntitlementToSubscriptionPlan(final String offeringId, String itemId,
        String licensingModel, final boolean defaultLicensingModel) {

        String itemExternalKey;
        final Applications applications = resource.application().getApplications();
        final String appId = applications.getApplications().get(0).getId();

        // if item id/feature is not null, get the item/feature external key
        if (itemId != null) {
            final Item item = resource.item().getItem(itemId);
            LOGGER.info("item id " + item.getId());
            itemExternalKey = item.getExternalKey();
        } else {
            // else create the item/feature
            // add item type/feature type
            final FeatureApiUtils featureApiUtils = new FeatureApiUtils(environmentVariables);
            final Item item = featureApiUtils.addFeature(null, null, null);
            itemId = item.getId();
            itemExternalKey = item.getExternalKey();
        }
        if (defaultLicensingModel) {
            licensingModel = PelicanConstants.RETAIL_LICENSING_MODEL_EXTERNAL_KEY;
        }
        // create subscription entitlement data
        final JSubscriptionEntitlementData subscriptionEntitlementData = new JSubscriptionEntitlementData();
        subscriptionEntitlementData.setAppId(appId);
        subscriptionEntitlementData.setEntityType(EntityType.SUBSCRIPTION_ENTITLEMENT);
        subscriptionEntitlementData.setExternalKey(itemExternalKey);
        subscriptionEntitlementData.setGrantType(GrantType.FEATURE);
        subscriptionEntitlementData.setLicensingModel(licensingModel);

        // add entitlement
        final JSubscriptionEntitlementRequest subscriptionEntitlementRequest = new JSubscriptionEntitlementRequest();
        subscriptionEntitlementRequest.setData(Lists.newArrayList(subscriptionEntitlementData));
        resource.subscriptionEntitlement().addEntitlement(subscriptionEntitlementRequest, offeringId);

        LOGGER.info("Item/Feature id: " + itemId);
        LOGGER.info("Entitlement added to offering id: " + offeringId);
        return itemId;
    }

}
