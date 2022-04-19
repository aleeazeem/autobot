package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.enums.Status;

import java.util.List;

public class Promotion extends BaseEntity {

    private String applicationFamily;
    private String application;
    private String name;
    private String description;
    private String state;
    private boolean storeWide;
    private boolean isBundled;
    private String promotionCode;
    private String promotionType;
    private String discountType;
    private String amount;
    private String cashAmount;
    private String percentage;
    private String percentageAmount;
    private String standalonePromotion;
    private String effectiveDate;
    private String expirationDate;
    private String maxUses;
    private String maxUsesPerUser;
    private boolean activatePromotion;
    private String addPromotion;
    private Status status;
    private String storeId;
    private String basicOfferings;
    private String offeringType;
    private String[] offeringsExternalKey;
    private String subscriptionOfferings;
    private Status activateStatus;
    private String timeInHours;
    private String timeInMinutes;
    private String timeInSeconds;
    private String expirationTimeInHours;
    private String expirationTimeInMinutes;
    private String expirationTimeInSeconds;
    private String supplementType;
    private String timePeriodCount;
    private String timePeriodType;
    private String value;
    private String basicOrSubscriptionOffering;
    private String dateTime;
    private String billingCycle;
    private String storeWideField;
    private String discountOrSupplementSubType;
    private String storeErrorMessage;
    private String windowTitle;
    private String discountErrorMessage;
    private String priceListExternalKey;
    private String priceListErrorMessage;
    private String basicOfferingErrorMessage;
    private String subscriptionOfferErrorMessage;
    private String storeWideErrorMessage;
    private String timeMismatchError;
    private String addOffering;
    private String promotionCodeErrorMessage;
    private int priceListSize;
    private String priceListMessage;
    private List<String> priceListNames;
    private List<String> currencyNames;
    private String amountMessage;
    private String offeringMessage;
    private String amountInputMessage;
    private List<String> basicOfferingsExternalKey;
    private List<String> subscriptionOfferingsExternalKey;
    private List<String> basicOfferingNameList;
    private List<String> subscriptionOfferNameList;
    private List<Integer> quantityOfBasicOfferingsList;
    private List<Integer> quantityOfSubscriptionOfferingsList;
    private List<Boolean> applyDiscountForBasicOfferingsList;
    private List<Boolean> applyDiscountForSubscriptionOfferingsList;
    private String errorMessageForBasicOfferingInvalidQuantity;
    private String errorMessageForSubscriptionOfferingInvalidQuantity;
    private String errorMessageForMoreBasicOfferings;
    private String errorMessageForMoreSubscriptionOfferings;

    public String getPercentage() {
        return percentage;
    }

    public String getSupplementType() {
        return supplementType;
    }

    public void setSupplementType(final String supplementType) {
        if (supplementType != null) {
            this.supplementType = supplementType;
        }
    }

    public String getTimePeriodCount() {
        return timePeriodCount;
    }

    public void setTimePeriodCount(final String timePeriodCount) {
        if (timePeriodCount != null) {
            this.timePeriodCount = timePeriodCount;
        }
    }

    public String getTimePeriodType() {
        return timePeriodType;
    }

    public void setTimePeriodType(final String timePeriodType) {
        if (timePeriodType != null) {
            this.timePeriodType = timePeriodType;
        }
    }

    public void setPercentage(final String percentage) {
        this.percentage = percentage;
    }

    public String[] getOfferingsExternalKey() {
        return offeringsExternalKey;
    }

    public void setOfferingsExternalKey(final String[] offeringsExternalKey) {
        if (offeringsExternalKey != null) {
            this.offeringsExternalKey = offeringsExternalKey;
        }
    }

    public String getApplicationFamily() {
        return applicationFamily;
    }

