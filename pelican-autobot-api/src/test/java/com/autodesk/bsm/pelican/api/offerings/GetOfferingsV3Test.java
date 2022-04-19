package com.autodesk.bsm.pelican.api.offerings;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.OfferingsV3Client;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.json.BasicOfferingPrice;
import com.autodesk.bsm.pelican.api.pojos.json.BasicOfferingPriceData;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOfferPrice;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.AddDescriptorPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.DescriptorDefinitionDetailPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.FeatureApiUtils;
import com.autodesk.bsm.pelican.util.StoreApiUtils;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Get Offerings API Test
 *
 * @author mandas
 */
public class GetOfferingsV3Test extends SeleniumWebdriver {

    private PelicanPlatform resource;

    private Offerings metaOffering1;
    private Offerings bicOffering1;

    private String featureExternalKey;
    private String productLine;

    private Item item;

    private AdminToolPage adminToolPage;
    private Descriptor descriptor;
    private AddDescriptorPage addDescriptorsPage;

    /**
     * Data SetUp
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        initializeDriver(getEnvironmentVariables());

        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        final DescriptorDefinitionDetailPage detailPage =
            new DescriptorDefinitionDetailPage(adminToolPage, getDriver(), getEnvironmentVariables());
        descriptor = new Descriptor();

        adminToolPage.login();

        resource = new PelicanClient(getEnvironmentVariables()).platform();

        // Add EMEA store without VAT
        final StoreApiUtils storeApiUtils = new StoreApiUtils(getEnvironmentVariables());
        final JStore storeFranceWithoutVat = storeApiUtils.addStore(resource, Status.ACTIVE, Country.FR, Currency.EUR,
            10.0, null, false, null, null, getStoreTypeNameBic());
        final String pricelistExternalKeyOfStoreFrance =
            storeFranceWithoutVat.getIncluded().getPriceLists().get(0).getExternalKey();

        productLine = "SQA_PRODLINE" + RandomStringUtils.randomAlphabetic(10);
        subscriptionPlanApiUtils.addProductLine(productLine, true);

        // 1. create Basic Offerings
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());
        final Offerings basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(),
            OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, UsageType.COM, productLine);

        final String basicOfferingId = basicOffering.getOfferings().get(0).getId();

        // add price to the offering
        basicOfferingApiUtils.addPricesToBasicOffering(resource,
            addPriceToBasicOffering(1000, pricelistExternalKeyOfStoreFrance, 2, 2), basicOfferingId);

        final FeatureApiUtils featureApiUtils = new FeatureApiUtils(getEnvironmentVariables());
        item = featureApiUtils.addFeature(null, null, null);
        featureExternalKey = item.getExternalKey();
        final String featureId = item.getId();

        // 2. create Bic Offerings
        bicOffering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM, BillingFrequency.MONTH, 1,
            getPricelistExternalKeyUs(), 30);
        SubscriptionOfferPrice subscriptionOfferPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(40, getPricelistExternalKeyUk(), 0, 30);
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionOfferPrice,
            bicOffering1.getOfferings().get(0).getId(), bicOffering1.getIncluded().getBillingPlans().get(0).getId())
            .getData().getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(bicOffering1.getOfferings().get(0).getId(),
            featureId, null, true);

        bicOffering1 = resource.offerings().getOfferingById(bicOffering1.getOfferings().get(0).getId(),
            PelicanConstants.INCLUDE_ALL_PARAMS_FOR_OFFERING);

        // 3. create Meta Offerings
        metaOffering1 = subscriptionPlanApiUtils.addSubscriptionPlanWithProductLine(productLine,
            OfferingType.META_SUBSCRIPTION, Status.ACTIVE, SupportLevel.BASIC, UsageType.COM,
            BillingFrequency.SEMIMONTH, 3, getPricelistExternalKeyUs(), 45);
        subscriptionOfferPrice =
            subscriptionPlanApiUtils.helperToAddPricesToSubscriptionOffer(50, getPricelistExternalKeyUk(), 0, 30);
        subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionOfferPrice,
            metaOffering1.getOfferings().get(0).getId(), metaOffering1.getIncluded().getBillingPlans().get(0).getId())
            .getData().getId();
        subscriptionPlanApiUtils.helperToAddEntitlementToSubscriptionPlan(metaOffering1.getOfferings().get(0).getId(),
            featureId, null, true);
        metaOffering1 = resource.offerings().getOfferingById(metaOffering1.getOfferings().get(0).getId(),
            PelicanConstants.INCLUDE_ALL_PARAMS_FOR_OFFERING);

        // 4. create Bic Offerings with offering but no price
        Offerings bicOfferingWithOfferNoPrice = subscriptionPlanApiUtils.addPlanWithProductLine(productLine,
            OfferingType.BIC_SUBSCRIPTION, Status.NEW, SupportLevel.BASIC, UsageType.COM, resource,
            RandomStringUtils.randomAlphanumeric(10), PackagingType.VG);
        final String bicOfferingIdForOfferWithNoPrice = bicOfferingWithOfferNoPrice.getOffering().getId();
        subscriptionPlanApiUtils.addOffer(bicOfferingIdForOfferWithNoPrice);
        bicOfferingWithOfferNoPrice = resource.offerings().getOfferingById(bicOfferingIdForOfferWithNoPrice,
            PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);

        // Add Discriptor to Promotion for Include Descriptor Offering tests
        final String fieldName = "TestDescriptor_" + RandomStringUtils.randomAlphanumeric(5);
        final String apiName = "DescriptorHeader_" + RandomStringUtils.randomAlphanumeric(5);

        final Descriptor descriptors = getDescriptorsData(DescriptorEntityTypes.getEntityType("Promotion"), "ipp",
            fieldName, apiName, "Yes", "15");
        addDescriptorsPage.addDescriptor(descriptors);
        descriptor = detailPage.getDescriptorEntityFromDetails();

    }

    /**
     * Get offerings by valid product line , feature external Key and Include filters
     *
     * @throws Exception
     *
     * @result Appropriate response returned
     */
    @Test
    public void testGetOfferingsWithMultipleFilters() throws Exception {

        final HashMap<String, String> params = new HashMap<>();
        params.put(OfferingsV3Client.Parameter.PRODUCT_LINE.getName(), productLine);
        params.put(OfferingsV3Client.Parameter.FEATURE_EXTERNAL_KEY.getName(), featureExternalKey);
        params.put(OfferingsV3Client.Parameter.INCLUDE.getName(), PelicanConstants.INCLUDE_ENTITLEMENTS);

        final Map<String, Object> map = resource.offeringsV3().getOfferings(params);

        final List<?> dataList = (List<?>) map.get("data");
        final List<?> included = (List<?>) map.get("included");

        final Map bicOfferingDataMap =
            getJsonApiObject(dataList, Long.parseLong(bicOffering1.getOfferings().get(0).getId()));
        final Map metaOfferingDataMap =
            getJsonApiObject(dataList, Long.parseLong(metaOffering1.getOfferings().get(0).getId()));
        final Map bicOfferingIncludedMap =
            getJsonApiObject(included, Long.parseLong(bicOffering1.getIncluded().getBillingPlans().get(0).getId()));
        final Map metaOfferingIncludedMap =
            getJsonApiObject(included, Long.parseLong(metaOffering1.getIncluded().getBillingPlans().get(0).getId()));
        final Map bicOfferingItemMap = getJsonApiObject(included,
            Long.parseLong(bicOffering1.getOfferings().get(0).getOneTimeEntitlements().get(0).getEntitlementDbId()));
        final Map metaOfferingItemMap = getJsonApiObject(included,
            Long.parseLong(metaOffering1.getOfferings().get(0).getOneTimeEntitlements().get(0).getEntitlementDbId()));

        assertSubscriptionPlan(bicOfferingDataMap, bicOffering1);
        assertSubscriptionPlan(metaOfferingDataMap, metaOffering1);

        assertOffers(bicOfferingIncludedMap, bicOffering1);
        assertOffers(metaOfferingIncludedMap, metaOffering1);

        assertOneTimeEntitlements(bicOfferingItemMap, bicOffering1);
        assertOneTimeEntitlements(metaOfferingItemMap, metaOffering1);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Assertion method for Offer
     *
     * @param jsonApiObject
     * @param bicOffering
     */
    private void assertOffers(final Map jsonApiObject, final Offerings bicOffering) {

        AssertCollector.assertThat(
            "Subscription Offer with id " + bicOffering.getIncluded().getBillingPlans().get(0).getId() + " null.",
            jsonApiObject, is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("Offer Type didnt match", jsonApiObject.get("type").toString(), equalTo("offer"),
            assertionErrorList);
        AssertCollector.assertThat("Offer Id didnt match", jsonApiObject.get("id").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getId()), assertionErrorList);

        final Map attributes = (Map) jsonApiObject.get("attributes");

        AssertCollector.assertThat("offer externalKey didnt match", attributes.get("externalKey").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("offer name didnt match", attributes.get("name").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getName()), assertionErrorList);
        AssertCollector.assertThat("offer billingCycleCount didnt match",
            attributes.get("billingCycleCount") != null ? attributes.get("billingCycleCount").toString() : null,
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getBillingCycleCount()), assertionErrorList);
        AssertCollector.assertThat("offer billingPeriodCount didnt match",
            attributes.get("billingPeriodCount").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getBillingPeriodCount()), assertionErrorList);
        AssertCollector.assertThat("offer billingPeriod didnt match", attributes.get("billingPeriod").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getBillingPeriod()), assertionErrorList);
        AssertCollector.assertThat("offer status didnt match", attributes.get("status").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getStatus()), assertionErrorList);

        final Map billingDate = (Map) attributes.get("billingDate");
        AssertCollector.assertThat("offer - billingDate month didnt match", billingDate.get("month").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getBillingDate().getMonth()),
            assertionErrorList);
        AssertCollector.assertThat("offer - billingDate day didnt match", billingDate.get("day").toString(),
            equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getBillingDate().getDay()), assertionErrorList);
    }

    /**
     * Assertion method for One Time Entitlements
     *
     * @param jsonApiObject
     * @param bicOffering
     */
    private void assertOneTimeEntitlements(final Map jsonApiObject, final Offerings bicOffering) {
        AssertCollector.assertThat(
            "SubscriptionEntitlement with id " + bicOffering.getIncluded().getBillingPlans().get(0).getId() + " null.",
            jsonApiObject, is(notNullValue()), assertionErrorList);
        AssertCollector.assertThat("SubscriptionEntitlement Type didnt match", jsonApiObject.get("type").toString(),
            equalTo("oneTimeEntitlement"), assertionErrorList);
        AssertCollector.assertThat("SubscriptionEntitlement DB ID didnt match", jsonApiObject.get("id").toString(),
            equalTo(bicOffering.getOfferings().get(0).getOneTimeEntitlements().get(0).getEntitlementDbId()),
            assertionErrorList);

        final Map attributes = (Map) jsonApiObject.get("attributes");

        AssertCollector.assertThat("One Time Entitlement Type didnt match", attributes.get("type").toString(),
            equalTo("ITEM"), assertionErrorList);
        AssertCollector.assertThat("One Time Entitlement Type Id didnt match", attributes.get("itemId").toString(),
            equalTo(item.getId()), assertionErrorList);
        AssertCollector.assertThat("One Time Entitlement Name didnt match", attributes.get("name").toString(),
            equalTo(item.getName()), assertionErrorList);
        AssertCollector.assertThat("One Time Entitlement External Key didnt match",
            attributes.get("externalKey").toString(), equalTo(item.getExternalKey()), assertionErrorList);
    }

    /**
     * Assertion method for Subscription Offering
     *
     * @param jsonApiObject1
     * @param bicOffering
     * @throws JSONException
     */
    private void assertSubscriptionPlan(final Map jsonApiObject, final Offerings bicOffering) throws JSONException {
        AssertCollector.assertThat("Subscription Plan type didnt match", jsonApiObject.get("type").toString(),
            equalTo("offering"), assertionErrorList);
        AssertCollector.assertThat("Subscription Plan Id didnt match", jsonApiObject.get("id").toString(),
            equalTo(String.valueOf(bicOffering.getOfferings().get(0).getId())), assertionErrorList);

        final Map attributes = (Map) jsonApiObject.get("attributes");
        AssertCollector.assertThat("Offering attributes-Type didnt match", attributes.get("offeringType").toString(),
            equalTo(bicOffering.getOfferings().get(0).getOfferingType().toString()), assertionErrorList);
        AssertCollector.assertThat("Offering attributes-externalKey didnt match",
            attributes.get("externalKey").toString(), equalTo(bicOffering.getOfferings().get(0).getExternalKey()),
            assertionErrorList);
        AssertCollector.assertThat("Offering attributes-productLine didnt match",
            attributes.get("productLine").toString(), equalTo(bicOffering.getOfferings().get(0).getProductLine()),
            assertionErrorList);
        AssertCollector.assertThat("Offering attributes-productLineName didnt match",
            attributes.get("productLineName").toString(),
            equalTo(bicOffering.getOfferings().get(0).getProductLineName()), assertionErrorList);
        AssertCollector.assertThat("Offering attributes-name didnt match", attributes.get("name").toString(),
            equalTo(bicOffering.getOfferings().get(0).getName()), assertionErrorList);
        AssertCollector.assertThat("Offering attributes-supportLevel didnt match",
            attributes.get("supportLevel").toString(),
            equalTo((bicOffering.getOfferings().get(0).getSupportLevel() != null
                ? bicOffering.getOfferings().get(0).getSupportLevel().toString()
                : null)),
            assertionErrorList);
        assertOfferingAttribute(attributes, bicOffering);

        final Map entitlementPeriodInAttributes = (Map) attributes.get("entitlementPeriod");
        AssertCollector.assertThat("EntitlementPeriod count didnt match",
            Integer.parseInt(entitlementPeriodInAttributes.get("count").toString()),
            equalTo(bicOffering.getOfferings().get(0).getEntitlementPeriod().getCount()), assertionErrorList);
        AssertCollector.assertThat("EntitlementPeriod Type didnt match",
            entitlementPeriodInAttributes.get("type").toString(),
            equalTo(bicOffering.getOfferings().get(0).getEntitlementPeriod().getType()), assertionErrorList);

        assertRelationshipForPlan(jsonApiObject, bicOffering);
    }

    /**
     * Method assert Attributes in Offerings
     *
     * @param attributes
     * @param bicOffering
     */
    private void assertOfferingAttribute(final Map attributes, final Offerings bicOffering) {

        AssertCollector.assertThat("Offering attributes-status didnt match", attributes.get("status").toString(),
            equalTo(bicOffering.getOfferings().get(0).getStatus()), assertionErrorList);

        final String expectedPackagingType =
            attributes.get("packagingType") != null ? attributes.get("packagingType").toString() : null;
        final String actualPackagingType = bicOffering.getOfferings().get(0).getPackagingType() != null
            ? bicOffering.getOfferings().get(0).getPackagingType().toString()
            : null;
        AssertCollector.assertThat("Offering attributes-packagingType didnt match", expectedPackagingType,
            equalTo(actualPackagingType), assertionErrorList);

        AssertCollector.assertThat("Offering attributes-usageType didnt match", attributes.get("usageType").toString(),
            equalTo(bicOffering.getOfferings().get(0).getUsageType().getUploadName()), assertionErrorList);
        AssertCollector.assertThat("Offering attributes-cancellationPolicy didnt match",
            attributes.get("cancellationPolicy").toString(),
            equalTo(bicOffering.getOfferings().get(0).getCancellationPolicy().toString()), assertionErrorList);
        AssertCollector.assertThat("Offering attributes-module didnt match", attributes.get("module").toString(),
            equalTo(String.valueOf(bicOffering.getOfferings().get(0).isModuled())), assertionErrorList);

        final String expectedShortDescription =
            attributes.get("shortDescription") != null ? attributes.get("shortDescription").toString() : null;
        AssertCollector.assertThat("Offering attributes-shortDescription didnt match", expectedShortDescription,
            equalTo(bicOffering.getOfferings().get(0).getShortDescription()), assertionErrorList);
    }

    /**
     * Method assert Relationship in Offerings
     *
     * @param jsonApiObject
     * @param bicOffering
     * @throws JSONException
     */
    private void assertRelationshipForPlan(final Map jsonApiObject, final Offerings bicOffering) throws JSONException {

        final Map relationships = (Map) jsonApiObject.get("relationships");
        final Map oneTimeEntitlements = (Map) relationships.get("oneTimeEntitlements");
        final List entitlementData = (List) oneTimeEntitlements.get("data");

        final Map offers = (Map) relationships.get("offers");
        final List relationshipsData = (List) offers.get("data");

        final JSONObject entitlementDataJson = new JSONObject(entitlementData.get(0).toString());
        final JSONObject relationshipsDataJson = new JSONObject(relationshipsData.get(0).toString());

        AssertCollector.assertThat("OneTimeEntitlement relationship data - Type didnt match",
            entitlementDataJson.getString("type"), equalTo("oneTimeEntitlement"), assertionErrorList);
        AssertCollector.assertThat("Offer relationship data - Type didnt match",
            relationshipsDataJson.getString("type"), equalTo("offer"), assertionErrorList);
        AssertCollector.assertThat("Offer relationship data - Offer ID in Offering Object didnt match",
            relationshipsDataJson.getString("id"), equalTo(bicOffering.getIncluded().getBillingPlans().get(0).getId()),
            assertionErrorList);
    }

    /**
     * Method to delete the descriptors.
     */
    private void deleteDescriptors(final String fieldName, final String groupName, final String entity) {
        // Navigate to Find Descriptors page.
        addDescriptorsPage = adminToolPage.getPage(AddDescriptorPage.class);
        addDescriptorsPage.navigateToFindDescriptors();
        // Delete the descriptors.
        addDescriptorsPage.deleteDescriptors(fieldName, groupName, entity);
    }

    /**
     * This method reads the values of all attributes in a descriptor details Page and creates a descriptor entity
     */
    private Descriptor getDescriptorsData(final DescriptorEntityTypes entityType, final String groupName,
        final String fieldName, final String apiName, final String localized, final String maxLength) {
        final Descriptor descriptors = new Descriptor();
        descriptors.setAppFamily("Automated Tests (AUTO)");
        descriptors.setEntity(entityType);
        descriptors.setGroupName(groupName);
        descriptors.setFieldName(fieldName);
        descriptors.setApiName(apiName);
        descriptors.setLocalized(localized);
        descriptors.setMaxLength(maxLength);
        return descriptors;
    }

    /**
     * This method adds price to basic offering
     */
    private BasicOfferingPrice addPriceToBasicOffering(final int amount, final String priceListExternalKey,
        final int startDateNeeded, final int endDateNeeded) {

        // Add basic offering price
        final BasicOfferingPrice basicOfferingPrice = new BasicOfferingPrice();
        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, startDateNeeded);
        final Date startDate = calendar.getTime();
        calendar.add(Calendar.YEAR, endDateNeeded);
        final Date endDate = calendar.getTime();
        final String priceStartDate = dateFormat.format(startDate);
        final String priceEndDate = dateFormat.format(endDate);
        final BasicOfferingPriceData basicOfferingPriceData = new BasicOfferingPriceData();
        basicOfferingPriceData.setType("price");
        basicOfferingPriceData.setAmount(amount);
        basicOfferingPriceData.setStartDate(priceStartDate);
        basicOfferingPriceData.setEndDate(priceEndDate);
        basicOfferingPriceData.setPriceList(priceListExternalKey);
        basicOfferingPrice.setData(basicOfferingPriceData);

        return basicOfferingPrice;
    }

    public Map getJsonApiObject(final List list, final long id) {
        for (final Object object : list) {
            if (object instanceof Map) {
                final Map map = (Map) object;

                if (String.valueOf(id).equals(map.get("id"))) {
                    return map;
                }
            }
        }
        return null;
    }

    @AfterClass
    public void tearDown() {

        deleteDescriptors(descriptor.getFieldName(), descriptor.getGroupName(), descriptor.getEntity().getEntity());
    }
}
