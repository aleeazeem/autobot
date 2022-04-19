package com.autodesk.bsm.pelican.cse.entitlement;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.autodesk.bsm.pelican.api.clients.CSEClient;
import com.autodesk.bsm.pelican.api.clients.ItemInstancesClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.clients.UserClient;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.item.ItemInstances;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.cse.CSEHelper;
import com.autodesk.bsm.pelican.cse.HelperForAssertions;
import com.autodesk.bsm.pelican.enums.ApplicationFamily;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.BootstrapEntityType;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationChannel;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationEntity;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationFilterType;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationRequester;
import com.autodesk.bsm.pelican.enums.RepublishChangeNotificationType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapChangeNotificationsPage;
import com.autodesk.bsm.pelican.ui.pages.events.BootstrapEventStatusPage;
import com.autodesk.bsm.pelican.ui.pages.events.RepublishEventsPage;
import com.autodesk.bsm.pelican.ui.pages.events.RepublishJobsPage;
import com.autodesk.bsm.pelican.ui.pages.subscriptionplan.AddSubscriptionPlanPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.ChangeNotificationAuthClient;
import com.autodesk.bsm.pelican.util.ChangeNotificationConsumer;
import com.autodesk.bsm.pelican.util.ChangeNotificationMessage;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.UserUtils;
import com.autodesk.bsm.pelican.util.Util;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestClientConfigFactory;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class should contain all tests related to Entitlements Republish tests
 *
 * @author Shweta Hegde
 */
public class RepublishAndBootstrapEntitlementTest extends SeleniumWebdriver {

