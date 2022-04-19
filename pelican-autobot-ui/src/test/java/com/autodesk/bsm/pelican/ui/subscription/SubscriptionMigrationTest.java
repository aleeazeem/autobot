package com.autodesk.bsm.pelican.ui.subscription;

import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.DEFAULT_ERRORS_MESSAGE;
import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.DEFAULT_ERROR_MESSAGE;
import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.INVALID_OFFER_EXTERNAL_KEY_ERROR_MESSAGE;
import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.INVALID_PRICE_ERROR_MESSAGE;
import static com.autodesk.bsm.pelican.constants.PelicanErrorConstants.MAIN_ERROR_MESSAGE_2;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.json.JProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffering;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.PurchaseOrder;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.FulfillmentCallbackStatus;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.configuration.BankingConfigurationPropertiesPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.FindSubscriptionsPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionDetailPage;
import com.autodesk.bsm.pelican.ui.pages.subscription.SubscriptionMigrationPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PurchaseOrderUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.Iterables;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This test class tests subscription migration from one plan to another plan.
 *
 * @author Muhammad
 */
@Test(groups = { "excludedClass" })
public class SubscriptionMigrationTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private SubscriptionDetailPage subscriptionDetailPage;
    private static PurchaseOrderUtils purchaseOrderUtils;
    private static BankingConfigurationPropertiesPage bankingConfigurationPropertiesPage;
    private static SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private static FindSubscriptionsPage findSubscriptionPage;
    private static SubscriptionMigrationPage subscriptionMigrationPage;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionMigrationTest.class.getSimpleName());
    private static final int PRICE_AMOUNT_1 = 500;
    private static final int PRICE_AMOUNT_2 = 250;
    private static final String INVALID_PRICE_ID = "1234567890";
    private static final String INVALID_OFFER_EXTERNAL_KEY = "AkfjhUIcnlJKKHE";
    private static final String USE_PRICE_ID = "Use Price Id";
    private static final String USE_OFFER_EXTERNAL_KEY = "Use offer external key";
    private static final String USE_FILTERS = "Use Filters";
    private String pelicanEventsNotificationChannel;
    private List<ChangeNotificationMessage> eventsList;
    private CSEHelper cseHelper;
    private ChangeNotificationConsumer pelicanEventsConsumer = null;
    private SubscriptionOffering subscriptionOffering;
    private JProductLine productLine;

    /**
     * Data setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();
        subscriptionOffering = new SubscriptionOffering();
        productLine = new JProductLine();

        subscriptionDetailPage = adminToolPage.getPage(SubscriptionDetailPage.class);
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        subscriptionMigrationPage = adminToolPage.getPage(SubscriptionMigrationPage.class);
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(getEnvironmentVariables());
        purchaseOrderUtils =
            new PurchaseOrderUtils(getEnvironmentVariables(), getEnvironmentVariables().getAppFamilyId());
        findSubscriptionPage = adminToolPage.getPage(FindSubscriptionsPage.class);
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);

        // Set migrate subscription feature flag to true
        bankingConfigurationPropertiesPage = adminToolPage.getPage(BankingConfigurationPropertiesPage.class);
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, true);

        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        final PelicanPlatform resource = new PelicanClient(getEnvironmentVariables()).platform();
        eventsList = new ArrayList<>();
        cseHelper = new CSEHelper(resource, getEnvironmentVariables());
        // Initialize Consumer
        pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        pelicanEventsConsumer = cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);
    }

    /**
     * Driver Close.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanEventsConsumer, pelicanEventsNotificationChannel, eventsList);
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result - ITestResult would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {
        eventsList.clear();
    }

    /**
     * this test method tests three options, which are given below as params, to migrate subscription from one plan to
     * another. Moreover, this method tests subscriptionChangeNotification and changeNotification respectively for
     * subscription when it migrates from one plan to another.
     *
     * @param migrateByPriceId
     * @param migrateByOfferExternalKey
     * @param migrateUsingFilters
     * @param sourceOfferingType
     * @param targetOfferingType
     * @result migration of subscription from one subscription plan to another plan.
     */
    @Test(dataProvider = "testDataForSubscriptionMigration")
    public void testSubscriptionMigrationFromSubscriptionPlanToAnother_DEFECT_BIC_4371(final String migrateByPriceId,
        final String migrateByOfferExternalKey, final String migrateUsingFilters, final OfferingType sourceOfferingType,
        final OfferingType targetOfferingType) {
        // add product line
        final String productLineExternalKey1 = RandomStringUtils.randomAlphabetic(18);
        final String productLineExternalKey2 = RandomStringUtils.randomAlphabetic(18);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);

        // add subscription Plan in order to create a subscription
        final Offerings offering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            sourceOfferingType, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), PRICE_AMOUNT_1);
        LOGGER.info("Offering1 id: " + offering1.getOfferings().get(0).getId());
        final String offerPriceId1 = offering1.getIncluded().getPrices().get(0).getId();
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(offerPriceId1, 1);

        // submit purchase order to create subscription
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        if (sourceOfferingType.equals(OfferingType.META_SUBSCRIPTION)) {
            purchaseOrderUtils.fulfillRequest(purchaseOrder, FulfillmentCallbackStatus.Created);
        }
        findSubscriptionPage.findSubscriptionByPoId(purchaseOrder.getId());
        findSubscriptionPage.clickOnFindSubscriptionsButtonToDetails();
        final String subscriptionId = subscriptionDetailPage.getId();

        final String subscriptionOffer1 = Util.excludeBracketPart(subscriptionDetailPage.getSubscriptionOffer());

        // add second subscription plan to migrate subscription which is created above from first
        // subscription plan
        final Offerings offering2 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            targetOfferingType, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), PRICE_AMOUNT_2);
        final String offerPriceId2 = offering2.getIncluded().getPrices().get(0).getId();
        final String offerExternalKey2 = offering2.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String planName2 = offering2.getOfferings().get(0).getName();
        final String planName1 = offering1.getOfferings().get(0).getName();
        LOGGER.info("Offering2 id: " + offering2.getOfferings().get(0).getId());
        subscriptionMigrationPage = subscriptionDetailPage.clickOnMigrateSubscriptionLink();
        // assertions required at subscription migration page
        AssertCollector.assertThat("Title of the Page is not " + PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE,
            subscriptionMigrationPage.getTitle(), equalTo(PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE),
            assertionErrorList);
        helperToValidateAssertionsMigrateFrom(productLineExternalKey1, planName1, subscriptionOffer1,
            String.valueOf(PRICE_AMOUNT_1));

        // conditions required to select one of the three options to migrate subscription
        if (migrateByPriceId != null) {
            subscriptionMigrationPage.setPriceId(offerPriceId2);
        } else if (migrateByOfferExternalKey != null) {
            subscriptionMigrationPage.setOfferExternalKey(offerExternalKey2);
        } else if (migrateUsingFilters != null) {
            final String productLineInDropDown = productLineExternalKey2 + " (" + productLineExternalKey2 + ")";

            String price2StartDate = null;
            final List<String> pricesStartDateInDb =
                DbUtils.selectQuery("select START_DATE  from subscription_price  " + "where id = " + offerPriceId2,
                    "START_DATE", getEnvironmentVariables());
            if (pricesStartDateInDb.size() > 0) {
                price2StartDate = Iterables.getLast(pricesStartDateInDb);
            }
            String startDateToEnter = null;
            if (price2StartDate != null) {
                startDateToEnter =
                    (DateTimeUtils.changeDateFormat(price2StartDate.substring(0, 10), "yyyy-MM-dd", "MM-dd-yyyy"))
                        .replace("-", "/");
            }
            String price2EndDate = null;
            final List<String> pricesEndDateInDb =
                DbUtils.selectQuery("select END_DATE  from subscription_price  " + "where id = " + offerPriceId2,
                    "END_DATE", getEnvironmentVariables());
            if (pricesEndDateInDb.size() > 0) {
                price2EndDate = Iterables.getLast(pricesEndDateInDb);
            }
            String endDateToEnter = null;
            if (price2EndDate != null) {
                endDateToEnter =
                    (DateTimeUtils.changeDateFormat(price2EndDate.substring(0, 10), "yyyy-MM-dd", "MM-dd-yyyy"))
                        .replace("-", "/");
            }
            final String priceInDropDown = PRICE_AMOUNT_2 + ".00 USD (Price Id:" + offerPriceId2 + ", Dates:"
                + startDateToEnter + "-" + endDateToEnter + ")";
            subscriptionMigrationPage.selectionOfFiltersForMigration(productLineInDropDown, planName2,
                offerExternalKey2, priceInDropDown);
        }

        // clear change notification list since we don't need older events.
        pelicanEventsConsumer.clearNotificationsList();

        subscriptionMigrationPage.clickOnMigrateButton();

        pelicanEventsConsumer.waitForEvents(5000);

        eventsList = pelicanEventsConsumer.getNotifications();
        // Validate the subscription last modified date and the events added to
        // the subscription
        AssertCollector.assertThat("Empty events list", eventsList.size(), greaterThan(0), assertionErrorList);

        // set subscription offering object
        subscriptionOffering.setId(offering2.getOfferings().get(0).getId());
        productLine.setCode(offering2.getOfferings().get(0).getProductLine());
        subscriptionOffering.setJProductLine(productLine);

        boolean isBicSubscription = false;
        if (sourceOfferingType == OfferingType.BIC_SUBSCRIPTION) {
            isBicSubscription = true;
        }

        // verify cse header message for AUM notifications
        if (isBicSubscription) {
            cseHelper.assertionToValidateSubscriptionChangeNotificationHeaderForSherpaMigration(eventsList,
                subscriptionId, PelicanConstants.UPDATED, assertionErrorList);
        }

        // verify header for change notification
        cseHelper.assertionToValidateChangeNotificationHeaderForSubscriptionForSherpaMigration(eventsList,
            subscriptionId, PelicanConstants.UPDATED, assertionErrorList);

        // assertions required at subscription Detail page
        AssertCollector.assertThat("Title of the Page is not " + PelicanConstants.SUBSCRIPTION_DETAIL_TITLE,
            subscriptionMigrationPage.getTitle(), equalTo(PelicanConstants.SUBSCRIPTION_DETAIL_TITLE),
            assertionErrorList);
        AssertCollector.assertThat("Billing charge is not changed to new billing charge for subscription ",
            subscriptionDetailPage.getNextBillingCharge(),
            equalTo(String.valueOf(PRICE_AMOUNT_2) + ".00 USD + taxes and fees"), assertionErrorList);
        AssertCollector.assertThat("Subscription Offer not changed to New Offer for Subscription",
            (Util.excludeBracketPart(subscriptionDetailPage.getSubscriptionOffer())).trim(), equalTo(offerExternalKey2),
            assertionErrorList);
        AssertCollector.assertThat("Subscription Name not changed to New Name for Subscription",
            (Util.excludeBracketPart(subscriptionDetailPage.getSubscriptionPlan())).trim(), equalTo(planName2),
            assertionErrorList);
        AssertCollector.assertThat("Price Under Memo are not Correct ",
            subscriptionDetailPage.getSubscriptionActivity().get(2).getMemo(),
            equalTo("Migration Job Id:\nOld Price Id: " + offerPriceId1 + "\n" + "New Price Id: " + offerPriceId2),
            assertionErrorList);
        cseHelper.assertionToValidateChangeAndSubscriptionChangeNotificationFound(
            cseHelper.helperToFindChangeAndSubscriptionChangeNotificationInCSEEvent(eventsList, subscriptionId,
                getUser(), subscriptionOffering, isBicSubscription, assertionErrorList),
            PelicanConstants.UPDATED, subscriptionId, isBicSubscription, assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method verifies that migrate subscription is not present when Subscription Migration is set to false.
     */
    @Test
    public void testMigrateSubscritptionWithFeatureFlagOff() {
        bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG, false);
        try {
            findSubscriptionPage.getSubscriptionByAdvancedFind(null, null, null, Status.ACTIVE, null, null, null, null,
                null);
            final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);
            final int totalResults = grid.getTotalItems();
            final int randomNumber = grid.selectRowRandomlyFromFirstPage(totalResults);
            grid.selectResultRow(randomNumber);
            AssertCollector.assertThat("Migrate Subscription Link is Present",
                subscriptionDetailPage.isMigrateSubscriptionLinkExists(), equalTo(false), assertionErrorList);
        } catch (final Exception e) {
            LOGGER.info(e.getMessage());
        } finally {
            bankingConfigurationPropertiesPage.setFeatureFlag(PelicanConstants.SUBSCRIPTION_MIGRATION_FEATURE_FLAG,
                true);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * this test method tests the error sceanrios for invalid price id and offer external key. If error is generated on
     * subscription migration page than page stays there and it will not navigate to subscription detail page.There are
     * three scenarios which are given below: i) Invalid Price Id ii) Invalid Offer External Key iii) Using Filters.
     */
    @Test
    public void testAllErrorScenariosForSubscriptionMigration() {
        // add product line
        final String productLineExternalKey1 = RandomStringUtils.randomAlphabetic(18);
        final String productLineExternalKey2 = RandomStringUtils.randomAlphabetic(18);
        LOGGER.info("External Key of Product Line: " + productLineExternalKey1);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey1);
        subscriptionPlanApiUtils.addProductLine(productLineExternalKey2);

        // add Subscription Plan with offer
        final Offerings offering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey1,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), PRICE_AMOUNT_1);
        final String offerPriceId1 = offering1.getIncluded().getPrices().get(0).getId();

        // create subscription
        final LinkedHashMap<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put(offerPriceId1, 1);
        final PurchaseOrder purchaseOrder =
            purchaseOrderUtils.submitAndProcessNewAcquisitionPurchaseOrderWithCC(priceMap, false, null);
        final String subscriptionId =
            purchaseOrder.getLineItems().getLineItems().get(0).getOffering().getOfferingResponse().getSubscriptionId();

        // add second subscription plan to migrate subscription which is created above from first
        // subscription plan
        subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLineExternalKey2,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.ADVANCED, UsageType.COM, BillingFrequency.MONTH,
            1, getPricelistExternalKeyUs(), PRICE_AMOUNT_2);

        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        subscriptionMigrationPage = subscriptionDetailPage.clickOnMigrateSubscriptionLink();
        // assertions required at subscription migration page
        AssertCollector.assertThat("Title of the Page is not " + PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE,
            subscriptionMigrationPage.getTitle(), equalTo(PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE),
            assertionErrorList);

        // testing Error Scenario without selecting any option
        subscriptionMigrationPage.clickOnMigrateButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        AssertCollector.assertThat("Title of the Page is not " + PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE,
            subscriptionMigrationPage.getTitle(), equalTo(PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE),
            assertionErrorList);
        AssertCollector.assertThat("Error Below Title of the Page is not generated",
            subscriptionMigrationPage.getError(), equalTo(MAIN_ERROR_MESSAGE_2), assertionErrorList);

        // testing Error Scenario for Invalid Offer External Key
        subscriptionMigrationPage.setOfferExternalKey(INVALID_OFFER_EXTERNAL_KEY);
        subscriptionMigrationPage.clickOnMigrateButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        subscriptionMigrationPage = adminToolPage.getPage(SubscriptionMigrationPage.class);
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        AssertCollector.assertThat("Title of the Page is not " + PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE,
            subscriptionMigrationPage.getTitle(), equalTo(PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE),
            assertionErrorList);
        AssertCollector.assertThat("Error Below Title of the Page is not generated",
            subscriptionMigrationPage.getError(), equalTo(DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Error Message is not generated for invalid offer external key",
            subscriptionMigrationPage.getErrorMessageForField(),
            equalTo(INVALID_OFFER_EXTERNAL_KEY_ERROR_MESSAGE + " '" + INVALID_OFFER_EXTERNAL_KEY + "'"),
            assertionErrorList);

        // testing Error Scenario for Invalid Price Id
        subscriptionMigrationPage.setPriceId(INVALID_PRICE_ID);
        subscriptionMigrationPage.clickOnMigrateButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        AssertCollector.assertThat("Title of the Page is not " + PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE,
            subscriptionMigrationPage.getTitle(), equalTo(PelicanConstants.SUBSCRIPTION_MIGRATION_TITLE),
            assertionErrorList);
        AssertCollector.assertThat("Error Below Title of the Page is not generated",
            subscriptionMigrationPage.getError(), equalTo(DEFAULT_ERROR_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Error Message is not generated for invalid Invalid Price Id",
            subscriptionMigrationPage.getErrorMessageForField(),
            equalTo(INVALID_PRICE_ERROR_MESSAGE + INVALID_PRICE_ID), assertionErrorList);

        // testing Error Scenario for missing filters
        subscriptionDetailPage = findSubscriptionPage.findBySubscriptionId(subscriptionId);
        subscriptionMigrationPage = subscriptionDetailPage.clickOnMigrateSubscriptionLink();
        final String productLineInDropDown = productLineExternalKey2 + " (" + productLineExternalKey2 + ")";
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        subscriptionMigrationPage.selectProductLine(productLineInDropDown);
        subscriptionMigrationPage.clickOnMigrateButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        AssertCollector.assertThat("Title of the Page is not ", subscriptionMigrationPage.getError(),
            equalTo(DEFAULT_ERRORS_MESSAGE), assertionErrorList);
        AssertCollector.assertThat("Total number of Error messages are not correct",
            subscriptionMigrationPage.getErrorMessageList().size(), equalTo(3), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * data provider to test subscription migration
     *
     * @return option of Migrate To.
     */
    @DataProvider(name = "testDataForSubscriptionMigration")
    public Object[][] testDataForSubscriptionMigration() {
        return new Object[][] {
                { null, null, USE_FILTERS, OfferingType.BIC_SUBSCRIPTION, OfferingType.BIC_SUBSCRIPTION },
                { null, USE_OFFER_EXTERNAL_KEY, null, OfferingType.BIC_SUBSCRIPTION, OfferingType.BIC_SUBSCRIPTION },
                { USE_PRICE_ID, null, null, OfferingType.META_SUBSCRIPTION, OfferingType.BIC_SUBSCRIPTION } };
    }

    /**
     * helper method of assertions to validate fileds which comes under Migrate From.
     */
    private void helperToValidateAssertionsMigrateFrom(final String productLineExternalKey, final String planName,
        final String subscriptionOffer, final String price) {
        AssertCollector.assertThat("Incorrect product line", subscriptionMigrationPage.getProductLine(),
            equalTo(productLineExternalKey), assertionErrorList);
        AssertCollector.assertThat("Incorrect plan", subscriptionMigrationPage.getPlan(), equalTo(planName),
            assertionErrorList);
        AssertCollector.assertThat("Incorrect offer", subscriptionMigrationPage.getOffer(),
            equalTo(subscriptionOffer.trim()), assertionErrorList);
        AssertCollector.assertThat("Incorrect price id",
            (Util.excludeBracketPart(subscriptionMigrationPage.getPrice())).trim(), equalTo(price + ".00 USD"),
            assertionErrorList);
    }
}
