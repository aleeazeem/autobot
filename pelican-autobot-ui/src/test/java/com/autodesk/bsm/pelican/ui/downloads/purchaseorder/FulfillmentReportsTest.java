package com.autodesk.bsm.pelican.ui.downloads.purchaseorder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Every.everyItem;

import com.autodesk.bsm.pelican.api.pojos.purchaseorder.FulfillmentGroup.FulFillmentStatus;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.generic.SeleniumWebdriver;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.reports.FulfillmentReportsPage;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.DateTimeUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Admin Tool's Fulfillment Reports tests.
 * <p>
 * Validate the correct header and data text are in csv format. This test will run in AUTO application family for
 * BIC-STORE-DEV and DEV_NEW
 *
 * @author sunitha
 */
public class FulfillmentReportsTest extends SeleniumWebdriver {

    private AdminToolPage adminToolPage;
    private final String ORDER_FULFILLMENT_STATUS = "Fulfillment Group Status";

    @BeforeClass(alwaysRun = true)
    public void classSetup() {

        initializeDriver(getEnvironmentVariables());
        adminToolPage = new AdminToolPage(getDriver(), getEnvironmentVariables());
        adminToolPage.login();

    }

    /**
     * Method to generate Successful Fulfillment report
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testFulfillmentReportWithSuccessfulFulfillmentStatus() {
        final FulfillmentReportsPage reportPage = adminToolPage.getPage(FulfillmentReportsPage.class);
        // Set Successful fulfillment order after and before date.
        reportPage.setOrderAfterDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        reportPage.setOrderBeforeDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        // Check only Successful Fulfillment check box keeping other statuses
        // unchecked
        reportPage.activateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox();
        reportPage.deactivateOptionsIncludeFailedFulfillmentGroupsCheckbox();
        reportPage.deactivateOptionsIncludePendingFulfillmentGroupsCheckbox();
        reportPage.clickGenerateReport();

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);

        // Validate order fulfillment status as "FULFILLED".
        final List<String> actualOfferingStatus = grid.getColumnValues(ORDER_FULFILLMENT_STATUS);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>FULFILLED status</b>",
            actualOfferingStatus, everyItem(equalTo(FulFillmentStatus.FULFILLED.toString())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to generate Failed Fulfillment report
     *
     * @result Valid header and data is returned
     */
    @Test
    public void testFulfillmentReportWithFailedFulfillmentStatus() {
        final FulfillmentReportsPage reportPage = adminToolPage.getPage(FulfillmentReportsPage.class);
        // Set Successful fulfillment order after and before date.
        reportPage.setOrderAfterDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        reportPage.setOrderBeforeDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        // Check only Failed Fulfillment check box keeping other statuses
        // unchecked
        reportPage.deactivateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox();
        reportPage.activateOptionsIncludeFailedFulfillmentGroupsCheckbox();
        reportPage.deactivateOptionsIncludePendingFulfillmentGroupsCheckbox();
        reportPage.clickGenerateReport();

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);

        // Validate order fulfillment status as "FAILED".
        final List<String> actualOfferingStatus = grid.getColumnValues(ORDER_FULFILLMENT_STATUS);
        AssertCollector.assertThat("AdminTool: Found offerings other than <b>FAILED status</b>", actualOfferingStatus,
            everyItem(equalTo(FulFillmentStatus.FAILED.toString())), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to generate Pending Fulfillment report with Filter as Hours and Days * @result Valid header and data is
     * returned
     */
    @Test
    public void testFulfillmentReportWithPendingFulfillmentStatusWithHoursFilter() {
        final FulfillmentReportsPage reportPage = adminToolPage.getPage(FulfillmentReportsPage.class);
        // Set Successful fulfillment order after and before date.
        reportPage.setOrderAfterDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        reportPage.setOrderBeforeDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        // Check only Pending Fulfillment check box keeping other statuses
        // unchecked
        reportPage.deactivateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox();
        reportPage.deactivateOptionsIncludeFailedFulfillmentGroupsCheckbox();
        reportPage.activateOptionsIncludePendingFulfillmentGroupsCheckbox();

        // Select pending duration and pending units drop down.
        reportPage.setPendingDuration("6");
        reportPage.selectPendingUnits("hour(s)");
        reportPage.clickGenerateReport();

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);

        // Validate order fulfillment status as "PENDING".
        final List<String> actualOfferingStatus = grid.getColumnValues(ORDER_FULFILLMENT_STATUS);
        for (final String status : actualOfferingStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found offerings other than <b>PENDING status</b>", status,
                    equalTo(FulFillmentStatus.PENDING.toString()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to generate Pending Fulfillment report with Filter as Minutes * @result Valid header and data is returned
     */
    @Test
    public void testFulfillmentReportWithPendingFulfillmentStatusWithMinutesFilter() {
        final FulfillmentReportsPage reportPage = adminToolPage.getPage(FulfillmentReportsPage.class);
        // Set Successful fulfillment order after and before date.
        reportPage.setOrderAfterDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        reportPage.setOrderBeforeDate(DateTimeUtils.getNowAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH));

        // Check only Pending Fulfillment check box keeping other statuses
        // unchecked
        reportPage.deactivateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox();
        reportPage.deactivateOptionsIncludeFailedFulfillmentGroupsCheckbox();
        reportPage.activateOptionsIncludePendingFulfillmentGroupsCheckbox();

        // Select pending duration and pending units drop down.
        reportPage.setPendingDuration("30");
        reportPage.selectPendingUnits("minute(s)");
        reportPage.clickGenerateReport();

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);

        // Validate order fulfillment status as "PENDING".
        final List<String> actualOfferingStatus = grid.getColumnValues(ORDER_FULFILLMENT_STATUS);
        for (final String status : actualOfferingStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found offerings other than <b>PENDING status</b>", status,
                    equalTo(FulFillmentStatus.PENDING.toString()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Method to generate Pending Fulfillment report with Filter as Days * @result Valid header and data is returned
     */
    @Test
    public void testFulfillmentReportWithPendingFulfillmentStatusWithDaysFilter() {
        final FulfillmentReportsPage reportPage = adminToolPage.getPage(FulfillmentReportsPage.class);
        // Set Successful fulfillment order after and before date.
        reportPage.setOrderAfterDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));
        reportPage.setOrderBeforeDate(DateTimeUtils.getNowMinusDays(PelicanConstants.DATE_FORMAT_WITH_SLASH, 1));

        // Check only Pending Fulfillment check box keeping other statuses
        // unchecked
        reportPage.deactivateOptionsIncludeSuccessfulFulfillmentGroupsCheckbox();
        reportPage.deactivateOptionsIncludeFailedFulfillmentGroupsCheckbox();
        reportPage.activateOptionsIncludePendingFulfillmentGroupsCheckbox();

        // Select pending duration and pending units drop down.
        reportPage.setPendingDuration("1");
        reportPage.selectPendingUnits("day(s)");
        reportPage.clickGenerateReport();

        final GenericGrid grid = adminToolPage.getPage(GenericGrid.class);

        // Validate order fulfillment status as "PENDING".
        final List<String> actualOfferingStatus = grid.getColumnValues(ORDER_FULFILLMENT_STATUS);
        for (final String status : actualOfferingStatus) {
            if (!status.isEmpty()) {
                AssertCollector.assertThat("AdminTool: Found offerings other than <b>PENDING status</b>", status,
                    equalTo(FulFillmentStatus.PENDING.toString()), assertionErrorList);
            }
        }
        AssertCollector.assertAll(assertionErrorList);
    }

}
