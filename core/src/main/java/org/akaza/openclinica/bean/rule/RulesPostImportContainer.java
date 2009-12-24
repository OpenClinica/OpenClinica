/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule;

import java.util.ArrayList;
import java.util.HashMap;

public class RulesPostImportContainer {

    private ArrayList<RuleSetBean> ruleSets;
    private ArrayList<RuleBean> ruleDefs;

    private ArrayList<AuditableBeanWrapper<RuleBean>> validRuleDefs = new ArrayList<AuditableBeanWrapper<RuleBean>>();
    private ArrayList<AuditableBeanWrapper<RuleBean>> duplicateRuleDefs = new ArrayList<AuditableBeanWrapper<RuleBean>>();
    private ArrayList<AuditableBeanWrapper<RuleBean>> inValidRuleDefs = new ArrayList<AuditableBeanWrapper<RuleBean>>();

    // Utility lists to help with Rule Import
    private HashMap<String, AuditableBeanWrapper<RuleBean>> validRules = new HashMap<String, AuditableBeanWrapper<RuleBean>>();
    private HashMap<String, AuditableBeanWrapper<RuleBean>> inValidRules = new HashMap<String, AuditableBeanWrapper<RuleBean>>();

    private ArrayList<AuditableBeanWrapper<RuleSetBean>> validRuleSetDefs = new ArrayList<AuditableBeanWrapper<RuleSetBean>>();
    private ArrayList<AuditableBeanWrapper<RuleSetBean>> duplicateRuleSetDefs = new ArrayList<AuditableBeanWrapper<RuleSetBean>>();
    private ArrayList<AuditableBeanWrapper<RuleSetBean>> inValidRuleSetDefs = new ArrayList<AuditableBeanWrapper<RuleSetBean>>();

    private ArrayList<String> validRuleSetExpressionValues = new ArrayList<String>();

    public ArrayList<String> getValidRuleSetExpressionValues() {
        return validRuleSetExpressionValues;
    }

    public void setValidRuleSetExpressionValues(ArrayList<String> validRuleSetExpressionValues) {
        this.validRuleSetExpressionValues = validRuleSetExpressionValues;
    }

    public ArrayList<RuleBean> getRuleDefs() {
        return ruleDefs;
    }

    public void setRuleDefs(ArrayList<RuleBean> ruleDefs) {
        this.ruleDefs = ruleDefs;
    }

    public ArrayList<RuleSetBean> getRuleSets() {
        return ruleSets;
    }

    public void setRuleSets(ArrayList<RuleSetBean> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public ArrayList<AuditableBeanWrapper<RuleBean>> getValidRuleDefs() {
        return validRuleDefs;
    }

    public void setValidRuleDefs(ArrayList<AuditableBeanWrapper<RuleBean>> validruleDefs) {
        this.validRuleDefs = validruleDefs;
    }

    public ArrayList<AuditableBeanWrapper<RuleBean>> getDuplicateRuleDefs() {
        return duplicateRuleDefs;
    }

    public void setDuplicateRuleDefs(ArrayList<AuditableBeanWrapper<RuleBean>> duplicateRuleDefs) {
        this.duplicateRuleDefs = duplicateRuleDefs;
    }

    public ArrayList<AuditableBeanWrapper<RuleBean>> getInValidRuleDefs() {
        return inValidRuleDefs;
    }

    public void setInValidRuleDefs(ArrayList<AuditableBeanWrapper<RuleBean>> inValidRuleDefs) {
        this.inValidRuleDefs = inValidRuleDefs;
    }

    /**
     * @return the validRules
     */
    public HashMap<String, AuditableBeanWrapper<RuleBean>> getValidRules() {
        return validRules;
    }

    /**
     * @param validRules
     *            the validRules to set
     */
    public void setValidRules(HashMap<String, AuditableBeanWrapper<RuleBean>> validRules) {
        this.validRules = validRules;
    }

    /**
     * @return the inValidRules
     */
    public HashMap<String, AuditableBeanWrapper<RuleBean>> getInValidRules() {
        return inValidRules;
    }

    /**
     * @param inValidRules
     *            the inValidRules to set
     */
    public void setInValidRules(HashMap<String, AuditableBeanWrapper<RuleBean>> inValidRules) {
        this.inValidRules = inValidRules;
    }

    /**
     * @return the validRuleSetDefs
     */
    public ArrayList<AuditableBeanWrapper<RuleSetBean>> getValidRuleSetDefs() {
        return validRuleSetDefs;
    }

    /**
     * @param validRuleSetDefs
     *            the validRuleSetDefs to set
     */
    public void setValidRuleSetDefs(ArrayList<AuditableBeanWrapper<RuleSetBean>> validRuleSetDefs) {
        this.validRuleSetDefs = validRuleSetDefs;
    }

    /**
     * @return the duplicateRuleSetDefs
     */
    public ArrayList<AuditableBeanWrapper<RuleSetBean>> getDuplicateRuleSetDefs() {
        return duplicateRuleSetDefs;
    }

    /**
     * @param duplicateRuleSetDefs
     *            the duplicateRuleSetDefs to set
     */
    public void setDuplicateRuleSetDefs(ArrayList<AuditableBeanWrapper<RuleSetBean>> duplicateRuleSetDefs) {
        this.duplicateRuleSetDefs = duplicateRuleSetDefs;
    }

    /**
     * @return the inValidRuleSetDefs
     */
    public ArrayList<AuditableBeanWrapper<RuleSetBean>> getInValidRuleSetDefs() {
        return inValidRuleSetDefs;
    }

    /**
     * @param inValidRuleSetDefs
     *            the inValidRuleSetDefs to set
     */
    public void setInValidRuleSetDefs(ArrayList<AuditableBeanWrapper<RuleSetBean>> inValidRuleSetDefs) {
        this.inValidRuleSetDefs = inValidRuleSetDefs;
    }
}
