package com.autodesk.bsm.pelican.api.pojos.json;

/**
 * This POJO is for CSE EC status change notification messages
 *
 * @author mandas
 */
public class CseECNotification {
    private String[] oxygen_id;
    private String SummaryECStatus;
    private String SummaryECUpdateTimestamp;

    public String[] getEcOxygenIds() {
        return oxygen_id;
    }

    public void setEcOxygenIds(final String[] oxygen_id) {
        this.oxygen_id = oxygen_id;
    }

    public String getECStatus() {
        return SummaryECStatus;
    }

    public void setECStatus(final String SummaryECStatus) {
        this.SummaryECStatus = SummaryECStatus;
    }

    public String getECUpdateTimeStamp() {
        return SummaryECUpdateTimestamp;
    }

    public void setECUpdateTimeStamp(final String SummaryECUpdateTimestamp) {
        this.SummaryECUpdateTimestamp = SummaryECUpdateTimestamp;
    }
}
