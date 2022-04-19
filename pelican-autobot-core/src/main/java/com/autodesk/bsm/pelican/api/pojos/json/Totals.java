package com.autodesk.bsm.pelican.api.pojos.json;

import com.autodesk.bsm.pelican.enums.Currency;

public class Totals {

    private Currency currency;
    private String subtotal;
    private String subtotalAfterPromotions;
    private String discount;
    private String unitPriceAfterPromotions;
    private String unitPriceAfterPromotionsWithoutProration;
    private String quantity;
    private String taxes;
    private String shipping;
    private String total;
    private String subtotalWithTax;
    private String subtotalAfterPromotionsWithTax;
    private String prorationDays;
    private String unitPriceAmount;
    private String unitPriceDiscount;
    private String unitPriceAfterDiscount;

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(final String subtotal) {
        this.subtotal = subtotal;
    }

    public String getSubtotalAfterPromotions() {
        return subtotalAfterPromotions;
    }

    public void setSubtotalAfterPromotions(final String subtotalAfterPromotions) {
        this.subtotalAfterPromotions = subtotalAfterPromotions;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(final String discount) {
        this.discount = discount;
    }

    public String getUnitPriceAfterPromotions() {
        return unitPriceAfterPromotions;
    }

    public void setUnitPriceAfterPromotions(final String unitPriceAfterPromotions) {
        this.unitPriceAfterPromotions = unitPriceAfterPromotions;
    }

    public String getUnitPriceAfterPromotionsWithoutProration() {
        return unitPriceAfterPromotionsWithoutProration;
    }

    public void setUnitPriceAfterPromotionsWithoutProration(final String unitPriceAfterPromotionsWithoutProration) {
        this.unitPriceAfterPromotionsWithoutProration = unitPriceAfterPromotionsWithoutProration;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(final String quantity) {
        this.quantity = quantity;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(final String taxes) {
        this.taxes = taxes;
    }

    public String getShipping() {
        return shipping;
    }

    public void setShipping(final String shipping) {
        this.shipping = shipping;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(final String total) {
        this.total = total;
    }

    public String getSubtotalWithTax() {
        return subtotalWithTax;
    }

    public void setSubtotalWithTax(final String subtotalWithTax) {
        this.subtotalWithTax = subtotalWithTax;
    }

    public String getSubtotalAfterPromotionsWithTax() {
        return subtotalAfterPromotionsWithTax;
    }

    public void setSubtotalAfterPromotionsWithTax(final String subtotalAfterPromotionsWithTax) {
        this.subtotalAfterPromotionsWithTax = subtotalAfterPromotionsWithTax;
    }

    /**
     * @return the prorationDays
     */
    public String getProrationDays() {
        return prorationDays;
    }

    /**
     * @param prorationDays the prorationDays to set
     */
    public void setProrationDays(final String prorationDays) {
        this.prorationDays = prorationDays;
    }

    public String getUnitPriceAmount() {
        return unitPriceAmount;
    }

    public void setUnitPriceAmount(final String unitPriceAmount) {
        this.unitPriceAmount = unitPriceAmount;
    }

    public String getUnitPriceDiscount() {
        return unitPriceDiscount;
    }

    public void setUnitPriceDiscount(final String unitPriceDiscount) {
        this.unitPriceDiscount = unitPriceDiscount;
    }

    public String getUnitPriceAfterDiscount() {
        return unitPriceAfterDiscount;
    }

    public void setUnitPriceAfterDiscount(final String unitPriceAfterDiscount) {
        this.unitPriceAfterDiscount = unitPriceAfterDiscount;
    }
}
