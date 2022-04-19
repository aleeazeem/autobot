package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.DefaultStoreData;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.GetDefaultStore;
import com.autodesk.bsm.pelican.api.pojos.json.MetaPagination;
import com.autodesk.bsm.pelican.api.pojos.json.Pagination;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class provides the methods which actually invoke the getDefaultStore API and returns the parsed response.
 *
 * @author Muhammad
 */
public class GetDefaultStoreClient {

    private static final String END_POINT = "defaultStores";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDefaultStoreClient.class.getSimpleName());

    public enum Parameter {
        COUNTRY_CODE("filter[countryCode]"),
        STORE_TYPE("filter[storeType]"),
        START_INDEX("fr.startIndex"),
        BLOCK_SIZE("fr.blockSize"),
        SKIP_COUNT("fr.skipCount");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Constructor to set the same environment across tests
     *
     * @param appFamily TODO
     */
    public GetDefaultStoreClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get default store by storeType and country
     *
     * @return response
     */
    public <T extends PelicanPojo> T getDefaultStore(final Map<String, String> params) {
        final CloseableHttpResponse response =
            client.doGet(getUrl(), params, authInfo, PelicanConstants.CONTENT_TYPE, PelicanConstants.CONTENT_TYPE);
        // Return the default store
        return parseResponse(response, params);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * Get Default Store response has data and errors array.
     *
     * @return default store or HttpError
     */
    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T parseResponse(final CloseableHttpResponse response,
        final Map<String, String> params) {
        T pojo = null;
        final int status = response.getStatusLine().getStatusCode();
        final GetDefaultStore getDefaultStore = new GetDefaultStore();

        try {
            final Gson gson = new GsonBuilder().create();
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);
            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();

            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);
                final HttpError httpError = new HttpError();
                httpError.setStatus(response.getStatusLine().getStatusCode());
                httpError.setErrorMessage(response.getStatusLine().getReasonPhrase());
                pojo = (T) httpError;
            } else {
                if (!(jsonObject.get(PelicanConstants.DATA) instanceof JsonNull)) {
                    final JsonArray dataArray = jsonObject.getAsJsonArray(PelicanConstants.DATA);
                    final List<DefaultStoreData> defaultStoreDataList = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++) {
                        final DefaultStoreData defaultStoreData = new DefaultStoreData();
                        final JsonObject dataObject = dataArray.get(i).getAsJsonObject();
                        defaultStoreData.setType(dataObject.getAsJsonPrimitive(PelicanConstants.TYPE).getAsString());
                        defaultStoreData.setId(dataObject.getAsJsonPrimitive(PelicanConstants.ID).getAsString());
                        defaultStoreData
                            .setStoreType(dataObject.getAsJsonPrimitive(PelicanConstants.STORE_TYPE).getAsString());
                        defaultStoreData
                            .setCountryCode(dataObject.getAsJsonPrimitive(PelicanConstants.COUNTRY_CODE).getAsString());
                        defaultStoreData.setStore(dataObject.getAsJsonPrimitive(PelicanConstants.STORE).getAsString());
                        defaultStoreDataList.add(defaultStoreData);
                    }
                    getDefaultStore.setData(defaultStoreDataList);
                }
            }
            // Parse errors if present.
            if (!(jsonObject.get(PelicanConstants.ERRORS) instanceof JsonNull)) {
                final JsonArray errorsArray = jsonObject.getAsJsonArray(PelicanConstants.ERRORS);
                final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
                getDefaultStore.setErrors(Arrays.asList(errors));
            }
            // parse meta object
            if ((jsonObject.get(PelicanConstants.ERRORS) instanceof JsonNull)) {
                final Pagination pagination = new Pagination();
                final MetaPagination metaPagination = new MetaPagination();

                final JsonObject metaPaginationObject = jsonObject.getAsJsonObject(PelicanConstants.META);
                final JsonObject paginationObject = metaPaginationObject.getAsJsonObject(PelicanConstants.PAGINATION);
                pagination.setblockSize(paginationObject.getAsJsonPrimitive(PelicanConstants.BLOCK_SIZE).getAsString());
                if (params.containsKey(Parameter.SKIP_COUNT.getName())) {
                    pagination.setCount(paginationObject.getAsJsonPrimitive(PelicanConstants.COUNT).getAsString());
                }
                pagination.setSkipCount(paginationObject.getAsJsonPrimitive(PelicanConstants.SKIP_COUNT).getAsString());
                pagination
                    .setstartIndex(paginationObject.getAsJsonPrimitive(PelicanConstants.START_INDEX).getAsString());

                metaPagination.setPagination(pagination);
                getDefaultStore.setMeta(metaPagination);
            }

            pojo = (T) getDefaultStore;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return pojo;
    }
}
