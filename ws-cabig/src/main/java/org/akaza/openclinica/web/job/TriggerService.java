package org.akaza.openclinica.web.job;


import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;


public class TriggerService {

    public TriggerService() {
        // do nothing, for the moment
    }

    public static final String PERIOD = "periodToRun";
    public static final String TAB = "tab";
    public static final String CDISC = "cdisc";
    public static final String SPSS = "spss";
    public static final String DATASET_ID = "dsId";
    public static final String DATE_START_JOB = "job";
    public static final String EMAIL = "contactEmail";
    public static final String JOB_NAME = "jobName";
    public static final String JOB_DESC = "jobDesc";
    public static final String USER_ID = "user_id";
    public static final String STUDY_NAME = "study_name";
    public static final String STUDY_OID = "study_oid";
    public static final String DIRECTORY = "filePathDir";

    private static String IMPORT_TRIGGER = "importTrigger";

    

    public String generateSummaryStatsMessage(SummaryStatsBean ssBean, ResourceBundle respage, HashMap<String, String> validationMsgs) {
        // TODO i18n
        StringBuffer sb = new StringBuffer();
        sb.append("");
        sb.append("Summary Statistics: ");
        sb.append("Subjects Affected: " + ssBean.getStudySubjectCount() + ", ");
        sb.append("Event CRFs Affected: " + ssBean.getEventCrfCount() + ", ");
        sb.append("# of Warnings: " + validationMsgs.size() + ", ");
        sb.append("# of Discrepancy Notes: " + ssBean.getDiscNoteCount() + ". ");
        /*
         * <table border="0" cellpadding="0" cellspacing="0" width="100%">
         * 
         * <tr valign="top"> <td class="table_header_row">Summary
         * Statistics:</td> </tr> <tr valign="top"> <td
         * class="table_cell_left">Subjects Affected: <c:out
         * value="${summaryStats.studySubjectCount}" /></td> </tr> <tr
         * valign="top"> <td class="table_cell_left">Event CRFs Affected: <c:out
         * value="${summaryStats.eventCrfCount}" /></td> </tr> <tr valign="top">
         * <td class="table_cell_left">Validation Rules Generated: <c:out
         * value="${summaryStats.discNoteCount}" /></td> </tr>
         * 
         * </table>
         */

        return sb.toString();
    }

    public String generateHardValidationErrorMessage(ArrayList<SubjectDataBean> subjectData, HashMap<String, String> hardValidationErrors, boolean isValid) {
        StringBuffer sb = new StringBuffer();
        String studyEventRepeatKey = "1";
        String groupRepeatKey = "1";
        sb.append("");
        for (SubjectDataBean subjectDataBean : subjectData) {
            // sb.append("Study Subject: " + subjectDataBean.getSubjectOID() + "  ");
            // next step here
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                //                sb.append("Event CRF OID: ");
                //                sb.append("");
                //                sb.append(studyEventDataBean.getStudyEventOID());
                //                if (studyEventDataBean.getStudyEventRepeatKey() != null) {
                //                    studyEventRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                //                    sb.append(" (Repeat key " + studyEventDataBean.getStudyEventRepeatKey() + ")");
                //                } else {
                //                    // reset
                //                    studyEventRepeatKey = "1";
                //                }
                //                sb.append("");
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                for (FormDataBean formDataBean : formDataBeans) {
                    //                    sb.append(" ");
                    //                    sb.append("CRF Version OID: ");
                    //                    sb.append("");
                    //                    sb.append(formDataBean.getFormOID());
                    //                    sb.append("");
                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                    for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                        //                        sb.append("");
                        //                        sb.append(" ");
                        //                        sb.append(itemGroupDataBean.getItemGroupOID());
                        //                        if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
                        //                            groupRepeatKey = itemGroupDataBean.getItemGroupRepeatKey();
                        //                            sb.append(" (Repeat key " + itemGroupDataBean.getItemGroupRepeatKey() + ")");
                        //                        } else {
                        //                            groupRepeatKey = "1";
                        //                        }
                        //                        sb.append(" ");
                        ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                        for (ImportItemDataBean itemDataBean : itemDataBeans) {
                            String oidKey =
                                itemDataBean.getItemOID() + "_" + studyEventRepeatKey + "_" + groupRepeatKey + "_" + subjectDataBean.getSubjectOID();
                            if (!isValid) {
                                if (hardValidationErrors.containsKey(oidKey)) {
                                    //                                    sb.append("");
                                    //                                    sb.append("");
                                    sb.append(itemDataBean.getItemOID());
                                    sb.append(": ");
                                    sb.append(itemDataBean.getValue() + " -- ");
                                    sb.append(hardValidationErrors.get(oidKey));
                                    sb.append("");
                                    /*
                                     * <tr valign="top"> <td
                                     * class="table_cell_left"></td> <td
                                     * class="table_cell"></td> <td
                                     * class="table_cell"><font
                                     * color="red"><c:out
                                     * value="${itemData.itemOID}"/></font></td>
                                     * <td class="table_cell"> <c:out
                                     * value="${itemData.value}"/><br/> <c:out
                                     * value="${hardValidationErrors[oidKey]}"/>
                                     * </td> </tr>
                                     */
                                }
                            } else {
                                if (!hardValidationErrors.containsKey(oidKey)) {
                                    //                                    sb.append("");
                                    //                                    sb.append("");
                                    //                                    sb.append(itemDataBean.getItemOID());
                                    //                                    sb.append(": ");
                                    //                                    sb.append(itemDataBean.getValue());
                                    //                                    sb.append(" -- ");
                                }
                            }
                        }
                    }
                }
            }
        }
        sb.append("");
        return sb.toString();
    }

    public String generateValidMessage(ArrayList<SubjectDataBean> subjectData, HashMap<String, String> totalValidationErrors) {
        return generateHardValidationErrorMessage(subjectData, totalValidationErrors, true);
    }

    
}
