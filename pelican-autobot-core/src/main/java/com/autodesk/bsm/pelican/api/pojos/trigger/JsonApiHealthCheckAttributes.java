package com.autodesk.bsm.pelican.api.pojos.trigger;

/**
 * This is pojo class for JsonApiHealthCheckAttributes
 *
 * @author Rohini
 */
public class JsonApiHealthCheckAttributes {

    private String created;
    private String system;
    private JsonApiHealthCheckDetails jsonApiHealthCheckDetails;

    /**
     * This method returns created date
     *
     * @return created
     */
    public String getCreated() {
        return created;
    }

    /**
     * This method is to set created date
     */
    public void setCreated(final String created) {
        this.created = created;
    }

    /**
     * This method returns system
     *
     * @return system
     */
    public String getSystem() {
        return system;
    }

    /**
     * This method sets system
     */
    public void setSystem(final String system) {
        this.system = system;
    }

    /**
     * This method returns jsonApiHealthCheckDetails
     *
     * @return jsonApiHealthCheckDetails
     */
    public JsonApiHealthCheckDetails getJsonApiHealthCheckDetails() {
        return jsonApiHealthCheckDetails;
    }

    /**
     * This method is to set jsonApiHealthCheckDetails
     */
    public void setJsonApiHealthCheckDetails(final JsonApiHealthCheckDetails jsonApiHealthCheckDetails) {
        this.jsonApiHealthCheckDetails = jsonApiHealthCheckDetails;
    }
}
