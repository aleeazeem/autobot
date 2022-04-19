package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusesData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonInvoiceNumberStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonPendingPurchaseOrderStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonSubscriptionId;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.JobCategory;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.Util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class JobsClient {

    private static final String END_POINT = "jobs";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final String ACCEPT = "application/vnd.api+json";
    private String baseURL;
    private HttpRestClient client = new HttpRestClient();
    private AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsClient.class.getSimpleName());

    private enum TriggerJob {

        SCF_EMAILS("SCFEmailsForProductLine"),
        INVOICE_NUMBER("invoiceNumber"),
        PENDING_PURCHASEORDER_SFDC_CASE("monitor/pendingPurchaseOrders"),
        PROMOTION_EXPIRATION("promotionExpiration"),
        UPLOAD_JOB_CLEAN_UP("uploadJobCleanup"),
        SUBSCRIPTION_RENEWALS("subscriptionRenewals"),
        SUBSCRIPTION_EXPIRATION("subscriptionExpiration"),
        RENEWAL_REMINDERS("renewalReminders"),
        SCF_WEEKLY_JOB("SCFEmailsForWeeklyJob"),
        SUBSCRIPTION_MONITOR("monitor/subscription/renewal"),
        SUBSCRIPTION_EXPIRATION_REMINDER("expirationReminders"),
        ENTITLEMENT_END_DATE_NOTIFICATION("entitlementsEndDateNotification");

        private String jobName;

        TriggerJob(final String jobName) {
            this.jobName = jobName;
        }

        String getJobName() {
            return jobName;
        }

    }

    public JobsClient(final String baseURL, final EnvironmentVariables environmentVariables) {
        this.baseURL = baseURL;
        authInfo = new AuthenticationInfo(environmentVariables, environmentVariables.getAppFamily());

    }

    /**
     * Trigger job for SCFEmails
     */
    public JsonApiJobStatusData scfEmails() {

        final CloseableHttpResponse response =
            client.doPost(getUrl() + "/" + TriggerJob.SCF_EMAILS.getJobName(), null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }

        return parseResponse(response);
    }

    /**
     * Trigger job for Invoice Numbers
     * <p>
     * A sample usage in the tests could be like this environmentVariables = new EnvironmentType().init("AUTO_FAMILY");
     * resource = new PelicanResource(environmentVariables).trigger();
     * <p>
     * JobsResource jobsResource = resource.jobs(); JsonInvoiceNumberStatus response = jobsResource.invoiceNumbers();
     */
    public JsonInvoiceNumberStatus invoiceNumbers(final String authorization) {

        final CloseableHttpResponse response = client.doPost(getUrl() + "/" + TriggerJob.INVOICE_NUMBER.getJobName(),
            null, CONTENT_TYPE, null, true, authorization);

        if (response == null) {
            return null;
        }

        return parseInvoiceNumberResponse(response);
    }

    /**
     * Trigger job for Pending Purchase Order.
     */
    public Collection<JsonPendingPurchaseOrderStatus> pendingPurchaseOrder() {

        final CloseableHttpResponse response =
            client.doPost(getUrl() + "/" + TriggerJob.PENDING_PURCHASEORDER_SFDC_CASE.getJobName(), null, CONTENT_TYPE,
                null, false, null);

        if (response == null) {
            return null;
        }

        final Collection<JsonPendingPurchaseOrderStatus> data = parsePendingPurchaseOrderResponse(response);

        LOGGER.info("The Response from Pending Purchase Order SFDC case trigger job is : "
            + response.getStatusLine().getStatusCode());

        return data;
    }

    /**
     * Trigger job for scf job for industry collection
     */
    public CloseableHttpResponse scfForWeeklyJob() {

        final CloseableHttpResponse response = client.doPost(getUrl() + "/" + TriggerJob.SCF_WEEKLY_JOB.getJobName(),
            null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        LOGGER.info("The Response from scf trigger job is : " + response.getStatusLine().getStatusCode());

        return response;
    }

    /**
     * Trigger job for promotion expiration
     */
    public JsonStatus promotionExpiration(final String promotionID) {

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.PROMOTION_EXPIRATION.getJobName(), "{\"promoIds\":\"" + promotionID + "\"}",
            PelicanConstants.APPLICATION_JSON, PelicanConstants.APPLICATION_JSON, false, null);

        if (response == null) {
            return null;
        }
        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info(
            "The Response from Promotion Expiration trigger job is : " + response.getStatusLine().getStatusCode());
        return data;
    }

    /**
     * Trigger job for promotion expiration
     */
    public JsonStatus promotionExpiration() {

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.PROMOTION_EXPIRATION.getJobName(), null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info(
            "The Response from Promotion Expiration trigger job is : " + response.getStatusLine().getStatusCode());
        return data;
    }

    /**
     * Trigger job for Renewal Reminder
     */
    public JsonStatus renewalReminder() {

        final CloseableHttpResponse response = client.doPost(getUrl() + "/" + TriggerJob.RENEWAL_REMINDERS.getJobName(),
            null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info(
            "The Response from Trigger job for Renewal Reminder is : " + response.getStatusLine().getStatusCode());
        return data;
    }

    /**
     * Trigger job for subscription auto renewals
     *
     * @return JsonApiJobStatusData
     */
    public JsonApiJobStatusData subscriptionRenewals() {

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.SUBSCRIPTION_RENEWALS.getJobName(), null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        final JsonApiJobStatusData jsonApiJobStatusData = parseResponse(response);
        LOGGER.info("The status from Subscription Renewal response is : " + response.getStatusLine().getStatusCode());
        return jsonApiJobStatusData;
    }

    /**
     * Trigger job for subscription renewal with subscription id
     *
     * @param environmentVariables
     * @param String comma separated list of subscription ids
     *
     * @return subscriptionRenewalJobGuid
     */
    public String subscriptionRenewals(final JsonSubscriptionId subscriptionId,
        final EnvironmentVariables environmentVariables) {
        final String body = toJSONSubscriptionId(subscriptionId);
        LOGGER.info("*****body " + body);

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.SUBSCRIPTION_RENEWALS.getJobName(), body, authInfo, CONTENT_TYPE, ACCEPT);

        if (response == null) {
            return null;
        }
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.MINI_WAIT);

        // Run the Get Job Statuses Api
        final PelicanTriggerClient triggerResource = new PelicanClient(environmentVariables).trigger();
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        JsonApiJobStatusesData jobStatusApiResponse = null;
        try {
            jobStatusApiResponse =
                jobsStatusesResource.getJobStatuses(JobCategory.SUBSCRIPTION_RENEWALS.toString(), null, null, null);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        final List<JsonApiJobStatus> jobStatusesList = jobStatusApiResponse.getData();

        String subscriptionRenewalJobGuid = null;
        if (jobStatusesList.size() > 0) {
            subscriptionRenewalJobGuid = jobStatusesList.get(0).getId();
        }
        LOGGER.info("SubscriptionRenewalJobGuid: " + subscriptionRenewalJobGuid);
        return subscriptionRenewalJobGuid;
    }

    /**
     * Trigger job for subscription expiration
     *
     * @return JsonStatus
     */
    public JsonStatus subscriptionExpirationForSingleSubscription(final JsonSubscriptionId subscriptionId) {

        final String body = toJSONSubscriptionId(subscriptionId);
        LOGGER.info("*****body " + body);

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.SUBSCRIPTION_EXPIRATION.getJobName(), body, authInfo, CONTENT_TYPE, ACCEPT);

        if (response == null) {
            return null;
        }
        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        return data;
    }

    /**
     * Trigger job for subscription expiration
     *
     * @return JsonStatus
     */
    public JsonStatus subscriptionExpiration() {

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.SUBSCRIPTION_EXPIRATION.getJobName(), null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        return data;
    }

    /**
     * Trigger job for subscription expiration with subscription id
     *
     * @param String comma separated list of subscription ids
     * @return JsonStatus
     */
    public JsonStatus subscriptionExpiration(final JsonSubscriptionId subscriptionId) {
        final String body = toJSONSubscriptionId(subscriptionId);
        LOGGER.info("*****body " + body);

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.SUBSCRIPTION_EXPIRATION.getJobName(), body, authInfo, CONTENT_TYPE, ACCEPT);

        if (response == null) {
            return null;
        }
        LOGGER.info("Parsing the correct response");

        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        return data;
    }

    /**
     * Trigger job for upload clean up
     */
    public String uploadJobCleanUp() {

        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.UPLOAD_JOB_CLEAN_UP.getJobName(), null, CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        final String content = parseUploadJobCleanUpResponse(response);
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        return content;
    }

    /**
     * Trigger job for subscription monitoring.
     */
    public JsonApiJobStatus subscriptionMonitoring() {
        final CloseableHttpResponse response = client.doPost(
            getUrl() + "/" + TriggerJob.SUBSCRIPTION_MONITOR.getJobName(), null, CONTENT_TYPE, null, false, null);
        if (response == null) {
            return null;
        }
        final JsonApiJobStatus jsonApiJobStatus = parseSubscriptionMonitoringJobResponse(response);
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.LONG_WAIT);

        return jsonApiJobStatus;
    }

    /**
     * Trigger job for subscription expiration reminder.
     */
    public JsonApiJobStatus subscriptionExpirationReminder(final EnvironmentVariables environmentVariables) {
        final CloseableHttpResponse response =
            client.doPost(getUrl() + "/" + TriggerJob.SUBSCRIPTION_EXPIRATION_REMINDER.getJobName(), null, CONTENT_TYPE,
                null, false, null);
        if (response == null) {
            return null;
        }
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        final PelicanTriggerClient triggerResource = new PelicanClient(environmentVariables).trigger();
        final JobStatusesClient jobsStatusesResource = triggerResource.jobStatuses();
        JsonApiJobStatusesData jsonResponse = null;

        try {
            jsonResponse =
                jobsStatusesResource.getJobStatuses(JobCategory.EXPIRATION_REMINDERS.toString(), null, null, null);
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        final List<JsonApiJobStatus> jobStatusesList = jsonResponse.getData();
        if (jobStatusesList.size() > 0) {
            return jobStatusesList.get(0);
        }
        return null;
    }

    private String getUrl() {
        return baseURL + "/" + END_POINT;
    }

    private JsonApiJobStatusData parseResponse(final CloseableHttpResponse response) {

        JsonApiJobStatusData data = null;
        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Response = " + content);

            final Gson gson = new GsonBuilder().setDateFormat("MM-dd-yyyy").create();
            data = gson.fromJson(content, JsonApiJobStatusData.class);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private JsonInvoiceNumberStatus parseInvoiceNumberResponse(final CloseableHttpResponse response) {

        JsonInvoiceNumberStatus data = null;
        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final Gson gson = new GsonBuilder().setDateFormat("MM-dd-yyyy").create();
            data = gson.fromJson(content, JsonInvoiceNumberStatus.class);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private Collection<JsonPendingPurchaseOrderStatus> parsePendingPurchaseOrderResponse(
        final CloseableHttpResponse response) {
        Collection<JsonPendingPurchaseOrderStatus> data = null;
        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final Gson gson = new GsonBuilder().create();

            final Type collectionType = new TypeToken<Collection<JsonPendingPurchaseOrderStatus>>() {}.getType();
            data = gson.fromJson(content, collectionType);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private JsonStatus parseExpirationJobResponse(final CloseableHttpResponse response) {
        JsonStatus content = null;
        try {
            final Gson gson = new GsonBuilder().create();
            content = gson.fromJson(EntityUtils.toString(response.getEntity()), JsonStatus.class);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    private String parseUploadJobCleanUpResponse(final CloseableHttpResponse response) {
        String content = null;

        try {
            content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Response message is " + content);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    /**
     * This method converts subscription id to json format. Used for expiration, renewal and invoice job.
     *
     * @return String
     */
    private String toJSONSubscriptionId(final JsonSubscriptionId subscriptionId) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(subscriptionId);
    }

    /**
     * This method parse Subscription Monitoring Job Response.
     *
     * @param response
     * @return JsonApiJobStatus
     */
    private JsonApiJobStatus parseSubscriptionMonitoringJobResponse(final CloseableHttpResponse response) {
        JsonApiJobStatus jsonApiJobStatus = new JsonApiJobStatus();
        try {
            final Gson gson = new GsonBuilder().create();
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);
            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
            jsonApiJobStatus = gson.fromJson(jsonObject.getAsJsonObject("data").toString(), JsonApiJobStatus.class);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return jsonApiJobStatus;
    }

    /**
     * Trigger job for entitlement end date notification
     *
     * @return JsonStatus
     */
    public JsonStatus entitlementEndDate() {

        final CloseableHttpResponse response =
            client.doPost(getUrl() + "/" + TriggerJob.ENTITLEMENT_END_DATE_NOTIFICATION.getJobName(), null,
                CONTENT_TYPE, null, false, null);

        if (response == null) {
            return null;
        }
        final JsonStatus data = parseExpirationJobResponse(response);
        LOGGER.info("The status from response is : " + response.getStatusLine().getStatusCode());
        Util.waitInSeconds(TimeConstants.FIFTEEN_SEC);
        return data;
    }
}
