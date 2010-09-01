package org.akaza.openclinica.domain.rule.action;

import java.util.Comparator;
import java.util.HashMap;

public class RuleActionComparator implements Comparator<RuleActionBean> {

    HashMap<ActionType, String> order = new HashMap<ActionType, String>();

    public RuleActionComparator() {

        order.put(ActionType.FILE_DISCREPANCY_NOTE, "1");
        order.put(ActionType.EMAIL, "2");

    }

    public int compare(RuleActionBean o1, RuleActionBean o2) {
        return order.get(o1.getActionType()).compareTo(order.get(o2.getActionType()));
    }

}