/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.extract.FilterBean;

import java.util.ArrayList;

/**
 * <p>
 * FilterRow.java, an extension on Shai Sachs' tabling classes, by Tom
 * Hickerson.
 * <p>
 * Keep in mind that we declare the columns here, and the compareColumn and
 * getSearchString functions, together with the ability to generate rows from
 * beans. This is used later in the servlet body when you set up the table to be
 * set to the users' HTTP request.
 *
 * @author thickerson
 *
 */
public class FilterRow extends EntityBeanRow {
    // declare columns first
    public static final int COL_FILTERNAME = 0;
    public static final int COL_FILTERDESC = 1;
    public static final int COL_FILTEROWNER = 2;
    public static final int COL_FILTERCREATEDDATE = 3;
    public static final int COL_STATUS = 4;

    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(FilterRow.class)) {
            return 0;
        }

        FilterBean thisAccount = (FilterBean) bean;
        FilterBean argAccount = (FilterBean) ((FilterRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_FILTERNAME:
            answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
            break;
        case COL_FILTERDESC:
            answer = thisAccount.getDescription().toLowerCase().compareTo(argAccount.getDescription().toLowerCase());
            break;
        case COL_FILTEROWNER:
            answer = thisAccount.getOwner().getName().toLowerCase().compareTo(argAccount.getOwner().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
            break;
        case COL_FILTERCREATEDDATE:
            answer = thisAccount.getCreatedDate().compareTo(argAccount.getCreatedDate());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        FilterBean thisAccount = (FilterBean) bean;
        return thisAccount.getName() + " " + thisAccount.getDescription();
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                FilterRow row = new FilterRow();
                row.setBean((FilterBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return FilterRow.generateRowsFromBeans(beans);
    }
}
