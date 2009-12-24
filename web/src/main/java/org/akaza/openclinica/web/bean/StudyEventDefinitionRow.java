/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;

import java.util.ArrayList;

/**
 * @author jxu
 *
 */
public class StudyEventDefinitionRow extends EntityBeanRow {
    // columns:
    // YW << Currently, for URL .../ListEventDefinition, the following collumn
    // match is wrong
    // and not all of them are used.
    // Change has been made and no trouble has been found to couple this this
    // change.
    // YW >>
    public static final int COL_ORDINAL = 0;

    public static final int COL_NAME = 1;
    
    public static final int COL_OID = 2;

    public static final int COL_REPEATING = 3;

    public static final int COL_TYPE = 4;

    public static final int COL_CATEGORY = 5;// was 4, tbh 09/2009

    public static final int COL_POPULATED = 6;

    public static final int COL_DATE_CREATED = 7;// 6; -- not been used?

    public static final int COL_OWNER = 7; // -- not been used?

    public static final int COL_DATE_UPDATED = 8;// 8;

    public static final int COL_UPDATER = 9; // -- not been used?

    public static final int COL_STATUS = 10; // -- not been used?
    
    public static final int COL_DEFAULT_VERSION = 11;
    
    public static final int COL_ACTIONS = 12;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(StudyEventDefinitionRow.class)) {
            return 0;
        }

        StudyEventDefinitionBean thisDefinition = (StudyEventDefinitionBean) bean;
        StudyEventDefinitionBean argDefinition = (StudyEventDefinitionBean) ((StudyEventDefinitionRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_ORDINAL:
            if (thisDefinition.getOrdinal() > argDefinition.getOrdinal()) {
                answer = 1;
            } else if (thisDefinition.getOrdinal() == argDefinition.getOrdinal()) {
                answer = 0;
            } else {
                answer = -1;
            }
            break;
        case COL_NAME:
            answer = thisDefinition.getName().toLowerCase().compareTo(argDefinition.getName().toLowerCase());
            break;
        case COL_REPEATING:
            if (thisDefinition.isRepeating() && !argDefinition.isRepeating()) {
                answer = 1;
            } else if (!thisDefinition.isRepeating() && argDefinition.isRepeating()) {
                answer = -1;
            } else {
                answer = 0;
            }
            break;
        case COL_TYPE:
            answer = thisDefinition.getType().toLowerCase().compareTo(argDefinition.getType().toLowerCase());
            break;
        case COL_CATEGORY:
        	String category = "ZZZZZZZZZ";
        	if (!"".equals(thisDefinition.getCategory())) {
        		category = thisDefinition.getCategory();
        		// System.out.println("switched out ...");
        	}
        	
            answer = category.toLowerCase().compareTo(argDefinition.getCategory().toLowerCase());
            // System.out.println("returning " + answer + " for " + category);
            break;
        case COL_POPULATED:
            if (thisDefinition.isPopulated() && !argDefinition.isPopulated()) {
                answer = 1;
            } else if (!thisDefinition.isPopulated() && argDefinition.isPopulated()) {
                answer = -1;
            } else {
                answer = 0;
            }
            break;
//        case COL_DATE_CREATED:
//            answer = compareDate(thisDefinition.getCreatedDate(), argDefinition.getCreatedDate());
//            break;
//        case COL_OWNER:
//            answer = thisDefinition.getOwner().getName().toLowerCase().compareTo(argDefinition.getOwner().getName().toLowerCase());
//            break;
        case COL_DATE_UPDATED:
            answer = compareDate(thisDefinition.getUpdatedDate(), argDefinition.getUpdatedDate());
            break;
        case COL_UPDATER:
            answer = thisDefinition.getUpdater().getName().toLowerCase().compareTo(argDefinition.getUpdater().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisDefinition.getStatus().compareTo(argDefinition.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyEventDefinitionBean thisDefinition = (StudyEventDefinitionBean) bean;
        return thisDefinition.getName() + " " + thisDefinition.getType() + " " + thisDefinition.getCategory() + " " + thisDefinition.getOwner().getName() + " "
            + thisDefinition.getUpdater().getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return StudyEventDefinitionRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyEventDefinitionRow row = new StudyEventDefinitionRow();
                row.setBean((StudyEventDefinitionBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}