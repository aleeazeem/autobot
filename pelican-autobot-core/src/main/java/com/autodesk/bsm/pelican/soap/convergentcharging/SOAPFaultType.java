//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation,
// v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.02.01 at 10:17:03 PM PST
//

package com.autodesk.bsm.pelican.soap.convergentcharging;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for SOAPFaultType complex type.
 * <p>
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;complexType name="SOAPFaultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ErrorCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         &lt;element name="ErrorMsg" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ErrorType" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SOAPFaultType", propOrder = { "errorCode", "errorMsg", "errorType" })
public class SOAPFaultType {

    @XmlElement(name = "ErrorCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String errorCode;
    @XmlElement(name = "ErrorMsg", required = true)
    private String errorMsg;
    @XmlElement(name = "ErrorType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String errorType;

    /**
     * Gets the value of the errorCode property.
     *
     * @return possible object is {@link String }
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     *
     * @param value allowed object is {@link String }
     */
    public void setErrorCode(final String value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the errorMsg property.
     *
     * @return possible object is {@link String }
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Sets the value of the errorMsg property.
     *
     * @param value allowed object is {@link String }
     */
    public void setErrorMsg(final String value) {
        this.errorMsg = value;
    }

    /**
     * Gets the value of the errorType property.
     *
     * @return possible object is {@link String }
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Sets the value of the errorType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setErrorType(final String value) {
        this.errorType = value;
    }

}