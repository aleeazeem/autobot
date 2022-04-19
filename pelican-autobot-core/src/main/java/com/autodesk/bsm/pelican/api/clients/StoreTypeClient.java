package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Data;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.StoreType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// import org.json.JSONObject;

/*
 * This class is used to add a store type and also to parse the response returned after hitting the create store type
 * api
 */
public class StoreTypeClient {

    private static final String END_POINT = "storetypes";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreTypeClient.class.getSimpleName());

    public StoreTypeClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add a store type
     *
     * @return Pelican Pojo
     */
    public <T extends PelicanPojo> T addStoreType(final StoreType storeType) {

        T pojo;
        LOGGER.info("Add Store Type");
        final String body = toJSON(storeType);
        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        LOGGER.info("Parsing the Add Store Type Response");
        pojo = (T) parseResponse(response);
        return pojo;
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private String toJSON(final StoreType storeType) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(storeType);
    }

    private StoreType parseResponse(final CloseableHttpResponse response) {

        final StoreType storeType = new StoreType();
        final int status = response.getStatusLine().getStatusCode();

        try {
            final Gson gson = new GsonBuilder().create();

            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
            if (status != HttpStatus.SC_CREATED) {
                LOGGER.error("Bad request with status code = " + status);
                // Parse error
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                storeType.setErrors(errors[0]);
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final StoreType storeObj = gson.fromJson(jsonObj, StoreType.class);
                        }
                    } else {
                        final Data data = gson.fromJson(jsonObject.getAsJsonObject("data").toString(), Data.class);
                        storeType.setData(data);
                    }
                }
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
        return storeType;
    }
}
