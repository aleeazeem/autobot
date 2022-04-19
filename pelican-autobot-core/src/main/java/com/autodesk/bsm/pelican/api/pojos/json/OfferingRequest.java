package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;

import java.util.List;

/**
 * Offering request entity for Json API
 *
 * @author t_mohag
 */
public class OfferingRequest {

    private OfferingRequestData data;

    public OfferingRequest(final OfferingRequestData data) {
        this.data = data;
    }

    public static class OfferingRequestData {
        private EntityType type;
        private Status status;
        private String externalKey;
        private OfferingType offeringType;
        private String name;
        private String supportLevel;
        private List<Price> prices;
        private MediaType mediaType;

        public EntityType getType() {
            return type;
        }

        public void setType(final EntityType type) {
            this.type = type;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(final Status status) {
            this.status = status;
        }

        public String getExternalKey() {
            return externalKey;
        }

        public void setExternalKey(final String externalKey) {
            this.externalKey = externalKey;
        }

        public OfferingType getOfferingType() {
            return offeringType;
        }

        public void setOfferingType(final OfferingType offeringType) {
            this.offeringType = offeringType;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getSupportLevel() {
            return supportLevel;
        }

        public void setSupportLevel(final String supportLevel) {
            this.supportLevel = supportLevel;
        }

        public List<Price> getPrices() {
            return prices;
        }

        public void setPrices(final List<Price> prices) {
            this.prices = prices;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public void setMediaType(final MediaType mediaType) {
            this.mediaType = mediaType;
        }
    }

    public OfferingRequestData getData() {
        return data;
    }
}
