package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatus;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiJobStatusesData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiWipData;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class JobStatusesClient {

    private static final String END_POINT = "jobStatuses";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private String baseURL;

    private HttpRestClient client = new HttpRestClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(JobStatusesClient.class.getSimpleName());

    public JobStatusesClient(final String baseUrl) {
        this.baseURL = baseUrl;
    }

    /**
     * This method will hit the get job statuses api without any query parameters
     *
     * @return JsonApiJobStatusesData
     */
    public JsonApiJobStatusesData getJobStatuses() throws ParseException {

        final CloseableHttpResponse response = client.doGet(getUrl(), null, null, CONTENT_TYPE, CONTENT_TYPE);

        if (response == null) {
            return null;
        }

        return parseResponse(response);
    }

    /**
     * This method will run the job statuses api with optional query paremeters
     *
     * @return JsonApiJobStatusesData
     */
    @SuppressWarnings("null")
    public JsonApiJobStatusesData getJobStatuses(final String jobCategory, final String jobState,
        final String startDate, final String endDate) throws ParseException {

        String url = getUrl();

        if (jobCategory != null && !(jobCategory.isEmpty())) {
            url = url + "?" + "jobCategory" + "=" + jobCategory;
        }

        if (jobState != null && !(jobState.isEmpty())) {
            url = url + "&" + "jobState" + "=" + jobState;
        }

        if (startDate != null && !(startDate.isEmpty())) {
            url = url + "&" + "createdDateStart" + "=" + startDate;
        }

        if (endDate != null && !(endDate.isEmpty())) {
            url = url + "&" + "createdDateEnd" + "=" + endDate;
        }

        final CloseableHttpResponse response = client.doGet(url, null, null, CONTENT_TYPE, CONTENT_TYPE);

        if (response == null) {
            return null;
        }

        return parseResponse(response);
    }

    /**
     * This method will hit the get job statuses api with job id
     */
    public JsonApiJobStatusData getById(final String jobId) {

        final CloseableHttpResponse response =
            client.doGet(getUrl() + "/" + jobId, null, null, CONTENT_TYPE, CONTENT_TYPE);

        if (response == null) {
            return null;
        }

        return (JsonApiJobStatusData) parseResponse(response, JsonApiJobStatusData.class);
    }

    /**
     * Get first page of wips record by job id
     */
    public JsonApiWipData getWipsById(final String jobId) {

        final CloseableHttpResponse response =
            client.doGet(getUrl() + "/" + jobId + "/wips", null, null, CONTENT_TYPE, CONTENT_TYPE);

        if (response == null) {
            return null;
        }

        return (JsonApiWipData) parseResponse(response, JsonApiWipData.class);
    }

    /**
     * Get first page of wips record by job id, wip state, object type and object id
     *
     * @param object type
     * @param object id
     * @return JsonApiWipData
     */
    public JsonApiWipData getWipsById(final String jobId, final String wipState, final String objectType,
        final String objectId) {

        String url = getUrl() + "/" + jobId + "/wips";

        if (wipState != null && !(wipState.isEmpty())) {
            url = url + "?" + "wipState" + "=" + wipState;
        }

        if (objectType != null && !(objectType.isEmpty())) {
            url = url + "&" + "objectType" + "=" + objectType;
        }

        if (objectId != null && !(objectId.isEmpty())) {
            url = url + "&" + "objectId" + "=" + objectId;
        }

        final CloseableHttpResponse response = client.doGet(url, null, null, CONTENT_TYPE, CONTENT_TYPE);

        if (response == null) {
            return null;
        }

        return (JsonApiWipData) parseResponse(response, JsonApiWipData.class);
    }

    /**
     * Get page x of wips record by job id
     */
    public JsonApiWipData getWipsById(final String jobId, final int pageNumber) {

        if (pageNumber < 0) {
            throw new RuntimeException("Page number must be equal or greater than 0");
        } else if (pageNumber == 0) {
            return getWipsById(jobId);
        }
        final CloseableHttpResponse response = client.doGet(getUrl() + "/" + jobId + "/wips?page[number]=" + pageNumber,
            null, null, CONTENT_TYPE, CONTENT_TYPE);

        if (response == null) {
            return null;
        }

        return (JsonApiWipData) parseResponse(response, JsonApiWipData.class);
    }

    private String getUrl() {
        return baseURL + "/" + END_POINT;
    }

    /**
     * This method parse the http response and return the response object
     */
    private Object parseResponse(final CloseableHttpResponse response, final Class<?> klass) {

        Object data = null;
        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final Gson gson = new GsonBuilder().create();
            data = gson.fromJson(content, klass);

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

    /**
     * This method will parse the http response and return the JsonApiJobStatusesData object
     */
    private JsonApiJobStatusesData parseResponse(final CloseableHttpResponse response) {

        final JsonApiJobStatusesData jsonApiJobStatusesData = new JsonApiJobStatusesData();
        JsonApiJobStatus[] jobstatusArray;
        Errors[] errorArray;

        final int status = response.getStatusLine().getStatusCode();
        LOGGER.info("Status is: " + status);

        try {
            final Gson gson = new GsonBuilder().create();

            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();

            if (status == 200) {
                final JsonArray dataArray = jsonObject.getAsJsonArray("data");
                jobstatusArray = gson.fromJson(dataArray.toString(), JsonApiJobStatus[].class);
                jsonApiJobStatusesData.setData(Arrays.asList(jobstatusArray));
            } else if (!(jsonObject.get("errors") instanceof JsonNull)) {
                final JsonArray errorsArray = jsonObject.getAsJsonArray("errors");
                errorArray = gson.fromJson(errorsArray.toString(), Errors[].class);
                jsonApiJobStatusesData.setErrors(Arrays.asList(errorArray));
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return jsonApiJobStatusesData;
    }
}
