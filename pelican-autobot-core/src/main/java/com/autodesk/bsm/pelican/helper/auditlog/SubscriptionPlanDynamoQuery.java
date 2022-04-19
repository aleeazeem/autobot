package com.autodesk.bsm.pelican.helper.auditlog;

import com.autodesk.bsm.pelican.enums.Action;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PackagingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.SupportLevel;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.util.AuditLogEntry.ChangeDetails;

import java.util.Map;

public class SubscriptionPlanDynamoQuery {

    private Map<String, ChangeDetails> auditData;
    private String subscriptionPlanId;
    private final String oldName;
    private final String newName;
    private final String oldExternalKey;
    private final String newExternalKey;
    private final OfferingType oldOfferingType;
    private final OfferingType newOfferingType;
    private final Status oldStatus;
    private final Status newStatus;
    private final CancellationPolicy oldCancellationPolicy;
    private final CancellationPolicy newCancellationPolicy;
    private final UsageType oldUsageType;
    private final UsageType newUsageType;
    private final String oldOfferingDetailId;
    private final String newOfferingDetailId;
    private final String oldProductLine;
    private final String newProductLine;
    private final SupportLevel oldSupportLevel;
    private final SupportLevel newSupportLevel;
    private final Action action;
    private final String fileName;
    private final PackagingType oldPackagingValue;
    private final PackagingType newPackagingValue;
    private final String oldExpReminderEmailEnabled;
    private final String newExpReminderEmailEnabled;

