package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiHealthCheck;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiHealthCheckAttributes;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiHealthCheckData;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonApiHealthCheckDetails;
import com.autodesk.bsm.pelican.api.pojos.trigger.JsonHealthCheckApi;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class is used to get health check url and to parse the json response content
 *
 * @author Rohini
 */
public class HealthCheckStatusClient {
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckStatusClient.class.getSimpleName());

    /*
     * Set up the environment
     */
    public HealthCheckStatusClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get Healthcheck api
     *
     * @return Pelican Pojo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T getHealthCheckStatus() {

        T pojo;
        LOGGER.info("Get HealthCheck Status");
        final CloseableHttpResponse response = client.doGet(getUrl(), null, authInfo, null, null);
        LOGGER.info("Parsing the get health check Response");
        pojo = (T) parseResponse(response);

        return pojo;
    }

    /**
     * Method to get the pelican trigger url
     *
     * @return HealthCheckUrl
     */
    private String getUrl() {
        return environmentVariables.getHealthCheckUrl();
    }

    /**
     * Method to parse the json response
     *
     * @return jsonApiHealthCheck
     */
    private JsonApiHealthCheck parseResponse(final CloseableHttpResponse response) {

        final JsonApiHealthCheck jsonApiHealthCheck = new JsonApiHealthCheck();
        final int status = response.getStatusLine().getStatusCode();

        try {
            final Gson gson = new GsonBuilder().create();

            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();

            // checking Http status response
            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);

                // Parse error
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                jsonApiHealthCheck.setErrors(errors[0]);
            } else {

                // Parse JSON Api Object
                if (!jsonObject.get("jsonapi").isJsonNull()) {

                    // Comparing with JsonArray
                    if (!jsonObject.get("jsonapi").isJsonArray()) {
                        final JsonHealthCheckApi healthCheckObject =
                            gson.fromJson(jsonObject.getAsJsonObject("jsonapi").toString(), JsonHealthCheckApi.class);
                        jsonApiHealthCheck.setJsonApi(healthCheckObject);
                    }
                }
                // Retrieving json values such as data, attributes and details
                if (!jsonObject.get("data").isJsonNull()) {
                    if (!jsonObject.get("data").isJsonArray()) {

                        final JsonApiHealthCheckData data =
                            gson.fromJson(jsonObject.getAsJsonObject("data").toString(), JsonApiHealthCheckData.class);
                        jsonApiHealthCheck.setData(data);
                        final JsonObject jsonObject1 = jsonObject.getAsJsonObject("data");
                        final JsonApiHealthCheckAttributes attributes = gson.fromJson(
                            jsonObject1.getAsJsonObject("attributes").toString(), JsonApiHealthCheckAttributes.class);
                        data.setJsonApiHealthCheckAttributes(attributes);
                        final JsonObject jsonObject2 = jsonObject1.getAsJsonObject("attributes");
                        final JsonApiHealthCheckDetails details = gson.fromJson(
                            jsonObject2.getAsJsonObject("details").toString(), JsonApiHealthCheckDetails.class);
                        attributes.setJsonApiHealthCheckDetails(details);
                    }
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return jsonApiHealthCheck;
    }

}
