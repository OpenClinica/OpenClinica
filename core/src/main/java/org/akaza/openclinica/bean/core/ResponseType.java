/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.Arrays;
import java.util.List;

/**
 * @author ssachs
 */
public class ResponseType extends Term {
    public static final ResponseType INVALID = new ResponseType(0, "invalid");
    public static final ResponseType TEXT = new ResponseType(1, "text");

    public static final ResponseType TEXTAREA = new ResponseType(2, "textarea");

    public static final ResponseType CHECKBOX = new ResponseType(3, "checkbox");

    public static final ResponseType FILE = new ResponseType(4, "file");

    public static final ResponseType RADIO = new ResponseType(5, "radio");

    public static final ResponseType SELECT = new ResponseType(6, "single-select");

    public static final ResponseType SELECTMULTI = new ResponseType(7, "multi-select");

    public static final ResponseType CALCULATION = new ResponseType(8, "calculation");

    public static final ResponseType GROUP_CALCULATION = new ResponseType(9, "group-calculation");

    public static final ResponseType INSTANT_CALCULATION = new ResponseType(10, "instant-calculation");

    // public static final ResponseType CODING = new ResponseType(10, "text(non-editable)");// added 03/2009, tbh
    // changed 03/09, from 'coding' to 'text(non-editable)' to accept spreadsheet changes


    private static final ResponseType[] members = { TEXT, TEXTAREA, CHECKBOX, FILE, RADIO, SELECT, SELECTMULTI, CALCULATION, GROUP_CALCULATION, INSTANT_CALCULATION};// , CODING };

    public static final List list = Arrays.asList(members);

    private ResponseType(int id, String name) {
        super(id, name);
    }

    private ResponseType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static ResponseType get(int id) {
        Term t = Term.get(id, list);

        if (!t.isActive()) {
            return TEXT;
        } else {
            return (ResponseType) t;
        }
    }

    public static boolean findByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            ResponseType temp = (ResponseType) list.get(i);
            if (temp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static ResponseType getByName(String name) {
        /*
         * BWP, 08/26/07: made the method more protective and robust, so that it
         * handles problems with parameters such as "text " (note the space
         * within the String).
         */
        if (name == null || name.length() < 1) {
            return ResponseType.INVALID;
        }
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            ResponseType temp = (ResponseType) list.get(i);
            if (temp.getName().equalsIgnoreCase(name)) {
                return temp;
            }
        }
        return ResponseType.INVALID;
    }

    @Override
    public String getName() {
        return name;
    }
}
