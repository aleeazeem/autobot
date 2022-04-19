package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

public class OfferingEntitlement extends PelicanPojo {

    private String itemExternalKey;
    private String type;
    private String date;
    private boolean isEos;
    private boolean isEolImmediate;
    private boolean isEolRenewal;

    public void setItemExternalKey(final String itemExternalKey) {
        this.itemExternalKey = itemExternalKey;
    }

    public String getItemExternalKey() {
        return itemExternalKey;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public boolean getIsEos() {
        return isEos;
    }

    public void setIsEos(final boolean isEos) {
        this.isEos = isEos;
    }

    public boolean getIsEolImme() {
        return isEolImmediate;
    }

    public void setIsEolImme(final boolean isEolImmediate) {
        this.isEolImmediate = isEolImmediate;
    }

    public boolean getIsEolRenewal() {
        return isEolRenewal;
    }

    public void setIsEosRenewal(final boolean isEolRenewal) {
        this.isEolRenewal = isEolRenewal;
    }
}
