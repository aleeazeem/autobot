package com.autodesk.bsm.pelican.api.itemtype;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.Applications;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.item.ItemType;
import com.autodesk.bsm.pelican.api.pojos.item.ItemTypes;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test methods to test item type APIs
 *
 * @author t_mohag
 */
public class ItemTypeTest extends BaseTestData {

    private PelicanPlatform resource;
    private String appId;
    private static final String NAME = "AUTO";
    private static final int MAX_NAME_LENGTH = 16;
    private static final int DEFAULT_BLOCK_SIZE = 10;

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final Applications applications = resource.application().getApplications();
        appId = applications.getApplications().get(0).getId();
    }

    /**
     * Add item type
     */
    @Test
    public void addItemTypeTest() {
        final String itemTypeName = NAME.concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        final ItemType itemType = resource.itemType().addItemType(appId, itemTypeName);
        AssertCollector.assertThat("Invalid App ID", itemType.getAppId(), equalTo(appId), assertionErrorList);
        AssertCollector.assertThat("Invalid item type ID", itemType.getId(), notNullValue(), assertionErrorList);
        AssertCollector.assertThat("Invalid item type external key", itemType.getExternalKey(), nullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid item type name", itemType.getName(), equalTo(itemTypeName),
            assertionErrorList);
        AssertCollector.assertThat("Invalid display left nav", itemType.isDisplayLeftNav(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find item type by id
     */
    @Test
    public void getItemTypeByIdTest() {
        final String itemTypeName = NAME.concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        final ItemType itemType = resource.itemType().addItemType(appId, itemTypeName);
        final ItemType itemTypeResult = resource.itemType().getItemTypeById(itemType.getId());
        AssertCollector.assertThat("Invalid App ID", itemTypeResult.getAppId(), equalTo(itemType.getAppId()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid item type ID", itemTypeResult.getId(), equalTo(itemType.getId()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid item type external key", itemTypeResult.getExternalKey(), notNullValue(),
            assertionErrorList);
        AssertCollector.assertThat("Invalid item type name", itemTypeResult.getName(), equalTo(itemType.getName()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid display left nav", itemTypeResult.isDisplayLeftNav(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Find item type by app id and external key
     */
    @Test
    public void getItemTypeByAppIdAndExternalKeyTest() {
        final String itemTypeName = NAME.concat(RandomStringUtils.randomAlphanumeric(MAX_NAME_LENGTH - NAME.length()));
        ItemType itemType = resource.itemType().addItemType(appId, itemTypeName);
        itemType = resource.itemType().getItemTypeById(itemType.getId());
        final ItemType itemTypeResult =
            resource.itemType().getItemTypeByAppIdAndExternalKey(itemType.getAppId(), itemType.getExternalKey());
        AssertCollector.assertThat("Invalid App ID", itemTypeResult.getAppId(), equalTo(itemType.getAppId()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid item type ID", itemTypeResult.getId(), equalTo(itemType.getId()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid item type external key", itemTypeResult.getExternalKey(),
            equalTo(itemType.getExternalKey()), assertionErrorList);
        AssertCollector.assertThat("Invalid item type name", itemTypeResult.getName(), equalTo(itemType.getName()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid display left nav", itemTypeResult.isDisplayLeftNav(), equalTo(false),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API works without providing any pagination parameters
     */
    @Test
    public void findItemtypesUsingDefaultParams() {
        final Object entity = resource.itemTypes().getItemTypesByAppId(appId);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(ItemTypes.class), assertionErrorList);
        } else {
            final ItemTypes itemTypes = (ItemTypes) entity;
            AssertCollector.assertThat("Unable to get itemTypes using default params. Are there any itemTypes?",
                itemTypes.getItemTypes(), is(notNullValue()), assertionErrorList);
            AssertCollector.assertThat("Invalid start index", itemTypes.getStartIndex(), equalTo(0),
                assertionErrorList);
            AssertCollector.assertThat("Invalid block size", itemTypes.getBlockSize(), equalTo(DEFAULT_BLOCK_SIZE),
                assertionErrorList);
            AssertCollector.assertThat("Total is not null", itemTypes.getTotal(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid number of itemTypes found", itemTypes.getItemTypes().size(),
                equalTo(DEFAULT_BLOCK_SIZE), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API works when only startIndex is provided.
     */
    @Test
    public void findItemTypesWithStartIndex() {
        final Object entity = resource.itemTypes().getItemTypesWithStartIndex(appId, 2);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(ItemTypes.class), assertionErrorList);
        } else {
            final ItemTypes itemTypes = (ItemTypes) entity;
            AssertCollector.assertThat("Invalid start index", itemTypes.getStartIndex(), equalTo(2),
                assertionErrorList);
            AssertCollector.assertThat("Invalid block size", itemTypes.getBlockSize(), equalTo(DEFAULT_BLOCK_SIZE),
                assertionErrorList);
            AssertCollector.assertThat("Total is not null", itemTypes.getTotal(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid number of itemTypes found", itemTypes.getItemTypes().size(),
                equalTo(DEFAULT_BLOCK_SIZE), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API works when only blockSize is provided
     */
    @Test
    public void findItemTypesWithBlockSize() {
        final Object entity = resource.itemTypes().getItemTypesWithBlockSize(appId, 5);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(ItemTypes.class), assertionErrorList);
        } else {
            final ItemTypes itemTypes = (ItemTypes) entity;
            AssertCollector.assertThat("Invalid start index", itemTypes.getStartIndex(), equalTo(0),
                assertionErrorList);
            AssertCollector.assertThat("Invalid block size", itemTypes.getBlockSize(), equalTo(5), assertionErrorList);
            AssertCollector.assertThat("Total is not null", itemTypes.getTotal(), nullValue(), assertionErrorList);
            AssertCollector.assertThat("Invalid number of itemTypes found", itemTypes.getItemTypes().size(), equalTo(5),
                assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API works with skipCount parameter value as false
     */
    @Test
    public void findItemTypesWithSkipCountFalse() {
        final Object entity = resource.itemTypes().getItemTypesWithSkipCount(appId, false);

        if (entity instanceof HttpError) {
            final HttpError error = (HttpError) entity;
            AssertCollector.assertThat("Bad request with status of " + error.getStatus(), entity,
                instanceOf(ItemTypes.class), assertionErrorList);
        } else {
            final ItemTypes itemTypes = (ItemTypes) entity;
            AssertCollector.assertThat("Invalid start index", itemTypes.getStartIndex(), equalTo(0),
                assertionErrorList);
            AssertCollector.assertThat("Invalid block size", itemTypes.getBlockSize(), equalTo(DEFAULT_BLOCK_SIZE),
                assertionErrorList);
            AssertCollector.assertThat("Total must not be null", itemTypes.getTotal(), notNullValue(),
                assertionErrorList);
            AssertCollector.assertThat("Invalid number of itemTypes found", itemTypes.getItemTypes().size(),
                equalTo(DEFAULT_BLOCK_SIZE), assertionErrorList);
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API handles negative startIndex properly.
     */
    @Test
    public void finditemTypesWithNegativeStartIndex() {
        final Object entity = resource.itemTypes().getItemTypesWithStartIndex(appId, -1);
        final HttpError error = (HttpError) entity;
        AssertCollector.assertThat("Invalid status", error.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error code", error.getErrorCode(), equalTo(990010), assertionErrorList);
        AssertCollector.assertThat("Unexpected message", error.getErrorMessage(),
            equalTo("Negative value (-1) passed to setFirstResult"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API handles negative blockSize properly.
     */
    @Test
    public void findItemTypesWithNegativeBlockSize() {
        final Object entity = resource.itemTypes().getItemTypesWithBlockSize(appId, -1);
        final HttpError error = (HttpError) entity;
        AssertCollector.assertThat("Invalid status", error.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error code ", error.getErrorCode(), equalTo(40026), assertionErrorList);
        AssertCollector.assertThat("Unexpected message", error.getErrorMessage(),
            equalTo("Block Size should be greater than zero. -1 is an invalid block size"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API handles huge blockSize parameter value properly
     */
    @Test
    public void findItemTypesWithLargeBlockSize() {
        final Object entity = resource.itemTypes().getItemTypesWithBlockSize(appId, 1000000);
        final HttpError error = (HttpError) entity;
        AssertCollector.assertThat("Invalid status", error.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error code ", error.getErrorCode(), equalTo(40025), assertionErrorList);
        AssertCollector.assertThat("Unexpected message", error.getErrorMessage(),
            equalTo("Block Size should not exceed 1000"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify that Find ItemTypes API handles blockSize parameter value as zero
     */
    @Test
    public void findItemTypesWithBlockSizeZero() {
        final Object entity = resource.itemTypes().getItemTypesWithBlockSize(appId, 0);
        final HttpError error = (HttpError) entity;
        AssertCollector.assertThat("Invalid status", error.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid error code ", error.getErrorCode(), equalTo(40026), assertionErrorList);
        AssertCollector.assertThat("Unexpected message", error.getErrorMessage(),
            equalTo("Block Size should be greater than zero. 0 is an invalid block size"), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }
}
