/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.JDBCType;
import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.bean.admin.QueryObject;
import org.akaza.openclinica.bean.admin.SqlParameter;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.oid.MeasurementUnitOidGenerator;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.form.spreadsheet.OnChangeSheetValidationCell;
import org.akaza.openclinica.control.form.spreadsheet.OnChangeSheetValidationType;
import org.akaza.openclinica.control.form.spreadsheet.OnChangeSheetValidator;
import org.akaza.openclinica.control.form.spreadsheet.SheetCell;
import org.akaza.openclinica.control.form.spreadsheet.SheetValidationContainer;
import org.akaza.openclinica.control.form.spreadsheet.SheetValidationType;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.core.util.CrfTemplateColumnNameEnum;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.exception.CRFReadingException;
import org.akaza.openclinica.logic.score.ScoreValidator;
import org.akaza.openclinica.web.SQLInitServlet;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <P>
 * Returns multiple types of things based on the parsing; returns html table
 * returns data objects as SQL strings.
 * <P>
 * The most important method here is the toNIB() method, which returns the
 * NewInstrumentBean which in turn creates a new instrument version in the
 * database.
 *
 *
 * @author thickerson with help from Brian Gilman @ the Whitehead Institute
 *         modified by jxu
 * @version CVS: $Id: SpreadSheetTableRepeating.java 13005 2009-06-23 13:45:33Z
 *          kkrumlian $
 */

public class SpreadSheetTableRepeating implements SpreadSheetTable {

    private POIFSFileSystem fs = null;

    private UserAccountBean ub = null;

    private String versionName = null;

    private int crfId = 0;

    private String crfName = "";

    private String versionIdString = "";
    private String versionIdStringWithParameter ="";

    private boolean isRepeating = false;

    private final HashMap itemGroups = new HashMap();

    private final HashMap itemsToGrouplabels = new HashMap();

    private Locale locale;

    private final int studyId;

    private Set<String> existingUnits = new TreeSet<String>();

    private Set<String> existingOIDs = new TreeSet<String>();

    private MeasurementUnitDao measurementUnitDao = new MeasurementUnitDao();

    // the default; all crf ids should be > 0, tbh 8-29 :-)
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public SpreadSheetTableRepeating(FileInputStream parseStream, UserAccountBean ub, String versionName, Locale locale, int studyId) throws IOException {
        // super();

        this.fs = new POIFSFileSystem(parseStream);
        this.ub = ub;
        this.versionName = versionName;
        this.locale = locale;
        this.studyId = studyId;
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        int numSheets = wb.getNumberOfSheets();
        for (int j = 0; j < numSheets; j++) {
            HSSFSheet sheet = wb.getSheetAt(j);// sheetIndex);
            String sheetName = wb.getSheetName(j);
            if (sheetName.equalsIgnoreCase("groups")) {
                isRepeating = true;
            }
            // *** now we've set it up so that we can switch back to classic,
            // tbh, 06/07
        }
        // should be set in the super(), tbh 05/2007
    }

    public void setCrfId(int id) {
        this.crfId = id;
    }

    public int getCrfId() {
        return this.crfId;
    }

