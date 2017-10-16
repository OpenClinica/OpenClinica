/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

/*
 * Created on Oct 26, 2004
 *
 */
package org.akaza.openclinica.bean.admin;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Jun Xu
 */
public class AuditEventBean extends AuditableEntityBean {
    // AUDIT_ID AUDIT_DATE AUDIT_TABLE USER_ID
    // ENTITY_ID REASON_FOR_CHANGE ACTION_MESSAGE

    private Date auditDate;
    private String auditTable = "";
    private int userId = 0;
    private int entityId = 0;
    private String reasonForChange = "";
    private String actionMessage = "";
    private String columnName = "";
    private String oldValue = "";
    private String newValue = "";
    private int updateCount = 0;
    private HashMap changes = new HashMap();
    private HashMap otherInfo = new HashMap();
    private String studyName = "NULL";
    private String subjectName = "NULL";
    private int studyId = 0;
    private int subjectId = 0;
    private ResourceBundle resaudit;

    public AuditEventBean() {
        super();
        this.resaudit = ResourceBundleProvider.getAuditEventsBundle();
    }

    /**
     * @return Returns the auditDate.
     */
    public Date getAuditDate() {
        return auditDate;
    }

    /**
     * @param auditDate
     *            The auditDate to set.
     */
    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    /**
     * @return Returns the auditTable.
     */
    public String getAuditTable() {
        return auditTable;
    }

    /**
     * @param auditTable
     *            The auditTable to set.
     */
    public void setAuditTable(String auditTable) {
        this.auditTable = auditTable;
    }

    /**
     * @return Returns the columnName.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param columnName
     *            The columnName to set.
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
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
     * @return Returns the newValue.
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * @param newValue
     *            The newValue to set.
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /**
     * @return Returns the oldValue.
     */
    public String getOldValue() {
        return oldValue;
    }

    /**
     * @param oldValue
     *            The oldValue to set.
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * @return Returns the internationalized reasonForChange.
     */
    public String getReasonForChange() {
        String rfc;
        try {
            rfc = resaudit.getString(reasonForChange);
        } catch (MissingResourceException mre) {
            rfc = reasonForChange;
        }
        return rfc;
    }

    /**
     * @return Returns the reasonForChange key, should be used when storing the
     *         Audit Event in the database.
     */
    public String getReasonForChangeKey() {
        return reasonForChange;
    }

    /**
     * @param reasonForChange
     *            The reasonForChange to set.
     */
    public void setReasonForChange(String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }

    /**
     * @return Returns the userId.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            The userId to set.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return Returns the updateCount.
     */
    public int getUpdateCount() {
        return userId;
    }

    /**
     * @param updateCount
     *            The updateCount to set.
     */
    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * @return Returns the changes.
     */
    public HashMap getChanges() {
        return changes;
    }

    /**
     * @param changes
     *            The changes to set.
     */
    public void setChanges(HashMap changes) {
        this.changes = changes;
    }

    /**
     * @return Returns the otherInfo.
     */
    public HashMap getOtherInfo() {
        return otherInfo;
    }

    /**
     * @param otherInfo
     *            The otherInfo to set.
     */
    public void setOtherInfo(HashMap otherInfo) {
        this.otherInfo = otherInfo;
    }

    /**
     * @return Returns the internationalized actionMessage.
     */
    public String getActionMessage() {
        String am;
        try {
            am = resaudit.getString(actionMessage);
        } catch (MissingResourceException mre) {
            am = actionMessage;
        }
        return am;
    }

    /**
     * @return Returns the actionMessage key, should be used when storing the
     *         Audit Event in the database.
     */
    public String getActionMessageKey() {
        return actionMessage;
    }

    /**
     * @param actionMessage
     *            The actionMessage to set.
     */
    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    /**
     * @return Returns the studyName.
     */
    public String getStudyName() {
        return studyName;
    }

    /**
     * @param studyName
     *            The studyName to set.
     */
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    /**
     * @return Returns the subjectName.
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * @param subjectName
     *            The subjectName to set.
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    /**
     * @return Returns the subjectId.
     */
    public int getSubjectId() {
        return subjectId;
    }

    /**
     * @param subjectId
     *            The subjectId to set.
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
}
