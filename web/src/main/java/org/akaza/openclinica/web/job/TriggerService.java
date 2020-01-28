/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.ImportCRFInfo;
import org.akaza.openclinica.control.submit.ImportCRFInfoContainer;
import org.quartz.SimpleTrigger;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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
    public static final String STUDY_ID = "studyId";

    private static String IMPORT_TRIGGER = "importTrigger";

    public SimpleTrigger generateImportTrigger(FormProcessor fp, UserAccountBean userAccount, StudyBean study, String locale) {
        Date startDateTime = new Date(System.currentTimeMillis());
        return generateImportTrigger(fp, userAccount, study, startDateTime, locale);
    }

    public SimpleTrigger generateImportTrigger(FormProcessor fp, UserAccountBean userAccount, StudyBean study, Date startDateTime, String locale) {

        String jobName = fp.getString(JOB_NAME);
        String email = fp.getString(EMAIL);
        String jobDesc = fp.getString(JOB_DESC);
        String directory = fp.getString(DIRECTORY);

        // what kinds of periods do we have? hourly, daily, weekly?
        long interval = 0;
        int hours = fp.getInt("hours");
        int minutes = fp.getInt("minutes");
        if (hours > 0) {
            long hoursInt = hours * 3600000;
            interval = interval + hoursInt;
        }
        if (minutes > 0) {
            long minutesInt = minutes * 60000;
            interval = interval + minutesInt;
        }

        SimpleTrigger trigger = newTrigger()
            .withIdentity(jobName, IMPORT_TRIGGER)
            .forJob(jobName, IMPORT_TRIGGER)
            .withDescription(jobDesc)
            .startAt(startDateTime)
            .withSchedule(
                simpleSchedule()
                    .withRepeatCount(64000)
                    .withIntervalInSeconds(
                        new Long(interval).intValue()
                    )
                    .withMisfireHandlingInstructionNextWithExistingCount()
            )
            .build();
        
        // set job data map
        trigger.getJobDataMap().put(EMAIL, email);
        trigger.getJobDataMap().put(USER_ID, userAccount.getId());
        trigger.getJobDataMap().put(STUDY_NAME, study.getName());
        trigger.getJobDataMap().put(STUDY_OID, study.getOid());
        trigger.getJobDataMap().put(DIRECTORY, directory);
        trigger.getJobDataMap().put(ExampleSpringJob.LOCALE, locale);
        trigger.getJobDataMap().put("hours", hours);
        trigger.getJobDataMap().put("minutes", minutes);

        return trigger;
    }

    public HashMap validateForm(FormProcessor fp, HttpServletRequest request, String[] triggerNames, String properName) {
        Validator v = new Validator(request);
        v.addValidation(JOB_NAME, Validator.NO_BLANKS);
        // need to be unique too
        v.addValidation(JOB_DESC, Validator.NO_BLANKS);
        v.addValidation(EMAIL, Validator.IS_A_EMAIL);
        v.addValidation(PERIOD, Validator.NO_BLANKS);
        v.addValidation(DATE_START_JOB + "Date", Validator.IS_A_DATE);
        // v.addValidation(DATE_START_JOB + "Date", new Date(), Validator.DATE_IS_AFTER_OR_EQUAL);
        // TODO job names will have to be unique, tbh

        String tab = fp.getString(TAB);
        String cdisc = fp.getString(CDISC);
        String cdisc12 = fp.getString(ExampleSpringJob.CDISC12);
        String cdisc13 = fp.getString(ExampleSpringJob.CDISC13);
        String cdisc13oc = fp.getString(ExampleSpringJob.CDISC13OC);
        String spss = fp.getString(SPSS);
        Date jobDate = fp.getDateTime(DATE_START_JOB);
        HashMap errors = v.validate();
        if ((tab == "") && (cdisc == "") && (spss == "") && (cdisc12 == "") && (cdisc13 == "") && (cdisc13oc == "")) {
            // throw an error here, at least one should work
            // errors.put(TAB, "Error Message - Pick one of the below");
            v.addError(errors, TAB, "Please pick at least one of the below.");
        }
        for (String triggerName : triggerNames) {
            if (triggerName.equals(fp.getString(JOB_NAME)) && (!triggerName.equals(properName))) {
                v.addError(errors, JOB_NAME, "A job with that name already exists.  Please pick another name.");
            }
        }
        if (jobDate.before(new Date())) {
            v.addError(errors, DATE_START_JOB + "Date", "This date needs to be later than the present time.");
        }
        return errors;
    }

    public String generateSummaryStatsMessage(SummaryStatsBean ssBean, ResourceBundle respage) {
        // TODO i18n
        StringBuffer sb = new StringBuffer();
        sb.append("<table border=\'0\' cellpadding=\'0\' cellspacing=\'0\' width=\'100%\'>");
        sb.append("<tr valign=\'top\'> <td class=\'table_header_row\'>Summary Statistics:</td> </tr> <tr valign=\'top\'>");
        sb.append("<td class=\'table_cell_left\'>Subjects Affected: " + ssBean.getStudySubjectCount() + "</td> </tr>");
        sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'>Total Event CRFs: " + ssBean.getEventCrfCount() + "</td> </tr> ");
        sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'>Event CRFs Available for Import: "
                + (ssBean.getEventCrfCount() - ssBean.getSkippedCrfCount()) + "</td> </tr> ");
        sb.append("<tr valign=\'top\'> <td class=\'table_cell_left\'>Event CRFs Skipped: " + ssBean.getSkippedCrfCount() + "</td> </tr> ");
        sb.append("<tr valign=\'top\'><td class=\'table_cell_left\'>Validation Rules Generated: " + ssBean.getDiscNoteCount() + "</td> </tr> </table>");
        /*
         * <table border="0" cellpadding="0" cellspacing="0" width="100%">
         * 
         * <tr valign="top"> <td class="table_header_row">Summary Statistics:</td> </tr> <tr valign="top"> <td
         * class="table_cell_left">Subjects Affected: <c:out value="${summaryStats.studySubjectCount}" /></td> </tr> <tr
         * valign="top"> <td class="table_cell_left">Event CRFs Affected: <c:out value="${summaryStats.eventCrfCount}"
         * /></td> </tr> <tr valign="top"> <td class="table_cell_left">Validation Rules Generated: <c:out
         * value="${summaryStats.discNoteCount}" /></td> </tr>
         * 
         * </table>
         */

        return sb.toString();
    }

    public String generateSkippedCRFMessage(ImportCRFInfoContainer importCRFList, ResourceBundle resword) {
        // TODO i18n
        StringBuffer sb = new StringBuffer();
        sb.append("Skipped CRFs (due to import rules):<br/>");
        sb.append("<table border=\'0\' cellpadding=\'0\' cellspacing=\'0\' width=\'100%\'>");
        sb.append("<tr valign=\'top\'> <td>Study OID :</td> <td>Study Subject OID :</td> <td>Event CRF OID:</td> <td>CRF Version OID:</td> <td>Event CRF Status:</td> </tr>");

        for (ImportCRFInfo importCrfInfo : importCRFList.getImportCRFList()) {
            String preImportStatus = "";

            if (importCrfInfo.getPreImportStage().isInitialDE())
                preImportStatus = resword.getString("initial_data_entry");
            else if (importCrfInfo.getPreImportStage().isInitialDE_Complete())
                preImportStatus = resword.getString("initial_data_entry_complete");
            else if (importCrfInfo.getPreImportStage().isDoubleDE())
                preImportStatus = resword.getString("double_data_entry");
            else if (importCrfInfo.getPreImportStage().isDoubleDE_Complete())
                preImportStatus = resword.getString("data_entry_complete");
            else if (importCrfInfo.getPreImportStage().isAdmin_Editing())
                preImportStatus = resword.getString("administrative_editing");
            else if (importCrfInfo.getPreImportStage().isLocked())
                preImportStatus = resword.getString("locked");
            else
                preImportStatus = resword.getString("invalid");

            if (!importCrfInfo.isProcessImport())
                sb.append("<tr valign=\'top\'> <td>" + importCrfInfo.getStudyOID() + "</td> <td>" + importCrfInfo.getStudySubjectOID() + "</td>" + "<td>"
                        + importCrfInfo.getStudyEventOID() + "</td>" + "<td>" + importCrfInfo.getFormOID() + "</td> <td>" + preImportStatus + "</td></tr>");
        }
        sb.append("</table>");
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
                            String oidKey = itemDataBean.getItemOID() + "_" + studyEventRepeatKey + "_" + groupRepeatKey + "_"
                                    + subjectDataBean.getSubjectOID();
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
                                     * <tr valign="top"> <td class="table_cell_left"></td> <td class="table_cell"></td>
                                     * <td class="table_cell"><font color="red"><c:out
                                     * value="${itemData.itemOID}"/></font></td> <td class="table_cell"> <c:out
                                     * value="${itemData.value}"/><br/> <c:out value="${hardValidationErrors[oidKey]}"/>
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

    public HashMap validateImportJobForm(FormProcessor fp, HttpServletRequest request, String[] triggerNames, String properName) {
        Validator v = new Validator(request);
        v.addValidation(JOB_NAME, Validator.NO_BLANKS);
        v.addValidation(JOB_NAME, Validator.NO_LEADING_OR_TRAILING_SPACES);
        // need to be unique too
        v.addValidation(JOB_DESC, Validator.NO_BLANKS);
        if (!"".equals(fp.getString(EMAIL))) {
            v.addValidation(EMAIL, Validator.IS_A_EMAIL);
        }
        // << tbh we are now allowing email to be optional
        // v.addValidation(PERIOD, Validator.NO_BLANKS);
        // v.addValidation(DIRECTORY, Validator.NO_BLANKS);
        // v.addValidation(DATE_START_JOB + "Date", Validator.IS_A_DATE);

        // TODO job names will have to be unique, tbh

        String hours = fp.getString("hours");
        String minutes = fp.getString("minutes");

        HashMap errors = v.validate();
        if ((hours.equals("0")) && (minutes.equals("0"))) {
            // System.out.println("got in the ERROR LOOP");
            // throw an error here, at least one should be greater than zero
            // errors.put(TAB, "Error Message - Pick one of the below");
            v.addError(errors, "hours", "At least one of the following should be greater than zero.");
        }
        for (String triggerName : triggerNames) {
            if (triggerName.equals(fp.getString(JOB_NAME)) && (!triggerName.equals(properName))) {
                v.addError(errors, JOB_NAME, "A job with that name already exists.  Please pick another name.");
            }
        }
        return errors;
    }

    public HashMap validateImportJobForm(FormProcessor fp, HttpServletRequest request, String[] triggerNames) {
        return validateImportJobForm(fp, request, triggerNames, "");
    }

    public HashMap validateForm(FormProcessor fp, HttpServletRequest request, String[] triggerNames) {
        return validateForm(fp, request, triggerNames, "");
    }

    public HashMap validateImportForm(HttpServletRequest request) {
        Validator v = new Validator(request);

        HashMap errors = v.validate();

        return errors;
    }
    
}
