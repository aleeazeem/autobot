package com.autodesk.bsm.pelican.ui.financereport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.autodesk.bsm.pelican.constants.FinanceReportHeaderConstants;
import com.autodesk.bsm.pelican.util.AssertCollector;

import java.util.List;

public class HelperForFinanceReport {

    public static void assertionsForFinanceReport(final List<Integer> poRecordsList, final String purchaseOrderId,
        final List<String> reportData, final List<AssertionError> assertionErrorList) {

        for (final Integer aPoRecordsList : poRecordsList) {
            final String requiredData = reportData.get(aPoRecordsList);
            final String[] dataArray = requiredData.split(",");

            // Validate correct data is returned
            AssertCollector.assertThat("Incorrect purchase order id", dataArray[0], equalTo(purchaseOrderId),
                assertionErrorList);
            AssertCollector.assertThat("Incorrect last modified date",
                dataArray[FinanceReportHeaderConstants.LAST_MODIFIED_DATE_POSITION], is(notNullValue()),
                assertionErrorList);
        }
    }
}
