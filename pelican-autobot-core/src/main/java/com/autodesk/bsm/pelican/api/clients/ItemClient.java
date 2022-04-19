package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.AuthenticationInfo;
import com.autodesk.bsm.pelican.util.HttpRestClient;
import com.autodesk.bsm.pelican.util.RestClientUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Resource class to process Item
 *
 * @author Shweta Hegde
 */
public class ItemClient {

    private static final String END_POINT = "item";

    private final EnvironmentVariables environmentVariables;
    private final HttpRestClient client = new HttpRestClient();
    private final AuthenticationInfo authInfo;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemClient.class.getSimpleName());

    /**
     * Constructor for ItemResource
     *
     * @param appFamily TODO
     */
    public ItemClient(final EnvironmentVariables environmentVariables, final String appFamily) {
        this.environmentVariables = environmentVariables;
        authInfo = new AuthenticationInfo(environmentVariables, appFamily);
    }

    /**
     * Enum for Item Parameter
     */
    public enum ItemParameter {
        NAME("name"),
        APPLICATION_ID("appId"),
        OWNER_ID("ownerId"),
        ITEMTYPE_ID("itemTypeId"),
        EXTERNAL_KEY("externalKey"),
        SKU("sku"),
        SKU_EXTENSION("skuextension");

        private String name;

        ItemParameter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Get item by item id
     *
     * @return Item
     */
    public <T extends PelicanPojo> T getItem(final String id) {

        final CloseableHttpResponse response = client.doGet(getUrl() + "/" + id, null, authInfo, null, null);
        return getPojo(response);
    }

    /**
     * Get item by item external key.
     *
     * @return Item
     */
    public <T extends PelicanPojo> T getItemByExternalKey(final String externalKey, final String appId) {

        final CloseableHttpResponse response = client.doGet(getUrl() + "/?" + ItemParameter.APPLICATION_ID.getName()
            + "=" + appId + "&" + ItemParameter.EXTERNAL_KEY.getName() + "=" + externalKey, null, authInfo, null, null);
        return getPojo(response);
    }

    private String getUrl() {
        return environmentVariables.getV2ApiUrl() + "/" + END_POINT;
    }

    @SuppressWarnings("unchecked")
    public <T extends PelicanPojo> T addItem(final HashMap<String, String> paramMap) {

        LOGGER.info("Add item: " + paramMap.get(ItemParameter.NAME.getName()));

        LOGGER.info("Post body before encoding: " + paramMap);
        final String body = RestClientUtils.urlEncode(paramMap);

        final CloseableHttpResponse response =
            client.doPost(getUrl(), body, authInfo, MediaType.APPLICATION_FORM_URLENCODED_VALUE, null);
        final int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_CREATED) {
            LOGGER.error("Bad request with status code = " + status);
            return (T) RestClientUtils.parseErrorResponse(response);
        } else {
            return (T) parseResponse(response);
        }
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

    private Item parseResponse(final CloseableHttpResponse response) {

        Item item = null;
        try {
            final HttpEntity entity = response.getEntity();
            final InputStream inputStream = entity.getContent();
            final JAXBContext jaxbContext = JAXBContext.newInstance(Item.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            item = (Item) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (IllegalStateException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return item;
    }

}
