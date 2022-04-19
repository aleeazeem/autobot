package com.autodesk.bsm.pelican.cse;

/**
 * This POJO is for Feature, which comes in attributes section of CSE messages
 *
 * @author mandas
 */
public class Features {
    private String externalKey;
    private String changeType;
    private String type;
    private String eosDate;
    private String eolRenewalDate;
    private String eolImmediateDate;

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(final String changeType) {
        this.changeType = changeType;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getEosDate() {
        return eosDate;
    }

    public void setEosDate(final String eosDate) {
        this.eosDate = eosDate;
    }

    public String getEolRenewalDate() {
        return eolRenewalDate;
    }

    public void setEolRenewalDate(final String eolRenewalDate) {
        this.eolRenewalDate = eolRenewalDate;
    }

    public String getEolImmediateDate() {
        return eolImmediateDate;
    }

    public void setEolImmediateDate(final String eolImmediateDate) {
        this.eolImmediateDate = eolImmediateDate;
    }
}
