package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

/**
 * This class represents the JSON object of Promotions.
 *
 * @author t_mohag
 */
public class Promotions extends PelicanPojo {
    private List<JPromotionData> data;
    private List<Error> errors;

    public static class Error {
        private String detail;
        private String code;
        private int status;

        public String getDetail() {
            return detail;
        }

        public void setDetail(final String title) {
            this.detail = title;
        }

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(final int status) {
            this.status = status;
        }
    }

    public List<JPromotionData> getData() {
        return data;
    }

    public void setData(final List<JPromotionData> data) {
        this.data = data;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(final List<Error> errors) {
        this.errors = errors;
    }
}
