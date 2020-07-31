package core.org.akaza.openclinica.web.bean;

import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;

import java.util.ArrayList;

/**
 * @author thickerson
 */
public class ArchivedDatasetFileRow extends EntityBeanRow {

    public static final int COL_DATASETFORMAT = 0;
    public static final int COL_FILENAME = 1;
    public static final int COL_FILERUNTIME = 2;
    public static final int COL_FILESIZE = 3;
    public static final int COL_FILECREATEDDATE = 4;
    public static final int COL_FILEOWNER = 5;
    public static final int COL_STATUS = 6;
    public static final int COL_JOB_TYPE = 7;
    public static final int COL_ACTION = 8;

    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(ArchivedDatasetFileRow.class)) {
            return 0;
        }

        ArchivedDatasetFileBean thisAccount = (ArchivedDatasetFileBean) bean;
        ArchivedDatasetFileBean argAccount = (ArchivedDatasetFileBean) ((ArchivedDatasetFileRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
            case COL_DATASETFORMAT:
                answer = thisAccount.getFormat().toLowerCase().compareTo(argAccount.getFormat().toLowerCase());
                break;
            case COL_FILENAME:
                answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
                break;
            case COL_FILERUNTIME:
                answer = Double.compare(thisAccount.getRunTime(), argAccount.getRunTime());
                break;
            case COL_FILESIZE:
                answer = Integer.compare(thisAccount.getFileSize(), argAccount.getFileSize());
                break;
            case COL_FILECREATEDDATE:
                answer = thisAccount.getDateCreated().compareTo(argAccount.getDateCreated());
                break;
            case COL_FILEOWNER:
                answer = thisAccount.getOwner().getName().toLowerCase().compareTo(argAccount.getOwner().getName().toLowerCase());
                break;
            case COL_STATUS:
                answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
                break;

        }

        return answer;
    }

    @Override
    public String getSearchString() {
        ArchivedDatasetFileBean thisAccount = (ArchivedDatasetFileBean) bean;
        return thisAccount.getFormat() + " " + thisAccount.getName() + " " +
                thisAccount.getDateCreated() + " " + thisAccount.getOwner() + " " +
                thisAccount.getStatus() + " " + thisAccount.getJobType();
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

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
        return null;
    }
}
