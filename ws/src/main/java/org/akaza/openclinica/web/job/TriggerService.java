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

        return sb.toString();
    }

    public String generateHardValidationErrorMessage(ArrayList<SubjectDataBean> subjectData, HashMap<String, String> hardValidationErrors, String groupRepeatKey) {
        StringBuffer sb = new StringBuffer();
        String studyEventRepeatKey = null;
        sb.append("");
        for (SubjectDataBean subjectDataBean : subjectData) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                studyEventRepeatKey = studyEventDataBean.getStudyEventRepeatKey();

                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                for (FormDataBean formDataBean : formDataBeans) {
                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                    for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                        ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                        for (ImportItemDataBean itemDataBean : itemDataBeans) {

                            String oidKey = itemDataBean.getItemOID() + "_" + studyEventRepeatKey + "_" + groupRepeatKey + "_"
                                    + subjectDataBean.getSubjectOID();
                            if (hardValidationErrors.containsKey(oidKey)) {
                                // What about event repeat ordinal and item group and item group repeat ordinal?
                                sb.append(subjectDataBean.getSubjectOID() + "." + studyEventDataBean.getStudyEventOID());
                                if (studyEventDataBean.getStudyEventRepeatKey() != null)
                                    sb.append("(" + studyEventDataBean.getStudyEventRepeatKey() + ")");
                                sb.append("." + formDataBean.getFormOID() + "." + itemGroupDataBean.getItemGroupOID());
                                if (itemGroupDataBean.getItemGroupRepeatKey() != null)
                                    sb.append("(" + itemGroupDataBean.getItemGroupRepeatKey() + ")");
                                sb.append("." + itemDataBean.getItemOID());
                                sb.append(": ");
                                sb.append(itemDataBean.getValue() + " -- ");
                                sb.append(hardValidationErrors.get(oidKey));
                                sb.append("");
                            }

                        }
                    }
                }
            }
        }

        sb.append("");
        return sb.toString();
    }

}
