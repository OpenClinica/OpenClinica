/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "rule_set_rule")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "rule_set_rule_id_seq") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RuleSetRuleBean extends AbstractAuditableMutableDomainObject implements Serializable {

    RuleSetBean ruleSetBean;
    RuleBean ruleBean;
    List<RuleActionBean> actions;



    // Transient
    String oid;
    RuleSetRuleBeanImportStatus ruleSetRuleBeanImportStatus;

    public enum RuleSetRuleBeanImportStatus {
        EXACT_DOUBLE, TO_BE_REMOVED, LINE
    }


    @Transient
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

    @Transient
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

    @Transient
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
     * Run the rule and pass in the result. Will return all actions that match the result.
     *
     * @param actionEvaluatesTo
     * @return
     */
    @Transient
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

    /**
     * Run the rule and pass in the result. Will return all actions that match the result.
     *
     * @param actionEvaluatesTo
     * @return
     */
    @Transient
    public List<RuleActionBean> getActions(String ruleEvaluatedTo, Phase phase) {
        List<RuleActionBean> ruleActions = new ArrayList<RuleActionBean>();
        for (RuleActionBean action : actions) {
            String key = action.getExpressionEvaluatesTo().toString();
            if (ruleEvaluatedTo.equals(key) && action.getRuleActionRun().canRun(phase)) {
                ruleActions.add(action);
            }
        }
        return ruleActions;
    }

    @Transient
    public void addAction(RuleActionBean ruleAction) {
        if (actions == null) {
            actions = new ArrayList<RuleActionBean>();
        }
        actions.add(ruleAction);
    }

    @ManyToOne
    @JoinColumn(name = "rule_set_id", nullable = false, updatable = false, insertable = false)
    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    public RuleBean getRuleBean() {
        return ruleBean;
    }

    public void setRuleBean(RuleBean ruleBean) {
        this.ruleBean = ruleBean;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "rule_set_rule_id", nullable = false)
    public List<RuleActionBean> getActions() {
        return actions;
    }

    public void setActions(List<RuleActionBean> actions) {
        this.actions = actions;
    }

    @Transient
    public String getOriginalOid() {
        return oid;
    }

    @Transient
    public String getOid() {
        return this.oid == null && getRuleBean() != null ? getRuleBean().getOid() : oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @Transient
    public RuleSetRuleBeanImportStatus getRuleSetRuleBeanImportStatus() {
        return ruleSetRuleBeanImportStatus;
    }

    public void setRuleSetRuleBeanImportStatus(RuleSetRuleBeanImportStatus ruleSetRuleBeanImportStatus) {
        this.ruleSetRuleBeanImportStatus = ruleSetRuleBeanImportStatus;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (actions == null ? 0 : actions.hashCode());
        result = prime * result + (ruleBean == null ? 0 : ruleBean.hashCode());
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
        RuleSetRuleBean other = (RuleSetRuleBean) obj;
        if (actions == null) {
            if (other.actions != null)
                return false;
        } else {// if (!actions.equals(other.actions))
            if (actions.size() != other.actions.size())
                return false;
            for (RuleActionBean ruleActionBean : other.actions) {
                if (!actions.contains(ruleActionBean))
                    return false;
            }
        }
        if (ruleBean == null) {
            if (other.ruleBean != null)
                return false;
        } else if (!ruleBean.equals(other.ruleBean))
            return false;
        return true;
    }

}
