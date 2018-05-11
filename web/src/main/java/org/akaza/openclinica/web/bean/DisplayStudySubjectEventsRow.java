package org.akaza.openclinica.web.bean;

import java.util.ArrayList;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;

/**
 * @author sshamim
 *         Date: Nov 17, 2008
 *         Time: 7:57:23 PM
 */
public class DisplayStudySubjectEventsRow extends EntityBeanRow {

    // columns:
    public static final int COL_SUBJECT_LABEL = 0;
    public static final int COL_STATUS = 1;
    public static final int COL_GENDER = 2;
    public static final int COL_EVENT_STATUS = 3;
    public static final int COL_EVENT_DATE = 4;
    public static final int COL_STUDYCRF = 5;
    public static final int COL_STUDYGROUP = 6;

    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(DisplayStudySubjectEventsRow.class)) {
            return 0;
        }

        DisplayStudySubjectBean thisStudy = (DisplayStudySubjectBean) bean;
        DisplayStudySubjectBean argStudy = (DisplayStudySubjectBean) ((DisplayStudySubjectEventsRow) row).bean;
        int answer = 0;
        // YW <<
        int groupSize = thisStudy.getStudyGroups().size();
        int code;
        if (sortingColumn > 4 + groupSize) {
            code = COL_STUDYCRF;
        } else if (sortingColumn > 4 && sortingColumn <= 4 + groupSize) {
            code = COL_STUDYGROUP;
        } else {
            code = sortingColumn;
        }
        switch (code) {
        case COL_SUBJECT_LABEL:
            answer = thisStudy.getStudySubject().getLabel().toLowerCase().compareTo(argStudy.getStudySubject().getLabel().toLowerCase());
            break;
        case COL_GENDER:
            answer = (thisStudy.getStudySubject().getGender() + "").compareTo(argStudy.getStudySubject().getGender() + "");
            break;
        case COL_STATUS:
            answer = thisStudy.getStudySubject().getStatus().compareTo(argStudy.getStudySubject().getStatus());
            break;
        case COL_EVENT_STATUS:
            if (thisStudy.getStudyEvents() == null || thisStudy.getStudyEvents().isEmpty()) {
                answer = -1;
                break;
            }
            if (argStudy.getStudyEvents() == null || argStudy.getStudyEvents().isEmpty()) {
                answer = 1;
                break;
            }
            answer = ((DisplayStudyEventBean) thisStudy.getStudyEvents().get(0)).getStudyEvent().getSubjectEventStatus()
                    .compareTo(((DisplayStudyEventBean) argStudy.getStudyEvents().get(0)).getStudyEvent().getSubjectEventStatus());
            break;
        case COL_EVENT_DATE:
            if (thisStudy.getStudyEvents() == null || thisStudy.getStudyEvents().isEmpty()) {
                answer = -1;
                break;
            }
            if (argStudy.getStudyEvents() == null || argStudy.getStudyEvents().isEmpty()) {
                answer = 1;
                break;
            }
            if (((DisplayStudyEventBean) thisStudy.getStudyEvents().get(0)).getStudyEvent().getDateStarted() != null
                    && ((DisplayStudyEventBean) argStudy.getStudyEvents().get(0)).getStudyEvent().getDateStarted() != null)
                answer = ((DisplayStudyEventBean) thisStudy.getStudyEvents().get(0)).getStudyEvent().getDateStarted()
                        .compareTo(((DisplayStudyEventBean) argStudy.getStudyEvents().get(0)).getStudyEvent().getDateStarted());
            break;

        case COL_STUDYGROUP:
            answer = ((SubjectGroupMapBean) thisStudy.getStudyGroups().get(sortingColumn - 5)).getStudyGroupName().toLowerCase()
                    .compareTo(((SubjectGroupMapBean) argStudy.getStudyGroups().get(sortingColumn - 5)).getStudyGroupName().toLowerCase());
            break;
        case COL_STUDYCRF:
            if (thisStudy.getStudyEvents() == null || thisStudy.getStudyEvents().isEmpty()) {
                answer = -1;
                break;
            }
            if (argStudy.getStudyEvents() == null || argStudy.getStudyEvents().isEmpty()) {
                answer = 1;
                break;
            }
            ArrayList thisAllEventCRFs = ((DisplayStudyEventBean) thisStudy.getStudyEvents().get(0)).getAllEventCRFs();
            ArrayList argAllEventCRFs = ((DisplayStudyEventBean) argStudy.getStudyEvents().get(0)).getAllEventCRFs();

            if (thisAllEventCRFs == null || thisAllEventCRFs.isEmpty()) {
                answer = -1;
                break;
            }
            if (argAllEventCRFs == null || argAllEventCRFs.isEmpty()) {
                answer = 1;
                break;
            }

            // Event crf status comparision
            DataEntryStage thisDes = ((DisplayEventCRFBean) thisAllEventCRFs.get(sortingColumn - 5)).getEventCRF().getStage();
            DataEntryStage argDes = ((DisplayEventCRFBean) argAllEventCRFs.get(sortingColumn - 5)).getEventCRF().getStage();
            /*
             * Event crf status is ordered in this sequence, Not Started=1,Data Entry Started=2,Initial Data Entry
             * Completed=3,
             * Double Data Entry Started=4,Double Data Entry Complete=5,Complete=6,Locked=7,Invalid/Removed=0
             */
            switch (thisDes.getId()) {
            // DataEntryStage.INVALID
            case 0:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else {
                    answer = 1;
                }
                break;
            // DataEntryStage.UNCOMPLETED
            case 1:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else {
                    answer = -1;
                }
                break;
            // DataEntryStage.INITIAL_DATA_ENTRY
            case 2:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else if (argDes.getId() == 1) {
                    answer = 1;
                } else {
                    answer = -1;
                }
                break;
            // DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE
            case 3:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else if (argDes.getId() == 1 || argDes.getId() == 2) {
                    answer = 1;
                } else {
                    answer = -1;
                }
                break;
            // DataEntryStage.DOUBLE_DATA_ENTRY
            case 4:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else if (argDes.getId() == 1 || argDes.getId() == 2 || argDes.getId() == 3) {
                    answer = 1;
                } else {
                    answer = -1;
                }
                break;
            // DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE
            case 5:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else if (argDes.getId() == 0 || argDes.getId() == 7) {
                    answer = -1;
                } else {
                    answer = 1;
                }
                break;
            // DataEntryStage.ADMINISTRATIVE_EDITING
            case 6:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else if (argDes.getId() == 7 || argDes.getId() == 0) {
                    answer = -1;
                } else {
                    answer = 1;
                }
                break;
            // DataEntryStage.LOCKED
            case 7:
                if (thisDes.getId() == argDes.getId()) {
                    answer = 0;
                } else if (argDes.getId() == 0) {
                    answer = -1;
                } else {
                    answer = 1;
                }
                break;
            default:
                answer = 1;
            }
        }
        return answer;
    }

    @Override
    public String getSearchString() {
        DisplayStudySubjectBean thisStudy = (DisplayStudySubjectBean) bean;
        String searchString = thisStudy.getStudySubject().getLabel();
        String secondaryLabel = thisStudy.getStudySubject().getSecondaryLabel();
        if (!"".equalsIgnoreCase(secondaryLabel)) {
            searchString += " ";
            searchString += secondaryLabel;
        }
        /*
         * String toStr = "";
         * Date enrDate = thisStudy.getStudySubject().getEnrollmentDate();
         * if (enrDate != null) {
         * SimpleDateFormat sdf = new
         * SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_format_string"));
         * toStr = sdf.format(enrDate);
         * // TODO l10n dates?
         * }
         */
        // BWP>>8/6/2008
        /*
         * return thisStudy.getStudySubject().getLabel() + " " + thisStudy.getStudySubject().getSecondaryLabel() +
         * " " + thisStudy.getStudySubject().getGender() + " " + toStr;
         */

        return searchString;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return DisplayStudySubjectEventsRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                DisplayStudySubjectEventsRow row = new DisplayStudySubjectEventsRow();
                row.setBean((DisplayStudySubjectBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
