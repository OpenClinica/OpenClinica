/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.managestudy.StudyBean;

import java.util.ArrayList;

/**
 * @author jxu
 *
 */
public class StudyRow extends EntityBeanRow {

    // columns:
    public static final int COL_NAME = 0;
    public static final int COL_UNIQUEIDENTIFIER = 1;
    public static final int COL_PRINCIPAL_INVESTIGATOR = 2;
    public static final int COL_FACILITY_NAME = 3;
    public static final int COL_DATE_CREATED = 4;
    public static final int COL_STATUS = 5;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(StudyRow.class)) {
            return 0;
        }

        StudyBean thisStudy = (StudyBean) bean;
        StudyBean argStudy = (StudyBean) ((StudyRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisStudy.getName().toLowerCase().compareTo(argStudy.getName().toLowerCase());
            break;
        case COL_UNIQUEIDENTIFIER:
            answer = thisStudy.getIdentifier().toLowerCase().compareTo(argStudy.getIdentifier().toLowerCase());
            break;
        case COL_PRINCIPAL_INVESTIGATOR:
            answer = thisStudy.getPrincipalInvestigator().toLowerCase().compareTo(argStudy.getPrincipalInvestigator().toLowerCase());
            break;
        case COL_FACILITY_NAME:
            answer = thisStudy.getFacilityName().toLowerCase().compareTo(argStudy.getFacilityName().toLowerCase());
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisStudy.getCreatedDate(), argStudy.getCreatedDate());
            break;
        case COL_STATUS:
            answer = thisStudy.getStatus().compareTo(argStudy.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyBean thisStudy = (StudyBean) bean;
        return thisStudy.getName() + " " + thisStudy.getIdentifier() + " " + thisStudy.getPrincipalInvestigator() + " " + thisStudy.getFacilityName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return StudyRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyRow row = new StudyRow();
                row.setBean((StudyBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
