/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;

/*
 * @author Krikor Krumlian
 */

public class RuleBulkExecuteContainerTwo {

    String expression;
    StudyEventBean studyEvent;
    String studyEventDefinitionName;
    String itemGroupName;
    String itemName;

    public RuleBulkExecuteContainerTwo(String expression, StudyEventBean studyEvent, String studyEventDefinitionName, String itemGroupName, String itemName) {
        super();
        this.expression = expression;
        this.studyEvent = studyEvent;
        this.studyEventDefinitionName = studyEventDefinitionName;
        this.itemGroupName = itemGroupName;
        this.itemName = itemName;
    }

    /**
     * @return the studyEventDefinitionName
     */
    public String getStudyEventDefinitionName() {
        return studyEventDefinitionName;
    }

    /**
     * @param studyEventDefinitionName the studyEventDefinitionName to set
     */
    public void setStudyEventDefinitionName(String studyEventDefinitionName) {
        this.studyEventDefinitionName = studyEventDefinitionName;
    }

    /**
     * @return the itemGroupName
     */
    public String getItemGroupName() {
        return itemGroupName;
    }

    /**
     * @param itemGroupName the itemGroupName to set
     */
    public void setItemGroupName(String itemGroupName) {
        this.itemGroupName = itemGroupName;
    }

    /**
     * @return the itemName
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @param itemName the itemName to set
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (expression == null ? 0 : expression.hashCode());
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
        final RuleBulkExecuteContainerTwo other = (RuleBulkExecuteContainerTwo) obj;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return expression + " : " + studyEvent.getId();
    }

}
