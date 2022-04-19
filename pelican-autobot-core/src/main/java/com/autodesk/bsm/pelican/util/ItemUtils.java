package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.ItemClient.ItemParameter;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.item.Item;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import java.util.HashMap;

/**
 * This is a util class for item.
 *
 * @author yerragv.
 *
 */
public class ItemUtils {

    private PelicanPlatform resource;
    private final HashMap<String, String> paramMap = new HashMap<>();
    private Item item;

    public ItemUtils(final EnvironmentVariables environmentVariables) {

        resource = new PelicanClient(environmentVariables).platform();
        item = new Item();

    }

    /**
     * This method will make add item api call and create item.
     *
     * @param itemName
     * @param appId
     * @param buyerId
     * @param itemTypeId
     * @param itemExternalKey
     *
     * @return item
     */
    public Item addItem(final String itemName, final String appId, final String buyerId, final String itemTypeId,
        final String itemExternalKey) {

        paramMap.clear();
        paramMap.put(ItemParameter.NAME.getName(), itemName);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), buyerId);
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemTypeId);
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), itemExternalKey);
        item = resource.item().addItem(paramMap);

        return item;

    }
}
