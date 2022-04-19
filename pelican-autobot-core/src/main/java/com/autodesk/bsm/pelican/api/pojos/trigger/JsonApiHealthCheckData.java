package com.autodesk.bsm.pelican.api.pojos.trigger;

/**
 * This is pojo class for JsonApiHealthCheckData
 *
 * @author Rohini
 */
public class JsonApiHealthCheckData {

    private String type;
    private String id;
    private JsonApiHealthCheckAttributes jsonApiHealthCheckAttributes;

    /**
     * This method returns type
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * This method is to set type
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * This method returns id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * This method is to set id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * This method returns
     *
     * @return jsonApiHealthCheckAttributes
     */
    public JsonApiHealthCheckAttributes getJsonApiHealthCheckAttributes() {
        return jsonApiHealthCheckAttributes;
    }

    /**
     * This method is to set jsonApiHealthCheckAttributes
     */
    public void setJsonApiHealthCheckAttributes(final JsonApiHealthCheckAttributes jsonApiHealthCheckAttributes) {
        this.jsonApiHealthCheckAttributes = jsonApiHealthCheckAttributes;
    }

}
