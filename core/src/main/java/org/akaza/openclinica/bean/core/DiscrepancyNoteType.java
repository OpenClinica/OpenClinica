/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jun Xu
 *
 */
// Internationalized name and description in Term.getName and
// Term.getDescription()
public class DiscrepancyNoteType extends Term {

    public static final DiscrepancyNoteType INVALID = new DiscrepancyNoteType(0, "Invalid", null);

    public static final DiscrepancyNoteType FAILEDVAL = new DiscrepancyNoteType(1, "Failed_Validation_Check", null);

    //public static final DiscrepancyNoteType INCOMPLETE = new DiscrepancyNoteType(2, "Incomplete", null);

    //public static final DiscrepancyNoteType UNCLEAR = new DiscrepancyNoteType(3, "Unclear/Unreadable", null);

    public static final DiscrepancyNoteType ANNOTATION = new DiscrepancyNoteType(2, "Annotation", null);

    public static final DiscrepancyNoteType QUERY = new DiscrepancyNoteType(3, "query", null);

    public static final DiscrepancyNoteType REASON_FOR_CHANGE = new DiscrepancyNoteType(4, "reason_for_change", null);

    //public static final DiscrepancyNoteType OTHER = new DiscrepancyNoteType(5, "Other", null);

    //private static final DiscrepancyNoteType[] members = { FAILEDVAL, INCOMPLETE, UNCLEAR, ANNOTATION, QUERY, REASON_FOR_CHANGE, OTHER };

    private static final DiscrepancyNoteType[] members = { FAILEDVAL, ANNOTATION, QUERY, REASON_FOR_CHANGE };

    public static final List<DiscrepancyNoteType> list = Arrays.asList(members);

    private List privileges;

    private DiscrepancyNoteType(int id, String name, Privilege[] myPrivs) {
        super(id, name);

    }

    private DiscrepancyNoteType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static DiscrepancyNoteType get(int id) {
        return (DiscrepancyNoteType) Term.get(id, list);
    }

    public static DiscrepancyNoteType getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            DiscrepancyNoteType temp = list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return INVALID;
    }

    public boolean hasPrivilege(Privilege p) {
        Iterator it = privileges.iterator();

        while (it.hasNext()) {
            Privilege myPriv = (Privilege) it.next();
            if (myPriv.equals(p)) {
                return true;
            }
        }
        return false;
    }

}
