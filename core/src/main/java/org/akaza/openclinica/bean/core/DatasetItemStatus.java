package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasetItemStatus extends Term {
    public static final DatasetItemStatus COMPLETED = new DatasetItemStatus(1, "completed", "completed_items");
    public static final DatasetItemStatus NONCOMPLETED = new DatasetItemStatus(2, "non_completed", "non_completed_items");
    public static final DatasetItemStatus COMPLETED_AND_NONCOMPLETED =
        new DatasetItemStatus(3, "completed_and_non_completed", "completed_and_non_completed_items");

    private static final DatasetItemStatus[] members = { COMPLETED, NONCOMPLETED, COMPLETED_AND_NONCOMPLETED };
    private static List<DatasetItemStatus> list = Arrays.asList(members);

    private DatasetItemStatus(int id, String name, String description) {
        super(id, name, description);
    }

    private DatasetItemStatus() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static DatasetItemStatus get(int id) {
        return (DatasetItemStatus) Term.get(id, list);
    }

    public static ArrayList<DatasetItemStatus> toArrayList() {
        return new ArrayList<DatasetItemStatus>(list);
    }

    public static List<DatasetItemStatus> getList() {
        return list;
    }

    public static void setList(List<DatasetItemStatus> list) {
        DatasetItemStatus.list = list;
    }

    public static DatasetItemStatus getCOMPLETED() {
        return COMPLETED;
    }

    public static DatasetItemStatus getNONCOMPLETED() {
        return NONCOMPLETED;
    }

    public static DatasetItemStatus getCOMPLETEDANDNONCOMPLETED() {
        return COMPLETED_AND_NONCOMPLETED;
    }

    public static DatasetItemStatus[] getMembers() {
        return members;
    }
}
