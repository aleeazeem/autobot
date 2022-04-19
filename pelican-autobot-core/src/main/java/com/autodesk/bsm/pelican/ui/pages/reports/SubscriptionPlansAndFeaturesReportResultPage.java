package com.autodesk.bsm.pelican.ui.pages.reports;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * This is the page object for Subscription Plan and Features Report result page.
 *
 * @author jains
 */
public class SubscriptionPlansAndFeaturesReportResultPage extends GenericGrid {

    public SubscriptionPlansAndFeaturesReportResultPage(final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    public List<String> getValuesFromPlanIdColumn() {
        return getColumnValues("Plan ID");
    }

    /**
     * Method to get Plan External Key
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanExternalKeyColumn() {
        return getColumnValues("Plan Ext Key");
    }

    /**
     * Method to get Plan Name
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanNameColumn() {
        return getColumnValues("Plan Name");
    }

    /**
     * Method to get Plan Status
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanStatusColumn() {
        return getColumnValues("Plan Status");
    }

    /**
     * Method to get Plan Usage Type
     *
     * @return List<String>
     */
    public List<String> getValuesFromPlanUsageTypeColumn() {
        return getColumnValues("Plan Usage Type");
    }

    /**
     * Method to get Product Line Code
     *
     * @return List<String>
     */
    public List<String> getValuesFromProductLineCodeColumn() {
        return getColumnValues("Product Line Code");
    }

    /**
     * Method to get Product Line Name
     *
     * @return List<String>
     */
    public List<String> getValuesFromProductLineNameColumn() {
        return getColumnValues("Product Line Name");
    }

    /**
     * Method to get Feature External Key
     *
     * @return List<String>
     */
    public List<String> getValuesFromFeatureExternalKeyColumn() {
        return getColumnValues("Feature Ext Key");
    }

    /**
     * Method to get Feature Name
     *
     * @return List<String>
     */
    public List<String> getValuesFromFeatureNameColumn() {
        return getColumnValues("Feature Name");
    }

    /**
     * Method to get Feature Type External Key
     *
     * @return List<String>
     */
    public List<String> getValuesFromFeatureTypeExternalKeyColumn() {
        return getColumnValues("Feature Type Ext Key");
    }

    /**
     * Method to get Feature Type Name
     *
     * @return List<String>
     */
    public List<String> getValuesFromFeatureTypeNameColumn() {
        return getColumnValues("Feature Type Name");
    }

    /**
     * Method to get Licensing Model
     *
     * @return List<String>
     */
    public List<String> getValuesFromLicensingModelColumn() {
        return getColumnValues("Licensing Model");
    }

    /**
     * Method to get Parent Feature Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromParentFeatureColumn() {
        return getColumnValues("Parent Feature");
    }

    /**
     * Method to get Parent Feature Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromAssignableColumn() {
        return getColumnValues("Assignable");
    }

    /**
     * Method to get Parent Feature Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromParentEOSDateColumn() {
        return getColumnValues("EOS Date");
    }

    /**
     * Method to get Parent Feature Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromParentEOLImmediateDateColumn() {
        return getColumnValues("EOL Immediate Date");
    }

    /**
     * Method to get Parent Feature Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromParentEOLRenewalDateColumn() {
        return getColumnValues("EOL Renewal Date");
    }

    /**
     * Method to get Offering Detail External Key Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferingDetailExternalKeyColumn() {
        return getColumnValues("Offering Detail Ext Key");
    }

    /**
     * Method to get Offering Detail Currency Name Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferingDetailCurrencyNameColumn() {
        return getColumnValues(PelicanConstants.CURRENCY_NAME_REPORT);
    }

    /**
     * Method to get Offering Detail SKU Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferingDetailSkuColumn() {
        return getColumnValues(PelicanConstants.SKU_REPORT);
    }

    /**
     * Method to get Offering Detail Amount Column
     *
     * @return List<String>
     */
    public List<String> getValuesFromOfferingDetailAmountColumn() {
        return getColumnValues(PelicanConstants.AMOUNT_REPORT);
    }
}
