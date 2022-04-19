package com.autodesk.bsm.pelican.util;

import com.autodesk.ism.pelican.client.CustomHttpResponse;
import com.autodesk.ism.pelican.client.config.RestClientConfig;
import com.autodesk.ism.pelican.client.config.RestHttpClient;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.http.MediaType;

import java.net.SocketTimeoutException;

public class ChangeNotificationAuthClient {

    private static RestHttpClient httpClient;
    private static RestClientConfig restConfig;
    private static String changeNotificationConsumerKey;
    private static String changeNotificationConsumerSecret;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeNotificationAuthClient.class.getSimpleName());

    public ChangeNotificationAuthClient(final RestClientConfig restConfig, final String changeNotificationConsumerKey,
        final String changeNotificationConsumerSecret) {
        LOGGER.info("$$$$$$$$$$$$$$$$$$ CSE AUTH $$$$$$$$$$$$$$$$$$$$");

        ChangeNotificationAuthClient.restConfig = restConfig;
        ChangeNotificationAuthClient.httpClient = new RestHttpClient(restConfig);
        ChangeNotificationAuthClient.changeNotificationConsumerKey = changeNotificationConsumerKey;
        ChangeNotificationAuthClient.changeNotificationConsumerSecret = changeNotificationConsumerSecret;
    }

    public ChangeNotificationAuthClient() {
        LOGGER.info("changeNotificationConsumerKey:" + changeNotificationConsumerKey
            + "\tchangeNotificationConsumerSecret" + changeNotificationConsumerSecret);
    }

    /**
     * Returns generated auth token by invoking the auth broker
     *
     * @return Auth token
     */
    public String getAuthToken() {
        final ChangeNotificationAuthResponse response = sendAuthRequest();
        if (response == null || !response.isSuccessful() || response.getResponse() == null
            || StringUtils.isEmpty(response.getResponse().getAccessToken())) {
            throw new RuntimeException("Unable to generate auth token to notify changes to CSE");
        }
        return response.getResponse().getAccessToken();
    }

    /**
     * Sends a Auth request to Autodesk developer API to get client credentials
     *
     * @return Auth Response
     */
    private ChangeNotificationAuthResponse sendAuthRequest() throws ListenerExecutionFailedException {
        final String payload =
            new StringBuilder("client_id=").append(ChangeNotificationAuthClient.changeNotificationConsumerKey)
                .append("&client_secret=").append(ChangeNotificationAuthClient.changeNotificationConsumerSecret)
                .append("&grant_type=client_credentials").toString();
        try {
            final CustomHttpResponse response =
                httpClient.post(restConfig.getBaseUri(), MediaType.APPLICATION_FORM_URLENCODED_VALUE, payload, "UTF-8");
            final int statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new ListenerExecutionFailedException(
                    "Got status code: " + statusCode + " from Authentication Server", null);
            }
            LOGGER.info("Sent auth request to: " + restConfig.getBaseUri() + " and got statusCode: " + statusCode);
            return new ChangeNotificationAuthResponse(response);
        } catch (final ListenerExecutionFailedException e) {
            throw e;
        } catch (final SocketTimeoutException e) {
            throw new ListenerExecutionFailedException("Authentication Server not responding, payload: " + payload, e);
        } catch (final Exception e) {
            throw new ListenerExecutionFailedException(
                "Error in commuting with Authentication Server, payload: " + payload, e);
        }
    }
}
