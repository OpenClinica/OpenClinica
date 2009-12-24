/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.Arrays;
import java.util.List;

// note: if you're going to add another member to this class, please update
// Validator accordingly

public class TermType extends Term {
    public static final TermType ENTITY_ACTION = new TermType(1, "entity_action");
    public static final TermType ROLE = new TermType(2, "role");
    public static final TermType STATUS = new TermType(3, "status");
    public static final TermType USER_TYPE = new TermType(4, "user_type");
    public static final TermType NUMERIC_COMPARISON_OPERATOR = new TermType(5, "numeric_comparison_operator");

    private static final TermType[] members = { ENTITY_ACTION, ROLE, STATUS, USER_TYPE, NUMERIC_COMPARISON_OPERATOR };
    private static List list = Arrays.asList(members);

    private TermType(int id, String name) {
        super(id, name);
    }

    private TermType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static TermType get(int id) {
        return (TermType) Term.get(id, list);
    }

    @Override
    public String getName() {
        return name;
    }
}
