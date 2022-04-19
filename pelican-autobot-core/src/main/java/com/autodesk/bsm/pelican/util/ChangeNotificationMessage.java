package com.autodesk.bsm.pelican.util;

import org.springframework.amqp.support.converter.JsonMessageConverter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Sumant Manda
 */
public class ChangeNotificationMessage implements Serializable {
    private static final long serialVersionUID = 3644520621214498437L;

    private String id;
    private String json;
    private String header;

    public ChangeNotificationMessage() {}

    // public ChangeNotificationMessage(String id, String json) {
    // this.id = id;
    // this.json = json;
    // }

    public ChangeNotificationMessage(final String id, final String json, final String header) {
        this.id = id;
        this.json = json;
        this.header = header;
    }

    public ChangeNotificationMessage(final String json) {
        this.id = UUID.randomUUID().toString();
        this.json = json;
    }

    public ChangeNotificationMessage(final String json, final String header) {
        this.id = UUID.randomUUID().toString();
        this.json = json;
        this.header = header;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return json;
    }

    public String getHeader() {
        return header;
    }

    public void setData(final String json) {
        this.json = json;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((json == null) ? 0 : json.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChangeNotificationMessage other = (ChangeNotificationMessage) obj;
        if (json == null) {
            return other.json == null;
        } else {
            return json.equals(other.json);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(new JsonMessageConverter().toMessage(this, null));
    }
}
