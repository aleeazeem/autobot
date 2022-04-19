package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfiles;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * This is a Resource class for Payment Profiles Api Xml. This class has methods to parse the GET payment profiles api
 * response.
 */
public class PaymentProfilesClient {

    private static final String END_POINT = "paymentProfiles";
    private EnvironmentVariables environmentVariables;
    private HttpRestClient client = new HttpRestClient();
    private AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProfilesClient.class.getSimpleName());

    /*
     * Set up the environment
     */
    public PaymentProfilesClient(final EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, environmentVariables.getAppFamily());
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    /**
     * This method would return the PaymentProfiles associated to the User. Here is the reference to API Doc :
     * https://adsk-pelican-dev-lb-api-899156841.us-west-1.elb.amazonaws.com/tfel2rs/doc/v2/ paymentProfiles/get.jsp
     *
     */
    public <T extends PelicanPojo> T getPaymentProfiles(final Map<String, String> params) {

        T pojo;
        LOGGER.info("Get Payment Profiles: ");
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);

        final int status = response.getStatusLine().getStatusCode();

        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_CREATED) {
            LOGGER.error("Get Payment Profile for User, Failed. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }

        return pojo;
    }

    private PaymentProfiles parseResponse(final CloseableHttpResponse response) {

        PaymentProfiles paymentProfiles = null;
        try {
            final HttpEntity entity = response.getEntity();
            String result = "";

            try {
                result = EntityUtils.toString(entity);
                LOGGER.info("Response Body: " + result);
            } catch (UnsupportedOperationException | IOException e) {
                e.printStackTrace();
            }

            final JAXBContext jaxbContext = JAXBContext.newInstance(PaymentProfiles.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            paymentProfiles = (PaymentProfiles) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(result)));

        } catch (IllegalStateException | JAXBException e) {
            e.printStackTrace();
        }

        return paymentProfiles;
    }
}
