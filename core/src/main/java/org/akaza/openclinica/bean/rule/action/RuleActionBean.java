/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule.action;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleBean;

/**
 * @author Krikor Krumlian
 */
public class RuleActionBean extends AuditableEntityBean {

    private static final long serialVersionUID = 7019049957184162568L;
    private RuleSetRuleBean ruleSetRule;
    private ActionType actionType;
    private Boolean expressionEvaluatesTo;
    private String summary;
    private String curatedMessage;

    public String getCuratedMessage() {
        return curatedMessage;
    }

    public void setCuratedMessage(String curatedMessage) {
        this.curatedMessage = curatedMessage;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Boolean getExpressionEvaluatesTo() {
        return expressionEvaluatesTo;
    }

    public void setExpressionEvaluatesTo(Boolean ifExpressionEvaluates) {
        this.expressionEvaluatesTo = ifExpressionEvaluates;
    }

    public RuleSetRuleBean getRuleSetRule() {
        return ruleSetRule;
    }

    public void setRuleSetRule(RuleSetRuleBean ruleSetRule) {
        this.ruleSetRule = ruleSetRule;
    }

}