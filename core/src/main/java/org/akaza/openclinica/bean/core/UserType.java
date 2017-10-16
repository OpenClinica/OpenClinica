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

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class UserType extends Term {

    public static final UserType INVALID = new UserType(0, "invalid");
    public static final UserType SYSADMIN = new UserType(1, "business_administrator");
    public static final UserType USER = new UserType(2, "user");
    public static final UserType TECHADMIN = new UserType(3, "technical_administrator");

    private static final UserType[] members = { INVALID, USER, SYSADMIN, TECHADMIN };
    public static final List list = Arrays.asList(members);

    private UserType(int id, String name) {
        super(id, name);
    }

    private UserType() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static UserType get(int id) {
        return (UserType) Term.get(id, list);
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }
}
