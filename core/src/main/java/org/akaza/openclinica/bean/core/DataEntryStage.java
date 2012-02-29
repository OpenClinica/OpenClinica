/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ssachs
 */
// Internationalized name and description in Term.getName and
// Term.getDescription()
public class DataEntryStage extends Term {
    public static final DataEntryStage INVALID = new DataEntryStage(0, "invalid");
    public static final DataEntryStage UNCOMPLETED = new DataEntryStage(1, "not_started", "not_started");
    public static final DataEntryStage INITIAL_DATA_ENTRY = new DataEntryStage(2, "initial_data_entry", "data_being_entered");
    public static final DataEntryStage INITIAL_DATA_ENTRY_COMPLETE = new DataEntryStage(3, "initial_data_entry_complete", "initial_data_entry_completed");
    public static final DataEntryStage DOUBLE_DATA_ENTRY = new DataEntryStage(4, "double_data_entry", "being_validated");
    public static final DataEntryStage DOUBLE_DATA_ENTRY_COMPLETE = new DataEntryStage(5, "data_entry_complete", "validation_completed");
    public static final DataEntryStage ADMINISTRATIVE_EDITING = new DataEntryStage(6, "administrative_editing", "completed");
    public static final DataEntryStage LOCKED = new DataEntryStage(7, "locked", "locked");

    private static final DataEntryStage[] members =
        { UNCOMPLETED, INITIAL_DATA_ENTRY, INITIAL_DATA_ENTRY_COMPLETE, DOUBLE_DATA_ENTRY, DOUBLE_DATA_ENTRY_COMPLETE, ADMINISTRATIVE_EDITING, LOCKED };

    public boolean isInvalid() {
        return this == DataEntryStage.INVALID;
    }

    public boolean isUncompleted() {
        return this == DataEntryStage.UNCOMPLETED;
    }

    public boolean isInitialDE() {
        return this == DataEntryStage.INITIAL_DATA_ENTRY;
    }

    public boolean isInitialDE_Complete() {
        return this == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE;
    }

    public boolean isDoubleDE() {
        return this == DataEntryStage.DOUBLE_DATA_ENTRY;
    }

    public boolean isDoubleDE_Complete() {
        return this == DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE;
    }

    public boolean isAdmin_Editing() {
        return this == DataEntryStage.ADMINISTRATIVE_EDITING;
    }

    public boolean isLocked() {
        return this == DataEntryStage.LOCKED;
    }

    public static final List<DataEntryStage> list = Arrays.asList(members);

    private List privileges;

    private DataEntryStage(int id, String name) {
        super(id, name);
    }

    private DataEntryStage(int id, String name, String description) {
        super(id, name, description);
    }

    private DataEntryStage() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static DataEntryStage get(int id) {
        return (DataEntryStage) Term.get(id, list);
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    public String getNameRaw() {
        return super.name;
    }

    public static DataEntryStage getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            DataEntryStage temp = list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return INVALID;
    }
}
