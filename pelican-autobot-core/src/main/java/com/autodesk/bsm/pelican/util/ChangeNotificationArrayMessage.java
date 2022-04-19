package com.autodesk.bsm.pelican.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zaheer on 1/26/16.
 */
public class ChangeNotificationArrayMessage implements Serializable {
    private static final long serialVersionUID = -2250119138596915408L;

    private Meta meta;
    private Jsonapi jsonapi;
    private List<String> data;

    public List<String> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    public void setData(final List<String> data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(final Meta meta) {
        this.meta = meta;
    }

    public Jsonapi getJsonapi() {
        return jsonapi;
    }

    public void setJsonapi(final Jsonapi jsonapi) {
        this.jsonapi = jsonapi;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeNotificationArrayMessage{");
        sb.append("meta=").append(meta);
        sb.append(", jsonapi=").append(jsonapi);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }

    public static class Meta implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 8998013216707167152L;
        private String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Meta [version=");
            builder.append(version);
            builder.append("]");
            return builder.toString();
        }
    }

    public static class Jsonapi implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -4337030827120582583L;
        private String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("Jsonapi [version=");
            builder.append(version);
            builder.append("]");
            return builder.toString();
        }
    }
}
