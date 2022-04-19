package com.autodesk.bsm.pelican.api.pojos;

import javax.xml.bind.annotation.XmlAttribute;

public class BillingDate {

    private boolean custom;
    private String month;
    private String day;
    private String customDate;

    public boolean isCustom() {
        return custom;
    }

    @XmlAttribute
    public void setCustom(final boolean custom) {
        this.custom = custom;
    }

    public String getDay() {
        return day;
    }

    public void setDay(final String day) {
        this.day = day;
    }

    public String getCustomDate() {
        return customDate;
    }

    public void setCustomDate(final String customDate) {
        this.customDate = customDate;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(final String month) {
        this.month = month;
    }

}
