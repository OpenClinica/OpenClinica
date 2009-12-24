/*
 * Created on Sep 1, 2005
 *
 *
 */
package org.akaza.openclinica.bean.masking;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.HashMap;

/**
 * @author thickerson
 *
 *
 */
public class MaskingBean extends AuditableEntityBean {
    public HashMap ruleMap;// String property_name -> Boolean value
    public String entityName;
    public int entityId;
    public int studyId;
    public int roleId;

    public MaskingBean() {
        ruleMap = new HashMap();
    }

    public HashMap getRuleMap() {
        return this.ruleMap;
    }

    public void setRuleMap(HashMap ruleMap) {
        this.ruleMap = ruleMap;
    }

    /**
     * @return Returns the entityId.
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * @param entityId
     *            The entityId to set.
     */
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    /**
     * @return Returns the entityName.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName
     *            The entityName to set.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return Returns the roleId.
     */
    public int getRoleId() {
        return roleId;
    }

    /**
     * @param roleId
     *            The roleId to set.
     */
    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    /**
     * @return Returns the studyId.
     */
    public int getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            The studyId to set.
     */
    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }
}
