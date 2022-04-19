package com.autodesk.bsm.pelican.api.pojos.user;

/**
 * WebHookInfo POJO to support GDPR Request
 *
 * @author t_joshv
 *
 */
public class WebHookInfo {

    private String eventType;
    private String hookId;
    private String clientID;
    private String webhookEndpoint;
    private String createdDate;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public String getHookId() {
        return hookId;
    }

    public void setHookId(final String hookId) {
        this.hookId = hookId;
    }

    public String getclientID() {
        return clientID;
    }

    public void setclientID(final String clientID) {
        this.clientID = clientID;
    }

    public String getWebhookEndpoint() {
        return webhookEndpoint;
    }

    public void setWebhookEndpoint(final String webhookEndpoint) {
        this.webhookEndpoint = webhookEndpoint;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final String createdDate) {
        this.createdDate = createdDate;
    }

}
