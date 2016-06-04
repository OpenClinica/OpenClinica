/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.oid.MeasurementUnitOidGenerator;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
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
 * @version CVS: $Id: SpreadSheetTable.java,v 1.28 2006/09/01 00:37:19 jxu Exp $
 */

public class SpreadSheetTableClassic implements SpreadSheetTable {// extends
    // SpreadSheetTable
    // {

    private POIFSFileSystem fs = null;

    private UserAccountBean ub = null;

    private String versionName = null;

    private int crfId = 0;

    private String crfName = "";

    private String versionIdString = "";

    private Locale locale;

    private final int studyId;

    private Set<String> existingUnits = new TreeSet<String>();

    private Set<String> existingOIDs = new TreeSet<String>();

    private MeasurementUnitDao measurementUnitDao = new MeasurementUnitDao();

    // the default; all crf ids should be > 0, tbh 8-29 :-)
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public SpreadSheetTableClassic(FileInputStream parseStream, UserAccountBean ub, String versionName, Locale locale, int studyId) throws IOException {
        // super();
        this.fs = new POIFSFileSystem(parseStream);
        this.ub = ub;
        this.versionName = versionName;
        this.locale = locale;
        this.studyId = studyId;
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
        ArrayList errors = new ArrayList();
        ArrayList repeats = new ArrayList();
        HashMap tableNames = new HashMap();
        HashMap items = new HashMap();
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
        // YW 1-30-2008
        HashMap<String, String> allItems = new HashMap<String, String>();

        ArrayList<String> itemGroupOids = new ArrayList<String>();
        ArrayList<String> itemOids = new ArrayList<String>();

        CRFDAO cdao = new CRFDAO(ds);
        CRFBean crf = (CRFBean) cdao.findByPK(crfId);

        ItemDataDAO iddao = new ItemDataDAO(ds);
        ItemDAO idao = new ItemDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        ItemGroupDAO itemGroupDao = new ItemGroupDAO(ds);

        int validSheetNum = 0;
        for (int j = 0; j < numSheets; j++) {
            HSSFSheet sheet = wb.getSheetAt(j);// sheetIndex);
            String sheetName = wb.getSheetName(j);
            if (sheetName.equalsIgnoreCase("CRF") || sheetName.equalsIgnoreCase("Sections") || sheetName.equalsIgnoreCase("Items")) {
                validSheetNum++;
            }
        }
        if (validSheetNum != 3) {
            // errors.add("The excel spreadsheet doesn't have required valid
            // worksheets. Please check whether it contains" +
            // " sheets of CRF, Sections and Items.");
            errors.add(resPageMsg.getString("excel_not_have_valid_worksheet"));
        }
        // check to see if questions are referencing a valid section name, tbh
        // 7/30
        for (int j = 0; j < numSheets; j++) {
            HSSFSheet sheet = wb.getSheetAt(j);// sheetIndex);
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
                // logger.info("PhysicalNumberOfRows" +
                // sheet.getPhysicalNumberOfRows());
                logger.info("PhysicalNumberOfRows" + sheet.getPhysicalNumberOfRows());
                // logger.info("LastRowNum()" + sheet.getLastRowNum());
                String secName = "";
                String page = "";
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

                if (sheetName.equalsIgnoreCase("Items")) {
                    logger.info("read an item in sheet" + sheetName);
                    Map labelWithType = new HashMap<String, String>();

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
                        defaultSql =
                            "INSERT INTO ITEM_GROUP ( " + "name, crf_id, status_id, date_created ,owner_id,oc_oid)" + "VALUES ('" + defaultGroup.getName()
                                + "', " + defaultGroup.getCrfId() + "," + defaultGroup.getStatus().getId() + "," + "sysdate," + ub.getId() + ",'"
                                + defaultGroupOid + "')";
                    } else {
                        defaultSql =
                            "INSERT INTO ITEM_GROUP ( " + "name, crf_id, status_id, date_created ,owner_id,oc_oid)" + "VALUES ('" + defaultGroup.getName()
                                + "', " + defaultGroup.getCrfId() + "," + defaultGroup.getStatus().getId() + "," + "now()," + ub.getId() + ",'"
                                + defaultGroupOid + "')";
                    }

                    if (!GroupCheck.containsKey("Ungrouped")) {
                        queries.add(defaultSql);
                    }
                    //Adding itemnames for further use
                    HashMap itemNames = new HashMap();
                    for (int k = 1; k < numRows; k++) {
                        HSSFCell cell = sheet.getRow(k).getCell((short) 0);
                        String itemName = getValue(cell);
                        itemName = itemName.replaceAll("<[^>]*>", "");
                        itemNames.put(k, itemName);
                    }
                    for (int k = 1; k < numRows; k++) {
                        // logger.info("hit row "+k);
                        if (blankRowCount == 5) {
                            logger.info("hit end of the row ");
                            break;
                        }
                        if (sheet.getRow(k) == null) {
                            blankRowCount++;
                            continue;
                        }
                        HSSFCell cell = sheet.getRow(k).getCell((short) 0);
                        String itemName = getValue(cell);
                        
                        itemName = itemName.replaceAll("<[^>]*>", "");
                        // regexp to make sure it is all word characters, '\w+' in regexp terms
                        if (!Utils.isMatchingRegexp(itemName, "\\w+")) {
                            // different item error to go here
                            errors.add(resPageMsg.getString("item_name_column") + " " + resPageMsg.getString("was_invalid_at_row") + " " + k + ", "
                                    + resPageMsg.getString("items_worksheet") + ". "  + resPageMsg.getString("you_can_only_use_letters_or_numbers"));
                                htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("INVALID_FIELD"));
                        }
                        if (StringUtil.isBlank(itemName)) {
                            // errors.add("The ITEM_NAME column was blank at row
                            // " + k + ", Items worksheet.");
                            // htmlErrors.put(j + "," + k + ",0", "REQUIRED
                            // FIELD");
                            errors.add(resPageMsg.getString("item_name_column") + " " + resPageMsg.getString("was_blank_at_row") + " " + k + ", "
                                + resPageMsg.getString("items_worksheet") + ". ");
                            htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("required_field"));
                        }
                        if (itemName != null && itemName.length() > 255) {
                            errors.add(resPageMsg.getString("item_name_length_error"));
                        }

                        if (repeats.contains(itemName)) {
                            // errors.add("A duplicate ITEM_NAME of " + itemName
                            // + " was detected at row " + k
                            // + ", Items worksheet.");
                            // htmlErrors.put(j + "," + k + ",0", "DUPLICATE
                            // FIELD");
                            errors.add(resPageMsg.getString("duplicate") + " " + resPageMsg.getString("item_name_column") + " " + itemName + " "
                                + resPageMsg.getString("was_detected_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("required_field"));
                        } else if (itemName.length() > 0) {
                            allItems.put(itemName, "Ungrouped");
                        }
                        repeats.add(itemName);

                        cell = sheet.getRow(k).getCell((short) 1);
                        String descLabel = getValue(cell);
                        descLabel = descLabel.replaceAll("<[^>]*>", "");

                        if (StringUtil.isBlank(descLabel)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DESCRIPTION_LABEL_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + " " + k + "," + resPageMsg.getString("items_worksheet") + ".");
                            // errors.add("The DESCRIPTION_LABEL column was
                            // blank at row " + k + "," +
                            // resPageMsg.getString("items_worksheet") +".");
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
                            if (this.existingUnits.size() > 0) {
                            } else {
                                this.existingUnits = this.measurementUnitDao.findAllNamesInUpperCase();
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
                            if (this.existingUnits.contains(unit.toUpperCase())) {
                                this.logger.error("unit=" + unit + " existed.");
                            } else {
                                String oid = "";
                                try {
                                    oid = new MeasurementUnitOidGenerator().generateOidNoValidation(unit);
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
                                this.existingUnits.add(unit.toUpperCase());
                                muSql = this.getMUInsertSql(oid, unit, ub.getId(), dbName);
                                queries.add(muSql);
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 4);
                        String rightItemText = getValue(cell);
                        if (rightItemText != null && rightItemText.length() > 2000) {
                            errors.add(resPageMsg.getString("right_item_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 5);
                        if (cell != null) {
                            secName = getValue(cell);
                            secName = secName.replaceAll("<[^>]*>", "");
                        }

                        if (secName != null && secName.length() > 2000) {
                            errors.add(resPageMsg.getString("section_label_length_error"));
                        }

                        if (!secNames.contains(secName)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_LABEL_column") + " "
                                + resPageMsg.getString("not_valid_section_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ". "
                                + resPageMsg.getString("check_to_see_that_there_is_valid_LABEL"));
                            htmlErrors.put(j + "," + k + ",5", resPageMsg.getString("NOT_A_VALID_LABEL"));
                        }
                        cell = sheet.getRow(k).getCell((short) 6);
                        String header = getValue(cell);
                        if (header != null && header.length() > 2000) {
                            errors.add(resPageMsg.getString("item_header_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 7);
                        String subHeader = getValue(cell);
                        if (subHeader != null && subHeader.length() > 240) {
                            errors.add(resPageMsg.getString("item_subheader_length_error"));
                        }

                        cell = sheet.getRow(k).getCell((short) 8);
                        String parentItem = getValue(cell);
                        parentItem = parentItem.replaceAll("<[^>]*>", "");
                        // Checking for a valid paren item name
                        if(!StringUtil.isBlank(parentItem)){
                            if(!itemNames.containsValue(parentItem)){
                                errors.add("the Parent item specified on row "+k+" does not exist in the CRF template. Please update the value. ");
                            }
                        }
                        // BWP>>Prevent parent names that equal the Item names
                        if (itemName != null && itemName.equalsIgnoreCase(parentItem)) {
                            parentItem = "";
                        }


                        cell = sheet.getRow(k).getCell((short) 9);
                        int columnNum = 0;
                        String column = getValue(cell);
                        if (!StringUtil.isBlank(column)) {
                            try {
                                columnNum = Integer.parseInt(column);
                            } catch (NumberFormatException ne) {
                                columnNum = 0;
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 10);
                        if (cell != null) {
                            page = getValue(cell);
                        }

                        cell = sheet.getRow(k).getCell((short) 11);
                        String questionNum = getValue(cell);

                        cell = sheet.getRow(k).getCell((short) 12);
                        String responseType = getValue(cell);
                        int responseTypeId = 1;
                        if (StringUtil.isBlank(responseType)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_TYPE_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",12", resPageMsg.getString("required_field"));

                        } else {
                            if (!ResponseType.findByName(responseType.toLowerCase())) {
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_TYPE_column") + " "
                                    + resPageMsg.getString("was_invalid_at_row") + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                                htmlErrors.put(j + "," + k + ",12", resPageMsg.getString("INVALID_FIELD"));
                            } else {
                                responseTypeId = ResponseType.getByName(responseType.toLowerCase()).getId();
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 13);
                        String responseLabel = getValue(cell);
                        // responseLabel = responseLabel.replaceAll("<[^>]*>",
                        // "");
                        if (StringUtil.isBlank(responseLabel) && responseTypeId != ResponseType.TEXT.getId()
                            && responseTypeId != ResponseType.TEXTAREA.getId()) {
                            // << tbh #4180
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_LABEL_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",13", resPageMsg.getString("required_field"));
                        } else if ("file".equalsIgnoreCase(responseType) && !"file".equalsIgnoreCase(responseLabel)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_LABEL_column") + " "
                                + resPageMsg.getString("should_be_file") + resPageMsg.getString("at_row") + " " + k + ", "
                                + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",13", resPageMsg.getString("should_be_file"));
                        }

                        cell = sheet.getRow(k).getCell((short) 14);
                        String resOptions = getValue(cell);
                        // resOptions = resOptions.replaceAll("<[^>]*>", "");
                        if (responseLabel.equalsIgnoreCase("text") || responseLabel.equalsIgnoreCase("textarea")) {
                            resOptions = "text";
                        }
                        if ("file".equalsIgnoreCase(responseType)) {
                            resOptions = "file";
                        }
                        // YW 2-5-2008 << set default resOptions for calculation
                        // and group-calculation type
                        // if(responseTypeId==8 || responseTypeId==9) {
                        // resOptions = resOptions.length()>0 ? resOptions :
                        // "can not calculate";
                        // }
                        // YW >>
                        int numberOfOptions = 0;
                        if (!resNames.contains(responseLabel) && StringUtil.isBlank(resOptions) && responseTypeId != ResponseType.TEXT.getId()
                            && responseTypeId != ResponseType.TEXTAREA.getId()) {
                            // << tbh #4180
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_OPTIONS_TEXT_column")
                                + resPageMsg.getString("was_blank_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",14", resPageMsg.getString("required_field"));
                        }
                        if (!resNames.contains(responseLabel) && !StringUtil.isBlank(resOptions)) {
                            // YW 1-29-2008 << only one option for "calculation"
                            // type and "group-calculation" type
                            // but do we really need this variable these two
                            // types?
                            // actually, responseTypeId=9 is not necessary for
                            // old template
                            if (responseTypeId == 8 || responseTypeId == 9) {
                                numberOfOptions = 1;
                                // YW >>
                            } else {
                                // String[] resArray = resOptions.split(",");
                                String text1 = resOptions.replaceAll("\\\\,", "##");
                                String[] resArray = text1.split(",");
                                numberOfOptions = resArray.length;
                            }
                        }
                        cell = sheet.getRow(k).getCell((short) 15);
                        String resValues = getValue(cell);
                        String value1 = resValues.replaceAll("\\\\,", "##");
                        String[] resValArray = value1.split(",");
                        if (responseLabel.equalsIgnoreCase("text") || responseLabel.equalsIgnoreCase("textarea")) {
                            resValues = "text";
                        }
                        if ("file".equalsIgnoreCase(responseType)) {
                            resValues = "file";
                        }
                        if (!resNames.contains(responseLabel) && StringUtil.isBlank(resValues) && responseTypeId != ResponseType.TEXT.getId()
                            && responseTypeId != ResponseType.TEXTAREA.getId()) {
                            // << tbh, #4180, add textarea too?
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("required_field"));
                        }
                        // YW 1-25-2008 << validate scoring expression
                        if (responseTypeId == 8 || responseTypeId == 9) {
                            // right now, func is not required; but if there is
                            // func, it must be correctly spelled
                            if (resValues.contains(":")) {
                                String[] s = resValues.split(":");
                                if (!"func".equalsIgnoreCase(s[0].trim())) {
                                    errors.add(resPageMsg.getString("expression_not_start_with_func_at") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet") + ". ");
                                    htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("INVALID_FIELD"));
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
                                htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("INVALID_FIELD"));
                            }
                            String group = "Ungrouped";
                            for (String v : variables) {
                                if (!allItems.containsKey(v)) {
                                    errors.add(resPageMsg.getString("item") + v + resPageMsg.getString("must_listed_before_item") + itemName
                                        + resPageMsg.getString("item_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ". ");
                                    htmlErrors.put(j + "," + k + ",15", resPageMsg.getString("INVALID_FIELD"));
                                }
                            }
                        } else if (numberOfOptions > 0) {
                            // YW >>
                            if (resValArray.length != numberOfOptions) {
                                errors.add(resPageMsg.getString("incomplete_option_value_pair") + " " + resPageMsg.getString("RESPONSE_OPTIONS_column") + " "
                                    + resPageMsg.getString("and") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " " + resPageMsg.getString("at_row") + k
                                    + " " + resPageMsg.getString("items_worksheet") + "; " + resPageMsg.getString("perhaps_missing_comma"));
                                htmlErrors.put(j + ", " + k + ", 14", resPageMsg.getString("number_option_not_match"));
                                htmlErrors.put(j + ", " + k + ", 15", resPageMsg.getString("number_value_not_match"));
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 16);
                        String dataType = getValue(cell);
                        dataType = dataType.replaceAll("<[^>]*>", "");
                        String dataTypeIdString = "1";
                        if (StringUtil.isBlank(dataType)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet"));
                            htmlErrors.put(j + ", " + k + ", 16", resPageMsg.getString("required_field"));

                        } else {
                            if (!ItemDataType.findByName(dataType.toLowerCase())) {
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                    + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ".");
                                htmlErrors.put(j + ", " + k + ", 16", resPageMsg.getString("INVALID_FIELD"));
                            } else {
                                if ("file".equalsIgnoreCase(responseType) && !"FILE".equalsIgnoreCase(dataType)) {
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                        + resPageMsg.getString("should_be_file") + " "+resPageMsg.getString("at_row") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet") + ".");
                                    htmlErrors.put(j + ", " + k + ", 16", resPageMsg.getString("should_be_file"));
                                }
                                // dataTypeId =
                                // (ItemDataType.getByName(dataType)).getId();
                                dataTypeIdString = "(SELECT ITEM_DATA_TYPE_ID From ITEM_DATA_TYPE Where CODE='" + dataType.toUpperCase() + "')";
                            }
                        }

                        if (responseTypeId == 3 || responseTypeId == 5 || responseTypeId == 6 || responseTypeId == 7) {
                            // make sure same responseLabels have same datatype
                            if (labelWithType.containsKey(responseLabel)) {
                                logger.debug("in label=" + responseLabel);
                                if (!dataType.equalsIgnoreCase(labelWithType.get(responseLabel).toString())) {
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("DATA_TYPE_column") + " "
                                        + resPageMsg.getString("does_not_match_the_item_data_type_with_the_same_response_label") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet"));
                                    htmlErrors.put(j + "," + k + ",16", resPageMsg.getString("INVALID_FIELD"));
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
                                                    Integer I = Integer.parseInt(s.trim());
                                                    // eg, s=2.3 => I=2,
                                                    // but 2.3 is not integer
                                                    if (!I.toString().equals(s.trim())) {
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
                                                + resPageMsg.getString("items_worksheet") + ".");
                                            htmlErrors.put(j + ", " + k + ", 15", resPageMsg.getString("should_be_integer"));
                                        }
                                    } else if ("real".equalsIgnoreCase(dataType)) {
                                        for (String s : resValArray) {
                                            String st = s != null && s.length() > 0 ? s.trim() : "";
                                            if (st.length() > 0) {
                                                try {
                                                    Double I = Double.parseDouble(s.trim());
                                                } catch (Exception e) {
                                                    wrongType = true;
                                                }
                                            }
                                        }
                                        if (wrongType) {
                                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("RESPONSE_VALUES_column") + " "
                                                + resPageMsg.getString("should_be_real") + " " + resPageMsg.getString("at_row") + " " + k + ", "
                                                + resPageMsg.getString("items_worksheet") + ".");
                                            htmlErrors.put(j + ", " + k + ", 15", resPageMsg.getString("should_be_real"));
                                        }
                                    }
                                }
                            }
                        }

                        cell = sheet.getRow(k).getCell((short) 17);
                        String regexp = getValue(cell);
                        String regexp1 = "";
                        if (!StringUtil.isBlank(regexp)) {
                            // parse the string and get reg exp eg. regexp:
                            // /[0-9]*/
                            regexp1 = regexp.trim();
                            if (regexp1.startsWith("regexp:")) {
                                String finalRegexp = regexp1.substring(7).trim();
                                if (finalRegexp.contains("\\\\")) {
                                    // \\ in the regular expression it should
                                    // not be allowed
                                    errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column") + " "
                                        + resPageMsg.getString("has_an_invalid_regular_expression_at_row") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet") + ". " + resPageMsg.getString("regular_expression_contained") + " '\\\\', "
                                        + resPageMsg.getString("it_should_only_contain_one") + "'\\'. ");
                                    htmlErrors.put(j + "," + k + ",17", resPageMsg.getString("INVALID_FIELD"));
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
                                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column")
                                                + resPageMsg.getString("has_an_invalid_regular_expression_at_row") + " " + k + ", "
                                                + resPageMsg.getString("items_worksheet") + ". " + resPageMsg.getString("Example:") + " regexp: /[0-9]*/ ");
                                            htmlErrors.put(j + "," + k + ",17", resPageMsg.getString("INVALID_FIELD"));
                                        }
                                    } else {
                                        // errors.add("The VALIDATION column has
                                        // an invalid regular expression at row
                                        // " + k
                                        // + ", Items worksheet. Example:
                                        // regexp: /[0-9]*/ ");
                                        errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column") + " "
                                            + resPageMsg.getString("has_an_invalid_regular_expression_at_row") + " " + k + ", "
                                            + resPageMsg.getString("items_worksheet") + ". " + resPageMsg.getString("Example") + " regexp: /[0-9]*/ ");
                                        htmlErrors.put(j + "," + k + ",17", resPageMsg.getString("INVALID_FIELD"));
                                    }
                                }

                            } else if (regexp1.startsWith("func:")) {
                                boolean isProperFunction = false;
                                try {
                                    Validator.processCRFValidationFunction(regexp1);
                                    isProperFunction = true;
                                } catch (Exception e) {
                                    errors.add(e.getMessage() + ", " + resPageMsg.getString("at_row") + " " + k + ", "
                                        + resPageMsg.getString("items_worksheet") + ". ");
                                    htmlErrors.put(j + "," + k + ",17", resPageMsg.getString("INVALID_FIELD"));
                                }
                            } else {
                                errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_column") + " "
                                    + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ". ");
                                htmlErrors.put(j + "," + k + ",17", resPageMsg.getString("INVALID_FIELD"));
                            }

                        }

                        cell = sheet.getRow(k).getCell((short) 18);
                        String regexpError = getValue(cell);
                        regexpError = regexpError.replaceAll("<[^>]*>", "");
                        if (!StringUtil.isBlank(regexp) && StringUtil.isBlank(regexpError)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("VALIDATION_ERROR_MESSAGE_column")
                                + resPageMsg.getString("was_blank_at_row") + k + ", " + resPageMsg.getString("items_worksheet") + ". "
                                + resPageMsg.getString("cannot_be_blank_if_VALIDATION_not_blank"));
                            htmlErrors.put(j + "," + k + ",18", resPageMsg.getString("required_field"));
                        }
                        if (regexpError != null && regexpError.length() > 255) {
                            errors.add(resPageMsg.getString("regexp_errror_length_error"));
                        }

                        boolean phiBoolean = false;
                        cell = sheet.getRow(k).getCell((short) 19);
                        // String phi = getValue(cell);
                        String phi = "";
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            double dphi = cell.getNumericCellValue();
                            if ((dphi - (int) dphi) * 1000 == 0) {
                                phi = (int) dphi + "";
                            }
                        }
                        if (!"0".equals(phi) && !"1".equals(phi)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("PHI_column") + resPageMsg.getString("was_invalid_at_row") + k
                                + ", " + resPageMsg.getString("items_worksheet") + ". " + resPageMsg.getString("PHI_column") + " "
                                + resPageMsg.getString("can_only_be_either_0_or_1"));
                            htmlErrors.put(j + "," + k + ",19", resPageMsg.getString("INVALID_VALUE"));
                        } else {
                            phiBoolean = "1".equals(phi) ? true : false;
                        }

