package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.EntityType;

import java.util.List;

/**
 * This class represents the JSON entity of ShippingMethods which is available as inner entity of Store Mainly used in
 * getStore API Tests automation
 *
 * @author kishor
 */
public class ShippingMethod {

    private EntityType type;
    private String externalKey;
    private String id;
    private Price price;
    private Descriptor descriptor;
    private List<Destination> destinations;
    private String agentId;

    public static class Descriptor {
        private String name;
        private String deliveryTime;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * @return the description
         */
        public String getDeliveryTime() {
            return deliveryTime;
        }

        /**
         * @param description the description to set
         */
        public void setDelveryTime(final String deliveryTime) {
            this.deliveryTime = deliveryTime;
        }
    }

    public static class Destination {
        private String country;
        private String state;

        /**
         * @return the country
         */
        public String getCountry() {
            return country;
        }

        /**
         * @param country the country to set
         */
        public void setCountry(final String country) {
            this.country = country;
        }

        /**
         * @return the state
         */
        public String getState() {
            return state;
        }

        /**
         * @param state the state to set
         */
        public void setState(final String state) {
            this.state = state;
        }
    }

    public EntityType getType() {
        return type;
    }

    public void setType(final EntityType type) {
        this.type = type;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(final Price price) {
        this.price = price;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(final Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(final List<Destination> destinations) {
        this.destinations = destinations;
    }

    /**
     * @return the agentid
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @param agentid the agentid to set
     */
    public void setAgentId(final String agentId) {
        this.agentId = agentId;
    }

}
