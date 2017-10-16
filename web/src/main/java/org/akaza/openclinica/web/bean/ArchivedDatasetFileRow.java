/*
 * Created on Apr 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;

import java.util.ArrayList;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ArchivedDatasetFileRow extends EntityBeanRow {
    // declare columns first
    public static final int COL_FILENAME = 0;
    public static final int COL_FILERUNTIME = 1;
    public static final int COL_FILESIZE = 2;
    public static final int COL_FILECREATEDDATE = 3;
    public static final int COL_FILEOWNER = 4;

    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(ArchivedDatasetFileRow.class)) {
            return 0;
        }

        ArchivedDatasetFileBean thisAccount = (ArchivedDatasetFileBean) bean;
        ArchivedDatasetFileBean argAccount = (ArchivedDatasetFileBean) ((ArchivedDatasetFileRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_FILENAME:
            answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
            break;

        case COL_FILECREATEDDATE:
            answer = thisAccount.getDateCreated().compareTo(argAccount.getDateCreated());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        ArchivedDatasetFileBean thisAccount = (ArchivedDatasetFileBean) bean;
        return thisAccount.getName();
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                ArchivedDatasetFileRow row = new ArchivedDatasetFileRow();
                row.setBean((ArchivedDatasetFileBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return ArchivedDatasetFileRow.generateRowsFromBeans(beans);
    }
}