    public SubscriptionPlanDynamoQuery(final Builder builder) {

        this.auditData = builder.auditData;
        this.subscriptionPlanId = builder.subscriptionPlanId;
        this.oldName = builder.oldName;
        this.newName = builder.newName;
        this.oldExternalKey = builder.oldExternalKey;
        this.newExternalKey = builder.newExternalKey;
        this.oldOfferingType = builder.oldOfferingType;
        this.newOfferingType = builder.newOfferingType;
        this.oldStatus = builder.oldStatus;
        this.newStatus = builder.newStatus;
        this.oldCancellationPolicy = builder.oldCancellationPolicy;
        this.newCancellationPolicy = builder.newCancellationPolicy;
        this.oldUsageType = builder.oldUsageType;
        this.newUsageType = builder.newUsageType;
        this.oldOfferingDetailId = builder.oldOfferingDetailId;
        this.newOfferingDetailId = builder.newOfferingDetailId;
        this.oldProductLine = builder.oldProductLine;
        this.newProductLine = builder.newProductLine;
        this.oldSupportLevel = builder.oldSupportLevel;
        this.newSupportLevel = builder.newSupportLevel;
        this.action = builder.action;
        this.fileName = builder.fileName;
        this.oldPackagingValue = builder.oldPackagingValue;
        this.newPackagingValue = builder.newPackagingValue;
        this.oldExpReminderEmailEnabled = builder.oldExpReminderEmailEnabled;
        this.newExpReminderEmailEnabled = builder.newExpReminderEmailEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, ChangeDetails> getAuditData() {
        return auditData;
    }

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldExternalKey() {
        return oldExternalKey;
    }

    public String getNewExternalKey() {
        return newExternalKey;
    }

    public OfferingType getOldOfferingType() {
        return oldOfferingType;
    }

    public OfferingType getNewOfferingType() {
        return newOfferingType;
    }

    public Status getOldStatus() {
        return oldStatus;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    public CancellationPolicy getOldCancellationPolicy() {
        return oldCancellationPolicy;
    }

    public CancellationPolicy getNewCancellationPolicy() {
        return newCancellationPolicy;
    }

    public UsageType getOldUsageType() {
        return oldUsageType;
    }

    public UsageType getNewUsageType() {
        return newUsageType;
    }

    public String getOldOfferingDetailId() {
        return oldOfferingDetailId;
    }

    public String getNewOfferingDetailId() {
        return newOfferingDetailId;
    }

    public String getOldProductLine() {
        return oldProductLine;
    }

    public String getNewProductLine() {
        return newProductLine;
    }

    public SupportLevel getOldSupportLevel() {
        return oldSupportLevel;
    }

    public SupportLevel getNewSupportLevel() {
        return newSupportLevel;
    }

    public Action getAction() {
        return action;
    }

    public String getFileName() {
        return fileName;
    }

    public PackagingType getOldPackagingValue() {
        return oldPackagingValue;
    }

    public PackagingType getNewPackagingValue() {
        return newPackagingValue;
    }

    public String getOldExpReminderEmailEnabled() {
        return oldExpReminderEmailEnabled;
    }

    public String getNewExpReminderEmailEnabled() {
        return newExpReminderEmailEnabled;
    }

    public void setAuditData(final Map<String, ChangeDetails> auditData) {
        this.auditData = auditData;
    }

    public void setSubscriptionPlanId(final String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public static class Builder {

        private Map<String, ChangeDetails> auditData;
        private String subscriptionPlanId;
        private String oldName;
        private String newName;
        private String oldExternalKey;
        private String newExternalKey;
        private OfferingType oldOfferingType;
        private OfferingType newOfferingType;
        private Status oldStatus;
        private Status newStatus;
        private CancellationPolicy oldCancellationPolicy;
        private CancellationPolicy newCancellationPolicy;
        private UsageType oldUsageType;
        private UsageType newUsageType;
        private String oldOfferingDetailId;
        private String newOfferingDetailId;
        private String oldProductLine;
        private String newProductLine;
        private SupportLevel oldSupportLevel;
        private SupportLevel newSupportLevel;
        private Action action;
        private String fileName;
        private PackagingType oldPackagingValue;
        private PackagingType newPackagingValue;
        private String oldExpReminderEmailEnabled;
        private String newExpReminderEmailEnabled;

        public Builder(final SubscriptionPlanDynamoQuery subscriptionPlanDynamoQuery) {
            this.auditData = subscriptionPlanDynamoQuery.auditData;
            this.subscriptionPlanId = subscriptionPlanDynamoQuery.subscriptionPlanId;
            this.oldName = subscriptionPlanDynamoQuery.oldName;
            this.newName = subscriptionPlanDynamoQuery.newName;
            this.oldExternalKey = subscriptionPlanDynamoQuery.oldExternalKey;
            this.newExternalKey = subscriptionPlanDynamoQuery.newExternalKey;
            this.oldOfferingType = subscriptionPlanDynamoQuery.oldOfferingType;
            this.newOfferingType = subscriptionPlanDynamoQuery.newOfferingType;
            this.oldStatus = subscriptionPlanDynamoQuery.oldStatus;
            this.newStatus = subscriptionPlanDynamoQuery.newStatus;
            this.oldCancellationPolicy = subscriptionPlanDynamoQuery.oldCancellationPolicy;
            this.newCancellationPolicy = subscriptionPlanDynamoQuery.newCancellationPolicy;
            this.oldUsageType = subscriptionPlanDynamoQuery.oldUsageType;
            this.newUsageType = subscriptionPlanDynamoQuery.newUsageType;
            this.oldOfferingDetailId = subscriptionPlanDynamoQuery.oldOfferingDetailId;
            this.newOfferingDetailId = subscriptionPlanDynamoQuery.newOfferingDetailId;
            this.oldProductLine = subscriptionPlanDynamoQuery.oldProductLine;
            this.newProductLine = subscriptionPlanDynamoQuery.newProductLine;
            this.oldSupportLevel = subscriptionPlanDynamoQuery.oldSupportLevel;
            this.newSupportLevel = subscriptionPlanDynamoQuery.newSupportLevel;
            this.action = subscriptionPlanDynamoQuery.action;
            this.oldPackagingValue = subscriptionPlanDynamoQuery.oldPackagingValue;
            this.fileName = subscriptionPlanDynamoQuery.fileName;
            this.newPackagingValue = subscriptionPlanDynamoQuery.newPackagingValue;
            this.oldExpReminderEmailEnabled = subscriptionPlanDynamoQuery.oldExpReminderEmailEnabled;
            this.newExpReminderEmailEnabled = subscriptionPlanDynamoQuery.newExpReminderEmailEnabled;
        }

        public Builder() {
            // TODO Auto-generated constructor stub
        }

        public SubscriptionPlanDynamoQuery build() {
            return new SubscriptionPlanDynamoQuery(this);
        }

        public Builder setAuditData(final Map<String, ChangeDetails> auditData) {
            this.auditData = auditData;
            return this;
        }

        public Builder setSubscriptionPlanId(final String subscriptionPlanId) {
            this.subscriptionPlanId = subscriptionPlanId;
            return this;
        }

        public Builder setOldName(final String oldName) {
            this.oldName = oldName;
            return this;
        }

        public Builder setNewName(final String newName) {
            this.newName = newName;
            return this;
        }

        public Builder setOldExternalKey(final String oldExternalKey) {
            this.oldExternalKey = oldExternalKey;
            return this;
        }

        public Builder setNewExternalKey(final String newExternalKey) {
            this.newExternalKey = newExternalKey;
            return this;
        }

        public Builder setOldOfferingType(final OfferingType oldOfferingType) {
            this.oldOfferingType = oldOfferingType;
            return this;
        }

        public Builder setNewOfferingType(final OfferingType newOfferingType) {
            this.newOfferingType = newOfferingType;
            return this;
        }

        public Builder setOldStatus(final Status oldStatus) {
            this.oldStatus = oldStatus;
            return this;
        }

        public Builder setNewStatus(final Status newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public Builder setOldCancellationPolicy(final CancellationPolicy oldCancellationPolicy) {
            this.oldCancellationPolicy = oldCancellationPolicy;
            return this;
        }

        public Builder setNewCancellationPolicy(final CancellationPolicy newCancellationPolicy) {
            this.newCancellationPolicy = newCancellationPolicy;
            return this;
        }

        public Builder setOldUsageType(final UsageType oldUsageType) {
            this.oldUsageType = oldUsageType;
            return this;
        }

        public Builder setNewUsageType(final UsageType newUsageType) {
            this.newUsageType = newUsageType;
            return this;
        }

        public Builder setOldOfferingDetailId(final String oldOfferingDetailId) {
            this.oldOfferingDetailId = oldOfferingDetailId;
            return this;
        }

        public Builder setNewOfferingDetailId(final String newOfferingDetailId) {
            this.newOfferingDetailId = newOfferingDetailId;
            return this;
        }

        public Builder setOldProductLine(final String oldProductLine) {
            this.oldProductLine = oldProductLine;
            return this;
        }

        public Builder setNewProductLine(final String newProductLine) {
            this.newProductLine = newProductLine;
            return this;
        }

        public Builder setOldSupportLevel(final SupportLevel oldSupportLevel) {
            this.oldSupportLevel = oldSupportLevel;
            return this;
        }

        public Builder setNewSupportLevel(final SupportLevel newSupportLevel) {
            this.newSupportLevel = newSupportLevel;
            return this;
        }

        public Builder setAction(final Action action) {
            this.action = action;
            return this;
        }

        public Builder setFileName(final String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setOldPackagingValue(final PackagingType oldPackagingValue) {
            this.oldPackagingValue = oldPackagingValue;
            return this;
        }

        public Builder setNewPackagingValue(final PackagingType newPackagingValue) {
            this.newPackagingValue = newPackagingValue;
            return this;
        }

        public Builder setOldExpReminderEmailEnabled(final String oldExpReminderEmailEnabled) {
            this.oldExpReminderEmailEnabled = oldExpReminderEmailEnabled;
            return this;
        }

        public Builder setNewExpReminderEmailEnabled(final String newExpReminderEmailEnabled) {
            this.newExpReminderEmailEnabled = newExpReminderEmailEnabled;
            return this;
        }
    }

}
