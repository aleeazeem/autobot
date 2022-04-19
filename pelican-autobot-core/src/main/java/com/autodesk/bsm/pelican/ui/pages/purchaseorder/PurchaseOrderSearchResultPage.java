package com.autodesk.bsm.pelican.ui.pages.purchaseorder;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Page class for Purchase Order Search Result Page.
 *
 * @author t_joshv
 *
 */
public class PurchaseOrderSearchResultPage extends GenericGrid {
    public PurchaseOrderSearchResultPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Method to select given row and navigate to Purchase Order Detail Page.
     *
     * @param rowIndex
     * @return PurchaseOrderDetailPage.
     */
    public PurchaseOrderDetailPage selectResultRow(final int row) {
        final GenericGrid purchaseOrderSearchResultGrid = getPage(GenericGrid.class);
        purchaseOrderSearchResultGrid.selectResultRow(row);
        return getPage(PurchaseOrderDetailPage.class);
    }

    /**
     * Method to get all values in order type column as list
     *
     * @return List<String>
     */
    public List<String> getValuesFromOrderTypeColumn() {
        return getColumnValues(PelicanConstants.ORDER_TYPE);
    }
}
