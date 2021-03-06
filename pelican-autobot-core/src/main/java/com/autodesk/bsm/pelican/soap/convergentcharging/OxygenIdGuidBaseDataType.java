//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation,
// v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.02.01 at 10:17:03 PM PST
//

package com.autodesk.bsm.pelican.soap.convergentcharging;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for OxygenIdGuidBaseDataType complex type.
 * <p>
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;complexType name="OxygenIdGuidBaseDataType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.autodesk.com/schemas/Business/ConvergentChargingV1.0}ContractBaseDataType">
 *       &lt;sequence>
 *         &lt;element name="Guid" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="OxygenId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OxygenIdGuidBaseDataType", propOrder = { "guid", "oxygenId" })
public class OxygenIdGuidBaseDataType extends ContractBaseDataType {

    @XmlElementRef(name = "Guid", type = JAXBElement.class, required = false)
    private JAXBElement<String> guid;
    @XmlElementRef(name = "OxygenId", type = JAXBElement.class, required = false)
    private JAXBElement<String> oxygenId;

    /**
     * Gets the value of the guid property.
     *
     * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     *
     * @param value allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setGuid(final JAXBElement<String> value) {
        this.guid = value;
    }

    /**
     * Gets the value of the oxygenId property.
     *
     * @return possible object is {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public JAXBElement<String> getOxygenId() {
        return oxygenId;
    }

    /**
     * Sets the value of the oxygenId property.
     *
     * @param value allowed object is {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public void setOxygenId(final JAXBElement<String> value) {
        this.oxygenId = value;
    }

}
