/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.Privilege;
import org.akaza.openclinica.bean.core.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jxu
 *
 * Text  free form text entry limited to one line
 *
 * Text Area  free form text area display
 *
 * Single-Select - pick one from a list
 *
 * Multi-Select  pick many from a list
 *
 * Checkbox  selecting multiple options
 *
 * Radio button  selecting one from many options
 *
 * File  for upload of files
 * @deprecated
 */

// Internationalized description in Term.getDescription()
@Deprecated
public class ResponseType extends Term {
    public static final ResponseType TEXT = new ResponseType(1, "text", null);

    public static final ResponseType TEXT_AREA = new ResponseType(2, "text area", null);

    public static final ResponseType SINGLE_SELECT = new ResponseType(3, "single-select", null);

    public static final ResponseType MULTI_SELECT = new ResponseType(4, "multi-select", null);

    public static final ResponseType CHECKBOX = new ResponseType(5, "checkbox", null);

    public static final ResponseType RADIOBUTTON = new ResponseType(6, "radio", null);

    public static final ResponseType FILE = new ResponseType(7, "file", null);

    private static final ResponseType[] members = { TEXT, TEXT_AREA, SINGLE_SELECT, MULTI_SELECT, CHECKBOX, RADIOBUTTON, FILE };

    public static final List list = Arrays.asList(members);

    private List privileges;

    private ResponseType(int id, String name, Privilege[] myPrivs) {
        super(id, name);
    }

    private ResponseType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static ResponseType get(int id) {
        return (ResponseType) Term.get(id, list);
    }

    public static ResponseType getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            ResponseType temp = (ResponseType) list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return new ResponseType();
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

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    @Override
    public String getName() {
        return name;
    }

}
