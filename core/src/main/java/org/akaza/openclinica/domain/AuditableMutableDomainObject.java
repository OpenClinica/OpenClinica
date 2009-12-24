package org.akaza.openclinica.domain;

import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.Date;

public interface AuditableMutableDomainObject extends MutableDomainObject {

    /**
     * @return the createdDate
     */
    public abstract Date getCreatedDate();

    /**
     * @param createdDate the createdDate to set
     */
    public abstract void setCreatedDate(Date createdDate);

    /**
     * @return the updatedDate
     */
    public abstract Date getUpdatedDate();

    /**
     * @param updatedDate the updatedDate to set
     */
    public abstract void setUpdatedDate(Date updatedDate);

    /**
     * @return the owner
     */
    public abstract UserAccountBean getOwner();

    /**
     * @param owner the owner to set
     */
    public abstract void setOwner(UserAccountBean owner);

    /**
     * @return the updater
     */
    public abstract UserAccountBean getUpdater();

    /**
     * @param updater the updater to set
     */
    public abstract void setUpdater(UserAccountBean updater);

    /**
     * @return the status
     */
    public abstract Status getStatus();

    /**
     * @param status the status to set
     */
    public abstract void setStatus(Status status);

}