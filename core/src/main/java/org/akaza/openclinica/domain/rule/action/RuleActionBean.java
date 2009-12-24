/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Krikor Krumlian
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "action_type", discriminatorType = DiscriminatorType.INTEGER)
@Table(name = "rule_action")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_action_id_seq") })
public class RuleActionBean extends AbstractAuditableMutableDomainObject {

    private RuleSetRuleBean ruleSetRule;
    private ActionType actionType;
    private Boolean expressionEvaluatesTo;
    private String summary;
    private String curatedMessage;

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

}