package com.autodesk.bsm.pelican.ui.subscription;

import static org.hamcrest.Matchers.equalTo;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentType;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.SalesChannel;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.ReduceSeatsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.UserSubscriptionResultsPage;
import com.autodesk.bsm.pelican.ui.pages.users.FindUserPage;
import com.autodesk.bsm.pelican.ui.pages.users.UserDetailsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class tests the all subscriptions for required user page. There are two ways to navigate to Subscriptions for
 * user: User ---> User Details Page ---> Find Subscriptions Subscription ---> Subscription Details Page ---> Find
 * Subscriptions for this user
 *
 * @author Muhammad
 *
 */
public class SubscriptionsForUserTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private static PurchaseOrderUtils purchaseOrderUtils;
    private static Offerings bicOffering;
    private static Offerings metaOffering;
    private static LinkedHashMap<String, Integer> priceMap;
    private List<String> expectedColumnHeaderList;
    private static UserSubscriptionResultsPage userSubscriptionResultsPage;
    private static FindSubscriptionsPage findSubscriptionsPage;
    private static SubscriptionDetailPage subscriptionDetailPage;
    private static FindUserPage findUserPage;
    private static UserDetailsPage userDetailsPage;
    private static final String NOTE = "Reducing seats to test user subscriptions report";
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionsForUserTest.class.getSimpleName());
    private static List<String> bicCreditCardSubscriptionDataList;
    private static List<String> metaCreditCardSubscriptionDataList;
    private static List<String> bicPayPalSubscriptionDataList;
    private static final String SUBSCRIPTION_FOR_USER = "Subscriptions For User: ";
    private static String subscriptionsForUserTitle;
    private BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private boolean isReduceSeatsFeatureFlagChanged;
    private BuyerUser buyerUser;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        initializeDriver(getEnvironmentVariables());
        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        userSubscriptionResultsPage = adminToolPage.getPage(UserSubscriptionResultsPage.class);
        findSubscriptionsPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        findUserPage = adminToolPage.getPage(FindUserPage.class);
        userDetailsPage = adminToolPage.getPage(UserDetailsPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        isReduceSeatsFeatureFlagChanged =
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REDUCE_SEATS_FEATURE_FLAG, true);

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        final SubscriptionPlanApiUtils subscriptionPlanApiUtils =
            new SubscriptionPlanApiUtils(getEnvironmentVariables());
        priceMap = new LinkedHashMap<>();

        // Add subscription bic Offers
        bicOffering = subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(),
            OfferingType.BIC_SUBSCRIPTION, BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String bicPriceId = bicOffering.getIncluded().getPrices().get(0).getId();

        // Add subscription meta Offers
        metaOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.META_SUBSCRIPTION,
                BillingFrequency.MONTH, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM);
        final String metaPriceId = metaOffering.getIncluded().getPrices().get(0).getId();

        final UserUtils userUtils = new UserUtils();
        buyerUser =
            userUtils.createBuyerUser(getEnvironmentVariables(), null, getEnvironmentVariables().getAppFamily());
        subscriptionsForUserTitle = SUBSCRIPTION_FOR_USER + buyerUser.getName() + " " + "(" + buyerUser.getId() + ")";

        // create bic subscription with credit card purchase
        bicCreditCardSubscriptionDataList =
            getSubscriptionDataForUser(bicPriceId, 10, PaymentType.CREDIT_CARD, OfferingType.BIC_SUBSCRIPTION, "6");

        // create meta subscription with credit card purchase
        metaCreditCardSubscriptionDataList =
            getSubscriptionDataForUser(metaPriceId, 8, PaymentType.CREDIT_CARD, OfferingType.META_SUBSCRIPTION, "5");

        // create bic subscription with credit card purchase
        bicPayPalSubscriptionDataList =
            getSubscriptionDataForUser(bicPriceId, 6, PaymentType.PAYPAL, OfferingType.BIC_SUBSCRIPTION, "4");

        // Add all column names to a list
        expectedColumnHeaderList = new ArrayList<>();
        expectedColumnHeaderList.add(PelicanConstants.ID_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.EXTERNAL_KEY_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.STORE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.PAYMENT_METHOD);
        expectedColumnHeaderList.add(PelicanConstants.CREDIT_CARD_TYPE);
        expectedColumnHeaderList.add(PelicanConstants.SUBSCRIPTION_OFFER_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.BILLING_PERIOD);
        expectedColumnHeaderList.add(PelicanConstants.STATUS_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.CURRENT_QUANTITY);
        expectedColumnHeaderList.add(PelicanConstants.RENEWAL_QUANTITY);
        expectedColumnHeaderList.add(PelicanConstants.NEXT_BILLING_DATE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.NEXT_BILLING_PRICE);
        expectedColumnHeaderList.add(PelicanConstants.CREATED_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.EXPIRATION_DATE_FIELD);
        expectedColumnHeaderList.add(PelicanConstants.SALES_CHANNEL);
    }

    /**
     * Driver Close
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        // If feature flag is changed in this class, turn it OFF
        if (isReduceSeatsFeatureFlagChanged) {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.REDUCE_SEATS_FEATURE_FLAG, false);
        }
    }

    /**
     * This method tests title of the page and all columns of the report.
     */
    @Test
    public void testUserSubscriptionSearchResultColumnHeadersThroughUserPage() {
        userDetailsPage = findUserPage.getById(buyerUser.getId());
        userSubscriptionResultsPage = userDetailsPage.clickOnFindSubscriptionsLink();
        AssertCollector.assertThat("Title of the page is not correct ", userSubscriptionResultsPage.getTitle(),
            equalTo(subscriptionsForUserTitle), assertionErrorList);
        final List<String> actualColumnHeadersList = userSubscriptionResultsPage.getColumnHeaders();
        if (expectedColumnHeaderList.size() != actualColumnHeadersList.size()) {
            // Failing the test here before checking header names to prevent
            // array out of bounds
            // exception.
            Assert.fail("Number of columns are not correct.");
        }

        for (int i = 0; i < expectedColumnHeaderList.size(); i++) {
            AssertCollector.assertThat("Incorrect Header '" + i + 1 + "'", actualColumnHeadersList.get(i),
                equalTo(expectedColumnHeaderList.get(i)), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method create subscriptions and test the existence of those subscriptions and the values of each column
     * which are populated in report.
     */
    @Test(dataProvider = "getSubscription")
    public void testDataInSubscriptionsForUserReport(final List<String> subscriptionData) {
        int indexOfRequiredRow;
        final String subscriptionId = subscriptionData.get(0);
        userDetailsPage = findUserPage.getById(buyerUser.getId());
        userSubscriptionResultsPage = userDetailsPage.clickOnFindSubscriptionsLink();
        indexOfRequiredRow =
            (userSubscriptionResultsPage.getRowIndexForRequiredValue(PelicanConstants.ID_FIELD, subscriptionId)) - 1;
        LOGGER.info("Index of required subscription id is  " + indexOfRequiredRow);
        if ((userSubscriptionResultsPage.getColumnValuesOfID().get(indexOfRequiredRow)).equals(subscriptionId)) {
            AssertCollector.assertThat("External key is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfExternalKey().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(1)), assertionErrorList);
            AssertCollector.assertThat("Store is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfStore().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(2)), assertionErrorList);
            AssertCollector.assertThat("Payment Method is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfPaymentMethod().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(3)), assertionErrorList);
            AssertCollector.assertThat("Credit Card Type is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfCreditCardType().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(4)), assertionErrorList);
            AssertCollector.assertThat("Subscription offer is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfSubscriptionOffer().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(5)), assertionErrorList);
            AssertCollector.assertThat("Billing Period is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfBillingPeriod().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(6)), assertionErrorList);
            AssertCollector.assertThat("Status is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfStatus().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(7)), assertionErrorList);
            AssertCollector.assertThat("Current quantity is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfCurrentQuantity().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(8)), assertionErrorList);
            AssertCollector.assertThat("Renewal Quantity is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfRenewalQuantity().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(9)), assertionErrorList);
            AssertCollector.assertThat("Next Billing Date is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfNextBillingDate().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(10)), assertionErrorList);

            final String nextBillingPrice = String.format("%,.2f", (Double.valueOf(userSubscriptionResultsPage
                .getColumnValuesOfNextBillingPrice().get(indexOfRequiredRow).split(" ")[0])));

            AssertCollector.assertThat("Next billing price is not correct for subscription: " + subscriptionId,
                nextBillingPrice + " USD + taxes and fees", equalTo(subscriptionData.get(11)), assertionErrorList);
            AssertCollector.assertThat("Created is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfCreated().get(indexOfRequiredRow).substring(0, 10),
                equalTo(subscriptionData.get(12)), assertionErrorList);
            AssertCollector.assertThat("Expiration date is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfExpirationDate().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(13)), assertionErrorList);
            AssertCollector.assertThat("Usage is not correct for subscription: " + subscriptionId,
                userSubscriptionResultsPage.getColumnValuesOfSalesChannel().get(indexOfRequiredRow),
                equalTo(subscriptionData.get(14)), assertionErrorList);
        } else {
            Assert
                .fail("Subscription with id: " + bicCreditCardSubscriptionDataList.get(0) + " is not found in results");
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests the subscriptions for user page can be accessed through subscription detail page.
     */
    @Test
    public void testUserSubscriptionSearchResultPageThroughSubscriptionPage() {
        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(bicCreditCardSubscriptionDataList.get(0));
        AssertCollector.assertThat("Link of Find subscriptions is not visible",
            subscriptionDetailPage.isSubscriptionsForUserLinkVisible(), equalTo(true), assertionErrorList);
        userSubscriptionResultsPage = subscriptionDetailPage.clickOnFindSubscriptionsForUserLink();

        AssertCollector.assertThat("Title of the page is not correct ", userSubscriptionResultsPage.getTitle(),
            equalTo(subscriptionsForUserTitle), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    @DataProvider(name = "getSubscription")
    public Object[][] getTestDataForSubscription() {
        return new Object[][] { { metaCreditCardSubscriptionDataList }, { bicPayPalSubscriptionDataList } };
    }

    /**
     * Method to create bic or meta subscription with required payment method, quantity and reduce seats and returns the
     * required values which need to be tested in report.
     *
     * @param priceId
     * @param quantity
     * @param paymentMethod
     * @param offeringType
     * @param quantityToReduce
     * @return list of strings
     */
    private List<String> getSubscriptionDataForUser(final String priceId, final int quantity,
        final PaymentType paymentMethod, final OfferingType offeringType, final String quantityToReduce) {
        priceMap.put(priceId, quantity);
        String subscriptionId;
        UsageType usageType;
        String billingPeriodCount;
        String creditCardType = "";
        String creditCardNumber = "";
        PurchaseOrder purchaseOrder;
        if (paymentMethod == PaymentType.CREDIT_CARD) {
            purchaseOrder = purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrder(priceMap, false,
                PaymentType.CREDIT_CARD, null, null, buyerUser);
            if (offeringType.equals(OfferingType.BIC_SUBSCRIPTION)) {
                subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                    .getSubscriptionId();
            } else {
                // fulfill the request, since it is Meta
                purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
                purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
                subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                    .getSubscriptionId();
            }
            purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
            final String storedPaymentProfileId =
                purchaseOrder.getPayment().getStoredProfilePayment().getStoredPaymentProfileId();
            final PaymentProfile paymentProfile = resource.paymentProfile().getPaymentProfile(storedPaymentProfileId);
            creditCardType = paymentProfile.getCreditCard().getCreditCardType();
            creditCardNumber = paymentProfile.getCreditCard().getCreditCardNumber();
        } else {
            purchaseOrder =
                purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithPaypal(priceMap, false, buyerUser);
            if (offeringType.equals(OfferingType.BIC_SUBSCRIPTION)) {
                subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                    .getSubscriptionId();
            } else {
                purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
                purchaseOrder = resource.purchaseOrder().getById(purchaseOrder.getId());
                subscriptionId = purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse()
                    .getSubscriptionId();
            }
        }

        if (offeringType.equals(OfferingType.BIC_SUBSCRIPTION)) {
            usageType = bicOffering.getOfferings().get(0).getUsageType();
            billingPeriodCount = (bicOffering.getIncluded().getBillingPlans().get(0).getBillingPeriodCount()) + " "
                + (bicOffering.getIncluded().getBillingPlans().get(0).getBillingPeriod());
        } else {
            usageType = metaOffering.getOfferings().get(0).getUsageType();
            billingPeriodCount = (metaOffering.getIncluded().getBillingPlans().get(0).getBillingPeriodCount()) + " "
                + (metaOffering.getIncluded().getBillingPlans().get(0).getBillingPeriod());
        }

        subscriptionDetailPage = findSubscriptionsPage.findBySubscriptionId(subscriptionId);
        final ReduceSeatsPage reduceSeatsPage = subscriptionDetailPage.clickOnReduceSeatsLink();
        reduceSeatsPage.clickOnReduceSeats(quantityToReduce, NOTE);

        final List<String> subscriptionDataList = new ArrayList<>();
        subscriptionDataList.add(subscriptionId);
        subscriptionDataList.add(subscriptionDetailPage.getExternalKey());
        subscriptionDataList.add(BaseTestData.getStoreExternalKeyUs());
        subscriptionDataList.add(paymentMethod.getValue());
        if (paymentMethod == PaymentType.CREDIT_CARD) {
            subscriptionDataList.add(creditCardType + " " + creditCardNumber);
        } else {
            subscriptionDataList.add("-");
        }
        subscriptionDataList.add(Util.excludeBracketPart(
            subscriptionDetailPage.getSubscriptionOffer() + "(" + usageType.getDisplayName() + ")"));
        subscriptionDataList.add(billingPeriodCount);
        subscriptionDataList.add(subscriptionDetailPage.getStatus());
        subscriptionDataList.add(String.valueOf(subscriptionDetailPage.getQuantity()));
        subscriptionDataList
            .add(String.valueOf(subscriptionDetailPage.getQuantity() - subscriptionDetailPage.getQuantityToReduce()));
        subscriptionDataList.add(subscriptionDetailPage.getNextBillingDate());
        subscriptionDataList.add(subscriptionDetailPage.getNextBillingCharge());
        subscriptionDataList.add(subscriptionDetailPage.getSubscriptionActivity().get(0).getDate().substring(0, 10));
        if (subscriptionDetailPage.getExpirationDate().equals("-")) {
            subscriptionDataList.add("-");
        } else {
            subscriptionDataList.add(subscriptionDetailPage.getExpirationDate());
        }
        subscriptionDataList.add(SalesChannel.BIC_DIRECT.toString());
        return subscriptionDataList;
    }
}
