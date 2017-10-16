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
 * Type safe enumeration of study group types
 *
 * @author Jun Xu
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class GroupClassType extends Term {
    public static final GroupClassType INVALID = new GroupClassType(0, "invalid");
    public static final GroupClassType ARM = new GroupClassType(1, "Arm");

    public static final GroupClassType FAMILY = new GroupClassType(2, "Family/Pedigree");

    public static final GroupClassType DEMOGRAPHIC = new GroupClassType(3, "Demographic");

    public static final GroupClassType OTHER = new GroupClassType(4, "Other");

    private static final GroupClassType[] members = { ARM, FAMILY, DEMOGRAPHIC, OTHER };

    public static final List list = Arrays.asList(members);

    private GroupClassType(int id, String name) {
        super(id, name);
    }

    private GroupClassType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static GroupClassType get(int id) {
        Term t = Term.get(id, list);

        if (!t.isActive()) {
            return INVALID;
        } else {
            return (GroupClassType) t;
        }
    }

    public static boolean findByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            GroupClassType temp = (GroupClassType) list.get(i);
            if (temp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static GroupClassType getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            GroupClassType temp = (GroupClassType) list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return GroupClassType.INVALID;
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

}
