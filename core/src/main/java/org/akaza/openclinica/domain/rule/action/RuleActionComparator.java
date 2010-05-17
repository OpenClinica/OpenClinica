/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.action;

import java.util.Comparator;
import java.util.HashMap;

/**
 * @author Krikor Krumlian
 */

public class RuleActionComparator implements Comparator<RuleActionBean> {

    HashMap<ActionType, String> order = new HashMap<ActionType, String>();

    public RuleActionComparator() {
        order.put(ActionType.EMAIL, "1");
        order.put(ActionType.FILE_DISCREPANCY_NOTE, "2");
        order.put(ActionType.INSERT, "3");
        order.put(ActionType.SHOW, "4");
        order.put(ActionType.HIDE, "5");

    }

    public int compare(RuleActionBean o1, RuleActionBean o2) {
        return order.get(o1.getActionType()).compareTo(order.get(o2.getActionType()));
    }

}