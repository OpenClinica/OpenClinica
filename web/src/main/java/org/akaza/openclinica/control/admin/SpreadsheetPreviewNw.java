package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.ApplicationConstants;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class SpreadsheetPreviewNw implements Preview {
    public static final String ITEMS = "Items";
    public static final String SECTIONS = "Sections";
    public static final String GROUPS = "Groups";
    private static final Logger logger = LoggerFactory.getLogger("org.akaza.openclinica.control.admin.SpreadsheetPreviewNw");
    private SimpleDateFormat simpleDateFormat;

    public SpreadsheetPreviewNw() {
        super();
        String pattern = "";
        pattern = ResourceBundleProvider.getFormatBundle().getString("date_format_string");
        if (pattern != null && pattern.length() > 0) {
            this.simpleDateFormat = new SimpleDateFormat(pattern);
        } else {
            this.simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        }
    }

    public SpreadsheetPreviewNw(SimpleDateFormat simpleDateFormat) {
        super();
        this.simpleDateFormat = simpleDateFormat;
    }

    /*
     * Ths method returns a HashMap object that contains all of the information that you are likely to need from a CRF template or spreadsheet. The map key is a
     * String describing the sheet name as in "groups" or "items." The values are Maps representing the sheet's column names and values. The four returned keys
     * are items, sections, groups, and crf_info. Here is an example return value: groups: {1={group_repeat_number=1.0, group_header=, group_layout=Horizontal,
     * group_repeat_array=, group_row_start_number=2.0, group_sub_header=, group_label=MyGroupLabel, group_repeat_max=3.0}}
     */
    public Map<String, Map> createCrfMetaObject(HSSFWorkbook workbook) {
        if (workbook == null)
            return new HashMap<String, Map>();
        Map<String, Map> spreadSheetMap = new HashMap<String, Map>();
        Map<Integer, Map<String, String>> sections = createItemsOrSectionMap(workbook, SECTIONS);
        Map<Integer, Map<String, String>> items = createItemsOrSectionMap(workbook, ITEMS);
        Map<String, String> crfInfo = createCrfMap(workbook);
        Map<Integer, Map<String, String>> groups = this.createGroupsMap(workbook);

        if (sections.isEmpty() && items.isEmpty() && crfInfo.isEmpty()) {
            return spreadSheetMap;
        }
        spreadSheetMap.put("sections", sections);
        spreadSheetMap.put("items", items);
        spreadSheetMap.put("crf_info", crfInfo);
        spreadSheetMap.put("groups", groups);
        return spreadSheetMap;
    }

    /**
     * This method searches for a sheet named "Items" or "Sections" in an Excel Spreadsheet object, then creates a sorted Map whose members represent a row of
     * data for each "Item" or "Section" on the sheet. This method was created primarily to get Items and section data for previewing a CRF.
     *
     * @return A SortedMap implementation (TreeMap) containing row numbers, each pointing to a Map. The Maps represent each Item or section row in a
     *         spreadsheet. The items or sections themselves are in rows 1..N. An example data value from a Section row is: 1: {page_number=1.0,
     *         section_label=Subject Information, section_title=SimpleSection1} Returns an empty Map if the spreadsheet does not contain any sheets named
     *         "sections" or "items" (case insensitive).
     * @param workbook
     *            is an object representing a spreadsheet.
     * @param itemsOrSection
     *            should specify "items" or "sections" or the associated static variable, i.e. SpreadsheetPreview.ITEMS
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
         * New itemHeaders String[] itemHeaders = {"item_name","description_label","left_item_text",
         * "units","right_item_text","section_label","group_label","header", "subheader","parent_item","column_number","page_number",
         * "question_number","response_type","response_label", "response_options_text","response_values","response_layout","default_value", "data_type",
         * "validation","validation_error_message","phi","required"};
         */
        String[] itemHeaders =
            { "item_name", "description_label", "left_item_text", "units", "right_item_text", "section_label", "group_label", "header", "subheader",
                "parent_item", "column_number", "page_number", "question_number", "response_type", "response_label", "response_options_text",
                "response_values", "response_layout", "default_value", "data_type", "width_decimal", "validation", "validation_error_message", "phi", "required" };
        String[] sectionHeaders = { "section_label", "section_title", "subtitle", "instructions", "page_number", "parent_section", "borders" };
        Map<String, String> rowCells = new HashMap<String, String>();
        SortedMap<Integer, Map<String, String>> allRows = new TreeMap<Integer, Map<String, String>>();
        String str;
        String dateFormat = "";
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
                    if (row == null) {
                        continue;
                    }
                    for (int k = 0; k < headers.length; k++) {
                        cell = row.getCell((short) k);
                        if ("default_value".equalsIgnoreCase(headers[k]) && isDateDatatype(headers, row) && !"".equalsIgnoreCase(getCellValue(cell))) {
                            try {
                                // BWP>> getDateCellValue() wll throw an
                                // exception if
                                // the value is an invalid date. Keep the date
                                // format the same as
                                // it is in the database; MM/dd/yyyy
                                // String pttrn = ResourceBundleProvider.getFormatBundle().getString("oc_date_format_string");
                                String pttrn = ApplicationConstants.getDateFormatInItemData();
                                dateFormat = new SimpleDateFormat(pttrn).format(cell.getDateCellValue());
                                rowCells.put(headers[k], dateFormat);
                                continue;
                            } catch (Exception e) {
                                String cellVal = getCellValue(cell);
                                logger.info("An invalid date format was encountered when reading a default value in the spreadsheet.");
                                rowCells.put(headers[k], cellVal);
                                continue;
                            }

                        }

                        if (headers[k].equalsIgnoreCase("left_item_text") || headers[k].equalsIgnoreCase("right_item_text")
                                || headers[k].equalsIgnoreCase("header") || headers[k].equalsIgnoreCase("subheader")
                                || headers[k].equalsIgnoreCase("question_number")|| headers[k].equalsIgnoreCase("section_title")
                                || headers[k].equalsIgnoreCase("subtitle")|| headers[k].equalsIgnoreCase("instructions")|| headers[k].equalsIgnoreCase("response_options_text")) {
                            rowCells.put(headers[k], getCellValue(cell).replaceAll("\\\\,", "\\,"));
                        } else {
                            rowCells.put(headers[k], getCellValue(cell).replaceAll("\\\\,", "\\,").replaceAll("<[^>]*>", ""));
                        }
                        //logger.warn("BADS: "+headers[k]+": "+getCellValue(cell));
                    }
                    // item_name

                    allRows.put(j, rowCells);
                }// end inner for loop
            }// end if
        }// end outer for
        return allRows;
    }

    private boolean isDateDatatype(String[] headers, HSSFRow row) {
        HSSFCell cell;
        String currentDataType = "";
        if (headers == null || headers.length == 0 || row == null) {
            return false;
        }
        for (int k = 0; k < headers.length; k++) {
            cell = row.getCell((short) k);
            if ("data_type".equalsIgnoreCase(headers[k])) {
                currentDataType = getCellValue(cell);
                if ("date".equalsIgnoreCase(currentDataType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Integer, Map<String, String>> createGroupsMap(HSSFWorkbook workbook) {
        if (workbook == null || workbook.getNumberOfSheets() == 0) {
            return new HashMap<Integer, Map<String, String>>();
        }
        HSSFSheet sheet;
        HSSFRow row;
        HSSFCell cell;
        sheet = workbook.getSheetAt(4);
        cell = sheet.getRow(1).getCell((short) 0);
        String version = cell.getStringCellValue();
        // static group headers for a CRF; TODO: change these so they are not
        // static and hard-coded
        // BWP>>remove "group_borders" column
        String[] groupHeaders = { "group_label", "repeating_group", "group_header", "group_repeat_number", "group_repeat_max" };
        if(version.equalsIgnoreCase("Version: 2.2")
                || version.equalsIgnoreCase("Version: 2.5")
                || version.equalsIgnoreCase("Version: 3.0")){
            groupHeaders = new String[]{ "group_label", "group_header", "group_repeat_number", "group_repeat_max" };
        }

        Map<String, String> rowCells = new HashMap<String, String>();
        SortedMap<Integer, Map<String, String>> allRows = new TreeMap<Integer, Map<String, String>>();
        String str;
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
                            rowCells.put(groupHeaders[k], getCellValue(cell));
                        } else {
                            rowCells.put(groupHeaders[k], getCellValue(cell).replaceAll("<[^>]*>", ""));
                        }
                    }

                    allRows.put(j, rowCells);
                }// end inner for loop
            }// end if
        }// end outer for
        return allRows;
    }

    private String getCellValue(HSSFCell cell) {
        String val = "";
        if (cell == null)
            return "";
        // new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
        case HSSFCell.CELL_TYPE_NUMERIC:
            val = Double.toString(cell.getNumericCellValue());
            // code derived from SpreadsheetTableRepeating.java
            double dphi = cell.getNumericCellValue();
            if ((dphi - (int) dphi) * 1000 == 0) {
                val = (int) dphi + "";
            }
            return val;
        case HSSFCell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(cell.getBooleanCellValue());
        case HSSFCell.CELL_TYPE_FORMULA:
            return cell.getCellFormula();

        }
        return "";
    }

    public static void main(String[] args) throws IOException {

        // Simple3.xls , Cancer_History5.xls , Can3.xls
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File("d:/23TestComma2.xls")));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        SpreadsheetPreviewNw spnw = new SpreadsheetPreviewNw();
        // createSectionsMap createItemsMap
        Map map = spnw.createCrfMetaObject(wb);
        // Map map2 = spnw.createItemsOrSectionMap(wb,"items");
        Map.Entry me;
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            me = (Map.Entry) iter.next();
            Map mp = (Map) me.getValue();
            logger.debug(me.getKey() + ": " + me.getValue());
        }

    }

    /*
     * This method searches for a sheet named "Sections" in an Excel Spreadsheet object, then creates a HashMap containing that sheet's data. The HashMap
     * contains the sheet name as the key, and a List of cells (only the ones that contain data, not blank ones). This method was created primarly to get the
     * section names for a CRF preview page. The Map does not contain data for any sections that have duplicate names; just one section per section name. This
     * method does not yet validate the spreadsheet as a CRF.
     * @author Bruce Perry @returns A HashMap containing CRF section names as keys. Returns an empty HashMap if the spreadsheet does not contain any sheets
     * named "Sections."
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
                            val = Boolean.toString(cell.getBooleanCellValue());
                            break;
                        case HSSFCell.CELL_TYPE_FORMULA:
                            cell.getCellFormula();
                            break;
                        }
                    }
                    crfInfo.put(mapKey, val);
                }
            }// end if
        }// end outer for
        return crfInfo;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }
}
