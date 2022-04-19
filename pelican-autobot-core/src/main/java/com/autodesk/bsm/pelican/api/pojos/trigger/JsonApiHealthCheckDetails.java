package com.autodesk.bsm.pelican.api.pojos.trigger;

/**
 * This is pojo class for JsonApiHealthCheckDetails
 *
 * @author Rohini
 */
public class JsonApiHealthCheckDetails {

    private String databases;
    private String queues;
    private String tempestDB;

    /**
     * This method returns databases
     */
    public String getDatabases() {
        return databases;
    }

    /**
     * This method is to set databases
     */
    public void setDatabases(final String databases) {
        this.databases = databases;
    }

    /**
     * This method returns queues
     *
     * @return queues
     */
    public String getQueues() {
        return queues;
    }

    /**
     * This method is to set Queues
     */
    public void setQueues(final String queues) {
        this.queues = queues;
    }

    /**
     * This method returns tempestDB
     *
     * @return tempestDB
     */
    public String getTempestDB() {
        return tempestDB;
    }

    /**
     * This method is to set tempestDB
     */
    public void setTempestDB(final String tempestDB) {
        this.tempestDB = tempestDB;
    }

}
