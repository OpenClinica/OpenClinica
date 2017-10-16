/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.Arrays;
import java.util.List;

// Internationalized name and description in Term.getName and
// Term.getDescription()

public class EntityAction extends Term {

    public static final EntityAction VIEW = new EntityAction(1, "view");
    public static final EntityAction EDIT = new EntityAction(2, "edit");
    public static final EntityAction DELETE = new EntityAction(3, "delete");
    public static final EntityAction RESTORE = new EntityAction(4, "restore");
    public static final EntityAction DEPLOY = new EntityAction(5, "deploy");

    private static final EntityAction[] members = { VIEW, EDIT, DELETE, RESTORE, DEPLOY };
    public static final List list = Arrays.asList(members);

    private EntityAction(int id, String name) {
        super(id, name);
    }

    private EntityAction() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static EntityAction get(int id) {
        return (EntityAction) Term.get(id, list);
    }
}
