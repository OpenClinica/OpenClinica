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
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

// Internationalized name and description in Term.getName and
// Term.getDescription()
public class Privilege extends Term {
    public static final Privilege ADMIN = new Privilege(1, "admin");
    public static final Privilege STUDYDIRECTOR = new Privilege(2, "director");
    public static final Privilege INVESTIGATOR = new Privilege(3, "investigator");
    public static final Privilege RESEARCHASSISTANT = new Privilege(4, "ra");
    public static final Privilege MONITOR = new Privilege(5, "monitor");
    public static final Privilege RESEARCHASSISTANT2 = new Privilege(6, "ra2");

    private static final Privilege[] members = { ADMIN, STUDYDIRECTOR, INVESTIGATOR, RESEARCHASSISTANT, MONITOR,RESEARCHASSISTANT2 };
    public static final List list = Arrays.asList(members);

    private Privilege(int id, String name) {
        super(id, name);
    }

    private Privilege() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static Privilege get(int id) {
        return (Privilege) Term.get(id, list);
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }
}
