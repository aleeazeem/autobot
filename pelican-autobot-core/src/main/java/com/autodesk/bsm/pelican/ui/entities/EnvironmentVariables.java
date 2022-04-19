package com.autodesk.bsm.pelican.ui.entities;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.util.Util;

public class EnvironmentVariables extends BaseEntity {

    // browser
    private String browser;

    // admin tool
    private String applFamilyDesc;
    private String applDesc;
    private String combinedApplFamily;
    private String userName;
    private String password;
    private String adminUrl;
    private String timezoneOffSet;
    private String envType;
    private String appFamily;

    // api
    private String apiV2Url;
    private String apiV3Url;
    private String gdprUrl;
    private String partnerId;
    private String appFamilyId;
    private String secretCredential;
    private String appId;

    // api creds for other applicationFamily
    private String otherAppFamilyId;

    // trigger
    private String triggerUrl;

    // health check
    private String healthCheckUrl;

    // financeGateway
    private String bluesnapNamerPaymentGatewayId;
    private String bluesnapEmeaPaymentGatewayId;
    private String paypalNamerPaymentGatewayId;
    private String paypalEmeaPaymentGatewayId;

    // finance report
    private String financeReportDateRange;

    // Government Entities
    private String feeCollectorId;

    // Cloud Currency id
    private String cloudCurrencyId;

    // Database Connection Driver
    private String JDBCDriver;

    // Database platform connection url
    private String dbUrl;

    // Database triggers connection url
    private String triggersDBUrl;

    // Platform Database Credentials
    private String dbUsername;
    private String dbPassword;

    // Triggers Database Credentials
    private String triggersDBUsername;
    private String triggersDBPassword;

    private String financeReportHeaders;

    // Chrome Browser Download Path
    private String downloadPathForMac;
    private String downloadPathForWindows;

    // Invoice Generation Job Basic Auth Credentials
    private String invoiceAdminUsername;
    private String invoiceAdminPassword;

    // IMAP email properties to check emails
    private String imapHost;
    private String mailStoreType;
    private String emailUsername;
    private String emailPassword;

    // User emailaddress
    private String userEmail;
    private String userExternalKey;
    private String userId;

    // User id in twofish family
    private String getUserIdInTwoFishFamily;

    // CSE
    private String brokerUrl;
    private String pelicanEventsNotificationChannel;
    private String pelicanEventsBootstrapNotificationChannel;
    private String personMasterNotificationChannel;
    private String notificationConsKey;
    private String notificationConsSecret;
    private String authUrl;

    // DynamodDB
    private String DynamoDBTableName;

    // Autodesk Cart
    private String cartUrl;
    private String cartUserName;
    private String cartPassword;
    private String cartPaypalEmail;
    private String cartPaypalPassword;
    private String otherAppFamily;
    private String cartUserExternalKey;
    private String cartUserId;

    // Create Roles
    private String roleAppFamilyID;

    // DR migration
    private String skuCodeForMonthlyOffer;
    private String offerId;
    private String planId;
    private String priceId;
    private String ownerId;

    // Offering detail id
    private String offeringDetailId;

    // Trigger republish job url
    private String republishTriggerUrl;

    // flag for screenshot
    private Boolean isScreenShotRequired;

    // ConvergentCharging SOAP Wsdl
    private String convergentChargingWsdl;

    // flag to clean up QA mail inbox
    private boolean isCleanUpMailbox;

    // user contract number for ConvergentCharging
    private String userContractNumberForConvergentCharging;

    // get AWS Region
    private String awsRegion;

    // AUM Email test related external keys
    private String primaryAdminExternalKey;
    private String secondaryAdminExternalKey;
    private String namedPartyUserExternalKey;

    // Demo Family Price Ids
    private String bicMonthlyPriceId;

    private String taxCode;

    private String tableSuffix;

    // Subscription services url
    private String subscriptionServiceUrl;

    public String getApplicationFamilyDescription() {
        return applFamilyDesc;
    }

    public void setApplicationFamilyDescription(final String value) {
        this.applFamilyDesc = value;
    }

    public String getApplicationDescription() {
        return applDesc;
    }

    public void setApplicationDescription(final String value) {
        this.applDesc = value;
    }

    public String getAppFamily() {
        return appFamily;
    }

