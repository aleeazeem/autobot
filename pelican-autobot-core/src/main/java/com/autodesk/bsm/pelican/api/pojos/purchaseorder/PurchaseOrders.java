package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is Pojo element of Purchase Orders
 *
 * @author Shweta Hegde
 */
@XmlRootElement(name = "purchaseOrders")
public class PurchaseOrders extends PelicanPojo {

    private List<PurchaseOrder> purchaseOrdersList;
    private int startIndex;
    private int blockSize;
    private int total;

    public List<PurchaseOrder> getPurchaseOrders() {
        return purchaseOrdersList;
    }

    @XmlElement(name = "purchaseOrder")
    public void setPurchaseOrders(final List<PurchaseOrder> purchaseOrdersList) {
        this.purchaseOrdersList = purchaseOrdersList;
    }

    public int getStartIndex() {
        return startIndex;
    }

    @XmlAttribute(name = "startIndex")
    public void setStartIndex(final int value) {
        this.startIndex = value;
    }

    public int getBlockSize() {
        return blockSize;
    }

    @XmlAttribute(name = "blockSize")
    public void setBlockSize(final int value) {
        this.blockSize = value;
    }

    public int getTotal() {
        return total;
    }

    @XmlAttribute(name = "total")
    public void setTotal(final int value) {
        this.total = value;
    }
}
