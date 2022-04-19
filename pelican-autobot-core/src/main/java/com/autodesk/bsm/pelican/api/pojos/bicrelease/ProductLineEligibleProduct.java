package com.autodesk.bsm.pelican.api.pojos.bicrelease;

import java.util.HashSet;
import java.util.Set;

/**
 * Pojo that contains the productLineCode and a list of eligible products
 *
 * @author yin
 */
public class ProductLineEligibleProduct {

    private String productLineCode;
    private String productLineName;
    private Set<EligibleProduct> eligibleProducts = new HashSet<>();

    public String getProductLineCode() {
        return productLineCode;
    }

    public void setProductLineCode(final String productLineCode) {
        this.productLineCode = productLineCode;
    }

    public Set<EligibleProduct> getEligibleProducts() {
        return eligibleProducts;
    }

    public void setEligibleProducts(final Set<EligibleProduct> eligibleProducts) {
        this.eligibleProducts = eligibleProducts;
    }

    public boolean addEligibleProduct(final EligibleProduct product) {
        return eligibleProducts.add(product);
    }

    public String getProductLineName() {
        return productLineName;
    }

    public void setProductLineName(final String productLineName) {
        this.productLineName = productLineName;
    }
}
