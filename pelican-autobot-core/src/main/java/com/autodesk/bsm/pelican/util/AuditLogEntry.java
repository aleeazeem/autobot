package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.PelicanConstants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

@SuppressWarnings("serial")

// table name could potentially be set dynamically here
@DynamoDBTable(tableName = "OVERRIDE")
public class AuditLogEntry implements Serializable {

    private static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping()
        .setDateFormat(PelicanConstants.AUDIT_LOG_DATE_FORMAT).create();
    private static final Type CHANGEDETAILS_TYPE = new TypeToken<Map<String, ChangeDetails>>() {}.getType();

    private String id;
    private String date;
    private String timestamp;
    private String userId;
    private String ipAddress;
    private String entityType;
    private String entityId;
    private String offeringId;
    private String offerId;
    private String storeId;
    private String action;
    private String changeDetails;
    private String fileName;

    public static class ChangeDetails implements Serializable {
        private String oldValue;
        private String newValue;

        public ChangeDetails() {}

        public String getOldValue() {
            return oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public String toString() {
            return String.format("{oldValue=%s,newValue=%s}", oldValue, newValue);
        }
    }

    public AuditLogEntry() {}

    @DynamoDBRangeKey(attributeName = PelicanConstants.AUDIT_LOG_ID)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @DynamoDBHashKey(attributeName = PelicanConstants.DATE)
    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    @DynamoDBAttribute(attributeName = PelicanConstants.AUDIT_LOG_TIMESTAMP)
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBAttribute(attributeName = "UserId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    @DynamoDBAttribute(attributeName = "IpAddress")
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @DynamoDBAttribute(attributeName = "EntityType")
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(final String entityType) {
        this.entityType = entityType;
    }

    @DynamoDBAttribute(attributeName = "EntityId")
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    @DynamoDBAttribute(attributeName = "OfferingId")
    public String getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(final String offeringId) {
        this.offeringId = offeringId;
    }

    @DynamoDBAttribute(attributeName = "OfferId")
    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(final String offerId) {
        this.offerId = offerId;
    }

    @DynamoDBAttribute(attributeName = "StoreId")
    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(final String storeId) {
        this.storeId = storeId;
    }

    @DynamoDBAttribute(attributeName = "Action")
    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    @DynamoDBAttribute(attributeName = "AuditData")
    public String getChangeDetails() {
        return changeDetails;
    }

    public void setChangeDetails(final String changeDetails) {
        this.changeDetails = changeDetails;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @DynamoDBAttribute(attributeName = "FileName")
    public String getFileName() {
        return fileName;
    }

    public Map<String, ChangeDetails> getChangeDetailsAsMap() {
        return GSON.fromJson(changeDetails, CHANGEDETAILS_TYPE);
    }

    public String toString() {
        return GSON.toJson(this);
    }

}
