package com.autodesk.bsm.pelican.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinanceReportUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceReportUtils.class.getSimpleName());

    /**
     * @return a string value in the list which corresponds the columname Given the single report row and a header row
     *         as strings. It finds the string which belongs to the column from the report row
     */
    public static String getColumnValueFromList(final String fullLine, final String allHeaders,
        final String columnName) {

        // get column index for order date
        int columnIndex = -1;
        final String[] columns = allHeaders.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }
        LOGGER.info("Column Header list : \n" + allHeaders);
        if (columnIndex < 0) {
            throw new RuntimeException("Unable to find header '" + columnName + "' in Finance Report\n");
        }

        // get values from the report row by splitting using a ","
        final String[] rowData = fullLine.split(",", -1);
        // Find the value from the list which
        // belongs to the column name index
        final String value = rowData[columnIndex];
        LOGGER.info("The Report Values :" + value);
        return value;
    }
}
