package com.autodesk.bsm.pelican.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;

public class HttpRestClient {

    private static final String HMAC_SIGNATURE_CUSTOM_HEADER_NAME = "X-E2-HMAC-Signature";
    private static final String HMAC_SIGNATURE_GDPR_HEADER_NAME = "x-adsk-signature";
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String HMAC_TIMESTAMP_CUSTOM_HEADER_NAME = "X-E2-HMAC-Timestamp";
    private static final String HMAC_APP_FAMILY_CUSTOM_HEADER_NAME = "X-E2-AppFamilyId";
    private static final String HMAC_PARTNER_ID_CUSTOM_HEADER_NAME = "X-E2-PartnerId";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRestClient.class.getSimpleName());

    /**
     * This method accepts get call with below params and send response
     *
     * @param url
     * @param params
     * @param authInfo
     * @param contentType
     * @param accept
     * @return CloseableHttpResponse
     */
    public CloseableHttpResponse doGet(final String url, final Map<String, String> params,
        final AuthenticationInfo authInfo, final String contentType, final String accept) {

        String requestUrl = url;
        if (params != null) {
            final String query = RestClientUtils.urlEncode(params);
            requestUrl = url + (!StringUtils.isEmpty(query) ? '?' + query : "");
        }

        final HttpClient client = getHttpClient();
        final HttpGet getRequest = new HttpGet(requestUrl);

        if (StringUtils.isNotEmpty(contentType)) {
            getRequest.addHeader(CONTENT_TYPE, contentType);
        }

        if (StringUtils.isNotEmpty(accept)) {
            getRequest.addHeader(ACCEPT, accept);
        }

        // generate timeStamp and Signature
        if (authInfo != null) {
            authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());
            authInfo.setRequestPath(getRequestPath(requestUrl));
            authInfo.setRequestBody(null);
            authInfo.setSignature(RestClientUtils.getHMACSignatureValue(authInfo));

            // Set request header
            getRequest.addHeader(HMAC_PARTNER_ID_CUSTOM_HEADER_NAME, authInfo.getPartnerId());
            getRequest.addHeader(HMAC_APP_FAMILY_CUSTOM_HEADER_NAME, String.valueOf(authInfo.getAppFamilyId()));
            getRequest.addHeader(HMAC_TIMESTAMP_CUSTOM_HEADER_NAME, authInfo.getTimestamp());
            getRequest.addHeader(HMAC_SIGNATURE_CUSTOM_HEADER_NAME, authInfo.getSignature());
        }

        // executing request
        CloseableHttpResponse response = null;
        try {
            LOGGER.info("URL = " + requestUrl);
            response = (CloseableHttpResponse) client.execute(getRequest);
        } catch (final Exception e) {
            LOGGER.error("Exception while execution the GET call: " + requestUrl);
            e.printStackTrace();
        }
        LOGGER.info("Response = " + response);

        // Adding extra line after RestAPI response for readability
        LOGGER.info("");
        return response;

    }

    /**
     * This method accepts body as a string and makes POST call
     *
     * @param url
     * @param body
     * @param authInfo
     * @param contentType
     * @param accept
     * @return
     */
    public CloseableHttpResponse doPost(final String url, final String body, final AuthenticationInfo authInfo,
        final String contentType, final String accept) {

        final HttpClient client = getHttpClient();
        final HttpPost postRequest = new HttpPost(url);

        if (StringUtils.isNotEmpty(accept)) {
            postRequest.addHeader(ACCEPT, accept);
        }

        if (StringUtils.isNotEmpty(contentType)) {
            postRequest.addHeader(CONTENT_TYPE, contentType);
        }

        CloseableHttpResponse response = null;
        try {

            LOGGER.info("URL = " + url);
            LOGGER.info("Post body: " + body);

            // generate timeStamp and Signature
            authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());
            authInfo.setRequestPath(getRequestPath(url));
            authInfo.setRequestBody(body);
            authInfo.setSignature(RestClientUtils.getHMACSignatureValue(authInfo));

            // Set request header
            postRequest.addHeader(HMAC_PARTNER_ID_CUSTOM_HEADER_NAME, authInfo.getPartnerId());
            postRequest.addHeader(HMAC_APP_FAMILY_CUSTOM_HEADER_NAME, String.valueOf(authInfo.getAppFamilyId()));
            postRequest.addHeader(HMAC_TIMESTAMP_CUSTOM_HEADER_NAME, authInfo.getTimestamp());
            postRequest.addHeader(HMAC_SIGNATURE_CUSTOM_HEADER_NAME, authInfo.getSignature());

            postRequest.setEntity(new StringEntity(body));
            response = (CloseableHttpResponse) client.execute(postRequest);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Response = " + response);

        // Adding extra line after RestAPI response for readability
        LOGGER.info("");
        return response;
    }

    /**
     * This is a method to make a post call with request body and authorization This is mainly for trigger jobs client
     *
     * @param url
     * @param body
     * @param contentType
     * @param accept
     * @param useBasicAuth
     * @param authorization
     * @return CloseableHttpResponse
     */
    public CloseableHttpResponse doPost(final String url, final String body, final String contentType,
        final String accept, final boolean useBasicAuth, final String authorization) {
        final HttpClient client = getHttpClient();
        final HttpPost postRequest = new HttpPost(url);

        if (useBasicAuth) {
            postRequest.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }

        if (StringUtils.isNotEmpty(contentType)) {
            postRequest.addHeader(CONTENT_TYPE, contentType);
        }

        if (StringUtils.isNotEmpty(accept)) {
            postRequest.addHeader(ACCEPT, contentType);
        }

        CloseableHttpResponse response = null;
        try {

            LOGGER.info("URL = " + url);
            if (StringUtils.isNotEmpty(body)) {
                postRequest.setEntity(new StringEntity(body));
                LOGGER.info("BODY = " + new StringEntity(body));
            }
            response = (CloseableHttpResponse) client.execute(postRequest);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Response = " + response);
        // Adding extra line after RestAPI response for readability
        LOGGER.info("");
        return response;
    }

    /**
     * This method is specifically for GDPR Forge Post. Separate method is maintained because the way HMAC is generated
     * is different than other POST
     *
     * @param url
     * @param body
     * @param key
     * @return
     */
    public CloseableHttpResponse doGDPRPost(final String url, final String body, final String key) {

        final HttpClient client = getHttpClient();
        final HttpPost postRequest = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            LOGGER.info("URL = {}", url);
            LOGGER.info("Post body: {}", body);

            // Set request header
            postRequest.addHeader(HMAC_SIGNATURE_GDPR_HEADER_NAME,
                "sha1hash=" + getHMACSignatureValueUsingSHA1(body.replaceAll("^\"|\"$", "").trim(), key));
            postRequest.addHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

            postRequest.setEntity(new StringEntity(body));
            response = (CloseableHttpResponse) client.execute(postRequest);
        } catch (final IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            LOGGER.error("Error occurred while processing post request {}", e);
        }
        LOGGER.info("Response ={} ", response);
        // Adding extra line after RestAPI response for readability
        LOGGER.info("");

        return response;
    }

    /**
     * Http Client method for PUT request to accept String body, Accept and Content Type
     *
     * @param url
     * @param contentType
     * @param accept
     * @param body
     * @param authInfo
     * @return
     */
    public CloseableHttpResponse doPut(final String url, final String contentType, final String accept,
        final String body, final AuthenticationInfo authInfo) {

        final HttpClient client = getHttpClient();
        final HttpPut putRequest = new HttpPut(url);

        // generate timeStamp and Signature
        authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());
        authInfo.setRequestPath(getRequestPath(url));
        authInfo.setRequestBody(body);
        authInfo.setSignature(RestClientUtils.getHMACSignatureValue(authInfo));

        // Set request header
        putRequest.addHeader(HMAC_PARTNER_ID_CUSTOM_HEADER_NAME, authInfo.getPartnerId());
        putRequest.addHeader(HMAC_APP_FAMILY_CUSTOM_HEADER_NAME, String.valueOf(authInfo.getAppFamilyId()));
        putRequest.addHeader(HMAC_TIMESTAMP_CUSTOM_HEADER_NAME, authInfo.getTimestamp());
        putRequest.addHeader(HMAC_SIGNATURE_CUSTOM_HEADER_NAME, authInfo.getSignature());
        if (StringUtils.isNotEmpty(accept)) {
            putRequest.addHeader(ACCEPT, accept);
        }
        if (StringUtils.isNotEmpty(contentType)) {
            putRequest.addHeader(CONTENT_TYPE, contentType);
        }

        CloseableHttpResponse response = null;
        try {

            LOGGER.info("URL = " + url);
            LOGGER.info("Put body: " + body);

            putRequest.setEntity(new StringEntity(body));
            response = (CloseableHttpResponse) client.execute(putRequest);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Response = " + response);
        // Adding extra line after RestAPI response for readability
        LOGGER.info("");
        return response;
    }

    /**
     * This method is to update the entity by calling patch.
     *
     * @param url
     * @param contentType
     * @param accept
     * @param body
     * @param authInfo
     * @return
     */
    public CloseableHttpResponse doPatch(final String url, final String contentType, final String accept,
        final String body, final AuthenticationInfo authInfo) {

        final HttpClient client = getHttpClient();
        final HttpPatch patchRequest = new HttpPatch(url);

        // generate timeStamp and Signature
        authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());
        authInfo.setRequestPath(getRequestPath(url));
        authInfo.setRequestBody(body);
        authInfo.setSignature(RestClientUtils.getHMACSignatureValue(authInfo));

        // Set request header
        patchRequest.addHeader(HMAC_PARTNER_ID_CUSTOM_HEADER_NAME, authInfo.getPartnerId());
        patchRequest.addHeader(HMAC_APP_FAMILY_CUSTOM_HEADER_NAME, String.valueOf(authInfo.getAppFamilyId()));
        patchRequest.addHeader(HMAC_TIMESTAMP_CUSTOM_HEADER_NAME, authInfo.getTimestamp());
        patchRequest.addHeader(HMAC_SIGNATURE_CUSTOM_HEADER_NAME, authInfo.getSignature());
        if (StringUtils.isNotEmpty(accept)) {
            patchRequest.addHeader(ACCEPT, accept);
        }
        if (StringUtils.isNotEmpty(contentType)) {
            patchRequest.addHeader(CONTENT_TYPE, contentType);
        }

        CloseableHttpResponse response = null;
        try {
            LOGGER.info("URL = " + url);
            LOGGER.info("Patch body: " + body);

            patchRequest.setEntity(new StringEntity(body));
            response = (CloseableHttpResponse) client.execute(patchRequest);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Response = " + response);
        LOGGER.info("");
        return response;
    }

    /**
     * This method is to execute DELETE call
     *
     * @param url
     * @param authInfo
     * @return
     */
    public CloseableHttpResponse doDelete(final String url, final AuthenticationInfo authInfo) {

        final HttpClient client = getHttpClient();
        final HttpDelete deleteRequest = new HttpDelete(url);

        // generate timeStamp and Signature
        authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());
        authInfo.setRequestPath(getRequestPath(url));
        authInfo.setRequestBody(null);
        authInfo.setSignature(RestClientUtils.getHMACSignatureValue(authInfo));

        // Set request header
        deleteRequest.addHeader(HMAC_PARTNER_ID_CUSTOM_HEADER_NAME, authInfo.getPartnerId());
        deleteRequest.addHeader(HMAC_APP_FAMILY_CUSTOM_HEADER_NAME, String.valueOf(authInfo.getAppFamilyId()));
        deleteRequest.addHeader(HMAC_TIMESTAMP_CUSTOM_HEADER_NAME, authInfo.getTimestamp());
        deleteRequest.addHeader(HMAC_SIGNATURE_CUSTOM_HEADER_NAME, authInfo.getSignature());
        deleteRequest.addHeader(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        deleteRequest.addHeader(ACCEPT, MediaType.APPLICATION_XML);

        CloseableHttpResponse response = null;
        try {
            LOGGER.info("URL = " + url);
            response = (CloseableHttpResponse) client.execute(deleteRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                LOGGER.error("Bad request: " + response.getStatusLine().getReasonPhrase());
                response.close();
                response = null;
            }
        } catch (final Exception e) {
            LOGGER.error("Exception while execution the DELETE call: " + url);
            e.printStackTrace();
        }
        return response;

    }

    private HttpClient getHttpClient() {
        HttpClient client = null;
        try {
            client = HttpClients.custom().setHostnameVerifier(new AllowAllHostnameVerifier())
                .setSslcontext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(final X509Certificate[] arg0, final String arg1) {
                        return true;
                    }
                }).build()).build();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return client;
    }

    private static String getHMACSignatureValueUsingSHA1(final String requestBody, final String key)
        throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac mac = Mac.getInstance(HMAC_SHA1);
        final SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
        mac.init(keySpec);
        return Hex.encodeHexString(mac.doFinal(requestBody.getBytes()));
    }

    /**
     * Method to return Request Path from URL
     *
     * @param url
     * @return String
     */
    private static String getRequestPath(final String url) {
        String requestPath = null;
        try {
            final URI uri = new URI(url);
            if (StringUtils.contains(url, ".com")) {
                requestPath = url.split("\\.com")[1];
            } else {
                requestPath = url.split(Integer.toString(uri.getPort()))[1];
            }
        } catch (final URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return requestPath;
    }
}
