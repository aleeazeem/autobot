package com.autodesk.bsm.pelican.api.pojos.json;

public class JPromotionReference {
    private String id;
    private String code;

    public JPromotionReference(final String id, final String code) {
        this.id = id;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }
}
