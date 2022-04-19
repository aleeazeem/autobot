package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.api.pojos.Property;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * The pojo for properties entity for Submit PO request XML
 *
 * @author Shipra
 */
public class PurchaseOrderProperty {

    private List<Property> propertyList;

    public List<Property> getProperty() {
        return propertyList;
    }

    @XmlElement(name = "property")
    public void setProperty(final List<Property> propertyList) {
        this.propertyList = propertyList;
    }

}
