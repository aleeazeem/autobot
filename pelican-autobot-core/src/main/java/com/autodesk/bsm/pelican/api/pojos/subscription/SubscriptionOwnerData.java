package com.autodesk.bsm.pelican.api.pojos.subscription;

/**
 * Pojo for data for GetSubscriptionOwners api.
 *
 * @author jains
 *
 */
public class SubscriptionOwnerData {
    private String type;
    private String id;
    private String externalKey;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }
}
