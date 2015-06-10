/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.rule.expression.ExpressionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> RuleSetBean, the object that collects rules associated with study events. </p>
 * @author Krikor Krumlian
 */
public class RuleSetBean extends AuditableEntityBean {

    private static final long serialVersionUID = 1L;

    private StudyEventDefinitionBean studyEventDefinition;
    private StudyBean study;
    private CRFBean crf;
    private CRFVersionBean crfVersion;

    private List<RuleSetRuleBean> ruleSetRules;
    private ExpressionBean target;

    // transient properties
    private List<ExpressionBean> expressions;
    // itemGroup & item populated when RuleSets are retrieved
    private ItemGroupBean itemGroup;
    private ItemBean item;
    // originalTarget populated with same value as target.
    private ExpressionBean originalTarget;

    public void addRuleSetRule(RuleSetRuleBean ruleSetRuleBean) {
        if (this.ruleSetRules == null)
            this.ruleSetRules = new ArrayList<RuleSetRuleBean>();
        ruleSetRuleBean.setRuleSetBean(this);
        ruleSetRules.add(ruleSetRuleBean);
    }

    public void addRuleSetRule(RuleBean ruleBean) {
        if (this.ruleSetRules == null)
            this.ruleSetRules = new ArrayList<RuleSetRuleBean>();
        RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
        ruleSetRuleBean.setRuleBean(ruleBean);
        ruleSetRuleBean.setRuleSetBean(this);
    }

    public void addExpression(ExpressionBean expressionBean) {
        if (this.expressions == null)
            this.expressions = new ArrayList<ExpressionBean>();
        expressions.add(expressionBean);
    }

    public String getStudyEventDefinitionName() {
        return getStudyEventDefinition().getName();
    }

    public String getStudyEventDefinitionNameWithOID() {
        return getStudyEventDefinitionName() + " (" + getStudyEventDefinition().getOid() + ")";
    }

    public String getCrfWithVersionName() {
        String crfVersionName = getCrfVersion() != null ? " - " + getCrfVersion().getName() : "";
        String crfName = getCrf().getName();
        return crfName + crfVersionName;
    }

    public String getCrfWithVersionNameWithOid() {
        String oid = getCrfVersion() != null ? getCrfVersion().getOid() : getCrf().getOid();
        return getCrfWithVersionName() + " (" + oid + ")";
    }

    public int getRuleSetRuleSize() {
        return this.ruleSetRules.size();
    }

    public String getGroupLabel() {
        return getItemGroup().getName();
    }

    public String getItemName() {
        return getItem().getName();
    }

    public String getGroupLabelWithOid() {
        return getGroupLabel() + " (" + getItemGroup().getOid() + ")";
    }

    public String getItemNameWithOid() {
        return getItemName() + " (" + getItem().getOid() + ")";
    }

    // Getters & Setters
    public StudyEventDefinitionBean getStudyEventDefinition() {
        return studyEventDefinition;
    }

    public void setStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        this.studyEventDefinition = studyEventDefinition;
    }

    public List<RuleSetRuleBean> getRuleSetRules() {
        return ruleSetRules;
    }

    public void setRuleSetRules(List<RuleSetRuleBean> ruleSetRuleAssignment) {
        this.ruleSetRules = ruleSetRuleAssignment;
    }

    public ExpressionBean getTarget() {
        return target;
    }

    public void setTarget(ExpressionBean target) {
        this.target = target;
    }

    public StudyBean getStudy() {
        return study;
    }

    public void setStudy(StudyBean study) {
        this.study = study;
    }

    public ItemGroupBean getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroupBean itemGroup) {
        this.itemGroup = itemGroup;
    }

    public ItemBean getItem() {
        return item;
    }

    public void setItem(ItemBean item) {
        this.item = item;
    }

    public CRFBean getCrf() {
        return crf;
    }

    public void setCrf(CRFBean crf) {
        this.crf = crf;
    }

    public CRFVersionBean getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(CRFVersionBean crfVersion) {
        this.crfVersion = crfVersion;
    }

    public List<ExpressionBean> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionBean> expressions) {
        this.expressions = expressions;
    }

    public ExpressionBean getOriginalTarget() {
        return originalTarget;
    }

    public void setOriginalTarget(ExpressionBean originalTarget) {
        this.originalTarget = originalTarget;
    }
    
}
