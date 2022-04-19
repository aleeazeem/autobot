package com.autodesk.bsm.pelican.ui.pages.purchaseorder;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * This class is for Processing the Purchase Order. This class should contain only those webelements and methods, which
 * are done in this page.
 *
 * @author Shweta Hegde
 */
public class ProcessPurchaseOrderPage extends GenericDetails {

    @FindBy(id = "commandType")
    private WebElement commandTypeSelect;

    @FindBy(id = "finalExportControlStatus")
    private WebElement exportControlStatusSelect;

    @FindBy(id = "notes")
    private WebElement processPoNotesText;

    @FindBy(id = "storedPaymentProfileId")
    private WebElement sppSelect;

    @FindBy(className = "select-note")
    private WebElement selectSppNote;

    public ProcessPurchaseOrderPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
    }

    /**
     * This method selects multiple elements to process a PO.
     *
     * @param orderCommand
     * @param ecStatus
     * @param notes
     * @param spp
     */
    public void processPurchaseOrder(final String orderCommand, final String ecStatus, final String notes,
        final String spp) {
        selectOrderCommand(orderCommand);
        selectEcStatus(ecStatus);
        selectSpp(spp);
        addNotes(notes);
    }

    private void selectOrderCommand(final String orderCommand) {

        if (StringUtils.isNotEmpty(orderCommand)) {
            getActions().select(commandTypeSelect, orderCommand);
        }
    }

    private void selectEcStatus(final String ecStatus) {

        if (StringUtils.isNotEmpty(ecStatus)) {
            getActions().select(exportControlStatusSelect, ecStatus);
        }
    }

    private void selectSpp(final String spp) {

        if (StringUtils.isNotEmpty(spp)) {
            getActions().select(sppSelect, spp);
        }
    }

    private void addNotes(final String notes) {

        if (StringUtils.isNotEmpty(notes)) {
            getActions().setText(processPoNotesText, notes);
        }
    }

    /**
     * Click on confirm, which returns PurchaseOrderDetailPage, in success scenarios.
     *
     * @return PurchaseOrderDetailPage
     */
    public PurchaseOrderDetailPage clickOnConfirm() {
        submit();
        return super.getPage(PurchaseOrderDetailPage.class);
    }

    /**
     * Click on confirm, which returns the error page, in error scenarios.
     *
     * @return ProcessOrderErrorPage
     */
    public ProcessOrderErrorPage clickOnConfirmError() {
        submit();
        return super.getPage(ProcessOrderErrorPage.class);
    }

    /**
     * This method clicks on Cancel in Process po page and returns PurchaseOrderDetailPage.
     *
     * @return PurchaseOrderDetailPage
     */
    public PurchaseOrderDetailPage clickOnCancel() {
        cancelInEditPage();
        return super.getPage(PurchaseOrderDetailPage.class);
    }

    public String getSppNote() {
        return getActions().getText(selectSppNote);
    }

    public boolean isSppDefaultNoteVisible() {
        return doesWebElementExist(selectSppNote);
    }
}
