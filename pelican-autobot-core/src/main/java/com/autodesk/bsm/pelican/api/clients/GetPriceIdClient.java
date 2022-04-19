package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.GetPriceByPriceId;
import com.autodesk.bsm.pelican.api.pojos.json.GetPriceByPriceId.Included;
import com.autodesk.bsm.pelican.api.pojos.json.Price;
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
import java.util.Arrays;

/**
 * This class provides the methods which actually invoke the get price by price id API and returns the parsed response.
 *
 * @author Muhammad
 */
public class GetPriceIdClient {

    private static final String END_POINT = "price";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPriceIdClient.class.getSimpleName());

    /**
     * Constructor to set the same environment across tests
     *
     * @param appFamily TODO
     */
    public GetPriceIdClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get price by Id.
     *
     * @return price
     */
    public <T extends PelicanPojo> T getPriceById(final String id) {
        final CloseableHttpResponse response = client.doGet(getPriceByIdUrl(id), null, authInfo,
            PelicanConstants.CONTENT_TYPE, PelicanConstants.CONTENT_TYPE);
        return parseResponse(response);
    }

    private String getPriceByIdUrl(final String id) {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT + "/" + id;
    }

    /**
     * Get price by price id response has data and errors array.
     *
     * @return default store or HttpError
     */
    private <T extends PelicanPojo> T parseResponse(final CloseableHttpResponse response) {
        T pojo = null;
        final int status = response.getStatusLine().getStatusCode();
        final GetPriceByPriceId getPriceById = new GetPriceByPriceId();

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
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get(PelicanConstants.DATA).isJsonNull()) {
                    final Price data = new Price();
                    final JsonObject dataObject = jsonObject.getAsJsonObject(PelicanConstants.DATA);
                    data.setType(dataObject.getAsJsonPrimitive(PelicanConstants.TYPE).getAsString());
                    data.setId(dataObject.getAsJsonPrimitive(PelicanConstants.ID).getAsString());
                    data.setCurrency(dataObject.getAsJsonPrimitive(PelicanConstants.CUREENCY_CODE).getAsString());
                    data.setAmount(dataObject.getAsJsonPrimitive(PelicanConstants.AMOUNT).getAsString());
                    data.setStatus(dataObject.getAsJsonPrimitive(PelicanConstants.STATUS).getAsString());
                    data.setPricelistId(dataObject.getAsJsonPrimitive(PelicanConstants.PRICE_LIST_ID).getAsString());
                    data.setPriceListExternalKey(dataObject.getAsJsonPrimitive("priceListExternalKey").getAsString());
                    data.setStoreId(dataObject.getAsJsonPrimitive(PelicanConstants.STORE_ID).getAsString());
                    data.setStoreExternalKey(dataObject.getAsJsonPrimitive("storeExternalKey").getAsString());

                    getPriceById.setData(data);
                }
            }
            // Parse errors if present.
            if (!(jsonObject.get(PelicanConstants.ERRORS) instanceof JsonNull)) {
                final JsonArray errorsArray = jsonObject.getAsJsonArray(PelicanConstants.ERRORS);
                final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
                getPriceById.setErrors(Arrays.asList(errors));
            }

            final JsonArray includedArray = jsonObject.getAsJsonArray(PelicanConstants.INCLUDED);
            final Included[] included = gson.fromJson(includedArray.toString(), Included[].class);
            getPriceById.setIncluded(Arrays.asList(included));
            pojo = (T) getPriceById;
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
