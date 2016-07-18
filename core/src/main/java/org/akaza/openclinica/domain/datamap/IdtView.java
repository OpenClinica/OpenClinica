package org.akaza.openclinica.domain.datamap;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "view_item_data_toolkit_filtered")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class IdtView extends DataMapDomainObject {

    private int itemDataId;
    // private Study study;
    private int parentStudyId;
    private int studyId;
    private String studySubjectId;
    private String ssOid;
    private String studyEventDefn;
    private String sedOid;
    private int eventOrdinal;
    private String crfName;
    private String crfOid;
    private String groupName;
    private String groupOid;
    private int groupOrdinal;
    private String itemOid;
    private String leftItemText;
    private String value;
    private int eventCrfId;
    private int eventCrfStatusId;

    private String path;
    private int tagId;
    
    private String itemDataWorkflowStatus;
    
    @Id
    @Column(name = "item_data_id")
    public int getItemDataId() {
        return itemDataId;
    }

    public void setItemDataId(int itemDataId) {
        this.itemDataId = itemDataId;
    }

    @Column(name = "study_event_defn")
    public String getStudyEventDefn() {
        return studyEventDefn;
    }

    public void setStudyEventDefn(String studyEventDefn) {
        this.studyEventDefn = studyEventDefn;
    }

    @Column(name = "crf_name")
    public String getCrfName() {
        return crfName;
    }

    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    @Column(name = "group_name")
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Column(name = "group_ordinal")
    public int getGroupOrdinal() {
        return groupOrdinal;
    }

    public void setGroupOrdinal(int groupOrdinal) {
        this.groupOrdinal = groupOrdinal;
    }

    @Column(name = "left_item_text")
    public String getLeftItemText() {
        return leftItemText;
    }

    public void setLeftItemText(String leftItemText) {
        this.leftItemText = leftItemText;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "study_id")
    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    @Column(name = "parent_study_id")
    public int getParentStudyId() {
        return parentStudyId;
    }

    public void setParentStudyId(int parentStudyId) {
        this.parentStudyId = parentStudyId;
    }

    @Column(name = "event_crf_id")
    public int getEventCrfId() {
        return eventCrfId;
    }

    public void setEventCrfId(int eventCrfId) {
        this.eventCrfId = eventCrfId;
    }

    @Column(name = "event_crf_status_id")
    public int getEventCrfStatusId() {
        return eventCrfStatusId;
    }

    public void setEventCrfStatusId(int eventCrfStatusId) {
        this.eventCrfStatusId = eventCrfStatusId;
    }

    @Column(name = "sed_oid")
    public String getSedOid() {
        return sedOid;
    }

    public void setSedOid(String sedOid) {
        this.sedOid = sedOid;
    }

    @Column(name = "crf_oid")
    public String getCrfOid() {
        return crfOid;
    }

    public void setCrfOid(String crfOid) {
        this.crfOid = crfOid;
    }

    @Column(name = "path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Column(name = "tag_id")
    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    @Column(name = "study_subject_id")
    public String getStudySubjectId() {
        return studySubjectId;
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    @Column(name = "ss_oid")
    public String getSsOid() {
        return ssOid;
    }

    public void setSsOid(String ssOid) {
        this.ssOid = ssOid;
    }

    @Column(name = "event_ordinal")
    public int getEventOrdinal() {
        return eventOrdinal;
    }

    public void setEventOrdinal(int eventOrdinal) {
        this.eventOrdinal = eventOrdinal;
    }

    @Column(name = "group_oid")
    public String getGroupOid() {
        return groupOid;
    }

    public void setGroupOid(String groupOid) {
        this.groupOid = groupOid;
    }

    @Column(name = "item_oid")
    public String getItemOid() {
        return itemOid;
    }

    public void setItemOid(String itemOid) {
        this.itemOid = itemOid;
    }

    @Column(name = "item_data_workflow_status")
    public String getItemDataWorkflowStatus() {
        return itemDataWorkflowStatus;
    }

    public void setItemDataWorkflowStatus(String itemDataWorkflowStatus) {
        this.itemDataWorkflowStatus = itemDataWorkflowStatus;
    }
                        
}
