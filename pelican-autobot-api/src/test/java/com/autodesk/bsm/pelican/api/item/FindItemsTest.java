package com.autodesk.bsm.pelican.api.item;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.api.clients.ItemClient.ItemParameter;
import com.autodesk.bsm.pelican.api.clients.ItemsClient.Parameter;
import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.api.pojos.item.Items;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Test Case : Get Items API
 *
 * @author Shweta Hegde
 */
public class FindItemsTest extends BaseTestData {

    private PelicanPlatform resource;
    private static final String NAME = "AUTO_";
    private static final int MAX_NAME_LENGTH = 10;
    private ItemType itemType;
    private Object apiResponse;
    private HashMap<String, String> params;
    private String itemName;
    private HttpError httpError;
    private Items items;
    private String appId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final Applications applications = resource.application().getApplications();
        appId = applications.getApplications().get(0).getId();
        final String itemTypeName =
            NAME.concat("ItemType_").concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        itemType = resource.itemType().addItemType(appId, itemTypeName);
        itemName = NAME.concat("Item_").concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        final String itemExternalKey = NAME.concat("Item_ExternalKey_")
            .concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        final HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(ItemParameter.NAME.getName(), itemName);
        paramMap.put(ItemParameter.APPLICATION_ID.getName(), appId);
        paramMap.put(ItemParameter.OWNER_ID.getName(), getBuyerUser().getId());
        paramMap.put(ItemParameter.ITEMTYPE_ID.getName(), itemType.getId());
        paramMap.put(ItemParameter.EXTERNAL_KEY.getName(), itemExternalKey);
        resource.item().addItem(paramMap);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        params = new HashMap<>();
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case
     *
     * @param result would contain the result of the test method
     */
    @AfterMethod(alwaysRun = true)
    public void endTestMethod(final ITestResult result) {

        apiResponse = null;
    }

    /**
     * This method is a smoke test for "Get Items" api Only application id is passed as Parameter for the api and
     * receive payloads of items
     */
    @Test
    public void getItems() {
        params.put(Parameter.APPLICATION_ID.getName(), appId);
        apiResponse = resource.items().getItems(params);
        if (apiResponse instanceof HttpError) {
            // if get items api returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Items.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to items
            items = (Items) apiResponse;
            AssertCollector.assertThat("Unable to find items", items.getItems(), is(notNullValue()),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests "Get Items" by id Application id & item type id passed as Parameters for the api and receive
     * payloads of items
     */
    @Test
    public void getItemByTypeId() {
        params.put(Parameter.APPLICATION_ID.getName(), appId);
        params.put(Parameter.TYPE_ID.getName(), itemType.getId());
        apiResponse = resource.items().getItems(params);

        if (apiResponse instanceof HttpError) {
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Items.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to items
            items = (Items) apiResponse;
            AssertCollector.assertThat("Unable to find items", items.getItems(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect item type id", items.getItems().get(0).getItemType().getId(),
                equalTo(itemType.getId()), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * This method tests "Get Items" by name Application id & item name passed as Parameters for the api and receive
     * payloads of items
     */
    @Test
    public void getItemByName() {
        params.put(Parameter.APPLICATION_ID.getName(), appId);
        params.put(Parameter.NAME.getName(), itemName);
        apiResponse = resource.items().getItems(params);
        if (apiResponse instanceof HttpError) {
            // if get items api returns HTTP Error
            httpError = (HttpError) apiResponse;
            AssertCollector.assertThat("Bad request with status of " + httpError.getStatus(), apiResponse,
                instanceOf(Items.class), assertionErrorList);
        } else {
            // type casting Object type apiResponse to items
            items = (Items) apiResponse;
            AssertCollector.assertThat("Unable to find items", items.getItems(), is(notNullValue()),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect item name", items.getItems().get(0).getName(), equalTo(itemName),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }
}
