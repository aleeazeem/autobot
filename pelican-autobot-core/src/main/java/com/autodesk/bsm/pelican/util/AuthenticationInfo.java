package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

public class AuthenticationInfo {

    private String partnerId;
    private String appFamilyId;
    private String timestamp;
    private String signature;
    private String secret;
    private String requestPath;
    private String requestBody;

    public AuthenticationInfo(final EnvironmentVariables environmentVariables, final String applicationFamily) {

        this.partnerId = environmentVariables.getPartnerId();
        if (applicationFamily.equals(environmentVariables.getAppFamily())) {
            this.appFamilyId = environmentVariables.getAppFamilyId();
        } else {
            this.appFamilyId = environmentVariables.getOtherAppFamilyId();
        }
        this.secret = environmentVariables.getSecretCredential();
    }

    public AuthenticationInfo(final String partnerId, final String appFamilyId, final String secret) {
        this.partnerId = partnerId;
        this.appFamilyId = appFamilyId;
        this.secret = secret;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getAppFamilyId() {
        return appFamilyId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public String getSecretKey() {
        return secret;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setPartnerId(final String partnerId) {
        this.partnerId = partnerId;
    }

    public void setAppFamilyId(final String appFamilyId) {
        this.appFamilyId = appFamilyId;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }

    public void setSecretKey(final String secret) {
        this.secret = secret;
    }

    public void setRequestPath(final String requestPath) {
        this.requestPath = requestPath;
    }

    public void setRequestBody(final String requestBody) {
        this.requestBody = requestBody;
    }

}
