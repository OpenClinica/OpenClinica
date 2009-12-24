/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class StudyUserRoleRow extends EntityBeanRow {
    // columns:
    public static final int COL_USERNAME = 0;
    public static final int COL_FIRSTNAME = 1;
    public static final int COL_LASTNAME = 2;
    public static final int COL_ROLE = 3;
    public static final int COL_STUDYNAME = 4;
    public static final int COL_STATUS = 5;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(StudyUserRoleRow.class)) {
            return 0;
        }

        StudyUserRoleBean thisAccount = (StudyUserRoleBean) bean;
        StudyUserRoleBean argAccount = (StudyUserRoleBean) ((StudyUserRoleRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_USERNAME:
            answer = thisAccount.getUserName().toLowerCase().compareTo(argAccount.getUserName().toLowerCase());
            break;
        case COL_FIRSTNAME:
            answer = thisAccount.getFirstName().toLowerCase().compareTo(argAccount.getFirstName().toLowerCase());
            break;
        case COL_LASTNAME:
            answer = thisAccount.getLastName().toLowerCase().compareTo(argAccount.getLastName().toLowerCase());
            break;
        case COL_ROLE:
            answer = thisAccount.getRoleName().toLowerCase().compareTo(argAccount.getRoleName().toLowerCase());
            break;
        case COL_STUDYNAME:
            answer = thisAccount.getStudyName().toLowerCase().compareTo(argAccount.getStudyName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyUserRoleBean thisAccount = (StudyUserRoleBean) bean;
        return thisAccount.getUserName() + " " + thisAccount.getFirstName() + " " + thisAccount.getLastName() + " " + thisAccount.getRoleName() + " "
            + thisAccount.getStudyName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return StudyUserRoleRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyUserRoleRow row = new StudyUserRoleRow();
                row.setBean((StudyUserRoleBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
