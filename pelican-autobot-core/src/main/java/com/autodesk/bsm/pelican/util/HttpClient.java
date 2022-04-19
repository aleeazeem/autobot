package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.PelicanConstants;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderGroup;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class HttpClient {

    private static final String HMAC_SIGNATURE_CUSTOM_HEADER_NAME = "X-E2-HMAC-Signature";
    private static final String HMAC_TIMESTAMP_CUSTOM_HEADER_NAME = "X-E2-HMAC-Timestamp";
    private static final String HMAC_APP_FAMILY_CUSTOM_HEADER_NAME = "X-E2-AppFamilyId";
    private static final String HMAC_PARTNER_ID_CUSTOM_HEADER_NAME = "X-E2-PartnerId";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class.getSimpleName());

    public static int doDelete(final String url, final String contentType, final AuthenticationInfo authInfo,
        final String requestBody, final HttpResponseListener listener) throws IOException {

        final org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        final HeaderGroup headers = new HeaderGroup();

        // generate timeStamp and Signature
        authInfo.setTimestamp(DateTimeUtils.getCurrentTimeStamp());
        authInfo.setSignature(RestClientUtils.getHMACSignatureValue(authInfo));

        headers.addHeader(new Header(HMAC_PARTNER_ID_CUSTOM_HEADER_NAME, authInfo.getPartnerId()));
        headers.addHeader(new Header(HMAC_APP_FAMILY_CUSTOM_HEADER_NAME, String.valueOf(authInfo.getAppFamilyId())));
        headers.addHeader(new Header(ACCEPT, PelicanConstants.CONTENT_TYPE));
        headers.addHeader(new Header(CONTENT_TYPE, PelicanConstants.CONTENT_TYPE));
        headers.addHeader(new Header(HMAC_TIMESTAMP_CUSTOM_HEADER_NAME, authInfo.getTimestamp()));
        headers.addHeader(new Header(HMAC_SIGNATURE_CUSTOM_HEADER_NAME, authInfo.getSignature()));

        LOGGER.info("doDelete: " + url + "\nrequestBody:" + requestBody);

        HttpMethod method;
        if (StringUtils.isEmpty(requestBody)) {
            method = new DeleteMethod(url);
        } else {
            final HttpDeleteWithBody deleteMethod = new HttpDeleteWithBody(url);
            deleteMethod.setRequestEntity(new StringRequestEntity(requestBody, contentType, "UTF-8"));
            method = deleteMethod;
        }

        for (final Header h : headers.getAllHeaders()) {
            method.setRequestHeader(h);
        }

        int statusCode;
        try {
            statusCode = customExecute(client, method, listener);
        } finally {
            try {
                method.releaseConnection();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("statusCode: " + statusCode);
        return statusCode;
    }

    private static int customExecute(final org.apache.commons.httpclient.HttpClient client, final HttpMethod method,
        final HttpResponseListener listener) throws IOException {
        int statusCode;
        InputStream is = null;
        try {
            statusCode = client.executeMethod(method);
            final Header contentEncoding = method.getResponseHeader("Content-Encoding");
            is = method.getResponseBodyAsStream();

            if (listener != null) {
                listener.setStatusCode(method.getStatusCode());
                listener.setStatusText(method.getStatusText());

                final Header[] headers = method.getResponseHeaders();
                if (headers != null) {
                    for (final Header h : headers) {
                        listener.header(h.getName(), h.getValue());
                    }
                }

                if (method.getStatusCode() != 204) {
                    if (contentEncoding != null) {
                        if ("gzip".equalsIgnoreCase(contentEncoding.getValue())) {
                            listener.content(new GZIPInputStream(is));
                        } else {
                            listener.content(is);
                        }
                    } else {
                        listener.content(is);
                    }
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (final Exception e) {
                e.getMessage();
            }
        }

        return statusCode;
    }

    public static CloseableHttpResponse doGet(final CloseableHttpClient httpClient, final String url)
        throws IOException {

        final HttpGet request = new HttpGet(url);
        request.addHeader(PelicanConstants.ACCEPT, PelicanConstants.CONTENT_TYPE_XML);

        return httpClient.execute(request);

    }

}
