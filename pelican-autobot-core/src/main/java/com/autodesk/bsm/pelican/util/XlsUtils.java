package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is a utility class for creating and entering values in Excel sheet
 *
 * @author Shweta Hegde
 */
public class XlsUtils {

    private XSSFSheet sheet;
    private String filePath = Util.getTestRootDir() + "/src/test/resources/testdata/";
    private static String downloadPath;
    private static final Logger LOGGER = LoggerFactory.getLogger(XlsUtils.class.getSimpleName());

    /**
     * This method creates Excel and writes data into it
     *
     * @param fileName - with .xlsx extension, ex : myFile.xlsx
     * @param columnHeaders - arraylist of string for column headers
     * @param columnData - arraylist of string column data
     * @param append - 'true' if append to existing file, 'false' if creating new file
     */
    public void createAndWriteToXls(final String fileName, final ArrayList<String> columnHeaders,
        final ArrayList<String> columnData, final boolean append) throws IOException {

        XSSFWorkbook workbook;
        if (!append) {
            // Create blank workbook
            workbook = new XSSFWorkbook();
            // Create a blank sheet
            sheet = workbook.createSheet();
            final int numberOfRows = sheet.getPhysicalNumberOfRows();
            // Added this method to add headers and data to excel with comma (,) based String
            writeExcel(columnHeaders, columnData, numberOfRows);

        } else {
            final FileInputStream fileInputStream = new FileInputStream(new File(filePath + fileName));
            // Get the workbook instance for XLSX file
            workbook = new XSSFWorkbook(fileInputStream);
            sheet = workbook.getSheetAt(0);
            final int numOfLastRow = sheet.getLastRowNum();
            // leaving 2 blank rows for cosmetic look of the file
            // Added this method to add headers and data to excel with comma (,) based String
            writeExcel(columnHeaders, columnData, numOfLastRow + 3);
            fileInputStream.close();
        }
        try {
            // Write the workbook in file system
            final FileOutputStream fileOut = new FileOutputStream(filePath + fileName);
            LOGGER.info(fileName + " is created in " + filePath + fileName);
            workbook.write(fileOut);
            fileOut.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to write excel with comma (,) based String.
     *
     * @param columnHeaders (column headers)
     * @param columnData (column data)
     * @param numOfRows (number of rows)
     */
    private void writeExcel(final ArrayList<String> columnHeaders, final ArrayList<String> columnData, int numOfRows) {

        // create a row for headers
        Row row = sheet.createRow(numOfRows);
        // insert headers in cells
        int numberOfColumns = 0;
        for (final String columnHeader : columnHeaders) {
            final String[] headers = columnHeader.split(",");
            for (final String header : headers) {
                final Cell cell = row.createCell(numberOfColumns++);
                cell.setCellValue(header);
            }
        }
        LOGGER.info("Column headers are written to workbook : " + columnHeaders);

        // insert data in cells below headers
        for (int i = 0; i < columnData.size() / columnHeaders.size(); i++) {
            row = sheet.createRow(++numOfRows);
            numberOfColumns = 0;
            final String[] cData = columnData.get(i).split(",");
            for (final String value : cData) {
                final Cell cell = row.createCell(numberOfColumns++);
                cell.setCellValue(value);
            }
        }
        LOGGER.info("Column data are written to workbook : " + columnData);
    }

    /*
     * Read the data from the xlsx file
     *
     * @param: FileName
     *
     * @Return: String[][] - Two Dimensional String array
     */
    public static String[][] readDataFromXlsx(final String fileName) throws IOException {
        final File excelFile = new File(fileName);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final FileInputStream fileInputStream = new FileInputStream(excelFile);
        // Create Workbook instance holding reference to .xlsx file
        final XSSFWorkbook workBook = new XSSFWorkbook(fileInputStream);
        final XSSFSheet sheet = workBook.getSheetAt(0);
        final int rowSize = sheet.getPhysicalNumberOfRows();
        final int colSize = sheet.getRow(sheet.getFirstRowNum()).getLastCellNum();
        final String[][] excelData = new String[rowSize][colSize];
        int i = 0;
        // Iterate through each rows one by one
        for (final Row row : sheet) {
            int j = 0;
            // For each row, iterate through all the columns
            final Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                final Cell cell = cellIterator.next();
                if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
                    excelData[i][j] = Double.toString(cell.getNumericCellValue());
                } else {
                    excelData[i][j] = cell.toString();
                }
                j++;
            }
            i++;
        }
        workBook.close();
        fileInputStream.close();
        return excelData;
    }

