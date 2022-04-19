package com.autodesk.bsm.pelican.api.pojos.trigger;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;
import com.autodesk.bsm.pelican.api.pojos.json.Errors;

/**
 * This is pojo class for JsonApiHealthCheck which extends PelicanPojo
 *
 * @author Rohini
 */
public class JsonApiHealthCheck extends PelicanPojo {

    private JsonApiHealthCheckData data;
    private JsonHealthCheckApi jsonApi;
    private Errors errors;

    /**
     * This method is to get data
     *
     * @return the data
     */
    public JsonApiHealthCheckData getData() {
        return data;
    }

    /**
     * This method is to set data
     */
    public void setData(final JsonApiHealthCheckData data) {
        this.data = data;
    }

    /**
     * This method returns jsonApi
     *
     * @return jsonApi
     */
    public JsonHealthCheckApi getJsonApi() {
        return jsonApi;
    }

    /**
     * This method is to set jsonApi
     */
    public void setJsonApi(final JsonHealthCheckApi jsonApi) {
        this.jsonApi = jsonApi;
    }

    /**
     * This method is to return errors
     *
     * @return errors
     */
    public Errors getErrors() {
        return errors;
    }

    /**
     * This method is to set errors
     */
    public void setErrors(final Errors errors) {
        this.errors = errors;
    }

}
