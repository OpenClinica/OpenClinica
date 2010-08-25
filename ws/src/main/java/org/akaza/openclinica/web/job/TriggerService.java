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

    

    public String generateSummaryStatsMessage(SummaryStatsBean ssBean, ResourceBundle respage) {
        // TODO i18n
        StringBuffer sb = new StringBuffer();
        sb.append("<table border=\'0\' cellpadding=\'0\' cellspacing=\'0\' width=\'100%\'>");
        sb.append("<tr valign=\'top\'> <td class=\'table_header_row\'>Summary Statistics:</td> </tr> <tr valign=\'top\'>");
        sb.append("<td class=\'table_cell_left\'>Subjects Affected: " + ssBean.getStudySubjectCount() + "</td> </tr>");
        sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'>Event CRFs Affected: " + ssBean.getEventCrfCount() + "</td> </tr> ");
        sb.append("<tr valign=\'top\'><td class=\'table_cell_left\'>Validation Rules Generated: " + ssBean.getDiscNoteCount() + "</td> </tr> </table>");
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
        sb.append("<table border=\'0\' cellpadding=\'0\' cellspacing=\'0\' width=\'100%\'>");
        for (SubjectDataBean subjectDataBean : subjectData) {
            sb.append("<tr valign=\'top\'> <td class=\'table_header_row\' colspan=\'4\'>Study Subject: " + subjectDataBean.getSubjectOID() + "</td> </tr>");
            // next step here
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                sb.append("<tr valign=\'top\'> <td class=\'table_header_row\'>Event CRF OID</td> <td class=\'table_header_row\' colspan=\'3\'></td>");
                sb.append("</tr> <tr valign=\'top\'> <td class=\'table_cell_left\'>");
                sb.append(studyEventDataBean.getStudyEventOID());
                if (studyEventDataBean.getStudyEventRepeatKey() != null) {
                    studyEventRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                    sb.append(" (Repeat key " + studyEventDataBean.getStudyEventRepeatKey() + ")");
                } else {
                    // reset
                    studyEventRepeatKey = "1";
                }
                sb.append("</td> <td class=\'table_cell\' colspan=\'3\'></td> </tr>");
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                for (FormDataBean formDataBean : formDataBeans) {
                    sb.append("<tr valign=\'top\'> <td class=\'table_header_row\'></td> ");
                    sb.append("<td class=\'table_header_row\'>CRF Version OID</td> <td class=\'table_header_row\' colspan=\'2\'></td></tr>");
                    sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'></td> <td class=\'table_cell\'>");
                    sb.append(formDataBean.getFormOID());
                    sb.append("</td> <td class=\'table_cell\' colspan=\'2\'></td> </tr>");
                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                    for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                        sb.append("<tr valign=\'top\'> <td class=\'table_header_row\'></td>");
                        sb.append("<td class=\'table_header_row\'></td> <td class=\'table_header_row\' colspan=\'2\'>");
                        sb.append(itemGroupDataBean.getItemGroupOID());
                        if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
                            groupRepeatKey = itemGroupDataBean.getItemGroupRepeatKey();
                            sb.append(" (Repeat key " + itemGroupDataBean.getItemGroupRepeatKey() + ")");
                        } else {
                            groupRepeatKey = "1";
                        }
                        sb.append("</td></tr>");
                        ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                        for (ImportItemDataBean itemDataBean : itemDataBeans) {
                            String oidKey =
                                itemDataBean.getItemOID() + "_" + studyEventRepeatKey + "_" + groupRepeatKey + "_" + subjectDataBean.getSubjectOID();
                            if (!isValid) {
                                if (hardValidationErrors.containsKey(oidKey)) {
                                    sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'></td>");
                                    sb.append("<td class=\'table_cell\'></td> <td class=\'table_cell\'><font color=\'red\'>");
                                    sb.append(itemDataBean.getItemOID());
                                    sb.append("</font></td> <td class=" + "\'table_cell\'>");
                                    sb.append(itemDataBean.getValue() + "<br/>");
                                    sb.append(hardValidationErrors.get(oidKey));
                                    sb.append("</td></tr>");
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
                                    sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'></td>");
                                    sb.append("<td class=\'table_cell\'></td> <td class=\'table_cell\'>");
                                    sb.append(itemDataBean.getItemOID());
                                    sb.append("</td> <td class=" + "\'table_cell\'>");
                                    sb.append(itemDataBean.getValue());
                                    sb.append("</td></tr>");
                                }
                            }
                        }
                    }
                }
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String generateValidMessage(ArrayList<SubjectDataBean> subjectData, HashMap<String, String> totalValidationErrors) {
        return generateHardValidationErrorMessage(subjectData, totalValidationErrors, true);
    }

    
}
