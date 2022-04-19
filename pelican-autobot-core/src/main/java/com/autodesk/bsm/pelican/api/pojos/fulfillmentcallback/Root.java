package com.autodesk.bsm.pelican.api.pojos.fulfillmentcallback;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a pojo class for <root> element in FulfillmentCallBack API request body
 *
 * @author kishor
 */
@XmlRootElement
public class Root extends PelicanPojo {

    private Order order;

    /**
     * @return the order
     */
    public Order getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    @XmlElement(name = "Order", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public void setOrder(final Order order) {
        this.order = order;
    }

    /**
     * This is a pojo for <order> entity in Fulfillment CallBack API request body
     *
     * @author kishor
     */
    @XmlRootElement(namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
    public static class Order {
        private String externalRefNumber;
        private String orderNumber;
        private String poDate;
        private String poNumber;
        private String status;
        private Contract contract;
        private ListOfLineItem listOfLineItem;
        private ShipmentData shipmentData;
        private ListOfMessage listOfMessage;

        /**
         * Method which get the externalRefNumber from Order entity in fulfillmentCallback API
         *
         * @return externalRefNumber String
         */
        public String getExternalRefNumber() {
            return externalRefNumber;
        }

        /**
         * Method which set the externalRefNumber to Order entity in fulfillmentCallback API
         */
        @XmlElement(name = "ExternalRefNumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setExternalRefNumber(final String externalRefNumber) {
            this.externalRefNumber = externalRefNumber;
        }

        /**
         * Method which get the orderNumber from Order entity in fulfillmentCallback API
         */
        public String getOrderNumber() {
            return orderNumber;
        }

        /**
         * Method which set the OrderNumber to Order entity in fulfillmentCallback API
         */
        @XmlElement(name = "OrderNumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setOrderNumber(final String orderNumber) {
            this.orderNumber = orderNumber;
        }

        /**
         * Method which get the PODate from Order entity in fulfillmentCallback API
         */
        public String getPODate() {
            return poDate;
        }

        /**
         * Method which set the PODate to Order entity in fulfillmentCallback API
         */
        @XmlElement(name = "PODate", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setPODate(final String poDate) {
            this.poDate = poDate;
        }

        /**
         * Method which get the PONumber from Order entity in fulfillmentCallback API
         */
        public String getPONumber() {
            return poNumber;
        }

        /**
         * Method which set the PONumber to Order entity in fulfillmentCallback API
         */
        @XmlElement(name = "PONumber", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setPONumber(final String poNumber) {
            this.poNumber = poNumber;
        }

        /**
         * Method which get the Status from Order entity in fulfillmentCallback API
         */
        public String getStatus() {
            return status;
        }

        /**
         * Method which set the Status to Order entity in fulfillmentCallback API
         */
        @XmlElement(name = "Status", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setStatus(final String status) {
            this.status = status;
        }

        /**
         * @return the listOfLineItem
         */
        public ListOfLineItem getListOfLineItem() {
            return listOfLineItem;
        }

        /**
         * @param listOfLineItem the listOfLineItem to set
         */
        @XmlElement(name = "ListOfLineItem", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setListOfLineItem(final ListOfLineItem listOfLineItem) {
            this.listOfLineItem = listOfLineItem;
        }

        /**
         * @return the contract
         */
        public Contract getContract() {
            return contract;
        }

        /**
         * @param contract the contract to set
         */
        @XmlElement(name = "Contract", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setContract(final Contract contract) {
            this.contract = contract;
        }

        /**
         * @return the shipmentData
         */
        public ShipmentData getShipmentData() {
            return shipmentData;
        }

        /**
         * @param shipmentData the shipmentData to set
         */
        @XmlElement(name = "ShipmentData", namespace = "http://www.autodesk.com/schemas/Business/OrderV1.0")
        public void setShipmentData(final ShipmentData shipmentData) {
            this.shipmentData = shipmentData;
        }

        /**
         * @return the listOfMessage
         */
        public ListOfMessage getListOfMessage() {
            return listOfMessage;
        }

        /**
         * @param listOfMessage the listOfMessage to set
         */
        public void setListOfMessage(final ListOfMessage listOfMessage) {
            this.listOfMessage = listOfMessage;
        }
    }
}
