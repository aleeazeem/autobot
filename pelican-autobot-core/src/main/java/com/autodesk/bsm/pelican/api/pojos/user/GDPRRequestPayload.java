package com.autodesk.bsm.pelican.api.pojos.user;

import com.google.gson.annotations.SerializedName;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GDPR Request Payload POJO to support GDPR request.
 *
 * @author t_joshv
 *
 */
public class GDPRRequestPayload {

    private String version;
    private String taskId;
    @SerializedName("user_info")
    private UserInfo userInfo;
    private String callbackUrl;
    private String respondBy;
    private String status;

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

    @JsonGetter("user_info")
    public UserInfo getUserInfo() {
        return userInfo;
    }

    @JsonProperty("user_info")
    public void setUserInfo(final UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getRespondBy() {
        return respondBy;
    }

    public void setRespondBy(final String respondBy) {
        this.respondBy = respondBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

}
