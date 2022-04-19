package com.autodesk.bsm.pelican.api.pojos.user;

/**
 * GDPR Request Pojo.
 *
 * @author t_joshv
 *
 */
public class GDPRRequest {

    private String version;
    private WebHookInfo hook;
    private GDPRRequestPayload payload;

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public WebHookInfo getHook() {
        return hook;
    }

    public void setHook(final WebHookInfo hook) {
        this.hook = hook;
    }

    public GDPRRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(final GDPRRequestPayload payload) {
        this.payload = payload;
    }

}
