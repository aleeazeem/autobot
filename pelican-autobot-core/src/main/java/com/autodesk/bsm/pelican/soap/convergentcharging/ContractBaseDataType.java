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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for ContractBaseDataType complex type.
 * <p>
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;complexType name="ContractBaseDataType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.autodesk.com/schemas/Business/ConvergentChargingV1.0}ContractRefDataType">
 *       &lt;sequence>
 *         &lt;element name="ContractEndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="ContractStartDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="OrderNumber" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="PONumber" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ContractStatus" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ContractSubType" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ContractTerm" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ContractType" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ContractUsageType" type="{http://www.autodesk.com/schemas/Business/ConvergentChargingV1.0}UsageTypeEnum" minOccurs="0"/>
 *         &lt;element name="ContractOfferingType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsNewContract" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsRenewContract" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="OrderType" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="POType" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="RatePlan" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContractBaseDataType",
    propOrder = { "contractEndDate", "contractStartDate", "orderNumber", "poNumber", "contractStatus",
            "contractSubType", "contractTerm", "contractType", "contractUsageType", "contractOfferingType",
            "isNewContract", "isRenewContract", "orderType", "poType", "ratePlan" })
@XmlSeeAlso({ OxygenIdGuidBaseDataType.class })
public class ContractBaseDataType extends ContractRefDataType {

    @XmlElement(name = "ContractEndDate")
    @XmlSchemaType(name = "date")
    private XMLGregorianCalendar contractEndDate;
    @XmlElement(name = "ContractStartDate")
    @XmlSchemaType(name = "date")
    private XMLGregorianCalendar contractStartDate;
    @XmlElement(name = "OrderNumber")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String orderNumber;
    @XmlElement(name = "PONumber")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String poNumber;
    @XmlElement(name = "ContractStatus")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String contractStatus;
    @XmlElement(name = "ContractSubType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String contractSubType;
    @XmlElement(name = "ContractTerm")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String contractTerm;
    @XmlElement(name = "ContractType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String contractType;
    @XmlElement(name = "ContractUsageType")
    @XmlSchemaType(name = "string")
    private UsageTypeEnum contractUsageType;
    @XmlElement(name = "ContractOfferingType")
    private String contractOfferingType;
    @XmlElement(name = "IsNewContract")
    private Boolean isNewContract;
    @XmlElement(name = "IsRenewContract")
    private Boolean isRenewContract;
    @XmlElement(name = "OrderType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String orderType;
    @XmlElement(name = "POType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String poType;
    @XmlElement(name = "RatePlan")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    private String ratePlan;

    /**
     * Gets the value of the contractEndDate property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getContractEndDate() {
        return contractEndDate;
    }

    /**
     * Sets the value of the contractEndDate property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setContractEndDate(final XMLGregorianCalendar value) {
        this.contractEndDate = value;
    }

    /**
     * Gets the value of the contractStartDate property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getContractStartDate() {
        return contractStartDate;
    }

    /**
     * Sets the value of the contractStartDate property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setContractStartDate(final XMLGregorianCalendar value) {
        this.contractStartDate = value;
    }

    /**
     * Gets the value of the orderNumber property.
     *
     * @return possible object is {@link String }
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * Sets the value of the orderNumber property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOrderNumber(final String value) {
        this.orderNumber = value;
    }

    /**
     * Gets the value of the poNumber property.
     *
     * @return possible object is {@link String }
     */
    public String getPONumber() {
        return poNumber;
    }

    /**
     * Sets the value of the poNumber property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPONumber(final String value) {
        this.poNumber = value;
    }

    /**
     * Gets the value of the contractStatus property.
     *
     * @return possible object is {@link String }
     */
    public String getContractStatus() {
        return contractStatus;
    }

    /**
     * Sets the value of the contractStatus property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContractStatus(final String value) {
        this.contractStatus = value;
    }

    /**
     * Gets the value of the contractSubType property.
     *
     * @return possible object is {@link String }
     */
    public String getContractSubType() {
        return contractSubType;
    }

    /**
     * Sets the value of the contractSubType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContractSubType(final String value) {
        this.contractSubType = value;
    }

    /**
     * Gets the value of the contractTerm property.
     *
     * @return possible object is {@link String }
     */
    public String getContractTerm() {
        return contractTerm;
    }

    /**
     * Sets the value of the contractTerm property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContractTerm(final String value) {
        this.contractTerm = value;
    }

    /**
     * Gets the value of the contractType property.
     *
     * @return possible object is {@link String }
     */
    public String getContractType() {
        return contractType;
    }

    /**
     * Sets the value of the contractType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContractType(final String value) {
        this.contractType = value;
    }

    /**
     * Gets the value of the contractUsageType property.
     *
     * @return possible object is {@link UsageTypeEnum }
     */
    public UsageTypeEnum getContractUsageType() {
        return contractUsageType;
    }

    /**
     * Sets the value of the contractUsageType property.
     *
     * @param value allowed object is {@link UsageTypeEnum }
     */
    public void setContractUsageType(final UsageTypeEnum value) {
        this.contractUsageType = value;
    }

    /**
     * Gets the value of the contractOfferingType property.
     *
     * @return possible object is {@link String }
     */
    public String getContractOfferingType() {
        return contractOfferingType;
    }

    /**
     * Sets the value of the contractOfferingType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContractOfferingType(final String value) {
        this.contractOfferingType = value;
    }

    /**
     * Gets the value of the isNewContract property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsNewContract() {
        return isNewContract;
    }

    /**
     * Sets the value of the isNewContract property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsNewContract(final Boolean value) {
        this.isNewContract = value;
    }

    /**
     * Gets the value of the isRenewContract property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsRenewContract() {
        return isRenewContract;
    }

    /**
     * Sets the value of the isRenewContract property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsRenewContract(final Boolean value) {
        this.isRenewContract = value;
    }

    /**
     * Gets the value of the orderType property.
     *
     * @return possible object is {@link String }
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * Sets the value of the orderType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOrderType(final String value) {
        this.orderType = value;
    }

    /**
     * Gets the value of the poType property.
     *
     * @return possible object is {@link String }
     */
    public String getPOType() {
        return poType;
    }

    /**
     * Sets the value of the poType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPOType(final String value) {
        this.poType = value;
    }

    /**
     * Gets the value of the ratePlan property.
     *
     * @return possible object is {@link String }
     */
    public String getRatePlan() {
        return ratePlan;
    }

    /**
     * Sets the value of the ratePlan property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRatePlan(final String value) {
        this.ratePlan = value;
    }

}
