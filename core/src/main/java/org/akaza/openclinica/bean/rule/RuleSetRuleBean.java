/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.rule.action.RuleActionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleSetRuleBean extends AuditableEntityBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    RuleSetBean ruleSetBean;
    RuleBean ruleBean;
    List<RuleActionBean> actions;

    // Transient
    String oid;

    public HashMap<String, ArrayList<RuleActionBean>> getAllActionsWithEvaluatesToAsKey() {
        HashMap<String, ArrayList<RuleActionBean>> h = new HashMap<String, ArrayList<RuleActionBean>>();
        for (RuleActionBean action : actions) {
            String key = action.getExpressionEvaluatesTo().toString();
            if (h.containsKey(key)) {
                h.get(key).add(action);
            } else {
                ArrayList<RuleActionBean> a = new ArrayList<RuleActionBean>();
                a.add(action);
                h.put(key, a);
            }
        }
        return h;
    }

    public HashMap<String, ArrayList<RuleActionBean>> getAllActionsWithEvaluatesToAsKey(String actionEvaluatesTo) {
        HashMap<String, ArrayList<RuleActionBean>> h = new HashMap<String, ArrayList<RuleActionBean>>();
        for (RuleActionBean action : actions) {
            String key = action.getExpressionEvaluatesTo().toString();
            if (actionEvaluatesTo == null || actionEvaluatesTo.equals(key)) {
                if (h.containsKey(key)) {
                    h.get(key).add(action);
                } else {
                    ArrayList<RuleActionBean> a = new ArrayList<RuleActionBean>();
                    a.add(action);
                    h.put(key, a);
                }
            }
        }
        return h;
    }

    public HashMap<String, ArrayList<String>> getActionsAsKeyPair(String actionEvaluatesTo) {
        HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        for (RuleActionBean action : actions) {
            String key = action.getExpressionEvaluatesTo().toString();
            if (actionEvaluatesTo.equals(key)) {
                if (h.containsKey(key)) {
                    h.get(key).add(action.getSummary());
                } else {
                    ArrayList<String> a = new ArrayList<String>();
                    a.add(action.getSummary());
                    h.put(key, a);
                }
            }
        }
        return h;
    }

    /**
     * Run the rule and pass in the result. Will return all actions 
     * that match the result. 
     * @param actionEvaluatesTo
     * @return
     */
    public List<RuleActionBean> getActions(String ruleEvaluatedTo) {
        List<RuleActionBean> ruleActions = new ArrayList<RuleActionBean>();
        for (RuleActionBean action : actions) {
            String key = action.getExpressionEvaluatesTo().toString();
            if (ruleEvaluatedTo.equals(key)) {
                ruleActions.add(action);
            }
        }
        return ruleActions;
    }

    public void addAction(RuleActionBean ruleAction) {
        if (actions == null) {
            actions = new ArrayList<RuleActionBean>();
        }
        actions.add(ruleAction);
    }

    // getters & setters
    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }

    public RuleBean getRuleBean() {
        return ruleBean;
    }

    public void setRuleBean(RuleBean ruleBean) {
        this.ruleBean = ruleBean;
    }

    public List<RuleActionBean> getActions() {
        return actions;
    }

    public void setActions(List<RuleActionBean> actions) {
        this.actions = actions;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}