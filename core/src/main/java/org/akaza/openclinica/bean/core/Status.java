/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class Status extends Term implements Comparable {
    // waiting for the db to come in sync with our set of terms...
    public static final Status INVALID = new Status(0, "invalid");
    public static final Status AVAILABLE = new Status(1, "available");
    public static final Status UNAVAILABLE = new Status(2, "unavailable");
    public static final Status PRIVATE = new Status(3, "private");
    public static final Status PENDING = new Status(4, "pending");
    public static final Status DELETED = new Status(5, "removed");
    public static final Status LOCKED = new Status(6, "locked");
    public static final Status AUTO_DELETED = new Status(7, "auto-removed");
    public static final Status SIGNED = new Status(8, "signed");
    public static final Status FROZEN = new Status(9, "frozen");
    public static final Status SOURCE_DATA_VERIFICATION = new Status(10, "source_data_verification");
    public static final Status RESET = new Status(11, "reset");
    public static final Status ARCHIVED = new Status(12, "archived");


    private static final Status[] members =
        { INVALID, AVAILABLE, PENDING, PRIVATE, UNAVAILABLE, LOCKED, DELETED, AUTO_DELETED, SIGNED, FROZEN, SOURCE_DATA_VERIFICATION, RESET, ARCHIVED };
    private static List list = Arrays.asList(members);

    private static final Status[] activeMembers = { AVAILABLE, SIGNED, DELETED, AUTO_DELETED };
    private static List activeList = Arrays.asList(activeMembers);

    private static final Status[] studySubjectDropDownMembers = { AVAILABLE, SIGNED, DELETED, AUTO_DELETED };
    private static List studySubjectDropDownList = Arrays.asList(studySubjectDropDownMembers);

    private static final Status[] subjectDropDownMembers = { AVAILABLE, DELETED };
    private static List subjectDropDownList = Arrays.asList(subjectDropDownMembers);

    private static final Status[] studyUpdateMembers = { PENDING, AVAILABLE, FROZEN, LOCKED };
    private static List studyUpdateMembersList = Arrays.asList(studyUpdateMembers);

    //Solve the problem with the get() method...
    private static final Map<Integer, String> membersMap = new HashMap<Integer, String>();
    static {
        membersMap.put(0, "invalid");
        membersMap.put(1, "available");
        membersMap.put(2, "unavailable");
        membersMap.put(3, "private");
        membersMap.put(4, "pending");
        membersMap.put(5, "removed");
        membersMap.put(6, "locked");
        membersMap.put(7, "auto-removed");
        membersMap.put(8, "signed");
        membersMap.put(9, "frozen");
        membersMap.put(10, "source_data_verification");
        membersMap.put(11, "reset");
    }

    private Status(int id, String name) {
        super(id, name);
    }

    private Status() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static Status get(int id) {
        return (Status) Term.get(id, list);
    }

    public static Status getFromMap(int id) {
        if (id < 0 || id > membersMap.size() - 1) {
            return Status.INVALID;
        }
        return (Status) get(id, list);
    }

    public static Status getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            Status temp = (Status) list.get(i);
            if (temp.getName().equalsIgnoreCase(name)) {
                return temp;
            }
        }
        return INVALID;
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    public static ArrayList toActiveArrayList() {
        return new ArrayList(activeList);
    }

    public static ArrayList toDropDownArrayList() {
        return new ArrayList(studySubjectDropDownList);
    }

    public static ArrayList toStudyUpdateMembersList() {
        return new ArrayList(studyUpdateMembersList);
    }

    public static ArrayList toSubjectDropDownArrayList() {
        return new ArrayList(subjectDropDownList);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (!this.getClass().equals(o.getClass())) {
            return 0;
        }

        Status arg = (Status) o;

        return name.compareTo(arg.getName());

        //
        // int thisInd = list.indexOf(arg);
        // int argInd = list.indexOf(arg);
        //
        // // note that an INVALID status will result in an index of -1;
        // // all other statuses will result in a non-negative index
        // // consider the cases:
        // // 1. thisInd != -1, argInd != -1: compareTo orders the statuses
        // according to their ordering in list (as expected)
        // // 2. thisInd == -1, argInd != -1: thisInd < argInd => compareTo says
        // that INVALID < a normal status, as expected
        // // 3. thisInd != -1, argInd == -1: thisInd > argInd => compareTo says
        // that a normal status > INVALID, as expected
        // // 4. thisInd == -1, argInd == -1: thisInd == argInd => compareTo
        // says that INVALID == INVALID, as expected
        //
        // if (thisInd < argInd) {
        // return -1;
        // }
        // else if (thisInd > argInd) {
        // return 1;
        // }
        // else {
        // return 0;
        // }
    }

    public boolean isInvalid() {
        return this == Status.INVALID;
    }

    public boolean isAvailable() {
        return this == Status.AVAILABLE;
    }

    public boolean isPending() {
        return this == Status.PENDING;
    }

    public boolean isPrivate() {
        return this == Status.PRIVATE;
    }

    public boolean isUnavailable() {
        return this == Status.UNAVAILABLE;
    }

    public boolean isDeleted() {
        return this == Status.DELETED || this == Status.AUTO_DELETED;
    }

    public boolean isLocked() {
        return this == Status.LOCKED;
    }

    public boolean isSigned() {
        return this == Status.SIGNED;
    }

    public boolean isFrozen() {
        return this == Status.FROZEN;
    }

    public boolean isArchived() {
        return this == Status.ARCHIVED;
    }


    /* public static void main(String[] args) {
         int[] nums = {0,1,2,3,4,5,6,7,8,9};
         Status stat;

         for(int tmp : nums){
              stat = (Status) get(tmp);
         }
     }*/

}
