package com.autodesk.bsm.pelican.api.pojos;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vineel
 */
@XmlRootElement(name = "applications")
public class Applications extends PelicanPojo {

    private List<Application> applications;
    private String startIndex;
    private String blockSize;
    private HttpError httpError;

    @XmlElement(name = "application")
    public void setApplications(final List<Application> applications) {
        this.applications = applications;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public String getStartIndex() {
        return startIndex;
    }

    @XmlAttribute(name = "startIndex")
    public void setStartIndex(final String startIndex) {
        this.startIndex = startIndex;
    }

    public String getBlockSize() {
        return blockSize;
    }

    @XmlAttribute(name = "blockSize")
    public void setBlockSize(final String blockSize) {
        this.blockSize = blockSize;
    }

    public HttpError getHttpError() {
        return httpError;
    }

    @XmlElement(name = "errors")
    public void setHttpError(final HttpError httpError) {
        this.httpError = httpError;
    }
}