    public void setAppFamily(final String appFamily) {
        this.appFamily = appFamily;
    }

    public String getCombinedApplicationFamily() {
        return combinedApplFamily;
    }

    public void setCombinedApplicationFamily(final String value) {
        this.combinedApplFamily = value;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
        Util.waitInSeconds(TimeConstants.TWO_SEC);
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(final String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getTimezoneOffset() {
        return timezoneOffSet;
    }

    public void setTimezoneOffset(final String value) {
        this.timezoneOffSet = value;
    }

    public String getV2ApiUrl() {
        return apiV2Url;
    }

    public void setV2ApiUrl(final String apiV2Url) {
        this.apiV2Url = apiV2Url;
    }

    public String getV3ApiUrl() {
        return apiV3Url;
    }

    public void setV3ApiUrl(final String apiV3Url) {
        this.apiV3Url = apiV3Url;
    }

    public void setGdprUrl(final String value) {
        this.gdprUrl = value;
    }

    public String getGdprUrl() {
        return gdprUrl;
    }

    public String getPartnerId() {
        return partnerId;

    }

    public void setPartnerId(final String value) {
        this.partnerId = value;
    }

    public String getAppFamilyId() {
        return appFamilyId;
    }

    public void setAppFamilyId(final String value) {
        this.appFamilyId = value;
    }

    public String getSecretCredential() {
        return secretCredential;
    }

    public void setSecretCredential(final String value) {
        this.secretCredential = value;
    }

    public String getEnvironmentType() {
        return envType;
    }

    public void setEnvironmentType(final String value) {
        this.envType = value;
    }

    public String getTriggerUrl() {
        return triggerUrl;
    }

    public void setTriggerUrl(final String value) {
        this.triggerUrl = value;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(final String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getFeeCollectorId() {
        return feeCollectorId;
    }

    public void setFeeCollectorId(final String feeCollectorId) {
        this.feeCollectorId = feeCollectorId;
    }

    public String getBluesnapNamerPaymentGatewayId() {
        return bluesnapNamerPaymentGatewayId;
    }

    public void setBluesnapNamerPaymentGatewayId(final String bluesnapNamerPaymentGatewayId) {
        this.bluesnapNamerPaymentGatewayId = bluesnapNamerPaymentGatewayId;
    }

    public String getBluesnapEmeaPaymentGatewayId() {
        return bluesnapEmeaPaymentGatewayId;
    }

    public void setBluesnapEmeaPaymentGatewayId(final String bluesnapEmeaPaymentGatewayId) {
        this.bluesnapEmeaPaymentGatewayId = bluesnapEmeaPaymentGatewayId;
    }

    public String getPaypalNamerPaymentGatewayId() {
        return paypalNamerPaymentGatewayId;
    }

    public void setPaypalNamerPaymentGatewayId(final String paypalNamerPaymentGatewayId) {
        this.paypalNamerPaymentGatewayId = paypalNamerPaymentGatewayId;
    }

    public String getPaypalEmeaPaymentGatewayId() {
        return paypalEmeaPaymentGatewayId;
    }

    public void setPaypalEmeaPaymentGatewayId(final String paypalEmeaPaymentGatewayId) {
        this.paypalEmeaPaymentGatewayId = paypalEmeaPaymentGatewayId;
    }

    public String getJDBCDriver() {
        return JDBCDriver;
    }

    public void setJDBCDriver(final String jDBCDriver) {
        this.JDBCDriver = jDBCDriver;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(final String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(final String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(final String dbPassword) {
        this.dbPassword = dbPassword;
    }

    /**
     * Get triggers db url
     *
     * @return db url
     */
    public String getTriggersDBUrl() {
        return triggersDBUrl;
    }

    /**
     * Set triggers db url
     */
    public void setTriggersDBUrl(final String triggersDBUrl) {
        this.triggersDBUrl = triggersDBUrl;
    }

    /**
     * Get triggers db username
     *
     * @return db username
     */
    public String getTriggersDBUsername() {
        return triggersDBUsername;
    }

    /**
     * Set triggers db username
     */
    public void setTriggersDBUsername(final String triggersDBUsername) {
        this.triggersDBUsername = triggersDBUsername;
    }

    /**
     * Get triggers db password
     *
     * @return db password
     */
    public String getTriggersDBPassword() {
        return triggersDBPassword;
    }

    /**
     * Set triggers db password
     */
    public void setTriggersDBPassword(final String triggersDBPassword) {
        this.triggersDBPassword = triggersDBPassword;
    }

    /**
     * @return the financeReportHeaders
     */
    public String getFinanceReportHeaders() {
        return financeReportHeaders;
    }

    /**
     * @param financeReportHeaders the financeReportHeaders to set
     */
    public void setFinanceReportHeaders(final String financeReportHeaders) {
        this.financeReportHeaders = financeReportHeaders;
    }

    /*
     * @ return: download path for mac
     */
    public String getDownloadPathForMac() {
        return downloadPathForMac;
    }

    /*
     * @param: download path for mac
     */
    public void setDownloadPathForMac(final String downloadPathForMac) {
        this.downloadPathForMac = downloadPathForMac;
    }

    /*
     * @return: download path for windows
     */
    public String getDownloadPathForWindows() {
        return downloadPathForWindows;
    }

    /*
     * @param: download path for windows
     */
    public void setDownloadPathForWindows(final String downloadPathForWindows) {
        this.downloadPathForWindows = downloadPathForWindows;
    }

    public String getInvoiceAdminUsername() {
        return invoiceAdminUsername;
    }

    public void setInvoiceAdminUsername(final String invoiceAdminUsername) {
        this.invoiceAdminUsername = invoiceAdminUsername;
    }

    public String getInvoiceAdminPassword() {
        return invoiceAdminPassword;
    }

    public void setInvoiceAdminPassword(final String invoiceAdminPassword) {
        this.invoiceAdminPassword = invoiceAdminPassword;
    }

    public void setImaphost(final String imapHost) {
        this.imapHost = imapHost;
    }

    public String getImapHost() {
        return imapHost;
    }

    public void setEmailStoreType(final String mailStoreType) {
        this.mailStoreType = mailStoreType;
    }

    public String getEmailStoreType() {
        return mailStoreType;
    }

    public void setEmailUsername(final String emailUsername) {
        this.emailUsername = emailUsername;
    }

    public String getEmailUsername() {
        return emailUsername;
    }

    public void setEmailPassword(final String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(final String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserExternalKey() {
        return userExternalKey;
    }

    public void setUserExternalKey(final String userExternalKey) {
        this.userExternalKey = userExternalKey;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(final String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getPelicanEventsNotificationChannel() {
        return pelicanEventsNotificationChannel;
    }

    public void setPelicanEventsNotificationChannel(final String pelicanEventsNotificationChannel) {
        this.pelicanEventsNotificationChannel = pelicanEventsNotificationChannel;
    }

    public String getPelicanEventsBootstrapNotificationChannel() {
        return pelicanEventsBootstrapNotificationChannel;
    }

    public void setPelicanEventsBootstrapNotificationChannel(final String pelicanEventsBootstrapNotificationChannel) {
        this.pelicanEventsBootstrapNotificationChannel = pelicanEventsBootstrapNotificationChannel;
    }

    public String getPersonMasterNotificationChannel() {
        return personMasterNotificationChannel;
    }

    public void setPersonMasterNotificationChannel(final String personMasterNotificationChannel) {
        this.personMasterNotificationChannel = personMasterNotificationChannel;
    }

    public String getNotificationConsKey() {
        return notificationConsKey;
    }

    public void setNotificationConsKey(final String notificationConsKey) {
        this.notificationConsKey = notificationConsKey;
    }

    public String getNotificationConsSecret() {
        return notificationConsSecret;
    }

    public void setNotificationConsSecret(final String notificationConsSecret) {
        this.notificationConsSecret = notificationConsSecret;
    }

    public String getDynamoDBTable() {
        return DynamoDBTableName;
    }

    public void setDynamoDBTable(final String DynamoDBTableName) {
        this.DynamoDBTableName = DynamoDBTableName;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(final String authUrl) {
        this.authUrl = authUrl;
    }

    public String getCartUrl() {
        return cartUrl;
    }

    public void setCartUrl(final String cartUrl) {
        this.cartUrl = cartUrl;
    }

    public String getCartUserName() {
        return cartUserName;
    }

    public void setCartUserName(final String cartUserName) {
        this.cartUserName = cartUserName;
    }

    public String getCartPassword() {
        return cartPassword;
    }

    public void setCartPassword(final String cartPassword) {
        this.cartPassword = cartPassword;
    }

    public String getCartPaypalEmailAddress() {
        return cartPaypalEmail;
    }

    public void setCartPaypalEmailAddress(final String cartPaypalEmail) {
        this.cartPaypalEmail = cartPaypalEmail;
    }

    public String getCartPaypalPassword() {
        return cartPaypalPassword;
    }

    public void setCartPaypalPassword(final String cartPaypalPassword) {
        this.cartPaypalPassword = cartPaypalPassword;
    }

    public String getOtherAppFamily() {
        return otherAppFamily;
    }

    public void setOtherAppFamily(final String otherAppFamily) {
        this.otherAppFamily = otherAppFamily;
    }

    public String getCartUserId() {
        return cartUserId;
    }

    public void setCartUserId(final String cartUserId) {
        this.cartUserId = cartUserId;
    }

    public String getCartUserExternalKey() {
        return cartUserExternalKey;
    }

    public void setCartUserExternalKey(final String cartUserExternalKey) {
        this.cartUserExternalKey = cartUserExternalKey;
    }

    public String getSkuCodeForMonthlyOffer() {
        return skuCodeForMonthlyOffer;
    }

    public void setSkuCodeForMonthlyOffer(final String skuCodeForMonthlyOffer) {
        this.skuCodeForMonthlyOffer = skuCodeForMonthlyOffer;
    }

    public String getOfferingDetailId() {
        return offeringDetailId;
    }

    public void setOfferingDetailId(final String offeringDetailId) {
        this.offeringDetailId = offeringDetailId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(final String planId) {
        this.planId = planId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(final String offerId) {
        this.offerId = offerId;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(final String priceId) {
        this.priceId = priceId;
    }

    public String getCloudCurrencyId() {
        return cloudCurrencyId;
    }

    public void setCloudCurrencyId(final String cloudCurrencyId) {
        this.cloudCurrencyId = cloudCurrencyId;
    }

    /**
     * @return the roleAppDamilyID
     */
    public String getRoleAppFamilyID() {
        return roleAppFamilyID;
    }

    /**
     * @param roleAppFamilyID the roleAppDamilyID to set
     */
    public void setRoleAppFamilyID(final String roleAppFamilyID) {
        this.roleAppFamilyID = roleAppFamilyID;
    }

    /**
     * @return the getUserIdInTwoFishFamily
     */
    public String getGetUserIdInTwoFishFamily() {
        return getUserIdInTwoFishFamily;
    }

    /**
     * @param getUserIdInTwoFishFamily the getUserIdInTwoFishFamily to set
     */
    public void setUserIdInTwoFishFamily(final String getUserIdInTwoFishFamily) {
        this.getUserIdInTwoFishFamily = getUserIdInTwoFishFamily;
    }

    /**
     * @return the otherAppFamilyId
     */
    public String getOtherAppFamilyId() {
        return otherAppFamilyId;
    }

    /**
     * @param otherAppFamilyId the otherAppFamilyId to set
     */
    public void setOtherAppFamilyId(final String otherAppFamilyId) {
        this.otherAppFamilyId = otherAppFamilyId;
    }

    /**
     * Method to return appId
     *
     * @return appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Method to set app id
     *
     * @param value
     */
    public void setAppId(final String value) {
        this.appId = value;
    }

    /**
     * Method to return republishTriggerUrl
     *
     * @return republishTriggerUrl
     */
    public String getRepublishTriggerUrl() {
        return republishTriggerUrl;
    }

    /**
     * Method to set republishTriggerUrl republishTriggerUrl would like => <triggerUsername>:
     * <triggersPassword>@<triggers URL>
     *
     * @param republishTriggerUrl
     */
    public void setRepublishTriggerUrl(final String republishTriggerUrl) {
        this.republishTriggerUrl = republishTriggerUrl;
    }

    /**
     * Method to get the ConvergentCharging SOAP Wsdl URL
     *
     * @return
     */
    public String getConvergentChargingWsdl() {
        return convergentChargingWsdl;
    }

    /**
     * Method to set the ConvergentCharging SOAP Wsdl URL
     *
     * @param convergentChargingWsdl required
     */
    public void setConvergentChargingWsdl(final String convergentChargingWsdl) {
        this.convergentChargingWsdl = convergentChargingWsdl;
    }

    /**
     * Method to return flag value for takeScreenshot
     *
     * @return takeScreenshot
     */
    public Boolean getScreenshotFlag() {
        return isScreenShotRequired;
    }

    /**
     * Method to set flag value takeScreenshot
     *
     * @param isScreenShotRequired
     */
    public void setScreenshotFlag(final String isScreenShotRequired) {
        this.isScreenShotRequired = Boolean.valueOf(isScreenShotRequired);
    }

    /**
     * Method to return flag value for clean up email inbox.
     *
     * @return isCleanUpMailbox
     */
    public boolean getCleanUpMailboxFlag() {
        return isCleanUpMailbox;
    }

    /**
     * Method to set flag value for clean up email inbox.
     *
     * @param isCleanUpMailbox
     */
    public void setCleanUpMailboxFlag(final String isCleanUpMailbox) {
        this.isCleanUpMailbox = Boolean.valueOf(isCleanUpMailbox);
    }

    /**
     * Method to return finance report date range.
     *
     * @return isCleanUpMailbox
     */
    public String getFinanceReportDateRange() {
        return financeReportDateRange;
    }

    /**
     * Method to set finance report date range.
     *
     * @param financeReportDateRange
     */
    public void setFinanceReportDateRange(final String financeReportDateRange) {
        this.financeReportDateRange = financeReportDateRange;
    }

    /**
     * Method to set userContractNumberForConvergentCharging.
     *
     * @param userContractNumberForConvergentCharging
     */
    public void setUserContractNumberForConvergentCharging(final String userContractNumberForConvergentCharging) {
        this.userContractNumberForConvergentCharging = userContractNumberForConvergentCharging;
    }

    /**
     * Method to get userContractNumberForConvergentCharging.
     *
     * @return userContractNumberForConvergentCharging
     */
    public String getUserContractNumberForConvergentCharging() {
        return userContractNumberForConvergentCharging;
    }

    /**
     * Method to set AWS Region
     *
     * @param awsRegion
     */
    public void setAWSRegion(final String awsRegion) {
        this.awsRegion = awsRegion;
    }

    /**
     * Method to return AWS Region
     *
     * @return awsRegion
     */
    public String getAWSRegion() {
        return awsRegion;
    }

    public String getBrowser() {
        return browser;
    }

    // set browser name from environmentVariables properties
    public void setBrowser(final String browser) {
        this.browser = browser;
    }

    public String getPrimaryAdminExternalKey() {
        return primaryAdminExternalKey;
    }

    public void setPrimaryAdminExternalKey(final String primaryAdminExternalKey) {
        this.primaryAdminExternalKey = primaryAdminExternalKey;
    }

    public String getSecondaryAdminExternalKey() {
        return secondaryAdminExternalKey;
    }

    public void setSecondaryAdminExternalKey(final String secondaryAdminExternalKey) {
        this.secondaryAdminExternalKey = secondaryAdminExternalKey;
    }

    public String getNamedPartyUserExternalKey() {
        return namedPartyUserExternalKey;
    }

    public void setNamedPartyUserExternalKey(final String namedPartyUserExternalKey) {
        this.namedPartyUserExternalKey = namedPartyUserExternalKey;
    }

    public String getBicMonthlyPriceId() {
        return bicMonthlyPriceId;
    }

    public void setBicMonthlyPriceId(final String bicMonthlyPriceId) {
        this.bicMonthlyPriceId = bicMonthlyPriceId;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(final String taxCode) {
        this.taxCode = taxCode;
    }

    public String getTableSuffix() {
        return tableSuffix;
    }

    public void setTableSuffix(final String tableSuffix) {
        this.tableSuffix = tableSuffix;
    }

    public String getSubscriptionServiceUrl() {
        return subscriptionServiceUrl;
    }

    public void setSubscriptionServiceUrl(final String subscriptionServiceUrl) {
        this.subscriptionServiceUrl = subscriptionServiceUrl;
    }
}
