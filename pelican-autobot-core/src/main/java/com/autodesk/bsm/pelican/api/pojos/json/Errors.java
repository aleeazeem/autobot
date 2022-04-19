package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.EntityType;

import java.util.List;

/**
 * Errors in the Json API response
 *
 * @author vineel
 */
public class Errors {

    private String detail;
    private String code;
    private int status;
    private Links links;
    private String title;
    private String message;

    public static class Links {
        private Price price;
        private Promotions promotions;

        public Price getPrice() {
            return price;
        }

        public void setPrice(final Price price) {
            this.price = price;
        }

        public Promotions getPromotions() {
            return promotions;
        }

        public void setPromotions(final Promotions promotions) {
            this.promotions = promotions;
        }
    }

    public static class Linkage {
        private EntityType type;
        private String id;
        private Meta meta;

        public EntityType getType() {
            return type;
        }

        public void setType(final EntityType type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public Meta getMeta() {
            return meta;
        }

        public void setMeta(final Meta meta) {
            this.meta = meta;
        }
    }

    public static class Meta {
        private String requestedCode;

        public String getRequestedCode() {
            return requestedCode;
        }

        public void setRequestedCode(final String requestedCode) {
            this.requestedCode = requestedCode;
        }
    }

    public static class Promotions {
        private List<Linkage> linkage;

        public List<Linkage> getLinkage() {
            return linkage;
        }

        public void setLinkage(final List<Linkage> linkage) {
            this.linkage = linkage;
        }
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(final String title) {
        this.detail = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
