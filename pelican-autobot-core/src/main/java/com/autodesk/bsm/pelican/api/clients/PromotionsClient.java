package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Promotions;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for Promotions. This class provides the methods which actually invoke the getPromotions API and returns the
 * parsed response.
 *
 * @author t_mohag
 */
public class PromotionsClient {

    private static final String END_POINT = "promotions";
    private static final String CONTENT_TYPE = "application/vnd.api+json";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionsClient.class.getSimpleName());

    public enum Parameter {
        IDS("filter[ids]"),
        CODES("filter[codes]"),
        STATES("filter[states]");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    public PromotionsClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get promotions by ids
     *
     * @param ids : pass list of id
     * @param states : Pass list of state
     *
     * @return Promotions or HttpError
     */
    public <T extends PelicanPojo> T getPromotionsByIds(final List<String> ids, final List<String> states) {
        final Map<String, String> params = new HashMap<>();
        if (ids != null) {
            params.put(Parameter.IDS.getName(), Util.getString(ids));
        }
        if (states != null) {
            params.put(Parameter.STATES.getName(), Util.getString(states));
        }
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, CONTENT_TYPE, null);
        // Return the promotions.
        return parseResponse(response);
    }

    /**
     * Get promotions by codes
     *
     * @param states TODO
     *
     * @return Promotions or HttpError
     */
    public <T extends PelicanPojo> T getPromotionsByCodes(final List<String> codes, final List<String> states) {
        final Map<String, String> params = new HashMap<>();
        if (codes != null) {
            params.put(Parameter.CODES.getName(), Util.getString(codes));
        }
        if (states != null) {
            params.put(Parameter.STATES.getName(), Util.getString(states));
        }
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, CONTENT_TYPE, null);
        // Return the promotions.
        return parseResponse(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * Promotions response has data and errors array.
     *
     * @return Promotions or HttpError
     */
    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T parseResponse(final CloseableHttpResponse response) {
        T pojo = null;
        final int status = response.getStatusLine().getStatusCode();

        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = " + status);
            final HttpError httpError = new HttpError();
            httpError.setStatus(response.getStatusLine().getStatusCode());
            if (status == 400) {
                httpError.setErrorMessage("Bad Request");
            }
            pojo = (T) httpError;
        } else {
            try {
                final Gson gson = new GsonBuilder().create();
                final String content = EntityUtils.toString(response.getEntity());
                LOGGER.info("Content = " + content);
                pojo = (T) gson.fromJson(content, Promotions.class);
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    response.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pojo;
    }
}
