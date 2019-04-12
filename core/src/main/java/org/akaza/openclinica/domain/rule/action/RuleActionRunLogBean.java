/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Krikor Krumlian
 */

@Entity
@Table(name = "rule_action_run_log")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "rule_action_run_log_id_seq") })
public class RuleActionRunLogBean extends AbstractMutableDomainObject {

    ActionType actionType;
    ItemDataBean itemDataBean;
    String value;
    String ruleOid;

    // TODO : Pending conversion of the objects below to use Hibernate
    private Integer itemDataId;

    public RuleActionRunLogBean() {
    }

    public RuleActionRunLogBean(ActionType actionType, ItemDataBean itemDataBean, String value, String ruleOid) {
        super();
        this.actionType = actionType;
        this.itemDataBean = itemDataBean;
        this.itemDataId = itemDataBean.getId();
        this.value = value;
        this.ruleOid = ruleOid;
    }

    @Type(type = "actionType")
    @Column(name = "action_type", updatable = false)
    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    @Transient
    public ItemDataBean getItemDataBean() {
        return itemDataBean;
    }

    public void setItemDataBean(ItemDataBean itemDataBean) {
        if (itemDataBean.getId() > 0) {
            this.itemDataId = itemDataBean.getId();
        }
        this.itemDataBean = itemDataBean;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "rule_oc_oid")
    public String getRuleOid() {
        return ruleOid;
    }

    public void setRuleOid(String ruleOid) {
        this.ruleOid = ruleOid;
    }

    public Integer getItemDataId() {
        return itemDataId;
    }

    public void setItemDataId(Integer itemDataId) {
        this.itemDataId = itemDataId;
    }

}