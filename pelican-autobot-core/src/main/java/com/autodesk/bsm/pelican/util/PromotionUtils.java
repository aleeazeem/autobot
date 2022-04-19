package com.autodesk.bsm.pelican.util;

import static com.autodesk.bsm.pelican.constants.PelicanConstants.UTC_TIME_ZONE;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.BundlePromoOfferings;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData.PriceList;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class PromotionUtils {

    private PelicanPlatform resource;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private static final String CODE = "AUTO";
    private static final int MAX_PROMO_CODE_LENGTH = 16;

    public static final String SUBSCRIPTION_OFFERS_STR = "Subscription Offers";
    public static final String DISCOUNT_STR = "Discount";
    public static final String DISCOUNT_TYPE_AMOUNT = "Cash Amount";
    public static final String DISCOUNT_TYPE_PERCENTAGE = "Percentage Discount";
    public static final String DISCOUNT_TYPE_SUPPLEMENT_TIME = "Supplement";
    public static final String PROMO_DISCOUNT_TYPE_PERCENT = "Percentage";
    public static final String PROMO_DISCOUNT_TYPE_SUPPLEMENT = "Supplement";
    public static final String AUDIT_DATA_PROMO_MAX_USES = "maxUses";
    public static final String AUDIT_DATA_PROMO_STORE_IDS = "storeIds";
    public static final String AUDIT_DATA_PROMO_STORE_WIDE = "isStoreWide";
    public static final String AUDIT_DATA_PROMO_STATE = "state";
    public static final String AUDIT_DATA_PROMO_STANDALONE = "isStandalone";
    public static final String AUDIT_DATA_PROMO_DISCOUNT_PERCENT = "discountPercent";
    public static final String AUDIT_DATA_PROMO_MAX_USES_PER_USER = "maxUsesPerUser";
    public static final String AUDIT_DATA_PROMO_TIME_PERIOD_TYPE = "timePeriodType";
    public static final String AUDIT_DATA_PROMO_TIME_PERIOD_COUNT = "timePeriodCount";
    private JPromotionData.PromotionOfferings offeringList;

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionUtils.class.getSimpleName());

    public PromotionUtils(final EnvironmentVariables environmentVariables) {
        resource = new PelicanClient(environmentVariables).platform();
        dateFormat.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));
    }

    /**
     * This method is used to dynamically create a discount percentage promotion with subscription offers or basic
     * offerings
     *
     * @return JPromotion
     */
    public JPromotion getDiscountPercentPromo(final JStore store, final Offerings offering, final String promoCode,
        final boolean isStorewide, final Status status, final Double discountPercent, final Date expirationDate,
        final int numberOfBillingCycles) {
        final String promoName = "GetDiscountPercentPromoName-" + UUID.randomUUID().toString();
        final String promoDescription = "GetDiscountPercentPromoDesc-" + UUID.randomUUID().toString();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;

        final JPromotion promoRequest = new JPromotion();
        final JPromotionData promoData = new JPromotionData();
        promoData.setType(EntityType.PROMOTION);
        promoData.setName(promoName);
        promoData.setDescription(promoDescription);
        promoData.setCustomPromoCode(promoCode);
        promoData.setStatus(status);
        promoData.setPromotionType(promoType);
        promoData.setStoreWide(isStorewide);
        promoData.setDiscountPercent(discountPercent);
        promoData.setStoreIds(Lists.newArrayList(store.getId()));
        promoData.setNumberOfBillingCycles(numberOfBillingCycles);

        final JPromotionData.PromotionOfferings promoOffering = new JPromotionData.PromotionOfferings();
        promoOffering.setQuantity(1);
        promoOffering.setApplyDiscount(true);

        if (OfferingType.PERPETUAL == offering.getOfferings().get(0).getOfferingType()) {
            promoOffering.setId(offering.getOfferings().get(0).getId());
            promoData.setBasicOfferings(Lists.newArrayList(promoOffering));
        } else {
            promoOffering.setId(offering.getIncluded().getBillingPlans().get(0).getId());
            promoData.setSubscriptionOffers(Lists.newArrayList(promoOffering));
        }

        final String promoStartDate =
            DateTimeUtils.getYesterdayUTCDatetimeAsString(PelicanConstants.DATE_WITH_TIME_ZONE);
        final String promoExpirationDate = dateFormat.format(expirationDate);
        promoData.setEffectiveDate(promoStartDate);
        promoData.setExpirationDate(promoExpirationDate);
        promoRequest.setData(promoData);

        JPromotion createdPromo;
        createdPromo = resource.promotion().addPromotion(promoRequest);

        LOGGER.info("Id of the created promotion: " + createdPromo.getData().getId());
        LOGGER.info("EntityType of the created Promotion: " + createdPromo.getData().getType());

        // The response from add promotion API currently has only promoId and entityType. So, inorder to
        // validate
        // the data from get promotions API, I am populating the request data (i.e. name, status,
        // discount etc.)
        // in the response pojo of add promotion API call.
        createdPromo.getData().setName(promoName);
        createdPromo.getData().setDescription(promoDescription);
        createdPromo.getData().setCustomPromoCode(promoCode);
        createdPromo.getData().setStatus(status);
        createdPromo.getData().setPromotionType(promoType);
        createdPromo.getData().setDiscountPercent(discountPercent);
        createdPromo.getData().setStoreWide(isStorewide);
        createdPromo.getData().setNumberOfBillingCycles(numberOfBillingCycles);

        final JPromotionData.PromotionOfferings createdPromoOffering = new JPromotionData.PromotionOfferings();
        createdPromoOffering.setQuantity(1);
        createdPromoOffering.setApplyDiscount(true);

        if (OfferingType.PERPETUAL == offering.getOfferings().get(0).getOfferingType()) {
            createdPromoOffering.setId(offering.getOfferings().get(0).getId());
            createdPromo.getData().setBasicOfferings(Lists.newArrayList(createdPromoOffering));
        } else {
            createdPromoOffering.setId(offering.getIncluded().getBillingPlans().get(0).getId());
            createdPromo.getData().setSubscriptionOffers(Lists.newArrayList(createdPromoOffering));

        }
        return createdPromo;
    }

    /**
     * This method is used to dynamically create a promotion with subscription offers or basic offerings.
     *
     * @return JPromotion
     */
    public JPromotion addPromotion(final PromotionType promotionType, final List<JStore> store,
        final List<Offerings> offerings, final String promoCode, final boolean isStorewide, final Status status,
        final String discountPercent, final String discountAmount, final Date expirationDate,
        final String timePeriodCount, final String timePeriodType, final Integer numberOfBillingCycles,
        final String maxUses, final String maxUsesPerUser) {
        final String promoName = "PromoName-" + UUID.randomUUID().toString();
        final String promoDescription = "PromoDesc-" + UUID.randomUUID().toString();
        // setting up the promotion payload to add a promotion
        final JPromotion promoRequest = new JPromotion();
        final JPromotionData promoData = new JPromotionData();
        promoData.setType(EntityType.PROMOTION);
        promoData.setName(promoName);
        promoData.setDescription(promoDescription);
        promoData.setCustomPromoCode(promoCode);
        promoData.setStatus(status);
        promoData.setPromotionType(promotionType);
        promoData.setStoreWide(isStorewide);

        // creating store id list from jstore list
        final List<String> storeIds = new ArrayList<>();
        for (final JStore aStore : store) {
            storeIds.add(aStore.getId());
        }

        // if promotion type is discount percentage, set discount percent
        if (promotionType == PromotionType.DISCOUNT_PERCENTAGE) {
            promoData.setDiscountPercent(Double.parseDouble(discountPercent));
        } else if (promotionType == PromotionType.DISCOUNT_AMOUNT) {
            // if promotion type is discount amount, set pricelist and amount

            final JPromotionData.PriceList priceList = new JPromotionData.PriceList();
            final List<PriceList> priceLists = new ArrayList<>();

            // Setting price list for all stores
            for (final JStore jstore : store) {
                priceList.setId(jstore.getIncluded().getPriceLists().get(0).getId());
                priceList.setDiscountAmount(Double.parseDouble(discountAmount));
                priceLists.add(priceList);
            }

            promoData.setPriceLists(priceLists);
        } else {
            // else if promotion type is supplement type, add time period count and time period type
            promoData.setTimePeriodCount(Integer.parseInt(timePeriodCount));
            promoData.setTimePeriodType(timePeriodType);
        }

        promoData.setStoreIds(storeIds);
        promoData.setNumberOfBillingCycles(numberOfBillingCycles);

        // Setting offering
        final ArrayList<JPromotionData.PromotionOfferings> basicOffering = new ArrayList<>();
        final ArrayList<JPromotionData.PromotionOfferings> subscriptionOffers = new ArrayList<>();
        JPromotionData.PromotionOfferings joffering;

        // creating list of offering
        for (final Offerings offering : offerings) {
            if (offering.getOfferings().get(0).getOfferingType().equals(OfferingType.PERPETUAL)) {
                joffering = new JPromotionData.PromotionOfferings();
                joffering.setId(offering.getOfferings().get(0).getId());
                joffering.setQuantity(1);
                joffering.setApplyDiscount(true);
                basicOffering.add(joffering);
            } else {
                joffering = new JPromotionData.PromotionOfferings();
                joffering.setId(offering.getIncluded().getBillingPlans().get(0).getId());
                joffering.setQuantity(1);
                joffering.setApplyDiscount(true);
                subscriptionOffers.add(joffering);

            }
        }
        // setting basic offering
        if (basicOffering.size() > 0) {
            promoData.setBasicOfferings(basicOffering);
        } else {
            // else setting subscription offering
            promoData.setSubscriptionOffers(subscriptionOffers);
        }

        final String promoStartDate = DateTimeUtils.getYesterdayUTCDatetimeAsString("MM/dd/yyyy HH:mm:ss zzz");
        final String promoExpirationDate = dateFormat.format(expirationDate);
        promoData.setEffectiveDate(promoStartDate);
        promoData.setExpirationDate(promoExpirationDate);
        // setting up maximum uses and maximum uses per user
        if (maxUses != null) {
            promoData.setMaxUses(Integer.parseInt(maxUses));
        }
        if (maxUsesPerUser != null) {
            promoData.setMaxUsesPerUser(Integer.parseInt(maxUsesPerUser));
        }
        promoRequest.setData(promoData);

        JPromotion createdPromo;
        createdPromo = resource.promotion().addPromotion(promoRequest);
        LOGGER.info("Id of the created promotion: " + createdPromo.getData().getId());
        LOGGER.info("EntityType of the created Promotion: " + createdPromo.getData().getType());
        // The response from add promotion API currently has only promoId and entityType. So, inorder to
        // validate
        // the data from get promotions API, thats why populating the request data (i.e. name, status,
        // discount etc.)
        // in the response pojo of add promotion API call.
        createdPromo.getData().setName(promoName);
        createdPromo.getData().setDescription(promoDescription);
        createdPromo.getData().setCustomPromoCode(promoCode);
        createdPromo.getData().setStatus(status);
        if (promotionType.equals(PromotionType.DISCOUNT_PERCENTAGE)) {
            createdPromo.getData().setDiscountPercent(Double.parseDouble(discountPercent));
        } else if (promotionType.equals(PromotionType.DISCOUNT_AMOUNT)) {
            createdPromo.getData().setDiscountAmount(Double.parseDouble(discountAmount));
        } else {
            createdPromo.getData().setTimePeriodCount(Integer.parseInt(timePeriodCount));
        }
        createdPromo.getData().setTimePeriodType(timePeriodType);
        createdPromo.getData().setPromotionType(promotionType);
        createdPromo.getData().setStoreWide(isStorewide);
        createdPromo.getData().setNumberOfBillingCycles(numberOfBillingCycles);
        createdPromo.getData().setStoreIds(storeIds);
        createdPromo.getData().setExpirationDate(expirationDate.toString());
        createdPromo.getData().setEffectiveDate(promoStartDate);
        if (maxUses != null) {
            createdPromo.getData().setMaxUses(Integer.parseInt(maxUses));
        }
        if (maxUsesPerUser != null) {
            createdPromo.getData().setMaxUsesPerUser(Integer.parseInt(maxUsesPerUser));
        }

        offeringList = new JPromotionData.PromotionOfferings();
        offeringList.setQuantity(1);
        offeringList.setApplyDiscount(true);

        for (final Offerings offering : offerings) {
            if (offering.getOfferings().get(0).getOfferingType().equals(OfferingType.PERPETUAL)) {
                offeringList.setId(offering.getOfferings().get(0).getId());
                createdPromo.getData().setBasicOfferings(Lists.newArrayList(offeringList));
            } else {
                offeringList.setId(offering.getOfferings().get(0).getId());
                createdPromo.getData().setSubscriptionOffers(Lists.newArrayList(offeringList));
            }
        }
        return createdPromo;
    }

    /**
     * This method submits a PO with promotion This method is used to dynamically create a bundle promotion with
     * subscription offers or basic offerings.
     *
     * @return JPromotion
     */

    public JPromotion addBundlePromotion(final PromotionType promotionType, final List<JStore> store,
        final List<BundlePromoOfferings> offerings, final String promoCode, final boolean isStorewide,
        final Status status, final String discountPercent, final String discountAmount, final Date expirationDate,
        final Integer numberOfBillingCycles, final String maxUses, final String maxUsesPerUser) {

        final String promoName = "BundlePromoName-" + UUID.randomUUID().toString();
        final String promoDescription = "BundlePromoDesc-" + UUID.randomUUID().toString();

        // setting up the promotion payload to add a promotion

        final JPromotion promoRequest = new JPromotion();
        final JPromotionData promoData = new JPromotionData();
        promoData.setType(EntityType.PROMOTION);
        promoData.setName(promoName);
        promoData.setDescription(promoDescription);
        promoData.setCustomPromoCode(promoCode);
        promoData.setStatus(status);
        promoData.setPromotionType(promotionType);
        promoData.setStoreWide(isStorewide);
        promoData.setIsBundledPromo(true);

        // creating store id list from jstore list
        final List<String> storeIds = new ArrayList<>();
        for (final JStore aStore : store) {
            storeIds.add(aStore.getId());
        }

        // if promotion type is discount percentage, set discount percent
        if (promotionType == PromotionType.DISCOUNT_PERCENTAGE) {
            promoData.setDiscountPercent(Double.parseDouble(discountPercent));
        } else if (promotionType == PromotionType.DISCOUNT_AMOUNT) {
            // if promotion type is discount amount, set pricelist and amount
            final JPromotionData.PriceList priceList = new JPromotionData.PriceList();
            final List<PriceList> priceLists = new ArrayList<>();

            // Setting price list for all stores
            for (final JStore jstore : store) {
                priceList.setId(jstore.getIncluded().getPriceLists().get(0).getId());
                priceList.setDiscountAmount(Double.parseDouble(discountAmount));
                priceLists.add(priceList);
            }

            promoData.setPriceLists(priceLists);
        }

        promoData.setStoreIds(storeIds);
        promoData.setNumberOfBillingCycles(numberOfBillingCycles);

        // Setting offering
        final ArrayList<JPromotionData.PromotionOfferings> basicOffering = new ArrayList<>();
        final ArrayList<JPromotionData.PromotionOfferings> subscriptionOffering = new ArrayList<>();
        JPromotionData.PromotionOfferings promoOffering;

        // creating list of offering
        for (final BundlePromoOfferings offering : offerings) {
            if (offering.getBundleOfferings().getOfferings().get(0).getOfferingType().equals(OfferingType.PERPETUAL)
                || offering.getBundleOfferings().getOfferings().get(0).getOfferingType()
                    .equals(OfferingType.PHYSICAL_MEDIA)
                || offering.getBundleOfferings().getOfferings().get(0).getOfferingType() == OfferingType.CURRENCY) {
                promoOffering = new JPromotionData.PromotionOfferings();
                promoOffering.setId(offering.getBundleOfferings().getOfferings().get(0).getId());
                promoOffering.setQuantity(offering.getQuantity());
                promoOffering.setApplyDiscount(offering.getApplyDiscount());
                basicOffering.add(promoOffering);
            } else {
                promoOffering = new JPromotionData.PromotionOfferings();
                promoOffering.setId(offering.getBundleOfferings().getIncluded().getBillingPlans().get(0).getId());
                promoOffering.setQuantity(offering.getQuantity());
                promoOffering.setApplyDiscount(offering.getApplyDiscount());
                subscriptionOffering.add(promoOffering);
            }
        }
        // setting basic offering
        if (basicOffering.size() > 0) {
            promoData.setBasicOfferings(basicOffering);
        }
        if (subscriptionOffering.size() > 0) {
            promoData.setSubscriptionOffers(subscriptionOffering);
        }

        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC_TIME_ZONE));
        final Date startDate = calendar.getTime();
        final String promoStartDate = dateFormat.format(startDate);
        final String promoExpirationDate = dateFormat.format(expirationDate);
        promoData.setEffectiveDate(promoStartDate);
        promoData.setExpirationDate(promoExpirationDate);
        // setting up maximum uses and maximum uses per user

        if (maxUses != null) {
            promoData.setMaxUses(Integer.parseInt(maxUses));
        }
        if (maxUsesPerUser != null) {
            promoData.setMaxUsesPerUser(Integer.parseInt(maxUsesPerUser));
        }
        promoRequest.setData(promoData);

        JPromotion createdPromo;
        createdPromo = resource.promotion().addPromotion(promoRequest);
        LOGGER.info("Id of the created promotion: " + createdPromo.getData().getId());
        LOGGER.info("EntityType of the created Promotion: " + createdPromo.getData().getType());

        // The response from add promotion API currently has only promoId and entityType. So, inorder to
        // validate
        // the data from get promotions API, thats why populating the request data (i.e. name, status,
        // discount etc.)
        // in the response pojo of add promotion API call.
        createdPromo.getData().setName(promoName);
        createdPromo.getData().setDescription(promoDescription);
        createdPromo.getData().setCustomPromoCode(promoCode);
        createdPromo.getData().setStatus(status);
        createdPromo.getData().setIsBundledPromo(true);

        if (promotionType.equals(PromotionType.DISCOUNT_PERCENTAGE)) {
            createdPromo.getData().setDiscountPercent(Double.parseDouble(discountPercent));
        } else if (promotionType.equals(PromotionType.DISCOUNT_AMOUNT)) {
            createdPromo.getData().setDiscountAmount(Double.parseDouble(discountAmount));
        }

        createdPromo.getData().setPromotionType(promotionType);
        createdPromo.getData().setStoreWide(isStorewide);
        createdPromo.getData().setNumberOfBillingCycles(numberOfBillingCycles);
        createdPromo.getData().setStoreIds(storeIds);
        createdPromo.getData().setExpirationDate(expirationDate.toString());
        createdPromo.getData().setEffectiveDate(promoStartDate);
        if (maxUses != null) {
            createdPromo.getData().setMaxUses(Integer.parseInt(maxUses));
        }
        if (maxUsesPerUser != null) {
            createdPromo.getData().setMaxUsesPerUser(Integer.parseInt(maxUsesPerUser));
        }

        final List<JPromotionData.PromotionOfferings> basicOfferings = new ArrayList<>();
        final List<JPromotionData.PromotionOfferings> subscriptionOfferings = new ArrayList<>();
        for (final BundlePromoOfferings offering : offerings) {
            offeringList = new JPromotionData.PromotionOfferings();
            offeringList.setQuantity(offering.getQuantity());
            offeringList.setApplyDiscount(true);
            if (offering.getBundleOfferings().getOfferings().get(0).getOfferingType() == OfferingType.PERPETUAL
                || offering.getBundleOfferings().getOfferings().get(0).getOfferingType() == OfferingType.PHYSICAL_MEDIA
                || offering.getBundleOfferings().getOfferings().get(0).getOfferingType() == OfferingType.CURRENCY) {
                offeringList.setId(offering.getBundleOfferings().getOfferings().get(0).getId());
                basicOfferings.add(offeringList);
            } else {
                offeringList.setId(offering.getBundleOfferings().getIncluded().getBillingPlans().get(0).getId());
                final JPromotionData.PromotionOfferings subscriptionOffer = offeringList;
                subscriptionOfferings.add(subscriptionOffer);
            }
        }
        createdPromo.getData().setBasicOfferings(basicOfferings);
        createdPromo.getData().setSubscriptionOffers(subscriptionOfferings);
        return createdPromo;
    }

    public String getRandomPromoCode() {
        return CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
    }

    /**
     * This method is a helper method for creating payload for bundle promotion
     *
     * @param offerings
     * @param quantity
     * @param isApplyDiscount
     * @return
     */
    public BundlePromoOfferings createBundlePromotionOffering(final Offerings offerings, final int quantity,
        final boolean isApplyDiscount) {
        final BundlePromoOfferings bundlePromoOfferings = new BundlePromoOfferings();
        bundlePromoOfferings.setBundleOfferings(offerings);
        bundlePromoOfferings.setQuantity(quantity);
        bundlePromoOfferings.setApplyDiscount(isApplyDiscount);

        return bundlePromoOfferings;
    }
}
