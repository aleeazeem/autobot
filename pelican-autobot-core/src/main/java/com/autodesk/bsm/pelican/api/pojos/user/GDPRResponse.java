package com.autodesk.bsm.pelican.api.pojos.user;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

/**
 * GDPR Response POJO.
 *
 * @author t_joshv
 *
 */
public class GDPRResponse extends PelicanPojo {

    private String taskId;
    private GDPRResponseStatus status;
    private int httpStatus;

    public GDPRResponse(final String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

    public GDPRResponseStatus getStatus() {
        return status;
    }

    public void setStatus(final GDPRResponseStatus status) {
        this.status = status;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(final int httpStatus) {
        this.httpStatus = httpStatus;
    }
}
