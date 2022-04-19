package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.paymentprofile.PaymentProfile;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This is a pojo class for Add Payment Profile Api Xml. This class has methods to parse the add payment profile api
 * response.
 */
public class PaymentProfileClient {

    private static final String END_POINT = "paymentProfile";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProfileClient.class.getSimpleName());

    /*
     * Set up the environment
     */
    public PaymentProfileClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add payment profile
     *
     * @param paymentProfile
     * @return Payment profile object or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T add(final PaymentProfile paymentProfile) {

        T pojo;
        LOGGER.info("Add Payment Profile");
        final String body = toXml(paymentProfile);
        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo,
            PelicanConstants.CONTENT_TYPE_XML_UTF8, PelicanConstants.CONTENT_TYPE_XML);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_CREATED) {
            LOGGER.error("Unable to create a payment profile. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }

        return pojo;
    }

    /**
     * This is a client method for Update Payment Profile API.
     *
     * @param paymentProfile
     * @param paymentProfileId
     */
    public void update(final PaymentProfile paymentProfile, final String paymentProfileId) {

        LOGGER.info("Update Payment Profile");
        final String body = toXml(paymentProfile);
        final CloseableHttpResponse response = client.doPost(getUrl() + "/" + paymentProfileId, body, authInfo,
            PelicanConstants.CONTENT_TYPE_XML_UTF8, PelicanConstants.CONTENT_TYPE_XML);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Unable to update a payment profile. Got status code of " + status);
            RestClientUtils.parseErrorResponse(response);
        }
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private String toXml(final PaymentProfile paymentProfile) {

        final StringWriter body = new StringWriter();
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(PaymentProfile.class);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(paymentProfile, body);
        } catch (final JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return body.toString();
    }

    /**
     * This method would return the PaymentProfiles associated to the User.
     *
     */
    public <T extends PelicanPojo> T getPaymentProfile(final String paymentProfileId) {

        T pojo;
        final String url = getUrl() + "/" + paymentProfileId;
        LOGGER.info("Get Payment Profiles: ");
        final CloseableHttpResponse response = client.doGet(url, null, authInfo, null, null);

        final int status = response.getStatusLine().getStatusCode();

        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_CREATED) {
            LOGGER.error("Get Payment Profile for User, Failed. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }

        return pojo;
    }

    /**
     * Delete Payment Profile.
     *
     * @param paymentProfileId
     * @return
     */
    public boolean deletePaymentProfile(final String paymentProfileId) {

        boolean success = false;
        final String url = getUrl() + "/" + paymentProfileId;
        final CloseableHttpResponse response = client.doDelete(url, authInfo);

        if (response.getStatusLine().getStatusCode() == 200) {
            success = true;
        }
        return success;
    }

    private PaymentProfile parseResponse(final CloseableHttpResponse response) {

        PaymentProfile paymentProfile = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(PaymentProfile.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            paymentProfile = (PaymentProfile) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | JAXBException | IOException e) {
            LOGGER.error(e.getMessage());
        }

        return paymentProfile;
    }

}
