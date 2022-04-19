package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.pojos.HttpError;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class RestClientUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientUtils.class.getSimpleName());

    public static String urlEncode(final Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        final String ENC = "UTF-8";

        if (params != null) {
            for (final Map.Entry<String, String> param : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                try {
                    sb.append(URLEncoder.encode(param.getKey(), ENC)).append('=');

                    String value = param.getValue();
                    if (null == value) {
                        value = "";
                    }

                    sb.append(URLEncoder.encode(value, ENC));
                } catch (final UnsupportedEncodingException e) {
                    LOGGER.error("unsupported encoding: " + e);
                    throw new AssertionError();
                }
            }
        }
        return sb.toString();
    }

    public static HttpError parseErrorResponse(final CloseableHttpResponse response) {
        HttpError errorResponse = null;
        try {
            final int status = response.getStatusLine().getStatusCode();
            final String reason = response.getStatusLine().getReasonPhrase();
            if (status == HttpStatus.SC_NO_CONTENT) {
                errorResponse = new HttpError();
            } else {
                final HttpEntity entity = response.getEntity();
                final String result = EntityUtils.toString(entity);
                final JAXBContext jaxbContext = JAXBContext.newInstance(HttpError.class);
                final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                LOGGER.info("Response : " + result);
                errorResponse = (HttpError) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(result)));
            }
            errorResponse.setStatus(status);
            errorResponse.setReason(reason);

            response.close();
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }

        return errorResponse;
    }

    public static String getHMACSignatureValue(final AuthenticationInfo authInfo) {

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            final SecretKeySpec keySpec = new SecretKeySpec(authInfo.getSecretKey().getBytes(), "HmacSHA256");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        final StringBuilder message = new StringBuilder().append(authInfo.getPartnerId())
            .append(authInfo.getAppFamilyId()).append(authInfo.getTimestamp());

        if (!StringUtils.isEmpty(authInfo.getRequestPath())) {
            message.append(authInfo.getRequestPath());
        }
        if (!StringUtils.isEmpty(authInfo.getRequestBody())) {
            message.append(authInfo.getRequestBody());
        }

        byte[] signatureBytes = new byte[0];
        if (null != mac) {
            signatureBytes = mac.doFinal(message.toString().getBytes());
        }

        LOGGER.debug("Partnerid: " + authInfo.getPartnerId());
        LOGGER.debug("getTimestamp: " + authInfo.getTimestamp());
        LOGGER.debug("getAppFamilyId: " + authInfo.getAppFamilyId());
        LOGGER.debug("getRequestPath: " + authInfo.getRequestPath());
        LOGGER.debug("getRequestBody: " + authInfo.getRequestBody());
        LOGGER.debug("signatureBytes: " + Hex.encodeHexString(signatureBytes));
        return Hex.encodeHexString(signatureBytes);
    }
}
