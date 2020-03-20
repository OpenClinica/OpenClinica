/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.web.bean;

import core.org.akaza.openclinica.bean.managestudy.DisplayStudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DisplayStudySubjectRow extends EntityBeanRow {

    // columns:
    // YW << the order of columns has been changed to couple with modified view
    public static final int COL_SUBJECT_LABEL = 0;
    public static final int COL_STATUS = 1;
    public static final int COL_OID = 2;  
    

    public static final int COL_GENDER = 3; 
    
    public static final int COL_SECONDARY_LABEL = 4; 
   

    public static final int COL_STUDYGROUP = 5;

    public static final int COL_STUDYEVENT = 6;

    // YW >>

    /*
     * (non-Javadoc)
     *
     * @see core.org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(DisplayStudySubjectRow.class)) {
            return 0;
        }

        DisplayStudySubjectBean thisStudy = (DisplayStudySubjectBean) bean;
        DisplayStudySubjectBean argStudy = (DisplayStudySubjectBean) ((DisplayStudySubjectRow) row).bean;
        int answer = 0;
        // YW <<
        int groupSize = thisStudy.getStudyGroups().size();
        int code;
        if (sortingColumn > 4 + groupSize) {
            if (thisStudy.getSedId() <= 0) {
                code = COL_STUDYEVENT;
            } else {
                code = -1;
            }
        } else if (sortingColumn > 4 && sortingColumn <= 4 + groupSize) {
            code = COL_STUDYGROUP;
        } else {
            code = sortingColumn;
        }
        switch (code) {
        // switch (sortingColumn) {
        // YW >>

        // case COL_UNIQUEIDENTIFIER:
        // answer =
        // thisStudy.getStudySubject().getUniqueIdentifier().toLowerCase().compareTo(
        // argStudy.getStudySubject().getUniqueIdentifier().toLowerCase());
        // break;
        case COL_SUBJECT_LABEL:
            answer = thisStudy.getStudySubject().getLabel().toLowerCase().compareTo(argStudy.getStudySubject().getLabel().toLowerCase());
            break;
        case COL_GENDER:
            answer = (thisStudy.getStudySubject().getGender() + "").compareTo(argStudy.getStudySubject().getGender() + "");
            break;
        case COL_OID:
            answer = thisStudy.getStudySubject().getOid().toLowerCase().compareTo(argStudy.getStudySubject().getOid().toLowerCase());
            break;
        case COL_SECONDARY_LABEL:
            answer = thisStudy.getStudySubject().getSecondaryLabel().toLowerCase().compareTo(argStudy.getStudySubject().getSecondaryLabel().toLowerCase());
            break;
        // case COL_STUDY_NAME:
        // answer =
        // thisStudy.getStudySubject().getStudyName().toLowerCase().compareTo(argStudy.getStudySubject().getStudyName().toLowerCase());
        // break;
        /*
         * case COL_ENROLLMENTDATE: answer =
         * compareDate(thisStudy.getStudySubject().getEnrollmentDate(),argStudy.getStudySubject().getEnrollmentDate());
         * break;
         */
        case COL_STATUS:
            answer = thisStudy.getStudySubject().getStatus().compareTo(argStudy.getStudySubject().getStatus());
            break;

        // YW <<
        case COL_STUDYGROUP:
            answer =
                ((SubjectGroupMapBean) thisStudy.getStudyGroups().get(sortingColumn - 5)).getStudyGroupName().toLowerCase().compareTo(
                        ((SubjectGroupMapBean) argStudy.getStudyGroups().get(sortingColumn - 5)).getStudyGroupName().toLowerCase());
            break;
        case COL_STUDYEVENT:
            // studyEvent status comparision
            StudyEventWorkflowStatusEnum thisSes = ((StudyEventBean) thisStudy.getStudyEvents().get(sortingColumn - 5 - groupSize)).getWorkflowStatus();
            StudyEventWorkflowStatusEnum argSes = ((StudyEventBean) argStudy.getStudyEvents().get(sortingColumn - 5 - groupSize)).getWorkflowStatus();

        }
        return answer;
    }

    @Override
    public String getSearchString() {
        DisplayStudySubjectBean thisStudy = (DisplayStudySubjectBean) bean;
        String searchString = thisStudy.getStudySubject().getLabel();
        String secondaryLabel = thisStudy.getStudySubject().getSecondaryLabel();
        if(! "".equalsIgnoreCase(secondaryLabel)){
           searchString += " ";
           searchString += secondaryLabel;
        }
        /*String toStr = "";
        Date enrDate = thisStudy.getStudySubject().getEnrollmentDate();
        if (enrDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_format_string"));
            toStr = sdf.format(enrDate);
            // TODO l10n dates?
        }*/
        //BWP>>8/6/2008
        /*return thisStudy.getStudySubject().getLabel() + " "  + thisStudy.getStudySubject().getSecondaryLabel() +
        " " + thisStudy.getStudySubject().getGender() + " " + toStr;*/

        return  searchString;
    }

    /*
     * (non-Javadoc)
     *
     * @see core.org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return DisplayStudySubjectRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                DisplayStudySubjectRow row = new DisplayStudySubjectRow();
                row.setBean((DisplayStudySubjectBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    private int compare(int thisStatus, int argStatus) {
        int result;
        if (thisStatus < argStatus) {
            result = -1;
        } else if (thisStatus == argStatus) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }

}