/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule;

import java.util.HashSet;
import java.util.Set;

/*
 * @author Krikor Krumlian
 */

public class RuleSetBasedViewContainer {

    String ruleName;
    String expression;
    String executeOn;
    String actionType;
    String actionSummary;
    Set<String> subjects;

    public RuleSetBasedViewContainer(String ruleName, String expression, String executeOn, String actionType, String actionSummary) {
        super();
        this.ruleName = ruleName;
        this.expression = expression;
        this.executeOn = executeOn;
        this.actionType = actionType;
        this.actionSummary = actionSummary;
    }

    public void addSubject(String subject) {
        if (subjects == null) {
            subjects = new HashSet<String>();
        }
        subjects.add(subject);
    }

    /**
     * @return the ruleName
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * @param ruleName the ruleName to set
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return the executeOn
     */
    public String getExecuteOn() {
        return executeOn;
    }

    /**
     * @param executeOn the executeOn to set
     */
    public void setExecuteOn(String executeOn) {
        this.executeOn = executeOn;
    }

    /**
     * @return the actionType
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * @param actionType the actionType to set
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * @return the actionSummary
     */
    public String getActionSummary() {
        return actionSummary;
    }

    /**
     * @param actionSummary the actionSummary to set
     */
    public void setActionSummary(String actionSummary) {
        this.actionSummary = actionSummary;
    }

    /**
     * @return the subjects
     */
    public Set<String> getSubjects() {
        return subjects;
    }

    /**
     * @param subjects the subjects to set
     */
    public void setSubjects(Set<String> subjects) {
        this.subjects = subjects;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (actionType == null ? 0 : actionType.hashCode());
        result = prime * result + (executeOn == null ? 0 : executeOn.hashCode());
        result = prime * result + (expression == null ? 0 : expression.hashCode());
        result = prime * result + (ruleName == null ? 0 : ruleName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RuleSetBasedViewContainer other = (RuleSetBasedViewContainer) obj;
        if (actionType == null) {
            if (other.actionType != null)
                return false;
        } else if (!actionType.equals(other.actionType))
            return false;
        if (executeOn == null) {
            if (other.executeOn != null)
                return false;
        } else if (!executeOn.equals(other.executeOn))
            return false;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        if (ruleName == null) {
            if (other.ruleName != null)
                return false;
        } else if (!ruleName.equals(other.ruleName))
            return false;
        return true;
    }

}
