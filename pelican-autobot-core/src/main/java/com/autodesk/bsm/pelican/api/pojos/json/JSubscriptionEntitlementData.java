package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.GrantType;

import java.util.List;

/**
 * This class represents the JSON object of Add Entitlement.
 *
 * @author jains
 */
public class JSubscriptionEntitlementData {

    private EntityType type;
    private String appId;
    private GrantType grantType;
    private String externalKey;
    private String licensingModel;
    private List<String> coreProducts;
    private String responseType;
    private String id;
    private String itemTypeExternalKey;
    private String name;
    private String licensingModelExternalKey;
    private String entitlementDbId;

    public EntityType getEntityType() {
        return type;
    }

    public void setEntityType(final EntityType entityType) {
        this.type = entityType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(final String appId) {
        this.appId = appId;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(final GrantType grantType) {
        this.grantType = grantType;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getLicensingModel() {
        return licensingModel;
    }

    public void setLicensingModel(final String licensingModel) {
        this.licensingModel = licensingModel;
    }

    public List<String> getCoreProducts() {
        return coreProducts;
    }

    public void setCoreProducts(final List<String> coreProducts) {
        this.coreProducts = coreProducts;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getItemTypeExternalKey() {
        return itemTypeExternalKey;
    }

    public void setItemTypeExternalKey(final String itemTypeExternalKey) {
        this.itemTypeExternalKey = itemTypeExternalKey;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLicensingModelExternalKey() {
        return licensingModelExternalKey;
    }

    public void setLicensingModelExternalKey(final String licensingModelExternalKey) {
        this.licensingModelExternalKey = licensingModelExternalKey;
    }

    public String getEntitlementDbId() {
        return entitlementDbId;
    }

    public void setEntitlementDbId(final String entitlementDbId) {
        this.entitlementDbId = entitlementDbId;
    }
}
