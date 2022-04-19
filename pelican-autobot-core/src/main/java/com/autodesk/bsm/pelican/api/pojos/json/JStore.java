package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the JSON object of Store This class is named as JStore to avoid ambiguity as there is another
 * class named Store which represents the XML entity
 *
 * @author kishor
 */
public class JStore {

    private String name;
    private String externalKey;
    private String storeType;
    private String callbackUrl;
    private String createdOn;
    private String lastModifiedOn;
    private String status;
    private List<Country> countries;
    private String id;
    private String applicationFamily;
    private Included included;
    private Errors errors;
    private EntityType entityType;

    public static class Included {
        private List<ShippingMethod> shippingMethods;
        private List<PriceList> priceLists;

        public List<ShippingMethod> getShippingMethods() {
            if (shippingMethods == null) {
                shippingMethods = new ArrayList<>();
            }
            return shippingMethods;
        }

        public void setShippingMethods(final List<ShippingMethod> shippingMethods) {
            this.shippingMethods = shippingMethods;
        }

        public List<PriceList> getPriceLists() {
            if (priceLists == null) {
                priceLists = new ArrayList<>();
            }
            return priceLists;
        }

        public void setPriceLists(final List<PriceList> priceLists) {
            this.priceLists = priceLists;
        }
    }

    public String getExtKey() {
        return externalKey;
    }

    public void setExtKey(final String extKey) {
        this.externalKey = extKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String value) {
        this.status = value;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String value) {
        this.externalKey = value;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(final String value) {
        this.storeType = value;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(final String value) {
        this.callbackUrl = value;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(final String value) {
        this.createdOn = value;
    }

    public String getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(final String value) {
        this.lastModifiedOn = value;
    }

    public List<String> getProperties() {
        final List<String> properties = new ArrayList<>();
        properties.add("applicationFamily");
        properties.add("id");
        properties.add("name");
        properties.add("externalKey");
        properties.add("storeType");
        properties.add("callbackUrl");

        return properties;
    }

    public List<String> getDateProperties() {
        final List<String> properties = new ArrayList<>();
        properties.add("createdOn");
        properties.add("lastModifiedOn");

        return properties;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(final List<Country> countries) {
        this.countries = countries;
    }

    /**
     * @return the applicationFamilyId
     */
    public String getApplicationFamily() {
        return applicationFamily;
    }

    /**
     * @param applicationFamilyId the applicationFamilyId to set
     */
    public void setApplicationFamily(final String applicationFamily) {
        this.applicationFamily = applicationFamily;
    }

    /**
     * @return the included
     */
    public Included getIncluded() {
        return included;
    }

    /**
     * @param included the included to set
     */
    public void setIncluded(final Included included) {
        this.included = included;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(final Errors errors) {
        this.errors = errors;
    }

    /**
     * @return the entityType
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(final EntityType entityType) {
        this.entityType = entityType;
    }
}
