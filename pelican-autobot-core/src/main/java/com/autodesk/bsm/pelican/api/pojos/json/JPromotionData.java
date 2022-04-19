package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.DiscountType;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;

import java.util.List;

/**
 * This class represents the JSON object of Promotion.
 *
 * @author t_mohag
 */
public class JPromotionData {

    private EntityType type;
    private String id;
    private String name;
    private String description;
    private String customPromoCode;
    private Integer maxUsesPerUser;
    private Integer maxUses;
    private String effectiveDate;
    private String expirationDate;
    private Status status;
    private PromotionType promotionType;
    private Double discountPercent;
    private Double discountAmount;
    private DiscountType discountType;
    private String supplementType;
    public Descriptors descriptors;
    private Meta meta;
    private List<String> storeIds;
    private boolean storeWide;
    private List<PriceList> priceLists;
    private List<PromotionOfferings> basicOfferings;
    private List<PromotionOfferings> subscriptionOffers;
    private String offeringType;
    private Integer numberOfBillingCycles;
    private int timePeriodCount;
    private String timePeriodType;
    private boolean isBundledPromo;
    private boolean isBundled;

    public static class Meta {
        private String requestedCode;

        public String getRequestedCode() {
            return requestedCode;
        }

        public void setRequestedCode(final String requestedCode) {
            this.requestedCode = requestedCode;
        }
    }

    public static class PriceList {
        private String id;
        private double amount;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public Double getDiscountAmount() {
            return amount;
        }

        public void setDiscountAmount(final Double amount) {
            this.amount = amount;
        }
    }

    public static class PromotionOfferings {
        private String id;
        private int qty;
        private boolean applyDiscount;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public int getQuantity() {
            return qty;
        }

        public void setQuantity(final int qty) {
            this.qty = qty;
        }

        public boolean getApplyDiscount() {
            return applyDiscount;
        }

        public void setApplyDiscount(final boolean applyDiscount) {
            this.applyDiscount = applyDiscount;
        }

    }

    public EntityType getType() {
        return type;
    }

    public void setType(final EntityType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCustomPromoCode() {
        return customPromoCode;
    }

    public void setCustomPromoCode(final String customPromoCode) {
        this.customPromoCode = customPromoCode;
    }

    public Integer getMaxUsesPerUser() {
        return maxUsesPerUser;
    }

    public void setMaxUsesPerUser(final Integer maxUsesPerUser) {
        this.maxUsesPerUser = maxUsesPerUser;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(final Integer maxUses) {
        this.maxUses = maxUses;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public PromotionType getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(final PromotionType promotionType) {
        this.promotionType = promotionType;
    }

    public String getSupplementType() {
        return supplementType;
    }

    public void setSupplementType(final String supplementType) {
        this.supplementType = supplementType;
    }

    public Descriptors getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(final Descriptors descriptors) {
        this.descriptors = descriptors;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(final Meta meta) {
        this.meta = meta;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(final DiscountType discountType) {
        this.discountType = discountType;
    }

    public boolean isStoreWide() {
        return storeWide;
    }

    public void setStoreWide(final boolean storeWide) {
        this.storeWide = storeWide;
    }

    public List<String> getStoreIds() {
        return storeIds;
    }

    public void setStoreIds(final List<String> storeIds) {
        this.storeIds = storeIds;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(final Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(final Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<PromotionOfferings> getBasicOfferings() {
        return basicOfferings;
    }

    public void setBasicOfferings(final List<PromotionOfferings> basicOfferings) {
        this.basicOfferings = basicOfferings;
    }

    public List<PromotionOfferings> getSubscriptionOffers() {
        return subscriptionOffers;
    }

    public void setSubscriptionOffers(final List<PromotionOfferings> subscriptionOffers) {
        this.subscriptionOffers = subscriptionOffers;
    }

    public List<PriceList> getPriceLists() {
        return priceLists;
    }

    public void setPriceLists(final List<PriceList> priceLists) {
        this.priceLists = priceLists;
    }

    public void setOfferingType(final String offeringType) {
        this.offeringType = offeringType;
    }

    public String getOfferingType() {
        return offeringType;
    }

    public void setNumberOfBillingCycles(final Integer numberOfBillingCycles) {
        this.numberOfBillingCycles = numberOfBillingCycles;
    }

    public Integer getNumberOfBillingCycles() {
        return numberOfBillingCycles;
    }

    public void setTimePeriodCount(final int timePeriodCount) {
        this.timePeriodCount = timePeriodCount;
    }

    public int getTimePeriodCount() {
        return timePeriodCount;
    }

    public void setTimePeriodType(final String timePeriodType) {
        this.timePeriodType = timePeriodType;
    }

    public String getTimePeriodType() {
        return timePeriodType;
    }

    public void setIsBundledPromo(final boolean isBundledPromo) {
        this.isBundledPromo = isBundledPromo;
    }

    public boolean getIsBundledPromo() {
        return isBundledPromo;
    }

    public void setIsBundled(final boolean isBundled) {
        this.isBundled = isBundled;
    }

    public boolean getIsBundled() {
        return isBundled;
    }
}
