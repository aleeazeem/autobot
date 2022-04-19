package com.autodesk.bsm.pelican.ui.entities;

public class PromotionReport extends BaseEntity {
    private String promotionId;
    private String promotionCode;
    private String storeWide;
    private String promotionType;
    private String[] promotionState;
    private String viewDownload;

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(final String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(final String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getStoreWide() {
        return storeWide;
    }

    public void setStoreWide(final String storeWide) {
        this.storeWide = storeWide;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(final String promotionType) {
        this.promotionType = promotionType;
    }

    public String[] getPromotionState() {
        return promotionState;
    }

    public void setPromotionState(final String[] promotionState) {
        this.promotionState = promotionState;
    }

    public String getViewDownload() {
        return viewDownload;
    }

    public void setViewDownload(final String viewDownload) {
        this.viewDownload = viewDownload;
    }

}
