/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.action;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * @author Krikor Krumlian
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "action_type", discriminatorType = DiscriminatorType.INTEGER)
@Table(name = "rule_action")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_action_id_seq") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class RuleActionBean extends AbstractAuditableMutableDomainObject implements Comparable<ActionType> {

    private RuleSetRuleBean ruleSetRule;
    private ActionType actionType;
    private Boolean expressionEvaluatesTo;
    private String summary;
    private String curatedMessage;
    private RuleActionRunBean ruleActionRun;

    @Transient
    public String getCuratedMessage() {
        return curatedMessage;
    }

    public void setCuratedMessage(String curatedMessage) {
        this.curatedMessage = curatedMessage;
    }

    @Transient
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Transient
    public HashMap<String, Object> getPropertiesForDisplay() {
        LinkedHashMap<String, Object> p = new LinkedHashMap<String, Object>();
        p.put("rule_action_type", getActionType());
        return p;
    }

    @Type(type = "actionType")
    @Column(name = "action_type", updatable = false, insertable = false)
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

    @ManyToOne
    @JoinColumn(name = "rule_set_rule_id", nullable = false, updatable = false, insertable = false)
    public RuleSetRuleBean getRuleSetRule() {
        return ruleSetRule;
    }

    public void setRuleSetRule(RuleSetRuleBean ruleSetRule) {
        this.ruleSetRule = ruleSetRule;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_action_run_id")
    public RuleActionRunBean getRuleActionRun() {
        return ruleActionRun;
    }

    public void setRuleActionRun(RuleActionRunBean ruleActionRun) {
        this.ruleActionRun = ruleActionRun;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionType == null) ? 0 : actionType.hashCode());
        result = prime * result + ((expressionEvaluatesTo == null) ? 0 : expressionEvaluatesTo.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RuleActionBean other = (RuleActionBean) obj;
        if (actionType == null) {
            if (other.actionType != null)
                return false;
        } else if (!actionType.equals(other.actionType))
            return false;
        if (expressionEvaluatesTo == null) {
            if (other.expressionEvaluatesTo != null)
                return false;
        } else if (!expressionEvaluatesTo.equals(other.expressionEvaluatesTo))
            return false;
        if (summary == null) {
            if (other.summary != null)
                return false;
        } else if (!summary.equals(other.summary))
            return false;
        if (ruleActionRun == null) {
            if (other.ruleActionRun != null)
                return false;
        } else if (!ruleActionRun.equals(other.ruleActionRun))
            return false;
        return true;
    }

    @Transient
    public int compareTo(ActionType o) {
        // TODO Auto-generated method stub
        return 0;
    }

}
