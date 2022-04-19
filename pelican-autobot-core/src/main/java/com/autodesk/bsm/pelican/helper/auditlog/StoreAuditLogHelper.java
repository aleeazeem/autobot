package com.autodesk.bsm.pelican.helper.auditlog;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.PriceList;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.AuditLogEntry;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;
import com.autodesk.bsm.pelican.util.DynamoDBUtil;
import com.autodesk.bsm.pelican.util.Util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class serves as a common class for Dynamo DB query of Store and assertion
 *
 * @author t_joshv
 */
public class StoreAuditLogHelper {

    /*
     * This method does query Dynamo DB and validates the assertions for store
     *
     * @return true if finds the Audit Log, false otherwise.
     */
    public static boolean helperToValidateDynamoDbForStore(final String storeId, final String oldExternalKey,
        final String newExternalKey, final String oldStoreName, final String newStoreName, final String oldStoreTypeId,
        final String newStoreTypeId, final Status oldStatus, final Status newStatus, final String oldVatPercentage,
        final String newVatPercentage, final String oldSoldToCsn, final String newSoldToCsn,
        final List<AssertionError> assertionErrorList) {

        // Audit log verification
        final List<Map<String, AttributeValue>> items = DynamoDBUtil.store(storeId);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final List<AuditLogEntry> auditLogEntries = DynamoDBUtil.getAuditLogEntries(items);

        for (final AuditLogEntry auditLogEntry : auditLogEntries) {
            final Map<String, ChangeDetails> auditData = auditLogEntry.getChangeDetailsAsMap();

            if (auditLogEntry.getAction().equals(Action.CREATE.toString())) {

                // Assertion on id
                AssertCollector.assertThat("Incorrect id old value for Store : " + storeId,
                    auditData.get(PelicanConstants.ID).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect id new value for Store : " + storeId,
                    auditData.get(PelicanConstants.ID).getNewValue(), equalTo(storeId), assertionErrorList);

                // Assertion on external key
                AssertCollector.assertThat("Incorrect external key old value for Store : " + storeId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect external key new value for Store : " + storeId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                    assertionErrorList);

                // Assertion on Name
                AssertCollector.assertThat("Incorrect name old value for Store : " + storeId,
                    auditData.get(PelicanConstants.NAME).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store : " + storeId,
                    auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newStoreName), assertionErrorList);

                // Assertion on Store Type id
                AssertCollector.assertThat("Incorrect name old value for Store Type: " + storeId,
                    auditData.get(PelicanConstants.TYPE_ID).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store Type: " + storeId,
                    auditData.get(PelicanConstants.TYPE_ID).getNewValue(), equalTo(newStoreTypeId), assertionErrorList);

                // Assertion on Store Type
                AssertCollector.assertThat("Incorrect name old value for Store Status: " + storeId,
                    auditData.get(PelicanConstants.STATUS).getOldValue(), nullValue(), assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store Status: " + storeId,
                    auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus.toString()),
                    assertionErrorList);

                // Assertion on Vat Percentage
                if (newVatPercentage != null) {
                    AssertCollector.assertThat("Incorrect Vat Percentage old value for Store: " + storeId,
                        auditData.get(PelicanConstants.VAT_PERCENT).getOldValue(), nullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect Vat Percentage new value for Store: " + storeId,
                        auditData.get(PelicanConstants.VAT_PERCENT).getNewValue(), equalTo(newVatPercentage),
                        assertionErrorList);
                }

                // Assertion on Sold To Csn
                if (newSoldToCsn != null) {
                    AssertCollector.assertThat("Incorrect sold to csn old value for Store: " + storeId,
                        auditData.get(PelicanConstants.SOLD_TO_CSN).getOldValue(), nullValue(), assertionErrorList);
                    AssertCollector.assertThat("Incorrect sold to csn new value for Store: " + storeId,
                        auditData.get(PelicanConstants.SOLD_TO_CSN).getNewValue(), equalTo(newSoldToCsn),
                        assertionErrorList);
                }

                return true;
            } else if (auditLogEntry.getAction().equals(Action.UPDATE.toString())) {

                // Assertion on external key
                AssertCollector.assertThat("Incorrect external key old value for Store : " + storeId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect external key new value for Store : " + storeId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), equalTo(newExternalKey),
                    assertionErrorList);

                // Assertion on Name
                AssertCollector.assertThat("Incorrect name old value for Store : " + storeId,
                    auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldStoreName), assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store : " + storeId,
                    auditData.get(PelicanConstants.NAME).getNewValue(), equalTo(newStoreName), assertionErrorList);

                // Assertion on Store Status
                AssertCollector.assertThat("Incorrect name old value for Store Status: " + storeId,
                    auditData.get(PelicanConstants.STATUS).getOldValue(), equalTo(oldStatus.toString()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store Status: " + storeId,
                    auditData.get(PelicanConstants.STATUS).getNewValue(), equalTo(newStatus.toString()),
                    assertionErrorList);

                // Assertion on Vat Percentage
                if (oldVatPercentage != null && newVatPercentage != null) {
                    AssertCollector.assertThat("Incorrect Vat Percentage old value for Store: " + storeId,
                        auditData.get(PelicanConstants.VAT_PERCENT).getOldValue(), equalTo(oldVatPercentage),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect Vat Percentage new value for Store: " + storeId,
                        auditData.get(PelicanConstants.VAT_PERCENT).getNewValue(), equalTo(newVatPercentage),
                        assertionErrorList);
                }

                // Assertion on Sold To Csn
                if (oldSoldToCsn != null && newSoldToCsn != null) {
                    AssertCollector.assertThat("Incorrect sold to csn old value for Store: " + storeId,
                        auditData.get(PelicanConstants.SOLD_TO_CSN).getOldValue(), equalTo(oldSoldToCsn),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect sold to csn new value for Store: " + storeId,
                        auditData.get(PelicanConstants.SOLD_TO_CSN).getNewValue(), equalTo(newSoldToCsn),
                        assertionErrorList);
                }

                return true;
            } else if (auditLogEntry.getAction().equals(Action.DELETE.toString())) {
                // Assertion on id
                AssertCollector.assertThat("Incorrect id old value for Store : " + storeId,
                    auditData.get(PelicanConstants.ID).getOldValue(), equalTo(storeId), assertionErrorList);
                AssertCollector.assertThat("Incorrect id new value for Store : " + storeId,
                    auditData.get(PelicanConstants.ID).getNewValue(), nullValue(), assertionErrorList);

                // Assertion on external key
                AssertCollector.assertThat("Incorrect external key old value for Store : " + storeId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getOldValue(), equalTo(oldExternalKey),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect external key new value for Store : " + storeId,
                    auditData.get(PelicanConstants.EXTERNAL_KEY).getNewValue(), nullValue(), assertionErrorList);

                // Assertion on Name
                AssertCollector.assertThat("Incorrect name old value for Store : " + storeId,
                    auditData.get(PelicanConstants.NAME).getOldValue(), equalTo(oldStoreName), assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store : " + storeId,
                    auditData.get(PelicanConstants.NAME).getNewValue(), nullValue(), assertionErrorList);

                // Assertion on Store Type id
                AssertCollector.assertThat("Incorrect name old value for Store Type: " + storeId,
                    auditData.get(PelicanConstants.TYPE_ID).getOldValue(), equalTo(oldStoreTypeId), assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store Type: " + storeId,
                    auditData.get(PelicanConstants.TYPE_ID).getNewValue(), nullValue(), assertionErrorList);

                // Assertion on Store Type
                AssertCollector.assertThat("Incorrect name old value for Store Status: " + storeId,
                    auditData.get(PelicanConstants.STATUS).getOldValue(), equalTo(oldStatus.toString()),
                    assertionErrorList);
                AssertCollector.assertThat("Incorrect name new value for Store Status: " + storeId,
                    auditData.get(PelicanConstants.STATUS).getNewValue(), nullValue(), assertionErrorList);

                // Assertion on Vat Percentage
                if (oldVatPercentage != null) {
                    AssertCollector.assertThat("Incorrect Vat Percentage old value for Store: " + storeId,
                        auditData.get(PelicanConstants.VAT_PERCENT).getOldValue(), equalTo(oldVatPercentage),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect Vat Percentage new value for Store: " + storeId,
                        auditData.get(PelicanConstants.VAT_PERCENT).getNewValue(), nullValue(), assertionErrorList);
                }

                // Assertion on Sold To Csn
                if (oldSoldToCsn != null) {
                    AssertCollector.assertThat("Incorrect sold to csn old value for Store: " + storeId,
                        auditData.get(PelicanConstants.SOLD_TO_CSN).getOldValue(), equalTo(oldSoldToCsn),
                        assertionErrorList);
                    AssertCollector.assertThat("Incorrect sold to csn new value for Store: " + storeId,
                        auditData.get(PelicanConstants.SOLD_TO_CSN).getNewValue(), nullValue(), assertionErrorList);
                }

                return true;
            }
        }
        return false;

    }

    /**
     * Verifying Dynamo DB logs for Create Store Price List.
     *
     * @param assertionErrorList
     */
    public static void helperToValidateDynamoDbForCreatePriceListForStore(final String storeId,
        final List<PriceList> createdPriceList, final List<AssertionError> assertionErrorList) {

        for (final PriceList priceList : createdPriceList) {
            final List<AuditLogEntry> auditEntries = DynamoDBUtil.priceList(priceList.getId());
            AssertCollector.assertThat("There is only one entry in the DynamoDB table", auditEntries.size(), equalTo(1),
                assertionErrorList);
            final AuditLogEntry entry = auditEntries.get(0);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityId", entry.getEntityId(),
                equalTo(priceList.getId()), assertionErrorList);
            AssertCollector.assertThat("PriceList is associated with the correct store", entry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct action", entry.getAction(), equalTo("CREATE"),
                assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", entry.getEntityType(),
                equalTo("PriceList"), assertionErrorList);
            final Map<String, ChangeDetails> auditData = entry.getChangeDetailsAsMap();
            for (final String key : auditData.keySet()) {
                AssertCollector.assertThat("AuditData old values must be null", auditData.get(key).getOldValue(),
                    nullValue(), assertionErrorList);
                if ("id".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct PriceListId",
                        auditData.get(key).getNewValue(), equalTo(priceList.getId()), assertionErrorList);
                } else if ("name".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct Name", auditData.get(key).getNewValue(),
                        equalTo(priceList.getName()), assertionErrorList);
                } else if ("externalKey".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct ExternalKey",
                        auditData.get(key).getNewValue(), equalTo(priceList.getExternalKey()), assertionErrorList);
                } else if ("storeId".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct StoreId", auditData.get(key).getNewValue(),
                        equalTo(storeId), assertionErrorList);
                }
            }
        }
    }

    /**
     * Verifying Dynamo DB logs for Update Store Price List.
     *
     * @param assertionErrorList
     */
    public static void helperToValidateDynamoDbForUpdatedPriceListForStore(final String storeId,
        final List<PriceList> updatedPriceList, final List<AssertionError> assertionErrorList) {

        for (final PriceList priceList : updatedPriceList) {
            final List<AuditLogEntry> auditEntries = DynamoDBUtil.priceList(priceList.getId());
            AssertCollector.assertThat("There are 2 entries in the DynamoDB table", auditEntries.size(), equalTo(2),
                assertionErrorList);
            final AuditLogEntry createEntry = auditEntries.get(1);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityId", createEntry.getEntityId(),
                equalTo(priceList.getId()), assertionErrorList);
            AssertCollector.assertThat("PriceList is associated with the correct store", createEntry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct action", createEntry.getAction(),
                equalTo("CREATE"), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", createEntry.getEntityType(),
                equalTo("PriceList"), assertionErrorList);

            final AuditLogEntry updateEntry = auditEntries.get(0);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityId", updateEntry.getEntityId(),
                equalTo(priceList.getId()), assertionErrorList);
            AssertCollector.assertThat("PriceList is associated with the correct store", updateEntry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct action", updateEntry.getAction(),
                equalTo("UPDATE"), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", updateEntry.getEntityType(),
                equalTo("PriceList"), assertionErrorList);
            final Map<String, ChangeDetails> auditData = updateEntry.getChangeDetailsAsMap();
            for (final String key : auditData.keySet()) {
                AssertCollector.assertThat("AuditData new values must not be null", auditData.get(key).getNewValue(),
                    notNullValue(), assertionErrorList);
                AssertCollector.assertThat("AuditData old values must not be null", auditData.get(key).getOldValue(),
                    notNullValue(), assertionErrorList);
                if ("id".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct PriceListId",
                        auditData.get(key).getNewValue(), equalTo(priceList.getId()), assertionErrorList);
                } else if ("name".equals(key)) {
                    AssertCollector.assertThat("AuditData has the updated Name", auditData.get(key).getNewValue(),
                        equalTo(priceList.getName()), assertionErrorList);
                } else if ("externalKey".equals(key)) {
                    AssertCollector.assertThat("AuditData has the updated ExternalKey",
                        auditData.get(key).getNewValue(), equalTo(priceList.getExternalKey()), assertionErrorList);
                } else if ("storeId".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct StoreId", auditData.get(key).getNewValue(),
                        equalTo(storeId), assertionErrorList);
                }
            }
        }
    }

    /**
     * Verifying Dynamo DB logs for Create Store Country List.
     *
     * @param assertionErrorList
     */
    public static void helperToValidateDynamoDbForCreatedCountryForStore(final String storeId, final String timestamp,
        final List<AssertionError> assertionErrorList) {

        final List<AuditLogEntry> auditEntries = DynamoDBUtil.storeCountry(storeId, timestamp);

        for (final AuditLogEntry entry : auditEntries) {
            AssertCollector.assertThat("Store Country is associated with the correct store", entry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the CREATE action", entry.getAction(), equalTo("CREATE"),
                assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", entry.getEntityType(),
                equalTo("StoreCountry"), assertionErrorList);
            final Map<String, ChangeDetails> auditData = entry.getChangeDetailsAsMap();
            for (final String key : auditData.keySet()) {
                AssertCollector.assertThat("AuditData old values must be null", auditData.get(key).getOldValue(),
                    nullValue(), assertionErrorList);
                AssertCollector.assertThat("AuditData new values must not be null", auditData.get(key).getNewValue(),
                    notNullValue(), assertionErrorList);
                if ("storeId".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct storeId", auditData.get(key).getNewValue(),
                        equalTo(storeId), assertionErrorList);
                }
            }
        }

    }

    /**
     * Verifying Dynamo DB logs for Delete Store Price List.
     *
     * @param assertionErrorList
     */
    public static void helperToValidateDynamoDbForDeletedPriceListForStore(final String storeId,
        final List<PriceList> deletedPriceList, final List<AssertionError> assertionErrorList) {

        for (final PriceList priceList : deletedPriceList) {
            final List<AuditLogEntry> auditEntries = DynamoDBUtil.priceList(priceList.getId());

            AssertCollector.assertThat("There are 2 entries in the DynamoDB table", auditEntries.size(), equalTo(2),
                assertionErrorList);
            final AuditLogEntry createEntry = auditEntries.get(1);

            AssertCollector.assertThat("AuditLogEntry has the correct EntityId", createEntry.getEntityId(),
                equalTo(priceList.getId()), assertionErrorList);
            AssertCollector.assertThat("PriceList is associated with the correct store", createEntry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the CREATE action", createEntry.getAction(),
                equalTo("CREATE"), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", createEntry.getEntityType(),
                equalTo("PriceList"), assertionErrorList);
            final Map<String, ChangeDetails> auditData = createEntry.getChangeDetailsAsMap();
            for (final String key : auditData.keySet()) {
                AssertCollector.assertThat("AuditData old values must be null", auditData.get(key).getOldValue(),
                    nullValue(), assertionErrorList);
                if ("id".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct PriceListId",
                        auditData.get(key).getNewValue(), equalTo(priceList.getId()), assertionErrorList);
                } else if ("name".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct Name", auditData.get(key).getNewValue(),
                        equalTo(priceList.getName()), assertionErrorList);
                } else if ("externalKey".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct ExternalKey",
                        auditData.get(key).getNewValue(), equalTo(priceList.getExternalKey()), assertionErrorList);
                } else if ("storeId".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct StoreId", auditData.get(key).getNewValue(),
                        equalTo(storeId), assertionErrorList);
                }
            }

            final AuditLogEntry deleteEntry = auditEntries.get(0);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityId", deleteEntry.getEntityId(),
                equalTo(priceList.getId()), assertionErrorList);
            AssertCollector.assertThat("PriceList is associated with the correct store", deleteEntry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the DELETE action", deleteEntry.getAction(),
                equalTo("DELETE"), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", deleteEntry.getEntityType(),
                equalTo("PriceList"), assertionErrorList);
            final Map<String, ChangeDetails> auditDataForDelete = deleteEntry.getChangeDetailsAsMap();
            for (final String key : auditDataForDelete.keySet()) {
                AssertCollector.assertThat("AuditData new values must be null",
                    auditDataForDelete.get(key).getNewValue(), nullValue(), assertionErrorList);
                if ("id".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct PriceListId",
                        auditDataForDelete.get(key).getOldValue(), equalTo(priceList.getId()), assertionErrorList);
                } else if ("name".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct Name",
                        auditDataForDelete.get(key).getOldValue(), equalTo(priceList.getName()), assertionErrorList);
                } else if ("externalKey".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct ExternalKey",
                        auditDataForDelete.get(key).getOldValue(), equalTo(priceList.getExternalKey()),
                        assertionErrorList);
                } else if ("storeId".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct StoreId",
                        auditDataForDelete.get(key).getOldValue(), equalTo(storeId), assertionErrorList);
                }
            }
        }
    }

    /**
     * Verifying Dynamo DB logs for Delete Store Country List.
     *
     * @param assertionErrorList
     */
    public static void helperToValidateDynamoDbForDeletedCountryForStore(final String storeId, final String timestamp,
        final List<AssertionError> assertionErrorList) {

        final List<AuditLogEntry> auditEntries = DynamoDBUtil.storeCountry(storeId, timestamp);
        AssertCollector.assertThat("There are 4 entries in the DynamoDB table", auditEntries.size(), equalTo(6),
            assertionErrorList);
        final Set<Integer> idSet = new HashSet<>();
        final List<String> actions = Arrays.asList("CREATE", "DELETE");

        for (final AuditLogEntry entry : auditEntries) {
            idSet.add(Integer.valueOf(entry.getEntityId()));
            AssertCollector.assertThat("StoreCountry is associated with the correct store", entry.getStoreId(),
                equalTo(storeId), assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the CREATE action", entry.getAction(), isIn(actions),
                assertionErrorList);
            AssertCollector.assertThat("AuditLogEntry has the correct EntityType", entry.getEntityType(),
                equalTo("StoreCountry"), assertionErrorList);
            final Map<String, ChangeDetails> auditData = entry.getChangeDetailsAsMap();
            for (final String key : auditData.keySet()) {
                if (entry.getAction().equals("CREATE")) {
                    AssertCollector.assertThat("AuditData old values must be null", auditData.get(key).getOldValue(),
                        nullValue(), assertionErrorList);
                } else {
                    AssertCollector.assertThat("AuditData new values must be null", auditData.get(key).getNewValue(),
                        nullValue(), assertionErrorList);
                }
                if ("storeId".equals(key)) {
                    AssertCollector.assertThat("AuditData has the correct storeId",
                        auditData.get(key).getNewValue() != null ? auditData.get(key).getNewValue()
                            : auditData.get(key).getOldValue(),
                        equalTo(storeId), assertionErrorList);
                }
            }
        }
        AssertCollector.assertThat("There are only 3 unique EntityId(s) in the list", idSet.size(), equalTo(3),
            assertionErrorList);
    }

}
