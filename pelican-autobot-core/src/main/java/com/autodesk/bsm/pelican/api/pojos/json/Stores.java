package com.autodesk.bsm.pelican.api.pojos.json;

import java.util.ArrayList;
import java.util.List;

public class Stores {

    private List<JStore> stores;
    private Errors errors;

    public List<JStore> getStores() {
        if (stores == null) {
            stores = new ArrayList<>();
        }
        return stores;
    }

    public void setStores(final List<JStore> stores) {
        this.stores = stores;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(final Errors errors) {
        this.errors = errors;
    }
}
