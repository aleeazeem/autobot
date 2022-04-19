package com.autodesk.bsm.pelican.api.pojos.bicrelease;

import java.util.Date;

public class BicVersion {

    private String version;
    private Date fcsDate;
    private String legacySku;
    private boolean createLegacyAsset;

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Date getFcsDate() {
        return fcsDate;
    }

    public void setFcsDate(final Date fcsDate) {
        this.fcsDate = fcsDate;
    }

    public String getLegacySku() {
        return legacySku;
    }

    public void setLegacySku(final String legacySku) {
        this.legacySku = legacySku;
    }

    public boolean isCreateLegacyAsset() {
        return createLegacyAsset;
    }

    public void setCreateLegacyAsset(final boolean createLegacyAsset) {
        this.createLegacyAsset = createLegacyAsset;
    }
}
