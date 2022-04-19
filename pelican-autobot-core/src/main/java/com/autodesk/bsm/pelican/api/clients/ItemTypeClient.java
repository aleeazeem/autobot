package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
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
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * Endpoint for itemType. This class provides the methods which actually invoke the addItemType and getItemType APIs and
 * returns the parsed response.
 *
 * @author t_mohag
 */
public class ItemTypeClient {

    private static final String END_POINT = "itemType";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemTypeClient.class.getSimpleName());

    public enum Parameter {
        NAME("name"),
        APPLICATION_ID("appId"),
        ID("id"),
        EXTERNAL_KEY("externalKey"),
        BLOCK_SIZE("fr.blockSize");

        private String name;

        Parameter(final String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    public ItemTypeClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Add item type
     *
     * @return item type
     */
    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T addItemType(final String appId, final String name) {
        final Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(Parameter.APPLICATION_ID.getName(), appId);
        bodyParams.put(Parameter.NAME.getName(), name);

        LOGGER.info("Add item type: " + name);

        LOGGER.info("Post body before encoding: " + bodyParams);
        final String body = RestClientUtils.urlEncode(bodyParams);

        final CloseableHttpResponse response =
            client.doPost(getUrl(), body, authInfo, MediaType.APPLICATION_FORM_URLENCODED_VALUE, null);
        return (T) parseResponse(response);
    }

    /**
     * Get item type by id
     *
     * @return itemType or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemTypeById(final String id) {
        final CloseableHttpResponse response = client.doGet(getUrl() + "/" + id, null, authInfo, null, null);
        return getPojo(response);
    }

    /**
     * Get item type by external key
     *
     * @return ItemType or HttpErrorInfo
     */
    public <T extends PelicanPojo> T getItemTypeByAppIdAndExternalKey(final String appId, final String externalKey) {
        final Map<String, String> body = new HashMap<>();
        body.put(Parameter.APPLICATION_ID.getName(), appId);
        body.put(Parameter.EXTERNAL_KEY.getName(), externalKey);
        final CloseableHttpResponse response = client.doGet(getUrl(), body, authInfo, null, null);
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

    private ItemType parseResponse(final CloseableHttpResponse response) {
        ItemType itemType = null;
        try {
            final HttpEntity entity = response.getEntity();
            final String httpResponse = EntityUtils.toString(entity);
            LOGGER.info("Http Response : " + httpResponse);
            final JAXBContext jaxbContext = JAXBContext.newInstance(ItemType.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            itemType = (ItemType) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(httpResponse)));
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return itemType;
    }
}
