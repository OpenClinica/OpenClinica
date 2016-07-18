/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author jxu
 * 
 */
@Deprecated
public class SPSSReportBean extends ReportBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final static int FIRSTCASE = 2;

    private final static int FIRSTCASE_IND = 2;

    private final static int COLUMNS_IND = FIRSTCASE_IND - 1;

    // YW 10-31-2007, more titles have been added because of additional
    // attrubutes added for extracting data\
    private static final String[] builtin =
        { "SubjID", "ProtocolID", "DOB", "YOB", "Gender", "SubjectStatus", "UniqueID", "SecondaryID", "Location", "StartDate", "EndDate", "SubjectEventStatus",
            "AgeAtEvent", "InterviewDate", "InterviewerName", "CRFVersionStatus", "VersionName" };

    private static final String[] builtinType =
        { "A", "A", "ADATE10", "ADATE10", "A", "A", "A", "A", "A", "ADATE10", "ADATE10", "A", "F8.0", "ADATE10", "A", "A", "A" };

    // hold validated variable name
    private final ArrayList itemNames = new ArrayList();
    // YW >>

    public static final List list = Arrays.asList(builtin);

    public HashMap descriptions = new HashMap();

    private boolean gender = false;// whether exporting gender

    private String datFileName = "C:\\path\\to\\your\\file.dat";

    protected Locale locale = ResourceBundleProvider.getLocale();
    protected String local_df_string = ResourceBundleProvider.getFormatBundle(locale).getString("date_format_string");
    protected String local_datetime_string = ResourceBundleProvider.getFormatBundle(locale).getString("date_time_format_string");

    /**
     * @return Returns the datFileName.
     */
    public String getDatFileName() {
        return datFileName;
    }

    /**
     * @param datFileName
     *            The datFileName to set.
     */
    public void setDatFileName(String datFileName) {
        this.datFileName = datFileName;
    }

    /**
     * @return Returns the descriptions.
     */
    public HashMap getDescriptions() {
        return descriptions;
    }

    /**
     * @param descriptions
     *            The descriptions to set.
     */
    public void setDescriptions(HashMap descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * @return Returns the gender.
     */
    public boolean isGender() {
        return gender;
    }

    /**
     * @param gender
     *            The gender to set.
     */
    public void setGender(boolean gender) {
        this.gender = gender;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "";
    }

    public StringBuffer getMetadataFile(SPSSVariableNameValidator svnv, ExtractBean eb) {
        itemNames.clear();
        String[] attributes = createAttributes(eb); // Is it necessary to
        // validate
        // StudyGroupClassBean
        // names? YW
        String[] attributeTypes = createAttributeTypes(eb);

        StringBuffer answer = new StringBuffer();
        answer.append("* NOTE: If you have put this file in a different folder \n" + "* from the associated data file, you will have to change the FILE \n"
            + "* location on the line below to point to the physical location of your data file.\n");

        answer.append("GET DATA  " + "/TYPE = TXT" + "/FILE = '" + getDatFileName() + "' " + "/DELCASE = LINE " + "/DELIMITERS = \"\\t\" "
            + "/ARRANGEMENT = DELIMITED " + "/FIRSTCASE = " + FIRSTCASE + " " + "/IMPORTCASE = ALL ");// since
        // we
        // send
        // the
        // row
        // of column
        // to the .dat file, start
        // importing at row 2

        if (data.size() <= 0) {
            answer.append(".\n");
        } else {
            ArrayList columns = (ArrayList) data.get(this.COLUMNS_IND);

            int startItem = columns.size() - items.size();

            logger.debug("--> generated start item " + startItem + " from " + columns.size() + " minus " + items.size());
            answer.append("/VARIABLES = \n");

            int index;
            for (int i = 0; i < columns.size(); i++) {
                String itemName = (String) columns.get(i);
                // YW << Why do we need "V_" here? Right now it has been
                // removed, because:
                // This "V_" exists in .sps file but not exists in .dat file.
                // String varLabel = "V_" + itemName;
                String varLabel = svnv.getValidName(itemName);
                itemNames.add(varLabel);
                // builtin attributes
                if (i < startItem) {
                    if (varLabel.startsWith("Gender")) {
                        gender = true;
                    }

                    index = builtinIndex(varLabel, attributes);

                    logger.debug("varLabel[" + varLabel + "] index[" + index + "] attributeTypes[" + attributeTypes[1] + "]");

                    answer.append("\t" + varLabel + " " + attributeTypes[index]);
                    // >> tbh #5524
                    DisplayItemHeaderBean dih = (DisplayItemHeaderBean) items.get(i);
                    ItemBean ib = dih.getItem();
                    // << tbh
                    if (attributeTypes[index].equals("A")) {
                        int len = getDataColumnMaxLen(i);
                        if (len == 0) {
                            len = 1; // mininum length required by spss
                        }
                        // >> tbh #5524
                        ArrayList metas = ib.getItemMetas();
                        
                        for (int k = 0; k < metas.size(); k++) {
                            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(k);
                            ResponseSetBean rsb = ifmb.getResponseSet();
                            ArrayList options = rsb.getOptions();
                            for (int l = 0; l < options.size(); l++) {
                                ResponseOptionBean ro = (ResponseOptionBean) options.get(l);
                                if (ro.getText().length() > len) {
                                    len = ro.getText().length();
                                 }
                            }
                        }
                        // << tbh
                        if (len > 8) {
                            len = 8;
                        }
                        answer.append(len);
                    }
                    answer.append("\n");
                }
                // YW >>
            }

            // items
            logger.debug("--> looking at " + startItem + " out of " + columns.size());
            for (int i = startItem; i < columns.size(); i++) {
                DisplayItemHeaderBean dih = (DisplayItemHeaderBean) items.get(i - startItem);
                ItemBean ib = dih.getItem();
                // YW <<
                String varLabel = (String) itemNames.get(i);
                int dataTypeId = ib.getItemDataTypeId();
                // except int, float, date, all other data types are treated as
                // string
                // int has been set into two groups, one is pure integer which
                // can be used to do calculation
                // another group of int is it's item_data_type has been set as
                // int, but its response_type is among
                // one of those: checkbox, radio, select and selectmulti. So it
                // might be "null values".
                // Here the second kind of item group has been given datatype as
                // String.
                if (dataTypeId == 9) { // date
                    answer.append("\t" + varLabel + " ADATE10\n");
                } else if (dataTypeId == 7) { // float
                    answer.append("\t" + varLabel + " F8.2\n");
                } else if (dataTypeId == 6 && isIntType(ib)) { // pure int data
                    // type for one
                    // item.
                    answer.append("\t" + varLabel + " F8.0\n");
                } else { // string
                    // YW >>
                    int len = getDataColumnMaxLen(i);
                    if (len == 0) {
                        len = 1; // mininum length required by spss
                    }
                    ArrayList metas = ib.getItemMetas();
                    int optionCount = 0;
                    for (int k = 0; k < metas.size(); k++) {
                        ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(k);
                        ResponseSetBean rsb = ifmb.getResponseSet();
                        if (rsb.getResponseType().equals(ResponseType.CHECKBOX) || rsb.getResponseType().equals(ResponseType.RADIO)
                            || rsb.getResponseType().equals(ResponseType.SELECT) || rsb.getResponseType().equals(ResponseType.SELECTMULTI)) {
                            optionCount++;
                        }
                        // tbh >> #5524: all possible response value options should be reviewed so that length is OK
                        ArrayList options = rsb.getOptions();
                        for (int l = 0; l < options.size(); l++) {
                            ResponseOptionBean ro = (ResponseOptionBean) options.get(l);
                            if (ro.getText().length() > len) {
                                len = ro.getText().length();
                            }
                        }
                        // << tbh
                    }
                    if (optionCount == metas.size()) {
                        // all responsetype of metas have options,need to show
                        // value labels
                        if (len > 8) {
                            len = 8;
                        }
                    }
                    answer.append("\t" + varLabel + " A" + len + "\n");
                }
            }
            answer.append(".\n");

            answer.append("VARIABLE LABELS\n");
            // builtin attributes
            for (int i = 0; i < startItem; ++i) {
                String varLabel = (String) itemNames.get(i);
                if ((String) descriptions.get(itemNames.get(i)) != null) {
                    answer.append("\t" + varLabel + " \"" + (String) descriptions.get(itemNames.get(i)) + "\"\n");
                } else {
                    for (int j = 0; j < eb.getStudyGroupClasses().size(); ++j) {
                        answer.append("\t" + varLabel + " \"" + (String) descriptions.get(((StudyGroupClassBean) eb.getStudyGroupClasses().get(j)).getName())
                            + "\"\n");
                    }
                }
            }
            // items
            for (int i = startItem; i < itemNames.size(); i++) {
                DisplayItemHeaderBean dih = (DisplayItemHeaderBean) items.get(i - startItem);
                ItemBean ib = dih.getItem();
                String varLabel = (String) itemNames.get(i);
                answer.append("\t" + varLabel + " \"" + ib.getDescription() + "\"\n");
            }

            answer.append(".\n");

            answer.append("VALUE LABELS\n");

            if (isGender()) {

                answer.append("\t" + "Gender" + "\n");
                answer.append("\t" + "'M'" + " \"" + "Male" + "\"\n");
                answer.append("\t" + "'F'" + " \"" + "Female" + "\"\n\t/\n");

            }

            for (int i = 0; i < items.size(); i++) {
                String temp = "";
                DisplayItemHeaderBean dih = (DisplayItemHeaderBean) items.get(i);
                ItemBean ib = dih.getItem();

                String varLabel = (String) itemNames.get(i + startItem);
                temp += "\t" + varLabel + "\n";
                boolean allOption = true;
                ArrayList metas = ib.getItemMetas();
                HashMap optionMap = new LinkedHashMap();

                for (int k = 0; k < metas.size(); k++) {
                    ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(k);
                    ResponseSetBean rsb = ifmb.getResponseSet();
                    if (rsb.getResponseType().equals(ResponseType.TEXT) || rsb.getResponseType().equals(ResponseType.FILE)
                        || rsb.getResponseType().equals(ResponseType.TEXTAREA)) {
                        // has text response, dont show value labels
                        allOption = false;
                        break;

                    } else {
                        for (int j = 0; j < rsb.getOptions().size(); j++) {
                            ResponseOptionBean ob = (ResponseOptionBean) rsb.getOptions().get(j);

                            String key = ob.getValue();
                            if (optionMap.containsKey(key)) {
                                ArrayList a = (ArrayList) optionMap.get(key);
                                if (!a.contains(ob.getText())) {
                                    a.add(ob.getText());
                                    optionMap.put(key, a);
                                }

                            } else {
                                ArrayList a = new ArrayList();
                                a.add(ob.getText());
                                optionMap.put(key, a);

                            }

                        }

                    }
                }
                Iterator it = optionMap.keySet().iterator();
                while (it.hasNext()) {
                    String value = (String) it.next();
                    ArrayList a = (ArrayList) optionMap.get(value);
                    String texts = "";
                    if (a.size() > 1) {
                        for (int n = 0; n < a.size(); n++) {
                            texts += (String) a.get(n);
                            if (n < a.size() - 1) {
                                texts += "/";
                            }

                        }
                    } else {
                        texts = (String) a.get(0);
                    }
                    if (value.length() > 8) {
                        value = value.substring(0, 8);
                    }
                    if (isValueText(value)) {
                        temp += "\t'" + value + "' \"" + texts + "\"\n";
                    } else {
                        temp += "\t" + value + " \"" + texts + "\"\n";
                    }

                }// end of while (it.hasNext())

                if (allOption) {
                    answer.append(temp + "\t/\n");
                }

            }
        }

        answer.append(".\n EXECUTE.\n");

        return answer;
    }

    // YW << this method has been modified to add variable name validation and
    // set more datatypes
    // and get rid of first line of *spss.sps file
    // YW >>
    public StringBuffer getDataFile() {
        long mytime = System.currentTimeMillis();
        StringBuffer answer = new StringBuffer();

        // YW << use validated variable names which match .sps file
        // get rid of first line of *spss.sps file

        // ArrayList row = (ArrayList) data.get(0);
        // for (int j = 0; j < row.size(); j++) {
        // answer += (String) row.get(j) + "\t";
        // }
        // answer += "\n";

        // YW >>

        ArrayList row = (ArrayList) data.get(1);
        for (int j = 0; j < row.size(); j++) {
            // answer += (String) row.get(j) + "\t";
            answer.append(itemNames.get(j) + "\t");
        }
        answer.append("\n");

        for (int i = 2; i < data.size(); i++) {// if start with row 2, not
            // include header, just row data
            row = (ArrayList) data.get(i);
            for (int j = 0; j < row.size(); j++) {
                String s = ((String)row.get(j)).replaceAll("\\s", " ");
                // >> tbh #5523: we should catch strings of type dd-MMM-yyyy AND yyyy-MM-dd
                String convert = Utils.convertedItemDateValue(s, local_df_string, "MM/dd/yyyy");
                String convertAgain = Utils.convertedItemDateValue(convert, "yyyy-MM-dd", "MM/dd/yyyy");
                // and wait, what about date timestamps?
                String convertAgainAgain = Utils.convertedItemDateValue(convertAgain, local_datetime_string, "MM/dd/yyyy");
                answer.append(convertAgainAgain + "\t");
                 // << tbh
            }
            answer.append("\n");
        }

        return answer;
    }

    // YW
    private boolean isIntType(ItemBean ib) {
        ArrayList metas = ib.getItemMetas();
        for (int k = 0; k < metas.size(); k++) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(k);
            ResponseSetBean rsb = ifmb.getResponseSet();
            if (rsb.getResponseType().equals(ResponseType.TEXT) || rsb.getResponseType().equals(ResponseType.TEXTAREA)) {
                return true;
            }
        }
        return false;
    }

    // YW 10-31-2007
    private String[] createAttributes(ExtractBean eb) {
        SPSSVariableNameValidator svnv = new SPSSVariableNameValidator();
        ArrayList<StudyGroupClassBean> studyGroupClasses = eb.getStudyGroupClasses();
        int size = studyGroupClasses.size();
        String[] atts = new String[size + builtin.length];
        for (int i = 0; i < 7; ++i) {
            atts[i] = builtin[i];
        }
        for (int i = 7; i < 7 + size; ++i) {
            atts[i] = svnv.getValidName(studyGroupClasses.get(i - 7).getName());
        }
        for (int i = 7 + size; i < size + builtin.length; ++i) {
            atts[i] = builtin[i - size];
        }

        return atts;
    }

    // YW 10-31-2007
    private String[] createAttributeTypes(ExtractBean eb) {
        int size = eb.getStudyGroupClasses().size();
        String[] types = new String[size + builtin.length];
        for (int i = 0; i < 7; ++i) {
            types[i] = builtinType[i];
        }
        for (int i = 7; i < 7 + size; ++i) {
            types[i] = "A";
        }
        for (int i = 7 + size; i < size + builtin.length; ++i) {
            types[i] = builtinType[i - size];
        }

        return types;
    }

    private boolean isDataColumnText(int col) {
        for (int i = FIRSTCASE_IND; i < data.size(); i++) {
            String entry = getDataColumnEntry(col, i);
            try {
                float f = Float.parseFloat(entry);
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    private boolean isValueText(String value) {
        try {
            float f = Float.parseFloat(value);
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    private int getDataColumnMaxLen(int col) {
        int max = 0;

        for (int i = FIRSTCASE_IND; i < data.size(); i++) {
            String entry = getDataColumnEntry(col, i);

            if (entry.length() > max) {
                max = entry.length();
            }
        }

        return max;
    }

    // private int builtinIndex(String itemName) {
    // for (int i = 0; i < list.size(); i++) {
    // String attribute = (String) list.get(i);
    private int builtinIndex(String itemName, String[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            logger.debug("itemName[" + itemName + "] attribute[" + attributes[i] + "]");
            if (itemName != null & itemName.startsWith(attributes[i])) {
                return i;
            }
        }
        return -1;
    }

    // private String getDescription(String itemName) {
    // for (int i = 0; i < list.size(); i++) {
    // String attribute = (String) list.get(i);
    private String getDescription(String itemName, String[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            if (itemName != null & itemName.startsWith(attributes[i])) {
                return (String) descriptions.get(attributes[i]);
            }
        }
        return "";
    }

}