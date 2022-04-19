package com.autodesk.bsm.pelican.api.pojos.item;

import com.autodesk.bsm.pelican.api.pojos.PelicanPojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is to set and get page objects of Items
 *
 * @author Shweta Hegde
 */
@XmlRootElement(name = "items")
public class Items extends PelicanPojo {

    private List<Item> itemsList;
    private int startIndex;
    private int blockSize;
    private int total;

    @XmlElement(name = "item")
    public void setItems(final List<Item> itemsList) {
        this.itemsList = itemsList;
    }

    public List<Item> getItems() {
        return itemsList;
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
