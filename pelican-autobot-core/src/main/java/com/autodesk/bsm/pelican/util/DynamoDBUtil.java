package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Util will perform query operations on Amazon DynamoDB
 *
 * @author mandas
 */
public final class DynamoDBUtil {

    private static EnvironmentVariables environmentVariables;
    private static String typeTimestampIndex = "EntityTypeTimestamp-index";
    private static DynamoDBMapper mapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBUtil.class.getSimpleName());

    private DynamoDBUtil() {}

    static {
        environmentVariables = new PelicanEnvironment().initializeEnvironmentVariables();
        // inject tabPle name from wherever, see annotation in AuditLogEntry
        // class
        final DynamoDBMapperConfig config = new DynamoDBMapperConfig(
            TableNameOverride.withTableNameReplacement(environmentVariables.getDynamoDBTable()));
        mapper = new DynamoDBMapper(createAWSDynamoDBClient(), config);
    }

    /**
     * This method creates a client by reads aws credentials from its own properties file, and connects to us west
     * region region
     *
     * @return AmazonDynamoDBClient : client on which we can perform query
     */
    public static AmazonDynamoDBClient createAWSDynamoDBClient() {

        final AmazonDynamoDBClient client =
            new AmazonDynamoDBClient(new PropertiesFileCredentialsProvider(Util.getTestRootDir()
                + "/src/test/resources/environments/awsDynamo_" + PelicanEnvironment.getEnvironment() + ".properties"));
        if (environmentVariables.getAWSRegion().contains(Regions.US_EAST_1.toString())) {
            client.setRegion(Region.getRegion(Regions.US_EAST_1));
        } else if (environmentVariables.getAWSRegion().contains(Regions.US_WEST_1.toString())) {
            client.setRegion(Region.getRegion(Regions.US_WEST_1));
        }

        return client;
    }

    /**
     * This method creates a client by reads aws credentials from its own properties file, and connects to us west
     * region region for subscription table in dynamo db.
     *
     * @return
     */
    public static AmazonDynamoDBClient createAWSDynamoDBSubscriptonTableClient() {
        String rootPath;
        if (PelicanEnvironment.getEnvironment().equals("wildflystg")) {
            rootPath = Util.getTestRootDir() + "/src/test/resources/environments/awsDynamo_subscription_services_"
                + PelicanEnvironment.getEnvironment() + ".properties";
        } else {
            rootPath = Util.getTestRootDir() + "/src/test/resources/environments/awsDynamo_"
                + PelicanEnvironment.getEnvironment() + ".properties";
        }
        final AmazonDynamoDBClient client = new AmazonDynamoDBClient(new PropertiesFileCredentialsProvider(rootPath));
        if (environmentVariables.getAWSRegion().contains(Regions.US_EAST_1.toString())) {
            client.setRegion(Region.getRegion(Regions.US_EAST_1));
        } else if (environmentVariables.getAWSRegion().contains(Regions.US_WEST_1.toString())) {
            client.setRegion(Region.getRegion(Regions.US_WEST_1));
        }

        return client;
    }

    /**
     * This method returns the audit log entries from the dynamo db
     */
    public static List<AuditLogEntry> getAuditLogEntries(final List<Map<String, AttributeValue>> items) {

        final List<AuditLogEntry> auditLogEntries = mapper.marshallIntoObjects(AuditLogEntry.class, items);
        for (final AuditLogEntry entry : auditLogEntries) {
            LOGGER.info(entry.toString());
        }

        return auditLogEntries;
    }

    /**
     * Core method which performs query on DB Index and return list Map of items(rows from table)
     *
     * @param EntityTypeEntityId : this value is combination of EntityType and EntityId
     * @return List Map of items to parse for validation
     */
    private static List<Map<String, AttributeValue>> getAuditlogItemAWSDynamodb(final String EntityTypeEntityId) {

        final HashMap<String, Condition> keyConditions = new HashMap<>();
        final AmazonDynamoDBClient client = createAWSDynamoDBClient();

        final String typeIdTimestampIndex = "EntityTypeEntityIdTimestamp-index";
        final QueryRequest queryRequest = new QueryRequest().withTableName(environmentVariables.getDynamoDBTable())
            .withIndexName(typeIdTimestampIndex).withScanIndexForward(false);

        keyConditions.put("EntityTypeEntityId", new Condition().withComparisonOperator(ComparisonOperator.EQ)
            .withAttributeValueList(new AttributeValue().withS(EntityTypeEntityId)));
        queryRequest.setKeyConditions(keyConditions);
        LOGGER.info("Query Request: " + queryRequest);
        final QueryResult result = client.query(queryRequest);
        LOGGER.info("Total Items Found for " + EntityTypeEntityId + " EntityTypeEntityId :" + result.getCount());

        return result.getItems();
    }

    /**
     * Core method which performs query on DB Index and return list Map of items(rows from table)
     *
     * @param id : this value is combination of EntityType and EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> getSubscriptionItemAWSDynamodb(final String id,
        final AmazonDynamoDBClient client) {

        final HashMap<String, Condition> keyConditions = new HashMap<>();
        final QueryRequest queryRequest =
            new QueryRequest().withTableName("subscription" + "-" + environmentVariables.getTableSuffix())
                .withIndexName("idIndex").withScanIndexForward(false);

        keyConditions.put("id", new Condition().withComparisonOperator(ComparisonOperator.EQ)
            .withAttributeValueList(new AttributeValue().withN(id)));
        queryRequest.setKeyConditions(keyConditions);
        LOGGER.info("Query Request: " + queryRequest);
        final QueryResult result = client.query(queryRequest);
        LOGGER.info("Total Items Found for " + id + " with partition key: " + result.getCount());

        return result.getItems();
    }

    /**
     * Method to get total number of records in a table by scanning the whole table in dyanmo db. Note: Don't use this
     * method unless you really need it.
     *
     * @param client
     * @return totalRecords
     */
    public static Long getTotalEnteries(final AmazonDynamoDBClient client, final String tableName) {
        ScanResult result = null;
        Long numberOfItems = 0L;
        try {
            do {
                final ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
                if (result != null) {
                    scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                }
                result = client.scan(scanRequest);
                numberOfItems += result.getItems().size();
            } while (result.getLastEvaluatedKey() != null);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("Total records found in table " + tableName + ": " + numberOfItems);
        return numberOfItems;
    }

    /**
     * Core method which performs query on DB Index and return list Map of items(rows from table)
     *
     * @param entityType : the corresponding EntityType of the result
     * @param storeId : parent store Id for this EntityType
     * @param timeStamp : current timestamp
     * @return List Map of items to parse for validation
     */
    private static List<Map<String, AttributeValue>> getEntityByStoreIdAuditlogItemAWSDynamodb(final String entityType,
        final String storeId, final String timeStamp) {

        final HashMap<String, Condition> keyConditions = new HashMap<>();
        final AmazonDynamoDBClient client = createAWSDynamoDBClient();

        final QueryRequest queryRequest =
            new QueryRequest().withTableName(environmentVariables.getDynamoDBTable()).withIndexName(typeTimestampIndex);

        keyConditions.put("EntityType", new Condition().withComparisonOperator(ComparisonOperator.EQ)
            .withAttributeValueList(new AttributeValue().withS(entityType)));
        keyConditions.put("AuditTimeStamp", new Condition().withComparisonOperator(ComparisonOperator.GE)
            .withAttributeValueList(new AttributeValue().withS(timeStamp)));
        queryRequest.setKeyConditions(keyConditions);
        queryRequest.addQueryFilterEntry("StoreId", new Condition().withComparisonOperator(ComparisonOperator.EQ)
            .withAttributeValueList(new AttributeValue().withS(storeId)));
        LOGGER.info("firing query request test cases:" + environmentVariables.getDynamoDBTable());
        final QueryResult result = client.query(queryRequest);
        LOGGER.info("Total Items Found for " + entityType + " for Store " + storeId + " : " + result.getCount());

        return result.getItems();
    }

    /**
     * Core method which performs query on DB Index and return list Map of items(rows from table)
     *
     * @param entityType : the corresponding EntityType of the result
     * @param promotionId : promotion Id for this EntityType
     * @param timeStamp : current timestamp
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> getEntityByPromotionIdAuditlogItemAWSDynamodb(
        final String entityType, final String promotionId, final String timeStamp) {

        final HashMap<String, Condition> keyConditions = new HashMap<>();
        final AmazonDynamoDBClient client = createAWSDynamoDBClient();

        final QueryRequest queryRequest =
            new QueryRequest().withTableName(environmentVariables.getDynamoDBTable()).withIndexName(typeTimestampIndex);

        keyConditions.put("EntityType", new Condition().withComparisonOperator(ComparisonOperator.EQ)
            .withAttributeValueList(new AttributeValue().withS(entityType)));
        keyConditions.put("AuditTimeStamp", new Condition().withComparisonOperator(ComparisonOperator.GE)
            .withAttributeValueList(new AttributeValue().withS(timeStamp)));
        queryRequest.setKeyConditions(keyConditions);
        queryRequest.addQueryFilterEntry("PromotionId", new Condition().withComparisonOperator(ComparisonOperator.EQ)
            .withAttributeValueList(new AttributeValue().withS(promotionId)));
        final QueryResult result = client.query(queryRequest);
        LOGGER
            .info("Total Items Found for " + entityType + " for Promotion " + promotionId + " : " + result.getCount());

        return result.getItems();
    }

    /**
     * method for Stores which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> store(final String id) {
        return getAuditlogItemAWSDynamodb("Store" + id);
    }

    /**
     * method for SubscriptionOffers which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> subscriptionOffer(final String id) {
        return getAuditlogItemAWSDynamodb("SubscriptionOffer" + id);
    }

    /**
     * method for PriceList which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return list of AuditLogEntry
     */
    public static List<AuditLogEntry> priceList(final String id) {
        final List<Map<String, AttributeValue>> items = getAuditlogItemAWSDynamodb("PriceList" + id);
        final DynamoDBMapperConfig config = new DynamoDBMapperConfig(
            TableNameOverride.withTableNameReplacement(environmentVariables.getDynamoDBTable()));

        final DynamoDBMapper mapper = new DynamoDBMapper(createAWSDynamoDBClient(), config);

        return mapper.marshallIntoObjects(AuditLogEntry.class, items);
    }

    /**
     * method to retrieve StoreCountry based on StoreId
     *
     * @param id : EntityId
     * @return list of AuditLogEntry
     */
    public static List<AuditLogEntry> storeCountry(final String id, final String timeStamp) {
        final List<Map<String, AttributeValue>> items =
            getEntityByStoreIdAuditlogItemAWSDynamodb("StoreCountry", id, timeStamp);
        final DynamoDBMapperConfig config = new DynamoDBMapperConfig(
            TableNameOverride.withTableNameReplacement(environmentVariables.getDynamoDBTable()));

        final DynamoDBMapper mapper = new DynamoDBMapper(createAWSDynamoDBClient(), config);

        return mapper.marshallIntoObjects(AuditLogEntry.class, items);
    }

    /**
     * method for Subscription which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> subscription(final String id) {
        return getAuditlogItemAWSDynamodb("Subscription" + id);
    }

    /**
     * method for SubscriptionPlan which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> subscriptionPlan(final String id) {
        return getAuditlogItemAWSDynamodb("SubscriptionPlan" + id);
    }

    /**
     * method for SubscriptionPrice which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> subscriptionPrice(final String id) {
        return getAuditlogItemAWSDynamodb("SubscriptionPrice" + id);
    }

    /**
     * method for SubscriptionEntitlement which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> subscriptionEntitlement(final String id) {
        return getAuditlogItemAWSDynamodb("SubscriptionEntitlement" + id);
    }

    /**
     * Method for Promotion which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> promotion(final String id) {
        return getAuditlogItemAWSDynamodb("Promotion" + id);
    }

    /**
     * method for basicOffering which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> basicOffering(final String id) {
        return getAuditlogItemAWSDynamodb("BasicOffering" + id);
    }

    /**
     * method for Feature which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : featureId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> feature(final String id) {
        return getAuditlogItemAWSDynamodb("Item" + id);
    }

    /**
     * method for Descriptor which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : EntityId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> descriptor(final String id) {
        return getAuditlogItemAWSDynamodb(PelicanConstants.DESCRIPTOR + id);
    }

    /**
     * method for Role which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : roleId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> role(final String id) {
        return getAuditlogItemAWSDynamodb("Role" + id);
    }

    /**
     * method for Role Assignment which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : roleId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> roleAssignment(final String id) {
        return getAuditlogItemAWSDynamodb(PelicanConstants.ROLE_ASSIGNMENT + id);
    }

    /**
     * method for Purchase Order which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : purchaseOrderId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> purchaseOrder(final String id) {
        return getAuditlogItemAWSDynamodb(PelicanConstants.PURCHASE_ORDER + id);
    }

    /**
     * method for Actor which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : actorId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> actor(final String id) {
        return getAuditlogItemAWSDynamodb(PelicanConstants.ACTOR + id);
    }

    /**
     * method for APISecretCredential which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : apiSecretCredentialID
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> apiSecretCredential(final String id) {
        return getAuditlogItemAWSDynamodb(PelicanConstants.API_SECRET_CREDENTIAL + id);
    }

    /**
     * method for PasswordCredential which will construct the EntityTypeEntityId for getAuditlogItemAWSDynamodb
     *
     * @param id : passwordCredentialId
     * @return List Map of items to parse for validation
     */
    public static List<Map<String, AttributeValue>> passwordCredential(final String id) {
        return getAuditlogItemAWSDynamodb(PelicanConstants.PASSWORD_CREDENTIAL + id);
    }
}
