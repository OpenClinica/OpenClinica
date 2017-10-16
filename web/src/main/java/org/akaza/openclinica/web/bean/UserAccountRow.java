/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.ArrayList;

/**
 * @author ssachs
 */
public class UserAccountRow extends EntityBeanRow {
    // columns:
    public static final int COL_USERNAME = 0;
    public static final int COL_FIRSTNAME = 1;
    public static final int COL_LASTNAME = 2;
    public static final int COL_STATUS = 3;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(UserAccountRow.class)) {
            return 0;
        }

        UserAccountBean thisAccount = (UserAccountBean) bean;
        UserAccountBean argAccount = (UserAccountBean) ((UserAccountRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_USERNAME:
            answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
            break;
        case COL_FIRSTNAME:
            answer = thisAccount.getFirstName().toLowerCase().compareTo(argAccount.getFirstName().toLowerCase());
            break;
        case COL_LASTNAME:
            answer = thisAccount.getLastName().toLowerCase().compareTo(argAccount.getLastName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        UserAccountBean thisAccount = (UserAccountBean) bean;
        return thisAccount.getName() + " " + thisAccount.getFirstName() + " " + thisAccount.getLastName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return UserAccountRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                UserAccountRow row = new UserAccountRow();
                row.setBean((UserAccountBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }
}