/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

/**
 * @author jxu
 *
 */
public class SubjectGroupMapBean extends AuditableEntityBean {
    // subject_group_map_id serial NOT NULL,
    // study_group_class_id numeric,
    // study_subject_id numeric,
    // study_group_id numeric,
    // status_id numeric,
    // owner_id numeric,
    // date_created date,
    // date_updated date,
    // update_id numeric,
    // notes varchar(255),
    private int studyGroupClassId;
    private int studyGroupId;

    private int studySubjectId;
    private String notes = "";

    private String studyGroupName = "";// not in DB
    private String subjectLabel = "";// not in DB
    private String groupClassName = ""; // not in DB

    /**
     * @return Returns the subjectLabel.
     */
    public String getSubjectLabel() {
        return subjectLabel;
    }

    /**
     * @param subjectLabel
     *            The subjectLabel to set.
     */
    public void setSubjectLabel(String subjectLabel) {
        this.subjectLabel = subjectLabel;
    }

    /**
     * @return Returns the notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes
     *            The notes to set.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return Returns the studyGroupClassId.
     */
    public int getStudyGroupClassId() {
        return studyGroupClassId;
    }

    /**
     * @param studyGroupClassId
     *            The studyGroupClassId to set.
     */
    public void setStudyGroupClassId(int studyGroupClassId) {
        this.studyGroupClassId = studyGroupClassId;
    }

    /**
     * @return Returns the studyGroupId.
     */
    public int getStudyGroupId() {
        return studyGroupId;
    }

    /**
     * @param studyGroupId
     *            The studyGroupId to set.
     */
    public void setStudyGroupId(int studyGroupId) {
        this.studyGroupId = studyGroupId;
    }

    /**
     * @return Returns the studyGroupName.
     */
    public String getStudyGroupName() {
        return studyGroupName;
    }

    /**
     * @param studyGroupName
     *            The studyGroupName to set.
     */
    public void setStudyGroupName(String studyGroupName) {
        this.studyGroupName = studyGroupName;
    }

    /**
     * @return Returns the studySubjectId.
     */
    public int getStudySubjectId() {
        return studySubjectId;
    }

    /**
     * @param studySubjectId
     *            The studySubjectId to set.
     */
    public void setStudySubjectId(int studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    /**
     * @return Returns the groupClassName.
     */
    public String getGroupClassName() {
        return groupClassName;
    }

    /**
     * @param groupClassName
     *            The groupClassName to set.
     */
    public void setGroupClassName(String groupClassName) {
        this.groupClassName = groupClassName;
    }
}