                        boolean isRequired = false;
                        cell = sheet.getRow(k).getCell((short) 20);
                        String required = getValue(cell);
                        logger.info(getValue(cell));
                        // does the above no longer work???
                        // String required = "";
                        if (StringUtil.isBlank(required)) {
                            required = "0";
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            double dr = cell.getNumericCellValue();
                            if ((dr - (int) dr) * 1000 == 0) {
                                required = (int) dr + "";
                            }
                        }

                        if (!"0".equals(required) && !"1".equals(required)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("REQUIRED_column") + " "
                                + resPageMsg.getString("was_invalid_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet") + ". "
                                + resPageMsg.getString("REQUIRED_column") + resPageMsg.getString("can_only_be_either_0_or_1"));
                            htmlErrors.put(j + "," + k + ",20", resPageMsg.getString("INVALID_VALUE"));
                        } else {
                            isRequired = "1".equals(required) ? true : false;
                        }

                        // Create oid for Item Bean
                        String itemOid = idao.getValidOid(new ItemBean(), crfName, itemName, itemOids);
                        itemOids.add(itemOid);

                        // better spot for checking item might be right here,
                        // tbh 7-25
                        String vlSql = "";
                        if (dbName.equals("oracle")) {
                            vlSql =
                                "INSERT INTO ITEM (NAME,DESCRIPTION,UNITS,PHI_STATUS,"
                                    + "ITEM_DATA_TYPE_ID, ITEM_REFERENCE_TYPE_ID,STATUS_ID,OWNER_ID,DATE_CREATED,OC_OID) " + "VALUES ('"
                                    + stripQuotes(itemName) + "','" + stripQuotes(descLabel) + "','" + stripQuotes(unit) + "'," + (phiBoolean == true ? 1 : 0)
                                    + "," + dataTypeIdString + ",1,1," + ub.getId() + ", sysdate" + ",'" + itemOid + "')";
                        } else {
                            vlSql =
                                "INSERT INTO ITEM (NAME,DESCRIPTION,UNITS,PHI_STATUS,"
                                    + "ITEM_DATA_TYPE_ID, ITEM_REFERENCE_TYPE_ID,STATUS_ID,OWNER_ID,DATE_CREATED,OC_OID) " + "VALUES ('"
                                    + stripQuotes(itemName) + "','" + stripQuotes(descLabel) + "','" + stripQuotes(unit) + "'," + phiBoolean + ","
                                    + dataTypeIdString + ",1,1," + ub.getId() + ", NOW()" + ",'" + itemOid + "')";
                        }

                        backupItemQueries.put(itemName, vlSql);
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
                        String resOptions1 = resOptions.replaceAll("\\\\,", "\\,");
                        String resValues1 = resValues.replaceAll("\\\\,", "\\,");
                        rsb.setOptions(stripQuotes(resOptions1), stripQuotes(resValues1));

                        ItemFormMetadataBean ifmb = new ItemFormMetadataBean();
                        ifmb.setResponseSet(rsb);
                        ib.setItemMeta(ifmb);
                        items.put(itemName, ib);

                        int ownerId = ub.getId();

                        if (!itemCheck.containsKey(itemName)) {// item not in
                            // the DB
                            openQueries.put(itemName, vlSql);

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
                                            "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel) + "'," + "UNITS='" + stripQuotes(unit) + "',"
                                                + "PHI_STATUS=" + (phiBoolean ? 1 : 0) + "," + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                + " AND item.item_id = versioning_map.item_id)" + " AND item.name='" + stripQuotes(itemName)
                                                + "' AND item.owner_id = " + ownerId;
                                    } else {
                                        upSql =
                                            "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel) + "'," + "UNITS='" + stripQuotes(unit) + "',"
                                                + "PHI_STATUS=" + phiBoolean + "," + "ITEM_DATA_TYPE_ID=" + dataTypeIdString
                                                + " FROM versioning_map, crf_version" + " WHERE item.name='" + stripQuotes(itemName) + "' AND item.owner_id = "
                                                + ownerId + " AND item.item_id = versioning_map.item_id AND"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;
                                    }// end of if dbname
                                    openQueries.put(itemName, upSql);
                                } else {
                                    String upSql = "";
                                    if (dbName.equals("oracle")) {
                                        upSql =
                                            "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel) + "'," + "PHI_STATUS=" + (phiBoolean ? 1 : 0)
                                                + " WHERE exists (SELECT versioning_map.item_id from versioning_map, crf_version where"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id= " + crfId
                                                + " AND item.item_id = versioning_map.item_id)" + " AND item.name='" + stripQuotes(itemName)
                                                + "' AND item.owner_id = " + ownerId;

                                    } else {
                                        upSql =
                                            "UPDATE ITEM SET DESCRIPTION='" + stripQuotes(descLabel) + "'," + "PHI_STATUS=" + phiBoolean
                                                + " FROM versioning_map, crf_version" + " WHERE item.name='" + stripQuotes(itemName) + "' AND item.owner_id = "
                                                + ownerId + " AND item.item_id = versioning_map.item_id AND"
                                                + " versioning_map.crf_version_id = crf_version.crf_version_id" + " AND crf_version.crf_id = " + crfId;
                                    }// end of if dbName
                                    openQueries.put(itemName, upSql);
                                }
                            } else {
                                ownerId = oldItem.getOwner().getId();
                            }
                        }
                        String sql = "";
                        if (dbName.equals("oracle")) {
                            // resOptions = resOptions.replaceAll("\\\\,",
                            // "\\,");
                            sql =
                                "INSERT INTO RESPONSE_SET (LABEL, OPTIONS_TEXT, OPTIONS_VALUES, " + "RESPONSE_TYPE_ID, VERSION_ID)" + " VALUES ('"
                                    + stripQuotes(responseLabel) + "', '" + stripQuotes(resOptions.replaceAll("\\\\,", "\\,")) + "','"
                                    + stripQuotes(resValues.replace("\\\\", "\\")) + "'," + "(SELECT RESPONSE_TYPE_ID From RESPONSE_TYPE Where NAME='"
                                    + stripQuotes(responseType.toLowerCase()) + "')," + versionIdString + ")";
                        } else {
                            sql =
                                "INSERT INTO RESPONSE_SET (LABEL, OPTIONS_TEXT, OPTIONS_VALUES, " + "RESPONSE_TYPE_ID, VERSION_ID)" + " VALUES ('"
                                    + stripQuotes(responseLabel) + "', E'" + stripQuotes(resOptions) + "', E'" + stripQuotes(resValues) + "',"
                                    + "(SELECT RESPONSE_TYPE_ID From RESPONSE_TYPE Where NAME='" + stripQuotes(responseType.toLowerCase()) + "'),"
                                    + versionIdString + ")";
                        }
                        if (!resNames.contains(responseLabel)) {
                            queries.add(sql);
                            resNames.add(responseLabel);
                            // this will have to change since we have some data
                            // in the actual
                            // spreadsheet
                            // change it to caching response set names in a
                            // collection?
                            // or just delete the offending cells from the
                            // spreadsheet?
                        }
                        String parentItemString = "0";
                        if (!StringUtil.isBlank(parentItem)) {
                            if (dbName.equals("oracle")) {
                                parentItemString =
                                    "(SELECT MAX(ITEM_ID) FROM ITEM WHERE NAME='" + stripQuotes(parentItem) + "' AND owner_id = " + ownerId + " )";
                            } else {
                                parentItemString =
                                    "(SELECT ITEM_ID FROM ITEM WHERE NAME='" + stripQuotes(parentItem) + "' AND owner_id = " + ownerId
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
                                + " ORDER BY I.OC_ID DESC LIMIT 1) ";

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
                        if (dbName.equals("oracle")) {
                            sql2 =
                                "INSERT INTO ITEM_FORM_METADATA (CRF_VERSION_ID, RESPONSE_SET_ID," + "ITEM_ID,SUBHEADER,header,LEFT_ITEM_TEXT,"
                                    + "RIGHT_ITEM_TEXT,PARENT_ID,SECTION_ID,ORDINAL,PARENT_LABEL,COLUMN_NUMBER,PAGE_NUMBER_LABEL,question_number_label,"
                                    + "REGEXP,REGEXP_ERROR_MSG,REQUIRED)" + " VALUES ("
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
                                    + stripQuotes(regexp1) + "','" + stripQuotes(regexpError) + "', " + (isRequired ? 1 : 0) + ")";

                            logger.warn(sql2);

                        } else {
                            sql2 =
                                "INSERT INTO ITEM_FORM_METADATA (CRF_VERSION_ID, RESPONSE_SET_ID," + "ITEM_ID,SUBHEADER,HEADER,LEFT_ITEM_TEXT,"
                                    + "RIGHT_ITEM_TEXT,PARENT_ID,SECTION_ID,ORDINAL,PARENT_LABEL,COLUMN_NUMBER,PAGE_NUMBER_LABEL,question_number_label,"
                                    + "REGEXP,REGEXP_ERROR_MSG,REQUIRED)" + " VALUES ("
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
                                    + stripQuotes(regexp1) + "','" + stripQuotes(regexpError) + "', " + isRequired + ")";
                        }

                        queries.add(sql2);
                        // link version with items now
                        String sql3 = "";
                        if (dbName.equals("oracle")) {
                            sql3 =
                                "INSERT INTO VERSIONING_MAP (CRF_VERSION_ID, ITEM_ID) VALUES ( " + versionIdString + "," + selectCorrectItemQueryOracle + ")";
                        } else {
                            sql3 =
                                "INSERT INTO VERSIONING_MAP (CRF_VERSION_ID, ITEM_ID) VALUES ( " + versionIdString + "," + selectCorrectItemQueryPostgres + ")";
                        }
                        queries.add(sql3);

                        // this item doesn't have group, so put it into
                        // 'Ungrouped' group
                        String sqlGroupLabel = "";
                        if (dbName.equals("oracle")) {
                            sqlGroupLabel =
                                "INSERT INTO ITEM_GROUP_METADATA (" + "item_group_id,header," + "subheader, layout, repeat_number, repeat_max,"
                                    + " repeat_array,row_start_number, crf_version_id," + "item_id , ordinal, borders) VALUES ("
                                    + "(SELECT MAX(ITEM_GROUP_ID) FROM ITEM_GROUP WHERE NAME='Ungrouped' AND crf_id = "
                                    + crfId
                                    + " ),'"
                                    + ""
                                    + "', '"
                                    + ""
                                    + "', '"
                                    + ""
                                    + "', "
                                    + 1
                                    + ", "
                                    + 1
                                    + ", '', 1,"
                                    + versionIdString
                                    + ","
                                    // + "(SELECT MAX(ITEM_ID) FROM ITEM WHERE
                                    // NAME='"
                                    // + itemName + "' ),"
                                    + "(SELECT MAX(ITEM.ITEM_ID) FROM ITEM,ITEM_FORM_METADATA,CRF_VERSION WHERE ITEM.NAME='"
                                    + itemName
                                    + "' "
                                    + "AND ITEM.ITEM_ID = ITEM_FORM_METADATA.ITEM_ID and ITEM_FORM_METADATA.CRF_VERSION_ID=CRF_VERSION.CRF_VERSION_ID "
                                    + "AND CRF_VERSION.CRF_ID= " + crfId + " )," + k + ",0)";

                        } else {
                            sqlGroupLabel =
                                "INSERT INTO ITEM_GROUP_METADATA (" + "item_group_id,HEADER," + "subheader, layout, repeat_number, repeat_max,"
                                    + " repeat_array,row_start_number, crf_version_id," + "item_id , ordinal, borders) VALUES ("
                                    + "(SELECT ITEM_GROUP_ID FROM ITEM_GROUP WHERE NAME='Ungrouped' AND crf_id = "
                                    + crfId
                                    + " ORDER BY oc_oid DESC LIMIT 1),'"
                                    + ""
                                    + "', '"
                                    + ""
                                    + "', '"
                                    + ""
                                    + "', "
                                    + 1
                                    + ", "
                                    + 1
                                    + ", '', 1,"
                                    + versionIdString
                                    + ","
                                    // + "(SELECT ITEM_ID FROM ITEM WHERE
                                    // NAME='" + itemName + "' ORDER BY OID DESC
                                    // LIMIT 1),"
                                    + "(SELECT ITEM.ITEM_ID FROM ITEM,ITEM_FORM_METADATA,CRF_VERSION WHERE ITEM.NAME='"
                                    + itemName
                                    + "' "
                                    + "AND ITEM.ITEM_ID = ITEM_FORM_METADATA.ITEM_ID and ITEM_FORM_METADATA.CRF_VERSION_ID=CRF_VERSION.CRF_VERSION_ID "
                                    + "AND CRF_VERSION.CRF_ID= " + crfId + " ORDER BY ITEM.OC_OID DESC LIMIT 1)," + k + ",0)";
                        }

                        queries.add(sqlGroupLabel);
                    }
                } else if (sheetName.equalsIgnoreCase("Sections")) {
                    logger.info("read sections");

                    // multiple rows, six cells, last one is number
                    for (int k = 1; k < numRows; k++) {
                        if (blankRowCount == 5) {
                            logger.info("hit end of the row ");
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
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_LABEL_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + k + " " + ", " + resPageMsg.getString("sections_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("required_field"));
                        }
                        if (secLabel != null && secLabel.length() > 2000) {
                            errors.add(resPageMsg.getString("section_label_length_error"));
                        }

                        if (secNames.contains(secLabel)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_LABEL_column")
                                + resPageMsg.getString("was_a_duplicate_of") + secLabel + " " + resPageMsg.getString("at_row") + " " + k + ", "
                                + resPageMsg.getString("sections_worksheet") + ".");
                            htmlErrors.put(j + "," + k + ",0", resPageMsg.getString("DUPLICATE_FIELD"));
                        }
                        // logger.info("section name:" + secLabel + "row num:"
                        // +k);
                        secNames.add(secLabel);
                        cell = sheet.getRow(k).getCell((short) 1);
                        String title = getValue(cell);
                        title = title.replaceAll("<[^>]*>", "");
                        if (StringUtil.isBlank(title)) {
                            errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("SECTION_TITLE_column") + " "
                                + resPageMsg.getString("was_blank_at_row") + " " + k + ", " + resPageMsg.getString("sections_worksheet") + ".");
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
                        String sql = "";
                        if (dbName.equals("oracle")) {
                            sql =
                                "INSERT INTO SECTION (CRF_VERSION_ID," + "STATUS_ID,LABEL, TITLE, INSTRUCTIONS, SUBTITLE, PAGE_NUMBER_LABEL,"
                                    + "ORDINAL, PARENT_ID, OWNER_ID, DATE_CREATED) " + "VALUES (" + versionIdString + ",1,'" + secLabel + "','"
                                    + stripQuotes(title) + "', '" + stripQuotes(instructions) + "', '" + stripQuotes(subtitle) + "','" + pageNumber + "'," + k
                                    + "," + parentId + "," + ub.getId() + ",sysdate)";
                        } else {
                            sql =
                                "INSERT INTO SECTION (CRF_VERSION_ID," + "STATUS_ID,LABEL, TITLE, INSTRUCTIONS, SUBTITLE, PAGE_NUMBER_LABEL,"
                                    + "ORDINAL, PARENT_ID, OWNER_ID, DATE_CREATED) " + "VALUES (" + versionIdString + ",1,'" + secLabel + "','"
                                    + stripQuotes(title) + "', '" + stripQuotes(instructions) + "', '" + stripQuotes(subtitle) + "','" + pageNumber + "'," + k
                                    + "," + parentId + "," + ub.getId() + ",NOW())";
                        }
                        queries.add(sql);
                    }// end for loop
                } else if (sheetName.equalsIgnoreCase("CRF")) {
                    logger.info("read crf");
                    // one row, four cells, all strings
                    if (sheet == null || sheet.getRow(1) == null || sheet.getRow(1).getCell((short) 0) == null) {
                        throw new CRFReadingException("Blank row found in sheet CRF.");
                    }

                    HSSFCell cell = sheet.getRow(1).getCell((short) 0);
                    crfName = getValue(cell);
                    crfName = crfName.replaceAll("<[^>]*>", "");
                    if (StringUtil.isBlank(crfName)) {
                        // errors.add(resPageMsg.getString("the") + " " +
                        // resPageMsg.getString("CRF_NAME_column")
                        // +
                        // resPageMsg.getString("was_blank_in_the_CRF_worksheet"));
                        // htmlErrors.put(j + ",1,0",
                        // resPageMsg.getString("required_field"));
                        throw new CRFReadingException("The CRF_NAME column was blank in the CRF worksheet.");
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

                    // TODO Why the following codes are commented out? -jxu
                    // try {
                    // CRFBean checkName = (CRFBean) cdao.findByPK(crfId);
                    // if (!checkName.getName().equals(crfName)) {
                    // logger.info("crf name is mismatch");
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
                            CoreResources.setSchema(con);

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
                            String createCRFSql = "";
                            if (dbName.equals("oracle")) {
                                createCRFSql =
                                    "INSERT INTO CRF (CRF_ID, STATUS_ID, NAME, DESCRIPTION, OWNER_ID, DATE_CREATED, OC_OID, SOURCE_STUDY_ID) VALUES (" + crfId
                                        + ", 1,'" + stripQuotes(crfName) + "','" + stripQuotes(versionDesc) + "'," + ub.getId() + ",sysdate" + ",'" + crfOid
                                        + "'," + studyId + ")";
                            } else {
                                createCRFSql =
                                    "INSERT INTO CRF (CRF_ID, STATUS_ID, NAME, DESCRIPTION, OWNER_ID, DATE_CREATED, OC_OID, SOURCE_STUDY_ID) VALUES (" + crfId
                                        + ", 1,'" + stripQuotes(crfName) + "','" + stripQuotes(versionDesc) + "'," + ub.getId() + ",NOW()" + ",'" + crfOid
                                        + "'," + studyId + ")";
                            }
                            queries.add(createCRFSql);
                        } catch (SQLException e) {
                            logger.warn("Exception encountered with query select nextval('crf_crf_id_seq'), Message-" + e.getMessage());
                        } finally {
                            if (con != null) {
                                try {
                                    con.close();
                                } catch (SQLException e) {
                                    logger.warn("Connectin can't be closed");
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
                        logger.info("found a matching version name..." + version);
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
                        logger.warn("TEST 2");
                        if (crfId == 0) {
                            sql =
                                "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                                    + "VALUES ('" + stripQuotes(version) + "','" + stripQuotes(versionDesc) + "'," + "(SELECT CRF_ID FROM CRF WHERE NAME='"
                                    + crfName + "'),1,sysdate," + ub.getId() + ",'" + stripQuotes(revisionNotes) + "','" + oid + "')";
                        } else {
                            sql =
                                "INSERT INTO CRF_VERSION (NAME,DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) " + "VALUES ('"
                                    + version + "','" + stripQuotes(versionDesc) + "'," + crfId + ",1,sysdate," + ub.getId() + ",'"
                                    + stripQuotes(revisionNotes) + "','" + oid + "')";
                        }
                    } else {
                        if (crfId == 0) {
                            sql =
                                "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) "
                                    + "VALUES ('" + stripQuotes(version) + "','" + stripQuotes(versionDesc) + "'," + "(SELECT CRF_ID FROM CRF WHERE NAME='"
                                    + crfName + "'),1,NOW()," + ub.getId() + ",'" + stripQuotes(revisionNotes) + "','" + oid + "')";
                        } else {
                            sql =
                                "INSERT INTO CRF_VERSION (NAME,DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," + "OWNER_ID,REVISION_NOTES,OC_OID) " + "VALUES ('"
                                    + version + "','" + stripQuotes(versionDesc) + "'," + crfId + ",1,NOW()," + ub.getId() + ",'" + stripQuotes(revisionNotes)
                                    + "','" + oid + "')";
                        }
                    }

                    queries.add(sql);
                    for (int i = 0; i < queries.size(); i++) {
                        String s = (String) queries.get(i);
                        logger.info("====================" + s);
                    }
                    pVersion = version;
                    pVerDesc = versionDesc;
                }

                versionIdString = "(SELECT CRF_VERSION_ID FROM CRF_VERSION WHERE NAME ='" + pVersion + "' AND CRF_ID=" + crfId + ")";

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
        ncrf.setQueries(queries);
        ncrf.setItemQueries(openQueries);
        ncrf.setBackupItemQueries(backupItemQueries);
        ncrf.setItems(items);
        if (!errors.isEmpty()) {
            ncrf.setErrors(errors);
        }
        // logger.info("html table:" + buf.toString());
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
            logger.info("found a numeric cell: " + val);
            // what if the version is a whole number? added other code below
            // from PHI, tbh 6/5/07
            // So now we also treat 3, 3.0, 3.00 as same as 3. If users want 3.0
            // or 3.10 the best way is use String type. -YW
            // >> YW
            double dphi = cell.getNumericCellValue();
            if ((dphi - (int) dphi) * 1000 == 0) {
                val = (int) dphi + "";
            }
            logger.info("found a numeric cell after transfer: " + val);
            // buf.append("<td><font class=\"bodytext\">" +
            // cell.getNumericCellValue()
            // + "</font></td>");
            break;
        case HSSFCell.CELL_TYPE_STRING:
            val = cell.getStringCellValue();
            if (val.matches("'")) {
                // logger.info("Found single quote! "+val);
                val.replaceAll("'", "''");
            }
            // buf.append("<td><font class=\"bodytext\">" +
            // cell.getStringCellValue()
            // + "</font></td>");
            break;
        default:
            val = "";
            // buf.append("<td></td>");
        }
        // logger.info("final val returned: "+val);
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
        return "insert into measurement_unit (oc_oid, name) values ('" + oid + "', '" + stripQuotes(measurementUnitName) + "')";
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
