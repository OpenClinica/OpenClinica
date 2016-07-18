package org.akaza.openclinica.control.admin;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public final class SpreadsheetPreview implements Preview {

    public static final String ITEMS = "Items";
    public static final String SECTIONS = "Sections";
    public static final String GROUPS = "Groups";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public Map<String, Map> createCrfMetaObject(HSSFWorkbook workbook) {
        if (workbook == null)
            return new HashMap<String, Map>();
        Map<String, Map> spreadSheetMap = new HashMap<String, Map>();
        Map<Integer, Map<String, String>> sections = createItemsOrSectionMap(workbook, SECTIONS);
        Map<Integer, Map<String, String>> items = createItemsOrSectionMap(workbook, ITEMS);
        Map<String, String> crfInfo = createCrfMap(workbook);
        if (sections.isEmpty() && items.isEmpty() && crfInfo.isEmpty()) {
            return spreadSheetMap;
        }
        spreadSheetMap.put("sections", sections);
        spreadSheetMap.put("items", items);
        spreadSheetMap.put("crf_info", crfInfo);
        return spreadSheetMap;
    }

    /**
     * This method searches for a sheet named "Items" or "Sections" in an Excel
     * Spreadsheet object, then creates a sorted Map whose members represent a
     * row of data for each "Item" or "Section" on the sheet. This method was
     * created primarily to get Items and section data for previewing a CRF.
     * 
     * @return A SortedMap implementation (TreeMap) containing row numbers, each
     *         pointing to a Map. The Maps represent each Item or section row in
     *         a spreadsheet. The items or sections themselves are in rows 1..N.
     *         An example data value from a Section row is: 1: {page_number=1.0,
     *         section_label=Subject Information, section_title=SimpleSection1}
     *         Returns an empty Map if the spreadsheet does not contain any
     *         sheets named "sections" or "items" (case insensitive).
     * @param workbook
     *            is an object representing a spreadsheet.
     * @param itemsOrSection
     *            should specify "items" or "sections" or the associated static
     *            variable, i.e. SpreadsheetPreview.ITEMS
     */
    public Map<Integer, Map<String, String>> createItemsOrSectionMap(HSSFWorkbook workbook, String itemsOrSection) {
        if (workbook == null || workbook.getNumberOfSheets() == 0) {
            return new HashMap<Integer, Map<String, String>>();
        }
        if (itemsOrSection == null || !itemsOrSection.equalsIgnoreCase(ITEMS) && !itemsOrSection.equalsIgnoreCase(SECTIONS)) {
            return new HashMap<Integer, Map<String, String>>();
        }
        HSSFSheet sheet;
        HSSFRow row;
        HSSFCell cell;
        // static item headers for a CRF; TODO: change these so they are not
        // static and hard-coded
        /*
         * New itemHeaders String[] itemHeaders =
         * {"item_name","description_label","left_item_text",
         * "units","right_item_text","section_label","group_label","header",
         * "subheader","parent_item","column_number","page_number",
         * "question_number","response_type","response_label",
         * "response_options_text","response_values","response_layout","default_value",
         * "data_type",
         * "validation","validation_error_message","phi","required"};
         */
        String[] itemHeaders =
            { "item_name", "description_label", "left_item_text", "units", "right_item_text", "section_label", "header", "subheader", "parent_item",
                "column_number", "page_number", "question_number", "response_type", "response_label", "response_options_text", "response_values", "data_type",
                "validation", "validation_error_message", "phi", "required" };
        String[] sectionHeaders = { "section_label", "section_title", "subtitle", "instructions", "page_number", "parent_section" };
        Map<String, String> rowCells = new HashMap<String, String>();
        SortedMap<Integer, Map<String, String>> allRows = new TreeMap<Integer, Map<String, String>>();
        String str = "";
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);
            str = workbook.getSheetName(i);
            if (str.equalsIgnoreCase(itemsOrSection)) {
                for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {
                    String[] headers = itemsOrSection.equalsIgnoreCase(ITEMS) ? itemHeaders : sectionHeaders;
                    // create a new Map to add to the allRows Map
                    // rowCells has already been initialized in a higher code
                    // block
                    // so if j == 1 we don't have to init the new Map the first
                    // time again.
                    if (j > 1)
                        rowCells = new HashMap<String, String>();
                    row = sheet.getRow(j);
                    for (int k = 0; k < headers.length; k++) {
                        cell = row.getCell((short) k);
                        if (headers[k].equalsIgnoreCase("left_item_text") || headers[k].equalsIgnoreCase("right_item_text")
                                || headers[k].equalsIgnoreCase("header") || headers[k].equalsIgnoreCase("subheader")
                                || headers[k].equalsIgnoreCase("question_number")|| headers[k].equalsIgnoreCase("section_title")
                                || headers[k].equalsIgnoreCase("subtitle")|| headers[k].equalsIgnoreCase("instructions")) {
                            rowCells.put(headers[k], getCellValue(cell));
                        } else {
                            rowCells.put(headers[k], getCellValue(cell).replaceAll("<[^>]*>", ""));
                        }
                    }
                    // item_name

                    allRows.put(new Integer(j), rowCells);
                }// end inner for loop
            }// end if
        }// end outer for
        return allRows;
    }

    public Map<Integer, Map<String, String>> createGroupsMap(HSSFWorkbook workbook) {
        if (workbook == null || workbook.getNumberOfSheets() == 0) {
            return new HashMap<Integer, Map<String, String>>();
        }
        HSSFSheet sheet;
        HSSFRow row;
        HSSFCell cell;
        // static group headers for a CRF; TODO: change these so they are not
        // static and hard-coded
        String[] groupHeaders =
            { "group_label", "group_layout", "group_header", "group_sub_header", "group_repeat_number", "group_repeat_max", "group_repeat_array",
                "group_row_start_number" };
        Map<String, String> rowCells = new HashMap<String, String>();
        SortedMap<Integer, Map<String, String>> allRows = new TreeMap<Integer, Map<String, String>>();
        String str = "";
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);
            str = workbook.getSheetName(i);
            if (str.equalsIgnoreCase("Groups")) {
                for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {
                    // create a new Map to add to the allRows Map
                    // rowCells has already been initialized in a higher code
                    // block
                    // so if j == 1 we don't have to init the new Map the first
                    // time again.
                    if (j > 1)
                        rowCells = new HashMap<String, String>();
                    row = sheet.getRow(j);
                    for (int k = 0; k < groupHeaders.length; k++) {
                        cell = row.getCell((short) k);
                        if (groupHeaders[k].equalsIgnoreCase("group_header")) {
                            rowCells.put(groupHeaders[k], getCellValue(cell).replaceAll("<[^>]*>", ""));
                        } else {

                        }
                    }

                    allRows.put(j, rowCells);
                }// end inner for loop
            }// end if
        }// end outer for
        return allRows;
    }

    private String getCellValue(HSSFCell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
        case HSSFCell.CELL_TYPE_NUMERIC:
            return Double.toString(cell.getNumericCellValue());
        case HSSFCell.CELL_TYPE_BOOLEAN:
            return new Boolean(cell.getBooleanCellValue()).toString();
        case HSSFCell.CELL_TYPE_FORMULA:
            return cell.getCellFormula().toString();
        }
        return "";
    }

    public static void main(String[] args) throws IOException {

        // Simple3.xls , Cancer_History5.xls , Can3.xls
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File("/Users/bruceperry/work/OpenClinica-Cancer-Demo-Study/Cancer_History5.xls")));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        SpreadsheetPreview prev = new SpreadsheetPreview();
        // createSectionsMap createItemsMap
        Map map = prev.createItemsOrSectionMap(wb, "sections");
        Map.Entry me;
        Map.Entry me2;
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            me = (Map.Entry) iter.next();
            Map mp = (Map) me.getValue();
            // logger.info(me.getKey() + ": " + me.getValue());
        }
    }

    /*
     * This method searches for a sheet named "Sections" in an Excel Spreadsheet
     * object, then creates a HashMap containing that sheet's data. The HashMap
     * contains the sheet name as the key, and a List of cells (only the ones
     * that contain data, not blank ones). This method was created primarly to
     * get the section names for a CRF preview page. The Map does not contain
     * data for any sections that have duplicate names; just one section per
     * section name. This method does not yet validate the spreadsheet as a CRF.
     * @author Bruce Perry @returns A HashMap containing CRF section names as
     * keys. Returns an empty HashMap if the spreadsheet does not contain any
     * sheets named "Sections."
     */
    public Map<String, String> createCrfMap(HSSFWorkbook workbook) {
        if (workbook == null || workbook.getNumberOfSheets() == 0) {
            return new HashMap<String, String>();
        }
        HSSFSheet sheet;
        HSSFRow row;
        HSSFCell cell;
        Map<String, String> crfInfo = new HashMap<String, String>();
        String mapKey = "";
        String val = "";
        String str = "";
        String[] crfHeaders = { "crf_name", "version", "version_description", "revision_notes" };
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);
            str = workbook.getSheetName(i);
            if (str.equalsIgnoreCase("CRF")) {
                row = sheet.getRow(1);
                for (int k = 0; k < crfHeaders.length; k++) {
                    // The first cell in the row contains the header CRF_NAME
                    mapKey = crfHeaders[k];
                    cell = row.getCell((short) k);
                    if (cell != null) { // the cell does not have a blank value
                        // Set the Map key to the crf header

                        switch (cell.getCellType()) {
                        case HSSFCell.CELL_TYPE_STRING:
                            val = cell.getStringCellValue();
                            break;
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            val = Double.toString(cell.getNumericCellValue());
                            break;
                        case HSSFCell.CELL_TYPE_BOOLEAN:
                            val = new Boolean(cell.getBooleanCellValue()).toString();
                            break;
                        case HSSFCell.CELL_TYPE_FORMULA:
                            cell.getCellFormula().toString();
                            break;
                        }
                    }
                    crfInfo.put(mapKey, val);
                }
            }// end if
        }// end outer for
        return crfInfo;
    }
}