    public void setApplicationFamily(final String applicationFamily) {
        if (applicationFamily != null) {
            this.applicationFamily = applicationFamily;
        }
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(final String application) {
        if (application != null) {
            this.application = application;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        if (description != null) {
            this.description = description;
        }
    }

    public boolean getStoreWide() {
        return storeWide;
    }

    public void setStoreWide(final boolean storeWide) {
        this.storeWide = storeWide;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(final String promotionCode) {
        if (promotionCode != null) {
            this.promotionCode = promotionCode;
        }
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(final String promotionType) {
        if (promotionType != null) {
            this.promotionType = promotionType;
        }
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(final String discountType) {
        if (discountType != null) {
            this.discountType = discountType;
        }
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        if (amount != null) {
            this.amount = amount;
        }
    }

    public String getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(final String cashAmount) {
        if ((cashAmount) != null) {
            this.cashAmount = cashAmount;
        }
    }

    public String getStandalonePromotion() {
        return standalonePromotion;
    }

    public void setStandalonePromotion(final String standalonePromotion) {

        this.standalonePromotion = standalonePromotion;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final String effectiveDate) {
        if (effectiveDate != null) {
            this.effectiveDate = effectiveDate;
        }
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final String expirationDate) {
        if (expirationDate != null) {
            this.expirationDate = expirationDate;
        }
    }

    public String getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(final String maxUses) {
        if (maxUses != null) {
            this.maxUses = maxUses;
        }
    }

    public String getMaxUsesPerUser() {
        return maxUsesPerUser;
    }

    public void setMaxUsesPerUser(final String maxUsesPerUser) {
        if (maxUsesPerUser != null) {
            this.maxUsesPerUser = maxUsesPerUser;
        }
    }

    public boolean getActivatePromotion() {
        return activatePromotion;
    }

    public void setActivatePromotion(final boolean activatePromotion) {
        this.activatePromotion = activatePromotion;
    }

    public String getAddPromotion() {
        return addPromotion;
    }

    public void setAddPromotion(final String addPromotion) {
        if (addPromotion != null) {
            this.addPromotion = addPromotion;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(final String storeId) {
        if (storeId != null) {
            this.storeId = storeId;
        }
    }

    /**
     * @return the basicOfferings
     */
    public String getOfferingType() {
        return offeringType;
    }

    /**
     * @param offeringType the basicOfferings to set
     */
    public void setOfferingType(final String offeringType) {
        if (offeringType != null) {
            this.offeringType = offeringType;
        }
    }

    /**
     * @return the subscriptionOfferings
     */
    public String getSubscriptionOfferings() {
        return subscriptionOfferings;
    }

    /**
     * @param subscriptionOfferings the subscriptionOfferings to set
     */
    public void setSubscriptionOfferings(final String subscriptionOfferings) {
        this.subscriptionOfferings = subscriptionOfferings;
    }

    /**
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * @param basicOrSubscriptionOffering
     */
    public void setBasicOrSubscriptionOffering(final String basicOrSubscriptionOffering) {
        this.basicOrSubscriptionOffering = basicOrSubscriptionOffering;
    }

    /**
     * @return
     */
    public String getBasicOrSubscriptionOffering() {
        return basicOrSubscriptionOffering;
    }

    /**
     * @param dateTime
     */
    public void setEffectiveDateRange(final String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return
     */
    public String getEffectiveDateRange() {
        return dateTime;
    }

    /**
     * @param billingCycle
     */
    public void setNumberOfBillingCycles(final String billingCycle) {
        this.billingCycle = billingCycle;
    }

    /**
     * @return
     */
    public String getNumberOfBillingCycles() {
        return billingCycle;
    }

    /**
     * @return
     */
    public String getStoreWideField() {
        return storeWideField;
    }

    /**
     * @param storeWideField
     */
    public void setStoreWideField(final String storeWideField) {
        this.storeWideField = storeWideField;
    }

    /**
     * @return
     */
    public String getDiscountSupplementSubType() {
        return discountOrSupplementSubType;
    }

    /**
     * @param discountOrSupplementSubType
     */
    public void setDiscountOrSupplementSubType(final String discountOrSupplementSubType) {
        this.discountOrSupplementSubType = discountOrSupplementSubType;
    }

    /**
     * @return
     */

    public String getState() {
        return state;
    }

    /**
     * @param state
     */
    public void setState(final String state) {
        this.state = state;
    }

    /**
     * @return
     */
    public String getPercentageAmount() {
        return percentageAmount;
    }

    /**
     * @param percentageAmount
     */
    public void setPercentageAmount(final String percentageAmount) {
        if ((percentageAmount) != null) {
            this.percentageAmount = percentageAmount;
        }
    }

    /**
     * @return the basicOfferings
     */
    public String getBasicOfferings() {
        return basicOfferings;
    }

    /**
     * @param basicOfferings the basicOfferings to set
     */
    public void setBasicOfferings(final String basicOfferings) {
        this.basicOfferings = basicOfferings;
    }

    /**
     * @return
     */
    public Status getActivateStatus() {
        return activateStatus;
    }

    /**
     * @param activateStatus
     */
    public void setActivateStatus(final Status activateStatus) {
        this.activateStatus = activateStatus;
    }

    /**
     * @return
     */
    public String getTimeInHours() {
        return timeInHours;
    }

    /**
     * @param timeInHours
     */
    public void setTimeInHours(final String timeInHours) {
        this.timeInHours = timeInHours;
    }

    public String getTimeInMinutes() {
        return timeInMinutes;
    }

    public void setTimeInMinutes(final String timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }

    public String getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(final String timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public String getExpirationTimeInHours() {
        return expirationTimeInHours;
    }

    public void setExpirationTimeInHours(final String expirationTimeInHours) {
        this.expirationTimeInHours = expirationTimeInHours;
    }

    public String getExpirationTimeInMinutes() {
        return expirationTimeInMinutes;
    }

    public void setExpirationTimeInMinutes(final String expirationTimeInMinutes) {
        this.expirationTimeInMinutes = expirationTimeInMinutes;
    }

    public String getExpirationTimeInSeconds() {
        return expirationTimeInSeconds;
    }

    public void setExpirationTimeInSeconds(final String expirationTimeInSeconds) {
        this.expirationTimeInSeconds = expirationTimeInSeconds;
    }

    public String getStoreErrorMessage() {
        return storeErrorMessage;
    }

    public void setStoreErrorMessage(final String storeErrorMessage) {
        this.storeErrorMessage = storeErrorMessage;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(final String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public String getDiscountErrorMessage() {
        return discountErrorMessage;
    }

    public void setDiscountErrorMessage(final String discountErrorMessage) {
        this.discountErrorMessage = discountErrorMessage;
    }

    public String getPriceListExternalKey() {
        return priceListExternalKey;
    }

    public void setPriceListExternalKey(final String priceListExternalKey) {
        this.priceListExternalKey = priceListExternalKey;
    }

    public String getPriceListErrorMessage() {
        return priceListErrorMessage;
    }

    public void setPriceListErrorMessage(final String priceListErrorMessage) {
        this.priceListErrorMessage = priceListErrorMessage;
    }

    public String getBasicOfferingErrorMessage() {
        return basicOfferingErrorMessage;
    }

    public void setBasicOfferingErrorMessage(final String basicOfferingErrorMessage) {
        this.basicOfferingErrorMessage = basicOfferingErrorMessage;
    }

    public String getSubscriptionOfferErrorMessage() {
        return subscriptionOfferErrorMessage;
    }

    public void setSubscriptionOfferErrorMessage(final String subscriptionOfferErrorMessage) {
        this.subscriptionOfferErrorMessage = subscriptionOfferErrorMessage;
    }

    public String getStoreWideErrorMessage() {
        return storeWideErrorMessage;
    }

    public void setStoreWideErrorMessage(final String storeWideErrorMessage) {
        this.storeWideErrorMessage = storeWideErrorMessage;
    }

    public String getTimeMismatchError() {
        return timeMismatchError;
    }

    public void setTimeMismatchError(final String timeMismatchError) {
        this.timeMismatchError = timeMismatchError;
    }

    public String getAddOffering() {
        return addOffering;
    }

    public void setAddOffering(final String addOffering) {
        this.addOffering = addOffering;
    }

    public String getPromotionCodeErrorMessage() {
        return promotionCodeErrorMessage;
    }

    public void setPromotionCodeErrorMessage(final String promotionCodeErrorMessage) {
        this.promotionCodeErrorMessage = promotionCodeErrorMessage;
    }

    public int getPriceListSize() {
        return priceListSize;
    }

    public void setPriceListSize(final int priceListSize) {
        this.priceListSize = priceListSize;
    }

    public String getPriceListMessage() {
        return priceListMessage;
    }

    public void setPriceListMessage(final String priceListMessage) {
        this.priceListMessage = priceListMessage;
    }

    public List<String> getPriceListNames() {
        return priceListNames;
    }

    public void setPriceListNames(final List<String> priceListNames) {
        this.priceListNames = priceListNames;
    }

    public List<String> getCurrencyNames() {
        return currencyNames;
    }

    public void setCurrencyNames(final List<String> currencyNames) {
        this.currencyNames = currencyNames;
    }

    public String getAmountMessage() {
        return amountMessage;
    }

    public void setAmountMessage(final String amountMessage) {
        this.amountMessage = amountMessage;
    }

    public String getOfferingMessage() {
        return offeringMessage;
    }

    public void setOfferingMessage(final String offeringMessage) {
        this.offeringMessage = offeringMessage;
    }

    public String getAmountInputMessage() {
        return amountInputMessage;
    }

    public void setAmountInputMessage(final String amountInputMessage) {
        this.amountInputMessage = amountInputMessage;
    }

    public List<String> getBasicOfferingsExternalKey() {
        return basicOfferingsExternalKey;
    }

    public void setBasicOfferingsExternalKey(final List<String> basicOfferingsExternalKey) {
        this.basicOfferingsExternalKey = basicOfferingsExternalKey;
    }

    public List<String> getSubscriptionOfferingsExternalKey() {
        return subscriptionOfferingsExternalKey;
    }

    public void setSubscriptionOfferingsExternalKey(final List<String> subscriptionOfferingsExternalKey) {
        this.subscriptionOfferingsExternalKey = subscriptionOfferingsExternalKey;
    }

    public List<String> getBasicOfferingNameList() {
        return basicOfferingNameList;
    }

    public void setBasicOfferingNameList(final List<String> basicOfferingNameList) {
        this.basicOfferingNameList = basicOfferingNameList;
    }

    public List<String> getSubscriptionOfferNameList() {
        return subscriptionOfferNameList;
    }

    public void setSubscriptionOfferNameList(final List<String> subscriptionOfferNameList) {
        this.subscriptionOfferNameList = subscriptionOfferNameList;
    }

    /**
     * @return the isBundled
     */
    public boolean isBundled() {
        return isBundled;
    }

    /**
     * @param isBundled the isBundled to set
     */
    public void setBundled(final boolean bundled) {
        this.isBundled = bundled;
    }

    /**
     * @return the quantityOfBasicOfferingsList
     */
    public List<Integer> getQuantityOfBasicOfferingsList() {
        return quantityOfBasicOfferingsList;
    }

    /**
     * @param quantityOfBasicOfferingsList the quantityOfBasicOfferingsList to set
     */
    public void setQuantityOfBasicOfferingsList(final List<Integer> quantityOfBasicOfferingsList) {
        this.quantityOfBasicOfferingsList = quantityOfBasicOfferingsList;
    }

    /**
     * @return the quantityOfSubscriptionOfferingsList
     */
    public List<Integer> getQuantityOfSubscriptionOfferingsList() {
        return quantityOfSubscriptionOfferingsList;
    }

    /**
     * @param quantityOfSubscriptionOfferingsList the quantityOfSubscriptionOfferingsList to set
     */
    public void setQuantityOfSubscriptionOfferingsList(final List<Integer> quantityOfSubscriptionOfferingsList) {
        this.quantityOfSubscriptionOfferingsList = quantityOfSubscriptionOfferingsList;
    }

    /**
     * @return the applyDiscountForBasicOfferingsList
     */
    public List<Boolean> getApplyDiscountForBasicOfferingsList() {
        return applyDiscountForBasicOfferingsList;
    }

    /**
     * @param applyDiscountForBasicOfferingsList the applyDiscountForBasicOfferingsList to set
     */
    public void setApplyDiscountForBasicOfferingsList(final List<Boolean> applyDiscountForBasicOfferingsList) {
        this.applyDiscountForBasicOfferingsList = applyDiscountForBasicOfferingsList;
    }

    /**
     * @return the applyDiscountForSubscriptionOfferingsList
     */
    public List<Boolean> getApplyDiscountForSubscriptionOfferingsList() {
        return applyDiscountForSubscriptionOfferingsList;
    }

    /**
     * @param applyDiscountForSubscriptionOfferingsList the applyDiscountForSubscriptionOfferingsList to set
     */
    public void setApplyDiscountForSubscriptionOfferingsList(
        final List<Boolean> applyDiscountForSubscriptionOfferingsList) {
        this.applyDiscountForSubscriptionOfferingsList = applyDiscountForSubscriptionOfferingsList;
    }

    /**
     * @return the errorMessageForBasicOfferingInvalidQuantity
     */
    public String getErrorMessageForBasicOfferingInvalidQuantity() {
        return errorMessageForBasicOfferingInvalidQuantity;
    }

    /**
     * @param errorMessageForBasicOfferingInvalidQuantity the errorMessageForBasicOfferingInvalidQuantity to set
     */
    public void setErrorMessageForBasicOfferingInvalidQuantity(
        final String errorMessageForBasicOfferingInvalidQuantity) {
        this.errorMessageForBasicOfferingInvalidQuantity = errorMessageForBasicOfferingInvalidQuantity;
    }

    /**
     * @return the errorMessageForSubscriptionOfferingInvalidQuantity
     */
    public String getErrorMessageForSubscriptionOfferingInvalidQuantity() {
        return errorMessageForSubscriptionOfferingInvalidQuantity;
    }

    /**
     * @param errorMessageForSubscriptionOfferingInvalidQuantity the errorMessageForSubscriptionOfferingInvalidQuantity
     *        to set
     */
    public void setErrorMessageForSubscriptionOfferingInvalidQuantity(
        final String errorMessageForSubscriptionOfferingInvalidQuantity) {
        this.errorMessageForSubscriptionOfferingInvalidQuantity = errorMessageForSubscriptionOfferingInvalidQuantity;
    }

    /**
     * @return the errorMessageForMoreBasicOfferings
     */
    public String getErrorMessageForMoreBasicOfferings() {
        return errorMessageForMoreBasicOfferings;
    }

    /**
     * @param errorMessageForMoreBasicOfferings the errorMessageForMoreBasicOfferings to set
     */
    public void setErrorMessageForMoreBasicOfferings(final String errorMessageForMoreBasicOfferings) {
        this.errorMessageForMoreBasicOfferings = errorMessageForMoreBasicOfferings;
    }

    /**
     * @return the errorMessageForMoreSubscriptionOfferings
     */
    public String getErrorMessageForMoreSubscriptionOfferings() {
        return errorMessageForMoreSubscriptionOfferings;
    }

    /**
     * @param errorMessageForMoreSubscriptionOfferings the errorMessageForMoreSubscriptionOfferings to set
     */
    public void setErrorMessageForMoreSubscriptionOfferings(final String errorMessageForMoreSubscriptionOfferings) {
        this.errorMessageForMoreSubscriptionOfferings = errorMessageForMoreSubscriptionOfferings;
    }
}
