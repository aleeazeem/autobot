package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Shweta Hegde on 8/1/17.
 */
public class LineItemTotals {

    private String currencyId;
    private String currencyName;
    private Double subtotal;
    private Double subtotalAfterPromotions;
    private Double promotionDiscount;
    private Double taxAmount;
    private Double unitPriceAfterPromotions;
    private Double unitPriceAfterDiscounts;
    private Double unitPriceAfterPromotionsWithoutProration;
    private Double unitPriceAmount;
    private Double creditDaysDiscount;
    private String promotionId;
    private Double subtotalWithTax;
    private Double subtotalAfterPromotionsWithTax;
    private String priceIdUsed;
    private int prorationDays;
    private Double unitPriceDiscount;
    private Double unitPriceAfterDiscount;

    public Double getSubtotal() {
        return subtotal;
    }

    @XmlElement(name = "subtotal")
    public void setSubtotal(final Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getSubtotalAfterPromotions() {
        return subtotalAfterPromotions;
    }

    @XmlElement(name = "subtotalAfterPromotions")
    public void setSubtotalAfterPromotions(final Double subtotalAfterPromotions) {
        this.subtotalAfterPromotions = subtotalAfterPromotions;
    }

    public Double getPromotionDiscount() {
        return promotionDiscount;
    }

    @XmlElement(name = "promotionDiscount")
    public void setPromotionDiscount(final Double promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    @XmlElement(name = "taxAmount")
    public void setTaxAmount(final Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    @XmlElement(name = "currencyId")
    public void setCurrencyId(final String currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    @XmlElement(name = "currencyName")
    public void setCurrencyName(final String currencyName) {
        this.currencyName = currencyName;
    }

    public Double getUnitPriceAfterPromotions() {
        return unitPriceAfterPromotions;
    }

    @XmlElement(name = "unitPriceAfterPromotions")
    public void setUnitPriceAfterPromotions(final Double unitPriceAfterPromotions) {
        this.unitPriceAfterPromotions = unitPriceAfterPromotions;
    }

    public Double getUnitPriceAfterDiscounts() {
        return unitPriceAfterDiscounts;
    }

    @XmlElement(name = "unitPriceAfterDiscounts")
    public void setUnitPriceAfterDiscounts(final Double unitPriceAfterDiscounts) {
        this.unitPriceAfterDiscounts = unitPriceAfterDiscounts;
    }

    public Double getUnitPriceAfterPromotionsWithoutProration() {
        return unitPriceAfterPromotionsWithoutProration;
    }

    @XmlElement(name = "unitPriceAfterPromotionsWithoutProration")
    public void setUnitPriceAfterPromotionsWithoutProration(final Double unitPriceAfterPromotionsWithoutProration) {
        this.unitPriceAfterPromotionsWithoutProration = unitPriceAfterPromotionsWithoutProration;
    }

    public Double getCreditDaysDiscount() {
        return creditDaysDiscount;
    }

    @XmlElement(name = "creditDaysDiscount")
    public void setCreditDaysDiscount(final Double creditDaysDiscount) {
        this.creditDaysDiscount = creditDaysDiscount;
    }

    public String getPromotionId() {
        return promotionId;
    }

    @XmlElement(name = "promotionId")
    public void setPromotionId(final String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPriceIdUsed() {
        return priceIdUsed;
    }

    @XmlElement(name = "priceIdUsed")
    public void setPriceIdUsed(final String priceIdUsed) {
        this.priceIdUsed = priceIdUsed;
    }

    public Double getSubtotalWithTax() {
        return subtotalWithTax;
    }

    @XmlElement(name = "subtotalWithTax")
    public void setSubtotalWithTax(final Double subtotalWithTax) {
        this.subtotalWithTax = subtotalWithTax;
    }

    public Double getSubtotalAfterPromotionsWithTax() {
        return subtotalAfterPromotionsWithTax;
    }

    @XmlElement(name = "subtotalAfterPromotionsWithTax")
    public void setSubtotalAfterPromotionsWithTax(final Double subtotalAfterPromotionsWithTax) {
        this.subtotalAfterPromotionsWithTax = subtotalAfterPromotionsWithTax;
    }

    public int getProrationDays() {
        return prorationDays;
    }

    @XmlElement(name = "prorationDays")
    public void setProrationDays(final int prorationDays) {
        this.prorationDays = prorationDays;
    }

    public Double getUnitPriceAmount() {
        return unitPriceAmount;
    }

    @XmlElement(name = "unitPriceAmount")
    public void setUnitPriceAmount(final Double unitPriceAmount) {
        this.unitPriceAmount = unitPriceAmount;
    }

    public Double getUnitPriceDiscount() {
        return unitPriceDiscount;
    }

    public void setUnitPriceDiscount(final Double unitPriceDiscount) {
        this.unitPriceDiscount = unitPriceDiscount;
    }

    public Double getUnitPriceAfterDiscount() {
        return unitPriceAfterDiscount;
    }

    public void setUnitPriceAfterDiscount(final Double unitPriceAfterDiscount) {
        this.unitPriceAfterDiscount = unitPriceAfterDiscount;
    }
}
