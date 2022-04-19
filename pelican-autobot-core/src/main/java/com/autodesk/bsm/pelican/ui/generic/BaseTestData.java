package com.autodesk.bsm.pelican.ui.generic;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.autodesk.bsm.pelican.api.clients.CheckPelicanEmailClient;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.api.pojos.json.Data;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.json.StoreType;
import com.autodesk.bsm.pelican.api.pojos.json.SubscriptionOffer;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.BuyerUser;
import com.autodesk.bsm.pelican.api.pojos.purchaseorder.Payment.PaymentProcessor;
import com.autodesk.bsm.pelican.api.pojos.user.User;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.BillingFrequency;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.entities.PelicanTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PaymentProfileUtils;
import com.autodesk.bsm.pelican.util.PelicanEnvironment;
import com.autodesk.bsm.pelican.util.StoreApiUtils;
import com.autodesk.bsm.pelican.util.SubscriptionPlanApiUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Test class to create common test data.
 *
 * @author jains.
 */
public class BaseTestData {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTestData.class.getSimpleName());
    private static String propertyResourcePath =
        String.format("%s/src/test/resources/auto_env_testData.properties", Util.getTestRootDir());
    private static EnvironmentVariables environmentVariables;
    private static final double vatPercent = 10.0;
    private static JStore storeUs;
    private static JStore storeUk;
    private static String storeIdUs;
    private static String storeExternalKeyUs;
    private static String storeIdUk;
    private static String storeExternalKeyUk;
    private static String pricelistExternalKeyUs;
    private static String pricelistExternalKeyUk;
    private static String storeTypeBic;
    private static String productLineExternalKeyMaya;
    private static String productLineExternalKeyRevit;
    private static String trialCurrencyName;
    private static String cloudCurrencyName;
    private static Offerings bicSubscriptionPlan;
    private static Offerings metaSubscriptionPlan;
    private static ProductLine productLineMaya;
    private static ProductLine productLineRevit;
    private static String basicOfferingUsPerpetualDvdActiveExternalKey;
    private static String basicOfferingUsPerpetualDvdActivePriceId;
    private static User user;
    private static String userExternalKey;
    private static BuyerUser buyerUser;
    private static Offerings basicOfferingUsPerpetualDvdActive;
    private static final String productLineSQL = "Select * from Product_Line where External_Key = '";
    private static final String itemTypeSQL = "Select * from item_type where name = '";
    private static final String offeringSQL = "Select * from Offering where External_Key = '";
    private static final String storeTypeSQL = "select * from store_type where external_key = '";
    private static final String TRIAL_CREDIT_SKU = "TRLCR";
    private static final String CLOUD_CREDIT_SKU = "CLDCR";
    protected static SubscriptionPlanApiUtils subscriptionPlanApiUtils;
    private PelicanPlatform resource;
    private static String bicMonthlyUsPriceId;
    private static String bicMonthlyUkPriceId;
    private static String bicYearlyUsPriceId;
    private static String bicYearlyUkPriceId;
    private static String bic2YearsUsPriceId;
    private static String bic2YearsUkPriceId;
    private static String metaMonthlyUsPriceId;
    private static String metaMonthlyUkPriceId;
    private static String metaYearlyUsPriceId;
    private static String metaYearlyUkPriceId;
    private static String meta2YearsUsPriceId;
    private static String meta2YearsUkPriceId;
    private static String itemTypeId;
    private static String paymentProfileIdForBlueSnapNamer;
    private static String paymentProfileIdForBlueSnapEmea;
    private static String paymentProfileIdForPaypalNamer;
    private static String paymentProfileIdForPaypalEmea;
    protected List<AssertionError> assertionErrorList = new ArrayList();

    public static EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    protected static JStore getStoreUs() {
        return storeUs;
    }

    protected static JStore getStoreUk() {
        return storeUk;
    }

    protected static String getStoreIdUs() {
        return storeIdUs;
    }

    protected static String getStoreExternalKeyUs() {
        return storeExternalKeyUs;
    }

    protected static String getStoreIdUk() {
        return storeIdUk;
    }

    protected static String getStoreExternalKeyUk() {
        return storeExternalKeyUk;
    }

    protected static String getPricelistExternalKeyUs() {
        return pricelistExternalKeyUs;
    }

    protected static String getPricelistExternalKeyUk() {
        return pricelistExternalKeyUk;
    }

    protected static String getStoreTypeNameBic() {
        return storeTypeBic;
    }

    public static String getProductLineExternalKeyMaya() {
        return productLineExternalKeyMaya;
    }

    public static String getProductLineExternalKeyRevit() {
        return productLineExternalKeyRevit;
    }

    protected static String getTrialCurrencyName() {
        return trialCurrencyName;
    }

    public static String getCloudCurrencyName() {
        return cloudCurrencyName;
    }

    public static ProductLine getProductLineMaya() {
        return productLineMaya;
    }

    public static ProductLine getProductLineRevit() {
        return productLineRevit;
    }

    protected static String getBasicOfferingUsPerpetualDvdActiveExternalKey() {
        return basicOfferingUsPerpetualDvdActiveExternalKey;
    }

    public static User getUser() {
        return user;
    }

    protected static String getUserExternalKey() {
        return userExternalKey;
    }

    public static BuyerUser getBuyerUser() {
        return buyerUser;
    }

    protected static Offerings getBasicOfferingUsPerpetualDvdActive() {
        return basicOfferingUsPerpetualDvdActive;
    }

    protected static String getBasicOfferingUsPerpetualDvdActivePriceId() {
        return basicOfferingUsPerpetualDvdActivePriceId;
    }

    protected static Offerings getBicSubscriptionPlan() {
        return bicSubscriptionPlan;
    }

    protected static Offerings getMetaSubscriptionPlan() {
        return metaSubscriptionPlan;
    }

    protected static String getBicMonthlyUsPriceId() {
        return bicMonthlyUsPriceId;
    }

    protected static String getBicMonthlyUkPriceId() {
        return bicMonthlyUkPriceId;
    }

    protected static String getBicYearlyUsPriceId() {
        return bicYearlyUsPriceId;
    }

    protected static String getBicYearlyUkPriceId() {
        return bicYearlyUkPriceId;
    }

    protected static String getBic2YearsUsPriceId() {
        return bic2YearsUsPriceId;
    }

    protected static String getBic2YearsUkPriceId() {
        return bic2YearsUkPriceId;
    }

    protected static String getMetaMonthlyUsPriceId() {
        return metaMonthlyUsPriceId;
    }

    protected static String getMetaMonthlyUkPriceId() {
        return metaMonthlyUkPriceId;
    }

    protected static String getMetaYearlyUsPriceId() {
        return metaYearlyUsPriceId;
    }

    protected static String getMetaYearlyUkPriceId() {
        return metaYearlyUkPriceId;
    }

    protected static String getMeta2YearsUsPriceId() {
        return meta2YearsUsPriceId;
    }

    protected static String getMeta2YearsUkPriceId() {
        return meta2YearsUkPriceId;
    }

    protected static String getItemTypeId() {
        return itemTypeId;
    }

    public static String getPaymentProfileIdForBlueSnapNamer() {
        return paymentProfileIdForBlueSnapNamer;
    }

    public static String getPaymentProfileIdForBlueSnapEmea() {
        return paymentProfileIdForBlueSnapEmea;
    }

    public static String getPaymentProfileIdForPaypalNamer() {
        return paymentProfileIdForPaypalNamer;
    }

    public static String getPaymentProfileIdForPaypalEmea() {
        return paymentProfileIdForPaypalEmea;
    }

    /**
     * This before suite method validate the presence of required data to run regression suite. If any one of required
     * data doesn't exist then that will be created by getting external keys from properties file.
     */
    @BeforeSuite(alwaysRun = true)
    public synchronized void validateExistenceOfRequiredData() {
        environmentVariables = new PelicanEnvironment().initializeEnvironmentVariables();
        final String appFamilyCondition = " and App_Family_Id = " + environmentVariables.getAppFamilyId();
        final String appfIdCondition = " and Appf_Id = " + environmentVariables.getAppFamilyId();
        final String appIdCondition = " and app_id =  " + environmentVariables.getAppId();
        final StoreApiUtils storeApiUtils = new StoreApiUtils(environmentVariables);
        subscriptionPlanApiUtils = new SubscriptionPlanApiUtils(environmentVariables);
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(environmentVariables);
        final PelicanTestData testData = getDataFromPropertiesFiles();
        resource = new PelicanClient(environmentVariables).platform();
        storeExternalKeyUs = testData.getStoreExternalKeyUs();
        pricelistExternalKeyUs = testData.getExternalKeyOfPriceListForUs();
        storeExternalKeyUk = testData.getStoreExternalKeyUk();
        pricelistExternalKeyUk = testData.getExternalKeyOfPriceListForUk();
        storeTypeBic = testData.getStoreTypeBic();
        productLineExternalKeyMaya = testData.getProductLineExternalKeyMaya();
        productLineExternalKeyRevit = testData.getProductLineExternalKeyRevit();
        productLineMaya = new ProductLine();
        productLineRevit = new ProductLine();
        trialCurrencyName = testData.getTrialCurrencyName();
        cloudCurrencyName = testData.getCloudCurrencyName();
        basicOfferingUsPerpetualDvdActiveExternalKey = testData.getBasicOfferingUsPerpetualDvdActiveExternalKey();
        final String bicSubscriptionPlanExternalKey = testData.getBicSubscriptionPlanExternalKey();
        final String bicMonthlyOfferExternalKey = testData.getBicMonthlyOfferExternalKey();
        final String bicYearlyOfferExternalKey = testData.getBicYearlyOfferExternalKey();
        final String bic2YearsOfferExternalKey = testData.getBic2YearsOfferExternalKey();
        final String metaSubscriptionPlanExternalKey = testData.getMetaSubscriptionPlanExternalKey();
        final String metaMonthlyOfferExternalKey = testData.getMetaMonthlyOfferExternalKey();
        final String metaYearlyOfferExternalKey = testData.getMetaYearlyOfferExternalKey();
        final String meta2YearsOfferExternalKey = testData.getMeta2YearsOfferExternalKey();
        final String itemTypeName = testData.getItemTypeExternalKey();

        // check existence of store type. If doesn't exist then create store type
        if (testData.getStoreTypeBic() != null) {
            // storeTypeBic
            final List<Map<String, String>> storeTypeDataMap = DbUtils.selectQuery(
                storeTypeSQL + testData.getStoreTypeBic() + "'" + appFamilyCondition, environmentVariables);
            final int numberOfRecordsInStoreTypeSQL = storeTypeDataMap.size();
            if (numberOfRecordsInStoreTypeSQL == 0) {
                final StoreType storeType = new StoreType();
                final Data storeData = new Data();
                storeData.setExternalKey(testData.getStoreTypeBic());
                storeData.setName(testData.getStoreTypeBic());
                storeData.setType("storeType");
                storeType.setData(storeData);
                storeApiUtils.addNewStoreType(resource, storeType);
            }
        }

        // check existence of US Store (Namer). If doesn't exist then create US Store
        if (testData.getStoreExternalKeyUs() != null) {
            final JStore existingStoreUs = resource.stores().getStoreByExternalKey(storeExternalKeyUs);
            if (existingStoreUs.getErrors() != null) {
                if (existingStoreUs.getErrors().getStatus() == 404) {
                    LOGGER.info("Store US doesn't exist in Pelican so adding a US Store");
                    storeUs = storeApiUtils.addStore(resource, Status.ACTIVE, Country.US, Currency.USD, null, null,
                        false, storeExternalKeyUs, pricelistExternalKeyUs, testData.getStoreTypeBic());
                    AssertCollector.assertThat("Store ID is not created", storeUs.getId(), not(equalTo(null)),
                        assertionErrorList);
                    storeIdUs = storeUs.getId();
                }
            } else {
                storeUs = existingStoreUs;
                storeIdUs = existingStoreUs.getId();
            }
            LOGGER.info("storeIdUs: " + storeIdUs);
        } else {
            Assert.fail("External Key of US Store doesn't exist in properties files");
        }

        // check existence of Uk Store (Emea). If doesn't exist then create UK Store
        if (testData.getStoreExternalKeyUk() != null) {
            final JStore existingStoreUk = resource.stores().getStoreByExternalKey(testData.getStoreExternalKeyUk());
            if (existingStoreUk.getErrors() != null) {
                if (existingStoreUk.getErrors().getStatus() == 404) {
                    LOGGER.info("Store UK doesn't exist in Pelican so adding a UK Store");
                    storeUk = storeApiUtils.addStore(resource, Status.ACTIVE, Country.GB, Currency.GBP, vatPercent,
                        null, true, storeExternalKeyUk, pricelistExternalKeyUk, testData.getStoreTypeBic());
                    AssertCollector.assertThat("Store ID is not created", storeUk.getId(), not(equalTo(null)),
                        assertionErrorList);
                    storeIdUk = storeUk.getId();
                }
            } else {
                storeUk = existingStoreUk;
                storeIdUk = existingStoreUk.getId();
            }
            LOGGER.info("storeIdUk: " + storeIdUk);
        } else {
            Assert.fail("External Key of UK Store doesn't exist in properties files");
        }

        // check if product line MAYA already exists
        List<Map<String, String>> productLineDataMap = DbUtils
            .selectQuery(productLineSQL + productLineExternalKeyMaya + "'" + appFamilyCondition, environmentVariables);
        int numberOfRecordsInProductLineSQL = productLineDataMap.size();

        if (numberOfRecordsInProductLineSQL == 0) {
            subscriptionPlanApiUtils.addProductLine(productLineExternalKeyMaya);
            productLineDataMap = DbUtils.selectQuery(
                productLineSQL + productLineExternalKeyMaya + "'" + appFamilyCondition, environmentVariables);
        }

        // set product line object. This is useful for some other classes.
        ProductLineData productLineData = new ProductLineData();
        productLineData.setExternalKey(productLineExternalKeyMaya);
        productLineData.setId(productLineDataMap.get(0).get("ID"));
        productLineData.setName(productLineDataMap.get(0).get("Name"));
        productLineMaya.setData(productLineData);

        // check if product line REVIT already exists
        productLineDataMap = DbUtils
            .selectQuery(productLineSQL + productLineExternalKeyRevit + "'" + appFamilyCondition, environmentVariables);
        numberOfRecordsInProductLineSQL = productLineDataMap.size();

        if (numberOfRecordsInProductLineSQL == 0) {
            subscriptionPlanApiUtils.addProductLine(productLineExternalKeyRevit);
            productLineDataMap = DbUtils.selectQuery(
                productLineSQL + productLineExternalKeyRevit + "'" + appFamilyCondition, environmentVariables);
        }

        productLineData = new ProductLineData();
        productLineData.setExternalKey(productLineExternalKeyRevit);
        productLineData.setId(productLineDataMap.get(0).get("ID"));
        productLineData.setName(productLineDataMap.get(0).get("Name"));
        productLineRevit.setData(productLineData);

        // check if item type already exists
        final List<Map<String, String>> itemTypeDataMap =
            DbUtils.selectQuery(itemTypeSQL + itemTypeName + "'" + appIdCondition, environmentVariables);
        final int numberOfRecordsInItemTypeSQL = itemTypeDataMap.size();

        if (numberOfRecordsInItemTypeSQL == 0) {
            final ItemType itemType = resource.itemType().addItemType(environmentVariables.getAppId(), itemTypeName);
            itemTypeId = itemType.getId();
        } else {
            itemTypeId = itemTypeDataMap.get(0).get("ID");
        }

        // check if TRIAL currency exists
        final int numberOfRecordsInTrialCurrencySQL = DbUtils
            .selectQuery("Select * from twofish_currency where sku ='" + TRIAL_CREDIT_SKU + "'" + appfIdCondition,
                environmentVariables)
            .size();

        // create TRIAL currency if it does not exist
        if (numberOfRecordsInTrialCurrencySQL == 0) {
            final LinkedHashMap<String, String> requestBody = new LinkedHashMap<>();
            requestBody.put("name", trialCurrencyName);
            requestBody.put("description", trialCurrencyName);
            requestBody.put("sku", TRIAL_CREDIT_SKU);
            requestBody.put("shouldCreateUserAccount", "false");
            basicOfferingApiUtils.addCurrency(resource, requestBody);
        }

        // check if CLOUD currency exists
        final int numberOfRecordsInCloudCurrencySQL = DbUtils
            .selectQuery("Select * from twofish_currency where sku ='" + CLOUD_CREDIT_SKU + "'" + appfIdCondition,
                environmentVariables)
            .size();

        // create TRIAL currency if it does not exist
        if (numberOfRecordsInCloudCurrencySQL == 0) {
            final LinkedHashMap<String, String> requestBody = new LinkedHashMap<>();
            requestBody.put("name", cloudCurrencyName);
            requestBody.put("description", cloudCurrencyName);
            requestBody.put("sku", CLOUD_CREDIT_SKU);
            requestBody.put("shouldCreateUserAccount", "false");
            basicOfferingApiUtils.addCurrency(resource, requestBody);
        }

        // check if basic offering exists
        final List<Map<String, String>> offeringSQLResult =
            DbUtils.selectQuery(offeringSQL + basicOfferingUsPerpetualDvdActiveExternalKey + "'" + appFamilyCondition,
                environmentVariables);
        // create basic perpetual dvd active offering
        if (offeringSQLResult.size() == 0) {
            final int basicOfferingUsPrice = 100;
            basicOfferingUsPerpetualDvdActive = basicOfferingApiUtils.addBasicOffering(pricelistExternalKeyUs,
                OfferingType.PERPETUAL, MediaType.DVD, Status.ACTIVE, basicOfferingUsPrice, UsageType.COM,
                basicOfferingUsPerpetualDvdActiveExternalKey, null);
        } else {
            basicOfferingUsPerpetualDvdActive =
                resource.offerings().getOfferingById(offeringSQLResult.get(0).get("ID"), "prices");
        }
        basicOfferingUsPerpetualDvdActivePriceId =
            basicOfferingUsPerpetualDvdActive.getIncluded().getPrices().get(0).getId();

        // build user and userExternalKey object from environmentVariables
        userExternalKey = environmentVariables.getUserExternalKey();
        user = new User();
        user.setApplicationFamily(getEnvironmentVariables().getAppFamily());
        user.setId(environmentVariables.getUserId());
        user.setExternalKey(environmentVariables.getUserExternalKey());
        user.setName(environmentVariables.getUserName());
        LOGGER.info("User id: " + user.getId());
        LOGGER.info("User external key: " + user.getExternalKey());

        // build buyerUser object from environmentVariables
        buyerUser = new BuyerUser();
        buyerUser.setId(environmentVariables.getUserId());
        buyerUser.setEmail(environmentVariables.getUserEmail());
        buyerUser.setExternalKey(environmentVariables.getUserExternalKey());

        // adding bic subscription plan with monthly, yearly , 2 years offers with emea and namer price list
        bicSubscriptionPlan = addSubscriptionPlan(bicSubscriptionPlanExternalKey, OfferingType.BIC_SUBSCRIPTION,
            bicMonthlyOfferExternalKey, bicYearlyOfferExternalKey, bic2YearsOfferExternalKey);
        LOGGER.info("Bic subscription plan id: " + getBicSubscriptionPlan().getOfferings().get(0).getId());

        bicMonthlyUsPriceId = getBicSubscriptionPlan().getIncluded().getPrices().get(0).getId();
        bicMonthlyUkPriceId = getBicSubscriptionPlan().getIncluded().getPrices().get(1).getId();
        bicYearlyUsPriceId = getBicSubscriptionPlan().getIncluded().getPrices().get(2).getId();
        bicYearlyUkPriceId = getBicSubscriptionPlan().getIncluded().getPrices().get(3).getId();
        bic2YearsUsPriceId = getBicSubscriptionPlan().getIncluded().getPrices().get(4).getId();
        bic2YearsUkPriceId = getBicSubscriptionPlan().getIncluded().getPrices().get(5).getId();

        // adding meta subscription plan with monthly, yearly , 2 years offers with emea and namer price list
        metaSubscriptionPlan = addSubscriptionPlan(metaSubscriptionPlanExternalKey, OfferingType.META_SUBSCRIPTION,
            metaMonthlyOfferExternalKey, metaYearlyOfferExternalKey, meta2YearsOfferExternalKey);
        LOGGER.info("Meta subscription plan id: " + getMetaSubscriptionPlan().getOfferings().get(0).getId());

        metaMonthlyUsPriceId = getMetaSubscriptionPlan().getIncluded().getPrices().get(0).getId();
        metaMonthlyUkPriceId = getMetaSubscriptionPlan().getIncluded().getPrices().get(1).getId();
        metaYearlyUsPriceId = getMetaSubscriptionPlan().getIncluded().getPrices().get(2).getId();
        metaYearlyUkPriceId = getMetaSubscriptionPlan().getIncluded().getPrices().get(3).getId();
        meta2YearsUsPriceId = getMetaSubscriptionPlan().getIncluded().getPrices().get(4).getId();
        meta2YearsUkPriceId = getMetaSubscriptionPlan().getIncluded().getPrices().get(5).getId();

        // Cleanup mails from QA mailbox(except for last 2 days)
        if (environmentVariables.getCleanUpMailboxFlag()) {
            CheckPelicanEmailClient.mailBoxCleanup();
        }

        // Add payment profile
        final PaymentProfileUtils paymentProfileUtils =
            new PaymentProfileUtils(environmentVariables, getEnvironmentVariables().getAppFamilyId());

        // check if payment profile for bluesnap namer processor exists or not. If not then adding it.
        paymentProfileIdForBlueSnapNamer = paymentProfileUtils.createPaymentProfileIfNotExists(user.getId(),
            PaymentProcessor.BLUESNAP_NAMER.getValue());

        // check if payment profile for bluesnap emea processor exists or not. If not then adding it.
        paymentProfileIdForBlueSnapEmea = paymentProfileUtils.createPaymentProfileIfNotExists(user.getId(),
            PaymentProcessor.BLUESNAP_EMEA.getValue());

        // check if payment profile for paypal namer processor exists or not. If not then adding it.
        paymentProfileIdForPaypalNamer =
            paymentProfileUtils.createPaymentProfileIfNotExists(user.getId(), PaymentProcessor.PAYPAL_NAMER.getValue());

        // check if payment profile for paypal emea processor exists or not. If not then adding it.
        paymentProfileIdForPaypalEmea =
            paymentProfileUtils.createPaymentProfileIfNotExists(user.getId(), PaymentProcessor.PAYPAL_EMEA.getValue());
    }

    @AfterSuite(alwaysRun = true)
    public synchronized void afterSuite() {

        DbUtils.updateQuery(
            "update SUBSCRIPTION set STATUS = 1 where APP_FAMILY_ID = " + environmentVariables.getAppFamilyId()
                + " and OWNER_ID = " + user.getId() + " and status <> 1" + " and CREATED < '"
                + DateTimeUtils.getNowMinusDays(PelicanConstants.AUDIT_LOG_DATE_FORMAT.substring(0, 10), 2) + "%';",
            environmentVariables);
        LOGGER.info("All non expired subscriptions of 2 days old and more are EXPIRED");
        LOGGER.info("After Suite is completed");
    }

    /**
     * Method to retrieve a value of required fields form properties file
     *
     * @return testData
     */
    private PelicanTestData getDataFromPropertiesFiles() {
        LOGGER.info("Resource file path: " + propertyResourcePath);
        final Properties properties = Util.loadPropertiesFile(propertyResourcePath);
        final PelicanTestData testData = new PelicanTestData();
        testData.setStoreTypeIpp(properties.getProperty("storeTypeIpp"));
        testData.setStoreTypeBic(properties.getProperty("storeTypeBic"));
        testData.setStoreExternalKeyUs(properties.getProperty("storeExternalKeyUS"));
        testData.setStoreExternalKeyUk(properties.getProperty("storeExternalKeyUK"));
        testData.setExternalKeyOfPriceListForUs(properties.getProperty("externalKeyOfPriceListForUS"));
        testData.setExternalKeyOfPriceListForUk(properties.getProperty("externalKeyOfPriceListForUK"));
        testData.setStoreUsId(properties.getProperty("storeUsId"));
        testData.setStoreUkId(properties.getProperty("storeUkId"));
        testData.setProductLineExternalKeyMaya(properties.getProperty("productLineExternalKeyMaya"));
        testData.setProductLineExternalKeyRevit(properties.getProperty("productLineExternalKeyRevit"));
        testData.setTrialCurrencyName(properties.getProperty("trialCurrencyName"));
        testData.setCloudCurrencyName(properties.getProperty("cloudCurrencyName"));
        testData.setBasicOfferingUsPerpetualDvdActiveExternalKey(
            properties.getProperty("basicOfferingUsPerpetualDvdActiveExternalKey"));
        testData.setBicSubscriptionPlanExternalKey(properties.getProperty("bicSubscriptionPlanExternalKey"));
        testData.setBicMonthlyOfferExternalKey(properties.getProperty("bicMonthlyOfferExternalKey"));
        testData.setBicYearlyOfferExternalKey(properties.getProperty("bicYearlyOfferExternalKey"));
        testData.setBic2YearsOfferExternalKey(properties.getProperty("bic2YearsOfferExternalKey"));
        testData.setMetaSubscriptionPlanExternalKey(properties.getProperty("metaSubscriptionPlanExternalKey"));
        testData.setMetaMonthlyOfferExternalKey(properties.getProperty("metaMonthlyOfferExternalKey"));
        testData.setMetaYearlyOfferExternalKey(properties.getProperty("metaYearlyOfferExternalKey"));
        testData.setMeta2YearsOfferExternalKey(properties.getProperty("meta2YearsOfferExternalKey"));
        testData.setItemTypeExternalKey(properties.getProperty("itemTypeExternalKey"));
        return testData;
    }

    /**
     * Method to add subscription plan
     *
     * @param offeringExternalKey
     * @param offeringType
     * @param monthlyOfferExternalKey
     * @param yearlyOfferExternalKey
     * @return offering
     */
    private Offerings addSubscriptionPlan(final String offeringExternalKey, final OfferingType offeringType,
        final String monthlyOfferExternalKey, final String yearlyOfferExternalKey,
        final String twoYearsOfferExternalKey) {
        final String sqlQueryForOffering = String.format(PelicanDbConstants.SELECT_ID_OFFERING, offeringExternalKey,
            environmentVariables.getAppFamilyId());

        final List<Map<String, String>> offeringSQLResult =
            DbUtils.selectQuery(sqlQueryForOffering, environmentVariables);

        String subscriptionPlanId;

        if (offeringSQLResult.size() == 0) {
            LOGGER.info("Subscription plan doesn't exist in Pelican so adding a bic subscription plan");
            final Offerings subscriptionPlan =
                subscriptionPlanApiUtils.addPlanWithProductLine(productLineExternalKeyMaya, offeringType, Status.ACTIVE,
                    SupportLevel.ADVANCED, UsageType.COM, resource, offeringExternalKey, null);
            subscriptionPlanId = subscriptionPlan.getOffering().getId();
        } else {
            subscriptionPlanId = offeringSQLResult.get(0).get(PelicanConstants.ID_FIELD);
        }

        final String startDate = DateTimeUtils.getCurrentDate(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final String endDate = DateTimeUtils.getNowPlusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1000);

        // Create 1 month offer
        final String monthlyOfferId =
            getOfferId(monthlyOfferExternalKey, subscriptionPlanId, BillingFrequency.MONTH, 1);
        // add namer and emea prices to a monthly offer
        createPrice(100, pricelistExternalKeyUs, startDate, endDate, subscriptionPlanId, monthlyOfferId);
        createPrice(80, pricelistExternalKeyUk, startDate, endDate, subscriptionPlanId, monthlyOfferId);

        // Create 1 year offer
        final String yearlyOfferId = getOfferId(yearlyOfferExternalKey, subscriptionPlanId, BillingFrequency.YEAR, 1);
        // add namer and emea prices to a yearly offer
        createPrice(1000, pricelistExternalKeyUs, startDate, endDate, subscriptionPlanId, yearlyOfferId);
        createPrice(800, pricelistExternalKeyUk, startDate, endDate, subscriptionPlanId, yearlyOfferId);

        // Create 2 years Offer
        final String twoYearlyOfferId =
            getOfferId(twoYearsOfferExternalKey, subscriptionPlanId, BillingFrequency.YEAR, 2);
        // add namer and emea prices to a 2 years offer
        createPrice(1800, pricelistExternalKeyUs, startDate, endDate, subscriptionPlanId, twoYearlyOfferId);
        createPrice(1400, pricelistExternalKeyUk, startDate, endDate, subscriptionPlanId, twoYearlyOfferId);

        return resource.offerings().getOfferingById(subscriptionPlanId, PelicanConstants.INCLUDE_PRICES_OFFERS_PARAMS);
    }

    /**
     * Check the DB whether the offer already exists, if not create it
     *
     * @param offerExternalKey
     * @param subscriptionPlanId
     * @param billingFrequency
     * @param billingFrequencyCount
     * @return offerId
     */
    private String getOfferId(final String offerExternalKey, final String subscriptionPlanId,
        final BillingFrequency billingFrequency, final int billingFrequencyCount) {

        final List<Map<String, String>> offerSQLResult = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_ID_OFFER, offerExternalKey, environmentVariables.getAppFamilyId()),
            environmentVariables);

        String offerId;
        if (offerSQLResult.size() == 0) {
            // Add offer to Subscription plan
            final SubscriptionOffer subscriptionOffer = subscriptionPlanApiUtils.helperToAddSubscriptionOfferToPlan(
                offerExternalKey, billingFrequency, billingFrequencyCount, Status.ACTIVE);
            offerId = subscriptionPlanApiUtils.addSubscriptionOffer(resource, subscriptionOffer, subscriptionPlanId)
                .getData().getId();
        } else {
            offerId = offerSQLResult.get(0).get(PelicanConstants.ID_FIELD);
        }

        return offerId;
    }

    /**
     * Check the DB whether the price already exists, if not create it
     *
     * @param amount
     * @param priceListExternalKey
     * @param startDate
     * @param endDate
     * @param subscriptionPlanId
     * @param offerId
     */
    private void createPrice(final int amount, final String priceListExternalKey, final String startDate,
        final String endDate, final String subscriptionPlanId, final String offerId) {

        int currencyId;
        if (priceListExternalKey.equals(pricelistExternalKeyUs)) {
            currencyId = Currency.USD.getCode();
        } else {
            currencyId = Currency.GBP.getCode();
        }

        final List<Map<String, String>> priceSQLResult = DbUtils
            .selectQuery(String.format(PelicanDbConstants.SELECT_PRICE, offerId, currencyId), environmentVariables);

        if (priceSQLResult.size() == 0) {
            subscriptionPlanApiUtils.addPricesToSubscriptionOffer(resource, subscriptionPlanApiUtils
                .helperToAddPricesToSubscriptionOfferWithDates(amount, priceListExternalKey, startDate, endDate),
                subscriptionPlanId, offerId);
        }
    }

    /**
     * This method runs before every test method of a class which inherits BaseTestData and prints the log of starting a
     * test method.
     *
     * @param method
     */
    @BeforeMethod(alwaysRun = true)
    protected synchronized void printStartTestMethodLog(final Method method) {

        final String message = String.format("====== Start: %s ======", method.getName());
        LOGGER.info("");
        LOGGER.info(StringUtils.repeat("=", message.length()));
        LOGGER.info(message);
        LOGGER.info(StringUtils.repeat("=", message.length()));
        assertionErrorList = new ArrayList();
    }

    /**
     * This method runs after every test method of a class which inherits BaseTestData and prints the log of ending a
     * test method.
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    protected synchronized void printEndTestMethodLog(final ITestResult result) {
        Util.processResultAndOutput(result);
    }

    /**
     * This method prints the log of class start
     */
    @BeforeClass(alwaysRun = true)
    protected synchronized void printStartClassLog() {

        final String message = String.format("======########## Class Start: %s ##########======", getClass().getName());
        LOGGER.info(StringUtils.repeat("=", message.length()));
        LOGGER.info(message);
        LOGGER.info(StringUtils.repeat("=", message.length()));
    }

    /**
     * This method prints the log of class end
     */
    @AfterClass(alwaysRun = true)
    protected synchronized void printEndClassLog() {

        final String message = String.format("======########## Class End: %s ##########======", getClass().getName());
        LOGGER.info(StringUtils.repeat("=", message.length()));
        LOGGER.info(message);
        LOGGER.info(StringUtils.repeat("=", message.length()));
    }
}
