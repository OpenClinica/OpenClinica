/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.Date;

public class RuleSetRuleAuditBean extends EntityBean {

    RuleSetRuleBean ruleSetRuleBean;
    Status status;
    UserAccountBean updater;
    Date dateUpdated;

    /**
     * @return the ruleSetRuleBean
     */
    public RuleSetRuleBean getRuleSetRuleBean() {
        return ruleSetRuleBean;
    }

    /**
     * @param ruleSetRuleBean the ruleSetRuleBean to set
     */
    public void setRuleSetRuleBean(RuleSetRuleBean ruleSetRuleBean) {
        this.ruleSetRuleBean = ruleSetRuleBean;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the updater
     */
    public UserAccountBean getUpdater() {
        return updater;
    }

    /**
     * @param updater the updater to set
     */
    public void setUpdater(UserAccountBean updater) {
        this.updater = updater;
    }

    /**
     * @return the dateUpdated
     */
    public Date getDateUpdated() {
        return dateUpdated;
    }

    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

}
