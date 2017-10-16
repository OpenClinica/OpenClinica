/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

/**
 * @author Jun Xu
 *
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class SubjectEventStatus extends Term implements Comparable {
    // waiting for the db to come in sync with our set of terms...
    public static final SubjectEventStatus INVALID = new SubjectEventStatus(0, "invalid");

    public static final SubjectEventStatus SCHEDULED = new SubjectEventStatus(1, "scheduled");

    public static final SubjectEventStatus NOT_SCHEDULED = new SubjectEventStatus(2, "not_scheduled");

    public static final SubjectEventStatus DATA_ENTRY_STARTED = new SubjectEventStatus(3, "data_entry_started");

    public static final SubjectEventStatus COMPLETED = new SubjectEventStatus(4, "completed");

    public static final SubjectEventStatus STOPPED = new SubjectEventStatus(5, "stopped");

    public static final SubjectEventStatus SKIPPED = new SubjectEventStatus(6, "skipped");

    public static final SubjectEventStatus LOCKED = new SubjectEventStatus(7, "locked");

    public static final SubjectEventStatus SIGNED = new SubjectEventStatus(8, "signed");

    private static final SubjectEventStatus[] members = { SCHEDULED, NOT_SCHEDULED, DATA_ENTRY_STARTED, COMPLETED, STOPPED, SKIPPED, SIGNED, LOCKED };

    private static List list = Arrays.asList(members);

    // Solve the problem with the get() method...
    private static final Map<Integer, String> membersMap = new HashMap<Integer, String>();
    static {
        membersMap.put(0, "invalid");
        membersMap.put(1, "scheduled");
        membersMap.put(2, "not_scheduled");
        membersMap.put(3, "data_entry_started");
        membersMap.put(4, "completed");
        membersMap.put(5, "stopped");
        membersMap.put(6, "skipped");
        membersMap.put(7, "locked");
        membersMap.put(8, "signed");
    }

    public boolean isInvalid() {
        return this == SubjectEventStatus.INVALID;
    }

    public boolean isScheduled() {
        return this == SubjectEventStatus.SCHEDULED;
    }

    public boolean isNotScheduled() {
        return this == SubjectEventStatus.NOT_SCHEDULED;
    }

    public boolean isDE_Started() {
        return this == SubjectEventStatus.DATA_ENTRY_STARTED;
    }

    public boolean isCompleted() {
        return this == SubjectEventStatus.COMPLETED;
    }

    public boolean isStopped() {
        return this == SubjectEventStatus.STOPPED;
    }

    public boolean isSkipped() {
        return this == SubjectEventStatus.SKIPPED;
    }

    public boolean isLocked() {
        return this == SubjectEventStatus.LOCKED;
    }

    public boolean isSigned() {
        return this == SubjectEventStatus.SIGNED;
    }

    private SubjectEventStatus(int id, String name) {
        super(id, name);
    }

    private SubjectEventStatus() {
    }

    public static SubjectEventStatus getFromMap(int id) {
        if (id < 0 || id > membersMap.size() - 1) {
            return new SubjectEventStatus(0, "invalid");
        }

        return new SubjectEventStatus(id, membersMap.get(id));
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static SubjectEventStatus get(int id) {
        return (SubjectEventStatus) Term.get(id, list);
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    public int compareTo(Object o) {
        if (!this.getClass().equals(o.getClass())) {
            return 0;
        }

        SubjectEventStatus arg = (SubjectEventStatus) o;

        return name.compareTo(arg.getName());
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> getSubjectEventStatusValues() {
        return membersMap.values();
    }

    public static String getSubjectEventStatusName(int id) {
        if (id < 0 || id > membersMap.size() - 1) {
            return "invalid";
        }

        return membersMap.get(id);
    }

    /**
     * Return an id for a SubjectEventStatus when given a name like "complete."
     *
     * @param name
     *            A String name
     * @return An int id, like 1 for "scheduled"
     */
    public static int getSubjectEventStatusIdByName(String name) {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();

        if (name == null || "".equalsIgnoreCase(name)) {
            return 0;
        }
        boolean validArg = false;

        //String status_name = name.trim().replace(" ", "_").toLowerCase();

        for (String statusName : getSubjectEventStatusValues()) {
            if(resterm.getString(statusName) != null) {
                if (name.equalsIgnoreCase(resterm.getString(statusName).trim())) {
                    validArg = true;
                    break;
                }
            }
        }

        if (!validArg) {
            return 0;
        }

        for (int key : membersMap.keySet()) {
            if (name.equalsIgnoreCase(resterm.getString(getSubjectEventStatusName(key)))) {
                return key;
            }
        }
        return 0;
    }

    public static SubjectEventStatus getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            SubjectEventStatus temp = (SubjectEventStatus) list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return INVALID;
    }

   
}
