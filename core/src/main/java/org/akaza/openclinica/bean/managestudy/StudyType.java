/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class StudyType extends Term {

    public static final StudyType INVALID = new StudyType(0, "");

    public static final StudyType GENETIC = new StudyType(1, "genetic");

    public static final StudyType NONGENETIC = new StudyType(2, "non_genetic");

    private static final StudyType[] members = { GENETIC, NONGENETIC };

    public static final List list = Arrays.asList(members);

    private StudyType(int id, String name) {
        super(id, name);
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static StudyType get(int id) {
        Term t = Term.get(id, list);
        if (!t.isActive()) {
            return StudyType.INVALID;
        }
        return (StudyType) t;
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

}
