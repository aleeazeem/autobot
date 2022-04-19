package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
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

/*
 * This class is used to add a product line and also to parse the response returned after hitting the add product line
 * api
 */
public class ProductLineClient {
    private static final String END_POINT = "productlines";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLineClient.class.getSimpleName());

    public ProductLineClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add a PriceList
     *
     * @return Pelican Pojo
     */
    public <T extends PelicanPojo> T addProductLine(final ProductLine productLine) {

        T pojo;
        LOGGER.info("Add Product Line");
        final String body = toJSON(productLine);
        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo, CONTENT_TYPE, CONTENT_TYPE);
        // parse the response
        pojo = (T) parseResponse(response);
        return pojo;
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private String toJSON(final ProductLine productLine) {

        final Gson gson = new Gson();
        // convert java object to JSON format,
        // and returned as JSON formatted string
        return gson.toJson(productLine);
    }

    private ProductLine parseResponse(final CloseableHttpResponse response) {

        final ProductLine productLine = new ProductLine();
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
                productLine.setErrors(errors[0]);
            } else {
                LOGGER.info("Parsing the correct response");
                // Parse data - if there's an error, there's no data in response
                if (!jsonObject.get("data").isJsonNull()) {
                    if (jsonObject.get("data").isJsonArray()) {
                        final JsonArray jsonArray = jsonObject.getAsJsonArray("data");
                        for (final JsonElement jsonObj : jsonArray) {
                            final ProductLine productLineObj = gson.fromJson(jsonObj, ProductLine.class);
                        }
                    } else {
                        final ProductLineData data =
                            gson.fromJson(jsonObject.getAsJsonObject("data").toString(), ProductLineData.class);
                        productLine.setData(data);
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
        return productLine;
    }
}