    public NewCRFBean toNewCRF(javax.sql.DataSource ds, ResourceBundle resPageMsg) throws IOException, CRFReadingException {

        String dbName = SQLInitServlet.getDBName();

        NewCRFBean ncrf = new NewCRFBean(ds, crfId);

        ncrf.setCrfId(crfId);// set crf id

        StringBuffer buf = new StringBuffer();
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        int numSheets = wb.getNumberOfSheets();
        ArrayList queries = new ArrayList();
        // ArrayList groupItemMapQueries = new ArrayList();
        ArrayList errors = new ArrayList();
       // ArrayList repeats = new ArrayList();
        HashMap tableNames = new HashMap();
        HashMap items = new HashMap();
        SpreadSheetItemUtil item_from_row = null;
        String pVersion = "";
        String pVerDesc = "";
        int parentId = 0;
        int dataTypeId = 5;// default is ST(String) type
        HashMap itemCheck = ncrf.getItemNames();
        HashMap GroupCheck = ncrf.getItemGroupNames();
        HashMap openQueries = new LinkedHashMap();
        HashMap backupItemQueries = new LinkedHashMap();// save all the item
        // queries if
        // deleting item happens
        ArrayList secNames = new ArrayList(); // check for dupes, also

        ArrayList<String> itemGroupOids = new ArrayList<String>();
        ArrayList<String> itemOids = new ArrayList<String>();

        CRFDAO cdao = new CRFDAO(ds);
        CRFBean crf = (CRFBean) cdao.findByPK(crfId);
        ItemDAO idao = new ItemDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        ItemGroupDAO itemGroupDao = new ItemGroupDAO(ds);
        SheetValidationContainer sheetContainer = new SheetValidationContainer();
        HashMap<String, String> allItems = (HashMap<String, String>)sheetContainer.getAllItems();
        //HashMap<String, String> allItems = new HashMap<String, String>();
        Map<String, String[]> controlValues = new HashMap<String, String[]>();
        int maxItemFormMetadataId = new ItemFormMetadataDAO(ds).findMaxId();
        OnChangeSheetValidator instantValidator = new OnChangeSheetValidator(sheetContainer, resPageMsg);


        int validSheetNum = 0;
        for (int j = 0; j < numSheets; j++) {
            HSSFSheet sheet = wb.getSheetAt(j);// sheetIndex);
            String sheetName = wb.getSheetName(j);
            if (sheetName.equalsIgnoreCase("CRF") || sheetName.equalsIgnoreCase("Sections") || sheetName.equalsIgnoreCase("Items")) {
                validSheetNum++;
            }
        }
        if (validSheetNum != 3) {
            errors
                    .add("The excel spreadsheet doesn't have required valid worksheets. Please check whether it contains"
                        + " sheets of CRF, Sections and Items.");
        }
        HSSFSheet sheet = wb.getSheetAt(4);
        HSSFCell insCell = sheet.getRow(1).getCell((short) 0);
        String versionNo = insCell.toString();
        // check to see if questions are referencing a valid section name, tbh
        // 7/30
        for (int j = 0; j < numSheets; j++) {
            sheet = wb.getSheetAt(j);// sheetIndex);
            String sheetName = wb.getSheetName(j);
            if (sheetName.equalsIgnoreCase("Instructions")) {
                // totally ignore instructions
            } else {
                /*
                 * current strategem: build out the queries by hand and revisit
                 * this as part of the data loading module. We begin to check
                 * for errors here and look for blank cells where there should
                 * be data, tbh, 7/28
                 */
                int numRows = sheet.getPhysicalNumberOfRows();
                int lastNumRow = sheet.getLastRowNum();
                // logger.debug("PhysicalNumberOfRows" +
                // sheet.getPhysicalNumberOfRows());
                // great minds apparently think alike...tbh, commented out
                // 06/19/2007
                // logger.debug("LastRowNum()" + sheet.getLastRowNum());
                String secName = "";
                String page = "";
                // YW << for holding "responseLabel_responseType"
                ArrayList resPairs = new ArrayList();
                // YW >>
                ArrayList resNames = new ArrayList();// records all the
                // response_labels
                HashMap htmlErrors = new HashMap();

                // the above two need to persist across mult. queries,
                // and they should be created FIRST anyway, since instrument is
                // first
                // also need to add to VERSIONING_MAP, tbh, 6-6-3

                // try to count how many blank rows, if 5 concective blank rows
                // found, stop reading
                int blankRowCount = 0;
                String itemName=null;String default_value=null;
                if (sheetName.equalsIgnoreCase("Items")) {
                    logger.debug("read an item in sheet" + sheetName);
                    Map labelWithOptions = new HashMap();
                    Map labelWithValues = new HashMap();
                    Map labelWithType = new HashMap<String, String>();
                    logger.debug("row20 is: " + getValue(sheet.getRow(0).getCell((short) 20)));
                    boolean hasWDColumn = "width_decimal".equalsIgnoreCase(getValue(sheet.getRow(0).getCell((short) 20))) ? true : false;
                    //Adding itemnames for further use
                   // HashMap itemNames = new HashMap();
                    //htaycher : code should be competly refactored to use stucture to hold all data per row
                    
                    ArrayList< SpreadSheetItemUtil> row_items = new ArrayList< SpreadSheetItemUtil>();
                     
                   
                    for (int k = 1; k < numRows; k++) {
                        
                        if (sheet.getRow(k) == null) {
                            blankRowCount++;
                            if (blankRowCount == 5) {  break; }
                            continue;
                        }
                        int cellIndex = 0;
                       
                        HSSFCell cell = sheet.getRow(k).getCell((short) 0);
                        item_from_row =  new SpreadSheetItemUtil();
                        row_items.add( item_from_row);
                  	    item_from_row.setItemName(getValue(cell));
                  	    item_from_row.verifyItemName(row_items, errors, htmlErrors,j,  resPageMsg);
                   	 	itemName = item_from_row.getItemName();
                        //}
                   
                        
                        cell = sheet.getRow(k).getCell((short) 1);
                        String descLabel = getValue(cell);
                        descLabel = descLabel.replaceAll("<[^>]*>", "");
                        item_from_row.setDescriptionLabel(descLabel);

                        if (StringUtil.isBlank(descLabel)) {
                            // errors.add("The DESCRIPTION_LABEL column was
                            // blank at row " + k + ", Items worksheet.");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DESCRIPTION_LABEL_column") + " "
                                + resPageMsg.getString("was_blank_at_row") +" " + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",1", resPageMsg.getString("required_field"));
                        }
                        if (descLabel != null && descLabel.length() > 4000) {
                            errors.add(resPageMsg.getString("item_desc_length_error"));
                        }


                        cell = sheet.getRow(k).getCell((short) 2);
                        String leftItemText = getValue(cell);
                        if (leftItemText != null && leftItemText.length() > 4000) {
                            errors.add(resPageMsg.getString("left_item_length_error"));
                        }
                        item_from_row.setLeftItemText(leftItemText);
                        // Commented out to resolve issue-2413
                        // if (StringUtil.isBlank(leftItemText)) {
                        // errors.add(resPageMsg.getString("the") + " " +
                        // resPageMsg.getString("LEFT_ITEM_TEXT_column") + " "
                        // + resPageMsg.getString("was_blank_at_row") + k + ","
                        // + resPageMsg.getString("items_worksheet") + ".");
                        // htmlErrors.put(j + "," + k + ",2",
                        // resPageMsg.getString("required_field"));
                        // }

                        cell = sheet.getRow(k).getCell((short) 3);
                        String unit = getValue(cell).trim();
                        if (unit != null && unit.length() > 0) {
                            String muSql = "";
                            //htaycher max length=64
                            if (unit.length() > 64) {
	                            errors.add(resPageMsg.getString("units_length_error"));
	                            htmlErrors.put(j + "," + k + ","+CrfTemplateColumnNameEnum.UNITS.getCellNumber(), resPageMsg.getString("INVALID_FIELD"));
                            }
                            if (this.existingUnits.size() > 0) {
                            } else {
                                this.existingUnits = this.measurementUnitDao.findAllNames();
                                if (this.existingUnits == null) {
                                    this.existingUnits = new TreeSet<String>();
                                }
                            }
                            if (this.existingOIDs.size() > 0) {
                            } else {
                                this.existingOIDs = this.measurementUnitDao.findAllOIDs();
                                if (this.existingOIDs == null) {
                                    this.existingOIDs = new TreeSet<String>();
                                }
                            }
                            if (this.existingUnits.contains(unit)) {
                                this.logger.debug("unit=" + unit + " existed.");
                            } else {
                                String oid = "";
                                try {
                                    oid = new MeasurementUnitOidGenerator().generateOid(unit);
                                } catch (Exception e) {
                                    throw new RuntimeException("CANNOT GENERATE OID");
                                }
                                if(this.existingOIDs.contains(oid)) {
                                    if(oid.length()>40) {
                                        oid = oid.substring(0, 35);
                                    }
                                    oid = new MeasurementUnitOidGenerator().randomizeOid(oid);
                                }
                                this.existingOIDs.add(oid);
                                this.existingUnits.add(unit);
/*                                muSql = this.getMUInsertSql(oid, unit, ub.getId(), dbName);
                                queries.add(muSql);*/
                                muSql = this.getMUInsertSqlParameters();
                                
                                ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                                sqlParameters.add(new SqlParameter(oid));
                                sqlParameters.add(new SqlParameter(unit));
                                                             
                                QueryObject qo = new QueryObject();
                                qo.setSql(muSql);
                                qo.setSqlParameters(sqlParameters);
                                
                                queries.add(qo);
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 4);
                        String rightItemText = getValue(cell);
                        if (rightItemText != null && rightItemText.length() > 2000) {
                            errors.add(resPageMsg.getString("right_item_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 5);//section label
                        item_from_row.setSectionLabel(getValue(cell));
                        item_from_row.verifySectionLabel(row_items, errors, secNames, htmlErrors, j, resPageMsg);
                        secName=item_from_row.getSectionLabel();
                        // *******************************************
                        // group_label will go here, tbh in place 6
                        // have to advance all the rest by one at least (if
                        // there are
                        // no other columns) tbh, 5-14-2007

                        cell = sheet.getRow(k).getCell((short) 6);//group label
                        item_from_row.setGroupLabel(getValue(cell));
                     //htaycher: how 'NON-GROUPED' group is processed for 3.1 template?
                        //is it a reason for 13816
                        if (item_from_row.getItemName().length() > 0) {
                            if (!StringUtil.isBlank(item_from_row.getGroupLabel())) {
                                allItems.put(item_from_row.getItemName(), item_from_row.getGroupLabel());
                            } else {
                                allItems.put(item_from_row.getItemName(), "Ungrouped");
                            }
                        }
                        String groupLabel=item_from_row.getGroupLabel();

                        sheetContainer.getItemSectionNameMap().put(itemName, secName);
                        sheetContainer.collectRepGrpItemNameMap(itemName, item_from_row.getGroupLabel());

                        cell = sheet.getRow(k).getCell((short) 7);//header
                        String header = getValue(cell);
                        if (header != null && header.length() > 2000) {
                            errors.add(resPageMsg.getString("item_header_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 8);//subheader
                        String subHeader = getValue(cell);
                        if (subHeader != null && subHeader.length() > 240) {
                            errors.add(resPageMsg.getString("item_subheader_length_error"));
                            htmlErrors.put(j + "," + k + ","+CrfTemplateColumnNameEnum.SUBHEADER.getCellNumber(), resPageMsg.getString("INVALID_FIELD"));
                        }

                        cell = sheet.getRow(k).getCell((short) 9);//parentid
                        String parentItem = getValue(cell);
                        item_from_row.setParentItem(parentItem);
                        item_from_row.verifyParentID( row_items, errors,htmlErrors,j,resPageMsg, itemGroups);
                        //for now , when(if ) code refactoring will be done, item will be written by SpreadSheetItemUtil
                        parentItem=item_from_row.getParentItem();
                        
                        cell = sheet.getRow(k).getCell((short) 10);//column id
                        int columnNum = 0;
                        String column = getValue(cell);
                        if (!StringUtil.isBlank(column)) {
                            try {
                                columnNum = Integer.parseInt(column);
                            } catch (NumberFormatException ne) {
                                columnNum = 0;
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 11);//page number
                        if (cell != null) {
                            page = getValue(cell);
                        }

                        cell = sheet.getRow(k).getCell((short) 12);//question number
                        String questionNum = getValue(cell);

                        cell = sheet.getRow(k).getCell((short) 13);//response type
                         String responseType = getValue(cell);
                        int responseTypeId = 1;
                        if (StringUtil.isBlank(responseType)) {
                            // errors.add("The RESPONSE_TYPE column was blank at
                            // row " + k + ", items worksheet.");
                            // htmlErrors.put(j + "," + k + ",13", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_TYPE_column") + " "
                                + resPageMsg.getString("was_blank_at_row")+" "  + k + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                            htmlErrors.put(j + "," + k + ",13", resPageMsg.getString("required_field"));

                        } else {
                            if (!ResponseType.findByName(responseType.toLowerCase())) {
                                // errors.add("The RESPONSE_TYPE column was
                                // invalid at row " + k
                                // + ", items worksheet.");
                                // htmlErrors.put(j + "," + k + ",13", "INVALID
                                // FIELD");
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_TYPE_column") + " "
                                    + resPageMsg.getString("was_invalid_at_row") + " "+k + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                                htmlErrors.put(j + "," + k + ",13", resPageMsg.getString("INVALID_FIELD"));
                            } else {
                                responseTypeId = ResponseType.getByName(responseType.toLowerCase()).getId();
                                item_from_row.setResponseTypeId(responseTypeId);
                                
                            }
                            if(responseTypeId == 5){
                                cell = sheet.getRow(k).getCell((short) 18);
                                String def = getValue(cell);
                                if(!StringUtil.isBlank(def)){
                                    errors.add(resPageMsg.getString("radio_with_default")+ item_from_row.getItemName() +resPageMsg.getString("change_radio"));
                                    htmlErrors.put(j + "," + k + ","+CrfTemplateColumnNameEnum.DEFAULT_VALUE.getCellNumber()
                                       	 , resPageMsg.getString("INVALID_FIELD"));
                                
                                }
                            }else if(responseTypeId == ResponseType.INSTANT_CALCULATION.getId()) {
                                unit = "";
                             }
                        }

                        cell = sheet.getRow(k).getCell((short) 14);
                        String responseLabel = getValue(cell);
                        // responseLabel = responseLabel.replaceAll("<[^>]*>",
                        // "");

                        if (StringUtil.isBlank(responseLabel) && responseTypeId != ResponseType.TEXT.getId()
                            && responseTypeId != ResponseType.TEXTAREA.getId()) {
                            // << tbh #4180
                            // errors.add("The RESPONSE_LABEL column was blank
                            // at row " + k + ", items worksheet.");
                            // htmlErrors.put(j + "," + k + ",14", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_LABEL_column") + " "
                                + resPageMsg.getString("was_blank_at_row") +" "+ k + ", " + resPageMsg.getString("items_worksheet_with_dot") );
                            htmlErrors.put(j + "," + k + ",14", resPageMsg.getString("required_field"));
                        } else if ("file".equalsIgnoreCase(responseType) && !"file".equalsIgnoreCase(responseLabel)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_LABEL_column") + " "
                                + resPageMsg.getString("should_be_file") + resPageMsg.getString("at_row") + " " + k + ", "
                                + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",14", resPageMsg.getString("should_be_file"));
                        }
                        cell = sheet.getRow(k).getCell((short) 15);
                        String resOptions = getValue(cell);
                        // resOptions = resOptions.replaceAll("<[^>]*>", "");

                        // >> tbh #4180, we cant have blanks since they will trip us up later in the process
                        if (responseTypeId == ResponseType.TEXT.getId()) {
                            responseLabel = "text";
                        } else if (responseTypeId == ResponseType.TEXTAREA.getId()) {
                            responseLabel = "textarea";
                        }
                        // << tbh
                        if (responseLabel.equalsIgnoreCase("text") || responseLabel.equalsIgnoreCase("textarea")) {
                            resOptions = "text";
                        } else if ("file".equalsIgnoreCase(responseType)) {
                            resOptions = "file";
                        }
                        int numberOfOptions = 0;
                        if (!resNames.contains(responseLabel) && StringUtil.isBlank(resOptions) && responseTypeId != ResponseType.TEXT.getId()
                            && responseTypeId != ResponseType.TEXTAREA.getId()) {
                            // << tbh #4180
                            // errors.add("The RESPONSE_OPTIONS_TEXT column was
                            // blank at row " + k
                            // + ", Items worksheet.");
                            // htmlErrors.put(j + "," + k + ",15", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_OPTIONS_TEXT_column") + " "
                                + resPageMsg.getString("was_blank_at_row") +" " + k + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                            htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("required_field"));
                        }
                        if (!resNames.contains(responseLabel) && !StringUtil.isBlank(resOptions)) {
                            if (responseTypeId == 8 || responseTypeId == 9) {
                                // YW 1-29-2008 << only one option for
                                // "calculation" type and "group-calculation"
                                // type
                                // but do we really need this variable these two
                                // types?
                                numberOfOptions = 1;
                                // YW >>
                            } else {
                                // String[] resArray = resOptions.split(",");
                                String text1 = resOptions.replaceAll("\\\\,", "##");
                                String[] resArray = text1.split(",");           
                                numberOfOptions = resArray.length;
                            }
                        }

                        /**
                         * The application will show error on page if two
                         * identical RESPONSE_LABEL has different
                         * RESPONSE_OPTIONS_TEXT
                         */
                        String[] mapResArray = (String[]) labelWithOptions.get(responseLabel);
                        String text1 = resOptions.replaceAll("\\\\,", "##");
                        String[] resArray = text1.split(",");
                        item_from_row.setResponseOptions(resArray);
                        logger.debug(item_from_row.getItemName());
                        if (labelWithOptions.containsKey(responseLabel)) {
                            if (!StringUtil.isBlank(resOptions)) {
                                for (int i = 0; i < resArray.length; i++) {
                                    if (!resArray[i].equals(mapResArray[i])) {
                                        errors.add(resPageMsg.getString("resp_label_with_different_resp_options") + " " + k + ", "
                                            + resPageMsg.getString("items_worksheet_with_dot"));
                                        htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("resp_label_with_different_resp_options_html_error"));
                                        break;
                                    }
                                }
                            }
                        } else {
                            labelWithOptions.put(responseLabel, resArray);
                        }

                        cell = sheet.getRow(k).getCell((short) 16);
                        String resValues = getValue(cell);
                        if (responseLabel.equalsIgnoreCase("text") || responseLabel.equalsIgnoreCase("textarea")) {
                            resValues = "text";
                        } else if ("file".equalsIgnoreCase(responseType)) {
                            resValues = "file";
                        }
                        if (!resNames.contains(responseLabel) && StringUtil.isBlank(resValues) && responseTypeId != ResponseType.TEXT.getId()
                            && responseTypeId != ResponseType.TEXTAREA.getId() && responseTypeId != ResponseType.INSTANT_CALCULATION.getId()) {
                            // << tbh #4180

                            // errors.add("The RESPONSE_VALUES column was blank
                            // at row " + k + ", Items worksheet.");
                            // htmlErrors.put(j + "," + k + ",16", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " "
                                + resPageMsg.getString("was_blank_at_row") +" " + k + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                            htmlErrors.put(j + ", " + k + ",16", resPageMsg.getString("required_field"));
                        }
                        // YW 1-25-2008 << validate scoring expression
                        if (responseTypeId == ResponseType.CALCULATION.getId()
                                    || responseTypeId == ResponseType.GROUP_CALCULATION.getId()) {
                            // right now, func is not required; but if there is
                            // func, it must be correctly spelled
                            if (resValues.contains(":")) {
                                String[] s = resValues.split(":");
                                if (!"func".equalsIgnoreCase(s[0].trim())) {
                                    errors.add(resPageMsg.getString("expression_not_start_with_func_at") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet") + ".");
                                    htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("INVALID_FIELD"));
                                }
                            }
                            String exp = resValues;
                            // make both \\, and , works for functions
                            exp = exp.replace("\\\\,", "##");
                            exp = exp.replace("##", ",");
                            exp = exp.replace(",", "\\\\,");
                            resValues = exp;
                            if (exp.startsWith("func:")) {
                                exp = exp.substring(5).trim();
                            }
                            exp = exp.replace("\\\\,", "##");
                            StringBuffer err = new StringBuffer();
                            ArrayList<String> variables = new ArrayList<String>();
                            ScoreValidator scoreValidator = new ScoreValidator(locale);
                            if (!scoreValidator.isValidExpression(exp, err, variables)) {
                                errors.add(resPageMsg.getString("expression_invalid_at") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ": "
                                    + err);
                                htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("INVALID_FIELD"));
                            }
                            if (exp.startsWith("getexternalvalue") || exp.startsWith("getExternalValue")) {
                                // do a different set of validations here, tbh
                            } else {
                                String group = groupLabel.length() > 0 ? groupLabel : "Ungrouped";
                                for (String v : variables) {
                                    if (!allItems.containsKey(v)) {
                                        errors.add("Item '" + v + "' must be listed before the item '" + itemName + "' at row " + k + ", items worksheet. ");
                                        htmlErrors.put(j + "," + k + ",16", "INVALID FIELD");
                                    } else {
                                        if (responseTypeId == 8 && !allItems.get(v).equalsIgnoreCase(group)) {
                                            errors.add("Item '" + v + "' and item '" + itemName + "' must have a same GROUP_LABEL at row " + k
                                                + ", items worksheet. ");
                                            htmlErrors.put(j + "," + k + ",16", "INVALID FIELD");
                                        } else if (responseTypeId == 9) {
                                            String g = allItems.get(v);
                                            if (!g.equalsIgnoreCase("ungrouped") && g.equalsIgnoreCase(group)) {
                                                errors.add("Item '" + v + "' and item '" + itemName + "' should not have a same GROUP_LABEL at row " + k
                                                    + ", items worksheet. ");
                                                htmlErrors.put(j + "," + k + ",16", "INVALID FIELD");
                                            }
                                        }
                                    }
                                }
                            }
                        } else if("instant-calculation".equalsIgnoreCase(responseType)) {
                            OnChangeSheetValidationCell onchangecell =
                                    new OnChangeSheetValidationCell(OnChangeSheetValidationType.ALL, new SheetCell.Builder().
                                            rowName(itemName).colTitle("RESPONSE_VALUES_column").colValue(resValues).
                                            forWhich("instant_calculation").sheetNum(j).rowNum(k).colNum(16).build());
                            instantValidator.addValidationCells(onchangecell);
                        } else if (numberOfOptions > 0) {
                            // YW >>
                            String value1 = resValues.replaceAll("\\\\,", "##");
                            String[] resValArray = value1.split(",");
                            if (resValArray.length != numberOfOptions) {
                                /*
                                 * errors.add("There are an incomplete number of
                                 * option-value pairs in " + "RESPONSE_OPTIONS
                                 * and RESPONSE_VALUES at row " + k + ",
                                 * questions worksheet; perhaps you are missing
                                 * a comma? If there is a comma in any option
                                 * text/value itself, please use \\, instead.");
                                 * htmlErrors.put(j + "," + k + ",15", "NUMBER
                                 * OF OPTIONS DOES NOT MATCH"); htmlErrors.put(j +
                                 * "," + k + ",16", "NUMBER OF VALUES DOES NOT
                                 * MATCH");
                                 */
                                errors.add(resPageMsg.getString("incomplete_option_value_pair") + " " + resPageMsg.getString("RESPONSE_OPTIONS_column") + " "
                                    + resPageMsg.getString("and") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " "+resPageMsg.getString("at_row") + k
                                    + " " + resPageMsg.getString("items_worksheet") + "; " + resPageMsg.getString("perhaps_missing_comma"));
                                htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("number_option_not_match"));
                                htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("number_value_not_match"));
                            }
                        }

                        /**
                         * The application will show error on page if two
                         * identical RESPONSE_LABEL has different REPONSE_VALUES
                         */
                        String[] mapValArray = (String[]) labelWithValues.get(responseLabel);
                        String value1 = resValues.replaceAll("\\\\,", "##");
                        String[] resValArray = value1.split(",");
                        if (labelWithValues.containsKey(responseLabel)) {
                            if (!StringUtil.isBlank(resValues)) {
								// @pgawade 31-May-2011 Added the check to
								// compare the size of resValArray and
								// mapValArray before comparing the individual
								// elements in them
								if (null != resValArray && null != mapValArray && resValArray.length != mapValArray.length) {
									errors.add(resPageMsg.getString("resp_label_with_different_resp_values") + " " + k + ", "
										+ resPageMsg.getString("items_worksheet") + ".");
									htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("resp_label_with_different_resp_values_html_error"));
								}
								else {
									for (int i = 0; i < resValArray.length; i++) {
										if (!resValArray[i].equals(mapValArray[i])) {
											errors.add(resPageMsg.getString("resp_label_with_different_resp_values") + " " + k + ", "
												+ resPageMsg.getString("items_worksheet_with_dot") );
											htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("resp_label_with_different_resp_values_html_error"));
											break;
										}
									}
								}
                            }
                            controlValues.put(secName+"---"+itemName, mapValArray);
                        } else {
                            labelWithValues.put(responseLabel, resValArray);
                            controlValues.put(secName+"---"+itemName, resValArray);
                        }

                        /*
                         * Adding two columns here for the repeating rows,
                         * REsPONSE_LAYOUT and DEFAULT_VALUE TBH, 06/05/2007 YW
                         * 08-02-2007: move default_value down after data_type
                         */

                        // RESPONSE_LAYOUT
                        cell = sheet.getRow(k).getCell((short) 17);
                        // should be horizontal or vertical, tbh
                        // BWP: the application will assume a vertical layout if
                        // this value is not horizontal
                        // BWP 08-02-2007 <<
                        String responseLayout = getValue(cell);
                        responseLayout = responseLayout.replaceAll("<[^>]*>", "");

                        // BWP >>
                        cell = sheet.getRow(k).getCell((short) 19);
                        String dataType = getValue(cell);
                        dataType = dataType.replaceAll("<[^>]*>", "");
                        item_from_row.setDataType(dataType);
                        String dataTypeIdString = "1";
                        if (StringUtil.isBlank(dataType)) {
                            // errors.add("The DATA_TYPE column was blank at row
                            // " + k + ", items worksheet.");
                            // htmlErrors.put(j + "," + k + ",19", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                + resPageMsg.getString("was_blank_at_row") +" " + k + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                            htmlErrors.put(j + "," + k + ",19", resPageMsg.getString("required_field"));

                        } else {
                            if (!ItemDataType.findByName(dataType.toLowerCase())) {
                                // errors.add("The DATA_TYPE column was invalid
                                // at row " + k + ", Items worksheet.");
                                // htmlErrors.put(j + "," + k + ",19", "INVALID
                                // FIELD");
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                    + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot") );
                                htmlErrors.put(j + "," + k + ",19", resPageMsg.getString("INVALID_FIELD"));
                            } else {
                                if ("file".equalsIgnoreCase(responseType) && !"FILE".equalsIgnoreCase(dataType)) {
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                        + resPageMsg.getString("should_be_file") + resPageMsg.getString("at_row") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet_with_dot"));
                                    htmlErrors.put(j + "," + k + ",19", resPageMsg.getString("should_be_file"));
                                } else if("instant-calculation".equalsIgnoreCase(responseType)) {
                                    OnChangeSheetValidationCell onchangecell =
                                            new OnChangeSheetValidationCell(OnChangeSheetValidationType.NONE, SheetValidationType.SHOULD_BE_ST,
                                                    new SheetCell.Builder().rowName(itemName).colTitle("DATA_TYPE_column").colValue(dataType).
                                                    forWhich("instant_calculation").sheetNum(j).rowNum(k).colNum(19).build());
                                        instantValidator.addValidationCells(onchangecell);
                                }
                                // dataTypeId =
                                // (ItemDataType.getByName(dataType)).getId();
                                dataTypeIdString = "(SELECT ITEM_DATA_TYPE_ID From ITEM_DATA_TYPE Where CODE='" + dataType.toUpperCase() + "')";
                            }
                        }

                        if (responseTypeId == 3 || responseTypeId == 5 || responseTypeId == 6 || responseTypeId == 7) {
                            if (labelWithType.containsKey(responseLabel)) {
                                // make sure same responseLabels have same
                                // datatype
                                if (!dataType.equalsIgnoreCase(labelWithType.get(responseLabel).toString())) {
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                        + resPageMsg.getString("does_not_match_the_item_data_type_with_the_same_response_label") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet_with_dot"));
                                    htmlErrors.put(j + "," + k + ",19", resPageMsg.getString("INVALID_FIELD"));
                                }
                            } else {
                                labelWithType.put(responseLabel, dataType);
                                // make sure response values matching datatype
                                if (resValArray.length > 0) {
                                    boolean wrongType = false;
                                    if ("int".equalsIgnoreCase(dataType)) {
                                        for (String s : resValArray) {
                                            String st = s != null && s.length() > 0 ? s.trim() : "";
                                            if (st.length() > 0) {
                                                try {
                                                    Integer I = Integer.parseInt(st);
                                                    // eg, s=2.3 => I=2,
                                                    // but 2.3 is not integer
                                                    if (!I.toString().equals(st)) {
                                                        wrongType = true;
                                                    }
                                                } catch (Exception e) {
                                                    wrongType = true;
                                                }
                                            }
                                        }
                                        if (wrongType) {
                                            wrongType = false;
                                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " "
                                                + resPageMsg.getString("should_be_integer") + " " + resPageMsg.getString("at_row") + " " + k + ", "
                                                + resPageMsg.getString("items_worksheet_with_dot") );
                                            htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("should_be_integer"));
                                        }
                                    } else if ("real".equalsIgnoreCase(dataType)) {
                                        for (String s : resValArray) {
                                            String st = s != null && s.length() > 0 ? s.trim() : "";
                                            if (st.length() > 0) {
                                                try {
                                                    Double I = Double.parseDouble(st);
                                                } catch (Exception e) {
                                                    wrongType = true;
                                                }
                                            }
                                        }
                                        if (wrongType) {
                                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " "
                                                + resPageMsg.getString("should_be_real") + " " + resPageMsg.getString("at_row") + " " + k + ", "
                                                + resPageMsg.getString("items_worksheet") + ".");
                                            htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("should_be_real"));
                                        }
                                    }
                                }
                            }
                        }

                        // DEFAULT_VALUE
                        // can be anything, tbh
                        //
                        // YW 08-02-2007 << in database, default_value has been
                        // set type as varchar(255);
                        // outside database, it's going to be tied with item's
                        // DATA_TYPE
                        // here, default_value has been handled for dataType =
                        // date
                        cell = sheet.getRow(k).getCell((short) 18);//default value
                        item_from_row.setDefaultValue(getValue(cell));
                        item_from_row.verifyDefaultValue(row_items, errors, htmlErrors, j, resPageMsg);
                        default_value= item_from_row.getDefaultValue();
                        
                        cellIndex = 19;
                        String widthDecimal = "";
                        logger.debug("hasWidthDecimalColumn=" + hasWDColumn);
                        if (hasWDColumn) {
                            ++cellIndex;
                            cell = sheet.getRow(k).getCell((short) cellIndex);
                            widthDecimal = getValue(cell);
                            if (StringUtil.isBlank(widthDecimal)) {
                                widthDecimal = "";
                            } else {
                                if ("single-select".equalsIgnoreCase(responseType) || "multi-select".equalsIgnoreCase(responseType)
                                    || "radio".equalsIgnoreCase(responseType) || "checkbox".equalsIgnoreCase(responseType)) {
                                    errors.add(resPageMsg.getString("error_message_for_width_decimal_at") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet") + ":" + " "
                                        + resPageMsg.getString("width_decimal_unavailable_for_single_multi_checkbox_radio"));
                                    htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                                } else {
                                    StringBuffer message = new StringBuffer();
                                    boolean isCalc = responseTypeId == 8 || responseTypeId == 9 ? true : false;
                                    message = Validator.validateWidthDecimalSetting(widthDecimal, dataType, isCalc, this.locale);
                                    if (message.length() > 0) {
                                        errors.add(resPageMsg.getString("error_message_for_width_decimal_at") + " " + k + ", "
                                            + resPageMsg.getString("items_worksheet") + ":" + " " + message);
                                        htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                                    }
                                }
                            }
                        }

                        ++cellIndex;
                        cell = sheet.getRow(k).getCell((short) cellIndex);
                        String regexp = getValue(cell);
                        String regexp1 = "";
                        if (!StringUtil.isBlank(regexp)) {
                            // parse the string and get reg exp eg. regexp:
                            // /[0-9]*/
                            regexp1 = regexp.trim();

                            if (regexp1.startsWith("regexp:")) {
                                String finalRegexp = regexp1.substring(7).trim();
                                // logger.debug("reg:" + finalRegexp);
                                if (finalRegexp.contains("\\\\")) {
                                    // \\ in the regular expression it should
                                    // not be allowed
                                    // errors.add("The VALIDATION column has an
                                    // invalid regular expression at row " + k
                                    // + ", Items worksheet. Regular expression
                                    // contained '\\\\', it should only contain
                                    // one '\\'. ");
                                    // htmlErrors.put(j + "," + k + ",21",
                                    // "INVALID FIELD");
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column") + " "
                                        + resPageMsg.getString("has_an_invalid_regular_expression_at_row") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet_with_dot") + resPageMsg.getString("regular_expression_contained") + " '\\\\', "
                                        + resPageMsg.getString("it_should_only_contain_one") + "'\\'. ");
                                    htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                                } else {
                                    if (finalRegexp.startsWith("/") && finalRegexp.endsWith("/")) {
                                        finalRegexp = finalRegexp.substring(1, finalRegexp.length() - 1);
                                        try {
                                            Pattern p = Pattern.compile(finalRegexp);
                                            // YW 11-21-2007 << add another \ if
                                            // there is \ in regexp
                                            char[] chars = regexp1.toCharArray();
                                            regexp1 = "";
                                            for (char c : chars) {
                                                if (c == '\\' && !dbName.equals("oracle")) {
                                                    regexp1 += c + "\\";
                                                } else {
                                                    regexp1 += c;
                                                }
                                            }
                                            // YW >>
                                        } catch (PatternSyntaxException pse) {
                                            // errors.add("The VALIDATION column
                                            // has an invalid regular expression
                                            // at row " + k
                                            // + ", Items worksheet. Example:
                                            // regexp: /[0-9]*/ ");
                                            // htmlErrors.put(j + "," + k +
                                            // ",21", "INVALID FIELD");
                                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column")
                                                + resPageMsg.getString("has_an_invalid_regular_expression_at_row") + " " + k + ", "
                                                + resPageMsg.getString("items_worksheet_with_dot") + resPageMsg.getString("Example") + " regexp: /[0-9]*/ ");
                                            htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                                        }
                                    } else {
                                        // errors.add("The VALIDATION column has
                                        // an invalid regular expression at row
                                        // " + k
                                        // + ", Items worksheet. Example:
                                        // regexp: /[0-9]*/ ");
                                        // htmlErrors.put(j + "," + k + ",21",
                                        // "INVALID FIELD");
                                        errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column")
                                            + resPageMsg.getString("has_an_invalid_regular_expression_at_row") + " " + k + ", "
                                            + resPageMsg.getString("items_worksheet_with_dot") + " "+resPageMsg.getString("Example") + " regexp: /[0-9]*/ ");
                                        htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                                    }
                                }

                            } else if (regexp1.startsWith("func:")) {
                                boolean isProperFunction = false;
                                try {
                                    Validator.processCRFValidationFunction(regexp1);
                                    isProperFunction = true;
                                } catch (Exception e) {
                                    // errors.add(e.getMessage() + ", at row " +
                                    // k
                                    // + ", Items worksheet." );
                                    // htmlErrors.put(j + "," + k + ",21",
                                    // "INVALID FIELD");
                                    errors.add(e.getMessage() + ", " + resPageMsg.getString("at_row") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet_with_dot"));
                                    htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                                }
                            } else {
                                // errors.add("The VALIDATION column was invalid
                                // at row " + k
                                // + ", Items worksheet. ");
                                // htmlErrors.put(j + "," + k + ",21", "INVALID
                                // FIELD");
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column") + " "
                                    + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                                htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_FIELD"));
                            }

                        }

                        ++cellIndex;
                        cell = sheet.getRow(k).getCell((short) cellIndex);
                        String regexpError = getValue(cell);
                        regexpError = regexpError.replaceAll("<[^>]*>", "");
                        if (!StringUtil.isBlank(regexp) && StringUtil.isBlank(regexpError)) {
                            // errors.add("The VALIDATION_ERROR_MESSAGE column
                            // was blank at row " + k
                            // + ", Items worksheet. It cannot be blank if
                            // VALIDATION is not blank.");
                            // htmlErrors.put(j + "," + k + ",22", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_ERROR_MESSAGE_column")
                                + resPageMsg.getString("was_blank_at_row") +" "+ k + ", " + resPageMsg.getString("items_worksheet_with_dot")
                                + " "+resPageMsg.getString("cannot_be_blank_if_VALIDATION_not_blank"));
                            htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("required_field"));
                        }
                        if (regexpError != null && regexpError.length() > 255) {
                            errors.add(resPageMsg.getString("regexp_errror_length_error"));
                        }

                        ++cellIndex;
                        boolean phiBoolean = false;
                        cell = sheet.getRow(k).getCell((short) cellIndex);
                        String phi = getValue(cell);
                        // String phi = "";
                        // logger.debug("++ phi: "+getValue(cell));
                        if (StringUtil.isBlank(phi)) {
                            phi = "0";
                        } else
                        // throws NPE, so added the guard clause above, tbh
                        // 06/07
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            double dphi = cell.getNumericCellValue();
                            if ((dphi - (int) dphi) * 1000 == 0) {
                                phi = (int) dphi + "";
                            }
                        }
                        if (!"0".equals(phi) && !"1".equals(phi)) {
                            // errors.add("The PHI column was invalid at row " +
                            // k
                            // + ", Items worksheet. PHI can only be either 0 or
                            // 1.");
                            // htmlErrors.put(j + "," + k + ",23", "INVALID
                            // VALUE");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("PHI_column") + resPageMsg.getString("was_invalid_at_row") + k
                                + ", " + resPageMsg.getString("items_worksheet_with_dot") + resPageMsg.getString("PHI_column") + " "
                                + resPageMsg.getString("can_only_be_either_0_or_1"));
                            htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_VALUE"));
                        } else {
                            phiBoolean = "1".equals(phi) ? true : false;
                        }

                        ++cellIndex;
                        boolean isRequired = false;
                        cell = sheet.getRow(k).getCell((short) cellIndex);
                        String required = getValue(cell);
                        // String required = "";
                        // added to stop NPEs, tbh 06/04/2007
                        if (StringUtil.isBlank(required)) {
                            required = "0";
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            double dr = cell.getNumericCellValue();
                            if ((dr - (int) dr) * 1000 == 0) {
                                required = (int) dr + "";
                            }
                        }

                        if (!"0".equals(required) && !"1".equals(required)) {
                            // errors.add("The REQUIRED column was invalid at
                            // row " + k
                            // + ", Items worksheet. REQUIRED can only be either
                            // 0 or 1. ");
                            // htmlErrors.put(j + "," + k + ",24", "INVALID
                            // VALUE");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("REQUIRED_column") + " "
                                + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot")
                                + resPageMsg.getString("REQUIRED_column") + resPageMsg.getString("can_only_be_either_0_or_1"));
                            htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_VALUE"));
                        } else {
                            isRequired = "1".equals(required) ? true : false;
                        }
                        // >> tbh 02/04/2010 adding this column for Dynamics
                        ++cellIndex;
                        boolean isShowItem = true;
                        // default is true
                        cell = sheet.getRow(k).getCell((short) cellIndex);
                        String showItem = getValue(cell);

                        if (!StringUtil.isBlank(showItem)) {
                            isShowItem = "0".equals(showItem) ? false : true;
                            isShowItem = "Hide".equalsIgnoreCase(showItem) ? false : true;
                            // supporting both, tbh 03/2010
                        }

                        ++cellIndex;
                        cell = sheet.getRow(k).getCell((short) cellIndex);
                        String display = getValue(cell);
                        String controlItemName = "",optionValue="", message="";
                        if (!StringUtil.isBlank(display)) {
                            if(isShowItem != false) {
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("ITEM_DISPLAY_STATUS_column") + " "
                                        + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot")
                                        + resPageMsg.getString("should_be_hide_for_scd"));
                                    htmlErrors.put(j + "," + k + "," + (cellIndex-1), resPageMsg.getString("INVALID_VALUE"));
                            }

                            String pvKey = secName+"---";
                            String d = display.replaceAll("\\\\,", "##");
                            String[] par = d.split(",");
                            //validate availability of item_label
                            if(par.length==3) {
                                String p0 = par[0].trim();
                                String p1 = par[1].trim();
                                String p2 = par[2].trim();
                                if(p0.length()>0 && p1.length()>0 && p2.length()>0) {
                                    if(SpreadSheetItemUtil.isItemWithSameParameterExistsIncludingMyself(p0,  row_items)) {
                                        controlItemName = p0; optionValue = p1; message = p2;
                                        pvKey+=p0;
                                        if(controlValues.containsKey(pvKey)) {
                                            String[] pvs = controlValues.get(pvKey);
                                            boolean existing = false;
                                            for(String s: pvs) {
                                                if(s.trim().equals(p1)) {
                                                    existing = true;
                                                    break;
                                                }
                                            }
                                            if(!existing){
                                                optionValue = "";
                                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SIMPLE_CONDITIONAL_DISPLAY_column") + " "
                                                        + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot")
                                                        + resPageMsg.getString("control_response_value_invalid") + " " + p1.replace("##", "\\\\,"));
                                                    htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_VALUE"));
                                            }
                                        }
                                    }else {
                                        errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SIMPLE_CONDITIONAL_DISPLAY_column") + " "
                                                + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot")
                                                + resPageMsg.getString("control_item_name_invalid") + " " + p0);
                                            htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_VALUE"));
                                    }
                                } else {
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SIMPLE_CONDITIONAL_DISPLAY_column") + " "
                                            + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot")
                                            + resPageMsg.getString("correct_pattern"));
                                        htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_VALUE"));
                                }
                            } else {
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SIMPLE_CONDITIONAL_DISPLAY_column") + " "
                                        + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot") 
                                        + resPageMsg.getString("correct_pattern"));
                                    htmlErrors.put(j + "," + k + "," + cellIndex, resPageMsg.getString("INVALID_VALUE"));
                            }
                        }

                        // Create oid for Item Bean
                        String itemOid = idao.getValidOid(new ItemBean(), crfName, itemName, itemOids);
                        itemOids.add(itemOid);

                        // better spot for checking item might be right here,
                        // tbh 7-25
                        String vlSql = "";
                        if (dbName.equals("oracle")) {

                           /* vlSql =
                                "INSERT INTO ITEM (NAME,DESCRIPTION,UNITS,PHI_STATUS,"
                                    + "ITEM_DATA_TYPE_ID, ITEM_REFERENCE_TYPE_ID,STATUS_ID,OWNER_ID,DATE_CREATED,OC_OID) " + "VALUES ('"
                                    + stripQuotes(itemName) + "','" + stripQuotes(descLabel) + "','" + stripQuotes(unit) + "'," + (phiBoolean == true ? 1 : 0)
                                    + "," + dataTypeIdString + ",1,1," + ub.getId() + ", sysdate" + ",'" + itemOid + "')";*/
                        	 vlSql =
                                     "INSERT INTO ITEM (NAME,DESCRIPTION,UNITS,PHI_STATUS,"
                                         + "ITEM_DATA_TYPE_ID, ITEM_REFERENCE_TYPE_ID,STATUS_ID,OWNER_ID,DATE_CREATED,OC_OID) " 
                                    	 + "VALUES (?,?,?," + (phiBoolean == true ? 1 : 0)
                                    	 + "," + dataTypeIdString + ",1,1," + ub.getId() + ", sysdate" + ",?)";

                        } else {
                            vlSql =
                                "INSERT INTO ITEM (NAME,DESCRIPTION,UNITS,PHI_STATUS,"
                                    + "ITEM_DATA_TYPE_ID, ITEM_REFERENCE_TYPE_ID,STATUS_ID,OWNER_ID,DATE_CREATED,OC_OID) " 
                                	+ "VALUES (?,?,?," + phiBoolean 
                                	+ "," + dataTypeIdString + ",1,1," + ub.getId() + ", NOW()" + ",?)";
                        }

                        //backupItemQueries.put(itemName, vlSql);
                        ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                        QueryObject qo = new QueryObject();
                        
                        sqlParameters.add(new SqlParameter(itemName));
                        sqlParameters.add(new SqlParameter(descLabel));
                        sqlParameters.add(new SqlParameter(unit));
                        sqlParameters.add(new SqlParameter(itemOid));
                                                           
                        qo = new QueryObject();
                        qo.setSql(vlSql);
                        qo.setSqlParameters(sqlParameters);
                        backupItemQueries.put(itemName, qo);
                        
                        // to compare items from DB later, if two items have the
                        // same name,
                        // but different units or phiStatus, they are different
                        ItemBean ib = new ItemBean();
                        ib.setName(itemName);
                        ib.setUnits(unit);
                        ib.setPhiStatus(phiBoolean);
                        ib.setDescription(descLabel);
                        ib.setDataType(ItemDataType.getByName(dataType.toLowerCase()));

                        // put metadata into item
                        ResponseSetBean rsb = new ResponseSetBean();
                        // notice that still "\\," in options - jxu-08-31-06
                        String updatedResOptions = resOptions.replaceAll("\\\\,", "\\,");
                        String updatedResValues = resValues.replaceAll("\\\\,", "\\,");

                        //following rsb used in isResponseValid in CreateCRFVersionServlet for comparing response
                        // options text and values between form versions. Please keep as is. - Z 19-Jun-2020
                        rsb.setOptions(stripQuotes(updatedResOptions), stripQuotes(updatedResValues));

                        ItemFormMetadataBean ifmb = new ItemFormMetadataBean();
                        ifmb.setResponseSet(rsb);
                        ifmb.setShowItem(isShowItem);
                        ib.setItemMeta(ifmb);
                        items.put(itemName, ib);

                        int ownerId = ub.getId();

                        if (!itemCheck.containsKey(itemName)) {// item not in
                            // the DB
                            //openQueries.put(itemName, vlSql);
                        	openQueries.put(itemName, qo);

                        } else {// item in the DB
                            ItemBean oldItem = (ItemBean) idao.findByNameAndCRFId(itemName, crfId);
                            if (oldItem.getOwnerId() == ub.getId()) {// owner
                                // can
                                // update
                                if (!cvdao.hasItemData(oldItem.getId())) {// no
                                    // item
                                    // data
                                    String upSql = "";
                                    if (dbName.equals("oracle")) {
                                        upSql =
                                            "UPDATE ITEM SET DESCRIPTION=?,UNITS=?,"
                                                + "PHI_STATUS=" + (phiBoolean ? 1 : 0) + "," + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                + " AND item.item_id = versioning_map.item_id)" + " AND item.name=? "
                                                + " AND item.owner_id = ?";
                                    } else {
                                        upSql =
                                            "UPDATE ITEM SET DESCRIPTION=?,UNITS=?,"
                                                + "PHI_STATUS=" + phiBoolean
                                                + ","
                                                + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                // added by jxu 08-29-06 to fix
                                                // the missing from clause bug
                                                + " FROM versioning_map, crf_version" + " WHERE item.name=? AND item.owner_id =? "
                                                + " AND item.item_id = versioning_map.item_id AND"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;
                                    }// end of if dbName
                                    //openQueries.put(itemName, upSql);
                                    sqlParameters = new ArrayList<>();
                                    
                                    sqlParameters.add(new SqlParameter(descLabel));
                                    sqlParameters.add(new SqlParameter(unit));
                                    sqlParameters.add(new SqlParameter(itemName));
                                    sqlParameters.add(new SqlParameter(ownerId+"",JDBCType.INTEGER));
                                                                       
                                    qo = new QueryObject();
                                    qo.setSql(upSql);
                                    qo.setSqlParameters(sqlParameters);
                                    
                                    openQueries.put(itemName, qo);
                                } else {
                               	 String upSql = "";
                             	if(oldItem.getDataType() == oldItem.getDataType().DATE && ib.getDataType() == ib.getDataType().PDATE)//New Feature allow date to pdate even if the data is entered
                             	{

                                        if (dbName.equals("oracle")) {
                                            /*upSql =
                                                "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel)
                                                    + "',PHI_STATUS=" + (phiBoolean ? 1 : 0) + "," + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                    + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                    + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                    + " AND item.item_id = versioning_map.item_id)" + " AND item.name='" + stripQuotes(itemName)
                                                    + "' AND item.owner_id = " + ownerId;*/
                                        	upSql =
                                                    "UPDATE ITEM SET DESCRIPTION=? "
                                                        + ",PHI_STATUS=" + (phiBoolean ? 1 : 0) + "," + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                        + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                        + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                        + " AND item.item_id = versioning_map.item_id)" + " AND item.name=? " 
                                                        + " AND item.owner_id = ?";
                                        } else {
                                           /* upSql =
                                                "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel)
                                                    + "',PHI_STATUS=" + phiBoolean
                                                    + ","
                                                    + "ITEM_DATA_TYPE_ID="
                                                    + dataTypeIdString
                                                    + " FROM versioning_map, crf_version" + " WHERE item.name='" + stripQuotes(itemName) + "' AND item.owner_id = "
                                                    + ownerId + " AND item.item_id = versioning_map.item_id AND"
                                                    + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;*/
                                        	 upSql =
                                                     "UPDATE ITEM SET DESCRIPTION=? "
                                                         + ",PHI_STATUS=" + phiBoolean
                                                         + ","
                                                         + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                         + " FROM versioning_map, crf_version" + " WHERE item.name=? AND item.owner_id =? "
                                                         + " AND item.item_id = versioning_map.item_id AND"
                                                         + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;
                                        }// end of if dbName
                                        
                                        sqlParameters = new ArrayList<>();
                                        
                                        sqlParameters.add(new SqlParameter(descLabel));
                                        sqlParameters.add(new SqlParameter(itemName));
                                        sqlParameters.add(new SqlParameter(ownerId+"",JDBCType.INTEGER));
                                                                           
                                        qo = new QueryObject();
                                        qo.setSql(upSql);
                                        qo.setSqlParameters(sqlParameters);

                             	}
                             	else{
                             		if (dbName.equals("oracle")) {

                                       /* upSql =
                                            "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel) + "'," + "PHI_STATUS=" + (phiBoolean ? 1 : 0)
                                                + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                + " AND item.item_id = versioning_map.item_id)" + " AND item.name='" + stripQuotes(itemName)
                                                + "' AND item.owner_id = " + ownerId;*/
                             			 upSql =
                                                 "UPDATE ITEM SET DESCRIPTION=?,PHI_STATUS=" + (phiBoolean ? 1 : 0)
                                                     + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                     + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                     + " AND item.item_id = versioning_map.item_id)" + " AND item.name=? "
                                                     + " AND item.owner_id = ?";	
                                    } else {
                                       /* upSql =
                                            "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel) + "'," + "PHI_STATUS=" + phiBoolean
                                                + " FROM versioning_map, crf_version" + " WHERE item.name='" + stripQuotes(itemName) + "' AND item.owner_id = "
                                                + ownerId + " AND item.item_id = versioning_map.item_id AND"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;*/
                                    	 upSql =
                                                 "UPDATE ITEM SET DESCRIPTION=?,PHI_STATUS=" + phiBoolean
                                                     + " FROM versioning_map, crf_version" + " WHERE item.name=? AND item.owner_id =? "
                                                     + " AND item.item_id = versioning_map.item_id AND"
                                                     + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;	
                                    }// end of if dbName
                             		
                             		 sqlParameters = new ArrayList<>();
                                     
                                     sqlParameters.add(new SqlParameter(descLabel));
                                     sqlParameters.add(new SqlParameter(itemName));
                                     sqlParameters.add(new SqlParameter(ownerId+"",JDBCType.INTEGER));
                                                                        
                                     qo = new QueryObject();
                                     qo.setSql(upSql);
                                     qo.setSqlParameters(sqlParameters);
                             	}                             	                             	
                                 
                                 openQueries.put(itemName, qo);
                                 
                                }
                            } else {
                                ownerId = oldItem.getOwner().getId();
                            }
                        }
                        String sql = "";
                        sqlParameters = new ArrayList<>();
                        if (dbName.equals("oracle")) {
                            /*sql =
                                "INSERT INTO RESPONSE_SET (LABEL, OPTIONS_TEXT, OPTIONS_VALUES, " + "RESPONSE_TYPE_ID, VERSION_ID)" + " VALUES ('"
                                    + stripQuotes(responseLabel) + "', '" + stripQuotes(resOptions.replaceAll("\\\\,", "\\,")) + "','"
                                    + stripQuotes(resValues.replace("\\\\", "\\")) + "'," + "(SELECT RESPONSE_TYPE_ID From RESPONSE_TYPE Where NAME='"
                                    + stripQuotes(responseType.toLowerCase()) + "')," + versionIdString + ")";*/
                        	sql =
                                    "INSERT INTO RESPONSE_SET (LABEL, OPTIONS_TEXT, OPTIONS_VALUES, RESPONSE_TYPE_ID, VERSION_ID)" 
                                   		 + " VALUES (?, ?, ?,(SELECT RESPONSE_TYPE_ID From RESPONSE_TYPE Where NAME=?),"+ versionIdString + ")";
                       	  sqlParameters.add(new SqlParameter(responseLabel));
                             sqlParameters.add(new SqlParameter(resOptions.replaceAll("\\\\,", "\\,")));
                             sqlParameters.add(new SqlParameter(resValues.replace("\\\\", "\\")));
                             sqlParameters.add(new SqlParameter(responseType.toLowerCase()));
                            
                        } else {
                            /*sql =
                                "INSERT INTO RESPONSE_SET (LABEL, OPTIONS_TEXT, OPTIONS_VALUES, " + "RESPONSE_TYPE_ID, VERSION_ID)" + " VALUES ('"
                                    + stripQuotes(responseLabel) + "', E'" + stripQuotes(resOptions) + "', E'" + stripQuotes(resValues) + "',"
                                    + "(SELECT RESPONSE_TYPE_ID From RESPONSE_TYPE Where NAME='" + stripQuotes(responseType.toLowerCase()) + "'),"
                                    + versionIdString + ")";*/
                        	 sql =
                                     "INSERT INTO RESPONSE_SET (LABEL, OPTIONS_TEXT, OPTIONS_VALUES, RESPONSE_TYPE_ID, VERSION_ID)" 
                                    		 + " VALUES (?, ?, ?,(SELECT RESPONSE_TYPE_ID From RESPONSE_TYPE Where NAME=?),"+ versionIdString + ")";
                        	  sqlParameters.add(new SqlParameter(responseLabel));
                              sqlParameters.add(new SqlParameter(updatedResOptions));
                              sqlParameters.add(new SqlParameter(updatedResValues));
                              sqlParameters.add(new SqlParameter(responseType.toLowerCase()));
                             
                        }
                        // YW << a response Label can not be used for more than
                        // one response type
                        if (!resPairs.contains(responseLabel.toString().toLowerCase() + "_" + responseType.toString().toLowerCase())) {
                            // YW >>
                            if (!resNames.contains(responseLabel)) {
                                //queries.add(sql);
                            	qo = new QueryObject();
                                qo.setSql(sql);
                                qo.setSqlParameters(sqlParameters);
                                
                                queries.add(qo);
                                resNames.add(responseLabel);
                            }
                            // this will have to change since we have some data
                            // in the actual
                            // spreadsheet
                            // change it to caching response set names in a
                            // collection?
                            // or just delete the offending cells from the
                            // spreadsheet?

                            // YW <<
                            else {
                                errors.add("Error found at row \"" + (k + 1) + "\" in items worksheet. ResponseLabel \"" + responseLabel
                                    + "\" for ResponseType \"" + responseType + "\" has been used for another ResponseType.  ");
                                htmlErrors.put(j + "," + k + ",14", "INVALID FIELD");
                            }
                            resPairs.add(responseLabel.toString().toLowerCase() + "_" + responseType.toString().toLowerCase());
                            // YW >>
                        }

                        String parentItemString = "0";
                        if (!StringUtil.isBlank(parentItem)) {
                            if (dbName.equals("oracle")) {
                                parentItemString =
                                    "(SELECT MAX(ITEM_ID) FROM ITEM WHERE NAME='" + parentItem + "' AND owner_id = " + ownerId + " )";
                            } else {
                                parentItemString =
                                    "(SELECT ITEM_ID FROM ITEM WHERE NAME='" + parentItem + "' AND owner_id = " + ownerId
                                        + " ORDER BY OC_OID DESC LIMIT 1)";
                            }
                        }

                        String selectCorrectItemQueryPostgres =
                            " (SELECT I.ITEM_ID FROM ITEM I LEFT OUTER JOIN ITEM_FORM_METADATA IFM ON I.ITEM_Id = IFM.ITEM_ID LEFT OUTER JOIN CRF_VERSION CV ON IFM.CRF_VERSION_ID = CV.CRF_VERSION_ID  WHERE "
                                + " ( I.NAME='"
                                + itemName
                                + "'"
                                + " AND I.owner_id = "
                                + ownerId
                                + " AND CV.CRF_VERSION_ID is null )"
                                + " OR "
                                + " ( I.NAME='"
                                + itemName
                                + "'"
                                + " AND I.owner_id = "
                                + ownerId
                                + " AND CV.CRF_VERSION_ID is not null AND CV.CRF_ID ="
                                + crfId
                                + " ) "
                                + " ORDER BY I.OC_OID DESC LIMIT 1) ";

                        String selectCorrectItemQueryOracle =
                            " (SELECT MAX(I.ITEM_ID) FROM ITEM I LEFT OUTER JOIN ITEM_FORM_METADATA IFM ON I.ITEM_Id = IFM.ITEM_ID LEFT OUTER JOIN CRF_VERSION CV ON IFM.CRF_VERSION_ID = CV.CRF_VERSION_ID  WHERE "
                                + " ( I.NAME='"
                                + itemName
                                + "'"
                                + " AND I.owner_id = "
                                + ownerId
                                + " AND CV.CRF_VERSION_ID is null )"
                                + " OR "
                                + " ( I.NAME='"
                                + itemName + "'" + " AND I.owner_id = " + ownerId + " AND CV.CRF_VERSION_ID is not null AND CV.CRF_ID =" + crfId + " )) ";

                        String sql2 = "";
                        sqlParameters = new ArrayList<>();
                        if (dbName.equals("oracle")) {
                            /*sql2 =
                                "INSERT INTO ITEM_FORM_METADATA (CRF_VERSION_ID, RESPONSE_SET_ID," + "ITEM_ID,SUBHEADER,HEADER,LEFT_ITEM_TEXT,"
                                    + "RIGHT_ITEM_TEXT,PARENT_ID,SECTION_ID,ORDINAL,PARENT_LABEL,COLUMN_NUMBER,PAGE_NUMBER_LABEL,question_number_label,"
                                    + "REGEXP,REGEXP_ERROR_MSG,REQUIRED,DEFAULT_VALUE,RESPONSE_LAYOUT,WIDTH_DECIMAL, show_item)" + " VALUES ("
                                    + versionIdString
                                    + ",(SELECT RESPONSE_SET_ID FROM RESPONSE_SET WHERE LABEL='"
                                    + stripQuotes(responseLabel)
                                    + "'"
                                    + " AND VERSION_ID="
                                    + versionIdString
                                    + "),"
                                    + selectCorrectItemQueryOracle
                                    + ",'"
                                    + stripQuotes(subHeader)
                                    + "','"
                                    + stripQuotes(header)
                                    + "','"
                                    + stripQuotes(leftItemText)
                                    + "','"
                                    + stripQuotes(rightItemText)
                                    + "',"
                                    + parentItemString
                                    + ", (SELECT SECTION_ID FROM SECTION WHERE LABEL='"
                                    + secName
                                    + "' AND "
                                    + "CRF_VERSION_ID IN "
                                    + versionIdString
                                    + "), "
                                    + k
                                    + ",'"
                                    + parentItem
                                    + "',"
                                    + columnNum
                                    + ",'"
                                    + stripQuotes(page)
                                    + "','"
                                    + stripQuotes(questionNum)
                                    + "','"
                                    + stripQuotes(regexp1)
                                    + "','"
                                    + stripQuotes(regexpError)
                                    + "', "
                                    + (isRequired ? 1 : 0)
                                    + ", '"
                                    + stripQuotes(default_value)
                                    + "','"
                                    + stripQuotes(responseLayout)
                                    + "','"
                                    + widthDecimal
                                    + "', "
                                    + (isShowItem ? 1 : 0)
                                    + ")";*/
                        	sql2 =
                                    "INSERT INTO ITEM_FORM_METADATA (CRF_VERSION_ID, RESPONSE_SET_ID," + "ITEM_ID,SUBHEADER,header,LEFT_ITEM_TEXT,"
                                        + "RIGHT_ITEM_TEXT,PARENT_ID,SECTION_ID,ORDINAL,PARENT_LABEL,COLUMN_NUMBER,PAGE_NUMBER_LABEL,question_number_label,"
                                        + "REGEXP,REGEXP_ERROR_MSG,REQUIRED)" + " VALUES ("
                                        + versionIdString
                                        + ",(SELECT RESPONSE_SET_ID FROM RESPONSE_SET WHERE LABEL=?"
                                        + " AND VERSION_ID="
                                        + versionIdString
                                        + "),"
                                        + selectCorrectItemQueryOracle
                                        + ",?, ?, ?, ?," //subheader, header, leftItemText, rightItemText
                                        + parentItemString
                                        + ", (SELECT SECTION_ID FROM SECTION WHERE LABEL='"
                                        + secName
                                        + "' AND "
                                        + "CRF_VERSION_ID IN "
                                        + versionIdString
                                        + "), "
                                        + k
                                        + ",'"
                                        + parentItem
                                        + "',"
                                        + columnNum
                                        + ",?,?,?,?, "// page, questionNum, regexp1, regexpError
                                        + (isRequired ? 1 : 0)
                                        + ",?,?,'" + widthDecimal + "', " //default_value, responseLayout
                                        + (isShowItem ? 1 : 0)
                                        + ")";
                       

                        } else {
                            /*sql2 =
                                "INSERT INTO ITEM_FORM_METADATA (CRF_VERSION_ID, RESPONSE_SET_ID," + "ITEM_ID,SUBHEADER,HEADER,LEFT_ITEM_TEXT,"
                                    + "RIGHT_ITEM_TEXT,PARENT_ID,SECTION_ID,ORDINAL,PARENT_LABEL,COLUMN_NUMBER,PAGE_NUMBER_LABEL,question_number_label,"
                                    + "REGEXP,REGEXP_ERROR_MSG,REQUIRED,DEFAULT_VALUE,RESPONSE_LAYOUT,WIDTH_DECIMAL, show_item)" + " VALUES ("
                                    + versionIdString
                                    + ",(SELECT RESPONSE_SET_ID FROM RESPONSE_SET WHERE LABEL='"
                                    + stripQuotes(responseLabel)
                                    + "'"
                                    + " AND VERSION_ID="
                                    + versionIdString
                                    + "),"
                                    + selectCorrectItemQueryPostgres
                                    + ",'"
                                    + stripQuotes(subHeader)
                                    + "','"
                                    + stripQuotes(header)
                                    + "','"
                                    + stripQuotes(leftItemText)
                                    + "','"
                                    + stripQuotes(rightItemText)
                                    + "',"
                                    + parentItemString
                                    + ", (SELECT SECTION_ID FROM SECTION WHERE LABEL='"
                                    + secName
                                    + "' AND "
                                    + "CRF_VERSION_ID IN "
                                    + versionIdString
                                    + "), "
                                    + k
                                    + ",'"
                                    + parentItem
                                    + "',"
                                    + columnNum
                                    + ",'"
                                    + stripQuotes(page)
                                    + "','"
                                    + stripQuotes(questionNum)
                                    + "','"
                                    + stripQuotes(regexp1)
                                    + "','"
                                    + stripQuotes(regexpError)
                                    + "', "
                                    + isRequired
                                    + ", '"
                                    + stripQuotes(default_value)
                                    + "','"
                                    + stripQuotes(responseLayout) + "','" + widthDecimal + "'," + isShowItem
                                    + ")";*/
                        	sql2 =
                                    "INSERT INTO ITEM_FORM_METADATA (CRF_VERSION_ID, RESPONSE_SET_ID," + "ITEM_ID,SUBHEADER,HEADER,LEFT_ITEM_TEXT,"
                                        + "RIGHT_ITEM_TEXT,PARENT_ID,SECTION_ID,ORDINAL,PARENT_LABEL,COLUMN_NUMBER,PAGE_NUMBER_LABEL,question_number_label,"
                                        + "REGEXP,REGEXP_ERROR_MSG,REQUIRED,DEFAULT_VALUE,RESPONSE_LAYOUT,WIDTH_DECIMAL, show_item)" + " VALUES ("
                                        + versionIdString
                                        + ",(SELECT RESPONSE_SET_ID FROM RESPONSE_SET WHERE LABEL=?" //responseLabel
                                        + " AND VERSION_ID="
                                        + versionIdString
                                        + "),"
                                        + selectCorrectItemQueryPostgres                                       
                                        + ",?, ?, ?, ?, "  //subheader, header, leftItemText, rightItemText
                                        + parentItemString
                                        + ", (SELECT SECTION_ID FROM SECTION WHERE LABEL='"
                                        + secName
                                        + "' AND "
                                        + "CRF_VERSION_ID IN "
                                        + versionIdString
                                        + "), "
                                        + k
                                        + ",'"
                                        + parentItem
                                        + "',"
                                        + columnNum
                                        + ",?,?,?,?," //page, questionNum, regexp1, regexpError
                                        + isRequired
                                        + ",?,?,'"  //default_value, responseLayout
                                        + widthDecimal + "'," + isShowItem
                                        + ")";

                        }
                        //queries.add(sql2);

                        sqlParameters.add(new SqlParameter(responseLabel));

                        sqlParameters.add(new SqlParameter(subHeader));
                        sqlParameters.add(new SqlParameter(header));
                        sqlParameters.add(new SqlParameter(leftItemText));
                        sqlParameters.add(new SqlParameter(rightItemText));

                        sqlParameters.add(new SqlParameter(page));
                        sqlParameters.add(new SqlParameter(questionNum));
                        sqlParameters.add(new SqlParameter(regexp1));
                        sqlParameters.add(new SqlParameter(regexpError));

                        sqlParameters.add(new SqlParameter(default_value));
                        sqlParameters.add(new SqlParameter(responseLayout));
                        
                        qo = new QueryObject();
                        qo.setSql(sql2);
                        qo.setSqlParameters(sqlParameters);
                        
                        queries.add(qo);


                        // link version with items now
                        String sql3 = "";
                        if (dbName.equals("oracle")) {
                            /*sql3 =
                                "INSERT INTO VERSIONING_MAP (CRF_VERSION_ID, ITEM_ID) VALUES ( " + versionIdString + "," + selectCorrectItemQueryOracle + ")";*/
                        	sql3 =
                                    "INSERT INTO VERSIONING_MAP (CRF_VERSION_ID, ITEM_ID) VALUES ( " + versionIdStringWithParameter + "," + selectCorrectItemQueryOracle + ")";
                        } else {
                            /*sql3 =
                                "INSERT INTO VERSIONING_MAP (CRF_VERSION_ID, ITEM_ID) VALUES ( " + versionIdString + "," + selectCorrectItemQueryPostgres + ")";*/
                        	sql3 =
                                    "INSERT INTO VERSIONING_MAP (CRF_VERSION_ID, ITEM_ID) VALUES ( " + versionIdStringWithParameter + "," + selectCorrectItemQueryPostgres + ")";
                        }
                        //queries.add(sql3);
                        sqlParameters = new ArrayList<>();
                       //in versionIdStringWithParameter there is one parameter:crfId
                        sqlParameters.add(new SqlParameter(crfId+"",JDBCType.INTEGER));                   
                        
                        qo = new QueryObject();
                        qo.setSql(sql3);
                        qo.setSqlParameters(sqlParameters);
                        
                        queries.add(qo);

                        String sql2_1 = "";
                        if(display.length() > 0) {
                            if(controlItemName.length()>0 && optionValue.length()>0 && message.length()>0) {
                                //At this point, all errors for scd should be caught; and insert into item_form_metadata should be done
                               /* if (dbName.equals("oracle")) {
                                    sql2_1 = "insert into scd_item_metadata (scd_item_form_metadata_id,control_item_form_metadata_id,control_item_name,"
                                        + "option_value,message) values("
                                        + "(select max(ifm.item_form_metadata_id) from item_form_metadata ifm where ifm.item_id=" + selectCorrectItemQueryOracle
                                        + "and ifm.show_item=0 ),"
                                        + "(select cifm.item_form_metadata_id from item, item_form_metadata cifm"
                                        + " where cifm.crf_version_id = " + versionIdString
                                        + " and item.item_id = (select it.item_id from item it, versioning_map vm where it.name = '" + controlItemName +"'"
                                        + " and vm.crf_version_id = " + versionIdString + " and vm.item_id = it.item_id)"
                                        + " and cifm.item_id = item.item_id), "
                                        + "'" + controlItemName + "', '" + stripQuotes(optionValue) + "', '" + stripQuotes(message) + "'"
                                        + ")";*/
                            	 if (dbName.equals("oracle")) {
                                     sql2_1 = "insert into scd_item_metadata (scd_item_form_metadata_id,control_item_form_metadata_id,control_item_name,"
                                         + "option_value,message) values("
                                         + "(select max(ifm.item_form_metadata_id) from item_form_metadata ifm where ifm.item_id=" + selectCorrectItemQueryOracle
                                         + "and ifm.show_item=0 ),"
                                         + "(select cifm.item_form_metadata_id from item, item_form_metadata cifm"
                                         + " where cifm.crf_version_id = " + versionIdString
                                         + " and item.item_id = (select it.item_id from item it, versioning_map vm where it.name = '" + controlItemName +"'"
                                         + " and vm.crf_version_id = " + versionIdString + " and vm.item_id = it.item_id)"
                                         + " and cifm.item_id = item.item_id), "
                                         + "?,?,?"
                                         + ")";
                                } else {
                                  /*  sql2_1 = "insert into scd_item_metadata (scd_item_form_metadata_id,control_item_form_metadata_id,control_item_name,"
                                        + "option_value,message) values("
                                        + "(select max(ifm.item_form_metadata_id) from item_form_metadata ifm where ifm.item_id=" + selectCorrectItemQueryPostgres
                                        + "and ifm.show_item=false ),"
                                        + "(select cifm.item_form_metadata_id from item, item_form_metadata cifm"
                                        + " where cifm.crf_version_id = " + versionIdString
                                        + " and item.item_id = (select it.item_id from item it, versioning_map vm where it.name = '" + controlItemName +"'"
                                        + " and vm.crf_version_id = " + versionIdString + " and vm.item_id = it.item_id)"
                                        + " and cifm.item_id = item.item_id), "
                                        + "'" + controlItemName + "', '" + stripQuotes(optionValue) + "', '" + stripQuotes(message) + "'"
                                        + ")";*/
                                	  sql2_1 = "insert into scd_item_metadata (scd_item_form_metadata_id,control_item_form_metadata_id,control_item_name,"
                                              + "option_value,message) values("
                                              + "(select max(ifm.item_form_metadata_id) from item_form_metadata ifm where ifm.item_id=" + selectCorrectItemQueryPostgres
                                              + "and ifm.show_item=false ),"
                                              + "(select cifm.item_form_metadata_id from item, item_form_metadata cifm"
                                              + " where cifm.crf_version_id = " + versionIdString
                                              + " and item.item_id = (select it.item_id from item it, versioning_map vm where it.name = '" + controlItemName +"'"
                                              + " and vm.crf_version_id = " + versionIdString + " and vm.item_id = it.item_id)"
                                              + " and cifm.item_id = item.item_id), "
                                              + "?,?,?"
                                              + ")";
                                }
                                //queries.add(sql2_1);
                            	 sqlParameters = new ArrayList<>();
                                 sqlParameters.add(new SqlParameter(controlItemName));
                                 sqlParameters.add(new SqlParameter(optionValue));
                                 sqlParameters.add(new SqlParameter(message));
                                 
                                 qo = new QueryObject();
                                 qo.setSql(sql2_1);
                                 qo.setSqlParameters(sqlParameters);
                                 
                                 queries.add(qo);
         						
                            } else {
                                logger.debug("No insert into scd_item_metadata for item name = " + itemName +
                                        "with Simple_Conditional_Display = \"" + display + "\".");
                            }
                        }

                        // if (!StringUtil.isBlank(groupLabel)) {
                        // //add the item and the group label together
                        // //so that we can extract them
                        // //later down the road, tbh
                        // itemsToGrouplabels.put(itemName,groupLabel);
                        // }
                        if (!StringUtil.isBlank(groupLabel)) {
                            ItemGroupBean itemGroup;
                            ItemGroupMetadataBean igMeta;

                            igMeta = new ItemGroupMetadataBean();
                            itemGroup = new ItemGroupBean();

                            try {
                                logger.debug("found " + groupLabel);
                                itemGroup = (ItemGroupBean) itemGroups.get(groupLabel);
                                logger.debug("*** Found " + groupLabel + " and matched with " + itemGroup.getName());

                                // if(itemGroup != null){
                                igMeta = itemGroup.getMeta();
                                // } else {
                                // itemGroup = new ItemGroupBean();
                                // }

                                if (igMeta == null) {
                                    igMeta = new ItemGroupMetadataBean();
                                }

                                // above throws Nullpointer, need to change so
                                // that it does not, tbh 07-08-07

                                String sqlGroupLabel = "";
                                if (dbName.equals("oracle")) {
                                    sqlGroupLabel =
                                        "INSERT INTO ITEM_GROUP_METADATA (" + "item_group_id,HEADER," + "subheader, layout, repeat_number, repeat_max,"
                                            + " repeat_array,row_start_number, crf_version_id," + "item_id , ordinal, show_group, repeating_group) VALUES ("
                                            + "(SELECT MAX(ITEM_GROUP_ID) FROM ITEM_GROUP WHERE NAME='"
                                            + itemGroup.getName()
                                            + "' AND crf_id = "
                                            + crfId
                                            + " ),'"
                                            + igMeta.getHeader()
                                            + "', '"
                                            + igMeta.getSubheader()
                                            + "', '"
                                            +
                                            // above removed?
                                            igMeta.getLayout()
                                            + "', "
                                            +
                                            // above removed?
                                            igMeta.getRepeatNum()
                                            + ", "
                                            + igMeta.getRepeatMax()
                                            + ", '"
                                            + igMeta.getRepeatArray()
                                            + "', "
                                            +
                                            // above removed?
                                            igMeta.getRowStartNumber()
                                            + ",?"                                            
                                            + ","
                                            + "(SELECT MAX(ITEM.ITEM_ID) FROM ITEM,ITEM_FORM_METADATA,CRF_VERSION WHERE ITEM.NAME='"
                                            + itemName
                                            + "' "
                                            + "AND ITEM.ITEM_ID = ITEM_FORM_METADATA.ITEM_ID and ITEM_FORM_METADATA.CRF_VERSION_ID=CRF_VERSION.CRF_VERSION_ID "
                                            + "AND CRF_VERSION.CRF_ID= " + crfId + " ),"
                                            + k + ", "
                                            + (igMeta.isShowGroup() ? 1 : 0) + ", " + (igMeta.isRepeatingGroup() ? 1 : 0) + ")";

                                } else {
                                    sqlGroupLabel =
                                        "INSERT INTO ITEM_GROUP_METADATA (" + "item_group_id,header," + "subheader, layout, repeat_number, repeat_max,"
                                            + " repeat_array,row_start_number, crf_version_id," + "item_id , ordinal, show_group, repeating_group) VALUES ("
                                            + "(SELECT ITEM_GROUP_ID FROM ITEM_GROUP WHERE NAME='"
                                            + itemGroup.getName()
                                            + "' AND crf_id = "
                                            + crfId
                                            + " LIMIT 1),E'"
                                            + igMeta.getHeader()
                                            + "', E'"
                                            + igMeta.getSubheader()
                                            + "', '"
                                            +
                                            // above removed?
                                            igMeta.getLayout()
                                            + "', "
                                            +
                                            // above removed?
                                            igMeta.getRepeatNum()
                                            + ", "
                                            + igMeta.getRepeatMax()
                                            + ", '"
                                            + igMeta.getRepeatArray()
                                            + "', "
                                            +
                                            // above removed?
                                            igMeta.getRowStartNumber()
                                            + ","
                                            +versionIdStringWithParameter
                                            + ","
                                            // + "(SELECT ITEM_ID FROM ITEM
                                            // WHERE NAME='"
                                            // + itemName
                                            // + "' AND owner_id = " +
                                            // ub.getId() + " ORDER BY OID DESC
                                            // LIMIT 1),"
                                            + "(SELECT ITEM.ITEM_ID FROM ITEM,ITEM_FORM_METADATA,CRF_VERSION WHERE ITEM.NAME='"
                                            + itemName
                                            + "' "
                                            + "AND ITEM.ITEM_ID = ITEM_FORM_METADATA.ITEM_ID and ITEM_FORM_METADATA.CRF_VERSION_ID=CRF_VERSION.CRF_VERSION_ID "
                                            + "AND CRF_VERSION.CRF_ID= " + crfId + " ORDER BY ITEM.OC_OID DESC LIMIT 1)," + k + ", "
                                            + igMeta.isShowGroup() + ", " + igMeta.isRepeatingGroup() + ")";

                                }

                               // queries.add(sqlGroupLabel);
                                sqlParameters = new ArrayList<>();
                              //in versionIdStringWithParameter there is one parameter:crfId
                                sqlParameters.add(new SqlParameter(crfId+"",JDBCType.INTEGER));
                               
                                
                                qo = new QueryObject();
                                qo.setSql(sqlGroupLabel);
                                qo.setSqlParameters(sqlParameters);
                                
                                queries.add(qo);
                            } catch (NullPointerException e) {
                                // Auto-generated catch block, added tbh 102007
                                logger.error( "Error  message", e);
                                errors.add(resPageMsg.getString("Error_found_at_row") + " \"" + (k + 1) + "\"" + resPageMsg.getString("items_worksheet_with_dot")
                                    + resPageMsg.getString("GROUP_LABEL") + "\"" + groupLabel + "\" "
                                    + resPageMsg.getString("does_not_exist_in_group_spreadsheet"));
                                htmlErrors.put(j + "," + k + ",6", resPageMsg.getString("GROUP_DOES_NOT_EXIST"));
                            }
                        } else {

                            String sqlGroupLabel = "";
                            if (dbName.equals("oracle")) {
                                sqlGroupLabel =
                                    "INSERT INTO ITEM_GROUP_METADATA (item_group_id,HEADER,subheader, layout, repeat_number, repeat_max,"
                                        + " repeat_array,row_start_number, crf_version_id," + "item_id , ordinal, repeating_group) VALUES ("
                                        + "(SELECT MAX(ITEM_GROUP_ID) FROM ITEM_GROUP WHERE NAME='Ungrouped' AND crf_id = ? ),'" + "" + "', '" + ""
                                        + "', '" + "" + "', " + 1 + ", " + 1 + ", '', 1,?," + selectCorrectItemQueryOracle + "," + k
                                        + ", 0)";
                            } else {
                                sqlGroupLabel =
                                    "INSERT INTO ITEM_GROUP_METADATA (item_group_id,header,subheader, layout, repeat_number, repeat_max,"
                                        + " repeat_array,row_start_number, crf_version_id," + "item_id , ordinal, repeating_group) VALUES ("
                                        + "(SELECT ITEM_GROUP_ID FROM ITEM_GROUP WHERE NAME='Ungrouped' AND crf_id = ?" 
                                        + "  LIMIT 1),'" + "" + "', '" + "" + "', '" + "" + "', " + 1 + ", " + 1 + ", '', 1,"
                                        + versionIdString+"," + selectCorrectItemQueryPostgres + "," + k + ", false)";

                            }
                            // >>>>>>> .r10888

                            //queries.add(sqlGroupLabel);
                            sqlParameters = new ArrayList<>();
                            sqlParameters.add(new SqlParameter(crfId+"",JDBCType.INTEGER));
                           
                            
                            qo = new QueryObject();
                            qo.setSql(sqlGroupLabel);
                            qo.setSqlParameters(sqlParameters);
                            
                            queries.add(qo);

                        }
                    }

                    // **************************************
                    // above this is where we will add the first sql query for
                    // group names
                    // will have to be put in a seperate list
                    // and added at the end so that we insure
                    // that all new item_names and group_names have been added,
                    // tbh 5/14

                    // **************************************
                    // below is place where we will add the sheet name for
                    // Groups
                    // tbh, 5/14/2007
                    // DONE -- add sql insert queries below
                    // TODO review html error creation in table at end of file
                    // TODO find out where to add the form group beans
                    // TODO find out where to add the map beans

                    // we need to make sure groups sql are executed first,
                    // because item_group_id is
                    // used when we insert item group meta data with item
                    
                    //validate that items of one group are not spread over several sections
                    
                    SpreadSheetItemUtil.verifySectionGroupPlacementForItems( row_items, errors,  htmlErrors, j,resPageMsg,  itemGroups);
                    
                    SpreadSheetItemUtil.verifyUniqueItemPlacementInGroups( row_items, errors,  htmlErrors, j,resPageMsg, 
                    		crfName, ds);
                  
        			instantValidator.validate();
                    errors = (ArrayList<String>)instantValidator.getSheetErrors().addErrorsToSheet(errors);
                    htmlErrors = (HashMap<String,String>)instantValidator.getSheetErrors().putHtmlErrorsToSheet(htmlErrors);
                } else if (sheetName.equalsIgnoreCase("Groups")) {
                    logger.debug("read groups, ***comment added 5.14.07");
                    ArrayList groupNames = new ArrayList();
                    // create a group - item relationship with this table? hmm

                    // they are in order: group_label, group_layout,
                    // group_header,
                    // group_sub_header, group_repeat_number, group_repeat_max,
                    // group_repeat_array
                    // so: seven rows
                    // let's insert the default group first
                    ItemGroupBean defaultGroup = new ItemGroupBean();
                    defaultGroup.setName("Ungrouped");
                    defaultGroup.setCrfId(crfId);
                    defaultGroup.setStatus(Status.AVAILABLE);

                    // Create oid for Item Group
                    String defaultGroupOid = itemGroupDao.getValidOid(defaultGroup, crfName, defaultGroup.getName(), itemGroupOids);
                    itemGroupOids.add(defaultGroupOid);

                    String defaultSql = "";
                    if (dbName.equals("oracle")) {
                       /* defaultSql =
                            "INSERT INTO ITEM_GROUP ( name, crf_id, status_id, date_created ,owner_id,oc_oid) VALUES ('" + defaultGroup.getName()
                                + "', " + defaultGroup.getCrfId() + "," + defaultGroup.getStatus().getId() + ",sysdate," + ub.getId() + ",'"
                                + defaultGroupOid + "')";*/
                    	 defaultSql =
                                 "INSERT INTO ITEM_GROUP ( name, crf_id, status_id, date_created ,owner_id,oc_oid)" + 
                                		 " VALUES (?, ?, ?,sysdate, ?, ?)";
                    } else {
                       /* defaultSql =
                            "INSERT INTO ITEM_GROUP (  name, crf_id, status_id, date_created ,owner_id,oc_oid) VALUES ('" + defaultGroup.getName()
                                + "', " + defaultGroup.getCrfId() + "," + defaultGroup.getStatus().getId() + ",now()," + ub.getId() + ",'"
                                + defaultGroupOid + "')";*/
                    	defaultSql =
                                "INSERT INTO ITEM_GROUP ( name, crf_id, status_id, date_created ,owner_id,oc_oid)" + 
                               		 " VALUES (?, ?, ?,now(), ?, ?)";
                    }

                    if (!GroupCheck.containsKey("Ungrouped")) {
                        //queries.add(defaultSql);
                    	ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                        sqlParameters.add(new SqlParameter(defaultGroup.getName()));
                        sqlParameters.add(new SqlParameter(defaultGroup.getCrfId().toString(),JDBCType.INTEGER));
                        sqlParameters.add(new SqlParameter(defaultGroup.getStatus().getId()+"",JDBCType.INTEGER));                         
                        sqlParameters.add(new SqlParameter(ub.getId()+"",JDBCType.INTEGER));
                        sqlParameters.add(new SqlParameter(defaultGroupOid+""));
                        
                        QueryObject qo = new QueryObject();
                        qo.setSql(defaultSql);
                        qo.setSqlParameters(sqlParameters);
                        
                        queries.add(qo);
                    }
                    for (int gk = 1; gk < numRows; gk++) {
                       
                        if (sheet.getRow(gk) == null) {
                            blankRowCount++;
                            if (blankRowCount == 5) { break;
                            }
                            continue;
                        }
                        HSSFCell cell = sheet.getRow(gk).getCell((short) 0);
                        String groupLabel = getValue(cell);
                        groupLabel = groupLabel.replaceAll("<[^>]*>", "");

                        if (StringUtil.isBlank(groupLabel)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("GROUP_LABEL_column")
                                + resPageMsg.getString("was_blank_at_row") +" "+ gk + ", " + resPageMsg.getString("Groups_worksheet") + ".");
                            htmlErrors.put(j + "," + gk + ",0", resPageMsg.getString("required_field"));
                        }

                        if (groupLabel != null && groupLabel.length() > 255) {
                            errors.add(resPageMsg.getString("group_label_length_error"));
                        }
                        // must these be unique? probably so, tbh
                        if (groupNames.contains(groupLabel)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("GROUP_LABEL_column")
                                + resPageMsg.getString("was_a_duplicate_of") + " " + groupLabel + resPageMsg.getString("at_row") + gk + ", "
                                + resPageMsg.getString("Groups_worksheet") + ".");
                            htmlErrors.put(j + "," + gk + ",0", resPageMsg.getString("DUPLICATE_FIELD"));
                        } else {
                            groupNames.add(groupLabel);
                        }
                        // removed reference to 'groupLayout' here, tbh 102007

                        boolean isRepeatingGroup = true;
                        boolean newVersionCrf = false;
                        int cellNo = 0;
                        if(!(versionNo.equalsIgnoreCase("Version: 2.2")
                               || versionNo.equalsIgnoreCase("Version: 2.5")
                               || versionNo.equalsIgnoreCase("Version: 3.0"))){
                            cellNo = 1;
                            cell = sheet.getRow(gk).getCell((short) cellNo);
                            try {
                                isRepeatingGroup = getValue(cell).equalsIgnoreCase("grid");
                                newVersionCrf = true;
                            } catch (Exception eee) {
                                errors.add(resPageMsg.getString("repeating_group_error"));
                            }
                        }

                        cell = sheet.getRow(gk).getCell((short) ++cellNo);
                        String groupHeader = getValue(cell);
                        // replace any apostrophes in groupHeader: issue 3277
                        groupHeader = org.akaza.openclinica.core.form.StringUtil.escapeSingleQuote(groupHeader);
                        if (groupHeader != null && groupHeader.length() > 255) {
                            errors.add(resPageMsg.getString("group_header_length_error"));
                        }
                        if(isRepeatingGroup) {
                            sheetContainer.getRepeatingGroupLabels().add(groupLabel);
                        }

                        cell = sheet.getRow(gk).getCell((short) ++cellNo);
                        String groupRepeatNumber = getValue(cell);
                        // to be switched to int, tbh
                        // adding clause to convert to int, tbh, 06/07
                        if (newVersionCrf && !isRepeatingGroup && !StringUtil.isBlank(groupRepeatNumber)){
                            errors.add(resPageMsg.getString("repeat_number_none_repeating"));
                        } else if (!isRepeatingGroup && StringUtil.isBlank(groupRepeatNumber)) {
                                groupRepeatNumber = "1";
                        } else {
                            if (StringUtil.isBlank(groupRepeatNumber)) {
                                groupRepeatNumber = "1";
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                                double dr = cell.getNumericCellValue();
                                if ((dr - (int) dr) * 1000 == 0) {
                                    groupRepeatNumber = (int) dr + "";
                                }
                              
                            } else {
                                logger.debug("found a non-numeric code in a numeric field: groupRepeatNumber");
                              
                            }
                        }

                        cell = sheet.getRow(gk).getCell((short) ++cellNo);
                        String groupRepeatMax = getValue(cell);
                        // to be switched to int, tbh
                        // adding clause to convert to int, tbh 06/07
                        if (newVersionCrf && !isRepeatingGroup && !StringUtil.isBlank(groupRepeatMax)){
                            errors.add(resPageMsg.getString("repeat_max_none_repeating"));
                        } else if (!isRepeatingGroup && StringUtil.isBlank(groupRepeatMax)) {
                            groupRepeatMax = "1";
                        } else {
                            if (StringUtil.isBlank(groupRepeatMax)) {
                                groupRepeatMax = "40";// problem, tbh
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                                double dr = cell.getNumericCellValue();
                                if ((dr - (int) dr) * 1000 == 0) {
                                    groupRepeatMax = (int) dr + "";
                                    // check for zero value
                                    try {
                                        int repeatMaxInt = Integer.parseInt(groupRepeatMax);
                                        if (repeatMaxInt < 1) {
                                            groupRepeatMax = "40";
                                        }
                                    } catch (NumberFormatException nfe) {
                                        groupRepeatMax = "40";
                                    }
                                }
                            } else {
                                logger.debug("found a non-numeric code in a numeric field: groupRepeatMax");
                            }
                        }
                        // >> tbh 02/2010 adding show_hide for Dynamics
                        cell = sheet.getRow(gk).getCell((short) ++cellNo);
                        String showGroup = getValue(cell);
                        boolean isShowGroup = true;
                        if (!StringUtil.isBlank(showGroup)) {

                            try {
                                isShowGroup = "0".equals(showGroup) ? false : true;
                                isShowGroup = "Hide".equalsIgnoreCase(showGroup) ? false : true;
                            } catch (Exception eee) {
                                logger.debug("caught an exception with the boolean value for groups");
                            }
                        }
                        //                        if (!"1".equals(showGroup) && !"0".equals(showGroup)) {
                        //                            // throw an error here
                        //                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SHOW_GROUP_column") + " "
                        //                                    + resPageMsg.getString("was_invalid_at_row") + " " + gk + ", " + resPageMsg.getString("Groups_worksheet") + ". "
                        //                                    + resPageMsg.getString("SHOW_GROUP_column") + resPageMsg.getString("can_only_be_either_0_or_1"));
                        //                            htmlErrors.put(j + "," + gk + "," + 4, resPageMsg.getString("INVALID_VALUE"));
                        //                        }

                        // cell = sheet.getRow(gk).getCell((short) 6);
                        // String groupRepeatArray = getValue(cell);
                        // below added 06/14/2007, tbh
                        /*
                         * BWP>>commented out after removal of borders column
                         * cell = sheet.getRow(gk).getCell((short) 7); String
                         * groupBorders = getValue(cell); Integer borders = 0;
                         * try { borders = Integer.valueOf(groupBorders); if
                         * (borders.intValue() <0) { errors.add("The BORDERS
                         * column must be a positive integer. " + groupBorders + "
                         * at row " + gk + ", Groups worksheet.");
                         * htmlErrors.put(j + "," + gk + ",7", "INVALID FIELD"); } }
                         * catch (NumberFormatException ne) { errors.add("The
                         * BORDERS column must be a positive integer. " +
                         * groupBorders + " at row " + gk + ", Groups
                         * worksheet."); htmlErrors.put(j + "," + gk + ",7",
                         * "INVALID FIELD"); } >>
                         */
                        // above added 06/14/2007, tbh
                        ItemGroupBean fgb = new ItemGroupBean();
                        fgb.setName(groupLabel);
                        fgb.setCrfId(crfId);
                        fgb.setStatus(Status.AVAILABLE);
                        

                        ItemGroupMetadataBean igMeta = new ItemGroupMetadataBean();
                        igMeta.setHeader(groupHeader);
                        igMeta.setRepeatingGroup(isRepeatingGroup);
                        igMeta.setBorders(0);//htaycher: no borders anymnore //13817
                        // igMeta.setLayout(groupLayout);
                        // igMeta.setRepeatArray(groupRepeatArray);
                        igMeta.setShowGroup(isShowGroup);
                        try {
                            igMeta.setRepeatMax(new Integer(Integer.parseInt(groupRepeatMax)));
                            //mantiss 13917
                            
                            if (igMeta.getRepeatMax() < 1){
                            	 errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("GROUP_REPEAT_MAX_column") + " "
                                         + resPageMsg.getString("must_be_a_positive_integer") + ". " + groupRepeatMax + " " + resPageMsg.getString("at_row") + " "+gk
                                         + ", " + resPageMsg.getString("Groups_worksheet") + ". ");
                                     htmlErrors.put(j + "," + gk + ",3", resPageMsg.getString("INVALID_FIELD"));
                            }
                        } catch (NumberFormatException n2) {
                            logger.error("Error  message", n2);
                            if ("".equals(groupRepeatMax)) {
                                igMeta.setRepeatMax(40);
                            } else {
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("GROUP_REPEAT_MAX_column") + " "
                                    + resPageMsg.getString("must_be_a_positive_integer") + ". " + groupRepeatMax + " " + resPageMsg.getString("at_row") + " "+gk
                                    + ", " + resPageMsg.getString("Groups_worksheet") + ". ");
                                htmlErrors.put(j + "," + gk + ",3", resPageMsg.getString("INVALID_FIELD"));
                            }
                        }
                        try {
                            igMeta.setRepeatNum(new Integer(Integer.parseInt(groupRepeatNumber)));
                            if(igMeta.getRepeatNum() < 1){ //mantiss 13917
	                            errors.add(resPageMsg.getString("the") + " "+resPageMsg.getString("GROUP_REPEAT_NUM_column")+" "
	                                    + resPageMsg.getString("must_be_a_positive_integer_or_blank") + ". " + groupRepeatNumber + " "
	                                    + resPageMsg.getString("at_row") + " " + gk + ", " + resPageMsg.getString("Groups_worksheet") + ". ");
	                                htmlErrors.put(j + "," + gk + ",2", resPageMsg.getString("INVALID_FIELD"));
                            }
                        } catch (NumberFormatException n3) {
                        	logger.error(n3.getMessage());
                            errors.add(resPageMsg.getString("the") + " "+resPageMsg.getString("GROUP_REPEAT_NUM_column")+" "
                                + resPageMsg.getString("must_be_a_positive_integer_or_blank") + ". " + groupRepeatNumber + " "
                                + resPageMsg.getString("at_row") + " " + gk + ", " + resPageMsg.getString("Groups_worksheet") + ". ");
                            htmlErrors.put(j + "," + gk + ",2", resPageMsg.getString("INVALID_FIELD"));
                        }

                        // igMeta.setSubheader(groupSubheader);
                        fgb.setMeta(igMeta);

                        // now, we place the form group bean where we can
                        // generate the sql
                        // and find it again, tbh 5/14/2007
                        // Create oid for Item Group
                        String groupOid = itemGroupDao.getValidOid(fgb, crfName, fgb.getName(), itemGroupOids);
                        itemGroupOids.add(groupOid);

                        // changed to add metadata into item_group_metadata
                        // table-jxu

                        String gsql = "";
                        if (dbName.equals("oracle")) {
                           /* gsql =
                                "INSERT INTO ITEM_GROUP ( " + "name, crf_id, status_id, date_created ,owner_id,oc_oid)" + "VALUES ('" + fgb.getName() + "', "
                                    + fgb.getCrfId() + "," + fgb.getStatus().getId() + "," + "sysdate," + ub.getId() + ",'" + groupOid + "')";*/
                        	 gsql =
                                     "INSERT INTO ITEM_GROUP ( name, crf_id, status_id, date_created ,owner_id,oc_oid)" + 
                                    		 " VALUES (?, ?, ?,sysdate, ?, ?)";
                        } else {
                           /* gsql =
                                "INSERT INTO ITEM_GROUP ( " + "name, crf_id, status_id, date_created ,owner_id,oc_oid)" + "VALUES ('" + fgb.getName() + "', "
                                    + fgb.getCrfId() + "," + fgb.getStatus().getId() + "," + "now()," + ub.getId() + ",'" + groupOid + "')";*/
                        	gsql =
                                    "INSERT INTO ITEM_GROUP ( name, crf_id, status_id, date_created ,owner_id,oc_oid)" + 
                                   		  " VALUES (?, ?, ?,now(), ?, ?)";
                        }

                        itemGroups.put(fgb.getName(), fgb);

                        if (!GroupCheck.containsKey(fgb.getName())) {
                            // item group not in the DB, then insert
                            // otherwise, will use the existing group because
                            // group name is unique
                            // and shared within CRF
                            //queries.add(gsql);
                        	 ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                             sqlParameters.add(new SqlParameter(fgb.getName()));
                             sqlParameters.add(new SqlParameter(fgb.getCrfId().toString(),JDBCType.INTEGER));
                             sqlParameters.add(new SqlParameter(fgb.getStatus().getId()+"",JDBCType.INTEGER));                         
                             sqlParameters.add(new SqlParameter(ub.getId()+"",JDBCType.INTEGER));
                             sqlParameters.add(new SqlParameter(groupOid+""));
                             
                             QueryObject qo = new QueryObject();
                             qo.setSql(gsql);
                             qo.setSqlParameters(sqlParameters);
                             
                             queries.add(qo);

                        }
                        // if (!StringUtil.isBlank(groupLabel)) {
                        // String itemName =
                        // (String)itemsToGrouplabels.get(groupLabel);
                        // logger.debug("found "+itemName+" when we passed group
                        // label "+groupLabel);
                        // ItemGroupBean itemGroup = new ItemGroupBean();
                        // //logger.debug("found "+groupLabel);
                        // itemGroup= (ItemGroupBean)itemGroups.get(groupLabel);
                        // logger.debug("*** Found "+
                        // groupLabel+
                        // " and matched with "+
                        // itemGroup.getName());
                        // igMeta = itemGroup.getMeta();
                        // //above throws Nullpointer, need to change so that it
                        // does not, tbh 07-08-07
                        // //moved down here from line 590, tbh
                        //
                        // String sqlGroupLabel = "INSERT INTO
                        // ITEM_GROUP_METADATA ("+
                        // "item_group_id,header," +
                        // "subheader, layout, repeat_number, repeat_max," +
                        // " repeat_array,row_start_number, crf_version_id," +
                        // "item_id , ordinal, borders) VALUES (" +
                        // "(SELECT ITEM_GROUP_ID FROM ITEM_GROUP WHERE NAME='"
                        // + itemGroup.getName()
                        // + "' AND crf_id = " + crfId + " ORDER BY OID DESC
                        // LIMIT 1),'"+
                        // igMeta.getHeader()+"', '" +
                        // igMeta.getSubheader()+ "', '" +
                        // igMeta.getLayout()+ "', " +
                        // igMeta.getRepeatNum()+", " +
                        // igMeta.getRepeatMax()+", '" +
                        // igMeta.getRepeatArray()+"', " +
                        // igMeta.getRowStartNumber()+ "," +
                        // versionIdString + "," +
                        // "(SELECT ITEM_ID FROM ITEM WHERE NAME='" + itemName +
                        // "' AND owner_id = " + ub.getId() + " ORDER BY OID
                        // DESC LIMIT 1)," +
                        // igMeta.getOrdinal() + ",'" +
                        // igMeta.getBorders()+
                        // "')";
                        // queries.add(sqlGroupLabel);
                        //
                        // }
                    }
                } else if (sheetName.equalsIgnoreCase("Sections")) {
                    logger.debug("read sections");

                    // multiple rows, six cells, last one is number
                    // changed 06/14/2007: seven cells, last on is number, the
                    // BORDER, tbh
                    for (int k = 1; k < numRows; k++) {
                        if (blankRowCount == 5) {
                            // logger.debug("hit end of the row ");
                            // kludgey way to zero out the rows that can get
                            // created in the
                            // editing process; is there a better way? tbh
                            // 06/2007
                            break;
                        }
                        if (sheet.getRow(k) == null) {
                            blankRowCount++;
                            continue;
                        }
                        HSSFCell cell = sheet.getRow(k).getCell((short) 0);
                        String secLabel = getValue(cell);
                        secLabel = secLabel.replaceAll("<[^>]*>", "");

                        if (StringUtil.isBlank(secLabel)) {
                            // errors.add("The SECTION_LABEL column was blank at
                            // row " + k + ", Sections worksheet.");
                            // htmlErrors.put(j + "," + k + ",0", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_LABEL_column") + " "
                                + resPageMsg.getString("was_blank_at_row")+" " + k + " " + ", " + resPageMsg.getString("sections_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("required_field"));
                        }
                        if (secLabel != null && secLabel.length() > 2000) {
                            errors.add(resPageMsg.getString("section_label_length_error"));
                        }

                        if(secLabel.contains("'")){
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_LABEL_column")
                                    + " value " + secLabel + " " + resPageMsg.getString("has_invalid_character")+ "\" ' \""+". "
                                    + resPageMsg.getString("remove_invalid_character"));
                        }

                        if (secNames.contains(secLabel)) {
                            // errors.add("The SECTION_LABEL column was a
                            // duplicate of " + secLabel + " at row " + k
                            // + ", sections worksheet.");
                            // htmlErrors.put(j + "," + k + ",0", "DUPLICATE
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_LABEL_column")
                                + resPageMsg.getString("was_a_duplicate_of") + secLabel + " " + resPageMsg.getString("at_row") + " " + k + ", "
                                + resPageMsg.getString("sections_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("DUPLICATE_FIELD"));
                        }
                        // logger.debug("section name:" + secLabel + "row num:"
                        // +k);
                        secNames.add(secLabel);
                        cell = sheet.getRow(k).getCell((short) 1);
                        String title = getValue(cell);
                        title = title.replaceAll("<[^>]*>", "");
                        if (StringUtil.isBlank(title)) {
                            // errors.add("The SECTION_TITLE column was blank at
                            // row " + k + ", Sections worksheet.");
                            // htmlErrors.put(j + "," + k + ",1", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_TITLE_column") + " "
                                + resPageMsg.getString("was_blank_at_row")  +" "+ k + ", " + resPageMsg.getString("sections_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",1", resPageMsg.getString("required_field"));
                        }
                        if (title != null && title.length() > 2000) {
                            errors.add(resPageMsg.getString("section_title_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 2);
                        String subtitle = getValue(cell);
                        if (subtitle != null && subtitle.length() > 2000) {
                            errors.add(resPageMsg.getString("section_subtitle_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 3);
                        String instructions = getValue(cell);
                        if (instructions != null && instructions.length() > 2000) {
                            errors.add(resPageMsg.getString("section_instruction_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 4);
                        String pageNumber = getValue(cell);
                        if (pageNumber != null && pageNumber.length() > 5) {
                            errors.add(resPageMsg.getString("section_page_number_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 5);
                        String parentSection = getValue(cell);
                        parentSection = parentSection.replaceAll("<[^>]*>", "");
                        if (!StringUtil.isBlank(parentSection)) {
                            try {
                                parentId = Integer.parseInt(parentSection);
                            } catch (NumberFormatException ne) {
                                parentId = 0;
                            }
                        }
                        // below added 06/2007, tbh
                        cell = sheet.getRow(k).getCell((short) 6);
                        String strBorder = getValue(cell);
                        strBorder = strBorder.replaceAll("<[^>]*>", "");

                        Integer intBorder = new Integer(0);
                        try {
                            intBorder = new Integer(strBorder);
                        } catch (NumberFormatException npe) {
                            // let it pass here, tbh 06/18/2007
                        }
                        // change to sql 06/2007; change to section table in
                        // svn? tbh

                        String sql = "";
                        // BWP added borders column 4/24/2008
                        if (dbName.equals("oracle")) {
                           /* sql =
                                "INSERT INTO SECTION (CRF_VERSION_ID," + "STATUS_ID,LABEL, TITLE, INSTRUCTIONS, SUBTITLE, PAGE_NUMBER_LABEL,"
                                    + "ORDINAL, PARENT_ID, OWNER_ID, DATE_CREATED, BORDERS) " + "VALUES (" + versionIdString + ",1,'" + secLabel + "','"
                                    + stripQuotes(title) + "', '" + stripQuotes(instructions) + "', '" + stripQuotes(subtitle) + "','" + pageNumber + "'," + k
                                    + "," + parentId + "," + ub.getId() + ",sysdate," + intBorder + ")";*/
                        	 sql =
                                     "INSERT INTO SECTION (CRF_VERSION_ID," + "STATUS_ID,LABEL, TITLE, INSTRUCTIONS, SUBTITLE, PAGE_NUMBER_LABEL,"
                                             + "ORDINAL, PARENT_ID, OWNER_ID, DATE_CREATED,BORDERS) " + "VALUES (" +versionIdString+ ",1,?,?,?,?,'" + pageNumber + "'," + k
                                             + "," + parentId + "," + ub.getId() + ",NOW()," + intBorder + ")";
                        } else {
                            /*sql =
                                "INSERT INTO SECTION (CRF_VERSION_ID," + "STATUS_ID,LABEL, TITLE, INSTRUCTIONS, SUBTITLE, PAGE_NUMBER_LABEL,"
                                    + "ORDINAL, PARENT_ID, OWNER_ID, DATE_CREATED,BORDERS) " + "VALUES (" + versionIdString + ",1,'" + secLabel + "','"
                                    + stripQuotes(title) + "', '" + stripQuotes(instructions) + "', '" + stripQuotes(subtitle) + "','" + pageNumber + "'," + k
                                    + "," + parentId + "," + ub.getId() + ",NOW()," + intBorder + ")";*/
                        	sql =
                                    "INSERT INTO SECTION (CRF_VERSION_ID," + "STATUS_ID,LABEL, TITLE, INSTRUCTIONS, SUBTITLE, PAGE_NUMBER_LABEL,"
                                        + "ORDINAL, PARENT_ID, OWNER_ID, DATE_CREATED,BORDERS) " + "VALUES (" +versionIdString+ ",1,?,?,?,?,'" + pageNumber + "'," + k
                                        + "," + parentId + "," + ub.getId() + ",NOW()," + intBorder + ")";
                        }

                        //queries.add(sql);
                        ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                        sqlParameters.add(new SqlParameter(secLabel));
                        sqlParameters.add(new SqlParameter(title));
                        sqlParameters.add(new SqlParameter(instructions));
                        sqlParameters.add(new SqlParameter(subtitle));
                        //sqlParameters.add(new SqlParameter(pageNumber));
                        
                        QueryObject qo = new QueryObject();
                        qo.setSql(sql);
                        qo.setSqlParameters(sqlParameters);
                        
                        queries.add(qo);
                    }// end for loop
                } else if (sheetName.equalsIgnoreCase("CRF")) {
                    logger.debug("read crf");
                    // one row, four cells, all strings
                    if (sheet == null || sheet.getRow(1) == null || sheet.getRow(1).getCell((short) 0) == null) {
                        throw new CRFReadingException("Blank row found in sheet CRF.");
                    }
                    HSSFCell cell = sheet.getRow(1).getCell((short) 0);
                    crfName = getValue(cell);
                    crfName = crfName.replaceAll("<[^>]*>", "");

                    if (StringUtil.isBlank(crfName)) {
                        // errors.add("The CRF_NAME column was blank in the CRF
                        // worksheet.");
                        // htmlErrors.put(j + ",1,0", "REQUIRED FIELD");
                        throw new CRFReadingException("The CRF_NAME column was blank in the CRF worksheet.");
                    }

                    if(crfId > 0) {
                        CRFBean checkName = (CRFBean) cdao.findByPK(crfId);
                        if (!checkName.getName().equals(crfName)) {
                            throw new CRFReadingException(resPageMsg.getString("the") + " " +
                                    resPageMsg.getString("CRF_NAME_column") + " '" + crfName + "' " +
                                    resPageMsg.getString("did_not_match_crf_name") + " '" + checkName.getName() + "'.");
                        }
                    }

                    if (crfName.length() > 255) {
                        errors.add(resPageMsg.getString("crf_name_length_error"));
                    }

                    CRFBean existingCRFWithSameName = (CRFBean) cdao.findByName(crfName);
                    if (this.getCrfId() == 0) {
                        if (existingCRFWithSameName.getName() != null && existingCRFWithSameName.getName().equals(crfName)) {
                            errors.add(resPageMsg.getString("crf_name_already_used"));
                        }
                    }

                    // try {
                    // CRFBean checkName = (CRFBean) cdao.findByPK(crfId);
                    // if (!checkName.getName().equals(crfName)) {
                    // logger.debug("crf name is mismatch");
                    // //errors.add("The CRF_NAME column did not match the
                    // intended CRF version "
                    // // + "you want to upload. Make sure this reads '" +
                    // checkName.getName()
                    // // + "' before you continue.");
                    // //htmlErrors.put(j + ",1,0", "DID NOT MATCH CRF");
                    // errors.add(resPageMsg.getString("the") + " " +
                    // resPageMsg.getString("CRF_NAME_column") +
                    // resPageMsg.getString("did_not_match_crf_version") + " '"
                    // + checkName.getName()
                    // + "' " + resPageMsg.getString("before_you_continue"));
                    // htmlErrors.put(j + ",1,0",
                    // resPageMsg.getString("DID_NOT_MATCH_CRF"));
                    // }
                    // } catch (Exception pe) {
                    // logger.warn("Exception happened when check CRF name" +
                    // pe.getMessage());
                    // }

                    cell = sheet.getRow(1).getCell((short) 1);
                    String version = getValue(cell);
                    version = version.replaceAll("<[^>]*>", "");
                    ncrf.setVersionName(version);
                    if (version != null && version.length() > 255) {
                        errors.add(resPageMsg.getString("version_length_error"));
                    }

                    // YW, 08-22-2007, since versionName is now obtained from
                    // spreadsheet,
                    // blank check has been moved to
                    // CreateCRFVersionServlet.java
                    // and mismatch check is not necessary
                    // if (StringUtil.isBlank(version)) {
                    // errors.add("The VERSION column was blank in the CRF
                    // worksheet.");
                    // htmlErrors.put(j + ",1,1", "REQUIRED FIELD");
                    // }else if (!version.equals(versionName)) {
                    // errors.add("The VERSION column did not match the intended
                    // version name "
                    // + "you want to upload. Make sure this reads '" +
                    // versionName
                    // + "' before you continue.");
                    // htmlErrors.put(j + ",1,1", "DID NOT MATCH VERSION");
                    // }

                    cell = sheet.getRow(1).getCell((short) 2);
                    String versionDesc = getValue(cell);
                    versionDesc = versionDesc.replaceAll("<[^>]*>", "");
                    if (versionDesc != null && versionDesc.length() > 4000) {
                        errors.add(resPageMsg.getString("version_description_length_error"));
                    }

                    cell = sheet.getRow(1).getCell((short) 3);
                    String revisionNotes = getValue(cell);
                    revisionNotes = revisionNotes.replaceAll("<[^>]*>", "");
                    if (revisionNotes != null && revisionNotes.length() > 255) {
                        errors.add(resPageMsg.getString("revision_notes_length_error"));
                    }
                    if (StringUtil.isBlank(revisionNotes)) {
                        // errors.add("The REVISION_NOTES column was blank in
                        // the CRF worksheet.");
                        // htmlErrors.put(j + ",1,3", "REQUIRED FIELD");
                        errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("REVISION_NOTES_column") + " "
                            + resPageMsg.getString("was_blank_in_the_CRF_worksheet"));
                        htmlErrors.put(j + ",1,3", resPageMsg.getString("required_field"));
                    }

                    // Generating query string for the new CRF
                    Connection con = null;
                    String crfOid = null;
                    if (crfId == 0) {
                        crfOid = cdao.getValidOid(new CRFBean(), crfName);
                        int nextCRFId;
                        try {
                            con = ds.getConnection();
                            /*
                             * We are selecting the crf id which will be used to
                             * save the new CRF. Selecting the crf id in advance
                             * will not cause any problem in a multi threaded
                             * environment because the nextVal() method always
                             * returns unique values. So there is no chance of
                             * processing two CRF simultaneously with same crf
                             * id.
                             */
                            ResultSet nextIdRs;
                            if (dbName.equals("oracle")) {
                                nextIdRs = con.createStatement().executeQuery("select crf_id_seq.nextval from dual");
                            } else {
                                nextIdRs = con.createStatement().executeQuery("select nextval('crf_crf_id_seq')");
                            }

                            nextIdRs.next();
                            nextCRFId = nextIdRs.getInt(1);
                            crfId = nextCRFId;
                            ncrf.setCrfId(crfId);
                            String createCRFSql;
                            if (dbName.equals("oracle")) {
                               /* createCRFSql =
                                    "INSERT INTO CRF (CRF_ID, STATUS_ID, NAME, DESCRIPTION, OWNER_ID, DATE_CREATED, OC_OID, SOURCE_STUDY_ID) VALUES (" + crfId
                                        + ", 1,'" + stripQuotes(crfName) + "','" + stripQuotes(versionDesc) + "'," + ub.getId() + ",sysdate" + ",'" + crfOid
                                        + "'," + studyId + ")";*/
                            	 createCRFSql =
                                         "INSERT INTO CRF (CRF_ID, STATUS_ID, NAME, DESCRIPTION, OWNER_ID, DATE_CREATED, OC_OID, SOURCE_STUDY_ID) VALUES (" + crfId
                                             + ", 1, ? ,?," + ub.getId() + ",sysdate" + ",'" + crfOid
                                             + "'," + studyId + ")";
                            } else {
                               /* createCRFSql =
                                    "INSERT INTO CRF (CRF_ID, STATUS_ID, NAME, DESCRIPTION, OWNER_ID, DATE_CREATED, OC_OID, SOURCE_STUDY_ID) VALUES (" + crfId
                                        + ", 1,'" + stripQuotes(crfName) + "','" + stripQuotes(versionDesc) + "'," + ub.getId() + ",NOW()" + ",'" + crfOid
                                        + "'," + studyId + ")";*/
                            	 createCRFSql =
                                         "INSERT INTO CRF (CRF_ID, STATUS_ID, NAME, DESCRIPTION, OWNER_ID, DATE_CREATED, OC_OID, SOURCE_STUDY_ID) VALUES (" + crfId
                                             + ", 1, ? ,?," + ub.getId() + ",NOW()" + ",'" + crfOid
                                             + "'," + studyId + ")";
                            }
                            //queries.add(createCRFSql);
                            ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                            sqlParameters.add(new SqlParameter(crfName));
                            sqlParameters.add(new SqlParameter(versionDesc));

                            QueryObject qo = new QueryObject();
                            qo.setSql(createCRFSql);
                            qo.setSqlParameters(sqlParameters);
                            
                            queries.add(qo);
                        } catch (SQLException e) {
                            logger.warn("Exception encountered with query select nextval('crf_crf_id_seq'), Message-" + e.getMessage());
                        } finally {
                            if (con != null) {
                                try {
                                    con.close();
                                } catch (SQLException e) {
                                    logger.warn("Connection can't be closed");
                                }
                            }
                        }
                    }

                    // check for instrument existence here??? tbh 7/28
                    // engaging in new validation, tbh, 6-4-04
                    // modify nib.getinstversions to look for version name and
                    // description
                    // need to stop uploads of same name-description pairs

                    HashMap checkCRFVersions = ncrf.getCrfVersions();

                    // this now returns a hash map of key:version_name
                    // ->value:version_description
                    boolean overwrite = false;

                    if (checkCRFVersions.containsKey(version)) {
                        logger.debug("found a matching version name..." + version);
                        /*
                         * errors.add("The VERSION column is not unique. This
                         * can cause confusion in " + "selecting the correct
                         * CRF. Please make sure you change the " + "version
                         * name so that it can be uniquely identified by users
                         * in the system. " + "Otherwise, the previous same
                         * version will be deleted from database.");
                         * htmlErrors.put(j + ",1,2", "NOT UNIQUE");
                         */
                        errors.add(resPageMsg.getString("version_not_unique_cause_confusion"));
                        htmlErrors.put(j + ",1,2", resPageMsg.getString("NOT_UNIQUE"));

                    }
                    // Create oid for Crf Version
                    String oid;
                    if (crfOid != null) {
                        oid = cvdao.getValidOid(new CRFVersionBean(), crfOid, version);
                    } else {
                        CRFBean crfBean = (CRFBean) cdao.findByName(crfName);
                        oid = cvdao.getValidOid(new CRFVersionBean(), crfBean.getOid(), version);
                    }
                    String sql = "";

                    if (dbName.equals("oracle")) {
                        if (crfId == 0) {
                            /*sql =
                                "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                                    + "VALUES ('" + stripQuotes(version) + "','" + stripQuotes(versionDesc) + "'," + "(SELECT CRF_ID FROM CRF C WHERE C.NAME='"
                                    + crfName + "'),1,sysdate," + ub.getId() + ",'" + stripQuotes(revisionNotes) + "','" + oid + "')";*/
                        	sql =
                                    "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                                        + "VALUES (?,?," + "(SELECT CRF_ID FROM CRF C WHERE C.NAME='"
                                        + crfName + "'),1,sysdate," + ub.getId() + ",?,'" + oid + "')";

                        } else {
                           /* sql =
                                "INSERT INTO CRF_VERSION (NAME,DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) " + "VALUES ('"
                                    + version + "','" + stripQuotes(versionDesc) + "'," + crfId + ",1,sysdate," + ub.getId() + ",'"
                                    + stripQuotes(revisionNotes) + "','" + oid + "')";*/
                        	 sql =
                                     "INSERT INTO CRF_VERSION (NAME,DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                        	 + "VALUES (?,?," + crfId + ",1,sysdate," + ub.getId() + ",?,'" + oid + "')";

                        }
                    } else {
                        if (crfId == 0) {
                            /*sql =
                                "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                                    + "VALUES ('" + stripQuotes(version) + "','" + stripQuotes(versionDesc) + "'," + "(SELECT CRF_ID FROM CRF WHERE NAME='"
                                    + crfName + "'),1,NOW()," + ub.getId() + ",'" + stripQuotes(revisionNotes) + "','" + oid + "')";*/
                        	sql =
                                    "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                                        + "VALUES (?,?," + "(SELECT CRF_ID FROM CRF WHERE NAME='"
                                        + crfName + "'),1,NOW()," + ub.getId() + ",?,'" + oid + "')";
                        } else {
                           /* sql =
                                "INSERT INTO CRF_VERSION (NAME,DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) " + "VALUES ('"
                                    + version + "','" + stripQuotes(versionDesc) + "'," + crfId + ",1,NOW()," + ub.getId() + ",'" + stripQuotes(revisionNotes)
                                    + "','" + oid + "')";*/
                        	 sql =
                                     "INSERT INTO CRF_VERSION (NAME,DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                        	     + "VALUES (?,?," + crfId + ",1,NOW()," + ub.getId() + ",?,'" + oid + "')";
                        }
                    }

                    //queries.add(sql);
                    ArrayList<SqlParameter> sqlParameters = new ArrayList<>();
                    sqlParameters.add(new SqlParameter(version));
                    sqlParameters.add(new SqlParameter(versionDesc));
                    sqlParameters.add(new SqlParameter(revisionNotes));
                    
                    QueryObject qo = new QueryObject();
                    qo.setSql(sql);
                    qo.setSqlParameters(sqlParameters);
                    
                    queries.add(qo);
					
                    pVersion = version;
                    pVerDesc = versionDesc;
                }

                versionIdString = "(SELECT CRF_VERSION_ID FROM CRF_VERSION WHERE NAME ='" + pVersion + "' AND CRF_ID=" + crfId + ")";
                versionIdStringWithParameter = "(SELECT CRF_VERSION_ID FROM CRF_VERSION WHERE NAME ='" + pVersion + "' AND CRF_ID=?)";

                // move html creation to here, include error creation as well,
                // tbh 7/28
                buf.append(sheetName + "<br>");
                buf
                        .append("<div class=\"box_T\"><div class=\"box_L\"><div class=\"box_R\"><div class=\"box_B\"><div class=\"box_TL\"><div class=\"box_TR\"><div class=\"box_BL\"><div class=\"box_BR\">");

                buf.append("<div class=\"textbox_center\">");
                buf.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"");
                buf.append("caption=\"" + wb.getSheetName(j) + "\"" + ">");

                for (int i = 0; i < numRows; i++) {
                    buf.append("<tr>");

                    if (sheet.getRow(i) == null) {
                        continue;
                    }

                    int numCells = sheet.getRow(i).getLastCellNum();

                    for (int y = 0; y < numCells; y++) {
                        HSSFCell cell = sheet.getRow(i).getCell((short) y);
                        int cellType = 0;
                        String error = "&nbsp;";
                        String errorKey = j + "," + i + "," + y;
                        if (htmlErrors.containsKey(errorKey)) {
                            error = "<span class=\"alert\">" + htmlErrors.get(errorKey) + "</span>";
                        }
                        if (cell == null) {
                            cellType = HSSFCell.CELL_TYPE_BLANK;
                        } else {
                            cellType = cell.getCellType();
                        }
                        switch (cellType) {
                        case HSSFCell.CELL_TYPE_BLANK:
                            buf.append("<td class=\"table_cell\">" + error + "</td>");
                            break;
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            buf.append("<td class=\"table_cell\">" + cell.getNumericCellValue() + " " + error + "</td>");
                            break;
                        case HSSFCell.CELL_TYPE_STRING:
                            buf.append("<td class=\"table_cell\">" + cell.getStringCellValue() + " " + error + "</td>");
                            break;
                        default:
                            buf.append("<td class=\"table_cell\">" + error + "</td>");
                        }
                    }
                    buf.append("</tr>");
                }
                buf.append("</table>");
                buf.append("<br></div>");
                buf.append("</div></div></div></div></div></div></div></div>");
                buf.append("</div><br>");
            }// end of the else sheet loop

        }// end of the for loop for sheets

        // queries.addAll(groupItemMapQueries);

        // added at the end so that items and groups already exist, tbh 5.15.07
        ncrf.setQueries(queries);
        ncrf.setItemQueries(openQueries);
        ncrf.setBackupItemQueries(backupItemQueries);
        ncrf.setItems(items);
        if (!errors.isEmpty()) {
            ncrf.setErrors(errors);
        }
        // logger.debug("html table:" + buf.toString());
        ncrf.setHtmlTable(buf.toString());
        return ncrf;
    }

    /**
     * stripQuotes, utility function meant to replace single quotes in strings
     * with double quotes for SQL compatability. Don't -> Don''t, for example.
     *
     * @param subj
     *            the subject line
     * @return A string with all the quotes escaped.
     */
    public String stripQuotes(String subj) {
        if (subj == null) {
            return null;
        }
        String returnme = "";
        String[] subjarray = subj.split("'");
        if (subjarray.length == 1) {
            returnme = subjarray[0];
        } else {
            for (int i = 0; i < subjarray.length - 1; i++) {
                returnme += subjarray[i];
                returnme += "''";
            }
            returnme += subjarray[subjarray.length - 1];
        }
        return returnme;
    }

    public String getValue(HSSFCell cell) {
        String val = null;
        int cellType = 0;
        if (cell == null) {
            cellType = HSSFCell.CELL_TYPE_BLANK;
        } else {
            cellType = cell.getCellType();
        }

        switch (cellType) {
        case HSSFCell.CELL_TYPE_BLANK:
            val = "";
            break;
        case HSSFCell.CELL_TYPE_NUMERIC:
            // YW << Modify code so that floating number alone can be used for
            // CRF version. Before it must use, e.g. v1.1
            // Meanwhile modification has been done for read PHI cell and
            // Required cell
            val = cell.getNumericCellValue() + "";
            // >> YW
            // buf.append("<td><font class=\"bodytext\">" +
            // cell.getNumericCellValue()
            // + "</font></td>");
            // added to check for whole numbers, tbh 6/5/07
            double dphi = cell.getNumericCellValue();
            if ((dphi - (int) dphi) * 1000 == 0) {
                val = (int) dphi + "";
            }
            // logger.debug("found a numeric cell after transfer: "+val);
            break;
        case HSSFCell.CELL_TYPE_STRING:
            val = cell.getStringCellValue();
            if (val.matches("'")) {
                // logger.debug("Found single quote! "+val);
                val.replaceAll("'", "''");
            }
            // buf.append("<td><font class=\"bodytext\">" +
            // cell.getStringCellValue()
            // + "</font></td>");
            break;
        case HSSFCell.CELL_TYPE_BOOLEAN:
            boolean val2 = cell.getBooleanCellValue();
            if (val2) {
                val = "true";
            } else {
                val = "false";
            }
        default:
            val = "";
            // buf.append("<td></td>");
        }

        return val.trim();
    }

    public String toHTML(int sheetIndex) throws IOException {
        StringBuffer buf = new StringBuffer();
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        int numSheets = wb.getNumberOfSheets();
        for (int j = 0; j < numSheets; j++) {
            HSSFSheet sheet = wb.getSheetAt(j);// sheetIndex);
            String sheetName = wb.getSheetName(j);
            buf.append(sheetName + "<br>");
            buf.append("<table border=\"2\"");
            buf.append("caption=\"" + wb.getSheetName(sheetIndex) + "\"" + ">");
            int numCols = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < numCols; i++) {
                buf.append("<tr>");

                if (sheet.getRow(i) == null) {
                    continue;
                }

                int numCells = sheet.getRow(i).getLastCellNum();

                for (int y = 0; y < numCells; y++) {

                    HSSFCell cell = sheet.getRow(i).getCell((short) y);
                    int cellType = 0;
                    if (cell == null) {
                        cellType = HSSFCell.CELL_TYPE_BLANK;
                    } else {
                        cellType = cell.getCellType();
                    }

                    switch (cellType) {
                    case HSSFCell.CELL_TYPE_BLANK:
                        buf.append("<td> </td>");
                        break;
                    case HSSFCell.CELL_TYPE_NUMERIC:
                        buf.append("<td>" + cell.getNumericCellValue() + "</td>");
                        break;
                    case HSSFCell.CELL_TYPE_STRING:
                        buf.append("<td>" + cell.getStringCellValue() + "</td>");
                        break;
                    default:
                        buf.append("<td></td>");
                    }
                }
                buf.append("</tr>");
            }

            buf.append("</table>");
        }// end of sheet count, added by tbh 5-31
        return buf.toString();
    }

    private String getMUInsertSql(String oid, String measurementUnitName, int ownerId, String dbName) {
        String muSql = "";
        // if (dbName.equals("oracle")) {
        // muSql =
        // "insert into measurement_unit (oc_oid, name) values ('" + oid + "',
        // '" + stripQuotes(measurementUnitName) + "', "
        // + ownerId + ", sysdate)";
        // } else {
        muSql = "insert into measurement_unit (oc_oid, name) values ('" + oid + "', '" + measurementUnitName + "')";
        // }
        return muSql;
    }

    private String getMUInsertSqlParameters() {
        return "insert into measurement_unit (oc_oid, name) values (?, ?)";
    }

    /**
     * Checks whether the parent_item is valid a name
     *
     */

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public MeasurementUnitDao getMeasurementUnitDao() {
        return measurementUnitDao;
    }

    public void setMeasurementUnitDao(MeasurementUnitDao measurementUnitDao) {
        this.measurementUnitDao = measurementUnitDao;
    }
}
