package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.pojos.Entitlement;
import com.autodesk.bsm.pelican.api.pojos.json.BillingPlan;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptionEntitlementData;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.ECStatus;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class AssertionUtilsForGetSubscription extends BaseTestData {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(AssertionUtilsForGetSubscription.class.getSimpleName());

    public static void assertionsForBillingPlan(final BillingPlan billingPlanActual,
        final BillingPlan billingPlanExpected, final List<AssertionError> assertionErrorList) {
        LOGGER.info("Validating billing plan.");
        AssertCollector.assertThat("Incorrect type for billing plan under included", billingPlanActual.getType(),
            equalTo(EntityType.BILLINGPLAN), assertionErrorList);
        AssertCollector.assertThat("Incorrect id for billing plan under included", billingPlanActual.getId(),
            equalTo((billingPlanExpected.getId())), assertionErrorList);
        AssertCollector.assertThat("Incorrect app family id for billing plan under included",
            billingPlanActual.getAppFamilyId(), equalTo(getEnvironmentVariables().getAppFamilyId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect external key for billing plan under included",
            billingPlanActual.getExternalKey(), equalTo(billingPlanExpected.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect one time entitlements for billing plan under included",
            billingPlanActual.getName(), equalTo(billingPlanExpected.getName()), assertionErrorList);
    }

    public static void assertionsForOffering(final SubscriptionOffering actualOffering,
        final Offerings expectedOffering, final List<AssertionError> assertionErrorList) {
        LOGGER.info("Validating offering.");
        AssertCollector.assertThat("Incorrect type for offering under included", actualOffering.getEntityType(),
            equalTo(EntityType.OFFERING), assertionErrorList);
        AssertCollector.assertThat("Incorrect id for offering under included", actualOffering.getId(),
            equalTo(expectedOffering.getOfferings().get(0).getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect app family id for offering under included",
            actualOffering.getAppFamilyId(), equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect name for offering under included", actualOffering.getName(),
            equalTo(expectedOffering.getOfferings().get(0).getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect external key for offering under included",
            actualOffering.getExternalKey(), equalTo(expectedOffering.getOfferings().get(0).getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect support level for offering under included",
            actualOffering.getSupportLevel(),
            equalTo(expectedOffering.getOfferings().get(0).getSupportLevel().toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for offering under included",
            actualOffering.getStatus().toUpperCase(),
            equalTo(expectedOffering.getOfferings().get(0).getStatus().toUpperCase()), assertionErrorList);
        AssertCollector.assertThat("Incorrect cancellation policy for offering under included",
            actualOffering.getCancellationPolicy().getName(),
            equalTo(expectedOffering.getOfferings().get(0).getCancellationPolicy().getName()), assertionErrorList);
        AssertCollector.assertThat("Incorrect usage type for offering under included",
            actualOffering.getUsageType().getDisplayName(),
            equalTo(expectedOffering.getOfferings().get(0).getUsageType().getDisplayName()), assertionErrorList);

        // validate offering detail
        AssertCollector.assertThat("Incorrect code under offeringDetail for offering under included",
            actualOffering.getOfferingDetail().getCode(), equalTo(PelicanConstants.OFFERING_DETAILS1),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect description under offeringDetail for offering under included",
            actualOffering.getOfferingDetail().getDescription(), equalTo(PelicanConstants.OFFERING_DETAILS1),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect taxcode under offeringDetail for offering under included",
            actualOffering.getOfferingDetail().getTaxCode(), equalTo(getEnvironmentVariables().getTaxCode()),
            assertionErrorList);

        AssertCollector.assertThat("Incorrect isModule for offering under included", actualOffering.isModule(),
            equalTo(false), assertionErrorList);
        AssertCollector.assertThat("Incorrect code under productLine for offering under included",
            actualOffering.getJProductLine().getCode(),
            equalTo(expectedOffering.getOfferings().get(0).getProductLine()), assertionErrorList);
        AssertCollector.assertThat("Incorrect name under productLine for offering under included",
            actualOffering.getJProductLine().getName(),
            equalTo(expectedOffering.getOfferings().get(0).getProductLine()), assertionErrorList);
        AssertCollector.assertThat("Incorrect count under entitlementPeriod for offering under included",
            actualOffering.getEntitlementPeriod().getCount(),
            equalTo(expectedOffering.getOfferings().get(0).getEntitlementPeriod().getCount()), assertionErrorList);
        AssertCollector.assertThat("Incorrect type under entitlementPeriod for offering under included",
            actualOffering.getEntitlementPeriod().getType(),
            equalTo(expectedOffering.getOfferings().get(0).getEntitlementPeriod().getType()), assertionErrorList);
    }

    public static void assertionsForEntitlements(final List<Entitlement> oneTimeEntitlementsActual,
        final List<JSubscriptionEntitlementData> oneTimeEntitlementsExpected,
        final List<AssertionError> assertionErrorList) {

        LOGGER.info("Validating entitlements.");
        AssertCollector.assertThat("Incorrect oneTimeEntitlements for offering under included",
            oneTimeEntitlementsActual.size(), equalTo(oneTimeEntitlementsExpected.size()), assertionErrorList);

        for (int i = 0; i < oneTimeEntitlementsActual.size(); i++) {
            AssertCollector.assertThat("Incorrect oneTimeEntitlement id for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getId(), equalTo(oneTimeEntitlementsExpected.get(i).getId()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect oneTimeEntitlement name for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getName(), equalTo(oneTimeEntitlementsExpected.get(i).getName()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect oneTimeEntitlement external key for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getExternalKey(),
                equalTo(oneTimeEntitlementsExpected.get(i).getExternalKey()), assertionErrorList);
            AssertCollector.assertThat("Incorrect oneTimeEntitlement Type for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getType(), equalTo("ITEM"), assertionErrorList);
            AssertCollector.assertThat("Incorrect oneTimeEntitlement ItemTypeExternalKey for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getItemTypeExternalKey(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat(
                "Incorrect oneTimeEntitlement licensingModelExternalKey for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getLicensingModelExternalKey(), notNullValue(), assertionErrorList);
            AssertCollector.assertThat("Incorrect oneTimeEntitlement coreProducts for Entitlement number " + i,
                oneTimeEntitlementsActual.get(i).getCoreProducts().size(), equalTo(0), assertionErrorList);

        }
    }

    public static void assertionsForPrice(final Price actualPrice, final Price expectedPrice,
        final List<AssertionError> assertionErrorList) {
        LOGGER.info("Validating price.");
        // validate price
        AssertCollector.assertThat("Incorrect type under price under included", actualPrice.getType().toUpperCase(),

            equalTo(EntityType.PRICE.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect id under price under included", actualPrice.getId(),
            equalTo(expectedPrice.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect currency under price under included", actualPrice.getCurrency(),
            equalTo(expectedPrice.getCurrency()), assertionErrorList);
        AssertCollector.assertThat("Incorrect amount under price under included", actualPrice.getAmount(),
            equalTo(expectedPrice.getAmount()), assertionErrorList);
        AssertCollector.assertThat("Incorrect priceListId under price under included", actualPrice.getPricelistId(),
            equalTo(expectedPrice.getPricelistId()), assertionErrorList);

        AssertCollector.assertThat("Incorrect store id under price under included", actualPrice.getStoreId(),
            equalTo(expectedPrice.getStoreId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Store External key under price under included",
            actualPrice.getStoreExternalKey(), equalTo(expectedPrice.getStoreExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Pricelist External Key under price under included",
            expectedPrice.getPriceListExternalKey(), equalTo(expectedPrice.getPriceListExternalKey()),
            assertionErrorList);
    }

    public static void assertionsForSubscriptionData(final Subscription subscription, final Offerings offering,
        final Status status, final String storedPaymentProfileId, final Price price, final int quantity,
        final Date currentDateTimeStamp, final BillingPlan billingPlan, final List<AssertionError> assertionErrorList)
        throws ParseException {
        LOGGER.info("Validating subscription data.");
        // AssertCollector.assertThat("Incorrect type", subscription.getData().getType(),
        // equalTo(EntityType.SUBSCRIPTION));
        AssertCollector.assertThat("Incorrect createdDate", subscription.getCreatedDate().split(" ")[0],
            equalTo(DateTimeUtils.getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH)), assertionErrorList);
        AssertCollector.assertThat("Incorrect owner id", subscription.getOwnerId(), equalTo(getBuyerUser().getId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect owner external key", subscription.getOwnerExternalKey(),
            equalTo(getBuyerUser().getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Application Family Id ", subscription.getApplicationFamilyId(),
            equalTo(getEnvironmentVariables().getAppFamilyId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect status ", subscription.getStatus(), equalTo(status.toString()),
            assertionErrorList);
        if (status == Status.ACTIVE && offering.getOfferings().get(0).getUsageType() == UsageType.COM) {
            AssertCollector.assertTrue("Incorrect isAutoRenewEnabled ", subscription.isAutoRenewed(),
                assertionErrorList);
        } else {
            AssertCollector.assertFalse("Incorrect isAutoRenewEnabled ", subscription.isAutoRenewed(),
                assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect Export Control Status ", subscription.getExportControlStatus(),
            equalTo(ECStatus.ACCEPT.getDisplayName()), assertionErrorList);

        if (subscription.getNextBillingPriceAmount() != null
            && subscription.getStatus().equals(Status.ACTIVE.toString())) {
            AssertCollector.assertThat("Incorrect storedPaymentProfileId ", subscription.getStorePaymentProfileId(),
                equalTo(storedPaymentProfileId), assertionErrorList);
        }
        AssertCollector.assertThat("Incorrect offer id in data", subscription.getNextBillingOfferId(),
            equalTo(billingPlan.getId()), assertionErrorList);
        AssertCollector.assertThat("Incorrect currency id in data", subscription.getNextBillingPriceCurrencyName(),
            equalTo(price.getCurrency()), assertionErrorList);
        AssertCollector.assertThat("Incorrect nextBillingPriceCurrencyId", subscription.getNextBillingPriceCurrencyId(),
            equalTo(String.valueOf(Currency.valueOf(price.getCurrency()).getCode())), assertionErrorList);
        AssertCollector.assertThat("Incorrect credit days in data", subscription.getCreditDays(), equalTo(0),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect next billing date",
            DateTimeUtils.getDateStamp(subscription.getNextBillingDate()),
            equalTo(DateTimeUtils.getDateStamp(DateTimeUtils.getNowAsUTCPlusMonths(PelicanConstants.DATE_TIME_WITH_ZONE,
                Integer.parseInt(billingPlan.getBillingPeriodCount())))),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect priceId in data", subscription.getPriceId(), equalTo(price.getId()),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Quantity in data", subscription.getQuantity(), equalTo(quantity),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect Offering type ", subscription.getOfferingType(),
            equalTo(offering.getOfferings().get(0).getOfferingType().toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect lastModified",
            DateTimeUtils.convertStringToDate(subscription.getLastModified(), PelicanConstants.DB_DATE_FORMAT),
            greaterThanOrEqualTo(currentDateTimeStamp), assertionErrorList);
    }
}
