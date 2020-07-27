package core.org.akaza.openclinica.web.bean;

import java.util.ArrayList;
import java.util.Date;

import core.org.akaza.openclinica.bean.admin.TriggerBean;

/**
 * @author thickerson, dec 2008
 */
public class TriggerRow extends EntityBeanRow {
    // columns:
    public static final int COL_TRIGGER_NAME = 0;
    public static final int COL_LAST_FIRED_DATE = 1;
    public static final int COL_NEXT_FIRED_DATE = 2;
    public static final int COL_DESCRIPTION = 3;
    public static final int COL_PERIOD = 4;
    public static final int COL_DATASET_NAME = 5;
    public static final int COL_JOB_UUID = 6;

    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(TriggerRow.class)) {
            return 0;
        }

        TriggerBean thisTrigger = (TriggerBean) bean;
        TriggerBean argTrigger = (TriggerBean) ((TriggerRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
            case COL_TRIGGER_NAME:
                answer = thisTrigger.getFullName().toLowerCase().compareTo(argTrigger.getFullName().toLowerCase());
                break;
            case COL_LAST_FIRED_DATE:
                boolean thisPrevNull = false;
                boolean argPrevNull = false;
                // if previous date for trigger is null, set it to min date
                if (thisTrigger.getPreviousDate() == null) {
                    thisTrigger.setPreviousDate(new Date(Long.MIN_VALUE));
                    thisPrevNull = true;
                }
                if (argTrigger.getPreviousDate() == null) {
                    argTrigger.setPreviousDate(new Date(Long.MIN_VALUE));
                    argPrevNull = true;
                }
                // do the sorting
                if (thisTrigger.getPreviousDate() != null && argTrigger.getPreviousDate() != null)
                    answer = thisTrigger.getPreviousDate().compareTo(argTrigger.getPreviousDate());

                // set the dates back to null
                if (thisPrevNull)
                    thisTrigger.setPreviousDate(null);
                if (argPrevNull)
                    argTrigger.setPreviousDate(null);
                break;
            case COL_NEXT_FIRED_DATE:
                answer = thisTrigger.getNextDate().compareTo(argTrigger.getNextDate());
                break;
            case COL_DESCRIPTION:
                answer = thisTrigger.getDescription().compareTo(argTrigger.getDescription());
                break;
            case COL_PERIOD:
                answer = thisTrigger.getPeriodToRun().compareTo(argTrigger.getPeriodToRun());
                break;
            case COL_DATASET_NAME:
                answer = thisTrigger.getDatasetName().compareTo(argTrigger.getDatasetName());
                break;
            case COL_JOB_UUID:
                answer = thisTrigger.getJobUuid().compareTo(argTrigger.getJobUuid());
        }

        return answer;
    }

    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSearchString() {
        TriggerBean thisTrigger = (TriggerBean) bean;
        return thisTrigger.getFullName() + " " + thisTrigger.getDescription() + " " + thisTrigger.getPeriodToRun() + " " + thisTrigger.getDatasetName();
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        for (int i = 0; i < beans.size(); i++) {
            try {
                TriggerRow row = new TriggerRow();
                row.setBean((TriggerBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }
}
