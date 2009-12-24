/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.admin.DisplayStudyBean;

import java.util.ArrayList;

/**
 * A class for displaying study object in show table class
 * 
 * @author Jun Xu
 */
public class DisplayStudyRow extends EntityBeanRow {
    // columns:
    public static final int COL_NAME = 0;

    public static final int COL_UNIQUEIDENTIFIER = 1;

    public static final int COL_OID = 2;

    public static final int COL_PRINCIPAL_INVESTIGATOR = 3;

    public static final int COL_FACILITY_NAME = 4;

    public static final int COL_DATE_CREATED = 5;

    public static final int COL_STATUS = 6;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     * int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(DisplayStudyRow.class)) {
            return 0;
        }

        DisplayStudyBean thisStudy = (DisplayStudyBean) bean;
        DisplayStudyBean argStudy = (DisplayStudyBean) ((DisplayStudyRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisStudy.getParent().getName().toLowerCase().compareTo(argStudy.getParent().getName().toLowerCase());
            break;
        case COL_OID:
            answer = thisStudy.getParent().getOid().toLowerCase().compareTo(argStudy.getParent().getOid().toLowerCase());
            break;
        case COL_UNIQUEIDENTIFIER:
            answer = thisStudy.getParent().getIdentifier().toLowerCase().compareTo(argStudy.getParent().getIdentifier().toLowerCase());
            break;
        case COL_PRINCIPAL_INVESTIGATOR:
            answer = thisStudy.getParent().getPrincipalInvestigator().toLowerCase().compareTo(argStudy.getParent().getPrincipalInvestigator().toLowerCase());
            break;
        case COL_FACILITY_NAME:
            answer = thisStudy.getParent().getFacilityName().toLowerCase().compareTo(argStudy.getParent().getFacilityName().toLowerCase());
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisStudy.getParent().getCreatedDate(), argStudy.getParent().getCreatedDate());
            break;
        case COL_STATUS:
            answer = thisStudy.getParent().getStatus().compareTo(argStudy.getParent().getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        DisplayStudyBean thisStudy = (DisplayStudyBean) bean;
        return thisStudy.getParent().getName() + " " + thisStudy.getParent().getIdentifier() + " " + thisStudy.getParent().getPrincipalInvestigator() + " "
            + thisStudy.getParent().getFacilityName() + " " + thisStudy.getParent().getOid() + " " + thisStudy.getParent().getKeywords().replace(',', ' ')
            + " " + thisStudy.getParent().getSummary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util
     * .ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return DisplayStudyRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                DisplayStudyRow row = new DisplayStudyRow();
                row.setBean((DisplayStudyBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}