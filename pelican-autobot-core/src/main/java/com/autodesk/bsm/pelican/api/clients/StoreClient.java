package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.JStore;
import com.autodesk.bsm.pelican.api.pojos.json.JStore.Included;
import com.autodesk.bsm.pelican.api.pojos.json.JsonApi;
import com.autodesk.bsm.pelican.api.pojos.json.PaymentMethod;
import com.autodesk.bsm.pelican.api.pojos.json.PriceList;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod;
import com.autodesk.bsm.pelican.enums.EntityType;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for Store This class provides the methods which actually invokes the getSTore API and return the parsed
 * responses.
 *
 * @author kishor
 */
public class StoreClient {

    private static final String END_POINT = "store";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreClient.class.getSimpleName());

    public enum Parameter {
        COUNTRY("filter[country]"),
        TYPE("filter[storeType]"),
        EXTKEY("filter[externalKey]"),
        PRICE_ID("filter[priceId]");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    public StoreClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;

        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get Store by Id
     *
     * @return Store
     */
    public JStore getStore(final String id) {

        final CloseableHttpResponse response =
            client.doGet(getStoreByIdUrl(id), null, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        // Return the only JStore in the response as ExternalKey/Id of the store will by unique and
        // returns only single Store entity
        return parseResponse(response).get(0);
    }

    /**
     * Get Store By ExternalKey
     *
     * @return JStore
     */
    public JStore getStoreByExternalKey(final String externalKey) {

        final Map<String, String> params = new HashMap<>();
        params.put(Parameter.EXTKEY.getName(), externalKey);

        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        // Return the only JStore in the response as ExternalKey/Id of the store will by unique and
        // returns only single Store entity
        return parseResponse(response).get(0);
    }

    /**
     * Get Store By Country
     *
     * @return List<JStore>
     */
    public List<JStore> getStoresByCountry(final String country, String type) {

        final Map<String, String> params = new HashMap<>();
        params.put(Parameter.COUNTRY.getName(), country);
        if (type.isEmpty()) {
            type = "None";
        }
        params.put(Parameter.TYPE.getName(), type);

        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        return parseResponse(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private String getStoreByIdUrl(final String id) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + id;
    }

    /**
     * Store response has data, included and errors array Data contains list of Stores Included has the supporting info:
     * Store, ShippingMethods, and priceList.
     *
     * @return Stores
     */
    private List<JStore> parseResponse(final CloseableHttpResponse response) {

        JStore store = new JStore();
        final Included included = new Included();
        final List<ShippingMethod> shippingMethods = new ArrayList<>();
        final List<PriceList> priceLists = new ArrayList<>();
        final ArrayList<JStore> storeArray = new ArrayList<>();
        final List<PaymentMethod> paymentMethods = new ArrayList<>();
        final int status = response.getStatusLine().getStatusCode();

        try {
            final Gson gson = new GsonBuilder().create();

            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);

            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);
                // Parse error
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                store.setErrors(errors[0]);
                storeArray.add(store);
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final JStore storeObj = gson.fromJson(jsonObj, JStore.class);
                            storeArray.add(storeObj);
                        }
                    } else {
                        store = gson.fromJson(jsonObject.getAsJsonObject("data").toString(), JStore.class);
                        storeArray.add(store);
                    }
                }

                // Parse included
                final JsonArray includedArray = jsonObject.getAsJsonArray("included");
                final int count = includedArray.size();
                for (int i = 0; i < count; i++) {
                    final String includedResponse = includedArray.get(i).toString();
                    final JsonApi jsonApi = gson.fromJson(includedResponse, JsonApi.class);
                    if (jsonApi.getType().equals(EntityType.SHIPPING_METHOD)) {
                        final ShippingMethod shippingMethod = gson.fromJson(includedResponse, ShippingMethod.class);
                        shippingMethods.add(shippingMethod);
                        included.setShippingMethods(shippingMethods);
                    } else if (jsonApi.getType().equals(EntityType.PRICE_LIST)) {
                        final PriceList pricelist = gson.fromJson(includedResponse, PriceList.class);
                        priceLists.add(pricelist);
                    } else if (jsonApi.getType().equals(EntityType.PAYMENT_METHOD)) {
                        final PaymentMethod paymentMethod = gson.fromJson(includedResponse, PaymentMethod.class);
                        paymentMethods.add(paymentMethod);
                    } else {
                        throw new RuntimeException(
                            "Parse Offerings: Unknown entity type:" + jsonApi.getType().toString());
                    }
                }

                included.setPriceLists(priceLists);
                included.setShippingMethods(shippingMethods);
                store.setIncluded(included);
                storeArray.add(store);
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
        return storeArray;
    }
}
