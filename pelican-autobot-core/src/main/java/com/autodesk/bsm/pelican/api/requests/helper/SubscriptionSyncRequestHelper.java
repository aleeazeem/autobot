package com.autodesk.bsm.pelican.api.requests.helper;

import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.requests.SubscriptionSyncRequest;
import com.autodesk.bsm.pelican.api.requests.SubscriptionSyncRequestBuilder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SubscriptionSyncRequestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionSyncRequestHelper.class.getSimpleName());

    public static SubscriptionSyncRequest constructRequestBody(final Subscription subscription,
        final SubscriptionSyncRequestBuilder builder, final long purchaseOrderId, final long paymentGatewayConfigId,
        final long priceId) {

        final SubscriptionSyncRequest request = new SubscriptionSyncRequest();
        request.setId(Long.parseLong(subscription.getId()));
        request.setExternalKey(subscription.getExternalKey());
        request.setCreated(getStringAsDateTime(subscription.getCreatedDate(), false));
        request.setAppFamilyId(Long.parseLong(subscription.getApplicationFamilyId()));
        request.setLastModified(new DateTime());
        request.setStatus(SubscriptionStatus.valueOf(subscription.getStatus()).ordinal());
        request.setQuantity(subscription.getQuantity());
        request.setQuantityToReduce(subscription.getQtyToReduce());
        request.setUsageType(builder.getUsageType().ordinal());
        request.setOfferingType(OfferingType.valueOf(builder.getPlan().getOfferingType()).ordinal());
        request.setNextBillingDate(getStringAsDateTime(builder.getNewNextBillingDate(), true));
        request.setExpirationDate(getStringAsDateTime(subscription.getExpirationDate(), false));
        request.setResolveByDate(new DateTime());
        if (subscription.getExportControlStatus() != null) {
            request.setEcStatus(ECStatus.valueOf(subscription.getExportControlStatus()).ordinal());
        }
        request.setEcStatusLastUpdated(new DateTime());
        request.setUserId(Long.parseLong(subscription.getOwnerId()));
        request.setUserExternalKey(builder.getUserExternalKey());
        request.setPlanId(Long.parseLong(builder.getPlan().getId()));
        request.setPlanExternalKey(builder.getPlan().getExternalKey());
        request.setOfferId(
            Long.parseLong(builder.getPlan().getSubscriptionOffers().getSubscriptionOffers().get(0).getId()));
        request.setPriceId(priceId);
        request.setPaymentProfileId(Long.parseLong(subscription.getStorePaymentProfileId()));
        request.setAddedToSubscriptionId(Long.parseLong(subscription.getId()));
        request.setDaysCredited(subscription.getCreditDays());
        request.setBillingCount(0);
        request.setPaymentPending(false);
        request.setLastRenewalReminderTimeStamp(null);
        request.setEmailRemindersEnabled(false);
        request.setAutoRenewEnabled(true);
        if (builder.getPromotionId() > 0 && builder.getPromotionCyclesRemaining() > 0) {
            request.setPromotionId(builder.getPromotionId());
            request.setPromotionCyclesRemaining(builder.getPromotionCyclesRemaining());
            request.setPromotionCyclesUsed(builder.getAppliedCount());
        }
        request.setPurchaseOrderId(purchaseOrderId);
        request.setPaymentGatewayConfigId(paymentGatewayConfigId);
        return request;
    }

    public static String getSubscriptionAsRequestBodyString(final SubscriptionSyncRequest request) {
        return getJsonBuilder().toJson(request);
    }

    private static Gson getJsonBuilder() {
        return new GsonBuilder().registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc,
            context) -> new JsonPrimitive(ISODateTimeFormat.dateTime().print(json))).create();
    }

    private static String formatDate(final String inputDate) {
        String outputDate = "";
        if (inputDate != null) {
            try {
                final Date date = PelicanConstants.DATE_TIME_FORMAT_WITH_TIME_ZONE.parse(inputDate);
                outputDate = PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE_IN_SECONDS.format(date);
            } catch (final Exception ex) {
                LOGGER.info(
                    "Unable to format date: " + PelicanConstants.DATE_TIME_FORMAT_WITH_TIME_ZONE + ex.getMessage());
                ex.printStackTrace();
            }
        }
        return outputDate;
    }

    private static String formatNextBillingDateDate(final String inputDate) {
        String outputDate = "";
        final SimpleDateFormat DATE_TIME_FORMAT_WITH_TIME_ZONE_UTC =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        DATE_TIME_FORMAT_WITH_TIME_ZONE_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        PelicanConstants.DATE_TIME_FORMAT_NEXT_BILLING_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (inputDate != null) {
            try {
                final Date date = PelicanConstants.DATE_TIME_FORMAT_NEXT_BILLING_DATE.parse(inputDate);
                outputDate = DATE_TIME_FORMAT_WITH_TIME_ZONE_UTC.format(date);
            } catch (final Exception ex) {
                LOGGER.info(
                    "Unable to format date: " + PelicanConstants.DATE_TIME_FORMAT_NEXT_BILLING_DATE + ex.getMessage());
                ex.printStackTrace();
            }
        }
        return outputDate;
    }

    /**
     * This is a method to convert String to Date Time object
     *
     * @param dateTime
     * @return DateTime Object
     */
    private static DateTime getStringAsDateTime(final String dateTime, final boolean isNextRenewalBillingDate) {

        if (!isNextRenewalBillingDate) {
            return StringUtils.isNotEmpty(dateTime) ? new DateTime(formatDate(dateTime)) : null;
        } else {
            return StringUtils.isNotEmpty(dateTime) ? new DateTime(formatNextBillingDateDate(dateTime)) : null;
        }
    }
}
