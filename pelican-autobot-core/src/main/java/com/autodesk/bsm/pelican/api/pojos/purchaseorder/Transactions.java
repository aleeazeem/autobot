package com.autodesk.bsm.pelican.api.pojos.purchaseorder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Transactions {

    private List<Transaction> transactions;

    public List<Transaction> getTransactions() {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        return transactions;
    }

    @XmlElement(name = "transaction")
    public void setTransactions(final List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
