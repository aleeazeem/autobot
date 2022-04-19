package com.autodesk.bsm.pelican.cse;

public class Attributes {
    private String externalKey;
    private String productLine;
    private String offeringType;
    private String usageType;
    private String salesChannel;
    private String aggrementNumber;

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(final String externalKey) {
        this.externalKey = externalKey;
    }

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(final String productLine) {
        this.productLine = productLine;
    }

    public String getOfferingType() {
        return offeringType;
    }

    public void setOfferingType(String offeringType) {
        this.offeringType = offeringType;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getAggrementNumber() {
        return aggrementNumber;
    }

    public void setAggrementNumber(String aggrementNumber) {
        this.aggrementNumber = aggrementNumber;
    }
}
