package com.autodesk.bsm.pelican.api.pojos.user;

/**
 * GDPR Response Status POJO to support GDPR Response.
 *
 * @author t_joshv
 *
 */
public class GDPRResponseStatus {

    private String code;
    private String type;
    private String reason;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
