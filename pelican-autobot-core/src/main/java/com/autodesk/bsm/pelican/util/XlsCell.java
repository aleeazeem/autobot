package com.autodesk.bsm.pelican.util;

public class XlsCell {

    private int rowIndex;
    private int columnIndex;

    public XlsCell(final int rowIndex, final int columnIndex) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}
