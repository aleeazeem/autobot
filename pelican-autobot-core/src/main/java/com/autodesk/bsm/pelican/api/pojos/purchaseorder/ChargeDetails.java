package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Vineel Reddy
 */
public class ChargeDetails {

    private float amountCharged;
    private float creditDaysDiscount;
    private float promotionDiscount;
    private int quantity;
    private float totalPrice;
    private String unitPrice;
    private int prorationDays;

    public float getAmountCharged() {
        return amountCharged;
    }

    @XmlElement(name = "amountCharged")
    public void setAmountCharged(final float amountCharged) {
        this.amountCharged = amountCharged;
    }

    public int getQuantity() {
        return quantity;
    }

    @XmlElement(name = "quantity")
    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    @XmlElement(name = "totalPrice")
    public void setTotalPrice(final float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    @XmlElement(name = "unitPrice")
    public void setUnitPrice(final String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public float getCreditDaysDiscount() {
        return creditDaysDiscount;
    }

    @XmlElement(name = "creditDaysDiscount")
    public void setCreditDaysDiscount(final float creditDaysDiscount) {
        this.creditDaysDiscount = creditDaysDiscount;
    }

    public float getPromotionDiscount() {
        return promotionDiscount;
    }

    @XmlElement(name = "promotionDiscount")
    public void setPromotionDiscount(final float promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public int getProrationDays() {
        return prorationDays;
    }

    @XmlElement(name = "prorationDays")
    public void setProrationDays(final int prorationDays) {
        this.prorationDays = prorationDays;
    }
}
