package com.autodesk.bsm.pelican.cse;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/**
 * @author mandas
 */
public class ChangeNotifications extends PelicanPojo {
    private ChangeNotificationData data;
    private ChangeNotificationMeta meta;
    private ChangeNotificationJsonAPI jsonapi;

    public ChangeNotificationData getData() {
        return data;
    }

    public void setData(final ChangeNotificationData data) {
        this.data = data;
    }

    public ChangeNotificationMeta getMeta() {
        return meta;
    }

    public void setMeta(final ChangeNotificationMeta meta) {
        this.meta = meta;
    }

    public ChangeNotificationJsonAPI getJsonapi() {
        return jsonapi;
    }

    public void setJsonapi(final ChangeNotificationJsonAPI jsonapi) {
        this.jsonapi = jsonapi;
    }

}
