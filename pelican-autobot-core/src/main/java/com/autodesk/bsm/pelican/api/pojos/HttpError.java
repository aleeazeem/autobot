package com.autodesk.bsm.pelican.api.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpError extends PelicanPojo {

    private int status;
    private String reason;
    private int errorCode;
    private String errorMessage;
    private String uuid;

    public int getStatus() {
        return status;
    }

    @XmlElement(name = "status")
    @JsonProperty(value = "status")
    public void setStatus(final int status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    @XmlElement(name = "reason")
    @JsonProperty(value = "reason")
    public void setReason(final String reason) {
        this.reason = reason;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @XmlElement(name = "code")
    @JsonProperty(value = "code")
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @XmlElement(name = "message")
    @JsonProperty(value = "message")
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUuid() {
        return uuid;
    }

    @XmlElement(name = "uuid")
    @JsonProperty(value = "uuid")
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
