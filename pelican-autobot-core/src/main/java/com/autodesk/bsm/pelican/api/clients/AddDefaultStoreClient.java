package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.AddDefaultStore;
import com.autodesk.bsm.pelican.api.pojos.json.DefaultStoreData;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
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
import java.util.Arrays;

/*
 * This class is used to add a store type and also to parse the response returned after hitting the create store type
 * api
 */
public class AddDefaultStoreClient {

    private static final String END_POINT = "defaultStores";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client;
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(AddDefaultStoreClient.class.getSimpleName());

    public AddDefaultStoreClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
        client = new HttpRestClient();
    }

    /**
     * Add a default store
     *
     * @return Pelican Pojo
     */
    public <T extends PelicanPojo> T addDefaultStore(final AddDefaultStore store) {

        T pojo;
        LOGGER.info("Add default store");
        final String body = toJSON(store);
        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        LOGGER.info("Parsing the Add Default Store  Response");
        pojo = (T) parseResponse(response);
        return pojo;
    }

    /**
     * This method constructs the URL for the create default store api
     *
     * @return URL
     */
    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * This method will marshal the Default Store Java Object to JSON String
     *
     * @return JSON String
     */
    private String toJSON(final AddDefaultStore store) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(store);
    }

    /**
     * This method will parse the json response returned by the api and unmarshal the response into DefaultStore object
     *
     * @return DefaultStore object
     */
    private AddDefaultStore parseResponse(final CloseableHttpResponse response) {

        final AddDefaultStore store = new AddDefaultStore();
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
                store.setErrors(Arrays.asList(errors));
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final AddDefaultStore storeObj = gson.fromJson(jsonObj, AddDefaultStore.class);
                        }
                    } else {
                        final DefaultStoreData data =
                            gson.fromJson(jsonObject.getAsJsonObject("data").toString(), DefaultStoreData.class);
                        store.setData(data);
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
        return store;
    }
}
