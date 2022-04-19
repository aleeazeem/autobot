package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.item.Items;
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
 * Resource class to get Items
 *
 * @author Shweta Hegde
 */
public class ItemsClient {

    private static final String END_POINT = "items";
    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsClient.class.getSimpleName());

    /**
     * Enum for Parameter
     */
    public enum Parameter {

        APPLICATION_ID("appId"),
        TYPE_ID("typeId"),
        NAME("name"),
        SKU("sku"),
        START_INDEX("fr.startIndex"),
        BLOCK_SIZE("fr.blockSize"),
        SKIP_COUNT("fr.skipCount");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Constructor for ItemsResource
     *
     * @param appFamily TODO
     */
    public ItemsClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Get Items by Parameter
     *
     * @return Items or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItems(final Map<String, String> params) {
        final CloseableHttpResponse response = client.doGet(getUrl(), params, authInfo, null, null);
        return getPojo(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    @SuppressWarnings("unchecked")
    private <T extends PelicanPojo> T getPojo(final CloseableHttpResponse response) {
        T pojo;
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            LOGGER.error("Bad request with status code = " + status);
            pojo = (T) RestClientUtils.parseErrorResponse(response);
        } else {
            pojo = (T) parseResponse(response);
        }
        return pojo;
    }

    private Items parseResponse(final CloseableHttpResponse response) {

        Items items = null;
        try {
            final HttpEntity entity = response.getEntity();
            final String httpResponse = EntityUtils.toString(entity);
            LOGGER.info("Response : " + httpResponse);
            final JAXBContext jaxbContext = JAXBContext.newInstance(Items.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            items = (Items) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(httpResponse)));
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return items;
    }
}