    /**
     * Get column value from xlsx file
     *
     * @param: fileName
     * @param: xlsCell
     * @return: string - column value
     */
    public static String getColumnValueFromXlsx(final String fileName, final XlsCell xlsCell) throws IOException {
        final File excelFile = new File(fileName);
        final FileInputStream fileInputStream = new FileInputStream(excelFile);
        // Create Workbook instance holding reference to .xlsx file
        final XSSFWorkbook workBook = new XSSFWorkbook(fileInputStream);
        final XSSFSheet sheet = workBook.getSheetAt(0);
        final XSSFRow row = sheet.getRow(xlsCell.getRowIndex());
        if (null != row) {
            final Cell cell = row.getCell(xlsCell.getColumnIndex());
            workBook.close();
            fileInputStream.close();
            return cell.getStringCellValue();
        }
        fileInputStream.close();
        return null;
    }

    /*
     * Get the number of rows in xlsx file
     *
     * @param: fileName
     *
     * @Return: int - number of rows in xlsx file
     */
    public static int getNumRowsInXlsx(final String fileName) throws IOException {
        final File excelFile = new File(fileName);
        final FileInputStream fileInputStream = new FileInputStream(excelFile);
        // Create Workbook instance holding reference to .xlsx file
        final XSSFWorkbook workBook = new XSSFWorkbook(fileInputStream);
        final XSSFSheet sheet = workBook.getSheetAt(0);
        workBook.close();
        fileInputStream.close();
        return sheet.getPhysicalNumberOfRows();
    }

    /**
     * Update the column values in xlsx file
     *
     * @param: fileName
     */
    public void updateColumnsInXlsx(final String fileName, final Map<XlsCell, String> columnValuesMap)
        throws IOException {
        final File excelFile = new File(fileName);
        final FileInputStream fileInputStream = new FileInputStream(excelFile);
        // Create Workbook instance holding reference to .xlsx file
        final XSSFWorkbook workBook = new XSSFWorkbook(fileInputStream);
        final XSSFSheet sheet = workBook.getSheetAt(0);

        // Update the columns in xlsx file
        for (final Entry<XlsCell, String> entry : columnValuesMap.entrySet()) {
            final Cell cell = sheet.getRow(entry.getKey().getRowIndex()).getCell(entry.getKey().getColumnIndex());
            cell.setCellValue(entry.getValue());
        }
        fileInputStream.close();

        // Write the changes to the xlsx file
        final FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName));
        workBook.write(fileOutputStream);
        workBook.close();
        fileOutputStream.close();
    }

    /**
     * Delete all the download features excel file from the download path
     */
    public static void cleanDownloadedFile(final EnvironmentVariables environmentVariables, final String fileName) {
        // Delete all existing files with name "ExportControlStatistics" in the
        // download path
        final Util util = new Util();
        if (System.getProperty("os.name").startsWith("Mac")) {
            final String home = System.getProperty("user.home");
            downloadPath = home + environmentVariables.getDownloadPathForMac();
            LOGGER.info("Deleting " + fileName + " from Download path: " + downloadPath);
            util.deleteAllFilesWithSpecificFileName(downloadPath, fileName);
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            downloadPath = environmentVariables.getDownloadPathForWindows();
            LOGGER.info("Deleting " + fileName + " from Download path: " + downloadPath);
            util.deleteAllFilesWithSpecificFileName(downloadPath, fileName);
        }
    }

    /*
     * Get the directory path
     */
    public static String getDirPath(final EnvironmentVariables environmentVariables) {
        if (System.getProperty("os.name").startsWith("Mac")) {
            final String home = System.getProperty("user.home");
            downloadPath = home + environmentVariables.getDownloadPathForMac();
            LOGGER.info("Download Directory path for MAC: " + downloadPath);
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            downloadPath = environmentVariables.getDownloadPathForWindows();
            LOGGER.info("Download Directory path for Windows: " + downloadPath);
        }

        return downloadPath;
    }
}
