/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p> RuleSetBean, Holds a collection of Rules & Actions </p>
 * @author Krikor Krumlian
 */
@Entity
@Table(name = "rule_set")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_set_id_seq") })
public class RuleSetBean extends AbstractAuditableMutableDomainObject {

    private StudyEventDefinitionBean studyEventDefinition;
    private StudyBean study;
    private CRFBean crf;
    private CRFVersionBean crfVersion;

    private List<RuleSetRuleBean> ruleSetRules;
    private ExpressionBean target;

    // transient properties
    private List<ExpressionBean> expressions; // itemGroup & item populated when RuleSets are retrieved
    private ItemGroupBean itemGroup;
    private ItemBean item; // originalTarget populated with same value as target.
    private ExpressionBean originalTarget;

    // TODO : Pending conversion of the objects below to use Hibernate
    private Integer studyEventDefinitionId;
    private Integer studyId;
    private Integer crfId;
    private Integer crfVersionId;

    // Business

    @Transient
    public void addRuleSetRule(RuleSetRuleBean ruleSetRuleBean) {
        if (this.ruleSetRules == null)
            this.ruleSetRules = new ArrayList<RuleSetRuleBean>();
        ruleSetRuleBean.setRuleSetBean(this);
        ruleSetRules.add(ruleSetRuleBean);
    }

    @Transient
    public void addRuleSetRule(RuleBean ruleBean) {
        if (this.ruleSetRules == null)
            this.ruleSetRules = new ArrayList<RuleSetRuleBean>();
        RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
        ruleSetRuleBean.setRuleBean(ruleBean);
        ruleSetRuleBean.setRuleSetBean(this);
    }

    @Transient
    public void addRuleSetRules(List<RuleSetRuleBean> ruleSetRuleBeans) {
        if (this.ruleSetRules == null)
            this.ruleSetRules = new ArrayList<RuleSetRuleBean>();

        for (RuleSetRuleBean ruleSetRuleBean : ruleSetRuleBeans) {
            ruleSetRuleBean.setRuleSetBean(this);
        }
        this.ruleSetRules.addAll(ruleSetRuleBeans);
    }

    @Transient
    public void addExpression(ExpressionBean expressionBean) {
        if (this.expressions == null)
            this.expressions = new ArrayList<ExpressionBean>();
        expressions.add(expressionBean);
    }

    @Transient
    public String getStudyEventDefinitionName() {
        return getStudyEventDefinition().getName();
    }

    @Transient
    public String getStudyEventDefinitionNameWithOID() {
        return getStudyEventDefinitionName() + " (" + getStudyEventDefinition().getOid() + ")";
    }

    @Transient
    public String getCrfWithVersionName() {
        String crfVersionName = getCrfVersion() != null ? " - " + getCrfVersion().getName() : "";
        String crfName = getCrf().getName();
        return crfName + crfVersionName;
    }

    @Transient
    public String getCrfWithVersionNameWithOid() {
        String oid = getCrfVersion() != null ? getCrfVersion().getOid() : getCrf().getOid();
        return getCrfWithVersionName() + " (" + oid + ")";
    }

    @Transient
    public int getRuleSetRuleSize() {
        return this.ruleSetRules.size();
    }

    @Transient
    public String getGroupLabel() {
        return getItemGroup().getName();
    }

    @Transient
    public String getItemName() {
        return getItem().getName();
    }

    @Transient
    public String getGroupLabelWithOid() {
        return getGroupLabel() + " (" + getItemGroup().getOid() + ")";
    }

    @Transient
    public String getItemNameWithOid() {
        return getItemName() + " (" + getItem().getOid() + ")";
    }

    // Getters & Setters
    @Transient
    public StudyEventDefinitionBean getStudyEventDefinition() {
        return studyEventDefinition;
    }

    public void setStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        if (studyEventDefinition.getId() > 0) {
            this.studyEventDefinitionId = studyEventDefinition.getId();
        }
        this.studyEventDefinition = studyEventDefinition;
    }

    // @OneToMany(mappedBy = "ruleSetBean")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_set_id", nullable = false)
    public List<RuleSetRuleBean> getRuleSetRules() {
        return ruleSetRules;
    }

    public void setRuleSetRules(List<RuleSetRuleBean> ruleSetRuleAssignment) {
        this.ruleSetRules = ruleSetRuleAssignment;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_expression_id")
    public ExpressionBean getTarget() {
        return target;
    }

    public void setTarget(ExpressionBean target) {
        this.target = target;
    }

    @Transient
    public StudyBean getStudy() {
        return study;
    }

    public void setStudy(StudyBean study) {
        if (study.getId() > 0) {
            this.studyId = study.getId();
        }
        this.study = study;
    }

    @Transient
    public ItemGroupBean getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroupBean itemGroup) {
        this.itemGroup = itemGroup;
    }

    @Transient
    public ItemBean getItem() {
        return item;
    }

    public void setItem(ItemBean item) {
        this.item = item;
    }

    @Transient
    public CRFBean getCrf() {
        return crf;
    }

    public void setCrf(CRFBean crf) {
        if (crf.getId() > 0) {
            this.crfId = crf.getId();
        }
        this.crf = crf;
    }

    @Transient
    public CRFVersionBean getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(CRFVersionBean crfVersion) {
        if (crfVersion != null && crfVersion.getId() > 0) {
            this.crfVersionId = crfVersion.getId();
        }
        this.crfVersion = crfVersion;
    }

    @Transient
    public List<ExpressionBean> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionBean> expressions) {
        this.expressions = expressions;
    }

    /**
     * @return originalTarget
     */
    @Transient
    public ExpressionBean getOriginalTarget() {
        return originalTarget;
    }

    /**
     * @param originalTarget
     */
    public void setOriginalTarget(ExpressionBean originalTarget) {
        this.originalTarget = originalTarget;
    }

    /**
     * @return the studyEventDefinitionId
     */
    public Integer getStudyEventDefinitionId() {
        return studyEventDefinitionId;
    }

    /**
     * @param studyEventDefinitionId the studyEventDefinitionId to set
     */
    public void setStudyEventDefinitionId(Integer studyEventDefinitionId) {
        this.studyEventDefinitionId = studyEventDefinitionId;
    }

    /**
     * @return the studyId
     */
    public Integer getStudyId() {
        return studyId;
    }

    /**
     * @param studyId the studyId to set
     */
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    /**
     * @return the crfId
     */
    public Integer getCrfId() {
        return crfId;
    }

    /**
     * @param crfId the crfId to set
     */
    public void setCrfId(Integer crfId) {
        this.crfId = crfId;
    }

    /**
     * @return the crfVersionId
     */
    public Integer getCrfVersionId() {
        return crfVersionId;
    }

    /**
     * @param crfVersionId the crfVersionId to set
     */
    public void setCrfVersionId(Integer crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

}
