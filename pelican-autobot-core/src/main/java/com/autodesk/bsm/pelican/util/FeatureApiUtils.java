package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.ItemClient.ItemParameter;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.apache.commons.lang.RandomStringUtils;

import java.util.HashMap;

public class FeatureApiUtils {

    private PelicanPlatform resource;
    private static final String NAME = "AUTO_";
    private static final int MAX_NAME_LENGTH = 10;
    private final EnvironmentVariables environmentVariables;

    public FeatureApiUtils(final EnvironmentVariables environmentVariables) {
        resource = new PelicanClient(environmentVariables).platform();
        this.environmentVariables = environmentVariables;
    }

    /**
     * This method creates an item/feature and returns Item object.
     *
     * @return Item
     */
    public Item addFeature(String name, String externalKey, String itemTypeId) {

        final Applications applications = resource.application().getApplications();
        String appId = applications.getApplications().get(0).getId();
        if (appId == null) {
            appId = environmentVariables.getAppId();
        }

        if (itemTypeId == null) {
            final ItemType itemType = addFeatureType(appId);
            itemTypeId = itemType.getId();
        }

        // add item/feature
        if (name == null) {
            name = NAME.concat("Item_").concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        }
        if (externalKey == null) {
            externalKey = NAME.concat("Item_ExternalKey_")
                .concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        }
        final HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(ItemParameter.NAME.getName(), name);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemTypeId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), environmentVariables.getUserId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), externalKey);
        return resource.item().addItem(paramMap);
    }

    private ItemType addFeatureType(final String appId) {

        final String itemTypeName =
            NAME.concat("ItemType_").concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        return resource.itemType().addItemType(appId, itemTypeName);
    }
}