    private PelicanPlatform resource;
    private RepublishEventsPage eventsPage;
    private BootstrapChangeNotificationsPage bootstrapChangeNotificationsPage;
    private CSEHelper cseHelper;
    private ChangeNotificationConsumer pelicanBootStrapEventsConsumer = null;
    private String pelicanBootStrapNotificationChannel;
    private List<ChangeNotificationMessage> eventsList = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());
        resource = new PelicanClient(getEnvironmentVariables()).platform();

        final AdminToolPage adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

        cseHelper = new CSEHelper(resource);
        final String brokerUrl = getEnvironmentVariables().getBrokerUrl();
        final String notificationKey = getEnvironmentVariables().getNotificationConsKey();
        final String notificationSecret = getEnvironmentVariables().getNotificationConsSecret();
        final String authUrl = getEnvironmentVariables().getAuthUrl();
        RepublishEventsPage.checkAndClearRepublishHungJobs(getEnvironmentVariables());

        // Get access token
        final RestClientConfig restConfig = new RestClientConfigFactory().getClientConfigInstance();
        restConfig.setBaseUri(authUrl);
        final ChangeNotificationAuthClient authClient =
            new ChangeNotificationAuthClient(restConfig, notificationKey, notificationSecret);
        final String accessToken = authClient.getAuthToken();

        final String pelicanEventsNotificationChannel = getEnvironmentVariables().getPelicanEventsNotificationChannel();
        cseHelper.initializeConsumer(brokerUrl, pelicanEventsNotificationChannel, accessToken);

        eventsPage = adminToolPage.getPage(RepublishEventsPage.class);
        bootstrapChangeNotificationsPage = adminToolPage.getPage(BootstrapChangeNotificationsPage.class);

        // Initialize Consumer
        pelicanBootStrapNotificationChannel = getEnvironmentVariables().getPelicanEventsBootstrapNotificationChannel();
        pelicanBootStrapEventsConsumer =
            cseHelper.initializeConsumer(brokerUrl, pelicanBootStrapNotificationChannel, accessToken);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        final Item item = featureApiUtils.addFeature(null, null, null);
        final String itemId1 = item.getId();
        final Item item1 = featureApiUtils.addFeature(null, null, null);
        final String itemId2 = item1.getId();

        final AddSubscriptionPlanPage addSubscriptionPlanPage = adminToolPage.getPage(AddSubscriptionPlanPage.class);

        final String productLineExternalKey = "SQA_Product_Line_" + RandomStringUtils.randomAlphabetic(6);
        final ProductLine productLine = new ProductLine();
        final ProductLineData productLineData = new ProductLineData();
        productLineData.setExternalKey(productLineExternalKey);
        productLineData.setName(productLineExternalKey);
        productLineData.setType("productLine");
        productLine.setData(productLineData);
        subscriptionPlanApiUtils.addProductLine(resource, productLine);

        final String productLineNameAndExternalKey =
            productLine.getData().getName() + " (" + productLine.getData().getName() + ")";

        final String eosDate =
            new DateTime().plusDays(1).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolRenewalDate =
            new DateTime().plusDays(2).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final String eolImmediateDate =
            new DateTime().plusDays(3).toString(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);

        final String name = RandomStringUtils.randomAlphanumeric(8);
        addSubscriptionPlanPage.addSubscriptionPlanInfo(name, name, OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE,
            CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD, UsageType.COM, null, productLineNameAndExternalKey,
            SupportLevel.BASIC, null, true);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId1, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 0);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 0);

        addSubscriptionPlanPage.addOneTimeFeatureEntitlement(itemId2, PelicanConstants.RETAIL_LICENSING_MODEL_NAME,
            new ArrayList<>(ImmutableList.of(PelicanConstants.CORE_PRODUCT_AUTO_1)), 1);
        addSubscriptionPlanPage.addAssignableAndRemoveFeatureDates(false, eosDate, eolRenewalDate, eolImmediateDate, 1);
        addSubscriptionPlanPage.clickOnSave(true);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        cseHelper.terminateConsumer(pelicanBootStrapEventsConsumer, pelicanBootStrapNotificationChannel, eventsList);
    }

    /**
     * Test GDPR Delete Flagged User's Entitlements should not be republished. 1. Verify that the Entitlements is not
     * returned in "Find Matching Entities" 2. Verify that the Entitlements is not included in Processed Record count
     */
    @Test
    public void testGDPRDeleteUsersEntitlementsAreNotRepublished() {

        // Create a user for GDPR delete flag value 1
        final Map<String, String> userParams = new HashMap<>();
        String userExternalKey = RandomStringUtils.randomAlphanumeric(9);
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        final UserUtils userUtils = new UserUtils();
        final User gdprDeleteUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        final BuyerUser gdprBuyerUser = new BuyerUser();
        gdprBuyerUser.setId(gdprDeleteUser.getId());
        gdprBuyerUser.setExternalKey(gdprDeleteUser.getExternalKey());
        gdprBuyerUser.setEmail("gfgsdhfg@xyz.com");

        // Create Trial Offering with entitlement
        final Offerings trialOffering =
            subscriptionPlanApiUtils.addSubscriptionPlan(getPricelistExternalKeyUs(), OfferingType.BIC_SUBSCRIPTION,
                BillingFrequency.SEMIMONTH, Status.ACTIVE, SupportLevel.BASIC, UsageType.TRL);
        final String trialOfferExternalKey = trialOffering.getIncluded().getBillingPlans().get(0).getExternalKey();
        final String trialOfferingId = trialOffering.getOfferings().get(0).getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(trialOfferingId, null, null, true);

        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        String trialSubscriptionId =
            resource.subscription().add(userExternalKey, trialOfferExternalKey, Currency.USD).getId();

        // get item instances
        HashMap<String, String> requestParameters = new HashMap<>();
        requestParameters.put(ItemInstancesClient.Parameter.SUBSCRIPTION_ID.getName(), trialSubscriptionId);
        ItemInstances itemInstances = resource.itemInstances().getItemInstances(requestParameters);
        final String entitlementId1 = itemInstances.getItemInstances().get(0).getId();

        // Update the GDPR delete flag to 1
        DbUtils.updateQuery(String.format(PelicanDbConstants.UPDATE_GDPR_DELETE_FLAG, 1, gdprDeleteUser.getId()),
            getEnvironmentVariables());

        // Add one more user instead of svc_p_pelican
        userExternalKey = RandomStringUtils.randomAlphanumeric(9);
        userParams.clear();
        userParams.put(UserClient.UserParameter.APPLICATION_FAMILY.getName(), ApplicationFamily.AUTO.toString());
        userParams.put(UserClient.UserParameter.EXTERNAL_KEY.getName(), userExternalKey);
        userParams.put(UserClient.UserParameter.NAME.getName(), userExternalKey);

        final User trialUser = userUtils.createPelicanUser(userParams, getEnvironmentVariables());
        trialSubscriptionId =
            resource.subscription().add(trialUser.getExternalKey(), trialOfferExternalKey, Currency.USD).getId();

        // get item instances
        requestParameters = new HashMap<>();
        requestParameters.put(ItemInstancesClient.Parameter.SUBSCRIPTION_ID.getName(), trialSubscriptionId);
        itemInstances = resource.itemInstances().getItemInstances(requestParameters);
        final String entitlementId2 = itemInstances.getItemInstances().get(0).getId();

        final List<String> entitlementIdList = Arrays.asList(entitlementId1, entitlementId2);

        // Republish the POs through Admin Tool
        eventsPage.navigateToEventsPublishPage();
        eventsPage.selectEntity(RepublishChangeNotificationEntity.ENTITLEMENT.getEntityName());
        eventsPage.selectChangeNotificationType(RepublishChangeNotificationType.CREATE_UPDATE.getValue());
        eventsPage.selectFilterType(RepublishChangeNotificationFilterType.ID_LIST.getFilterType());
        eventsPage.selectRequesterAndChannel(RepublishChangeNotificationRequester.PELICAN.toString(),
            RepublishChangeNotificationChannel.PELICAN_EVENTS.getChannel());
        eventsPage.selectIdList(entitlementIdList);
        eventsPage.clickFindMatchingEntities();

        AssertCollector.assertThat("Incorrect total count of entitlements eligible for republish",
            eventsPage.getTotalCount(), is(1), assertionErrorList);
        final RepublishJobsPage republishJobsPage = eventsPage.clickRepublishButton();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        eventsPage.refreshPage();

        final String jobId = republishJobsPage.getId();
        // Verify that only 1 entitlement is republished
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + jobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("1"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + jobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + jobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.ENTITLEMENT_CHANGENOTIFICATION_REPUBLISH_JOB.getJobCategory()), assertionErrorList);

        // Bootstrap
        final BootstrapEventStatusPage bootstrapEventStatusPage =
            bootstrapChangeNotificationsPage.publishBootStrapEntities(BootstrapEntityType.ENTITLEMENT.getDisplayName(),
                PelicanConstants.ID_RANGE, PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, entitlementId1,
                entitlementId2, null, null, RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        bootstrapEventStatusPage.refreshPage();
        final String bootstrapJobId = bootstrapEventStatusPage.getId();
        // Verify that only 1 entitlement is bootstrapped
        AssertCollector.assertThat("Incorrect processed records count for Job Id : " + bootstrapJobId,
            republishJobsPage.getProcessedEventsCount(), equalTo("1"), assertionErrorList);
        AssertCollector.assertThat("Incorrect status for job id : " + bootstrapJobId, republishJobsPage.getStatus(),
            equalTo(Status.COMPLETED.toString()), assertionErrorList);
        AssertCollector.assertThat("Incorrect Category for Job id : " + bootstrapJobId, republishJobsPage.getCategory(),
            equalTo(JobCategory.ENITITLEMENT_CHANGE_NOTIFICATION_BOOTSTRAP_JOB.getJobCategory()), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method which will test the cse bootstrap from the admin tool with date range as filter
     */
    @Test
    public void testBootStrapWithDateRangeFilter() {

        final BootstrapEventStatusPage bootstrapEventStatusPage = bootstrapChangeNotificationsPage
            .publishBootStrapEntities(BootstrapEntityType.ENTITLEMENT.getDisplayName(), PelicanConstants.DATE_RANGE,
                PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, null, null, DateTimeUtils.getToday(),
                DateTimeUtils.getToday(), RepublishChangeNotificationType.CREATE_UPDATE.getValue());

        pelicanBootStrapEventsConsumer.waitForEvents(30000);

        eventsList.clear();
        eventsList = pelicanBootStrapEventsConsumer.getNotifications();

        bootstrapEventStatusPage.refreshPage();

        eventsList = eventsList.stream()
            .filter(message -> PelicanConstants.ENTITLEMENT.equalsIgnoreCase(
                CSEClient.parseCSENotificationResponse(message.getData()).getData().getAttributes().getSubject()))
            .collect(Collectors.toList());
        HelperForAssertions.assertionsForBootstrap(bootstrapEventStatusPage,
            PelicanConstants.ENTITLEMENT_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB, eventsList.size(), assertionErrorList);
        HelperForAssertions.commonAssertionsOnChangeNotifications(eventsList,
            BootstrapEntityType.ENTITLEMENT.getDisplayName(), assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This is a test method to test the bootstrap functionality for entitlements end date notification events
     */
    @Test
    public void testBootstrapEntitlementsEndDateNotification() {

        // Bootstrap
        final BootstrapEventStatusPage bootstrapEventStatusPage =
            bootstrapChangeNotificationsPage.publishBootStrapEntities(BootstrapEntityType.ENTITLEMENT.getDisplayName(),
                PelicanConstants.DATE_RANGE, PelicanConstants.REQUESTER, PelicanConstants.CHANNEL, null, null,
                DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT),
                DateTimeUtils.getTomorrowUTCDatetimeAsString(PelicanConstants.DB_DATE_FORMAT),
                RepublishChangeNotificationType.OFFERINGENTITLEMENTENDDATE.getValue());

        Util.waitInSeconds(TimeConstants.MEDIUM_WAIT);
        bootstrapEventStatusPage.refreshPage();

        HelperForAssertions.assertionsForBootstrapWhenEventsAreUnKnown(bootstrapEventStatusPage,
            PelicanConstants.ENTITLEMENT_CHANGE_NOTIFICATIONS_BOOTSTRAP_JOB, 0, Status.COMPLETED, assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }
}
