/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.domain.rule.action.RuleActionBean;

import java.util.List;

/*
 * @author Krikor Krumlian
 */

public class RuleBulkExecuteContainer {

    String crfVersion;
    String ruleName;
    String result;
    List<RuleActionBean> actions;

    public RuleBulkExecuteContainer(String crfVersion, RuleBean rule, String result, List<RuleActionBean> actions) {
        super();
        this.crfVersion = crfVersion;
        this.ruleName = rule.getName();
        this.result = result;
        this.actions = actions;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the crfVersion
     */
    public String getCrfVersion() {
        return crfVersion;
    }

    /**
     * @param crfVersion the crfVersion to set
     */
    public void setCrfVersion(String crfVersion) {
        this.crfVersion = crfVersion;
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
     * @return the actions
     */
    public List<RuleActionBean> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List<RuleActionBean> actions) {
        this.actions = actions;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (crfVersion == null ? 0 : crfVersion.hashCode());
        result = prime * result + (this.result == null ? 0 : this.result.hashCode());
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
        final RuleBulkExecuteContainer other = (RuleBulkExecuteContainer) obj;
        if (crfVersion == null) {
            if (other.crfVersion != null)
                return false;
        } else if (!crfVersion.equals(other.crfVersion))
            return false;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        if (ruleName == null) {
            if (other.ruleName != null)
                return false;
        } else if (!ruleName.equals(other.ruleName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return crfVersion + " : " + ruleName;
    }

}
