package com.autodesk.bsm.pelican.enums;

/**
 * This is an ENUM for the entity types listed in Add Descriptor Forms.
 * <p>
 * Need to update this enum if any changes/additions available in add descriptor page
 *
 * @author kishor
 */
public enum DescriptorEntityTypes {

    BASIC_OFFERING("Basic Offering"),
    SUBSCRIPTION_PLAN("Subscription Plan"),
    SUBSCRIPTION_OFFER("Subscription Offer"),
    PROMOTION("Promotion"),
    SHIPPING_METHODS("Shipping Method");

    String entity = "";

    DescriptorEntityTypes(final String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public static DescriptorEntityTypes getEntityType(final String entityString) {
        DescriptorEntityTypes typeObj = null;
        for (final DescriptorEntityTypes type : DescriptorEntityTypes.values()) {
            if (type.getEntity().equalsIgnoreCase(entityString)) {
                typeObj = type;
            }
        }
        return typeObj;
    }
}
