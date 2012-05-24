/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright  2003-2005 Akaza Research
 */

package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.admin.CRFBean;

import java.util.ArrayList;

/**
 * A help class for ListCRF view to display CRF objects in show table class
 *
 * @author ywang
 * @author jxu
 *
 */
public class ListCRFRow extends EntityBeanRow {
    // columns:
    public static final int COL_NAME = 0;

    public static final int COL_DATE_UPDATED = 1;

    public static final int COL_UPDATER = 2;

    public static final int COL_CRF_OID = 3;

    public static final int COL_VERSIONS = 4;

    public static final int COL_VERSION_OID = 5;

    public static final int COL_DATE_CREATED = 6;

    public static final int COL_OWNER = 7;

    public static final int COL_STATUS = 8;

    public static final int COL_DOWNLOAD = 9;

    public static final int COL_ACTIONS = 10;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(ListCRFRow.class)) {
            return 0;
        }

        CRFBean thisCRF = (CRFBean) bean;
        CRFBean argCRF = (CRFBean) ((ListCRFRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisCRF.getName().toLowerCase().compareTo(argCRF.getName().toLowerCase());
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisCRF.getCreatedDate(), argCRF.getCreatedDate());
            break;
        case COL_OWNER:
            answer = thisCRF.getOwner().getName().toLowerCase().compareTo(argCRF.getOwner().getName().toLowerCase());
            break;
        case COL_DATE_UPDATED:
            answer = compareDate(thisCRF.getUpdatedDate(), argCRF.getUpdatedDate());
            break;
        case COL_UPDATER:
            answer = thisCRF.getUpdater().getName().toLowerCase().compareTo(argCRF.getUpdater().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisCRF.getStatus().compareTo(argCRF.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        CRFBean thisCRF = (CRFBean) bean;
        return thisCRF.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return ListCRFRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                ListCRFRow row = new ListCRFRow();
                row.setBean((CRFBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }
}