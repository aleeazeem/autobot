package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a page object for Find BasicOffering Page This class works for - Find Basic Offering By Id - Find Basic
 * Offering By External Key - Find Basic Offering By Advanced Find with multiple filters
 *
 * @author t_mohag
 */
public class FindBasicOfferingPage extends GenericDetails {

    public FindBasicOfferingPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = ".form-group-labels > h3:nth-child(3)")
    private WebElement findByAdvancedFindTab;

    @FindBy(id = "offeringType")
    private WebElement offeringTypeSelect;

    @FindBy(id = "offeringDetailId")
    private WebElement offeringDetailSelect;

    @FindBy(id = "usageType")
    private WebElement usageTypeSelect;

    @FindBy(name = "includeNew")
    private WebElement newStatusCheckbox;

    @FindBy(name = "includeActive")
    private WebElement activeStatusCheckBox;

    @FindBy(name = "includeCanceled")
    private WebElement canceledStatusCheckbox;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindBasicOfferingPage.class.getSimpleName());

    /**
     * Method to navigate to Find Basic Offering
     */
    private void navigateToFindBasicOffering() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BASIC_OFFERINGS.getForm() + "/findForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to navigate to Advanced Find Basic Offering
     */
    public void navigateToAdvancedFind() {
        navigateToFindBasicOffering();
        findByAdvancedFindTab.click();
    }

    /**
     * Find a Basic Offering by id
     *
     * @return GenericGrid
     */
    public GenericGrid findBasicOfferingById() {

        navigateToFindBasicOffering();
        submit(TimeConstants.ONE_SEC);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return super.getPage(GenericGrid.class);
    }

    /**
     * Find Basic Offering Id
     *
     * @return OfferingDetailsPage
     */
    public BasicOfferingDetailPage findBasicOfferingById(final String id) {

        navigateToFindBasicOffering();
        setId(id);
        submit(TimeConstants.ONE_SEC);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return super.getPage(BasicOfferingDetailPage.class);
    }

    /**
     * Find a Basic Offering by external key
     *
     * @return OfferingDetailsPage
     */
    public BasicOfferingDetailPage findBasicOfferingByExternalKey(final String externalKey) {
        navigateToFindBasicOffering();
        clickOnFindByExternalKeyLink();
        setExternalKey(externalKey);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        submit(1);
        return super.getPage(BasicOfferingDetailPage.class);
    }

    /**
     * Find a Basic Offering by Advanced Find
     *
     * @return GenericGrid
     */
    public GenericGrid findBasicOfferingByAdvancedFind(final String productLine, final String offeringType,
        final String offeringDetail, final Boolean isNewSelected, final Boolean isActiveSelected,
        final Boolean isCanceledSelected) {

        navigateToFindBasicOffering();
        clickOnFindAdvancedFindButton();
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        // select filters
        selectProductLine(productLine);
        selectOfferingType(offeringType);
        selectOfferingDetail(offeringDetail);

        // select/unselect the status checkboxes
        if (isNewSelected) {
            checkNewStatusCheckbox();
        } else {
            uncheckNewStatusCheckbox();
        }
        if (isActiveSelected) {
            checkActiveStatusCheckbox();
        } else {
            uncheckActiveStatusCheckbox();
        }
        if (isCanceledSelected) {
            checkCanceledStatusCheckbox();
        } else {
            uncheckCanceledStatusCheckbox();
        }

        submit(2);
        Util.waitInSeconds(TimeConstants.TWO_SEC);

        return super.getPage(GenericGrid.class);
    }

    /**
     * This is a method where after doing advanced search, it will select the row which is sent
     */
    public BasicOfferingDetailPage selectResultRowWithAdvancedFind(final int row, final String productLine,
        final String offeringType, final String offeringDetail, final Boolean isNewSelected,
        final Boolean isActiveSelected, final Boolean isCanceledSelected) {

        final GenericGrid genericGrid = findBasicOfferingByAdvancedFind(productLine, offeringType, offeringDetail,
            isNewSelected, isActiveSelected, isCanceledSelected);
        genericGrid.selectResultRow(row);
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        return super.getPage(BasicOfferingDetailPage.class);
    }

    /**
     * Click on the Advanced Find tab of the Basic Offering
     */
    private void clickOnFindAdvancedFindButton() {
        findByAdvancedFindTab.click();
    }

    /**
     * This method is to select offering type if it is not null
     */
    private void selectOfferingType(final String offeringType) {

        if (offeringType != null) {
            getActions().select(offeringTypeSelect, offeringType);
        }
    }

    /**
     * This method is to select offering detail if it is not null
     */
    private void selectOfferingDetail(final String offeringDetail) {

        if (offeringDetail != null) {
            getActions().select(offeringDetailSelect, offeringDetail);
        }
    }

    /**
     * This method is to select usage type if it is not null
     */
    public void selectUsageType(final String usageType) {

        if (usageType != null) {
            getActions().select(usageTypeSelect, usageType);
        }
    }

    /**
     * Select NEW status checkbox, if not selected in the advanced find of the Basic Offering
     */
    private void checkNewStatusCheckbox() {
        getActions().check(newStatusCheckbox);
    }

    /**
     * Select ACTIVE status checkbox, if not selected in the advanced find of the Basic Offering
     */
    private void checkActiveStatusCheckbox() {
        getActions().check(activeStatusCheckBox);
    }

    /**
     * Select CANCELED status checkbox, if not selected in the advanced find of the Basic Offering
     */
    private void checkCanceledStatusCheckbox() {
        getActions().check(canceledStatusCheckbox);
    }

    /**
     * UnSelect NEW status checkbox, if not selected in the advanced find of the Basic Offering
     */
    private void uncheckNewStatusCheckbox() {

        getActions().uncheck(newStatusCheckbox);
    }

    /**
     * Unselect ACTIVE status checkbox, if not selected in the advanced find of the Basic Offering
     */
    private void uncheckActiveStatusCheckbox() {
        getActions().uncheck(activeStatusCheckBox);
    }

    /**
     * UnSelect CANCELED status checkbox, if not selected in the advanced find of the Basic Offering
     */
    private void uncheckCanceledStatusCheckbox() {
        getActions().uncheck(canceledStatusCheckbox);
    }
}
