package org.akaza.openclinica.domain.rule.action;

import java.util.Comparator;
import java.util.HashMap;

public class RuleActionComparator implements Comparator<RuleActionBean> {

    HashMap<ActionType, String> order = new HashMap<ActionType, String>();

    public RuleActionComparator() {

        order.put(ActionType.FILE_DISCREPANCY_NOTE, "1");
        order.put(ActionType.EMAIL, "2");
        order.put(ActionType.SHOW, "3");
        order.put(ActionType.HIDE, "4");
        order.put(ActionType.INSERT, "5");
        order.put(ActionType.EVENT, "6");
        order.put(ActionType.NOTIFICATION, "7");
        order.put(ActionType.RANDOMIZE, "8");
    }

    public int compare(RuleActionBean o1, RuleActionBean o2) {
        return order.get(o1.getActionType()).compareTo(order.get(o2.getActionType()));
    }

}