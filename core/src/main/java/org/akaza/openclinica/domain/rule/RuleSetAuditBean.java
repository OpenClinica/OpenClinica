/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.akaza.openclinica.domain.Status;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rule_set_audit")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "rule_set_audit_id_seq") })
public class RuleSetAuditBean extends AbstractMutableDomainObject {

    RuleSetBean ruleSetBean;
    Status status;
    UserAccountBean updater;
    Date dateUpdated;

    // TODO: phase out the use of these Once the above beans become Hibernated
    protected Integer updaterId;

    /**
     * @return the ruleSetBean
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_set_id")
    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    /**
     * @param ruleSetBean the ruleSetBean to set
     */
    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }

    @Type(type = "status")
    @Column(name = "status_id")
    public Status getStatus() {
        if (status != null) {
            return status;
        } else
            return Status.AVAILABLE;
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
    @Transient
    public UserAccountBean getUpdater() {
        return updater;
    }

    /**
     * @param updater the updater to set
     */
    public void setUpdater(UserAccountBean updater) {
        this.updater = updater;
        if (updater != null) {
            this.updaterId = updater.getId();
        }
    }

    /**
     * @return the dateUpdated
     */
    @Column(name = "date_updated")
    public Date getDateUpdated() {
        return new Date();
    }

    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Transient
    public Date getCurrentUpdatedDate() {
        return this.dateUpdated;
    }

    /**
     * @return the updaterId
     */
    @Column(name = "updater_id")
    public Integer getUpdaterId() {
        return updaterId;
    }

    /**
     * @param updaterId the updaterId to set
     */
    public void setUpdaterId(Integer updaterId) {
        this.updaterId = updaterId;
    }

}
