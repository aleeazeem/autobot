package com.autodesk.bsm.pelican.api.pojos.bicrelease;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo for the response of productLine/eligibleVersions. This is the root which contains multiple
 * ProductLineEligibleProducts
 *
 * @author yin
 */
public class EligibleVersions extends PelicanPojo {

    private List<ProductLineEligibleProduct> products = new ArrayList<>();

    public List<ProductLineEligibleProduct> getProducts() {
        return products;
    }

    public void setProducts(final List<ProductLineEligibleProduct> products) {
        this.products = products;
    }

}
