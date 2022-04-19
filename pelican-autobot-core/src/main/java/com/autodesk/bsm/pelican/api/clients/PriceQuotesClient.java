package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes;
import com.autodesk.bsm.pelican.api.pojos.json.PriceQuotes.PriceQuoteData;
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
 * Endpoint for PriceQuotes. This class provides the method which invokes the getPriceQuotes API and returns the parsed
 * response
 *
 * @author t_mohag
 */
public class PriceQuotesClient {

    private static final String END_POINT = "priceQuotes";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private static final String ACCEPT = "application/vnd.api+json";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceQuotesClient.class.getSimpleName());

    public PriceQuotesClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get Price Quotes
     *
     * @return PriceQuotes
     */
    public PriceQuotes getPriceQuotes(final PriceQuotes priceQuotes) {
        final Gson gson = new GsonBuilder().create();
        final CloseableHttpResponse response =
            client.doPost(getUrl(), gson.toJson(priceQuotes), authInfo, CONTENT_TYPE, ACCEPT);
        return parsePriceQuoteResponse(gson, response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * Parse the response from get price quotes API request which contains data, included and errors
     *
     * @return PriceQuote
     */
    private PriceQuotes parsePriceQuoteResponse(final Gson gson, final CloseableHttpResponse response) {

        final PriceQuotes priceQuote = new PriceQuotes();
        PriceQuoteData priceQuoteData;
        final int status = response.getStatusLine().getStatusCode();

        try {
            final String content = EntityUtils.toString(response.getEntity());
            LOGGER.info("Content = " + content);
            final JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();

            if (status != HttpStatus.SC_OK) {
                LOGGER.error("Bad request with status code = " + status);
                // Parse error
                final JsonArray errorArray = jsonObject.getAsJsonArray("errors");
                final Errors[] errors = gson.fromJson(errorArray.toString(), Errors[].class);
                priceQuote.setErrors(Arrays.asList(errors));
            } else {
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    final JsonObject dataObj = jsonObject.getAsJsonObject("data");
                    priceQuoteData = gson.fromJson(dataObj.toString(), PriceQuoteData.class);
                    priceQuote.setData(priceQuoteData);
                }

                // Parse errors if present.
                if (!(jsonObject.get("errors") instanceof JsonNull)) {
                    final JsonArray errorsArray = jsonObject.getAsJsonArray("errors");
                    final Errors[] errors = gson.fromJson(errorsArray.toString(), Errors[].class);
                    priceQuote.setErrors(Arrays.asList(errors));
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
        return priceQuote;
    }
}
