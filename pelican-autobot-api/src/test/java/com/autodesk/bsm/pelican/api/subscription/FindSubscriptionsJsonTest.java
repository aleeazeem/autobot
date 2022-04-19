package com.autodesk.bsm.pelican.api.subscription;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.SubscriptionsClient.FieldName;
import com.autodesk.bsm.pelican.api.clients.UserClient.UserParameter;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscription;
import com.autodesk.bsm.pelican.api.pojos.json.JSubscriptions;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder.OrderCommand;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SubscriptionStatus;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Find Subscriptions API Test: Test methods to test scenarios of all the Subscription APIs
 *
 * @author Muhammad
 */
public class FindSubscriptionsJsonTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private HashMap<String, String> requestParametersMap;
    private JSubscriptions subscriptions;
    private static final String INCLUDE_ENTITLEMENTS = "offering.entitlements";
    private static final String USER_EXTERNAL_KEY = RandomStringUtils.randomAlphanumeric(10);
    private static final int TOTAL_NO_OF_COMMERCIAL_SUBSCRIPTIONS = 1;
    private static final int TOTAL_NO_OF_TRIAL_SUBSCRIPTIONS = 2;
    private static List<JSubscription> allSubscriptions = new ArrayList<>();
    private static String userExternalKey;
    private static String paymentProfileId;
    private static Offerings trialOffering;
    private static Offerings commercialOffering;
    private static final Date currentDateTimeStamp = DateTimeUtils.convertStringToDate(
        DateTimeUtils.getCurrentDate(PelicanConstants.DB_DATE_FORMAT), PelicanConstants.DB_DATE_FORMAT);
    private static BuyerUser buyerUser;
    private static SubscriptionPlanApiUtils subscriptionPlanApiUtils;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        resource = new PelicanClient(getEnvironmentVariables()).platform();
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        User user = createUser();
        userExternalKey = user.getExternalKey();
        createCommercialSubscriptions(TOTAL_NO_OF_COMMERCIAL_SUBSCRIPTIONS);
        createTrialSubscriptions(TOTAL_NO_OF_TRIAL_SUBSCRIPTIONS);
    }

    /**
     * Verify Find Subscriptions API with user external key
     *
     * @throws ParseException
     */
    @Test
    public void findSubscriptionsByUserExtKey() throws ParseException {
        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), USER_EXTERNAL_KEY);
        requestParametersMap.put(FieldName.INCLUDE.getName(), INCLUDE_ENTITLEMENTS);
        subscriptions = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);

        AssertCollector.assertThat("Total number of subscriptions are not correct: ",
            subscriptions.getData().getSubscriptions().size(),
            equalTo(TOTAL_NO_OF_COMMERCIAL_SUBSCRIPTIONS + TOTAL_NO_OF_TRIAL_SUBSCRIPTIONS), assertionErrorList);
        Offerings offering = null;
        int index = 0;
        for (final Subscription subscription : subscriptions.getData().getSubscriptions()) {
            if (subscriptions.getData().getSubscriptions().get(index).getNextBillingPriceAmount() == null) {
                offering = trialOffering;
            } else {
                offering = commercialOffering;
            }
            AssertionUtilsForGetSubscription.assertionsForSubscriptionData(subscription, offering, Status.ACTIVE,
                paymentProfileId, offering.getIncluded().getPrices().get(0), 1, currentDateTimeStamp,
                offering.getIncluded().getBillingPlans().get(0), assertionErrorList);
            index++;
        }
        for (int i = 0; i < subscriptions.getIncluded().getOfferings().size(); i++) {
            if (subscriptions.getIncluded().getOfferings().get(i).getUsageType() == UsageType.COM) {
                offering = commercialOffering;
            } else {
                offering = trialOffering;
            }
            AssertionUtilsForGetSubscription.assertionsForOffering(subscriptions.getIncluded().getOfferings().get(i),
                offering, assertionErrorList);
            AssertionUtilsForGetSubscription.assertionsForEntitlements(
                subscriptions.getIncluded().getOfferings().get(i).getOneTimeEntitlements(),
                offering.getOfferings().get(0).getOneTimeEntitlements(), assertionErrorList);
            AssertionUtilsForGetSubscription.assertionsForBillingPlan(
                subscriptions.getIncluded().getBillingPlans().get(i), offering.getIncluded().getBillingPlans().get(0),
                assertionErrorList);
            AssertionUtilsForGetSubscription.assertionsForPrice(subscriptions.getIncluded().getPrices().get(i),
                offering.getIncluded().getPrices().get(0), assertionErrorList);
        }
    }

    /**
     * Verify Find Subscriptions API with days past expired
     *
     * @throws ParseException
     */
    @Test
    public void findSubscriptionsByDaysPastExpired() throws ParseException {

        requestParametersMap = new HashMap<>();
        requestParametersMap.put(FieldName.USER_EXTERNAL_KEY.getName(), getUser().getExternalKey());
        requestParametersMap.put(FieldName.DAYS_PAST_EXPIRED.getName(), "1");
        requestParametersMap.put(FieldName.STATUSES.getName(), SubscriptionStatus.EXPIRED.toString());
        requestParametersMap.put(FieldName.BLOCK_SIZE.getName(), "3");
        // Get subscriptions with days past expired
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        final Date expirationDate = cal.getTime();
        subscriptions = resource.subscriptions().getSubscriptions(requestParametersMap, PelicanConstants.CONTENT_TYPE);

        for (final Subscription subscription : subscriptions.getData().getSubscriptions()) {
            AssertCollector.assertThat("Incorrect expiration date for subscription: " + subscription.getId(),
                dateFormat.parse(subscription.getExpirationDate()), greaterThanOrEqualTo(expirationDate),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    private JSubscription createSubscription(final String priceId) {
        final PurchaseOrderUtils purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final String purchaseOrderId = purchaseOrderUtils
            .submitNewAcquisitionPurchaseOrder(PaymentType.CREDIT_CARD, priceId, buyerUser, 1).getId();
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.PENDING, purchaseOrderId);
        purchaseOrderUtils.processPurchaseOrder(OrderCommand.CHARGE, purchaseOrderId);
        final PurchaseOrder purchaseOrder = resource.purchaseOrder().getById(purchaseOrderId);
        paymentProfileId = purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId();
        return resource.subscriptionJson().getSubscription(
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId(),
            "offering.entitlements", PelicanConstants.CONTENT_TYPE);

    }

    private void createCommercialSubscriptions(final int totalSubscriptions) {
        final Offerings bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String bicPriceId = bicOffering.getIncluded().getPrices().get(0).getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering.getOfferings().get(0).getId(),
            null, null, true);
        commercialOffering = resource.offerings().getOfferingById(bicOffering.getOfferings().get(0).getId(),
            "offers,prices,entitlements");
        for (int i = 0; i < totalSubscriptions; i++) {
            final JSubscription subscription = createSubscription(bicPriceId);
            allSubscriptions.add(subscription);
        }
    }

    private void createTrialSubscriptions(final int totalSubscriptions) {
        final Offerings offeringTrial =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        final String trialOfferExternalKey = offeringTrial.getIncluded().getBillingPlans().get(0).getExternalKey();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(offeringTrial.getOfferings().get(0).getId(),
            null, null, true);
        trialOffering = resource.offerings().getOfferingById(offeringTrial.getOfferings().get(0).getId(),
            "offers,prices,entitlements");

        for (int i = 0; i < totalSubscriptions; i++) {
            final String trialSubscriptionId =
                resource.subscription().add(userExternalKey, trialOfferExternalKey, Currency.USD).getId();
            final JSubscription subscription = resource.subscriptionJson().getSubscription(trialSubscriptionId,
                "offering.entitlements", PelicanConstants.CONTENT_TYPE);
            allSubscriptions.add(subscription);
        }
    }

    private User createUser() {
        final Map<String, String> userRequestParam = new HashMap<>();
        userRequestParam.put(UserParameter.NAME.getName(), USER_EXTERNAL_KEY);
        userRequestParam.put(UserParameter.EXTERNAL_KEY.getName(), USER_EXTERNAL_KEY);
        final User user = resource.user().addUser(userRequestParam);
        buyerUser = new BuyerUser();
        buyerUser.setId(user.getId());
        buyerUser.setEmail(getEnvironmentVariables().getUserEmail());
        return resource.user().getUserById(user.getId());
    }

}
