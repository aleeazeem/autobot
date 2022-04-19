package com.autodesk.bsm.pelican.enums;

public enum RepublishChangeNotificationChannel {

    PELICAN_EVENTS("pelican-events");

    private String channel;

    RepublishChangeNotificationChannel(final String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

}
