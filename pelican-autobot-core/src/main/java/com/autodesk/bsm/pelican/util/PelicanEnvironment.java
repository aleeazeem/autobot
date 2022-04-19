package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A representation of the -DenvironmentType argument which specifies the hosts we are using.
 *
 * @author jains
 */
public class PelicanEnvironment {

    private static String environment = null;
    private static Properties properties = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(PelicanEnvironment.class);
    private static EnvironmentVariables envVariable;

    // Constructor to initialize environment variable
    public PelicanEnvironment() {
        // This condition checks whether environmentType variable has value.
        // else get from environment.properties

        if (environment == null) {
            environment = getValueFromProperty(PelicanConstants.ENVIRONMENT);
        }

        // If no value from environment.properties default it to dev2
        if (environment == null) {
            environment = PelicanConstants.DEFAULT_ENVIRONMENT;
        }

        environment = environment.toLowerCase();
        LOGGER.info("Test environment is " + environment);
    }

    /**
     * Method to read environment property file and initialize PelicanEnvironment variables. Environment property file
     * is located within the test package.
     *
     * @return EnvironmentVariables
     */
    public EnvironmentVariables initializeEnvironmentVariables() {

        if (envVariable == null) {
            LOGGER.info("envVariable is null.");
            final String propertyFilePath = Util.getTestRootDir()
                + String.format("/src/test/resources/environments/auto_env_%s.properties", environment);

            properties = getProperties(propertyFilePath);
            envVariable = new EnvironmentVariables();

            envVariable.setBrowser(getValueFromProperty("browser"));
            envVariable.setAdminUrl(getValueFromProperty("pelicanAtURL") + "/admin");
            envVariable.setApplicationFamily(getValueFromProperty("appFamily"));
            envVariable.setApplicationDescription(getValueFromProperty("app_Description_AT"));
            envVariable.setApplicationFamilyDescription(getValueFromProperty("appFamilyDescription_AT"));
            envVariable.setCombinedApplicationFamily(String.format("%s (%s)",
                envVariable.getApplicationFamilyDescription(), envVariable.getApplicationFamily()));
            envVariable.setUserName(getValueFromProperty("userName"));
            envVariable.setPassword(getValueFromProperty("password"));
            envVariable.setTimezoneOffset(getValueFromProperty("timezone_offset"));
            envVariable.setTableSuffix(getValueFromProperty("tableSuffix"));

            envVariable.setV2ApiUrl(getValueFromProperty("pelicanApiURL") + "/tfel2rs/v2");
            envVariable.setV3ApiUrl(getValueFromProperty("pelicanApiURL") + "/tfel2rs/v3");
            envVariable.setGdprUrl(getValueFromProperty("pelicanGdprURL") + "/tfel2rs/v2");
            envVariable.setPartnerId(getValueFromProperty("partnerId"));
            envVariable.setAppFamilyId(getValueFromProperty("appFamilyId"));
            envVariable.setSecretCredential(getValueFromProperty("secretCredential"));
            envVariable.setAppId(getValueFromProperty("appId"));

            envVariable.setOtherAppFamilyId(getValueFromProperty("otherAppFamilyId"));

            envVariable.setTriggerUrl(getValueFromProperty("pelicanTriggerURL") + "/triggers-workers/api");
            envVariable.setEnvironmentType(environment);
            envVariable
                .setHealthCheckUrl(getValueFromProperty("pelicanTriggerURL") + "/triggers-workers/api/healthChecks");

            // Setting Payment GatewayId and feeCollectorId
            envVariable.setFeeCollectorId(getValueFromProperty("feeCollectorId"));
            envVariable.setBluesnapNamerPaymentGatewayId(getValueFromProperty("bluesnapNamerPaymentGatewayId"));
            envVariable.setBluesnapEmeaPaymentGatewayId(getValueFromProperty("bluesnapEmeaPaymentGatewayId"));
            envVariable.setPaypalNamerPaymentGatewayId(getValueFromProperty("paypalNamerPaymentGatewayId"));
            envVariable.setPaypalEmeaPaymentGatewayId(getValueFromProperty("paypalEmeaPaymentGatewayId"));

            // Setting Database url and Database username and password
            envVariable.setJDBCDriver(getValueFromProperty("JDBCDriver"));
            envVariable.setDbUrl(getValueFromProperty("dbUrl"));
            envVariable.setDbUsername(getValueFromProperty("dbUsername"));
            envVariable.setDbPassword(getValueFromProperty("dbPassword"));
            envVariable.setFinanceReportHeaders(getValueFromProperty("financeReportHeaders"));

            // Setting triggers database url, username and password
            envVariable.setTriggersDBUrl(getValueFromProperty("triggersDBUrl"));
            envVariable.setTriggersDBUsername(getValueFromProperty("triggersDBUsername"));
            envVariable.setTriggersDBPassword(getValueFromProperty("triggersDBPassword"));

            // Setting the Chrome browser default download path for mac and windows
            envVariable.setDownloadPathForMac(getValueFromProperty("downloadPathForMac"));
            envVariable.setDownloadPathForWindows(getValueFromProperty("downloadPathForWindows"));

            envVariable.setInvoiceAdminUsername(getValueFromProperty("invoiceAdminUsername"));
            envVariable.setInvoiceAdminPassword(getValueFromProperty("invoiceAdminPassword"));

            // Set Email Properties
            envVariable.setImaphost(getValueFromProperty("imapHost"));
            envVariable.setEmailUsername(getValueFromProperty("emailUsername"));
            envVariable.setEmailPassword(getValueFromProperty("emailPassword"));
            envVariable.setEmailStoreType(getValueFromProperty("mailStoreType"));
            envVariable.setUserEmail(getValueFromProperty("userEmail"));
            envVariable.setUserExternalKey(getValueFromProperty("userExternalKey"));

            // Set CSE properties
            envVariable.setBrokerUrl(getValueFromProperty("brokerUrl"));
            envVariable.setAuthUrl(getValueFromProperty("authUrl"));
            envVariable.setPelicanEventsNotificationChannel(getValueFromProperty("pelicanEventsNotificationChannel"));
            envVariable.setPelicanEventsBootstrapNotificationChannel(
                getValueFromProperty("pelicanEventsBootstrapNotificationChannel"));
            envVariable.setPersonMasterNotificationChannel(
                getValueFromProperty("personMasterExportControlNotificationChannel"));
            envVariable.setNotificationConsKey(getValueFromProperty("notificationConsKey"));
            envVariable.setNotificationConsSecret(getValueFromProperty("notificationConsSecret"));

            // DynamodDB
            envVariable.setDynamoDBTable(getValueFromProperty("dynamodbTable"));

            // Shopping Cart
            envVariable.setCartUrl(getValueFromProperty("cartUrl"));
            envVariable.setCartUserName(getValueFromProperty("cartUserName"));
            envVariable.setCartPassword(getValueFromProperty("cartPassword"));
            envVariable.setCartPaypalEmailAddress(getValueFromProperty("cartPaypalEmail"));
            envVariable.setCartPaypalPassword(getValueFromProperty("cartPaypalPassword"));
            envVariable.setOtherAppFamily(getValueFromProperty("otherAppFamily"));
            envVariable.setCartUserId(getValueFromProperty("cartUserId"));
            envVariable.setCartUserExternalKey(getValueFromProperty("CartUserExternalKey"));

            // App Family for role creation/edit/delete
            envVariable.setRoleAppFamilyID(getValueFromProperty("twoFishAppFamily"));

            // DR migration
            envVariable.setSkuCodeForMonthlyOffer(getValueFromProperty("skuCodeForMonthlyOffer"));
            envVariable.setOwnerId(getValueFromProperty("ownerId"));
            envVariable.setPlanId(getValueFromProperty("planId"));
            envVariable.setOfferId(getValueFromProperty("offerId"));
            envVariable.setPriceId(getValueFromProperty("priceId"));
            envVariable.setOfferingDetailId(getValueFromProperty("offeringDetailId"));

            // userid
            envVariable.setUserId(getValueFromProperty("userId"));
            envVariable.setCloudCurrencyId(getValueFromProperty("cloudCurrencyId"));

            // user id in twofish family
            envVariable.setUserIdInTwoFishFamily(getValueFromProperty("userIdInTwoFishFamily"));

            // construct the trigger URL along with credentials as part of the URL
            envVariable.setRepublishTriggerUrl(
                "http://" + getValueFromProperty("triggerUser") + ":" + getValueFromProperty("triggerPassword") + "@"
                    + getValueFromProperty("pelicanTriggerURL").split("//")[1]);

            // set setTakeScreenshot value
            envVariable.setScreenshotFlag(getValueFromProperty("isScreenShotRequired"));
            // ConvergentCharging Wsdl URL
            envVariable.setConvergentChargingWsdl(getValueFromProperty("convergentChargingWsdl"));
            // set email inbox clean up flag
            envVariable.setCleanUpMailboxFlag(getValueFromProperty("isCleanUpMailbox"));
            // finance report date range
            envVariable.setFinanceReportDateRange(getValueFromProperty("financeReportDateRange"));
            // userContractNumberForConvergentCharging for cloud credit related test
            envVariable.setUserContractNumberForConvergentCharging(
                getValueFromProperty("userContractNumberForConvergentCharging"));
            // set AWS Region
            envVariable.setAWSRegion(getValueFromProperty("awsRegion"));

            // Set AUM related users
            envVariable.setPrimaryAdminExternalKey(getValueFromProperty("primaryAdminExternalKey"));
            envVariable.setSecondaryAdminExternalKey(getValueFromProperty("secondaryAdminExternalKey"));
            envVariable.setNamedPartyUserExternalKey(getValueFromProperty("namedPartyUserExternalKey"));
            envVariable.setAppFamily(getValueFromProperty("appFamily"));
            envVariable.setBicMonthlyPriceId(getValueFromProperty("bicMonthlyPriceId"));
            envVariable.setTaxCode(getValueFromProperty("taxCode"));
            envVariable.setSubscriptionServiceUrl(getValueFromProperty("pelicanApiURL") + "/subscriptions/");
        }
        return envVariable;
    }

    private static Properties getProperties(final String sourceFile) {

        final Properties properties = new Properties();
        FileInputStream inputStream = null;

        try {
            LOGGER.info("Reading environment properties from : " + sourceFile);
            inputStream = new FileInputStream(sourceFile);
            properties.load(inputStream);
        } catch (final IOException e) {
            LOGGER.info("Error while loading properties from '" + sourceFile + "': " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    // failTest("Error in finally block while loading properties
                    // from '"
                    // + filename + "': " + e.getMessage());
                    LOGGER.info(
                        "Error in finally block while loading properties from '" + sourceFile + "': " + e.getMessage());
                }
            }
        }
        return properties;
    }

    public static String getValueFromProperty(final String key) {

        String value;
        if (System.getProperty(key) != null) {
            value = System.getProperty(key);
        } else {
            value = properties.getProperty(key);
        }

        LOGGER.debug("Returning value of " + key + " as " + value);
        return value;
    }

    public static String getEnvironment() {
        return environment;
    }
}
