package com.autodesk.bsm.pelican.cse;

public class ChangeNotificationAttributes {
    private String publishDate;
    private String changeType;
    private String subject;

    public void setPublishDate(final String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setChangeType(final String changeType) {
        this.changeType = changeType;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
}
