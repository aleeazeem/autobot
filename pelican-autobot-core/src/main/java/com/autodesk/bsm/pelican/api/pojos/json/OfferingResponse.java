package com.autodesk.bsm.pelican.api.pojos.json;

import java.util.List;

/**
 * Offering response entity for Json API
 *
 * @author t_mohag
 */
public class OfferingResponse {

    private OfferingResponseData data;
    private List<String> included;
    private List<Errors> errors;

    public static class OfferingResponseData {
        private String type;
        private String id;

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }
    }

    public OfferingResponseData getData() {
        return data;
    }

    public void setData(final OfferingResponseData data) {
        this.data = data;
    }

    public List<String> getIncluded() {
        return included;
    }

    public void setIncluded(final List<String> included) {
        this.included = included;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(final List<Errors> errors) {
        this.errors = errors;
    }
}
