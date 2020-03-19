/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.web.bean;

import java.util.ArrayList;

import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;

/**
 * @author jxu
 *
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class StudyEventRow extends EntityBeanRow {
    // columns:

    public static final int COL_STUDY_SUBJECT_LABEL = 0;

    public static final int COL_START_DATE = 1;

    public static final int COL_SUBJECT_EVENT_STATUS = 2;

    /*
     * (non-Javadoc)
     *
     * @see core.org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     * int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(StudyEventRow.class)) {
            return 0;
        }

        StudyEventBean thisEvent = (StudyEventBean) bean;
        StudyEventBean argEvent = (StudyEventBean) ((StudyEventRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {

        case COL_STUDY_SUBJECT_LABEL:
            answer = thisEvent.getStudySubjectLabel().toLowerCase().compareTo(argEvent.getStudySubjectLabel().toLowerCase());
            break;

        case COL_START_DATE:
            if (thisEvent.getDateStarted() != null && argEvent.getDateStarted() != null)
                answer = compareDate(thisEvent.getDateStarted(), argEvent.getDateStarted());
            break;

        case COL_SUBJECT_EVENT_STATUS:
            answer = thisEvent.getWorkflowStatus().compareTo(argEvent.getWorkflowStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyEventBean thisEvent = (StudyEventBean) bean;
        return thisEvent.getStudySubjectLabel() + " " + thisEvent.getWorkflowStatus();
    }

    /*
     * (non-Javadoc)
     *
     * @see core.org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return StudyEventRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyEventRow row = new StudyEventRow();
                row.setBean((StudyEventBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
