package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.basicoffering.Currency;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
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

import java.io.StringReader;
import java.util.LinkedHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class CurrencyClient {
    private static final String END_POINT = "currency/";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyClient.class.getSimpleName());

    public CurrencyClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add currency
     *
     * @return currency or HttpErrorInfo
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T add(final LinkedHashMap<String, String> requestBody) {

        T pojo = null;
        LOGGER.info("Add Currency");

        LOGGER.info("Post body before encoding: " + requestBody);
        final String body = RestClientUtils.urlEncode(requestBody);

        final CloseableHttpResponse response = client.doPost(getUrl(), body, authInfo,
            PelicanConstants.CONTENT_TYPE_URL_ENCODED, PelicanConstants.CONTENT_TYPE_XML);
        final int status = response.getStatusLine().getStatusCode();
        /*
         * @ Return HttpErrorResponse if status is not ok
         *
         * @ Return 200 ok if it the update is success Request Body is null if the update is success
         */
        if (status != HttpStatus.SC_CREATED) {
            LOGGER.error("Unable to add a currency. Got status code of " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        }

        return pojo;
    }

    /**
     * This method returns response from the Find Currency by id API Currency Id is accepted as an argument to retrieve
     * payload
     *
     * @return Currency
     */
    public Currency getById(final String currencyId) {
        final CloseableHttpResponse response = client.doGet(getUrl() + currencyId, null, authInfo, null, null);
        return parseResponse(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    private Currency parseResponse(final CloseableHttpResponse response) {

        Currency currency = null;
        try {
            final HttpEntity entity = response.getEntity();
            final String httpResponse = EntityUtils.toString(entity);
            LOGGER.info("Response : " + httpResponse);
            final JAXBContext jaxbContext = JAXBContext.newInstance(Currency.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            currency = (Currency) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(httpResponse)));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return currency;
    }
}
