/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author thickerson
 *
 *
 */
public class DatasetRow extends EntityBeanRow {

    public static final int COL_DATASETNAME = 0;
    public static final int COL_DATASETDESC = 1;
    public static final int COL_DATASETOWNER = 2;
    public static final int COL_DATASETCREATEDDATE = 3;
    public static final int COL_STATUS = 4;
    public SimpleDateFormat sdf = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_format_string"));

    // TODO l10n dates, tbh
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(DatasetRow.class)) {
            return 0;
        }

        DatasetBean thisAccount = (DatasetBean) bean;
        DatasetBean argAccount = (DatasetBean) ((DatasetRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_DATASETNAME:
            answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
            break;
        case COL_DATASETDESC:
            answer = thisAccount.getDescription().toLowerCase().compareTo(argAccount.getDescription().toLowerCase());
            break;
        case COL_DATASETOWNER:
            answer = thisAccount.getOwner().getName().toLowerCase().compareTo(argAccount.getOwner().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
            break;
        case COL_DATASETCREATEDDATE:
            answer = thisAccount.getCreatedDate().compareTo(argAccount.getCreatedDate());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        DatasetBean thisAccount = (DatasetBean) bean;
        return thisAccount.getName() + " " + thisAccount.getDescription() + " " + thisAccount.getOwner().getName() + " "
            + sdf.format(thisAccount.getCreatedDate());
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                DatasetRow row = new DatasetRow();
                row.setBean((DatasetBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    public static ArrayList generateRowFromBean(DatasetBean db) {
        ArrayList answer = new ArrayList();
        DatasetRow row = new DatasetRow();
        row.setBean(db);
        answer.add(row);
        return answer;
    }

    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return DatasetRow.generateRowsFromBeans(beans);
    }
}
