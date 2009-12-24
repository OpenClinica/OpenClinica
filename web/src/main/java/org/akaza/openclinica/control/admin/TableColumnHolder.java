package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.domain.rule.action.RuleActionBean;

import java.util.List;

public class TableColumnHolder {

    String versionName;
    String versionId;
    String ruleName;
    String ruleExpression;
    String ruleSetRuleId;
    List<RuleActionBean> actions;

    // Place holder for items in RuleActionBean
    String executeOnPlaceHolder;
    String actionTypePlaceHolder;
    String actionSummaryPlaceHolder;

    // Place holder for actions on these rows
    String link;

    public TableColumnHolder(String versionName, int versionId, String ruleName, String ruleExpression, List<RuleActionBean> actions, int ruleSetRuleId) {
        super();
        this.versionName = versionName;
        this.versionId = String.valueOf(versionId);
        this.ruleName = ruleName;
        this.ruleExpression = ruleExpression;
        this.actions = actions;
        this.ruleSetRuleId = String.valueOf(ruleSetRuleId);

    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link
     *            the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the actionTypePlaceHolder
     */
    public String getActionTypePlaceHolder() {
        return actionTypePlaceHolder;
    }

    /**
     * @param actionTypePlaceHolder
     *            the actionTypePlaceHolder to set
     */
    public void setActionTypePlaceHolder(String actionTypePlaceHolder) {
        this.actionTypePlaceHolder = actionTypePlaceHolder;
    }

    /**
     * @return the actionSummaryPlaceHolder
     */
    public String getActionSummaryPlaceHolder() {
        return actionSummaryPlaceHolder;
    }

    /**
     * @param actionSummaryPlaceHolder
     *            the actionSummaryPlaceHolder to set
     */
    public void setActionSummaryPlaceHolder(String actionSummaryPlaceHolder) {
        this.actionSummaryPlaceHolder = actionSummaryPlaceHolder;
    }

    /**
     * @return the executeOnPlaceHolder
     */
    public String getExecuteOnPlaceHolder() {
        return executeOnPlaceHolder;
    }

    /**
     * @param executeOnPlaceHolder
     *            the executeOnPlaceHolder to set
     */
    public void setExecuteOnPlaceHolder(String executeOnPlaceHolder) {
        this.executeOnPlaceHolder = executeOnPlaceHolder;
    }

    /**
     * @return the versionName
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * @param versionName
     *            the versionName to set
     */
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    /**
     * @return the ruleName
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * @param ruleName
     *            the ruleName to set
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * @return the ruleExpression
     */
    public String getRuleExpression() {
        return ruleExpression;
    }

    /**
     * @param ruleExpression
     *            the ruleExpression to set
     */
    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    /**
     * @return the actions
     */
    public List<RuleActionBean> getActions() {
        return actions;
    }

    /**
     * @param actions
     *            the actions to set
     */
    public void setActions(List<RuleActionBean> actions) {
        this.actions = actions;
    }

    /**
     * @return the ruleSetRuleId
     */
    public String getRuleSetRuleId() {
        return ruleSetRuleId;
    }

    /**
     * @param ruleSetRuleId
     *            the ruleSetRuleId to set
     */
    public void setRuleSetRuleId(String ruleSetRuleId) {
        this.ruleSetRuleId = ruleSetRuleId;
    }

    /**
     * @return the versionId
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * @param versionId the versionId to set
     */
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

}
